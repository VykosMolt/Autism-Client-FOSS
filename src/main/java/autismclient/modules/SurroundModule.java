package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.util.AuditClientFixes;
import autismclient.util.AutismLiteVariant;
import autismclient.util.PerTickPlacementLock;
import autismclient.util.RemoteViewController;
import autismclient.util.RotationMath;
import autismclient.util.ServerRotationTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import autismclient.util.RegistryIdValidator;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class SurroundModule extends Module implements SilentMovementCoordinator.Participant {
   private static final Direction[] FACE_ORDER = new Direction[]{Direction.DOWN, Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
   private static final Direction[] HORIZONTALS = new Direction[]{Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH};
   private static final float init_F = 600.0F;
   private static final double init_D = 0.25;
   private static final int init_I = 4;
   private static final double load_D = 0.001;
   private static final double read_D = 0.001;
   private static final int load_I = 6;
   private static final int read_I = 5;
   private static final int write_I = 256;
   private static final long init_J = 2000000000L;
   private static final int update_I = 64;
   private final Map<Integer, SurroundModule.Inner_DataUtil> breakProgress = new ConcurrentHashMap<>();
   private final Set<BlockPos> init_Set = new HashSet<>();
   private final List<BlockPos> toPlace = new ArrayList<>();
   private final Set<BlockPos> recentPlacements = new LinkedHashSet<>();
   private boolean init_Z;
   private double startY;
   private double startX;
   private double startZ;
   private double flush_D;
   private double close_D;
   private boolean load_Z;
   private int lastTick = -2147483648;
   private int flush_I = -2147483648;
   private final autismclient.util.BlockPlacementPlanner.RayBudget rayBudget = new autismclient.util.BlockPlacementPlanner.RayBudget(4);
   private int previousSlot = -1;
   private int switchedToSlot = -1;
   private int switchBackDelay;
   private final KillAuraModule.TickCachedFlag init_aa_JsonUtil = new KillAuraModule.TickCachedFlag();
   private String raw;
   private Set<Block> allowedBlocks = Set.of();
   private BlockPos init_BlockPos;

   public SurroundModule() {
      super("surround", "Surround", ae.read, "Places blocks around your feet.");
      this.add(new BoolSetting("down", "Down", true).group("Features").description("Fill under the surround floor"));
      this.add(new ChoiceSetting("ring", "Ring", "Center", "Center", "Hitbox").group("Features").description("Ring around center or hitbox"));
      this.add(
         new BoolSetting("no-waste", "NoWaste", true)
            .group("Features")
            .visibleWhen(() -> "Hitbox".equals(this.choice("ring")))
            .description("Skip extra cells inside holes")
      );
      this.add(new BoolSetting("disable-on-y-change", "YChange", true).group("DisableOn").description("Turn off on Y change"));
      this.add(new BoolSetting("disable-on-xz-move", "XZMove", false).group("DisableOn").description("Turn off leaving start block"));
      this.add(new BoolSetting("disable-on-xz-speed", "XZSpeed", false).group("DisableOn").description("Turn off when moving fast"));
      this.add(new ChoiceSetting("filter-mode", "Filter", "Whitelist", "Whitelist", "Blacklist").group("Blocks").description("How the list is applied"));
      this.add(
         RegistryListSetting.blocks("blocks", "Blocks", "minecraft:obsidian|minecraft:crying_obsidian|minecraft:ender_chest")
            .group("Blocks")
            .description("Surround material")
      );
      this.add(new IntSetting("aim-speed", "Aim Speed", 3, 1, 5, 1).group("Placing").description("Turn and place speed"));
      this.add(new BoolSetting("switch-back", "Switch Back", true).group("Placing").description("Return to your old slot"));
      this.add(new BoolSetting("protect", "Protect", true).group("Protect").description("Refill blocks others are mining"));
      this.add(
         new IntSetting("min-destroy-progress", "Min Destroy Progress", 4, 0, 9, 1)
            .group("Protect")
            .unit("stage")
            .visibleWhen(() -> this.bool("protect"))
            .description("Stage to start reacting at")
      );
      this.add(
         new BoolSetting("extra-layer", "Extra Layer", true)
            .group("Protect")
            .visibleWhen(() -> this.bool("protect"))
            .description("Second ring around mined cells")
      );
      this.add(
         new BoolSetting("extra-layer-corners", "Corners", false)
            .group("Protect")
            .visibleWhen(() -> this.bool("protect") && this.bool("extra-layer"))
            .description("Also fill the diagonals")
      );
      this.add(new BoolSetting("force-extra-layer", "Force Extra Layer", false).group("Protect").description("Extra layer on every cell"));
   }

   @Override
   public void onEnable() {
      this.resetState();
   }

   @Override
   public void onDisable() {
      this.resetState();
      autismclient.util.SilentRotationManager.releaseOwner(this.id());
   }

   @Override
   public void onGameLeft() {
      this.resetState();
      if (this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.resetAll();
      }
   }

   private void resetState() {
      this.init_Z = false;
      this.load_Z = false;
      this.toPlace.clear();
      this.recentPlacements.clear();
      this.init_Set.clear();
      this.breakProgress.clear();
      this.lastTick = -2147483648;
      this.previousSlot = -1;
      this.switchedToSlot = -1;
      this.switchBackDelay = 0;
      autismclient.util.InventorySlotArbiter.releaseAllClaims(this.id());
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return autismclient.util.SilentRotationManager.hasActiveRotation() && this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId());
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && MC != null && MC.player != null && this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.tick(this.id(), MC.player);
      }
   }

   @Override
   public String info() {
      int remaining = this.toPlace.size();
      return remaining <= 0 ? "" : Integer.toString(remaining);
   }

   public static boolean init_Z_27() {
      if (!(al.init_Module_Str("surround") instanceof SurroundModule surround)) {
         return false;
      } else {
         int age = AutismSessionState.getInstance().getClientTick() - surround.flush_I;
         return age >= 0 && age <= 5 && "surround".equals(autismclient.util.SilentRotationManager.getActiveOwnerId());
      }
   }

   @Override
   public boolean isSilentMovementActive() {
      boolean enabled = this.isEnabled();
      return !SilentMovementCoordinator.isSafeWalkActive() && (autismclient.util.SilentRotationManager.isReturningToPlayer() || enabled && this.canAct());
   }

   private boolean canAct() {
      return MC != null
         && MC.player != null
         && MC.level != null
         && MC.getConnection() != null
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && !aD.init_Z()
         && !FreecamController.isActive()
         && !RemoteViewController.isActive()
         && (AutismLiteVariant.enabled() || !PkgCls_a.init_Z())
         && !MacroExecutor.isRunning()
         && !MC.player.isDeadOrDying()
         && !MC.player.isSpectator()
         && !MC.player.isUsingItem()
         && !MC.player.isHandsBusy()
         && !SilentMovementCoordinator.isSafeWalkActive()
         && !ScaffoldModule.update_Z_5()
         && !AutoTotemModule.init_Z_6()
         && !AutoArmorModule.isClickPending();
   }

   private boolean sync_Z_7() {
      return this.init_aa_JsonUtil.valueFor(AutismSessionState.getInstance().getClientTick(), this::flush_Z_6);
   }

   private boolean flush_Z_6() {
      if (MC != null && MC.player != null) {
         ItemStack mainHand = MC.player.getMainHandItem();
         return KillAuraModule.isThrowable(mainHand) ? true : mainHand.isEmpty() && KillAuraModule.isThrowable(MC.player.getOffhandItem());
      } else {
         return false;
      }
   }

   private void releaseRotation() {
      if (MC != null && MC.player != null && this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.releaseOwner(this.id());
         autismclient.util.SilentRotationManager.tick(this.id(), MC.player);
      }
   }

   @Override
   public boolean onPacketReceive(Packet<?> packet) {
      if (packet instanceof ClientboundBlockDestructionPacket destruction) {
         if (!this.isEnabled() || !this.bool("protect")) {
            return false;
         }

         if (AuditClientFixes.isLocalPlayerId(destruction.getId())) {
            return false;
         }

         int progress = destruction.getProgress();
         if (progress >= 0 && progress <= 9) {
            if (this.breakProgress.size() >= 256 && !this.breakProgress.containsKey(destruction.getId())) {
               return false;
            }

            this.breakProgress.put(destruction.getId(), new SurroundModule.Inner_DataUtil(destruction.getPos().asLong(), progress, System.nanoTime()));
            return false;
         } else {
            this.breakProgress.remove(destruction.getId());
            return false;
         }
      } else {
         return false;
      }
   }

   private void updateBreakProgress() {
      this.init_Set.clear();
      if (!this.bool("protect")) {
         this.breakProgress.clear();
      } else {
         long now = System.nanoTime();
         int minStage = this.integer("min-destroy-progress");
         this.breakProgress.values().removeIf(entryx -> now - entryx.load() > 2000000000L);

         for (SurroundModule.Inner_DataUtil entry : this.breakProgress.values()) {
            if (entry.progress() >= minStage) {
               this.init_Set.add(BlockPos.of(entry.posKey()));
            }
         }
      }
   }

   @Override
   public void preMovementTick() {
      if (!AutismLiteVariant.enabled()) {
         if (MC != null && MC.player != null && MC.level != null && MC.getConnection() != null) {
            if (!this.init_Z) {
               this.captureStart();
            }

            this.flush_V_6();
            double x = MC.player.getX();
            double y = MC.player.getY();
            double z = MC.player.getZ();
            String reason = this.disableReason(x, y, z);
            this.flush_D = x;
            this.close_D = z;
            this.load_Z = true;
            if (reason != null) {
               this.disableWithToggleMessage("Surround disabled: " + reason + ".");
            } else {
               this.updateBreakProgress();
               this.collectTargets();
               this.sync_V_7();
            }
         } else {
            this.toPlace.clear();
            this.releaseRotation();
         }
      }
   }

   private void captureStart() {
      this.startY = MC.player.getY();
      BlockPos block = MC.player.blockPosition();
      this.startX = block.getX() + 0.5;
      this.startZ = block.getZ() + 0.5;
      this.load_Z = false;
      this.init_Z = true;
   }

   private String disableReason(double x, double y, double z) {
      if (this.bool("disable-on-y-change") && y != this.startY) {
         return "your Y changed";
      }

      if (!this.bool("disable-on-xz-move") || !(Math.abs(x - this.startX) > 0.5) && !(Math.abs(z - this.startZ) > 0.5)) {
         if (this.bool("disable-on-xz-speed") && this.load_Z) {
            double dx = x - this.flush_D;
            double dz = z - this.close_D;
            if (dx * dx + dz * dz >= 0.25) {
               return "you moved too fast";
            }
         }

         return null;
      } else {
         return "you left the block";
      }
   }

   private void collectTargets() {
      this.toPlace.clear();
      AABB box = MC.player.getBoundingBox();
      int feetY = Mth.floor(box.minY + 0.001);
      List<BlockPos> hole = this.surroundCells(box, feetY);
      Set<BlockPos> seen = new HashSet<>(hole);
      List<BlockPos> ring = new ArrayList<>();
      List<BlockPos> down = new ArrayList<>();
      List<BlockPos> extra = new ArrayList<>();
      boolean addDown = this.bool("down");
      boolean forceExtra = this.bool("force-extra-layer");
      boolean extraLayer = this.bool("protect") && this.bool("extra-layer");
      boolean corners = extraLayer && this.bool("extra-layer-corners");
      Direction[] order = this.faceOrder();

      for (BlockPos holePos : hole) {
         for (Direction direction : order) {
            BlockPos pos = holePos.relative(direction);
            if (seen.add(pos)) {
               if (direction == Direction.DOWN) {
                  down.add(pos);
                  if (addDown) {
                     addUnique(down, seen, holePos.below(2));
                  }
               } else {
                  ring.add(pos);
                  if (forceExtra || extraLayer && this.init_Set.contains(pos)) {
                     addUnique(extra, seen, pos.relative(direction));
                     addUnique(extra, seen, pos.above());
                     if (corners) {
                        addUnique(extra, seen, pos.relative(direction.getClockWise()));
                     }
                  }
               }
            }
         }
      }

      this.addPlaceable(ring);
      this.addPlaceable(down);
      this.addPlaceable(extra);
   }

   private Direction[] faceOrder() {
      if (this.integer("aim-speed") <= 1) {
         return FACE_ORDER;
      }

      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
      float fromYaw = wire.initialized() ? wire.currentYaw() : MC.player.getYRot();
      int start = 0;
      float best = 3.4028235E38F;

      for (int i = 0; i < HORIZONTALS.length; i++) {
         float turn = Math.abs(RotationMath.angleDelta(HORIZONTALS[i].toYRot(), fromYaw));
         if (turn < best) {
            best = turn;
            start = i;
         }
      }

      Direction[] order = new Direction[HORIZONTALS.length + 1];
      order[0] = Direction.DOWN;

      for (int i = 0; i < HORIZONTALS.length; i++) {
         order[i + 1] = HORIZONTALS[(start + i) % HORIZONTALS.length];
      }

      return order;
   }

   private static void addUnique(List<BlockPos> target, Set<BlockPos> seen, BlockPos pos) {
      if (seen.add(pos)) {
         target.add(pos);
      }
   }

   private void addPlaceable(List<BlockPos> group) {
      for (BlockPos pos : group) {
         if (this.recentPlacements.contains(pos) && MC.level.getBlockState(pos).canBeReplaced()) {
            this.toPlace.add(pos);
         }
      }

      for (BlockPos pos : group) {
         if (!this.recentPlacements.contains(pos) && MC.level.getBlockState(pos).canBeReplaced()) {
            this.toPlace.add(pos);
         }
      }
   }

   private List<BlockPos> surroundCells(AABB box, int feetY) {
      BlockPos feet = new BlockPos(Mth.floor((box.minX + box.maxX) * 0.5), feetY, Mth.floor((box.minZ + box.maxZ) * 0.5));
      if (!"Hitbox".equals(this.choice("ring"))) {
         return List.of(feet);
      }

      if (this.bool("no-waste") && this.isSurrounded(feet)) {
         return List.of(feet);
      }

      int minColX = Mth.floor(box.minX + 0.001);
      int maxColX = Mth.floor(box.maxX - 0.001);
      int minColZ = Mth.floor(box.minZ + 0.001);
      int maxColZ = Mth.floor(box.maxZ - 0.001);
      List<BlockPos> columns = new ArrayList<>(4);

      for (int columnX = minColX; columnX <= maxColX; columnX++) {
         for (int columnZ = minColZ; columnZ <= maxColZ; columnZ++) {
            columns.add(new BlockPos(columnX, feetY, columnZ));
         }
      }

      return columns.isEmpty() ? List.of(feet) : columns;
   }

   private boolean isSurrounded(BlockPos feet) {
      for (Direction direction : FACE_ORDER) {
         float resistance = MC.level.getBlockState(feet.relative(direction)).getBlock().getExplosionResistance();
         if (resistance < 600.0F) {
            return false;
         }
      }

      return true;
   }

   private void sync_V_7() {
      if (!this.canAct()) {
         this.releaseRotation();
      } else {
         int slot = this.findBlockSlot();
         if (this.sync_Z_7()) {
            this.releaseRotation();
            if (slot >= 0 && slot != MC.player.getInventory().getSelectedSlot()) {
               this.init_V_I_9(slot);
            }
         } else if (BlinkPacketManager.isHoldingActionsOnly()) {
            this.releaseRotation();
         } else if (!this.toPlace.isEmpty() && slot >= 0) {
            ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
            RotationMath.Rotation wireRotation = wire.initialized() ? new RotationMath.Rotation(wire.currentYaw(), wire.currentPitch()) : null;
            RotationMath.Rotation from = wireRotation != null ? wireRotation : RotationMath.ofPlayer(MC.player);
            ItemStack material = MC.player.getInventory().getItem(slot);
            SurroundModule.Inner_StreamUtil plan = this.findPlacement(from, material, null);
            if (plan == null) {
               this.releaseRotation();
               this.restoreSlot();
            } else {
               this.switchBackDelay = 0;
               boolean slotReady = slot == MC.player.getInventory().getSelectedSlot();
               BlockHitResult hit = wireRotation == null ? null : this.hitFor(plan, wireRotation);
               boolean fire = hit != null && slotReady && !BedDefenderModule.init_Z_10() && this.isNewTick();
               boolean aimed;
               if (fire) {
                  SurroundModule.Inner_StreamUtil next = this.findPlacement(from, material, plan.cell());
                  aimed = this.requestRotation(next != null ? next.goalRotation() : plan.goalRotation(), true);
               } else {
                  aimed = this.requestRotation(plan.goalRotation(), false);
               }

               if (fire && aimed) {
                  RotationMath.Rotation outgoing = SilentMovementCoordinator.getRenderRotation(MC.player);
                  if (outgoing != null && !(Math.abs(outgoing.pitch() - wireRotation.pitch()) > 0.05F)) {
                     this.placeAt(plan, hit);
                  }
               } else if (!slotReady) {
                  this.init_V_I_9(slot);
               }
            }
         } else {
            this.releaseRotation();
            this.restoreSlot();
         }
      }
   }

   private boolean requestRotation(RotationMath.Rotation goal, boolean holdPitch) {
      autismclient.util.SilentRotationManager.requestRotation(this.id(), 25, goal);
      this.flush_I = AutismSessionState.getInstance().getClientTick();
      if (!this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         return false;
      }

      int speed = this.integer("aim-speed");
      autismclient.util.SilentRotationManager.tick(this.id(), MC.player, maxStepForSpeed(speed), holdPitch ? 0.0F : maxStepForSpeed(speed), profileForSpeed(speed));
      return true;
   }

   private static autismclient.util.RotationStreamPlanner.MotionProfile profileForSpeed(int speed) {
      autismclient.util.RotationStreamPlanner.MotionProfile var10000;
      switch (speed) {
         case 2:
            var10000 = autismclient.util.RotationStreamPlanner.MotionProfile.SURROUND_FAST_2;
            break;
         case 3:
            var10000 = autismclient.util.RotationStreamPlanner.MotionProfile.SURROUND_FAST_3;
            break;
         case 4:
            var10000 = autismclient.util.RotationStreamPlanner.MotionProfile.SURROUND_FAST_4;
            break;
         case 5:
            var10000 = autismclient.util.RotationStreamPlanner.MotionProfile.SURROUND_FAST_5;
            break;
         default:
            var10000 = autismclient.util.RotationStreamPlanner.MotionProfile.STANDARD;
      }

      return var10000;
   }

   private static float maxStepForSpeed(int speed) {
      float var10000;
      switch (speed) {
         case 3:
            var10000 = 90.0F;
            break;
         case 4:
            var10000 = 135.0F;
            break;
         case 5:
            var10000 = 180.0F;
            break;
         default:
            var10000 = 72.0F;
      }

      return var10000;
   }

   private SurroundModule.Inner_StreamUtil findPlacement(RotationMath.Rotation from, ItemStack material, BlockPos skip) {
      double reach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
      Vec3 eye = MC.player.getEyePosition();
      AABB playerBox = MC.player.getBoundingBox();
      Vec3 delta = MC.player.getDeltaMovement();
      BlockState placed = material.getItem() instanceof BlockItem blockItem ? blockItem.getBlock().defaultBlockState() : null;
      autismclient.util.BlockPlacementPlanner.Query request = new autismclient.util.BlockPlacementPlanner.Query(
            null, eye, reach, autismclient.util.BlockPlacementPlanner.blockItemValidator(material, MC.player, InteractionHand.MAIN_HAND)
         )
         .withFromRotation(from)
         .withSneaking(MC.player.isShiftKeyDown())
         .withLeadEye(eye.add(delta))
         .withRayBudget(this.rayBudget);

      for (BlockPos cell : this.toPlace) {
         if (!cell.equals(skip) && !this.isCellBlocked(cell, placed, playerBox, delta)) {
            this.rayBudget.reset(4);
            autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.plan(request.withTargetCell(cell));
            if (candidate != null) {
               return new SurroundModule.Inner_StreamUtil(cell, candidate);
            }
         }
      }

      return null;
   }

   private boolean isCellBlocked(BlockPos cell, BlockState placed, AABB playerBox, Vec3 delta) {
      AABB box = new AABB(cell);
      if (!box.intersects(playerBox) && !box.intersects(playerBox.move(delta)) && !box.intersects(playerBox.move(delta.scale(-1.0)))) {
         return placed != null
            ? !MC.level.isUnobstructed(placed, cell, CollisionContext.empty())
            : !MC.level.getEntities(MC.player, box, EntitySelector.NO_SPECTATORS).isEmpty();
      } else {
         return true;
      }
   }

   private BlockHitResult hitFor(SurroundModule.Inner_StreamUtil plan, RotationMath.Rotation rotation) {
      double reach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
      Vec3 eye = MC.player.getEyePosition();
      autismclient.util.BlockPlacementPlanner.Query gate = new autismclient.util.BlockPlacementPlanner.Query(
            plan.cell(),
            eye,
            reach,
            autismclient.util.BlockPlacementPlanner.blockItemValidator(MC.player.getItemInHand(InteractionHand.MAIN_HAND), MC.player, InteractionHand.MAIN_HAND)
         )
         .withSneaking(MC.player.isShiftKeyDown());
      return autismclient.util.BlockPlacementPlanner.verifyWithRotation(plan.plan(), rotation, eye, reach, gate);
   }

   private boolean isNewTick() {
      return AutismSessionState.getInstance().getClientTick() != this.lastTick;
   }

   private void placeAt(SurroundModule.Inner_StreamUtil plan, BlockHitResult hit) {
      InteractionHand hand = InteractionHand.MAIN_HAND;
      if (!al.init_Z_HitResult(hit, hand, this.id()) && autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
         try {
            if (!autismclient.util.ClientInteractionOverride.queueUseBlock(hit, hand)) {
               return;
            }

            if (!PerTickPlacementLock.init(this.id())) {
               autismclient.util.ClientInteractionOverride.clearAll();
               return;
            }

            this.markTick();
            this.init_BlockPos = plan.cell();
         } finally {
            autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
         }
      }
   }

   private void flush_V_6() {
      if (this.init_BlockPos != null) {
         BlockPos cell = this.init_BlockPos;
         this.init_BlockPos = null;
         if (!MC.level.getBlockState(cell).canBeReplaced()) {
            this.markPlaced(cell);
         }
      }
   }

   private void markTick() {
      this.lastTick = AutismSessionState.getInstance().getClientTick();
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      return this.lastTick == AutismSessionState.getInstance().getClientTick();
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return hitResult instanceof EntityHitResult && this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && autismclient.util.SilentRotationManager.hasActiveRotation();
   }

   private void markPlaced(BlockPos cell) {
      this.toPlace.remove(cell);
      BlockPos immutable = cell.immutable();
      this.recentPlacements.remove(immutable);
      this.recentPlacements.add(immutable);

      while (this.recentPlacements.size() > 64) {
         this.recentPlacements.remove(this.recentPlacements.iterator().next());
      }
   }

   private int findBlockSlot() {
      Inventory inventory = MC.player.getInventory();
      int selected = inventory.getSelectedSlot();
      int best = -1;

      for (int slot = 0; slot < 9; slot++) {
         if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, this.id())) {
            ItemStack stack = inventory.getItem(slot);
            if (this.isUsableBlock(stack)) {
               if (slot == selected) {
                  return slot;
               }

               if (best < 0 || stack.getCount() > inventory.getItem(best).getCount()) {
                  best = slot;
               }
            }
         }
      }

      return best;
   }

   private void init_V_I_9(int slot) {
      if (!BedDefenderModule.init_Z_10() && autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
         try {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (this.previousSlot < 0 && this.bool("switch-back")) {
               this.previousSlot = selected;
            }

            autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, slot);
            this.switchedToSlot = slot;
         } finally {
            autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
         }
      }
   }

   private void restoreSlot() {
      if (this.previousSlot >= 0) {
         if (!this.bool("switch-back")) {
            this.previousSlot = -1;
            this.switchedToSlot = -1;
         } else if (this.switchedToSlot >= 0 && MC.player.getInventory().getSelectedSlot() != this.switchedToSlot) {
            this.previousSlot = -1;
            this.switchedToSlot = -1;
         } else if (++this.switchBackDelay >= 6 && autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
            try {
               autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, this.previousSlot);
               this.previousSlot = -1;
               this.switchedToSlot = -1;
               this.switchBackDelay = 0;
            } finally {
               autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
            }
         }
      }
   }

   private boolean isUsableBlock(ItemStack stack) {
      if (!(stack != null && !stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem)) {
         return false;
      }

      if (!stack.isItemEnabled(MC.level.enabledFeatures())) {
         return false;
      }

      boolean listed = this.getAllowedBlocks().contains(blockItem.getBlock());
      return "Whitelist".equals(this.choice("filter-mode")) == listed;
   }

   private Set<Block> getAllowedBlocks() {
      String raw = this.value("blocks");
      if (raw.equals(this.raw)) {
         return this.allowedBlocks;
      }

      Set<Block> blocks = new HashSet<>();

      for (String entry : this.list("blocks")) {
         Identifier identifier = Identifier.tryParse(RegistryIdValidator.normalize(entry));
         if (identifier != null) {
            BuiltInRegistries.BLOCK.getOptional(identifier).ifPresent(blocks::add);
         }
      }

      this.raw = raw;
      this.allowedBlocks = Set.copyOf(blocks);
      return this.allowedBlocks;
   }

   private record Inner_DataUtil(long posKey, int progress, long load) {
   }

   private record Inner_StreamUtil(BlockPos cell, autismclient.util.BlockPlacementPlanner.PlacementPlan plan) {
      RotationMath.Rotation goalRotation() {
         return this.plan.aim().goalRotation();
      }
   }
}
