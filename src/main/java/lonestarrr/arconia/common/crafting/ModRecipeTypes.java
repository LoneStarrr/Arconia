package lonestarrr.arconia.common.crafting;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.mixin.AccessorRecipeManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;

import java.util.Map;

/**
 * Register new recipe types for custom crafting methods
 */
public class ModRecipeTypes {
    public static final IRecipeType<IPedestalRecipe> PEDESTAL_TYPE = new RecipeType<>(); // Crafting with pedestals
    public static final IRecipeSerializer<PedestalRecipe> PEDESTAL_SERIALIZER = new PedestalRecipe.Serializer();


    public static void registerRecipeTypes(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        ResourceLocation id = new ResourceLocation(Arconia.MOD_ID, "pedestal");
        Registry.register(Registry.RECIPE_TYPE, id, PEDESTAL_TYPE);
        event.getRegistry().register(PEDESTAL_SERIALIZER.setRegistryName(id));


    }

    private static class RecipeType<T extends IRecipe<?>> implements IRecipeType<T> {
        @Override
        public String toString() {
            return Registry.RECIPE_TYPE.getKey(this).toString();
        }
    }

    public static <C extends IInventory, T extends IRecipe<C>> Map<ResourceLocation, IRecipe<C>> getRecipes(World world, IRecipeType<T> type) {
        // RecipeManger.getRecipes() is private, custom mixin makes it available
        return ((AccessorRecipeManager) world.getRecipeManager()).arconia_getRecipes(type);
    }


}
