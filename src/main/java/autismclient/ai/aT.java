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

final class aT implements AutoCloseable {
   private static final int open = 6144;
   private static final int poll = 4096;
   private static final long init_J = 2000000L;
   private static final Map<BlockState, autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil> init_Map = new IdentityHashMap<>();
   private static Object init_Obj;
   BlockPos init_BlockPos;
   int init_I;
   int load_I;
   int read_I;
   private boolean init_Z;
   final List<TableIndex.Inner_StreamUtil> init_List;
   private final boolean load_Z;
   private final Predicate<BlockPos> init_Predicate;
   private final BlockState[] init_BlockStateArr;
   private final boolean[] init_ZArr;
   private final int[] init_IArr;
   private final long[] init_JArr;
   private long load_J = 1L;
   private final Map<Long, Integer> load_Map;
   private final List<aT.Inner_BufferHelper> load_List = new ArrayList<>();
   private int push;
   private int pop = -1;
   int write_I;
   int update;
   int sync;
   int flush;
   int close;
   private boolean read_Z;
   private aT.Inner_CachePool init_aT_CachePool;
   private long read_J;
   private aT.Inner_StreamUtil init_aT_StreamUtil;
   private List<Integer> read_List = List.of();
   private Vec3 init_Vec3;
   private long write_J = -1L;
   private int tick;

   aT(BlockPos origin, List<TableIndex.Inner_StreamUtil> cells, boolean preview) {
      this(origin, cells, preview, null);
   }

   aT(BlockPos origin, List<TableIndex.Inner_StreamUtil> cells, boolean preview, Predicate<BlockPos> hiddenSolid) {
      this.init_BlockPos = origin.immutable();
      this.init_List = cells;
      this.load_Z = preview;
      this.init_Predicate = hiddenSolid;
      int n = cells.size();
      this.init_BlockStateArr = new BlockState[n];
      this.init_ZArr = new boolean[n];
      this.init_IArr = new int[n];
      this.init_JArr = new long[n];
      this.update = n;
      this.load_Map = new bj.Inner_ConfigStore<>();
      this.init_aT_CachePool = new aT.Inner_CachePool();
   }

   boolean init_Z() {
      return !this.read_Z && this.init_aT_CachePool == null;
   }

   int init_I() {
      return this.init_aT_CachePool == null ? this.init_List.size() : this.init_aT_CachePool.init_I;
   }

   int init_I_I_J(int budget, long deadline) {
      int used;
      for (used = 0; !this.read_Z && this.init_aT_CachePool != null && used < budget && (used <= 0 || System.nanoTime() < deadline); used++) {
         this.init_aT_CachePool.init();
      }

      return used;
   }

   private void init_V() {
      this.init_I_I_J(4096, System.nanoTime() + 2000000L);
   }

   int load_I() {
      int count = 0;

      for (aT.Inner_BufferHelper section : this.load_List) {
         count += section.init_IntArrayList.size();
      }

      return count;
   }

   boolean init(BlockPos origin, List<TableIndex.Inner_StreamUtil> cells, boolean preview) {
      return !this.read_Z && this.init_List == cells && (preview || this.init_BlockPos.equals(origin)) && this.load_Z == preview;
   }

   void init_V_BlockPos(BlockPos origin) {
      if (!this.init_BlockPos.equals(origin)) {
         this.init_BlockPos = origin.immutable();
         this.load_V();
      }
   }

   void init_V_Z(boolean value) {
      if (this.init_Z != value) {
         this.init_Z = value;
         this.push = 0;
         this.load_V();
         if (value) {
            this.load_J++;
            this.write_I = this.sync = 0;
            this.update = this.init_IArr.length;
         }
      }
   }

   int read() {
      return this.init_List.size();
   }

   BlockState init_BlockState_I(int i) {
      return this.init_BlockStateArr[i];
   }

   BlockPos init_BlockPos_I(int i) {
      TableIndex.Inner_StreamUtil cell = this.init_List.get(i);
      return this.init_BlockPos.offset(cell.dx(), cell.dy(), cell.dz());
   }

