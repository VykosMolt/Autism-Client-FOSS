package autismclient.a;

import autismclient.util.mm.IoWorker;
import autismclient.util.mm.StateCache;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;

public final class CachePool {
   private static final String init_Str = autismclient.e.DataUtil.init(
      new int[]{
         -1937938063,
         -1937938116,
         53462259,
         1657327933,
         -162441082,
         -740702863,
         -1486954141,
         1149060429,
         1261133111,
         440422590,
         768200070,
         -1955600256,
         -494063230,
         1863912694,
         1946716751,
         218676445,
         -1810966986,
         -305399777,
         -877455623,
         -1431322577,
         -1421325664,
         -1111419888,
         -94276746,
         1372386413,
         -405696717,
         308305814,
         967548808,
         -70756417,
         2101519282,
         722590668,
         925473916,
         -1375081580,
         322144317,
         1396541368,
         343107830,
         2046406418,
         -985907895,
         -498375828,
         1730217224,
         2097128286,
         586465753,
         1000566608,
         952791464,
         -528234863,
         804591164,
         -827479411,
         117943840,
         -638707044,
         1674918635,
         382867162,
         -1016022397,
         -1431921112,
         -1439879390,
         -1686588865,
         -744646788,
         -1609215394,
         1653928920,
         -267813298,
         287720442,
         329397675,
         1621395504
      }
   );
   private static volatile PublicKey init_PublicKey;
   private static volatile boolean init_Z;
   private static int init_I;
   private static int load;

   private CachePool() {
   }

   public static boolean init_Z() {
      int var0 = init_I ^ load;
      if ((var0 * var0 - var0 & 1) == 0) {
         var0 ^= load;
         if ((var0 * var0 - var0 & 1) == 0) {
            var0 ^= load;

            try {
               return !autismclient.e.DataUtil.init(
                     new int[]{
                        -1937938083,
                        -1937938160,
                        53461511,
                        1657308753,
                        -163036686,
                        -759173283,
                        -2059537257,
                        578856025,
                        764664899,
                        -2065193814,
                        403502970,
                        -376311404,
                        1219249918,
                        -857961758,
                        -827012685,
                        132411329,
                        -190217910,
                        -1601789949,
                        1884122125,
                        -1721754317,
                        -1834775596,
                        -1043470836,
                        2012143234,
                        -2048066695,
                        934438471,
                        -1097180286,
                        347152740,
                        -2123167573,
                        -1393684258,
                        -254541352,
                        699151248,
                        198854328,
                        1869515601,
                        2120406868,
                        1308104290,
                        1896526910,
                        -1337207331,
                        1496243456,
                        -861091684,
                        -924036502,
                        1419636813,
                        1059066532,
                        -1528673484,
                        -144236635,
                        -176366944,
                        -1172408071,
                        -1984913988,
                        -1402790496,
                        -536832649,
                        538054182,
                        -500187113,
                        1674070804,
                        356589494,
                        -1830628285,
                        -914902296,
                        1702799722,
                        1247181388,
                        7915962,
                        245396822,
                        -982632801,
                        -396843876
                     }
                  )
                  .isEmpty();
            } catch (Throwable var2) {
               throw var2;
            }
         } else {
            init_I = var0 ^ load;
            String var7 = autismclient.e.DataUtil.init(new int[]{885974333, 885974362, 1695400470, 1017809535, 1487321547, -1137673589});
            var0 = init_I_I_I(var7.length() ^ var0, -1077395659);
            init_Z_Str_IArr(var7, new int[]{2060768886, -368746359, 1246321765});
            return true;
         }
      } else {
         init_I = var0 ^ load;
         String var1 = autismclient.e.DataUtil.init(
            new int[]{
               -905487953,
               -905487926,
               1994644563,
               1704438370,
               1297979295,
               1582654984,
               1817667481,
               513117875,
               -1273214186,
               -814931120,
               506938195,
               -1464785945,
               1836273284
            }
         );
         var0 = init_I_I_I(var1.length() ^ var0, 645429690);
         init_Z_Str_IArr(var1, new int[]{1511242639, -761958817, 568423943});
         return true;
      }
   }

   public static PublicKey init_PublicKey(String pinnedB64) {
      if (init_Z()) {
         return init_PublicKey();
      }

      PublicKey pinned = load(pinnedB64);
      return pinned != null ? pinned : StateCache.init_PublicKey();
   }

   public static String init(PublicKey key) {
      return key == null ? autismclient.e.DataUtil.init(new int[]{-1937938083}) : Base64.getEncoder().encodeToString(key.getEncoded());
   }

