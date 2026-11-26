package autismclient.util.macro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class TpRoutePlanner {
   private static final double init_D = 0.25;
   private static final double load_D = 0.5;
   private static final double read_D = 0.5;
   private static final double write_D = 0.25;
   private static final double update = 0.5;
   private static final int init_I = 3;
   private static final int load_I = 1800;
   private static final int read_I = 1500;
   private static final int write_I = 128;
   private static final double sync = 128.0;
   private static final double flush = 64.0;
   private static final double close = 1.5;
   private static final double open = 1.0;
   private static final double poll = 6.0;

   private TpRoutePlanner() {
   }

   public static TpRoutePlanner.Inner_StreamUtil init_F_StreamUtil_Entity(final Entity entity) {
      if (entity != null && entity.level() != null) {
         final Vec3 origin = entity.position();
         final AABB bounds = entity.getBoundingBox();
         final double height = bounds.getYsize();
         return new TpRoutePlanner.Inner_StreamUtil() {
            private final Map<Long, Boolean> init = new HashMap<>();

            @Override
            public Entity init_Entity() {
               return entity;
            }

            private boolean init(BlockPos position) {
               long key = position.asLong();
               Boolean cached = this.init.get(key);
               if (cached != null) {
                  return cached;
               }

               boolean lava = entity.level().getFluidState(position).is(FluidTags.LAVA);
               this.init.put(key, lava);
               return lava;
            }

            @Override
            public boolean init_Z(Vec3 position) {
               return position != null && entity.level().isLoaded(BlockPos.containing(position));
            }

            @Override
            public boolean load(Vec3 position) {
               if (position != null && !(position.y < this.init_D()) && !(position.y > this.load())) {
                  AABB moved = bounds.move(position.subtract(origin));
                  return entity.level().noCollision(entity, moved);
               } else {
                  return false;
               }
            }

            @Override
            public boolean read(Vec3 position) {
               if (!this.load(position)) {
                  return false;
               }

               AABB moved = bounds.move(position.subtract(origin));
               return !entity.level().noCollision(entity, moved.move(0.0, -0.0625, 0.0));
            }

            @Override
            public boolean write(Vec3 position) {
               if (!this.load(position)) {
                  return false;
               }

               double feetY = position.y;
               double eyeY = feetY + entity.getEyeHeight();
               double surface = TpRoutePlanner.init_D_Entity(entity, position.x, position.z, feetY, eyeY);
               if (!Double.isFinite(surface)) {
                  return false;
               }

               double depth = surface - feetY;
               boolean eyeSubmerged = TpRoutePlanner.init_Z_Entity(entity, position.x, eyeY - 0.05, position.z);
               return TpRoutePlanner.init_Z_D(bounds.getYsize(), entity.getEyeHeight(), depth, eyeSubmerged)
                  && TpRoutePlanner.init_Z_Entity(entity, position.x, feetY + 0.1, position.z)
                  && TpRoutePlanner.init_Z_Entity(entity, position.x, feetY + Math.min(0.8, bounds.getYsize() * 0.48), position.z);
            }

            @Override
            public boolean update(Vec3 position) {
               return position != null && TpRoutePlanner.init(entity, bounds.move(position.subtract(origin)), this::init);
            }

            @Override
            public double init_D(Vec3 position) {
               return position == null ? 0.0 : TpRoutePlanner.init_D_Entity(entity, bounds.move(position.subtract(origin)), 6.0, this::init);
            }

            @Override
            public double init_D() {
               return entity.level().getMinY();
            }

            @Override
            public double load() {
               return entity.level().getMaxY() + 1.0 - height;
            }
         };
      } else {
         return null;
      }
   }

   static boolean init_Z_D(double entityHeight, double eyeHeight, double waterDepth, boolean eyeSubmerged) {
      if (!eyeSubmerged && Double.isFinite(waterDepth)) {
         double minDepth = Math.min(0.85, Math.max(0.45, entityHeight * 0.45));
         double maxDepth = Math.max(minDepth + 0.1, Math.min(Math.max(0.0, eyeHeight - 0.3), entityHeight * 0.68));
         return waterDepth >= minDepth && waterDepth <= maxDepth;
      } else {
         return false;
      }
   }

   private static double init_D_Entity(Entity entity, double x, double z, double feetY, double eyeY) {
      if (entity != null && entity.level() != null) {
         double highest = 0.0 / 0.0;
         int min = (int)Math.floor(feetY);
         int max = (int)Math.floor(eyeY);

         for (int y = min; y <= max; y++) {
            BlockPos block = BlockPos.containing(x, y, z);
            FluidState fluid = entity.level().getFluidState(block);
            if (fluid.is(FluidTags.WATER)) {
               double surface = block.getY() + fluid.getHeight(entity.level(), block);
               if (!Double.isFinite(highest) || surface > highest) {
                  highest = surface;
               }
            }
         }

         return highest;
      } else {
         return 0.0 / 0.0;
      }
   }

   private static boolean init_Z_Entity(Entity entity, double x, double y, double z) {
      if (entity != null && entity.level() != null) {
         BlockPos block = BlockPos.containing(x, y, z);
         FluidState fluid = entity.level().getFluidState(block);
         return fluid.is(FluidTags.WATER) && y < block.getY() + fluid.getHeight(entity.level(), block) - 1.0E-4;
      } else {
         return false;
      }
   }

   private static boolean init(Entity entity, AABB body, Predicate<BlockPos> lavaAt) {
      if (entity != null && entity.level() != null && body != null) {
         AABB shell = new AABB(body.minX - 1.5, body.minY - 1.0, body.minZ - 1.5, body.maxX + 1.5, body.maxY + 1.0, body.maxZ + 1.5);
         MutableBlockPos cursor = new MutableBlockPos();
         int minY = Math.max(init_I_D(shell.minY), entity.level().getMinY());
         int maxY = Math.min(load(shell.maxY), entity.level().getMaxY() + 1);

         for (int x = init_I_D(shell.minX); x < load(shell.maxX); x++) {
            for (int z = init_I_D(shell.minZ); z < load(shell.maxZ); z++) {
               for (int y = minY; y < maxY; y++) {
                  cursor.set(x, y, z);
                  if (!entity.level().isLoaded(cursor)) {
                     return false;
                  }

                  if (lavaAt.test(cursor)) {
                     return false;
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static double init_D_Entity(Entity entity, AABB body, double scanRadius, Predicate<BlockPos> lavaAt) {
      if (entity != null && entity.level() != null && body != null) {
         double radius = Math.max(0.0, scanRadius);
         AABB scan = new AABB(body.minX - radius, body.minY - radius, body.minZ - radius, body.maxX + radius, body.maxY + radius, body.maxZ + radius);
         double bestSqr = 1.0 / 0.0;
         MutableBlockPos cursor = new MutableBlockPos();
         int minY = Math.max(init_I_D(scan.minY), entity.level().getMinY());
         int maxY = Math.min(load(scan.maxY), entity.level().getMaxY() + 1);

         for (int x = init_I_D(scan.minX); x < load(scan.maxX); x++) {
            for (int z = init_I_D(scan.minZ); z < load(scan.maxZ); z++) {
               for (int y = minY; y < maxY; y++) {
                  cursor.set(x, y, z);
                  if (!entity.level().isLoaded(cursor)) {
                     return 0.0;
                  }

                  if (lavaAt.test(cursor)) {
                     double dx = init_D_D(body.minX, body.maxX, x, x + 1.0);
                     double dy = init_D_D(body.minY, body.maxY, y, y + 1.0);
                     double dz = init_D_D(body.minZ, body.maxZ, z, z + 1.0);
                     bestSqr = Math.min(bestSqr, dx * dx + dy * dy + dz * dz);
                  }
               }
            }
         }

         return Double.isFinite(bestSqr) ? Math.sqrt(bestSqr) : 1.0 / 0.0;
      } else {
         return 0.0;
      }
   }

   private static double init_D_D(double firstMin, double firstMax, double secondMin, double secondMax) {
      if (firstMax < secondMin) {
         return secondMin - firstMax;
      } else {
         return secondMax < firstMin ? firstMin - secondMax : 0.0;
      }
   }

   private static int init_I_D(double value) {
      return (int)Math.floor(value);
   }

   private static int load(double value) {
      return (int)Math.ceil(value);
   }

   public static TpRoutePlanner.Inner_BufferHelper init_F_BufferHelper_F_StreamUtil(
      TpRoutePlanner.Inner_StreamUtil view, Vec3 start, Vec3 target, int searchRadius, int verticalRange, int maxWaypoints, boolean allowPartial
   ) {
      if (view != null && start != null && target != null) {
         int radius = Math.max(1, Math.min(128, searchRadius));
         int escapeRange = Math.max(0, Math.min(128, verticalRange));
         int routeCap = Math.max(1, Math.min(512, maxWaypoints));
         Vec3 horizontalTarget = new Vec3(target.x, start.y, target.z);
         if (init(view, horizontalTarget) && load(view, start, horizontalTarget)) {
            return init(init(start, List.of(horizontalTarget)), "direct");
         }

         TpRoutePlanner.Inner_BufferHelper layered = init_F_BufferHelper_F_StreamUtil_2(view, start, horizontalTarget, escapeRange, routeCap);
         if (layered.init_Z()) {
            return layered;
         }

         List<Vec3> route = new ArrayList<>();
         Vec3 current = start;
         boolean unloadedFrontier = false;
         int guard = 0;

         label104:
         while (init_D(current, horizontalTarget) > 0.5 && guard++ < routeCap) {
            Vec3 advance = init_Vec3_F_StreamUtil(view, current, horizontalTarget, radius);
            if (init_D(advance, current) > 0.5) {
               route.add(advance);
               current = advance;
            } else {
               Vec3 next = init_Vec3(current, horizontalTarget, 0.5);
               if (!view.init_Z(next)) {
                  unloadedFrontier = true;
                  break;
               }

               boolean escaped = false;
               Iterator var19 = init_List_I(escapeRange).iterator();

               while (true) {
                  if (var19.hasNext()) {
                     int dy = (Integer)var19.next();
                     if (dy == 0) {
                        continue;
                     }

                     Vec3 layer = new Vec3(current.x, init(view, current.y + dy), current.z);
                     if (!init(view, layer) || !init(view, current, layer)) {
                        continue;
                     }

                     Vec3 layerAdvance = init_Vec3_F_StreamUtil(view, layer, horizontalTarget, radius);
                     if (init_D(layerAdvance, layer) <= 1.0) {
                        continue;
                     }

                     route.add(layer);
                     route.add(layerAdvance);
                     current = layerAdvance;
                     escaped = true;
                  }

                  if (!escaped) {
                     break label104;
                  }
                  break;
               }
            }
         }

         boolean reached = init_D(current, horizontalTarget) <= 0.5;
         if (reached) {
            Vec3 landing = init_Vec3_F_StreamUtil(view, current.x, current.z, start.y, 128.0);
            if (landing != null && !init_Z(landing, current) && init(view, current, landing)) {
               route.add(landing);
            }
         }

         List<Vec3> cleaned = init(start, route);
         if (reached && !cleaned.isEmpty()) {
            return init(cleaned, "segmented");
         } else if (allowPartial && !cleaned.isEmpty()) {
            return new TpRoutePlanner.Inner_BufferHelper(TpRoutePlanner.Inner_ConfigStore.PARTIAL, cleaned, unloadedFrontier ? "loaded frontier" : "route stopped before target");
         } else {
            return unloadedFrontier
               ? init_F_BufferHelper_Str("loaded frontier unavailable")
               : init_F_BufferHelper_Str(cleaned.isEmpty() ? "no clear route" : "route stopped before target");
         }
      } else {
         return init_F_BufferHelper_Str("no collision view");
      }
   }

   public static TpRoutePlanner.Inner_BufferHelper init_F_BufferHelper_F_StreamUtil(
      TpRoutePlanner.Inner_StreamUtil view, Vec3 start, Vec3 target, int frontierRadius, int verticalRange, int maxWaypoints
   ) {
      if (view != null && start != null && target != null) {
         if (start.distanceTo(target) <= 0.05) {
            return new TpRoutePlanner.Inner_BufferHelper(TpRoutePlanner.Inner_ConfigStore.COMPLETE, List.of(), "arrived");
         }

         if (!(Math.hypot(target.x - start.x, target.z - start.z) <= 0.05)) {
            TpRoutePlanner.Inner_BufferHelper horizontal = init_F_BufferHelper_F_StreamUtil(
               view, start, new Vec3(target.x, start.y, target.z), frontierRadius, verticalRange, maxWaypoints, true
            );
            if (horizontal.init_F_ConfigStore() == TpRoutePlanner.Inner_ConfigStore.BLOCKED) {
               return horizontal;
            }

            List<Vec3> route = new ArrayList<>(horizontal.init_List());
            Vec3 end = route.isEmpty() ? start : route.getLast();
            boolean atTargetColumn = Math.hypot(end.x - target.x, end.z - target.z) <= 0.5;
            if (!atTargetColumn || !init(view, target)) {
               return new TpRoutePlanner.Inner_BufferHelper(TpRoutePlanner.Inner_ConfigStore.PARTIAL, init(start, route), horizontal.init_Str());
            }

            if (!init(view, end, target)) {
               return new TpRoutePlanner.Inner_BufferHelper(TpRoutePlanner.Inner_ConfigStore.PARTIAL, init(start, route), "lava-safe target approach unavailable");
            }

            if (!init_Z(end, target)) {
               route.add(target);
            }

            return init(init(start, route), "target column");
         } else {
            return !init(view, target) ? init_F_BufferHelper_Str("target unsafe or unavailable") : init(List.of(target), "vertical");
         }
      } else {
         return init_F_BufferHelper_Str("no collision view");
      }
   }

   public static TpRoutePlanner.Inner_BufferHelper init_F_BufferHelper_F_StreamUtil(TpRoutePlanner.Inner_StreamUtil view, Vec3 start, Vec3 target, int maxWaypoints) {
      return read(view, start, target, 128, 128, Math.min(3, Math.max(1, maxWaypoints)));
   }

   public static TpRoutePlanner.Inner_BufferHelper load_F_BufferHelper_F_StreamUtil(
      TpRoutePlanner.Inner_StreamUtil view, Vec3 start, Vec3 target, int frontierRadius, int verticalRange, int maxWaypoints
   ) {
      return read(view, start, target, frontierRadius, verticalRange, maxWaypoints);
   }

   public static TpRoutePlanner.Inner_BufferHelper read(TpRoutePlanner.Inner_StreamUtil view, Vec3 start, Vec3 target, int frontierRadius, int verticalRange, int maxWaypoints) {
      if (view != null && start != null && target != null) {
         if (start.distanceTo(target) <= 0.05) {
            return new TpRoutePlanner.Inner_BufferHelper(TpRoutePlanner.Inner_ConfigStore.COMPLETE, List.of(), "arrived");
         }

         TpRoutePlanner.Inner_CachePool probe = new TpRoutePlanner.Inner_CachePool(view, 1800);
         if (!(init_D(start, target) <= 0.05)) {
            double radius = Math.max(1.0, Math.min(128, frontierRadius));
            Vec3 frontier = init_Vec3_F_CachePool_2(probe, start, target, Math.min(init_D(start, target), radius));
            if (frontier != null && !(init_D(start, frontier) < 0.5)) {
               boolean targetColumn = init_D(frontier, target) <= 0.05;
               double preferredY = targetColumn ? target.y : start.y;
               double range = Math.max(0.0, Math.min(128.0, verticalRange));
               Vec3 anchor = init_Vec3_F_CachePool(probe, frontier.x, frontier.z, preferredY, Math.min(128.0, range), Math.min(64.0, range));
               if (anchor != null) {
                  List<Vec3> anchored = init_List_F_CachePool(probe, start, anchor, target.y, range, false);
                  TpRoutePlanner.Inner_BufferHelper route = init_F_BufferHelper_F_CachePool(probe, start, target, anchored, targetColumn, "hybrid stable anchor");
                  if (route.load()) {
                     return route;
                  }
               }

               TpRoutePlanner.Inner_CachePool topProbe = new TpRoutePlanner.Inner_CachePool(view, 1500);
               Vec3 topExit = init(topProbe, start, range);
               List<Vec3> topAdvance = init_List_F_CachePool(topProbe, start, frontier, topExit);
               if (!topAdvance.isEmpty()) {
                  List<Vec3> topLanding = new ArrayList<>(topAdvance);
                  if (anchor != null && topLanding.size() < 3 && !init_Z(topLanding.getLast(), anchor)) {
                     topLanding.add(anchor);
                  }

                  TpRoutePlanner.Inner_BufferHelper topRoute = init_F_BufferHelper_F_CachePool(topProbe, start, target, topLanding, targetColumn, "VClip Top escape");
                  if (topRoute.load()) {
                     return topRoute;
                  }
               }

               if (topExit != null && init_Z(topProbe, start, topExit)) {
                  return new TpRoutePlanner.Inner_BufferHelper(TpRoutePlanner.Inner_ConfigStore.ESCAPE, init(start, List.of(topExit)), "VClip Top preparatory escape");
               }

               if (anchor != null) {
                  List<Vec3> layeredAnchor = init_List_F_CachePool(probe, start, anchor, target.y, range, true);
                  TpRoutePlanner.Inner_BufferHelper layeredRoute = init_F_BufferHelper_F_CachePool(
                     probe, start, target, layeredAnchor, targetColumn, "extended V/H/V anchor"
                  );
                  if (layeredRoute.load()) {
                     return layeredRoute;
                  }
               }

               List<Vec3> airborne = init_List_F_CachePool_2(probe, start, frontier, target, targetColumn, range);
               TpRoutePlanner.Inner_BufferHelper airRoute = init_F_BufferHelper_F_CachePool(probe, start, target, airborne, targetColumn, "air fallback");
               if (airRoute.load()) {
                  return airRoute;
               }

               Vec3 advance = init_Vec3(probe, start, frontier);
               if (advance != null && init_D(start, advance) >= 0.5) {
                  Vec3 localAnchor = init_Vec3_F_CachePool(probe, advance.x, advance.z, start.y, Math.min(16.0, range), Math.min(8.0, range));
                  if (localAnchor != null) {
                     List<Vec3> localRoute = init_List_F_CachePool_2(probe, start, localAnchor, target.y, Math.min(16.0, range));
                     if (!localRoute.isEmpty()) {
                        return new TpRoutePlanner.Inner_BufferHelper(TpRoutePlanner.Inner_ConfigStore.PARTIAL, localRoute, "stable obstacle frontier");
                     }
                  }

                  return new TpRoutePlanner.Inner_BufferHelper(TpRoutePlanner.Inner_ConfigStore.PARTIAL, init(start, List.of(advance)), "air obstacle frontier");
               } else {
                  return init_F_BufferHelper_F_StreamUtil(
                     view, start, target, radius, probe.init() ? "probe-budget unrestricted fallback" : "unrestricted clip fallback"
                  );
               }
            } else {
               return init_F_BufferHelper_F_StreamUtil(view, start, target, radius, "unloaded safety wait");
            }
         } else {
            return !probe.write(target) ? init_F_BufferHelper_Str("lava-safe vertical target unavailable") : init(List.of(target), "VClip target");
         }
      } else {
         return init_F_BufferHelper_Str("no collision view");
      }
   }

   public static Vec3 init(TpRoutePlanner.Inner_StreamUtil view, Vec3 target, double radius) {
      if (view != null && target != null && view.init_Z(target)) {
         if (init(view, target)) {
            return target;
         }

         double limit = Math.max(0.0, radius);
         Vec3 best = null;
         double bestDistance = 1.0 / 0.0;

         for (double dy = -limit; dy <= limit + 1.0E-9; dy += 0.5) {
            for (double dx = -limit; dx <= limit + 1.0E-9; dx += 0.5) {
               for (double dz = -limit; dz <= limit + 1.0E-9; dz += 0.5) {
                  double distance = dx * dx + dy * dy + dz * dz;
                  if (!(distance > limit * limit + 1.0E-9) && !(distance >= bestDistance)) {
                     Vec3 candidate = new Vec3(target.x + dx, init(view, target.y + dy), target.z + dz);
                     if (init(view, candidate)) {
                        best = candidate;
                        bestDistance = distance;
                     }
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   public static Vec3 load(TpRoutePlanner.Inner_StreamUtil view, Vec3 target, double radius) {
      if (view != null && target != null && view.init_Z(target)) {
         if (init(view, target) && view.sync(target)) {
            return target;
         }

         double limit = Math.max(0.0, radius);
         Vec3 best = null;
         double bestDistance = 1.0 / 0.0;

         for (double dy = -limit; dy <= limit + 1.0E-9; dy += 0.0625) {
            for (double dx = -limit; dx <= limit + 1.0E-9; dx += 0.5) {
               for (double dz = -limit; dz <= limit + 1.0E-9; dz += 0.5) {
                  double distance = dx * dx + dy * dy + dz * dz;
                  if (!(distance > limit * limit + 1.0E-9) && !(distance >= bestDistance)) {
                     Vec3 candidate = new Vec3(target.x + dx, init(view, target.y + dy), target.z + dz);
                     if (init(view, candidate) && view.sync(candidate)) {
                        best = candidate;
                        bestDistance = distance;
                     }
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   public static Vec3 read(TpRoutePlanner.Inner_StreamUtil view, Vec3 target, double radius) {
      if (view == null || target == null) {
         return null;
      }

      if (init(view, target) && view.sync(target)) {
         return target;
      }

      int maxRing = Math.max(0, Math.min(64, (int)Math.ceil(radius)));
      double[] yOffsets = new double[]{0.0, 0.5, -0.5, 1.0, -1.0, 2.0, -2.0, 3.0, -3.0, 4.0, -4.0};
      Vec3 best = null;
      double bestClearance = -1.0 / 0.0;
      double bestDistance = 1.0 / 0.0;
      double bestVertical = 1.0 / 0.0;
      int firstSafeRing = -1;

      for (int ring = 0; ring <= maxRing && (firstSafeRing < 0 || ring <= Math.min(maxRing, firstSafeRing + 6)); ring++) {
         for (int dx = -ring; dx <= ring; dx++) {
            for (int dz = -ring; dz <= ring; dz++) {
               if (ring <= 0 || Math.max(Math.abs(dx), Math.abs(dz)) == ring) {
                  for (double dy : yOffsets) {
                     Vec3 candidate = new Vec3(target.x + dx, init(view, target.y + dy), target.z + dz);
                     if (init(view, candidate) && view.sync(candidate)) {
                        if (firstSafeRing < 0) {
                           firstSafeRing = ring;
                        }

                        double clearance = view.init_D(candidate);
                        if (Double.isNaN(clearance)) {
                           clearance = 0.0;
                        }

                        if (clearance >= 6.0) {
                           return candidate;
                        }

                        double distance = candidate.distanceToSqr(target);
                        double vertical = Math.abs(candidate.y - target.y);
                        if (init(candidate, clearance, distance, vertical, best, bestClearance, bestDistance, bestVertical)) {
                           best = candidate;
                           bestClearance = clearance;
                           bestDistance = distance;
                           bestVertical = vertical;
                        }
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   private static boolean init(
      Vec3 candidate, double clearance, double distance, double vertical, Vec3 best, double bestClearance, double bestDistance, double bestVertical
   ) {
      if (best == null) {
         return true;
      }

      int clearanceOrder = Double.compare(clearance, bestClearance);
      if (clearanceOrder != 0) {
         return clearanceOrder > 0;
      }

      int distanceOrder = Double.compare(distance, bestDistance);
      if (distanceOrder != 0) {
         return distanceOrder < 0;
      }

      int verticalOrder = Double.compare(vertical, bestVertical);
      if (verticalOrder != 0) {
         return verticalOrder < 0;
      }

      int xOrder = Double.compare(candidate.x, best.x);
      if (xOrder != 0) {
         return xOrder < 0;
      }

      int yOrder = Double.compare(candidate.y, best.y);
      return yOrder != 0 ? yOrder < 0 : candidate.z < best.z;
   }

   public static Vec3 write(TpRoutePlanner.Inner_StreamUtil view, Vec3 target, double verticalRange) {
      if (view != null && target != null && view.init_Z(target)) {
         double range = Math.max(0.0, Math.min(192.0, verticalRange));
         return init_Vec3_F_CachePool(new TpRoutePlanner.Inner_CachePool(view, 1800), target.x, target.z, target.y, range, range);
      } else {
         return null;
      }
   }

   public static Vec3 update(TpRoutePlanner.Inner_StreamUtil view, Vec3 start, double verticalRange) {
      return view != null && start != null ? init(new TpRoutePlanner.Inner_CachePool(view, 1800), start, Math.max(0.0, Math.min(128.0, verticalRange))) : null;
   }

   private static Vec3 init(TpRoutePlanner.Inner_CachePool probe, Vec3 start, double verticalRange) {
      if (probe != null && start != null) {
         boolean seenBlocked = false;
         double range = Math.max(0.0, Math.min(128.0, verticalRange));

         for (double offset = 0.5; offset <= range + 1.0E-9 && !probe.init(); offset += 0.5) {
            Vec3 candidate = new Vec3(start.x, init(probe.init_F_StreamUtil, start.y + offset), start.z);
            if (init(candidate.y, start.y)) {
               break;
            }

            if (probe.init_Z(candidate)) {
               if (!probe.load(candidate)) {
                  seenBlocked = true;
               } else if (seenBlocked && probe.read(candidate) && probe.update(candidate)) {
                  return candidate;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static List<Vec3> init_List_F_CachePool(TpRoutePlanner.Inner_CachePool probe, Vec3 start, Vec3 frontier, Vec3 topExit) {
      if (topExit != null && frontier != null) {
         Vec3 end = new Vec3(frontier.x, topExit.y, frontier.z);
         if (!init_Z(probe, start, topExit)) {
            return List.of();
         } else if (!probe.write(end)) {
            return List.of();
         } else {
            return !load(probe, topExit, end) ? List.of() : init(start, List.of(topExit, end));
         }
      } else {
         return List.of();
      }
   }

   private static TpRoutePlanner.Inner_BufferHelper init_F_BufferHelper_F_StreamUtil(TpRoutePlanner.Inner_StreamUtil view, Vec3 start, Vec3 target, double frontierRadius, String detail) {
      List<Vec3> route = init_List_F_StreamUtil(view, start, target, frontierRadius);
      return route.isEmpty()
         ? init_F_BufferHelper_Str("lava safety wait: " + detail)
         : new TpRoutePlanner.Inner_BufferHelper(init_Z(route.getLast(), target) ? TpRoutePlanner.Inner_ConfigStore.COMPLETE : TpRoutePlanner.Inner_ConfigStore.PARTIAL, route, detail);
   }

   public static List<Vec3> init_List_F_StreamUtil(TpRoutePlanner.Inner_StreamUtil view, Vec3 start, Vec3 target, double frontierRadius) {
      if (view != null && start != null && target != null) {
         double horizontal = init_D(start, target);
         if (!(horizontal <= 0.05)) {
            double amount = Math.min(horizontal, Math.max(1.0, Math.min(128.0, frontierRadius)));
            Vec3 horizontalEnd = init_Vec3(start, target, amount);
            boolean targetColumn = amount + 0.05 >= horizontal;
            List<Vec3> direct = init_List_F_StreamUtil(view, start, horizontalEnd, target, targetColumn, List.of());
            if (!direct.isEmpty()) {
               return direct;
            }

            double[] offsets = new double[]{
               3.0, -3.0, 4.0, -4.0, 8.0, -8.0, 16.0, -16.0, 32.0, -32.0, 48.0, -48.0, 64.0, -64.0, 80.0, -80.0, 96.0, -96.0, 112.0, -112.0, 128.0, -128.0
            };

            for (double offset : offsets) {
               double layerY = init(view, start.y + offset);
               if (!init(layerY, start.y)) {
                  Vec3 escapeStart = new Vec3(start.x, layerY, start.z);
                  Vec3 escapeEnd = new Vec3(horizontalEnd.x, layerY, horizontalEnd.z);
                  if (init(view, start, escapeStart)) {
                     List<Vec3> layered = init_List_F_StreamUtil(view, escapeStart, escapeEnd, target, targetColumn, List.of(escapeStart));
                     if (!layered.isEmpty()) {
                        return init(start, layered);
                     }
                  }
               }
            }

            return List.of();
         } else {
            return init(view, start, target) && load(view, target) ? init(start, List.of(target)) : List.of();
         }
      } else {
         return List.of();
      }
   }

   private static List<Vec3> init_List_F_StreamUtil(
      TpRoutePlanner.Inner_StreamUtil view, Vec3 segmentStart, Vec3 horizontalEnd, Vec3 target, boolean targetColumn, List<Vec3> prefix
   ) {
      if (!init(view, segmentStart, horizontalEnd)) {
         return List.of();
      }

      List<Vec3> route = new ArrayList<>(prefix);
      route.add(horizontalEnd);
      if (targetColumn && !init_Z(horizontalEnd, target)) {
         if (!init(view, horizontalEnd, target) || !load(view, target)) {
            return List.of();
         }

         route.add(target);
      }

      return List.copyOf(route);
   }

   private static boolean init(TpRoutePlanner.Inner_StreamUtil view, Vec3 from, Vec3 to) {
      if (view != null && from != null && to != null) {
         double distance = from.distanceTo(to);
         int samples = Math.max(1, (int)Math.ceil(distance / 0.5));

         for (int i = 1; i <= samples; i++) {
            Vec3 sample = from.lerp(to, (double)i / samples);
            if (!view.init_Z(sample) || !view.update(sample)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean init_Z(TpRoutePlanner.Inner_CachePool probe, Vec3 from, Vec3 to) {
      if (probe != null && from != null && to != null) {
         double distance = from.distanceTo(to);
         int samples = Math.max(1, (int)Math.ceil(distance / 0.5));

         for (int i = 1; i <= samples && !probe.init(); i++) {
            Vec3 sample = from.lerp(to, (double)i / samples);
            if (!probe.init_Z(sample) || !probe.read(sample)) {
               return false;
            }
         }

         return !probe.init();
      } else {
         return false;
      }
   }

   private static TpRoutePlanner.Inner_BufferHelper init_F_BufferHelper_F_CachePool(
      TpRoutePlanner.Inner_CachePool probe, Vec3 start, Vec3 target, List<Vec3> route, boolean targetColumn, String detail
   ) {
      List<Vec3> cleaned = init(start, route);
      if (cleaned.size() > 3) {
         cleaned = List.copyOf(cleaned.subList(0, 3));
      }

      if (cleaned.isEmpty()) {
         return init_F_BufferHelper_Str(probe.init() ? "collision probe budget exhausted" : detail);
      } else {
         Vec3 end = cleaned.getLast();
         if (init_Z(end, target)) {
            return init(cleaned, detail);
         } else if (targetColumn && init_D(end, target) <= 0.05 && cleaned.size() < 3 && probe.write(target) && init_Z(probe, end, target)) {
            List<Vec3> completed = new ArrayList<>(cleaned);
            completed.add(target);
            return init(init(start, completed), detail + " then VClip target");
         } else {
            return new TpRoutePlanner.Inner_BufferHelper(TpRoutePlanner.Inner_ConfigStore.PARTIAL, cleaned, detail);
         }
      }
   }

   private static List<Vec3> init_List_F_CachePool_2(TpRoutePlanner.Inner_CachePool probe, Vec3 start, Vec3 anchor, double targetY, double verticalRange) {
      return init_List_F_CachePool(probe, start, anchor, targetY, verticalRange, true);
   }

   private static List<Vec3> init_List_F_CachePool(
      TpRoutePlanner.Inner_CachePool probe, Vec3 start, Vec3 anchor, double targetY, double verticalRange, boolean includeLayerScan
   ) {
      if (init_D(start, anchor) <= 0.05) {
         return init(start, List.of(anchor));
      }

      Vec3 startAtAnchorY = new Vec3(start.x, anchor.y, start.z);
      if (probe.write(startAtAnchorY) && probe.write(anchor) && init_Z(probe, start, startAtAnchorY) && load(probe, startAtAnchorY, anchor)) {
         return init(start, List.of(startAtAnchorY, anchor));
      }

      Vec3 endAtStartY = new Vec3(anchor.x, start.y, anchor.z);
      if (probe.write(endAtStartY) && load(probe, start, endAtStartY) && init_Z(probe, endAtStartY, anchor)) {
         return init(start, List.of(endAtStartY, anchor));
      }

      if (!includeLayerScan) {
         return List.of();
      }

      for (double layerY : init_List_F_CachePool(probe, start.y, anchor.y, targetY, verticalRange)) {
         if (!init(layerY, start.y) && !init(layerY, anchor.y)) {
            Vec3 escapeStart = new Vec3(start.x, layerY, start.z);
            Vec3 escapeEnd = new Vec3(anchor.x, layerY, anchor.z);
            if (probe.write(escapeStart)
               && probe.write(escapeEnd)
               && init_Z(probe, start, escapeStart)
               && load(probe, escapeStart, escapeEnd)
               && init_Z(probe, escapeEnd, anchor)) {
               return init(start, List.of(escapeStart, escapeEnd, anchor));
            }
         }
      }

      return List.of();
   }

   private static List<Vec3> init_List_F_CachePool_2(
      TpRoutePlanner.Inner_CachePool probe, Vec3 start, Vec3 frontier, Vec3 target, boolean targetColumn, double verticalRange
   ) {
      Vec3 endAtStartY = new Vec3(frontier.x, start.y, frontier.z);
      if (probe.write(endAtStartY) && load(probe, start, endAtStartY)) {
         List<Vec3> route = new ArrayList<>(2);
         route.add(endAtStartY);
         if (targetColumn && !init_Z(endAtStartY, target) && probe.write(target) && init_Z(probe, endAtStartY, target)) {
            route.add(target);
         }

         return init(start, route);
      } else {
         for (double layerY : init_List_F_CachePool(probe, start.y, target.y, target.y, verticalRange)) {
            if (!init(layerY, start.y)) {
               Vec3 escapeStart = new Vec3(start.x, layerY, start.z);
               Vec3 escapeEnd = new Vec3(frontier.x, layerY, frontier.z);
               if (probe.write(escapeStart) && probe.write(escapeEnd) && init_Z(probe, start, escapeStart) && load(probe, escapeStart, escapeEnd)) {
                  List<Vec3> route = new ArrayList<>(3);
                  route.add(escapeStart);
                  route.add(escapeEnd);
                  if (targetColumn && !init_Z(escapeEnd, target) && probe.write(target) && init_Z(probe, escapeEnd, target)) {
                     route.add(target);
                  }

                  return init(start, route);
               }
            }
         }

         return List.of();
      }
   }

   private static Vec3 init_Vec3_F_CachePool_2(TpRoutePlanner.Inner_CachePool probe, Vec3 start, Vec3 target, double maxDistance) {
      for (double distance = Math.max(0.0, maxDistance); distance >= 0.499999999 && !probe.init(); distance -= 2.0) {
         Vec3 candidate = init_Vec3(start, target, distance);
         if (probe.init_Z(candidate)) {
            return candidate;
         }
      }

      return null;
   }

   private static Vec3 init_Vec3(TpRoutePlanner.Inner_CachePool probe, Vec3 start, Vec3 target) {
      double distance = init_D(start, target);
      if (distance < 0.5) {
         return null;
      }

      Vec3 best = null;

      for (double step = 0.5; step <= distance + 1.0E-9 && !probe.init(); step += 0.5) {
         Vec3 candidate = init_Vec3(start, target, Math.min(step, distance));
         if (!probe.write(candidate)) {
            break;
         }

         best = candidate;
      }

      return best;
   }

   private static boolean load(TpRoutePlanner.Inner_CachePool probe, Vec3 from, Vec3 to) {
      if (!init(from.y, to.y)) {
         return false;
      }

      double distance = init_D(from, to);
      int samples = Math.max(1, (int)Math.ceil(distance / 0.5));

      for (int i = 1; i <= samples && !probe.init(); i++) {
         Vec3 candidate = from.lerp(to, (double)i / samples);
         if (!probe.write(candidate)) {
            return false;
         }
      }

      if (probe.init()) {
         return false;
      }

      Entity entity = probe.init_F_StreamUtil.init_Entity();
      if (entity != null) {
         int segments = Math.max(1, (int)Math.ceil(distance / 8.0));
         Vec3 prev = from;

         for (int i = 1; i <= segments; i++) {
            Vec3 next = from.lerp(to, (double)i / segments);
            if (!ClipCollisionHelper.init_Z(entity, prev, next)) {
               return false;
            }

            prev = next;
         }
      }

      return true;
   }

   private static Vec3 init_Vec3_F_CachePool(TpRoutePlanner.Inner_CachePool probe, double x, double z, double preferredY, double downRange, double upRange) {
      double preferred = init(probe.init_F_StreamUtil, preferredY);
      Vec3 exact = init_Vec3_F_CachePool(probe, x, z, preferred);
      if (exact != null) {
         return exact;
      }

      Vec3 down = load_Vec3_F_CachePool(probe, x, z, preferred, -1.0, Math.max(0.0, downRange));
      return down != null ? down : load_Vec3_F_CachePool(probe, x, z, preferred, 1.0, Math.max(0.0, upRange));
   }

   private static Vec3 load_Vec3_F_CachePool(TpRoutePlanner.Inner_CachePool probe, double x, double z, double preferredY, double direction, double range) {
      double previousY = preferredY;
      Vec3 previous = new Vec3(x, previousY, z);
      boolean previousClear = probe.init_Z(previous) && probe.load(previous);

      for (double offset = 0.25; offset <= range + 1.0E-9 && !probe.init(); offset += 0.25) {
         double y = init(probe.init_F_StreamUtil, preferredY + direction * offset);
         if (init(y, previousY)) {
            break;
         }

         Vec3 candidate = new Vec3(x, y, z);
         TpRoutePlanner.Inner_DataUtil check = probe.init_F_DataUtil(candidate);
         if (check.load()) {
            return candidate;
         }

         boolean clear = check.init();
         if (clear != previousClear) {
            Vec3 boundary = init_Vec3_F_CachePool(probe, x, z, previousY, previousClear, y, clear);
            if (boundary != null) {
               return boundary;
            }
         }

         previousY = y;
         previousClear = clear;
      }

      return null;
   }

   private static Vec3 init_Vec3_F_CachePool(
      TpRoutePlanner.Inner_CachePool probe, double x, double z, double firstY, boolean firstClear, double secondY, boolean secondClear
   ) {
      if (firstClear == secondClear) {
         return null;
      }

      double clearY = firstClear ? firstY : secondY;
      double blockedY = firstClear ? secondY : firstY;

      for (int i = 0; i < 7 && !probe.init(); i++) {
         double middleY = (clearY + blockedY) * 0.5;
         Vec3 middle = new Vec3(x, middleY, z);
         if (probe.init_Z(middle) && probe.load(middle)) {
            clearY = middleY;
         } else {
            blockedY = middleY;
         }
      }

      return init_Vec3_F_CachePool(probe, x, z, clearY);
   }

   private static Vec3 init_Vec3_F_CachePool(TpRoutePlanner.Inner_CachePool probe, double x, double z, double y) {
      double clamped = init(probe.init_F_StreamUtil, y);
      Vec3 candidate = new Vec3(x, clamped, z);
      if (probe.init_F_DataUtil(candidate).load()) {
         return candidate;
      }

      double snapped = init(probe.init_F_StreamUtil, Math.rint(clamped * 16.0) / 16.0);
      if (!init(snapped, clamped)) {
         Vec3 snappedCandidate = new Vec3(x, snapped, z);
         if (probe.init_F_DataUtil(snappedCandidate).load()) {
            return snappedCandidate;
         }
      }

      return null;
   }

   private static List<Double> init_List_F_CachePool(TpRoutePlanner.Inner_CachePool probe, double startY, double anchorY, double targetY, double verticalRange) {
      List<Double> heights = new ArrayList<>(32);
      init(heights, init(probe.init_F_StreamUtil, startY));
      init(heights, init(probe.init_F_StreamUtil, anchorY));
      init(heights, init(probe.init_F_StreamUtil, targetY));
      int[] offsets = new int[]{
         1, -1, 2, -2, 3, 4, -3, -4, 6, -6, 8, -8, 12, -12, 16, -16, 24, -24, 32, -32, 48, -48, 64, -64, 80, -80, 96, -96, 112, -112, 128, -128
      };
      double range = Math.max(0.0, Math.min(128.0, verticalRange));

      for (int offset : offsets) {
         if (heights.size() >= 32) {
            break;
         }

         if (!(Math.abs(offset) > range + 1.0E-9)) {
            init(heights, init(probe.init_F_StreamUtil, startY + offset));
         }
      }

      return List.copyOf(heights);
   }

   private static void init(List<Double> heights, double y) {
      for (double existing : heights) {
         if (init(existing, y)) {
            return;
         }
      }

      heights.add(y);
   }

   private static boolean init(double a, double b) {
      return Math.abs(a - b) <= 1.0E-5;
   }

   static boolean init_Z(Vec3 actual, Vec3 target, double tolerance) {
      return actual != null && target != null && actual.distanceTo(target) <= Math.max(0.0, tolerance);
   }

   private static TpRoutePlanner.Inner_BufferHelper init_F_BufferHelper_F_StreamUtil_2(TpRoutePlanner.Inner_StreamUtil view, Vec3 start, Vec3 target, int escapeRange, int routeCap) {
      if (!view.init_Z(target)) {
         return init_F_BufferHelper_Str("target chunk unavailable");
      }

      for (int dy : init_List_I(escapeRange)) {
         if (dy != 0) {
            double y = init(view, start.y + dy);
            Vec3 escapeStart = new Vec3(start.x, y, start.z);
            Vec3 escapeEnd = new Vec3(target.x, y, target.z);
            if (init(view, escapeStart) && init(view, escapeEnd) && init(view, start, escapeStart) && load(view, escapeStart, escapeEnd)) {
               List<Vec3> route = new ArrayList<>(3);
               route.add(escapeStart);
               route.add(escapeEnd);
               Vec3 landing = init_Vec3_F_StreamUtil(view, target.x, target.z, start.y, 128.0);
               if (landing != null && !init_Z(landing, escapeEnd) && init(view, escapeEnd, landing)) {
                  route.add(landing);
               }

               List<Vec3> cleaned = init(start, route);
               if (!cleaned.isEmpty() && cleaned.size() <= routeCap) {
                  return init(cleaned, "layered dy=" + dy);
               }
            }
         }
      }

      return init_F_BufferHelper_Str("no clear layer");
   }

   private static Vec3 init_Vec3_F_StreamUtil(TpRoutePlanner.Inner_StreamUtil view, double x, double z, double preferredY, double range) {
      for (double offset = 0.0; offset <= range; offset += 0.5) {
         int[] signs = offset == 0.0 ? new int[]{1} : new int[]{1, -1};

         for (int sign : signs) {
            Vec3 candidate = new Vec3(x, init(view, preferredY + sign * offset), z);
            if (init(view, candidate)) {
               return candidate;
            }
         }
      }

      return null;
   }

   private static Vec3 init_Vec3_F_StreamUtil(TpRoutePlanner.Inner_StreamUtil view, Vec3 from, Vec3 target, int searchRadius) {
      double distance = init_D(from, target);
      if (distance < 1.0E-5) {
         return from;
      }

      double maxDistance = Math.min(distance, Math.max(1, searchRadius));
      Entity entity = view.init_Entity();
      Vec3 best = from;

      for (double step = 0.5; step <= maxDistance + 1.0E-9; step += 0.5) {
         Vec3 candidate = new Vec3(from.x + (target.x - from.x) / distance * step, from.y, from.z + (target.z - from.z) / distance * step);
         if (entity != null ? !ClipCollisionHelper.init_Z(entity, best, candidate) : !init(view, candidate)) {
            break;
         }

         best = candidate;
      }

      return best;
   }

   private static boolean load(TpRoutePlanner.Inner_StreamUtil view, Vec3 from, Vec3 to) {
      if (Math.abs(from.y - to.y) > 1.0E-5) {
         return false;
      }

      Entity entity = view.init_Entity();
      if (entity != null) {
         double distance = from.distanceTo(to);
         int segments = Math.max(1, (int)Math.ceil(distance / 8.0));
         Vec3 prev = from;

         for (int i = 1; i <= segments; i++) {
            Vec3 next = from.lerp(to, (double)i / segments);
            if (!ClipCollisionHelper.init_Z(entity, prev, next)) {
               return false;
            }

            prev = next;
         }

         return true;
      } else {
         double distance = from.distanceTo(to);
         int samples = Math.max(1, (int)Math.ceil(distance / 0.25));

         for (int i = 1; i <= samples; i++) {
            Vec3 candidate = from.lerp(to, (double)i / samples);
            if (!init(view, candidate)) {
               return false;
            }
         }

         return true;
      }
   }

   private static List<Integer> init_List_I(int range) {
      List<Integer> offsets = new ArrayList<>(range * 2 + 1);
      offsets.add(0);

      for (int i = 1; i <= range; i++) {
         offsets.add(i);
         offsets.add(-i);
      }

      return offsets;
   }

   private static List<Vec3> init(Vec3 start, List<Vec3> route) {
      List<Vec3> cleaned = new ArrayList<>();
      Vec3 previous = start;

      for (Vec3 waypoint : route) {
         if (waypoint != null && !init_Z(previous, waypoint)) {
            cleaned.add(waypoint);
            previous = waypoint;
         }
      }

      return List.copyOf(cleaned);
   }

   private static boolean init(TpRoutePlanner.Inner_StreamUtil view, Vec3 position) {
      return load(view, position) && view.load(position);
   }

   private static boolean load(TpRoutePlanner.Inner_StreamUtil view, Vec3 position) {
      return view != null && position != null && view.init_Z(position) && view.update(position);
   }

   private static Vec3 init_Vec3(Vec3 from, Vec3 target, double distance) {
      double horizontal = init_D(from, target);
      return horizontal <= distance
         ? new Vec3(target.x, from.y, target.z)
         : new Vec3(from.x + (target.x - from.x) / horizontal * distance, from.y, from.z + (target.z - from.z) / horizontal * distance);
   }

   private static double init(TpRoutePlanner.Inner_StreamUtil view, double y) {
      return Math.max(view.init_D(), Math.min(view.load(), y));
   }

   private static double init_D(Vec3 a, Vec3 b) {
      return Math.hypot(a.x - b.x, a.z - b.z);
   }

   private static boolean init_Z(Vec3 a, Vec3 b) {
      return a.distanceToSqr(b) < 1.0E-10;
   }

   private static TpRoutePlanner.Inner_BufferHelper init(List<Vec3> waypoints, String detail) {
      return new TpRoutePlanner.Inner_BufferHelper(TpRoutePlanner.Inner_ConfigStore.COMPLETE, waypoints, detail);
   }

   private static TpRoutePlanner.Inner_BufferHelper init_F_BufferHelper_Str(String detail) {
      return new TpRoutePlanner.Inner_BufferHelper(TpRoutePlanner.Inner_ConfigStore.BLOCKED, List.of(), detail);
   }

   public record Inner_BufferHelper(TpRoutePlanner.Inner_ConfigStore init_F_ConfigStore, List<Vec3> init_List, String init_Str) {
      public Inner_BufferHelper {
         init_F_ConfigStore = init_F_ConfigStore == null ? TpRoutePlanner.Inner_ConfigStore.BLOCKED : init_F_ConfigStore;
         init_List = init_List == null ? List.of() : List.copyOf(init_List);
         init_Str = init_Str == null ? "" : init_Str;
      }

      public boolean init_Z() {
         return this.init_F_ConfigStore == TpRoutePlanner.Inner_ConfigStore.COMPLETE;
      }

      public boolean load() {
         return !this.init_List.isEmpty();
      }
   }

   private static final class Inner_CachePool {
      final TpRoutePlanner.Inner_StreamUtil init_F_StreamUtil;
      private int init_I;

      Inner_CachePool(TpRoutePlanner.Inner_StreamUtil view, int budget) {
         this.init_F_StreamUtil = view;
         this.init_I = Math.max(1, budget);
      }

      boolean init_Z(Vec3 position) {
         return this.load() && this.init_F_StreamUtil.init_Z(position);
      }

      boolean load(Vec3 position) {
         return this.load() && this.init_F_StreamUtil.load(position);
      }

      boolean read(Vec3 position) {
         return this.load() && this.init_F_StreamUtil.update(position);
      }

      boolean write(Vec3 position) {
         return this.init_Z(position) && this.read(position) && this.load(position);
      }

      boolean update(Vec3 position) {
         return this.load() && this.init_F_StreamUtil.read(position);
      }

      TpRoutePlanner.Inner_DataUtil init_F_DataUtil(Vec3 position) {
         if (!this.init_Z(position)) {
            return new TpRoutePlanner.Inner_DataUtil(false, false);
         }

         if (!this.read(position)) {
            return new TpRoutePlanner.Inner_DataUtil(false, false);
         }

         boolean clear = this.load(position);
         return clear && this.load() ? new TpRoutePlanner.Inner_DataUtil(true, this.init_F_StreamUtil.sync(position)) : new TpRoutePlanner.Inner_DataUtil(clear, false);
      }

      boolean init() {
         return this.init_I <= 0;
      }

      private boolean load() {
         if (this.init_I <= 0) {
            return false;
         }

         this.init_I--;
         return true;
      }
   }

   public enum Inner_ConfigStore {
      COMPLETE,
      PARTIAL,
      ESCAPE,
      BLOCKED;
   }

   private record Inner_DataUtil(boolean init, boolean load) {
   }

   public interface Inner_StreamUtil {
      boolean init_Z(Vec3 var1);

      boolean load(Vec3 var1);

      default Entity init_Entity() {
         return null;
      }

      default boolean read(Vec3 position) {
         return this.load(position);
      }

      default boolean write(Vec3 position) {
         return false;
      }

      default boolean sync(Vec3 position) {
         return this.read(position) || this.write(position);
      }

      default boolean update(Vec3 position) {
         return true;
      }

      default double init_D(Vec3 position) {
         return this.update(position) ? 1.0 / 0.0 : 0.0;
      }

      double init_D();

      double load();
   }
}
