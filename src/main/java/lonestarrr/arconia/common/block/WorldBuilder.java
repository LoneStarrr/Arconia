package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.entities.ModBlockEntities;
import lonestarrr.arconia.common.block.entities.PotMultiBlockPrimaryBlockEntity;
import lonestarrr.arconia.common.block.entities.WorldBuilderEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class WorldBuilder extends BaseEntityBlock {
    public WorldBuilder() {
        // tools that can mine it are defined by setting tags, in datagen
        super(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).requiresCorrectToolForDrops().strength(3.0F).sound(SoundType.WOOD));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WorldBuilderEntity(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide) {
            return createTickerHelper(type, ModBlockEntities.WORLD_BUILDER, WorldBuilderEntity::tick);
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null || !(be instanceof WorldBuilderEntity)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        WorldBuilderEntity wbe = (WorldBuilderEntity) be;
        if (wbe.isConverting()) {
            player.sendSystemMessage(Component.translatable("arconia.block.world_builder.in_progress"));
            return InteractionResult.FAIL;
        }

        float boostFactor = 1F;
        BlockState toMatch = Blocks.OAK_PLANKS.defaultBlockState();
        if (player.isCreative()) {
            toMatch = null;
        }
        boolean willConvertBlocks = wbe.startBuild(toMatch, boostFactor);
        if (willConvertBlocks) {
            player.sendSystemMessage(Component.translatable("arconia.block.world_builder.start_build"));
        } else {
            player.sendSystemMessage(Component.translatable("arconia.block.world_builder.no_blocks_found"));
        }
        return InteractionResult.SUCCESS;
    }
}
