package lonestarrr.arconia.client.gui.crate;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lonestarrr.arconia.common.block.RainbowCrateBlock;
import lonestarrr.arconia.common.block.tile.RainbowCrateTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.block.tile.crate.RainbowCrateItemStackHandler;

import java.util.stream.Stream;

/**
 * A Container is a temporary object that combines the player inventory with the container's inventory. A gui
 * will display this combination when accessing the container.
 */
public class RainbowCrateContainer extends Container {
    // hints for the gui so it knows where to draw the Titles
    public static final int TILE_INVENTORY_YPOS = 5;
    public static final int PLAYER_INVENTORY_YPOS = 175;

    private final RainbowCrateItemStackHandler chestContents;
    private final RainbowColor tier;
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;

    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    private static final Logger LOGGER = LogManager.getLogger();

    public RainbowCrateContainer(RainbowColor tier, int windowId, PlayerInventory playerInventory,
                                 RainbowCrateItemStackHandler chestInventory) {
        super(RainbowCrateBlock.getContainerTypeByTier(tier), windowId);
        this.tier = tier;
        //Container c = playerInventory.player.openContainer;

        // Forge wraps IInventory in IItemHandler to facilitate mod interactions
        PlayerInvWrapper playerInventoryForge = new PlayerInvWrapper(playerInventory);
        this.chestContents = chestInventory;

        // "Vanilla" refers to the player inventory + hot bar slots
        final int VANILLA_SLOT_X_SPACING = 18;
        final int VANILLA_SLOT_Y_SPACING = 18;
        final int HOTBAR_XPOS = 41;
        final int HOTBAR_YPOS = 233;
        // Add the players hotbar to the gui - the [xpos, ypos] location of each item
        for (int x = 0; x < HOTBAR_SLOT_COUNT; x++) {
            int slotNumber = x;
            addSlot(new SlotItemHandler(playerInventoryForge, slotNumber, HOTBAR_XPOS + VANILLA_SLOT_X_SPACING * x,
                    HOTBAR_YPOS));
        }

        final int PLAYER_INVENTORY_XPOS = HOTBAR_XPOS;
        // Add the rest of the player's inventory to the gui
        for (int y = 0; y < PLAYER_INVENTORY_ROW_COUNT; y++) {
            for (int x = 0; x < PLAYER_INVENTORY_COLUMN_COUNT; x++) {
                final int slotNumber = HOTBAR_SLOT_COUNT + y * PLAYER_INVENTORY_COLUMN_COUNT + x;
                final int xpos = PLAYER_INVENTORY_XPOS + x * VANILLA_SLOT_X_SPACING;
                final int ypos = PLAYER_INVENTORY_YPOS + y * VANILLA_SLOT_Y_SPACING;
                addSlot(new SlotItemHandler(playerInventoryForge, slotNumber,  xpos, ypos));
            }
        }

        if (RainbowCrateTileEntity.NUM_SLOTS != chestContents.getSlots()) {
            LOGGER.warn("Mismatched slot count in RainbowCrateContainer(" + RainbowCrateTileEntity.NUM_SLOTS
                    + ") and TileInventory (" + chestContents.getSlots()+")");
        }

        final int TILE_INVENTORY_XPOS = 5;
        final int SLOT_X_SPACING = 18;
        final int SLOT_Y_SPACING = 21;
        // Add the tile inventory container to the gui
        for (int y =0; y < RainbowCrateTileEntity.ROWS; y++) {
            for (int x = 0; x < RainbowCrateTileEntity.COLUMNS; x++) {
                int slotNumber = y * RainbowCrateTileEntity.COLUMNS + x;
                addSlot(new SlotItemHandler(chestContents, slotNumber, TILE_INVENTORY_XPOS + SLOT_X_SPACING * x,
                        TILE_INVENTORY_YPOS + y * SLOT_Y_SPACING));
            }
        }
    }

    public static RainbowCrateContainer createContainerClientSide(
            RainbowColor tier, int windowId,
            PlayerInventory playerInventory, net.minecraft.network.PacketBuffer extraData) {
        // Server sends the position of the associated tile entity tracking the inventory. This is used to
        // query the internal inventory capacity in the UI
        BlockPos tileEntityPos = extraData.readBlockPos();
        TileEntity te = playerInventory.player.world.getTileEntity(tileEntityPos);
        RainbowCrateItemStackHandler inventory;
        if (te instanceof RainbowCrateTileEntity) {
            RainbowCrateTileEntity rcte = (RainbowCrateTileEntity)te;
            inventory = rcte.getInventory();
        } else {
            // TODO can I just return null here and expect that to do something meaningfully?
            LOGGER.warn("Tile entity no longer exists for rainbow crate?");
            inventory = RainbowCrateTileEntity.createInventory(tier);
        }
        return new RainbowCrateContainer(tier, windowId, playerInventory, inventory);
    }

    public RainbowColor getTier() {
        return this.tier;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true; // TODO proximity check etc
    }

    /**
     * Called when a player shift-clicks in the container GUI on any slot
     */
    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerEntity, int sourceSlotIndex)
    {
        Slot sourceSlot = inventorySlots.get(sourceSlotIndex);
        if (sourceSlot == null || !sourceSlot.getHasStack()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (!isCrateSlot(sourceSlotIndex)) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!mergeItemStack(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX,
                    TE_INVENTORY_FIRST_SLOT_INDEX + RainbowCrateTileEntity.NUM_SLOTS,
                    false)){
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else {
            // This is a TE slot so merge the stack into the players inventory
            if (!mergeItemStack(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        }

        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.putStack(ItemStack.EMPTY);
        } else {
            sourceSlot.onSlotChanged();
        }

        sourceSlot.onTake(playerEntity, sourceStack);
        return copyOfSourceStack;
    }

    /**
     * @return Slots in the crate, e.g. not player inventory or hot bar.
     */
    public Stream<Slot> getCrateSlots() {
        return this.inventorySlots.stream().filter(s -> isCrateSlot(s.slotNumber));
    }

    private boolean isCrateSlot(int slotIndex) {
        return slotIndex >= TE_INVENTORY_FIRST_SLOT_INDEX && slotIndex < TE_INVENTORY_FIRST_SLOT_INDEX + RainbowCrateTileEntity.NUM_SLOTS;
    }

    /**
     *
     * @return Per-slot item limit for the internal inventory slots (not the chest slots)
     */
    public int getInternalSlotLimit() {
        return this.chestContents.getInternalCapacity();
    }

    /** Rainbow crates have a secondary internal inventory - return the item count for the internal inventory's slot
     *
     * @param slotIndex
     * @return
     */
    public int getInternalSlotCount(int slotIndex) {
        if (!isCrateSlot(slotIndex)) {
            throw new RuntimeException("Requested slot count for non-crate slot index " + slotIndex);
        }
        int chestSlotIndex = slotIndex - TE_INVENTORY_FIRST_SLOT_INDEX;
        return this.chestContents.getInternalItemCount(chestSlotIndex);
    }
}
