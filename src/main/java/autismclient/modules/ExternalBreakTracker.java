package autismclient.modules;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;

final class ExternalBreakTracker {
   private final Set<BlockPos> solidCells = new HashSet<>();
   private final Set<BlockPos> externallyBroken = new HashSet<>();

   void onBlockEdit(BlockPos pos, boolean solid, boolean ownBreak) {
      if (solid) {
         this.solidCells.add(pos.immutable());
         this.externallyBroken.remove(pos);
      } else if (this.solidCells.remove(pos)) {
         if (ownBreak) {
            this.externallyBroken.remove(pos);
         } else {
            this.externallyBroken.add(pos.immutable());
         }
      }
   }

   boolean wasExternallyBroken(BlockPos pos) {
      return this.externallyBroken.contains(pos);
   }

   void clear() {
      this.solidCells.clear();
      this.externallyBroken.clear();
   }
}
