package autismclient.a;

import autismclient.util.L;
import autismclient.util.mm.IoWorker;
import autismclient.util.mm.StateCache;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DataUtil {
   private static final long init_J = 30000L;
   private static final long load_J = 21600000L;
   private static final AtomicBoolean init_AtomicBoolean = new AtomicBoolean();
   private static final ConcurrentLinkedQueue<Runnable> init_ConcurrentLinkedQu = new ConcurrentLinkedQueue<>();
   private static final AtomicBoolean load_AtomicBoolean = new AtomicBoolean();
   private static volatile DataUtil.Inner_DataUtil init_DataUtil_DataUtil = DataUtil.Inner_DataUtil.init_DataUtil_DataUtil;
   private static volatile String init_Str = autismclient.e.DataUtil.init(new int[]{-648408655});
   private static volatile boolean init_Z;
   private static volatile boolean load_Z;
   private static volatile DataUtil.Inner_StreamUtil init_DataUtil_StreamUti = DataUtil.Inner_StreamUtil.init_DataUtil_StreamUti;
   private static volatile long read_J;
   private static volatile long write_J;
   private static volatile long update;
   private static volatile boolean read_Z;
   private static volatile boolean write_Z = true;
   private static volatile String load_Str = autismclient.e.DataUtil.init(new int[]{-648408655});
   private static volatile String read_Str = autismclient.e.DataUtil.init(new int[]{-648408653});
   private static volatile long sync;
   private static int init_I;
   private static int load_I;

   private DataUtil() {
   }

   public static boolean init_Z() {
      int var2 = init_I ^ load_I;
      if ((var2 * var2 - var2 & 1) != 0) {
         init_I = var2 ^ load_I;
         String var9 = autismclient.e.DataUtil.init(
            new int[]{
               -1016941515,
               -1016941542,
               -1460415987,
               1971747701,
               994633678,
               768871826,
               -1934774115,
               151547051,
               402992640,
               -392128546,
               728915714,
               1121554171,
               408437983,
               -223325937
            }
         );
         var2 = init_I_I_I(var9.length() ^ var2, 849225858);
         init_Z_Str_IArr(var9, new int[]{1305052940, -1525589266, -559612518});
         return true;
      }

      var2 ^= load_I;
      if ((var2 * var2 - var2 & 1) == 0) {
         var2 ^= load_I;

         try {
            if (!init_Z) {
               update_V();
            }

            long now = System.currentTimeMillis();
            if (!load_Z) {
               if (now - update >= 30000L) {
                  update = now;
                  read_V();
               }

               return load_Z;
            } else {
               if (now - read_J >= 30000L) {
                  read_J = now;
                  write_Z = CachePool.load();
                  if (IoWorker.init() < sync && write_Z && !NetHelper.init()) {
                     if (now - write_J >= 21600000L) {
                        write_J = now;
                        init((Runnable)null);
                     }
                  } else {
                     load_Z = false;
                     init_DataUtil_StreamUti = load_DataUtil_StreamUti();
                  }
               }

               return load_Z;
            }
         } catch (Throwable var4) {
            throw var4;
         }
      } else {
         init_I = var2 ^ load_I;
         String var3 = autismclient.e.DataUtil.init(new int[]{-2140255683, -2140255654, -1923416810, 503623551, -1567542069, -1349165173});
         var2 = init_I_I_I(var3.length() ^ var2, -622212791);
         init_Z_Str_IArr(var3, new int[]{-1120171526, 553554274, 1251779621});
         return true;
      }
   }

   private static void read_V() {
      int var2 = init_I ^ load_I;
      if ((var2 * var2 - var2 & 1) == 0) {
         var2 ^= load_I;
         if ((var2 * var2 - var2 & 1) == 0) {
            var2 ^= load_I;

            try {
               if (!init_AtomicBoolean.get()) {
                  if (L.load_Z()) {
                     init((Runnable)null);
                  } else {
                     synchronized (DataUtil.class) {
                        if (!load_Z) {
                           sync();
                        }
                     }
                  }
               }
            } catch (Throwable var5) {
               throw var5;
            }
         } else {
            init_I = var2 ^ load_I;
            String var10 = autismclient.e.DataUtil.init(
               new int[]{-2070077501, -2070077460, 252106999, -774620352, 1756571951, -1380842768, 143543874, 154891661, 506676868, -1472883394}
            );
            var2 = init_I_I_I(var10.length() ^ var2, 24251094);
            init_Z_Str_IArr(var10, new int[]{294084331, -1158802814, 198798828});
         }
      } else {
         init_I = var2 ^ load_I;
         String var3 = autismclient.e.DataUtil.init(
            new int[]{
               1009168801,
               1009168782,
               1219461857,
               -851391007,
               -623320414,
               -2143062778,
               -2010433679,
               2101061943,
               708411244,
               485910850,
               -2116633610,
               -1191135897,
               1729489835,
               2074578067
            }
         );
         var2 = init_I_I_I(var3.length() ^ var2, 679022475);
         init_Z_Str_IArr(var3, new int[]{1275206058, 632486389, -1767425149});
      }
   }

   public static boolean load_Z() {
      return read_Z;
   }

   public static DataUtil.Inner_StreamUtil init_DataUtil_StreamUti() {
      if (!init_Z) {
         update_V();
      }

      return init_DataUtil_StreamUti;
   }

   public static DataUtil.Inner_DataUtil init_DataUtil_DataUtil() {
      return init_DataUtil_DataUtil;
   }

   public static boolean read_Z() {
      return init_AtomicBoolean.get();
   }

   public static String init_Str() {
      return init_Str;
   }

   public static boolean write_Z() {
      if (!init_Z) {
         update_V();
      }

      return write_Z;
   }

   public static boolean update_Z() {
      return NetHelper.init();
   }

   public static void init(Runnable onDone) {
      init_V_Runnable_Z(onDone, false);
   }

   public static void load(Runnable onDone) {
      init_V_Runnable_Z(onDone, true);
   }

   private static void init_V_Runnable_Z(Runnable onDone, boolean force) {
      int var3 = init_I ^ load_I;
      if ((var3 * var3 - var3 & 1) == 0) {
         var3 ^= load_I;

         try {
            if (!init_Z) {
               update_V();
            }

            if (onDone != null) {
               init_ConcurrentLinkedQu.add(onDone);
            }

            if (force) {
               load_AtomicBoolean.set(true);
            }

            if (init_AtomicBoolean.compareAndSet(false, true)) {
               Thread thread = new Thread(
                  () -> {
                     int var1x = init_I ^ load_I;
                     if ((var1x * var1x - var1x & 1) != 0) {
                        init_I = var1x ^ load_I;
                        String var2x = autismclient.e.DataUtil.init(
                           new int[]{
                              -1963754243, -1963754286, -746839347, -1677184582, -453113451, -1161612598, -1650252476, 381779915, -1049725950, 1818237176
                           }
                        );
                        var1x = init_I_I_I(var2x.length() ^ var1x, 1883052645);
                        init_Z_Str_IArr(var2x, new int[]{1424571229, -2018925669, -229227617});
                     } else {
                        var1x ^= load_I;

                        try {
                           do {
                              try {
                                 load(load_AtomicBoolean.getAndSet(false));
                              } catch (Throwable var2x) {
                              }

                              init_DataUtil_DataUtil = DataUtil.Inner_DataUtil.init_DataUtil_DataUtil;

                              Runnable r;
                              while ((r = init_ConcurrentLinkedQu.poll()) != null) {
                                 read(r);
                              }

                              init_AtomicBoolean.set(false);
                           } while (!init_ConcurrentLinkedQu.isEmpty() && init_AtomicBoolean.compareAndSet(false, true));
                        } catch (Throwable var4x) {
                           throw var4x;
                        }
                     }
                  },
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -648408665,
                        -648408602,
                        1374167887,
                        -350468996,
                        2020359314,
                        -1793367526,
                        240184017,
                        -1144230681,
                        -1111413700,
                        -94084280,
                        1378353757,
                        -220707351,
                        1748003724,
                        -1646455906,
                        499476871,
                        -1696084433,
                        -1039008779,
                        -2144504630,
                        -2055134865,
                        715327330,
                        700308326,
                        234718487,
                        -1313658698,
                        -2068713545,
                        294390518,
                        536168027
                     }
                  )
               );
               thread.setDaemon(true);
               thread.start();
            }
         } catch (Throwable var5) {
            throw var5;
         }
      } else {
         init_I = var3 ^ load_I;
         String var4 = autismclient.e.DataUtil.init(
            new int[]{733322371, 733322412, 1258156983, 348161920, -2091879185, -423747792, -251279102, 800285389, -960957884, 275075710}
         );
         var3 = init_I_I_I(var4.length() ^ var3, -820027783);
         init_Z_Str_IArr(var4, new int[]{633830620, 563133381, -124087800});
      }
   }

   public static void init_V() {
      if (!init_Z) {
         update_V();
      }

      try {
         load(false);
      } catch (Throwable var1) {
      }
   }

   public static synchronized void load_V() {
      int var0 = init_I ^ load_I;
      if ((var0 * var0 - var0 & 1) == 0) {
         var0 ^= load_I;
         if ((var0 * var0 - var0 & 1) == 0) {
            var0 ^= load_I;

            try {
               init_Str = autismclient.e.DataUtil.init(new int[]{-648408663});
               load_Str = autismclient.e.DataUtil.init(new int[]{-648408663});
               read_Str = autismclient.e.DataUtil.init(new int[]{-648408661});
               sync = 0L;
               read_J = 0L;
               load_Z = false;
               init_DataUtil_StreamUti = DataUtil.Inner_StreamUtil.load_DataUtil_StreamUti;
               StreamUtil.load();
            } catch (Throwable var2) {
               throw var2;
            }
         } else {
            init_I = var0 ^ load_I;
            String var7 = autismclient.e.DataUtil.init(
               new int[]{
                  -1732119413,
                  -1732119388,
                  2138873015,
                  1880554895,
                  -1832337420,
                  -967888088,
                  60241451,
                  1867486429,
                  2057503030,
                  -641916312,
                  1575428008,
                  1593629457,
                  -2137091339,
                  -1825324891
               }
            );
            var0 = init_I_I_I(var7.length() ^ var0, 123714244);
            init_Z_Str_IArr(var7, new int[]{1470008933, 567399353, 644787913});
         }
      } else {
         init_I = var0 ^ load_I;
         String var1 = autismclient.e.DataUtil.init(
            new int[]{83063715, 83063692, -1719992105, -1780148384, 649971983, -1325701616, 1852925474, 1606115757, -1750018204, 1584007518}
         );
         var0 = init_I_I_I(var1.length() ^ var0, -1721907019);
         init_Z_Str_IArr(var1, new int[]{-1527360566, 517582701, 499898484});
      }
   }

   public static void init_V(boolean donatorLost) {
      int var2 = init_I ^ load_I;
      if ((var2 * var2 - var2 & 1) == 0) {
         var2 ^= load_I;
         if ((var2 * var2 - var2 & 1) == 0) {
            var2 ^= load_I;

            try {
               Thread thread = new Thread(
                  () -> {
                     try {
                        if (donatorLost) {
                           write_V();
                        } else {
                           load_V();
                        }
                     } catch (Throwable var2x) {
                     }
                  },
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -648408661,
                        -648408598,
                        1374168003,
                        -350465160,
                        2020478494,
                        -1789673442,
                        354700629,
                        -1889183261,
                        1564860344,
                        1266032460,
                        592299737,
                        1181421813,
                        -2030632696,
                        1474899330,
                        -1522757509,
                        39155507,
                        1213821577,
                        -1026233130,
                        -1748457069,
                        1632407142,
                        -934988774,
                        1080122115,
                        -875949265,
                        -1384622915,
                        26363492
                     }
                  )
               );
               thread.setDaemon(true);
               thread.start();
            } catch (Throwable var4) {
               throw var4;
            }
         } else {
            init_I = var2 ^ load_I;
            String var9 = autismclient.e.DataUtil.init(new int[]{-1456235207, -1456235190, 2101348747, 717304693, 761606233, 2134955166});
            var2 = init_I_I_I(var9.length() ^ var2, -646249912);
            init_Z_Str_IArr(var9, new int[]{-2037324344, -1241781406, -71435085});
         }
      } else {
         init_I = var2 ^ load_I;
         String var3 = autismclient.e.DataUtil.init(
            new int[]{
               1245691713,
               1245691684,
               -38262655,
               -1186139312,
               1884385613,
               -1713589382,
               -1581666993,
               -1787036027,
               436460992,
               645387942,
               -1467809407,
               1742551765,
               -1815471530
            }
         );
         var2 = init_I_I_I(var3.length() ^ var2, -326981618);
         init_Z_Str_IArr(var3, new int[]{-1074233844, -2019723981, -50073958});
      }
   }

   private static synchronized void write_V() {
      int var0 = init_I ^ load_I;
      if ((var0 * var0 - var0 & 1) == 0) {
         var0 ^= load_I;

         try {
            load_Str = autismclient.e.DataUtil.init(new int[]{-648408659});
            read_Str = autismclient.e.DataUtil.init(new int[]{-648408659});
            sync = 0L;
            read_J = 0L;
            load_Z = false;
            init_DataUtil_StreamUti = DataUtil.Inner_StreamUtil.read;
            StreamUtil.load();
         } catch (Throwable var2) {
            throw var2;
         }
      } else {
         init_I = var0 ^ load_I;
         String var1 = autismclient.e.DataUtil.init(new int[]{-1419121085, -1419121116, -1043080596, 2024237509, -1673147727, -327970447});
         var0 = init_I_I_I(var1.length() ^ var0, 520845944);
         init_Z_Str_IArr(var1, new int[]{204431449, -661972683, -380973392});
      }
   }

   private static void update_V() {
      int var2 = init_I ^ load_I;
      if ((var2 * var2 - var2 & 1) == 0) {
         var2 ^= load_I;
         if ((var2 * var2 - var2 & 1) == 0) {
            var2 ^= load_I;

            try {
               if (!init_Z) {
                  synchronized (DataUtil.class) {
                     if (!init_Z) {
                        if (!sync()) {
                           init_DataUtil_StreamUti = load_DataUtil_StreamUti();
                        }

                        update = System.currentTimeMillis();
                        init_Z = true;
                     }
                  }
               }
            } catch (Throwable var5) {
               throw var5;
            }
         } else {
            init_I = var2 ^ load_I;
            String var10 = autismclient.e.DataUtil.init(
               new int[]{
                  1903044531,
                  1903044508,
                  -1135161649,
                  -830273673,
                  31316748,
                  970822160,
                  30719043,
                  952289701,
                  -543788834,
                  322415952,
                  1404959168,
                  604060921,
                  1546016285,
                  681867149
               }
            );
            var2 = init_I_I_I(var10.length() ^ var2, 1054805716);
            init_Z_Str_IArr(var10, new int[]{-1509983064, -1562552140, 390148892});
         }
      } else {
         init_I = var2 ^ load_I;
         String var3 = autismclient.e.DataUtil.init(
            new int[]{
               -1669135533,
               -1669135562,
               -203593865,
               -2016445698,
               1914690471,
               -774136201,
               1771578583,
               -915641067,
               1679900984,
               537323672,
               -522833285,
               972034162,
               68286101,
               2116871107,
               1198493649,
               -1501404504,
               701103105,
               259362314,
               -549700283,
               139159029,
               18960804,
               587786580,
               1041513206,
               -2072831956,
               166721388,
               873395132,
               1305446489,
               1814133513,
               403565805,
               -374362392,
               1279665655,
               1014926962,
               1397961221,
               387127605,
               -883944611,
               -1632481138,
               932693990
            }
         );
         var2 = init_I_I_I(var3.length() ^ var2, 1718876775);
         init_Z_Str_IArr(var3, new int[]{1132827904, -1842128304, 1307731071});
      }
   }

   private static void load(boolean force) {
      int var7 = init_I ^ load_I;
      if ((var7 * var7 - var7 & 1) == 0) {
         var7 ^= load_I;

         try {
            boolean networkFailed = false;
            init_Str = autismclient.e.DataUtil.init(new int[]{-648408657});
            read_Z = false;
            if (L.load_Z()) {
               init_DataUtil_DataUtil = DataUtil.Inner_DataUtil.read;
               long roundTripsBefore = L.init_J();
               String jwt = force ? L.close() : L.tick();
               boolean definitiveAnswer = jwt != null && L.init_J() != roundTripsBefore;
               if (jwt != null) {
                  init_DataUtil_DataUtil = DataUtil.Inner_DataUtil.write;
                  if (init(L.sync())) {
                     return;
                  }

                  boolean denied = L.update_Z();
                  if (definitiveAnswer && denied) {
                     write_V();
                     return;
                  }

                  if (sync()) {
                     return;
                  }

                  load_Z = false;
                  if (denied) {
                     init_DataUtil_StreamUti = DataUtil.Inner_StreamUtil.read;
                  } else {
                     read_Z = true;
                     init_DataUtil_StreamUti = DataUtil.Inner_StreamUtil.write;
                  }

                  return;
               }

               String denial = L.flush();
               if (!denial.isEmpty()) {
                  if (autismclient.e.DataUtil.init(
                        new int[]{
                           -648408657,
                           -648408639,
                           1374168157,
                           -350461340,
                           2020597676,
                           -1785979371,
                           469217243,
                           1660831420,
                           -53832940,
                           -1668818105,
                           -193754279,
                           -1711416346
                        }
                     )
                     .equals(denial)) {
                     write_V();
                  } else {
                     load_V();
                  }

                  init_Str = denial;
                  return;
               }

               networkFailed = true;
            }

            if (!sync()) {
               load_Z = false;
               if (!L.load_Z()) {
                  init_DataUtil_DataUtil = DataUtil.Inner_DataUtil.load_DataUtil_DataUtil;
                  networkFailed = !init_Z(force);
                  init_DataUtil_StreamUti = networkFailed ? DataUtil.Inner_StreamUtil.write : DataUtil.Inner_StreamUtil.load_DataUtil_StreamUti;
               } else {
                  if (force) {
                     init_DataUtil_DataUtil = DataUtil.Inner_DataUtil.load_DataUtil_DataUtil;
                     if (init_Z(true) && !networkFailed) {
                        boolean var14 = false;
                     } else {
                        boolean var13 = true;
                     }
                  }

                  init_DataUtil_StreamUti = DataUtil.Inner_StreamUtil.write;
               }
            }
         } catch (Throwable var9) {
            throw var9;
         }
      } else {
         init_I = var7 ^ load_I;
         String var8 = autismclient.e.DataUtil.init(new int[]{-801872677, -801872728, 911750885, -1800496557, 19180855, 594607096});
         var7 = init_I_I_I(var8.length() ^ var7, 2113089088);
         init_Z_Str_IArr(var8, new int[]{616240125, -1939246614, 843358597});
      }
   }

   private static boolean init_Z(boolean force) {
      int var3 = init_I ^ load_I;
      if ((var3 * var3 - var3 & 1) == 0) {
         var3 ^= load_I;

         try {
            for (int attempt = 0; attempt < 2; attempt++) {
               if (attempt > 0) {
                  try {
                     Thread.sleep(1000L);
                  } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                     return false;
                  }
               }

               PublicKey key = CachePool.init_Z() ? StateCache.init(force || attempt > 0) : CachePool.init_PublicKey(read_Str);
               if (key != null) {
                  return true;
               }
            }

            return false;
         } catch (Throwable var6) {
            throw var6;
         }
      } else {
         init_I = var3 ^ load_I;
         String var4 = autismclient.e.DataUtil.init(
            new int[]{
               571519865,
               571519830,
               537246665,
               -525223623,
               897937802,
               2066271070,
               -370107303,
               1411574127,
               809122372,
               -687009126,
               177556702,
               1209288895,
               -1166749053,
               -1809478965
            }
         );
         var3 = init_I_I_I(var4.length() ^ var3, 1096868945);
         init_Z_Str_IArr(var4, new int[]{-183064457, -1791603286, 1580975059});
         return true;
      }
   }

   private static boolean init(String grant) {
      int var4 = init_I ^ load_I;
      if ((var4 * var4 - var4 & 1) == 0) {
         var4 ^= load_I;

         try {
            if (grant != null && !grant.isEmpty()) {
               PublicKey key = CachePool.init_PublicKey(read_Str);
               CachePool.Inner_DataUtil claims = CachePool.init_CachePool_DataUtil_Str_PublicKey(grant, key);
               if (claims == null) {
                  return false;
               }

               String keyB64 = CachePool.init(key);
               if (!init(grant, keyB64, claims.load_J())) {
                  return false;
               }

               StreamUtil.init_V_Str_Str(grant, keyB64);
               return true;
            } else {
               return false;
            }
         } catch (Throwable var6) {
            throw var6;
         }
      } else {
         init_I = var4 ^ load_I;
         String var5 = autismclient.e.DataUtil.init(new int[]{-617836055, -617836134, -1973048485, -1034962011, -2019050231, 1833955790});
         var4 = init_I_I_I(var5.length() ^ var4, 2080046123);
         init_Z_Str_IArr(var5, new int[]{1496760662, 613113138, -789208235});
         return true;
      }
   }

   private static boolean sync() {
      int var5 = init_I ^ load_I;
      if ((var5 * var5 - var5 & 1) == 0) {
         var5 ^= load_I;
         if ((var5 * var5 - var5 & 1) == 0) {
            var5 ^= load_I;

            try {
               String grant = load_Str;
               long storedLastSeenMs = -1L;
               if (grant.isEmpty()) {
                  StreamUtil.Inner_DataUtil entry = StreamUtil.init_StreamUtil_DataUti();
                  if (entry == null) {
                     return false;
                  }

                  grant = entry.init_Str();
                  read_Str = entry.load_Str();
                  storedLastSeenMs = entry.init_J();
               }

               PublicKey key = CachePool.init_PublicKey(read_Str);
               CachePool.Inner_DataUtil claims = CachePool.init_CachePool_DataUtil_Str_PublicKey(grant, key);
               if (claims == null) {
                  return false;
               }

               if (!init(grant, CachePool.init(key), claims.load_J())) {
                  return false;
               }

               if (storedLastSeenMs == 0L) {
                  StreamUtil.init_V_Str_Str(grant, CachePool.init(key));
               }

               return true;
            } catch (Throwable var7) {
               throw var7;
            }
         } else {
            init_I = var5 ^ load_I;
            String var13 = autismclient.e.DataUtil.init(
               new int[]{
                  -1337399379,
                  -1337399422,
                  1490292117,
                  -1045581587,
                  1946706262,
                  218350858,
                  -1821054971,
                  -618125789,
                  -1982028904,
                  -1313352874,
                  -2059234678,
                  588237939,
                  1055503959,
                  -1639116665
               }
            );
            var5 = init_I_I_I(var13.length() ^ var5, -2119180985);
            init_Z_Str_IArr(var13, new int[]{-817671425, 289522000, 1504748665});
            return true;
         }
      } else {
         init_I = var5 ^ load_I;
         String var6 = autismclient.e.DataUtil.init(new int[]{-1106974971, -1106974879, 43514355, 1348942216, -1132461380, -746563405, -1668628367, -187874342});
         var5 = init_I_I_I(var6.length() ^ var5, 1448591741);
         init_Z_Str_IArr(var6, new int[]{-1927393129, -2082484756, -1286541658});
         return true;
      }
   }

   private static boolean init(String grant, String keyB64, long expSeconds) {
      int var4 = init_I ^ load_I;
      if ((var4 * var4 - var4 & 1) == 0) {
         var4 ^= load_I;

         try {
            write_Z = CachePool.load();
            if (write_Z && !NetHelper.init()) {
               StreamUtil.init_V();
               load_Str = grant;
               read_Str = keyB64;
               sync = expSeconds * 1000L;
               read_J = System.currentTimeMillis();
               write_J = read_J;
               init_DataUtil_StreamUti = DataUtil.Inner_StreamUtil.update;
               load_Z = true;
               return true;
            } else {
               return false;
            }
         } catch (Throwable var6) {
            throw var6;
         }
      } else {
         init_I = var4 ^ load_I;
         String var5 = autismclient.e.DataUtil.init(new int[]{-1738227129, -1738227168, 1949533928, 306007241, 896288821, 2015150973});
         var4 = init_I_I_I(var5.length() ^ var4, -1395096772);
         init_Z_Str_IArr(var5, new int[]{1056384195, -1522386114, 181015154});
         return true;
      }
   }

   private static DataUtil.Inner_StreamUtil load_DataUtil_StreamUti() {
      return L.load_Z() ? DataUtil.Inner_StreamUtil.init_DataUtil_StreamUti : DataUtil.Inner_StreamUtil.load_DataUtil_StreamUti;
   }

   private static void read(Runnable onDone) {
      if (onDone != null) {
         try {
            onDone.run();
         } catch (Throwable var2) {
         }
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

   public enum Inner_DataUtil {
      init_DataUtil_DataUtil,
      load_DataUtil_DataUtil,
      read,
      write;

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

   public enum Inner_StreamUtil {
      init_DataUtil_StreamUti,
      load_DataUtil_StreamUti,
      read,
      write,
      update;

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
