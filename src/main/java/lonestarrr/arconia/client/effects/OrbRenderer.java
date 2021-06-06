package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import lonestarrr.arconia.common.block.tile.OrbTileEntity;
import lonestarrr.arconia.common.block.tile.PedestalTileEntity;

import java.util.ArrayList;
import java.util.List;

public class OrbRenderer extends TileEntityRenderer<OrbTileEntity> {
    public OrbRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(
            OrbTileEntity tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight,
            int combinedOverlay) {
        List<ItemStack> items = tileEntity.getItems();
        if (items.isEmpty()) {
            return;
        }

        BlockPos tePos = tileEntity.getPos();
        BlockPos itemPos = tePos;

        matrixStack.push();
        matrixStack.translate(0.5, 0.5, 0.5);

        int light = WorldRenderer.getCombinedLight(Minecraft.getInstance().world, itemPos);
        int ticksPerGoAround = 400;
        renderItemCarousel(items, matrixStack, buffer, light, ticksPerGoAround);
        matrixStack.pop();
    }

    private void renderItemCarousel(List<ItemStack> items, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int ticksPerCycle) {
        final Vector3f yAxis = new Vector3f(0f, 1f, 0f);
        boolean isDegrees = true;

        // Rotate the entire carousel for a nice visual effect
        float rotationCarousel = (Minecraft.getInstance().world.getGameTime() % ticksPerCycle) / (float)ticksPerCycle * 360f;
        matrixStack.push();
        matrixStack.rotate(new Quaternion(yAxis, rotationCarousel, isDegrees));

        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            float rotation = i * (360f / items.size());
            matrixStack.push();
            matrixStack.rotate(new Quaternion(yAxis, rotation, isDegrees));
            matrixStack.translate(0.3f, 0.0f, 0.0f);
            matrixStack.scale(0.4f, 0.4f, 0.4f);
            Minecraft.getInstance().getItemRenderer()
                    .renderItem(item, ItemCameraTransforms.TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.pop();
        }

        matrixStack.pop();
    }
}
