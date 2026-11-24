package autismclient.ai;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;

// Preview/paste (cR) and build (TableIndex) transform every chest-like state the same way.
public final class ChestMirrorCheck {
   public static void main(String[] a) {
      SharedConstants.tryDetectVersion();
      Bootstrap.bootStrap();
      int checked = 0;
      int blocks = 0;
      for (Block block : BuiltInRegistries.BLOCK) {
         if (!(block instanceof ChestBlock)) {
            continue;
         }

         blocks++;
         String id = BuiltInRegistries.BLOCK.getKey(block).toString();
         for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            TableIndex.Inner_StreamUtil source = cQ.init_TableIndex_StreamU_BlockState(state);
            for (int rotation = 0; rotation < 360; rotation += 90) {
               for (String mirror : new String[]{"none", "x", "z"}) {
                  var preview = cQ.init_TableIndex_StreamU_BlockState(cQ.init_BlockState_BlockState(state, rotation, mirror)).properties();
                  var build = TableIndex.init_TableIndex_StreamU_TableIndex_StreamU(
                        new TableIndex.Inner_StreamUtil(0, 0, 0, id, source.properties()), rotation, mirror
                     )
                     .properties();
                  if (!preview.equals(build)) {
                     System.out.println("FAIL ChestMirrorCheck: " + state + " rot=" + rotation + " mirror=" + mirror + " preview=" + preview + " build=" + build);
                     System.exit(1);
                  }

                  checked++;
               }
            }
         }
      }

      if (blocks < 2) {
         System.out.println("FAIL ChestMirrorCheck: found only " + blocks + " chest blocks");
         System.exit(1);
      }

      System.out.println("PASS ChestMirrorCheck (" + blocks + " chest blocks, " + checked + " transforms)");
      System.exit(0);
   }
}
