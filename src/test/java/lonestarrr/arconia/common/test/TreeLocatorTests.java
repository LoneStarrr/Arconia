package lonestarrr.arconia.common.test;

import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.entities.PotMultiBlockPrimaryBlockEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PotMultiBlockPrimaryBlockEntity.TreeLocator}.
 * Uses {@link EphemeralTestServerProvider} for registry bootstrap and
 * {@link MockLevel} for isolated block storage per test.
 */
@ExtendWith(EphemeralTestServerProvider.class)
class TreeLocatorTests {

    @BeforeAll
    static void init(MinecraftServer server) {
        // Server parameter injection ensures registries are loaded

    }

    private static void placeTree(MockLevel level, BlockPos basePos, RainbowColor color, int height, int width) {
        for (int i = 0; i < height; i++) {
            level.setBlock(basePos.above(i), Blocks.OAK_LOG.defaultBlockState(), 0);
        }
        for (int y = 2; y < height; y++) {
            BlockPos layer = basePos.above(y);
            for (int dx = -width; dx <= width; dx++) {
                for (int dz = -width; dz <= width; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    level.setBlock(layer.offset(dx, 0, dz), ModBlocks.getArconiumTreeLeaves(color).get().defaultBlockState(), 0);
                }
            }
        }
        int capWidth = width - 1;
        BlockPos cap = basePos.above(height);
        for (int dx = -capWidth; dx <= capWidth; dx++) {
            for (int dz = -capWidth; dz <= capWidth; dz++) {
                level.setBlock(cap.offset(dx, 0, dz), ModBlocks.getArconiumTreeLeaves(color).get().defaultBlockState(), 0);
            }
        }
    }

