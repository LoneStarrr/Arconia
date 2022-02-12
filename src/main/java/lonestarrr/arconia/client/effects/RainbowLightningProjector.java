package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Random;

public class RainbowLightningProjector {

    /**
     * Render a lightning effect with a given block position at the center. Similar to end dragon death effect, but with cycling rainbow colors.
     *
     * @param pos
     * @param beamLength
     * @param beamCount
     * @param matrixStack
     * @param buffer
     */
    public static void renderRainbowLighting(BlockPos pos, float beamLength, int beamCount, PoseStack matrixStack, MultiBufferSource buffer) {
        renderRainbowLighting(pos, beamLength, beamCount, matrixStack, buffer, null);
    }

    /**
     * Render a lightning effect using a single color
     *
     * @param pos
     * @param beamLength
     * @param beamCount
     * @param matrixStack
     * @param buffer
     * @param color The color to use, or null if it should cycle through all rainbow colors
     */
    public static void renderRainbowLighting(BlockPos pos, float beamLength, int beamCount, PoseStack matrixStack, MultiBufferSource buffer, Color color) {
        matrixStack.pushPose();
        matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
        matrixStack.translate(0.5f, 0.5f, 0.5f);

        final Vector3f[] rotationVectors = new Vector3f[] {
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0),
                new Vector3f(0, 0, 1),
        };

        // Seed rng with a fixed seed to procedurally generate consistent angles etc without having to keep state
        Random rand = new Random(pos.asLong());

        // Vertices for a single  beam - w is used for alpha
        Vector4f[] vertices = new Vector4f[6];

        for (int i = 0; i < 6; i++) {
            vertices[i] = new Vector4f();
        }

        VertexConsumer builder = buffer.getBuffer(LightningRenderType.BEAM_TRIANGLE);
        long ticks = Minecraft.getInstance().level.getGameTime();

        for (int i = 0; i < beamCount; i++) {
            for (Vector3f vec : rotationVectors) {
                // Rotate every beam along every cardinal axis - note the use of a random generated with a fixed seed makes sure these initial angles are
                // consistent across render calls, so we don't need to store the angles between calls.
                float speedFactor = 3f; // higher == slower
                float angle = (float) ticks / speedFactor * rand.nextFloat();
                matrixStack.mulPose(new Quaternion(vec, angle, true));
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
            if (color == null) {
                float hue = rand.nextFloat() + (ticks % 256) / 256f;
                color = Color.getHSBColor(hue, 1f, 1f);
            }
            float colorR = color.getRed() / 255f;
            float colorG = color.getGreen() / 255f;
            float colorB = color.getBlue() / 255f;

//            if (builder instanceof BufferBuilder) {
//                BufferBuilder bBuilder = (BufferBuilder)builder;
//                bBuilder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);
//            }

            Matrix4f positionMatrix = matrixStack.last().pose();
            for (int vix = 0; vix < vertices.length; vix++) {
                Vector4f vertex = vertices[vix];
                float alpha = vertex.w();
//                alpha = 1;
                builder.vertex(positionMatrix, vertex.x(), vertex.y(), vertex.z()).color(colorR, colorG, colorB, alpha).endVertex();
            }
            // Close the polygon
            Vector4f vertex = vertices[0];
            builder.vertex(positionMatrix, vertex.x(), vertex.y(), vertex.z()).color(colorR, colorG, colorB, vertex.w()).endVertex();

            // FIXME How am I supposed to indicate with mode GL_POLYGON that I'm done drawing? Closing the polygon? Nope. This here works, but something
            // tells me I am not supposed to be doing this this way..should I just forego the builder and directly call methods on buffer!?
            LightningRenderType.BEAM_TRIANGLE.end((BufferBuilder)builder, 0, 0, 0);
            ((BufferBuilder)builder).begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        }

        matrixStack.popPose();

    }
}

/**
 * Render types for rendering the beam between tree and crate - inspiration gleaned from desht's ModularRouters
 */
@OnlyIn(Dist.CLIENT)
class LightningRenderType extends RenderType {
    public LightningRenderType(
            String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_,
            Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    public static final RenderType BEAM_TRIANGLE = create("beam_triangle",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, 32768, false, false,
            RenderType.CompositeState.builder()
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TransparencyStateShard.LIGHTNING_TRANSPARENCY)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
    );
}