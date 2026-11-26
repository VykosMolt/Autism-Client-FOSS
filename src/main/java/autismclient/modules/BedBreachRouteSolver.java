package autismclient.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class BedBreachRouteSolver {
   static BedBreachRouteSolver.Inner_BufferHelper init_DataPipe_BufferHel(List<BedBreachRouteSolver.Inner_DataUtil> cells, Vec3 eye, double reach, Vec3 preferred) {
      return init_DataPipe_CachePool(cells, eye, reach, preferred).init(null, 0.0F, preferred);
   }

   static BedBreachRouteSolver.Inner_CachePool init_DataPipe_CachePool(List<BedBreachRouteSolver.Inner_DataUtil> cells, Vec3 eye, double reach, Vec3 preferred) {
      LinkedHashMap<List<BedBreachRouteSolver.Inner_DataUtil>, BedBreachRouteSolver.Inner_BufferHelper> routes = new LinkedHashMap<>();
      if (cells.stream().noneMatch(c -> Double.isFinite(c.init_D) && c.init_List.stream().anyMatch(b -> b.distanceToSqr(eye) <= reach * reach))) {
         return new BedBreachRouteSolver.Inner_CachePool(List.of());
      }

      ArrayList<AABB> hard = new ArrayList<>();

      for (BedBreachRouteSolver.Inner_DataUtil cell : cells) {
         if (!cell.init_Z && !Double.isFinite(cell.init_D)) {
            hard.addAll(cell.init_List);
         }
      }

      for (BedBreachRouteSolver.Inner_DataUtil bed : cells) {
         if (bed.init_Z) {
            for (AABB box : bed.init_List) {
               ArrayList<AABB> blockers = new ArrayList<>();

               for (BedBreachRouteSolver.Inner_DataUtil cell : cells) {
                  if (cell != bed) {
                     blockers.addAll(cell.init_List);
                  }
               }

               LinkedHashSet<Vec3> points = new LinkedHashSet<>();

               for (Direction side : Direction.values()) {
                  points.addAll(FaceVisibilitySolver.init(eye, box, side, blockers, preferred));
                  FaceVisibilitySolver.Inner_StreamUtil face = FaceVisibilitySolver.init(eye, box, side, hard);
                  points.addAll(face.init(List.of(), preferred));

                  for (BedBreachRouteSolver.Inner_DataUtil cover : cells) {
                     if (!cover.init_Z && Double.isFinite(cover.init_D)) {
                        points.addAll(face.init(cover.init_List, preferred));
                     }
                  }
               }

               for (Vec3 point : points) {
                  Vec3 end = point.add(point.subtract(eye).normalize().scale(1.0E-5));
                  BedBreachRouteSolver.Inner_BufferHelper route = init(cells, eye, end, reach);
                  if (route != null) {
                     BedBreachRouteSolver.Inner_BufferHelper previous = routes.get(route.init_List);
                     if (previous == null
                        || route.init_Z && !previous.init_Z
                        || route.init_Z == previous.init_Z && point.distanceToSqr(preferred) < previous.init_Vec3.distanceToSqr(preferred)) {
                        routes.put(route.init_List, route);
                     }
                  }
               }
            }
         }
      }

      return new BedBreachRouteSolver.Inner_CachePool(List.copyOf(routes.values()));
   }

   static BedBreachRouteSolver.Inner_BufferHelper init(List<BedBreachRouteSolver.Inner_DataUtil> cells, Vec3 eye, Vec3 end, double reach) {
      ArrayList<BedBreachRouteSolver.Inner_StreamUtil> crossings = new ArrayList<>();

      for (BedBreachRouteSolver.Inner_DataUtil cell : cells) {
         double closest = 1.0 / 0.0;

         for (AABB box : cell.init_List) {
            Optional<Vec3> hit = box.clip(eye, end);
            if (box.contains(eye)) {
               closest = 0.0;
            }

            if (hit.isPresent()) {
               closest = Math.min(closest, eye.distanceToSqr(hit.get()));
            }
         }

         if (Double.isFinite(closest)) {
            crossings.add(new BedBreachRouteSolver.Inner_StreamUtil(cell, closest));
         }
      }

      crossings.sort(Comparator.comparingDouble(BedBreachRouteSolver.Inner_StreamUtil::init_D).thenComparing(c -> Double.isFinite(c.init_DataPipe_DataUtil.init_D)));
      ArrayList<BedBreachRouteSolver.Inner_DataUtil> blocks = new ArrayList<>();
      double ticks = 0.0;

      for (BedBreachRouteSolver.Inner_StreamUtil hit : crossings) {
         if (!Double.isFinite(hit.init_DataPipe_DataUtil.init_D) || blocks.isEmpty() && hit.init_D > reach * reach) {
            return null;
         }

         if (!blocks.isEmpty()) {
            ticks += 5.0;
         }

         blocks.add(hit.init_DataPipe_DataUtil);
         ticks += hit.init_DataPipe_DataUtil.init_D;
         if (hit.init_DataPipe_DataUtil.init_Z) {
            return new BedBreachRouteSolver.Inner_BufferHelper(List.copyOf(blocks), end, ticks, hit.init_D <= reach * reach);
         }
      }

      return null;
   }

   static boolean init(double remainingTicks, double combatBudget, boolean criticalHealth) {
      return !criticalHealth && Double.isFinite(remainingTicks) && remainingTicks <= Math.min(6.0, combatBudget);
   }

   record Inner_BufferHelper(List<BedBreachRouteSolver.Inner_DataUtil> init_List, Vec3 init_Vec3, double init_D, boolean init_Z) {
      Inner_BufferHelper(List<BedBreachRouteSolver.Inner_DataUtil> blocks, Vec3 aim, double ticks) {
         this(blocks, aim, ticks, true);
      }

      BedBreachRouteSolver.Inner_DataUtil init_DataPipe_DataUtil() {
         return this.init_List.getFirst();
      }
   }

   record Inner_CachePool(List<BedBreachRouteSolver.Inner_BufferHelper> init) {
      BedBreachRouteSolver.Inner_BufferHelper init(BlockPos mining, float progress, Vec3 preferred) {
         BedBreachRouteSolver.Inner_BufferHelper best = null;

         for (BedBreachRouteSolver.Inner_BufferHelper original : this.init) {
            double ticks = original.init_D;
            List<BedBreachRouteSolver.Inner_DataUtil> blocks = original.init_List;
            if (mining != null && progress > 0.0F) {
               for (int i = 0; i < blocks.size(); i++) {
                  if (blocks.get(i).init_BlockPos.equals(mining)) {
                     BedBreachRouteSolver.Inner_DataUtil cell = blocks.get(i);
                     double remaining = Math.max(1.0, Math.ceil(cell.init_D * (1.0F - Math.clamp(progress, 0.0F, 1.0F))));
                     ticks += remaining - cell.init_D;
                     ArrayList<BedBreachRouteSolver.Inner_DataUtil> changed = new ArrayList<>(blocks);
                     changed.set(i, new BedBreachRouteSolver.Inner_DataUtil(cell.init_BlockPos, cell.init_List, remaining, cell.init_I, cell.init_Z));
                     blocks = List.copyOf(changed);
                     break;
                  }
               }
            }

            BedBreachRouteSolver.Inner_BufferHelper route = blocks == original.init_List
               ? original
               : new BedBreachRouteSolver.Inner_BufferHelper(blocks, original.init_Vec3, ticks, original.init_Z);
            if (best == null
               || route.init_Z && !best.init_Z
               || route.init_Z == best.init_Z
                  && (
                     ticks < best.init_D - 1.0E-7
                        || Math.abs(ticks - best.init_D) < 1.0E-7 && route.init_Vec3.distanceToSqr(preferred) < best.init_Vec3.distanceToSqr(preferred)
                  )) {
               best = route;
            }
         }

         return best;
      }
   }

   record Inner_DataUtil(BlockPos init_BlockPos, List<AABB> init_List, double init_D, int init_I, boolean init_Z) {
      Inner_DataUtil {
         init_BlockPos = init_BlockPos.immutable();
         init_List = List.copyOf(init_List);
      }
   }

   private record Inner_StreamUtil(BedBreachRouteSolver.Inner_DataUtil init_DataPipe_DataUtil, double init_D) {
   }
}
