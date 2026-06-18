#!/usr/bin/env python3
"""Generates blockstate / block model / item model JSON for Urban blocks."""
import json
import os

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "urban")
BS = os.path.join(ROOT, "blockstates")
BM = os.path.join(ROOT, "models", "block")
IM = os.path.join(ROOT, "models", "item")
for d in (BS, BM, IM):
    os.makedirs(d, exist_ok=True)

CUBES = ["asphalt", "road_marking_white", "road_marking_yellow", "crosswalk", "sidewalk"]
SIGNS = ["sign_stop", "sign_priority", "sign_yield", "sign_speed_50",
         "sign_autobahn", "sign_city_limit", "sign_exit"]


def write(path, obj):
    with open(path, "w") as f:
        json.dump(obj, f, indent=4)
        f.write("\n")


for name in CUBES:
    write(os.path.join(BS, name + ".json"),
          {"variants": {"": {"model": "urban:block/" + name}}})
    write(os.path.join(BM, name + ".json"),
          {"parent": "minecraft:block/cube_all", "textures": {"all": "urban:block/" + name}})
    write(os.path.join(IM, name + ".json"),
          {"parent": "urban:block/" + name})

# Shared sign model: a tall steel post with a panel mounted near the top. The
# panel deliberately extends above the 16px block height (up to y=32) so the
# sign reads at a realistic scale instead of being squashed into one block.
SIGN_BASE = {
    "parent": "block/block",
    "textures": {"particle": "#post"},
    "elements": [
        {
            "from": [7, 0, 7],
            "to": [9, 26, 9],
            "faces": {
                "north": {"uv": [7, 0, 9, 16], "texture": "#post"},
                "south": {"uv": [7, 0, 9, 16], "texture": "#post"},
                "east": {"uv": [7, 0, 9, 16], "texture": "#post"},
                "west": {"uv": [7, 0, 9, 16], "texture": "#post"},
                "up": {"uv": [7, 7, 9, 9], "texture": "#post"},
            },
        },
        {
            "from": [1, 21, 7.25],
            "to": [15, 32, 8.75],
            "faces": {
                "north": {"uv": [16, 0, 0, 16], "texture": "#panel"},
                "south": {"uv": [0, 0, 16, 16], "texture": "#panel"},
                "up": {"uv": [1, 7, 15, 8], "texture": "#post"},
                "down": {"uv": [1, 7, 15, 8], "texture": "#post"},
                "east": {"uv": [7, 0, 8, 11], "texture": "#post"},
                "west": {"uv": [7, 0, 8, 11], "texture": "#post"},
            },
        },
    ],
}
write(os.path.join(BM, "sign_base.json"), SIGN_BASE)

for name in SIGNS:
    write(os.path.join(BS, name + ".json"), {"variants": {
        "facing=north": {"model": "urban:block/" + name},
        "facing=east": {"model": "urban:block/" + name, "y": 90},
        "facing=south": {"model": "urban:block/" + name, "y": 180},
        "facing=west": {"model": "urban:block/" + name, "y": 270},
    }})
    write(os.path.join(BM, name + ".json"),
          {"parent": "urban:block/sign_base",
           "textures": {"post": "urban:block/post", "panel": "urban:block/" + name}})
    write(os.path.join(IM, name + ".json"),
          {"parent": "urban:block/" + name})

print("models written")
