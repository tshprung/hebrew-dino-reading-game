"""Generate short mono 44.1kHz 16-bit PCM WAVs for kid-game SFX (no external deps)."""
from __future__ import annotations

import math
import struct
import wave
from pathlib import Path

SR = 44100


def write_wav(path: Path, samples: list[float]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(path), "w") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        for x in samples:
            xi = int(max(-1.0, min(1.0, x)) * 32767)
            w.writeframes(struct.pack("<h", xi))


def sine_tone(freq: float, seconds: float, amp: float, envelope: str = "hann") -> list[float]:
    n = int(SR * seconds)
    out: list[float] = []
    for i in range(n):
        t = i / SR
        ph = 2 * math.pi * freq * t
        env = 1.0
        if envelope == "hann":
            env = 0.5 * (1 - math.cos(2 * math.pi * i / max(1, n - 1)))
        elif envelope == "exp":
            env = math.exp(-4.5 * t / seconds)
        out.append(amp * env * math.sin(ph))
    return out


def noise_burst(seconds: float, amp: float) -> list[float]:
    import random

    rng = random.Random(42)
    n = int(SR * seconds)
    out: list[float] = []
    for i in range(n):
        u = i / max(1, n - 1)
        env = math.sin(math.pi * u) ** 1.4
        out.append(amp * env * (rng.random() * 2 - 1))
    return out


def main() -> None:
    root = Path(__file__).resolve().parents[1]
    out_dir = root / "app" / "src" / "main" / "assets" / "audio"

    # Correct: short bright two-tone “ding”
    a = sine_tone(784, 0.07, 0.42, "hann")
    b = sine_tone(1047, 0.09, 0.38, "hann")
    write_wav(out_dir / "sfx_correct.wav", a + b)

    # Wrong: short low “bonk”
    c = sine_tone(180, 0.12, 0.55, "exp")
    d = sine_tone(140, 0.10, 0.35, "exp")
    write_wav(out_dir / "sfx_wrong.wav", c + d)

    # Balloon pop: very short noise + tiny click
    e = noise_burst(0.035, 0.55)
    f = sine_tone(1200, 0.012, 0.25, "hann")
    write_wav(out_dir / "sfx_pop.wav", e + f)

    print("Wrote:", out_dir / "sfx_correct.wav")
    print("Wrote:", out_dir / "sfx_wrong.wav")
    print("Wrote:", out_dir / "sfx_pop.wav")


if __name__ == "__main__":
    main()
