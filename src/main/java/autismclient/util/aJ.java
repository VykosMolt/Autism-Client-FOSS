package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.compat263.Compat263;
import java.util.Locale;
import net.minecraft.util.Util;

public final class aJ {
   public static final String init = "https://discord.com/invite/JZ7XgUCtBu";
   public static final String load = "https://discord.gg/V8GsKP6k5u";

   private aJ() {
   }

   public static void init_V(String url) {
      if (!init_Z(url)) {
         AutismClientAddon.LOG.warn("[Autism] Refused to open non-http(s) URL: {}", url);
      } else {
         try {
            Compat263.openUri(Util.getPlatform(), url);
         } catch (Throwable var2) {
         }
      }
   }

   public static boolean init_Z(String url) {
      if (url == null) {
         return false;
      }

      String lower = url.trim().toLowerCase(Locale.ROOT);
      return lower.startsWith("https://") || lower.startsWith("http://") || lower.startsWith("mailto:");
   }
}
