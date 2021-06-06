package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.tile.ResourceTreeRootTileEntity;
import lonestarrr.arconia.common.core.helper.VectorHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Random;

/**
 * Draws pretty visual effects related to the resource tree
 */
@Mod.EventBusSubscriber(modid=Arconia.MOD_ID, value= Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public class RainbowBeamRenderer extends TileEntityRenderer<ResourceTreeRootTileEntity>  {
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

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        // All textures are stitched into 1 large atlas texture - regular item / block models automatically take care of this,
        // but other textures need to be manually added to it for it to be available for rendering.
        // In this case, add the texture for the beam to the main atlas.
        if (event.getMap().getTextureLocation().equals((AtlasTexture.LOCATION_BLOCKS_TEXTURE))) {
            event.addSprite(BEAM_TEXTURE);
            event.addSprite(BEAM_ANIMATED_TEXTURE);
            event.addSprite(BEAM_SINE);
        }
    }

    @Override
    public void render(
            ResourceTreeRootTileEntity tileEntity, float partialTicks, MatrixStack matrixStack,
            IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockPos startPos = tileEntity.getPos();
        BlockPos treeBasePos = startPos.up(2);

//        renderWaveAnimated(startPos, endPos, matrixStack, buffer);
//          renderWaveLines(startPos, endPos, matrixStack, buffer);
//        renderSineSprite(startPos, endPos, matrixStack, buffer);
//          renderBeam(startPos, endPos, matrixStack, buffer);
        renderRainbowLightning(startPos, treeBasePos, matrixStack, buffer);
    }

    private static double vectorLength(Vector3f v) {
        return Math.sqrt(Math.pow(v.getX(), 2) + Math.pow(v.getY(), 2) + Math.pow(v.getZ(), 2));

    }

    /**
     * Funky lightning effects appearing from a tree makes for a very colorful experience
     *
     * @param tePos Position of tile entity
     * @param treeBasePos Position of first log of tree
     * @param matrixStack
     * @param buffer
     */
    private void renderRainbowLightning(BlockPos tePos, BlockPos treeBasePos, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        // Find the top of the tree trunk - that's where we want to generate the effect from
        final int maxLogCount = 7;
        final Block logBlock = Blocks.OAK_LOG;
        BlockPos scanPos = treeBasePos; // position of first log
        ClientWorld world = Minecraft.getInstance().world;
        if (world.getBlockState(treeBasePos).getBlock() != logBlock) {
            return;
        }
        int logsFound = 1;
        boolean trunkValid = true;
        BlockPos centerPos = null;

        while (trunkValid && centerPos == null) {
            scanPos = scanPos.up();
            if (world.getBlockState(scanPos).getBlock() != logBlock) {
                centerPos = scanPos.down().subtract(tePos); // Center of the visual effect
            } else if (++logsFound > maxLogCount) {
                trunkValid = false;
            }
        }

        if (!trunkValid) {
            return;
        }

        matrixStack.push();
        RainbowLightningProjector.renderRainbowLighting(centerPos, 3, 16, matrixStack, buffer);

        matrixStack.pop();

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
        matrixStack.push();
        matrixStack.translate(0.5, 0.5, 0.5);
        Vector3d start = new Vector3d(startPos.getX(), startPos.getY(), startPos.getZ());
        Vector3d end = new Vector3d(endPos.getX(), endPos.getY(), endPos.getZ());
        Quaternion rotation = VectorHelper.getRotation(start, end);
        float distance = (float)end.subtract(start).length();
        matrixStack.rotate(rotation);
        // Draw the animated sprite stretched out over the full distance along the X axis. Animation is taken care of by the game already based on the .mcmeta
        // file associated with the texture's png.
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(BEAM_ANIMATED_TEXTURE);
        IVertexBuilder builder = buffer.getBuffer(RainbowBeamRenderType.BEAM_TEXTURED);
        Matrix4f positionMatrix = matrixStack.getLast().getMatrix();
        float x = 0;
        float y = 0;
        float z = 0;
        float amplitude = 0.0375f * distance;
        final int ticksPerColorCycle = 64;
        float hue = (float)(Minecraft.getInstance().world.getGameTime() % ticksPerColorCycle) / ticksPerColorCycle;
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
            builder.pos(positionMatrix, x + 0, y + 0, z).tex(sprite.getMaxU(), sprite.getMinV()).color(colorR, colorG, colorB, colorA).endVertex();
            builder.pos(positionMatrix, x + distance, y + 0, z).tex(sprite.getMaxU(), sprite.getMaxV()).color(colorR, colorG, colorB, colorA).endVertex();
            builder.pos(positionMatrix, x + distance, y + amplitude * 0.3f, z).tex(sprite.getMinU(), sprite.getMaxV()).color(colorR, colorG, colorB, colorA).endVertex();
            builder.pos(positionMatrix, x + 0, y + amplitude, z).tex(sprite.getMinU(), sprite.getMinV()).color(colorR, colorG, colorB, colorA).endVertex();
            matrixStack.rotate(new Quaternion(120, 0, 0, true));
        }
        matrixStack.pop();
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
        matrixStack.push();
        matrixStack.translate(0.5, 0.5, 0.5);


        Vector3d start = new Vector3d(startPos.getX(), startPos.getY(), startPos.getZ());
        Vector3d end = new Vector3d(endPos.getX(), endPos.getY(), endPos.getZ());
        Quaternion rotation = VectorHelper.getRotation(start, end);
        float distance = (float)end.subtract(start).length();
        matrixStack.rotate(rotation);
        // Draw the animated sprite stretched out over the full distance along the X axis. Animation is taken care of by the game already based on the .mcmeta
        // file associated with the texture's png.
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(BEAM_SINE);
        IVertexBuilder builder = buffer.getBuffer(RainbowBeamRenderType.BEAM_TEXTURED);
        Matrix4f positionMatrix = matrixStack.getLast().getMatrix();
        float x = 0;
        float y = 0;
        float z = 0;
        float amplitude = 0.25f * distance;
        final int ticksPerAnimationCycle = 64;
        final float cycle = Minecraft.getInstance().world.getGameTime() % ticksPerAnimationCycle;
        float hue = cycle / ticksPerAnimationCycle;
        float xRotation = (cycle / ticksPerAnimationCycle) * 360;
        float saturation = 1f;
        float brightness = 1f;
        Color color = Color.getHSBColor(hue, saturation, brightness);
        int colorR = color.getRed();
        int colorG = color.getGreen();
        int colorB = color.getBlue();
        int colorA = 192;

        matrixStack.rotate(new Quaternion(xRotation, 0, 0, true));
        // Shift y down so that sprite is rendered with the center of the image along the X axis
        builder.pos(positionMatrix, x + 0, y - 0.5f * amplitude, z).tex(sprite.getMinU(), sprite.getMinV()).color(colorR, colorG, colorB, colorA).endVertex();
        builder.pos(positionMatrix, x + distance, y - 0.3f * amplitude, z).tex(sprite.getMaxU(), sprite.getMinV()).color(colorR, colorG, colorB, colorA).endVertex();
        builder.pos(positionMatrix, x + distance, y + 0.3f * amplitude, z).tex(sprite.getMaxU(), sprite.getMaxV()).color(colorR, colorG, colorB, colorA).endVertex();
        builder.pos(positionMatrix, x + 0, y + 0.5f * amplitude, z).tex(sprite.getMinU(), sprite.getMaxV()).color(colorR, colorG, colorB, colorA).endVertex();
        matrixStack.pop();
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
        matrixStack.push();
        matrixStack.translate(0.5, 0.5, 0.5);

        /*
         * To make an animated link connection, we render a sine wave. The animation involves changing the
         * amplitude of the wave so that the begin and end points stay fixed. For one cycle, the amplitude changes
         * from +1 to -1. Then, a second cycle does the inverse of that, thus ending up at the begin state once
         * both cycles are complete.
         */
        int ticksPerCycle = 100;
        int cycle = (int)(Minecraft.getInstance().world.getGameTime() % (ticksPerCycle * 2));
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

        matrixStack.pop();
    }

    private void renderSineWave(MatrixStack matrixStack, IRenderTypeBuffer buffer, Quaternion rotation, float scaleX, float amplitudeFactor, Color color) {
        // Render a sine wave using Multiple line thicknesses and alpha values
        //int alpha = (int)((1 - Math.abs(progress - 0.5)) * 32 + 16);
        int alpha = 64;
        int[] colorInts = {color.getRed(), color.getGreen(), color.getBlue(), alpha};

        matrixStack.push();
        matrixStack.rotate(rotation);

        Matrix4f positionMatrix = matrixStack.getLast().getMatrix();
        IVertexBuilder builder;
        builder = buffer.getBuffer(RainbowBeamRenderType.BEAM_LINE_THICK);
        renderSineWaveSegments(positionMatrix, builder, colorInts, scaleX, 0, amplitudeFactor);
        builder = buffer.getBuffer(RainbowBeamRenderType.BEAM_LINE_THIN);
        colorInts[3] = alpha * 2;
        renderSineWaveSegments(positionMatrix, builder, colorInts, scaleX, 0, amplitudeFactor);

        matrixStack.pop();
    }

    private void renderSineWaveSegments(Matrix4f positionMatrix, IVertexBuilder builder, int[] color, float scaleX, int animationOffset, float amplitudeFactor) {
        // Render a sine wave using line segments
        for (int i = 0; i < sinPrecalculated.size(); i++) {
            int idxY = (i + animationOffset) % sinPrecalculated.size();
            int idxX = i;
            float x = sinPrecalculated.get(idxX).x * scaleX;
            float y = sinPrecalculated.get(idxY).y * amplitudeFactor;
            float z = 0;
            builder.pos(positionMatrix, x, y, z).color(color[0], color[1], color[2], color[3]).endVertex();
            if (i !=0 && i < sinPrecalculated.size() - 1) {
                builder.pos(positionMatrix, x, y, z).color(color[0], color[1], color[2], color[3]).endVertex();
            }
        }

        builder.endVertex();
    }

    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v) {
        // The order of the calls is important and is
        renderer.pos(stack.getLast().getMatrix(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .tex(u, v)
                .lightmap(0, 240) // Override lighting
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

        matrixStack.push();
        matrixStack.translate(-startPos.getX(), -startPos.getY(), -startPos.getZ());
        matrixStack.translate(0.5f, 0.5f, 0.5f);
        Matrix4f positionMatrix = matrixStack.getLast().getMatrix();
        IVertexBuilder builder = buffer.getBuffer(RainbowBeamRenderType.BEAM_LINE_THICK);
        builder.pos(positionMatrix, startPos.getX(), startPos.getY(), startPos.getZ())
                .color(colors[0], colors[1], colors[2], alpha)
                .endVertex();
        builder.pos(positionMatrix, endPos.getX(), endPos.getY(), endPos.getZ())
                .color(colors[0], colors[1], colors[2], alpha)
                .endVertex();

        IVertexBuilder builder2 = buffer.getBuffer(RainbowBeamRenderType.BEAM_LINE_THIN);
        builder2.pos(positionMatrix, startPos.getX(), startPos.getY(), startPos.getZ())
                .color(colors[0], colors[1], colors[2], alpha * 2)
                .endVertex();
        builder2.pos(positionMatrix, endPos.getX(), endPos.getY(), endPos.getZ())
                .color(colors[0], colors[1], colors[2], alpha * 2)
                .endVertex();
        matrixStack.pop();
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

    public static final RenderType BEAM_LINE_THICK = RenderType.makeType("beam_line_thick",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.getBuilder().line(THICK_LINE)
                    .layer(RenderState.field_239235_M_)
                    .transparency(TransparencyState.LIGHTNING_TRANSPARENCY)
                    .lightmap(RenderState.LIGHTMAP_DISABLED)
                    .texture(RenderState.NO_TEXTURE)
                    .writeMask(RenderState.COLOR_DEPTH_WRITE)
                    .build(false)
    );

    public static final RenderType BEAM_LINE_THIN = makeType("beam_line_thin",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.getBuilder().line(THIN_LINE)
                    .layer(RenderState.field_239235_M_)
                    .transparency(RenderState.TransparencyState.TRANSLUCENT_TRANSPARENCY)
                    .lightmap(RenderState.LIGHTMAP_DISABLED)
                    .texture(RenderState.NO_TEXTURE)
                    .writeMask(RenderState.COLOR_DEPTH_WRITE)
                    .build(false)
    );

    // RenderType for rendering a texture in 2D in the world. Note that the source here is not the texture, but the ATLAS containing the texture
    public static final RenderType BEAM_TEXTURED = makeType("beam_textured",
            DefaultVertexFormats.POSITION_TEX_COLOR, 7, 262144,
            RenderType.State.getBuilder()
                    .texture(new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS_TEXTURE, false, false))
                    .transparency(TransparencyState.TRANSLUCENT_TRANSPARENCY)
                    .cull(RenderState.CULL_DISABLED)
                    .writeMask(RenderState.COLOR_WRITE)
                    .build(false)
    );
}