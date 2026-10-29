package io.github.timursl.hexquantum.mixin.client;

import io.github.timursl.hexquantum.network.ClientQuantumState;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Shifted Optics: render the world from a displaced viewpoint.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    public abstract Vec3 getPosition();

    @Shadow
    protected abstract void setPosition(Vec3 pos);

    @Inject(method = "setup", at = @At("TAIL"), require = 0)
    private void hexquantum$shiftedOptics(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse,
        float partialTick, CallbackInfo ci) {
        if (ClientQuantumState.shiftedActive()) {
            this.setPosition(this.getPosition().add(ClientQuantumState.shiftedOffset));
        }
    }
}
