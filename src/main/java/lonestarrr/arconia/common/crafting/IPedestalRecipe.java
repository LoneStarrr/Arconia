package lonestarrr.arconia.common.crafting;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import lonestarrr.arconia.common.Arconia;

import javax.annotation.Nonnull;

public interface IPedestalRecipe extends IRecipe<IInventory> {
    ResourceLocation TYPE_ID = new ResourceLocation(Arconia.MOD_ID, "pedestal");

    @Nonnull
    @Override
    default IRecipeType<?> getType() {
        return Registry.RECIPE_TYPE.getOptional(TYPE_ID).get();
    }

    @Override
    default boolean canFit(int width, int height) {
        return false;
    }

    @Override
    default boolean isDynamic() {
        return true;
    }

    int getDurationTicks();
}
