package io.github.timursl.hexquantum.entity;

import io.github.timursl.hexquantum.registry.HQEntities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

/**
 * The caster's personal Illusion: invulnerable, no collision, may be targeted by mobs but never harmed.
 */
public class IllusionEntity extends PlayerCopyEntity {
    public IllusionEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    public static IllusionEntity create(ServerPlayer owner) {
        IllusionEntity illusion = new IllusionEntity(HQEntities.type(HQEntities.ILLUSION), owner.serverLevel());
        illusion.setOwner(owner);
        return illusion;
    }

    // Deliberately not setInvulnerable(true): mobs refuse to target invulnerable entities.
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        ServerPlayer owner = getOwner();
        if (owner == null || owner.level() != this.level() || !IllusionManager.isCurrent(owner, this)) {
            this.discard();
            return;
        }
        if (this.tickCount % 10 == 0) {
            lureMobs(owner);
        }
    }
}
