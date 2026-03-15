package lonestarrr.arconia.common.test;

import lonestarrr.arconia.common.core.SimplifiedLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * A lightweight {@link SimplifiedLevel} implementation backed by a {@link HashMap} for unit testing.
 * Returns AIR for any position that hasn't been explicitly set.
 */
public class MockLevel implements SimplifiedLevel {
    private final Map<BlockPos, BlockState> blockStates = new HashMap<>();

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return blockStates.getOrDefault(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
        blockStates.put(pos.immutable(), newState);
        return true;
    }
}
