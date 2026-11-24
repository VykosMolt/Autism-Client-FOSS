package autismclient.util.mm.guardian.impl;

import autismclient.e.DataUtil;
import autismclient.util.mm.guardian.Guardians;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class LobbyUnwrap {
   private LobbyUnwrap() {
   }

   public static byte[] apply(byte[] original, JsonObject b) {
      try {
         String wrapped = b.has(
                  DataUtil.init(
                     new int[]{
                        2141306587, 2141306544, 1955994851, 506295365, -1484713200, 1218532847, -880189847, -1516084594, 246021305, -963273670, 203288558
                     }
                  )
               )
               && !b.get(
                     DataUtil.init(
                        new int[]{
                           2141306589, 2141306550, 1955994785, 506297287, -1484653486, 1220379885, -822931673, 258922992, -563325193, -283215356, -189738580
                        }
                     )
                  )
                  .isJsonNull()
            ? b.get(
                  DataUtil.init(
                     new int[]{
                        2141306589, 2141306550, 1955994785, 506297287, -1484653486, 1220379885, -822931673, 258922992, -563325193, -283215356, -189738580
                     }
                  )
               )
               .getAsString()
            : DataUtil.init(new int[]{2141306591});
         String sess = b.has(
                  DataUtil.init(
                     new int[]{2141306591, 2141306540, 1955994983, 506299187, -1484594000, 1222226927, -765673237, 2033930348, -1372671962, 396843014}
                  )
               )
               && !b.get(
                     DataUtil.init(
                        new int[]{2141306593, 2141306514, 1955994917, 506301105, -1484534286, 1224073965, -708415059, -486029590, 2112948832, 1076901376}
                     )
                  )
                  .isJsonNull()
            ? b.get(
                  DataUtil.init(
                     new int[]{2141306593, 2141306514, 1955994917, 506301105, -1484534286, 1224073965, -708415059, -486029590, 2112948832, 1076901376}
                  )
               )
               .getAsString()
            : DataUtil.init(new int[]{2141306595});
         if (!wrapped.isEmpty() && !sess.isEmpty()) {
            byte[] k = Guardians.get().unwrapKey(Base64.getDecoder().decode(wrapped), sess.getBytes(StandardCharsets.UTF_8));
            if (k != null && k.length == 32) {
               return k;
            }
         }
      } catch (Throwable var5) {
      }

      return original;
   }
}
