package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Methods for projecting an animated item
 */
public class ItemProjector {
    /**
     * Renders a floating animated item at the given block position.
     *
     * @param stack
     * @param itemPos
     * @param poseStack
     * @param buffer
     * @param combinedLight
     * @param combinedOverlay
     * @param forceShow attempt to show item, even in case of collissions
     */
    public static void projectItem(ItemStack stack, BlockPos itemPos, PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                                    int combinedOverlay, boolean forceShow) {
        // Don't draw the item if something's in the way
        Level level = Minecraft.getInstance().level;
        if (!forceShow) {
            VoxelShape shape = level.getBlockState(itemPos).getCollisionShape(level, itemPos);
            if (!shape.isEmpty() && shape.bounds().move(itemPos).contains(itemPos.getX(), itemPos.getY(), itemPos.getZ())) {
                return;
            }
        }

        poseStack.pushPose();
        poseStack.translate(itemPos.getX(), itemPos.getY(), itemPos.getZ());
        poseStack.translate(0.5, 0.1, 0.5);
        Vector3f rotationVector = new Vector3f(0, 1, 0);
        int light = LevelRenderer.getLightColor(Minecraft.getInstance().level, itemPos);
        long ticks = Minecraft.getInstance().level.getGameTime();

        // rotation animation
        float angleDegrees = (ticks % 128f) / 128f * 360;
        poseStack.mulPose(rotationVector.rotationDegrees(angleDegrees));

        // scaling animation
        final float SCALE_INTERVAL = 128f;
        final float HALF_INTERVAL = SCALE_INTERVAL / 2;
        float scale = (ticks % HALF_INTERVAL) / HALF_INTERVAL;
        scale = (ticks % SCALE_INTERVAL < HALF_INTERVAL ? scale: 1 - scale);
        scale = 0.5f + 0.1f * scale;
        poseStack.scale(scale, scale, scale);

        Minecraft.getInstance().getItemRenderer()
                .renderStatic(stack, ItemTransforms.TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, buffer, 0);
        poseStack.popPose();
    }
}
