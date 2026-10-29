package io.github.timursl.hexquantum.entity;

import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.registry.HQEntities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Replays the last few seconds of the caster's movement in a loop, jumping back to the start at the end. 1 HP.
 */
public class CyclicEchoEntity extends PlayerCopyEntity {
    private List<MovementRecorder.Frame> route = List.of();
    private int index;
    private int lifetime;

    public CyclicEchoEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static CyclicEchoEntity create(ServerPlayer owner, List<MovementRecorder.Frame> route) {
        CyclicEchoEntity echo = new CyclicEchoEntity(HQEntities.type(HQEntities.CYCLIC_ECHO), owner.serverLevel());
        echo.setOwner(owner);
        echo.route = List.copyOf(route);
        echo.lifetime = (int) Math.round(HQConfig.CYCLIC_ECHO_LIFETIME.value() * 20);
        if (!echo.route.isEmpty()) {
            MovementRecorder.Frame first = echo.route.get(0);
            echo.moveTo(first.x(), first.y(), first.z(), first.yRot(), first.xRot());
        }
        return echo;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        ServerPlayer owner = getOwner();
        if (owner == null || owner.level() != this.level() || --lifetime <= 0 || route.isEmpty()) {
            this.discard();
            return;
        }
        MovementRecorder.Frame frame = route.get(index);
        index = (index + 1) % route.size();
        this.setPos(frame.x(), frame.y(), frame.z());
        this.setYRot(frame.yRot());
        this.setXRot(frame.xRot());
        this.setYHeadRot(frame.yRot());
        this.yBodyRot = frame.yRot();
        if (this.tickCount % 10 == 0) {
            lureMobs(owner);
        }
    }
}
