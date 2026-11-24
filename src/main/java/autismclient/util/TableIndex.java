package autismclient.util;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class TableIndex {
   private final cj init = new cj();

   public void init(LivingEntity target, int tick) {
      this.init.init(target, tick);
   }

   public AABB init_AABB_LivingEntity(LivingEntity target, int tick, int horizonTicks, double maxLead) {
      Vec3 base = this.init.init_Z_LivingEntity(target) ? this.init.init_Vec3() : cj.init_Vec3_LivingEntity(target);
      AABB box = target.getBoundingBox().move(base.subtract(target.position()));
      if (horizonTicks <= 0) {
         return box;
      }

      Vec3 velocity = new Vec3(target.getX() - target.xo, 0.0, target.getZ() - target.zo);
      Vec3 wanted = init_Vec3_Vec3(velocity, horizonTicks, maxLead, 1.0);
      if (wanted.lengthSqr() < 1.0E-8) {
         return box;
      }

      Vec3 clipped = Entity.collideBoundingBox(target, wanted, box, target.level(), List.of());
      return box.move(clipped);
   }

   public void init() {
      this.init.init_V();
   }

   public static AABB init(LivingEntity target) {
      return target.getBoundingBox().move(cj.init_Vec3_LivingEntity(target).subtract(target.position()));
   }

   public static S.Inner_BufferHelper init(LivingEntity target, Vec3 source, float power, S.Inner_CachePool options, AABB targetBox) {
      double targetDamage = S.read(target, source, power, options.init(targetBox));
      double selfDamage = target == Minecraft.getInstance().player ? targetDamage : S.read(source, power, options);
      return new S.Inner_BufferHelper(targetDamage, selfDamage);
   }

   static Vec3 init_Vec3_Vec3(Vec3 perTick, int horizonTicks, double maxLead, double wallFraction) {
      int horizon = Math.max(0, Math.min(20, horizonTicks));
      Vec3 offset = perTick.scale(horizon);
      double length = Math.hypot(offset.x, offset.z);
      if (length < 1.0E-6) {
         return Vec3.ZERO;
      }

      double cap = Math.max(0.0, maxLead);
      double scale = length > cap ? cap / length : 1.0;
      return offset.scale(scale * Math.max(0.0, Math.min(1.0, wallFraction)));
   }
}
