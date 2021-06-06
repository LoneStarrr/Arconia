package lonestarrr.arconia.common.core.helper;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

/**
 * Matrix helper methods
 */
public abstract class Matrix {
    /**
     * Rotate a 2D matrix clockwise by 90 degrees
     *
     * @param matrix        Matrix to rotate (MxN)
     * @param rotatedMatrix Rotated matrix elements are stored in this matrix. It must have the correct dimensions (NxM)
     */
    public static <T> void rotate2DMatrixClockWise(final T[][] matrix, final T[][] rotatedMatrix) {
        int rows = matrix.length;
        int columns = matrix[0].length;

        //Passed in rotatedMatrix is necessary because with a Generics type one cannot create a new array without
        //knowing the type
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                rotatedMatrix[c][r] = matrix[rows - r - 1][c];
            }
        }
    }

    /**
     * Given a coordinate in a 3d matrix, calculate the new coordinate in that matrix after a given amount of
     * clockwise 90 degree rotations of a horizontal plane along the x/z axis
     *
     * @param coordinate coordinate in matrix
     * @param matrixWidth   width of 2d matrix
     * @param matrixHeight  height of 2d matrix
     * @param rotationCount Number of clock-wise 90 degree rotations of the 2d matrix
     * @return coordinate in matrix after rotations
     */
    public static Vector3i getRotatedCoordinate(final Vector3i coordinate, final int matrixWidth, final int matrixHeight, final int rotationCount) {
        int x = coordinate.getX();
        int z = coordinate.getZ();
        int xNew = x, zNew = z;
        int w, h;

        // TODO unwind
        for (int i = 0; i < rotationCount % 4; i++) {
            x = xNew;
            z = zNew;
            w = (i % 2 == 1 ? matrixHeight : matrixWidth);
            h = (i % 2 == 1? matrixWidth: matrixHeight);
            xNew = h - 1 - z;
            zNew = x;
        }

        return new Vector3i(xNew, coordinate.getY(), zNew);
    }
}
