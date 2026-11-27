package autismclient.util;

import java.util.Random;
import net.minecraft.client.player.LocalPlayer;

public final class SilentRotationManager {
   public static final float DEFAULT_MAX_STEP_DEGREES = 72.0F;
   public static final float RETURN_BASE_STEP_DEGREES = 37.0F;
   public static final float URGENT_RETURN_STEP_DEGREES = 37.0F;
   public static final float RELEASE_TOLERANCE_DEGREES = 2.0F;
   public static final int REQUEST_TTL_TICKS = 5;
   public static final int PRIORITY_CLUTCH = 50;
   public static final int PRIORITY_DANGER = 40;
   public static final int PRIORITY_BED_DEFENDER = 30;
   public static final int PRIORITY_SURROUND = 25;
   public static final int PRIORITY_ANCHOR_AURA = 20;
   public static final int PRIORITY_CRYSTAL_AURA = 18;
   public static final int PRIORITY_AUTO_TRAP = 15;
   public static final int PRIORITY_KILL_AURA = 10;
   public static final int PRIORITY_KB_DISPLACEMENT = 11;
   public static final int PRIORITY_BED_NUKER = 12;
   public static final int PRIORITY_AUTO_FARM = 5;
   public static final int PRIORITY_AI_ASSISTANT = 1;
   public static final String OWNER_DANGER = "danger";
   public static final String OWNER_BED_DEFENDER = "bed-defender";
   public static final String OWNER_SURROUND = "surround";
   public static final String OWNER_CRYSTAL_AURA = "crystal-aura";
   public static final String OWNER_ANCHOR_AURA = "anchor-aura";
   public static final String OWNER_AUTO_TRAP = "auto-trap";
   public static final String OWNER_KILL_AURA = "kill-aura";
   public static final String OWNER_KB_DISPLACEMENT = "kb-displacement";
   public static final String OWNER_AUTO_FARM = "auto-farm";
   public static final String OWNER_AI_ASSISTANT = "ai-assistant";
   private static final RotationStreamPlanner.Inner_TaskQueue streamState = new RotationStreamPlanner.Inner_TaskQueue();
   private static RotationMath.Rotation currentRotation = null;
   private static RotationMath.Rotation targetRotation = null;
   private static String activeOwnerId = null;
   private static String tickWinnerOwnerId = null;
   private static int tickWinnerPriority = -2147483648;
   private static int tickWinnerTick = -2147483648;
   private static int lastSteppedTick = -2147483648;
   private static final float PIN_QUIET_EPSILON_DEGREES = 0.05F;
   private static int remainingHoldTicks = 0;
   private static final int DEFAULT_RETURN_TIMEOUT_TICKS = 27;
   private static int remainingReturnTicks = 0;
   private static boolean returningToPlayer = false;
   private static float returnStepDegrees = 37.0F;
   private static int returnTimeoutTicks = 27;
   private static float lastReturnCameraYaw = 0.0F / 0.0F;
   private static float lastReturnCameraPitch = 0.0F / 0.0F;
   private static final Random RANDOM = new Random();
   private static HumanizedAimStepper humanizedStepper;
   private static String humanizedOwnerId;
   private static int humanizedRequestTick = -2147483648;
   private static double humanizedToleranceDegrees = 4.0;
   private static boolean usedHumanizedStep;
   private static int lastHumanizedTick = -2147483648;
   private static final double DEFAULT_ACCEL_UP = 18.0;
   private static final int HUMANIZED_RESET_GAP_TICKS = 10;
   private static final double RETURN_HUMANIZED_TOLERANCE = 2.5;
   private static final float FAST_RELEASE_MIN_CAMERA_DELTA = 4.0F;
   private static final float FAST_RELEASE_MAX_COMBINED_ANGLE = 38.0F;
   private static SilentRotationManager.Inner_DataUtil customHandStepper;
   private static String visibleAimOwnerId;
   private static String customHandOwnerId;
   private static int customHandRequestTick = -2147483648;
   private static double customHandAccelUp = 18.0;

   private SilentRotationManager() {
   }

