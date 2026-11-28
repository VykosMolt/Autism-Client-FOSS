package autismclient.util;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;

public final class PendingCrystalAttackCache {
   private final Map<Object, PendingCrystalAttackCache.PendingAttack> pending = new IdentityHashMap<>();

   public void add(Object crystal, BlockPos cell, int tick, int latencyMs) {
      this.expire(tick);
      if (this.pending.size() >= 16) {
         this.pending.clear();
      }

      this.pending.put(crystal, new PendingCrystalAttackCache.PendingAttack(cell.immutable(), tick + LatencyTickWindow.windowTicks(latencyMs), false));
   }

   public boolean isPending(Object crystal, int tick) {
      PendingCrystalAttackCache.PendingAttack entry = this.pending.get(crystal);
      return entry != null && tick - entry.expiresTick() < 0;
   }

   public boolean isPendingAt(Object crystal, BlockPos cell, int tick) {
      PendingCrystalAttackCache.PendingAttack entry = this.pending.get(crystal);
      return entry != null && !entry.init_Z() && entry.cell().equals(cell) && tick - entry.expiresTick() < 0;
   }

   public void markHit(BlockPos cell, int tick) {
      this.expire(tick);
      this.pending
         .replaceAll(
            (crystal, entry) -> entry.cell().equals(cell) ? new PendingCrystalAttackCache.PendingAttack(entry.cell(), entry.expiresTick(), true) : entry
         );
   }

   public void expire(int tick) {
      this.pending.entrySet().removeIf(entry -> tick - entry.getValue().expiresTick() >= 0);
   }

   public void clear() {
      this.pending.clear();
   }

   private record PendingAttack(BlockPos cell, int expiresTick, boolean init_Z) {
   }
}
