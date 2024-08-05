package lonestarrr.arconia.client.core.handler;

import lonestarrr.arconia.common.block.*;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ColoredRoot;
import lonestarrr.arconia.common.item.MagicInABottle;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Registers blocks that are colored dynamically, typically based on nbt/metadata or simply to share a single texture model with different
 * in-game colors
 */
public class ColorHandler {

    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        BlockColors colorBlocks = event.getBlockColors();

        // Gleaned from minecraft's ItemColors
        for (RainbowColor tier : RainbowColor.values()) {
            // Tree leaves
            ArconiumTreeLeaves treeLeaf = ModBlocks.getArconiumTreeLeaves(tier).get();
            event.register(treeLeaf, treeLeaf);

            // Tree saplings
            ArconiumTreeSapling treeSapling = ModBlocks.getArconiumTreeSapling(tier).get();
            colorBlocks.register(treeSapling, treeSapling);

            // Arconium blocks
            ArconiumBlock arconiumBlock = ModBlocks.getArconiumBlock(tier).get();
            colorBlocks.register(arconiumBlock, arconiumBlock);

            // Infinite Gold Arconium Blocks
            InfiniteGoldArconiumBlock infiniteGoldArconiumBlock = ModBlocks.getInfiniteGoldArconiumBlock(tier).get();
            colorBlocks.register(infiniteGoldArconiumBlock, infiniteGoldArconiumBlock);

            RainbowGrassBlock grassBlock = ModBlocks.getRainbowGrassBlock(tier).get();
            colorBlocks.register(grassBlock, grassBlock);
        }
    }

    // This one is fired after registering blocks, hence blockColors can be used
    public static void registerItemColors(RegisterColorHandlersEvent.Item event){
        ItemColors itemColors = event.getItemColors();
        BlockColors blockColors = event.getBlockColors();

        //magic in a bottle is colored differently based on ItemStack NBT data
        //'color' corresponds to the layer in the model (layer0 -> color 0, etc)
        //layer 0 is not dynamically colored, only layer1 is
        itemColors.register((stack, tint) -> {
            // only the overlay is colored - each layer is a tint index
            if (tint == 0) {
                return 0xffffff;
            }
            MagicInABottle bottle = (MagicInABottle) stack.getItem();
            return bottle.getTier(stack).getColorValue();
        }, ModItems.magicInABottle.get());

        for (RainbowColor tier : RainbowColor.values()) {
            // Tree leaves
            ArconiumTreeLeaves treeLeaf = ModBlocks.getArconiumTreeLeaves(tier).get();
            // Taken from minecraft's ItemColors
            itemColors.register((stack, tint) -> {
                BlockState blockstate = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
                return blockColors.getColor(blockstate, (BlockAndTintGetter) null, (BlockPos) null, tint);
            }, Item.byBlock(treeLeaf));

            // Tree saplings
            ArconiumTreeSapling treeSapling = ModBlocks.getArconiumTreeSapling(tier).get();
            // Taken from minecraft's ItemColors - for saplings, only layer0 is dynamically colored
            itemColors.register((stack, tint) -> {
                if (tint != 0) {
                    return 0xFFFFFF;
                }
                BlockState blockstate = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
                return blockColors.getColor(blockstate, (BlockAndTintGetter) null, (BlockPos) null, tint);
            }, Item.byBlock(treeSapling));


            RainbowGrassBlock grassBlock = ModBlocks.getRainbowGrassBlock(tier).get();
            itemColors.register((stack, tint) -> {
                BlockState blockstate = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
                return blockColors.getColor(blockstate, (BlockAndTintGetter) null, (BlockPos) null, tint);
            }, Item.byBlock(grassBlock));

            // Arconium blocks
            ArconiumBlock arconiumBlock = ModBlocks.getArconiumBlock(tier).get();
            // Taken from minecraft's ItemColors
            itemColors.register((stack, layer) -> {
                BlockState blockstate = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
                return blockColors.getColor(blockstate, (BlockAndTintGetter) null, (BlockPos) null, layer);
            }, Item.byBlock(arconiumBlock));

            // Infinite Gold Arconium Blocks
            InfiniteGoldArconiumBlock infiniteGoldArconiumBlock = ModBlocks.getInfiniteGoldArconiumBlock(tier).get();
            // Taken from minecraft's ItemColors
            itemColors.register((stack, layer) -> {
                if (layer != 0) {
                    return 0xFFFFFF;
                }
                BlockState blockstate = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
                return blockColors.getColor(blockstate, (BlockAndTintGetter) null, (BlockPos) null, layer);
            }, Item.byBlock(infiniteGoldArconiumBlock));

            // Colored tree roots
            itemColors.register((stack, layer) -> {
                return ((ColoredRoot) (stack.getItem())).getTier().getColorValue();
            }, ModItems.getColoredRoot(tier).get());

            // Colored arconium essence
            itemColors.register((stack, layer) -> {
                return tier.getColorValue();
            }, ModItems.getArconiumEssence(tier).get());

            // Colored arconium ingots
            itemColors.register((stack, layer) -> {
                return tier.getColorValue();
            }, ModItems.getArconiumIngot(tier).get());

            // Colored arconium hoes.
            // Hoes are colored differently based on ItemStack NBT data
            // 'color' corresponds to the layer in the model (layer0 -> color 0, etc)
            // layer 0 is not dynamically colored, only layer1 is
            itemColors.register((stack, layer) -> {
                // only the overlay is colored - each layer is a tint index
                if (layer == 0) {
                    return 0xffffff;
                }
                return tier.getColorValue();
            }, ModItems.getArconiumSickle(tier).get());
        }
    }
}
