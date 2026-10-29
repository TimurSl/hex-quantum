package io.github.timursl.hexquantum.entity;

import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.registry.HQEntities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

/**
 * A stationary 1 HP decoy of the caster. Unlike the Illusion it can be killed (in one hit).
 */
public class PersonalEchoEntity extends PlayerCopyEntity {
    private int lifetime;

    public PersonalEchoEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    public static PersonalEchoEntity create(ServerPlayer owner) {
        PersonalEchoEntity echo = new PersonalEchoEntity(HQEntities.type(HQEntities.PERSONAL_ECHO), owner.serverLevel());
        echo.setOwner(owner);
        echo.lifetime = (int) Math.round(HQConfig.PERSONAL_ECHO_LIFETIME.value() * 20);
        return echo;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        ServerPlayer owner = getOwner();
        if (owner == null || owner.level() != this.level() || --lifetime <= 0) {
            this.discard();
            return;
        }
        if (this.tickCount % 10 == 0) {
            lureMobs(owner);
        }
    }
}
