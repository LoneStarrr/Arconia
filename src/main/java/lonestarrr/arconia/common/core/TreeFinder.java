package lonestarrr.arconia.common.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Finds all blocks belonging to trees within a bounding box.
 * <p>
 * Uses 26-connectivity for wood block grouping and checks leaf proximity
 * to determine tree membership.
 */
public class TreeFinder {

    /**
     * Represents a found tree with its blocks.
     */
    public record Tree(List<BlockPos> woodBlocks, List<BlockPos> leafBlocks) {
        public Tree {
            woodBlocks = sortedByY(woodBlocks);
            leafBlocks = sortedByY(leafBlocks);
        }

        public boolean isEmpty() {
            return woodBlocks.isEmpty() && leafBlocks.isEmpty();
        }

        /**
         * Sorts the blocks by Y coordinate (asscending), then by X, then by Z.
         *
         * @return a list of blocks sorted by position
         */
        private List<BlockPos> sortedByY(Collection<BlockPos> blocks) {
            List<BlockPos> sorted = new ArrayList<>(blocks);
            sorted.sort(Comparator
                .comparing(Vec3i::getY)
                .thenComparing(Vec3i::getX)
                .thenComparing(Vec3i::getZ));
            return sorted;
        }
    }

    /**
     * Finds all trees within the given bounding box.
     *
     * @param level           the block getter to search in
     * @param woodBlockType   the wood block type to search for
     * @param leavesBlockType the leaves block type to search for
     * @param first           First corner of the bounding box
     * @param second          Second corner of the bounding box
     * @return a list of trees found, each containing its blocks
     */
    public static List<Tree> findTrees(
        BlockGetter level,
        BlockState woodBlockType,
        BlockState leavesBlockType,
        BlockPos first,
        BlockPos second
    ) {
        int x1 = Math.min(first.getX(), second.getX());
        int y1 = Math.min(first.getY(), second.getY());
        int z1 = Math.min(first.getZ(), second.getZ());
        int x2 = Math.max(first.getX(), second.getX());
        int y2 = Math.max(first.getY(), second.getY());
        int z2 = Math.max(first.getZ(), second.getZ());

        // Collect all wood and leaf blocks in the bounding box
        Map<BlockPos, BlockState> woodBlocks = new HashMap<>();
        Map<BlockPos, BlockState> leafBlocks = new HashMap<>();

        for (BlockPos pos : BlockPos.betweenClosed(x1, y1, z1, x2, y2, z2)) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock().equals(woodBlockType.getBlock())) {
                woodBlocks.put(pos.immutable(), state);
            } else if (state.getBlock().equals(leavesBlockType.getBlock())) {
                leafBlocks.put(pos.immutable(), state);
            }
        }

        // Group wood blocks into connected components using 26-connectivity
        List<List<BlockPos>> woodComponents = findConnectedComponents(woodBlocks.keySet());

        // For each wood component, find nearby leaves and create a tree
        List<Tree> trees = new ArrayList<>();
        for (List<BlockPos> component : woodComponents) {
            Set<BlockPos> componentWood = new HashSet<>(component);

            // Find leaves within 3 blocks (Manhattan distance) of any wood in this component that are within
            // the bounding box
            Set<BlockPos> componentLeaves = new HashSet<>();
            for (BlockPos leafPos : leafBlocks.keySet()) {
                if (isNearAnyWood(leafPos, componentWood, 3) &&
                    leafPos.getX() >= x1 && leafPos.getX() <= x2 &&
                    leafPos.getY() >= y1 && leafPos.getY() <= y2 &&
                    leafPos.getZ() >= z1 && leafPos.getZ() <= z2)
                {
                    componentLeaves.add(leafPos);
                }
            }

            if (!componentLeaves.isEmpty()) {
                trees.add(new Tree(componentWood.stream().toList(), componentLeaves.stream().toList()));
            }
        }

        return trees;
    }

    /**
     * Groups positions into connected components using 26-connectivity (3D adjacency
     * including diagonals).
     *
     * @param positions the positions to group
     * @return a list of connected components
     */
    private static List<List<BlockPos>> findConnectedComponents(Set<BlockPos> positions) {
        if (positions.isEmpty()) {
            return List.of();
        }

        Set<BlockPos> remaining = new HashSet<>(positions);
        List<List<BlockPos>> components = new ArrayList<>();

        while (!remaining.isEmpty()) {
            BlockPos start = remaining.iterator().next();
            List<BlockPos> component = new ArrayList<>();
            Deque<BlockPos> stack = new ArrayDeque<>();
            stack.push(start);

            while (!stack.isEmpty()) {
                BlockPos current = stack.pop();
                if (!remaining.contains(current)) {
                    continue;
                }
                remaining.remove(current);
                component.add(current);

                // Add all 26 neighbors
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) {
                                continue;
                            }
                            BlockPos neighbor = current.offset(dx, dy, dz);
                            if (remaining.contains(neighbor)) {
                                stack.push(neighbor);
                            }
                        }
                    }
                }
            }

            components.add(component);
        }

        return components;
    }

    /**
     * Checks if any position in the set is within the given cuboid distance of the reference position.
     *
     * @param leafPos    the reference position
     * @param woodBlocks the positions to check
     * @param maxDist   the maximum Manhattan distance
     * @return true if any position is within maxDist
     */
    private static boolean isNearAnyWood(BlockPos leafPos, Set<BlockPos> woodBlocks, int maxDist) {
        for (BlockPos pos : woodBlocks) {
            if (Math.abs(leafPos.getX() - pos.getX()) <= maxDist
                    && Math.abs(leafPos.getY() - pos.getY()) <= maxDist
                    && Math.abs(leafPos.getZ() - pos.getZ()) <= maxDist)
            return true;
        }
        return false;
    }
}
