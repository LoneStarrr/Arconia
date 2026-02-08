package lonestarrr.arconia.common.block.entities;

import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import static lonestarrr.arconia.common.core.helper.ResourceLocationHelper.prefix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class WorldBuilderEntity extends BaseBlockEntity {
    private static DistributionTable distributionTable;

    private int blocksPerTick = 2; // How many blocks may be converted per game tick?
    private int blockRadiusHorizontal = 3;
    private int blockLevels = 7; // This many vertical levels to convert, starting with the level directly below the builder
    private int gameTicksPerBuilderTick = 1; // how often to tick
    private float boostFactor = 1F; // Multiplication factor to increase chance to convert into boostable blocks (typically non-stone blocks)

    private BlockState blockToMatch = Blocks.OAK_PLANKS.defaultBlockState(); // The block the builder is supposed to convert
    private ComputedDistribution currentDistribution; // Calculated weight-based distribution for current y level in process of being converted
    private List<BlockPos> blocksToConvert = null; // Calculated list of block positions in process of being converted
    private int currentBlockIndex = 0; // Index in blocksToConvert that is being converted
    private long lastBuilderTick = 0; // Last time the builder ticked

    public WorldBuilderEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WORLD_BUILDER.get(), pos, state);

    }

    @Override
    public void writePacketNBT(CompoundTag tag, HolderLookup.@NotNull Provider registries) {

    }

    @Override
    public void readPacketNBT(CompoundTag tag, HolderLookup.@NotNull Provider registries) {

    }

    public static void tick(Level level, BlockPos pos, BlockState state, WorldBuilderEntity blockEntity) {
        blockEntity.tickInternal();
    }

    private void tickInternal() {
        if (level == null || level.getGameTime() - lastBuilderTick < this.gameTicksPerBuilderTick) {
            return;
        }
        lastBuilderTick = level.getGameTime();

        if (blocksToConvert == null) {
            return;
        }

        for (int i = 0; i < blocksPerTick; i++) {
            if (currentBlockIndex >= blocksToConvert.size()) {
                blocksToConvert = null;
                currentBlockIndex = 0;
                currentDistribution = null;
                return;
            }

            BlockPos convertPos = blocksToConvert.get(currentBlockIndex);
            if (currentDistribution == null || convertPos.getY() != currentDistribution.yLevel) {
                currentDistribution = new ComputedDistribution(distributionTable.distributions.get("overworld"), convertPos.getY(), this.boostFactor, level.random);
                if (Arconia.logger.isDebugEnabled()) {
                    Arconia.logger.debug("Calculating new distribution for y=" + convertPos.getY() + ": " + currentDistribution);
                }
            }
            BlockState converted = currentDistribution.selectRandomBlock();
            level.setBlock(convertPos, converted, Block.UPDATE_ALL);
            level.playSound(null, convertPos, converted.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1, 1);
            currentBlockIndex++;
        }

    }

    /**
     * Recursive function to find all blocks of a specific blockstate that are connected through one of the cardinal directions and up/down
     * @param pos
     * @param toMatch
     * @param neighbors
     * @param maxPos x/y/z max values defining the region to search in (inclusive)
     * @param minPos x/y/z min values defining the region to search in (inclusive)
     */
    private void findConnectedNeighbors(BlockPos pos, final BlockState toMatch, final Set<BlockPos> neighbors, final BlockPos maxPos, final BlockPos minPos) {
        if (level == null) { return; }
        if (pos.getX() < minPos.getX() || pos.getX() > maxPos.getX()
                || pos.getY() < minPos.getY() || pos.getY() > maxPos.getY()
                || pos.getZ() < minPos.getZ() || pos.getZ() > maxPos.getZ()
                ) return;

        if (neighbors.contains(pos)) {
            return;
        }

        if (toMatch != null && !level.getBlockState(pos).equals(toMatch)) {
            return;
        }

        if (level.getBlockState(pos).getBlock() == Blocks.BEDROCK) {
            return;
        }

        neighbors.add(pos);
        for (BlockPos potential: new BlockPos[] { pos.above(), pos.below(), pos.north(), pos.south(), pos.east(), pos.west() }) {
            findConnectedNeighbors(potential, toMatch, neighbors, maxPos, minPos);
        }
    }

    public boolean startBuild(BlockState blockToMatch, float boostFactor) {
        if (level.isClientSide) {
            return false;
        }

        this.blockToMatch = blockToMatch;
        // Find all the blocks to be replaced, and sort them by y level
        List<BlockPos> blockPositions = findAndSortConnectedBlocks(this.blockToMatch);
        if (blockPositions.size() == 0) {
            return false;
        }
        blocksToConvert = blockPositions;
        currentBlockIndex = 0;
        this.boostFactor = boostFactor;
        return true;
    }

    public boolean isConverting() {
        return blocksToConvert != null;
    }

    private List<BlockPos> findAndSortConnectedBlocks(BlockState inputBlock) {
        Set<BlockPos> connectedBlocks = findConnectedBlocks(inputBlock);
        List<BlockPos> toBeConverted = sortBlocksByYlevel(connectedBlocks);
        return toBeConverted;
    }

    private Set<BlockPos> findConnectedBlocks(BlockState state) {
        BlockPos startPos = this.worldPosition.below();
        // Define the bounds of the area, which is defined by 2 blocks since it's a cuboid shape. The cuboid sits directly
        // underneath the world builder, with the builder sitting in the middle of the horizontal face.
        BlockPos minPos = startPos.offset(-this.blockRadiusHorizontal, -(this.blockLevels - 1), -this.blockRadiusHorizontal);
        BlockPos maxPos = startPos.offset(this.blockRadiusHorizontal, 0, this.blockRadiusHorizontal);
        BlockState toMatch = this.blockToMatch;
        Set<BlockPos> connectedBlocks = new HashSet<>((this.blockRadiusHorizontal * 2 + 1) * this.blockLevels);
        findConnectedNeighbors(startPos, toMatch, connectedBlocks, maxPos, minPos);
        return connectedBlocks;
    }

    private List<BlockPos> sortBlocksByYlevel(Set<BlockPos> positions) {
        List<BlockPos> sorted = new ArrayList<BlockPos>(positions);
        sorted.sort(Comparator.comparingInt(Vec3i::getY));
        return sorted;
    }

    /**
     * Load JSON-serialized table with blocks and weights to generate
     * @throws IOException
     */
    public static void loadDistributionTables() throws IOException {
        Arconia.logger.info("***** Loading distribution tables for world builder");
        ResourceLocation tableResource = prefix("world_builder/world_builder.json");
        InputStream in = Minecraft.getInstance().getResourceManager().getResource(tableResource).get().open();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        // Apparently you can't tell gson to throw an exception if it encounters an unknown field. wtf.
        Gson gson = new Gson();
        distributionTable = gson.fromJson(reader, DistributionTable.class);
        Arconia.logger.info("***** Loaded world builder distribution table: " + distributionTable);
    }
}

