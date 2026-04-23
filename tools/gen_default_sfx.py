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


def mix(a: list[float], b: list[float]) -> list[float]:
    n = max(len(a), len(b))
    out = []
    for i in range(n):
        va = a[i] if i < len(a) else 0.0
        vb = b[i] if i < len(b) else 0.0
        out.append(va + vb)
    return out


def soft_pop_kid1() -> list[float]:
    """Bright, very short kid-friendly pop (soft noise + tiny ping)."""
    n1 = noise_burst(0.028, 0.38)
    p1 = sine_tone(920, 0.018, 0.22, "hann")
    return mix(n1, p1 + [0.0] * max(0, len(n1) - len(p1)))


def soft_pop_kid2() -> list[float]:
    """Slightly rounder / warmer pop variant."""
    n1 = noise_burst(0.032, 0.32)
    p1 = sine_tone(740, 0.022, 0.26, "hann")
    p2 = sine_tone(990, 0.014, 0.12, "hann")
    return mix(mix(n1, p1), p2)


def soft_pop_plop() -> list[float]:
    """Gentle 'plop' — softer, a bit longer tail, no harsh attack."""
    n1 = noise_burst(0.045, 0.22)
    down = sine_tone(520, 0.055, 0.18, "exp")
    return mix(n1, down)


def soft_pop_finale() -> list[float]:
    """Mini-finale: soft double-bubble + tiny cheerful lift (still non-aggressive)."""
    a = soft_pop_kid1()
    gap = [0.0] * int(SR * 0.04)
    b = mix(noise_burst(0.03, 0.36), sine_tone(880, 0.02, 0.2, "hann"))
    lift = sine_tone(660, 0.08, 0.14, "hann") + sine_tone(990, 0.1, 0.12, "hann")
    return a + gap + b + lift


def main() -> None:
    root = Path(__file__).resolve().parents[1]
    out_dir = root / "app" / "src" / "main" / "assets" / "audio"

    # Note: We no longer generate Station 2 synthetic pops here.


if __name__ == "__main__":
    main()
