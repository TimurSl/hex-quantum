package io.github.timursl.hexquantum.mixin.client;

import io.github.timursl.hexquantum.shift.QuantumShift;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Quantum Shift: no "inside a block" overlay while phasing through walls.
 */
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
    @Inject(method = "getViewBlockingState", at = @At("HEAD"), cancellable = true, require = 0)
    private static void hexquantum$seeThroughWalls(Player player, CallbackInfoReturnable<BlockState> cir) {
        if (QuantumShift.isShifting(player)) {
            cir.setReturnValue(null);
        }
    }
}