   public static CachePool.Inner_DataUtil init_CachePool_DataUtil_Str_PublicKey(String grant, PublicKey key) {
      int var15 = init_I ^ load;
      if ((var15 * var15 - var15 & 1) == 0) {
         var15 ^= load;

         try {
            if (grant != null && !grant.isEmpty() && key != null) {
               if (init_Z()) {
                  PublicKey pin = init_PublicKey();
                  if (pin == null || !Arrays.equals(key.getEncoded(), pin.getEncoded())) {
                     return null;
                  }
               }

               try {
                  int dot1 = grant.indexOf(46);
                  int dot2 = grant.indexOf(46, dot1 + 1);
                  if (dot1 > 0 && dot2 > dot1) {
                     String signingInput = grant.substring(0, dot2);
                     byte[] signature = Base64.getUrlDecoder().decode(grant.substring(dot2 + 1));
                     if (!autismclient.util.mm.a.StreamUtil.init_Z_PublicKey(key, signingInput.getBytes(StandardCharsets.US_ASCII), signature)) {
                        return null;
                     }

                     byte[] payload = Base64.getUrlDecoder().decode(grant.substring(dot1 + 1, dot2));
                     JsonObject claims = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
                     if (claims.has(autismclient.e.DataUtil.init(new int[]{-1937938081, -1937938132, 53461729, 1657310639, -162977229, -757326268}))
                        && autismclient.e.DataUtil.init(
                              new int[]{
                                 -1937938081,
                                 -1937938118,
                                 53461731,
                                 1657310642,
                                 -162977233,
                                 -757326248,
                                 -2002278871,
                                 -1941103901,
                                 -44681818,
                                 -1385135488,
                                 10476003,
                                 324753207,
                                 1477415220
                              }
                           )
                           .equals(
                              claims.get(autismclient.e.DataUtil.init(new int[]{-1937938079, -1937938158, 53461667, 1657312557, -162917519, -755479226}))
                                 .getAsString()
                           )) {
                        long exp = claims.has(autismclient.e.DataUtil.init(new int[]{-1937938079, -1937938172, 53461688, 1657312562}))
                           ? claims.get(autismclient.e.DataUtil.init(new int[]{-1937938077, -1937938170, 53461638, 1657314484})).getAsLong()
                           : 0L;
                        if (exp * 1000L <= IoWorker.init()) {
                           return null;
                        }

                        String sub = claims.has(autismclient.e.DataUtil.init(new int[]{-1937938077, -1937938160, 53461643, 1657314470}))
                           ? claims.get(autismclient.e.DataUtil.init(new int[]{-1937938075, -1937938154, 53461833, 1657316388})).getAsString()
                           : autismclient.e.DataUtil.init(new int[]{-1937938075});
                        if (sub.isEmpty()) {
                           return null;
                        }

                        String role = claims.has(autismclient.e.DataUtil.init(new int[]{-1937938073, -1937938155, 53461781, 1657318308, -162738850}))
                           ? claims.get(autismclient.e.DataUtil.init(new int[]{-1937938073, -1937938155, 53461781, 1657318308, -162738850})).getAsString()
                           : autismclient.e.DataUtil.init(new int[]{-1937938071});
                        long iat = claims.has(autismclient.e.DataUtil.init(new int[]{-1937938071, -1937938176, 53461977, 1657320254}))
                           ? claims.get(autismclient.e.DataUtil.init(new int[]{-1937938069, -1937938174, 53461911, 1657322168})).getAsLong()
                           : 0L;
                        String fp = claims.has(autismclient.e.DataUtil.init(new int[]{-1937938069, -1937938163, 53461894}))
                           ? claims.get(autismclient.e.DataUtil.init(new int[]{-1937938067, -1937938165, 53462084})).getAsString()
                           : null;
                        return fp != null && !fp.isEmpty() && !fp.equalsIgnoreCase(init_Str()) ? null : new CachePool.Inner_DataUtil(sub, role, iat, exp, fp);
                     } else {
                        return null;
                     }
                  } else {
                     return null;
                  }
               } catch (Throwable t) {
                  return null;
               }
            } else {
               return null;
            }
         } catch (Throwable var18) {
            throw var18;
         }
      } else {
         init_I = var15 ^ load;
         String var16 = autismclient.e.DataUtil.init(new int[]{2057628669, 2057628571, -638020652});
         var15 = init_I_I_I(var16.length() ^ var15, 1292862426);
         init_Z_Str_IArr(var16, new int[]{225557363, 911825014, -180038148});
         return null;
      }
   }

