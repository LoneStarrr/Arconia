package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.BlockNames;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Arconia.MOD_ID);

    public static final Supplier<BlockEntityType<WorldBuilderEntity>> WORLD_BUILDER = BLOCK_ENTITIES.register(BlockNames.WORLD_BUILDER,
            () -> BlockEntityType.Builder.of(WorldBuilderEntity::new, ModBlocks.worldBuilder.get()).build(null));
    public static final Supplier<BlockEntityType<PedestalBlockEntity>> PEDESTAL = BLOCK_ENTITIES.register(BlockNames.PEDESTAL,
            () -> BlockEntityType.Builder.of(PedestalBlockEntity::new, ModBlocks.pedestal.get()).build(null));
    public static final Supplier<BlockEntityType<CenterPedestalBlockEntity>> CENTER_PEDESTAL = BLOCK_ENTITIES.register(BlockNames.CENTER_PEDESTAL,
            () -> BlockEntityType.Builder.of(CenterPedestalBlockEntity::new, ModBlocks.centerPedestal.get()).build(null));
    public static final Supplier<BlockEntityType<PotMultiBlockPrimaryBlockEntity>> POT_MULTIBLOCK_PRIMARY = BLOCK_ENTITIES.register(
            BlockNames.POT_MULTIBLOCK_PRIMARY,
            () -> BlockEntityType.Builder.of(PotMultiBlockPrimaryBlockEntity::new, ModBlocks.potMultiBlockPrimary.get()).build(null));
    public static final Supplier<BlockEntityType<PotMultiBlockSecondaryBlockEntity>> POT_MULTIBLOCK_SECONDARY = BLOCK_ENTITIES.register(
            BlockNames.POT_MULTIBLOCK_SECONDARY,
            () -> BlockEntityType.Builder.of(PotMultiBlockSecondaryBlockEntity::new, ModBlocks.potMultiBlockSecondary.get()).build(null));
}
