package autismclient.ai;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;

/**
 * 3-axis schematic hull: a non-schematic position is considered inside the build only when
 * schematic blocks lie on both sides of it along X, Y, or Z. Exterior bounding-box terrain is
 * left untouched when {@code clearTerrain} is enabled.
 */
public final class AuditBuilderHull {
   private final Map<Long, int[]> alongX = new HashMap<>();
   private final Map<Long, int[]> alongY = new HashMap<>();
   private final Map<Long, int[]> alongZ = new HashMap<>();

   public AuditBuilderHull(Collection<Long> cells) {
      for (long cell : cells) {
         int x = BlockPos.getX(cell);
         int y = BlockPos.getY(cell);
         int z = BlockPos.getZ(cell);
         span(this.alongX, line(y, z), x);
         span(this.alongY, line(x, z), y);
         span(this.alongZ, line(x, y), z);
      }
   }

   public boolean encloses(int x, int y, int z) {
      return between(this.alongX.get(line(y, z)), x)
         || between(this.alongY.get(line(x, z)), y)
         || between(this.alongZ.get(line(x, y)), z);
   }

   private static long line(int a, int b) {
      return (long)a << 32 | b & 4294967295L;
   }

   private static void span(Map<Long, int[]> lines, long line, int at) {
      int[] span = lines.get(line);
      if (span == null) {
         lines.put(line, new int[]{at, at});
      } else {
         span[0] = Math.min(span[0], at);
         span[1] = Math.max(span[1], at);
      }
   }

   private static boolean between(int[] span, int at) {
      return span != null && span[0] < at && at < span[1];
   }
}
