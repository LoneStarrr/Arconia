package lonestarrr.arconia.client.integration.jei;

import lonestarrr.arconia.common.crafting.ModRecipeTypes;
import lonestarrr.arconia.common.crafting.PedestalRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import java.util.List;

/**
 * Client-side cache of {@link PedestalRecipe}s shipped from the server via NeoForge's recipe-sync
 * machinery (see {@link lonestarrr.arconia.common.network.PedestalRecipeSync}).
 * The JEI plugin reads from this cache when registering recipes.
 */
public final class ClientPedestalRecipes {
    private static volatile List<PedestalRecipe> recipes = List.of();

    private ClientPedestalRecipes() {}

    public static List<PedestalRecipe> getRecipes() {
        return recipes;
    }

    public static void onRecipesReceived(RecipesReceivedEvent event) {
        if (!event.getRecipeTypes().contains(ModRecipeTypes.PEDESTAL_TYPE.get())) {
            return;
        }
        recipes = event.getRecipeMap().byType(ModRecipeTypes.PEDESTAL_TYPE.get()).stream()
                .map(RecipeHolder::value)
                .toList();
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        recipes = List.of();
    }
}
