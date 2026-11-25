package autismclient.a;

import autismclient.util.mm.IoWorker;
import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ConfigStore {
   private static final long init_J = 30000L;
   private static final long load_J = 5000L;
   private static final AtomicBoolean init_AtomicBoolean = new AtomicBoolean();
   private static volatile boolean init_Z;
   private static volatile boolean load_Z;
   private static volatile boolean read_Z = true;
   private static volatile long read_J;
   private static volatile int init_I;
   private static int load_I;
   private static int read_I;

   private ConfigStore() {
   }

   public static boolean init_Z() {
      int var4 = load_I ^ read_I;
      if ((var4 * var4 - var4 & 1) != 0) {
         load_I = var4 ^ read_I;
         String var14 = autismclient.e.DataUtil.init(
            new int[]{
               -1471358831,
               -1471358786,
               1632516593,
               -931596271,
               1185289650,
               -1910725578,
               897050209,
               2038750279,
               -1223251332,
               733913202,
               1276469990,
               915861399,
               -1673064773,
               -325399645
            }
         );
         var4 = init_I_I_I(var14.length() ^ var4, 604570783);
         init_Z_Str_IArr(var14, new int[]{-684134142, 149975135, -171095774});
         return true;
      }

      var4 ^= read_I;
      if ((var4 * var4 - var4 & 1) == 0) {
         var4 ^= read_I;

         try {
            if (!init_Z) {
               init_V();
            }

            long now = System.currentTimeMillis();
            boolean due = now - read_J >= (load_Z ? 30000L : 5000L) || init_I != StreamUtil.init_I();
            if (due && init_AtomicBoolean.compareAndSet(false, true)) {
               try {
                  read_J = now;
                  load_V();
               } finally {
                  init_AtomicBoolean.set(false);
               }
            }

            return load_Z;
         } catch (Throwable var9) {
            throw var9;
         }
      } else {
         load_I = var4 ^ read_I;
         String var5 = autismclient.e.DataUtil.init(
            new int[]{-1534866451, -1534866494, -336219683, -1832876886, -984607611, -458061350, -1315001260, -2110334213, -995854574, -806717688}
         );
         var4 = init_I_I_I(var5.length() ^ var4, -1153046487);
         init_Z_Str_IArr(var5, new int[]{1362205721, 898092209, 930727940});
         return true;
      }
   }

   public static boolean load_Z() {
      if (!init_Z) {
         init_V();
      }

      return read_Z;
   }

   private static void init_V() {
      int var2 = load_I ^ read_I;
      if ((var2 * var2 - var2 & 1) == 0) {
         var2 ^= read_I;

         try {
            if (!init_Z) {
               synchronized (ConfigStore.class) {
                  if (!init_Z) {
                     read_J = System.currentTimeMillis();
                     load_V();
                     init_Z = true;
                  }
               }
            }
         } catch (Throwable var5) {
            throw var5;
         }
      } else {
         load_I = var2 ^ read_I;
         String var3 = autismclient.e.DataUtil.init(
            new int[]{
               1225715079,
               1225715170,
               -657538245,
               1091151770,
               -534030985,
               624905776,
               -2102756623,
               -760946485,
               -2114505474,
               -1125163160,
               -520322501,
               1049872671,
               -1813683092
            }
         );
         var2 = init_I_I_I(var3.length() ^ var2, -1983617069);
         init_Z_Str_IArr(var3, new int[]{1523603990, 871359739, -3366693});
      }
   }

   private static void load_V() {
      init_I = StreamUtil.init_I();
      read_Z = CachePool.load();
      load_Z = read_Z && read() && !NetHelper.init();
   }

   private static boolean read() {
      int var3 = load_I ^ read_I;
      if ((var3 * var3 - var3 & 1) == 0) {
         var3 ^= read_I;

         try {
            StreamUtil.Inner_DataUtil entry = StreamUtil.init_StreamUtil_DataUti();
            if (entry == null) {
               return false;
            }

            PublicKey key = CachePool.init_PublicKey(entry.load_Str());
            CachePool.Inner_DataUtil claims = CachePool.init_CachePool_DataUtil_Str_PublicKey(entry.init_Str(), key);
            return claims != null && IoWorker.init() < claims.load_J() * 1000L;
         } catch (Throwable var5) {
            throw var5;
         }
      } else {
         load_I = var3 ^ read_I;
         String var4 = autismclient.e.DataUtil.init(new int[]{-86231389, -86231355, 1621794254});
         var3 = init_I_I_I(var4.length() ^ var3, 1078404960);
         init_Z_Str_IArr(var4, new int[]{861821236, 814640363, 1493995206});
         return true;
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
