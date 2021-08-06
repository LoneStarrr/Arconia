package lonestarrr.arconia.common.block.tile;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.BlockNames;
import lonestarrr.arconia.common.core.RainbowColor;

import java.util.HashMap;
import java.util.Map;

import static lonestarrr.arconia.common.block.ModBlocks.register;

@Mod.EventBusSubscriber(modid = Arconia.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModTiles {
    public static final TileEntityType<ResourceGenTileEntity> RESOURCEGEN = TileEntityType.Builder.create(ResourceGenTileEntity::new, ModBlocks.resourceGenBlock).build(null);
    public static final TileEntityType<PedestalTileEntity> PEDESTAL = TileEntityType.Builder.create(PedestalTileEntity::new, ModBlocks.pedestal).build(null);
    public static final TileEntityType<CenterPedestalTileEntity> CENTER_PEDESTAL = TileEntityType.Builder.create(CenterPedestalTileEntity::new, ModBlocks.centerPedestal).build(null);
    public static final TileEntityType<OrbTileEntity> ORB = TileEntityType.Builder.create(OrbTileEntity::new, ModBlocks.orb).build(null);
    private static final Map<RainbowColor, TileEntityType<ResourceTreeRootTileEntity>> treeRootBlockTileEntityTypes = new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, TileEntityType<RainbowCrateTileEntity>> rainbowCrateTileEntityTypes =
            new HashMap<>(RainbowColor.values().length);


    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        IForgeRegistry<TileEntityType<?>> r = event.getRegistry();

        register(r, RESOURCEGEN, BlockNames.RESOURCEGEN_BLOCK);
        register(r, PEDESTAL, BlockNames.PEDESTAL);
        register(r, CENTER_PEDESTAL, BlockNames.CENTER_PEDESTAL);
        register(r, ORB, BlockNames.ORB);

        // tree root blocks (generating the resource tree loot)
        for (RainbowColor tier: RainbowColor.values()) {
            TileEntityType<ResourceTreeRootTileEntity> teType =
                    TileEntityType.Builder.create(() -> new ResourceTreeRootTileEntity(tier),
                            ModBlocks.getResourceTreeRootBlock(tier)).build(null);
            treeRootBlockTileEntityTypes.put(tier, teType);
            register(r, teType, tier.getTierName() + "_pattern_lootblock_tile_entity");
        }

        // Rainbow crates
        for (RainbowColor tier: RainbowColor.values()) {
            TileEntityType<RainbowCrateTileEntity> teType =
                    TileEntityType.Builder.create(() -> new RainbowCrateTileEntity(tier),
                            ModBlocks.getRainbowCrateBlock(tier)).build(null);
            rainbowCrateTileEntityTypes.put(tier, teType);
            register(r, teType, tier.getTierName() + "_rainbow_crate_tile_entity");
        }

        Arconia.logger.info("************ registered tile entities");

    }

    public static TileEntityType<ResourceTreeRootTileEntity> getTreeRootBlockTileEntityType(RainbowColor tier) {
        return treeRootBlockTileEntityTypes.get(tier);
    }

    public static TileEntityType<RainbowCrateTileEntity> getRainbowCrateTileEntityType(RainbowColor tier) {
        return rainbowCrateTileEntityTypes.get(tier);
    }
}
