#!/usr/bin/env python3
"""Generate HexQuantum's lang file, Hex Casting book entries and pattern tables from tools/patterns.json.

Run from the repository root:  python3 tools/gen_assets.py
It also checks that HQActions.java registers exactly the patterns in patterns.json and that every pattern is
a valid, unique drawing (via check_patterns.py).
"""
import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TOOLS = ROOT / "tools"
RES = ROOT / "common/src/main/resources"
ASSETS = RES / "assets"
BOOK = ASSETS / "hexcasting/patchouli_books/thehexbook/en_us"
JAVA = ROOT / "common/src/main/java/io/github/timursl/hexquantum"

GROUPS = {
    1: "Storage, Illusions and Waiting",
    2: "Quantum Media",
    3: "Echoes and Wisps",
    4: "Damage Links",
    5: "Phantoms",
    6: "Pocket Space",
    7: "Optics",
}

# Items, blocks and entities: (book entry id, lang key, name, icon, description)
THINGS = [
    ("quantum_phial", "item.hexquantum.quantum_phial", "Quantum Phial", "hexquantum:quantum_phial",
     "A phial opened onto a rift in media itself, made by $(l:hexquantum/media_rift)Open Media Rift/$. It is bound "
     "to me alone and its capacity has no end. Whenever I cast with it anywhere in my inventory, the media is drawn from "
     "it first, before amethyst and before my own life. The media does not live in the glass but in me: if the phial is "
     "lost, a new one finds it all again, and I keep the phial when I die."),
    ("phantom_block", "block.hexquantum.phantom_block", "Phantom Block", "minecraft:glass",
     "A block that is not there. It looks and feels like the block it copies, but vanishes when touched, or, if "
     "empowered, casts its maker's hex instead."),
    ("portal_door", "block.hexquantum.portal_door", "Portal Door", "hexquantum:portal_door",
     "Every pocket platform has one. Using it returns me to wherever I entered the pocket from."),
    ("illusion", "entity.hexquantum.illusion", "Illusion", "minecraft:armor_stand",
     "My personal Illusion: a perfect copy of me that nothing can hurt or bump into."),
    ("delayed_echo", "entity.hexquantum.delayed_echo", "Delayed Echo", "minecraft:echo_shard",
     "An intangible echo of me that waits, casts a hex once, and fades."),
    ("personal_echo", "entity.hexquantum.personal_echo", "Personal Echo", "minecraft:echo_shard",
     "A motionless decoy of me with a single point of health."),
    ("cyclic_echo", "entity.hexquantum.cyclic_echo", "Cyclic Echo", "minecraft:echo_shard",
     "An echo of me forever retracing my steps. One hit ends it."),
    ("cyclic_wisp", "entity.hexquantum.cyclic_wisp", "Cyclic Wisp", "minecraft:end_rod",
     "A mote of media that casts the same hex again and again."),
    ("projectile_wisp", "entity.hexquantum.projectile_wisp", "Projectile Wisp", "minecraft:end_rod",
     "A mote of media that flies straight and casts its hex where it strikes."),
    ("phantom_item", "entity.hexquantum.phantom_item", "Phantom Item", "minecraft:glass_bottle",
     "A dropped item that is not there: it vanishes when anyone tries to pick it up."),
]

LANG_EXTRA = {
    "item.hexquantum.quantum_phial.bound": "Bound to %s",
    "item.hexquantum.quantum_phial.unbound": "Unbound",
    "item.hexquantum.quantum_phial.desc": "Its media lives in its owner, not in the glass.",
    "effect.hexquantum.phantasmal": "Phantasmal",
    "effect.hexquantum.empowered_phantasmal": "Empowered Phantasmal",
    "container.hexquantum.pocket_stash": "Pocket Stash",
    "container.hexquantum.phantom_vault": "Phantom Vault",
    "hexcasting.iota.hexquantum:dimension": "Dimension",
    "hexcasting.iota.hexquantum:dimension.desc": "a dimension",
    "hexquantum.iota.dimension.display": "Dimension %s",
    "hexcasting.mishap.hexquantum.no_illusion": "I have no Illusion in this dimension",
    "hexcasting.mishap.hexquantum.illusion_too_far": "my Illusion is %s blocks away, more than %s",
    "hexcasting.mishap.hexquantum.too_many_waits": "I already have %s hexes waiting",
    "hexcasting.mishap.hexquantum.no_phial": "needs my own Quantum Phial in my other hand",
    "hexcasting.mishap.hexquantum.no_route": "there is no recent movement to echo",
    "hexcasting.mishap.hexquantum.vault_full": "my Phantom Vault is full",
    "hexcasting.mishap.hexquantum.vault_slot_empty": "slot %s of my Phantom Vault is empty",
    "hexcasting.mishap.hexquantum.no_pocket_dimension": "the pocket dimension is missing from this world",
    "hexcasting.mishap.invalid_value.hexquantum.seconds": "a number of seconds above 0 and at most %s",
    "hexcasting.mishap.invalid_value.hexquantum.hex": "a list of patterns (a hex)",
    "hexcasting.mishap.invalid_value.hexquantum.entity_list": "a list of living entities",
    "hexcasting.mishap.invalid_value.hexquantum.velocity": "a non-zero vector no longer than %s",
    "hexcasting.mishap.invalid_value.hexquantum.link_size": "a list of between %s and %s entities",
    "hexcasting.mishap.invalid_value.hexquantum.offset": "a vector no longer than %s",
    "hexcasting.mishap.bad_item.hexquantum.amethyst": "a form of amethyst",
    "hexcasting.mishap.bad_item.hexquantum.phantom_template": "an ordinary creature to copy",
    "hexcasting.mishap.bad_item.hexquantum.phantom_item": "a phantom item",
    "hexcasting.mishap.bad_block.hexquantum.copyable": "a block to copy",
    "hexquantum.category.hexquantum": "HexQuantum",
    "hexquantum.category.hexquantum.desc": "Illusions, echoes and phantoms; pockets folded out of space; media drawn from a rift. "
                                          "The patterns here bend what is real around me.",
}


