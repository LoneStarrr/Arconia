package lonestarrr.arconia.common.test;

import lonestarrr.arconia.common.core.TreeFinder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TreeFinder}.
 * Uses {@link EphemeralTestServerProvider} for registry bootstrap and
 * {@link MockLevel} for isolated block storage per test.
 */
@ExtendWith(EphemeralTestServerProvider.class)
class TreeFinderTests {

    private static BlockState WOOD;
    private static BlockState LEAVES;

    @BeforeAll
    static void init(MinecraftServer server) {
        // Server parameter injection ensures registries are loaded
        WOOD = Blocks.OAK_LOG.defaultBlockState();
        LEAVES = Blocks.OAK_LEAVES.defaultBlockState();
    }

    @Test
    void findsSingleTree() {
        MockLevel level = new MockLevel();
        // Trunk
        level.setBlockState(new BlockPos(0, 64, 0), WOOD);
        level.setBlockState(new BlockPos(0, 65, 0), WOOD);
        level.setBlockState(new BlockPos(0, 66, 0), WOOD);
        // Canopy (Manhattan distance <= 2 from top wood)
        level.setBlockState(new BlockPos(0, 67, 0), LEAVES);
        level.setBlockState(new BlockPos(1, 67, 0), LEAVES);
        level.setBlockState(new BlockPos(-1, 67, 0), LEAVES);
        level.setBlockState(new BlockPos(0, 67, 1), LEAVES);

        List<TreeFinder.Tree> trees = TreeFinder.findTrees(
            level, WOOD, LEAVES,
            new BlockPos(-5, 60, -5),
            new BlockPos(5, 70, 5)
        );

        assertEquals(1, trees.size());
        TreeFinder.Tree tree = trees.getFirst();
        assertEquals(3, tree.woodBlocks().size());
        assertEquals(4, tree.leafBlocks().size());
    }

    @Test
    void findsMultipleSeparateTrees() {
        MockLevel level = new MockLevel();

        // Tree A at x=0
        level.setBlockState(new BlockPos(0, 64, 0), WOOD);
        level.setBlockState(new BlockPos(0, 65, 0), WOOD);
        level.setBlockState(new BlockPos(0, 66, 0), LEAVES);

        // Tree B at x=20 — far enough to be a separate component
        level.setBlockState(new BlockPos(20, 64, 0), WOOD);
        level.setBlockState(new BlockPos(20, 65, 0), WOOD);
        level.setBlockState(new BlockPos(20, 66, 0), LEAVES);

        List<TreeFinder.Tree> trees = TreeFinder.findTrees(
            level, WOOD, LEAVES,
                new BlockPos(-5, 60, -5),
                new BlockPos(25, 70, 5)
        );

        assertEquals(2, trees.size());
    }

    @Test
    void returnsEmptyListForEmptyLevel() {
        MockLevel level = new MockLevel();

        List<TreeFinder.Tree> trees = TreeFinder.findTrees(
            level, WOOD, LEAVES,
            new BlockPos(-5, 60, -5),
            new BlockPos(5, 70, 5)
        );

        assertTrue(trees.isEmpty());
    }

    @Test
    void returnsEmptyListWhenOnlyLeavesExist() {
        MockLevel level = new MockLevel();
        level.setBlockState(new BlockPos(0, 65, 0), LEAVES);
        level.setBlockState(new BlockPos(1, 65, 0), LEAVES);

        List<TreeFinder.Tree> trees = TreeFinder.findTrees(
            level, WOOD, LEAVES,
            new BlockPos(-5, 60, -5),
            new BlockPos(5, 70, 5)
        );

        assertTrue(trees.isEmpty());
    }

    @Test
    void groupsDiagonalWoodWith26Connectivity() {
        MockLevel level = new MockLevel();
        // Three wood blocks connected only diagonally (each differs by 1 on every axis)
        level.setBlockState(new BlockPos(0, 64, 0), WOOD);
        level.setBlockState(new BlockPos(1, 65, 1), WOOD);
        level.setBlockState(new BlockPos(2, 66, 2), WOOD);
        // Leaf near the first wood block
        level.setBlockState(new BlockPos(0, 65, 0), LEAVES);

        List<TreeFinder.Tree> trees = TreeFinder.findTrees(
            level, WOOD, LEAVES,
            new BlockPos(-5, 60, -5),
            new BlockPos(10, 70, 10)
        );

        assertEquals(1, trees.size(), "Diagonally adjacent wood should form a single component");
        assertEquals(3, trees.getFirst().woodBlocks().size());
    }

