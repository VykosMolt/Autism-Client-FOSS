package autismclient.ai;

import autismclient.api.ai.AiOperation;
import net.minecraft.core.BlockPos;

public final class AiOps {
   public static void init_V(BlockPos pos) {
      AiSmelt.init(pos);
   }

   private AiOps() {
   }

   public static AiOperation init_AiOperation(BlockPos pos) {
      return AiGameOperations.init("interact", AiJson.init_JsonObject_ObjArr("x", pos.getX(), "y", pos.getY(), "z", pos.getZ()));
   }
}
