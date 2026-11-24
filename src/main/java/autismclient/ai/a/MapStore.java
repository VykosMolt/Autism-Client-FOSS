package autismclient.ai.a;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

final class MapStore {
   private static final int init_I = 720;
   private static final int load_I = 11;
   private static final int[] init_IArr = new int[]{0, -1, 1};
   private static final int[] load_IArr = new int[]{0, -4, 4, -10, 10};
   private static final int[] read = new int[]{0, -30, 30};

   private MapStore() {
   }

   static TableIndex.Inner_StateCache init(NodeWalker.Inner_CachePool world, BlockPos from, double surface, BlockPos to, double targetSurface) {
      List<TableIndex.Inner_SyncManager> landingStances = TableIndex.init_List(world, to, targetSurface);
      if (!landingStances.isEmpty() && !TableIndex.init_Z_NodeWalker_CachePo(world, to.getX() + 0.5, targetSurface, to.getZ() + 0.5)) {
         TableIndex.Inner_SyncManager landing = landingStances.getFirst();
         AABB floor = new AABB(from.getX(), surface - 0.02, from.getZ(), from.getX() + 1, surface - 0.001, from.getZ() + 1);

         for (AABB support : world.init_List(floor)) {
            if (!(Math.abs(support.maxY - surface) > 0.01)) {
               boolean alongX = support.getXsize() >= 0.75 && support.getZsize() <= 0.25;
               if (alongX || support.getZsize() >= 0.75 && support.getXsize() <= 0.25) {
                  int along = alongX ? to.getX() - from.getX() : to.getZ() - from.getZ();
                  int across = alongX ? to.getZ() - from.getZ() : to.getX() - from.getX();
                  if (Math.abs(along) >= 2 && across != 0) {
                     int alongSign = Integer.signum(along);
                     int acrossSign = Integer.signum(across);
                     float yaw = alongX ? -90 * alongSign : (alongSign > 0 ? 0.0F : 180.0F);
                     double middle = alongX ? to.getX() + 0.5 : to.getZ() + 0.5;
                     double edge = alongX ? (alongSign > 0 ? support.maxX : support.minX) : (alongSign > 0 ? support.maxZ : support.minZ);
                     double acrossGoal = (alongX ? landing.z() : landing.x()) * acrossSign;
                     MapStore.Inner_DataUtil approach = new MapStore.Inner_DataUtil(
                        alongX,
                        alongSign,
                        acrossSign,
                        yaw,
                        edge * alongSign + 0.65,
                        middle * alongSign - 0.78,
                        acrossGoal - 0.06,
                        middle * alongSign - 0.4,
                        acrossGoal - 0.01
                     );
                     TableIndex.Inner_StateCache plan = init(world, from, surface, to, targetSurface, support, approach);
                     if (plan != null) {
                        return plan;
                     }
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static TableIndex.Inner_StateCache init(
      NodeWalker.Inner_CachePool world, BlockPos from, double surface, BlockPos to, double targetSurface, AABB panel, MapStore.Inner_DataUtil approach
   ) {
      Long2ObjectOpenHashMap<MapStore.Inner_StreamUtil> launches = new Long2ObjectOpenHashMap<>();
      List<MapStore.Inner_StreamUtil> runway = init(world, from, panel, surface, approach.alongX());
      int turnSign = approach.alongX() ? approach.alongSign() * approach.acrossSign() : -approach.alongSign() * approach.acrossSign();

      for (int run = 0; run <= 11 && !runway.isEmpty(); run++) {
         if (Thread.currentThread().isInterrupted()) {
            return null;
         }

         Long2ObjectOpenHashMap<MapStore.Inner_StreamUtil> next = new Long2ObjectOpenHashMap<>();

         for (MapStore.Inner_StreamUtil frame : runway) {
            NodeWalker.Inner_StreamUtil state = frame.state();
            float view = frame.input() == null ? approach.yaw() : frame.input().yaw();

            for (int turn = -18; turn <= 102; turn += 6) {
               float yaw = approach.yaw() + turn * turnSign;
               if (!(Math.abs(Mth.wrapDegrees(yaw - view)) > 56.0F)) {
                  for (int side : init_IArr) {
                     NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(1.0, side, true, true, false, yaw);
                     NodeWalker.Inner_StreamUtil airborne = NodeWalker.init(state, input, world);
                     if (!airborne.onGround() && !(airborne.y() < surface + 0.12) && !NodeWalker.load(world, state, airborne)) {
                        init(launches, new MapStore.Inner_StreamUtil(airborne, input, frame, run, approach.score(airborne, targetSurface)));
                     }
                  }
               }
            }

            for (int turn : load_IArr) {
               for (int side : init_IArr) {
                  NodeWalker.Inner_DataUtil key = new NodeWalker.Inner_DataUtil(1.0, side, false, true, false, approach.yaw() + turn);
                  NodeWalker.Inner_StreamUtil grounded = NodeWalker.init(state, key, world);
                  if (grounded.onGround()
                     && !(Math.abs(grounded.y() - surface) > 0.02)
                     && !(approach.along(grounded) > approach.launchLimit())
                     && !NodeWalker.load(world, state, grounded)) {
                     init(next, new MapStore.Inner_StreamUtil(grounded, key, frame, run, 0.0));
                  }
               }
            }
         }

         runway = new ArrayList<>(next.values());
      }

      List<MapStore.Inner_StreamUtil> layer = init(launches);
      TableIndex.Inner_StateCache viable = null;

      for (int tick = 0; tick < 14 && !layer.isEmpty(); tick++) {
         if (Thread.currentThread().isInterrupted()) {
            return null;
         }

         Long2ObjectOpenHashMap<MapStore.Inner_StreamUtil> next = new Long2ObjectOpenHashMap<>();

         for (MapStore.Inner_StreamUtil frame : layer) {
            for (int turn : read) {
               for (int forward : init_IArr) {
                  for (int side : init_IArr) {
                     if (forward != 0 || side != 0 || turn == 0) {
                        float yaw = frame.input().yaw() + turn;
                        NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(forward, side, false, forward > 0, false, yaw);
                        NodeWalker.Inner_StreamUtil air = NodeWalker.init(frame.state(), input, world);
                        if ((!(air.y() < targetSurface - 0.02) || !(air.vy() < 0.0)) && !NodeWalker.load(world, frame.state(), air)) {
                           MapStore.Inner_StreamUtil result = new MapStore.Inner_StreamUtil(
                              air, input, frame, frame.run(), approach.score(air, targetSurface) + Math.abs(turn) * 4.0E-4
                           );
                           if (air.onGround() && Math.abs(air.y() - targetSurface) <= 0.02) {
                              double margin = Math.min(
                                 TableIndex.init_D_NodeWalker_CachePo_2(world, air, to, targetSurface, yaw), init(world, air, targetSurface)
                              );
                              if (margin >= -0.04 && (viable == null || margin > viable.margin())) {
                                 TableIndex.Inner_StateCache plan = init(from, result, margin, approach.yaw());
                                 if (plan.maneuver().replay(world, plan.maneuver().start(), 0, to, targetSurface) >= -0.04) {
                                    if (margin >= 0.03) {
                                       return plan;
                                    }

                                    viable = plan;
                                 }
                              }
                           }

                           init(next, result);
                        }
                     }
                  }
               }
            }
         }

         layer = init(next);
      }

      return viable;
   }

   private static double init(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, double surface) {
      double half = 0.3;
      AABB feet = new AABB(state.x() - half, surface - 0.02, state.z() - half, state.x() + half, surface - 0.001, state.z() + half);
      double margin = -1.0;

      for (AABB support : world.init_List(feet)) {
         if (!(Math.abs(support.maxY - surface) > 0.01)) {
            double x = Math.min(feet.maxX, support.maxX) - Math.max(feet.minX, support.minX);
            double z = Math.min(feet.maxZ, support.maxZ) - Math.max(feet.minZ, support.minZ);
            margin = Math.max(margin, Math.min(x, z) - 0.055);
         }
      }

      return margin;
   }

   private static List<MapStore.Inner_StreamUtil> init(NodeWalker.Inner_CachePool world, BlockPos from, AABB panel, double surface, boolean alongX) {
      ArrayList<MapStore.Inner_StreamUtil> out = new ArrayList<>();
      int along = alongX ? from.getX() : from.getZ();
      int across = alongX ? from.getZ() : from.getX();
      double low = alongX ? panel.minZ : panel.minX;
      double high = alongX ? panel.maxZ : panel.maxX;

      for (double width : new double[]{0.288, 0.2275, 0.1875, 0.1475}) {
         for (double side : new double[]{low - width, high + width}) {
            if (!(side < across) && !(side > across + 1)) {
               for (double pos : new double[]{-0.265, 0.015, 0.44, 0.72, 1.265}) {
                  double x = alongX ? along + pos : side;
                  double z = alongX ? side : along + pos;
                  if (TableIndex.init_Z_NodeWalker_CachePo(world, x, surface, z)) {
                     out.add(new MapStore.Inner_StreamUtil(NodeWalker.Inner_StreamUtil.standing(x, surface, z), null, null, 0, 0.0));
                  }
               }
            }
         }
      }

      return out;
   }

   private static void init(Long2ObjectOpenHashMap<MapStore.Inner_StreamUtil> best, MapStore.Inner_StreamUtil candidate) {
      NodeWalker.Inner_StreamUtil s = candidate.state();
      long key = Math.round(s.x() * 32.0) & 511L
         | (Math.round(s.z() * 32.0) & 511L) << 9
         | (Math.round(s.y() * 40.0) & 511L) << 18
         | (Math.round(s.vx() * 60.0) & 127L) << 27
         | (Math.round(s.vz() * 60.0) & 127L) << 34
         | (Math.round(s.vy() * 60.0) & 63L) << 41;
      if (s.onGround()) {
         key |= 140737488355328L;
      }

      MapStore.Inner_StreamUtil previous = best.get(key);
      if (previous == null || candidate.score() < previous.score()) {
         best.put(key, candidate);
      }
   }

   private static List<MapStore.Inner_StreamUtil> init(Long2ObjectOpenHashMap<MapStore.Inner_StreamUtil> candidates) {
      ArrayList<MapStore.Inner_StreamUtil> ordered = new ArrayList<>(candidates.values());
      ordered.sort(Comparator.comparingDouble(MapStore.Inner_StreamUtil::score));
      return ordered.subList(0, Math.min(720, ordered.size()));
   }

   private static TableIndex.Inner_StateCache init(BlockPos from, MapStore.Inner_StreamUtil end, double margin, float yaw) {
      ArrayDeque<NodeWalker.Inner_DataUtil> inputs = new ArrayDeque<>();

      MapStore.Inner_StreamUtil frame;
      for (frame = end; frame.input() != null; frame = frame.previous()) {
         inputs.addFirst(frame.input());
      }

      NodeWalker.Inner_StreamUtil start = frame.state();
      NodeWalker.Inner_StreamUtil landing = end.state();
      PoolEntry maneuver = new PoolEntry(List.copyOf(inputs), start, landing, margin, end.run());
      double backup = Math.hypot(start.x() - from.getX() - 0.5, start.z() - from.getZ() - 0.5);
      return new TableIndex.Inner_StateCache(
         yaw,
         end.run(),
         true,
         2147483647,
         inputs.size() - end.run(),
         start.x(),
         start.z(),
         landing.x(),
         landing.y(),
         landing.z(),
         landing.speed(),
         margin,
         backup,
         0.0,
         maneuver
      );
   }

   private record Inner_DataUtil(
      boolean alongX,
      int alongSign,
      int acrossSign,
      float yaw,
      double launchLimit,
      double cornerAlong,
      double cornerAcross,
      double landingAlong,
      double landingAcross
   ) {
      double along(NodeWalker.Inner_StreamUtil s) {
         return (this.alongX ? s.x() : s.z()) * this.alongSign;
      }

      double across(NodeWalker.Inner_StreamUtil s) {
         return (this.alongX ? s.z() : s.x()) * this.acrossSign;
      }

      double score(NodeWalker.Inner_StreamUtil s, double surface) {
         double along = this.along(s);
         double across = this.across(s);
         double alongSpeed = (this.alongX ? s.vx() : s.vz()) * this.alongSign;
         double acrossSpeed = (this.alongX ? s.vz() : s.vx()) * this.acrossSign;
         return along < this.cornerAlong && across < this.cornerAcross
            ? Math.hypot((this.cornerAcross - across - acrossSpeed * 0.9) * 1.46, this.cornerAlong + 0.1 - along - alongSpeed * 0.9)
               + Math.max(0.0, surface + 0.1 - s.y()) * 0.3
            : Math.hypot(this.landingAcross - across - acrossSpeed * 0.6, this.landingAlong - along - alongSpeed * 0.6) + Math.max(0.0, surface - s.y()) * 0.3;
      }
   }

   private record Inner_StreamUtil(
      NodeWalker.Inner_StreamUtil state, NodeWalker.Inner_DataUtil input, MapStore.Inner_StreamUtil previous, int run, double score
   ) {
   }
}
