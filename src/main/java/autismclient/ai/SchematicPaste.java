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
   private static volatile SchematicPaste init_cT;
   private static boolean read_Z;
   private final IntegratedServer init_IntegratedServer;
   private final ServerLevel init_ServerLevel;
   private final SchematicDocument init_cQ;
   private final BlockPos init_BlockPos;
   private final int init_I;
   private final String init_Str;
   private final String load_Str;
   private final boolean write_Z;
   private final boolean update_Z;
   private final Future<SchematicPaste.Inner_TableCache> init_Future;
   private List<SchematicPaste.Inner_DataUtil> init_List;
   private BitSet[][] occupiedMasks;
   private long init_J;
   private final Map<BlockState, BlockState> init_Map = new IdentityHashMap<>();
   private final Map<Long, BitSet> load_Map = new HashMap<>();
   private final boolean sync_Z;
   private final MutableBlockPos init_BlockPos_MutableBl = new MutableBlockPos();
   private final MutableBlockPos load_BlockPos_MutableBl = new MutableBlockPos();
   private int load_I;
   private int read_I;
   private int write_I;
   private int update_I;
   private int sync_I;
   private int flush_I;
   private long load_J;
   private ChunkPos init_ChunkPos;
   private CompletableFuture<?> init_CompletableFuture;
   private int close;
   private long read_J;
   private LevelChunk cachedChunk;
   volatile boolean init_Z;
   volatile boolean load_Z;
   volatile SchematicPaste.Inner_StreamUtil init_cT_StreamUtil;

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
      this.init_IntegratedServer = server;
      this.init_ServerLevel = level;
      this.init_cQ = document;
      this.init_BlockPos = origin.immutable();
      this.init_I = rotation;
      this.init_Str = mirror;
      this.write_Z = includeAir;
      this.update_Z = includeEntities;
      long top = (long)origin.getY() + document.read_I - 1L;
      if (origin.getY() >= level.getMinY() && top <= level.getMaxY()) {
         int endX = Math.addExact(origin.getX(), document.init_I_I_Str(rotation, mirror) - 1);
         int endZ = Math.addExact(origin.getZ(), document.load_I_I(rotation) - 1);
         if (level.getWorldBorder().isWithinBounds(origin) && level.getWorldBorder().isWithinBounds(new BlockPos(endX, origin.getY(), endZ))) {
            this.sync_Z = !checkpoint.isEmpty();
            this.load_Str = SchematicLibrary.init_Str_JsonObject(checkpoint, "operation", UUID.randomUUID().toString());
            this.load_I = (int)SchematicLibrary.init_J_JsonObject(checkpoint, "phase", 0L);
            this.read_I = (int)SchematicLibrary.init_J_JsonObject(checkpoint, "column", 0L);
            this.write_I = (int)SchematicLibrary.init_J_JsonObject(checkpoint, "range", 0L);
            this.update_I = (int)SchematicLibrary.init_J_JsonObject(checkpoint, "cell", 0L);
            this.sync_I = (int)SchematicLibrary.init_J_JsonObject(checkpoint, "blockEntity", 0L);
            this.flush_I = (int)SchematicLibrary.init_J_JsonObject(checkpoint, "entity", 0L);
            this.load_J = SchematicLibrary.init_J_JsonObject(checkpoint, "done", 0L);
            this.init_J = Math.max(0L, SchematicLibrary.init_J_JsonObject(checkpoint, "total", 0L));
            if (this.load_I >= 0
               && this.load_I <= 4
               && this.read_I >= 0
               && this.write_I >= 0
               && this.update_I >= 0
               && this.sync_I >= 0
               && this.sync_I <= document.load_List.size()
               && this.flush_I >= 0
               && this.flush_I <= document.read_List.size()
               && this.load_J >= 0L) {
               this.init_Future = SchematicLibrary.init_ExecutorService.submit(() -> prepare(document, this.init_BlockPos, rotation, mirror, includeAir));
               this.load("");
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
      this.init_List = prepared.columns();
      this.occupiedMasks = prepared.occupied();
      this.init_J = 2L * prepared.work() + this.init_cQ.load_List.size() + (this.update_Z ? this.init_cQ.read_List.size() : 0);
      if (this.read_I <= this.init_List.size() && this.load_J <= this.init_J) {
         if (this.read_I < this.init_List.size()
            && (
               this.write_I >= this.init_List.get(this.read_I).ranges.size()
                  || this.update_I
                     > 256
                        * (
                           this.init_List.get(this.read_I).ranges.get(this.write_I).max
                              - this.init_List.get(this.read_I).ranges.get(this.write_I).min
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

         if (init_cT != null && !init_cT.load_Z) {
            throw new IllegalStateException("Another paste is still running");
         }

         SchematicPaste job = new SchematicPaste(server, level, document, origin, rotation, mirror, includeAir, includeEntities, checkpoint);
         if (!read_Z) {
            read_Z = true;
            ServerTickEvents.END_SERVER_TICK.register(s -> {
               SchematicPaste current = init_cT;
               if (current != null && current.init_IntegratedServer == s) {
                  current.load();
               }
            });
            ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
               SchematicPaste current = init_cT;
               if (current != null && current.init_IntegratedServer == s) {
                  current.init("");
               }
            });
         }

         init_cT = job;
         return job;
      } else {
         throw new IllegalStateException("Paste is available only in your single-player world");
      }
   }

   void init() {
      this.init_Z = true;
   }

   private void load() {
      if (!this.init_Z && !this.init_cT_StreamUtil.complete && !this.init_IntegratedServer.isPublished()) {
         long deadline = System.nanoTime() + Math.clamp(37000000L - this.init_IntegratedServer.getAverageTickTimeNanos(), 4000000L, 14000000L);
         this.cachedChunk = null;

         try {
            if (this.init_List == null) {
               if (!this.init_Future.isDone()) {
                  return;
               }

               this.applyPrepared(this.init_Future.get());
            }

            for (int n = 0; n < 32768 && ((n & 31) != 0 || System.nanoTime() < deadline) && !this.init_Z; n++) {
               if (this.load_I == 0 || this.load_I == 3) {
                  if (this.read_I >= this.init_List.size()) {
                     this.load_I = this.load_I == 0 ? 1 : 4;
                     this.read_I = this.write_I = this.update_I = 0;
                  } else {
                     SchematicPaste.Inner_DataUtil column = this.init_List.get(this.read_I);
                     SchematicPaste.Inner_CachePool range = column.ranges.get(this.write_I);
                     int rangeSize = (range.max - range.min + 1) * 256;
                     long maskKey = (long)this.read_I << 32 | this.write_I;
                     if (this.update_I < rangeSize) {
                        BitSet mask = this.load_I == 3 && !this.sync_Z ? this.load_Map.get(maskKey) : this.occupiedMasks[this.read_I][this.write_I];
                        int next = mask == null ? -1 : mask.nextSetBit(this.update_I);
                        if (next < 0) {
                           next = rangeSize;
                        }

                        this.load_J = this.load_J + (next - this.update_I);
                        this.update_I = next;
                     }

                     if (this.update_I >= rangeSize) {
                        if (this.load_I == 3) {
                           this.load_Map.remove(maskKey);
                        }

                        this.update_I = 0;
                        if (++this.write_I >= column.ranges.size()) {
                           this.read_I++;
                           this.write_I = 0;
                        }
                     } else {
                        if (!this.init(column.x, column.z)) {
                           break;
                        }

                        int index = this.update_I;
                        int y = range.min + index / 256;
                        int x = (column.x << 4) + (index & 15);
                        int z = (column.z << 4) + (index % 256 >> 4);
                        int tx = x - this.init_BlockPos.getX();
                        int tz = z - this.init_BlockPos.getZ();
                        this.init_V_I(tx, y - this.init_BlockPos.getY(), tz);
                        SchematicDocument.Inner_CachePool region = this.init_cQ
                           .init_cQ_CachePool_I(this.init_BlockPos_MutableBl.getX(), this.init_BlockPos_MutableBl.getY(), this.init_BlockPos_MutableBl.getZ());
                        if (region != null) {
                           BlockState saved = region.init_BlockState(
                              this.init_BlockPos_MutableBl.getX(), this.init_BlockPos_MutableBl.getY(), this.init_BlockPos_MutableBl.getZ()
                           );
                           if (this.write_Z || !saved.isAir()) {
                              this.load_BlockPos_MutableBl.set(x, y, z);
                              if (this.load_I == 0) {
                                 BlockState state = this.init_Map.computeIfAbsent(saved, s -> SchematicDocument.init_BlockState_BlockState(s, this.init_I, this.init_Str));
                                 if (this.cachedChunk.getBlockState(this.load_BlockPos_MutableBl) != state) {
                                    boolean written = this.init_ServerLevel.setBlock(this.load_BlockPos_MutableBl, state, 818);
                                    if (!written && this.cachedChunk.getBlockState(this.load_BlockPos_MutableBl) != state) {
                                       throw new IllegalStateException("Could not paste at " + this.load_BlockPos_MutableBl.toShortString());
                                    }

                                    if (!this.sync_Z) {
                                       this.load_Map.computeIfAbsent(maskKey, key -> new BitSet()).set(index);
                                    }
                                 }
                              } else {
                                 BlockState live = this.cachedChunk.getBlockState(this.load_BlockPos_MutableBl);
                                 this.init_ServerLevel.updateNeighborsAt(this.load_BlockPos_MutableBl, live.getBlock());
                                 if (!live.getFluidState().isEmpty()) {
                                    this.init_ServerLevel.scheduleTick(this.load_BlockPos_MutableBl, live.getFluidState().getType(), 1);
                                 }
                              }
                           }
                        }

                        this.update_I++;
                        this.load_J++;
                     }
                  }
               } else if (this.load_I == 1) {
                  if (this.sync_I >= this.init_cQ.load_List.size()) {
                     this.load_I = 2;
                  } else {
                     SchematicDocument.Inner_DataUtil saved = this.init_cQ.load_List.get(this.sync_I);
                     BlockPos local = this.init_cQ.init(saved.position().getX(), saved.position().getY(), saved.position().getZ(), this.init_I, this.init_Str);
                     BlockPos at = this.init_BlockPos.offset(local);
                     if (!this.init(at.getX() >> 4, at.getZ() >> 4)) {
                        break;
                     }

                     BlockEntity be = this.init_ServerLevel.getBlockEntity(at);
                     if (be != null) {
                        CompoundTag tag = saved.data().copy();
                        tag.putInt("x", at.getX());
                        tag.putInt("y", at.getY());
                        tag.putInt("z", at.getZ());
                        be.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, this.init_ServerLevel.registryAccess(), tag));
                        be.setChanged();
                        this.init_ServerLevel.sendBlockUpdated(at, this.init_ServerLevel.getBlockState(at), this.init_ServerLevel.getBlockState(at), 2);
                     }

                     this.sync_I++;
                     this.load_J++;
                  }
               } else {
                  if (this.load_I != 2) {
                     this.init("");
                     return;
                  }

                  if (this.update_Z && this.flush_I < this.init_cQ.read_List.size()) {
                     SchematicDocument.Inner_BufferHelper saved = this.init_cQ.read_List.get(this.flush_I);
                     double[] at = this.init_DArr_D(saved.x(), saved.y(), saved.z());
                     if (!this.init((int)Math.floor(at[0]) >> 4, (int)Math.floor(at[2]) >> 4)) {
                        break;
                     }

                     UUID uuid = init_UUID_Str(this.load_Str, this.flush_I, 0);
                     if (this.init_ServerLevel.getEntity(uuid) == null) {
                        CompoundTag tag = saved.data().copy();
                        ListTag raw = tag.getListOrEmpty("Pos");
                        double ox = saved.x() - (raw.size() == 3 ? raw.getDoubleOr(0, 0.0) : 0.0);
                        double oy = saved.y() - (raw.size() == 3 ? raw.getDoubleOr(1, 0.0) : 0.0);
                        double oz = saved.z() - (raw.size() == 3 ? raw.getDoubleOr(2, 0.0) : 0.0);
                        int ordinal = this.flush_I;
                        int[] child = new int[]{0};
                        Entity entity = EntityType.loadEntityRecursive(
                           TagValueInput.create(ProblemReporter.DISCARDING, this.init_ServerLevel.registryAccess(), tag),
                           this.init_ServerLevel,
                           EntitySpawnReason.LOAD,
                           ex -> {
                              ex.setUUID(init_UUID_Str(this.load_Str, ordinal, (int)(child[0]++)));
                              double[] p = this.init_DArr_D(ex.getX() + ox, ex.getY() + oy, ex.getZ() + oz);
                              ex.setPos(p[0], p[1], p[2]);
                              if (this.init_Str.equals("x")) {
                                 ex.setYRot(ex.mirror(Mirror.FRONT_BACK));
                              } else if (this.init_Str.equals("z")) {
                                 ex.setYRot(ex.mirror(Mirror.LEFT_RIGHT));
                              }

                              Rotation var10000;
                              switch (this.init_I) {
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
                           throw new IllegalArgumentException("Could not load saved entity " + this.flush_I);
                        }

                        this.init_ServerLevel.addFreshEntityWithPassengers(entity);
                     }

                     this.flush_I++;
                     this.load_J++;
                  } else {
                     this.load_I = 3;
                     this.read_I = this.write_I = this.update_I = 0;
                  }
               }
            }

            this.load("");
         } catch (Exception e) {
            this.init(SchematicLibrary.init_Str_Throwable(e));
         }
      } else {
         this.init("");
      }
   }

   private void init(String error) {
      if (!this.init_Future.isDone()) {
         this.init_Future.cancel(true);
      }

      this.load(error);
      this.load_Z = true;
      if (init_cT == this) {
         init_cT = null;
      }
   }

   private boolean init(int x, int z) {
      long key = ChunkPos.pack(x, z);
      if (this.cachedChunk != null && this.read_J == key) {
         return true;
      }

      ChunkPos pos = new ChunkPos(x, z);
      ServerChunkCache source = this.init_ServerLevel.getChunkSource();
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
               () -> source.getChunkFuture(x, z, ChunkStatus.FULL, true), SchematicLibrary.init_ExecutorService
            )
            .thenCompose(future -> (CompletionStage<ChunkResult<ChunkAccess>>)future);
         this.close = this.init_IntegratedServer.getTickCount();
      }

      if (this.init_IntegratedServer.getTickCount() - this.close > 1200) {
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
      switch (this.init_I) {
         case 90:
            sx = z;
            sz = this.init_cQ.write_I - 1 - x;
            break;
         case 180:
            sx = this.init_cQ.load_I - 1 - x;
            sz = this.init_cQ.write_I - 1 - z;
            break;
         case 270:
            sx = this.init_cQ.load_I - 1 - z;
            sz = x;
            break;
         default:
            sx = x;
            sz = z;
      }

      if (this.init_Str.equals("x")) {
         sx = this.init_cQ.load_I - 1 - sx;
      } else if (this.init_Str.equals("z")) {
         sz = this.init_cQ.write_I - 1 - sz;
      }

      this.init_BlockPos_MutableBl.set(sx, y, sz);
   }

   private double[] init_DArr_D(double x, double y, double z) {
      if (this.init_Str.equals("x")) {
         x = this.init_cQ.load_I - x;
      } else if (this.init_Str.equals("z")) {
         z = this.init_cQ.write_I - z;
      }

      double tx;
      double tz;
      switch (this.init_I) {
         case 90:
            tx = this.init_cQ.write_I - z;
            tz = x;
            break;
         case 180:
            tx = this.init_cQ.load_I - x;
            tz = this.init_cQ.write_I - z;
            break;
         case 270:
            tx = z;
            tz = this.init_cQ.load_I - x;
            break;
         default:
            tx = x;
            tz = z;
      }

      return new double[]{this.init_BlockPos.getX() + tx, this.init_BlockPos.getY() + y, this.init_BlockPos.getZ() + tz};
   }

   private void load(String error) {
      JsonObject cursor = AiJson.objectOf(
         "operation",
         this.load_Str,
         "phase",
         this.load_I,
         "column",
         this.read_I,
         "range",
         this.write_I,
         "cell",
         this.update_I,
         "blockEntity",
         this.sync_I,
         "entity",
         this.flush_I,
         "done",
         this.load_J,
         "total",
         this.init_J,
         "includeAir",
         this.write_Z,
         "includeEntities",
         this.update_Z
      );
      String var10000;
      if (this.init_List == null) {
         var10000 = "Preparing paste";
      } else {
         switch (this.load_I) {
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
      this.init_cT_StreamUtil = new SchematicPaste.Inner_StreamUtil(
         status,
         this.load_I == 4 ? 100 : (this.init_J == 0L ? 0 : (int)Math.min(99L, 100L * this.load_J / this.init_J)),
         this.load_J,
         this.init_J,
         cursor,
         this.load_I == 4,
         error
      );
   }

   static UUID init_UUID_Str(String operation, int index, int child) {
      return UUID.nameUUIDFromBytes((operation + ":" + index + ":" + child).getBytes(StandardCharsets.UTF_8));
   }

   static SchematicPaste.Inner_TableCache prepare(SchematicDocument doc, BlockPos origin, int rotation, String mirror, boolean includeAir) {
      List<SchematicPaste.Inner_DataUtil> columns = init(doc, origin, rotation, mirror);
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
      boolean multiRegion = doc.init_List.size() > 1;

      for (SchematicDocument.Inner_CachePool r : doc.init_List) {
         SchematicDocument.init();
         if (includeAir || !Arrays.stream(r.init_BlockStateArr).allMatch(BlockStateBase::isAir)) {
            int cellIndex = 0;

            for (int sy = r.load; sy < r.load + r.update; sy++) {
               int wy = sy + origin.getY();

               for (int sz = r.read; sz < r.read + r.sync; sz++) {
                  int wx = origin.getX() + p0.getX() + dx.getX() * r.init_I + dz.getX() * sz;
                  int wz = origin.getZ() + p0.getZ() + dx.getZ() * r.init_I + dz.getZ() * sz;
                  int lastCol = -1;
                  int rangeIdx = 0;
                  int yOffset = 0;
                  BitSet bits = null;

                  for (int sx = r.init_I; sx < r.init_I + r.write; wz += dx.getZ()) {
                     int idx = cellIndex++;
                     if ((idx & 4095) == 0) {
                        SchematicDocument.init();
                     }

                     if (includeAir || !r.init_BlockState(idx).isAir() && (!multiRegion || doc.init_cQ_CachePool_I(sx, sy, sz) == r)) {
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

   static List<SchematicPaste.Inner_DataUtil> init(SchematicDocument doc, BlockPos origin, int rotation, String mirror) {
      TreeMap<Long, List<SchematicPaste.Inner_CachePool>> ranges = new TreeMap<>();

      for (SchematicDocument.Inner_CachePool r : doc.init_List) {
         BlockPos a = doc.init(r.init_I, r.load, r.read, rotation, mirror);
         BlockPos b = doc.init(r.init_I + r.write - 1, r.load, r.read + r.sync - 1, rotation, mirror);
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
