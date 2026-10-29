package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.misc.MediaConstants;
import io.github.timursl.hexquantum.platform.Services;

import java.util.List;

/**
 * Phial Reflection: push how much media (in dust) the caster's Quantum Phial holds.
 */
public class OpPhialRead extends HQAction {
    @Override
    protected int argc() {
        return 0;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        double dust = Services.PLATFORM.playerData(player).quantumMedia() / (double) MediaConstants.DUST_UNIT;
        return Result.of(0).push(new DoubleIota(dust));
    }
}
