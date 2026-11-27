package autismclient.modules;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

final class StrafeInputCorrector {
   private Vec3 lastPosition;
   private float lastWantedYaw;
   private int lastTick = -2147483648;
   private int load = -2147483648;
   private int read;

   Input init(Input raw, Vec3 position, float cameraYaw, float aimYaw, double drag, int now, StrafeInputCorrector.Inner_DataUtil step) {
      double f = (raw.forward() ? 1 : 0) - (raw.backward() ? 1 : 0);
      double s = (raw.left() ? 1 : 0) - (raw.right() ? 1 : 0);
      if (f == 0.0 && s == 0.0) {
         this.init();
         return raw;
      }

      float wantedWorld = cameraYaw - (float)Math.toDegrees(Math.atan2(s, f));
      if (this.lastPosition == null || now != this.lastTick + 1 || Math.abs(Mth.wrapDegrees(wantedWorld - this.lastWantedYaw)) > 3.0F) {
         this.lastPosition = position;
         this.lastWantedYaw = wantedWorld;
         this.load = -2147483648;
         this.read = 0;
      }

      this.lastTick = now;
      double a = Math.toRadians(wantedWorld);
      double px = Math.cos(a);
      double pz = Math.sin(a);
      double error = (position.x - this.lastPosition.x) * px + (position.z - this.lastPosition.z) * pz;
      double relative = Mth.wrapDegrees(aimYaw - wantedWorld) / 45.0;
      int low = (int)Math.floor(relative);
      int high = low + 1;
      Input first = init(low, raw);
      Input second = init(high, raw);
      Vec3 v1 = step.movement(first);
      Vec3 v2 = step.movement(second);
      double divisor = Math.max(0.05, 1.0 - drag);
      double c1 = Math.abs(error + (v1.x * px + v1.z * pz) / divisor);
      double c2 = Math.abs(error + (v2.x * px + v2.z * pz) / divisor);
      int chosen = c1 <= c2 ? low : high;
      if (this.read < 2) {
         if (this.load == Math.floorMod(low, 8) && c1 <= Math.min(c1, c2) + 0.05) {
            chosen = low;
         }

         if (this.load == Math.floorMod(high, 8) && c2 <= Math.min(c1, c2) + 0.05) {
            chosen = high;
         }
      }

      int next = Math.floorMod(chosen, 8);
      this.read = next == this.load ? this.read + 1 : 1;
      this.load = next;
      return chosen == low ? first : second;
   }

   private static Input init(int octant, Input raw) {
      int n = Math.floorMod(octant, 8);
      boolean f = n == 7 || n == 0 || n == 1;
      boolean b = n == 3 || n == 4 || n == 5;
      boolean l = n == 1 || n == 2 || n == 3;
      boolean r = n == 5 || n == 6 || n == 7;
      return new Input(f, b, l, r, raw.jump(), raw.shift(), f && raw.sprint());
   }

   void init() {
      this.lastPosition = null;
      this.lastTick = -2147483648;
      this.load = -2147483648;
      this.read = 0;
   }

   interface Inner_DataUtil {
      Vec3 movement(Input var1);
   }
}
