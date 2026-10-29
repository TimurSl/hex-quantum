# HexQuantum

An addon for [Hex Casting](https://github.com/FallingColors/HexMod) about illusions, echoes, phantoms, pocket space
and quantum media. Minecraft **1.21.1**, **NeoForge**, Java 21. Mod id: `hexquantum`.

> **Hex Casting version.** Hex Casting has no stable release for 1.21.1 yet, and its 1.21 development builds are
> not published to Maven. HexQuantum is built against development build `0.12.0-devel-pre-53` (Hex Casting commit
> `32c65158`, the build the current 1.21.1 addon ports use) and declares `hexcasting >= 0.12`. You need a 1.21.1 Hex
> Casting build and its dependencies (PAUCAL, Patchouli, Inline, Caelus, Kotlin for Forge) installed.

## Download

Every push and pull request is built by GitHub Actions.

1. Open the repository on GitHub and click the **Actions** tab.
2. Click the **Build** workflow on the left, then the most recent run with a green check mark (pick your branch if
   needed).
3. Scroll down to **Artifacts** and click **hexquantum-&lt;commit&gt;**. GitHub downloads a zip file (you must be
   signed in to GitHub to download artifacts).
4. Unzip it and put `hexquantum-neoforge-1.21.1-<version>.jar` into your `mods` folder.

Tagged versions (`v*`) are also published on the **Releases** page with the jar attached.

## Building

```sh
bash tools/build-hexcasting.sh   # once: builds the pinned Hex Casting commit into libs/ (CI caches this)
./gradlew build
```

The mod jar ends up in `build/libs/` (and `neoforge/build/libs/`). `./gradlew :neoforge:runClient` starts a dev client.
Instead of building Hex Casting you can put any `hexcasting-neoforge-1.21.1-<hexcasting_version>.jar` into `libs/`.

## Project structure

```
hex-quantum/
├── .github/workflows/build.yml      CI: build, upload jars, release on v* tags
├── build.gradle                     shared Gradle setup (repositories, Hex Casting dependency, jar collection)
├── settings.gradle, gradle.properties, gradlew(.bat), gradle/wrapper/
├── common/                          ALL gameplay code; compiled against vanilla Minecraft only (NeoForm)
│   └── src/main/
│       ├── java/io/github/timursl/hexquantum/
│       │   ├── HexQuantum.java          common entry point (init, ids)
│       │   ├── HQEvents.java            loader-independent event handlers
│       │   ├── platform/                IPlatform service interface + ServiceLoader lookup
│       │   ├── config/HQConfig.java     every media cost and numeric limit
│       │   ├── registry/                actions, iota types, blocks, items, entities, effects
│       │   ├── casting/                 HQAction base, argument readers, remote cast env, Wait scheduler,
│       │   │   ├── actions/             one Op* class per pattern (or pattern family)
│       │   │   ├── iota/                DimensionIota (custom iota type)
│       │   │   └── mishaps/             MishapQuantum
│       │   ├── entity/                  illusion, echoes, wisps, phantom items
│       │   ├── block/                   phantom block (+ block entity), portal door
│       │   ├── item/                    Quantum Phial, amethyst media values
│       │   ├── phantom/ link/ pocket/ shift/ optics/   spell systems
│       │   ├── data/                    per-player data (stash, vault, phial media, pocket entry), menus
│       │   ├── network/                 S2C payloads + client state
│       │   ├── client/                  renderers, mirror renderer, shifted picking
│       │   └── mixin/                   collision/picking/camera/render hooks (fail-soft)
│       └── resources/                   lang, Hex Casting book entries, models, textures, tags, pocket dimension
├── neoforge/                        NeoForge-only code: @Mod entry, IPlatform impl (registries, attachments,
│                                    networking), config file, event and renderer wiring, neoforge.mods.toml
├── tools/
│   ├── build-hexcasting.sh          builds the pinned Hex Casting 1.21 commit into libs/ (not on Maven)
│   ├── patterns.json                single source of truth for pattern names/signatures/stack effects/costs
│   ├── gen_assets.py                regenerates lang, book entries and docs/PATTERNS.md; checks HQActions.java
│   ├── check_patterns.py            validates signatures (no overlap, no clash with Hex Casting)
│   └── hexcasting_signatures.txt    every Hex Casting pattern signature (from its 1.21 source)
└── docs/PATTERNS.md                 the tables below
```

Adding Fabric later means adding a `fabric/` project with its own `IPlatform` implementation (registration,
player data, networking, config) and event wiring that calls `HQEvents`; nothing in `common/` changes.

## Patterns

All media costs and limits live in `hexquantum-server.toml` (per world, under `serverconfig/`). Costs are written in
amethyst dust. Every pattern has an entry in Hex Casting's own book categories (**Spells** and **Great Spells**; items and entities are under **Items**). The mod is translated into English and Russian (`ru_ru`).

## 1. Storage, Illusions and Waiting

| Name | Angle signature | Stack in | Stack out | Media cost |
|---|---|---|---|---|
| Stash Gate | `qaqdwdaqd` (start east) | — | — | 0.1 dust |
| Stash Offering | `qaqdwdaqa` (start east) | entity (item) | — | 0.25 dust |
| Summon Illusion | `eqqqqewded` (start north east) | — | entity (illusion) | 5 dust |
| Dispel Illusion | `qeeeeqwaqa` (start north west) | — | — | 0 dust |
| Illusion Swap | `eqqqqewdwdw` (start north east) | — | — | 1 dust + 0.1 dust × distance |
| Rune of Waiting | `qqqqqwdeddw` (start west) | number (seconds) | — | 0.05 dust |

## 2. Quantum Media

| Name | Angle signature | Stack in | Stack out | Media cost |
|---|---|---|---|---|
| Open Media Rift (great spell, per-world pattern) | `wqwqwqwddwddwqwqwqw` (start east) | — | — | 640 dust |
| Phial Infusion | `aqqqdwwdqqqa` (start south east) | entity (item) | — | 0 dust |
| Phial Reflection | `aqqqdwwdqq` (start south east) | — | number | 0 |
| Quantum Shift | `wdwewdwqaqqa` (start east) | number (seconds) | — | 100 dust + 10 dust × seconds |

## 3. Echoes and Wisps

| Name | Angle signature | Stack in | Stack out | Media cost |
|---|---|---|---|---|
| Delayed Echo | `dewewedqd` (start east) | list (hex), number (seconds) | entity (echo) | 5 dust + 0.5 dust × seconds |
| Personal Echo | `dewewedwa` (start east) | — | entity (echo) | 3 dust |
| Cyclic Echo | `dewewedeaqa` (start east) | — | entity (echo) | 5 dust |
| Cyclic Wisp | `wqqwqwqqwdd` (start south west) | list (hex), vector, number (interval s), number (count) | entity (wisp) | 5 dust now; 0.5 dust per run |
| Projectile Wisp | `wqqwqwqqwaa` (start south west) | list (hex), vector (velocity) | entity (wisp) | 3 dust + 1 dust × speed |

## 4. Damage Links

| Name | Angle signature | Stack in | Stack out | Media cost |
|---|---|---|---|---|
| Shared Damage | `qdqwdqdwqdq` (start north east) | list (entities) | — | 10 dust + 5 dust × n |
| Distributed Damage | `eaewaeaweaed` (start north west) | list (entities) | — | 10 dust + 5 dust × n |

## 5. Phantoms

| Name | Angle signature | Stack in | Stack out | Media cost |
|---|---|---|---|---|
| Phantom Mob | `aawqqwqqwdd` (start west) | entity (mob), vector | entity (phantom) | 10 dust |
| Empowered Phantom Mob | `aawqqwqqwddw` (start west) | entity (mob), vector, list (hex) | entity (phantom) | 100 dust |
| Phantom Block | `aawqqwqqwaa` (start west) | vector (source), vector (target) | — | 1 dust |
| Empowered Phantom Block | `aawqqwqqwaaqqq` (start west) | vector (source), vector (target), list (hex) | — | 20 dust |
| Phantom Item | `aawqqwqqwqe` (start west) | entity (item), vector | entity (phantom item) | 0.5 dust |
| Empowered Phantom Item | `aawqqwqqwqeqe` (start west) | entity (item), vector, list (hex) | entity (phantom item) | 10 dust |
| Phantom Vault Gate | `qaqdwdaqwa` (start east) | — | — | 0.1 dust |
| Phantom Vault Offering | `qaqdwdaqwd` (start east) | entity (phantom item) | — | 0.25 dust |
| Phantom Vault Release | `qaqdwdaqdw` (start east) | number (slot), vector | entity (phantom item) | 0.5 dust |

## 6. Pocket Space

| Name | Angle signature | Stack in | Stack out | Media cost |
|---|---|---|---|---|
| Pocket Dimension | `qwqwqwqeqaqeq` (start south east) | — | — | 10 dust |
| Exit Anchor | `qwqwqwqedwd` (start south east) | — | dimension, vector | 0 |

## 7. Optics

| Name | Angle signature | Stack in | Stack out | Media cost |
|---|---|---|---|---|
| Mirror Optics | `dwaqawdwe` (start north east) | entity (player), number (seconds) | — | 5 dust + 0.5 dust × seconds |
| Shifted Optics | `dwaqawdwq` (start north east) | entity (player), vector (offset), number (seconds) | — | 5 dust + 0.5 dust × seconds |

## Design notes

These follow the answers to the clarifying questions asked before implementation:

- **Pocket Stash / Phantom Vault** use the vanilla chest screen. The vault is view-only: phantom items only leave it
  through *Phantom Vault Release*, as phantoms again.
- **Rune of Waiting** halts the running hex and resumes its continuation later, in the same casting environment and
  with the same stack. When staff-casting, the paused part runs in the background (the staff grid is cleared). It is
  cancelled if the caster logs out or dies, or the server stops.
- **Open Media Rift** is a great spell (per-world pattern, requires enlightenment) and gives the **Quantum Phial**
  (the "Rift Phial" is the same item). Recasting gives a fresh phial and invalidates older ones; the media is stored
  in player data, so nothing is lost. The phial is charged with *Phial Infusion* (phial in the other hand, amethyst
  item entity as the target) and pays for casts automatically from anywhere in the owner's inventory, before amethyst
  items and before overcasting.
- **Echoes, wisps and empowered phantoms** cast as their owner: media comes from the owner, ambit is centred on the
  echo/wisp/phantom. Delayed Echo and Cyclic Wisp start with themselves on the stack; Projectile Wisp and empowered
  phantoms start with the entity that was hit / that interacted (Projectile Wisp pushes the block centre when it hits a
  block).
- **Damage links** are permanent; members that die or unload are dropped and the split is recalculated. Linked damage
  is magic damage. An entity belongs to at most one link.
- **Phantom mobs** copy the type of an existing mob (not players, bosses or other HexQuantum entities). Regular ones are
  see-through (vanilla's "invisible but visible" rendering), with configurable health and damage multipliers;
  phantom creepers explode with power 2 by default and never break blocks. No phantom drops loot or experience.
- **Pocket Dimension** is one shared void dimension with an isolated 16x16 platform per player, 10 000 blocks apart.
  The Portal Door on the platform returns you to where you entered; *Exit Anchor* pushes that dimension (new
  `dimension` iota) and position.
- **Mirror Optics** draws the terrain a second time mirrored across the horizontal plane at eye level (terrain only,
  not entities). **Shifted Optics** moves the camera and crosshair picking; the server extends reach by the offset
  length so interactions from the shifted viewpoint are accepted.
- Haunt (#11), Pocket Mobs (#23) and Pocket Phantoms (#24) were dropped from the original list.

### Known limitations

- This code has only been compiled by CI, not play-tested. The client hooks (Quantum Shift collision, Mirror and
  Shifted Optics, see-through phantoms) use `require = 0` mixins and reflection, so if a target does not match they
  log an error and that effect is disabled; the game keeps running.
- Quantum Shift makes the caster immune to damage, projectiles and mob targeting; other casters' Hex spells that
  don't deal damage (e.g. Impulse) still affect them.
- Illusions, echoes and wisps are not saved with the world; they disappear on restart, as do paused hexes.
- Textures are simple placeholders.
