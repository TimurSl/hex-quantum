package io.github.timursl.hexquantum;

import io.github.timursl.hexquantum.casting.WaitScheduler;
import io.github.timursl.hexquantum.data.PlayerQuantumData;
import io.github.timursl.hexquantum.entity.IllusionManager;
import io.github.timursl.hexquantum.entity.MovementRecorder;
import io.github.timursl.hexquantum.item.QuantumPhialItem;
import io.github.timursl.hexquantum.link.DamageLinks;
import io.github.timursl.hexquantum.optics.Optics;
import io.github.timursl.hexquantum.phantom.PhantomMobs;
import io.github.timursl.hexquantum.platform.Services;
import io.github.timursl.hexquantum.shift.QuantumShift;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Collection;
import java.util.UUID;

/**
 * Loader-independent game event handlers. Each loader forwards its events here; the return values say whether the
 * loader should cancel the event.
 */
public final class HQEvents {
    private HQEvents() {
    }

    // ---- lifecycle / ticking ----

    public static void onServerTick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            MovementRecorder.record(player);
        }
        WaitScheduler.tick(server);
        QuantumShift.tick(server);
        Optics.tick(server);
    }

    public static void onServerStopped() {
        WaitScheduler.clear();
        QuantumShift.clear();
    }

    public static void onLivingTick(LivingEntity entity) {
        PhantomMobs.tick(entity);
    }

    public static void onPlayerLogout(ServerPlayer player) {
        IllusionManager.dispel(player);
        WaitScheduler.cancel(player.getUUID());
        QuantumShift.end(player);
        Optics.clear(player);
        MovementRecorder.reset(player);
    }

    public static void onPlayerChangedDimension(ServerPlayer player) {
        IllusionManager.dispel(player);
        MovementRecorder.reset(player);
    }

    /**
     * Keep bound Quantum Phials out of the death drops; one is handed back on respawn.
     */
    public static void onPlayerDrops(ServerPlayer player, Collection<ItemEntity> drops) {
        boolean kept = drops.removeIf(item -> QuantumPhialItem.owner(item.getItem()) != null
            && player.getUUID().equals(QuantumPhialItem.owner(item.getItem())));
        if (kept) {
            Services.PLATFORM.playerData(player).setRestorePhial(true);
        }
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        PlayerQuantumData data = Services.PLATFORM.playerData(player);
        QuantumShift.end(player);
        Optics.clear(player);
        if (data.restorePhial()) {
            data.setRestorePhial(false);
            UUID id = data.phialId();
            if (id != null) {
                ItemStack phial = QuantumPhialItem.create(player, id);
                if (!player.getInventory().add(phial)) {
                    player.drop(phial, false);
                }
            }
        }
    }

    // ---- combat ----

    /**
     * @return the damage the entity should take, or a negative value to cancel the damage completely
     */
    public static float onIncomingDamage(LivingEntity victim, DamageSource source, float amount) {
        if (QuantumShift.isShifting(victim) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return -1;
        }
        amount = PhantomMobs.scaleDamage(source, amount);
        if (victim.level().getServer() != null) {
            amount = DamageLinks.get(victim.level().getServer()).onDamage(victim, source, amount);
        }
        return amount;
    }

    public static void onLivingDeath(LivingEntity entity) {
        if (entity.level().getServer() != null) {
            DamageLinks.get(entity.level().getServer()).unlink(entity.getUUID());
        }
        PhantomMobs.forget(entity);
    }

    /**
     * Phantoms drop nothing.
     *
     * @return true if all drops (and experience) must be removed
     */
    public static boolean shouldSuppressDrops(LivingEntity entity) {
        return PhantomMobs.isAnyPhantom(entity);
    }

    /**
     * @return true to cancel a mob picking this target
     */
    public static boolean onChangeTarget(LivingEntity mob, LivingEntity newTarget) {
        return newTarget != null && QuantumShift.isShifting(newTarget);
    }

    /**
     * @return true to let the projectile pass through
     */
    public static boolean onProjectileImpact(Projectile projectile, HitResult hit) {
        return hit instanceof EntityHitResult entityHit && QuantumShift.isShifting(entityHit.getEntity());
    }

    /**
     * @return true to cancel the replacement explosion's original
     */
    public static boolean onExplosionStart(Level level, Explosion explosion) {
        return PhantomMobs.onExplosionStart(level, explosion);
    }

    // ---- interaction ----

    /**
     * @return true to cancel the attack
     */
    public static boolean onAttackEntity(Player player, Entity target) {
        if (QuantumShift.isShifting(player)) {
            return true;
        }
        if (!player.level().isClientSide()) {
            PhantomMobs.onInteract(target, player);
        }
        return false;
    }

    /**
     * Right-click on an entity. @return true to cancel
     */
    public static boolean onInteractEntity(Player player, Entity target) {
        if (QuantumShift.isShifting(player)) {
            return true;
        }
        if (!player.level().isClientSide()) {
            PhantomMobs.onInteract(target, player);
        }
        return false;
    }

    /**
     * Clicking, breaking or placing blocks. @return true to cancel
     */
    public static boolean onBlockInteraction(Player player) {
        return QuantumShift.isShifting(player);
    }

    /**
     * Using an item in the air. Casting items stay usable while shifted. @return true to cancel
     */
    public static boolean onUseItem(Player player, ItemStack stack) {
        return QuantumShift.isShifting(player) && !QuantumShift.isCastingItem(stack);
    }
}
