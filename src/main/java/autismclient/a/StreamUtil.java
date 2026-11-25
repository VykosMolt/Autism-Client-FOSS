package autismclient.a;

import autismclient.AutismClientAddon;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicInteger;

public final class StreamUtil {
   private static final long init_J = 300000L;
   private static volatile long load_J;
   private static volatile StreamUtil.Inner_DataUtil init_StreamUtil_DataUti;
   private static final StreamUtil.Inner_DataUtil load_StreamUtil_DataUti = new StreamUtil.Inner_DataUtil(
      autismclient.e.DataUtil.init(new int[]{624737051}), autismclient.e.DataUtil.init(new int[]{624737051}), 0L
   );
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private static int init_I;
   private static int load_I;

   private StreamUtil() {
   }

   public static int init_I() {
      return init_AtomicInteger.get();
   }

   public static void init_V() {
      long now = System.currentTimeMillis();
      long current = load_J;
      if (now > current) {
         load_J = now;
      }
   }

   public static void init_V_Str_Str(String grant, String serverPubKeyB64) {
      int var11 = init_I ^ load_I;
      if ((var11 * var11 - var11 & 1) == 0) {
         var11 ^= load_I;
         if ((var11 * var11 - var11 & 1) == 0) {
            var11 ^= load_I;

            try {
               if (grant != null && !grant.isEmpty()) {
                  long lastSeen = Math.max(System.currentTimeMillis(), load_J);
                  load_J = lastSeen;
                  synchronized (StreamUtil.class) {
                     init_StreamUtil_DataUti = new StreamUtil.Inner_DataUtil(
                        grant, serverPubKeyB64 == null ? autismclient.e.DataUtil.init(new int[]{624737037}) : serverPubKeyB64, lastSeen
                     );
                  }

                  try {
                     JsonObject o = new JsonObject();
                     o.addProperty(autismclient.e.DataUtil.init(new int[]{624737039, 624737128, -2107988320, -923127023, 1447836413, 1933252517}), grant);
                     o.addProperty(
                        autismclient.e.DataUtil.init(new int[]{624737039, 624737124, -2107988297, -923127031}),
                        serverPubKeyB64 == null ? autismclient.e.DataUtil.init(new int[]{624737041}) : serverPubKeyB64
                     );
                     o.addProperty(
                        autismclient.e.DataUtil.init(
                           new int[]{624737041, 624737149, -2107988111, -923125119, 1447895845, 1935099520, -141454345, -90120495, 1501231037}
                        ),
                        lastSeen
                     );
                     byte[] sealed = autismclient.util.mm.a.DataUtil.init(o.toString().getBytes(StandardCharsets.UTF_8));
                     File file = init_File();
                     File dir = file.getParentFile();
                     if (dir != null) {
                        dir.mkdirs();
                     }

                     File tmp = new File(dir, file.getName() + ".tmp");
                     Files.write(tmp.toPath(), sealed);

                     try {
                        Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                     } catch (AtomicMoveNotSupportedException notAtomic) {
                        Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                     }
                  } catch (Throwable var18) {
                  } finally {
                     init_AtomicInteger.incrementAndGet();
                  }
               }
            } catch (Throwable var22) {
               throw var22;
            }
         } else {
            init_I = var11 ^ load_I;
            String var28 = autismclient.e.DataUtil.init(
               new int[]{
                  2126832077,
                  2126832098,
                  1507284917,
                  -518806835,
                  1096856438,
                  -357191702,
                  1811956261,
                  336070659,
                  1828259256,
                  841461046,
                  315489450,
                  1190239827,
                  -1757271945,
                  1359141735
               }
            );
            var11 = init_I_I_I(var28.length() ^ var11, 286373558);
            init_Z_Str_IArr(var28, new int[]{543480652, -2042083315, -908771913});
         }
      } else {
         init_I = var11 ^ load_I;
         String var12 = autismclient.e.DataUtil.init(
            new int[]{
               101622241,
               101622222,
               -1144677855,
               -1125274207,
               -523760926,
               943277510,
               -823169359,
               251554295,
               -791754964,
               1225398530,
               -667348170,
               787043623,
               -1371450389,
               434707795
            }
         );
         var11 = init_I_I_I(var12.length() ^ var11, 1563985317);
         init_Z_Str_IArr(var12, new int[]{-953900931, -1044514738, -255808345});
      }
   }

