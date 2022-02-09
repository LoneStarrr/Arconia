package lonestarrr.arconia.common.crafting;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;

public interface IPedestalRecipe extends Recipe<Container> {
    ResourceLocation TYPE_ID = new ResourceLocation(Arconia.MOD_ID, "pedestal");

    @Nonnull
    @Override
    default RecipeType<?> getType() {
        return Registry.RECIPE_TYPE.getOptional(TYPE_ID).get();
    }

    @Override
    default boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    default boolean isSpecial() {
        return true;
    }

    int getDurationTicks();
}
