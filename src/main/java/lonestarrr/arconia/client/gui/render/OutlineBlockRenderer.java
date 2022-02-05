package lonestarrr.arconia.client.gui.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import lonestarrr.arconia.common.Arconia;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Renders block outlines
 */
public class OutlineBlockRenderer {
    private static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY;
    private static final IRenderTypeBuffer.Impl LINE_BUFFERS;
    private static Set<BlockPos> highlightedBlocks = null;
    public static final RenderType LINE_3_NO_DEPTH;
    public static final RenderType LINE_8_NO_DEPTH;

    static {
        // Gleaned from Botania's RenderHelper
        TRANSLUCENT_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null,
                "TRANSLUCENT_TRANSPARENCY");
        RenderState.LayerState projectionLayering = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "VIEW_OFFSET_Z_LAYERING");
        RenderState.WriteMaskState colorMask = new RenderState.WriteMaskState(true, false);
        RenderState.DepthTestState noDepth = new RenderState.DepthTestState("always", GL11.GL_ALWAYS);

        // https://github.com/Vazkii/Botania/blob/24715c509e47dfc32a80d7e94aeba6d84c022503/src/main/java/vazkii/botania/client/core/helper/RenderHelper.java
        // May want to enable depthTest() ?
        RenderType.State glState = RenderType.State.builder().setLineState(new RenderState.LineState(OptionalDouble.of(3))).setLayeringState(projectionLayering).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setWriteMaskState(colorMask).setDepthTestState(noDepth).createCompositeState(false);
        LINE_3_NO_DEPTH = RenderType.create(Arconia.MOD_ID + ":line_3_no_depth", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 128, glState);
        glState = RenderType.State.builder().setLineState(new RenderState.LineState(OptionalDouble.of(8))).setLayeringState(projectionLayering).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setWriteMaskState(colorMask).setDepthTestState(noDepth).createCompositeState(false);
        LINE_8_NO_DEPTH = RenderType.create(Arconia.MOD_ID + ":line_8_no_depth", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 128, glState);
        // https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/client/core/handler/BoundTileRenderer.java
        LINE_BUFFERS = IRenderTypeBuffer.immediateWithBuffers(Util.make(() -> {
            Map<RenderType, BufferBuilder> ret = new IdentityHashMap<>();
            ret.put(LINE_3_NO_DEPTH, new BufferBuilder(LINE_3_NO_DEPTH.bufferSize()));
            ret.put(LINE_8_NO_DEPTH, new BufferBuilder(LINE_8_NO_DEPTH.bufferSize()));
            return ret;
        }), Tessellator.getInstance().getBuilder());
    }

    /**
     * Renders a wireframe-like outline that will always be on top.
     * @param hue Outline hue
     */
    public static void renderOutline(@Nonnull final MatrixStack matrixStack, @Nonnull final BlockPos pos, float hue, RenderType lineType) {
        double renderPosX = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition().x();
        double renderPosY = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition().y();
        double renderPosZ = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition().z();

        matrixStack.pushPose();
        matrixStack.translate(pos.getX() - renderPosX, pos.getY() - renderPosY, pos.getZ() - renderPosZ + 1);
        int alfa = 64;
        int color = (alfa << 24) | MathHelper.hsvToRgb(hue, 0.8F, 1F);

        IVertexBuilder buffer = LINE_BUFFERS.getBuffer(lineType);
        World world = Minecraft.getInstance().level;
        // A block can have multiple areas - overkill for simple cube-shape blocks
        VoxelShape shape = world.getBlockState(pos).getShape(world, pos);
        List<AxisAlignedBB> list = shape.toAabbs().stream().map(bl -> bl.move(pos)).collect(Collectors.toList());
        if (!list.isEmpty()) {
            matrixStack.scale(1F, 1F, 1F);
            for (AxisAlignedBB axis : list) {
                axis = axis.move(-pos.getX(), -pos.getY(), -(pos.getZ() + 1));
                renderBlockOutline(matrixStack.last().pose(), buffer, axis, color);
            }
        }
        matrixStack.popPose();
        RenderSystem.disableDepthTest();
        LINE_BUFFERS.endBatch();
    }

    private static void renderBlockOutline(Matrix4f mat, IVertexBuilder buffer, AxisAlignedBB aabb, int color) {
        float ix = (float) aabb.minX;
        float iy = (float) aabb.minY;
        float iz = (float) aabb.minZ;
        float ax = (float) aabb.maxX;
        float ay = (float) aabb.maxY;
        float az = (float) aabb.maxZ;
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        buffer.vertex(mat, ix, iy, iz).color(r, g, b, a).endVertex();
        buffer.vertex(mat, ix, ay, iz).color(r, g, b, a).endVertex();

        buffer.vertex(mat, ix, ay, iz).color(r, g, b, a).endVertex();
        buffer.vertex(mat, ax, ay, iz).color(r, g, b, a).endVertex();

        buffer.vertex(mat, ax, ay, iz).color(r, g, b, a).endVertex();
        buffer.vertex(mat, ax, iy, iz).color(r, g, b, a).endVertex();

        buffer.vertex(mat, ax, iy, iz).color(r, g, b, a).endVertex();
        buffer.vertex(mat, ix, iy, iz).color(r, g, b, a).endVertex();

        buffer.vertex(mat, ix, iy, az).color(r, g, b, a).endVertex();
        buffer.vertex(mat, ix, ay, az).color(r, g, b, a).endVertex();

        buffer.vertex(mat, ix, iy, az).color(r, g, b, a).endVertex();
        buffer.vertex(mat, ax, iy, az).color(r, g, b, a).endVertex();

        buffer.vertex(mat, ax, iy, az).color(r, g, b, a).endVertex();
        buffer.vertex(mat, ax, ay, az).color(r, g, b, a).endVertex();

        buffer.vertex(mat, ix, ay, az).color(r, g, b, a).endVertex();
        buffer.vertex(mat, ax, ay, az).color(r, g, b, a).endVertex();

        buffer.vertex(mat, ix, iy, iz).color(r, g, b, a).endVertex();
        buffer.vertex(mat, ix, iy, az).color(r, g, b, a).endVertex();

        buffer.vertex(mat, ix, ay, iz).color(r, g, b, a).endVertex();
        buffer.vertex(mat, ix, ay, az).color(r, g, b, a).endVertex();

        buffer.vertex(mat, ax, iy, iz).color(r, g, b, a).endVertex();
        buffer.vertex(mat, ax, iy, az).color(r, g, b, a).endVertex();

        buffer.vertex(mat, ax, ay, iz).color(r, g, b, a).endVertex();
        buffer.vertex(mat, ax, ay, az).color(r, g, b, a).endVertex();
    }

}
