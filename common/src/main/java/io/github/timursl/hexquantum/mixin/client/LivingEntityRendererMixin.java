package io.github.timursl.hexquantum.mixin.client;

import io.github.timursl.hexquantum.registry.HQEffects;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Regular phantom mobs are drawn see-through, using vanilla's own translucent path for "invisible but seen" mobs.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "isBodyVisible", at = @At("HEAD"), cancellable = true, require = 0)
    private void hexquantum$phantomsAreTranslucent(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.hasEffect(HQEffects.PHANTASMAL)) {
            cir.setReturnValue(false);
        }
    }
}
