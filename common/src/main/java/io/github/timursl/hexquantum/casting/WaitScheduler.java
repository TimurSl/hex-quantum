package io.github.timursl.hexquantum.casting;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.ContinuationIota;
import io.github.timursl.hexquantum.HexQuantum;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Hexes paused by the Rune of Waiting. A paused hex keeps its original casting environment, stack and
 * continuation, and is resumed by evaluating the continuation as a jump iota in a fresh VM.
 * <p>
 * Nothing here is saved: a server restart or the caster logging out cancels every paused hex.
 */
public final class WaitScheduler {
    private record Pending(CastingEnvironment env, CastingImage image, SpellContinuation continuation,
                           long resumeAt, @Nullable UUID caster) {
    }

    private static final List<Pending> PENDING = new ArrayList<>();

    private WaitScheduler() {
    }

    public static synchronized int pendingFor(UUID caster) {
        int n = 0;
        for (Pending p : PENDING) {
            if (caster.equals(p.caster)) {
                n++;
            }
        }
        return n;
    }

    public static synchronized void schedule(CastingEnvironment env, CastingImage image, SpellContinuation continuation,
        int delayTicks) {
        LivingEntity caster = env.getCastingEntity();
        long now = env.getWorld().getServer().getTickCount();
        PENDING.add(new Pending(env, image, continuation, now + delayTicks, caster == null ? null : caster.getUUID()));
    }

    public static synchronized void cancel(UUID caster) {
        PENDING.removeIf(p -> caster.equals(p.caster));
    }

    public static void tick(MinecraftServer server) {
        List<Pending> due = new ArrayList<>();
        synchronized (WaitScheduler.class) {
            long now = server.getTickCount();
            Iterator<Pending> it = PENDING.iterator();
            while (it.hasNext()) {
                Pending p = it.next();
                if (p.resumeAt <= now) {
                    it.remove();
                    due.add(p);
                }
            }
        }
        for (Pending p : due) {
            resume(p);
        }
    }

    public static synchronized void clear() {
        PENDING.clear();
    }

    private static void resume(Pending p) {
        if (p.caster != null) {
            LivingEntity caster = p.env.getCastingEntity();
            if (!(caster instanceof ServerPlayer player) || player.hasDisconnected() || !player.isAlive()) {
                return;
            }
        }
        try {
            CastingVM vm = new CastingVM(p.image, p.env);
            vm.queueExecuteAndWrapIota(new ContinuationIota(p.continuation), p.env.getWorld());
        } catch (RuntimeException e) {
            HexQuantum.LOGGER.error("Failed to resume a waiting hex", e);
        }
    }
}
