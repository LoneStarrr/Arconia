package lonestarrr.arconia.common.block.tile.crate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

/**
 * An ItemStackHandler implementation used by containers that supports more then 64 items per slot.
 *
 * Implementation outline:
 *
 * There are 2 inventories: One 'normal' chest inventory that is interacted with, and an additional internal inventory.
 * Each chest inventory slot has a corresponding internal inventory slot. Those internal slots have higher stack limits.
 * When ticking the handler, it will move items from the chest inventory into the internal inventory if the chest
 * inventory item amount exceeds a given limit. Vice versa, if the chest slot count drops below that limit, and the
 * internal inventory item contains items, it will top up the chest slot. Thus, a simple self-contained storage system
 * is provided this way.
 *
 * On client worlds, no management of the internal inventory will be done, nor will the regular inventory be
 * modified. This data will be provided by the server side through network packets as it is necessary to render
 * information in the UI.
 *
 * "Did you just implement yet another yet another barrel?" - Sssshhhhhh...
 */
public class RainbowCrateItemStackHandler  extends ItemStackHandler {
    // Number of items in a slot to keep in the chest. More? Drain to internal inventory. Less? Top up from internal
    // inventory.
    private static final int KEEP_COUNT_IN_CHEST = 32;

    private final RainbowCrateInternalInventory internalInventory;

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     *
     * @param slots Number of slots
     * @param capacity Maximum number of items per slot of the secondary internal inventory
     */
    public RainbowCrateItemStackHandler(int slots, int capacity) {
        super(slots);
        internalInventory = new RainbowCrateInternalInventory(slots, capacity);
    }

    /**
     *
     * @return Per-slot item limit for the internal inventory slots.
     */
    public int getInternalCapacity() {
        return internalInventory.getSlotCapacity();
    }

    /**
     * @param slot
     * @return Item count of the internal inventory corresponding to the given chest slot
     */
    public int getInternalItemCount(int slot) {
        validateSlotIndex(slot);
        return this.internalInventory.getItemCount(slot);
    }

    // TODO quick hacky test method
    public int[] getInternalItemCounts() {
        return this.internalInventory.getItemCounts();
    }

    /**
     * This method is only to be called for inventories on the client side. It will update the internal item
     * counts, so that the UI can render the capacity per slot.
     *
     * @param itemCounts
     */
    public void receiveServerSideInventoryData(int[] itemCounts) {
        if (itemCounts.length != this.getSlots()) {
            throw new RuntimeException("Invalid itemcounts from network packet");
        }
        // ItemStack is irrelevant for client side worlds, only the counts are used to render capacity
        ItemStack dummyStack = new ItemStack(Items.PUMPKIN, 1);
        for (int i = 0; i < itemCounts.length; i++) {
            this.internalInventory.setItem(dummyStack, i, itemCounts[i]);
        }
    }

    /**
     * Chest items must match the internal inventory type if the slot is not empty
     *
     * @param slot
     * @param stack
     * @return
     */
    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        ItemStack internal = internalInventory.getItem(slot);

