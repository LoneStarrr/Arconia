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
import lonestarrr.arconia.common.Arconia;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Render a rainbow between two points, with 1 or more colors */
public class RainbowRenderer {
  /**
   * Replaces the legacy {@code POSITION_COLOR_SHADER + LIGHTNING_TRANSPARENCY + NO_CULL +
   * COLOR_WRITE} combo from 1.21.4. The pipeline is registered via {@code
   * RegisterRenderPipelinesEvent} in {@code ClientProxy}; without that registration its shader
   * files would never get loaded.
   */
  public static final RenderPipeline RAINBOW_SEGMENT_PIPELINE =
      RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
          .withLocation(Identifier.fromNamespaceAndPath(Arconia.MOD_ID, "pipeline/rainbow_segment"))
          .withVertexShader("core/position_color")
          .withFragmentShader("core/position_color")
          .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
          .withCull(false)
          .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
          .build();

  public static final RenderType RAINBOW_SEGMENT =
      RenderType.create(
          "rainbow_segment",
          RenderSetup.builder(RAINBOW_SEGMENT_PIPELINE)
              .bufferSize(32768)
              .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
              .createRenderSetup());

  public static void renderRainbow(
      float diameter, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
    float radiusOuter = diameter / 2;
    float radiusInner = radiusOuter * 0.8f;
    final float alpha = 0.5f;
    Color color = Color.RED;
    final float colorR = color.getRed() / 255f;
    final float colorG = color.getGreen() / 255f;
    final float colorB = color.getBlue() / 255f;

    poseStack.pushPose();

    // TODO this should obviously be calculated only once per rendered rainbow
    int numEdges = 50;
    float z = 0;
    float innerXOffset = radiusOuter - radiusInner;
    final List<Vector3f> outerArch = getRainbowArchVertices(radiusOuter, numEdges, z, -radiusOuter);
    final List<Vector3f> innerArch =
        getRainbowArchVertices(radiusInner, numEdges, z, -radiusOuter + innerXOffset);

    final int finalNumEdges = numEdges;
    nodeCollector.submitCustomGeometry(
        poseStack,
        RAINBOW_SEGMENT,
        (pose, builder) -> {
          Matrix4f positionMatrix = pose.pose();
          // Draw the rainbow in quad segments, as making a single large polygon would make it a
          // concave (complex) polygon that cannot be rendered as-is
          for (int i = 0; i < finalNumEdges - 1; i++) {
            Vector3f[] corners = {
              outerArch.get(i), outerArch.get(i + 1), innerArch.get(i + 1), innerArch.get(i)
            };
            for (Vector3f v : corners) {
              builder
                  .addVertex(positionMatrix, v.x(), v.y(), v.z())
                  .setColor(colorR, colorG, colorB, alpha);
            }
          }
        });

    poseStack.popPose();
  }

  /**
   * @param radius Radius of the circle
   * @param z Z coordinate for each vertex
   * @param xOffset X offset for each vertex
   * @return A list of vertices that will draw a 2D rainbow arch originating at (0,0)
   */
  private static List<Vector3f> getRainbowArchVertices(
      final float radius, final int numEdges, final float z, final float xOffset) {
    // Formula for a circle: x^2 + y^2 = r^2 =>  y^2  = r^2 - x^2  =>  y = sqrt(r^2-x^2)
    // Circle starts with highest point at y=0, so compensate by offsetting with r: y = sqrt(r^2 -
    // (x - r)^2)
    final int numVertices = numEdges + 1;
    List<Vector3f> result = new ArrayList<>(numVertices);

    final float diameter = 2 * radius;
    final float xIncrement = diameter / numEdges;
    float x = 0;
    float y;
    float rSquared = radius * radius;

    for (int i = 0; i < numVertices - 1; i++) {
      y = (float) Math.sqrt(rSquared - ((x - radius) * (x - radius)));
      result.add(new Vector3f(x + xOffset, y, z));
      x += xIncrement;
    }
    result.add(new Vector3f(x + xOffset, 0, z));
    return result;
  }
}
