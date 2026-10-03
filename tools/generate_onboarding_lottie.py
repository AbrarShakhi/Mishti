#!/usr/bin/env python3
"""Generates Mishti's onboarding Lottie animations.

Every layer is named after the Material colour role it should take ("primary",
"tertiaryContainer", ...); the app recolours them at runtime from the current scheme, so the
placeholder colours below only matter when a file is opened outside the app.

Each file carries two markers: "intro" (shapes arrive) and "loop" (an idle cycle whose first
and last frames match, so it repeats without a seam).

Layers are added front to back: Lottie draws the first layer in the list on top.

Usage, from the repository root:
    python3 tools/generate_onboarding_lottie.py app/src/main/res/raw
"""
import json
import math
import sys
from pathlib import Path

FPS = 60
SIZE = 512
C = SIZE / 2

# Material 3 cubic-bezier easings as ((x1, y1), (x2, y2)).
STANDARD = ((0.2, 0.0), (0.0, 1.0))
DECELERATE = ((0.05, 0.7), (0.1, 1.0))
SINE = ((0.37, 0.0), (0.63, 1.0))
OVERSHOOT = ((0.34, 1.56), (0.64, 1.0))
LINEAR = ((0.0, 0.0), (1.0, 1.0))
HOLD = None

PLACEHOLDER = {
    "primary": "#6750A4",
    "onPrimary": "#FFFFFF",
    "primaryContainer": "#EADDFF",
    "secondary": "#625B71",
    "secondaryContainer": "#E8DEF8",
    "tertiary": "#7D5260",
    "tertiaryContainer": "#FFD8E4",
    "surface": "#FEF7FF",
    "onSurfaceVariant": "#49454F",
}


def rgba(role):
    hex_ = PLACEHOLDER[role].lstrip("#")
    return [round(int(hex_[i:i + 2], 16) / 255, 4) for i in (0, 2, 4)] + [1]


# --- animatable values -------------------------------------------------------------------

def static(v):
    return {"a": 0, "k": list(v) if isinstance(v, tuple) else v}


def keyframes(frames, spatial=False):
    """frames: [(t, value, easing), ...]. The easing shapes the segment that starts at that
    keyframe; HOLD keeps the value until the next keyframe. The last easing is ignored."""
    k = []
    for index, (t, v, ease) in enumerate(frames):
        values = list(v) if isinstance(v, (list, tuple)) else [v]
        entry = {"t": t, "s": values}
        if index < len(frames) - 1:
            if ease is HOLD:
                entry["h"] = 1
            else:
                (ox, oy), (ix, iy) = ease
                if spatial:
                    entry.update({"o": {"x": ox, "y": oy}, "i": {"x": ix, "y": iy},
                                  "to": [0, 0, 0], "ti": [0, 0, 0]})
                else:
                    n = len(values)
                    entry.update({"o": {"x": [ox] * n, "y": [oy] * n},
                                  "i": {"x": [ix] * n, "y": [iy] * n}})
        k.append(entry)
    return {"a": 1, "k": k}


def prop(v):
    return v if isinstance(v, dict) else static(v)


# --- shapes ------------------------------------------------------------------------------

def group(name, items, position=(0, 0), scale=(100, 100), rotation=0, opacity=100):
    transform = {"ty": "tr", "p": prop(position), "a": static([0, 0]), "s": prop(scale),
                 "r": prop(rotation), "o": prop(opacity), "sk": static(0), "sa": static(0)}
    return {"ty": "gr", "nm": name, "it": items + [transform]}


def ellipse(diameter, position=(0, 0)):
    size = diameter if isinstance(diameter, dict) else static([diameter, diameter])
    return {"ty": "el", "d": 1, "s": size, "p": static(position)}


def rect(width, height, radius, position=(0, 0)):
    return {"ty": "rc", "d": 1, "s": static([width, height]), "p": static(position), "r": static(radius)}


def star(points, outer, inner, outer_roundness=0, inner_roundness=0):
    return {"ty": "sr", "sy": 1, "d": 1, "pt": static(points), "p": static([0, 0]), "r": static(0),
            "or": static(outer), "os": static(outer_roundness),
            "ir": static(inner), "is": static(inner_roundness)}


