package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.OptionalDouble;
import java.util.Random;

public class RainbowLightningProjector {
    /**
     * Render a rainbow lightning effect with a given block position at the center. Similar to end dragon death effect, but with colors.
     *
     * @param pos
     * @param beamLength
     * @param beamCount
     * @param matrixStack
     * @param buffer
     */
    public static void renderRainbowLighting(BlockPos pos, float beamLength, int beamCount, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        matrixStack.push();
        matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
        matrixStack.translate(0.5f, 0.5f, 0.5f);

        final Vector3f[] rotationVectors = new Vector3f[] {
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0),
                new Vector3f(0, 0, 1),
        };

        // Seed rng with a fixed seed to procedurally generate consistent angles etc without having to keep state
        Random rand = new Random(pos.toLong());

        // Vertices for a single  beam - w is used for alpha
        Vector4f[] vertices = new Vector4f[6];

        for (int i = 0; i < 6; i++) {
            vertices[i] = new Vector4f();
        }

        IVertexBuilder builder = buffer.getBuffer(LightningRenderType.BEAM_TRIANGLE);
        long ticks = Minecraft.getInstance().world.getGameTime();

        for (int i = 0; i < beamCount; i++) {
            for (Vector3f vec : rotationVectors) {
                // Rotate every beam along every cardinal axis - note the use of a random generated with a fixed seed makes sure these initial angles are
                // consistent across render calls, so we don't need to store the angles between calls.
                float speedFactor = 3f; // higher == slower
                float angle = (float) ticks / speedFactor * rand.nextFloat();
                matrixStack.rotate(new Quaternion(vec, angle, true));
            }

            // Draw a sword-like shape. w = alpha
            float beamLengthRnd = beamLength + 2 * rand.nextFloat();
            vertices[0].set(0, 0, 0, 0.9f);
            vertices[1].set(0f, 0.10f * beamLengthRnd, 0.4f * beamLengthRnd, 0.4f);
            vertices[2].set(0f, 0.15f * beamLengthRnd, beamLengthRnd, 0.1f);
            vertices[3].set(0f, 0, 1.01f * beamLengthRnd, 0.02f);
            vertices[4].set(0f, -0.15f * beamLengthRnd, beamLengthRnd, 0.1f);
            vertices[5].set(0f, -0.10f * beamLengthRnd, 0.4f * beamLengthRnd, 0.4f);

            // Every beam cycles through the colors of the rainbow, with some random offset
            float hue = rand.nextFloat() + (ticks % 256) / 256f;
            Color color = Color.getHSBColor(hue, 1f, 1f);
            float colorR = color.getRed() / 255f;
            float colorG = color.getGreen() / 255f;
            float colorB = color.getBlue() / 255f;

//            if (builder instanceof BufferBuilder) {
//                BufferBuilder bBuilder = (BufferBuilder)builder;
//                bBuilder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);
//            }

            Matrix4f positionMatrix = matrixStack.getLast().getMatrix();
            for (int vix = 0; vix < vertices.length; vix++) {
                Vector4f vertex = vertices[vix];
                float alpha = vertex.getW();
//                alpha = 1;
                builder.pos(positionMatrix, vertex.getX(), vertex.getY(), vertex.getZ()).color(colorR, colorG, colorB, alpha).endVertex();
            }
            // Close the polygon
            Vector4f vertex = vertices[0];
            builder.pos(positionMatrix, vertex.getX(), vertex.getY(), vertex.getZ()).color(colorR, colorG, colorB, vertex.getW()).endVertex();

            // FIXME How am I supposed to indicate with mode GL_POLYGON that I'm done drawing? Closing the polygon? Nope. This here works, but something
            // tells me I am not supposed to be doing this this way..should I just forego the builder and directly call methods on buffer!?
            LightningRenderType.BEAM_TRIANGLE.finish((BufferBuilder)builder, 0, 0, 0);
            ((BufferBuilder)builder).begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);
        }

        matrixStack.pop();

    }
}

/**
 * Render types for rendering the beam between tree and crate - inspiration gleaned from desht's ModularRouters
 */
@OnlyIn(Dist.CLIENT)
class LightningRenderType extends RenderType {
    public LightningRenderType(
            String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn,
            boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    public static final RenderType BEAM_TRIANGLE = makeType("beam_triangle",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_POLYGON, 32768,
            RenderType.State.getBuilder()
                    .layer(RenderState.field_239235_M_)
                    .alpha(RenderState.ZERO_ALPHA)
                    .transparency(TransparencyState.LIGHTNING_TRANSPARENCY)
                    .lightmap(RenderState.LIGHTMAP_DISABLED)
                    .shadeModel(RenderState.SHADE_ENABLED)
                    .texture(RenderState.NO_TEXTURE)
                    .writeMask(RenderState.COLOR_WRITE)
                    .cull(RenderState.CULL_DISABLED)
                    .build(false)
    );
}