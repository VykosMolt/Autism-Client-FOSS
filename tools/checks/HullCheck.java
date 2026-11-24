import autismclient.ai.AuditBuilderHull;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;

// A skipper-like shape: a one-block-thick wall 14 wide and 154 tall with a hollow 14x7x22 box on top.
public final class HullCheck {
   public static void main(String[] a) {
      List<Long> cells = new ArrayList<>();
      for (int y = 0; y < 154; y++) {
         for (int z = 3; z <= 16; z++) {
            cells.add(BlockPos.asLong(4, y, z));
         }
      }

      for (int y = 154; y <= 160; y++) {
         for (int x = 0; x <= 13; x++) {
            for (int z = 0; z <= 21; z++) {
               if (x == 0 || x == 13 || z == 0 || z == 21 || y == 154 || y == 160) {
                  cells.add(BlockPos.asLong(x, y, z));
               }
            }
         }
      }

      AuditBuilderHull hull = new AuditBuilderHull(cells);
      check(!hull.encloses(3, 7, 6), "dirt pillar touching the wall is outside");
      check(!hull.encloses(1, 25, 14), "dirt three blocks from the wall is outside");
      check(!hull.encloses(4, 20, 2), "block past the end of the wall is outside");
      check(!hull.encloses(6, -1, 8), "ground under the build is outside");
      check(!hull.encloses(6, 161, 8), "block above the roof is outside");
      check(hull.encloses(6, 157, 8), "inside the hollow box is inside");
      check(hull.encloses(1, 155, 1), "corner of the box interior is inside");
      AuditBuilderHull room = new AuditBuilderHull(List.of(BlockPos.asLong(-5, 70, 100), BlockPos.asLong(5, 70, 100)));
      check(room.encloses(0, 70, 100) && !room.encloses(0, 71, 100) && !room.encloses(6, 70, 100), "negative coordinates and line ends");
      System.out.println("PASS HullCheck");
   }

   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL HullCheck: " + what);
         System.exit(1);
      }
   }
}
