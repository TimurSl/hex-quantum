package io.github.timursl.hexquantum.registry;

import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.common.lib.HexRegistries;
import io.github.timursl.hexquantum.casting.iota.DimensionIota;
import io.github.timursl.hexquantum.platform.Services;
import net.minecraft.core.Holder;

public final class HQIotaTypes {
    private static final HQRegistrar<IotaType<?>> R = Services.PLATFORM.registrar(HexRegistries.IOTA_TYPE);

    public static final Holder<IotaType<?>> DIMENSION = R.register("dimension", () -> DimensionIota.TYPE);

    private HQIotaTypes() {
    }

    public static IotaType<DimensionIota> dimension() {
        return DimensionIota.TYPE;
    }

    public static void init() {
    }
}
