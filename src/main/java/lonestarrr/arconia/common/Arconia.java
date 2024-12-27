package lonestarrr.arconia.common;

import lonestarrr.arconia.client.particle.ModParticles;
import lonestarrr.arconia.common.advancements.ModCriteriaTriggers;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.entities.ModBlockEntities;
import lonestarrr.arconia.common.block.entities.WorldBuilderEntity;
import lonestarrr.arconia.common.capabilities.ModCapabilities;
import lonestarrr.arconia.common.core.command.ArconiaCommand;
import lonestarrr.arconia.common.core.command.FractalTreeCommand;
import lonestarrr.arconia.common.core.handler.ConfigHandler;
import lonestarrr.arconia.common.core.helper.BlockPatternException;
import lonestarrr.arconia.common.core.helper.BuildPatternTier;
import lonestarrr.arconia.common.core.proxy.ClientProxy;
import lonestarrr.arconia.common.core.proxy.IProxy;
import lonestarrr.arconia.common.core.proxy.ServerProxy;
import lonestarrr.arconia.common.crafting.ModRecipeTypes;
import lonestarrr.arconia.common.item.ModItems;
import lonestarrr.arconia.common.loot.ModLootModifiers;
import lonestarrr.arconia.common.network.ModPackets;
import lonestarrr.arconia.compat.theoneprobe.TheOneProbe;
import lonestarrr.arconia.data.DataGenerators;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(Arconia.MOD_ID)
public class Arconia {
    // mod_id must also be updated in build.gradle
    public static final String MOD_ID = "arconia";

    public static volatile boolean configLoaded = false;

    public static final Logger logger = LogManager.getLogger(Arconia.MOD_ID);

    public static IProxy proxy;

    public Arconia(IEventBus modBus, Dist dist) {
        proxy = (dist == Dist.CLIENT ? new ClientProxy() : new ServerProxy());
        proxy.registerHandlers(modBus); // TODO get rid of the proxy pattern

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);

        modBus.addListener(this::commonSetup);
        modBus.addListener(DataGenerators::gatherData);

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModRecipeTypes.RECIPE_TYPES.register(modBus);
        ModRecipeTypes.RECIPE_SERIALIZERS.register(modBus);
        ModLootModifiers.CODECS.register(modBus);
        ModLootModifiers.LOOT_CONDITION_TYPES.register(modBus);
        ModParticles.PARTICLE_TYPES.register(modBus);
        ModCriteriaTriggers.CRITERIA_TRIGGERS.register(modBus);

        modBus.addListener(ConfigHandler::onConfigLoad);
        modBus.addListener(ConfigHandler::onConfigReload);

        modBus.addListener(ModBlocks::addToCreativeTabs);
        modBus.addListener(ModItems::addToCreativeTabs);
        modBus.addListener(ModCapabilities::registerCapabilities);
        modBus.addListener(ModPackets::registerPackets);

        IEventBus eventBus = NeoForge.EVENT_BUS;
        eventBus.addListener(EventPriority.HIGH, this::registerCommands);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        Arconia.logger.info("Running commonSetup");

        // !! This mod life cycle event is called in parallel with any other mods - use event.enqueueWork() for things that are not thread-safe.

        // The One Probe - optional, checks for mod presence
        TheOneProbe.init();

        try {
            BuildPatternTier.loadPatterns();
        } catch (IOException e) {
            throw new RuntimeException("Error loading build patterns", e);
        } catch (BlockPatternException e) {
            throw new RuntimeException("Error parsing build patterns", e);
        }

        try {
            WorldBuilderEntity.loadDistributionTables();
        } catch (IOException e) {
            throw new RuntimeException("Error loading world builder distribution tables", e);
        }
    }

    private void registerCommands(RegisterCommandsEvent event) {
        FractalTreeCommand.register(event.getDispatcher());
        ArconiaCommand.register(event.getDispatcher(), event.getBuildContext());
        logger.info("Registered commands");
    }
}
