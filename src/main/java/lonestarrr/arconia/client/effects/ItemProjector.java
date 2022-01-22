package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

/**
 * Methods for projecting an animated item
 */
public class ItemProjector {
    /**
     * Renders a floating animated item at the given block position.
     *
     * @param stack
     * @param itemPos
     * @param matrixStack
     * @param buffer
     * @param combinedLight
     * @param combinedOverlay
     * @param forceShow attempt to show item, even in case of collissions
     */
    public static void projectItem(ItemStack stack, BlockPos itemPos, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight,
                                    int combinedOverlay, boolean forceShow) {
        // Don't draw the item if something's in the way
        World world = Minecraft.getInstance().level;
        if (!forceShow) {
            VoxelShape shape = world.getBlockState(itemPos).getCollisionShape(world, itemPos);
            if (!shape.isEmpty() && shape.bounds().move(itemPos).contains(itemPos.getX(), itemPos.getY(), itemPos.getZ())) {
                return;
            }
        }

        matrixStack.pushPose();
        matrixStack.translate(itemPos.getX(), itemPos.getY(), itemPos.getZ());
        matrixStack.translate(0.5, 0.1, 0.5);
        Vector3f rotationVector = new Vector3f(0, 1, 0);
        int light = WorldRenderer.getLightColor(Minecraft.getInstance().level, itemPos);
        long ticks = Minecraft.getInstance().level.getGameTime();

        // rotation animation
        float angleDegrees = (ticks % 128f) / 128f * 360;
        matrixStack.mulPose(rotationVector.rotationDegrees(angleDegrees));

        // scaling animation
        final float SCALE_INTERVAL = 128f;
        final float HALF_INTERVAL = SCALE_INTERVAL / 2;
        float scale = (ticks % HALF_INTERVAL) / HALF_INTERVAL;
        scale = (ticks % SCALE_INTERVAL < HALF_INTERVAL ? scale: 1 - scale);
        scale = 0.5f + 0.1f * scale;
        matrixStack.scale(scale, scale, scale);

        Minecraft.getInstance().getItemRenderer()
                .renderStatic(stack, ItemCameraTransforms.TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
        matrixStack.popPose();
    }
}
