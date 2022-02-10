package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import lonestarrr.arconia.common.block.tile.OrbBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class OrbRenderer implements BlockEntityRenderer<OrbBlockEntity> {
    public OrbRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(
            OrbBlockEntity tileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {
        List<ItemStack> items = tileEntity.getItems();
        if (items.isEmpty()) {
            return;
        }

        BlockPos tePos = tileEntity.getBlockPos();
        BlockPos itemPos = tePos;

        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);

        int light = LevelRenderer.getLightColor(Minecraft.getInstance().level, itemPos);
        int ticksPerGoAround = 400;
        renderItemCarousel(items, matrixStack, buffer, light, ticksPerGoAround);
        matrixStack.popPose();
    }

    private void renderItemCarousel(List<ItemStack> items, PoseStack matrixStack, MultiBufferSource buffer, int light, int ticksPerCycle) {
        final Vector3f yAxis = new Vector3f(0f, 1f, 0f);
        boolean isDegrees = true;

        // Rotate the entire carousel for a nice visual effect
        float rotationCarousel = (Minecraft.getInstance().level.getGameTime() % ticksPerCycle) / (float)ticksPerCycle * 360f;
        matrixStack.pushPose();
        matrixStack.mulPose(new Quaternion(yAxis, rotationCarousel, isDegrees));

        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            float rotation = i * (360f / items.size());
            matrixStack.pushPose();
            matrixStack.mulPose(new Quaternion(yAxis, rotation, isDegrees));
            matrixStack.translate(0.3f, 0.0f, 0.0f);
            matrixStack.scale(0.4f, 0.4f, 0.4f);
            Minecraft.getInstance().getItemRenderer()
                    .renderStatic(item, ItemTransforms.TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer, 0);
            matrixStack.popPose();
        }

        matrixStack.popPose();
    }
}
