package autismclient.ai.engine;

import autismclient.compat263.Compat263;

import autismclient.AutismClientAddon;
import autismclient.ai.AiVisualization;
import autismclient.ai.AiServerEvidence;
import autismclient.ai.AiPlanks;
import autismclient.ai.AiItems;
import autismclient.api.ai.AiOperation;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.AutismLiteVariant;
import autismclient.util.RotationMath;
import autismclient.util.BlockPlacementPlanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AiWrExecutor implements AiPathfinder.SearchTask {
   private static final int MAX_MOVES_PER_TICK = 16;
   private static final int flowTemp = 6;
   private static final int readEx_I = 6;
   private static final int DOOR_CLOSE_TICKS = 24;
   private static final int prevEx_I = 60;
   private static final int prev_I = 3;
   private static final int chunkAt_I = 2;
   private static final int state_I = 60;
   private static final int closeOf_I = 6;
   private static final long putAt_J = 6000000L;
   private static final long fill_J = 8000000L;
   private static final ItemStack[] scratchHotbar = new ItemStack[9];
   private static final boolean navigationDebug = Boolean.getBoolean("autism.navigation.debug");
   private final Minecraft client = Minecraft.getInstance();
   private final AiAim putAt_CacheHelper = new AiAim();
   private final AiBlockDigger putAt_JsonCache = new AiBlockDigger(
      this.client, this::selectSlot, this::putAt_V_Vec3, false, false, this.putAt_CacheHelper::tick, () -> !this.putAt_CacheHelper.isSilent()
   );
   private List<AiPathfinder.PathStep> moves = List.of();
   private int moveIndex;
   private boolean endsAtGoal = true;
   private AiWrExecutor.RunState putAt_RingCache_TableCac;
   private AiWrExecutor.PaceRun putAt_RingCache_TaskStat;
   private AiWrExecutor.PendingRecovery putAt_RingCache_JsonCach;
   private AiPathfinder.PathStep currentMove;
   private AiPathfinder.PathStep replacedStep;
   private int moveTicks;
   private int placeAttempts;
   private int placeConfirmTicks;
   private int stateOf_I;
   private boolean bridgeRetried;
   private boolean supportPlaced;
   private boolean prevEx_Z;
   private boolean pillarJumpStarted;
   private BlockPos pendingPlacePos;
   private long placeRevision;
   private Block putAt_Block;
   private int store_I;
   private int nextBy_I;
   private BlockPos doorToClose;
   private int dataEx_I;
   private int frameOf_I;
   private int syncAt_I;
   private AiBridgeDriver putAt_TokenCodec;
   private int bridgeStartIndex;
   private int bridgeEndIndex;
   private int bridgeRunEnd;
   private int bridgeTickBudget;
   private final Set<BlockPos> pendingBreakCells;
   private AiDigLine.Inner_DataUtil putAt_EventBus_CacheHelp;
   private AiDigLine.Inner_DataUtil fill_EventBus_CacheHelp;
   private AiPathfinder.PathStep readEx_FileStore_PacketHe;
   private AiPathfinder.PathStep value_FileStore_PacketHe;
   private AiPathfinder.PathStep prevEx_FileStore_PacketHe;
   private Set<BlockPos> lastDigCells;
   private Set<BlockPos> readEx_Set;
   private boolean aimingDigLine;
   private BlockPos stalledDigTarget;
   private BlockPos shaftCapPos;
   private BlockPos prevEx_BlockPos;
   private boolean shaftCapped;
   private int shaftCapTicks;
   private int shaftX;
   private int shaftZ;
   private int shaftTopY;
   private int plannedShaftDepth;
   private int swappedSlot;
   private int originalSlot;
   private boolean restockEnabled;
   private AiRestock restock;
   private ItemStack[] fill_ItemStackArr;
   private String lastPlacedItemId;
   private int restockCount;
   private boolean restockFailed;
   private boolean plankCraftFailed;
   private boolean plankCraftDone;
   private AiOperation plankCraftOp;
   private AiChunkMemory chunkMemory;
   private AiPathfinder.FallPolicy fallPolicy;
   private boolean allowHops;
   private boolean stateOf_Z;
   private boolean store_Z;
   private boolean nextBy_Z;
   private double planSpeedOverride;
   private boolean dataEx_Z;
   private static final Predicate<BlockPos> putAt_Predicate = var0 -> true;
   private Predicate<BlockPos> fill_Predicate;
   private AiPathfinder.PathStep prev_FileStore_PacketHe;
   private boolean frameOf_Z;
   private Predicate<BlockPos> readEx_Predicate;
   private Function<BlockPos, Vec3> putAt_Function;
   private Function<Predicate<AABB>, Vec3> fill_Function;
   private Runnable walkingWorkAction;
   private AiLiveWorld putAt_NetEntry;
   private AiLiveGrid putAt_PoolStore;
   private AiChunkMemory fill_PacketHelper;
   private final AiRoutePolicy.LoopDetector putAt_ValueSink_CacheHel;
   private boolean syncAt_Z;
   private BlockPos prev_BlockPos;
   private AiPathfinder.PathStep chunkAt_FileStore_PacketHe;
   private String lastFailureReason;
   private int blocksBroken;
   private int pillarBlocksPlaced;
   private int bridgeBlocksPlaced;
   private double elapsedCostTicks;

   public AiWrExecutor() {
      this.putAt_RingCache_TableCac = AiWrExecutor.RunState.putAt_RingCache_TableCac;
      this.syncAt_I = -1073741824;
      this.pendingBreakCells = new HashSet<>();
      this.lastDigCells = Set.of();
      this.readEx_Set = Set.of();
      this.shaftX = -2147483648;
      this.swappedSlot = -1;
      this.originalSlot = -1;
      this.fallPolicy = AiPathfinder.FallPolicy.STANDARD;
      this.allowHops = true;
      this.stateOf_Z = true;
      this.nextBy_Z = true;
      this.planSpeedOverride = 0.0 / 0.0;
      this.fill_Predicate = putAt_Predicate;
      this.putAt_ValueSink_CacheHel = new AiRoutePolicy.LoopDetector();
      this.lastFailureReason = "";
   }

   public static boolean isPlayerSupported() {
      return AiMotionSafety.isPlayerSupported();
   }

   public static void releaseMovementKeys() {
      AiMotionSafety.releaseMovementKeys();
   }

   public static void applyEmergencyBrake() {
      AiMotionSafety.applyEmergencyBrake();
   }

   public static int putAt_I_List(List<AiPathfinder.PathStep> var0) {
      return AiRoutePolicy.putAt(var0);
   }

   static boolean putAt_Z_FileStore_PacketHe(
      AiPathfinder.PathStep var0, Function<BlockPos, AiPathfinder.CellInfo> var1, Predicate<BlockPos> var2, Predicate<BlockPos> var3
   ) {
      if (var0 != null && var0.cellsToBreak().isEmpty() && var0.cellsToPlace().isEmpty()) {
         AiPathfinder.CellInfo var4 = (AiPathfinder.CellInfo)var1.apply(var0.from());
         AiPathfinder.CellInfo var5 = (AiPathfinder.CellInfo)var1.apply(var0.to());
         if (!var4.water() && !var5.water()) {
            boolean var10000;
            switch (var0.type()) {
               case WALK:
               case WALK_DIAGONAL:
               case SPRINT:
               case SPRINT_DIAGONAL:
               case SPRINT_JUMP:
               case STEP_UP_1:
               case JUMP_UP_1:
               case JUMP:
               case JUMP_GAP_2:
               case JUMP_GAP_3:
               case PARKOUR_ASCEND:
               case SLIME_BOUNCE:
               case LADDER_JUMP:
               case CLIMB_1:
               case DESCEND_1:
               case DESCEND_2:
               case DESCEND_3:
               case DESCEND_SOFT:
                  var10000 = true;
                  break;
               case DOOR:
                  var10000 = !var2.test(var0.to()) && !var3.test(var0.to());
                  break;
               default:
                  var10000 = false;
            }

            return var10000;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static int putAt_I_List(
      List<AiPathfinder.PathStep> var0, int var1, Function<BlockPos, AiPathfinder.CellInfo> var2, Predicate<BlockPos> var3, Predicate<BlockPos> var4
   ) {
      int var5 = var1;

      while (var5 < var0.size() && putAt_Z_FileStore_PacketHe((AiPathfinder.PathStep)var0.get(var5), var2, var3, var4)) {
         var5++;
      }

      return var5;
   }

   static boolean putAt_Z_List_Function(List<AiPathfinder.PathStep> var0, Function<BlockPos, AiPathfinder.CellInfo> var1) {
      for (AiPathfinder.PathStep var3 : var0) {
         if (((AiPathfinder.CellInfo)var1.apply(var3.from().below())).farmland() || ((AiPathfinder.CellInfo)var1.apply(var3.to().below())).farmland()) {
            return true;
         }
      }

      return false;
   }

   static boolean needsGoalBrake(boolean var0, boolean var1, boolean var2, boolean var3, double var4, double var6) {
      return var0 && var1 && var2 && !var3 && var4 * var4 + var6 * var6 > 4.0E-4;
   }

   public void putAt_V_List(List<AiPathfinder.PathStep> var1) {
      this.putAt_V_List_Z(var1, true);
   }

   public void putAt_V_List_Z(List<AiPathfinder.PathStep> var1, boolean var2) {
      this.replacedStep = this.putAt_RingCache_TableCac == AiWrExecutor.RunState.fill
         ? (
            this.currentMove != null
               ? this.currentMove
               : (this.moveIndex < this.moves.size() ? this.moves.get(this.moveIndex) : null)
         )
         : null;
      this.clearRunState(false);
      this.moves = List.copyOf(var1);
      this.moveIndex = 0;
      this.currentMove = null;
      this.endsAtGoal = var2;
      this.prev_FileStore_PacketHe = null;
      this.putAt_RingCache_TableCac = AiWrExecutor.RunState.fill;
   }

   public boolean putAt_Z_List_Z(List<AiPathfinder.PathStep> var1, boolean var2) {
      if (this.putAt_RingCache_TableCac == AiWrExecutor.RunState.fill && !this.moves.isEmpty() && !var1.isEmpty()) {
         if (!((AiPathfinder.PathStep)var1.get(0)).from().equals(this.moves.get(this.moves.size() - 1).to())) {
            return false;
         }

         int var3 = this.moveIndex;
         ArrayList var4 = new ArrayList(this.moves.size() - var3 + var1.size());
         var4.addAll(this.moves.subList(var3, this.moves.size()));
         var4.addAll(var1);
         this.moves = List.copyOf(var4);
         if (this.putAt_RingCache_TaskStat != null) {
            this.putAt_RingCache_TaskStat.startIndex = Math.max(0, this.putAt_RingCache_TaskStat.startIndex - var3);
            this.putAt_RingCache_TaskStat.endIndex = Math.max(0, this.putAt_RingCache_TaskStat.endIndex - var3);
         }

         this.bridgeStartIndex -= var3;
         this.bridgeEndIndex -= var3;
         this.bridgeRunEnd -= var3;
         this.moveIndex = 0;
         this.endsAtGoal = var2;
         return true;
      } else {
         return false;
      }
   }

   boolean putAt_Z_List(List<AiPathfinder.PathStep> var1) {
      if (this.putAt_RingCache_TableCac != AiWrExecutor.RunState.fill) {
         return false;
      }

      int var2 = this.moveIndex;
      var var3 = AiPathfinder.spliceDetour(this.moves.subList(var2, this.moves.size()), var1);
      if (var3 == null) {
         return false;
      }

      this.moves = var3;
      if (this.putAt_RingCache_TaskStat != null) {
         this.putAt_RingCache_TaskStat.startIndex = Math.max(0, this.putAt_RingCache_TaskStat.startIndex - var2);
         this.putAt_RingCache_TaskStat.endIndex = Math.min(this.moves.size(), Math.max(0, this.putAt_RingCache_TaskStat.endIndex - var2));
         boolean var4 = false;
         if (this.putAt_RingCache_TaskStat.putAt_TextPool_JsonCache != null) {
            BlockPos[] var5 = this.putAt_RingCache_TaskStat.putAt_TextPool_JsonCache.cells();

            for (int var6 = 0; var6 < var5.length && this.putAt_RingCache_TaskStat.startIndex + var6 < this.moves.size(); var6++) {
               if (!var5[var6].equals(this.moves.get(this.putAt_RingCache_TaskStat.startIndex + var6).to())) {
                  var4 = true;
                  break;
               }
            }
         }

         if (var4) {
            this.putAt_RingCache_TaskStat.replanOnLanding = true;
         }
      }

      this.bridgeStartIndex -= var2;
      this.bridgeEndIndex -= var2;
      this.bridgeRunEnd -= var2;
      this.moveIndex = 0;
      this.endsAtGoal = true;
      return true;
   }

   public AiWrExecutor.RunState putAt_RingCache_TableCac() {
      return this.putAt_RingCache_TableCac;
   }

   public BlockPos fill_BlockPos() {
      return this.prev_BlockPos;
   }

   public AiPathfinder.PathStep putAt_FileStore_PacketHe() {
      return this.chunkAt_FileStore_PacketHe;
   }

   public boolean readEx_Z() {
      return this.syncAt_Z;
   }

   public String lastFailureReason() {
      return this.lastFailureReason;
   }

   public int readEx_I() {
      return this.blocksBroken;
   }

   public int value_I() {
      return this.pillarBlocksPlaced;
   }

   public int prevEx_I() {
      return this.bridgeBlocksPlaced;
   }

   public int prev_I() {
      return this.restockCount;
   }

   public double elapsedCostTicks() {
      return this.elapsedCostTicks;
   }

   public void putAt_V_Z(boolean var1) {
      this.restockEnabled = var1;
      if (var1 && this.restock == null) {
         this.restock = new AiRestock(this.client);
      }
   }

   public void putAt_V_PacketHelper(AiChunkMemory var1) {
      this.chunkMemory = var1;
   }

   void putAt_V_FileStore_RingUtil(AiPathfinder.FallPolicy var1) {
      this.fallPolicy = var1;
   }

   void fill(boolean var1) {
      this.store_Z = var1;
   }

   void readEx(boolean var1) {
      this.nextBy_Z = var1;
   }

   void putAt_V_D(double var1) {
      this.planSpeedOverride = var1;
   }

   void putAt_V_FileStore_PacketHe(AiPathfinder.PathStep var1) {
      this.prev_FileStore_PacketHe = var1;
   }

   public void value(boolean var1) {
      this.allowHops = var1;
   }

   public void prevEx_V_Z(boolean var1) {
      this.stateOf_Z = var1;
   }

   public void prev(boolean var1) {
      this.dataEx_Z = var1;
   }

   public void putAt_V_Predicate(Predicate<BlockPos> var1) {
      this.fill_Predicate = var1 != null ? var1 : putAt_Predicate;
   }

   public void setAcceptedTool(BiPredicate<BlockState, ItemStack> var1) {
      this.putAt_JsonCache.withAcceptedTool(var1);
   }

   public void putAt_V_Function(Function<BlockPos, Vec3> var1) {
      this.putAt_Function = var1;
   }

   public void putAt_V_I(int var1) {
      this.plannedShaftDepth = Math.max(0, var1);
   }

   public void putAt_V_Function_Runnable(Function<Predicate<AABB>, Vec3> var1, Runnable var2) {
      if (!AutismLiteVariant.enabled()) {
         this.fill_Function = var1;
         this.walkingWorkAction = var2;
      }
   }

   public void readEx_V() {
      this.putAt_ValueSink_CacheHel.putAt();
   }

   public void value_V() {
      this.putAt_ValueSink_CacheHel.putAt();
   }

   public void prevEx_V() {
      this.syncAt_Z = false;
   }

   public void chunkAt(boolean var1) {
      this.putAt_V_Z_Predicate(var1, null);
   }

   public void putAt_V_Z_Predicate(boolean var1, Predicate<BlockPos> var2) {
      this.frameOf_Z = var1;
      this.readEx_Predicate = var1 ? var2 : null;
   }

   public void prev_V() {
      this.releaseDirectionKeys();
      if (this.client.player != null) {
         this.applySprint(this.client.player, false);
      }

      this.chunkAt_V();
      AiBlockDigger.tickDestroyDelay(this.client);
   }

   void chunkAt_V() {
      if (this.client.player != null
         && this.client.player.onClimbable()
         && !this.client.player.isInWater()
         && AiLadderJump.canClimbAt(this.putAt_NetEntry(), this.client.player.blockPosition())) {
         LocalPlayer var1 = this.client.player;
         AiPhysics.MoveInput var2 = AiLadderJump.putAt_TokenCore_CacheHel_TokenCore_TaskStat(
            this.putAt_NetEntry(), putAt_TokenCore_TableCac_LocalPlayer(var1), var1.blockPosition(), false, var1.getYRot()
         );
         this.putAt_V_LocalPlayer_TokenCore_CacheHel(var1, new AiPhysics.MoveInput(var2.forward(), var2.strafe(), false, false, true, var2.yaw()));
      }
   }

   public boolean value_Z() {
      if (this.client.player != null && this.client.level != null) {
         LocalPlayer var1 = this.client.player;
         if (!var1.isInWater() && (!var1.onGround() || !(var1.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4))) {
            if (AiMotionSafety.isMoveSafe(this.putAt_NetEntry(), putAt_TokenCore_TableCac_LocalPlayer(var1), AiPhysics.MoveInput.NONE, 64)
               )
             {
               return true;
            }

            AiMotionSafety.applyEmergencyBrake();
            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public int chunkAt_I() {
      return this.putAt_RingCache_TableCac == AiWrExecutor.RunState.fill && this.endsAtGoal && this.moves != null
         ? Math.max(0, this.moves.size() - this.moveIndex)
         : -1;
   }

   public int remainingMoveCount() {
      return this.putAt_RingCache_TableCac == AiWrExecutor.RunState.fill && this.moves != null && !this.moves.isEmpty()
         ? Math.max(0, this.moves.size() - this.moveIndex)
         : -1;
   }

   public BlockPos readEx_BlockPos() {
      return this.putAt_RingCache_TableCac == AiWrExecutor.RunState.fill && this.moves != null && !this.moves.isEmpty()
         ? this.moves.get(this.moves.size() - 1).to()
         : null;
   }

   public BlockPos value_BlockPos() {
      return !this.moves.isEmpty() && this.putAt_RingCache_TableCac == AiWrExecutor.RunState.fill
         ? this.moves.get(this.moves.size() - 1).to()
         : null;
   }

   public Set<BlockPos> putAt_Set() {
      if (this.putAt_RingCache_TableCac == AiWrExecutor.RunState.fill && this.moves != null && !this.moves.isEmpty()) {
         HashSet<BlockPos> var1 = new HashSet<>();

         for (int var2 = Math.max(0, this.moveIndex); var2 < this.moves.size(); var2++) {
            AiPathfinder.PathStep var3 = this.moves.get(var2);
            var1.add(var3.from().below());
            var1.add(var3.to().below());
            int var4 = var3.to().getX() - var3.from().getX();
            int var5 = var3.to().getZ() - var3.from().getZ();
            if (var4 != 0 && var5 != 0) {
               var1.add(var3.from().offset(var4, -1, 0));
               var1.add(var3.from().offset(0, -1, var5));
            }
         }

         return var1;
      } else {
         return Set.of();
      }
   }

   public Set<BlockPos> putAt_Set_I(int var1) {
      if (this.putAt_RingCache_TableCac != AiWrExecutor.RunState.fill) {
         return Set.of();
      }

      HashSet<BlockPos> var2 = new HashSet<>();

      for (int var3 = this.moveIndex; var3 < Math.min(this.moves.size(), this.moveIndex + var1); var3++) {
         for (BlockPos var5 : this.moves.get(var3).cellsToBreak()) {
            if (!isPassableNonWeb(this.cellAt(var5))) {
               var2.add(var5);
            }
         }
      }

      return var2;
   }

   int closeOf_I() {
      if (this.chunkAt_I() < 0) {
         return -1;
      }

      double var1 = 0.0;

      for (int var3 = this.moveIndex; var3 < this.moves.size(); var3++) {
         var1 += this.moves.get(var3).costTicks();
      }

      double var5 = this.elapsedCostTicks + var1;
      return var5 > 0.0 ? Math.clamp((int)(100.0 * this.elapsedCostTicks / var5), 0, 99) : 0;
   }

   public boolean prevEx_Z() {
      if (this.client.player != null && !isPlayerSupported()) {
         return false;
      } else if (this.putAt_RingCache_TableCac != AiWrExecutor.RunState.fill) {
         return true;
      } else if (this.putAt_RingCache_TaskStat == null) {
         return this.currentMove == null ? true : this.isInterruptible();
      } else {
         return this.client.player == null
            || this.client.player.onGround()
            || this.client.player.onClimbable()
            || this.client.player.isInWater();
      }
   }

   public boolean prev_Z() {
      return this.pendingPlacePos != null && (this.client.level == null || !this.isPlacementConfirmed(this.pendingPlacePos))
         || this.restock != null && this.restock.isRunning()
         || this.plankCraftOp != null
         || this.nextBy_I > 0;
   }

   public boolean chunkAt_Z() {
      return this.doorToClose != null;
   }

   public boolean state_Z() {
      return this.currentMove != null && !this.currentMove.cellsToPlace().isEmpty() && !this.supportPlaced;
   }

   boolean closeOf_Z() {
      return false;
   }

   public void state_V() {
      if (!AutismLiteVariant.enabled()) {
         AiVisualization.clearOwner(this);
      }

      if (this.putAt_RingCache_TableCac != AiWrExecutor.RunState.prevEx) {
         this.putAt_RingCache_TableCac = AiWrExecutor.RunState.prevEx;
         this.clearRunState(true);
      }
   }

   public boolean pollAt_Z() {
      if (!this.prevEx_Z()) {
         return false;
      }

      if (this.putAt_RingCache_TableCac == AiWrExecutor.RunState.prevEx) {
         return true;
      }

      if (!AutismLiteVariant.enabled()) {
         AiVisualization.clearOwner(this);
      }

      this.clearRunState(false);
      this.putAt_CacheHelper.clear();
      this.moves = List.of();
      this.moveIndex = 0;
      this.currentMove = null;
      this.prev_BlockPos = null;
      this.chunkAt_FileStore_PacketHe = null;
      this.putAt_RingCache_TableCac = AiWrExecutor.RunState.putAt_RingCache_TableCac;
      return true;
   }

   public void closeOf_V() {
      if (this.putAt_RingCache_TableCac == AiWrExecutor.RunState.value) {
         this.putAt_RingCache_TableCac = AiWrExecutor.RunState.putAt_RingCache_TableCac;
         this.prev_BlockPos = null;
         this.chunkAt_FileStore_PacketHe = null;
         this.moves = List.of();
         this.currentMove = null;
         this.tick_V();
      }
   }

   @Override
   public boolean isFollowing() {
      return this.putAt_RingCache_TableCac == AiWrExecutor.RunState.fill;
   }

   @Override
   public BlockPos currentPos() {
      return AiStance.standingPos(this.client);
   }

   @Override
   public int pendingPlacementCount() {
      int var1 = 0;

      for (int var2 = this.moveIndex; var2 < this.moves.size(); var2++) {
         if (var2 != this.moveIndex || !this.supportPlaced) {
            var1 += this.moves.get(var2).cellsToPlace().size();
         }
      }

      return var1;
   }

   @Override
   public List<AiPathfinder.PathStep> remainingMoves() {
      return this.putAt_RingCache_TableCac == AiWrExecutor.RunState.fill
         ? List.copyOf(this.moves.subList(this.moveIndex, this.moves.size()))
         : List.of();
   }

   @Override
   public int remainingTicks() {
      double var1 = 0.0;

      for (int var3 = this.moveIndex + (this.currentMove == null && this.putAt_RingCache_TaskStat == null ? 0 : 1);
         var3 < this.moves.size();
         var3++
      ) {
         var1 += this.moves.get(var3).costTicks();
      }

      return (int)Math.ceil(var1);
   }

   public AiWrExecutor.RunState fill_RingCache_TableCac() {
      if (this.putAt_RingCache_TableCac != AiWrExecutor.RunState.fill) {
         return this.putAt_RingCache_TableCac;
      }

      if (this.client.player != null && this.client.level != null) {
         this.fill_ItemStackArr = null;
         if (this.putAt_NetEntry != null) {
            this.putAt_NetEntry.resetCaches();
         }

         this.pollAt_V();
         this.pollOf_V();
         if (this.putAt_RingCache_TableCac == AiWrExecutor.RunState.fill && this.putAt_RingCache_TaskStat == null && this.putAt_TokenCodec == null) {
            this.putAt_CacheHelper.tick();
         }

         return this.putAt_RingCache_TableCac;
      } else {
         this.state_V();
         return this.putAt_RingCache_TableCac;
      }
   }

   private void pollAt_V() {
      if (!AutismLiteVariant.enabled()) {
         if (this.putAt_RingCache_TaskStat != null && this.putAt_RingCache_TaskStat.putAt_TextPool_JsonCache != null) {
            var var1 = List.of(this.putAt_RingCache_TaskStat.putAt_TextPool_JsonCache.checkpoints());
            if (this.putAt_RingCache_TaskStat.putAt_TextPool_IoHelper.size() > 0 && this.putAt_RingCache_TaskStat.putAt_TableUtil != null) {
               AiVisualization.showPaceScript(this, this.putAt_RingCache_TaskStat.putAt_TextPool_IoHelper, this.putAt_RingCache_TaskStat.putAt_TableUtil.stepIndex(), var1, 0);
            } else {
               AiVisualization.showPreview(this, this.putAt_RingCache_TaskStat.putAt_TextPool_JsonCache, var1, 0);
            }
         } else {
            AiVisualization.putAt_V_Obj_3(this, this.moves, this.moveIndex, this::scaffoldPreviewState);
         }
      }
   }

   private void pollOf_V() {
      LocalPlayer var1 = this.client.player;
      int var2 = 0;

      while (this.putAt_RingCache_TableCac == AiWrExecutor.RunState.fill) {
         if (this.moveIndex >= this.moves.size()) {
            if (this.doorToClose != null) {
               this.flushOf_V();
               return;
            }

            if (!AutismLiteVariant.enabled()) {
               AiVisualization.clearOwner(this);
            }

            this.putAt_RingCache_TableCac = AiWrExecutor.RunState.readEx;
            this.clearRunState(false);
            return;
         }

         if (this.putAt_RingCache_JsonCach == null) {
            if (this.putAt_RingCache_TaskStat == null) {
               if (this.currentMove == null
                  && !var1.isInWater()
                  && putAt_Z_FileStore_PacketHe(
                     this.moves.get(this.moveIndex), this::cellAt, this::isClosedDoorAt, AiRoutePolicy::isKnownClosedDoor
                  )) {
                  int var3 = putAt_I_List(this.moves, this.moveIndex, this::cellAt, this::isClosedDoorAt, AiRoutePolicy::isKnownClosedDoor);
                  if (var3 > this.moveIndex) {
                     if (this.frameOf_Z && var1.onGround() && !this.isAllowedPlainStep(this.moves.get(this.moveIndex))) {
                        this.releaseDirectionKeys();
                        return;
                     }

                     int var9 = AiRoutePolicy.putAt_I_List(this.moves, this.moveIndex, 5, this.putAt_Function(), this.dataEx_Z);
                     if (var9 < 0 && !this.isStepFeasible(this.moves.get(this.moveIndex))) {
                        var9 = this.moveIndex;
                     }

                     if (var9 < 0) {
                        var9 = AiRoutePolicy.putAt_I_List_2(this.moves, this.moveIndex, 5, this.hasSprintFood(var1), false);
                     }

                     if (var9 >= 0) {
                        this.lastFailureReason = "a move ahead turned impossible";
                        this.putAt_V_BlockPos_FileStore_PacketHe(this.moves.get(var9).to(), this.moves.get(var9));
                        return;
                     }

                     AiPathfinder.PathStep var10 = this.moves.get(this.moveIndex);
                     if (!putAt_Z_FileStore_PacketHe_FileStore_PacketHe(var10, this.replacedStep)
                        && this.putAt_ValueSink_CacheHel.putAt(var10, System.nanoTime())) {
                        this.syncAt_Z = true;
                        this.lastFailureReason = "looping on " + var10.type() + " " + var10.from().toShortString() + " -> " + var10.to().toShortString();
                        this.putAt_V_BlockPos_FileStore_PacketHe(var10.to(), var10);
                        return;
                     }

                     this.replacedStep = null;
                     BlockPos var11 = this.moves.get(var3 - 1).to();
                     boolean var7 = this.endsAtGoal && var3 == this.moves.size();
                     if (var3 - this.moveIndex == 1
                        && AiPaceScript.putAt_Z_TokenCore_TaskStat_4(this.putAt_NetEntry(), putAt_TokenCore_TableCac_LocalPlayer(var1), var11, false, var7)) {
                        this.elapsedCostTicks = this.elapsedCostTicks + this.moves.get(this.moveIndex).costTicks();
                        this.moveIndex = var3;
                        if (++var2 >= 16) {
                           return;
                        }
                     } else {
                        this.putAt_V_LocalPlayer(var1, this.moveIndex, var3, false, 0);
                        if (this.tickPaceRun(var1)) {
                           return;
                        }

                        if (++var2 >= 16) {
                           return;
                        }
                     }
                     continue;
                  }
               }

               if (this.currentMove == null) {
                  this.currentMove = this.moves.get(this.moveIndex);
                  this.beginMove(this.currentMove);
                  if (this.putAt_RingCache_TableCac != AiWrExecutor.RunState.fill) {
                     return;
                  }
               }

               if (this.currentMove.type() != AiPathfinder.MoveType.BRIDGE_CELL || this.putAt_TokenCodec == null && !this.isSingleBridgeCell()) {
                  if (!this.isMoveComplete(this.currentMove)) {
                     int var8 = AiRoutePolicy.putAt_I_List(this.moves, this.moveIndex, 5, this.putAt_Function(), this.dataEx_Z);
                     if (var8 < 0 && this.isInterruptible() && !this.isStepFeasible(this.currentMove)) {
                        var8 = this.moveIndex;
                     }

                     if (var8 >= 0) {
                        this.lastFailureReason = "a move ahead turned impossible";
                        this.putAt_V_BlockPos_FileStore_PacketHe(this.moves.get(var8).to(), this.moves.get(var8));
                        return;
                     }

                     if (this.plankCraftOp != null) {
                        this.releaseDirectionKeys();
                        this.stateOf_V();
                        return;
                     }

                     if (this.restock != null && this.restock.isRunning()) {
                        this.releaseDirectionKeys();
                        this.store_V();
                        return;
                     }

                     if (this.frameOf_Z && var1.onGround() && !this.isAllowedPlainStep(this.currentMove)) {
                        this.putAt_CacheHelper.setProfile(AiAim.AimProfile.FAST);
                        if (!this.currentMove.cellsToBreak().isEmpty() && this.putAt_Z_LocalPlayer(var1, this.currentMove, false)) {
                           return;
                        }

                        this.releaseDirectionKeys();
                        return;
                     }

                     boolean var4 = this.currentMove.type() == AiPathfinder.MoveType.DESCEND_SOFT
                        && !var1.onGround()
                        && !var1.isInWater()
                        && !var1.onClimbable();
                     boolean var5 = this.currentMove.type() == AiPathfinder.MoveType.BRIDGE_CELL
                        && !this.supportPlaced
                        && (this.prevEx_Z || this.pendingPlacePos != null)
                        && this.moveTicks < AiPathfinder.MoveCosts.moveTimeoutTicks(this.currentMove.costTicks()) + 60;
                     boolean var6 = !var1.isInWater() || ++this.stateOf_I % 3 == 0;
                     if (var5) {
                        this.moveTicks++;
                     } else if (!var4 && var6 && ++this.moveTicks > AiPathfinder.MoveCosts.moveTimeoutTicks(this.currentMove.costTicks())) {
                        this.putAt_V_Str("over its time budget" + this.putAt_Str_FileStore_PacketHe(this.currentMove));
                        return;
                     }

                     this.putAt_V_LocalPlayer_FileStore_PacketHe(var1, this.currentMove);
                     AiFallGuard.setMoveContext(
                        this.currentMove.type(),
                        this.putAt_TokenCodec == null
                           || this.putAt_TokenCodec.getBridger().putAt_TextStore_BitCore() == AiBridger.Inner_ConfigStore.TELLY,
                        false
                     );
                     this.flushOf_V();
                     return;
                  }

                  if (this.currentMove.type() == AiPathfinder.MoveType.DOOR
                     && !this.putAt_Z_FileStore_PacketHe(this.currentMove)) {
                     if (++this.moveTicks > AiPathfinder.MoveCosts.moveTimeoutTicks(this.currentMove.costTicks())) {
                        this.putAt_V_Str("over its time budget" + this.putAt_Str_FileStore_PacketHe(this.currentMove));
                     }

                     return;
                  }

                  this.elapsedCostTicks = this.elapsedCostTicks + this.currentMove.costTicks();
                  this.moveIndex++;
                  this.currentMove = null;
                  this.stopWalking();
                  if (++var2 >= 16) {
                     return;
                  }
               } else {
                  if (this.prevEx_Z_LocalPlayer(var1)) {
                     return;
                  }

                  this.stopWalking();
                  if (++var2 >= 16) {
                     return;
                  }
               }
            } else {
               if (this.tickPaceRun(var1)) {
                  return;
               }

               if (++var2 >= 16) {
                  return;
               }
            }
         } else {
            if (this.putAt_Z_LocalPlayer(var1)) {
               return;
            }

            if (++var2 >= 16) {
               return;
            }
         }
      }
   }

   private void putAt_V_LocalPlayer(LocalPlayer var1, int var2, int var3, boolean var4, int var5) {
      this.startPaceRun(var1, var2, var3, var4, var5, 0, 0);
   }

   private void startPaceRun(LocalPlayer var1, int var2, int var3, boolean var4, int var5, int var6, int var7) {
      this.tick_V();
      var var8 = List.copyOf(this.moves.subList(var2, var3));
      boolean var9 = this.allowHops && !putAt_Z_List_Function(var8, this::cellAt);
      boolean var10 = this.stateOf_Z && this.hasSprintFood(var1) && !var1.hasEffect(MobEffects.BLINDNESS);
      double var11 = AiPacePlayback.putAt_D_LocalPlayer(var1);
      boolean var13 = Double.isFinite(this.planSpeedOverride) && this.planSpeedOverride > 0.0 && Math.abs(var11 - this.planSpeedOverride) > 1.0E-4;
      double var14 = var13 ? this.planSpeedOverride : var11;
      double var16 = var13 ? var11 : 0.0 / 0.0;
      AiPhysics.MotionState var18 = putAt_TokenCore_TableCac_LocalPlayer(var1);
      float var19 = var1.getYRot();
      AiLiveGrid var20 = this.putAt_PoolStore();
      boolean var21 = this.endsAtGoal && var3 == this.moves.size();
      AiPaceScript.Inner_JsonCache var22 = AiPaceScript.putAt_TextPool_JsonCache_FileStore_TextStor_2(var20, var18, var8, var14, var9, var10, var21);
      if (var22 == null) {
         this.putAt_V_Str("wr guide construction returned null");
      } else {
         AiPaceScript.Inner_TextState var23 = var4 ? AiPaceScript.Inner_TextState.DEFAULT : AiPaceScript.Inner_TextState.TRAVEL;
         Predicate var24 = this.putAt_Predicate_TokenCore_TableCac_Predicate(var18, this.fill_Predicate == putAt_Predicate ? null : this.fill_Predicate);
         AiPaceScript.Inner_TextState var25 = var23.withControls(var9, var10).withAllowedCells(var24);
         AiWrExecutor.PaceRun var26 = new AiWrExecutor.PaceRun(
            var2, var3, var14, var16, var18, var22, var24 != null ? var24 : this.fill_Predicate, var4, var5, var6, var7
         );
         this.putAt_RingCache_TaskStat = var26;
         AiPaceScript.Inner_IoPool var27 = AiPaceScript.Inner_IoPool.of(var18, var19);
         if (!var4
            && !var13
            && var22.length() <= 35.0
            && AiPaceScript.putAt_Z_TokenCore_TaskStat_2(this.putAt_NetEntry(), var27, var22, var25, var26.putAt_TextPool_IoHelper)) {
            var26.searchDone = true;
         } else {
            var var28 = AiPaceScript.putAt_Supplier_FileStore_TextStor(var20, var14, this.fallPolicy);
            Thread.ofVirtual().name("autism-wr-travel").start(() -> {
               try {
                  AiPaceScript.putAt(var28, var27, var22, var25, () -> var26.cancelled, var26.putAt_TextPool_IoHelper);
               } catch (Throwable var9x) {
                  if (navigationDebug) {
                     AiNavDebug.log("wr travel search failed: " + var9x);
                  }

                  var26.putAt_TextPool_IoHelper.finish(false, null);
               } finally {
                  var26.searchDone = true;
               }
            });
            boolean var29 = !var18.onGround() || Math.hypot(var18.vx(), var18.vz()) > 0.005;
            var26.awaitFirstTicks(var29 ? 8000000L : 6000000L);
         }
      }
   }

   private Predicate<BlockPos> putAt_Predicate_TokenCore_TableCac_Predicate(AiPhysics.MotionState var1, Predicate<BlockPos> var2) {
      if (var2 != null && var1.onGround()) {
         BlockPos var3 = BlockPos.containing(var1.x(), var1.y() + 1.0E-4, var1.z());
         BlockPos var4 = AiStance.standingPos(this.client);
         if (var3.equals(var4)) {
            return var2;
         }

         int var5 = var3.getX();
         int var6 = var3.getY();
         int var7 = var3.getZ();
         int var8 = var4.getX();
         int var9 = var4.getZ();
         return var7x -> var2.test(var7x)
            || var7x.equals(var3)
            || var7x.getY() == var6 && (var7x.getX() == var5 && var7x.getZ() == var9 || var7x.getX() == var8 && var7x.getZ() == var7);
      } else {
         return var2;
      }
   }

   private void tick_V() {
      this.putAt_RingCache_JsonCach = null;
      if (this.putAt_RingCache_TaskStat != null) {
         this.putAt_RingCache_TaskStat.cancelled = true;
         this.putAt_RingCache_TaskStat = null;
      }
   }

   private boolean putAt_Z_LocalPlayer(LocalPlayer var1) {
      AiWrExecutor.PendingRecovery var2 = this.putAt_RingCache_JsonCach;
      if (var2 == null) {
         return false;
      }

      if (var1.isInWater()) {
         this.putAt_RingCache_JsonCach = null;
         return false;
      }

      if (!var1.onGround() && !var1.onClimbable()) {
         if (var2.airborneState != null) {
            AiPacePlayback.putAt_V_LocalPlayer_TokenCore_TableCac(var1, var2.airborneState);
         } else {
            AiPacePlayback.putAt_V_LocalPlayer(var1);
         }

         var2.settleTicks = 0;
         return true;
      } else {
         if (var1.onClimbable()) {
            this.chunkAt_V();
         } else {
            if (var1.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
               AiPacePlayback.putAt_V_LocalPlayer(var1);
               var2.settleTicks = 0;
               return true;
            }

            if (var2.settleTicks < 2) {
               AiPacePlayback.fill_V_LocalPlayer(var1);
               var2.settleTicks++;
               return true;
            }
         }

         this.putAt_RingCache_JsonCach = null;
         int var3 = putAt_I_List_2(this.moves, var1.position(), var2.startIndex, this.moveIndex, Math.min(var2.endIndex, this.moves.size()));
         if (var3 > this.moveIndex) {
            for (int var4 = this.moveIndex; var4 < var3; var4++) {
               this.elapsedCostTicks = this.elapsedCostTicks + this.moves.get(var4).costTicks();
            }
         }

         this.moveIndex = var3;
         int var5 = putAt_I_List(this.moves, this.moveIndex, this::cellAt, this::isClosedDoorAt, AiRoutePolicy::isKnownClosedDoor);
         if (var5 <= this.moveIndex) {
            return false;
         }

         this.startPaceRun(var1, this.moveIndex, var5, var2.fineSearch, var2.recoverCount, 0, 0);
         return this.putAt_RingCache_TaskStat != null && this.tickPaceRun(var1);
      }
   }

   private boolean tickPaceRun(LocalPlayer var1) {
      AiWrExecutor.PaceRun var2 = this.putAt_RingCache_TaskStat;
      if (var2 == null) {
         return false;
      }

      if (this.frameOf_Z && var1.onGround() && this.moveIndex < this.moves.size() && !this.isAllowedPlainStep(this.moves.get(this.moveIndex))) {
         this.releaseDirectionKeys();
         return true;
      }

      if (var2.braking) {
         Vec3 var14 = var1.getDeltaMovement();
         if (needsGoalBrake(this.endsAtGoal && this.nextBy_Z, var2.endIndex >= this.moves.size(), var1.onGround(), var1.isInWater(), var14.x, var14.z)
            && ++var2.brakeTicks <= 6) {
            AiPhysics.MoveInput var17 = AiPaceScript.putAt(var1.getYRot(), putAt_TokenCore_TableCac_LocalPlayer(var1));
            this.putAt_V_LocalPlayer_TokenCore_CacheHel(var1, new AiPhysics.MoveInput(var17.forward(), var17.strafe(), false, false, true, var17.yaw()));
            return true;
         } else {
            this.putAt_V_RingCache_TaskStat(var2);
            return false;
         }
      } else if (!var2.replanOnLanding || !var1.onGround() && !var1.onClimbable()) {
         if (var1.onGround() || var1.onClimbable()) {
            int var10 = AiRoutePolicy.putAt_I_List(this.moves, this.moveIndex, 5, this.putAt_Function(), this.dataEx_Z);
            if (var10 < 0) {
               var10 = AiRoutePolicy.putAt_I_List_2(this.moves, this.moveIndex, 5, this.hasSprintFood(var1), false);
            }

            if (var10 >= 0) {
               this.tick_V();
               this.lastFailureReason = "a move ahead turned impossible";
               this.putAt_V_BlockPos_FileStore_PacketHe(this.moves.get(var10).to(), this.moves.get(var10));
               return true;
            }
         }

         if (var2.putAt_TextPool_IoHelper.size() == 0) {
            if (var2.putAt_TextPool_IoHelper.isFinished()) {
               return this.putAt_Z_LocalPlayer_2(var1, var2, "wr search found no way on");
            }

            if (++var2.starvedTicks > 60) {
               return this.putAt_Z_LocalPlayer_2(var1, var2, "wr startup timed out");
            }

            if (var1.onClimbable() && !var1.isInWater()) {
               this.chunkAt_V();
            } else if (var1.onGround() && var1.getDeltaMovement().horizontalDistanceSqr() < 1.0E-6) {
               AiPacePlayback.fill_V_LocalPlayer(var1);
            } else {
               AiPacePlayback.putAt_V_LocalPlayer(var1);
            }

            if (var2.driftRestarts < 2 && var1.onGround() && var1.getDeltaMovement().horizontalDistanceSqr() < 1.0E-6) {
               AiPhysics.MotionState var13 = putAt_TokenCore_TableCac_LocalPlayer(var1);
               double var16 = Math.hypot(var13.x() - var2.startState.x(), var13.z() - var2.startState.z());
               if (var13.pose() == 0 && (var16 > 0.025 || var13.pose() != var2.startState.pose())) {
                  this.startPaceRun(var1, var2.startIndex, var2.endIndex, var2.fineSearch, var2.recoverCount, var2.starvedTicks, var2.driftRestarts + 1);
               }
            }

            return true;
         } else {
            if (var2.putAt_TableUtil.stepIndex() == 0) {
               AiPhysics.MotionState var11 = putAt_TokenCore_TableCac_LocalPlayer(var1);
               double var4 = Math.hypot(var11.x() - var2.startState.x(), var11.z() - var2.startState.z());
               if (var4 > 0.025 || var11.pose() != var2.startState.pose()) {
                  if (var2.driftRestarts < 2 && var1.onGround() && var1.getDeltaMovement().horizontalDistanceSqr() < 1.0E-6 && var11.pose() == 0) {
                     this.startPaceRun(var1, var2.startIndex, var2.endIndex, var2.fineSearch, var2.recoverCount, var2.starvedTicks, var2.driftRestarts + 1);
                     return true;
                  } else {
                     return this.putAt_Z_LocalPlayer_2(var1, var2, "wr start drifted before tick 0");
                  }
               }
            }

            BlockPos var12 = this.putAt_BlockPos_I(var2.endIndex);
            if (var12 != null && var1.position().distanceToSqr(Vec3.atCenterOf(var12)) <= 12.25) {
               this.tryOpenDoor(var12);
            }

            Vec3 var15 = null;
            Runnable var5 = null;
            if (!AutismLiteVariant.enabled() && this.fill_Function != null && var1.onGround()) {
               var15 = this.fill_Function
                  .apply(var2x -> AiRoutePolicy.putAt_Z_AABB(var2x, this.moves, this.moveIndex, var1.getBoundingBox(), var1.getDeltaMovement()));
               if (var15 != null) {
                  var5 = this.walkingWorkAction;
               }
            }

            Vec3 var6 = null;
            if (this.putAt_Function != null && this.endsAtGoal && var2.endIndex >= this.moves.size()) {
               var6 = this.putAt_Function.apply(this.moves.get(this.moves.size() - 1).to());
            }

            AiPacePlayback.Inner_JsonCache var7 = var2.putAt_TableUtil.tickPlayback(var1, var15, var5, var6, var2.putAt_Predicate);
            this.putAt_V_LocalPlayer_RingCache_TaskStat(var1, var2);
            switch (var7) {
               case putAt_TableUtil_JsonCach:
                  var2.starvedTicks = 0;
                  this.flushOf_V();
                  return true;
               case readEx:
                  if (++var2.starvedTicks > 60) {
                     return this.putAt_Z_LocalPlayer_2(var1, var2, "wr search starved mid-stretch");
                  } else {
                     if (!var1.onGround() && !var1.onClimbable() && !var1.isInWater()) {
                        AiPhysics.MotionState var20 = var2.putAt_TableUtil.expectedState();
                        if (var20 != null) {
                           AiPacePlayback.putAt_V_LocalPlayer_TokenCore_TableCac(var1, var20);
                           return true;
                        }
                     }

                     if (var2.putAt_TableUtil.stepIndex() <= 0 || var2.starvedTicks < 2 || !var1.onGround() && !var1.onClimbable()) {
                        if (var1.onClimbable() && !var1.isInWater()) {
                           this.chunkAt_V();
                        } else {
                           AiPacePlayback.putAt_V_LocalPlayer(var1);
                        }

                        return true;
                     }

                     return this.putAt_Z_LocalPlayer_2(var1, var2, "wr search starved mid-stretch");
                  }
               case fill:
                  if (!var2.putAt_TextPool_IoHelper.finishedOk()) {
                     return this.putAt_Z_LocalPlayer_2(var1, var2, "wr partial script ended short of goal");
                  } else {
                     Vec3 var19 = var1.getDeltaMovement();
                     if (needsGoalBrake(this.endsAtGoal && this.nextBy_Z, var2.endIndex >= this.moves.size(), var1.onGround(), var1.isInWater(), var19.x, var19.z)) {
                        var2.braking = true;
                        var2.brakeTicks = 0;
                        AiPhysics.MoveInput var9 = AiPaceScript.putAt(var1.getYRot(), putAt_TokenCore_TableCac_LocalPlayer(var1));
                        this.putAt_V_LocalPlayer_TokenCore_CacheHel(
                           var1, new AiPhysics.MoveInput(var9.forward(), var9.strafe(), false, false, true, var9.yaw())
                        );
                        return true;
                     }

                     this.putAt_V_RingCache_TaskStat(var2);
                     return false;
                  }
               case prevEx:
               case value:
                  if (!var1.onGround() && !var1.onClimbable() && !var1.isInWater()) {
                     AiPhysics.MotionState var8 = var2.putAt_TableUtil.expectedState();
                     if (var8 != null) {
                        AiPacePlayback.putAt_V_LocalPlayer_TokenCore_TableCac(var1, var8);
                        return true;
                     }
                  }

                  String var18 = var2.putAt_TableUtil.putAt_Str() != null ? var2.putAt_TableUtil.putAt_Str() : var7.name();
                  return this.putAt_Z_LocalPlayer_2(var1, var2, "wr " + var7.name().toLowerCase(Locale.ROOT) + ": " + var18);
               default:
                  return true;
            }
         }
      } else {
         int var3 = putAt_I_List(this.moves, this.moveIndex, this::cellAt, this::isClosedDoorAt, AiRoutePolicy::isKnownClosedDoor);
         this.tick_V();
         if (var3 > this.moveIndex) {
            this.putAt_V_LocalPlayer(var1, this.moveIndex, var3, false, 0);
         }

         return this.putAt_RingCache_TaskStat != null && this.tickPaceRun(var1);
      }
   }

   private void putAt_V_LocalPlayer_RingCache_TaskStat(LocalPlayer var1, AiWrExecutor.PaceRun var2) {
      if (var2.putAt_TextPool_JsonCache != null) {
         int var3 = Math.max(0, var2.putAt_TableUtil.stepIndex() - 1);
         int var4 = var2.putAt_TextPool_IoHelper.putAt_I_I(var3);
         if (var4 >= 0) {
            int[] var5 = var2.putAt_TextPool_JsonCache.cellPoint();
            int var6 = var2.cellCursor;

            while (var6 < var5.length && var5[var6] <= var4) {
               var6++;
            }

            int var7 = Math.min(var2.endIndex, var2.startIndex + var6);
            if (var7 > this.moveIndex) {
               for (int var8 = this.moveIndex; var8 < var7; var8++) {
                  this.elapsedCostTicks = this.elapsedCostTicks + this.moves.get(var8).costTicks();
               }

               this.moveIndex = var7;
               var2.cellCursor = var6;
            }
         }
      }
   }

   private void putAt_V_RingCache_TaskStat(AiWrExecutor.PaceRun var1) {
      for (int var2 = this.moveIndex; var2 < var1.endIndex && var2 < this.moves.size(); var2++) {
         this.elapsedCostTicks = this.elapsedCostTicks + this.moves.get(var2).costTicks();
      }

      this.moveIndex = Math.min(var1.endIndex, this.moves.size());
      this.tick_V();
      if (this.moveIndex >= this.moves.size() && this.endsAtGoal) {
         AiPacePlayback.fill_V_LocalPlayer(this.client.player);
      }
   }

   private boolean putAt_Z_LocalPlayer_2(LocalPlayer var1, AiWrExecutor.PaceRun var2, String var3) {
      int var4 = AiRoutePolicy.putAt_I_List(this.moves, this.moveIndex, 5, this.putAt_Function(), this.dataEx_Z);
      if (var4 < 0 && this.moveIndex < this.moves.size() && !this.isStepFeasible(this.moves.get(this.moveIndex))) {
         var4 = this.moveIndex;
      }

      if (var4 >= 0) {
         this.tick_V();
         this.lastFailureReason = var3 + " (path invalidated)";
         this.putAt_V_BlockPos_FileStore_PacketHe(this.moves.get(var4).to(), this.moves.get(var4));
         return true;
      }

      if (var2.endIndex > this.moveIndex && var2.endIndex <= this.moves.size()) {
         BlockPos var5 = this.moves.get(var2.endIndex - 1).to();
         if (AiPaceScript.putAt_Z_TokenCore_TaskStat_4(
            this.putAt_NetEntry(), putAt_TokenCore_TableCac_LocalPlayer(var1), var5, false, this.endsAtGoal && var2.endIndex == this.moves.size()
         )) {
            this.putAt_V_RingCache_TaskStat(var2);
            return false;
         }
      }

      boolean var14 = !var2.fineSearch && var2.putAt_TextPool_IoHelper.isFinished() && !var2.putAt_TextPool_IoHelper.finishedOk();
      int var6 = var2.recoverCount + 1;
      if ((var14 || var6 <= 3) && this.moveIndex < var2.endIndex && var2.endIndex <= this.moves.size()) {
         if (navigationDebug) {
            AiNavDebug.log("wr recover #" + var6 + " (fine=" + (var14 || var2.fineSearch) + "): " + var3);
         }

         int var7 = var2.startIndex;
         int var8 = var2.endIndex;
         boolean var9 = var14 || var2.fineSearch;
         AiPhysics.MotionState var10 = !var1.onGround() && !var1.onClimbable() ? var2.putAt_TableUtil.expectedState() : null;
         this.tick_V();
         boolean var11 = var1.onGround() && var1.getDeltaMovement().horizontalDistanceSqr() <= 1.0E-6 && putAt_TokenCore_TableCac_LocalPlayer(var1).pose() == 0;
         if (!var11) {
            this.putAt_RingCache_JsonCach = new AiWrExecutor.PendingRecovery(var7, var8, var9, var6, var10);
            return this.putAt_Z_LocalPlayer(var1);
         }

         int var12 = putAt_I_List_2(this.moves, var1.position(), var7, this.moveIndex, Math.min(var8, this.moves.size()));
         if (var12 > this.moveIndex) {
            for (int var13 = this.moveIndex; var13 < var12; var13++) {
               this.elapsedCostTicks = this.elapsedCostTicks + this.moves.get(var13).costTicks();
            }
         }

         this.moveIndex = var12;
         int var16 = putAt_I_List(this.moves, this.moveIndex, this::cellAt, this::isClosedDoorAt, AiRoutePolicy::isKnownClosedDoor);
         if (var16 > this.moveIndex) {
            this.startPaceRun(var1, this.moveIndex, var16, var9, var6, 0, 0);
            return true;
         }
      }

      this.tick_V();
      AiPathfinder.PathStep var15 = this.moveIndex < this.moves.size()
         ? this.moves.get(this.moveIndex)
         : (this.moves.isEmpty() ? null : this.moves.get(this.moves.size() - 1));
      this.lastFailureReason = var3;
      this.putAt_V_BlockPos_FileStore_PacketHe(var15 == null ? this.currentPos() : var15.to(), var15);
      return true;
   }

   static int putAt_I_List_2(List<AiPathfinder.PathStep> var0, Vec3 var1, int var2, int var3, int var4) {
      int var5 = Math.max(Math.max(0, var2), var3 - 6);
      int var6 = Math.min(var0.size(), Math.min(var4, var3 + 8));
      int var7 = Math.min(var3, Math.max(0, var6 - 1));
      double var8 = 1.7976931348623157E308;

      for (int var10 = var5; var10 < var6; var10++) {
         BlockPos var11 = ((AiPathfinder.PathStep)var0.get(var10)).from();
         double var12 = var1.distanceToSqr(var11.getX() + 0.5, var11.getY(), var11.getZ() + 0.5);
         if (var12 < var8) {
            var8 = var12;
            var7 = var10;
         }
      }

      return var7;
   }

   private BlockPos putAt_BlockPos_I(int var1) {
      if (var1 < this.moves.size() && this.moves.get(var1).type() == AiPathfinder.MoveType.DOOR) {
         BlockPos var2 = this.moves.get(var1).to();
         if (this.isClosedDoorAt(var2)) {
            return var2;
         }
      }

      return null;
   }

   private void tryOpenDoor(BlockPos var1) {
      LocalPlayer var2 = this.client.player;
      if (var2 != null && this.isClosedDoorAt(var1) && var2.tickCount - this.syncAt_I >= 3) {
         BlockHitResult var3 = AiAim.raycastLookBlock();
         if (var3 != null && (var3.getBlockPos().equals(var1) || var3.getBlockPos().equals(var1.above()))) {
            this.client.gameMode.useItemOn(var2, InteractionHand.MAIN_HAND, var3);
            Compat263.swing(var2, InteractionHand.MAIN_HAND);
            this.syncAt_I = var2.tickCount;
         }
      }
   }

   private void beginMove(AiPathfinder.PathStep var1) {
      this.moveTicks = 0;
      this.placeAttempts = 0;
      this.placeConfirmTicks = 0;
      this.stateOf_I = 0;
      this.bridgeRetried = false;
      this.supportPlaced = false;
      this.prevEx_Z = false;
      this.pillarJumpStarted = false;
      this.pendingPlacePos = null;
      this.putAt_Block = null;
      this.restockFailed = false;
      this.plankCraftFailed = false;
      this.value_FileStore_PacketHe = null;
      this.prevEx_FileStore_PacketHe = null;
      this.nextBy_I = 0;
      if (var1.type() != AiPathfinder.MoveType.DIG_DOWN_1) {
         this.shaftCapped = false;
      }

      this.putAt_CacheHelper.setProfile(
            var1.type() != AiPathfinder.MoveType.PILLAR && var1.type() != AiPathfinder.MoveType.BRIDGE_CELL
               ? AiAim.AimProfile.FAST
               : AiAim.AimProfile.EXACT
         );
      if (!putAt_Z_FileStore_PacketHe_FileStore_PacketHe(var1, this.replacedStep) && this.putAt_ValueSink_CacheHel.putAt(var1, System.nanoTime())) {
         this.syncAt_Z = true;
         this.lastFailureReason = "looping on " + var1.type() + " " + var1.from().toShortString() + " -> " + var1.to().toShortString();
         this.putAt_V_BlockPos_FileStore_PacketHe(var1.to(), var1);
      } else {
         this.replacedStep = null;
         if (AiRoutePolicy.putAt_Z_FileStore_PacketHe_Function(var1, this.putAt_Function())) {
            this.putAt_V_Str("dig cost jumped since planning");
         }
      }
   }

   private static boolean putAt_Z_FileStore_PacketHe_FileStore_PacketHe(AiPathfinder.PathStep var0, AiPathfinder.PathStep var1) {
      return var0 != null && var1 != null && var0.type() == var1.type() && var0.from().equals(var1.from()) && var0.to().equals(var1.to());
   }

   private void putAt_V_LocalPlayer_FileStore_PacketHe(LocalPlayer var1, AiPathfinder.PathStep var2) {
      switch (var2.type()) {
         case CLIMB_1:
            this.runClimb(var1, var2);
            break;
         case DESCEND_1:
         case DESCEND_2:
         case DESCEND_3:
         default:
            if (!var2.cellsToBreak().isEmpty() && this.putAt_Z_LocalPlayer(var1, var2, true)) {
               return;
            }

            boolean var3 = var2.to().getY() > var2.from().getY();
            this.putAt_V_LocalPlayer_2(
               var1, var2, this.stateOf_Z && AiRoutePolicy.putAt_Z_FileStore_SyncPool_Z(var2.type(), this.hasSprintFood(var1)) && !var1.isInWater(), var3, null
            );
            break;
         case DESCEND_SOFT:
            if (this.cellAt(var2.to()).water()) {
               this.runSoftDescend(var1, var2);
            } else {
               this.putAt_V_LocalPlayer_2(var1, var2, false, false, null);
            }
            break;
         case DOOR:
            this.runDoor(var1, var2);
            break;
         case DIG_DOWN_1:
            this.runDigDown(var1, var2);
            break;
         case DIG_TUNNEL:
            this.runDigTunnel(var1, var2);
            break;
         case DIG_DESCEND:
            this.runDigDescend(var1, var2);
            break;
         case DIG_ASCEND:
            this.runDigAscend(var1, var2);
            break;
         case PILLAR:
            this.runPillar(var1, var2);
            break;
         case BRIDGE_CELL:
            this.runBridgeCell(var1, var2);
      }
   }

   private void runDigDown(LocalPlayer var1, AiPathfinder.PathStep var2) {
      if (!this.putAt_Z_LocalPlayer_FileStore_PacketHe(var1, var2)) {
         this.releaseDirectionKeys();
         BlockPos var3 = var2.cellsToBreak().get(0);

         for (int var4 = 1; var4 < var2.cellsToBreak().size(); var4++) {
            BlockPos var5 = var2.cellsToBreak().get(var4);
            if (!this.client.level.getBlockState(var5).isAir()) {
               this.putAt_V_Vec3(Vec3.atCenterOf(var5));
               switch (this.putAt_JsonCache.dig(var5)) {
                  case fill:
                     this.blocksBroken++;
                     this.putAt_JsonCache.stop();
                     break;
                  case readEx:
                     this.putAt_V_Str("dig obstruction failed: " + this.putAt_JsonCache.lastFailureReason());
                  case putAt_JsonCache_CacheHel:
               }

               return;
            }
         }

         double var10 = var3.getX() + 0.5;
         double var6 = var3.getZ() + 0.5;
         this.putAt_V_List_List(var2.cellsToBreak(), this.cellAt(var3).passable() ? List.of() : List.of(var3));
         if (this.cellAt(var3).passable()) {
            this.putAt_V_LocalPlayer(var1, var10, var6, 0.1);
            AiBlockDigger.tickDestroyDelay(this.client);
         } else if (!this.startToolRestock(var3)) {
            BlockPos var8 = var2.from();
            BlockState var9 = this.client.level.getBlockState(var8);
            if (!var9.isAir()
               && var9.getFluidState().isEmpty()
               && this.prevEx_FileStore_PacketHe != var2
               && var9.getCollisionShape(this.client.level, var8).isEmpty()
               && !var9.getShape(this.client.level, var8).isEmpty()
               && var9.getDestroySpeed(this.client.level, var8) >= 0.0F) {
               this.putAt_V_Vec3(Vec3.atCenterOf(var8));
               switch (this.putAt_JsonCache.dig(var8)) {
                  case fill:
                     this.putAt_JsonCache.stop();
                     break;
                  case readEx:
                     this.putAt_JsonCache.stop();
                     this.prevEx_FileStore_PacketHe = var2;
                  case putAt_JsonCache_CacheHel:
               }
            } else {
               this.putAt_V_Vec3(Vec3.atCenterOf(var3));
               this.putAt_V_LocalPlayer(var1, var10, var6, 0.2);
               switch (this.putAt_JsonCache.dig(var3)) {
                  case fill:
                     this.blocksBroken++;
                     this.putAt_JsonCache.stop();
                     break;
                  case readEx:
                     this.putAt_V_Str("dig failed: " + this.putAt_JsonCache.lastFailureReason());
                  case putAt_JsonCache_CacheHel:
               }
            }
         }
      }
   }

   private boolean putAt_Z_LocalPlayer_FileStore_PacketHe(LocalPlayer var1, AiPathfinder.PathStep var2) {
      if (var2.from().getX() != this.shaftX || var2.from().getZ() != this.shaftZ || var2.from().getY() > this.shaftTopY) {
         this.shaftX = var2.from().getX();
         this.shaftZ = var2.from().getZ();
         this.shaftTopY = var2.from().getY();
      }

      if (this.shaftCapPos != null) {
         if (!this.cellAt(this.shaftCapPos).passable()) {
            this.prevEx_BlockPos = this.shaftCapPos;
            this.shaftCapped = true;
            AutismClientAddon.LOG.info("[AI] capped the shaft at {}", this.shaftCapPos.toShortString());
            this.shaftCapPos = null;
            return false;
         }

         if (++this.shaftCapTicks > 40) {
            this.shaftCapPos = null;
            return false;
         }

         this.releaseDirectionKeys();
         AiBlockDigger.tickDestroyDelay(this.client);
         if (AiMotionAudit.isEnabled()) {
            AiMotionAudit.note("cap " + this.shaftCapPos.toShortString());
         }

         if (this.fill_RingCache_CacheHel(this.shaftCapPos) == AiWrExecutor.PlaceResult.readEx) {
            this.shaftCapPos = null;
            return false;
         } else {
            return true;
         }
      } else if (var1.onGround() && !var1.isInWater() && this.bestScaffoldSlot() >= 0) {
         BlockPos var3 = var1.blockPosition();
         BlockPos var4 = var3.above(2);
         if (!this.canCapShaft(var3, var4)) {
            return false;
         }

         int var5 = this.shaftDepth(var3);
         boolean var6 = var5 >= 4 && this.hasAggressiveEnemyNearby(var1);
         if (var5 <= 8 && !var6) {
            return false;
         }

         AutismClientAddon.LOG.info("[AI] capping the shaft at {}: {} deep{}", var4.toShortString(), var5, var6 ? ", a hostile after the player" : "");
         this.shaftCapPos = var4;
         this.shaftCapTicks = 0;
         AiEditLedger.read_V(var4);
         return this.putAt_Z_LocalPlayer_FileStore_PacketHe(var1, var2);
      } else {
         return false;
      }
   }

   private int shaftDepth(BlockPos var1) {
      int var2 = this.shaftTopY - var1.getY();
      int var3 = 0;

      for (int var4 = this.moveIndex; var4 < this.moves.size(); var4++) {
         AiPathfinder.PathStep var5 = this.moves.get(var4);
         if (var5.type() != AiPathfinder.MoveType.DIG_DOWN_1 || var5.to().getX() != this.shaftX || var5.to().getZ() != this.shaftZ) {
            break;
         }

         var3++;
      }

      return var2 + Math.max(var3, this.plannedShaftDepth);
   }

   private boolean hasAggressiveEnemyNearby(LocalPlayer var1) {
      for (Mob var3 : this.client.level.getEntitiesOfClass(Mob.class, var1.getBoundingBox().inflate(24.0))) {
         if (var3.isAlive() && var3 instanceof Enemy && var3.isAggressive()) {
            return true;
         }
      }

      return false;
   }

   private boolean canCapShaft(BlockPos var1, BlockPos var2) {
      BlockPos var3 = var1.above();
      if (this.cellAt(var3).passable()
         && this.cellAt(var2).passable()
         && this.cellAt(var2.above()).passable()) {
         for (Direction var5 : Plane.HORIZONTAL) {
            if (this.cellAt(var3.relative(var5)).passable()
               || this.cellAt(var2.relative(var5)).passable()) {
               return false;
            }
         }

         if (this.shaftCapped
            && this.prevEx_BlockPos != null
            && this.prevEx_BlockPos.getY() > var1.getY()
            && Math.max(Math.abs(this.prevEx_BlockPos.getX() - var1.getX()), Math.abs(this.prevEx_BlockPos.getZ() - var1.getZ())) <= 6) {
            return false;
         }

         for (int var6 = this.moveIndex; var6 < this.moves.size(); var6++) {
            BlockPos var7 = this.moves.get(var6).to();
            if (var7.getX() == var2.getX() && var7.getZ() == var2.getZ() && var7.getY() >= var2.getY() - 1) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void runDigTunnel(LocalPlayer var1, AiPathfinder.PathStep var2) {
      if (!this.putAt_Z_LocalPlayer(var1, var2, true)) {
         Vec3 var3 = this.putAt_Vec3_LocalPlayer(var1);
         if (var3 == null && this.putAt_Function != null && this.moveIndex == this.moves.size() - 1) {
            var3 = this.putAt_Function.apply(var2.to());
         }

         AiBlockDigger.tickDestroyDelay(this.client);
         this.putAt_V_LocalPlayer_2(
            var1, var2, false, false, var3 != null ? var3 : new Vec3(var2.to().getX() + 0.5, var1.getEyePosition().y, var2.to().getZ() + 0.5)
         );
      }
   }

   private void runDigDescend(LocalPlayer var1, AiPathfinder.PathStep var2) {
      if (!this.putAt_Z_LocalPlayer(var1, var2, true)) {
         AiBlockDigger.tickDestroyDelay(this.client);
         Vec3 var3 = this.putAt_Vec3_LocalPlayer(var1);
         if (var3 == null) {
            var3 = new Vec3(var2.to().getX() + 0.5, var1.getEyePosition().y, var2.to().getZ() + 0.5);
         }

         if (!var1.onGround() && !var1.isInWater() && !var1.onClimbable()) {
            this.putAt_V_Vec3(var3);
            this.releaseDirectionKeys();
            this.applySprint(var1, false);
         } else {
            this.putAt_V_LocalPlayer_2(var1, var2, false, false, var3);
         }
      }
   }

   private void runDigAscend(LocalPlayer var1, AiPathfinder.PathStep var2) {
      boolean var3 = false;

      for (BlockPos var5 : var2.cellsToBreak()) {
         if (var5.getX() == var2.from().getX()
            && var5.getZ() == var2.from().getZ()
            && !isPassableNonWeb(this.cellAt(var5))) {
            var3 = true;
         }
      }

      if (!this.putAt_Z_LocalPlayer(var1, var2, !var3)) {
         AiBlockDigger.tickDestroyDelay(this.client);
         Vec3 var6 = this.putAt_Vec3_LocalPlayer(var1);
         this.putAt_V_LocalPlayer_2(var1, var2, false, var1.onGround(), var6);
      }
   }

   private boolean putAt_Z_LocalPlayer(LocalPlayer var1, AiPathfinder.PathStep var2, boolean var3) {
      var var4 = var2.cellsToBreak();
      ArrayList var5 = new ArrayList(var4.size());

      for (BlockPos var7 : var4) {
         if (!isPassableNonWeb(this.cellAt(var7))) {
            var5.add(var7);
         }
      }

      this.putAt_V_List_List(var4, var5);
      if (var5.isEmpty()) {
         if (!this.fallingBlockAbove(var4)) {
            return false;
         }

         this.releaseDirectionKeys();
         return true;
      } else {
         BlockPos var21 = null;
         Vec3 var22 = null;
         if (!this.putAt_CacheHelper.isSilent()) {
            Vec3 var8 = var1.getEyePosition();
            double var9 = var1.blockInteractionRange();
            AiDigLine.Inner_CachePool var11 = this.putAt_EventBus_JsonCache();
            Vec3 var12 = var3 && var2.type() == AiPathfinder.MoveType.DIG_ASCEND ? AiRoutePolicy.putAt_Vec3_Vec3_FileStore_PacketHe(var8, var2) : var8;
            this.putAt_EventBus_CacheHelp = this.putAt_EventBus_CacheHelp_FileStore_PacketHe(var2, var5, var12, var1, var11, var9);
            BlockPos var13 = this.putAt_EventBus_CacheHelp == null ? null : AiDigLine.firstHit(this.putAt_EventBus_CacheHelp, var8, var5, var11, var9);
            if (var13 != null) {
               var21 = var13;
               var22 = AiDigLine.aimPoint(this.putAt_EventBus_CacheHelp, var8, var11, var9);
            }
         }

         if (var21 == null) {
            var21 = (BlockPos)var5.get(0);
         }

         boolean var23 = var22 != null;
         if (this.client.gameMode instanceof AutismMultiPlayerGameModeAccessor var24 && var24.autism$getDestroyProgress() > 0.0F) {
            BlockPos var26 = var24.autism$getDestroyBlockPos();
            if (var26 != null && !var26.equals(var21) && var5.contains(var26)) {
               BlockHitResult var28 = AiAim.raycastLookBlock();
               BlockHitResult var29 = var28 != null && var28.getBlockPos().equals(var26) ? var28 : AiBlockInteraction.findReachableHit(this.client, var26);
               if (var29 != null) {
                  var21 = var26.immutable();
                  var22 = var29.getLocation();
                  var23 = false;
               }
            }
         }

         BlockPos var25 = this.putAt_BlockPos_LocalPlayer_BlockPos(var1, var21);
         if (var25 != null && this.value_FileStore_PacketHe != var2) {
            this.releaseDirectionKeys();
            if (!this.putAt_JsonCache.isWaitingDestroyDelay()) {
               this.putAt_V_Vec3(Vec3.atCenterOf(var25));
            }

            switch (this.putAt_JsonCache.dig(var25)) {
               case fill:
                  this.putAt_JsonCache.stop();
                  break;
               case readEx:
                  this.putAt_JsonCache.stop();
                  this.value_FileStore_PacketHe = var2;
               case putAt_JsonCache_CacheHel:
            }

            return true;
         } else {
            if (this.startToolRestock(var21)) {
               this.releaseDirectionKeys();
               return true;
            }

            this.aimingDigLine = var23;
            double var27 = var2.to().getX() + 0.5 - var1.getX();
            double var30 = var2.to().getZ() + 0.5 - var1.getZ();
            boolean var14 = var3
               && var27 * var27 + var30 * var30 > 0.6889
               && (var2.to().getY() >= var2.from().getY() || !isPassableNonWeb(this.cellAt(var2.to().above())));
            if (var14) {
               this.putAt_Z_F(AiPhysics.yawBetween(var1.getX(), var1.getZ(), var2.to().getX() + 0.5, var2.to().getZ() + 0.5));
            } else {
               this.client.options.keyUp.setDown(false);
               this.client.options.keyDown.setDown(false);
               this.client.options.keyLeft.setDown(false);
               this.client.options.keyRight.setDown(false);
            }

            this.client.options.keyJump.setDown(false);
            this.client.options.keyShift.setDown(var1.onClimbable() && !var1.onGround());
            this.applySprint(var1, false);

            try {
               switch (this.putAt_JsonCache.dig(var21, var22)) {
                  case fill:
                     this.putAt_JsonCache.stop();
                     boolean var15 = false;

                     for (BlockPos var17 : var4) {
                        if (!isPassableNonWeb(this.cellAt(var17))) {
                           var15 = true;
                           break;
                        }
                     }

                     if (!var15 && !this.fallingBlockAbove(var4)) {
                        return false;
                     }
                     break;
                  case readEx:
                     this.putAt_V_Str("dig failed: " + this.putAt_JsonCache.lastFailureReason());
                  case putAt_JsonCache_CacheHel:
               }
            } finally {
               this.aimingDigLine = false;
            }

            return true;
         }
      }
   }

   private BlockPos putAt_BlockPos_LocalPlayer_BlockPos(LocalPlayer var1, BlockPos var2) {
      if (AiBlockInteraction.findReachableHit(this.client, var2) != null) {
         return null;
      }

      Vec3 var3 = var1.getEyePosition();
      BlockHitResult var4 = this.client
         .level
         .clip(new ClipContext(var3, Vec3.atCenterOf(var2), net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, var1));
      if (var4.getType() != Type.BLOCK) {
         return null;
      }

      BlockPos var5 = var4.getBlockPos().immutable();
      if (!var5.equals(var2) && !(var3.distanceToSqr(var4.getLocation()) > var1.blockInteractionRange() * var1.blockInteractionRange())) {
         BlockState var6 = this.client.level.getBlockState(var5);
         if (!var6.isAir() && var6.getFluidState().isEmpty() && !var6.hasBlockEntity() && !(var6.getDestroySpeed(this.client.level, var5) < 0.0F)) {
            Block var7 = var6.getBlock();
            if (!AiRoutePolicy.putAt(var5)
               && (var6.canBeReplaced() || var7 instanceof VegetationBlock || var7 instanceof LeafLitterBlock || var7 instanceof SnowLayerBlock)) {
               return var6.getCollisionShape(this.client.level, var5).isEmpty() ? var5 : null;
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private AiDigLine.Inner_DataUtil putAt_EventBus_CacheHelp_FileStore_PacketHe(
      AiPathfinder.PathStep var1, List<BlockPos> var2, Vec3 var3, LocalPlayer var4, AiDigLine.Inner_CachePool var5, double var6
   ) {
      var var8 = new ArrayList<>(var2);
      var8.addAll(this.putAt_List_List(var2));
      var var9 = Set.copyOf(var8);
      boolean var10 = !var9.equals(this.lastDigCells);
      this.lastDigCells = var9;
      if (var10
         && var1.equals(this.readEx_FileStore_PacketHe)
         && this.fill_EventBus_CacheHelp != null
         && var9.equals(this.readEx_Set)
         && AiDigLine.firstHit(this.fill_EventBus_CacheHelp, var3, var2, var5, var6) != null) {
         this.readEx_FileStore_PacketHe = null;
         return this.fill_EventBus_CacheHelp;
      } else {
         return AiDigLine.updateLatched(
            this.putAt_EventBus_CacheHelp, var10, var3, var4.getYRot(), var4.getXRot(), var2, var8, var5, var6, this.pathHeadingYaw()
         );
      }
   }

   private float pathHeadingYaw() {
      if (this.moveIndex >= this.moves.size()) {
         return 0.0F / 0.0F;
      }

      BlockPos var1 = this.moves.get(this.moveIndex).from();
      BlockPos var2 = this.moves.get(Math.min(this.moves.size() - 1, this.moveIndex + 3)).to();
      return Math.hypot(var2.getX() - var1.getX(), var2.getZ() - var1.getZ()) < 1.5
         ? 0.0F / 0.0F
         : AiPhysics.yawBetween(var1.getX(), var1.getZ(), var2.getX(), var2.getZ());
   }

   private List<BlockPos> putAt_List_List(List<BlockPos> var1) {
      int var2 = -1;

      for (int var3 = this.moveIndex; var3 < Math.min(this.moves.size(), this.moveIndex + 3) && var2 < 0; var3++) {
         if (!Collections.disjoint(this.moves.get(var3).cellsToBreak(), var1)) {
            var2 = var3;
         }
      }

      if (var2 >= 0 && var2 + 1 < this.moves.size()) {
         AiPathfinder.PathStep var7 = this.moves.get(var2 + 1);
         if (var7.type() != AiPathfinder.MoveType.DIG_TUNNEL && var7.type() != AiPathfinder.MoveType.DIG_ASCEND && var7.type() != AiPathfinder.MoveType.DIG_DESCEND) {
            return List.of();
         }

         ArrayList<BlockPos> var4 = new ArrayList<>();

         for (BlockPos var6 : var7.cellsToBreak()) {
            if (!isPassableNonWeb(this.cellAt(var6)) && !var1.contains(var6)) {
               var4.add(var6);
            }
         }

         return var4;
      } else {
         return List.of();
      }
   }

   private void putAt_V_List_List(List<BlockPos> var1, List<BlockPos> var2) {
      for (BlockPos var4 : var1) {
         if (var2.contains(var4)) {
            this.pendingBreakCells.add(var4);
         } else if (this.pendingBreakCells.remove(var4)) {
            this.blocksBroken++;
         }
      }
   }

   private AiDigLine.Inner_CachePool putAt_EventBus_JsonCache() {
      return var1 -> this.client.level.getBlockState(var1).getShape(this.client.level, var1);
   }

   private Vec3 putAt_Vec3_LocalPlayer(LocalPlayer var1) {
      if (!this.putAt_CacheHelper.isSilent() && this.moveIndex + 1 < this.moves.size()) {
         AiPathfinder.PathStep var2 = this.moves.get(this.moveIndex + 1);
         if (var2.cellsToBreak().isEmpty()) {
            return null;
         }

         ArrayList var3 = new ArrayList();

         for (BlockPos var5 : var2.cellsToBreak()) {
            if (!isPassableNonWeb(this.cellAt(var5))) {
               var3.add(var5);
            }
         }

         if (var3.isEmpty()) {
            return null;
         }

         Vec3 var15 = new Vec3(var2.from().getX() + 0.5, var2.from().getY() + var1.getEyeHeight(), var2.from().getZ() + 0.5);
         double var16 = var1.blockInteractionRange();
         AiDigLine.Inner_CachePool var7 = this.putAt_EventBus_JsonCache();
         ArrayList var8 = new ArrayList(var3);
         var8.addAll(this.putAt_List_List(var3));
         var var9 = Set.copyOf(var8);
         if (!var2.equals(this.readEx_FileStore_PacketHe)
            || !var9.equals(this.readEx_Set)
            || this.fill_EventBus_CacheHelp == null
            || AiDigLine.firstHit(this.fill_EventBus_CacheHelp, var15, var3, var7, var16) == null) {
            this.fill_EventBus_CacheHelp = AiDigLine.planLine(
               var15, var1.getYRot(), var1.getXRot(), var3, var8, var7, var16, this.pathHeadingYaw()
            );
            this.readEx_FileStore_PacketHe = var2;
            this.readEx_Set = var9;
         }

         Vec3 var10 = this.fill_EventBus_CacheHelp == null ? null : AiDigLine.aimPoint(this.fill_EventBus_CacheHelp, var15, var7, var16);
         if (var10 == null) {
            return null;
         }

         RotationMath.Rotation var11 = RotationMath.lookAt(var10, var15);
         float var12 = Math.abs(var11.pitch()) > 70.0F ? var1.getYRot() : var11.yaw();
         Vec3 var13 = var1.getEyePosition().add(Vec3.directionFromRotation(var11.pitch(), var12).scale(Math.max(2.0, var10.distanceTo(var15))));
         float var14 = AiDigLine.pitchOf(var13.subtract(var1.getEyePosition()));
         if (Math.abs(var14) > 70.0F) {
            var13 = var1.getEyePosition().add(Vec3.directionFromRotation(var14, var1.getYRot()).scale(3.0));
         }

         return var13;
      } else {
         return null;
      }
   }

   private static boolean isPassableNonWeb(AiPathfinder.CellInfo var0) {
      return var0.passable() && !var0.web();
   }

   private boolean fallingBlockAbove(List<BlockPos> var1) {
      for (Entity var3 : this.client.level.entitiesForRendering()) {
         if (var3 instanceof FallingBlockEntity) {
            BlockPos var4 = var3.blockPosition();

            for (BlockPos var6 : var1) {
               if (var4.getX() == var6.getX() && var4.getZ() == var6.getZ() && var4.getY() >= var6.getY() - 1 && var4.getY() <= var6.getY() + 8) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private void runPillar(LocalPlayer var1, AiPathfinder.PathStep var2) {
      BlockPos var3 = var2.cellsToPlace().get(0);
      if (this.pillarJumpStarted || var2.cellsToBreak().isEmpty() || !var1.onGround() || !this.putAt_Z_LocalPlayer(var1, var2, false)) {
         if (!this.pillarJumpStarted) {
            double var4 = var3.getX() + 0.5 - var1.getX();
            double var6 = var3.getZ() + 0.5 - var1.getZ();
            this.releaseDirectionKeys();
            this.client.options.keyShift.setDown(true);
            if (!var1.onGround()) {
               return;
            }

            if (var4 * var4 + var6 * var6 > 0.04) {
               this.putAt_CacheHelper.setTargetRotation(putAt_TaskUtil_CacheHelp_LocalPlayer(var1));
               this.putAt_V_LocalPlayer(var1, var3.getX() + 0.5, var3.getZ() + 0.5, 0.08);
               return;
            }

            if (!AiRoutePolicy.putAt_Z_D(var1.getY(), var3.getY(), var2.repairFarmland())
               && (!this.isOnSoulSand(var1) || !AiRoutePolicy.putAt_Z_D(var1.getY() + 0.125, var3.getY(), false))) {
               this.putAt_V_Str("pillar takeoff is below the new support");
               return;
            }

            Vec3 var8 = var1.getDeltaMovement();
            if (var8.x * var8.x + var8.z * var8.z > 0.0064) {
               return;
            }

            if (this.placeSlotFor(var3) < 0 && (this.startScaffoldRestock() || this.stateOf_Z())) {
               return;
            }

            this.putAt_CacheHelper.setTargetRotation(putAt_TaskUtil_CacheHelp_LocalPlayer(var1));
            this.putAt_CacheHelper.tick();
            if (this.putAt_CacheHelper.angularError() > 6.0) {
               return;
            }

            this.pillarJumpStarted = true;
         }

         this.putAt_CacheHelper.setTargetRotation(putAt_TaskUtil_CacheHelp_LocalPlayer(var1));
         this.client.options.keyJump.setDown(true);
         this.client.options.keyUp.setDown(false);
         this.applySprint(var1, false);
         this.client.options.keyShift.setDown(false);
         if (this.supportPlaced) {
            this.client.options.keyJump.setDown(false);
         } else if (this.isPlacementConfirmed(var3)) {
            this.supportPlaced = true;
            this.client.options.keyJump.setDown(false);
            if (this.pendingPlacePos != null) {
               this.pillarBlocksPlaced++;
               AiRoutePolicy.putAt_V_BlockPos_Block(var3, this.putAt_Block);
               AiEditLedger.recordPlacement(var3);
               AutismClientAddon.LOG.info(
                     "[AI] placed {} at {} (pillar, {} -> {})",
                     this.putAt_Block,
                     var3.toShortString(),
                     this.currentMove == null ? "-" : this.currentMove.from().toShortString(),
                     this.currentMove == null ? "-" : this.currentMove.to().toShortString()
                  );
            }
         } else if (this.pendingPlacePos != null) {
            if (++this.placeConfirmTicks > 6) {
               this.putAt_V_Str("pillar block never confirmed");
            }
         } else {
            if (var1.getY() > var3.getY() + 1.01) {
               if (this.placeAttempts >= 6) {
                  this.putAt_V_Str("pillar place attempts used up");
                  return;
               }

               AiWrExecutor.PlaceResult var9 = this.putAt_RingCache_CacheHel_BlockPos(var3);
               if (var9 == AiWrExecutor.PlaceResult.putAt_RingCache_CacheHel) {
                  this.pendingPlacePos = var3;
               }

               if (var9 != AiWrExecutor.PlaceResult.fill) {
                  this.placeAttempts++;
               }
            }
         }
      }
   }

   private static RotationMath.Rotation putAt_TaskUtil_CacheHelp_LocalPlayer(LocalPlayer var0) {
      return new RotationMath.Rotation(var0.getYRot(), 90.0F);
   }

   private boolean isOnSoulSand(LocalPlayer var1) {
      BlockPos var2 = BlockPos.containing(var1.getX(), var1.getY() - 0.01, var1.getZ());
      return this.client.level.getBlockState(var2).is(Blocks.SOUL_SAND);
   }

   private AiWrExecutor.PlaceResult putAt_RingCache_CacheHel_BlockPos(BlockPos var1) {
      int var2 = this.placeSlotFor(var1);
      if (var2 < 0) {
         return this.fill_RingCache_CacheHel(var1);
      }

      if (this.putAt_CacheHelper.angularError() > 1.0) {
         return AiWrExecutor.PlaceResult.fill;
      }

      HitResult var3 = this.client.player.pick(this.client.player.blockInteractionRange(), 1.0F, false);
      if (var3 instanceof BlockHitResult var4 && var3.getType() == Type.BLOCK && var4.getBlockPos().equals(var1.below()) && var4.getDirection() == Direction.UP
         )
       {
         if (BlockPlacementPlanner.requiresSneakToPlace(this.client.level.getBlockState(var4.getBlockPos()), var4.getBlockPos())) {
            this.client.options.keyShift.setDown(true);
            if (!this.client.player.isShiftKeyDown() || !this.client.player.getLastSentInput().shift()) {
               return AiWrExecutor.PlaceResult.fill;
            }
         }

         this.selectSlot(var2);
         ItemStack var5 = this.client.player.getInventory().getItem(var2);
         this.lastPlacedItemId = BuiltInRegistries.ITEM.getKey(var5.getItem()).toString();
         this.placeRevision = AiServerEvidence.blockRevision(var1);
         this.putAt_Block = ((BlockItem)var5.getItem()).getBlock();
         InteractionResult var6 = this.client.gameMode.useItemOn(this.client.player, InteractionHand.MAIN_HAND, var4);
         if (var6.consumesAction() && !this.client.level.getBlockState(var1).isAir()) {
            AiRoutePolicy.putAt_V_BlockPos_Block(var1, this.putAt_Block);
         }

         return AiWrExecutor.PlaceResult.putAt_RingCache_CacheHel;
      } else {
         return this.fill_RingCache_CacheHel(var1);
      }
   }

   private boolean isSingleBridgeCell() {
      if (!this.putAt_CacheHelper.isSilent() && this.currentMove != null) {
         int var1 = this.currentMove.to().getX() - this.currentMove.from().getX();
         int var2 = this.currentMove.to().getZ() - this.currentMove.from().getZ();
         return this.currentMove.to().getY() == this.currentMove.from().getY()
            && Math.abs(var1) + Math.abs(var2) == 1
            && this.currentMove.cellsToPlace().size() == 1
            && this.currentMove.cellsToPlace().get(0).equals(this.currentMove.to().below());
      } else {
         return false;
      }
   }

   private void putAt_V_LocalPlayer(LocalPlayer var1) {
      AiPathfinder.PathStep var2 = this.currentMove;
      int var3 = var2.to().getX() - var2.from().getX();
      int var4 = var2.to().getZ() - var2.from().getZ();

      int var5;
      for (var5 = this.moveIndex; var5 + 1 < this.moves.size(); var5++) {
         AiPathfinder.PathStep var6 = this.moves.get(var5 + 1);
         if (var6.type() != AiPathfinder.MoveType.BRIDGE_CELL
            || !var6.from().equals(this.moves.get(var5).to())
            || var6.to().getX() - var6.from().getX() != var3
            || var6.to().getZ() - var6.from().getZ() != var4
            || var6.to().getY() != var2.from().getY()) {
            break;
         }
      }

      int var15 = var2.from().getY() - 1;
      int var7 = var5 - this.moveIndex + 1;
      this.bridgeRunEnd = var5;
      int var8 = var5 + 1 < this.moves.size() ? this.putAt_I_FileStore_PacketHe(this.moves.get(var5 + 1), var2.from(), var3, var4, var7) : -1;
      if (var8 > var7) {
         var7 = var8;
         var5++;
      }

      AiBridger.Inner_CachePool var9 = new AiBridger.Inner_CachePool(var2.from().getX(), var2.from().getZ(), var15, var3, var4, var7);
      AiBridger.Inner_ConfigStore var10 = AiBridgeDriver.chooseStyle(!this.dataEx_Z, var9, var1, this.scaffoldBlockCount());
      boolean var11 = var5 + 1 < this.moves.size() && putAt_Z_FileStore_PacketHe(this.moves.get(var5 + 1), var3, var4);
      if (var9.length() <= 2) {
         var10 = AiBridger.Inner_ConfigStore.LEGIT;
      }

      this.putAt_TokenCodec = new AiBridgeDriver(var9, var10, this.putAt_CacheHelper, var11);
      this.bridgeStartIndex = this.moveIndex;
      this.bridgeEndIndex = var5;
      double var12 = 0.0;

      for (int var14 = this.moveIndex; var14 <= var5; var14++) {
         var12 += AiPathfinder.MoveCosts.moveTimeoutTicks(this.moves.get(var14).costTicks());
      }

      this.bridgeTickBudget = (int)Math.ceil(var12) + 60;
      AutismClientAddon.LOG.info("[AI] bridge {} cells {} from {} toward {},{}", var9.length(), var10.name().toLowerCase(Locale.ROOT), var2.from().toShortString(), var3, var4);
   }

   private static boolean putAt_Z_FileStore_PacketHe(AiPathfinder.PathStep var0, int var1, int var2) {
      int var3 = Integer.signum(var0.to().getX() - var0.from().getX());
      int var4 = Integer.signum(var0.to().getZ() - var0.from().getZ());
      return var3 != var1 || var4 != var2;
   }

   private int putAt_I_FileStore_PacketHe(AiPathfinder.PathStep var1, BlockPos var2, int var3, int var4, int var5) {
      if (AiRoutePolicy.isGapJump(var1.type()) && var1.to().getY() == var2.getY()) {
         int var6 = var1.to().getX() - var2.getX();
         int var7 = var1.to().getZ() - var2.getZ();
         int var8 = var6 * var3 + var7 * var4;
         int var9 = Math.abs(var6 * var4 - var7 * var3);
         if (var8 > var5 && var9 <= 1) {
            for (int var10 = var5 + 1; var10 <= var8; var10++) {
               BlockPos var11 = new BlockPos(var2.getX() + var3 * var10, var2.getY() - 1, var2.getZ() + var4 * var10);
               AiPathfinder.CellInfo var12 = this.cellAt(var11);
               if (!var12.loaded()) {
                  return -1;
               }

               if (!var12.passable()) {
                  return var12.top16() >= 16 ? var10 - 1 : -1;
               }

               if (var12.water()
                  || var12.lava()
                  || !this.cellAt(var11.above()).passable()
                  || !this.cellAt(var11.above(2)).passable()) {
                  return -1;
               }
            }

            return -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private boolean prevEx_Z_LocalPlayer(LocalPlayer var1) {
      if (this.putAt_TokenCodec == null) {
         this.putAt_V_LocalPlayer(var1);
      }

      AiBridger.Inner_CachePool var2 = this.putAt_TokenCodec.getLane();
      if (this.putAt_TokenCodec.getTicks() > this.bridgeTickBudget) {
         this.putAt_V_Str("bridge over its time budget");
         return true;
      }

      int var3 = this.placeSlotFor(this.putAt_TokenCodec.nextSupport());
      if (var3 < 0 && var1.onGround() && this.putAt_TokenCodec.getBridger().putAt_I_TokenCore_TaskStat(this.putAt_NetEntry()) <= var2.length()) {
         this.releaseDirectionKeys();
         this.client.options.keyShift.setDown(true);
         if (this.plankCraftOp != null) {
            this.stateOf_V();
            return true;
         } else if (this.restock != null && this.restock.isRunning()) {
            this.store_V();
            return true;
         } else if (!this.startScaffoldRestock() && !this.stateOf_Z()) {
            this.putAt_V_Str("out of blocks mid-bridge");
            return true;
         } else {
            return true;
         }
      } else {
         AiBridgeDriver.BridgeStatus var4 = this.putAt_TokenCodec.step(var3, this::selectSlot, var3x -> {
            this.bridgeBlocksPlaced++;
            ItemStack var4x = var1.getInventory().getItem(var3);
            this.lastPlacedItemId = BuiltInRegistries.ITEM.getKey(var4x.getItem()).toString();
            Block var5x = this.client.level.getBlockState(var3x).getBlock();
            AiRoutePolicy.putAt_V_BlockPos_Block(var3x, var5x);
            AiEditLedger.recordPlacement(var3x);
         });
         if (var4 == AiBridgeDriver.BridgeStatus.INVALID_FOOTING) {
            this.putAt_V_Str("bridge footing below the planned support level");
            return true;
         }

         if (var4 == AiBridgeDriver.BridgeStatus.FELL) {
            this.putAt_V_Str("fell off the bridge");
            return true;
         }

         double var5 = var2.along(var1.getX(), var1.getZ());

         while (
            this.moveIndex < this.bridgeRunEnd
               && var5 >= this.moveIndex - this.bridgeStartIndex + 1
               && !this.cellAt(this.moves.get(this.moveIndex).cellsToPlace().get(0)).passable()
         ) {
            this.moveIndex++;
         }

         this.currentMove = this.moves.get(this.moveIndex);
         if (var4 != AiBridgeDriver.BridgeStatus.DONE) {
            return true;
         }

         this.elapsedCostTicks = this.elapsedCostTicks + this.moves.get(this.bridgeEndIndex).costTicks();
         this.moveIndex = this.bridgeEndIndex + 1;
         this.currentMove = null;
         this.putAt_TokenCodec = null;

         while (this.moveIndex < this.moves.size() && putAt_Z_FileStore_PacketHe_LocalPlayer(this.moves.get(this.moveIndex), var1)) {
            this.elapsedCostTicks = this.elapsedCostTicks + this.moves.get(this.moveIndex++).costTicks();
         }

         return false;
      }
   }

   private static boolean putAt_Z_FileStore_PacketHe_LocalPlayer(AiPathfinder.PathStep var0, LocalPlayer var1) {
      if (AiRoutePolicy.putAt_Z(var0) && !(Math.abs(var1.getY() - var0.to().getY()) > 0.6)) {
         double var2 = var0.to().getX() - var0.from().getX();
         double var4 = var0.to().getZ() - var0.from().getZ();
         double var6 = Math.hypot(var2, var4);
         if (var6 < 1.0E-6) {
            return false;
         }

         double var8 = var1.getX() - (var0.to().getX() + 0.5);
         double var10 = var1.getZ() - (var0.to().getZ() + 0.5);
         return (var8 * var2 + var10 * var4) / var6 > -0.3 && Math.abs(var8 * var4 - var10 * var2) / var6 < 1.5;
      } else {
         return false;
      }
   }

   private void runBridgeCell(LocalPlayer var1, AiPathfinder.PathStep var2) {
      BlockPos var3 = var2.cellsToPlace().get(0);
      if (!this.cellAt(var3).passable()) {
         if (!this.supportPlaced) {
            this.supportPlaced = true;
            if (this.pendingPlacePos != null) {
               this.bridgeBlocksPlaced++;
               AiRoutePolicy.putAt_V_BlockPos_Block(var3, this.putAt_Block);
               AiEditLedger.recordPlacement(var3);
               this.pendingPlacePos = null;
            }
         }
      } else if (this.supportPlaced) {
         this.supportPlaced = false;
      }

      if (this.supportPlaced) {
         this.client.options.keyShift.setDown(false);
         this.putAt_V_LocalPlayer_2(var1, var2, false, false, new Vec3(var2.to().getX() + 0.5, var1.getEyePosition().y, var2.to().getZ() + 0.5));
      } else {
         this.putAt_V_Vec3(Vec3.atCenterOf(var3));
         this.client.options.keyShift.setDown(true);
         this.client.options.keyJump.setDown(false);
         this.applySprint(var1, false);
         if (!var1.onGround() && var1.getY() < var2.from().getY() - 0.5) {
            this.putAt_V_Str("slipped into the gap before the support landed");
         } else if (this.pendingPlacePos != null) {
            if (++this.placeConfirmTicks > 6) {
               if (!this.bridgeRetried) {
                  this.placeConfirmTicks = 0;
                  AiWrExecutor.PlaceResult var8 = this.fill_RingCache_CacheHel(var3);
                  if (var8 == AiWrExecutor.PlaceResult.putAt_RingCache_CacheHel) {
                     this.bridgeRetried = true;
                  } else if (var8 == AiWrExecutor.PlaceResult.readEx) {
                     this.putAt_V_Str("bridge place impossible");
                  }
               } else {
                  this.putAt_V_Str("bridge block never confirmed");
               }
            }
         } else {
            double var4 = var1.getEyePosition().distanceTo(Vec3.atCenterOf(var3));
            boolean var6 = var1.onGround() && putAt_D_LocalPlayer_FileStore_PacketHe(var1, var2) <= 0.55;
            this.client.options.keyUp.setDown(!var6 && var4 > 1.8);
            if (!(var4 > 4.4)) {
               if (this.placeAttempts >= 6) {
                  this.putAt_V_Str("no face to place the bridge block on");
               } else {
                  AiWrExecutor.PlaceResult var7 = this.fill_RingCache_CacheHel(var3);
                  if (var7 == AiWrExecutor.PlaceResult.putAt_RingCache_CacheHel) {
                     this.pendingPlacePos = var3;
                  }

                  if (var7 != AiWrExecutor.PlaceResult.fill) {
                     this.placeAttempts++;
                  }
               }
            }
         }
      }
   }

   private static double putAt_D_LocalPlayer_FileStore_PacketHe(LocalPlayer var0, AiPathfinder.PathStep var1) {
      int var2 = Integer.signum(var1.to().getX() - var1.from().getX());
      int var3 = Integer.signum(var1.to().getZ() - var1.from().getZ());
      if (var2 > 0) {
         return var1.from().getX() + 1.0 - var0.getX();
      } else if (var2 < 0) {
         return var0.getX() - var1.from().getX();
      } else if (var3 > 0) {
         return var1.from().getZ() + 1.0 - var0.getZ();
      } else {
         return var3 < 0 ? var0.getZ() - var1.from().getZ() : 1.0;
      }
   }

   private void runDoor(LocalPlayer var1, AiPathfinder.PathStep var2) {
      BlockPos var3 = var2.to();
      boolean var4 = this.isClosedDoorAt(var3);
      int var5 = Integer.signum(var2.to().getX() - var2.from().getX());
      int var6 = Integer.signum(var2.to().getZ() - var2.from().getZ());
      Vec3 var7 = new Vec3(var2.to().getX() + 0.5 + var5 * 1.2, var1.getEyePosition().y, var2.to().getZ() + 0.5 + var6 * 1.2);
      if (var4) {
         this.putAt_V_Vec3(this.putAt_Vec3_BlockPos(var3));
         this.aimAndOpenDoor(var3);
      } else {
         this.putAt_V_Vec3(var7);
      }

      float var8 = AiPhysics.yawBetween(var1.getX(), var1.getZ(), var7.x, var7.z);
      boolean var9 = this.putAt_Z_F(var8);
      this.applySprint(var1, this.stateOf_Z && var9 && this.hasSprintFood(var1));
      this.client.options.keyJump.setDown(false);
      this.client.options.keyShift.setDown(false);
   }

   private Vec3 putAt_Vec3_BlockPos(BlockPos var1) {
      double var2 = this.client.player.getEyeY();
      boolean var4 = this.client.level.getBlockState(var1).getBlock() instanceof DoorBlock;
      double var5 = var1.getY() + (var4 ? 1.9 : 0.8);
      double var7 = var1.getY() + 0.1;
      return new Vec3(var1.getX() + 0.5, Math.max(var7, Math.min(var5, var2)), var1.getZ() + 0.5);
   }

   private void aimAndOpenDoor(BlockPos var1) {
      LocalPlayer var2 = this.client.player;
      if (var2 != null && this.isClosedDoorAt(var1) && var2.tickCount - this.syncAt_I >= 3) {
         this.putAt_CacheHelper.tick();
         BlockHitResult var3 = AiAim.raycastLookBlock();
         if (var3 != null && (var3.getBlockPos().equals(var1) || var3.getBlockPos().equals(var1.above()))) {
            this.client.gameMode.useItemOn(var2, InteractionHand.MAIN_HAND, var3);
            Compat263.swing(var2, InteractionHand.MAIN_HAND);
            this.syncAt_I = var2.tickCount;
         }
      }
   }

   private boolean putAt_Z_FileStore_PacketHe(AiPathfinder.PathStep var1) {
      if (this.isClosedDoorAt(var1.to())) {
         this.runDoor(this.client.player, var1);
         return false;
      }

      if (AiRoutePolicy.isKnownClosedDoor(var1.to())) {
         this.doorToClose = var1.to().immutable();
         this.store_I = 0;
         this.dataEx_I = Integer.signum(var1.to().getX() - var1.from().getX());
         this.frameOf_I = Integer.signum(var1.to().getZ() - var1.from().getZ());
      }

      return true;
   }

   private void flushOf_V() {
      if (this.doorToClose != null) {
         BlockPos var1 = this.doorToClose;
         if (this.client.player != null
            && this.client.level != null
            && this.isOpenDoorAt(var1)
            && ++this.store_I <= 24
            && !(this.client.player.blockPosition().distSqr(var1) > 36.0)) {
            if (AiRoutePolicy.putAt(
               this.putAt_List_BlockPos(var1), Vec3.atLowerCornerOf(var1), this.client.player.getBoundingBox(), this.dataEx_I, this.frameOf_I, 0.05
            )) {
               AABB var2 = new AABB(var1);
               if (this.client.level.getBlockState(var1).getBlock() instanceof DoorBlock) {
                  var2 = var2.expandTowards(0.0, 1.0, 0.0).expandTowards(0.0, -1.0, 0.0);
               }

               if (!var2.intersects(this.client.player.getBoundingBox())) {
                  this.releaseDirectionKeys();
                  this.putAt_V_Vec3(
                     AiRoutePolicy.putAt_Vec3_List(
                        this.client.level.getBlockState(var1).getShape(this.client.level, var1).toAabbs(),
                        Vec3.atLowerCornerOf(var1),
                        this.client.player.getEyePosition()
                     )
                  );
                  this.putAt_CacheHelper.tick();
                  if (AiAim.isLookingAtBlock(var1)) {
                     BlockHitResult var3 = AiAim.raycastLookBlock();
                     if (var3 != null) {
                        this.client.gameMode.useItemOn(this.client.player, InteractionHand.MAIN_HAND, var3);
                        Compat263.swing(this.client.player, InteractionHand.MAIN_HAND);
                     }

                     this.doorToClose = null;
                  }
               }
            }
         } else {
            this.doorToClose = null;
         }
      }
   }

   private List<AABB> putAt_List_BlockPos(BlockPos var1) {
      BlockState var2 = this.client.level.getBlockState(var1);
      return var2.hasProperty(BlockStateProperties.OPEN)
         ? var2.setValue(BlockStateProperties.OPEN, false).getShape(this.client.level, var1).toAabbs()
         : var2.getShape(this.client.level, var1).toAabbs();
   }

   private static boolean isDoorOrGate(BlockState var0) {
      return var0.getBlock() instanceof DoorBlock || var0.getBlock() instanceof FenceGateBlock;
   }

   private boolean isClosedDoorAt(BlockPos var1) {
      if (this.client.level == null) {
         return false;
      }

      BlockState var2 = this.client.level.getBlockState(var1);
      return isDoorOrGate(var2) && var2.hasProperty(BlockStateProperties.OPEN) && !var2.getValue(BlockStateProperties.OPEN);
   }

   private boolean isOpenDoorAt(BlockPos var1) {
      if (this.client.level == null) {
         return false;
      }

      BlockState var2 = this.client.level.getBlockState(var1);
      return isDoorOrGate(var2) && var2.hasProperty(BlockStateProperties.OPEN) && var2.getValue(BlockStateProperties.OPEN);
   }

   private void runSoftDescend(LocalPlayer var1, AiPathfinder.PathStep var2) {
      if (!var1.onGround() && !var1.isInWater() && !var1.onClimbable()) {
         this.steerFallIntoWater(var1, var2);
      } else if (var1.onGround() && !var1.isInWater() && !this.putAt_Z_TokenCore_TableCac_FileStore_PacketHe(putAt_TokenCore_TableCac_LocalPlayer(var1), var2)) {
         this.approachDropEdge(var1, var2);
      } else {
         this.putAt_V_LocalPlayer_2(var1, var2, false, false, null);
      }
   }

   private void steerFallIntoWater(LocalPlayer var1, AiPathfinder.PathStep var2) {
      Vec3 var3 = new Vec3(var2.to().getX() + 0.5, var1.getEyePosition().y, var2.to().getZ() + 0.5);
      this.putAt_V_Vec3(var3);
      double var4 = var2.to().getX() + 0.5;
      double var6 = var2.to().getZ() + 0.5;
      float var8 = AiPhysics.yawBetween(var1.getX(), var1.getZ(), var4, var6);
      AiPhysics.MotionState var9 = putAt_TokenCore_TableCac_LocalPlayer(var1);
      AiLiveWorld var10 = this.putAt_NetEntry();
      float var11 = this.putAt_CacheHelper.predictedNextYaw();
      AiPhysics.MoveInput[] var12 = new AiPhysics.MoveInput[]{
         putAt_TokenCore_CacheHel_F_F(var11, var8),
         new AiPhysics.MoveInput(0.0, 0.0, false, false, false, var11),
         putAt_TokenCore_CacheHel_F_F(var11, var8 + 180.0F)
      };
      int var13 = -1;
      int var14 = 1;
      double var15 = 1.7976931348623157E308;
      double var17 = 1.7976931348623157E308;

      for (int var19 = 0; var19 < var12.length; var19++) {
         AiStride.FallResult var20 = AiStride.fallResult(var9, var12[var19], false, var10);
         if (var20.down()) {
            double var21 = Math.hypot(var20.at().x() - var4, var20.at().z() - var6);
            if (var21 < var17) {
               var17 = var21;
               var14 = var19;
            }

            if (!(var20.at().y() > var2.to().getY() + 0.5)
               && this.cellAt(BlockPos.containing(var20.at().x(), var2.to().getY(), var20.at().z())).water()
               && var21 < var15) {
               var15 = var21;
               var13 = var19;
            }
         }
      }

      this.client.options.keyJump.setDown(false);
      this.client.options.keyShift.setDown(false);
      this.applySprint(var1, false);
      switch (var13 >= 0 ? var13 : var14) {
         case 0:
            this.putAt_Z_F(var8);
            break;
         case 2:
            this.putAt_Z_F(var8 + 180.0F);
            break;
         default:
            this.releaseDirectionKeys();
      }
   }

   private boolean putAt_Z_TokenCore_TableCac_FileStore_PacketHe(AiPhysics.MotionState var1, AiPathfinder.PathStep var2) {
      AiLiveWorld var3 = this.putAt_NetEntry();
      double var4 = var2.to().getX() + 0.5;
      double var6 = var2.to().getZ() + 0.5;

      for (int var8 = 0; var8 < 3; var8++) {
         AiPhysics.MotionState var9 = var1;
         boolean var10 = false;
         boolean var11 = false;
         boolean var12 = false;
         boolean var13 = false;
         ArrayList<AiPhysics.MotionState> var14 = new ArrayList<>();

         for (int var15 = 0; var15 < 60 && !var11; var15++) {
            float var16 = AiPhysics.yawBetween(var9.x(), var9.z(), var4, var6);
            AiPhysics.MoveInput var17 = var10 && var8 != 0
               ? (
                  var8 == 1
                     ? new AiPhysics.MoveInput(0.0, 0.0, false, false, false, var16)
                     : new AiPhysics.MoveInput(-1.0, 0.0, false, false, false, var16)
               )
               : new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var16);
            AiPhysics.MotionState var18 = AiPhysics.step(var9, var17, var3);
            if (AiPhysics.hitsHazard(var3, var9, var18)) {
               break;
            }

            var9 = var18;
            if (!var9.onGround()) {
               var10 = true;
            } else if (var10) {
               var11 = true;
            } else if (var15 > 20) {
               break;
            }

            if (var10) {
               var14.add(var9);
            }

            BlockPos var19 = BlockPos.containing(var9.x(), var9.y() + 0.01, var9.z());
            if (var10 && !var13 && this.cellAt(var19).water()) {
               var13 = true;
               var12 = !AiPathfinder.MoveGenerator.hasDropLandingClearance(var3, var9.x(), var9.z(), var19)
                  || !AiPathfinder.MoveGenerator.isFallTrajectoryClear(var3, var14, var19.getY());
            }
         }

         if (var11
            && !var12
            && !(var9.y() > var2.to().getY() + 0.5)
            && this.cellAt(BlockPos.containing(var9.x(), var2.to().getY(), var9.z())).water()) {
            return true;
         }
      }

      return false;
   }

   private void approachDropEdge(LocalPlayer var1, AiPathfinder.PathStep var2) {
      double var3 = var2.from().getX() + 0.5;
      double var5 = var2.from().getZ() + 0.5;
      double var7 = var3 - var1.getX();
      double var9 = var5 - var1.getZ();
      AiPhysics.MotionState var11 = putAt_TokenCore_TableCac_LocalPlayer(var1);
      if (Math.hypot(var7, var9) > 1.5) {
         this.putAt_V_LocalPlayer_2(var1, var2, false, false, null);
      } else if (Math.hypot(var7, var9) <= 0.1 && Math.hypot(var11.vx(), var11.vz()) < 0.03) {
         this.releaseDirectionKeys();
         this.putAt_V_Str("no way into the water from the drop's takeoff");
      } else {
         this.putAt_V_LocalPlayer(var1, var3, var5, 0.06);
         this.client.options.keyJump.setDown(false);
         this.client.options.keyShift.setDown(true);
         this.applySprint(var1, false);
      }
   }

   private void runClimb(LocalPlayer var1, AiPathfinder.PathStep var2) {
      boolean var3 = var2.to().getY() > var2.from().getY();
      if (var1.isInWater()) {
         this.putAt_CacheHelper.clear();
         this.putAt_V_LocalPlayer(var1, var2.to().getX() + 0.5, var2.to().getZ() + 0.5, 0.15);
         this.applySprint(var1, false);
         this.client.options.keyJump.setDown(var3);
         this.client.options.keyShift.setDown(!var3);
      } else {
         this.putAt_V_LocalPlayer_2(var1, var2, false, var3, null);
      }
   }

   private void putAt_V_LocalPlayer_2(LocalPlayer var1, AiPathfinder.PathStep var2, boolean var3, boolean var4, Vec3 var5) {
      Vec3 var6 = var5 != null ? var5 : AiRoutePolicy.putAt_Vec3_FileStore_PacketHe_D(var2, var1.getEyePosition().y);
      this.putAt_V_Vec3(var6);
      float var7 = AiPhysics.yawBetween(var1.getX(), var1.getZ(), var2.to().getX() + 0.5, var2.to().getZ() + 0.5);
      boolean var8 = this.putAt_Z_F(var7);
      this.applySprint(var1, var8 && var3 && !var1.isInWater());
      double var9 = var2.to().getY();
      if (var1.isInWater() && this.moveIndex < this.moves.size() && this.cellAt(var1.blockPosition().above(2)).passable()) {
         var9 = AiRoutePolicy.putAt_D_List_I(this.moves, this.moveIndex);
      }

      boolean var11 = var1.isInWater() && AiRoutePolicy.putAt_Z_D(var1.getY(), var9, var1.onGround(), var1.isUnderWater());
      this.client.options.keyJump.setDown(var4 || var11);
      this.client.options.keyShift.setDown(false);
   }

   private boolean isMoveComplete(AiPathfinder.PathStep var1) {
      LocalPlayer var2 = this.client.player;
      if (!var1.cellsToPlace().isEmpty()
         && !this.supportPlaced
         && !AiRoutePolicy.putAt(
            var2.blockPosition().getX(),
            var2.getY(),
            var2.blockPosition().getZ(),
            var2.onGround() || var2.isInWater() || var2.onClimbable(),
            var1.from().getX(),
            var1.to().getX(),
            var1.from().getZ(),
            var1.to().getZ(),
            var1.to().getY()
         )) {
         return false;
      } else if (var1.type() == AiPathfinder.MoveType.DOOR) {
         int var6 = Integer.signum(var1.to().getX() - var1.from().getX());
         int var4 = Integer.signum(var1.to().getZ() - var1.from().getZ());
         return !this.isClosedDoorAt(var1.to())
            && AiRoutePolicy.putAt(this.putAt_List_BlockPos(var1.to()), Vec3.atLowerCornerOf(var1.to()), var2.getBoundingBox(), var6, var4, 0.05);
      } else if (var1.type() == AiPathfinder.MoveType.CLIMB_1) {
         boolean var5 = var1.to().getY() > var1.from().getY();
         return (var5 ? var2.getY() >= var1.to().getY() + 0.1 : var2.getY() < var1.to().getY() + 0.9 && var2.getY() > var1.to().getY() - 0.5)
            && Math.abs(var2.getX() - (var1.to().getX() + 0.5)) < 1.4
            && Math.abs(var2.getZ() - (var1.to().getZ() + 0.5)) < 1.4
            && (var2.isInWater() || var2.onClimbable() || var2.onGround());
      } else if (var1.type() == AiPathfinder.MoveType.DIG_DOWN_1) {
         return !this.cellAt(var1.cellsToBreak().get(0)).passable()
            ? false
            : var2.getY() < var1.from().getY() - 0.5 && (var2.onGround() || var2.isInWater() || var2.onClimbable());
      } else if (var1.type() == AiPathfinder.MoveType.DIG_DESCEND) {
         return AiRoutePolicy.putAt_Z_FileStore_PacketHe(
            var1, var2.blockPosition().getX(), var2.blockPosition().getZ(), var2.getY(), var2.onGround() || var2.isInWater() || var2.onClimbable()
         );
      } else if (var1.type() == AiPathfinder.MoveType.DESCEND_SOFT) {
         return AiRoutePolicy.putAt_Z_D(var2.getY(), var1.to().getY(), var2.onGround(), var2.isInWater(), var2.onClimbable())
            && Math.abs(var2.getX() - (var1.to().getX() + 0.5)) < 1.4
            && Math.abs(var2.getZ() - (var1.to().getZ() + 0.5)) < 1.4;
      } else if (var2.isInWater()
         && this.cellAt(var1.to()).water()
         && !this.moves.isEmpty()
         && var1 != this.moves.get(this.moves.size() - 1)
         && Math.hypot(var2.getX() - (var1.to().getX() + 0.5), var2.getZ() - (var1.to().getZ() + 0.5)) <= 1.2
         && Math.abs(var2.getY() - var1.to().getY()) <= 1.0) {
         return true;
      } else if (var2.blockPosition().getX() == var1.to().getX() && var2.blockPosition().getZ() == var1.to().getZ()) {
         double var3 = AiPathfinder.MoveGenerator.surfaceY(var1.to().getY(), this.cellAt(var1.to().below()));
         return var1.type() != AiPathfinder.MoveType.PILLAR && var1.type() != AiPathfinder.MoveType.DIG_ASCEND
            ? Math.abs(var2.getY() - var3) <= 0.6 && (var2.onGround() || var2.isInWater() || var2.onClimbable())
            : var2.onGround() && Math.abs(var2.getY() - var3) <= 0.55;
      } else {
         return false;
      }
   }

   private boolean isInterruptible() {
      return this.currentMove == null
         || AiRoutePolicy.putAt_Z_FileStore_SyncPool(this.currentMove.type(), this.pillarJumpStarted, this.pendingPlacePos != null || this.supportPlaced);
   }

   private boolean isStepFeasible(AiPathfinder.PathStep var1) {
      return AiRoutePolicy.putAt_Z_FileStore_PacketHe(var1, this.putAt_Function(), this.dataEx_Z);
   }

   private boolean isAllowedPlainStep(AiPathfinder.PathStep var1) {
      return this.readEx_Predicate != null
         && var1.cellsToBreak().isEmpty()
         && var1.cellsToPlace().isEmpty()
         && var1.jump() == null
         && this.readEx_Predicate.test(var1.to());
   }

   private boolean isPlacementConfirmed(BlockPos var1) {
      return this.cellAt(var1).passable()
         ? false
         : this.pendingPlacePos == null
            || this.client.level.getBlockState(var1).getBlock() == this.putAt_Block && AiServerEvidence.blockRevision(var1) > this.placeRevision;
   }

   private AiWrExecutor.PlaceResult fill_RingCache_CacheHel(BlockPos var1) {
      int var2 = this.placeSlotFor(var1);
      if (var2 < 0) {
         if (this.startScaffoldRestock()) {
            return AiWrExecutor.PlaceResult.fill;
         } else {
            return this.stateOf_Z() ? AiWrExecutor.PlaceResult.fill : AiWrExecutor.PlaceResult.readEx;
         }
      } else {
         this.selectSlot(var2);
         ItemStack var3 = this.client.player.getInventory().getItem(var2);
         this.lastPlacedItemId = BuiltInRegistries.ITEM.getKey(var3.getItem()).toString();
         BlockPlacementPlanner.Query var4 = new BlockPlacementPlanner.Query(
               var1,
               this.client.player.getEyePosition(),
               this.client.player.blockInteractionRange(),
               BlockPlacementPlanner.blockItemValidator(var3, this.client.player, InteractionHand.MAIN_HAND)
            )
            .withSneaking(this.client.player.isShiftKeyDown() && this.client.player.getLastSentInput().shift())
            .withRayBudget(new BlockPlacementPlanner.RayBudget(6));
         BlockPlacementPlanner.RejectReason[] var5 = new BlockPlacementPlanner.RejectReason[1];
         BlockPlacementPlanner.PlacementPlan var6 = BlockPlacementPlanner.plan(var4, var5);
         if (var6 == null) {
            return AiWrExecutor.PlaceResult.readEx;
         }

         this.putAt_CacheHelper.setProfile(AiAim.AimProfile.FAST);
         RotationMath.Rotation var7 = var6.aim().goalRotation();
         Vec3 var8 = this.client.player.getEyePosition();
         Vec3 var9 = var6.aim().point();
         boolean var10 = this.currentMove != null && this.currentMove.type() == AiPathfinder.MoveType.PILLAR;
         if (var10 && var7 != null && var9 != null && Math.hypot(var9.x - var8.x, var9.z - var8.z) < 0.4) {
            this.putAt_CacheHelper.aimAt(var9);
         } else if (var7 != null) {
            this.putAt_CacheHelper.setTargetRotation(var7);
         } else {
            this.putAt_CacheHelper.aimAt(var9);
         }

         this.putAt_CacheHelper.tick();
         if (!AiAim.isLookingAtBlock(var6.hit().getBlockPos())) {
            return AiWrExecutor.PlaceResult.fill;
         }

         this.placeRevision = AiServerEvidence.blockRevision(var1);
         this.putAt_Block = ((BlockItem)var3.getItem()).getBlock();
         InteractionResult var11 = this.client.gameMode.useItemOn(this.client.player, InteractionHand.MAIN_HAND, var6.hit());
         if (var11.consumesAction() && !this.client.level.getBlockState(var1).isAir()) {
            AiRoutePolicy.putAt_V_BlockPos_Block(var1, this.putAt_Block);
         }

         return AiWrExecutor.PlaceResult.putAt_RingCache_CacheHel;
      }
   }

   private int placeSlotFor(BlockPos var1) {
      AiRoutePolicy.PlacementPolicy var2 = AiRoutePolicy.putAt_ValueSink_TableCac();
      if (var2 != null && var1 != null && this.client.player != null) {
         if (!var2.fill(var1)) {
            return -1;
         }

         Item var3 = var2.putAt_Item(var1);
         if (var3 != null) {
            for (int var4 = 0; var4 < 9; var4++) {
               if (this.client.player.getInventory().getItem(var4).is(var3)) {
                  return var4;
               }
            }

            return -1;
         }
      }

      return this.bestScaffoldSlot();
   }

   private int bestScaffoldSlot() {
      int var1 = -1;
      int var2 = 2147483647;
      int var3 = -1;

      for (int var4 = 0; var4 < 9; var4++) {
         ItemStack var5 = this.client.player.getInventory().getItem(var4);
         if (var5.getItem() instanceof BlockItem var6
            && !(var6.getBlock() instanceof FallingBlock)
            && var6.getBlock().defaultBlockState().isCollisionShapeFullBlock(this.client.level, this.client.player.blockPosition())
            && !var6.getBlock().defaultBlockState().hasBlockEntity()) {
            String var9 = BuiltInRegistries.ITEM.getKey(var5.getItem()).toString();
            int var8 = AiRestock.scaffoldRank(var9);
            if (var8 >= 0 && AiRestock.canSpendStack(var5, this.client.player.getInventory())) {
               if (var9.equals(this.lastPlacedItemId)) {
                  var8 = -1;
               }

               if (var8 < var2 || var8 == var2 && var5.getCount() > var3) {
                  var1 = var4;
                  var2 = var8;
                  var3 = var5.getCount();
               }
            }
         }
      }

      return var1;
   }

   private int scaffoldBlockCount() {
      int var1 = 0;

      for (int var2 = 0; var2 < 9; var2++) {
         ItemStack var3 = this.client.player.getInventory().getItem(var2);
         if (var3.getItem() instanceof BlockItem var4
            && !(var4.getBlock() instanceof FallingBlock)
            && var4.getBlock().defaultBlockState().isCollisionShapeFullBlock(this.client.level, this.client.player.blockPosition())
            && !var4.getBlock().defaultBlockState().hasBlockEntity()) {
            String var6 = BuiltInRegistries.ITEM.getKey(var3.getItem()).toString();
            if (AiRestock.scaffoldRank(var6) >= 0 && AiRestock.canSpendStack(var3, this.client.player.getInventory())) {
               var1 += var3.getCount();
            }
         }
      }

      return var1;
   }

   private BlockState scaffoldPreviewState() {
      int var1 = this.bestScaffoldSlot();
      return var1 >= 0 && this.client.player.getInventory().getItem(var1).getItem() instanceof BlockItem var2
         ? var2.getBlock().defaultBlockState()
         : null;
   }

   private Item putAt_Item() {
      AiRoutePolicy.PlacementPolicy var1 = AiRoutePolicy.putAt_ValueSink_TableCac();
      return var1 != null && this.currentMove != null && !this.currentMove.cellsToPlace().isEmpty()
         ? var1.putAt_Item(this.currentMove.cellsToPlace().get(0))
         : null;
   }

   private boolean startScaffoldRestock() {
      Item var1 = this.putAt_Item();
      if (!this.restockEnabled && !this.plankCraftDone && var1 == null) {
         return false;
      }

      if (this.restock == null) {
         this.restock = new AiRestock(this.client);
      }

      if (!this.restock.isRunning() && !this.restockFailed) {
         ArrayList var2 = new ArrayList(27);

         for (int var3 = 9; var3 < 36; var3++) {
            var2.add(this.putAt_SyncCore_JsonCache_ItemStack(this.client.player.getInventory().getItem(var3)));
         }

         int var7 = -1;
         if (var1 != null) {
            for (int var4 = 9; var4 < 36 && var7 < 0; var4++) {
               if (this.client.player.getInventory().getItem(var4).is(var1)) {
                  var7 = var4 - 9;
               }
            }

            if (var7 < 0) {
               return false;
            }
         } else {
            var7 = AiRestock.pickBestBackpackSlot(var2, this.lastPlacedItemId);
         }

         if (var7 < 0) {
            return false;
         }

         ArrayList<AiRestock.HotbarSlotView> var8 = new ArrayList<>(9);

         for (int var5 = 0; var5 < 9; var5++) {
            ItemStack var6 = this.client.player.getInventory().getItem(var5);
            var8.add(
               new AiRestock.HotbarSlotView(var6.isEmpty(), var6.getItem() instanceof BlockItem, var6.has(DataComponents.FOOD), var6.isDamageableItem())
            );
         }

         int var9 = AiRestock.pickTargetHotbarSlot(var8);
         if (var9 < 0) {
            return false;
         }

         String var10 = var1 != null ? BuiltInRegistries.ITEM.getKey(var1).toString() : ((AiRestock.BackpackSlotView)var2.get(var7)).itemId();
         this.restock.startSwap(9 + var7, var9, var10);
         return true;
      } else {
         return false;
      }
   }

   private boolean stateOf_Z() {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      if (this.plankCraftOp != null || this.plankCraftFailed || this.client.player == null) {
         return false;
      }

      if (AiRestock.availableSpareWood(this.client.player.getInventory()) <= 0) {
         return false;
      }

      int var1 = 0;

      for (int var2 = this.moveIndex; var2 < this.moves.size(); var2++) {
         var1 += this.moves.get(var2).cellsToPlace().size();
      }

      this.plankCraftOp = AiPlanks.craftPlanks(this.client.player.getInventory(), Math.max(1, var1));
      if (this.plankCraftOp == null) {
         this.plankCraftFailed = true;
         return false;
      } else {
         return true;
      }
   }

   private boolean startToolRestock(BlockPos var1) {
      if (this.restockEnabled && this.restock != null && !this.restock.isRunning() && !this.restockFailed) {
         BlockState var2 = this.client.level.getBlockState(var1);
         int var3 = autismclient.util.BlockBreakSpeedHelper.bestToolSlot(this.client, var2, 9, false);
         if (var3 < 0
            || var2.requiresCorrectToolForDrops()
               && AiPathfinder.LiveGridView.isOreBlock(var2)
               && !this.client.player.getInventory().getItem(var3).isCorrectToolForDrops(var2)) {
            if (ToolUpkeep.decide(this.putAt_List_BlockState(var2)) instanceof ToolUpkeep.UpkeepDecision.SwapFromBackpack var4) {
               int var6 = var4.backpackSlot();
               this.restock.startSwap(
                     var6,
                     this.client.player.getInventory().getSelectedSlot(),
                     BuiltInRegistries.ITEM.getKey(this.client.player.getInventory().getItem(var6).getItem()).toString()
                  );
               return true;
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

   private AiRestock.BackpackSlotView putAt_SyncCore_JsonCache_ItemStack(ItemStack var1) {
      return var1.getItem() instanceof BlockItem var2 && AiRestock.canSpendStack(var1, this.client.player.getInventory())
         ? new AiRestock.BackpackSlotView(
            BuiltInRegistries.ITEM.getKey(var1.getItem()).toString(),
            var1.getCount(),
            var2.getBlock() instanceof FallingBlock,
            var2.getBlock().defaultBlockState().isCollisionShapeFullBlock(this.client.level, this.client.player.blockPosition()),
            var2.getBlock().defaultBlockState().hasBlockEntity()
         )
         : new AiRestock.BackpackSlotView(null, 0, false, false, false);
   }

   private List<ToolUpkeep.ToolSlot> putAt_List_BlockState(BlockState var1) {
      boolean var2 = !var1.requiresCorrectToolForDrops();
      ArrayList<ToolUpkeep.ToolSlot> var3 = new ArrayList<>(36);

      for (int var4 = 0; var4 < 36; var4++) {
         ItemStack var5 = this.client.player.getInventory().getItem(var4);
         var3.add(
            new ToolUpkeep.ToolSlot(
               var4,
               var5.isDamageableItem() ? var5.getMaxDamage() - var5.getDamageValue() : -1,
               var2 || var5.isCorrectToolForDrops(var1),
               AiItems.isDisposableTierTool(var5)
            )
         );
      }

      return var3;
   }

   private void stateOf_V() {
      AiOperation.Result var1 = this.plankCraftOp.tick();
      if (var1.terminal()) {
         if (var1.status() == AiOperation.Status.SUCCEEDED) {
            this.plankCraftDone = true;
         } else {
            this.plankCraftFailed = true;
         }

         this.plankCraftOp = null;
      }
   }

   private void store_V() {
      switch (this.restock.tick()) {
         case SUCCEEDED:
            this.restockCount++;
            this.restock.reset();
            break;
         case FAILED:
            this.restockFailed = true;
            this.restock.reset();
      }
   }

   private void selectSlot(int var1) {
      if (this.swappedSlot == -1) {
         this.originalSlot = this.client.player.getInventory().getSelectedSlot();
      }

      if (this.client.player.getInventory().getSelectedSlot() != var1) {
         this.client.player.getInventory().setSelectedSlot(var1);
         this.swappedSlot = var1;
      }
   }

   private AiLiveWorld putAt_NetEntry() {
      if (this.putAt_NetEntry == null || this.putAt_NetEntry.clientLevel() != this.client.level) {
         this.putAt_NetEntry = new AiLiveWorld(this.client.level, this.client.player);
      }

      this.putAt_NetEntry.putAt_V_D(this.fallPolicy.maxDropBlocks);
      return this.putAt_NetEntry;
   }

   private AiLiveGrid putAt_PoolStore() {
      AiChunkMemory var1 = this.chunkMemory != null ? this.chunkMemory : AiChunkMemory.getInstance();
      if (this.putAt_PoolStore == null || this.putAt_PoolStore.level() != this.client.level || this.fill_PacketHelper != var1) {
         ItemStack[] var2 = new ItemStack[9];
         Arrays.fill(var2, ItemStack.EMPTY);
         this.putAt_PoolStore = new AiLiveGrid(this.client, var2, var1);
         this.fill_PacketHelper = var1;
      }

      return this.putAt_PoolStore;
   }

   private static AiPhysics.MotionState putAt_TokenCore_TableCac_LocalPlayer(LocalPlayer var0) {
      return AiMotionSafety.snapshotPlayer(var0);
   }

   private AiPathfinder.CellInfo cellAt(BlockPos var1) {
      AiPathfinder.CellInfo var2 = AiPathfinder.LiveGridView.classifyCell(
         this.client, this.client.level, var1, scratchHotbar
      );
      if (!var2.loaded() && this.chunkMemory != null) {
         AiPathfinder.CellInfo var3 = this.chunkMemory.probeCell(var1.getX(), var1.getY(), var1.getZ());
         return var3 != null ? var3 : var2;
      } else {
         return var2;
      }
   }

   private Function<BlockPos, AiPathfinder.CellInfo> putAt_Function() {
      if (this.fill_ItemStackArr == null && this.client.player != null) {
         this.fill_ItemStackArr = new ItemStack[36];

         for (int var1 = 0; var1 < 36; var1++) {
            this.fill_ItemStackArr[var1] = this.client.player.getInventory().getItem(var1).copy();
         }
      }

      ItemStack[] var2 = this.fill_ItemStackArr != null ? this.fill_ItemStackArr : scratchHotbar;
      return var2x -> {
         AiPathfinder.CellInfo var3 = AiPathfinder.LiveGridView.classifyCell(
            this.client, this.client.level, var2x, var2
         );
         if (!var3.loaded() && this.chunkMemory != null) {
            AiPathfinder.CellInfo var4 = this.chunkMemory.probeCell(var2x.getX(), var2x.getY(), var2x.getZ());
            return var4 != null ? var4 : var3;
         } else {
            return var3;
         }
      };
   }

   private void putAt_V_Vec3(Vec3 var1) {
      LocalPlayer var2 = this.client.player;
      if (this.stalledDigTarget != null && !this.putAt_JsonCache.isDiggingTarget(this.stalledDigTarget)) {
         this.stalledDigTarget = null;
      }

      if (this.putAt_JsonCache.isAimStalled() && this.putAt_JsonCache.activeTarget() != null) {
         this.stalledDigTarget = this.putAt_JsonCache.activeTarget();
      }

      if (var2 != null && !this.putAt_JsonCache.isAimStalled()) {
         Vec3 var3 = var2.getEyePosition();
         if (Mth.floor(var1.x) == Mth.floor(var3.x)
            && Mth.floor(var1.z) == Mth.floor(var3.z)
            && Math.abs(var1.y - var3.y) > 1.0
            && (
               this.currentMove == null
                  || this.currentMove.cellsToBreak().isEmpty()
                  || this.currentMove.cellsToBreak().contains(BlockPos.containing(var1))
            )) {
            BlockPos var12 = BlockPos.containing(var1);
            VoxelShape var13 = this.client.level == null
               ? null
               : this.client.level.getBlockState(var12).getShape(this.client.level, var12);
            boolean var14 = var1.y < var3.y;
            double var7 = var13 != null && !var13.isEmpty()
               ? var12.getY() + (var14 ? var13.max(Axis.Y) : var13.min(Axis.Y))
               : var12.getY() + (var14 ? 1.0 : 0.0);
            this.putAt_CacheHelper.setTargetRotation(
                  new RotationMath.Rotation(var2.getYRot(), AiMotionSafety.requiredPitchForPlane(var3, var2.getYRot(), var2.getXRot(), var14, var7))
               );
            return;
         }

         if (this.currentMove != null
            && !this.currentMove.cellsToBreak().isEmpty()
            && !this.aimingDigLine
            && this.stalledDigTarget == null) {
            int var4 = this.currentMove.to().getX() - this.currentMove.from().getX();
            int var5 = this.currentMove.to().getZ() - this.currentMove.from().getZ();
            if (var4 == 0 != (var5 == 0)
               && Mth.floor(var1.x) == this.currentMove.to().getX()
               && Mth.floor(var1.z) == this.currentMove.to().getZ()) {
               double var6 = var4 != 0
                  ? Math.abs(var2.getZ() - (this.currentMove.from().getZ() + 0.5))
                  : Math.abs(var2.getX() - (this.currentMove.from().getX() + 0.5));
               if (var6 < 0.3) {
                  double var8 = Math.hypot(var1.x - var3.x, var1.z - var3.z);
                  float var10 = (float)Math.toDegrees(Math.atan2(var3.y - var1.y, Math.max(0.3, var8)));
                  float var11 = AiPhysics.yawBetween(0.0, 0.0, var4, var5);
                  if (this.putAt_Z_Vec3(var3, var11, var10, BlockPos.containing(var1))) {
                     this.putAt_CacheHelper.setTargetRotation(new RotationMath.Rotation(var11, var10));
                     return;
                  }
               }
            }
         }
      }

      this.putAt_CacheHelper.aimAt(var1);
   }

   private boolean putAt_Z_Vec3(Vec3 var1, float var2, float var3, BlockPos var4) {
      Vec3 var5 = var1.add(Vec3.directionFromRotation(var3, var2).scale(Math.max(4.5, this.client.player.blockInteractionRange())));
      BlockHitResult var6 = this.client
         .level
         .clip(new ClipContext(var1, var5, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, this.client.player));
      return var6.getType() == Type.BLOCK && var6.getBlockPos().equals(var4);
   }

   private void putAt_V_LocalPlayer(LocalPlayer var1, double var2, double var4, double var6) {
      double var8 = Math.toRadians(var1.getYRot());
      double var10 = var2 - var1.getX();
      double var12 = var4 - var1.getZ();
      double var14 = var10 * -Math.sin(var8) + var12 * Math.cos(var8);
      double var16 = var10 * -Math.cos(var8) + var12 * -Math.sin(var8);
      this.client.options.keyUp.setDown(var14 > var6);
      this.client.options.keyDown.setDown(var14 < -var6);
      this.client.options.keyRight.setDown(var16 > var6);
      this.client.options.keyLeft.setDown(var16 < -var6);
   }

   private boolean putAt_Z_F(float var1) {
      int[] var2 = AiMotionSafety.relativeMoveAxes(this.putAt_CacheHelper.predictedNextYaw(), var1);
      this.client.options.keyUp.setDown(var2[0] > 0);
      this.client.options.keyDown.setDown(var2[0] < 0);
      this.client.options.keyRight.setDown(var2[1] > 0);
      this.client.options.keyLeft.setDown(var2[1] < 0);
      return var2[0] > 0;
   }

   private static AiPhysics.MoveInput putAt_TokenCore_CacheHel_F_F(float var0, float var1) {
      int[] var2 = AiMotionSafety.relativeMoveAxes(var0, var1);
      return new AiPhysics.MoveInput(var2[0], -var2[1], false, false, false, var0);
   }

   private void putAt_V_LocalPlayer_TokenCore_CacheHel(LocalPlayer var1, AiPhysics.MoveInput var2) {
      AiPacePlayback.putAt_V_LocalPlayer(var1, var2, var1.getXRot());
   }

   private boolean hasSprintFood(LocalPlayer var1) {
      return var1.getFoodData().getFoodLevel() > 6;
   }

   private void applySprint(LocalPlayer var1, boolean var2) {
      AiMotionSafety.setSprintAllowed(var1, var2);
      var1.setSprinting(var2);
      if (!this.client.options.toggleSprint().get()) {
         this.client.options.keySprint.setDown(var2);
      }
   }

   private void releaseDirectionKeys() {
      this.client.options.keyUp.setDown(false);
      this.client.options.keyDown.setDown(false);
      this.client.options.keyLeft.setDown(false);
      this.client.options.keyRight.setDown(false);
      this.client.options.keySprint.setDown(false);
      this.client.options.keyJump.setDown(false);
      this.client.options.keyShift.setDown(false);
      if (this.client.player != null) {
         this.client.player.setSprinting(false);
      }
   }

   private void stopWalking() {
      this.releaseDirectionKeys();
   }

   private String putAt_Str_FileStore_PacketHe(AiPathfinder.PathStep var1) {
      if (var1 != null && this.client.level != null) {
         BlockPos var2 = var1.to();
         Function var3 = var1x -> BuiltInRegistries.BLOCK.getKey(this.client.level.getBlockState(((BlockPos)var1x)).getBlock()).getPath();
         return " (target feet "
            + (String)var3.apply(var2)
            + ", head "
            + (String)var3.apply(var2.above())
            + ", floor "
            + (String)var3.apply(var2.below())
            + ")";
      } else {
         return "";
      }
   }

   private void putAt_V_Str(String var1) {
      this.lastFailureReason = var1;
      this.putAt_V_BlockPos_FileStore_PacketHe(
         this.currentMove == null ? this.currentPos() : this.currentMove.to(), this.currentMove
      );
   }

   private void putAt_V_BlockPos_FileStore_PacketHe(BlockPos var1, AiPathfinder.PathStep var2) {
      if (navigationDebug && this.client.player != null) {
         AiNavDebug.log("wr stall move=" + var2 + " reason=" + this.lastFailureReason + " pos=" + this.client.player.position());
      }

      this.prev_BlockPos = var1;
      this.chunkAt_FileStore_PacketHe = var2;
      this.putAt_RingCache_TableCac = AiWrExecutor.RunState.value;
      this.clearRunState(true);
   }

   private void clearRunState(boolean var1) {
      this.tick_V();
      this.putAt_TokenCodec = null;
      this.pendingBreakCells.clear();
      this.putAt_JsonCache.stop();
      AiMotionSafety.releaseMovementKeys();
      this.doorToClose = null;
      if (this.restock != null) {
         this.restock.reset();
      }

      if (this.plankCraftOp != null) {
         this.plankCraftOp.cancel();
         this.plankCraftOp = null;
      }

      if (var1) {
         this.putAt_PoolStore = null;
         this.fill_PacketHelper = null;
         if (this.client.player != null && this.swappedSlot >= 0 && this.client.player.getInventory().getSelectedSlot() == this.swappedSlot) {
            this.client.player.getInventory().setSelectedSlot(this.originalSlot);
         }

         this.swappedSlot = -1;
      }
   }

   enum PlaceResult {
      putAt_RingCache_CacheHel,
      fill,
      readEx;
   }

   private static final class PendingRecovery {
      final int startIndex;
      final int endIndex;
      final int recoverCount;
      final boolean fineSearch;
      final AiPhysics.MotionState airborneState;
      int settleTicks;

      PendingRecovery(int var1, int var2, boolean var3, int var4, AiPhysics.MotionState var5) {
         this.startIndex = var1;
         this.endIndex = var2;
         this.fineSearch = var3;
         this.recoverCount = var4;
         this.airborneState = var5;
      }
   }

   public enum RunState {
      putAt_RingCache_TableCac,
      fill,
      readEx,
      value,
      prevEx;
   }

   private static final class PaceRun {
      int startIndex;
      int endIndex;
      final double planSpeed;
      final AiPhysics.MotionState startState;
      final AiPaceScript.Inner_JsonCache putAt_TextPool_JsonCache;
      final Predicate<BlockPos> putAt_Predicate;
      final AiPaceScript.Inner_IoHelper putAt_TextPool_IoHelper = new AiPaceScript.Inner_IoHelper();
      final AiPacePlayback putAt_TableUtil;
      final boolean fineSearch;
      final int recoverCount;
      final int driftRestarts;
      volatile boolean cancelled;
      volatile boolean searchDone;
      int starvedTicks;
      boolean braking;
      int brakeTicks;
      boolean replanOnLanding;
      int cellCursor;

      PaceRun(
         int var1,
         int var2,
         double var3,
         double var5,
         AiPhysics.MotionState var7,
         AiPaceScript.Inner_JsonCache var8,
         Predicate<BlockPos> var9,
         boolean var10,
         int var11,
         int var12,
         int var13
      ) {
         this.startIndex = var1;
         this.endIndex = var2;
         this.planSpeed = var3;
         this.startState = var7;
         this.putAt_TextPool_JsonCache = var8;
         this.putAt_Predicate = var9;
         this.putAt_TableUtil = new AiPacePlayback(this.putAt_TextPool_IoHelper, var3, var5);
         this.fineSearch = var10;
         this.recoverCount = var11;
         this.starvedTicks = var12;
         this.driftRestarts = var13;
         this.cellCursor = 0;
      }

      void awaitFirstTicks(long var1) {
         long var3 = System.nanoTime() + var1;

         while (this.putAt_TextPool_IoHelper.size() == 0 && !this.putAt_TextPool_IoHelper.isFinished() && !this.cancelled && System.nanoTime() < var3) {
            Thread.onSpinWait();
         }
      }
   }
}