    @Test
    void doesNotGroupWoodeyond26Connectivity() {
        MockLevel level = new MockLevel();
        // Two wood blocks separated by 2 on one axis — not 26-connected
        level.setBlockState(new BlockPos(0, 64, 0), WOOD);
        level.setBlockState(new BlockPos(2, 64, 0), WOOD);
        // Each gets its own leaf
        level.setBlockState(new BlockPos(0, 65, 0), LEAVES);
        level.setBlockState(new BlockPos(2, 65, 0), LEAVES);

        List<TreeFinder.Tree> trees = TreeFinder.findTrees(
            level, WOOD, LEAVES,
            new BlockPos(-5, 60, -5),
            new BlockPos(5, 70, 5)
        );

        assertEquals(2, trees.size(), "Wood separated by 2 blocks should be two components");
    }

    @Test
    void assignsLeavesWithinDistance3() {
        MockLevel level = new MockLevel();
        level.setBlockState(new BlockPos(0, 64, 0), WOOD);
        //included blocks with no x/y/z exceeding the max distance
        level.setBlockState(new BlockPos(1, 64, 0), LEAVES);
        level.setBlockState(new BlockPos(-3, 61, -3), LEAVES);
        level.setBlockState(new BlockPos(3, 67, 3), LEAVES);
        // excluded
        level.setBlockState(new BlockPos(0, 68, 0), LEAVES);
        level.setBlockState(new BlockPos(4, 64, 4), LEAVES);

        List<TreeFinder.Tree> trees = TreeFinder.findTrees(
            level, WOOD, LEAVES,
            new BlockPos(-5, 60, -5),
            new BlockPos(5, 70, 5)
        );

        assertEquals(1, trees.size());
        assertEquals(3, trees.getFirst().leafBlocks().size(),
            "Only leaves within distance 3 should be assigned");
    }

    @Test
    void sortedByYOrdersBlocksCorrectly() {
        MockLevel level = new MockLevel();
        // Connected trunk from y=64 to y=68
        level.setBlockState(new BlockPos(0, 64, 0), WOOD);
        level.setBlockState(new BlockPos(0, 65, 0), WOOD);
        level.setBlockState(new BlockPos(0, 66, 0), WOOD);
        level.setBlockState(new BlockPos(0, 67, 0), WOOD);
        level.setBlockState(new BlockPos(0, 68, 0), WOOD);
        // Leaf at top
        level.setBlockState(new BlockPos(0, 69, 0), LEAVES);

        List<TreeFinder.Tree> trees = TreeFinder.findTrees(
            level, WOOD, LEAVES,
            new BlockPos(-5, 60, -5),
            new BlockPos(5, 75, 5)
        );

        assertEquals(1, trees.size());
        for (List<BlockPos> sorted: List.of(trees.getFirst().leafBlocks(), trees.getFirst().woodBlocks())) {
            for (int i = 1; i < sorted.size(); i++) {
                assertTrue(sorted.get(i).getY() >= sorted.get(i - 1).getY(),
                        "Blocks should be in ascending Y order");
            }
        }
        assertEquals(64, trees.getFirst().woodBlocks().getFirst().getY());
        assertEquals(69, trees.getFirst().leafBlocks().getFirst().getY());
    }

    @Test
    void boundingBoxNormalizesCoordinates() {
        MockLevel level = new MockLevel();
        level.setBlockState(new BlockPos(0, 64, 0), WOOD);
        level.setBlockState(new BlockPos(0, 65, 0), LEAVES);

        // Pass reversed bounding box (max before min)
        List<TreeFinder.Tree> trees = TreeFinder.findTrees(
            level, WOOD, LEAVES,
            new BlockPos(5, 70, 5),
            new BlockPos(-5, 60, -5)
        );

        assertEquals(1, trees.size(), "Reversed bounding box coordinates should still work");
    }

    @Test
    void blocksOutsideBoundingBoxAreNotScanned() {
        MockLevel level = new MockLevel();
        // Wood inside bbox
        level.setBlockState(new BlockPos(0, 64, 0), WOOD);
        // Leaf inside bbox — near wood
        level.setBlockState(new BlockPos(0, 65, 0), LEAVES);
        // Leaf outside bbox (x=5, bbox ends at x=3) — never scanned
        level.setBlockState(new BlockPos(5, 64, 0), LEAVES);
        // Wood outside bbox — never scanned
        level.setBlockState(new BlockPos(5, 65, 0), WOOD);

        List<TreeFinder.Tree> trees = TreeFinder.findTrees(
            level, WOOD, LEAVES,
            new BlockPos(-3, 60, -3),
            new BlockPos(3, 70, 3)
        );

        assertEquals(1, trees.size());
        TreeFinder.Tree tree = trees.getFirst();
        // Only the in-bbox wood and leaf are found
        assertEquals(1, tree.woodBlocks().size());
        assertEquals(1, tree.leafBlocks().size());
    }
}
