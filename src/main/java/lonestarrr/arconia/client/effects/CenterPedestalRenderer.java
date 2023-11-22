package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import lonestarrr.arconia.client.particle.ModParticles;
import lonestarrr.arconia.common.block.entities.CenterPedestalBlockEntity;
import lonestarrr.arconia.common.item.MagicInABottle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class CenterPedestalRenderer implements BlockEntityRenderer<CenterPedestalBlockEntity> {
    public CenterPedestalRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(
            CenterPedestalBlockEntity tileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {
        ItemStack stack = tileEntity.getItemOnDisplay();
        if (stack.isEmpty()) {
            if (tileEntity.getRitualProgressPercentage() > 0) {
                renderRitualProgress(tileEntity, matrixStack, buffer, combinedLight, combinedOverlay, partialTicks);
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
            CenterPedestalBlockEntity blockEntity, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay, float partialTicks) {
        Level level = blockEntity.getLevel();
        BlockPos bePos = blockEntity.getBlockPos();
        matrixStack.pushPose();

        matrixStack.translate(0.5, 0.5, 0.5);
        // TODO Animation is choppy because % is updated every few ticks only, should track start time on client side probably
        float progressPct = blockEntity.getRitualProgressPercentage();
        float beamLength = 0.1f + (((float)progressPct / 100f) * 1.2f);
        int beamCount = Math.round(10 + (progressPct / 5));
        Random random = new Random(bePos.asLong());
        RainbowLightningProjector.renderRainbowLighting(random, beamLength, beamCount, matrixStack, buffer);
        //renderRainbow(bePos, progressPct, matrixStack, buffer)
        matrixStack.popPose();
        float particleInterval = 3;
        if (blockEntity.lastParticleDisplayTime == 0 || level.getGameTime() - blockEntity.lastParticleDisplayTime >= particleInterval) {
            blockEntity.lastParticleDisplayTime = level.getGameTime();
            spawnParticlesRandomPositions(blockEntity);
        }
    }

    private void spawnParticlesRandomPositions(CenterPedestalBlockEntity blockEntity) {
        // Should send this from the server (sendParticle) probably rather than hacking it in like this.
        BlockPos bePos = blockEntity.getBlockPos();
        Level level = blockEntity.getLevel();
        RandomSource rnd = level.getRandom();
        int spawnCount = 1;
        for (int i = 0; i < spawnCount; i++) {
            //double speedX = blockEntity.getLevel().random.nextGaussian() * Math.cos(i) * 0.2d;
            double speedX = 0;
            double speedY = 0.02;
            double speedZ = 0;
            // spawn particles AROUND the pedestal, but avoid the pedestal itself
            double posX = bePos.getX() + 0.5 + (rnd.nextFloat() * 0.5 + 0.4) * (rnd.nextInt(2) == 1 ? -1 : 1);
            double posY = bePos.getY() + 1 + rnd.nextFloat() * 0.5;
            double posZ = bePos.getZ() + 0.5 + (rnd.nextFloat() * 0.5 + 0.4) * (rnd.nextInt(2) == 1 ? -1 : 1);;
            level.addParticle(ModParticles.RAINBOW_PARTICLES.get(), posX, posY, posZ, speedX, speedY, speedZ);
        }

    }

    /* renders a rainbow in segments - just playing around. Probably won't use this for anything, ever */
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
