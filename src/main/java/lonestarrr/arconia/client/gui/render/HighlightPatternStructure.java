package lonestarrr.arconia.client.gui.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * When a player builds a structure using pattern blocks, highlight the pattern blocks that are detected when requesting
 * pattern completion inspection
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
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

    @SubscribeEvent
    public static void render(RenderWorldLastEvent event) {
        if (highlightedBlocks != null) {
            renderHighlightedBlocks(highlightedBlocks, event.getMatrixStack());
        }
    }

    private static void renderHighlightedBlocks(@Nonnull final Set<BlockPos> blocks, @Nonnull  final MatrixStack matrixStack) {
        float gameTime = Minecraft.getInstance().world.getGameTime(); // ticks since start
        float hue = gameTime % 100 / 100F; // cycle hue as animation effect
        matrixStack.push();
        for (BlockPos pos: blocks) {
            OutlineBlockRenderer.renderOutline(matrixStack, pos, hue, OutlineBlockRenderer.LINE_3_NO_DEPTH);
        }
        matrixStack.pop();
    }

}
