package autismclient.ai.a;

import it.unimi.dsi.fastutil.HashCommon;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.core.BlockPos;

public final class IoWorker {
   private static final Map<Long, AtomicInteger> init_Map = new ConcurrentHashMap<>();
   static final int init_I = 8192;
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private static volatile int load;
   private static final AtomicLong init_AtomicLong = new AtomicLong();

   public static long init_J() {
      return init_AtomicLong.get();
   }

   private IoWorker() {
   }

   public static void init(BlockPos pos) {
      init_V(pos.getX() >> 4, pos.getZ() >> 4);
   }

   public static void init_V(int cx, int cz) {
      init_Map.computeIfAbsent(HashCommon.mix(init_J(cx, cz)), k -> new AtomicInteger()).set(init_AtomicInteger.incrementAndGet());
      init_AtomicLong.incrementAndGet();
      if (init_Map.size() > 8192) {
         load();
      }
   }

   public static int init(long chunkKey) {
      AtomicInteger revision = init_Map.get(HashCommon.mix(chunkKey));
      return revision == null ? load : revision.get();
   }

   public static long init_J(int cx, int cz) {
      return (long)cx << 32 | cz & 4294967295L;
   }

   private static void load() {
      int[] stamps = init_Map.values().stream().mapToInt(AtomicInteger::get).toArray();
      Arrays.sort(stamps);
      int cutoff = stamps[stamps.length / 2];
      init_Map.values().removeIf(revision -> revision.get() <= cutoff);
   }

   static int init_I() {
      return init_Map.size();
   }

   public static void init_V() {
      load = init_AtomicInteger.incrementAndGet();
      init_Map.clear();
      init_AtomicLong.incrementAndGet();
   }
}
