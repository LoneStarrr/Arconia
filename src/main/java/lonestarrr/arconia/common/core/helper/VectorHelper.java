package lonestarrr.arconia.common.core.helper;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.world.phys.Vec3;

public class VectorHelper {
    /**
     * The goal of this hacky thing is to rotate a 2D drawing on (X,Y) such that the X axis is rotated along a specified vector.
     * The rotation point is (x,y) = (0,0).
     * It probably ruins 3D plots that also use the Z axis.
     *
     * I got this working somehow by sketching stuff out, but it nearly broke my brain. My 3D Matrix fu is seriously lacking, and
     * I should do some generic OpenGL tutorials in a playground.
     *
     * @param startPos Start position for vector to rotate along
     * @param endPos End position for vector to rotate along
     * @return
     */
    public static Quaternion getRotation(Vec3 startPos, Vec3 endPos) {
        /*
         * http://web.archive.org/web/20060914224155/
         * http://web.archive.org/web/20041029003853/
         * http://www.j3d.org/matrix_faq/matrfaq_latest.html#Q56
         *
         * After drawing this out:
         *
         * There is only rotation along y and z axis, since this assumes things being plotted along x and y.
         *
         * This probably could be done better by someone who is well versed in 3D matrix / game dev - but hey,
         * it works.
         *
         */

        Vec3 diff = endPos.subtract(startPos);
        float distance = (float)startPos.distanceTo(endPos);
        float rotationY, rotationZ;
        Vec3 vectorXZPlane = new Vec3(diff.x(), 0, diff.z());
        double vectorXZPlaneLength = vectorXZPlane.length();
        // Rotation in radians.
        rotationY = vectorXZPlaneLength == 0 ? 0 : (float)Math.acos(Math.abs(diff.x()) / vectorXZPlaneLength);
        if (diff.x() < 0) rotationY = (float)Math.PI - rotationY; // mirror X on Z axis
        rotationY = (diff.z() < 0 ? 1 : -1) * rotationY;
        rotationZ = (diff.y() == 0 ? 0 : (float)Math.acos(vectorXZPlaneLength / distance));
        rotationZ = (diff.y() < 0 ? -1 : 1) * rotationZ;
        Quaternion rotation = new Quaternion(0f, rotationY, rotationZ, false);
        return rotation;
    }

    /**
     * Calculates a quaternion for rotation based on rotating a vector to a new direction.
     *
     * Implementation taken from
     * http://www.opengl-tutorial.org/intermediate-tutorials/tutorial-17-quaternions/#how-do-i-find-the-rotation-between-2-vectors-
     *
     * @param a Vector to rotate
     * @param b Rotated vector
     * @return The quaternion needed to rotate vector a so that it matches vector b.
     */
    public static Quaternion getRotationBetweenVectors(Vec3 a, Vec3 b) {
        Vec3 start = a.normalize();
        Vec3 dest = b.normalize();
        float cosTheta = (float)start.dot(dest);
        Vec3 rotationAxis;

        if (cosTheta < -1 + 0.001f) {
            // Special case when vectors are in opposite direction. Guess one, as long as it's perpendicular to start
            rotationAxis = new Vec3(0,0, 1).cross(start);
            if (rotationAxis.length() < 0.01) {
                rotationAxis = new Vec3(1, 0, 0).cross(start);
            }
            rotationAxis = rotationAxis.normalize();
            return new Quaternion(new Vector3f((float)rotationAxis.x, (float)rotationAxis.y, (float)rotationAxis.z), 180f, true);
        }

        rotationAxis = a.cross(b);
        float s = (float)Math.sqrt((1 + cosTheta) * 2);
        float invs = 1 / s;
        return new Quaternion((float)rotationAxis.x * invs, (float)rotationAxis.y * invs, (float)rotationAxis.z * invs, s * 0.5f);
    }
}
