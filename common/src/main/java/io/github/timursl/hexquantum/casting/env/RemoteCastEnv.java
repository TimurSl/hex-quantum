package io.github.timursl.hexquantum.casting.env;

import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

/**
 * Casting environment for hexes that run "as" a player but from somewhere else: echoes, wisps and empowered
 * phantoms. Media comes from the player (inventory, Quantum Phial, overcasting if unlocked); the ambit is a sphere of
 * the player's ambit radius around {@link #center}.
 */
public class RemoteCastEnv extends PlayerBasedCastEnv {
    private final Vec3 center;

    public RemoteCastEnv(ServerPlayer caster, Vec3 center) {
        super(caster, InteractionHand.MAIN_HAND);
        this.center = center;
    }

    public Vec3 getCenter() {
        return center;
    }

    @Override
    public boolean isVecInRangeEnvironment(Vec3 vec) {
        double r = this.getAmbitRadius();
        return vec.distanceToSqr(center) <= r * r + 0.00000000001;
    }

    @Override
    public Vec3 mishapSprayPos() {
        return center;
    }

    @Override
    protected long extractMediaEnvironment(long cost, boolean simulate) {
        if (this.caster.isCreative()) {
            return 0;
        }
        return this.extractMediaFromInventory(cost, this.canOvercast(), simulate);
    }

    @Override
    public InteractionHand getCastingHand() {
        return InteractionHand.MAIN_HAND;
    }

    @Override
    public FrozenPigment getPigment() {
        return IXplatAbstractions.INSTANCE.getPigment(this.caster);
    }
}
