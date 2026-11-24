package autismclient.ai.a;

import autismclient.AutismClientAddon;
import autismclient.ai.A;
import autismclient.ai.aF;
import autismclient.ai.aV;
import autismclient.ai.ag;
import autismclient.ai.cG;
import autismclient.ai.ca;
import autismclient.api.ai.AiOperation;
import autismclient.modules.Module;
import autismclient.modules.aP;
import autismclient.modules.al;
import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismWaypoints;
import autismclient.util.ce;
import autismclient.util.cx;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class HashUtil {
   private static final ItemStack[] init_ItemStackArr = new ItemStack[9];
   private static volatile long load = -9223372036854775808L;
   private static volatile int init_I;
   private static volatile long read;
   public static final String init_Str = "Player is wedged in solid blocks";
   static final boolean init_Z = Boolean.getBoolean("autism.ai.snapshotGrid");
   static final long init_J = 30000000000L;
   private static volatile long write;

   private HashUtil() {
   }

   static boolean init(RefQueue.Inner_TaskQueue status, Vec3 motion) {
      return status != RefQueue.Inner_TaskQueue.load && motion.horizontalDistanceSqr() <= 4.0E-4;
   }

   private static void init_V(BlockPos feet) {
      long now = System.nanoTime();
      if (feet.asLong() == load && now - read < 30000000000L) {
         init_I++;
      } else {
         load = feet.asLong();
         init_I = 1;
      }

      read = now;
   }

   public static int init_I(BlockPos feet) {
      return feet.asLong() == load && System.nanoTime() - read < 30000000000L ? init_I : 0;
   }

   static boolean init_Z_ClassCache_JsonUti(ClassCache.Inner_JsonUtil goal, boolean allowPillar, boolean allowBridge, BlockPos cell) {
      return (allowPillar || allowBridge) && goal.read(cell);
   }

   static boolean init(ClassCache.Inner_PacketUtil segment) {
      return segment.exhausted() && segment.movements().isEmpty();
   }

   static boolean init_Z_VoxelShape(VoxelShape floor, VoxelShape feet, VoxelShape head) {
      if (floor.isEmpty()) {
         return true;
      }

      for (VoxelShape shape : new VoxelShape[]{floor, feet, head}) {
         if (!shape.isEmpty() && !Block.isShapeFullBlock(shape)) {
            return true;
         }
      }

      return false;
   }

   static boolean init(VoxelShape floor, VoxelShape lower) {
      return floor.isEmpty() && !lower.isEmpty() && lower.max(Axis.Y) > 1.0;
   }

   public static BlockPos init_BlockPos(BlockPos target, int range) {
      if (init_Z(target)) {
         return target;
      }

      for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
         BlockPos lateral = target.offset(d[0], 0, d[1]);
         if (init_Z(lateral)) {
            return lateral;
         }
      }

      for (int d = 1; d <= range; d++) {
         BlockPos up = target.above(d);
         BlockPos down = target.below(d);
         if (init_Z(up)) {
            return up;
         }

         if (init_Z(down)) {
            return down;
         }
      }

      return target;
   }

   public static boolean init_Z(BlockPos cell) {
      Minecraft mc = Minecraft.getInstance();
      return mc.level == null
         ? false
         : ClassCache.Inner_RenderState.init_Z_ClassCache_FileCac(
            ClassCache.Inner_StreamUtil.init_ClassCache_FileCac_Minecraft(mc, mc.level, cell, init_ItemStackArr),
            ClassCache.Inner_StreamUtil.init_ClassCache_FileCac_Minecraft(mc, mc.level, cell.above(), init_ItemStackArr),
            ClassCache.Inner_StreamUtil.init_ClassCache_FileCac_Minecraft(mc, mc.level, cell.below(), init_ItemStackArr)
         );
   }

   static BlockPos init(BlockPos feet, Predicate<BlockPos> breathable) {
      BlockPos best = null;
      double bestSqr = 1.7976931348623157E308;

      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = -1; dy <= 2; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
               if (dx != 0 || dy != 0 || dz != 0) {
                  BlockPos cell = feet.offset(dx, dy, dz);
                  if (breathable.test(cell)) {
                     double distSqr = cell.distSqr(feet);
                     if (distSqr < bestSqr) {
                        bestSqr = distSqr;
                        best = cell;
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   public static HashUtil.Inner_CachePool init_HashUtil_CachePool_BlockPos(
      BlockPos anchor, ClassCache.Inner_JsonUtil goal, int timeoutSeconds, HashUtil.Inner_BufferHelper options
   ) {
      return init(anchor, 0.6, true, true, true, timeoutSeconds, ClassCache.Inner_IoWorker.load, true, 2.0, goal, options, true);
   }

   public static void init_V_HashUtil_CachePool(HashUtil.Inner_CachePool travel, Function<Predicate<AABB>, Vec3> look, Runnable click) {
      if (!AutismLiteVariant.enabled()) {
         o.init(travel, look, click);
      }
   }

   public static HashUtil.Inner_CachePool init_HashUtil_CachePool_BlockPos(BlockPos anchor, ClassCache.Inner_JsonUtil goal, int timeoutSeconds) {
      return init(anchor, 0.6, true, true, true, timeoutSeconds, ClassCache.Inner_IoWorker.load, true, 2.0, goal, HashUtil.Inner_BufferHelper.DEFAULT, true);
   }

   static boolean init(Entity entity) {
      if (entity instanceof ZombifiedPiglin zp) {
         return !zp.isAggressive();
      } else if (!(entity instanceof Enderman enderman)) {
         return entity instanceof Piglin piglin ? !piglin.isAggressive() : false;
      } else {
         return !enderman.isCreepy() && !enderman.isAggressive();
      }
   }

   public static AiOperation init(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos target = init(args, mc);
         double radius = Math.max(0.6, Math.min(8.0, ag.init_D_JsonObject(args, "radius", 1.5)));
         boolean parkour = !AutismLiteVariant.enabled() && ag.init_Z_JsonObject(args, "parkour", false);
         boolean allowDig = !parkour && ag.init_Z_JsonObject(args, "allowDig", true);
         boolean allowPillar = !parkour && ag.init_Z_JsonObject(args, "allowPillar", true);
         boolean allowBridge = !parkour && ag.init_Z_JsonObject(args, "allowBridge", true);
         boolean allowInventory = ag.init_Z_JsonObject(args, "allowInventory", false);
         boolean yOnly = ag.init_Z_JsonObject(args, "yOnly", false);
         boolean interactOnArrival = ag.init_Z_JsonObject(args, "interactOnArrival", false);
         int timeoutSeconds = args.has("timeoutSeconds") ? Math.max(1, Math.min(3600, ag.init_I_JsonObject(args, "timeoutSeconds", 300))) : 0;
         int digReluctance = Math.max(0, Math.min(60, ag.init_I_JsonObject(args, "digReluctance", 2)));
         ClassCache.Inner_IoWorker strategy = ClassCache.Inner_IoWorker.init(ag.init_Str_JsonObject(args, "strategy", "fastest"));
         HashUtil.Inner_BufferHelper options = new HashUtil.Inner_BufferHelper(
            ag.init_Z_JsonObject(args, "sprint", true),
            ag.init_Z_JsonObject(args, "bunnyhop", true),
            Math.max(0, Math.min(3, ag.init_I_JsonObject(args, "maxFall", 3))),
            ag.init_Z_JsonObject(args, "avoidWater", false),
            ag.init_Z_JsonObject(args, "allowClimb", true),
            ag.init_Z_JsonObject(args, "allowDoors", true)
         );
         AiOperation op = init(
            target,
            radius,
            allowDig,
            allowPillar,
            allowBridge,
            timeoutSeconds,
            strategy,
            allowInventory,
            digReluctance,
            yOnly ? ClassCache.Inner_JsonUtil.init_ClassCache_JsonUti_I_BlockPos(target.getY(), target) : null,
            options,
            false,
            true,
            parkour,
            parkour && ag.init_Z_JsonObject(args, "assumeNoFallDamage", false)
         );
         return !AutismLiteVariant.enabled() && interactOnArrival ? init(op, target) : op;
      } else {
         throw new IllegalArgumentException("Engine travel requires a connected living player");
      }
   }

   static BlockPos init(JsonObject args, Minecraft mc) {
      BlockPos feet = mc.player.blockPosition();
      if (args.has("x") && args.has("y") && args.has("z")) {
         return new BlockPos(ag.init_I_JsonObject(args, "x", 0), ag.init_I_JsonObject(args, "y", 0), ag.init_I_JsonObject(args, "z", 0));
      }

      if (args.has("x") && args.has("z")) {
         BlockPos column = new BlockPos(ag.init_I_JsonObject(args, "x", 0), feet.getY(), ag.init_I_JsonObject(args, "z", 0));
         BlockPos standable = init_BlockPos(column, 24);
         return standable != null ? standable : column;
      }

      if (args.has("dx") || args.has("dy") || args.has("dz")) {
         return feet.offset(ag.init_I_JsonObject(args, "dx", 0), ag.init_I_JsonObject(args, "dy", 0), ag.init_I_JsonObject(args, "dz", 0));
      }

      if (args.has("direction")) {
         double distance = ag.init_D_JsonObject(args, "distance", 10.0);
         if (distance > 0.0 && !(distance > 512.0)) {
            return feet.offset(init_BlockPos_Str(ag.init_Str_JsonObject(args, "direction", ""), mc.player.getYRot(), distance));
         } else {
            throw new IllegalArgumentException("distance must be 1-512 blocks");
         }
      } else if (args.has("waypoint")) {
         String name = ag.init_Str_JsonObject(args, "waypoint", "").trim();
         AutismWaypoints store = AutismWaypoints.init_AutismWaypoints();
         String scope = AuditWaypointScope.current(mc);
         AutismWaypoints.Waypoint wp = store.init_AutismWaypoints_Wa(scope, name);
         if (wp != null) {
            return new BlockPos(wp.x(), wp.y(), wp.z());
         }

         List<String> names = new ArrayList<>();

         for (AutismWaypoints.Waypoint w : store.init_List_Str(scope)) {
            names.add(w.name());
         }

         throw new IllegalArgumentException("No waypoint named '" + name + "' in this world; known: " + names);
      } else if (args.has("entity")) {
         Entity entity = mc.level.getEntity(ag.init_I_JsonObject(args, "entity", -1));
         if (entity == null) {
            throw new IllegalArgumentException("No loaded entity with id " + ag.init_I_JsonObject(args, "entity", -1));
         } else {
            return entity.blockPosition();
         }
      } else {
         if (!args.has("player")) {
            throw new IllegalArgumentException("A destination is required: x,y,z (or x,z), dx/dy/dz, direction+distance, waypoint, entity or player");
         }

         String name = ag.init_Str_JsonObject(args, "player", "").trim();

         for (AbstractClientPlayer p : mc.level.players()) {
            if (p.getName().getString().equalsIgnoreCase(name) || p.getGameProfile().name().equalsIgnoreCase(name)) {
               return p.blockPosition();
            }
         }

         throw new IllegalArgumentException("No loaded player named '" + name + "'");
      }
   }

   static BlockPos init_BlockPos_Str(String direction, float yaw, double distance) {
      String dir = direction.toLowerCase(Locale.ROOT).trim();
      double fx = -Math.sin(Math.toRadians(yaw));
      double fz = Math.cos(Math.toRadians(yaw));
      double dy = 0.0;
      double dx;
      double dz;
      switch (dir) {
         case "north":
         case "n":
            dx = 0.0;
            dz = -1.0;
            break;
         case "south":
         case "s":
            dx = 0.0;
            dz = 1.0;
            break;
         case "east":
         case "e":
            dx = 1.0;
            dz = 0.0;
            break;
         case "west":
         case "w":
            dx = -1.0;
            dz = 0.0;
            break;
         case "forward":
         case "forwards":
         case "ahead":
         case "straight":
            dx = fx;
            dz = fz;
            break;
         case "back":
         case "backward":
         case "backwards":
            dx = -fx;
            dz = -fz;
            break;
         case "left":
            dx = fz;
            dz = -fx;
            break;
         case "right":
            dx = -fz;
            dz = fx;
            break;
         case "up":
            dx = 0.0;
            dz = 0.0;
            dy = distance;
            break;
         case "down":
            dx = 0.0;
            dz = 0.0;
            dy = -distance;
            break;
         default:
            throw new IllegalArgumentException("Unknown direction '" + direction + "' (north|south|east|west|forward|back|left|right|up|down)");
      }

      return new BlockPos((int)Math.round(dx * distance), (int)Math.round(dy), (int)Math.round(dz * distance));
   }

   static boolean init(Block block) {
      String id = BuiltInRegistries.BLOCK.getKey(block).toString();
      boolean var10000;
      switch (id) {
         case "minecraft:chest":
         case "minecraft:trapped_chest":
         case "minecraft:ender_chest":
         case "minecraft:barrel":
         case "minecraft:furnace":
         case "minecraft:blast_furnace":
         case "minecraft:smoker":
         case "minecraft:crafting_table":
         case "minecraft:enchanting_table":
         case "minecraft:brewing_stand":
         case "minecraft:stonecutter":
         case "minecraft:grindstone":
         case "minecraft:loom":
         case "minecraft:cartography_table":
         case "minecraft:smithing_table":
         case "minecraft:anvil":
         case "minecraft:chipped_anvil":
         case "minecraft:damaged_anvil":
         case "minecraft:hopper":
         case "minecraft:dispenser":
         case "minecraft:dropper":
         case "minecraft:beacon":
            var10000 = true;
            break;
         default:
            var10000 = id.equals("minecraft:shulker_box") || id.startsWith("minecraft:") && id.endsWith("_shulker_box");
      }

      return var10000;
   }

   static AiOperation init(final AiOperation inner, final BlockPos target) {
      return new AiOperation() {
         private AiOperation load;

         @Override
         public AiOperation.Result tick() {
            if (this.load != null) {
               AiOperation.Result r = this.load.tick();
               return !r.terminal()
                  ? new AiOperation.Result(AiOperation.Status.RUNNING, "Opening the target block", "{}", List.of())
                  : new AiOperation.Result(r.status(), "Arrived at target; " + r.message(), r.dataJson(), r.evidence(), r.failure());
            } else {
               AiOperation.Result r = inner.tick();
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  return r;
               } else {
                  Minecraft mc = Minecraft.getInstance();
                  if (mc.level != null && HashUtil.init(mc.level.getBlockState(target).getBlock())) {
                     this.load = aF.init_AiOperation(target);
                     return new AiOperation.Result(AiOperation.Status.RUNNING, "Arrived at target; opening the block", "{}", List.of());
                  } else {
                     return r;
                  }
               }
            }
         }

         @Override
         public void cancel() {
            inner.cancel();
            if (this.load != null) {
               this.load.cancel();
            }
         }

         @Override
         public boolean canInterruptNow() {
            return (this.load == null ? inner : this.load).canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return (this.load == null ? inner : this.load).requiresReconciliationAfterEmergency();
         }
      };
   }

   public static AiOperation init_AiOperation_BlockPos(
      BlockPos target, double radius, boolean allowDig, boolean allowPillar, boolean allowBridge, int timeoutSeconds
   ) {
      Minecraft mc = Minecraft.getInstance();
      boolean overworld = mc.level == null || mc.level.dimension() == Level.OVERWORLD;
      return init(
         target, radius, allowDig, allowPillar, allowBridge, timeoutSeconds, overworld ? ClassCache.Inner_IoWorker.init : ClassCache.Inner_IoWorker.load
      );
   }

   public static AiOperation init_AiOperation(BlockPos target, int timeoutSeconds) {
      return init_AiOperation_BlockPos(target, timeoutSeconds, Set.of());
   }

   public static AiOperation init_AiOperation_BlockPos(BlockPos target, int timeoutSeconds, Set<Long> protectedCells) {
      return load(target, timeoutSeconds, protectedCells, true);
   }

   public static AiOperation load(BlockPos source, int seconds) {
      return load(source, seconds, Set.of());
   }

   public static AiOperation load(BlockPos source, int seconds, Set<Long> protectedCells) {
      return init_AiOperation_BlockPos_2(source, seconds, protectedCells, false);
   }

   public static AiOperation init_AiOperation_BlockPos_2(BlockPos source, int seconds, Set<Long> protectedCells, boolean supportedAccess) {
      return init_AiOperation_BlockPos_3(source, seconds, protectedCells, supportedAccess, Set.of());
   }

   public static AiOperation init_AiOperation_BlockPos_3(
      BlockPos source, int seconds, Set<Long> protectedCells, boolean supportedAccess, Set<Long> forbiddenCells
   ) {
      Minecraft mc = Minecraft.getInstance();
      ClassCache.Inner_JsonUtil sourceGoal = new HashUtil.Inner_StreamUtil(
         source,
         mc.player.blockInteractionRange() - 0.35,
         mc.player.getEyeHeight(),
         null,
         ConcurrentHashMap.newKeySet(),
         mc.level.getFluidState(source).is(FluidTags.WATER)
      );
      ClassCache.Inner_JsonUtil goal = init(protectedCells.isEmpty() ? sourceGoal : read(sourceGoal, protectedCells), forbiddenCells);
      return init(source, 0.6, true, supportedAccess, supportedAccess, seconds, ClassCache.Inner_IoWorker.load, true, 2.0, supportedAccess ? init(goal) : goal);
   }

   public static AiOperation init_AiOperation_BlockPos(BlockPos target, double radius, int seconds, Set<Long> protectedCells) {
      return init_AiOperation_BlockPos(target, radius, seconds, protectedCells, Set.of());
   }

   public static AiOperation init_AiOperation_BlockPos(BlockPos target, double radius, int seconds, Set<Long> protectedCells, Set<Long> forbiddenCells) {
      return init(
         target,
         radius,
         true,
         true,
         true,
         seconds,
         ClassCache.Inner_IoWorker.load,
         true,
         2.0,
         init(init(read(ClassCache.Inner_JsonUtil.init_ClassCache_JsonUti_BlockPos_D(target, radius), protectedCells), forbiddenCells))
      );
   }

   public static ClassCache.Inner_JsonUtil init(final ClassCache.Inner_JsonUtil delegate, Set<Long> cells) {
      if (cells.isEmpty()) {
         return delegate;
      }

      final Set<Long> forbidden = Set.copyOf(cells);
      return new HashUtil.Inner_DataUtil() {
         @Override
         public ClassCache.Inner_JsonUtil init_ClassCache_JsonUti() {
            return delegate;
         }

         @Override
         public boolean isInGoal(int x, int y, int z) {
            return !forbidden.contains(BlockPos.asLong(x, y, z)) && delegate.isInGoal(x, y, z);
         }

         @Override
         public boolean init_Z_I(int x, int y, int z, ClassCache.Inner_StateCache grid) {
            return !forbidden.contains(BlockPos.asLong(x, y, z)) && delegate.init_Z_I(x, y, z, grid);
         }

         @Override
         public double heuristic(int x, int y, int z) {
            return delegate.heuristic(x, y, z);
         }

         @Override
         public BlockPos init_BlockPos() {
            return delegate.init_BlockPos();
         }

         @Override
         public boolean outsideSearchBox(BlockPos from, int horizontal, int vertical) {
            return delegate.outsideSearchBox(from, horizontal, vertical);
         }

         @Override
         public boolean init_Z(BlockPos cell) {
            return !forbidden.contains(cell.asLong()) && delegate.init_Z(cell);
         }

         @Override
         public boolean load(BlockPos cell) {
            return delegate.load(cell);
         }

         @Override
         public boolean read(BlockPos cell) {
            return delegate.read(cell);
         }

         @Override
         public boolean init_Z() {
            return delegate.init_Z();
         }

         @Override
         public double init_D() {
            return delegate.init_D();
         }

         @Override
         public Set<ClassCache.Inner_SyncManager> init_Set() {
            return delegate.init_Set();
         }

         @Override
         public ClassCache.Inner_JsonUtil init(ClassCache.Inner_StateCache grid) {
            return HashUtil.init(delegate.init(grid), forbidden);
         }
      };
   }

   static ClassCache.Inner_JsonUtil init(final ClassCache.Inner_JsonUtil delegate) {
      return new HashUtil.Inner_DataUtil() {
         @Override
         public ClassCache.Inner_JsonUtil init_ClassCache_JsonUti() {
            return delegate;
         }

         @Override
         public boolean isInGoal(int x, int y, int z) {
            return delegate.isInGoal(x, y, z);
         }

         @Override
         public boolean init_Z_I(int x, int y, int z, ClassCache.Inner_StateCache grid) {
            return delegate.init_Z_I(x, y, z, grid);
         }

         @Override
         public double heuristic(int x, int y, int z) {
            return delegate.heuristic(x, y, z);
         }

         @Override
         public BlockPos init_BlockPos() {
            return delegate.init_BlockPos();
         }

         @Override
         public boolean outsideSearchBox(BlockPos from, int horizontal, int vertical) {
            return delegate.outsideSearchBox(from, horizontal, vertical);
         }

         @Override
         public boolean init_Z(BlockPos cell) {
            return delegate.init_Z(cell);
         }

         @Override
         public boolean load(BlockPos cell) {
            return delegate.load(cell);
         }

         @Override
         public boolean read(BlockPos cell) {
            return delegate.read(cell);
         }

         @Override
         public boolean init_Z() {
            return true;
         }

         @Override
         public double init_D() {
            return delegate.init_D();
         }

         @Override
         public ClassCache.Inner_JsonUtil init(ClassCache.Inner_StateCache grid) {
            return HashUtil.init(delegate.init(grid));
         }

         @Override
         public Set<ClassCache.Inner_SyncManager> init_Set() {
            EnumSet<ClassCache.Inner_SyncManager> moves = EnumSet.of(
               ClassCache.Inner_SyncManager.start, ClassCache.Inner_SyncManager.put, ClassCache.Inner_SyncManager.add, ClassCache.Inner_SyncManager.clear
            );
            moves.addAll(delegate.init_Set());
            return Set.copyOf(moves);
         }
      };
   }

   public static AiOperation init(List<BlockPos> sources, int seconds) {
      if (sources.isEmpty()) {
         throw new IllegalArgumentException("No water source candidates");
      }

      Minecraft mc = Minecraft.getInstance();
      ClassCache.Inner_JsonUtil sourceGoal = new HashUtil.Inner_StreamUtil(
         sources, mc.player.blockInteractionRange() - 0.35, mc.player.getEyeHeight(), null, ConcurrentHashMap.newKeySet(), true
      );
      return init(sources.getFirst(), 0.6, true, true, true, seconds, ClassCache.Inner_IoWorker.load, true, 2.0, sourceGoal);
   }

   public static AiOperation load(BlockPos target, int timeoutSeconds, Set<Long> protectedCells, boolean allowScaffold) {
      return init_AiOperation_BlockPos_4(target, timeoutSeconds, protectedCells, allowScaffold, null);
   }

   public static AiOperation init_AiOperation_BlockPos_4(
      BlockPos target, int timeoutSeconds, Set<Long> protectedCells, boolean allowScaffold, Direction requiredFace
   ) {
      return init_AiOperation_BlockPos_3(target, timeoutSeconds, protectedCells, allowScaffold, requiredFace, 0.25);
   }

   public static AiOperation init_AiOperation_BlockPos_3(
      BlockPos target, int timeoutSeconds, Set<Long> protectedCells, boolean allowScaffold, Direction requiredFace, double slack
   ) {
      Minecraft mc = Minecraft.getInstance();
      return init(
         target,
         0.6,
         true,
         allowScaffold,
         allowScaffold,
         timeoutSeconds,
         ClassCache.Inner_IoWorker.init,
         true,
         2.0,
         read(new q(target, Math.max(1.5, mc.player.blockInteractionRange() - slack), CachePool.init(mc).y - mc.player.getY(), requiredFace), protectedCells)
      );
   }

   public static AiOperation read(BlockPos target, int timeoutSeconds, Set<Long> protectedCells, boolean allowScaffold) {
      return init_AiOperation_BlockPos_2(target, timeoutSeconds, protectedCells, allowScaffold, 0.25);
   }

   public static AiOperation init_AiOperation_BlockPos_2(BlockPos target, int timeoutSeconds, Set<Long> protectedCells, boolean allowScaffold, double slack) {
      return init_AiOperation_BlockPos_2(target, timeoutSeconds, protectedCells, allowScaffold, slack, false);
   }

   public static AiOperation init_AiOperation_BlockPos_2(
      BlockPos target, int timeoutSeconds, Set<Long> protectedCells, boolean allowScaffold, double slack, boolean dry
   ) {
      Minecraft mc = Minecraft.getInstance();
      ClassCache.Inner_JsonUtil goal = read(
         new q(target, Math.max(1.5, mc.player.blockInteractionRange() - slack), CachePool.init(mc).y - mc.player.getY()).init_q(), protectedCells
      );
      return init(target, 0.6, true, allowScaffold, allowScaffold, timeoutSeconds, ClassCache.Inner_IoWorker.init, true, 2.0, dry ? load(goal) : goal);
   }

   static ClassCache.Inner_JsonUtil load(final ClassCache.Inner_JsonUtil delegate) {
      return new HashUtil.Inner_DataUtil() {
         @Override
         public ClassCache.Inner_JsonUtil init_ClassCache_JsonUti() {
            return delegate;
         }

         @Override
         public boolean isInGoal(int x, int y, int z) {
            return delegate.isInGoal(x, y, z);
         }

         @Override
         public boolean init_Z_I(int x, int y, int z, ClassCache.Inner_StateCache planned) {
            return delegate.init_Z_I(x, y, z, planned) && !planned.init_ClassCache_FileCac_I(x, y, z).water();
         }

         @Override
         public double heuristic(int x, int y, int z) {
            return delegate.heuristic(x, y, z);
         }

         @Override
         public BlockPos init_BlockPos() {
            return delegate.init_BlockPos();
         }

         @Override
         public boolean outsideSearchBox(BlockPos from, int horizontal, int vertical) {
            return delegate.outsideSearchBox(from, horizontal, vertical);
         }

         @Override
         public boolean init_Z(BlockPos cell) {
            return delegate.init_Z(cell);
         }

         @Override
         public boolean load(BlockPos cell) {
            return delegate.load(cell);
         }

         @Override
         public boolean read(BlockPos cell) {
            return delegate.read(cell);
         }

         @Override
         public boolean init_Z() {
            return delegate.init_Z();
         }

         @Override
         public double init_D() {
            return delegate.init_D();
         }

         @Override
         public ClassCache.Inner_JsonUtil init(ClassCache.Inner_StateCache grid) {
            return HashUtil.load(delegate.init(grid));
         }

         @Override
         public Set<ClassCache.Inner_SyncManager> init_Set() {
            return delegate.init_Set();
         }
      };
   }

   public static AiOperation init_AiOperation_BlockPos_2(BlockPos target, Item item, int timeoutSeconds) {
      return init_AiOperation_BlockPos_3(target, item, timeoutSeconds, null);
   }

   public static AiOperation init_AiOperation_BlockPos_3(BlockPos target, Item item, int timeoutSeconds, AABB workVolume) {
      return init_AiOperation_BlockPos_6(target, item, timeoutSeconds, workVolume, true);
   }

   public static AiOperation init_AiOperation_BlockPos_6(BlockPos target, Item item, int timeoutSeconds, AABB workVolume, boolean walkOnly) {
      return init_AiOperation_BlockPos_5(target, item, timeoutSeconds, workVolume, walkOnly, Set.of());
   }

   public static AiOperation init(BlockPos target, Item item, int timeoutSeconds, AABB workVolume, boolean walkOnly, Set<Long> keep, Set<Long> noStand) {
      Minecraft mc = Minecraft.getInstance();
      ClassCache.Inner_JsonUtil goal = read(new u(target, item, mc.player.blockInteractionRange() - 0.25, mc.player.getEyeHeight(), workVolume), keep);
      return init(target, 0.6, false, !walkOnly, !walkOnly, timeoutSeconds, ClassCache.Inner_IoWorker.init, true, 2.0, load(goal, noStand));
   }

   static ClassCache.Inner_JsonUtil load(final ClassCache.Inner_JsonUtil delegate, final Set<Long> cells) {
      return cells.isEmpty() ? delegate : new HashUtil.Inner_DataUtil() {
         @Override
         public ClassCache.Inner_JsonUtil init_ClassCache_JsonUti() {
            return delegate;
         }

         boolean init_Z_I(int x, int y, int z) {
            return cells.contains(BlockPos.asLong(x, y, z)) || cells.contains(BlockPos.asLong(x, y + 1, z));
         }

         @Override
         public boolean isInGoal(int x, int y, int z) {
            return !this.init_Z_I(x, y, z) && delegate.isInGoal(x, y, z);
         }

         @Override
         public boolean init_Z_I(int x, int y, int z, ClassCache.Inner_StateCache planned) {
            return !this.init_Z_I(x, y, z) && delegate.init_Z_I(x, y, z, planned);
         }

         @Override
         public double heuristic(int x, int y, int z) {
            return delegate.heuristic(x, y, z);
         }

         @Override
         public BlockPos init_BlockPos() {
            return delegate.init_BlockPos();
         }

         @Override
         public boolean outsideSearchBox(BlockPos from, int horizontal, int vertical) {
            return delegate.outsideSearchBox(from, horizontal, vertical);
         }

         @Override
         public boolean init_Z(BlockPos cell) {
            return delegate.init_Z(cell);
         }

         @Override
         public boolean load(BlockPos cell) {
            return delegate.load(cell);
         }

         @Override
         public boolean read(BlockPos cell) {
            return delegate.read(cell);
         }

         @Override
         public boolean init_Z() {
            return delegate.init_Z();
         }

         @Override
         public double init_D() {
            return delegate.init_D();
         }

         @Override
         public ClassCache.Inner_JsonUtil init(ClassCache.Inner_StateCache grid) {
            return HashUtil.load(delegate.init(grid), cells);
         }

         @Override
         public Set<ClassCache.Inner_SyncManager> init_Set() {
            return delegate.init_Set();
         }

         @Override
         public boolean write(BlockPos cell) {
            return delegate.write(cell);
         }
      };
   }

   public static AiOperation init_AiOperation_BlockPos_5(BlockPos target, Item item, int timeoutSeconds, AABB workVolume, boolean walkOnly, Set<Long> keep) {
      Minecraft mc = Minecraft.getInstance();
      return init(
         target,
         0.6,
         false,
         !walkOnly,
         !walkOnly,
         timeoutSeconds,
         ClassCache.Inner_IoWorker.init,
         true,
         2.0,
         read(new u(target, item, mc.player.blockInteractionRange() - 0.25, mc.player.getEyeHeight(), workVolume), keep)
      );
   }

   public static AiOperation load(BlockPos target, Item item, int timeoutSeconds, AABB workVolume) {
      Minecraft mc = Minecraft.getInstance();
      return init(
         target,
         0.6,
         true,
         true,
         true,
         timeoutSeconds,
         ClassCache.Inner_IoWorker.init,
         true,
         2.0,
         new u(target, item, mc.player.blockInteractionRange() - 0.25, mc.player.getEyeHeight(), workVolume)
      );
   }

   public static AiOperation init_AiOperation_BlockPos_5(BlockPos target, Item item, int timeoutSeconds, AABB volume, Set<Long> cells) {
      return init_AiOperation_BlockPos_4(target, item, timeoutSeconds, volume, cells, null);
   }

   public static ClassCache.Inner_JsonUtil read(ClassCache.Inner_JsonUtil delegate, Set<Long> cells) {
      return init_ClassCache_JsonUti_ClassCache_JsonUti(delegate, cells, -2147483648);
   }

   static ClassCache.Inner_JsonUtil read(ClassCache.Inner_JsonUtil goal) {
      while (goal instanceof HashUtil.Inner_DataUtil) {
         HashUtil.Inner_DataUtil build = (HashUtil.Inner_DataUtil)goal;
         goal = build.init_ClassCache_JsonUti();
      }

      return goal;
   }

   public static ClassCache.Inner_JsonUtil init_ClassCache_JsonUti_ClassCache_JsonUti(
      final ClassCache.Inner_JsonUtil delegate, final Set<Long> cells, final int minimumDigY
   ) {
      return cells.isEmpty() && minimumDigY == -2147483648 ? delegate : new HashUtil.Inner_DataUtil() {
         @Override
         public ClassCache.Inner_JsonUtil init_ClassCache_JsonUti() {
            return delegate;
         }

         @Override
         public boolean isInGoal(int x, int y, int z) {
            return delegate.isInGoal(x, y, z);
         }

         @Override
         public boolean init_Z_I(int x, int y, int z, ClassCache.Inner_StateCache planned) {
            return delegate.init_Z_I(x, y, z, planned);
         }

         @Override
         public double heuristic(int x, int y, int z) {
            return delegate.heuristic(x, y, z);
         }

         @Override
         public BlockPos init_BlockPos() {
            return delegate.init_BlockPos();
         }

         @Override
         public boolean outsideSearchBox(BlockPos from, int horizontal, int vertical) {
            return delegate.outsideSearchBox(from, horizontal, vertical);
         }

         @Override
         public boolean init_Z(BlockPos cell) {
            return delegate.init_Z(cell);
         }

         @Override
         public boolean load(BlockPos cell) {
            return cell.getY() >= minimumDigY && !cells.contains(cell.asLong()) && delegate.load(cell);
         }

         @Override
         public boolean read(BlockPos cell) {
            return !cells.contains(cell.asLong()) && delegate.read(cell);
         }

         @Override
         public boolean init_Z() {
            return delegate.init_Z();
         }

         @Override
         public double init_D() {
            return Math.min(delegate.init_D(), minimumDigY == -2147483648 ? 40.0 : 12.0);
         }

         @Override
         public ClassCache.Inner_JsonUtil init(ClassCache.Inner_StateCache grid) {
            return HashUtil.init_ClassCache_JsonUti_ClassCache_JsonUti(delegate.init(grid), cells, minimumDigY);
         }

         @Override
         public Set<ClassCache.Inner_SyncManager> init_Set() {
            return delegate.init_Set();
         }
      };
   }

   public static AiOperation init_AiOperation_BlockPos_4(
      BlockPos target, Item item, int timeoutSeconds, AABB volume, Set<Long> cells, u.Inner_DataUtil constraint
   ) {
      return init_AiOperation_List(List.of(new u.Inner_CachePool(target, item, constraint)), timeoutSeconds, volume, cells);
   }

   public static AiOperation init_AiOperation_List(List<u.Inner_CachePool> batch, int timeoutSeconds, AABB volume, Set<Long> cells) {
      return init_AiOperation_List(batch, timeoutSeconds, volume, cells, Set.of());
   }

   public static AiOperation init_AiOperation_List(List<u.Inner_CachePool> batch, int timeoutSeconds, AABB volume, Set<Long> cells, Set<Long> avoid) {
      if (AutismLiteVariant.enabled()) {
         throw new IllegalStateException("Schematic construction is unavailable in Autism Lite");
      } else {
         return new b(batch, timeoutSeconds, volume, cells, avoid);
      }
   }

   public static AiOperation init(
      BlockPos target, double radius, boolean allowDig, boolean allowPillar, boolean allowBridge, int timeoutSeconds, ClassCache.Inner_IoWorker strategy
   ) {
      return init(target, radius, allowDig, allowPillar, allowBridge, timeoutSeconds, strategy, false);
   }

   public static AiOperation init(
      BlockPos target,
      double radius,
      boolean allowDig,
      boolean allowPillar,
      boolean allowBridge,
      int timeoutSeconds,
      ClassCache.Inner_IoWorker strategy,
      boolean allowInventory
   ) {
      return init(target, radius, allowDig, allowPillar, allowBridge, timeoutSeconds, strategy, allowInventory, 2.0);
   }

   public static AiOperation init(
      BlockPos target,
      double radius,
      boolean allowDig,
      boolean allowPillar,
      boolean allowBridge,
      int timeoutSeconds,
      ClassCache.Inner_IoWorker strategy,
      boolean allowInventory,
      double breakReluctance
   ) {
      return init(target, radius, allowDig, allowPillar, allowBridge, timeoutSeconds, strategy, allowInventory, breakReluctance, null);
   }

   public static AiOperation init(
      BlockPos target,
      double radius,
      boolean allowDig,
      boolean allowPillar,
      boolean allowBridge,
      int timeoutSeconds,
      ClassCache.Inner_IoWorker strategy,
      boolean allowInventory,
      double breakReluctance,
      ClassCache.Inner_JsonUtil goalOverride
   ) {
      return init(
         target,
         radius,
         allowDig,
         allowPillar,
         allowBridge,
         timeoutSeconds,
         strategy,
         allowInventory,
         breakReluctance,
         goalOverride,
         HashUtil.Inner_BufferHelper.DEFAULT
      );
   }

   public static AiOperation init(
      BlockPos target,
      double radius,
      boolean allowDig,
      boolean allowPillar,
      boolean allowBridge,
      int timeoutSeconds,
      ClassCache.Inner_IoWorker strategy,
      boolean allowInventory,
      double breakReluctance,
      ClassCache.Inner_JsonUtil goalOverride,
      HashUtil.Inner_BufferHelper options
   ) {
      return init(target, radius, allowDig, allowPillar, allowBridge, timeoutSeconds, strategy, allowInventory, breakReluctance, goalOverride, options, false);
   }

   public static HashUtil.Inner_CachePool init(
      BlockPos target,
      double radius,
      boolean allowDig,
      boolean allowPillar,
      boolean allowBridge,
      int timeoutSeconds,
      ClassCache.Inner_IoWorker strategy,
      boolean allowInventory,
      double breakReluctance,
      ClassCache.Inner_JsonUtil goalOverride,
      HashUtil.Inner_BufferHelper options,
      boolean persistent
   ) {
      return init(
         target,
         radius,
         allowDig,
         allowPillar,
         allowBridge,
         timeoutSeconds,
         strategy,
         allowInventory,
         breakReluctance,
         goalOverride,
         options,
         persistent,
         false
      );
   }

   private static HashUtil.Inner_CachePool init(
      BlockPos target,
      double radius,
      boolean allowDig,
      boolean allowPillar,
      boolean allowBridge,
      int timeoutSeconds,
      ClassCache.Inner_IoWorker strategy,
      boolean allowInventory,
      double breakReluctance,
      ClassCache.Inner_JsonUtil goalOverride,
      HashUtil.Inner_BufferHelper options,
      boolean persistent,
      boolean completeRoute
   ) {
      return init(
         target,
         radius,
         allowDig,
         allowPillar,
         allowBridge,
         timeoutSeconds,
         strategy,
         allowInventory,
         breakReluctance,
         goalOverride,
         options,
         persistent,
         completeRoute,
         false,
         false
      );
   }

   private static HashUtil.Inner_CachePool init(
      final BlockPos target,
      double radius,
      final boolean allowDig,
      final boolean allowPillar,
      final boolean allowBridge,
      int timeoutSeconds,
      final ClassCache.Inner_IoWorker strategy,
      final boolean allowInventory,
      final double breakReluctance,
      final ClassCache.Inner_JsonUtil goalOverride,
      final HashUtil.Inner_BufferHelper options,
      final boolean persistent,
      final boolean completeRoute,
      final boolean parkour,
      final boolean assumeNoDamage
   ) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final long deadline = completeRoute && timeoutSeconds == 0
            ? 9223372036854775807L
            : System.nanoTime() + Math.max(1, Math.min(3600, timeoutSeconds)) * 1000000000L;
         final double arrivalRadius = Math.max(0.6, Math.min(8.0, radius));
         return new HashUtil.Inner_CachePool() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            final Object init_Obj = this.init_Minecraft.level;
            final BlockPos init_BlockPos = this.read_BlockPos_2();
            final int init_I = this.init_Minecraft.player.getInventory().getSelectedSlot();
            final ClassCache.Inner_StateCache init_ClassCache_StateCa;
            ClassCache.Inner_JsonUtil init_ClassCache_JsonUti;
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            boolean write_Z;
            final ClassCache.Inner_BufferHelper init_ClassCache_BufferH;
            final Object load_Obj;
            long init_J;
            final RefQueue init_RefQueue;
            ClassCache.Inner_NetHelper init_ClassCache_NetHelp;
            boolean update_Z;
            ClassCache.Inner_PacketUtil init_ClassCache_PacketU;
            ArrayUtil init_ArrayUtil;
            boolean sync_Z;
            aP.Inner_StreamUtil init_aP_StreamUtil;
            static final int load_I = 60;
            final DataUtil init_DataUtil;
            boolean flush_Z;
            boolean close_Z;
            int read_I;
            int write_I;
            int update_I;
            Vec3 init_Vec3;
            boolean open_Z;
            boolean poll_Z;
            boolean push_Z;
            final Set<Long> init_Set;
            int sync_I;
            int flush_I;
            int close_I;
            double init_D;
            double load_D;
            Vec3 load_Vec3;
            final long load_J;
            double read_D;
            final Set<ClassCache.Inner_SyncManager> load_Set;
            ClassCache.Inner_SyncManager init_ClassCache_SyncMan;
            BlockPos load_BlockPos;
            BlockPos read_BlockPos;
            int open_I;
            long read_J;
            long write_J;
            boolean pop_Z;
            boolean tick_Z;
            ClassCache.Inner_JsonUtil load_ClassCache_JsonUti;
            final DataPipe init_DataPipe;
            BlockPos write_BlockPos;
            int poll_I;
            int push_I;
            int pop_I;
            int tick_I;
            static final long update_J = 20000000L;
            long sync_J;
            ItemStack[] init_ItemStackArr;

            {
               this.init_ClassCache_StateCa = HashUtil.init_Z
                  ? new ClassCache.Inner_StreamUtil(this.init_Minecraft, this.init_BlockPos, target, this.load_ItemStackArr(), StateCache.init_StateCache())
                  : new RingBuffer(this.init_Minecraft, this.load_ItemStackArr(), StateCache.init_StateCache());
               this.init_ClassCache_JsonUti = goalOverride != null
                  ? goalOverride.init(this.init_ClassCache_StateCa)
                  : ClassCache.Inner_JsonUtil.init_ClassCache_JsonUti_BlockPos(target, arrivalRadius, parkour);
               this.init_ClassCache_BufferH = new ClassCache.Inner_BufferHelper(this.init_ClassCache_StateCa, completeRoute);
               this.load_Obj = new Object();
               this.init_RefQueue = new RefQueue();
               this.init_ClassCache_NetHelp = assumeNoDamage
                  ? ClassCache.Inner_NetHelper.read
                  : (parkour ? ClassCache.Inner_NetHelper.load : ClassCache.Inner_NetHelper.init_ClassCache_NetHelp);
               if (!AutismLiteVariant.enabled()) {
                  o.init(this, this.init_RefQueue);
               }

               this.init_RefQueue.read_V_Z(goalOverride != null && goalOverride.init_Z());
               this.init_RefQueue.init_V_Predicate(cell -> this.init_ClassCache_JsonUti.init_Z(cell));
               this.init_RefQueue.init_V_Z(allowInventory);
               this.init_RefQueue.init_V_StateCache(StateCache.init_StateCache());
               this.init_RefQueue.load_V_Z(options == null || options.bunnyhop());
               this.init_DataUtil = new DataUtil();
               this.init_Set = new HashSet<>();
               this.init_D = 1.7976931348623157E308;
               this.load_J = System.nanoTime();
               this.read_D = -1.0;
               this.load_Set = new HashSet<>();
               this.read_J = System.nanoTime();
               this.init_DataPipe = new DataPipe();
               this.sync_J = -1L;
            }

            @Override
            public AiOperation.Result tick() {
               PacketUtil.init_V();
               if (!this.flush_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
                  if (this.init_Minecraft.level != this.init_Obj) {
                     return this.init(A.valid, "World changed during travel");
                  }

                  if (!AutismLiteVariant.enabled()) {
                     if (this.sync_Z) {
                        return this.init(AiOperation.Status.CANCELLED, "Parkour cancelled");
                     }

                     if (this.init_ClassCache_PacketU != null) {
                        return this.open_AiOperation_Result();
                     }
                  }

                  if (System.nanoTime() > deadline) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Goto deadline reached");
                  }

                  if (this.init_Minecraft.player.getHealth() <= 4.0F && !CharEncoder.init_Z() && !parkour) {
                     return this.init(A.start, CharEncoder.init("travel", this.init_Minecraft.player.getHealth(), this.init_Minecraft.player.getMaxHealth()));
                  }

                  if (this.init_Vec3 != null) {
                     return this.flush_AiOperation_Result();
                  }

                  if (this.write_BlockPos != null) {
                     DataPipe.Inner_DataUtil status = this.push_I < 60 ? this.init_DataPipe.init_DataPipe_DataUtil() : DataPipe.Inner_DataUtil.read;
                     this.push_I++;
                     if (status == DataPipe.Inner_DataUtil.init) {
                        return this.load("Blocking the water at " + this.write_BlockPos.toShortString());
                     }

                     BlockPos feet = this.init_Minecraft.player.blockPosition();
                     boolean wet = !this.init_Minecraft.level.getFluidState(feet).isEmpty() || !this.init_Minecraft.level.getFluidState(feet.above()).isEmpty();
                     if (status == DataPipe.Inner_DataUtil.load && wet && this.push_I < 80) {
                        return this.load("Waiting for the water to drain");
                     }

                     AutismClientAddon.LOG
                        .info(
                           "[AI] travel: water at {} {} ({})",
                           this.write_BlockPos.toShortString(),
                           status == DataPipe.Inner_DataUtil.load ? "blocked" : "not blocked",
                           status == DataPipe.Inner_DataUtil.load ? "replanning" : this.init_DataPipe.init_Str()
                        );
                     this.write_BlockPos = null;
                     this.init_ClassCache_BufferH.init(this.init_RefQueue.write_BlockPos());
                     this.init_RefQueue.update_V();
                     return this.load("Replanning after blocking the water");
                  } else {
                     if (this.init_ClassCache_StateCa instanceof ClassCache.Inner_StreamUtil snapshot) {
                        snapshot.init_V_BlockPos(this.close_Z ? this.init_ClassCache_BufferH.init_BlockPos() : this.init_Minecraft.player.blockPosition());
                        snapshot.init_V_J(this.init_RefQueue.init_RefQueue_TaskQueue() == RefQueue.Inner_TaskQueue.load ? 750000L : 4000000L);
                     } else {
                        RingBuffer.init(this.init_Minecraft, StateCache.init_StateCache());
                     }

                     long tickNow = System.nanoTime();
                     if (this.load_Z
                        || !this.close_Z
                        || completeRoute && this.init_ClassCache_BufferH.init_Z() && !this.init_RefQueue.write_Z()
                        || this.write_J != 0L && tickNow - this.write_J > 1000000000L) {
                        this.read_J = tickNow;
                     }

                     this.write_J = tickNow;
                     if (!this.load_Z && this.close_Z && System.nanoTime() - this.read_J > 30000000000L) {
                        AutismClientAddon.LOG
                           .info(
                              "[AI] NO PROGRESS: {} for {} s at {} (best h {})",
                              this.init_ClassCache_JsonUti,
                              30L,
                              this.init_RefQueue.write_BlockPos().toShortString(),
                              String.format(Locale.ROOT, "%.1f", this.init_D)
                           );
                        if (this.init_Minecraft.level != null) {
                           AutismClientAddon.LOG.info("[AI] {}", n.init(this.init_Minecraft.level, this.init_Minecraft.player.blockPosition(), null, 6, 4));
                        }

                        return this.init(A.close, "No progress toward the target in 30 s");
                     } else if (this.load_Z) {
                        if (this.init_RefQueue.write_Z() && !this.init_RefQueue.read_Z()) {
                           this.init_RefQueue.load_RefQueue_TaskQueue();
                           return this.load("Pausing");
                        } else {
                           this.init_RefQueue.sync_V();
                           return this.load("Paused");
                        }
                     } else {
                        if (this.init_Z && this.close_Z && this.init_RefQueue.read_Z()) {
                           this.init_Z = false;
                           this.init_ClassCache_BufferH
                              .init_V_BlockPos_2(this.init_RefQueue.write_BlockPos(), this.init_ClassCache_JsonUti, this.init_ClassCache_DataUti());
                        }

                        Vec3 pos = this.init_Minecraft.player.position();
                        if (this.load_Vec3 != null) {
                           this.load_D = this.load_D + pos.distanceTo(this.load_Vec3);
                        }

                        this.load_Vec3 = pos;
                        BlockPos feetNow = this.init_RefQueue.write_BlockPos();
                        if (!this.flush_Z_2()
                           && !this.init_RefQueue.write_Z()
                           && !this.init_RefQueue.open_Z()
                           && this.init_ClassCache_JsonUti.isInGoal(feetNow.getX(), feetNow.getY(), feetNow.getZ())) {
                           this.init_RefQueue.sync_V();
                           return this.load("Waiting for endpoint landing");
                        }

                        double heuristic = this.init_ClassCache_JsonUti.heuristic(feetNow.getX(), feetNow.getY(), feetNow.getZ());
                        if (heuristic < this.init_D - 0.5) {
                           this.init_D = heuristic;
                           this.read_J = System.nanoTime();
                           this.read_I = 0;
                           this.write_I = 0;
                           this.load_Set.clear();
                           this.init_ClassCache_SyncMan = null;
                           this.load_BlockPos = null;
                           this.pop_Z = false;
                        }

                        if (this.sync_Z_3() && !this.init_RefQueue.close_Z()) {
                           return this.init_AiOperation_Result_Str_6("Arrived at target");
                        }

                        this.read_Z = false;
                        if (this.init_J != 0L) {
                           if (System.nanoTime() < this.init_J) {
                              this.init_RefQueue.sync_V();
                              return this.load("Waiting for terrain ahead to load");
                           }

                           this.init_J = 0L;
                           this.init_ClassCache_BufferH.init(this.read_BlockPos_2());
                        }

                        if (!this.close_Z) {
                           if (!this.open_Z()) {
                              return this.load("Landing before starting the route");
                           }

                           if (!parkour
                              && this.flush_I < 2
                              && this.init_Minecraft.player.onGround()
                              && HashUtil.init(
                                 this.init_Minecraft
                                    .level
                                    .getBlockState(this.init_Minecraft.player.blockPosition().below())
                                    .getCollisionShape(this.init_Minecraft.level, this.init_Minecraft.player.blockPosition().below()),
                                 this.init_Minecraft
                                    .level
                                    .getBlockState(this.init_Minecraft.player.blockPosition().below(2))
                                    .getCollisionShape(this.init_Minecraft.level, this.init_Minecraft.player.blockPosition().below(2))
                              )
                              && this.poll()) {
                              this.flush_I++;
                              return this.load("Centering on supported footing before planning");
                           }

                           if (allowDig && !allowPillar && this.close_Z()) {
                              return this.init(A.tick, "No harvesting tool for the blocks enclosing the current stance");
                           }

                           this.close_Z = true;
                           this.init_ClassCache_BufferH.init_V_BlockPos_2(this.read_BlockPos_2(), this.init_ClassCache_JsonUti, this.init_ClassCache_DataUti());
                           if (!this.init_ClassCache_BufferH.init(20000000L)) {
                              return this.close_AiOperation_Result();
                           }
                        }

                        this.init_ClassCache_BufferH.init(this.init_ClassCache_DataUti());
                        this.init_ClassCache_BufferH.init(this.init_RefQueue);
                        if (this.init_RefQueue.init_RefQueue_TaskQueue() == RefQueue.Inner_TaskQueue.load) {
                           ClassCache.Inner_PacketUtil ahead = this.init_ClassCache_BufferH.load_ClassCache_PacketU();
                           if (ahead != null && !ahead.movements().isEmpty() && this.init_RefQueue.init_Z_List_Z(ahead.movements(), ahead.reachesGoal())) {
                              this.init_ClassCache_BufferH.read();
                              this.init_ClassCache_BufferH.init(ahead);
                           }
                        }

                        for (int guard = 0; guard < 2; guard++) {
                           RefQueue.Inner_TaskQueue st = this.init_RefQueue.load_RefQueue_TaskQueue();
                           if (st == RefQueue.Inner_TaskQueue.load) {
                              return this.load(this.init_Str_Vec3(pos));
                           }

                           if (st == RefQueue.Inner_TaskQueue.write) {
                              return this.init(this.init_RefQueue.load_BlockPos(), "Movement stalled");
                           }

                           if (st == RefQueue.Inner_TaskQueue.update) {
                              return this.init(AiOperation.Status.CANCELLED, "Goto cancelled");
                           }

                           if (st == RefQueue.Inner_TaskQueue.init) {
                              if (!this.open_Z()) {
                                 return this.load("Landing before accepting the route");
                              }

                              ClassCache.Inner_PacketUtil s = this.init_ClassCache_BufferH.init_ClassCache_PacketU();
                              if (s == null) {
                                 if (!this.init_ClassCache_BufferH.init_Z() && !this.init_ClassCache_BufferH.load_Z()) {
                                    this.init_ClassCache_BufferH.init(this.init_RefQueue.write_BlockPos());
                                 }

                                 if (this.init_ClassCache_BufferH.init(20000000L)) {
                                    this.init_ClassCache_BufferH.init(this.init_RefQueue);
                                    s = this.init_ClassCache_BufferH.init_ClassCache_PacketU();
                                 }

                                 if (s == null) {
                                    return this.close_AiOperation_Result();
                                 }
                              }

                              if (completeRoute && !AutismLiteVariant.enabled()) {
                                 cG.init_V(this.load_Obj);
                              }

                              if (s.movements().isEmpty()) {
                                 if (s.awaitingTerrain()) {
                                    return this.init_AiOperation_Result_13();
                                 }

                                 if (s.reachesGoal() && this.sync_Z_3()) {
                                    return this.init_AiOperation_Result_Str_6("Already at target");
                                 }

                                 if (s.reachesGoal() && this.push()) {
                                    return this.load("Adjusting the interaction position");
                                 }

                                 if (s.wedgedStart()) {
                                    return this.load_AiOperation_Result_10();
                                 }

                                 if (s.confinedStart()) {
                                    return this.read_AiOperation_Result_6();
                                 }

                                 if (s.hitPending()) {
                                    return this.write_AiOperation_Result_3();
                                 }

                                 if (HashUtil.init(s)) {
                                    return this.init(
                                       A.prev,
                                       completeRoute
                                          ? "Route search reached its memory limit before it could prove a route"
                                          : "Route search budget exhausted before a route could be proved"
                                    );
                                 }

                                 return this.sync_AiOperation_Result();
                              }

                              BlockPos liveStart = this.read_BlockPos_2();
                              if (!s.movements().get(0).from().equals(liveStart)) {
                                 this.init_ClassCache_BufferH.init(liveStart);
                                 if (!this.init_ClassCache_BufferH.init(20000000L)) {
                                    return this.load("Updating the route from the live stance");
                                 }

                                 this.init_ClassCache_BufferH.init(this.init_RefQueue);
                              } else {
                                 if (!AutismLiteVariant.enabled() && this.update_Z) {
                                    if (!s.reachesGoal()) {
                                       return this.init(A.close, "No complete route proved through loaded terrain");
                                    }

                                    this.init_ClassCache_PacketU = s;
                                    this.init_RefQueue.sync_V();
                                    return this.open_AiOperation_Result();
                                 }

                                 this.init_V_ClassCache_PacketU(s);
                                 this.init_ClassCache_BufferH.init(this.init_RefQueue);
                              }
                           } else {
                              BlockPos f = this.init_RefQueue.write_BlockPos();
                              if (this.tick_Z && this.load_ClassCache_JsonUti != null && this.load_ClassCache_JsonUti.isInGoal(f.getX(), f.getY(), f.getZ())) {
                                 this.tick_Z = false;
                                 this.load_ClassCache_JsonUti = null;
                                 this.init_ClassCache_BufferH.init_V_BlockPos_2(f, this.init_ClassCache_JsonUti, this.init_ClassCache_DataUti());
                                 return this.load("Escape complete; resuming the placement route");
                              }

                              if (this.sync_Z_3() && !this.init_RefQueue.close_Z()) {
                                 return this.init_AiOperation_Result_Str_6("Arrived at target");
                              }

                              if (this.push()) {
                                 return this.load("Adjusting the interaction position");
                              }

                              if (!this.open_Z()) {
                                 this.write_Z = true;
                                 return this.load("Landing before accepting the next route");
                              }

                              if (this.write_Z) {
                                 this.write_Z = false;
                                 this.init_ClassCache_BufferH.init(f);
                                 if (this.init_ClassCache_BufferH.init(20000000L)) {
                                    this.init_ClassCache_BufferH.init(this.init_RefQueue);
                                 }
                              }

                              ClassCache.Inner_PacketUtil next = this.init_ClassCache_BufferH.read();
                              if (next == null) {
                                 next = this.init_ClassCache_BufferH.init_ClassCache_PacketU();
                              }

                              if (next != null && next.movements().isEmpty() && next.reachesGoal() && this.sync_Z_3() && !this.init_RefQueue.close_Z()) {
                                 return this.init_AiOperation_Result_Str_6("Arrived at target");
                              }

                              if (next != null && next.movements().isEmpty()) {
                                 if (next.awaitingTerrain()) {
                                    return this.init_AiOperation_Result_13();
                                 }

                                 if (next.wedgedStart()) {
                                    return this.load_AiOperation_Result_10();
                                 }

                                 if (next.confinedStart()) {
                                    return this.read_AiOperation_Result_6();
                                 }

                                 if (next.hitPending()) {
                                    return this.write_AiOperation_Result_3();
                                 }

                                 if (HashUtil.init(next)) {
                                    return this.init(A.prev, "Route search budget exhausted before a route could be proved");
                                 }

                                 return this.sync_AiOperation_Result();
                              }

                              if (next == null) {
                                 if (!this.init_ClassCache_BufferH.load_Z() && !this.init_ClassCache_BufferH.init_Z()) {
                                    this.init_ClassCache_BufferH.init(this.init_ClassCache_DataUti());
                                    this.init_ClassCache_BufferH.init(f);
                                    return this.load("Planning next segment");
                                 }

                                 this.init_ClassCache_BufferH.load_V();
                                 return this.load("Planning next segment");
                              }

                              if (!next.movements().getFirst().from().equals(f)) {
                                 this.init_ClassCache_BufferH.init(f);
                                 return this.load("Updating the next segment from the live stance");
                              }

                              this.init_V_ClassCache_PacketU(next);
                              this.init_ClassCache_BufferH.init(this.init_RefQueue);
                           }
                        }

                        return this.load(this.init_Str_Vec3(pos));
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Goto cancelled");
               }
            }

            AiOperation.Result init_AiOperation_Result_13() {
               this.init_RefQueue.sync_V();
               this.init_J = System.nanoTime() + 250000000L;
               return this.load("Waiting for terrain ahead to load");
            }

            AiOperation.Result init_AiOperation_Result_Str_6(String message) {
               if (!this.init_RefQueue.load_Z()) {
                  return this.load("Braking before arrival beside lava");
               }

               if (!persistent) {
                  return this.init(AiOperation.Status.SUCCEEDED, message);
               }

               this.read_Z = true;
               this.init_RefQueue.read_Z();
               this.init_RefQueue.sync_V();
               return this.load("At the goal");
            }

            @Override
            public void init_V_15() {
               this.init_RefQueue.init_V();
            }

            @Override
            public void init_V_ClassCache_JsonUti(ClassCache.Inner_JsonUtil next) {
               ClassCache.Inner_JsonUtil bound = next.init(this.init_ClassCache_StateCa);
               this.init_ClassCache_JsonUti = bound;
               this.init_ClassCache_BufferH.init(bound);
               this.init_D = 1.7976931348623157E308;
               this.read_J = System.nanoTime();
               this.read_I = 0;
               this.write_I = 0;
               this.load_Set.clear();
               this.init_ClassCache_SyncMan = null;
               this.load_BlockPos = null;
               BlockPos end = this.init_RefQueue.read_BlockPos();
               if (end == null || !bound.isInGoal(end.getX(), end.getY(), end.getZ())) {
                  if (end != null && this.init_Minecraft.player != null) {
                     BlockPos feet = this.init_Minecraft.player.blockPosition();
                     if (bound.heuristic(end.getX(), end.getY(), end.getZ()) + 1.0 < bound.heuristic(feet.getX(), feet.getY(), feet.getZ())) {
                        return;
                     }
                  }

                  this.read_Z = false;
                  this.init_Z = true;
               }
            }

            @Override
            public boolean init_Z_13() {
               return this.read_Z;
            }

            @Override
            public void init_V_Z(boolean hold) {
               this.init_RefQueue.write_V_Z(hold);
            }

            @Override
            public void init(boolean hold, Predicate<BlockPos> keeps) {
               this.init_RefQueue.init_V_Z_Predicate(hold, keeps);
            }

            @Override
            public int init_I_14() {
               return this.init_RefQueue.init_I();
            }

            @Override
            public int load_I_8() {
               return this.init_RefQueue.load_I();
            }

            @Override
            public boolean load_Z_8() {
               return this.init_RefQueue.update_Z();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.init_RefQueue.sync_Z() || this.init_DataPipe.load_Z();
            }

            @Override
            public BlockPos init_BlockPos_6() {
               return this.init_RefQueue.init_BlockPos();
            }

            @Override
            public Set<BlockPos> init_Set_4() {
               return this.init_RefQueue.init_Set_I(2);
            }

            @Override
            public Set<BlockPos> load_Set_2() {
               return this.init_RefQueue.init_Set();
            }

            @Override
            public void init_V_Function(Function<BlockPos, Vec3> aim) {
               this.init_RefQueue.init_V_Function(aim);
            }

            @Override
            public void init_V_I_2(int blocks) {
               this.init_RefQueue.init_V_I(blocks);
            }

            @Override
            public void init_V_BiPredicate(BiPredicate<BlockState, ItemStack> accepted) {
               this.init_RefQueue.init_V_BiPredicate(accepted);
               if (this.init_ClassCache_StateCa instanceof ClassCache.Inner_StreamUtil snapshot) {
                  snapshot.init_V_BiPredicate(accepted);
               } else if (this.init_ClassCache_StateCa instanceof RingBuffer live) {
                  live.init_V_BiPredicate_2(accepted);
               }
            }

            @Override
            public BlockPos load_BlockPos_3() {
               return this.read_BlockPos;
            }

            @Override
            public void init_V_BlockPos(BlockPos cell) {
               this.init_ClassCache_BufferH.init(cell, this.init_RefQueue.write_BlockPos());
            }

            @Override
            public void load(boolean pause) {
               if (pause != this.load_Z) {
                  this.load_Z = pause;
                  if (pause) {
                     this.init_RefQueue.read_Z();
                     this.init_RefQueue.read_V();
                     if (completeRoute && !AutismLiteVariant.enabled()) {
                        cG.init_V(this.load_Obj);
                     }
                  } else {
                     this.init_Z = true;
                  }

                  this.read_Z = false;
               }
            }

            AiOperation.Result load_AiOperation_Result_10() {
               if (this.flush_I < 2 && this.poll()) {
                  this.flush_I++;
                  this.init_RefQueue.read_Z();
                  return this.load("Stepping clear of a partial block before replanning");
               } else {
                  return !allowDig
                     ? this.init(
                        A.close,
                        "Player is wedged in solid blocks at "
                           + this.init_RefQueue.write_BlockPos().toShortString()
                           + " (the feet or head cell is inside solid terrain); retry with allowDig:true so the route can dig out"
                     )
                     : this.init(
                        (BlockPos)null,
                        "No route toward the target in loaded terrain (Player is wedged in solid blocks at "
                           + this.init_RefQueue.write_BlockPos().toShortString()
                           + " and even dig-out moves found no way toward it)"
                     );
               }
            }

            AiOperation.Result read_AiOperation_Result_6() {
               if (this.flush_I < 2 && this.poll()) {
                  this.flush_I++;
                  this.init_RefQueue.read_Z();
                  return this.load("Stepping onto a full floor before replanning");
               } else {
                  return completeRoute && !allowDig
                     ? this.sync_AiOperation_Result()
                     : this.init(
                        (BlockPos)null,
                        "No route toward the target in loaded terrain (player is stuck in a non-solid hazard at "
                           + this.init_RefQueue.write_BlockPos().toShortString()
                           + ": no floor, fire, powder snow, lava or a hazardous floor, and no adjacent cell is standable)"
                     );
               }
            }

            AiOperation.Result write_AiOperation_Result_3() {
               return this.init(
                  (BlockPos)null,
                  "No route toward the target in loaded terrain (the terrain snapshot around the player at "
                     + this.init_RefQueue.write_BlockPos().toShortString()
                     + " never settled; chunk copies kept failing or invalidating)"
               );
            }

            AiOperation.Result init(BlockPos cell, String reason) {
               if (this.init_RefQueue.init_Str().equals("dig failed: no tool that gets its drops")) {
                  return this.init(A.tick, "No usable harvesting tool for a block on the route");
               }

               if (this.write_Z_4()) {
                  return this.load(reason + "; stabilizing before replan");
               }

               if ((goalOverride instanceof HashUtil.Inner_DataUtil || goalOverride instanceof u placementGoal && placementGoal.load())
                  && this.flush_I < 2
                  && this.poll()) {
                  this.flush_I++;
                  this.init_RefQueue.read_Z();
                  return this.load("Leaving the partial construction footing before replanning");
               }

               if (reason.startsWith("No route toward the target")) {
                  HashUtil.init_V(this.init_RefQueue.write_BlockPos());
                  if (Boolean.getBoolean("autism.ai.schematics") && HashUtil.init_I(this.init_RefQueue.write_BlockPos()) == 1) {
                     this.read_V_6();
                  }
               }

               ClassCache.UiHelper stalledNow = this.init_RefQueue.init_ClassCache_UiHelpe();
               AutismClientAddon.LOG
                  .info(
                     "[AI] travel {}: {}{} at {} (replans {}, stalls {}){}",
                     persistent ? "job" : "goto",
                     reason,
                     stalledNow == null ? "" : " - " + stalledNow.type() + " " + stalledNow.from().toShortString() + " -> " + stalledNow.to().toShortString(),
                     this.init_RefQueue.write_BlockPos().toShortString(),
                     this.write_I,
                     this.read_I,
                     stalledNow != null && !this.init_RefQueue.init_Str().isEmpty() ? ": " + this.init_RefQueue.init_Str() : ""
                  );
               if (!parkour
                  && !this.pop_Z
                  && !CharEncoder.init_Z()
                  && this.init_Minecraft.player.mayBuild()
                  && reason.startsWith("No route toward the target")
                  && this.read_Z_5()) {
                  this.pop_Z = true;
                  if (goalOverride instanceof u placementGoal && placementGoal.init_AABB() != null && !placementGoal.load()) {
                     AABB volume = placementGoal.init_AABB();
                     BlockPos outside = HashUtil.init_BlockPos(
                        new BlockPos((int)Math.floor(volume.minX) - 2, this.init_Minecraft.player.blockPosition().getY(), (int)Math.floor(volume.getCenter().z)),
                        12
                     );
                     this.load_ClassCache_JsonUti = ClassCache.Inner_JsonUtil.init_ClassCache_JsonUti_BlockPos_D(outside, 2.0);
                     this.tick_Z = true;
                     this.init_ClassCache_BufferH
                        .init_V_BlockPos_2(this.init_RefQueue.write_BlockPos(), this.load_ClassCache_JsonUti, this.init_ClassCache_DataUti().withDig());
                  } else {
                     this.init_ClassCache_BufferH.init(this.init_ClassCache_DataUti().withDig());
                     this.init_ClassCache_BufferH.init(this.init_RefQueue.write_BlockPos());
                  }

                  this.init_RefQueue.update_V();
                  return this.load(reason + "; sealed pocket: dig-enabled escape replan");
               } else {
                  ClassCache.UiHelper stalledMovement = this.init_RefQueue.init_ClassCache_UiHelpe();
                  if (completeRoute && stalledMovement != null) {
                     return this.init(stalledMovement, reason);
                  }

                  if (this.init_RefQueue.init_Z() && stalledMovement != null) {
                     this.init_RefQueue.close_V();
                     if (++this.open_I >= 3) {
                        return this.init(A.close, reason + "; went round in circles " + this.open_I + " times");
                     }

                     this.init_ClassCache_BufferH.init(stalledMovement.to(), this.init_RefQueue.write_BlockPos());
                     this.init_ClassCache_BufferH.init(this.init_ClassCache_DataUti());
                     this.init_ClassCache_BufferH.init(this.init_RefQueue.write_BlockPos());
                     this.init_RefQueue.update_V();
                     return this.load("Going round in circles; replanning without " + stalledMovement.to().toShortString());
                  } else {
                     boolean drifted = reason.contains("past its target") || reason.contains("slid below a flat step");
                     if (stalledMovement != null && !drifted) {
                        ClassCache.init_V_ClassCache_UiHelpe_J(stalledMovement, 120000000000L);
                     }

                     if (stalledMovement != null && !drifted) {
                        ClassCache.init_V(stalledMovement);
                     }

                     if (stalledMovement != null && !drifted) {
                        if (stalledMovement.type() == this.init_ClassCache_SyncMan && stalledMovement.to().equals(this.load_BlockPos)) {
                           this.read_BlockPos = stalledMovement.to();
                           ClassCache.load(stalledMovement, 120000000000L);
                           return this.init(A.close, reason + "; " + stalledMovement.type() + " stalled again with its type excluded");
                        }

                        this.init_ClassCache_SyncMan = stalledMovement.type();
                        this.load_BlockPos = stalledMovement.to();
                        this.load_Set.addAll(ClassCache.init(stalledMovement.type()));
                     }

                     this.read_I++;
                     this.write_I++;
                     ClassCache.Inner_HashUtil action = ClassCache.init_ClassCache_HashUti_I_I(this.read_I, this.write_I);
                     if (persistent && reason.startsWith("No route toward the target") && this.write_I >= 2) {
                        action = ClassCache.Inner_HashUtil.read;
                     }

                     if (action == ClassCache.Inner_HashUtil.read) {
                        return this.init(A.close, reason + "; no reachable route after repeated replans");
                     }

                     if (action == ClassCache.Inner_HashUtil.load && cell != null) {
                        this.init_ClassCache_BufferH.init(cell, this.init_RefQueue.write_BlockPos());
                     }

                     this.init_ClassCache_BufferH.init(this.init_ClassCache_DataUti());
                     this.init_ClassCache_BufferH.init(this.init_RefQueue.write_BlockPos());
                     this.init_RefQueue.update_V();
                     return this.load(reason + "; replanning (attempt " + this.write_I + ")");
                  }
               }
            }

            private AiOperation.Result init(ClassCache.UiHelper move, String reason) {
               BlockPos feet = this.init_RefQueue.write_BlockPos();
               String detail = this.init_RefQueue.init_Str();
               boolean drifted = detail.contains("past its target") || detail.contains("slid below") || detail.contains("below the takeoff");
               boolean cycled = this.init_RefQueue.init_Z();
               if (cycled) {
                  this.init_RefQueue.close_V();
               }

               this.write_I++;
               if (++this.read_I < 6 && (!cycled || ++this.open_I < 3)) {
                  if (!drifted || cycled || move.jump() != null && detail.contains("below the takeoff")) {
                     this.init_ClassCache_BufferH.init(move, feet);
                  }

                  this.init_ClassCache_BufferH.init(this.init_ClassCache_DataUti());
                  this.init_ClassCache_BufferH.init(feet);
                  this.init_RefQueue.update_V();
                  return this.load(reason + "; replanning from " + feet.toShortString());
               } else {
                  this.read_BlockPos = move.to();
                  return this.init(A.close, reason + "; repeatedly stalled near " + feet.toShortString());
               }
            }

            private void read_V_6() {
               BlockPos from = this.init_RefQueue.write_BlockPos();
               ClassCache.Inner_DataUtil caps = this.init_ClassCache_DataUti();
               ArrayList<ClassCache.UiHelper> edges = new ArrayList<>();
               int flags = ClassCache.Inner_RenderState.init_I_ClassCache_StateCa(
                  this.init_ClassCache_StateCa, caps, this.init_ClassCache_BufferH.init_Set(), from, edges
               );
               AutismClientAddon.LOG
                  .info(
                     "[AI] no-route detail: {} goal {} flags {} disabled {} blacklist {} floor {}",
                     from.toShortString(),
                     this.init_ClassCache_JsonUti,
                     flags,
                     caps.disabledTypes(),
                     this.init_ClassCache_BufferH.init_I(),
                     this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(from.getX(), from.getY() - 1, from.getZ())
                  );

               for (Direction direction : Plane.HORIZONTAL) {
                  BlockPos next = from.relative(direction);
                  AutismClientAddon.LOG
                     .info(
                        "[AI] no-route neighbor: {} enter {} banned {} feet {} head {} floor {}",
                        next.toShortString(),
                        this.init_ClassCache_JsonUti.init_Z(next),
                        this.init_ClassCache_BufferH.init_Set().contains(next.asLong()),
                        this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(next.getX(), next.getY(), next.getZ()),
                        this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(next.getX(), next.getY() + 1, next.getZ()),
                        this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(next.getX(), next.getY() - 1, next.getZ())
                     );
               }

               for (int i = 0; i < Math.min(16, edges.size()); i++) {
                  ClassCache.UiHelper edge = edges.get(i);
                  boolean allowed = this.init_ClassCache_JsonUti.init_Z(edge.to()) && !ClassCache.init_Z(edge);

                  for (BlockPos broken : edge.cellsToBreak()) {
                     allowed &= this.init_ClassCache_JsonUti.load(broken);
                  }

                  for (BlockPos placed : edge.cellsToPlace()) {
                     allowed &= this.init_ClassCache_JsonUti.read(placed);
                  }

                  AutismClientAddon.LOG
                     .info("[AI] no-route move: {} to {} allowed {} globalBan {}", edge.type(), edge.to().toShortString(), allowed, ClassCache.init_Z(edge));
               }

               ClassCache.Inner_TextFormat check = new ClassCache.Inner_TextFormat(
                  this.init_ClassCache_StateCa, from, this.init_ClassCache_JsonUti, caps, this.init_ClassCache_BufferH.init_Set()
               );
               if (!check.init_Z_I(512)) {
                  check.init_V();
               }

               ClassCache.Inner_PacketUtil result = check.init_ClassCache_PacketU();
               AutismClientAddon.LOG
                  .info(
                     "[AI] no-route live check: reaches {} moves {} expanded {} exhausted {} pending {}",
                     result.reachesGoal(),
                     result.movements().size(),
                     result.expanded(),
                     result.exhausted(),
                     result.hitPending()
                  );
            }

            AiOperation.Result update_AiOperation_Result() {
               if ((allowPillar || allowBridge)
                  && this.poll_I < 4
                  && this.init_Minecraft.player.mayBuild()
                  && (this.init_Minecraft.player.onGround() || this.init_Minecraft.player.isInWater())) {
                  BlockPos feet = this.init_Minecraft.player.blockPosition();
                  BlockPos wet = null;

                  for (BlockPos body : List.of(feet, feet.above())) {
                     for (Direction d : Direction.values()) {
                        if (d != Direction.DOWN) {
                           BlockPos n = body.relative(d);
                           if (!n.equals(feet)
                              && !n.equals(feet.above())
                              && wet == null
                              && this.init_Minecraft.level.getFluidState(n).is(FluidTags.WATER)
                              && this.init_Minecraft.level.getBlockState(n).canBeReplaced()) {
                              if (!HashUtil.init_Z_ClassCache_JsonUti(this.init_ClassCache_JsonUti, allowPillar, allowBridge, n)) {
                                 this.pop_I++;
                              } else {
                                 wet = n.immutable();
                              }
                           }
                        }
                     }
                  }

                  if (wet == null) {
                     return null;
                  }

                  Inventory inventory = this.init_Minecraft.player.getInventory();
                  Item item = null;
                  int bestRank = 2147483647;

                  for (int slot = 0; slot < 36; slot++) {
                     ItemStack stack = inventory.getItem(slot);
                     if (stack.getItem() instanceof BlockItem block && !(block.getBlock() instanceof FallingBlock) && UtilCore.init(stack, inventory)) {
                        int rank = UtilCore.init_I(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                        if (rank >= 0 && rank < bestRank) {
                           bestRank = rank;
                           item = stack.getItem();
                        }
                     }
                  }

                  if (item == null) {
                     return null;
                  }

                  this.poll_I++;
                  this.write_BlockPos = wet;
                  this.push_I = 0;
                  this.init_RefQueue.read_Z();
                  this.init_DataPipe.init_V_BlockPos_Item(wet, item);
                  AutismClientAddon.LOG.info("[AI] travel: no way out of {} beside water; blocking the water at {}", feet.toShortString(), wet.toShortString());
                  return this.load("Blocking the water at " + wet.toShortString());
               } else {
                  return null;
               }
            }

            boolean read_Z_5() {
               BlockPos start = this.init_Minecraft.player.blockPosition();
               AABB volume = goalOverride instanceof u placementGoal ? placementGoal.init_AABB() : null;
               ArrayDeque<BlockPos> queue = new ArrayDeque<>();
               HashSet<Long> seen = new HashSet<>();
               queue.add(start);
               seen.add(start.asLong());

               while (!queue.isEmpty() && seen.size() < 2000) {
                  BlockPos cell = queue.poll();
                  if (volume != null) {
                     if (!volume.contains(cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5)) {
                        return false;
                     }
                  } else if (Math.max(Math.abs(cell.getX() - start.getX()), Math.abs(cell.getZ() - start.getZ())) >= 6) {
                     return false;
                  }

                  for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                     for (int dy = -3; dy <= 1; dy++) {
                        BlockPos next = cell.offset(d[0], dy, d[1]);
                        if (seen.add(next.asLong()) && this.init_Z_BlockPos_4(next)) {
                           queue.add(next);
                        }
                     }
                  }
               }

               return true;
            }

            boolean init(BlockPos feet, int r) {
               for (BlockPos p : BlockPos.betweenClosed(feet.offset(-r, -2, -r), feet.offset(r, 0, r))) {
                  if (this.init_Minecraft.level.getFluidState(p).is(FluidTags.LAVA)) {
                     return true;
                  }
               }

               return false;
            }

            boolean init_Z_BlockPos_4(BlockPos cell) {
               return this.init_Minecraft.level.getBlockState(cell).getCollisionShape(this.init_Minecraft.level, cell).isEmpty()
                  && this.init_Minecraft.level.getBlockState(cell.above()).getCollisionShape(this.init_Minecraft.level, cell.above()).isEmpty()
                  && !this.init_Minecraft.level.getBlockState(cell.below()).getCollisionShape(this.init_Minecraft.level, cell.below()).isEmpty();
            }

            boolean write_Z_4() {
               LocalPlayer player = this.init_Minecraft.player;
               if (player == null) {
                  return false;
               }

               boolean drowning = player.isInWater()
                  && player.getAirSupply() < player.getMaxAirSupply()
                  && this.init_Minecraft.level.getFluidState(player.blockPosition().above()).is(FluidTags.WATER);
               boolean inLava = player.isInLava();
               if ((drowning || inLava) && ++this.update_I <= 60) {
                  this.init_Minecraft.options.keyJump.setDown(true);
                  this.load_V_8();
                  return true;
               }

               this.update_I = 0;
               this.init_Minecraft.options.keyJump.setDown(false);
               this.init_Minecraft.options.keyUp.setDown(false);
               this.init_Minecraft.options.keyDown.setDown(false);
               this.init_Minecraft.options.keyLeft.setDown(false);
               this.init_Minecraft.options.keyRight.setDown(false);
               if (!inLava && !player.onGround() && !player.isInWater() && !player.onClimbable() && player.getDeltaMovement().y < -0.08) {
                  return true;
               }

               if (player.onGround() && !player.isInWater() && player.getDeltaMovement().horizontalDistanceSqr() > 9.0E-4 && ++this.tick_I <= 12) {
                  this.init_Minecraft.options.keyShift.setDown(true);
                  return true;
               }

               if (this.tick_I > 0) {
                  this.tick_I = 0;
                  this.init_Minecraft.options.keyShift.setDown(false);
               }

               return false;
            }

            void load_V_8() {
               this.init_Minecraft.options.keyUp.setDown(false);
               this.init_Minecraft.options.keyDown.setDown(false);
               this.init_Minecraft.options.keyLeft.setDown(false);
               this.init_Minecraft.options.keyRight.setDown(false);
               BlockPos pocket = HashUtil.init(
                  this.init_Minecraft.player.blockPosition(),
                  cell -> this.init_Minecraft.level.getBlockState(cell).getCollisionShape(this.init_Minecraft.level, cell).isEmpty()
                     && this.init_Minecraft.level.getFluidState(cell).isEmpty()
               );
               if (pocket != null) {
                  LocalPlayer player = this.init_Minecraft.player;
                  double dx = pocket.getX() + 0.5 - player.getX();
                  double dz = pocket.getZ() + 0.5 - player.getZ();
                  if (dx * dx + dz * dz >= 1.5625) {
                     this.init_DataUtil.init_V_Vec3(Vec3.atCenterOf(pocket));
                     this.init_DataUtil.load_V();
                     this.init_Minecraft.options.keyUp.setDown(true);
                  } else {
                     double yaw = Math.toRadians(player.getYRot());
                     double forward = dx * -Math.sin(yaw) + dz * Math.cos(yaw);
                     double right = dx * -Math.cos(yaw) + dz * -Math.sin(yaw);
                     this.init_Minecraft.options.keyUp.setDown(forward > 0.15);
                     this.init_Minecraft.options.keyDown.setDown(forward < -0.15);
                     this.init_Minecraft.options.keyRight.setDown(right > 0.15);
                     this.init_Minecraft.options.keyLeft.setDown(right < -0.15);
                  }
               }
            }

            private void init_V_ClassCache_PacketU(ClassCache.Inner_PacketUtil segment) {
               if (this.read_D < 0.0) {
                  this.read_D = (System.nanoTime() - this.load_J) / 1000000.0;
               }

               this.init_RefQueue.init_V_ClassCache_NetHelp(this.init_ClassCache_NetHelp);
               this.init_RefQueue.init_V_List_Z(segment.movements(), segment.reachesGoal());
               this.init_ClassCache_BufferH.init(segment);
            }

            private AiOperation.Result open_AiOperation_Result() {
               if (this.init_ArrayUtil == null && !cx.init_Z()) {
                  ArrayUtil.Inner_DataUtil loss = ArrayUtil.init_ArrayUtil_DataUtil_Minecraft_List(
                     this.init_Minecraft, this.init_ClassCache_PacketU.movements()
                  );
                  if (loss.damage() == 0.0F) {
                     this.read(false);
                     return this.load("Found a route with a harmless landing");
                  }

                  this.init_ArrayUtil = new ArrayUtil(this.init_Minecraft, loss, this::read, () -> this.sync_Z = true);
                  if (!this.init_ArrayUtil.init_Z()) {
                     this.init_ArrayUtil = null;
                  }
               }

               return this.load("Waiting for fall damage approval");
            }

            private void read(boolean enableNoFall) {
               if (!this.flush_Z && this.init_Minecraft.level == this.init_Obj && this.init_Minecraft.player != null && this.init_Minecraft.player.isAlive()) {
                  if (enableNoFall) {
                     Module noFall = al.init_Module_Str("no-fall");
                     if (noFall != null && !noFall.isEnabled()) {
                        this.init_aP_StreamUtil = noFall.borrowEnabled(true);
                     }
                  }

                  ClassCache.Inner_PacketUtil route = this.init_ClassCache_PacketU;
                  this.init_ClassCache_PacketU = null;
                  this.init_ArrayUtil = null;
                  this.update_Z = false;
                  if (this.open_Z() && this.read_BlockPos_2().equals(route.movements().getFirst().from())) {
                     this.init_V_ClassCache_PacketU(route);
                  } else {
                     this.init_ClassCache_BufferH.init(this.read_BlockPos_2());
                  }
               } else {
                  this.sync_Z = true;
               }
            }

            AiOperation.Result sync_AiOperation_Result() {
               if (this.init_ClassCache_NetHelp == ClassCache.Inner_NetHelper.load) {
                  this.update_Z = true;
                  this.init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.read;
                  if (this.read_D >= 0.0) {
                     this.init_RefQueue.read_Z();
                  }

                  this.init_ClassCache_BufferH.init_V_BlockPos_2(this.read_BlockPos_2(), this.init_ClassCache_JsonUti, this.init_ClassCache_DataUti());
                  return this.load("Checking whether a route requires a damaging fall");
               } else {
                  String reason = "No route toward the target in loaded terrain";
                  if (Boolean.getBoolean("autism.ai.schematics")) {
                     this.read_V_6();
                  }

                  if (this.init_Minecraft.level != null && System.nanoTime() - HashUtil.write > 10000000000L) {
                     HashUtil.write = System.nanoTime();
                     AutismClientAddon.LOG
                        .info(
                           "[AI] no route from {} to {}: {}",
                           this.init_RefQueue.write_BlockPos().toShortString(),
                           target.toShortString(),
                           n.init(this.init_Minecraft.level, this.init_RefQueue.write_BlockPos(), target, 4, 2)
                        );
                  }

                  if (this.write_Z_4()) {
                     return this.load(reason + "; stabilizing before replan");
                  }

                  if ((goalOverride instanceof HashUtil.Inner_DataUtil || goalOverride instanceof u placementGoal && placementGoal.load())
                     && this.flush_I < 2
                     && this.poll()) {
                     this.flush_I++;
                     this.init_RefQueue.read_Z();
                     return this.load("Leaving the partial construction footing before replanning");
                  }

                  if (!parkour && !this.pop_Z && !CharEncoder.init_Z() && this.init_Minecraft.player.mayBuild() && this.read_Z_5()) {
                     this.pop_Z = true;
                     ClassCache.Inner_DataUtil escape = this.init_ClassCache_DataUti().withDig();
                     if (this.init(this.init_RefQueue.write_BlockPos(), 3)) {
                        HashSet<ClassCache.Inner_SyncManager> disabled = new HashSet<>(escape.disabledTypes());
                        disabled.add(ClassCache.Inner_SyncManager.tick);
                        escape = escape.withDisabledTypes(disabled);
                     }

                     this.init_ClassCache_BufferH.init(escape);
                     this.init_ClassCache_BufferH.init(this.init_RefQueue.write_BlockPos());
                     this.init_RefQueue.update_V();
                     return this.load(reason + "; sealed pocket: dig-enabled escape replan");
                  } else {
                     AiOperation.Result plug = this.update_AiOperation_Result();
                     if (plug != null) {
                        return plug;
                     }

                     AutismClientAddon.LOG
                        .info("[AI] travel {}: {} at {}", persistent ? "job" : "goto", reason, this.init_RefQueue.write_BlockPos().toShortString());
                     return this.init(A.close, reason);
                  }
               }
            }

            String init_Str_Vec3(Vec3 pos) {
               BlockPos shown = persistent && this.init_RefQueue.read_BlockPos() != null ? this.init_RefQueue.read_BlockPos() : target;
               int remaining = (int)Math.round(Math.sqrt(pos.distanceToSqr(Vec3.atBottomCenterOf(shown))));
               String extra = this.init_ClassCache_BufferH.load_Z() ? ", planning ahead" : "";
               return "Travelling to " + shown.toShortString() + " (" + remaining + "m" + extra + ")";
            }

            ClassCache.Inner_DataUtil init_ClassCache_DataUti() {
               boolean sprint = options.sprint() && this.init_Minecraft.player.getFoodData().getFoodLevel() > 6;
               if (this.init_ClassCache_StateCa instanceof ClassCache.Inner_StreamUtil snapshot) {
                  snapshot.init_V_ItemStackArr(this.init_ItemStackArr());
               } else if (this.init_ClassCache_StateCa instanceof RingBuffer live) {
                  live.init_V_ItemStackArr_2(this.init_ItemStackArr());
               }

               boolean jumpBoost = this.init_Minecraft.player.hasEffect(MobEffects.JUMP_BOOST);
               boolean slowFalling = this.init_Minecraft.player.hasEffect(MobEffects.SLOW_FALLING);
               boolean levitation = this.init_Minecraft.player.hasEffect(MobEffects.LEVITATION);
               boolean recovery = CharEncoder.init_Z() && this.init_Minecraft.player.getHealth() <= 4.0F;
               boolean build = this.init_Minecraft.player.mayBuild();
               Set<ClassCache.Inner_SyncManager> withheld = new HashSet<>(this.load_Set);
               if (!options.bunnyhop()) {
                  withheld.add(ClassCache.Inner_SyncManager.reset);
               }

               if (!options.allowClimb()) {
                  withheld.add(ClassCache.Inner_SyncManager.get);
               }

               if (!options.allowDoors()) {
                  withheld.add(ClassCache.Inner_SyncManager.set);
               }

               if (this.init_ClassCache_JsonUti != null) {
                  withheld.addAll(this.init_ClassCache_JsonUti.init_Set());
               }

               return new ClassCache.Inner_DataUtil(
                     allowDig && build,
                     allowPillar && build && !recovery,
                     allowBridge && build && !recovery,
                     sprint && !recovery,
                     this.update_Z_4(),
                     !recovery,
                     recovery ? ClassCache.Inner_IoWorker.load : strategy,
                     this.init_ClassCache_PoolEnt(),
                     jumpBoost,
                     slowFalling,
                     levitation,
                     breakReluctance
                  )
                  .withDisabledTypes(withheld)
                  .withConstructionBlocks(this.read_I_4())
                  .withMovementLimits(options.maxFall(), options.avoidWater())
                  .withFarmlandRepair(goalOverride instanceof HashUtil.Inner_DataUtil || goalOverride instanceof u placement && placement.load())
                  .withSupportedBridges(this.init_ClassCache_JsonUti != null && this.init_ClassCache_JsonUti.init_Z())
                  .withFallPolicy(this.init_ClassCache_NetHelp);
            }

            int read_I_4() {
               int count = 0;
               int planks = 0;
               int stone = 0;

               for (int i = 0; i < (allowInventory ? 36 : 9); i++) {
                  ItemStack stack = this.init_Minecraft.player.getInventory().getItem(i);
                  int owned = this.init_ClassCache_JsonUti == null ? 0 : this.init_ClassCache_JsonUti.init(stack);
                  if (owned > 0) {
                     count += owned;
                  } else if (this.init_Z_ItemStack_2(stack)) {
                     if (stack.is(ItemTags.PLANKS)) {
                        planks += stack.getCount();
                     } else if (UtilCore.init(stack)) {
                        stone += stack.getCount();
                     } else {
                        count += stack.getCount();
                     }
                  }
               }

               count += UtilCore.write() > 0 ? Math.min(stone, UtilCore.read(this.init_Minecraft.player.getInventory())) : stone;
               if (!AutismLiteVariant.enabled()) {
                  planks += aV.init(this.init_Minecraft.player.getInventory());
               }

               if (UtilCore.init_I() > 0) {
                  planks = Math.min(planks, UtilCore.init(this.init_Minecraft.player.getInventory()));
               }

               return count + planks;
            }

            boolean update_Z_4() {
               if (this.update_I() < 0 && (!allowInventory || this.write_I_2() < 0)) {
                  return AutismLiteVariant.enabled()
                     ? false
                     : aV.init(this.init_Minecraft.player.getInventory()) > 0 && UtilCore.init(this.init_Minecraft.player.getInventory()) > 0;
               } else {
                  return true;
               }
            }

            int write_I_2() {
               List<UtilCore.Inner_CachePool> backpack = new ArrayList<>(27);

               for (int slot = 9; slot < 36; slot++) {
                  ItemStack stack = this.init_Minecraft.player.getInventory().getItem(slot);
                  if (stack.getItem() instanceof BlockItem block && UtilCore.init(stack, this.init_Minecraft.player.getInventory())) {
                     backpack.add(
                        new UtilCore.Inner_CachePool(
                           BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
                           stack.getCount(),
                           block.getBlock() instanceof FallingBlock,
                           block.getBlock()
                              .defaultBlockState()
                              .isCollisionShapeFullBlock(this.init_Minecraft.level, this.init_Minecraft.player.blockPosition()),
                           block.getBlock().defaultBlockState().hasBlockEntity()
                        )
                     );
                  } else {
                     backpack.add(new UtilCore.Inner_CachePool(null, 0, false, false, false));
                  }
               }

               int index = UtilCore.init(backpack, null);
               return index < 0 ? -1 : 9 + index;
            }

            ClassCache.Inner_PoolEntry init_ClassCache_PoolEnt() {
               if (this.init_Minecraft.level == null) {
                  return ClassCache.Inner_PoolEntry.write;
               }

               final ClassCache.Inner_PoolEntry dragon;
               if (AutismLiteVariant.enabled()) {
                  dragon = ClassCache.Inner_PoolEntry.write;
               } else {
                  dragon = autismclient.ai.r.init_ClassCache_PoolEnt(this.init_Minecraft);
               }

               if (strategy != ClassCache.Inner_IoWorker.load) {
                  return dragon == ClassCache.Inner_PoolEntry.write ? ClassCache.Inner_PoolEntry.write : new ClassCache.Inner_PoolEntry() {
                     @Override
                     public double surcharge(int x, int y, int z) {
                        return 0.0;
                     }

                     @Override
                     public double init(int x, int y, int z) {
                        return dragon.surcharge(x, y, z);
                     }
                  };
               }

               final List<BlockPos> hostiles = new ArrayList<>();
               final List<BlockPos> heavy = new ArrayList<>();
               List<Vec3> shooters = new ArrayList<>();
               List<Double> charges = new ArrayList<>();

               for (Entity entity : this.init_Minecraft.level.entitiesForRendering()) {
                  if (entity instanceof Enemy && entity.isAlive() && entity.distanceToSqr(this.init_Minecraft.player) <= 576.0 && !HashUtil.init(entity)) {
                     hostiles.add(entity.blockPosition());
                     if (entity instanceof PiglinBrute || entity instanceof Hoglin || entity instanceof Zoglin || entity instanceof WitherSkeleton) {
                        heavy.add(entity.blockPosition());
                     }

                     if (f.init(entity)) {
                        shooters.add(entity.getEyePosition());
                        charges.add(f.load(entity) ? 40.0 : 10.0);
                     }
                  }
               }

               final ClassCache.Inner_PoolEntry mobs = ClassCache.Inner_PoolEntry.init(hostiles);
               final ClientLevel level = this.init_Minecraft.level;
               final ClassCache.Inner_PoolEntry sight = f.init(level, shooters, charges);
               final boolean nether = level.dimension() == Level.NETHER;
               final var outerSelf = this;
               return new ClassCache.Inner_PoolEntry() {
                  @Override
                  public double surcharge(int x, int y, int z) {
                     double seen = sight.surcharge(x, y, z);
                     return mobs.surcharge(x, y, z)
                        + load(level, x, y, z)
                        + seen
                        + init_D(level, x, y, z)
                        + outerSelf.init(heavy, x, y, z)
                        + ((seen > 0.0 || nether && outerSelf.init(hostiles, x, y, z, 10)) && init_Z(level, x, y, z) ? 60.0 : 0.0);
                  }

                  @Override
                  public double init(int fx, int fy, int fz, int tx, int ty, int tz) {
                     return load(level, tx, ty, tz) > 0.0 && load(level, fx, fy, fz) == 0.0 ? 40.0 : 0.0;
                  }

                  @Override
                  public double init(int x, int y, int z) {
                     return dragon.surcharge(x, y, z);
                  }
               };
            }

            static double init_D(ClientLevel level, int x, int y, int z) {
               try {
                  for (int dx = -1; dx <= 1; dx++) {
                     for (int dz = -1; dz <= 1; dz++) {
                        if (dx != 0 || dz != 0) {
                           for (int dy = -1; dy <= 0; dy++) {
                              if (level.getFluidState(new BlockPos(x + dx, y + dy, z + dz)).is(FluidTags.LAVA)) {
                                 return 8.0;
                              }
                           }
                        }
                     }
                  }

                  return 0.0;
               } catch (RuntimeException raced) {
                  return 0.0;
               }
            }

            static boolean init_Z(ClientLevel level, int x, int y, int z) {
               try {
                  for (int dx = -1; dx <= 1; dx++) {
                     for (int dz = -1; dz <= 1; dz++) {
                        if (dx != 0 || dz != 0) {
                           boolean open = true;

                           for (int dy = 0; dy >= -5 && open; dy--) {
                              BlockPos c = new BlockPos(x + dx, y + dy, z + dz);
                              if (!level.getBlockState(c).getCollisionShape(level, c).isEmpty() || level.getFluidState(c).is(FluidTags.WATER)) {
                                 open = false;
                              }
                           }

                           if (open) {
                              return true;
                           }
                        }
                     }
                  }

                  return false;
               } catch (RuntimeException raced) {
                  return false;
               }
            }

            static boolean init(List<BlockPos> positions, int x, int y, int z, int r) {
               for (BlockPos m : positions) {
                  double dx = x - m.getX();
                  double dy = y - m.getY();
                  double dz = z - m.getZ();
                  if (dx * dx + dy * dy + dz * dz <= r * r) {
                     return true;
                  }
               }

               return false;
            }

            static double init(List<BlockPos> heavy, int x, int y, int z) {
               if (heavy.isEmpty()) {
                  return 0.0;
               }

               double nearestSqr = 1.7976931348623157E308;

               for (BlockPos m : heavy) {
                  double dx = x - m.getX();
                  double dy = y - m.getY();
                  double dz = z - m.getZ();
                  nearestSqr = Math.min(nearestSqr, dx * dx + dy * dy + dz * dz);
               }

               return nearestSqr >= 400.0 ? 0.0 : 400.0 * (1.0 - Math.sqrt(nearestSqr) / 20.0);
            }

            static double load(ClientLevel level, int x, int y, int z) {
               if (level.dimension() != Level.OVERWORLD) {
                  return 0.0;
               }

               BlockPos p = new BlockPos(x, y, z);

               try {
                  if (!level.getBlockState(p).isAir() || level.getBrightness(LightLayer.SKY, p) > 0 || level.getBrightness(LightLayer.BLOCK, p) >= 8) {
                     return 0.0;
                  }
               } catch (RuntimeException raced) {
                  return 0.0;
               }

               return FileCache.write_Z(p) ? 0.0 : 12.0;
            }

            int update_I() {
               for (int i = 0; i < 9; i++) {
                  if (this.init_Z_ItemStack_2(this.init_Minecraft.player.getInventory().getItem(i))) {
                     return i;
                  }
               }

               return -1;
            }

            boolean init_Z_ItemStack_2(ItemStack stack) {
               return stack.getItem() instanceof BlockItem block
                  && UtilCore.init_Z(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                  && UtilCore.init(stack, this.init_Minecraft.player.getInventory())
                  && !(block.getBlock() instanceof FallingBlock)
                  && block.getBlock().defaultBlockState().isCollisionShapeFullBlock(this.init_Minecraft.level, this.init_Minecraft.player.blockPosition())
                  && !block.getBlock().defaultBlockState().hasBlockEntity();
            }

            ItemStack[] init_ItemStackArr() {
               long revision = ca.init_J();
               if (this.init_ItemStackArr != null && revision == this.sync_J) {
                  return this.init_ItemStackArr;
               }

               this.init_ItemStackArr = this.load_ItemStackArr();
               this.sync_J = revision;
               return this.init_ItemStackArr;
            }

            ItemStack[] load_ItemStackArr() {
               int slots = 36;
               ItemStack[] snapshot = new ItemStack[slots];

               for (int i = 0; i < slots; i++) {
                  snapshot[i] = this.init_Minecraft.player.getInventory().getItem(i).copy();
               }

               return snapshot;
            }

            BlockPos read_BlockPos_2() {
               return i.init(this.init_Minecraft);
            }

            boolean sync_Z_3() {
               ClassCache.Inner_JsonUtil interactionGoal = HashUtil.read(this.init_ClassCache_JsonUti);
               if ((this.flush_Z_2() || interactionGoal instanceof HashUtil.Inner_StreamUtil fluid && fluid.init_Z && this.init_Minecraft.player.isInWater())
                  && !this.init_RefQueue.open_Z()) {
                  BlockPos current = this.init_RefQueue.write_BlockPos();
                  if (!this.init_ClassCache_JsonUti.init_Z(current)) {
                     return false;
                  }

                  if (interactionGoal instanceof HashUtil.Inner_StreamUtil fluid) {
                     if (fluid.init(this.init_Minecraft)) {
                        return true;
                     }

                     if (this.init_ClassCache_JsonUti.isInGoal(current.getX(), current.getY(), current.getZ()) && fluid.update(current)) {
                        this.init_ClassCache_BufferH.init(current);
                     }

                     return false;
                  } else if (interactionGoal instanceof u placement) {
                     return !HashUtil.init(this.init_RefQueue.init_RefQueue_TaskQueue(), this.init_Minecraft.player.getDeltaMovement())
                        ? false
                        : placement.init(this.init_Minecraft);
                  } else if (!(interactionGoal instanceof q interaction)) {
                     return this.init_ClassCache_JsonUti.init_Z_I(current.getX(), current.getY(), current.getZ(), this.init_ClassCache_StateCa);
                  } else if (interaction.load_Z() && !RefQueue.init_Z_BlockPos(interaction.load_BlockPos())) {
                     interaction.init_V(current);
                     return false;
                  } else {
                     BlockHitResult hit = CachePool.load(this.init_Minecraft, interaction.load_BlockPos(), interaction.init_Direction());
                     return hit != null && CachePool.init(this.init_Minecraft).distanceTo(hit.getLocation()) <= interaction.load_D() + 0.25;
                  }
               } else {
                  return false;
               }
            }

            boolean flush_Z_2() {
               return RefQueue.flush_Z();
            }

            boolean close_Z() {
               if (this.init_Minecraft.level.noCollision(this.init_Minecraft.player, this.init_Minecraft.player.getBoundingBox().move(0.0, 1.0, 0.0))) {
                  return false;
               }

               BlockPos current = this.read_BlockPos_2();

               for (Direction direction : List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
                  for (BlockPos cell : List.of(current.relative(direction), current.relative(direction).above())) {
                     if (this.init_Minecraft.level.isInsideBuildHeight(cell)
                        && this.init_Minecraft.level.getChunkSource().getChunk(cell.getX() >> 4, cell.getZ() >> 4, ChunkStatus.FULL, false) != null) {
                        BlockState state = this.init_Minecraft.level.getBlockState(cell);
                        if (state.isCollisionShapeFullBlock(this.init_Minecraft.level, cell)
                           && state.requiresCorrectToolForDrops()
                           && r.load(this.init_Minecraft, state, true) < 0) {
                           continue;
                        }

                        return false;
                     }

                     return false;
                  }
               }

               return true;
            }

            boolean open_Z() {
               return this.flush_Z_2()
                  || this.init_Minecraft.player.isInLava()
                  || this.init_Minecraft.player.getAbilities().flying
                  || this.init_Minecraft.level.getBlockState(this.init_Minecraft.player.blockPosition()).is(Blocks.COBWEB)
                  || this.init_Minecraft.level.getBlockState(this.init_Minecraft.player.blockPosition().above()).is(Blocks.COBWEB);
            }

            boolean poll() {
               LocalPlayer player = this.init_Minecraft.player;
               if (player.onGround() && this.init_Minecraft.level.noCollision(player, player.getBoundingBox().deflate(0.001))) {
                  BlockPos actualFeet = player.blockPosition();
                  if (!HashUtil.init_Z_VoxelShape(
                     this.init_Minecraft.level.getBlockState(actualFeet.below()).getCollisionShape(this.init_Minecraft.level, actualFeet.below()),
                     this.init_Minecraft.level.getBlockState(actualFeet).getCollisionShape(this.init_Minecraft.level, actualFeet),
                     this.init_Minecraft.level.getBlockState(actualFeet.above()).getCollisionShape(this.init_Minecraft.level, actualFeet.above())
                  )) {
                     return false;
                  }

                  Vec3 best = null;
                  double bestScore = 1.7976931348623157E308;
                  boolean bestJump = false;
                  boolean bestClimb = false;
                  BitSetUtil collisionWorld = new BitSetUtil(this.init_Minecraft.level, player);
                  BlockPos start = player.blockPosition();

                  for (int dx = -2; dx <= 2; dx++) {
                     for (int dz = -2; dz <= 2; dz++) {
                        for (int dy = -1; dy <= 1; dy++) {
                           BlockPos cell = start.offset(dx, dy, dz);
                           ClassCache.Inner_FileCache feetProbe = this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(cell.getX(), cell.getY(), cell.getZ());
                           ClassCache.Inner_FileCache headProbe = this.init_ClassCache_StateCa
                              .init_ClassCache_FileCac_I(cell.getX(), cell.getY() + 1, cell.getZ());
                           boolean climb = options.allowClimb()
                              && feetProbe.loaded()
                              && feetProbe.climbable()
                              && !feetProbe.lava()
                              && !feetProbe.hazardFeet()
                              && headProbe.loaded()
                              && (headProbe.passable() || headProbe.climbable())
                              && !headProbe.lava()
                              && !headProbe.hazardFeet()
                              && collisionWorld.sync(cell.getX(), cell.getY(), cell.getZ());
                           if (climb
                              || ClassCache.Inner_RenderState.init_Z_ClassCache_FileCac(
                                 feetProbe,
                                 this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(cell.getX(), cell.getY() + 1, cell.getZ()),
                                 this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(cell.getX(), cell.getY() - 1, cell.getZ())
                              )) {
                              BlockState floor = this.init_Minecraft.level.getBlockState(cell.below());
                              VoxelShape shape = floor.getCollisionShape(this.init_Minecraft.level, cell.below());
                              if (climb
                                 || !shape.isEmpty()
                                    && floor.getFluidState().isEmpty()
                                    && !floor.is(Blocks.FARMLAND)
                                    && !floor.is(Blocks.MAGMA_BLOCK)
                                    && !floor.is(Blocks.CACTUS)) {
                                 double top = climb ? 1.0 : shape.max(Axis.Y);
                                 if (!(top < 0.05) && !(top > 1.5)) {
                                    Vec3 destination = new Vec3(cell.getX() + 0.5, cell.getY() - 1 + top, cell.getZ() + 0.5);
                                    Vec3 delta = destination.subtract(player.position());
                                    if (!(delta.lengthSqr() < 0.03)
                                       && (!climb || !(Math.abs(delta.y) > 0.08))
                                       && !(delta.y < -0.6)
                                       && !(delta.y > 1.25)
                                       && this.init_Minecraft.level.noCollision(player, player.getBoundingBox().move(delta).deflate(0.001))) {
                                       boolean jump = delta.y > 0.6;
                                       if ((!jump || !ClassCache.Inner_CachePool.init() || !this.load(player.blockPosition()))
                                          && (jump || RefQueue.init_Z_Vec3_Vec3(player.position(), destination))) {
                                          if (jump) {
                                             float heading = (float)(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0);
                                             TableIndex.UiHelper launch = TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(
                                                collisionWorld, NodeWalker.Inner_StreamUtil.of(player), heading, false, cell, destination.y
                                             );
                                             if (launch.runTicks() != 0 || launch.margin() < 0.08) {
                                                continue;
                                             }
                                          }

                                          boolean clear = true;
                                          int samples = Math.max(8, (int)Math.ceil(Math.hypot(delta.x, delta.z) * 8.0));

                                          for (int i = 1; !jump && i <= samples; i++) {
                                             AABB box = player.getBoundingBox().move(delta.x * i / samples, Math.max(0.0, delta.y), delta.z * i / samples);
                                             BlockPos sample = BlockPos.containing(box.getCenter().x, box.minY, box.getCenter().z);
                                             boolean rung = climb && collisionWorld.sync(sample.getX(), sample.getY(), sample.getZ());
                                             if (!this.init_Minecraft.level.noCollision(player, box.deflate(0.001))
                                                || !rung && this.init_Minecraft.level.noCollision(player, box.move(0.0, -Math.abs(delta.y) - 0.08, 0.0))) {
                                                clear = false;
                                                break;
                                             }
                                          }

                                          double score = delta.length() * 4.5 + this.init_ClassCache_JsonUti.heuristic(cell.getX(), cell.getY(), cell.getZ());
                                          if (clear && score < bestScore) {
                                             best = destination;
                                             bestScore = score;
                                             bestJump = jump;
                                             bestClimb = climb;
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  this.init_Vec3 = best;
                  this.open_Z = bestJump;
                  this.poll_Z = bestClimb;
                  this.push_Z = false;
                  this.sync_I = 0;
                  if (best != null && Boolean.getBoolean("autism.navigation.debug")) {
                     AutismClientAddon.LOG.info("[AI] footing exit {} -> {} (jump {})", player.position(), best, bestJump);
                  }

                  return best != null;
               } else {
                  return false;
               }
            }

            boolean load(BlockPos feet) {
               for (Direction side : Plane.HORIZONTAL) {
                  BlockPos column = feet.relative(side);
                  boolean floor = false;

                  for (int down = 0; down <= 4 && !floor; down++) {
                     BlockPos p = column.below(down);
                     BlockState state = this.init_Minecraft.level.getBlockState(p);
                     floor = !state.getCollisionShape(this.init_Minecraft.level, p).isEmpty() || !state.getFluidState().isEmpty();
                  }

                  if (!floor) {
                     return true;
                  }
               }

               return false;
            }

            AiOperation.Result flush_AiOperation_Result() {
               LocalPlayer player = this.init_Minecraft.player;
               double distance = player.position().distanceToSqr(this.init_Vec3);
               double horizontal = Math.pow(player.getX() - this.init_Vec3.x, 2.0) + Math.pow(player.getZ() - this.init_Vec3.z, 2.0);
               this.init_Minecraft.options.keyJump.setDown(false);
               this.init_Minecraft.options.keyShift.setDown(this.push_Z || this.poll_Z && player.onClimbable());
               if (this.push_Z) {
                  player.setSprinting(false);
               }

               this.init_Minecraft.options.keyDown.setDown(false);
               this.init_Minecraft.options.keyLeft.setDown(false);
               this.init_Minecraft.options.keyRight.setDown(false);
               boolean reachedRung = this.poll_Z && player.onClimbable() && horizontal < 0.03;
               double tolerance = this.push_Z ? 0.0025 : 0.03;
               if ((!player.onGround() || !(distance < tolerance)) && !reachedRung && ++this.sync_I <= (this.push_Z ? 48 : 24)) {
                  this.init_DataUtil.init_V_DataUtil_DataUtil(DataUtil.Inner_DataUtil.read_DataUtil_DataUtil);
                  if (horizontal >= tolerance) {
                     this.init_DataUtil
                        .init_V_ce_DataUtil(new ce.Inner_DataUtil(NodeWalker.init_F_D(player.getX(), player.getZ(), this.init_Vec3.x, this.init_Vec3.z), 0.0F));
                  }

                  this.init_DataUtil.load_V();
                  this.init_Minecraft.options.keyUp.setDown(horizontal >= tolerance && this.init_DataUtil.load_Z());
                  if (this.open_Z && this.init_DataUtil.load_Z()) {
                     BitSetUtil collisionWorld = new BitSetUtil(this.init_Minecraft.level, player);
                     NodeWalker.Inner_StreamUtil state = NodeWalker.Inner_StreamUtil.of(player);
                     BlockPos landing = BlockPos.containing(this.init_Vec3.x, Math.ceil(this.init_Vec3.y - 0.001), this.init_Vec3.z);
                     if (player.onGround() && this.init_Vec3.y - player.getY() > 0.6) {
                        TableIndex.UiHelper launch = TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(
                           collisionWorld, state, player.getYRot(), false, landing, this.init_Vec3.y
                        );
                        this.init_Minecraft.options.keyUp.setDown(launch.runTicks() == 0 && launch.margin() >= 0.08);
                        this.init_Minecraft.options.keyJump.setDown(launch.runTicks() == 0 && launch.margin() >= 0.08);
                     } else if (!player.onGround()) {
                        TableIndex.Inner_DataUtil air = TableIndex.init_TableIndex_DataUti_NodeWalker_CachePo(
                           collisionWorld, state, player.getYRot(), false, landing, this.init_Vec3.y
                        );
                        this.init_Minecraft.options.keyUp.setDown(air.forward() > 0.0);
                        this.init_Minecraft.options.keyLeft.setDown(air.strafe() > 0.0);
                        this.init_Minecraft.options.keyRight.setDown(air.strafe() < 0.0);
                     }
                  }

                  double forward = this.init_Minecraft.options.keyUp.isDown() ? 1.0 : 0.0;
                  double strafe = (this.init_Minecraft.options.keyLeft.isDown() ? 1 : 0) - (this.init_Minecraft.options.keyRight.isDown() ? 1 : 0);
                  if (!RefQueue.init_Z_D_2(forward, strafe, this.init_Minecraft.options.keyJump.isDown(), false)) {
                     RefQueue.load_V();
                     this.init_Vec3 = null;
                     this.init_ClassCache_BufferH.init(this.init_RefQueue.write_BlockPos());
                     return this.load("Partial-block exit became unsafe; replanning");
                  } else {
                     return this.load("Stepping clear of the partial block");
                  }
               } else {
                  this.init_Minecraft.options.keyUp.setDown(false);
                  this.init_Vec3 = null;
                  if (this.push_Z) {
                     this.init_Minecraft.options.keyShift.setDown(false);
                  }

                  this.push_Z = false;
                  if (this.close_Z) {
                     this.init_ClassCache_BufferH.init(this.init_RefQueue.write_BlockPos());
                  }

                  return this.load("Replanning from the clear stance");
               }
            }

            boolean read(BlockPos current) {
               LocalPlayer player = this.init_Minecraft.player;
               if (player.onGround() && !this.init_Set.contains(current.asLong())) {
                  Vec3 centre = new Vec3(current.getX() + 0.5, player.getY(), current.getZ() + 0.5);
                  Vec3 delta = centre.subtract(player.position());
                  if (!(delta.lengthSqr() < 0.0025) && RefQueue.init_Z_Vec3_Vec3(player.position(), centre)) {
                     for (int i = 1; i <= 8; i++) {
                        AABB box = player.getBoundingBox().move(delta.scale(i / 8.0)).deflate(0.001);
                        if (!this.init_Minecraft.level.noCollision(player, box) || this.init_Minecraft.level.noCollision(player, box.move(0.0, -0.08, 0.0))) {
                           return false;
                        }
                     }

                     this.init_Set.add(current.asLong());
                     this.init_Vec3 = centre;
                     this.open_Z = this.poll_Z = false;
                     this.push_Z = true;
                     this.sync_I = 0;
                     return true;
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            }

            boolean push() {
               if (HashUtil.read(this.init_ClassCache_JsonUti) instanceof u placement) {
                  BlockPos current = this.init_RefQueue.write_BlockPos();
                  if (!this.flush_Z_2() || !this.init_ClassCache_JsonUti.isInGoal(current.getX(), current.getY(), current.getZ())) {
                     return false;
                  }

                  if (this.init_Vec3 != null) {
                     return true;
                  }

                  if (this.read(current)) {
                     return true;
                  }

                  if (!HashUtil.init(this.init_RefQueue.init_RefQueue_TaskQueue(), this.init_Minecraft.player.getDeltaMovement())) {
                     RefQueue.open_V();
                     return true;
                  }

                  if (++this.close_I > 16) {
                     return false;
                  }

                  placement.update(current);
                  AutismClientAddon.LOG.info("[AI] stance {} cannot place at {}; picking another", current.toShortString(), target.toShortString());
                  this.init_ClassCache_BufferH.init(current);
                  return true;
               } else if (HashUtil.read(this.init_ClassCache_JsonUti) instanceof q interaction && this.flush_Z_2() && interaction.init_I() < 16) {
                  BlockPos current = this.init_RefQueue.write_BlockPos();
                  if (!this.init_ClassCache_JsonUti.isInGoal(current.getX(), current.getY(), current.getZ())) {
                     return false;
                  }

                  if (CachePool.load(this.init_Minecraft, interaction.load_BlockPos(), interaction.init_Direction()) != null) {
                     return false;
                  }

                  interaction.init_V(current);
                  AutismClientAddon.LOG
                     .info("[AI] stance {} has no clear line to {}; picking another", current.toShortString(), interaction.load_BlockPos().toShortString());
                  this.init_ClassCache_BufferH.init(current);
                  return true;
               } else {
                  return false;
               }
            }

            JsonObject init_JsonObject_27() {
               JsonObject out = ag.init_JsonObject_ObjArr(
                  "x",
                  target.getX(),
                  "y",
                  target.getY(),
                  "z",
                  target.getZ(),
                  "firstPathReadyMs",
                  this.read_D,
                  "pathTicks",
                  Math.round(this.init_RefQueue.init_D()),
                  "blocksDug",
                  this.init_RefQueue.read_I(),
                  "pillarsPlaced",
                  this.init_RefQueue.write_I(),
                  "bridgesPlaced",
                  this.init_RefQueue.update_I(),
                  "restocks",
                  this.init_RefQueue.sync_I(),
                  "distanceTravelled",
                  Math.round(this.load_D * 10.0) / 10.0,
                  "replans",
                  this.write_I,
                  "waterPlugs",
                  this.poll_I,
                  "protectedWaterPlugVetoes",
                  this.pop_I,
                  "blacklisted",
                  this.init_ClassCache_BufferH.init_I()
               );
               ClassCache.Inner_ByteReader searchStats = this.init_ClassCache_BufferH.init_ClassCache_ByteRea();
               if (HashUtil.read(this.init_ClassCache_JsonUti) instanceof HashUtil.Inner_StreamUtil fluid && fluid.load_BlockPos != null) {
                  out.addProperty("fluidSourceX", fluid.load_BlockPos.getX());
                  out.addProperty("fluidSourceY", fluid.load_BlockPos.getY());
                  out.addProperty("fluidSourceZ", fluid.load_BlockPos.getZ());
               }

               if (Boolean.getBoolean("autism.navigation.debug")) {
                  ClassCache.Inner_DataUtil liveCaps = this.init_ClassCache_DataUti();
                  BlockPos foot = this.init_RefQueue.write_BlockPos();
                  ClassCache.Inner_FileCache floor = this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(foot.getX(), foot.getY() - 1, foot.getZ());
                  out.add(
                     "construction",
                     ag.init_JsonObject_ObjArr(
                        "blocks",
                        liveCaps.constructionBlocks(),
                        "pillar",
                        liveCaps.allowPillar(),
                        "bridge",
                        liveCaps.allowBridge(),
                        "repairFarmland",
                        liveCaps.repairFarmland(),
                        "disabled",
                        liveCaps.disabledTypes(),
                        "floorTop16",
                        floor.top16(),
                        "floorFarmland",
                        floor.farmland()
                     )
                  );
                  if (floor.farmland()) {
                     JsonArray probes = new JsonArray();

                     for (int up = 0; up <= 3; up++) {
                        probes.add(this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(foot.getX(), foot.getY() + up, foot.getZ()).toString());
                     }

                     out.add("pillarColumn", probes);
                  }
               }

               if (searchStats != null) {
                  out.add(
                     "search",
                     ag.init_JsonObject_ObjArr(
                        "elapsedMs",
                        searchStats.elapsedNanos() / 1000000.0,
                        "rounds",
                        searchStats.rounds(),
                        "expanded",
                        searchStats.expanded(),
                        "generated",
                        searchStats.generated(),
                        "blockQueries",
                        searchStats.blockQueries(),
                        "peakOpen",
                        searchStats.peakOpen()
                     )
                  );
               }

               ClassCache.UiHelper stalled = this.init_RefQueue.init_ClassCache_UiHelpe();
               if (stalled != null) {
                  out.addProperty("stalledMove", stalled.type().name());
                  out.addProperty("stalledFrom", stalled.from().toShortString());
                  out.addProperty("stalledTo", stalled.to().toShortString());
               }

               return out;
            }

            AiOperation.Result close_AiOperation_Result() {
               if (!completeRoute) {
                  return this.load("Planning route");
               }

               ClassCache.Inner_EventBus progress = this.init_ClassCache_BufferH.init_ClassCache_EventBu();
               String activity = progress == null ? "Checking terrain" : String.format(Locale.ROOT, "%,d positions checked", progress.expanded());
               if (!AutismLiteVariant.enabled()) {
                  if (progress != null) {
                     cG.init_V_Obj(this.load_Obj, progress.trail(), 0, () -> null);
                  }

                  cG.init(this.load_Obj, "Planning route", activity, 0, 0);
               }

               return this.load("Planning route - " + activity);
            }

            AiOperation.Result load(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_27().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.cancel();
               return new AiOperation.Result(status, message, this.init_JsonObject_27().toString(), List.of());
            }

            AiOperation.Result init(A code, String message) {
               this.cancel();
               return AiOperation.Result.failed(code, message, this.init_JsonObject_27().toString());
            }

            @Override
            public void cancel() {
               if (!this.flush_Z) {
                  this.flush_Z = true;
                  if (!AutismLiteVariant.enabled() && this.init_ArrayUtil != null) {
                     this.init_ArrayUtil.init_V();
                  }

                  if (this.init_aP_StreamUtil != null) {
                     this.init_aP_StreamUtil.close();
                  }

                  this.init_ClassCache_BufferH.init_V();
                  this.init_RefQueue.write_V();
                  if (completeRoute && !AutismLiteVariant.enabled()) {
                     cG.init_V(this.load_Obj);
                  }

                  if (this.init_Minecraft.player != null && this.init_Minecraft.player.getInventory().getSelectedSlot() != this.init_I) {
                     this.init_Minecraft.player.getInventory().setSelectedSlot(this.init_I);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Engine travel requires a connected living player");
      }
   }

   public record Inner_BufferHelper(boolean sprint, boolean bunnyhop, int maxFall, boolean avoidWater, boolean allowClimb, boolean allowDoors) {
      public static final HashUtil.Inner_BufferHelper DEFAULT = new HashUtil.Inner_BufferHelper(true, true, 3, false, true, true);
   }

   public interface Inner_CachePool extends AiOperation {
      void init_V_ClassCache_JsonUti(ClassCache.Inner_JsonUtil var1);

      default void init_V_15() {
      }

      boolean init_Z_13();

      void load(boolean var1);

      default void init_V_Z(boolean hold) {
      }

      default void init(boolean hold, Predicate<BlockPos> keeps) {
         this.init_V_Z(hold);
      }

      default BlockPos load_BlockPos_3() {
         return null;
      }

      default void init_V_BlockPos(BlockPos cell) {
      }

      default int init_I_14() {
         return -1;
      }

      default int load_I_8() {
         return -1;
      }

      default boolean load_Z_8() {
         return true;
      }

      @Override
      default boolean canInterruptNow() {
         return this.load_Z_8();
      }

      default BlockPos init_BlockPos_6() {
         return null;
      }

      default Set<BlockPos> init_Set_4() {
         return Set.of();
      }

      default Set<BlockPos> load_Set_2() {
         return Set.of();
      }

      default void init_V_Function(Function<BlockPos, Vec3> aim) {
      }

      default void init_V_I_2(int blocks) {
      }

      default void init_V_BiPredicate(BiPredicate<BlockState, ItemStack> accepted) {
      }
   }

   private interface Inner_DataUtil extends ClassCache.Inner_JsonUtil {
      ClassCache.Inner_JsonUtil init_ClassCache_JsonUti();
   }

   static final class Inner_StreamUtil implements ClassCache.Inner_JsonUtil {
      final BlockPos init_BlockPos;
      final List<BlockPos> init_List;
      final double init_D;
      final double load_D;
      final ClassCache.Inner_StateCache init_ClassCache_StateCa;
      final Set<Long> init_Set;
      final boolean init_Z;
      BlockPos load_BlockPos;

      Inner_StreamUtil(BlockPos source, double reach, double eye, ClassCache.Inner_StateCache grid, Set<Long> rejected) {
         this(source, reach, eye, grid, rejected, false);
      }

      Inner_StreamUtil(BlockPos source, double reach, double eye, ClassCache.Inner_StateCache grid, Set<Long> rejected, boolean water) {
         this(List.of(source), reach, eye, grid, rejected, water);
      }

      Inner_StreamUtil(List<BlockPos> sources, double reach, double eye, ClassCache.Inner_StateCache grid, Set<Long> rejected, boolean water) {
         this.init_List = List.copyOf(sources);
         this.init_BlockPos = this.init_List.getFirst();
         this.init_D = reach;
         this.load_D = eye;
         this.init_ClassCache_StateCa = grid;
         this.init_Set = rejected;
         this.init_Z = water;
      }

      @Override
      public ClassCache.Inner_JsonUtil init(ClassCache.Inner_StateCache planned) {
         return new HashUtil.Inner_StreamUtil(this.init_List, this.init_D, this.load_D, planned, this.init_Set, this.init_Z);
      }

      @Override
      public boolean isInGoal(int x, int y, int z) {
         return this.init_Z_I(x, y, z, this.init_ClassCache_StateCa);
      }

      @Override
      public boolean init_Z_I(int x, int y, int z, ClassCache.Inner_StateCache planned) {
         if (planned != null && !this.init_Set.contains(BlockPos.asLong(x, y, z))) {
            ClassCache.Inner_FileCache feet = planned.init_ClassCache_FileCac_I(x, y, z);
            if (feet.loaded() && (this.init_Z || !feet.water()) && !feet.lava()) {
               for (BlockPos candidate : this.init_List) {
                  double dx = candidate.getX() - x;
                  double dy = candidate.getY() + 0.8 - y - this.load_D;
                  double dz = candidate.getZ() - z;
                  if (dx * dx + dy * dy + dz * dz <= this.init_D * this.init_D
                     && q.load(planned, candidate, x, y, z, this.load_D, candidate.getX() + 0.5, candidate.getY() + 0.8, candidate.getZ() + 0.5, this.init_D)) {
                     return true;
                  }
               }

               return false;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      @Override
      public double heuristic(int x, int y, int z) {
         double best = 1.0 / 0.0;

         for (BlockPos candidate : this.init_List) {
            best = Math.min(best, ClassCache.Inner_CachePool.load(x - candidate.getX(), candidate.getY() - y, z - candidate.getZ()));
         }

         return Math.max(0.0, best - this.init_D * ClassCache.Inner_CachePool.f_a_D);
      }

      @Override
      public boolean init_Z(BlockPos cell) {
         return this.init_Z || !cell.equals(this.init_BlockPos);
      }

      @Override
      public boolean read(BlockPos cell) {
         return !this.init_List.contains(cell);
      }

      boolean init(Minecraft mc) {
         if (mc.player != null
            && mc.level != null
            && !mc.player.isInLava()
            && (this.init_Z ? mc.player.onGround() || mc.player.isInWater() : mc.player.onGround() && !mc.player.isInWater())) {
            for (BlockPos source : this.init_List) {
               if (mc.level.hasChunkAt(source) && mc.level.getFluidState(source).isSource()) {
                  Vec3 point = new Vec3(source.getX() + 0.5, source.getY() + 0.8, source.getZ() + 0.5);
                  if (!(mc.player.getEyePosition().distanceToSqr(point) > this.init_D * this.init_D)) {
                     BlockHitResult hit = mc.level
                        .clip(
                           new ClipContext(mc.player.getEyePosition(), point, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.SOURCE_ONLY, mc.player)
                        );
                     if (hit.getType() == Type.BLOCK) {
                        FluidState fluid = mc.level.getFluidState(hit.getBlockPos());
                        if (fluid.isSource() && fluid.is(this.init_Z ? FluidTags.WATER : FluidTags.LAVA)) {
                           this.load_BlockPos = hit.getBlockPos().immutable();
                           return true;
                        }
                     }
                  }
               }
            }

            return false;
         } else {
            return false;
         }
      }

      boolean update(BlockPos pos) {
         return this.init_Set.size() < 16 && this.init_Set.add(pos.asLong());
      }
   }
}
