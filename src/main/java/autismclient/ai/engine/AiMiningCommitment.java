package autismclient.ai.engine;

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
   private final Set<Long> commitment = new LinkedHashSet<>();
   private final Set<Long> pendingCells = new LinkedHashSet<>();
   private BlockPos anchor;
   private long emptySince = -1L;

   public void begin(BlockPos seed, Collection<BlockPos> observed, Predicate<BlockPos> allowed) {
      this.clear();
      this.anchor = seed.immutable();
      this.commitment.add(seed.asLong());
      if (observed.contains(seed) && allowed.test(seed)) {
         this.pendingCells.add(seed.asLong());
      }

      this.extend(observed, allowed);
   }

   public void extend(Collection<BlockPos> observed, Predicate<BlockPos> allowed) {
      if (this.anchor != null) {
         Map<Long, BlockPos> pending = new HashMap<>();

         for (BlockPos cell : observed) {
            if (!this.commitment.contains(cell.asLong()) && allowed.test(cell)) {
               pending.putIfAbsent(cell.asLong(), cell.immutable());
            }
         }

         ArrayDeque<BlockPos> frontier = new ArrayDeque<>();

         for (BlockPos cell : pending.values()) {
            if (this.touchesCommitment(cell)) {
               frontier.add(cell);
            }
         }

         while (!frontier.isEmpty() && this.commitment.size() < 256) {
            BlockPos cell = frontier.poll();
            long key = cell.asLong();
            if (!this.commitment.contains(key)) {
               pending.remove(key);
               this.commitment.add(key);
               this.pendingCells.add(key);

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

   private boolean touchesCommitment(BlockPos cell) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if ((dx | dy | dz) != 0 && this.commitment.contains(BlockPos.asLong(cell.getX() + dx, cell.getY() + dy, cell.getZ() + dz))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public void retainPresent(Set<Long> present) {
      this.pendingCells.removeIf(cell -> !present.contains(cell));
   }

   public void dropMissing(Set<Long> present, Predicate<BlockPos> gone) {
      this.pendingCells.removeIf(cell -> !present.contains(cell) && gone.test(BlockPos.of(cell)));
   }

   public boolean isFinished(long now, boolean scanSettled, long grace) {
      if (this.anchor == null) {
         return true;
      }

      if (this.pendingCells.isEmpty() && scanSettled) {
         if (this.emptySince < 0L) {
            this.emptySince = now;
         }

         return now - this.emptySince >= grace;
      } else {
         this.emptySince = -1L;
         return false;
      }
   }

   public List<BlockPos> remainingCells() {
      List<BlockPos> out = new ArrayList<>(this.pendingCells.size());

      for (long cell : this.pendingCells) {
         out.add(BlockPos.of(cell));
      }

      return out;
   }

   public boolean isActive() {
      return this.anchor != null;
   }

   public boolean isIdle() {
      return !this.isActive();
   }

   public int remainingCount() {
      return this.pendingCells.size();
   }

   public boolean isPending(long cell) {
      return this.pendingCells.contains(cell);
   }

   public boolean isCommitted(long cell) {
      return this.commitment.contains(cell);
   }

   public BlockPos getAnchor() {
      return this.anchor;
   }

   public int committedCount() {
      return this.commitment.size();
   }

   public void clear() {
      this.commitment.clear();
      this.pendingCells.clear();
      this.anchor = null;
      this.emptySince = -1L;
   }
}
