package io.github.timursl.hexquantum.platform;

import io.github.timursl.hexquantum.data.PlayerQuantumData;
import io.github.timursl.hexquantum.registry.HQRegistrar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

/**
 * Everything the common code needs from the mod loader. Keep this small: anything that can be done with vanilla
 * code belongs in common.
 */
public interface IPlatform {
    /**
     * Create a registrar for the given registry. Entries are registered when the loader flushes its registrars.
     */
    <T> HQRegistrar<T> registrar(ResourceKey<? extends Registry<T>> registry);

    /**
     * Persistent per-player data (survives death and relogging).
     */
    PlayerQuantumData playerData(Player player);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    /**
     * Whether the player is a loader-provided fake player (machines etc.).
     */
    boolean isFakePlayer(Player player);

    /**
     * Vanilla's block entity factory interface is private (loaders widen it), so block entity types are built here.
     */
    <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks);
}
