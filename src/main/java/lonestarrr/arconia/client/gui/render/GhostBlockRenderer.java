package lonestarrr.arconia.client.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nonnull;

/**
 * Render ghost blocks in the client world
 */
public class GhostBlockRenderer {
    /**
     * Render a 'ghost block' at a given position in the client world, typically used for previewing structures
     * @param poseStack
     * @param pos Position in the world to render block
     * @param state Blockstate to render - The texture of this blockstate will be used to draw the ghost block
     * @param lightLevel Light level of the block to render
     * @param scale Scale to render at
     */
    public static void renderGhostBlock(@Nonnull final PoseStack poseStack, @Nonnull final BlockPos pos, @Nonnull final BlockState state, final int lightLevel, final float scale) {
        BlockRenderDispatcher renderer = Minecraft.getInstance().getBlockRenderer();
        ClientLevel world = Minecraft.getInstance().level;
//        ModelData model = renderer.getBlockModel(state).getModelData(world, new BlockPos(pos), state, world.getModelDataManager().getAt(pos));
        ModelData model = world.getModelDataManager().getAt(pos);
        Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();

        poseStack.pushPose();
        poseStack.translate(-renderInfo.getPosition().x() + pos.getX(), -renderInfo.getPosition().y() + pos.getY(), -renderInfo.getPosition().z() + pos.getZ());

        final float scaleOffset = (1F - scale) / 2F; // render scaled block in the center of this world pos
        poseStack.translate(scaleOffset, scaleOffset, scaleOffset);
        poseStack.scale(scale, scale, scale);
        // Overlay texture = custom RGBA on top of texture, 0 -> red
        //getBufferSource -> display over everything else
        //getCrumblingBufferSource -> display as part of chunk rendering

        // overlay: first int is light level (0..15)
        // looking at implementation: 2nd val = 3 or 10, is that 'UV'? No, 'UV' is used in models to indicate a crop of a texture area
        // TODO added RenderType.TRANSLUCENT her as a new parameter - does that make sense..?
        // TODO Maybe take inspiration from e.g. https://github.com/XFactHD/FramedBlocks/blob/1.20/src/main/java/xfacthd/framedblocks/client/render/special/GhostBlockRenderer.java#L167-L222
        int combinedOverlayIn = OverlayTexture.pack(lightLevel, 10);
        int combinedLightIn = 240<<16 + 240; // What do these values represent?
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, Minecraft.getInstance().renderBuffers().crumblingBufferSource(), combinedLightIn, combinedOverlayIn, model, RenderType.translucent());
        poseStack.popPose();
    }
}
