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
   private static final int REGION_GEN_SIZE = 5;
   private static final int MAX_CACHED_ORES = 600000;
   private static final int MAX_CACHED_CONTEXTS = 3;
   private static final long AUDIT_WRITE_DELAY_NANOS = TimeUnit.SECONDS.toNanos(5L);
   private static final Object LOCK = new Object();
   private static final Map<Long, OreSimEngine.ChunkOres> chunkOres = new ConcurrentHashMap<>();
   private static final Map<Long, OreSimEngine.ChunkOres> pendingChunkOres = new HashMap<>();
   private static final LongOpenHashSet inFlightTiles = new LongOpenHashSet();
   private static boolean initialChunkPending;
   private static final LongOpenHashSet disprovenPositions = new LongOpenHashSet();
   private static final int MAX_DISPROVEN = 200000;
   private static final OreSimWorkerPool WORKERS = new OreSimWorkerPool("Autism-OreSim-Worldgen");
   private static final LinkedHashMap<OreSimEngine.WorldKey, autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap> contextCache = new LinkedHashMap<OreSimEngine.WorldKey, autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap>(
      4, 0.75F, true
   ) {
      @Override
      protected boolean removeEldestEntry(Entry<OreSimEngine.WorldKey, autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap> eldest) {
         return this.size() > 3;
      }
   };
   private static OreSimEngine.WorldKey activeWorldKey;
   private static volatile autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap worldgenContext;
   private static volatile long generation;
   private static volatile boolean contextLoading;
   private static volatile boolean generating;
   private static boolean verificationRunning;
   private static volatile boolean errored;
   private static volatile boolean unsupportedDimension;
   private static volatile boolean worldgenUnverified;
   private static volatile String statusMessage = "";
   private static int trackedCenterX;
   private static int trackedCenterZ;
   private static int requestedRadius = 1;
   private static boolean centerChunkDone;
   private static final AtomicInteger changeRevision = new AtomicInteger();
   private static volatile int totalPositions;
   private static volatile boolean oreCapReached;
   private static volatile boolean lastQueryCapped;
   private static volatile long pausedSinceNanos;
   private static long auditWriteDeadline;
   private static boolean auditHasWriteDeadline;

   private OreSimEngine() {
   }

   public static OreSimEngine.EngineStatus status() {
      if (errored) {
         return OreSimEngine.EngineStatus.ERROR;
      } else if (unsupportedDimension) {
         return OreSimEngine.EngineStatus.UNSUPPORTED_DIMENSION;
      } else if (worldgenUnverified) {
         return OreSimEngine.EngineStatus.UNVERIFIED_WORLDGEN;
      } else if (activeWorldKey == null) {
         return OreSimEngine.EngineStatus.IDLE;
      } else if (!contextLoading && worldgenContext != null) {
         return generating ? OreSimEngine.EngineStatus.GENERATING : OreSimEngine.EngineStatus.READY;
      } else {
         return OreSimEngine.EngineStatus.LOADING_CONTEXT;
      }
   }

   public static boolean isReady() {
      return worldgenContext != null && !errored && !unsupportedDimension && !worldgenUnverified;
   }

   public static boolean isBusy() {
      return contextLoading || generating;
   }

   public static boolean hasError() {
      return errored;
   }

   public static String errorMessage() {
      return statusMessage;
   }

   public static int simulatedChunkCount() {
      return chunkOres.size();
   }

   public static OreSimEngine.GenerationProgress progress() {
      synchronized (LOCK) {
         if (activeWorldKey == null) {
            return new OreSimEngine.GenerationProgress(generation, 0, 0);
         }

         int centerX = SectionPos.blockToSectionCoord(trackedCenterX);
         int centerZ = SectionPos.blockToSectionCoord(trackedCenterZ);
         int radius = Math.max(1, Math.min(requestedRadius, 8));
         int diameter = radius * 2 + 1;
         int complete = 0;

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               long key = ChunkPos.pack(centerX + dx, centerZ + dz);
               if (chunkOres.containsKey(key) || pendingChunkOres.containsKey(key)) {
                  complete++;
               }
            }
         }

         return new OreSimEngine.GenerationProgress(generation, complete, diameter * diameter);
      }
   }

   public static int positionCount() {
      return totalPositions;
   }

   public static boolean isOreCapReached() {
      return oreCapReached;
   }

   public static int revision() {
      return changeRevision.get();
   }

   public static void reset() {
      pausedSinceNanos = 0L;
      synchronized (LOCK) {
         if (activeWorldKey != null
            || !chunkOres.isEmpty()
            || !pendingChunkOres.isEmpty()
            || contextLoading
            || generating
            || verificationRunning
            || errored
            || unsupportedDimension
            || worldgenUnverified
            || !statusMessage.isEmpty()
            || !auditIsDisprovenEmpty()
            || !WORKERS.isIdle()) {
            generation++;
            cancelWorkers();
            activeWorldKey = null;
            worldgenContext = null;
            contextLoading = false;
            generating = false;
            verificationRunning = false;
            centerChunkDone = false;
            errored = false;
            unsupportedDimension = false;
            worldgenUnverified = false;
            statusMessage = "";
            oreCapReached = false;
            auditWriteDeadline = 0L;
            auditHasWriteDeadline = false;
            totalPositions = 0;
            chunkOres.clear();
            pendingChunkOres.clear();
            inFlightTiles.clear();
            initialChunkPending = false;
            synchronized (disprovenPositions) {
               disprovenPositions.clear();
            }
            changeRevision.incrementAndGet();
         }
      }
   }

   private static boolean auditIsDisprovenEmpty() {
      synchronized (disprovenPositions) {
         return disprovenPositions.isEmpty();
      }
   }

   public static void pause() {
      if (pausedSinceNanos == 0L) {
         pausedSinceNanos = System.nanoTime();
      }
   }

   public static void resume() {
      pausedSinceNanos = 0L;
   }

   public static boolean isPauseTimerRunning() {
      return pausedSinceNanos != 0L;
   }

   public static boolean pausedLongerThan(long retentionNanos) {
      long since = pausedSinceNanos;
      if (since != 0L && System.nanoTime() - since >= retentionNanos) {
         pausedSinceNanos = 0L;
         reset();
         return true;
      } else {
         return false;
      }
   }

   public static void write_V() {
      boolean cleared;
      synchronized (disprovenPositions) {
         cleared = !disprovenPositions.isEmpty();
         if (cleared) {
            disprovenPositions.clear();
         }
      }
      if (cleared) {
         changeRevision.incrementAndGet();
      }
   }

   public static void init(ClientLevel level, BlockPos playerPos, Long worldSeed, int radius, int enabledMask) {
      if (level != null && playerPos != null && worldSeed != null) {
         ResourceKey<Level> dimension = level.dimension();
         if (!supportsDimension(dimension)) {
            synchronized (LOCK) {
               if (!unsupportedDimension || activeWorldKey != null || !chunkOres.isEmpty() || !pendingChunkOres.isEmpty() || contextLoading || generating || worldgenUnverified) {
                  generation++;
                  cancelWorkers();
                  activeWorldKey = null;
                  worldgenContext = null;
                  contextLoading = false;
                  generating = false;
                  verificationRunning = false;
                  centerChunkDone = false;
                  chunkOres.clear();
                  pendingChunkOres.clear();
                  inFlightTiles.clear();
                  initialChunkPending = false;
                  synchronized (disprovenPositions) {
                     disprovenPositions.clear();
                  }
                  totalPositions = 0;
                  changeRevision.incrementAndGet();
               }

               errored = false;
               unsupportedDimension = true;
               worldgenUnverified = false;
               statusMessage = "Unsupported dimension " + dimension.identifier();
            }
         } else {
            OreSimEngine.WorldKey wanted = new OreSimEngine.WorldKey(worldSeed, dimension);
            synchronized (LOCK) {
               if (!wanted.equals(activeWorldKey)) {
                  switchWorld(wanted);
               }

               trackedCenterX = playerPos.getX();
               trackedCenterZ = playerPos.getZ();
               requestedRadius = Math.max(1, Math.min(radius, 8));
               flush();
               promoteReadyPending();
               if (worldgenContext != null && !generating && !verificationRunning && (!auditHasWriteDeadline || System.nanoTime() - auditWriteDeadline >= 0L)) {
                  verifyWorldgenAsync(worldgenContext, generation);
               } else if (!worldgenUnverified && !verificationRunning && enabledMask != 0 && !errored && !oreCapReached && worldgenContext != null && !initialChunkPending) {
                  if (!centerChunkDone) {
                     int centerChunkX = SectionPos.blockToSectionCoord(trackedCenterX);
                     int centerChunkZ = SectionPos.blockToSectionCoord(trackedCenterZ);
                     long centerKey = ChunkPos.pack(centerChunkX, centerChunkZ);
                     if (!chunkOres.containsKey(centerKey) && !pendingChunkOres.containsKey(centerKey)) {
                        generateChunk(centerChunkX, centerChunkZ, worldgenContext, generation);
                        return;
                     }

                     centerChunkDone = true;
                  }

                  int maxParallel = WORKERS.maxWorkers();

                  while (inFlightTiles.size() < maxParallel) {
                     OreSimEngine.ChunkTile tile = nearestTile(trackedCenterX, trackedCenterZ, requestedRadius);
                     if (tile == null) {
                        break;
                     }

                     generateTile(tile, worldgenContext, generation);
                  }
               }
            }
         }
      } else {
         reset();
      }
   }

   private static boolean supportsDimension(ResourceKey<Level> dimension) {
      return Level.OVERWORLD.equals(dimension) || Level.NETHER.equals(dimension) || Level.END.equals(dimension);
   }

   private static void switchWorld(OreSimEngine.WorldKey wanted) {
      generation++;
      cancelWorkers();
      activeWorldKey = wanted;
      worldgenContext = contextCache.get(wanted);
      contextLoading = worldgenContext == null;
      generating = false;
      verificationRunning = false;
      centerChunkDone = false;
      errored = false;
      unsupportedDimension = false;
      worldgenUnverified = false;
      statusMessage = "";
      oreCapReached = false;
      auditWriteDeadline = 0L;
      auditHasWriteDeadline = false;
      totalPositions = 0;
      synchronized (disprovenPositions) {
         disprovenPositions.clear();
      }
      if (!chunkOres.isEmpty()) {
         chunkOres.clear();
      }

      pendingChunkOres.clear();
      inFlightTiles.clear();
      initialChunkPending = false;
      changeRevision.incrementAndGet();
      if (worldgenContext == null) {
         buildContext(wanted, generation);
      }
   }

   private static void buildContext(OreSimEngine.WorldKey key, long token) {
      WORKERS.init(() -> {
         autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap built;
         boolean tagsVerified;
         try {
            built = autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap.create(key.seed(), key.dimension());
            tagsVerified = built.init_Z();
         } catch (Throwable error) {
            fail(token, "Minecraft 26.3 worldgen bootstrap failed", error);
            return;
         }

         synchronized (LOCK) {
            if (token == generation && key.equals(activeWorldKey)) {
               contextCache.put(key, built);
               worldgenContext = built;
               contextLoading = false;
               auditWriteDeadline = System.nanoTime() + AUDIT_WRITE_DELAY_NANOS;
               auditHasWriteDeadline = true;
               if (!tagsVerified) {
                  abort("Minecraft block tags differ from the vanilla 26.3 worldgen profile");
               }
            }
         }
      });
   }

   private static void generateChunk(int chunkX, int chunkZ, autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap context, long token) {
      initialChunkPending = true;
      generating = true;
      WORKERS.init(() -> {
         try {
            try {
               if (!context.init_Z()) {
                  failIfCurrent(token, "Minecraft block tags changed before initial chunk generation");
                  return;
               }

               int regionMinX = chunkX - 2;
               int regionMinZ = chunkZ - 2;
               Map<Long, ChunkAccess> generated = new autismclient.util.oresim.worldgen.OreSimRegionGenerator(context).generateRegion(regionMinX, regionMinZ, 5, () -> token != generation);
               long chunkKey = ChunkPos.pack(chunkX, chunkZ);
               ChunkAccess chunk = generated.get(chunkKey);
               if (chunk == null) {
                  throw new IllegalStateException("Generator omitted initial complete chunk " + chunkX + ", " + chunkZ);
               }

               OreSimEngine.ChunkOres ores = extractOres(chunk, () -> token != generation);
               if (!context.init_Z()) {
                  failIfCurrent(token, "Minecraft block tags changed during initial chunk generation");
                  return;
               }

               synchronized (LOCK) {
                  if (token != generation || context != worldgenContext) {
                     return;
                  }

                  if (!chunkOres.containsKey(chunkKey) && !pendingChunkOres.containsKey(chunkKey)) {
                     if (totalPositions + ores.size() > 600000) {
                        oreCapReached = true;
                        return;
                     }

                     chunkOres.put(chunkKey, ores);
                     totalPositions = totalPositions + ores.size();
                     changeRevision.incrementAndGet();
                  }

                  auditWriteDeadline = System.nanoTime() + AUDIT_WRITE_DELAY_NANOS;
                  auditHasWriteDeadline = true;
                  centerChunkDone = true;
               }
            } catch (CancellationException var33) {
            } catch (Throwable error) {
               fail(token, "Minecraft 26.3 initial chunk generation failed at " + chunkX + ", " + chunkZ, error);
            }
         } finally {
            synchronized (LOCK) {
               if (token == generation) {
                  initialChunkPending = false;
                  generating = !inFlightTiles.isEmpty();
               }
            }
         }
      });
   }

   private static void generateTile(OreSimEngine.ChunkTile tile, autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap context, long token) {
      long tileKey = ChunkPos.pack(tile.minChunkX(), tile.load());
      inFlightTiles.add(tileKey);
      generating = true;
      LongOpenHashSet alreadyComplete = new LongOpenHashSet(16);

      for (int dx = 0; dx < 4; dx++) {
         for (int dz = 0; dz < 4; dz++) {
            long key = ChunkPos.pack(tile.minChunkX() + dx, tile.load() + dz);
            if (chunkOres.containsKey(key) || pendingChunkOres.containsKey(key)) {
               alreadyComplete.add(key);
            }
         }
      }

      WORKERS.submitParallel(() -> {
         try {
            if (!context.init_Z()) {
               failIfCurrent(token, "Minecraft block tags changed before region generation");
            } else {
               int regionMinX = tile.minChunkX() - 2;
               int regionMinZ = tile.load() - 2;
               Map<Long, ChunkAccess> generated = new autismclient.util.oresim.worldgen.OreSimRegionGenerator(context).generateRegion(regionMinX, regionMinZ, 8, () -> token != generation);
               Map<Long, OreSimEngine.ChunkOres> completed = new HashMap<>(16);

               for (int dxx = 0; dxx < 4; dxx++) {
                  for (int dzx = 0; dzx < 4; dzx++) {
                     int chunkX = tile.minChunkX() + dxx;
                     int chunkZ = tile.load() + dzx;
                     long chunkKey = ChunkPos.pack(chunkX, chunkZ);
                     if (!alreadyComplete.contains(chunkKey)) {
                        ChunkAccess chunk = generated.get(chunkKey);
                        if (chunk == null) {
                           throw new IllegalStateException("Generator omitted complete chunk " + chunkX + ", " + chunkZ);
                        }

                        OreSimEngine.ChunkOres ores = extractOres(chunk, () -> token != generation);
                        completed.put(chunkKey, ores);
                     }
                  }
               }

               if (!context.init_Z()) {
                  failIfCurrent(token, "Minecraft block tags changed during region generation");
               } else {
                  synchronized (LOCK) {
                     if (token == generation && context == worldgenContext) {
                        int added = 0;

                        for (Entry<Long, OreSimEngine.ChunkOres> entry : completed.entrySet()) {
                           if (!chunkOres.containsKey(entry.getKey()) && !pendingChunkOres.containsKey(entry.getKey())) {
                              added += entry.getValue().size();
                           }
                        }

                        if (totalPositions + added > 600000) {
                           oreCapReached = true;
                        } else {
                           for (Entry<Long, OreSimEngine.ChunkOres> entry : completed.entrySet()) {
                              if (!chunkOres.containsKey(entry.getKey()) && !pendingChunkOres.containsKey(entry.getKey())) {
                                 pendingChunkOres.put(entry.getKey(), entry.getValue());
                                 totalPositions = totalPositions + entry.getValue().size();
                              }
                           }

                           auditWriteDeadline = System.nanoTime() + AUDIT_WRITE_DELAY_NANOS;
                           auditHasWriteDeadline = true;
                        }
                     }
                  }
               }
            }
         } catch (CancellationException var36) {
         } catch (Throwable error) {
            fail(token, "Minecraft 26.3 region generation failed at tile " + tile.minChunkX() + ", " + tile.load(), error);
         } finally {
            synchronized (LOCK) {
               if (token == generation) {
                  inFlightTiles.remove(tileKey);
                  generating = initialChunkPending || !inFlightTiles.isEmpty();
               }
            }
         }
      });
   }

   private static OreSimEngine.ChunkOres extractOres(ChunkAccess chunk, BooleanSupplier cancelled) {
      LongArrayList positions = new LongArrayList();
      IntArrayList stateIds = new IntArrayList();
      autismclient.util.oresim.worldgen.OreSimRegionGenerator.scanChunk(chunk, state -> OreBlockCatalog.isOre(state.getBlock()), (x, y, z, state) -> {
         int stateId = OreBlockCatalog.StateRegistry.init(state);
         if (stateId >= 0) {
            positions.add(BlockPos.asLong(x, y, z));
            stateIds.add(stateId);
         }
      }, cancelled);
      return positions.isEmpty()
         ? OreSimEngine.ChunkOres.EMPTY
         : new OreSimEngine.ChunkOres(positions.toLongArray(), stateIds.toIntArray());
   }

   private static void fail(long token, String message, Throwable error) {
      boolean accepted = false;
      synchronized (LOCK) {
         if (token != generation) {
            return;
         }

         errored = true;
         contextLoading = false;
         generating = false;
         verificationRunning = false;
         centerChunkDone = false;
         statusMessage = message + ": " + error.getClass().getSimpleName() + (error.getMessage() == null ? "" : " - " + error.getMessage());
         worldgenContext = null;
         chunkOres.clear();
         pendingChunkOres.clear();
         inFlightTiles.clear();
         initialChunkPending = false;
         totalPositions = 0;
         worldgenUnverified = false;
         changeRevision.incrementAndGet();
         accepted = true;
      }

      if (accepted) {
         AutismClientAddon.LOG.warn(message, error);
      }
   }

   private static void failIfCurrent(long token, String message) {
      synchronized (LOCK) {
         if (token == generation) {
            abort(message);
         }
      }
   }

   private static void abort(String message) {
      generation++;
      cancelWorkers();
      contextLoading = false;
      generating = false;
      verificationRunning = false;
      centerChunkDone = false;
      errored = false;
      unsupportedDimension = false;
      worldgenUnverified = true;
      statusMessage = message;
      oreCapReached = false;
      auditWriteDeadline = System.nanoTime() + AUDIT_WRITE_DELAY_NANOS;
      auditHasWriteDeadline = true;
      totalPositions = 0;
      chunkOres.clear();
      pendingChunkOres.clear();
      inFlightTiles.clear();
      initialChunkPending = false;
      synchronized (disprovenPositions) {
         disprovenPositions.clear();
      }
      changeRevision.incrementAndGet();
   }

   private static void cancelWorkers() {
      WORKERS.init_V();
   }

   private static void verifyWorldgenAsync(autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap context, long token) {
      verificationRunning = true;
      WORKERS.init(() -> {
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

         synchronized (LOCK) {
            if (token == generation && context == worldgenContext) {
               verificationRunning = false;
               auditWriteDeadline = System.nanoTime() + AUDIT_WRITE_DELAY_NANOS;
               auditHasWriteDeadline = true;
               if (!verified) {
                  abort(failure == null ? "Minecraft block tags differ from the vanilla 26.3 worldgen profile" : "Minecraft block tags could not be verified");
               } else if (worldgenUnverified) {
                  generation++;
                  worldgenUnverified = false;
                  statusMessage = "";
                  centerChunkDone = false;
                  changeRevision.incrementAndGet();
               }
            }
         }
      });
   }

   private static OreSimEngine.ChunkTile nearestTile(int blockX, int blockZ, int radius) {
      int centerChunkX = SectionPos.blockToSectionCoord(blockX);
      int centerChunkZ = SectionPos.blockToSectionCoord(blockZ);
      int minTileX = Math.floorDiv(centerChunkX - radius, 4);
      int maxTileX = Math.floorDiv(centerChunkX + radius, 4);
      int minTileZ = Math.floorDiv(centerChunkZ - radius, 4);
      int maxTileZ = Math.floorDiv(centerChunkZ + radius, 4);
      OreSimEngine.ChunkTile best = null;

      for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
         for (int tileZ = minTileZ; tileZ <= maxTileZ; tileZ++) {
            int minChunkX = tileX * 4;
            int minChunkZ = tileZ * 4;
            if (!inFlightTiles.contains(ChunkPos.pack(minChunkX, minChunkZ)) && !tileComplete(minChunkX, minChunkZ)) {
               double distance = distanceToTile(blockX + 0.5, blockZ + 0.5, minChunkX, minChunkZ);
               if (best == null || distance < best.distance() || distance == best.distance() && tileOrderedBefore(minChunkX, minChunkZ, best)) {
                  best = new OreSimEngine.ChunkTile(minChunkX, minChunkZ, distance);
               }
            }
         }
      }

      return best;
   }

   private static boolean tileComplete(int minChunkX, int minChunkZ) {
      for (int dx = 0; dx < 4; dx++) {
         for (int dz = 0; dz < 4; dz++) {
            long key = ChunkPos.pack(minChunkX + dx, minChunkZ + dz);
            if (!chunkOres.containsKey(key) && !pendingChunkOres.containsKey(key)) {
               return false;
            }
         }
      }

      return true;
   }

   private static double distanceToTile(double x, double z, int minChunkX, int minChunkZ) {
      double minX = minChunkX * 16.0 + 0.5;
      double minZ = minChunkZ * 16.0 + 0.5;
      double maxX = (minChunkX + 4) * 16.0 - 0.5;
      double maxZ = (minChunkZ + 4) * 16.0 - 0.5;
      double dx = x < minX ? minX - x : (x > maxX ? x - maxX : 0.0);
      double dz = z < minZ ? minZ - z : (z > maxZ ? z - maxZ : 0.0);
      return dx * dx + dz * dz;
   }

   private static boolean tileOrderedBefore(int x, int z, OreSimEngine.ChunkTile other) {
      return x < other.minChunkX() || x == other.minChunkX() && z < other.load();
   }

   private static void promoteReadyPending() {
      if (!pendingChunkOres.isEmpty()) {
         NearestChunkSelector.Inner_DataUtil missing = nearestMissingChunk();
         boolean changed = false;
         Iterator<Entry<Long, OreSimEngine.ChunkOres>> iterator = pendingChunkOres.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<Long, OreSimEngine.ChunkOres> entry = iterator.next();
            int chunkX = ChunkPos.getX(entry.getKey());
            int chunkZ = ChunkPos.getZ(entry.getKey());
            if (NearestChunkSelector.init(trackedCenterX + 0.5, trackedCenterZ + 0.5, chunkX, chunkZ, missing)) {
               chunkOres.put(entry.getKey(), entry.getValue());
               iterator.remove();
               changed = true;
            }
         }

         if (changed) {
            changeRevision.incrementAndGet();
         }
      }
   }

   private static NearestChunkSelector.Inner_DataUtil nearestMissingChunk() {
      int centerX = SectionPos.blockToSectionCoord(trackedCenterX);
      int centerZ = SectionPos.blockToSectionCoord(trackedCenterZ);
      return NearestChunkSelector.select(
         trackedCenterX + 0.5, trackedCenterZ + 0.5, centerX, centerZ, requestedRadius, key -> chunkOres.containsKey(key) || pendingChunkOres.containsKey(key)
      );
   }

   private static void flush() {
      int centerX = SectionPos.blockToSectionCoord(trackedCenterX);
      int centerZ = SectionPos.blockToSectionCoord(trackedCenterZ);
      int keep = requestedRadius + 4;
      int removed = 0;

      for (Entry<Long, OreSimEngine.ChunkOres> entry : chunkOres.entrySet()) {
         long key = entry.getKey();
         if ((Math.abs(ChunkPos.getX(key) - centerX) > keep || Math.abs(ChunkPos.getZ(key) - centerZ) > keep) && chunkOres.remove(key, entry.getValue())) {
            removed += entry.getValue().size();
         }
      }

      Iterator<Entry<Long, OreSimEngine.ChunkOres>> pending = pendingChunkOres.entrySet().iterator();

      while (pending.hasNext()) {
         Entry<Long, OreSimEngine.ChunkOres> entry = pending.next();
         long key = entry.getKey();
         if (Math.abs(ChunkPos.getX(key) - centerX) > keep || Math.abs(ChunkPos.getZ(key) - centerZ) > keep) {
            removed += entry.getValue().size();
            pending.remove();
         }
      }

      if (removed > 0) {
         totalPositions = Math.max(0, totalPositions - removed);
         oreCapReached = false;
         changeRevision.incrementAndGet();
      }
   }

   public static boolean wasLastQueryCapped() {
      return lastQueryCapped;
   }

   public static int nearest(double eyeX, double eyeY, double eyeZ, int radius, IntPredicate stateAllowed, int cap, long[] outPos, int[] outState) {
      lastQueryCapped = false;
      if (cap > 0 && outPos != null && outState != null && !chunkOres.isEmpty()) {
         int limit = Math.min(cap, Math.min(outPos.length, outState.length));
         if (limit <= 0) {
            return 0;
         }

         NearestOreHeap nearest = new NearestOreHeap(limit);
         int centerX = SectionPos.blockToSectionCoord((int)Math.floor(eyeX));
         int centerZ = SectionPos.blockToSectionCoord((int)Math.floor(eyeZ));
         int drawRadius = Math.max(1, Math.min(radius, 8));
         synchronized (disprovenPositions) {
            boolean anyDisproven = !disprovenPositions.isEmpty();

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
                        OreSimEngine.ChunkOres ores = chunkOres.get(ChunkPos.pack(centerX + dx, centerZ + dz));
                        if (ores != null && ores.positions.length != 0) {
                           for (int i = 0; i < ores.positions.length; i++) {
                              int stateId = ores.stateIds[i];
                              if (stateAllowed == null || stateAllowed.test(stateId)) {
                                 long packed = ores.positions[i];
                                 if (!anyDisproven || !disprovenPositions.contains(packed)) {
                                    double px = BlockPos.getX(packed) + 0.5 - eyeX;
                                    double py = BlockPos.getY(packed) + 0.5 - eyeY;
                                    double pz = BlockPos.getZ(packed) + 0.5 - eyeZ;
                                    double distance = px * px + py * py + pz * pz;
                                    nearest.offer(distance, packed, stateId);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         lastQueryCapped = nearest.overflowed();
         return nearest.drain(outPos, outState);
      } else {
         return 0;
      }
   }

   public static void collectPositions(BlockPos center, int blockRadius, LongOpenHashSet out) {
      if (center != null && out != null && !chunkOres.isEmpty()) {
         int minX = center.getX() - blockRadius;
         int maxX = center.getX() + blockRadius;
         int minY = center.getY() - blockRadius;
         int maxY = center.getY() + blockRadius;
         int minZ = center.getZ() - blockRadius;
         int maxZ = center.getZ() + blockRadius;

         synchronized (disprovenPositions) {
            for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
               for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                  OreSimEngine.ChunkOres ores = chunkOres.get(ChunkPos.pack(chunkX, chunkZ));
                  if (ores != null) {
                     for (long packed : ores.positions) {
                        if (!disprovenPositions.contains(packed)) {
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

   public static void forEachPosition(BlockPos center, int blockRadius, OreSimEngine.DropJudge judge) {
      if (center != null && judge != null && !chunkOres.isEmpty()) {
         int minX = center.getX() - blockRadius;
         int maxX = center.getX() + blockRadius;
         int minY = center.getY() - blockRadius;
         int maxY = center.getY() + blockRadius;
         int minZ = center.getZ() - blockRadius;
         int maxZ = center.getZ() + blockRadius;
         boolean changed = false;

         synchronized (disprovenPositions) {
            for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
               for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                  OreSimEngine.ChunkOres ores = chunkOres.get(ChunkPos.pack(chunkX, chunkZ));
                  if (ores != null) {
                     for (int i = 0; i < ores.positions.length; i++) {
                        long packed = ores.positions[i];
                        if (!disprovenPositions.contains(packed)) {
                           int x = BlockPos.getX(packed);
                           int y = BlockPos.getY(packed);
                           int z = BlockPos.getZ(packed);
                           if (x >= minX
                              && x <= maxX
                              && y >= minY
                              && y <= maxY
                              && z >= minZ
                              && z <= maxZ
                              && judge.shouldDrop(ores.stateIds[i], packed)
                              && disprovenPositions.size() < 200000) {
                              changed |= disprovenPositions.add(packed);
                           }
                        }
                     }
                  }
               }
            }
         }

         if (changed) {
            changeRevision.incrementAndGet();
         }
      }
   }

   public static int write_I() {
      return -1;
   }

   public enum EngineStatus {
      IDLE,
      LOADING_CONTEXT,
      GENERATING,
      READY,
      UNVERIFIED_WORLDGEN,
      UNSUPPORTED_DIMENSION,
      ERROR;
   }

   @FunctionalInterface
   public interface DropJudge {
      boolean shouldDrop(int var1, long var2);
   }

   public record GenerationProgress(long generation, int done, int load) {
      public GenerationProgress {
         load = Math.max(0, load);
         done = Math.max(0, Math.min(done, load));
      }

      public double fraction() {
         return this.load == 0 ? 0.0 : (double)this.done / this.load;
      }

      public boolean isComplete() {
         return this.load > 0 && this.done >= this.load;
      }
   }

   private static final class ChunkOres {
      static final OreSimEngine.ChunkOres EMPTY = new OreSimEngine.ChunkOres(new long[0], new int[0]);
      final long[] positions;
      final int[] stateIds;

      ChunkOres(long[] positions, int[] stateIds) {
         this.positions = positions;
         this.stateIds = stateIds;
      }

      int size() {
         return this.positions.length;
      }
   }

   private record WorldKey(long seed, ResourceKey<Level> dimension) {
   }

   private record ChunkTile(int minChunkX, int load, double distance) {
      long packedPos() {
         return ChunkPos.pack(this.minChunkX, this.load);
      }
   }
}
