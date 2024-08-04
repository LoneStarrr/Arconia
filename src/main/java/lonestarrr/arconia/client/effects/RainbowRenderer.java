package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;

/**
 * Render a rainbow between two points, with 1 or more colors
 */
public class RainbowRenderer {
    public static void renderRainbow(float diameter, PoseStack poseStack, MultiBufferSource buffer) {
        float radiusOuter = diameter / 2;
        float radiusInner = radiusOuter * 0.8f;
        float alpha = 0.5f;
        Color color = Color.RED;
        float colorR = color.getRed() / 255f;
        float colorG = color.getGreen() / 255f;
        float colorB = color.getBlue() / 255f;

        poseStack.pushPose();

        VertexConsumer builder = buffer.getBuffer(RainbowSegmentRenderType.RAINBOW_SEGMENT);
        Matrix4f positionMatrix = poseStack.last().pose();


        // TODO this should obviously be calculated only once per rendered rainbow
        int numEdges = 50;
        float z = 0;
        float innerXOffset = radiusOuter - radiusInner;
        List<Vector3f> outerArch = getRainbowArchVertices(radiusOuter, numEdges, z, -radiusOuter);
        List<Vector3f> innerArch = getRainbowArchVertices(radiusInner, numEdges, z, -radiusOuter + innerXOffset);
        // Draw the rainbow in quad segments, as making a single large polygon would make it a concave (complex) polygon that cannot be rendered as-is

        for (int i = 0; i < numEdges - 1; i++) {
            Vector3f[] corners = {outerArch.get(i), outerArch.get(i + 1), innerArch.get(i +1), innerArch.get(i)};
            for (Vector3f v: corners) {
                builder.vertex(positionMatrix, v.x(), v.y(), v.z()).color(colorR, colorG, colorB, alpha).endVertex();
            }
        }

        // FIXME How am I supposed to indicate that I'm done drawing? Closing the polygon? Nope. This here works, but something
        // tells me I am not supposed to be doing this this way.
        RainbowSegmentRenderType.RAINBOW_SEGMENT.end((BufferBuilder)builder, VertexSorting.DISTANCE_TO_ORIGIN);
        ((BufferBuilder)builder).begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        poseStack.popPose();
    }

    /**
     *
     * @param radius Radius of the circle
     * @param z Z coordinate for each vertex
     * @param xOffset X offset for each vertex
     * @return A list of vertices that will draw a 2D rainbow arch originating at (0,0)
     */
    private static List<Vector3f> getRainbowArchVertices(final float radius, final int numEdges, final float z, final float xOffset) {
        // Formula for a circle: x^2 + y^2 = r^2 =>  y^2  = r^2 - x^2  =>  y = sqrt(r^2-x^2)
        // Circle starts with highest point at y=0, so compensate by offsetting with r: y = sqrt(r^2 - (x - r)^2)
        final int numVertices = numEdges + 1;
        List<Vector3f> result = new ArrayList<>(numVertices);

        final float diameter = 2 * radius;
        final float xIncrement = diameter / numEdges;
        float x = 0;
        float y;
        float rSquared = radius * radius;

        for (int i = 0; i < numVertices - 1; i++) {
            y = (float)Math.sqrt(rSquared - ((x - radius) * (x - radius)));
            result.add(new Vector3f(x + xOffset, y, z));
            x += xIncrement;
        }
        result.add(new Vector3f(x + xOffset, 0, z));
        return result;
    }
}

/**
 * Render type for rendering the rainbow in quad segments
 */
@OnlyIn(Dist.CLIENT)
class RainbowSegmentRenderType extends RenderType {
    // Default constructor to satisfy compiler
    public RainbowSegmentRenderType(
            String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_,
            Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    public static final RenderType RAINBOW_SEGMENT = create("rainbow_segment",
           DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 32768, false, false,
           RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TransparencyStateShard.LIGHTNING_TRANSPARENCY)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
    );
}
