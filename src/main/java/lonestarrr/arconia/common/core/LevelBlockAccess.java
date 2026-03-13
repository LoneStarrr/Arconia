package lonestarrr.arconia.common.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wraps a real {@link Level} to implement {@link SimplifiedLevel}, delegating block-related calls to the underlying
 * level. This allows production code to use {@link SimplifiedLevel} while still accessing a real level.
 */
public class LevelBlockAccess implements SimplifiedLevel {
    private final Level level;

    public LevelBlockAccess(Level level) {
        this.level = level;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return level.getBlockState(pos);
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
        return level.setBlock(pos, newState, flags);
    }
}
