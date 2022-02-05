package lonestarrr.arconia.common.block.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.items.ItemStackHandler;
import lonestarrr.arconia.common.lib.tile.BaseInventoryTileEntity;

public class BasePedestalTileEntity extends BaseInventoryTileEntity {
    private final ItemStackHandler inventory = new ItemStackHandler(1);

    public BasePedestalTileEntity(TileEntityType<?> type) {
        super(type);
    }

    @Override
    public ItemStackHandler getInventory() {
        return inventory;
    }

    /**
     * Sets an item on display. At most 1 item from the stack will be added.
     * @param stack Item to display. This stack is not modified
     * @return true if item was placed, false if another item was present
     */
    public boolean putItem(ItemStack stack) {
        ItemStack current = getItemOnDisplay();
        if (!current.isEmpty()) {
            return false;
        }
        ItemStack toPlace = stack.copy();
        toPlace.setCount(1);
        getInventory().setStackInSlot(0, toPlace);
        setChanged();
        updateClient();
        return true;
    }

    public ItemStack removeItem() {
        ItemStack current = getItemOnDisplay();
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }

        getInventory().setStackInSlot(0, ItemStack.EMPTY);
        setChanged();
        updateClient();
        return current;
    }

    public ItemStack getItemOnDisplay() {
        ItemStack onDisplay = getInventory().getStackInSlot(0);
        if (onDisplay == null || onDisplay.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            return onDisplay.copy();
        }
    }
}
