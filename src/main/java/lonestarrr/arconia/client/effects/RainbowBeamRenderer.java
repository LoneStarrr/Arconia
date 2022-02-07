package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.tile.ArconiumTreeRootTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.VectorHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Draws pretty visual effects related to the resource tree
 *
 * TODO deprecated - remove me
 */
public class RainbowBeamRenderer extends TileEntityRenderer<ArconiumTreeRootTileEntity>  {
    public static final ResourceLocation BEAM_TEXTURE = new ResourceLocation(Arconia.MOD_ID, "effects/link");
    public static final ResourceLocation BEAM_ANIMATED_TEXTURE = new ResourceLocation(Arconia.MOD_ID, "effects/beam_animated");
    public static final ResourceLocation BEAM_SINE = new ResourceLocation(Arconia.MOD_ID, "effects/sine_wave");

    private static List<Vector2f> sinPrecalculated;
    private static final int NUM_SIN_VERTICES = 40;
    private int sinRenderOffsetGlobal = 0;
    public RainbowBeamRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    static {
        sinPrecalculated = new ArrayList<>(NUM_SIN_VERTICES);
        for (int i = 0; i < NUM_SIN_VERTICES; i++) {
            sinPrecalculated.add(new Vector2f(i / (float)NUM_SIN_VERTICES,
                    (float)Math.sin(i * Math.PI * 2 / (float)NUM_SIN_VERTICES)));
        }
    }

    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        // All textures are stitched into 1 large atlas texture - regular item / block models automatically take care of this,
        // but other textures need to be manually added to it for it to be available for rendering.
        // In this case, add the texture for the beam to the main atlas.
        if (event.getMap().location().equals((AtlasTexture.LOCATION_BLOCKS))) {
            event.addSprite(BEAM_TEXTURE);
            event.addSprite(BEAM_ANIMATED_TEXTURE);
            event.addSprite(BEAM_SINE);
        }
    }

    @Override
    public void render(
            ArconiumTreeRootTileEntity tileEntity, float partialTicks, MatrixStack matrixStack,
            IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockPos startPos = tileEntity.getBlockPos();
        BlockPos treeBasePos = startPos.above(2);
        RainbowColor tier = tileEntity.getTier();

//        renderWaveAnimated(startPos, endPos, matrixStack, buffer);
//          renderWaveLines(startPos, endPos, matrixStack, buffer);
//        renderSineSprite(startPos, endPos, matrixStack, buffer);
//          renderBeam(startPos, endPos, matrixStack, buffer);
        renderRainbowLightning(startPos, treeBasePos, tier, matrixStack, buffer);
    }

    private static double vectorLength(Vector3f v) {
        return Math.sqrt(Math.pow(v.x(), 2) + Math.pow(v.y(), 2) + Math.pow(v.z(), 2));

    }

    /**
     * Funky lightning effects appearing from a tree makes for a very colorful experience
     *
     * @param tePos Position of tile entity
     * @param treeBasePos Position of first log of tree
     * @param matrixStack
     * @param buffer
     */
    private void renderRainbowLightning(BlockPos tePos, BlockPos treeBasePos, RainbowColor tier, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        // Find the top of the tree trunk - that's where we want to generate the effect from
        final int maxLogCount = 7;
        final Block logBlock = Blocks.OAK_LOG;
        BlockPos scanPos = treeBasePos; // position of first log
        ClientWorld world = Minecraft.getInstance().level;
        if (world.getBlockState(treeBasePos).getBlock() != logBlock) {
            return;
        }
        int logsFound = 1;
        boolean trunkValid = true;
        BlockPos centerPos = null;

        while (trunkValid && centerPos == null) {
            scanPos = scanPos.above();
            if (world.getBlockState(scanPos).getBlock() != logBlock) {
                centerPos = scanPos.below().subtract(tePos); // Center of the visual effect
            } else if (++logsFound > maxLogCount) {
                trunkValid = false;
            }
        }

        if (!trunkValid) {
            return;
        }

        // Each tier has its own specific beam color
        int colorInt = tier.getColorValue();
        Color color = new Color(colorInt);
        matrixStack.pushPose();
        RainbowLightningProjector.renderRainbowLighting(centerPos, 2, 12, matrixStack, buffer, color);
        matrixStack.popPose();
    }

    /**
     * For this effect, an animated sprite is stretched between start and endpos, colorized with a shifting color, and drawn 3 times at different angles for a
     * 3D effect. Animation does not look great though unless it's either fast, or it needs a lot more frames for smoother animation.
     * @param startPos
     * @param endPos
     * @param matrixStack
     * @param buffer
     */
    private void renderWaveAnimated(BlockPos startPos, BlockPos endPos, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);
        Vector3d start = new Vector3d(startPos.getX(), startPos.getY(), startPos.getZ());
        Vector3d end = new Vector3d(endPos.getX(), endPos.getY(), endPos.getZ());
        Quaternion rotation = VectorHelper.getRotation(start, end);
        float distance = (float)end.subtract(start).length();
        matrixStack.mulPose(rotation);
        // Draw the animated sprite stretched out over the full distance along the X axis. Animation is taken care of by the game already based on the .mcmeta
        // file associated with the texture's png.
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(BEAM_ANIMATED_TEXTURE);
        IVertexBuilder builder = buffer.getBuffer(RainbowBeamRenderType.BEAM_TEXTURED);
        Matrix4f positionMatrix = matrixStack.last().pose();
        float x = 0;
        float y = 0;
        float z = 0;
        float amplitude = 0.0375f * distance;
        final int ticksPerColorCycle = 64;
        float hue = (float)(Minecraft.getInstance().level.getGameTime() % ticksPerColorCycle) / ticksPerColorCycle;
        float saturation = 1f;
        float brightness = 1f;
        Color color = Color.getHSBColor(hue, saturation, brightness);
        int colorR = color.getRed();
        int colorG = color.getGreen();
        int colorB = color.getBlue();
        int colorA = 192;

        // The animated sprite's individual frames need to be rotated by 90 degrees due to the way the spritesheet was generated
        for(int i = 0; i < 3; i++) {
            // Rotate around X axis to give it a 3D effect
            builder.vertex(positionMatrix, x + 0, y + 0, z).uv(sprite.getU1(), sprite.getV0()).color(colorR, colorG, colorB, colorA).endVertex();
            builder.vertex(positionMatrix, x + distance, y + 0, z).uv(sprite.getU1(), sprite.getV1()).color(colorR, colorG, colorB, colorA).endVertex();
            builder.vertex(positionMatrix, x + distance, y + amplitude * 0.3f, z).uv(sprite.getU0(), sprite.getV1()).color(colorR, colorG, colorB, colorA).endVertex();
            builder.vertex(positionMatrix, x + 0, y + amplitude, z).uv(sprite.getU0(), sprite.getV0()).color(colorR, colorG, colorB, colorA).endVertex();
            matrixStack.mulPose(new Quaternion(120, 0, 0, true));
        }
        matrixStack.popPose();
    }

    /**
     * For this effect, a sprite containing a prerendered sine wave is stretched between start and endpos. It is rotated around the X axis.
     * This one can be smooth and use a single high resolution sprite, and by drawing more of them in different colors it could look pretty good.
     * Downside is...the rotating wave looks very 2D if the sprite has any line thickness, and that can't really be fixed.
     *
     * @param startPos
     * @param endPos
     * @param matrixStack
     * @param buffer
     */
    private void renderSineSprite(BlockPos startPos, BlockPos endPos, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);


        Vector3d start = new Vector3d(startPos.getX(), startPos.getY(), startPos.getZ());
        Vector3d end = new Vector3d(endPos.getX(), endPos.getY(), endPos.getZ());
        Quaternion rotation = VectorHelper.getRotation(start, end);
        float distance = (float)end.subtract(start).length();
        matrixStack.mulPose(rotation);
        // Draw the animated sprite stretched out over the full distance along the X axis. Animation is taken care of by the game already based on the .mcmeta
        // file associated with the texture's png.
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(BEAM_SINE);
        IVertexBuilder builder = buffer.getBuffer(RainbowBeamRenderType.BEAM_TEXTURED);
        Matrix4f positionMatrix = matrixStack.last().pose();
        float x = 0;
        float y = 0;
        float z = 0;
        float amplitude = 0.25f * distance;
        final int ticksPerAnimationCycle = 64;
        final float cycle = Minecraft.getInstance().level.getGameTime() % ticksPerAnimationCycle;
        float hue = cycle / ticksPerAnimationCycle;
        float xRotation = (cycle / ticksPerAnimationCycle) * 360;
        float saturation = 1f;
        float brightness = 1f;
        Color color = Color.getHSBColor(hue, saturation, brightness);
        int colorR = color.getRed();
        int colorG = color.getGreen();
        int colorB = color.getBlue();
        int colorA = 192;

        matrixStack.mulPose(new Quaternion(xRotation, 0, 0, true));
        // Shift y down so that sprite is rendered with the center of the image along the X axis
        builder.vertex(positionMatrix, x + 0, y - 0.5f * amplitude, z).uv(sprite.getU0(), sprite.getV0()).color(colorR, colorG, colorB, colorA).endVertex();
        builder.vertex(positionMatrix, x + distance, y - 0.3f * amplitude, z).uv(sprite.getU1(), sprite.getV0()).color(colorR, colorG, colorB, colorA).endVertex();
        builder.vertex(positionMatrix, x + distance, y + 0.3f * amplitude, z).uv(sprite.getU1(), sprite.getV1()).color(colorR, colorG, colorB, colorA).endVertex();
        builder.vertex(positionMatrix, x + 0, y + 0.5f * amplitude, z).uv(sprite.getU0(), sprite.getV1()).color(colorR, colorG, colorB, colorA).endVertex();
        matrixStack.popPose();
    }

    /**
     * This effect renders a sine wave by drawing small connected edges - animation is done by shifting the offset of the sine wave. The animation itself
     * looks very smooth, and with some additional effects and optimizations, this is promising.
     * @param startPos
     * @param endPos
     * @param matrixStack
     * @param buffer
     */
    private void renderWaveLines(BlockPos startPos, BlockPos endPos, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);

        /*
         * To make an animated link connection, we render a sine wave. The animation involves changing the
         * amplitude of the wave so that the begin and end points stay fixed. For one cycle, the amplitude changes
         * from +1 to -1. Then, a second cycle does the inverse of that, thus ending up at the begin state once
         * both cycles are complete.
         */
        int ticksPerCycle = 100;
        int cycle = (int)(Minecraft.getInstance().level.getGameTime() % (ticksPerCycle * 2));
        boolean secondCycle = cycle >= ticksPerCycle;
        if (secondCycle) {
            cycle -= ticksPerCycle;
        }

        float amplitudeFactor;
        if (secondCycle) {
            amplitudeFactor = 2 * (float)(ticksPerCycle - cycle) / ticksPerCycle - 1; // -1..1
        } else {
            amplitudeFactor = 2 * (float)cycle / ticksPerCycle - 1;
        }

        float sineWidth = 5;
        float angleY = 0;
        amplitudeFactor *= (0.05 * sineWidth);

        int angleX = 90;

        for (int angleZ = 0; angleZ < 360; angleZ += 5) {
            float hue = angleZ / 360f;
            hue += ((float)cycle / ticksPerCycle);
            Color color = Color.getHSBColor(hue, 1f, 1f);
            Quaternion rotation = new Quaternion(angleX, angleY, angleZ, true);
            renderSineWave(matrixStack, buffer, rotation, sineWidth, amplitudeFactor, color);
        }

