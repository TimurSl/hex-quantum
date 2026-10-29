package io.github.timursl.hexquantum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.timursl.hexquantum.entity.PhantomItemEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws a phantom item exactly like a dropped item (bobbing and spinning).
 */
public class PhantomItemRenderer extends EntityRenderer<PhantomItemEntity> {
    private final ItemRenderer itemRenderer;

    public PhantomItemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15f;
        this.shadowStrength = 0.75f;
    }

    @Override
    public void render(PhantomItemEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
        MultiBufferSource buffer, int packedLight) {
        ItemStack stack = entity.getItem();
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            float age = entity.tickCount + partialTick;
            BakedModel model = itemRenderer.getModel(stack, entity.level(), null, entity.getId());
            float bob = Mth.sin(age / 10.0f + entity.bobOffset) * 0.1f + 0.1f;
            float yScale = model.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
            poseStack.translate(0.0f, bob + 0.25f * yScale, 0.0f);
            poseStack.mulPose(Axis.YP.rotation(age / 20.0f + entity.bobOffset));
            itemRenderer.render(stack, ItemDisplayContext.GROUND, false, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, model);
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PhantomItemEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
