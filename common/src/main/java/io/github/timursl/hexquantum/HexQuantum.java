package io.github.timursl.hexquantum;

import com.mojang.logging.LogUtils;
import io.github.timursl.hexquantum.casting.QuantumPhialMedia;
import io.github.timursl.hexquantum.registry.HQActions;
import io.github.timursl.hexquantum.registry.HQBlocks;
import io.github.timursl.hexquantum.registry.HQEffects;
import io.github.timursl.hexquantum.registry.HQEntities;
import io.github.timursl.hexquantum.registry.HQIotaTypes;
import io.github.timursl.hexquantum.registry.HQItems;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

/**
 * Loader-independent entry point. Each loader calls {@link #init()} once from its mod constructor/initializer,
 * before it flushes its registrars.
 */
public final class HexQuantum {
    public static final String MOD_ID = "hexquantum";
    public static final Logger LOGGER = LogUtils.getLogger();

    private HexQuantum() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void init() {
        HQIotaTypes.init();
        HQActions.init();
        HQBlocks.init();
        HQItems.init();
        HQEntities.init();
        HQEffects.init();
        QuantumPhialMedia.init();
    }
}
