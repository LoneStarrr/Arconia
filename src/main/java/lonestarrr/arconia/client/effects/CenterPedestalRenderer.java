package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import lonestarrr.arconia.client.particle.ModParticles;
import lonestarrr.arconia.common.block.entities.CenterPedestalBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CenterPedestalRenderer
    implements BlockEntityRenderer<
        CenterPedestalBlockEntity, CenterPedestalRenderer.CenterPedestalRenderState> {
  public CenterPedestalRenderer(BlockEntityRendererProvider.Context ctx) {}

  @Override
  public CenterPedestalRenderState createRenderState() {
    return new CenterPedestalRenderState();
  }

  @Override
  public void extractRenderState(
      CenterPedestalBlockEntity blockEntity,
      CenterPedestalRenderState renderState,
      float partialTick,
      Vec3 cameraPosition,
      @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(
        blockEntity, renderState, partialTick, cameraPosition, breakProgress);
    renderState.itemOnDisplay = blockEntity.getItemOnDisplay();
    renderState.ritualProgressPercentage = blockEntity.getRitualProgressPercentage();

    // Spawn particles during state extraction since the live block entity is available here
    Level level = blockEntity.getLevel();
    if (level != null
        && renderState.itemOnDisplay.isEmpty()
        && renderState.ritualProgressPercentage > 0) {
      float particleInterval = 3;
      if (blockEntity.lastParticleDisplayTime == 0
          || level.getGameTime() - blockEntity.lastParticleDisplayTime >= particleInterval) {
        blockEntity.lastParticleDisplayTime = level.getGameTime();
        spawnParticlesRandomPositions(blockEntity);
      }
    }
  }

  @Override
  public void submit(
      CenterPedestalRenderState state,
      PoseStack poseStack,
      SubmitNodeCollector nodeCollector,
      CameraRenderState cameraRenderState) {
    ItemStack stack = state.itemOnDisplay;
    if (stack.isEmpty()) {
      if (state.ritualProgressPercentage > 0) {
        renderRitualProgress(state, poseStack, nodeCollector);
      }
      return;
    }

    BlockPos bePos = state.blockPos;
    BlockPos itemPos = bePos.above();
    poseStack.pushPose();
    // BERs have the block entity at (0, 0, 0), compensate
    poseStack.translate(-bePos.getX(), -bePos.getY(), -bePos.getZ());
    ItemProjector.projectItem(
        stack,
        itemPos,
        poseStack,
        nodeCollector,
        state.lightCoords,
        OverlayTexture.NO_OVERLAY,
        false);
    poseStack.popPose();
  }

  private void renderRitualProgress(
      CenterPedestalRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
    BlockPos bePos = state.blockPos;
    poseStack.pushPose();

    poseStack.translate(0.5, 0.5, 0.5);
    // TODO Animation is choppy because % is updated every few ticks only, should track start time
    // on client side probably
    float progressPct = state.ritualProgressPercentage;
    float beamLength = 0.1f + (((float) progressPct / 100f) * 1.2f);
    int beamCount = Math.round(10 + (progressPct / 5));
    Random random = new Random(bePos.asLong());
    RainbowLightningProjector.renderRainbowLighting(
        random, beamLength, beamCount, poseStack, nodeCollector);
    poseStack.popPose();
  }

  private void spawnParticlesRandomPositions(CenterPedestalBlockEntity blockEntity) {
    // Should send this from the server (sendParticle) probably rather than hacking it in like this.
    BlockPos bePos = blockEntity.getBlockPos();
    Level level = blockEntity.getLevel();
    RandomSource rnd = level.getRandom();
    int spawnCount = 1;
    for (int i = 0; i < spawnCount; i++) {
      double speedX = 0;
      double speedY = 0.02;
      double speedZ = 0;
      // spawn particles AROUND the pedestal, but avoid the pedestal itself
      double posX =
          bePos.getX() + 0.5 + (rnd.nextFloat() * 0.5 + 0.4) * (rnd.nextInt(2) == 1 ? -1 : 1);
      double posY = bePos.getY() + 1 + rnd.nextFloat() * 0.5;
      double posZ =
          bePos.getZ() + 0.5 + (rnd.nextFloat() * 0.5 + 0.4) * (rnd.nextInt(2) == 1 ? -1 : 1);
      level.addParticle(
          ModParticles.RAINBOW_PARTICLES.get(), posX, posY, posZ, speedX, speedY, speedZ);
    }
  }

  public static class CenterPedestalRenderState extends BlockEntityRenderState {
    public ItemStack itemOnDisplay = ItemStack.EMPTY;
    public float ritualProgressPercentage = 0;
  }
}
