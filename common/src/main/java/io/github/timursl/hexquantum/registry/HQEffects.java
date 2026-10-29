package io.github.timursl.hexquantum.registry;

import io.github.timursl.hexquantum.effect.PhantasmalEffect;
import io.github.timursl.hexquantum.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

public final class HQEffects {
    private static final HQRegistrar<MobEffect> R = Services.PLATFORM.registrar(Registries.MOB_EFFECT);

    public static final Holder<MobEffect> PHANTASMAL = R.register("phantasmal", () -> new PhantasmalEffect(0x9a7fd1));
    public static final Holder<MobEffect> EMPOWERED_PHANTASMAL = R.register("empowered_phantasmal", () -> new PhantasmalEffect(0x3b1d63));

    private HQEffects() {
    }

    public static void init() {
    }
}
