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
