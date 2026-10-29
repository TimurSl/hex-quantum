package io.github.timursl.hexquantum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.timursl.hexquantum.block.PhantomBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Draws a phantom block as the block it imitates.
 */
public class PhantomBlockRenderer implements BlockEntityRenderer<PhantomBlockEntity> {
    private final BlockRenderDispatcher blocks;

    public PhantomBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.blocks = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(PhantomBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
        int packedLight, int packedOverlay) {
        blocks.renderSingleBlock(entity.getMimic(), poseStack, buffer, packedLight, packedOverlay);
    }
}
