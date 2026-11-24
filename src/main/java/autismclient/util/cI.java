package autismclient.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class cI {
   private static final ThreadLocal<Vector3f> init = ThreadLocal.withInitial(Vector3f::new);

   private cI() {
   }

   public static void init(Pose pose, VertexConsumer buffer, Vec3 start, Vec3 end, int color, float width) {
      if (start != null && end != null) {
         init(pose, buffer, start.x, start.y, start.z, end.x, end.y, end.z, color, width);
      }
   }

   public static void init(Pose pose, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, int color, float width) {
      init(pose, buffer, x1, y1, z1, x2, y2, z2, color, color, width);
   }

   public static void init(
      Pose pose, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, int color1, int color2, float width
   ) {
      Vector3f normal = init.get().set((float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1));
      if (!(normal.lengthSquared() <= 1.0E-8F)) {
         normal.normalize();
         pose.transformNormal(normal, normal);
         buffer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(color1).setNormal(normal.x, normal.y, normal.z).setLineWidth(width);
         buffer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(color2).setNormal(normal.x, normal.y, normal.z).setLineWidth(width);
      }
   }

   public static void init(Pose pose, VertexConsumer buffer, AABB b, int color, float width) {
      for (int i = 0; i < 2; i++) {
         for (int j = 0; j < 2; j++) {
            double x = i == 0 ? b.minX : b.maxX;
            double y = i == 0 ? b.minY : b.maxY;
            double y2 = j == 0 ? b.minY : b.maxY;
            double z = j == 0 ? b.minZ : b.maxZ;
            init(pose, buffer, b.minX, y, z, b.maxX, y, z, color, width);
            init(pose, buffer, x, b.minY, z, x, b.maxY, z, color, width);
            init(pose, buffer, x, y2, b.minZ, x, y2, b.maxZ, color, width);
         }
      }
   }

   public static void init(Pose pose, VertexConsumer buffer, AABB b, int color) {
      init(pose, buffer, color, b.minX, b.minY, b.minZ, b.maxX, b.minY, b.minZ, b.maxX, b.minY, b.maxZ, b.minX, b.minY, b.maxZ);
      init(pose, buffer, color, b.minX, b.maxY, b.maxZ, b.maxX, b.maxY, b.maxZ, b.maxX, b.maxY, b.minZ, b.minX, b.maxY, b.minZ);
      init(pose, buffer, color, b.minX, b.minY, b.maxZ, b.maxX, b.minY, b.maxZ, b.maxX, b.maxY, b.maxZ, b.minX, b.maxY, b.maxZ);
      init(pose, buffer, color, b.maxX, b.minY, b.minZ, b.minX, b.minY, b.minZ, b.minX, b.maxY, b.minZ, b.maxX, b.maxY, b.minZ);
      init(pose, buffer, color, b.minX, b.minY, b.minZ, b.minX, b.minY, b.maxZ, b.minX, b.maxY, b.maxZ, b.minX, b.maxY, b.minZ);
      init(pose, buffer, color, b.maxX, b.minY, b.maxZ, b.maxX, b.minY, b.minZ, b.maxX, b.maxY, b.minZ, b.maxX, b.maxY, b.maxZ);
   }

   private static void init(
      Pose pose,
      VertexConsumer buffer,
      int color,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      double x3,
      double y3,
      double z3,
      double x4,
      double y4,
      double z4
   ) {
      buffer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(color);
      buffer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(color);
      buffer.addVertex(pose, (float)x3, (float)y3, (float)z3).setColor(color);
      buffer.addVertex(pose, (float)x4, (float)y4, (float)z4).setColor(color);
   }
}
