package lonestarrr.arconia.data;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.data.client.ModBlockStateProvider;
import lonestarrr.arconia.data.client.ModItemModelProvider;
import lonestarrr.arconia.data.client.ModLanguageProvider;
import lonestarrr.arconia.data.loot.ArconiaLootTableProvider;
import lonestarrr.arconia.data.recipes.ModRecipeProvider;
import lonestarrr.arconia.data.world.BiomeModifiers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Executed as part of runData gradle task - need to run this once on a fresh checkout to generate data files which are used automatically when executing
 * the runClient task.
 */
public class DataGenerators {
    public static void gatherData(GatherDataEvent event) {
        ExistingFileHelper helper = event.getExistingFileHelper();
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();


        if (event.includeServer()) {
            gen.addProvider(event.includeServer(), new ModRecipeProvider(output, event.getLookupProvider()));
            gen.addProvider(event.includeServer(), new ModBlockTagsProvider(output, lookupProvider, helper));
            gen.addProvider(event.includeServer(), new BiomeModifiers(output, lookupProvider));
            gen.addProvider(event.includeServer(), ArconiaLootTableProvider.create(output, lookupProvider));
            gen.addProvider(event.includeServer(), new AdvancementProvider(output, lookupProvider, helper, List.of(new AdvancementSubProvider())));

        }
        if (event.includeClient()) {
            gen.addProvider(event.includeServer(), new ModBlockStateProvider(output, helper));
            gen.addProvider(event.includeServer(), new ModItemModelProvider(output, helper));
            gen.addProvider(event.includeServer(), new ModLanguageProvider(output, "en_us"));
            // AdvancementProvider exists, but it's hardcoded for vanilla advancements only
        }
        Arconia.logger.info("**** DataGenerators executed");
    }
}
