package autismclient.util;

import autismclient.AutismClientAddon;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;

public final class RefQueue {
   private static volatile Path init;

   private RefQueue() {
   }

   public static String init(String authBase, String version) {
      try {
         JsonObject challenge = AutismHttp.postForm(authBase + "/challenge", "version=" + init(version));
         if (challenge == null) {
            AutismClientAddon.LOG
               .debug(
                  autismclient.e.DataUtil.init(
                     new int[]{
                        1784072973,
                        1784073046,
                        -528312587,
                        802175898,
                        -902348383,
                        2091972778,
                        426647909,
                        341180868,
                        1986671626,
                        1457280689,
                        -2068939849,
                        287373073,
                        318633649,
                        1287709746,
                        1264297590,
                        538516824,
                        -485848770,
                        2118558331,
                        1250795771,
                        119960211,
                        -576197635,
                        -682258526,
                        324823920,
                        1479604426,
                        -1376903622,
                        265661944,
                        -354417213,
                        1897967122,
                        -1292558208,
                        -1414595783,
                        -902795186,
                        2078119001,
                        -2821309,
                        -87459461,
                        1583721163,
                        1850714508,
                        1537578437,
                        420292265,
                        144160067,
                        173993906,
                        1098844180,
                        -295566136,
                        -572616761,
                        -571251220,
                        -528921911,
                        783290904,
                        -1487783337
                     }
                  ),
                  version
               );
            return autismclient.e.DataUtil.init(new int[]{1784072973});
         }

         if (challenge.has(autismclient.e.DataUtil.init(new int[]{1784072975, 1784073068, -528312645, 802177812}))
            && challenge.has(autismclient.e.DataUtil.init(new int[]{1784072975, 1784073057, -528312643, 802177822, -902288656, 2093819828}))) {
            String cid = challenge.get(autismclient.e.DataUtil.init(new int[]{1784072977, 1784073074, -528312455, 802179734})).getAsString();
            byte[] nonce = Base64.getDecoder()
               .decode(
                  challenge.get(autismclient.e.DataUtil.init(new int[]{1784072979, 1784073085, -528312543, 802181658, -902169492, 2097513904})).getAsString()
               );
            byte[] jar = init();
            if (jar == null) {
               AutismClientAddon.LOG
                  .debug(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           1784072979,
                           1784073032,
                           -528312529,
                           802181632,
                           -902169477,
                           2097513904,
                           598422691,
                           1371235650,
                           -441368116,
                           -797511497,
                           1046946353,
                           -1904400487,
                           1093130109,
                           -472708297,
                           -1769054277,
                           993893070,
                           745913525,
                           1648481897,
                           -436671181,
                           -651903715,
                           1265824371,
                           585847037,
                           981387959,
                           358256713,
                           -1778944009,
                           687307711,
                           -168298448,
                           -922283167,
                           1473989993,
                           -1550952895,
                           -834898502,
                           -112048723,
                           821457647,
                           -304619261,
                           -853261528,
                           -681301791,
                           354478189,
                           -1896080490,
                           1351049353,
                           -1067140311,
                           1278389711,
                           975373959,
                           171822617,
                           1031536245
                        }
                     ),
                     version
                  );
               return autismclient.e.DataUtil.init(new int[]{1784072981});
            }

            MessageDigest digest = MessageDigest.getInstance(
               autismclient.e.DataUtil.init(new int[]{1784072981, 1784073030, -528312380, 802183607, -902109984, 2099360997, 655681083, -1148724338})
            );
            digest.update(nonce);
            JsonArray ranges = challenge.has(
                  autismclient.e.DataUtil.init(new int[]{1784072983, 1784073061, -528312405, 802185494, -902050324, 2101207996, 712939327})
               )
               ? challenge.getAsJsonArray(
                  autismclient.e.DataUtil.init(new int[]{1784072983, 1784073061, -528312405, 802185494, -902050324, 2101207996, 712939327})
               )
               : null;
            if (ranges != null && ranges.size() != 0) {
               for (int i = 0; i < ranges.size(); i++) {
                  JsonArray range = ranges.get(i).getAsJsonArray();
                  int start = (int)Math.max(0L, Math.min(jar.length, range.get(0).getAsLong()));
                  int end = (int)Math.max(start, Math.min(jar.length, range.get(1).getAsLong()));
                  digest.update(jar, start, end - start);
               }
            } else {
               digest.update(jar);
            }

            return "&cid=" + init(cid) + "&answer=" + init(digest.digest());
         } else {
            return autismclient.e.DataUtil.init(new int[]{1784072977});
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG
            .debug(
               autismclient.e.DataUtil.init(
                  new int[]{
                     1784072985,
                     1784073026,
                     -528312215,
                     802187406,
                     -901990851,
                     2103055038,
                     770197753,
                     -1893676856,
                     1425559190,
                     1242663613,
                     -132134547,
                     198793232,
                     1867626302,
                     2061840936,
                     -507438613,
                     1449269298,
                     1977675759,
                     1178405447,
                     -2124137871,
                     -1423767445,
                     -1187121091,
                     1853952390,
                     1637952246,
                     -763090972,
                     2113982645,
                     1108953546,
                     17820518,
                     552433574,
                     -54429405,
                     -1687310078,
                     -767001628,
                     1992750907,
                     1645736382,
                     -521778874,
                     1004723318,
                     1081648938,
                     -828622302,
                     82514070,
                     -1737028448,
                     1986692844,
                     1457935008,
                     -2048653104
                  }
               ),
               version,
               t.toString()
            );
         return autismclient.e.DataUtil.init(new int[]{1784072985});
      }
   }

   private static byte[] init() {
      try {
         Path jar = init;
         if (jar == null) {
            jar = FabricLoader.getInstance()
               .getModContainer(autismclient.e.DataUtil.init(new int[]{1784072987, 1784073082, -528312269, 802189320, -901931154, 2104902062, 827455909}))
               .flatMap(container -> container.getOrigin().getPaths().stream().findFirst())
               .filter(
                  path -> path.toString()
                     .toLowerCase(Locale.ROOT)
                     .endsWith(autismclient.e.DataUtil.init(new int[]{1784072989, 1784073011, -528312082, 802191263, -901871689}))
               )
               .filter(x$0 -> Files.isRegularFile(x$0))
               .orElse(null);
            init = jar;
         }

         return jar == null ? null : Files.readAllBytes(jar);
      } catch (Throwable t) {
         return null;
      }
   }

   private static String init(byte[] bytes) {
      StringBuilder out = new StringBuilder(bytes.length * 2);

      for (byte b : bytes) {
         out.append(Character.forDigit(b >> 4 & 15, 16)).append(Character.forDigit(b & 15, 16));
      }

      return out.toString();
   }

   private static String init(String value) {
      return URLEncoder.encode(value == null ? autismclient.e.DataUtil.init(new int[]{1784072987}) : value, StandardCharsets.UTF_8);
   }
}
