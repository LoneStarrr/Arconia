package lonestarrr.arconia.common.block.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PedestalTileEntity extends BasePedestalTileEntity {
    public PedestalTileEntity(BlockPos pos, BlockState state) {
        super(ModTiles.PEDESTAL, pos, state);
    }
}
