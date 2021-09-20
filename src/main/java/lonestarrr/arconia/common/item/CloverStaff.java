package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.block.PotMultiBlockPrimary;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Staff. Magic's wrench. Because every mod needs a staff or wrench.
 *
 */
public class CloverStaff extends Item {
    public CloverStaff(Item.Properties builder) {
        super(builder);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos = context.getPos();
        World world = context.getWorld();

        BlockState bs = world.getBlockState(pos);
        if (bs.getBlock() == Blocks.GOLD_BLOCK) {
            // Might be an attempt to form a pot of gold multiblock
            if (world.isRemote) {
                if (PotMultiBlockPrimary.canFormMultiBlock(world, pos)) {
                    return ActionResultType.SUCCESS; // Assume it'll work
                }
                return ActionResultType.FAIL;
            } else {
                boolean formed = PotMultiBlockPrimary.formMultiBlock(world, pos);
                return formed ? ActionResultType.SUCCESS : ActionResultType.FAIL;
            }
        }

        return ActionResultType.PASS;
    }
}
