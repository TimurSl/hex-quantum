package io.github.timursl.hexquantum.entity;

import io.github.timursl.hexquantum.registry.HQEntities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
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
        mirror(owner);
        if (this.tickCount % 10 == 0) {
            lureMobs(owner);
        }
    }

    /**
     * Copy everything visible about the owner: gear, look direction, pose and arm swings. Position is left to
     * the illusion's own physics.
     */
    private void mirror(ServerPlayer owner) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = owner.getItemBySlot(slot);
            if (!ItemStack.matches(stack, this.getItemBySlot(slot))) {
                this.setItemSlot(slot, stack.copy());
            }
        }
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
        this.setYHeadRot(owner.getYHeadRot());
        this.yHeadRotO = owner.yHeadRotO;
        this.yBodyRot = owner.yBodyRot;
        this.yBodyRotO = owner.yBodyRotO;
        this.setPose(owner.getPose());
        this.setShiftKeyDown(owner.isShiftKeyDown());
        this.setSprinting(owner.isSprinting());
        this.setSwimming(owner.isSwimming());
        this.setLeftHanded(owner.getMainArm() == HumanoidArm.LEFT);
        if (owner.swinging && !this.swinging) {
            this.swing(owner.swingingArm);
        }
    }
}
