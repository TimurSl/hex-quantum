package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.optics.Optics;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Shifted Optics: [player, offset, seconds] -> (). The player's view is displaced by the offset; aiming and
 * interacting happen from the displaced viewpoint.
 */
public class OpShiftedOptics extends HQAction {
    @Override
    protected int argc() {
        return 3;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var target = HQArgs.player(args, 0, argc(), env);
        Vec3 offset = HQArgs.vector(args, 1, argc());
        double seconds = HQArgs.seconds(args, 2, argc(), HQConfig.SHIFTED_OPTICS_MAX_SECONDS.value());
        double maxOffset = HQConfig.SHIFTED_OPTICS_MAX_OFFSET.value();
        if (offset.length() > maxOffset) {
            throw MishapInvalidIota.of(args.get(1), 1, "hexquantum.offset", maxOffset);
        }
        env.assertEntityInRange(target);
        long cost = HQConfig.SHIFTED_OPTICS_BASE_COST.media() + Math.round(HQConfig.SHIFTED_OPTICS_COST_PER_SECOND.media() * seconds);
        int ticks = Math.max(1, (int) Math.round(seconds * 20));
        return Result.of(cost).effect(e -> Optics.shift(target, offset, ticks));
    }
}
