package io.github.timursl.hexquantum.registry;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexRegistries;
import io.github.timursl.hexquantum.casting.actions.OpCyclicEcho;
import io.github.timursl.hexquantum.casting.actions.OpCyclicWisp;
import io.github.timursl.hexquantum.casting.actions.OpDamageLink;
import io.github.timursl.hexquantum.casting.actions.OpDelayedEcho;
import io.github.timursl.hexquantum.casting.actions.OpDispelIllusion;
import io.github.timursl.hexquantum.casting.actions.OpExitAnchor;
import io.github.timursl.hexquantum.casting.actions.OpIllusionSwap;
import io.github.timursl.hexquantum.casting.actions.OpMediaRift;
import io.github.timursl.hexquantum.casting.actions.OpMirrorOptics;
import io.github.timursl.hexquantum.casting.actions.OpPersonalEcho;
import io.github.timursl.hexquantum.casting.actions.OpPhantomBlock;
import io.github.timursl.hexquantum.casting.actions.OpPhantomItem;
import io.github.timursl.hexquantum.casting.actions.OpPhantomMob;
import io.github.timursl.hexquantum.casting.actions.OpPhantomVault;
import io.github.timursl.hexquantum.casting.actions.OpPhialCharge;
import io.github.timursl.hexquantum.casting.actions.OpPhialRead;
import io.github.timursl.hexquantum.casting.actions.OpPocketEnter;
import io.github.timursl.hexquantum.casting.actions.OpProjectileWisp;
import io.github.timursl.hexquantum.casting.actions.OpQuantumShift;
import io.github.timursl.hexquantum.casting.actions.OpShiftedOptics;
import io.github.timursl.hexquantum.casting.actions.OpStashOpen;
import io.github.timursl.hexquantum.casting.actions.OpStashStore;
import io.github.timursl.hexquantum.casting.actions.OpSummonIllusion;
import io.github.timursl.hexquantum.casting.actions.OpWait;
import io.github.timursl.hexquantum.link.DamageLinks;
import io.github.timursl.hexquantum.platform.Services;
import net.minecraft.core.Holder;

/**
 * Every HexQuantum pattern. Keep in sync with tools/patterns.json (tools/gen_assets.py checks this).
 */
public final class HQActions {
    private static final HQRegistrar<ActionRegistryEntry> R = Services.PLATFORM.registrar(HexRegistries.ACTION);

    // 1. Storage, illusions, timing
    public static final Holder<ActionRegistryEntry> STASH_OPEN = make("pocket_stash/open", "qaqdwdaqd", HexDir.EAST, new OpStashOpen());
    public static final Holder<ActionRegistryEntry> STASH_STORE = make("pocket_stash/store", "qaqdwdaqa", HexDir.EAST, new OpStashStore());
    public static final Holder<ActionRegistryEntry> ILLUSION_SUMMON = make("illusion/summon", "eqqqqewded", HexDir.NORTH_EAST, new OpSummonIllusion());
    public static final Holder<ActionRegistryEntry> ILLUSION_DISPEL = make("illusion/dispel", "qeeeeqwaqa", HexDir.NORTH_WEST, new OpDispelIllusion());
    public static final Holder<ActionRegistryEntry> ILLUSION_SWAP = make("illusion/swap", "eqqqqewdwdw", HexDir.NORTH_EAST, new OpIllusionSwap());
    public static final Holder<ActionRegistryEntry> WAIT = make("wait", "qqqqqwdeddw", HexDir.WEST, new OpWait());

    // 2. Media
    public static final Holder<ActionRegistryEntry> MEDIA_RIFT = make("media_rift", "wqwqwqwddwddwqwqwqw", HexDir.EAST, new OpMediaRift());
    public static final Holder<ActionRegistryEntry> PHIAL_CHARGE = make("phial/charge", "aqqqdwwdqqqa", HexDir.SOUTH_EAST, new OpPhialCharge());
    public static final Holder<ActionRegistryEntry> PHIAL_READ = make("phial/read", "aqqqdwwdqq", HexDir.SOUTH_EAST, new OpPhialRead());
    public static final Holder<ActionRegistryEntry> QUANTUM_SHIFT = make("quantum_shift", "wdwewdwqaqqa", HexDir.EAST, new OpQuantumShift());

