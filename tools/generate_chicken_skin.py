"""Create vanilla chicken texture overrides without changing entity models.

Run with: python tools/generate_chicken_skin.py
Requires Pillow. Original textures are read from the local Minecraft cache.
"""

from pathlib import Path
from zipfile import ZipFile

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "src/main/resources/assets/minecraft/textures/entity/chicken"
HAIR = (132, 135, 141, 255)
HAIR_LIGHT = (163, 166, 171, 255)
PART = (72, 75, 81, 255)
SHIRT = (27, 29, 34, 255)
PANTS = (117, 121, 130, 255)
STRAP = (240, 240, 235, 255)


def adult(image, cold=False):
    # The adult eyes occupy (3, 4) and (6, 4) on the front face.
    # Preserve their original pixels and the light feathers below them.
    eyes = image.crop((3, 4, 7, 6))
    draw = ImageDraw.Draw(image)
    # Head top: two grey halves with a darker center part.
    draw.rectangle((3, 0, 6, 2), fill=HAIR)
    draw.line((4, 0, 4, 2), fill=PART)
    draw.line((6, 0, 6, 2), fill=HAIR_LIGHT)
    # Front fringe leaves the eyes and lower face visible.
    draw.rectangle((3, 3, 6, 3), fill=HAIR)
    draw.point((4, 3), fill=PART)
    draw.point((5, 3), fill=HAIR_LIGHT)
    for start, width in ((0, 3), (7, 3), (10, 4)):
        draw.rectangle((start, 3, start + width - 1, 4), fill=HAIR)
    draw.rectangle((10, 5, 13, 7), fill=HAIR)
    if cold:
        # The cold variant has an additional head feather cube.
        draw.rectangle((44, 0, 63, 6), fill=HAIR)
        draw.line((50, 0, 50, 3), fill=PART)
        draw.line((50, 4, 50, 6), fill=PART)
        draw.line((53, 4, 53, 6), fill=HAIR_LIGHT)
    # Body is rotated 90 degrees by vanilla: local -Z is world up.
    # North face forms shoulders; south face forms the underside.
    draw.rectangle((6, 15, 11, 22), fill=SHIRT)
    draw.rectangle((18, 15, 23, 22), fill=PANTS)
    # Connect the front and rear straps across the upward-facing surface.
    # The body is rotated, so this is its local north face, not the UV top.
    for x in (7, 10):
        draw.rectangle((x, 15, x + 1, 22), fill=STRAP)
    # Local top/bottom faces are the visible back/chest after rotation.
    for start in (6, 12):
        draw.rectangle((start, 9, start + 5, 14), fill=PANTS)
        draw.rectangle((start, 9, start + 5, 11), fill=SHIRT)
        for x in (start + 1, start + 4):
            draw.line((x, 9, x, 12), fill=STRAP)
            draw.point((x, 13), fill=PART)
        draw.line((start, 13, start + 5, 13), fill=PART)
    for start, reverse in ((0, True), (12, False)):
        for dx in range(6):
            upper = dx >= 3 if reverse else dx < 3
            draw.line((start + dx, 15, start + dx, 22), fill=SHIRT if upper else PANTS)
    # Wings are shirt sleeves; legs carry grey trousers with dark cuffs.
    draw.rectangle((24, 13, 37, 22), fill=SHIRT)
    draw.rectangle((26, 0, 37, 7), fill=PANTS)
    draw.rectangle((26, 6, 37, 7), fill=PART)
    if cold:
        draw.rectangle((38, 9, 47, 16), fill=PANTS)
    image.paste(eyes, (3, 4))
    return image


def baby(image):
    draw = ImageDraw.Draw(image)
    # Baby chickens have a single head/body cube and separate tiny wings.
    draw.rectangle((4, 0, 7, 3), fill=HAIR)
    draw.line((5, 0, 5, 3), fill=PART)
    for start in (0, 4, 8, 12):
        draw.rectangle((start, 4, start + 3, 4), fill=HAIR)
        draw.rectangle((start, 6, start + 3, 6), fill=SHIRT)
        draw.rectangle((start, 7, start + 3, 7), fill=PANTS)
    for start in (4, 12):
        draw.point((start, 6), fill=STRAP)
        draw.point((start + 3, 6), fill=STRAP)
    draw.rectangle((4, 8, 9, 9), fill=SHIRT)
    draw.rectangle((0, 2, 3, 3), fill=PANTS)
    return image


