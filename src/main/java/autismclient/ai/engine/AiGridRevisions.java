package autismclient.ai.engine;

import it.unimi.dsi.fastutil.HashCommon;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.core.BlockPos;

public final class AiGridRevisions {
   private static final Map<Long, AtomicInteger> chunkRevisions = new ConcurrentHashMap<>();
   static final int init_I = 8192;
   private static final AtomicInteger stampCounter = new AtomicInteger();
   private static volatile int fallbackRevision;
   private static final AtomicLong changeCount = new AtomicLong();

   public static long globalRevision() {
      return changeCount.get();
   }

   private AiGridRevisions() {
   }

   public static void bump(BlockPos pos) {
      bumpChunk(pos.getX() >> 4, pos.getZ() >> 4);
   }

   public static void bumpChunk(int cx, int cz) {
      chunkRevisions.computeIfAbsent(HashCommon.mix(chunkKey(cx, cz)), k -> new AtomicInteger()).set(stampCounter.incrementAndGet());
      changeCount.incrementAndGet();
      if (chunkRevisions.size() > 8192) {
         pruneOldest();
      }
   }

   public static int revisionOf(long chunkKey) {
      AtomicInteger revision = chunkRevisions.get(HashCommon.mix(chunkKey));
      return revision == null ? fallbackRevision : revision.get();
   }

   public static long chunkKey(int cx, int cz) {
      return (long)cx << 32 | cz & 4294967295L;
   }

   private static void pruneOldest() {
      int[] stamps = chunkRevisions.values().stream().mapToInt(AtomicInteger::get).toArray();
      Arrays.sort(stamps);
      int cutoff = stamps[stamps.length / 2];
      chunkRevisions.values().removeIf(revision -> revision.get() <= cutoff);
   }

   static int trackedChunks() {
      return chunkRevisions.size();
   }

   public static void resetAll() {
      fallbackRevision = stampCounter.incrementAndGet();
      chunkRevisions.clear();
      changeCount.incrementAndGet();
   }
}
