package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.InventoryHelper;
import lonestarrr.arconia.common.lib.tile.BaseTileEntity;
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
public class HatTileEntity extends BaseTileEntity {
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
    public void writePacketNBT(CompoundNBT tag) {
        if (!world.isRemote()) {
            tag.putInt("tier", tier.getTier());
            tag.put("item", this.itemStack.serializeNBT());
            tag.putInt("interval", resourceGenInterval);
            tag.putInt("coin_cost", resourceCoinCost);
            if (this.linkedPotPos != null) {
                tag.putLong("pot_pos", this.linkedPotPos.toLong());
            }
        }
    }

    @Override
    public void readPacketNBT(CompoundNBT tag) {
        ItemStack stack = ItemStack.EMPTY;
        RainbowColor tier = RainbowColor.RED;
        int interval = 1;
        int coinCost = 1;

        try {
            int tierNum = tag.getInt("tier");
            for (RainbowColor clr: RainbowColor.values()) {
                if (clr.getTier() == tierNum) {
                    tier = clr;
                }
            }
            stack = ItemStack.read(tag.getCompound("item"));
            if (!stack.isEmpty()) {
                interval = tag.getInt("interval");
                coinCost = tag.getInt("coin_cost");
            }

            if (tag.contains("pot_pos")) {
                this.linkedPotPos = BlockPos.fromLong(tag.getLong("pot_pos"));
            } else {
                this.linkedPotPos = null;
            }
            Arconia.logger.debug("***** World remote = " + (world != null ? world.isRemote() : "null") + ", itemStack = " + stack);
        } catch(Exception e) {
            Arconia.logger.error("Failed to read tile entity data: " + e.getMessage(), e);
        }
        setResourceGenerated(tier, stack, interval, coinCost <= 0 ? 1: coinCost);
    }
}
