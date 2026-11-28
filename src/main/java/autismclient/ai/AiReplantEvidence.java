package autismclient.ai;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

final class AiReplantEvidence {
   private AiReplantEvidence() {
   }

   static boolean wasHarvested(BlockState before, BlockState after, Block crop) {
      return before.is(crop) && AiFarm.isRipe(before) && after.isAir();
   }

   static boolean init(BlockState current, AiServerEvidence.BlockUpdate update, long brokenRevision, int now, int brokenAt, int latencyMillis, boolean ownClickPending) {
      return !ownClickPending
         && current.isAir()
         && update != null
         && update.revision() > brokenRevision
         && update.state().isAir()
         && !autismclient.util.BlockBreakGraceCalc.inGrace(now, brokenAt, latencyMillis);
   }

   static AiReplantEvidence.ReplantState init(BlockState current, AiServerEvidence.BlockUpdate update, long brokenRevision, Block crop, boolean ownReplantClick, int seedsHeld) {
      if (current.is(crop) && update != null && update.revision() > brokenRevision && update.state().is(crop) && !AiFarm.isRipe(update.state())) {
         return ownReplantClick ? AiReplantEvidence.ReplantState.LOCAL : AiReplantEvidence.ReplantState.EXTERNAL;
      } else {
         return current.isAir() && seedsHeld <= 0 && !ownReplantClick ? AiReplantEvidence.ReplantState.MISSING : AiReplantEvidence.ReplantState.PENDING;
      }
   }

   enum ReplantState {
      PENDING,
      LOCAL,
      EXTERNAL,
      MISSING;
   }
}
