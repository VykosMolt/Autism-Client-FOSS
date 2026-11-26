package autismclient.ai;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

final class bG {
   private bG() {
   }

   static boolean init(BlockState before, BlockState after, Block crop) {
      return before.is(crop) && B.init_Z(before) && after.isAir();
   }

   static boolean init(BlockState current, ca.Inner_DataUtil update, long brokenRevision, int now, int brokenAt, int latencyMillis, boolean ownClickPending) {
      return !ownClickPending
         && current.isAir()
         && update != null
         && update.revision() > brokenRevision
         && update.state().isAir()
         && !autismclient.util.BlockBreakGraceCalc.init(now, brokenAt, latencyMillis);
   }

   static bG.Inner_DataUtil init(BlockState current, ca.Inner_DataUtil update, long brokenRevision, Block crop, boolean ownReplantClick, int seedsHeld) {
      if (current.is(crop) && update != null && update.revision() > brokenRevision && update.state().is(crop) && !B.init_Z(update.state())) {
         return ownReplantClick ? bG.Inner_DataUtil.load : bG.Inner_DataUtil.read;
      } else {
         return current.isAir() && seedsHeld <= 0 && !ownReplantClick ? bG.Inner_DataUtil.write : bG.Inner_DataUtil.init;
      }
   }

   enum Inner_DataUtil {
      init,
      load,
      read,
      write;
   }
}
