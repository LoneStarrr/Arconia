package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.lib.tile.BaseTileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class PotMultiBlockSecondaryTileEntity extends BaseTileEntity {
    BlockPos primaryPos;

    public PotMultiBlockSecondaryTileEntity() {
        super(ModTiles.POT_MULTIBLOCK_SECONDARY);
    }

    public void setPrimaryPos(@Nonnull final BlockPos primaryPos) {
        this.primaryPos = primaryPos;
        markDirty();
    }

    public BlockPos getPrimaryPos() {
        return primaryPos;
    }

    public void writePacketNBT(CompoundNBT tag) {
        if (this.primaryPos != null) {
            tag.putLong("primaryPos", primaryPos.toLong());
        }
    }

    public void readPacketNBT(CompoundNBT tag) {
        this.primaryPos = BlockPos.fromLong(tag.getLong("primaryPos"));
    }

}
