package lonestarrr.arconia.data;

import com.klikli_dev.modonomicon.api.datagen.LanguageProviderCache;
import com.klikli_dev.modonomicon.api.datagen.NeoBookProvider;
import com.klikli_dev.modonomicon.datagen.EnUsProvider;
import java.util.List;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.data.client.ArconiaModelProvider;
import lonestarrr.arconia.data.client.ModLanguageProvider;
import lonestarrr.arconia.data.loot.ArconiaLootTableProvider;
import lonestarrr.arconia.data.modonomicon.GuideBook;
import lonestarrr.arconia.data.recipes.ModRecipeProvider;
import lonestarrr.arconia.data.world.BiomeModifiers;
import net.minecraft.data.advancements.AdvancementProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Executed as part of the {@code runData} gradle task — run this once on a fresh checkout (and
 * after touching anything that affects generated data) to refresh files under {@code
 * src/generated/resources}, which are picked up automatically when the {@code runClient} task
 * launches.
 *
 * <p>1.21.4 NeoForge splits {@code GatherDataEvent} into {@code Server} and {@code Client}
 * subclasses. Per the 21.4 release-notes recommendation, we generate everything in a single {@code
 * clientData} run (configured in {@code build.gradle}) and listen only on the {@code Client}
 * variant — the {@code Server} variant is never fired in this build.
 */
public class DataGenerators {
  public static void gatherClientData(GatherDataEvent.Client event) {
    registerAll(event);
  }

  private static void registerAll(GatherDataEvent event) {
    event.createProvider((output, lookupProvider) -> new ModLanguageProvider(output, "en_us"));
    event.createProvider(ArconiaModelProvider::new);
    event.createProvider(ModRecipeProvider.Runner::new);
    event.createProvider(ModBlockTagsProvider::new);
    event.createProvider(BiomeModifiers::new);
    event.createProvider(ArconiaLootTableProvider::create);
    event.createProvider(ArconiaBiomeTagsProvider::new);
    event.createProvider(ModItemTagsProvider::new);

    event.createProvider(
        (output, lookupProvider) ->
            new AdvancementProvider(output, lookupProvider, List.of(new AdvancementSubProvider())));

    // Modonomicon
    var enUsCache = new LanguageProviderCache("en_us");
    event
        .getGenerator()
        .addProvider(true, NeoBookProvider.of(event, new GuideBook(Arconia.MOD_ID, enUsCache)));
    // Important: lang provider needs to be added after the book provider, so it can read the texts
    // added by the book provider out of the cache
    event
        .getGenerator()
        .addProvider(
            true,
            new EnUsProvider(
                event.getGenerator().getPackOutput(),
                enUsCache)); // E: error: cannot find symbol: EnUsProvider

    Arconia.logger.info(
        "**** DataGenerators executed (event class: {})", event.getClass().getSimpleName());
  }
}
