package lonestarrr.arconia.client.effects;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
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
     * @param poseStack
     * @param buffer
     * @param color The color to use, or null if it should cycle through all rainbow colors
     */
    public static void renderRainbowLighting(BlockPos pos, float beamLength, int beamCount, PoseStack poseStack, MultiBufferSource buffer, Color fixedColor) {
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
        poseStack.translate(0.5f, 0.5f, 0.5f);

        final Vector3f[] rotationVectors = new Vector3f[] {
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0),
                new Vector3f(0, 0, 1),
        };

        // Seed rng with a fixed seed to procedurally generate consistent angles etc without having to keep state
        Random rand = new Random(pos.asLong());

        // Vertices for a single  beam - w is used for alpha
        List<Vector4f> vertices = new ArrayList<>(10);

        VertexConsumer builder = buffer.getBuffer(LightningRenderType.BEAM_TRIANGLE);
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
                poseStack.mulPose(new Quaternion(vec, angle, true));
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
                builder.vertex(positionMatrix, vertex.x(), vertex.y(), vertex.z()).color(colorR, colorG, colorB, alpha).endVertex();
            }
            poseStack.popPose();
        }

        poseStack.popPose();

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
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TransparencyStateShard.LIGHTNING_TRANSPARENCY)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
    );
}