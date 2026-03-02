package lonestarrr.arconia.common.test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal mock of {@link BlockGetter} for unit testing.
 * Stores block states in a map; positions not explicitly set return air.
 */
public class MockLevel implements BlockGetter {
    private final Map<BlockPos, BlockState> blockStates = new HashMap<>();

    public void setBlockState(BlockPos pos, BlockState state) {
        blockStates.put(pos.immutable(), state);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return blockStates.getOrDefault(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getHeight() {
        return 384;
    }

    @Override
    public int getMinBuildHeight() {
        return -64;
    }
}
