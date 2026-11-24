package autismclient.ai;

import autismclient.api.ai.AiOperation;
import net.minecraft.core.BlockPos;

public final class aF {
   public static void init_V(BlockPos pos) {
      ci.init(pos);
   }

   private aF() {
   }

   public static AiOperation init_AiOperation(BlockPos pos) {
      return R.init("interact", ag.init_JsonObject_ObjArr("x", pos.getX(), "y", pos.getY(), "z", pos.getZ()));
   }
}
