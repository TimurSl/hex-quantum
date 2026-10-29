package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.casting.mishaps.MishapQuantum;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.entity.IllusionEntity;
import io.github.timursl.hexquantum.entity.IllusionManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Illusion Swap: the caster and their illusion trade places. Cost grows with distance.
 */
public class OpIllusionSwap extends HQAction {
    @Override
    protected int argc() {
        return 0;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        IllusionEntity illusion = IllusionManager.find(player);
        if (illusion == null || illusion.level() != player.level()) {
            throw new MishapQuantum("no_illusion");
        }
        double distance = illusion.position().distanceTo(player.position());
        double max = HQConfig.ILLUSION_SWAP_MAX_RANGE.value();
        if (distance > max) {
            throw new MishapQuantum("illusion_too_far", Math.round(distance), Math.round(max));
        }
        long cost = HQConfig.ILLUSION_SWAP_BASE_COST.media()
            + Math.round(HQConfig.ILLUSION_SWAP_COST_PER_BLOCK.media() * distance);
        Vec3 playerPos = player.position();
        Vec3 illusionPos = illusion.position();
        return Result.of(cost)
            .particles(ParticleSpray.cloud(playerPos.add(0, 1, 0), 1.0, 30))
            .particles(ParticleSpray.cloud(illusionPos.add(0, 1, 0), 1.0, 30))
            .effect(e -> {
                if (!illusion.isAlive()) {
                    return;
                }
                illusion.moveTo(playerPos.x, playerPos.y, playerPos.z, player.getYRot(), player.getXRot());
                player.teleportTo(illusionPos.x, illusionPos.y, illusionPos.z);
                player.resetFallDistance();
                burst(player.serverLevel(), illusionPos);
                burst(player.serverLevel(), playerPos);
            });
    }

    private static void burst(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y + 1, pos.z, 3, 0.4, 0.5, 0.4, 0.0);
        level.sendParticles(ParticleTypes.POOF, pos.x, pos.y + 1, pos.z, 40, 0.3, 0.6, 0.3, 0.12);
        level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1, pos.z, 30, 0.3, 0.6, 0.3, 0.2);
    }
}
