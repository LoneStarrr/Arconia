package lonestarrr.arconia.common.core.helper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link Structures} methods.
 */
public class PlaceStructureTests {

    @BeforeAll
    public static void setup() {
        // Setup enough minecraft context to be able to access block registry
        //Loader.instance();
        Bootstrap.bootStrap();
//        Loader.instance().setupTestHarness(new DummyModContainer(new ModMetadata() {{
//            modId = "test";
//        }}));
    }

    @Test
    void testPlaceStructureHappyPath() {
        Iterator<BlockPos> pos;
        int[][] positions = new int[][]{{0, 0, 0}, {1, 0, 1}};
        List<BlockPos> blockPositions =
                Arrays.stream(positions).map((arr) -> new BlockPos(arr[0], arr[1], arr[2])).collect(Collectors.toList());
        List<BlockState> states = new ArrayList<BlockState>(Arrays.asList(
                Blocks.DIRT.defaultBlockState(),
                Blocks.AIR.defaultBlockState()
        ));

        List<BlockState> placedBlocks = new ArrayList<BlockState>();
        List<BlockPos> placedPositions = new ArrayList<BlockPos>();
        Structures.placeStructure(blockPositions.iterator(), states.iterator(), (p, s) -> {
            placedPositions.add(p);
            placedBlocks.add(s);
            return true;
        });
        assertEquals(2, placedBlocks.size());
        assertEquals(2, placedPositions.size());

    }

    /**
     * Test placing a bigger pattern and validate that the structure placer adds blocks in the correct order and
     * using the correct type. Also validates that {@link BlockPos#betweenClosed(BlockPos, BlockPos)} generates items in
     * the expected order.
     */
    @Test
    void testPlaceStructureBigPattern() {
        BlockState evenBlock = Blocks.STONE.defaultBlockState();
        BlockState oddBlock = Blocks.GRASS_BLOCK.defaultBlockState();
        int structureWidth = 40;
        int structureHeight = 40;
        int blockCount = structureWidth * structureHeight;

        BlockPos from = new BlockPos(0, 63, 0);
        BlockPos to = new BlockPos(structureWidth - 1, 63, structureHeight - 1);
        Stream<BlockPos> blocks = BlockPos.betweenClosedStream(from, to);
        List<BlockState> states = new ArrayList<BlockState>(blockCount);
        // iterator that generates a different block every 100 iterations
        IntStream.range(0, blockCount).forEach((i) -> {
            states.add((i / 100) % 2 == 0 ? evenBlock : oddBlock);
        });

        // Correct blocks placed in expected order (x first, then z) ?
        int[] lastIndexBag = new int[]{0}; // XXX le ugly
        Structures.placeStructure(blocks.iterator(), states.iterator(), (p, s) -> {
            int index = p.getX() + p.getZ() * structureHeight;
            assertEquals(lastIndexBag[0], index);
            lastIndexBag[0]++;
            assertEquals(((index / 100) % 2 == 0) ? evenBlock : oddBlock, s);
            assertEquals(from.getY(), p.getY());
            return true;
        });

        assertEquals(blockCount, lastIndexBag[0]);
    }
}