   int init_I_I(int i) {
      return this.init_JArr[i] == this.load_J ? this.init_IArr[i] : -11093505;
   }

   void init_V_I_I(int i, int value) {
      int previous = this.init_I_I(i);
      if (previous == -12067963 != (value == -12067963)) {
         this.read_J++;
      }

      if (previous == -12067963) {
         this.write_I--;
      } else if (previous == -11093505) {
         this.update--;
      } else if (previous == -20923) {
         this.sync--;
      }

      this.init_IArr[i] = value;
      this.init_JArr[i] = this.load_J;
      if (value == -12067963) {
         this.write_I++;
      } else if (value == -11093505) {
         this.update++;
      } else if (value == -20923) {
         this.sync++;
      }
   }

   private void load_V() {
      this.read_J++;
      this.init_aT_StreamUtil = null;
      this.read_List = List.of();
      this.init_Vec3 = null;
   }

   List<Integer> init(Vec3 eye, int limit, int budget, long deadline) {
      if (this.init_Z() && limit > 0) {
         if (this.init_aT_StreamUtil != null && (this.init_aT_StreamUtil.init_J != this.read_J || this.init_aT_StreamUtil.init_I != limit)) {
            this.init_aT_StreamUtil = null;
         }

         if (this.init_aT_StreamUtil == null) {
            if (this.init_Vec3 != null && this.init_Vec3.equals(eye) && this.write_J == this.read_J && this.tick == limit) {
               return this.read_List;
            }

            this.init_aT_StreamUtil = new aT.Inner_StreamUtil(eye, limit);
         }

         this.init_aT_StreamUtil.init_V_I_J(budget, deadline);
         if (this.init_aT_StreamUtil.init_Z) {
            this.read_List = List.copyOf(this.init_aT_StreamUtil.load_PriorityQueue);
            this.init_Vec3 = this.init_aT_StreamUtil.init_Vec3;
            this.write_J = this.init_aT_StreamUtil.init_J;
            this.tick = limit;
            this.init_aT_StreamUtil = null;
         }

         return this.read_List;
      } else {
         return List.of();
      }
   }

   boolean load_Z() {
      return this.init_aT_StreamUtil != null;
   }

   void init_V_ClientLevel(ClientLevel world) {
      this.init_V();
      if (this.init_Z() && !this.init_Z) {
         int n = this.init_BlockStateArr.length;
         if (n != 0) {
            int budget = Math.min(n, 6144);
            HashSet<Long> dirtySections = new HashSet<>();

            for (int k = 0; k < budget; k++) {
               int i = this.push;
               this.push = (this.push + 1) % n;
               TableIndex.Inner_StreamUtil cell = this.init_List.get(i);
               BlockPos at = this.init_BlockPos.offset(cell.dx(), cell.dy(), cell.dz());
               boolean loaded = cu.init(world, at);
               BlockState live = loaded ? world.getBlockState(at) : Blocks.AIR.defaultBlockState();
               boolean satisfied = loaded && TableIndex.update(cell, live);
               int color = cG.init(satisfied, live.canBeReplaced(), loaded);
               if (loaded
                  && !satisfied
                  && !live.isAir()
                  && (live.hasBlockEntity() || live.getDestroySpeed(world, at) < 0.0F || !live.getFluidState().isEmpty() || !bQ.init(live))) {
                  color = -885830;
               }

               if (this.init_I_I(i) != color) {
                  this.init_V_I_I(i, color);
                  BlockPos local = at.subtract(this.init_BlockPos);
                  dirtySections.add(BlockPos.asLong(local.getX() >> 4, local.getY() >> 4, local.getZ() >> 4));

                  for (Direction d : Direction.values()) {
                     BlockPos next = local.relative(d);
                     dirtySections.add(BlockPos.asLong(next.getX() >> 4, next.getY() >> 4, next.getZ() >> 4));
                  }
               }
            }

            if (!dirtySections.isEmpty()) {
               for (aT.Inner_BufferHelper section : this.load_List) {
                  if (dirtySections.contains(
                     BlockPos.asLong(section.init_BlockPos.getX() >> 4, section.init_BlockPos.getY() >> 4, section.init_BlockPos.getZ() >> 4)
                  )) {
                     section.init_Z = true;
                  }
               }
            }
         }
      }
   }

