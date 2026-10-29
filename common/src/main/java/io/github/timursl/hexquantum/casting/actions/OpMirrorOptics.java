package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.optics.Optics;

import java.util.List;

/**
 * Mirror Optics: [player, seconds] -> (). The player sees a second, upside-down copy of the world mirrored at eye
 * level. Purely visual: nothing in it can be touched.
 */
public class OpMirrorOptics extends HQAction {
    @Override
    protected int argc() {
        return 2;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var target = HQArgs.player(args, 0, argc(), env);
        double seconds = HQArgs.seconds(args, 1, argc(), HQConfig.MIRROR_OPTICS_MAX_SECONDS.value());
        env.assertEntityInRange(target);
        long cost = HQConfig.MIRROR_OPTICS_BASE_COST.media() + Math.round(HQConfig.MIRROR_OPTICS_COST_PER_SECOND.media() * seconds);
        int ticks = Math.max(1, (int) Math.round(seconds * 20));
        return Result.of(cost).effect(e -> Optics.mirror(target, ticks));
    }
}
