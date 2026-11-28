package autismclient.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BlastShieldPosFinder {
   public static final int init_I = 24;
   public static final double init_D = 2.0;

   private BlastShieldPosFinder() {
   }

   public static List<BlockPos> candidates(AABB body, AABB nextBody, Vec3 source) {
      Vec3 middle = body.getCenter();
      Vec3 toward = source.subtract(middle);
      if (isFinite(source) && isFinite(middle) && !(toward.lengthSqr() < 1.0E-8)) {
         LinkedHashSet<BlockPos> found = new LinkedHashSet<>();

         for (double y : new double[]{body.minY + 0.25, middle.y, body.maxY - 0.15}) {
            Vec3 from = new Vec3(middle.x, y, middle.z);
            Vec3 direction = source.subtract(from).normalize();

            for (double step = 0.65; step <= Math.min(3.0, source.distanceTo(from)); step += 0.4) {
               found.add(BlockPos.containing(from.add(direction.scale(step))));
            }
         }

         int minX = Mth.floor(body.minX) - 1;
         int maxX = Mth.floor(body.maxX) + 1;
         int minZ = Mth.floor(body.minZ) - 1;
         int maxZ = Mth.floor(body.maxZ) + 1;

         for (int y = Mth.floor(body.minY) - 1; y <= Mth.floor(body.maxY); y++) {
            for (int x = minX; x <= maxX; x++) {
               for (int z = minZ; z <= maxZ; z++) {
                  BlockPos cell = new BlockPos(x, y, z);
                  Vec3 offset = Vec3.atCenterOf(cell).subtract(middle);
                  if (toward.horizontalDistanceSqr() < 0.0625 || offset.x * toward.x + offset.z * toward.z > 0.0) {
                     found.add(cell);
                  }
               }
            }
         }

         ArrayList<BlockPos> cells = new ArrayList<>();

         for (BlockPos cell : found) {
            AABB block = new AABB(cell);
            if (!block.intersects(body) && !block.intersects(nextBody) && !block.contains(source)) {
               cells.add(cell);
            }
         }

         cells.sort(Comparator.comparingDouble(cellx -> {
            Vec3 offsetx = Vec3.atCenterOf(cellx).subtract(middle);
            double along = Math.max(0.0, Math.min(1.0, offsetx.dot(toward) / toward.lengthSqr()));
            return offsetx.subtract(toward.scale(along)).lengthSqr() + 0.02 * offsetx.lengthSqr();
         }));
         return List.copyOf(cells.subList(0, Math.min(24, cells.size())));
      } else {
         return List.of();
      }
   }

   public static boolean init(
      double beforeTarget, double beforeSelf, double afterTarget, double afterSelf, double minimumTarget, double maximumSelf, boolean efficient
   ) {
      if (WeaponDamageCalculator.init(afterTarget, afterSelf, minimumTarget, maximumSelf, efficient)
         && Double.isFinite(beforeTarget)
         && Double.isFinite(beforeSelf)
         && !(beforeTarget < 0.0)
         && !(beforeSelf - afterSelf < 2.0)) {
         return !WeaponDamageCalculator.init(beforeTarget, beforeSelf, minimumTarget, maximumSelf, efficient)
            ? true
            : afterTarget >= beforeTarget * 0.9 && (afterTarget - afterSelf) * 3.0 >= (beforeTarget - beforeSelf) * 4.0;
      } else {
         return false;
      }
   }

   private static boolean isFinite(Vec3 p) {
      return Double.isFinite(p.x) && Double.isFinite(p.y) && Double.isFinite(p.z);
   }
}
