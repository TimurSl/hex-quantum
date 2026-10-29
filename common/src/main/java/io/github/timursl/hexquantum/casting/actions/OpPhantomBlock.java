package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock;
import io.github.timursl.hexquantum.block.PhantomBlock;
import io.github.timursl.hexquantum.block.PhantomBlockEntity;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.casting.HexStorage;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.registry.HQBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Phantom Block: [source, target] -> (). Empowered Phantom Block: [source, target, hex] -> ().
 * Places a block at target that looks like the block at source.
 */
public class OpPhantomBlock extends HQAction {
    private final boolean empowered;

    public OpPhantomBlock(boolean empowered) {
        this.empowered = empowered;
    }

    @Override
    protected int argc() {
        return empowered ? 3 : 2;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        BlockPos source = BlockPos.containing(HQArgs.vector(args, 0, argc()));
        BlockPos target = BlockPos.containing(HQArgs.vector(args, 1, argc()));
        List<Iota> hex = empowered ? HQArgs.hex(args, 2, argc()) : null;
        env.assertPosInRange(source);
        env.assertPosInRangeForEditing(target);
        var level = env.getWorld();
        BlockState mimic = level.getBlockState(source);
        if (mimic.isAir() || mimic.getBlock() instanceof PhantomBlock) {
            throw new MishapBadBlock(source, Component.translatable("hexcasting.mishap.bad_block.hexquantum.copyable"));
        }
        if (!level.getBlockState(target).canBeReplaced()) {
            throw new MishapBadBlock(target, Component.translatable("hexcasting.mishap.bad_block.replaceable"));
        }
        long cost = empowered ? HQConfig.EMPOWERED_PHANTOM_BLOCK_COST.media() : HQConfig.PHANTOM_BLOCK_COST.media();
        return Result.of(cost)
            .particles(ParticleSpray.cloud(Vec3.atCenterOf(target), 0.6, 20))
            .effect(e -> {
                if (!level.getBlockState(target).canBeReplaced()) {
                    return;
                }
                level.setBlock(target, HQBlocks.PHANTOM_BLOCK.value().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof PhantomBlockEntity be) {
                    be.setup(mimic, player.getUUID(), hex == null ? null : HexStorage.save(hex, level.registryAccess()));
                }
            });
    }
}
