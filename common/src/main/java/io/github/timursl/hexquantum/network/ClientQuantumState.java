package io.github.timursl.hexquantum.network;

import net.minecraft.world.phys.Vec3;

/**
 * Client-side state set by the payloads. Plain fields (no client classes) so common code and mixins can read it on
 * either side safely.
 */
public final class ClientQuantumState {
    public static volatile int quantumShiftTicks;
    public static volatile int mirrorTicks;
    public static volatile int shiftedTicks;
    public static volatile Vec3 shiftedOffset = Vec3.ZERO;

    private ClientQuantumState() {
    }

    public static void handle(QuantumShiftPayload payload) {
        quantumShiftTicks = payload.ticks();
    }

    public static void handle(MirrorOpticsPayload payload) {
        mirrorTicks = payload.ticks();
    }

    public static void handle(ShiftedOpticsPayload payload) {
        shiftedOffset = new Vec3(payload.x(), payload.y(), payload.z());
        shiftedTicks = payload.ticks();
    }

    public static boolean mirrorActive() {
        return mirrorTicks > 0;
    }

    public static boolean shiftedActive() {
        return shiftedTicks > 0;
    }

    /**
     * Called every client tick.
     */
    public static void tick() {
        if (quantumShiftTicks > 0) {
            quantumShiftTicks--;
        }
        if (mirrorTicks > 0) {
            mirrorTicks--;
        }
        if (shiftedTicks > 0) {
            shiftedTicks--;
        }
    }

    public static void reset() {
        quantumShiftTicks = 0;
        mirrorTicks = 0;
        shiftedTicks = 0;
        shiftedOffset = Vec3.ZERO;
    }
}