    // 3. Echoes and wisps
    public static final Holder<ActionRegistryEntry> ECHO_DELAYED = make("echo/delayed", "dewewedqd", HexDir.EAST, new OpDelayedEcho());
    public static final Holder<ActionRegistryEntry> ECHO_PERSONAL = make("echo/personal", "dewewedwa", HexDir.EAST, new OpPersonalEcho());
    public static final Holder<ActionRegistryEntry> ECHO_CYCLIC = make("echo/cyclic", "dewewedeaqa", HexDir.EAST, new OpCyclicEcho());
    public static final Holder<ActionRegistryEntry> WISP_CYCLIC = make("wisp/cyclic", "wqqwqwqqwdd", HexDir.SOUTH_WEST, new OpCyclicWisp());
    public static final Holder<ActionRegistryEntry> WISP_PROJECTILE = make("wisp/projectile", "wqqwqwqqwaa", HexDir.SOUTH_WEST, new OpProjectileWisp());

    // 4. Damage links
    public static final Holder<ActionRegistryEntry> LINK_SHARED = make("link/shared", "qdqwdqdwqdq", HexDir.NORTH_EAST, new OpDamageLink(DamageLinks.Mode.SHARED));
    public static final Holder<ActionRegistryEntry> LINK_DISTRIBUTED = make("link/distributed", "eaewaeaweaed", HexDir.NORTH_WEST, new OpDamageLink(DamageLinks.Mode.DISTRIBUTED));

    // 5. Phantoms
    public static final Holder<ActionRegistryEntry> PHANTOM_MOB = make("phantom/mob", "aawqqwqqwdd", HexDir.WEST, new OpPhantomMob(false));
    public static final Holder<ActionRegistryEntry> PHANTOM_MOB_EMPOWERED = make("phantom/mob/empowered", "aawqqwqqwddw", HexDir.WEST, new OpPhantomMob(true));
    public static final Holder<ActionRegistryEntry> PHANTOM_BLOCK = make("phantom/block", "aawqqwqqwaa", HexDir.WEST, new OpPhantomBlock(false));
    public static final Holder<ActionRegistryEntry> PHANTOM_BLOCK_EMPOWERED = make("phantom/block/empowered", "aawqqwqqwaaqqq", HexDir.WEST, new OpPhantomBlock(true));
    public static final Holder<ActionRegistryEntry> PHANTOM_ITEM = make("phantom/item", "aawqqwqqwqe", HexDir.WEST, new OpPhantomItem(false));
    public static final Holder<ActionRegistryEntry> PHANTOM_ITEM_EMPOWERED = make("phantom/item/empowered", "aawqqwqqwqeqe", HexDir.WEST, new OpPhantomItem(true));
    public static final Holder<ActionRegistryEntry> PHANTOM_VAULT_OPEN = make("phantom_storage/open", "qaqdwdaqwa", HexDir.EAST, new OpPhantomVault(OpPhantomVault.Mode.OPEN));
    public static final Holder<ActionRegistryEntry> PHANTOM_VAULT_STORE = make("phantom_storage/store", "qaqdwdaqwd", HexDir.EAST, new OpPhantomVault(OpPhantomVault.Mode.STORE));
    public static final Holder<ActionRegistryEntry> PHANTOM_VAULT_RELEASE = make("phantom_storage/release", "qaqdwdaqdw", HexDir.EAST, new OpPhantomVault(OpPhantomVault.Mode.RELEASE));

    // 6. Pocket space
    public static final Holder<ActionRegistryEntry> POCKET_ENTER = make("pocket/enter", "qwqwqwqeqaqeq", HexDir.SOUTH_EAST, new OpPocketEnter());
    public static final Holder<ActionRegistryEntry> EXIT_ANCHOR = make("pocket/exit_anchor", "qwqwqwqedwd", HexDir.SOUTH_EAST, new OpExitAnchor());

    // 7. Optics
    public static final Holder<ActionRegistryEntry> MIRROR_OPTICS = make("optics/mirror", "dwaqawdwe", HexDir.NORTH_EAST, new OpMirrorOptics());
    public static final Holder<ActionRegistryEntry> SHIFTED_OPTICS = make("optics/shifted", "dwaqawdwq", HexDir.NORTH_EAST, new OpShiftedOptics());

    private HQActions() {
    }

    private static Holder<ActionRegistryEntry> make(String name, String signature, HexDir startDir, Action action) {
        return R.register(name, () -> new ActionRegistryEntry(HexPattern.fromAngleString(signature, startDir), action));
    }

    public static void init() {
    }
}
