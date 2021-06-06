package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import lonestarrr.arconia.common.block.CenterPedestal;
import lonestarrr.arconia.common.block.tile.CenterPedestalTileEntity;
import lonestarrr.arconia.common.block.tile.PedestalTileEntity;

public class CenterPedestalRenderer extends TileEntityRenderer<CenterPedestalTileEntity> {
    public CenterPedestalRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(
            CenterPedestalTileEntity tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight,
            int combinedOverlay) {
        ItemStack stack = tileEntity.getItemOnDisplay();
        if (stack == ItemStack.EMPTY) {
            if (tileEntity.getRitualProgressPercentage() > 0) {
                renderRitualProgress(tileEntity, matrixStack, buffer, combinedLight, combinedOverlay);
            }
            return;
        }

        BlockPos tePos = tileEntity.getPos();
        BlockPos itemPos = tePos.up();
        matrixStack.push();
        // TER's have the tile entity at (0, 0, 0), compensate
        matrixStack.translate(-tePos.getX(), -tePos.getY(), -tePos.getZ());
        ItemProjector.projectItem(stack, itemPos, matrixStack, buffer, combinedLight, combinedOverlay, false);

        matrixStack.pop();
    }

    private void renderRitualProgress(CenterPedestalTileEntity tileEntity, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight,
                                      int combinedOverlay) {

        BlockPos tePos = tileEntity.getPos();
        matrixStack.push();

        // TER's have the tile entity at (0, 0, 0), compensate
        matrixStack.translate(-tePos.getX(), -tePos.getY(), -tePos.getZ());
        int progressPct = tileEntity.getRitualProgressPercentage();
        float beamLength = 1 + progressPct / 50f;
        int beamCount = 4 + (progressPct / 10);
        RainbowLightningProjector.renderRainbowLighting(tePos.up(), beamLength, beamCount, matrixStack, buffer);
        matrixStack.pop();

    }
}
