package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.common.block.entities.BaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Base BlockEntity that has an inventory
 */
public abstract class BaseInventoryBlockEntity extends BaseBlockEntity {
    private final Lazy<IItemHandler> itemHandler = Lazy.of(this::getInventory);

    public BaseInventoryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public IItemHandler getItemHandler() { return itemHandler.get(); }

    protected abstract ItemStackHandler getInventory();

    @Override
    public void writePacketNBT(CompoundTag tag) {
        tag.merge(getInventory().serializeNBT());
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
        getInventory().deserializeNBT(tag);
    }
}
