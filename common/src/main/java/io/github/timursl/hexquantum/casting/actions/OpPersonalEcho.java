package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.entity.PersonalEchoEntity;

import java.util.List;

/**
 * Personal Echo: leave a stationary 1 HP decoy of yourself where you stand.
 */
public class OpPersonalEcho extends HQAction {
    @Override
    protected int argc() {
        return 0;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        PersonalEchoEntity echo = PersonalEchoEntity.create(player);
        return Result.of(HQConfig.PERSONAL_ECHO_COST.media())
            .push(new EntityIota(echo))
            .particles(ParticleSpray.cloud(player.position().add(0, 1, 0), 0.8, 20))
            .effect(e -> player.serverLevel().addFreshEntity(echo));
    }
}
