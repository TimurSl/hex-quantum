package io.github.timursl.hexquantum.casting;

import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.utils.TreeList;
import io.github.timursl.hexquantum.HexQuantum;
import io.github.timursl.hexquantum.casting.env.RemoteCastEnv;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Runs a stored hex as a player from a remote position.
 */
public final class HexRunner {
    private HexRunner() {
    }

    @Nullable
    public static ServerPlayer onlineOwner(@Nullable MinecraftServer server, @Nullable UUID owner) {
        if (server == null || owner == null) {
            return null;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(owner);
        return player == null || player.hasDisconnected() ? null : player;
    }

    /**
     * Run {@code hex} as {@code caster} with ambit around {@code center}, starting with {@code initialStack}.
     * Does nothing (and returns false) if the caster is not in {@code level}.
     */
    public static boolean run(ServerPlayer caster, ServerLevel level, Vec3 center, List<Iota> hex, List<Iota> initialStack) {
        if (caster.serverLevel() != level || hex.isEmpty()) {
            return false;
        }
        try {
            RemoteCastEnv env = new RemoteCastEnv(caster, center);
            CastingImage image = new CastingImage(TreeList.from(initialStack), 0, TreeList.empty(), false, false, 0L,
                Map.of());
            CastingVM vm = new CastingVM(image, env);
            vm.queueExecuteAndWrapIotas(hex, level);
            return true;
        } catch (RuntimeException e) {
            HexQuantum.LOGGER.error("Failed to run a remote hex for {}", caster.getGameProfile().getName(), e);
            return false;
        }
    }

    /**
     * Charge {@code cost} media to {@code caster} as a remote cast at {@code center}. Returns whether it was paid.
     */
    public static boolean charge(ServerPlayer caster, Vec3 center, long cost) {
        if (cost <= 0) {
            return true;
        }
        RemoteCastEnv env = new RemoteCastEnv(caster, center);
        env.triggerCreateEvent(new CastingImage());
        if (env.extractMedia(cost, true) > 0) {
            return false;
        }
        env.extractMedia(cost, false);
        return true;
    }
}
