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
   static BedBreachRouteSolver.BreachRoute init_DataPipe_BufferHel(List<BedBreachRouteSolver.BreachCell> cells, Vec3 eye, double reach, Vec3 preferred) {
      return solve(cells, eye, reach, preferred).init(null, 0.0F, preferred);
   }

   static BedBreachRouteSolver.RouteSet solve(List<BedBreachRouteSolver.BreachCell> cells, Vec3 eye, double reach, Vec3 preferred) {
      LinkedHashMap<List<BedBreachRouteSolver.BreachCell>, BedBreachRouteSolver.BreachRoute> routes = new LinkedHashMap<>();
      if (cells.stream().noneMatch(c -> Double.isFinite(c.ticks) && c.boxes.stream().anyMatch(b -> b.distanceToSqr(eye) <= reach * reach))) {
         return new BedBreachRouteSolver.RouteSet(List.of());
      }

      ArrayList<AABB> hard = new ArrayList<>();

      for (BedBreachRouteSolver.BreachCell cell : cells) {
         if (!cell.init_Z && !Double.isFinite(cell.ticks)) {
            hard.addAll(cell.boxes);
         }
      }

      for (BedBreachRouteSolver.BreachCell bed : cells) {
         if (bed.init_Z) {
            for (AABB box : bed.boxes) {
               ArrayList<AABB> blockers = new ArrayList<>();

               for (BedBreachRouteSolver.BreachCell cell : cells) {
                  if (cell != bed) {
                     blockers.addAll(cell.boxes);
                  }
               }

               LinkedHashSet<Vec3> points = new LinkedHashSet<>();

               for (Direction side : Direction.values()) {
                  points.addAll(FaceVisibilitySolver.init(eye, box, side, blockers, preferred));
                  FaceVisibilitySolver.FaceProjection face = FaceVisibilitySolver.init(eye, box, side, hard);
                  points.addAll(face.init(List.of(), preferred));

                  for (BedBreachRouteSolver.BreachCell cover : cells) {
                     if (!cover.init_Z && Double.isFinite(cover.ticks)) {
                        points.addAll(face.init(cover.boxes, preferred));
                     }
                  }
               }

               for (Vec3 point : points) {
                  Vec3 end = point.add(point.subtract(eye).normalize().scale(1.0E-5));
                  BedBreachRouteSolver.BreachRoute route = init(cells, eye, end, reach);
                  if (route != null) {
                     BedBreachRouteSolver.BreachRoute previous = routes.get(route.cells);
                     if (previous == null
                        || route.init_Z && !previous.init_Z
                        || route.init_Z == previous.init_Z && point.distanceToSqr(preferred) < previous.eye.distanceToSqr(preferred)) {
                        routes.put(route.cells, route);
                     }
                  }
               }
            }
         }
      }

      return new BedBreachRouteSolver.RouteSet(List.copyOf(routes.values()));
   }

   static BedBreachRouteSolver.BreachRoute init(List<BedBreachRouteSolver.BreachCell> cells, Vec3 eye, Vec3 end, double reach) {
      ArrayList<BedBreachRouteSolver.RayCrossing> crossings = new ArrayList<>();

      for (BedBreachRouteSolver.BreachCell cell : cells) {
         double closest = 1.0 / 0.0;

         for (AABB box : cell.boxes) {
            Optional<Vec3> hit = box.clip(eye, end);
            if (box.contains(eye)) {
               closest = 0.0;
            }

            if (hit.isPresent()) {
               closest = Math.min(closest, eye.distanceToSqr(hit.get()));
            }
         }

         if (Double.isFinite(closest)) {
            crossings.add(new BedBreachRouteSolver.RayCrossing(cell, closest));
         }
      }

      crossings.sort(Comparator.comparingDouble(BedBreachRouteSolver.RayCrossing::closest).thenComparing(c -> Double.isFinite(c.init_DataPipe_DataUtil.ticks)));
      ArrayList<BedBreachRouteSolver.BreachCell> blocks = new ArrayList<>();
      double ticks = 0.0;

      for (BedBreachRouteSolver.RayCrossing hit : crossings) {
         if (!Double.isFinite(hit.init_DataPipe_DataUtil.ticks) || blocks.isEmpty() && hit.closest > reach * reach) {
            return null;
         }

         if (!blocks.isEmpty()) {
            ticks += 5.0;
         }

         blocks.add(hit.init_DataPipe_DataUtil);
         ticks += hit.init_DataPipe_DataUtil.ticks;
         if (hit.init_DataPipe_DataUtil.init_Z) {
            return new BedBreachRouteSolver.BreachRoute(List.copyOf(blocks), end, ticks, hit.closest <= reach * reach);
         }
      }

      return null;
   }

   static boolean init(double remainingTicks, double combatBudget, boolean criticalHealth) {
      return !criticalHealth && Double.isFinite(remainingTicks) && remainingTicks <= Math.min(6.0, combatBudget);
   }

   record BreachRoute(List<BedBreachRouteSolver.BreachCell> cells, Vec3 eye, double ticks, boolean init_Z) {
      BreachRoute(List<BedBreachRouteSolver.BreachCell> blocks, Vec3 aim, double ticks) {
         this(blocks, aim, ticks, true);
      }

      BedBreachRouteSolver.BreachCell first() {
         return this.cells.getFirst();
      }
   }

   record RouteSet(List<BedBreachRouteSolver.BreachRoute> init) {
      BedBreachRouteSolver.BreachRoute init(BlockPos mining, float progress, Vec3 preferred) {
         BedBreachRouteSolver.BreachRoute best = null;

         for (BedBreachRouteSolver.BreachRoute original : this.init) {
            double ticks = original.ticks;
            List<BedBreachRouteSolver.BreachCell> blocks = original.cells;
            if (mining != null && progress > 0.0F) {
               for (int i = 0; i < blocks.size(); i++) {
                  if (blocks.get(i).pos.equals(mining)) {
                     BedBreachRouteSolver.BreachCell cell = blocks.get(i);
                     double remaining = Math.max(1.0, Math.ceil(cell.ticks * (1.0F - Math.clamp(progress, 0.0F, 1.0F))));
                     ticks += remaining - cell.ticks;
                     ArrayList<BedBreachRouteSolver.BreachCell> changed = new ArrayList<>(blocks);
                     changed.set(i, new BedBreachRouteSolver.BreachCell(cell.pos, cell.boxes, remaining, cell.slot, cell.init_Z));
                     blocks = List.copyOf(changed);
                     break;
                  }
               }
            }

            BedBreachRouteSolver.BreachRoute route = blocks == original.cells
               ? original
               : new BedBreachRouteSolver.BreachRoute(blocks, original.eye, ticks, original.init_Z);
            if (best == null
               || route.init_Z && !best.init_Z
               || route.init_Z == best.init_Z
                  && (
                     ticks < best.ticks - 1.0E-7
                        || Math.abs(ticks - best.ticks) < 1.0E-7 && route.eye.distanceToSqr(preferred) < best.eye.distanceToSqr(preferred)
                  )) {
               best = route;
            }
         }

         return best;
      }
   }

   record BreachCell(BlockPos pos, List<AABB> boxes, double ticks, int slot, boolean init_Z) {
      BreachCell {
         pos = pos.immutable();
         boxes = List.copyOf(boxes);
      }
   }

   private record RayCrossing(BedBreachRouteSolver.BreachCell init_DataPipe_DataUtil, double closest) {
   }
}
