package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.lib.tile.BaseTileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public class PotMultiBlockPrimaryTileEntity extends BaseTileEntity {
    public PotMultiBlockPrimaryTileEntity() {
        super(ModTiles.POT_MULTIBLOCK_PRIMARY);
        // TODO check for valid structure at an interval, if not, destroy ourselves
    }

    public void writePacketNBT(CompoundNBT tag) {
    }

    public void readPacketNBT(CompoundNBT tag) {
    }

}
