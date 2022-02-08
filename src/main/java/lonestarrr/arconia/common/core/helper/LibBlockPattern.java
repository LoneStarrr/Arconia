package lonestarrr.arconia.common.core.helper;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Definition of a block in a json file, used to deserialize json.
 */
class BlockDefinition {
    private String blockId;

    public BlockDefinition(String blockId, Map<String, String> metadata) {
        this.blockId = blockId;
    }

    public String getBlockId() {
        return blockId;
    }

    @Override
    public String toString() {
        return "id: " + this.blockId;
    }
}


/**
 * Abstract class containing methods to read a JSON-formatted file containing a 2D matrix of blocks.
 */
public abstract class LibBlockPattern {
    public static List<List<BlockState>> readBlockPattern(ResourceLocation location) throws IOException,
            BlockPatternException {
        /*
         * Example file:
         * {
         *   "pattern": [
         *     "111",
         *     "121",
         *     "111"
         *   ],
         *   "blocks": {
         *     "1": {"id": "minecraft:lime_wool"},
         *     "2": {"ID": "arconia:block_colored_soil"}
         *   }
         */
        InputStream in = Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        Gson gson = new Gson();
        JsonElement je = gson.fromJson(reader, JsonElement.class);
        JsonObject json = je.getAsJsonObject();
        return parseJson(json);
    }

    public static List<List<BlockState>> readBlockPattern(String jsonString) throws BlockPatternException {
        Gson gson = new Gson();
        JsonElement je = gson.fromJson(jsonString, JsonElement.class);
        JsonObject json = je.getAsJsonObject();

        return parseJson(json);
    }

    private static List<List<BlockState>> parseJson(JsonObject json) throws BlockPatternException {
        Gson gson = new Gson();

        //TODO: why not make a class representing the entire data structure including pattern/blocks/..?
        JsonElement pattern = json.get("pattern");
        if (pattern == null) {
            throw new BlockPatternException("Missing pattern key");
        }

        JsonElement blocks = json.get("blocks");
        if (blocks == null) {
            throw new BlockPatternException("Missing blocks key");
        }

        List<String> patternLines;
        try {
            Type patternType = new TypeToken<List<String>>() {
            }.getType();
            patternLines = gson.fromJson(pattern, patternType);
        } catch (JsonSyntaxException jse) {
            throw new BlockPatternException("Invalid pattern data structure");

        }

        Map<String, BlockDefinition> blockDefinitionMap;
        try {
            Type blocksType = new TypeToken<Map<String, BlockDefinition>>() {
            }.getType();
            blockDefinitionMap = gson.fromJson(blocks, blocksType);
        } catch (JsonSyntaxException jse) {
            throw new BlockPatternException("Invalid blocks data structure");
        }

        Map<String, BlockState> blockMap = new HashMap<String, BlockState>(blockDefinitionMap.size());
        for (Map.Entry<String, BlockDefinition> blockDefEntry : blockDefinitionMap.entrySet()) {
            BlockDefinition blockDef = blockDefEntry.getValue();
            blockMap.put(blockDefEntry.getKey(), LibBlockPattern.deserializeBlockState(blockDef.getBlockId()));
        }

        int patternWidth = 0;
        List<List<BlockState>> result = new ArrayList<List<BlockState>>(patternLines.size());
        for (String line : patternLines) {
            if (patternWidth == 0) {
                patternWidth = line.length();
            } else if (patternWidth != line.length()) {
                // TODO: write a test for me
                throw new BlockPatternException("Not all pattern entries are of equal length");
            }

            List<BlockState> lineBlocks = new ArrayList<BlockState>(patternWidth);
            for (char c : Lists.charactersOf(line)) {
                String patternId = String.valueOf(c);
                BlockState blockState = blockMap.get(patternId);

                if (blockState == null) {
                    // TODO write a test for me
                    throw new BlockPatternException("Pattern contains characters not in blocks: " + c);
                }
                lineBlocks.add(blockState);
            }
            result.add(lineBlocks);
        }

        return result;
    }

    /**
     * Given a block by name (e.g. minecraft:wool) and a map of metadata (e.g. {"color":"lime"}, produce a
     * BlockState.
     *
     * @param blockByName Name of the block, including mod id
     * @return IBlockstate instance
     */
    public static BlockState deserializeBlockState(String blockByName) throws BlockPatternException {
        String[] blockNameParts = blockByName.split(":");
        String namespace = blockNameParts[0];
        String blockId = blockNameParts[1];
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(namespace, blockId));

        if (block == null || !block.getRegistryName().getNamespace().equals(namespace) || !block.getRegistryName().getPath().equals(blockId)) {
            throw new BlockPatternException("Unknown block: " + blockByName);
        }

        BlockState blockState = block.defaultBlockState();
        return blockState;
    }
}
