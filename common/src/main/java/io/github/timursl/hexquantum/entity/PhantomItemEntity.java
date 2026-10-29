package io.github.timursl.hexquantum.entity;

import io.github.timursl.hexquantum.phantom.PhantomTriggers;
import io.github.timursl.hexquantum.registry.HQEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A phantom item: looks like a dropped item but is its own entity type (so hoppers and magnets ignore it) and
 * vanishes when a player walks over it to pick it up. An empowered one runs its creator's hex at that moment.
 */
public class PhantomItemEntity extends Entity {
    private static final EntityDataAccessor<ItemStack> ITEM =
        SynchedEntityData.defineId(PhantomItemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> EMPOWERED =
        SynchedEntityData.defineId(PhantomItemEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private UUID owner;
    @Nullable
    private Tag hex;
    /**
     * Random spin offset, like vanilla item entities.
     */
    public final float bobOffset;

    public PhantomItemEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.bobOffset = this.random.nextFloat() * (float) Math.PI * 2.0f;
    }

    public static PhantomItemEntity create(ServerLevel level, Vec3 pos, ItemStack look, UUID owner, @Nullable Tag hex) {
        PhantomItemEntity entity = new PhantomItemEntity(HQEntities.type(HQEntities.PHANTOM_ITEM), level);
        entity.setPos(pos);
        entity.setItem(look.copy());
        entity.owner = owner;
        entity.hex = hex;
        entity.entityData.set(EMPOWERED, hex != null);
        entity.setDeltaMovement(level.random.nextDouble() * 0.2 - 0.1, 0.2, level.random.nextDouble() * 0.2 - 0.1);
        return entity;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ITEM, ItemStack.EMPTY);
        builder.define(EMPOWERED, false);
    }

    public ItemStack getItem() {
        return this.entityData.get(ITEM);
    }

    public void setItem(ItemStack stack) {
        this.entityData.set(ITEM, stack);
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        float friction = this.onGround() ? 0.6f * 0.98f : 0.98f;
        this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.98, friction));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, -0.5, 1.0));
        }
        if (this.level().isClientSide() && this.entityData.get(EMPOWERED) && this.random.nextInt(6) == 0) {
            this.level().addParticle(ParticleTypes.REVERSE_PORTAL, this.getRandomX(0.5), this.getY() + 0.25,
                this.getRandomZ(0.5), 0, 0.02, 0);
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (this.level().isClientSide() || this.isRemoved() || player.isSpectator()) {
            return;
        }
        this.discard();
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE,
            SoundSource.PLAYERS, 0.4f, 1.6f);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.2, this.getZ(), 6, 0.1, 0.1, 0.1, 0.01);
            if (hex != null) {
                PhantomTriggers.fire(server, this.position(), owner, hex, player);
            }
        }
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ItemStack.parse(this.registryAccess(), tag.getCompound("item")).ifPresent(this::setItem);
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        hex = tag.contains("hex") ? tag.get("hex") : null;
        this.entityData.set(EMPOWERED, hex != null);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (!getItem().isEmpty()) {
            tag.put("item", getItem().save(this.registryAccess()));
        }
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        if (hex != null) {
            tag.put("hex", hex.copy());
        }
    }
}
