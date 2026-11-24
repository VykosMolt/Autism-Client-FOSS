package autismclient.util;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

public final class UtilCore {
   private static final boolean init_Z = !System.getProperty("autism.combat.server.dir", "").isEmpty();
   private static long init_J;

   private UtilCore() {
   }

   public static void init_V() {
      if (init_Z && ++init_J == 40L) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.gameDirectory.toPath().toString().contains("autism-ai-qualification-")) {
            Screenshot.grab(
               new File(System.getProperty("autism.combat.server.dir")), "sword-block-pose.png", mc.gameRenderer.mainRenderTarget(), 1, ignored -> {}
            );
         }
      }
   }

   public static long init_J() {
      return init_J;
   }
}
