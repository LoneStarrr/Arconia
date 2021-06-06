package lonestarrr.arconia.common;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        Arconia.logger.info("Running commonSetup");
        // Register network packets to synchronize server/client data
        ModPackets.init();

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
        logger.info("Registered commands");
    }

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        // TODO This needs some more work - also put block names in a constant like botania does
        // https://github.com/Vazkii/Botania/search?q=TileEnderEye&unscoped_q=TileEnderEye
        // https://mcforge.readthedocs.io/en/1.14.x/tileentities/tileentity/
        /*
        IForgeRegistry<TileEntityType<?>> r = event.getRegistry();
        r.register(TileEntityType.Builder.create(TileEntityRainbowCrop::new, Blocks.block_rainbow_crop_red).build(null));
         */
    }
}
