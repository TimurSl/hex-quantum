package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.entity.ProjectileWispEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Projectile Wisp: [hex, velocity] -> wisp. Fired from the caster's eyes; the vector's length is the speed in
 * blocks per tick.
 */
public class OpProjectileWisp extends HQAction {
    @Override
    protected int argc() {
        return 2;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        List<Iota> hex = HQArgs.hex(args, 0, argc());
        Vec3 velocity = HQArgs.vector(args, 1, argc());
        double speed = velocity.length();
        double max = HQConfig.PROJECTILE_WISP_MAX_SPEED.value();
        if (!(speed > 0.0001 && speed <= max)) {
            throw MishapInvalidIota.of(args.get(1), 0, "hexquantum.velocity", max);
        }
        long cost = HQConfig.PROJECTILE_WISP_BASE_COST.media()
            + Math.round(HQConfig.PROJECTILE_WISP_COST_PER_SPEED.media() * speed);
        ProjectileWispEntity wisp = ProjectileWispEntity.create(player, hex, velocity);
        return Result.of(cost)
            .push(new EntityIota(wisp))
            .effect(e -> player.serverLevel().addFreshEntity(wisp));
    }
}
