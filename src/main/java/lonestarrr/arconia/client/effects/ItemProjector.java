package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Methods for projecting an animated item via the new submit-based render pipeline.
 */
public class ItemProjector {
    /**
     * Renders a floating animated item at the given block position.
     *
     * @param stack item to render
     * @param itemPos position (in block coords) of the item
     * @param poseStack matrix stack
     * @param nodeCollector node collector to submit draw calls into
     * @param combinedLight packed light coordinates
     * @param combinedOverlay packed overlay coordinates
     * @param forceShow attempt to show item, even in case of collisions
     */
    public static void projectItem(ItemStack stack, BlockPos itemPos, PoseStack poseStack, SubmitNodeCollector nodeCollector, int combinedLight,
                                    int combinedOverlay, boolean forceShow) {
        // Don't draw the item if something's in the way
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
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
        int light = LevelRenderer.getLightColor(level, itemPos);
        long ticks = level.getGameTime();

        // rotation animation
        float angleDegrees = (ticks % 128f) / 128f * 360;
        poseStack.mulPose((new Quaternionf()).fromAxisAngleDeg(rotationVector, angleDegrees));

        // scaling animation
        final float SCALE_INTERVAL = 128f;
        final float HALF_INTERVAL = SCALE_INTERVAL / 2;
        float scale = (ticks % HALF_INTERVAL) / HALF_INTERVAL;
        scale = (ticks % SCALE_INTERVAL < HALF_INTERVAL ? scale: 1 - scale);
        scale = 0.75f + 0.25f * scale;
        poseStack.scale(scale, scale, scale);

        submitItemStack(stack, poseStack, nodeCollector, light, combinedOverlay);
        poseStack.popPose();
    }

    /** Displays a carousel of multiple items, where the carousel itself rotates around its center coordinate over time
     *
      * @param items items to display in carousel
     * @param poseStack matrix stack
     * @param nodeCollector node collector to submit draw calls into
     * @param light packed light
     * @param ticksPerRotation How fast to rotate
     * @param distanceFromCenter How far from the center should the items render (e.g. radius)
     * @param scale Item display scale factor
     * @param itemsPerLevel How many items to display per y layer. If there are more items, a new y layer will be rendered above
     * @param levelOffset Additional render offset (x+y) for each extra y layer
     */
    public static void projectItemCarousel(List<ItemStack> items, PoseStack poseStack, SubmitNodeCollector nodeCollector, int light, int ticksPerRotation, float distanceFromCenter, float scale, int itemsPerLevel, float levelOffset) {
        final Vector3f yAxis = new Vector3f(0f, 1f, 0f);

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        // Rotate the entire carousel for a nice visual effect
        float rotationCarousel = (level.getGameTime() % ticksPerRotation) / (float)ticksPerRotation * 360f;
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(yAxis, rotationCarousel));

        for (int i = 0; i < items.size(); i++) {
            int yLevel = Math.floorDiv(i, itemsPerLevel);
            int itemsOnThisLevel = Math.min(itemsPerLevel, items.size() - (yLevel * itemsPerLevel));
            float yOffset = yLevel * levelOffset;
            float xOffset = distanceFromCenter + (yLevel * levelOffset);
            ItemStack item = items.get(i);
            float rotation = i * (360f / itemsOnThisLevel);
            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(yAxis, rotation));
            poseStack.translate(xOffset, yOffset, 0.0f);
            poseStack.scale(scale, scale, scale);
            submitItemStack(item, poseStack, nodeCollector, light, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void submitItemStack(ItemStack stack, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemStackRenderState state = new ItemStackRenderState();
        minecraft.getItemModelResolver().updateForTopItem(state, stack, ItemDisplayContext.GROUND, minecraft.level, null, 0);
        state.submit(poseStack, nodeCollector, packedLight, packedOverlay, 0);
    }
}
