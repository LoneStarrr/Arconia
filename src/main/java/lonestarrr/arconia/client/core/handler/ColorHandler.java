package lonestarrr.arconia.client.core.handler;

import lonestarrr.arconia.common.block.ResourceTreeLeaves;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.ResourceGenBlock;
import lonestarrr.arconia.common.block.ResourceTreeRootBlock;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ColoredRoot;
import lonestarrr.arconia.common.item.MagicInABottle;
import lonestarrr.arconia.common.item.ModItems;

import java.awt.*;

public class ColorHandler {
    public static void registerColorBlocks() {
        BlockColors colorBlocks = Minecraft.getInstance().getBlockColors();
        ItemColors items = Minecraft.getInstance().getItemColors();

        colorBlocks.register(ModBlocks.resourceGenBlock, ModBlocks.resourceGenBlock);
        //taken from minecraft's ItemColors
        items.register((stack, color) -> {
            BlockState blockstate = ((BlockItem)stack.getItem()).getBlock().getDefaultState();
            return colorBlocks.getColor(blockstate, (IBlockDisplayReader)null, (BlockPos)null, color);
        }, Item.getItemFromBlock(ModBlocks.resourceGenBlock));

        //magic in a bottle is colored differently based on ItemStack NBT data
        //'color' corresponds to the layer in the model (layer0 -> color 0, etc)
        //layer 0 is not dynamically colored, only layer1 is
        items.register((stack, color) -> {
            // only the overlay is colored - each layer is a tint index
            if (color == 0) {
                return 0xffffff;
            }
            MagicInABottle bottle = (MagicInABottle)stack.getItem();
            return RainbowColor.getColorRGB(bottle.getTier(stack));
        }, ModItems.magicInABottle);

        for (RainbowColor tier: RainbowColor.values()) {
            // Tree root tops are modified using the corresponding rainbow color.
            ResourceTreeRootBlock treeRoot = ModBlocks.getResourceTreeRootBlock(tier);
            colorBlocks.register(treeRoot, treeRoot);
            // Taken from minecraft's ItemColors
            items.register((stack, color) -> {
                BlockState blockstate = ((BlockItem)stack.getItem()).getBlock().getDefaultState();
                return colorBlocks.getColor(blockstate, (IBlockDisplayReader)null, (BlockPos)null, color);
            }, Item.getItemFromBlock(treeRoot));

            // Colored tree leaves
            ResourceTreeLeaves treeLeaf = ModBlocks.getMoneyTreeLeaves(tier);
            colorBlocks.register(treeLeaf, treeLeaf);
            // Taken from minecraft's ItemColors
            items.register((stack, color) -> {
                BlockState blockstate = ((BlockItem)stack.getItem()).getBlock().getDefaultState();
                return colorBlocks.getColor(blockstate, (IBlockDisplayReader)null, (BlockPos)null, color);
            }, Item.getItemFromBlock(treeLeaf));

            // Colored tree roots
            items.register((stack, color) -> {
                return RainbowColor.getColorRGB(((ColoredRoot)(stack.getItem())).getTier());
            }, ModItems.getColoredRoot(tier));

            // Colored arconium essence
            items.register((stack, color) -> {
                return RainbowColor.getColorRGB(tier);
            }, ModItems.getArconiumEssence(tier));
        }
    }
}
