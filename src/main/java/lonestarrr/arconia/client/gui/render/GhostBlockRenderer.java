package lonestarrr.arconia.client.gui.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;

/**
 * Render ghost blocks in the client world
 */
public class GhostBlockRenderer {
    /**
     * Render a 'ghost block' at a given position in the client world, typically used for previewing structures
     * @param matrixStack
     * @param pos Position in the world to render block
     * @param state Blockstate to render - The texture of this blockstate will be used to draw the ghost block
     * @param lightLevel Light level of the block to render
     * @param scale Scale to render at
     */
    public static void renderGhostBlock(@Nonnull final MatrixStack matrixStack, @Nonnull final BlockPos pos, @Nonnull final BlockState state, final int lightLevel, final float scale) {
        BlockRendererDispatcher renderer = Minecraft.getInstance().getBlockRendererDispatcher();
        ClientWorld world = Minecraft.getInstance().world;
        IModelData model = renderer.getModelForState(state).getModelData(world, new BlockPos(pos), state, ModelDataManager.getModelData(world, new BlockPos(pos)));
        ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();

        matrixStack.push();
        matrixStack.translate(-renderInfo.getProjectedView().getX() + pos.getX(), -renderInfo.getProjectedView().getY() + pos.getY(), -renderInfo.getProjectedView().getZ() + pos.getZ());

        final float scaleOffset = (1F - scale) / 2F; // render scaled block in the center of this world pos
        matrixStack.translate(scaleOffset, scaleOffset, scaleOffset);
        matrixStack.scale(scale, scale, scale);
        // Overlay texture = custom RGBA on top of texture, 0 -> red
        //getBufferSource -> display over everything else
        //getCrumblingBufferSource -> display as part of chunk rendering

        // overlay: first int is light level (0..15)
        // looking at implementation: 2nd val = 3 or 10, is that 'UV'? No, 'UV' is used in models to indicate a texture area size (e.g. if not 16x16).
        int combinedOverlayIn = OverlayTexture.getPackedUV(lightLevel, 10);
        int combinedLightIn = 240<<16 + 240; // What do these values represent?
        Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(state, matrixStack, Minecraft.getInstance().getRenderTypeBuffers().getCrumblingBufferSource(), combinedLightIn, combinedOverlayIn, model);
        matrixStack.pop();
    }
}
