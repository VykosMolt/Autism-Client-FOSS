package autismclient.util.mm.guardian.impl;

import autismclient.e.DataUtil;
import autismclient.util.AutismHttp;
import autismclient.util.L;
import autismclient.util.mm.a.CachePool;
import autismclient.util.mm.guardian.Guardians;
import com.google.gson.JsonObject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class LoginProof {
   private LoginProof() {
   }

   public static String build() {
      try {
         String version = L.read_Str();
         JsonObject ch = AutismHttp.postForm(
            DataUtil.init(
               new int[]{
                  810797041,
                  810796953,
                  -635095484,
                  1786875814,
                  -441423359,
                  -799218752,
                  994022024,
                  749913019,
                  1772465884,
                  -888131820,
                  -1762279615,
                  1203906338,
                  -1333607651,
                  1607838489,
                  -1696613417,
                  -1055409299,
                  1642047363,
                  -636135792,
                  1754630219,
                  -1441038665,
                  -1722524646,
                  -1858653481,
                  -1783683885,
                  540373753,
                  -428285803,
                  -391954935,
                  734302482,
                  1288541757,
                  1290089746,
                  1338079826,
                  -1469198959,
                  1699473987,
                  1144084887,
                  1106896480,
                  -45950420,
                  -1424462450,
                  -1208660252,
                  1186234797,
                  -1881426267,
                  1805328833
               }
            ),
            "version=" + enc(version)
         );
         if (ch != null && ch.has(DataUtil.init(new int[]{810797043, 810796929, -635095537, 1786877754, -441363640, -797371696, 1051280259}))) {
            String cid = ch.has(DataUtil.init(new int[]{810797045, 810796950, -635095355, 1786879666}))
               ? ch.get(DataUtil.init(new int[]{810797045, 810796950, -635095355, 1786879666})).getAsString()
               : DataUtil.init(new int[]{810797047});
            if (cid.isEmpty()) {
               return DataUtil.init(new int[]{810797047});
            }

            String nonce = ch.has(DataUtil.init(new int[]{810797049, 810796951, -635095225, 1786883508, -441185014, -791830562}))
               ? ch.get(DataUtil.init(new int[]{810797049, 810796951, -635095225, 1786883508, -441185014, -791830562})).getAsString()
               : DataUtil.init(new int[]{810797051});
            String pubkey = pubkey();
            String answer = AutismJarAttest.answer(ch);
            String guardian = Guardians.get().attest(nonce, version, pubkey);
            if (answer != null && !answer.isEmpty() || guardian != null && !guardian.isEmpty()) {
               StringBuilder sb = new StringBuilder(DataUtil.init(new int[]{810797053, 810797019, -635095097, 1786887351, -441065855, -788136574}))
                  .append(enc(cid));
               if (answer != null && !answer.isEmpty()) {
                  sb.append(DataUtil.init(new int[]{810797053, 810797019, -635095099, 1786887344, -441065834, -788136504, 1337571907, -1484944686, 1211353434}))
                     .append(enc(answer));
               }

               if (guardian != null && !guardian.isEmpty()) {
                  sb.append(DataUtil.init(new int[]{810797055, 810797017, -635095163, 1786889309})).append(enc(guardian));
               }

               return sb.toString();
            } else {
               return DataUtil.init(new int[]{810797051});
            }
         } else {
            return DataUtil.init(new int[]{810797043});
         }
      } catch (Throwable t) {
         return DataUtil.init(new int[]{810797055});
      }
   }

   private static String pubkey() {
      try {
         return Base64.getEncoder().encodeToString(CachePool.init_CachePool().init_BArr());
      } catch (Throwable t) {
         return DataUtil.init(new int[]{810797057});
      }
   }

   private static String enc(String s) {
      return URLEncoder.encode(s, StandardCharsets.UTF_8);
   }
}
