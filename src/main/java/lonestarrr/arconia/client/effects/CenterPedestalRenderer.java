package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import lonestarrr.arconia.common.block.entities.CenterPedestalBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class CenterPedestalRenderer implements BlockEntityRenderer<CenterPedestalBlockEntity> {
    public CenterPedestalRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(
            CenterPedestalBlockEntity tileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {
        ItemStack stack = tileEntity.getItemOnDisplay();
        if (stack == ItemStack.EMPTY) {
            if (tileEntity.getRitualProgressPercentage() > 0) {
                renderRitualProgress(tileEntity, matrixStack, buffer, combinedLight, combinedOverlay);
            }
            return;
        }

        BlockPos bePos = tileEntity.getBlockPos();
        BlockPos itemPos = bePos.above();
        matrixStack.pushPose();
        // BERs have the block entity at (0, 0, 0), compensate
        matrixStack.translate(-bePos.getX(), -bePos.getY(), -bePos.getZ());
        ItemProjector.projectItem(stack, itemPos, matrixStack, buffer, combinedLight, combinedOverlay, false);

        matrixStack.popPose();
    }

    private void renderRitualProgress(
            CenterPedestalBlockEntity tileEntity, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {

        BlockPos bePos = tileEntity.getBlockPos();
        matrixStack.pushPose();

        // BERs have the block entity at (0, 0, 0), compensate
        matrixStack.translate(-bePos.getX(), -bePos.getY(), -bePos.getZ());
        int progressPct = tileEntity.getRitualProgressPercentage();
        float beamLength = 1 + progressPct / 50f;
        int beamCount = 4 + (progressPct / 10);
        RainbowLightningProjector.renderRainbowLighting(bePos.above(), beamLength, beamCount, matrixStack, buffer);
        matrixStack.popPose();

    }
}
