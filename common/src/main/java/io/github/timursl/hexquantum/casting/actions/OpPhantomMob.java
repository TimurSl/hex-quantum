package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.casting.HexStorage;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.phantom.PhantomMobs;
import io.github.timursl.hexquantum.phantom.PhantomRegistry;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Phantom Mob: [mob, position] -> phantom. Empowered Phantom Mob: [mob, position, hex] -> phantom.
 */
public class OpPhantomMob extends HQAction {
    private final boolean empowered;

    public OpPhantomMob(boolean empowered) {
        this.empowered = empowered;
    }

    @Override
    protected int argc() {
        return empowered ? 3 : 2;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        Entity template = HQArgs.entity(args, 0, argc(), env);
        Vec3 pos = HQArgs.vector(args, 1, argc());
        List<Iota> hex = empowered ? HQArgs.hex(args, 2, argc()) : null;
        env.assertEntityInRange(template);
        env.assertVecInRange(pos);
        if (!PhantomMobs.canCopy(template)) {
            throw MishapBadEntity.of(template, "hexquantum.phantom_template");
        }
        Mob phantom = PhantomMobs.create(env.getWorld(), template, pos, empowered);
        if (phantom == null) {
            throw MishapBadEntity.of(template, "hexquantum.phantom_template");
        }
        long cost = empowered ? HQConfig.EMPOWERED_PHANTOM_MOB_COST.media() : HQConfig.PHANTOM_MOB_COST.media();
        return Result.of(cost)
            .push(new EntityIota(phantom))
            .particles(ParticleSpray.cloud(pos.add(0, phantom.getBbHeight() / 2, 0), 1.0, 30))
            .effect(e -> {
                if (hex != null) {
                    Tag stored = HexStorage.save(hex, env.getWorld().registryAccess());
                    PhantomRegistry.get(env.getWorld().getServer()).put(phantom.getUUID(), player.getUUID(), stored);
                }
                env.getWorld().addFreshEntityWithPassengers(phantom);
            });
    }
}
