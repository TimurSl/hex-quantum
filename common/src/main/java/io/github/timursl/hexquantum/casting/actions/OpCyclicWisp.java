package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.entity.CyclicWispEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Cyclic Wisp: [hex, position, interval seconds, count] -> wisp. Runs the hex every interval, count times.
 */
public class OpCyclicWisp extends HQAction {
    @Override
    protected int argc() {
        return 4;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        List<Iota> hex = HQArgs.hex(args, 0, argc());
        Vec3 pos = HQArgs.vector(args, 1, argc());
        double interval = HQArgs.numberBetween(args, 2, argc(), HQConfig.CYCLIC_WISP_MIN_INTERVAL.value(),
            HQConfig.CYCLIC_WISP_MAX_INTERVAL.value());
        int count = HQArgs.positiveInt(args, 3, argc(), HQConfig.CYCLIC_WISP_MAX_COUNT.value());
        env.assertVecInRange(pos);
        CyclicWispEntity wisp = CyclicWispEntity.create(player, pos, hex, Math.max(1, (int) Math.round(interval * 20)), count);
        return Result.of(HQConfig.CYCLIC_WISP_BASE_COST.media())
            .push(new EntityIota(wisp))
            .particles(ParticleSpray.burst(pos, 0.5, 20))
            .effect(e -> player.serverLevel().addFreshEntity(wisp));
    }
}
