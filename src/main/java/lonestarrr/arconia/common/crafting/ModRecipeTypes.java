package lonestarrr.arconia.common.crafting;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.mixin.AccessorRecipeManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegistryEvent;

import java.util.Map;

/**
 * Register new recipe types for custom crafting methods
 */
public class ModRecipeTypes {
    public static final RecipeType<IPedestalRecipe> PEDESTAL_TYPE = RecipeType.register("arconia_pedestal"); // Crafting with pedestals
    public static final RecipeSerializer<PedestalRecipe> PEDESTAL_SERIALIZER = new PedestalRecipe.Serializer();

    public static void registerRecipeTypes(RegistryEvent.Register<RecipeSerializer<?>> event) {
        ResourceLocation id = new ResourceLocation(Arconia.MOD_ID, "pedestal");
        Registry.register(Registry.RECIPE_TYPE, id, PEDESTAL_TYPE);
        event.getRegistry().register(PEDESTAL_SERIALIZER.setRegistryName(id));


    }

    public static <C extends Container, T extends Recipe<C>> Map<ResourceLocation, Recipe<C>> getRecipes(Level world, RecipeType<T> type) {
        // RecipeManger.byType() is private, custom mixin makes it available
        return ((AccessorRecipeManager) world.getRecipeManager()).arconia_getRecipes(type);
    }
}
