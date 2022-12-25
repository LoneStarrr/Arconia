package lonestarrr.arconia.client.core.handler;

import lonestarrr.arconia.common.block.*;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ColoredRoot;
import lonestarrr.arconia.common.item.MagicInABottle;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class ColorHandler {
    public static void registerColorBlocks() {
        BlockColors colorBlocks = Minecraft.getInstance().getBlockColors();
        ItemColors items = Minecraft.getInstance().getItemColors();

        colorBlocks.register(ModBlocks.resourceGenBlock, ModBlocks.resourceGenBlock);
        //taken from minecraft's ItemColors
        items.register((stack, color) -> {
            BlockState blockstate = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
            return colorBlocks.getColor(blockstate, (BlockAndTintGetter)null, (BlockPos)null, color);
        }, Item.byBlock(ModBlocks.resourceGenBlock));

        //magic in a bottle is colored differently based on ItemStack NBT data
        //'color' corresponds to the layer in the model (layer0 -> color 0, etc)
        //layer 0 is not dynamically colored, only layer1 is
        items.register((stack, layer) -> {
            // only the overlay is colored - each layer is a tint index
            if (layer == 0) {
                return 0xffffff;
            }
            MagicInABottle bottle = (MagicInABottle)stack.getItem();
            return bottle.getTier(stack).getColorValue();
        }, ModItems.magicInABottle);

        for (RainbowColor tier: RainbowColor.values()) {
            // Tree leaves
            ArconiumTreeLeaves treeLeaf = ModBlocks.getArconiumTreeLeaves(tier);
            colorBlocks.register(treeLeaf, treeLeaf);
            // Taken from minecraft's ItemColors
            items.register((stack, layer) -> {
                BlockState blockstate = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
                return colorBlocks.getColor(blockstate, (BlockAndTintGetter)null, (BlockPos)null, layer);
            }, Item.byBlock(treeLeaf));

            // Tree saplings
            ArconiumTreeSapling treeSapling = ModBlocks.getArconiumTreeSapling(tier);
            colorBlocks.register(treeSapling, treeSapling);
            // Taken from minecraft's ItemColors - for saplings, only layer0 is dynamically colored
            items.register((stack, layer) -> {
                if (layer != 0) {
                    return 0xFFFFFF;
                }
                BlockState blockstate = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
                return colorBlocks.getColor(blockstate, (BlockAndTintGetter)null, (BlockPos)null, layer);
            }, Item.byBlock(treeSapling));

            // Arconium blocks
            ArconiumBlock arconiumBlock = ModBlocks.getArconiumBlock(tier);
            colorBlocks.register(arconiumBlock, arconiumBlock);
            // Taken from minecraft's ItemColors
            items.register((stack, layer) -> {
                BlockState blockstate = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
                return colorBlocks.getColor(blockstate, (BlockAndTintGetter)null, (BlockPos)null, layer);
            }, Item.byBlock(arconiumBlock));

            // Infinite Gold Arconium Blocks
            InfiniteGoldArconiumBlock infiniteGoldArconiumBlock = ModBlocks.getInfiniteGoldArconiumBlock(tier);
            colorBlocks.register(infiniteGoldArconiumBlock, infiniteGoldArconiumBlock);
            // Taken from minecraft's ItemColors
            items.register((stack, layer) -> {
                if (layer != 0) {
                    return 0xFFFFFF;
                }
                BlockState blockstate = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
                return colorBlocks.getColor(blockstate, (BlockAndTintGetter)null, (BlockPos)null, layer);
            }, Item.byBlock(infiniteGoldArconiumBlock));

            // Rainbow crates
            RainbowCrateBlock crateBlock = ModBlocks.getRainbowCrateBlock(tier);
            colorBlocks.register(crateBlock, crateBlock);
            // Taken from minecraft's ItemColors
            items.register((stack, layer) -> {
                BlockState blockstate = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
                return colorBlocks.getColor(blockstate, (BlockAndTintGetter)null, (BlockPos)null, layer);
            }, Item.byBlock(crateBlock));

            // Colored tree roots
            items.register((stack, layer) -> {
                return ((ColoredRoot)(stack.getItem())).getTier().getColorValue();
            }, ModItems.getColoredRoot(tier));

            // Colored arconium essence
            items.register((stack, layer) -> {
                return tier.getColorValue();
            }, ModItems.getArconiumEssence(tier));

            // Colored arconium ingots
            items.register((stack, layer) -> {
                return tier.getColorValue();
            }, ModItems.getArconiumIngot(tier));

            // Colored arconium hoes
            // Hoes are colored differently based on ItemStack NBT data
            // 'color' corresponds to the layer in the model (layer0 -> color 0, etc)
            // layer 0 is not dynamically colored, only layer1 is
            items.register((stack, layer) -> {
                // only the overlay is colored - each layer is a tint index
                if (layer == 0) {
                    return 0xffffff;
                }
                return tier.getColorValue();
            }, ModItems.getArconiumSickle(tier));
        }
    }
}
