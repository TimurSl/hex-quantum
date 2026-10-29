package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.casting.mishaps.MishapQuantum;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.entity.CyclicEchoEntity;
import io.github.timursl.hexquantum.entity.MovementRecorder;

import java.util.List;

/**
 * Cyclic Echo: an echo that endlessly replays the caster's last few seconds of movement.
 */
public class OpCyclicEcho extends HQAction {
    @Override
    protected int argc() {
        return 0;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        List<MovementRecorder.Frame> route = MovementRecorder.snapshot(player);
        if (route.isEmpty()) {
            throw new MishapQuantum("no_route");
        }
        CyclicEchoEntity echo = CyclicEchoEntity.create(player, route);
        return Result.of(HQConfig.CYCLIC_ECHO_COST.media())
            .push(new EntityIota(echo))
            .particles(ParticleSpray.cloud(echo.position().add(0, 1, 0), 0.8, 20))
            .effect(e -> player.serverLevel().addFreshEntity(echo));
    }
}
