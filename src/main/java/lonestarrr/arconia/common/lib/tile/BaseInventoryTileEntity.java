package lonestarrr.arconia.common.lib.tile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
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

    public BaseInventoryTileEntity(TileEntityType<?> type) {
        super(type);
    }

    public abstract ItemStackHandler getInventory();

    @Override
    public void writePacketNBT(CompoundNBT tag) {
        tag.merge(getInventory().serializeNBT());
    }

    @Override
    public void readPacketNBT(CompoundNBT tag) {
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
