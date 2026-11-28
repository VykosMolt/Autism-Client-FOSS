package autismclient.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class BlockPlacementOrderSolver {
   private BlockPlacementOrderSolver() {
   }

   static List<BlockPos> init(Vec3 eye, List<BlockPos> preferred, Map<BlockPos, List<Vec3>> rays) {
      return order(eye, preferred, rays, Map.of());
   }

   static List<BlockPos> order(Vec3 eye, List<BlockPos> preferred, Map<BlockPos, List<Vec3>> rays, Map<BlockPos, Set<BlockPos>> prerequisites) {
      int count = Math.min(64, preferred.size());
      boolean[][] before = new boolean[count][count];
      int[] dependencies = new int[count];

      for (int blocker = 0; blocker < count; blocker++) {
         for (int victim = 0; victim < count; victim++) {
            if (victim != blocker) {
               List<Vec3> points = rays.get(preferred.get(victim));
               if (init(eye, preferred.get(blocker), points) || prerequisites.getOrDefault(preferred.get(blocker), Set.of()).contains(preferred.get(victim))) {
                  before[victim][blocker] = true;
                  dependencies[blocker]++;
               }
            }
         }
      }

      ArrayList<BlockPos> out = new ArrayList<>(preferred.size());
      boolean[] used = new boolean[count];

      for (int step = 0; step < count; step++) {
         int best = -1;

         for (int i = 0; i < count; i++) {
            if (!used[i] && (best < 0 || dependencies[i] < dependencies[best])) {
               best = i;
            }

            if (!used[i] && dependencies[i] == 0) {
               best = i;
               break;
            }
         }

         used[best] = true;
         out.add(preferred.get(best));

         for (int i = 0; i < count; i++) {
            if (before[best][i]) {
               dependencies[i]--;
            }
         }
      }

      out.addAll(preferred.subList(count, preferred.size()));
      return out;
   }

   static boolean init(Vec3 eye, BlockPos blocker, List<Vec3> points) {
      if (points != null && !points.isEmpty()) {
         AABB cube = new AABB(blocker).deflate(0.002);

         for (Vec3 point : points) {
            if (cube.clip(eye, point).isEmpty() && !cube.contains(eye)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
