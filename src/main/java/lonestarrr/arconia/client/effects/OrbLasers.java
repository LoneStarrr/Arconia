package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders 'laser beams' being shot from an orb when it vacuums up items in the world
 */
public class OrbLasers {
    private static final Set<LaserBeam> beams = new HashSet<LaserBeam>();

    public static void addLaserBeam(BlockPos orbPos, BlockPos itemPos, ItemStack itemStack) {
        World world = Minecraft.getInstance().level;

        if (!world.isClientSide()) {
            return;
        }

        int beamColor = RainbowColor.RED.getColorValue();
        long startTick = world.getGameTime();
        Vector3d orbPosExact = new Vector3d(orbPos.getX() + 0.5, orbPos.getY() + 0.5, orbPos.getZ() + 0.5);
        Vector3d itemPosExact = new Vector3d(itemPos.getX(), itemPos.getY(), itemPos.getZ());
        LaserBeam beam = new LaserBeam(orbPosExact, itemPosExact, itemStack, startTick, beamColor);
        beams.add(beam);
    }

    public static void render(RenderWorldLastEvent event) {
        World world = Minecraft.getInstance().level;

        long now = world.getGameTime();
        List<LaserBeam> toRemove = new ArrayList<>();

        MatrixStack matrix = event.getMatrixStack();
        matrix.pushPose();

        // Correct for player projection view
        Vector3d projected = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
        matrix.translate(-projected.x(), -projected.y(), -projected.z());

        IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());

        for (LaserBeam beam: beams) {
            if (now - beam.startTick > LaserBeam.BEAM_DISPLAY_TICKS) {
                toRemove.add(beam);
                continue;
            }

            BlockPos playerPos = Minecraft.getInstance().player.blockPosition();
            boolean fromCenter = true;
            if (playerPos.distSqr(beam.orbPos.x, beam.orbPos.y, beam.orbPos.z, fromCenter) > 64 * 64) {
                continue;
            }

            renderBeamItem(beam, event.getMatrixStack(), buffer, event.getPartialTicks());
        }
        buffer.endBatch();

        for (LaserBeam beam: toRemove) {
            beams.remove(beam);
        }

        matrix.popPose();
    }

    private static void renderBeamItem(LaserBeam beam, MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        double elapsedTicks = beam.getTicksElapsed() + partialTicks;
        Vector3d velocity = beam.getVelocity(); // TODO - calculate once at beam creation time if this works out well
        // TODO move code below into beam 
        double itemX = beam.itemPos.x + velocity.x * elapsedTicks;
        double itemY = beam.itemPos.y + velocity.y * elapsedTicks - (LaserBeam.GRAVITY * elapsedTicks * elapsedTicks) / 2d;
        double itemZ = beam.itemPos.z + velocity.z * elapsedTicks;

        int light = WorldRenderer.getLightColor(Minecraft.getInstance().level, new BlockPos(itemX, itemY, itemZ));
        matrixStack.pushPose();
        matrixStack.translate(itemX, itemY, itemZ);
        Minecraft.getInstance().getItemRenderer()
                .renderStatic(beam.itemStack, ItemCameraTransforms.TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
        matrixStack.popPose();
    }
}

class LaserBeam {
    public static final int BEAM_DISPLAY_TICKS = 20;
    public static final double GRAVITY = 2 / 20d;

    public final Vector3d orbPos;
    public final Vector3d itemPos;
    public final long startTick;
    public final int beamColor;
    public final ItemStack itemStack;

    public LaserBeam(Vector3d orbPos, Vector3d itemPos, ItemStack itemStack, long startTick, int beamColor) {
        this.orbPos = orbPos;
        this.itemPos = itemPos;
        this.itemStack = itemStack.copy();
        this.startTick = startTick;
        this.beamColor = beamColor;
    }

    /**
     * @return
     *     A velocity vector V for a parabolic animation of an item being flung at the orb where the fling time is constant.
     */
    public Vector3d getVelocity() {
        final double animationTicks = BEAM_DISPLAY_TICKS;
        final double gravity = GRAVITY;
        final double vx = (orbPos.x - itemPos.x) / animationTicks;
        final double vz = (orbPos.z - itemPos.z) / animationTicks;
        final double vy = (orbPos.y - itemPos.y + (gravity * animationTicks * animationTicks) / 2d) / animationTicks;
        return new Vector3d(vx, vy, vz);
    }
    /**
     * @return Elapsed time % as a fraction [0..1]
     */
    public float getTimeElapsedPct() {
        return Math.min(1f, (Minecraft.getInstance().level.getGameTime() - startTick) / (float) BEAM_DISPLAY_TICKS);

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