package autismclient.util;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;

public final class RenderState {
   private final Map<Object, RenderState.Inner_DataUtil> init = new IdentityHashMap<>();

   public void init(Object crystal, BlockPos cell, int tick, int latencyMs) {
      this.init(tick);
      if (this.init.size() >= 16) {
         this.init.clear();
      }

      this.init.put(crystal, new RenderState.Inner_DataUtil(cell.immutable(), tick + FileCache.init_I_I(latencyMs), false));
   }

   public boolean init(Object crystal, int tick) {
      RenderState.Inner_DataUtil entry = this.init.get(crystal);
      return entry != null && tick - entry.init_I() < 0;
   }

   public boolean init(Object crystal, BlockPos cell, int tick) {
      RenderState.Inner_DataUtil entry = this.init.get(crystal);
      return entry != null && !entry.init_Z() && entry.init_BlockPos().equals(cell) && tick - entry.init_I() < 0;
   }

   public void init(BlockPos cell, int tick) {
      this.init(tick);
      this.init
         .replaceAll(
            (crystal, entry) -> entry.init_BlockPos().equals(cell) ? new RenderState.Inner_DataUtil(entry.init_BlockPos(), entry.init_I(), true) : entry
         );
   }

   public void init(int tick) {
      this.init.entrySet().removeIf(entry -> tick - entry.getValue().init_I() >= 0);
   }

   public void init() {
      this.init.clear();
   }

   private record Inner_DataUtil(BlockPos init_BlockPos, int init_I, boolean init_Z) {
   }
}
