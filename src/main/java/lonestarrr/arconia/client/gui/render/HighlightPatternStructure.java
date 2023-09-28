package lonestarrr.arconia.client.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.event.RenderLevelLastEvent;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * When a player builds a structure using pattern blocks, highlight the pattern blocks that are detected when requesting
 * pattern completion inspection
 */
public class HighlightPatternStructure {
    private static Set<BlockPos> highlightedBlocks = null;

    public static void togglePreview(@Nonnull final Set<BlockPos> blocksToHighlight) {
        if (highlightedBlocks == null) {
            highlightedBlocks = blocksToHighlight;
        } else {
            highlightedBlocks = null;
        }
    }

    public static void disablePreview() {
        highlightedBlocks = null;
    }

    public static boolean previewEnabled() {
        return highlightedBlocks != null;
    }

    public static void render(RenderLevelLastEvent event) {
        if (highlightedBlocks != null) {
            renderHighlightedBlocks(highlightedBlocks, event.getPoseStack());
        }
    }

    private static void renderHighlightedBlocks(@Nonnull final Set<BlockPos> blocks, @Nonnull  final PoseStack matrixStack) {
        float gameTime = Minecraft.getInstance().level.getGameTime(); // ticks since start
        float hue = gameTime % 100 / 100F; // cycle hue as animation effect
        matrixStack.pushPose();
        for (BlockPos pos: blocks) {
            OutlineBlockRenderer.renderOutline(matrixStack, pos, hue, OutlineBlockRenderer.LINE_3_NO_DEPTH);
        }
        matrixStack.popPose();
    }

}
