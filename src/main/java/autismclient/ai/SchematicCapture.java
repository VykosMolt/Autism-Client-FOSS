package autismclient.ai;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.IntConsumer;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.storage.TagValueOutput;

final class SchematicCapture {
   private final ClientLevel level;
   private final IntegratedServer server;
   private final List<SchematicCapture.Inner_CachePool> chunks;
   private final Map<Long, SchematicCapture.Inner_CachePool> chunksByPos = new HashMap<>();
   private final Path folder;
   private final String name;
   private final boolean includeEntities;
   private int chunkIndex;
   private int sectionY;
   private List<BlockPos> blockEntityPositions;
   private int blockEntityIndex;
   private List<Integer> entityIds;
   private int entityIndex;
   private boolean update_Z;
   private boolean sync_Z;
   private CompletableFuture<Boolean> serverTask;
   private int serverChunkIndex;
   private int blockEntityCursor;
   private List<BlockPos> write_List;
   volatile boolean cancelled;
   volatile boolean finished;
   volatile int progress;
   volatile String stage = "Copying chunks";
   volatile String error = "";
   volatile Path savedPath;
   private Future<?> saveTask;

   SchematicCapture(Minecraft mc, Path folder, String name, SchematicCapture.Inner_DataUtil selection, boolean includeEntities) {
      if (mc.level != null && mc.player != null) {
         this.level = mc.level;
         this.server = mc.getSingleplayerServer();
         this.folder = folder;
         this.name = normalizeName(name);
         this.includeEntities = includeEntities;
         ArrayList<LevelChunk> chunks = new ArrayList<>();
         if (selection != null) {
            if (selection.volume() < 1L || selection.volume() > 134217728L) {
               throw new IllegalArgumentException("Select at most 128 million blocks");
            }

            int minCx = selection.min.getX() >> 4;
            int maxCx = selection.max.getX() >> 4;
            int minCz = selection.min.getZ() >> 4;
            int maxCz = selection.max.getZ() >> 4;

            for (int cx = minCx; cx <= maxCx; cx++) {
               for (int cz = minCz; cz <= maxCz; cz++) {
                  LevelChunk live = this.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                  if (live == null) {
                     throw new IllegalArgumentException("A selected chunk is outside view distance");
                  }

                  chunks.add(live);
               }
            }
         } else {
            chunks.addAll(SchematicWorkspace.loadedChunks(this.level));
            chunks.sort(Comparator.<LevelChunk>comparingInt(c -> c.getPos().x()).thenComparingInt(c -> c.getPos().z()));
         }

         ArrayList<SchematicCapture.Inner_CachePool> captured = new ArrayList<>(chunks.size());

         for (LevelChunk live : chunks) {
            ChunkPos chunkPos = live.getPos();
            int x0 = chunkPos.x() << 4;
            int z0 = chunkPos.z() << 4;
            SchematicCapture.Inner_DataUtil fullArea = new SchematicCapture.Inner_DataUtil(
               new BlockPos(x0, this.level.getMinY(), z0), new BlockPos(x0 + 15, this.level.getMaxY(), z0 + 15)
            );
            SchematicCapture.Inner_DataUtil area = selection == null ? fullArea : fullArea.intersection(selection);
            if (area != null) {
               SchematicCapture.Inner_CachePool chunk = new SchematicCapture.Inner_CachePool(chunkPos, area);
               captured.add(chunk);
               this.chunksByPos.put(chunkPos.pack(), chunk);
            }
         }

         if (captured.isEmpty()) {
            throw new IllegalArgumentException("No loaded chunks in this area");
         }

         this.chunks = List.copyOf(captured);
         this.sectionY = this.chunks.get(0).area.min.getY() >> 4;
      } else {
         throw new IllegalStateException("Join a world first");
      }
   }

   void cancel() {
      this.cancelled = true;
      if (this.saveTask != null) {
         this.saveTask.cancel(true);
      }

      this.stage = "Cancelled";
   }

