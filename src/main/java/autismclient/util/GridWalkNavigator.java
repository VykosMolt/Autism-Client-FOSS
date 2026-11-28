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

public final class GridWalkNavigator {
   private static final Minecraft mc = Minecraft.getInstance();
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
   private static final float[] YAW_OFFSETS = new float[]{0.0F, -45.0F, 45.0F, -90.0F, 90.0F, -135.0F, 135.0F, 180.0F};
   private static final int[] KEY_MASKS = new int[]{1, 3, 9, 2, 8, 6, 12, 4};
   private static final int[] DX = new int[]{1, -1, 0, 0};
   private static final int[] DZ = new int[]{0, 0, 1, -1};
   private static final ArrayList<GridWalkNavigator.PathNode> path = new ArrayList<>();
   private static int pathIndex;
   private static BlockPos currentTarget;
   private static int nodeX;
   private static int nodeY;
   private static int nodeZ;
   private static final HashMap<Long, Integer> gScore = new HashMap<>();
   private static final HashMap<Long, Long> cameFrom = new HashMap<>();
   private static final HashSet<Long> init_HashSet = new HashSet<>();
   private static final HashSet<Long> closed = new HashSet<>();
   private static final PriorityQueue<GridWalkNavigator.OpenEntry> openQueue = new PriorityQueue<>();
   private static final ArrayList<Long> pathKeys = new ArrayList<>();
   private static final ArrayList<GridWalkNavigator.PathNode> read_ArrayList = new ArrayList<>();
   private static final HashSet<Long> goalKeys = new HashSet<>();
   private static final HashMap<Long, Boolean> standableCache = new HashMap<>();
   private static boolean cacheEnabled;
   private static final HashMap<BlockPos, Double> write_HashMap = new HashMap<>();
   private static int costCacheTick = -2147483648;
   private static final MutableBlockPos scratchPos = new MutableBlockPos();
   private static int targetX;
   private static int targetY;
   private static int targetZ;
   private static int searchOriginX;
   private static int searchOriginZ;
   private static BlockPos load_BlockPos;
   private static int planTick;
   private static double poll_D;
   private static double push_D;
   private static int stuckTicks;
   private static int jumpStreak;
   private static int heldKeyMask;
   private static boolean load_Z;
   private static final HashMap<BlockPos, Integer> update_HashMap = new HashMap<>();
   private static final HashMap<BlockPos, Integer> sync_HashMap = new HashMap<>();
   private static final HashMap<BlockPos, Integer> flush_HashMap = new HashMap<>();

   private GridWalkNavigator() {
   }

   public static boolean init_Z_BlockPos(BlockPos pos) {
      return pos != null && mc != null && mc.level != null && isStandable(pos.getX(), pos.getY(), pos.getZ());
   }

   public static boolean load(BlockPos target) {
      if (target != null && mc != null && mc.level != null && mc.player != null) {
         double dx = target.getX() + 0.5 - mc.player.getX();
         double dz = target.getZ() + 0.5 - mc.player.getZ();
         return dx * dx + dz * dz <= 1.44 && init_Z_Vec3_BlockPos(mc.player.getEyePosition(), target);
      } else {
         return false;
      }
   }

   public static double pathCost(BlockPos target) {
      if (target != null && mc != null && mc.level != null && mc.player != null && !write(target)) {
         int now = AutismSessionState.getInstance().getClientTick();
         if (costCacheTick != now) {
            costCacheTick = now;
            write_HashMap.clear();
         }

         Double cached = write_HashMap.get(target);
         if (cached != null) {
            return cached;
         }

         double cost = load(target) ? 0.0 : estimatePathCost(mc.player.getX(), mc.player.getY(), mc.player.getZ(), target, false, 1200);
         write_HashMap.put(target.immutable(), cost);
         return cost;
      } else {
         return 1.0 / 0.0;
      }
   }

   private static boolean init_Z_Vec3_BlockPos(Vec3 eye, BlockPos target) {
      Vec3 aim = Vec3.atCenterOf(target);
      double reach = mc.player.blockInteractionRange();
      if (eye.distanceToSqr(aim) > reach * reach) {
         return false;
      }

      BlockHitResult hit = mc.level.clip(new ClipContext(eye, aim, Block.OUTLINE, Fluid.NONE, mc.player));
      return hit.getType() == Type.MISS || hit.getBlockPos().equals(target);
   }

   public static void reset() {
      load();
      update_HashMap.clear();
      sync_HashMap.clear();
      flush_HashMap.clear();
   }

