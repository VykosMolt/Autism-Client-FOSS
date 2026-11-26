package autismclient.ai.engine;

final class AiGroundContinuity {
   private AiGroundContinuity() {
   }

   static boolean putAt(double var0, double var2) {
      return Math.abs(var0 - var2) <= 0.062501;
   }

   static boolean putAt(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, AiPhysics.Inner_StreamUtil var2) {
      if (!var2.onGround()) {
         return false;
      } else {
         double var3 = var2.y() - var1.y();
         if (!(var3 > 0.600001) && !(var3 < -1.062501)) {
            return Math.abs(var3) <= 0.01 ? true : var2.verticalCollision() && AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var2.x(), var2.y(), var2.z());
         } else {
            return false;
         }
      }
   }

   static boolean putAt(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, AiPhysics.Inner_StreamUtil var2, double var3) {
      if (var2.y() < var3 - 1.0625) {
         return false;
      } else {
         return !var2.onGround() ? var2.vy() <= 0.0 : putAt(var0, var1, var2);
      }
   }
}
