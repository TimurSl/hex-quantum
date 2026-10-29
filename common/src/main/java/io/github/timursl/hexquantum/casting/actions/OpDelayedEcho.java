package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.entity.DelayedEchoEntity;

import java.util.List;

/**
 * Delayed Echo: [hex, seconds] -> echo. An intangible copy appears where the caster stands and, after the delay,
 * casts the hex once from its own position (the echo is on the starting stack; media is charged to the caster).
 */
public class OpDelayedEcho extends HQAction {
    @Override
    protected int argc() {
        return 2;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        List<Iota> hex = HQArgs.hex(args, 0, argc());
        double seconds = HQArgs.seconds(args, 1, argc(), HQConfig.DELAYED_ECHO_MAX_SECONDS.value());
        long cost = HQConfig.DELAYED_ECHO_BASE_COST.media()
            + Math.round(HQConfig.DELAYED_ECHO_COST_PER_SECOND.media() * seconds);
        DelayedEchoEntity echo = DelayedEchoEntity.create(player, hex, Math.max(1, (int) Math.round(seconds * 20)));
        return Result.of(cost)
            .push(new EntityIota(echo))
            .particles(ParticleSpray.cloud(player.position().add(0, 1, 0), 0.8, 20))
            .effect(e -> player.serverLevel().addFreshEntity(echo));
    }
}
