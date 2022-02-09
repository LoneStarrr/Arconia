package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.tile.ResourceGenTileEntity;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nullable;

/**
 * A block to be placed near the base of a resource tree that will indicate an item for the tree to generate.
 * Resource generator blocks are both tiered, and specific to the item they generate. Tier and resource are stored as NBT data, and are set after
 * placing the block by use of a magically configured tree root.
 */
public class ResourceGenBlock extends BaseEntityBlock implements BlockColor {
    private static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ResourceGenBlock() {
        super(Block.Properties.of(Material.DIRT).strength(0.5f).sound(SoundType.GRAVEL));

        BlockState defaultBlockState = this.stateDefinition.any().setValue(FACING, Direction.NORTH);
        this.registerDefaultState(defaultBlockState);
    }

    /**
     * BlockState properties for this block
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * When the block is placed into the world, calculates the correct BlockState based on which direction the player is facing
     *
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
    public int getColor(BlockState state, @Nullable BlockAndTintGetter reader, @Nullable BlockPos pos, int color) {
        return reader != null && pos != null ? BiomeColors.getAverageGrassColor(reader, pos) : GrassColor.get(0.5D, 1.0D);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResourceGenTileEntity(pos, state);
    }
}