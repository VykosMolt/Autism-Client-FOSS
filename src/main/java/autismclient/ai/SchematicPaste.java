package autismclient.ai;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.storage.TagValueInput;

final class SchematicPaste {
   private static volatile SchematicPaste active;
   private static boolean read_Z;
   private final IntegratedServer server;
   private final ServerLevel level;
   private final SchematicDocument document;
   private final BlockPos origin;
   private final int rotation;
   private final String mirror;
   private final String operationId;
   private final boolean includeAir;
   private final boolean includeEntities;
   private final Future<SchematicPaste.Inner_TableCache> init_Future;
   private List<SchematicPaste.Inner_DataUtil> columns;
   private BitSet[][] occupiedMasks;
   private long total;
   private final Map<BlockState, BlockState> transformedStates = new IdentityHashMap<>();
   private final Map<Long, BitSet> load_Map = new HashMap<>();
   private final boolean resumed;
   private final MutableBlockPos init_BlockPos_MutableBl = new MutableBlockPos();
   private final MutableBlockPos load_BlockPos_MutableBl = new MutableBlockPos();
   private int phase;
   private int column;
   private int range;
   private int cell;
   private int blockEntityIndex;
   private int entityIndex;
   private long done;
   private ChunkPos init_ChunkPos;
   private CompletableFuture<?> init_CompletableFuture;
   private int stallCheckTick;
   private long read_J;
   private LevelChunk cachedChunk;
   volatile boolean init_Z;
   volatile boolean finished;
   volatile SchematicPaste.Inner_StreamUtil progress;

   private SchematicPaste(
      IntegratedServer server,
      ServerLevel level,
      SchematicDocument document,
      BlockPos origin,
      int rotation,
      String mirror,
      boolean includeAir,
      boolean includeEntities,
      JsonObject checkpoint
   ) {
      this.server = server;
      this.level = level;
      this.document = document;
      this.origin = origin.immutable();
      this.rotation = rotation;
      this.mirror = mirror;
      this.includeAir = includeAir;
      this.includeEntities = includeEntities;
      long top = (long)origin.getY() + document.sizeY - 1L;
      if (origin.getY() >= level.getMinY() && top <= level.getMaxY()) {
         int endX = Math.addExact(origin.getX(), document.rotatedSizeX(rotation, mirror) - 1);
         int endZ = Math.addExact(origin.getZ(), document.rotatedSizeZ(rotation) - 1);
         if (level.getWorldBorder().isWithinBounds(origin) && level.getWorldBorder().isWithinBounds(new BlockPos(endX, origin.getY(), endZ))) {
            this.resumed = !checkpoint.isEmpty();
            this.operationId = SchematicLibrary.getString(checkpoint, "operation", UUID.randomUUID().toString());
            this.phase = (int)SchematicLibrary.getLong(checkpoint, "phase", 0L);
            this.column = (int)SchematicLibrary.getLong(checkpoint, "column", 0L);
            this.range = (int)SchematicLibrary.getLong(checkpoint, "range", 0L);
            this.cell = (int)SchematicLibrary.getLong(checkpoint, "cell", 0L);
            this.blockEntityIndex = (int)SchematicLibrary.getLong(checkpoint, "blockEntity", 0L);
            this.entityIndex = (int)SchematicLibrary.getLong(checkpoint, "entity", 0L);
            this.done = SchematicLibrary.getLong(checkpoint, "done", 0L);
            this.total = Math.max(0L, SchematicLibrary.getLong(checkpoint, "total", 0L));
            if (this.phase >= 0
               && this.phase <= 4
               && this.column >= 0
               && this.range >= 0
               && this.cell >= 0
               && this.blockEntityIndex >= 0
               && this.blockEntityIndex <= document.blockEntities.size()
               && this.entityIndex >= 0
               && this.entityIndex <= document.entities.size()
               && this.done >= 0L) {
               this.init_Future = SchematicLibrary.EXECUTOR.submit(() -> prepare(document, this.origin, rotation, mirror, includeAir));
               this.writeCursor("");
            } else {
               throw new IllegalArgumentException("Invalid paste checkpoint");
            }
         } else {
            throw new IllegalArgumentException("The schematic crosses the world border");
         }
      } else {
         throw new IllegalArgumentException(
            "Paste spans Y " + origin.getY() + " to " + top + ". Move the placement inside Y " + level.getMinY() + " to " + level.getMaxY() + "."
         );
      }
   }

