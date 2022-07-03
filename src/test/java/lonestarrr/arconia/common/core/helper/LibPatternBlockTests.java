package lonestarrr.arconia.common.core.helper;

import com.google.common.io.CharStreams;
import com.google.gson.JsonSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.Main;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link LibBlockPattern} class
 */
public class LibPatternBlockTests {

    @BeforeAll
    public static void setup() {
        // Setup enough minecraft context to be able to access block registry
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void testReadBlockPatternHappyCase() throws BlockPatternException {
        String jsonString = "{'pattern': ['00','11'], 'blocks': {'0': {'blockId':'minecraft:lime_wool'}, " +
                "'1': {'blockId':'minecraft:dirt'}}}";
        jsonString = jsonString.replaceAll("'", "\"");
        List<List<BlockState>> blocks = LibBlockPattern.readBlockPattern(jsonString);
        BlockState woolLime = Blocks.LIME_WOOL.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        assertEquals(blocks,
                new ArrayList<List<BlockState>>(
                        Arrays.asList(
                                new ArrayList<BlockState>(Arrays.asList(woolLime, woolLime)),
                                new ArrayList<BlockState>(Arrays.asList(dirt, dirt))
                        )
                )
        );
    }

    @Test
    void testReadBlockPatternLarge() throws BlockPatternException, IOException {
        InputStream is = getClass().getResourceAsStream("/assets/block_pattern_hard.json");
        String text;
        try (final Reader reader = new InputStreamReader(is)) {
            text = CharStreams.toString(reader);
        }
        List<List<BlockState>> blocks = LibBlockPattern.readBlockPattern(text);
        assertTrue(blocks.size() == 34);
        blocks.forEach(row -> assertEquals(42, row.size()));
        // Spot checks
        BlockState[][] array =
                blocks.stream().map(u -> u.toArray(new BlockState[0])).toArray(BlockState[][]::new);
        assertEquals(Blocks.AIR.defaultBlockState(), array[0][0]);
        assertEquals(Blocks.AIR.defaultBlockState(), array[33][41]);
        assertEquals(Blocks.GREEN_CONCRETE.defaultBlockState(), array[0][12]);
        assertEquals(Blocks.EMERALD_BLOCK.defaultBlockState(), array[24][21]);
    }

    @Test
    void testReadBlockPatternInvalidJson() {
        String jsonString = "{\"bogus data}";
        Assertions.assertThrows(JsonSyntaxException.class, () -> LibBlockPattern.readBlockPattern(jsonString));
    }

    @Test
    void testReadBlockPatternMissingKeys() {
        String jsonString = "{}";
        Assertions.assertThrows(BlockPatternException.class, () -> LibBlockPattern.readBlockPattern(jsonString));
    }

    @Test
    void testDeserializeBlockStateHappyCase() throws BlockPatternException {
        BlockState state = LibBlockPattern.deserializeBlockState("minecraft:lime_wool");
        assert state.getBlock() == Blocks.LIME_WOOL;
    }

    @Test
    void testDeserializeBlockStateHappyCaseNoMetadata() throws BlockPatternException {
        assertEquals(Blocks.LIME_WOOL.defaultBlockState(), LibBlockPattern.deserializeBlockState("minecraft:lime_wool"));
    }

    @Test
    void testDeserializeBlockStateUnknownBlock() {
        Assertions.assertThrows(BlockPatternException.class,
                () -> LibBlockPattern.deserializeBlockState("bobross:happy_tree"));
    }

}
