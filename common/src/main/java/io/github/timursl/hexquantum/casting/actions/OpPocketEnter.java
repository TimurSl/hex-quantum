package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.casting.mishaps.MishapQuantum;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.pocket.PocketManager;

import java.util.List;

/**
 * Pocket Dimension: step onto your own isolated platform. The place you left from is remembered for the Portal Door
 * and Exit Anchor (casting it again inside the pocket keeps the original entry point).
 */
public class OpPocketEnter extends HQAction {
    @Override
    protected int argc() {
        return 0;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        if (PocketManager.pocketLevel(player.server) == null) {
            throw new MishapQuantum("no_pocket_dimension");
        }
        return Result.of(HQConfig.POCKET_ENTER_COST.media())
            .particles(ParticleSpray.cloud(player.position().add(0, 1, 0), 1.0, 40))
            .effect(e -> PocketManager.enter(player));
    }
}
