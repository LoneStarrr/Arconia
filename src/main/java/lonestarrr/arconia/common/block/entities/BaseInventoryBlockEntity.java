package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.common.block.entities.BaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Base BlockEntity that has an inventory
 */
public abstract class BaseInventoryBlockEntity extends BaseBlockEntity {
    private final LazyOptional<IItemHandler> capability = LazyOptional.of(this::getInventory);

    public BaseInventoryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract ItemStackHandler getInventory();

    @Override
    public void writePacketNBT(CompoundTag tag) {
        tag.merge(getInventory().serializeNBT());
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
        getInventory().deserializeNBT(tag);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (!isRemoved() && cap == ForgeCapabilities.ITEM_HANDLER) {
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(this::getInventory));
        }

        return super.getCapability(cap, side);
    }

}
