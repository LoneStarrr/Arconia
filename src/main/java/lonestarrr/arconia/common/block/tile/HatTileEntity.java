package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.GoldArconiumBlock;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.InventoryHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
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
    private ItemStack itemStack; // item to generate (should this be an ItemStack?)
    private int resourceGenInterval;
    private long lastGenTime;
    public long nextTickParticleRender = 0; // used by TE renderer to track particle rendering - not persisted

    public HatTileEntity() {
        super(ModTiles.HAT);
        this.tier = RainbowColor.RED;
        this.itemStack = ItemStack.EMPTY;
    }

    public void setResourceGenerated(RainbowColor tier, ItemStack itemStack, int interval) {
        this.tier = tier;
        this.itemStack = itemStack.copy();
        this.resourceGenInterval = interval;
        this.lastGenTime = 0;
        markDirty();
    }

    /**
     * @return Whether a resource to be generated has been associated with the hat
     */
    public boolean hasResourceGenerator() {
        return !this.itemStack.isEmpty();
    }

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
        }
        return super.write(compound);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        ItemStack stack = ItemStack.EMPTY;
        RainbowColor tier = RainbowColor.RED;
        int interval = 1;

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
            }
            Arconia.logger.debug("***** World remote = " + (world != null ? world.isRemote() : "null") + ", itemStack = " + stack);
        } catch(Exception e) {
            Arconia.logger.error("Failed to read tile entity data: " + e.getMessage(), e);
        }
        setResourceGenerated(tier, stack, interval);
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
