package autismclient.util.oresim;

import java.util.function.LongPredicate;
import net.minecraft.world.level.ChunkPos;

final class NearestChunkSelector {
   private NearestChunkSelector() {
   }

   static NearestChunkSelector.Inner_DataUtil select(
      double playerX, double playerZ, int centerChunkX, int centerChunkZ, int radius, LongPredicate completed
   ) {
      NearestChunkSelector.Inner_DataUtil nearest = null;

      for (int chunkX = centerChunkX - radius; chunkX <= centerChunkX + radius; chunkX++) {
         for (int chunkZ = centerChunkZ - radius; chunkZ <= centerChunkZ + radius; chunkZ++) {
            if (!completed.test(ChunkPos.pack(chunkX, chunkZ))) {
               double distance = init(playerX, playerZ, chunkX, chunkZ);
               if (nearest == null || init_I_D(distance, chunkX, chunkZ, nearest.init_D(), nearest.init_I(), nearest.load()) < 0) {
                  nearest = new NearestChunkSelector.Inner_DataUtil(chunkX, chunkZ, distance);
               }
            }
         }
      }

      return nearest;
   }

   static boolean init(double playerX, double playerZ, int chunkX, int chunkZ, NearestChunkSelector.Inner_DataUtil nearestMissing) {
      return nearestMissing == null
         ? true
         : init_I_D(init(playerX, playerZ, chunkX, chunkZ), chunkX, chunkZ, nearestMissing.init_D(), nearestMissing.init_I(), nearestMissing.load()) <= 0;
   }

   static double init(double x, double z, int chunkX, int chunkZ) {
      double minX = chunkX * 16.0 + 0.5;
      double minZ = chunkZ * 16.0 + 0.5;
      double maxX = (chunkX + 1) * 16.0 - 0.5;
      double maxZ = (chunkZ + 1) * 16.0 - 0.5;
      double dx = x < minX ? minX - x : (x > maxX ? x - maxX : 0.0);
      double dz = z < minZ ? minZ - z : (z > maxZ ? z - maxZ : 0.0);
      return dx * dx + dz * dz;
   }

   static int init_I_D(double leftDistance, int leftX, int leftZ, double rightDistance, int rightX, int rightZ) {
      int distanceOrder = Double.compare(leftDistance, rightDistance);
      if (distanceOrder != 0) {
         return distanceOrder;
      }

      int xOrder = Integer.compare(leftX, rightX);
      return xOrder != 0 ? xOrder : Integer.compare(leftZ, rightZ);
   }

   record Inner_DataUtil(int init_I, int load, double init_D) {
   }
}
