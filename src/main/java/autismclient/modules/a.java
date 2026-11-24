package autismclient.modules;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;

final class a {
   private final Set<BlockPos> init = new HashSet<>();
   private final Set<BlockPos> load = new HashSet<>();

   void init(BlockPos pos, boolean solid, boolean ownBreak) {
      if (solid) {
         this.init.add(pos.immutable());
         this.load.remove(pos);
      } else if (this.init.remove(pos)) {
         if (ownBreak) {
            this.load.remove(pos);
         } else {
            this.load.add(pos.immutable());
         }
      }
   }

   boolean init(BlockPos pos) {
      return this.load.contains(pos);
   }

   void init() {
      this.init.clear();
      this.load.clear();
   }
}
