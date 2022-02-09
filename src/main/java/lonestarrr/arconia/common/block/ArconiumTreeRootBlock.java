package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.tile.ArconiumTreeRootTileEntity;
import lonestarrr.arconia.common.block.tile.ModTiles;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Tree root block to be placed under a resource tree of the matching tier. Combined with one or more Resource Gen blocks placed near the base of the tree, this
 * will determine the resources to be generated. The root block has a tile entity which is responsible for the resource generation.
 */
public class ArconiumTreeRootBlock extends BaseEntityBlock implements BlockColor {
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArconiumTreeRootTileEntity(tier, pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide) {
            return createTickerHelper(type, ModTiles.getTreeRootBlockTileEntityType(tier), ArconiumTreeRootTileEntity::tick);
        }
        return null;
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