   void init_V_PoseStack_Pose(Pose pose, Vec3 camera, int alpha, double range, long bakeDeadline, ClientLevel world) {
      if (!this.read_Z) {
         this.init_V();
         if (this.init_Z()) {
            if (alpha != this.pop) {
               for (aT.Inner_BufferHelper section : this.load_List) {
                  section.init_Z = true;
               }

               this.pop = alpha;
            }

            double reach = range + 28.0;
            ArrayList<aT.Inner_BufferHelper> visible = new ArrayList<>();

            for (aT.Inner_BufferHelper section : this.load_List) {
               double dx = this.init_BlockPos.getX() + section.init_BlockPos.getX() + 8 - camera.x;
               double dy = this.init_BlockPos.getY() + section.init_BlockPos.getY() + 8 - camera.y;
               double dz = this.init_BlockPos.getZ() + section.init_BlockPos.getZ() + 8 - camera.z;
               if (dx * dx + dy * dy + dz * dz <= reach * reach) {
                  visible.add(section);
               }
            }

            visible.sort(Comparator.comparingDouble(s -> {
               double dx = this.init_BlockPos.getX() + s.init_BlockPos.getX() + 8 - camera.x;
               double dyx = this.init_BlockPos.getY() + s.init_BlockPos.getY() + 8 - camera.y;
               double dzx = this.init_BlockPos.getZ() + s.init_BlockPos.getZ() + 8 - camera.z;
               return dx * dx + dyx * dyx + dzx * dzx;
            }));
            this.flush = this.close = 0;
            RenderType type = autismclient.render.AutismRenderTypes.aiPlanGhost();

            for (aT.Inner_BufferHelper section : visible) {
               if (section.init_J != this.load_J) {
                  section.init_Z = true;
               }

               if (section.init_Z && System.nanoTime() < bakeDeadline) {
                  this.init(section, alpha, world);
               }

               if (section.load_Z) {
                  section.init_P
                     .init(
                        type,
                        pose.pose(),
                        this.init_BlockPos.getX() + section.init_BlockPos.getX() - camera.x,
                        this.init_BlockPos.getY() + section.init_BlockPos.getY() - camera.y,
                        this.init_BlockPos.getZ() + section.init_BlockPos.getZ() - camera.z
                     );
                  this.flush = this.flush + section.load_I;
                  this.close = this.close + section.init_I;
               }
            }
         }
      }
   }