   public static StreamUtil.Inner_DataUtil init_StreamUtil_DataUti() {
      int var11 = init_I ^ load_I;
      if ((var11 * var11 - var11 & 1) != 0) {
         init_I = var11 ^ load_I;
         String var20 = autismclient.e.DataUtil.init(new int[]{1822339985, 1822340085, 657964799, -1077931236, 943872688, -804716761, 823585661, -238651578});
         var11 = init_I_I_I(var20.length() ^ var11, 1123627663);
         init_Z_Str_IArr(var20, new int[]{114254808, 52078620, -1552633493});
         return null;
      }

      var11 ^= load_I;
      if ((var11 * var11 - var11 & 1) == 0) {
         var11 ^= load_I;

         try {
            StreamUtil.Inner_DataUtil cached = init_StreamUtil_DataUti;
            if (cached == load_StreamUtil_DataUti) {
               return null;
            }

            if (cached != null) {
               return init(cached.init_J()) ? null : cached;
            }

            try {
               File file = init_File();
               if (!file.isFile()) {
                  return null;
               }

               byte[] plain = autismclient.util.mm.a.DataUtil.read(Files.readAllBytes(file.toPath()));
               if (plain == null) {
                  return null;
               }

               JsonObject o = JsonParser.parseString(new String(plain, StandardCharsets.UTF_8)).getAsJsonObject();
               if (!o.has(autismclient.e.DataUtil.init(new int[]{624737043, 624737140, -2107988164, -923123179, 1447955553, 1936946593}))) {
                  return null;
               }

               String grant = o.get(autismclient.e.DataUtil.init(new int[]{624737043, 624737140, -2107988164, -923123179, 1447955553, 1936946593}))
                  .getAsString();
               if (grant.isEmpty()) {
                  return null;
               }

               String key = o.has(autismclient.e.DataUtil.init(new int[]{624737045, 624737150, -2107987991, -923121265}))
                  ? o.get(autismclient.e.DataUtil.init(new int[]{624737045, 624737150, -2107987991, -923121265})).getAsString()
                  : autismclient.e.DataUtil.init(new int[]{624737047});
               long lastSeen = o.has(
                     autismclient.e.DataUtil.init(
                        new int[]{624737047, 624737147, -2107988053, -923119349, 1448074751, 1940640650, 30320425, 939934303, -926808733}
                     )
                  )
                  ? o.get(
                        autismclient.e.DataUtil.init(
                           new int[]{624737049, 624737141, -2107987863, -923117431, 1448134205, 1942487688, 87578863, -1580025639, -1736155483}
                        )
                     )
                     .getAsLong()
                  : 0L;
               if (init(lastSeen)) {
                  return null;
               }

               StreamUtil.Inner_DataUtil entry = new StreamUtil.Inner_DataUtil(grant, key, lastSeen);
               synchronized (StreamUtil.class) {
                  if (init_StreamUtil_DataUti == null) {
                     init_StreamUtil_DataUti = entry;
                  }
               }

               return entry;
            } catch (Throwable t) {
               return null;
            }
         } catch (Throwable var15) {
            throw var15;
         }
      } else {
         init_I = var11 ^ load_I;
         String var12 = autismclient.e.DataUtil.init(
            new int[]{
               1638475789,
               1638475880,
               -746858003,
               -1677764444,
               -471087939,
               -1718823507,
               -1743920047,
               1773051603,
               -869976258,
               -1199459362,
               1471463153,
               -1629285320,
               1031764383,
               1919926537,
               -611817121,
               -1786463642,
               454200335,
               1195310916,
               -1600068657,
               1937477567,
               -67735890,
               -2099811170,
               -669634960,
               716150250,
               725819754,
               1025575674,
               1728071907,
               2030619443,
               -1475304489,
               1510201362,
               -428395703,
               -395366068,
               628553275,
               -1989683637,
               -1550651929,
               -825571660,
               177084636
            }
         );
         var11 = init_I_I_I(var12.length() ^ var11, -1586457928);
         init_Z_Str_IArr(var12, new int[]{55500086, 1981129077, -703393328});
         return null;
      }
   }

   public static void load() {
      int var2 = init_I ^ load_I;
      if ((var2 * var2 - var2 & 1) == 0) {
         var2 ^= load_I;

         try {
            synchronized (StreamUtil.class) {
               init_StreamUtil_DataUti = load_StreamUtil_DataUti;
               load_J = 0L;
               init_AtomicInteger.incrementAndGet();
            }

            try {
               Files.deleteIfExists(init_File().toPath());
            } catch (Throwable var3) {
            }
         } catch (Throwable var6) {
            throw var6;
         }
      } else {
         init_I = var2 ^ load_I;
         String var3 = autismclient.e.DataUtil.init(new int[]{788248941, 788248841, -1334086565, 1592988192, 2137995028, 1853339483, 1618947801, -1352226718});
         var2 = init_I_I_I(var3.length() ^ var2, 377125138);
         init_Z_Str_IArr(var3, new int[]{-1380301469, -2002016223, 1120929904});
      }
   }

   private static boolean init(long lastSeenMs) {
      return lastSeenMs > 0L && System.currentTimeMillis() < lastSeenMs - 300000L;
   }

   public static boolean init_Z() {
      return true;
   }

   private static File init_File() {
      int var0 = init_I ^ load_I;
      if ((var0 * var0 - var0 & 1) == 0) {
         var0 ^= load_I;
         if ((var0 * var0 - var0 & 1) == 0) {
            var0 ^= load_I;

            try {
               return new File(
                  AutismClientAddon.FOLDER,
                  autismclient.e.DataUtil.init(
                     new int[]{
                        624737049, 624737144, -2107987861, -923117415, 1448134188, 1942487720, 87578873, -1580025710, -1736155473, 2013756156, -1998071432
                     }
                  )
               );
            } catch (Throwable var2) {
               throw var2;
            }
         } else {
            init_I = var0 ^ load_I;
            String var7 = autismclient.e.DataUtil.init(
               new int[]{
                  30849725,
                  30849752,
                  956341381,
                  -418185236,
                  -78842807,
                  1850839046,
                  1541439307,
                  539979009,
                  -440516924,
                  -771123678,
                  1864970637,
                  1979517777,
                  1235507546
               }
            );
            var0 = init_I_I_I(var7.length() ^ var0, -1202524866);
            init_Z_Str_IArr(var7, new int[]{-37763656, -45026848, -1649923524});
            return null;
         }
      } else {
         init_I = var0 ^ load_I;
         String var1 = autismclient.e.DataUtil.init(new int[]{922254527, 922254555, -1474880691, 1523337550, -21177726, -656506379, 1123135179, 457450896});
         var0 = init_I_I_I(var1.length() ^ var0, 777059471);
         init_Z_Str_IArr(var1, new int[]{854738371, 435030624, -421514821});
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

   public record Inner_DataUtil(String init_Str, String load_Str, long init_J) {
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