   public static boolean load() {
      int var4 = init_I ^ load;
      if ((var4 * var4 - var4 & 1) != 0) {
         init_I = var4 ^ load;
         String var14 = autismclient.e.DataUtil.init(new int[]{-212880309, -212880328, 1990645109, 1580453315, 1749411751, -1602808984});
         var4 = init_I_I_I(var14.length() ^ var4, 244698932);
         init_Z_Str_IArr(var14, new int[]{266520406, 1664598913, 1486904728});
         return true;
      }

      var4 ^= load;
      if ((var4 * var4 - var4 & 1) == 0) {
         var4 ^= load;

         try {
            try {
               Class<?> holder = Class.forName(
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -1937938067,
                        -1937938164,
                        53462081,
                        1657324090,
                        -162560228,
                        -744396964,
                        -1601470757,
                        1894013043,
                        -1415140965,
                        -919694184,
                        1554254173,
                        937237948,
                        -1010394099,
                        -1257442659,
                        -326016043,
                        -1516563466,
                        231169693,
                        -1423671205,
                        -1184133515,
                        1946564920,
                        213971209,
                        -1956823470,
                        -531986215,
                        688295786,
                        -137669738,
                        27208589,
                        843469625
                     }
                  )
               );

               String[] entries;
               try {
                  entries = (String[])holder.getDeclaredMethod(
                        autismclient.e.DataUtil.init(new int[]{-1937938065, -1937938166, 53462044, 1657325988, -162500543, -742549928, -1544212591, -625946911})
                     )
                     .invoke(null);
               } catch (ReflectiveOperationException | ClassCastException badHolder) {
                  return false;
               }

               if (entries != null && (entries.length & 1) == 0) {
                  for (int i = 0; i < entries.length; i += 2) {
                     byte[] bytes = init_BArr(entries[i]);
                     if (bytes == null) {
                        return false;
                     }

                     if (!autismclient.util.mm.a.StreamUtil.init_Str_BArr(autismclient.util.mm.a.StreamUtil.init_BArr_BArr2(bytes))
                        .equalsIgnoreCase(entries[i + 1])) {
                        return false;
                     }
                  }

                  return true;
               } else {
                  return false;
               }
            } catch (ClassNotFoundException noClass) {
               return !StreamUtil.init_Z();
            } catch (Throwable t) {
               return false;
            }
         } catch (Throwable var9) {
            throw var9;
         }
      } else {
         init_I = var4 ^ load;
         String var5 = autismclient.e.DataUtil.init(
            new int[]{
               135640863,
               135640880,
               -90100573,
               1501850595,
               -687274784,
               169315204,
               953806903,
               -496756687,
               1780412458,
               -641789756,
               1579353012,
               1715303781,
               1634806761,
               -860601071
            }
         );
         var4 = init_I_I_I(var5.length() ^ var4, -2103918628);
         init_Z_Str_IArr(var5, new int[]{-632195973, 766334302, 1990743148});
         return true;
      }
   }

   private static byte[] init_BArr(String className) {
      int var5 = init_I ^ load;
      if ((var5 * var5 - var5 & 1) != 0) {
         init_I = var5 ^ load;
         String var15 = autismclient.e.DataUtil.init(new int[]{1919622599, 1919622561, -621241494});
         var5 = init_I_I_I(var15.length() ^ var5, 639290115);
         init_Z_Str_IArr(var15, new int[]{1160057610, 612616756, -128168977});
         return null;
      }

      var5 ^= load;
      if ((var5 * var5 - var5 & 1) == 0) {
         var5 ^= load;

         try {
            String path = className.replace('.', '/') + ".class";

            try (InputStream in = CachePool.class.getClassLoader().getResourceAsStream(path)) {
               return in == null ? null : in.readAllBytes();
            } catch (Throwable t) {
               return null;
            }
         } catch (Throwable var10) {
            throw var10;
         }
      } else {
         init_I = var5 ^ load;
         String var6 = autismclient.e.DataUtil.init(new int[]{-1614177919, -1614177817, 1500092112});
         var5 = init_I_I_I(var6.length() ^ var5, 135997730);
         init_Z_Str_IArr(var6, new int[]{-1550122899, -1125087670, -1756147977});
         return null;
      }
   }

   private static String init_Str() {
      try {
         return autismclient.util.mm.a.StreamUtil.init_Str_BArr(autismclient.util.mm.a.CachePool.init_CachePool().load_BArr());
      } catch (Throwable t) {
         return autismclient.e.DataUtil.init(new int[]{-1937938065});
      }
   }

   private static PublicKey init_PublicKey() {
      int var2 = init_I ^ load;
      if ((var2 * var2 - var2 & 1) == 0) {
         var2 ^= load;

         try {
            if (!init_Z) {
               synchronized (CachePool.class) {
                  if (!init_Z) {
                     init_PublicKey = load(
                        autismclient.e.DataUtil.init(
                           new int[]{
                              -1937938063,
                              -1937938116,
                              53462259,
                              1657327933,
                              -162441082,
                              -740702863,
                              -1486954141,
                              1149060429,
                              1261133111,
                              440422590,
                              768200070,
                              -1955600256,
                              -494063230,
                              1863912694,
                              1946716751,
                              218676445,
                              -1810966986,
                              -305399777,
                              -877455623,
                              -1431322577,
                              -1421325664,
                              -1111419888,
                              -94276746,
                              1372386413,
                              -405696717,
                              308305814,
                              967548808,
                              -70756417,
                              2101519282,
                              722590668,
                              925473916,
                              -1375081580,
                              322144317,
                              1396541368,
                              343107830,
                              2046406418,
                              -985907895,
                              -498375828,
                              1730217224,
                              2097128286,
                              586465753,
                              1000566608,
                              952791464,
                              -528234863,
                              804591164,
                              -827479411,
                              117943840,
                              -638707044,
                              1674918635,
                              382867162,
                              -1016022397,
                              -1431921112,
                              -1439879390,
                              -1686588865,
                              -744646788,
                              -1609215394,
                              1653928920,
                              -267813298,
                              287720442,
                              329397675,
                              1621395504
                           }
                        )
                     );
                     init_Z = true;
                  }
               }
            }

            return init_PublicKey;
         } catch (Throwable var5) {
            throw var5;
         }
      } else {
         init_I = var2 ^ load;
         String var3 = autismclient.e.DataUtil.init(
            new int[]{
               1216563991,
               1216564082,
               -941221965,
               886890802,
               1723813347,
               1898603451,
               -1272834293,
               -803155367,
               871986532,
               1261776084,
               460351143,
               1385984694,
               15854793,
               491496711,
               -1943468523,
               -117980180,
               637580357,
               -1709846962,
               -1465649791,
               1809499337,
               259902432,
               -532961624,
               658060602,
               -1074957040,
               1036071712,
               2053451712,
               -767507307,
               1977079373,
               1159916193,
               1597663276,
               -2012044237,
               2051139254,
               -839191999,
               -245149327,
               990303129,
               634628034,
               -1801369182
            }
         );
         var2 = init_I_I_I(var3.length() ^ var2, 368214775);
         init_Z_Str_IArr(var3, new int[]{-1091527797, 1728121074, -1974342203});
         return null;
      }
   }

   private static PublicKey load(String b64) {
      if (b64 != null && !b64.isEmpty()) {
         try {
            return autismclient.util.mm.a.StreamUtil.init_PublicKey_BArr(Base64.getDecoder().decode(b64));
         } catch (Throwable t) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static int init_I_I_I(int var0, int var1) {
      int var2 = var0 * 31 + var1;
      var2 ^= var2 >>> 16;
      var2 *= 73244475;
      return var2 ^ var2 >>> 13;
   }

   private static long init_J_J_I(long var0, int var2) {
      int var4 = (var2 & 3) + 1;

      for (int var3 = 0; var3 < var4; var3++) {
         var0 ^= var0 << 13;
         var0 ^= var0 >>> 7;
         var0 ^= var0 << 17;
         var0 += var2;
      }

      return var0;
   }

   private static boolean init_Z_Str_IArr(String var0, int[] var1) {
      int var2 = var0.length() ^ var1.length;

      for (int var3 = 0; var3 < var0.length() && var3 < var1.length; var3++) {
         var2 |= var0.charAt(var3) ^ var1[var3];
      }

      return var2 == 0;
   }

   public record Inner_DataUtil(String init_Str, String load_Str, long init_J, long load_J, String read) {
      private static int init_I;
      private static int load_I;

      private static int init_I_I_I(int var0, int var1) {
         int var2 = var0 * 31 + var1;
         var2 ^= var2 >>> 16;
         var2 *= 73244475;
         return var2 ^ var2 >>> 13;
      }

      private static long init_J_J_I(long var0, int var2) {
         int var4 = (var2 & 3) + 1;

         for (int var3 = 0; var3 < var4; var3++) {
            var0 ^= var0 << 13;
            var0 ^= var0 >>> 7;
            var0 ^= var0 << 17;
            var0 += var2;
         }

         return var0;
      }

      private static boolean init_Z_Str_IArr(String var0, int[] var1) {
         int var2 = var0.length() ^ var1.length;

         for (int var3 = 0; var3 < var0.length() && var3 < var1.length; var3++) {
            var2 |= var0.charAt(var3) ^ var1[var3];
         }

         return var2 == 0;
      }
   }
}
