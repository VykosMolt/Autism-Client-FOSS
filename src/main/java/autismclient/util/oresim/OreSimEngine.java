package autismclient.util.oresim;

import autismclient.AutismClientAddon;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.IntPredicate;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

public final class OreSimEngine {
   public static final int init_I = 8;
   private static final int load_I = 4;
   private static final int read_I = 8;
   private static final int write_I = 5;
   private static final int update_I = 600000;
   private static final int sync_I = 3;
   private static final long init_J = TimeUnit.SECONDS.toNanos(5L);
   private static final Object init_Obj = new Object();
   private static final Map<Long, OreSimEngine.Inner_DataUtil> init_Map = new ConcurrentHashMap<>();
   private static final Map<Long, OreSimEngine.Inner_DataUtil> load_Map = new HashMap<>();
   private static final LongOpenHashSet inFlightTiles = new LongOpenHashSet();
   private static boolean initialChunkPending;
   private static final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
   private static final int flush_I = 200000;
   private static final OreSimWorkerPool init_ConfigStore_2 = new OreSimWorkerPool("Autism-OreSim-Worldgen");
   private static final LinkedHashMap<OreSimEngine.Inner_StreamUtil, autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap> init_LinkedHashMap = new LinkedHashMap<OreSimEngine.Inner_StreamUtil, autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap>(
      4, 0.75F, true
   ) {
      @Override
      protected boolean removeEldestEntry(Entry<OreSimEngine.Inner_StreamUtil, autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap> eldest) {
         return this.size() > 3;
      }
   };
   private static OreSimEngine.Inner_StreamUtil init_StreamUtil_StreamU;
   private static volatile autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap init_ConfigStore;
   private static volatile long load_J;
   private static volatile boolean init_Z;
   private static volatile boolean load_Z;
   private static boolean read_Z;
   private static volatile boolean write_Z;
   private static volatile boolean update_Z;
   private static volatile boolean sync_Z;
   private static volatile String init_Str = "";
   private static int close_I;
   private static int open_I;
   private static int poll = 1;
   private static boolean flush_Z;
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private static volatile int push;
   private static volatile boolean close_Z;
   private static volatile boolean open_Z;
   private static volatile long read_J;
   private static long write_J;
   private static boolean auditHasWriteDeadline;

   private OreSimEngine() {
   }

   public static OreSimEngine.Inner_BufferHelper status() {
      if (write_Z) {
         return OreSimEngine.Inner_BufferHelper.ERROR;
      } else if (update_Z) {
         return OreSimEngine.Inner_BufferHelper.UNSUPPORTED_DIMENSION;
      } else if (sync_Z) {
         return OreSimEngine.Inner_BufferHelper.UNVERIFIED_WORLDGEN;
      } else if (init_StreamUtil_StreamU == null) {
         return OreSimEngine.Inner_BufferHelper.IDLE;
      } else if (!init_Z && init_ConfigStore != null) {
         return load_Z ? OreSimEngine.Inner_BufferHelper.GENERATING : OreSimEngine.Inner_BufferHelper.READY;
      } else {
         return OreSimEngine.Inner_BufferHelper.LOADING_CONTEXT;
      }
   }

   public static boolean isReady() {
      return init_ConfigStore != null && !write_Z && !update_Z && !sync_Z;
   }

   public static boolean load_Z() {
      return init_Z || load_Z;
   }

   public static boolean hasError() {
      return write_Z;
   }

   public static String errorMessage() {
      return init_Str;
   }

   public static int simulatedChunkCount() {
      return init_Map.size();
   }

   public static OreSimEngine.Inner_ConfigStore progress() {
      synchronized (init_Obj) {
         if (init_StreamUtil_StreamU == null) {
            return new OreSimEngine.Inner_ConfigStore(load_J, 0, 0);
         }

         int centerX = SectionPos.blockToSectionCoord(close_I);
         int centerZ = SectionPos.blockToSectionCoord(open_I);
         int radius = Math.max(1, Math.min(poll, 8));
         int diameter = radius * 2 + 1;
         int complete = 0;

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               long key = ChunkPos.pack(centerX + dx, centerZ + dz);
               if (init_Map.containsKey(key) || load_Map.containsKey(key)) {
                  complete++;
               }
            }
         }

