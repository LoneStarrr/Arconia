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
    public static final TileEntityType<HatTileEntity> HAT = TileEntityType.Builder.create(HatTileEntity::new, ModBlocks.hat).build(null);
    public static final TileEntityType<PedestalTileEntity> PEDESTAL = TileEntityType.Builder.create(PedestalTileEntity::new, ModBlocks.pedestal).build(null);
    public static final TileEntityType<CenterPedestalTileEntity> CENTER_PEDESTAL = TileEntityType.Builder.create(CenterPedestalTileEntity::new, ModBlocks.centerPedestal).build(null);
    public static final TileEntityType<OrbTileEntity> ORB = TileEntityType.Builder.create(OrbTileEntity::new, ModBlocks.orb).build(null);
    public static final TileEntityType<PotMultiBlockPrimaryTileEntity> POT_MULTIBLOCK_PRIMARY = TileEntityType.Builder.create(PotMultiBlockPrimaryTileEntity::new, ModBlocks.potMultiBlockPrimary).build(null);
    public static final TileEntityType<PotMultiBlockSecondaryTileEntity> POT_MULTIBLOCK_SECONDARY = TileEntityType.Builder.create(PotMultiBlockSecondaryTileEntity::new, ModBlocks.potMultiBlockSecondary).build(null);
    private static final Map<RainbowColor, TileEntityType<ResourceTreeRootTileEntity>> treeRootBlockTileEntityTypes = new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, TileEntityType<RainbowCrateTileEntity>> rainbowCrateTileEntityTypes =
            new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, TileEntityType<GoldArconiumTileEntity>> goldArconiumTileEntityTypes = new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, TileEntityType<GoldArconiumTileEntity>> infiniteGoldArconiumTileEntityTypes = new HashMap<>(RainbowColor.values().length);


    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        IForgeRegistry<TileEntityType<?>> r = event.getRegistry();

        register(r, RESOURCEGEN, BlockNames.RESOURCEGEN_BLOCK);
        register(r, HAT, BlockNames.HAT);
        register(r, PEDESTAL, BlockNames.PEDESTAL);
        register(r, CENTER_PEDESTAL, BlockNames.CENTER_PEDESTAL);
        register(r, ORB, BlockNames.ORB);
        register(r, POT_MULTIBLOCK_PRIMARY, BlockNames.POT_MULTIBLOCK_PRIMARY);
        register(r, POT_MULTIBLOCK_SECONDARY, BlockNames.POT_MULTIBLOCK_SECONDARY);

        // tree root blocks
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

        // Gold arconium blocks
        for (RainbowColor tier: RainbowColor.values()) {
            TileEntityType<GoldArconiumTileEntity> teType = TileEntityType.Builder.create(() -> new GoldArconiumTileEntity(tier, false), ModBlocks.getGoldArconiumBlock(tier)).build(null);
            goldArconiumTileEntityTypes.put(tier, teType);
            register(r, teType, tier.getTierName() + "_gold_arconium_tile_entity");
        }

        // Infinite Gold arconium blocks
        for (RainbowColor tier: RainbowColor.values()) {
            TileEntityType<GoldArconiumTileEntity> teType = TileEntityType.Builder.create(() -> new GoldArconiumTileEntity(tier, true), ModBlocks.getInfiniteGoldArconiumBlock(tier)).build(null);
            infiniteGoldArconiumTileEntityTypes.put(tier, teType);
            register(r, teType, tier.getTierName() + "_infinite_gold_arconium_tile_entity");
        }
        Arconia.logger.info("************ Registered tile entities");

    }

    public static TileEntityType<ResourceTreeRootTileEntity> getTreeRootBlockTileEntityType(RainbowColor tier) {
        return treeRootBlockTileEntityTypes.get(tier);
    }

    public static TileEntityType<RainbowCrateTileEntity> getRainbowCrateTileEntityType(RainbowColor tier) {
        return rainbowCrateTileEntityTypes.get(tier);
    }

    public static TileEntityType<GoldArconiumTileEntity> getGoldArconiumTileEntityType(RainbowColor tier) {
        return goldArconiumTileEntityTypes.get(tier);
    }

    public static TileEntityType<GoldArconiumTileEntity> getInfiniteGoldArconiumTileEntityType(RainbowColor tier) {
        return infiniteGoldArconiumTileEntityTypes.get(tier);
    }
}
