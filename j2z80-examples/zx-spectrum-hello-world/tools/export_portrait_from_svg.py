#!/usr/bin/env python3
"""Export Portrait.java DATA from iam_line.svg path coordinates.

Uses SVG path geometry directly (cubic Beziers sampled to polylines).
Packed format: [n, x0, y0, ...] with n=0 point, n=1 line, n>1 polyline segments.
SVG y=0 top → Spectrum y=0 bottom; image centered on 256x192.
"""

from __future__ import annotations

import math
import re
from pathlib import Path
from xml.etree import ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SVG = ROOT / "iam_line.svg"
OUT_JAVA = ROOT / "src/main/java/com/igormaznitsa/test/helloworld/Portrait.java"
PREVIEW = ROOT / "tools" / "portrait_spectrum_preview.png"

SCREEN_W = 256
SCREEN_H = 192
SVG_W = 256.0
SVG_H = 128.0
BEZIER_STEPS = 8
MAX_BYTES = 2500
DP_EPSILON = 0.85
MIN_SEGMENT_LEN = 0.75

TOKEN_RE = re.compile(
    r"([MmZzLlHhVvCcSsQqTtAa])|([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)"
)


def local_name(tag: str) -> str:
    return tag.split("}")[-1]


def parse_fill_luminance(fill: str) -> float | None:
    fill = fill.strip().lower()
    if not fill or fill == "none":
        return None
    if fill.startswith("#"):
        hex_color = fill[1:]
        if len(hex_color) == 3:
            hex_color = "".join(ch * 2 for ch in hex_color)
        if len(hex_color) != 6:
            return None
        r = int(hex_color[0:2], 16)
        g = int(hex_color[2:4], 16)
        b = int(hex_color[4:6], 16)
        return (r + g + b) / 3.0
    return None


def tokenize(path_d: str):
    for match in TOKEN_RE.finditer(path_d.replace(",", " ")):
        command, number = match.groups()
        if command:
            yield ("cmd", command)
        else:
            yield ("num", float(number))


def cubic_point(p0, p1, p2, p3, t: float):
    u = 1.0 - t
    return (
        u * u * u * p0[0] + 3 * u * u * t * p1[0] + 3 * u * t * t * p2[0] + t * t * t * p3[0],
        u * u * u * p0[1] + 3 * u * u * t * p1[1] + 3 * u * t * t * p2[1] + t * t * t * p3[1],
    )


def sample_cubic(p0, p1, p2, p3, steps: int = BEZIER_STEPS):
    return [cubic_point(p0, p1, p2, p3, i / steps) for i in range(1, steps + 1)]


def path_to_subpaths(path_d: str):
    tokens = list(tokenize(path_d))
    index = 0
    command = None
    current = (0.0, 0.0)
    start = (0.0, 0.0)
    last_cubic_ctrl = None
    subpaths: list[list[tuple[float, float]]] = []
    points: list[tuple[float, float]] = []

    def read_nums(count: int):
        nonlocal index
        values = []
        while len(values) < count and index < len(tokens):
            kind, value = tokens[index]
            if kind != "num":
                break
            values.append(value)
            index += 1
        if len(values) != count:
            raise ValueError(f"expected {count} numbers, got {values}")
        return values

    def append_point(point):
        if not points or points[-1] != point:
            points.append(point)

    while index < len(tokens):
        kind, value = tokens[index]
        if kind == "cmd":
            command = value
            index += 1
            if command in "Zz":
                if points:
                    if points[0] != points[-1]:
                        points.append(points[0])
                    subpaths.append(points)
                    points = []
                current = start
                last_cubic_ctrl = None
            continue

        if command is None:
            raise ValueError("number before command")

        relative = command.islower()
        op = command.upper()

        if op == "M":
            if points:
                subpaths.append(points)
                points = []
            x, y = read_nums(2)
            current = (current[0] + x, current[1] + y) if relative else (x, y)
            start = current
            append_point(current)
            last_cubic_ctrl = None
            command = "l" if relative else "L"
            while index < len(tokens) and tokens[index][0] == "num":
                x, y = read_nums(2)
                current = (current[0] + x, current[1] + y) if relative else (x, y)
                append_point(current)
        elif op == "L":
            while index < len(tokens) and tokens[index][0] == "num":
                x, y = read_nums(2)
                current = (current[0] + x, current[1] + y) if relative else (x, y)
                append_point(current)
                last_cubic_ctrl = None
        elif op == "H":
            while index < len(tokens) and tokens[index][0] == "num":
                x = read_nums(1)[0]
                current = (current[0] + x if relative else x, current[1])
                append_point(current)
                last_cubic_ctrl = None
        elif op == "V":
            while index < len(tokens) and tokens[index][0] == "num":
                y = read_nums(1)[0]
                current = (current[0], current[1] + y if relative else y)
                append_point(current)
                last_cubic_ctrl = None
        elif op == "C":
            while index < len(tokens) and tokens[index][0] == "num":
                nums = read_nums(6)
                if relative:
                    c1 = (current[0] + nums[0], current[1] + nums[1])
                    c2 = (current[0] + nums[2], current[1] + nums[3])
                    end = (current[0] + nums[4], current[1] + nums[5])
                else:
                    c1 = (nums[0], nums[1])
                    c2 = (nums[2], nums[3])
                    end = (nums[4], nums[5])
                for point in sample_cubic(current, c1, c2, end):
                    append_point(point)
                last_cubic_ctrl = c2
                current = end
        elif op == "S":
            while index < len(tokens) and tokens[index][0] == "num":
                nums = read_nums(4)
                if last_cubic_ctrl is None:
                    c1 = current
                else:
                    c1 = (2 * current[0] - last_cubic_ctrl[0], 2 * current[1] - last_cubic_ctrl[1])
                if relative:
                    c2 = (current[0] + nums[0], current[1] + nums[1])
                    end = (current[0] + nums[2], current[1] + nums[3])
                else:
                    c2 = (nums[0], nums[1])
                    end = (nums[2], nums[3])
                for point in sample_cubic(current, c1, c2, end):
                    append_point(point)
                last_cubic_ctrl = c2
                current = end
        elif op == "Q":
            while index < len(tokens) and tokens[index][0] == "num":
                nums = read_nums(4)
                if relative:
                    ctrl = (current[0] + nums[0], current[1] + nums[1])
                    end = (current[0] + nums[2], current[1] + nums[3])
                else:
                    ctrl = (nums[0], nums[1])
                    end = (nums[2], nums[3])
                c1 = (current[0] + 2.0 / 3.0 * (ctrl[0] - current[0]), current[1] + 2.0 / 3.0 * (ctrl[1] - current[1]))
                c2 = (end[0] + 2.0 / 3.0 * (ctrl[0] - end[0]), end[1] + 2.0 / 3.0 * (ctrl[1] - end[1]))
                for point in sample_cubic(current, c1, c2, end):
                    append_point(point)
                last_cubic_ctrl = None
                current = end
        elif op == "T":
            raise ValueError("T command not expected in this SVG")
        elif op == "A":
            raise ValueError("A command not expected in this SVG")
        else:
            raise ValueError(f"unsupported command {command}")

    if points:
        subpaths.append(points)
    return subpaths


