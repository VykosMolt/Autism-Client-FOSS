package autismclient.ai.b;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class PacketUtil {
   private PacketUtil() {
   }

   static ArrayList<Long> init(BlockPos target) {
      double reach = 4.1000000000000005;
      int horizontal = (int)Math.floor(reach + 0.5);
      int down = (int)Math.ceil(reach + 1.62);
      int up = (int)Math.floor(1.0 + reach - 1.62);
      ArrayList<Long> cells = new ArrayList<>();

      for (int x = -horizontal; x <= horizontal; x++) {
         for (int z = -horizontal; z <= horizontal; z++) {
            for (int y = -down; y <= up; y++) {
               cells.add(target.offset(x, y, z).asLong());
            }
         }
      }

      return cells;
   }

   static List<Vec3> init(ArrayUtil view, ListPool target, ConfigStore.Inner_CachePool mode, long key) {
      Vec3 center = TextFormat.init(key);
      if (!TextFormat.load(view, center)) {
         return List.of(center);
      }

      ArrayList<Vec3> connected = new ArrayList<>();

      for (Vec3 point : load(view, target, mode, key)) {
         if (point.equals(center) || TextFormat.init_Z(view, center, point)) {
            connected.add(point);
         }
      }

      return List.copyOf(connected);
   }

   static List<Vec3> load(ArrayUtil view, ListPool target, ConfigStore.Inner_CachePool mode, long key) {
      Vec3 center = TextFormat.init(key);
      double low = BlockPos.getY(key);
      double high = low + 1.0;
      AABB column = new AABB(center.x - 0.3, low, center.z - 0.3, center.x + 0.3, high + 1.8, center.z + 0.3);
      ArrayList<AABB> boxes = new ArrayList<>(view.init_List_AABB(column));
      if (mode == ConfigStore.Inner_CachePool.init) {
         BlockState placed = target.wanted();
         if (placed.hasProperty(BlockStateProperties.OPEN)) {
            placed = placed.setValue(BlockStateProperties.OPEN, false);
         }

         for (AABB box : view.init_List_BlockState_BlockPos(placed, target.pos())) {
            boxes.add(box.move(target.pos()));
         }
      }

      TreeSet<Double> edges = new TreeSet<>();
      edges.add(low);
      edges.add(high);

      for (AABB box : boxes) {
         if (init(column, box)) {
            double below = box.minY - 1.8;
            double above = box.maxY;
            if (below > low && below < high) {
               edges.add(below);
            }

            if (above > low && above < high) {
               edges.add(above);
            }
         }
      }

      ArrayList<Vec3> alternatives = new ArrayList<>();
      double previous = low;

      for (double next : edges) {
         if (next - previous > 1.0E-6) {
            Vec3 point = new Vec3(center.x, (previous + next) * 0.5, center.z);
            AABB body = IoWorker.init(point.x, point.y, point.z, false);
            boolean free = true;

            for (AABB box : boxes) {
               if (body.intersects(box)) {
                  free = false;
                  break;
               }
            }

            if (free && Math.abs(point.y - center.y) > 1.0E-6 && TextFormat.load(view, point)) {
               alternatives.add(point);
            }
         }

         previous = next;
      }

      alternatives.sort(Comparator.comparingDouble(pointx -> Math.abs(pointx.y - center.y)));
      ArrayList<Vec3> points = new ArrayList<>(alternatives.size() + 1);
      points.add(center);
      points.addAll(alternatives);
      return List.copyOf(points);
   }

   private static boolean init(AABB a, AABB b) {
      return a.minX < b.maxX && a.maxX > b.minX && a.minZ < b.maxZ && a.maxZ > b.minZ;
   }
}
