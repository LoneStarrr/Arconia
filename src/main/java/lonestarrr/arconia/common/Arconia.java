package lonestarrr.arconia.common;

import lonestarrr.arconia.common.advancements.ModCriterialTriggers;
import lonestarrr.arconia.common.core.command.ArconiaCommand;
import lonestarrr.arconia.common.loot.DirtLootModifier;
import lonestarrr.arconia.common.loot.ModLootModifiers;
import lonestarrr.arconia.compat.theoneprobe.TheOneProbe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lonestarrr.arconia.common.core.command.FractalTreeCommand;
import lonestarrr.arconia.common.core.helper.BlockPatternException;
import lonestarrr.arconia.common.core.helper.BuildPatternTier;
import lonestarrr.arconia.common.core.proxy.ClientProxy;
import lonestarrr.arconia.common.core.proxy.IProxy;
import lonestarrr.arconia.common.core.proxy.ServerProxy;
import lonestarrr.arconia.common.network.ModPackets;
import lonestarrr.arconia.common.world.ModFeatures;

import java.io.IOException;

@Mod("arconia")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Arconia {
    // mod_id must also be updated in build.gradle
    public static final String MOD_ID = "arconia";
    public static final String MOD_PACKAGE = "lonestarrr.arconia";

    static final String NAME = "Arconia";

    public static final Logger logger = LogManager.getLogger(Arconia.MOD_ID);

    public static IProxy proxy = DistExecutor.runForDist(
            ()-> ClientProxy::new, ()-> ServerProxy::new
    );

    public Arconia() {
        proxy.registerHandlers();

        // Events for buses other than mod  bus
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(EventPriority.HIGH, this::biomeSetup);
        forgeBus.addListener(EventPriority.HIGH, this::registerCommands);

        // Mod bus Registries - starting to use those in favor of the SubscribeEvent annotation which mostly just makes it harder to track
        // where things are being registered and do not allow static initialization
        ModLootModifiers.LOOT_MODIFIERS.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModLootModifiers.init();
        ModCriterialTriggers.init();
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        Arconia.logger.info("Running commonSetup");
        // Register network packets to synchronize server/client data
        ModPackets.init();
        // The One Probe - optional, checks for mod presence
        TheOneProbe.init();

        try {
            BuildPatternTier.loadPatterns();
        } catch (IOException e) {
            throw new RuntimeException("Error loading build patterns", e);
        } catch (BlockPatternException e) {
            throw new RuntimeException("Error parsing build patterns", e);
        }
    }

    private void biomeSetup(BiomeLoadingEvent event) {
        ModFeatures.onBiomeLoad(event);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        FractalTreeCommand.register(event.getDispatcher());
        ArconiaCommand.register(event.getDispatcher());
        logger.info("Registered commands");
    }
}
