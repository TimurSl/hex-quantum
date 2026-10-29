package io.github.timursl.hexquantum.entity;

import io.github.timursl.hexquantum.config.HQConfig;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps a short rolling history of every online player's position, for Cyclic Echo.
 */
public final class MovementRecorder {
    public record Frame(double x, double y, double z, float yRot, float xRot) {
    }

    private static final Map<UUID, Deque<Frame>> HISTORY = new ConcurrentHashMap<>();

    private MovementRecorder() {
    }

    public static void record(ServerPlayer player) {
        int max = (int) Math.round(HQConfig.CYCLIC_ECHO_RECORD_SECONDS.value() * 20);
        Deque<Frame> frames = HISTORY.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>());
        frames.addLast(new Frame(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()));
        while (frames.size() > max) {
            frames.removeFirst();
        }
    }

    /**
     * Clear the history when the player teleports between dimensions, so a route never spans two worlds.
     */
    public static void reset(ServerPlayer player) {
        HISTORY.remove(player.getUUID());
    }

    public static List<Frame> snapshot(ServerPlayer player) {
        Deque<Frame> frames = HISTORY.get(player.getUUID());
        return frames == null ? List.of() : new ArrayList<>(frames);
    }
}
