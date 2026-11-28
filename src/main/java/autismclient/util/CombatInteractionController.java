package autismclient.util;

import autismclient.mixin.accessor.AutismKeyMappingAccessor;
import autismclient.modules.Module;
import autismclient.modules.f;
import autismclient.util.macro.MacroExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class CombatInteractionController {
   private static final int init_I = 20;
   private static final long init_J = 1250000000L;
   private static LocalPlayer trackedPlayer;
   private static ClientPacketListener trackedConnection;
   private static ClientLevel trackedLevel;
   private static RotationMath.Rotation silentRotation;
   private static ItemStack mainHandSnapshot = ItemStack.EMPTY;
   private static ItemStack offHandSnapshot = ItemStack.EMPTY;
   private static int selectedSlotSnapshot;
   private static int captureTick;
   private static int write_I = -2147483648;
   private static int update_I = -2147483648;
   private static int sync_I;
   private static boolean init_Z;
   private static boolean load_Z;
   private static boolean read_Z;
   private static boolean write_Z;
   private static boolean update_Z;
   private static boolean sync_Z;
   private static boolean flush;
   private static boolean close;
   private static HitResult capturedHit;
   private static BlockState capturedBlockState;
   private static Vec3 hitOffset;
   private static RotationMath.Rotation capturedRotation;
   private static RotationMath.Rotation aimRotation;
   private static long captureNanos;
   private static final CombatAimEngine.AimFrame aimState = new CombatAimEngine.AimFrame();

   private static boolean close() {
      Module aura = autismclient.modules.al.init_Module_Str("kill-aura");
      return aura != null && "Legit".equals(aura.value("interactions"));
   }

   public static boolean init_Z() {
      return sync_Z && !read();
   }

   private CombatInteractionController() {
   }

   private static boolean open() {
      Module aura = autismclient.modules.al.init_Module_Str("kill-aura");
      return aura != null && aura.isEnabled() && "kill-aura".equals(SilentRotationManager.getActiveOwnerId()) && SilentRotationManager.hasActiveRotation();
   }

   public static boolean load() {
      Module aura = autismclient.modules.al.init_Module_Str("kill-aura");
      return aura != null && aura.isEnabled() && "Block".equals(aura.value("interactions"));
   }

   private static boolean canInteract(Minecraft mc) {
      return mc.player != null
         && mc.level != null
         && mc.gameMode != null
         && mc.getConnection() != null
         && !mc.gameMode.isDestroying()
         && !BlockAttackGraceWindow.init(mc)
         && mc.getCameraEntity() == mc.player
         && !mc.player.isDeadOrDying()
         && !mc.player.isSpectator()
         && mc.gui.screen() == null
         && mc.gui.overlay() == null
         && !autismclient.modules.aD.init_Z()
         && !autismclient.modules.FreecamController.isActive()
         && !RemoteViewController.isActive()
         && !MacroExecutor.isRunning()
         && (AutismLiteVariant.enabled() || !autismclient.util.g.PkgCls_a.init_Z())
         && !f.load()
         && !SilentMovementCoordinator.isSafeWalkActive()
         && !autismclient.modules.BlinkPacketManager.isBlinking()
         && !autismclient.modules.AutoTotemModule.init_Z_6()
         && !autismclient.modules.AutoArmorModule.init_Z_20();
   }

   private static boolean isUseKeyPhysicallyDown(Minecraft mc) {
      return mc.options != null && KeyMappingControl.of(mc.options.keyUse).autism$physicalForBody();
   }

   private static AutismKeyMappingAccessor useKeyAccessor(Minecraft mc) {
      return (AutismKeyMappingAccessor)mc.options.keyUse;
   }

   public static boolean read() {
      Minecraft mc = Minecraft.getInstance();
      return mc.options != null && useKeyAccessor(mc).autism$clickCount() > 0 && !ClientInteractionOverride.ownsUseInput();
   }

   public static boolean write() {
      Minecraft mc = Minecraft.getInstance();
      int tick = AutismSessionState.getInstance().getClientTick();
      if (open() && canInteract(mc) && !load()) {
         if (trackedPlayer != null && (trackedPlayer != mc.player || trackedConnection != mc.getConnection() || trackedLevel != mc.level)) {
            reset();
         }

         if (autismclient.modules.KillAuraModule.init_Z_31()) {
            reset();
            return false;
         }

         boolean pressed = isUseKeyPhysicallyDown(mc);
         boolean edge = read();
         if (sync_Z) {
            if (pressed && !edge) {
               return false;
            }

            reset();
         }

         if (edge && !init_Z) {
            init_Z = true;
            sync_I = tick;
         }

         if (init_Z && tick - sync_I > 20) {
            update_V(mc);
            return false;
         }

         if (pressed || init_Z || load_Z && mc.player.isUsingItem()) {
            if (load_Z && !isSameHeldState(mc) && !pressed) {
               update_V(mc);
               return false;
            }

            trackedPlayer = mc.player;
            trackedConnection = mc.getConnection();
            trackedLevel = mc.level;
            load_Z = true;
            write_I = tick;
            autismclient.modules.KillAuraModule.init_V_27();
            if (!trackedPlayer.isUsingItem() || pressed) {
               update_Z = false;
            } else if (!update_Z) {
               update_Z = true;
               silentRotation = null;
               capturedHit = null;
            }

            if (silentRotation == null || !isSameHeldState(mc) || flush && !trackedPlayer.isUsingItem()) {
               if (close()) {
                  captureUseState(mc);
               } else {
                  aimRotation = init_ce_DataUtil(mc);
                  captureTick = tick;
               }

               selectedSlotSnapshot = trackedPlayer.getInventory().getSelectedSlot();
               mainHandSnapshot = trackedPlayer.getMainHandItem().copy();
               offHandSnapshot = trackedPlayer.getOffhandItem().copy();
               flush = false;
            }

            if (close()) {
               if (!flush && !update_Z(mc) || update_Z && init(tick) || !trackedPlayer.isUsingItem() && init(tick)) {
                  update_V(mc);
                  return false;
               }

               if (!flush && !update_Z && RotationMath.angularDistance(capturedRotation, RotationMath.ofPlayer(trackedPlayer)) > 8.0F) {
                  update_V(mc);
                  return false;
               }

               if (trackedPlayer.isUsingItem() && !update_Z) {
                  capturedRotation = RotationMath.ofPlayer(trackedPlayer);
                  capturedHit = null;
                  aimRotation = capturedRotation;
               } else if (capturedHit != null && capturedHit.getType() != Type.MISS) {
                  aimRotation = RotationMath.lookAt(targetPoint(), trackedPlayer.getEyePosition());
               }

               ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
               float yaw = ServerRotationTracker.getLastWireYaw();
               RotationMath.Rotation written = wire.initialized() ? new RotationMath.Rotation(Float.isFinite(yaw) ? yaw : wire.currentYaw(), wire.currentPitch()) : null;
               boolean ready = tick > captureTick && written != null && init_Z(mc, written);
               write_V(mc);
               if (!autismclient.modules.KillAuraModule.init_Z_ce_DataUtil(ready ? written : aimRotation, aimState, ready)) {
                  reset();
                  return false;
               }

               if (!ready) {
                  close = false;
               }
            } else {
               if (!update_Z && silentRotation != null && !load(mc, silentRotation)) {
                  if (!pressed && !trackedPlayer.isUsingItem()) {
                     update_V(mc);
                     return false;
                  }

                  aimRotation = init_ce_DataUtil(mc);
                  captureTick = tick;
               }

               if (update_Z && tick - captureTick > 20) {
                  update_V(mc);
                  return false;
               }

               if (!SilentRotationManager.snapKillAuraRotation(aimRotation)) {
                  reset();
                  return false;
               }
            }

            silentRotation = SilentRotationManager.getCurrentRotation();
            return true;
         } else {
            reset();
            return false;
         }
      } else {
         reset();
         return false;
      }
   }

   public static boolean update() {
      Minecraft mc = Minecraft.getInstance();
      return load_Z
         && trackedPlayer == mc.player
         && trackedConnection == mc.getConnection()
         && trackedLevel == mc.level
         && write_I == AutismSessionState.getInstance().getClientTick()
         && open();
   }

   public static boolean sync() {
      return open();
   }

   private static boolean poll() {
      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
      return silentRotation != null
         && wire.initialized()
         && Math.abs(RotationMath.angleDelta(wire.currentYaw(), silentRotation.yaw())) < 0.001F
         && Math.abs(wire.currentPitch() - silentRotation.pitch()) < 0.001F
         && AutismSessionState.getInstance().getClientTick() > captureTick;
   }

   public static void tick(Minecraft mc) {
      if (mc.options != null && open() && canInteract(mc)) {
         if (autismclient.modules.KillAuraModule.init_Z_31()) {
            sync_Z = false;
         } else {
            AutismKeyMappingAccessor key = useKeyAccessor(mc);
            if (sync_Z) {
               if (!read()) {
                  key.autism$setClickCount(0);
                  return;
               }

               sync_Z = false;
            }

            if (close() && update() && !flush && !update_Z(mc)) {
               update_V(mc);
            } else if (!update()) {
               if (!load() && (key.autism$clickCount() > 0 || isUseKeyPhysicallyDown(mc))) {
                  trackedPlayer = mc.player;
                  trackedConnection = mc.getConnection();
                  trackedLevel = mc.level;
                  if (!init_Z) {
                     sync_I = AutismSessionState.getInstance().getClientTick();
                  }

                  init_Z = true;
                  key.autism$setClickCount(0);
               }
            } else {
               if (key.autism$clickCount() > 0) {
                  if (!init_Z) {
                     sync_I = AutismSessionState.getInstance().getClientTick();
                  }

                  init_Z = true;
                  key.autism$setClickCount(0);
               }

               if (poll() && isSameHeldState(mc) && (update_Z || init_Z(mc, silentRotation))) {
                  if (init_Z) {
                     key.autism$setClickCount(1);
                  }
               } else {
                  KeyMappingControl.of(mc.options.keyUse).autism$simulatePress(mc.player.isUsingItem());
                  key.autism$setClickCount(0);
                  write_Z = true;
               }
            }
         }
      }
   }

   public static void releaseUse(Minecraft mc) {
      if (write_Z && mc.options != null) {
         KeyMappingControl.of(mc.options.keyUse).autism$resetPressedState();
      }

      write_Z = false;
   }

   public static boolean init_Z(Minecraft mc) {
      if (!read_Z
         && open()
         && !ClientInteractionOverride.ownsUseInput()
         && !autismclient.modules.KillAuraModule.init_Z_31()
         && !autismclient.modules.ScaffoldModule.init_Z_26()
         && !f.load()
         && !autismclient.modules.aD.init_Z()
         && !autismclient.modules.FreecamController.isActive()
         && !RemoteViewController.isActive()) {
         AutismSessionState state = AutismSessionState.getInstance();
         return !state.hasEntityCaptureCallback() && !state.hasBlockCaptureCallback();
      } else {
         return false;
      }
   }

   public static CombatInteractionController.Inner_DataUtil frame(Minecraft mc) {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (!load() && !sync_Z && canInteract(mc) && update_I != tick) {
         if (!close() || update_Z(mc) && !init(tick)) {
            if (update() && poll() && isSameHeldState(mc) && init_Z(mc, silentRotation)) {
               HitResult hit = raycastAt(mc, silentRotation);
               CombatInteractionController.Inner_DataUtil frame = new CombatInteractionController.Inner_DataUtil(
                  trackedPlayer, mc.hitResult, trackedPlayer.getYRot(), trackedPlayer.getXRot()
               );
               trackedPlayer.setYRot(silentRotation.yaw());
               trackedPlayer.setXRot(silentRotation.pitch());
               mc.hitResult = hit;
               read_Z = true;
               init_Z = false;
               update_I = tick;
               flush = true;
               return frame;
            }

            if (!init_Z) {
               sync_I = tick;
            }

            init_Z = true;
            return null;
         } else {
            update_V(mc);
            return null;
         }
      } else {
         return null;
      }
   }

   public static boolean flush() {
      return read_Z;
   }

   private static boolean isSameHeldState(Minecraft mc) {
      return trackedPlayer == mc.player
         && selectedSlotSnapshot == trackedPlayer.getInventory().getSelectedSlot()
         && ItemStack.isSameItemSameComponents(mainHandSnapshot, trackedPlayer.getMainHandItem())
         && ItemStack.isSameItemSameComponents(offHandSnapshot, trackedPlayer.getOffhandItem());
   }

   private static void captureUseState(Minecraft mc) {
      capturedRotation = RotationMath.ofPlayer(trackedPlayer);
      capturedHit = trackedPlayer.isUsingItem() ? null : raycastAt(mc, capturedRotation);
      hitOffset = capturedHit == null
         ? null
         : (capturedHit instanceof EntityHitResult entity ? entity.getLocation().subtract(entity.getEntity().position()) : capturedHit.getLocation());
      capturedBlockState = capturedHit instanceof BlockHitResult block && block.getType() == Type.BLOCK ? mc.level.getBlockState(block.getBlockPos()) : null;
      aimRotation = capturedRotation;
      captureTick = AutismSessionState.getInstance().getClientTick();
      captureNanos = System.nanoTime();
      close = true;
   }

   private static boolean init(int tick) {
      return captureNanos != 0L && (tick - captureTick > 20 || System.nanoTime() - captureNanos > 1250000000L);
   }

   private static Vec3 targetPoint() {
      return capturedHit instanceof EntityHitResult hit ? hit.getEntity().position().add(hitOffset) : hitOffset;
   }

   private static boolean update_Z(Minecraft mc) {
      if (capturedHit != null && capturedHit.getType() != Type.MISS) {
         if (capturedHit instanceof EntityHitResult entity
            && (entity.getEntity().isRemoved() || entity.getEntity().level() != mc.level || !entity.getEntity().isPickable())) {
            return false;
         } else if (capturedHit instanceof BlockHitResult block && !mc.level.getBlockState(block.getBlockPos()).equals(capturedBlockState)) {
            return false;
         } else {
            Vec3 eye = trackedPlayer.getEyePosition();
            Vec3 point = targetPoint();
            double reach = capturedHit.getType() == Type.BLOCK ? trackedPlayer.blockInteractionRange() : trackedPlayer.entityInteractionRange();
            return point != null && !(eye.distanceToSqr(point) > (reach - 0.035) * (reach - 0.035)) ? init(raycastAt(mc, RotationMath.lookAt(point, eye))) : false;
         }
      } else {
         return true;
      }
   }

   private static boolean init(HitResult hit) {
      if (capturedHit != null && hit != null && capturedHit.getType() == hit.getType()) {
         if (capturedHit instanceof EntityHitResult expected && hit instanceof EntityHitResult actual) {
            return expected.getEntity() == actual.getEntity();
         } else {
            return capturedHit instanceof BlockHitResult expected && hit instanceof BlockHitResult actual
               ? expected.getBlockPos().equals(actual.getBlockPos()) && expected.getDirection() == actual.getDirection()
               : capturedHit.getType() == Type.MISS;
         }
      } else {
         return false;
      }
   }

   private static boolean init_Z(Minecraft mc, RotationMath.Rotation at) {
      if (!close()) {
         return load(mc, at);
      } else {
         return capturedHit != null && capturedHit.getType() != Type.MISS
            ? init(raycastAt(mc, at))
            : RotationMath.angularDistance(aimRotation, at) <= Math.max(0.2, RotationMath.gcdStep() * 1.5) && (capturedHit == null || raycastAt(mc, at).getType() == Type.MISS);
      }
   }

   private static void write_V(Minecraft mc) {
      Vec3 eye = trackedPlayer.getEyePosition();
      Vec3 point = capturedHit != null && capturedHit.getType() != Type.MISS
         ? targetPoint()
         : eye.add(Vec3.directionFromRotation(aimRotation.pitch(), aimRotation.yaw()).scale(3.0));
      AABB box = capturedHit instanceof EntityHitResult entity
         ? entity.getEntity().getBoundingBox()
         : (
            capturedHit instanceof BlockHitResult block && block.getType() == Type.BLOCK
               ? new AABB(block.getBlockPos())
               : new AABB(point, point).inflate(0.1)
         );
      aimState.setTarget(eye, box, point);
      aimState.targetYaw = aimRotation.yaw();
      aimState.targetPitch = aimRotation.pitch();
      aimState.yawHalfWidth = Math.max(0.25, Math.min(2.5, aimState.yawHalfWidth));
      aimState.pitchUpHalfWidth = aimState.pitchDownHalfWidth = Math.max(0.25, Math.min(1.5, aimState.yawHalfWidth));
      aimState.allowExcursion = false;
      aimState.clampInsideBox = true;
      aimState.compensateObserverMotion = !close;
      aimState.allowPromptFlick = true;
      aimState.instantPromptFlick = true;
      aimState.prioritizeStrike = true;
      aimState.ticksUntilAttack = 0.0;
      aimState.comboCount = 0;
      aimState.inAttackWindow = false;
      aimState.gcdStep = RotationMath.gcdStep();
      aimState.maxAccelUp = 18.0;
      aimState.forceWhiff = false;
      aimState.legacyNoCooldown = LegacyCombatDetector.init(trackedPlayer);
      aimState.targetChanged = close;
   }

   private static void update_V(Minecraft mc) {
      boolean held = isUseKeyPhysicallyDown(mc);
      if (mc.options != null) {
         useKeyAccessor(mc).autism$setClickCount(0);
      }

      reset();
      trackedPlayer = mc.player;
      trackedConnection = mc.getConnection();
      trackedLevel = mc.level;
      sync_Z = held;
   }

   private static RotationMath.Rotation init_ce_DataUtil(Minecraft mc) {
      RotationMath.Rotation camera = RotationMath.ofPlayer(mc.player);
      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
      if (!wire.initialized()) {
         return camera;
      }

      float yaw = ServerRotationTracker.getLastWireYaw();
      RotationMath.Rotation from = new RotationMath.Rotation(Float.isFinite(yaw) ? yaw : wire.currentYaw(), wire.currentPitch());
      RotationMath.Rotation rounded = RotationMath.applyGcd(camera, from);
      if (load(mc, rounded)) {
         return rounded;
      }

      RotationMath.Rotation best = rounded;
      double bestDistance = 1.0 / 0.0;
      float step = (float)RotationMath.gcdStep();

      for (int x = -1; x <= 1; x++) {
         for (int y = -1; y <= 1; y++) {
            if (x != 0 || y != 0) {
               RotationMath.Rotation candidate = RotationMath.applyGcd(new RotationMath.Rotation(rounded.yaw() + x * step, rounded.pitch() + y * step), from);
               double distance = RotationMath.angularDistance(camera, candidate);
               if (distance < bestDistance && load(mc, candidate)) {
                  best = candidate;
                  bestDistance = distance;
               }
            }
         }
      }

      return best;
   }

   private static boolean load(Minecraft mc, RotationMath.Rotation at) {
      HitResult wanted = raycastAt(mc, RotationMath.ofPlayer(mc.player));
      HitResult actual = raycastAt(mc, at);
      if (wanted.getType() != actual.getType()) {
         return false;
      } else if (wanted instanceof BlockHitResult a && actual instanceof BlockHitResult b && wanted.getType() == Type.BLOCK) {
         return a.getBlockPos().equals(b.getBlockPos()) && a.getDirection() == b.getDirection();
      } else {
         return wanted instanceof EntityHitResult a && actual instanceof EntityHitResult b
            ? a.getEntity() == b.getEntity() && !a.getEntity().isRemoved()
            : RotationMath.angularDistance(at, RotationMath.ofPlayer(mc.player)) <= Math.max(0.15, RotationMath.gcdStep());
      }
   }

   private static HitResult raycastAt(Minecraft mc, RotationMath.Rotation at) {
      Vec3 eye = mc.player.getEyePosition();
      Vec3 look = Vec3.directionFromRotation(at.pitch(), at.yaw());
      double blockReach = mc.player.blockInteractionRange();
      double entityReach = mc.player.entityInteractionRange();
      double reach = Math.max(blockReach, entityReach);
      Vec3 end = eye.add(look.scale(reach));
      BlockHitResult block = mc.level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, mc.player));
      double obstruction = block.getType() == Type.BLOCK ? eye.distanceToSqr(block.getLocation()) : reach * reach;
      EntityHitResult entity = ProjectileUtil.getEntityHitResult(
         mc.player,
         eye,
         end,
         mc.player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0),
         target -> !target.isSpectator() && target.isPickable(),
         Math.min(obstruction, entityReach * entityReach)
      );
      if (entity != null && eye.distanceToSqr(entity.getLocation()) <= entityReach * entityReach) {
         return entity;
      } else {
         return block.getType() == Type.BLOCK && obstruction <= blockReach * blockReach
            ? block
            : BlockHitResult.miss(end, Direction.getApproximateNearest(look.x, look.y, look.z), mc.player.blockPosition());
      }
   }

   public static void reset() {
      load_Z = false;
      init_Z = false;
      silentRotation = null;
      trackedPlayer = null;
      trackedConnection = null;
      trackedLevel = null;
      update_Z = false;
      sync_Z = false;
      capturedHit = null;
      hitOffset = null;
      aimRotation = null;
      capturedRotation = null;
      capturedBlockState = null;
      captureNanos = 0L;
      flush = false;
      mainHandSnapshot = offHandSnapshot = ItemStack.EMPTY;
      update_I = -2147483648;
      write_I = -2147483648;
   }

   public record Inner_DataUtil(LocalPlayer player, HitResult hit, float yaw, float load) {
      public void init(Minecraft mc) {
         this.player.setYRot(this.yaw);
         this.player.setXRot(this.load);
         if (mc.player == this.player) {
            mc.hitResult = this.hit;
         }

         CombatInteractionController.read_Z = false;
      }
   }
}
