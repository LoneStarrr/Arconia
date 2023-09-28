package lonestarrr.arconia.data;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.data.client.ModBlockStateProvider;
import lonestarrr.arconia.data.client.ModItemModelProvider;
import lonestarrr.arconia.data.client.ModLanguageProvider;
import lonestarrr.arconia.data.recipes.PedestalProvider;
import lonestarrr.arconia.data.recipes.VanillaRecipeProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

/**
 * Executed as part of runData gradle task - need to run this once on a fresh checkout to generate data files which are used automatically when executing
 * the runClient task.
 */
public class DataGenerators {
    public static void gatherData(GatherDataEvent event) {
        ExistingFileHelper helper = event.getExistingFileHelper();
        if (event.includeServer()) {
            event.getGenerator().addProvider(event.includeServer(), new PedestalProvider(event.getGenerator()));
            event.getGenerator().addProvider(event.includeServer(), new VanillaRecipeProvider(event.getGenerator()));
            event.getGenerator().addProvider(event.includeServer(), new ModBlockTagsProvider(event.getGenerator(), helper));
        }
        if (event.includeClient()) {
            event.getGenerator().addProvider(event.includeServer(), new ModBlockStateProvider(event.getGenerator(), helper));
            event.getGenerator().addProvider(event.includeServer(), new ModItemModelProvider(event.getGenerator(), helper));
            event.getGenerator().addProvider(event.includeServer(), new ModLanguageProvider(event.getGenerator(), "en_us"));
            // AdvancementProvider exists, but it's hardcoded for vanilla advancements only
        }
        Arconia.logger.info("**** DataGenerators executed");
    }
}
