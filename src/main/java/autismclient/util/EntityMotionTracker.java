package autismclient.util;

import autismclient.compat263.Compat263;
import java.util.Arrays;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class EntityMotionTracker {
   public static final int MAX_LOOKAHEAD = 3;
   private int trackedId = -2147483648;
   private Vec3 lastPosition;
   private int lastSampleTick;
   private Vec3 velocity = Vec3.ZERO;
   private final Vec3[] positions = new Vec3[32];
   private final int[] ticks = new int[32];

   public void track(LivingEntity target, int tick) {
      if (target == null) {
         this.trackedId = -2147483648;
         this.lastPosition = null;
         this.velocity = Vec3.ZERO;
         Arrays.fill(this.positions, null);
      } else {
         this.sample(target.getId(), observedPosition(target), tick);
      }
   }

   void sample(int targetId, Vec3 now, int tick) {
      if (targetId != this.trackedId || this.lastPosition == null) {
         this.trackedId = targetId;
         Arrays.fill(this.positions, null);
         this.velocity = Vec3.ZERO;
         this.lastPosition = now;
         this.lastSampleTick = tick;
      } else if (!now.equals(this.lastPosition)) {
         this.velocity = now.subtract(this.lastPosition).scale(1.0 / Math.max(1, Math.min(4, tick - this.lastSampleTick)));
         this.lastPosition = now;
         this.lastSampleTick = tick;
      }

      int slot = tick & this.positions.length - 1;
      this.positions[slot] = now;
      this.ticks[slot] = tick;
   }

   public Vec3 positionAt(int tick, int age) {
      int wanted = tick - Math.max(0, Math.min(this.positions.length - 1, age));
      int slot = wanted & this.positions.length - 1;
      return this.positions[slot] != null && this.ticks[slot] == wanted ? this.positions[slot] : this.lastPosition;
   }

   public void reset() {
      this.track(null, 0);
   }

   public boolean isTracking(LivingEntity target) {
      return target != null && this.lastPosition != null && target.getId() == this.trackedId;
   }

   public Vec3 trackedPosition() {
      return this.lastPosition;
   }

   public Vec3 predictedAt(int tick) {
      int ahead = Math.min(3, tick - this.lastSampleTick + 1);
      return this.lastPosition.add(this.velocity.scale(ahead));
   }

   public double init(Vec3 eye, double centerYaw, int tick) {
      if (this.lastPosition == null) {
         return 0.0;
      }

      Vec3 server = this.predictedAt(tick);
      double serverYaw = Math.toDegrees(Math.atan2(server.z - eye.z, server.x - eye.x)) - 90.0;
      return Mth.wrapDegrees(serverYaw - centerYaw);
   }

   public static Vec3 observedPosition(LivingEntity entity) {
      if (!AutismLiteVariant.enabled()) {
         Vec3 observed = autismclient.modules.BacktrackModule.backtrackedPosition(entity);
         if (observed != null) {
            return observed;
         }
      }

      InterpolationHandler interpolation = entity.getInterpolation();
      return interpolation != null && interpolation.hasActiveInterpolation() ? Compat263.interpPos(interpolation) : entity.position();
   }
}
