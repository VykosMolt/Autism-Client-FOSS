package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.RemoteViewController;
import autismclient.util.RotationMath;
import autismclient.util.ServerRotationTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class BedNukerModule extends Module implements SilentMovementCoordinator.Participant {
   private BedBreachRouteSolver.BreachRoute route;
   private final MineBreakPaceGuard paceGuard = new MineBreakPaceGuard();
   private BlockPos targetBed;
   private boolean miningStarted;
   private boolean load_Z;
   private BlockPos miningPos;
   private BlockHitResult lastHit;
   private boolean read_Z;
   private boolean write_Z;
   private int routeTick = -2147483648;
   private int load_I = -2147483648;
   private int pendingSlot = -1;
   private long slotSwapAtNanos;
   private Object lastLevel;
   private String statusText = "Idle";
   private int previousSlot = -1;
   private int update = -1;
   private int sync = -2147483648;
   private ItemStack previousHeld = ItemStack.EMPTY;
   private Object previousSlotLevel;

   public BedNukerModule() {
      super("bed-nuker", "BedNuker", ae.read, "Silently mine the quickest bed opening in reach");
      this.add(new BoolSetting("auto-tool", "Auto Tool", true).description("Use Auto Tool defaults independently; keep the held item when no tool is faster"));
      this.add(new BoolSetting("move-fix", "Move Fix", true).description("Walk where you look"));
      this.add(new BoolSetting("own-bed", "Own Bed", false).description("Allow mining your team's bed"));
   }

   @Override
   public String settingsHint() {
      return "Uses BedESP settings, even when disabled.";
   }

   @Override
   public boolean usesMoveFix() {
      return this.bool("move-fix");
   }

   private int clientTick() {
      return AutismSessionState.getInstance().getClientTick();
   }

   public static BedNukerModule active() {
      return al.init_Module_Str("bed-nuker") instanceof BedNukerModule m && m.isEnabled() ? m : null;
   }

   static BedBreachRouteSolver.BreachRoute routeFor(BlockPos pos) {
      BedNukerModule m = active();
      return m != null && pos.equals(m.targetBed) ? m.route : null;
   }

   static BedEspModule.BedInfo init_ClassCache_DataUti() {
      BedNukerModule m = active();
      if (m != null && m.route != null && m.targetBed != null && MC.level != null && MC.player != null) {
         BedEspModule.EspPolicy policy = BedEspModule.policy();
         if (!policy.load() && !policy.read()) {
            return null;
         }

         BlockState state = MC.level.getBlockState(m.targetBed);
         if (state.getBlock() instanceof BedBlock block) {
            BlockPos var7 = m.targetBed.relative(BedBlock.getConnectedDirection(state));
            AABB box = new AABB(m.targetBed);
            if (MC.level.getBlockState(var7).is(block)) {
               box = box.minmax(new AABB(var7));
            }

            box = box.setMaxY(m.targetBed.getY() + 0.5625);
            List<BlockPos> cuts = m.route.cells().stream().filter(c -> !c.init_Z()).map(BedBreachRouteSolver.BreachCell::pos).toList();
            return new BedEspModule.BedInfo(
               m.targetBed,
               var7,
               block.getColor(),
               box,
               box.getCenter().add(0.0, 1.25, 0.0),
               List.of(),
               cuts,
               List.of(),
               m.route.ticks(),
               "",
               !cuts.isEmpty(),
               List.of(),
               MC.player.getEyePosition(),
               m.route.eye()
            );
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static boolean init_Z_23() {
      BedNukerModule m = active();
      return m != null && m.routeTick == m.clientTick();
   }

   public static boolean read_Z_10() {
      BedNukerModule m = active();
      return m != null && m.miningStarted && autismclient.util.SilentMiningCoordinator.isOwner(m.id());
   }

   static ItemStack init_ItemStack() {
      if (MC.player == null) {
         return ItemStack.EMPTY;
      }

      if (al.init_Module_Str("bed-nuker") instanceof BedNukerModule m) {
         int slot = m.load_I_7();
         if (slot >= 0) {
            return MC.player.getInventory().getItem(slot);
         }
      }

      return MC.player.getMainHandItem();
   }

   static boolean sync() {
      return al.init_Module_Str("bed-nuker") instanceof BedNukerModule m && m.previousSlot >= 0 && m.load_I_7() >= 0;
   }

   static boolean flush() {
      if (al.init_Module_Str("bed-nuker") instanceof BedNukerModule m && !init_Z_23()) {
         int slot = m.load_I_7();
         if (slot >= 0 && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, m.id()) && autismclient.util.InventorySlotArbiter.beginActionGroup(m.id())) {
            try {
               m.stopMining();
               m.pendingSlot = -1;
               autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, slot);
               m.clearSlotRestore();
               return true;
            } finally {
               autismclient.util.InventorySlotArbiter.endActionGroup(m.id());
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private int load_I_7() {
      if (MC.player == null) {
         this.clearSlotRestore();
         return -1;
      }

      int slot = init(
         MC.player.getInventory().getSelectedSlot(),
         this.previousSlot,
         this.update,
         this.previousSlotLevel == MC.level,
         this.isEnabled(),
         (long)this.clientTick() - this.sync,
         ItemStack.isSameItem(MC.player.getMainHandItem(), this.previousHeld)
      );
      if (slot < 0) {
         this.clearSlotRestore();
      }

      return slot;
   }

   static int init(int selected, int original, int tool, boolean sameWorld, boolean enabled, long age, boolean sameTool) {
      return sameWorld
            && sameTool
            && original >= 0
            && original < 9
            && tool >= 0
            && tool < 9
            && original != tool
            && selected == tool
            && (enabled || age >= 0L && age <= 6L)
         ? original
         : -1;
   }

   private void clearSlotRestore() {
      this.previousSlot = this.update = -1;
      this.sync = -2147483648;
      this.previousHeld = ItemStack.EMPTY;
      this.previousSlotLevel = null;
   }

   public static boolean close() {
      BedNukerModule m = active();
      return m == null || !read_Z_10() || m.write_Z && m.paceGuard.init_Z(System.nanoTime());
   }

   public static void init_V_BlockPos_3(BlockPos pos) {
      BedNukerModule m = active();
      if (m != null && read_Z_10() && pos.equals(m.miningPos)) {
         m.paceGuard.init_V(System.nanoTime());
      }
   }

   public static float init_F_F_BlockPos(float step, BlockPos pos) {
      BedNukerModule m = active();
      if (m != null && read_Z_10() && pos.equals(m.miningPos)) {
         float damage = ((AutismMultiPlayerGameModeAccessor)MC.gameMode).autism$getDestroyProgress();
         return !m.read_Z || !m.write_Z && damage + step >= 1.0F ? 0.0F : m.paceGuard.init(step, damage, System.nanoTime());
      } else {
         return step;
      }
   }

   private boolean open() {
      return this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId());
   }

   @Override
   public boolean isSilentMovementActive() {
      return !SilentMovementCoordinator.isSafeWalkActive() && (autismclient.util.SilentRotationManager.isReturningToPlayer() || this.isEnabled() && this.routeTick == this.clientTick());
   }

   private boolean poll() {
      return MC.player != null
         && MC.level != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && MC.player.isAlive()
         && !MC.player.isSpectator()
         && !MC.player.isUsingItem()
         && !MC.player.isHandsBusy()
         && !aD.init_Z()
         && !FreecamController.isActive()
         && !FreecamController.isAiControllingBody()
         && !RemoteViewController.isActive()
         && !PkgCls_a.init_Z()
         && !MacroExecutor.isRunning()
         && !f.load()
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && !SilentMovementCoordinator.isSafeWalkActive()
         && !ScaffoldModule.update_Z_5()
         && !ClutchModule.init_Z_49()
         && !DangerModule.sync_Z_12()
         && !BedDefenderModule.init_Z_10()
         && !SurroundModule.init_Z_27()
         && !AnchorAuraModule.init_Z_2()
         && !CrystalAuraModule.init_Z_51()
         && !"auto-trap".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())
         && !AutoTotemModule.init_Z_6()
         && !AutoArmorModule.init_Z_20()
         && !BlinkPacketManager.isHoldingActionsOnly()
         && !autismclient.util.ManualInteractionYield.isYielding();
   }

   @Override
   public void preMovementTick() {
      this.load_Z = true;

      try {
         this.load_V_16();
      } finally {
         this.load_Z = false;
      }
   }

   private void load_V_16() {
      this.routeTick = -2147483648;
      this.read_Z = this.write_Z = false;
      if (this.lastLevel != MC.level) {
         this.resetRoute();
         this.clearSlotRestore();
         this.paceGuard.load();
         this.lastLevel = MC.level;
      }

      if (!this.poll()) {
         this.stopWithReason("Paused", true);
      } else {
         if (this.load_I_7() >= 0) {
            this.sync = this.clientTick();
         }

         EspChunkWorker.setLevel(MC.level);
         AutismMultiPlayerGameModeAccessor mining = (AutismMultiPlayerGameModeAccessor)MC.gameMode;
         BlockPos current = this.miningStarted && mining.autism$isDestroying() ? mining.autism$getDestroyBlockPos() : null;
         float progress = current == null ? 0.0F : mining.autism$getDestroyProgress();
         BedBreachRouteSolver.BreachRoute best = null;
         BlockPos selected = null;
         DyeColor own = TeamDyeColorResolver.init();
         BedEspModule.EspPolicy policy = BedEspModule.policy();
         Vec3 nextEye = this.init_Vec3_2();
         BedMiningEstimator.Inner_StreamUtil tracking = null;
         if (current != null
            && current.equals(this.miningPos)
            && this.route != null
            && this.targetBed != null
            && MC.level.getBlockState(this.targetBed).getBlock() instanceof BedBlock block
            && this.isTargetBed(block, own)) {
            tracking = BedMiningEstimator.init(current, this.route.eye(), progress, nextEye);
            if (tracking != null) {
               ArrayList<BedBreachRouteSolver.BreachCell> cells = new ArrayList<>();
               cells.add(tracking.init_DataPipe_DataUtil());

               for (BedBreachRouteSolver.BreachCell cell : this.route.cells().subList(1, this.route.cells().size())) {
                  BlockState state = MC.level.getBlockState(cell.pos());
                  if (!state.isAir()) {
                     cells.add(
                        new BedBreachRouteSolver.BreachCell(
                           cell.pos(),
                           cell.boxes(),
                           BedMiningEstimator.init(state, cell.pos(), MC.player.getInventory().getItem(cell.slot())),
                           cell.slot(),
                           cell.init_Z()
                        )
                     );
                  }
               }

               AABB bounds = new AABB(this.targetBed)
                  .minmax(new AABB(this.targetBed.relative(BedBlock.getConnectedDirection(MC.level.getBlockState(this.targetBed)))))
                  .setMaxY(this.targetBed.getY() + 0.5625);
               boolean complete = bounds.distanceToSqr(MC.player.getEyePosition()) <= Math.pow(MC.player.blockInteractionRange(), 2.0);
               best = new BedBreachRouteSolver.BreachRoute(
                  List.copyOf(cells),
                  tracking.init_Vec3() == null ? this.route.eye() : tracking.init_Vec3(),
                  cells.stream().mapToDouble(BedBreachRouteSolver.BreachCell::ticks).sum() + Math.max(0, cells.size() - 1) * 5,
                  complete
               );
               selected = this.targetBed;
            }
         }

         if (current != null && tracking == null) {
            this.stopMining();
            current = null;
            progress = 0.0F;
         }

         if (best == null && (own != null || this.bool("own-bed"))) {
            int radius = (int)Math.ceil(MC.player.blockInteractionRange()) + 8;
            ArrayList<BlockPos> candidates = new ArrayList<>();
            BlockPos playerPos = MC.player.blockPosition();

            for (int cx = playerPos.getX() - radius >> 4; cx <= playerPos.getX() + radius >> 4; cx++) {
               for (int cz = playerPos.getZ() - radius >> 4; cz <= playerPos.getZ() + radius >> 4; cz++) {
                  LevelChunk chunk = MC.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                  if (chunk != null) {
                     for (EspChunkWorker.EspBox entry : BedEspModule.scanChunk(chunk)) {
                        BlockPos p = BlockPos.containing(entry.tracer());
                        if (!(new AABB(p).distanceToSqr(MC.player.getEyePosition()) > (double)radius * radius)) {
                           BlockState state = MC.level.getBlockState(p);
                           if (state.getBlock() instanceof BedBlock block
                              && this.isTargetBed(block, own)
                              && !(new AABB(p).distanceToSqr(MC.player.position()) > (double)policy.range() * policy.range())
                              && (
                                 state.getValue(BedBlock.PART) != BedPart.HEAD
                                    || !MC.level.getBlockState(p.relative(BedBlock.getConnectedDirection(state))).is(block)
                              )) {
                              candidates.add(p.immutable());
                           }
                        }
                     }
                  }
               }
            }

            candidates.sort(Comparator.comparingDouble(px -> px.distToCenterSqr(MC.player.position())));

            for (BlockPos p : candidates.stream().limit(8L).toList()) {
               BedBreachRouteSolver.BreachRoute candidate = BedMiningEstimator.load(p, this.bool("auto-tool"), current, progress);
               if (candidate != null
                  && (
                     best == null
                        || candidate.init_Z() && !best.init_Z()
                        || candidate.init_Z() == best.init_Z()
                           && (candidate.ticks() < best.ticks() - 0.5 || candidate.ticks() <= best.ticks() + 0.5 && p.equals(this.targetBed))
                  )) {
                  best = candidate;
                  selected = p;
               }
            }
         }

         this.route = best;
         this.targetBed = selected;
         if (this.route == null) {
            this.stopWithReason("No reachable bed", true);
         } else {
            double budget = KillAuraModule.init_D_9();
            boolean critical = MC.player.getHealth() + MC.player.getAbsorptionAmount() <= 6.0F;
            RotationMath.Rotation at = autismclient.util.SilentRotationManager.getCurrentRotation();
            BedMiningEstimator.Inner_StreamUtil follow = tracking == null
               ? BedMiningEstimator.init(this.route.first().pos(), this.route.eye(), 0.0F, nextEye)
               : tracking;
            Vec3 point = follow != null && follow.init_Vec3() != null ? follow.init_Vec3() : this.route.eye();
            RotationMath.Rotation goal = RotationMath.lookAt(point, nextEye);
            double turn = RotationMath.angularDistance(at == null ? RotationMath.ofPlayer(MC.player) : at, goal);
            double finish = this.init_D_6()
               + Math.ceil(Math.max(0.0, turn - 2.0) / 20.0)
               + Math.max(this.paceGuard.load(System.nanoTime()), Math.max(0, mining.autism$getDestroyDelay()));
            if (this.route.first().slot() != MC.player.getInventory().getSelectedSlot()) {
               finish += 2.0;
            }

            if (!Double.isFinite(budget) || this.route.init_Z() && BedBreachRouteSolver.fitsBudget(finish, budget, critical)) {
               this.routeTick = this.clientTick();
               this.statusText = this.route.first().init_Z() ? "Breaking bed" : "Opening bed";
               autismclient.util.SilentRotationManager.requestRotation(this.id(), 12, goal);
               if (!this.open()) {
                  this.stopMining();
               } else {
                  autismclient.util.SilentRotationManager.tick(this.id(), MC.player, 37.0F, 37.0F);
                  int slot = this.route.first().slot();
                  if (slot != MC.player.getInventory().getSelectedSlot()) {
                     this.stopMining();
                     if (this.pendingSlot != slot) {
                        this.pendingSlot = slot;
                        this.slotSwapAtNanos = System.nanoTime() + 12000000L;
                     }

                     if (System.nanoTime() >= this.slotSwapAtNanos && autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
                        try {
                           if (this.load_I_7() < 0) {
                              this.previousSlot = MC.player.getInventory().getSelectedSlot();
                           }

                           autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, slot);
                           this.load_I = this.clientTick();
                           this.update = slot;
                           this.previousHeld = MC.player.getMainHandItem().copy();
                           this.previousSlotLevel = MC.level;
                           this.sync = this.clientTick();
                        } finally {
                           autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
                        }
                     }
                  } else {
                     this.pendingSlot = -1;
                  }

                  ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
                  BlockHitResult hit = null;
                  if (wire.initialized() && this.load_I != this.clientTick() && slot == MC.player.getInventory().getSelectedSlot()) {
                     RotationMath.Rotation written = new RotationMath.Rotation(wire.currentYaw(), wire.currentPitch());
                     BlockHitResult before = this.raycastAlong(written);
                     this.write_Z = this.hitsRouteTarget(before);
                     if (this.write_Z) {
                        hit = before;
                     }
                  }

                  this.read_Z = this.write_Z || tracking == null || tracking.init_Z();
                  double[] lastStep = autismclient.util.SilentRotationManager.getLastStepDeltas();
                  boolean continuing = current != null && this.miningStarted && current.equals(this.miningPos);
                  if (continuing && slot == MC.player.getInventory().getSelectedSlot()) {
                     BlockHitResult visible = this.raycastAlong(
                        RotationMath.lookAt(this.route.eye(), MC.player.getEyePosition())
                     );
                     if (this.hitsRouteTarget(visible)) {
                        this.lastHit = visible;
                     }

                     if (!autismclient.util.SilentMiningCoordinator.requestMining(this.id(), this.lastHit)) {
                        this.stopMining();
                     }
                  } else if (hit != null && lastStep[0] <= 18.0 && lastStep[1] <= 18.0 && autismclient.util.SilentMiningCoordinator.requestMining(this.id(), hit)) {
                     this.miningStarted = true;
                     this.miningPos = hit.getBlockPos().immutable();
                     this.lastHit = hit;
                  } else {
                     this.stopMining();
                  }
               }
            } else {
               this.stopWithReason("Combat", false);
            }
         }
      }
   }

   private boolean isTargetBed(BedBlock block, DyeColor own) {
      return this.bool("own-bed") || own != null && block.getColor() != own;
   }

   private Vec3 init_Vec3_2() {
      if (!MC.player.isInWater() && !MC.player.isInLava() && !MC.player.getAbilities().flying && !MC.player.isFallFlying()) {
         autismclient.ai.engine.AiPhysics.MotionState state = autismclient.ai.engine.AiPhysics.MotionState.of(MC.player);
         autismclient.ai.engine.AiPhysics.MoveInput input = new autismclient.ai.engine.AiPhysics.MoveInput(
            (MC.options.keyUp.isDown() ? 1 : 0) - (MC.options.keyDown.isDown() ? 1 : 0),
            (MC.options.keyLeft.isDown() ? 1 : 0) - (MC.options.keyRight.isDown() ? 1 : 0),
            MC.options.keyJump.isDown(),
            MC.player.isSprinting(),
            MC.options.keyShift.isDown(),
            SilentMovementCoordinator.getMovementYaw(MC.player)
         );
         return BedTargetAimSolver.init(
            state, input, autismclient.ai.engine.AiPhysics.createLiveWorld(MC.level, MC.player), MC.player.getEyeHeight()
         );
      } else {
         return MC.player.getEyePosition();
      }
   }

   private BlockHitResult raycastAlong(RotationMath.Rotation aim) {
      Vec3 eye = MC.player.getEyePosition();
      return MC.level
         .clip(
            new ClipContext(
               eye, eye.add(Vec3.directionFromRotation(aim.pitch(), aim.yaw()).scale(MC.player.blockInteractionRange())), Block.OUTLINE, Fluid.NONE, MC.player
            )
         );
   }

   private boolean hitsRouteTarget(BlockHitResult hit) {
      return hit != null && hit.getType() == Type.BLOCK && this.route.first().pos().equals(hit.getBlockPos());
   }

   private void stopMining() {
      if (this.miningStarted) {
         this.paceGuard.init();
         autismclient.util.SilentMiningCoordinator.release(this.id());
         if (this.load_Z) {
            autismclient.util.SilentMiningCoordinator.init(this.miningPos);
         } else {
            autismclient.util.SilentMiningCoordinator.load(this.miningPos);
         }
      }

      this.miningStarted = false;
      this.miningPos = null;
      this.lastHit = null;
      this.write_Z = this.read_Z = false;
   }

   private void stopWithReason(String reason, boolean pump) {
      this.stopMining();
      this.pendingSlot = -1;
      this.statusText = reason;
      if (this.open()) {
         autismclient.util.SilentRotationManager.releaseOwner(this.id());
         if (pump && MC.player != null) {
            autismclient.util.SilentRotationManager.tick(this.id(), MC.player);
         }
      }
   }

   private void resetRoute() {
      this.stopMining();
      this.pendingSlot = -1;
      this.route = null;
      this.targetBed = null;
      this.routeTick = this.load_I = -2147483648;
   }

   @Override
   public void onDisable() {
      this.sync = this.clientTick();
      this.resetRoute();
      BedMiningEstimator.init();
      this.stopWithReason("Idle", false);
   }

   @Override
   public void onGameLeft() {
      this.resetRoute();
      this.clearSlotRestore();
      BedMiningEstimator.init();
      this.paceGuard.load();
      if (this.open()) {
         autismclient.util.SilentRotationManager.resetAll();
      }

      this.lastLevel = null;
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.open();
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && this.open() && MC.player != null) {
         autismclient.util.SilentRotationManager.tick(this.id(), MC.player);
      }
   }

   @Override
   public String info() {
      return this.statusText;
   }

   public BlockPos routeTargetPos() {
      return this.route == null ? null : this.route.first().pos();
   }

   public String hitFaceName() {
      return this.lastHit == null ? "" : this.lastHit.getDirection().name();
   }

   public double init_D_6() {
      return this.route == null
         ? -1.0
         : this.route.ticks()
            + Math.max(0.0, this.paceGuard.init_D(System.nanoTime()) - this.route.first().ticks());
   }
}