def path(vertices, ins=None, outs=None, closed=True):
    n = len(vertices)
    return {"ty": "sh", "ks": static({
        "c": closed,
        "v": [list(p) for p in vertices],
        "i": [list(p) for p in (ins or [(0, 0)] * n)],
        "o": [list(p) for p in (outs or [(0, 0)] * n)],
    })}


def line(x0, x1, y=0):
    return path([(x0, y), (x1, y)], closed=False)


def fill(role):
    return {"ty": "fl", "c": static(rgba(role)), "o": static(100), "r": 1, "bm": 0}


def stroke(role, width):
    return {"ty": "st", "c": static(rgba(role)), "o": static(100), "w": static(width),
            "lc": 2, "lj": 2, "ml": 4, "bm": 0}


def trim(end):
    return {"ty": "tm", "s": static(0), "e": prop(end), "o": static(0), "m": 1}


# --- motion ------------------------------------------------------------------------------

def pop(start, duration=28):
    """Grows a layer from nothing with a little overshoot."""
    frames = [(start, [0, 0, 100], OVERSHOOT), (start + duration, [100, 100, 100], LINEAR)]
    if start > 0:
        frames.insert(0, (0, [0, 0, 100], HOLD))
    return keyframes(frames)


def fade_in(start, duration=20):
    return keyframes([(0, 0, HOLD), (start, 0, STANDARD), (start + duration, 100, LINEAR)])


