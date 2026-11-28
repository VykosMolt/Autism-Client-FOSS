package autismclient.ai.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.ToLongFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

final class AiSchematicBatchPlan {
   static final int init_I = 192;
   static final int load_I = 32;
   static final int read_I = 64;
   static final long init_J = 8000000000L;
   static final long load_J = 5000000000L;
   private static final double init_D = 24.0;
   private static final double load_D = 10.0;
   private static final double read_D = 24.0;

   private AiSchematicBatchPlan() {
   }

   static double stationCost(BlockPos feet, Vec3 worker, int useful, int supports, int clearance) {
      if (useful <= 0) {
         return 1.0 / 0.0;
      }

      double travel = AiPathfinder.MoveCosts.estimateDirectionalHeuristicCompat(feet.getX() - Mth.floor(worker.x), feet.getY() - Mth.floor(worker.y), feet.getZ() - Mth.floor(worker.z));
      return (24.0 + travel + supports * 64.0 + clearance * 24.0) / useful;
   }

   static double stationScore(BlockPos feet, Vec3 worker, int useful, int supports, int clearance) {
      return (stationCost(feet, worker, useful, supports, clearance) * useful + useful * 10.0) / 20.0;
   }

   static <T> List<T> travelOrder(List<T> candidates, Vec3 start, Function<T, BlockPos> position) {
      if (candidates.size() > 192) {
         throw new IllegalArgumentException("Unbounded source window");
      }

      ArrayList<T> ranked = new ArrayList<>(candidates);
      ranked.sort(Comparator.comparingDouble(cell -> position.apply((T)cell).distToCenterSqr(start)));
      return List.copyOf(ranked.subList(0, Math.min(32, ranked.size())));
   }

   static List<Integer> dependencyOrder(long available, long[] before) {
      if (before.length > 32) {
         throw new IllegalArgumentException("Unbounded batch");
      }

      long mask = (1L << before.length) - 1L;
      if ((available & ~mask) != 0L) {
         throw new IllegalArgumentException("Unknown batch target");
      }

      ArrayList<Integer> result = new ArrayList<>();
      long pending = available;

      while (pending != 0L) {
         int next = -1;

         for (int i = 0; i < before.length; i++) {
            if ((pending & 1L << i) != 0L && (before[i] & pending) == 0L) {
               next = i;
               break;
            }
         }

         if (next >= 0) {
            result.add(next);
            pending &= ~(1L << next);
         } else {
            int drop = -1;
            int outgoing = -1;

            for (int i = 0; i < before.length; i++) {
               if ((pending & 1L << i) != 0L) {
                  int count = 0;

                  for (int j = 0; j < before.length; j++) {
                     if ((pending & 1L << j) != 0L && (before[j] & 1L << i) != 0L) {
                        count++;
                     }
                  }

                  if (count > outgoing) {
                     outgoing = count;
                     drop = i;
                  }
               }
            }

            pending &= ~(1L << drop);
         }
      }

      return List.copyOf(result);
   }

   static <T> void promoteBatch(List<T> source, int cursor, List<T> batch) {
      LinkedHashSet<T> selected = new LinkedHashSet<>(batch);
      ArrayList<T> rest = new ArrayList<>();
      HashSet<T> found = new HashSet<>();
      int end = Math.min(source.size(), cursor + 192);

      for (int i = cursor; i < end; i++) {
         T cell = source.get(i);
         if (selected.contains(cell)) {
            found.add(cell);
         } else {
            rest.add(cell);
         }
      }

      int at = cursor;

      for (T cell : selected) {
         if (found.contains(cell)) {
            source.set(at++, cell);
         }
      }

      for (T cell : rest) {
         source.set(at++, cell);
      }

      if (at < end) {
         source.subList(at, end).clear();
      }
   }

   static <T> Set<Long> windowCells(List<T> source, int cursor, Set<Long> existing, ToLongFunction<T> position) {
      HashSet<Long> result = new HashSet<>(existing);

      for (int i = cursor; i < source.size() && i < cursor + 192; i++) {
         result.add(position.applyAsLong(source.get(i)));
      }

      return Collections.unmodifiableSet(result);
   }

