package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RainbowLightningProjector {

    /**
     * Mirrors the legacy {@code RENDERTYPE_LIGHTNING_SHADER + LIGHTNING_TRANSPARENCY + NO_CULL + COLOR_WRITE}
     * combo. Same shader as vanilla {@link RenderPipelines#LIGHTNING}, but with cull disabled and
     * {@code TRIANGLES} mode for the sword-blade beam geometry. Registered via
     * {@code RegisterRenderPipelinesEvent} in {@code ClientProxy}.
     */
    public static final RenderPipeline BEAM_TRIANGLE_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_FOG_SNIPPET)
            .withLocation(ResourceLocation.fromNamespaceAndPath(Arconia.MOD_ID, "pipeline/beam_triangle"))
            .withVertexShader("core/rendertype_lightning")
            .withFragmentShader("core/rendertype_lightning")
            .withBlend(BlendFunction.LIGHTNING)
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .build();

    public static final RenderType BEAM_TRIANGLE = RenderType.create(
            "beam_triangle",
            32768,
            BEAM_TRIANGLE_PIPELINE,
            RenderType.CompositeState.builder()
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .createCompositeState(false)
    );

    /**
     * Render a lightning effect with a given block position at the center. Similar to end dragon death effect, but with cycling rainbow colors.
     *
     * @param random
     * @param beamLength
     * @param beamCount
     * @param poseStack
     * @param buffer
     */
    public static void renderRainbowLighting(Random random, float beamLength, int beamCount, PoseStack poseStack, MultiBufferSource buffer) {
        renderRainbowLighting(random, beamLength, beamCount, poseStack, buffer, null);
    }

    /**
     * Render a lightning effect
     *
     * @param rand Random number generator. Successive calls for the same animation should pass in a new Random with identical seed
     * @param beamLength
     * @param beamCount
     * @param poseStack
     * @param buffer
     * @param fixedColor The color to use, or null if it should cycle through all rainbow colors
     */
    public static void renderRainbowLighting(Random rand, float beamLength, int beamCount, PoseStack poseStack, MultiBufferSource buffer, Color fixedColor) {
        poseStack.pushPose();

        final Vector3f[] rotationVectors = new Vector3f[] {
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0),
                new Vector3f(0, 0, 1),
        };

        // Vertices for a single  beam - w is used for alpha
        List<Vector4f> vertices = new ArrayList<>(10);

        VertexConsumer builder = buffer.getBuffer(BEAM_TRIANGLE);
        long ticks = Minecraft.getInstance().level.getGameTime();
        Color color = fixedColor;

        for (int i = 0; i < beamCount; i++) {
            poseStack.pushPose();
            for (Vector3f vec : rotationVectors) {
                // Rotate every beam along every cardinal axis - note the use of a random generated with a fixed seed makes sure these initial angles are
                // consistent across render calls, so we don't need to store the angles between calls.
                float speedFactor = 0.5f; // higher == slower
                // ticks increases always, quaternion wraps it by applying sin() to it
                float angle = (float) ticks / speedFactor * rand.nextFloat();
                poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(vec, angle));
            }

            // Draw a sword-like shape. w = alpha
            float bl = beamLength + rand.nextFloat() * beamLength;
            float a = 0.5f; // alpha

            // Draw three triangles, together forming 1 'sword blade' like shape
            // We start with the 'tip' of the blade, this leads to the widest section, then back to near the center of the
            // blade but not quite. The blade is cut, length wise, in 3 segments (triangles) because this render type makes
            // us (there's probably something smarter out there, but I wanted to get something working!)
            float bl1 = 0.2f * bl; //length of first segment of the blade
            float bl2 = 1.0f * bl; //length of second segment of the blade
            float bw1 = 0.05f * bl; //width of first segment of the blade at its widest point
            float bw2 = 0.15f * bl; // width of second segment of the blade
            vertices.add(new Vector4f(0, 0, 0, a));
            vertices.add(new Vector4f(0f,  bw1, bl1, a));
            vertices.add(new Vector4f(0f,  bw2, bl2, a));
            vertices.add(new Vector4f(0, 0, 0, a));
            vertices.add(new Vector4f(0f,  bw2, bl2, a));
            vertices.add(new Vector4f(0f, -bw2, bl2, a));
            vertices.add(new Vector4f(0, 0, 0, a));
            vertices.add(new Vector4f(0f, -bw1, bl1, a));
            vertices.add(new Vector4f(0f, -bw2, bl2, a));
            vertices.add(new Vector4f(0, 0, 0, a));

                        // Every beam cycles through the colors of the rainbow, with some random offset
            if (fixedColor == null) {
                float hue = rand.nextFloat() + (ticks % 100) / 100f;
                color = Color.getHSBColor(hue % 1f, 1f, 1f);
            }
            float colorR = color.getRed() / 255f;
            float colorG = color.getGreen() / 255f;
            float colorB = color.getBlue() / 255f;

            Matrix4f positionMatrix = poseStack.last().pose();
            for (Vector4f vertex: vertices) {
                float alpha = vertex.w();
                builder.addVertex(positionMatrix, vertex.x(), vertex.y(), vertex.z()).setColor(colorR, colorG, colorB, alpha);
            }
            poseStack.popPose();
        }

        poseStack.popPose();

    }
}
