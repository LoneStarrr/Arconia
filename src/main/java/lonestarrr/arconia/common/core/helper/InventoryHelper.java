package lonestarrr.arconia.common.core.helper;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InventoryHelper {

    /**
     * Get inventory for a given position and side - taken verbatim from Botania, thanks Vazkii!
     *
     * @param world
     * @param pos
     * @param side
     * @return
     */
    @Nullable
    public static IItemHandler getInventory(World world, BlockPos pos, Direction side) {
        TileEntity te = world.getBlockEntity(pos);

        if (te == null) {
            return null;
        }

        LazyOptional<IItemHandler> ret = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
        if (!ret.isPresent()) {
            ret = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        }
        return ret.orElse(null);
    }

    /**
     * Attempt to insert items in an inventory - taken verbatim from Botania src -thanks Vazkii!
     * @param dest
     * @param stack
     * @param simulate If set, no items will actually be transferred, but the return value represents of what would happen
     * @return Empty itemstack if all items could be inserted or there were none to insert, otherwise items that would not fit.
     */
    @Nonnull
    public static ItemStack insertItem(IItemHandler dest, @Nonnull ItemStack stack, boolean simulate)
    {
        if (dest == null || stack.isEmpty())
            return stack;

        for (int i = 0; i < dest.getSlots(); i++)
        {
            stack = dest.insertItem(i, stack, simulate);
            if (stack.isEmpty())
            {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }


}