   record Inner_BufferHelper(BlockPos feet, long jobs, long[] blockers, int supports, int clearance, long requires) {
      Inner_BufferHelper(BlockPos feet, long jobs, long[] blockers, int supports, int clearance) {
         this(feet, jobs, blockers, supports, clearance, 0L);
      }

      Inner_BufferHelper(BlockPos feet, long jobs, long[] blockers, int supports, int clearance, long requires) {
         if (blockers.length <= 32 && supports >= 0 && clearance >= 0) {
            blockers = (long[])blockers.clone();
            this.feet = feet;
            this.jobs = jobs;
            this.blockers = blockers;
            this.supports = supports;
            this.clearance = clearance;
            this.requires = requires;
         } else {
            throw new IllegalArgumentException("Invalid workstation");
         }
      }

      double constructionTicks() {
         return this.supports * 64.0 + this.clearance * 24.0;
      }
   }

   static final class Inner_CachePool {
      private static final int init_I = 16;
      private final List<AiSchematicBatchPlan.Inner_BufferHelper> stations;
      private final int targets;
      private final long all;
      private final double[][] legCost;
      private final double[] startLegCost;
      private final double[] continuationCost;
      private final long[][][] blockedViews;
      private List<AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil> beam;
      private final Map<AiSchematicBatchPlan.Inner_CachePool.Inner_DataUtil, AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil> nextBeam = new HashMap<>();
      private AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil bestComplete;
      private AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil mostCovered;
      private final AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil rootState;
      private AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil greedyState;
      private AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil greedyBest;
      private double greedyBestValue;
      private int greedyPass;
      private int greedyStation;
      private int beamIndex;
      private int beamStation;
      private int beamDepth;
      private boolean searchFinished;

      Inner_CachePool(List<AiSchematicBatchPlan.Inner_BufferHelper> stations, Vec3 worker, BlockPos continuation, int targets, long available) {
         if (targets >= 0 && targets <= 32 && stations.size() <= 128 && (available & ~((1L << targets) - 1L)) == 0L) {
            this.stations = List.copyOf(stations);
            this.targets = targets;
            int count = stations.size();
            long all = 0L;

            for (AiSchematicBatchPlan.Inner_BufferHelper station : stations) {
               if (station.blockers.length != targets || (station.jobs & ~available) != 0L) {
                  throw new IllegalArgumentException("Unknown route target");
               }

               all |= station.jobs;
            }

            this.all = all;
            this.legCost = new double[count][count];
            this.startLegCost = new double[count];
            this.continuationCost = new double[count];
            BlockPos start = BlockPos.containing(worker);

            for (int i = 0; i < count; i++) {
               this.startLegCost[i] = directionalHeuristic(start, stations.get(i).feet);
               this.continuationCost[i] = continuation == null ? 0.0 : directionalHeuristic(stations.get(i).feet, continuation);

               for (int j = 0; j < count; j++) {
                  this.legCost[i][j] = directionalHeuristic(stations.get(i).feet, stations.get(j).feet);
               }
            }

            this.blockedViews = new long[targets][targets][2];
            long[][] views = new long[targets][2];

            for (int s = 0; s < count; s++) {
               for (int job = 0; job < targets; job++) {
                  if ((stations.get(s).jobs & 1L << job) != 0L) {
                     views[job][s >>> 6] = views[job][s >>> 6] | 1L << (s & 63);
                     long blockers = stations.get(s).blockers[job];

                     while (blockers != 0L) {
                        int placed = Long.numberOfTrailingZeros(blockers);
                        blockers &= blockers - 1L;
                        if (placed >= targets) {
                           throw new IllegalArgumentException("Unknown occluder");
                        }

                        this.blockedViews[placed][job][s >>> 6] = this.blockedViews[placed][job][s >>> 6] | 1L << (s & 63);
                     }
                  }
               }
            }

            this.rootState = new AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil(0L, -1, 0.0, 0L, 0L, views, List.of(), 0, 0.0);
            this.beam = List.of(this.rootState);
            this.mostCovered = this.rootState;
            this.greedyState = this.rootState;
            this.greedyBestValue = 1.0 / 0.0;
            this.searchFinished = this.all == 0L;
         } else {
            throw new IllegalArgumentException("Unbounded route search");
         }
      }

