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

sys.path.insert(0, str(Path(__file__).resolve().parent))
import ru_translations as ru

ROOT = Path(__file__).resolve().parent.parent
TOOLS = ROOT / "tools"
RES = ROOT / "common/src/main/resources"
ASSETS = RES / "assets"
BOOK = ASSETS / "hexcasting/patchouli_books/thehexbook/en_us"
BOOK_RU = ASSETS / "hexcasting/patchouli_books/thehexbook/ru_ru"
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


def translate_io(text):
    for en, ru_word in ru.IO_WORDS:
        text = text.replace(en, ru_word)
    return text


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
    # Entries go into Hex Casting's own categories: spells and great spells (patterns), items (things).
    lang_ru = dict(ru.EXTRA)
    for book in (BOOK, BOOK_RU):
        if (book / "entries/hexquantum").exists():
            for old in (book / "entries/hexquantum").glob("*.json"):
                old.unlink()
    (BOOK / "categories/hexquantum.json").unlink(missing_ok=True)
    entries_dir = BOOK / "entries/hexquantum"
    entries_dir_ru = BOOK_RU / "entries/hexquantum"
    for i, p in enumerate(patterns):
        name = entry_name(p["id"])
        lang[f"hexquantum.page.{name}"] = p["desc"]
        entry = {
            "name": f"hexcasting.action.hexquantum:{p['id']}",
            "category": "hexcasting:patterns/great_spells" if p.get("great") else "hexcasting:patterns/spells",
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
        }
        write_json(entries_dir / f"{name}.json", entry)
        ru_name, ru_desc = ru.PATTERNS[p["id"]]
        lang_ru[f"hexcasting.action.hexquantum:{p['id']}"] = ru_name
        lang_ru[f"hexquantum.page.{name}"] = ru_desc
        entry_ru = json.loads(json.dumps(entry))
        entry_ru["pages"][0]["input"] = translate_io(p["in"])
        entry_ru["pages"][0]["output"] = translate_io(p["out"])
        write_json(entries_dir_ru / f"{name}.json", entry_ru)
    for j, (name, key, _, icon, text) in enumerate(THINGS):
        lang[f"hexquantum.page.{name}"] = text
        write_json(entries_dir / f"{name}.json", {
            "name": key,
            "category": "hexcasting:items",
            "icon": icon,
            "sortnum": 100 + j,
            "advancement": "hexcasting:root",
            "read_by_default": True,
            "pages": [{"type": "patchouli:text", "text": f"hexquantum.page.{name}"}],
        })

    for name, key, _, _, _ in THINGS:
        lang_ru[key], lang_ru[f"hexquantum.page.{name}"] = ru.THINGS[name]
    missing = set(lang) - set(lang_ru)
    if missing:
        sys.exit(f"missing Russian translations: {sorted(missing)}")
    write_json(ASSETS / "hexquantum/lang/en_us.json", dict(sorted(lang.items())))
    write_json(ASSETS / "hexquantum/lang/ru_ru.json", dict(sorted(lang_ru.items())))

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
