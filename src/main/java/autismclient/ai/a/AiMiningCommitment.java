package autismclient.ai.a;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;

public final class AiMiningCommitment {
   static final int init_I = 256;
   private final Set<Long> init_Set = new LinkedHashSet<>();
   private final Set<Long> load = new LinkedHashSet<>();
   private BlockPos init_BlockPos;
   private long init_J = -1L;

   public void init(BlockPos seed, Collection<BlockPos> observed, Predicate<BlockPos> allowed) {
      this.init_V();
      this.init_BlockPos = seed.immutable();
      this.init_Set.add(seed.asLong());
      if (observed.contains(seed) && allowed.test(seed)) {
         this.load.add(seed.asLong());
      }

      this.init(observed, allowed);
   }

   public void init(Collection<BlockPos> observed, Predicate<BlockPos> allowed) {
      if (this.init_BlockPos != null) {
         Map<Long, BlockPos> pending = new HashMap<>();

         for (BlockPos cell : observed) {
            if (!this.init_Set.contains(cell.asLong()) && allowed.test(cell)) {
               pending.putIfAbsent(cell.asLong(), cell.immutable());
            }
         }

         ArrayDeque<BlockPos> frontier = new ArrayDeque<>();

         for (BlockPos cell : pending.values()) {
            if (this.init(cell)) {
               frontier.add(cell);
            }
         }

         while (!frontier.isEmpty() && this.init_Set.size() < 256) {
            BlockPos cell = frontier.poll();
            long key = cell.asLong();
            if (!this.init_Set.contains(key)) {
               pending.remove(key);
               this.init_Set.add(key);
               this.load.add(key);

               for (int dx = -1; dx <= 1; dx++) {
                  for (int dy = -1; dy <= 1; dy++) {
                     for (int dz = -1; dz <= 1; dz++) {
                        if ((dx | dy | dz) != 0) {
                           BlockPos next = pending.get(BlockPos.asLong(cell.getX() + dx, cell.getY() + dy, cell.getZ() + dz));
                           if (next != null) {
                              frontier.add(next);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean init(BlockPos cell) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if ((dx | dy | dz) != 0 && this.init_Set.contains(BlockPos.asLong(cell.getX() + dx, cell.getY() + dy, cell.getZ() + dz))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public void init(Set<Long> present) {
      this.load.removeIf(cell -> !present.contains(cell));
   }

   public void init(Set<Long> present, Predicate<BlockPos> gone) {
      this.load.removeIf(cell -> !present.contains(cell) && gone.test(BlockPos.of(cell)));
   }

   public boolean init(long now, boolean scanSettled, long grace) {
      if (this.init_BlockPos == null) {
         return true;
      }

      if (this.load.isEmpty() && scanSettled) {
         if (this.init_J < 0L) {
            this.init_J = now;
         }

         return now - this.init_J >= grace;
      } else {
         this.init_J = -1L;
         return false;
      }
   }

   public List<BlockPos> init_List() {
      List<BlockPos> out = new ArrayList<>(this.load.size());

      for (long cell : this.load) {
         out.add(BlockPos.of(cell));
      }

      return out;
   }

   public boolean init_Z() {
      return this.init_BlockPos != null;
   }

   public boolean load_Z() {
      return !this.init_Z();
   }

   public int init_I() {
      return this.load.size();
   }

   public boolean init(long cell) {
      return this.load.contains(cell);
   }

   public boolean load(long cell) {
      return this.init_Set.contains(cell);
   }

   public BlockPos init_BlockPos() {
      return this.init_BlockPos;
   }

   public int load_I() {
      return this.init_Set.size();
   }

   public void init_V() {
      this.init_Set.clear();
      this.load.clear();
      this.init_BlockPos = null;
      this.init_J = -1L;
   }
}
