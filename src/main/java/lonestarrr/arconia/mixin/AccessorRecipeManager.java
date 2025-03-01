package lonestarrr.arconia.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collections;
import java.util.Map;

// Mixin generic guide: https://darkhax.net/2020/07/mixins
@Mixin(RecipeManager.class)
public interface AccessorRecipeManager {
    @Invoker("byType")
    <C extends Container, T extends Recipe<C>> Map<ResourceLocation, RecipeHolder<T>> arconia_getRecipes(RecipeType<T> type);
}