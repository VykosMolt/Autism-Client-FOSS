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

public final class DataUtil {
   public static final long init_J = 750L;
   public static final long load_J = 6000L;
   public static final long read_J = 8000L;
   private final Map<Long, DataUtil.Inner_StreamUtil> init_Map = new LinkedHashMap<>();
   private final Map<Long, Deque<DataUtil.Inner_StreamUtil>> load_Map = new HashMap<>();
   private final Map<Long, Long> read_Map = new HashMap<>();
   private final List<DataUtil.Inner_DataUtil> init_List = new ArrayList<>();
   private final List<DataUtil.Inner_ConfigStore> load_List = new ArrayList<>();

   public void init(BlockPos pos, String after) {
      DataUtil.Inner_StreamUtil old = this.init_Map.get(pos.asLong());
      if (old != null && !old.load.equals(after)) {
         this.init_Map.remove(pos.asLong());
      }

      for (DataUtil.Inner_ConfigStore streak : this.load_List) {
         DataUtil.Inner_StreamUtil settled = streak.init_Map.get(pos.asLong());
         if (settled != null && !settled.load.equals(after)) {
            streak.init_Map.remove(pos.asLong());
            streak.init_DataUtil_BufferHel = null;
         }
      }
   }

   public void init_V_DataUtil_StreamUti(DataUtil.Inner_StreamUtil change) {
      if (change != null && change.init_BlockPos != null && !change.init_Str.equals(change.load)) {
         long chunk = init_J_BlockPos(change.init_BlockPos);
         Deque<DataUtil.Inner_StreamUtil> burst = this.load_Map.computeIfAbsent(chunk, k -> new ArrayDeque<>());
         burst.addLast(change);

         while (!burst.isEmpty() && change.init_J - burst.peekFirst().init_J > 150L) {
            burst.removeFirst();
         }

         if (burst.stream().map(c -> c.init_BlockPos.asLong()).distinct().count() > 8L) {
            this.init_V(change.init_BlockPos, change.init_J + 3500L);
         } else if (this.read_Map.getOrDefault(chunk, 0L) <= change.init_J && !this.init_Z(change.init_BlockPos, change.init_J)) {
            if (this.init_Map.size() >= 1024) {
               this.init_Map.remove(this.init_Map.keySet().iterator().next());
            }

            this.init_Map.put(change.init_BlockPos.asLong(), change);
         }
      }
   }

   public void init_V(BlockPos pos, long until) {
      long chunk = init_J_BlockPos(pos);
      this.read_Map.merge(chunk, until, Math::max);
      this.init_Map.values().removeIf(c -> init_J_BlockPos(c.init_BlockPos) == chunk);
      this.load_List.removeIf(s -> init_J_BlockPos(s.init_BlockPos) == chunk);
   }

   public void init(BlockPos pos, double radius, long until) {
      this.init_List.add(new DataUtil.Inner_DataUtil(pos.immutable(), radius * radius, until));
      this.init_Map.values().removeIf(c -> c.init_BlockPos.distSqr(pos) <= radius * radius);
      this.load_List
         .removeIf(
            s -> s.init_BlockPos.distSqr(pos) <= radius * radius || s.init_Map.values().stream().anyMatch(c -> c.init_BlockPos.distSqr(pos) <= radius * radius)
         );
      if (this.init_List.size() > 1024) {
         this.init_List.subList(0, this.init_List.size() - 1024).clear();
      }
   }

   private boolean init_Z(BlockPos pos, long now) {
      return this.init_List.stream().anyMatch(c -> c.init_J > now && c.init_BlockPos.distSqr(pos) <= c.init_D);
   }