      boolean isDone() {
         return this.searchFinished;
      }

      void step() {
         if (!this.searchFinished) {
            if (this.greedyPass < 3) {
               this.greedyStep();
            } else if (this.beamIndex >= this.beam.size()) {
               if (!this.nextBeam.isEmpty() && ++this.beamDepth < this.targets) {
                  ArrayList<AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil> ranked = new ArrayList<>(this.nextBeam.values());
                  ranked.sort(
                     Comparator.comparingInt(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil::supports)
                        .thenComparingDouble(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil::bound)
                        .thenComparingDouble(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil::cost)
                        .thenComparingLong(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil::done)
                        .thenComparingInt(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil::at)
                  );
                  this.beam = List.copyOf(ranked.subList(0, Math.min(16, ranked.size())));
                  this.nextBeam.clear();
                  this.beamIndex = this.beamStation = 0;
               } else {
                  this.searchFinished = true;
               }
            } else {
               AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil state = this.beam.get(this.beamIndex);
               if (this.beamStation >= this.stations.size()) {
                  this.beamIndex++;
                  this.beamStation = 0;
               } else {
                  int at = this.beamStation++;
                  AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil candidate = this.expand(state, at);
                  if (candidate != null) {
                     if (Long.bitCount(candidate.done) > Long.bitCount(this.mostCovered.done)
                        || Long.bitCount(candidate.done) == Long.bitCount(this.mostCovered.done)
                           && candidate.bound < this.mostCovered.bound) {
                        this.mostCovered = candidate;
                     }

                     if (candidate.done == this.all) {
                        if (this.isBetterCandidate(candidate, this.bestComplete)) {
                           this.bestComplete = candidate;
                        }
                     } else if (this.bestComplete == null
                        || candidate.supports <= this.bestComplete.supports
                           && (candidate.supports != this.bestComplete.supports || !(candidate.bound >= this.costWithContinuation(this.bestComplete)))) {
                        AiSchematicBatchPlan.Inner_CachePool.Inner_DataUtil key = new AiSchematicBatchPlan.Inner_CachePool.Inner_DataUtil(
                           candidate.done, at, candidate.visitedLow, candidate.visitedHigh
                        );
                        AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil prior = this.nextBeam.get(key);
                        if (prior == null || candidate.cost < prior.cost) {
                           this.nextBeam.put(key, candidate);
                        }
                     }
                  }
               }
            }
         }
      }

      private void greedyStep() {
         if (this.greedyStation >= this.stations.size()) {
            if (this.greedyBest != null) {
               this.greedyState = this.greedyBest;
               if (Long.bitCount(this.greedyState.done) > Long.bitCount(this.mostCovered.done)) {
                  this.mostCovered = this.greedyState;
               }

               if (this.greedyState.done != this.all) {
                  this.greedyStation = 0;
                  this.greedyBest = null;
                  this.greedyBestValue = 1.0 / 0.0;
                  return;
               }

               if (this.isBetterCandidate(this.greedyState, this.bestComplete)) {
                  this.bestComplete = this.greedyState;
               }
            }

            this.greedyPass++;
            this.greedyState = this.rootState;
            this.greedyStation = 0;
            this.greedyBest = null;
            this.greedyBestValue = 1.0 / 0.0;
         } else {
            AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil candidate = this.expand(this.greedyState, this.greedyStation++);
            if (candidate != null) {
               if (Long.bitCount(candidate.done) > Long.bitCount(this.mostCovered.done)) {
                  this.mostCovered = candidate;
               }

               if (candidate.done == this.all && this.isBetterCandidate(candidate, this.bestComplete)) {
                  this.bestComplete = candidate;
               }

               int gain = Long.bitCount(candidate.done & ~this.greedyState.done);
               double var10000;
               switch (this.greedyPass) {
                  case 0:
                     var10000 = (candidate.cost - this.greedyState.cost) / gain;
                     break;
                  case 1:
                     var10000 = candidate.cost - this.greedyState.cost;
                     break;
                  default:
                     var10000 = candidate.bound;
               }

               double value = var10000;
               if (this.greedyBest == null
                  || candidate.supports < this.greedyBest.supports
                  || candidate.supports == this.greedyBest.supports && value < this.greedyBestValue) {
                  this.greedyBestValue = value;
                  this.greedyBest = candidate;
               }
            }
         }
      }

