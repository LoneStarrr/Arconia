package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import lonestarrr.arconia.common.block.tile.PedestalTileEntity;
import lonestarrr.arconia.common.block.tile.ResourceGenTileEntity;

import java.util.Random;

public class PedestalRenderer extends BlockEntityRenderer<PedestalTileEntity> {
    public PedestalRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(
            PedestalTileEntity tileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {
        // TODO Duplicate logic w/ResourceGenRenderer - separate out into shared code
        ItemStack stack = tileEntity.getItemOnDisplay();
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
