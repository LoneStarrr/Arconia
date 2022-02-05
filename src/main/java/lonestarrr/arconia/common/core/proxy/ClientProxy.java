package lonestarrr.arconia.common.core.proxy;

import lonestarrr.arconia.client.effects.BuildPatternPreview;
import lonestarrr.arconia.client.effects.OrbLasers;
import lonestarrr.arconia.client.effects.PotItemTransfers;
import lonestarrr.arconia.client.effects.RainbowBeamRenderer;
import lonestarrr.arconia.client.gui.render.HighlightPatternStructure;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import lonestarrr.arconia.client.core.handler.ColorHandler;
import lonestarrr.arconia.client.core.handler.TileEntityRendererHandler;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.client.gui.crate.RainbowCrateContainerScreen;
import lonestarrr.arconia.common.item.MagicInABottle;
import lonestarrr.arconia.common.item.ModItems;

/**
 * Proxy code inspired by: http://jabelarminecraft.blogspot.com/p/minecraft-modding-organizing-your-proxy.html
 */
public class ClientProxy implements IProxy {
    @Override
    public void registerHandlers() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::loadComplete);
        modBus.addListener(RainbowBeamRenderer::onTextureStitch);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(BuildPatternPreview::render);
        forgeBus.addListener(PotItemTransfers::render);
        forgeBus.addListener(HighlightPatternStructure::render);
//        forgeBus.addListener(OrbLasers::render);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        Arconia.logger.info("********************* client-side proxy init");

        event.enqueueWork(() -> {
            // TODO Can these blocks themselves provide this hint?
            RenderType cutout = RenderType.cutout();

            RenderTypeLookup.setRenderLayer(ModBlocks.clover, cutout);

            for (RainbowColor tier : RainbowColor.values()) {
//            RenderTypeLookup.setRenderLayer(ModBlocks.getRainbowCrop(tier), cutout);
                RenderTypeLookup.setRenderLayer(ModBlocks.getArconiumTreeLeaves(tier), cutout);
                RenderTypeLookup.setRenderLayer(ModBlocks.getArconiumTreeSapling(tier), cutout);
                // Crates are solid, but use overlapping textures with gaps
                RenderTypeLookup.setRenderLayer(ModBlocks.getRainbowCrateBlock(tier), cutout);
                // gleaned from Blocks.GRASS_BLOCK - this is for overlaying the top with a rainbow tint
                RenderTypeLookup.setRenderLayer(ModBlocks.getArconiumTreeRootBlocks(tier), RenderType.cutoutMipped());
                RenderTypeLookup.setRenderLayer(ModBlocks.getGoldArconiumBlock(tier), RenderType.cutoutMipped());
                RenderTypeLookup.setRenderLayer(ModBlocks.getInfiniteGoldArconiumBlock(tier), RenderType.cutoutMipped());
                RenderTypeLookup.setRenderLayer(ModBlocks.orb, RenderType.translucent());
            }

            // gleaned from Blocks.GRASS_BLOCK - this is for overlaying the sides with the tinted grass
            RenderTypeLookup.setRenderLayer(ModBlocks.resourceGenBlock, RenderType.cutoutMipped());

            // GUI screens associated with containers
            RainbowCrateContainerScreen.registerContainerScreens();

            TileEntityRendererHandler.registerTileEntityRenderers();

            registerItemProperties();
        });
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
        // Register dynamically colored blocks
        ColorHandler.registerColorBlocks();
    }

    private static void registerItemProperties() {
        ItemModelsProperties.register(ModItems.magicInABottle, new ResourceLocation(Arconia.MOD_ID, "filled"), MagicInABottle::getFilledPercentage);
    }
}
