package autismclient.ai.a;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;

public class JournalCacheCheck {
   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL JournalCacheCheck: " + what);
         System.exit(1);
      }
   }

   public static void main(String[] args) throws Exception {
      SharedConstants.tryDetectVersion();
      Bootstrap.bootStrap();
      Path dir = Files.createTempDirectory("journals");
      a.init_V();
      Path live = dir.resolve("live.json");
      a.Inner_StreamUtil open = a.init(live, List.of());
      BlockPos pos = new BlockPos(1, 2, 3);
      a.init(pos, Blocks.DIRT);
      check(open.init(pos) == Blocks.DIRT, "live scope records the scaffold");

      for (int i = 0; i < 70; i++) {
         Path other = dir.resolve("other-" + i + ".json");
         a.init(other, List.of()).close();
         check(Files.isRegularFile(other), "close() leaves the journal written");
      }

      // The live journal was never written, so a re-read would come back empty.
      check(!Files.exists(live), "live journal is still unsaved");
      a.Inner_StreamUtil again = a.init(live, List.of());
      check(again.init(pos) == Blocks.DIRT, "cache must not evict a journal a live scope still uses");
      System.out.println("PASS JournalCacheCheck");
   }
}