         return new OreSimEngine.Inner_ConfigStore(load_J, complete, diameter * diameter);
      }
   }

   public static int positionCount() {
      return push;
   }

   public static boolean write_Z() {
      return close_Z;
   }

   public static int revision() {
      return init_AtomicInteger.get();
   }

   public static void reset() {
      read_J = 0L;
      synchronized (init_Obj) {
         if (init_StreamUtil_StreamU != null
            || !init_Map.isEmpty()
            || !load_Map.isEmpty()
            || init_Z
            || load_Z
            || read_Z
            || write_Z
            || update_Z
            || sync_Z
            || !init_Str.isEmpty()
            || !auditIsDisprovenEmpty()
            || !init_ConfigStore_2.isIdle()) {
            load_J++;
            update_V();
            init_StreamUtil_StreamU = null;
            init_ConfigStore = null;
            init_Z = false;
            load_Z = false;
            read_Z = false;
            flush_Z = false;
            write_Z = false;
            update_Z = false;
            sync_Z = false;
            init_Str = "";
            close_Z = false;
            write_J = 0L;
            auditHasWriteDeadline = false;
            push = 0;
            init_Map.clear();
            load_Map.clear();
            inFlightTiles.clear();
            initialChunkPending = false;
            synchronized (init_LongOpenHashSet) {
               init_LongOpenHashSet.clear();
            }
            init_AtomicInteger.incrementAndGet();
         }
      }
   }

   private static boolean auditIsDisprovenEmpty() {
      synchronized (init_LongOpenHashSet) {
         return init_LongOpenHashSet.isEmpty();
      }
   }

   public static void load_V() {
      if (read_J == 0L) {
         read_J = System.nanoTime();
      }
   }

   public static void read_V() {
      read_J = 0L;
   }

   public static boolean update_Z() {
      return read_J != 0L;
   }

   public static boolean init(long retentionNanos) {
      long since = read_J;
      if (since != 0L && System.nanoTime() - since >= retentionNanos) {
         read_J = 0L;
         reset();
         return true;
      } else {
         return false;
      }
   }

   public static void write_V() {
      boolean cleared;
      synchronized (init_LongOpenHashSet) {
         cleared = !init_LongOpenHashSet.isEmpty();
         if (cleared) {
            init_LongOpenHashSet.clear();
         }
      }
      if (cleared) {
         init_AtomicInteger.incrementAndGet();
      }
   }

   public static void init(ClientLevel level, BlockPos playerPos, Long worldSeed, int radius, int enabledMask) {
      if (level != null && playerPos != null && worldSeed != null) {
         ResourceKey<Level> dimension = level.dimension();
         if (!init(dimension)) {
            synchronized (init_Obj) {
               if (!update_Z || init_StreamUtil_StreamU != null || !init_Map.isEmpty() || !load_Map.isEmpty() || init_Z || load_Z || sync_Z) {
                  load_J++;
                  update_V();
                  init_StreamUtil_StreamU = null;
                  init_ConfigStore = null;
                  init_Z = false;
                  load_Z = false;
                  read_Z = false;
                  flush_Z = false;
                  init_Map.clear();
                  load_Map.clear();
                  inFlightTiles.clear();
                  initialChunkPending = false;
                  synchronized (init_LongOpenHashSet) {
                     init_LongOpenHashSet.clear();
                  }
                  push = 0;
                  init_AtomicInteger.incrementAndGet();
               }

               write_Z = false;
               update_Z = true;
               sync_Z = false;
               init_Str = "Unsupported dimension " + dimension.identifier();
            }
         } else {
            OreSimEngine.Inner_StreamUtil wanted = new OreSimEngine.Inner_StreamUtil(worldSeed, dimension);
            synchronized (init_Obj) {
               if (!wanted.equals(init_StreamUtil_StreamU)) {
                  init(wanted);
               }

               close_I = playerPos.getX();
               open_I = playerPos.getZ();
               poll = Math.max(1, Math.min(radius, 8));
               flush();
               sync_V();
               if (init_ConfigStore != null && !load_Z && !read_Z && (!auditHasWriteDeadline || System.nanoTime() - write_J >= 0L)) {
                  init_V_ConfigStore_J(init_ConfigStore, load_J);
               } else if (!sync_Z && !read_Z && enabledMask != 0 && !write_Z && !close_Z && init_ConfigStore != null && !initialChunkPending) {
                  if (!flush_Z) {
                     int centerChunkX = SectionPos.blockToSectionCoord(close_I);
                     int centerChunkZ = SectionPos.blockToSectionCoord(open_I);
                     long centerKey = ChunkPos.pack(centerChunkX, centerChunkZ);
                     if (!init_Map.containsKey(centerKey) && !load_Map.containsKey(centerKey)) {
                        init_V_I(centerChunkX, centerChunkZ, init_ConfigStore, load_J);
                        return;
                     }

                     flush_Z = true;
                  }

                  int maxParallel = init_ConfigStore_2.maxWorkers();

                  while (inFlightTiles.size() < maxParallel) {
                     OreSimEngine.Inner_TaskQueue tile = init_StreamUtil_TaskQue_I(close_I, open_I, poll);
                     if (tile == null) {
                        break;
                     }

                     init_V_StreamUtil_TaskQue(tile, init_ConfigStore, load_J);
                  }
               }
            }
         }
      } else {
         reset();
      }
   }

   private static boolean init(ResourceKey<Level> dimension) {
      return Level.OVERWORLD.equals(dimension) || Level.NETHER.equals(dimension) || Level.END.equals(dimension);
   }

   private static void init(OreSimEngine.Inner_StreamUtil wanted) {
      load_J++;
      update_V();
      init_StreamUtil_StreamU = wanted;
      init_ConfigStore = init_LinkedHashMap.get(wanted);
      init_Z = init_ConfigStore == null;
      load_Z = false;
      read_Z = false;
      flush_Z = false;
      write_Z = false;
      update_Z = false;
      sync_Z = false;
      init_Str = "";
      close_Z = false;
      write_J = 0L;
      auditHasWriteDeadline = false;
      push = 0;
      synchronized (init_LongOpenHashSet) {
         init_LongOpenHashSet.clear();
      }
      if (!init_Map.isEmpty()) {
         init_Map.clear();
      }

      load_Map.clear();
      inFlightTiles.clear();
      initialChunkPending = false;
      init_AtomicInteger.incrementAndGet();
      if (init_ConfigStore == null) {
         init_V_StreamUtil_StreamU_J(wanted, load_J);
      }
   }

   private static void init_V_StreamUtil_StreamU_J(OreSimEngine.Inner_StreamUtil key, long token) {
      init_ConfigStore_2.init(() -> {
         autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap built;
         boolean tagsVerified;
         try {
            built = autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap.init(key.init_J(), key.init_ResourceKey());
            tagsVerified = built.init_Z();
         } catch (Throwable error) {
            init_V_J(token, "Minecraft 26.3 worldgen bootstrap failed", error);
            return;
         }

         synchronized (init_Obj) {
            if (token == load_J && key.equals(init_StreamUtil_StreamU)) {
               init_LinkedHashMap.put(key, built);
               init_ConfigStore = built;
               init_Z = false;
               write_J = System.nanoTime() + init_J;
               auditHasWriteDeadline = true;
               if (!tagsVerified) {
                  init("Minecraft block tags differ from the vanilla 26.3 worldgen profile");
               }
            }
         }
      });
   }

   private static void init_V_I(int chunkX, int chunkZ, autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap context, long token) {
      initialChunkPending = true;
      load_Z = true;
      init_ConfigStore_2.init(() -> {
         try {
            try {
               if (!context.init_Z()) {
                  init_V_J_Str(token, "Minecraft block tags changed before initial chunk generation");
                  return;
               }

               int regionMinX = chunkX - 2;
               int regionMinZ = chunkZ - 2;
               Map<Long, ChunkAccess> generated = new autismclient.util.oresim.worldgen.OreSimRegionGenerator(context).generateRegion(regionMinX, regionMinZ, 5, () -> token != load_J);
               long chunkKey = ChunkPos.pack(chunkX, chunkZ);
               ChunkAccess chunk = generated.get(chunkKey);
               if (chunk == null) {
                  throw new IllegalStateException("Generator omitted initial complete chunk " + chunkX + ", " + chunkZ);
               }

               OreSimEngine.Inner_DataUtil ores = init_StreamUtil_DataUti_ChunkAccess_BooleanSupplier(chunk, () -> token != load_J);
               if (!context.init_Z()) {
                  init_V_J_Str(token, "Minecraft block tags changed during initial chunk generation");
                  return;
               }

               synchronized (init_Obj) {
                  if (token != load_J || context != init_ConfigStore) {
                     return;
                  }

                  if (!init_Map.containsKey(chunkKey) && !load_Map.containsKey(chunkKey)) {
                     if (push + ores.init() > 600000) {
                        close_Z = true;
                        return;
                     }

                     init_Map.put(chunkKey, ores);
                     push = push + ores.init();
                     init_AtomicInteger.incrementAndGet();
                  }

                  write_J = System.nanoTime() + init_J;
                  auditHasWriteDeadline = true;
                  flush_Z = true;
               }
            } catch (CancellationException var33) {
            } catch (Throwable error) {
               init_V_J(token, "Minecraft 26.3 initial chunk generation failed at " + chunkX + ", " + chunkZ, error);
            }
         } finally {
            synchronized (init_Obj) {
               if (token == load_J) {
                  initialChunkPending = false;
                  load_Z = !inFlightTiles.isEmpty();
               }
            }
         }
      });
   }

   private static void init_V_StreamUtil_TaskQue(OreSimEngine.Inner_TaskQueue tile, autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap context, long token) {
      long tileKey = ChunkPos.pack(tile.init_I(), tile.load());
      inFlightTiles.add(tileKey);
      load_Z = true;
      LongOpenHashSet alreadyComplete = new LongOpenHashSet(16);

      for (int dx = 0; dx < 4; dx++) {
         for (int dz = 0; dz < 4; dz++) {
            long key = ChunkPos.pack(tile.init_I() + dx, tile.load() + dz);
            if (init_Map.containsKey(key) || load_Map.containsKey(key)) {
               alreadyComplete.add(key);
            }
         }
      }

      init_ConfigStore_2.submitParallel(() -> {
         try {
            if (!context.init_Z()) {
               init_V_J_Str(token, "Minecraft block tags changed before region generation");
            } else {
               int regionMinX = tile.init_I() - 2;
               int regionMinZ = tile.load() - 2;
               Map<Long, ChunkAccess> generated = new autismclient.util.oresim.worldgen.OreSimRegionGenerator(context).generateRegion(regionMinX, regionMinZ, 8, () -> token != load_J);
               Map<Long, OreSimEngine.Inner_DataUtil> completed = new HashMap<>(16);

               for (int dxx = 0; dxx < 4; dxx++) {
                  for (int dzx = 0; dzx < 4; dzx++) {
                     int chunkX = tile.init_I() + dxx;
                     int chunkZ = tile.load() + dzx;
                     long chunkKey = ChunkPos.pack(chunkX, chunkZ);
                     if (!alreadyComplete.contains(chunkKey)) {
                        ChunkAccess chunk = generated.get(chunkKey);
                        if (chunk == null) {
                           throw new IllegalStateException("Generator omitted complete chunk " + chunkX + ", " + chunkZ);
                        }

                        OreSimEngine.Inner_DataUtil ores = init_StreamUtil_DataUti_ChunkAccess_BooleanSupplier(chunk, () -> token != load_J);
                        completed.put(chunkKey, ores);
                     }
                  }
               }

               if (!context.init_Z()) {
                  init_V_J_Str(token, "Minecraft block tags changed during region generation");
               } else {
                  synchronized (init_Obj) {
                     if (token == load_J && context == init_ConfigStore) {
                        int added = 0;

                        for (Entry<Long, OreSimEngine.Inner_DataUtil> entry : completed.entrySet()) {
                           if (!init_Map.containsKey(entry.getKey()) && !load_Map.containsKey(entry.getKey())) {
                              added += entry.getValue().init();
                           }
                        }

                        if (push + added > 600000) {
                           close_Z = true;
                        } else {
                           for (Entry<Long, OreSimEngine.Inner_DataUtil> entry : completed.entrySet()) {
                              if (!init_Map.containsKey(entry.getKey()) && !load_Map.containsKey(entry.getKey())) {
                                 load_Map.put(entry.getKey(), entry.getValue());
                                 push = push + entry.getValue().init();
                              }
                           }

                           write_J = System.nanoTime() + init_J;
                           auditHasWriteDeadline = true;
                        }
                     }
                  }
               }
            }
         } catch (CancellationException var36) {
         } catch (Throwable error) {
            init_V_J(token, "Minecraft 26.3 region generation failed at tile " + tile.init_I() + ", " + tile.load(), error);
         } finally {
            synchronized (init_Obj) {
               if (token == load_J) {
                  inFlightTiles.remove(tileKey);
                  load_Z = initialChunkPending || !inFlightTiles.isEmpty();
               }
            }
         }
      });
   }

   private static OreSimEngine.Inner_DataUtil init_StreamUtil_DataUti_ChunkAccess_BooleanSupplier(ChunkAccess chunk, BooleanSupplier cancelled) {
      LongArrayList positions = new LongArrayList();
      IntArrayList stateIds = new IntArrayList();
      autismclient.util.oresim.worldgen.OreSimRegionGenerator.scanChunk(chunk, state -> OreBlockCatalog.init(state.getBlock()), (x, y, z, state) -> {
         int stateId = OreBlockCatalog.Inner_StreamUtil.init(state);
         if (stateId >= 0) {
            positions.add(BlockPos.asLong(x, y, z));
            stateIds.add(stateId);
         }
      }, cancelled);
      return positions.isEmpty()
         ? OreSimEngine.Inner_DataUtil.init_StreamUtil_DataUti
         : new OreSimEngine.Inner_DataUtil(positions.toLongArray(), stateIds.toIntArray());
   }

   private static void init_V_J(long token, String message, Throwable error) {
      boolean accepted = false;
      synchronized (init_Obj) {
         if (token != load_J) {
            return;
         }

         write_Z = true;
         init_Z = false;
         load_Z = false;
         read_Z = false;
         flush_Z = false;
         init_Str = message + ": " + error.getClass().getSimpleName() + (error.getMessage() == null ? "" : " - " + error.getMessage());
         init_ConfigStore = null;
         init_Map.clear();
         load_Map.clear();
         inFlightTiles.clear();
         initialChunkPending = false;
         push = 0;
         sync_Z = false;
         init_AtomicInteger.incrementAndGet();
         accepted = true;
      }

      if (accepted) {
         AutismClientAddon.LOG.warn(message, error);
      }
   }

   private static void init_V_J_Str(long token, String message) {
      synchronized (init_Obj) {
         if (token == load_J) {
            init(message);
         }
      }
   }

   private static void init(String message) {
      load_J++;
      update_V();
      init_Z = false;
      load_Z = false;
      read_Z = false;
      flush_Z = false;
      write_Z = false;
      update_Z = false;
      sync_Z = true;
      init_Str = message;
      close_Z = false;
      write_J = System.nanoTime() + init_J;
      auditHasWriteDeadline = true;
      push = 0;
      init_Map.clear();
      load_Map.clear();
      inFlightTiles.clear();
      initialChunkPending = false;
      synchronized (init_LongOpenHashSet) {
         init_LongOpenHashSet.clear();
      }
      init_AtomicInteger.incrementAndGet();
   }

   private static void update_V() {
      init_ConfigStore_2.init_V();
   }

   private static void init_V_ConfigStore_J(autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap context, long token) {
      read_Z = true;
      init_ConfigStore_2.init(() -> {
         boolean verified = false;
         Throwable failure = null;

         try {
            verified = context.init_Z();
         } catch (Throwable error) {
            failure = error;
         }

         if (failure != null) {
            AutismClientAddon.LOG.warn("OreSim could not verify Minecraft block tags", failure);
         }

         synchronized (init_Obj) {
            if (token == load_J && context == init_ConfigStore) {
               read_Z = false;
               write_J = System.nanoTime() + init_J;
               auditHasWriteDeadline = true;
               if (!verified) {
                  init(failure == null ? "Minecraft block tags differ from the vanilla 26.3 worldgen profile" : "Minecraft block tags could not be verified");
               } else if (sync_Z) {
                  load_J++;
                  sync_Z = false;
                  init_Str = "";
                  flush_Z = false;
                  init_AtomicInteger.incrementAndGet();
               }
            }
         }
      });
   }

   private static OreSimEngine.Inner_TaskQueue init_StreamUtil_TaskQue_I(int blockX, int blockZ, int radius) {
      int centerChunkX = SectionPos.blockToSectionCoord(blockX);
      int centerChunkZ = SectionPos.blockToSectionCoord(blockZ);
      int minTileX = Math.floorDiv(centerChunkX - radius, 4);
      int maxTileX = Math.floorDiv(centerChunkX + radius, 4);
      int minTileZ = Math.floorDiv(centerChunkZ - radius, 4);
      int maxTileZ = Math.floorDiv(centerChunkZ + radius, 4);
      OreSimEngine.Inner_TaskQueue best = null;

      for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
         for (int tileZ = minTileZ; tileZ <= maxTileZ; tileZ++) {
            int minChunkX = tileX * 4;
            int minChunkZ = tileZ * 4;
            if (!inFlightTiles.contains(ChunkPos.pack(minChunkX, minChunkZ)) && !init_Z_I_I(minChunkX, minChunkZ)) {
               double distance = init_D_D(blockX + 0.5, blockZ + 0.5, minChunkX, minChunkZ);
               if (best == null || distance < best.init_D() || distance == best.init_D() && init_Z_I(minChunkX, minChunkZ, best)) {
                  best = new OreSimEngine.Inner_TaskQueue(minChunkX, minChunkZ, distance);
               }
            }
         }
      }

      return best;
   }

   private static boolean init_Z_I_I(int minChunkX, int minChunkZ) {
      for (int dx = 0; dx < 4; dx++) {
         for (int dz = 0; dz < 4; dz++) {
            long key = ChunkPos.pack(minChunkX + dx, minChunkZ + dz);
            if (!init_Map.containsKey(key) && !load_Map.containsKey(key)) {
               return false;
            }
         }
      }

      return true;
   }

   private static double init_D_D(double x, double z, int minChunkX, int minChunkZ) {
      double minX = minChunkX * 16.0 + 0.5;
      double minZ = minChunkZ * 16.0 + 0.5;
      double maxX = (minChunkX + 4) * 16.0 - 0.5;
      double maxZ = (minChunkZ + 4) * 16.0 - 0.5;
      double dx = x < minX ? minX - x : (x > maxX ? x - maxX : 0.0);
      double dz = z < minZ ? minZ - z : (z > maxZ ? z - maxZ : 0.0);
      return dx * dx + dz * dz;
   }

   private static boolean init_Z_I(int x, int z, OreSimEngine.Inner_TaskQueue other) {
      return x < other.init_I() || x == other.init_I() && z < other.load();
   }

   private static void sync_V() {
      if (!load_Map.isEmpty()) {
         NearestChunkSelector.Inner_DataUtil missing = init_TaskQueue_DataUtil();
         boolean changed = false;
         Iterator<Entry<Long, OreSimEngine.Inner_DataUtil>> iterator = load_Map.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<Long, OreSimEngine.Inner_DataUtil> entry = iterator.next();
            int chunkX = ChunkPos.getX(entry.getKey());
            int chunkZ = ChunkPos.getZ(entry.getKey());
            if (NearestChunkSelector.init(close_I + 0.5, open_I + 0.5, chunkX, chunkZ, missing)) {
               init_Map.put(entry.getKey(), entry.getValue());
               iterator.remove();
               changed = true;
            }
         }

         if (changed) {
            init_AtomicInteger.incrementAndGet();
         }
      }
   }

   private static NearestChunkSelector.Inner_DataUtil init_TaskQueue_DataUtil() {
      int centerX = SectionPos.blockToSectionCoord(close_I);
      int centerZ = SectionPos.blockToSectionCoord(open_I);
      return NearestChunkSelector.select(
         close_I + 0.5, open_I + 0.5, centerX, centerZ, poll, key -> init_Map.containsKey(key) || load_Map.containsKey(key)
      );
   }

   private static void flush() {
      int centerX = SectionPos.blockToSectionCoord(close_I);
      int centerZ = SectionPos.blockToSectionCoord(open_I);
      int keep = poll + 4;
      int removed = 0;

      for (Entry<Long, OreSimEngine.Inner_DataUtil> entry : init_Map.entrySet()) {
         long key = entry.getKey();
         if ((Math.abs(ChunkPos.getX(key) - centerX) > keep || Math.abs(ChunkPos.getZ(key) - centerZ) > keep) && init_Map.remove(key, entry.getValue())) {
            removed += entry.getValue().init();
         }
      }

      Iterator<Entry<Long, OreSimEngine.Inner_DataUtil>> pending = load_Map.entrySet().iterator();

      while (pending.hasNext()) {
         Entry<Long, OreSimEngine.Inner_DataUtil> entry = pending.next();
         long key = entry.getKey();
         if (Math.abs(ChunkPos.getX(key) - centerX) > keep || Math.abs(ChunkPos.getZ(key) - centerZ) > keep) {
            removed += entry.getValue().init();
            pending.remove();
         }
      }

      if (removed > 0) {
         push = Math.max(0, push - removed);
         close_Z = false;
         init_AtomicInteger.incrementAndGet();
      }
   }

   public static boolean sync_Z() {
      return open_Z;
   }

   public static int init(double eyeX, double eyeY, double eyeZ, int radius, IntPredicate stateAllowed, int cap, long[] outPos, int[] outState) {
      open_Z = false;
      if (cap > 0 && outPos != null && outState != null && !init_Map.isEmpty()) {
         int limit = Math.min(cap, Math.min(outPos.length, outState.length));
         if (limit <= 0) {
            return 0;
         }

         NearestOreHeap nearest = new NearestOreHeap(limit);
         int centerX = SectionPos.blockToSectionCoord((int)Math.floor(eyeX));
         int centerZ = SectionPos.blockToSectionCoord((int)Math.floor(eyeZ));
         int drawRadius = Math.max(1, Math.min(radius, 8));
         synchronized (init_LongOpenHashSet) {
            boolean anyDisproven = !init_LongOpenHashSet.isEmpty();

            for (int ring = 0; ring <= drawRadius; ring++) {
               if (nearest.isFull()) {
                  double ringMin = Math.max(0.0, (ring - 1) * 16.0);
                  if (ringMin * ringMin > nearest.init_D()) {
                     break;
                  }
               }

               for (int dx = -ring; dx <= ring; dx++) {
                  for (int dz = -ring; dz <= ring; dz++) {
                     if (Math.max(Math.abs(dx), Math.abs(dz)) == ring) {
                        OreSimEngine.Inner_DataUtil ores = init_Map.get(ChunkPos.pack(centerX + dx, centerZ + dz));
                        if (ores != null && ores.init_JArr.length != 0) {
                           for (int i = 0; i < ores.init_JArr.length; i++) {
                              int stateId = ores.init_IArr[i];
                              if (stateAllowed == null || stateAllowed.test(stateId)) {
                                 long packed = ores.init_JArr[i];
                                 if (!anyDisproven || !init_LongOpenHashSet.contains(packed)) {
                                    double px = BlockPos.getX(packed) + 0.5 - eyeX;
                                    double py = BlockPos.getY(packed) + 0.5 - eyeY;
                                    double pz = BlockPos.getZ(packed) + 0.5 - eyeZ;
                                    double distance = px * px + py * py + pz * pz;
                                    nearest.init(distance, packed, stateId);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         open_Z = nearest.load();
         return nearest.init(outPos, outState);
      } else {
         return 0;
      }
   }

   public static void collectPositions(BlockPos center, int blockRadius, LongOpenHashSet out) {
      if (center != null && out != null && !init_Map.isEmpty()) {
         int minX = center.getX() - blockRadius;
         int maxX = center.getX() + blockRadius;
         int minY = center.getY() - blockRadius;
         int maxY = center.getY() + blockRadius;
         int minZ = center.getZ() - blockRadius;
         int maxZ = center.getZ() + blockRadius;

         synchronized (init_LongOpenHashSet) {
            for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
               for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                  OreSimEngine.Inner_DataUtil ores = init_Map.get(ChunkPos.pack(chunkX, chunkZ));
                  if (ores != null) {
                     for (long packed : ores.init_JArr) {
                        if (!init_LongOpenHashSet.contains(packed)) {
                           int x = BlockPos.getX(packed);
                           int y = BlockPos.getY(packed);
                           int z = BlockPos.getZ(packed);
                           if (x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ) {
                              out.add(packed);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void forEachPosition(BlockPos center, int blockRadius, OreSimEngine.Inner_CachePool judge) {
      if (center != null && judge != null && !init_Map.isEmpty()) {
         int minX = center.getX() - blockRadius;
         int maxX = center.getX() + blockRadius;
         int minY = center.getY() - blockRadius;
         int maxY = center.getY() + blockRadius;
         int minZ = center.getZ() - blockRadius;
         int maxZ = center.getZ() + blockRadius;
         boolean changed = false;

         synchronized (init_LongOpenHashSet) {
            for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
               for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                  OreSimEngine.Inner_DataUtil ores = init_Map.get(ChunkPos.pack(chunkX, chunkZ));
                  if (ores != null) {
                     for (int i = 0; i < ores.init_JArr.length; i++) {
                        long packed = ores.init_JArr[i];
                        if (!init_LongOpenHashSet.contains(packed)) {
                           int x = BlockPos.getX(packed);
                           int y = BlockPos.getY(packed);
                           int z = BlockPos.getZ(packed);
                           if (x >= minX
                              && x <= maxX
                              && y >= minY
                              && y <= maxY
                              && z >= minZ
                              && z <= maxZ
                              && judge.shouldDrop(ores.init_IArr[i], packed)
                              && init_LongOpenHashSet.size() < 200000) {
                              changed |= init_LongOpenHashSet.add(packed);
                           }
                        }
                     }
                  }
               }
            }
         }

         if (changed) {
            init_AtomicInteger.incrementAndGet();
         }
      }
   }

   public static int write_I() {
      return -1;
   }

   public enum Inner_BufferHelper {
      IDLE,
      LOADING_CONTEXT,
      GENERATING,
      READY,
      UNVERIFIED_WORLDGEN,
      UNSUPPORTED_DIMENSION,
      ERROR;
   }

   @FunctionalInterface
   public interface Inner_CachePool {
      boolean shouldDrop(int var1, long var2);
   }

   public record Inner_ConfigStore(long init_J, int done, int load) {
      public Inner_ConfigStore {
         load = Math.max(0, load);
         done = Math.max(0, Math.min(done, load));
      }

      public double init_D() {
         return this.load == 0 ? 0.0 : (double)this.done / this.load;
      }

      public boolean isComplete() {
         return this.load > 0 && this.done >= this.load;
      }
   }

   private static final class Inner_DataUtil {
      static final OreSimEngine.Inner_DataUtil init_StreamUtil_DataUti = new OreSimEngine.Inner_DataUtil(new long[0], new int[0]);
      final long[] init_JArr;
      final int[] init_IArr;

      Inner_DataUtil(long[] positions, int[] stateIds) {
         this.init_JArr = positions;
         this.init_IArr = stateIds;
      }

      int init() {
         return this.init_JArr.length;
      }
   }

   private record Inner_StreamUtil(long init_J, ResourceKey<Level> init_ResourceKey) {
   }

   private record Inner_TaskQueue(int init_I, int load, double init_D) {
      long init_J() {
         return ChunkPos.pack(this.init_I, this.load);
      }
   }
}
