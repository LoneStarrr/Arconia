package lonestarrr.arconia.common.core;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.helper.BlockPatternException;
import lonestarrr.arconia.common.core.helper.LibBlockPattern;
import lonestarrr.arconia.common.core.helper.Matrix;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a 2D pattern of blockstates to be built by a player to progress in the game. The pattern layouts
 * are stored under the assets/ resources dir in a JSON format.
 */
public class BuildPattern {
    private final List<List<BlockState>> pattern;
    private final Map<Direction, BlockState[][]> rotations; //precalculated rotations

    /**
     * Instantiates a pattern.
     * @param res
     *   ResourceLocation of the pattern
     * @return Instance of this class
     * @throws IOException
     * @throws BlockPatternException
     */
    public static BuildPattern loadPattern(final ResourceLocation res) throws IOException,
            BlockPatternException {
        List<List<BlockState>> pattern = LibBlockPattern.readBlockPattern(res);
        Map<Direction, BlockState[][]> rotations = createRotations(pattern);
        for (Map.Entry<Direction, BlockState[][]> entry: rotations.entrySet()) {
            BlockState[][] rotated = entry.getValue();
            Arconia.logger.info("Pattern for direction " + entry.getKey().toString());
            logPattern(rotated);
        }
        return new BuildPattern(pattern, rotations);
    }

    private static void logPattern(final BlockState[][] pattern) {
        StringBuilder builder = new StringBuilder();
        Block firstBlock = null;
        builder.append('\n');
        for (BlockState[] row: pattern) {
            for (BlockState col: row) {
                if (firstBlock == null) {
                    firstBlock = col.getBlock(); //assume first block in pattern is 'filler' block
                }
                char c = (col.getBlock() == firstBlock ? ' ': 'X');
                builder.append(c);
            }
            builder.append('\n');
        }
        Arconia.logger.info("Pattern: " + builder.toString());
    }
    /**
     * @return Width of the pattern
     */
    public int getWidth() {
        return this.pattern.get(0).size();
    }

    /**
     * @return Height of the pattern
     */
    public int getHeight() {
        return this.pattern.size();
    }

    /**
     * @param y Desired Y coordinate in return value as only X and Z are relevant
     * @return
     *   Pattern coordinate of bottom-left corner
     */
    public Vec3i getBottomLeftCoordinate(int y) {
        return new Vec3i(0, y, this.getHeight() - 1);
    }
    /**
     * Produces a list of expected blockstates and block positions if a pattern were located in the world at a
     * given world position and pattern orientation.
     *
     * @param dir
     *   Direction along which pattern is rotated. Rotation is clockwise, starting at north (no rotation). Up/Down
     *   are considered as no rotation.
     * @param patternPos
     *   Anchor coordinate in the _unrotated_ pattern that will be placed at location {worldPos} in the world.
     *   Y component is ignored.
     * @param worldPos
     *   The world position where the pattern anchor indicated by patternPos is located.
     * @return
     *  List of BlockPos, BlockState pairs. Order of list is always left to right (increasing X), top to bottom
     *  (increasing Z). Y coordinate equals that of worldPos
     */
    public Map<BlockPos, BlockState> getBlockStates(@Nonnull Direction dir, @Nonnull Vec3i patternPos, @Nonnull BlockPos worldPos) {
        BlockState[][] rotation = this.rotations.get(dir);
        Arconia.logger.info("Pattern for direction " + dir.toString() + ": ");
        logPattern(rotation);
        Vec3i anchorPatternPos = this.calculateRotatedAnchorCoordinate(patternPos, dir);
        Arconia.logger.info("Rotated pattern anchor coordinate: " + anchorPatternPos.toString());
        final int startX = worldPos.getX() - anchorPatternPos.getX();
        final int startZ = worldPos.getZ() - anchorPatternPos.getZ();
        Arconia.logger.info("Starting coordinate of pattern in world (x,z) = (" + startX + "," + startZ + ")");
        final int width = rotation[0].length;
        final int height = rotation.length;
        final int y = worldPos.getY();
        Map<BlockPos, BlockState> result = new HashMap<>(width * height);

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                BlockPos pos = new BlockPos(x + startX, y, z + startZ);
                BlockState blockState = rotation[z][x];
                result.put(pos, blockState);
                //Arconia.logger.info("Expected at (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "): " + blockState.getBlock().getRegistryName());
            }
        }
        return result;
    }

    private BuildPattern(final List<List<BlockState>> pattern, final Map<Direction, BlockState[][]> rotations) {
        this.pattern = pattern;
        this.rotations = rotations;
    }

    /**
     * Given a coordinate in the 2D build pattern, calculate the new X,Z coordinate in the rotated pattern
     * @param anchorCoordinate
     *   Coordinate in the build pattern
     * @param dir
     *   Direction the pattern will be rotated in. Rotation is clockwise with North representing no rotation. Up/Down
     *   are considered no rotation.
     * @return
     *   New coordinate
     */
    private Vec3i calculateRotatedAnchorCoordinate(@Nonnull final Vec3i anchorCoordinate, @Nonnull final Direction dir) {
        int patternWidth = this.pattern.get(0).size();
        int patternHeight = this.pattern.size();
        int rotationCount;

        switch(dir) {
            case EAST:
                rotationCount = 1;
                break;
            case SOUTH:
                rotationCount = 2;
                break;
            case WEST:
                rotationCount = 3;
                break;
            default:
                rotationCount = 0;
        }

        return Matrix.getRotatedCoordinate(anchorCoordinate, patternWidth, patternHeight, rotationCount);
    }

    // TODO: Add method that streams blockstates / blockpositions

    /**
     * Precalculate rotated build patterns for each horizontal direction
     * @param pattern Pattern to rotate
     * @return rotated patterns
     */
    private static Map<Direction, BlockState[][]> createRotations(List<List<BlockState>> pattern) {
        Map<Direction, BlockState[][]> rotations = new HashMap<Direction, BlockState[][]>(4);

        //convert nested lists to 2d array for easier manipulation
        BlockState[][] rotatedPattern =
                pattern.stream().map(u -> u.toArray(new BlockState[0])).toArray(BlockState[][]::new);

        List<Direction> directions = new ArrayList<>(3);
        directions.add(Direction.EAST);
        directions.add(Direction.SOUTH);
        directions.add(Direction.WEST);

        rotations.put(Direction.NORTH, rotatedPattern);

        for (Direction dir: directions) {
            int N = rotatedPattern.length;
            int M = rotatedPattern[0].length;
            BlockState[][] nextPattern = new BlockState[M][N];
            Matrix.rotate2DMatrixClockWise(rotatedPattern, nextPattern);
            rotations.put(dir, nextPattern);
            rotatedPattern = nextPattern;
        }

        return rotations;
    }
}