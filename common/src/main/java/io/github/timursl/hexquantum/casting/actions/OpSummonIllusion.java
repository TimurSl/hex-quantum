package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.entity.IllusionEntity;
import io.github.timursl.hexquantum.entity.IllusionManager;

import java.util.List;

/**
 * Summon Illusion: create (or replace) the caster's illusion where the caster stands and push it.
 */
public class OpSummonIllusion extends HQAction {
    @Override
    protected int argc() {
        return 0;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        // Created now so its iota can be pushed; only added to the world once the spell actually goes through.
        IllusionEntity illusion = IllusionEntity.create(player);
        return Result.of(HQConfig.ILLUSION_SUMMON_COST.media())
            .push(new EntityIota(illusion))
            .particles(ParticleSpray.cloud(player.position().add(0, 1, 0), 1.0, 30))
            .effect(e -> IllusionManager.summon(player, illusion));
    }
}
