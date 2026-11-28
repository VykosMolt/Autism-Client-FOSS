package autismclient.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;

public final class UnexplainedBlockTracker {
   public static final long init_J = 750L;
   public static final long load_J = 6000L;
   public static final long ALERT_DURATION_MS = 8000L;
   private final Map<Long, UnexplainedBlockTracker.Inner_StreamUtil> changes = new LinkedHashMap<>();
   private final Map<Long, Deque<UnexplainedBlockTracker.Inner_StreamUtil>> bursts = new HashMap<>();
   private final Map<Long, Long> read_Map = new HashMap<>();
   private final List<UnexplainedBlockTracker.Inner_DataUtil> explanations = new ArrayList<>();
   private final List<UnexplainedBlockTracker.Inner_ConfigStore> streaks = new ArrayList<>();

   public void init(BlockPos pos, String after) {
      UnexplainedBlockTracker.Inner_StreamUtil old = this.changes.get(pos.asLong());
      if (old != null && !old.load.equals(after)) {
         this.changes.remove(pos.asLong());
      }

      for (UnexplainedBlockTracker.Inner_ConfigStore streak : this.streaks) {
         UnexplainedBlockTracker.Inner_StreamUtil settled = streak.changes.get(pos.asLong());
         if (settled != null && !settled.load.equals(after)) {
            streak.changes.remove(pos.asLong());
            streak.init_DataUtil_BufferHel = null;
         }
      }
   }

   public void onChange(UnexplainedBlockTracker.Inner_StreamUtil change) {
      if (change != null && change.pos != null && !change.init_Str.equals(change.load)) {
         long chunk = chunkKey(change.pos);
         Deque<UnexplainedBlockTracker.Inner_StreamUtil> burst = this.bursts.computeIfAbsent(chunk, k -> new ArrayDeque<>());
         burst.addLast(change);

         while (!burst.isEmpty() && change.time - burst.peekFirst().time > 150L) {
            burst.removeFirst();
         }

         if (burst.stream().map(c -> c.pos.asLong()).distinct().count() > 8L) {
            this.suppress(change.pos, change.time + 3500L);
         } else if (this.read_Map.getOrDefault(chunk, 0L) <= change.time && !this.isExplained(change.pos, change.time)) {
            if (this.changes.size() >= 1024) {
               this.changes.remove(this.changes.keySet().iterator().next());
            }

            this.changes.put(change.pos.asLong(), change);
         }
      }
   }

   public void suppress(BlockPos pos, long until) {
      long chunk = chunkKey(pos);
      this.read_Map.merge(chunk, until, Math::max);
      this.changes.values().removeIf(c -> chunkKey(c.pos) == chunk);
      this.streaks.removeIf(s -> chunkKey(s.origin) == chunk);
   }

   public void init(BlockPos pos, double radius, long until) {
      this.explanations.add(new UnexplainedBlockTracker.Inner_DataUtil(pos.immutable(), radius * radius, until));
      this.changes.values().removeIf(c -> c.pos.distSqr(pos) <= radius * radius);
      this.streaks
         .removeIf(
            s -> s.origin.distSqr(pos) <= radius * radius || s.changes.values().stream().anyMatch(c -> c.pos.distSqr(pos) <= radius * radius)
         );
      if (this.explanations.size() > 1024) {
         this.explanations.subList(0, this.explanations.size() - 1024).clear();
      }
   }

   private boolean isExplained(BlockPos pos, long now) {
      return this.explanations.stream().anyMatch(c -> c.until > now && c.pos.distSqr(pos) <= c.radius);
   }