      private AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil expand(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil state, int at) {
         AiSchematicBatchPlan.Inner_BufferHelper station = this.stations.get(at);
         if ((station.requires & ~state.done) != 0L) {
            return null;
         }

         long available = station.jobs & ~state.done;

         for (int i = 0; i < this.targets; i++) {
            if ((station.blockers[i] & state.done) != 0L) {
               available &= ~(1L << i);
            }
         }

         if (available == 0L) {
            return null;
         }

         long[] before = new long[this.targets];

         for (int job = 0; job < this.targets; job++) {
            if ((available & 1L << job) != 0L) {
               long blockers = station.blockers[job] & available;

               while (blockers != 0L) {
                  int close = Long.numberOfTrailingZeros(blockers);
                  blockers &= blockers - 1L;
                  before[close] |= 1L << job;
               }
            }
         }

         List<Integer> ordered = AiSchematicBatchPlan.dependencyOrder(available, before);
         long done = state.done;
         long[][] views = new long[this.targets][2];

         for (int j = 0; j < this.targets; j++) {
            views[j] = (long[])state.views[j].clone();
         }

         ArrayList<Integer> clicks = new ArrayList<>();

         for (int job : ordered) {
            if ((station.blockers[job] & done) == 0L) {
               long after = done | 1L << job;
               boolean safe = true;

               for (int j = 0; j < this.targets; j++) {
                  if ((this.all & ~after & 1L << j) != 0L
                     && (views[j][0] & ~this.blockedViews[job][j][0]) == 0L
                     && (views[j][1] & ~this.blockedViews[job][j][1]) == 0L) {
                     safe = false;
                     break;
                  }
               }

               if (safe) {
                  done = after;
                  clicks.add(job);

                  for (int j = 0; j < this.targets; j++) {
                     views[j][0] = views[j][0] & ~this.blockedViews[job][j][0];
                     views[j][1] = views[j][1] & ~this.blockedViews[job][j][1];
                  }
               }
            }
         }

         if (clicks.isEmpty()) {
            return null;
         }

         long bit = 1L << (at & 63);
         boolean visited = ((at < 64 ? state.visitedLow : state.visitedHigh) & bit) != 0L;
         double cost = state.cost + (state.at < 0 ? this.startLegCost[at] : this.legCost[state.at][at]) + 24.0 + (visited ? 0.0 : station.constructionTicks());
         ArrayList<AiSchematicBatchPlan.Inner_ConfigStore> stops = new ArrayList<>(state.stops);
         stops.add(new AiSchematicBatchPlan.Inner_ConfigStore(at, List.copyOf(clicks)));
         return new AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil(
            done,
            at,
            cost,
            state.visitedLow | (at < 64 ? bit : 0L),
            state.visitedHigh | (at >= 64 ? bit : 0L),
            views,
            List.copyOf(stops),
            state.supports + (visited ? 0 : station.supports),
            cost + this.remainingLowerBound(done, at)
         );
      }

