"""Validate HexQuantum pattern signatures: no edge overlap, no clash with Hex Casting or each other."""
import json
import sys

DIRS = ["NORTH_EAST", "EAST", "SOUTH_EAST", "SOUTH_WEST", "WEST", "NORTH_WEST"]
DELTA = {0: (1, -1), 1: (1, 0), 2: (0, 1), 3: (-1, 1), 4: (-1, 0), 5: (0, -1)}
ANG = {"w": 0, "e": 1, "d": 2, "s": 3, "a": 4, "q": 5}


def overlaps(sig, start):
    d = DIRS.index(start)
    pos = (0, 0)
    edges = set()

    def step(p, dd):
        n = (p[0] + DELTA[dd][0], p[1] + DELTA[dd][1])
        e = frozenset((p, n))
        if e in edges:
            return None
        edges.add(e)
        return n

    pos = step(pos, d)
    for c in sig:
        d = (d + ANG[c]) % 6
        pos = step(pos, d)
        if pos is None:
            return True
    return False


def main(spec_path, hex_sigs_path):
    hex_sigs = set(open(hex_sigs_path).read().split())
    spec = json.load(open(spec_path))
    seen = {}
    bad = False
    for p in spec["patterns"]:
        sig, start, pid = p["sig"], p["start"], p["id"]
        problems = []
        if overlaps(sig, start):
            problems.append("overlaps itself")
        if sig in hex_sigs:
            problems.append("clashes with Hex Casting")
        if sig in seen:
            problems.append("clashes with " + seen[sig])
        if sig.startswith(("aqaa", "dedd", "waaddw")):
            problems.append("collides with a special handler prefix")
        if not any(c in "qd" for c in sig):
            problems.append("may be parsed as a Bookkeeper mask")
        seen[sig] = pid
        if problems:
            bad = True
            print(f"{pid}: {sig} {start}: {', '.join(problems)}")
    print("checked", len(spec["patterns"]), "patterns;", "FAIL" if bad else "OK")
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
