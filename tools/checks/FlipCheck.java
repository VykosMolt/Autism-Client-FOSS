package autismclient.ai.a;

import java.util.Arrays;
import java.util.Random;
import net.minecraft.util.Mth;

public class FlipCheck {
   static float dist(float yawA, float pitchA, float yawB, float pitchB) {
      float dy = Mth.wrapDegrees(yawA - yawB);
      float dp = pitchA - pitchB;
      return (float) Math.sqrt(dy * dy + dp * dp);
   }

   // The search loop as it was in EventBus/MapStore before the shared helper.
   static int[] old(float[] yaws, float[] pitches, int next, int filled, int maxK, float back, float outMin, float yaw, float pitch) {
      int n = yaws.length;
      for (int k = 2; k <= Math.min(filled, maxK); k++) {
         int then = Math.floorMod(next - k, n);
         if (!(dist(yaw, pitch, yaws[then], pitches[then]) > back)) {
            float out = 0.0F;
            int peak = then;
            for (int j = 1; j < k; j++) {
               int at = Math.floorMod(next - j, n);
               float a = dist(yaws[at], pitches[at], yaws[then], pitches[then]);
               if (a > out) {
                  out = a;
                  peak = at;
               }
            }
            if (!(out < outMin)) {
               return new int[]{k, then, peak};
            }
         }
      }
      return null;
   }

   public static void main(String[] args) {
      Random random = new Random(7);
      int found = 0;
      for (int round = 0; round < 20000; round++) {
         boolean mapStore = round % 2 == 0;
         int n = 16 + random.nextInt(8);
         float[] yaws = new float[n];
         float[] pitches = new float[n];
         float scale = mapStore ? 60.0F : 6.0F;
         for (int i = 0; i < n; i++) {
            yaws[i] = (random.nextFloat() - 0.5F) * scale * 2;
            pitches[i] = (random.nextFloat() - 0.5F) * scale;
         }
         int next = random.nextInt(n);
         int filled = random.nextInt(n + 1);
         int back0 = Math.floorMod(next - 2 - random.nextInt(4), n);
         boolean comesBack = random.nextBoolean();
         float yaw = comesBack ? yaws[back0] : (random.nextFloat() - 0.5F) * scale * 2;
         float pitch = comesBack ? pitches[back0] : (random.nextFloat() - 0.5F) * scale;
         int maxK = mapStore ? 12 : 10;
         float back = mapStore ? 10.0F : 1.0F;
         float outMin = mapStore ? 25.0F : 2.5F;
         int[] expected = old(yaws, pitches, next, filled, maxK, back, outMin, yaw, pitch);
         int[] actual = RenderState.auditFlip(yaws, pitches, next, filled, maxK, back, outMin, yaw, pitch);
         if (!Arrays.equals(expected, actual)) {
            System.out.println("FAIL FlipCheck: round " + round + " expected " + Arrays.toString(expected) + " got " + Arrays.toString(actual));
            System.exit(1);
         }
         if (expected != null) {
            found++;
         }
      }
      if (found < 100) {
         System.out.println("FAIL FlipCheck: too few flips exercised (" + found + ")");
         System.exit(1);
      }
      System.out.println("PASS FlipCheck (" + found + " flips in 20000 rings)");
   }
}