      private double remainingLowerBound(long done, int at) {
         double reach = 0.0;
         int maxCover = 1;

         for (int job = 0; job < this.targets; job++) {
            if ((this.all & ~done & 1L << job) != 0L) {
               double nearest = 1.0 / 0.0;

               for (int s = 0; s < this.stations.size(); s++) {
                  if ((this.stations.get(s).jobs & 1L << job) != 0L && (this.stations.get(s).blockers[job] & done) == 0L) {
                     nearest = Math.min(nearest, this.legCost[at][s]);
                  }
               }

               reach = Math.max(reach, nearest);
            }
         }

         for (AiSchematicBatchPlan.Inner_BufferHelper station : this.stations) {
            maxCover = Math.max(maxCover, Long.bitCount(station.jobs & ~done));
         }

         int stops = (Long.bitCount(this.all & ~done) + maxCover - 1) / maxCover;
         return reach + stops * 24.0;
      }

      private double costWithContinuation(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil state) {
         return state.cost + (state.at < 0 ? 0.0 : this.continuationCost[state.at]);
      }

      private boolean isBetterCandidate(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil candidate, AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil prior) {
         return prior == null || candidate.supports < prior.supports || candidate.supports == prior.supports && this.costWithContinuation(candidate) < this.costWithContinuation(prior);
      }

      AiSchematicBatchPlan.Inner_StreamUtil getChoice() {
         AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil chosen = this.bestComplete == null ? this.mostCovered : this.bestComplete;
         return new AiSchematicBatchPlan.Inner_StreamUtil(chosen.stops, chosen.done, this.all, this.costWithContinuation(chosen) + Long.bitCount(chosen.done) * 10.0, chosen.supports);
      }

      private static double directionalHeuristic(BlockPos from, BlockPos to) {
         return AiPathfinder.MoveCosts.estimateDirectionalHeuristicCompat(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ());
      }

      private record Inner_DataUtil(long done, int at, long visitedLow, long visitedHigh) {
      }

      private record Inner_StreamUtil(
         long done, int at, double cost, long visitedLow, long visitedHigh, long[][] views, List<AiSchematicBatchPlan.Inner_ConfigStore> stops, int supports, double bound
      ) {
      }
   }

   record Inner_ConfigStore(int station, List<Integer> jobs) {
   }

   static final class Inner_DataUtil {
      private final LongSupplier clock;
      private final long startedAt;
      private long deadlineAt;
      private long startMarkedAt = -9223372036854775808L;

      Inner_DataUtil(LongSupplier clock, long nanos) {
         if (nanos > 0L && nanos <= 10000000000L) {
            this.clock = clock;
            this.startedAt = clock.getAsLong();
            this.deadlineAt = this.startedAt + nanos;
         } else {
            throw new IllegalArgumentException("Planning budget must be at most ten seconds");
         }
      }

      void capDeadline(long nanos) {
         this.deadlineAt = Math.min(this.deadlineAt, this.clock.getAsLong() + Math.max(0L, nanos));
      }

      boolean isExpired() {
         return this.clock.getAsLong() >= this.deadlineAt;
      }

      long remainingNanos() {
         return Math.max(0L, this.deadlineAt - this.clock.getAsLong());
      }

      void start() {
         if (this.startMarkedAt == -9223372036854775808L) {
            this.startMarkedAt = this.clock.getAsLong();
         }
      }

      double elapsedMs() {
         return ((this.startMarkedAt == -9223372036854775808L ? this.clock.getAsLong() : this.startMarkedAt) - this.startedAt) / 1000000.0;
      }
   }

   record Inner_StreamUtil(List<AiSchematicBatchPlan.Inner_ConfigStore> stops, long covered, long required, double ticks, int supports) {
      boolean complete() {
         return this.covered == this.required;
      }

      List<Integer> order() {
         return this.stops.stream().flatMap(stop -> stop.jobs.stream()).toList();
      }

      AiSchematicBatchPlan.Inner_ConfigStore nextStop(long completed) {
         for (AiSchematicBatchPlan.Inner_ConfigStore stop : this.stops) {
            if (stop.jobs.stream().anyMatch(i -> (completed & 1L << i) == 0L)) {
               return stop;
            }
         }

         return null;
      }
   }
}
