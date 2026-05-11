package lonestarrr.arconia.data;

import java.util.List;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.data.client.ArconiaModelProvider;
import lonestarrr.arconia.data.client.ModLanguageProvider;
import lonestarrr.arconia.data.loot.ArconiaLootTableProvider;
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
    Arconia.logger.info(
        "**** DataGenerators executed (event class: {})", event.getClass().getSimpleName());
  }
}
