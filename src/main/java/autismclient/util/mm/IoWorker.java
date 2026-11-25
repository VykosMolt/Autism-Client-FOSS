package autismclient.util.mm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class IoWorker {
   private static volatile IoWorker.Inner_DataUtil init;

   private IoWorker() {
   }

   public static void init(String jwt) {
      long iatMs = init(jwt, "iat");
      if (iatMs > 0L) {
         init = new IoWorker.Inner_DataUtil(iatMs, System.nanoTime());
      }
   }

   public static void init(long serverMs) {
      if (serverMs > 0L) {
         init = new IoWorker.Inner_DataUtil(serverMs, System.nanoTime());
      }
   }

   public static long init() {
      IoWorker.Inner_DataUtil a = init;
      return a == null ? System.currentTimeMillis() : a.init + (System.nanoTime() - a.load) / 1000000L;
   }

   private static long init(String jwt, String name) {
      if (jwt != null && !jwt.isEmpty()) {
         try {
            int d1 = jwt.indexOf(46);
            int d2 = jwt.indexOf(46, d1 + 1);
            if (d1 > 0 && d2 > d1) {
               byte[] payload = Base64.getUrlDecoder().decode(jwt.substring(d1 + 1, d2));
               JsonObject o = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
               return o.has(name) ? o.get(name).getAsLong() * 1000L : 0L;
            } else {
               return 0L;
            }
         } catch (Throwable t) {
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   private record Inner_DataUtil(long init, long load) {
   }
}
