package lonestarrr.arconia.common.core.proxy;

import lonestarrr.arconia.client.core.handler.ColorHandler;
import lonestarrr.arconia.client.core.handler.BlockEntityRendererHandler;
import lonestarrr.arconia.client.effects.BuildPatternPreview;
import lonestarrr.arconia.client.effects.PotItemTransfers;
import lonestarrr.arconia.client.gui.render.HighlightPatternStructure;
import lonestarrr.arconia.client.particle.ModParticles;
import lonestarrr.arconia.client.particle.custom.RainbowParticles;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.MagicInABottle;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Proxy code inspired by: http://jabelarminecraft.blogspot.com/p/minecraft-modding-organizing-your-proxy.html
 */
public class ClientProxy implements IProxy {
    @Override
    public void registerHandlers() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::loadComplete);
        modBus.addListener(this::registerBlockColors);
        modBus.addListener(this::registerItemColors);
        modBus.addListener(BlockEntityRendererHandler::registerBlockEntityRenderers);
        modBus.addListener(this::registerParticleFactories);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(BuildPatternPreview::render);
        forgeBus.addListener(PotItemTransfers::render);
        forgeBus.addListener(HighlightPatternStructure::render);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        Arconia.logger.info("********************* client-side proxy init");

        event.enqueueWork(() -> {
            // TODO Can these blocks themselves provide this hint?
            RenderType cutout = RenderType.cutout();

            // TODO See McJTY porting guide on how to set render_type in the models
            // https://www.mcjty.eu/docs/1.19/porting
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.clover.get(), cutout);

            for (RainbowColor tier : RainbowColor.values()) {
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.getArconiumTreeLeaves(tier).get(), cutout);
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.getArconiumTreeSapling(tier).get(), cutout);
                // gleaned from Blocks.GRASS_BLOCK - this is for overlaying the top with a rainbow tint
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.getInfiniteGoldArconiumBlock(tier).get(), RenderType.cutoutMipped());
            }

            registerItemProperties();
        });
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
    }

    private void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Register dynamically colored blocks
        ColorHandler.registerItemColors(event);
    }

    private void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        // Register dynamically colored blocks
        ColorHandler.registerBlockColors(event);
    }

    private static void registerItemProperties() {
        ItemProperties.register(ModItems.magicInABottle.get(), new ResourceLocation(Arconia.MOD_ID, "filled"), MagicInABottle::getFilledPercentage);
    }

    private void registerParticleFactories(RegisterParticleProvidersEvent event) {
        Minecraft.getInstance().particleEngine.register(ModParticles.RAINBOW_PARTICLES.get(), RainbowParticles.Provider::new);
    }
}
