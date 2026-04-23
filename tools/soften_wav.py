"""
Softens WAVs in-place (no external deps):
- Apply gentle one-pole low-pass (reduces clicky highs)
- Apply short fade-out (10–20ms) to remove sharp edges
- Optional gain reduction (dB)

Usage:
  python tools/soften_wav.py path1.wav path2.wav ...
"""

from __future__ import annotations

import math
import struct
import sys
import wave
from pathlib import Path


def db_to_gain(db: float) -> float:
    return 10 ** (db / 20.0)


def clamp_i16(x: int) -> int:
    return max(-32768, min(32767, x))


def soften_pcm16(
    samples: list[int],
    sr: int,
    gain_db: float = -4.5,
    fade_out_ms: float = 15.0,
    lowpass_hz: float = 3500.0,
) -> list[int]:
    g = db_to_gain(gain_db)

    # One-pole LPF: y[n] = y[n-1] + a*(x-y[n-1])
    # a derived from RC filter.
    dt = 1.0 / sr
    rc = 1.0 / (2.0 * math.pi * max(50.0, lowpass_hz))
    a = dt / (rc + dt)

    y = 0.0
    out: list[int] = []
    for s in samples:
        x = (s / 32768.0) * g
        y = y + a * (x - y)
        out.append(clamp_i16(int(round(y * 32768.0))))

    n = len(out)
    fade_n = int(sr * (fade_out_ms / 1000.0))
    fade_n = max(0, min(n, fade_n))
    if fade_n > 0:
        for i in range(n - fade_n, n):
            u = (n - 1 - i) / max(1, fade_n - 1)  # 1..0
            out[i] = clamp_i16(int(round(out[i] * u)))

    return out


def process_file(path: Path) -> None:
    raw = path.read_bytes()

    # Minimal RIFF parser to support PCM16 and IEEE float32.
    if raw[0:4] != b"RIFF" or raw[8:12] != b"WAVE":
        raise RuntimeError(f"{path}: not a RIFF/WAVE file")

    off = 12
    audio_format = None
    n_ch = None
    sr = None
    bits = None
    data_chunk = None

    while off + 8 <= len(raw):
        cid = raw[off : off + 4]
        csz = struct.unpack_from("<I", raw, off + 4)[0]
        cdata = raw[off + 8 : off + 8 + csz]
        off = off + 8 + csz + (csz % 2)  # word align

        if cid == b"fmt ":
            if csz < 16:
                raise RuntimeError(f"{path}: fmt chunk too small")
            audio_format, n_ch, sr = struct.unpack_from("<HHI", cdata, 0)
            bits = struct.unpack_from("<H", cdata, 14)[0]
            fmt_chunk = cdata
        elif cid == b"data":
            data_chunk = cdata

    if audio_format is None or n_ch is None or sr is None or bits is None or data_chunk is None:
        raise RuntimeError(f"{path}: missing fmt/data chunks")

    def decode_pcm_int(bits_per_sample: int) -> list[float]:
        if bits_per_sample == 16:
            total = len(data_chunk) // 2
            ints = struct.unpack("<" + "h" * total, data_chunk[: total * 2])
            return [x / 32768.0 for x in ints]
        if bits_per_sample == 24:
            total = len(data_chunk) // 3
            out = []
            for i in range(total):
                b0 = data_chunk[i * 3 + 0]
                b1 = data_chunk[i * 3 + 1]
                b2 = data_chunk[i * 3 + 2]
                v = b0 | (b1 << 8) | (b2 << 16)
                if v & 0x800000:
                    v -= 0x1000000
                out.append(v / 8388608.0)
            return out
        if bits_per_sample == 32:
            total = len(data_chunk) // 4
            ints = struct.unpack("<" + "i" * total, data_chunk[: total * 4])
            return [x / 2147483648.0 for x in ints]
        raise RuntimeError(f"{path}: unsupported PCM bit depth: {bits_per_sample}")

    # Decode to per-sample float [-1,1], interleaved.
    if audio_format == 1 and bits == 16:
        samples_f = decode_pcm_int(16)
    elif audio_format == 3 and bits == 32:
        total = len(data_chunk) // 4
        flts = struct.unpack("<" + "f" * total, data_chunk[: total * 4])
        samples_f = [max(-1.0, min(1.0, x)) for x in flts]
    elif audio_format == 65534:
        # WAVE_FORMAT_EXTENSIBLE — inspect subformat GUID (last 16 bytes in fmt chunk).
        if csz < 40:
            raise RuntimeError(f"{path}: extensible fmt chunk too small")
        sub = fmt_chunk[24:40]
        pcm_guid = bytes.fromhex("0100000000001000800000aa00389b71")
        flt_guid = bytes.fromhex("0300000000001000800000aa00389b71")
        if sub == pcm_guid:
            samples_f = decode_pcm_int(bits)
        elif sub == flt_guid:
            total = len(data_chunk) // 4
            flts = struct.unpack("<" + "f" * total, data_chunk[: total * 4])
            samples_f = [max(-1.0, min(1.0, x)) for x in flts]
        else:
            raise RuntimeError(f"{path}: unknown extensible subformat guid={sub.hex()}")
    else:
        raise RuntimeError(f"{path}: unsupported WAV format (format={audio_format}, bits={bits})")

    n_frames = len(samples_f) // n_ch
    # Convert to interleaved int16 list.
    data = [clamp_i16(int(round(x * 32767.0))) for x in samples_f[: n_frames * n_ch]]

    # Process per channel.
    ch_out: list[list[int]] = []
    for ch in range(n_ch):
        ch_samples = data[ch::n_ch]
        ch_out.append(soften_pcm16(ch_samples, sr))

    # Re-interleave.
    interleaved: list[int] = []
    for i in range(n_frames):
        for ch in range(n_ch):
            interleaved.append(ch_out[ch][i])

    out_frames = struct.pack("<" + "h" * len(interleaved), *interleaved)
    tmp = path.with_suffix(path.suffix + ".tmp")
    with wave.open(str(tmp), "wb") as w:
        w.setnchannels(n_ch)
        w.setsampwidth(2)
        w.setframerate(sr)
        w.writeframes(out_frames)
    tmp.replace(path)


def main() -> None:
    if len(sys.argv) < 2:
        print("Usage: python tools/soften_wav.py file1.wav file2.wav ...")
        raise SystemExit(2)
    for p in sys.argv[1:]:
        path = Path(p)
        process_file(path)
        print("Softened:", path)


if __name__ == "__main__":
    main()

