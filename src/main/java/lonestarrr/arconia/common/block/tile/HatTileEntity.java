package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.InventoryHelper;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Hats linked to a pot of gold can produce a specific resource, and are tiered using RainbowColor. The tile entity stores the resource to be generated.
 */
public class HatTileEntity extends TileEntity {
    private RainbowColor tier;
    private BlockPos linkedPotPos;
    private ItemStack itemStack; // item to generate (should this be an ItemStack?)
    private int resourceGenInterval;
    private int resourceCoinCost;
    public long nextTickParticleRender = 0; // used by TE renderer to track particle rendering - not persisted

    public HatTileEntity() {
        super(ModTiles.HAT);
        this.tier = RainbowColor.RED;
        this.itemStack = ItemStack.EMPTY;
    }

    /**
     * @return Pot this hat is linked to, or null if not linked
     */
    public BlockPos getLinkedPot() {
        return this.linkedPotPos;
    }

    /**
     * Links hat to a pot of gold. No checks are performed to validate whether the pot is there and valid.
     * @param potPos Block position of the pot's multiblock's primary block
     */
    public void linkToPot(BlockPos potPos) {
        this.linkedPotPos = potPos;
        markDirty();
    }

    /**
     * Unlinks hat. No checks are performed whether the hat was already linked.
     */
    public void unlink() {
        this.linkedPotPos = null;
        markDirty();
    }

    public void setResourceGenerated(RainbowColor tier, ItemStack itemStack, int interval, int coinCost) {
        this.tier = tier;
        this.itemStack = itemStack.copy();
        this.resourceGenInterval = interval;
        this.resourceCoinCost = coinCost <= 0 ? 1: coinCost;
        markDirty();
    }

    @Nonnull
    public ItemStack getResourceGenerated() {
        return this.itemStack.copy(); //deals with isEmpty() smartly
    }

    public int getResourceGenInterval() {
        return resourceGenInterval;
    }

    public int getResourceCoinCost() { return resourceCoinCost; }

    public final ItemStack getItemStack() {
        return this.itemStack.copy();
    }

    public final RainbowColor getTier() { return this.tier; }

    /**
     * Attempt to have the hat generate the associated resource and insert it into an inventory below.
     * @return True if this is invoked on the server side, and a non-zero item count was inserted into the inventory below.
     */
    @Nonnull
    public ItemStack generateResource(World world) {
        if (world.isRemote) {
            return ItemStack.EMPTY;
        }

        ItemStack toGenerate = getItemStack();
        if (toGenerate.isEmpty()) {
            return ItemStack.EMPTY;
        }

        IItemHandler inv = InventoryHelper.getInventory(world, pos.down(), Direction.UP);
        if (inv == null) {
            return ItemStack.EMPTY;
        }

        ItemStack sent = toGenerate.copy();
        ItemStack left = InventoryHelper.insertItem(inv, toGenerate, false);
        sent.setCount(sent.getCount() - left.getCount());
        return sent;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        if (!world.isRemote()) {
            compound.putInt("tier", tier.getTier());
            compound.put("item", this.itemStack.serializeNBT());
            compound.putInt("interval", resourceGenInterval);
            compound.putInt("coin_cost", resourceCoinCost);
            if (this.linkedPotPos != null) {
                compound.putLong("pot_pos", this.linkedPotPos.toLong());
            }
        }
        return super.write(compound);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        ItemStack stack = ItemStack.EMPTY;
        RainbowColor tier = RainbowColor.RED;
        int interval = 1;
        int coinCost = 1;

        try {
            int tierNum = nbt.getInt("tier");
            for (RainbowColor clr: RainbowColor.values()) {
                if (clr.getTier() == tierNum) {
                    tier = clr;
                }
            }
            stack = ItemStack.read(nbt.getCompound("item"));
            if (!stack.isEmpty()) {
                interval = nbt.getInt("interval");
                coinCost = nbt.getInt("coin_cost");
            }

            if (nbt.contains("pot_pos")) {
                this.linkedPotPos = BlockPos.fromLong(nbt.getLong("pot_pos"));
            } else {
                this.linkedPotPos = null;
            }
            Arconia.logger.debug("***** World remote = " + (world != null ? world.isRemote() : "null") + ", itemStack = " + stack);
        } catch(Exception e) {
            Arconia.logger.error("Failed to read tile entity data: " + e.getMessage(), e);
        }
        setResourceGenerated(tier, stack, interval, coinCost <= 0 ? 1: coinCost);
        super.read(state, nbt);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        // sync server -> client, which needs the item to know how to render it in its tile entity renderer
        return this.write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        // Called on client to read server data
        read(state, tag);
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        CompoundNBT nbtTagCompound = getUpdateTag();
        int tileEntityType = -1;  // arbitrary number; only used for vanilla TileEntities.  You can use it, or not, as you want.
        return new SUpdateTileEntityPacket(this.pos, tileEntityType, nbtTagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(this.getBlockState(), pkt.getNbtCompound());
    }

}
