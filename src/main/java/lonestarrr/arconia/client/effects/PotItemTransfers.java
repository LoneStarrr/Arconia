package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RenderWorldLastEvent;

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

    public static void addItemTransfer(BlockPos hatPos, BlockPos potPos, ItemStack itemStack) {
        Level world = Minecraft.getInstance().level;

        if (!world.isClientSide()) {
            return;
        }

        long startTick = world.getGameTime();
        Vec3 hatPosExact = new Vec3(hatPos.getX() + 0.5, hatPos.getY() + 0.5, hatPos.getZ() + 0.5);
        Vec3 potPosExact = new Vec3(potPos.getX(), potPos.getY(), potPos.getZ());
        ItemTransfer transfer = new ItemTransfer(hatPosExact, potPosExact, itemStack, startTick);
        transfers.add(transfer);
    }

    public static void render(RenderWorldLastEvent event) {
        Level world = Minecraft.getInstance().level;

        long now = world.getGameTime();
        List<ItemTransfer> toRemove = new ArrayList<>();

        PoseStack matrix = event.getMatrixStack();
        matrix.pushPose();

        // Correct for player projection view
        Vec3 projected = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
        matrix.translate(-projected.x(), -projected.y(), -projected.z());

        // 2021-11-14 XXX This code crashes when a nether star is being transferred. I think it's because it has a glint.
        // I attempted to use my own buffer to no avail, it might be a bug in the mojang code?
//        BufferBuilder bufferBuilder = new BufferBuilder(2097152); // taken from Tesselator
//        IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(bufferBuilder);
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        // Probably also add a random delay and don't fire them all at the same tick
        for (ItemTransfer transfer: transfers) {
            if (transfer.isComplete(event.getPartialTicks())) {
                toRemove.add(transfer);
                continue;
            }

            BlockPos playerPos = Minecraft.getInstance().player.blockPosition();
            boolean fromCenter = true;
            if (playerPos.distSqr(transfer.hatPos.x, transfer.hatPos.y, transfer.hatPos.z, fromCenter) > 64 * 64) {
                continue;
            }

            renderItemTransfer(transfer, event.getMatrixStack(), buffer, event.getPartialTicks());
            // TODO: Temporary - testing rainbow rendering
//            RainbowRenderer.renderRainbow(transfer.potPos, transfer.hatPos, event.getMatrixStack(), buffer);
        }
        buffer.endBatch();

        for (ItemTransfer transfer: toRemove) {
            transfers.remove(transfer);
        }

        matrix.popPose();
    }

    private static void renderItemTransfer(ItemTransfer transfer, PoseStack matrixStack, MultiBufferSource buffer, float partialTicks) {
        double elapsedTicks = transfer.getTicksElapsed() + partialTicks;
        Vec3 itemPos = transfer.getPosition(elapsedTicks);
        int light = LevelRenderer.getLightColor(Minecraft.getInstance().level, new BlockPos(itemPos.x, itemPos.y, itemPos.z));
        matrixStack.pushPose();
        matrixStack.translate(itemPos.x, itemPos.y, itemPos.z);
        Minecraft.getInstance().getItemRenderer()
                .renderStatic(transfer.itemStack, ItemTransforms.TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
        matrixStack.popPose();
        // TODO temporarily rendering a rainbow just to see what it looks like

    }
}

class ItemTransfer {
    public static final int DISPLAY_TICKS = 20;
    public static final double GRAVITY = 2 / 20d;

    public final Vec3 hatPos;
    public final Vec3 potPos;
    public final long startTick;
    public final ItemStack itemStack;
    private final double gravity;
    private final double displayTicks;

    public ItemTransfer(Vec3 hatPos, Vec3 potPos, ItemStack itemStack, long startTick) {
        this.hatPos = hatPos;
        this.potPos = potPos;
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
        double itemX = potPos.x + velocity.x * elapsedTicks;
        double itemY = potPos.y + velocity.y * elapsedTicks - (ItemTransfer.GRAVITY * elapsedTicks * elapsedTicks) / 2d;
        double itemZ = potPos.z + velocity.z * elapsedTicks;
        return new Vec3(itemX, itemY, itemZ);
    }

    /**
     * @return
     *     A velocity vector V for a parabolic animation of an item being flung at the hat where the fling time is constant.
     */
    public Vec3 getVelocity() {
        final double vx = (hatPos.x - potPos.x) / displayTicks;
        final double vz = (hatPos.z - potPos.z) / displayTicks;
        final double vy = (hatPos.y - potPos.y + (gravity * displayTicks * displayTicks) / 2d) / displayTicks;
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