def perp_dist(point, start, end):
    ax, ay = start
    bx, by = end
    px, py = point
    dx, dy = bx - ax, by - ay
    if dx == 0 and dy == 0:
        return math.hypot(px - ax, py - ay)
    t = max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / float(dx * dx + dy * dy)))
    return math.hypot(px - (ax + t * dx), py - (ay + t * dy))


def douglas_peucker(points, epsilon):
    if len(points) < 3:
        return list(points)
    start, end = points[0], points[-1]
    index, farthest = 0, 0.0
    for i in range(1, len(points) - 1):
        distance = perp_dist(points[i], start, end)
        if distance > farthest:
            index, farthest = i, distance
    if farthest > epsilon:
        left = douglas_peucker(points[: index + 1], epsilon)
        right = douglas_peucker(points[index:], epsilon)
        return left[:-1] + right
    return [points[0], points[-1]]


def polyline_length(points):
    return sum(math.hypot(points[i][0] - points[i - 1][0], points[i][1] - points[i - 1][1]) for i in range(1, len(points)))


def bbox_size(points):
    xs = [p[0] for p in points]
    ys = [p[1] for p in points]
    return max(xs) - min(xs), max(ys) - min(ys)


def rounded_centroid(points):
    cx = sum(p[0] for p in points) / len(points)
    cy = sum(p[1] for p in points) / len(points)
    return int(round(cx)), int(round(cy))


def is_tiny_closed_blob(points):
    if len(points) < 2:
        return False
    width, height = bbox_size(points)
    return max(width, height) <= 2 and polyline_length(points) <= 8


def to_spectrum(x: float, y: float):
    sx = int(round(max(0.0, min(SVG_W - 1.0, x))))
    sy_img = int(round(max(0.0, min(SVG_H - 1.0, y))))
    offset_y = (SCREEN_H - int(SVG_H)) // 2
    sy = (SCREEN_H - 1) - (offset_y + sy_img)
    return sx, max(0, min(SCREEN_H - 1, sy))


def dedupe(points):
    out = []
    for point in points:
        if not out or out[-1] != point:
            out.append(point)
    return out


def command_size(points) -> int:
    return 3 if len(points) == 1 else 1 + 2 * len(points)


def pack_commands(strokes):
    packed = []
    for stroke in strokes:
        if len(stroke) == 1:
            packed.extend([0, stroke[0][0], stroke[0][1]])
            continue
        n = len(stroke) - 1
        if n > 255:
            raise ValueError(f"stroke too long: {len(stroke)}")
        packed.append(n)
        for x, y in stroke:
            packed.extend([x, y])
    return packed


