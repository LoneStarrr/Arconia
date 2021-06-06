package lonestarrr.arconia.client.core.handler;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import lonestarrr.arconia.client.effects.*;
import lonestarrr.arconia.common.block.ResourceTreeRootBlock;
import lonestarrr.arconia.common.block.tile.ModTiles;
import lonestarrr.arconia.common.core.RainbowColor;

/**
 * Registers tile entity renderers
 */
public class TileEntityRendererHandler {
    public static void registerTileEntityRenderers() {
        ClientRegistry.bindTileEntityRenderer(ModTiles.RESOURCEGEN, ResourceGenRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ModTiles.PEDESTAL, PedestalRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ModTiles.CENTER_PEDESTAL, CenterPedestalRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ModTiles.ORB, OrbRenderer::new);


        // Visual effect rendering between money tree and rainbow crate is accomplished using a Tile Entity Renderer
        for (RainbowColor color: RainbowColor.values()) {
            ClientRegistry.bindTileEntityRenderer(ResourceTreeRootBlock.getTileEntityTypeByTier(color),
                    RainbowBeamRenderer::new);
        }

    }
}
