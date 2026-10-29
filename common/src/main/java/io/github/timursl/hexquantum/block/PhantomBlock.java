package io.github.timursl.hexquantum.block;

import com.mojang.serialization.MapCodec;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.phantom.PhantomTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A phantom block: looks (and collides) like the block it copies. A regular one vanishes when touched (used,
 * hit or broken); an empowered one stays and runs its creator's hex instead.
 */
public class PhantomBlock extends BaseEntityBlock {
    public static final MapCodec<PhantomBlock> CODEC = simpleCodec(PhantomBlock::new);

    public PhantomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PhantomBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    private static BlockState mimicAt(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof PhantomBlockEntity be && !(be.getMimic().getBlock() instanceof PhantomBlock)) {
            return be.getMimic();
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = mimicAt(level, pos).getShape(level, pos, context);
        return shape.isEmpty() ? Shapes.block() : shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return mimicAt(level, pos).getCollisionShape(level, pos, context);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        touch(level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
        InteractionHand hand, BlockHitResult hit) {
        touch(level, pos, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        touch(level, pos, player);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        touch(level, pos, player);
        return super.playerWillDestroy(level, pos, state, player);
    }

    private static void touch(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel server) || !(level.getBlockEntity(pos) instanceof PhantomBlockEntity be)) {
            return;
        }
        if (be.isEmpowered()) {
            if (be.tryTrigger(level.getGameTime(), HQConfig.EMPOWERED_TRIGGER_COOLDOWN.value())) {
                PhantomTriggers.fire(server, Vec3.atCenterOf(pos), be.getOwner(), be.getHex(), player);
            }
            return;
        }
        level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.BLOCKS, 0.6f, 1.4f);
        server.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.01);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof PhantomBlockEntity be && be.isEmpowered() && random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                pos.getZ() + random.nextDouble(), 0, 0.02, 0);
        }
    }
}
