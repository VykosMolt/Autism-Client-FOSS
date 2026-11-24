package autismclient.ai.a;

import it.unimi.dsi.fastutil.HashCommon;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public record PoolEntry(
   List<NodeWalker.Inner_DataUtil> inputs, NodeWalker.Inner_StreamUtil start, NodeWalker.Inner_StreamUtil landing, double margin, int takeoffTick
) {
   public static final double BACKUP = 4.0;
   private static final int RUN_TICKS = 48;
   private static final int[] RELEASES = new int[]{2147483647, 0, 3, 6, 9, 12};
   private static final int[] COMMON_RELEASES = new int[]{2147483647, 3, 6, 9};
   private static final int[] STRAFES = new int[]{0, 1, -1};
   private static final float[] AIR_OFFSETS = new float[]{0.0F, -20.0F, 20.0F, -40.0F, 40.0F, -60.0F, 60.0F};
   private static final float[] COMMON_AIR_OFFSETS = new float[]{0.0F, -20.0F, 20.0F, -40.0F, 40.0F};
   private static final float[] LAUNCH_TURNS = new float[]{1.0F, 0.0F, 0.5F};
   private static final float[] DROP_LAUNCH_TURNS = new float[]{1.0F, 0.0F};

   public PoolEntry {
      inputs = List.copyOf(inputs);
   }

   static NodeWalker.Inner_DataUtil settleInput(NodeWalker.Inner_StreamUtil state, float yaw) {
      return settleInput(null, state, yaw);
   }

   static NodeWalker.Inner_DataUtil settleInput(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, float yaw) {
      double speed = state.speed();
      double forward = 0.0;
      double strafe = 0.0;
      if (speed >= 0.1) {
         double r = Math.toRadians(yaw);
         double along = (-state.vx() * Math.sin(r) + state.vz() * Math.cos(r)) / speed;
         double across = (state.vx() * Math.cos(r) + state.vz() * Math.sin(r)) / speed;
         forward = Math.abs(along) > 0.38 ? -Math.signum(along) : 0.0;
         strafe = Math.abs(across) > 0.38 ? -Math.signum(across) : 0.0;
      }

      boolean sneak = true;
      if (world != null && state.onGround()) {
         NodeWalker.Inner_DataUtil plain = new NodeWalker.Inner_DataUtil(forward, strafe, false, false, false, yaw);
         NodeWalker.Inner_StreamUtil next = state;
         boolean safe = true;

         for (int t = 0; t < 8; t++) {
            NodeWalker.Inner_DataUtil input = next.speed() > 0.03 ? plain : new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, yaw);
            NodeWalker.Inner_StreamUtil after = NodeWalker.init(next, input, world);
            if (NodeWalker.load(world, next, after) || !after.onGround() || Math.abs(after.y() - state.y()) > 0.02) {
               safe = false;
               break;
            }

            next = after;
            if (next.speed() <= 0.02) {
               break;
            }
         }

         if (safe) {
            sneak = false;
         }
      }

      return new NodeWalker.Inner_DataUtil(forward, strafe, false, false, sneak, yaw);
   }

   public PoolEntry shifted(int dx, int dy, int dz) {
      return new PoolEntry(this.inputs, shifted(this.start, dx, dy, dz), shifted(this.landing, dx, dy, dz), this.margin, this.takeoffTick);
   }

   private static NodeWalker.Inner_StreamUtil shifted(NodeWalker.Inner_StreamUtil s, int dx, int dy, int dz) {
      return new NodeWalker.Inner_StreamUtil(
         s.x() + dx,
         s.y() + dy,
         s.z() + dz,
         s.vx(),
         s.vy(),
         s.vz(),
         s.onGround(),
         s.jumpDelay(),
         s.horizontalCollision(),
         s.verticalCollision(),
         s.support() == -9223372036854775808L ? -9223372036854775808L : BlockPos.of(s.support()).offset(dx, dy, dz).asLong(),
         s.sprintCut(),
         s.pose(),
         s.stuck()
      );
   }

   public double replay(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, int first, BlockPos to, double surface) {
      return this.replay(world, state, first, to, surface, -1, null);
   }

   private double replay(
      NodeWalker.Inner_CachePool world,
      NodeWalker.Inner_StreamUtil state,
      int first,
      BlockPos to,
      double surface,
      int correctionTick,
      NodeWalker.Inner_DataUtil correction
   ) {
      if (TableIndex.init(world, state)) {
         return -1.0;
      }

      double peak = state.y();

      for (int i = first; i < this.inputs.size(); i++) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, i == correctionTick ? correction : this.inputs.get(i), world);
         if (NodeWalker.load(world, state, next) || TableIndex.init(world, next) || next.y() < surface - 4.0) {
            return -1.0;
         }

         peak = Math.max(peak, next.y());
         if (TableIndex.init(next)) {
            if (peak - next.y() > world.init_D() + 1.0E-5) {
               return -1.0;
            }

            peak = next.y();
         }

         state = next;
      }

      return TableIndex.init(state) && !(Math.abs(state.y() - surface) > 0.02)
         ? TableIndex.init_D_NodeWalker_CachePo_2(world, state, to, surface, this.inputs.get(this.inputs.size() - 1).yaw())
         : -1.0;
   }

   PoolEntry fitFlight(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil live, int first, BlockPos to, double surface) {
      int last = Math.min(first + 4, this.inputs.size() - 1);
      double best = -0.04;
      NodeWalker.Inner_DataUtil correction = null;
      int changed = -1;

      for (int tick = first; tick < last; tick++) {
         NodeWalker.Inner_DataUtil planned = this.inputs.get(tick);
         if (!planned.jump() && !planned.sneak()) {
            for (int forward = -1; forward <= 1; forward++) {
               for (int side = -1; side <= 1; side++) {
                  if (forward != planned.forward() || side != planned.strafe()) {
                     NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(
                        forward, side, false, forward > 0 && planned.sprint(), false, planned.yaw()
                     );
                     double margin = this.replay(world, live, first, to, surface, tick, input);
                     if (!(margin < best)) {
                        best = margin;
                        changed = tick;
                        correction = input;
                     }
                  }
               }
            }
         }
      }

      if (correction == null) {
         return null;
      }

      ArrayList<NodeWalker.Inner_DataUtil> suffix = new ArrayList<>(this.inputs.subList(first, this.inputs.size()));
      suffix.set(changed - first, correction);
      NodeWalker.Inner_StreamUtil end = live;

      for (NodeWalker.Inner_DataUtil input : suffix) {
         end = NodeWalker.init(end, input, world);
      }

      return new PoolEntry(suffix, live, end, best, 0);
   }

   static boolean coastsIntoLanding(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, BlockPos to, double surface, float yaw) {
      for (int tick = 0; tick < 4 && state.speed() > 0.01; tick++) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, NodeWalker.Inner_DataUtil.NONE, world);
         if (!TableIndex.init(next) || Math.abs(next.y() - surface) > 0.02 || NodeWalker.load(world, state, next)) {
            return false;
         }

         if ((int)Math.floor(next.x()) == to.getX()
            && (int)Math.floor(next.z()) == to.getZ()
            && TableIndex.init_D_NodeWalker_CachePo_2(world, next, to, surface, yaw) >= -0.12) {
            return true;
         }

         state = next;
      }

      return false;
   }

   PoolEntry fitApproach(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil live, BlockPos to, double surface, float yaw) {
      if (live.onGround() && !(Math.abs(live.y() - this.start.y()) > 0.02) && !(Math.hypot(live.x() - this.start.x(), live.z() - this.start.z()) > 0.35)) {
         List<PoolEntry.Inner_TaskQueue> frontier = List.of(new PoolEntry.Inner_TaskQueue(live, List.of(), 0.0));
         float[] setupTurns = new float[]{0.0F, -3.0F, 3.0F, -8.0F, 8.0F, -16.0F, 16.0F};

         for (int depth = 0; depth < 5; depth++) {
            ArrayList<PoolEntry.Inner_TaskQueue> queue = new ArrayList<>();

            for (PoolEntry.Inner_TaskQueue setup : frontier) {
               for (boolean sneak : new boolean[]{false, true}) {
                  for (int f = -1; f <= 1; f++) {
                     for (int side = -1; side <= 1; side++) {
                        for (float turn : setupTurns) {
                           if (f != 0 || side != 0 || turn == 0.0F) {
                              NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(f, side, false, false, sneak, yaw + turn);
                              NodeWalker.Inner_StreamUtil state = NodeWalker.init(setup.state(), input, world);
                              if (state.onGround()
                                 && !(Math.abs(state.y() - live.y()) > 0.02)
                                 && !NodeWalker.load(world, setup.state(), state)
                                 && (this.start.pose() != 0 || TableIndex.init_Z_NodeWalker_CachePo(world, state.x(), state.y(), state.z()))) {
                                 double error = Math.hypot(state.x() - this.start.x(), state.z() - this.start.z());
                                 if (!(error > 0.32)) {
                                    double proof = error <= 0.09 && state.pose() == this.start.pose() ? this.replay(world, state, 0, to, surface) : -1.0;
                                    ArrayList<NodeWalker.Inner_DataUtil> prefix = new ArrayList<>(setup.prefix());
                                    prefix.add(input);
                                    if (proof >= -0.04) {
                                       return this.withApproach(world, live, state, prefix, proof);
                                    }

                                    queue.add(new PoolEntry.Inner_TaskQueue(state, prefix, error + state.speed() * 0.35 + (sneak ? 0.025 : 0.0)));
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            queue.sort(Comparator.comparingDouble(PoolEntry.Inner_TaskQueue::distance));
            if (queue.isEmpty()) {
               return null;
            }

            frontier = queue.subList(0, Math.min(18, queue.size()));

            for (PoolEntry.Inner_TaskQueue setup : frontier) {
               if (setup.state().pose() == this.start.pose()) {
                  double proof = this.replay(world, setup.state(), 0, to, surface);
                  if (!(proof < -0.04)) {
                     return this.withApproach(world, live, setup.state(), setup.prefix(), proof);
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private PoolEntry withApproach(
      NodeWalker.Inner_CachePool world,
      NodeWalker.Inner_StreamUtil live,
      NodeWalker.Inner_StreamUtil end,
      List<NodeWalker.Inner_DataUtil> approach,
      double proof
   ) {
      ArrayList<NodeWalker.Inner_DataUtil> fitted = new ArrayList<>(approach);
      fitted.addAll(this.inputs);

      for (NodeWalker.Inner_DataUtil input : this.inputs) {
         end = NodeWalker.init(end, input, world);
      }

      return new PoolEntry(fitted, live, end, proof, this.takeoffTick + approach.size());
   }

   static TableIndex.Inner_StateCache solve(NodeWalker.Inner_CachePool world, BlockPos from, double surface, BlockPos to, double targetSurface, boolean sprint) {
      return solve(world, from, surface, to, targetSurface, sprint, true, false);
   }

   static TableIndex.Inner_StateCache solve(
      NodeWalker.Inner_CachePool world,
      BlockPos from,
      double surface,
      BlockPos to,
      double targetSurface,
      boolean sprint,
      boolean wideSearch,
      boolean limitedAirSearch
   ) {
      double cx = from.getX() + 0.5;
      double cz = from.getZ() + 0.5;
      float toward = NodeWalker.init_F_D(cx, cz, to.getX() + 0.5, to.getZ() + 0.5);
      List<Float> headings = headings(toward);
      if (wideSearch) {
         if (!TableIndex.init_Z_NodeWalker_CachePo(world, cx, surface, cz)) {
            TableIndex.Inner_StateCache narrow = narrowExit(world, from, surface, to, targetSurface, sprint);
            if (narrow != null) {
               return narrow;
            }
         }

         TableIndex.Inner_StateCache offset = offsetExit(world, from, surface, to, targetSurface, sprint, toward);
         if (offset != null) {
            return offset;
         }
      }

      TableIndex.Inner_StateCache best = null;

      for (float yaw : headings) {
         double r = Math.toRadians(yaw);
         double ux = -Math.sin(r);
         double uz = Math.cos(r);
         double back = backRoom(world, cx, cz, surface, ux, uz);
         double[] starts = wideSearch && back > 0.8 ? new double[]{0.0, 0.6, back} : (back > 0.3 ? new double[]{0.0, back} : new double[]{0.0});

         for (double distance : starts) {
            NodeWalker.Inner_StreamUtil start = new NodeWalker.Inner_StreamUtil(
               cx - ux * distance, surface, cz - uz * distance, 0.0, -0.0784, 0.0, true, 0, false, false
            );
            PoolEntry maneuver = search(world, start, from, to, targetSurface, yaw, sprint, limitedAirSearch);
            if (maneuver != null) {
               TableIndex.Inner_StateCache plan = plan(maneuver, yaw, sprint, distance);
               if (best == null || maneuver.margin() > best.margin()) {
                  best = plan;
               }

               if (maneuver.margin() >= 0.08) {
                  return plan;
               }
            }
         }
      }

      if (best != null && best.margin() >= -0.04) {
         return best;
      }

      if (!wideSearch) {
         return null;
      }

      if (targetSurface - surface > 0.6 && Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ()) >= 3.5) {
         TableIndex.Inner_StateCache rising = risingOffsetExit(world, from, surface, to, targetSurface, sprint, toward);
         if (rising != null) {
            return rising;
         }
      }

      return solveAroundWall(world, from, surface, to, targetSurface, sprint, toward, headings, limitedAirSearch);
   }

   private static TableIndex.Inner_StateCache offsetExit(
      NodeWalker.Inner_CachePool world, BlockPos from, double surface, BlockPos to, double targetSurface, boolean sprint, float toward
   ) {
      float yaw = Math.round(toward / 90.0F) * 90.0F;
      float turn = Mth.wrapDegrees(toward - yaw);
      if (!(Math.abs(turn) < 8.0F) && !(Math.abs(turn) > 40.0F)) {
         int side = turn < 0.0F ? 1 : -1;
         NodeWalker.Inner_StreamUtil start = NodeWalker.Inner_StreamUtil.standing(from.getX() + 0.5, surface, from.getZ() + 0.5);
         NodeWalker.Inner_StreamUtil state = start;
         ArrayList<NodeWalker.Inner_DataUtil> prefix = new ArrayList<>();

         for (int run = 0; run <= 3; run++) {
            PoolEntry.Inner_ConfigStore launch = prepareLaunch(world, state, yaw, yaw, sprint);
            if (launch != null) {
               for (int delay : new int[]{3, 5, 7, 1}) {
                  for (int release : new int[]{7, 9, 11, 2147483647, 5}) {
                     PoolEntry result = flight(world, start, prefix, to, targetSurface, launch, yaw, sprint, 0, release, side, delay);
                     if (result != null) {
                        return plan(result, yaw, sprint, 0.0);
                     }
                  }
               }
            }

            NodeWalker.Inner_DataUtil drive = NodeWalker.Inner_DataUtil.run(yaw, sprint);
            NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, drive, world);
            if (!next.onGround() || next.horizontalCollision() || NodeWalker.load(world, state, next)) {
               break;
            }

            prefix.add(drive);
            state = next;
         }

         return null;
      } else {
         return null;
      }
   }

   private static TableIndex.Inner_StateCache narrowExit(
      NodeWalker.Inner_CachePool world, BlockPos from, double surface, BlockPos to, double targetSurface, boolean sprint
   ) {
      ArrayList<TableIndex.Inner_SyncManager> starts = new ArrayList<>(TableIndex.init_List(world, from, surface));
      boolean rising = targetSurface > surface + 0.6;
      boolean hasRunUp = rising || from.distSqr(to) >= 7.0;
      if (rising) {
         for (double[] stance : new double[][]{{0.3, 0.06}, {0.34, 0.18}}) {
            for (int along : new int[]{-1, 1}) {
               for (int across : new int[]{-1, 1}) {
                  for (int axis = 0; axis < 2; axis++) {
                     double a = along * stance[0];
                     double b = across * stance[1];
                     double x = from.getX() + 0.5 + (axis == 0 ? a : b);
                     double z = from.getZ() + 0.5 + (axis == 0 ? b : a);
                     if (TableIndex.init_Z_NodeWalker_CachePo(world, x, surface, z)) {
                        starts.add(new TableIndex.Inner_SyncManager(x, z));
                     }
                  }
               }
            }
         }
      }

      if (hasRunUp) {
         addPanelEdges(world, from, surface, starts);
      }

      for (TableIndex.Inner_SyncManager stand : starts) {
         if (Thread.currentThread().isInterrupted()) {
            return null;
         }

         NodeWalker.Inner_StreamUtil start = NodeWalker.Inner_StreamUtil.standing(stand.x(), surface, stand.z());
         float direct = NodeWalker.init_F_D(start.x(), start.z(), to.getX() + 0.5, to.getZ() + 0.5);

         for (float offset : new float[]{-16.0F, 16.0F, -20.0F, 20.0F, -24.0F, 24.0F, -28.0F, 28.0F, -33.0F, 33.0F, -8.0F, 8.0F, -4.0F, 4.0F, 0.0F}) {
            float yaw = Math.round(direct + offset);
            int side = offset < 0.0F ? -1 : 1;
            NodeWalker.Inner_StreamUtil state = start;
            ArrayList<NodeWalker.Inner_DataUtil> prefix = new ArrayList<>();
            NodeWalker.Inner_DataUtil drive = NodeWalker.Inner_DataUtil.run(yaw, sprint);

            for (int run = 0; run <= (hasRunUp ? 4 : 0); run++) {
               for (boolean jumpSprint : rising && sprint ? new boolean[]{true, false} : new boolean[]{sprint}) {
                  PoolEntry.Inner_ConfigStore launch = prepareLaunch(world, state, yaw, yaw, jumpSprint);
                  if (launch != null) {
                     int[] releases = rising ? new int[]{0, 2, 4, 6, 8, 2147483647} : new int[]{11, 8, 5, 2147483647};

                     for (int delay : new int[]{2, 4, 0, 6, 8}) {
                        PoolEntry.Inner_BufferHelper held = new PoolEntry.Inner_BufferHelper();
                        PoolEntry heldResult = flight(world, start, prefix, to, targetSurface, launch, yaw, jumpSprint, 0, 2147483647, side, delay, held);

                        for (int release : releases) {
                           PoolEntry result = release == 2147483647
                              ? heldResult
                              : flight(world, start, prefix, to, targetSurface, launch, yaw, jumpSprint, 0, release, side, delay, held);
                           if (result != null) {
                              return plan(result, yaw, sprint, Math.hypot(stand.x() - from.getX() - 0.5, stand.z() - from.getZ() - 0.5));
                           }
                        }
                     }
                  }
               }

               NodeWalker.Inner_StreamUtil step = NodeWalker.init(state, drive, world);
               if (!step.onGround() || step.horizontalCollision() || Math.abs(step.y() - surface) > 0.02 || NodeWalker.load(world, state, step)) {
                  break;
               }

               prefix.add(drive);
               state = step;
            }
         }
      }

      return sprint && rising && from.distSqr(to) >= 9.0 ? MapStore.init(world, from, surface, to, targetSurface) : null;
   }

   private static void addPanelEdges(NodeWalker.Inner_CachePool world, BlockPos from, double surface, List<TableIndex.Inner_SyncManager> starts) {
      double x = from.getX();
      double z = from.getZ();
      AABB floor = new AABB(x, surface - 0.02, z, x + 1.0, surface - 0.001, z + 1.0);

      for (AABB box : world.init_List(floor)) {
         if (!(Math.abs(box.maxY - surface) > 0.01)) {
            for (int axis = 0; axis < 2; axis++) {
               double width = axis == 0 ? box.getXsize() : box.getZsize();
               double length = axis == 0 ? box.getZsize() : box.getXsize();
               if (!(width > 0.25) && !(length < 0.75)) {
                  double lo = axis == 0 ? box.minX : box.minZ;
                  double hi = axis == 0 ? box.maxX : box.maxZ;
                  double alongBase = axis == 0 ? z : x;

                  for (double overhang : new double[]{0.288, 0.225}) {
                     for (double thin : new double[]{lo - overhang, hi + overhang}) {
                        if (!(thin < (axis == 0 ? x : z)) && !(thin >= (axis == 0 ? x : z) + 1.0)) {
                           for (double along : new double[]{0.44, 0.56, -0.265, 1.265}) {
                              double px = axis == 0 ? thin : alongBase + along;
                              double pz = axis == 0 ? alongBase + along : thin;
                              if (TableIndex.init_Z_NodeWalker_CachePo(world, px, surface, pz)) {
                                 starts.add(new TableIndex.Inner_SyncManager(px, pz));
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static TableIndex.Inner_StateCache plan(PoolEntry maneuver, float yaw, boolean sprint, double setupDistance) {
      NodeWalker.Inner_StreamUtil start = maneuver.start();
      NodeWalker.Inner_StreamUtil end = maneuver.landing();
      return new TableIndex.Inner_StateCache(
         yaw,
         maneuver.takeoffTick(),
         sprint,
         2147483647,
         maneuver.inputs().size() - maneuver.takeoffTick(),
         start.x(),
         start.z(),
         end.x(),
         end.y(),
         end.z(),
         end.speed(),
         maneuver.margin(),
         setupDistance,
         0.0,
         maneuver
      );
   }

   static TableIndex.Inner_StateCache lowClearance(
      NodeWalker.Inner_CachePool world, BlockPos from, double fromY, BlockPos to, double toY, boolean startsCrouched, boolean endsCrouched, boolean sprint
   ) {
      double x = from.getX() + 0.5;
      double z = from.getZ() + 0.5;
      NodeWalker.Inner_StreamUtil start = new NodeWalker.Inner_StreamUtil(
         x, fromY, z, 0.0, -0.0784, 0.0, true, 0, false, false, -9223372036854775808L, false, startsCrouched ? 1 : 0
      );
      if (!world.init_List(start.box(false)).isEmpty()) {
         return null;
      }

      float toward = NodeWalker.init_F_D(x, z, to.getX() + 0.5, to.getZ() + 0.5);

      for (float offset : new float[]{0.0F, -10.0F, 10.0F}) {
         float yaw = toward + offset;
         ArrayList<NodeWalker.Inner_DataUtil> inputs = new ArrayList<>();
         NodeWalker.Inner_StreamUtil state = start;

         for (int tick = 0; tick < 36; tick++) {
            NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(
               endsCrouched && tick == 0 ? 0.0 : 1.0, 0.0, false, !endsCrouched && sprint, endsCrouched, yaw
            );
            NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, input, world);
            if (NodeWalker.load(world, state, next) || TableIndex.init(world, next) || next.y() < toY - 0.6) {
               break;
            }

            inputs.add(input);
            state = next;
            if (TableIndex.init(state)
               && !(Math.abs(state.y() - toY) > 0.02)
               && (int)Math.floor(state.x()) == to.getX()
               && (int)Math.floor(state.z()) == to.getZ()) {
               double margin = TableIndex.init_D_NodeWalker_CachePo_2(world, state, to, toY, yaw);
               if (margin >= -0.04) {
                  return plan(new PoolEntry(inputs, start, state, margin, inputs.size()), yaw, sprint && !endsCrouched, 0.0);
               }
            }
         }
      }

      return null;
   }

   private static TableIndex.Inner_StateCache risingOffsetExit(
      NodeWalker.Inner_CachePool world, BlockPos from, double surface, BlockPos to, double targetSurface, boolean sprint, float toward
   ) {
      if (!sprint) {
         return null;
      }

      float primary = Math.round(toward / 90.0F) * 90.0F;
      float turn = Mth.wrapDegrees(toward - primary);
      if (!(Math.abs(turn) < 5.0F) && !(Math.abs(turn) > 40.0F)) {
         int side = turn < 0.0F ? -1 : 1;
         double radians = Math.toRadians(primary);
         double dx = -Math.sin(radians);
         double dz = Math.cos(radians);

         for (double lateral : new double[]{0.45, 0.35, 0.55}) {
            for (double back : new double[]{0.4, 0.48, 0.3}) {
               double x = from.getX() + 0.5 - dx * back + dz * lateral * side;
               double z = from.getZ() + 0.5 - dz * back - dx * lateral * side;
               if (TableIndex.init_Z_NodeWalker_CachePo(world, x, surface, z)) {
                  NodeWalker.Inner_StreamUtil initial = NodeWalker.Inner_StreamUtil.standing(x, surface, z);

                  for (float delta : new float[]{15.0F, 12.0F, 18.0F}) {
                     float yaw = Math.round(toward) + side * delta;
                     NodeWalker.Inner_DataUtil drive = NodeWalker.Inner_DataUtil.run(yaw, true);
                     NodeWalker.Inner_StreamUtil state = initial;
                     ArrayList<NodeWalker.Inner_DataUtil> prefix = new ArrayList<>();

                     for (int tick = 0; tick <= 9; tick++) {
                        if (state.speed() >= 0.13) {
                           PoolEntry.Inner_ConfigStore launch = prepareLaunch(world, state, yaw, yaw, true);
                           if (launch != null) {
                              for (int delay : new int[]{2, 4, 0}) {
                                 for (int release : new int[]{8, 7, 9}) {
                                    PoolEntry result = flight(world, initial, prefix, to, targetSurface, launch, yaw, true, 0, release, side, delay);
                                    if (result != null) {
                                       return plan(result, yaw, true, Math.hypot(lateral, back));
                                    }
                                 }
                              }
                           }
                        }

                        NodeWalker.Inner_StreamUtil step = NodeWalker.init(state, drive, world);
                        if (!step.onGround() || step.horizontalCollision() || Math.abs(step.y() - surface) > 0.02 || NodeWalker.load(world, state, step)) {
                           break;
                        }

                        prefix.add(drive);
                        state = step;
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

   private static TableIndex.Inner_StateCache solveAroundWall(
      NodeWalker.Inner_CachePool world,
      BlockPos from,
      double surface,
      BlockPos to,
      double targetSurface,
      boolean sprint,
      float toward,
      List<Float> headings,
      boolean limitedAirSearch
   ) {
      double radians = Math.toRadians(toward);
      double dx = -Math.sin(radians);
      double dz = Math.cos(radians);
      double cx = from.getX() + 0.5;
      double cz = from.getZ() + 0.5;
      AABB wall = new AABB(
         cx + Math.min(dx * 0.6, dx * 1.35) - 0.2,
         surface + 0.3,
         cz + Math.min(dz * 0.6, dz * 1.35) - 0.2,
         cx + Math.max(dx * 0.6, dx * 1.35) + 0.2,
         surface + 1.65,
         cz + Math.max(dz * 0.6, dz * 1.35) + 0.2
      );
      if (world.init_List(wall).isEmpty()) {
         return null;
      }

      for (double side : new double[]{-0.45, 0.45}) {
         for (double back : new double[]{0.25, 0.5}) {
            double x = cx - dx * back + dz * side;
            double z = cz - dz * back - dx * side;
            if (TableIndex.init_Z_NodeWalker_CachePo(world, x, surface, z)
               && TableIndex.init_Z_NodeWalker_CachePo(world, (x + cx) * 0.5, surface, (z + cz) * 0.5)) {
               NodeWalker.Inner_StreamUtil start = NodeWalker.Inner_StreamUtil.standing(x, surface, z);

               for (float yaw : headings) {
                  PoolEntry result = search(world, start, from, to, targetSurface, yaw, sprint, limitedAirSearch);
                  if (result != null) {
                     return plan(result, yaw, sprint, Math.hypot(side, back));
                  }
               }
            }
         }
      }

      return Math.abs(surface - targetSurface) <= 0.25 && from.distSqr(to) <= 9.0
         ? shoulderJump(world, from, surface, to, targetSurface, sprint, toward)
         : null;
   }

   private static PoolEntry recordFlight(
      NodeWalker.Inner_StreamUtil start,
      NodeWalker.Inner_StreamUtil next,
      NodeWalker.Inner_DataUtil input,
      PoolEntry.Inner_StreamUtil step,
      double margin,
      int run
   ) {
      ArrayDeque<NodeWalker.Inner_DataUtil> controls = new ArrayDeque<>();
      controls.add(input);

      for (PoolEntry.Inner_StreamUtil p = step; p != null; p = p.parent()) {
         controls.addFirst(p.input());
      }

      return new PoolEntry(List.copyOf(controls), start, next, margin, run);
   }

   private static List<PoolEntry.Inner_StreamUtil> retainFlight(List<PoolEntry.Inner_StreamUtil> nextLayer, double x, double z, int width) {
      nextLayer.sort(Comparator.comparingDouble(PoolEntry.Inner_StreamUtil::distance));
      HashSet<Long> distinct = new HashSet<>();
      ArrayList<PoolEntry.Inner_StreamUtil> retained = new ArrayList<>(width);

      for (PoolEntry.Inner_StreamUtil candidate : nextLayer) {
         NodeWalker.Inner_StreamUtil state = candidate.state();
         long bin = (Math.round((state.x() - x) * 40.0) & 511L) << 21
            | (Math.round((state.z() - z) * 40.0) & 511L) << 12
            | (Math.round(state.vx() * 80.0) & 63L) << 6
            | Math.round(state.vz() * 80.0) & 63L;
         if (distinct.add(bin)) {
            retained.add(candidate);
         }

         if (retained.size() == width) {
            break;
         }
      }

      return retained;
   }

   private static TableIndex.Inner_StateCache shoulderJump(
      NodeWalker.Inner_CachePool world, BlockPos from, double surface, BlockPos to, double targetSurface, boolean sprint, float toward
   ) {
      double radians = Math.toRadians(toward);
      double dx = -Math.sin(radians);
      double dz = Math.cos(radians);
      double cx = from.getX() + 0.5;
      double cz = from.getZ() + 0.5;

      for (double side : new double[]{-0.77, 0.77, -0.71, 0.71, -0.6, 0.6}) {
         for (double back : new double[]{0.25, 0.0, 0.45, 0.65, -0.25}) {
            double x = cx - dx * back + dz * side;
            double z = cz - dz * back - dx * side;
            if (TableIndex.init_Z_NodeWalker_CachePo(world, x, surface, z)
               && TableIndex.init_Z_NodeWalker_CachePo(world, (x + cx) * 0.5, surface, (z + cz) * 0.5)) {
               NodeWalker.Inner_StreamUtil start = NodeWalker.Inner_StreamUtil.standing(x, surface, z);

               for (float angle : new float[]{7.0F, 12.0F, 20.0F, 30.0F, 42.0F}) {
                  float yaw = toward - (float)Math.copySign(angle, side);
                  NodeWalker.Inner_StreamUtil state = start;
                  PoolEntry.Inner_StreamUtil approach = null;

                  for (int run = 0; run <= 3; run++) {
                     NodeWalker.Inner_DataUtil press = NodeWalker.Inner_DataUtil.jump(yaw, sprint);
                     NodeWalker.Inner_StreamUtil takeoff = NodeWalker.init(state, press, world);
                     if (!takeoff.onGround() && !NodeWalker.load(world, state, takeoff)) {
                        PoolEntry result = steerAroundWall(
                           world, start, new PoolEntry.Inner_StreamUtil(takeoff, press, approach, false, 0.0), from, to, targetSurface, yaw, sprint, side, run
                        );
                        if (result != null) {
                           return plan(result, yaw, sprint, Math.hypot(side, back));
                        }
                     }

                     if (run >= 3) {
                        break;
                     }

                     NodeWalker.Inner_DataUtil drive = NodeWalker.Inner_DataUtil.run(yaw, sprint);
                     NodeWalker.Inner_StreamUtil step = NodeWalker.init(state, drive, world);
                     if (!step.onGround() || step.horizontalCollision() || NodeWalker.load(world, state, step)) {
                        break;
                     }

                     approach = new PoolEntry.Inner_StreamUtil(step, drive, approach, false, 0.0);
                     state = step;
                  }
               }
            }
         }
      }

      return null;
   }

   private static PoolEntry steerAroundWall(
      NodeWalker.Inner_CachePool world,
      NodeWalker.Inner_StreamUtil start,
      PoolEntry.Inner_StreamUtil launch,
      BlockPos from,
      BlockPos target,
      double surface,
      float yaw,
      boolean sprint,
      double side,
      int run
   ) {
      List<PoolEntry.Inner_StreamUtil> layer = List.of(launch);
      double x = target.getX() + 0.5;
      double z = target.getZ() + 0.5;
      double dx = x - (from.getX() + 0.5);
      double dz = z - (from.getZ() + 0.5);
      double length = Math.hypot(dx, dz);
      double ux = dx / length;
      double uz = dz / length;

      for (int tick = 0; tick < 22; tick++) {
         ArrayList<PoolEntry.Inner_StreamUtil> nextLayer = new ArrayList<>();

         for (PoolEntry.Inner_StreamUtil step : layer) {
            for (int f = -1; f <= 1; f++) {
               for (int strafe = -1; strafe <= 1; strafe++) {
                  NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(f, strafe, false, sprint && f > 0 && !step.state().onGround(), false, yaw);
                  NodeWalker.Inner_StreamUtil next = NodeWalker.init(step.state(), input, world);
                  if (!(next.y() < surface - 0.08) && !NodeWalker.load(world, step.state(), next) && !TableIndex.init(world, next)) {
                     if (TableIndex.init(next)) {
                        if (Math.abs(next.y() - surface) > 0.02) {
                           continue;
                        }

                        double margin = TableIndex.init_D_NodeWalker_CachePo_2(world, next, target, surface, yaw);
                        if (margin >= -0.04) {
                           return recordFlight(start, next, input, step, margin, run);
                        }

                        if (Math.hypot(x - next.x(), z - next.z()) > 1.35) {
                           continue;
                        }
                     }

                     double gx = x - next.x();
                     double gz = z - next.z();
                     double remaining = gx * ux + gz * uz;
                     boolean cleared = step.cleared() || remaining < 0.13;
                     double clearance = cleared ? 0.0 : Math.copySign(0.83, side);
                     double lateral = gx * uz - gz * ux + clearance;
                     double error = Math.hypot(remaining - 0.8 * (next.vx() * ux + next.vz() * uz), lateral * 1.6)
                        + (cleared ? 0.0 : 1.4)
                        + (next.horizontalCollision() ? 0.12 : 0.0);
                     nextLayer.add(new PoolEntry.Inner_StreamUtil(next, input, step, cleared, error));
                  }
               }
            }
         }

         if (nextLayer.isEmpty()) {
            break;
         }

         layer = retainFlight(nextLayer, x, z, 16);
      }

      return null;
   }

   static PoolEntry live(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil start, BlockPos from, BlockPos to, double surface, float runYaw, boolean sprint
   ) {
      return search(world, start, from, to, surface, runYaw, sprint, false);
   }

   private static List<Float> headings(float toward) {
      ArrayList<Float> headings = new ArrayList<>();

      for (int i = 0; i < 8; i++) {
         float yaw = i * 45.0F;
         if (Math.abs(Mth.wrapDegrees(yaw - toward)) <= 76.0F) {
            headings.add(yaw);
         }
      }

      headings.sort(Comparator.comparingDouble(yawx -> Math.abs(Mth.wrapDegrees(yawx - toward))));
      return headings;
   }

   private static double backRoom(NodeWalker.Inner_CachePool world, double x, double z, double y, double ux, double uz) {
      double back = 0.0;

      for (double d = 0.1; d <= 4.000001 && TableIndex.init_Z_NodeWalker_CachePo(world, x - ux * d, y, z - uz * d); d += 0.1) {
         NodeWalker.Inner_StreamUtil a = new NodeWalker.Inner_StreamUtil(x - ux * back, y, z - uz * back, 0.0, 0.0, 0.0, true, 0, false, false);
         NodeWalker.Inner_StreamUtil b = new NodeWalker.Inner_StreamUtil(x - ux * d, y, z - uz * d, 0.0, 0.0, 0.0, true, 0, false, false);
         if (NodeWalker.load(world, a, b)) {
            break;
         }

         back = d;
      }

      return Math.max(0.0, back - 0.1);
   }

   private static PoolEntry search(
      NodeWalker.Inner_CachePool world,
      NodeWalker.Inner_StreamUtil start,
      BlockPos from,
      BlockPos to,
      double surface,
      float runYaw,
      boolean sprint,
      boolean limitedAirSearch
   ) {
      PoolEntry best = null;
      PoolEntry.Inner_CachePool transitions = new PoolEntry.Inner_CachePool();
      float[] launchTurns = limitedAirSearch && surface < start.y() - 0.01 ? DROP_LAUNCH_TURNS : LAUNCH_TURNS;
      float[] airOffsets = limitedAirSearch ? COMMON_AIR_OFFSETS : AIR_OFFSETS;
      int[] releases = limitedAirSearch ? COMMON_RELEASES : RELEASES;
      float delayedTurnThreshold = limitedAirSearch ? 20.0F : 0.0F;

      for (boolean hops : sprint ? new boolean[]{false, true} : new boolean[]{false}) {
         NodeWalker.Inner_StreamUtil state = start;
         ArrayList<NodeWalker.Inner_DataUtil> prefix = new ArrayList<>();

         for (int tick = 0; tick <= 48; tick++) {
            if (Thread.currentThread().isInterrupted()) {
               return null;
            }

            if (state.onGround() && state.jumpDelay() == 0 && Math.hypot(state.x() - (from.getX() + 0.5), state.z() - (from.getZ() + 0.5)) < 1.25) {
               float targetYaw = NodeWalker.init_F_D(state.x(), state.z(), to.getX() + 0.5, to.getZ() + 0.5);
               HashMap<Float, PoolEntry.Inner_ConfigStore> launches = new HashMap<>();

               for (float launch : launchTurns) {
                  for (float offset : airOffsets) {
                     float airYaw = targetYaw + offset;
                     if (!(Math.abs(Mth.wrapDegrees(airYaw - runYaw)) > 100.0F)) {
                        float launchYaw = runYaw + Mth.wrapDegrees(airYaw - runYaw) * launch;
                        if (!launches.containsKey(launchYaw)) {
                           launches.put(launchYaw, prepareLaunch(world, state, runYaw, launchYaw, sprint));
                        }

                        PoolEntry.Inner_ConfigStore prepared = launches.get(launchYaw);
                        if (prepared != null) {
                           int maxDelay = Math.abs(Mth.wrapDegrees(airYaw - prepared.yaw())) <= delayedTurnThreshold ? 0 : 2;

                           for (int delay = 0; delay <= maxDelay; delay += 2) {
                              PoolEntry.Inner_BufferHelper[] held = new PoolEntry.Inner_BufferHelper[STRAFES.length];

                              for (int release : releases) {
                                 for (int side = 0; side < STRAFES.length; side++) {
                                    if (held[side] == null) {
                                       held[side] = new PoolEntry.Inner_BufferHelper();
                                    }

                                    PoolEntry arc = flight(
                                       world,
                                       start,
                                       prefix,
                                       to,
                                       surface,
                                       prepared,
                                       airYaw,
                                       sprint,
                                       delay,
                                       release,
                                       STRAFES[side],
                                       0,
                                       held[side],
                                       transitions,
                                       limitedAirSearch
                                    );
                                    if (arc != null) {
                                       if (best == null || arc.margin() > best.margin()) {
                                          best = arc;
                                       }

                                       if (best.margin() >= 0.08) {
                                          return best;
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(1.0, 0.0, hops && state.onGround(), sprint, false, runYaw);
            NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, input, world);
            if (NodeWalker.load(world, state, next)
               || TableIndex.init(world, next)
               || next.y() < start.y() - 0.02
               || next.horizontalCollision()
               || !hops && !next.onGround()) {
               break;
            }

            prefix.add(input);
            state = next;
         }
      }

      return best;
   }

   private static PoolEntry.Inner_ConfigStore prepareLaunch(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil takeoff, float corridorYaw, float launchYaw, boolean sprint
   ) {
      if (TableIndex.init(world, takeoff)) {
         return null;
      }

      ArrayList<NodeWalker.Inner_DataUtil> controls = new ArrayList<>();
      NodeWalker.Inner_StreamUtil state = takeoff;
      float camera = corridorYaw;
      if (Math.abs(Mth.wrapDegrees(launchYaw - corridorYaw)) > 60.0F) {
         for (int align = 0; Math.abs(Mth.wrapDegrees(launchYaw - camera)) > 1.0F && align < 8; align++) {
            float controlYaw = state.speed() > 0.03 ? camera : launchYaw;
            float nextCamera = camera
               + DataUtil.init_F_F_DataUtil_DataUtil(Mth.wrapDegrees(controlYaw - camera), DataUtil.Inner_DataUtil.load_DataUtil_DataUtil);
            NodeWalker.Inner_DataUtil input = settleInput(world, state, controlYaw).withYaw(nextCamera);
            NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, input, world);
            if (NodeWalker.load(world, state, next) || TableIndex.init(world, next) || !next.onGround() || Math.abs(next.y() - takeoff.y()) > 0.02) {
               return null;
            }

            controls.add(input);
            state = next;
            camera = nextCamera;
         }

         if (Math.abs(Mth.wrapDegrees(launchYaw - camera)) > 1.0F) {
            return null;
         }
      }

      NodeWalker.Inner_DataUtil press = NodeWalker.Inner_DataUtil.jump(launchYaw, sprint);
      NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, press, world);
      if (!NodeWalker.load(world, state, next) && !TableIndex.init(world, next) && !TableIndex.init(next)) {
         controls.add(press);
         return new PoolEntry.Inner_ConfigStore(next, controls, Math.max(next.y(), takeoff.y()), launchYaw);
      } else {
         return null;
      }
   }

   private static PoolEntry flight(
      NodeWalker.Inner_CachePool world,
      NodeWalker.Inner_StreamUtil initial,
      List<NodeWalker.Inner_DataUtil> prefix,
      BlockPos to,
      double surface,
      PoolEntry.Inner_ConfigStore launch,
      float airYaw,
      boolean sprint,
      int turnDelay,
      int release,
      int strafe,
      int strafeDelay
   ) {
      return flight(world, initial, prefix, to, surface, launch, airYaw, sprint, turnDelay, release, strafe, strafeDelay, null);
   }

   private static PoolEntry flight(
      NodeWalker.Inner_CachePool world,
      NodeWalker.Inner_StreamUtil initial,
      List<NodeWalker.Inner_DataUtil> prefix,
      BlockPos to,
      double surface,
      PoolEntry.Inner_ConfigStore launch,
      float airYaw,
      boolean sprint,
      int turnDelay,
      int release,
      int strafe,
      int strafeDelay,
      PoolEntry.Inner_BufferHelper shared
   ) {
      return flight(world, initial, prefix, to, surface, launch, airYaw, sprint, turnDelay, release, strafe, strafeDelay, shared, null, false);
   }

   private static PoolEntry flight(
      NodeWalker.Inner_CachePool world,
      NodeWalker.Inner_StreamUtil initial,
      List<NodeWalker.Inner_DataUtil> prefix,
      BlockPos to,
      double surface,
      PoolEntry.Inner_ConfigStore launch,
      float airYaw,
      boolean sprint,
      int turnDelay,
      int release,
      int strafe,
      int strafeDelay,
      PoolEntry.Inner_BufferHelper shared,
      PoolEntry.Inner_CachePool transitions,
      boolean limitedAirSearch
   ) {
      boolean recording = shared != null && release == 2147483647;
      if (shared != null && !recording && release >= shared.init_I) {
         return shared.init_PoolEntry;
      }

      PoolEntry result = simulateFlight(
         world, initial, prefix, to, surface, launch, airYaw, sprint, turnDelay, release, strafe, strafeDelay, shared, recording, transitions, limitedAirSearch
      );
      if (recording) {
         shared.init_PoolEntry = result;
      }

      return result;
   }

   private static PoolEntry simulateFlight(
      NodeWalker.Inner_CachePool world,
      NodeWalker.Inner_StreamUtil initial,
      List<NodeWalker.Inner_DataUtil> prefix,
      BlockPos to,
      double surface,
      PoolEntry.Inner_ConfigStore launch,
      float airYaw,
      boolean sprint,
      int turnDelay,
      int release,
      int strafe,
      int strafeDelay,
      PoolEntry.Inner_BufferHelper shared,
      boolean recording,
      PoolEntry.Inner_CachePool transitions,
      boolean limitedAirSearch
   ) {
      NodeWalker.Inner_StreamUtil state = launch.state();
      double peak = launch.peak();
      float yaw = launch.yaw();
      int settleTicks = 0;
      int matched = shared != null && !recording ? Math.min(release, shared.init_List.size()) : 0;
      if (matched > 0) {
         PoolEntry.Inner_DataUtil frame = shared.init_List.get(matched - 1);
         state = frame.state();
         peak = frame.peak();
         yaw = frame.yaw();
      }

      for (int tick = matched + 1; tick <= 40; tick++) {
         if (recording) {
            shared.init_I = tick;
         }

         if (tick > turnDelay) {
            yaw += Mth.clamp(Mth.wrapDegrees(airYaw - yaw), -30.0F, 30.0F);
         }

         NodeWalker.Inner_DataUtil input = flightInput(tick, yaw, sprint, release, strafe, strafeDelay);
         NodeWalker.Inner_StreamUtil next = transitions != null && tick <= 8
            ? transitions.init(world, state, input, surface)
            : flightStep(world, state, input, surface);
         if (next == null || limitedAirSearch && next.vy() < 0.0 && next.y() < surface - 0.6 - 0.01) {
            return null;
         }

         state = next;
         peak = Math.max(peak, state.y());
         if (!TableIndex.init(state)) {
            if (recording && shared.init_List.size() == tick - 1 && tick <= 12) {
               shared.init_List.add(new PoolEntry.Inner_DataUtil(state, yaw, peak));
            }
         } else {
            if (Math.abs(state.y() - surface) > 0.02 || peak - state.y() > world.init_D() + 1.0E-5) {
               return null;
            }

            double margin = TableIndex.init_D_NodeWalker_CachePo_2(world, state, to, surface, yaw);
            if (!(margin < -0.04)) {
               ArrayList<NodeWalker.Inner_DataUtil> controls = new ArrayList<>(prefix.size() + launch.controls().size() + tick + 4);
               controls.addAll(prefix);
               controls.addAll(launch.controls());
               float recordedYaw = launch.yaw();

               for (int i = 1; i <= tick; i++) {
                  if (i > turnDelay) {
                     recordedYaw += Mth.clamp(Mth.wrapDegrees(airYaw - recordedYaw), -30.0F, 30.0F);
                  }

                  controls.add(flightInput(i, recordedYaw, sprint, release, strafe, strafeDelay));
               }

               if (TableIndex.init_D_NodeWalker_CachePo(world, state.x(), state.z(), to, surface) < -0.04) {
                  NodeWalker.Inner_DataUtil coast = new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, yaw);
                  NodeWalker.Inner_StreamUtil slide = state;

                  for (int t = 1; t <= 6; t++) {
                     NodeWalker.Inner_StreamUtil nextSlide = NodeWalker.init(slide, coast, world);
                     if (!TableIndex.init(nextSlide) || Math.abs(nextSlide.y() - surface) > 0.02 || NodeWalker.load(world, slide, nextSlide)) {
                        break;
                     }

                     slide = nextSlide;
                     if (TableIndex.init_D_NodeWalker_CachePo(world, slide.x(), slide.z(), to, surface) >= -0.04
                        && TableIndex.init_D_NodeWalker_CachePo_2(world, slide, to, surface, yaw) >= -0.04) {
                        for (int i = 0; i < t; i++) {
                           controls.add(coast);
                        }

                        state = slide;
                        break;
                     }
                  }
               }

               for (int settle = 0; settle < 10 && TableIndex.init_D_NodeWalker_CachePo(world, state.x(), state.z(), to, surface) < -0.04; settle++) {
                  float inward = NodeWalker.init_F_D(state.x(), state.z(), to.getX() + 0.5, to.getZ() + 0.5);
                  yaw += Mth.clamp(Mth.wrapDegrees(inward - yaw), -30.0F, 30.0F);
                  NodeWalker.Inner_DataUtil enter = new NodeWalker.Inner_DataUtil(1.0, 0.0, false, false, false, yaw);
                  NodeWalker.Inner_StreamUtil entered = NodeWalker.init(state, enter, world);
                  if (!TableIndex.init(entered)
                     || Math.abs(entered.y() - surface) > 0.02
                     || NodeWalker.load(world, state, entered)
                     || TableIndex.init(world, entered)) {
                     return null;
                  }

                  controls.add(enter);
                  state = entered;
               }

               if (TableIndex.init_D_NodeWalker_CachePo(world, state.x(), state.z(), to, surface) < -0.04) {
                  return null;
               }

               margin = TableIndex.init_D_NodeWalker_CachePo_2(world, state, to, surface, yaw);
               if (margin < -0.04) {
                  return null;
               }

               return new PoolEntry(controls, initial, state, margin, prefix.size() + launch.controls().size() - 1);
            }

            double touchdownMargin = TableIndex.init_D_NodeWalker_CachePo(world, state.x(), state.z(), to, surface);
            if (!(touchdownMargin >= -0.6) || ++settleTicks > 4) {
               return null;
            }
         }
      }

      return null;
   }

   private static NodeWalker.Inner_DataUtil flightInput(int tick, float yaw, boolean sprint, int release, int strafe, int strafeDelay) {
      boolean forward = tick <= release;
      return new NodeWalker.Inner_DataUtil(forward ? 1.0 : 0.0, tick > strafeDelay ? strafe : 0.0, false, forward && sprint, false, yaw);
   }

   private static NodeWalker.Inner_StreamUtil flightStep(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, NodeWalker.Inner_DataUtil input, double surface
   ) {
      NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, input, world);
      return !(next.y() < surface - 4.0) && !NodeWalker.load(world, state, next) && !TableIndex.init(world, next) ? next : null;
   }

   private static final class Inner_BufferHelper {
      final List<PoolEntry.Inner_DataUtil> init_List = new ArrayList<>(12);
      int init_I;
      PoolEntry init_PoolEntry;
   }

   private static final class Inner_CachePool {
      private static final int init_I = 2048;
      private final NodeWalker.Inner_StreamUtil[] init_NodeWalker_StreamUArr = new NodeWalker.Inner_StreamUtil[2048];
      private final NodeWalker.Inner_StreamUtil[] load = new NodeWalker.Inner_StreamUtil[2048];
      private final long[] init_JArr = new long[2048];

      NodeWalker.Inner_StreamUtil init(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, NodeWalker.Inner_DataUtil input, double surface) {
         long key = (long)Float.floatToIntBits(input.yaw()) << 8
            | (long)(input.strafe() + 1.0) << 2
            | (input.forward() > 0.0 ? 2 : 0)
            | (input.sprint() ? 1 : 0);
         int slot = (int)HashCommon.mix(key ^ System.identityHashCode(state)) & 2047;
         if (this.init_NodeWalker_StreamUArr[slot] == state && this.init_JArr[slot] == key) {
            return this.load[slot];
         }

         NodeWalker.Inner_StreamUtil next = PoolEntry.flightStep(world, state, input, surface);
         this.init_NodeWalker_StreamUArr[slot] = state;
         this.load[slot] = next;
         this.init_JArr[slot] = key;
         return next;
      }
   }

   private record Inner_ConfigStore(NodeWalker.Inner_StreamUtil state, List<NodeWalker.Inner_DataUtil> controls, double peak, float yaw) {
   }

   private record Inner_DataUtil(NodeWalker.Inner_StreamUtil state, float yaw, double peak) {
   }

   private record Inner_StreamUtil(
      NodeWalker.Inner_StreamUtil state, NodeWalker.Inner_DataUtil input, PoolEntry.Inner_StreamUtil parent, boolean cleared, double distance
   ) {
   }

   private record Inner_TaskQueue(NodeWalker.Inner_StreamUtil state, List<NodeWalker.Inner_DataUtil> prefix, double distance) {
   }
}
