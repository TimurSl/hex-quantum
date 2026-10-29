package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.entity.IllusionManager;

import java.util.List;

/**
 * Dispel Illusion: the cancel pattern for the caster's illusion.
 */
public class OpDispelIllusion extends HQAction {
    @Override
    protected int argc() {
        return 0;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        return Result.of(HQConfig.ILLUSION_DISPEL_COST.media()).effect(e -> IllusionManager.dispel(player));
    }
}
