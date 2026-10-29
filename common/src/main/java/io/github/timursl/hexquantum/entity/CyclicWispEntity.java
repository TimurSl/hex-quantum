package io.github.timursl.hexquantum.entity;

import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.casting.HexRunner;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.registry.HQEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A stationary wisp that runs its hex every {@code interval} ticks, {@code remaining} times, as its owner and from
 * its own position (the wisp itself is pushed onto the starting stack). Each run costs the owner
 * {@code cyclicWispCostPerCycle} on top of the hex's own costs; it fizzles when the owner cannot pay or logs out.
 */
public class CyclicWispEntity extends Entity {
    private UUID owner;
    private List<Iota> hex = new ArrayList<>();
    private int interval = 20;
    private int remaining;
    private int timer;

    public CyclicWispEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static CyclicWispEntity create(ServerPlayer owner, Vec3 pos, List<Iota> hex, int intervalTicks, int count) {
        CyclicWispEntity wisp = new CyclicWispEntity(HQEntities.type(HQEntities.CYCLIC_WISP), owner.serverLevel());
        wisp.owner = owner.getUUID();
        wisp.hex = new ArrayList<>(hex);
        wisp.interval = intervalTicks;
        wisp.timer = intervalTicks;
        wisp.remaining = count;
        wisp.setPos(pos);
        return wisp;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.END_ROD, this.getX() + (random.nextDouble() - 0.5) * 0.3,
                this.getY() + (random.nextDouble() - 0.5) * 0.3, this.getZ() + (random.nextDouble() - 0.5) * 0.3,
                0, 0.01, 0);
            return;
        }
        ServerPlayer player = HexRunner.onlineOwner(this.level().getServer(), owner);
        if (player == null || player.level() != this.level() || remaining <= 0) {
            this.discard();
            return;
        }
        if (--timer > 0) {
            return;
        }
        timer = interval;
        if (!HexRunner.charge(player, this.position(), HQConfig.CYCLIC_WISP_COST_PER_CYCLE.media())) {
            this.discard();
            return;
        }
        HexRunner.run(player, (ServerLevel) this.level(), this.position(), hex, List.of(new EntityIota(this)));
        if (--remaining <= 0) {
            this.discard();
        }
    }
}
