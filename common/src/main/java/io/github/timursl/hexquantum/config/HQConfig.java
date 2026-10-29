package io.github.timursl.hexquantum.config;

import at.petrak.hexcasting.api.misc.MediaConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Every media cost and numeric limit of the mod. The entries are declared here, loader-independently; each loader
 * builds its own config file from {@link #ALL} and {@link Entry#bind binds} the entries to it. Until bound, entries
 * return their defaults.
 * <p>
 * Media costs are written in amethyst dust (1 dust = {@value MediaConstants#DUST_UNIT} media units), the same unit
 * the Hex Casting book uses.
 */
public final class HQConfig {
    private static final List<Entry<?>> ENTRIES = new ArrayList<>();
    public static final List<Entry<?>> ALL = Collections.unmodifiableList(ENTRIES);

    private HQConfig() {
    }

    // ---- Media costs (in dust) ----
    public static final Cost STASH_OPEN_COST = cost("stashOpenCost", 0.1, "Pocket Stash: opening the stash");
    public static final Cost STASH_STORE_COST = cost("stashStoreCost", 0.25, "Pocket Stash: depositing an item entity");
    public static final Cost ILLUSION_SUMMON_COST = cost("illusionSummonCost", 5, "Summon Illusion");
    public static final Cost ILLUSION_DISPEL_COST = cost("illusionDispelCost", 0, "Dispel Illusion");
    public static final Cost ILLUSION_SWAP_BASE_COST = cost("illusionSwapBaseCost", 1, "Illusion Swap: base cost");
    public static final Cost ILLUSION_SWAP_COST_PER_BLOCK = cost("illusionSwapCostPerBlock", 0.1, "Illusion Swap: extra cost per block of distance");
    public static final Cost WAIT_COST = cost("waitCost", 0.05, "Rune of Waiting");
    public static final Cost MEDIA_RIFT_COST = cost("mediaRiftCost", 640, "Open Media Rift (great spell)");
    public static final Cost PHIAL_CHARGE_COST = cost("phialChargeCost", 0, "Phial Infusion");
    public static final Cost QUANTUM_SHIFT_BASE_COST = cost("quantumShiftBaseCost", 100, "Quantum Shift: base cost");
    public static final Cost QUANTUM_SHIFT_COST_PER_SECOND = cost("quantumShiftCostPerSecond", 10, "Quantum Shift: cost per second");
    public static final Cost DELAYED_ECHO_BASE_COST = cost("delayedEchoBaseCost", 5, "Delayed Echo: base cost");
    public static final Cost DELAYED_ECHO_COST_PER_SECOND = cost("delayedEchoCostPerSecond", 0.5, "Delayed Echo: cost per second of delay");
    public static final Cost PERSONAL_ECHO_COST = cost("personalEchoCost", 3, "Personal Echo");
    public static final Cost CYCLIC_ECHO_COST = cost("cyclicEchoCost", 5, "Cyclic Echo");
    public static final Cost CYCLIC_WISP_BASE_COST = cost("cyclicWispBaseCost", 5, "Cyclic Wisp: cost to summon");
    public static final Cost CYCLIC_WISP_COST_PER_CYCLE = cost("cyclicWispCostPerCycle", 0.5, "Cyclic Wisp: cost of each run (on top of the hex's own costs)");
    public static final Cost PROJECTILE_WISP_BASE_COST = cost("projectileWispBaseCost", 3, "Projectile Wisp: base cost");
    public static final Cost PROJECTILE_WISP_COST_PER_SPEED = cost("projectileWispCostPerSpeed", 1, "Projectile Wisp: cost per block/tick of speed");
    public static final Cost DAMAGE_LINK_BASE_COST = cost("damageLinkBaseCost", 10, "Shared/Distributed Damage: base cost");
    public static final Cost DAMAGE_LINK_COST_PER_ENTITY = cost("damageLinkCostPerEntity", 5, "Shared/Distributed Damage: cost per linked entity");
    public static final Cost PHANTOM_MOB_COST = cost("phantomMobCost", 10, "Phantom Mob");
    public static final Cost EMPOWERED_PHANTOM_MOB_COST = cost("empoweredPhantomMobCost", 100, "Empowered Phantom Mob");
    public static final Cost PHANTOM_BLOCK_COST = cost("phantomBlockCost", 1, "Phantom Block");
    public static final Cost EMPOWERED_PHANTOM_BLOCK_COST = cost("empoweredPhantomBlockCost", 20, "Empowered Phantom Block");
    public static final Cost PHANTOM_ITEM_COST = cost("phantomItemCost", 0.5, "Phantom Item");
    public static final Cost EMPOWERED_PHANTOM_ITEM_COST = cost("empoweredPhantomItemCost", 10, "Empowered Phantom Item");
    public static final Cost PHANTOM_STORAGE_OPEN_COST = cost("phantomStorageOpenCost", 0.1, "Phantom Vault: opening the vault");
    public static final Cost PHANTOM_STORAGE_STORE_COST = cost("phantomStorageStoreCost", 0.25, "Phantom Vault: storing a phantom item");
    public static final Cost PHANTOM_STORAGE_RELEASE_COST = cost("phantomStorageReleaseCost", 0.5, "Phantom Vault: releasing a phantom item");
    public static final Cost POCKET_ENTER_COST = cost("pocketEnterCost", 10, "Pocket Dimension");
    public static final Cost MIRROR_OPTICS_BASE_COST = cost("mirrorOpticsBaseCost", 5, "Mirror Optics: base cost");
    public static final Cost MIRROR_OPTICS_COST_PER_SECOND = cost("mirrorOpticsCostPerSecond", 0.5, "Mirror Optics: cost per second");
    public static final Cost SHIFTED_OPTICS_BASE_COST = cost("shiftedOpticsBaseCost", 5, "Shifted Optics: base cost");
    public static final Cost SHIFTED_OPTICS_COST_PER_SECOND = cost("shiftedOpticsCostPerSecond", 0.5, "Shifted Optics: cost per second");

    // ---- Limits ----
    public static final DoubleEntry ILLUSION_SWAP_MAX_RANGE = dbl("illusionSwapMaxRange", 64, 1, 4096, "Illusion Swap: maximum distance between caster and illusion, in blocks");
    public static final DoubleEntry ILLUSION_LURE_RADIUS = dbl("illusionLureRadius", 8, 0, 64, "Radius in which hostile mobs chasing the owner may switch to the illusion or a personal/cyclic echo (0 disables)");
    public static final DoubleEntry WAIT_MAX_SECONDS = dbl("waitMaxSeconds", 60, 0.05, 3600, "Rune of Waiting: longest allowed pause, in seconds");
    public static final IntEntry WAIT_MAX_PENDING = integer("waitMaxPendingPerPlayer", 16, 1, 1024, "Rune of Waiting: how many paused hexes one player may have at once");
    public static final DoubleEntry QUANTUM_SHIFT_MAX_SECONDS = dbl("quantumShiftMaxSeconds", 30, 1, 3600, "Quantum Shift: longest duration, in seconds");
    public static final DoubleEntry DELAYED_ECHO_MAX_SECONDS = dbl("delayedEchoMaxSeconds", 60, 0.05, 3600, "Delayed Echo: longest delay, in seconds");
    public static final DoubleEntry PERSONAL_ECHO_LIFETIME = dbl("personalEchoLifetimeSeconds", 60, 1, 3600, "Personal Echo: how long the decoy lasts, in seconds");
    public static final DoubleEntry CYCLIC_ECHO_LIFETIME = dbl("cyclicEchoLifetimeSeconds", 60, 1, 3600, "Cyclic Echo: how long the echo keeps replaying, in seconds");
    public static final DoubleEntry CYCLIC_ECHO_RECORD_SECONDS = dbl("cyclicEchoRecordSeconds", 5, 1, 30, "Cyclic Echo: how many seconds of movement are recorded and replayed");
    public static final IntEntry CYCLIC_WISP_MAX_COUNT = integer("cyclicWispMaxCount", 100, 1, 100000, "Cyclic Wisp: maximum number of runs");
    public static final DoubleEntry CYCLIC_WISP_MIN_INTERVAL = dbl("cyclicWispMinIntervalSeconds", 0.5, 0.05, 3600, "Cyclic Wisp: shortest interval between runs, in seconds");
    public static final DoubleEntry CYCLIC_WISP_MAX_INTERVAL = dbl("cyclicWispMaxIntervalSeconds", 60, 0.05, 3600, "Cyclic Wisp: longest interval between runs, in seconds");
    public static final DoubleEntry PROJECTILE_WISP_MAX_SPEED = dbl("projectileWispMaxSpeed", 3, 0.01, 10, "Projectile Wisp: maximum speed, in blocks per tick");
    public static final DoubleEntry PROJECTILE_WISP_LIFETIME = dbl("projectileWispLifetimeSeconds", 10, 1, 600, "Projectile Wisp: seconds before an unimpacted wisp fizzles");
    public static final IntEntry DAMAGE_LINK_MAX_ENTITIES = integer("damageLinkMaxEntities", 16, 2, 256, "Shared/Distributed Damage: maximum entities in one link");
    public static final DoubleEntry PHANTOM_HEALTH_MULTIPLIER = dbl("phantomHealthMultiplier", 0.5, 0.01, 1, "Regular phantom mobs: fraction of the original max health");
    public static final DoubleEntry PHANTOM_DAMAGE_MULTIPLIER = dbl("phantomDamageMultiplier", 0.5, 0, 1, "Regular phantom mobs: fraction of the damage they deal");
    public static final DoubleEntry PHANTOM_CREEPER_EXPLOSION_POWER = dbl("phantomCreeperExplosionPower", 2, 0, 16, "Regular phantom creepers: explosion power (they never break blocks)");
    public static final IntEntry EMPOWERED_TRIGGER_COOLDOWN = integer("empoweredTriggerCooldownTicks", 20, 0, 72000, "Empowered phantom mobs/blocks: ticks between two hex triggers");
    public static final IntEntry POCKET_PLATFORM_RADIUS = integer("pocketPlatformRadius", 8, 1, 64, "Pocket Dimension: half the side length of each player's platform (8 = 16x16)");
    public static final IntEntry POCKET_SPACING = integer("pocketSpacing", 10000, 256, 1000000, "Pocket Dimension: distance between two players' platforms, in blocks");
    public static final IntEntry POCKET_PLATFORM_Y = integer("pocketPlatformY", 64, 0, 250, "Pocket Dimension: Y level of the platforms");
    public static final DoubleEntry MIRROR_OPTICS_MAX_SECONDS = dbl("mirrorOpticsMaxSeconds", 120, 1, 3600, "Mirror Optics: longest duration, in seconds");
    public static final DoubleEntry SHIFTED_OPTICS_MAX_SECONDS = dbl("shiftedOpticsMaxSeconds", 120, 1, 3600, "Shifted Optics: longest duration, in seconds");
    public static final DoubleEntry SHIFTED_OPTICS_MAX_OFFSET = dbl("shiftedOpticsMaxOffset", 8, 0.1, 64, "Shifted Optics: maximum length of the offset vector, in blocks");
    public static final StringListEntry AMETHYST_MEDIA_VALUES = strings("amethystMediaValues", List.of(
        "hexcasting:amethyst_dust=1",
        "minecraft:amethyst_shard=5",
        "hexcasting:charged_amethyst=10",
        "minecraft:amethyst_block=20",
        "minecraft:amethyst_cluster=20",
        "minecraft:large_amethyst_bud=3",
        "minecraft:medium_amethyst_bud=2",
        "minecraft:small_amethyst_bud=1"
    ), "Phial Infusion: which items the Quantum Phial accepts, as item_id=dust per item");

    public static long dust(double dust) {
        return Math.round(dust * MediaConstants.DUST_UNIT);
    }

    // ---- Entry types ----

    public abstract static sealed class Entry<T> permits DoubleEntry, IntEntry, StringListEntry {
        public final String key;
        public final String comment;
        public final T defaultValue;
        private Supplier<T> source;

        Entry(String key, T defaultValue, String comment) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.comment = comment;
            ENTRIES.add(this);
        }

        public void bind(Supplier<T> source) {
            this.source = source;
        }

        public T get() {
            if (source == null) {
                return defaultValue;
            }
            try {
                T value = source.get();
                return value == null ? defaultValue : value;
            } catch (IllegalStateException notLoadedYet) {
                return defaultValue;
            }
        }
    }

    public static sealed class DoubleEntry extends Entry<Double> permits Cost {
        public final double min;
        public final double max;

        DoubleEntry(String key, double def, double min, double max, String comment) {
            super(key, def, comment);
            this.min = min;
            this.max = max;
        }

        public double value() {
            return get();
        }
    }

    /**
     * A media cost in dust. {@link #media()} returns raw media units.
     */
    public static final class Cost extends DoubleEntry {
        Cost(String key, double def, String comment) {
            super(key, def, 0, 1_000_000, comment + " (in amethyst dust)");
        }

        public long media() {
            return dust(value());
        }
    }

    public static final class IntEntry extends Entry<Integer> {
        public final int min;
        public final int max;

        IntEntry(String key, int def, int min, int max, String comment) {
            super(key, def, comment);
            this.min = min;
            this.max = max;
        }

        public int value() {
            return get();
        }
    }

    public static final class StringListEntry extends Entry<List<String>> {
        StringListEntry(String key, List<String> def, String comment) {
            super(key, def, comment);
        }
    }

    private static Cost cost(String key, double def, String comment) {
        return new Cost(key, def, comment);
    }

    private static DoubleEntry dbl(String key, double def, double min, double max, String comment) {
        return new DoubleEntry(key, def, min, max, comment);
    }

    private static IntEntry integer(String key, int def, int min, int max, String comment) {
        return new IntEntry(key, def, min, max, comment);
    }

    private static StringListEntry strings(String key, List<String> def, String comment) {
        return new StringListEntry(key, def, comment);
    }
}
