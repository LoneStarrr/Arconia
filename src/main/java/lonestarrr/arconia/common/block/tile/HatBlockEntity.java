package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.InventoryHelper;
import lonestarrr.arconia.common.lib.tile.BaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

/**
 * Hats linked to a pot of gold can produce a specific resource, and are tiered using RainbowColor. The tile entity stores the resource to be generated.
 */
public class HatBlockEntity extends BaseBlockEntity {
    private RainbowColor tier;
    private BlockPos linkedPotPos;
    private ItemStack itemStack; // item to generate (should this be an ItemStack?)
    private int resourceGenInterval;
    private int resourceCoinCost;
    public long nextTickParticleRender = 0; // used by TE renderer to track particle rendering - not persisted

    public HatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HAT, pos, state);
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
        setChanged();
    }

    /**
     * Unlinks hat. No checks are performed whether the hat was already linked.
     */
    public void unlink() {
        this.linkedPotPos = null;
        setChanged();
    }

    public void setResourceGenerated(RainbowColor tier, ItemStack itemStack, int interval, int coinCost) {
        this.tier = tier;
        this.itemStack = itemStack.copy();
        this.resourceGenInterval = interval;
        this.resourceCoinCost = coinCost <= 0 ? 1: coinCost;
        setChanged();
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
    public ItemStack generateResource(Level world) {
        if (world.isClientSide) {
            return ItemStack.EMPTY;
        }

        ItemStack toGenerate = getItemStack();
        if (toGenerate.isEmpty()) {
            return ItemStack.EMPTY;
        }

        IItemHandler inv = InventoryHelper.getInventory(world, worldPosition.below(), Direction.UP);
        if (inv == null) {
            return ItemStack.EMPTY;
        }

        ItemStack sent = toGenerate.copy();
        ItemStack left = InventoryHelper.insertItem(inv, toGenerate, false);
        sent.setCount(sent.getCount() - left.getCount());
        return sent;
    }

    @Override
    public void writePacketNBT(CompoundTag tag) {
        if (!level.isClientSide()) {
            tag.putInt("tier", tier.getTier());
            tag.put("item", this.itemStack.serializeNBT());
            tag.putInt("interval", resourceGenInterval);
            tag.putInt("coin_cost", resourceCoinCost);
            if (this.linkedPotPos != null) {
                tag.putLong("pot_pos", this.linkedPotPos.asLong());
            }
        }
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
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
            stack = ItemStack.of(tag.getCompound("item"));
            if (!stack.isEmpty()) {
                interval = tag.getInt("interval");
                coinCost = tag.getInt("coin_cost");
            }

            if (tag.contains("pot_pos")) {
                this.linkedPotPos = BlockPos.of(tag.getLong("pot_pos"));
            } else {
                this.linkedPotPos = null;
            }
            Arconia.logger.debug("***** World remote = " + (level != null ? level.isClientSide() : "null") + ", itemStack = " + stack);
        } catch(Exception e) {
            Arconia.logger.error("Failed to read tile entity data: " + e.getMessage(), e);
        }
        setResourceGenerated(tier, stack, interval, coinCost <= 0 ? 1: coinCost);
    }
}
