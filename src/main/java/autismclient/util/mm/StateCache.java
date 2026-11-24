package autismclient.util.mm;

import autismclient.util.AutismHttp;
import autismclient.util.L;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;

public final class StateCache {
   private static volatile PublicKey init_PublicKey;
   private static volatile long init_J;
   private static final long load = 60000L;

   private StateCache() {
   }

   public static boolean init_Z() {
      return load() != null;
   }

   public static PublicKey init_PublicKey() {
      return load();
   }

   public static PublicKey init(boolean forceFetch) {
      if (forceFetch) {
         init_J = 0L;
      }

      return load();
   }

   private static PublicKey load() {
      PublicKey k = init_PublicKey;
      if (k != null) {
         return k;
      }

      long now = System.currentTimeMillis();
      if (now - init_J < 60000L) {
         return null;
      }

      init_J = now;

      try {
         JsonObject r = AutismHttp.init_JsonObject(
            autismclient.e.DataUtil.init(
               new int[]{
                  1179736741,
                  1179736781,
                  -2082866808,
                  -144357902,
                  -180130995,
                  -1289094124,
                  -1307209660,
                  -1868791961,
                  -2097976944,
                  -612774328,
                  -1816135819,
                  -465639042,
                  -1549912023,
                  -802631739,
                  888221155,
                  1765049017,
                  -1118054729,
                  -299957820,
                  -708758409,
                  -496677661,
                  1782859046,
                  -565943549,
                  -364377361,
                  1589200437,
                  2020574121,
                  -1786710715,
                  446542118,
                  957902961,
                  -369782234,
                  1421653662,
                  1121593253,
                  409651645,
                  -185700177,
                  -1461737275,
                  1930784377,
                  -275229708,
                  57810726,
                  1792133650,
                  -278428425
               }
            ),
            null
         );
         if (r != null && r.has(autismclient.e.DataUtil.init(new int[]{1179736743, 1179736791, -2082866609, -144355990}))) {
            byte[] spki = Base64.getDecoder()
               .decode(r.get(autismclient.e.DataUtil.init(new int[]{1179736743, 1179736791, -2082866609, -144355990})).getAsString());
            init_PublicKey = autismclient.util.mm.a.StreamUtil.init_PublicKey_BArr(spki);
         }
      } catch (Throwable var5) {
      }

      return init_PublicKey;
   }

   public static StateCache.Inner_DataUtil init(String idtoken, byte[] senderSpki) {
      if (idtoken != null && !idtoken.isEmpty() && senderSpki != null) {
         PublicKey key = load();
         if (key == null) {
            return null;
         }

         try {
            int dot1 = idtoken.indexOf(46);
            int dot2 = idtoken.indexOf(46, dot1 + 1);
            if (dot1 > 0 && dot2 > dot1) {
               String signingInput = idtoken.substring(0, dot2);
               byte[] sig = Base64.getUrlDecoder().decode(idtoken.substring(dot2 + 1));
               if (!autismclient.util.mm.a.StreamUtil.init_Z_PublicKey(key, signingInput.getBytes(StandardCharsets.US_ASCII), sig)) {
                  return null;
               }

               JsonObject claims = init(idtoken, dot1, dot2);
               long exp = claims.has(autismclient.e.DataUtil.init(new int[]{1179736745, 1179736780, -2082866688, -144354054}))
                  ? claims.get(autismclient.e.DataUtil.init(new int[]{1179736745, 1179736780, -2082866688, -144354054})).getAsLong()
                  : 0L;
               if (exp * 1000L <= IoWorker.init()) {
                  return null;
               }

               String fp = claims.has(autismclient.e.DataUtil.init(new int[]{1179736747, 1179736781, -2082866490}))
                  ? claims.get(autismclient.e.DataUtil.init(new int[]{1179736747, 1179736781, -2082866490})).getAsString()
                  : autismclient.e.DataUtil.init(new int[]{1179736749});
               if (!fp.equalsIgnoreCase(autismclient.util.mm.a.StreamUtil.init_Str_BArr(autismclient.util.mm.a.StreamUtil.init_BArr_BArr2(senderSpki)))) {
                  return null;
               }

               String did = claims.has(autismclient.e.DataUtil.init(new int[]{1179736749, 1179736777, -2082866531, -144350230}))
                  ? claims.get(autismclient.e.DataUtil.init(new int[]{1179736751, 1179736779, -2082866341, -144348300})).getAsString()
                  : autismclient.e.DataUtil.init(new int[]{1179736751});
               if (did.isEmpty()) {
                  return null;
               }

               String uname = claims.has(autismclient.e.DataUtil.init(new int[]{1179736753, 1179736772, -2082866402, -144346381, -179773476, -1278011882}))
                  ? claims.get(autismclient.e.DataUtil.init(new int[]{1179736753, 1179736772, -2082866402, -144346381, -179773476, -1278011882})).getAsString()
                  : autismclient.e.DataUtil.init(new int[]{1179736755});
               return new StateCache.Inner_DataUtil(did, uname);
            } else {
               return null;
            }
         } catch (Throwable t) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static String init_Str() {
      String token = L.open();
      if (token != null && !token.isEmpty()) {
         try {
            int dot1 = token.indexOf(46);
            int dot2 = token.indexOf(46, dot1 + 1);
            if (dot1 > 0 && dot2 > dot1) {
               JsonObject claims = init(token, dot1, dot2);
               return claims.has(autismclient.e.DataUtil.init(new int[]{1179736757, 1179736768, -2082866302, -144342537, -179654336, -1274317806}))
                  ? claims.get(autismclient.e.DataUtil.init(new int[]{1179736759, 1179736770, -2082866108, -144340615, -179594874, -1272470756})).getAsString()
                  : autismclient.e.DataUtil.init(new int[]{1179736759});
            } else {
               return autismclient.e.DataUtil.init(new int[]{1179736757});
            }
         } catch (Throwable t) {
            return autismclient.e.DataUtil.init(new int[]{1179736761});
         }
      } else {
         return autismclient.e.DataUtil.init(new int[]{1179736755});
      }
   }

   private static JsonObject init(String jwt, int dot1, int dot2) {
      byte[] payload = Base64.getUrlDecoder().decode(jwt.substring(dot1 + 1, dot2));
      return JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
   }

   public record Inner_DataUtil(String init, String load) {
   }
}