   private void init(aT.Inner_BufferHelper section, int alpha, ClientLevel world) {
      section.init_Z = false;
      section.init_J = this.load_J;
      Minecraft mc = Minecraft.getInstance();
      int[] quads = new int[]{0};
      int[] drawn = new int[]{0};
      Pose identity = new PoseStack().last();
      section.load_Z = section.init_P
         .init(
            autismclient.render.AutismRenderTypes.aiPlanGhost(),
            buffer -> {
               label91:
               for (int i : section.init_IntArrayList) {
                  int classification = this.init_I_I(i);
                  if (classification != -12067963) {
                     BlockState state = this.init_BlockStateArr[i];
                     if (!state.isAir()) {
                        autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil model = init_DataUtil_StreamUti_BlockState(state);
                        if (!model.init_List().isEmpty()) {
                           BlockPos at = this.init_BlockPos_I(i);
                           float ox = at.getX() - this.init_BlockPos.getX() - section.init_BlockPos.getX();
                           float oy = at.getY() - this.init_BlockPos.getY() - section.init_BlockPos.getY();
                           float oz = at.getZ() - this.init_BlockPos.getZ() - section.init_BlockPos.getZ();
                           boolean proud = classification == -20923 || classification == -885830;
                           drawn[0]++;
                           Iterator i$ = model.init_List().iterator();

                           while (true) {
                              autismclient.util.oresim.OreGhostModelCache.Inner_DataUtil face;
                              Direction cull;
                              Integer next;
                              do {
                                 if (!i$.hasNext()) {
                                    continue label91;
                                 }

                                 face = (autismclient.util.oresim.OreGhostModelCache.Inner_DataUtil)i$.next();
                                 cull = face.init_Direction();
                                 if (cull == null) {
                                    break;
                                 }

                                 next = this.load_Map.get(at.relative(cull).subtract(this.init_BlockPos).asLong());
                              } while (
                                 next != null && this.init_ZArr[next]
                                    || next == null && this.init_Predicate != null && this.init_Predicate.test(at.relative(cull).subtract(this.init_BlockPos))
                              );

                              BlockTintSource tintSource = face.init_I() < 0 ? null : mc.getBlockColors().getTintSource(state, face.init_I());
                              int tint = tintSource == null ? 16777215 : tintSource.colorInWorld(state, world, at);
                              float shade = face.load() == null ? 1.0F : world.cardinalLighting().byFace(face.load());
                              int color = cG.init_I_I(classification, tint, shade, alpha);
                              float[] data = face.init_FArr();

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
      section.init_I = quads[0];
      section.load_I = drawn[0];
   }

   static autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil init_DataUtil_StreamUti_BlockState(BlockState state) {
      autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil model = autismclient.util.oresim.OreGhostModelCache.init(state);
      if (!model.init_List().isEmpty()) {
         return model;
      }

      Object set = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
      if (set != init_Obj) {
         init_Map.clear();
         init_Obj = set;
      }

      return init_Map.computeIfAbsent(state, aT::load);
   }

   private static autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil load(BlockState state) {
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
                  null, d, init_FArr_AABB(box, d, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1()), -1
               )
            );
         }
      }

      return new autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil(List.copyOf(faces));
   }

   private static float[] init_FArr_AABB(AABB b, Direction d, float u0, float v0, float u1, float v1) {
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
      this.read_Z = true;
      this.init_aT_CachePool = null;
      this.init_aT_StreamUtil = null;
      this.read_List = List.of();

      for (aT.Inner_BufferHelper section : this.load_List) {
         section.init_P.init_V();
      }
   }

   private static final class Inner_BufferHelper {
      final BlockPos init_BlockPos;
      final IntArrayList init_IntArrayList;
      final autismclient.util.P init_P = new autismclient.util.P("autism_ai_plan_section");
      boolean init_Z = true;
      boolean load_Z;
      int init_I;
      int load_I;
      long init_J;

      Inner_BufferHelper(BlockPos anchor, IntArrayList members) {
         this.init_BlockPos = anchor;
         this.init_IntArrayList = members;
      }
   }

   private final class Inner_CachePool {
      final Map<TableIndex.Inner_StreamUtil, BlockState> init_Map = new HashMap<>();
      final TreeMap<Long, IntArrayList> init_TreeMap = new TreeMap<>();
      Iterator<Entry<Long, IntArrayList>> init_Iterator;
      int init_I;

      void init() {
         if (this.init_I < aT.this.init_List.size()) {
            int i = this.init_I++;
            TableIndex.Inner_StreamUtil cell = aT.this.init_List.get(i);
            TableIndex.Inner_StreamUtil key = new TableIndex.Inner_StreamUtil(0, 0, 0, cell.block(), cell.properties());
            aT.this.init_BlockStateArr[i] = this.init_Map.computeIfAbsent(key, TableIndex::init_BlockState);
            aT.this.init_ZArr[i] = aT.this.init_BlockStateArr[i].isSolidRender();
            BlockPos at = new BlockPos(cell.dx(), cell.dy(), cell.dz());
            aT.this.init_I = Math.max(aT.this.init_I, cell.dx() + 1);
            aT.this.load_I = Math.max(aT.this.load_I, cell.dy() + 1);
            aT.this.read_I = Math.max(aT.this.read_I, cell.dz() + 1);
            aT.this.load_Map.put(at.asLong(), i);
            this.init_TreeMap.computeIfAbsent(BlockPos.asLong(at.getX() >> 4, at.getY() >> 4, at.getZ() >> 4), ignored -> new IntArrayList()).add(i);
         } else {
            if (this.init_Iterator == null) {
               this.init_Iterator = this.init_TreeMap.entrySet().iterator();
            }

            if (!this.init_Iterator.hasNext()) {
               aT.this.init_aT_CachePool = null;
               return;
            }

            Entry<Long, IntArrayList> entry = this.init_Iterator.next();
            BlockPos key = BlockPos.of(entry.getKey());
            aT.this.load_List.add(new aT.Inner_BufferHelper(new BlockPos(key.getX() << 4, key.getY() << 4, key.getZ() << 4), entry.getValue()));
         }
      }
   }

   private record Inner_DataUtil(aT.Inner_BufferHelper section, double distance) {
   }

   private final class Inner_StreamUtil {
      final Vec3 init_Vec3;
      final int init_I;
      final long init_J = aT.this.read_J;
      final PriorityQueue<aT.Inner_DataUtil> init_PriorityQueue = new PriorityQueue<>(Comparator.comparingDouble(aT.Inner_DataUtil::distance));
      final PriorityQueue<Integer> load_PriorityQueue;
      int load_I;
      int read;
      aT.Inner_BufferHelper init_aT_BufferHelper;
      boolean init_Z;

      Inner_StreamUtil(Vec3 eye, int limit) {
         this.init_Vec3 = eye;
         this.init_I = limit;
         this.load_PriorityQueue = new PriorityQueue<>(Comparator.<Integer>comparingDouble(i -> this.init(i)).thenComparingInt(Integer::intValue).reversed());
      }

      double init(int i) {
         TableIndex.Inner_StreamUtil cell = aT.this.init_List.get(i);
         double dx = aT.this.init_BlockPos.getX() + cell.dx() + 0.5 - this.init_Vec3.x;
         double dy = aT.this.init_BlockPos.getY() + cell.dy() + 0.5 - this.init_Vec3.y;
         double dz = aT.this.init_BlockPos.getZ() + cell.dz() + 0.5 - this.init_Vec3.z;
         return dx * dx + dy * dy + dz * dz;
      }

      double init(aT.Inner_BufferHelper section) {
         double dx = this.init_D_D_D(this.init_Vec3.x, aT.this.init_BlockPos.getX() + section.init_BlockPos.getX() + 0.5);
         double dy = this.init_D_D_D(this.init_Vec3.y, aT.this.init_BlockPos.getY() + section.init_BlockPos.getY() + 0.5);
         double dz = this.init_D_D_D(this.init_Vec3.z, aT.this.init_BlockPos.getZ() + section.init_BlockPos.getZ() + 0.5);
         return dx * dx + dy * dy + dz * dz;
      }

      double init_D_D_D(double coordinate, double minimum) {
         return coordinate < minimum ? minimum - coordinate : (coordinate > minimum + 15.0 ? coordinate - minimum - 15.0 : 0.0);
      }

      void init_V_I_J(int budget, long deadline) {
         for (int used = 0; used < budget && !this.init_Z; used++) {
            if (used > 0 && System.nanoTime() >= deadline) {
               return;
            }

            if (this.load_I < aT.this.load_List.size()) {
               aT.Inner_BufferHelper section = aT.this.load_List.get(this.load_I++);
               this.init_PriorityQueue.add(new aT.Inner_DataUtil(section, this.init(section)));
            } else if (this.init_aT_BufferHelper != null && this.read < this.init_aT_BufferHelper.init_IntArrayList.size()) {
               int i = this.init_aT_BufferHelper.init_IntArrayList.getInt(this.read++);
               if (aT.this.init_I_I(i) != -12067963) {
                  this.load_PriorityQueue.add(i);
                  if (this.load_PriorityQueue.size() > this.init_I) {
                     this.load_PriorityQueue.poll();
                  }
               }
            } else if (this.init_PriorityQueue.isEmpty()
               || this.load_PriorityQueue.size() == this.init_I && this.init_PriorityQueue.peek().distance() > this.init(this.load_PriorityQueue.peek())) {
               this.init_Z = true;
            } else {
               this.init_aT_BufferHelper = this.init_PriorityQueue.remove().section();
               this.read = 0;
            }
         }
      }
   }
}