   private void applyPrepared(SchematicPaste.Inner_TableCache prepared) {
      this.columns = prepared.columns();
      this.occupiedMasks = prepared.occupied();
      this.total = 2L * prepared.work() + this.document.blockEntities.size() + (this.includeEntities ? this.document.entities.size() : 0);
      if (this.column <= this.columns.size() && this.done <= this.total) {
         if (this.column < this.columns.size()
            && (
               this.range >= this.columns.get(this.column).ranges.size()
                  || this.cell
                     > 256
                        * (
                           this.columns.get(this.column).ranges.get(this.range).max
                              - this.columns.get(this.column).ranges.get(this.range).min
                              + 1
                        )
            )) {
            throw new IllegalArgumentException("Invalid paste cursor");
         }
      } else {
         throw new IllegalArgumentException("Invalid paste checkpoint");
      }
   }

   static SchematicPaste init(Minecraft mc, SchematicDocument document, BlockPos origin, int rotation, String mirror, boolean includeAir, boolean includeEntities, JsonObject checkpoint) {
      IntegratedServer server = mc.getSingleplayerServer();
      if (server != null && !server.isPublished() && mc.level != null) {
         ServerLevel level = server.getLevel(mc.level.dimension());
         if (level == null) {
            throw new IllegalStateException("World is unavailable");
         }

         if (active != null && !active.finished) {
            throw new IllegalStateException("Another paste is still running");
         }

         SchematicPaste job = new SchematicPaste(server, level, document, origin, rotation, mirror, includeAir, includeEntities, checkpoint);
         if (!read_Z) {
            read_Z = true;
            ServerTickEvents.END_SERVER_TICK.register(s -> {
               SchematicPaste current = active;
               if (current != null && current.server == s) {
                  current.runSlice();
               }
            });
            ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
               SchematicPaste current = active;
               if (current != null && current.server == s) {
                  current.finish("");
               }
            });
         }

