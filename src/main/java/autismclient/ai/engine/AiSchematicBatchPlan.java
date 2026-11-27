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

   static double init(BlockPos feet, Vec3 worker, int useful, int supports, int clearance) {
      if (useful <= 0) {
         return 1.0 / 0.0;
      }

      double travel = AiPathfinder.MoveCosts.estimateDirectionalHeuristicCompat(feet.getX() - Mth.floor(worker.x), feet.getY() - Mth.floor(worker.y), feet.getZ() - Mth.floor(worker.z));
      return (24.0 + travel + supports * 64.0 + clearance * 24.0) / useful;
   }

   static double load(BlockPos feet, Vec3 worker, int useful, int supports, int clearance) {
      return (init(feet, worker, useful, supports, clearance) * useful + useful * 10.0) / 20.0;
   }

   static <T> List<T> init(List<T> candidates, Vec3 start, Function<T, BlockPos> position) {
      if (candidates.size() > 192) {
         throw new IllegalArgumentException("Unbounded source window");
      }

      ArrayList<T> ranked = new ArrayList<>(candidates);
      ranked.sort(Comparator.comparingDouble(cell -> position.apply((T)cell).distToCenterSqr(start)));
      return List.copyOf(ranked.subList(0, Math.min(32, ranked.size())));
   }

   static List<Integer> init(long available, long[] before) {
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

   static <T> void init(List<T> source, int cursor, List<T> batch) {
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

   static <T> Set<Long> init(List<T> source, int cursor, Set<Long> existing, ToLongFunction<T> position) {
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
      private final List<AiSchematicBatchPlan.Inner_BufferHelper> init_List;
      private final int load_I;
      private final long init_J;
      private final double[][] init_DArr2;
      private final double[] init_DArr;
      private final double[] load_DArr;
      private final long[][][] init_JArr3;
      private List<AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil> load_List;
      private final Map<AiSchematicBatchPlan.Inner_CachePool.Inner_DataUtil, AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil> init_Map = new HashMap<>();
      private AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil init_c_CachePool_Stream;
      private AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil load_c_CachePool_Stream;
      private final AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil read_c_CachePool_Stream;
      private AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil write_c_CachePool_Stream;
      private AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil update_c_CachePool_Stream;
      private double init_D;
      private int read_I;
      private int write_I;
      private int update_I;
      private int sync;
      private int flush;
      private boolean init_Z;

      Inner_CachePool(List<AiSchematicBatchPlan.Inner_BufferHelper> stations, Vec3 worker, BlockPos continuation, int targets, long available) {
         if (targets >= 0 && targets <= 32 && stations.size() <= 128 && (available & ~((1L << targets) - 1L)) == 0L) {
            this.init_List = List.copyOf(stations);
            this.load_I = targets;
            int count = stations.size();
            long all = 0L;

            for (AiSchematicBatchPlan.Inner_BufferHelper station : stations) {
               if (station.blockers.length != targets || (station.jobs & ~available) != 0L) {
                  throw new IllegalArgumentException("Unknown route target");
               }

               all |= station.jobs;
            }

            this.init_J = all;
            this.init_DArr2 = new double[count][count];
            this.init_DArr = new double[count];
            this.load_DArr = new double[count];
            BlockPos start = BlockPos.containing(worker);

            for (int i = 0; i < count; i++) {
               this.init_DArr[i] = init_D_BlockPos_BlockPos(start, stations.get(i).feet);
               this.load_DArr[i] = continuation == null ? 0.0 : init_D_BlockPos_BlockPos(stations.get(i).feet, continuation);

               for (int j = 0; j < count; j++) {
                  this.init_DArr2[i][j] = init_D_BlockPos_BlockPos(stations.get(i).feet, stations.get(j).feet);
               }
            }

            this.init_JArr3 = new long[targets][targets][2];
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

                        this.init_JArr3[placed][job][s >>> 6] = this.init_JArr3[placed][job][s >>> 6] | 1L << (s & 63);
                     }
                  }
               }
            }

            this.read_c_CachePool_Stream = new AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil(0L, -1, 0.0, 0L, 0L, views, List.of(), 0, 0.0);
            this.load_List = List.of(this.read_c_CachePool_Stream);
            this.load_c_CachePool_Stream = this.read_c_CachePool_Stream;
            this.write_c_CachePool_Stream = this.read_c_CachePool_Stream;
            this.init_D = 1.0 / 0.0;
            this.init_Z = this.init_J == 0L;
         } else {
            throw new IllegalArgumentException("Unbounded route search");
         }
      }

      boolean init_Z() {
         return this.init_Z;
      }

      void init_V() {
         if (!this.init_Z) {
            if (this.read_I < 3) {
               this.load();
            } else if (this.update_I >= this.load_List.size()) {
               if (!this.init_Map.isEmpty() && ++this.flush < this.load_I) {
                  ArrayList<AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil> ranked = new ArrayList<>(this.init_Map.values());
                  ranked.sort(
                     Comparator.comparingInt(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil::supports)
                        .thenComparingDouble(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil::bound)
                        .thenComparingDouble(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil::cost)
                        .thenComparingLong(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil::done)
                        .thenComparingInt(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil::at)
                  );
                  this.load_List = List.copyOf(ranked.subList(0, Math.min(16, ranked.size())));
                  this.init_Map.clear();
                  this.update_I = this.sync = 0;
               } else {
                  this.init_Z = true;
               }
            } else {
               AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil state = this.load_List.get(this.update_I);
               if (this.sync >= this.init_List.size()) {
                  this.update_I++;
                  this.sync = 0;
               } else {
                  int at = this.sync++;
                  AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil candidate = this.init_c_CachePool_Stream_c_CachePool_Stream_I(state, at);
                  if (candidate != null) {
                     if (Long.bitCount(candidate.done) > Long.bitCount(this.load_c_CachePool_Stream.done)
                        || Long.bitCount(candidate.done) == Long.bitCount(this.load_c_CachePool_Stream.done)
                           && candidate.bound < this.load_c_CachePool_Stream.bound) {
                        this.load_c_CachePool_Stream = candidate;
                     }

                     if (candidate.done == this.init_J) {
                        if (this.init_Z_c_CachePool_Stream_c_CachePool_Stream(candidate, this.init_c_CachePool_Stream)) {
                           this.init_c_CachePool_Stream = candidate;
                        }
                     } else if (this.init_c_CachePool_Stream == null
                        || candidate.supports <= this.init_c_CachePool_Stream.supports
                           && (candidate.supports != this.init_c_CachePool_Stream.supports || !(candidate.bound >= this.init(this.init_c_CachePool_Stream)))) {
                        AiSchematicBatchPlan.Inner_CachePool.Inner_DataUtil key = new AiSchematicBatchPlan.Inner_CachePool.Inner_DataUtil(
                           candidate.done, at, candidate.visitedLow, candidate.visitedHigh
                        );
                        AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil prior = this.init_Map.get(key);
                        if (prior == null || candidate.cost < prior.cost) {
                           this.init_Map.put(key, candidate);
                        }
                     }
                  }
               }
            }
         }
      }

      private void load() {
         if (this.write_I >= this.init_List.size()) {
            if (this.update_c_CachePool_Stream != null) {
               this.write_c_CachePool_Stream = this.update_c_CachePool_Stream;
               if (Long.bitCount(this.write_c_CachePool_Stream.done) > Long.bitCount(this.load_c_CachePool_Stream.done)) {
                  this.load_c_CachePool_Stream = this.write_c_CachePool_Stream;
               }

               if (this.write_c_CachePool_Stream.done != this.init_J) {
                  this.write_I = 0;
                  this.update_c_CachePool_Stream = null;
                  this.init_D = 1.0 / 0.0;
                  return;
               }

               if (this.init_Z_c_CachePool_Stream_c_CachePool_Stream(this.write_c_CachePool_Stream, this.init_c_CachePool_Stream)) {
                  this.init_c_CachePool_Stream = this.write_c_CachePool_Stream;
               }
            }

            this.read_I++;
            this.write_c_CachePool_Stream = this.read_c_CachePool_Stream;
            this.write_I = 0;
            this.update_c_CachePool_Stream = null;
            this.init_D = 1.0 / 0.0;
         } else {
            AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil candidate = this.init_c_CachePool_Stream_c_CachePool_Stream_I(this.write_c_CachePool_Stream, this.write_I++);
            if (candidate != null) {
               if (Long.bitCount(candidate.done) > Long.bitCount(this.load_c_CachePool_Stream.done)) {
                  this.load_c_CachePool_Stream = candidate;
               }

               if (candidate.done == this.init_J && this.init_Z_c_CachePool_Stream_c_CachePool_Stream(candidate, this.init_c_CachePool_Stream)) {
                  this.init_c_CachePool_Stream = candidate;
               }

               int gain = Long.bitCount(candidate.done & ~this.write_c_CachePool_Stream.done);
               double var10000;
               switch (this.read_I) {
                  case 0:
                     var10000 = (candidate.cost - this.write_c_CachePool_Stream.cost) / gain;
                     break;
                  case 1:
                     var10000 = candidate.cost - this.write_c_CachePool_Stream.cost;
                     break;
                  default:
                     var10000 = candidate.bound;
               }

               double value = var10000;
               if (this.update_c_CachePool_Stream == null
                  || candidate.supports < this.update_c_CachePool_Stream.supports
                  || candidate.supports == this.update_c_CachePool_Stream.supports && value < this.init_D) {
                  this.init_D = value;
                  this.update_c_CachePool_Stream = candidate;
               }
            }
         }
      }

      private AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil init_c_CachePool_Stream_c_CachePool_Stream_I(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil state, int at) {
         AiSchematicBatchPlan.Inner_BufferHelper station = this.init_List.get(at);
         if ((station.requires & ~state.done) != 0L) {
            return null;
         }

         long available = station.jobs & ~state.done;

         for (int i = 0; i < this.load_I; i++) {
            if ((station.blockers[i] & state.done) != 0L) {
               available &= ~(1L << i);
            }
         }

         if (available == 0L) {
            return null;
         }

         long[] before = new long[this.load_I];

         for (int job = 0; job < this.load_I; job++) {
            if ((available & 1L << job) != 0L) {
               long blockers = station.blockers[job] & available;

               while (blockers != 0L) {
                  int close = Long.numberOfTrailingZeros(blockers);
                  blockers &= blockers - 1L;
                  before[close] |= 1L << job;
               }
            }
         }

         List<Integer> ordered = AiSchematicBatchPlan.init(available, before);
         long done = state.done;
         long[][] views = new long[this.load_I][2];

         for (int j = 0; j < this.load_I; j++) {
            views[j] = (long[])state.views[j].clone();
         }

         ArrayList<Integer> clicks = new ArrayList<>();

         for (int job : ordered) {
            if ((station.blockers[job] & done) == 0L) {
               long after = done | 1L << job;
               boolean safe = true;

               for (int j = 0; j < this.load_I; j++) {
                  if ((this.init_J & ~after & 1L << j) != 0L
                     && (views[j][0] & ~this.init_JArr3[job][j][0]) == 0L
                     && (views[j][1] & ~this.init_JArr3[job][j][1]) == 0L) {
                     safe = false;
                     break;
                  }
               }

               if (safe) {
                  done = after;
                  clicks.add(job);

                  for (int j = 0; j < this.load_I; j++) {
                     views[j][0] = views[j][0] & ~this.init_JArr3[job][j][0];
                     views[j][1] = views[j][1] & ~this.init_JArr3[job][j][1];
                  }
               }
            }
         }

         if (clicks.isEmpty()) {
            return null;
         }

         long bit = 1L << (at & 63);
         boolean visited = ((at < 64 ? state.visitedLow : state.visitedHigh) & bit) != 0L;
         double cost = state.cost + (state.at < 0 ? this.init_DArr[at] : this.init_DArr2[state.at][at]) + 24.0 + (visited ? 0.0 : station.constructionTicks());
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
            cost + this.init_D_J_I(done, at)
         );
      }

      private double init_D_J_I(long done, int at) {
         double reach = 0.0;
         int maxCover = 1;

         for (int job = 0; job < this.load_I; job++) {
            if ((this.init_J & ~done & 1L << job) != 0L) {
               double nearest = 1.0 / 0.0;

               for (int s = 0; s < this.init_List.size(); s++) {
                  if ((this.init_List.get(s).jobs & 1L << job) != 0L && (this.init_List.get(s).blockers[job] & done) == 0L) {
                     nearest = Math.min(nearest, this.init_DArr2[at][s]);
                  }
               }

               reach = Math.max(reach, nearest);
            }
         }

         for (AiSchematicBatchPlan.Inner_BufferHelper station : this.init_List) {
            maxCover = Math.max(maxCover, Long.bitCount(station.jobs & ~done));
         }

         int stops = (Long.bitCount(this.init_J & ~done) + maxCover - 1) / maxCover;
         return reach + stops * 24.0;
      }

      private double init(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil state) {
         return state.cost + (state.at < 0 ? 0.0 : this.load_DArr[state.at]);
      }

      private boolean init_Z_c_CachePool_Stream_c_CachePool_Stream(AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil candidate, AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil prior) {
         return prior == null || candidate.supports < prior.supports || candidate.supports == prior.supports && this.init(candidate) < this.init(prior);
      }

      AiSchematicBatchPlan.Inner_StreamUtil init_c_StreamUtil() {
         AiSchematicBatchPlan.Inner_CachePool.Inner_StreamUtil chosen = this.init_c_CachePool_Stream == null ? this.load_c_CachePool_Stream : this.init_c_CachePool_Stream;
         return new AiSchematicBatchPlan.Inner_StreamUtil(chosen.stops, chosen.done, this.init_J, this.init(chosen) + Long.bitCount(chosen.done) * 10.0, chosen.supports);
      }

      private static double init_D_BlockPos_BlockPos(BlockPos from, BlockPos to) {
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
      private final LongSupplier init_LongSupplier;
      private final long init_J;
      private long load;
      private long read = -9223372036854775808L;

      Inner_DataUtil(LongSupplier clock, long nanos) {
         if (nanos > 0L && nanos <= 10000000000L) {
            this.init_LongSupplier = clock;
            this.init_J = clock.getAsLong();
            this.load = this.init_J + nanos;
         } else {
            throw new IllegalArgumentException("Planning budget must be at most ten seconds");
         }
      }

      void init(long nanos) {
         this.load = Math.min(this.load, this.init_LongSupplier.getAsLong() + Math.max(0L, nanos));
      }

      boolean init_Z() {
         return this.init_LongSupplier.getAsLong() >= this.load;
      }

      long init_J() {
         return Math.max(0L, this.load - this.init_LongSupplier.getAsLong());
      }

      void init_V() {
         if (this.read == -9223372036854775808L) {
            this.read = this.init_LongSupplier.getAsLong();
         }
      }

      double init_D() {
         return ((this.read == -9223372036854775808L ? this.init_LongSupplier.getAsLong() : this.read) - this.init_J) / 1000000.0;
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
