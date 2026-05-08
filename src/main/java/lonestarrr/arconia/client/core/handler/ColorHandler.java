package lonestarrr.arconia.client.core.handler;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.*;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.client.color.block.BlockColors;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Registers blocks that are colored dynamically, typically based on nbt/metadata or simply to share a single texture model with different
 * in-game colors
 */
public class ColorHandler {

    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        Arconia.logger.info("***** registerBlockColors");
        BlockColors colorBlocks = event.getBlockColors();

        // Gleaned from minecraft's ItemColors
        for (RainbowColor tier : RainbowColor.values()) {
            // Tree leaves
            ArconiumTreeLeaves treeLeaf = ModBlocks.getArconiumTreeLeaves(tier).get();
            colorBlocks.register((state, level, pos, tintIndex) -> {
                return tier.getColorValue();
            }, treeLeaf);

            // Tree saplings
            ArconiumTreeSapling treeSapling = ModBlocks.getArconiumTreeSapling(tier).get();
            colorBlocks.register((state, level, pos, tintIndex) -> {
                return tier.getColorValue();
            }, treeSapling);

            // Arconium blocks
            ArconiumBlock arconiumBlock = ModBlocks.getArconiumBlock(tier).get();
            colorBlocks.register((state, level, pos, tintIndex) -> {
                return tier.getColorValue();
            }, arconiumBlock);

            RainbowGrassBlock grassBlock = ModBlocks.getRainbowGrassBlock(tier).get();
            colorBlocks.register((state, level, pos, tintIndex) -> {
                return tier.getColorValue();
            }, grassBlock);
        }
    }

    // Pass 2 step C: registerItemColors removed — RegisterColorHandlersEvent.Item is gone in 1.21.4 and item tinting is now data-driven via
    // ItemTintSource references in ClientItem JSON. The old logic (per-tier tints for branches/essences/ingots/sickles, plus the per-stack
    // MagicInABottle filled-state coloring) needs to be re-expressed either as `minecraft:constant` tints in the per-item ClientItem JSON
    // emitted by the new data generators, or as a custom registered ItemTintSource for the dynamic cases.
}
