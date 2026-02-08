package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders animations of items being flung out of a pot of gold into hats collecting the item
 * TODO This is a straight up copy of OrbLasers - do I want to get rid of that, or keep it and abstract out the shared logic?
 */
public class PotItemTransfers {
    private static final Set<ItemTransfer> transfers = new HashSet<>();

    public static void addItemTransfer(BlockPos startPos, BlockPos endPos, ItemStack itemStack) {
        Level world = Minecraft.getInstance().level;

        if (!world.isClientSide()) {
            return;
        }

        long startTick = world.getGameTime();
        Vec3 startPosExact = new Vec3(startPos.getX() + 0.5D, startPos.getY(), startPos.getZ() + 0.5D);
        Vec3 endPosExact = new Vec3(endPos.getX() + 0.5D, endPos.getY(), endPos.getZ() + 0.5D);
        ItemTransfer transfer = new ItemTransfer(startPosExact, endPosExact, itemStack, startTick);
        transfers.add(transfer);
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Level world = Minecraft.getInstance().level;

        long now = world.getGameTime();
        List<ItemTransfer> toRemove = new ArrayList<>();

        PoseStack matrix = event.getPoseStack();
        matrix.pushPose();

        // Correct for player projection view
        Vec3 projected = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
        matrix.translate(-projected.x(), -projected.y(), -projected.z());

        // 2021-11-14 XXX This code crashes when a nether star is being transferred. I think it's because it has a glint.
        // I attempted to use my own buffer to no avail, it might be a bug in the mojang code?
//        BufferBuilder bufferBuilder = new BufferBuilder(2097152); // taken from Tesselator
//        IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(bufferBuilder);
//        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        // 2023-10-08 Traced buffer used in block entity renderer's buffer, which had a large fixedBuffers list, while mine had none. Traced to where those
        // were set and found this. This should prevent crashes on special render types
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        // Probably also add a random delay and don't fire them all at the same tick
        for (ItemTransfer transfer: transfers) {
            if (transfer.isComplete(event.getPartialTick().getGameTimeDeltaPartialTick(false))) {
                toRemove.add(transfer);
                continue;
            }

            BlockPos playerPos = Minecraft.getInstance().player.blockPosition();
            if (playerPos.distToCenterSqr(transfer.startPos.x, transfer.startPos.y, transfer.startPos.z) > 64 * 64) {
                continue;
            }

            renderItemTransfer(transfer, event.getPoseStack(), buffer, event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
        buffer.endBatch();

        for (ItemTransfer transfer: toRemove) {
            transfers.remove(transfer);
        }

        matrix.popPose();
    }

    private static void renderItemTransfer(ItemTransfer transfer, PoseStack poseStack, MultiBufferSource buffer, float partialTicks) {
        double elapsedTicks = transfer.getTicksElapsed() + partialTicks;
        Vec3 itemPos = transfer.getPosition(elapsedTicks);
        int light = LevelRenderer.getLightColor(Minecraft.getInstance().level, new BlockPos((int)itemPos.x, (int)itemPos.y, (int)itemPos.z));
        poseStack.pushPose();
        poseStack.translate(itemPos.x, itemPos.y, itemPos.z);
        ItemStack toRender = transfer.itemStack;
        Minecraft.getInstance().getItemRenderer()
                .renderStatic(toRender, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, buffer, Minecraft.getInstance().level, 0);
        poseStack.popPose();
    }
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
        this.gravity = GRAVITY * (0.9 + Minecraft.getInstance().level.random.nextFloat() / 10f);
        this.displayTicks = DISPLAY_TICKS * (0.6 + 0.4 * Minecraft.getInstance().level.random.nextFloat());
    }

    public boolean isComplete(float partialTicks) {
        return getTicksElapsed() + partialTicks >= displayTicks;
    }

    /**
     * @param elapsedTicks Ticks elapsed since start of transfer. Must include any partial ticks.
     * @return
     *  Item location in transfer animation
     */
    public Vec3 getPosition(double elapsedTicks) {
        Vec3 velocity = getVelocity();
        // TODO move code below into ItemTransfer
        double itemX = endPos.x + velocity.x * elapsedTicks;
        double itemY = endPos.y + velocity.y * elapsedTicks - (ItemTransfer.GRAVITY * elapsedTicks * elapsedTicks) / 2d;
        double itemZ = endPos.z + velocity.z * elapsedTicks;
        return new Vec3(itemX, itemY, itemZ);
    }

    /**
     * @return
     *     A velocity vector V for a parabolic animation of an item being flung at the hat where the fling time is constant.
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