   public static void load() {
      releaseKeys();
      path.clear();
      pathIndex = 0;
      currentTarget = null;
      load_BlockPos = null;
      planTick = 0;
      stuckTicks = 0;
      jumpStreak = 0;
      gScore.clear();
      cameFrom.clear();
      init_HashSet.clear();
      closed.clear();
      openQueue.clear();
      pathKeys.clear();
      read_ArrayList.clear();
      goalKeys.clear();
      standableCache.clear();
      write_HashMap.clear();
      costCacheTick = -2147483648;
      load_Z = false;
   }

   public static boolean read(BlockPos target) {
      if (mc == null || mc.player == null || mc.level == null || mc.options == null) {
         releaseKeys();
         clearPath();
         currentTarget = null;
         return false;
      }

      if (target == null) {
         releaseKeys();
         clearPath();
         return false;
      }

      int now = AutismSessionState.getInstance().getClientTick();
      if (write(target)) {
         releaseKeys();
         clearPath();
         currentTarget = null;
         return false;
      }

      LocalPlayer player = mc.player;
      double px = player.getX();
      double py = player.getY();
      double pz = player.getZ();
      if (currentTarget == null || !currentTarget.equals(target)) {
         if (currentTarget != null && isNearPlannedTarget(target)) {
            currentTarget = target.immutable();
            if (pathIndex >= path.size() && heldKeyMask == 0) {
               stuckTicks = 0;
               jumpStreak = 0;
               poll_D = px;
               push_D = pz;
            }
         } else {
            releaseKeys();
            clearPath();
            currentTarget = target.immutable();
            stuckTicks = 0;
            jumpStreak = 0;
            poll_D = px;
            push_D = pz;
         }
      }

      if (load(currentTarget)) {
         flush_HashMap.remove(currentTarget);
         stuckTicks = 0;
         jumpStreak = 0;
         poll_D = px;
         push_D = pz;
         releaseKeys();
         clearPath();
         return false;
      }

      int feetY = Mth.floor(py);
      init_V_D(px, py, pz, feetY);
      boolean followGoal = pathIndex >= path.size()
         && !path.isEmpty()
         && init_Z()
         && init_Z_D(px, py, pz, currentTarget.getX() + 0.5, currentTarget.getZ() + 0.5);
      boolean needPlan = pathIndex >= path.size() && !followGoal;
      if (pathIndex < path.size()) {
         GridWalkNavigator.PathNode node = path.get(pathIndex);
         double segX = node.init_I - nodeX;
         double segZ = node.z - nodeZ;
         double fromX = nodeX + 0.5;
         double fromZ = nodeZ + 0.5;
         double lenSq = segX * segX + segZ * segZ;
         double t = lenSq < 1.0E-6 ? 0.0 : Mth.clamp(((px - fromX) * segX + (pz - fromZ) * segZ) / lenSq, 0.0, 1.0);
         double offX = px - (fromX + segX * t);
         double offZ = pz - (fromZ + segZ * t);
         if (offX * offX + offZ * offZ > 2.25 || feetY < node.y - 2) {
            needPlan = true;
         }

         if (!needPlan
            && !node.jump
            && node.y == nodeY
            && player.onGround()
            && !isFireOrPowderSnow(Mth.floor(px), feetY, Mth.floor(pz))
            && !isFireOrPowderSnow(Mth.floor(px), feetY + 1, Mth.floor(pz))
            && PathfindingGeometryHelper.init(floorHeight(nodeX, nodeY, nodeZ), floorHeight(node.init_I, node.y, node.z), 0.6)) {
            double nx = node.init_I + 0.5 - px;
            double nz = node.z + 0.5 - pz;
            double fraction = Math.min(1.0, 0.8 / Math.max(0.001, Math.hypot(nx, nz)));
            needPlan = !straightLineWalkable(px, py, pz, px + nx * fraction, pz + nz * fraction, false);
         }
      }

      if (needPlan && init_Z_BlockPos_I(currentTarget, now)) {
         markPlanned(currentTarget, now);
         if (!init_Z_D(px, py, pz)) {
            recordTargetFailure();
            releaseKeys();
            clearPath();
            return false;
         }

         pathIndex = 0;
         init_V_D(px, py, pz, feetY);
      } else if (needPlan) {
         releaseKeys();
         if (!autismclient.modules.AutoTotemModule.read_Z_3() && !autismclient.modules.AutoArmorModule.load_Z_9()) {
            double movedX = px - poll_D;
            double movedZ = pz - push_D;
            if (movedX * movedX + movedZ * movedZ >= 0.09) {
               poll_D = px;
               push_D = pz;
               stuckTicks = 0;
            } else if (++stuckTicks >= 15) {
               recordTargetFailure();
               releaseKeys();
               clearPath();
               return false;
            }

            return true;
         }

         poll_D = px;
         push_D = pz;
         stuckTicks = 0;
         return true;
      }

      if (pathIndex >= path.size() && !followGoal) {
         releaseKeys();
         load_Z = false;
         if (!path.isEmpty()) {
            clearPath();
         }

         return false;
      } else {
         int nodeX;
         int nodeY;
         int nodeZ;
         boolean nodeJump;
         if (pathIndex < path.size()) {
            GridWalkNavigator.PathNode node = path.get(pathIndex);
            nodeX = node.init_I;
            nodeY = node.y;
            nodeZ = node.z;
            nodeJump = node.jump;
            load_Z = false;
         } else {
            nodeX = currentTarget.getX();
            nodeY = currentTarget.getY();
            nodeZ = currentTarget.getZ();
            nodeJump = false;
            load_Z = true;
         }

         double dx = nodeX + 0.5 - px;
         double dz = nodeZ + 0.5 - pz;
         double distSq = dx * dx + dz * dz;
         float desiredYaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
         float delta = Mth.wrapDegrees(desiredYaw - player.getYRot());
         int mask = keyMaskFor(delta);
         double frac = py - feetY;
         int standCellY = frac > 0.5 ? feetY + 1 : feetY;
         boolean headroom = isPassable(Mth.floor(px), standCellY + 2, Mth.floor(pz));
         double jumpRangeSq = floorHeight(nodeX, nodeY, nodeZ) - py > 0.6 ? 3.61 : 2.5600000000000005;
         double liveRise = floorHeight(nodeX, nodeY, nodeZ) - py;
         if (liveRise <= -0.5 && !FarmlandTrampleGuard.init(mc.level, Mth.floor(px), standCellY, Mth.floor(pz), nodeX, nodeY, nodeZ, false, liveRise)) {
            releaseKeys();
            clearPath();
            return true;
         }

         boolean jump = headroom && nodeJump && distSq <= jumpRangeSq;
         if (!jump && headroom && !load_Z && stuckTicks >= 5) {
            jump = !init_Z_D_2(px, feetY, pz) && !isFarmland(nodeX, nodeY - 1, nodeZ);
         }

         if (jump && !FarmlandTrampleGuard.init(mc.level, Mth.floor(px), standCellY, Mth.floor(pz), nodeX, nodeY, nodeZ, true, 0.0)) {
            jump = false;
            if (nodeJump) {
               releaseKeys();
               clearPath();
               return true;
            }
         }

         int sprint = (mask & 1) != 0 && !jump ? 32 : 0;
         applyKeyMask(mask | (jump ? 16 : 0) | sprint);
         jumpStreak = jump ? jumpStreak + 1 : 0;
         if (!autismclient.modules.AutoTotemModule.read_Z_3() && !autismclient.modules.AutoArmorModule.load_Z_9()) {
            double movedX = px - poll_D;
            double movedZ = pz - push_D;
            if (movedX * movedX + movedZ * movedZ >= 0.09) {
               poll_D = px;
               push_D = pz;
               stuckTicks = 0;
            } else {
               stuckTicks++;
               if (stuckTicks >= 8 && jumpStreak >= 8 && init_Z_D(px, py, pz, standCellY, dx, dz, distSq)) {
                  recordTargetFailure();
                  releaseKeys();
                  clearPath();
                  return false;
               }

               if (stuckTicks >= 15) {
                  recordTargetFailure();
                  releaseKeys();
                  clearPath();
                  return false;
               }
            }

            return true;
         } else {
            poll_D = px;
            push_D = pz;
            stuckTicks = 0;
            return true;
         }
      }
   }

