package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.tile.ArconiumTreeRootTileEntity;
import net.minecraft.block.*;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import lonestarrr.arconia.common.block.tile.ModTiles;
import lonestarrr.arconia.common.core.RainbowColor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static lonestarrr.arconia.common.block.ModBlocks.register;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tree root block to be placed under a resource tree of the matching tier. Combined with one or more Resource Gen blocks placed near the base of the tree, this
 * will determine the resources to be generated. The root block has a tile entity which is responsible for the resource generation.
 */
public class ArconiumTreeRootBlock extends Block implements BlockColor {
    private final RainbowColor tier;
    private static final Map<RainbowColor, BlockEntityType<ArconiumTreeRootTileEntity>> tileEntityTypes =
            new HashMap<>(RainbowColor.values().length);
    private static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ArconiumTreeRootBlock(RainbowColor tier) {
        super(Block.Properties.of(Material.WOOD).strength(0.8f).sound(SoundType.WOOD));
        this.tier = tier;
        BlockState defaultBlockState = this.stateDefinition.any().setValue(FACING, Direction.NORTH);
        this.registerDefaultState(defaultBlockState);
    }

    public final RainbowColor getTier() { return tier; }

    /**
     * BlockState properties for this block
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * When the block is placed into the world, calculates the correct BlockState based on which direction the player is facing
     * @param blockItemUseContext
     * @return
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockItemUseContext) {
        Direction direction = blockItemUseContext.getHorizontalDirection();  // north, east, south, or west
        return defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new ArconiumTreeRootTileEntity(tier);
    }

    public static BlockEntityType<ArconiumTreeRootTileEntity> getTileEntityTypeByTier(RainbowColor tier) {
        return ModTiles.getTreeRootBlockTileEntityType(tier);
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable BlockAndTintGetter iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }
}
