package lonestarrr.arconia.client.core.handler;

import lonestarrr.arconia.client.effects.*;
import lonestarrr.arconia.common.block.entities.ModBlockEntities;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraftforge.client.event.EntityRenderersEvent;

/**
 * Registers block entity renderers
 */
public class BlockEntityRendererHandler {
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL, PedestalRenderer::new);
        evt.registerBlockEntityRenderer(ModBlockEntities.CENTER_PEDESTAL, CenterPedestalRenderer::new);
        evt.registerBlockEntityRenderer(ModBlockEntities.HAT, HatRenderer::new);
    }
}
