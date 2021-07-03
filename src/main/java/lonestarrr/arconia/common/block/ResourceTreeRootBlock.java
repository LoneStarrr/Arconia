package lonestarrr.arconia.common.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import lonestarrr.arconia.client.core.handler.ColorHandler;
import lonestarrr.arconia.common.block.tile.ModTiles;
import lonestarrr.arconia.common.block.tile.ResourceTreeRootTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static lonestarrr.arconia.common.block.ModBlocks.register;

/**
 * Tree root block to be placed under a resource tree of the matching tier. Combined with one or more Resource Gen blocks placed near the base of the tree, this
 * will determine the resources to be generated. The root block has a tile entity which is responsible for the resource generation.
 */
public class ResourceTreeRootBlock extends Block implements IBlockColor {
    private final RainbowColor tier;
    private static final Map<RainbowColor, TileEntityType<ResourceTreeRootTileEntity>> tileEntityTypes =
            new HashMap<>(RainbowColor.values().length);
    private static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

    public ResourceTreeRootBlock(RainbowColor tier) {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(0.8f).sound(SoundType.WOOD));
        this.tier = tier;
        BlockState defaultBlockState = this.stateContainer.getBaseState().with(FACING, Direction.NORTH);
        this.setDefaultState(defaultBlockState);
    }

    public final RainbowColor getTier() { return tier; }

    /**
     * BlockState properties for this block
     */
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * When the block is placed into the world, calculates the correct BlockState based on which direction the player is facing
     * @param blockItemUseContext
     * @return
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext blockItemUseContext) {
        Direction direction = blockItemUseContext.getPlacementHorizontalFacing();  // north, east, south, or west
        return getDefaultState().with(FACING, direction);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ResourceTreeRootTileEntity(tier);
    }

    public static TileEntityType<ResourceTreeRootTileEntity> getTileEntityTypeByTier(RainbowColor tier) {
        return ModTiles.getLootBlockTileEntityType(tier);
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable IBlockDisplayReader iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return RainbowColor.getColorRGB(tier);
    }
}
