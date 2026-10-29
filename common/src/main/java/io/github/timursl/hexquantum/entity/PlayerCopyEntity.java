package io.github.timursl.hexquantum.entity;

import io.github.timursl.hexquantum.config.HQConfig;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * A frozen copy of a player (rendered with the owner's skin): base of the Illusion and the Echoes.
 * <p>
 * Copies have no AI of their own and are not saved with the chunk; they only live while their owner is around.
 */
public abstract class PlayerCopyEntity extends Mob {
    private static final EntityDataAccessor<Optional<UUID>> OWNER =
        SynchedEntityData.defineId(PlayerCopyEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    protected PlayerCopyEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes(double maxHealth) {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, maxHealth)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, Optional.empty());
    }

    @Nullable
    public UUID getOwnerId() {
        return this.entityData.get(OWNER).orElse(null);
    }

    public void setOwner(ServerPlayer owner) {
        this.entityData.set(OWNER, Optional.of(owner.getUUID()));
        this.setCustomName(owner.getName());
        this.setCustomNameVisible(true);
        this.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), owner.getXRot());
        this.setYHeadRot(owner.getYHeadRot());
        this.yBodyRot = owner.yBodyRot;
    }

    @Nullable
    public ServerPlayer getOwner() {
        UUID id = getOwnerId();
        if (id == null || this.level().getServer() == null) {
            return null;
        }
        return this.level().getServer().getPlayerList().getPlayer(id);
    }

    /**
     * Whether this copy is rendered see-through.
     */
    public boolean isGhostly() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public void push(Entity entity) {
    }

    /**
     * Pull hostile mobs that are chasing the owner onto this copy.
     */
    protected void lureMobs(ServerPlayer owner) {
        double radius = HQConfig.ILLUSION_LURE_RADIUS.value();
        if (radius <= 0) {
            return;
        }
        for (Mob mob : this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(radius),
            m -> m instanceof Enemy && m.getTarget() == owner)) {
            mob.setTarget(this);
        }
    }
}
