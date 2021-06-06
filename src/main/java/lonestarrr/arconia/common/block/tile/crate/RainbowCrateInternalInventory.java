package lonestarrr.arconia.common.block.tile.crate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.List;

/**
 * Internal inventory of a rainbow crate.
 */
public class RainbowCrateInternalInventory {
    private NonNullList<ItemStack> items;
    private final int[] itemCounts;
    private final int capacity;
    private final int slots;

    public RainbowCrateInternalInventory(int slots, int capacity) {
        itemCounts = new int[slots];
        this.slots = slots;
        this.capacity = capacity;
        items = NonNullList.withSize(this.slots, ItemStack.EMPTY);
    }

    public void clear() {
        for (int slot = 0; slot < this.slots; slot++) {
            setItem(ItemStack.EMPTY, slot, 0);
        }
    }

    public int getSlotCapacity() {
        return capacity;
    }

    public ItemStack getItemStack(int slot) {
        return items.get(slot);
    }

    public void setItem(ItemStack item, int slot, int count) {
        if (item == ItemStack.EMPTY && count != 0) {
            throw new RuntimeException("Can't set a non-zero item count for an empty slot");
        }
        items.set(slot, item);
        itemCounts[slot] = count;
    }

    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    public int getItemCount(int slot) {
        return itemCounts[slot];
    }

    public int[] getItemCounts() {
        return itemCounts;
    }

    public void increaseItemCount(int slot, int amount) {
        int newCount = itemCounts[slot] + amount;
        if (newCount > this.capacity || newCount < 0) {
            throw new IllegalArgumentException("Attempt to increase item count beyond capacity");
        }
        this.updateItemCount(slot, newCount);
    }

    public void decreaseItemCount(int slot, int amount) {
        this.increaseItemCount(slot, -amount);
    }

    private void updateItemCount(int slot, int newCount) {
        if (newCount == 0) {
            this.setItem(ItemStack.EMPTY, slot, 0);
        } else {
            this.setItem(this.getItem(slot), slot, newCount);
        }
    }

}
