package autismclient.util.macro;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class A {
   public static final double init = 3.0;
   public static final double load = 0.0625;

   private A() {
   }

   public static List<A.Inner_DataUtil> init(Vec3 from, Vec3 to, boolean finalOnGround) {
      if (from != null && to != null) {
         if (to.y - from.y >= -3.0) {
            return List.of(new A.Inner_DataUtil(to, finalOnGround));
         }

         Vec3 reset = new Vec3(to.x, to.y + 0.0625, to.z);
         return List.of(new A.Inner_DataUtil(to, false), new A.Inner_DataUtil(reset, false), new A.Inner_DataUtil(to, finalOnGround));
      } else {
         return List.of();
      }
   }

   public static Vec3 init_Vec3(Entity entity, Vec3 from, Vec3 to) {
      if (entity != null && entity.level() != null && from != null && to != null) {
         AABB moved = entity.getBoundingBox().move(from.subtract(entity.position()));
         Vec3 delta = to.subtract(from);
         if (delta.lengthSqr() <= 1.0E-10) {
            return delta;
         }

         List<VoxelShape> shapes = new ArrayList<>();

         for (VoxelShape shape : entity.level().getBlockCollisions(entity, moved.expandTowards(delta))) {
            shapes.add(shape);
         }

         return Entity.collideBoundingBox(entity, delta, moved, entity.level(), shapes);
      } else {
         return Vec3.ZERO;
      }
   }

   public static boolean init_Z(Entity entity, Vec3 from, Vec3 to) {
      if (entity != null && from != null && to != null) {
         Vec3 delta = to.subtract(from);
         return delta.lengthSqr() <= 1.0E-10 ? true : init_Vec3(entity, from, to).equals(delta);
      } else {
         return false;
      }
   }

   public record Inner_DataUtil(Vec3 init_Vec3, boolean init_Z) {
      public Inner_DataUtil {
         if (init_Vec3 == null) {
            init_Vec3 = Vec3.ZERO;
         }
      }
   }
}