   public static BlockPos nextNode() {
      if (currentTarget == null) {
         return null;
      } else if (pathIndex < path.size()) {
         GridWalkNavigator.PathNode node = path.get(pathIndex);
         return new BlockPos(node.init_I, node.y, node.z);
      } else {
         return load_Z ? currentTarget : null;
      }
   }

   public static void init_V_BlockPos_I(BlockPos target, int ticks) {
      if (target != null && ticks > 0) {
         BlockPos key = target.immutable();
         update_HashMap.put(key, AutismSessionState.getInstance().getClientTick());
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

      int age = AutismSessionState.getInstance().getClientTick() - stamp;
      if (age >= 0 && age <= sync_HashMap.getOrDefault(target, 0)) {
         return true;
      }

      update_HashMap.remove(target);
      sync_HashMap.remove(target);
      return false;
   }

   private static void recordTargetFailure() {
      int fails = flush_HashMap.merge(currentTarget, 1, Integer::sum);
      int ticks = Math.min(100 << Math.min(fails - 1, 2), 400);
      init_V_BlockPos_I(currentTarget, ticks);
   }

   private static int keyMaskFor(float delta) {
      if (Math.abs(delta) <= 12.0F) {
         return 1;
      }

      int best = 0;
      float bestError = 3.4028235E38F;

      for (int i = 0; i < KEY_MASKS.length; i++) {
         float error = Math.abs(Mth.wrapDegrees(delta - YAW_OFFSETS[i]));
         if (error < bestError) {
            bestError = error;
            best = i;
         }
      }

      return KEY_MASKS[best];
   }

   private static void applyKeyMask(int mask) {
      if (mc.options == null) {
         heldKeyMask = 0;
      } else {
         int changed = mask ^ heldKeyMask;
         if (changed != 0) {
            if ((changed & 1) != 0) {
               setKey(mc.options.keyUp, (mask & 1) != 0);
            }

            if ((changed & 2) != 0) {
               setKey(mc.options.keyLeft, (mask & 2) != 0);
            }

            if ((changed & 4) != 0) {
               setKey(mc.options.keyDown, (mask & 4) != 0);
            }

            if ((changed & 8) != 0) {
               setKey(mc.options.keyRight, (mask & 8) != 0);
            }

            if ((changed & 16) != 0) {
               setKey(mc.options.keyJump, (mask & 16) != 0);
            }

            if ((changed & 32) != 0) {
               setKey(mc.options.keySprint, (mask & 32) != 0);
            }

            heldKeyMask = mask;
         }
      }
   }

   private static void setKey(KeyMapping key, boolean down) {
      if (down) {
         key.setDown(true);
      } else {
         KeyMappingControl.of(key).autism$resetPressedState();
      }
   }

   private static void releaseKeys() {
      applyKeyMask(0);
   }

   public static void read() {
      heldKeyMask = 0;
   }

   private static void clearPath() {
      path.clear();
      pathIndex = 0;
      load_Z = false;
   }

   private static void init_V_D(double px, double py, double pz, int feetY) {
      while (pathIndex < path.size() && (pathIndex != path.size() - 1 || init_Z())) {
         GridWalkNavigator.PathNode node = path.get(pathIndex);
         double dx = node.init_I + 0.5 - px;
         double dz = node.z + 0.5 - pz;
         if (dx * dx + dz * dz < 0.25 && Math.abs(feetY - node.y) <= 1 && (!node.jump || init_Z_bA_StreamUtil_D(node, py))) {
            nodeX = node.init_I;
            nodeY = node.y;
            nodeZ = node.z;
            pathIndex++;
            continue;
         }
         break;
      }
   }

   private static boolean init_Z_bA_StreamUtil_D(GridWalkNavigator.PathNode node, double py) {
      double ledgeTop = floorHeight(node.init_I, node.y, node.z);
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

   private static boolean isNearPlannedTarget(BlockPos target) {
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
      return load_BlockPos != null && !load_BlockPos.equals(currentTarget) && isNearPlannedTarget(currentTarget);
   }

   private static boolean init_Z_BlockPos_I(BlockPos target, int now) {
      return load_BlockPos != null && load_BlockPos.equals(target) ? now - planTick >= 10 : true;
   }

   private static void markPlanned(BlockPos target, int now) {
      load_BlockPos = target;
      planTick = now;
   }

   private static boolean init_Z_D(double px, double py, double pz) {
      clearPath();
      return Double.isFinite(estimatePathCost(px, py, pz, currentTarget, true, 6000));
   }

   private static double estimatePathCost(double px, double py, double pz, BlockPos target, boolean materialize, int expansionLimit) {
      gScore.clear();
      cameFrom.clear();
      init_HashSet.clear();
      closed.clear();
      openQueue.clear();
      pathKeys.clear();
      read_ArrayList.clear();
      goalKeys.clear();
      standableCache.clear();
      cacheEnabled = true;

      try {
         return computePath(px, py, pz, target, materialize, expansionLimit);
      } finally {
         cacheEnabled = false;
      }
   }

   private static double computePath(double px, double py, double pz, BlockPos target, boolean materialize, int expansionLimit) {
      int startX = Mth.floor(px);
      int startZ = Mth.floor(pz);
      int startY = findLandingY(startX, Mth.floor(py), startZ);
      if (startY == -2147483648) {
         return 1.0 / 0.0;
      }

      searchOriginX = startX;
      GridWalkNavigator.searchOriginZ = startZ;
      targetX = target.getX();
      targetZ = target.getZ();
      targetY = target.getY();
      init_V_I_2(targetX, targetZ, target);

      for (int d = 0; d < 4; d++) {
         init_V_I_2(targetX + DX[d], targetZ + DZ[d], target);
      }

      if (goalKeys.isEmpty()) {
         return 1.0 / 0.0;
      }

      if (Math.abs(targetX - startX) + Math.abs(targetZ - startZ) > 41) {
         return 1.0 / 0.0;
      }

      long startKey = BlockPos.asLong(startX, startY, startZ);
      if (materialize) {
         nodeX = startX;
         nodeY = startY;
         nodeZ = startZ;
      }

      gScore.put(startKey, 0);
      openQueue.add(new GridWalkNavigator.OpenEntry(startKey, heuristic(startX, startY, startZ)));
      int expanded = 0;

      while (!openQueue.isEmpty() && expanded < expansionLimit) {
         GridWalkNavigator.OpenEntry entry = openQueue.poll();
         if (closed.add(entry.key)) {
            expanded++;
            if (goalKeys.contains(entry.key)) {
               if (materialize) {
                  reconstructPath(startKey, entry.key, startX, startY, startZ);
                  if (path.isEmpty()) {
                     path.add(new GridWalkNavigator.PathNode(startX, startY, startZ, false));
                  }
               }

               return gScore.get(entry.key).intValue();
            }

            expand(entry.key);
         }
      }

      return 1.0 / 0.0;
   }

   private static void init_V_I_2(int x, int z, BlockPos target) {
      double eyeHeight = mc.player.getEyeY() - mc.player.getY();

      for (int y = target.getY() + 1; y >= target.getY() - 3; y--) {
         if (isStandable(x, y, z) && init_Z_Vec3_BlockPos(new Vec3(x + 0.5, floorHeight(x, y, z) + eyeHeight, z + 0.5), target)) {
            goalKeys.add(BlockPos.asLong(x, y, z));
         }
      }
   }

   private static void expand(long currentKey) {
      int cx = BlockPos.getX(currentKey);
      int cy = BlockPos.getY(currentKey);
      int cz = BlockPos.getZ(currentKey);
      int gCurrent = gScore.get(currentKey);

      for (int d = 0; d < 4; d++) {
         int nx = cx + DX[d];
         int nz = cz + DZ[d];
         if (Math.abs(nx - searchOriginX) + Math.abs(nz - searchOriginZ) <= 40) {
            double fromFloor = floorHeight(cx, cy, cz);
            double rise = floorHeight(nx, cy + 1, nz) - fromFloor;
            boolean hop = rise > 0.6;
            if (isStandable(nx, cy + 1, nz)
               && (!hop || isPassable(cx, cy + 2, cz))
               && rise <= 1.25
               && FarmlandTrampleGuard.init(mc.level, cx, cy, cz, nx, cy + 1, nz, hop, rise)) {
               relaxNeighbor(currentKey, gCurrent, nx, cy + 1, nz, (hop ? 14 : 10) + (hop && isFarmland(nx, cy, nz) ? 26 : 0), hop);
            }

            for (int ny = cy; ny >= cy - 3 && init_Z_I(nx, ny, nz) && isPassable(nx, ny + 1, nz) && !isFireOrPowderSnow(nx, ny, nz) && !isFireOrPowderSnow(nx, ny + 1, nz); ny--) {
               if (isStandableSurface(nx, ny, nz)) {
                  double toFloor = floorHeight(nx, ny, nz);
                  double floorRise = toFloor - fromFloor;
                  boolean needsHop = floorRise > 0.6;
                  if (isStandable(nx, ny, nz)
                     && floorRise <= 1.25
                     && floorRise >= -3.0
                     && (!needsHop || isPassable(cx, cy + 2, cz))
                     && FarmlandTrampleGuard.init(mc.level, cx, cy, cz, nx, ny, nz, needsHop, floorRise)) {
                     boolean trample = (needsHop || floorRise <= -0.5) && isFarmland(nx, ny - 1, nz);
                     relaxNeighbor(
                        currentKey, gCurrent, nx, ny, nz, 10 + (needsHop ? 4 : (int)Math.ceil(Math.max(0.0, -floorRise) * 2.0)) + (trample ? 26 : 0), needsHop
                     );
                  }
                  break;
               }
            }

            int jx = cx + DX[d] * 2;
            int jz = cz + DZ[d] * 2;
            if (Math.abs(jx - searchOriginX) + Math.abs(jz - searchOriginZ) <= 40
               && !isStandable(nx, cy, nz)
               && isStandable(jx, cy, jz)
               && isPassable(cx, cy + 2, cz)
               && isPassable(jx, cy + 2, jz)
               && isPassable(nx, cy + 1, nz)
               && isPassable(nx, cy + 2, nz)
               && !isFireOrPowderSnow(nx, cy + 1, nz)
               && !isFireOrPowderSnow(nx, cy + 2, nz)
               && Math.abs(floorHeight(jx, cy, jz) - fromFloor) < 0.0625
               && hasSafeLandingBelow(nx, cy, nz)
               && FarmlandTrampleGuard.init(mc.level, cx, cy, cz, jx, cy, jz, true, 0.0)) {
               relaxNeighbor(currentKey, gCurrent, jx, cy, jz, 18 + (isFarmland(jx, cy - 1, jz) ? 26 : 0), true);
            }
         }
      }
   }

   private static void relaxNeighbor(long fromKey, int gFrom, int nx, int ny, int nz, int stepCost, boolean jump) {
      long key = BlockPos.asLong(nx, ny, nz);
      if (!closed.contains(key)) {
         int g = gFrom + stepCost + hazardCost(nx, ny, nz);
         Integer old = gScore.get(key);
         if (old == null || old > g) {
            gScore.put(key, g);
            cameFrom.put(key, fromKey);
            if (jump) {
               init_HashSet.add(key);
            } else {
               init_HashSet.remove(key);
            }

            openQueue.add(new GridWalkNavigator.OpenEntry(key, g + heuristic(nx, ny, nz)));
         }
      }
   }

   private static int hazardCost(int x, int y, int z) {
      int cost = 0;
      if (isContactHazard(x, y, z)) {
         cost += 30;
      }

      if (isFarmland(x, y - 1, z)) {
         cost += 4;
      }

      return cost;
   }

   private static int heuristic(int x, int y, int z) {
      int best = 2147483647;

      for (long key : goalKeys) {
         best = Math.min(best, PathfindingGeometryHelper.init(x, z, BlockPos.getX(key), BlockPos.getZ(key)));
      }

      return best;
   }

   private static void reconstructPath(long startKey, long goalKey, int startX, int startY, int startZ) {
      pathKeys.clear();

      for (long key = goalKey; key != startKey; key = cameFrom.get(key)) {
         pathKeys.add(key);
      }

      path.clear();

      for (int i = pathKeys.size() - 1; i >= 0; i--) {
         long nodeKey = pathKeys.get(i);
         path.add(new GridWalkNavigator.PathNode(BlockPos.getX(nodeKey), BlockPos.getY(nodeKey), BlockPos.getZ(nodeKey), init_HashSet.contains(nodeKey)));
      }

      init_V_I(startX, startY, startZ);
   }

   private static void init_V_I(int startX, int startY, int startZ) {
      if (path.size() >= 3) {
         read_ArrayList.clear();
         int anchorX = startX;
         int anchorY = startY;
         int anchorZ = startZ;
         int i = 0;

         while (i < path.size()) {
            int far = i;
            int limit = Math.min(path.size() - 1, i + 12);

            for (int j = limit; j > i; j--) {
               GridWalkNavigator.PathNode candidate = path.get(j);
               if (!candidate.jump && candidate.y == anchorY && init_Z_I(anchorX, anchorY, anchorZ, candidate.init_I, candidate.z)) {
                  far = j;
                  break;
               }
            }

            GridWalkNavigator.PathNode next = path.get(far);
            read_ArrayList.add(next);
            anchorX = next.init_I;
            anchorY = next.y;
            anchorZ = next.z;
            i = far + 1;
         }

         path.clear();
         path.addAll(read_ArrayList);
         read_ArrayList.clear();
      }
   }

   private static boolean init_Z_I(int ax, int ay, int az, int bx, int bz) {
      return init_Z_D(ax + 0.5, floorHeight(ax, ay, az), az + 0.5, bx + 0.5, bz + 0.5);
   }

   private static boolean init_Z_D(double ax, double floorY, double az, double bx, double bz) {
      return straightLineWalkable(ax, floorY, az, bx, bz, true);
   }

   private static boolean straightLineWalkable(double ax, double floorY, double az, double bx, double bz, boolean avoidHazards) {
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
         if (cellY == -2147483648 || avoidHazards && isContactHazard(cx, cellY, cz)) {
            return false;
         }

         double nextFloor = floorHeight(cx, cellY, cz);
         if (!autismclient.util.PathfindingGeometryHelper.init(lastFloor, nextFloor, 0.6)) {
            return false;
         }

         if (!segmentClear(lastX, lastZ, x, z, Math.max(lastFloor, nextFloor))) {
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
         if (isStandable(x, y, z) && PathfindingGeometryHelper.init(fromFloor, floorHeight(x, y, z), 0.6)) {
            return y;
         }
      }

      return -2147483648;
   }

   private static boolean segmentClear(double ax, double az, double bx, double bz, double floor) {
      double half = 0.29999;
      AABB bounds = new AABB(
         Math.min(ax, bx) - half, floor + 1.0E-5, Math.min(az, bz) - half, Math.max(ax, bx) + half, floor + 1.8 - 1.0E-5, Math.max(az, bz) + half
      );

      for (VoxelShape shape : mc.level.getBlockCollisions(mc.player, bounds)) {
         for (AABB box : shape.toAabbs()) {
            if (!(box.maxY <= bounds.minY)
               && !(box.minY >= bounds.maxY)
               && autismclient.util.PathfindingGeometryHelper.init(ax, az, bx, bz, box.minX - half, box.minZ - half, box.maxX + half, box.maxZ + half)) {
               return false;
            }
         }
      }

      return true;
   }

   private static boolean init_Z_I(int x, int y, int z) {
      BlockState state = mc.level.getBlockState(scratchPos.set(x, y, z));
      if (!state.getFluidState().isEmpty()) {
         return false;
      } else {
         VoxelShape shape = state.getCollisionShape(mc.level, scratchPos);
         if (shape.isEmpty() || !(shape.min(Axis.Y) > 1.0E-4) && !(shape.max(Axis.Y) > 0.6)) {
            VoxelShape below = mc.level
               .getBlockState(scratchPos.set(x, y - 1, z))
               .getCollisionShape(mc.level, scratchPos);
            return below.isEmpty() || below.max(Axis.Y) <= 1.0001;
         } else {
            return false;
         }
      }
   }

   private static boolean isPassable(int x, int y, int z) {
      BlockState state = mc.level.getBlockState(scratchPos.set(x, y, z));
      return !state.getFluidState().isEmpty() ? false : state.getCollisionShape(mc.level, scratchPos).isEmpty();
   }

   private static boolean isSturdyFloor(int x, int y, int z) {
      BlockState state = mc.level.getBlockState(scratchPos.set(x, y, z));
      if (state.isFaceSturdy(mc.level, scratchPos, Direction.UP)) {
         return true;
      }

      VoxelShape shape = state.getCollisionShape(mc.level, scratchPos);
      return !shape.isEmpty() && shape.max(Axis.Y) >= 0.5;
   }

   private static boolean isStandable(int x, int y, int z) {
      long key = BlockPos.asLong(x, y, z);
      if (cacheEnabled) {
         Boolean cached = standableCache.get(key);
         if (cached != null) {
            return cached;
         }
      }

      boolean result = y >= mc.level.getMinY()
         && y < mc.level.getMaxY()
         && mc.level.hasChunkAt(scratchPos.set(x, y, z))
         && init_Z_I(x, y, z)
         && isPassable(x, y + 1, z)
         && isStandableSurface(x, y, z)
         && !isFireOrPowderSnow(x, y, z)
         && !isFireOrPowderSnow(x, y + 1, z)
         && segmentClear(x + 0.5, z + 0.5, x + 0.5, z + 0.5, floorHeight(x, y, z));
      if (cacheEnabled) {
         standableCache.put(key, result);
      }

      return result;
   }

   private static boolean isFireOrPowderSnow(int x, int y, int z) {
      BlockState state = mc.level.getBlockState(scratchPos.set(x, y, z));
      return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.POWDER_SNOW);
   }

   private static boolean isContactHazard(int x, int y, int z) {
      if (mc.level.getBlockState(scratchPos.set(x, y, z)).is(Blocks.SWEET_BERRY_BUSH)) {
         return true;
      }

      BlockState floor = mc.level.getBlockState(scratchPos.set(x, y - 1, z));
      return floor.is(Blocks.MAGMA_BLOCK) || floor.is(Blocks.CACTUS);
   }

   private static boolean isFarmland(int x, int y, int z) {
      return mc.level.getBlockState(scratchPos.set(x, y, z)).is(Blocks.FARMLAND);
   }

   private static boolean init_Z_D_2(double px, int feetY, double pz) {
      int x = Mth.floor(px);
      int z = Mth.floor(pz);
      return isFarmland(x, feetY - 1, z) || isFarmland(x, feetY, z);
   }

   private static boolean isStandableSurface(int x, int y, int z) {
      if (isSturdyFloor(x, y - 1, z)) {
         return true;
      }

      BlockState state = mc.level.getBlockState(scratchPos.set(x, y, z));
      VoxelShape shape = state.getCollisionShape(mc.level, scratchPos);
      return !shape.isEmpty() && shape.min(Axis.Y) <= 1.0E-4 && shape.max(Axis.Y) <= 0.6;
   }

   private static double init_D_I(int x, int y, int z) {
      VoxelShape shape = mc.level
         .getBlockState(scratchPos.set(x, y, z))
         .getCollisionShape(mc.level, scratchPos);
      return shape.isEmpty() ? 0.0 : shape.max(Axis.Y);
   }

   private static double floorHeight(int x, int y, int z) {
      BlockState state = mc.level.getBlockState(scratchPos.set(x, y, z));
      if (!state.getFluidState().isEmpty()) {
         return y;
      }

      VoxelShape shape = state.getCollisionShape(mc.level, scratchPos);
      return !shape.isEmpty() && shape.min(Axis.Y) <= 1.0E-4 && shape.max(Axis.Y) <= 0.6 ? y + shape.max(Axis.Y) : y - 1 + init_D_I(x, y - 1, z);
   }

   private static boolean hasSafeLandingBelow(int x, int y, int z) {
      for (int gy = y; gy >= y - 3; gy--) {
         BlockState state = mc.level.getBlockState(scratchPos.set(x, gy, z));
         if (!state.getFluidState().isEmpty()) {
            return !state.getFluidState().is(FluidTags.LAVA);
         }

         if (isFireOrPowderSnow(x, gy, z)) {
            return false;
         }

         if (init_Z_I(x, gy, z) && isStandableSurface(x, gy, z)) {
            return true;
         }

         if (!state.getCollisionShape(mc.level, scratchPos).isEmpty()) {
            return false;
         }
      }

      return false;
   }

   private static int findLandingY(int x, int feetY, int z) {
      for (int y = feetY + 1; y >= feetY - 3; y--) {
         if (isStandable(x, y, z)) {
            return y;
         }
      }

      for (int y = feetY + 1; y >= feetY - 3; y--) {
         if (init_Z_I(x, y, z) && isPassable(x, y + 1, z) && isStandableSurface(x, y, z)) {
            return y;
         }
      }

      for (int y = feetY + 1; y >= feetY - 3; y--) {
         BlockState state = mc.level.getBlockState(scratchPos.set(x, y, z));
         if (!state.getFluidState().isEmpty() && !state.getFluidState().is(FluidTags.LAVA) && isPassable(x, y + 1, z)) {
            return y;
         }
      }

      return -2147483648;
   }

   private static int standableY(int x, int ty, int z) {
      if (isStandable(x, ty, z)) {
         return ty;
      } else if (isStandable(x, ty + 1, z)) {
         return ty + 1;
      } else {
         return isStandable(x, ty - 1, z) ? ty - 1 : -2147483648;
      }
   }

   private static final class OpenEntry implements Comparable<GridWalkNavigator.OpenEntry> {
      final long key;
      final int fScore;

      OpenEntry(long key, int f) {
         this.key = key;
         this.fScore = f;
      }

      public int compareTo(GridWalkNavigator.OpenEntry other) {
         return this.compareScore(other);
      }

      public int compareScore(GridWalkNavigator.OpenEntry other) {
         return Integer.compare(this.fScore, other.fScore);
      }
   }

   private static final class PathNode {
      final int init_I;
      final int y;
      final int z;
      final boolean jump;

      PathNode(int x, int y, int z, boolean jump) {
         this.init_I = x;
         this.y = y;
         this.z = z;
         this.jump = jump;
      }
   }
}
