package autismclient.ai.a;

import autismclient.AutismClientAddon;
import autismclient.ai.A;
import autismclient.api.ai.AiOperation;
import autismclient.util.AutismLiteVariant;
import autismclient.util.U;
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

public final class b implements AiOperation {
   private final b.Inner_DataUtil init_b_DataUtil;
   private final long init_J;
   private final Set<Long> init_Set;
   private final Set<Long> load_Set;
   private final Vec3 init_Vec3;
   private final c.Inner_DataUtil init_c_DataUtil = new c.Inner_DataUtil(System::nanoTime, 10000000000L);
   private boolean init_Z;
   private AiOperation init_AiOperation;
   private boolean load_Z;
   private BlockPos init_BlockPos;
   private Function<Predicate<AABB>, Vec3> init_Function;
   private Runnable init_Runnable;
   private BooleanSupplier init_BooleanSupplier = () -> false;

   public void init(Function<Predicate<AABB>, Vec3> look, Runnable click, BooleanSupplier pending) {
      if (!AutismLiteVariant.enabled()) {
         this.init_Function = look;
         this.init_Runnable = click;
         this.init_BooleanSupplier = pending;
         if (this.init_AiOperation != null) {
            o.init(this.init_AiOperation, look, click);
         }
      }
   }

   b(List<u.Inner_CachePool> targets, int seconds, AABB volume, Set<Long> protectedCells, Set<Long> avoid) {
      this(new b.Inner_DataUtil(targets, volume, protectedCells, avoid), seconds, protectedCells, avoid);
   }

   private b(b.Inner_DataUtil draft, int seconds, Set<Long> keep, Set<Long> avoid) {
      this.init_b_DataUtil = draft;
      this.init_Set = Set.copyOf(keep);
      this.load_Set = Set.copyOf(avoid);
      this.init_Vec3 = Minecraft.getInstance().player.position();
      this.init_J = System.nanoTime() + Math.max(1, seconds) * 1000000000L;
      draft.load_V();
      AiMotionSafety.putAt_V();
   }

