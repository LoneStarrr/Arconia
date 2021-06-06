package lonestarrr.arconia.common.item;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.tile.ResourceGenTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;

/**
 * Tree roots are droppped as rare loot from chopping down resource trees growing in the wild. They will get 'magic' properties through a ritual that will
 * make them turn grass blocks into resource generating blocks for a resource generating tree.
 */
public class TreeRoot extends Item {
    public TreeRoot(Properties builder) {
        super(builder);
    }
}