   void tick(Minecraft mc) {
      if (!this.finished && !this.cancelled && !this.update_Z) {
         if (mc.level != this.level) {
            this.cancel();
         } else {
            long deadline = System.nanoTime() + 1500000L;

            try {
               do {
                  if (this.chunkIndex < this.chunks.size()) {
                     SchematicCapture.Inner_CachePool target = this.chunks.get(this.chunkIndex);
                     LevelChunk live = this.level
                        .getChunkSource()
                        .getChunk(target.chunkPos.x(), target.chunkPos.z(), ChunkStatus.FULL, false);
                     if (live == null) {
                        throw new IllegalStateException("A chunk unloaded during capture; save again");
                     }

                     if (this.sectionY <= target.area.max.getY() >> 4) {
                        int index = live.getSectionIndex(this.sectionY << 4);
                        LevelChunkSection section = live.getSections()[index];
                        if (section != null && !section.hasOnlyAir()) {
                           target.init_Map.put(this.sectionY, section.getStates().copy());
                        }

                        this.sectionY++;
                        this.progress = Math.max(
                           this.progress,
                           (int)(
                              65.0
                                 * (
                                    this.chunkIndex
                                       + (double)(this.sectionY - (target.area.min.getY() >> 4))
                                          / Math.max(1, (target.area.max.getY() >> 4) - (target.area.min.getY() >> 4) + 1)
                                 )
                                 / this.chunks.size()
                           )
                        );
                     } else if (this.server != null) {
                        this.chunkIndex++;
                        if (this.chunkIndex < this.chunks.size()) {
                           this.sectionY = this.chunks.get(this.chunkIndex).area.min.getY() >> 4;
                        }
                     } else {
                        if (this.blockEntityPositions == null) {
                           this.blockEntityPositions = List.copyOf(live.getBlockEntities().keySet());
                           this.blockEntityIndex = 0;
                        }

                        if (this.blockEntityIndex < this.blockEntityPositions.size()) {
                           BlockPos p = this.blockEntityPositions.get(this.blockEntityIndex++);
                           if (target.area.contains(p.getX(), p.getY(), p.getZ())) {
                              BlockEntity be = this.level.getBlockEntity(p);
                              if (be != null) {
                                 CompoundTag tag = be.saveWithFullMetadata(this.level.registryAccess());
                                 tag.putInt("x", p.getX() - target.area.min.getX());
                                 tag.putInt("y", p.getY() - target.area.min.getY());
                                 tag.putInt("z", p.getZ() - target.area.min.getZ());
                                 target.blockEntityTags.add(tag);
                              }
                           }
                        } else {
                           this.chunkIndex++;
                           this.blockEntityPositions = null;
                           if (this.chunkIndex < this.chunks.size()) {
                              this.sectionY = this.chunks.get(this.chunkIndex).area.min.getY() >> 4;
                           }
                        }
                     }
                  } else if (this.server != null && !this.sync_Z) {
                     if (this.serverTask != null && !this.serverTask.isDone()) {
                        return;
                     }

                     if (this.serverTask == null || !this.serverTask.join()) {
                        this.stage = "Copying block data";
                        this.serverTask = this.server.submit(this::captureOnServer);
                        return;
                     }

                     this.serverTask = null;
                     this.sync_Z = true;
                  } else {
                     if (!this.includeEntities) {
                        this.startSave();
                        return;
                     }

                     if (this.entityIds == null) {
                        this.entityIds = new ArrayList<>();

                        for (Entity entity : this.level.entitiesForRendering()) {
                           if (!(entity instanceof Player) && !entity.isPassenger()) {
                              this.entityIds.add(entity.getId());
                              if (this.entityIds.size() > 16384) {
                                 throw new IllegalArgumentException("Too many loaded entities");
                              }
                           }
                        }
                     }

                     if (this.entityIndex >= this.entityIds.size()) {
                        this.startSave();
                        return;
                     }

                     Entity entity = this.level.getEntity(this.entityIds.get(this.entityIndex++));
                     if (entity != null) {
                        SchematicCapture.Inner_CachePool target = this.chunksByPos.get(ChunkPos.pack(entity.getBlockX() >> 4, entity.getBlockZ() >> 4));
                        if (target != null && target.area.contains(entity.getX(), entity.getY(), entity.getZ())) {
                           TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, this.level.registryAccess());
                           if (entity.save(output)) {
                              CompoundTag tag = output.buildResult();
                              relativizeEntityPos(tag, target.area.min);
                              target.entityTags.add(tag);
                           }
                        }
                     }
                  }
               } while (System.nanoTime() < deadline);
            } catch (Exception e) {
               this.error = SchematicLibrary.describeError(e);
               this.stage = "Save failed";
               this.finished = true;
            }
         }
      }
   }

   private boolean captureOnServer() {
      ServerLevel level = this.server.getLevel(this.level.dimension());
      if (level == null) {
         throw new IllegalStateException("World changed during capture");
      }

      long deadline = System.nanoTime() + 1200000L;

      while (this.serverChunkIndex < this.chunks.size() && !this.cancelled && System.nanoTime() < deadline) {
         SchematicCapture.Inner_CachePool target = this.chunks.get(this.serverChunkIndex);
         ChunkAccess live = level.getChunkSource().getChunk(target.chunkPos.x(), target.chunkPos.z(), ChunkStatus.FULL, false);
         if (live == null) {
            throw new IllegalStateException("A chunk unloaded during capture; save again");
         }

         if (this.write_List == null) {
            this.write_List = List.copyOf(live.getBlockEntitiesPos());
            this.blockEntityCursor = 0;
         }

         if (this.blockEntityCursor >= this.write_List.size()) {
            this.write_List = null;
            this.serverChunkIndex++;
         } else {
            BlockPos at = this.write_List.get(this.blockEntityCursor++);
            if (target.area.contains(at.getX(), at.getY(), at.getZ())) {
               CompoundTag tag = live.getBlockEntityNbtForSaving(at, level.registryAccess());
               if (tag != null) {
                  tag = tag.copy();
                  tag.remove("keepPacked");
                  tag.putInt("x", at.getX() - target.area.min.getX());
                  tag.putInt("y", at.getY() - target.area.min.getY());
                  tag.putInt("z", at.getZ() - target.area.min.getZ());
                  target.blockEntityTags.add(tag);
               }
            }
         }
      }

      return this.serverChunkIndex == this.chunks.size();
   }

   private void startSave() {
      this.update_Z = true;
      this.progress = 70;
      this.stage = "Packing schematic";
      this.saveTask = SchematicLibrary.EXECUTOR.submit(() -> {
         try {
            this.savedPath = save(this.folder, this.name, this.chunks, true, p -> {
               this.progress = 70 + p / 5;
               this.checkCancelled();
            });
            this.progress = 100;
            this.stage = "Saved";
            this.finished = true;
         } catch (CancellationException ignored) {
            this.cancelled = true;
         } catch (Exception e) {
            this.error = SchematicLibrary.describeError(e);
            this.stage = "Save failed";
            this.finished = true;
         } finally {
            Thread.interrupted();
         }
      });
   }

   static Path save(Path folder, String name, List<SchematicCapture.Inner_CachePool> chunks, boolean trimAir, IntConsumer progress) throws IOException {
      Files.createDirectories(folder);
      CompoundTag root = packSchematic(chunks, trimAir, progress);
      SchematicDocument.checkInterrupted();
      Path temporary = Files.createTempFile(folder, ".capture-", ".tmp");

      try {
         NbtIo.writeCompressed(root, temporary);
         SchematicDocument.checkInterrupted();
         Path output = folder.resolve(name + ".litematic");

         for (int i = 2; Files.exists(output); i++) {
            output = folder.resolve(name + " (" + i + ").litematic");
         }

         Files.move(temporary, output);
         temporary = null;
         return output;
      } finally {
         if (temporary != null) {
            try {
               Files.deleteIfExists(temporary);
            } catch (Exception var14) {
            }
         }
      }
   }

   private void checkCancelled() {
      SchematicDocument.checkInterrupted();
      if (this.cancelled) {
         throw new CancellationException();
      }
   }

   static CompoundTag packSchematic(List<SchematicCapture.Inner_CachePool> chunks, boolean trimAir, IntConsumer progress) {
      SchematicCapture.Inner_StreamUtil bounds = new SchematicCapture.Inner_StreamUtil();
      IdentityHashMap<SchematicCapture.Inner_CachePool, SchematicCapture.Inner_StreamUtil> perChunkBounds = trimAir ? new IdentityHashMap<>() : null;
      if (trimAir) {
         int checked = 0;

         for (SchematicCapture.Inner_CachePool chunk : chunks) {
            SchematicDocument.checkInterrupted();
            SchematicCapture.Inner_StreamUtil chunkBounds = new SchematicCapture.Inner_StreamUtil();
            chunkBounds.includeNonAirBlocks(chunk);
            chunkBounds.includeBlockEntities(chunk);
            if (!chunkBounds.init_Z()) {
               perChunkBounds.put(chunk, chunkBounds);
               bounds.includeBounds(chunkBounds);
            }

            checked++;
            progress.accept(checked * 20 / chunks.size());
         }
      } else {
         for (SchematicCapture.Inner_CachePool chunk : chunks) {
            BlockPos lo = chunk.area.min;
            BlockPos hi = chunk.area.max;
            bounds.include(lo.getX(), lo.getY(), lo.getZ());
            bounds.include(hi.getX(), hi.getY(), hi.getZ());
         }
      }

      SchematicCapture.Inner_DataUtil enclosing = bounds.toArea();
      LinkedHashMap<SchematicCapture.Inner_CachePool, SchematicCapture.Inner_DataUtil> areas = new LinkedHashMap<>();
      long capturedVolume = 0L;

      for (SchematicCapture.Inner_CachePool chunk : chunks) {
         SchematicCapture.Inner_DataUtil area;
         if (trimAir) {
            SchematicCapture.Inner_StreamUtil chunkBounds = perChunkBounds.get(chunk);
            if (chunkBounds == null) {
               continue;
            }

            area = chunk.area.intersection(chunkBounds.toArea());
         } else {
            area = chunk.area.intersection(enclosing);
         }

         if (area != null) {
            areas.put(chunk, area);
            capturedVolume += area.volume();
            if (capturedVolume > 134217728L) {
               throw new IllegalArgumentException("Too many captured blocks; lower view distance or select an area");
            }
         }
      }

      CompoundTag root = new CompoundTag();
      CompoundTag regions = new CompoundTag();
      root.putInt("Version", 7);
      root.putInt("SubVersion", 1);
      root.putInt("MinecraftDataVersion", SharedConstants.getCurrentVersion().dataVersion().version());
      long nonAir = 0L;
      int done = 0;

      for (Entry<SchematicCapture.Inner_CachePool, SchematicCapture.Inner_DataUtil> entry : areas.entrySet()) {
         SchematicDocument.checkInterrupted();
         SchematicCapture.Inner_CachePool chunk = entry.getKey();
         SchematicCapture.Inner_DataUtil a = entry.getValue();
         int w = a.max.getX() - a.min.getX() + 1;
         int h = a.max.getY() - a.min.getY() + 1;
         int l = a.max.getZ() - a.min.getZ() + 1;
         int[] values = new int[w * h * l];
         ArrayList<BlockState> palette = new ArrayList<>();
         Reference2IntOpenHashMap<BlockState> ids = new Reference2IntOpenHashMap<>();
         ids.defaultReturnValue(-1);
         palette.add(Blocks.AIR.defaultBlockState());
         ids.put(palette.get(0), 0);

         for (int y = 0; y < h; y++) {
            SchematicDocument.checkInterrupted();
            int worldY = a.min.getY() + y;
            PalettedContainer<BlockState> section = chunk.init_Map.get(worldY >> 4);
            if (section != null) {
               for (int z = 0; z < l; z++) {
                  for (int x = 0; x < w; x++) {
                     BlockState state = section.get(a.min.getX() + x & 15, worldY & 15, a.min.getZ() + z & 15);
                     int id = ids.getInt(state);
                     if (id < 0) {
                        palette.add(state);
                        id = palette.size() - 1;
                        ids.put(state, id);
                     }

                     values[(y * l + z) * w + x] = id;
                     if (!state.isAir()) {
                        nonAir++;
                     }
                  }
               }
            }
         }

         CompoundTag region = new CompoundTag();
         region.put("Position", posTag(a.min.getX() - enclosing.min.getX(), a.min.getY() - enclosing.min.getY(), a.min.getZ() - enclosing.min.getZ()));
         region.put("Size", posTag(w, h, l));
         ListTag entries = new ListTag();

         for (BlockState state : palette) {
            entries.add(NbtUtils.writeBlockState(state));
         }

         region.put("BlockStatePalette", entries);
         region.putLongArray("BlockStates", packIndices(values, palette.size()));
         BlockPos shift = a.min.subtract(chunk.area.min);
         ListTag bes = new ListTag();

         for (CompoundTag saved : chunk.blockEntityTags) {
            CompoundTag tag = saved.copy();
            tag.putInt("x", tag.getIntOr("x", 0) - shift.getX());
            tag.putInt("y", tag.getIntOr("y", 0) - shift.getY());
            tag.putInt("z", tag.getIntOr("z", 0) - shift.getZ());
            bes.add(tag);
         }

         region.put("TileEntities", bes);
         ListTag entities = new ListTag();

         for (CompoundTag saved : chunk.entityTags) {
            CompoundTag tag = saved.copy();
            relativizeEntityPos(tag, shift);
            entities.add(tag);
         }

         region.put("Entities", entities);
         regions.put("chunk_" + chunk.chunkPos.x() + "_" + chunk.chunkPos.z(), region);
         done++;
         progress.accept(20 + done * 80 / areas.size());
      }

      root.put("Regions", regions);
      CompoundTag meta = new CompoundTag();
      meta.putString("Name", "Captured area");
      meta.putString("Author", "AUTISM Client");
      meta.putInt("RegionCount", areas.size());
      meta.putInt("TotalBlocks", Math.toIntExact(nonAir));
      meta.putInt("TotalVolume", Math.toIntExact(capturedVolume));
      meta.put(
         "EnclosingSize",
         posTag(enclosing.max.getX() - enclosing.min.getX() + 1, enclosing.max.getY() - enclosing.min.getY() + 1, enclosing.max.getZ() - enclosing.min.getZ() + 1)
      );
      meta.putLong("TimeCreated", System.currentTimeMillis());
      meta.putLong("TimeModified", System.currentTimeMillis());
      root.put("Metadata", meta);
      return root;
   }

   static long[] packIndices(int[] ids, int paletteSize) {
      if (paletteSize > 65536) {
         throw new IllegalArgumentException("A captured region has " + paletteSize + " block states; schematics are limited to 65536");
      }

      int bits = SchematicDocument.bitsForPaletteSize(paletteSize);
      long[] data = new long[(int)(((long)ids.length * bits + 63L) / 64L)];

      for (int i = 0; i < ids.length; i++) {
         SchematicDocument.pack(data, i, bits, ids[i]);
      }

      return data;
   }

   static CompoundTag posTag(int x, int y, int z) {
      CompoundTag t = new CompoundTag();
      t.putInt("x", x);
      t.putInt("y", y);
      t.putInt("z", z);
      return t;
   }

   static void relativizeEntityPos(CompoundTag tag, BlockPos base) {
      ListTag pos = tag.getListOrEmpty("Pos");
      if (pos.size() == 3) {
         ListTag result = new ListTag();
         result.add(DoubleTag.valueOf(pos.getDoubleOr(0, 0.0) - base.getX()));
         result.add(DoubleTag.valueOf(pos.getDoubleOr(1, 0.0) - base.getY()));
         result.add(DoubleTag.valueOf(pos.getDoubleOr(2, 0.0) - base.getZ()));
         tag.put("Pos", result);
      }

      for (Tag passenger : tag.getListOrEmpty("Passengers")) {
         if (passenger instanceof CompoundTag p) {
            relativizeEntityPos(p, base);
         }
      }
   }

   static String normalizeName(String name) {
      String n = name == null ? "" : name.trim().replaceFirst("(?i)\\.litematic$", "");
      if (n.isEmpty()) {
         n = "Capture " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"));
      }

      if (n.length() <= 100 && !n.contains("..") && !n.matches(".*[\\\\/:*?\"<>|\\p{Cntrl}].*")) {
         return n;
      } else {
         throw new IllegalArgumentException("Use a short file name without path separators");
      }
   }

   static final class Inner_CachePool {
      final ChunkPos chunkPos;
      final SchematicCapture.Inner_DataUtil area;
      final Int2ObjectOpenHashMap<PalettedContainer<BlockState>> init_Map = new Int2ObjectOpenHashMap<>();
      final List<CompoundTag> blockEntityTags = new ArrayList<>();
      final List<CompoundTag> entityTags = new ArrayList<>();

      Inner_CachePool(ChunkPos pos, SchematicCapture.Inner_DataUtil area) {
         this.chunkPos = pos;
         this.area = area;
      }
   }

   record Inner_DataUtil(BlockPos min, BlockPos max) {
      static SchematicCapture.Inner_DataUtil between(BlockPos a, BlockPos b) {
         return new SchematicCapture.Inner_DataUtil(
            new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ())),
            new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()))
         );
      }

      boolean contains(double x, double y, double z) {
         return x >= this.min.getX()
            && x < this.max.getX() + 1.0
            && y >= this.min.getY()
            && y < this.max.getY() + 1.0
            && z >= this.min.getZ()
            && z < this.max.getZ() + 1.0;
      }

      SchematicCapture.Inner_DataUtil intersection(SchematicCapture.Inner_DataUtil other) {
         int x0 = Math.max(this.min.getX(), other.min.getX());
         int x1 = Math.min(this.max.getX(), other.max.getX());
         int y0 = Math.max(this.min.getY(), other.min.getY());
         int y1 = Math.min(this.max.getY(), other.max.getY());
         int z0 = Math.max(this.min.getZ(), other.min.getZ());
         int z1 = Math.min(this.max.getZ(), other.max.getZ());
         return x0 <= x1 && y0 <= y1 && z0 <= z1 ? new SchematicCapture.Inner_DataUtil(new BlockPos(x0, y0, z0), new BlockPos(x1, y1, z1)) : null;
      }

      long volume() {
         return ((long)this.max.getX() - this.min.getX() + 1L)
            * ((long)this.max.getY() - this.min.getY() + 1L)
            * ((long)this.max.getZ() - this.min.getZ() + 1L);
      }

      String size() {
         return this.max.getX() - this.min.getX() + 1 + " × " + (this.max.getY() - this.min.getY() + 1) + " × " + (this.max.getZ() - this.min.getZ() + 1);
      }
   }

   private static final class Inner_StreamUtil {
      private int minX = 2147483647;
      private int minY = 2147483647;
      private int minZ = 2147483647;
      private int maxX = -2147483648;
      private int maxY = -2147483648;
      private int maxZ = -2147483648;

      void include(int x, int y, int z) {
         this.minX = Math.min(this.minX, x);
         this.minY = Math.min(this.minY, y);
         this.minZ = Math.min(this.minZ, z);
         this.maxX = Math.max(this.maxX, x);
         this.maxY = Math.max(this.maxY, y);
         this.maxZ = Math.max(this.maxZ, z);
      }

      void includeBounds(SchematicCapture.Inner_StreamUtil other) {
         if (!other.init_Z()) {
            this.include(other.minX, other.minY, other.minZ);
            this.include(other.maxX, other.maxY, other.maxZ);
         }
      }

      boolean init_Z() {
         return this.minX > this.maxX;
      }

      SchematicCapture.Inner_DataUtil toArea() {
         if (this.init_Z()) {
            throw new IllegalArgumentException("The captured area contains only air");
         } else {
            return new SchematicCapture.Inner_DataUtil(new BlockPos(this.minX, this.minY, this.minZ), new BlockPos(this.maxX, this.maxY, this.maxZ));
         }
      }

      void includeNonAirBlocks(SchematicCapture.Inner_CachePool chunk) {
         SchematicCapture.Inner_DataUtil a = chunk.area;
         int x0 = a.min.getX();
         int x1 = a.max.getX();
         int z0 = a.min.getZ();
         int z1 = a.max.getZ();

         for (int sectionY : chunk.init_Map.keySet()) {
            SchematicDocument.checkInterrupted();
            PalettedContainer<BlockState> section = chunk.init_Map.get(sectionY);
            int bottom = sectionY << 4;
            int y0 = Math.max(bottom, a.min.getY());
            int y1 = Math.min(bottom + 15, a.max.getY());
            if (y0 <= y1 && (x0 < this.minX || x1 > this.maxX || y0 < this.minY || y1 > this.maxY || z0 < this.minZ || z1 > this.maxZ)) {
               if (!section.maybeHas(BlockStateBase::isAir)) {
                  this.include(x0, y0, z0);
                  this.include(x1, y1, z1);
               } else {
                  for (int y = y0; y <= y1; y++) {
                     boolean insideY = y >= this.minY && y <= this.maxY;

                     for (int z = z0; z <= z1; z++) {
                        boolean insideRow = insideY && z >= this.minZ && z <= this.maxZ;

                        for (int x = x0; x <= x1; x++) {
                           if ((!insideRow || x < this.minX || x > this.maxX) && !section.get(x & 15, y & 15, z & 15).isAir()) {
                              this.include(x, y, z);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      void includeBlockEntities(SchematicCapture.Inner_CachePool chunk) {
         BlockPos at = chunk.area.min;

         for (CompoundTag tag : chunk.blockEntityTags) {
            this.include(at.getX() + tag.getIntOr("x", 0), at.getY() + tag.getIntOr("y", 0), at.getZ() + tag.getIntOr("z", 0));
         }

         for (CompoundTag tag : chunk.entityTags) {
            ListTag pos = tag.getListOrEmpty("Pos");
            if (pos.size() == 3) {
               this.include(
                  at.getX() + (int)Math.floor(pos.getDoubleOr(0, 0.0)),
                  at.getY() + (int)Math.floor(pos.getDoubleOr(1, 0.0)),
                  at.getZ() + (int)Math.floor(pos.getDoubleOr(2, 0.0))
               );
            }
         }
      }
   }
}
