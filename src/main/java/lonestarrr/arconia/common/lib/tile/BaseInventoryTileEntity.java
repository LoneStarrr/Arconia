package lonestarrr.arconia.common.lib.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Base TileEntity that has an inventory
 */
public abstract class BaseInventoryTileEntity extends BaseTileEntity {
    private final LazyOptional<IItemHandler> capability = LazyOptional.of(this::getInventory);

    public BaseInventoryTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (!isRemoved() && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(this::getInventory));
        }

        return super.getCapability(cap, side);
    }

}