class Animation:
    def __init__(self, name, intro, loop):
        self.name = name
        self.intro = intro
        self.loop = loop
        self.end = intro + loop
        self.layers = []
        self._next_index = 1

    def reserve(self):
        """An index for a layer added later, so its children can be added in front of it."""
        index = self._next_index
        self._next_index += 1
        return index

    def layer(self, role, shapes, position=(C, C), anchor=(0, 0), scale=None, rotation=0,
              opacity=100, parent=None, index=None):
        position_value = position if isinstance(position, dict) else static(list(position) + [0])
        self.layers.append({
            "ddd": 0, "ind": index or self.reserve(), "ty": 4, "nm": role, "sr": 1,
            "ks": {"o": prop(opacity), "r": prop(rotation), "p": position_value,
                   "a": static(list(anchor) + [0]), "s": scale or static([100, 100, 100])},
            "ao": 0, "shapes": shapes, "ip": 0, "op": self.end, "st": 0, "bm": 0,
            **({"parent": parent} if parent else {}),
        })

    # Loop helpers: each returns to its starting value at the end of the loop.

    def spin(self, degrees):
        return keyframes([(0, 0, HOLD), (self.intro, 0, LINEAR), (self.end, degrees, LINEAR)])

    def sway(self, degrees):
        return keyframes([(0, 0, HOLD), (self.intro, 0, SINE),
                          (self.intro + self.loop // 2, degrees, SINE), (self.end, 0, LINEAR)])

    def bob(self, point, lift):
        """Floats a point up by [lift] (down if negative) and back once per loop."""
        x, y = point
        return keyframes([(0, [x, y, 0], HOLD), (self.intro, [x, y, 0], SINE),
                          (self.intro + self.loop // 2, [x, y - lift, 0], SINE),
                          (self.end, [x, y, 0], LINEAR)], spatial=True)

    def arrive_then_pulse(self, start, low, high, high_first=True):
        """Pops in during the intro, then breathes between [low] and [high] percent."""
        first, second = (high, low) if high_first else (low, high)
        return keyframes([(0, [0, 0, 100], HOLD), (start, [0, 0, 100], OVERSHOOT),
                          (start + 24, [first, first, 100], SINE),
                          (self.intro, [first, first, 100], SINE),
                          (self.intro + self.loop // 2, [second, second, 100], SINE),
                          (self.end, [first, first, 100], LINEAR)])

    def backdrop(self, role, points, outer, inner, roundness):
        """The scalloped cookie behind each scene, turning one scallop per loop."""
        self.layer(role, [group("cookie", [star(points, outer, inner, roundness, roundness), fill(role)])],
                   scale=pop(0, duration=36), rotation=self.spin(360 / points))

    def to_json(self):
        return {"v": "5.7.4", "fr": FPS, "ip": 0, "op": self.end, "w": SIZE, "h": SIZE,
                "nm": self.name, "ddd": 0, "assets": [], "layers": self.layers,
                "markers": [{"tm": 0, "cm": "intro", "dr": self.intro},
                            {"tm": self.intro, "cm": "loop", "dr": self.loop}]}


# --- scenes ------------------------------------------------------------------------------

def chat_scene():
    """A question and an answer, with a couple of sparkles."""
    a = Animation("Meet Mishti", intro=130, loop=240)

    a.layer("tertiary", [group("sparkle", [star(4, 28, 8, 0, 30), fill("tertiary")])],
            position=(394, 118), scale=a.arrive_then_pulse(96, 70, 100), rotation=a.spin(90))
    a.layer("tertiary", [group("sparkle", [star(4, 18, 5, 0, 30), fill("tertiary")])],
            position=(112, 168), scale=a.arrive_then_pulse(100, 70, 100, high_first=False),
            rotation=a.spin(-90))

    # Mishti's reply: lines that write themselves into a bubble growing from its lower-left.
    reply = a.reserve()
    lines = []
    for n, (x1, y) in enumerate(((58, -24), (80, 0), (22, 24))):
        start = 84 + n * 12
        lines.append(group(f"line{n}", [
            line(-76, x1, y),
            trim(keyframes([(0, 0, HOLD), (start, 0, DECELERATE), (start + 28, 100, LINEAR)])),
            stroke("onSurfaceVariant", 12),
        ]))
    a.layer("onSurfaceVariant", lines, position=(0, 0), parent=reply)
    a.layer("surface", [group("reply", [rect(208, 100, 30), fill("surface")])],
            position=a.bob((126, 370), -8), anchor=(-104, 50), scale=pop(58), index=reply)

    # The question, growing from its lower-right corner.
    question = a.reserve()
    a.layer("onPrimary", [
        group("line0", [line(-60, 40, -8), stroke("onPrimary", 10)]),
        group("line1", [line(-60, 8, 12), stroke("onPrimary", 10)]),
    ], position=(0, 0), opacity=fade_in(34, 16), parent=question)
    a.layer("primary", [group("question", [rect(184, 64, 32), fill("primary")])],
            position=a.bob((392, 222), 8), anchor=(92, 32), scale=pop(18), index=question)

    a.backdrop("primaryContainer", points=12, outer=200, inner=186, roundness=26)
    return a


def private_scene():
    """A shield locks shut and sends out quiet pulses; three dots keep watch."""
    a = Animation("Private and offline", intro=110, loop=240)
    center = (C, 262)

    a.layer("tertiary", [
        group(f"dot{n}", [ellipse(18), fill("tertiary")],
              position=(round(184 * math.cos(math.radians(n * 120 - 90)), 2),
                        round(184 * math.sin(math.radians(n * 120 - 90)), 2)))
        for n in range(3)
    ], position=center, rotation=a.spin(120), opacity=fade_in(80, 24))

    # Keyhole, lock body, and a shackle that drops shut.
    a.layer("primary", [
        group("hole", [ellipse(22), fill("primary")], position=(0, -6)),
        group("slot", [rect(10, 22, 5, position=(0, 8)), fill("primary")]),
    ], position=(C, 290), scale=pop(70, duration=22))
    a.layer("onPrimary", [group("body", [rect(92, 72, 18), fill("onPrimary")])],
            position=(C, 290), scale=pop(36, duration=26))
    a.layer("onPrimary", [group("shackle", [
        path([(-26, 18), (-26, -8), (26, -8), (26, 18)],
             ins=[(0, 0), (0, 0), (0, -36), (0, 0)],
             outs=[(0, 0), (0, -36), (0, 0), (0, 0)], closed=False),
        stroke("onPrimary", 14),
    ])], position=keyframes([(0, [C, 220, 0], HOLD), (50, [C, 220, 0], OVERSHOOT),
                             (74, [C, 246, 0], LINEAR)], spatial=True),
        opacity=fade_in(42, 12))

    shield = path(
        [(-96, -84), (0, -120), (96, -84), (96, 8), (0, 124), (-96, 8)],
        ins=[(0, 0), (-50, 0), (-26, -20), (0, 0), (58, -30), (0, 58)],
        outs=[(26, -20), (50, 0), (0, 0), (0, 58), (-58, -30), (0, 0)],
    )
    a.layer("primary", [group("shield", [shield, fill("primary")])], position=center,
            scale=a.arrive_then_pulse(10, 100, 104))

    # Rings that swell out of the shield and fade, twice per loop; both are invisible at the
    # seam, so the loop restarts cleanly.
    period = a.loop // 2
    for delay, duration in ((0, 110), (34, 84)):
        size = [(0, [236, 236], HOLD)]
        alpha = [(0, 0, HOLD)]
        for cycle in range(2):
            start = a.intro + cycle * period + delay
            end = start + duration
            size += [(start, [236, 236], DECELERATE), (end, [440, 440], HOLD)]
            alpha += [(start, 46, DECELERATE), (end, 0, HOLD)]
        size.append((a.end, [236, 236], LINEAR))
        alpha.append((a.end, 0, LINEAR))
        a.layer("primary", [group("ring", [ellipse(keyframes(size)), stroke("primary", 4)],
                                  opacity=keyframes(alpha))], position=center)

    a.backdrop("secondaryContainer", points=9, outer=204, inner=184, roundness=40)
    return a


def personalize_scene():
    """A palette that sways, over two sliders being tuned."""
    a = Animation("Make it yours", intro=100, loop=240)
    left, right = -96, 96

    for n, (y, role, low, high) in enumerate(((366, "primary", 0.3, 0.82), (414, "tertiary", 0.74, 0.24))):
        appear = fade_in(42 + n * 10, 20)
        knob = left + (right - left) * low, left + (right - left) * high
        a.layer(role, [group("knob", [ellipse(28), fill(role)])], opacity=appear,
                position=keyframes([(0, [C + knob[0], y, 0], HOLD), (a.intro, [C + knob[0], y, 0], SINE),
                                    (a.intro + a.loop // 2, [C + knob[1], y, 0], SINE),
                                    (a.end, [C + knob[0], y, 0], LINEAR)], spatial=True))
        a.layer(role, [group("active", [
            line(left, right),
            trim(keyframes([(0, 100 * low, HOLD), (a.intro, 100 * low, SINE),
                            (a.intro + a.loop // 2, 100 * high, SINE), (a.end, 100 * low, LINEAR)])),
            stroke(role, 10),
        ])], position=(C, y), opacity=appear)
        a.layer("surface", [group("track", [line(left, right), stroke("surface", 10)])],
                position=(C, y), opacity=appear)

    for n, role in enumerate(("primary", "secondary", "tertiary")):
        angle = math.radians(n * 120 - 90)
        offset = (round(48 * math.cos(angle), 2), round(48 * math.sin(angle), 2))
        a.layer(role, [group("swatch", [ellipse(112), fill(role)], position=offset)],
                position=(C, 214), scale=pop(12 + n * 10), rotation=a.sway(40))

    a.backdrop("tertiaryContainer", points=8, outer=206, inner=184, roundness=40)
    return a


# --- checks ------------------------------------------------------------------------------

def check_keyframes(node, where):
    if isinstance(node, dict):
        if node.get("a") == 1:
            times = [frame["t"] for frame in node["k"]]
            assert times == sorted(set(times)), f"{where}: keyframe times must increase: {times}"
        for key, child in node.items():
            check_keyframes(child, f"{where}.{key}")
    elif isinstance(node, list):
        for i, child in enumerate(node):
            check_keyframes(child, f"{where}[{i}]")


def validate(animation):
    layers = animation["layers"]
    indices = [layer["ind"] for layer in layers]
    assert len(indices) == len(set(indices)), "layer indices must be unique"
    for layer in layers:
        assert layer.get("parent") in (None, *indices), f"{layer['nm']}: missing parent"
        assert layer["nm"] in PLACEHOLDER, f"unknown role {layer['nm']}"
        check_keyframes(layer, f"{animation['nm']}/{layer['nm']}#{layer['ind']}")


def main(out_dir):
    out = Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)
    for file_name, scene in (("onboarding_chat.json", chat_scene),
                             ("onboarding_private.json", private_scene),
                             ("onboarding_personalize.json", personalize_scene)):
        animation = scene().to_json()
        validate(animation)
        (out / file_name).write_text(json.dumps(animation, separators=(",", ":")))
        roles = sorted({layer["nm"] for layer in animation["layers"]})
        print(f"{file_name}: {len(animation['layers'])} layers, {animation['op']} frames, roles={roles}")


if __name__ == "__main__":
    main(sys.argv[1])
