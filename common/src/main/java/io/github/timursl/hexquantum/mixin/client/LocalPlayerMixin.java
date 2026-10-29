package io.github.timursl.hexquantum.mixin.client;

import io.github.timursl.hexquantum.shift.QuantumShift;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Quantum Shift: do not push the player out of the wall they are walking through.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"), cancellable = true, require = 0)
    private void hexquantum$noPushOut(double x, double z, CallbackInfo ci) {
        if (QuantumShift.isShifting((LocalPlayer) (Object) this)) {
            ci.cancel();
        }
    }
}
