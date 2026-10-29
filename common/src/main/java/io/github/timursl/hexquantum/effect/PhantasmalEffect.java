package io.github.timursl.hexquantum.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Marker effect for phantom mobs. Mob effects are sent to every client tracking the mob, which is how clients know
 * to draw regular phantoms see-through. The server re-applies it if anything removes it.
 */
public class PhantasmalEffect extends MobEffect {
    public PhantasmalEffect(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }
}
