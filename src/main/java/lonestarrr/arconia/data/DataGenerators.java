package lonestarrr.arconia.data;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.data.client.ModBlockStateProvider;
import lonestarrr.arconia.data.client.ModItemModelProvider;
import lonestarrr.arconia.data.client.ModLanguageProvider;
import lonestarrr.arconia.data.loot.ModLootTableProvider;
import lonestarrr.arconia.data.recipes.PedestalProvider;
import lonestarrr.arconia.data.recipes.VanillaRecipeProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

/**
 * Executed as part of runData gradle task - need to run this once on a fresh checkout to generate data files which are used automatically when executing
 * the runClient task.
 */
public class DataGenerators {
    public static void gatherData(GatherDataEvent evt) {
        ExistingFileHelper helper = evt.getExistingFileHelper();
        if (evt.includeServer()) {
            evt.getGenerator().addProvider(new PedestalProvider(evt.getGenerator()));
            evt.getGenerator().addProvider(new VanillaRecipeProvider(evt.getGenerator()));
            evt.getGenerator().addProvider(new ModBlockTagsProvider(evt.getGenerator(), helper));
            evt.getGenerator().addProvider(new ModLootTableProvider(evt.getGenerator()));
        }
        if (evt.includeClient()) {
            evt.getGenerator().addProvider(new ModBlockStateProvider(evt.getGenerator(), helper));
            evt.getGenerator().addProvider(new ModItemModelProvider(evt.getGenerator(), helper));
            evt.getGenerator().addProvider(new ModLanguageProvider(evt.getGenerator(), "en_us"));
            // AdvancementProvider exists, but it's hardcoded for vanilla advancements only
        }
        Arconia.logger.info("**** DataGenerators executed");
    }
}
