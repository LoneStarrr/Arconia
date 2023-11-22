package lonestarrr.arconia.common.block.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class PedestalBlockEntity extends BasePedestalBlockEntity {

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            updateClient();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PEDESTAL.get(), pos, state);
    }

    @Override
    public ItemStackHandler getInventory() {
        return inventory;
    }
}
