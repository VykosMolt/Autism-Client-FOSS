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

public final class af {
   static final af init_af = new af();
   static final af load = new af();
   static final af read = new af();
   static final af write = new af();
   static final af update = new af();
   static final af sync = new af();
   static final af flush = new af();
   private static final long init_J = 100L;
   private static final int init_I = 4096;
   private static volatile Object init_Obj;
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private static final AtomicInteger ESP_WORKER_IDS = new AtomicInteger();
   private static final ExecutorService ESP_POOL = Executors.newFixedThreadPool(
      Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors() / 2)), r -> {
         Thread t = new Thread(r, "Autism-ESP-Worker-" + ESP_WORKER_IDS.incrementAndGet());
         t.setDaemon(true);
         t.setPriority(Math.max(1, 4));
         return t;
      }
   );
   private final ConcurrentHashMap<Long, af.Inner_DataUtil> init_ConcurrentHashMap = new ConcurrentHashMap<>();
   private final Set<Long> inFlight = ConcurrentHashMap.newKeySet();

   static void submitEspWork(Runnable task) {
      if (task != null) {
         ESP_POOL.execute(task);
      }
   }

   public static int init_I() {
      return init_AtomicInteger.get();
   }

   private af() {
   }

   List<af.Inner_CachePool> initAsync(LevelChunk chunk, long gameTime, String stamp, af.Inner_StreamUtil scanner) {
      long key = chunk.getPos().pack();
      af.Inner_DataUtil cached = this.init_ConcurrentHashMap.get(key);
      if (cached != null && cached.init_Str.equals(stamp) && gameTime - cached.init_J < 100L) {
         return cached.init_List;
      }

      Object expectedLevel = init_Obj;
      if (this.inFlight.add(key)) {
         ESP_POOL.execute(() -> {
            try {
               if (init_Obj != expectedLevel) {
                  return;
               }

               if (this.init_ConcurrentHashMap.size() >= 4096) {
                  this.init_V_ChunkPos(chunk.getPos());
               }

               List<af.Inner_CachePool> entries = new ArrayList<>();
               scanner.scan(chunk, entries);
               if (init_Obj == expectedLevel) {
                  List<af.Inner_CachePool> frozen = List.copyOf(entries);
                  af.Inner_DataUtil prev = this.init_ConcurrentHashMap.put(key, new af.Inner_DataUtil(stamp, gameTime, frozen));
                  if (prev == null || !prev.init_List.equals(frozen)) {
                     init_AtomicInteger.incrementAndGet();
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

      return cached != null && cached.init_Str.equals(stamp) ? cached.init_List : List.of();
   }

   List<af.Inner_CachePool> init(LevelChunk chunk, long gameTime, String stamp, af.Inner_StreamUtil scanner) {
      long key = chunk.getPos().pack();
      af.Inner_DataUtil cached = this.init_ConcurrentHashMap.get(key);
      if (cached != null && cached.init_Str.equals(stamp) && gameTime - cached.init_J < 100L) {
         return cached.init_List;
      }

      if (this.init_ConcurrentHashMap.size() >= 4096) {
         this.init_V_ChunkPos(chunk.getPos());
      }

      List<af.Inner_CachePool> entries = new ArrayList<>();
      scanner.scan(chunk, entries);
      List<af.Inner_CachePool> frozen = List.copyOf(entries);
      this.init_ConcurrentHashMap.put(key, new af.Inner_DataUtil(stamp, gameTime, frozen));
      if (cached == null || !cached.init_List.equals(frozen)) {
         init_AtomicInteger.incrementAndGet();
      }

      return frozen;
   }

   private void init_V_ChunkPos(ChunkPos center) {
      int target = 3072;

      for (int keep = 24; keep >= 4 && this.init_ConcurrentHashMap.size() > target; keep -= 4) {
         int radius = keep;
         this.init_ConcurrentHashMap
            .keySet()
            .removeIf(key -> Math.max(Math.abs(ChunkPos.getX(key) - center.x()), Math.abs(ChunkPos.getZ(key) - center.z())) > radius);
      }

      if (this.init_ConcurrentHashMap.size() >= 4096) {
         this.init_ConcurrentHashMap.clear();
      }
   }

   static void init_V_Obj(Object level) {
      if (init_Obj != level) {
         init_Obj = level;
         load();
      }
   }

   static void init_V() {
      read.inFlight.clear();
      if (!read.init_ConcurrentHashMap.isEmpty()) {
         read.init_ConcurrentHashMap.clear();
         init_AtomicInteger.incrementAndGet();
      }
   }

   static void load() {
      init_af.init_ConcurrentHashMap.clear();
      init_af.inFlight.clear();
      load.init_ConcurrentHashMap.clear();
      load.inFlight.clear();
      read.init_ConcurrentHashMap.clear();
      read.inFlight.clear();
      write.init_ConcurrentHashMap.clear();
      write.inFlight.clear();
      update.init_ConcurrentHashMap.clear();
      update.inFlight.clear();
      sync.init_ConcurrentHashMap.clear();
      sync.inFlight.clear();
      flush.init_ConcurrentHashMap.clear();
      flush.inFlight.clear();
      init_AtomicInteger.incrementAndGet();
   }

   private static void init(int chunkX, int chunkZ) {
      long key = ChunkPos.pack(chunkX, chunkZ);
      boolean had = init_af.init_ConcurrentHashMap.remove(key) != null;
      had |= load.init_ConcurrentHashMap.remove(key) != null;
      had |= read.init_ConcurrentHashMap.remove(key) != null;
      had |= write.init_ConcurrentHashMap.remove(key) != null;
      had |= update.init_ConcurrentHashMap.remove(key) != null;
      had |= sync.init_ConcurrentHashMap.remove(key) != null;
      had |= flush.init_ConcurrentHashMap.remove(key) != null;
      if (had) {
         init_AtomicInteger.incrementAndGet();
      }
   }

   private static boolean hasAnyCachedChunks() {
      return !init_af.init_ConcurrentHashMap.isEmpty()
         || !load.init_ConcurrentHashMap.isEmpty()
         || !read.init_ConcurrentHashMap.isEmpty()
         || !write.init_ConcurrentHashMap.isEmpty()
         || !update.init_ConcurrentHashMap.isEmpty()
         || !sync.init_ConcurrentHashMap.isEmpty()
         || !flush.init_ConcurrentHashMap.isEmpty();
   }

   public static void init_V_BlockPos(BlockPos pos) {
      if (pos != null && hasAnyCachedChunks()) {
         int x = pos.getX();
         int z = pos.getZ();
         int cx = x >> 4;
         int cz = z >> 4;
         init(cx, cz);
         int lx = x & 15;
         int lz = z & 15;
         if (lx == 0) {
            init(cx - 1, cz);
         } else if (lx == 15) {
            init(cx + 1, cz);
         }

         if (lz == 0) {
            init(cx, cz - 1);
         } else if (lz == 15) {
            init(cx, cz + 1);
         }
      }
   }

   public static void init_V_Packet(Packet<?> packet) {
      if (packet instanceof ClientboundLevelChunkWithLightPacket chunkPacket) {
         init(chunkPacket.x(), chunkPacket.z());
         init_AtomicInteger.incrementAndGet();
      } else if (packet instanceof ClientboundRespawnPacket || packet instanceof ClientboundLoginPacket) {
         load();
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
            init(forget.pos().x(), forget.pos().z());
         }
      }
   }

   record Inner_CachePool(AABB init_AABB, Vec3 init_Vec3, int init_I) {
   }

   private record Inner_DataUtil(String init_Str, long init_J, List<af.Inner_CachePool> init_List) {
   }

   interface Inner_StreamUtil {
      void scan(LevelChunk var1, List<af.Inner_CachePool> var2);
   }
}
