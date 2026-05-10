package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders animation of items being flung between blocks.
 *
 * <p>TODO 1.21.9 port: rendering disabled. The legacy implementation rendered ItemStacks via
 * {@code ItemRenderer.renderStatic} during a {@code RenderLevelStageEvent.AfterParticles} hook,
 * but that API is gone — items now render via the deferred submit pipeline (see
 * {@code SubmitNodeCollector}), which is not exposed during this event. The transfer state is
 * still tracked so that a future renderer rewrite can resume drawing without touching callers.
 */
public class PotItemTransfers {
    private static final Set<ItemTransfer> transfers = new HashSet<>();

    public static void addItemTransfer(BlockPos startPos, BlockPos endPos, ItemStack itemStack) {
        Level world = Minecraft.getInstance().level;

        if (world == null || !world.isClientSide()) {
            return;
        }

        long startTick = world.getGameTime();
        Vec3 startPosExact = new Vec3(startPos.getX() + 0.5D, startPos.getY(), startPos.getZ() + 0.5D);
        Vec3 endPosExact = new Vec3(endPos.getX() + 0.5D, endPos.getY(), endPos.getZ() + 0.5D);
        ItemTransfer transfer = new ItemTransfer(startPosExact, endPosExact, itemStack, startTick);
        transfers.add(transfer);
    }

    public static void render(RenderLevelStageEvent.AfterTranslucentParticles event) {
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }

        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        List<ItemTransfer> toRemove = new ArrayList<>();

        for (ItemTransfer transfer : transfers) {
            if (transfer.isComplete(partialTick)) {
                toRemove.add(transfer);
                continue;
            }

            BlockPos playerPos = Minecraft.getInstance().player.blockPosition();
            if (playerPos.distToCenterSqr(transfer.startPos.x, transfer.startPos.y, transfer.startPos.z) > 64 * 64) {
                continue;
            }
            // TODO 1.21.9: re-implement item rendering for in-flight transfers. The new submit-based
            // render pipeline (SubmitNodeCollector) is not directly accessible during the
            // RenderLevelStageEvent. Options: migrate the effect to a particle, or render via a
            // BlockEntityRenderer that has access to a SubmitNodeCollector.
            // renderItemTransfer(transfer, event.getPoseStack(), buffer, event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }

        for (ItemTransfer transfer : toRemove) {
            transfers.remove(transfer);
        }
    }

    /*
    private static void renderItemTransfer(ItemTransfer transfer, PoseStack poseStack, MultiBufferSource buffer, float partialTicks) {
        double elapsedTicks = transfer.getTicksElapsed() + partialTicks;
        Vec3 itemPos = transfer.getPosition(elapsedTicks);
        int light = LevelRenderer.getLightColor(Minecraft.getInstance().level, new BlockPos((int)itemPos.x, (int)itemPos.y, (int)itemPos.z));
        poseStack.pushPose();
        poseStack.translate(itemPos.x, itemPos.y, itemPos.z);
        ItemStack toRender = transfer.itemStack;
        Minecraft.getInstance().getItemRenderer()
                .renderStatic(toRender, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, buffer, Minecraft.getInstance().level,
                        0);
        poseStack.popPose();
    }
    */
}

class ItemTransfer {
    public static final int DISPLAY_TICKS = 20;
    public static final double GRAVITY = 2 / 20d;

    public final Vec3 startPos;
    public final Vec3 endPos;
    public final long startTick;
    public final ItemStack itemStack;
    private final double gravity;
    private final double displayTicks;

    public ItemTransfer(Vec3 startPos, Vec3 endPos, ItemStack itemStack, long startTick) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.itemStack = itemStack.copy();
        this.startTick = startTick;
        // Vary gravity and speed a little for visual effect
        this.gravity = GRAVITY * (0.9 + Minecraft.getInstance().level.getRandom().nextFloat() / 10f);
        this.displayTicks = DISPLAY_TICKS * (0.6 + 0.4 * Minecraft.getInstance().level.getRandom().nextFloat());
    }

    public boolean isComplete(float partialTicks) {
        return getTicksElapsed() + partialTicks >= displayTicks;
    }

    /**
     * @param elapsedTicks Ticks elapsed since start of transfer. Must include any partial ticks.
     * @return Item location in transfer animation
     */
    public Vec3 getPosition(double elapsedTicks) {
        Vec3 velocity = getVelocity();
        double itemX = endPos.x + velocity.x * elapsedTicks;
        double itemY = endPos.y + velocity.y * elapsedTicks - (ItemTransfer.GRAVITY * elapsedTicks * elapsedTicks) / 2d;
        double itemZ = endPos.z + velocity.z * elapsedTicks;
        return new Vec3(itemX, itemY, itemZ);
    }

    /**
     * @return A velocity vector V for a parabolic animation of an item being flung at the hat where the fling time is constant.
     */
    public Vec3 getVelocity() {
        final double vx = (startPos.x - endPos.x) / displayTicks;
        final double vz = (startPos.z - endPos.z) / displayTicks;
        final double vy = (startPos.y - endPos.y + (gravity * displayTicks * displayTicks) / 2d) / displayTicks;
        return new Vec3(vx, vy, vz);
    }
    /**
     * @return Elapsed time % as a fraction [0..1]
     */
    public float getTimeElapsedPct() {
        return Math.min(1f, (Minecraft.getInstance().level.getGameTime() - startTick) / (float) DISPLAY_TICKS);
    }

    public long getTicksElapsed() {
        return Minecraft.getInstance().level.getGameTime() - startTick;
    }

    /**
     * @return Distance traveled % as a fraction [0..1] - not the same as time elapsed as the speed is non-linear.
     */
    public float getDistanceElapsedPct() {
        // f(y) = x^2, take x=0..4 and map y=0..16 to pctDistanceElapsed
        float pctTimeElapsed = getTimeElapsedPct();
        float x = 4 * pctTimeElapsed; // 0..4
        float pctDistanceElapsed = x * x / 16f; //0..1
        return pctDistanceElapsed;
    }
}
