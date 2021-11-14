package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders animations of items being flung out of a pot of gold into hats collecting the item
 * TODO This is a straight up copy of OrbLasers - do I want to get rid of that, or keep it and abstract out the shared logic?
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PotItemTransfers {
    private static final Set<ItemTransfer> transfers = new HashSet<>();

    public static void addItemTransfer(BlockPos hatPos, BlockPos potPos, ItemStack itemStack) {
        World world = Minecraft.getInstance().world;

        if (!world.isRemote()) {
            return;
        }

        long startTick = world.getGameTime();
        Vector3d hatPosExact = new Vector3d(hatPos.getX() + 0.5, hatPos.getY() + 0.5, hatPos.getZ() + 0.5);
        Vector3d potPosExact = new Vector3d(potPos.getX(), potPos.getY(), potPos.getZ());
        ItemTransfer transfer = new ItemTransfer(hatPosExact, potPosExact, itemStack, startTick);
        transfers.add(transfer);
    }

    @SubscribeEvent
    public static void render(RenderWorldLastEvent event) {
        World world = Minecraft.getInstance().world;

        long now = world.getGameTime();
        List<ItemTransfer> toRemove = new ArrayList<>();

        MatrixStack matrix = event.getMatrixStack();
        matrix.push();

        // Correct for player projection view
        Vector3d projected = Minecraft.getInstance().getRenderManager().info.getProjectedView();
        matrix.translate(-projected.getX(), -projected.getY(), -projected.getZ());

        // 2021-11-14 XXX This code crashes when a nether star is being transferred. I think it's because it has a glint.
        // I attempted to use my own buffer to no avail, it might be a bug in the mojang code?
//        BufferBuilder bufferBuilder = new BufferBuilder(2097152); // taken from Tesselator
//        IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(bufferBuilder);
        IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());

        // TODO add some random variation in speed, item rotation etc, otherwise it looks a bit boring with many items being flung simultaneously
        // Probably also add a random delay and don't fire them all at the same tick
        for (ItemTransfer transfer: transfers) {
            if (transfer.isComplete(event.getPartialTicks())) {
                toRemove.add(transfer);
                continue;
            }

            BlockPos playerPos = Minecraft.getInstance().player.getPosition();
            boolean fromCenter = true;
            if (playerPos.distanceSq(transfer.hatPos.x, transfer.hatPos.y, transfer.hatPos.z, fromCenter) > 64 * 64) {
                continue;
            }

            renderItemTransfer(transfer, event.getMatrixStack(), buffer, event.getPartialTicks());
        }
        buffer.finish();

        for (ItemTransfer transfer: toRemove) {
            transfers.remove(transfer);
        }

        matrix.pop();
    }

    private static void renderItemTransfer(ItemTransfer transfer, MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        double elapsedTicks = transfer.getTicksElapsed() + partialTicks;
        Vector3d itemPos = transfer.getPosition(elapsedTicks);
        int light = WorldRenderer.getCombinedLight(Minecraft.getInstance().world, new BlockPos(itemPos.x, itemPos.y, itemPos.z));
        matrixStack.push();
        matrixStack.translate(itemPos.x, itemPos.y, itemPos.z);
        Minecraft.getInstance().getItemRenderer()
                .renderItem(transfer.itemStack, ItemCameraTransforms.TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
        matrixStack.pop();
    }
}

class ItemTransfer {
    public static final int DISPLAY_TICKS = 20;
    public static final double GRAVITY = 2 / 20d;

    public final Vector3d hatPos;
    public final Vector3d potPos;
    public final long startTick;
    public final ItemStack itemStack;
    private final double gravity;
    private final double displayTicks;

    public ItemTransfer(Vector3d hatPos, Vector3d potPos, ItemStack itemStack, long startTick) {
        this.hatPos = hatPos;
        this.potPos = potPos;
        this.itemStack = itemStack.copy();
        this.startTick = startTick;
        // Vary gravity and speed a little for visual effect
        this.gravity = GRAVITY * (0.9 + Minecraft.getInstance().world.rand.nextFloat() / 10f);
        this.displayTicks = DISPLAY_TICKS * (0.6 + 0.4 * Minecraft.getInstance().world.rand.nextFloat());
    }

    public boolean isComplete(float partialTicks) {
        return getTicksElapsed() + partialTicks >= displayTicks;
    }

    /**
     * @param elapsedTicks Ticks elapsed since start of transfer. Must include any partial ticks.
     * @return
     *  Item location in transfer animation
     */
    public Vector3d getPosition(double elapsedTicks) {
        Vector3d velocity = getVelocity();
        // TODO move code below into ItemTransfer
        double itemX = potPos.x + velocity.x * elapsedTicks;
        double itemY = potPos.y + velocity.y * elapsedTicks - (ItemTransfer.GRAVITY * elapsedTicks * elapsedTicks) / 2d;
        double itemZ = potPos.z + velocity.z * elapsedTicks;
        return new Vector3d(itemX, itemY, itemZ);
    }

    /**
     * @return
     *     A velocity vector V for a parabolic animation of an item being flung at the hat where the fling time is constant.
     */
    public Vector3d getVelocity() {
        final double vx = (hatPos.x - potPos.x) / displayTicks;
        final double vz = (hatPos.z - potPos.z) / displayTicks;
        final double vy = (hatPos.y - potPos.y + (gravity * displayTicks * displayTicks) / 2d) / displayTicks;
        return new Vector3d(vx, vy, vz);
    }
    /**
     * @return Elapsed time % as a fraction [0..1]
     */
    public float getTimeElapsedPct() {
        return Math.min(1f, (Minecraft.getInstance().world.getGameTime() - startTick) / (float) DISPLAY_TICKS);

    }

    public long getTicksElapsed() {
        return Minecraft.getInstance().world.getGameTime() - startTick;
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