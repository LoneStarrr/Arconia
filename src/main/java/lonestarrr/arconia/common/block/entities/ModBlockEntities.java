package lonestarrr.arconia.common.block.entities;

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
    public static final BlockEntityType<HatBlockEntity> HAT = BlockEntityType.Builder.of(HatBlockEntity::new, ModBlocks.hat).build(null);
    public static final BlockEntityType<WorldBuilderEntity> WORLD_BUILDER = BlockEntityType.Builder.of(WorldBuilderEntity::new, ModBlocks.worldBuilder).build(null);
    public static final BlockEntityType<PedestalBlockEntity> PEDESTAL = BlockEntityType.Builder.of(PedestalBlockEntity::new, ModBlocks.pedestal).build(null);
    public static final BlockEntityType<CenterPedestalBlockEntity> CENTER_PEDESTAL = BlockEntityType.Builder.of(CenterPedestalBlockEntity::new, ModBlocks.centerPedestal).build(null);
    public static final BlockEntityType<PotMultiBlockPrimaryBlockEntity> POT_MULTIBLOCK_PRIMARY = BlockEntityType.Builder.of(PotMultiBlockPrimaryBlockEntity::new, ModBlocks.potMultiBlockPrimary).build(null);
    public static final BlockEntityType<PotMultiBlockSecondaryBlockEntity> POT_MULTIBLOCK_SECONDARY = BlockEntityType.Builder.of(
            PotMultiBlockSecondaryBlockEntity::new, ModBlocks.potMultiBlockSecondary).build(null);
    private static final Map<RainbowColor, BlockEntityType<GoldArconiumBlockEntity>> infiniteGoldArconiumBlockEntityTypes = new HashMap<>(RainbowColor.values().length);

    public static void registerBlockEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
        IForgeRegistry<BlockEntityType<?>> r = event.getRegistry();

        register(r, HAT, BlockNames.HAT);
        register(r, WORLD_BUILDER, BlockNames.WORLD_BUILDER);
        register(r, PEDESTAL, BlockNames.PEDESTAL);
        register(r, CENTER_PEDESTAL, BlockNames.CENTER_PEDESTAL);
        register(r, POT_MULTIBLOCK_PRIMARY, BlockNames.POT_MULTIBLOCK_PRIMARY);
        register(r, POT_MULTIBLOCK_SECONDARY, BlockNames.POT_MULTIBLOCK_SECONDARY);

        // Infinite Gold arconium blocks
        for (RainbowColor tier: RainbowColor.values()) {
            BlockEntityType<GoldArconiumBlockEntity> teType = BlockEntityType.Builder.of((pos, state) -> new GoldArconiumBlockEntity(tier, pos, state), ModBlocks.getInfiniteGoldArconiumBlock(tier)).build(null);
            infiniteGoldArconiumBlockEntityTypes.put(tier, teType);
            register(r, teType, tier.getTierName() + "_infinite_gold_arconium_tile_entity");
        }
        Arconia.logger.info("************ Registered block entities");

    }

    public static BlockEntityType<GoldArconiumBlockEntity> getInfiniteGoldArconiumBlockEntityType(RainbowColor tier) {
        return infiniteGoldArconiumBlockEntityTypes.get(tier);
    }
}
