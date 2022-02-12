package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import lonestarrr.arconia.common.block.entities.PedestalBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity> {
    public PedestalRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(
            PedestalBlockEntity blockEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {
        // TODO Duplicate logic w/ResourceGenRenderer - separate out into shared code
        ItemStack stack = blockEntity.getItemOnDisplay();
        if (stack == ItemStack.EMPTY) {
            return;
        }

        BlockPos bePos = blockEntity.getBlockPos();
        BlockPos itemPos = bePos.above();

        matrixStack.pushPose();
        // BERs have the block entity at (0, 0, 0), compensate
        matrixStack.translate(-bePos.getX(), -bePos.getY(), -bePos.getZ());
        ItemProjector.projectItem(stack, itemPos, matrixStack, buffer, combinedLight, combinedOverlay, false);

        matrixStack.popPose();
    }
}
