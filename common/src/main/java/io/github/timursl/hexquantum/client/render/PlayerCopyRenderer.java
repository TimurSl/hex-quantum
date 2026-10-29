package io.github.timursl.hexquantum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.timursl.hexquantum.entity.PlayerCopyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Draws Illusions and Echoes as their owner, skin included. Ghostly copies use vanilla's see-through path.
 */
public class PlayerCopyRenderer<T extends PlayerCopyEntity> extends LivingEntityRenderer<T, PlayerModel<T>> {
    public PlayerCopyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        UUID owner = entity.getOwnerId();
        if (owner == null) {
            return DefaultPlayerSkin.getDefaultTexture();
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo info = connection == null ? null : connection.getPlayerInfo(owner);
        return info != null ? info.getSkin().texture() : DefaultPlayerSkin.get(owner).texture();
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        // Same scale as the vanilla player renderer.
        poseStack.scale(0.9375f, 0.9375f, 0.9375f);
    }

    @Override
    protected boolean isBodyVisible(T entity) {
        return !entity.isGhostly() && super.isBodyVisible(entity);
    }
}
