package lonestarrr.arconia.common.crafting;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.mixin.AccessorRecipeManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Register new recipe types for custom crafting methods
 */
public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Arconia.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Arconia.MOD_ID);
//    public static final RegistryObject<RecipeType<IPedestalRecipe>> PEDESTAL_TYPE = RECIPE_TYPES.register("pedestal", () -> new ModRecipeType<>()); // Crafting with pedestals
    public static final Supplier<RecipeSerializer<PedestalRecipe>> PEDESTAL_SERIALIZER = RECIPE_SERIALIZERS.register("pedestal", PedestalRecipe.Serializer::new);

    public static <C extends Container, T extends Recipe<C>> Map<ResourceLocation, RecipeHolder<T>> getRecipes(Level level, RecipeType<T> type) {
        // RecipeManger.byType() is private, custom mixin makes it available
        return ((AccessorRecipeManager) level.getRecipeManager()).arconia_getRecipes(type);
    }
}
