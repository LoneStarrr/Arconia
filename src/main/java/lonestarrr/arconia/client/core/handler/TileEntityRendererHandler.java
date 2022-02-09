package lonestarrr.arconia.client.core.handler;

import lonestarrr.arconia.client.effects.*;
import lonestarrr.arconia.common.block.ArconiumTreeRootBlock;
import lonestarrr.arconia.common.block.tile.ModTiles;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.EntityRenderersEvent;

/**
 * Registers tile entity renderers
 */
public class TileEntityRendererHandler {
    public interface BERConsumer {
        <E extends BlockEntity> void register(BlockEntityType<E> type, BlockEntityRendererProvider<? super E> factory);
    }

    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerBlockEntityRenderer(ModTiles.RESOURCEGEN::get, ResourceGenRenderer::new);
        consumer.register(ModTiles.RESOURCEGEN, ResourceGenRenderer::new);
        consumer.register(ModTiles.PEDESTAL, PedestalRenderer::new);
        consumer.register(ModTiles.CENTER_PEDESTAL, CenterPedestalRenderer::new);
        consumer.register(ModTiles.HAT, HatRenderer::new);
        consumer.register(ModTiles.ORB, OrbRenderer::new);


        // Visual effect for an 'activated' arconium tree
        for (RainbowColor color: RainbowColor.values()) {
            ClientRegistry.bindTileEntityRenderer(ArconiumTreeRootBlock.getTileEntityTypeByTier(color),
                    RainbowBeamRenderer::new);
        }

    }
}
