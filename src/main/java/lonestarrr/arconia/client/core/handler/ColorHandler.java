package lonestarrr.arconia.client.core.handler;

import java.util.List;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ArconiumBlock;
import lonestarrr.arconia.common.block.ArconiumTreeLeaves;
import lonestarrr.arconia.common.block.ArconiumTreeSapling;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.RainbowGrassBlock;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.client.color.block.BlockTintSources;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Registers blocks that are colored dynamically, typically based on nbt/metadata or simply to share
 * a single texture model with different in-game colors
 */
public class ColorHandler {

  public static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
    Arconia.logger.info("***** registerBlockColors");

    // Gleaned from minecraft's ItemColors
    for (RainbowColor tier : RainbowColor.values()) {
      // Tree leaves
      ArconiumTreeLeaves treeLeaf = ModBlocks.getArconiumTreeLeaves(tier).get();
      event.register(List.of(BlockTintSources.constant(tier.getColorValue())), treeLeaf);

      // Tree saplings
      ArconiumTreeSapling treeSapling = ModBlocks.getArconiumTreeSapling(tier).get();
      event.register(List.of(BlockTintSources.constant(tier.getColorValue())), treeSapling);

      // Arconium blocks
      ArconiumBlock arconiumBlock = ModBlocks.getArconiumBlock(tier).get();
      event.register(List.of(BlockTintSources.constant(tier.getColorValue())), arconiumBlock);

      RainbowGrassBlock grassBlock = ModBlocks.getRainbowGrassBlock(tier).get();
      event.register(List.of(BlockTintSources.constant(tier.getColorValue())), grassBlock);
    }
  }

  // Pass 2 step C: registerItemColors removed — RegisterColorHandlersEvent.Item is gone in 1.21.4
  // and item tinting is now data-driven via
  // ItemTintSource references in ClientItem JSON. The old logic (per-tier tints for
  // branches/essences/ingots/sickles, plus the per-stack
  // MagicInABottle filled-state coloring) needs to be re-expressed either as `minecraft:constant`
  // tints in the per-item ClientItem JSON
  // emitted by the new data generators, or as a custom registered ItemTintSource for the dynamic
  // cases.
}
