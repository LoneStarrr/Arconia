package lonestarrr.arconia.data;

import lonestarrr.arconia.data.client.ModItemModelProvider;
import lonestarrr.arconia.data.recipes.VanillaRecipeProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.data.client.ModBlockStateProvider;
import lonestarrr.arconia.data.recipes.PedestalProvider;

/**
 * Executed as part of runData gradle task - need to run this once on a fresh checkout to generate data files which are used automatically when executing
 * the runClient task.
 */
@Mod.EventBusSubscriber(modid = Arconia.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent evt) {
        ExistingFileHelper helper = evt.getExistingFileHelper();
        if (evt.includeServer()) {
            evt.getGenerator().addProvider(new PedestalProvider(evt.getGenerator()));
            evt.getGenerator().addProvider(new VanillaRecipeProvider(evt.getGenerator()));
        }
        if (evt.includeClient()) {
            evt.getGenerator().addProvider(new ModBlockStateProvider(evt.getGenerator(), helper));
            evt.getGenerator().addProvider(new ModItemModelProvider(evt.getGenerator(), helper));
        }
            Arconia.logger.info("**** DataGenerators executed");
    }
}
