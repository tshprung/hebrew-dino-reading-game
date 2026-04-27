import argparse
import os
import wave
from dataclasses import dataclass


@dataclass(frozen=True)
class WavPcm:
    nchannels: int
    sampwidth: int
    framerate: int
    nframes: int
    comptype: str
    compname: str
    frames: bytes


def read_wav(path: str) -> WavPcm:
    with wave.open(path, "rb") as w:
        params = w.getparams()
        frames = w.readframes(params.nframes)
        return WavPcm(
            nchannels=params.nchannels,
            sampwidth=params.sampwidth,
            framerate=params.framerate,
            nframes=params.nframes,
            comptype=params.comptype,
            compname=params.compname,
            frames=frames,
        )


def write_wav(path: str, wav: WavPcm, frames: bytes) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with wave.open(path, "wb") as w:
        w.setnchannels(wav.nchannels)
        w.setsampwidth(wav.sampwidth)
        w.setframerate(wav.framerate)
        w.writeframes(frames)


def max_abs_sample(frame_bytes: bytes, sampwidth: int) -> int:
    # Returns max abs sample across all channels for one frame.
    if sampwidth == 1:
        # unsigned 8-bit PCM
        return max(abs(b - 128) for b in frame_bytes)
    if sampwidth == 2:
        # signed 16-bit little-endian PCM
        m = 0
        for i in range(0, len(frame_bytes), 2):
            s = int.from_bytes(frame_bytes[i : i + 2], "little", signed=True)
            a = abs(s)
            if a > m:
                m = a
        return m
    if sampwidth == 3:
        # signed 24-bit little-endian PCM
        m = 0
        for i in range(0, len(frame_bytes), 3):
            b0, b1, b2 = frame_bytes[i], frame_bytes[i + 1], frame_bytes[i + 2]
            v = b0 | (b1 << 8) | (b2 << 16)
            if v & 0x800000:
                v -= 0x1000000
            a = abs(v)
            if a > m:
                m = a
        return m
    if sampwidth == 4:
        # signed 32-bit little-endian PCM
        m = 0
        for i in range(0, len(frame_bytes), 4):
            s = int.from_bytes(frame_bytes[i : i + 4], "little", signed=True)
            a = abs(s)
            if a > m:
                m = a
        return m
    raise ValueError(f"Unsupported sample width: {sampwidth} bytes")


def trim_indices(wav: WavPcm, threshold: int, pad_ms: int) -> tuple[int, int]:
    frame_size = wav.nchannels * wav.sampwidth
    if frame_size <= 0:
        return 0, wav.nframes
    pad_frames = int((pad_ms / 1000.0) * wav.framerate)

    first = None
    last = None

    for i in range(wav.nframes):
        off = i * frame_size
        frame = wav.frames[off : off + frame_size]
        if max_abs_sample(frame, wav.sampwidth) > threshold:
            first = i
            break

    if first is None:
        # all silence; keep minimal (0 frames)
        return 0, 0

    for i in range(wav.nframes - 1, -1, -1):
        off = i * frame_size
        frame = wav.frames[off : off + frame_size]
        if max_abs_sample(frame, wav.sampwidth) > threshold:
            last = i
            break

    assert last is not None
    start = max(0, first - pad_frames)
    end = min(wav.nframes, last + pad_frames + 1)
    return start, end


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--in_dir", required=True)
    ap.add_argument("--out_dir", required=True)
    ap.add_argument("--glob_prefix", default="word_w_mem_")
    ap.add_argument("--threshold", type=int, default=800)  # for 16-bit, ~2.4%
    ap.add_argument("--pad_ms", type=int, default=20)
    args = ap.parse_args()

    in_dir = args.in_dir
    out_dir = args.out_dir
    os.makedirs(out_dir, exist_ok=True)

    files = [
        f
        for f in os.listdir(in_dir)
        if f.lower().endswith(".wav") and f.lower().startswith(args.glob_prefix.lower())
    ]
    files.sort()

    if not files:
        print(f"No files matching {args.glob_prefix}*.wav in {in_dir}")
        return 1

    for fname in files:
        in_path = os.path.join(in_dir, fname)
        out_path = os.path.join(out_dir, fname)
        wav = read_wav(in_path)
        start, end = trim_indices(wav, threshold=args.threshold, pad_ms=args.pad_ms)
        frame_size = wav.nchannels * wav.sampwidth
        trimmed = wav.frames[start * frame_size : end * frame_size]
        write_wav(out_path, wav, trimmed)
        dur_in = wav.nframes / wav.framerate if wav.framerate else 0.0
        dur_out = (end - start) / wav.framerate if wav.framerate else 0.0
        print(f"{fname}: {dur_in:.3f}s -> {dur_out:.3f}s (trim {start}..{end} frames)")

    print(f"Done. Wrote {len(files)} files to {out_dir}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

