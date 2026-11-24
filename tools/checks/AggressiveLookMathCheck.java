import autismclient.aggressivepvp.LookMath;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Random;

public class AggressiveLookMathCheck {
    static int checks;

    static void check(boolean ok, String what) {
        checks++;
        if (!ok) throw new AssertionError(what);
    }

    public static void main(String[] args) {
        Random random = new Random(11);

        for (int i = 0; i < 20_000; i++) {
            float yaw = random.nextFloat() * 1440 - 720, pitch = random.nextFloat() * 180 - 90;
            double[] d = LookMath.direction(yaw, pitch);
            Vec3 game = Vec3.directionFromRotation(pitch, yaw);
            check(Math.abs(d[0] - game.x) < 2e-4 && Math.abs(d[1] - game.y) < 2e-4 && Math.abs(d[2] - game.z) < 2e-4, "look vector at yaw " + yaw + " pitch " + pitch);
        }

        int hits = 0, misses = 0;
        for (int i = 0; i < 200_000; i++) {
            double[] box = {random.nextInt(9) - 4, random.nextInt(5) - 2, random.nextInt(9) - 4, 0, 0, 0};
            double w = random.nextBoolean() ? 1 : 2;
            box[3] = box[0] + w;
            box[4] = box[1] + (w == 1 ? 1 : 2);
            box[5] = box[2] + w;
            double ex = random.nextDouble() * 10 - 5, ey = random.nextDouble() * 6 - 1, ez = random.nextDouble() * 10 - 5;
            float yaw = random.nextFloat() * 360 - 180, pitch = random.nextFloat() * 180 - 90;
            if (i % 2 == 0) {
                double tx = box[0] + random.nextDouble() * (box[3] - box[0]) - ex, ty = box[1] + random.nextDouble() * (box[4] - box[1]) - ey, tz = box[2] + random.nextDouble() * (box[5] - box[2]) - ez;
                yaw = (float) (Math.toDegrees(Math.atan2(tz, tx)) - 90 + (random.nextDouble() - 0.5) * 20);
                pitch = (float) Math.max(-90, Math.min(90, -Math.toDegrees(Math.atan2(ty, Math.sqrt(tx * tx + tz * tz))) + (random.nextDouble() - 0.5) * 20));
            }

            AABB aabb = new AABB(box[0], box[1], box[2], box[3], box[4], box[5]);
            Vec3 eye = new Vec3(ex, ey, ez);
            double[] d = LookMath.direction(yaw, pitch);
            Optional<Vec3> clip = aabb.clip(eye, eye.add(d[0] * 64, d[1] * 64, d[2] * 64));
            LookMath.Entry entry = LookMath.enter(ex, ey, ez, yaw, pitch, box);

            if (aabb.contains(eye)) {
                check(entry != null && entry.distance() == 0 && entry.face() == -1, "an eye inside the box enters at 0");
                continue;
            }
            if (clip.isPresent() != (entry != null)) {
                AABB wide = aabb.inflate(1e-6), narrow = aabb.deflate(1e-6);
                boolean clear = wide.clip(eye, eye.add(d[0] * 64, d[1] * 64, d[2] * 64)).isPresent() == narrow.clip(eye, eye.add(d[0] * 64, d[1] * 64, d[2] * 64)).isPresent();
                check(!clear, "hit or miss differs from AABB.clip away from any edge");
                continue;
            }
            if (entry == null) {
                misses++;
                continue;
            }
            hits++;
            Vec3 p = clip.get();
            check(Math.abs(p.x - entry.x()) < 1e-6 && Math.abs(p.y - entry.y()) < 1e-6 && Math.abs(p.z - entry.z()) < 1e-6, "entry point differs from AABB.clip");
            check(Math.abs(eye.distanceTo(p) - entry.distance()) < 1e-6, "entry distance differs from AABB.clip");
            int f = entry.face();
            double[] pt = {entry.x(), entry.y(), entry.z()};
            double[] e = {ex, ey, ez};
            int axis = f < 2 ? 1 : f < 4 ? 2 : 0;
            boolean low = (f & 1) == 0;
            check(Math.abs(pt[axis] - (low ? box[axis] : box[axis + 3])) < 1e-9, "entry point is not on the face named");
            check(low ? e[axis] < box[axis] : e[axis] > box[axis + 3], "the face named does not face the eye");
            for (int a = 0; a < 3; a++) check(pt[a] >= box[a] - 1e-9 && pt[a] <= box[a + 3] + 1e-9, "entry point outside the box");
            check(LookMath.nearest(ex, ey, ez, box) <= entry.distance() + 1e-9, "nearest point farther than the entry");
        }
        check(hits > 5_000 && misses > 5_000, "random looks hit and miss");

        int aimed = 0;
        for (int i = 0; i < 200_000; i++) {
            boolean crystal = random.nextBoolean();
            double bx = random.nextInt(7) - 3, by = random.nextInt(4) - 2, bz = random.nextInt(7) - 3;
            double[] box = crystal ? new double[]{bx - 0.5, by, bz - 0.5, bx + 1.5, by + 2, bz + 1.5} : new double[]{bx, by, bz, bx + 1, by + 1, bz + 1};
            double ex = random.nextDouble() * 8 - 4, ey = random.nextDouble() * 5 - 1, ez = random.nextDouble() * 8 - 4;
            float from = random.nextFloat() * 1440 - 720;

            float[] look = LookMath.aim(ex, ey, ez, box, 0.25, from);
            if (look == null) {
                check(LookMath.nearest(ex, ey, ez, box) == 0, "no aim only from inside");
                continue;
            }
            aimed++;
            check(Math.abs(look[0] - from) <= 180.001, "yaw jump");
            check(look[1] >= -90 && look[1] <= 90, "pitch range");
            double reach = LookMath.reach(ex, ey, ez, look[0], look[1], box);
            double near = LookMath.nearest(ex, ey, ez, box);
            check(reach < Double.POSITIVE_INFINITY, "aimed look misses");
            check(reach <= near + 0.36, "aimed look past nearest");

            double mx = (random.nextDouble() - 0.5) * 0.2, my = (random.nextDouble() - 0.5) * 0.2, mz = (random.nextDouble() - 0.5) * 0.2;
            if (near > 0.3) check(LookMath.reach(ex + mx, ey + my, ez + mz, look[0], look[1], box) < Double.POSITIVE_INFINITY, "look misses after move");
            float jy = look[0] + (random.nextFloat() - 0.5f) * 1.5f, jp = Math.max(-90, Math.min(90, look[1] + (random.nextFloat() - 0.5f)));
            if (near > 0.3 && near < 4.5) check(LookMath.reach(ex, ey, ez, jy, jp, box) < Double.POSITIVE_INFINITY, "look misses with jitter");
        }
        check(aimed > 100_000, "most eyes outside");

        double worstFace = 0, worstBox = 0;
        for (int i = 0; i < 200_000; i++) {
            double bx = random.nextInt(9) - 4, by = random.nextInt(5) - 2, bz = random.nextInt(9) - 4;
            double[] block = {bx, by, bz, bx + 1, by + 1, bz + 1};
            double ex = random.nextDouble() * 10 - 5, ey = random.nextDouble() * 7 - 2, ez = random.nextDouble() * 10 - 5;
            double[] e = {ex, ey, ez};
            int face = -1;
            for (int f = 0; f < 6 && face < 0; f++) {
                int axis = f < 2 ? 1 : f < 4 ? 2 : 0;
                boolean low = (f & 1) == 0;
                if (low ? e[axis] < block[axis] - 0.05 : e[axis] > block[axis + 3] + 0.05) face = f;
            }
            if (face < 0) continue;
            int axis = face < 2 ? 1 : face < 4 ? 2 : 0;
            double[] slab = block.clone();
            if ((face & 1) == 0) slab[axis + 3] = slab[axis] + 0.1;
            else slab[axis] = slab[axis + 3] - 0.1;

            float[] look = LookMath.aim(ex, ey, ez, slab, 0.4, 0);
            if (look == null) continue;
            LookMath.Entry entry = LookMath.enter(ex, ey, ez, look[0], look[1], block);
            check(entry != null, "slab aim misses");
            worstFace = Math.max(worstFace, entry.distance() - LookMath.nearest(ex, ey, ez, block));

            double[] crystal = {bx - 0.5, by + 1, bz - 0.5, bx + 1.5, by + 3, bz + 1.5};
            look = LookMath.aim(ex, ey, ez, crystal, 0.25, 0);
            if (look != null) worstBox = Math.max(worstBox, LookMath.reach(ex, ey, ez, look[0], look[1], crystal) - LookMath.nearest(ex, ey, ez, crystal));
        }
        check(worstFace <= 0.45, "worstFace=" + worstFace);
        check(worstBox <= 0.15, "worstBox=" + worstBox);

        check(LookMath.wrap(190) == -170 && LookMath.wrap(-190) == 170 && LookMath.wrap(720) == 0 && LookMath.wrap(180) == -180, "wrap");

        double[] base = {2, 63, -3, 3, 64, -2};
        float[] look = LookMath.aim(0.5, 65.62, 0.5, base, 0.25, 0);
        LookMath.Entry entry = LookMath.enter(0.5, 65.62, 0.5, look[0], look[1], base);
        check(entry != null && entry.face() == LookMath.SOUTH, "whole block side");
        double[] top = {2, 63.9, -3, 3, 64, -2};
        look = LookMath.aim(0.5, 65.62, 0.5, top, 0.4, 0);
        entry = LookMath.enter(0.5, 65.62, 0.5, look[0], look[1], base);
        check(entry != null && entry.face() == LookMath.UP && entry.distance() < 4.45, "top face");
        for (int i = 0; i < 1000; i++) {
            LookMath.Entry moved = LookMath.enter(0.5 + (random.nextDouble() - 0.5) * 0.2, 65.62 + (random.nextDouble() - 0.5) * 0.2, 0.5 + (random.nextDouble() - 0.5) * 0.2, look[0], look[1], base);
            check(moved != null && moved.face() == LookMath.UP, "top face after move");
        }
        double[] crystal = {1.5, 64, -3.5, 3.5, 66, -1.5};
        look = LookMath.aim(0.5, 65.62, 0.5, crystal, 0.25, 0);
        check(LookMath.reach(0.5, 65.62, 0.5, look[0], look[1], crystal) < 2.95, "crystal reach");

        System.out.println("PASS: AggressiveLookMathCheck (" + checks + " checks)");
    }
}
