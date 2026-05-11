package lonestarrr.arconia.client.core.handler;

import lonestarrr.arconia.client.effects.CenterPedestalRenderer;
import lonestarrr.arconia.client.effects.PedestalRenderer;
import lonestarrr.arconia.client.effects.PotRenderer;
import lonestarrr.arconia.common.block.entities.ModBlockEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Registers block entity renderers */
public class BlockEntityRendererHandler {
  public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers evt) {
    evt.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL.get(), PedestalRenderer::new);
    evt.registerBlockEntityRenderer(
        ModBlockEntities.CENTER_PEDESTAL.get(), CenterPedestalRenderer::new);
    evt.registerBlockEntityRenderer(
        ModBlockEntities.POT_MULTIBLOCK_PRIMARY.get(), PotRenderer::new);
  }
}
