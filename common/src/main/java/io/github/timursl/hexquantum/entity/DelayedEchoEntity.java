package io.github.timursl.hexquantum.entity;

import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.casting.HexRunner;
import io.github.timursl.hexquantum.registry.HQEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Intangible copy of the caster that waits, casts its hex once from its own position (media charged to the
 * caster) and fades.
 */
public class DelayedEchoEntity extends PlayerCopyEntity {
    private List<Iota> hex = new ArrayList<>();
    private int delay;

    public DelayedEchoEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static DelayedEchoEntity create(ServerPlayer owner, List<Iota> hex, int delayTicks) {
        DelayedEchoEntity echo = new DelayedEchoEntity(HQEntities.type(HQEntities.DELAYED_ECHO), owner.serverLevel());
        echo.setOwner(owner);
        echo.setCustomNameVisible(false);
        echo.hex = new ArrayList<>(hex);
        echo.delay = delayTicks;
        return echo;
    }

    @Override
    public boolean isGhostly() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !isInvulnerableTo(source) && super.hurt(source, amount);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.REVERSE_PORTAL, this.getRandomX(0.6), this.getRandomY(),
                    this.getRandomZ(0.6), 0, 0.02, 0);
            }
            return;
        }
        ServerPlayer owner = getOwner();
        if (owner == null) {
            this.discard();
            return;
        }
        if (--delay <= 0) {
            HexRunner.run(owner, (ServerLevel) this.level(), this.getEyePosition(), hex, List.of(new EntityIota(this)));
            this.discard();
        }
    }
}
