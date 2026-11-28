package autismclient.ai.engine;

import autismclient.AutismClientAddon;
import autismclient.ai.AiItems;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.CaveVinesPlantBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndGatewayBlock;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.PotentSulfurBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SulfurSpikeBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TwistingVinesBlock;
import net.minecraft.world.level.block.TwistingVinesPlantBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.WeepingVinesBlock;
import net.minecraft.world.level.block.WeepingVinesPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AiPathfinder {
   private static final ConcurrentHashMap<AiPathfinder.MoveKey, Long> GLOBAL_MOVE_BLACKLIST = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<AiPathfinder.MoveKey, long[]> GLOBAL_MOVE_FAILURE_COUNTS = new ConcurrentHashMap<>();

   private AiPathfinder() {
   }

   private static int headingBucket(AiPathfinder.PathStep var0) {
      if (var0 == null) {
         return 4;
      }

      int var1 = Integer.compare(var0.to().getX(), var0.from().getX());
      int var2 = Integer.compare(var0.to().getZ(), var0.from().getZ());
      return 3 * (var1 + 1) + var2 + 1;
   }

   private static double transitionPenalty(AiPathfinder.PathStep var0, AiPathfinder.PathStep var1) {
      if (var0 == null) {
         return 0.0;
      }

      double var2 = var0.to().getX() - var0.from().getX();
      double var4 = var0.to().getZ() - var0.from().getZ();
      double var6 = var1.to().getX() - var1.from().getX();
      double var8 = var1.to().getZ() - var1.from().getZ();
      double var10 = Math.hypot(var2, var4) * Math.hypot(var6, var8);
      if (var10 == 0.0) {
         return var1.jump() == null ? 0.0 : 2.0;
      }

      double var12 = 1.0 - Math.clamp((var2 * var6 + var4 * var8) / var10, -1.0, 1.0);
      return var12 * (var1.jump() != null ? 6 : (var0.jump() != null ? 4 : 2));
   }

   private static double scorePathDuration(AiPathfinder.PathResult var0) {
      double var1 = 0.0;
      AiPathfinder.PathStep var3 = null;

      for (AiPathfinder.PathStep var5 : var0.movements()) {
         var1 += effectiveMoveTicks(var5) + transitionPenalty(var3, var5);
         var3 = var5;
      }

      return var1;
   }

   private static double effectiveMoveTicks(AiPathfinder.PathStep var0) {
      AiJumpSolver.JumpPlan var1 = var0.jump();
      return var0.type() == AiPathfinder.MoveType.JUMP && var1 != null && var1.maneuver() != null
         ? Math.max(var1.ticks() + 4, var0.costTicks() - 12.0)
         : var0.costTicks();
   }

   private static AiPathfinder.PathResult optimizeWithShortcutJumps(
      AiPathfinder.PathResult var0,
      AiPathfinder.GridView var1,
      AiPathfinder.PathOptions var2,
      double var3,
      Set<Long> var5,
      Set<AiPathfinder.MoveKey> var6
   ) {
      var var7 = var0.movements();
      if (var7.size() < 2) {
         return var0;
      }

      ArrayList<List<AiPathfinder.TrailNode>> var8 = new ArrayList<>(Collections.nCopies(var7.size(), List.of()));
      AiPathfinder.MoveGenerator.CachedGrid var9 = new AiPathfinder.MoveGenerator.CachedGrid(var1, 50000, var3);
      var9.ignoreCachedCells = var9.fullPhysicsParkour = true;
      var9.fastPassMode = true;
      var9.fallPolicy = var2.fallPolicy();

      for (int var10 = var7.size() - 1; var10 >= 0; var10--) {
         if (Thread.currentThread().isInterrupted()) {
            return var0;
         }

         AiPathfinder.PathStep var11 = (AiPathfinder.PathStep)var7.get(var10);
         ArrayList var12 = new ArrayList();
         var12.add(chainTrailNode(var11, var10 + 1, var8, 0.0));
         BlockPos var13 = var11.from();
         AiPathfinder.CellInfo var14 = var9.probePos(var13.below());
         double var15 = AiPathfinder.MoveGenerator.surfaceY(var13.getY(), var14);
         if (!var14.farmland()
            && !var14.slime()
            && AiPathfinder.MoveGenerator.isStandable(
               var9.probePos(var13), var9.probePos(var13.above()), var14
            )
            && AiJumpSolver.canStandAt(var9.plannerWorld(), var13.getX() + 0.5, var15, var13.getZ() + 0.5)) {
            var9.incomingMove = var10 == 0 ? null : (AiPathfinder.PathStep)var7.get(var10 - 1);

            for (int var17 = var10 + 1; var17 < Math.min(var10 + 16, var7.size()); var17++) {
               BlockPos var18 = ((AiPathfinder.PathStep)var7.get(var17)).to();
               double var19 = Math.hypot(var18.getX() - var13.getX(), var18.getZ() - var13.getZ());
               AiPathfinder.CellInfo var21 = var9.probePos(var18.below());
               double var22 = AiPathfinder.MoveGenerator.surfaceY(var18.getY(), var21);
               double var24 = var15 - var22;
               if (!(var19 < 1.6)
                  && !(var19 > (var24 > 14.0 ? 10.5 : 8.5))
                  && !(-var24 > 1.25)
                  && !(var24 > var2.fallPolicy().maxDropBlocks)
                  && !var5.contains(var18.asLong())
                  && !var21.slime()
                  && AiPathfinder.MoveGenerator.isStandable(
                     var9.probePos(var18), var9.probePos(var18.above()), var21
                  )) {
                  AiJumpSolver.JumpPlan var26 = var9.solveJumpCached(var13, var15, var18, var22, 0.0, var2.canSprint());
                  if (var26 != null && !(var26.margin() < 0.08) && !AiPathfinder.MoveGenerator.hasLavaUnderJump(var9, var13, var18)) {
                     AiPathfinder.PathStep var27 = AiPathfinder.PathStep.jump(
                        var13, var18, AiPathfinder.MoveCosts.jumpPlanCost(var26), var26
                     );
                     if (!var6.contains(AiPathfinder.MoveKey.of(var27))) {
                        var12.add(chainTrailNode(var27, var17 + 1, var8, 1.5));
                     }
                  }
               }
            }
         }

         var8.set(var10, var12);
      }

      AiPathfinder.TrailNode var28 = ((TrailNode)(((List)var8.getFirst()).stream().min(Comparator.comparingDouble(AiPathfinder.TrailNode::ticks)).orElseThrow()));
      ArrayList var29 = new ArrayList();

      for (AiPathfinder.TrailNode var30 = var28; var30 != null; var30 = var30.next()) {
         var29.add(var30.first());
      }

      return new AiPathfinder.PathResult(var29, true, var0.expanded(), false, false);
   }

   private static AiPathfinder.TrailNode chainTrailNode(AiPathfinder.PathStep var0, int var1, List<List<AiPathfinder.TrailNode>> var2, double var3) {
      AiPathfinder.TrailNode var5 = null;
      double var6 = 1.0 / 0.0;
      if (var1 < var2.size()) {
         for (AiPathfinder.TrailNode var9 : (Iterable<AiPathfinder.TrailNode>)(Iterable<?>)var2.get(var1)) {
            double var10 = var9.ticks() + transitionPenalty(var0, var9.first());
            if (var10 < var6) {
               var6 = var10;
               var5 = var9;
            }
         }
      }

      return new AiPathfinder.TrailNode(var0, var5, effectiveMoveTicks(var0) + var3 + (var5 == null ? 0.0 : var6));
   }

   private static AiPathfinder.PathResult optimizeWithPaceJumps(
      AiPathfinder.PathResult var0, AiPathfinder.GridView var1, AiPathfinder.PathOptions var2, double var3
   ) {
      var var5 = new ArrayList<>(var0.movements());
      AiPathfinder.MoveGenerator.CachedGrid var6 = new AiPathfinder.MoveGenerator.CachedGrid(var1, 50000, var3);
      var6.ignoreCachedCells = true;
      var6.fallPolicy = var2.fallPolicy();
      AiPlannerWorld var7 = var6.plannerWorld();

      for (int var8 = 0; var8 + 1 < var5.size() && !Thread.currentThread().isInterrupted(); var8++) {
         AiPathfinder.PathStep var9 = (AiPathfinder.PathStep)var5.get(var8);
         ArrayList<AiJumpSolver.SurfaceCell> var10 = new ArrayList<>(4);
         int var11 = 0;

         for (int var12 = var8; var12 < Math.min(var8 + 4, var5.size()); var12++) {
            AiPathfinder.PathStep var13 = (AiPathfinder.PathStep)var5.get(var12);
            if (var13.type() != AiPathfinder.MoveType.JUMP
               || var13.jump() == null
               || !var13.jump().sprint()
               || var6.probePos(var13.from().below()).ice()
               || var6.probePos(var13.to().below()).ice()) {
               break;
            }

            AiPathfinder.CellInfo var14 = var6.probePos(var13.from().below());
            double var15 = var13.jump().landY() - AiPathfinder.MoveGenerator.surfaceY(var13.from().getY(), var14);
            if (var15 > 1.29 || var15 < -2.25) {
               break;
            }

            var10.add(new AiJumpSolver.SurfaceCell(var13.to(), var13.jump().landY()));
            var11 += var13.jump().ticks();
         }

         if (var10.size() >= 2) {
            AiJumpSolver.JumpPlan var20 = var9.jump();
            double var21 = AiPathfinder.MoveGenerator.surfaceY(var9.from().getY(), var6.probePos(var9.from().below()));
            double var22 = Math.toRadians(var20.yaw());
            AiPhysics.MotionState var17 = var20.maneuver() == null
               ? new AiPhysics.MotionState(
                  var20.startX(),
                  var21,
                  var20.startZ(),
                  -Math.sin(var22) * var20.approach(),
                  -0.0784,
                  Math.cos(var22) * var20.approach(),
                  true,
                  0,
                  false,
                  false
               )
               : var20.maneuver().start();
            var var18 = AiJumpSolver.solveJumpChain(var7, var17, var20.yaw(), var10);
            if (var18.size() == var10.size() && var18.stream().mapToInt(AiJumpSolver.JumpPlan::ticks).sum() < var11 - 1) {
               for (int var19 = 0; var19 < var18.size(); var19++) {
                  var5.set(var8 + var19, ((AiPathfinder.PathStep)var5.get(var8 + var19)).withPaceJump((AiJumpSolver.JumpPlan)var18.get(var19)));
               }

               var8 += var18.size() - 1;
            }
         }
      }

      return new AiPathfinder.PathResult(var5, true, var0.expanded(), false, false);
   }

   public static List<AiPathfinder.PathStep> concatSegments(List<AiPathfinder.PathStep> var0, List<AiPathfinder.PathStep> var1) {
      if (!var0.isEmpty() && !var1.isEmpty()) {
         if (!((AiPathfinder.PathStep)var1.get(0)).from().equals(((AiPathfinder.PathStep)var0.get(var0.size() - 1)).to())) {
            return null;
         }

         ArrayList<AiPathfinder.PathStep> var2 = new ArrayList<>(var0.size() + var1.size());
         var2.addAll(var0);
         var2.addAll(var1);
         return var2;
      } else {
         return null;
      }
   }

   static List<AiPathfinder.PathStep> spliceDetour(List<AiPathfinder.PathStep> var0, List<AiPathfinder.PathStep> var1) {
      if (!var0.isEmpty() && !var1.isEmpty()) {
         HashMap var2 = new HashMap();

         for (int var3 = 0; var3 < var1.size(); var3++) {
            var2.putIfAbsent(((AiPathfinder.PathStep)var1.get(var3)).from(), var3);
         }

         for (int var6 = 0; var6 < var0.size(); var6++) {
            Integer var4 = (Integer)var2.get(((AiPathfinder.PathStep)var0.get(var6)).to());
            if (var4 != null) {
               ArrayList var5 = new ArrayList(var6 + 1 + var1.size() - var4);
               var5.addAll(var0.subList(0, var6 + 1));
               var5.addAll(var1.subList(var4, var1.size()));
               return List.copyOf(var5);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static AiPathfinder.ReplanAction decideReplanAction(int var0, int var1) {
      if (var1 >= 6) {
         return AiPathfinder.ReplanAction.FAILED;
      } else {
         return var0 >= 3 ? AiPathfinder.ReplanAction.BLACKLIST_AND_REPLAN : AiPathfinder.ReplanAction.REPLAN;
      }
   }

   public static void blacklistJumpTemporarily(AiPathfinder.PathStep var0, long var1) {
      if (var0 != null && var0.type() == AiPathfinder.MoveType.JUMP) {
         blacklistMoveTemporarily(var0, var1);
      }
   }

   public static void blacklistMoveTemporarily(AiPathfinder.PathStep var0, long var1) {
      if (var0 != null) {
         long var3 = System.nanoTime();
         GLOBAL_MOVE_BLACKLIST.values().removeIf(var2 -> var2 - var3 < 0L);
         GLOBAL_MOVE_BLACKLIST.put(new AiPathfinder.MoveKey(var0.type(), var0.from().asLong(), var0.to().asLong()), var3 + var1);
      }
   }

   public static void recordMoveFailure(AiPathfinder.PathStep var0) {
      if (var0 != null) {
         long var1 = System.nanoTime();
         GLOBAL_MOVE_FAILURE_COUNTS.values().removeIf(var2 -> var1 - var2[1] > 120000000000L);
         AiPathfinder.MoveKey var3 = new AiPathfinder.MoveKey(var0.type(), var0.from().asLong(), var0.to().asLong());
         long[] var4 = GLOBAL_MOVE_FAILURE_COUNTS.computeIfAbsent(var3, var2 -> new long[]{0L, var1});
         var4[0]++;
         var4[1] = var1;
         if (var4[0] >= 2L) {
            blacklistMoveTemporarily(var0, 120000000000L);
         }
      }
   }

   static boolean isGloballyBlacklisted(AiPathfinder.PathStep var0) {
      if (GLOBAL_MOVE_BLACKLIST.isEmpty()) {
         return false;
      }

      Long var1 = GLOBAL_MOVE_BLACKLIST.get(new AiPathfinder.MoveKey(var0.type(), var0.from().asLong(), var0.to().asLong()));
      return var1 != null && var1 - System.nanoTime() > 0L;
   }

   public static Set<AiPathfinder.MoveType> relatedMoveTypes(AiPathfinder.MoveType var0) {
      Set var10000;
      switch (var0) {
         case WALK:
         case SPRINT:
            var10000 = Set.of(AiPathfinder.MoveType.SPRINT_JUMP, AiPathfinder.MoveType.SPRINT);
            break;
         case WALK_DIAGONAL:
         case SPRINT_DIAGONAL:
            var10000 = Set.of(AiPathfinder.MoveType.SPRINT_DIAGONAL);
            break;
         case STEP_UP_1:
         case DESCEND_1:
         case DESCEND_2:
         case DESCEND_3:
         case DESCEND_SOFT:
         case DIG_DOWN_1:
         case DIG_TUNNEL:
         case PILLAR:
         case BRIDGE_CELL:
         case CLIMB_1:
         case DOOR:
         case JUMP_GAP_2:
         case JUMP_GAP_3:
         case PARKOUR_ASCEND:
         case SPRINT_JUMP:
         case SLIME_BOUNCE:
         case LADDER_JUMP:
         default:
            var10000 = Set.of(var0);
            break;
         case JUMP_UP_1:
            var10000 = Set.of(AiPathfinder.MoveType.JUMP_UP_1, AiPathfinder.MoveType.DIG_ASCEND);
            break;
         case JUMP:
            var10000 = Set.of();
            break;
         case DIG_ASCEND:
            var10000 = Set.of(AiPathfinder.MoveType.DIG_ASCEND, AiPathfinder.MoveType.JUMP_UP_1);
      }

      return var10000;
   }

   static AiPathfinder.GridView withSimulatedEdits(
      final AiPathfinder.GridView var0, List<AiPathfinder.PathStep> var1
   ) {
      HashMap var2 = new HashMap();

      for (AiPathfinder.PathStep var4 : var1) {
         for (BlockPos var6 : var4.cellsToBreak()) {
            var2.put(var6.asLong(), AiPathfinder.CellInfo.air());
         }

         for (BlockPos var9 : var4.cellsToPlace()) {
            var2.put(var9.asLong(), AiPathfinder.CellInfo.fullBlock(1.0 / 0.0));
         }
      }

      if (var2.isEmpty()) {
         return var0;
      }

      final var var7 = Map.copyOf(var2);
      return new AiPathfinder.GridView() {
         @Override
         public AiPathfinder.CellInfo probe(int var1, int var2x, int var3) {
            AiPathfinder.CellInfo var4 = (AiPathfinder.CellInfo)var7.get(BlockPos.asLong(var1, var2x, var3));
            return var4 == null ? var0.probe(var1, var2x, var3) : var4;
         }

         @Override
         public short putAt_S_I(int var1, int var2x, int var3) {
            AiPathfinder.CellInfo var4 = (AiPathfinder.CellInfo)var7.get(BlockPos.asLong(var1, var2x, var3));
            return var4 == null ? var0.putAt_S_I(var1, var2x, var3) : AiShapes.shapeIdOf(var4);
         }

         @Override
         public short fill_S(int var1, int var2x, int var3) {
            AiPathfinder.CellInfo var4 = (AiPathfinder.CellInfo)var7.get(BlockPos.asLong(var1, var2x, var3));
            return var4 == null ? var0.fill_S(var1, var2x, var3) : AiShapes.shapeIdOf(var4);
         }

         @Override
         public byte putAt_B_I(int var1, int var2x, int var3) {
            return var7.containsKey(BlockPos.asLong(var1, var2x, var3)) ? AiSurface.NORMAL.id() : var0.putAt_B_I(var1, var2x, var3);
         }

         @Override
         public long revision() {
            return var0.revision();
         }
      };
   }

   public record CellInfo(
      boolean loaded,
      boolean passable,
      int top16,
      boolean water,
      boolean lava,
      boolean climbable,
      boolean hazardFeet,
      boolean hazardFloor,
      boolean falling,
      int fallMult10,
      double breakTicks,
      boolean door,
      boolean slabStair,
      boolean farmland,
      boolean ice,
      boolean cached,
      int destructionPenalty,
      boolean soulSand,
      boolean thorny,
      boolean portal,
      boolean narrowFooting,
      boolean feetTrigger,
      boolean pressurePlate
   ) {
      public static final double NOT_DIGGABLE = 1.0 / 0.0;
      public static final AiPathfinder.CellInfo UNLOADED = new AiPathfinder.CellInfo(
         false, false, 0, false, false, false, false, false, false, 10, 1.0 / 0.0, false, false, false, false, false
      );
      private static final AiPathfinder.CellInfo AIR = new AiPathfinder.CellInfo(
         true, true, 0, false, false, false, false, false, false, 10, 1.0 / 0.0, false, false, false, false, false
      );
      private static final AiPathfinder.CellInfo WATER = new AiPathfinder.CellInfo(
         true, true, 0, true, false, false, false, false, false, 0, 1.0 / 0.0, false, false, false, false, false
      );
      private static final AiPathfinder.CellInfo LAVA = new AiPathfinder.CellInfo(
         true, true, 0, false, true, false, false, false, false, 10, 1.0 / 0.0, false, false, false, false, false
      );
      public static final AiPathfinder.CellInfo CACHED_AIR = air().withCached();
      public static final AiPathfinder.CellInfo CACHED_SOLID = fullBlock(1.0 / 0.0).withCached();
      public static final AiPathfinder.CellInfo CACHED_WATER = waterCell().withCached();
      public static final AiPathfinder.CellInfo CACHED_HAZARD = lavaCell().withCached();

      public CellInfo(
         boolean var1,
         boolean var2,
         int var3,
         boolean var4,
         boolean var5,
         boolean var6,
         boolean var7,
         boolean var8,
         boolean var9,
         int var10,
         double var11,
         boolean var13,
         boolean var14,
         boolean var15,
         boolean var16,
         boolean var17,
         int var18,
         boolean var19,
         boolean var20,
         boolean var21,
         boolean var22,
         boolean var23
      ) {
         this(
            var1,
            var2,
            var3,
            var4,
            var5,
            var6,
            var7,
            var8,
            var9,
            var10,
            var11,
            var13,
            var14,
            var15,
            var16,
            var17,
            var18,
            var19,
            var20,
            var21,
            var22,
            var23,
            false
         );
      }

      public CellInfo(
         boolean var1,
         boolean var2,
         int var3,
         boolean var4,
         boolean var5,
         boolean var6,
         boolean var7,
         boolean var8,
         boolean var9,
         int var10,
         double var11,
         boolean var13,
         boolean var14,
         boolean var15,
         boolean var16,
         boolean var17,
         int var18,
         boolean var19,
         boolean var20,
         boolean var21,
         boolean var22
      ) {
         this(
            var1,
            var2,
            var3,
            var4,
            var5,
            var6,
            var7,
            var8,
            var9,
            var10,
            var11,
            var13,
            var14,
            var15,
            var16,
            var17,
            var18,
            var19,
            var20,
            var21,
            var22,
            false,
            false
         );
      }

      public CellInfo(
         boolean var1,
         boolean var2,
         int var3,
         boolean var4,
         boolean var5,
         boolean var6,
         boolean var7,
         boolean var8,
         boolean var9,
         int var10,
         double var11,
         boolean var13,
         boolean var14,
         boolean var15,
         boolean var16,
         boolean var17,
         int var18,
         boolean var19,
         boolean var20,
         boolean var21
      ) {
         this(
            var1,
            var2,
            var3,
            var4,
            var5,
            var6,
            var7,
            var8,
            var9,
            var10,
            var11,
            var13,
            var14,
            var15,
            var16,
            var17,
            var18,
            var19,
            var20,
            var21,
            false,
            false,
            false
         );
      }

      public CellInfo(
         boolean var1,
         boolean var2,
         int var3,
         boolean var4,
         boolean var5,
         boolean var6,
         boolean var7,
         boolean var8,
         boolean var9,
         int var10,
         double var11,
         boolean var13,
         boolean var14,
         boolean var15,
         boolean var16,
         boolean var17,
         int var18,
         boolean var19,
         boolean var20
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var13, var14, var15, var16, var17, var18, var19, var20, false);
      }

      public CellInfo(
         boolean var1,
         boolean var2,
         int var3,
         boolean var4,
         boolean var5,
         boolean var6,
         boolean var7,
         boolean var8,
         boolean var9,
         int var10,
         double var11,
         boolean var13,
         boolean var14,
         boolean var15,
         boolean var16,
         boolean var17,
         int var18,
         boolean var19
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var13, var14, var15, var16, var17, var18, var19, false);
      }

      public CellInfo(
         boolean var1,
         boolean var2,
         int var3,
         boolean var4,
         boolean var5,
         boolean var6,
         boolean var7,
         boolean var8,
         boolean var9,
         int var10,
         double var11,
         boolean var13,
         boolean var14,
         boolean var15,
         boolean var16,
         boolean var17
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var13, var14, var15, var16, var17, 0, false);
      }

      boolean floorContactHazard() {
         return this.hazardFloor && !this.narrowFooting;
      }

      public AiPathfinder.CellInfo withDestructionPenalty(int var1) {
         if (var1 < 0) {
            throw new IllegalArgumentException("Negative destruction penalty");
         } else {
            return new AiPathfinder.CellInfo(
               this.loaded,
               this.passable,
               this.top16,
               this.water,
               this.lava,
               this.climbable,
               this.hazardFeet,
               this.hazardFloor,
               this.falling,
               this.fallMult10,
               this.breakTicks,
               this.door,
               this.slabStair,
               this.farmland,
               this.ice,
               this.cached,
               var1,
               this.soulSand,
               this.thorny,
               this.portal,
               this.narrowFooting,
               this.feetTrigger,
               this.pressurePlate
            );
         }
      }

      public static AiPathfinder.CellInfo air() {
         return AIR;
      }

      public static AiPathfinder.CellInfo waterCell() {
         return WATER;
      }

      public static AiPathfinder.CellInfo lavaCell() {
         return LAVA;
      }

      public static AiPathfinder.CellInfo climbableCell() {
         return new AiPathfinder.CellInfo(true, true, 0, false, false, true, false, false, false, 10, 1.0 / 0.0, false, false, false, false, false);
      }

      public static AiPathfinder.CellInfo fullBlock(double var0) {
         return new AiPathfinder.CellInfo(true, false, 16, false, false, false, false, false, false, 10, var0, false, false, false, false, false);
      }

      public static AiPathfinder.CellInfo doorCell(double var0) {
         return new AiPathfinder.CellInfo(true, false, 16, false, false, false, false, false, false, 10, var0, true, false, false, false, false);
      }

      public AiPathfinder.CellInfo withTop16(int var1) {
         return new AiPathfinder.CellInfo(
            this.loaded,
            this.passable,
            var1,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public AiPathfinder.CellInfo withFallMult10(int var1) {
         return new AiPathfinder.CellInfo(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            var1,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public AiPathfinder.CellInfo withHazards(boolean var1, boolean var2) {
         return new AiPathfinder.CellInfo(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            var1,
            var2,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            var2 && this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public AiPathfinder.CellInfo withFalling() {
         return new AiPathfinder.CellInfo(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            true,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public AiPathfinder.CellInfo withSlabStair() {
         return new AiPathfinder.CellInfo(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            true,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public AiPathfinder.CellInfo withFarmland() {
         return new AiPathfinder.CellInfo(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            true,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public AiPathfinder.CellInfo withIce() {
         return new AiPathfinder.CellInfo(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            true,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public AiPathfinder.CellInfo withSoulSand() {
         return new AiPathfinder.CellInfo(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            true,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public AiPathfinder.CellInfo withThorny() {
         return new AiPathfinder.CellInfo(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            true,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public AiPathfinder.CellInfo withPortal() {
         return new AiPathfinder.CellInfo(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            true,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public AiPathfinder.CellInfo withCached() {
         return new AiPathfinder.CellInfo(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            true,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      boolean diggable() {
         return !this.passable && this.breakTicks < 1.0 / 0.0 && !this.lava && !this.water;
      }

      public boolean web() {
         return this.loaded && this.passable && this.fallMult10 == 0 && !this.water && !this.lava && !this.climbable && !this.hazardFeet;
      }

      public boolean honey() {
         return this.loaded && !this.passable && this.top16 == 15 && this.fallMult10 == 2;
      }

      public boolean slime() {
         return this.loaded && !this.passable && this.top16 == 16 && this.fallMult10 == 0;
      }

      public double crawl() {
         return !this.soulSand && !this.honey() ? (this.slime() ? 3.6 : 1.0) : 2.5;
      }

      public boolean contactHazard() {
         return this.loaded && (this.hazardFeet || this.lava || this.thorny || this.portal || !this.passable && this.hazardFloor && this.top16 == 15);
      }
   }

   record MoveKey(AiPathfinder.MoveType type, long from, long to) {
      static AiPathfinder.MoveKey of(AiPathfinder.PathStep var0) {
         return new AiPathfinder.MoveKey(var0.type(), var0.from().asLong(), var0.to().asLong());
      }
   }

   public record PathOptions(
      boolean allowDig,
      boolean allowPillar,
      boolean allowBridge,
      boolean canSprint,
      boolean hasBlocks,
      boolean allowParkour,
      AiPathfinder.RouteStrategy strategy,
      AiPathfinder.ThreatField threats,
      boolean jumpBoost,
      boolean slowFalling,
      boolean levitation,
      double breakReluctance,
      Set<AiPathfinder.MoveType> disabledTypes,
      int constructionBlocks,
      int maxFall,
      boolean avoidWater,
      boolean repairFarmland,
      boolean supportedBridges,
      AiPathfinder.FallPolicy fallPolicy
   ) {
      public static final AiPathfinder.PathOptions WALK_ONLY = new AiPathfinder.PathOptions(false, false, false, true, false);
      public static final AiPathfinder.PathOptions FULL = new AiPathfinder.PathOptions(true, true, true, true, true);

      public PathOptions(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         AiPathfinder.RouteStrategy var7,
         AiPathfinder.ThreatField var8,
         boolean var9,
         boolean var10,
         boolean var11,
         double var12,
         Set<AiPathfinder.MoveType> var14,
         int var15,
         int var16,
         boolean var17,
         boolean var18,
         boolean var19
      ) {
         this(
            var1,
            var2,
            var3,
            var4,
            var5,
            var6,
            var7,
            var8,
            var9,
            var10,
            var11,
            var12,
            var14,
            var15,
            var16,
            var17,
            var18,
            var19,
            AiPathfinder.FallPolicy.STANDARD
         );
      }

      public PathOptions(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         AiPathfinder.RouteStrategy var7,
         AiPathfinder.ThreatField var8,
         boolean var9,
         boolean var10,
         boolean var11,
         double var12,
         Set<AiPathfinder.MoveType> var14,
         int var15,
         int var16,
         boolean var17,
         boolean var18
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var14, var15, var16, var17, var18, false);
      }

      public PathOptions(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         AiPathfinder.RouteStrategy var7,
         AiPathfinder.ThreatField var8,
         boolean var9,
         boolean var10,
         boolean var11,
         double var12,
         Set<AiPathfinder.MoveType> var14,
         int var15,
         int var16,
         boolean var17
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var14, var15, var16, var17, false);
      }

      public PathOptions(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         AiPathfinder.RouteStrategy var7,
         AiPathfinder.ThreatField var8,
         boolean var9,
         boolean var10,
         boolean var11,
         double var12,
         Set<AiPathfinder.MoveType> var14,
         int var15
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var14, var15, 3, false);
      }

      public PathOptions(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         AiPathfinder.RouteStrategy var7,
         AiPathfinder.ThreatField var8,
         boolean var9,
         boolean var10,
         boolean var11,
         double var12,
         Set<AiPathfinder.MoveType> var14
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var14, var5 ? 2147483647 : 0);
      }

      public PathOptions(
         boolean allowDig,
         boolean allowPillar,
         boolean allowBridge,
         boolean canSprint,
         boolean hasBlocks,
         boolean allowParkour,
         AiPathfinder.RouteStrategy strategy,
         AiPathfinder.ThreatField threats,
         boolean jumpBoost,
         boolean slowFalling,
         boolean levitation,
         double breakReluctance,
         Set<AiPathfinder.MoveType> disabledTypes,
         int constructionBlocks,
         int maxFall,
         boolean avoidWater,
         boolean repairFarmland,
         boolean supportedBridges,
         AiPathfinder.FallPolicy fallPolicy
      ) {
         if (constructionBlocks < 0) {
            throw new IllegalArgumentException("Negative construction inventory");
         }

         disabledTypes = Set.copyOf(disabledTypes);
         this.allowDig = allowDig;
         this.allowPillar = allowPillar;
         this.allowBridge = allowBridge;
         this.canSprint = canSprint;
         this.hasBlocks = hasBlocks;
         this.allowParkour = allowParkour;
         this.strategy = strategy;
         this.threats = threats;
         this.jumpBoost = jumpBoost;
         this.slowFalling = slowFalling;
         this.levitation = levitation;
         this.breakReluctance = breakReluctance;
         this.disabledTypes = disabledTypes;
         this.constructionBlocks = constructionBlocks;
         this.maxFall = maxFall;
         this.avoidWater = avoidWater;
         this.repairFarmland = repairFarmland;
         this.supportedBridges = supportedBridges;
         this.fallPolicy = fallPolicy;
      }

      public PathOptions(boolean var1, boolean var2, boolean var3, boolean var4, boolean var5) {
         this(var1, var2, var3, var4, var5, false, AiPathfinder.RouteStrategy.FASTEST, AiPathfinder.ThreatField.NONE);
      }

      public PathOptions(
         boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, AiPathfinder.RouteStrategy var6, AiPathfinder.ThreatField var7
      ) {
         this(var1, var2, var3, var4, var5, false, var6, var7);
      }

      public PathOptions(
         boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6, AiPathfinder.RouteStrategy var7, AiPathfinder.ThreatField var8
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, false, false, false);
      }

      public PathOptions(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         AiPathfinder.RouteStrategy var7,
         AiPathfinder.ThreatField var8,
         boolean var9,
         boolean var10,
         boolean var11
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, 2.0);
      }

      public PathOptions(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         AiPathfinder.RouteStrategy var7,
         AiPathfinder.ThreatField var8,
         boolean var9,
         boolean var10,
         boolean var11,
         double var12
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, Set.of());
      }

      AiPathfinder.PathOptions withFallPolicy(AiPathfinder.FallPolicy var1) {
         return new AiPathfinder.PathOptions(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            this.disabledTypes,
            this.constructionBlocks,
            this.maxFall,
            this.avoidWater,
            this.repairFarmland,
            this.supportedBridges,
            var1
         );
      }

      public AiPathfinder.PathOptions withSupportedBridges(boolean var1) {
         return new AiPathfinder.PathOptions(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            this.disabledTypes,
            this.constructionBlocks,
            this.maxFall,
            this.avoidWater,
            this.repairFarmland,
            var1,
            this.fallPolicy
         );
      }

      public AiPathfinder.PathOptions withFarmlandRepair(boolean var1) {
         return new AiPathfinder.PathOptions(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            this.disabledTypes,
            this.constructionBlocks,
            this.maxFall,
            this.avoidWater,
            var1,
            this.supportedBridges,
            this.fallPolicy
         );
      }

      public AiPathfinder.PathOptions withConstructionBlocks(int var1) {
         return new AiPathfinder.PathOptions(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            var1 > 0,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            this.disabledTypes,
            var1,
            this.maxFall,
            this.avoidWater,
            this.repairFarmland,
            this.supportedBridges,
            this.fallPolicy
         );
      }

      public AiPathfinder.PathOptions withMovementLimits(int var1, boolean var2) {
         return new AiPathfinder.PathOptions(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            this.disabledTypes,
            this.constructionBlocks,
            Math.max(0, Math.min(3, var1)),
            var2,
            this.repairFarmland,
            this.supportedBridges,
            this.fallPolicy
         );
      }

      AiPathfinder.PathOptions withoutConstruction() {
         return !this.allowDig && !this.allowPillar && !this.allowBridge && !this.hasBlocks && this.constructionBlocks == 0 && !this.supportedBridges
            ? this
            : new AiPathfinder.PathOptions(
               false,
               false,
               false,
               this.canSprint,
               false,
               this.allowParkour,
               this.strategy,
               this.threats,
               this.jumpBoost,
               this.slowFalling,
               this.levitation,
               this.breakReluctance,
               this.disabledTypes,
               0,
               this.maxFall,
               this.avoidWater,
               this.repairFarmland,
               false,
               this.fallPolicy
            );
      }

      public AiPathfinder.PathOptions withBreakReluctance(double var1) {
         return new AiPathfinder.PathOptions(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            var1,
            this.disabledTypes,
            this.constructionBlocks,
            this.maxFall,
            this.avoidWater,
            this.repairFarmland,
            this.supportedBridges,
            this.fallPolicy
         );
      }

      public AiPathfinder.PathOptions withDig() {
         return new AiPathfinder.PathOptions(
            true,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            this.disabledTypes,
            this.constructionBlocks,
            this.maxFall,
            this.avoidWater,
            this.repairFarmland,
            this.supportedBridges,
            this.fallPolicy
         );
      }

      public AiPathfinder.PathOptions withDisabledTypes(Set<AiPathfinder.MoveType> var1) {
         return new AiPathfinder.PathOptions(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            var1,
            this.constructionBlocks,
            this.maxFall,
            this.avoidWater,
            this.repairFarmland,
            this.supportedBridges,
            this.fallPolicy
         );
      }
   }

   public record SearchProgress(int expanded, List<AiPathfinder.PathStep> trail) {
   }

   public enum ReplanAction {
      REPLAN,
      BLACKLIST_AND_REPLAN,
      FAILED;
   }

   private record PathBounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
      static AiPathfinder.PathBounds around(AiPathfinder.PathResult var0) {
         BlockPos var1 = var0.movements().getFirst().from();
         int var2 = var1.getX();
         int var3 = var2;
         int var4 = var1.getY();
         int var5 = var4;
         int var6 = var1.getZ();
         int var7 = var6;

         for (AiPathfinder.PathStep var9 : var0.movements()) {
            BlockPos var10 = var9.to();
            var2 = Math.min(var2, var10.getX());
            var3 = Math.max(var3, var10.getX());
            var4 = Math.min(var4, var10.getY());
            var5 = Math.max(var5, var10.getY());
            var6 = Math.min(var6, var10.getZ());
            var7 = Math.max(var7, var10.getZ());
         }

         return new AiPathfinder.PathBounds(var2 - 7, var3 + 7, var4 - 5, var5 + 5, var6 - 7, var7 + 7);
      }

      boolean contains(int var1, int var2, int var3) {
         return var1 >= this.minX && var1 <= this.maxX && var2 >= this.minY && var2 <= this.maxY && var3 >= this.minZ && var3 <= this.maxZ;
      }
   }

   public enum RouteStrategy {
      FASTEST,
      SAFEST,
      SAVER;

      public static AiPathfinder.RouteStrategy parseCompat(String var0) {
         return parse(var0);
      }

      public static AiPathfinder.RouteStrategy parse(String var0) {
         try {
            return valueOf(var0.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException | NullPointerException var2) {
            throw new IllegalArgumentException("strategy must be fastest, safest or saver");
         }
      }
   }

   public record CellOccupancy(long occupied, boolean pending) {
   }

   public static final class MoveGenerator {
      private static final int[][] CARDINAL_DIRS = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
      private static final int[][] DIAGONAL_DIRS = new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
      static final int MAX_JUMP_RISE = 1;
      static final int MIN_PARKOUR_GAP = 2;
      static final int MAX_STANDARD_JUMP_DROP = 4;
      static final int BASE_JUMP_RANGE = 5;
      private static final int[][] EIGHT_DIRS = new int[][]{{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
      private static final AiPathfinder.MoveGenerator.JumpOffset[] BASE_JUMP_OFFSETS = buildJumpOffsets(5);
      private static final AiPathfinder.MoveGenerator.JumpOffset[] SPEED_JUMP_OFFSETS = buildJumpOffsets(7);
      private static final AiPathfinder.MoveGenerator.JumpOffset[] DROP_JUMP_OFFSETS = buildJumpOffsets(11);
      private static final AiPathfinder.MoveGenerator.JumpOffset[] BOUNDED_DROP_JUMP_OFFSETS = buildJumpOffsets(8);
      private static final int MAX_BOUNDED_JUMP_DROP = 14;
      static final double MIN_JUMP_MARGIN = 0.1;
      static final double BASE_MOVEMENT_SPEED = 0.1;
      static final int MAX_SAFE_BRIDGE_DEPTH = 3;
      static final int MAX_RUNUP_SCAN_BLOCKS = 4;
      static final int MAX_BRIDGE_DEPTH_SCAN = 32;
      static boolean allowCantileverBridgeFromSolid = true;

      private MoveGenerator() {
      }

      public static boolean isStandableCompat(AiPathfinder.CellInfo a, AiPathfinder.CellInfo b, AiPathfinder.CellInfo c) {
         return isStandable(a, b, c);
      }

            public static boolean isStandable(AiPathfinder.CellInfo var0, AiPathfinder.CellInfo var1, AiPathfinder.CellInfo var2) {
         if (!var0.loaded() || !var1.loaded() || !var2.loaded()) {
            return false;
         }

         if (var0.hazardFeet() || var0.feetTrigger() || var1.hazardFeet() || var2.hazardFloor() || var0.lava() || var1.lava() || var2.lava()) {
            return false;
         }

         if (var0.water() && var1.water()) {
            return false;
         }

         if (!var2.passable() && var2.top16() > 16) {
            return false;
         }

         boolean var3 = var1.passable() || !var2.passable() && var2.top16() > 0 && var2.top16() <= 3;
         return var0.passable() && var3 && (!var2.passable() && !var2.door() || var0.water() || var0.climbable());
      }

      static boolean isTightLandingStandable(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, AiJumpSolver.JumpPlan var2) {
         AiPathfinder.CellInfo var3 = (AiPathfinder.CellInfo)var0.apply(var1.below());
         if (var3.loaded()
            && !var3.passable()
            && var3.top16() <= 24
            && !var3.floorContactHazard()
            && !var3.farmland()
            && !var3.lava()
            && !var3.water()
            && !var3.door()) {
            boolean var4 = var3.narrowFooting();
            int var5 = var3.top16() > 16 ? 2 : 1;

            for (int var6 = 0; var6 <= var5; var6++) {
               BlockPos var7 = var1.above(var6);
               AiPathfinder.CellInfo var8 = (AiPathfinder.CellInfo)var0.apply(var7);
               if (!var8.loaded()
                  || var8.hazardFeet()
                  || var6 == 0 && var8.feetTrigger()
                  || var8.lava()
                  || var8.water()
                  || var8.climbable()
                  || var8.web()
                  || var8.portal()) {
                  return false;
               }

               if (!var8.passable()) {
                  if (var0 instanceof AiPathfinder.MoveGenerator.CachedGrid var9
                     && (!var9.fullPhysicsParkour || var9.putAt_S_I(var7.getX(), var7.getY(), var7.getZ()) == 1)) {
                     return false;
                  }

                  var4 = true;
               }
            }

            if (!var4) {
               return var3.top16() > 16;
            } else if (var2 != null) {
               return true;
            } else if (var0 instanceof AiPathfinder.MoveGenerator.CachedGrid var10) {
               double var11 = effectiveSurfaceY(var10, var1, var3);
               return AiJumpSolver.hasStandablePoint(var10.plannerWorld(), var1, var11);
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      static double effectiveSurfaceY(AiPathfinder.MoveGenerator.CachedGrid var0, BlockPos var1, AiPathfinder.CellInfo var2) {
         double var3 = surfaceY(var1.getY(), var2);
         return var0.fullPhysicsParkour
               && var2.slabStair()
               && var2.top16() == 16
               && !var0.probePos(var1.above()).passable()
               && !AiJumpSolver.hasStandablePoint(var0.plannerWorld(), var1, var3)
               && AiJumpSolver.hasStandablePoint(var0.plannerWorld(), var1, var3 - 0.5)
            ? var3 - 0.5
            : var3;
      }

      private static boolean isLowClearanceCell(AiPathfinder.MoveGenerator.CachedGrid var0, BlockPos var1, AiPathfinder.CellInfo var2) {
         if (var2.loaded() && var2.top16() > 0 && var2.top16() <= 8 && !var2.hazardFloor() && !var2.lava() && !var2.water() && !var2.farmland()) {
            AiPathfinder.CellInfo var3 = var0.probePos(var1.above());
            if (var3.loaded() && !var3.passable() && !var3.hazardFeet()) {
               double var4 = surfaceY(var1.getY(), var2);
               double var6 = var1.getX() + 0.5;
               double var8 = var1.getZ() + 0.5;
               double var10 = 0.3;
               AABB var12 = new AABB(var6 - var10, var4 + 1.0E-5, var8 - var10, var6 + var10, var4 + 1.5 - 1.0E-5, var8 + var10);
               return var0.plannerWorld().collisions(var12).isEmpty() && !AiJumpSolver.canStandAt(var0.plannerWorld(), var6, var4, var8);
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private static void generateLowClearanceMoves(
         AiPathfinder.MoveGenerator.CachedGrid var0,
         AiPathfinder.PathOptions var1,
         Set<Long> var2,
         BlockPos var3,
         double var4,
         boolean var6,
         List<AiPathfinder.PathStep> var7
      ) {
         int var8 = var6 ? 2 : 1;

         for (int var9 = -var8; var9 <= var8; var9++) {
            for (int var10 = -var8; var10 <= var8; var10++) {
               if (var9 != 0 || var10 != 0) {
                  for (int var11 = 1; var11 >= -1; var11--) {
                     BlockPos var12 = var3.offset(var9, var11, var10);
                     if (!var2.contains(var12.asLong())) {
                        AiPathfinder.CellInfo var13 = var0.probePos(var12.below());
                        if (var13.loaded() && !var13.passable() && !var13.hazardFloor() && !var13.water() && !var13.lava()) {
                           double var14 = surfaceY(var12.getY(), var13);
                           if (!(var14 > var4 + 0.5625) && !(var14 < var4 - 1.25)) {
                              boolean var16 = isLowClearanceCell(var0, var12, var13);
                              if (var16
                                 || var6
                                    && isStandable(
                                       var0.probePos(var12), var0.probePos(var12.above()), var13
                                    )) {
                                 AiJumpSolver.JumpPlan var17 = AiJumpManeuver.lowClearance(
                                    var0.plannerWorld(),
                                    var3,
                                    var4,
                                    var12,
                                    var14,
                                    var6,
                                    var16,
                                    var1.canSprint() && var1.strategy() != AiPathfinder.RouteStrategy.SAVER
                                 );
                                 if (var17 != null) {
                                    var7.add(AiPathfinder.PathStep.jump(var3, var12, AiPathfinder.MoveCosts.jumpPlanCost(var17), var17));
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      private static boolean canStepUpStairFace(
         AiPathfinder.MoveGenerator.CachedGrid var0, BlockPos var1, int var2, int var3, double var4, double var6
      ) {
         short var8 = var0.putAt_S_I(var1.getX(), var1.getY(), var1.getZ());
         double var9 = -1.0 / 0.0;

         for (AABB var12 : AiShapes.boxes(var8)) {
            if ((var2 <= 0 || !(var12.minX > 1.0E-6))
               && (var2 >= 0 || !(var12.maxX < 0.999999))
               && (var3 <= 0 || !(var12.minZ > 1.0E-6))
               && (var3 >= 0 || !(var12.maxZ < 0.999999))) {
               double var13 = var2 != 0 ? var12.minZ : var12.minX;
               double var15 = var2 != 0 ? var12.maxZ : var12.maxX;
               if (!(var13 >= 0.8) && !(var15 <= 0.2)) {
                  var9 = Math.max(var9, var12.maxY);
               }
            }
         }

         return Double.isFinite(var9) && var6 > var9 && var1.getY() + var9 - var4 <= 0.5625 && var6 - var9 <= 0.5625;
      }

      static double surfaceY(int var0, AiPathfinder.CellInfo var1) {
         return var0 - 1 + var1.top16() / 16.0;
      }

      static boolean hasTwoBlockHeadroom(AiPathfinder.CellInfo var0, AiPathfinder.CellInfo var1) {
         return var0.passable() && var1.passable();
      }

      public static int generateMoves(
         AiPathfinder.GridView var0, AiPathfinder.PathOptions var1, Set<Long> var2, BlockPos var3, List<AiPathfinder.PathStep> var4
      ) {
         return generateMovesCached(new AiPathfinder.MoveGenerator.CachedGrid(var0, 256), var1, var2, var3, var4);
      }

      static int generateMovesCached(
         AiPathfinder.MoveGenerator.CachedGrid var0,
         AiPathfinder.PathOptions var1,
         Set<Long> var2,
         BlockPos var3,
         List<AiPathfinder.PathStep> var4
      ) {
         var0.queryFlags = 0;
         var0.fallPolicy = var1.fallPolicy();
         AiPathfinder.CellInfo var5 = var0.probePos(var3);
         if (!var5.loaded()) {
            return var0.queryFlags;
         }

         AiPathfinder.CellInfo var6 = var0.probePos(var3.below());
         AiPathfinder.CellInfo var7 = var0.probePos(var3.above());
         boolean var8 = var1.allowParkour()
            && !var1.jumpBoost()
            && !var1.levitation()
            && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.JUMP)
            && canTakeoffFrom(var5, var6)
            && !var7.web();
         double var9 = var6.passable() ? var3.getY() : effectiveSurfaceY(var0, var3, var6);
         boolean var11 = !var6.narrowFooting() || AiJumpSolver.canStandAt(var0.plannerWorld(), var3.getX() + 0.5, var9, var3.getZ() + 0.5);
         AABB var12 = null;
         if (var7.loaded()
            && (
               !var5.passable() && var0.putAt_S_I(var3.getX(), var3.getY(), var3.getZ()) > 1
                  || !var7.passable() && var0.putAt_S_I(var3.getX(), var3.getY() + 1, var3.getZ()) > 1
            )
            && AiJumpSolver.canStandAt(var0.plannerWorld(), var3.getX() + 0.5, var9, var3.getZ() + 0.5)) {
            var12 = AiPhysics.MotionState.standing(var3.getX() + 0.5, var9, var3.getZ() + 0.5).box(false).deflate(1.0E-4);
         }

         if (var0.fullPhysicsParkour && var8 && isLowClearanceCell(var0, var3, var6)) {
            generateLowClearanceMoves(var0, var1, var2, var3, var9, true, var4);
            return var0.queryFlags;
         }

         if (!isStandable(var5, var7, var6) && !var5.water() && !var5.climbable() && var12 == null) {
            boolean var13 = var8 && var0.fullPhysicsParkour && isTightLandingStandable(var0, var3, null);
            if (!var5.passable() || var7.loaded() && !var7.passable()) {
               if (var13) {
                  generateParkourMoves(var0, var1, var2, var3, var6, var9, var4);
                  return var0.queryFlags;
               }

               var0.queryFlags |= 2;
               if (var1.allowDig() && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.DIG_TUNNEL)) {
                  generateDigOutMoves(var0, var1, var2, var3, var5, var4);
               }

               return var0.queryFlags;
            }

            if (!var13 && var7.loaded() && var6.loaded()) {
               var0.queryFlags |= 4;
            }
         }

         if (var5.water() || var5.climbable()) {
            BlockPos var48 = var3.above();
            AiPathfinder.CellInfo var14 = var0.probePos(var48);
            boolean var15 = var14.climbable() || var14.water();
            if (!isMoveTypeDisabled(var1, AiPathfinder.MoveType.CLIMB_1)
               && var15
               && !var2.contains(var48.asLong())
               && (var14.passable() || var14.climbable())
               && !var14.lava()
               && (var0.probePos(var48.above()).passable() || var0.probePos(var48.above()).climbable())
               && !var14.hazardFeet()) {
               var4.add(AiPathfinder.PathStep.travel(AiPathfinder.MoveType.CLIMB_1, var3, var48, 5.5));
            }

            BlockPos var16 = var3.below();
            if (!isMoveTypeDisabled(var1, AiPathfinder.MoveType.CLIMB_1)
               && !var2.contains(var16.asLong())
               && (
                  var5.climbable() && var0.probePos(var16).climbable()
                     || isStandable(
                        var0.probePos(var16),
                        var0.probePos(var16.above()),
                        var0.probePos(var16.below())
                     )
               )) {
               var4.add(AiPathfinder.PathStep.travel(AiPathfinder.MoveType.CLIMB_1, var3, var16, 5.5));
            }

            if (var5.climbable()
               && var0.fullPhysicsParkour
               && var1.allowParkour()
               && !var1.jumpBoost()
               && !var1.levitation()
               && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.LADDER_JUMP)) {
               boolean var17 = var1.canSprint() && var1.strategy() != AiPathfinder.RouteStrategy.SAVER;
               generateLadderSideJumps(var0, var2, var3, var17, var4);
               generateClimbTargetJumps(var0, var2, var3, var3.getY() + 0.7, 255, true, var17, var4);
            }
         }

         for (int[] var60 : CARDINAL_DIRS) {
            BlockPos var64 = var3.offset(var60[0], 0, var60[1]);
            if ((var12 == null || !var0.plannerWorld().putAt_Z_AABB(var12.expandTowards(var60[0], 0.0, var60[1])))
               && !var0.isBlockedByOpenDoor(var3, var60[0], var60[1])
               && !var0.isBlockedByOpenDoor(var64, var60[0], var60[1])) {
               AiPathfinder.CellInfo var18 = var0.probePos(var64);
               AiPathfinder.CellInfo var19 = var0.probePos(var64.above());
               AiPathfinder.CellInfo var20 = var0.probePos(var64.below());
               if (var1.allowDig()
                  && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.DIG_TUNNEL)
                  && var20.loaded()
                  && !var20.passable()
                  && !var20.hazardFloor()
                  && !var20.lava()
                  && !var2.contains(var64.asLong())
                  && !var18.hazardFeet()
                  && !var18.lava()
                  && !var19.hazardFeet()
                  && !var19.lava()) {
                  boolean var21 = var18.diggable();
                  boolean var22 = var19.diggable();
                  if ((var21 || var18.passable()) && (var22 || var19.passable()) && (var21 || var22)) {
                     ArrayList var23 = new ArrayList(2);
                     double var24 = 0.0;
                     double var26 = 0.0;
                     if (var21) {
                        var23.add(var64);
                        var24 += var18.breakTicks() + var1.breakReluctance() + var18.destructionPenalty();
                        var26 += var18.breakTicks();
                     }

                     if (var22) {
                        var23.add(var64.above());
                        var24 += var19.breakTicks() + var1.breakReluctance() + var19.destructionPenalty();
                        var26 += var19.breakTicks();
                     }

                     if (var1.strategy() == AiPathfinder.RouteStrategy.SAVER) {
                        var24 *= 2.0;
                     }

                     var4.add(
                        new AiPathfinder.PathStep(
                           AiPathfinder.MoveType.DIG_TUNNEL,
                           var3,
                           var64,
                           var23,
                           List.of(),
                           AiPathfinder.MoveCosts.digTunnelCost(var24) + portalAndWebPenalty(var0, var3, var64, false),
                           var26
                        )
                     );
                  }
               }

               BlockPos var74 = var64.below();
               if (var1.allowDig() && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.DIG_DESCEND) && !var2.contains(var74.asLong())) {
                  AiPathfinder.CellInfo var77 = var0.probePos(var74);
                  AiPathfinder.CellInfo var86 = var0.probePos(var74.below());
                  if (var86.loaded()
                     && !var86.passable()
                     && !var86.hazardFloor()
                     && !var86.lava()
                     && !var86.farmland()
                     && var86.top16() == 16
                     && isPassableOrDiggableDry(var19)
                     && isPassableOrDiggableDry(var18)
                     && isPassableOrDiggableDry(var77)
                     && !var19.web()
                     && !var18.web()
                     && !var77.web()
                     && (var19.diggable() || var18.diggable() || var77.diggable())
                     && hasSafeOvershootAhead(var0, var74, var60[0], var60[1])) {
                     ArrayList var93 = new ArrayList(3);
                     double var25 = 0.0;
                     double var27 = 0.0;

                     for (BlockPos var32 : new BlockPos[]{var64.above(), var64, var74}) {
                        AiPathfinder.CellInfo var33 = var32.equals(var74) ? var77 : (var32.equals(var64) ? var18 : var19);
                        if (var33.diggable()) {
                           var93.add(var32);
                           var25 += var33.breakTicks() + var1.breakReluctance() + var33.destructionPenalty();
                           var27 += var33.breakTicks();
                        }
                     }

                     if (var1.strategy() == AiPathfinder.RouteStrategy.SAVER) {
                        var25 *= 2.0;
                     }

                     double var120 = var1.strategy() == AiPathfinder.RouteStrategy.SAFEST ? 1.0 : 0.0;
                     var4.add(
                        new AiPathfinder.PathStep(
                           AiPathfinder.MoveType.DIG_DESCEND, var3, var74, var93, List.of(), AiPathfinder.MoveCosts.descendCost(1) + var120 + var25, var27
                        )
                     );
                  }
               }

               if (var1.allowBridge()
                  && var1.hasBlocks()
                  && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.BRIDGE_CELL)
                  && var0.probePos(var3.below()).top16() == 16
                  && var0.putAt_S_I(var3.getX(), var3.getY() - 1, var3.getZ()) == 1
                  && !var2.contains(var64.asLong())
                  && var18.passable()
                  && var19.passable()
                  && !var18.hazardFeet()
                  && !var18.lava()
                  && !var19.hazardFeet()
                  && !var19.lava()
                  && var20.loaded()
                  && var20.passable()
                  && (var1.supportedBridges() || !var20.water() && !var20.lava())
                  && (
                     var1.supportedBridges()
                        || !var0.probePos(var64.below(2)).water() && !var0.probePos(var64.below(2)).lava()
                  )
                  && hasBridgeSupportAnchor(var0, var3, var64, var0.incomingMove)) {
                  int var78 = var1.supportedBridges() ? 0 : measureBridgeDepth(var0, var64.below());
                  if ((var1.strategy() == AiPathfinder.RouteStrategy.SAFEST || AiPathfinder.MoveCosts.isCautiousBridgeActive()) && var78 > 3) {
                     var78 = -1;
                  }

                  if (var78 >= 0) {
                     double var87 = var1.strategy() == AiPathfinder.RouteStrategy.SAVER ? 9.0 : 4.5;
                     AiPathfinder.PathStep var98 = var0.incomingMove;
                     boolean var105 = var98 != null
                        && var98.type() == AiPathfinder.MoveType.BRIDGE_CELL
                        && var98.to().getX() - var98.from().getX() == var64.getX() - var3.getX()
                        && var98.to().getZ() - var98.from().getZ() == var64.getZ() - var3.getZ();
                     if (!var105) {
                        var87 += 10.0;
                     }

                     if (var1.supportedBridges() || !var0.probePos(var64.above(2)).passable()) {
                        var87 += 2.0;
                     }

                     if (var78 > 3) {
                        var87 += 1.125 * Math.min(var78 - 3, 20);
                     }

                     var4.add(new AiPathfinder.PathStep(AiPathfinder.MoveType.BRIDGE_CELL, var3, var64, List.of(), List.of(var64.below()), var87));
                  }
               }

               if (AiPathfinder.MoveCosts.isDropBridgeActive()
                  && var1.allowBridge()
                  && var1.hasBlocks()
                  && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.BRIDGE_CELL)
                  && var0.probePos(var3.below()).top16() == 16
                  && var0.putAt_S_I(var3.getX(), var3.getY() - 1, var3.getZ()) == 1
                  && var18.passable()
                  && var19.passable()
                  && !var18.hazardFeet()
                  && !var18.lava()
                  && !var19.hazardFeet()
                  && !var19.lava()
                  && var20.loaded()
                  && var20.passable()
                  && !var20.water()
                  && !var20.lava()
                  && !var20.hazardFeet()) {
                  BlockPos var79 = var64.below();
                  BlockPos var88 = var79.below();
                  AiPathfinder.CellInfo var94 = var0.probePos(var88);
                  if (!var2.contains(var79.asLong()) && var94.loaded() && var94.passable() && !var94.water() && !var94.lava() && hasAdjacentSolidAnchor(var0, var88)) {
                     int var99 = measureBridgeDepth(var0, var88);
                     if ((var1.strategy() == AiPathfinder.RouteStrategy.SAFEST || AiPathfinder.MoveCosts.isCautiousBridgeActive()) && var99 > 3) {
                        var99 = -1;
                     }

                     if (var99 >= 0) {
                        var4.add(
                           new AiPathfinder.PathStep(
                              AiPathfinder.MoveType.BRIDGE_CELL, var3, var79, List.of(), List.of(var88), 14.5 + AiPathfinder.MoveCosts.descendCost(1)
                           )
                        );
                     }
                  }
               }

               if (var11 && !var2.contains(var64.asLong()) && isStandable(var18, var19, var20) && (!var1.avoidWater() || !var18.water())) {
                  double var80 = surfaceY(var64.getY(), var20) - var9;
                  if (var80 <= 0.5625) {
                     boolean var95 = var1.canSprint()
                        && var1.strategy() != AiPathfinder.RouteStrategy.SAVER
                        && !var18.water()
                        && !var18.thorny()
                        && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.SPRINT);
                     double var100 = var18.water() ? 3.0 + (var1.strategy() == AiPathfinder.RouteStrategy.SAFEST ? 8.0 : 0.0) : 0.0;
                     double var110 = var20.crawl();
                     double var121 = var18.thorny() ? 25.0 : 0.0;
                     AiPathfinder.MoveType var131 = var95 ? AiPathfinder.MoveType.SPRINT : AiPathfinder.MoveType.WALK;
                     if (!isMoveTypeDisabled(var1, var131)) {
                        var4.add(
                           AiPathfinder.PathStep.travel(
                              var131,
                              var3,
                              var64,
                              (var95 ? 3.2 : 4.5 + var100) * var110 + var121 + portalAndWebPenalty(var0, var3, var64, var95) + var0.adjacentContactHazardPenalty(var64)
                           )
                        );
                     }

                     BlockPos var137 = var64.offset(var60[0], 0, var60[1]);
                     AiPathfinder.CellInfo var141 = var0.probePos(var137.below());
                     if (var95
                        && !var141.passable()
                        && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.SPRINT_JUMP)
                        && canSprintJumpFlat(var0, var3, var64)
                        && !var0.hasAdjacentContactHazard(var64)
                        && !var0.hasAdjacentContactHazard(var137)) {
                        var4.add(
                           AiPathfinder.PathStep.travel(AiPathfinder.MoveType.SPRINT_JUMP, var3, var64, hasLowCeilingSprintJump(var0, var3, var64) ? 2.2 : 2.9000000000000004)
                        );
                     }
                  } else if (var80 <= 1.250001
                     && !var5.water()
                     && !var6.farmland()
                     && !var6.honey()
                     && (!var0.fullPhysicsParkour || !var1.allowParkour() || !var5.climbable() || !var6.passable())
                     && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.JUMP_UP_1)
                     && !var5.web()
                     && !var7.web()
                     && !var18.web()
                     && !var19.web()
                     && !var0.probePos(var3.above(2)).web()
                     && var0.probePos(var3.above(2)).passable()) {
                     var4.add(AiPathfinder.PathStep.travel(AiPathfinder.MoveType.JUMP_UP_1, var3, var64, 9.0));
                  }
               }

               if (var1.allowDig()
                  && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.DIG_TUNNEL)
                  && (var18.web() || var19.web())
                  && !var2.contains(var64.asLong())
                  && isStandable(var18, var19, var20)
                  && surfaceY(var64.getY(), var20) - var9 <= 0.5625) {
                  ArrayList var81 = new ArrayList(2);
                  double var89 = 0.0;
                  double var101 = 0.0;

                  for (BlockPos var127 : new BlockPos[]{var64, var64.above()}) {
                     AiPathfinder.CellInfo var132 = var127 == var64 ? var18 : var19;
                     if (var132.web()) {
                        if (var132.breakTicks() >= 1.0 / 0.0) {
                           var81 = null;
                           break;
                        }

                        var81.add(var127);
                        var89 += var132.breakTicks() + var1.breakReluctance();
                        var101 += var132.breakTicks();
                     }
                  }

                  if (var81 != null && !var81.isEmpty()) {
                     var4.add(
                        new AiPathfinder.PathStep(
                           AiPathfinder.MoveType.DIG_TUNNEL,
                           var3,
                           var64,
                           var81,
                           List.of(),
                           AiPathfinder.MoveCosts.digTunnelCost(var89) + var0.adjacentContactHazardPenalty(var64),
                           var101
                        )
                     );
                  }
               }

               if (!var2.contains(var64.asLong())
                  && var18.door()
                  && !var18.passable()
                  && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.DOOR)
                  && !var18.hazardFeet()
                  && !var18.lava()
                  && !var19.hazardFeet()
                  && !var19.lava()
                  && (var19.door() || var19.passable())
                  && var20.loaded()
                  && !var20.passable()
                  && !var20.hazardFloor()
                  && !var20.lava()
                  && surfaceY(var64.getY(), var20) - var9 <= 0.5625) {
                  var4.add(AiPathfinder.PathStep.travel(AiPathfinder.MoveType.DOOR, var3, var64, 7.5));
               }

               BlockPos var82 = var64.above();
               AiPathfinder.CellInfo var90 = var0.probePos(var82);
               int var96 = !var18.passable() && var90.top16() > 0 && var90.top16() <= 4 ? 2 : 1;
               BlockPos var102 = var64.above(var96);
               if (!var2.contains(var102.asLong())) {
                  AiPathfinder.CellInfo var106 = var0.probePos(var102);
                  AiPathfinder.CellInfo var112 = var0.probePos(var102.above());
                  AiPathfinder.CellInfo var116 = var0.probePos(var102.below());
                  if ((!var116.passable() || var5.water() && var106.water()) && isStandable(var106, var112, var116)) {
                     double var123 = surfaceY(var102.getY(), var116) - var9;
                     if (var123 <= 0.5625
                        || var116.slabStair() && canStepUpStairFace(var0, var102.below(), var60[0], var60[1], var9, var116.top16() / 16.0)) {
                        if (!isMoveTypeDisabled(var1, AiPathfinder.MoveType.STEP_UP_1)) {
                           boolean var133 = var1.canSprint()
                              && var1.strategy() != AiPathfinder.RouteStrategy.SAVER
                              && !var106.water()
                              && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.SPRINT);
                           double var138 = var123 > 0.0 && var123 <= 0.062501 ? 4.5 : (var133 ? 3.6 : 5.0);
                           var4.add(
                              AiPathfinder.PathStep.travel(
                                 AiPathfinder.MoveType.STEP_UP_1,
                                 var3,
                                 var102,
                                 var138 + portalAndWebPenalty(var0, var3, var102, false) + var0.adjacentContactHazardPenalty(var102)
                              )
                           );
                        }
                     } else if (var123 <= 1.25
                        && (!var6.farmland() || var5.water())
                        && !var6.honey()
                        && (!var0.fullPhysicsParkour || !var1.allowParkour() || !var5.climbable() || !var6.passable())
                        && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.JUMP_UP_1)
                        && !var5.web()
                        && !var7.web()
                        && !var106.web()
                        && !var112.web()
                        && !var0.probePos(var3.above(2)).web()
                        && (!var5.water() || checkOvershootColumn(var0, var82, var60[0], var60[1], 1, true))
                        && var0.probePos(var3.above(2)).passable()) {
                        var4.add(AiPathfinder.PathStep.travel(AiPathfinder.MoveType.JUMP_UP_1, var3, var102, 9.0));
                     }
                  }
               }

               if (var1.allowDig()
                  && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.DIG_ASCEND)
                  && !var2.contains(var82.asLong())
                  && !var6.farmland()
                  && !var6.honey()
                  && !var5.water()
                  && !var5.lava()
                  && !var5.web()
                  && !var7.web()
                  && var18.loaded()
                  && !var18.passable()
                  && var18.top16() == 16
                  && !var18.hazardFloor()
                  && !var18.lava()
                  && !var18.door()
                  && surfaceY(var82.getY(), var18) - var9 <= 1.250001) {
                  BlockPos var107 = var3.above(2);
                  AiPathfinder.CellInfo var113 = var0.probePos(var107);
                  AiPathfinder.CellInfo var117 = var0.probePos(var82);
                  AiPathfinder.CellInfo var124 = var0.probePos(var82.above());
                  if (isPassableOrDiggableDry(var113)
                     && isPassableOrDiggableDry(var117)
                     && isPassableOrDiggableDry(var124)
                     && !var113.web()
                     && !var117.web()
                     && !var124.web()
                     && (var113.diggable() || var117.diggable() || var124.diggable())
                     && (!var113.diggable() || !var0.probePos(var107.above()).falling())) {
                     ArrayList var128 = new ArrayList(3);
                     double var134 = 0.0;
                     double var142 = 0.0;

                     for (BlockPos var38 : new BlockPos[]{var107, var82, var82.above()}) {
                        AiPathfinder.CellInfo var39 = var38 == var107 ? var113 : (var38 == var82 ? var117 : var124);
                        if (var39.diggable()) {
                           var128.add(var38);
                           var134 += var39.breakTicks() + var1.breakReluctance() + var39.destructionPenalty();
                           var142 += var39.breakTicks();
                        }
                     }

                     if (var1.strategy() == AiPathfinder.RouteStrategy.SAVER) {
                        var134 *= 2.0;
                     }

                     var4.add(new AiPathfinder.PathStep(AiPathfinder.MoveType.DIG_ASCEND, var3, var82, var128, List.of(), 9.0 + var134, var142));
                  }
               }

               long var108 = -9223372036854775808L;
               int var118 = (int)Math.floor(var9 + 1.8 - 1.0E-6);
               int var125 = var1.fallPolicy().allowsExtendedDrop() ? (int)var1.fallPolicy().maxDropBlocks : 8;

               for (int var129 = 1; var129 <= var125; var129++) {
                  BlockPos var135 = var64.below(var129);
                  AiPathfinder.CellInfo var139 = var0.probePos(var135);
                  if (!var139.loaded()) {
                     break;
                  }

                  BlockPos var143 = var135.above();
                  if (var143.getY() <= var118 && !var0.probePos(var143).passable()
                     || var129 == 1 && var64.above().getY() <= var118 && !var0.probePos(var64.above()).passable()
                     || var139.feetTrigger()
                     || var1.fallPolicy() != AiPathfinder.FallPolicy.STANDARD && (var139.lava() || var139.hazardFeet())) {
                     break;
                  }

                  if (!var2.contains(var135.asLong())) {
                     AiPathfinder.CellInfo var34 = var0.probePos(var135.below());
                     double var144 = var139.climbable() ? var135.getY() : surfaceY(var135.getY(), var34);
                     double var145 = var9 - var144;
                     double var146 = var1.fallPolicy().allowsExtendedDrop()
                        ? var125
                        : Math.min(3, var1.maxFall()) + (var1.fallPolicy() == AiPathfinder.FallPolicy.STANDARD ? 0.5625 : 0.0);
                     if (var145 <= var146
                        && !var34.farmland()
                        && isStandable(var139, var0.probePos(var135.above()), var34)
                        && (
                           var145 <= 0.562501
                              || var129 == 1 && var139.climbable()
                              || hasSafeOvershootAhead(var0, var135, var60[0], var60[1], (int)Math.ceil(var145 - 1.0E-6))
                        )) {
                        AiPathfinder.MoveType var41 = var129 == 1
                           ? AiPathfinder.MoveType.DESCEND_1
                           : (var129 == 2 ? AiPathfinder.MoveType.DESCEND_2 : AiPathfinder.MoveType.DESCEND_3);
                        if (var129 == 1 && var145 > 1.5625) {
                           var41 = AiPathfinder.MoveType.DESCEND_2;
                        }

                        double var42 = var1.strategy() == AiPathfinder.RouteStrategy.SAFEST ? var129 * 1.0 : 0.0;
                        if (!isMoveTypeDisabled(var1, var41)) {
                           double var44 = var145 > 0.0 && var145 <= 0.062501
                              ? 4.5
                              : (
                                 var129 <= 3
                                    ? AiPathfinder.MoveCosts.descendCost(var129)
                                    : AiPathfinder.MoveCosts.descendCost(3) + Math.ceil(Math.sqrt(var145) * 5.0 - 8.0)
                              );
                           var4.add(
                              AiPathfinder.PathStep.travel(
                                 var41,
                                 var3,
                                 var135,
                                 var44
                                    + var42
                                    + portalPenalty(var0, var135)
                                    + portalAndWebPenalty(var0, var3, var64, false)
                                    + 255.0 * fallingWebOverlap(var0, var64.getX(), var64.getZ(), var9, surfaceY(var135.getY(), var34))
                              )
                           );
                        }
                     }

                     int var147 = Math.min(var139.fallMult10(), var34.fallMult10());
                     if (var139.passable()
                        && var147 <= 2
                        && !var139.lava()
                        && !var34.lava()
                        && !var139.hazardFeet()
                        && !var139.feetTrigger()
                        && (!var1.avoidWater() || !var139.water())) {
                        BlockPos var148 = null;
                        if (!var139.water()) {
                           if (var34.passable()) {
                              if (!var34.water() && !var34.hazardFeet()) {
                                 var148 = var135.below();
                              }
                           } else {
                              var148 = var135;
                           }
                        } else if (!var0.probePos(var135.above()).water()) {
                           BlockPos var43 = var135;

                           while (var0.probePos(var43.below()).water()) {
                              var43 = var43.below();
                           }

                           if (var3.getY() - var43.getY() <= 8 && isSolidSafeFloor(var0.probePos(var43.below()))) {
                              var148 = var135;
                           }
                        }

                        if (var148 != null && var148.asLong() != var108) {
                           var108 = var148.asLong();
                           if (!var0.probePos(var148.below()).farmland()) {
                              int var149 = Math.max(var129, var3.getY() - var148.getY());
                              double var150 = var1.strategy() == AiPathfinder.RouteStrategy.SAFEST ? var149 * 2.0 : 0.0;
                              double var46 = 255.0 * fallingWebOverlap(var0, var64.getX(), var64.getZ(), var9, var148.getY());
                              if (!isMoveTypeDisabled(var1, AiPathfinder.MoveType.DESCEND_SOFT)) {
                                 var4.add(
                                    AiPathfinder.PathStep.travel(
                                       AiPathfinder.MoveType.DESCEND_SOFT,
                                       var3,
                                       var148,
                                       AiPathfinder.MoveCosts.softDescendCost(var149)
                                          + var150
                                          + portalAndWebPenalty(var0, var3, var64, false)
                                          + var46
                                          + portalPenalty(var0, var148)
                                    )
                                 );
                              }
                           }
                        }
                     }
                  }
               }

               if (var108 == -9223372036854775808L
                  && !var1.avoidWater()
                  && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.DESCEND_SOFT)
                  && var0.probePos(var64).passable()
                  && var0.probePos(var64.above()).passable()
                  && var0.probePos(var64.below()).passable()
                  && var0.probePos(var64.below(2)).passable()) {
                  BlockPos var130 = var0.simulateStepOffLanding(var3, var9, var60[0], var60[1]);
                  if (var130 != null && !var2.contains(var130.asLong()) && !var0.probePos(var130.below()).farmland()) {
                     int var136 = Math.max(1, var3.getY() - var130.getY());
                     double var140 = var1.strategy() == AiPathfinder.RouteStrategy.SAFEST ? var136 * 2.0 : 0.0;
                     var4.add(
                        AiPathfinder.PathStep.travel(AiPathfinder.MoveType.DESCEND_SOFT, var3, var130, AiPathfinder.MoveCosts.softDescendCost(var136) + var140)
                     );
                  }
               }
            }
         }

         for (int[] var61 : DIAGONAL_DIRS) {
            if (!var11) {
               break;
            }

            if (var12 == null || !var0.plannerWorld().putAt_Z_AABB(var12.expandTowards(var61[0], 0.0, var61[1]))) {
               BlockPos var65 = var3.offset(var61[0], 0, var61[1]);
               if (!var2.contains(var65.asLong())) {
                  AiPathfinder.CellInfo var67 = var0.probePos(var65);
                  AiPathfinder.CellInfo var70 = var0.probePos(var65.below());
                  if (isStandable(var67, var0.probePos(var65.above()), var70)
                     && (!var1.avoidWater() || !var67.water())
                     && !(surfaceY(var65.getY(), var70) - var9 > 0.5625)) {
                     BlockPos var71 = var3.offset(var61[0], 0, 0);
                     BlockPos var75 = var3.offset(0, 0, var61[1]);
                     if (isDiagonalCornerClear(var0, var71)
                        && isDiagonalCornerClear(var0, var75)
                        && !var0.hasOpenDoorObstacle(var3)
                        && !var0.hasOpenDoorObstacle(var65)
                        && !var0.hasOpenDoorObstacle(var71)
                        && !var0.hasOpenDoorObstacle(var75)) {
                        boolean var83 = var1.canSprint()
                           && var1.strategy() != AiPathfinder.RouteStrategy.SAVER
                           && !var67.water()
                           && !var67.thorny()
                           && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.SPRINT_DIAGONAL);
                        double var91 = var67.water() ? 3.0 + (var1.strategy() == AiPathfinder.RouteStrategy.SAFEST ? 8.0 : 0.0) : 0.0;
                        double var103 = var70.crawl();
                        AiPathfinder.MoveType var114 = var83 ? AiPathfinder.MoveType.SPRINT_DIAGONAL : AiPathfinder.MoveType.WALK_DIAGONAL;
                        if (!isMoveTypeDisabled(var1, var114)) {
                           var4.add(
                              AiPathfinder.PathStep.travel(
                                 var114,
                                 var3,
                                 var65,
                                 (var83 ? 4.5 : 6.4 + var91) * var103
                                    + (var67.thorny() ? 25.0 : 0.0)
                                    + portalAndWebPenalty(var0, var3, var65, var83)
                                    + var0.adjacentContactHazardPenalty(var65)
                              )
                           );
                        }
                     }
                  }
               }
            }
         }

         if (var8) {
            generateParkourMoves(var0, var1, var2, var3, var6, var9, var4);
            if (var0.fullPhysicsParkour
               && var1.canSprint()
               && var1.strategy() != AiPathfinder.RouteStrategy.SAVER
               && var0.fallPolicy != AiPathfinder.FallPolicy.STANDARD
               && var9 == var3.getY()) {
               generateWallJumpMoves(var0, var2, var3, var9, var4);
            }

            if (var0.fullPhysicsParkour) {
               generateLowClearanceMoves(var0, var1, var2, var3, var9, false, var4);
            }

            if (var0.fullPhysicsParkour && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.SLIME_BOUNCE)) {
               generateSlimeBounceMoves(var0, var1, var2, var3, var9, var4);
            }
         }

         if (var1.allowPillar()
            && var1.hasBlocks()
            && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.PILLAR)
            && !var5.lava()
            && !var5.water()
            && !var6.honey()
            && (!var6.farmland() && var6.top16() >= 16 || var1.repairFarmland() && var6.farmland() && var6.top16() == 15 || var6.soulSand())
            && !var6.door()
            && !var5.web()
            && !var7.web()
            && !var2.contains(var3.above().asLong())
            && !var0.probePos(var3.above()).hazardFeet()) {
            var var51 = collectPillarBreakCells(var0, var1, var3);
            if (var51 != null) {
               double var55 = var1.strategy() == AiPathfinder.RouteStrategy.SAVER ? 24.0 : 12.0;
               double var62 = 0.0;
               double var68 = 0.0;

               for (BlockPos var76 : var51) {
                  AiPathfinder.CellInfo var84 = var0.probePos(var76);
                  var62 += var84.breakTicks() + var1.breakReluctance() + var84.destructionPenalty();
                  var68 += var84.breakTicks();
               }

               if (var1.strategy() == AiPathfinder.RouteStrategy.SAVER) {
                  var62 *= 2.0;
               }

               var4.add(
                  new AiPathfinder.PathStep(
                     AiPathfinder.MoveType.PILLAR,
                     var3,
                     var3.above(),
                     var51,
                     List.of(var3),
                     var55 + var62,
                     var51.isEmpty() ? -1.0 : var68,
                     null,
                     var1.repairFarmland() && var6.farmland()
                  )
               );
            }
         }

         BlockPos var52 = var3.below();
         AiPathfinder.CellInfo var56 = var0.probePos(var52);
         boolean var59 = var56.passable() && !var56.water() && !var56.lava() && !var56.hazardFeet();
         if (!isMoveTypeDisabled(var1, AiPathfinder.MoveType.DIG_DOWN_1)
            && (var59 || var1.allowDig() && var56.diggable())
            && !var2.contains(var52.asLong())
            && !var5.lava()
            && (!var5.water() || !var0.probePos(var3.above()).water())
            && !var0.probePos(var3.above(2)).falling()) {
            AiPathfinder.CellInfo var63 = var0.probePos(var52.below());
            boolean var66 = var63.loaded()
               && !var63.passable()
               && !var63.hazardFloor()
               && !var63.lava()
               && !var63.water()
               && !var63.farmland()
               && var63.top16() <= 16;
            if (var66) {
               double var69 = var59 ? 0.0 : var56.breakTicks() + var1.breakReluctance() + var56.destructionPenalty();
               double var73 = var59 ? 0.0 : var56.breakTicks();
               ArrayList var85 = new ArrayList(3);
               var85.add(var52);
               boolean var92 = true;

               for (BlockPos var115 : new BlockPos[]{var3.above(), var3}) {
                  AiPathfinder.CellInfo var119 = var0.probePos(var115);
                  if (var119.passable() && !var119.water() && !var119.lava()) {
                     short var126 = var0.fill_S(var115.getX(), var115.getY(), var115.getZ());
                     if (var126 == -1) {
                        var0.queryFlags |= 1;
                        var92 = false;
                        break;
                     }

                     if (var126 != 0) {
                        if (!var1.allowDig() || var119.breakTicks() >= 1.0 / 0.0) {
                           var92 = false;
                           break;
                        }

                        var85.add(var115);
                        var69 += var119.breakTicks() + var1.breakReluctance() + var119.destructionPenalty();
                        var73 += var119.breakTicks();
                     }
                  }
               }

               if (var92) {
                  if (var1.strategy() == AiPathfinder.RouteStrategy.SAVER) {
                     var69 *= 2.0;
                  }

                  var4.add(
                     new AiPathfinder.PathStep(
                        AiPathfinder.MoveType.DIG_DOWN_1,
                        var3,
                        var52,
                        var85,
                        List.of(),
                        var59 && var85.size() == 1 ? AiPathfinder.MoveCosts.descendCost(1) : AiPathfinder.MoveCosts.digDownCost(var69),
                        var73
                     )
                  );
               }
            }
         }

         return var0.queryFlags;
      }

      static List<BlockPos> collectPillarBreakCells(Function<BlockPos, AiPathfinder.CellInfo> var0, AiPathfinder.PathOptions var1, BlockPos var2) {
         BlockPos var3 = var2.above(2);
         BlockPos var4 = var2.above(3);
         AiPathfinder.CellInfo var5 = (AiPathfinder.CellInfo)var0.apply(var3);
         AiPathfinder.CellInfo var6 = (AiPathfinder.CellInfo)var0.apply(var4);
         if (var5.loaded() && !var5.lava() && !var5.water() && !var5.hazardFeet() && !var5.web()) {
            ArrayList<BlockPos> var7 = new ArrayList<>(2);
            if (!var5.passable()) {
               if (!var1.allowDig() || !var5.diggable() || var6.falling() || var6.lava() || var6.water()) {
                  return null;
               }

               var7.add(var3);
            }

            if (var6.loaded() && !var6.lava() && !var6.hazardFeet()) {
               if (!var6.passable() && var1.allowDig() && var6.diggable()) {
                  AiPathfinder.CellInfo var8 = (AiPathfinder.CellInfo)var0.apply(var4.above());
                  if (!var8.falling() && !var8.lava() && !var8.water()) {
                     var7.add(var4);
                  }
               }

               return var7;
            } else {
               return null;
            }
         } else {
            return null;
         }
      }

      static boolean isPassableOrDiggableDry(AiPathfinder.CellInfo var0) {
         if (var0.loaded() && !var0.lava() && !var0.hazardFeet()) {
            return var0.passable() ? !var0.water() : var0.diggable();
         } else {
            return false;
         }
      }

      private static void generateLadderSideJumps(
         AiPathfinder.MoveGenerator.CachedGrid var0, Set<Long> var1, BlockPos var2, boolean var3, List<AiPathfinder.PathStep> var4
      ) {
         boolean var5 = var0.probePos(var2.above()).climbable();

         for (int[] var9 : EIGHT_DIRS) {
            for (int var10 = 1; var10 <= 2; var10++) {
               int var11 = var9[0] * var10;
               int var12 = var9[1] * var10;
               float var13 = AiPhysics.yawBetween(0.0, 0.0, var11, var12);

               for (int var14 = var5 ? 0 : 2; var14 >= -2; var14--) {
                  BlockPos var15 = var2.offset(var11, var14, var12);
                  if (!var1.contains(var15.asLong())) {
                     AiPathfinder.CellInfo var16 = var0.probePos(var15.below());
                     if (isStandable(var0.probePos(var15), var0.probePos(var15.above()), var16)
                        && !var16.farmland()
                        && !var16.slime()) {
                        double var17 = surfaceY(var15.getY(), var16);

                        for (double var22 : new double[]{0.35, 0.65, 0.85}) {
                           AiPhysics.MotionState var24 = new AiPhysics.MotionState(
                              var2.getX() + 0.5, var2.getY() + var22, var2.getZ() + 0.5, 0.0, 0.1176, 0.0, false, 0, false, false
                           );
                           AiJumpSolver.JumpPlan var25 = AiLadderJump.putAt_IoUtil_TextState_TokenCore_TaskStat(var0.plannerWorld(), var24, var13, var15, var17);
                           if (var25 != null) {
                              var4.add(
                                 new AiPathfinder.PathStep(
                                    AiPathfinder.MoveType.LADDER_JUMP, var2, var15, List.of(), List.of(), var25.ticks() + 5.5, -1.0, var25
                                 )
                              );
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }

         if (!var5 && var0.putAt_S_I(var2.getX(), var2.getY(), var2.getZ()) != 0) {
            AiPlannerWorld var26 = var0.plannerWorld();
            AiPhysics.MotionState var27 = new AiPhysics.MotionState(
               var2.getX() + 0.5, var2.getY() + 0.7, var2.getZ() + 0.5, 0.0, 0.1176, 0.0, false, 0, false, false
            );
            AiLadderJump.Inner_CacheHelper var28 = AiLadderJump.putAt_TableCore_CacheHel(var26, var27, var2);
            if (var28 != null) {
               for (AiPathfinder.MoveGenerator.JumpOffset var32 : BASE_JUMP_OFFSETS) {
                  if (var32.distance() > 2 && !(var32.length() > 5.2)) {
                     for (int var33 = 2; var33 >= -2; var33--) {
                        BlockPos var34 = var2.offset(var32.x(), var33, var32.z());
                        AiPathfinder.CellInfo var35 = var0.probePos(var34.below());
                        AiPathfinder.CellInfo var36 = var0.probePos(var34);
                        if (!var36.climbable()
                           && !var35.slime()
                           && !var35.farmland()
                           && !var1.contains(var34.asLong())
                           && isStandable(var36, var0.probePos(var34.above()), var35)) {
                           AiJumpSolver.JumpPlan var37 = AiLadderJump.putAt_IoUtil_TextState_TokenCore_TaskStat(
                              var26, var27, var28, var34, surfaceY(var34.getY(), var35), var3
                           );
                           if (var37 != null) {
                              var4.add(
                                 new AiPathfinder.PathStep(
                                    AiPathfinder.MoveType.LADDER_JUMP, var2, var34, List.of(), List.of(), var37.ticks() + 5.5, -1.0, var37
                                 )
                              );
                           }
                           break;
                        }
                     }
                  }
               }
            }
         }
      }

      private static void generateClimbTargetJumps(
         AiPathfinder.MoveGenerator.CachedGrid var0,
         Set<Long> var1,
         BlockPos var2,
         double var3,
         int var5,
         boolean var6,
         boolean var7,
         List<AiPathfinder.PathStep> var8
      ) {
         AiPlannerWorld var9 = var0.plannerWorld();
         AiPhysics.MotionState var10 = var6
            ? new AiPhysics.MotionState(var2.getX() + 0.5, var3, var2.getZ() + 0.5, 0.0, 0.1176, 0.0, false, 0, false, false)
            : AiPhysics.MotionState.standing(var2.getX() + 0.5, var3, var2.getZ() + 0.5);
         if (var6 || AiJumpSolver.canStandAt(var9, var10.x(), var3, var10.z())) {
            boolean var11 = !var6 && var0.fallPolicy.allowsExtendedDrop();
            AiPathfinder.MoveGenerator.JumpOffset[] var12 = var11 ? BOUNDED_DROP_JUMP_OFFSETS : BASE_JUMP_OFFSETS;
            double var13 = var6 ? (var0.probePos(var2.above()).climbable() ? 3.8 : 5.2) : (var11 ? 7.3 : 6.1);

            for (AiPathfinder.MoveGenerator.JumpOffset var18 : var12) {
               if ((var5 & var18.exits()) != 0 && !(var18.length() > var13)) {
                  for (int var19 = 1; var19 >= -4; var19--) {
                     BlockPos var20 = var2.offset(var18.x(), var19, var18.z());
                     AiPathfinder.CellInfo var21 = var0.probePos(var20);
                     if (var21.climbable() && !var21.hazardFeet() && !var21.water() && !var1.contains(var20.asLong())) {
                        if (var18.distance() <= 2 && hasClimbBaseSupport(var0, var20, var3)) {
                           break;
                        }

                        AiJumpSolver.JumpPlan var22 = var6
                           ? AiLadderJump.putAt_IoUtil_TextState_TokenCore_TaskStat_2(var9, var10, var2, var20, var7)
                           : AiLadderJump.putAt_IoUtil_TextState_TokenCore_TaskStat(var9, var10, var20, var7);
                        if (var22 != null) {
                           BlockPos var24 = BlockPos.containing(var22.landX(), var22.landY(), var22.landZ());
                           if (!var1.contains(var24.asLong())) {
                              var8.add(
                                 new AiPathfinder.PathStep(
                                    AiPathfinder.MoveType.LADDER_JUMP, var2, var24, List.of(), List.of(), var22.ticks() + 2.0, -1.0, var22
                                 )
                              );
                           }
                           break;
                        }

                        int var23 = Math.max(-4, var19 - 4);

                        while (var19 > var23 && var0.probePos(var2.offset(var18.x(), var19 - 1, var18.z())).climbable()) {
                           var19--;
                        }
                     }
                  }
               }
            }
         }
      }

      private static boolean hasClimbBaseSupport(AiPathfinder.MoveGenerator.CachedGrid var0, BlockPos var1, double var2) {
         for (BlockPos var4 = var1; var4.getY() >= Math.floor(var2) - 1.0; var4 = var4.below()) {
            if (!var0.probePos(var4.below()).climbable()) {
               AiPathfinder.CellInfo var5 = var0.probePos(var4.below());
               return var4.getY() - var2 <= 1.25 && var5.loaded() && var5.top16() >= 8 && !var5.passable() && !var5.hazardFloor();
            }
         }

         return false;
      }

      private static void generateSlimeBounceMoves(
         AiPathfinder.MoveGenerator.CachedGrid var0,
         AiPathfinder.PathOptions var1,
         Set<Long> var2,
         BlockPos var3,
         double var4,
         List<AiPathfinder.PathStep> var6
      ) {
         boolean var7 = !var0.hasActiveEdits();
         List var8 = var7 ? var0.slimeBounceCache.get(var3) : null;
         if (var8 == null) {
            AiPlannerWorld var9 = var0.plannerWorld();
            long var10 = var9.putAt_J;
            var8 = solveSlimeBounces(var0, var1, var3, var4);
            if (var7 && var9.putAt_J == var10 && (var0.queryFlags & 1) == 0) {
               var0.slimeBounceCache.put(var3, var8);
            }
         }

         for (AiPathfinder.PathStep var13 : (Iterable<AiPathfinder.PathStep>)(Iterable<?>)(var8)) {
            if (!var2.contains(var13.to().asLong())) {
               var6.add(var13);
            }
         }
      }

      private static List<AiPathfinder.PathStep> solveSlimeBounces(
         AiPathfinder.MoveGenerator.CachedGrid var0, AiPathfinder.PathOptions var1, BlockPos var2, double var3
      ) {
         ArrayList<AiPathfinder.PathStep> var5 = new ArrayList<>();
         boolean var6 = var1.canSprint() && var1.strategy() != AiPathfinder.RouteStrategy.SAVER;

         for (int[] var10 : EIGHT_DIRS) {
            int var11 = var10[0];
            int var12 = var10[1];
            if (var0.probePos(var2.offset(var11, -1, var12)).passable()
               && var0.probePos(var2.offset(var11, 0, var12)).passable()
               && hasSlimeLandingAhead(var0, var2, var11, var12)) {
               AiPlannerWorld var13 = var0.plannerWorld();
               float var14 = AiPhysics.yawBetween(0.0, 0.0, var11, var12);
               double[][] var15 = new double[][]{{0.0, 0.0}, {0.0, var12 * 0.35}, {var11 * 0.35, 0.0}, {var11 * 0.35, var12 * 0.35}};

               for (int var16 = 0; var16 < var15.length; var16++) {
                  double[] var17 = var15[var16];
                  if (var16 <= 0 || var17[0] != 0.0 || var17[1] != 0.0) {
                     AiPhysics.MotionState var18 = AiPhysics.MotionState.standing(var2.getX() + 0.5 + var17[0], var3, var2.getZ() + 0.5 + var17[1]);
                     if (AiJumpSolver.canStandAt(var13, var18.x(), var18.y(), var18.z())) {
                        var var19 = AiSlimeBounce.findBounces(var13, var18, var14, null, var6);

                        for (AiSlimeBounce.Inner_JsonCache var21 : var19) {
                           if (!var0.probePos(var21.cell().below()).farmland()) {
                              AiJumpSolver.JumpPlan var22 = var21.plan();
                              var5.add(
                                 new AiPathfinder.PathStep(
                                    AiPathfinder.MoveType.SLIME_BOUNCE,
                                    var2,
                                    var21.cell(),
                                    List.of(),
                                    List.of(),
                                    var22.ticks() + 2.0 + Math.hypot(var17[0], var17[1]) * 10.0,
                                    -1.0,
                                    var22
                                 )
                              );
                           }
                        }

                        if (!var19.isEmpty()) {
                           break;
                        }
                     }
                  }
               }
            }
         }

         return var5;
      }

      private static boolean hasSlimeLandingAhead(AiPathfinder.MoveGenerator.CachedGrid var0, BlockPos var1, int var2, int var3) {
         boolean var4 = var0.delegate instanceof AiLiveGrid && !var0.hasActiveEdits();
         int var5 = var1.getY() - 24 - 1;

         for (int var6 = 2; var6 <= 8; var6++) {
            int var7 = var1.getX() + var2 * var6;
            int var8 = var1.getZ() + var3 * var6;

            for (int var9 = var1.getY() - 4; var9 >= var5; var9--) {
               if (var4) {
                  var9 = var0.findHighestSolidBelow(var7, var9, var8, var5);
                  if (var9 < var5) {
                     break;
                  }
               }

               AiPathfinder.CellInfo var10 = var0.probe(var7, var9, var8);
               if (!var10.loaded()) {
                  break;
               }

               if (var10.slime()) {
                  return true;
               }

               if (var10.top16() >= 16 && !var10.passable()) {
                  break;
               }
            }
         }

         return false;
      }

      private static AiPathfinder.MoveGenerator.JumpOffset[] buildJumpOffsets(int var0) {
         ArrayList<AiPathfinder.MoveGenerator.JumpOffset> var1 = new ArrayList<>();

         for (int var2 = -var0; var2 <= var0; var2++) {
            for (int var3 = -var0; var3 <= var0; var3++) {
               int var4 = Math.max(Math.abs(var2), Math.abs(var3));
               if (var4 != 0) {
                  double var5 = Math.hypot(var2, var3);
                  int var7 = (int)Math.round((double)var2 / var4);
                  int var8 = (int)Math.round((double)var3 / var4);
                  int var9 = 1 << eightDirIndex(var7, var8);

                  for (int var10 = 0; var10 < EIGHT_DIRS.length; var10++) {
                     int[] var11 = EIGHT_DIRS[var10];
                     if ((var11[0] * var2 + var11[1] * var3) / (Math.hypot(var11[0], var11[1]) * var5) >= 0.5) {
                        var9 |= 1 << var10;
                     }
                  }

                  double var14 = 0.5 * var5 / var4;
                  double var12 = 0.5 * (Math.abs(var2) + Math.abs(var3)) / var5;
                  var1.add(
                     new AiPathfinder.MoveGenerator.JumpOffset(
                        var2, var3, var4, var7, var8, var5, var5 - 2.0 * (var14 - 0.5), var5 - 2.0 * (var12 - 0.5), var9
                     )
                  );
               }
            }
         }

         return var1.toArray(AiPathfinder.MoveGenerator.JumpOffset[]::new);
      }

      static boolean canTakeoffFrom(AiPathfinder.CellInfo var0, AiPathfinder.CellInfo var1) {
         return var0.loaded() && !var0.water() && !var0.lava() && !var0.climbable() && !var0.hazardFeet() && !var0.web()
            ? var1.loaded() && !var1.passable() && !var1.floorContactHazard() && !var1.lava() && !var1.water()
            : false;
      }

      private static void generateParkourMoves(
         AiPathfinder.MoveGenerator.CachedGrid var0,
         AiPathfinder.PathOptions var1,
         Set<Long> var2,
         BlockPos var3,
         AiPathfinder.CellInfo var4,
         double var5,
         List<AiPathfinder.PathStep> var7
      ) {
         boolean var8 = var0.isJumpBudgetExhausted();
         if (!var8 || !var0.isJumpBudgetExceededAt(var3)) {
            boolean var9 = var1.canSprint() && var1.strategy() != AiPathfinder.RouteStrategy.SAVER;
            boolean var10 = var0.fullPhysicsParkour
               && (
                  var4.narrowFooting()
                     || var4.top16() > 16
                     || var5 != surfaceY(var3.getY(), var4)
                     || !isStandable(var0.probePos(var3), var0.probePos(var3.above()), var4)
               );
            double var11 = var4.ice() ? 0.0 : incomingApproachSpeed(var0, var3, var5, var9);
            double var13 = 0.0;
            double var15 = 0.0;
            if (var11 > 0.0) {
               var13 = var0.incomingMove.to().getX() - var0.incomingMove.from().getX();
               var15 = var0.incomingMove.to().getZ() - var0.incomingMove.from().getZ();
               double var17 = Math.hypot(var13, var15);
               if (var17 < 1.0E-9) {
                  var11 = 0.0;
               } else {
                  var13 /= var17;
                  var15 /= var17;
               }
            }

            int var72 = 0;

            for (int var18 = 0; var18 < EIGHT_DIRS.length; var18++) {
               BlockPos var19 = var3.offset(EIGHT_DIRS[var18][0], 0, EIGHT_DIRS[var18][1]);
               if (!isFlatWalkableCell(var0, var19, var5) && isJumpClearCell(var0, var19) && isJumpClearCell(var0, var19.above())) {
                  var72 |= 1 << var18;
               }
            }

            if (var72 != 0 && (var8 || !var0.isJumpBudgetExceededAt(var3))) {
               if (var0.fullPhysicsParkour && !isMoveTypeDisabled(var1, AiPathfinder.MoveType.LADDER_JUMP)) {
                  generateClimbTargetJumps(var0, var2, var3, var5, var72, false, var9, var7);
               }

               boolean var73 = var0.fallPolicy.allowsExtendedDrop();
               boolean var74 = var73 && var0.delegate instanceof AiLiveGrid && !var0.hasActiveEdits();
               double var20 = 2.0 * Math.max(0.0, var0.movementSpeed / 0.1 - 1.0);
               int var22 = 5 + Math.min(2, (int)Math.ceil(var20 - 1.0E-5));
               AiPathfinder.MoveGenerator.JumpOffset[] var23 = var73
                  ? DROP_JUMP_OFFSETS
                  : (var22 > 5 ? SPEED_JUMP_OFFSETS : BASE_JUMP_OFFSETS);
               int var24 = var73 ? -((int)var0.fallPolicy.maxDropBlocks) : -4;
               if (var73 && var0.limitJumpDropDepth) {
                  var24 = -8;
               }

               if (var73 && var0.quickJumpMode) {
                  var23 = BOUNDED_DROP_JUMP_OFFSETS;
                  var24 = Math.max(var24, -14);
               }

               int var25 = (int)Math.ceil(var5 + 1.25) - var3.getY();

               for (AiPathfinder.MoveGenerator.JumpOffset var29 : var23) {
                  int var30 = var29.x();
                  int var31 = var29.z();
                  int var32 = var29.distance();
                  if ((var73 || var32 <= var22) && ((var72 & var29.exits()) != 0 || var32 < 2)) {
                     double var33 = var29.length();
                     double var35 = var0.fullPhysicsParkour ? var29.cornerReach() : var29.reach();
                     int var37 = var32 <= var22 ? var25 : -Math.max(3, (int)Math.floor((var35 - 5.2 - var20) / 0.35));

                     for (int var38 = var37; var38 >= var24; var38--) {
                        if (var74 || var73 && var38 < -3) {
                           int var39 = var3.getY() - 1;
                           var38 = var0.findHighestSolidBelow(var3.getX() + var30, var39 + var38, var3.getZ() + var31, var39 + var24) - var39;
                           if (var38 < var24) {
                              break;
                           }
                        }

                        BlockPos var75 = var3.offset(var30, var38, var31);
                        AiPathfinder.CellInfo var40 = var0.probePos(var75.below());
                        if (var40.loaded() && !var40.passable()) {
                           boolean var41 = (var40.top16() > 16 || var0.fullPhysicsParkour) && isTightLandingStandable(var0, var75, null);
                           if (var32 == 1 && !var41 && !var10 && (var30 == 0 || var31 == 0 || !hasNonFlatGapOnPath(var0, var3, var75, var5))) {
                              break;
                           }

                           AiPathfinder.CellInfo var42 = var0.probePos(var75);
                           if (!var2.contains(var75.asLong())
                              && !var40.farmland()
                              && !var40.floorContactHazard()
                              && !var40.lava()
                              && !var42.water()
                              && !var42.climbable()
                              && !var42.web()
                              && !var42.portal()
                              && (var41 || isStandable(var42, var0.probePos(var75.above()), var40))
                              && !var0.probePos(var75.above()).web()) {
                              double var43 = effectiveSurfaceY(var0, var75, var40);
                              double var45 = var43 - var5;
                              if (var45 > 1.25) {
                                 if (var0.putAt_S_I(var75.getX(), var75.getY() - 1, var75.getZ()) == 1) {
                                    break;
                                 }
                              } else {
                                 if (var45 < (var0.fallPolicy.allowsExtendedDrop() ? -var0.fallPolicy.maxDropBlocks : -3.6)) {
                                    break;
                                 }

                                 double var47 = var0.fullPhysicsParkour && var9 ? 4.2 : 4.1;
                                 double var49 = var0.fullPhysicsParkour && var45 <= -3.5 ? 0.45 : 0.35;
                                 if (var35 > (var45 > 0.6 ? var47 : (var45 > 0.0 ? 4.6 : 5.2)) + var20 + var49 * Math.max(0.0, -var45) + 1.0E-6) {
                                    break;
                                 }

                                 BlockPos var51 = var75.offset(-var29.stepX(), 0, -var29.stepZ());
                                 if (Math.max(Math.abs(var51.getX() - var3.getX()), Math.abs(var51.getZ() - var3.getZ())) >= 2
                                    && isFlatWalkableCell(var0, var51, var43)
                                    && (!var0.allowDeepPlatformLanding || isFlatWalkableCell(var0, var51.offset(-var29.stepX(), 0, -var29.stepZ()), var43))) {
                                    break;
                                 }

                                 double var52 = var11 <= 0.0 ? 0.0 : (var13 * var30 + var15 * var31) / var33;
                                 double var54 = var52 >= 0.65 ? var11 * var52 : 0.0;
                                 if (var54 < 0.003) {
                                    var54 = 0.0;
                                 }

                                 boolean var56 = var54 > 0.0 && var0.incomingMove != null && var0.incomingMove.jump() != null;
                                 double var57 = var35 + (var54 > 0.0 ? 0.2 : 0.0);
                                 double var59 = var57 - 0.5 + 0.3 + -0.12;
                                 double var61 = var57 + 0.5 - 0.3 - -0.12;
                                 double var63 = AiJumpSolver.maxJumpReach(var45, var9, var54 > 0.0, var0.movementSpeed);
                                 double var65 = var54 > 0.0 ? AiJumpSolver.maxJumpReach(var45, var9, false, var0.movementSpeed) : var63;
                                 double var67 = Math.max(var63 < 0.0 ? -1.0 : var63 + (var54 > 0.0 ? 0.25 : 1.0), var65 < 0.0 ? -1.0 : var65 + 1.0);
                                 if (var4.ice() && var0.fullPhysicsParkour && var32 >= 4 && var9) {
                                    var67 = Math.max(var67, var63 + 1.5);
                                 }

                                 if (var67 < 0.0 || var59 > var67 || var32 >= 2 && var61 < AiJumpSolver.standingJumpReach(var45) - 0.25) {
                                    break;
                                 }

                                 AiPathfinder.MoveGenerator.JumpCandidate var69 = new AiPathfinder.MoveGenerator.JumpCandidate(
                                    var3, var75, var5, var43, var54, var9, var29.stepX(), var29.stepZ(), var56
                                 );
                                 if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
                                    AiNavDebug.log("jump candidate from=" + var3 + " to=" + var75 + " approach=" + var54);
                                 }

                                 if (var0.deferredJumpSink != null) {
                                    var0.deferredJumpSink.accept(var69);
                                 } else {
                                    AiPathfinder.PathStep var70 = solveJumpCandidate(var0, var2, var69);
                                    if (var70 != null) {
                                       var7.add(var70);
                                       AiPathfinder.PathStep var71 = tryDoubleJumpChain(var0, var70, var2);
                                       if (var71 != null) {
                                          var7.add(var71);
                                       }
                                    }
                                 }

                                 if (!var0.fullPhysicsParkour
                                    || var40.slabStair()
                                    || var0.putAt_S_I(var75.getX(), var75.getY() - 1, var75.getZ()) == 1
                                    || var0.probePos(var75.below(2)).passable()) {
                                    break;
                                 }
                              }
                           } else if (!var0.fullPhysicsParkour) {
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      private static void generateWallJumpMoves(
         AiPathfinder.MoveGenerator.CachedGrid var0, Set<Long> var1, BlockPos var2, double var3, List<AiPathfinder.PathStep> var5
      ) {
         for (int[] var9 : CARDINAL_DIRS) {
            int var10 = var9[0];
            int var11 = var9[1];

            for (int var12 = 2; var12 <= 4; var12++) {
               BlockPos var13 = var2.offset(var10 * var12, 0, var11 * var12);
               if (!var0.probePos(var13.offset(var10, 0, var11)).passable() && isWallJumpLandingCell(var0, var1, var13)) {
                  ArrayList<BlockPos> var14 = new ArrayList<>();
                  var14.add(var13);

                  for (int var15 = 0;
                     var15 < 2
                        && isTwoHighFullWall(var0, var13.offset(var10, 0, var11))
                        && isTwoHighFullWall(var0, var13.offset(2 * var10, 0, 2 * var11))
                        && isTwoHighFullWall(var0, var13.offset(3 * var10, 0, 3 * var11));
                     var15++
                  ) {
                     BlockPos var16 = var13.offset(4 * var10, 0, 4 * var11);
                     if (!isWallJumpLandingCell(var0, var1, var16)) {
                        break;
                     }

                     var14.add(var16);
                     var13 = var16;
                  }

                  if (var14.size() != 1) {
                     AiJumpSolver.JumpPlan var17 = AiWallJump.putAt(var0.plannerWorld(), var2, var3, var14);
                     if (var17 != null && !(var17.maneuver().replay(var0.plannerWorld(), var17.maneuver().start(), 0, var13, var3) < -0.04)) {
                        var5.add(AiPathfinder.PathStep.jump(var2, var13, AiPathfinder.MoveCosts.jumpPlanCost(var17), var17));
                     }
                  }
               }
            }
         }
      }

      private static boolean isWallJumpLandingCell(AiPathfinder.MoveGenerator.CachedGrid var0, Set<Long> var1, BlockPos var2) {
         if (!var1.contains(var2.asLong()) && var0.probePos(var2).passable()) {
            AiPathfinder.CellInfo var3 = var0.probePos(var2.below());
            return var3.top16() == 16
               && !var3.farmland()
               && !var3.slime()
               && !var3.ice()
               && isStandable(var0.probePos(var2), var0.probePos(var2.above()), var3);
         } else {
            return false;
         }
      }

      private static boolean isTwoHighFullWall(AiPathfinder.MoveGenerator.CachedGrid var0, BlockPos var1) {
         return var0.putAt_S_I(var1.getX(), var1.getY(), var1.getZ()) == 1 && var0.putAt_S_I(var1.getX(), var1.getY() + 1, var1.getZ()) == 1;
      }

      private static boolean isJumpClearCell(AiPathfinder.MoveGenerator.CachedGrid var0, BlockPos var1) {
         AiPathfinder.CellInfo var2 = var0.probePos(var1);
         return var2.passable()
            ? true
            : var0.fullPhysicsParkour
               && var2.loaded()
               && !var2.hazardFeet()
               && !var2.lava()
               && !var2.water()
               && var0.putAt_S_I(var1.getX(), var1.getY(), var1.getZ()) != 1;
      }

      private static AiPathfinder.PathStep solveJumpCandidate(
         AiPathfinder.MoveGenerator.CachedGrid var0, Set<Long> var1, AiPathfinder.MoveGenerator.JumpCandidate var2
      ) {
         BlockPos var3 = var2.from();
         BlockPos var4 = var2.to();
         double var5 = var2.targetY();
         if (var0.fullPhysicsParkour
            && var2.sprint()
            && var0.probePos(var3.below()).ice()
            && var3.distToCenterSqr(var4.getX() + 0.5, var3.getY() + 0.5, var4.getZ() + 0.5) >= 16.0) {
            AiJumpSolver.JumpPlan var7 = AiJumpSolver.solveIceRunupJump(var0.plannerWorld(), var3, var2.sourceY(), var4, var5, true);
            if (var7 != null && !hasLavaUnderJump(var0, var3, var4) && (!AiPathfinder.MoveCosts.isCautiousParkourActive() || !hasDeepDropUnderJump(var0, var3, var4))) {
               return AiPathfinder.PathStep.jump(var3, var4, AiPathfinder.MoveCosts.jumpPlanCost(var7), var7);
            }
         }

         AiJumpSolver.JumpPlan var25 = var0.solveJumpCached(var3, var2.sourceY(), var4, var5, var2.approach(), var2.sprint());
         BlockPos var8 = var4;
         if (var25 == null
            && var0.fullPhysicsParkour
            && var2.sprint()
            && var3.distSqr(var8) >= 9.0
            && var3.distSqr(var8) <= 29.0
            && var0.probePos(var3.below()).top16() <= 3) {
            var25 = var0.cachedJumpSolve(
               var3,
               var8,
               -9223372036854775807L,
               true,
               () -> AiJumpSolver.solveEdgeSetupJump(var0.plannerWorld(), var3, var2.sourceY(), var8, var5)
            );
         }

         if (var25 == null
            && var0.fullPhysicsParkour
            && var3.distSqr(var8) <= 18.0
            && (
               !var0.probePos(var8.above()).passable()
                  || var0.probePos(var8.below()).slabStair() && !var0.probePos(var8.above(2)).passable()
            )) {
            var25 = var0.cachedJumpSolve(
               var3,
               var8,
               -9223372036854775806L,
               var2.sprint(),
               () -> AiJumpManeuver.roofedLanding(
                  var0.plannerWorld(), var3, var2.sourceY(), var8, var5, var2.sprint(), var0.probePos(var3.below()).slabStair()
               )
            );
         }

         if (var25 == null
            && var0.fullPhysicsParkour
            && var3.distSqr(var8) <= 18.0
            && var0.probePos(var3.below()).slabStair()
            && (
               !var0.probePos(var3.offset(var2.dx(), 0, var2.dz())).passable()
                     && (!var0.probePos(var8).passable() || !var0.probePos(var8.above()).passable())
                  || !var0.probePos(var3.above()).passable() && var5 > var2.sourceY() + 0.4
            )) {
            var25 = var0.cachedJumpSolve(
               var3,
               var8,
               -9223372036854775805L,
               var2.sprint(),
               () -> AiJumpManeuver.cornerDeparture(var0.plannerWorld(), var3, var2.sourceY(), var8, var5, var2.sprint())
            );
         }

         boolean var9 = var25 != null;
         boolean var10 = false;
         if (var0.fullPhysicsParkour && !var0.fastPassMode && var2.sprint() && Math.abs(var2.sourceY() - var5) <= 1.0E-6 && isNeoWallCandidate(var0, var3, var4)) {
            AiJumpSolver.JumpPlan var11 = AiWallJump.putAt_IoUtil_TextState_TokenCore_TaskStat_3(var0.plannerWorld(), var3, var5, var4, var2.approach());
            if (var11 != null
               && (var25 == null || var11.ticks() <= var25.ticks() + 4)
               && var11.maneuver().replay(var0.plannerWorld(), var11.maneuver().start(), 0, var4, var5) >= -0.04) {
               var25 = var11;
               var9 = false;
               var10 = true;
               if (AiNavDebug.isEnabled()) {
                  AiNavDebug.log("proved sprint shoulder from=" + var3.toShortString() + " to=" + var4.toShortString() + " ticks=" + var11.ticks());
               }
            }
         }

         double var26 = 0.0;
         if (var2.landedAhead()
            && var25 != null
            && var25.maneuver() == null
            && var0.incomingMove != null
            && var0.incomingMove.jump() != null) {
            AiJumpSolver.JumpPlan var13 = var0.incomingMove.jump();
            double var14 = Math.toRadians(var13.yaw());
            AiPhysics.MotionState var16 = new AiPhysics.MotionState(
               var13.landX(),
               var2.sourceY(),
               var13.landZ(),
               -Math.sin(var14) * var13.landSpeed(),
               -0.0784,
               Math.cos(var14) * var13.landSpeed(),
               true,
               0,
               false,
               false
            );
            if (AiJumpSolver.solveTakeoff(var0.plannerWorld(), var16, var25.yaw(), var2.sprint(), var4, var5, 6).margin() < -0.04) {
               var25 = null;
            }
         }

         if (var9 && var2.approach() > 0.0 && (var25 == null || var25.maneuver() == null && var25.margin() < 0.14)) {
            AiJumpSolver.JumpPlan var27 = var0.solveJumpCached(var3, var2.sourceY(), var4, var5, 0.0, var2.sprint());
            if (var27 != null && var27.margin() >= -0.04) {
               boolean var30 = var25 == null
                  || var25.margin() < -0.04
                  || var27.margin() > var25.margin() + 0.04
                  || var27.margin() >= var25.margin() && var27.ticks() + 4 < var25.ticks();
               if (var30) {
                  var25 = var27;
                  var26 = 4.0;
               }
            }
         }

         if (var25 != null && var2.approach() > 0.0 && !var25.running()) {
            var26 = Math.max(var26, 4.0);
         }

         if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
            AiNavDebug.log("jump proof from=" + var3 + " to=" + var4 + " plan=" + var25);
         }

         if (var25 == null) {
            return null;
         }

         if (var25.margin() < 0.1) {
            BlockPos var28 = var2.deeper();
            AiPathfinder.CellInfo var31 = var0.probePos(var28.below());
            boolean var15 = var25.margin() < -0.04
               || var2.dx() == 0
               || var2.dz() == 0
               || isFlatWalkableCell(var0, var4.offset(var2.dx(), 0, 0), var5)
                  && Math.abs(surfaceY(var4.getY(), var0.probePos(var4.offset(var2.dx(), -1, 0))) - var5) < 1.0E-6
               || isFlatWalkableCell(var0, var4.offset(0, 0, var2.dz()), var5)
                  && Math.abs(surfaceY(var4.getY(), var0.probePos(var4.offset(0, -1, var2.dz()))) - var5) < 1.0E-6;
            if (var15 && !var1.contains(var28.asLong()) && isFlatWalkableCell(var0, var28, var5) && Math.abs(surfaceY(var28.getY(), var31) - var5) < 1.0E-6) {
               AiJumpSolver.JumpPlan var34 = var10
                  ? AiWallJump.putAt_IoUtil_TextState_TokenCore_TaskStat_3(var0.plannerWorld(), var3, var5, var28, var2.approach())
                  : var0.solveJumpCached(var3, var2.sourceY(), var28, var5, var25.approach(), var2.sprint());
               if (var34 != null && var34.margin() > var25.margin()) {
                  var25 = var34;
                  var4 = var28;
               }
            }
         }

         boolean var29 = hasDeepDropUnderJump(var0, var3, var4);
         boolean var32 = hasLavaUnderJump(var0, var3, var4);
         if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
            AiNavDebug.log(
               "jump decision from="
                  + var3
                  + " to="
                  + var4
                  + " margin="
                  + var25.margin()
                  + " hard="
                  + var29
                  + " cautious="
                  + AiPathfinder.MoveCosts.isCautiousParkourActive()
                  + " lava="
                  + var32
            );
         }

         if (!(var25.margin() < -0.04) && (!var29 || !AiPathfinder.MoveCosts.isCautiousParkourActive()) && !var32) {
            double var33 = var3.distToCenterSqr(var4.getX() + 0.5, var3.getY() + 0.5, var4.getZ() + 0.5);
            boolean var17 = var0.movementSpeed > 0.125;
            if (var17 && var0.fastPassMode && var25.maneuver() == null && var25.running() && var5 - var2.sourceY() > 0.6 && var33 >= 23.04 && var25.margin() < 0.16) {
               AiJumpSolver.JumpPlan var18 = var0.solveJumpCached(var3, var2.sourceY(), var4, var5, 0.0, var2.sprint());
               if (var18 == null || var18.margin() < -0.04) {
                  return null;
               }
            }

            boolean var35 = var17 ? var33 > 29.160000000000004 : var33 >= 20.25 && var5 - var2.sourceY() > 0.6;
            AiPathfinder.CellInfo var19 = var0.probePos(var3.below());
            boolean var20 = var25.runTicks() >= 3 && var33 >= 16.0 && var5 - var2.sourceY() >= 0.75 && (var19.narrowFooting() || var19.top16() > 16);
            if (var0.movementSpeed > 0.100001
               && var25.maneuver() == null
               && (var20 || var25.runTicks() >= 6 || var0.fastPassMode && var35)
               && !AiJumpSolver.verifyRunupPath(var0.plannerWorld(), var25, var2.sourceY())) {
               return null;
            }

            double var21 = var29 ? 20.0 : 0.0;
            if (var29 && (var0.fastPassMode || var0.fallPolicy != AiPathfinder.FallPolicy.STANDARD)) {
               double var23 = 0.12;
               var21 *= Math.clamp((var23 - var25.margin()) / (var23 - -0.04), 0.0, 1.0);
            }

            if (var0.fallPolicy != AiPathfinder.FallPolicy.STANDARD) {
               AiPathfinder.CellInfo var36 = var0.probePos(var4.below());
               if (var36.narrowFooting() || var36.top16() > 16) {
                  var26 += 3.0;
               }

               if (usesSneakManeuver(var25)) {
                  var26 += 3.0;
               }
            }

            double var37 = AiPathfinder.MoveCosts.jumpPlanCost(var25);
            if (var25.maneuver() != null && var25.backup() == 0.0 && var33 <= 2.5 && !usesSneakManeuver(var25)) {
               var37 -= 10.0;
            }

            return AiPathfinder.PathStep.jump(
               var3, var4, var37 + var26 + (var0.probePos(var4).thorny() ? 25.0 : 0.0) + var21, var25
            );
         } else {
            return null;
         }
      }

      static boolean isNeoWallCandidate(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, BlockPos var2) {
         int var3 = var2.getX() - var1.getX();
         int var4 = var2.getZ() - var1.getZ();
         int var5 = Math.abs(var3) + Math.abs(var4);
         if ((var3 == 0 || var4 == 0) && var5 >= 2 && var5 <= 4) {
            int var6 = Integer.signum(var3);
            int var7 = Integer.signum(var4);
            BlockPos var8 = var1.offset(var6, 0, var7);
            if (!((AiPathfinder.CellInfo)var0.apply(var8)).passable() && !((AiPathfinder.CellInfo)var0.apply(var8.above())).passable()) {
               BlockPos var9 = var1.offset(var6 * 3, 0, var7 * 3);
               return var5 < 4 || ((AiPathfinder.CellInfo)var0.apply(var9)).passable() && ((AiPathfinder.CellInfo)var0.apply(var9.above())).passable();
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private static AiPathfinder.PathStep tryDoubleJumpChain(
         AiPathfinder.MoveGenerator.CachedGrid var0, AiPathfinder.PathStep var1, Set<Long> var2
      ) {
         AiJumpSolver.JumpPlan var3 = var1.jump();
         if (var0.fullPhysicsParkour && !var3.running() && var3.maneuver() == null && var3.sprint() && var3.coastAfter() != 2147483647) {
            BlockPos var4 = var1.from();
            BlockPos var5 = var1.to();
            AiPathfinder.CellInfo var6 = var0.probePos(var5.below());
            if (var6.top16() == 16 && var0.putAt_S_I(var5.getX(), var5.getY() - 1, var5.getZ()) != 1) {
               int var7 = var5.getX() - var4.getX();
               int var8 = var5.getZ() - var4.getZ();
               if (Math.max(Math.abs(var7), Math.abs(var8)) < 3) {
                  return null;
               }

               int var9 = Math.abs(var7) > Math.abs(var8) ? Integer.signum(var7) : 0;
               int var10 = var9 == 0 ? Integer.signum(var8) : 0;
               if (isFlatWalkableCell(var0, var5.offset(var9, 0, var10), var3.landY())) {
                  return null;
               }

               double var11 = surfaceY(var4.getY(), var0.probePos(var4.below()));
               AiPlannerWorld var13 = var0.plannerWorld();
               AiJumpSolver.JumpPlan var14 = AiJumpSolver.solveUncoastedChainedJump(var13, var3, var5, var11, var3.landY());
               if (var14 == null) {
                  return null;
               }

               for (int var15 = 4; var15 <= 7; var15++) {
                  for (int var16 = -1; var16 <= 1; var16++) {
                     int var17 = var15 <= 5 ? -3 : -5;
                     int var18 = var15 <= 5 ? 0 : -3;

                     for (int var19 = var17; var19 <= var18; var19++) {
                        BlockPos var20 = var5.offset(var9 * var15 + var10 * var16, var19, var10 * var15 + var9 * var16);
                        if (!var2.contains(var20.asLong())) {
                           AiPathfinder.CellInfo var21 = var0.probePos(var20.below());
                           if ((
                                 isStandable(
                                       var0.probePos(var20), var0.probePos(var20.above()), var21
                                    )
                                    || isTightLandingStandable(var0, var20, null)
                              )
                              && !var21.farmland()
                              && !var21.slime()
                              && var0.putAt_S_I(var20.getX(), var20.getY() - 1, var20.getZ()) != 1) {
                              double var22 = surfaceY(var20.getY(), var21);
                              AiJumpSolver.JumpPlan var24 = AiJumpSolver.solveFollowUpJump(var13, var14, var20, var22, var14.sprint());
                              if (var24 != null
                                 && !(var24.margin() < 0.03)
                                 && !hasLavaUnderJump(var0, var5, var20)
                                 && (!AiPathfinder.MoveCosts.isCautiousParkourActive() || !hasDeepDropUnderJump(var0, var5, var20))) {
                                 var var25 = new ArrayList<>(var14.maneuver().inputs());
                                 var25.addAll(var24.maneuver().inputs());
                                 AiJumpManeuver var26 = new AiJumpManeuver(
                                    var25, var14.maneuver().start(), var24.maneuver().landing(), var24.margin(), var14.maneuver().takeoffTick()
                                 );
                                 if (!(var26.replay(var13, var26.start(), 0, var20, var22) < -0.04)) {
                                    AiJumpSolver.JumpPlan var27 = new AiJumpSolver.JumpPlan(
                                       var14.yaw(),
                                       var14.runTicks(),
                                       var14.sprint(),
                                       2147483647,
                                       var25.size() - var14.runTicks(),
                                       var3.startX(),
                                       var3.startZ(),
                                       var24.landX(),
                                       var22,
                                       var24.landZ(),
                                       var24.landSpeed(),
                                       var24.margin(),
                                       var3.backup(),
                                       0.0,
                                       var26
                                    );
                                    return AiPathfinder.PathStep.jump(
                                       var4, var20, var1.costTicks() + AiPathfinder.MoveCosts.jumpPlanCost(var24), var27
                                    );
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               return null;
            } else {
               return null;
            }
         } else {
            return null;
         }
      }

      private static boolean usesSneakManeuver(AiJumpSolver.JumpPlan var0) {
         return var0.maneuver() != null && var0.maneuver().inputs().stream().anyMatch(AiPhysics.MoveInput::sneak);
      }

      static boolean hasDeepDropUnderJump(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, BlockPos var2) {
         int var3 = var2.getX() - var1.getX();
         int var4 = var2.getZ() - var1.getZ();
         int var5 = Math.max(Math.abs(var3), Math.abs(var4));
         int var6 = Math.max(var1.getY(), var2.getY());

         for (int var7 = 1; var7 < var5; var7++) {
            BlockPos var8 = new BlockPos(
               var1.getX() + (int)Math.round((double)var3 * var7 / var5), var6, var1.getZ() + (int)Math.round((double)var4 * var7 / var5)
            );
            if (hasDeepAirDrop(var0, var8)) {
               return true;
            }
         }

         for (int[] var10 : EIGHT_DIRS) {
            if (hasDeepAirDrop(var0, var2.offset(var10[0], 0, var10[1]))) {
               return true;
            }
         }

         return false;
      }

      static boolean hasLavaUnderJump(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, BlockPos var2) {
         int var3 = var2.getX() - var1.getX();
         int var4 = var2.getZ() - var1.getZ();
         int var5 = Math.max(Math.abs(var3), Math.abs(var4));
         int var6 = Math.max(var1.getY(), var2.getY());

         for (int var7 = 1; var7 < var5; var7++) {
            BlockPos var8 = new BlockPos(
               var1.getX() + (int)Math.round((double)var3 * var7 / var5), var6, var1.getZ() + (int)Math.round((double)var4 * var7 / var5)
            );
            if (hasLavaInColumn(var0, var8)) {
               return true;
            }
         }

         for (int[] var10 : EIGHT_DIRS) {
            if (hasLavaInColumn(var0, var2.offset(var10[0], 0, var10[1]))) {
               return true;
            }
         }

         return false;
      }

      private static boolean hasLavaInColumn(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1) {
         for (int var2 = 0; var2 <= 12; var2++) {
            AiPathfinder.CellInfo var3 = (AiPathfinder.CellInfo)var0.apply(var1.below(var2));
            if (!var3.loaded()) {
               return false;
            }

            if (var3.lava()) {
               return true;
            }

            if (!var3.passable() || var3.water()) {
               return false;
            }
         }

         return false;
      }

      private static boolean hasDeepAirDrop(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1) {
         for (int var2 = 0; var2 <= 4; var2++) {
            AiPathfinder.CellInfo var3 = (AiPathfinder.CellInfo)var0.apply(var1.below(var2));
            if (!var3.loaded() || !var3.passable() || var3.water()) {
               return false;
            }
         }

         return true;
      }

      private static int eightDirIndex(int var0, int var1) {
         for (int var2 = 0; var2 < EIGHT_DIRS.length; var2++) {
            if (EIGHT_DIRS[var2][0] == var0 && EIGHT_DIRS[var2][1] == var1) {
               return var2;
            }
         }

         throw new IllegalArgumentException("not a neighbour: " + var0 + "," + var1);
      }

      static boolean isFlatWalkableCell(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, double var2) {
         AiPathfinder.CellInfo var4 = (AiPathfinder.CellInfo)var0.apply(var1);
         AiPathfinder.CellInfo var5 = (AiPathfinder.CellInfo)var0.apply(var1.below());
         return isStandable(var4, (AiPathfinder.CellInfo)var0.apply(var1.above()), var5) && !var4.water()
            ? Math.abs(surfaceY(var1.getY(), var5) - var2) <= 0.562500001
            : false;
      }

      static boolean hasNonFlatGapOnPath(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, BlockPos var2, double var3) {
         int var5 = var2.getX() - var1.getX();
         int var6 = var2.getZ() - var1.getZ();
         int var7 = Math.max(Math.abs(var5), Math.abs(var6));
         if (var7 != 1) {
            for (int var8 = 1; var8 < var7; var8++) {
               int var9 = var1.getX() + (int)Math.round((double)var5 * var8 / var7);
               int var10 = var1.getZ() + (int)Math.round((double)var6 * var8 / var7);
               if (!isFlatWalkableCell(var0, new BlockPos(var9, var1.getY(), var10), var3)) {
                  return true;
               }
            }

            return false;
         } else {
            return var5 != 0 && var6 != 0
               ? !isFlatWalkableCell(var0, var1.offset(var5, 0, 0), var3) || !isFlatWalkableCell(var0, var1.offset(0, 0, var6), var3)
               : false;
         }
      }

      private static double incomingApproachSpeed(AiPathfinder.MoveGenerator.CachedGrid var0, BlockPos var1, double var2, boolean var4) {
         if (var0.incomingMove == null) {
            return 0.0;
         }

         double var10000;
         switch (var0.incomingMove.type()) {
            case WALK:
            case WALK_DIAGONAL:
            case SPRINT:
            case SPRINT_DIAGONAL:
               AiPlannerWorld var5 = var0.plannerWorld();
               int var6 = (int)Math.floor(var2 - 0.5000001);
               double var7 = var5.frictionAt(var1.getX(), var6, var1.getZ());
               double var9 = 0.91 * var7 * var5.speedFactorAt(var1.getX(), var6, var1.getZ());
               boolean var11 = var0.incomingMove.type() == AiPathfinder.MoveType.SPRINT
                  || var0.incomingMove.type() == AiPathfinder.MoveType.SPRINT_DIAGONAL;
               double var12 = var5.movementSpeed() * (var11 ? 1.3 : 1.0) * (0.21600002 / (var7 * var7 * var7)) * 0.98;
               double var14 = var12 * var9 / (1.0 - var9);
               var10000 = var7 >= 0.9 ? var14 * (1.0 - Math.pow(var9, Math.max(1.0, var0.incomingMove.costTicks()))) : var14;
               break;
            default:
               var10000 = exitSpeedOfMove(var0.incomingMove, var4);
         }

         return var10000;
      }

      static double exitSpeedOfMove(AiPathfinder.PathStep var0) {
         return exitSpeedOfMove(var0, false);
      }

      static double exitSpeedOfMove(AiPathfinder.PathStep var0, boolean var1) {
         if (var0 == null) {
            return 0.0;
         }

         double var10000;
         switch (var0.type()) {
            case WALK:
            case WALK_DIAGONAL:
            case STEP_UP_1:
            case DESCEND_1:
               var10000 = 0.2;
               break;
            case SPRINT:
            case SPRINT_DIAGONAL:
            case JUMP_GAP_2:
            case JUMP_GAP_3:
            case PARKOUR_ASCEND:
            case SPRINT_JUMP:
               var10000 = 0.28;
               break;
            case JUMP_UP_1:
               var10000 = var1 ? 0.28 : 0.2;
               break;
            case DESCEND_2:
            case DESCEND_3:
            case DESCEND_SOFT:
            case DIG_DOWN_1:
            case DIG_TUNNEL:
            case PILLAR:
            case BRIDGE_CELL:
            case CLIMB_1:
            case DOOR:
            default:
               var10000 = 0.0;
               break;
            case JUMP:
               var10000 = var0.jump() == null ? 0.2 : Math.min(0.28, var0.jump().landSpeed());
         }

         return var10000;
      }

      private static void generateDigOutMoves(
         AiPathfinder.MoveGenerator.CachedGrid var0,
         AiPathfinder.PathOptions var1,
         Set<Long> var2,
         BlockPos var3,
         AiPathfinder.CellInfo var4,
         List<AiPathfinder.PathStep> var5
      ) {
         for (int[] var9 : CARDINAL_DIRS) {
            BlockPos var10 = var3.offset(var9[0], 0, var9[1]);
            if (!var2.contains(var10.asLong())) {
               AiPathfinder.CellInfo var11 = var0.probePos(var10);
               AiPathfinder.CellInfo var12 = var0.probePos(var10.above());
               AiPathfinder.CellInfo var13 = var0.probePos(var10.below());
               if (var13.loaded()
                  && !var13.passable()
                  && !var13.hazardFloor()
                  && !var13.lava()
                  && !var11.hazardFeet()
                  && !var11.lava()
                  && !var12.hazardFeet()
                  && !var12.lava()) {
                  boolean var14 = var11.diggable();
                  boolean var15 = var12.diggable();
                  if ((var14 || var11.passable()) && (var15 || var12.passable())) {
                     ArrayList var16 = new ArrayList(3);
                     double var17 = 0.0;
                     double var19 = 0.0;
                     if (!var4.passable() && var4.diggable()) {
                        var16.add(var3);
                        var17 += var4.breakTicks() + var1.breakReluctance() + var4.destructionPenalty();
                        var19 += var4.breakTicks();
                     }

                     if (var14) {
                        var16.add(var10);
                        var17 += var11.breakTicks() + var1.breakReluctance() + var11.destructionPenalty();
                        var19 += var11.breakTicks();
                     }

                     if (var15) {
                        var16.add(var10.above());
                        var17 += var12.breakTicks() + var1.breakReluctance() + var12.destructionPenalty();
                        var19 += var12.breakTicks();
                     }

                     if (var1.strategy() == AiPathfinder.RouteStrategy.SAVER) {
                        var17 *= 2.0;
                     }

                     var5.add(
                        new AiPathfinder.PathStep(
                           AiPathfinder.MoveType.DIG_TUNNEL, var3, var10, var16, List.of(), AiPathfinder.MoveCosts.digTunnelCost(var17), var19
                        )
                     );
                  }
               }
            }
         }
      }

      static double portalAndWebPenalty(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, BlockPos var2, boolean var3) {
         AiPathfinder.CellInfo var4 = (AiPathfinder.CellInfo)var0.apply(var2);
         AiPathfinder.CellInfo var5 = (AiPathfinder.CellInfo)var0.apply(var2.above());
         double var6 = !var4.portal() && !var5.portal() ? 0.0 : 400.0;
         boolean var8 = var4.web() || var5.web();
         if (!var8) {
            return var6;
         }

         boolean var9 = ((AiPathfinder.CellInfo)var0.apply(var1)).web() || ((AiPathfinder.CellInfo)var0.apply(var1.above())).web();
         double var10 = var9 ? 1.0 : 1.6;
         double var12 = Math.hypot(var2.getX() - var1.getX(), var2.getZ() - var1.getZ());
         return var6 + var10 * var12 * (var3 ? 32.0 : 41.0);
      }

      static double portalPenalty(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1) {
         return !((AiPathfinder.CellInfo)var0.apply(var1)).portal() && !((AiPathfinder.CellInfo)var0.apply(var1.above())).portal() ? 0.0 : 400.0;
      }

      static double fallingWebOverlap(Function<BlockPos, AiPathfinder.CellInfo> var0, int var1, int var2, double var3, double var5) {
         double var7 = 0.0;
         double var9 = var5;

         for (int var11 = (int)Math.floor(var5); var11 <= (int)Math.floor(var3) + 1; var11++) {
            if (((AiPathfinder.CellInfo)var0.apply(new BlockPos(var1, var11, var2))).web()) {
               double var12 = Math.max(Math.max(var11 - 1.8, var5), var9);
               double var14 = Math.min(var11 + 1.0, var3);
               if (var14 > var12) {
                  var7 += var14 - var12;
                  var9 = var14;
               }
            }
         }

         return var7;
      }

      static boolean isDiagonalCornerClear(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1) {
         AiPathfinder.CellInfo var2 = (AiPathfinder.CellInfo)var0.apply(var1);
         AiPathfinder.CellInfo var3 = (AiPathfinder.CellInfo)var0.apply(var1.above());
         AiPathfinder.CellInfo var4 = (AiPathfinder.CellInfo)var0.apply(var1.below());
         if (!var2.passable() || !var3.passable() || var2.hazardFeet() || var2.lava()) {
            return false;
         }

         if (var2.contactHazard() || var3.contactHazard()) {
            return false;
         }

         if (var2.web() || var3.web() || var2.portal() || var3.portal()) {
            return false;
         }

         if (var2.water()) {
            return true;
         }

         if (!var4.loaded() || var4.lava() || var4.hazardFloor()) {
            return false;
         }

         if (!var4.passable()) {
            return true;
         }

         if (var4.water()) {
            return true;
         }

         AiPathfinder.CellInfo var5 = (AiPathfinder.CellInfo)var0.apply(var1.below(2));
         return var5.loaded() && !var5.passable() && !var5.lava() && !var5.hazardFloor();
      }

      static boolean hasSafeOvershootAhead(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, int var2, int var3) {
         return hasSafeOvershootAhead(var0, var1, var2, var3, 1);
      }

      public static boolean hasDropLandingClearance(AiPhysics.PhysicsWorld var0, double var1, double var3, BlockPos var5) {
         double var6 = 0.4;
         AABB var8 = new AABB(var1 - var6, var5.getY(), var3 - var6, var1 + var6, var5.getY() + 2, var3 + var6);

         for (AABB var10 : var0.collisions(var8)) {
            if (var10.intersects(var8)) {
               return false;
            }
         }

         return true;
      }

      public static boolean isFallTrajectoryClear(AiPhysics.PhysicsWorld var0, List<AiPhysics.MotionState> var1, int var2) {
         double var3 = 0.4;

         for (AiPhysics.MotionState var6 : var1) {
            if (!(var6.y() > var2 + 3) && !(var6.y() < var2)) {
               AABB var7 = new AABB(var6.x() - var3, var6.y(), var6.z() - var3, var6.x() + var3, var6.y() + 1.8, var6.z() + var3);

               for (AABB var9 : var0.collisions(var7)) {
                  if (var9.intersects(var7)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }

      public static boolean withinStepDownHeight(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, BlockPos var2) {
         AiPathfinder.CellInfo var3 = (AiPathfinder.CellInfo)var0.apply(var1.below());
         AiPathfinder.CellInfo var4 = (AiPathfinder.CellInfo)var0.apply(var2.below());
         return !var3.passable() && !var4.passable() ? surfaceY(var1.getY(), var3) - surfaceY(var2.getY(), var4) <= 0.562501 : false;
      }

      static boolean hasSafeOvershootAhead(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, int var2, int var3, int var4) {
         return checkOvershootColumn(var0, var1, var2, var3, var4, false);
      }

      private static boolean checkOvershootColumn(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, int var2, int var3, int var4, boolean var5) {
         BlockPos var6 = var1.offset(var2, 0, var3);
         AiPathfinder.CellInfo var7 = (AiPathfinder.CellInfo)var0.apply(var6);
         if (var7.loaded() && !var7.lava() && !var7.hazardFeet()) {
            if (!var7.web() && !((AiPathfinder.CellInfo)var0.apply(var6.above())).web()) {
               if (!var7.passable()) {
                  return var4 < 2 || !((AiPathfinder.CellInfo)var0.apply(var6.above())).passable();
               }

               if (var7.water()) {
                  return true;
               }

               int var8 = 0;

               while (var8 <= 2) {
                  BlockPos var9 = var6.below(var8);
                  AiPathfinder.CellInfo var10 = (AiPathfinder.CellInfo)var0.apply(var9);
                  if (var10.loaded() && !var10.lava() && !var10.hazardFeet() && !var10.web()) {
                     if (var8 > 0 && var10.water()) {
                        return true;
                     }

                     AiPathfinder.CellInfo var11 = (AiPathfinder.CellInfo)var0.apply(var9.below());
                     if (var11.loaded() && !var11.lava() && !var11.hazardFloor() && (!var11.farmland() || var5)) {
                        if (var11.passable() && !var11.water()) {
                           var8++;
                           continue;
                        }

                        return isStandable(var10, (AiPathfinder.CellInfo)var0.apply(var9.above()), var11);
                     }

                     return false;
                  }

                  return false;
               }

               return false;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private static boolean isMoveTypeDisabled(AiPathfinder.PathOptions var0, AiPathfinder.MoveType var1) {
         return var0.disabledTypes().contains(var1);
      }

      private static boolean isSolidSafeFloor(AiPathfinder.CellInfo var0) {
         return var0.loaded() && !var0.passable() && !var0.water() && !var0.lava() && !var0.hazardFloor();
      }

      static boolean canSprintJumpFlat(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, BlockPos var2) {
         AiPathfinder.CellInfo var3 = (AiPathfinder.CellInfo)var0.apply(var1);
         AiPathfinder.CellInfo var4 = (AiPathfinder.CellInfo)var0.apply(var1.below());
         if (var3.water() || var3.lava() || var3.climbable() || var3.hazardFeet() || var3.web() || ((AiPathfinder.CellInfo)var0.apply(var1.above())).web()) {
            return false;
         }

         if (var4.passable() || var4.farmland() || var4.hazardFloor() || var4.lava() || var4.soulSand()) {
            return false;
         }

         if (!var4.honey() && !var4.slime()) {
            AiPathfinder.CellInfo var5 = (AiPathfinder.CellInfo)var0.apply(var2);
            AiPathfinder.CellInfo var6 = (AiPathfinder.CellInfo)var0.apply(var2.below());
            if (!var6.honey() && !var6.slime()) {
               if (!var5.water() && !var5.thorny() && !var5.web() && !((AiPathfinder.CellInfo)var0.apply(var2.above())).web() && !var6.farmland()) {
                  int var7 = Integer.signum(var2.getX() - var1.getX());
                  int var8 = Integer.signum(var2.getZ() - var1.getZ());
                  int var9 = 0;

                  while (var9 <= 4) {
                     BlockPos var10 = var1.offset(var7 * var9, 0, var8 * var9);
                     if (!((AiPathfinder.CellInfo)var0.apply(var10)).web()
                        && !((AiPathfinder.CellInfo)var0.apply(var10.above())).web()
                        && !((AiPathfinder.CellInfo)var0.apply(var10.above(2))).web()) {
                        if (((AiPathfinder.CellInfo)var0.apply(var10.below())).farmland()) {
                           return false;
                        }

                        for (byte var11 = -1; var11 <= 1; var11 += 2) {
                           if (((AiPathfinder.CellInfo)var0.apply(var10.offset(var8 * var11, -1, var7 * var11))).farmland()) {
                              return false;
                           }
                        }

                        if (!((AiPathfinder.CellInfo)var0.apply(var10)).portal() && !((AiPathfinder.CellInfo)var0.apply(var10.above())).portal()) {
                           var9++;
                           continue;
                        }

                        return false;
                     }

                     return false;
                  }

                  if (surfaceY(var1.getY(), var4) != surfaceY(var2.getY(), var6)) {
                     return false;
                  } else {
                     return !isStandable(var5, (AiPathfinder.CellInfo)var0.apply(var2.above()), var6)
                        ? false
                        : hasTwoBlockHeadroom((AiPathfinder.CellInfo)var0.apply(var1.above(2)), (AiPathfinder.CellInfo)var0.apply(var1.above(3)))
                           || hasLowCeilingSprintJump(var0, var1, var2);
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      static boolean hasLowCeilingSprintJump(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, BlockPos var2) {
         AiPathfinder.CellInfo var3 = (AiPathfinder.CellInfo)var0.apply(var1.above(2));
         AiPathfinder.CellInfo var4 = (AiPathfinder.CellInfo)var0.apply(var2.above(2));
         return var3.loaded() && var4.loaded() && !var3.passable() && !var4.passable() && !var3.hazardFeet() && !var4.hazardFeet();
      }

      static boolean canSprintJumpGap(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, int var2, int var3, int var4, boolean var5) {
         AiPathfinder.CellInfo var6 = (AiPathfinder.CellInfo)var0.apply(var1);
         AiPathfinder.CellInfo var7 = (AiPathfinder.CellInfo)var0.apply(var1.below());
         if (!var6.loaded() || var6.water() || var6.lava() || var6.climbable() || var6.hazardFeet() || var6.feetTrigger() || var6.web()) {
            return false;
         }

         if (var7.ice()) {
            return false;
         }

         if (!var7.passable() && var7.top16() == 16 && !var7.slabStair() && !var7.hazardFloor()) {
            for (int var8 = 0; var8 <= var4 + 1; var8++) {
               int var9 = var1.getY() + 1;
               int var10 = var1.getY() + 2;
               if (var5 && var8 == var4 + 1) {
                  var9++;
                  var10++;
               } else if (var5 && var8 > 0) {
                  var10++;
               }

               for (int var11 = var9; var11 <= var10; var11++) {
                  AiPathfinder.CellInfo var12 = (AiPathfinder.CellInfo)var0.apply(
                     new BlockPos(var1.getX() + var2 * var8, var11, var1.getZ() + var3 * var8)
                  );
                  if (!var12.passable() || var12.lava() || var12.water() || var12.hazardFeet() || var12.climbable() || var12.web()) {
                     return false;
                  }
               }
            }

            boolean var13 = false;

            for (int var14 = 1; var14 <= var4; var14++) {
               BlockPos var16 = new BlockPos(var1.getX() + var2 * var14, var1.getY(), var1.getZ() + var3 * var14);
               AiPathfinder.CellInfo var18 = (AiPathfinder.CellInfo)var0.apply(var16);
               if (!var18.passable() || var18.lava() || var18.hazardFeet() || var18.feetTrigger() || var18.climbable() || var18.web()) {
                  return false;
               }

               if (((AiPathfinder.CellInfo)var0.apply(var16.below())).passable()) {
                  var13 = true;
               }
            }

            if (!var13) {
               return false;
            } else {
               BlockPos var15 = new BlockPos(var1.getX() + var2 * (var4 + 1), var1.getY() + (var5 ? 1 : 0), var1.getZ() + var3 * (var4 + 1));
               AiPathfinder.CellInfo var17 = (AiPathfinder.CellInfo)var0.apply(var15.below());
               if (!isStandable((AiPathfinder.CellInfo)var0.apply(var15), (AiPathfinder.CellInfo)var0.apply(var15.above()), var17)
                  || var17.farmland()
                  || var17.ice()
                  || var17.slime()) {
                  return false;
               } else {
                  return ((AiPathfinder.CellInfo)var0.apply(var15)).web()
                     ? false
                     : hasNearbySafeFloor(var0, var15.offset(var2, 0, var3)) && hasNearbySafeFloor(var0, var15.offset(2 * var2, 0, 2 * var3));
               }
            }
         } else {
            return false;
         }
      }

      static int measureBridgeDepth(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1) {
         AiPathfinder.CellInfo var2 = (AiPathfinder.CellInfo)var0.apply(var1);
         if (var2.loaded() && !var2.lava() && !var2.hazardFeet()) {
            if (!var2.passable()) {
               return 0;
            }

            int var3 = 1;

            while (var3 <= 32) {
               AiPathfinder.CellInfo var4 = (AiPathfinder.CellInfo)var0.apply(var1.below(var3));
               if (!var4.loaded()) {
                  return var3;
               }

               if (!var4.lava() && !var4.hazardFloor() && !var4.hazardFeet()) {
                  if (!var4.water() && var4.passable()) {
                     var3++;
                     continue;
                  }

                  return var3 - 1;
               }

               return -1;
            }

            return 32;
         } else {
            return -1;
         }
      }

      static boolean hasNearbySafeFloor(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1) {
         AiPathfinder.CellInfo var2 = (AiPathfinder.CellInfo)var0.apply(var1);
         if (!var2.loaded() || var2.lava() || var2.hazardFeet()) {
            return false;
         }

         if (!var2.passable()) {
            return true;
         }

         AiPathfinder.CellInfo var3 = (AiPathfinder.CellInfo)var0.apply(var1.below());
         if (var3.loaded() && !var3.lava() && !var3.hazardFloor()) {
            if (isStandable(var2, (AiPathfinder.CellInfo)var0.apply(var1.above()), var3)) {
               return true;
            }

            for (int var4 = 1; var4 <= 4; var4++) {
               AiPathfinder.CellInfo var5 = (AiPathfinder.CellInfo)var0.apply(var1.below(var4));
               if (!var5.loaded() || var5.lava() || var5.hazardFloor()) {
                  return false;
               }

               if (var5.water()) {
                  return true;
               }

               if (!var5.passable()) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }

      static boolean hasAdjacentSolidAnchor(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1) {
         AiPathfinder.CellInfo var2 = (AiPathfinder.CellInfo)var0.apply(var1.below());
         if (var2.loaded() && !var2.passable()) {
            return true;
         }

         for (int[] var6 : CARDINAL_DIRS) {
            AiPathfinder.CellInfo var7 = (AiPathfinder.CellInfo)var0.apply(var1.offset(var6[0], 0, var6[1]));
            if (var7.loaded() && !var7.passable()) {
               return true;
            }
         }

         return false;
      }

      static boolean hasBridgeSupportAnchor(Function<BlockPos, AiPathfinder.CellInfo> var0, BlockPos var1, BlockPos var2, AiPathfinder.PathStep var3) {
         BlockPos var4 = var2.below();
         AiPathfinder.CellInfo var5 = (AiPathfinder.CellInfo)var0.apply(var4.below());
         if (var5.loaded() && !var5.passable()) {
            return true;
         }

         if (var3 != null && var3.type() == AiPathfinder.MoveType.BRIDGE_CELL) {
            return true;
         }

         if (allowCantileverBridgeFromSolid) {
            AiPathfinder.CellInfo var6 = (AiPathfinder.CellInfo)var0.apply(var1.below());
            if (var6.loaded() && !var6.passable() && var6.top16() >= 16 && !var6.hazardFloor()) {
               return true;
            }
         }

         for (int[] var9 : CARDINAL_DIRS) {
            BlockPos var10 = var4.offset(var9[0], 0, var9[1]);
            if (!var10.equals(var1.below())
               && ((AiPathfinder.CellInfo)var0.apply(var10)).loaded()
               && !((AiPathfinder.CellInfo)var0.apply(var10)).passable()) {
               return true;
            }
         }

         return false;
      }

      private record JumpCandidate(
         BlockPos from, BlockPos to, double sourceY, double targetY, double approach, boolean sprint, int dx, int dz, boolean landedAhead
      ) {
         BlockPos deeper() {
            return this.to.offset(this.dx, 0, this.dz);
         }
      }

      static final class CachedGrid implements AiPathfinder.GridView, Function<BlockPos, AiPathfinder.CellInfo> {
         @Override
         public AiPathfinder.CellInfo apply(BlockPos var1) {
            return this.probePos(var1);
         }
         final AiPathfinder.GridView delegate;
         private long[] cacheKeys;
         private AiPathfinder.CellInfo[] cacheValues;
         private final int maxCacheCapacity;
         int queryFlags;
         AiPathfinder.PathStep incomingMove;
         boolean allowDeepPlatformLanding;
         NavigationOpenSet.Inner_DataUtil activeNode;
         static final int EDIT_TRAIL_DEPTH = 6;
         long blockQueries;
         private AiPlannerWorld plannerWorld;
         private Long2LongOpenHashMap columnOccupancyCache;
         Map<AiPathfinder.MoveGenerator.CachedGrid.JumpCacheKey, Object> localJumpCache = new HashMap<>();
         private final Map<BlockPos, List<AiPathfinder.PathStep>> slimeBounceCache = new HashMap<>();
         int jumpSolves;
         int jumpCacheHits;
         long jumpSolveNanos;
         private long normalJumpBudgetSpentNanos;
         private long tightJumpBudgetSpentNanos;
         private boolean jumpBudgetExhausted;
         private boolean ignoreCachedCells;
         private boolean fullPhysicsParkour;
         private boolean fastPassMode;
         private boolean quickJumpMode;
         private boolean reducedJumpMode;
         boolean allowManeuverShortcuts;
         private boolean limitJumpDropDepth;
         AiPathfinder.FallPolicy fallPolicy;
         private Consumer<AiPathfinder.MoveGenerator.JumpCandidate> deferredJumpSink;
         static final long NORMAL_JUMP_BUDGET_NANOS = 65000000L;
         static final long TIGHT_JUMP_BUDGET_NANOS = 200000000L;
         private boolean loggedBudgetExhausted;
         private static final Object NULL_SENTINEL = new Object();
         private static final Long2ObjectOpenHashMap<Object> GLOBAL_JUMP_CACHE = new Long2ObjectOpenHashMap<>();
         private static final int GLOBAL_JUMP_CACHE_LIMIT = 65536;
         final double movementSpeed;
         static final int DOOR_NONE = 0;
         static final int DOOR_X_AXIS = 1;
         static final int DOOR_Z_AXIS = 2;
         private final Long2ByteOpenHashMap adjacentContactHazardCache;
         private final Map<Long, Object> fallLandingCache;
         static final int MAX_FALL_SIM_TICKS = 120;
         private static final AiPathfinder.CellInfo PLACED_SOLID_CELL = AiPathfinder.CellInfo.fullBlock(1.0 / 0.0);
         private long[] overlayKeys;
         private AiPathfinder.CellInfo[] overlayValues;
         private int overlayCount;
         private AiPathfinder.PathStep overlayIncomingMove;
         private NavigationOpenSet.Inner_DataUtil overlayNode;
         private boolean overlayValid;
         private long overlayBloomMask;

         int findHighestSolidBelow(int var1, int var2, int var3, int var4) {
            if (this.columnOccupancyCache == null) {
               this.columnOccupancyCache = new Long2LongOpenHashMap();
            }

            for (int var5 = var2 & -64; var5 >= (var4 & -64); var5 -= 64) {
               long var6 = BlockPos.asLong(var1, var5, var3);
               long var8 = this.columnOccupancyCache.getOrDefault(var6, -9223372036854775808L);
               if (var8 == -9223372036854775808L && !this.columnOccupancyCache.containsKey(var6)) {
                  AiPathfinder.CellOccupancy var10 = this.delegate.putAt_FileStore_IoCodec_I(var1, var5, var3, this.ignoreCachedCells);
                  var8 = var10.occupied();
                  if (var10.pending()) {
                     this.queryFlags |= 1;
                  } else {
                     this.columnOccupancyCache.put(var6, var8);
                  }
               }

               if (var2 - var5 < 63) {
                  var8 &= -1L >>> 63 - (var2 - var5);
               }

               if (var8 != 0L) {
                  return Math.max(var4 - 1, var5 + 63 - Long.numberOfLeadingZeros(var8));
               }
            }

            return var4 - 1;
         }

         CachedGrid(AiPathfinder.GridView var1, int var2) {
            this(var1, var2, var1.movementSpeed());
         }

         CachedGrid(AiPathfinder.GridView var1, int var2, double var3) {
            this.fallPolicy = AiPathfinder.FallPolicy.STANDARD;
            this.adjacentContactHazardCache = new Long2ByteOpenHashMap();
            this.fallLandingCache = new HashMap<>();
            this.overlayKeys = new long[16];
            this.overlayValues = new AiPathfinder.CellInfo[16];
            this.delegate = var1;
            this.maxCacheCapacity = var2;
            this.movementSpeed = var3;
            this.cacheKeys = new long[256];
            this.cacheValues = new AiPathfinder.CellInfo[256];
         }

         boolean isJumpBudgetExhausted() {
            return this.jumpBudgetExhausted;
         }

         private boolean isTightHeadroomCell(BlockPos var1) {
            return !this.probePos(var1.above(2)).passable()
               ? true
               : !this.probePos(var1.west().above()).passable()
                     && !this.probePos(var1.east().above()).passable()
                  || !this.probePos(var1.north().above()).passable()
                     && !this.probePos(var1.south().above()).passable();
         }

         private boolean isJumpBudgetExceededAt(BlockPos var1) {
            if (this.fullPhysicsParkour) {
               return false;
            }

            boolean var2 = this.normalJumpBudgetSpentNanos >= 65000000L && (!this.isTightHeadroomCell(var1) || this.tightJumpBudgetSpentNanos >= 200000000L);
            if (var2) {
               this.jumpBudgetExhausted = true;
            }

            return var2;
         }

         int openDoorAxis(BlockPos var1) {
            AiPathfinder.CellInfo var2 = this.probePos(var1);
            if (var2.door() && var2.passable()) {
               short var3 = this.delegate.putAt_S_I(var1.getX(), var1.getY(), var1.getZ());
               if (var3 != 0 && var3 != 1 && var3 != -1) {
                  List var4 = AiShapes.boxes(var3);
                  if (var4.isEmpty()) {
                     return 0;
                  }

                  AABB var5 = (AABB)var4.get(0);
                  return var5.maxX - var5.minX >= var5.maxZ - var5.minZ ? 1 : 2;
               } else {
                  return 0;
               }
            } else {
               return 0;
            }
         }

         boolean hasOpenDoorObstacle(BlockPos var1) {
            return this.openDoorAxis(var1) != 0 || this.openDoorAxis(var1.above()) != 0;
         }

         boolean isBlockedByOpenDoor(BlockPos var1, int var2, int var3) {
            int var4 = this.openDoorAxis(var1);
            if (var4 == 0) {
               var4 = this.openDoorAxis(var1.above());
            }

            if (var4 == 0) {
               return false;
            } else if (var2 != 0 && var3 != 0) {
               return true;
            } else {
               return var2 != 0 ? var4 != 1 : var4 != 2;
            }
         }

         AiPlannerWorld plannerWorld() {
            if (this.plannerWorld == null) {
               this.plannerWorld = new AiPlannerWorld(this.delegate, this);
            }

            return this.plannerWorld;
         }

         boolean hasAdjacentContactHazard(BlockPos var1) {
            long var2 = var1.asLong();
            byte var4 = this.adjacentContactHazardCache.get(var2);
            if (var4 != 0) {
               return var4 == 2;
            }

            boolean var5 = false;

            for (int[] var9 : AiPathfinder.MoveGenerator.EIGHT_DIRS) {
               BlockPos var10 = var1.offset(var9[0], 0, var9[1]);
               if (this.probePos(var10).contactHazard() || this.probePos(var10.above()).contactHazard()) {
                  var5 = true;
                  break;
               }
            }

            this.adjacentContactHazardCache.put(var2, (byte)(var5 ? 2 : 1));
            return var5;
         }

         double adjacentContactHazardPenalty(BlockPos var1) {
            return this.hasAdjacentContactHazard(var1) ? 2.0 : 0.0;
         }

         BlockPos simulateStepOffLanding(BlockPos var1, double var2, int var4, int var5) {
            long var6 = var1.asLong() * 31L + (var4 + 1) * 3 + (var5 + 1);
            Object var8 = this.fallLandingCache.get(var6);
            if (var8 != null) {
               return var8 == NULL_SENTINEL ? null : (BlockPos)var8;
            }

            if (!this.hasWaterDropAhead(var1, var4, var5)) {
               this.fallLandingCache.put(var6, NULL_SENTINEL);
               return null;
            }

            AiPlannerWorld var9 = this.plannerWorld();
            long var10 = var9.putAt_J;
            BlockPos var12 = this.traceStepOffLanding(var9, var1, var2, (float)Math.toDegrees(Math.atan2(-var4, var5)), null);
            BlockPos var13 = null;
            if (var12 != null) {
               BlockPos var14 = this.traceStepOffLanding(var9, var1, var2, 0.0F / 0.0F, var12);
               if (var14 != null) {
                  BlockPos var15 = var14;

                  while (this.probePos(var15.below()).water() && var1.getY() - var15.getY() < 40) {
                     var15 = var15.below();
                  }

                  BlockPos var16 = var14;

                  while (this.probePos(var16.above()).water() && var16.getY() < var1.getY()) {
                     var16 = var16.above();
                  }

                  if (AiPathfinder.MoveGenerator.isSolidSafeFloor(this.probePos(var15.below())) && var16.getY() < var1.getY()) {
                     var13 = var16;
                  }
               }
            }

            if (var9.putAt_J == var10) {
               this.fallLandingCache.put(var6, var13 == null ? NULL_SENTINEL : var13);
            }

            return var13;
         }

         private boolean hasWaterDropAhead(BlockPos var1, int var2, int var3) {
            for (int var4 = 1; var4 <= 3; var4++) {
               int var5 = var1.getX() + var2 * var4;
               int var6 = var1.getZ() + var3 * var4;

               for (int var7 = var1.getY() - 1; var7 >= var1.getY() - 40; var7--) {
                  AiPathfinder.CellInfo var8 = this.probePos(new BlockPos(var5, var7, var6));
                  if (!var8.loaded()) {
                     break;
                  }

                  if (var8.water()) {
                     return true;
                  }

                  if (!var8.passable()) {
                     break;
                  }
               }
            }

            return false;
         }

         private BlockPos traceStepOffLanding(AiPlannerWorld var1, BlockPos var2, double var3, float var5, BlockPos var6) {
            AiPhysics.MotionState var7 = AiPhysics.MotionState.standing(var2.getX() + 0.5, var3, var2.getZ() + 0.5);
            boolean var8 = false;
            ArrayList<AiPhysics.MotionState> var9 = new ArrayList<>();

            for (int var10 = 0; var10 < 120; var10++) {
               float var11 = var6 == null ? var5 : AiPhysics.yawBetween(var7.x(), var7.z(), var6.getX() + 0.5, var6.getZ() + 0.5);
               var7 = AiPhysics.step(var7, new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var11), var1);
               if (!var7.onGround()) {
                  var8 = true;
               } else if (var8) {
                  return null;
               }

               if (var8 && var7.horizontalCollision()) {
                  return null;
               }

               if (!var8 && var10 > 12) {
                  return null;
               }

               if (var8) {
                  var9.add(var7);
               }

               BlockPos var12 = BlockPos.containing(var7.x(), var7.y() + 0.01, var7.z());
               if (var8 && this.probePos(var12).water()) {
                  return AiPathfinder.MoveGenerator.hasDropLandingClearance(var1, var7.x(), var7.z(), var12)
                        && AiPathfinder.MoveGenerator.isFallTrajectoryClear(var1, var9, var12.getY())
                     ? var12
                     : null;
               }

               if (this.probePos(var12).lava() || var2.getY() - var7.y() > 40.0) {
                  return null;
               }
            }

            return null;
         }

         AiJumpSolver.JumpPlan cachedJumpSolve(BlockPos var1, BlockPos var2, long var3, boolean var5, Supplier<AiJumpSolver.JumpPlan> var6) {
            AiPathfinder.MoveGenerator.CachedGrid.JumpCacheKey var7 = new AiPathfinder.MoveGenerator.CachedGrid.JumpCacheKey(
               var1.asLong(), var2.asLong(), var3, var5
            );
            Object var8 = this.localJumpCache.get(var7);
            if (var8 != null) {
               return var8 == NULL_SENTINEL ? null : (AiJumpSolver.JumpPlan)var8;
            }

            AiPlannerWorld var9 = this.plannerWorld();
            long var10 = var9.putAt_J;
            AiJumpSolver.JumpPlan var12 = (AiJumpSolver.JumpPlan)var6.get();
            if (var9.putAt_J == var10 && !this.hasActiveEdits()) {
               this.localJumpCache.put(var7, var12 == null ? NULL_SENTINEL : var12);
            }

            return var12;
         }

         AiJumpSolver.JumpPlan solveJumpCached(BlockPos var1, double var2, BlockPos var4, double var5, double var7, boolean var9) {
            long var10 = Double.doubleToLongBits(var7);
            AiPathfinder.MoveGenerator.CachedGrid.JumpCacheKey var12 = new AiPathfinder.MoveGenerator.CachedGrid.JumpCacheKey(
               var1.asLong(), var4.asLong(), var10, var9
            );
            Object var13 = this.localJumpCache.get(var12);
            if (var13 != null) {
               return var13 == NULL_SENTINEL ? null : (AiJumpSolver.JumpPlan)var13;
            }

            boolean var14 = !this.fullPhysicsParkour && (this.isTightHeadroomCell(var1) || this.isTightHeadroomCell(var4));
            if (this.fullPhysicsParkour || (var14 ? this.tightJumpBudgetSpentNanos < 200000000L : this.normalJumpBudgetSpentNanos < 65000000L)) {
               long var15 = System.nanoTime();
               AiPlannerWorld var17 = this.plannerWorld();
               long var18 = var17.putAt_J;
               boolean var20 = this.fullPhysicsParkour && this.fallPolicy == AiPathfinder.FallPolicy.PARKOUR_SAFE && this.hasTwoHighWallOnDrop(var1, var2, var4, var5);
               boolean var21 = this.fullPhysicsParkour
                  && Math.abs(var4.getX() - var1.getX()) == 1
                  && Math.abs(var4.getZ() - var1.getZ()) == 1
                  && Math.abs(var5 - var2) <= 0.25
                  && (
                     !this.probePos(new BlockPos(var4.getX(), var1.getY(), var1.getZ())).passable()
                        || !this.probePos(new BlockPos(var1.getX(), var1.getY(), var4.getZ())).passable()
                  );
               boolean var22 = var20 || var21;
               if (!this.fastPassMode) {
                  var22 |= !this.probePos(var1.above(2)).passable()
                     || !this.probePos(var4.above(2)).passable();
                  if (this.fullPhysicsParkour) {
                     var22 |= this.probePos(var1.below()).narrowFooting()
                        || this.probePos(var4.below()).narrowFooting()
                        || !this.probePos(var1).passable()
                        || !this.probePos(var4).passable();
                  }

                  for (int[] var26 : AiPathfinder.MoveGenerator.CARDINAL_DIRS) {
                     if (var22) {
                        break;
                     }

                     BlockPos var27 = var1.offset(var26[0], 0, var26[1]);
                     BlockPos var28 = var4.offset(var26[0], 0, var26[1]);
                     var22 = !this.probePos(var27.above()).passable()
                        || !this.probePos(var28.above()).passable()
                        || this.fullPhysicsParkour
                           && (!this.probePos(var27).passable() || !this.probePos(var28).passable());
                  }
               }

               if (var7 > 0.0 && var22) {
                  AiJumpSolver.JumpPlan var36 = this.solveDirectJumpCached(var17, var1, var2, var4, var5, var7, var10, var9, var18);
                  AiJumpSolver.JumpPlan var37 = var36;
                  if (var37 == null || var37.margin() < -0.04) {
                     AiJumpSolver.JumpPlan var39 = this.solveJumpCached(var1, var2, var4, var5, 0.0, var9);
                     if (var39 != null && (var37 == null || var39.margin() > var37.margin())) {
                        var37 = var39;
                     }
                  }

                  if (var17.putAt_J == var18) {
                     this.localJumpCache.put(var12, var37 == null ? NULL_SENTINEL : var37);
                  }

                  long var40 = System.nanoTime() - var15;
                  this.jumpSolveNanos += var40;
                  if (var14) {
                     this.tightJumpBudgetSpentNanos += var40;
                  } else {
                     this.normalJumpBudgetSpentNanos += var40;
                  }

                  return var37;
               } else {
                  long var33 = var17.putAt_J_BlockPos(var1, var4, var22) * 31L
                     + (var4.getX() - var1.getX()) * 1000003L
                     + (var4.getY() - var1.getY()) * 10007L
                     + (var4.getZ() - var1.getZ()) * 7L
                     + (var9 ? 3 : 0);
                  if (this.fallPolicy != AiPathfinder.FallPolicy.STANDARD) {
                     var33 ^= 6384245875588680899L * this.fallPolicy.ordinal();
                  }

                  if (var22 && this.fullPhysicsParkour) {
                     var33 ^= -3263064605168079213L;
                  }

                  if (this.fullPhysicsParkour && !var22 && var5 - var2 > 0.6 && Math.abs((var4.getX() - var1.getX()) * (var4.getZ() - var1.getZ())) == 8) {
                     var33 ^= -3263064605168079213L;
                  }

                  boolean var38 = this.reducedJumpMode && !var20;
                  if (var22 && var38) {
                     var33 ^= -7046029254386353131L;
                  }

                  if (var22 && this.allowManeuverShortcuts) {
                     var33 ^= -1065810590584100411L;
                  }

                  if (this.quickJumpMode) {
                     var33 ^= -6752110988234923001L;
                  }

                  var33 = var33 * 31L + HashCommon.mix(var10);
                  var33 = var33 * 31L + HashCommon.mix(Double.doubleToLongBits(this.movementSpeed));
                  Object var42;
                  synchronized (GLOBAL_JUMP_CACHE) {
                     var42 = GLOBAL_JUMP_CACHE.get(var33);
                  }

                  AiJumpSolver.JumpPlan var41;
                  if (var42 != null && var17.putAt_J == var18) {
                     this.jumpCacheHits++;
                     var41 = var42 == NULL_SENTINEL
                        ? null
                        : ((AiPathfinder.MoveGenerator.CachedGrid.CachedJump)var42)
                           .plan()
                           .shifted(
                              var1.getX() - ((AiPathfinder.MoveGenerator.CachedGrid.CachedJump)var42).x(),
                              var1.getY() - ((AiPathfinder.MoveGenerator.CachedGrid.CachedJump)var42).y(),
                              var1.getZ() - ((AiPathfinder.MoveGenerator.CachedGrid.CachedJump)var42).z()
                           );
                     if (var41 != null && var41.maneuver() != null && var41.maneuver().replay(var17, var41.maneuver().start(), 0, var4, var5) < -0.04) {
                        var42 = null;
                     }
                  } else {
                     var41 = null;
                  }

                  if (var42 == null) {
                     this.jumpSolves++;
                     var41 = AiJumpSolver.solve(var17, var1, var2, var4, var5, var7, var9, var22, this.fullPhysicsParkour, var38, this.quickJumpMode);
                     if ((var41 != null || !var22) && var17.putAt_J == var18) {
                        synchronized (GLOBAL_JUMP_CACHE) {
                           if (GLOBAL_JUMP_CACHE.size() >= 65536) {
                              GLOBAL_JUMP_CACHE.clear();
                           }

                           GLOBAL_JUMP_CACHE.put(
                              var33,
                              var41 == null
                                 ? NULL_SENTINEL
                                 : new AiPathfinder.MoveGenerator.CachedGrid.CachedJump(var41, var1.getX(), var1.getY(), var1.getZ())
                           );
                        }
                     }
                  }

                  if (var17.putAt_J == var18) {
                     this.localJumpCache.put(var12, var41 == null ? NULL_SENTINEL : var41);
                  }

                  long var45 = System.nanoTime() - var15;
                  this.jumpSolveNanos += var45;
                  if (var14) {
                     this.tightJumpBudgetSpentNanos += var45;
                  } else {
                     this.normalJumpBudgetSpentNanos += var45;
                  }

                  return var41;
               }
            } else {
               this.jumpBudgetExhausted = true;
               if (!this.loggedBudgetExhausted) {
                  this.loggedBudgetExhausted = true;
                  AiNavDebug.log("jump solver budget spent: solves=" + this.jumpSolves + " hits=" + this.jumpCacheHits);
               }

               return null;
            }
         }

         private AiJumpSolver.JumpPlan solveDirectJumpCached(
            AiPlannerWorld var1, BlockPos var2, double var3, BlockPos var5, double var6, double var8, long var10, boolean var12, long var13
         ) {
            long var15 = var1.putAt_J_BlockPos(var2, var5, false) * 31L
               + (var5.getX() - var2.getX()) * 1000003L
               + (var5.getY() - var2.getY()) * 10007L
               + (var5.getZ() - var2.getZ()) * 7L
               + (var12 ? 3 : 0);
            if (this.fallPolicy != AiPathfinder.FallPolicy.STANDARD) {
               var15 ^= 6384245875588680899L * this.fallPolicy.ordinal();
            }

            if (this.quickJumpMode) {
               var15 ^= -6752110988234923001L;
            }

            var15 = var15 * 31L + HashCommon.mix(var10);
            var15 = var15 * 31L + HashCommon.mix(Double.doubleToLongBits(this.movementSpeed));
            Object var17;
            synchronized (GLOBAL_JUMP_CACHE) {
               var17 = GLOBAL_JUMP_CACHE.get(var15);
            }

            if (var17 != null && var1.putAt_J == var13) {
               this.jumpCacheHits++;
               return var17 == NULL_SENTINEL
                  ? null
                  : ((AiPathfinder.MoveGenerator.CachedGrid.CachedJump)var17)
                     .plan()
                     .shifted(
                        var2.getX() - ((AiPathfinder.MoveGenerator.CachedGrid.CachedJump)var17).x(),
                        var2.getY() - ((AiPathfinder.MoveGenerator.CachedGrid.CachedJump)var17).y(),
                        var2.getZ() - ((AiPathfinder.MoveGenerator.CachedGrid.CachedJump)var17).z()
                     );
            }

            this.jumpSolves++;
            AiJumpSolver.JumpPlan var25 = AiJumpSolver.solve(var1, var2, var3, var5, var6, var8, var12, false, false, false, this.quickJumpMode);
            if (var1.putAt_J == var13) {
               synchronized (GLOBAL_JUMP_CACHE) {
                  if (GLOBAL_JUMP_CACHE.size() >= 65536) {
                     GLOBAL_JUMP_CACHE.clear();
                  }

                  GLOBAL_JUMP_CACHE.put(
                     var15,
                     var25 == null ? NULL_SENTINEL : new AiPathfinder.MoveGenerator.CachedGrid.CachedJump(var25, var2.getX(), var2.getY(), var2.getZ())
                  );
               }
            }

            return var25;
         }

         private boolean hasTwoHighWallOnDrop(BlockPos var1, double var2, BlockPos var4, double var5) {
            int var7 = var4.getX() - var1.getX();
            int var8 = var4.getZ() - var1.getZ();
            int var9 = Math.max(Math.abs(var7), Math.abs(var8));
            if (var9 >= 4 && !(var2 - var5 < 1.5)) {
               int var10 = (int)Math.floor(var2);

               for (int var11 = 2; var11 < var9; var11++) {
                  int var12 = var1.getX() + (int)Math.round((double)var11 * var7 / var9);
                  int var13 = var1.getZ() + (int)Math.round((double)var11 * var8 / var9);
                  if (this.putAt_S_I(var12, var10, var13) == 1 && this.putAt_S_I(var12, var10 + 1, var13) == 1) {
                     return true;
                  }
               }

               return false;
            } else {
               return false;
            }
         }

         private void growCellCache() {
            long[] var1 = this.cacheKeys;
            AiPathfinder.CellInfo[] var2 = this.cacheValues;
            this.cacheKeys = new long[this.cacheKeys.length * 2];
            this.cacheValues = new AiPathfinder.CellInfo[this.cacheKeys.length];

            for (int var3 = 0; var3 < var1.length; var3++) {
               if (var2[var3] != null) {
                  int var4 = (int)HashCommon.mix(var1[var3]) & this.cacheKeys.length - 1;
                  this.cacheKeys[var4] = var1[var3];
                  this.cacheValues[var4] = var2[var3];
               }
            }
         }

         private void rebuildEditOverlay() {
            this.overlayValid = true;
            this.overlayIncomingMove = this.incomingMove;
            this.overlayNode = this.activeNode;
            this.overlayCount = 0;
            this.overlayBloomMask = 0L;
            if (this.incomingMove != null) {
               for (BlockPos var2 : this.incomingMove.cellsToBreak()) {
                  this.putOverlayCell(var2.asLong(), AiPathfinder.CellInfo.air());
               }

               for (BlockPos var8 : this.incomingMove.cellsToPlace()) {
                  this.putOverlayCell(var8.asLong(), PLACED_SOLID_CELL);
               }

               if (this.incomingMove.type() == AiPathfinder.MoveType.DOOR) {
                  this.putOverlayCell(this.incomingMove.to().asLong(), AiPathfinder.CellInfo.air());
                  this.putOverlayCell(this.incomingMove.to().above().asLong(), AiPathfinder.CellInfo.air());
               }
            }

            if (this.activeNode != null) {
               int var7 = 0;

               for (NavigationOpenSet.Inner_DataUtil var9 = this.activeNode.parent;
                  var9 != null && var9.move != null && var7 < 6;
                  var7++
               ) {
                  AiPathfinder.PathStep var3 = var9.move;

                  for (BlockPos var5 : var3.cellsToPlace()) {
                     this.putOverlayCell(var5.asLong(), PLACED_SOLID_CELL);
                  }

                  for (BlockPos var11 : var3.cellsToBreak()) {
                     this.putOverlayCell(var11.asLong(), AiPathfinder.CellInfo.air());
                  }

                  var9 = var9.parent;
               }
            }
         }

         private static long overlayBloomBit(long var0) {
            return 1L << (int)(var0 * -7046029254386353131L >>> 58);
         }

         private void putOverlayCell(long var1, AiPathfinder.CellInfo var3) {
            this.overlayBloomMask = this.overlayBloomMask | overlayBloomBit(var1);
            if (this.overlayCount == this.overlayKeys.length) {
               this.overlayKeys = Arrays.copyOf(this.overlayKeys, this.overlayCount * 2);
               this.overlayValues = Arrays.copyOf(this.overlayValues, this.overlayCount * 2);
            }

            this.overlayKeys[this.overlayCount] = var1;
            this.overlayValues[this.overlayCount++] = var3;
         }

         public AiPathfinder.CellInfo probePos(BlockPos var1) {
            return this.probe(var1.getX(), var1.getY(), var1.getZ());
         }

         @Override
         public AiPathfinder.CellInfo probe(int var1, int var2, int var3) {
            if (!this.overlayValid
               || this.incomingMove != this.overlayIncomingMove
               || this.activeNode != this.overlayNode) {
               this.rebuildEditOverlay();
            }

            long var4 = BlockPos.asLong(var1, var2, var3);
            if ((this.overlayBloomMask & overlayBloomBit(var4)) != 0L) {
               for (int var6 = 0; var6 < this.overlayCount; var6++) {
                  if (this.overlayKeys[var6] == var4) {
                     return this.overlayValues[var6];
                  }
               }
            }

            if (this.blockQueries >= this.cacheKeys.length && this.cacheKeys.length < this.maxCacheCapacity) {
               this.growCellCache();
            }

            int var8 = (int)HashCommon.mix(var4) & this.cacheKeys.length - 1;
            if (this.cacheValues[var8] != null && this.cacheKeys[var8] == var4) {
               return this.cacheValues[var8];
            }

            this.blockQueries++;
            AiPathfinder.CellInfo var7 = this.delegate.probe(var1, var2, var3);
            if (var7 == null) {
               this.queryFlags |= 1;
               return AiPathfinder.CellInfo.UNLOADED;
            }

            if (this.ignoreCachedCells && var7.cached()) {
               var7 = AiPathfinder.CellInfo.UNLOADED;
            }

            this.cacheKeys[var8] = var4;
            this.cacheValues[var8] = var7;
            return var7;
         }

         boolean hasActiveEdits() {
            if (!this.overlayValid
               || this.incomingMove != this.overlayIncomingMove
               || this.activeNode != this.overlayNode) {
               this.rebuildEditOverlay();
            }

            return this.overlayCount != 0;
         }

         @Override
         public short putAt_S_I(int var1, int var2, int var3) {
            if (!this.overlayValid
               || this.incomingMove != this.overlayIncomingMove
               || this.activeNode != this.overlayNode) {
               this.rebuildEditOverlay();
            }

            long var4 = BlockPos.asLong(var1, var2, var3);
            if ((this.overlayBloomMask & overlayBloomBit(var4)) != 0L) {
               for (int var6 = 0; var6 < this.overlayCount; var6++) {
                  if (this.overlayKeys[var6] == var4) {
                     return AiShapes.shapeIdOf(this.overlayValues[var6]);
                  }
               }
            }

            return this.ignoreCachedCells && !this.probe(var1, var2, var3).loaded() ? 1 : this.delegate.putAt_S_I(var1, var2, var3);
         }

         @Override
         public short fill_S(int var1, int var2, int var3) {
            if (!this.overlayValid
               || this.incomingMove != this.overlayIncomingMove
               || this.activeNode != this.overlayNode) {
               this.rebuildEditOverlay();
            }

            long var4 = BlockPos.asLong(var1, var2, var3);
            if ((this.overlayBloomMask & overlayBloomBit(var4)) != 0L) {
               for (int var6 = 0; var6 < this.overlayCount; var6++) {
                  if (this.overlayKeys[var6] == var4) {
                     return AiShapes.shapeIdOf(this.overlayValues[var6]);
                  }
               }
            }

            return this.delegate.fill_S(var1, var2, var3);
         }

         private record JumpCacheKey(long from, long to, long approach, boolean sprint) {
         }

         private record CachedJump(AiJumpSolver.JumpPlan plan, int x, int y, int z) {
         }
      }

      private record JumpOffset(int x, int z, int distance, int stepX, int stepZ, double length, double reach, double cornerReach, int exits) {
      }
   }

   public static final class SearchSession {
      public static final int DEFAULT_HORIZ_RADIUS = 64;
      public static final int DEFAULT_VERT_RADIUS = 40;
      public static final int MAX_EXPANSIONS = 2000000;
      private static final int MAX_OPEN_NODES = 3000000;
      static final double[] PROGRESS_WEIGHTS = new double[]{1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 10.0};
      static final int MIN_PROGRESS_DIST = 5;
      private final NavigationOpenSet.Inner_DataUtil[] bestWeightedNodes = new NavigationOpenSet.Inner_DataUtil[PROGRESS_WEIGHTS.length];
      private final double[] bestWeightedScores = new double[PROGRESS_WEIGHTS.length];
      private final AiPathfinder.GridView grid;
      private final BlockPos startPos;
      private final AiPathfinder.PathGoal goal;
      private final AiPathfinder.PathOptions options;
      private final Set<Long> blacklistedCells;
      private final NavigationOpenSet openSet = new NavigationOpenSet();
      private final Long2ObjectOpenHashMap<NavigationOpenSet.Inner_DataUtil> nodeTable = new Long2ObjectOpenHashMap<>(256);
      private final AiPathfinder.MoveGenerator.CachedGrid cachedGrid;
      private final List<AiPathfinder.PathStep> scratchMoves = new ArrayList<>(16);
      private NavigationOpenSet.Inner_DataUtil bestNode;
      private final long gridRevision;
      private int expandedCount;
      private int generatedCount;
      private int peakOpenSize;
      private int allocatedNodeCount = 1;
      private boolean finished;
      private boolean reachedGoal;
      private boolean hitPendingChunk;
      private boolean wedgedStart;
      private boolean confinedStart;
      private int horizRadius = 64;
      private int vertRadius = 40;
      private BlockPos corridorTarget;
      private boolean slopedCorridorFloor;
      private static final int CORRIDOR_MARGIN = 24;
      private static final int CORRIDOR_FLOOR_PAD = 14;
      private boolean routeProofMode;
      private boolean destinationUnknown;
      private boolean hitSearchBoxLimit;
      private boolean worldRecordMode;
      private boolean trackHeadingState;
      private AiPathfinder.PathBounds searchBounds;
      private NavigationOpenSet.Inner_DataUtil bestUnknownEdgeNode;
      private PriorityQueue<AiPathfinder.SearchSession.QueuedJump> deferredJumpQueue;
      private final Map<AiPathfinder.SearchSession.QueuedJump, Future<AiPathfinder.SearchSession.JumpSolveStats>> pendingJumpFutures = new HashMap<>();
      private final ConcurrentLinkedQueue<AiPathfinder.MoveGenerator.CachedGrid> workerGridPool = new ConcurrentLinkedQueue<>();
      private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
      private static final int JUMP_WORKER_THREADS = Math.clamp(CPU_CORES >= 10 ? CPU_CORES - 2 : CPU_CORES / 2, 1, 10);
      private static final ExecutorService JUMP_PROOF_EXECUTOR = Executors.newFixedThreadPool(JUMP_WORKER_THREADS, var0 -> {
         Thread var1 = new Thread(var0, "Autism jump proof");
         var1.setDaemon(true);
         var1.setPriority(4);
         return var1;
      });
      private int asyncWorkerLimit;
      Set<AiPathfinder.MoveKey> blacklistedMoves = Set.of();
      private boolean ignoreGlobalBlacklist;
      private long queuedJumpSeq;
      private static final double MAX_ESTIMATED_JUMP_TICKS = 18.0;
      private boolean exhaustedSearch;

      static AiPathfinder.SearchSession createProofSession(
         AiPathfinder.GridView var0, BlockPos var1, AiPathfinder.PathGoal var2, AiPathfinder.PathOptions var3, Set<Long> var4
      ) {
         return createProofSession(var0, var1, var2, var3, var4, false);
      }

      static AiPathfinder.SearchSession createProofSession(
         AiPathfinder.GridView var0, BlockPos var1, AiPathfinder.PathGoal var2, AiPathfinder.PathOptions var3, Set<Long> var4, boolean var5
      ) {
         return createProofSession(var0, var1, var2, var3, var4, var5, true, var0.movementSpeed());
      }

      private static AiPathfinder.SearchSession createProofSession(
         AiPathfinder.GridView var0,
         BlockPos var1,
         AiPathfinder.PathGoal var2,
         AiPathfinder.PathOptions var3,
         Set<Long> var4,
         boolean var5,
         boolean var6,
         double var7
      ) {
         AiPathfinder.SearchSession var9 = new AiPathfinder.SearchSession(var0, var1, var2, var3, var4, var7);
         var9.routeProofMode = true;
         var9.cachedGrid.ignoreCachedCells = true;
         var9.cachedGrid.fullPhysicsParkour = var6;
         var9.cachedGrid.fastPassMode = var5;
         if (var6
            && !var5
            && !var3.allowDig()
            && !var3.allowBridge()
            && !var3.allowPillar()
            && (var0 instanceof AiLiveGrid || var0 instanceof AiPathfinder.LiveGridView || var0.putAt_Z())) {
            var9.asyncWorkerLimit = JUMP_WORKER_THREADS;
            var9.cachedGrid.localJumpCache = new ConcurrentHashMap<>();
         }

         if (var5) {
            var9.openSet.setWeight(2.0);
         }

         BlockPos var10 = var2.goalTarget();
         AiPathfinder.CellInfo var11 = var10 == null ? null : var0.probe(var10.getX(), var10.getY(), var10.getZ());
         var9.destinationUnknown = var10 != null && var11 != null && (!var11.loaded() || var11.cached());
         if (var10 != null && var11 == null) {
            var9.hitPendingChunk = true;
         }

         if (AiNavDebug.isEnabled()) {
            AiNavDebug.log("route proof target=" + var10 + " unknown=" + var9.destinationUnknown + " grid=" + var0.getClass().getSimpleName() + " destination=" + var11);
         }

         var9.horizRadius = var10 == null
            ? 512
            : (
               var9.destinationUnknown
                  ? 2048
                  : (int)Math.min(2048L, Math.max(128L, Math.max(Math.abs((long)var10.getX() - var1.getX()), Math.abs((long)var10.getZ() - var1.getZ())) + 64L))
            );
         var9.vertRadius = var10 == null ? 512 : (int)Math.min(512L, Math.max(96L, Math.abs((long)var10.getY() - var1.getY()) + 32L));
         return var9;
      }

      private boolean isAtUnknownTerrainEdge(BlockPos var1) {
         AiPathfinder.CellInfo var2 = this.cachedGrid.probePos(var1);
         AiPathfinder.CellInfo var3 = this.cachedGrid.probePos(var1.below());
         if (AiPathfinder.MoveGenerator.isStandable(var2, this.cachedGrid.probePos(var1.above()), var3)
            && !var2.water()
            && !var2.climbable()
            && var3.top16() <= 16) {
            BlockPos var4 = this.goal.goalTarget();
            int var5 = Integer.compare(var4.getX(), var1.getX());
            int var6 = Integer.compare(var4.getZ(), var1.getZ());

            for (int[] var10 : AiPathfinder.MoveGenerator.CARDINAL_DIRS) {
               if (var10[0] * var5 + var10[1] * var6 > 0) {
                  AiPathfinder.CellInfo var11 = this.grid.probe(var1.getX() + var10[0], var1.getY(), var1.getZ() + var10[1]);
                  if (var11 == null) {
                     this.hitPendingChunk = true;
                  } else {
                     if (!var11.loaded() || var11.cached()) {
                        return true;
                     }

                     if (var11.passable() && var11.hazardFeet()) {
                        AiPathfinder.CellInfo var12 = this.grid
                           .probe(var1.getX() + 2 * var10[0], var1.getY(), var1.getZ() + 2 * var10[1]);
                        if (var12 == null) {
                           this.hitPendingChunk = true;
                        } else if (!var12.loaded() || var12.cached()) {
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

      public SearchSession(AiPathfinder.GridView var1, BlockPos var2, AiPathfinder.PathGoal var3, AiPathfinder.PathOptions var4, Set<Long> var5) {
         this(var1, var2, var3, var4, var5, var1.movementSpeed());
      }

      private SearchSession(
         AiPathfinder.GridView var1, BlockPos var2, AiPathfinder.PathGoal var3, AiPathfinder.PathOptions var4, Set<Long> var5, double var6
      ) {
         this.grid = var1;
         this.startPos = var2.immutable();
         this.goal = var3.putAt_FileStore_TokenCod_FileStore_TextStor(var1);
         this.options = var4;
         this.blacklistedCells = var5;
         this.gridRevision = var1.revision();
         this.cachedGrid = new AiPathfinder.MoveGenerator.CachedGrid(var1, 16384, var6);
         this.cachedGrid.fallPolicy = var4.fallPolicy();
         BlockPos var8 = var3.goalTarget();
         if (var8 != null && var4.allowPillar() && var4.strategy() == AiPathfinder.RouteStrategy.FASTEST) {
            int var9 = var8.getY() - var2.getY();
            double var10 = Math.hypot(var8.getX() - var2.getX(), var8.getZ() - var2.getZ());
            if (var9 >= 8 && var10 * 2.0 < var9 && var4.constructionBlocks() >= var9) {
               this.openSet.setWeight(1.3);
            }
         }

         if (var4.allowParkour()) {
            this.deferredJumpQueue = new PriorityQueue<>(
               Comparator.comparingDouble(AiPathfinder.SearchSession.QueuedJump::priority).thenComparingLong(AiPathfinder.SearchSession.QueuedJump::order)
            );
            this.cachedGrid.deferredJumpSink = this::enqueueDeferredJump;
         }

         this.bestNode = new NavigationOpenSet.Inner_DataUtil(this.startPos, var3.heuristic(var2.getX(), var2.getY(), var2.getZ()));
         this.bestNode.cost = 0.0;
         this.nodeTable.put(this.startPos.asLong(), this.bestNode);
         this.openSet.insertOrUpdate(this.bestNode);
         this.peakOpenSize = 1;
         Arrays.fill(this.bestWeightedScores, 1.7976931348623157E308);
      }

      public boolean hasProgressNode() {
         return this.selectProgressNode() != null;
      }

      private NavigationOpenSet.Inner_DataUtil selectProgressNode() {
         for (NavigationOpenSet.Inner_DataUtil var4 : this.bestWeightedNodes) {
            if (var4 != null && var4.pos.distSqr(this.startPos) > 25.0) {
               return var4;
            }
         }

         return this.bestNode != null
               && this.bestNode.parent != null
               && this.bestNode.pos.distSqr(this.startPos) > 25.0
            ? this.bestNode
            : null;
      }

      private static double estimateJumpTicks(AiPathfinder.MoveGenerator.JumpCandidate var0) {
         double var1 = Math.hypot(var0.to().getX() - var0.from().getX(), var0.to().getZ() - var0.from().getZ());
         double var3 = Math.max(0.0, var0.targetY() - var0.sourceY());
         return Math.min(18.0, 2.0 + Math.max(8.0, var1 * 2.7 + var3 * 2.0));
      }

      private void enqueueDeferredJump(AiPathfinder.MoveGenerator.JumpCandidate var1) {
         NavigationOpenSet.Inner_DataUtil var2 = this.cachedGrid.activeNode;
         BlockPos var3 = var1.to();
         BlockPos var4 = var1.deeper();
         if (this.corridorTarget == null && this.searchBounds == null || this.isWithinSearchBox(var3) || this.isWithinSearchBox(var4)) {
            if (this.blacklistedCells.contains(var4.asLong())
               || !AiPathfinder.MoveGenerator.isFlatWalkableCell(this.cachedGrid, var4, var1.targetY())
               || Math.abs(
                     AiPathfinder.MoveGenerator.surfaceY(var4.getY(), this.cachedGrid.probePos(var4.below()))
                        - var1.targetY()
                  )
                  >= 1.0E-6) {
               var4 = null;
            }

            double var5 = this.optimisticJumpCost(var1);
            if (!this.cachedGrid.quickJumpMode
               || !this.isJumpDominated(var2, var3, var2.cost + var5)
               || var4 != null && !this.isJumpDominated(var2, var4, var2.cost + var5)) {
               double var7 = this.adjustedHeuristic(var3.getX(), var3.getY(), var3.getZ());
               if (var4 != null) {
                  var7 = Math.min(var7, this.adjustedHeuristic(var4.getX(), var4.getY(), var4.getZ()));
               }

               double var9 = this.openSet.priority(var2.cost + var5, var7);
               this.deferredJumpQueue.add(new AiPathfinder.SearchSession.QueuedJump(var1, var4, var2, var2.cost, var9, this.queuedJumpSeq++));
            }
         }
      }

      private double optimisticJumpCost(AiPathfinder.MoveGenerator.JumpCandidate var1) {
         double var2 = estimateJumpTicks(var1);
         if (this.cachedGrid.quickJumpMode) {
            double var4 = var1.targetY() - var1.sourceY();
            var2 = Math.max(var2, 7.0 + Math.sqrt(Math.max(0.0, 1.25 - var4) * 22.0));
         }

         return var2;
      }

      private double adjustedHeuristic(int var1, int var2, int var3) {
         double var4 = this.goal.heuristic(var1, var2, var3);
         BlockPos var6 = this.goal.goalTarget();
         if ((this.cachedGrid.quickJumpMode || this.corridorTarget != null || this.searchBounds != null)
            && var6 != null
            && var2 > var6.getY() + 1) {
            double var7 = var2 - var6.getY() - 1.0;
            double var9 = Math.min(var7 * 4.0, Math.sqrt(var7 * 26.0));
            return Math.max(0.0, var4 - var7 * 4.0 + var9);
         } else {
            return var4;
         }
      }

      private boolean isWithinSearchBox(BlockPos var1) {
         int var2 = var1.getX();
         int var3 = var1.getY();
         int var4 = var1.getZ();
         return Math.abs((long)var2 - this.startPos.getX()) <= this.horizRadius
            && Math.abs((long)var4 - this.startPos.getZ()) <= this.horizRadius
            && Math.abs((long)var3 - this.startPos.getY()) <= this.vertRadius
            && (this.searchBounds == null || this.searchBounds.contains(var2, var3, var4))
            && (
               this.corridorTarget == null
                  || withinRangeMargin(var2, this.startPos.getX(), this.corridorTarget.getX(), 24)
                     && withinRangeMargin(var4, this.startPos.getZ(), this.corridorTarget.getZ(), 24)
                     && var3 <= Math.max(this.startPos.getY(), this.corridorTarget.getY()) + 14
                     && var3 >= this.corridorMinY(var2, var4)
            );
      }

      private double corridorMinY(int var1, int var2) {
         if (!this.slopedCorridorFloor) {
            return Math.min(this.startPos.getY(), this.corridorTarget.getY()) - 14;
         }

         double var3 = (double)this.corridorTarget.getX() - this.startPos.getX();
         double var5 = (double)this.corridorTarget.getZ() - this.startPos.getZ();
         double var7 = Math.clamp(
            ((var1 - this.startPos.getX()) * var3 + (var2 - this.startPos.getZ()) * var5) / (var3 * var3 + var5 * var5), 0.0, 1.0
         );
         return this.startPos.getY() + var7 * (this.corridorTarget.getY() - this.startPos.getY()) - 8.0;
      }

      private static boolean withinRangeMargin(int var0, int var1, int var2, int var3) {
         return var0 >= Math.min(var1, var2) - var3 && var0 <= Math.max(var1, var2) + var3;
      }

      private boolean isDominatedAt(BlockPos var1, double var2, int var4) {
         for (NavigationOpenSet.Inner_DataUtil var5 = this.nodeTable.get(var1.asLong()); var5 != null; var5 = var5.nextInCell) {
            if (!var5.closed && var5.cost <= var2 && var5.load_I <= var4) {
               return true;
            }
         }

         return false;
      }

      private boolean isJumpDominated(NavigationOpenSet.Inner_DataUtil var1, BlockPos var2, double var3) {
         return this.isDominatedAt(var2, var3, var1.load_I)
            ? true
            : this.cachedGrid.fallPolicy == AiPathfinder.FallPolicy.STANDARD
               && !this.cachedGrid.fastPassMode
               && this.isDominatedAt(var2, var3 + 20.0, var1.load_I)
               && AiPathfinder.MoveGenerator.hasDeepDropUnderJump(this.cachedGrid, var1.pos, var2);
      }

      private boolean stepDeferredJump() {
         if (this.deferredJumpQueue != null
            && !this.deferredJumpQueue.isEmpty()
            && !(this.deferredJumpQueue.peek().priority() > this.openSet.peekPriority())) {
            AiPathfinder.SearchSession.QueuedJump var1 = this.deferredJumpQueue.remove();
            NavigationOpenSet.Inner_DataUtil var2 = var1.source();
            if (!var2.closed && var2.cost == var1.sourceCost()) {
               double var3 = this.deferredJumpLowerBound(var1);
               this.cachedGrid.queryFlags = 0;
               this.cachedGrid.incomingMove = var2.move;
               this.cachedGrid.activeNode = var2;
               if (!this.isJumpDominated(var2, var1.candidate().to(), var3)
                  || var1.continuation() != null && !this.isJumpDominated(var2, var1.continuation(), var3)) {
                  AiPathfinder.PathStep var5 = this.resolveQueuedJump(var1);
                  if (var5 != null) {
                     this.generatedCount++;
                     this.relaxEdge(var2, var5);
                     AiPathfinder.PathStep var6 = AiPathfinder.MoveGenerator.tryDoubleJumpChain(
                        this.cachedGrid, var5, this.blacklistedCells
                     );
                     if (var6 != null) {
                        this.generatedCount++;
                        this.relaxEdge(var2, var6);
                     }
                  }

                  if ((this.cachedGrid.queryFlags & 1) != 0) {
                     this.hitPendingChunk = true;
                  }

                  return true;
               } else {
                  this.cancelQueuedJumpFuture(var1);
                  if ((this.cachedGrid.queryFlags & 1) != 0) {
                     this.hitPendingChunk = true;
                  }

                  return true;
               }
            } else {
               this.cancelQueuedJumpFuture(var1);
               return true;
            }
         } else {
            return false;
         }
      }

      private double deferredJumpLowerBound(AiPathfinder.SearchSession.QueuedJump var1) {
         return var1.sourceCost()
            + (this.worldRecordMode || !this.cachedGrid.fastPassMode && !this.cachedGrid.reducedJumpMode ? 3.0 : this.optimisticJumpCost(var1.candidate()));
      }

      private AiPathfinder.PathStep resolveQueuedJump(AiPathfinder.SearchSession.QueuedJump var1) {
         if (this.asyncWorkerLimit >= 2 && !this.destinationUnknown && !this.cachedGrid.hasActiveEdits()) {
            var var2 = this.pendingJumpFutures.computeIfAbsent(var1, this::submitAsyncJumpSolve);
            if (this.pendingJumpFutures.size() < this.asyncWorkerLimit * 2) {
               int var3 = 0;

               for (AiPathfinder.SearchSession.QueuedJump var5 : this.deferredJumpQueue) {
                  if (++var3 > this.asyncWorkerLimit * 8 || this.pendingJumpFutures.size() >= this.asyncWorkerLimit * 2) {
                     break;
                  }

                  NavigationOpenSet.Inner_DataUtil var6 = var5.source();
                  if (!var6.closed && var6.cost == var5.sourceCost() && !this.pendingJumpFutures.containsKey(var5)) {
                     double var7 = this.deferredJumpLowerBound(var5);
                     if ((
                           !this.isDominatedAt(var5.candidate().to(), var7, var6.load_I)
                              || var5.continuation() != null && !this.isDominatedAt(var5.continuation(), var7, var6.load_I)
                        )
                        && (var6.move == null || var6.move.type() != AiPathfinder.MoveType.DOOR)) {
                        this.pendingJumpFutures.computeIfAbsent(var5, this::submitAsyncJumpSolve);
                     }
                  }
               }
            }

            try {
               AiPathfinder.SearchSession.JumpSolveStats var16 = (AiPathfinder.SearchSession.JumpSolveStats)var2.get();
               this.cachedGrid.queryFlags = this.cachedGrid.queryFlags | var16.flags();
               this.cachedGrid.jumpSolves = this.cachedGrid.jumpSolves + var16.solves();
               this.cachedGrid.jumpCacheHits = this.cachedGrid.jumpCacheHits + var16.hits();
               this.cachedGrid.jumpSolveNanos = this.cachedGrid.jumpSolveNanos + var16.nanos();
               this.cachedGrid.blockQueries = this.cachedGrid.blockQueries + var16.queries();
               return var16.movement();
            } catch (InterruptedException var13) {
               var2.cancel(true);
               Thread.currentThread().interrupt();
               this.finished = true;
               return null;
            } catch (ExecutionException var14) {
               return AiPathfinder.MoveGenerator.solveJumpCandidate(this.cachedGrid, this.blacklistedCells, var1.candidate());
            } finally {
               this.pendingJumpFutures.remove(var1);
            }
         } else {
            return AiPathfinder.MoveGenerator.solveJumpCandidate(this.cachedGrid, this.blacklistedCells, var1.candidate());
         }
      }

      private Future<AiPathfinder.SearchSession.JumpSolveStats> submitAsyncJumpSolve(AiPathfinder.SearchSession.QueuedJump var1) {
         AiPathfinder.PathStep var2 = var1.source().move;
         return JUMP_PROOF_EXECUTOR.submit(
            () -> {
               AiPathfinder.MoveGenerator.CachedGrid var3 = this.workerGridPool.poll();
               if (var3 == null) {
                  var3 = new AiPathfinder.MoveGenerator.CachedGrid(this.grid, 16384, this.cachedGrid.movementSpeed);
                  var3.ignoreCachedCells = true;
                  var3.fullPhysicsParkour = true;
                  var3.reducedJumpMode = this.cachedGrid.reducedJumpMode;
                  var3.allowManeuverShortcuts = this.cachedGrid.allowManeuverShortcuts;
                  var3.quickJumpMode = this.cachedGrid.quickJumpMode;
                  var3.fallPolicy = this.cachedGrid.fallPolicy;
                  var3.fastPassMode = this.cachedGrid.fastPassMode;
                  var3.limitJumpDropDepth = this.cachedGrid.limitJumpDropDepth;
                  var3.localJumpCache = this.cachedGrid.localJumpCache;
               }

               var3.incomingMove = var2;
               var3.queryFlags = var3.jumpSolves = var3.jumpCacheHits = 0;
               var3.jumpSolveNanos = var3.blockQueries = 0L;

               try {
                  AiPathfinder.PathStep var4 = AiPathfinder.MoveGenerator.solveJumpCandidate(
                     var3, this.blacklistedCells, var1.candidate()
                  );
                  return new AiPathfinder.SearchSession.JumpSolveStats(var4, var3.queryFlags, var3.jumpSolves, var3.jumpCacheHits, var3.jumpSolveNanos, var3.blockQueries);
               } finally {
                  if (!Thread.currentThread().isInterrupted()) {
                     this.workerGridPool.offer(var3);
                  }
               }
            }
         );
      }

      private void cancelQueuedJumpFuture(AiPathfinder.SearchSession.QueuedJump var1) {
         Future var2 = this.pendingJumpFutures.remove(var1);
         if (var2 != null) {
            var2.cancel(true);
         }
      }

      private void cancelAllJumpFutures() {
         for (Future var2 : this.pendingJumpFutures.values()) {
            var2.cancel(true);
         }

         this.pendingJumpFutures.clear();
      }

      public boolean step(int var1) {
         if (var1 <= 0) {
            throw new IllegalArgumentException("Expansion budget must be positive");
         }

         for (int var2 = 0; var2 < var1 && !this.finished; var2++) {
            if ((var2 & 63) == 0) {
               if (Thread.currentThread().isInterrupted()) {
                  this.finished = true;
                  break;
               }

               if (this.grid.revision() != this.gridRevision) {
                  this.hitPendingChunk = true;
                  this.finished = true;
                  break;
               }
            }

            boolean var3 = this.deferredJumpQueue != null && !this.deferredJumpQueue.isEmpty();
            if (!var3 && this.openSet.isEmpty() || this.expandedCount >= 2000000 || this.allocatedNodeCount + (var3 ? this.deferredJumpQueue.size() : 0) >= 3000000) {
               this.exhaustedSearch = this.exhaustedSearch | (var3 || !this.openSet.isEmpty());
               this.finished = true;
               break;
            }

            if (!this.stepDeferredJump()) {
               NavigationOpenSet.Inner_DataUtil var4 = this.openSet.poll();
               if (!var4.closed) {
                  this.expandedCount++;
                  BlockPos var5 = var4.pos;
                  if (var4.heuristic < this.bestNode.heuristic) {
                     this.bestNode = var4;
                  }

                  this.cachedGrid.incomingMove = var4.move;
                  this.cachedGrid.activeNode = var4;
                  if (this.goal.goalReached(var5.getX(), var5.getY(), var5.getZ(), this.cachedGrid)) {
                     this.bestNode = var4;
                     this.reachedGoal = true;
                     this.finished = true;
                     break;
                  }

                  if (this.routeProofMode && this.destinationUnknown && this.isAtOrCloserThanStart(var5, var4.heuristic) && this.isAtUnknownTerrainEdge(var5)) {
                     this.bestUnknownEdgeNode = var4;
                     this.finished = true;
                     break;
                  }

                  if (!this.routeProofMode
                     && this.goal.outsideSearchBox(this.startPos, 64, 40)
                     && var4.heuristic
                        < this.goal.heuristic(this.startPos.getX(), this.startPos.getY(), this.startPos.getZ())
                     && (
                        Math.abs(var5.getX() - this.startPos.getX()) == 64
                           || Math.abs(var5.getZ() - this.startPos.getZ()) == 64
                           || Math.abs(var5.getY() - this.startPos.getY()) == 40
                     )) {
                     this.bestNode = var4;
                     this.finished = true;
                     break;
                  }

                  this.scratchMoves.clear();
                  int var6 = AiPathfinder.MoveGenerator.generateMovesCached(
                     this.cachedGrid, this.options, this.blacklistedCells, var5, this.scratchMoves
                  );
                  if ((var6 & 1) != 0) {
                     this.hitPendingChunk = true;
                  }

                  if ((var6 & 2) != 0 && var5.equals(this.startPos)) {
                     this.wedgedStart = true;
                  }

                  if ((var6 & 4) != 0 && var5.equals(this.startPos)) {
                     this.confinedStart = true;
                  }

                  this.generatedCount = this.generatedCount + this.scratchMoves.size();

                  for (AiPathfinder.PathStep var8 : this.scratchMoves) {
                     this.relaxEdge(var4, var8);
                  }
               }
            }
         }

         if (this.finished) {
            this.cancelAllJumpFutures();
         }

         return this.finished;
      }

      private boolean isAtOrCloserThanStart(BlockPos var1, double var2) {
         if (var1.equals(this.startPos)) {
            return true;
         }

         double var4 = this.goal.heuristic(this.startPos.getX(), this.startPos.getY(), this.startPos.getZ());
         if (var2 < var4) {
            return true;
         }

         BlockPos var6 = this.goal.goalTarget();
         return var2 == var4
            && var1.distToCenterSqr(var6.getX() + 0.5, var6.getY() + 0.5, var6.getZ() + 0.5)
               < this.startPos.distToCenterSqr(var6.getX() + 0.5, var6.getY() + 0.5, var6.getZ() + 0.5);
      }

      private void relaxEdge(NavigationOpenSet.Inner_DataUtil var1, AiPathfinder.PathStep var2) {
         if ((this.blacklistedMoves.isEmpty() || !this.blacklistedMoves.contains(AiPathfinder.MoveKey.of(var2))) && (this.ignoreGlobalBlacklist || !AiPathfinder.isGloballyBlacklisted(var2))) {
            boolean var3 = false;

            for (BlockPos var5 : var2.cellsToBreak()) {
               if (!this.goal.fill_Z_BlockPos(var5)) {
                  var3 = true;
                  break;
               }
            }

            for (BlockPos var21 : var2.cellsToPlace()) {
               if (!this.goal.readEx_Z_BlockPos(var21)) {
                  var3 = true;
                  break;
               }
            }

            if (!var3) {
               BlockPos var20 = var2.to();
               if (this.goal.cellAllowed(var20)) {
                  if (!this.isWithinSearchBox(var20)) {
                     if (this.routeProofMode) {
                        this.hitSearchBoxLimit = true;
                        this.exhaustedSearch = true;
                     }
                  } else {
                     double var22 = (
                           this.options.strategy() == AiPathfinder.RouteStrategy.SAFEST
                              ? this.options.threats().surcharge(var20.getX(), var20.getY(), var20.getZ())
                                 + this.options
                                    .threats()
                                    .moveSurcharge(var2.from().getX(), var2.from().getY(), var2.from().getZ(), var20.getX(), var20.getY(), var20.getZ())
                              : 0.0
                        )
                        + this.options.threats().cellSurcharge(var20.getX(), var20.getY(), var20.getZ());
                     double var10000;
                     if (this.routeProofMode && this.options.allowParkour()) {
                        switch (var2.type()) {
                           case WALK:
                           case WALK_DIAGONAL:
                           case SPRINT:
                           case SPRINT_DIAGONAL:
                           case STEP_UP_1:
                              var10000 = var2.costTicks() * Math.min(1.0, 0.1 / this.cachedGrid.movementSpeed);
                              break;
                           case JUMP_UP_1:
                           case DESCEND_1:
                           case DESCEND_2:
                           case DESCEND_3:
                           case DESCEND_SOFT:
                           case DIG_DOWN_1:
                           case DIG_TUNNEL:
                           case PILLAR:
                           case BRIDGE_CELL:
                           case CLIMB_1:
                           case DOOR:
                           case JUMP_GAP_2:
                           case JUMP_GAP_3:
                           case PARKOUR_ASCEND:
                           default:
                              var10000 = var2.costTicks();
                              break;
                           case SPRINT_JUMP:
                              var10000 = var2.costTicks() * AiPathfinder.MoveCosts.speedTickMultiplier(this.cachedGrid.movementSpeed);
                        }
                     } else {
                        var10000 = var2.costTicks();
                     }

                     double var7 = var10000;
                     if (this.worldRecordMode) {
                        var7 += AiPathfinder.effectiveMoveTicks(var2) - var2.costTicks();
                     }

                     double var9 = var1.cost
                        + var7
                        + var22
                        + var2.cellsToPlace().size() * this.goal.placeCost()
                        + AiPathfinder.MoveCosts.avoidPlacementPenalty(var2.cellsToPlace());
                     if (this.worldRecordMode) {
                        var9 += AiPathfinder.transitionPenalty(var1.move, var2);
                     }

                     for (BlockPos var12 : var2.cellsToPlace()) {
                        if (AiEditLedger.wasRecentlyBroken(var12)) {
                           var9 += 200.0;
                        }
                     }

                     for (BlockPos var25 : var2.cellsToBreak()) {
                        if (AiEditLedger.load_Z(var25)) {
                           var9 += 60.0;
                        }

                        if (this.goal.breakIsUseful(var25)) {
                           double var13 = this.cachedGrid.probePos(var25).breakTicks();
                           if (Double.isFinite(var13)) {
                              var9 -= var13;
                           }
                        }
                     }

                     if (var9 < var1.cost + 1.0) {
                        var9 = var1.cost + 1.0;
                     }

                     if (Double.isFinite(var9) && !(var2.costTicks() <= 0.0) && !(var22 < 0.0)) {
                        int var24 = var1.load_I + var2.cellsToPlace().size();
                        if (var24 <= this.options.constructionBlocks()) {
                           NavigationOpenSet.Inner_DataUtil var26 = this.nodeTable.get(var20.asLong());
                           NavigationOpenSet.Inner_DataUtil var27 = null;
                           boolean var14 = false;
                           int var15 = this.trackHeadingState ? AiPathfinder.headingBucket(var2) : 0;

                           for (NavigationOpenSet.Inner_DataUtil var16 = var26; var16 != null; var16 = var16.nextInCell) {
                              if (!var16.closed && (!this.trackHeadingState || AiPathfinder.headingBucket(var16.move) == var15)) {
                                 if (var16.load_I <= var24 && var16.cost <= var9) {
                                    var14 = true;
                                    break;
                                 }

                                 if (var16.load_I == var24) {
                                    var27 = var16;
                                 }
                              }
                           }

                           if (!var14) {
                              if (var27 == null) {
                                 var27 = new NavigationOpenSet.Inner_DataUtil(var20, this.adjustedHeuristic(var20.getX(), var20.getY(), var20.getZ()));
                                 var27.load_I = var24;
                                 var27.nextInCell = var26;
                                 this.nodeTable.put(var20.asLong(), var27);
                                 this.allocatedNodeCount++;
                              }

                              for (NavigationOpenSet.Inner_DataUtil var28 = var26; var28 != null; var28 = var28.nextInCell) {
                                 if (var28 != var27
                                    && var28.load_I >= var24
                                    && var28.cost >= var9
                                    && (!this.trackHeadingState || AiPathfinder.headingBucket(var28.move) == var15)) {
                                    var28.closed = true;
                                 }
                              }

                              if (var9 < var27.cost) {
                                 var27.cost = var9;
                                 var27.parent = var1;
                                 var27.move = var2;
                                 this.openSet.insertOrUpdate(var27);
                                 this.peakOpenSize = Math.max(this.peakOpenSize, this.openSet.size());

                                 for (int var29 = 0; var29 < PROGRESS_WEIGHTS.length; var29++) {
                                    double var17 = var27.heuristic + var9 / PROGRESS_WEIGHTS[var29];
                                    if (var17 < this.bestWeightedScores[var29]) {
                                       this.bestWeightedScores[var29] = var17;
                                       this.bestWeightedNodes[var29] = var27;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      public AiPathfinder.PathResult resultCompat() { return this.result(); }
      public boolean hasProgressNodeCompat() { return this.hasProgressNode(); }
      public boolean stepCompat(int budget) { return this.step(budget); }
      public void finishCompat() { this.finish(); }

            public AiPathfinder.PathResult result() {
         if (!this.finished) {
            throw new IllegalStateException("Search still running");
         }

         if (this.routeProofMode && !this.reachedGoal && this.bestUnknownEdgeNode == null) {
            if (AiNavDebug.isEnabled()) {
               AiNavDebug.log(
                  "unproved route nearest=" + this.bestNode.pos + " unknown=" + this.destinationUnknown + " pending=" + this.hitPendingChunk
               );
            }

            return new AiPathfinder.PathResult(
               List.of(), false, this.expandedCount, this.hitPendingChunk, this.exhaustedSearch || this.cachedGrid.isJumpBudgetExhausted(), this.wedgedStart, this.confinedStart
            );
         } else {
            ArrayDeque var1 = new ArrayDeque();
            NavigationOpenSet.Inner_DataUtil var2 = this.routeProofMode && !this.reachedGoal ? this.bestUnknownEdgeNode : this.bestNode;
            if (!this.routeProofMode && !this.reachedGoal && var2.pos.distSqr(this.startPos) <= 25.0) {
               NavigationOpenSet.Inner_DataUtil var3 = this.selectProgressNode();
               if (var3 != null) {
                  var2 = var3;
               }
            }

            while (var2.parent != null) {
               if (var1.size() >= this.allocatedNodeCount) {
                  throw new IllegalStateException("Cyclic path parent chain");
               }

               var1.addFirst(var2.move);
               var2 = var2.parent;
            }

            return new AiPathfinder.PathResult(
               new ArrayList<>(var1),
               this.reachedGoal,
               this.expandedCount,
               this.hitPendingChunk,
               this.exhaustedSearch || !this.reachedGoal && this.cachedGrid.isJumpBudgetExhausted(),
               this.wedgedStart,
               this.confinedStart,
               this.routeProofMode
                  && !this.reachedGoal
                  && this.bestUnknownEdgeNode != null
                  && this.bestUnknownEdgeNode.pos.equals(this.startPos)
            );
         }
      }

      List<AiPathfinder.PathStep> currentBestTrail() {
         ArrayDeque var1 = new ArrayDeque();

         for (NavigationOpenSet.Inner_DataUtil var2 = this.bestNode;
            var2.parent != null && var1.size() < 64;
            var2 = var2.parent
         ) {
            var1.addFirst(var2.move);
         }

         return List.copyOf(var1);
      }

      public void finish() {
         this.exhaustedSearch = !this.finished;
         this.finished = true;
         this.cancelAllJumpFutures();
      }

      public int expandedCount() {
         return this.expandedCount;
      }

      public long jumpSolveNanos() {
         return this.cachedGrid.jumpSolveNanos;
      }

      public int jumpSolveCount() {
         return this.cachedGrid.jumpSolves;
      }

      public int generatedCount() {
         return this.generatedCount;
      }

      public int peakOpenSize() {
         return this.peakOpenSize;
      }

      public long blockQueryCount() {
         return this.cachedGrid.blockQueries;
      }

      private record JumpSolveStats(AiPathfinder.PathStep movement, int flags, int solves, int hits, long nanos, long queries) {
      }

      private record QueuedJump(
         AiPathfinder.MoveGenerator.JumpCandidate candidate,
         BlockPos continuation,
         NavigationOpenSet.Inner_DataUtil source,
         double sourceCost,
         double priority,
         long order
      ) {
      }
   }

   public static final class MoveCosts {
      public static final double WALK_COST = 4.5;
      public static final double WALK_DIAGONAL_COST = 6.4;
      public static final double SPRINT_COST = 3.2;
      public static final double SPRINT_DIAGONAL_COST = 4.5;
      public static final double SPRINT_JUMP_COST = 2.9000000000000004;
      public static final double LOW_CEILING_SPRINT_JUMP_COST = 2.2;
      private static final ConcurrentHashMap<Long, Double> SPEED_SCALE_CACHE = new ConcurrentHashMap<>();
      public static final double SLOW_FLOOR_COST_MULTIPLIER = 2.5;
      public static final double SLIME_COST_MULTIPLIER = 3.6;
      public static final double THORNY_PENALTY = 25.0;
      public static final double SPRINT_WEB_COST = 32.0;
      public static final double WALK_WEB_COST = 41.0;
      public static final double FALL_WEB_COST = 255.0;
      public static final double PORTAL_PENALTY = 400.0;
      public static final double ADJACENT_CONTACT_HAZARD_PENALTY = 2.0;
      public static final double DEEP_DROP_THRESHOLD = 14.0;
      public static final double STEP_UP_COST = 5.0;
      public static final double SPRINT_STEP_UP_COST = 3.6;
      public static final double JUMP_UP_COST = 9.0;
      public static final double WATER_COST_PENALTY = 3.0;
      public static final double FALL_HEURISTIC_PER_BLOCK = 4.0;
      public static final double DEFAULT_PLACE_COST = 40.0;
      public static final double RECENT_PLACE_CONFLICT_PENALTY = 200.0;
      public static final double RECENT_BREAK_CONFLICT_PENALTY = 60.0;
      public static final double PILLAR_COST = 12.0;
      public static final double BRIDGE_CELL_COST = 4.5;
      public static final double BRIDGE_START_PENALTY = 10.0;
      public static final double SUPPORTED_BRIDGE_PENALTY = 2.0;
      public static final double CLIMB_COST = 5.5;
      public static final double DESCEND_PER_BLOCK_COST = 3.0;
      public static final double DESCEND_BASE_COST = 5.0;
      public static final double MAX_STEP_UP_HEIGHT = 0.5625;
      public static final double TINY_STEP_HEIGHT = 0.0625;
      public static final double HEIGHT_EPSILON = 1.0E-6;
      public static final double MAX_JUMP_UP_HEIGHT = 1.25;
      public static final int SAFE_FALL_BLOCKS = 3;
      public static final int MAX_DESCEND_SCAN_BLOCKS = 8;
      public static final double LANDING_SURFACE_TOLERANCE = 0.25;
      public static final int MAX_FALL_HEIGHT = 40;
      public static final int MIN_PARKOUR_GAP = 2;
      public static final double THREAT_MAX_SURCHARGE = 60.0;
      public static final double THREAT_RADIUS = 12.0;
      public static final double ADJACENT_LAVA_SURCHARGE = 8.0;
      public static final double GOAL_PLACE_COST_CAP = 40.0;
      public static final double UNEDITED_PLACE_PENALTY = 12.0;
      public static final double SIGHT_LINE_SURCHARGE = 10.0;
      public static final double SHOOTER_SIGHT_SURCHARGE = 40.0;
      public static final double CLOSE_THREAT_SURCHARGE = 60.0;
      public static final double SOFT_DESCEND_BASE_COST = 2.0;
      public static final double SAFEST_DESCEND_PENALTY = 1.0;
      public static final double WATER_SAFEST_SURCHARGE = 8.0;
      public static final double SAVER_DIG_MULTIPLIER = 2.0;
      public static final double SAVER_PLACE_MULTIPLIER = 2.0;
      public static final double JUMP_OVERHEAD_TICKS = 2.0;
      public static final double PARKOUR_GAP_OVERHEAD = 2.0;
      public static final double PARKOUR_BASE_TICKS = 7.0;
      public static final double PARKOUR_UP_OVERHEAD = 2.0;
      public static final double HARD_JUMP_PENALTY = 20.0;
      private static volatile long cautiousParkourUntilNanos;
      public static final double RECENT_PLACEMENT_PENALTY = 60.0;
      private static volatile Set<Long> avoidPlacementCells = Set.of();
      private static volatile long avoidPlacementUntilNanos;
      private static volatile long cautiousBridgeUntilNanos;
      private static volatile long allowDropBridgeUntilNanos;
      static final double MIN_HORIZONTAL_TICK_RATE = Math.min(2.9000000000000004, Math.min(3.2, 4.5 / Math.sqrt(2.0)));
      static final double MIN_ASCEND_TICK_RATE = Math.min(9.0 + MIN_HORIZONTAL_TICK_RATE, 12.0);

      private MoveCosts() {
      }

      static double speedTickMultiplier(double var0) {
         if (!(var0 > 0.100001)) {
            return 1.0;
         }

         long var2 = Math.round(var0 * 100000.0);
         Double var4 = SPEED_SCALE_CACHE.get(var2);
         if (var4 != null) {
            return var4;
         }

         double var5 = simulateSprintJumpTicksPerBlock(var0) / simulateSprintJumpTicksPerBlock(0.1);
         if (SPEED_SCALE_CACHE.size() < 64) {
            SPEED_SCALE_CACHE.put(var2, var5);
         }

         return var5;
      }

      static double simulateSprintJumpTicksPerBlock(final double var0) {
         AiPhysics.PhysicsWorld var2 = new AiPhysics.PhysicsWorld() {
            @Override
            public double movementSpeed() {
               return var0;
            }

            @Override
            public List<AABB> collisions(AABB var1) {
               ArrayList<AABB> var2x = new ArrayList<>(4);

               for (int var3 = (int)Math.floor(var1.minX); var3 <= (int)Math.floor(var1.maxX); var3++) {
                  for (int var4 = (int)Math.floor(var1.minZ); var4 <= (int)Math.floor(var1.maxZ); var4++) {
                     AABB var5 = new AABB(var3, 63.0, var4, var3 + 1, 64.0, var4 + 1);
                     if (var5.intersects(var1)) {
                        var2x.add(var5);
                     }
                  }
               }

               return var2x;
            }
         };
         AiPhysics.MotionState var3 = new AiPhysics.MotionState(0.5, 64.0, 0.5, 0.0, -0.0784, 0.0, true, 0, false, false);
         double var4 = 0.0;

         for (int var6 = 0; var6 < 400; var6++) {
            if (var6 == 200) {
               var4 = var3.z();
            }

            var3 = AiPhysics.step(var3, var3.onGround() ? AiPhysics.MoveInput.jump(0.0F, true) : AiPhysics.MoveInput.running(0.0F, true), var2);
         }

         return 200.0 / Math.max(1.0E-6, var3.z() - var4);
      }

      // 5.3 external ABI bridges
      public static double parkourGapCostCompat(int gap) { return parkourGapCost(gap); }
      public static double parkourUpGapCostCompat(int gap) { return parkourUpGapCost(gap); }
      public static void setAvoidPlacementCellsCompat(Set<Long> cells) { setAvoidPlacementCells(cells); }
      public static void enableCautiousParkourCompat(long nanos) { enableCautiousParkour(nanos); }
      static boolean isCautiousParkourActiveCompat() { return isCautiousParkourActive(); }
      public static void enableCautiousBridgeCompat(long nanos) { enableCautiousBridge(nanos); }
      static boolean isCautiousBridgeActiveCompat() { return isCautiousBridgeActive(); }
      public static void enableDropBridgeCompat(long nanos) { enableDropBridge(nanos); }
      static boolean isDropBridgeActiveCompat() { return isDropBridgeActive(); }
      public static double breakBlockTicksCompat(float hardness, double speed, boolean correctTool) { return breakBlockTicks(hardness, speed, correctTool); }
      public static double estimateHeuristicCompat(int dx, int dy, int dz) { return estimateHeuristic(dx, dy, dz); }
      public static double estimateDirectionalHeuristicCompat(int dx, int rise, int dz) { return estimateDirectionalHeuristic(dx, rise, dz); }

      public static double parkourGapCost(int var0) {
         return 7.0 + (var0 + 1) * 3.2 + 2.0 + (var0 - 2);
      }

      public static double parkourUpGapCost(int var0) {
         return parkourGapCost(var0) + 2.0;
      }

      public static void setAvoidPlacementCells(Set<Long> var0) {
         avoidPlacementCells = Set.copyOf(var0);
         avoidPlacementUntilNanos = var0.isEmpty() ? 0L : System.nanoTime() + 3000000000L;
      }

      static double avoidPlacementPenalty(List<BlockPos> var0) {
         if (!var0.isEmpty() && System.nanoTime() < avoidPlacementUntilNanos) {
            var var1 = avoidPlacementCells;
            double var2 = 0.0;

            for (BlockPos var5 : var0) {
               if (var1.contains(var5.asLong())) {
                  var2 += 60.0;
               }
            }

            return var2;
         } else {
            return 0.0;
         }
      }

      public static void enableCautiousParkour(long var0) {
         cautiousParkourUntilNanos = var0 <= 0L ? 0L : System.nanoTime() + var0;
      }

      static boolean isCautiousParkourActive() {
         return System.nanoTime() < cautiousParkourUntilNanos;
      }

      public static void enableCautiousBridge(long var0) {
         cautiousBridgeUntilNanos = var0 <= 0L ? 0L : System.nanoTime() + var0;
      }

      static boolean isCautiousBridgeActive() {
         return System.nanoTime() < cautiousBridgeUntilNanos;
      }

      public static void enableDropBridge(long var0) {
         allowDropBridgeUntilNanos = var0 <= 0L ? 0L : System.nanoTime() + var0;
      }

      static boolean isDropBridgeActive() {
         return System.nanoTime() < allowDropBridgeUntilNanos;
      }

      public static double jumpPlanCost(AiJumpSolver.JumpPlan var0) {
         return 2.0 + var0.ticks() + Math.max(0.0, 0.2 - var0.margin()) * 10.0 + (var0.maneuver() == null ? 0 : 16);
      }

      public static double descendCost(int var0) {
         return 5.0 + 3.0 * var0;
      }

      public static double softDescendCost(int var0) {
         return 2.0 + var0;
      }

      public static double breakBlockTicks(float var0, double var1, boolean var3) {
         if (var0 < 0.0F) {
            return 1.0 / 0.0;
         }

         if (!(var1 > 0.0) || !Double.isFinite(var1)) {
            return 1.0 / 0.0;
         }

         if (var0 == 0.0F) {
            return 6.0;
         }

         double var4 = var1 / var0 / (var3 ? 30.0 : 100.0);
         return var4 >= 1.0 ? 6.0 : Math.ceil(1.0 / var4) + 5.0;
      }

      public static int destructionPenaltyForBlock(String var0) {
         short var10000;
         switch (var0) {
            case "diamond_block":
            case "emerald_block":
            case "netherite_block":
            case "ancient_debris":
               var10000 = 4000;
               break;
            case "gold_block":
            case "iron_block":
            case "raw_iron_block":
            case "raw_gold_block":
            case "lapis_block":
               var10000 = 400;
               break;
            case "piston":
            case "sticky_piston":
            case "observer":
            case "redstone_block":
            case "powered_rail":
            case "repeater":
            case "comparator":
            case "crafting_table":
               var10000 = 200;
               break;
            case "infested_stone":
            case "infested_cobblestone":
            case "infested_stone_bricks":
            case "infested_mossy_stone_bricks":
            case "infested_cracked_stone_bricks":
            case "infested_chiseled_stone_bricks":
            case "infested_deepslate":
               var10000 = 2000;
               break;
            default:
               var10000 = 0;
         }

         return var10000;
      }

      public static double digDownCost(double var0) {
         return var0 + 2.0;
      }

      public static double digTunnelCost(double var0) {
         return var0 + 4.5;
      }

      public static int moveTimeoutTicks(double var0) {
         return (int)Math.ceil(var0 * 3.0) + 20;
      }

      public static double estimateHeuristic(int var0, int var1, int var2) {
         return Math.hypot(var0, var2) * Math.min(2.9000000000000004, Math.min(3.2, 4.5 / Math.sqrt(2.0))) + Math.abs(var1) * Math.min(5.5, 4.0);
      }

      public static double estimateDirectionalHeuristic(int var0, int var1, int var2) {
         double var3 = Math.sqrt((double)var0 * var0 + (double)var2 * var2) * MIN_HORIZONTAL_TICK_RATE;
         return var1 <= 0 ? var3 - var1 * Math.min(5.5, 4.0) : var3 + var1 * MIN_ASCEND_TICK_RATE;
      }
   }

   public interface SearchTask {
      boolean isFollowing();

      int remainingTicks();

      default int pendingPlacementCount() {
         return 0;
      }

      default List<AiPathfinder.PathStep> remainingMoves() {
         return List.of();
      }

      BlockPos currentPos();
   }

   public record PathStep(
      AiPathfinder.MoveType type,
      BlockPos from,
      BlockPos to,
      List<BlockPos> cellsToBreak,
      List<BlockPos> cellsToPlace,
      double costTicks,
      double plannedBreakTicks,
      AiJumpSolver.JumpPlan jump,
      boolean repairFarmland,
      AiJumpSolver.JumpPlan paceJump
   ) {
      public PathStep(
         AiPathfinder.MoveType var1,
         BlockPos var2,
         BlockPos var3,
         List<BlockPos> var4,
         List<BlockPos> var5,
         double var6,
         double var8,
         AiJumpSolver.JumpPlan var10,
         boolean var11
      ) {
         this(var1, var2, var3, var4, var5, var6, var8, var10, var11, null);
      }

      public PathStep(
         AiPathfinder.MoveType var1,
         BlockPos var2,
         BlockPos var3,
         List<BlockPos> var4,
         List<BlockPos> var5,
         double var6,
         double var8,
         AiJumpSolver.JumpPlan var10
      ) {
         this(var1, var2, var3, var4, var5, var6, var8, var10, false);
      }

      public PathStep(
         AiPathfinder.MoveType type,
         BlockPos from,
         BlockPos to,
         List<BlockPos> cellsToBreak,
         List<BlockPos> cellsToPlace,
         double costTicks,
         double plannedBreakTicks,
         AiJumpSolver.JumpPlan jump,
         boolean repairFarmland,
         AiJumpSolver.JumpPlan paceJump
      ) {
         cellsToBreak = List.copyOf(cellsToBreak);
         cellsToPlace = List.copyOf(cellsToPlace);
         this.type = type;
         this.from = from;
         this.to = to;
         this.cellsToBreak = cellsToBreak;
         this.cellsToPlace = cellsToPlace;
         this.costTicks = costTicks;
         this.plannedBreakTicks = plannedBreakTicks;
         this.jump = jump;
         this.repairFarmland = repairFarmland;
         this.paceJump = paceJump;
      }

      public PathStep(AiPathfinder.MoveType var1, BlockPos var2, BlockPos var3, List<BlockPos> var4, List<BlockPos> var5, double var6, double var8) {
         this(var1, var2, var3, var4, var5, var6, var8, null);
      }

      PathStep(AiPathfinder.MoveType var1, BlockPos var2, BlockPos var3, List<BlockPos> var4, List<BlockPos> var5, double var6) {
         this(var1, var2, var3, var4, var5, var6, -1.0);
      }

      static AiPathfinder.PathStep travel(AiPathfinder.MoveType var0, BlockPos var1, BlockPos var2, double var3) {
         return new AiPathfinder.PathStep(var0, var1, var2, List.of(), List.of(), var3);
      }

      static AiPathfinder.PathStep jump(BlockPos var0, BlockPos var1, double var2, AiJumpSolver.JumpPlan var4) {
         return new AiPathfinder.PathStep(AiPathfinder.MoveType.JUMP, var0, var1, List.of(), List.of(), var2, -1.0, var4);
      }

      AiPathfinder.PathStep withPaceJump(AiJumpSolver.JumpPlan var1) {
         return new AiPathfinder.PathStep(
            this.type, this.from, this.to, this.cellsToBreak, this.cellsToPlace, this.costTicks, this.plannedBreakTicks, this.jump, this.repairFarmland, var1
         );
      }
   }

   public record SearchStats(long elapsedNanos, int rounds, int expanded, long generated, long blockQueries, int peakOpen) {
   }

   enum FallPolicy {
      STANDARD(4.0),
      PARKOUR_SAFE(3.999),
      PARKOUR_NO_DAMAGE(32.0);

      final double maxDropBlocks;

      FallPolicy(double var3) {
         this.maxDropBlocks = var3;
      }

      boolean allowsExtendedDrop() {
         return this == PARKOUR_NO_DAMAGE;
      }
   }

   public record PathResult(
      List<AiPathfinder.PathStep> movements,
      boolean reachesGoal,
      int expanded,
      boolean hitPending,
      boolean exhausted,
      boolean wedgedStart,
      boolean confinedStart,
      boolean awaitingTerrain
   ) {
      public PathResult(List<AiPathfinder.PathStep> var1, boolean var2, int var3, boolean var4, boolean var5, boolean var6, boolean var7) {
         this(var1, var2, var3, var4, var5, var6, var7, false);
      }

      public PathResult(List<AiPathfinder.PathStep> var1, boolean var2, int var3, boolean var4, boolean var5) {
         this(var1, var2, var3, var4, var5, false, false);
      }

      public PathResult(List<AiPathfinder.PathStep> var1, boolean var2, int var3, boolean var4, boolean var5, boolean var6) {
         this(var1, var2, var3, var4, var5, var6, false);
      }

      public PathResult(
         List<AiPathfinder.PathStep> movements,
         boolean reachesGoal,
         int expanded,
         boolean hitPending,
         boolean exhausted,
         boolean wedgedStart,
         boolean confinedStart,
         boolean awaitingTerrain
      ) {
         movements = List.copyOf(movements);
         this.movements = movements;
         this.reachesGoal = reachesGoal;
         this.expanded = expanded;
         this.hitPending = hitPending;
         this.exhausted = exhausted;
         this.wedgedStart = wedgedStart;
         this.confinedStart = confinedStart;
         this.awaitingTerrain = awaitingTerrain;
      }

      public BlockPos destination() {
         return this.movements.isEmpty() ? null : this.movements.get(this.movements.size() - 1).to();
      }

      public double totalCostTicks() {
         double var1 = 0.0;

         for (AiPathfinder.PathStep var4 : this.movements) {
            var1 += var4.costTicks();
         }

         return var1;
      }
   }

   public enum MoveType {
      WALK,
      WALK_DIAGONAL,
      SPRINT,
      SPRINT_DIAGONAL,
      STEP_UP_1,
      JUMP_UP_1,
      DESCEND_1,
      DESCEND_2,
      DESCEND_3,
      DESCEND_SOFT,
      DIG_DOWN_1,
      DIG_TUNNEL,
      PILLAR,
      BRIDGE_CELL,
      CLIMB_1,
      DOOR,
      JUMP_GAP_2,
      JUMP_GAP_3,
      PARKOUR_ASCEND,
      SPRINT_JUMP,
      JUMP,
      SLIME_BOUNCE,
      LADDER_JUMP,
      DIG_ASCEND,
      DIG_DESCEND;
   }

   public static final class LiveGridView implements AiPathfinder.GridView {
      // 5.3 external ABI bridges
      public long changeVersionCompat() { return this.changeVersion(); }
      public void scanStepCompat() { this.scanStep(); }
      public void setFocusCompat(BlockPos focus) { this.setFocus(focus); }
      public void updateHotbarCompat(ItemStack[] snapshot) { this.updateHotbar(snapshot); }
      public void setAcceptedToolCompat(BiPredicate<BlockState, ItemStack> accepted) { this.setAcceptedTool(accepted); }
      public void scanBudgetCompat(long budgetNanos) { this.scanBudget(budgetNanos); }
      public static AiPathfinder.CellInfo classifyCellCompat(Minecraft mc, ClientLevel level, BlockPos p, ItemStack[] hotbar) {
         return classifyCell(mc, level, p, hotbar);
      }
      public static boolean isFeetHazardCompat(BlockState state) { return isFeetHazard(state); }
      public static boolean isFloorHazardCompat(BlockState state) { return isFloorHazard(state); }
      public static boolean isOreBlockCompat(BlockState state) { return isOreBlock(state); }
      public static boolean isLitPortalFrameCompat(BlockGetter level, BlockPos target) { return isLitPortalFrame(level, target); }
      static boolean isSafeToDigCompat(ClientLevel level, BlockPos target) { return isSafeToDig(level, target); }
      static boolean lacksRequiredToolCompat(boolean requiresCorrectTool, boolean correctToolAvailable) { return lacksRequiredTool(requiresCorrectTool, correctToolAvailable); }
      public static boolean hasCorrectToolInSlotsCompat(Minecraft mc, BlockState state, int slots) { return hasCorrectToolInSlots(mc, state, slots); }
      private static final long DEFAULT_SCAN_BUDGET_NANOS = 750000L;
      private final Object updateMonitor = new Object();
      private volatile long sliceUpdateVersion;
      private volatile long invalidationRevision;
      private BiPredicate<BlockState, ItemStack> acceptedTool;
      private final Minecraft minecraft;
      private final ClientLevel level;
      private final int minY;
      private final int maxY;
      private final LongSupplier nanoClock;
      private volatile ItemStack[] hotbarSnapshot;
      private final AiPathfinder.LiveGridView.CellSource cellSource;
      private final AiChunkMemory chunkMemory;
      private final ConcurrentHashMap<Long, AiPathfinder.LiveGridView.SectionSlice> loadedSlices = new ConcurrentHashMap<>();
      private final Set<Long> pendingChunkKeys = ConcurrentHashMap.newKeySet();
      private final Set<Long> priorityColumnKeys = ConcurrentHashMap.newKeySet();
      private final Map<Long, AiPathfinder.LiveGridView.SectionSlice> memoryFallbackSlices = new HashMap<>();
      private volatile BlockPos focusPos = BlockPos.ZERO;
      private long activeChunkKey = -9223372036854775808L;
      private AiPathfinder.LiveGridView.SectionSlice activeSlice;
      private int activeColumnCursor;
      private int[] activeColumnOrder;
      private long savingChunkKey = -9223372036854775808L;
      private int savingChunkX;
      private int savingChunkZ;
      private int savingColumnCursor;
      private int savingYOffset;
      private int savingChunkRevision;
      private short[] savingFlags;
      private Map<Integer, String> savingBlockNames;
      private static final long TOOL_RESCAN_INTERVAL_NANOS = 1000000000L;
      private long lastHotbarRescanNanos;

      @Override
      public long revision() {
         return this.invalidationRevision;
      }

      public long changeVersion() {
         return this.sliceUpdateVersion + this.revision();
      }

      void awaitChangeVersion(long var1, long var3) throws java.lang.InterruptedException {
         synchronized (this.updateMonitor) {
            while (this.changeVersion() == var1) {
               long var6 = var3 - System.nanoTime();
               if (var6 <= 0L) {
                  return;
               }

               TimeUnit.NANOSECONDS.timedWait(this.updateMonitor, var6);
            }
         }
      }

      private void notifySliceUpdated() {
         synchronized (this.updateMonitor) {
            this.sliceUpdateVersion++;
            this.updateMonitor.notifyAll();
         }
      }

      public LiveGridView(Minecraft var1, BlockPos var2, BlockPos var3, ItemStack[] var4) {
         this(var1, var2, var3, var4, null);
      }

      public LiveGridView(final Minecraft var1, BlockPos var2, BlockPos var3, ItemStack[] var4, AiChunkMemory var5) {
         this.minecraft = var1;
         this.nanoClock = System::nanoTime;
         this.level = var1.level;
         int var6 = this.level.getMinY();
         int var7 = this.level.getMaxY() - 1;
         this.minY = Math.max(var6, Math.min(var2.getY(), var3.getY()) - 28);
         this.maxY = Math.min(var7, Math.max(var2.getY(), var3.getY()) + 28);
         this.hotbarSnapshot = var4;
         this.chunkMemory = var5;
         this.cellSource = new AiPathfinder.LiveGridView.CellSource() {
            @Override
            public AiPathfinder.CellInfo cellAt(BlockPos var1x) {
               return AiPathfinder.LiveGridView.classifyCell(
                  var1, LiveGridView.this.level, var1x, LiveGridView.this.hotbarSnapshot, LiveGridView.this.acceptedTool
               );
            }

            @Override
            public boolean isChunkLoaded(int var1x, int var2x) {
               return LiveGridView.this.level.getChunkSource().getChunk(var1x, var2x, ChunkStatus.FULL, false) != null;
            }

            @Override
            public short putAt_S(BlockPos var1x) {
               return AiPathfinder.LiveGridView.encodeCollisionShape(LiveGridView.this.level, var1x);
            }

            @Override
            public byte putAt_B(BlockPos var1x) {
               return AiPathfinder.LiveGridView.encodeSurfaceByte(LiveGridView.this.level, var1x);
            }
         };
      }

      LiveGridView(int var1, int var2, AiPathfinder.LiveGridView.CellSource var3) {
         this(var1, var2, var3, null);
      }

      LiveGridView(int var1, int var2, AiPathfinder.LiveGridView.CellSource var3, AiChunkMemory var4) {
         this(var1, var2, var3, var4, System::nanoTime);
      }

      @Override
      public double movementSpeed() {
         return this.minecraft != null && this.minecraft.player != null ? AiLiveWorld.putAt_D_LivingEntity(this.minecraft.player) : 0.1;
      }

      LiveGridView(int var1, int var2, AiPathfinder.LiveGridView.CellSource var3, AiChunkMemory var4, LongSupplier var5) {
         this.nanoClock = var5;
         this.minecraft = null;
         this.level = null;
         this.minY = var1;
         this.maxY = var2;
         this.hotbarSnapshot = null;
         this.cellSource = var3;
         this.chunkMemory = var4;
      }

      public void setFocus(BlockPos var1) {
         this.focusPos = var1.immutable();
      }

      public void setAcceptedTool(BiPredicate<BlockState, ItemStack> var1) {
         if (this.acceptedTool != var1) {
            this.acceptedTool = var1;
            this.pendingChunkKeys.addAll(this.loadedSlices.keySet());
            this.loadedSlices.clear();
            this.memoryFallbackSlices.clear();
            this.invalidationRevision++;
            this.activeSlice = null;
            this.activeChunkKey = -9223372036854775808L;
            this.notifySliceUpdated();
         }
      }

      public void updateHotbar(ItemStack[] var1) {
         ItemStack[] var2 = this.hotbarSnapshot;
         boolean var3 = var2 != null && var2.length == var1.length;
         if (var3) {
            for (int var4 = 0; var4 < var1.length; var4++) {
               if (!sameToolStack(var2[var4], var1[var4])) {
                  var3 = false;
                  break;
               }
            }
         }

         if (!var3 && sameItemTypes(var2, var1) && System.nanoTime() - this.lastHotbarRescanNanos < 1000000000L) {
            this.hotbarSnapshot = var1;
         } else {
            this.hotbarSnapshot = var1;
            if (!var3) {
               if (sameItemTypes(var2, var1)) {
                  this.lastHotbarRescanNanos = System.nanoTime();
               }

               this.pendingChunkKeys.addAll(this.loadedSlices.keySet());
               this.loadedSlices.clear();
               this.memoryFallbackSlices.clear();
               this.invalidationRevision++;
               this.activeSlice = null;
               this.activeChunkKey = -9223372036854775808L;
               this.notifySliceUpdated();
            }
         }
      }

      private static boolean sameItemTypes(ItemStack[] var0, ItemStack[] var1) {
         if (var0 != null && var1 != null && var0.length == var1.length) {
            for (int var2 = 0; var2 < var0.length; var2++) {
               ItemStack var3 = var0[var2];
               ItemStack var4 = var1[var2];
               if (var3 != var4) {
                  if (var3 == null || var4 == null) {
                     return false;
                  }

                  boolean var5 = var3.isEmpty();
                  boolean var6 = var4.isEmpty();
                  if (!var5 && !var6) {
                     if (var3.getItem() != var4.getItem()) {
                        return false;
                     }

                     if (!Objects.equals(var3.getEnchantments(), var4.getEnchantments())) {
                        return false;
                     }

                     if (AiItems.hasEnoughDurability(var3, 2) != AiItems.hasEnoughDurability(var4, 2)) {
                        return false;
                     }
                  } else if (var5 != var6) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            return false;
         }
      }

      private static boolean sameToolStack(ItemStack var0, ItemStack var1) {
         if (var0 == var1) {
            return true;
         } else if (var0 != null && var1 != null) {
            boolean var2 = var0.isEmpty();
            boolean var3 = var1.isEmpty();
            return !var2 && !var3
               ? var0.getItem() == var1.getItem()
                  && var0.getCount() == var1.getCount()
                  && Objects.equals(var0.getEnchantments(), var1.getEnchantments())
                  && AiItems.hasEnoughDurability(var0, 2) == AiItems.hasEnoughDurability(var1, 2)
               : var2 == var3;
         } else {
            return false;
         }
      }

      @Override
      public AiPathfinder.CellInfo probe(int var1, int var2, int var3) {
         if (var2 >= this.minY && var2 <= this.maxY) {
            long var4 = packChunkKey(var1 >> 4, var3 >> 4);
            AiPathfinder.LiveGridView.SectionSlice var6 = this.loadedSlices.get(var4);
            if (var6 != null && var6.revision() == AiGridRevisions.revisionOf(var4)) {
               if (var6.ready().get((var1 & 15) * 16 + (var3 & 15)) == 0) {
                  this.pendingChunkKeys.add(var4);
                  this.priorityColumnKeys.add(packChunkKey(var1, var3));
                  return null;
               }

               int var7 = this.maxY - this.minY + 1;
               int var8 = var6.index(var1, var2, var3, this.minY, var7);
               int var9 = var6.flags()[var8] & '\uffff';
               if ((var9 & 1) == 0) {
                  return AiPathfinder.CellInfo.UNLOADED;
               }

               int var10 = var6.digX4()[var8] & '\uffff';
               return var9 == 3 && var6.top16()[var8] == 0 && var6.mult10()[var8] == 10 && var10 == 65535
                  ? AiPathfinder.CellInfo.air()
                  : new AiPathfinder.CellInfo(
                     true,
                     (var9 & 2) != 0,
                     var6.top16()[var8] & 31,
                     (var9 & 4) != 0,
                     (var9 & 8) != 0,
                     (var9 & 16) != 0,
                     (var9 & 32) != 0,
                     (var9 & 64) != 0,
                     (var9 & 128) != 0,
                     var6.mult10()[var8],
                     var10 == 65535 ? 1.0 / 0.0 : var10 / 4.0,
                     (var9 & 256) != 0,
                     (var9 & 512) != 0,
                     (var9 & 1024) != 0,
                     (var9 & 4096) != 0,
                     (var9 & 2048) != 0,
                     var6.penalties()[var8] & 65535,
                     (var9 & 8192) != 0,
                     (var9 & 16384) != 0,
                     (var9 & 32768) != 0,
                     (var6.top16()[var8] & 128) != 0,
                     (var6.top16()[var8] & 64) != 0,
                     (var6.top16()[var8] & 32) != 0
                  );
            } else {
               if (var6 != null) {
                  this.loadedSlices.remove(var4, var6);
               }

               this.pendingChunkKeys.add(var4);
               this.priorityColumnKeys.add(packChunkKey(var1, var3));
               return null;
            }
         } else {
            return AiPathfinder.CellInfo.UNLOADED;
         }
      }

      private AiPathfinder.LiveGridView.SectionSlice getReadySlice(int var1, int var2) {
         long var3 = packChunkKey(var1 >> 4, var2 >> 4);
         AiPathfinder.LiveGridView.SectionSlice var5 = this.loadedSlices.get(var3);
         if (var5 != null && var5.revision() == AiGridRevisions.revisionOf(var3) && var5.ready().get((var1 & 15) * 16 + (var2 & 15)) != 0) {
            return var5;
         }

         if (var5 != null && var5.revision() != AiGridRevisions.revisionOf(var3)) {
            this.loadedSlices.remove(var3, var5);
         }

         this.pendingChunkKeys.add(var3);
         this.priorityColumnKeys.add(packChunkKey(var1, var2));
         return null;
      }

      @Override
      public short putAt_S_I(int var1, int var2, int var3) {
         if (var2 < this.minY) {
            return 1;
         } else if (var2 > this.maxY) {
            return 0;
         } else {
            AiPathfinder.LiveGridView.SectionSlice var4 = this.getReadySlice(var1, var3);
            if (var4 == null) {
               return -1;
            } else {
               int var5 = var4.index(var1, var2, var3, this.minY, this.maxY - this.minY + 1);
               short var6 = var4.flags()[var5];
               if ((var6 & 1) == 0) {
                  return 1;
               } else {
                  return var4.shape() != null ? var4.shape()[var5] : AiShapes.shapeId(true, (var6 & 2) != 0, var4.top16()[var5] & 31);
               }
            }
         }
      }

      @Override
      public byte putAt_B_I(int var1, int var2, int var3) {
         if (var2 >= this.minY && var2 <= this.maxY) {
            AiPathfinder.LiveGridView.SectionSlice var4 = this.getReadySlice(var1, var3);
            if (var4 == null) {
               return AiSurface.NORMAL.id();
            } else {
               int var5 = var4.index(var1, var2, var3, this.minY, this.maxY - this.minY + 1);
               if (var4.surface() != null) {
                  return var4.surface()[var5];
               } else {
                  short var6 = var4.flags()[var5];
                  if ((var6 & 4096) != 0) {
                     return AiSurface.ICE.id();
                  } else {
                     return (var6 & 8192) != 0 ? AiSurface.SOUL_SAND.id() : AiSurface.NORMAL.id();
                  }
               }
            }
         } else {
            return AiSurface.NORMAL.id();
         }
      }

      public void scanStep() {
         this.scanBudget(750000L);
      }

      public void scanBudget(long var1) {
         if (!this.memoryFallbackSlices.isEmpty()) {
            this.promoteLoadedFallbackChunks();
         }

         long var3 = this.nanoClock.getAsLong() + var1;
         int var5 = this.maxY - this.minY + 1;

         while (this.nanoClock.getAsLong() < var3) {
            Long var6 = this.peekNearestPriorityColumn();
            if (var6 != null) {
               this.priorityColumnKeys.remove(var6);
               int var7 = (int)(var6 >> 32);
               int var8 = var6.intValue();
               long var9 = packChunkKey(var7 >> 4, var8 >> 4);
               if (this.cellSource.isChunkLoaded(var7 >> 4, var8 >> 4)) {
                  AiPathfinder.LiveGridView.SectionSlice var20 = this.loadedSlices.get(var9);
                  if (var20 == null || var20.revision() != AiGridRevisions.revisionOf(var9)) {
                     var20 = this.allocateSlice(var5, var9);
                     this.loadedSlices.put(var9, var20);
                  }

                  int var12 = (var7 & 15) * 16 + (var8 & 15);
                  if (var20.ready().get(var12) == 0) {
                     this.scanColumnIntoSlice(var20, var7, var8, var5);
                     var20.ready().set(var12, 1);
                     this.notifySliceUpdated();
                  }
                  continue;
               }
            }

            if (this.activeSlice != null && this.loadedSlices.get(this.activeChunkKey) != this.activeSlice) {
               this.activeSlice = null;
               this.activeChunkKey = -9223372036854775808L;
            }

            if (this.activeSlice == null) {
               Long var13 = this.peekNearestPendingChunk();
               if (var13 == null) {
                  if (this.savingChunkKey != -9223372036854775808L) {
                     this.stepMemorySave(var3);
                  }

                  return;
               }

               this.activeChunkKey = var13;
               this.activeColumnCursor = 0;
               this.activeColumnOrder = spiralColumnOrder(
                  Math.max(0, Math.min(15, this.focusPos.getX() - ((int)(var13 >> 32) << 4))),
                  Math.max(0, Math.min(15, this.focusPos.getZ() - (var13.intValue() << 4)))
               );
               this.activeSlice = this.loadedSlices.get(var13);
               if (this.activeSlice == null || this.activeSlice.revision() != AiGridRevisions.revisionOf(var13)) {
                  this.activeSlice = this.allocateSlice(var5, var13);
               }

               int var15 = (int)(var13 >> 32);
               int var17 = var13.intValue();
               if (!this.cellSource.isChunkLoaded(var15, var17)) {
                  AiPathfinder.LiveGridView.SectionSlice var19 = this.chunkMemory == null
                     ? null
                     : this.chunkMemory.sectionSlice(var15, var17, this.minY, var5, AiGridRevisions.revisionOf(var13));
                  if (var19 != null) {
                     this.loadedSlices.put(var13, var19);
                     this.memoryFallbackSlices.put(var13, var19);
                  } else {
                     for (int var11 = 0; var11 < 256; var11++) {
                        this.activeSlice.ready().set(var11, 1);
                     }

                     this.loadedSlices.put(var13, this.activeSlice);
                     this.memoryFallbackSlices.put(var13, this.activeSlice);
                  }

                  this.pendingChunkKeys.remove(var13);
                  this.notifySliceUpdated();
                  this.activeSlice = null;
                  this.activeChunkKey = -9223372036854775808L;
                  continue;
               }

               this.loadedSlices.put(var13, this.activeSlice);
            }

            AiPathfinder.LiveGridView.SectionSlice var14 = this.activeSlice;
            int var16 = (int)(this.activeChunkKey >> 32);
            int var18 = (int)this.activeChunkKey;

            while (this.activeColumnCursor < 256 && this.nanoClock.getAsLong() < var3) {
               int var10 = this.activeColumnOrder[this.activeColumnCursor];
               if (var14.ready().get(var10) == 0) {
                  this.scanColumnIntoSlice(var14, (var16 << 4) + var10 / 16, (var18 << 4) + var10 % 16, var5);
                  var14.ready().set(var10, 1);
                  this.notifySliceUpdated();
               }

               this.activeColumnCursor++;
            }

            if (this.activeColumnCursor < 256) {
               return;
            }

            this.loadedSlices.put(this.activeChunkKey, var14);
            this.pendingChunkKeys.remove(this.activeChunkKey);
            this.notifySliceUpdated();
            if (this.chunkMemory != null && this.level != null && var5 <= 128 && this.savingChunkKey == -9223372036854775808L) {
               this.savingChunkKey = this.activeChunkKey;
               this.savingChunkX = var16;
               this.savingChunkZ = var18;
               this.savingFlags = var14.flags();
               this.savingChunkRevision = var14.revision();
               this.savingColumnCursor = 0;
               this.savingYOffset = 0;
               this.savingBlockNames = null;
            }

            this.activeSlice = null;
            this.activeChunkKey = -9223372036854775808L;
         }
      }

      private void promoteLoadedFallbackChunks() {
         Iterator var1 = this.memoryFallbackSlices.entrySet().iterator();

         while (var1.hasNext()) {
            Entry var2 = (Entry)var1.next();
            long var3 = (Long)var2.getKey();
            if (this.cellSource.isChunkLoaded((int)(var3 >> 32), (int)var3)) {
               this.loadedSlices.remove(var3, var2.getValue());
               this.pendingChunkKeys.add(var3);
               var1.remove();
            }
         }
      }

      private boolean stepMemorySave(long var1) {
         int var3 = this.maxY - this.minY + 1;
         MutableBlockPos var4 = new MutableBlockPos();

         while (this.savingColumnCursor < 256) {
            if (this.nanoClock.getAsLong() >= var1) {
               return false;
            }

            int var5 = this.savingColumnCursor >> 4;
            int var6 = this.savingColumnCursor & 15;

            while (this.savingYOffset < var3) {
               int var7 = (var5 * 16 + var6) * var3 + this.savingYOffset;
               short var8 = this.savingFlags[var7];
               if ((var8 & 1) != 0 && AiChunkMemory.init_I(var8) == 1) {
                  var4.set((this.savingChunkX << 4) + var5, this.minY + this.savingYOffset, (this.savingChunkZ << 4) + var6);
                  String var9 = BuiltInRegistries.BLOCK.getKey(this.level.getBlockState(var4).getBlock()).getPath();
                  if (AiChunkMemory.isNotableBlock(var9)) {
                     if (this.savingBlockNames == null) {
                        this.savingBlockNames = new HashMap<>();
                     }

                     this.savingBlockNames.put(this.savingYOffset * 256 + var6 * 16 + var5, "minecraft:" + var9);
                  }
               }

               this.savingYOffset++;
            }

            this.savingYOffset = 0;
            this.savingColumnCursor++;
         }

         long var12 = this.savingChunkKey;
         int var13 = this.savingChunkX;
         int var14 = this.savingChunkZ;
         int var15 = this.savingChunkRevision;
         short[] var10 = this.savingFlags;
         Map var11 = this.savingBlockNames;
         this.savingChunkKey = -9223372036854775808L;
         this.savingFlags = null;
         this.savingBlockNames = null;
         if (var15 == AiGridRevisions.revisionOf(var12)) {
            this.chunkMemory.storeChunk(var13, var14, this.minY, var3, var10, var11);
         }

         return true;
      }

      private void scanColumnIntoSlice(AiPathfinder.LiveGridView.SectionSlice var1, int var2, int var3, int var4) {
         MutableBlockPos var5 = new MutableBlockPos();

         for (int var6 = this.minY; var6 <= this.maxY; var6++) {
            var5.set(var2, var6, var3);
            AiPathfinder.CellInfo var7 = this.cellSource.cellAt(var5);
            int var8 = var1.index(var2, var6, var3, this.minY, var4);
            if (!var7.loaded()) {
               var1.shape()[var8] = 1;
            } else {
               var1.shape()[var8] = this.cellSource.putAt_S(var5);
               var1.surface()[var8] = this.cellSource.putAt_B(var5);
               char var9 = 1;
               if (var7.passable()) {
                  var9 |= 2;
               }

               if (var7.water()) {
                  var9 |= 4;
               }

               if (var7.lava()) {
                  var9 |= '\b';
               }

               if (var7.climbable()) {
                  var9 |= 16;
               }

               if (var7.hazardFeet()) {
                  var9 |= ' ';
               }

               if (var7.hazardFloor()) {
                  var9 |= '@';
               }

               if (var7.falling()) {
                  var9 |= 128;
               }

               if (var7.door()) {
                  var9 |= 256;
               }

               if (var7.slabStair()) {
                  var9 |= 512;
               }

               if (var7.farmland()) {
                  var9 |= 1024;
               }

               if (var7.ice()) {
                  var9 |= 4096;
               }

               if (var7.soulSand()) {
                  var9 |= 8192;
               }

               if (var7.thorny()) {
                  var9 |= 16384;
               }

               if (var7.portal()) {
                  var9 |= '耀';
               }

               var1.flags()[var8] = (short)var9;
               var1.top16()[var8] = (byte)(var7.top16() | (var7.narrowFooting() ? 128 : 0) | (var7.feetTrigger() ? 64 : 0) | (var7.pressurePlate() ? 32 : 0));
               var1.mult10()[var8] = (byte)var7.fallMult10();
               var1.penalties()[var8] = (short)Math.min(65535, var7.destructionPenalty());
               var1.digX4()[var8] = var7.breakTicks() == 1.0 / 0.0 ? -1 : (short)Math.min(65534, (int)Math.round(var7.breakTicks() * 4.0));
            }
         }
      }

      private static int[] spiralColumnOrder(int var0, int var1) {
         int[] var2 = new int[256];
         int var3 = 0;

         for (int var4 = 0; var4 < 16; var4++) {
            for (int var5 = -var4; var5 <= var4; var5++) {
               for (int var6 = -var4; var6 <= var4; var6++) {
                  if (Math.max(Math.abs(var5), Math.abs(var6)) == var4) {
                     int var7 = var0 + var5;
                     int var8 = var1 + var6;
                     if (var7 >= 0 && var7 < 16 && var8 >= 0 && var8 < 16) {
                        var2[var3++] = var7 * 16 + var8;
                     }
                  }
               }
            }
         }

         return var2;
      }

      private AiPathfinder.LiveGridView.SectionSlice allocateSlice(int var1, long var2) {
         return new AiPathfinder.LiveGridView.SectionSlice(
            new short[256 * var1],
            new byte[256 * var1],
            new short[256 * var1],
            new byte[256 * var1],
            AiGridRevisions.revisionOf(var2),
            new short[256 * var1],
            new AtomicIntegerArray(256),
            new short[256 * var1],
            new byte[256 * var1]
         );
      }

      private Long peekNearestPriorityColumn() {
         Long var1 = null;
         long var2 = 9223372036854775807L;

         for (Long var5 : this.priorityColumnKeys) {
            long var6 = (int)(var5 >> 32) - this.focusPos.getX();
            long var8 = var5.intValue() - this.focusPos.getZ();
            long var10 = var6 * var6 + var8 * var8;
            if (var10 < var2 || var10 == var2 && (var1 == null || var5 < var1)) {
               var1 = var5;
               var2 = var10;
            }
         }

         return var1;
      }

      private Long peekNearestPendingChunk() {
         Long var1 = null;
         double var2 = 1.7976931348623157E308;

         for (Long var5 : this.pendingChunkKeys) {
            int var6 = (int)(var5 >> 32);
            int var7 = (int)var5.longValue();
            double var8 = (var6 << 4) + 8 - this.focusPos.getX();
            double var10 = (var7 << 4) + 8 - this.focusPos.getZ();
            double var12 = var8 * var8 + var10 * var10;
            if (var12 < var2) {
               var2 = var12;
               var1 = var5;
            }
         }

         return var1;
      }

      private static long packChunkKey(int var0, int var1) {
         return AiGridRevisions.chunkKey(var0, var1);
      }

      public static AiPathfinder.CellInfo classifyCell(Minecraft var0, ClientLevel var1, BlockPos var2, ItemStack[] var3) {
         return classifyCell(var0, var1, var2, var3, null);
      }

      private static AiPathfinder.CellInfo classifyCell(Minecraft var0, ClientLevel var1, BlockPos var2, ItemStack[] var3, BiPredicate<BlockState, ItemStack> var4) {
         if (var1 != null
            && var1.isInsideBuildHeight(var2)
            && var1.getChunkSource().getChunk(var2.getX() >> 4, var2.getZ() >> 4, ChunkStatus.FULL, false) != null) {
            BlockState var5 = var1.getBlockState(var2);
            boolean var6 = AiEnvironmentHazards.isHazardous(var1, var2);
            if (var5.isAir()) {
               return var6 ? AiPathfinder.CellInfo.air().withHazards(true, false) : AiPathfinder.CellInfo.air();
            }

            FluidState var7 = var5.getFluidState();
            boolean var8 = var7.is(FluidTags.WATER);
            boolean var9 = var7.is(FluidTags.LAVA);
            VoxelShape var10 = var5.getCollisionShape(var1, var2);
            boolean var11 = var10.isEmpty();
            Block var12 = var5.getBlock();
            boolean var13 = false;
            if (var12 instanceof DoorBlock) {
               var13 = DoorBlock.isWoodenDoor(var5);
               if (var13 && var5.getValue(BlockStateProperties.OPEN)) {
                  var11 = true;
               }
            } else if (var12 instanceof FenceGateBlock) {
               var13 = true;
               if (var5.getValue(BlockStateProperties.OPEN)) {
                  var11 = true;
               }
            }

            boolean var14 = isClimbableBlock(var5) || var2.getY() > var1.getMinY() && isClimbableTrapdoor(var5, var1.getBlockState(var2.below()));
            if (var14 && !(var12 instanceof ScaffoldingBlock)) {
               var11 = true;
            }

            int var15 = var11 ? 0 : (int)Math.round(var10.max(Axis.Y) * 16.0);
            boolean var16 = isFeetHazard(var5) || var6;
            boolean var17 = isInvisibleLightBlock(var5);
            boolean var18 = isPressurePlate(var5);
            boolean var19 = var5.is(Blocks.SWEET_BERRY_BUSH);
            boolean var20 = isFloorHazard(var5);
            boolean var21 = !var20 && !var11 && !hasCenterFootingShape(var10);
            boolean var22 = isPortalBlock(var5);
            String var23 = BuiltInRegistries.BLOCK.getKey(var12).getPath();
            int var24 = Math.round(fallDamageMultiplier(var23, var8) * 10.0F);
            boolean var25 = var12 instanceof FallingBlock;
            boolean var26 = var12 instanceof SlabBlock || var12 instanceof StairBlock;
            boolean var27 = var12 instanceof FarmlandBlock;
            boolean var28 = var5.is(Blocks.ICE) || var5.is(Blocks.PACKED_ICE) || var5.is(Blocks.BLUE_ICE) || var5.is(Blocks.FROSTED_ICE);
            boolean var29 = var5.is(Blocks.SOUL_SAND);
            double var30 = 1.0 / 0.0;
            if ((!var11 || var12 instanceof WebBlock) && var7.isEmpty()) {
               float var32 = var5.getDestroySpeed(var1, var2);
               BlockState var33 = var1.getBlockState(var2.below());
               boolean var34 = isDangerousToBreak(
                  var5.is(Blocks.ICE) || var5.is(Blocks.FROSTED_ICE),
                  var33.isSolid(),
                  var33.liquid(),
                  var1.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)
               );
               if (var32 >= 0.0F
                  && !var5.hasBlockEntity()
                  && !var34
                  && isSafeToDigFromHazards(var1x -> probeHazardFlags(var1, var1x), var2)
                  && (var0.player.hasInfiniteMaterials() || !lacksRequiredTool(var5.requiresCorrectToolForDrops(), hasCorrectToolInHotbar(var5, var3)))) {
                  MiningCost.Inner_DataUtil var35 = MiningCost.Inner_DataUtil.of(var0);
                  var30 = var4 == null ? fastestBreakTicks(var0, var5, var32, var3, var35) : fastestBreakTicks(var0, var5, var32, var3, var35, var4);

                  for (BlockPos var36 = var2.above(); var1.isInsideBuildHeight(var36); var36 = var36.above()) {
                     BlockState var37 = var1.getBlockState(var36);
                     if (!(var37.getBlock() instanceof FallingBlock)) {
                        break;
                     }

                     var30 += var4 == null
                        ? fastestBreakTicks(var0, var37, var37.getDestroySpeed(var1, var36), var3, var35)
                        : fastestBreakTicks(var0, var37, var37.getDestroySpeed(var1, var36), var3, var35, var4);
                  }
               }
            }

            return new AiPathfinder.CellInfo(
               true,
               var11,
               var15,
               var8,
               var9,
               var14,
               var16,
               var20 || var21,
               var25,
               var24,
               var30,
               var13,
               var26,
               var27,
               var28,
               false,
               AiPathfinder.MoveCosts.destructionPenaltyForBlock(var23),
               var29,
               var19,
               var22,
               var21,
               var17,
               var18
            );
         } else {
            return AiPathfinder.CellInfo.UNLOADED;
         }
      }

      public static boolean hasCenterFootingShape(VoxelShape var0) {
         if (var0.isEmpty()) {
            return false;
         }

         double var1 = var0.max(Axis.Y);

         for (AABB var4 : var0.toAabbs()) {
            if (!(var4.maxY < var1 - 1.0E-6) && var4.maxX > 0.3 && var4.minX < 0.7 && var4.maxZ > 0.3 && var4.minZ < 0.7) {
               return true;
            }
         }

         return false;
      }

      static boolean isClimbableBlock(BlockState var0) {
         Block var1 = var0.getBlock();
         return var1 instanceof LadderBlock
            || var1 instanceof VineBlock
            || var1 instanceof WeepingVinesBlock
            || var1 instanceof WeepingVinesPlantBlock
            || var1 instanceof TwistingVinesBlock
            || var1 instanceof TwistingVinesPlantBlock
            || var1 instanceof CaveVinesBlock
            || var1 instanceof CaveVinesPlantBlock
            || var1 instanceof ScaffoldingBlock
            || var0.is(BlockTags.CLIMBABLE);
      }

      static boolean isClimbableTrapdoor(BlockState var0, BlockState var1) {
         return var0.getBlock() instanceof TrapDoorBlock
            && var0.getValue(TrapDoorBlock.OPEN)
            && var1 != null
            && var1.getBlock() instanceof LadderBlock
            && var1.getValue(LadderBlock.FACING) == var0.getValue(TrapDoorBlock.FACING);
      }

      public static boolean isFeetHazard(BlockState var0) {
         Block var1 = var0.getBlock();
         return var1 instanceof BaseFireBlock
            || var0.is(Blocks.POWDER_SNOW)
            || var0.is(Blocks.WITHER_ROSE)
            || var0.is(Blocks.LAVA_CAULDRON)
            || var1 instanceof BubbleColumnBlock && var0.getValue(BubbleColumnBlock.DRAG_DOWN);
      }

      public static boolean isInvisibleLightBlock(BlockState var0) {
         return var0.is(Blocks.LIGHT) && var0.getValue(LightBlock.LEVEL) == 0;
      }

      public static boolean isPressurePlate(BlockState var0) {
         return var0.getBlock() instanceof BasePressurePlateBlock;
      }

      public static boolean isFloorHazard(BlockState var0) {
         Block var1 = var0.getBlock();
         return var0.is(Blocks.MAGMA_BLOCK)
            || var0.is(Blocks.LAVA_CAULDRON)
            || var0.is(Blocks.CACTUS)
            || var1 instanceof CampfireBlock && var0.getValue(CampfireBlock.LIT)
            || var1 instanceof PointedDripstoneBlock
            || var1 instanceof SulfurSpikeBlock
            || var1 instanceof PotentSulfurBlock
            || var1 instanceof SculkShriekerBlock && var0.getValue(SculkShriekerBlock.CAN_SUMMON);
      }

      static boolean isPortalBlock(BlockState var0) {
         Block var1 = var0.getBlock();
         return var1 instanceof NetherPortalBlock || var1 instanceof EndPortalBlock || var1 instanceof EndGatewayBlock;
      }

      public static short encodeCollisionShape(ClientLevel var0, BlockPos var1) {
         if (var0 != null
            && var0.isInsideBuildHeight(var1)
            && var0.getChunkSource().getChunk(var1.getX() >> 4, var1.getZ() >> 4, ChunkStatus.FULL, false) != null) {
            BlockState var2 = var0.getBlockState(var1);
            if (var2.isAir()) {
               return 0;
            } else {
               VoxelShape var3 = var2.getCollisionShape(var0, var1);
               if (var3.isEmpty()) {
                  return 0;
               } else {
                  return var3 == Shapes.block() ? 1 : AiShapes.shapeId(var3.toAabbs());
               }
            }
         } else {
            return 1;
         }
      }

      public static byte encodeSurfaceByte(ClientLevel var0, BlockPos var1) {
         if (var0 != null && var0.isInsideBuildHeight(var1)) {
            Block var2 = var0.getBlockState(var1).getBlock();
            double var3 = var2 instanceof SlimeBlock ? 1.0 : (var2 instanceof BedBlock ? 0.75 : 0.0);
            return AiSurface.classify(var2.getFriction(), var2.getSpeedFactor(), var2.getJumpFactor(), var3).id();
         } else {
            return AiSurface.NORMAL.id();
         }
      }

      static boolean isSafeToDigFromHazards(Function<BlockPos, AiPathfinder.LiveGridView.HazardFlags> var0, BlockPos var1) {
         return !hasAdjacentLava(var0, var1) && !hasAdjacentWater(var0, var1) && countFallingAbove(var0, var1) <= 6 && !hasFallingColumnWater(var0, var1);
      }

      static boolean hasFallingColumnWater(Function<BlockPos, AiPathfinder.LiveGridView.HazardFlags> var0, BlockPos var1) {
         BlockPos var2 = var1.above();

         for (int var3 = 0; var3 < 384; var3++) {
            AiPathfinder.LiveGridView.HazardFlags var4 = (AiPathfinder.LiveGridView.HazardFlags)var0.apply(var2);
            if (var4 == null || !var4.falling()) {
               return false;
            }

            if (hasAdjacentWater(var0, var2)) {
               return true;
            }

            var2 = var2.above();
         }

         return false;
      }

      static boolean hasAdjacentWater(Function<BlockPos, AiPathfinder.LiveGridView.HazardFlags> var0, BlockPos var1) {
         for (int[] var5 : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, 0, 1}, {0, 0, -1}}) {
            AiPathfinder.LiveGridView.HazardFlags var6 = (AiPathfinder.LiveGridView.HazardFlags)var0.apply(var1.offset(var5[0], var5[1], var5[2]));
            if (var6 != null && var6.water()) {
               return true;
            }
         }

         return false;
      }

      static boolean hasAdjacentLava(Function<BlockPos, AiPathfinder.LiveGridView.HazardFlags> var0, BlockPos var1) {
         for (int[] var5 : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}) {
            AiPathfinder.LiveGridView.HazardFlags var6 = (AiPathfinder.LiveGridView.HazardFlags)var0.apply(var1.offset(var5[0], var5[1], var5[2]));
            if (var6 != null && var6.lava()) {
               return true;
            }
         }

         return false;
      }

      static int countFallingAbove(Function<BlockPos, AiPathfinder.LiveGridView.HazardFlags> var0, BlockPos var1) {
         int var2 = 0;
         BlockPos var3 = var1.above();

         for (int var4 = 0; var4 < 384; var4++) {
            AiPathfinder.LiveGridView.HazardFlags var5 = (AiPathfinder.LiveGridView.HazardFlags)var0.apply(var3);
            if (var5 == null || !var5.falling()) {
               return var2;
            }

            var2++;
            var3 = var3.above();
         }

         return var2;
      }

      static boolean isSafeToDig(ClientLevel var0, BlockPos var1) {
         return isSafeToDigFromHazards(var1x -> probeHazardFlags(var0, var1x), var1);
      }

      public static boolean isDangerousToBreak(boolean var0, boolean var1, boolean var2, boolean var3) {
         return var0 && !var3 && (var1 || var2);
      }

      public static boolean isLitPortalFrame(BlockGetter var0, BlockPos var1) {
         try {
            for (Direction var5 : Direction.values()) {
               if (var0.getBlockState(var1.relative(var5)).is(Blocks.NETHER_PORTAL)) {
                  return true;
               }
            }

            return false;
         } catch (RuntimeException var6) {
            return false;
         }
      }

      static boolean isSafeToDig(ClientLevel var0, BlockPos var1, Set<BlockPos> var2) {
         return var2.isEmpty()
            ? isSafeToDig(var0, var1)
            : isSafeToDigFromHazards(
               var2x -> {
                  AiPathfinder.LiveGridView.HazardFlags var3 = probeHazardFlags(var0, var2x);
                  return var3 != null && var3.water() && var2.contains(var2x)
                     ? new AiPathfinder.LiveGridView.HazardFlags(var3.falling(), var3.open(), var3.lava(), false)
                     : var3;
               },
               var1
            );
      }

      private static AiPathfinder.LiveGridView.HazardFlags probeHazardFlags(ClientLevel var0, BlockPos var1) {
         if (!var0.isInsideBuildHeight(var1)) {
            return null;
         }

         if (var0.getChunkSource().getChunk(var1.getX() >> 4, var1.getZ() >> 4, ChunkStatus.FULL, false) == null) {
            return new AiPathfinder.LiveGridView.HazardFlags(false, false, true);
         }

         BlockState var2 = var0.getBlockState(var1);
         return new AiPathfinder.LiveGridView.HazardFlags(
            var2.getBlock() instanceof FallingBlock,
            var2.getCollisionShape(var0, var1).isEmpty(),
            var2.getFluidState().is(FluidTags.LAVA),
            var2.getFluidState().is(FluidTags.WATER)
         );
      }

      public static boolean isOreBlock(BlockState var0) {
         return var0.is(BlockTags.IRON_ORES)
            || var0.is(BlockTags.GOLD_ORES)
            || var0.is(BlockTags.COPPER_ORES)
            || BuiltInRegistries.BLOCK.getKey(var0.getBlock()).getPath().endsWith("_ore");
      }

      static boolean lacksRequiredTool(boolean var0, boolean var1) {
         return var0 && !var1;
      }

      private static boolean isUsableCorrectTool(ItemStack var0, BlockState var1) {
         return var0 != null && !var0.isEmpty() && var0.isCorrectToolForDrops(var1) && AiItems.hasEnoughDurability(var0, 2);
      }

      static boolean hasCorrectToolInHotbar(BlockState var0, ItemStack[] var1) {
         for (ItemStack var5 : var1) {
            if (isUsableCorrectTool(var5, var0)) {
               return true;
            }
         }

         return false;
      }

      public static boolean hasCorrectToolInSlots(Minecraft var0, BlockState var1, int var2) {
         for (int var3 = 0; var3 < var2; var3++) {
            if (isUsableCorrectTool(var0.player.getInventory().getItem(var3), var1)) {
               return true;
            }
         }

         return false;
      }

      static float fallDamageMultiplier(String var0, boolean var1) {
         if (var1) {
            return 0.0F;
         }

         float var10000;
         switch (var0) {
            case "cobweb":
            case "powder_snow":
               var10000 = 0.0F;
               break;
            case "hay_block":
            case "honey_block":
               var10000 = 0.2F;
               break;
            case "slime_block":
               var10000 = 0.0F;
               break;
            default:
               var10000 = var0.endsWith("_bed") ? 0.5F : 1.0F;
         }

         return var10000;
      }

      private static double fastestBreakTicks(Minecraft var0, BlockState var1, float var2, ItemStack[] var3) {
         return fastestBreakTicks(var0, var1, var2, var3, MiningCost.Inner_DataUtil.of(var0));
      }

      static double fastestBreakTicks(Minecraft var0, BlockState var1, float var2, ItemStack[] var3, MiningCost.Inner_DataUtil var4) {
         double var5 = MiningCost.breakTicks(var0, ItemStack.EMPTY, var1, var2, var4);

         for (ItemStack var10 : var3) {
            if (var10 != null && !var10.isEmpty() && AiItems.hasEnoughDurability(var10, 2)) {
               var5 = Math.min(var5, MiningCost.breakTicks(var0, var10, var1, var2, var4));
            }
         }

         return var5;
      }

      static double fastestBreakTicks(
         Minecraft var0, BlockState var1, float var2, ItemStack[] var3, MiningCost.Inner_DataUtil var4, BiPredicate<BlockState, ItemStack> var5
      ) {
         var4 = adjustMiningEnvForAcceptedTool(var4, var3, var1, var5);
         boolean var6 = var1.requiresCorrectToolForDrops() && !var4.creative();
         double var7 = !var6 && var5.test(var1, ItemStack.EMPTY) ? MiningCost.breakTicks(var0, ItemStack.EMPTY, var1, var2, var4) : 1.0 / 0.0;

         for (ItemStack var12 : var3) {
            if (var12 != null && !var12.isEmpty() && AiItems.hasEnoughDurability(var12, 2) && (!var6 || var12.isCorrectToolForDrops(var1)) && var5.test(var1, var12)) {
               var7 = Math.min(var7, MiningCost.breakTicks(var0, var12, var1, var2, var4));
            }
         }

         return var7;
      }

      static MiningCost.Inner_DataUtil adjustMiningEnvForAcceptedTool(
         MiningCost.Inner_DataUtil var0, ItemStack[] var1, BlockState var2, BiPredicate<BlockState, ItemStack> var3
      ) {
         if (!var0.stonePick()) {
            return var0;
         }

         for (ItemStack var7 : var1) {
            if (var7 != null && var7.is(Items.STONE_PICKAXE) && AiItems.hasEnoughDurability(var7, 2) && var3.test(var2, var7)) {
               return var0;
            }
         }

         return new MiningCost.Inner_DataUtil(var0.haste(), var0.fatigue(), var0.blockSpeed(), var0.creative(), var0.ironReserve(), false);
      }

      record SectionSlice(
         short[] flags, byte[] top16, short[] digX4, byte[] mult10, int revision, short[] penalties, AtomicIntegerArray ready, short[] shape, byte[] surface
      ) {
         static final int LOADED = 1;
         static final int PASSABLE = 2;
         static final int WATER = 4;
         static final int LAVA = 8;
         static final int CLIMB = 16;
         static final int HAZ_FEET = 32;
         static final int HAZ_FLOOR = 64;
         static final int FALLING = 128;
         static final int DOOR = 256;
         static final int SLAB_STAIR = 512;
         static final int FARMLAND = 1024;
         static final int CACHED = 2048;
         static final int ICE = 4096;
         static final int SOUL_SAND = 8192;
         static final int THORNY = 16384;
         static final int PORTAL = 32768;
         static final int NARROW_FOOTING = 128;
         static final int FEET_TRIGGER = 64;
         static final int PRESSURE_PLATE = 32;
         static final int HEIGHT_MASK = 31;

         SectionSlice(short[] var1, byte[] var2, short[] var3, byte[] var4, int var5) {
            this(var1, var2, var3, var4, var5, new short[var1.length], allReady(), null, null);
         }

         private static AtomicIntegerArray allReady() {
            int[] var0 = new int[256];
            Arrays.fill(var0, 1);
            return new AtomicIntegerArray(var0);
         }

         int index(int var1, int var2, int var3, int var4, int var5) {
            return ((var1 & 15) * 16 + (var3 & 15)) * var5 + (var2 - var4);
         }
      }

      record HazardFlags(boolean falling, boolean open, boolean lava, boolean water) {
         HazardFlags(boolean var1, boolean var2, boolean var3) {
            this(var1, var2, var3, false);
         }
      }

      interface CellSource {
         AiPathfinder.CellInfo cellAt(BlockPos var1);

         boolean isChunkLoaded(int var1, int var2);

         default short putAt_S(BlockPos var1) {
            return AiShapes.shapeIdOf(this.cellAt(var1));
         }

         default byte putAt_B(BlockPos var1) {
            return AiSurface.of(this.cellAt(var1)).id();
         }
      }
   }

   public static final class RouteBlacklist {
      static final long INITIAL_BUDGET_NANOS = 4000000000L;
      static final long EXTENDED_BUDGET_NANOS = 10000000000L;
      private static final int SEGMENT_REPLAN_LOOKAHEAD = 32;
      private long awaitTerrainRetryNanos;
      private static final long BLACKLIST_TTL_NANOS = 30000000000L;
      private static final double BLACKLIST_CLEAR_DIST_SQ = 64.0;
      private final AiPathfinder.GridView grid;
      private final LongSupplier nanoClock;
      private final Map<Long, AiPathfinder.RouteBlacklist.BlacklistEntry> blacklistedCells = new ConcurrentHashMap<>();
      private final Set<Long> blacklistedCellKeys = this.blacklistedCells.keySet();
      private final Map<AiPathfinder.MoveKey, AiPathfinder.RouteBlacklist.BlacklistEntry> blacklistedMoves = new HashMap<>();
      private final AtomicInteger generation = new AtomicInteger();
      private volatile boolean closed;
      private volatile AiPathfinder.RouteBlacklist.CachedSegment completedSegment;
      private volatile String lastError;
      private volatile AiPathfinder.SearchStats lastStats;
      private volatile AiPathfinder.SearchProgress liveProgress;
      private final boolean unlimitedBudget;
      private final double overrideSpeed;
      private final boolean worldRecordOptimize;
      private AiPathfinder.PathGoal goal = AiPathfinder.PathGoal.near(BlockPos.ZERO, 1.0);
      private AiPathfinder.PathOptions options = AiPathfinder.PathOptions.WALK_ONLY;
      private Thread plannerThread;
      private BlockPos activeOrigin = BlockPos.ZERO;
      private AiPathfinder.PathResult currentPath;
      private AiPathfinder.PathResult queuedNextPath;
      private AiPathfinder.PathResult readyReplacementPath;
      private boolean precomputingNext;
      private long replanCooldownUntilNanos;
      private volatile long searchStartedNanos;
      private volatile boolean trimExtendedBudget;

      public AiPathfinder.SearchProgress liveProgress() {
         return this.liveProgress;
      }

      public AiPathfinder.SearchStats lastStats() {
         return this.lastStats;
      }

      String lastError() {
         return this.lastError;
      }

      public RouteBlacklist(AiPathfinder.GridView var1) {
         this(var1, System::nanoTime, false);
      }

      RouteBlacklist(AiPathfinder.GridView var1, boolean var2) {
         this(var1, System::nanoTime, var2);
      }

      RouteBlacklist(AiPathfinder.GridView var1, boolean var2, double var3) {
         this(var1, var2, var3, false);
      }

      RouteBlacklist(AiPathfinder.GridView var1, boolean var2, double var3, boolean var5) {
         this(var1, System::nanoTime, var2, var3, var5);
      }

      RouteBlacklist(AiPathfinder.GridView var1, LongSupplier var2) {
         this(var1, var2, false);
      }

      private RouteBlacklist(AiPathfinder.GridView var1, LongSupplier var2, boolean var3) {
         this(var1, var2, var3, 0.0 / 0.0, false);
      }

      private RouteBlacklist(AiPathfinder.GridView var1, LongSupplier var2, boolean var3, double var4, boolean var6) {
         this.unlimitedBudget = var3;
         this.grid = var1;
         this.nanoClock = var2;
         this.overrideSpeed = var4;
         this.worldRecordOptimize = var6;
      }

      double effectiveSpeed() {
         return Double.isNaN(this.overrideSpeed) ? this.grid.movementSpeed() : this.overrideSpeed;
      }

      public void startSearch(BlockPos var1, AiPathfinder.PathGoal var2, AiPathfinder.PathOptions var3) {
         this.closed = false;
         this.goal = var2;
         this.options = var3;
         this.currentPath = null;
         this.queuedNextPath = null;
         this.readyReplacementPath = null;
         this.precomputingNext = false;
         this.launchSearch(var1);
      }

      public void updateOptions(AiPathfinder.PathOptions var1) {
         this.options = var1;
      }

      public synchronized void updateGoal(AiPathfinder.PathGoal var1) {
         if (this.goal != var1) {
            this.goal = var1;
            if (this.currentPath == null) {
               this.generation.incrementAndGet();
               this.completedSegment = null;
               this.readyReplacementPath = this.queuedNextPath = null;
               this.precomputingNext = false;
               if (this.plannerThread != null) {
                  this.plannerThread.interrupt();
               }
            } else {
               if (this.queuedNextPath != null) {
                  BlockPos var2 = this.queuedNextPath.destination();
                  if (var2 == null || !var1.isInGoal(var2.getX(), var2.getY(), var2.getZ())) {
                     this.queuedNextPath = null;
                  }
               }

               if (this.precomputingNext) {
                  this.generation.incrementAndGet();
                  this.completedSegment = null;
                  if (this.plannerThread != null) {
                     this.plannerThread.interrupt();
                  }

                  this.precomputingNext = false;
               }
            }
         }
      }

      public void replanFrom(BlockPos var1) {
         this.queuedNextPath = null;
         this.currentPath = null;
         this.readyReplacementPath = null;
         this.precomputingNext = false;
         this.replanCooldownUntilNanos = System.nanoTime() + 200000000L;
         this.launchSearch(var1);
      }

      public void blacklistCell(BlockPos var1, BlockPos var2) {
         this.blacklistedCells
            .put(
               var1.asLong(),
               new AiPathfinder.RouteBlacklist.BlacklistEntry(this.nanoClock.getAsLong() + 30000000000L, var2.getX(), var2.getY(), var2.getZ())
            );
      }

      void blacklistMove(AiPathfinder.PathStep var1, BlockPos var2) {
         this.blacklistedMoves
            .put(
               AiPathfinder.MoveKey.of(var1),
               new AiPathfinder.RouteBlacklist.BlacklistEntry(this.nanoClock.getAsLong() + 30000000000L, var2.getX(), var2.getY(), var2.getZ())
            );
      }

      public int blacklistedCellCount() {
         return this.blacklistedCells.size();
      }

      Set<Long> blacklistedCellKeys() {
         return this.blacklistedCellKeys;
      }

      public boolean isPlanning() {
         return this.readyReplacementPath == null && this.plannerThread != null && this.plannerThread.isAlive() || this.completedSegment != null;
      }

      public boolean isPrecomputingNext() {
         return this.precomputingNext && this.queuedNextPath == null;
      }

      public boolean awaitInitialResult(long var1) {
         if (this.grid instanceof AiPathfinder.LiveGridView) {
            return this.completedSegment != null;
         }

         if (System.nanoTime() - this.searchStartedNanos > var1) {
            return this.completedSegment != null;
         }

         long var3 = System.nanoTime() + var1;
         synchronized (this) {
            while (this.completedSegment == null && this.lastError == null && !this.closed) {
               Thread var6 = this.plannerThread;
               if (var6 == null || !var6.isAlive()) {
                  break;
               }

               long var7 = var3 - System.nanoTime();
               if (var7 <= 0L) {
                  break;
               }

               try {
                  this.wait(var7 / 1000000L, (int)(var7 % 1000000L));
               } catch (InterruptedException var11) {
                  Thread.currentThread().interrupt();
                  break;
               }
            }

            return this.completedSegment != null;
         }
      }

      public boolean awaitResultBlocking(long var1) {
         long var3 = System.nanoTime() + var1;
         synchronized (this) {
            while (this.completedSegment == null && this.lastError == null && !this.closed) {
               Thread var6 = this.plannerThread;
               if (var6 == null || !var6.isAlive()) {
                  break;
               }

               long var7 = var3 - System.nanoTime();
               if (var7 <= 0L) {
                  break;
               }

               try {
                  this.wait(var7 / 1000000L, (int)(var7 % 1000000L));
               } catch (InterruptedException var11) {
                  Thread.currentThread().interrupt();
                  break;
               }
            }

            return this.completedSegment != null || this.lastError != null || this.closed || this.plannerThread == null || !this.plannerThread.isAlive();
         }
      }

      public AiPathfinder.PathResult pollReplacementPath() {
         AiPathfinder.PathResult var1 = this.readyReplacementPath;
         this.readyReplacementPath = null;
         return var1;
      }

      public void setCurrentPath(AiPathfinder.PathResult var1) {
         this.currentPath = var1;
         if (this.queuedNextPath == var1) {
            this.queuedNextPath = null;
         }
      }

      void installPathAndStop(AiPathfinder.PathResult var1) {
         this.close();
         this.closed = false;
         this.precomputingNext = false;
         this.setCurrentPath(var1);
      }

      public AiPathfinder.PathResult peekQueuedNextPath() {
         return this.queuedNextPath;
      }

      public AiPathfinder.PathResult pollQueuedNextPath() {
         AiPathfinder.PathResult var1 = this.queuedNextPath;
         this.queuedNextPath = null;
         return var1;
      }

      public void tickDriver(AiPathfinder.SearchTask var1) {
         this.pruneExpiredBlacklists(var1.currentPos());
         AiPathfinder.RouteBlacklist.CachedSegment var2 = this.completedSegment;
         if (var2 != null) {
            if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
               AiNavDebug.log(
                  "driver result gen="
                     + var2.generation()
                     + " currentGen="
                     + this.generation.get()
                     + " from="
                     + var2.from()
                     + " current="
                     + (this.currentPath == null ? null : this.currentPath.destination())
               );
            }

            if (var2.generation() == this.generation.get()) {
               this.completedSegment = null;
               if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
                  AiNavDebug.log(
                     "driver plan done from="
                        + var2.from()
                        + " current="
                        + (this.currentPath == null ? null : this.currentPath.destination())
                        + " moves="
                        + var2.segment().movements().size()
                        + " segment="
                        + var2.segment().movements()
                  );
               }

               if (this.currentPath == null) {
                  this.readyReplacementPath = var2.segment();
               } else {
                  this.precomputingNext = false;
                  var var3 = var2.segment().movements();
                  if (var2.from().equals(this.currentPath.destination())
                     && (var3.isEmpty() || ((AiPathfinder.PathStep)var3.get(0)).from().equals(this.currentPath.destination()))) {
                     this.queuedNextPath = var2.segment();
                     if (this.queuedNextPath.awaitingTerrain()) {
                        this.awaitTerrainRetryNanos = System.nanoTime() + 250000000L;
                     }
                  }
               }
            } else {
               this.completedSegment = null;
            }
         }

         if (this.queuedNextPath != null && this.queuedNextPath.awaitingTerrain() && System.nanoTime() >= this.awaitTerrainRetryNanos) {
            this.queuedNextPath = null;
         }

         if (this.currentPath != null && this.queuedNextPath == null && !this.precomputingNext && var1.isFollowing()) {
            if (System.nanoTime() >= this.replanCooldownUntilNanos) {
               BlockPos var4 = this.currentPath.destination();
               if (var4 != null && !this.goal.goalReached(var4.getX(), var4.getY(), var4.getZ(), this.grid)) {
                  this.precomputingNext = true;
                  this.launchSearchOnGrid(
                     var4, var1.pendingPlacementCount(), true, AiPathfinder.withSimulatedEdits(this.grid, var1.remainingMoves())
                  );
               }
            }
         }
      }

      private void pruneExpiredBlacklists(BlockPos var1) {
         if (!this.blacklistedCells.isEmpty() || !this.blacklistedMoves.isEmpty()) {
            long var2 = this.nanoClock.getAsLong();
            this.blacklistedCells.values().removeIf(var3 -> isBlacklistEntryExpired(var3, var1, var2));
            this.blacklistedMoves.values().removeIf(var3 -> isBlacklistEntryExpired(var3, var1, var2));
         }
      }

      private static boolean isBlacklistEntryExpired(AiPathfinder.RouteBlacklist.BlacklistEntry var0, BlockPos var1, long var2) {
         double var4 = var1.getX() - var0.x();
         double var6 = var1.getY() - var0.y();
         double var8 = var1.getZ() - var0.z();
         return var2 >= var0.expiresAt() || var4 * var4 + var6 * var6 + var8 * var8 >= 64.0;
      }

      public BlockPos activeOrigin() {
         return this.activeOrigin;
      }

      public synchronized void close() {
         this.closed = true;
         this.liveProgress = null;
         this.generation.incrementAndGet();
         this.completedSegment = null;
         this.lastError = null;
         this.readyReplacementPath = this.queuedNextPath = this.currentPath = null;
         Thread var1 = this.plannerThread;
         if (var1 != null) {
            var1.interrupt();
         }
      }

      private void launchSearch(BlockPos var1) {
         this.launchSearch(var1, 0, false);
      }

      private synchronized void launchSearch(BlockPos var1, int var2, boolean var3) {
         this.launchSearchOnGrid(var1, var2, var3, this.grid);
      }

      public void requestBudgetTrim() {
         this.trimExtendedBudget = true;
      }

      private synchronized void launchSearchOnGrid(BlockPos var1, int var2, boolean var3, AiPathfinder.GridView var4) {
         this.trimExtendedBudget = false;
         if (this.plannerThread != null) {
            this.plannerThread.interrupt();
         }

         this.searchStartedNanos = System.nanoTime();
         int var5 = this.generation.incrementAndGet();
         this.completedSegment = null;
         this.lastError = null;
         this.activeOrigin = var1.immutable();
         AiPathfinder.PathGoal var6 = this.goal;
         AiPathfinder.PathOptions var7 = this.options
            .withConstructionBlocks(Math.max(0, this.options.constructionBlocks() - var2));
         var var8 = Set.copyOf(this.blacklistedCellKeys);
         Set var9;
         if (var3 && this.currentPath != null && !this.currentPath.movements().isEmpty()) {
            var9 = new HashSet<>(this.blacklistedMoves.keySet());
            AiPathfinder.PathStep var10 = this.currentPath.movements().getLast();
            var9.add(new AiPathfinder.MoveKey(var10.type(), var10.to().asLong(), var10.from().asLong()));
         } else {
            var9 = Set.copyOf(this.blacklistedMoves.keySet());
         }

         double var13 = this.effectiveSpeed();
         Thread var12 = Thread.ofVirtual().unstarted(() -> {
            try {
               this.runSearchLoop(var5, var1.immutable(), var6, var7, var8, var9, var3, var4, var13);
            } catch (RuntimeException var15) {
               RuntimeException var11 = var15;
               synchronized (this) {
                  if (this.closed || this.generation.get() != var5) {
                     return;
                  }

                  this.lastError = "Route planner failed (" + var11.getClass().getSimpleName() + ")";
                  this.liveProgress = null;
                  this.notifyAll();
               }

               AutismClientAddon.LOG.error("[AI] Route planner failed from {}", var1.toShortString(), var15);
            }
         });
         this.plannerThread = var12;
         var12.start();
      }

      private void runSearchLoop(
         int var1,
         BlockPos var2,
         AiPathfinder.PathGoal var3,
         AiPathfinder.PathOptions var4,
         Set<Long> var5,
         Set<AiPathfinder.MoveKey> var6,
         boolean var7,
         AiPathfinder.GridView var8,
         double var9
      ) {
         long var11 = System.nanoTime();
         long var13 = 0L;
         long var15 = 0L;
         long var17 = this.unlimitedBudget ? 9223372036854775807L : var11 + (var7 ? 10000000000L : 4000000000L);
         boolean var19 = this.unlimitedBudget && var4.allowParkour() && var4.strategy() == AiPathfinder.RouteStrategy.FASTEST;
         boolean var20 = var19 && !var4.allowDig() && !var4.allowBridge() && !var4.allowPillar();
         BlockPos var21 = var3.goalTarget();
         int var22 = var21 == null ? 0 : var21.getY() - var2.getY();
         boolean var23 = var20
            && var21 != null
            && (var22 >= 16 || var22 <= -16 && !var4.fallPolicy().allowsExtendedDrop())
            && Math.hypot(var2.getX() - var21.getX(), var2.getZ() - var21.getZ()) <= 32.0;
         if (var23) {
            var20 = false;
         }

         boolean var24 = var23;
         boolean var25 = var20 && var4.fallPolicy().allowsExtendedDrop() && var21 != null && var2.distSqr(var21) >= 1024.0;
         boolean var26 = var25 && var9 <= 0.100001;
         boolean var27 = var26;
         boolean var28 = var20 && var22 <= -16 && var4.fallPolicy().allowsExtendedDrop() && Math.hypot(var2.getX() - var21.getX(), var2.getZ() - var21.getZ()) <= 32.0;
         boolean var29 = var25 && isHighDropLongRoute(var2, var21);
         boolean var30 = var29;
         long var31 = var11;
         AiPathfinder.PathResult var33 = null;
         AiPathfinder.PathResult var34 = null;
         int var35 = 0;
         int var36 = 0;
         int var37 = 0;
         int var38 = 0;
         boolean var39 = false;
         boolean var40 = false;

         while (!this.closed && this.generation.get() == var1) {
            long var41 = System.nanoTime();
            long var43 = this.grid instanceof AiPathfinder.LiveGridView var45 ? var45.changeVersion() : this.grid.revision();
            AiPathfinder.SearchSession var60 = AiPathfinder.SearchSession.createProofSession(
               var8, var2, var3, var19 ? var4.withoutConstruction() : var4, var5, var19, this.unlimitedBudget, var9
            );
            var60.ignoreGlobalBlacklist = this.unlimitedBudget;
            var60.blacklistedMoves = var6;
            if ((var29 || var28) && !var60.destinationUnknown) {
               var60.corridorTarget = var21;
               var60.slopedCorridorFloor = var30;
            }

            var60.cachedGrid.quickJumpMode = var27 && !var60.destinationUnknown;
            var60.cachedGrid.reducedJumpMode = var20 && !var19 && !var60.destinationUnknown;
            var60.cachedGrid.allowManeuverShortcuts = var24 && !var19 && !var60.destinationUnknown;
            var60.cachedGrid.limitJumpDropDepth = var28 || var60.slopedCorridorFloor || var25 && var60.cachedGrid.reducedJumpMode;
            if (var19 && var23 && var22 >= 16) {
               var60.openSet.setWeight(1.45);
            }

            if (var19
               && !var60.destinationUnknown
               && !var23
               && !var28
               && var21 != null
               && var4.fallPolicy().allowsExtendedDrop()
               && Math.abs(var22) <= 12
               && var2.distSqr(var21) <= 4096.0
               && var9 > 0.100001) {
               var60.openSet.setWeight(1.0);
            }

            if (var60.cachedGrid.allowManeuverShortcuts) {
               var60.openSet.setWeight(1.55);
            }

            if (var35 > 0) {
               var60.worldRecordMode = true;
               var60.trackHeadingState = var35 == 1;
               if (var35 == 1) {
                  var60.searchBounds = AiPathfinder.PathBounds.around(var34);
               }

               var60.cachedGrid.allowDeepPlatformLanding = var35 == 2;
               var60.openSet.setWeight(var35 == 1 ? 1.55 : 2.05);
               var60.cachedGrid.limitJumpDropDepth = false;
               var60.cachedGrid.quickJumpMode = false;
               var60.cachedGrid.allowManeuverShortcuts = false;
            }

            if (this.unlimitedBudget && (!var19 || var40) && var60.corridorTarget == null) {
               var60.horizRadius = var60.vertRadius = 2147483647;
            }

            boolean var61 = false;
            boolean var47 = false;
            long var48 = var11;

            while (
               !this.closed
                  && this.generation.get() == var1
                  && (var35 == 0 || System.nanoTime() - var41 < 8000000000L)
                  && System.nanoTime() < (!this.unlimitedBudget && var7 && this.trimExtendedBudget ? Math.min(var17, var11 + 4000000000L) : var17)
                  && !(var61 = var60.step(16))
            ) {
               long var50 = System.nanoTime();
               if (this.unlimitedBudget && var60.destinationUnknown && !var39 && var50 >= var48) {
                  var47 = isCellLoadedLive(var8, var21);
                  if (var47) {
                     break;
                  }

                  var48 = var50 + 250000000L;
               }

               if (this.unlimitedBudget && !var7 && var50 >= var31) {
                  synchronized (this) {
                     if (this.generation.get() == var1) {
                        this.liveProgress = new AiPathfinder.SearchProgress(var37 + var60.expandedCount(), var60.currentBestTrail());
                     }
                  }

                  var31 = var50 + 350000000L;
               }
            }

            if (!var61) {
               var60.finish();
            }

            var33 = var60.result();
            if (AiNavDebug.isEnabled() && this.unlimitedBudget) {
               AiNavDebug.log(
                  "route round "
                     + (var36 + 1)
                     + ": "
                     + (System.nanoTime() - var41) / 1000000L
                     + " ms, ordinary="
                     + var19
                     + ", quick="
                     + var60.cachedGrid.quickJumpMode
                     + ", reduced="
                     + var60.cachedGrid.reducedJumpMode
                     + ", expanded="
                     + var60.expandedCount()
                     + ", jump solves="
                     + var60.jumpSolveCount()
                     + ", cache hits="
                     + var60.cachedGrid.jumpCacheHits
                     + ", reached="
                     + var33.reachesGoal()
               );
            }

            var36++;
            var13 += var60.blockQueryCount();
            var15 += var60.generatedCount();
            var37 += var60.expandedCount();
            var38 = Math.max(var38, var60.peakOpenSize());
            if (this.closed || this.generation.get() != var1) {
               return;
            }

            if (this.unlimitedBudget && var60.destinationUnknown && !var39 && (var47 || isCellLoadedLive(var8, var21))) {
               var39 = true;
               continue;
            }

            if (!this.worldRecordOptimize || var7 || var60.destinationUnknown || !var33.reachesGoal() && var34 == null) {
               if ((var60.corridorTarget != null || var19 || var60.cachedGrid.reducedJumpMode || var60.cachedGrid.allowManeuverShortcuts)
                  && !var33.reachesGoal()
                  && !var33.awaitingTerrain()
                  && var33.movements().isEmpty()
                  && !var33.hitPending()) {
                  if (var60.corridorTarget != null) {
                     if (var28) {
                        var28 = false;
                     } else if (var60.slopedCorridorFloor) {
                        var30 = false;
                     } else if (var60.cachedGrid.quickJumpMode) {
                        var27 = false;
                     } else if (var19) {
                        var19 = false;
                     } else {
                        var29 = false;
                        var19 = true;
                        var27 = var26;
                     }
                     continue;
                  }

                  if (var19 && var60.hitSearchBoxLimit && !var40) {
                     var40 = true;
                     continue;
                  }

                  if (var60.cachedGrid.quickJumpMode) {
                     var27 = false;
                  } else if (var19) {
                     var19 = false;
                  } else if (var60.cachedGrid.limitJumpDropDepth) {
                     var25 = false;
                  } else if (var60.cachedGrid.reducedJumpMode) {
                     var20 = false;
                  } else {
                     var24 = false;
                  }
                  continue;
               }

               if (!this.unlimitedBudget && var33.exhausted() && !var33.hitPending() && !var33.reachesGoal() && System.nanoTime() < var17) {
                  continue;
               }

               if (var33.hitPending() && !var33.reachesGoal() && !var33.awaitingTerrain() && var33.movements().isEmpty() && System.nanoTime() < var17) {
                  try {
                     if (this.grid instanceof AiPathfinder.LiveGridView var62) {
                        var62.awaitChangeVersion(var43, var17);
                        continue;
                     }
                  } catch (InterruptedException var56) {
                     return;
                  }
               }
               break;
            }

            if (var33.reachesGoal()) {
               var33 = AiPathfinder.optimizeWithShortcutJumps(var33, var8, var4, var9, var5, var6);
            }

            if (var33.reachesGoal() && (var34 == null || AiPathfinder.scorePathDuration(var33) < AiPathfinder.scorePathDuration(var34) - 0.5)) {
               var34 = var33;
            }

            AiNavDebug.log(
               "Parkour WR pass "
                  + (var35 + 1)
                  + " from "
                  + var2.toShortString()
                  + ": "
                  + (var33.reachesGoal() ? String.format(Locale.ROOT, "%.1f ticks", AiPathfinder.scorePathDuration(var33)) : "no faster proof")
            );
            if (var35 < 2 && var34.movements().size() > 2) {
               var35++;
               var19 = var35 == 1;
               var27 = false;
               var29 = false;
               var28 = false;
               var40 = false;
               continue;
            }

            var33 = AiPathfinder.optimizeWithPaceJumps(var34, var8, var4, var9);
            break;
         }

         synchronized (this) {
            if (!this.closed && this.generation.get() == var1 && var33 != null) {
               this.lastStats = new AiPathfinder.SearchStats(System.nanoTime() - var11, var36, var37, var15, var13, var38);
               this.liveProgress = null;
               this.completedSegment = new AiPathfinder.RouteBlacklist.CachedSegment(var1, var2, var33);
            }

            this.notifyAll();
         }

         long var58 = (System.nanoTime() - var11) / 1000000L;
         if (var33 != null && (var58 >= 400L && !var7 || AiNavDebug.isEnabled())) {
            String var59 = String.format(
               Locale.ROOT,
               "plan%s from %s: %d ms, %d expanded, %d rounds, %s, %d moves%s",
               var7 ? " ahead" : "",
               var2.toShortString(),
               var58,
               var37,
               var36,
               var33.reachesGoal() ? "reaches the goal" : "partial",
               var33.movements().size(),
               var33.movements().isEmpty() ? "" : " to " + var33.movements().get(var33.movements().size() - 1).to().toShortString()
            );
            if (var58 >= 400L && !var7) {
               AutismClientAddon.LOG.info("[AI] slow " + var59);
            } else {
               AiNavDebug.log(var59);
            }
         }
      }

      private static boolean isHighDropLongRoute(BlockPos var0, BlockPos var1) {
         return var0.getY() - var1.getY() >= 12 && Math.hypot(var0.getX() - var1.getX(), var0.getZ() - var1.getZ()) >= 32.0;
      }

      private static boolean isCellLoadedLive(AiPathfinder.GridView var0, BlockPos var1) {
         AiPathfinder.CellInfo var2 = var0.probe(var1.getX(), var1.getY(), var1.getZ());
         return var2 != null && var2.loaded() && !var2.cached();
      }

      private record BlacklistEntry(long expiresAt, double x, double y, double z) {
      }

      private record CachedSegment(int generation, BlockPos from, AiPathfinder.PathResult segment) {
      }
   }

   public interface ThreatField {
      AiPathfinder.ThreatField NONE = (var0, var1, var2) -> 0.0;
      AiPathfinder.ThreatField NONE_COMPAT = NONE;
      static AiPathfinder.ThreatField fromMobsCompat(List<BlockPos> mobs) { return fromMobs(mobs); }
      default double init(int x, int y, int z) { return this.cellSurcharge(x, y, z); }
      default double init(int fx, int fy, int fz, int tx, int ty, int tz) { return this.moveSurcharge(fx, fy, fz, tx, ty, tz); }

      double surcharge(int var1, int var2, int var3);

      default double cellSurcharge(int var1, int var2, int var3) {
         return 0.0;
      }

      default double moveSurcharge(int var1, int var2, int var3, int var4, int var5, int var6) {
         return 0.0;
      }

      static AiPathfinder.ThreatField fromMobs(List<BlockPos> var0) {
         var var1 = List.copyOf(var0);
         return var1.isEmpty() ? NONE : (var1x, var2, var3) -> {
            double var4 = 1.7976931348623157E308;

            for (BlockPos var7 : var1) {
               double var8 = var1x - var7.getX();
               double var10 = var2 - var7.getY();
               double var12 = var3 - var7.getZ();
               var4 = Math.min(var4, var8 * var8 + var10 * var10 + var12 * var12);
            }

            double var14 = 12.0;
            return var4 >= var14 * var14 ? 0.0 : 60.0 * (1.0 - Math.sqrt(var4) / var14);
         };
      }
   }

   private record TrailNode(AiPathfinder.PathStep first, AiPathfinder.TrailNode next, double ticks) {
   }

   public interface GridView {
      AiPathfinder.CellInfo probe(int var1, int var2, int var3);

      default AiPathfinder.CellInfo probeCell(int var1, int var2, int var3) {
         return this.probe(var1, var2, var3);
      }

      default long init_J() { return this.revision(); }
      default short init_S_I(int x, int y, int z) { return this.putAt_S_I(x, y, z); }
      default short collisionShapeCode(int x, int y, int z) { return this.fill_S(x, y, z); }
      default byte init_B_I(int x, int y, int z) { return this.putAt_B_I(x, y, z); }

      default long revision() {
         return 0L;
      }

      default short putAt_S_I(int var1, int var2, int var3) {
         return AiShapes.shapeIdOf(this.probe(var1, var2, var3));
      }

      default short fill_S(int var1, int var2, int var3) {
         return this.putAt_S_I(var1, var2, var3);
      }

      default byte putAt_B_I(int var1, int var2, int var3) {
         return AiSurface.of(this.probe(var1, var2, var3)).id();
      }

      default double movementSpeed() {
         return 0.1;
      }

      default boolean putAt_Z() {
         return false;
      }

      default AiPathfinder.CellOccupancy putAt_FileStore_IoCodec_I(int var1, int var2, int var3, boolean var4) {
         long var5 = 0L;
         boolean var7 = false;

         for (int var8 = 0; var8 < 64; var8++) {
            AiPathfinder.CellInfo var9 = this.probe(var1, var2 + var8, var3);
            if (var9 == null) {
               var7 = true;
            } else if (var9.loaded() && (!var4 || !var9.cached()) && !var9.passable()) {
               var5 |= 1L << var8;
            }
         }

         return new AiPathfinder.CellOccupancy(var5, var7);
      }
   }

   public interface PathGoal {
      // 5.3 external ABI bridges for untouched ai/* goals
      default boolean isInGoalWithGrid(int x, int y, int z, AiPathfinder.GridView planned) { return this.isInGoal(x, y, z); }
      default AiPathfinder.PathGoal bindToGrid(AiPathfinder.GridView grid) { return this; }
      default boolean allowsCell(BlockPos cell) { return true; }
      default boolean mayPlace(BlockPos cell) { return true; }
      default boolean init_Z() { return false; }
      default boolean mayBreak(BlockPos cell) { return true; }
      default int init(ItemStack stack) { return 0; }
      default double blockPlaceCost() { return 40.0; }
      default boolean wantsBroken(BlockPos cell) { return false; }
      default Set<AiPathfinder.MoveType> disallowedMoves() { return Set.of(); }
      default BlockPos getTargetBlock() { return null; }
      static AiPathfinder.PathGoal nearCompat(BlockPos target, double radius) {
         return near(target, radius);
      }
      boolean isInGoal(int var1, int var2, int var3);

      default boolean goalReached(int var1, int var2, int var3, AiPathfinder.GridView var4) {
         return this.isInGoalWithGrid(var1, var2, var3, var4);
      }

      double heuristic(int var1, int var2, int var3);

      default AiPathfinder.PathGoal putAt_FileStore_TokenCod_FileStore_TextStor(AiPathfinder.GridView var1) {
         return this.bindToGrid(var1);
      }

      default boolean readEx_Z_BlockPos(BlockPos var1) {
         return this.mayPlace(var1);
      }

      default boolean putAt_Z() {
         return this.init_Z();
      }

      default boolean fill_Z_BlockPos(BlockPos var1) {
         return this.mayBreak(var1);
      }

      default int putAt_I_ItemStack(ItemStack var1) {
         return this.init(var1);
      }

      default double placeCost() {
         return this.blockPlaceCost();
      }

      default boolean breakIsUseful(BlockPos var1) {
         return this.wantsBroken(var1);
      }

      default boolean cellAllowed(BlockPos var1) {
         return this.allowsCell(var1);
      }

      default Set<AiPathfinder.MoveType> blockedMoves() {
         return this.disallowedMoves();
      }

      default boolean outsideSearchBox(BlockPos var1, int var2, int var3) {
         return false;
      }

      default BlockPos goalTarget() {
         return this.getTargetBlock();
      }

      static AiPathfinder.PathGoal near(BlockPos var0, double var1) {
         return near(var0, var1, false);
      }

      static AiPathfinder.PathGoal near(final BlockPos var0, final double var1, final boolean var3) {
         final double var4 = var1 * var1;
         final double var6 = var1 * Math.hypot(AiPathfinder.MoveCosts.MIN_HORIZONTAL_TICK_RATE, AiPathfinder.MoveCosts.MIN_ASCEND_TICK_RATE);
         return new AiPathfinder.PathGoal() {
            @Override
            public BlockPos goalTarget() {
               return var0;
            }

            @Override
            public BlockPos getTargetBlock() {
               return this.goalTarget();
            }

            @Override
            public boolean isInGoalWithGrid(int x, int y, int z, AiPathfinder.GridView planned) {
               return this.goalReached(x, y, z, planned);
            }

            @Override
            public boolean isInGoal(int var1x, int var2, int var3x) {
               double var4x = var1x - var0.getX();
               double var6x = var2 - var0.getY();
               double var8 = var3x - var0.getZ();
               return var4x * var4x + var6x * var6x + var8 * var8 <= var4;
            }

            @Override
            public boolean goalReached(int var1x, int var2, int var3x, AiPathfinder.GridView var4x) {
               if (this.isInGoal(var1x, var2, var3x)) {
                  return true;
               }

               double var5 = var1x - var0.getX();
               double var7 = var3x - var0.getZ();
               if (var4x == null || var5 * var5 + var7 * var7 > var4) {
                  return false;
               }

               if (var3 && var5 == 0.0 && var7 == 0.0 && var2 == var0.getY() - 1) {
                  AiPathfinder.CellInfo var13 = var4x.probe(var1x, var2 + 1, var3x);
                  AiPathfinder.CellInfo var10 = var4x.probe(var1x, var2 + 2, var3x);
                  AiPathfinder.CellInfo var11 = var4x.probe(var1x, var2, var3x);
                  AiPathfinder.CellInfo var12 = var4x.probe(var1x, var2 - 1, var3x);
                  return var13 != null
                     && var10 != null
                     && var11 != null
                     && var12 != null
                     && var13.passable()
                     && var10.passable()
                     && var13.loaded()
                     && var10.loaded()
                     && !var13.cached()
                     && !var10.cached()
                     && !var11.cached()
                     && !var12.cached()
                     && !var13.water()
                     && !var13.climbable()
                     && AiPathfinder.MoveGenerator.isStandable(var11, var13, var12);
               }

               if (var2 != var0.getY() + 1) {
                  return false;
               }

               AiPathfinder.CellInfo var9 = var4x.probe(var0.getX(), var0.getY(), var0.getZ());
               return var9 != null
                  && var9.loaded()
                  && !var9.cached()
                  && !var9.passable()
                  && var9.top16() > 0
                  && var9.top16() < 16
                  && !var9.door()
                  && !var9.hazardFloor()
                  && !var9.lava();
            }

            @Override
            public double heuristic(int var1x, int var2, int var3x) {
               int var4x = var0.getY() - var2;
               if (var4x < 0) {
                  var4x++;
               } else if (var3 && var4x > 0) {
                  var4x--;
               }

               double var5 = AiPathfinder.MoveCosts.estimateDirectionalHeuristic(var1x - var0.getX(), var4x, var3x - var0.getZ());
               if (var3 && var4x > 0) {
                  double var7 = var4x * AiPathfinder.MoveCosts.MIN_ASCEND_TICK_RATE;
                  var5 = Math.max(var5 - var7, var7);
               }

               return Math.max(0.0, var5 - var6);
            }

            @Override
            public boolean outsideSearchBox(BlockPos var1x, int var2, int var3x) {
               return Math.abs(var0.getX() - var1x.getX()) > var2 + var1
                  || Math.abs(var0.getZ() - var1x.getZ()) > var2 + var1
                  || Math.abs(var0.getY() - var1x.getY()) > var3x + var1;
            }

            @Override
            public String toString() {
               return "near(" + var0.toShortString() + ", r=" + var1 + ")";
            }
         };
      }

      static AiPathfinder.PathGoal reachYLevel(final int var0, final BlockPos var1) {
         return new AiPathfinder.PathGoal() {
            @Override
            public boolean isInGoal(int var1x, int var2, int var3) {
               return var2 == var0;
            }

            @Override
            public double heuristic(int var1x, int var2, int var3) {
               return Math.abs(var2 - var0) * 4.0 + Math.hypot(var1x - var1.getX(), var3 - var1.getZ()) * 0.5;
            }

            @Override
            public String toString() {
               return "yLevel(" + var0 + ")";
            }
         };
      }
   }
}
