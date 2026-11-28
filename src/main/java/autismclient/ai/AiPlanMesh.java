package autismclient.ai;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiPlanMesh implements AutoCloseable {
   private static final int open = 6144;
   private static final int poll = 4096;
   private static final long init_J = 2000000L;
   private static final Map<BlockState, autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil> MODEL_CACHE = new IdentityHashMap<>();
   private static Object modelCacheSet;
   BlockPos origin;
   int sizeX;
   int sizeY;
   int sizeZ;
   private boolean moving;
   final List<AiBuilder.CellSpec> cells;
   private final boolean preview;
   private final Predicate<BlockPos> hiddenSolid;
   private final BlockState[] states;
   private final boolean[] solid;
   private final int[] colors;
   private final long[] colorRevisions;
   private long colorRevision = 1L;
   private final Map<Long, Integer> cellIndexByPos;
   private final List<AiPlanMesh.Inner_BufferHelper> sections = new ArrayList<>();
   private int scanCursor;
   private int lastAlpha = -1;
   int write_I;
   int update;
   int sync;
   int flush;
   int close;
   private boolean closed;
   private AiPlanMesh.Inner_CachePool pendingBuild;
   private long queryRevision;
   private AiPlanMesh.Inner_StreamUtil nearestQuery;
   private List<Integer> nearestCellsSnapshot = List.of();
   private Vec3 nearestEye;
   private long nearestRevision = -1L;
   private int nearestLimit;

   AiPlanMesh(BlockPos origin, List<AiBuilder.CellSpec> cells, boolean preview) {
      this(origin, cells, preview, null);
   }

   AiPlanMesh(BlockPos origin, List<AiBuilder.CellSpec> cells, boolean preview, Predicate<BlockPos> hiddenSolid) {
      this.origin = origin.immutable();
      this.cells = cells;
      this.preview = preview;
      this.hiddenSolid = hiddenSolid;
      int n = cells.size();
      this.states = new BlockState[n];
      this.solid = new boolean[n];
      this.colors = new int[n];
      this.colorRevisions = new long[n];
      this.update = n;
      this.cellIndexByPos = new AiPrintSupply.Inner_ConfigStore<>();
      this.pendingBuild = new AiPlanMesh.Inner_CachePool();
   }

   boolean isReady() {
      return !this.closed && this.pendingBuild == null;
   }

   int preparedCount() {
      return this.pendingBuild == null ? this.cells.size() : this.pendingBuild.nextCell;
   }

   int buildStep(int budget, long deadline) {
      int used;
      for (used = 0; !this.closed && this.pendingBuild != null && used < budget && (used <= 0 || System.nanoTime() < deadline); used++) {
         this.pendingBuild.step();
      }

      return used;
   }

   private void buildSlice() {
      this.buildStep(4096, System.nanoTime() + 2000000L);
   }

   int memberCount() {
      int count = 0;

      for (AiPlanMesh.Inner_BufferHelper section : this.sections) {
         count += section.members.size();
      }

      return count;
   }

   boolean init(BlockPos origin, List<AiBuilder.CellSpec> cells, boolean preview) {
      return !this.closed && this.cells == cells && (preview || this.origin.equals(origin)) && this.preview == preview;
   }

   void setOrigin(BlockPos origin) {
      if (!this.origin.equals(origin)) {
         this.origin = origin.immutable();
         this.invalidateNearest();
      }
   }

   void setMoving(boolean value) {
      if (this.moving != value) {
         this.moving = value;
         this.scanCursor = 0;
         this.invalidateNearest();
         if (value) {
            this.colorRevision++;
            this.write_I = this.sync = 0;
            this.update = this.colors.length;
         }
      }
   }

   int read() {
      return this.cells.size();
   }

   BlockState stateAt(int i) {
      return this.states[i];
   }

   BlockPos posAt(int i) {
      AiBuilder.CellSpec cell = this.cells.get(i);
      return this.origin.offset(cell.dx(), cell.dy(), cell.dz());
   }

   int colorAt(int i) {
      return this.colorRevisions[i] == this.colorRevision ? this.colors[i] : -11093505;
   }

   void setColor(int i, int value) {
      int previous = this.colorAt(i);
      if (previous == -12067963 != (value == -12067963)) {
         this.queryRevision++;
      }

      if (previous == -12067963) {
         this.write_I--;
      } else if (previous == -11093505) {
         this.update--;
      } else if (previous == -20923) {
         this.sync--;
      }

      this.colors[i] = value;
      this.colorRevisions[i] = this.colorRevision;
      if (value == -12067963) {
         this.write_I++;
      } else if (value == -11093505) {
         this.update++;
      } else if (value == -20923) {
         this.sync++;
      }
   }

   private void invalidateNearest() {
      this.queryRevision++;
      this.nearestQuery = null;
      this.nearestCellsSnapshot = List.of();
      this.nearestEye = null;
   }

   List<Integer> init(Vec3 eye, int limit, int budget, long deadline) {
      if (this.isReady() && limit > 0) {
         if (this.nearestQuery != null && (this.nearestQuery.revision != this.queryRevision || this.nearestQuery.limit != limit)) {
            this.nearestQuery = null;
         }

         if (this.nearestQuery == null) {
            if (this.nearestEye != null && this.nearestEye.equals(eye) && this.nearestRevision == this.queryRevision && this.nearestLimit == limit) {
               return this.nearestCellsSnapshot;
            }

            this.nearestQuery = new AiPlanMesh.Inner_StreamUtil(eye, limit);
         }

         this.nearestQuery.advance(budget, deadline);
         if (this.nearestQuery.finished) {
            this.nearestCellsSnapshot = List.copyOf(this.nearestQuery.nearestCells);
            this.nearestEye = this.nearestQuery.eye;
            this.nearestRevision = this.nearestQuery.revision;
            this.nearestLimit = limit;
            this.nearestQuery = null;
         }

         return this.nearestCellsSnapshot;
      } else {
         return List.of();
      }
   }

   boolean isQuerying() {
      return this.nearestQuery != null;
   }

   void refreshFromWorld(ClientLevel world) {
      this.buildSlice();
      if (this.isReady() && !this.moving) {
         int n = this.states.length;
         if (n != 0) {
            int budget = Math.min(n, 6144);
            HashSet<Long> dirtySections = new HashSet<>();

            for (int k = 0; k < budget; k++) {
               int i = this.scanCursor;
               this.scanCursor = (this.scanCursor + 1) % n;
               AiBuilder.CellSpec cell = this.cells.get(i);
               BlockPos at = this.origin.offset(cell.dx(), cell.dy(), cell.dz());
               boolean loaded = AiTerrain.isLoaded(world, at);
               BlockState live = loaded ? world.getBlockState(at) : Blocks.AIR.defaultBlockState();
               boolean satisfied = loaded && AiBuilder.matchesSchematicCell(cell, live);
               int color = AiVisualization.init(satisfied, live.canBeReplaced(), loaded);
               if (loaded
                  && !satisfied
                  && !live.isAir()
                  && (live.hasBlockEntity() || live.getDestroySpeed(world, at) < 0.0F || !live.getFluidState().isEmpty() || !AiSchematicActions.init(live))) {
                  color = -885830;
               }

               if (this.colorAt(i) != color) {
                  this.setColor(i, color);
                  BlockPos local = at.subtract(this.origin);
                  dirtySections.add(BlockPos.asLong(local.getX() >> 4, local.getY() >> 4, local.getZ() >> 4));

                  for (Direction d : Direction.values()) {
                     BlockPos next = local.relative(d);
                     dirtySections.add(BlockPos.asLong(next.getX() >> 4, next.getY() >> 4, next.getZ() >> 4));
                  }
               }
            }

            if (!dirtySections.isEmpty()) {
               for (AiPlanMesh.Inner_BufferHelper section : this.sections) {
                  if (dirtySections.contains(
                     BlockPos.asLong(section.anchor.getX() >> 4, section.anchor.getY() >> 4, section.anchor.getZ() >> 4)
                  )) {
                     section.dirty = true;
                  }
               }
            }
         }
      }
   }

   void render(Pose pose, Vec3 camera, int alpha, double range, long bakeDeadline, ClientLevel world) {
      if (!this.closed) {
         this.buildSlice();
         if (this.isReady()) {
            if (alpha != this.lastAlpha) {
               for (AiPlanMesh.Inner_BufferHelper section : this.sections) {
                  section.dirty = true;
               }

               this.lastAlpha = alpha;
            }

            double reach = range + 28.0;
            ArrayList<AiPlanMesh.Inner_BufferHelper> visible = new ArrayList<>();

            for (AiPlanMesh.Inner_BufferHelper section : this.sections) {
               double dx = this.origin.getX() + section.anchor.getX() + 8 - camera.x;
               double dy = this.origin.getY() + section.anchor.getY() + 8 - camera.y;
               double dz = this.origin.getZ() + section.anchor.getZ() + 8 - camera.z;
               if (dx * dx + dy * dy + dz * dz <= reach * reach) {
                  visible.add(section);
               }
            }

            visible.sort(Comparator.comparingDouble(s -> {
               double dx = this.origin.getX() + s.anchor.getX() + 8 - camera.x;
               double dyx = this.origin.getY() + s.anchor.getY() + 8 - camera.y;
               double dzx = this.origin.getZ() + s.anchor.getZ() + 8 - camera.z;
               return dx * dx + dyx * dyx + dzx * dzx;
            }));
            this.flush = this.close = 0;
            RenderType type = autismclient.render.AutismRenderTypes.aiPlanGhost();

            for (AiPlanMesh.Inner_BufferHelper section : visible) {
               if (section.colorRevision != this.colorRevision) {
                  section.dirty = true;
               }

               if (section.dirty && System.nanoTime() < bakeDeadline) {
                  this.rebuildSection(section, alpha, world);
               }

               if (section.meshReady) {
                  section.mesh
                     .init(
                        type,
                        pose.pose(),
                        this.origin.getX() + section.anchor.getX() - camera.x,
                        this.origin.getY() + section.anchor.getY() - camera.y,
                        this.origin.getZ() + section.anchor.getZ() - camera.z
                     );
                  this.flush = this.flush + section.drawnCount;
                  this.close = this.close + section.quadCount;
               }
            }
         }
      }
   }

   private void rebuildSection(AiPlanMesh.Inner_BufferHelper section, int alpha, ClientLevel world) {
      section.dirty = false;
      section.colorRevision = this.colorRevision;
      Minecraft mc = Minecraft.getInstance();
      int[] quads = new int[]{0};
      int[] drawn = new int[]{0};
      Pose identity = new PoseStack().last();
      section.meshReady = section.mesh
         .init(
            autismclient.render.AutismRenderTypes.aiPlanGhost(),
            buffer -> {
               label91:
               for (int i : section.members) {
                  int classification = this.colorAt(i);
                  if (classification != -12067963) {
                     BlockState state = this.states[i];
                     if (!state.isAir()) {
                        autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil model = ghostModel(state);
                        if (!model.faces().isEmpty()) {
                           BlockPos at = this.posAt(i);
                           float ox = at.getX() - this.origin.getX() - section.anchor.getX();
                           float oy = at.getY() - this.origin.getY() - section.anchor.getY();
                           float oz = at.getZ() - this.origin.getZ() - section.anchor.getZ();
                           boolean proud = classification == -20923 || classification == -885830;
                           drawn[0]++;
                           Iterator i$ = model.faces().iterator();

                           while (true) {
                              autismclient.util.oresim.OreGhostModelCache.Inner_DataUtil face;
                              Direction cull;
                              Integer next;
                              do {
                                 if (!i$.hasNext()) {
                                    continue label91;
                                 }

                                 face = (autismclient.util.oresim.OreGhostModelCache.Inner_DataUtil)i$.next();
                                 cull = face.cullFace();
                                 if (cull == null) {
                                    break;
                                 }

                                 next = this.cellIndexByPos.get(at.relative(cull).subtract(this.origin).asLong());
                              } while (
                                 next != null && this.solid[next]
                                    || next == null && this.hiddenSolid != null && this.hiddenSolid.test(at.relative(cull).subtract(this.origin))
                              );

                              BlockTintSource tintSource = face.tintIndex() < 0 ? null : mc.getBlockColors().getTintSource(state, face.tintIndex());
                              int tint = tintSource == null ? 16777215 : tintSource.colorInWorld(state, world, at);
                              float shade = face.load() == null ? 1.0F : world.cardinalLighting().byFace(face.load());
                              int color = AiVisualization.ghostColor(classification, tint, shade, alpha);
                              float[] data = face.vertices();

                              for (int v = 0; v < 4; v++) {
                                 int k = v * 5;
                                 float x = data[k];
                                 float y = data[k + 1];
                                 float z = data[k + 2];
                                 if (proud) {
                                    x = (x - 0.5F) * 1.004F + 0.5F;
                                    y = (y - 0.5F) * 1.004F + 0.5F;
                                    z = (z - 0.5F) * 1.004F + 0.5F;
                                 }

                                 buffer.addVertex(identity, ox + x, oy + y, oz + z).setUv(data[k + 3], data[k + 4]).setColor(color);
                              }

                              quads[0]++;
                           }
                        }
                     }
                  }
               }
            }
         );
      section.quadCount = quads[0];
      section.drawnCount = drawn[0];
   }

   static autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil ghostModel(BlockState state) {
      autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil model = autismclient.util.oresim.OreGhostModelCache.init(state);
      if (!model.faces().isEmpty()) {
         return model;
      }

      Object set = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
      if (set != modelCacheSet) {
         MODEL_CACHE.clear();
         modelCacheSet = set;
      }

      return MODEL_CACHE.computeIfAbsent(state, AiPlanMesh::buildGhostModel);
   }

   private static autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil buildGhostModel(BlockState state) {
      List<AABB> boxes = new ArrayList<>(state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).toAabbs());
      if (!state.getFluidState().isEmpty() && boxes.isEmpty()) {
         float height = state.getFluidState().getOwnHeight();
         boxes.add(new AABB(0.0, 0.0, 0.0, 1.0, Math.max(0.125, height), 1.0));
      }

      if (boxes.isEmpty()) {
         boxes.add(new AABB(0.25, 0.0, 0.25, 0.75, 0.5, 0.75));
      }

      TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
      ArrayList<autismclient.util.oresim.OreGhostModelCache.Inner_DataUtil> faces = new ArrayList<>();

      for (AABB box : boxes) {
         for (Direction d : Direction.values()) {
            faces.add(
               new autismclient.util.oresim.OreGhostModelCache.Inner_DataUtil(
                  null, d, faceQuad(box, d, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1()), -1
               )
            );
         }
      }

      return new autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil(List.copyOf(faces));
   }

   private static float[] faceQuad(AABB b, Direction d, float u0, float v0, float u1, float v1) {
      float x0 = (float)b.minX;
      float y0 = (float)b.minY;
      float z0 = (float)b.minZ;
      float x1 = (float)b.maxX;
      float y1 = (float)b.maxY;
      float z1 = (float)b.maxZ;
      float[][] var10000;
      switch (d) {
         case DOWN:
            var10000 = new float[][]{{x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1}, {x0, y0, z1}};
            break;
         case UP:
            var10000 = new float[][]{{x0, y1, z1}, {x1, y1, z1}, {x1, y1, z0}, {x0, y1, z0}};
            break;
         case NORTH:
            var10000 = new float[][]{{x1, y0, z0}, {x0, y0, z0}, {x0, y1, z0}, {x1, y1, z0}};
            break;
         case SOUTH:
            var10000 = new float[][]{{x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
            break;
         case WEST:
            var10000 = new float[][]{{x0, y0, z0}, {x0, y0, z1}, {x0, y1, z1}, {x0, y1, z0}};
            break;
         case EAST:
            var10000 = new float[][]{{x1, y0, z1}, {x1, y0, z0}, {x1, y1, z0}, {x1, y1, z1}};
            break;
         default:
            throw new MatchException(null, null);
      }

      float[][] corners = var10000;
      float[] out = new float[20];

      for (int i = 0; i < 4; i++) {
         float[] c = corners[i];
         float s = d.getAxis() == Axis.X ? c[2] : c[0];
         float t = d.getAxis() == Axis.Y ? c[2] : 1.0F - c[1];
         out[i * 5] = c[0];
         out[i * 5 + 1] = c[1];
         out[i * 5 + 2] = c[2];
         out[i * 5 + 3] = u0 + (u1 - u0) * s;
         out[i * 5 + 4] = v0 + (v1 - v0) * t;
      }

      return out;
   }

   @Override
   public void close() {
      this.closed = true;
      this.pendingBuild = null;
      this.nearestQuery = null;
      this.nearestCellsSnapshot = List.of();

      for (AiPlanMesh.Inner_BufferHelper section : this.sections) {
         section.mesh.release();
      }
   }

   private static final class Inner_BufferHelper {
      final BlockPos anchor;
      final IntArrayList members;
      final autismclient.util.CachedGpuMeshBuffer mesh = new autismclient.util.CachedGpuMeshBuffer("autism_ai_plan_section");
      boolean dirty = true;
      boolean meshReady;
      int quadCount;
      int drawnCount;
      long colorRevision;

      Inner_BufferHelper(BlockPos anchor, IntArrayList members) {
         this.anchor = anchor;
         this.members = members;
      }
   }

   private final class Inner_CachePool {
      final Map<AiBuilder.CellSpec, BlockState> stateByCell = new HashMap<>();
      final TreeMap<Long, IntArrayList> sectionMembers = new TreeMap<>();
      Iterator<Entry<Long, IntArrayList>> sectionIterator;
      int nextCell;

      void step() {
         if (this.nextCell < AiPlanMesh.this.cells.size()) {
            int i = this.nextCell++;
            AiBuilder.CellSpec cell = AiPlanMesh.this.cells.get(i);
            AiBuilder.CellSpec key = new AiBuilder.CellSpec(0, 0, 0, cell.block(), cell.properties());
            AiPlanMesh.this.states[i] = this.stateByCell.computeIfAbsent(key, AiBuilder::toBlockState);
            AiPlanMesh.this.solid[i] = AiPlanMesh.this.states[i].isSolidRender();
            BlockPos at = new BlockPos(cell.dx(), cell.dy(), cell.dz());
            AiPlanMesh.this.sizeX = Math.max(AiPlanMesh.this.sizeX, cell.dx() + 1);
            AiPlanMesh.this.sizeY = Math.max(AiPlanMesh.this.sizeY, cell.dy() + 1);
            AiPlanMesh.this.sizeZ = Math.max(AiPlanMesh.this.sizeZ, cell.dz() + 1);
            AiPlanMesh.this.cellIndexByPos.put(at.asLong(), i);
            this.sectionMembers.computeIfAbsent(BlockPos.asLong(at.getX() >> 4, at.getY() >> 4, at.getZ() >> 4), ignored -> new IntArrayList()).add(i);
         } else {
            if (this.sectionIterator == null) {
               this.sectionIterator = this.sectionMembers.entrySet().iterator();
            }

            if (!this.sectionIterator.hasNext()) {
               AiPlanMesh.this.pendingBuild = null;
               return;
            }

            Entry<Long, IntArrayList> entry = this.sectionIterator.next();
            BlockPos key = BlockPos.of(entry.getKey());
            AiPlanMesh.this.sections.add(new AiPlanMesh.Inner_BufferHelper(new BlockPos(key.getX() << 4, key.getY() << 4, key.getZ() << 4), entry.getValue()));
         }
      }
   }

   private record Inner_DataUtil(AiPlanMesh.Inner_BufferHelper section, double distance) {
   }

   private final class Inner_StreamUtil {
      final Vec3 eye;
      final int limit;
      final long revision = AiPlanMesh.this.queryRevision;
      final PriorityQueue<AiPlanMesh.Inner_DataUtil> sectionQueue = new PriorityQueue<>(Comparator.comparingDouble(AiPlanMesh.Inner_DataUtil::distance));
      final PriorityQueue<Integer> nearestCells;
      int nextSection;
      int memberIndex;
      AiPlanMesh.Inner_BufferHelper currentSection;
      boolean finished;

      Inner_StreamUtil(Vec3 eye, int limit) {
         this.eye = eye;
         this.limit = limit;
         this.nearestCells = new PriorityQueue<>(Comparator.<Integer>comparingDouble(i -> this.cellDistance(i)).thenComparingInt(Integer::intValue).reversed());
      }

      double cellDistance(int i) {
         AiBuilder.CellSpec cell = AiPlanMesh.this.cells.get(i);
         double dx = AiPlanMesh.this.origin.getX() + cell.dx() + 0.5 - this.eye.x;
         double dy = AiPlanMesh.this.origin.getY() + cell.dy() + 0.5 - this.eye.y;
         double dz = AiPlanMesh.this.origin.getZ() + cell.dz() + 0.5 - this.eye.z;
         return dx * dx + dy * dy + dz * dz;
      }

      double sectionDistance(AiPlanMesh.Inner_BufferHelper section) {
         double dx = this.axisGap(this.eye.x, AiPlanMesh.this.origin.getX() + section.anchor.getX() + 0.5);
         double dy = this.axisGap(this.eye.y, AiPlanMesh.this.origin.getY() + section.anchor.getY() + 0.5);
         double dz = this.axisGap(this.eye.z, AiPlanMesh.this.origin.getZ() + section.anchor.getZ() + 0.5);
         return dx * dx + dy * dy + dz * dz;
      }

      double axisGap(double coordinate, double minimum) {
         return coordinate < minimum ? minimum - coordinate : (coordinate > minimum + 15.0 ? coordinate - minimum - 15.0 : 0.0);
      }

      void advance(int budget, long deadline) {
         for (int used = 0; used < budget && !this.finished; used++) {
            if (used > 0 && System.nanoTime() >= deadline) {
               return;
            }

            if (this.nextSection < AiPlanMesh.this.sections.size()) {
               AiPlanMesh.Inner_BufferHelper section = AiPlanMesh.this.sections.get(this.nextSection++);
               this.sectionQueue.add(new AiPlanMesh.Inner_DataUtil(section, this.sectionDistance(section)));
            } else if (this.currentSection != null && this.memberIndex < this.currentSection.members.size()) {
               int i = this.currentSection.members.getInt(this.memberIndex++);
               if (AiPlanMesh.this.colorAt(i) != -12067963) {
                  this.nearestCells.add(i);
                  if (this.nearestCells.size() > this.limit) {
                     this.nearestCells.poll();
                  }
               }
            } else if (this.sectionQueue.isEmpty()
               || this.nearestCells.size() == this.limit && this.sectionQueue.peek().distance() > this.cellDistance(this.nearestCells.peek())) {
               this.finished = true;
            } else {
               this.currentSection = this.sectionQueue.remove().section();
               this.memberIndex = 0;
            }
         }
      }
   }
}
