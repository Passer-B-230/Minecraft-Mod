"""Generate tick-by-tick reload models from the original Claude pistol model.

Run from any directory with: python tools/generate_pistol_reload.py
The original pistol.json and texture are never modified.
"""

import copy
import json
import math
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/template-mod"
MODEL_DIR = ASSETS / "models/item"
RELOAD_TICKS = 30


def smooth(value):
    value = max(0.0, min(1.0, value))
    return value * value * (3.0 - 2.0 * value)


def model_ref(name):
    return {"type": "minecraft:model", "model": f"template-mod:item/{name}"}


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def generate():
    original = json.loads((MODEL_DIR / "pistol.json").read_text(encoding="utf-8"))
    assert {"magazine", "mag_plate"}.issubset({part["name"] for part in original["elements"]})
    aimed = copy.deepcopy(original)
    for side in ("righthand", "lefthand"):
        pose = aimed["display"][f"firstperson_{side}"]
        pose["rotation"] = [0, 0, 0]
        pose["translation"] = [-9.0 if side == "righthand" else 9.0, 4.3, -4.0]
        pose["scale"] = [0.9, 0.9, 0.9]
    write_json(MODEL_DIR / "pistol_aim.json", aimed)
    # Generate separate recoil poses for normal and aimed firing. The aimed
    # poses start from pistol_aim so recoil never snaps back to hip-fire.
    recoil_frames = 10
    for frame in range(recoil_frames):
        progress = frame / (recoil_frames - 1)
        strength = 1.0 - smooth(progress)
        for prefix, base in (("pistol_recoil", original), ("pistol_aim_recoil", aimed)):
            recoil = copy.deepcopy(base)
            for context, pose in recoil["display"].items():
                if context.startswith("firstperson") or context.startswith("thirdperson"):
                    side = -1 if context.endswith("lefthand") else 1
                    # Positive X rotation raises the muzzle in the hand pose.
                    pose["rotation"][0] += round(28 * strength, 4)
                    pose["rotation"][2] -= round(9 * strength * side, 4)
            for part in recoil["elements"]:
                if part["name"] in {"slide", "barrel", "muzzle", "front_sight"}:
                    for field in ("from", "to"):
                        part[field][2] += round(3.5 * strength, 4)
            write_json(MODEL_DIR / f"{prefix}/frame_{frame:02d}.json", recoil)
    entries = []
    for tick in range(RELOAD_TICKS):
        model = copy.deepcopy(original)
        # Tilt in during ticks 0-6, hold the pose, return during ticks 24-29.
        tilt = smooth(tick / 6) * (1 - smooth((tick - 24) / 5))
        # Pull along the grip axis, pause outside, then seat the new magazine.
        pull = smooth((tick - 5) / 7) * (1 - smooth((tick - 17) / 7))
        distance = 6.0 * pull
        angle = math.radians(-22.5)
        offset = [0.0, -distance * math.cos(angle), -distance * math.sin(angle)]
        for part in model["elements"]:
            if part["name"] in {"magazine", "mag_plate"}:
                # Translate the rotation pivot too, to preserve the original grip angle.
                for field in ("from", "to"):
                    part[field] = [round(v + d, 5) for v, d in zip(part[field], offset)]
                part["rotation"]["origin"] = [
                    round(v + d, 5) for v, d in zip(part["rotation"]["origin"], offset)
                ]
            if part["name"] == "slide":
                # Briefly rack the slide after seating the magazine.
                rack = math.sin(math.pi * max(0.0, min(1.0, (tick - 23) / 5)))
                for field in ("from", "to"):
                    part[field][2] += round(1.2 * rack, 5)
        for context, pose in model["display"].items():
            if context.startswith("firstperson") or context.startswith("thirdperson"):
                side = -1 if context.endswith("lefthand") else 1
                pose["rotation"][0] += round(-18 * tilt, 4)
                pose["rotation"][1] += round(20 * tilt * side, 4)
                pose["rotation"][2] += round(-32 * tilt * side, 4)
                pose["translation"][1] += round(2 * tilt, 4)
        name = f"pistol_reload/frame_{tick:02d}"
        write_json(MODEL_DIR / f"{name}.json", model)
        entries.append({"threshold": tick, "model": model_ref(name)})

    write_json(ASSETS / "items/pistol.json", {
        "model": {
            "type": "minecraft:condition",
            "property": "minecraft:using_item",
            "on_true": {
                "type": "minecraft:range_dispatch",
                "property": "minecraft:use_duration",
                "remaining": False,
                "fallback": model_ref("pistol"),
                "entries": entries,
            },
            "on_false": {
                "type": "minecraft:range_dispatch",
                "property": "minecraft:cooldown",
                "fallback": {
                    "type": "minecraft:condition",
                    "property": "template-mod:pistol_aiming",
                    "on_true": model_ref("pistol_aim"),
                    "on_false": model_ref("pistol"),
                },
                "entries": [
                    {"threshold": round((frame + 1) / recoil_frames, 3),
                     "model": {
                         "type": "minecraft:condition",
                         "property": "template-mod:pistol_aiming",
                         "on_true": model_ref(f"pistol_aim_recoil/frame_{recoil_frames - 1 - frame:02d}"),
                         "on_false": model_ref(f"pistol_recoil/frame_{recoil_frames - 1 - frame:02d}"),
                     }}
                    for frame in range(recoil_frames)
                ],
            },
        }
    })
    print(f"Generated {RELOAD_TICKS} reload frames and pistol item definition.")


if __name__ == "__main__":
    generate()
