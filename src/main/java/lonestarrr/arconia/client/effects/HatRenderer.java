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
            HatBlockEntity tileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {
        // TODO Duplicate logic w/ResourceGenRenderer - separate out into shared code
        ItemStack stack = tileEntity.getResourceGenerated();
        if (stack == ItemStack.EMPTY) {
            return;
        }

        BlockPos tePos = tileEntity.getBlockPos();
        BlockPos itemPos = tePos.above();

        matrixStack.pushPose();
        // TER's have the tile entity at (0, 0, 0), compensate
        matrixStack.translate(-tePos.getX(), -tePos.getY(), -tePos.getZ());
        ItemProjector.projectItem(stack, itemPos, matrixStack, buffer, combinedLight, combinedOverlay, false);

        matrixStack.popPose();
    }
}