   public static void requestKillAuraRotation(RotationMath.Rotation rotation) {
      requestRotation("kill-aura", 10, rotation);
   }

   public static void requestRotation(String ownerId, int priority, RotationMath.Rotation rotation) {
      if (ownerId != null && rotation != null) {
         int tick = AutismSessionState.getInstance().getClientTick();
         if (tick != tickWinnerTick) {
            tickWinnerTick = tick;
            tickWinnerOwnerId = null;
            tickWinnerPriority = -2147483648;
         }

         if (tickWinnerOwnerId == null || priority >= tickWinnerPriority && (priority != tickWinnerPriority || ownerId.equals(tickWinnerOwnerId))) {
            tickWinnerOwnerId = ownerId;
            tickWinnerPriority = priority;
            activeOwnerId = ownerId;
            targetRotation = rotation;
            remainingHoldTicks = 5;
            returningToPlayer = false;
         }
      }
   }

   public static boolean snapKillAuraRotation(RotationMath.Rotation rotation) {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      if (rotation != null && Float.isFinite(rotation.yaw()) && Float.isFinite(rotation.pitch())) {
         requestRotation("kill-aura", 10, rotation);
         if ("kill-aura".equals(activeOwnerId) && "kill-aura".equals(tickWinnerOwnerId)) {
            ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
            float yaw = ServerRotationTracker.getLastWireYaw();
            RotationMath.Rotation from = wire.initialized() ? new RotationMath.Rotation(Float.isFinite(yaw) ? yaw : wire.currentYaw(), wire.currentPitch()) : rotation;
            currentRotation = RotationMath.applyGcd(rotation, from);
            targetRotation = currentRotation;
            lastSteppedTick = AutismSessionState.getInstance().getClientTick();
            usedHumanizedStep = false;
            RotationStreamPlanner.init_V_ap_TaskQueue_3(streamState, currentRotation, 0.0, 0.0, RotationMath.gcdStep());
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean snapKbDisplacementRotation(RotationMath.Rotation rotation) {
      if (!AutismLiteVariant.enabled() && rotation != null) {
         ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
         if (!wire.initialized()) {
            return false;
         }

         RotationMath.Rotation from = new RotationMath.Rotation(ServerRotationTracker.getLastWireYaw(), wire.currentPitch());
         requestRotation("kb-displacement", 11, rotation);
         if (!"kb-displacement".equals(activeOwnerId)) {
            return false;
         }

         currentRotation = RotationMath.applyGcd(rotation, from);
         targetRotation = currentRotation;
         lastSteppedTick = AutismSessionState.getInstance().getClientTick();
         usedHumanizedStep = false;
         RotationStreamPlanner.init_V_ap_TaskQueue_3(
            streamState, currentRotation, RotationMath.angleDelta(currentRotation.yaw(), from.yaw()), currentRotation.pitch() - from.pitch(), RotationMath.gcdStep()
         );
         return true;
      } else {
         return false;
      }
   }

   public static void enableHumanizedAim(String ownerId, double toleranceDegrees) {
      humanizedOwnerId = ownerId;
      humanizedRequestTick = AutismSessionState.getInstance().getClientTick();
      humanizedToleranceDegrees = Math.max(0.5, toleranceDegrees);
   }

   private static boolean isHumanizedAimActive(String ownerId, int tick) {
      return ownerId != null && ownerId.equals(humanizedOwnerId) && humanizedRequestTick == tick;
   }

   public static void setCustomHandStepper(String ownerId, SilentRotationManager.Inner_DataUtil ownerHand) {
      setCustomHandStepper(ownerId, ownerHand, 18.0);
   }

   public static void setCustomHandStepper(String ownerId, SilentRotationManager.Inner_DataUtil ownerHand, double accelUp) {
      customHandStepper = ownerHand;
      customHandOwnerId = ownerId;
      customHandRequestTick = AutismSessionState.getInstance().getClientTick();
      customHandAccelUp = Math.max(18.0, accelUp);
   }

   private static boolean isCustomHandStepperActive(String ownerId, int tick) {
      return customHandStepper != null && ownerId != null && ownerId.equals(customHandOwnerId) && customHandRequestTick == tick;
   }

   private static RotationMath.Rotation stepWithCustomHand(float maxYawStep, float maxPitchStep) {
      double[] last = RotationStreamPlanner.init_DArr_ap_TaskQueue(streamState);
      double count = RotationMath.gcdStep();
      maxYawStep = (float)Math.max(maxYawStep, last[0] - 18.0 + count);
      maxPitchStep = (float)Math.max(maxPitchStep, last[1] - 18.0 + count);
      maxPitchStep = (float)Math.min(maxPitchStep, last[1] + 18.0);
      RotationMath.Rotation at = RotationStreamPlanner.init_ce_DataUtil_ap_TaskQueue(streamState);
      double[] want = customHandStepper.step(at, maxYawStep, maxPitchStep);
      usedHumanizedStep = true;
      lastHumanizedTick = -1073741824;
      return RotationStreamPlanner.init(streamState, want[0], want[1], maxYawStep, maxPitchStep, RotationMath.gcdStep(), customHandAccelUp, 18.0, CombatAimEngine.isPitchRestingStep(want), "kill-aura".equals(customHandOwnerId));
   }

   public static void setExactYawHints(float exactYaw, float exactClosestYaw) {
      RotationStreamPlanner.init_V_ap_TaskQueue(streamState, exactYaw, exactClosestYaw);
   }

   private static RotationMath.Rotation stepHumanized(
      RotationMath.Rotation goal, float maxYawStep, float maxPitchStep, double tolerance, int tick, boolean handBack
   ) {
      if (humanizedStepper == null) {
         humanizedStepper = HumanizedAimStepper.init_an();
      }

      if (!usedHumanizedStep || tick - lastHumanizedTick > 10) {
         humanizedStepper.init_V();
      }

      humanizedStepper.init_V_Z(handBack);
      lastHumanizedTick = tick;
      usedHumanizedStep = true;
      RotationMath.Rotation at = RotationStreamPlanner.init_ce_DataUtil_ap_TaskQueue(streamState);
      double[] want = humanizedStepper.init(goal.yaw(), goal.pitch(), at.yaw(), at.pitch(), tolerance, maxYawStep, maxPitchStep, true);
      return RotationStreamPlanner.init_ce_DataUtil_ap_TaskQueue(streamState, want[0], want[1], maxYawStep, maxPitchStep, RotationMath.gcdStep(), 18.0);
   }

   public static double[] getLastStepDeltas() {
      return RotationStreamPlanner.init_DArr_ap_TaskQueue(streamState);
   }

   public static String getActiveOwnerId() {
      return activeOwnerId;
   }

   public static void syncFromServerRotation() {
      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
      if (wire.initialized()) {
         float yaw = ServerRotationTracker.getLastWireYaw();
         if (!Float.isFinite(yaw)) {
            yaw = wire.currentYaw();
         }

         if (currentRotation == null || !(Math.abs(RotationMath.angleDelta(currentRotation.yaw(), yaw)) < 0.2) || !(Math.abs(currentRotation.pitch() - wire.currentPitch()) < 0.2)) {
            currentRotation = new RotationMath.Rotation(yaw, wire.currentPitch());
            RotationStreamPlanner.init_V_ap_TaskQueue_3(streamState, currentRotation, RotationMath.angleDelta(wire.currentYaw(), wire.previousYaw()), wire.currentPitch() - wire.previousPitch(), RotationMath.gcdStep());
         }
      }
   }

   public static void resetAll() {
      clearActiveState();
      tickWinnerOwnerId = null;
      tickWinnerPriority = -2147483648;
      tickWinnerTick = -2147483648;
      lastSteppedTick = -2147483648;
   }

   private static void clearActiveState() {
      currentRotation = null;
      targetRotation = null;
      activeOwnerId = null;
      visibleAimOwnerId = null;
      remainingHoldTicks = 0;
      remainingReturnTicks = 0;
      returningToPlayer = false;
      usedHumanizedStep = false;
      RotationStreamPlanner.init_V_ap_TaskQueue(streamState);
   }

   public static void releaseOwner(String ownerId) {
      if (activeOwnerId == null || activeOwnerId.equals(ownerId)) {
         if (isVisibleAimActive()) {
            clearActiveState();
         } else {
            targetRotation = null;
            remainingHoldTicks = 0;
            returningToPlayer = currentRotation != null;
            returnStepDegrees = 34.0F + RANDOM.nextFloat() * 3.0F;
            returnTimeoutTicks = 22 + RANDOM.nextInt(13);
            if (usedHumanizedStep) {
               returnTimeoutTicks = 45 + RANDOM.nextInt(15);
            }

            lastReturnCameraYaw = 0.0F / 0.0F;
            lastReturnCameraPitch = 0.0F / 0.0F;
         }
      }
   }

   static boolean shouldFastReleaseOnCameraMove(float gapDegrees, float cameraDeltaDegrees) {
      return cameraDeltaDegrees >= 4.0F && gapDegrees + cameraDeltaDegrees <= 38.0F;
   }

   public static boolean isReturningToPlayer() {
      return returningToPlayer && currentRotation != null;
   }

   public static boolean isVisibleAimActive() {
      return visibleAimOwnerId != null && visibleAimOwnerId.equals(activeOwnerId);
   }

   public static boolean hasActiveRotation() {
      return currentRotation != null;
   }

   public static RotationMath.Rotation getCurrentRotation() {
      return currentRotation;
   }

   public static void stepDangerRotation(LocalPlayer player, float step) {
      stepUrgentFixed("danger", player, step);
   }

   public static void stepUrgentFixed(String requester, LocalPlayer player, float step) {
      if (player != null && ("danger".equals(requester) || "clutch".equals(requester)) && requester.equals(activeOwnerId) && targetRotation != null && remainingHoldTicks > 0) {
         int tick = AutismSessionState.getInstance().getClientTick();
         if (lastSteppedTick != tick) {
            syncFromServerRotation();
            if (currentRotation == null) {
               currentRotation = RotationMath.ofPlayer(player);
            }

            currentRotation = RotationMath.stepQuantized(currentRotation, targetRotation, step, step, RotationMath.gcdStep());
            RotationStreamPlanner.init_V_ap_TaskQueue_ce_DataUtil(streamState, currentRotation);
            usedHumanizedStep = false;
            lastSteppedTick = tick;
            remainingHoldTicks--;
         }
      }
   }

   public static SilentRotationManager.Inner_StreamUtil selectUrgencyProfile(double remaining, int ticksLeft) {
      int n = Math.max(1, ticksLeft);
      double angle = Math.max(0.0, remaining);
      if ((!(angle <= 4.0) || n <= 1 && !(angle <= 1.0)) && !(n >= 2.0 + angle / 16.0)) {
         if (n >= 1.0 + angle / 30.0) {
            return new SilentRotationManager.Inner_StreamUtil(RotationStreamPlanner.Inner_StreamUtil.DANGER_RETURN, 37.0F, false);
         } else {
            return n >= 1.0 + angle / 68.0
               ? new SilentRotationManager.Inner_StreamUtil(RotationStreamPlanner.Inner_StreamUtil.TELLY_AIR_FLICK, 110.0F, false)
               : new SilentRotationManager.Inner_StreamUtil(RotationStreamPlanner.Inner_StreamUtil.STANDARD, (float)Math.min(180.0, Math.max(30.0, angle / n + 2.0)), true);
         }
      } else {
         return new SilentRotationManager.Inner_StreamUtil(RotationStreamPlanner.Inner_StreamUtil.STANDARD, 40.0F, false);
      }
   }

   public static void stepUrgentTimed(String requester, LocalPlayer player, int ticksLeft) {
      if (player != null && ("danger".equals(requester) || "clutch".equals(requester)) && requester.equals(activeOwnerId) && targetRotation != null && remainingHoldTicks > 0) {
         int tick = AutismSessionState.getInstance().getClientTick();
         if (lastSteppedTick != tick) {
            syncFromServerRotation();
            if (currentRotation == null) {
               currentRotation = RotationMath.ofPlayer(player);
            }

            if (!RotationStreamPlanner.init_Z_ap_TaskQueue(streamState)) {
               RotationStreamPlanner.init_V_ap_TaskQueue_ce_DataUtil(streamState, currentRotation);
            }

            double remaining = Math.max(
               Math.abs(RotationMath.angleDelta(targetRotation.yaw(), currentRotation.yaw())), Math.abs(targetRotation.pitch() - currentRotation.pitch())
            );
            SilentRotationManager.Inner_StreamUtil urgency = selectUrgencyProfile(remaining, ticksLeft);
            double gcd = RotationMath.gcdStep();
            if (urgency.directQuantized()) {
               currentRotation = RotationMath.stepQuantized(currentRotation, targetRotation, urgency.maxStepDegrees(), urgency.maxStepDegrees(), gcd);
               RotationStreamPlanner.init_V_ap_TaskQueue_ce_DataUtil(streamState, currentRotation);
            } else {
               currentRotation = RotationStreamPlanner.init_ce_DataUtil_ap_TaskQueue_2(
                  streamState, targetRotation, urgency.maxStepDegrees(), urgency.maxStepDegrees(), gcd, false, urgency.profile()
               );
            }

            usedHumanizedStep = false;
            lastSteppedTick = tick;
            remainingHoldTicks--;
         }
      }
   }

   public static void tick(String ownerId, LocalPlayer player) {
      tick(ownerId, player, 72.0F, 72.0F);
   }

   public static void tickPinQuiet(String ownerId, LocalPlayer player, boolean pinQuiet) {
      tickInternal(ownerId, player, 72.0F, 72.0F, RotationStreamPlanner.Inner_StreamUtil.STANDARD, pinQuiet);
   }

   public static void tick(String ownerId, LocalPlayer player, float maxYawStep, float maxPitchStep) {
      tick(ownerId, player, maxYawStep, maxPitchStep, RotationStreamPlanner.Inner_StreamUtil.STANDARD);
   }

   public static void tick(String ownerId, LocalPlayer player, float maxYawStep, float maxPitchStep, RotationStreamPlanner.Inner_StreamUtil profile) {
      tickInternal(ownerId, player, maxYawStep, maxPitchStep, profile, false);
   }

   private static void tickInternal(String ownerId, LocalPlayer player, float maxYawStep, float maxPitchStep, RotationStreamPlanner.Inner_StreamUtil profile, boolean pinQuiet) {
      if (player == null) {
         resetAll();
      } else if (activeOwnerId == null || activeOwnerId.equals(ownerId)) {
         int tick = AutismSessionState.getInstance().getClientTick();
         if (tick != lastSteppedTick) {
            lastSteppedTick = tick;
            RotationMath.Rotation playerRotation = RotationMath.ofPlayer(player);
            if (targetRotation != null && remainingHoldTicks > 0) {
               boolean visible = autismclient.modules.al.init_Module_Str(ownerId) instanceof SilentMovementCoordinator.Participant aimOwner && aimOwner.update_Z_9();
               if (visible
                  && isVisibleAimActive()
                  && !pinQuiet
                  && currentRotation != null
                  && (playerRotation.yaw() != currentRotation.yaw() || playerRotation.pitch() != currentRotation.pitch())) {
                  currentRotation = playerRotation;
                  RotationStreamPlanner.init_V_ap_TaskQueue_3(streamState, playerRotation, 0.0, 0.0, RotationMath.gcdStep());
               }

               if (!RotationStreamPlanner.init_Z_ap_TaskQueue(streamState)) {
                  RotationMath.Rotation seedFrom = currentRotation;
                  if (seedFrom == null) {
                     ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
                     float wireYaw = ServerRotationTracker.getLastWireYaw();
                     seedFrom = wire.initialized() ? new RotationMath.Rotation(Float.isFinite(wireYaw) ? wireYaw : wire.currentYaw(), wire.currentPitch()) : playerRotation;
                  }

                  RotationStreamPlanner.init_V_ap_TaskQueue_ce_DataUtil(streamState, seedFrom);
                  usedHumanizedStep = false;
               }

               if (!pinQuiet
                  || currentRotation == null
                  || (AutismLiteVariant.enabled() || !SilentMiningCoordinator.init(ownerId, currentRotation, targetRotation, streamState))
                     && (
                        !(Math.abs(RotationMath.angleDelta(currentRotation.yaw(), targetRotation.yaw())) <= 0.05F)
                           || !(Math.abs(currentRotation.pitch() - targetRotation.pitch()) <= 0.05F)
                     )) {
                  if (isCustomHandStepperActive(ownerId, tick)) {
                     currentRotation = stepWithCustomHand(maxYawStep, maxPitchStep);
                  } else if (isHumanizedAimActive(ownerId, tick)) {
                     currentRotation = stepHumanized(targetRotation, maxYawStep, maxPitchStep, humanizedToleranceDegrees, tick, false);
                  } else {
                     usedHumanizedStep = false;
                     currentRotation = RotationStreamPlanner.init_ce_DataUtil_ap_TaskQueue_2(
                        streamState, targetRotation, maxYawStep, maxPitchStep, RotationMath.gcdStep(), false, profile
                     );
                  }

                  applyVisiblePlayerAngles(player, visible);
                  remainingHoldTicks--;
               } else {
                  remainingHoldTicks--;
                  applyVisiblePlayerAngles(player, visible);
               }
            } else if (isVisibleAimActive()) {
               clearActiveState();
            } else {
               targetRotation = null;
               if (currentRotation == null) {
                  remainingReturnTicks = 0;
                  returningToPlayer = false;
               } else {
                  if (remainingReturnTicks <= 0) {
                     remainingReturnTicks = returnTimeoutTicks;
                  }

                  float cameraDelta = Float.isNaN(lastReturnCameraYaw) ? 0.0F : RotationMath.vectorAngleDegrees(playerRotation, new RotationMath.Rotation(lastReturnCameraYaw, lastReturnCameraPitch));
                  lastReturnCameraYaw = playerRotation.yaw();
                  lastReturnCameraPitch = playerRotation.pitch();
                  boolean releaseNow = shouldFastReleaseOnCameraMove(RotationMath.vectorAngleDegrees(currentRotation, playerRotation), cameraDelta);
                  RotationMath.Rotation next = releaseNow
                     ? playerRotation
                     : (
                        usedHumanizedStep && RotationStreamPlanner.init_Z_ap_TaskQueue(streamState)
                           ? stepHumanized(playerRotation, returnStepDegrees, returnStepDegrees, 2.5, tick, true)
                           : RotationStreamPlanner.init_ce_DataUtil_ap_TaskQueue(streamState, playerRotation, returnStepDegrees, returnStepDegrees, RotationMath.gcdStep(), false)
                     );
                  remainingReturnTicks--;
                  if (!releaseNow && !(RotationMath.vectorAngleDegrees(next, playerRotation) <= 2.0F) && remainingReturnTicks > 0) {
                     currentRotation = next;
                  } else {
                     float fixedYaw = currentRotation.yaw() + RotationMath.angleDelta(player.getYRot(), currentRotation.yaw());
                     player.setYRot(fixedYaw);
                     player.yBob = fixedYaw;
                     player.yBobO = fixedYaw;
                     clearActiveState();
                  }
               }
            }
         }
      }
   }

   private static void applyVisiblePlayerAngles(LocalPlayer player, boolean visible) {
      visibleAimOwnerId = visible && currentRotation != null ? activeOwnerId : null;
      if (visibleAimOwnerId != null) {
         player.setYRot(currentRotation.yaw());
         player.setXRot(currentRotation.pitch());
      }
   }

   public interface Inner_DataUtil {
      double[] step(RotationMath.Rotation var1, float var2, float var3);
   }

   public record Inner_StreamUtil(RotationStreamPlanner.Inner_StreamUtil profile, float maxStepDegrees, boolean directQuantized) {
   }
}