   public void init(long now, BiPredicate<BlockPos, String> unchanged, Predicate<BlockPos> worldCause) {
      this.explanations.removeIf(cx -> cx.until <= now);
      this.read_Map.values().removeIf(until -> until <= now);
      this.bursts.values().removeIf(q -> {
         while (!q.isEmpty() && now - q.peekFirst().time > 150L) {
            q.removeFirst();
         }

         return q.isEmpty();
      });

      for (UnexplainedBlockTracker.Inner_StreamUtil c : new ArrayList<>(this.changes.values())) {
         if (now - c.time >= 750L) {
            this.changes.remove(c.pos.asLong());
            if (now - c.time <= 3000L
               && this.read_Map.getOrDefault(chunkKey(c.pos), 0L) <= now
               && !this.isExplained(c.pos, now)
               && unchanged.test(c.pos, c.load)
               && !worldCause.test(c.pos)) {
               UnexplainedBlockTracker.Inner_ConfigStore streak = this.streaks
                  .stream()
                  .filter(s -> s.origin.distSqr(c.pos) <= 64.0 && now - s.init_J <= 6000L)
                  .findFirst()
                  .orElse(null);
               if (streak == null) {
                  if (this.streaks.size() >= 32) {
                     this.streaks.removeFirst();
                  }

                  streak = new UnexplainedBlockTracker.Inner_ConfigStore(c.pos);
                  this.streaks.add(streak);
               }

               streak.recordChange(c);
            }
         }
      }

      this.streaks.removeIf(s -> now - s.init_J > 14000L);
   }

   public List<UnexplainedBlockTracker.Inner_BufferHelper> activeReadings(long now) {
      return this.streaks
         .stream()
         .map(s -> s.init_DataUtil_BufferHel)
         .filter(r -> r != null && r.until > now)
         .sorted(Comparator.comparingLong(UnexplainedBlockTracker.Inner_BufferHelper::until).reversed())
         .limit(2L)
         .toList();
   }

   public void clear() {
      this.changes.clear();
      this.bursts.clear();
      this.read_Map.clear();
      this.explanations.clear();
      this.streaks.clear();
   }

   public int changeCount() {
      return this.changes.size();
   }

   private static long chunkKey(BlockPos p) {
      return (long)(p.getX() >> 4) << 32 ^ p.getZ() >> 4 & 4294967295L;
   }

   public record Inner_BufferHelper(BlockPos pos, int init_I, int load, long until) {
      public String label() {
         return this.init_I > 0 && this.load > 0 ? "Unexplained block changes" : (this.init_I > 0 ? "Unexplained placements" : "Unexplained breaks");
      }
   }

   public enum Inner_CachePool {
      PLACE,
      BREAK;
   }

   private static final class Inner_ConfigStore {
      final BlockPos origin;
      final Map<Long, UnexplainedBlockTracker.Inner_StreamUtil> changes = new LinkedHashMap<>();
      long init_J = -1L;
      UnexplainedBlockTracker.Inner_BufferHelper init_DataUtil_BufferHel;

      Inner_ConfigStore(BlockPos anchor) {
         this.origin = anchor;
      }

      void recordChange(UnexplainedBlockTracker.Inner_StreamUtil c) {
         this.changes.values().removeIf(old -> c.time - old.time > 6000L);
         if (this.init_J < 0L || c.time - this.init_J >= 140L) {
            this.init_J = c.time;
            this.changes.put(c.pos.asLong(), c);
            if (this.changes.size() > 32) {
               this.changes.remove(this.changes.keySet().iterator().next());
            }

            if (this.changes.size() >= 4) {
               long first = this.changes.values().stream().mapToLong(UnexplainedBlockTracker.Inner_StreamUtil::time).min().orElse(c.time);
               if (c.time - first >= 450L) {
                  int placements = (int)this.changes.values().stream().filter(e -> e.init_DataUtil_CachePool == UnexplainedBlockTracker.Inner_CachePool.PLACE).count();
                  this.init_DataUtil_BufferHel = new UnexplainedBlockTracker.Inner_BufferHelper(
                     c.pos, placements, this.changes.size() - placements, c.time + 8000L
                  );
               }
            }
         }
      }
   }

   private record Inner_DataUtil(BlockPos pos, double radius, long until) {
   }

   public record Inner_StreamUtil(BlockPos pos, String init_Str, String load, UnexplainedBlockTracker.Inner_CachePool init_DataUtil_CachePool, long time) {
   }
}
