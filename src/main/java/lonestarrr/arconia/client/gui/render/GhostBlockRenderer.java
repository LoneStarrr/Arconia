package lonestarrr.arconia.client.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
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
    public static void renderGhostBlock(@Nonnull final PoseStack matrixStack, @Nonnull final BlockPos pos, @Nonnull final BlockState state, final int lightLevel, final float scale) {
        BlockRenderDispatcher renderer = Minecraft.getInstance().getBlockRenderer();
        ClientLevel world = Minecraft.getInstance().level;
        IModelData model = renderer.getBlockModel(state).getModelData(world, new BlockPos(pos), state, ModelDataManager.getModelData(world, new BlockPos(pos)));
        Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();

        matrixStack.pushPose();
        matrixStack.translate(-renderInfo.getPosition().x() + pos.getX(), -renderInfo.getPosition().y() + pos.getY(), -renderInfo.getPosition().z() + pos.getZ());

        final float scaleOffset = (1F - scale) / 2F; // render scaled block in the center of this world pos
        matrixStack.translate(scaleOffset, scaleOffset, scaleOffset);
        matrixStack.scale(scale, scale, scale);
        // Overlay texture = custom RGBA on top of texture, 0 -> red
        //getBufferSource -> display over everything else
        //getCrumblingBufferSource -> display as part of chunk rendering

        // overlay: first int is light level (0..15)
        // looking at implementation: 2nd val = 3 or 10, is that 'UV'? No, 'UV' is used in models to indicate a texture area size (e.g. if not 16x16).
        int combinedOverlayIn = OverlayTexture.pack(lightLevel, 10);
        int combinedLightIn = 240<<16 + 240; // What do these values represent?
        Minecraft.getInstance().getBlockRenderer().renderBlock(state, matrixStack, Minecraft.getInstance().renderBuffers().crumblingBufferSource(), combinedLightIn, combinedOverlayIn, model);
        matrixStack.popPose();
    }
}
