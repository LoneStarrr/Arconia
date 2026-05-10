package lonestarrr.arconia.common.block.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Base BlockEntity that has an inventory
 */
public abstract class BaseInventoryBlockEntity extends BaseBlockEntity {
    private final Lazy<ResourceHandler<ItemResource>> itemHandler = Lazy.of(this::getInventory);

    public BaseInventoryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ResourceHandler<ItemResource> getItemHandler() { return itemHandler.get(); }

    protected abstract ItemStacksResourceHandler getInventory();

    @Override
    public void writePacketNBT(@NotNull ValueOutput output) {
        getInventory().serialize(output);
    }

    @Override
    public void readPacketNBT(@NotNull ValueInput input) {
        getInventory().deserialize(input);
    }
}
