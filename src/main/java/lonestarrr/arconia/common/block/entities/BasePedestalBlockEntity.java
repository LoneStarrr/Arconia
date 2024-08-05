package lonestarrr.arconia.common.block.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class BasePedestalBlockEntity extends BaseInventoryBlockEntity {

    public BasePedestalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public abstract ItemStackHandler getInventory();

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
