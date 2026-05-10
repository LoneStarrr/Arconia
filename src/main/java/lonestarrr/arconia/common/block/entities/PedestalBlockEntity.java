package lonestarrr.arconia.common.block.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class PedestalBlockEntity extends BasePedestalBlockEntity {

    private final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(1) {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
            updateClient();
        }

        @Override
        protected int getCapacity(int index, ItemResource resource) {
            return 1;
        }
    };

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PEDESTAL.get(), pos, state);
    }

    @Override
    protected ItemStacksResourceHandler getInventory() {
        return inventory;
    }
}
