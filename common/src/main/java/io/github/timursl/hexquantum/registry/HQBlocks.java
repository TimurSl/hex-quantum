package io.github.timursl.hexquantum.registry;

import io.github.timursl.hexquantum.block.PhantomBlock;
import io.github.timursl.hexquantum.block.PhantomBlockEntity;
import io.github.timursl.hexquantum.block.PortalDoorBlock;
import io.github.timursl.hexquantum.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class HQBlocks {
    private static final HQRegistrar<Block> BLOCKS = Services.PLATFORM.registrar(Registries.BLOCK);
    private static final HQRegistrar<BlockEntityType<?>> BLOCK_ENTITIES = Services.PLATFORM.registrar(Registries.BLOCK_ENTITY_TYPE);

    public static final Holder<Block> PHANTOM_BLOCK = BLOCKS.register("phantom_block", () -> new PhantomBlock(
        BlockBehaviour.Properties.of()
            .strength(0.3f)
            .noOcclusion()
            .noLootTable()
            .pushReaction(PushReaction.BLOCK)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)));

    public static final Holder<Block> PORTAL_DOOR = BLOCKS.register("portal_door", () -> new PortalDoorBlock(
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(-1.0f, 3600000.0f)
            .noLootTable()
            .lightLevel(state -> 10)
            .sound(SoundType.AMETHYST)
            .pushReaction(PushReaction.BLOCK)));

    public static final Holder<BlockEntityType<?>> PHANTOM_BLOCK_ENTITY = BLOCK_ENTITIES.register("phantom_block",
        () -> Services.PLATFORM.blockEntityType(PhantomBlockEntity::new, PHANTOM_BLOCK.value()));

    private HQBlocks() {
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<PhantomBlockEntity> phantomBlockEntity() {
        return (BlockEntityType<PhantomBlockEntity>) PHANTOM_BLOCK_ENTITY.value();
    }

    public static void init() {
    }
}
