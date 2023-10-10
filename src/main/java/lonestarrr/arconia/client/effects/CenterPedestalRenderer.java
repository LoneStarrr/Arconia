package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import lonestarrr.arconia.common.block.entities.CenterPedestalBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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
        // TODO Animation is choppy because % is updated every few ticks only, should track start time on client side probably
        float progressPct = tileEntity.getRitualProgressPercentage();
        float beamLength = 0.1f + (((float)progressPct / 100f) * 1.2f);
        int beamCount = Math.round(10 + (progressPct / 5));
        //RainbowLightningProjector.renderRainbowLighting(bePos.above(), beamLength, beamCount, matrixStack, buffer);
        renderRainbow(bePos, progressPct, matrixStack, buffer);
        matrixStack.popPose();

    }

    private void renderRainbow(BlockPos pos, float progressPct, PoseStack poseStack, MultiBufferSource buffer) {
        final float diameter = 2 + (progressPct / 100 * 2.5f);
        poseStack.pushPose();
        poseStack.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        // Rotate along Y axis
        float angle = progressPct / 100 * 720;
        poseStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), angle, true));

        RainbowRenderer.renderRainbow(diameter, poseStack, buffer);
        poseStack.popPose();
    }
}
