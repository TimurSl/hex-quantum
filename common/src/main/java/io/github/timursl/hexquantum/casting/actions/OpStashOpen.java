package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.data.QuantumMenus;

import java.util.List;

/**
 * Pocket Stash: open the caster's personal chest from anywhere.
 */
public class OpStashOpen extends HQAction {
    @Override
    protected int argc() {
        return 0;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        return Result.of(HQConfig.STASH_OPEN_COST.media()).effect(e -> QuantumMenus.openStash(player));
    }
}
