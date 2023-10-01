package lonestarrr.arconia.common.crafting;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.mixin.AccessorRecipeManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

/**
 * Register new recipe types for custom crafting methods
 */
public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Arconia.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Arconia.MOD_ID);
    public static final RegistryObject<RecipeType<IPedestalRecipe>> PEDESTAL_TYPE = RECIPE_TYPES.register("pedestal", () -> new ModRecipeType<>()); // Crafting with pedestals
    public static final RegistryObject<RecipeSerializer<PedestalRecipe>> PEDESTAL_SERIALIZER = RECIPE_SERIALIZERS.register("pedestal", () -> new PedestalRecipe.Serializer());

    public static <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> getRecipes(Level level, RecipeType<T> type) {
        // RecipeManger.byType() is private, custom mixin makes it available
        return ((AccessorRecipeManager) level.getRecipeManager()).arconia_getRecipes(type);
    }

    private static class ModRecipeType<T extends Recipe<?>> implements RecipeType<T> {
        @Override
        public String toString() {
            return Registry.RECIPE_TYPE.getKey(this).toString();
        }
    }

}
