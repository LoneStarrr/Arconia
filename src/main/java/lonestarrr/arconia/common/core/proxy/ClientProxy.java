package lonestarrr.arconia.common.core.proxy;

import lonestarrr.arconia.client.core.handler.BlockEntityRendererHandler;
import lonestarrr.arconia.client.core.handler.ColorHandler;
import lonestarrr.arconia.client.effects.PotItemTransfers;
import lonestarrr.arconia.client.effects.RainbowLightningProjector;
import lonestarrr.arconia.client.effects.RainbowRenderer;
import lonestarrr.arconia.client.integration.jei.ClientPedestalRecipes;
import lonestarrr.arconia.client.gui.render.BranchItemRenderer;
import lonestarrr.arconia.client.item.MagicInABottleFilledProperty;
import lonestarrr.arconia.client.item.MagicInABottleTintSource;
import lonestarrr.arconia.client.particle.ModParticles;
import lonestarrr.arconia.client.particle.custom.RainbowParticles;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Proxy code inspired by: http://jabelarminecraft.blogspot.com/p/minecraft-modding-organizing-your-proxy.html
 */
public class ClientProxy implements IProxy {
    @Override
    public void registerHandlers(IEventBus modBus) {
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::loadComplete);
        modBus.addListener(this::registerBlockColors);
        modBus.addListener(this::registerItemTintSources);
        modBus.addListener(this::registerSpecialModelRenderers);
        modBus.addListener(this::registerRangeSelectItemModelProperties);
        modBus.addListener(this::registerRenderPipelines);
        modBus.addListener(BlockEntityRendererHandler::registerBlockEntityRenderers);
        modBus.addListener(this::registerParticleFactories);

        IEventBus forgeBus = NeoForge.EVENT_BUS;
        forgeBus.addListener(PotItemTransfers::render);
        forgeBus.addListener(ClientPedestalRecipes::onRecipesReceived);
        forgeBus.addListener(ClientPedestalRecipes::onLoggingOut);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        Arconia.logger.info("********************* client-side proxy init");
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
    }

    private void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        // Register dynamically colored blocks
        ColorHandler.registerBlockColors(event);
    }

    private void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(MagicInABottleTintSource.ID, MagicInABottleTintSource.MAP_CODEC);
    }

    private void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(BranchItemRenderer.ID, BranchItemRenderer.Unbaked.MAP_CODEC);
    }

    private void registerRangeSelectItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(MagicInABottleFilledProperty.ID, MagicInABottleFilledProperty.MAP_CODEC);
    }

    private void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(RainbowRenderer.RAINBOW_SEGMENT_PIPELINE);
        event.registerPipeline(RainbowLightningProjector.BEAM_TRIANGLE_PIPELINE);
    }

    private void registerParticleFactories(RegisterParticleProvidersEvent event) {
        Minecraft.getInstance().particleEngine.register(ModParticles.RAINBOW_PARTICLES.get(), RainbowParticles.Provider::new);
        Minecraft.getInstance().particleEngine.register(ModParticles.RAINBOW_PARTICLES_RED.get(), RainbowParticles.Provider::new);
        Minecraft.getInstance().particleEngine.register(ModParticles.RAINBOW_PARTICLES_ORANGE.get(), RainbowParticles.Provider::new);
        Minecraft.getInstance().particleEngine.register(ModParticles.RAINBOW_PARTICLES_YELLOW.get(), RainbowParticles.Provider::new);
        Minecraft.getInstance().particleEngine.register(ModParticles.RAINBOW_PARTICLES_GREEN.get(), RainbowParticles.Provider::new);
        Minecraft.getInstance().particleEngine.register(ModParticles.RAINBOW_PARTICLES_LIGHT_BLUE.get(), RainbowParticles.Provider::new);
        Minecraft.getInstance().particleEngine.register(ModParticles.RAINBOW_PARTICLES_BLUE.get(), RainbowParticles.Provider::new);
        Minecraft.getInstance().particleEngine.register(ModParticles.RAINBOW_PARTICLES_PURPLE.get(), RainbowParticles.Provider::new);
    }
}
