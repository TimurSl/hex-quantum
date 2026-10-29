package io.github.timursl.hexquantum.client;

import io.github.timursl.hexquantum.HexQuantum;
import io.github.timursl.hexquantum.network.ClientQuantumState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Mirror Optics: draws the terrain a second time, mirrored across the horizontal plane at the camera's eye level.
 * It is only geometry on screen; the server knows nothing about it, so it has no collision.
 * <p>
 * Uses {@code LevelRenderer.renderSectionLayer} by reflection so that a mismatch only disables the effect (with a
 * log line) instead of crashing the game.
 */
public final class MirrorRenderer {
    private static final List<RenderType> LAYERS = List.of(RenderType.solid(), RenderType.cutoutMipped(), RenderType.cutout());
    private static Method renderSectionLayer;
    private static boolean unavailable;

    private MirrorRenderer() {
    }

    public static void render(LevelRenderer levelRenderer, Matrix4f modelView, Matrix4f projection, Camera camera) {
        if (!ClientQuantumState.mirrorActive() || unavailable) {
            return;
        }
        Method method = method();
        if (method == null) {
            return;
        }
        Vec3 cam = camera.getPosition();
        // Positions reaching the shader are camera-relative, so flipping Y here mirrors the world about eye level.
        Matrix4f mirrored = new Matrix4f(modelView).scale(1.0f, -1.0f, 1.0f);
        // Mirroring flips triangle winding; swap the front face so back-face culling still hides the right faces.
        GL11.glFrontFace(GL11.GL_CW);
        try {
            for (RenderType layer : LAYERS) {
                method.invoke(levelRenderer, layer, cam.x, cam.y, cam.z, mirrored, projection);
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            unavailable = true;
            HexQuantum.LOGGER.error("Mirror Optics rendering failed and has been disabled", e);
        } finally {
            GL11.glFrontFace(GL11.GL_CCW);
        }
    }

    private static Method method() {
        if (renderSectionLayer == null) {
            try {
                renderSectionLayer = LevelRenderer.class.getDeclaredMethod("renderSectionLayer", RenderType.class,
                    double.class, double.class, double.class, Matrix4f.class, Matrix4f.class);
                renderSectionLayer.setAccessible(true);
            } catch (ReflectiveOperationException | RuntimeException e) {
                unavailable = true;
                HexQuantum.LOGGER.error("Mirror Optics is unavailable: LevelRenderer.renderSectionLayer not found", e);
                return null;
            }
        }
        return renderSectionLayer;
    }
}
