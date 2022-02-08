package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.lib.tile.BaseTileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;

public class PotMultiBlockSecondaryTileEntity extends BaseTileEntity {
    BlockPos primaryPos;

    public PotMultiBlockSecondaryTileEntity() {
        super(ModTiles.POT_MULTIBLOCK_SECONDARY);
    }

    public void setPrimaryPos(@Nonnull final BlockPos primaryPos) {
        this.primaryPos = primaryPos;
        setChanged();
    }

    public BlockPos getPrimaryPos() {
        return primaryPos;
    }

    public void writePacketNBT(CompoundTag tag) {
        if (this.primaryPos != null) {
            tag.putLong("primaryPos", primaryPos.asLong());
        }
    }

    public void readPacketNBT(CompoundTag tag) {
        this.primaryPos = BlockPos.of(tag.getLong("primaryPos"));
    }

}
