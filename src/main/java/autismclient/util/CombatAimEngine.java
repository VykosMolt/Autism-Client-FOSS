package autismclient.util;

import java.util.Locale;
import java.util.Random;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class CombatAimEngine {
   static final double ANCHOR_REACH_MARGIN = 0.06;
   static final double NEAR_ANCHOR_REACH_GRACE = 0.6;
   private static final double CURVE_ARC_PITCH_FACTOR = 0.25;
   private static final double MS_PER_TICK = 50.0;
   private static final double MAX_DECEL_STEP_DEG = 16.5;
   private static final double SMOOTHSTEP_TIME_EXPONENT = 0.82;
   private static final double MIN_FORCED_PITCH_STEP_DEG = 0.13;
   private static final double SWEEP_GCD_THRESHOLD = 0.5;
   private static final double FITTS_TIME_SCALE = 0.42;
   private static final double PROMPT_FLICK_DURATION_SCALE = 0.7;
   static final double PITCH_HARD_CLAMP_FACTOR = 0.7;
   private static final double PITCH_SOFT_CLAMP_FACTOR = 0.6;
   private static final double PITCH_FOLLOW_BASE = 0.4;
   private static final double PITCH_FOLLOW_MAX = 0.9;
   static final double PITCH_SLACK_RAMP_TICKS = 4.0;
   private static final double FLICK_HALF_WIDTH_MULTIPLIER = 3.2;
   private static final double FLICK_MIN_DISTANCE_DEG = 18.0;
   private static final double STATE_RECONCILE_CLAMP_DEG = 0.75;
   private static final double STRIKE_BAND_MAX_FACTOR = 0.84;
   private static final int STRIKE_WINDOW_TICKS = 3;
   private static final double PITCH_REST_START_CHANCE = 0.25;
   private static final double PITCH_REST_END_CHANCE = 0.3;
   private static final double STRAFE_PITCH_REST_CHANCE = 0.12;
   private static final double LATERAL_MOTION_THRESHOLD_DEG = 3.0;
   private static final double BOUNDED_FLAT_STREAK_DECAY = 0.25;
   private static final double UNBOUNDED_FLAT_STREAK_LIMIT = 2.0;
   private static final double FAST_REST_MAX_YAW_DEG = 12.0;
   private static final double STEADY_YAW_HOLD_CHANCE = 0.15;
   private static final double STEADY_YAW_HOLD_MAX_YAW_DEG = 6.0;
   private static final double STEADY_YAW_HOLD_MAX_PITCH_DEG = 1.8;
   private static final double STEADY_YAW_HOLD_MIN_SHIFT_DEG = 1.0;
   private static final double SOFT_PITCH_CAP_DEG = 0.48;
   private static final double FAST_YAW_SOFT_PITCH_THRESHOLD_DEG = 2.9;
   private static final double UNUSED_AIM_CONST_A = 2.2;
   private static final double TRAILING_SIDE_BIAS_CHANCE = 0.82;
   private static final int SAMPLE_WINDOW_SIZE = 41;
   private static final int EXCURSION_MIN_LOCK_TICKS = 20;
   private static final int EXCURSION_URGENT_LOCK_TICKS = 33;
   private static final int CONSISTENT_TRACKING_STREAK_LIMIT = 8;
   private static final double LONG_TERM_ERROR_RESET_DEG = 4.0;
   private static final double UNUSED_AIM_CONST_B = 3.5;
   private static final double UNUSED_AIM_CONST_C = 12.5;
   private final Random random;
   private final HumanHandProfile handProfile;
   private final double basePursuitGain;
   private final double deadzoneMultiplier;
   private boolean aimInitialized;
   private double currentYaw;
   private double currentPitch;
   private boolean hasPreviousRealizedLook;
   private double lastRealizedYaw;
   private boolean hasTargetCenterHistory;
   private double lastTargetCenterYaw;
   private double lastTargetCenterPitch;
   private double smoothedTargetYawVel;
   private double smoothedTargetPitchVel;
   private double pursuitGain;
   private double targetYawOffset;
   private double sessionYawBias;
   private boolean clampOffsetInsideBox;
   private boolean aimingInsideStrikeBand;
   private double pitchOffset;
   private int postMoveCooldownTicks;
   private CombatAimEngine.AimMove activeMove;
   private double lastSpeedScale = 1.0;
   private double excursionTargetOffset;
   private double forcedPitchDirection = 1.0;
   private double lastRealizedPitch;
   private double lastRealizedPitchStep;
   private double lastStrafeYawBias;
   private double lastTargetHalfWidth = 1.0;
   private boolean pitchResting;
   private double flatYawStreak;
   private double sweepPitchGoal = 0.0 / 0.0;
   private double sweepPitchStepSpeed = 3.0;
   private int sweepHoldTicks;
   private int swingsSinceMiss;
   private double missFraction;
   private int nextMissSwingThreshold = 2147483647;
   private boolean allowMidComboMiss;
   private int missWidthTier;
   private boolean hasSampledLook;
   private double lastSampledYaw;
   private double lastSampledPitch;
   private final double[] recentYawSpeeds = new double[41];
   private final double[] recentYawErrors = new double[41];
   private int sampleCount;
   private int sampleWriteIndex;
   private int lockStreakTicks;
   private int longTermTightStreak;
   private int consistentDirectionScore;
   private int lastErrorSign = -1;
   private double lastSampledError;

   public static double computePitchStepCap(double ridingCap, double outsideBox, boolean ready, double roll) {
      double base = ready ? Math.max(ridingCap, Math.min(18.0, outsideBox + ridingCap)) : ridingCap;
      return base * (0.45 + 0.55 * clamp(roll, 0.0, 1.0));
   }

   public CombatAimEngine(HumanHandProfile hand, Random random) {
      this.handProfile = hand;
      this.random = random;
      this.basePursuitGain = clamp(hand.poll() - 0.02, 0.76, 0.92);
      this.deadzoneMultiplier = 0.8 + 0.35 * extractTraitFraction(hand.push());
      this.pursuitGain = this.basePursuitGain;
   }

   public static CombatAimEngine createDefault() {
      Random random = new Random();
      return new CombatAimEngine(HumanHandProfile.shared().forSession(random), random);
   }

   public void reset() {
      this.aimInitialized = false;
      this.hasPreviousRealizedLook = false;
      this.flatYawStreak = 0.0;
      this.hasTargetCenterHistory = false;
      this.smoothedTargetYawVel = this.smoothedTargetPitchVel = 0.0;
      this.activeMove = null;
      this.postMoveCooldownTicks = 0;
      this.hasSampledLook = false;
      this.sampleCount = 0;
      this.sampleWriteIndex = 0;
      this.lockStreakTicks = 0;
      this.longTermTightStreak = 0;
      this.consistentDirectionScore = 0;
      this.lastErrorSign = -1;
   }

   public double getPreferredAimHeightOffset() {
      return 1.22 + 0.2 * extractTraitFraction(this.handProfile.update());
   }

   public double[] computeStep(CombatAimEngine.AimFrame f, double atYaw, double atPitch, double maxYaw, double maxPitch) {
      if (this.lastSpeedScale != f.speedScale) {
         this.activeMove = null;
         this.lastSpeedScale = f.speedScale;
      }

      this.clampOffsetInsideBox = f.clampInsideBox;
      boolean boundedRests = f.allowBoundedRests && !f.legacyNoCooldown;
      double realizedYawStep = this.hasPreviousRealizedLook ? Math.abs(wrapDegrees(atYaw - this.lastRealizedYaw)) : 0.0;
      double realizedPitchStep = this.hasPreviousRealizedLook ? atPitch - this.lastRealizedPitch : 0.0;
      boolean pitchRepeating = this.hasPreviousRealizedLook && realizedPitchStep != 0.0 && Math.abs(realizedPitchStep - this.lastRealizedPitchStep) < 0.001;
      if (this.hasPreviousRealizedLook) {
         this.flatYawStreak = realizedYawStep > 2.75 && realizedPitchStep == 0.0 ? this.flatYawStreak + 1.0 : Math.max(0.0, this.flatYawStreak - (boundedRests ? 0.25 : 0.75));
      }

      this.lastRealizedPitchStep = realizedPitchStep;
      this.lastRealizedYaw = atYaw;
      this.lastRealizedPitch = atPitch;
      this.hasPreviousRealizedLook = true;
      if (this.aimInitialized && !f.targetChanged) {
         this.currentYaw = atYaw + clamp(wrapDegrees(this.currentYaw - atYaw), -0.75, 0.75);
         this.currentPitch = atPitch + clamp(this.currentPitch - atPitch, -0.75, 0.75);
         if (f.compensateObserverMotion && this.hasTargetCenterHistory) {
            this.currentYaw = this.currentYaw + f.observerYawShift;
            if (!usesBoundedPitchMode(f)) {
               this.currentPitch = this.currentPitch + f.observerPitchShift;
            }

            this.lastTargetCenterYaw = this.lastTargetCenterYaw + f.observerYawShift;
            this.lastTargetCenterPitch = this.lastTargetCenterPitch + f.observerPitchShift;
         }
      } else {
         this.currentYaw = atYaw;
         this.currentPitch = atPitch;
         this.aimInitialized = true;
         this.hasTargetCenterHistory = false;
         this.smoothedTargetYawVel = this.smoothedTargetPitchVel = 0.0;
         this.activeMove = null;
         this.postMoveCooldownTicks = 0;
         this.pitchOffset = 0.0;
         this.pursuitGain = this.basePursuitGain;
         this.sessionYawBias = (0.06 + 0.1 * this.random.nextDouble()) * (this.random.nextBoolean() ? 1.0 : -1.0) * Math.max(0.8, f.yawHalfWidth);
      }

      double carried = this.hasTargetCenterHistory ? wrapDegrees(this.currentYaw - this.lastTargetCenterYaw) : 0.0 / 0.0;
      double expected = this.hasTargetCenterHistory ? carried - this.smoothedTargetYawVel : 0.0 / 0.0;
      double centerYaw = this.currentYaw + wrapDegrees(f.targetYaw + this.sessionYawBias - this.currentYaw);
      this.updateTargetVelocity(centerYaw, f.targetPitch);
      double halfWidth = Math.max(0.8, f.yawHalfWidth);
      this.lastStrafeYawBias = f.strafeYawBias;
      this.lastTargetHalfWidth = halfWidth;
      double error = this.currentYaw - centerYaw;
      if (Double.isNaN(expected)) {
         expected = error;
         carried = error;
      }

      boolean pitchForced = false;
      if (usesBoundedPitchMode(f)) {
         double slack = f.pitchSlackDeg > 0.0 && !f.legacyNoCooldown && !f.forceWhiff && !this.isObserverStrafingFast(f) ? f.pitchSlackDeg * clamp((f.ticksUntilAttack - 3.0) / 4.0, 0.0, 1.0) : 0.0;
         double kept = this.currentPitch - f.targetPitch;
         double follow = 0.4 + 0.5 * this.random.nextDouble();
         double softUp = 0.6 * f.pitchUpHalfWidth + slack;
         double softDown = 0.6 * f.pitchDownHalfWidth + slack;
         if (kept < -softUp) {
            kept += (-softUp - kept) * follow;
         } else if (kept > softDown) {
            kept -= (kept - softDown) * follow;
         }

         kept = clamp(kept, -0.7 * f.pitchUpHalfWidth - slack, 0.7 * f.pitchDownHalfWidth + slack);
         pitchForced = Math.abs(f.targetPitch + kept - atPitch) > Math.abs(this.currentPitch - atPitch) + 1.0E-6;
         this.currentPitch = f.targetPitch + kept;
         this.pitchOffset = clamp(kept, -0.45 * f.pitchUpHalfWidth, 0.45 * f.pitchDownHalfWidth);
      }

      double pitchError = this.currentPitch - f.targetPitch;
      boolean strike = f.ticksUntilAttack <= 3.0;
      if (f.targetChanged) {
         this.targetYawOffset = this.pickTargetYawOffset(halfWidth, strike, this.pickPreferredOffsetSign());
      }

      this.pursuitGain = this.pursuitGain + (0.15 * (this.basePursuitGain - this.pursuitGain) + 0.025 * this.nextClampedGaussian());
      this.pursuitGain = clamp(this.pursuitGain, this.basePursuitGain - 0.08, Math.min(1.0, this.basePursuitGain + 0.08));
      double pursuit = strike ? Math.max(this.pursuitGain, 0.86) : this.pursuitGain;
      if (this.activeMove != null && this.activeMove.isExcursion && !f.allowExcursion) {
         this.activeMove = null;
      }

      if (f.prioritizeStrike && strike && !f.forceWhiff) {
         if (this.activeMove != null && (this.activeMove.isExcursion || this.activeMove.isWhiff)) {
            this.activeMove = null;
         }

         this.postMoveCooldownTicks = 0;
      }

      if (this.activeMove != null && !this.activeMove.isExcursion && strike && !this.activeMove.isStrike) {
         this.activeMove = null;
      }

      if (this.activeMove != null && !this.activeMove.isExcursion && !this.activeMove.isStrike && f.allowExcursion && this.shouldStartExcursion(f, true)) {
         this.activeMove = null;
      }

      boolean whiffNow = f.forceWhiff;
      if (this.activeMove != null && !this.activeMove.isExcursion && whiffNow && (this.activeMove.isStrike || !this.activeMove.isWhiff)) {
         this.activeMove = null;
      }

      if (this.activeMove == null && this.postMoveCooldownTicks > 0 && !f.forceWhiff) {
         this.postMoveCooldownTicks--;
      }

      if (this.activeMove == null) {
         double distance = usesBoundedPitchMode(f) ? Math.abs(error) : Math.hypot(error, pitchError);
         if (distance > Math.max(18.0, 3.2 * halfWidth)) {
            boolean prompt = f.targetChanged && f.allowPromptFlick;
            this.targetYawOffset = this.pickTargetYawOffset(halfWidth, true, this.pickPreferredOffsetSign());
            if (prompt) {
               this.targetYawOffset = clamp(this.targetYawOffset, -0.45 * halfWidth, 0.45 * halfWidth);
            }

            this.aimingInsideStrikeBand = true;
            double flickDuration = prompt && f.instantPromptFlick ? 1.0 : this.computeFittsFlickTicks(distance, halfWidth) * (prompt ? 0.7 : 1.0);
            double flickGain = 0.92 + 0.06 * this.random.nextDouble();
            this.startMove(f, carried, pitchError, this.targetYawOffset, flickDuration, prompt ? 1.0 : flickGain, false, maxYaw, maxPitch);
            this.activeMove.isStrike = true;
         } else if (!whiffNow || strike && !f.prioritizeStrike) {
            if (f.allowExcursion && (!strike || this.isExcursionUrgent()) && this.shouldStartExcursion(f, false)) {
               double side = Math.abs(error) > 0.5 ? Math.signum(error) : (this.random.nextBoolean() ? 1.0 : -1.0);
               double target = side * this.excursionTargetOffset;
               this.startMove(f, carried, pitchError, target, this.computeShortMoveTicks(Math.abs(target - carried)), 1.0, true, maxYaw, maxPitch);
            } else if (strike) {
               this.planStrikeCorrection(f, error, carried, pitchError, halfWidth, pursuit, maxYaw, maxPitch);
            } else if (this.postMoveCooldownTicks <= 0 && (this.aimingInsideStrikeBand && this.isTargetMovingFast() || this.isTrackingTooConsistent() || Math.abs(error - this.targetYawOffset) > this.computeDeadzoneThreshold(halfWidth))) {
               this.targetYawOffset = this.pickTargetYawOffset(halfWidth, !this.isTargetMovingFast(), this.pickPreferredOffsetSign());
               this.aimingInsideStrikeBand = !this.isTargetMovingFast();
               this.startMove(
                  f,
                  carried,
                  pitchError,
                  this.targetYawOffset,
                  this.computeShortMoveTicks(Math.abs(this.targetYawOffset - carried)),
                  0.85 + 0.3 * this.random.nextDouble(),
                  false,
                  maxYaw,
                  maxPitch
               );
            }
         } else {
            double side = Math.abs(error) > 0.3 ? Math.signum(error) : (this.random.nextBoolean() ? 1.0 : -1.0);
            this.targetYawOffset = side * this.computeMissYawOffset(f);
            this.aimingInsideStrikeBand = false;
            this.startMove(f, carried, pitchError, this.targetYawOffset, this.missWidthTier == 0 ? 1.0 : 2.0, 1.0, false, maxYaw, maxPitch);
            this.activeMove.isWhiff = true;
         }
      }

      double startYaw = this.currentYaw;
      if (this.activeMove != null) {
         this.advanceActiveMove(expected);
      } else {
         this.currentYaw = this.currentYaw + pursuit * this.smoothedTargetYawVel;
      }

      if (!usesBoundedPitchMode(f)) {
         this.currentPitch = this.currentPitch + pursuit * this.smoothedTargetPitchVel;
      }

      boolean sweep = f.gcdStep > 0.5;
      if (this.activeMove == null || this.activeMove.pitchTravel == 0.0) {
         if (sweep) {
            double top = Math.max(-0.82 * f.pitchUpHalfWidth, -8.0);
            double bottom = Math.min(0.82 * f.pitchDownHalfWidth, 10.0);
            if (Double.isNaN(this.sweepPitchGoal)) {
               this.sweepPitchGoal = this.random.nextBoolean() ? top : bottom;
            }

            if (Math.abs(this.sweepPitchGoal - this.pitchOffset) <= this.sweepPitchStepSpeed) {
               this.pitchOffset = this.sweepPitchGoal;
               if (this.sweepHoldTicks-- <= 0) {
                  boolean upNext = this.sweepPitchGoal > 0.5 * (top + bottom);
                  this.sweepPitchGoal = upNext ? top + 1.2 * this.random.nextDouble() : bottom - 1.2 * this.random.nextDouble();
                  this.sweepPitchStepSpeed = 3.0 + 1.0 * this.random.nextDouble();
                  this.sweepHoldTicks = 2 + this.random.nextInt(3);
               }
            }

            this.sweepPitchGoal = clamp(this.sweepPitchGoal, top, bottom);
            double push = this.sweepPitchStepSpeed * this.samplePitchJitterScale();
            this.pitchOffset = this.pitchOffset + clamp(this.sweepPitchGoal - this.pitchOffset, -push, push);
         } else {
            this.sweepPitchGoal = 0.0 / 0.0;
            this.pitchOffset = this.pitchOffset + (-0.08 * this.pitchOffset + 0.9 * this.nextClampedGaussian());
            this.pitchOffset = clamp(this.pitchOffset, -0.45 * f.pitchUpHalfWidth, 0.45 * f.pitchDownHalfWidth);
         }

         double want = f.targetPitch + this.pitchOffset - this.currentPitch;
         double rate = sweep ? 0.9 : (this.isPitchOutsideSoftBand(f, pitchError) ? 0.6 : 0.35);
         double limit = sweep ? 4.2 : (this.isPitchOutsideSoftBand(f, pitchError) ? (this.isObserverStrafingFast(f) ? 2.0 : 4.0) : 0.42);
         double step = clamp(want * rate, -limit, limit) * this.samplePitchJitterScale();
         if (!sweep && !this.isPitchOutsideSoftBand(f, pitchError)) {
            step = this.clampSoftPitchStep(step);
         }

         this.currentPitch += step;
      }

      double speed = Math.abs(this.currentYaw - startYaw);
      this.currentYaw = this.currentYaw + ((0.08 + 0.12 * speed) * this.nextClampedGaussian() + this.handProfile.tick() * this.nextClampedGaussian());
      this.currentPitch = this.currentPitch + this.handProfile.tick() * 0.6 * this.nextClampedGaussian();
      double wantYaw = this.currentYaw - atYaw;
      double wantPitch = this.currentPitch - atPitch;
      double scale = 1.0;
      if (maxYaw > 0.0 && Math.abs(wantYaw) > maxYaw) {
         scale = Math.min(scale, maxYaw / Math.abs(wantYaw));
      }

      if (maxPitch > 0.0 && Math.abs(wantPitch) > maxPitch) {
         scale = Math.min(scale, maxPitch / Math.abs(wantPitch));
      }

      wantYaw *= scale;
      wantPitch *= scale;
      double minPitch = Math.max(0.13, f.gcdStep * 1.02);
      double fastYaw = Math.min(2.6, 2.75 - 1.6 * f.gcdStep);
      boolean slowYaw = Math.abs(wantYaw) <= fastYaw && realizedYawStep <= 20.4;
      boolean fastRest = !slowYaw && Math.abs(wantYaw) < 12.0 && realizedYawStep <= 20.4 && this.flatYawStreak <= (boundedRests ? 0.0 : 2.0);
      boolean restable = slowYaw || fastRest || !boundedRests && this.isObserverStrafingFast(f);
      if (this.pitchResting) {
         if (!restable || this.isPitchOutsideSoftBand(f, pitchError) || pitchForced || this.random.nextDouble() < 0.3) {
            this.pitchResting = false;
         }
      } else if (restable
         && this.activeMove == null
         && !this.isPitchOutsideSoftBand(f, pitchError)
         && !pitchForced
         && this.random.nextDouble() < (slowYaw ? 0.25 : (this.isObserverStrafingFast(f) ? 0.12 : 0.25))) {
         this.pitchResting = true;
      }

      if (this.pitchResting) {
         wantPitch = 0.0;
         this.currentPitch = atPitch;
      } else if (Math.abs(wantYaw) > 2.9 && !this.isPitchOutsideSoftBand(f, pitchError) && !pitchForced && !(f.gcdStep > 0.5) && Math.abs(wantPitch) > 0.48) {
         wantPitch = this.clampSoftPitchStep(wantPitch);
         this.currentPitch = atPitch + wantPitch;
      }

      if (usesBoundedPitchMode(f) && f.maxPitchRidingCapDeg > 0.0) {
         double needed = Math.max(0.0, Math.abs(f.targetPitch - atPitch) - (f.targetPitch > atPitch ? f.pitchUpHalfWidth : f.pitchDownHalfWidth));
         double cap = computePitchStepCap(f.maxPitchRidingCapDeg, needed, f.prioritizeStrike && strike && !f.forceWhiff, this.random.nextDouble());
         double last = Math.abs(realizedPitchStep);
         if (last > 0.0 && Math.abs(cap - last) <= 0.1 * Math.max(cap, last)) {
            cap = last * (1.12 + 0.04 * this.random.nextDouble());
         }

         if (Math.abs(wantPitch) > cap) {
            wantPitch = Math.copySign(cap, wantPitch);
            this.currentPitch = atPitch + wantPitch;
         }
      }

      boolean steadyMove = this.activeMove == null || !this.activeMove.isStrike && !this.activeMove.isWhiff && !this.activeMove.isExcursion;
      if (steadyMove
         && !f.forceWhiff
         && f.ticksUntilAttack > 3.0
         && Math.abs(f.observerYawShift) > 1.0
         && Math.abs(wantYaw) < 6.0
         && Math.abs(wantPitch) >= f.gcdStep
         && Math.abs(wantPitch) < 1.8
         && this.random.nextDouble() < 0.15) {
         wantYaw = 0.0;
      }

      if (!this.pitchResting && (Math.abs(wantYaw) > fastYaw || realizedYawStep > 20.4) && Math.abs(wantPitch) < minPitch && maxPitch >= minPitch) {
         double toward = f.targetPitch + this.pitchOffset - atPitch;
         if (Math.abs(toward) > minPitch) {
            this.forcedPitchDirection = Math.signum(toward);
         } else {
            this.forcedPitchDirection = -this.forcedPitchDirection;
         }

         double step = minPitch * (this.random.nextDouble() < 0.6 ? 1.0 : 1.0 + this.random.nextDouble());
         wantPitch = this.forcedPitchDirection * Math.min(maxPitch, step);
         this.currentPitch = atPitch + wantPitch;
      }

      if (pitchRepeating && wantPitch != 0.0 && f.gcdStep > 0.0 && f.ticksUntilAttack > 3.0 && Math.round(wantPitch / f.gcdStep) == Math.round(this.lastRealizedPitchStep / f.gcdStep)) {
         double count = Math.abs(wantPitch) > 1.5 * f.gcdStep ? -f.gcdStep : f.gcdStep;
         wantPitch += Math.copySign(count, wantPitch);
         this.currentPitch = atPitch + wantPitch;
      }

      return new double[]{wantYaw, wantPitch, this.pitchResting ? 1.0 : 0.0};
   }

   public static boolean isPitchRestingStep(double[] want) {
      return want.length > 2 && want[2] != 0.0;
   }

   private double clampSoftPitchStep(double step) {
      double cap = 0.48 * (0.62 + 0.38 * this.random.nextDouble());
      return Math.abs(step) <= cap ? step : Math.copySign(cap, step);
   }

   private double samplePitchJitterScale() {
      return 0.55 + 0.9 * this.random.nextDouble();
   }

   public void recordSwing() {
      this.swingsSinceMiss++;
   }

   public void setMissChance(double fraction) {
      fraction = Math.max(0.0, fraction);
      if (fraction != this.missFraction) {
         this.missFraction = fraction;
         this.nextMissSwingThreshold = this.missFraction > 0.0 ? Math.max(1, (int)Math.round((0.8 + 0.4 * this.random.nextDouble()) / this.missFraction)) : 2147483647;
      }
   }

   public boolean shouldTriggerMiss(CombatAimEngine.AimFrame f) {
      if (this.missFraction <= 0.0 || this.swingsSinceMiss < this.nextMissSwingThreshold || f.comboCount < 1) {
         return false;
      } else if (f.legacyNoCooldown) {
         return f.ticksUntilAttack >= 3.0;
      } else {
         return f.comboCount <= 2 ? f.ticksUntilAttack >= 8.0 : this.allowMidComboMiss && f.comboCount <= 8 && f.ticksUntilAttack >= 5.0;
      }
   }

   public void resetMissCycle() {
      this.swingsSinceMiss = 0;
      this.nextMissSwingThreshold = this.missFraction > 0.0 ? Math.max(1, (int)Math.round((0.8 + 0.4 * this.random.nextDouble()) / this.missFraction)) : 2147483647;
      this.allowMidComboMiss = this.random.nextDouble() < 0.3;
      double roll = this.random.nextDouble();
      this.missWidthTier = roll < 0.7 ? 0 : (roll < 0.9 ? 1 : 2);
   }

   private double computeMissYawOffset(CombatAimEngine.AimFrame f) {
      double lateral;
      if (this.missWidthTier == 0) {
         lateral = f.targetHalfWidthBlocks + 0.1 + 0.05 * (this.random.nextDouble() - 0.5);
      } else if (this.missWidthTier == 1) {
         lateral = f.targetHalfWidthBlocks + 0.16 + 0.06 * this.random.nextDouble();
      } else {
         lateral = f.targetHalfWidthBlocks + 0.2 + 0.04 * this.random.nextDouble();
      }

      return Math.toDegrees(Math.atan(lateral / Math.max(0.3, f.horizontalDistance)));
   }

   public void recordRealizedSample(double yaw, double pitch, double perfectYaw, boolean sampled) {
      if (!this.hasSampledLook) {
         this.hasSampledLook = true;
         this.lastSampledYaw = yaw;
         this.lastSampledPitch = pitch;
      } else {
         double yawSpeed = Math.abs(wrapDegrees(yaw - this.lastSampledYaw));
         boolean changed = yawSpeed > 1.0E-6 || Math.abs(pitch - this.lastSampledPitch) > 1.0E-6;
         this.lastSampledYaw = yaw;
         this.lastSampledPitch = pitch;
         if (changed && sampled && !Double.isNaN(perfectYaw)) {
            double error = Math.abs(wrapDegrees(yaw - perfectYaw));
            int direction = perfectYaw > yaw ? 1 : 0;
            if (direction != this.lastErrorSign) {
               this.consistentDirectionScore = 0;
            } else if (yawSpeed > 3.0) {
               double deviation = Math.abs(wrapDegrees(this.lastSampledError - error));
               double increase = clamp((1.0 - deviation) * 4.0, -0.2, 4.0);
               this.consistentDirectionScore = (int)clamp(this.consistentDirectionScore + increase, 0.0, 100.0);
            }

            this.lastErrorSign = direction;
            this.lastSampledError = error;
            if (yawSpeed > 1.0) {
               this.longTermTightStreak = error > 4.0 ? 0 : this.longTermTightStreak + 1;
            }

            double needed = this.computeRequiredErrorThreshold();
            this.recentYawSpeeds[this.sampleWriteIndex] = yawSpeed;
            this.recentYawErrors[this.sampleWriteIndex] = error;
            this.sampleWriteIndex = (this.sampleWriteIndex + 1) % 41;
            this.sampleCount = Math.min(this.sampleCount + 1, 41);
            this.lockStreakTicks = error >= needed ? 0 : this.lockStreakTicks + 1;
         }
      }
   }

   private void planStrikeCorrection(CombatAimEngine.AimFrame f, double error, double carried, double pitchError, double halfWidth, double pursuit, double maxYaw, double maxPitch) {
      double ticks = Math.ceil(f.ticksUntilAttack - 1.0E-6) + 1.0;
      double predicted = error + (pursuit - 1.0) * this.smoothedTargetYawVel * ticks + this.smoothedTargetYawVel;
      double low = computeInnerStrikeBound(halfWidth);
      double high = 0.86 * halfWidth;
      double magnitude = Math.abs(predicted);
      boolean onBand = magnitude <= high && (magnitude >= low || Math.abs(this.smoothedTargetYawVel) < 1.2) && (!this.hasStrongStrafeBias() || predicted * this.lastStrafeYawBias > 0.0);
      if (onBand && !this.isPitchOutsideSoftBand(f, pitchError) && !this.isTrackingTooConsistent()) {
         this.targetYawOffset = predicted;
         this.aimingInsideStrikeBand = true;
      } else {
         this.targetYawOffset = this.pickTargetYawOffset(halfWidth, true, this.pickPreferredOffsetSign());
         this.aimingInsideStrikeBand = true;
         this.startMove(
            f,
            carried,
            pitchError,
            this.targetYawOffset,
            Math.min(ticks, this.computeShortMoveTicks(Math.abs(predicted - this.targetYawOffset))),
            0.96 + 0.08 * this.random.nextDouble(),
            false,
            maxYaw,
            maxPitch
         );
         this.activeMove.isStrike = true;
      }
   }

   private static boolean usesBoundedPitchMode(CombatAimEngine.AimFrame f) {
      return f.boundedPitchMode && f.gcdStep <= 0.5;
   }

   private boolean isPitchOutsideSoftBand(CombatAimEngine.AimFrame f, double pitchError) {
      return usesBoundedPitchMode(f) ? false : pitchError < -0.8 * f.pitchUpHalfWidth || pitchError > 0.8 * f.pitchDownHalfWidth;
   }

   private boolean isTrackingTooConsistent() {
      return this.consistentDirectionScore >= 8;
   }

   private boolean isObserverStrafingFast(CombatAimEngine.AimFrame f) {
      return !this.isTargetMovingFast() && Math.abs(f.observerYawShift) > 3.0;
   }

   private boolean isTargetMovingFast() {
      return Math.abs(this.smoothedTargetYawVel) >= 1.2;
   }

   private double pickPreferredOffsetSign() {
      if (this.hasStrongStrafeBias()) {
         return Math.signum(this.lastStrafeYawBias);
      }

      if (!this.isTargetMovingFast()) {
         return this.random.nextBoolean() ? 1.0 : -1.0;
      }

      double trailing = -Math.signum(this.smoothedTargetYawVel);
      return this.random.nextDouble() < 0.82 ? trailing : -trailing;
   }

   private boolean hasStrongStrafeBias() {
      return Math.abs(this.lastStrafeYawBias) >= this.lastTargetHalfWidth;
   }

   private static double computeInnerStrikeBound(double halfWidth) {
      return Math.min(2.3000000000000003, 0.5 * halfWidth);
   }

   private double pickTargetYawOffset(double halfWidth, boolean forStrike, double side) {
      double low;
      double high;
      if (forStrike) {
         double bottom = computeInnerStrikeBound(halfWidth);
         double top = Math.max(bottom + 0.4, 0.84 * halfWidth);
         double middle = (bottom + top) * 0.5;
         boolean wasOuter = this.aimingInsideStrikeBand && Math.abs(this.targetYawOffset) >= middle;
         low = wasOuter ? bottom : middle;
         high = wasOuter ? middle : top;
      } else {
         double inner = Math.max(2.3000000000000003, 0.36 * halfWidth);
         double outer = Math.max(inner + 2.8, 0.96 * halfWidth);
         boolean wasOuter = Math.abs(this.targetYawOffset) >= outer - 1.0E-6;
         if (wasOuter) {
            low = inner;
            high = Math.max(inner + 0.3, 0.48 * halfWidth);
         } else {
            low = outer;
            high = Math.max(outer + 0.8, (this.random.nextDouble() < 0.3 ? 1.3 : 1.12) * halfWidth);
         }
      }

      double picked = side * (low + (high - low) * this.random.nextDouble());
      if (this.clampOffsetInsideBox) {
         picked = clamp(picked, -0.84 * halfWidth, 0.84 * halfWidth);
      }

      return picked;
   }

   private double computeDeadzoneThreshold(double halfWidth) {
      return Math.max(1.2, 0.32 * halfWidth) * this.deadzoneMultiplier * (0.8 + 0.4 * this.random.nextDouble());
   }

   private double computeFittsFlickTicks(double distance, double halfWidth) {
      double width = Math.max(2.0, 2.0 * halfWidth);
      double ms = (this.handProfile.fittsIntercept() + this.handProfile.fittsSlope() * (Math.log(1.0 + distance / width) / Math.log(2.0))) * 0.42;
      return ms / 50.0 * Math.exp(this.handProfile.open() * this.nextClampedGaussian());
   }

   private double computeShortMoveTicks(double distance) {
      return distance < 2.5 ? 1.0 : (distance < 8.0 ? 2.0 : 3.0);
   }

   private static double evaluateSmoothstep(double s) {
      double u = Math.pow(Math.max(0.0, Math.min(1.0, s)), 0.82);
      return u * u * u * (10.0 + u * (-15.0 + 6.0 * u));
   }

   private static double fitMoveDurationTicks(double travel, double minTicks, double accelUp, double cap) {
      double distance = Math.abs(travel);

      int ticks;
      for (ticks = (int)Math.max(1.0, Math.round(minTicks)); ticks < 60; ticks++) {
         double previous = 0.0;
         boolean fits = true;

         for (int k = 1; k <= ticks && fits; k++) {
            double step = distance * (evaluateSmoothstep((double)k / ticks) - evaluateSmoothstep((double)(k - 1) / ticks));
            if (step - previous > accelUp * 0.92 || previous - step > 16.5 || cap > 0.0 && step > cap * 0.95) {
               fits = false;
            }

            previous = step;
         }

         if (fits && previous <= 16.5) {
            return ticks;
         }
      }

      return ticks;
   }

   private void startMove(
      CombatAimEngine.AimFrame f, double start, double pitchError, double targetError, double ticks, double gainRoll, boolean excursion, double maxYaw, double maxPitch
   ) {
      double duration = scaleTicksBySpeed(ticks, f.speedScale);
      double pitchTarget = clamp(this.pitchOffset, -0.45 * f.pitchUpHalfWidth, 0.45 * f.pitchDownHalfWidth);
      double dPitch = !excursion && this.isPitchOutsideSoftBand(f, pitchError) ? pitchTarget - pitchError : 0.0;
      double travel = (targetError - start) * gainRoll;
      if (f.speedScale < 1.0) {
         double normal = fitMoveDurationTicks(travel, Math.max(1L, Math.round(ticks)), f.maxAccelUp, maxYaw > 0.0 ? maxYaw / f.speedScale - Math.abs(this.smoothedTargetYawVel) : 0.0);
         if (dPitch != 0.0) {
            normal = Math.max(normal, fitMoveDurationTicks(dPitch, normal, f.maxAccelUp, maxPitch / f.speedScale));
         }

         duration = scaleTicksBySpeed(normal, f.speedScale);
      }

      duration = fitMoveDurationTicks(travel, duration, f.maxAccelUp, maxYaw > 0.0 ? maxYaw - Math.abs(this.smoothedTargetYawVel) : 0.0);
      if (dPitch != 0.0) {
         duration = Math.max(duration, fitMoveDurationTicks(dPitch, duration, f.maxAccelUp, maxPitch));
      }

      CombatAimEngine.AimMove next = new CombatAimEngine.AimMove();
      next.startError = start;
      next.yawTravel = travel;
      next.pitchTravel = dPitch;
      next.durationTicks = duration;
      next.isExcursion = excursion;
      next.arcAmplitude = (this.handProfile.sync() + this.handProfile.flush() * clamp(this.nextClampedGaussian(), -2.0, 2.0)) * Math.abs(travel);
      this.activeMove = next;
   }

   private void advanceActiveMove(double expected) {
      CombatAimEngine.AimMove m = this.activeMove;
      m.elapsedTicks++;
      double s = Math.min(1.0, m.elapsedTicks / m.durationTicks);
      double progress = evaluateSmoothstep(s);
      double side = Math.sin(3.141592653589793 * s) * m.arcAmplitude;
      this.currentYaw = this.currentYaw + (m.startError + m.yawTravel * progress - expected);
      double p = m.pitchTravel * progress + side * 0.25;
      this.currentPitch = this.currentPitch + (p - m.appliedPitch);
      m.appliedPitch = p;
      if (s >= 1.0) {
         this.activeMove = null;
         this.postMoveCooldownTicks = m.isExcursion ? 0 : this.random.nextInt(2);
         if (m.isExcursion) {
            this.aimingInsideStrikeBand = true;
         }
      }
   }

   private static double scaleTicksBySpeed(double ticks, double scale) {
      return Math.max(1L, Math.round(ticks / scale));
   }

   private void updateTargetVelocity(double centerYaw, double centerPitch) {
      if (this.hasTargetCenterHistory) {
         double dYaw = wrapDegrees(centerYaw - this.lastTargetCenterYaw);
         double dPitch = centerPitch - this.lastTargetCenterPitch;
         dYaw = clamp(dYaw, -72.0, 72.0);
         this.smoothedTargetYawVel = this.smoothedTargetYawVel + 0.55 * (dYaw - this.smoothedTargetYawVel);
         this.smoothedTargetPitchVel = this.smoothedTargetPitchVel + 0.4 * (dPitch - this.smoothedTargetPitchVel);
      }

      this.hasTargetCenterHistory = true;
      this.lastTargetCenterYaw = centerYaw;
      this.lastTargetCenterPitch = centerPitch;
   }

   private boolean shouldStartExcursion(CombatAimEngine.AimFrame f, boolean preempt) {
      if (f.prioritizeStrike && f.ticksUntilAttack <= 3.0) {
         return false;
      }

      boolean longTerm = this.longTermTightStreak > 26;
      if (this.lockStreakTicks < 20 && !longTerm) {
         return false;
      }

      boolean urgent = this.isExcursionUrgent();
      if (!f.prioritizeStrike && urgent || !f.inAttackWindow && f.comboCount >= 2) {
         double needed = this.lockStreakTicks >= 20 ? this.computeRequiredErrorThreshold() : 5.5 + 2.0 * this.random.nextDouble();
         double travel = fitMoveDurationTicks(needed, this.computeShortMoveTicks(needed), 18.0, 0.0) * 2.0 + 1.0;
         if (preempt && !urgent && f.comboCount > 3) {
            return false;
         }

         if ((f.prioritizeStrike || !urgent) && f.ticksUntilAttack < travel) {
            return false;
         }

         this.excursionTargetOffset = Math.min(28.0, needed + 2.0 + 2.5 * this.random.nextDouble());
         return true;
      } else {
         return false;
      }
   }

   private boolean isExcursionUrgent() {
      return this.lockStreakTicks >= 33 || this.longTermTightStreak > 44;
   }

   private double computeRequiredErrorThreshold() {
      if (this.sampleCount == 0) {
         return 13.0;
      }

      double speed = 0.0;
      double error = 0.0;

      for (int i = 0; i < this.sampleCount; i++) {
         speed += this.recentYawSpeeds[i];
         error += this.recentYawErrors[i];
      }

      speed /= this.sampleCount;
      error /= this.sampleCount;
      double needed = 2.3 * speed;
      if (speed >= 2.5 || error < speed + 1.0 && speed >= 2.0) {
         needed = Math.max(needed, 13.2);
      }

      return Math.min(Math.max(needed, 4.5), 26.0);
   }

   String formatDebugState() {
      return String.format(
         Locale.ROOT,
         "omega=%.2f gain=%.2f offset=%.2f move=%s since=%d need=%.1f lt=%d",
         this.smoothedTargetYawVel,
         this.pursuitGain,
         this.targetYawOffset,
         this.activeMove == null
            ? "-"
            : (this.activeMove.isExcursion ? "EXC" : (this.activeMove.isStrike ? "STR" : "COR"))
               + (int)this.activeMove.elapsedTicks
               + "/"
               + (int)this.activeMove.durationTicks,
         this.lockStreakTicks,
         this.computeRequiredErrorThreshold(),
         this.longTermTightStreak
      );
   }

   private double nextClampedGaussian() {
      return clamp(this.random.nextGaussian(), -3.0, 3.0);
   }

   private static double extractTraitFraction(double trait) {
      double v = Math.abs(trait * 1000.0);
      return v - Math.floor(v);
   }

   private static double clamp(double v, double lo, double hi) {
      return Math.max(lo, Math.min(hi, v));
   }

   private static double wrapDegrees(double degrees) {
      double wrapped = degrees % 360.0;
      if (wrapped >= 180.0) {
         wrapped -= 360.0;
      }

      if (wrapped < -180.0) {
         wrapped += 360.0;
      }

      return wrapped;
   }

   public static final class ArcStepper {
      private CombatAimEngine aimEngine;
      private double startYaw;
      private double startPitch;
      private double goalYaw;
      private double goalPitch;
      private double durationTicks;
      private double elapsedTicks;
      private double arcAmplitude;
      private double lastSpeedScale;

      public ArcStepper() {
      }

      ArcStepper(CombatAimEngine hand) {
         this.aimEngine = hand;
      }

      public void reset() {
         this.durationTicks = 0.0;
      }

      public double[] stepToward(RotationMath.Rotation at, RotationMath.Rotation goal, float maxYaw, float maxPitch, double scale) {
         double yaw = at.yaw() + CombatAimEngine.wrapDegrees(goal.yaw() - at.yaw());
         double pitch = goal.pitch();
         if (this.durationTicks == 0.0 || scale != this.lastSpeedScale || Math.abs(CombatAimEngine.wrapDegrees(yaw - this.goalYaw)) > 12.0 || Math.abs(pitch - this.goalPitch) > 12.0) {
            if (this.aimEngine == null) {
               this.aimEngine = CombatAimEngine.createDefault();
            }

            this.startYaw = at.yaw();
            this.startPitch = at.pitch();
            this.goalYaw = yaw;
            this.goalPitch = pitch;
            this.lastSpeedScale = scale;
            this.elapsedTicks = 0.0;
            double dy = this.goalYaw - this.startYaw;
            double dp = this.goalPitch - this.startPitch;
            double distance = Math.hypot(dy, dp);
            double ticks = distance < 18.0 ? this.aimEngine.computeShortMoveTicks(distance) : this.aimEngine.computeFittsFlickTicks(distance, 2.0);
            double preferred = CombatAimEngine.scaleTicksBySpeed(ticks, scale);
            this.durationTicks = Math.max(CombatAimEngine.fitMoveDurationTicks(dy, preferred, 18.0, maxYaw), CombatAimEngine.fitMoveDurationTicks(dp, preferred, 18.0, maxPitch));
            this.arcAmplitude = (this.aimEngine.handProfile.sync() + this.aimEngine.handProfile.flush() * CombatAimEngine.clamp(this.aimEngine.nextClampedGaussian(), -2.0, 2.0)) * dy;
         }

         this.elapsedTicks = Math.min(this.durationTicks, this.elapsedTicks + 1.0);
         double t = this.elapsedTicks / this.durationTicks;
         double progress = CombatAimEngine.evaluateSmoothstep(t);
         double nextYaw = this.startYaw + CombatAimEngine.wrapDegrees(yaw - this.startYaw) * progress;
         double nextPitch = this.startPitch + (pitch - this.startPitch) * progress + Math.sin(3.141592653589793 * t) * this.arcAmplitude * 0.25;
         return new double[]{CombatAimEngine.wrapDegrees(nextYaw - at.yaw()), nextPitch - at.pitch()};
      }
   }

   public static final class AimFrame {
      public double targetYaw;
      public double targetPitch;
      public double yawHalfWidth;
      public double pitchUpHalfWidth;
      public double pitchDownHalfWidth;
      public double ticksUntilAttack;
      public int comboCount;
      public boolean inAttackWindow;
      public boolean targetChanged;
      public boolean allowExcursion = true;
      public boolean clampInsideBox;
      public boolean compensateObserverMotion;
      public boolean boundedPitchMode;
      public double pitchSlackDeg;
      public double maxPitchRidingCapDeg;
      public boolean allowPromptFlick;
      public boolean instantPromptFlick;
      public boolean prioritizeStrike;
      public boolean allowBoundedRests;
      public double strafeYawBias;
      private Vec3 lastEyePos;
      private double observerYawShift;
      private double observerPitchShift;
      public double gcdStep = 0.15;
      public double maxAccelUp = 18.0;
      public double speedScale = 1.0;
      public boolean forceWhiff;
      public boolean legacyNoCooldown;
      public double targetHalfWidthBlocks = 0.3;
      public double horizontalDistance = 3.0;
      public float centerYawHint = 0.0F / 0.0F;
      public float nearestYawHint = 0.0F / 0.0F;

      public CombatAimEngine.AimFrame setTargetFromAimY(Vec3 eyes, AABB box, double aimY) {
         return this.setTarget(eyes, box, computeAnchorAtHeight(box, aimY));
      }

      public CombatAimEngine.AimFrame setTarget(Vec3 eyes, AABB box, Vec3 anchor) {
         return this.setTarget(eyes, box, box, anchor);
      }

      public CombatAimEngine.AimFrame setTarget(Vec3 eyes, AABB box, AABB region, Vec3 anchor) {
         return this.setTarget(eyes, box, region, anchor, null);
      }

      public CombatAimEngine.AimFrame setTarget(Vec3 eyes, AABB box, AABB region, Vec3 anchor, Vec3 receivedTarget) {
         return this.setTarget(eyes, box, region, anchor, receivedTarget, null);
      }

      public CombatAimEngine.AimFrame setTarget(Vec3 eyes, AABB box, AABB region, Vec3 anchor, Vec3 receivedTarget, AABB receivedBox) {
         double width = Math.min(region.getXsize(), region.getZsize());
         double height = region.getYsize();
         double dx = anchor.x - eyes.x;
         double dz = anchor.z - eyes.z;
         double horizontal = Math.max(1.0E-6, Math.hypot(dx, dz));
         this.targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
         this.yawHalfWidth = Math.toDegrees(Math.atan(Math.max(0.02, width * 0.5 - 0.04) / horizontal));
         double observerAnchorX = anchor.x;
         double observerAnchorZ = anchor.z;
         if (receivedTarget != null) {
            double bearing = Math.toDegrees(Math.atan2(receivedTarget.z - eyes.z, receivedTarget.x - eyes.x)) - 90.0;
            double shift = CombatAimEngine.wrapDegrees(bearing - this.targetYaw);
            double corridor = Math.toDegrees(0.2);
            this.yawHalfWidth = Math.min(this.yawHalfWidth, corridor);
            double low = Math.max(-this.yawHalfWidth, shift - corridor);
            double high = Math.min(this.yawHalfWidth, shift + corridor);
            if (low <= high) {
               this.targetYaw += (low + high) * 0.5;
               this.yawHalfWidth = (high - low) * 0.5;
            } else {
               this.targetYaw = this.targetYaw + CombatAimEngine.clamp(shift, -this.yawHalfWidth, this.yawHalfWidth);
               this.yawHalfWidth = 0.0;
            }

            observerAnchorX = eyes.x - Math.sin(Math.toRadians(this.targetYaw)) * horizontal;
            observerAnchorZ = eyes.z + Math.cos(Math.toRadians(this.targetYaw)) * horizontal;
         }

         if (receivedBox != null
            && (!(eyes.x >= receivedBox.minX) || !(eyes.x <= receivedBox.maxX) || !(eyes.z >= receivedBox.minZ) || !(eyes.z <= receivedBox.maxZ))) {
            double bearing = Math.toDegrees(Math.atan2(receivedBox.getCenter().z - eyes.z, receivedBox.getCenter().x - eyes.x)) - 90.0;
            double shift = CombatAimEngine.wrapDegrees(bearing - this.targetYaw);
            double left = 1.0 / 0.0;
            double right = -1.0 / 0.0;

            for (double x : new double[]{receivedBox.minX + 0.003, receivedBox.maxX - 0.003}) {
               for (double z : new double[]{receivedBox.minZ + 0.003, receivedBox.maxZ - 0.003}) {
                  double angle = Math.toDegrees(Math.atan2(z - eyes.z, x - eyes.x)) - 90.0;
                  double offset = shift + CombatAimEngine.wrapDegrees(angle - bearing);
                  left = Math.min(left, offset);
                  right = Math.max(right, offset);
               }
            }

            double low = Math.max(-this.yawHalfWidth, left);
            double high = Math.min(this.yawHalfWidth, right);
            if (low <= high) {
               this.targetYaw += (low + high) * 0.5;
               this.yawHalfWidth = (high - low) * 0.5;
            } else {
               this.targetYaw = this.targetYaw + CombatAimEngine.clamp(shift, -this.yawHalfWidth, this.yawHalfWidth);
               this.yawHalfWidth = 0.0;
            }

            observerAnchorX = eyes.x - Math.sin(Math.toRadians(this.targetYaw)) * horizontal;
            observerAnchorZ = eyes.z + Math.cos(Math.toRadians(this.targetYaw)) * horizontal;
         }

         this.targetPitch = -Math.toDegrees(Math.atan2(anchor.y - eyes.y, horizontal));
         this.centerYawHint = (float)Math.toDegrees(Math.atan2(box.getCenter().z - eyes.z, box.getCenter().x - eyes.x)) - 90.0F;
         this.nearestYawHint = (float)Math.toDegrees(Math.atan2(CombatAimEngine.clamp(eyes.z, box.minZ, box.maxZ) - eyes.z, CombatAimEngine.clamp(eyes.x, box.minX, box.maxX) - eyes.x))
            - 90.0F;
         this.observerYawShift = this.observerPitchShift = 0.0;
         if (this.lastEyePos != null) {
            double oldDx = observerAnchorX - this.lastEyePos.x;
            double oldDz = observerAnchorZ - this.lastEyePos.z;
            double oldHorizontal = Math.max(1.0E-6, Math.hypot(oldDx, oldDz));
            double share = CombatAimEngine.clamp((eyes.distanceTo(this.lastEyePos) - 0.12) / 0.12, 0.0, 1.0);
            this.observerYawShift = share * CombatAimEngine.wrapDegrees(this.targetYaw - (Math.toDegrees(Math.atan2(oldDz, oldDx)) - 90.0));
            this.observerPitchShift = share * (this.targetPitch + Math.toDegrees(Math.atan2(anchor.y - this.lastEyePos.y, oldHorizontal)));
         }

         this.lastEyePos = eyes;
         this.targetHalfWidthBlocks = width * 0.5;
         this.horizontalDistance = Math.max(0.3, horizontal);
         double top = -Math.toDegrees(Math.atan2(region.maxY - 0.08 * height - eyes.y, horizontal));
         double bottom = -Math.toDegrees(Math.atan2(region.minY + 0.1 * height - eyes.y, horizontal));
         this.pitchUpHalfWidth = Math.max(0.2, this.targetPitch - top);
         this.pitchDownHalfWidth = Math.max(0.2, bottom - this.targetPitch);
         return this;
      }

      public static Vec3 computeAnchorAtHeight(AABB box, double aimY) {
         double height = box.getYsize();
         double y = CombatAimEngine.clamp(aimY, box.minY + 0.22 * height, box.maxY - 0.12 * height);
         return new Vec3((box.minX + box.maxX) * 0.5, y, (box.minZ + box.maxZ) * 0.5);
      }

      public static Vec3 clampAnchorToReach(Vec3 eyes, AABB box, Vec3 anchor, double reach) {
         if (box.contains(eyes)) {
            return anchor;
         }

         double limit = reach - 0.06;
         if (rayDistanceToBox(eyes, box, anchor) <= limit) {
            return anchor;
         }

         Vec3 near = closestInteriorPoint(eyes, box);
         double nearest = rayDistanceToBox(eyes, box, near);
         if (nearest > limit) {
            return nearest <= reach + 0.6 ? near : anchor;
         }

         double low = 0.0;
         double high = 1.0;

         for (int i = 0; i < 12; i++) {
            double mid = (low + high) * 0.5;
            if (rayDistanceToBox(eyes, box, lerpVec3(anchor, near, mid)) <= limit) {
               high = mid;
            } else {
               low = mid;
            }
         }

         return lerpVec3(anchor, near, high);
      }

      public static AABB shrinkRegionToReach(Vec3 eyes, AABB box, Vec3 anchor, double reach) {
         double ex = Math.max(0.0, Math.min(anchor.x - box.minX, box.maxX - anchor.x));
         double ey = Math.max(0.0, Math.min(anchor.y - box.minY, box.maxY - anchor.y));
         double ez = Math.max(0.0, Math.min(anchor.z - box.minZ, box.maxZ - anchor.z));
         if (!box.contains(eyes) && !(ex <= 1.0E-4) && !(ey <= 1.0E-4) && !(ez <= 1.0E-4)) {
            double limit = reach - 0.03;
            if (maxScaledRegionRayDistance(eyes, box, anchor, ex, ey, ez, 1.0) <= limit) {
               return scaleRegionAroundAnchor(anchor, ex, ey, ez, 1.0);
            }

            if (maxScaledRegionRayDistance(eyes, box, anchor, ex, ey, ez, 0.25) > limit) {
               return scaleRegionAroundAnchor(anchor, ex, ey, ez, 0.25);
            }

            double low = 0.25;
            double high = 1.0;

            for (int i = 0; i < 10; i++) {
               double mid = (low + high) * 0.5;
               if (maxScaledRegionRayDistance(eyes, box, anchor, ex, ey, ez, mid) <= limit) {
                  low = mid;
               } else {
                  high = mid;
               }
            }

            return scaleRegionAroundAnchor(anchor, ex, ey, ez, low);
         } else {
            return box;
         }
      }

      public static double rayDistanceToBox(Vec3 eyes, AABB box, Vec3 point) {
         if (box.contains(eyes)) {
            return 0.0;
         }

         Vec3 direction = point.subtract(eyes);
         double length = direction.length();
         if (length < 1.0E-9) {
            return 0.0;
         }

         Vec3 far = eyes.add(direction.scale((length + 4.0) / length));
         return box.clip(eyes, far).map(hit -> hit.distanceTo(eyes)).orElse(1.7976931348623157E308);
      }

      static Vec3 closestInteriorPoint(Vec3 eyes, AABB box) {
         double ix = Math.min(0.003, box.getXsize() * 0.01);
         double iz = Math.min(0.003, box.getZsize() * 0.01);
         double iy = Math.min(0.003, box.getYsize() * 0.01);
         return new Vec3(
            CombatAimEngine.clamp(eyes.x, box.minX + ix, box.maxX - ix),
            CombatAimEngine.clamp(eyes.y, box.minY + iy, box.maxY - iy),
            CombatAimEngine.clamp(eyes.z, box.minZ + iz, box.maxZ - iz)
         );
      }

      private static double maxScaledRegionRayDistance(Vec3 eyes, AABB box, Vec3 anchor, double ex, double ey, double ez, double scale) {
         double dx = anchor.x - eyes.x;
         double dz = anchor.z - eyes.z;
         double horizontal = Math.max(1.0E-6, Math.hypot(dx, dz));
         double side = Math.min(ex, ez) * scale;
         Vec3 across = new Vec3(-dz / horizontal * side, 0.0, dx / horizontal * side);
         double worst = rayDistanceToBox(eyes, box, anchor);
         worst = Math.max(worst, rayDistanceToBox(eyes, box, anchor.add(across)));
         worst = Math.max(worst, rayDistanceToBox(eyes, box, anchor.subtract(across)));
         worst = Math.max(worst, rayDistanceToBox(eyes, box, anchor.add(0.0, ey * scale, 0.0)));
         return Math.max(worst, rayDistanceToBox(eyes, box, anchor.add(0.0, -ey * scale, 0.0)));
      }

      private static AABB scaleRegionAroundAnchor(Vec3 anchor, double ex, double ey, double ez, double scale) {
         return new AABB(
            anchor.x - ex * scale, anchor.y - ey * scale, anchor.z - ez * scale, anchor.x + ex * scale, anchor.y + ey * scale, anchor.z + ez * scale
         );
      }

      private static Vec3 lerpVec3(Vec3 from, Vec3 to, double t) {
         return new Vec3(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t, from.z + (to.z - from.z) * t);
      }
   }

   private static final class AimMove {
      double startError;
      double yawTravel;
      double pitchTravel;
      double durationTicks;
      double elapsedTicks;
      double appliedPitch;
      double arcAmplitude;
      boolean isExcursion;
      boolean isStrike;
      boolean isWhiff;
   }
}
