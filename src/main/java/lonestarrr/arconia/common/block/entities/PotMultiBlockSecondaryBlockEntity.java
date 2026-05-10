package lonestarrr.arconia.common.block.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class PotMultiBlockSecondaryBlockEntity extends BaseBlockEntity {
    BlockPos primaryPos;

    public PotMultiBlockSecondaryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POT_MULTIBLOCK_SECONDARY.get(), pos, state);
    }

    public void setPrimaryPos(@Nonnull final BlockPos primaryPos) {
        this.primaryPos = primaryPos;
        setChanged();
    }

    public BlockPos getPrimaryPos() {
        return primaryPos;
    }

    public void writePacketNBT(@NotNull ValueOutput output) {
        if (this.primaryPos != null) {
            output.putLong("primaryPos", primaryPos.asLong());
        }
    }

    public void readPacketNBT(@NotNull ValueInput input) {
        this.primaryPos = BlockPos.of(input.getLongOr("primaryPos", 0L));
    }

}
