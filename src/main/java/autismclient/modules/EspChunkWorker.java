package autismclient.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class EspChunkWorker {
   static final EspChunkWorker INSTANCE = new EspChunkWorker();
   static final EspChunkWorker beds = new EspChunkWorker();
   static final EspChunkWorker endPortals = new EspChunkWorker();
   static final EspChunkWorker storage = new EspChunkWorker();
   static final EspChunkWorker blocks = new EspChunkWorker();
   static final EspChunkWorker spawners = new EspChunkWorker();
   static final EspChunkWorker oreSim = new EspChunkWorker();
   private static final long init_J = 100L;
   private static final int init_I = 4096;
   private static volatile Object cacheLevel;
   private static final AtomicInteger cacheRevision = new AtomicInteger();
   private static final AtomicInteger ESP_WORKER_IDS = new AtomicInteger();
   private static final ExecutorService ESP_POOL = Executors.newFixedThreadPool(
      Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors() / 2)), r -> {
         Thread t = new Thread(r, "Autism-ESP-Worker-" + ESP_WORKER_IDS.incrementAndGet());
         t.setDaemon(true);
         t.setPriority(Math.max(1, 4));
         return t;
      }
   );
   private final ConcurrentHashMap<Long, EspChunkWorker.ChunkResult> results = new ConcurrentHashMap<>();
   private final Set<Long> inFlight = ConcurrentHashMap.newKeySet();

   static void submitEspWork(Runnable task) {
      if (task != null) {
         ESP_POOL.execute(task);
      }
   }

   public static int revision() {
      return cacheRevision.get();
   }

   private EspChunkWorker() {
   }

   List<EspChunkWorker.EspBox> initAsync(LevelChunk chunk, long gameTime, String stamp, EspChunkWorker.ChunkScanner scanner) {
      long key = chunk.getPos().pack();
      EspChunkWorker.ChunkResult cached = this.results.get(key);
      if (cached != null && cached.stamp.equals(stamp) && gameTime - cached.gameTime < 100L) {
         return cached.boxes;
      }

      Object expectedLevel = cacheLevel;
      if (this.inFlight.add(key)) {
         ESP_POOL.execute(() -> {
            try {
               if (cacheLevel != expectedLevel) {
                  return;
               }

               if (this.results.size() >= 4096) {
                  this.trimAround(chunk.getPos());
               }

               List<EspChunkWorker.EspBox> entries = new ArrayList<>();
               scanner.scan(chunk, entries);
               if (cacheLevel == expectedLevel) {
                  List<EspChunkWorker.EspBox> frozen = List.copyOf(entries);
                  EspChunkWorker.ChunkResult prev = this.results.put(key, new EspChunkWorker.ChunkResult(stamp, gameTime, frozen));
                  if (prev == null || !prev.boxes.equals(frozen)) {
                     cacheRevision.incrementAndGet();
                  }

                  return;
               }
            } catch (Throwable var15) {
               return;
            } finally {
               this.inFlight.remove(key);
            }
         });
      }

      return cached != null && cached.stamp.equals(stamp) ? cached.boxes : List.of();
   }

   List<EspChunkWorker.EspBox> scan(LevelChunk chunk, long gameTime, String stamp, EspChunkWorker.ChunkScanner scanner) {
      long key = chunk.getPos().pack();
      EspChunkWorker.ChunkResult cached = this.results.get(key);
      if (cached != null && cached.stamp.equals(stamp) && gameTime - cached.gameTime < 100L) {
         return cached.boxes;
      }

      if (this.results.size() >= 4096) {
         this.trimAround(chunk.getPos());
      }

      List<EspChunkWorker.EspBox> entries = new ArrayList<>();
      scanner.scan(chunk, entries);
      List<EspChunkWorker.EspBox> frozen = List.copyOf(entries);
      this.results.put(key, new EspChunkWorker.ChunkResult(stamp, gameTime, frozen));
      if (cached == null || !cached.boxes.equals(frozen)) {
         cacheRevision.incrementAndGet();
      }

      return frozen;
   }

   private void trimAround(ChunkPos center) {
      int target = 3072;

      for (int keep = 24; keep >= 4 && this.results.size() > target; keep -= 4) {
         int radius = keep;
         this.results
            .keySet()
            .removeIf(key -> Math.max(Math.abs(ChunkPos.getX(key) - center.x()), Math.abs(ChunkPos.getZ(key) - center.z())) > radius);
      }

      if (this.results.size() >= 4096) {
         this.results.clear();
      }
   }

   static void setLevel(Object level) {
      if (cacheLevel != level) {
         cacheLevel = level;
         clearAll();
      }
   }

   static void reset() {
      endPortals.inFlight.clear();
      if (!endPortals.results.isEmpty()) {
         endPortals.results.clear();
         cacheRevision.incrementAndGet();
      }
   }

   static void clearAll() {
      INSTANCE.results.clear();
      INSTANCE.inFlight.clear();
      beds.results.clear();
      beds.inFlight.clear();
      endPortals.results.clear();
      endPortals.inFlight.clear();
      storage.results.clear();
      storage.inFlight.clear();
      blocks.results.clear();
      blocks.inFlight.clear();
      spawners.results.clear();
      spawners.inFlight.clear();
      oreSim.results.clear();
      oreSim.inFlight.clear();
      cacheRevision.incrementAndGet();
   }

   private static void invalidateChunk(int chunkX, int chunkZ) {
      long key = ChunkPos.pack(chunkX, chunkZ);
      boolean had = INSTANCE.results.remove(key) != null;
      had |= beds.results.remove(key) != null;
      had |= endPortals.results.remove(key) != null;
      had |= storage.results.remove(key) != null;
      had |= blocks.results.remove(key) != null;
      had |= spawners.results.remove(key) != null;
      had |= oreSim.results.remove(key) != null;
      if (had) {
         cacheRevision.incrementAndGet();
      }
   }

   private static boolean hasAnyCachedChunks() {
      return !INSTANCE.results.isEmpty()
         || !beds.results.isEmpty()
         || !endPortals.results.isEmpty()
         || !storage.results.isEmpty()
         || !blocks.results.isEmpty()
         || !spawners.results.isEmpty()
         || !oreSim.results.isEmpty();
   }

   public static void init_V_BlockPos(BlockPos pos) {
      if (pos != null && hasAnyCachedChunks()) {
         int x = pos.getX();
         int z = pos.getZ();
         int cx = x >> 4;
         int cz = z >> 4;
         invalidateChunk(cx, cz);
         int lx = x & 15;
         int lz = z & 15;
         if (lx == 0) {
            invalidateChunk(cx - 1, cz);
         } else if (lx == 15) {
            invalidateChunk(cx + 1, cz);
         }

         if (lz == 0) {
            invalidateChunk(cx, cz - 1);
         } else if (lz == 15) {
            invalidateChunk(cx, cz + 1);
         }
      }
   }

   public static void onPacket(Packet<?> packet) {
      if (packet instanceof ClientboundLevelChunkWithLightPacket chunkPacket) {
         invalidateChunk(chunkPacket.x(), chunkPacket.z());
         cacheRevision.incrementAndGet();
      } else if (packet instanceof ClientboundRespawnPacket || packet instanceof ClientboundLoginPacket) {
         clearAll();
      } else if (hasAnyCachedChunks()) {
         if (packet instanceof ClientboundBlockUpdatePacket update) {
            init_V_BlockPos(update.getPos());
         } else if (packet instanceof ClientboundSectionBlocksUpdatePacket sectionUpdate) {
            boolean[] marked = new boolean[]{false};
            sectionUpdate.runUpdates((pos, state) -> {
               if (!marked[0]) {
                  marked[0] = true;
                  init_V_BlockPos(pos);
               }
            });
         } else if (packet instanceof ClientboundBlockEntityDataPacket beData) {
            init_V_BlockPos(beData.getPos());
         } else if (packet instanceof ClientboundForgetLevelChunkPacket forget) {
            invalidateChunk(forget.pos().x(), forget.pos().z());
         }
      }
   }

   record EspBox(AABB box, Vec3 tracer, int color) {
   }

   private record ChunkResult(String stamp, long gameTime, List<EspChunkWorker.EspBox> boxes) {
   }

   interface ChunkScanner {
      void scan(LevelChunk var1, List<EspChunkWorker.EspBox> var2);
   }
}
