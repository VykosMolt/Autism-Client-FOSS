import autismclient.aggressivepvp.MoveFix;

import java.util.Random;

public class AggressiveMoveFixCheck {
    static double heading(boolean f, boolean b, boolean l, boolean r, float yaw) {
        double x = (l ? 1 : 0) - (r ? 1 : 0), z = (f ? 1 : 0) - (b ? 1 : 0);
        double sin = Math.sin(Math.toRadians(yaw)), cos = Math.cos(Math.toRadians(yaw));
        return Math.toDegrees(Math.atan2(z * cos + x * sin, x * cos - z * sin));
    }

    static double apart(double a, double b) {
        double d = Math.abs(a - b) % 360;
        return d > 180 ? 360 - d : d;
    }

    public static void main(String[] args) {
        Random random = new Random(7);
        int checks = 0;
        double worst = 0;
        for (int i = 0; i < 200_000; i++) {
            boolean f = random.nextBoolean(), b = random.nextBoolean(), l = random.nextBoolean(), r = random.nextBoolean();
            float camera = random.nextFloat() * 720 - 360, sent = random.nextFloat() * 720 - 360;
            boolean whole = i % 5 == 0;
            if (whole) sent = camera + 45 * (random.nextInt(17) - 8);

            boolean[] keys = MoveFix.remap(f, b, l, r, camera, sent);
            boolean moving = (f != b) || (l != r);
            boolean movingAfter = (keys[0] != keys[1]) || (keys[2] != keys[3]);
            if (moving != movingAfter) throw new AssertionError("standing still must stay standing still, moving must stay moving");
            if (keys[0] && keys[1] || keys[2] && keys[3]) throw new AssertionError("opposite keys pressed together");
            if (moving) {
                double off = apart(heading(f, b, l, r, camera), heading(keys[0], keys[1], keys[2], keys[3], sent));
                worst = Math.max(worst, off);
                if (off > (whole ? 0.01 : 22.51)) throw new AssertionError("heading off by " + off + " (camera " + camera + ", sent " + sent + ")");
            }
            checks++;
        }
        boolean[] same = MoveFix.remap(true, false, false, true, 33.3f, 33.3f);
        if (!(same[0] && !same[1] && !same[2] && same[3])) throw new AssertionError("same yaw must keep the keys");
        boolean[] back = MoveFix.remap(true, false, false, false, 0, 180);
        if (!(!back[0] && back[1] && !back[2] && !back[3])) throw new AssertionError("a half turn must swap forward and backward");
        System.out.printf("PASS: AggressiveMoveFixCheck (%d cases, worst %.2f deg)%n", checks + 2, worst);
    }
}
