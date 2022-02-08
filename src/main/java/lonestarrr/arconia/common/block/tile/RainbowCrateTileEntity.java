package lonestarrr.arconia.common.block.tile;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.client.gui.crate.RainbowCrateContainer;
import lonestarrr.arconia.common.block.tile.crate.RainbowCrateItemStackHandler;
import lonestarrr.arconia.common.network.ModPackets;
import lonestarrr.arconia.common.network.RainbowCratePacket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Tile entity for rainbow crates, managing the crate's inventory.
 */
public class RainbowCrateTileEntity extends BlockEntity implements MenuProvider, TickableBlockEntity {
    public static final int ROWS = 8;
    public static final int COLUMNS = 13;
    public static final int NUM_SLOTS = ROWS * COLUMNS; // TODO This should be tiered - have fun refactoring
    private static final Map<RainbowColor, Integer> slotLimitPerTier = new HashMap<>(7);

    private final RainbowCrateItemStackHandler inventory;
    private final LazyOptional<ItemStackHandler> itemCap;
    private final RainbowColor tier;

    private int tickCount = 0;
    private int ticksSinceLastChange = 0;

    static {
        // Each crate's slot can actually contain more than 64 items. The higher the tier, the higher the max.
        // TODO: How about an "infinity" tier? Would be a cool reward for whatever 'end game' thing I can come up with
        int capacity = 64;
        for (RainbowColor tier: RainbowColor.values()) {
                slotLimitPerTier.put(tier, capacity);
                capacity *= 4;
        }
    }

    public RainbowCrateTileEntity(RainbowColor tier) {
        super(ModTiles.getRainbowCrateTileEntityType(tier));
        this.tier = tier;
        this.inventory = createInventory(tier);
        this.itemCap = LazyOptional.of(() -> inventory);
    }

    public static RainbowCrateItemStackHandler createInventory(RainbowColor tier) {
        return new RainbowCrateItemStackHandler(NUM_SLOTS, slotLimitPerTier.get(tier));
    }

    public RainbowCrateItemStackHandler getInventory() {
        // Used by client GUI to render the hidden inventory capacity
        return inventory;
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound.put("inventory", inventory.serializeNBT());
        return super.save(compound);
    }

    @Override
    public void load(BlockState state, CompoundTag compound) {
        inventory.deserializeNBT(compound.getCompound("inventory"));
        super.load(state, compound);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemCap.cast();
        }
        return super.getCapability(cap, side);
    }

    public void dropAllContents(Level world, BlockPos pos) {
        // TODO implement me - that will be fun with giant stacks, so best to instead preserve inventory through nbt data on the itemstack
    }

    // The following two methods are used to make the TileEntity perform as a NamedContainerProvider, i.e.
    //  1) Provide a name used when displaying the container, and
    //  2) Creating an instance of container on the server, and linking it to the inventory items stored within the TileEntity

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container." + Arconia.MOD_ID + ".rainbow_crate");
    }

    /**
     * Creates a temporary container that is only used to support the player UI with the combined inventories of
     * the player and the crate.
     * @param windowID
     * @param playerInventory
     * @param playerEntity
     * @return
     */
    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInventory, Player playerEntity) {
        return new RainbowCrateContainer(tier, windowID, playerInventory, inventory);
    }

    @Override
    public void tick() {
        // Only manage the inventory on server worlds - data required on the client side is sent through network
        // packets
        if (level.isClientSide()) {
            return;
        }

        // Increase time between ticks if no changes are detected - and vice versa - lag friendly and a better UX
        final int maxTicks = 20;
        this.ticksSinceLastChange++;
        int doWorkAt = Math.min(ticksSinceLastChange / 2, maxTicks);
        if (++(this.tickCount) >= doWorkAt) {
            this.tickCount = 0;
            // Send internal inventory data to relevant clients as that data is not synced as part of the standard
            // UI
            // TODO Can this be smarter? E.g. only send it to client worlds that have the UI open?
            boolean updates = this.inventory.tick();
            if (updates) {
                CompoundTag data = new CompoundTag();
                data = this.save(data);
                ModPackets.sendToNearby(level, worldPosition, new RainbowCratePacket(worldPosition, data));
                ticksSinceLastChange = 0;
            }
        }
    }

    /**
     * Data from the server world is synchronized to client worlds of nearby players to render the UI. This is
     * required as the inventory contains additional information that is not already synced as part of the Container
     * being used to render the UI.
     *
     * @param internalItemCounts Item counts, per slot, of the hidden internal inventory
     */
    public void receiveServerSideInventoryData(int[] internalItemCounts) {
        if (level.isClientSide()) {
            this.inventory.receiveServerSideInventoryData(internalItemCounts);
        }
    }
    // Network code below is to sync the server TileEntity data to the client

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        CompoundTag nbtTagCompound = new CompoundTag();
        save(nbtTagCompound);
        int tileEntityType = 42;  // arbitrary number; only used for vanilla TileEntities.  You can use it, or not, as you want.
        return new ClientboundBlockEntityDataPacket(this.worldPosition, tileEntityType, nbtTagCompound);
    }

    // I thought this would be triggered whenever the inv is updated (e.g. hopper) but nope..
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(level.getBlockState(pkt.getPos()), pkt.getTag());
    }

    /* Creates a tag containing all of the TileEntity information, used by vanilla to transmit from server to client
     */
    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag nbtTagCompound = new CompoundTag();
        save(nbtTagCompound);
        return nbtTagCompound;
    }

    // Triggered when loading the world (and probably chunk loading)
    @Override
    public void handleUpdateTag(BlockState state, CompoundTag tag)
    {
        load(state, tag);
    }

}
