package lonestarrr.arconia.common.block.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
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

    public void writePacketNBT(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        if (this.primaryPos != null) {
            tag.putLong("primaryPos", primaryPos.asLong());
        }
    }

    public void readPacketNBT(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        this.primaryPos = BlockPos.of(tag.getLong("primaryPos"));
    }

}
