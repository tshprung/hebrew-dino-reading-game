"""
Make near-white studio backgrounds transparent on upright egg PNGs.

1) Flood fill from edges through pixels with R,G,B >= flood_threshold.
2) Grow the background mask a few steps: any non-bg pixel touching bg that
   looks like neutral studio gray (high luminance, low saturation, channels
   close to each other) becomes background — removes the faint “square” halo.
"""
from __future__ import annotations

from collections import deque
from pathlib import Path

import numpy as np
from PIL import Image

FLOOD_THRESHOLD = 244
GROW_PASSES = 5
# Pixels bordering transparency that look like leftover studio fill → clear.
GROW_MIN_RGB = 218
GROW_LUMA = 222
GROW_MAX_SAT = 0.14  # (max-min)/max on RGB
FILES = ("egg_white_up.png", "egg_pink_up.png", "egg_cream_up.png")


def flood_background_mask(rgb: np.ndarray, threshold: int) -> np.ndarray:
    h, w = rgb.shape[:2]
    r, g, b = rgb[:, :, 0], rgb[:, :, 1], rgb[:, :, 2]
    m = (r >= threshold) & (g >= threshold) & (b >= threshold)
    vis = np.zeros((h, w), dtype=bool)
    q: deque[tuple[int, int]] = deque()

    for x in range(w):
        for y in (0, h - 1):
            if m[y, x] and not vis[y, x]:
                vis[y, x] = True
                q.append((y, x))
    for y in range(h):
        for x in (0, w - 1):
            if m[y, x] and not vis[y, x]:
                vis[y, x] = True
                q.append((y, x))

    while q:
        y, x = q.popleft()
        for ny, nx in ((y - 1, x), (y + 1, x), (y, x - 1), (y, x + 1)):
            if 0 <= ny < h and 0 <= nx < w and m[ny, nx] and not vis[ny, nx]:
                vis[ny, nx] = True
                q.append((ny, nx))
    return vis


def dilate4(bg: np.ndarray) -> np.ndarray:
    """One step 4-neighbor dilation."""
    h, w = bg.shape
    out = bg.copy()
    out[1:, :] |= bg[:-1, :]
    out[:-1, :] |= bg[1:, :]
    out[:, 1:] |= bg[:, :-1]
    out[:, :-1] |= bg[:, 1:]
    return out


def grow_studio_halo(rgb: np.ndarray, bg: np.ndarray, passes: int) -> np.ndarray:
    """Expand bg into near-neutral bright pixels that touch bg (anti-halo)."""
    rgbf = rgb.astype(np.float32)
    r, g, b = rgbf[:, :, 0], rgbf[:, :, 1], rgbf[:, :, 2]
    mx = np.maximum(np.maximum(r, g), b)
    mn = np.minimum(np.minimum(r, g), b)
    sat = np.where(mx > 1e-3, (mx - mn) / mx, 0.0)
    luma = 0.299 * r + 0.587 * g + 0.114 * b
    studio_like = (mn >= GROW_MIN_RGB) & (luma >= GROW_LUMA) & (sat <= GROW_MAX_SAT)

    mask = bg.copy()
    for _ in range(passes):
        border = dilate4(mask) & ~mask
        mask |= border & studio_like
    return mask


def process_png(path: Path) -> None:
    im = Image.open(path).convert("RGBA")
    arr = np.array(im)
    rgb = arr[:, :, :3].astype(np.uint8)
    bg = flood_background_mask(rgb, FLOOD_THRESHOLD)
    bg = grow_studio_halo(rgb, bg, GROW_PASSES)
    out = arr.copy()
    out[:, :, 3] = np.where(bg, 0, 255)
    Image.fromarray(out, mode="RGBA").save(path, optimize=True)
    bg_pct = 100.0 * float(bg.mean())
    cy, cx = bg.shape[0] // 2, bg.shape[1] // 2
    fg_center = bool(bg[cy, cx])
    print(f"{path.name}: transparent ~{bg_pct:.1f}%, center_is_bg={fg_center}")


def main() -> None:
    root = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res" / "drawable-nodpi"
    for name in FILES:
        p = root / name
        if not p.is_file():
            raise SystemExit(f"Missing {p}")
        process_png(p)


if __name__ == "__main__":
    main()