   public void init(long now, BiPredicate<BlockPos, String> unchanged, Predicate<BlockPos> worldCause) {
      this.init_List.removeIf(cx -> cx.init_J <= now);
      this.read_Map.values().removeIf(until -> until <= now);
      this.load_Map.values().removeIf(q -> {
         while (!q.isEmpty() && now - q.peekFirst().init_J > 150L) {
            q.removeFirst();
         }

         return q.isEmpty();
      });

      for (DataUtil.Inner_StreamUtil c : new ArrayList<>(this.init_Map.values())) {
         if (now - c.init_J >= 750L) {
            this.init_Map.remove(c.init_BlockPos.asLong());
            if (now - c.init_J <= 3000L
               && this.read_Map.getOrDefault(init_J_BlockPos(c.init_BlockPos), 0L) <= now
               && !this.init_Z(c.init_BlockPos, now)
               && unchanged.test(c.init_BlockPos, c.load)
               && !worldCause.test(c.init_BlockPos)) {
               DataUtil.Inner_ConfigStore streak = this.load_List
                  .stream()
                  .filter(s -> s.init_BlockPos.distSqr(c.init_BlockPos) <= 64.0 && now - s.init_J <= 6000L)
                  .findFirst()
                  .orElse(null);
               if (streak == null) {
                  if (this.load_List.size() >= 32) {
                     this.load_List.removeFirst();
                  }

                  streak = new DataUtil.Inner_ConfigStore(c.init_BlockPos);
                  this.load_List.add(streak);
               }

               streak.init(c);
            }
         }
      }

      this.load_List.removeIf(s -> now - s.init_J > 14000L);
   }

   public List<DataUtil.Inner_BufferHelper> init_List_J(long now) {
      return this.load_List
         .stream()
         .map(s -> s.init_DataUtil_BufferHel)
         .filter(r -> r != null && r.init_J > now)
         .sorted(Comparator.comparingLong(DataUtil.Inner_BufferHelper::init_J).reversed())
         .limit(2L)
         .toList();
   }

   public void init_V() {
      this.init_Map.clear();
      this.load_Map.clear();
      this.read_Map.clear();
      this.init_List.clear();
      this.load_List.clear();
   }

   public int init_I() {
      return this.init_Map.size();
   }

   private static long init_J_BlockPos(BlockPos p) {
      return (long)(p.getX() >> 4) << 32 ^ p.getZ() >> 4 & 4294967295L;
   }

   public record Inner_BufferHelper(BlockPos init_BlockPos, int init_I, int load, long init_J) {
      public String init_Str() {
         return this.init_I > 0 && this.load > 0 ? "Unexplained block changes" : (this.init_I > 0 ? "Unexplained placements" : "Unexplained breaks");
      }
   }

   public enum Inner_CachePool {
      PLACE,
      BREAK;
   }

   private static final class Inner_ConfigStore {
      final BlockPos init_BlockPos;
      final Map<Long, DataUtil.Inner_StreamUtil> init_Map = new LinkedHashMap<>();
      long init_J = -1L;
      DataUtil.Inner_BufferHelper init_DataUtil_BufferHel;

      Inner_ConfigStore(BlockPos anchor) {
         this.init_BlockPos = anchor;
      }

      void init(DataUtil.Inner_StreamUtil c) {
         this.init_Map.values().removeIf(old -> c.init_J - old.init_J > 6000L);
         if (this.init_J < 0L || c.init_J - this.init_J >= 140L) {
            this.init_J = c.init_J;
            this.init_Map.put(c.init_BlockPos.asLong(), c);
            if (this.init_Map.size() > 32) {
               this.init_Map.remove(this.init_Map.keySet().iterator().next());
            }

            if (this.init_Map.size() >= 4) {
               long first = this.init_Map.values().stream().mapToLong(DataUtil.Inner_StreamUtil::init_J).min().orElse(c.init_J);
               if (c.init_J - first >= 450L) {
                  int placements = (int)this.init_Map.values().stream().filter(e -> e.init_DataUtil_CachePool == DataUtil.Inner_CachePool.PLACE).count();
                  this.init_DataUtil_BufferHel = new DataUtil.Inner_BufferHelper(
                     c.init_BlockPos, placements, this.init_Map.size() - placements, c.init_J + 8000L
                  );
               }
            }
         }
      }
   }

   private record Inner_DataUtil(BlockPos init_BlockPos, double init_D, long init_J) {
   }

   public record Inner_StreamUtil(BlockPos init_BlockPos, String init_Str, String load, DataUtil.Inner_CachePool init_DataUtil_CachePool, long init_J) {
   }
}
