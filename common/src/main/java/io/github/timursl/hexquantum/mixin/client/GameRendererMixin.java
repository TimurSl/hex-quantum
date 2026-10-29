package io.github.timursl.hexquantum.mixin.client;

import io.github.timursl.hexquantum.client.ShiftedPicking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Shifted Optics: aim from the displaced viewpoint.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    Minecraft minecraft;

    @Inject(method = "pick(F)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void hexquantum$shiftedPick(float partialTick, CallbackInfo ci) {
        if (ShiftedPicking.pick(this.minecraft, partialTick)) {
            ci.cancel();
        }
    }
}
