package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.casting.HexStorage;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.entity.PhantomItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Phantom Item: [item entity, position] -> phantom item. Empowered: [item entity, position, hex] -> phantom item.
 * Drops a phantom copy of the item entity's stack (the original is untouched).
 */
public class OpPhantomItem extends HQAction {
    private final boolean empowered;

    public OpPhantomItem(boolean empowered) {
        this.empowered = empowered;
    }

    @Override
    protected int argc() {
        return empowered ? 3 : 2;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        var template = HQArgs.itemEntity(args, 0, argc(), env);
        Vec3 pos = HQArgs.vector(args, 1, argc());
        List<Iota> hex = empowered ? HQArgs.hex(args, 2, argc()) : null;
        env.assertEntityInRange(template);
        env.assertVecInRange(pos);
        var level = env.getWorld();
        PhantomItemEntity phantom = PhantomItemEntity.create(level, pos, template.getItem(), player.getUUID(),
            hex == null ? null : HexStorage.save(hex, level.registryAccess()));
        long cost = empowered ? HQConfig.EMPOWERED_PHANTOM_ITEM_COST.media() : HQConfig.PHANTOM_ITEM_COST.media();
        return Result.of(cost)
            .push(new EntityIota(phantom))
            .particles(ParticleSpray.cloud(pos, 0.4, 15))
            .effect(e -> level.addFreshEntity(phantom));
    }
}
