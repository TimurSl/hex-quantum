package io.github.timursl.hexquantum.shift;

import io.github.timursl.hexquantum.HexQuantum;
import io.github.timursl.hexquantum.network.ClientQuantumState;
import io.github.timursl.hexquantum.network.QuantumShiftPayload;
import io.github.timursl.hexquantum.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Quantum Shift: the caster walks through walls (the floor still holds unless they crouch), is ignored by mobs,
 * cannot be hurt or hit by projectiles, and cannot attack or touch the world, but can still cast.
 * <p>
 * Movement is client-authoritative for players, so the state is mirrored to the client and the collision change is
 * applied on both sides (see the Entity and LocalPlayer mixins).
 */
public final class QuantumShift {
    private static final Map<UUID, Integer> ACTIVE = new HashMap<>();
    private static final int PUSH_OUT_RADIUS = 8;

    private QuantumShift() {
    }

    public static boolean isShifting(Entity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        if (player.level().isClientSide()) {
            return player.isLocalPlayer() && ClientQuantumState.quantumShiftTicks > 0;
        }
        synchronized (ACTIVE) {
            return ACTIVE.containsKey(player.getUUID());
        }
    }

    public static void start(ServerPlayer player, int ticks) {
        synchronized (ACTIVE) {
            ACTIVE.put(player.getUUID(), ticks);
        }
        Services.PLATFORM.sendToPlayer(player, new QuantumShiftPayload(ticks));
        // Whatever was already hunting the caster forgets them.
        for (Mob mob : player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(64),
            m -> m.getTarget() == player)) {
            mob.setTarget(null);
        }
    }

    /**
     * End the shift early (no refund) or because it ran out.
     */
    public static void end(ServerPlayer player) {
        Integer removed;
        synchronized (ACTIVE) {
            removed = ACTIVE.remove(player.getUUID());
        }
        if (removed == null) {
            return;
        }
        Services.PLATFORM.sendToPlayer(player, new QuantumShiftPayload(0));
        pushOut(player);
    }

    public static void tick(MinecraftServer server) {
        Map<UUID, Integer> snapshot;
        synchronized (ACTIVE) {
            if (ACTIVE.isEmpty()) {
                return;
            }
            Iterator<Map.Entry<UUID, Integer>> it = ACTIVE.entrySet().iterator();
            snapshot = new HashMap<>();
            while (it.hasNext()) {
                Map.Entry<UUID, Integer> e = it.next();
                e.setValue(e.getValue() - 1);
                if (e.getValue() <= 0) {
                    snapshot.put(e.getKey(), 0);
                }
            }
        }
        for (UUID id : snapshot.keySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                end(player);
            } else {
                synchronized (ACTIVE) {
                    ACTIVE.remove(id);
                }
            }
        }
    }

    public static void clear() {
        synchronized (ACTIVE) {
            ACTIVE.clear();
        }
    }

    /**
     * Items that are allowed to be used while shifted: anything that casts (Hex Casting and HexQuantum items).
     */
    public static boolean isCastingItem(ItemStack stack) {
        String ns = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
        return ns.equals("hexcasting") || ns.equals(HexQuantum.MOD_ID);
    }

    /**
     * If the player ended up inside blocks, move them to the nearest spot where they fit.
     */
    public static void pushOut(ServerPlayer player) {
        var level = player.serverLevel();
        AABB box = player.getBoundingBox();
        if (level.noCollision(player, box)) {
            return;
        }
        Vec3 base = Vec3.atBottomCenterOf(player.blockPosition());
        Vec3 best = null;
        double bestDist = Double.MAX_VALUE;
        for (int r = 0; r <= PUSH_OUT_RADIUS && best == null; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) != r) {
                            continue;
                        }
                        Vec3 candidate = base.add(dx, dy, dz);
                        Vec3 delta = candidate.subtract(player.position());
                        if (!level.noCollision(player, box.move(delta))) {
                            continue;
                        }
                        double dist = delta.lengthSqr();
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = candidate;
                        }
                    }
                }
            }
        }
        if (best == null) {
            // Nowhere nearby: go straight up to the first free spot.
            Vec3 up = base;
            while (up.y < level.getMaxBuildHeight() && !level.noCollision(player, box.move(up.subtract(player.position())))) {
                up = up.add(0, 1, 0);
            }
            best = up;
        }
        player.teleportTo(best.x, best.y, best.z);
        player.resetFallDistance();
    }
}