/** Represents a list of blocks to randomly select from for a specific Y level - the builder will replace blocks per y level, and caching
 * this per y level should make it not horribly inefficient.
 */
class ComputedDistribution {
    public int weightSum;
    public int[] weights;
    public int yLevel;
    public BlockState[] blocks;
    final RandomSource random;

    @NotNull
    public BlockState selectRandomBlock() {
        final int rndIndex = this.random.nextInt(this.weightSum);
        int weightsSeen = 0;
        for (int i = 0; i < weights.length; i++) {
            int weight = weights[i];
            if (rndIndex < weightsSeen + weight) {
                return blocks[i];
            }
            weightsSeen += weight;
        }
        throw new RuntimeException("Unreachable code"); // TODO remove me lololo
    }

    /**
     * Precalculates a block distribution with weights for a given y level
     * @param entries
     * @param yLevel
     * @param random
     */
    public ComputedDistribution(final List<DistributionEntry> entries, int yLevel, float boostFactor, final RandomSource random) {
        this.random = random;
        List<Integer> tmpWeights = new ArrayList<>(entries.size());
        List<BlockState> tmpBlocks = new ArrayList<>(entries.size());
        weightSum = 0;
        for (DistributionEntry entry: entries) {
            if (entry.yMin <= yLevel && entry.yMax >= yLevel) {
                int weight = entry.weight;
                if (entry.distribution == DistributionType.CENTER) {
                    // Based on that famous 1.18 minecraft ore distribution chart: Weight is highest in the middle, then linearly decreased the further out you go
                    float yRangeHalf = (entry.yMax - entry.yMin) / 2;
                    float yCenter = entry.yMin + yRangeHalf;
                    float yDistance = Math.abs(yCenter - yLevel); // distance to center, smaller = better
                    float pctRange = yDistance * 100 / yRangeHalf;
                    weight = Math.round((100 - pctRange) * weight / 100);
                    weight = Math.max(1, weight);
                }
                if (entry.boostable) {
                    weight = Math.round(weight * boostFactor);
                }
                if (yLevel <= 0) {
                    tmpWeights.add(weight);
                    tmpBlocks.add(entry.deepslateBlock.defaultBlockState());
                } else if (yLevel > 0 && yLevel < 8) {
                    //1..7 deepslate creeps in
                    int deepslateWeight = (int)Math.floor((1F - (yLevel / 8F)) * weight);
                    int stoneWeight = weight - deepslateWeight;
                    tmpWeights.add(deepslateWeight);
                    tmpBlocks.add(entry.deepslateBlock.defaultBlockState());
                    tmpWeights.add(stoneWeight);
                    tmpBlocks.add(entry.block.defaultBlockState());
                } else {
                    tmpWeights.add(weight);
                    tmpBlocks.add(entry.block.defaultBlockState());
                }
                weightSum += weight;
            }
        }
        this.weights = Ints.toArray(tmpWeights);
        this.blocks = tmpBlocks.toArray(new BlockState[0]);
        this.yLevel = yLevel;
    }