def byte_literal(value: int) -> str:
    return f"(byte){value}"


def write_portrait(packed):
    draw_body = '''  public static void draw() {
    int i = 0;
    while (i < DATA.length) {
      int n = DATA[i] & 0xFF;
      i++;
      int x0 = DATA[i] & 0xFF;
      int y0 = (DATA[i + 1] & 0xFF);
      i += 2;
      if (n == 0) {
        line(x0, y0, x0, y0);
        continue;
      }
      int px = x0, py = y0;
      for (int seg = 0; seg < n; seg++) {
        int x = DATA[i] & 0xFF;
        int y = (DATA[i + 1] & 0xFF);
        i += 2;
        line(px, py, x, y);
        px = x;
        py = y;
      }
    }
  }

  private Portrait() {
  }

  public static void line(final int x0, final int y0, final int x1, final int y1) {
    int x = x0;
    int y = y0;
    int dx = x1 - x0;
    int dy = y1 - y0;
    int stepX = 1;
    int stepY = 1;

    if (dx < 0) {
      dx = -dx;
      stepX = -1;
    }
    if (dy < 0) {
      dy = -dy;
      stepY = -1;
    }

    int error = dx - dy;

    while (true) {
      Screen.plot(x, y);
      if (x == x1 && y == y1) {
        return;
      }

      int doubled = error + error;
      if (doubled > -dy) {
        error = error - dy;
        x = x + stepX;
      }
      if (doubled < dx) {
        error = error + dx;
        y = y + stepY;
      }
    }
  }
}
'''
    body = [
        "package com.igormaznitsa.test.helloworld;",
        "",
        "import j2z80.spectrum.Screen;",
        "",
        "public class Portrait {",
        "",
        "  // [n, x0, y0, ...] from iam_line.svg; n=0 point, n=1 line, n>1 polyline",
        "  public static final byte[] DATA = {",
    ]
    row = []
    for value in packed:
        row.append(byte_literal(int(value)))
        if len(row) == 20:
            body.append("      " + ", ".join(row) + ",")
            row = []
    if row:
        body.append("      " + ", ".join(row))
    if body[-1].endswith(","):
        body[-1] = body[-1][:-1]
    body.append("  };")
    body.append("")
    body.append(draw_body.rstrip())
    body.append("")
    OUT_JAVA.write_text("\n".join(body))


def write_preview(strokes):
    try:
        from PIL import Image, ImageDraw
    except ImportError:
        return
    preview = Image.new("RGB", (SCREEN_W, SCREEN_H), (255, 255, 255))
    draw = ImageDraw.Draw(preview)
    for stroke in strokes:
        if len(stroke) == 1:
            x, y = stroke[0]
            draw.point((x, SCREEN_H - 1 - y), fill=0)
            continue
        for i in range(1, len(stroke)):
            x0, y0 = stroke[i - 1]
            x1, y1 = stroke[i]
            draw.line([(x0, SCREEN_H - 1 - y0), (x1, SCREEN_H - 1 - y1)], fill=0, width=1)
    preview.save(PREVIEW)


def load_dark_subpaths():
    tree = ET.parse(SVG)
    root = tree.getroot()
    strokes = []
    for element in root.iter():
        if local_name(element.tag) != "path":
            continue
        luminance = parse_fill_luminance(element.attrib.get("fill", ""))
        if luminance is None or luminance > 80:
            continue
        for subpath in path_to_subpaths(element.attrib.get("d", "")):
            strokes.append(subpath)
    return strokes


def main():
    raw = load_dark_subpaths()
    strokes = []
    for subpath in raw:
        mapped = dedupe(to_spectrum(x, y) for x, y in subpath)
        mapped = douglas_peucker(mapped, DP_EPSILON)
        mapped = dedupe(mapped)
        if not mapped:
            continue
        if len(mapped) == 1:
            strokes.append(mapped)
            continue
        if is_tiny_closed_blob(mapped):
            strokes.append([rounded_centroid(mapped)])
            continue
        if polyline_length(mapped) < MIN_SEGMENT_LEN:
            strokes.append([mapped[0]])
            continue
        strokes.append(mapped)

    strokes.sort(key=lambda s: polyline_length(s) if len(s) > 1 else 0.0, reverse=True)

    kept = []
    used = 0
    for stroke in strokes:
        size = command_size(stroke)
        if used + size > MAX_BYTES:
            continue
        kept.append(stroke)
        used += size

    packed = pack_commands(kept)
    write_portrait(packed)
    write_preview(kept)

    n_point = sum(1 for s in kept if len(s) == 1)
    n_line = sum(1 for s in kept if len(s) == 2)
    n_poly = sum(1 for s in kept if len(s) > 2)
    print(
        f"wrote {OUT_JAVA} from {SVG.name}: {len(packed)} bytes, "
        f"{n_point} points, {n_line} lines, {n_poly} polylines "
        f"(raw dark subpaths {len(raw)})"
    )


if __name__ == "__main__":
    main()
