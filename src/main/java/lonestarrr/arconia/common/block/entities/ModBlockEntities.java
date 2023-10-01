package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.BlockNames;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Arconia.MOD_ID);

    public static final RegistryObject<BlockEntityType<HatBlockEntity>> HAT = BLOCK_ENTITIES.register(BlockNames.HAT,
            () -> BlockEntityType.Builder.of(HatBlockEntity::new, ModBlocks.hat.get()).build(null));
    public static final RegistryObject<BlockEntityType<WorldBuilderEntity>> WORLD_BUILDER = BLOCK_ENTITIES.register(BlockNames.WORLD_BUILDER,
            () -> BlockEntityType.Builder.of(WorldBuilderEntity::new, ModBlocks.worldBuilder.get()).build(null));
    public static final RegistryObject<BlockEntityType<PedestalBlockEntity>> PEDESTAL = BLOCK_ENTITIES.register(BlockNames.PEDESTAL,
            () -> BlockEntityType.Builder.of(PedestalBlockEntity::new, ModBlocks.pedestal.get()).build(null));
    public static final RegistryObject<BlockEntityType<CenterPedestalBlockEntity>> CENTER_PEDESTAL = BLOCK_ENTITIES.register(BlockNames.CENTER_PEDESTAL,
            () -> BlockEntityType.Builder.of(CenterPedestalBlockEntity::new, ModBlocks.centerPedestal.get()).build(null));
    public static final RegistryObject<BlockEntityType<PotMultiBlockPrimaryBlockEntity>> POT_MULTIBLOCK_PRIMARY = BLOCK_ENTITIES.register(
            BlockNames.POT_MULTIBLOCK_PRIMARY,
            () -> BlockEntityType.Builder.of(PotMultiBlockPrimaryBlockEntity::new, ModBlocks.potMultiBlockPrimary.get()).build(null));
    public static final RegistryObject<BlockEntityType<PotMultiBlockSecondaryBlockEntity>> POT_MULTIBLOCK_SECONDARY = BLOCK_ENTITIES.register(
            BlockNames.POT_MULTIBLOCK_SECONDARY,
            () -> BlockEntityType.Builder.of(PotMultiBlockSecondaryBlockEntity::new, ModBlocks.potMultiBlockSecondary.get()).build(null));
    private static final Map<RainbowColor, RegistryObject<BlockEntityType<GoldArconiumBlockEntity>>> infiniteGoldArconiumBlockEntityTypes = new HashMap<>(
            RainbowColor.values().length);

    static {
        // Infinite Gold arconium blocks
        for (RainbowColor tier : RainbowColor.values()) {
            RegistryObject<BlockEntityType<GoldArconiumBlockEntity>> regOb = BLOCK_ENTITIES.register(tier.getTierName() + "_infinite_gold_arconium_tile_entity",
                    () -> BlockEntityType.Builder.of((pos, state) -> new GoldArconiumBlockEntity(tier, pos, state),
                            ModBlocks.getInfiniteGoldArconiumBlock(tier).get()).build(null));
            infiniteGoldArconiumBlockEntityTypes.put(tier, regOb);
        }
    }

    public static BlockEntityType<GoldArconiumBlockEntity> getInfiniteGoldArconiumBlockEntityType(RainbowColor tier) {
        return infiniteGoldArconiumBlockEntityTypes.get(tier).get();
    }
}
