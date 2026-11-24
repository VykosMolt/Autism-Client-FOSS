/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package autismclient.aggressivepvp;

/**
 * What a look reaches, worked out the way a server works it out: a ray from the eye along the yaw and pitch it was
 * last sent, against the box of the block or crystal acted on. A click or a hit only counts on a strict server when
 * that ray enters the box within reach (Grim's RotationPlace for blocks, Reach and Hitboxes for entities), so every
 * action is put to this test before it is sent, and a look that is turned for an action is aimed with it.
 * Boxes are {minX, minY, minZ, maxX, maxY, maxZ}. No Minecraft types: LookMathCheck runs it.
 */
public final class LookMath {
    private LookMath() {}

    /** Faces in the game's own numbering (Direction.from3DDataValue). */
    public static final int DOWN = 0, UP = 1, NORTH = 2, SOUTH = 3, WEST = 4, EAST = 5;

    /** Where a look enters a box. */
    public record Entry(double distance, double x, double y, double z, int face) {}

    /** The unit vector of a look, as the game computes it (Entity.calculateViewVector). */
    public static double[] direction(float yaw, float pitch) {
        double p = Math.toRadians(pitch), y = Math.toRadians(-yaw);
        double cosP = Math.cos(p);
        return new double[]{Math.sin(y) * cosP, -Math.sin(p), Math.cos(y) * cosP};
    }

    /**
     * Where the look from this eye enters the box, null when it misses. An eye inside the box enters at distance 0,
     * at the eye, through no face (-1).
     */
    public static Entry enter(double ex, double ey, double ez, float yaw, float pitch, double[] box) {
        double[] d = direction(yaw, pitch);
        double[] o = {ex, ey, ez};
        double near = Double.NEGATIVE_INFINITY, far = Double.POSITIVE_INFINITY;
        int axis = -1;

        for (int a = 0; a < 3; a++) {
            double lo = box[a], hi = box[a + 3];
            if (Math.abs(d[a]) < 1e-12) {
                if (o[a] < lo || o[a] > hi) return null;
                continue;
            }
            double t1 = (lo - o[a]) / d[a], t2 = (hi - o[a]) / d[a];
            if (t1 > t2) {
                double t = t1;
                t1 = t2;
                t2 = t;
            }
            if (t1 > near) {
                near = t1;
                axis = a;
            }
            far = Math.min(far, t2);
        }

        if (near > far || far < 0) return null;
        if (near <= 0) return new Entry(0, ex, ey, ez, -1);

        // Going up an axis the ray comes in through the low face, going down it through the high one
        int face = switch (axis) {
            case 0 -> d[0] > 0 ? WEST : EAST;
            case 1 -> d[1] > 0 ? DOWN : UP;
            default -> d[2] > 0 ? NORTH : SOUTH;
        };
        return new Entry(near, ex + d[0] * near, ey + d[1] * near, ez + d[2] * near, face);
    }

    /** How far along the look the box is entered, infinity when the look misses it. */
    public static double reach(double ex, double ey, double ez, float yaw, float pitch, double[] box) {
        Entry entry = enter(ex, ey, ez, yaw, pitch, box);
        return entry == null ? Double.POSITIVE_INFINITY : entry.distance;
    }

    /** The distance from a point to the nearest point of a box, 0 inside it. */
    public static double nearest(double ex, double ey, double ez, double[] box) {
        double dx = Math.max(Math.max(box[0] - ex, 0), ex - box[3]);
        double dy = Math.max(Math.max(box[1] - ey, 0), ey - box[4]);
        double dz = Math.max(Math.max(box[2] - ez, 0), ez - box[5]);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * The yaw and pitch that look from the eye at the box: at its nearest point, pulled inside by inset so the look
     * still lands when the eye has moved a little by the time it is judged. The yaw continues from the given one (no
     * jump past half a turn). Null when the eye is inside the inset box, where any look will do.
     */
    public static float[] aim(double ex, double ey, double ez, double[] box, double inset, float fromYaw) {
        double[] e = {ex, ey, ez};
        double[] p = new double[3];
        boolean inside = true;
        for (int a = 0; a < 3; a++) {
            double in = Math.min(inset, (box[a + 3] - box[a]) / 2);
            double lo = box[a] + in, hi = box[a + 3] - in;
            p[a] = Math.max(lo, Math.min(hi, e[a]));
            if (p[a] != e[a]) inside = false;
        }
        if (inside) return null;

        double dx = p[0] - ex, dy = p[1] - ey, dz = p[2] - ez;
        double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        double pitch = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        return new float[]{(float) (fromYaw + wrap(yaw - fromYaw)), (float) Math.max(-90, Math.min(90, pitch))};
    }

    /** An angle brought into -180..180. */
    public static double wrap(double degrees) {
        double wrapped = degrees % 360;
        if (wrapped >= 180) wrapped -= 360;
        if (wrapped < -180) wrapped += 360;
        return wrapped;
    }
}
