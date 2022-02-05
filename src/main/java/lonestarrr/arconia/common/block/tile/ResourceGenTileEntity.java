package lonestarrr.arconia.common.block.tile;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;

import javax.annotation.Nullable;

/**
 * Tile Entity that stores a tier and type of resource to generate. Otherwise, this entity is passive. The resource generation magic happens in the
 * tile entity for the tree root block.
 */
public class ResourceGenTileEntity extends TileEntity {
    private RainbowColor tier;
    private ItemStack itemStack; // item to generate (should this be an ItemStack?)
    public long nextTickParticleRender = 0; // used by TE renderer to track particle rendering - not persisted

    public ResourceGenTileEntity() {
        super(ModTiles.RESOURCEGEN);
        this.tier = RainbowColor.RED;
        this.itemStack = ItemStack.EMPTY;
    }

    public void setTierAndItem(RainbowColor tier, ItemStack itemStack) {
        this.tier = tier;
        this.itemStack = itemStack.copy();
        setChanged();
    }

    public final ItemStack getItemStack() {
        return this.itemStack.copy();
    }

    public final RainbowColor getTier() { return this.tier; }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        if (!level.isClientSide()) {
            compound.putInt("tier", tier.getTier());
            compound.put("item", this.itemStack.serializeNBT());
        }
        return super.save(compound);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        try {
            int tierNum = nbt.getInt("tier");
            for (RainbowColor clr: RainbowColor.values()) {
                if (clr.getTier() == tierNum) {
                    tier = clr;
                }
            }
            itemStack = ItemStack.of(nbt.getCompound("item"));
            Arconia.logger.debug("***** World remote = " + (level != null ? level.isClientSide() : "null") + ", itemStack = " + itemStack);
        } catch(Exception e) {
            Arconia.logger.error("Failed to read tile entity data: " + e.getMessage(), e);
            tier = RainbowColor.RED;
            itemStack = ItemStack.EMPTY;
        }
        super.load(state, nbt);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        // sync server -> client, which needs the item to know how to render it in its tile entity renderer
        return this.save(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        // Called on client to read server data
        load(state, tag);
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        CompoundNBT nbtTagCompound = getUpdateTag();
        int tileEntityType = -1;  // arbitrary number; only used for vanilla TileEntities.  You can use it, or not, as you want.
        return new SUpdateTileEntityPacket(this.worldPosition, tileEntityType, nbtTagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(this.getBlockState(), pkt.getTag());
    }

}