def config_defaults():
    src = (JAVA / "config/HQConfig.java").read_text()
    out = {}
    for key, value in re.findall(r'(?:cost|dbl|integer)\("(\w+)",\s*([0-9.]+)', src):
        out[key] = value
    return out


def fmt_number(v):
    f = float(v)
    return str(int(f)) if f == int(f) else str(f)


def render_cost(expr, defaults):
    if expr.strip() == "0":
        return "0"

    def sub(m):
        key = m.group(0)
        if key in defaults:
            return f"{fmt_number(defaults[key])} dust"
        return key

    return re.sub(r"[a-z][A-Za-z]+(?=\b)", sub, expr).replace(" * ", " × ")


def check_registry(patterns):
    src = (JAVA / "registry/HQActions.java").read_text()
    found = re.findall(r'make\("([^"]+)",\s*"([a-z]+)",\s*HexDir\.(\w+)', src)
    registered = {pid: (sig, start) for pid, sig, start in found}
    spec = {p["id"]: (p["sig"], p["start"]) for p in patterns}
    if registered != spec:
        missing = set(spec) - set(registered)
        extra = set(registered) - set(spec)
        wrong = {k for k in spec.keys() & registered.keys() if spec[k] != registered[k]}
        sys.exit(f"HQActions.java and patterns.json disagree: missing={missing} extra={extra} different={wrong}")


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n")


def entry_name(pid):
    return pid.replace("/", "_")


def main():
    patterns = json.loads((TOOLS / "patterns.json").read_text())["patterns"]
    check_registry(patterns)
    subprocess.run([sys.executable, str(TOOLS / "check_patterns.py"), str(TOOLS / "patterns.json"),
                    str(TOOLS / "hexcasting_signatures.txt")], check=True)
    defaults = config_defaults()

    lang = {}
    for p in patterns:
        lang[f"hexcasting.action.hexquantum:{p['id']}"] = p["name"]
    for _, key, name, _, _ in THINGS:
        lang[key] = name
    lang.update(LANG_EXTRA)

    # ---- book ----
    write_json(BOOK / "categories/hexquantum.json", {
        "name": "hexquantum.category.hexquantum",
        "icon": "hexquantum:quantum_phial",
        "description": "hexquantum.category.hexquantum.desc",
        "sortnum": 10,
    })
    entries_dir = BOOK / "entries/hexquantum"
    if entries_dir.exists():
        for old in entries_dir.glob("*.json"):
            old.unlink()
    for i, p in enumerate(patterns):
        name = entry_name(p["id"])
        lang[f"hexquantum.page.{name}"] = p["desc"]
        write_json(entries_dir / f"{name}.json", {
            "name": f"hexcasting.action.hexquantum:{p['id']}",
            "category": "hexcasting:hexquantum",
            "icon": "minecraft:amethyst_shard" if not p.get("great") else "minecraft:nether_star",
            "sortnum": i,
            "advancement": "hexcasting:root",
            "read_by_default": True,
            "pages": [{
                "type": "hexcasting:pattern",
                "op_id": f"hexquantum:{p['id']}",
                "anchor": f"hexquantum:{p['id']}",
                "input": p["in"],
                "output": p["out"],
                "text": f"hexquantum.page.{name}",
            }],
        })
    for j, (name, key, _, icon, text) in enumerate(THINGS):
        lang[f"hexquantum.page.{name}"] = text
        write_json(entries_dir / f"{name}.json", {
            "name": key,
            "category": "hexcasting:hexquantum",
            "icon": icon,
            "sortnum": 100 + j,
            "advancement": "hexcasting:root",
            "read_by_default": True,
            "pages": [{"type": "patchouli:text", "text": f"hexquantum.page.{name}"}],
        })

    write_json(ASSETS / "hexquantum/lang/en_us.json", dict(sorted(lang.items())))

    # ---- docs ----
    lines = ["# HexQuantum patterns", "",
             "Generated by `tools/gen_assets.py` from `tools/patterns.json`. Costs are the config defaults "
             "(1 dust = 10000 media units); every cost and limit can be changed in `hexquantum-server.toml`.", ""]
    for group, title in GROUPS.items():
        lines += [f"## {group}. {title}", "",
                  "| Name | Angle signature | Stack in | Stack out | Media cost |",
                  "|---|---|---|---|---|"]
        for p in patterns:
            if p["group"] != group:
                continue
            great = " (great spell, per-world pattern)" if p.get("great") else ""
            lines.append(f"| {p['name']}{great} | `{p['sig']}` (start {p['start'].lower().replace('_', ' ')}) | "
                         f"{p['in'] or '—'} | {p['out'] or '—'} | {render_cost(p['cost'], defaults)} |")
        lines.append("")
    (ROOT / "docs").mkdir(exist_ok=True)
    (ROOT / "docs/PATTERNS.md").write_text("\n".join(lines))
    print(f"generated {len(lang)} lang keys, {len(patterns) + len(THINGS)} book entries, docs/PATTERNS.md")


if __name__ == "__main__":
    main()
