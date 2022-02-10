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

public class ModBlockEntities {
    public static final BlockEntityType<ResourceGenBlockEntity> RESOURCEGEN = BlockEntityType.Builder.of(ResourceGenBlockEntity::new, ModBlocks.resourceGenBlock).build(null);
    public static final BlockEntityType<HatBlockEntity> HAT = BlockEntityType.Builder.of(HatBlockEntity::new, ModBlocks.hat).build(null);
    public static final BlockEntityType<PedestalBlockEntity> PEDESTAL = BlockEntityType.Builder.of(PedestalBlockEntity::new, ModBlocks.pedestal).build(null);
    public static final BlockEntityType<CenterPedestalBlockEntity> CENTER_PEDESTAL = BlockEntityType.Builder.of(CenterPedestalBlockEntity::new, ModBlocks.centerPedestal).build(null);
    public static final BlockEntityType<OrbBlockEntity> ORB = BlockEntityType.Builder.of(OrbBlockEntity::new, ModBlocks.orb).build(null);
    public static final BlockEntityType<PotMultiBlockPrimaryBlockEntity> POT_MULTIBLOCK_PRIMARY = BlockEntityType.Builder.of(PotMultiBlockPrimaryBlockEntity::new, ModBlocks.potMultiBlockPrimary).build(null);
    public static final BlockEntityType<PotMultiBlockSecondaryBlockEntity> POT_MULTIBLOCK_SECONDARY = BlockEntityType.Builder.of(
            PotMultiBlockSecondaryBlockEntity::new, ModBlocks.potMultiBlockSecondary).build(null);
    private static final Map<RainbowColor, BlockEntityType<ArconiumTreeRootBlockEntity>> treeRootBlockBlockEntityTypes = new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, BlockEntityType<RainbowCrateBlockEntity>> rainbowCrateBlockEntityTypes =
            new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, BlockEntityType<GoldArconiumBlockEntity>> goldArconiumBlockEntityTypes = new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, BlockEntityType<GoldArconiumBlockEntity>> infiniteGoldArconiumBlockEntityTypes = new HashMap<>(RainbowColor.values().length);

    public static void registerBlockEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
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
            BlockEntityType<ArconiumTreeRootBlockEntity> teType =
                    BlockEntityType.Builder.of((pos, state) -> new ArconiumTreeRootBlockEntity(tier, pos, state),
                            ModBlocks.getArconiumTreeRootBlocks(tier)).build(null);
            treeRootBlockBlockEntityTypes.put(tier, teType);
            register(r, teType, tier.getTierName() + "_pattern_lootblock_tile_entity");
        }

        // Rainbow crates
        for (RainbowColor tier: RainbowColor.values()) {
            BlockEntityType<RainbowCrateBlockEntity> teType =
                    BlockEntityType.Builder.of((pos, state) -> new RainbowCrateBlockEntity(tier, pos, state),
                            ModBlocks.getRainbowCrateBlock(tier)).build(null);
            rainbowCrateBlockEntityTypes.put(tier, teType);
            register(r, teType, tier.getTierName() + "_rainbow_crate_tile_entity");
        }

        // Gold arconium blocks
        for (RainbowColor tier: RainbowColor.values()) {
            BlockEntityType<GoldArconiumBlockEntity> teType = BlockEntityType.Builder.of((pos, state) -> new GoldArconiumBlockEntity(tier, false, pos, state), ModBlocks.getGoldArconiumBlock(tier)).build(null);
            goldArconiumBlockEntityTypes.put(tier, teType);
            register(r, teType, tier.getTierName() + "_gold_arconium_tile_entity");
        }

        // Infinite Gold arconium blocks
        for (RainbowColor tier: RainbowColor.values()) {
            BlockEntityType<GoldArconiumBlockEntity> teType = BlockEntityType.Builder.of((pos, state) -> new GoldArconiumBlockEntity(tier, true, pos, state), ModBlocks.getInfiniteGoldArconiumBlock(tier)).build(null);
            infiniteGoldArconiumBlockEntityTypes.put(tier, teType);
            register(r, teType, tier.getTierName() + "_infinite_gold_arconium_tile_entity");
        }
        Arconia.logger.info("************ Registered tile entities");

    }

    public static BlockEntityType<ArconiumTreeRootBlockEntity> getTreeRootBlockBlockEntityType(RainbowColor tier) {
        return treeRootBlockBlockEntityTypes.get(tier);
    }

    public static BlockEntityType<RainbowCrateBlockEntity> getRainbowCrateBlockEntityType(RainbowColor tier) {
        return rainbowCrateBlockEntityTypes.get(tier);
    }

    public static BlockEntityType<GoldArconiumBlockEntity> getGoldArconiumBlockEntityType(RainbowColor tier) {
        return goldArconiumBlockEntityTypes.get(tier);
    }

    public static BlockEntityType<GoldArconiumBlockEntity> getInfiniteGoldArconiumBlockEntityType(RainbowColor tier) {
        return infiniteGoldArconiumBlockEntityTypes.get(tier);
    }
}
