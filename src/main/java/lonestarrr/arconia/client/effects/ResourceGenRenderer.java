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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.level.Level;
import lonestarrr.arconia.common.block.tile.ResourceGenTileEntity;

import java.util.Random;

public class ResourceGenRenderer extends BlockEntityRenderer<ResourceGenTileEntity> {

    public ResourceGenRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(
            ResourceGenTileEntity tileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {
        ItemStack stack = tileEntity.getItemStack();
        if (stack == ItemStack.EMPTY) {
            return;
        }

        BlockPos tePos = tileEntity.getBlockPos();
        BlockPos itemPos = tePos.above();

        matrixStack.pushPose();
        // TER's have the tile entity at (0, 0, 0), compensate
        matrixStack.translate(-tePos.getX(), -tePos.getY(), -tePos.getZ());
        ItemProjector.projectItem(stack, itemPos, matrixStack, buffer, combinedLight, combinedOverlay, true);

        matrixStack.popPose();

        // add particles
        Level world = Minecraft.getInstance().level;
        long ticks = Minecraft.getInstance().level.getGameTime();

        Random random = world.getRandom();
        if (ticks > tileEntity.nextTickParticleRender) {
            tileEntity.nextTickParticleRender = ticks + 20 + random.nextInt(20);
            double yOffset = 0.03125D;
            double xzOffset = (double) 0.3F;
            double xzVariation = (double) 0.4F;
            double speedX = random.nextGaussian() * 0.04D;
            double speedY = random.nextGaussian() * 0.04D;
            double speedZ = random.nextGaussian() * 0.04D;
            world.addParticle(ParticleTypes.COMPOSTER, (double) itemPos.getX() + xzOffset + xzVariation * (double) random.nextFloat(),
                    (double) itemPos.getY() + yOffset + (double) random.nextFloat() * (0.5D - yOffset),
                    (double) itemPos.getZ() + xzOffset + xzVariation * (double) random.nextFloat(), speedX, speedY, speedZ);
        }
    }
}
