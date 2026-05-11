package lonestarrr.arconia.common.core.helper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;

public class InventoryHelper {

  @Nullable
  public static ResourceHandler<ItemResource> getInventory(
      Level level, BlockPos pos, Direction direction) {
    return level.getCapability(Capabilities.Item.BLOCK, pos, direction);
  }

  /**
   * Attempt to insert items in an inventory
   *
   * @param dest
   * @param stack
   * @param simulate If set, no items will actually be transferred, but the return value represents
   *     what would happen
   * @return Empty itemstack if all items could be inserted or there were none to insert, otherwise
   *     items that would not fit.
   */
  @Nonnull
  public static ItemStack insertItem(
      ResourceHandler<ItemResource> dest, @Nonnull ItemStack stack, boolean simulate) {
    if (dest == null || stack.isEmpty()) return stack;

    return ItemUtil.insertItemReturnRemaining(dest, stack, simulate, null);
  }
}
