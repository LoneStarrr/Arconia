package lonestarrr.arconia.client.core.handler;

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
            return getRainbowColor(bottle.getTier(stack));
        }, ModItems.magicInABottle);

        for (RainbowColor tier: RainbowColor.values()) {
            // tree root tops are modified using the corresponding rainbow color.
            ResourceTreeRootBlock treeRoot = ModBlocks.getResourceTreeRootBlock(tier);
            colorBlocks.register(treeRoot, treeRoot);
            //taken from minecraft's ItemColors
            items.register((stack, color) -> {
                BlockState blockstate = ((BlockItem)stack.getItem()).getBlock().getDefaultState();
                return colorBlocks.getColor(blockstate, (IBlockDisplayReader)null, (BlockPos)null, color);
            }, Item.getItemFromBlock(treeRoot));

            //colored tree roots
            items.register((stack, color) -> {
                return getRainbowColor(((ColoredRoot)(stack.getItem())).getTier());
            }, ModItems.getColoredRoot(tier));

            //colored arconium essence
            items.register((stack, color) -> {
                return getRainbowColor(tier);
            }, ModItems.getArconiumEssence(tier));
        }
    }

    // A lot of blocks / items are tiered by rainbow color. Tint index corresponds to RainbowColor's ordinal value.
    public static int getRainbowColor(RainbowColor tier) {
        int color; // ARGB
        int alfa = 0;

        switch (tier.getTier()) {
            case 1: // RED
                color = 0xff << 16;
                break;
            case 2: // ORANGE
                color = 0xff << 16 | 0xa5 << 8;
                break;
            case 3: // YELLOW
                color = 0xff << 16 | 0xff << 8;
                break;
            case 4: // GREEN
                color = 0xff << 8;
//                color = 8431445; // birch
                break;
            case 5: // BLUE
                color = 0xff << 8 | 0xff;
                break;
            case 6: // INDIGO
                color = 0x42 << 16 | 0x82;
                break;
            default: // VIOLET
                color = 0xff << 16 | 0xff;
        }
        return color | alfa << 24;
    }

}
