package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import lonestarrr.arconia.client.particle.ModParticles;
import lonestarrr.arconia.client.particle.custom.RainbowParticles;
import lonestarrr.arconia.common.block.entities.HatBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HatRenderer implements BlockEntityRenderer<HatBlockEntity> {
    public static final long MIN_PARTICLE_INTERVAL = 25;
    public HatRenderer(BlockEntityRendererProvider.Context ctx) {}


    @Override
    public void render(
            HatBlockEntity hatEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight,
            int combinedOverlay) {

        showParticles(hatEntity);
        ItemStack stack = hatEntity.getResourceGenerated();
        if (stack == ItemStack.EMPTY) {
            return;
        }

        BlockPos hatPos = hatEntity.getBlockPos();
        BlockPos itemPos = hatPos.above();

        matrixStack.pushPose();
        // BERs have the tile entity at (0, 0, 0), compensate
        matrixStack.translate(-hatPos.getX(), -hatPos.getY(), -hatPos.getZ());
        ItemProjector.projectItem(stack, itemPos, matrixStack, buffer, combinedLight, combinedOverlay, true);

        matrixStack.popPose();
    }

    private void showParticles(HatBlockEntity hatEntity) {
        // Visualize when a hat is linked
        if (hatEntity.getLinkedPot() != null) {
            Level level = hatEntity.getLevel();
            long now = level.getGameTime();
            if (hatEntity.lastParticleRenderTime == 0 || now - hatEntity.lastParticleRenderTime >= MIN_PARTICLE_INTERVAL) {
                hatEntity.lastParticleRenderTime = now;
                int numParticles = level.random.nextInt(3);
                for (int i = 0; i < numParticles; i++) {
                    final BlockPos pos = hatEntity.getBlockPos();
                    final double posX = pos.getX() + 0.5 + level.random.nextFloat() * 0.2 * (level.random.nextBoolean() ? -1 : 1);
                    final double posY = pos.getY() + 1.2;
                    final double posZ = pos.getZ() + 0.5 + level.random.nextFloat() * 0.2 * (level.random.nextBoolean() ? -1 : 1);
                    final double speedX = 0;
                    final double speedY = 0.01;
                    final double speedZ = 0;
                    hatEntity.getLevel().addParticle(ModParticles.RAINBOW_PARTICLES.get(), posX, posY, posZ, speedX, speedY, speedZ);
                }
            }
        }
    }
}
