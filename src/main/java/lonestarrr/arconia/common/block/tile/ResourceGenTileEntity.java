package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.lib.tile.BaseTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Tile Entity that stores a tier and type of resource to generate. Otherwise, this entity is passive. The resource generation magic happens in the
 * tile entity for the tree root block.
 */
public class ResourceGenTileEntity extends BaseTileEntity {
    private RainbowColor tier;
    private ItemStack itemStack; // item to generate (should this be an ItemStack?)
    public long nextTickParticleRender = 0; // used by TE renderer to track particle rendering - not persisted

    public ResourceGenTileEntity(BlockPos pos, BlockState state) {
        super(ModTiles.RESOURCEGEN, pos, state);
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
    public void writePacketNBT(CompoundTag tag) {
        if (!level.isClientSide()) {
            tag.putInt("tier", tier.getTier());
            tag.put("item", this.itemStack.serializeNBT());
        }
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
        try {
            int tierNum = tag.getInt("tier");
            for (RainbowColor clr: RainbowColor.values()) {
                if (clr.getTier() == tierNum) {
                    tier = clr;
                }
            }
            itemStack = ItemStack.of(tag.getCompound("item"));
            Arconia.logger.debug("***** World remote = " + (level != null ? level.isClientSide() : "null") + ", itemStack = " + itemStack);
        } catch(Exception e) {
            Arconia.logger.error("Failed to read tile entity data: " + e.getMessage(), e);
            tier = RainbowColor.RED;
            itemStack = ItemStack.EMPTY;
        }
    }
}
