package lonestarrr.arconia.common.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.ArrayList;
import java.util.List;

public class PedestalInput implements RecipeInput {
    private final List<ItemStack> items;

    public PedestalInput(List<ItemStack> items) {
        this.items = new ArrayList<>(items);
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }
}
