package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import lonestarrr.arconia.common.core.helper.VectorHelper;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.RenderState.TransparencyState;

/**
 * Render a rainbow between two points, with 1 or more colors
 */
public class RainbowRenderer {
    public static void renderRainbow(Vector3d origin, Vector3d destination, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        // for the test-integration from pot item transfer: matrix has already been translated to compensate for player pov
        float radiusOuter = (float)origin.distanceTo(destination) / 2f;
        float radiusInner = radiusOuter * 0.8f;
        Quaternion rotation = VectorHelper.getRotation(origin, destination);
        // TODO This is a concave (complex, non-simple) polygon - that needs special rendering
        float alpha = 0.5f;
        Color color = Color.RED;
        float colorR = color.getRed() / 255f;
        float colorG = color.getGreen() / 255f;
        float colorB = color.getBlue() / 255f;

        matrixStack.pushPose();
        matrixStack.translate(origin.x, origin.y, origin.z);
        matrixStack.mulPose(rotation);

        IVertexBuilder builder = buffer.getBuffer(RainbowSegmentRenderType.RAINBOW_SEGMENT);
        Matrix4f positionMatrix = matrixStack.last().pose();


        // TODO this should obviously be calculated only once per rendered rainbow
        int numEdges = 50;
        float z = 0;
        float innerXOffset = radiusOuter - radiusInner;
        List<Vector3f> outerArch = getRainbowArchVertices(radiusOuter, numEdges, z, 0);
        List<Vector3f> innerArch = getRainbowArchVertices(radiusInner, numEdges, z, innerXOffset);
        // Draw the rainbow in quad segments, as making a single large polygon would make it a concave (complex) polygon that cannot be rendered as-is

        for (int i = 0; i < numEdges - 1; i++) {
            Vector3f[] corners = {outerArch.get(i), outerArch.get(i + 1), innerArch.get(i +1), innerArch.get(i)};
            for (Vector3f v: corners) {
                builder.vertex(positionMatrix, v.x(), v.y(), v.z()).color(colorR, colorG, colorB, alpha).endVertex();
            }
        }

        // FIXME How am I supposed to indicate that I'm done drawing? Closing the polygon? Nope. This here works, but something
        // tells me I am not supposed to be doing this this way.
        RainbowSegmentRenderType.RAINBOW_SEGMENT.end((BufferBuilder)builder, 0, 0, 0);
        ((BufferBuilder)builder).begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        matrixStack.popPose();
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
        List<Vector3f> result = new ArrayList<Vector3f>(numVertices);

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
    public RainbowSegmentRenderType(
            String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn,
            boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    public static final RenderType RAINBOW_SEGMENT = create("rainbow_segment",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 32768,
            RenderType.State.builder()
                    .setLayeringState(RenderState.VIEW_OFFSET_Z_LAYERING)
                    .setAlphaState(RenderState.NO_ALPHA)
                    .setTransparencyState(TransparencyState.LIGHTNING_TRANSPARENCY)
                    .setLightmapState(RenderState.NO_LIGHTMAP)
                    .setShadeModelState(RenderState.SMOOTH_SHADE)
                    .setTextureState(RenderState.NO_TEXTURE)
                    .setWriteMaskState(RenderState.COLOR_WRITE)
                    .setCullState(RenderState.NO_CULL)
                    .createCompositeState(false)
    );
}