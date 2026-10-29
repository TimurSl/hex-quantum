package io.github.timursl.hexquantum.phantom;

import io.github.timursl.hexquantum.HexQuantum;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.registry.HQEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Phantom mobs are real mobs of the copied type with normal AI, marked with a scoreboard tag:
 * <ul>
 *     <li>regular: see-through, {@code phantomHealthMultiplier} health, {@code phantomDamageMultiplier} damage,
 *     creepers explode weaker and never break blocks;</li>
 *     <li>empowered: opaque, full stats, only special particles give them away; interacting with them runs the
 *     creator's hex.</li>
 * </ul>
 * Neither kind drops loot or experience.
 */
public final class PhantomMobs {
    public static final String TAG = "hexquantum.phantom";
    public static final String EMPOWERED_TAG = "hexquantum.phantom.empowered";

    private static final Map<UUID, Long> LAST_TRIGGER = new HashMap<>();
    private static boolean replacingExplosion;

    private PhantomMobs() {
    }

    public static boolean isPhantom(Entity e) {
        return e.getTags().contains(TAG);
    }

    public static boolean isEmpowered(Entity e) {
        return e.getTags().contains(EMPOWERED_TAG);
    }

    public static boolean isAnyPhantom(Entity e) {
        return isPhantom(e) || isEmpowered(e);
    }

    /**
     * Whether a mob can be copied into a phantom.
     */
    public static boolean canCopy(Entity template) {
        return template instanceof Mob
            && !(template instanceof Player)
            && !isAnyPhantom(template)
            && !template.getType().getDescriptionId().startsWith("entity." + HexQuantum.MOD_ID)
            && template.canChangeDimensions(template.level(), template.level())
            && template.getType().canSerialize();
    }

    /**
     * Create (but do not add) a phantom of {@code template}'s type at {@code pos}.
     */
    @Nullable
    public static Mob create(ServerLevel level, Entity template, Vec3 pos, boolean empowered) {
        Entity created = template.getType().create(level);
        if (!(created instanceof Mob mob)) {
            return null;
        }
        mob.moveTo(pos.x, pos.y, pos.z, level.random.nextFloat() * 360f, 0f);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(pos)), MobSpawnType.MOB_SUMMONED, null);
        if (empowered) {
            mob.addTag(EMPOWERED_TAG);
            mob.addEffect(new MobEffectInstance(HQEffects.EMPOWERED_PHANTASMAL, MobEffectInstance.INFINITE_DURATION, 0, true, false));
        } else {
            mob.addTag(TAG);
            mob.addEffect(new MobEffectInstance(HQEffects.PHANTASMAL, MobEffectInstance.INFINITE_DURATION, 0, true, false));
            AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.addOrReplacePermanentModifier(new AttributeModifier(HexQuantum.id("phantom_health"),
                    HQConfig.PHANTOM_HEALTH_MULTIPLIER.value() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                mob.setHealth(mob.getMaxHealth());
            }
        }
        mob.setPersistenceRequired();
        return mob;
    }

    /**
     * Every tick for every living entity: keep the marker effects, and give empowered phantoms away with particles.
     */
    public static void tick(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        if (isPhantom(entity) && !entity.hasEffect(HQEffects.PHANTASMAL)) {
            entity.addEffect(new MobEffectInstance(HQEffects.PHANTASMAL, MobEffectInstance.INFINITE_DURATION, 0, true, false));
        } else if (isEmpowered(entity)) {
            if (!entity.hasEffect(HQEffects.EMPOWERED_PHANTASMAL)) {
                entity.addEffect(new MobEffectInstance(HQEffects.EMPOWERED_PHANTASMAL, MobEffectInstance.INFINITE_DURATION, 0, true, false));
            }
            if (entity.tickCount % 8 == 0 && entity.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5,
                    entity.getZ(), 2, entity.getBbWidth() * 0.4, entity.getBbHeight() * 0.3, entity.getBbWidth() * 0.4, 0.0);
            }
        }
    }

    /**
     * Damage dealt by a regular phantom (directly or with its projectiles) is scaled down.
     */
    public static float scaleDamage(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker != null && isPhantom(attacker)) {
            return (float) (amount * HQConfig.PHANTOM_DAMAGE_MULTIPLIER.value());
        }
        return amount;
    }

    /**
     * Replace a regular phantom creeper's explosion with a weaker one that breaks nothing.
     *
     * @return true if the original explosion must be cancelled
     */
    public static boolean onExplosionStart(Level level, Explosion explosion) {
        if (replacingExplosion || level.isClientSide()) {
            return false;
        }
        Entity source = explosion.getDirectSourceEntity();
        if (!(source instanceof Creeper creeper) || !isPhantom(creeper)) {
            return false;
        }
        replacingExplosion = true;
        try {
            level.explode(creeper, creeper.getX(), creeper.getY(), creeper.getZ(),
                (float) HQConfig.PHANTOM_CREEPER_EXPLOSION_POWER.value(), Level.ExplosionInteraction.NONE);
        } finally {
            replacingExplosion = false;
        }
        return true;
    }

    /**
     * Someone hit or used an empowered phantom: run its creator's hex (rate limited).
     */
    public static void onInteract(Entity target, Entity interactor) {
        if (!isEmpowered(target) || !(target.level() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        Long last = LAST_TRIGGER.get(target.getUUID());
        if (last != null && now - last < HQConfig.EMPOWERED_TRIGGER_COOLDOWN.value()) {
            return;
        }
        LAST_TRIGGER.put(target.getUUID(), now);
        PhantomRegistry.Entry entry = PhantomRegistry.get(level.getServer()).get(target.getUUID());
        if (entry != null) {
            Tag hex = entry.hex();
            PhantomTriggers.fire(level, target.position(), entry.owner(), hex, interactor);
        }
    }

    public static void forget(Entity entity) {
        LAST_TRIGGER.remove(entity.getUUID());
        if (isEmpowered(entity) && entity.level() instanceof ServerLevel level) {
            PhantomRegistry.get(level.getServer()).remove(entity.getUUID());
        }
    }
}
