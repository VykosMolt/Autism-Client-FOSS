package autismclient.a;

import java.lang.management.ManagementFactory;
import java.util.Locale;

public final class NetHelper {
   private static final long init_J = 30000L;
   private static final String[] init_StrArr = new String[]{
      autismclient.e.DataUtil.init(
         new int[]{
            -1546126505, -1546126470, -685281312, 231113177, -1425427139, -1238567816, 259100525, -557821411, -112591704, 804625589, -826409222, 151114502
         }
      ),
      autismclient.e.DataUtil.init(
         new int[]{-1546126503, -1546126476, -685281367, 231115101, -1425367444, -1236720779, 316358718, 1217185936, -921938206, 1484683967, -1219436298}
      ),
      autismclient.e.DataUtil.init(
         new int[]{
            -1546126503, -1546126476, -685281367, 231115101, -1425367444, -1236720779, 316358718, 1217185932, -921938198, 1484683945, -1219436380, 852179076
         }
      ),
      autismclient.e.DataUtil.init(
         new int[]{-1546126501, -1546126474, -685281154, 231117006, -1425307982, -1234873741, 373617122, -1302773990, -1731284930, -2130224977}
      ),
      autismclient.e.DataUtil.init(new int[]{-1546126501, -1546126474, -685281154, 231117016, -1425307998, -1234873729, 373617149, -1302773991}),
      autismclient.e.DataUtil.init(
         new int[]{
            -1546126499,
            -1546126480,
            -685281220,
            231118940,
            -1425248278,
            -1233026704,
            430875314,
            472233571,
            1754335851,
            -1450166656,
            -2005490373,
            -2040659023,
            1164080377,
            1726749890,
            1989638078,
            1549238764
         }
      )
   };
   private static final String init_Str = autismclient.e.DataUtil.init(
      new int[]{
         -1546126495,
         -1546126581,
         -685281116,
         231122729,
         -1425129169,
         -1229332670,
         545391926,
         -272718992,
         135642594,
         -90049914,
         1503422892,
         -638529816,
         1680411236,
         553137867,
         -32596438,
         -1010488601,
         -1260378384,
         -417027544,
         -42949956,
         -1331448258,
         1674777448,
         378490440,
         -1151699166,
         -1342934433,
         1318703086,
         -2069880447,
         258218922
      }
   );
   private static final String load_Str = autismclient.e.DataUtil.init(
      new int[]{
         -1546126493,
         -1546126514,
         -685281126,
         231124654,
         -1425069605,
         -1227485618,
         602650286,
         1502288359,
         -673704139,
         590008467,
         1110396003,
         62534699,
         1938576555,
         -33668217,
         -1043713563,
         2004615014,
         2013520425,
         -2005375484,
         -2037094543,
         1274575277,
         857128883,
         801192281,
         -932844311,
         1146594541,
         1184689441,
         -1929331812,
         320258671,
         1338081084,
         -1469158739,
         1700720268,
         1182722040,
         -1990325664,
         -1570555848,
         -1442590442
      }
   );
   private static final String[] load_StrArr = new String[]{
      autismclient.e.DataUtil.init(
         new int[]{
            -1546126499,
            -1546126546,
            -685281237,
            231118936,
            -1425248271,
            -1233026712,
            430875303,
            472233586,
            1754335842,
            -1450166577,
            -2005490389,
            -2040659027,
            1164080357,
            1726749894,
            1989638067,
            1549238698,
            781758329,
            -1535299072,
            -349627857,
            2046439207,
            -984892949,
            -466913271,
            -1589408637,
            -2027025047,
            1586730086,
            1943989639
         }
      ),
      autismclient.e.DataUtil.init(
         new int[]{
            -1546126497,
            -1546126542,
            -685281049,
            231120878,
            -1425188832,
            -1231179698,
            488133736,
            -2047726361,
            944989116,
            -770108211,
            1896450036,
            -1339594463,
            1422245668,
            1139943876,
            978520942,
            269375016,
            -239310181,
            1171320322,
            1951194857,
            357495465,
            -1802541523
         }
      ),
      autismclient.e.DataUtil.init(
         new int[]{
            -1546126497,
            -1546126532,
            -685281043,
            231120813,
            -1425188755,
            -1231179706,
            488133750,
            -2047726365,
            944989109,
            -770108269,
            1896450019,
            -1339594454,
            1422245674,
            1139943876,
            978520931,
            269375075,
            -239310149,
            1171320393,
            1951194848,
            357495466,
            -1802541518,
            -44211363,
            -1370553751,
            462503844,
            1452716423,
            2084538281,
            196179447,
            1786592312,
            -450212566,
            -1071686432,
            1137457535,
            901441898
         }
      ),
      autismclient.e.DataUtil.init(
         new int[]{
            -1546126495,
            -1546126587,
            -685281115,
            231122740,
            -1425129169,
            -1229332659,
            545391911,
            -272718993,
            135642604,
            -90049845,
            1503422890,
            -638529885,
            1680411246,
            553137864,
            -32596434,
            -1010488607,
            -1260378390,
            -417027572,
            -42949914,
            -1331448311,
            1674777445,
            378490434,
            -1151699153,
            -1342934430,
            1318703103
         }
      )
   };
   private static volatile boolean init_Z;
   private static volatile long load_J;
   private static int init_I;
   private static int load_I;