         active = job;
         return job;
      } else {
         throw new IllegalStateException("Paste is available only in your single-player world");
      }
   }

   void init() {
      this.init_Z = true;
   }

   private void runSlice() {
      if (!this.init_Z && !this.progress.complete && !this.server.isPublished()) {
         long deadline = System.nanoTime() + Math.clamp(37000000L - this.server.getAverageTickTimeNanos(), 4000000L, 14000000L);
         this.cachedChunk = null;

         try {
            if (this.columns == null) {
               if (!this.init_Future.isDone()) {
                  return;
               }

               this.applyPrepared(this.init_Future.get());
            }

            for (int n = 0; n < 32768 && ((n & 31) != 0 || System.nanoTime() < deadline) && !this.init_Z; n++) {
               if (this.phase == 0 || this.phase == 3) {
                  if (this.column >= this.columns.size()) {
                     this.phase = this.phase == 0 ? 1 : 4;
                     this.column = this.range = this.cell = 0;
                  } else {
                     SchematicPaste.Inner_DataUtil column = this.columns.get(this.column);
                     SchematicPaste.Inner_CachePool range = column.ranges.get(this.range);
                     int rangeSize = (range.max - range.min + 1) * 256;
                     long maskKey = (long)this.column << 32 | this.range;
                     if (this.cell < rangeSize) {
                        BitSet mask = this.phase == 3 && !this.resumed ? this.load_Map.get(maskKey) : this.occupiedMasks[this.column][this.range];
                        int next = mask == null ? -1 : mask.nextSetBit(this.cell);
                        if (next < 0) {
                           next = rangeSize;
                        }

                        this.done = this.done + (next - this.cell);
                        this.cell = next;
                     }

                     if (this.cell >= rangeSize) {
                        if (this.phase == 3) {
                           this.load_Map.remove(maskKey);
                        }

                        this.cell = 0;
                        if (++this.range >= column.ranges.size()) {
                           this.column++;
                           this.range = 0;
                        }
                     } else {
                        if (!this.loadChunk(column.x, column.z)) {
                           break;
                        }

                        int index = this.cell;
                        int y = range.min + index / 256;
                        int x = (column.x << 4) + (index & 15);
                        int z = (column.z << 4) + (index % 256 >> 4);
                        int tx = x - this.origin.getX();
                        int tz = z - this.origin.getZ();
                        this.init_V_I(tx, y - this.origin.getY(), tz);
                        SchematicDocument.Inner_CachePool region = this.document
                           .regionAt(this.init_BlockPos_MutableBl.getX(), this.init_BlockPos_MutableBl.getY(), this.init_BlockPos_MutableBl.getZ());
                        if (region != null) {
                           BlockState saved = region.stateAt(
                              this.init_BlockPos_MutableBl.getX(), this.init_BlockPos_MutableBl.getY(), this.init_BlockPos_MutableBl.getZ()
                           );
                           if (this.includeAir || !saved.isAir()) {
                              this.load_BlockPos_MutableBl.set(x, y, z);
                              if (this.phase == 0) {
                                 BlockState state = this.transformedStates.computeIfAbsent(saved, s -> SchematicDocument.transformState(s, this.rotation, this.mirror));
                                 if (this.cachedChunk.getBlockState(this.load_BlockPos_MutableBl) != state) {
                                    boolean written = this.level.setBlock(this.load_BlockPos_MutableBl, state, 818);
                                    if (!written && this.cachedChunk.getBlockState(this.load_BlockPos_MutableBl) != state) {
                                       throw new IllegalStateException("Could not paste at " + this.load_BlockPos_MutableBl.toShortString());
                                    }

                                    if (!this.resumed) {
                                       this.load_Map.computeIfAbsent(maskKey, key -> new BitSet()).set(index);
                                    }
                                 }
                              } else {
                                 BlockState live = this.cachedChunk.getBlockState(this.load_BlockPos_MutableBl);
                                 this.level.updateNeighborsAt(this.load_BlockPos_MutableBl, live.getBlock());
                                 if (!live.getFluidState().isEmpty()) {
                                    this.level.scheduleTick(this.load_BlockPos_MutableBl, live.getFluidState().getType(), 1);
                                 }
                              }
                           }
                        }

                        this.cell++;
                        this.done++;
                     }
                  }
               } else if (this.phase == 1) {
                  if (this.blockEntityIndex >= this.document.blockEntities.size()) {
                     this.phase = 2;
                  } else {
                     SchematicDocument.Inner_DataUtil saved = this.document.blockEntities.get(this.blockEntityIndex);
                     BlockPos local = this.document.init(saved.position().getX(), saved.position().getY(), saved.position().getZ(), this.rotation, this.mirror);
                     BlockPos at = this.origin.offset(local);
                     if (!this.loadChunk(at.getX() >> 4, at.getZ() >> 4)) {
                        break;
                     }

                     BlockEntity be = this.level.getBlockEntity(at);
                     if (be != null) {
                        CompoundTag tag = saved.data().copy();
                        tag.putInt("x", at.getX());
                        tag.putInt("y", at.getY());
                        tag.putInt("z", at.getZ());
                        be.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, this.level.registryAccess(), tag));
                        be.setChanged();
                        this.level.sendBlockUpdated(at, this.level.getBlockState(at), this.level.getBlockState(at), 2);
                     }

                     this.blockEntityIndex++;
                     this.done++;
                  }
               } else {
                  if (this.phase != 2) {
                     this.finish("");
                     return;
                  }

                  if (this.includeEntities && this.entityIndex < this.document.entities.size()) {
                     SchematicDocument.Inner_BufferHelper saved = this.document.entities.get(this.entityIndex);
                     double[] at = this.transformPoint(saved.x(), saved.y(), saved.z());
                     if (!this.loadChunk((int)Math.floor(at[0]) >> 4, (int)Math.floor(at[2]) >> 4)) {
                        break;
                     }

                     UUID uuid = entityUuid(this.operationId, this.entityIndex, 0);
                     if (this.level.getEntity(uuid) == null) {
                        CompoundTag tag = saved.data().copy();
                        ListTag raw = tag.getListOrEmpty("Pos");
                        double ox = saved.x() - (raw.size() == 3 ? raw.getDoubleOr(0, 0.0) : 0.0);
                        double oy = saved.y() - (raw.size() == 3 ? raw.getDoubleOr(1, 0.0) : 0.0);
                        double oz = saved.z() - (raw.size() == 3 ? raw.getDoubleOr(2, 0.0) : 0.0);
                        int ordinal = this.entityIndex;
                        int[] child = new int[]{0};
                        Entity entity = EntityType.loadEntityRecursive(
                           TagValueInput.create(ProblemReporter.DISCARDING, this.level.registryAccess(), tag),
                           this.level,
                           EntitySpawnReason.LOAD,
                           ex -> {
                              ex.setUUID(entityUuid(this.operationId, ordinal, (int)(child[0]++)));
                              double[] p = this.transformPoint(ex.getX() + ox, ex.getY() + oy, ex.getZ() + oz);
                              ex.setPos(p[0], p[1], p[2]);
                              if (this.mirror.equals("x")) {
                                 ex.setYRot(ex.mirror(Mirror.FRONT_BACK));
                              } else if (this.mirror.equals("z")) {
                                 ex.setYRot(ex.mirror(Mirror.LEFT_RIGHT));
                              }

                              Rotation var10000;
                              switch (this.rotation) {
                                 case 90:
                                    var10000 = Rotation.CLOCKWISE_90;
                                    break;
                                 case 180:
                                    var10000 = Rotation.CLOCKWISE_180;
                                    break;
                                 case 270:
                                    var10000 = Rotation.COUNTERCLOCKWISE_90;
                                    break;
                                 default:
                                    var10000 = Rotation.NONE;
                              }

                              Rotation r = var10000;
                              ex.setYRot(ex.rotate(r));
                              return ex;
                           }
                        );
                        if (entity == null) {
                           throw new IllegalArgumentException("Could not load saved entity " + this.entityIndex);
                        }

                        this.level.addFreshEntityWithPassengers(entity);
                     }

                     this.entityIndex++;
                     this.done++;
                  } else {
                     this.phase = 3;
                     this.column = this.range = this.cell = 0;
                  }
               }
            }

            this.writeCursor("");
         } catch (Exception e) {
            this.finish(SchematicLibrary.describeError(e));
         }
      } else {
         this.finish("");
      }
   }

   private void finish(String error) {
      if (!this.init_Future.isDone()) {
         this.init_Future.cancel(true);
      }

      this.writeCursor(error);
      this.finished = true;
      if (active == this) {
         active = null;
      }
   }

   private boolean loadChunk(int x, int z) {
      long key = ChunkPos.pack(x, z);
      if (this.cachedChunk != null && this.read_J == key) {
         return true;
      }

      ChunkPos pos = new ChunkPos(x, z);
      ServerChunkCache source = this.level.getChunkSource();
      source.addTicketWithRadius(TicketType.UNKNOWN, pos, 0);
      this.cachedChunk = source.getChunkNow(x, z);
      if (this.cachedChunk != null) {
         this.read_J = key;
         this.init_ChunkPos = null;
         return true;
      }

      if (!pos.equals(this.init_ChunkPos)) {
         this.init_ChunkPos = pos;
         this.init_CompletableFuture = CompletableFuture.<CompletableFuture<ChunkResult<ChunkAccess>>>supplyAsync(
               () -> source.getChunkFuture(x, z, ChunkStatus.FULL, true), SchematicLibrary.EXECUTOR
            )
            .thenCompose(future -> (CompletionStage<ChunkResult<ChunkAccess>>)future);
         this.stallCheckTick = this.server.getTickCount();
      }

      if (this.server.getTickCount() - this.stallCheckTick > 1200) {
         throw new IllegalStateException("Target chunk is still unavailable; paste can be resumed");
      }

      if (this.init_CompletableFuture.isCompletedExceptionally()) {
         this.init_CompletableFuture.join();
      }

      return false;
   }

   private void init_V_I(int x, int y, int z) {
      int sx;
      int sz;
      switch (this.rotation) {
         case 90:
            sx = z;
            sz = this.document.sizeZ - 1 - x;
            break;
         case 180:
            sx = this.document.sizeX - 1 - x;
            sz = this.document.sizeZ - 1 - z;
            break;
         case 270:
            sx = this.document.sizeX - 1 - z;
            sz = x;
            break;
         default:
            sx = x;
            sz = z;
      }

      if (this.mirror.equals("x")) {
         sx = this.document.sizeX - 1 - sx;
      } else if (this.mirror.equals("z")) {
         sz = this.document.sizeZ - 1 - sz;
      }

      this.init_BlockPos_MutableBl.set(sx, y, sz);
   }

   private double[] transformPoint(double x, double y, double z) {
      if (this.mirror.equals("x")) {
         x = this.document.sizeX - x;
      } else if (this.mirror.equals("z")) {
         z = this.document.sizeZ - z;
      }

      double tx;
      double tz;
      switch (this.rotation) {
         case 90:
            tx = this.document.sizeZ - z;
            tz = x;
            break;
         case 180:
            tx = this.document.sizeX - x;
            tz = this.document.sizeZ - z;
            break;
         case 270:
            tx = z;
            tz = this.document.sizeX - x;
            break;
         default:
            tx = x;
            tz = z;
      }

      return new double[]{this.origin.getX() + tx, this.origin.getY() + y, this.origin.getZ() + tz};
   }

   private void writeCursor(String error) {
      JsonObject cursor = AiJson.objectOf(
         "operation",
         this.operationId,
         "phase",
         this.phase,
         "column",
         this.column,
         "range",
         this.range,
         "cell",
         this.cell,
         "blockEntity",
         this.blockEntityIndex,
         "entity",
         this.entityIndex,
         "done",
         this.done,
         "total",
         this.total,
         "includeAir",
         this.includeAir,
         "includeEntities",
         this.includeEntities
      );
      String var10000;
      if (this.columns == null) {
         var10000 = "Preparing paste";
      } else {
         switch (this.phase) {
            case 0:
               var10000 = "Pasting blocks";
               break;
            case 1:
               var10000 = "Restoring block data";
               break;
            case 2:
               var10000 = "Restoring entities";
               break;
            case 3:
               var10000 = "Updating neighbours";
               break;
            default:
               var10000 = "Paste complete";
         }
      }

      String status = var10000;
      this.progress = new SchematicPaste.Inner_StreamUtil(
         status,
         this.phase == 4 ? 100 : (this.total == 0L ? 0 : (int)Math.min(99L, 100L * this.done / this.total)),
         this.done,
         this.total,
         cursor,
         this.phase == 4,
         error
      );
   }

   static UUID entityUuid(String operation, int index, int child) {
      return UUID.nameUUIDFromBytes((operation + ":" + index + ":" + child).getBytes(StandardCharsets.UTF_8));
   }

   static SchematicPaste.Inner_TableCache prepare(SchematicDocument doc, BlockPos origin, int rotation, String mirror, boolean includeAir) {
      List<SchematicPaste.Inner_DataUtil> columns = columnsFor(doc, origin, rotation, mirror);
      Long2IntOpenHashMap columnIndex = new Long2IntOpenHashMap(columns.size());
      BitSet[][] occupied = new BitSet[columns.size()][];
      long work = 0L;

      for (int i = 0; i < columns.size(); i++) {
         SchematicPaste.Inner_DataUtil col = columns.get(i);
         columnIndex.put(ChunkPos.pack(col.x(), col.z()), i);
         occupied[i] = new BitSet[col.ranges().size()];

         for (SchematicPaste.Inner_CachePool range : col.ranges()) {
            work += (range.max() - range.min() + 1) * 256L;
         }
      }

      BlockPos p0 = doc.init(0, 0, 0, rotation, mirror);
      BlockPos dx = doc.init(1, 0, 0, rotation, mirror).subtract(p0);
      BlockPos dz = doc.init(0, 0, 1, rotation, mirror).subtract(p0);
      boolean multiRegion = doc.regions.size() > 1;

      for (SchematicDocument.Inner_CachePool r : doc.regions) {
         SchematicDocument.init();
         if (includeAir || !Arrays.stream(r.palette).allMatch(BlockStateBase::isAir)) {
            int cellIndex = 0;

            for (int sy = r.load; sy < r.load + r.update; sy++) {
               int wy = sy + origin.getY();

               for (int sz = r.read; sz < r.read + r.sync; sz++) {
                  int wx = origin.getX() + p0.getX() + dx.getX() * r.originX + dz.getX() * sz;
                  int wz = origin.getZ() + p0.getZ() + dx.getZ() * r.originX + dz.getZ() * sz;
                  int lastCol = -1;
                  int rangeIdx = 0;
                  int yOffset = 0;
                  BitSet bits = null;

                  for (int sx = r.originX; sx < r.originX + r.write; wz += dx.getZ()) {
                     int idx = cellIndex++;
                     if ((idx & 4095) == 0) {
                        SchematicDocument.init();
                     }

                     if (includeAir || !r.stateAtIndex(idx).isAir() && (!multiRegion || doc.regionAt(sx, sy, sz) == r)) {
                        int colIdx = columnIndex.get(ChunkPos.pack(wx >> 4, wz >> 4));
                        if (colIdx != lastCol) {
                           List<SchematicPaste.Inner_CachePool> ranges = columns.get(colIdx).ranges();
                           rangeIdx = 0;

                           while (ranges.get(rangeIdx).max() < wy) {
                              rangeIdx++;
                           }

                           yOffset = (wy - ranges.get(rangeIdx).min()) * 256;
                           bits = occupied[colIdx][rangeIdx];
                           if (bits == null) {
                              occupied[colIdx][rangeIdx] = bits = new BitSet();
                           }

                           lastCol = colIdx;
                        }

                        bits.set(yOffset | (wz & 15) << 4 | wx & 15);
                     }

                     sx++;
                     wx += dx.getX();
                  }
               }
            }
         }
      }

      return new SchematicPaste.Inner_TableCache(columns, occupied, work);
   }

   static List<SchematicPaste.Inner_DataUtil> columnsFor(SchematicDocument doc, BlockPos origin, int rotation, String mirror) {
      TreeMap<Long, List<SchematicPaste.Inner_CachePool>> ranges = new TreeMap<>();

      for (SchematicDocument.Inner_CachePool r : doc.regions) {
         BlockPos a = doc.init(r.originX, r.load, r.read, rotation, mirror);
         BlockPos b = doc.init(r.originX + r.write - 1, r.load, r.read + r.sync - 1, rotation, mirror);
         int x0 = origin.getX() + Math.min(a.getX(), b.getX()) >> 4;
         int x1 = origin.getX() + Math.max(a.getX(), b.getX()) >> 4;
         int z0 = origin.getZ() + Math.min(a.getZ(), b.getZ()) >> 4;
         int z1 = origin.getZ() + Math.max(a.getZ(), b.getZ()) >> 4;

         for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
               ranges.computeIfAbsent(ChunkPos.pack(x, z), k -> new ArrayList<>())
                  .add(new SchematicPaste.Inner_CachePool(origin.getY() + r.load, origin.getY() + r.load + r.update - 1));
            }
         }
      }

      ArrayList<SchematicPaste.Inner_DataUtil> columns = new ArrayList<>();
      ranges.forEach((key, list) -> {
         list.sort(Comparator.comparingInt(SchematicPaste.Inner_CachePool::min));
         ArrayList<SchematicPaste.Inner_CachePool> merged = new ArrayList<>();

         for (SchematicPaste.Inner_CachePool range : list) {
            if (!merged.isEmpty() && range.min <= merged.get(merged.size() - 1).max + 1) {
               SchematicPaste.Inner_CachePool last = merged.remove(merged.size() - 1);
               merged.add(new SchematicPaste.Inner_CachePool(last.min, Math.max(last.max, range.max)));
            } else {
               merged.add(range);
            }
         }

         ChunkPos pos = ChunkPos.unpack(key);
         columns.add(new SchematicPaste.Inner_DataUtil(pos.x(), pos.z(), List.copyOf(merged)));
      });
      return List.copyOf(columns);
   }

   record Inner_CachePool(int min, int max) {
   }

   record Inner_DataUtil(int x, int z, List<SchematicPaste.Inner_CachePool> ranges) {
   }

   record Inner_StreamUtil(String phase, int percent, long done, long total, JsonObject checkpoint, boolean complete, String error) {
   }

   record Inner_TableCache(List<SchematicPaste.Inner_DataUtil> columns, BitSet[][] occupied, long work) {
   }
}
