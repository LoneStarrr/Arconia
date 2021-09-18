package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
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
import lonestarrr.arconia.client.core.handler.ColorHandler;
import lonestarrr.arconia.common.core.RainbowColor;

import java.util.*;
import java.util.List;

/**
 * Renders 'laser beams' being shot from an orb when it vacuums up items in the world
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class OrbLasers {
    private static final Set<LaserBeam> beams = new HashSet<LaserBeam>();

    public static void addLaserBeam(BlockPos orbPos, BlockPos itemPos, ItemStack itemStack) {
        World world = Minecraft.getInstance().world;

        if (!world.isRemote()) {
            return;
        }

        int beamColor = RainbowColor.RED.getColorValue();
        long startTick = world.getGameTime();
        Vector3d orbPosExact = new Vector3d(orbPos.getX() + 0.5, orbPos.getY() + 0.5, orbPos.getZ() + 0.5);
        Vector3d itemPosExact = new Vector3d(itemPos.getX(), itemPos.getY(), itemPos.getZ());
        LaserBeam beam = new LaserBeam(orbPosExact, itemPosExact, itemStack, startTick, beamColor);
        beams.add(beam);
    }

    @SubscribeEvent
    public static void render(RenderWorldLastEvent event) {
        World world = Minecraft.getInstance().world;

        long now = world.getGameTime();
        List<LaserBeam> toRemove = new ArrayList<>();

        MatrixStack matrix = event.getMatrixStack();
        matrix.push();

        // Correct for player projection view
        Vector3d projected = Minecraft.getInstance().getRenderManager().info.getProjectedView();
        matrix.translate(-projected.getX(), -projected.getY(), -projected.getZ());

        IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());

        for (LaserBeam beam: beams) {
            if (now - beam.startTick > LaserBeam.BEAM_DISPLAY_TICKS) {
                toRemove.add(beam);
                continue;
            }

            BlockPos playerPos = Minecraft.getInstance().player.getPosition();
            boolean fromCenter = true;
            if (playerPos.distanceSq(beam.orbPos.x, beam.orbPos.y, beam.orbPos.z, fromCenter) > 64 * 64) {
                continue;
            }

            renderBeamItem(beam, event.getMatrixStack(), buffer);
        }
        buffer.finish();

        for (LaserBeam beam: toRemove) {
            beams.remove(beam);
        }

        matrix.pop();
    }

    private static void renderBeamItem(LaserBeam beam, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        Vector3d directionVector = beam.itemPos.subtract(beam.orbPos);
        float pctRemaining = 1 - beam.getDistanceElapsedPct();
        double itemX = beam.orbPos.x + directionVector.x * pctRemaining;
        double itemY = beam.orbPos.y + directionVector.y * pctRemaining;
        double itemZ = beam.orbPos.z + directionVector.z * pctRemaining;
        int light = WorldRenderer.getCombinedLight(Minecraft.getInstance().world, new BlockPos(itemX, itemY, itemZ));
        matrixStack.push();
        matrixStack.translate(itemX, itemY, itemZ);
        Minecraft.getInstance().getItemRenderer()
                .renderItem(beam.itemStack, ItemCameraTransforms.TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
        matrixStack.pop();
    }
}

class LaserBeam {
    public static final int BEAM_DISPLAY_TICKS = 5;

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
     * @return Elapsed time % as a fraction [0..1]
     */
    public float getTimeElapsedPct() {
        return Math.min(1f, (Minecraft.getInstance().world.getGameTime() - startTick) / (float) BEAM_DISPLAY_TICKS);

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