   @Override
   public AiOperation.Result tick() {
      if (AutismLiteVariant.enabled()) {
         throw new IllegalStateException();
      }

      Minecraft mc = Minecraft.getInstance();
      if (!this.load_Z && mc.player != null && mc.level == this.init_b_DataUtil.init_ClientLevel) {
         this.init_Z = this.init_Z | mc.player.position().distanceToSqr(this.init_Vec3) > 0.0625;
         this.init_Z = this.init_Z
            | (this.init_AiOperation != null && (!this.init_AiOperation.canInterruptNow() || this.init_AiOperation.requiresReconciliationAfterEmergency()));
         if (!this.init_Z && this.init_c_DataUtil.init_Z()) {
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }

            return AiOperation.Result.failed(A.prev, "Construction planning reached its ten-second limit", "{}");
         } else {
            if (System.nanoTime() >= this.init_J) {
               return this.load(AiOperation.Status.TIMED_OUT, "Placement approach deadline");
            }

            if (this.init_AiOperation != null
               && this.init_BlockPos != null
               && !this.init_b_DataUtil
                  .init_Z_u_CachePool(
                     this.init_b_DataUtil.init_List.stream().filter(target -> target.pos().equals(this.init_BlockPos)).findFirst().orElseThrow()
                  )
               && !this.init_BooleanSupplier.getAsBoolean()
               && this.init_AiOperation.canInterruptNow()
               && !this.init_AiOperation.requiresReconciliationAfterEmergency()) {
               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
               this.init_BlockPos = null;
            }

            if (this.init_AiOperation == null) {
               this.init_b_DataUtil.init_V_J(2000000L);
               if (!this.init_b_DataUtil.init_Z()) {
                  return init(AiOperation.Status.RUNNING, "Planning reusable construction batch (" + this.init_b_DataUtil.load_I + " click checks)");
               }

               if (this.init_b_DataUtil.init_Z_Vec3(mc.player.position())) {
                  return init(AiOperation.Status.RUNNING, "Updating the itinerary from the actual handoff position");
               }

               if (this.init_b_DataUtil.init_List().isEmpty()) {
                  return this.load(AiOperation.Status.SUCCEEDED, "Prepared batch already satisfied; reobserving");
               }

               c.Inner_ConfigStore scheduled = this.init_b_DataUtil.init_c_ConfigStore();
               if (scheduled != null) {
                  Map<Long, u.Inner_StreamUtil> proofs = this.init_b_DataUtil.init_Map_c_ConfigStore(scheduled, this.init_Set, this.load_Set);
                  if (!proofs.isEmpty()) {
                     b.Inner_DataUtil.Inner_StreamUtil station = this.init_b_DataUtil.update.get(scheduled.station());
                     u.Inner_CachePool next = scheduled.jobs()
                        .stream()
                        .map(i -> this.init_b_DataUtil.init_List.get(i))
                        .filter(this.init_b_DataUtil::init_Z_u_CachePool)
                        .findFirst()
                        .orElseThrow();
                     u goal = u.init(
                           List.of(next),
                           mc.player.blockInteractionRange() - 0.25,
                           this.init_b_DataUtil.init_D,
                           this.init_b_DataUtil.init_AABB,
                           this.init_Set,
                           this.load_Set
                        )
                        .init(proofs);
                     boolean edits = !station.probe.load_Z;
                     int seconds = Math.max(1, (int)Math.ceil((this.init_J - System.nanoTime()) / 1.0E9));
                     HashUtil.Inner_BufferHelper options = new HashUtil.Inner_BufferHelper(true, this.init_Function == null, 3, false, true, true);
                     this.init_AiOperation = HashUtil.init(
                        station.probe.init_BlockPos,
                        0.6,
                        edits,
                        edits,
                        edits,
                        seconds,
                        ClassCache.Inner_IoWorker.init,
                        true,
                        2.0,
                        edits ? goal : goal.init_u(),
                        options
                     );
                     this.init_BlockPos = next.pos();
                     o.init(this.init_AiOperation, this.init_Function, this.init_Runnable);
                  } else {
                     this.init_b_DataUtil.load_BlockPos = this.init_b_DataUtil.update.get(scheduled.station()).probe.init_BlockPos;
                     this.init_b_DataUtil.update();
                  }
               }

               if (this.init_AiOperation == null) {
                  if (!this.init_b_DataUtil.read_Z && this.init_b_DataUtil.read_List.isEmpty() && this.init_b_DataUtil.write_List.isEmpty()) {
                     return AiOperation.Result.failed(A.write, "No proved workstation for the ready batch", "{}");
                  }

                  return AiOperation.Result.failed(A.prev, "Batch planning needs refreshed views before committing a route", "{}");
               }
            }

            AiOperation.Result r = this.init_AiOperation.tick();
            if (r.terminal()
               && r.status() != AiOperation.Status.SUCCEEDED
               && r.status() != AiOperation.Status.CANCELLED
               && r.status() != AiOperation.Status.UNCERTAIN) {
               c.Inner_ConfigStore failed = this.init_b_DataUtil.init_c_ConfigStore();
               if (failed != null) {
                  this.init_b_DataUtil.load_BlockPos = this.init_b_DataUtil.update.get(failed.station()).probe.init_BlockPos;
               }

               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
               this.init_b_DataUtil.update();
               return AiOperation.Result.failed(A.prev, "Batch planning route needs another workstation: " + r.message(), r.dataJson());
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
      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
      }

      return init(status, message);
   }

   @Override
   public boolean canInterruptNow() {
      return !this.init_BooleanSupplier.getAsBoolean() && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow());
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_BooleanSupplier.getAsBoolean() || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      this.load_Z = true;
      this.init_b_DataUtil.init_V();
      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
      }
   }

   static boolean init(VoxelShape shape) {
      return b.Inner_DataUtil.init_Z_VoxelShape(shape);
   }

   static LevelReader init(LevelReader world, Set<BlockPos> clear) {
      return b.Inner_DataUtil.init_LevelReader_LevelReader_Set(world, clear);
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
      private final Minecraft init_Minecraft = Minecraft.getInstance();
      private final ClientLevel init_ClientLevel;
      private final List<u.Inner_CachePool> init_List;
      private final u init_u;
      private final AABB init_AABB;
      private final List<BlockPos> load_List;
      private final List<b.Inner_DataUtil.Inner_StreamUtil> read_List = new ArrayList<>();
      private final List<b.Inner_DataUtil.Inner_StreamUtil> write_List = new ArrayList<>();
      private final Set<BlockPos> init_Set;
      private final Set<BlockPos> load_Set = new HashSet<>();
      private final double init_D;
      private final c.Inner_DataUtil init_c_DataUtil = new c.Inner_DataUtil(System::nanoTime, 8000000000L);
      private int init_I;
      private int load_I;
      private b.Inner_DataUtil.Inner_DataUtil_2 init_b_DataUtil_DataUti;
      private boolean init_Z;
      private boolean load_Z;
      private boolean read_Z;
      private final BlockPos init_BlockPos;
      private c.Inner_CachePool init_c_CachePool;
      private c.Inner_StreamUtil init_c_StreamUtil;
      private List<b.Inner_DataUtil.Inner_StreamUtil> update = List.of();
      private BlockPos load_BlockPos;
      private Vec3 init_Vec3;
      private Set<Long> read_Set = Set.of();
      private c.Inner_DataUtil load_c_DataUtil;
      private Vec3 load_Vec3;
      private boolean write_Z;

      public Inner_DataUtil(List<u.Inner_CachePool> targets, AABB volume, Set<Long> keep, Set<Long> avoid) {
         this(targets, volume, keep, avoid, null);
      }

      public Inner_DataUtil(List<u.Inner_CachePool> targets, AABB volume, Set<Long> keep, Set<Long> avoid, BlockPos continuation) {
         if (AutismLiteVariant.enabled()) {
            throw new IllegalStateException();
         }

         if (!targets.isEmpty() && targets.size() <= 32) {
            this.init_List = List.copyOf(targets);
            this.init_AABB = volume;
            this.init_BlockPos = continuation;
            this.init_ClientLevel = this.init_Minecraft.level;
            this.init_Set = targets.stream().map(u.Inner_CachePool::pos).collect(Collectors.toUnmodifiableSet());
            this.init_D = this.init_Minecraft.player.getEyeHeight(Pose.STANDING);
            if (this.init_Minecraft.player.onGround()) {
               this.load_Set.add(i.init(this.init_Minecraft));
            }

            this.init_u = u.init(targets, this.init_Minecraft.player.blockInteractionRange() - 0.25, this.init_D, volume, keep, avoid);
            Set<BlockPos> unique = new HashSet<>();
            int radius = (int)Math.ceil(this.init_Minecraft.player.blockInteractionRange());

            for (u.Inner_CachePool target : targets) {
               for (int dx = -radius; dx <= radius; dx++) {
                  for (int dy = -radius - 1; dy <= radius - 1; dy++) {
                     for (int dz = -radius; dz <= radius; dz++) {
                        BlockPos feet = target.pos().offset(dx, dy, dz);
                        if (!avoid.contains(feet.asLong())
                           && feet.getY() > this.init_ClientLevel.getMinY()
                           && feet.getY() <= this.init_ClientLevel.getMaxY()
                           && Vec3.atCenterOf(target.pos()).distanceToSqr(Vec3.atBottomCenterOf(feet).add(0.0, this.init_D, 0.0))
                              <= Math.pow(this.init_Minecraft.player.blockInteractionRange() + 0.9, 2.0)) {
                           unique.add(feet);
                        }
                     }
                  }
               }
            }

            this.load_List = new ArrayList<>(unique);
            this.load_List
               .sort(
                  Comparator.<BlockPos>comparingDouble(feetx -> feetx.distToCenterSqr(this.init_Minecraft.player.position()))
                     .thenComparingInt(Vec3i::getY)
                     .thenComparingInt(Vec3i::getX)
                     .thenComparingInt(Vec3i::getZ)
               );
         } else {
            throw new IllegalArgumentException("Invalid bounded batch");
         }
      }

      public boolean init_Z() {
         return this.init_Z || this.load_Z;
      }

      public boolean init_Z_BlockPos(BlockPos target) {
         return !this.load_Z && this.init_Set.contains(target);
      }

      public Set<BlockPos> init_Set() {
         return this.init_Set;
      }

      public void init_V() {
         this.load_Z = true;
         this.init_b_DataUtil_DataUti = null;
         this.init_c_DataUtil.init_V();
         if (this.load_c_DataUtil != null) {
            this.load_c_DataUtil.init_V();
         }
      }

      public void load_V() {
         this.init_c_DataUtil.init(5000000000L);
      }

      public double init_D() {
         return this.init_c_DataUtil.init_D() + (this.load_c_DataUtil == null ? 0.0 : this.load_c_DataUtil.init_D());
      }

      public BlockPos init_BlockPos() {
         return this.load_BlockPos;
      }

      public int init_I() {
         return this.init_c_StreamUtil == null ? 0 : this.init_c_StreamUtil.stops().size();
      }

      public Vec3 init_Vec3() {
         return this.init_c_StreamUtil != null && !this.init_c_StreamUtil.stops().isEmpty()
            ? this.update.get(this.init_c_StreamUtil.stops().getLast().station()).probe.init_Vec3
            : this.init_Minecraft.player.position();
      }

      public void init_V_Vec3(Vec3 worker) {
         if (this.init_c_CachePool == null) {
            this.init_Vec3 = worker;
         }
      }

      public void init_V_Set(Set<Long> cells) {
         this.read_Set = Collections.unmodifiableSet(cells);
      }

      public static List<u.Inner_CachePool> init_List_List_Vec3(List<u.Inner_CachePool> candidates, Vec3 start) {
         return c.init(candidates, start, u.Inner_CachePool::pos);
      }

      public AiOperation init_AiOperation_I(int seconds, Set<Long> keep, Set<Long> avoid) {
         return new b(this, seconds, keep, avoid);
      }

      private boolean init_Z_Vec3(Vec3 worker) {
         if (this.write_Z) {
            return false;
         } else {
            this.write_Z = true;
            if (this.load_Vec3 != null && !(this.load_Vec3.distanceToSqr(worker) <= 2.25) && this.init_c_StreamUtil != null) {
               long remaining = Math.max(1L, (long)((10000.0 - this.init_D()) * 1000000.0));
               this.load_c_DataUtil = new c.Inner_DataUtil(System::nanoTime, Math.min(2000000000L, remaining));
               this.init_c_CachePool = null;
               this.init_c_StreamUtil = null;
               this.init_Z = this.read_Z = false;
               this.init_Vec3 = worker;
               this.write();
               return true;
            } else {
               return false;
            }
         }
      }

      public void init_V_J(long sliceNanos) {
         if (!this.init_Z()) {
            if (this.init_Minecraft.level == this.init_ClientLevel && this.init_Minecraft.player != null) {
               long stop = System.nanoTime() + Math.max(1L, Math.min(2000000L, sliceNanos));
               int units = 0;

               while (units++ < 256 && System.nanoTime() < stop) {
                  if (this.init_c_CachePool != null) {
                     c.Inner_DataUtil activeBudget = this.load_c_DataUtil == null ? this.init_c_DataUtil : this.load_c_DataUtil;
                     if (activeBudget.init_Z() || this.init_c_CachePool.init_Z()) {
                        this.read_Z = this.read_Z | activeBudget.init_Z();
                        this.read();
                        return;
                     }

                     this.init_c_CachePool.init_V();
                  } else if (this.init_c_DataUtil.init_J() <= 1000000000L) {
                     this.write();
                  } else {
                     if (this.init_b_DataUtil_DataUti == null) {
                        if (this.init_I >= this.load_List.size()) {
                           this.write();
                           continue;
                        }

                        this.init_b_DataUtil_DataUti = this.init_b_DataUtil_DataUti_BlockPos(this.load_List.get(this.init_I++));
                        if (this.init_b_DataUtil_DataUti == null) {
                           continue;
                        }
                     }

                     if (this.init_b_DataUtil_DataUti.load_I >= this.init_List.size()) {
                        this.init_V_b_DataUtil_DataUti(this.init_b_DataUtil_DataUti);
                        this.init_b_DataUtil_DataUti = null;
                        if (this.load_Z()) {
                           this.write();
                        }
                     } else {
                        this.init_V_b_DataUtil_DataUti_I(this.init_b_DataUtil_DataUti, this.init_b_DataUtil_DataUti.load_I++);
                        this.load_I++;
                     }
                  }
               }
            } else {
               this.init_V();
            }
         }
      }

      private void read() {
         if (this.init_c_CachePool != null) {
            this.init_c_StreamUtil = this.init_c_CachePool.init_c_StreamUtil();
         }

         this.init_Z = true;
         this.init_c_DataUtil.init_V();
         if (this.load_c_DataUtil != null) {
            this.load_c_DataUtil.init_V();
         }

         AutismClientAddon.LOG
            .info(
               "[AI] batch plan: {} targets, {} checks, {} walking / {} construction workstations, {} stops / {} covered, {} ms{}",
               this.init_List.size(),
               this.load_I,
               this.read_List.size(),
               this.write_List.size(),
               this.init_c_StreamUtil == null ? 0 : this.init_c_StreamUtil.stops().size(),
               this.init_c_StreamUtil == null ? 0 : Long.bitCount(this.init_c_StreamUtil.covered()),
               Math.round(this.init_D()),
               this.read_Z ? " (budget)" : ""
            );
      }

      private void write() {
         if (this.init_b_DataUtil_DataUti != null) {
            this.init_V_b_DataUtil_DataUti(this.init_b_DataUtil_DataUti);
            this.init_b_DataUtil_DataUti = null;
         }

         this.update = new ArrayList<>(this.read_List);
         this.update.addAll(this.write_List);
         long available = 0L;

         for (int i = 0; i < this.init_List.size(); i++) {
            if (this.init_Z_u_CachePool(this.init_List.get(i))) {
               available |= 1L << i;
            }
         }

         long completed = (1L << this.init_List.size()) - 1L & ~available;
         ArrayList<c.Inner_BufferHelper> inputs = new ArrayList<>();

         for (b.Inner_DataUtil.Inner_StreamUtil station : this.update) {
            long jobs = 0L;

            for (int i : station.probe.init_Map.keySet()) {
               if ((station.blockers[i] & completed) == 0L) {
                  jobs |= 1L << i;
               }
            }

            inputs.add(
               new c.Inner_BufferHelper(
                  station.probe.init_BlockPos,
                  jobs & available,
                  station.blockers,
                  station.supports,
                  station.probe.init_Set.size(),
                  station.probe.init_I < 0 ? 0L : 1L << station.probe.init_I & available
               )
            );
         }

         this.load_Vec3 = this.init_Vec3 == null ? this.init_Minecraft.player.position() : this.init_Vec3;
         this.init_c_CachePool = new c.Inner_CachePool(inputs, this.load_Vec3, this.init_BlockPos, this.init_List.size(), available);
      }

      private boolean load_Z() {
         if (this.read_List.size() < Math.min(12, this.init_List.size() * 2)) {
            return false;
         }

         long covered = 0L;
         long needed = 0L;

         for (int i = 0; i < this.init_List.size(); i++) {
            if (this.init_Z_u_CachePool(this.init_List.get(i))) {
               needed |= 1L << i;
            }
         }

         for (b.Inner_DataUtil.Inner_StreamUtil station : this.read_List) {
            if (station.probe.init_I < 0) {
               for (int job : station.probe.init_Map.keySet()) {
                  covered |= 1L << job;
               }
            }
         }

         return (needed & ~covered) == 0L;
      }

      private b.Inner_DataUtil.Inner_DataUtil_2 init_b_DataUtil_DataUti_BlockPos(BlockPos feet) {
         if (this.init_ClientLevel.getChunkSource().getChunk(feet.getX() >> 4, feet.getZ() >> 4, ChunkStatus.FULL, false) == null) {
            return null;
         }

         BlockState floor = this.init_ClientLevel.getBlockState(feet.below());
         if (ClassCache.Inner_StreamUtil.read(floor)) {
            return null;
         }

         for (BlockPos occupied : List.of(feet, feet.above())) {
            BlockState state = this.init_ClientLevel.getBlockState(occupied);
            if (ClassCache.Inner_StreamUtil.load(state) || ClassCache.Inner_StreamUtil.read(state) || state.getFluidState().is(FluidTags.LAVA)) {
               return null;
            }
         }

         VoxelShape floorShape = floor.getCollisionShape(this.init_ClientLevel, feet.below());
         boolean supported = !floorShape.isEmpty();
         int foundationJob = -1;
         BlockState futureFloor = null;
         if (!supported && floor.canBeReplaced() && floor.getFluidState().isEmpty()) {
            for (int i = 0; i < this.init_List.size(); i++) {
               u.Inner_CachePool source = this.init_List.get(i);
               if (source.pos().equals(feet.below())
                  && source.requested() != null
                  && this.init_Z_u_CachePool(source)
                  && !(source.requested().getBlock() instanceof FallingBlock)
                  && !ClassCache.Inner_StreamUtil.read(source.requested())
                  && Block.isShapeFullBlock(source.requested().getCollisionShape(this.init_ClientLevel, source.pos()))) {
                  foundationJob = i;
                  futureFloor = source.requested();
                  floorShape = futureFloor.getCollisionShape(this.init_ClientLevel, source.pos());
                  supported = true;
                  break;
               }
            }
         }

         if (supported && !init_Z_VoxelShape(floorShape)) {
            return null;
         }

         if (supported || floor.canBeReplaced() && floor.getFluidState().isEmpty() && this.init_u.read(feet.below())) {
            double top = supported ? floorShape.max(Axis.Y) : 1.0;
            Vec3 position = Vec3.atBottomCenterOf(feet).add(0.0, top - 1.0, 0.0);
            AABB body = new AABB(position.x - 0.3, position.y, position.z - 0.3, position.x + 0.3, position.y + 1.8, position.z + 0.3);
            Set<BlockPos> clear = new HashSet<>();
            if (!this.init_ClientLevel.noCollision(this.init_Minecraft.player, body)) {
               if (!this.init_ClientLevel.getEntityCollisions(this.init_Minecraft.player, body).isEmpty()) {
                  return null;
               }

               for (BlockPos cell : BlockPos.betweenClosed(
                  BlockPos.containing(body.minX + 0.001, body.minY + 0.001, body.minZ + 0.001),
                  BlockPos.containing(body.maxX - 0.001, body.maxY - 0.001, body.maxZ - 0.001)
               )) {
                  BlockState state = this.init_ClientLevel.getBlockState(cell);
                  boolean obstructs = false;

                  for (AABB box : state.getCollisionShape(this.init_ClientLevel, cell).toAabbs()) {
                     if (box.move(cell).intersects(body.deflate(1.0E-4))) {
                        obstructs = true;
                        break;
                     }
                  }

                  if (obstructs) {
                     if (!this.init_u.load(cell)
                        || state.hasBlockEntity()
                        || !state.getFluidState().isEmpty()
                        || state.getDestroySpeed(this.init_ClientLevel, cell) < 0.0F
                        || ClassCache.Inner_StreamUtil.load(state)
                        || ClassCache.Inner_StreamUtil.read(state)) {
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
               : init_LevelReader_LevelReader(this.init_ClientLevel, Set.copyOf(clear), futureFloor == null ? Map.of() : Map.of(feet.below(), futureFloor));
            if (supported && foundationJob < 0 && clear.isEmpty()) {
               this.load_Set.add(feet);
            }

            return new b.Inner_DataUtil.Inner_DataUtil_2(
               feet, position, body, Set.copyOf(clear), view, supported, supported && clear.isEmpty(), top, foundationJob
            );
         } else {
            return null;
         }
      }

      private void init_V_b_DataUtil_DataUti_I(b.Inner_DataUtil.Inner_DataUtil_2 probe, int targetIndex) {
         if (targetIndex != probe.init_I) {
            u.Inner_CachePool target = this.init_List.get(targetIndex);
            if (this.init_Z_u_CachePool(target)
               && (
                  target.requested() == null
                     || target.requested().canSurvive(probe.init_LevelReader == null ? this.init_ClientLevel : probe.init_LevelReader, target.pos())
               )
               && !(
                  probe.init_Vec3.add(0.0, this.init_D, 0.0).distanceToSqr(Vec3.atCenterOf(target.pos()))
                     > Math.pow(this.init_Minecraft.player.blockInteractionRange() + 0.9, 2.0)
               )) {
               U.Inner_CachePool hit = probe.init_LevelReader == null
                  ? u.init(this.init_Minecraft, target.pos(), target.item(), target.constraint(), probe.init_Vec3, 32)
                  : this.init(target, probe.init_Vec3, probe.init_AABB, probe.init_Set, probe.init_LevelReader);
               if (hit != null) {
                  double height = u.init(this.init_Minecraft, target.pos(), target.item(), target.constraint(), true);
                  if (probe.init_Z
                     || !new AABB(probe.init_BlockPos.below()).clip(probe.init_Vec3.add(0.0, height, 0.0), hit.init_BlockHitResult().getLocation()).isPresent()
                     )
                   {
                     probe.init_Map
                        .put(
                           targetIndex,
                           new u.Inner_StreamUtil(
                              hit.init_BlockHitResult().getBlockPos(),
                              hit.init_BlockHitResult().getLocation(),
                              hit.init_BlockHitResult().getDirection(),
                              probe.init_D - 1.0,
                              height + probe.init_D - 1.0
                           )
                        );
                  }
               }
            }
         }
      }

      private void init_V_b_DataUtil_DataUti(b.Inner_DataUtil.Inner_DataUtil_2 probe) {
         if (!probe.init_Map.isEmpty()) {
            long available = 0L;
            long[] before = new long[this.init_List.size()];
            long[] blockers = new long[this.init_List.size()];

            for (int i : probe.init_Map.keySet()) {
               available |= 1L << i;
            }

            for (int i = 0; i < this.init_List.size(); i++) {
               u.Inner_CachePool target = this.init_List.get(i);
               if (target.requested() != null) {
                  VoxelShape shape = target.requested().getShape(this.init_ClientLevel, target.pos());

                  for (int j : probe.init_Map.keySet()) {
                     if (i != j) {
                        u.Inner_StreamUtil proof = probe.init_Map.get(j);
                        if (!proof.support().equals(target.pos())) {
                           Vec3 eye = Vec3.atBottomCenterOf(probe.init_BlockPos).add(0.0, proof.eyeHeight(), 0.0);

                           for (AABB box : shape.toAabbs()) {
                              if (box.move(target.pos()).clip(eye, proof.point()).isPresent() || box.move(target.pos()).intersects(probe.init_AABB)) {
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

            List<Integer> order = c.init(available, before);
            HashMap<BlockPos, BlockState> observed = new HashMap<>();
            observed.put(probe.init_BlockPos.below(), this.init_ClientLevel.getBlockState(probe.init_BlockPos.below()));

            for (int i : probe.init_Map.keySet()) {
               u.Inner_StreamUtil proof = probe.init_Map.get(i);
               BlockPos support = proof.support();
               observed.put(support, this.init_ClientLevel.getBlockState(support));
               Vec3 eye = Vec3.atBottomCenterOf(probe.init_BlockPos).add(0.0, proof.eyeHeight(), 0.0);
               b.init(
                  this.init_ClientLevel,
                  eye,
                  proof.point().add(-proof.face().getStepX() * 0.001, -proof.face().getStepY() * 0.001, -proof.face().getStepZ() * 0.001),
                  observed
               );
            }

            for (BlockPos clear : probe.init_Set) {
               observed.put(clear, this.init_ClientLevel.getBlockState(clear));
            }

            int supports = probe.init_Z
               ? 0
               : Math.max(
                  1,
                  this.load_Set
                     .stream()
                     .mapToInt(
                        anchor -> Math.abs(anchor.getX() - probe.init_BlockPos.getX())
                           + Math.abs(anchor.getZ() - probe.init_BlockPos.getZ())
                           + Math.max(0, probe.init_BlockPos.getY() - anchor.getY())
                     )
                     .min()
                     .orElse(1)
               );
            b.Inner_DataUtil.Inner_StreamUtil station = new b.Inner_DataUtil.Inner_StreamUtil(probe, order, blockers, Map.copyOf(observed), supports);
            List<b.Inner_DataUtil.Inner_StreamUtil> list = probe.load_Z ? this.read_List : this.write_List;
            list.add(station);
            list.sort(
               Comparator.<b.Inner_DataUtil.Inner_StreamUtil>comparingDouble(s -> s.score(this.init_Minecraft.player.position()))
                  .thenComparingLong(s -> s.probe.init_BlockPos.asLong())
            );
            if (list.size() > 64) {
               LinkedHashSet<b.Inner_DataUtil.Inner_StreamUtil> retained = new LinkedHashSet<>(list.subList(0, 24));

               for (int i = 0; i < this.init_List.size(); i++) {
                  int job = i;
                  list.stream()
                     .filter(s -> s.probe.init_Map.containsKey(job))
                     .min(Comparator.comparingDouble(s -> s.score(this.init_Minecraft.player.position()) * s.order.size()))
                     .ifPresent(retained::add);
               }

               list.stream()
                  .sorted(Comparator.comparingDouble(s -> s.probe.init_BlockPos.distToCenterSqr(this.init_Minecraft.player.position())))
                  .limit(8L)
                  .forEach(retained::add);

               for (b.Inner_DataUtil.Inner_StreamUtil s : list) {
                  if (retained.size() < 64) {
                     retained.add(s);
                  }
               }

               list.clear();
               list.addAll(retained);
            }
         }
      }

      private boolean init_Z_u_CachePool(u.Inner_CachePool target) {
         BlockState live = this.init_ClientLevel.getBlockState(target.pos());
         return live.canBeReplaced() && live.getFluidState().isEmpty();
      }

      private List<u.Inner_CachePool> init_List() {
         return this.init_List.stream().filter(this::init_Z_u_CachePool).toList();
      }

      private boolean init_Z_b_DataUtil_StreamU(b.Inner_DataUtil.Inner_StreamUtil station, Set<Long> keep, Set<Long> avoid) {
         if (avoid.contains(station.probe.init_BlockPos.asLong())) {
            return false;
         }

         if (!b.init(this.init_ClientLevel, station.observed)) {
            return false;
         }

         for (BlockPos cell : station.probe.init_Set) {
            if (keep.contains(cell.asLong())) {
               return false;
            }
         }

         for (BlockPos cell : BlockPos.betweenClosed(
            BlockPos.containing(station.probe.init_AABB.minX + 0.001, station.probe.init_AABB.minY + 0.001, station.probe.init_AABB.minZ + 0.001),
            BlockPos.containing(station.probe.init_AABB.maxX - 0.001, station.probe.init_AABB.maxY - 0.001, station.probe.init_AABB.maxZ - 0.001)
         )) {
            if (!station.probe.init_Set.contains(cell)) {
               BlockState state = this.init_ClientLevel.getBlockState(cell);
               if (ClassCache.Inner_StreamUtil.load(state) || ClassCache.Inner_StreamUtil.read(state)) {
                  return false;
               }

               for (AABB box : state.getCollisionShape(this.init_ClientLevel, cell).toAabbs()) {
                  if (box.move(cell).intersects(station.probe.init_AABB.deflate(1.0E-4))) {
                     return false;
                  }
               }
            }
         }

         return station.order.stream().anyMatch(i -> this.init_Z_u_CachePool(this.init_List.get(i)))
            && this.init_ClientLevel.getEntityCollisions(this.init_Minecraft.player, station.probe.init_AABB).isEmpty();
      }

      private Map<Long, u.Inner_StreamUtil> init_Map_Z(boolean edits, boolean preferred, Set<Long> keep, Set<Long> avoid) {
         ArrayList<b.Inner_DataUtil.Inner_StreamUtil> stations = new ArrayList<>(this.read_List);
         if (edits) {
            stations.addAll(this.write_List);
         }

         stations.removeIf(stationx -> !this.init_Z_b_DataUtil_StreamU(stationx, keep, avoid));
         stations.sort(
            Comparator.<b.Inner_DataUtil.Inner_StreamUtil>comparingDouble(s -> s.score(this.init_Minecraft.player.position()))
               .thenComparingLong(s -> s.probe.init_BlockPos.asLong())
         );
         LinkedHashMap<Long, u.Inner_StreamUtil> result = new LinkedHashMap<>();

         for (b.Inner_DataUtil.Inner_StreamUtil station : stations) {
            if (preferred && result.size() >= 4) {
               break;
            }

            for (int i : station.order) {
               if (this.init_Z_u_CachePool(this.init_List.get(i))) {
                  result.put(station.probe.init_BlockPos.asLong(), station.probe.init_Map.get(i));
                  break;
               }
            }
         }

         return result;
      }

      public List<BlockPos> init_List_BlockPos(BlockPos feet) {
         c.Inner_ConfigStore stop = this.init_c_ConfigStore();
         if (stop != null && this.update.get(stop.station()).probe.init_BlockPos.equals(feet)) {
            return this.init_c_StreamUtil
               .order()
               .stream()
               .filter(i -> this.init_Z_u_CachePool(this.init_List.get(i)))
               .map(i -> this.init_List.get(i).pos())
               .toList();
         }

         this.update();
         return List.of();
      }

      public double init_D_BlockPos(BlockPos feet) {
         if (this.init_c_StreamUtil != null) {
            return this.init_c_StreamUtil.ticks() / 20.0;
         }

         for (List<b.Inner_DataUtil.Inner_StreamUtil> list : List.of(this.read_List, this.write_List)) {
            for (b.Inner_DataUtil.Inner_StreamUtil station : list) {
               if (station.probe.init_BlockPos.equals(feet)) {
                  return c.load(feet, this.init_Minecraft.player.position(), station.order.size(), station.supports, station.probe.init_Set.size());
               }
            }
         }

         return 0.0;
      }

      public int init_I_BlockPos(BlockPos feet) {
         if (this.init_c_StreamUtil != null) {
            return this.init_c_StreamUtil.supports();
         }

         for (List<b.Inner_DataUtil.Inner_StreamUtil> list : List.of(this.read_List, this.write_List)) {
            for (b.Inner_DataUtil.Inner_StreamUtil station : list) {
               if (station.probe.init_BlockPos.equals(feet)) {
                  return station.supports;
               }
            }
         }

         return 0;
      }

      private c.Inner_ConfigStore init_c_ConfigStore() {
         if (this.init_c_StreamUtil == null) {
            return null;
         }

         long completed = 0L;

         for (int i = 0; i < this.init_List.size(); i++) {
            if (!this.init_Z_u_CachePool(this.init_List.get(i))) {
               completed |= 1L << i;
            }
         }

         return this.init_c_StreamUtil.nextStop(completed);
      }

      private void update() {
         this.init_c_StreamUtil = null;
      }

      public boolean load(BlockPos target) {
         return !this.load_Z
            && this.init_Minecraft.level == this.init_ClientLevel
            && this.init_c_StreamUtil != null
            && this.init_c_ConfigStore() != null
            && this.init_c_StreamUtil
               .order()
               .stream()
               .anyMatch(i -> this.init_List.get(i).pos().equals(target) && this.init_Z_u_CachePool(this.init_List.get(i)));
      }

      public boolean read(BlockPos pos) {
         if (this.init_c_StreamUtil != null && !this.load_Z) {
            int placed = -1;

            for (int i = 0; i < this.init_List.size(); i++) {
               if (this.init_List.get(i).pos().equals(pos)) {
                  placed = i;
                  break;
               }
            }

            if (placed >= 0 && this.init_Z_u_CachePool(this.init_List.get(placed))) {
               long bit = 1L << placed;

               for (c.Inner_ConfigStore stop : this.init_c_StreamUtil.stops()) {
                  b.Inner_DataUtil.Inner_StreamUtil station = this.update.get(stop.station());

                  for (int job : stop.jobs()) {
                     if (job != placed && this.init_Z_u_CachePool(this.init_List.get(job)) && (station.blockers[job] & bit) != 0L) {
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

      private Map<Long, u.Inner_StreamUtil> init_Map_c_ConfigStore(c.Inner_ConfigStore stop, Set<Long> keep, Set<Long> avoid) {
         b.Inner_DataUtil.Inner_StreamUtil station = this.update.get(stop.station());
         if (avoid.contains(station.probe.init_BlockPos.asLong())) {
            return Map.of();
         }

         for (Entry<BlockPos, BlockState> entry : station.observed.entrySet()) {
            BlockPos cell = entry.getKey();
            BlockState live = this.init_ClientLevel.getBlockState(cell);
            if (live != entry.getValue()
               && !this.init_Set.contains(cell)
               && !this.read_Set.contains(cell.asLong())
               && (!station.probe.init_Set.contains(cell) || !live.isAir())
               && (!cell.equals(station.probe.init_BlockPos.below()) || a.init_Block(cell) == null)) {
               return Map.of();
            }
         }

         b.Inner_DataUtil.Inner_DataUtil_2 fresh = this.init_b_DataUtil_DataUti_BlockPos(station.probe.init_BlockPos);
         if (fresh != null
            && !(Math.abs(fresh.init_D - station.probe.init_D) > 0.001)
            && !fresh.init_Set.stream().anyMatch(cellx -> keep.contains(cellx.asLong()))) {
            u.Inner_StreamUtil first = null;

            for (int i : stop.jobs()) {
               if (this.init_Z_u_CachePool(this.init_List.get(i))) {
                  this.init_V_b_DataUtil_DataUti_I(fresh, i);
                  u.Inner_StreamUtil proof = fresh.init_Map.get(i);
                  if (proof == null) {
                     return Map.of();
                  }

                  if (first == null) {
                     first = proof;
                  }
               }
            }

            return first == null ? Map.of() : Map.of(fresh.init_BlockPos.asLong(), first);
         } else {
            return Map.of();
         }
      }

      public static <T> void init_V_List(List<T> source, int cursor, List<T> batch) {
         c.init(source, cursor, batch);
      }

      public static <T> Set<Long> init_Set_List(List<T> source, int cursor, Set<Long> existing, ToLongFunction<T> position) {
         return c.init(source, cursor, existing, position);
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

      private U.Inner_CachePool init(u.Inner_CachePool target, Vec3 position, AABB body, Set<BlockPos> clear, LevelReader view) {
         u.Inner_DataUtil constraint = target.constraint();
         if (target.requested() != null && !target.requested().canSurvive(view, target.pos())) {
            return null;
         }

         if (constraint != null && constraint.placedShape() != null) {
            for (AABB box : e.init_List_S(constraint.placedShape())) {
               if (box.move(target.pos()).intersects(body)) {
                  return null;
               }
            }
         }

         double height = u.init(this.init_Minecraft, target.pos(), target.item(), constraint, true);
         Vec3 eye = position.add(0.0, height, 0.0);
         double reach = this.init_Minecraft.player.blockInteractionRange() - 0.25;
         U.Inner_NetHelper placement = (hitx, cell) -> !clear.contains(hitx.getBlockPos())
            && u.init(this.init_Minecraft, cell, target.item(), constraint, eye, body, hitx);
         U.Inner_StateCache request = new U.Inner_StateCache(target.pos(), eye, reach, placement).load(true);
         if (constraint != null) {
            EnumSet<Direction> faces = EnumSet.noneOf(Direction.class);

            for (Direction face : Direction.values()) {
               if (constraint.acceptsFace(face)) {
                  faces.add(face);
               }
            }

            request.init_U_StateCache_EnumSet(faces);
            if (constraint.clickYaw() != null) {
               request.init_U_StateCache_ce_DataUtil(new RotationMath.Inner_DataUtil(constraint.clickYaw(), 0.0F));
            }
         }

         ArrayList<U.Inner_TaskQueue> options = new ArrayList<>();
         U.init(request, options);
         int rays = 0;

         for (U.Inner_TaskQueue option : options) {
            if (!clear.contains(option.load_BlockPos())) {
               AABB rect = constraint != null && constraint.highHalf() != null
                  ? u.init_AABB_AABB(option.init_AABB(), option.init_Direction(), option.load_BlockPos().getY(), constraint.highHalf())
                  : option.init_AABB();
               if (rect != null && constraint != null && constraint.crouchForLook()) {
                  rect = u.init_AABB_AABB(rect, option.init_Direction(), eye);
               }

               if (rect != null) {
                  U.Inner_TaskQueue clipped = new U.Inner_TaskQueue(
                     option.init_BlockPos(),
                     option.load_BlockPos(),
                     option.init_Direction(),
                     option.init_I(),
                     option.init_BlockState(),
                     rect,
                     option.init_D(),
                     option.load_D(),
                     option.init_Z(),
                     option.init_U_BufferHelper()
                  );
                  U.Inner_DataUtil aim = U.init(clipped, request, null);
                  if (aim != null) {
                     Vec3 end = eye.add(Vec3.directionFromRotation(aim.load().load(), aim.load().init()).scale(reach));
                     BlockHitResult hit = view.clip(
                        new ClipContext(eye, end, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, this.init_Minecraft.player)
                     );
                     if (hit.getType() == Type.BLOCK
                        && placement.lands(hit, target.pos())
                        && placement.init(view.getBlockState(hit.getBlockPos()), hit.getBlockPos(), true)) {
                        return new U.Inner_CachePool(clipped, aim, hit, false, 0.0);
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

      static LevelReader init_LevelReader_LevelReader_Set(LevelReader world, Set<BlockPos> clear) {
         return init_LevelReader_LevelReader(world, clear, Map.of());
      }

      static LevelReader init_LevelReader_LevelReader(LevelReader world, Set<BlockPos> clear, Map<BlockPos, BlockState> placed) {
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
         final BlockPos init_BlockPos;
         final Vec3 init_Vec3;
         final AABB init_AABB;
         final Set<BlockPos> init_Set;
         final LevelReader init_LevelReader;
         final boolean init_Z;
         final boolean load_Z;
         final double init_D;
         final int init_I;
         final Map<Integer, u.Inner_StreamUtil> init_Map = new LinkedHashMap<>();
         int load_I;

         Inner_DataUtil_2(
            BlockPos feet, Vec3 position, AABB body, Set<BlockPos> clear, LevelReader view, boolean supported, boolean walk, double top, int foundationJob
         ) {
            this.init_BlockPos = feet;
            this.init_Vec3 = position;
            this.init_AABB = body;
            this.init_Set = clear;
            this.init_LevelReader = view;
            this.init_Z = supported;
            this.load_Z = walk;
            this.init_D = top;
            this.init_I = foundationJob;
         }
      }

      private record Inner_StreamUtil(
         b.Inner_DataUtil.Inner_DataUtil_2 probe, List<Integer> order, long[] blockers, Map<BlockPos, BlockState> observed, int supports
      ) {
         double score(Vec3 worker) {
            return c.init(this.probe.init_BlockPos, worker, this.order.size(), this.supports, this.probe.init_Set.size());
         }
      }
   }
}
