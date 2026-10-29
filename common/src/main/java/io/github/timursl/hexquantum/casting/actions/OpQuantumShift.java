package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.shift.QuantumShift;

import java.util.List;

/**
 * Quantum Shift: phase through blocks for N seconds. Recasting while shifted ends it early (no refund, no cost).
 */
public class OpQuantumShift extends HQAction {
    @Override
    protected int argc() {
        return 1;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        double seconds = HQArgs.seconds(args, 0, argc(), HQConfig.QUANTUM_SHIFT_MAX_SECONDS.value());
        if (QuantumShift.isShifting(player)) {
            return Result.of(0).effect(e -> QuantumShift.end(player));
        }
        long cost = HQConfig.QUANTUM_SHIFT_BASE_COST.media()
            + Math.round(HQConfig.QUANTUM_SHIFT_COST_PER_SECOND.media() * seconds);
        int ticks = Math.max(1, (int) Math.round(seconds * 20));
        return Result.of(cost)
            .particles(ParticleSpray.cloud(player.position().add(0, 1, 0), 1.0, 40))
            .effect(e -> QuantumShift.start(player, ticks));
    }
}
