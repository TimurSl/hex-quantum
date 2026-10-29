package io.github.timursl.hexquantum.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the one Illusion each player may have.
 */
public final class IllusionManager {
    private static final Map<UUID, UUID> ILLUSIONS = new ConcurrentHashMap<>();

    private IllusionManager() {
    }

    public static boolean isCurrent(ServerPlayer owner, IllusionEntity illusion) {
        return illusion.getUUID().equals(ILLUSIONS.get(owner.getUUID()));
    }

    @Nullable
    public static IllusionEntity find(ServerPlayer owner) {
        UUID id = ILLUSIONS.get(owner.getUUID());
        if (id == null) {
            return null;
        }
        for (ServerLevel level : owner.server.getAllLevels()) {
            Entity e = level.getEntity(id);
            if (e instanceof IllusionEntity illusion && illusion.isAlive()) {
                return illusion;
            }
        }
        ILLUSIONS.remove(owner.getUUID());
        return null;
    }

    /**
     * Replace the player's illusion with {@code illusion} (already positioned, not yet added to the world).
     */
    public static void summon(ServerPlayer owner, IllusionEntity illusion) {
        dispel(owner);
        ILLUSIONS.put(owner.getUUID(), illusion.getUUID());
        owner.serverLevel().addFreshEntity(illusion);
    }

    public static boolean dispel(ServerPlayer owner) {
        IllusionEntity current = find(owner);
        ILLUSIONS.remove(owner.getUUID());
        if (current != null) {
            current.discard();
            return true;
        }
        return false;
    }
}
