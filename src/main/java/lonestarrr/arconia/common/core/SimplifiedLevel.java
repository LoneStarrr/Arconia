package lonestarrr.arconia.common.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This interface represents a subset of a real level and only exposes block-related methods. This is done to support
 * unit testingwhere we only care about getting and setting blocks to validate code, where GameTest is too
 * heavy-handed, and there appears to be no equivalent easy JUnit supported way of providing a "mock" level to test
 * against.
 */
public interface SimplifiedLevel {
    BlockState getBlockState(BlockPos pos);
    public boolean setBlock(BlockPos pos, BlockState newState, int flags);
}
