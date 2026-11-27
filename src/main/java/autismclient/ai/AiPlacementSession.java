package autismclient.ai;

import net.minecraft.core.BlockPos;

abstract class AiPlacementSession {
   abstract boolean isReadyToConfirm();

   abstract boolean isActive();

   abstract BlockPos getOrigin();

   abstract int getRotation();

   abstract String getMirror();

   abstract void moveToAim(boolean var1);

   abstract void init(double var1, double var3, double var5, double var7, double var9);

   abstract void snapToGround();

   abstract void init(int var1, int var2, int var3);

   abstract void rotate(int var1);

   abstract void cycleMirror();

   abstract void init(BlockPos var1, int var2, String var3);

   abstract void cancel();
}
