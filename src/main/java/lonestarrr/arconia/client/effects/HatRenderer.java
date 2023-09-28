package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import lonestarrr.arconia.common.block.entities.HatBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class HatRenderer implements BlockEntityRenderer<HatBlockEntity> {
    public HatRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(
            HatBlockEntity hatEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {
        ItemStack stack = hatEntity.getResourceGenerated();
        if (stack == ItemStack.EMPTY) {
            return;
        }

        BlockPos hatPos = hatEntity.getBlockPos();
        BlockPos itemPos = hatPos.above();

        matrixStack.pushPose();
        // BERs have the tile entity at (0, 0, 0), compensate
        matrixStack.translate(-hatPos.getX(), -hatPos.getY(), -hatPos.getZ());
        ItemProjector.projectItem(stack, itemPos, matrixStack, buffer, combinedLight, combinedOverlay, false);

        matrixStack.popPose();
    }
}
