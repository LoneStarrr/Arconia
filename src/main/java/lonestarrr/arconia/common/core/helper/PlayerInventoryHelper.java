package lonestarrr.arconia.common.core.helper;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PlayerInventoryHelper {
    public final static int SLOT_HOTBAR = 1;
    public final static int SLOT_ITEMS = 2; // The 27 item slots minus the hotbar
    public final static int SLOT_ARMOR = 4;
    public final static int SLOT_OFFHAND = 8;

    /**
     * @param inv
     * @param toFind
     * @param matchTags If true, tags must also match
     * @param slotTypes Slot types to search, a bit pattern of the SLOT_* ints defined here
     * @return First slot containing the desired item, or -1 if no slot was found. Item slots are searched in hotbar, items, armor and offhand order.
     */
    public static int findSlotMatchingItem(Inventory inv, ItemStack toFind, boolean matchTags, int slotTypes) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            int slotType = getSlotType(i);
            if ((slotTypes & slotType) != 0) {
                ItemStack itemInv = inv.getItem(i);
                if (matchTags) {
                    if (ItemStack.isSameItemSameTags(toFind, itemInv)) {
                        return i;
                    }
                } else if (ItemStack.isSame(toFind, itemInv)) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * @param index
     * @return Type of slot for a given player inventory index, one of SLOT_* defined on this class
     */
    public static int getSlotType(int index) {
        // Sadly the player inventory implementation does not expose clear definitions of which slot is of which type, so we'll have to just encode that
        // logic here.
        if (index < 9) {
            return SLOT_HOTBAR;
        } else if (index < 36) {
            return SLOT_ITEMS;
        } else if (index < 40) {
            return SLOT_ARMOR;
        } else {
            return SLOT_OFFHAND;
        }
    }
}