    private static void placeGrass(MockLevel level, BlockPos pos, RainbowColor color) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                level.setBlock(pos.offset(dx, 0, dz), ModBlocks.getRainbowGrassBlock(color).get().defaultBlockState(), 0);
            }
        }
    }

    @Test
    void testTreeLocatorSingleTree(MinecraftServer server) {
        MockLevel level = new MockLevel();

        BlockPos potPos = new BlockPos(0, 60, 0);
        placeTree(level, potPos.offset(-6, 0, -6), RainbowColor.RED, 4, 3);
        placeGrass(level, potPos.offset(-6, -1, -6), RainbowColor.RED);

        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(RainbowColor.RED));
        assertEquals(1, result.get(RainbowColor.RED).size());

        PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree tree = result.get(RainbowColor.RED).getFirst();
        assertEquals(4, tree.trunkBlocks().size(), "Should find 4 trunk blocks");
        assertEquals(121, tree.leaves().size(), "Should find 121 leaf blocks (7x7-1 at 2 trunk levels, 1 5x5 cap)");
    }

    @Test
    void testTreeLocatorSingleTreeTooWide(MinecraftServer server) {
        MockLevel level = new MockLevel();

        BlockPos potPos = new BlockPos(0, 60, 0);
        placeTree(level, potPos.offset(-6, 0, -6), RainbowColor.RED, 4, 4);
        placeGrass(level, potPos.offset(-6, -1, -6), RainbowColor.RED);

        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(RainbowColor.RED));
        assertEquals(1, result.get(RainbowColor.RED).size());

        PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree tree = result.get(RainbowColor.RED).getFirst();
        assertEquals(4, tree.trunkBlocks().size(), "Should find 4 trunk blocks");
        // TreeLocator only looks in a 7x7 area, with this tree being 9x9
        assertEquals(145, tree.leaves().size(), "Should find 121 leaf blocks (7x7-1 at 2 trunk levels, 1 7x7 cap)");
    }


    @Test
    void testTreeLocatorMultipleTrees(MinecraftServer server) {
        MockLevel level = new MockLevel();

        BlockPos potPos = new BlockPos(0, 60, 0);

        placeTree(level, potPos.offset(-6, 0, -6), RainbowColor.RED, 4, 1);
        placeGrass(level, potPos.offset(-6, -1, -6), RainbowColor.RED);

        placeTree(level, potPos.offset(6, 0, -6), RainbowColor.ORANGE, 4, 1);
        placeGrass(level, potPos.offset(6, -1, -6), RainbowColor.ORANGE);

        placeTree(level, potPos.offset(-6, 0, 6), RainbowColor.YELLOW, 4, 1);
        placeGrass(level, potPos.offset(-6, -1, 6), RainbowColor.YELLOW);

        placeTree(level, potPos.offset(6, 0, 6), RainbowColor.GREEN, 4, 1);
        placeGrass(level, potPos.offset(6, -1, 6), RainbowColor.GREEN);

        placeTree(level, potPos.offset(0, 0, -6), RainbowColor.LIGHT_BLUE, 4, 1);
        placeGrass(level, potPos.offset(0, -1, -6), RainbowColor.LIGHT_BLUE);

        placeTree(level, potPos.offset(0, 0, 6), RainbowColor.BLUE, 4, 1);
        placeGrass(level, potPos.offset(0, -1, 6), RainbowColor.BLUE);

        placeTree(level, potPos.offset(6, 0, 0), RainbowColor.PURPLE, 4, 1);
        placeGrass(level, potPos.offset(6, -1, 0), RainbowColor.PURPLE);

        placeTree(level, potPos.offset(-6, 0, 0), RainbowColor.PURPLE, 4, 1);
        placeGrass(level, potPos.offset(-6, -1, 0), RainbowColor.PURPLE);

        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        assertEquals(7, result.size());
        assertTrue(result.containsKey(RainbowColor.RED));
        assertTrue(result.containsKey(RainbowColor.ORANGE));
        assertTrue(result.containsKey(RainbowColor.YELLOW));
        assertTrue(result.containsKey(RainbowColor.GREEN));
        assertTrue(result.containsKey(RainbowColor.LIGHT_BLUE));
        assertTrue(result.containsKey(RainbowColor.BLUE));
        assertTrue(result.containsKey(RainbowColor.PURPLE));
        assertEquals(2, result.get(RainbowColor.PURPLE).size());
    }

    @Test
    void testTreeLocatorDuplicateTrees(MinecraftServer server) {
        MockLevel level = new MockLevel();

        BlockPos potPos = new BlockPos(0, 60, 0);

        // RED tree 1
        placeTree(level, potPos.offset(-6, 0, -6), RainbowColor.RED, 4, 1);
        placeGrass(level, potPos.offset(-6, -1, -6), RainbowColor.RED);

        // RED tree 2 (same color)
        placeTree(level, potPos.offset(-6, 0, 6), RainbowColor.RED, 4, 1);
        placeGrass(level, potPos.offset(-6, -1, 6), RainbowColor.RED);

        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(RainbowColor.RED));
        assertEquals(2, result.get(RainbowColor.RED).size(), "Should find 2 trees of the same color");
    }

    @Test
    void testTreeLocatorIncompleteTree(MinecraftServer server) {
        MockLevel level = new MockLevel();

        BlockPos potPos = new BlockPos(0, 60, 0);

        // RED tree (complete)
        placeTree(level, potPos.offset(-6, 0, -6), RainbowColor.RED, 4, 1);
        placeGrass(level, potPos.offset(-6, -1, -6), RainbowColor.RED);

        // ORANGE tree (incomplete - one grass block replaced with vanilla grass)
        placeTree(level, potPos.offset(6, 0, -6), RainbowColor.ORANGE, 4, 1);
        placeGrass(level, potPos.offset(6, -1, -6), RainbowColor.ORANGE);
        level.setBlock(potPos.offset(7, -1, -6), Blocks.GRASS_BLOCK.defaultBlockState(), 0);

        // YELLOW tree (complete)
        placeTree(level, potPos.offset(-6, 0, 6), RainbowColor.YELLOW, 4, 1);
        placeGrass(level, potPos.offset(-6, -1, 6), RainbowColor.YELLOW);

        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        assertEquals(2, result.size());
        assertTrue(result.containsKey(RainbowColor.RED));
        assertTrue(result.containsKey(RainbowColor.YELLOW));
    }

    @Test
    void testTreeLocatorTrunkOnlyTree(MinecraftServer server) {
        MockLevel level = new MockLevel();

        BlockPos potPos = new BlockPos(0, 60, 0);

        placeTree(level, potPos.offset(-6, 0, -6), RainbowColor.RED, 4, 0);
        placeGrass(level, potPos.offset(-6, -1, -6), RainbowColor.RED);

        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(RainbowColor.RED));
        PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree tree = result.get(RainbowColor.RED).getFirst();
        assertFalse(tree.trunkBlocks().isEmpty(), "Should find trunk blocks");
        assertTrue(tree.leaves().isEmpty(), "Should find no leaves");
    }

    @Test
    void testTreeLocatorMultipleTreesSameColor(MinecraftServer server) throws Exception {
        MockLevel level = new MockLevel();

        BlockPos potPos = new BlockPos(0, 60, 0);

        placeTree(level, potPos.offset(-6, 0, -6), RainbowColor.RED, 4, 1);
        placeGrass(level, potPos.offset(-6, -1, -6), RainbowColor.RED);

        placeTree(level, potPos.offset(-6, 0, 6), RainbowColor.RED, 4, 1);
        placeGrass(level, potPos.offset(-6, -1, 6), RainbowColor.RED);

        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree> redTrees = result.get(RainbowColor.RED);
        assertNotNull(redTrees, "RED trees should be found");
        assertEquals(2, redTrees.size(), "Should find exactly 2 RED trees");
    }

    static Stream<Arguments> treeOffsetArgs() {
        return Stream.of(
            Arguments.of(-7,  0, -7, false, false), // x/z not a valid scan position
            Arguments.of(-6,  2, -6, false, false), // y too high, outside scan range
            Arguments.of(-6, -2, -6, false, false), // y too low, outside scan range
            Arguments.of(-6, -1, -6, true,  false), // y at potPos.y-1, lowest valid scan y
            Arguments.of(-6,  1, -6, true,  false), // y at potPos.y+1, highest valid scan y
            Arguments.of(-6,  0, -6, false, true)   // valid offset, bottom trunk block removed
        );
    }

    @ParameterizedTest
    @MethodSource("treeOffsetArgs")
    void testTreeLocatorOffset(int dx, int dy, int dz, boolean expectedFound, boolean removeBottomTrunk, MinecraftServer server) {
        MockLevel level = new MockLevel();
        BlockPos potPos = new BlockPos(0, 60, 0);

        BlockPos treeBase = potPos.offset(dx, dy, dz);
        placeTree(level, treeBase, RainbowColor.RED, 4, 1);
        placeGrass(level, treeBase.below(), RainbowColor.RED);

        if (removeBottomTrunk) {
            level.setBlock(treeBase, Blocks.AIR.defaultBlockState(), 0);
        }

        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result =
                PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        assertEquals(expectedFound, result.containsKey(RainbowColor.RED));
    }
}