        return (internal == ItemStack.EMPTY || (internal.sameItem(stack)) && ItemStack.tagMatches(internal, stack));
    }

    /**
     * The internal inventory is automatically added to, or drained, during tick updates.
     * Do not tick the inventory on client side worlds - they will sync data with the server on their own accord
     */
    public boolean tick() {
        boolean inventoryChanged = false;

        for (int i = 0; i < this.getSlots(); i++) {
            ItemStack chestStack = this.getStackInSlot(i);

            if (chestStack == ItemStack.EMPTY || chestStack.getCount() < KEEP_COUNT_IN_CHEST) {
                inventoryChanged = drainInternalToChest(i) != 0 || inventoryChanged;
            } else if (chestStack.getCount() > KEEP_COUNT_IN_CHEST) {
                inventoryChanged = drainChestToInternal(i) != 0 || inventoryChanged;
            }
        }

        return inventoryChanged;
    }

    /**
     * If a chest slot has less than KEEP_COUNT_IN_CHEST items, and the internal inventory slot is not empty, attempt
     * to top up the chest slot item amount.
     *
     * @param slot
     */
    private int drainInternalToChest(int slot) {
        ItemStack chestStack = this.getStackInSlot(slot);
        if (chestStack.getCount() >= chestStack.getMaxStackSize()) {
            return 0;
        }

        ItemStack internalStack = this.internalInventory.getItem(slot);
        if (internalStack == ItemStack.EMPTY) {
            return 0;
        }

        int internalCount = this.internalInventory.getItemCount(slot);
        if (internalCount == 0) {
            throw new RuntimeException("Internal inventory count is 0 but ItemStack is not EMPTY");
        }

        int toChest = 0;

        // Move no more than KEEP_COUNT_IN_CHEST items
        if (chestStack == ItemStack.EMPTY) {
            toChest = (int)Math.min(KEEP_COUNT_IN_CHEST, internalCount);
        } else {
            if (!chestStack.sameItem(internalStack)) {
                throw new RuntimeException("Internal inventory item does not match chest slot");
            }
            int chestSpace = Math.max(KEEP_COUNT_IN_CHEST - chestStack.getCount(), 0);
            toChest = (int)Math.min(internalCount, chestSpace);
        }

        // Validate we're not exceeding maximum stack size for items in the chest
        if (toChest > 0) {
            int newCount = chestStack.getCount() + toChest;
            if (internalStack.getMaxStackSize() < newCount) {
                toChest = internalStack.getMaxStackSize() - chestStack.getCount();
            }
        }

        // All checks have passed. I like to move it, move it
        if (toChest > 0) {
            ItemStack copy = internalStack.copy();
            copy.setCount(chestStack.getCount() + toChest);
            this.setStackInSlot(slot, copy);
            this.internalInventory.decreaseItemCount(slot, toChest);
            return toChest;
        }

        return 0;
    }

    /**
     * If a chest slot has more than KEEP_COUNT_IN_CHEST items, attempt to drain the excess items to the internal
     * inventory's corresponding slot.
     *
     * @param slot
     */
    private int drainChestToInternal(int slot) {
        ItemStack chestStack = this.getStackInSlot(slot);

        if (chestStack == ItemStack.EMPTY || chestStack.getCount() <= KEEP_COUNT_IN_CHEST) {
            return 0;
        }

        int fromChest = chestStack.getCount() - KEEP_COUNT_IN_CHEST;
        ItemStack internalStack = this.internalInventory.getItem(slot);
        int internalCount = internalInventory.getItemCount(slot);

        if (internalStack != ItemStack.EMPTY && (!internalStack.sameItem(chestStack) || !ItemStack.tagMatches(internalStack, chestStack))) {
            // TODO should this really crash the game? E.g. what if this happens due to sync errors?
            throw new RuntimeException("Chest slot item does not match internal inventory item");
        }

        if (internalCount + fromChest > this.internalInventory.getSlotCapacity() || internalCount + fromChest < 0) {
            fromChest = this.internalInventory.getSlotCapacity() - internalCount;
        }

        if (fromChest > 0) {
            if (internalStack != ItemStack.EMPTY) {
                internalInventory.increaseItemCount(slot, fromChest);
            } else {
                internalInventory.setItem(chestStack, slot, fromChest);
            }
            ItemStack copy = chestStack.copy();
            copy.setCount(chestStack.getCount() - fromChest);
            this.setStackInSlot(slot, copy); // triggers onContentsChanged
            return fromChest;
        }

        return 0;
    }

    /**
     * An item occupies a chest slot that is incompatible with the internal inventory. Shouldn't happen. Dump it
     * into the world.
     *
     * @param slot
     */
    private void evictChestItem(int slot) {
       // TODO implement me
        LOGGER.warn("Unexpected chest item in slot " + slot);
        this.setStackInSlot(slot, ItemStack.EMPTY); // Triggers onContentsChanged
    }

    @Override
    public CompoundTag serializeNBT() {
        // Take NBT data from ItemStackhandler and add the internal inventory list to it
        CompoundTag nbt = super.serializeNBT();
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < this.getSlots(); i++) {
            if (!this.internalInventory.getItem(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                // ItemStack already writes a 'count' entry!
                itemTag.putInt("CrateCount", this.internalInventory.getItemCount(i));
                this.internalInventory.getItem(i).save(itemTag);
                // not writing the item type here, it is assumed to be always equal to the chest item!
                nbtTagList.add(itemTag);
            }
        }
        nbt.put("InternalInventory", nbtTagList);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag tagList = nbt.getList("InternalInventory", Constants.NBT.TAG_COMPOUND);
        internalInventory.clear();
        for (int i = 0; i < tagList.size(); i++)
        {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < this.getSlots()) {
                ItemStack stack = ItemStack.of(itemTags);
                int count = itemTags.getInt("CrateCount");
                internalInventory.setItem(stack, slot, count);
            }
        }
        super.deserializeNBT(nbt); // Triggers onLoad()
    }
}
