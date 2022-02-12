package lonestarrr.arconia.common;

import lonestarrr.arconia.common.advancements.ModCriterialTriggers;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.RainbowCrateBlock;
import lonestarrr.arconia.common.block.tile.ModBlockEntities;
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
import lonestarrr.arconia.common.world.ModFeatures;
import lonestarrr.arconia.compat.theoneprobe.TheOneProbe;
import lonestarrr.arconia.data.DataGenerators;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(Arconia.MOD_ID)
public class Arconia {
    // mod_id must also be updated in build.gradle
    public static final String MOD_ID = "arconia";

    public static volatile boolean configLoaded = false;

    public static final Logger logger = LogManager.getLogger(Arconia.MOD_ID);

    public static IProxy proxy = DistExecutor.runForDist(
            ()-> ClientProxy::new, ()-> ServerProxy::new
    );

    public Arconia() {
        proxy.registerHandlers();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        modBus.addListener(DataGenerators::gatherData);

        modBus.addGenericListener(Block.class, ModBlocks::registerBlocks);
        modBus.addGenericListener(Item.class, ModBlocks::registerItemBlocks);
        modBus.addGenericListener(Item.class, ModItems::registerItems);
        modBus.addGenericListener(BlockEntityType.class, ModBlockEntities::registerBlockEntities);
        modBus.addGenericListener(MenuType.class, RainbowCrateBlock::registerContainers);
        modBus.addGenericListener(RecipeSerializer.class, ModRecipeTypes::registerRecipeTypes);
        modBus.addGenericListener(Feature.class, ModFeatures::registerFeatures);

        modBus.addListener(ConfigHandler::onConfigLoad);
        modBus.addListener(ConfigHandler::onConfigReload);

        ModLootModifiers.LOOT_MODIFIERS.register(modBus);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(EventPriority.HIGH, this::biomeSetup);
        forgeBus.addListener(EventPriority.HIGH, this::registerCommands);

        ModLootModifiers.init();
        ModCriterialTriggers.init();
    }

    public void commonSetup(FMLCommonSetupEvent event) {
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
