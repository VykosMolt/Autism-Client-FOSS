package autismclient.ai.a;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public final class TableIndex {
   public static final int init_I = 12;
   public static final int load_I = 40;
   public static final int read_I = 100;
   static final int write_I = 40;
   static final double init_D = 4.0;
   public static final double load_D = -0.12;
   public static final double read_D = -0.04;
   public static final double write_D = 0.55;
   public static final int update_I = 2147483647;
   public static final double update_D = 0.28;
   public static final double sync = 0.14;
   private static final int[] init_IArr = new int[]{2147483647, 1, 3, 5, 7, 9};
   private static final int[] load_IArr = new int[]{2147483647, 3, 6};
   private static final ConcurrentHashMap<Integer, Double> init_ConcurrentHashMap = new ConcurrentHashMap<>();

   private TableIndex() {
   }

   public static TableIndex.Inner_StateCache init_TableIndex_StateCa_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, BlockPos from, double fromSurface, BlockPos to, double toSurface, double approachSpeed, boolean allowSprint
   ) {
      return init_TableIndex_StateCa_NodeWalker_CachePo(world, from, fromSurface, to, toSurface, approachSpeed, allowSprint, true);
   }

   static TableIndex.Inner_StateCache init_TableIndex_StateCa_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world,
      BlockPos from,
      double fromSurface,
      BlockPos to,
      double toSurface,
      double approachSpeed,
      boolean allowSprint,
      boolean staged
   ) {
      return init(world, from, fromSurface, to, toSurface, approachSpeed, allowSprint, staged, true);
   }

   static TableIndex.Inner_StateCache init(
      NodeWalker.Inner_CachePool world,
      BlockPos from,
      double fromSurface,
      BlockPos to,
      double toSurface,
      double approachSpeed,
      boolean allowSprint,
      boolean staged,
      boolean wideSearch
   ) {
      return init(world, from, fromSurface, to, toSurface, approachSpeed, allowSprint, staged, wideSearch, false);
   }

   static TableIndex.Inner_StateCache init(
      NodeWalker.Inner_CachePool world,
      BlockPos from,
      double fromSurface,
      BlockPos to,
      double toSurface,
      double approachSpeed,
      boolean allowSprint,
      boolean staged,
      boolean wideSearch,
      boolean limitedAirSearch
   ) {
      return init(world, from, fromSurface, to, toSurface, approachSpeed, allowSprint, staged, wideSearch, limitedAirSearch, false);
   }

   static TableIndex.Inner_StateCache init(
      NodeWalker.Inner_CachePool world,
      BlockPos from,
      double fromSurface,
      BlockPos to,
      double toSurface,
      double approachSpeed,
      boolean allowSprint,
      boolean staged,
      boolean wideSearch,
      boolean limitedAirSearch,
      boolean quickRollout
   ) {
      double fx = from.getX() + 0.5;
      double fz = from.getZ() + 0.5;
      double tx = to.getX() + 0.5;
      double tz = to.getZ() + 0.5;
      double dx = tx - fx;
      double dz = tz - fz;
      double dist = Math.hypot(dx, dz);
      if (dist < 1.0E-6) {
         return null;
      }

      if (staged && wideSearch && !init_Z_NodeWalker_CachePo(world, fx, fromSurface, fz)) {
         TableIndex.Inner_StateCache offset = init_TableIndex_StateCa_NodeWalker_CachePo(world, from, fromSurface, to, toSurface, allowSprint);
         return offset != null ? offset : PoolEntry.solve(world, from, fromSurface, to, toSurface, allowSprint, true, limitedAirSearch);
      }

      double ux = dx / dist;
      double uz = dz / dist;
      double backX = approachSpeed > 0.0 ? fx - ux * 0.2 : fx;
      double backZ = approachSpeed > 0.0 ? fz - uz * 0.2 : fz;
      TableIndex.Inner_StateCache best = init(world, backX, backZ, fromSurface, to, toSurface, ux, uz, approachSpeed, allowSprint, 0.0, quickRollout);
      if (approachSpeed > 0.0 && (best == null || best.margin() < 0.14)) {
         TableIndex.Inner_StateCache rested = init(world, from, fromSurface, to, toSurface, 0.0, allowSprint, false, false, false, quickRollout);
         if (rested != null && (best == null || best.margin() < -0.04 || rested.margin() > best.margin() + 0.04)) {
            best = rested;
         }
      }

      if ((best == null || best.margin() < 0.0) && approachSpeed <= 0.0) {
         double back = init_D_NodeWalker_CachePo(world, fx, fz, fromSurface, ux, uz);
         if (back >= 0.25) {
            TableIndex.Inner_StateCache backed = init(
               world, fx - ux * back, fz - uz * back, fromSurface, to, toSurface, ux, uz, 0.0, allowSprint, back, quickRollout
            );
            if (backed != null && (best == null || init(backed, best))) {
               best = backed;
            }
         }
      }

      if (staged && (best == null || best.margin() < -0.04)) {
         if (wideSearch) {
            TableIndex.Inner_StateCache offset = init_TableIndex_StateCa_NodeWalker_CachePo(world, from, fromSurface, to, toSurface, allowSprint);
            if (offset != null) {
               return offset;
            }
         }

         TableIndex.Inner_StateCache maneuver = PoolEntry.solve(world, from, fromSurface, to, toSurface, allowSprint, wideSearch, limitedAirSearch);
         if (maneuver != null && (best == null || maneuver.margin() > best.margin())) {
            return maneuver;
         }
      }

      return best;
   }

   private static TableIndex.Inner_StateCache init(
      NodeWalker.Inner_CachePool world,
      double startX,
      double startZ,
      double fromSurface,
      BlockPos to,
      double toSurface,
      double ux,
      double uz,
      double approachSpeed,
      boolean allowSprint,
      double backup,
      boolean quickRollout
   ) {
      return init(world, startX, startZ, fromSurface, to, toSurface, to.getX() + 0.5, to.getZ() + 0.5, ux, uz, approachSpeed, allowSprint, backup, quickRollout);
   }

   private static TableIndex.Inner_StateCache init(
      NodeWalker.Inner_CachePool world,
      double startX,
      double startZ,
      double fromSurface,
      BlockPos to,
      double toSurface,
      double tx,
      double tz,
      double ux,
      double uz,
      double approachSpeed,
      boolean allowSprint,
      double backup,
      boolean quickRollout
   ) {
      if (!init_Z_NodeWalker_CachePo(world, startX, fromSurface, startZ)) {
         return null;
      }

      float yaw = NodeWalker.init_F_D(startX, startZ, tx, tz);
      TableIndex.Inner_NetHelper line = new TableIndex.Inner_NetHelper(startX, startZ, ux, uz, Math.hypot(tx - startX, tz - startZ));
      TableIndex.Inner_StateCache best = null;

      for (boolean sprint : allowSprint ? new boolean[]{true, false} : new boolean[]{false}) {
         NodeWalker.Inner_StreamUtil start = new NodeWalker.Inner_StreamUtil(
            startX, fromSurface, startZ, ux * approachSpeed, -0.0784, uz * approachSpeed, true, 0, false, false
         );
         NodeWalker.Inner_DataUtil run = NodeWalker.Inner_DataUtil.run(yaw, sprint);
         TableIndex.Inner_CachePool b = init(world, init_List_NodeWalker_CachePo(world, start, run, 12), run, run, line, to, toSurface, false, quickRollout);
         if (b != null && !(b.arc().margin() < -0.12)) {
            TableIndex.Inner_StreamUtil a = b.arc();
            TableIndex.Inner_StateCache candidate = new TableIndex.Inner_StateCache(
               yaw,
               b.runTicks(),
               sprint,
               a.coastAfter(),
               a.ticks(),
               startX,
               startZ,
               a.end().x(),
               a.end().y(),
               a.end().z(),
               a.end().speed(),
               a.margin(),
               backup,
               approachSpeed
            );
            if (best == null || init(candidate, best)) {
               best = candidate;
            }

            if (sprint && candidate.margin() >= 0.0) {
               break;
            }
         }
      }

      return best;
   }

   static List<TableIndex.Inner_SyncManager> init_List(NodeWalker.Inner_CachePool world, BlockPos cell, double y) {
      double x = cell.getX() + 0.5;
      double z = cell.getZ() + 0.5;
      if (init_Z_NodeWalker_CachePo(world, x, y, z)) {
         return List.of(new TableIndex.Inner_SyncManager(x, z));
      }

      ArrayList<TableIndex.Inner_SyncManager> stands = new ArrayList<>(4);

      for (double offset : new double[]{0.38, 0.4, 0.22, 0.12}) {
         for (int axis = 0; axis < 2; axis++) {
            for (int side : new int[]{-1, 1}) {
               double px = x + (axis == 0 ? offset * side : 0.0);
               double pz = z + (axis == 1 ? offset * side : 0.0);
               if (init_Z_NodeWalker_CachePo(world, px, y, pz)) {
                  stands.add(new TableIndex.Inner_SyncManager(px, pz));
               }
            }
         }
      }

      for (double offset : new double[]{0.38, 0.4, 0.42}) {
         for (int sx : new int[]{-1, 1}) {
            for (int sz : new int[]{-1, 1}) {
               double px = x + sx * offset;
               double pz = z + sz * offset;
               if (init_Z_NodeWalker_CachePo(world, px, y, pz)) {
                  stands.add(new TableIndex.Inner_SyncManager(px, pz));
               }
            }
         }
      }

      return stands;
   }

   static boolean init_Z(NodeWalker.Inner_CachePool world, BlockPos cell, double y) {
      return !init_List(world, cell, y).isEmpty();
   }

   private static TableIndex.Inner_StateCache init_TableIndex_StateCa_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, BlockPos from, double fromY, BlockPos to, double toY, boolean sprint
   ) {
      List<TableIndex.Inner_SyncManager> starts = init_List(world, from, fromY);
      List<TableIndex.Inner_SyncManager> ends = init_List(world, to, toY);
      if (!starts.isEmpty() && !ends.isEmpty()) {
         if (starts.getFirst().x() == from.getX() + 0.5
            && starts.getFirst().z() == from.getZ() + 0.5
            && ends.getFirst().x() == to.getX() + 0.5
            && ends.getFirst().z() == to.getZ() + 0.5) {
            return null;
         }

         TableIndex.Inner_StateCache best = null;

         for (TableIndex.Inner_SyncManager start : starts) {
            for (TableIndex.Inner_SyncManager end : ends) {
               double dx = end.x() - start.x();
               double dz = end.z() - start.z();
               double length = Math.hypot(dx, dz);
               if (!(length < 0.05)) {
                  TableIndex.Inner_StateCache plan = init(
                     world,
                     start.x(),
                     start.z(),
                     fromY,
                     to,
                     toY,
                     end.x(),
                     end.z(),
                     dx / length,
                     dz / length,
                     0.0,
                     sprint,
                     Math.hypot(start.x() - from.getX() - 0.5, start.z() - from.getZ() - 0.5),
                     false
                  );
                  if (plan != null && plan.margin() >= -0.04 && (best == null || init(plan, best))) {
                     best = plan;
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   static double init_D_NodeWalker_CachePo(NodeWalker.Inner_CachePool world, double fx, double fz, double surface, double ux, double uz) {
      double last = 0.0;

      for (double t = 0.05; t <= 1.500000001 && init_Z_NodeWalker_CachePo(world, fx - ux * t, surface, fz - uz * t); t += 0.05) {
         last = t;
      }

      return Math.max(0.0, last - 0.15);
   }

   static boolean init_Z_NodeWalker_CachePo(NodeWalker.Inner_CachePool world, double x, double y, double z) {
      NodeWalker.Inner_StreamUtil standing = new NodeWalker.Inner_StreamUtil(x, y, z, 0.0, 0.0, 0.0, true, 0, false, false);
      if (NodeWalker.load(world, standing, standing)) {
         return false;
      }

      double half = 0.3;
      boolean floor = false;

      for (AABB b : world.init_List(new AABB(x - half, y - 0.02, z - half, x + half, y - 0.001, z + half))) {
         if (b.maxY >= y - 0.01) {
            floor = true;
            break;
         }
      }

      return floor && world.init_List(new AABB(x - half, y + 0.001, z - half, x + half, y + 1.8 - 0.01, z + half)).isEmpty();
   }

   private static boolean init(TableIndex.Inner_StateCache a, TableIndex.Inner_StateCache b) {
      boolean aSafe = a.margin() >= 0.0;
      boolean bSafe = b.margin() >= 0.0;
      if (aSafe != bSafe) {
         return aSafe;
      }

      if (aSafe && a.sprint() != b.sprint()) {
         return a.sprint();
      }

      boolean aComfort = a.margin() >= 0.14;
      boolean bComfort = b.margin() >= 0.14;
      if (aComfort && bComfort) {
         int aTotal = a.ticks();
         int bTotal = b.ticks();
         if (aTotal != bTotal) {
            return aTotal < bTotal;
         }

         if (Math.abs(a.landSpeed() - b.landSpeed()) > 0.02) {
            return a.landSpeed() > b.landSpeed();
         }
      }

      return a.margin() > b.margin();
   }

   private static List<NodeWalker.Inner_StreamUtil> init_List_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil start, NodeWalker.Inner_DataUtil run, int maxRun
   ) {
      List<NodeWalker.Inner_StreamUtil> out = new ArrayList<>(maxRun + 1);
      out.add(start);
      NodeWalker.Inner_StreamUtil s = start;

      for (int k = 1; k <= maxRun; k++) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(s, run, world);
         if (NodeWalker.load(world, s, next)) {
            break;
         }

         s = next;
         if (!s.onGround()) {
            break;
         }

         out.add(s);
      }

      return out;
   }

   private static TableIndex.Inner_CachePool init(
      NodeWalker.Inner_CachePool world,
      List<NodeWalker.Inner_StreamUtil> runs,
      NodeWalker.Inner_DataUtil press,
      NodeWalker.Inner_DataUtil air,
      TableIndex.Inner_NetHelper line,
      BlockPos to,
      double toSurface,
      boolean live,
      boolean quickRollout
   ) {
      TableIndex.Inner_TaskQueue trials = new TableIndex.Inner_TaskQueue(world, runs, press, air, line, to, toSurface);
      TableIndex.Inner_CachePool found = init_TableIndex_CachePo(trials);
      if (found != null && found.arc().margin() >= -0.04) {
         return found;
      }

      if (!quickRollout && init_Z(trials)) {
         TableIndex.Inner_CachePool scanned = init(trials, live);
         if (scanned == null) {
            return found;
         } else {
            return found != null && !(scanned.arc().margin() > found.arc().margin()) ? found : scanned;
         }
      } else {
         return found;
      }
   }

   private static boolean init_Z(TableIndex.Inner_TaskQueue trials) {
      int last = trials.runs().size() - 1;
      TableIndex.Inner_StreamUtil top = trials.fly(last, 2147483647);
      if (top != null && top.bumped()) {
         return true;
      }

      TableIndex.Inner_StreamUtil low = last == 0 ? null : trials.fly(0, 2147483647);
      return low != null && low.bumped();
   }

   private static TableIndex.Inner_CachePool init(TableIndex.Inner_TaskQueue trials, boolean live) {
      TableIndex.Inner_CachePool best = null;
      int last = Math.min(trials.runs().size() - 1, 15);

      for (int k = 0; k <= last; k++) {
         for (int release : live ? load_IArr : init_IArr) {
            TableIndex.Inner_StreamUtil a = trials.fly(k, release);
            if (a == null) {
               break;
            }

            if (best == null || a.margin() > best.arc().margin()) {
               best = new TableIndex.Inner_CachePool(k, a);
            }

            if (best.arc().margin() >= 0.15) {
               return best;
            }
         }
      }

      return best;
   }

   private static TableIndex.Inner_CachePool init_TableIndex_CachePo(TableIndex.Inner_TaskQueue trials) {
      int hi = trials.runs().size() - 1;
      TableIndex.Inner_StreamUtil top = trials.fly(hi, 2147483647);
      if (top == null) {
         return null;
      }

      if (top.along() <= trials.line().target()) {
         return new TableIndex.Inner_CachePool(hi, top);
      }

      TableIndex.Inner_StreamUtil low = hi == 0 ? top : trials.fly(0, 2147483647);
      if (low != null && low.along() > trials.line().target()) {
         return low.settled() && !low.bumped() && low.margin() >= 0.14 && init(low.end().x(), low.end().z(), trials.to()) >= 0.08
            ? new TableIndex.Inner_CachePool(0, low)
            : new TableIndex.Inner_CachePool(0, trials.releaseEarlier(0, low));
      }

      int lo = 0;
      TableIndex.Inner_StreamUtil lowArc = low;
      TableIndex.Inner_StreamUtil highArc = top;

      while (hi - lo > 1) {
         int mid = (lo + hi) / 2;
         TableIndex.Inner_StreamUtil a = trials.fly(mid, 2147483647);
         if (a == null) {
            break;
         }

         if (a.along() <= trials.line().target()) {
            lo = mid;
            lowArc = a;
         } else {
            hi = mid;
            highArc = a;
         }
      }

      TableIndex.Inner_CachePool shortSide = lowArc == null ? null : new TableIndex.Inner_CachePool(lo, lowArc);
      TableIndex.Inner_CachePool longSide = new TableIndex.Inner_CachePool(hi, highArc);
      if (highArc.margin() < -0.12 && (lowArc == null || lowArc.margin() < -0.12)) {
         TableIndex.Inner_StreamUtil eased = trials.releaseEarlier(hi, highArc);
         if (eased.margin() > highArc.margin()) {
            longSide = new TableIndex.Inner_CachePool(hi, eased);
         }
      }

      if (shortSide == null) {
         return longSide;
      } else if (shortSide.arc().margin() >= 0.14 && longSide.arc().margin() >= 0.14) {
         return shortSide;
      } else {
         return longSide.arc().margin() >= shortSide.arc().margin() ? longSide : shortSide;
      }
   }

   private static TableIndex.Inner_StreamUtil init_TableIndex_StreamU_NodeWalker_CachePo_2(
      NodeWalker.Inner_CachePool world,
      NodeWalker.Inner_StreamUtil takeoff,
      NodeWalker.Inner_DataUtil press,
      NodeWalker.Inner_DataUtil air,
      TableIndex.Inner_StreamUtil held,
      TableIndex.Inner_NetHelper line,
      BlockPos to,
      double toSurface
   ) {
      TableIndex.Inner_StreamUtil coast = init_TableIndex_StreamU_NodeWalker_CachePo(world, takeoff, press, air, 0, line, to, toSurface);
      if (coast == null) {
         return held;
      }

      if (coast.along() >= line.target()) {
         return coast.margin() > held.margin() ? coast : held;
      }

      int lo = 0;
      int hi = held.ticks();
      TableIndex.Inner_StreamUtil shortArc = coast;
      TableIndex.Inner_StreamUtil longArc = held;

      for (int i = 0; i < 6 && hi - lo > 1; i++) {
         int mid = (lo + hi) / 2;
         TableIndex.Inner_StreamUtil a = init_TableIndex_StreamU_NodeWalker_CachePo(world, takeoff, press, air, mid, line, to, toSurface);
         if (a == null) {
            break;
         }

         if (a.along() < line.target()) {
            lo = mid;
            shortArc = a;
         } else {
            hi = mid;
            longArc = a;
         }
      }

      return longArc.margin() >= shortArc.margin() ? longArc : shortArc;
   }

   private static TableIndex.Inner_StreamUtil init_TableIndex_StreamU_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world,
      NodeWalker.Inner_StreamUtil takeoff,
      NodeWalker.Inner_DataUtil press,
      NodeWalker.Inner_DataUtil air,
      int coastAfter,
      TableIndex.Inner_NetHelper line,
      BlockPos to,
      double toSurface
   ) {
      if (init(world, takeoff)) {
         return null;
      }

      NodeWalker.Inner_StreamUtil s = NodeWalker.init(takeoff, press.withJump(true), world);
      if (NodeWalker.load(world, takeoff, s)) {
         return new TableIndex.Inner_StreamUtil(s, 0, false, coastAfter, line.along(s), -1.0, true);
      }

      if (init(world, s)) {
         return null;
      }

      if (s.onGround() && s.y() <= takeoff.y() + 1.0E-6) {
         return null;
      }

      NodeWalker.Inner_DataUtil coast = new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, air.yaw());
      boolean bumped = s.horizontalCollision() || s.verticalCollision() && !s.onGround();
      double peak = Math.max(takeoff.y(), s.y());

      for (int t = 1; t <= 40; t++) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(s, t <= coastAfter ? air : coast, world);
         if (NodeWalker.load(world, s, next)) {
            return new TableIndex.Inner_StreamUtil(next, t, false, coastAfter, line.along(next), -1.0, true);
         }

         s = next;
         peak = Math.max(peak, s.y());
         if (init(world, s)) {
            return null;
         }

         if (s.horizontalCollision() || s.verticalCollision() && !s.onGround()) {
            bumped = true;
         }

         if (init(s)) {
            if (peak - s.y() > world.init_D() + 1.0E-5) {
               return new TableIndex.Inner_StreamUtil(s, t, true, coastAfter, line.along(s), -1.0, bumped);
            }

            double margin = s.y() <= toSurface + 0.02 && s.y() >= toSurface - 0.55 ? init_D_NodeWalker_CachePo_2(world, s, to, toSurface, air.yaw()) : -1.0;
            if (margin >= -0.04 && bumped && t <= 4 && !init_Z_D(s.x(), s.y(), s.z(), to, toSurface) && !init_Z_NodeWalker_CachePo_3(world, s, to, toSurface)) {
               margin = -1.0;
            }

            return new TableIndex.Inner_StreamUtil(s, t, true, coastAfter, line.along(s), margin, bumped);
         }

         if (!s.onGround() && s.y() < toSurface - 4.0) {
            return new TableIndex.Inner_StreamUtil(s, t, false, coastAfter, line.along(s), -1.0, bumped);
         }
      }

      return new TableIndex.Inner_StreamUtil(s, 40, false, coastAfter, line.along(s), -1.0, bumped);
   }

   static boolean init(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state) {
      return world.sync((int)Math.floor(state.x()), (int)Math.floor(state.y()), (int)Math.floor(state.z()));
   }

   public static double init_D_NodeWalker_CachePo_2(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil touchdown, BlockPos to, double toSurface, float yaw
   ) {
      if (NodeWalker.load(world, touchdown, touchdown)) {
         return -1.0;
      }

      double margin = init_D_NodeWalker_CachePo(world, touchdown.x(), touchdown.z(), to, toSurface);
      if (margin >= -0.04) {
         NodeWalker.Inner_StreamUtil slide = touchdown;
         NodeWalker.Inner_DataUtil coast = new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, yaw);

         for (int i = 0; i < 64 && (slide.speed() > 0.01 || !slide.onGround()); i++) {
            NodeWalker.Inner_StreamUtil next = NodeWalker.init(slide, coast, world);
            if (NodeWalker.load(world, slide, next) || Math.abs(next.y() - touchdown.y()) > 0.61) {
               return -1.0;
            }

            slide = next;
         }

         if (slide.onGround() && !(slide.speed() > 0.01)) {
            return init_Z_NodeWalker_CachePo_2(world, (int)Math.floor(slide.x()), (int)Math.floor(slide.z()), slide.y())
               ? margin
               : Math.min(margin, init_D_NodeWalker_CachePo(world, slide.x(), slide.z(), to, slide.y()));
         } else {
            return -1.0;
         }
      } else {
         BlockPos base = init_BlockPos_NodeWalker_CachePo(world, touchdown.x(), touchdown.z(), to, toSurface);
         double hx = -Math.sin(Math.toRadians(yaw));
         double hz = Math.cos(Math.toRadians(yaw));
         boolean alongX = Math.abs(hx) >= Math.abs(hz);
         int stepX = alongX ? (int)Math.signum(hx) : 0;
         int stepZ = alongX ? 0 : (int)Math.signum(hz);
         boolean runsOn = init_Z_NodeWalker_CachePo_2(world, base.getX() + stepX, base.getZ() + stepZ, toSurface);
         float axisYaw = NodeWalker.init_F_D(0.0, 0.0, stepX, stepZ);
         NodeWalker.Inner_DataUtil input = runsOn
            ? new NodeWalker.Inner_DataUtil(1.0, 0.0, false, false, false, axisYaw)
            : new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, yaw);
         NodeWalker.Inner_StreamUtil c = touchdown;
         double best = margin;

         for (int t = 0; t < 6; t++) {
            NodeWalker.Inner_StreamUtil next = NodeWalker.init(c, input, world);
            if (NodeWalker.load(world, c, next)) {
               return -1.0;
            }

            c = next;
            if (!c.onGround() || Math.abs(c.y() - touchdown.y()) > 0.02) {
               return -1.0;
            }

            double now = init_D_NodeWalker_CachePo(world, c.x(), c.z(), to, toSurface);
            if (runsOn) {
               best = Math.max(best, now);
               if (best >= 0.1) {
                  return init_D_NodeWalker_CachePo_2(world, c, to, toSurface, yaw);
               }
            } else {
               if (Math.hypot(c.vx(), c.vz()) < 0.01) {
                  return now;
               }

               best = now;
            }
         }

         return init_D_NodeWalker_CachePo(world, c.x(), c.z(), to, toSurface) >= -0.04
            ? Math.min(best, init_D_NodeWalker_CachePo_2(world, c, to, toSurface, yaw))
            : -1.0;
      }
   }

   static double init_D_NodeWalker_CachePo(NodeWalker.Inner_CachePool world, double x, double z, BlockPos to, double surface) {
      BlockPos cell = init_BlockPos_NodeWalker_CachePo(world, x, z, to, surface);
      int cx = cell.getX();
      int cz = cell.getZ();
      double half = 0.3;
      double left = x - half - cx;
      double right = cx + 1 - (x + half);
      double front = z - half - cz;
      double back = cz + 1 - (z + half);
      if (left < 0.5 && init_Z_NodeWalker_CachePo_2(world, cx - 1, cz, surface)) {
         left++;
      }

      if (right < 0.5 && init_Z_NodeWalker_CachePo_2(world, cx + 1, cz, surface)) {
         right++;
      }

      if (front < 0.5 && init_Z_NodeWalker_CachePo_2(world, cx, cz - 1, surface)) {
         front++;
      }

      if (back < 0.5 && init_Z_NodeWalker_CachePo_2(world, cx, cz + 1, surface)) {
         back++;
      }

      double margin = Math.min(Math.min(left, right), Math.min(front, back));
      if (!(margin >= -0.04) && (int)Math.floor(x) == to.getX() && (int)Math.floor(z) == to.getZ()) {
         AABB feet = new AABB(x - half, surface - 0.02, z - half, x + half, surface - 0.001, z + half);

         for (AABB support : world.init_List(feet)) {
            if (!(Math.abs(support.maxY - surface) > 0.01)
               && (!(support.getXsize() >= 0.99) || !(support.getZsize() >= 0.99))
               && init_Z_NodeWalker_CachePo(world, x, surface, z)) {
               double overlapX = Math.min(feet.maxX, support.maxX) - Math.max(feet.minX, support.minX);
               double overlapZ = Math.min(feet.maxZ, support.maxZ) - Math.max(feet.minZ, support.minZ);
               margin = Math.max(margin, Math.min(overlapX, overlapZ) - 0.055);
            }
         }

         return margin;
      } else {
         return margin;
      }
   }

   private static BlockPos init_BlockPos_NodeWalker_CachePo(NodeWalker.Inner_CachePool world, double x, double z, BlockPos to, double surface) {
      int cx = (int)Math.floor(x);
      int cz = (int)Math.floor(z);
      if (cx == to.getX() && cz == to.getZ()) {
         return to;
      }

      boolean beside = Math.abs(cx - to.getX()) <= 1 && Math.abs(cz - to.getZ()) <= 1;
      return beside && init_Z_NodeWalker_CachePo_2(world, cx, cz, surface) ? new BlockPos(cx, to.getY(), cz) : to;
   }

   private static boolean init_Z_NodeWalker_CachePo_3(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, BlockPos target, double surface) {
      double x = target.getX() + 0.5;
      double z = target.getZ() + 0.5;
      return init_Z_NodeWalker_CachePo(world, state.x(), state.z(), x, z, surface)
         ? true
         : init_Z_NodeWalker_CachePo(world, state.x(), state.z(), x, state.z(), surface) && init_Z_NodeWalker_CachePo(world, x, state.z(), x, z, surface)
            || init_Z_NodeWalker_CachePo(world, state.x(), state.z(), state.x(), z, surface) && init_Z_NodeWalker_CachePo(world, state.x(), z, x, z, surface);
   }

   private static boolean init_Z_NodeWalker_CachePo(NodeWalker.Inner_CachePool world, double x, double z, double toX, double toZ, double surface) {
      int samples = Math.max(1, (int)Math.ceil(Math.hypot(toX - x, toZ - z) / 0.15));

      for (int i = 1; i <= samples; i++) {
         double t = (double)i / samples;
         if (!init_Z_NodeWalker_CachePo(world, x + (toX - x) * t, surface, z + (toZ - z) * t)) {
            return false;
         }
      }

      return true;
   }

   public static boolean init_Z_NodeWalker_CachePo_2(NodeWalker.Inner_CachePool world, int cx, int cz, double surface) {
      NodeWalker.Inner_StreamUtil standing = new NodeWalker.Inner_StreamUtil(cx + 0.5, surface, cz + 0.5, 0.0, 0.0, 0.0, true, 0, false, false);
      if (NodeWalker.load(world, standing, standing)) {
         return false;
      }

      boolean floor = false;

      for (AABB b : world.init_List(new AABB(cx + 0.05, surface - 0.02, cz + 0.05, cx + 0.95, surface - 0.001, cz + 0.95))) {
         if (b.maxY >= surface - 0.01 && b.maxY <= surface + 0.01 && b.minX <= cx + 0.01 && b.maxX >= cx + 0.99 && b.minZ <= cz + 0.01 && b.maxZ >= cz + 0.99) {
            floor = true;
            break;
         }
      }

      return floor && world.init_List(new AABB(cx + 0.05, surface + 0.001, cz + 0.05, cx + 0.95, surface + 1.8 - 0.01, cz + 0.95)).isEmpty();
   }

   static boolean init(NodeWalker.Inner_StreamUtil s) {
      return s.onGround() && s.vy() <= 0.0;
   }

   public static boolean init_Z_D(double x, double y, double z, BlockPos to, double toSurface) {
      return !(y > toSurface + 0.02) && !(y < toSurface - 0.55) ? init(x, z, to) >= -0.12 : false;
   }

   public static double init(double x, double z, BlockPos cell) {
      double half = 0.3;
      double left = x - half - cell.getX();
      double right = cell.getX() + 1 - (x + half);
      double front = z - half - cell.getZ();
      double back = cell.getZ() + 1 - (z + half);
      return Math.min(Math.min(left, right), Math.min(front, back));
   }

   public static TableIndex.UiHelper init_TableIndex_UiHelpe_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, float yaw, boolean sprint, BlockPos to, double toSurface
   ) {
      return init_TableIndex_UiHelpe_NodeWalker_CachePo(world, state, yaw, sprint, to, toSurface, 40);
   }

   public static TableIndex.UiHelper init_TableIndex_UiHelpe_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, float yaw, boolean sprint, BlockPos to, double toSurface, int maxRunTicks
   ) {
      double tx = to.getX() + 0.5;
      double tz = to.getZ() + 0.5;
      double dx = tx - state.x();
      double dz = tz - state.z();
      double dist = Math.hypot(dx, dz);
      if (dist < 1.0E-6) {
         return new TableIndex.UiHelper(0, 0.0, 2147483647, -1.0, sprint);
      }

      TableIndex.Inner_NetHelper line = new TableIndex.Inner_NetHelper(state.x(), state.z(), dx / dist, dz / dist, dist);
      NodeWalker.Inner_DataUtil air = NodeWalker.Inner_DataUtil.run(yaw, sprint);
      TableIndex.Inner_StreamUtil immediate = init_TableIndex_StreamU_NodeWalker_CachePo(world, state, air, air, 2147483647, line, to, toSurface);
      if (immediate != null
         && immediate.settled()
         && !immediate.bumped()
         && immediate.margin() >= 0.08
         && init(immediate.end().x(), immediate.end().z(), to) >= 0.08) {
         return new TableIndex.UiHelper(0, 0.0, 2147483647, immediate.margin(), sprint, immediate.ticks() + 1);
      }

      TableIndex.UiHelper best = null;
      double bestScore = -1.0 / 0.0;

      for (double strafe : new double[]{0.0, 1.0, -1.0}) {
         NodeWalker.Inner_DataUtil run = air.withStrafe(strafe);
         TableIndex.Inner_CachePool b = init(
            world, init_List_NodeWalker_CachePo(world, state, run, Math.clamp(maxRunTicks, 0, 40)), run, air, line, to, toSurface, true, false
         );
         double margin = b == null ? -1.0 : b.arc().margin();
         int k = b == null ? 0 : b.runTicks();
         double score = margin - 0.01 * k - (strafe == 0.0 ? 0.0 : 0.03);
         if (score > bestScore) {
            bestScore = score;
            best = new TableIndex.UiHelper(k, strafe, b == null ? 2147483647 : b.arc().coastAfter(), margin, sprint, b == null ? 0 : b.arc().ticks() + 1);
         }

         if (strafe == 0.0 && margin >= 0.0) {
            break;
         }
      }

      return best;
   }

   public static TableIndex.Inner_DataUtil init_TableIndex_DataUti_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, float yaw, boolean sprint, BlockPos to, double toSurface
   ) {
      return init_TableIndex_DataUti_NodeWalker_CachePo_2(world, state, yaw, sprint, to, toSurface, (TableIndex.Inner_BufferHelper)null);
   }

   public static TableIndex.Inner_DataUtil init_TableIndex_DataUti_NodeWalker_CachePo_3(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, float yaw, boolean sprint, BlockPos to, double toSurface, BlockPos continuation
   ) {
      return init_TableIndex_DataUti_NodeWalker_CachePo_2(
         world, state, yaw, sprint, to, toSurface, continuation == null ? null : TableIndex.Inner_BufferHelper.walk(continuation, toSurface)
      );
   }

   public static TableIndex.Inner_DataUtil init_TableIndex_DataUti_NodeWalker_CachePo_2(
      NodeWalker.Inner_CachePool world,
      NodeWalker.Inner_StreamUtil state,
      float yaw,
      boolean sprint,
      BlockPos to,
      double toSurface,
      TableIndex.Inner_BufferHelper continuation
   ) {
      TableIndex.Inner_DataUtil best = null;
      double bestScore = -1.0 / 0.0;

      for (double forward : new double[]{1.0, 0.0, -1.0}) {
         if (forward < 0.0 && best != null && best.margin() >= -0.12) {
            break;
         }

         for (double strafe : new double[]{0.0, 1.0, -1.0}) {
            NodeWalker.Inner_DataUtil air = new NodeWalker.Inner_DataUtil(forward, strafe, false, sprint && forward > 0.0, false, yaw);
            NodeWalker.Inner_StreamUtil s = state;
            double margin = -1.0;
            boolean unsafe = false;

            for (int t = 1; t <= 100; t++) {
               NodeWalker.Inner_StreamUtil next = NodeWalker.init(s, air, world);
               if (NodeWalker.load(world, s, next)) {
                  unsafe = true;
                  break;
               }

               s = next;
               if (init(world, s)) {
                  break;
               }

               if (init(s)) {
                  margin = s.y() <= toSurface + 0.02 && s.y() >= toSurface - 0.55 ? init_D_NodeWalker_CachePo_2(world, s, to, toSurface, yaw) : -1.0;
                  break;
               }

               if (!s.onGround() && s.vy() < 0.0 && s.y() < toSurface - 4.0) {
                  break;
               }
            }

            if (!unsafe) {
               boolean intended = init_Z_D(s.x(), s.y(), s.z(), to, toSurface);
               boolean continues = continuation != null
                  && margin >= -0.12
                  && (continuation.nextJump() == null || forward > 0.0 && strafe == 0.0)
                  && continuation.proves(world, s);
               if (continues) {
                  margin = Math.max(margin, 0.14);
               }

               double intent = margin >= -0.12 && (intended || continues) ? 2.0 : 0.0;
               if (forward > 0.0 && strafe == 0.0 && margin >= 0.08 && (intended || continues)) {
                  return new TableIndex.Inner_DataUtil(forward, strafe, margin);
               }

               double score = intent + margin - (strafe == 0.0 ? 0.0 : 0.02) - (forward > 0.0 ? 0.0 : 0.01);
               if (score > bestScore) {
                  bestScore = score;
                  best = new TableIndex.Inner_DataUtil(forward, strafe, margin);
               }
            }
         }
      }

      return best == null ? new TableIndex.Inner_DataUtil(0.0, 0.0, -1.0) : best;
   }

   public static TableIndex.Inner_DataUtil init_TableIndex_DataUti_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, float yaw, boolean sprint, BlockPos to, double toSurface, int forwardTicksLeft
   ) {
      if (forwardTicksLeft == 2147483647) {
         return null;
      }

      NodeWalker.Inner_StreamUtil s = state;

      for (int tick = 1; tick <= 100; tick++) {
         NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(
            tick <= forwardTicksLeft ? 1.0 : 0.0, 0.0, false, sprint && tick <= forwardTicksLeft, false, yaw
         );
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(s, input, world);
         if (NodeWalker.load(world, s, next) || init(world, next)) {
            return null;
         }

         s = next;
         if (init(s)) {
            double margin = s.y() <= toSurface + 0.02 && s.y() >= toSurface - 0.55 ? init_D_NodeWalker_CachePo_2(world, s, to, toSurface, yaw) : -1.0;
            return margin >= -0.12 ? new TableIndex.Inner_DataUtil(forwardTicksLeft > 0 ? 1.0 : 0.0, 0.0, margin) : null;
         }

         if (s.y() < toSurface - 4.0) {
            return null;
         }
      }

      return null;
   }

   public static double init(double rise, boolean sprint, boolean running) {
      int key = (int)Math.round(rise * 4.0) * 8 + (sprint ? 4 : 0) + (running ? 2 : 0) + 1;
      Double known = init_ConcurrentHashMap.get(key);
      if (known != null) {
         return known;
      }

      double value = init_D_D(rise, sprint, running, true);
      init_ConcurrentHashMap.put(key, value);
      return value;
   }

   public static double init(double rise) {
      int key = (int)Math.round(rise * 4.0) * 8;
      Double known = init_ConcurrentHashMap.get(key);
      if (known != null) {
         return known;
      }

      double value = init_D_D(rise, false, false, false);
      init_ConcurrentHashMap.put(key, value);
      return value;
   }

   private static double init_D_D(double rise, boolean sprint, boolean running, boolean longest) {
      TableIndex.Inner_ConfigStore world = new TableIndex.Inner_ConfigStore();
      double startX = running ? 0.3 : 0.5;
      NodeWalker.Inner_StreamUtil start = new NodeWalker.Inner_StreamUtil(startX, 64.0, 0.5, running ? 0.28 : 0.0, -0.0784, 0.0, true, 0, false, false);
      NodeWalker.Inner_DataUtil run = NodeWalker.Inner_DataUtil.run(-90.0F, sprint);
      List<NodeWalker.Inner_StreamUtil> runs = init_List_NodeWalker_CachePo(world, start, run, 12);
      NodeWalker.Inner_StreamUtil takeoff = longest ? runs.get(runs.size() - 1) : runs.get(0);
      NodeWalker.Inner_StreamUtil s = NodeWalker.init(takeoff, run.withJump(true), world);
      NodeWalker.Inner_DataUtil air = longest ? run : new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, -90.0F);
      double level = 64.0 + rise;
      double reach = -1.0;

      for (int t = 1; t <= 100; t++) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(s, air, world);
         if (next.vy() < 0.0 && s.y() >= level && next.y() < level) {
            reach = next.x() - startX;
            break;
         }

         if (next.y() < level - 2.0 && next.vy() < 0.0) {
            break;
         }

         s = next;
      }

      return reach;
   }

   public record Inner_BufferHelper(BlockPos cell, double surface, TableIndex.Inner_JsonUtil nextJump) {
      static TableIndex.Inner_BufferHelper walk(BlockPos cell, double surface) {
         return new TableIndex.Inner_BufferHelper(cell, surface, null);
      }

      static TableIndex.Inner_BufferHelper jump(BlockPos from, double fromSurface, BlockPos to, double toSurface, float yaw, boolean sprint) {
         return new TableIndex.Inner_BufferHelper(to, toSurface, new TableIndex.Inner_JsonUtil(from, fromSurface, yaw, sprint));
      }

      boolean proves(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil touchdown) {
         if (!TableIndex.init(touchdown)) {
            return false;
         } else if (this.nextJump != null) {
            return !atCell(touchdown, this.nextJump.from(), this.nextJump.fromSurface())
               ? false
               : TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(
                        world, touchdown, this.nextJump.yaw(), this.nextJump.sprint(), this.cell, this.surface, 6
                     )
                     .margin()
                  >= -0.04;
         } else {
            return atCell(touchdown, this.cell, this.surface)
               && TableIndex.init_Z_NodeWalker_CachePo_2(world, this.cell.getX(), this.cell.getZ(), this.surface);
         }
      }

      private static boolean atCell(NodeWalker.Inner_StreamUtil state, BlockPos cell, double surface) {
         return state.y() <= surface + 0.02
            && state.y() >= surface - 0.55
            && (int)Math.floor(state.x()) == cell.getX()
            && (int)Math.floor(state.z()) == cell.getZ();
      }
   }

   private record Inner_CachePool(int runTicks, TableIndex.Inner_StreamUtil arc) {
   }

   private static final class Inner_ConfigStore implements NodeWalker.Inner_CachePool {
      private static final AABB init = new AABB(-32.0, 63.0, -32.0, 1.0, 64.0, 32.0);

      @Override
      public List<AABB> init_List(AABB q) {
         return init.intersects(q) ? List.of(init) : List.of();
      }
   }

   public record Inner_DataUtil(double forward, double strafe, double margin) {
   }

   public record Inner_JsonUtil(BlockPos from, double fromSurface, float yaw, boolean sprint) {
   }

   private record Inner_NetHelper(double ox, double oz, double ux, double uz, double target) {
      double along(NodeWalker.Inner_StreamUtil s) {
         return (s.x() - this.ox) * this.ux + (s.z() - this.oz) * this.uz;
      }
   }

   public record Inner_StateCache(
      float yaw,
      int runTicks,
      boolean sprint,
      int coastAfter,
      int flightTicks,
      double startX,
      double startZ,
      double landX,
      double landY,
      double landZ,
      double landSpeed,
      double margin,
      double backup,
      double approach,
      PoolEntry maneuver
   ) {
      public Inner_StateCache(
         float yaw,
         int runTicks,
         boolean sprint,
         int coastAfter,
         int flightTicks,
         double startX,
         double startZ,
         double landX,
         double landY,
         double landZ,
         double landSpeed,
         double margin,
         double backup,
         double approach
      ) {
         this(yaw, runTicks, sprint, coastAfter, flightTicks, startX, startZ, landX, landY, landZ, landSpeed, margin, backup, approach, null);
      }

      public Inner_StateCache(
         float yaw,
         int runTicks,
         boolean sprint,
         int coastAfter,
         int flightTicks,
         double startX,
         double startZ,
         double landX,
         double landY,
         double landZ,
         double landSpeed,
         double margin
      ) {
         this(yaw, runTicks, sprint, coastAfter, flightTicks, startX, startZ, landX, landY, landZ, landSpeed, margin, 0.0, 0.0);
      }

      public boolean running() {
         return this.approach > 0.0;
      }

      public int ticks() {
         return this.backupTicks() + (this.maneuver == null ? this.runTicks + this.flightTicks + 1 : this.maneuver.inputs().size());
      }

      public int backupTicks() {
         return this.backup > 0.0 ? (int)Math.ceil(this.backup / 0.1) + 4 : 0;
      }

      public TableIndex.Inner_StateCache shifted(int dx, int dy, int dz) {
         return new TableIndex.Inner_StateCache(
            this.yaw,
            this.runTicks,
            this.sprint,
            this.coastAfter,
            this.flightTicks,
            this.startX + dx,
            this.startZ + dz,
            this.landX + dx,
            this.landY + dy,
            this.landZ + dz,
            this.landSpeed,
            this.margin,
            this.backup,
            this.approach,
            this.maneuver == null ? null : this.maneuver.shifted(dx, dy, dz)
         );
      }
   }

   private record Inner_StreamUtil(NodeWalker.Inner_StreamUtil end, int ticks, boolean settled, int coastAfter, double along, double margin, boolean bumped) {
   }

   record Inner_SyncManager(double x, double z) {
   }

   private record Inner_TaskQueue(
      NodeWalker.Inner_CachePool world,
      List<NodeWalker.Inner_StreamUtil> runs,
      NodeWalker.Inner_DataUtil press,
      NodeWalker.Inner_DataUtil air,
      TableIndex.Inner_NetHelper line,
      BlockPos to,
      double surface,
      TableIndex.Inner_StreamUtil[] held
   ) {
      Inner_TaskQueue(
         NodeWalker.Inner_CachePool world,
         List<NodeWalker.Inner_StreamUtil> runs,
         NodeWalker.Inner_DataUtil press,
         NodeWalker.Inner_DataUtil air,
         TableIndex.Inner_NetHelper line,
         BlockPos to,
         double surface
      ) {
         this(world, runs, press, air, line, to, surface, new TableIndex.Inner_StreamUtil[runs.size()]);
      }

      TableIndex.Inner_StreamUtil fly(int run, int release) {
         if (release != 2147483647) {
            return TableIndex.init_TableIndex_StreamU_NodeWalker_CachePo(
               this.world, this.runs.get(run), this.press, this.air, release, this.line, this.to, this.surface
            );
         }

         TableIndex.Inner_StreamUtil previous = this.held[run];
         return previous != null
            ? previous
            : (
               this.held[run] = TableIndex.init_TableIndex_StreamU_NodeWalker_CachePo(
                  this.world, this.runs.get(run), this.press, this.air, 2147483647, this.line, this.to, this.surface
               )
            );
      }

      TableIndex.Inner_StreamUtil releaseEarlier(int run, TableIndex.Inner_StreamUtil arc) {
         return TableIndex.init_TableIndex_StreamU_NodeWalker_CachePo_2(
            this.world, this.runs.get(run), this.press, this.air, arc, this.line, this.to, this.surface
         );
      }
   }

   public record UiHelper(int runTicks, double strafe, int coastAfter, double margin, boolean sprint, int flightTicks) {
      public UiHelper(int runTicks, double strafe, int coastAfter, double margin, boolean sprint) {
         this(runTicks, strafe, coastAfter, margin, sprint, 0);
      }
   }
}
