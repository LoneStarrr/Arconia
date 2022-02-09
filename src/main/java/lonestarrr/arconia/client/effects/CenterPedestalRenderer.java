package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import lonestarrr.arconia.common.block.tile.CenterPedestalTileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class CenterPedestalRenderer extends BlockEntityRenderer<CenterPedestalTileEntity> {
    public CenterPedestalRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(
            CenterPedestalTileEntity tileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {
        ItemStack stack = tileEntity.getItemOnDisplay();
        if (stack == ItemStack.EMPTY) {
            if (tileEntity.getRitualProgressPercentage() > 0) {
                renderRitualProgress(tileEntity, matrixStack, buffer, combinedLight, combinedOverlay);
            }
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

    private void renderRitualProgress(CenterPedestalTileEntity tileEntity, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
                                      int combinedOverlay) {

        BlockPos tePos = tileEntity.getBlockPos();
        matrixStack.pushPose();

        // TER's have the tile entity at (0, 0, 0), compensate
        matrixStack.translate(-tePos.getX(), -tePos.getY(), -tePos.getZ());
        int progressPct = tileEntity.getRitualProgressPercentage();
        float beamLength = 1 + progressPct / 50f;
        int beamCount = 4 + (progressPct / 10);
        RainbowLightningProjector.renderRainbowLighting(tePos.above(), beamLength, beamCount, matrixStack, buffer);
        matrixStack.popPose();

    }
}
