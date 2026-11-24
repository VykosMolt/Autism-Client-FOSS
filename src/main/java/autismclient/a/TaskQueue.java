package autismclient.a;

import autismclient.util.mm.IoWorker;
import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TaskQueue {
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

   private TaskQueue() {
   }

   public static boolean init_Z() {
      int var4 = load_I ^ read_I;
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
            new int[]{
               -1346391613,
               -1346391572,
               1211533055,
               -1097183929,
               347035452,
               -2126801696,
               -1506345901,
               547915669,
               -194480946,
               -1733941472,
               2082390480,
               129594057,
               -277552627,
               -14194051
            }
         );
         var4 = init_I_I_I(var5.length() ^ var4, 117472762);
         init_Z_Str_IArr(var5, new int[]{-1337429563, -511230726, 460616459});
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
               synchronized (TaskQueue.class) {
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
         String var3 = autismclient.e.DataUtil.init(new int[]{1290990283, 1290990255, 1365993209, -603886526, -1540613834, -514387463, 1233854583, -405216612});
         var2 = init_I_I_I(var3.length() ^ var2, -852328827);
         init_Z_Str_IArr(var3, new int[]{-1163402714, -1410918078, -2062551744});
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
         String var4 = autismclient.e.DataUtil.init(new int[]{1398229303, 1398229328, 395435480, -626403335, 2056329989, -678276723});
         var3 = init_I_I_I(var4.length() ^ var3, -2049327637);
         init_Z_Str_IArr(var4, new int[]{-296971700, 408800921, 2111582318});
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
