package io.github.timursl.hexquantum.mixin.client;

import io.github.timursl.hexquantum.network.ClientQuantumState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mirror Optics: the mirrored world shows what is below you overhead, so terrain outside the normal view cone must
 * not be culled while the effect lasts.
 */
@Mixin(Frustum.class)
public abstract class FrustumMixin {
    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true, require = 0)
    private void hexquantum$mirrorNoCull(AABB box, CallbackInfoReturnable<Boolean> cir) {
        if (ClientQuantumState.mirrorActive()) {
            cir.setReturnValue(true);
        }
    }
}