def refine_hair(image, cold=False):
    """Add finer pixel hair while keeping vanilla UV coordinates unchanged."""
    image = image.resize((256, 128), Image.Resampling.NEAREST)
    draw = ImageDraw.Draw(image)
    shadow = (91, 95, 103, 255)
    mid = (130, 135, 145, 255)
    light = (174, 180, 190, 255)
    highlight = (194, 199, 207, 255)
    # Front face occupies x=12..27, y=12..35 at this resolution.
    # Keep both eyes (y=16..19) and the face below completely exposed.
    draw.rectangle((12, 12, 27, 15), fill=mid)
    draw.line((19, 12, 20, 12), fill=PART)
    draw.line((18, 13, 18, 14), fill=shadow)
    draw.line((21, 13, 21, 14), fill=shadow)
    draw.line((13, 14, 17, 12), fill=light)
    draw.line((22, 12, 26, 14), fill=light)
    draw.point((14, 13), fill=highlight)
    draw.point((25, 13), fill=highlight)
    draw.line((12, 15, 14, 15), fill=shadow)
    draw.line((25, 15, 27, 15), fill=shadow)
    # Scalp: narrow part, symmetrical curved strands and darker edges.
    draw.rectangle((12, 0, 27, 11), fill=mid)
    draw.line((19, 0, 19, 10), fill=PART)
    draw.line((20, 0, 20, 10), fill=shadow)
    for offset in (0, 4, 8):
        draw.line([(18, offset), (16, offset + 1), (13, offset + 3)], fill=light)
        draw.line([(21, offset), (23, offset + 1), (26, offset + 3)], fill=light)
    draw.line((12, 0, 12, 11), fill=shadow)
    draw.line((27, 0, 27, 11), fill=shadow)
    # Side hair follows the cap down to the temples, outside the eye UVs.
    for start in (0, 28):
        draw.rectangle((start, 12, start + 11, 17), fill=mid)
        draw.line((start + 1, 13, start + 9, 13), fill=light)
        draw.line((start + 2, 15, start + 10, 15), fill=shadow)
        draw.line((start + 9, 16, start + 11, 18), fill=shadow)
    # Rear hair has vertical strands rather than a flat grey patch.
    draw.rectangle((40, 12, 55, 31), fill=mid)
    for x in (42, 46, 50, 54):
        draw.line((x, 13, x, 28), fill=light if x % 8 == 2 else shadow)
    draw.line((40, 31, 55, 31), fill=shadow)
    if cold:
        # Detail the cold chicken's existing feather cap as parted hair.
        draw.rectangle((176, 0, 255, 27), fill=mid)
        for x in range(178, 256, 6):
            draw.line((x, 1, x, 25), fill=light if x % 12 == 10 else shadow)
        draw.line((202, 0, 202, 11), fill=PART, width=2)
        draw.line((202, 16, 202, 27), fill=PART, width=2)
    return image


def generate():
    candidates = sorted((ROOT / ".gradle/loom-cache/minecraftMaven").rglob("*clientOnly*.jar"))
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for jar in candidates:
        with ZipFile(jar) as archive:
            names = [name for name in archive.namelist()
                     if name.startswith("assets/minecraft/textures/entity/chicken/")
                     and name.endswith(".png")]
            if not names:
                continue
            for name in names:
                with archive.open(name) as source:
                    image = Image.open(source).convert("RGBA")
                if "_baby" in name:
                    image = baby(image)
                else:
                    image = adult(image, cold="_cold" in name)
                    image = refine_hair(image, cold="_cold" in name)
                image.save(OUTPUT / Path(name).name)
                print(f"Generated {Path(name).name}: {image.width}x{image.height}")
            return
    raise FileNotFoundError("No vanilla chicken textures found in the Minecraft cache")


if __name__ == "__main__":
    generate()
