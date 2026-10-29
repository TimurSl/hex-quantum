package io.github.timursl.hexquantum.client;

import io.github.timursl.hexquantum.network.ClientQuantumState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Shifted Optics: crosshair picking from the displaced viewpoint instead of the real eyes.
 */
public final class ShiftedPicking {
    private ShiftedPicking() {
    }

    /**
     * Replace {@code Minecraft.hitResult} with a pick from the shifted camera. Returns false if it did nothing.
     */
    public static boolean pick(Minecraft mc, float partialTick) {
        Entity camera = mc.getCameraEntity();
        if (!ClientQuantumState.shiftedActive() || camera == null || mc.level == null || mc.player == null) {
            return false;
        }
        Vec3 offset = ClientQuantumState.shiftedOffset;
        // The server grants extra reach equal to the offset length; do not let that leak into the shifted view.
        double extra = offset.length();
        double blockRange = Math.max(0, mc.player.blockInteractionRange() - extra);
        double entityRange = Math.max(0, mc.player.entityInteractionRange() - extra);
        double range = Math.max(blockRange, entityRange);

        Vec3 eye = camera.getEyePosition(partialTick).add(offset);
        Vec3 look = camera.getViewVector(partialTick);
        Vec3 end = eye.add(look.scale(range));

        HitResult result;
        BlockHitResult blockHit = mc.level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, camera));
        double blockDistSqr = blockHit.getType() == HitResult.Type.MISS ? Double.MAX_VALUE : blockHit.getLocation().distanceToSqr(eye);

        AABB box = camera.getBoundingBox().move(offset).expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(camera, eye, end, box,
            e -> !e.isSpectator() && e.isPickable(), range * range);
        if (entityHit != null && entityHit.getLocation().distanceToSqr(eye) < Math.min(blockDistSqr, entityRange * entityRange)) {
            result = entityHit;
        } else if (blockDistSqr <= blockRange * blockRange) {
            result = blockHit;
        } else {
            result = BlockHitResult.miss(end, Direction.getNearest(look.x, look.y, look.z), BlockPos.containing(end));
        }
        mc.hitResult = result;
        mc.crosshairPickEntity = result instanceof EntityHitResult hit ? hit.getEntity() : null;
        return true;
    }
}
