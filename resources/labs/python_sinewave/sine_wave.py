#!/usr/bin/env python3
"""
Generate PNG images containing sine waves
"""

import math
from typing import Tuple

from PIL import Image


def generate_sine_wave(img: Image, offset: Tuple[int, int], size: int, amplitude: float):
    """
    Generate sine wave image
    """

    for x in range(size):
        y = round(math.sin(2 * math.pi / size * x) * amplitude) + (size // 2)
        y = min(size - 1, y)
        transparency_start = 250
        transparency_modifier = 0.6

        transparency = transparency_start
        color = [0xff, 0xff, 0xff, transparency]
        img.putpixel((x, y), tuple(color))

        for y_offset in range(1, 3):
            transparency *= transparency_modifier
            color[3] = round(transparency)
            img.putpixel((x, y - y_offset), tuple(color))
            img.putpixel((x, y + y_offset), tuple(color))


def _main():
    width = 64
    max_amplitude = width / 4
    bgcolor = (0, 0, 0, 0)  # full transparency
    offset = (0, 0)

    # Not actually doing animation yet
    frame_count = 1
    img = Image.new('RGBA', (width, width * frame_count), bgcolor)
    for frame in range(frame_count):
        offset = (0, frame * width)
        generate_sine_wave(img, offset, width, max_amplitude)

    img.save('sine.png')


if __name__ == "__main__":
    _main()
