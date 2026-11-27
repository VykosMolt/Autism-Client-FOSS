package autismclient.ai;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;

final class AiPlacementSearch {
   private final List<AiPlacementSearch.Inner_DataUtil> init_List;
   private final AiPlacementSearch.Inner_ConfigStore init_aQ_ConfigStore;
   private final boolean init_Z;
   private final Map<List<AiBuilder.CellSpec>, AiPlacementSearch.Inner_BufferHelper> init_Map = new IdentityHashMap<>();
   private int init_I;
   private int load_I;
   private int read;
   private int write;
   private int update;
   private int sync;
   private int flush;
   private int close;
   private int open;
   private int poll;
   private int push;
   private Map<Long, AiBuilder.CellSpec> load_Map;
   private AiPlacementSearch.Inner_CachePool init_aQ_CachePool;
   private boolean load_Z;
   private long init_J;

   AiPlacementSearch(List<AiPlacementSearch.Inner_DataUtil> candidates, AiPlacementSearch.Inner_ConfigStore world, boolean clearTerrain) {
      this.init_List = List.copyOf(candidates);
      this.init_aQ_ConfigStore = world;
      this.init_Z = clearTerrain;
   }

   void init(int budget, long deadline) {
      while (!this.load_Z && budget-- > 0 && System.nanoTime() < deadline) {
         if (this.load_Map == null) {
            if (this.init_I >= this.init_List.size()) {
               this.load_Z = true;
               break;
            }

            AiPlacementSearch.Inner_DataUtil c = this.init_List.get(this.init_I);
            AiPlacementSearch.Inner_BufferHelper template = this.init_Map.get(c.cells());
            if (template == null) {
               Map<Long, AiBuilder.CellSpec> map = new HashMap<>();
               int w = 0;
               int h = 0;
               int d = 0;

               for (AiBuilder.CellSpec cell : c.cells()) {
                  map.put(init(cell.dx(), cell.dy(), cell.dz()), cell);
                  w = Math.max(w, cell.dx() + 1);
                  h = Math.max(h, cell.dy() + 1);
                  d = Math.max(d, cell.dz() + 1);
               }

               template = new AiPlacementSearch.Inner_BufferHelper(map, w, h, d);
               this.init_Map.put(c.cells(), template);
            }

            this.load_Map = template.cells();
            this.update = template.width();
            this.sync = template.height();
            this.flush = template.depth();
            this.load_I = this.read = this.write = this.close = this.open = this.poll = this.push = 0;
            if (this.load_Map.isEmpty() || (long)this.update * this.sync * this.flush > 1000000L) {
               this.init_V();
               continue;
            }
         }

         AiPlacementSearch.Inner_DataUtil candidate = this.init_List.get(this.init_I);
         AiBuilder.CellSpec cell = this.load_Map.get(init(this.load_I, this.read, this.write));
         BlockPos pos = candidate.origin().offset(this.load_I, this.read, this.write);
         AiPlacementSearch.Inner_StreamUtil edit = this.init_aQ_ConfigStore.classify(pos, cell);
         this.init_J++;
         if (edit == AiPlacementSearch.Inner_StreamUtil.UNSAFE) {
            this.init_V();
         } else {
            if (cell != null) {
               if (edit == AiPlacementSearch.Inner_StreamUtil.MATCH) {
                  this.push++;
               } else {
                  this.close++;
                  if (edit == AiPlacementSearch.Inner_StreamUtil.BREAK) {
                     this.open++;
                  }

                  if (!this.load_Map.containsKey(init(this.load_I, this.read - 1, this.write)) && this.init_aQ_ConfigStore.init_Z(pos, cell)) {
                     this.poll++;
                  }
               }
            } else if (this.init_Z && edit == AiPlacementSearch.Inner_StreamUtil.BREAK) {
               this.open++;
            }

            if (this.init_aQ_CachePool != null && this.close + 3L * this.open + 2L * this.poll > this.init_aQ_CachePool.work()) {
               this.init_V();
            } else {
               if (++this.load_I == this.update) {
                  this.load_I = 0;
                  if (++this.write == this.flush) {
                     this.write = 0;
                     this.read++;
                  }
               }

               if (this.read == this.sync) {
                  AiPlacementSearch.Inner_CachePool score = new AiPlacementSearch.Inner_CachePool(candidate, this.close, this.open, this.poll, this.push);
                  if (this.init_aQ_CachePool == null || score.compareTo(this.init_aQ_CachePool) < 0) {
                     this.init_aQ_CachePool = score;
                  }

                  this.init_V();
               }
            }
         }
      }
   }

   private void init_V() {
      this.init_I++;
      this.load_Map = null;
   }

   private static long init(int x, int y, int z) {
      return (x & 65535L) << 32 | (y & 65535L) << 16 | z & 65535L;
   }

   AiPlacementSearch.Inner_CachePool getResult() {
      return this.init_aQ_CachePool;
   }

   boolean isDone() {
      return this.load_Z;
   }

   int comparedCount() {
      return Math.min(this.init_I, this.init_List.size());
   }

   int load() {
      return this.init_List.size();
   }

   long init_J() {
      return this.init_J;
   }

   private record Inner_BufferHelper(Map<Long, AiBuilder.CellSpec> cells, int width, int height, int depth) {
   }

   record Inner_CachePool(AiPlacementSearch.Inner_DataUtil candidate, int place, int remove, int support, int matched) implements Comparable<AiPlacementSearch.Inner_CachePool> {
      long work() {
         return this.place + 3L * this.remove + 2L * this.support;
      }

      public int compareTo(AiPlacementSearch.Inner_CachePool other) {
         int cost = Long.compare(this.work(), other.work());
         return cost != 0 ? cost : Double.compare(this.candidate.distance(), other.candidate.distance());
      }
   }

   interface Inner_ConfigStore {
      AiPlacementSearch.Inner_StreamUtil classify(BlockPos var1, AiBuilder.CellSpec var2);

      boolean init_Z(BlockPos var1, AiBuilder.CellSpec var2);
   }

   record Inner_DataUtil(BlockPos origin, int rotation, List<AiBuilder.CellSpec> cells, double distance) {
   }

   enum Inner_StreamUtil {
      MATCH,
      PLACE,
      BREAK,
      UNSAFE;
   }
}
