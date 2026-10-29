package io.github.timursl.hexquantum.pocket;

import io.github.timursl.hexquantum.HexQuantum;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.data.PlayerQuantumData;
import io.github.timursl.hexquantum.platform.Services;
import io.github.timursl.hexquantum.registry.HQBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Personal pocket platforms. All players share one void dimension ({@code hexquantum:pocket}); each player gets an
 * isolated platform at x = index * pocketSpacing, built the first time they enter. A Portal Door on the platform
 * leads back to where they came from.
 */
public final class PocketManager {
    public static final ResourceKey<Level> POCKET = ResourceKey.create(Registries.DIMENSION, HexQuantum.id("pocket"));

    private PocketManager() {
    }

    public static boolean isPocket(Level level) {
        return level.dimension().equals(POCKET);
    }

    @Nullable
    public static ServerLevel pocketLevel(MinecraftServer server) {
        return server.getLevel(POCKET);
    }

    /**
     * Centre of the floor of {@code player}'s platform (the block they stand on).
     */
    public static BlockPos platformCenter(MinecraftServer server, UUID player) {
        int index = Index.get(server).indexOf(player);
        return new BlockPos(index * HQConfig.POCKET_SPACING.value(), HQConfig.POCKET_PLATFORM_Y.value(), 0);
    }

    public static void enter(ServerPlayer player) {
        ServerLevel pocket = pocketLevel(player.server);
        if (pocket == null) {
            return;
        }
        Index index = Index.get(player.server);
        BlockPos center = platformCenter(player.server, player.getUUID());
        if (index.markBuilt(player.getUUID())) {
            buildPlatform(pocket, center);
        }
        if (!isPocket(player.level())) {
            Services.PLATFORM.playerData(player).setPocketEntry(player.level().dimension(), player.position());
        }
        player.teleportTo(pocket, center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.resetFallDistance();
    }

    /**
     * Send the player back where they entered from (world spawn if that is unknown or gone).
     */
    public static void exit(ServerPlayer player) {
        PlayerQuantumData data = Services.PLATFORM.playerData(player);
        ServerLevel target = data.pocketEntryDimension() == null ? null : player.server.getLevel(data.pocketEntryDimension());
        Vec3 pos = data.pocketEntryPos();
        if (target == null || pos == null || isPocket(target)) {
            target = player.server.overworld();
            BlockPos spawn = target.getSharedSpawnPos();
            pos = Vec3.atBottomCenterOf(target.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn));
        }
        player.teleportTo(target, pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());
        player.resetFallDistance();
    }

    private static void buildPlatform(ServerLevel level, BlockPos center) {
        int r = HQConfig.POCKET_PLATFORM_RADIUS.value();
        BlockState floor = Blocks.CALCITE.defaultBlockState();
        BlockState rim = Blocks.AMETHYST_BLOCK.defaultBlockState();
        for (int dx = -r; dx < r; dx++) {
            for (int dz = -r; dz < r; dz++) {
                boolean edge = dx == -r || dz == -r || dx == r - 1 || dz == r - 1;
                level.setBlockAndUpdate(center.offset(dx, 0, dz), edge ? rim : floor);
            }
        }
        // The way out: a Portal Door at the north edge, standing on the rim.
        level.setBlockAndUpdate(center.offset(0, 1, -r), HQBlocks.PORTAL_DOOR.value().defaultBlockState());
    }

    /**
     * Platform slot of every player, and whether it has been built.
     */
    public static class Index extends SavedData {
        private static final String NAME = "hexquantum_pocket_index";
        private static final SavedData.Factory<Index> FACTORY = new SavedData.Factory<>(Index::new, Index::load, null);

        private final Map<UUID, Integer> indices = new HashMap<>();
        private final Map<UUID, Boolean> built = new HashMap<>();
        // Slot 0 would sit at the origin, so start at 1.
        private int next = 1;

        public static Index get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
        }

        public int indexOf(UUID player) {
            Integer existing = indices.get(player);
            if (existing != null) {
                return existing;
            }
            int index = next++;
            indices.put(player, index);
            setDirty();
            return index;
        }

        /**
         * Mark the platform as built. Returns true the first time (i.e. when it still has to be built).
         */
        public boolean markBuilt(UUID player) {
            if (Boolean.TRUE.equals(built.get(player))) {
                return false;
            }
            built.put(player, true);
            setDirty();
            return true;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            CompoundTag players = new CompoundTag();
            indices.forEach((id, index) -> {
                CompoundTag p = new CompoundTag();
                p.putInt("index", index);
                p.putBoolean("built", Boolean.TRUE.equals(built.get(id)));
                players.put(id.toString(), p);
            });
            tag.put("players", players);
            tag.putInt("next", next);
            return tag;
        }

        private static Index load(CompoundTag tag, HolderLookup.Provider registries) {
            Index data = new Index();
            CompoundTag players = tag.getCompound("players");
            for (String key : players.getAllKeys()) {
                try {
                    UUID id = UUID.fromString(key);
                    CompoundTag p = players.getCompound(key);
                    data.indices.put(id, p.getInt("index"));
                    data.built.put(id, p.getBoolean("built"));
                } catch (IllegalArgumentException ignored) {
                    // not a player entry
                }
            }
            data.next = Math.max(1, tag.getInt("next"));
            return data;
        }
    }
}
