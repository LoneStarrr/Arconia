package lonestarrr.arconia.common.network;

import lonestarrr.arconia.common.crafting.ModRecipeTypes;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

/**
 * Server-side trigger that asks NeoForge to ship our pedestal recipes to clients on player login
 * and on /reload. The vanilla 1.21.4 client only receives recipe display data, so JEI has no way
 * to enumerate {@link lonestarrr.arconia.common.crafting.PedestalRecipe} without this opt-in.
 */
public final class PedestalRecipeSync {
    private PedestalRecipeSync() {}

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(ModRecipeTypes.PEDESTAL_TYPE.get());
    }
}
