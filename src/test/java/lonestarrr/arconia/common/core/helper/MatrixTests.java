package lonestarrr.arconia.common.core.helper;

import net.minecraft.util.math.vector.Vector3i;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MatrixTests {
    @ParameterizedTest(name = "Run {index}: input={0}, expected={1}")
    @MethodSource("testRotate2dMatrix_Parameters")
    void testRotate2DMatrix(final Integer[][] testMatrix, final Integer[][] expectedMatrix) {
        Integer[][] rotatedMatrix = new Integer[testMatrix[0].length][testMatrix.length];
        Matrix.rotate2DMatrixClockWise(testMatrix, rotatedMatrix);
        assertTrue(Arrays.deepEquals(expectedMatrix, rotatedMatrix));
    }

    static Stream<Arguments> testRotate2dMatrix_Parameters() throws Throwable {
        return Stream.of(
                Arguments.of(
                        new Integer[][]{
                                {0, 1},
                                {2, 3},
                        },
                        new Integer[][]{
                                {2, 0},
                                {3, 1},
                        }

                ),
                Arguments.of(
                        new Integer[][]{
                                {0},
                                {1},
                        },
                        new Integer[][]{
                                {1, 0},
                        }

                )
        );
    }

    @ParameterizedTest(name = "Run {index}: coordinate={0}, width={1}, height={2}, rotations={3} expected={4}")
    @MethodSource("testGetRotatedCoordinate_Parameters")
    void testGetRotatedCoordinate(Vector3i coordinate, final int matrixWidth, final int matrixHeight,
                                  final int rotationCount, final Vector3i expectedCoordinate) {
        Vector3i rotatedCoordinate = Matrix.getRotatedCoordinate(coordinate, matrixWidth, matrixHeight, rotationCount);
        assertEquals(expectedCoordinate, rotatedCoordinate);
    }

    static Stream<Arguments> testGetRotatedCoordinate_Parameters() throws Throwable {
        return Stream.of(
                // Single rotation of 3x4 matrix
                Arguments.of(new Vector3i(0, 10, 0), 3, 4, 1, new Vector3i(3, 10, 0)),
                Arguments.of(new Vector3i(0, 10, 3), 3, 4, 1, new Vector3i(0, 10, 0)),
                // 2..3 rotations
                Arguments.of(new Vector3i(0, 10, 3), 3, 4, 2, new Vector3i(2, 10, 0)),
                Arguments.of(new Vector3i(0, 10, 3), 3, 4, 3, new Vector3i(3, 10, 2)),
                // No [effective] rotation => no change
                Arguments.of(new Vector3i(0, 10, 3), 3, 4, 0, new Vector3i(0, 10, 03)),
                Arguments.of(new Vector3i(0, 10, 3), 3, 4, 4, new Vector3i(0, 10, 03))
        );
    }
}
