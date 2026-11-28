package autismclient.util;

import autismclient.mixin.accessor.AutismBlockStatePredictionAccessor;
import autismclient.mixin.accessor.AutismClientLevelPredictionAccessor;
import autismclient.mixin.accessor.AutismServerVerifiedBlockStateAccessor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class ServerVerifiedBlockState {
   private ServerVerifiedBlockState() {
   }

   public static BlockState serverState(ClientLevel level, BlockPos pos) {
      BlockStatePredictionHandler prediction = ((AutismClientLevelPredictionAccessor)level).autism$getBlockPredictionHandler();
      Object pending = ((AutismBlockStatePredictionAccessor)prediction).autism$getServerVerifiedStates().get(pos.asLong());
      BlockState received = pending == null ? null : ((AutismServerVerifiedBlockStateAccessor)pending).autism$getBlockState();
      return preferReceived(level.getBlockState(pos), received);
   }

   static <T> T preferReceived(T displayed, T received) {
      return received == null ? displayed : received;
   }
}
