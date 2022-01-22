package lonestarrr.arconia.common.core.helper;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import lonestarrr.arconia.common.Arconia;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Places block structures in the world
 * IDEA: Placing large amounts of blocks using setBlockState() is not efficient due to lighting updates for each
 *       individual placement. Can this be optimized without introducing bugs? Do it tick-based? It's only used in
 *       creative mode for now though.
 */
public abstract class Structures {
    /**
     * Detects a structure in the world
     *
     * @param structure
     *     Structure to detect
     * @param world
     *     World structure's in
     * @param matchBlockOnly
     *        Don't match the exact block state, but only the associated block
     * @return
     *     List of block positions in the world that did not contain the expected blockstate. Ergo, if empty, the
     *     structure was found
     */
    public static List<BlockPos> detectStructure(@Nonnull final Map<BlockPos, BlockState> structure, @Nonnull final World world, final boolean matchBlockOnly) {
        List<BlockPos> result = new ArrayList<>();

        for (Map.Entry<BlockPos, BlockState> entry: structure.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState expected = entry.getValue();
            BlockState actual = world.getBlockState(pos);
            boolean matches;

            if (matchBlockOnly) {
                matches = expected.getBlock().equals(actual.getBlock());
            } else {
                matches = expected.equals(actual);
            }

            if (!matches) {
                result.add(pos);
            }
        }
        return result;
    }

    /**
     * Places a structure in the world
     *
     * @param positions   Positions to update
     * @param blockStates States to set.
     * @param world       World to set state in
     * @param flags       flags to use when setting block state, see
     * {@link World#setBlockState(BlockPos, BlockState, int)}
     */
    public static void placeStructure(Iterator<BlockPos> positions, Iterator<BlockState> blockStates, World world,
                                      int flags) {
        placeStructure(positions, blockStates, (pos, state) -> world.setBlock(pos, state, flags));
    }

    /**
     * Place a structure in the world
     *
     * @param positions     Positions to set blockstates on
     * @param blockStates   States to set - assuming iterator in sync with positions
     * @param setBlockState Function that wil do the block placing (done this way for easy testability)
     */
    public static void placeStructure(Iterator<BlockPos> positions, Iterator<BlockState> blockStates,
                                      BiFunction<BlockPos, BlockState, Boolean> setBlockState) {
        while (positions.hasNext() && blockStates.hasNext()) {
            BlockPos pos = positions.next();
            BlockState blockState = blockStates.next();
            setBlockState.apply(pos, blockState);
        }
    }

    /**
     * Place a structure in the world
     *
     * @param blockStates 2D [X,Z] array of blockstates to place
     * @param world       World to place blocks in
     * @param flags       Flags to use when setting block states, see
     * {@link World#setBlockState(BlockPos, BlockState, int)}
     */
    public static void placeStructure(Map<BlockPos, BlockState> blockStates, World world, int flags) {
        for (Map.Entry<BlockPos, BlockState> pair: blockStates.entrySet()) {
            world.setBlock(pair.getKey(), pair.getValue(), flags);
        }
    }
}
