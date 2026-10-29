package io.github.timursl.hexquantum.optics;

import io.github.timursl.hexquantum.HexQuantum;
import io.github.timursl.hexquantum.network.MirrorOpticsPayload;
import io.github.timursl.hexquantum.network.ShiftedOpticsPayload;
import io.github.timursl.hexquantum.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server side of Mirror Optics and Shifted Optics. The visuals are entirely client-side; for Shifted Optics the
 * server also stretches the player's interaction reach by the offset length, because block/entity interactions are
 * validated against the player's real eye position.
 */
public final class Optics {
    private static final ResourceLocation REACH_ID = HexQuantum.id("shifted_optics_reach");
    private static final Map<UUID, Integer> SHIFTED = new HashMap<>();

    private Optics() {
    }

    public static void mirror(ServerPlayer target, int ticks) {
        Services.PLATFORM.sendToPlayer(target, new MirrorOpticsPayload(ticks));
    }

    public static void shift(ServerPlayer target, Vec3 offset, int ticks) {
        Services.PLATFORM.sendToPlayer(target, new ShiftedOpticsPayload(offset.x, offset.y, offset.z, ticks));
        setReach(target, offset.length());
        synchronized (SHIFTED) {
            SHIFTED.put(target.getUUID(), ticks);
        }
    }

    public static void tick(MinecraftServer server) {
        synchronized (SHIFTED) {
            var it = SHIFTED.entrySet().iterator();
            while (it.hasNext()) {
                var e = it.next();
                e.setValue(e.getValue() - 1);
                if (e.getValue() <= 0) {
                    it.remove();
                    ServerPlayer player = server.getPlayerList().getPlayer(e.getKey());
                    if (player != null) {
                        setReach(player, 0);
                    }
                }
            }
        }
    }

    /**
     * Drop every optics effect of this player (logout, death).
     */
    public static void clear(ServerPlayer player) {
        synchronized (SHIFTED) {
            SHIFTED.remove(player.getUUID());
        }
        setReach(player, 0);
    }

    private static void setReach(ServerPlayer player, double extra) {
        for (var attribute : java.util.List.of(Attributes.BLOCK_INTERACTION_RANGE, Attributes.ENTITY_INTERACTION_RANGE)) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            instance.removeModifier(REACH_ID);
            if (extra > 0) {
                instance.addTransientModifier(new AttributeModifier(REACH_ID, extra, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }
}