   private NetHelper() {
   }

   public static boolean init() {
      long now = System.currentTimeMillis();
      if (now - load_J >= 30000L) {
         load_J = now;
         init_Z = load();
      }

      return init_Z;
   }

   private static boolean load() {
      int var7 = init_I ^ load_I;
      if ((var7 * var7 - var7 & 1) != 0) {
         init_I = var7 ^ load_I;
         String var20 = autismclient.e.DataUtil.init(
            new int[]{
               -966076439,
               -966076474,
               116401497,
               -686521943,
               192657178,
               1677408206,
               460045769,
               1376516607,
               -277661740,
               -17578230,
               -544922034,
               287285551,
               315918355,
               1203537755
            }
         );
         var7 = init_I_I_I(var20.length() ^ var7, 200235569);
         init_Z_Str_IArr(var20, new int[]{157885681, -1002652621, -1694133082});
         return true;
      }

      var7 ^= load_I;
      if ((var7 * var7 - var7 & 1) == 0) {
         var7 ^= load_I;

         try {
            try {
               for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                  if (arg != null) {
                     String a = arg.toLowerCase(Locale.ROOT);

                     for (String bad : init_StrArr) {
                        if (a.startsWith(bad)) {
                           return true;
                        }
                     }

                     if (a.startsWith(
                        autismclient.e.DataUtil.init(
                           new int[]{
                              -1546126507,
                              -1546126472,
                              -685281496,
                              231111260,
                              -1425486615,
                              -1240414852,
                              201842144,
                              1962138521,
                              696755067,
                              124567213,
                              -433382351,
                              -549949991,
                              131418617,
                              -220993355,
                              1739139763,
                              -1921241328,
                              571064447,
                              523125298,
                              -962983645,
                              212279647,
                              -2009266463,
                              2137246827,
                              1830139831,
                              899760995,
                              2122784127,
                              1381795218,
                              -114018755,
                              760386734,
                              2097153023,
                              587231134,
                              1024296110,
                              1688409846,
                              801094254,
                              -935884544
                           }
                        )
                     )) {
                        return true;
                     }
                  }
               }

               if (Boolean.parseBoolean(
                  System.getProperty(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1546126505,
                           -1546126531,
                           -685281298,
                           231113171,
                           -1425427099,
                           -1238567816,
                           259100536,
                           -557821426,
                           -112591700,
                           804625592,
                           -826409242,
                           151114514,
                           389583918,
                           -807799311,
                           728022652,
                           1093862289,
                           -450004058,
                           -1065222626,
                           1337839078,
                           -1476664140,
                           1468052274,
                           -1735018686,
                           2048994684,
                           -905677359,
                           1988770480,
                           1522343823,
                           -51979024
                        }
                     )
                  )
               )) {
                  return true;
               }

               ClassLoader own = NetHelper.class.getClassLoader();

               for (String className : load_StrArr) {
                  if (init_Z_Str_ClassLoader(className, own) || init_Z_Str_ClassLoader(className, ClassLoader.getSystemClassLoader())) {
                     return true;
                  }
               }

               return false;
            } catch (Throwable t) {
               return false;
            }
         } catch (Throwable var10) {
            throw var10;
         }
      } else {
         init_I = var7 ^ load_I;
         String var8 = autismclient.e.DataUtil.init(new int[]{-964716229, -964716193, 158568009, 620639986, 2059967622, -565510263, -350945337, 2005595436});
         var7 = init_I_I_I(var8.length() ^ var7, -1040553816);
         init_Z_Str_IArr(var8, new int[]{-1927742797, 1436364066, 912515691});
         return true;
      }
   }

   private static boolean init_Z_Str_ClassLoader(String className, ClassLoader loader) {
      if (loader == null) {
         return false;
      }

      try {
         Class.forName(className, false, loader);
         return true;
      } catch (Throwable t) {
         return false;
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
}
