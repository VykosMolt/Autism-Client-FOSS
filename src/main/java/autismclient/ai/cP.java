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

final class cP {
   private final ClientLevel init_ClientLevel;
   private final IntegratedServer init_IntegratedServer;
   private final List<cP.Inner_CachePool> init_List;
   private final Map<Long, cP.Inner_CachePool> init_Map = new HashMap<>();
   private final Path load_Path;
   private final String read_Str;
   private final boolean read_Z;
   private int load_I;
   private int read_I;
   private List<BlockPos> load_List;
   private int write_I;
   private List<Integer> read_List;
   private int update_I;
   private boolean update_Z;
   private boolean sync_Z;
   private CompletableFuture<Boolean> init_CompletableFuture;
   private int sync_I;
   private int flush;
   private List<BlockPos> write_List;
   volatile boolean init_Z;
   volatile boolean load_Z;
   volatile int init_I;
   volatile String init_Str = "Copying chunks";
   volatile String load_Str = "";
   volatile Path init_Path;
   private Future<?> init_Future;

   cP(Minecraft mc, Path folder, String name, cP.Inner_DataUtil selection, boolean includeEntities) {
      if (mc.level != null && mc.player != null) {
         this.init_ClientLevel = mc.level;
         this.init_IntegratedServer = mc.getSingleplayerServer();
         this.load_Path = folder;
         this.read_Str = init_Str_Str(name);
         this.read_Z = includeEntities;
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
                  LevelChunk live = this.init_ClientLevel.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                  if (live == null) {
                     throw new IllegalArgumentException("A selected chunk is outside view distance");
                  }

                  chunks.add(live);
               }
            }
         } else {
            chunks.addAll(cV.loadedChunks(this.init_ClientLevel));
            chunks.sort(Comparator.<LevelChunk>comparingInt(c -> c.getPos().x()).thenComparingInt(c -> c.getPos().z()));
         }

         ArrayList<cP.Inner_CachePool> captured = new ArrayList<>(chunks.size());

         for (LevelChunk live : chunks) {
            ChunkPos chunkPos = live.getPos();
            int x0 = chunkPos.x() << 4;
            int z0 = chunkPos.z() << 4;
            cP.Inner_DataUtil fullArea = new cP.Inner_DataUtil(
               new BlockPos(x0, this.init_ClientLevel.getMinY(), z0), new BlockPos(x0 + 15, this.init_ClientLevel.getMaxY(), z0 + 15)
            );
            cP.Inner_DataUtil area = selection == null ? fullArea : fullArea.intersection(selection);
            if (area != null) {
               cP.Inner_CachePool chunk = new cP.Inner_CachePool(chunkPos, area);
               captured.add(chunk);
               this.init_Map.put(chunkPos.pack(), chunk);
            }
         }

         if (captured.isEmpty()) {
            throw new IllegalArgumentException("No loaded chunks in this area");
         }

         this.init_List = List.copyOf(captured);
         this.read_I = this.init_List.get(0).init_cP_DataUtil.min.getY() >> 4;
      } else {
         throw new IllegalStateException("Join a world first");
      }
   }

   void init_V() {
      this.init_Z = true;
      if (this.init_Future != null) {
         this.init_Future.cancel(true);
      }

      this.init_Str = "Cancelled";
   }

   void init_V_Minecraft(Minecraft mc) {
      if (!this.load_Z && !this.init_Z && !this.update_Z) {
         if (mc.level != this.init_ClientLevel) {
            this.init_V();
         } else {
            long deadline = System.nanoTime() + 1500000L;

            try {
               do {
                  if (this.load_I < this.init_List.size()) {
                     cP.Inner_CachePool target = this.init_List.get(this.load_I);
                     LevelChunk live = this.init_ClientLevel
                        .getChunkSource()
                        .getChunk(target.init_ChunkPos.x(), target.init_ChunkPos.z(), ChunkStatus.FULL, false);
                     if (live == null) {
                        throw new IllegalStateException("A chunk unloaded during capture; save again");
                     }

                     if (this.read_I <= target.init_cP_DataUtil.max.getY() >> 4) {
                        int index = live.getSectionIndex(this.read_I << 4);
                        LevelChunkSection section = live.getSections()[index];
                        if (section != null && !section.hasOnlyAir()) {
                           target.init_Map.put(this.read_I, section.getStates().copy());
                        }

                        this.read_I++;
                        this.init_I = Math.max(
                           this.init_I,
                           (int)(
                              65.0
                                 * (
                                    this.load_I
                                       + (double)(this.read_I - (target.init_cP_DataUtil.min.getY() >> 4))
                                          / Math.max(1, (target.init_cP_DataUtil.max.getY() >> 4) - (target.init_cP_DataUtil.min.getY() >> 4) + 1)
                                 )
                                 / this.init_List.size()
                           )
                        );
                     } else if (this.init_IntegratedServer != null) {
                        this.load_I++;
                        if (this.load_I < this.init_List.size()) {
                           this.read_I = this.init_List.get(this.load_I).init_cP_DataUtil.min.getY() >> 4;
                        }
                     } else {
                        if (this.load_List == null) {
                           this.load_List = List.copyOf(live.getBlockEntities().keySet());
                           this.write_I = 0;
                        }

                        if (this.write_I < this.load_List.size()) {
                           BlockPos p = this.load_List.get(this.write_I++);
                           if (target.init_cP_DataUtil.contains(p.getX(), p.getY(), p.getZ())) {
                              BlockEntity be = this.init_ClientLevel.getBlockEntity(p);
                              if (be != null) {
                                 CompoundTag tag = be.saveWithFullMetadata(this.init_ClientLevel.registryAccess());
                                 tag.putInt("x", p.getX() - target.init_cP_DataUtil.min.getX());
                                 tag.putInt("y", p.getY() - target.init_cP_DataUtil.min.getY());
                                 tag.putInt("z", p.getZ() - target.init_cP_DataUtil.min.getZ());
                                 target.init_List.add(tag);
                              }
                           }
                        } else {
                           this.load_I++;
                           this.load_List = null;
                           if (this.load_I < this.init_List.size()) {
                              this.read_I = this.init_List.get(this.load_I).init_cP_DataUtil.min.getY() >> 4;
                           }
                        }
                     }
                  } else if (this.init_IntegratedServer != null && !this.sync_Z) {
                     if (this.init_CompletableFuture != null && !this.init_CompletableFuture.isDone()) {
                        return;
                     }

                     if (this.init_CompletableFuture == null || !this.init_CompletableFuture.join()) {
                        this.init_Str = "Copying block data";
                        this.init_CompletableFuture = this.init_IntegratedServer.submit(this::init_Z);
                        return;
                     }

                     this.init_CompletableFuture = null;
                     this.sync_Z = true;
                  } else {
                     if (!this.read_Z) {
                        this.load();
                        return;
                     }

                     if (this.read_List == null) {
                        this.read_List = new ArrayList<>();

                        for (Entity entity : this.init_ClientLevel.entitiesForRendering()) {
                           if (!(entity instanceof Player) && !entity.isPassenger()) {
                              this.read_List.add(entity.getId());
                              if (this.read_List.size() > 16384) {
                                 throw new IllegalArgumentException("Too many loaded entities");
                              }
                           }
                        }
                     }

                     if (this.update_I >= this.read_List.size()) {
                        this.load();
                        return;
                     }

                     Entity entity = this.init_ClientLevel.getEntity(this.read_List.get(this.update_I++));
                     if (entity != null) {
                        cP.Inner_CachePool target = this.init_Map.get(ChunkPos.pack(entity.getBlockX() >> 4, entity.getBlockZ() >> 4));
                        if (target != null && target.init_cP_DataUtil.contains(entity.getX(), entity.getY(), entity.getZ())) {
                           TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, this.init_ClientLevel.registryAccess());
                           if (entity.save(output)) {
                              CompoundTag tag = output.buildResult();
                              init(tag, target.init_cP_DataUtil.min);
                              target.load.add(tag);
                           }
                        }
                     }
                  }
               } while (System.nanoTime() < deadline);
            } catch (Exception e) {
               this.load_Str = cR.init_Str_Throwable(e);
               this.init_Str = "Save failed";
               this.load_Z = true;
            }
         }
      }
   }

   private boolean init_Z() {
      ServerLevel level = this.init_IntegratedServer.getLevel(this.init_ClientLevel.dimension());
      if (level == null) {
         throw new IllegalStateException("World changed during capture");
      }

      long deadline = System.nanoTime() + 1200000L;

      while (this.sync_I < this.init_List.size() && !this.init_Z && System.nanoTime() < deadline) {
         cP.Inner_CachePool target = this.init_List.get(this.sync_I);
         ChunkAccess live = level.getChunkSource().getChunk(target.init_ChunkPos.x(), target.init_ChunkPos.z(), ChunkStatus.FULL, false);
         if (live == null) {
            throw new IllegalStateException("A chunk unloaded during capture; save again");
         }

         if (this.write_List == null) {
            this.write_List = List.copyOf(live.getBlockEntitiesPos());
            this.flush = 0;
         }

         if (this.flush >= this.write_List.size()) {
            this.write_List = null;
            this.sync_I++;
         } else {
            BlockPos at = this.write_List.get(this.flush++);
            if (target.init_cP_DataUtil.contains(at.getX(), at.getY(), at.getZ())) {
               CompoundTag tag = live.getBlockEntityNbtForSaving(at, level.registryAccess());
               if (tag != null) {
                  tag = tag.copy();
                  tag.remove("keepPacked");
                  tag.putInt("x", at.getX() - target.init_cP_DataUtil.min.getX());
                  tag.putInt("y", at.getY() - target.init_cP_DataUtil.min.getY());
                  tag.putInt("z", at.getZ() - target.init_cP_DataUtil.min.getZ());
                  target.init_List.add(tag);
               }
            }
         }
      }

      return this.sync_I == this.init_List.size();
   }

   private void load() {
      this.update_Z = true;
      this.init_I = 70;
      this.init_Str = "Packing schematic";
      this.init_Future = cR.init_ExecutorService.submit(() -> {
         try {
            this.init_Path = init(this.load_Path, this.read_Str, this.init_List, true, p -> {
               this.init_I = 70 + p / 5;
               this.read();
            });
            this.init_I = 100;
            this.init_Str = "Saved";
            this.load_Z = true;
         } catch (CancellationException ignored) {
            this.init_Z = true;
         } catch (Exception e) {
            this.load_Str = cR.init_Str_Throwable(e);
            this.init_Str = "Save failed";
            this.load_Z = true;
         } finally {
            Thread.interrupted();
         }
      });
   }

   static Path init(Path folder, String name, List<cP.Inner_CachePool> chunks, boolean trimAir, IntConsumer progress) throws IOException {
      Files.createDirectories(folder);
      CompoundTag root = init(chunks, trimAir, progress);
      cQ.init();
      Path temporary = Files.createTempFile(folder, ".capture-", ".tmp");

      try {
         NbtIo.writeCompressed(root, temporary);
         cQ.init();
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

   private void read() {
      cQ.init();
      if (this.init_Z) {
         throw new CancellationException();
      }
   }

   static CompoundTag init(List<cP.Inner_CachePool> chunks, boolean trimAir, IntConsumer progress) {
      cP.Inner_StreamUtil bounds = new cP.Inner_StreamUtil();
      IdentityHashMap<cP.Inner_CachePool, cP.Inner_StreamUtil> perChunkBounds = trimAir ? new IdentityHashMap<>() : null;
      if (trimAir) {
         int checked = 0;

         for (cP.Inner_CachePool chunk : chunks) {
            cQ.init();
            cP.Inner_StreamUtil chunkBounds = new cP.Inner_StreamUtil();
            chunkBounds.init(chunk);
            chunkBounds.load(chunk);
            if (!chunkBounds.init_Z()) {
               perChunkBounds.put(chunk, chunkBounds);
               bounds.init(chunkBounds);
            }

            checked++;
            progress.accept(checked * 20 / chunks.size());
         }
      } else {
         for (cP.Inner_CachePool chunk : chunks) {
            BlockPos lo = chunk.init_cP_DataUtil.min;
            BlockPos hi = chunk.init_cP_DataUtil.max;
            bounds.init(lo.getX(), lo.getY(), lo.getZ());
            bounds.init(hi.getX(), hi.getY(), hi.getZ());
         }
      }

      cP.Inner_DataUtil enclosing = bounds.init();
      LinkedHashMap<cP.Inner_CachePool, cP.Inner_DataUtil> areas = new LinkedHashMap<>();
      long capturedVolume = 0L;

      for (cP.Inner_CachePool chunk : chunks) {
         cP.Inner_DataUtil area;
         if (trimAir) {
            cP.Inner_StreamUtil chunkBounds = perChunkBounds.get(chunk);
            if (chunkBounds == null) {
               continue;
            }

            area = chunk.init_cP_DataUtil.intersection(chunkBounds.init());
         } else {
            area = chunk.init_cP_DataUtil.intersection(enclosing);
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

      for (Entry<cP.Inner_CachePool, cP.Inner_DataUtil> entry : areas.entrySet()) {
         cQ.init();
         cP.Inner_CachePool chunk = entry.getKey();
         cP.Inner_DataUtil a = entry.getValue();
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
            cQ.init();
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
         region.put("Position", init(a.min.getX() - enclosing.min.getX(), a.min.getY() - enclosing.min.getY(), a.min.getZ() - enclosing.min.getZ()));
         region.put("Size", init(w, h, l));
         ListTag entries = new ListTag();

         for (BlockState state : palette) {
            entries.add(NbtUtils.writeBlockState(state));
         }

         region.put("BlockStatePalette", entries);
         region.putLongArray("BlockStates", init(values, palette.size()));
         BlockPos shift = a.min.subtract(chunk.init_cP_DataUtil.min);
         ListTag bes = new ListTag();

         for (CompoundTag saved : chunk.init_List) {
            CompoundTag tag = saved.copy();
            tag.putInt("x", tag.getIntOr("x", 0) - shift.getX());
            tag.putInt("y", tag.getIntOr("y", 0) - shift.getY());
            tag.putInt("z", tag.getIntOr("z", 0) - shift.getZ());
            bes.add(tag);
         }

         region.put("TileEntities", bes);
         ListTag entities = new ListTag();

         for (CompoundTag saved : chunk.load) {
            CompoundTag tag = saved.copy();
            init(tag, shift);
            entities.add(tag);
         }

         region.put("Entities", entities);
         regions.put("chunk_" + chunk.init_ChunkPos.x() + "_" + chunk.init_ChunkPos.z(), region);
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
         init(enclosing.max.getX() - enclosing.min.getX() + 1, enclosing.max.getY() - enclosing.min.getY() + 1, enclosing.max.getZ() - enclosing.min.getZ() + 1)
      );
      meta.putLong("TimeCreated", System.currentTimeMillis());
      meta.putLong("TimeModified", System.currentTimeMillis());
      root.put("Metadata", meta);
      return root;
   }

   static long[] init(int[] ids, int paletteSize) {
      if (paletteSize > 65536) {
         throw new IllegalArgumentException("A captured region has " + paletteSize + " block states; schematics are limited to 65536");
      }

      int bits = cQ.init_I_I(paletteSize);
      long[] data = new long[(int)(((long)ids.length * bits + 63L) / 64L)];

      for (int i = 0; i < ids.length; i++) {
         cQ.init(data, i, bits, ids[i]);
      }

      return data;
   }

   static CompoundTag init(int x, int y, int z) {
      CompoundTag t = new CompoundTag();
      t.putInt("x", x);
      t.putInt("y", y);
      t.putInt("z", z);
      return t;
   }

   static void init(CompoundTag tag, BlockPos base) {
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
            init(p, base);
         }
      }
   }

   static String init_Str_Str(String name) {
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
      final ChunkPos init_ChunkPos;
      final cP.Inner_DataUtil init_cP_DataUtil;
      final Int2ObjectOpenHashMap<PalettedContainer<BlockState>> init_Map = new Int2ObjectOpenHashMap<>();
      final List<CompoundTag> init_List = new ArrayList<>();
      final List<CompoundTag> load = new ArrayList<>();

      Inner_CachePool(ChunkPos pos, cP.Inner_DataUtil area) {
         this.init_ChunkPos = pos;
         this.init_cP_DataUtil = area;
      }
   }

   record Inner_DataUtil(BlockPos min, BlockPos max) {
      static cP.Inner_DataUtil between(BlockPos a, BlockPos b) {
         return new cP.Inner_DataUtil(
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

      cP.Inner_DataUtil intersection(cP.Inner_DataUtil other) {
         int x0 = Math.max(this.min.getX(), other.min.getX());
         int x1 = Math.min(this.max.getX(), other.max.getX());
         int y0 = Math.max(this.min.getY(), other.min.getY());
         int y1 = Math.min(this.max.getY(), other.max.getY());
         int z0 = Math.max(this.min.getZ(), other.min.getZ());
         int z1 = Math.min(this.max.getZ(), other.max.getZ());
         return x0 <= x1 && y0 <= y1 && z0 <= z1 ? new cP.Inner_DataUtil(new BlockPos(x0, y0, z0), new BlockPos(x1, y1, z1)) : null;
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
      private int init = 2147483647;
      private int load = 2147483647;
      private int read = 2147483647;
      private int write = -2147483648;
      private int update = -2147483648;
      private int sync = -2147483648;

      void init(int x, int y, int z) {
         this.init = Math.min(this.init, x);
         this.load = Math.min(this.load, y);
         this.read = Math.min(this.read, z);
         this.write = Math.max(this.write, x);
         this.update = Math.max(this.update, y);
         this.sync = Math.max(this.sync, z);
      }

      void init(cP.Inner_StreamUtil other) {
         if (!other.init_Z()) {
            this.init(other.init, other.load, other.read);
            this.init(other.write, other.update, other.sync);
         }
      }

      boolean init_Z() {
         return this.init > this.write;
      }

      cP.Inner_DataUtil init() {
         if (this.init_Z()) {
            throw new IllegalArgumentException("The captured area contains only air");
         } else {
            return new cP.Inner_DataUtil(new BlockPos(this.init, this.load, this.read), new BlockPos(this.write, this.update, this.sync));
         }
      }

      void init(cP.Inner_CachePool chunk) {
         cP.Inner_DataUtil a = chunk.init_cP_DataUtil;
         int x0 = a.min.getX();
         int x1 = a.max.getX();
         int z0 = a.min.getZ();
         int z1 = a.max.getZ();

         for (int sectionY : chunk.init_Map.keySet()) {
            cQ.init();
            PalettedContainer<BlockState> section = chunk.init_Map.get(sectionY);
            int bottom = sectionY << 4;
            int y0 = Math.max(bottom, a.min.getY());
            int y1 = Math.min(bottom + 15, a.max.getY());
            if (y0 <= y1 && (x0 < this.init || x1 > this.write || y0 < this.load || y1 > this.update || z0 < this.read || z1 > this.sync)) {
               if (!section.maybeHas(BlockStateBase::isAir)) {
                  this.init(x0, y0, z0);
                  this.init(x1, y1, z1);
               } else {
                  for (int y = y0; y <= y1; y++) {
                     boolean insideY = y >= this.load && y <= this.update;

                     for (int z = z0; z <= z1; z++) {
                        boolean insideRow = insideY && z >= this.read && z <= this.sync;

                        for (int x = x0; x <= x1; x++) {
                           if ((!insideRow || x < this.init || x > this.write) && !section.get(x & 15, y & 15, z & 15).isAir()) {
                              this.init(x, y, z);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      void load(cP.Inner_CachePool chunk) {
         BlockPos at = chunk.init_cP_DataUtil.min;

         for (CompoundTag tag : chunk.init_List) {
            this.init(at.getX() + tag.getIntOr("x", 0), at.getY() + tag.getIntOr("y", 0), at.getZ() + tag.getIntOr("z", 0));
         }

         for (CompoundTag tag : chunk.load) {
            ListTag pos = tag.getListOrEmpty("Pos");
            if (pos.size() == 3) {
               this.init(
                  at.getX() + (int)Math.floor(pos.getDoubleOr(0, 0.0)),
                  at.getY() + (int)Math.floor(pos.getDoubleOr(1, 0.0)),
                  at.getZ() + (int)Math.floor(pos.getDoubleOr(2, 0.0))
               );
            }
         }
      }
   }
}
