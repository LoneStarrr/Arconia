package lonestarrr.arconia.common.test;

import lonestarrr.arconia.common.block.ArconiumTreeLeaves;
import lonestarrr.arconia.common.block.RainbowGrassBlock;
import lonestarrr.arconia.common.block.entities.PotMultiBlockPrimaryBlockEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.TreeFinder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TreeFinder}.
 * Uses {@link EphemeralTestServerProvider} for registry bootstrap and
 * {@link MockLevel} for isolated block storage per test.
 */
@ExtendWith(EphemeralTestServerProvider.class)
class TreeFinderTests {

    @BeforeAll
    static void init(MinecraftServer server) {
        // Server parameter injection ensures registries are loaded
        
    }

    @Test
    void testTreeLocatorSingleTree(MinecraftServer server) {
        ServerLevel level = server.overworld();

        // Tree at x=-6, y=60, z=-6 (color RED)
        level.setBlock(new BlockPos(-6, 60, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 61, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 63, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 64, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-7, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-5, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -7), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -5), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);

        BlockPos potPos = new BlockPos(0, 60, 0);

        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(RainbowColor.RED));
        assertEquals(1, result.get(RainbowColor.RED).size());
    }

    @Test
    void testTreeLocatorMultipleTrees(MinecraftServer server) {
        ServerLevel level = server.overworld();

        // RED tree at x=-6, z=-6
        level.setBlock(new BlockPos(-6, 60, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 61, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 63, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 64, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-7, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-5, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -7), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -5), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);

        // ORANGE tree at x=6, z=-6
        level.setBlock(new BlockPos(6, 60, -6), new ArconiumTreeLeaves(RainbowColor.ORANGE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 61, -6), new ArconiumTreeLeaves(RainbowColor.ORANGE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 62, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 63, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 64, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(7, 61, -6), new RainbowGrassBlock(RainbowColor.ORANGE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(5, 61, -6), new RainbowGrassBlock(RainbowColor.ORANGE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 62, -7), new RainbowGrassBlock(RainbowColor.ORANGE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 62, -5), new RainbowGrassBlock(RainbowColor.ORANGE).defaultBlockState(), 0);

        // YELLOW tree at x=-6, z=6
        level.setBlock(new BlockPos(-6, 60, 6), new ArconiumTreeLeaves(RainbowColor.YELLOW).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 61, 6), new ArconiumTreeLeaves(RainbowColor.YELLOW).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 63, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 64, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-7, 61, 6), new RainbowGrassBlock(RainbowColor.YELLOW).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-5, 61, 6), new RainbowGrassBlock(RainbowColor.YELLOW).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, 7), new RainbowGrassBlock(RainbowColor.YELLOW).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, 5), new RainbowGrassBlock(RainbowColor.YELLOW).defaultBlockState(), 0);

        // GREEN tree at x=6, z=6
        level.setBlock(new BlockPos(6, 60, 6), new ArconiumTreeLeaves(RainbowColor.GREEN).defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 61, 6), new ArconiumTreeLeaves(RainbowColor.GREEN).defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 62, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 63, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 64, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(7, 61, 6), new RainbowGrassBlock(RainbowColor.GREEN).defaultBlockState(), 0);
        level.setBlock(new BlockPos(5, 61, 6), new RainbowGrassBlock(RainbowColor.GREEN).defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 62, 7), new RainbowGrassBlock(RainbowColor.GREEN).defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 62, 5), new RainbowGrassBlock(RainbowColor.GREEN).defaultBlockState(), 0);

        // LIGHT_BLUE tree at x=0, z=-6
        level.setBlock(new BlockPos(0, 60, -6), new ArconiumTreeLeaves(RainbowColor.LIGHT_BLUE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 61, -6), new ArconiumTreeLeaves(RainbowColor.LIGHT_BLUE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 62, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 63, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 64, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-1, 61, -6), new RainbowGrassBlock(RainbowColor.LIGHT_BLUE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(1, 61, -6), new RainbowGrassBlock(RainbowColor.LIGHT_BLUE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 62, -7), new RainbowGrassBlock(RainbowColor.LIGHT_BLUE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 62, -5), new RainbowGrassBlock(RainbowColor.LIGHT_BLUE).defaultBlockState(), 0);

        // BLUE tree at x=0, z=6
        level.setBlock(new BlockPos(0, 60, 6), new ArconiumTreeLeaves(RainbowColor.BLUE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 61, 6), new ArconiumTreeLeaves(RainbowColor.BLUE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 62, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 63, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 64, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-1, 61, 6), new RainbowGrassBlock(RainbowColor.BLUE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(1, 61, 6), new RainbowGrassBlock(RainbowColor.BLUE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 62, 7), new RainbowGrassBlock(RainbowColor.BLUE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 62, 5), new RainbowGrassBlock(RainbowColor.BLUE).defaultBlockState(), 0);

        // PURPLE tree at x=0, z=0
        level.setBlock(new BlockPos(0, 60, 0), new ArconiumTreeLeaves(RainbowColor.PURPLE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 61, 0), new ArconiumTreeLeaves(RainbowColor.PURPLE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 62, 0), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 63, 0), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 64, 0), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-1, 61, 0), new RainbowGrassBlock(RainbowColor.PURPLE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(1, 61, 0), new RainbowGrassBlock(RainbowColor.PURPLE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 62, -1), new RainbowGrassBlock(RainbowColor.PURPLE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(0, 62, 1), new RainbowGrassBlock(RainbowColor.PURPLE).defaultBlockState(), 0);

        BlockPos potPos = new BlockPos(0, 60, 0);

        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        assertEquals(6, result.size());
        assertTrue(result.containsKey(RainbowColor.RED));
        assertTrue(result.containsKey(RainbowColor.ORANGE));
        assertTrue(result.containsKey(RainbowColor.YELLOW));
        assertTrue(result.containsKey(RainbowColor.GREEN));
        assertTrue(result.containsKey(RainbowColor.LIGHT_BLUE));
        assertTrue(result.containsKey(RainbowColor.BLUE));
        assertTrue(result.containsKey(RainbowColor.PURPLE));
    }

    @Test
    void testTreeLocatorDuplicateTrees(MinecraftServer server) {
        ServerLevel level = server.overworld();


        // RED tree 1 at x=-6, z=-6
        level.setBlock(new BlockPos(-6, 60, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 61, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 63, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 64, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-7, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-5, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -7), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -5), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);

        // RED tree 2 at x=-6, z=6 (same color RED)
        level.setBlock(new BlockPos(-6, 60, 6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 61, 6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 63, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 64, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-7, 61, 6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-5, 61, 6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, 7), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, 5), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);

        BlockPos potPos = new BlockPos(0, 60, 0);

        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(RainbowColor.RED));
        assertEquals(2, result.get(RainbowColor.RED).size(), "Should find 2 trees of the same color");
    }

    @Test
    void testTreeLocatorIncompleteTree(MinecraftServer server) {
        ServerLevel level = server.overworld();

        // RED tree at x=-6, z=-6 (complete with colored grass)
        level.setBlock(new BlockPos(-6, 60, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 61, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 63, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 64, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-7, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-5, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -7), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -5), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);

        // ORANGE tree at x=6, z=-6 (incomplete - missing colored grass)
        level.setBlock(new BlockPos(6, 60, -6), new ArconiumTreeLeaves(RainbowColor.ORANGE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 61, -6), new ArconiumTreeLeaves(RainbowColor.ORANGE).defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 62, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 63, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 64, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(7, 61, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(5, 61, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 62, -7), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(6, 62, -5), Blocks.OAK_LOG.defaultBlockState(), 0);

        // YELLOW tree at x=-6, z=6 (complete with colored grass)
        level.setBlock(new BlockPos(-6, 60, 6), new ArconiumTreeLeaves(RainbowColor.YELLOW).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 61, 6), new ArconiumTreeLeaves(RainbowColor.YELLOW).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 63, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 64, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-7, 61, 6), new RainbowGrassBlock(RainbowColor.YELLOW).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-5, 61, 6), new RainbowGrassBlock(RainbowColor.YELLOW).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, 7), new RainbowGrassBlock(RainbowColor.YELLOW).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, 5), new RainbowGrassBlock(RainbowColor.YELLOW).defaultBlockState(), 0);

        BlockPos potPos = new BlockPos(0, 60, 0);

        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        assertEquals(2, result.size());
        assertTrue(result.containsKey(RainbowColor.RED));
        assertTrue(result.containsKey(RainbowColor.YELLOW));
        assertFalse(result.containsKey(RainbowColor.ORANGE), "Incomplete tree should not be found");
    }

    @Test
    void testTreeLocator_withValidTree(MinecraftServer server) throws Exception {
        ServerLevel level = server.overworld();

        BlockPos potPos = new BlockPos(0, 60, 0);

        // Tree at x=-6, z=-6 (color RED)
        level.setBlock(new BlockPos(-6, 60, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 61, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 63, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 64, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-7, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-5, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -7), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -5), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);

        // Find trees using TreeLocator
        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree> redTrees = result.get(RainbowColor.RED);
        assertNotNull(redTrees, "RED trees should be found");
        assertFalse(redTrees.isEmpty(), "Should find at least one RED tree");

        // Verify tree structure
        PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree tree = redTrees.getFirst();
        assertNotNull(tree, "Tree should not be null");

        // Verify wood and leaves are found
        List<BlockPos> woodBlocks = tree.trunkBlocks();
        List<BlockPos> leafBlocks = tree.leaves();
        assertFalse(woodBlocks.isEmpty(), "Should find at least one wood block");
        assertFalse(leafBlocks.isEmpty(), "Should find at least one leaf block");

        // Verify wood is at lower Y levels
        assertTrue(woodBlocks.getFirst().getY() < 64, "Wood should be at lower Y levels");
        // Verify leaves are at higher Y levels
        assertTrue(leafBlocks.getFirst().getY() >= 60, "Leaves should be at higher Y levels");
    }

    @Test
    void testTreeLocator_withMultipleTrees(MinecraftServer server) throws Exception {
        // Set up mock level with multiple RED trees
        ServerLevel level = server.overworld();

        BlockPos potPos = new BlockPos(0, 60, 0);

        // Tree 1 at x=-6, z=-6
        level.setBlock(new BlockPos(-6, 60, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 61, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 63, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 64, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-7, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-5, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -7), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -5), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);

        // Tree 2 at x=-6, z=6 (same color RED)
        level.setBlock(new BlockPos(-6, 60, 6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 61, 6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 63, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 64, 6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-7, 61, 6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-5, 61, 6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, 5), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, 7), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);

        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree> redTrees = result.get(RainbowColor.RED);
        assertNotNull(redTrees, "RED trees should be found");
        assertEquals(2, redTrees.size(), "Should find exactly 2 RED trees");
    }

    @Test
    void testTreeLocator_withCompleteTree(MinecraftServer server) throws Exception {
        // Set up mock level with a complete RED tree (with colored grass)
        ServerLevel level = server.overworld();

        BlockPos potPos = new BlockPos(0, 60, 0);

        // Complete RED tree at x=-6, z=-6
        level.setBlock(new BlockPos(-6, 60, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 61, -6), new ArconiumTreeLeaves(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 63, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 64, -6), Blocks.OAK_LOG.defaultBlockState(), 0);
        level.setBlock(new BlockPos(-7, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-5, 61, -6), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -7), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);
        level.setBlock(new BlockPos(-6, 62, -5), new RainbowGrassBlock(RainbowColor.RED).defaultBlockState(), 0);

        // Find trees
        Map<RainbowColor, List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree>> result = PotMultiBlockPrimaryBlockEntity.TreeLocator.locateTrees(level, potPos);

        List<PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree> redTrees = result.get(RainbowColor.RED);
        assertNotNull(redTrees, "RED trees should be found");
        assertFalse(redTrees.isEmpty(), "Should find at least one RED tree");

        // Verify tree structure
        PotMultiBlockPrimaryBlockEntity.TreeLocator.Tree tree = redTrees.getFirst();
        assertNotNull(tree, "Tree should not be null");

        // Verify wood and leaves are found
        List<BlockPos> woodBlocks = tree.trunkBlocks();
        List<BlockPos> leafBlocks = tree.leaves();
        assertFalse(woodBlocks.isEmpty(), "Should find at least one wood block");
        assertFalse(leafBlocks.isEmpty(), "Should find at least one leaf block");
    }
}
