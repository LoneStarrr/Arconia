package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.BlockNames;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.Map;

import static lonestarrr.arconia.common.block.ModBlocks.register;

public class ModTiles {
    public static final BlockEntityType<ResourceGenTileEntity> RESOURCEGEN = BlockEntityType.Builder.of(ResourceGenTileEntity::new, ModBlocks.resourceGenBlock).build(null);
    public static final BlockEntityType<HatTileEntity> HAT = BlockEntityType.Builder.of(HatTileEntity::new, ModBlocks.hat).build(null);
    public static final BlockEntityType<PedestalTileEntity> PEDESTAL = BlockEntityType.Builder.of(PedestalTileEntity::new, ModBlocks.pedestal).build(null);
    public static final BlockEntityType<CenterPedestalTileEntity> CENTER_PEDESTAL = BlockEntityType.Builder.of(CenterPedestalTileEntity::new, ModBlocks.centerPedestal).build(null);
    public static final BlockEntityType<OrbTileEntity> ORB = BlockEntityType.Builder.of(OrbTileEntity::new, ModBlocks.orb).build(null);
    public static final BlockEntityType<PotMultiBlockPrimaryTileEntity> POT_MULTIBLOCK_PRIMARY = BlockEntityType.Builder.of(PotMultiBlockPrimaryTileEntity::new, ModBlocks.potMultiBlockPrimary).build(null);
    public static final BlockEntityType<PotMultiBlockSecondaryTileEntity> POT_MULTIBLOCK_SECONDARY = BlockEntityType.Builder.of(PotMultiBlockSecondaryTileEntity::new, ModBlocks.potMultiBlockSecondary).build(null);
    private static final Map<RainbowColor, BlockEntityType<ArconiumTreeRootTileEntity>> treeRootBlockTileEntityTypes = new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, BlockEntityType<RainbowCrateTileEntity>> rainbowCrateTileEntityTypes =
            new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, BlockEntityType<GoldArconiumTileEntity>> goldArconiumTileEntityTypes = new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, BlockEntityType<GoldArconiumTileEntity>> infiniteGoldArconiumTileEntityTypes = new HashMap<>(RainbowColor.values().length);

    public static void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
        IForgeRegistry<BlockEntityType<?>> r = event.getRegistry();

        register(r, RESOURCEGEN, BlockNames.RESOURCEGEN_BLOCK);
        register(r, HAT, BlockNames.HAT);
        register(r, PEDESTAL, BlockNames.PEDESTAL);
        register(r, CENTER_PEDESTAL, BlockNames.CENTER_PEDESTAL);
        register(r, ORB, BlockNames.ORB);
        register(r, POT_MULTIBLOCK_PRIMARY, BlockNames.POT_MULTIBLOCK_PRIMARY);
        register(r, POT_MULTIBLOCK_SECONDARY, BlockNames.POT_MULTIBLOCK_SECONDARY);

        // tree root blocks
        for (RainbowColor tier: RainbowColor.values()) {
            BlockEntityType<ArconiumTreeRootTileEntity> teType =
                    BlockEntityType.Builder.of(() -> new ArconiumTreeRootTileEntity(tier),
                            ModBlocks.getArconiumTreeRootBlocks(tier)).build(null);
            treeRootBlockTileEntityTypes.put(tier, teType);
            register(r, teType, tier.getTierName() + "_pattern_lootblock_tile_entity");
        }

        // Rainbow crates
        for (RainbowColor tier: RainbowColor.values()) {
            BlockEntityType<RainbowCrateTileEntity> teType =
                    BlockEntityType.Builder.of(() -> new RainbowCrateTileEntity(tier),
                            ModBlocks.getRainbowCrateBlock(tier)).build(null);
            rainbowCrateTileEntityTypes.put(tier, teType);
            register(r, teType, tier.getTierName() + "_rainbow_crate_tile_entity");
        }

        // Gold arconium blocks
        for (RainbowColor tier: RainbowColor.values()) {
            BlockEntityType<GoldArconiumTileEntity> teType = BlockEntityType.Builder.of(() -> new GoldArconiumTileEntity(tier, false), ModBlocks.getGoldArconiumBlock(tier)).build(null);
            goldArconiumTileEntityTypes.put(tier, teType);
            register(r, teType, tier.getTierName() + "_gold_arconium_tile_entity");
        }

        // Infinite Gold arconium blocks
        for (RainbowColor tier: RainbowColor.values()) {
            BlockEntityType<GoldArconiumTileEntity> teType = BlockEntityType.Builder.of(() -> new GoldArconiumTileEntity(tier, true), ModBlocks.getInfiniteGoldArconiumBlock(tier)).build(null);
            infiniteGoldArconiumTileEntityTypes.put(tier, teType);
            register(r, teType, tier.getTierName() + "_infinite_gold_arconium_tile_entity");
        }
        Arconia.logger.info("************ Registered tile entities");

    }

    public static BlockEntityType<ArconiumTreeRootTileEntity> getTreeRootBlockTileEntityType(RainbowColor tier) {
        return treeRootBlockTileEntityTypes.get(tier);
    }

    public static BlockEntityType<RainbowCrateTileEntity> getRainbowCrateTileEntityType(RainbowColor tier) {
        return rainbowCrateTileEntityTypes.get(tier);
    }

    public static BlockEntityType<GoldArconiumTileEntity> getGoldArconiumTileEntityType(RainbowColor tier) {
        return goldArconiumTileEntityTypes.get(tier);
    }

    public static BlockEntityType<GoldArconiumTileEntity> getInfiniteGoldArconiumTileEntityType(RainbowColor tier) {
        return infiniteGoldArconiumTileEntityTypes.get(tier);
    }
}
