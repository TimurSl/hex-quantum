package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster;
import net.minecraft.server.level.ServerPlayer;

/**
 * Most HexQuantum spells act on "the caster's" personal things and therefore need a player caster.
 */
final class Casters {
    private Casters() {
    }

    static ServerPlayer player(CastingEnvironment env) {
        if (env.getCastingEntity() instanceof ServerPlayer player) {
            return player;
        }
        throw new MishapBadCaster();
    }
}
