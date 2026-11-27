package autismclient.ai.engine;

import autismclient.AutismClientAddon;
import autismclient.ai.AiFailure;
import autismclient.api.ai.AiOperation;
import autismclient.util.AutismLiteVariant;
import autismclient.util.BlockPlacementPlanner;
import autismclient.util.RotationMath;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AiSchematicApproach implements AiOperation {
   private final AiSchematicApproach.Inner_DataUtil draft;
   private final long deadline;
   private final Set<Long> keepCells;
   private final Set<Long> avoidCells;
   private final Vec3 startPosition;
   private final AiSchematicBatchPlan.Inner_DataUtil init_c_DataUtil = new AiSchematicBatchPlan.Inner_DataUtil(System::nanoTime, 10000000000L);
   private boolean movedSinceStart;
   private AiOperation child;
   private boolean load_Z;
   private BlockPos init_BlockPos;
   private Function<Predicate<AABB>, Vec3> look;
   private Runnable click;
   private BooleanSupplier pending = () -> false;

   public void init(Function<Predicate<AABB>, Vec3> look, Runnable click, BooleanSupplier pending) {
      if (!AutismLiteVariant.enabled()) {
         this.look = look;
         this.click = click;
         this.pending = pending;
         if (this.child != null) {
            AiWalkingWork.init(this.child, look, click);
         }
      }
   }

   AiSchematicApproach(List<PlacementGoal.PlacementRequest> targets, int seconds, AABB volume, Set<Long> protectedCells, Set<Long> avoid) {
      this(new AiSchematicApproach.Inner_DataUtil(targets, volume, protectedCells, avoid), seconds, protectedCells, avoid);
   }

   private AiSchematicApproach(AiSchematicApproach.Inner_DataUtil draft, int seconds, Set<Long> keep, Set<Long> avoid) {
      this.draft = draft;
      this.keepCells = Set.copyOf(keep);
      this.avoidCells = Set.copyOf(avoid);
      this.startPosition = Minecraft.getInstance().player.position();
      this.deadline = System.nanoTime() + Math.max(1, seconds) * 1000000000L;
      draft.load_V();
      AiMotionSafety.releaseMovementKeys();
   }

   @Override
   public AiOperation.Result tick() {
      if (AutismLiteVariant.enabled()) {
         throw new IllegalStateException();
      }

      Minecraft mc = Minecraft.getInstance();
      if (!this.load_Z && mc.player != null && mc.level == this.draft.level) {
         this.movedSinceStart = this.movedSinceStart | mc.player.position().distanceToSqr(this.startPosition) > 0.0625;
         this.movedSinceStart = this.movedSinceStart
            | (this.child != null && (!this.child.canInterruptNow() || this.child.requiresReconciliationAfterEmergency()));
         if (!this.movedSinceStart && this.init_c_DataUtil.isExpired()) {
            if (this.child != null) {
               this.child.cancel();
            }

            return AiOperation.Result.failed(AiFailure.TIMEOUT, "Construction planning reached its ten-second limit", "{}");
         } else {
            if (System.nanoTime() >= this.deadline) {
               return this.load(AiOperation.Status.TIMED_OUT, "Placement approach deadline");
            }

            if (this.child != null
               && this.init_BlockPos != null
               && !this.draft
                  .init_Z_u_CachePool(
                     this.draft.targetRequests.stream().filter(target -> target.pos().equals(this.init_BlockPos)).findFirst().orElseThrow()
                  )
               && !this.pending.getAsBoolean()
               && this.child.canInterruptNow()
               && !this.child.requiresReconciliationAfterEmergency()) {
               this.child.cancel();
               this.child = null;
               this.init_BlockPos = null;
            }

            if (this.child == null) {
               this.draft.planSlice(2000000L);
               if (!this.draft.isFinished()) {
                  return init(AiOperation.Status.RUNNING, "Planning reusable construction batch (" + this.draft.clickCheckCount + " click checks)");
               }

               if (this.draft.init_Z_Vec3(mc.player.position())) {
                  return init(AiOperation.Status.RUNNING, "Updating the itinerary from the actual handoff position");
               }

               if (this.draft.unplacedTargets().isEmpty()) {
                  return this.load(AiOperation.Status.SUCCEEDED, "Prepared batch already satisfied; reobserving");
               }

               AiSchematicBatchPlan.Inner_ConfigStore scheduled = this.draft.init_c_ConfigStore();
               if (scheduled != null) {
                  Map<Long, PlacementGoal.ClickGeometry> proofs = this.draft.init_Map_c_ConfigStore(scheduled, this.keepCells, this.avoidCells);
                  if (!proofs.isEmpty()) {
                     AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil station = this.draft.update.get(scheduled.station());
                     PlacementGoal.PlacementRequest next = scheduled.jobs()
                        .stream()
                        .map(i -> this.draft.targetRequests.get(i))
                        .filter(this.draft::init_Z_u_CachePool)
                        .findFirst()
                        .orElseThrow();
                     PlacementGoal goal = PlacementGoal.init(
                           List.of(next),
                           mc.player.blockInteractionRange() - 0.25,
                           this.draft.eyeHeight,
                           this.draft.volume,
                           this.keepCells,
                           this.avoidCells
                        )
                        .init(proofs);
                     boolean edits = !station.probe.walk;
                     int seconds = Math.max(1, (int)Math.ceil((this.deadline - System.nanoTime()) / 1.0E9));
                     AiGoto.GotoOptions options = new AiGoto.GotoOptions(true, this.look == null, 3, false, true, true);
                     this.child = AiGoto.travelToCompat(
                        station.probe.feet,
                        0.6,
                        edits,
                        edits,
                        edits,
                        seconds,
                        AiPathfinder.RouteStrategy.FASTEST,
                        true,
                        2.0,
                        edits ? goal : goal.clientThreadProofsOnly(),
                        options
                     );
                     this.init_BlockPos = next.pos();
                     AiWalkingWork.init(this.child, this.look, this.click);
                  } else {
                     this.draft.rejected = this.draft.update.get(scheduled.station()).probe.feet;
                     this.draft.update();
                  }
               }

               if (this.child == null) {
                  if (!this.draft.read_Z && this.draft.read_List.isEmpty() && this.draft.write_List.isEmpty()) {
                     return AiOperation.Result.failed(AiFailure.PLACEMENT_IMPOSSIBLE, "No proved workstation for the ready batch", "{}");
                  }

                  return AiOperation.Result.failed(AiFailure.TIMEOUT, "Batch planning needs refreshed views before committing a route", "{}");
               }
            }

            AiOperation.Result r = this.child.tick();
            if (r.terminal()
               && r.status() != AiOperation.Status.SUCCEEDED
               && r.status() != AiOperation.Status.CANCELLED
               && r.status() != AiOperation.Status.UNCERTAIN) {
               AiSchematicBatchPlan.Inner_ConfigStore failed = this.draft.init_c_ConfigStore();
               if (failed != null) {
                  this.draft.rejected = this.draft.update.get(failed.station()).probe.feet;
               }

               this.child.cancel();
               this.child = null;
               this.draft.update();
               return AiOperation.Result.failed(AiFailure.TIMEOUT, "Batch planning route needs another workstation: " + r.message(), r.dataJson());
            } else {
               return r;
            }
         }
      } else {
         return this.load(AiOperation.Status.CANCELLED, "Placement approach cancelled");
      }
   }

   private static AiOperation.Result init(AiOperation.Status status, String message) {
      return new AiOperation.Result(status, message, "{}", List.of());
   }

   private AiOperation.Result load(AiOperation.Status status, String message) {
      if (this.child != null) {
         this.child.cancel();
      }

      return init(status, message);
   }

   @Override
   public boolean canInterruptNow() {
      return !this.pending.getAsBoolean() && (this.child == null || this.child.canInterruptNow());
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.pending.getAsBoolean() || this.child != null && this.child.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      this.load_Z = true;
      this.draft.cancel();
      if (this.child != null) {
         this.child.cancel();
      }
   }

   static boolean init(VoxelShape shape) {
      return AiSchematicApproach.Inner_DataUtil.init_Z_VoxelShape(shape);
   }

   static LevelReader init(LevelReader world, Set<BlockPos> clear) {
      return AiSchematicApproach.Inner_DataUtil.simulatedView(world, clear);
   }

   static boolean init(BlockGetter world, Map<BlockPos, BlockState> expected) {
      for (Entry<BlockPos, BlockState> entry : expected.entrySet()) {
         if (world.getBlockState(entry.getKey()) != entry.getValue()) {
            return false;
         }
      }

      return true;
   }

   static void init(BlockGetter world, Vec3 eye, Vec3 end, Map<BlockPos, BlockState> observed) {
      BlockGetter.traverseBlocks(eye, end, observed, (states, cell) -> {
         states.put(cell.immutable(), world.getBlockState(cell));
         return null;
      }, states -> null);
   }

   public static final class Inner_DataUtil {
      private final Minecraft mc = Minecraft.getInstance();
      private final ClientLevel level;
      private final List<PlacementGoal.PlacementRequest> targetRequests;
      private final PlacementGoal placementGoal;
      private final AABB volume;
      private final List<BlockPos> load_List;
      private final List<AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil> read_List = new ArrayList<>();
      private final List<AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil> write_List = new ArrayList<>();
      private final Set<BlockPos> targets;
      private final Set<BlockPos> load_Set = new HashSet<>();
      private final double eyeHeight;
      private final AiSchematicBatchPlan.Inner_DataUtil planningBudget = new AiSchematicBatchPlan.Inner_DataUtil(System::nanoTime, 8000000000L);
      private int init_I;
      private int clickCheckCount;
      private AiSchematicApproach.Inner_DataUtil.Inner_DataUtil_2 activeProbe;
      private boolean init_Z;
      private boolean load_Z;
      private boolean read_Z;
      private final BlockPos continuation;
      private AiSchematicBatchPlan.Inner_CachePool init_c_CachePool;
      private AiSchematicBatchPlan.Inner_StreamUtil batchChoice;
      private List<AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil> update = List.of();
      private BlockPos rejected;
      private Vec3 worker;
      private Set<Long> read_Set = Set.of();
      private AiSchematicBatchPlan.Inner_DataUtil load_c_DataUtil;
      private Vec3 workerPosition;
      private boolean write_Z;

      public Inner_DataUtil(List<PlacementGoal.PlacementRequest> targets, AABB volume, Set<Long> keep, Set<Long> avoid) {
         this(targets, volume, keep, avoid, null);
      }

      public Inner_DataUtil(List<PlacementGoal.PlacementRequest> targets, AABB volume, Set<Long> keep, Set<Long> avoid, BlockPos continuation) {
         if (AutismLiteVariant.enabled()) {
            throw new IllegalStateException();
         }

         if (!targets.isEmpty() && targets.size() <= 32) {
            this.targetRequests = List.copyOf(targets);
            this.volume = volume;
            this.continuation = continuation;
            this.level = this.mc.level;
            this.targets = targets.stream().map(PlacementGoal.PlacementRequest::pos).collect(Collectors.toUnmodifiableSet());
            this.eyeHeight = this.mc.player.getEyeHeight(Pose.STANDING);
            if (this.mc.player.onGround()) {
               this.load_Set.add(AiStance.init(this.mc));
            }

            this.placementGoal = PlacementGoal.init(targets, this.mc.player.blockInteractionRange() - 0.25, this.eyeHeight, volume, keep, avoid);
            Set<BlockPos> unique = new HashSet<>();
            int radius = (int)Math.ceil(this.mc.player.blockInteractionRange());

            for (PlacementGoal.PlacementRequest target : targets) {
               for (int dx = -radius; dx <= radius; dx++) {
                  for (int dy = -radius - 1; dy <= radius - 1; dy++) {
                     for (int dz = -radius; dz <= radius; dz++) {
                        BlockPos feet = target.pos().offset(dx, dy, dz);
                        if (!avoid.contains(feet.asLong())
                           && feet.getY() > this.level.getMinY()
                           && feet.getY() <= this.level.getMaxY()
                           && Vec3.atCenterOf(target.pos()).distanceToSqr(Vec3.atBottomCenterOf(feet).add(0.0, this.eyeHeight, 0.0))
                              <= Math.pow(this.mc.player.blockInteractionRange() + 0.9, 2.0)) {
                           unique.add(feet);
                        }
                     }
                  }
               }
            }

            this.load_List = new ArrayList<>(unique);
            this.load_List
               .sort(
                  Comparator.<BlockPos>comparingDouble(feetx -> feetx.distToCenterSqr(this.mc.player.position()))
                     .thenComparingInt(Vec3i::getY)
                     .thenComparingInt(Vec3i::getX)
                     .thenComparingInt(Vec3i::getZ)
               );
         } else {
            throw new IllegalArgumentException("Invalid bounded batch");
         }
      }

      public boolean isFinished() {
         return this.init_Z || this.load_Z;
      }

      public boolean covers(BlockPos target) {
         return !this.load_Z && this.targets.contains(target);
      }

      public Set<BlockPos> getTargets() {
         return this.targets;
      }

      public void cancel() {
         this.load_Z = true;
         this.activeProbe = null;
         this.planningBudget.start();
         if (this.load_c_DataUtil != null) {
            this.load_c_DataUtil.start();
         }
      }

      public void load_V() {
         this.planningBudget.init(5000000000L);
      }

      public double elapsedMs() {
         return this.planningBudget.elapsedMs() + (this.load_c_DataUtil == null ? 0.0 : this.load_c_DataUtil.elapsedMs());
      }

      public BlockPos getRejected() {
         return this.rejected;
      }

      public int stopCount() {
         return this.batchChoice == null ? 0 : this.batchChoice.stops().size();
      }

      public Vec3 endPosition() {
         return this.batchChoice != null && !this.batchChoice.stops().isEmpty()
            ? this.update.get(this.batchChoice.stops().getLast().station()).probe.position
            : this.mc.player.position();
      }

      public void setWorkerPosition(Vec3 worker) {
         if (this.init_c_CachePool == null) {
            this.worker = worker;
         }
      }

      public void init_V_Set(Set<Long> cells) {
         this.read_Set = Collections.unmodifiableSet(cells);
      }

      public static List<PlacementGoal.PlacementRequest> orderByRoute(List<PlacementGoal.PlacementRequest> candidates, Vec3 start) {
         return AiSchematicBatchPlan.init(candidates, start, PlacementGoal.PlacementRequest::pos);
      }

      public AiOperation operation(int seconds, Set<Long> keep, Set<Long> avoid) {
         return new AiSchematicApproach(this, seconds, keep, avoid);
      }

      private boolean init_Z_Vec3(Vec3 worker) {
         if (this.write_Z) {
            return false;
         } else {
            this.write_Z = true;
            if (this.workerPosition != null && !(this.workerPosition.distanceToSqr(worker) <= 2.25) && this.batchChoice != null) {
               long remaining = Math.max(1L, (long)((10000.0 - this.elapsedMs()) * 1000000.0));
               this.load_c_DataUtil = new AiSchematicBatchPlan.Inner_DataUtil(System::nanoTime, Math.min(2000000000L, remaining));
               this.init_c_CachePool = null;
               this.batchChoice = null;
               this.init_Z = this.read_Z = false;
               this.worker = worker;
               this.write();
               return true;
            } else {
               return false;
            }
         }
      }

      public void planSlice(long sliceNanos) {
         if (!this.isFinished()) {
            if (this.mc.level == this.level && this.mc.player != null) {
               long stop = System.nanoTime() + Math.max(1L, Math.min(2000000L, sliceNanos));
               int units = 0;

               while (units++ < 256 && System.nanoTime() < stop) {
                  if (this.init_c_CachePool != null) {
                     AiSchematicBatchPlan.Inner_DataUtil activeBudget = this.load_c_DataUtil == null ? this.planningBudget : this.load_c_DataUtil;
                     if (activeBudget.isExpired() || this.init_c_CachePool.isDone()) {
                        this.read_Z = this.read_Z | activeBudget.isExpired();
                        this.read();
                        return;
                     }

                     this.init_c_CachePool.step();
                  } else if (this.planningBudget.remainingNanos() <= 1000000000L) {
                     this.write();
                  } else {
                     if (this.activeProbe == null) {
                        if (this.init_I >= this.load_List.size()) {
                           this.write();
                           continue;
                        }

                        this.activeProbe = this.probeStation(this.load_List.get(this.init_I++));
                        if (this.activeProbe == null) {
                           continue;
                        }
                     }

                     if (this.activeProbe.load_I >= this.targetRequests.size()) {
                        this.init_V_b_DataUtil_DataUti(this.activeProbe);
                        this.activeProbe = null;
                        if (this.load_Z()) {
                           this.write();
                        }
                     } else {
                        this.init_V_b_DataUtil_DataUti_I(this.activeProbe, this.activeProbe.load_I++);
                        this.clickCheckCount++;
                     }
                  }
               }
            } else {
               this.cancel();
            }
         }
      }

      private void read() {
         if (this.init_c_CachePool != null) {
            this.batchChoice = this.init_c_CachePool.getChoice();
         }

         this.init_Z = true;
         this.planningBudget.start();
         if (this.load_c_DataUtil != null) {
            this.load_c_DataUtil.start();
         }

         AutismClientAddon.LOG
            .info(
               "[AI] batch plan: {} targets, {} checks, {} walking / {} construction workstations, {} stops / {} covered, {} ms{}",
               this.targetRequests.size(),
               this.clickCheckCount,
               this.read_List.size(),
               this.write_List.size(),
               this.batchChoice == null ? 0 : this.batchChoice.stops().size(),
               this.batchChoice == null ? 0 : Long.bitCount(this.batchChoice.covered()),
               Math.round(this.elapsedMs()),
               this.read_Z ? " (budget)" : ""
            );
      }

      private void write() {
         if (this.activeProbe != null) {
            this.init_V_b_DataUtil_DataUti(this.activeProbe);
            this.activeProbe = null;
         }

         this.update = new ArrayList<>(this.read_List);
         this.update.addAll(this.write_List);
         long available = 0L;

         for (int i = 0; i < this.targetRequests.size(); i++) {
            if (this.init_Z_u_CachePool(this.targetRequests.get(i))) {
               available |= 1L << i;
            }
         }

         long completed = (1L << this.targetRequests.size()) - 1L & ~available;
         ArrayList<AiSchematicBatchPlan.Inner_BufferHelper> inputs = new ArrayList<>();

         for (AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil station : this.update) {
            long jobs = 0L;

            for (int i : station.probe.init_Map.keySet()) {
               if ((station.blockers[i] & completed) == 0L) {
                  jobs |= 1L << i;
               }
            }

            inputs.add(
               new AiSchematicBatchPlan.Inner_BufferHelper(
                  station.probe.feet,
                  jobs & available,
                  station.blockers,
                  station.supports,
                  station.probe.clearCells.size(),
                  station.probe.foundationJob < 0 ? 0L : 1L << station.probe.foundationJob & available
               )
            );
         }

         this.workerPosition = this.worker == null ? this.mc.player.position() : this.worker;
         this.init_c_CachePool = new AiSchematicBatchPlan.Inner_CachePool(inputs, this.workerPosition, this.continuation, this.targetRequests.size(), available);
      }

      private boolean load_Z() {
         if (this.read_List.size() < Math.min(12, this.targetRequests.size() * 2)) {
            return false;
         }

         long covered = 0L;
         long needed = 0L;

         for (int i = 0; i < this.targetRequests.size(); i++) {
            if (this.init_Z_u_CachePool(this.targetRequests.get(i))) {
               needed |= 1L << i;
            }
         }

         for (AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil station : this.read_List) {
            if (station.probe.foundationJob < 0) {
               for (int job : station.probe.init_Map.keySet()) {
                  covered |= 1L << job;
               }
            }
         }

         return (needed & ~covered) == 0L;
      }

      private AiSchematicApproach.Inner_DataUtil.Inner_DataUtil_2 probeStation(BlockPos feet) {
         if (this.level.getChunkSource().getChunk(feet.getX() >> 4, feet.getZ() >> 4, ChunkStatus.FULL, false) == null) {
            return null;
         }

         BlockState floor = this.level.getBlockState(feet.below());
         if (AiPathfinder.LiveGridView.isFloorHazardCompat(floor)) {
            return null;
         }

         for (BlockPos occupied : List.of(feet, feet.above())) {
            BlockState state = this.level.getBlockState(occupied);
            if (AiPathfinder.LiveGridView.isFeetHazardCompat(state) || AiPathfinder.LiveGridView.isFloorHazardCompat(state) || state.getFluidState().is(FluidTags.LAVA)) {
               return null;
            }
         }

         VoxelShape floorShape = floor.getCollisionShape(this.level, feet.below());
         boolean supported = !floorShape.isEmpty();
         int foundationJob = -1;
         BlockState futureFloor = null;
         if (!supported && floor.canBeReplaced() && floor.getFluidState().isEmpty()) {
            for (int i = 0; i < this.targetRequests.size(); i++) {
               PlacementGoal.PlacementRequest source = this.targetRequests.get(i);
               if (source.pos().equals(feet.below())
                  && source.requested() != null
                  && this.init_Z_u_CachePool(source)
                  && !(source.requested().getBlock() instanceof FallingBlock)
                  && !AiPathfinder.LiveGridView.isFloorHazardCompat(source.requested())
                  && Block.isShapeFullBlock(source.requested().getCollisionShape(this.level, source.pos()))) {
                  foundationJob = i;
                  futureFloor = source.requested();
                  floorShape = futureFloor.getCollisionShape(this.level, source.pos());
                  supported = true;
                  break;
               }
            }
         }

         if (supported && !init_Z_VoxelShape(floorShape)) {
            return null;
         }

         if (supported || floor.canBeReplaced() && floor.getFluidState().isEmpty() && this.placementGoal.read(feet.below())) {
            double top = supported ? floorShape.max(Axis.Y) : 1.0;
            Vec3 position = Vec3.atBottomCenterOf(feet).add(0.0, top - 1.0, 0.0);
            AABB body = new AABB(position.x - 0.3, position.y, position.z - 0.3, position.x + 0.3, position.y + 1.8, position.z + 0.3);
            Set<BlockPos> clear = new HashSet<>();
            if (!this.level.noCollision(this.mc.player, body)) {
               if (!this.level.getEntityCollisions(this.mc.player, body).isEmpty()) {
                  return null;
               }

               for (BlockPos cell : BlockPos.betweenClosed(
                  BlockPos.containing(body.minX + 0.001, body.minY + 0.001, body.minZ + 0.001),
                  BlockPos.containing(body.maxX - 0.001, body.maxY - 0.001, body.maxZ - 0.001)
               )) {
                  BlockState state = this.level.getBlockState(cell);
                  boolean obstructs = false;

                  for (AABB box : state.getCollisionShape(this.level, cell).toAabbs()) {
                     if (box.move(cell).intersects(body.deflate(1.0E-4))) {
                        obstructs = true;
                        break;
                     }
                  }

                  if (obstructs) {
                     if (!this.placementGoal.load(cell)
                        || state.hasBlockEntity()
                        || !state.getFluidState().isEmpty()
                        || state.getDestroySpeed(this.level, cell) < 0.0F
                        || AiPathfinder.LiveGridView.isFeetHazardCompat(state)
                        || AiPathfinder.LiveGridView.isFloorHazardCompat(state)) {
                        return null;
                     }

                     clear.add(cell.immutable());
                  }
               }

               if (clear.isEmpty()) {
                  return null;
               }
            }

            LevelReader view = clear.isEmpty() && futureFloor == null
               ? null
               : simulatedView(this.level, Set.copyOf(clear), futureFloor == null ? Map.of() : Map.of(feet.below(), futureFloor));
            if (supported && foundationJob < 0 && clear.isEmpty()) {
               this.load_Set.add(feet);
            }

            return new AiSchematicApproach.Inner_DataUtil.Inner_DataUtil_2(
               feet, position, body, Set.copyOf(clear), view, supported, supported && clear.isEmpty(), top, foundationJob
            );
         } else {
            return null;
         }
      }

      private void init_V_b_DataUtil_DataUti_I(AiSchematicApproach.Inner_DataUtil.Inner_DataUtil_2 probe, int targetIndex) {
         if (targetIndex != probe.foundationJob) {
            PlacementGoal.PlacementRequest target = this.targetRequests.get(targetIndex);
            if (this.init_Z_u_CachePool(target)
               && (
                  target.requested() == null
                     || target.requested().canSurvive(probe.view == null ? this.level : probe.view, target.pos())
               )
               && !(
                  probe.position.add(0.0, this.eyeHeight, 0.0).distanceToSqr(Vec3.atCenterOf(target.pos()))
                     > Math.pow(this.mc.player.blockInteractionRange() + 0.9, 2.0)
               )) {
               BlockPlacementPlanner.PlacementPlan hit = probe.view == null
                  ? PlacementGoal.init(this.mc, target.pos(), target.item(), target.constraint(), probe.position, 32)
                  : this.init(target, probe.position, probe.body, probe.clearCells, probe.view);
               if (hit != null) {
                  double height = PlacementGoal.init(this.mc, target.pos(), target.item(), target.constraint(), true);
                  if (probe.supported
                     || !new AABB(probe.feet.below()).clip(probe.position.add(0.0, height, 0.0), hit.hit().getLocation()).isPresent()
                     )
                   {
                     probe.init_Map
                        .put(
                           targetIndex,
                           new PlacementGoal.ClickGeometry(
                              hit.hit().getBlockPos(),
                              hit.hit().getLocation(),
                              hit.hit().getDirection(),
                              probe.top - 1.0,
                              height + probe.top - 1.0
                           )
                        );
                  }
               }
            }
         }
      }

      private void init_V_b_DataUtil_DataUti(AiSchematicApproach.Inner_DataUtil.Inner_DataUtil_2 probe) {
         if (!probe.init_Map.isEmpty()) {
            long available = 0L;
            long[] before = new long[this.targetRequests.size()];
            long[] blockers = new long[this.targetRequests.size()];

            for (int i : probe.init_Map.keySet()) {
               available |= 1L << i;
            }

            for (int i = 0; i < this.targetRequests.size(); i++) {
               PlacementGoal.PlacementRequest target = this.targetRequests.get(i);
               if (target.requested() != null) {
                  VoxelShape shape = target.requested().getShape(this.level, target.pos());

                  for (int j : probe.init_Map.keySet()) {
                     if (i != j) {
                        PlacementGoal.ClickGeometry proof = probe.init_Map.get(j);
                        if (!proof.support().equals(target.pos())) {
                           Vec3 eye = Vec3.atBottomCenterOf(probe.feet).add(0.0, proof.eyeHeight(), 0.0);

                           for (AABB box : shape.toAabbs()) {
                              if (box.move(target.pos()).clip(eye, proof.point()).isPresent() || box.move(target.pos()).intersects(probe.body)) {
                                 before[i] |= 1L << j;
                                 blockers[j] |= 1L << i;
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
            }

            List<Integer> order = AiSchematicBatchPlan.init(available, before);
            HashMap<BlockPos, BlockState> observed = new HashMap<>();
            observed.put(probe.feet.below(), this.level.getBlockState(probe.feet.below()));

            for (int i : probe.init_Map.keySet()) {
               PlacementGoal.ClickGeometry proof = probe.init_Map.get(i);
               BlockPos support = proof.support();
               observed.put(support, this.level.getBlockState(support));
               Vec3 eye = Vec3.atBottomCenterOf(probe.feet).add(0.0, proof.eyeHeight(), 0.0);
               AiSchematicApproach.init(
                  this.level,
                  eye,
                  proof.point().add(-proof.face().getStepX() * 0.001, -proof.face().getStepY() * 0.001, -proof.face().getStepZ() * 0.001),
                  observed
               );
            }

            for (BlockPos clear : probe.clearCells) {
               observed.put(clear, this.level.getBlockState(clear));
            }

            int supports = probe.supported
               ? 0
               : Math.max(
                  1,
                  this.load_Set
                     .stream()
                     .mapToInt(
                        anchor -> Math.abs(anchor.getX() - probe.feet.getX())
                           + Math.abs(anchor.getZ() - probe.feet.getZ())
                           + Math.max(0, probe.feet.getY() - anchor.getY())
                     )
                     .min()
                     .orElse(1)
               );
            AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil station = new AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil(probe, order, blockers, Map.copyOf(observed), supports);
            List<AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil> list = probe.walk ? this.read_List : this.write_List;
            list.add(station);
            list.sort(
               Comparator.<AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil>comparingDouble(s -> s.score(this.mc.player.position()))
                  .thenComparingLong(s -> s.probe.feet.asLong())
            );
            if (list.size() > 64) {
               LinkedHashSet<AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil> retained = new LinkedHashSet<>(list.subList(0, 24));

               for (int i = 0; i < this.targetRequests.size(); i++) {
                  int job = i;
                  list.stream()
                     .filter(s -> s.probe.init_Map.containsKey(job))
                     .min(Comparator.comparingDouble(s -> s.score(this.mc.player.position()) * s.order.size()))
                     .ifPresent(retained::add);
               }

               list.stream()
                  .sorted(Comparator.comparingDouble(s -> s.probe.feet.distToCenterSqr(this.mc.player.position())))
                  .limit(8L)
                  .forEach(retained::add);

               for (AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil s : list) {
                  if (retained.size() < 64) {
                     retained.add(s);
                  }
               }

               list.clear();
               list.addAll(retained);
            }
         }
      }

      private boolean init_Z_u_CachePool(PlacementGoal.PlacementRequest target) {
         BlockState live = this.level.getBlockState(target.pos());
         return live.canBeReplaced() && live.getFluidState().isEmpty();
      }

      private List<PlacementGoal.PlacementRequest> unplacedTargets() {
         return this.targetRequests.stream().filter(this::init_Z_u_CachePool).toList();
      }

      private boolean init_Z_b_DataUtil_StreamU(AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil station, Set<Long> keep, Set<Long> avoid) {
         if (avoid.contains(station.probe.feet.asLong())) {
            return false;
         }

         if (!AiSchematicApproach.init(this.level, station.observed)) {
            return false;
         }

         for (BlockPos cell : station.probe.clearCells) {
            if (keep.contains(cell.asLong())) {
               return false;
            }
         }

         for (BlockPos cell : BlockPos.betweenClosed(
            BlockPos.containing(station.probe.body.minX + 0.001, station.probe.body.minY + 0.001, station.probe.body.minZ + 0.001),
            BlockPos.containing(station.probe.body.maxX - 0.001, station.probe.body.maxY - 0.001, station.probe.body.maxZ - 0.001)
         )) {
            if (!station.probe.clearCells.contains(cell)) {
               BlockState state = this.level.getBlockState(cell);
               if (AiPathfinder.LiveGridView.isFeetHazardCompat(state) || AiPathfinder.LiveGridView.isFloorHazardCompat(state)) {
                  return false;
               }

               for (AABB box : state.getCollisionShape(this.level, cell).toAabbs()) {
                  if (box.move(cell).intersects(station.probe.body.deflate(1.0E-4))) {
                     return false;
                  }
               }
            }
         }

         return station.order.stream().anyMatch(i -> this.init_Z_u_CachePool(this.targetRequests.get(i)))
            && this.level.getEntityCollisions(this.mc.player, station.probe.body).isEmpty();
      }

      private Map<Long, PlacementGoal.ClickGeometry> init_Map_Z(boolean edits, boolean preferred, Set<Long> keep, Set<Long> avoid) {
         ArrayList<AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil> stations = new ArrayList<>(this.read_List);
         if (edits) {
            stations.addAll(this.write_List);
         }

         stations.removeIf(stationx -> !this.init_Z_b_DataUtil_StreamU(stationx, keep, avoid));
         stations.sort(
            Comparator.<AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil>comparingDouble(s -> s.score(this.mc.player.position()))
               .thenComparingLong(s -> s.probe.feet.asLong())
         );
         LinkedHashMap<Long, PlacementGoal.ClickGeometry> result = new LinkedHashMap<>();

         for (AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil station : stations) {
            if (preferred && result.size() >= 4) {
               break;
            }

            for (int i : station.order) {
               if (this.init_Z_u_CachePool(this.targetRequests.get(i))) {
                  result.put(station.probe.feet.asLong(), station.probe.init_Map.get(i));
                  break;
               }
            }
         }

         return result;
      }

      public List<BlockPos> positionsFrom(BlockPos feet) {
         AiSchematicBatchPlan.Inner_ConfigStore stop = this.init_c_ConfigStore();
         if (stop != null && this.update.get(stop.station()).probe.feet.equals(feet)) {
            return this.batchChoice
               .order()
               .stream()
               .filter(i -> this.init_Z_u_CachePool(this.targetRequests.get(i)))
               .map(i -> this.targetRequests.get(i).pos())
               .toList();
         }

         this.update();
         return List.of();
      }

      public double estimatedSeconds(BlockPos feet) {
         if (this.batchChoice != null) {
            return this.batchChoice.ticks() / 20.0;
         }

         for (List<AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil> list : List.of(this.read_List, this.write_List)) {
            for (AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil station : list) {
               if (station.probe.feet.equals(feet)) {
                  return AiSchematicBatchPlan.load(feet, this.mc.player.position(), station.order.size(), station.supports, station.probe.clearCells.size());
               }
            }
         }

         return 0.0;
      }

      public int supportCount(BlockPos feet) {
         if (this.batchChoice != null) {
            return this.batchChoice.supports();
         }

         for (List<AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil> list : List.of(this.read_List, this.write_List)) {
            for (AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil station : list) {
               if (station.probe.feet.equals(feet)) {
                  return station.supports;
               }
            }
         }

         return 0;
      }

      private AiSchematicBatchPlan.Inner_ConfigStore init_c_ConfigStore() {
         if (this.batchChoice == null) {
            return null;
         }

         long completed = 0L;

         for (int i = 0; i < this.targetRequests.size(); i++) {
            if (!this.init_Z_u_CachePool(this.targetRequests.get(i))) {
               completed |= 1L << i;
            }
         }

         return this.batchChoice.nextStop(completed);
      }

      private void update() {
         this.batchChoice = null;
      }

      public boolean load(BlockPos target) {
         return !this.load_Z
            && this.mc.level == this.level
            && this.batchChoice != null
            && this.init_c_ConfigStore() != null
            && this.batchChoice
               .order()
               .stream()
               .anyMatch(i -> this.targetRequests.get(i).pos().equals(target) && this.init_Z_u_CachePool(this.targetRequests.get(i)));
      }

      public boolean read(BlockPos pos) {
         if (this.batchChoice != null && !this.load_Z) {
            int placed = -1;

            for (int i = 0; i < this.targetRequests.size(); i++) {
               if (this.targetRequests.get(i).pos().equals(pos)) {
                  placed = i;
                  break;
               }
            }

            if (placed >= 0 && this.init_Z_u_CachePool(this.targetRequests.get(placed))) {
               long bit = 1L << placed;

               for (AiSchematicBatchPlan.Inner_ConfigStore stop : this.batchChoice.stops()) {
                  AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil station = this.update.get(stop.station());

                  for (int job : stop.jobs()) {
                     if (job != placed && this.init_Z_u_CachePool(this.targetRequests.get(job)) && (station.blockers[job] & bit) != 0L) {
                        return false;
                     }
                  }
               }

               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private Map<Long, PlacementGoal.ClickGeometry> init_Map_c_ConfigStore(AiSchematicBatchPlan.Inner_ConfigStore stop, Set<Long> keep, Set<Long> avoid) {
         AiSchematicApproach.Inner_DataUtil.Inner_StreamUtil station = this.update.get(stop.station());
         if (avoid.contains(station.probe.feet.asLong())) {
            return Map.of();
         }

         for (Entry<BlockPos, BlockState> entry : station.observed.entrySet()) {
            BlockPos cell = entry.getKey();
            BlockState live = this.level.getBlockState(cell);
            if (live != entry.getValue()
               && !this.targets.contains(cell)
               && !this.read_Set.contains(cell.asLong())
               && (!station.probe.clearCells.contains(cell) || !live.isAir())
               && (!cell.equals(station.probe.feet.below()) || AiScaffold.recordedBlock(cell) == null)) {
               return Map.of();
            }
         }

         AiSchematicApproach.Inner_DataUtil.Inner_DataUtil_2 fresh = this.probeStation(station.probe.feet);
         if (fresh != null
            && !(Math.abs(fresh.top - station.probe.top) > 0.001)
            && !fresh.clearCells.stream().anyMatch(cellx -> keep.contains(cellx.asLong()))) {
            PlacementGoal.ClickGeometry first = null;

            for (int i : stop.jobs()) {
               if (this.init_Z_u_CachePool(this.targetRequests.get(i))) {
                  this.init_V_b_DataUtil_DataUti_I(fresh, i);
                  PlacementGoal.ClickGeometry proof = fresh.init_Map.get(i);
                  if (proof == null) {
                     return Map.of();
                  }

                  if (first == null) {
                     first = proof;
                  }
               }
            }

            return first == null ? Map.of() : Map.of(fresh.feet.asLong(), first);
         } else {
            return Map.of();
         }
      }

      public static <T> void fillBatch(List<T> source, int cursor, List<T> batch) {
         AiSchematicBatchPlan.init(source, cursor, batch);
      }

      public static <T> Set<Long> batchKeepCells(List<T> source, int cursor, Set<Long> existing, ToLongFunction<T> position) {
         return AiSchematicBatchPlan.init(source, cursor, existing, position);
      }

      static boolean init_Z_VoxelShape(VoxelShape shape) {
         if (shape.isEmpty()) {
            return false;
         }

         double top = shape.max(Axis.Y);

         for (AABB box : shape.toAabbs()) {
            if (Math.abs(box.maxY - top) < 1.0E-5
               && Math.min(0.8, box.maxX) - Math.max(0.2, box.minX) >= 0.15
               && Math.min(0.8, box.maxZ) - Math.max(0.2, box.minZ) >= 0.15) {
               return true;
            }
         }

         return false;
      }

      private BlockPlacementPlanner.PlacementPlan init(PlacementGoal.PlacementRequest target, Vec3 position, AABB body, Set<BlockPos> clear, LevelReader view) {
         PlacementGoal.PlaceConstraint constraint = target.constraint();
         if (target.requested() != null && !target.requested().canSurvive(view, target.pos())) {
            return null;
         }

         if (constraint != null && constraint.placedShape() != null) {
            for (AABB box : AiShapes.boxes(constraint.placedShape())) {
               if (box.move(target.pos()).intersects(body)) {
                  return null;
               }
            }
         }

         double height = PlacementGoal.init(this.mc, target.pos(), target.item(), constraint, true);
         Vec3 eye = position.add(0.0, height, 0.0);
         double reach = this.mc.player.blockInteractionRange() - 0.25;
         BlockPlacementPlanner.HitValidator placement = (hitx, cell) -> !clear.contains(hitx.getBlockPos())
            && PlacementGoal.init(this.mc, cell, target.item(), constraint, eye, body, hitx);
         BlockPlacementPlanner.Query request = new BlockPlacementPlanner.Query(target.pos(), eye, reach, placement).withAutoSneak(true);
         if (constraint != null) {
            EnumSet<Direction> faces = EnumSet.noneOf(Direction.class);

            for (Direction face : Direction.values()) {
               if (constraint.acceptsFace(face)) {
                  faces.add(face);
               }
            }

            request.withAllowedFaces(faces);
            if (constraint.clickYaw() != null) {
               request.withFromRotation(new RotationMath.Rotation(constraint.clickYaw(), 0.0F));
            }
         }

         ArrayList<BlockPlacementPlanner.SupportCandidate> options = new ArrayList<>();
         BlockPlacementPlanner.collectCandidates(request, options);
         int rays = 0;

         for (BlockPlacementPlanner.SupportCandidate option : options) {
            if (!clear.contains(option.supportPos())) {
               AABB rect = constraint != null && constraint.highHalf() != null
                  ? PlacementGoal.clipToHalf(option.faceRect(), option.face(), option.supportPos().getY(), constraint.highHalf())
                  : option.faceRect();
               if (rect != null && constraint != null && constraint.crouchForLook()) {
                  rect = PlacementGoal.clipToEyeCone(rect, option.face(), eye);
               }

               if (rect != null) {
                  BlockPlacementPlanner.SupportCandidate clipped = new BlockPlacementPlanner.SupportCandidate(
                     option.targetCell(),
                     option.supportPos(),
                     option.face(),
                     option.placementForm(),
                     option.supportState(),
                     rect,
                     option.faceArea(),
                     option.edgeMarginRatio(),
                     option.requiresSneak(),
                     option.supportFace()
                  );
                  BlockPlacementPlanner.AimSample aim = BlockPlacementPlanner.computeAimSample(clipped, request, null);
                  if (aim != null) {
                     Vec3 end = eye.add(Vec3.directionFromRotation(aim.emittedRotation().pitch(), aim.emittedRotation().yaw()).scale(reach));
                     BlockHitResult hit = view.clip(
                        new ClipContext(eye, end, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, this.mc.player)
                     );
                     if (hit.getType() == Type.BLOCK
                        && placement.lands(hit, target.pos())
                        && placement.init(view.getBlockState(hit.getBlockPos()), hit.getBlockPos(), true)) {
                        return new BlockPlacementPlanner.PlacementPlan(clipped, aim, hit, false, 0.0);
                     }

                     if (++rays >= 32) {
                        break;
                     }
                  }
               }
            }
         }

         return null;
      }

      static LevelReader simulatedView(LevelReader world, Set<BlockPos> clear) {
         return simulatedView(world, clear, Map.of());
      }

      static LevelReader simulatedView(LevelReader world, Set<BlockPos> clear, Map<BlockPos, BlockState> placed) {
         return (LevelReader)Proxy.newProxyInstance(LevelReader.class.getClassLoader(), new Class[]{LevelReader.class}, (proxy, method, args) -> {
            if (args != null && args.length > 0 && placed.containsKey(args[0])) {
               BlockState state = placed.get(args[0]);
               if (method.getName().equals("getBlockState")) {
                  return state;
               }

               if (method.getName().equals("getFluidState")) {
                  return state.getFluidState();
               }

               if (method.getName().equals("getBlockEntity")) {
                  return null;
               }

               if (method.getName().equals("isEmptyBlock")) {
                  return state.isAir();
               }
            }

            if (args != null && args.length > 0 && clear.contains(args[0])) {
               if (method.getName().equals("getBlockState")) {
                  return Blocks.AIR.defaultBlockState();
               }

               if (method.getName().equals("getFluidState")) {
                  return Fluids.EMPTY.defaultFluidState();
               }

               if (method.getName().equals("getBlockEntity")) {
                  return null;
               }

               if (method.getName().equals("isEmptyBlock")) {
                  return true;
               }
            }

            if (method.isDefault()) {
               return InvocationHandler.invokeDefault(proxy, method, args);
            }

            try {
               return method.invoke(world, args);
            } catch (InvocationTargetException e) {
               throw e.getCause();
            }
         });
      }

      private static final class Inner_DataUtil_2 {
         final BlockPos feet;
         final Vec3 position;
         final AABB body;
         final Set<BlockPos> clearCells;
         final LevelReader view;
         final boolean supported;
         final boolean walk;
         final double top;
         final int foundationJob;
         final Map<Integer, PlacementGoal.ClickGeometry> init_Map = new LinkedHashMap<>();
         int load_I;

         Inner_DataUtil_2(
            BlockPos feet, Vec3 position, AABB body, Set<BlockPos> clear, LevelReader view, boolean supported, boolean walk, double top, int foundationJob
         ) {
            this.feet = feet;
            this.position = position;
            this.body = body;
            this.clearCells = clear;
            this.view = view;
            this.supported = supported;
            this.walk = walk;
            this.top = top;
            this.foundationJob = foundationJob;
         }
      }

      private record Inner_StreamUtil(
         AiSchematicApproach.Inner_DataUtil.Inner_DataUtil_2 probe, List<Integer> order, long[] blockers, Map<BlockPos, BlockState> observed, int supports
      ) {
         double score(Vec3 worker) {
            return AiSchematicBatchPlan.init(this.probe.feet, worker, this.order.size(), this.supports, this.probe.clearCells.size());
         }
      }
   }
}
