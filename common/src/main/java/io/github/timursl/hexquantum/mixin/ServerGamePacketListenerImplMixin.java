package io.github.timursl.hexquantum.mixin;

import io.github.timursl.hexquantum.shift.QuantumShift;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Quantum Shift: the server must accept a shifted player moving into blocks instead of rubber-banding them.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "isPlayerCollidingWithAnythingNew", at = @At("HEAD"), cancellable = true, require = 0)
    private void hexquantum$quantumShiftNoRubberBand(LevelReader level, AABB box, double x, double y, double z,
        CallbackInfoReturnable<Boolean> cir) {
        if (QuantumShift.isShifting(this.player)) {
            cir.setReturnValue(false);
        }
    }
}
