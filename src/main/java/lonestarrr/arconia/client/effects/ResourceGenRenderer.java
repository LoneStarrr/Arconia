package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import lonestarrr.arconia.common.block.entities.ResourceGenBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Random;

public class ResourceGenRenderer implements BlockEntityRenderer<ResourceGenBlockEntity> {

    public ResourceGenRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(
            ResourceGenBlockEntity blockEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {
        ItemStack stack = blockEntity.getItemStack();
        if (stack == ItemStack.EMPTY) {
            return;
        }

        BlockPos bePos = blockEntity.getBlockPos();
        BlockPos itemPos = bePos.above();

        matrixStack.pushPose();
        // BERs have the block entity at (0, 0, 0), compensate
        matrixStack.translate(-bePos.getX(), -bePos.getY(), -bePos.getZ());
        ItemProjector.projectItem(stack, itemPos, matrixStack, buffer, combinedLight, combinedOverlay, true);

        matrixStack.popPose();

        // add particles
        Level world = Minecraft.getInstance().level;
        long ticks = Minecraft.getInstance().level.getGameTime();

        Random random = world.getRandom();
        if (ticks > blockEntity.nextTickParticleRender) {
            blockEntity.nextTickParticleRender = ticks + 20 + random.nextInt(20);
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
