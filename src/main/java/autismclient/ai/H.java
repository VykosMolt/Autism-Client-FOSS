package autismclient.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class H {
   private H() {
   }

   static List<Vec3> init(List<BlockPos> cells, int band, BlockPos near) {
      if (cells.isEmpty()) {
         return List.of();
      }

      H.Inner_DataUtil x = init(cells, Axis.X, band, near);
      H.Inner_DataUtil z = init(cells, Axis.Z, band, near);
      H.Inner_DataUtil best = x.cost() <= z.cost() ? x : z;
      List<Vec3> out = new ArrayList<>();

      for (H.Inner_CachePool leg : best.legs()) {
         out.add(Vec3.atBottomCenterOf(leg.start()));
         out.add(Vec3.atBottomCenterOf(leg.end()));
      }

      return out;
   }

   private static H.Inner_DataUtil init(List<BlockPos> cells, Axis axis, int band, BlockPos near) {
      int min = axis == Axis.X ? cells.stream().mapToInt(Vec3i::getZ).min().orElseThrow() : cells.stream().mapToInt(Vec3i::getX).min().orElseThrow();
      Map<H.Inner_BufferHelper, List<BlockPos>> rows = new HashMap<>();

      for (BlockPos cell : cells) {
         int perp = axis == Axis.X ? cell.getZ() : cell.getX();
         rows.computeIfAbsent(new H.Inner_BufferHelper(cell.getY(), Math.floorDiv(perp - min, band)), ignored -> new ArrayList<>()).add(cell);
      }

      List<H.Inner_CachePool> remaining = new ArrayList<>();

      for (List<BlockPos> row : rows.values()) {
         int minPerp = row.stream().mapToInt(p -> axis == Axis.X ? p.getZ() : p.getX()).min().orElseThrow();
         int maxPerp = row.stream().mapToInt(p -> axis == Axis.X ? p.getZ() : p.getX()).max().orElseThrow();
         int line = (minPerp + Math.min(maxPerp, minPerp + band - 1)) / 2;
         Set<Integer> along = new HashSet<>();

         for (BlockPos p : row) {
            along.add(axis == Axis.X ? p.getX() : p.getZ());
         }

         List<Integer> ordered = new ArrayList<>(along);
         ordered.sort(null);
         int runStart = 0;

         for (int i = 1; i <= ordered.size(); i++) {
            if (i >= ordered.size() || ordered.get(i) - ordered.get(i - 1) > 2) {
               int first = ordered.get(runStart);
               int last = ordered.get(i - 1);
               int y = row.getFirst().getY();
               BlockPos start = axis == Axis.X ? new BlockPos(first, y + 1, line) : new BlockPos(line, y + 1, first);
               BlockPos end = axis == Axis.X ? new BlockPos(last, y + 1, line) : new BlockPos(line, y + 1, last);
               remaining.add(new H.Inner_CachePool(start, end, last - first));
               runStart = i;
            }
         }
      }

      List<H.Inner_CachePool> orderedLegs = new ArrayList<>();
      Vec3 cursor = Vec3.atCenterOf(near);
      double travel = 0.0;

      while (!remaining.isEmpty()) {
         int best = -1;
         double bestDistance = 1.7976931348623157E308;
         boolean reverse = false;

         for (int i = 0; i < remaining.size(); i++) {
            H.Inner_CachePool leg = remaining.get(i);
            double start = cursor.distanceToSqr(Vec3.atCenterOf(leg.start()));
            double end = cursor.distanceToSqr(Vec3.atCenterOf(leg.end()));
            if (start < bestDistance) {
               bestDistance = start;
               best = i;
               reverse = false;
            }

            if (end < bestDistance) {
               bestDistance = end;
               best = i;
               reverse = true;
            }
         }

         H.Inner_CachePool next = remaining.remove(best);
         if (reverse) {
            next = next.reverse();
         }

         travel += Math.sqrt(bestDistance) + next.distance();
         orderedLegs.add(next);
         cursor = Vec3.atCenterOf(next.end());
      }

      return new H.Inner_DataUtil(List.copyOf(orderedLegs), travel + orderedLegs.size() * 2.0);
   }

   static boolean init(boolean rows, int waypoint, boolean onRay) {
      return !rows || waypoint % 2 == 1 && onRay;
   }

   static boolean init(List<BlockPos> cells) {
      if (cells.size() < 8) {
         return false;
      }

      int minX = 2147483647;
      int minZ = 2147483647;
      int maxX = -2147483648;
      int maxZ = -2147483648;
      int y = cells.getFirst().getY();

      for (BlockPos p : cells) {
         if (p.getY() != y) {
            return false;
         }

         minX = Math.min(minX, p.getX());
         maxX = Math.max(maxX, p.getX());
         minZ = Math.min(minZ, p.getZ());
         maxZ = Math.max(maxZ, p.getZ());
      }

      return cells.size() >= (long)(maxX - minX + 1) * (maxZ - minZ + 1) * 0.7;
   }

   static boolean init(H.Inner_StreamUtil previous, H.Inner_StreamUtil next, BlockHitResult ray) {
      return next == null ? false : ray == null || !ray.getBlockPos().equals(next.cell()) || ray.getDirection() != next.face();
   }

   private record Inner_BufferHelper(int y, int band) {
   }

   private record Inner_CachePool(BlockPos start, BlockPos end, double distance) {
      H.Inner_CachePool reverse() {
         return new H.Inner_CachePool(this.end, this.start, this.distance);
      }
   }

   private record Inner_DataUtil(List<H.Inner_CachePool> legs, double cost) {
   }

   record Inner_StreamUtil(BlockPos cell, Direction face) {
      static H.Inner_StreamUtil of(BlockPos cell, Direction face) {
         return new H.Inner_StreamUtil(cell.immutable(), face);
      }
   }
}
