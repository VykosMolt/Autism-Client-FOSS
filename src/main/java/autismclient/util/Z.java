package autismclient.util;

import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;

public final class Z {
   private Z() {
   }

   public static boolean init(BlockGetter world, int ax, int ay, int az, int bx, int by, int bz, boolean jump, double rise) {
      if (!jump && rise > -0.5) {
         return true;
      }

      MutableBlockPos cursor = new MutableBlockPos();

      for (int x = Math.min(ax, bx) - 1; x <= Math.max(ax, bx) + 1; x++) {
         for (int z = Math.min(az, bz) - 1; z <= Math.max(az, bz) + 1; z++) {
            for (int y = Math.min(ay, by) - 2; y <= Math.max(ay, by); y++) {
               if (world.getBlockState(cursor.set(x, y, z)).is(Blocks.FARMLAND)) {
                  return false;
               }
            }
         }
      }

      return true;
   }
}
