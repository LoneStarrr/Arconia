package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jdk.internal.org.objectweb.asm.tree.LineNumberNode;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import lonestarrr.arconia.common.core.helper.VectorHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Render a rainbow between two points, with 1 or more colors
 */
public class RainbowRenderer {
    public static void renderRainbow(Vector3d origin, Vector3d destination, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        // for the test-integration from pot item transfer: matrix has already been translated to compensate for player pov
        float radiusOuter = (float)origin.distanceTo(destination);
        float radiusInner = radiusOuter * 0.8f;
        Quaternion rotation = VectorHelper.getRotation(origin, destination);
        // TODO this should obviously be calculated only once per rendered rainbow
        List<Vector3f> polygon = getRainbowPolygon(radiusOuter, radiusInner, 10, 0);
        float alpha = 0.5f;
        Color color = Color.RED;
        float colorR = color.getRed() / 255f;
        float colorG = color.getGreen() / 255f;
        float colorB = color.getBlue() / 255f;

        matrixStack.push();
        matrixStack.translate(origin.x, origin.y, origin.z);
        matrixStack.rotate(rotation);

        IVertexBuilder builder = buffer.getBuffer(LightningRenderType.BEAM_TRIANGLE); //TODO define one here
        Matrix4f positionMatrix = matrixStack.getLast().getMatrix();

        for (Vector3f v: polygon) {
            builder.pos(positionMatrix, v.getX(), v.getY(), v.getZ()).color(colorR, colorG, colorB, alpha).endVertex();
        }

        // FIXME How am I supposed to indicate with mode GL_POLYGON that I'm done drawing? Closing the polygon? Nope. This here works, but something
        // tells me I am not supposed to be doing this this way..should I just forego the builder and directly call methods on buffer!?
        LightningRenderType.BEAM_TRIANGLE.finish((BufferBuilder)builder, 0, 0, 0);
        ((BufferBuilder)builder).begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);

        matrixStack.pop();
    }

    /**
     *
     * @param radius Radius of the circle
     * @param z Z coordinate for each vertex
     * @return A list of vertices that will draw a 2D rainbow arch originating at (0,0)
     */
    private static List<Vector3f> getRainbowArchVertices(final float radius, final int numEdges, final float z) {
        // Formula for a circle: x^2 + y^2 = r^2 =>  y^2  = r^2 - x^2  =>  y = sqrt(r^2-x^2)
        // Circle starts with highest point at y=0, so compensate by offsetting with r: y = sqrt(r^2 - (x - r)^2)
        List<Vector3f> result = new ArrayList<Vector3f>(numEdges + 1);

        final float xIncrement = radius / numEdges;
        float x = 0;
        float y;
        float rSquared = radius * radius;
        while (x <= radius) {
            y = (float)Math.sqrt(rSquared - ((x - radius) * (x - radius)));
            result.add(new Vector3f(x, y, z));
            x += xIncrement;
        }
        return result;
    }

    /**
     *
     * @param radiusOuter
     * @param radiusInner Inner rainbow radius, must be smaller than outer
     * @param numEdgesOuter
     * @param z
     * @return A closed polygon for a 2D rainbow
     */
    public static List<Vector3f> getRainbowPolygon(final float radiusOuter, final float radiusInner, final int numEdgesOuter, final float z) {
        if (radiusInner >= radiusOuter) {
            throw new ValueException(("Inner radius must be smaller than outer radius"));
        }
        final int numEdgesInner = (int)(radiusInner / radiusOuter * numEdgesOuter);
        // polygon: outer arch vertices, edge connecting to reversed inner arch vertices, line connecting to starting point
        List<Vector3f> outerArch = getRainbowArchVertices(radiusOuter, numEdgesOuter, z);
        List<Vector3f> innerArch = getRainbowArchVertices(radiusInner, numEdgesInner, z);
        Collections.reverse(innerArch);
        List<Vector3f> result = outerArch;
        // edge from right side of outer arch to right ride of inner arch
//        result.add(new Vector3f(2 * radiusOuter, 0, z));
        result.add(new Vector3f(2 * radiusOuter - (radiusOuter - radiusInner), 0, z));
        // inner arch edges, reversed already, offset X by difference in radius sizes
        innerArch.forEach(v -> result.add(new Vector3f(v.getX() + (radiusOuter - radiusInner), v.getY(), z)));
        // edge from left side of outer arch to left ride of inner arch
        result.add(new Vector3f(radiusOuter - radiusInner, 0, z));
        result.add(new Vector3f(0, 0, z));
        return result;
    }
}