//        angleZ = 30;
//
//        for (int angleY = 10; angleY < 360; angleY += 20) {
//            float hue = 0.33f + angleY / 360f;
//            hue += ((float)cycle / ticksPerCycle);
//            Color color = Color.getHSBColor(hue, 1f, 1f);
//            Quaternion rotation = new Quaternion(angleX, angleY, angleZ, true);
//            renderSineWave(matrixStack, buffer, rotation, sineWidth, amplitudeFactor, color);
//        }
//
//        angleZ = 60;
//
//        for (int angleY = 10; angleY < 360; angleY += 20) {
//            float hue = 0.66f + angleY / 360f;
//            hue += ((float)cycle / ticksPerCycle);
//            Color color = Color.getHSBColor(hue, 1f, 1f);
//            Quaternion rotation = new Quaternion(angleX, angleY, angleZ, true);
//            renderSineWave(matrixStack, buffer, rotation, sineWidth, amplitudeFactor, color);
//        }

        matrixStack.popPose();
    }

    private void renderSineWave(MatrixStack matrixStack, IRenderTypeBuffer buffer, Quaternion rotation, float scaleX, float amplitudeFactor, Color color) {
        // Render a sine wave using Multiple line thicknesses and alpha values
        //int alpha = (int)((1 - Math.abs(progress - 0.5)) * 32 + 16);
        int alpha = 64;
        int[] colorInts = {color.getRed(), color.getGreen(), color.getBlue(), alpha};

        matrixStack.pushPose();
        matrixStack.mulPose(rotation);

        Matrix4f positionMatrix = matrixStack.last().pose();
        IVertexBuilder builder;
        builder = buffer.getBuffer(RainbowBeamRenderType.BEAM_LINE_THICK);
        renderSineWaveSegments(positionMatrix, builder, colorInts, scaleX, 0, amplitudeFactor);
        builder = buffer.getBuffer(RainbowBeamRenderType.BEAM_LINE_THIN);
        colorInts[3] = alpha * 2;
        renderSineWaveSegments(positionMatrix, builder, colorInts, scaleX, 0, amplitudeFactor);

        matrixStack.popPose();
    }

    private void renderSineWaveSegments(Matrix4f positionMatrix, IVertexBuilder builder, int[] color, float scaleX, int animationOffset, float amplitudeFactor) {
        // Render a sine wave using line segments
        for (int i = 0; i < sinPrecalculated.size(); i++) {
            int idxY = (i + animationOffset) % sinPrecalculated.size();
            int idxX = i;
            float x = sinPrecalculated.get(idxX).x * scaleX;
            float y = sinPrecalculated.get(idxY).y * amplitudeFactor;
            float z = 0;
            builder.vertex(positionMatrix, x, y, z).color(color[0], color[1], color[2], color[3]).endVertex();
            if (i !=0 && i < sinPrecalculated.size() - 1) {
                builder.vertex(positionMatrix, x, y, z).color(color[0], color[1], color[2], color[3]).endVertex();
            }
        }

        builder.endVertex();
    }

    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v) {
        // The order of the calls is important and is
        renderer.vertex(stack.last().pose(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(u, v)
                .uv2(0, 240) // Override lighting
                .normal(1, 0, 0) // Perpendicular to x-axis
                .endVertex();
    }

    /**
     * This method simply draws a straight line between start and endpos using 2 thicknesses and alpha settings for a nicer effect. Boring.
     * @param startPos
     * @param endPos
     * @param matrixStack
     * @param buffer
     */
    private void renderBeam(BlockPos startPos, BlockPos endPos, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        float progress = 1f;
        int alpha = (int)((1 - Math.abs(progress - 0.5)) * 32 + 16);
        int[] colors = {255, 0, 255};

        matrixStack.pushPose();
        matrixStack.translate(-startPos.getX(), -startPos.getY(), -startPos.getZ());
        matrixStack.translate(0.5f, 0.5f, 0.5f);
        Matrix4f positionMatrix = matrixStack.last().pose();
        IVertexBuilder builder = buffer.getBuffer(RainbowBeamRenderType.BEAM_LINE_THICK);
        builder.vertex(positionMatrix, startPos.getX(), startPos.getY(), startPos.getZ())
                .color(colors[0], colors[1], colors[2], alpha)
                .endVertex();
        builder.vertex(positionMatrix, endPos.getX(), endPos.getY(), endPos.getZ())
                .color(colors[0], colors[1], colors[2], alpha)
                .endVertex();

        IVertexBuilder builder2 = buffer.getBuffer(RainbowBeamRenderType.BEAM_LINE_THIN);
        builder2.vertex(positionMatrix, startPos.getX(), startPos.getY(), startPos.getZ())
                .color(colors[0], colors[1], colors[2], alpha * 2)
                .endVertex();
        builder2.vertex(positionMatrix, endPos.getX(), endPos.getY(), endPos.getZ())
                .color(colors[0], colors[1], colors[2], alpha * 2)
                .endVertex();
        matrixStack.popPose();
    }

}

/**
 * Render types for rendering the beam between tree and crate - inspiration gleaned from desht's ModularRouters
 */
@OnlyIn(Dist.CLIENT)
class RainbowBeamRenderType extends RenderType {
    private static final RenderState.LineState THICK_LINE = new RenderState.LineState(OptionalDouble.of(12));
    private static final RenderState.LineState THIN_LINE = new RenderState.LineState(OptionalDouble.of(4));

    public RainbowBeamRenderType(
            String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn,
            boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    public static final RenderType BEAM_LINE_THICK = RenderType.create("beam_line_thick",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.builder().setLineState(THICK_LINE)
                    .setLayeringState(RenderState.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TransparencyState.LIGHTNING_TRANSPARENCY)
                    .setLightmapState(RenderState.NO_LIGHTMAP)
                    .setTextureState(RenderState.NO_TEXTURE)
                    .setWriteMaskState(RenderState.COLOR_DEPTH_WRITE)
                    .createCompositeState(false)
    );

    public static final RenderType BEAM_LINE_THIN = create("beam_line_thin",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.builder().setLineState(THIN_LINE)
                    .setLayeringState(RenderState.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderState.TransparencyState.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(RenderState.NO_LIGHTMAP)
                    .setTextureState(RenderState.NO_TEXTURE)
                    .setWriteMaskState(RenderState.COLOR_DEPTH_WRITE)
                    .createCompositeState(false)
    );

    // RenderType for rendering a texture in 2D in the world. Note that the source here is not the texture, but the ATLAS containing the texture
    public static final RenderType BEAM_TEXTURED = create("beam_textured",
            DefaultVertexFormats.POSITION_TEX_COLOR, 7, 262144,
            RenderType.State.builder()
                    .setTextureState(new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS, false, false))
                    .setTransparencyState(TransparencyState.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderState.NO_CULL)
                    .setWriteMaskState(RenderState.COLOR_WRITE)
                    .createCompositeState(false)
    );
}