package lonestarrr.arconia.common.crafting;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Register new recipe types for custom crafting methods
 */
public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Arconia.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Arconia.MOD_ID);
    public static final Supplier<RecipeType<PedestalRecipe>> PEDESTAL_TYPE = RECIPE_TYPES.register("pedestal", () -> RecipeType.<PedestalRecipe>simple(ResourceLocation.fromNamespaceAndPath(Arconia.MOD_ID, "pedestal"))); // Crafting with pedestals
    public static final Supplier<RecipeSerializer<PedestalRecipe>> PEDESTAL_SERIALIZER = RECIPE_SERIALIZERS.register("pedestal", PedestalRecipe.Serializer::new);
}
