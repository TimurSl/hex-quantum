package io.github.timursl.hexquantum.mixin;

import io.github.timursl.hexquantum.shift.QuantumShift;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Quantum Shift: walls do not block horizontal movement, but the floor still holds unless the player crouches.
 * Runs on both sides because player movement is computed by the client and re-checked by the server.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "collide", at = @At("HEAD"), cancellable = true, require = 0)
    private void hexquantum$quantumShiftCollide(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        Entity self = (Entity) (Object) this;
        if (!QuantumShift.isShifting(self)) {
            return;
        }
        double dy = movement.y;
        if (dy < 0 && !self.isShiftKeyDown()) {
            dy = Entity.collideBoundingBox(self, new Vec3(0, dy, 0), self.getBoundingBox(), self.level(), List.of()).y;
        }
        cir.setReturnValue(new Vec3(movement.x, dy, movement.z));
    }
}
