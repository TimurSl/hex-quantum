package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.link.DamageLinks;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * Shared Damage / Distributed Damage: [entities] -> (). Links the entities permanently with a magical beam.
 */
public class OpDamageLink extends HQAction {
    private final DamageLinks.Mode mode;

    public OpDamageLink(DamageLinks.Mode mode) {
        this.mode = mode;
    }

    @Override
    protected int argc() {
        return 1;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        List<LivingEntity> entities = HQArgs.livingList(args, 0, argc(), env);
        int max = HQConfig.DAMAGE_LINK_MAX_ENTITIES.value();
        if (entities.size() < 2 || entities.size() > max) {
            throw MishapInvalidIota.of(args.get(0), 0, "hexquantum.link_size", 2, max);
        }
        for (LivingEntity e : entities) {
            env.assertEntityInRange(e);
        }
        long cost = HQConfig.DAMAGE_LINK_BASE_COST.media() + HQConfig.DAMAGE_LINK_COST_PER_ENTITY.media() * entities.size();
        Result result = Result.of(cost).effect(e -> DamageLinks.get(env.getWorld().getServer()).link(mode, entities));
        // The "beam": a line of particles from each member to the next.
        for (int i = 0; i < entities.size(); i++) {
            var from = entities.get(i).position().add(0, entities.get(i).getBbHeight() / 2, 0);
            var to = entities.get((i + 1) % entities.size()).position().add(0, 1, 0);
            result.particles(new ParticleSpray(from, to.subtract(from).normalize().scale(0.5), 0.1, 0.05, 15));
        }
        return result;
    }
}
