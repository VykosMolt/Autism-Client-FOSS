package autismclient.util;

import autismclient.compat263.Compat263;
import java.util.Arrays;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class cj {
   public static final int init_I = 3;
   private int load_I = -2147483648;
   private Vec3 init_Vec3;
   private int read;
   private Vec3 load_Vec3 = Vec3.ZERO;
   private final Vec3[] init_Vec3Arr = new Vec3[32];
   private final int[] init_IArr = new int[32];

   public void init(LivingEntity target, int tick) {
      if (target == null) {
         this.load_I = -2147483648;
         this.init_Vec3 = null;
         this.load_Vec3 = Vec3.ZERO;
         Arrays.fill(this.init_Vec3Arr, null);
      } else {
         this.init(target.getId(), init_Vec3_LivingEntity(target), tick);
      }
   }

   void init(int targetId, Vec3 now, int tick) {
      if (targetId != this.load_I || this.init_Vec3 == null) {
         this.load_I = targetId;
         Arrays.fill(this.init_Vec3Arr, null);
         this.load_Vec3 = Vec3.ZERO;
         this.init_Vec3 = now;
         this.read = tick;
      } else if (!now.equals(this.init_Vec3)) {
         this.load_Vec3 = now.subtract(this.init_Vec3).scale(1.0 / Math.max(1, Math.min(4, tick - this.read)));
         this.init_Vec3 = now;
         this.read = tick;
      }

      int slot = tick & this.init_Vec3Arr.length - 1;
      this.init_Vec3Arr[slot] = now;
      this.init_IArr[slot] = tick;
   }

   public Vec3 init(int tick, int age) {
      int wanted = tick - Math.max(0, Math.min(this.init_Vec3Arr.length - 1, age));
      int slot = wanted & this.init_Vec3Arr.length - 1;
      return this.init_Vec3Arr[slot] != null && this.init_IArr[slot] == wanted ? this.init_Vec3Arr[slot] : this.init_Vec3;
   }

   public void init_V() {
      this.init(null, 0);
   }

   public boolean init_Z_LivingEntity(LivingEntity target) {
      return target != null && this.init_Vec3 != null && target.getId() == this.load_I;
   }

   public Vec3 init_Vec3() {
      return this.init_Vec3;
   }

   public Vec3 init_Vec3_I(int tick) {
      int ahead = Math.min(3, tick - this.read + 1);
      return this.init_Vec3.add(this.load_Vec3.scale(ahead));
   }

   public double init(Vec3 eye, double centerYaw, int tick) {
      if (this.init_Vec3 == null) {
         return 0.0;
      }

      Vec3 server = this.init_Vec3_I(tick);
      double serverYaw = Math.toDegrees(Math.atan2(server.z - eye.z, server.x - eye.x)) - 90.0;
      return Mth.wrapDegrees(serverYaw - centerYaw);
   }

   public static Vec3 init_Vec3_LivingEntity(LivingEntity entity) {
      if (!AutismLiteVariant.enabled()) {
         Vec3 observed = autismclient.modules.BacktrackModule.init_Vec3_LivingEntity(entity);
         if (observed != null) {
            return observed;
         }
      }

      InterpolationHandler interpolation = entity.getInterpolation();
      return interpolation != null && interpolation.hasActiveInterpolation() ? Compat263.interpPos(interpolation) : entity.position();
   }
}
