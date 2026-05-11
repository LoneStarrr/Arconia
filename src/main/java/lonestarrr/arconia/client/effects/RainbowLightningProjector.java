package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class RainbowLightningProjector {

  /**
   * Mirrors the legacy {@code RENDERTYPE_LIGHTNING_SHADER + LIGHTNING_TRANSPARENCY + NO_CULL +
   * COLOR_WRITE} combo. Same shader as vanilla {@link RenderPipelines#LIGHTNING}, but with cull
   * disabled and {@code TRIANGLES} mode for the sword-blade beam geometry. Registered via {@code
   * RegisterRenderPipelinesEvent} in {@code ClientProxy}.
   */
  public static final RenderPipeline BEAM_TRIANGLE_PIPELINE =
      RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
          .withLocation(Identifier.fromNamespaceAndPath(Arconia.MOD_ID, "pipeline/beam_triangle"))
          .withVertexShader("core/rendertype_lightning")
          .withFragmentShader("core/rendertype_lightning")
          .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
          .withCull(false)
          .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
          .build();

  public static final RenderType BEAM_TRIANGLE =
      RenderType.create(
          "beam_triangle",
          RenderSetup.builder(BEAM_TRIANGLE_PIPELINE)
              .bufferSize(32768)
              .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
              .createRenderSetup());

  /**
   * Render a lightning effect with a given block position at the center. Similar to end dragon
   * death effect, but with cycling rainbow colors.
   */
  public static void renderRainbowLighting(
      Random random,
      float beamLength,
      int beamCount,
      PoseStack poseStack,
      SubmitNodeCollector nodeCollector) {
    renderRainbowLighting(random, beamLength, beamCount, poseStack, nodeCollector, null);
  }

  /**
   * Render a lightning effect
   *
   * @param rand Random number generator. Successive calls for the same animation should pass in a
   *     new Random with identical seed
   * @param fixedColor The color to use, or null if it should cycle through all rainbow colors
   */
  public static void renderRainbowLighting(
      Random rand,
      float beamLength,
      int beamCount,
      PoseStack poseStack,
      SubmitNodeCollector nodeCollector,
      Color fixedColor) {
    poseStack.pushPose();

    final Vector3f[] rotationVectors =
        new Vector3f[] {
          new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1),
        };

    // Vertices for a single  beam - w is used for alpha
    List<Vector4f> vertices = new ArrayList<>(10);

    long ticks = Minecraft.getInstance().level.getGameTime();

    for (int i = 0; i < beamCount; i++) {
      vertices.clear();
      poseStack.pushPose();
      for (Vector3f vec : rotationVectors) {
        // Rotate every beam along every cardinal axis - note the use of a random generated with a
        // fixed seed makes sure these initial angles are
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
      float bl1 = 0.2f * bl; // length of first segment of the blade
      float bl2 = 1.0f * bl; // length of second segment of the blade
      float bw1 = 0.05f * bl; // width of first segment of the blade at its widest point
      float bw2 = 0.15f * bl; // width of second segment of the blade
      vertices.add(new Vector4f(0, 0, 0, a));
      vertices.add(new Vector4f(0f, bw1, bl1, a));
      vertices.add(new Vector4f(0f, bw2, bl2, a));
      vertices.add(new Vector4f(0, 0, 0, a));
      vertices.add(new Vector4f(0f, bw2, bl2, a));
      vertices.add(new Vector4f(0f, -bw2, bl2, a));
      vertices.add(new Vector4f(0, 0, 0, a));
      vertices.add(new Vector4f(0f, -bw1, bl1, a));
      vertices.add(new Vector4f(0f, -bw2, bl2, a));
      vertices.add(new Vector4f(0, 0, 0, a));

      // Every beam cycles through the colors of the rainbow, with some random offset
      final Color beamColor;
      if (fixedColor == null) {
        float hue = rand.nextFloat() + (ticks % 100) / 100f;
        beamColor = Color.getHSBColor(hue % 1f, 1f, 1f);
      } else {
        beamColor = fixedColor;
      }
      final float colorR = beamColor.getRed() / 255f;
      final float colorG = beamColor.getGreen() / 255f;
      final float colorB = beamColor.getBlue() / 255f;

      final List<Vector4f> beamVertices = new ArrayList<>(vertices);
      nodeCollector.submitCustomGeometry(
          poseStack,
          BEAM_TRIANGLE,
          (pose, builder) -> {
            Matrix4f positionMatrix = pose.pose();
            for (Vector4f vertex : beamVertices) {
              float alpha = vertex.w();
              builder
                  .addVertex(positionMatrix, vertex.x(), vertex.y(), vertex.z())
                  .setColor(colorR, colorG, colorB, alpha);
            }
          });
      poseStack.popPose();
    }

    poseStack.popPose();
  }
}
