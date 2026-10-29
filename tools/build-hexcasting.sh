#!/usr/bin/env bash
# Builds the Hex Casting NeoForge 1.21.1 jar from source and puts it in libs/.
#
# Hex Casting only publishes builds of its 1.20.1 branch to Maven; the 1.21 development builds
# (0.12.0-devel-pre-N) never reach maven.blamejared.com. CI therefore builds the pinned commit
# (hexcasting_commit in gradle.properties) itself. You can run this locally too, or drop a
# hexcasting-neoforge-1.21.1-<hexcasting_version>.jar into libs/ by hand.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
prop() { grep "^$1=" "$root/gradle.properties" | cut -d= -f2-; }
commit="$(prop hexcasting_commit)"
version="$(prop hexcasting_version)"
mc="$(prop minecraft_version)"
target="$root/libs/hexcasting-neoforge-${mc}-${version}.jar"

if [[ -f "$target" ]]; then
    echo "Already present: $target"
    exit 0
fi

work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
git clone --filter=blob:none --no-checkout https://github.com/FallingColors/HexMod.git "$work/HexMod"
git -C "$work/HexMod" checkout --quiet "$commit"

(cd "$work/HexMod" && chmod +x gradlew && ./gradlew :Neoforge:assemble --no-daemon --stacktrace)

jar="$(find "$work/HexMod/Neoforge/build/libs" -maxdepth 1 -name "hexcasting-neoforge-${mc}-*.jar" \
    ! -name '*-sources.jar' ! -name '*-dev.jar' ! -name '*-dev-shadow.jar' ! -name '*-javadoc.jar' | head -n 1)"
if [[ -z "$jar" ]]; then
    echo "Hex Casting build produced no jar" >&2
    ls -l "$work/HexMod/Neoforge/build/libs" >&2 || true
    exit 1
fi
mkdir -p "$root/libs"
cp "$jar" "$target"
echo "Built $(basename "$jar") -> $target"
