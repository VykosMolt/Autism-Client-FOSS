package autismclient.util;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;

public final class PendingCrystalAttackCache {
   private final Map<Object, PendingCrystalAttackCache.PendingAttack> init = new IdentityHashMap<>();

   public void add(Object crystal, BlockPos cell, int tick, int latencyMs) {
      this.init(tick);
      if (this.init.size() >= 16) {
         this.init.clear();
      }

      this.init.put(crystal, new PendingCrystalAttackCache.PendingAttack(cell.immutable(), tick + LatencyTickWindow.windowTicks(latencyMs), false));
   }

   public boolean isPending(Object crystal, int tick) {
      PendingCrystalAttackCache.PendingAttack entry = this.init.get(crystal);
      return entry != null && tick - entry.init_I() < 0;
   }

   public boolean isPendingAt(Object crystal, BlockPos cell, int tick) {
      PendingCrystalAttackCache.PendingAttack entry = this.init.get(crystal);
      return entry != null && !entry.init_Z() && entry.init_BlockPos().equals(cell) && tick - entry.init_I() < 0;
   }

   public void init(BlockPos cell, int tick) {
      this.init(tick);
      this.init
         .replaceAll(
            (crystal, entry) -> entry.init_BlockPos().equals(cell) ? new PendingCrystalAttackCache.PendingAttack(entry.init_BlockPos(), entry.init_I(), true) : entry
         );
   }

   public void init(int tick) {
      this.init.entrySet().removeIf(entry -> tick - entry.getValue().init_I() >= 0);
   }

   public void clear() {
      this.init.clear();
   }

   private record PendingAttack(BlockPos init_BlockPos, int init_I, boolean init_Z) {
   }
}