    @Override
    public String toString() {
        return "ComputedDistribution{" +
                "weightSum=" + weightSum +
                ", weights=" + Arrays.toString(weights) +
                ", yLevel=" + yLevel +
                ", blocks=" + Arrays.toString(blocks) +
                ", random=" + random +
                '}';
    }
}

enum DistributionType { @SerializedName("even") EVEN, @SerializedName("center") CENTER };

class DistributionEntry {
    @JsonAdapter(BlockJsonAdapter.class)
    public Block block;
    public int weight;
    @SerializedName("y_min") public int yMin;
    @SerializedName("y_max") public int yMax;
    public DistributionType distribution;
    @SerializedName("deepslate") @JsonAdapter(BlockJsonAdapter.class)public Block deepslateBlock;
    public boolean boostable = true;
    @Override
    public String toString() {
        return "DistributionEntry{" +
                "block='" + block + '\'' +
                ", weight=" + weight +
                ", yMin=" + yMin +
                ", yMax=" + yMax +
                ", distribution=" + distribution +
                ", deepslateBlock='" + deepslateBlock + '\'' +
                ", boostable=" + boostable +
                '}';
    }
}

class BlockJsonAdapter extends TypeAdapter<Block> {
    @Override
    public void write(JsonWriter out, Block value) throws IOException {
        // Not implemented since we only read
    }

    @Override
    public Block read(JsonReader in) throws IOException {
        String blockStr = in.nextString();
        ResourceLocation blockLoc = ResourceLocation.parse(blockStr);
        if (!BuiltInRegistries.BLOCK.containsKey(blockLoc)) {
            throw new RuntimeException("Unknown block " + blockStr + " in world builder configuration");
        }
        Block block = BuiltInRegistries.BLOCK.get(blockLoc);
        return block;
    }
}

class DistributionTable {
    Map<String, List<DistributionEntry>> distributions;

    @Override
    public String toString() {
        return "DistributionTable{" +
                "distributions=" + distributions +
                '}';
    }
}