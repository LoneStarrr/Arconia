package lonestarrr.arconia.client.gui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import org.joml.Matrix4f;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.RenderType.CompositeState;

/**
 * Renders block outlines
 */
@OnlyIn(Dist.CLIENT)
public class OutlineBlockRenderer extends RenderType {
    private static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY;
    private static final MultiBufferSource.BufferSource LINE_BUFFERS;
    private static Set<BlockPos> highlightedBlocks = null;
    public static final RenderType LINE_3_NO_DEPTH;
    public static final RenderType LINE_8_NO_DEPTH;

    // Default onstructor to satisfy the compiler
    public OutlineBlockRenderer(
            String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_,
            Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    private static RenderType makeLayer(
            String name, VertexFormat format, VertexFormat.Mode mode,
            int bufSize, boolean hasCrumbling, boolean sortOnUpload, CompositeState glState) {
        return create(name, format, mode, bufSize, hasCrumbling, sortOnUpload, glState);
    }

    private static RenderType makeLayer(
            String name, VertexFormat format, VertexFormat.Mode mode,
            int bufSize, CompositeState glState) {
        return makeLayer(name, format, mode, bufSize, false, false, glState);
    }

    static {
        // Most of this is gleaned from Botania's RenderHelper
        TRANSLUCENT_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderStateShard.class, null,
                "TRANSLUCENT_TRANSPARENCY");
        RenderStateShard.LayeringStateShard projectionLayering = ObfuscationReflectionHelper.getPrivateValue(RenderStateShard.class, null,
                "VIEW_OFFSET_Z_LAYERING");
        RenderStateShard.WriteMaskStateShard colorMask = new RenderStateShard.WriteMaskStateShard(true, false);
        RenderStateShard.DepthTestStateShard noDepth = new RenderStateShard.DepthTestStateShard("always", GL11.GL_ALWAYS);

        // https://github.com/Vazkii/Botania/blob/24715c509e47dfc32a80d7e94aeba6d84c022503/src/main/java/vazkii/botania/client/core/helper/RenderHelper.java
        // May want to enable depthTest() ?
        RenderType.CompositeState glState = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_SHADER)
                .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(3)))
                .setLayeringState(projectionLayering)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(colorMask)
                .setDepthTestState(noDepth)
                .createCompositeState(false);
        LINE_3_NO_DEPTH = makeLayer(Arconia.MOD_ID + ":line_3_no_depth", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES, 128, glState);
        glState = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_SHADER)
                .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(8)))
                .setLayeringState(projectionLayering)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(colorMask).setDepthTestState(noDepth)
                .createCompositeState(false);
        LINE_8_NO_DEPTH = makeLayer(Arconia.MOD_ID + ":line_8_no_depth", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES, 128, glState);
        // https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/client/core/handler/BoundTileRenderer.java
        LINE_BUFFERS = MultiBufferSource.immediateWithBuffers(Util.make(() -> {
            Map<RenderType, BufferBuilder> ret = new IdentityHashMap<>();
            ret.put(LINE_3_NO_DEPTH, new BufferBuilder(LINE_3_NO_DEPTH.bufferSize()));
            ret.put(LINE_8_NO_DEPTH, new BufferBuilder(LINE_8_NO_DEPTH.bufferSize()));
            return ret;
        }), Tesselator.getInstance().getBuilder());
    }

    /**
     * Renders a wireframe-like outline that will always be on top.
     *
     * @param hue Outline hue
     */
    public static void renderOutline(@Nonnull final PoseStack matrixStack, @Nonnull final BlockPos pos, float hue, RenderType lineType) {
        double renderPosX = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition().x();
        double renderPosY = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition().y();
        double renderPosZ = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition().z();

        matrixStack.pushPose();
        matrixStack.translate(pos.getX() - renderPosX, pos.getY() - renderPosY, pos.getZ() - renderPosZ + 1);
        int alfa = 64;
        int color = (alfa << 24) | Mth.hsvToRgb(hue, 0.8F, 1F);

        VertexConsumer buffer = LINE_BUFFERS.getBuffer(lineType);
        Level world = Minecraft.getInstance().level;
        // A block can have multiple areas - overkill for simple cube-shape blocks
        VoxelShape shape = world.getBlockState(pos).getShape(world, pos);
        List<AABB> list = shape.toAabbs().stream().map(bl -> bl.move(pos)).collect(Collectors.toList());
        if (!list.isEmpty()) {
            matrixStack.scale(1F, 1F, 1F);
            for (AABB axis : list) {
                axis = axis.move(-pos.getX(), -pos.getY(), -(pos.getZ() + 1));
                renderBlockOutline(matrixStack.last().pose(), buffer, axis, color);
            }
        }
        matrixStack.popPose();
        RenderSystem.disableDepthTest();
        LINE_BUFFERS.endBatch();
    }

    private static void renderBlockOutline(Matrix4f mat, VertexConsumer buffer, AABB aabb, int color) {
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
