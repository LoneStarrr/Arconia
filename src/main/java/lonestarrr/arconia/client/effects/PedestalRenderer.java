package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import lonestarrr.arconia.common.block.entities.PedestalBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity, PedestalRenderer.PedestalRenderState> {
    public PedestalRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public PedestalRenderState createRenderState() {
        return new PedestalRenderState();
    }

    @Override
    public void extractRenderState(
            PedestalBlockEntity blockEntity, PedestalRenderState renderState, float partialTick, Vec3 cameraPosition,
            @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.itemOnDisplay = blockEntity.getItemOnDisplay();
    }

    @Override
    public void submit(PedestalRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        ItemStack stack = state.itemOnDisplay;
        if (stack.isEmpty()) {
            return;
        }

        BlockPos bePos = state.blockPos;
        BlockPos itemPos = bePos.above();

        poseStack.pushPose();
        // BERs have the block entity at (0, 0, 0), compensate
        poseStack.translate(-bePos.getX(), -bePos.getY(), -bePos.getZ());
        ItemProjector.projectItem(stack, itemPos, poseStack, nodeCollector, state.lightCoords, 0, false);

        poseStack.popPose();
    }

    public static class PedestalRenderState extends BlockEntityRenderState {
        public ItemStack itemOnDisplay = ItemStack.EMPTY;
    }
}
