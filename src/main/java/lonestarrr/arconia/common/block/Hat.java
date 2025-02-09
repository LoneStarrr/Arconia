package lonestarrr.arconia.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A leprechaun's hat. Used in combination with a pot of gold to collect resources.
 */
public class Hat extends Block {
    private static final VoxelShape shape = box(0, 0, 0, 16, 10, 16);

    public Hat() {
        super(Block.Properties.of().mapColor(MapColor.COLOR_GREEN).ignitedByLava().strength(1.0F).noOcclusion());
    }

    @Override
    public VoxelShape getShape(
            BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}