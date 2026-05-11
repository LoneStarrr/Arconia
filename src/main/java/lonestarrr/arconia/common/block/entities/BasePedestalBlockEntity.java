package lonestarrr.arconia.common.block.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public abstract class BasePedestalBlockEntity extends BaseInventoryBlockEntity {

  public BasePedestalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  @Override
  protected abstract ItemStacksResourceHandler getInventory();

  /**
   * Sets an item on display. At most 1 item from the stack will be added.
   *
   * @param stack Item to display. This stack is not modified
   * @return true if item was placed, false if another item was present
   */
  public boolean putItem(ItemStack stack) {
    ItemStack current = getItemOnDisplay();
    if (!current.isEmpty()) {
      return false;
    }
    getInventory().set(0, ItemResource.of(stack), 1);
    setChanged();
    updateClient();
    return true;
  }

  public ItemStack removeItem() {
    ItemStack current = getItemOnDisplay();
    if (current.isEmpty()) {
      return ItemStack.EMPTY;
    }

    getInventory().set(0, ItemResource.EMPTY, 0);
    setChanged();
    updateClient();
    return current;
  }

  public ItemStack getItemOnDisplay() {
    ItemStacksResourceHandler inv = getInventory();
    ItemResource resource = inv.getResource(0);
    if (resource.isEmpty()) {
      return ItemStack.EMPTY;
    }
    return resource.toStack((int) inv.getAmountAsLong(0));
  }
}
