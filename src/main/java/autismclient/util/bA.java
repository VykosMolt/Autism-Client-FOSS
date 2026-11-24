package autismclient.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class bA {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final double init_D = 1.44;
   private static final double load_D = 0.25;
   private static final double read_D = 2.25;
   private static final int init_I = 2;
   private static final int load_I = 9;
   private static final int read_I = 6000;
   private static final int write_I = 1200;
   private static final int update_I = 10;
   private static final int sync_I = 40;
   private static final int flush_I = 3;
   private static final int close_I = 12;
   private static final int open_I = 30;
   private static final int poll_I = 4;
   private static final int push_I = 26;
   private static final int pop = 18;
   private static final double write_D = 0.6;
   private static final double update_D = 0.3;
   private static final double sync_D = 1.8;
   private static final float init_F = 12.0F;
   private static final double flush_D = 2.5600000000000005;
   private static final double close_D = 3.61;
   private static final int tick = 15;
   private static final double open_D = 0.09;
   private static final int run = 100;
   private static final int get = 400;
   private static final int set = 5;
   private static final int put = 8;
   private static final int add = 1;
   private static final int clear = 2;
   private static final int reset = 4;
   private static final int start = 8;
   private static final int stop = 16;
   private static final int next = 32;
   private static final float[] init_FArr = new float[]{0.0F, -45.0F, 45.0F, -90.0F, 90.0F, -135.0F, 135.0F, 180.0F};
   private static final int[] init_IArr = new int[]{1, 3, 9, 2, 8, 6, 12, 4};
   private static final int[] load_IArr = new int[]{1, -1, 0, 0};
   private static final int[] read_IArr = new int[]{0, 0, 1, -1};
   private static final ArrayList<bA.Inner_StreamUtil> init_ArrayList = new ArrayList<>();
   private static int prev;
   private static BlockPos init_BlockPos;
   private static int valid;
   private static int size;
   private static int data;
   private static final HashMap<Long, Integer> init_HashMap = new HashMap<>();
   private static final HashMap<Long, Long> load_HashMap = new HashMap<>();
   private static final HashSet<Long> init_HashSet = new HashSet<>();
   private static final HashSet<Long> load_HashSet = new HashSet<>();
   private static final PriorityQueue<bA.Inner_DataUtil> init_PriorityQueue = new PriorityQueue<>();
   private static final ArrayList<Long> load_ArrayList = new ArrayList<>();
   private static final ArrayList<bA.Inner_StreamUtil> read_ArrayList = new ArrayList<>();
   private static final HashSet<Long> read_HashSet = new HashSet<>();
   private static final HashMap<Long, Boolean> read_HashMap = new HashMap<>();
   private static boolean init_Z;
   private static final HashMap<BlockPos, Double> write_HashMap = new HashMap<>();
   private static int value = -2147483648;
   private static final MutableBlockPos init_BlockPos_MutableBl = new MutableBlockPos();
   private static int cache;
   private static int state;
   private static int queue;
   private static int buffer;
   private static int entry;
   private static BlockPos load_BlockPos;
   private static int node;
   private static double poll_D;
   private static double push_D;
   private static int task;
   private static int pool;
   private static int index;
   private static boolean load_Z;
   private static final HashMap<BlockPos, Integer> update_HashMap = new HashMap<>();
   private static final HashMap<BlockPos, Integer> sync_HashMap = new HashMap<>();
   private static final HashMap<BlockPos, Integer> flush_HashMap = new HashMap<>();

   private bA() {
   }

   public static boolean init_Z_BlockPos(BlockPos pos) {
      return pos != null && init_Minecraft != null && init_Minecraft.level != null && write_Z(pos.getX(), pos.getY(), pos.getZ());
   }

   public static boolean load(BlockPos target) {
      if (target != null && init_Minecraft != null && init_Minecraft.level != null && init_Minecraft.player != null) {
         double dx = target.getX() + 0.5 - init_Minecraft.player.getX();
         double dz = target.getZ() + 0.5 - init_Minecraft.player.getZ();
         return dx * dx + dz * dz <= 1.44 && init_Z_Vec3_BlockPos(init_Minecraft.player.getEyePosition(), target);
      } else {
         return false;
      }
   }

   public static double init_D_BlockPos(BlockPos target) {
      if (target != null && init_Minecraft != null && init_Minecraft.level != null && init_Minecraft.player != null && !write(target)) {
         int now = ck.init_ck().state_I();
         if (value != now) {
            value = now;
            write_HashMap.clear();
         }

         Double cached = write_HashMap.get(target);
         if (cached != null) {
            return cached;
         }

         double cost = load(target) ? 0.0 : init(init_Minecraft.player.getX(), init_Minecraft.player.getY(), init_Minecraft.player.getZ(), target, false, 1200);
         write_HashMap.put(target.immutable(), cost);
         return cost;
      } else {
         return 1.0 / 0.0;
      }
   }

   private static boolean init_Z_Vec3_BlockPos(Vec3 eye, BlockPos target) {
      Vec3 aim = Vec3.atCenterOf(target);
      double reach = init_Minecraft.player.blockInteractionRange();
      if (eye.distanceToSqr(aim) > reach * reach) {
         return false;
      }

      BlockHitResult hit = init_Minecraft.level.clip(new ClipContext(eye, aim, Block.OUTLINE, Fluid.NONE, init_Minecraft.player));
      return hit.getType() == Type.MISS || hit.getBlockPos().equals(target);
   }

   public static void init_V() {
      load();
      update_HashMap.clear();
      sync_HashMap.clear();
      flush_HashMap.clear();
   }

   public static void load() {
      update();
      init_ArrayList.clear();
      prev = 0;
      init_BlockPos = null;
      load_BlockPos = null;
      node = 0;
      task = 0;
      pool = 0;
      init_HashMap.clear();
      load_HashMap.clear();
      init_HashSet.clear();
      load_HashSet.clear();
      init_PriorityQueue.clear();
      load_ArrayList.clear();
      read_ArrayList.clear();
      read_HashSet.clear();
      read_HashMap.clear();
      write_HashMap.clear();
      value = -2147483648;
      load_Z = false;
   }

   public static boolean read(BlockPos target) {
      if (init_Minecraft == null || init_Minecraft.player == null || init_Minecraft.level == null || init_Minecraft.options == null) {
         update();
         sync();
         init_BlockPos = null;
         return false;
      }

      if (target == null) {
         update();
         sync();
         return false;
      }

      int now = ck.init_ck().state_I();
      if (write(target)) {
         update();
         sync();
         init_BlockPos = null;
         return false;
      }

      LocalPlayer player = init_Minecraft.player;
      double px = player.getX();
      double py = player.getY();
      double pz = player.getZ();
      if (init_BlockPos == null || !init_BlockPos.equals(target)) {
         if (init_BlockPos != null && update(target)) {
            init_BlockPos = target.immutable();
            if (prev >= init_ArrayList.size() && index == 0) {
               task = 0;
               pool = 0;
               poll_D = px;
               push_D = pz;
            }
         } else {
            update();
            sync();
            init_BlockPos = target.immutable();
            task = 0;
            pool = 0;
            poll_D = px;
            push_D = pz;
         }
      }

      if (load(init_BlockPos)) {
         flush_HashMap.remove(init_BlockPos);
         task = 0;
         pool = 0;
         poll_D = px;
         push_D = pz;
         update();
         sync();
         return false;
      }

      int feetY = Mth.floor(py);
      init_V_D(px, py, pz, feetY);
      boolean followGoal = prev >= init_ArrayList.size()
         && !init_ArrayList.isEmpty()
         && init_Z()
         && init_Z_D(px, py, pz, init_BlockPos.getX() + 0.5, init_BlockPos.getZ() + 0.5);
      boolean needPlan = prev >= init_ArrayList.size() && !followGoal;
      if (prev < init_ArrayList.size()) {
         bA.Inner_StreamUtil node = init_ArrayList.get(prev);
         double segX = node.init_I - valid;
         double segZ = node.read - data;
         double fromX = valid + 0.5;
         double fromZ = data + 0.5;
         double lenSq = segX * segX + segZ * segZ;
         double t = lenSq < 1.0E-6 ? 0.0 : Mth.clamp(((px - fromX) * segX + (pz - fromZ) * segZ) / lenSq, 0.0, 1.0);
         double offX = px - (fromX + segX * t);
         double offZ = pz - (fromZ + segZ * t);
         if (offX * offX + offZ * offZ > 2.25 || feetY < node.load - 2) {
            needPlan = true;
         }

         if (!needPlan
            && !node.init_Z
            && node.load == size
            && player.onGround()
            && !update(Mth.floor(px), feetY, Mth.floor(pz))
            && !update(Mth.floor(px), feetY + 1, Mth.floor(pz))
            && bz.init(load_D(valid, size, data), load_D(node.init_I, node.load, node.read), 0.6)) {
            double nx = node.init_I + 0.5 - px;
            double nz = node.read + 0.5 - pz;
            double fraction = Math.min(1.0, 0.8 / Math.max(0.001, Math.hypot(nx, nz)));
            needPlan = !init(px, py, pz, px + nx * fraction, pz + nz * fraction, false);
         }
      }

      if (needPlan && init_Z_BlockPos_I(init_BlockPos, now)) {
         load(init_BlockPos, now);
         if (!init_Z_D(px, py, pz)) {
            write();
            update();
            sync();
            return false;
         }

         prev = 0;
         init_V_D(px, py, pz, feetY);
      } else if (needPlan) {
         update();
         if (!autismclient.modules.FieldCache.read_Z_3() && !autismclient.modules.TableIndex.load_Z_9()) {
            double movedX = px - poll_D;
            double movedZ = pz - push_D;
            if (movedX * movedX + movedZ * movedZ >= 0.09) {
               poll_D = px;
               push_D = pz;
               task = 0;
            } else if (++task >= 15) {
               write();
               update();
               sync();
               return false;
            }

            return true;
         }

         poll_D = px;
         push_D = pz;
         task = 0;
         return true;
      }

      if (prev >= init_ArrayList.size() && !followGoal) {
         update();
         load_Z = false;
         if (!init_ArrayList.isEmpty()) {
            sync();
         }

         return false;
      } else {
         int nodeX;
         int nodeY;
         int nodeZ;
         boolean nodeJump;
         if (prev < init_ArrayList.size()) {
            bA.Inner_StreamUtil node = init_ArrayList.get(prev);
            nodeX = node.init_I;
            nodeY = node.load;
            nodeZ = node.read;
            nodeJump = node.init_Z;
            load_Z = false;
         } else {
            nodeX = init_BlockPos.getX();
            nodeY = init_BlockPos.getY();
            nodeZ = init_BlockPos.getZ();
            nodeJump = false;
            load_Z = true;
         }

         double dx = nodeX + 0.5 - px;
         double dz = nodeZ + 0.5 - pz;
         double distSq = dx * dx + dz * dz;
         float desiredYaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
         float delta = Mth.wrapDegrees(desiredYaw - player.getYRot());
         int mask = init_I_F(delta);
         double frac = py - feetY;
         int standCellY = frac > 0.5 ? feetY + 1 : feetY;
         boolean headroom = load_Z(Mth.floor(px), standCellY + 2, Mth.floor(pz));
         double jumpRangeSq = load_D(nodeX, nodeY, nodeZ) - py > 0.6 ? 3.61 : 2.5600000000000005;
         double liveRise = load_D(nodeX, nodeY, nodeZ) - py;
         if (liveRise <= -0.5 && !Z.init(init_Minecraft.level, Mth.floor(px), standCellY, Mth.floor(pz), nodeX, nodeY, nodeZ, false, liveRise)) {
            update();
            sync();
            return true;
         }

         boolean jump = headroom && nodeJump && distSq <= jumpRangeSq;
         if (!jump && headroom && !load_Z && task >= 5) {
            jump = !init_Z_D_2(px, feetY, pz) && !flush(nodeX, nodeY - 1, nodeZ);
         }

         if (jump && !Z.init(init_Minecraft.level, Mth.floor(px), standCellY, Mth.floor(pz), nodeX, nodeY, nodeZ, true, 0.0)) {
            jump = false;
            if (nodeJump) {
               update();
               sync();
               return true;
            }
         }

         int sprint = (mask & 1) != 0 && !jump ? 32 : 0;
         init_V_I(mask | (jump ? 16 : 0) | sprint);
         pool = jump ? pool + 1 : 0;
         if (!autismclient.modules.FieldCache.read_Z_3() && !autismclient.modules.TableIndex.load_Z_9()) {
            double movedX = px - poll_D;
            double movedZ = pz - push_D;
            if (movedX * movedX + movedZ * movedZ >= 0.09) {
               poll_D = px;
               push_D = pz;
               task = 0;
            } else {
               task++;
               if (task >= 8 && pool >= 8 && init_Z_D(px, py, pz, standCellY, dx, dz, distSq)) {
                  write();
                  update();
                  sync();
                  return false;
               }

               if (task >= 15) {
                  write();
                  update();
                  sync();
                  return false;
               }
            }

            return true;
         } else {
            poll_D = px;
            push_D = pz;
            task = 0;
            return true;
         }
      }
   }

   public static BlockPos init_BlockPos() {
      if (init_BlockPos == null) {
         return null;
      } else if (prev < init_ArrayList.size()) {
         bA.Inner_StreamUtil node = init_ArrayList.get(prev);
         return new BlockPos(node.init_I, node.load, node.read);
      } else {
         return load_Z ? init_BlockPos : null;
      }
   }

   public static void init_V_BlockPos_I(BlockPos target, int ticks) {
      if (target != null && ticks > 0) {
         BlockPos key = target.immutable();
         update_HashMap.put(key, ck.init_ck().state_I());
         sync_HashMap.put(key, ticks);
      }
   }

   public static boolean write(BlockPos target) {
      if (target == null) {
         return false;
      }

      Integer stamp = update_HashMap.get(target);
      if (stamp == null) {
         return false;
      }

      int age = ck.init_ck().state_I() - stamp;
      if (age >= 0 && age <= sync_HashMap.getOrDefault(target, 0)) {
         return true;
      }

      update_HashMap.remove(target);
      sync_HashMap.remove(target);
      return false;
   }

   private static void write() {
      int fails = flush_HashMap.merge(init_BlockPos, 1, Integer::sum);
      int ticks = Math.min(100 << Math.min(fails - 1, 2), 400);
      init_V_BlockPos_I(init_BlockPos, ticks);
   }

   private static int init_I_F(float delta) {
      if (Math.abs(delta) <= 12.0F) {
         return 1;
      }

      int best = 0;
      float bestError = 3.4028235E38F;

      for (int i = 0; i < init_IArr.length; i++) {
         float error = Math.abs(Mth.wrapDegrees(delta - init_FArr[i]));
         if (error < bestError) {
            bestError = error;
            best = i;
         }
      }

      return init_IArr[best];
   }

   private static void init_V_I(int mask) {
      if (init_Minecraft.options == null) {
         index = 0;
      } else {
         int changed = mask ^ index;
         if (changed != 0) {
            if ((changed & 1) != 0) {
               init_V_KeyMapping_Z(init_Minecraft.options.keyUp, (mask & 1) != 0);
            }

            if ((changed & 2) != 0) {
               init_V_KeyMapping_Z(init_Minecraft.options.keyLeft, (mask & 2) != 0);
            }

            if ((changed & 4) != 0) {
               init_V_KeyMapping_Z(init_Minecraft.options.keyDown, (mask & 4) != 0);
            }

            if ((changed & 8) != 0) {
               init_V_KeyMapping_Z(init_Minecraft.options.keyRight, (mask & 8) != 0);
            }

            if ((changed & 16) != 0) {
               init_V_KeyMapping_Z(init_Minecraft.options.keyJump, (mask & 16) != 0);
            }

            if ((changed & 32) != 0) {
               init_V_KeyMapping_Z(init_Minecraft.options.keySprint, (mask & 32) != 0);
            }

            index = mask;
         }
      }
   }

   private static void init_V_KeyMapping_Z(KeyMapping key, boolean down) {
      if (down) {
         key.setDown(true);
      } else {
         aB.init(key).autism$resetPressedState();
      }
   }

   private static void update() {
      init_V_I(0);
   }

   public static void read() {
      index = 0;
   }

   private static void sync() {
      init_ArrayList.clear();
      prev = 0;
      load_Z = false;
   }

   private static void init_V_D(double px, double py, double pz, int feetY) {
      while (prev < init_ArrayList.size() && (prev != init_ArrayList.size() - 1 || init_Z())) {
         bA.Inner_StreamUtil node = init_ArrayList.get(prev);
         double dx = node.init_I + 0.5 - px;
         double dz = node.read + 0.5 - pz;
         if (dx * dx + dz * dz < 0.25 && Math.abs(feetY - node.load) <= 1 && (!node.init_Z || init_Z_bA_StreamUtil_D(node, py))) {
            valid = node.init_I;
            size = node.load;
            data = node.read;
            prev++;
            continue;
         }
         break;
      }
   }

   private static boolean init_Z_bA_StreamUtil_D(bA.Inner_StreamUtil node, double py) {
      double ledgeTop = load_D(node.init_I, node.load, node.read);
      return py >= ledgeTop - 0.0625;
   }

   private static boolean init_Z_D(double px, double py, double pz, int standCellY, double dx, double dz, double distSq) {
      if (distSq < 1.0E-6) {
         return false;
      }

      double len = Math.sqrt(distSq);
      int aheadX = Mth.floor(px + dx / len * 0.9);
      int aheadZ = Mth.floor(pz + dz / len * 0.9);
      double rise = standCellY + init_D_I(aheadX, standCellY, aheadZ) - py;
      return rise > 1.25 ? true : rise > 0.6 && init_D_I(aheadX, standCellY + 1, aheadZ) > 0.0;
   }

   private static boolean update(BlockPos target) {
      if (load_BlockPos == null) {
         return false;
      }

      if (target.getY() != load_BlockPos.getY()) {
         return false;
      }

      int dx = target.getX() - load_BlockPos.getX();
      int dz = target.getZ() - load_BlockPos.getZ();
      return dx * dx + dz * dz <= 9;
   }

   private static boolean init_Z() {
      return load_BlockPos != null && !load_BlockPos.equals(init_BlockPos) && update(init_BlockPos);
   }

   private static boolean init_Z_BlockPos_I(BlockPos target, int now) {
      return load_BlockPos != null && load_BlockPos.equals(target) ? now - node >= 10 : true;
   }

   private static void load(BlockPos target, int now) {
      load_BlockPos = target;
      node = now;
   }

   private static boolean init_Z_D(double px, double py, double pz) {
      sync();
      return Double.isFinite(init(px, py, pz, init_BlockPos, true, 6000));
   }

   private static double init(double px, double py, double pz, BlockPos target, boolean materialize, int expansionLimit) {
      init_HashMap.clear();
      load_HashMap.clear();
      init_HashSet.clear();
      load_HashSet.clear();
      init_PriorityQueue.clear();
      load_ArrayList.clear();
      read_ArrayList.clear();
      read_HashSet.clear();
      read_HashMap.clear();
      init_Z = true;

      try {
         return load(px, py, pz, target, materialize, expansionLimit);
      } finally {
         init_Z = false;
      }
   }

   private static double load(double px, double py, double pz, BlockPos target, boolean materialize, int expansionLimit) {
      int startX = Mth.floor(px);
      int startZ = Mth.floor(pz);
      int startY = read_I(startX, Mth.floor(py), startZ);
      if (startY == -2147483648) {
         return 1.0 / 0.0;
      }

      buffer = startX;
      bA.entry = startZ;
      cache = target.getX();
      queue = target.getZ();
      state = target.getY();
      init_V_I_2(cache, queue, target);

      for (int d = 0; d < 4; d++) {
         init_V_I_2(cache + load_IArr[d], queue + read_IArr[d], target);
      }

      if (read_HashSet.isEmpty()) {
         return 1.0 / 0.0;
      }

      if (Math.abs(cache - startX) + Math.abs(queue - startZ) > 41) {
         return 1.0 / 0.0;
      }

      long startKey = BlockPos.asLong(startX, startY, startZ);
      if (materialize) {
         valid = startX;
         size = startY;
         data = startZ;
      }

      init_HashMap.put(startKey, 0);
      init_PriorityQueue.add(new bA.Inner_DataUtil(startKey, load_I(startX, startY, startZ)));
      int expanded = 0;

      while (!init_PriorityQueue.isEmpty() && expanded < expansionLimit) {
         bA.Inner_DataUtil entry = init_PriorityQueue.poll();
         if (load_HashSet.add(entry.init_J)) {
            expanded++;
            if (read_HashSet.contains(entry.init_J)) {
               if (materialize) {
                  init_V_J(startKey, entry.init_J, startX, startY, startZ);
                  if (init_ArrayList.isEmpty()) {
                     init_ArrayList.add(new bA.Inner_StreamUtil(startX, startY, startZ, false));
                  }
               }

               return init_HashMap.get(entry.init_J).intValue();
            }

            init_V_J(entry.init_J);
         }
      }

      return 1.0 / 0.0;
   }

   private static void init_V_I_2(int x, int z, BlockPos target) {
      double eyeHeight = init_Minecraft.player.getEyeY() - init_Minecraft.player.getY();

      for (int y = target.getY() + 1; y >= target.getY() - 3; y--) {
         if (write_Z(x, y, z) && init_Z_Vec3_BlockPos(new Vec3(x + 0.5, load_D(x, y, z) + eyeHeight, z + 0.5), target)) {
            read_HashSet.add(BlockPos.asLong(x, y, z));
         }
      }
   }

   private static void init_V_J(long currentKey) {
      int cx = BlockPos.getX(currentKey);
      int cy = BlockPos.getY(currentKey);
      int cz = BlockPos.getZ(currentKey);
      int gCurrent = init_HashMap.get(currentKey);

      for (int d = 0; d < 4; d++) {
         int nx = cx + load_IArr[d];
         int nz = cz + read_IArr[d];
         if (Math.abs(nx - buffer) + Math.abs(nz - entry) <= 40) {
            double fromFloor = load_D(cx, cy, cz);
            double rise = load_D(nx, cy + 1, nz) - fromFloor;
            boolean hop = rise > 0.6;
            if (write_Z(nx, cy + 1, nz)
               && (!hop || load_Z(cx, cy + 2, cz))
               && rise <= 1.25
               && Z.init(init_Minecraft.level, cx, cy, cz, nx, cy + 1, nz, hop, rise)) {
               init_V_J(currentKey, gCurrent, nx, cy + 1, nz, (hop ? 14 : 10) + (hop && flush(nx, cy, nz) ? 26 : 0), hop);
            }

            for (int ny = cy; ny >= cy - 3 && init_Z_I(nx, ny, nz) && load_Z(nx, ny + 1, nz) && !update(nx, ny, nz) && !update(nx, ny + 1, nz); ny--) {
               if (close(nx, ny, nz)) {
                  double toFloor = load_D(nx, ny, nz);
                  double floorRise = toFloor - fromFloor;
                  boolean needsHop = floorRise > 0.6;
                  if (write_Z(nx, ny, nz)
                     && floorRise <= 1.25
                     && floorRise >= -3.0
                     && (!needsHop || load_Z(cx, cy + 2, cz))
                     && Z.init(init_Minecraft.level, cx, cy, cz, nx, ny, nz, needsHop, floorRise)) {
                     boolean trample = (needsHop || floorRise <= -0.5) && flush(nx, ny - 1, nz);
                     init_V_J(
                        currentKey, gCurrent, nx, ny, nz, 10 + (needsHop ? 4 : (int)Math.ceil(Math.max(0.0, -floorRise) * 2.0)) + (trample ? 26 : 0), needsHop
                     );
                  }
                  break;
               }
            }

            int jx = cx + load_IArr[d] * 2;
            int jz = cz + read_IArr[d] * 2;
            if (Math.abs(jx - buffer) + Math.abs(jz - entry) <= 40
               && !write_Z(nx, cy, nz)
               && write_Z(jx, cy, jz)
               && load_Z(cx, cy + 2, cz)
               && load_Z(jx, cy + 2, jz)
               && load_Z(nx, cy + 1, nz)
               && load_Z(nx, cy + 2, nz)
               && !update(nx, cy + 1, nz)
               && !update(nx, cy + 2, nz)
               && Math.abs(load_D(jx, cy, jz) - fromFloor) < 0.0625
               && open(nx, cy, nz)
               && Z.init(init_Minecraft.level, cx, cy, cz, jx, cy, jz, true, 0.0)) {
               init_V_J(currentKey, gCurrent, jx, cy, jz, 18 + (flush(jx, cy - 1, jz) ? 26 : 0), true);
            }
         }
      }
   }

   private static void init_V_J(long fromKey, int gFrom, int nx, int ny, int nz, int stepCost, boolean jump) {
      long key = BlockPos.asLong(nx, ny, nz);
      if (!load_HashSet.contains(key)) {
         int g = gFrom + stepCost + init_I_I(nx, ny, nz);
         Integer old = init_HashMap.get(key);
         if (old == null || old > g) {
            init_HashMap.put(key, g);
            load_HashMap.put(key, fromKey);
            if (jump) {
               init_HashSet.add(key);
            } else {
               init_HashSet.remove(key);
            }

            init_PriorityQueue.add(new bA.Inner_DataUtil(key, g + load_I(nx, ny, nz)));
         }
      }
   }

   private static int init_I_I(int x, int y, int z) {
      int cost = 0;
      if (sync(x, y, z)) {
         cost += 30;
      }

      if (flush(x, y - 1, z)) {
         cost += 4;
      }

      return cost;
   }

   private static int load_I(int x, int y, int z) {
      int best = 2147483647;

      for (long key : read_HashSet) {
         best = Math.min(best, bz.init(x, z, BlockPos.getX(key), BlockPos.getZ(key)));
      }

      return best;
   }

   private static void init_V_J(long startKey, long goalKey, int startX, int startY, int startZ) {
      load_ArrayList.clear();

      for (long key = goalKey; key != startKey; key = load_HashMap.get(key)) {
         load_ArrayList.add(key);
      }

      init_ArrayList.clear();

      for (int i = load_ArrayList.size() - 1; i >= 0; i--) {
         long nodeKey = load_ArrayList.get(i);
         init_ArrayList.add(new bA.Inner_StreamUtil(BlockPos.getX(nodeKey), BlockPos.getY(nodeKey), BlockPos.getZ(nodeKey), init_HashSet.contains(nodeKey)));
      }

      init_V_I(startX, startY, startZ);
   }

   private static void init_V_I(int startX, int startY, int startZ) {
      if (init_ArrayList.size() >= 3) {
         read_ArrayList.clear();
         int anchorX = startX;
         int anchorY = startY;
         int anchorZ = startZ;
         int i = 0;

         while (i < init_ArrayList.size()) {
            int far = i;
            int limit = Math.min(init_ArrayList.size() - 1, i + 12);

            for (int j = limit; j > i; j--) {
               bA.Inner_StreamUtil candidate = init_ArrayList.get(j);
               if (!candidate.init_Z && candidate.load == anchorY && init_Z_I(anchorX, anchorY, anchorZ, candidate.init_I, candidate.read)) {
                  far = j;
                  break;
               }
            }

            bA.Inner_StreamUtil next = init_ArrayList.get(far);
            read_ArrayList.add(next);
            anchorX = next.init_I;
            anchorY = next.load;
            anchorZ = next.read;
            i = far + 1;
         }

         init_ArrayList.clear();
         init_ArrayList.addAll(read_ArrayList);
         read_ArrayList.clear();
      }
   }

   private static boolean init_Z_I(int ax, int ay, int az, int bx, int bz) {
      return init_Z_D(ax + 0.5, load_D(ax, ay, az), az + 0.5, bx + 0.5, bz + 0.5);
   }

   private static boolean init_Z_D(double ax, double floorY, double az, double bx, double bz) {
      return init(ax, floorY, az, bx, bz, true);
   }

   private static boolean init(double ax, double floorY, double az, double bx, double bz, boolean avoidHazards) {
      double dx = bx - ax;
      double dz = bz - az;
      int steps = Math.max(1, (int)Math.ceil(Math.hypot(dx, dz) * 8.0));
      double lastX = ax;
      double lastZ = az;
      double lastFloor = floorY;

      for (int s = 1; s <= steps; s++) {
         double t = (double)s / steps;
         double x = ax + dx * t;
         double z = az + dz * t;
         int cx = Mth.floor(x);
         int cz = Mth.floor(z);
         int cellY = init_I_I(cx, Mth.floor(lastFloor), cz, lastFloor);
         if (cellY == -2147483648 || avoidHazards && sync(cx, cellY, cz)) {
            return false;
         }

         double nextFloor = load_D(cx, cellY, cz);
         if (!autismclient.util.bz.init(lastFloor, nextFloor, 0.6)) {
            return false;
         }

         if (!load(lastX, lastZ, x, z, Math.max(lastFloor, nextFloor))) {
            return false;
         }

         lastX = x;
         lastZ = z;
         lastFloor = nextFloor;
      }

      return true;
   }

   private static int init_I_I(int x, int feetY, int z, double fromFloor) {
      for (int y = feetY + 1; y >= feetY - 1; y--) {
         if (write_Z(x, y, z) && bz.init(fromFloor, load_D(x, y, z), 0.6)) {
            return y;
         }
      }

      return -2147483648;
   }

   private static boolean load(double ax, double az, double bx, double bz, double floor) {
      double half = 0.29999;
      AABB bounds = new AABB(
         Math.min(ax, bx) - half, floor + 1.0E-5, Math.min(az, bz) - half, Math.max(ax, bx) + half, floor + 1.8 - 1.0E-5, Math.max(az, bz) + half
      );

      for (VoxelShape shape : init_Minecraft.level.getBlockCollisions(init_Minecraft.player, bounds)) {
         for (AABB box : shape.toAabbs()) {
            if (!(box.maxY <= bounds.minY)
               && !(box.minY >= bounds.maxY)
               && autismclient.util.bz.init(ax, az, bx, bz, box.minX - half, box.minZ - half, box.maxX + half, box.maxZ + half)) {
               return false;
            }
         }
      }

      return true;
   }

   private static boolean init_Z_I(int x, int y, int z) {
      BlockState state = init_Minecraft.level.getBlockState(init_BlockPos_MutableBl.set(x, y, z));
      if (!state.getFluidState().isEmpty()) {
         return false;
      } else {
         VoxelShape shape = state.getCollisionShape(init_Minecraft.level, init_BlockPos_MutableBl);
         if (shape.isEmpty() || !(shape.min(Axis.Y) > 1.0E-4) && !(shape.max(Axis.Y) > 0.6)) {
            VoxelShape below = init_Minecraft.level
               .getBlockState(init_BlockPos_MutableBl.set(x, y - 1, z))
               .getCollisionShape(init_Minecraft.level, init_BlockPos_MutableBl);
            return below.isEmpty() || below.max(Axis.Y) <= 1.0001;
         } else {
            return false;
         }
      }
   }

   private static boolean load_Z(int x, int y, int z) {
      BlockState state = init_Minecraft.level.getBlockState(init_BlockPos_MutableBl.set(x, y, z));
      return !state.getFluidState().isEmpty() ? false : state.getCollisionShape(init_Minecraft.level, init_BlockPos_MutableBl).isEmpty();
   }

   private static boolean read_Z(int x, int y, int z) {
      BlockState state = init_Minecraft.level.getBlockState(init_BlockPos_MutableBl.set(x, y, z));
      if (state.isFaceSturdy(init_Minecraft.level, init_BlockPos_MutableBl, Direction.UP)) {
         return true;
      }

      VoxelShape shape = state.getCollisionShape(init_Minecraft.level, init_BlockPos_MutableBl);
      return !shape.isEmpty() && shape.max(Axis.Y) >= 0.5;
   }

   private static boolean write_Z(int x, int y, int z) {
      long key = BlockPos.asLong(x, y, z);
      if (init_Z) {
         Boolean cached = read_HashMap.get(key);
         if (cached != null) {
            return cached;
         }
      }

      boolean result = y >= init_Minecraft.level.getMinY()
         && y < init_Minecraft.level.getMaxY()
         && init_Minecraft.level.hasChunkAt(init_BlockPos_MutableBl.set(x, y, z))
         && init_Z_I(x, y, z)
         && load_Z(x, y + 1, z)
         && close(x, y, z)
         && !update(x, y, z)
         && !update(x, y + 1, z)
         && load(x + 0.5, z + 0.5, x + 0.5, z + 0.5, load_D(x, y, z));
      if (init_Z) {
         read_HashMap.put(key, result);
      }

      return result;
   }

   private static boolean update(int x, int y, int z) {
      BlockState state = init_Minecraft.level.getBlockState(init_BlockPos_MutableBl.set(x, y, z));
      return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.POWDER_SNOW);
   }

   private static boolean sync(int x, int y, int z) {
      if (init_Minecraft.level.getBlockState(init_BlockPos_MutableBl.set(x, y, z)).is(Blocks.SWEET_BERRY_BUSH)) {
         return true;
      }

      BlockState floor = init_Minecraft.level.getBlockState(init_BlockPos_MutableBl.set(x, y - 1, z));
      return floor.is(Blocks.MAGMA_BLOCK) || floor.is(Blocks.CACTUS);
   }

   private static boolean flush(int x, int y, int z) {
      return init_Minecraft.level.getBlockState(init_BlockPos_MutableBl.set(x, y, z)).is(Blocks.FARMLAND);
   }

   private static boolean init_Z_D_2(double px, int feetY, double pz) {
      int x = Mth.floor(px);
      int z = Mth.floor(pz);
      return flush(x, feetY - 1, z) || flush(x, feetY, z);
   }

   private static boolean close(int x, int y, int z) {
      if (read_Z(x, y - 1, z)) {
         return true;
      }

      BlockState state = init_Minecraft.level.getBlockState(init_BlockPos_MutableBl.set(x, y, z));
      VoxelShape shape = state.getCollisionShape(init_Minecraft.level, init_BlockPos_MutableBl);
      return !shape.isEmpty() && shape.min(Axis.Y) <= 1.0E-4 && shape.max(Axis.Y) <= 0.6;
   }

   private static double init_D_I(int x, int y, int z) {
      VoxelShape shape = init_Minecraft.level
         .getBlockState(init_BlockPos_MutableBl.set(x, y, z))
         .getCollisionShape(init_Minecraft.level, init_BlockPos_MutableBl);
      return shape.isEmpty() ? 0.0 : shape.max(Axis.Y);
   }

   private static double load_D(int x, int y, int z) {
      BlockState state = init_Minecraft.level.getBlockState(init_BlockPos_MutableBl.set(x, y, z));
      if (!state.getFluidState().isEmpty()) {
         return y;
      }

      VoxelShape shape = state.getCollisionShape(init_Minecraft.level, init_BlockPos_MutableBl);
      return !shape.isEmpty() && shape.min(Axis.Y) <= 1.0E-4 && shape.max(Axis.Y) <= 0.6 ? y + shape.max(Axis.Y) : y - 1 + init_D_I(x, y - 1, z);
   }

   private static boolean open(int x, int y, int z) {
      for (int gy = y; gy >= y - 3; gy--) {
         BlockState state = init_Minecraft.level.getBlockState(init_BlockPos_MutableBl.set(x, gy, z));
         if (!state.getFluidState().isEmpty()) {
            return !state.getFluidState().is(FluidTags.LAVA);
         }

         if (update(x, gy, z)) {
            return false;
         }

         if (init_Z_I(x, gy, z) && close(x, gy, z)) {
            return true;
         }

         if (!state.getCollisionShape(init_Minecraft.level, init_BlockPos_MutableBl).isEmpty()) {
            return false;
         }
      }

      return false;
   }

   private static int read_I(int x, int feetY, int z) {
      for (int y = feetY + 1; y >= feetY - 3; y--) {
         if (write_Z(x, y, z)) {
            return y;
         }
      }

      for (int y = feetY + 1; y >= feetY - 3; y--) {
         if (init_Z_I(x, y, z) && load_Z(x, y + 1, z) && close(x, y, z)) {
            return y;
         }
      }

      for (int y = feetY + 1; y >= feetY - 3; y--) {
         BlockState state = init_Minecraft.level.getBlockState(init_BlockPos_MutableBl.set(x, y, z));
         if (!state.getFluidState().isEmpty() && !state.getFluidState().is(FluidTags.LAVA) && load_Z(x, y + 1, z)) {
            return y;
         }
      }

      return -2147483648;
   }

   private static int write_I(int x, int ty, int z) {
      if (write_Z(x, ty, z)) {
         return ty;
      } else if (write_Z(x, ty + 1, z)) {
         return ty + 1;
      } else {
         return write_Z(x, ty - 1, z) ? ty - 1 : -2147483648;
      }
   }

   private static final class Inner_DataUtil implements Comparable<bA.Inner_DataUtil> {
      final long init_J;
      final int init_I;

      Inner_DataUtil(long key, int f) {
         this.init_J = key;
         this.init_I = f;
      }

      public int compareTo(bA.Inner_DataUtil other) {
         return this.init(other);
      }

      public int init(bA.Inner_DataUtil other) {
         return Integer.compare(this.init_I, other.init_I);
      }
   }

   private static final class Inner_StreamUtil {
      final int init_I;
      final int load;
      final int read;
      final boolean init_Z;

      Inner_StreamUtil(int x, int y, int z, boolean jump) {
         this.init_I = x;
         this.load = y;
         this.read = z;
         this.init_Z = jump;
      }
   }
}
