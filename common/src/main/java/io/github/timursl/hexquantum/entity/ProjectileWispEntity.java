package io.github.timursl.hexquantum.entity;

import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import io.github.timursl.hexquantum.casting.HexRunner;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.registry.HQEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * A wisp that flies in a straight line and runs its hex on impact, as its owner, from the impact point. The hit
 * entity (or the hit block's centre) is pushed onto the starting stack.
 */
public class ProjectileWispEntity extends ThrowableProjectile {
    private List<Iota> hex = new ArrayList<>();
    private int life;

    public ProjectileWispEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public static ProjectileWispEntity create(ServerPlayer owner, List<Iota> hex, Vec3 velocity) {
        ProjectileWispEntity wisp = new ProjectileWispEntity(HQEntities.type(HQEntities.PROJECTILE_WISP), owner.serverLevel());
        wisp.setOwner(owner);
        wisp.setPos(owner.getEyePosition().add(owner.getLookAngle().scale(0.5)));
        wisp.setDeltaMovement(velocity);
        wisp.hex = new ArrayList<>(hex);
        wisp.life = (int) Math.round(HQConfig.PROJECTILE_WISP_LIFETIME.value() * 20);
        return wisp;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            return;
        }
        if (--life <= 0) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        fire(result.getLocation(), new EntityIota(result.getEntity()));
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        fire(result.getLocation(), new Vec3Iota(Vec3.atCenterOf(result.getBlockPos())));
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    private void fire(Vec3 at, Iota target) {
        if (this.level().isClientSide() || this.isRemoved()) {
            return;
        }
        if (this.getOwner() instanceof ServerPlayer owner && !owner.hasDisconnected()) {
            HexRunner.run(owner, (ServerLevel) this.level(), at, hex, List.of(target));
        }
    }
}
