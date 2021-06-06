package lonestarrr.arconia.mixin;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

// Mixin generic guide: https://darkhax.net/2020/07/mixins
@Mixin(RecipeManager.class)
public interface AccessorRecipeManager {
    @Invoker("getRecipes")
    <C extends IInventory, T extends IRecipe<C>> Map<ResourceLocation, IRecipe<C>> arconia_getRecipes(IRecipeType<T> type);
}