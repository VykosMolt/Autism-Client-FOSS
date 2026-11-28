package autismclient.util;

import java.util.Objects;
import java.util.Random;

public final class RotationStreamPlanner {
   private static final double DEFAULT_GCD = 0.15;
   private static final double load = 4.0;
   private static final double read = 0.7;
   private static final double write = 0.3;
   private static final double update = 0.55;
   private static final double sync = 0.35;
   private static final double flush = 8.0;
   private static final double close = 10.0;
   private static final double open = 3.0;
   private static final double poll = 0.75;
   private static final double push = 1.25;
   private static final double pop = 0.5;
   private static final double tick = 1.25;
   private static final double run = 0.25;
   private static final double get = 0.45;
   private static final double set = 0.55;
   private static final double put = 0.3;
   private static final double add = 0.08;
   private static final double clear = 0.9;
   private static final double MAX_WANDER_WEIGHT = 1.1;
   private static final double start = 2.75;
   private static final double stop = 0.5;
   private static final double next = 89.9;

   private RotationStreamPlanner() {
   }

   public static double pitchMargin(double gcd) {
      return 0.5 + (gcd > 0.0 ? gcd : 0.15);
   }

   public static double[] lastStep(RotationStreamPlanner.StreamState s) {
      return new double[]{s.lastYawStep, s.lastPitchStep};
   }

   public static boolean isActive(RotationStreamPlanner.StreamState s) {
      return s != null && s.seeded;
   }

   public static RotationMath.Rotation currentRotation(RotationStreamPlanner.StreamState s) {
      return isActive(s) ? new RotationMath.Rotation((float)s.yaw, (float)s.pitch) : null;
   }

   public static void begin(RotationStreamPlanner.StreamState s, RotationMath.Rotation from) {
      Objects.requireNonNull(s, "stream");
      Objects.requireNonNull(from, "from");
      s.yaw = from.yaw();
      s.pitch = from.pitch();
      s.lastYawStep = 0.0;
      s.lastPitchStep = 0.0;
      s.kYaw = 0;
      s.kPitch = 0;
      s.lastYawDelta = 0.0F;
      s.activeOffsets = null;
      s.pendingOffsets = null;
      s.goalYaw = from.yaw();
      s.goalPitch = from.pitch();
      s.seeded = true;
   }

   public static void reset(RotationStreamPlanner.StreamState s) {
      Objects.requireNonNull(s, "stream");
      s.seeded = false;
      s.yaw = 0.0;
      s.pitch = 0.0;
      s.lastYawStep = 0.0;
      s.lastPitchStep = 0.0;
      s.kYaw = 0;
      s.kPitch = 0;
      s.lastYawDelta = 0.0F;
      s.activeOffsets = null;
      s.pendingOffsets = null;
      s.goalYaw = 0.0;
      s.goalPitch = 0.0;
   }

   public static void resume(RotationStreamPlanner.StreamState s, RotationMath.Rotation at, double lastStepYaw, double lastStepPitch, double gcd) {
      Objects.requireNonNull(s, "stream");
      Objects.requireNonNull(at, "at");
      double g = gcd > 0.0 ? gcd : 0.15;
      s.yaw = at.yaw();
      s.pitch = at.pitch();
      s.lastYawStep = Math.abs(lastStepYaw);
      s.lastPitchStep = Math.abs(lastStepPitch);
      s.kYaw = (int)Math.round(s.lastYawStep / g);
      s.kPitch = (int)Math.round(s.lastPitchStep / g);
      s.lastYawDelta = (float)s.lastYawStep;
      s.activeOffsets = null;
      s.pendingOffsets = null;
      s.goalYaw = at.yaw();
      s.goalPitch = at.pitch();
      s.seeded = true;
   }

   public static RotationStreamPlanner.StepResult init_ap_ConfigStore_ap_TaskQueue(
      RotationStreamPlanner.StreamState s, RotationMath.Rotation goal, float maxYawStep, float maxPitchStep, double gcd
   ) {
      return planStep(s, goal, maxYawStep, maxPitchStep, gcd, true);
   }

   public static RotationStreamPlanner.StepResult planStep(
      RotationStreamPlanner.StreamState s, RotationMath.Rotation goal, float maxYawStep, float maxPitchStep, double gcd, boolean applyGoalOffset
   ) {
      return planStep(s, goal, maxYawStep, maxPitchStep, gcd, applyGoalOffset, RotationStreamPlanner.MotionProfile.STANDARD);
   }

   public static RotationStreamPlanner.StepResult planStep(
      RotationStreamPlanner.StreamState s, RotationMath.Rotation goal, float maxYawStep, float maxPitchStep, double gcd, boolean applyGoalOffset, RotationStreamPlanner.MotionProfile profile
   ) {
      Objects.requireNonNull(s, "stream");
      Objects.requireNonNull(goal, "goal");
      Objects.requireNonNull(profile, "profile");
      if (!s.seeded) {
         throw new IllegalStateException("stream is not seeded; seed() from the current server rotation first");
      }

      double g = gcd > 0.0 ? gcd : 0.15;
      RotationStreamPlanner.RolledOffsets rolls;
      if (needsReroll(s, goal, profile)) {
         s.pendingOffsets = rollOffsets(s.random, goal, profile);
         rolls = s.pendingOffsets;
      } else {
         s.pendingOffsets = null;
         rolls = s.activeOffsets;
      }

      s.goalYaw = goal.yaw();
      s.goalPitch = goal.pitch();
      s.wanderWeight = profile == RotationStreamPlanner.MotionProfile.STANDARD ? wanderWeight(s.wanderWeight, s.random) : 1.0;
      s.yawDither = nextDither(s.yawDither, s.random);
      s.pitchDither = nextDither(s.pitchDither, s.random);
      double effectiveYaw = goal.yaw() + (applyGoalOffset ? rolls.yawGoalOffset : 0.0);
      double effectivePitch = clampPitch(goal.pitch() + (applyGoalOffset ? rolls.pitchGoalOffset : 0.0));
      RotationStreamPlanner.AxisApproach yawAxis = new RotationStreamPlanner.AxisApproach(wrapDegrees(effectiveYaw - s.yaw), maxYawStep, g, s.lastYawStep, rolls.accelRoll, 0.0 / 0.0);
      RotationStreamPlanner.AxisApproach pitchAxis = new RotationStreamPlanner.AxisApproach(effectivePitch - s.pitch, maxPitchStep, g, s.lastPitchStep, rolls.accelRoll, s.pitch);
      int kYaw = chooseStepCount(yawAxis, s.kYaw, rolls.yawRoll, rolls.stepScaleRoll * s.wanderWeight, s.random, s.yawDither);
      int kPitch = chooseStepCount(pitchAxis, s.kPitch, rolls.pitchRoll, rolls.stepScaleRoll * s.wanderWeight, s.random, s.pitchDither);
      if (kYaw == 0 && Math.abs(kPitch * g) > 2.75 && yawAxis.isSettled() && s.random.nextDouble() < 0.5) {
         kYaw = randomNudge(yawAxis, s.random);
      }

      if (kPitch == 0 && Math.abs(kYaw * g) > 2.75 && pitchAxis.isSettled() && s.random.nextDouble() < 0.5) {
         kPitch = randomNudge(pitchAxis, s.random);
      }

      return new RotationStreamPlanner.StepResult(s, kYaw, kPitch, g);
   }

   public static RotationMath.Rotation applyStep(RotationStreamPlanner.StreamState s, RotationStreamPlanner.StepResult step) {
      Objects.requireNonNull(s, "stream");
      Objects.requireNonNull(step, "step");
      if (!s.seeded) {
         throw new IllegalStateException("stream is not seeded; seed() first");
      }

      if (step.owner != s) {
         throw new IllegalArgumentException("step was computed on a different stream");
      }

      if (s.pendingOffsets != null) {
         s.activeOffsets = s.pendingOffsets;
         s.pendingOffsets = null;
      }

      float yawBeforeTick = (float)s.yaw;
      s.yaw = applyGcdSteps(s.yaw, step.kYaw, step.gcd);
      s.pitch = clampPitch(applyGcdSteps(s.pitch, step.pitchSteps, step.gcd));
      s.lastYawStep = Math.abs(step.kYaw * step.gcd);
      s.lastPitchStep = Math.abs(step.pitchSteps * step.gcd);
      s.kYaw = step.kYaw;
      s.kPitch = step.pitchSteps;
      recordYawDelta(s, yawBeforeTick);
      return currentRotation(s);
   }

   public static RotationMath.Rotation init_ce_DataUtil_ap_TaskQueue(RotationStreamPlanner.StreamState s, RotationMath.Rotation goal, float maxYawStep, float maxPitchStep, double gcd) {
      return stepToward(s, goal, maxYawStep, maxPitchStep, gcd, true);
   }

   public static RotationMath.Rotation stepToward(
      RotationStreamPlanner.StreamState s, RotationMath.Rotation goal, float maxYawStep, float maxPitchStep, double gcd, boolean applyGoalOffset
   ) {
      return stepToward(s, goal, maxYawStep, maxPitchStep, gcd, applyGoalOffset, RotationStreamPlanner.MotionProfile.STANDARD);
   }

   public static RotationMath.Rotation stepToward(
      RotationStreamPlanner.StreamState s, RotationMath.Rotation goal, float maxYawStep, float maxPitchStep, double gcd, boolean applyGoalOffset, RotationStreamPlanner.MotionProfile profile
   ) {
      Objects.requireNonNull(s, "stream");
      Objects.requireNonNull(goal, "goal");
      Objects.requireNonNull(profile, "profile");
      if (!s.seeded) {
         begin(s, goal);
         return currentRotation(s);
      } else {
         return applyStep(
            s, planStep(s, goal, maxYawStep, maxPitchStep, gcd, applyGoalOffset, profile)
         );
      }
   }

   public static RotationMath.Rotation stepBy(
      RotationStreamPlanner.StreamState s, double yawDelta, double pitchDelta, float maxYawStep, float maxPitchStep, double gcd, double accelLimit
   ) {
      return planDeltaStep(s, yawDelta, pitchDelta, maxYawStep, maxPitchStep, gcd, accelLimit, accelLimit);
   }

   public static RotationMath.Rotation planDeltaStep(
      RotationStreamPlanner.StreamState s, double yawDelta, double pitchDelta, float maxYawStep, float maxPitchStep, double gcd, double accelUp, double accelDown
   ) {
      return planDeltaStep(s, yawDelta, pitchDelta, maxYawStep, maxPitchStep, gcd, accelUp, accelDown, false);
   }

   public static RotationMath.Rotation planDeltaStep(
      RotationStreamPlanner.StreamState s,
      double yawDelta,
      double pitchDelta,
      float maxYawStep,
      float maxPitchStep,
      double gcd,
      double accelUp,
      double accelDown,
      boolean holdPitch
   ) {
      return step(s, yawDelta, pitchDelta, maxYawStep, maxPitchStep, gcd, accelUp, accelDown, holdPitch, false);
   }

   public static RotationMath.Rotation step(
      RotationStreamPlanner.StreamState s,
      double yawDelta,
      double pitchDelta,
      float maxYawStep,
      float maxPitchStep,
      double gcd,
      double accelUp,
      double accelDown,
      boolean holdPitch,
      boolean naturalMouseAngles
   ) {
      Objects.requireNonNull(s, "stream");
      if (!s.seeded) {
         throw new IllegalStateException("stream is not seeded; seed() from the current server rotation first");
      }

      double g = gcd > 0.0 ? gcd : 0.15;
      RotationStreamPlanner.AxisQuantizer yaw = new RotationStreamPlanner.AxisQuantizer(
         maxYawStep, g, s.lastYawStep, s.kYaw, s.lastYawStep > 0.0 ? accelUp : accelDown, accelDown, 0.0 / 0.0, Math.abs(s.lastYawDelta)
      );
      RotationStreamPlanner.AxisQuantizer pitch = new RotationStreamPlanner.AxisQuantizer(maxPitchStep, g, s.lastPitchStep, s.kPitch, s.lastPitchStep > 0.0 ? accelUp : accelDown, accelDown, s.pitch);
      int kYaw = yaw.quantizeDelta(yawDelta, s.random);
      int kPitch = pitch.quantizeDelta(pitchDelta, s.random);
      if (kYaw == 0 && Math.abs(kPitch * g) > 2.75 && s.random.nextDouble() < 0.5) {
         kYaw = yaw.randomMinimalStep(s.random);
      }

      if (kPitch == 0 && !holdPitch && Math.abs(kYaw * g) > 2.75 && s.random.nextDouble() < 0.5) {
         kPitch = pitch.randomMinimalStep(s.random);
      }

      s.activeOffsets = null;
      s.pendingOffsets = null;
      float yawBeforeTick = (float)s.yaw;
      float pitchBeforeTick = (float)s.pitch;
      s.yaw = applyGcdSteps(s.yaw, kYaw, g);
      s.pitch = clampPitch(applyGcdSteps(s.pitch, kPitch, g));
      s.lastYawStep = Math.abs(kYaw * g);
      s.lastPitchStep = Math.abs(kPitch * g);
      s.kYaw = kYaw;
      s.kPitch = kPitch;
      boolean moving = Math.abs((float)s.yaw - yawBeforeTick) + Math.abs((float)s.pitch - pitchBeforeTick) > 0.75F;
      quantizeYaw(s, g, yawBeforeTick, yaw, naturalMouseAngles && moving);
      if (naturalMouseAngles) {
         quantizePitch(s, g, pitchBeforeTick, yawBeforeTick, pitch);
      }

      recordYawDelta(s, yawBeforeTick);
      return currentRotation(s);
   }

   private static void quantizeYaw(RotationStreamPlanner.StreamState s, double gcd, float before, RotationStreamPlanner.AxisQuantizer axis, boolean naturalMouseAngles) {
      float emitted = (float)s.yaw;
      float wrapped = wrapFloatDegrees(emitted);
      boolean forbidden = isQuarterTurn(wrapped);
      if (!forbidden && !Float.isNaN(s.exactYaw)) {
         forbidden = Float.compare(wrapped, wrapFloatDegrees(s.exactYaw)) == 0;
      }

      if (!forbidden && !Float.isNaN(s.exactClosestYaw)) {
         forbidden = Float.compare(wrapped, wrapFloatDegrees(s.exactClosestYaw)) == 0;
      }

      if (!forbidden && naturalMouseAngles) {
         forbidden = isGcdAligned(emitted, gcd);
      }

      if (forbidden) {
         int step = s.kYaw >= 0 ? 1 : -1;

         for (int distance = 1; distance <= (naturalMouseAngles ? 8 : 4); distance++) {
            for (int direction : new int[]{step, -step}) {
               int offset = distance * direction;
               int candidate = s.kYaw + offset;
               if (axis.isAcceptableStep(candidate) && (!naturalMouseAngles || !isQuarterMultiple(Math.abs(candidate * gcd)))) {
                  double corrected = applyGcdSteps(before, candidate, gcd);
                  float next = wrapFloatDegrees((float)corrected);
                  if (!isQuarterTurn(next)
                     && (Float.isNaN(s.exactYaw) || Float.compare(next, wrapFloatDegrees(s.exactYaw)) != 0)
                     && (Float.isNaN(s.exactClosestYaw) || Float.compare(next, wrapFloatDegrees(s.exactClosestYaw)) != 0)) {
                     float delta = Math.abs((float)corrected - before);
                     if ((!naturalMouseAngles || !isGcdAligned((float)corrected, gcd)) && (!(delta > 1.25) || !(Math.abs(delta - Math.abs(s.lastYawDelta)) < 1.0E-4F))) {
                        s.yaw = corrected;
                        s.kYaw = candidate;
                        s.lastYawStep = Math.abs(candidate * gcd);
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean isGcdAligned(float angle, double gcd) {
      float factor = (float)Math.cbrt(gcd / 1.2);
      float quantum = factor * factor * factor * 1.2F;
      return quantum > 0.0F && Math.abs(angle - (angle - angle % quantum)) == 0.0F;
   }

   private static void quantizePitch(RotationStreamPlanner.StreamState s, double gcd, float before, float yawBefore, RotationStreamPlanner.AxisQuantizer axis) {
      float delta = (float)s.pitch - before;
      float yawDelta = (float)s.yaw - yawBefore;
      if (!(Math.abs(yawDelta) + Math.abs(delta) <= 0.75F) && isGcdAligned((float)s.pitch, gcd)) {
         int sign = s.kPitch == 0 ? (before > 0.0F ? -1 : 1) : Integer.signum(s.kPitch);

         for (int distance = 1; distance <= 8; distance++) {
            for (int direction : new int[]{sign, -sign}) {
               int candidate = s.kPitch + distance * direction;
               if (Integer.signum(candidate) == sign && axis.isAcceptableStep(candidate) && !isQuarterMultiple(Math.abs(candidate * gcd))) {
                  double corrected = applyGcdSteps(before, candidate, gcd);
                  if (!isGcdAligned((float)corrected, gcd)) {
                     s.pitch = corrected;
                     s.kPitch = candidate;
                     s.lastPitchStep = Math.abs(candidate * gcd);
                     return;
                  }
               }
            }
         }
      }
   }

   static double applyGcdSteps(double angle, int counts, double gcd) {
      return counts == 0 ? angle : (float)angle + (float)(counts * (gcd / 0.15)) * 0.15F;
   }

   private static void recordYawDelta(RotationStreamPlanner.StreamState s, float before) {
      s.lastYawDelta = (float)s.yaw - before;
   }

   private static boolean isQuarterTurn(float wrappedYaw) {
      float quarter = wrappedYaw / 90.0F;
      return Float.compare(quarter, Math.round(quarter)) == 0;
   }

   private static float wrapFloatDegrees(float yaw) {
      float wrapped = yaw % 360.0F;
      if (wrapped > 180.0F) {
         wrapped -= 360.0F;
      }

      if (wrapped <= -180.0F) {
         wrapped += 360.0F;
      }

      return wrapped;
   }

   public static void setExactYaw(RotationStreamPlanner.StreamState s, float exactYaw, float exactClosestYaw) {
      Objects.requireNonNull(s, "stream");
      s.exactYaw = exactYaw;
      s.exactClosestYaw = exactClosestYaw;
   }

   static double[] offsetGoal(RotationStreamPlanner.StreamState s) {
      return isActive(s) && s.activeOffsets != null
         ? new double[]{s.goalYaw + s.activeOffsets.yawGoalOffset, clampPitch(s.goalPitch + s.activeOffsets.pitchGoalOffset)}
         : null;
   }

   private static RotationStreamPlanner.RolledOffsets rollOffsets(Random random, RotationMath.Rotation goal, RotationStreamPlanner.MotionProfile profile) {
      RotationStreamPlanner.RolledOffsets rolls = new RotationStreamPlanner.RolledOffsets();
      rolls.profile = profile;
      rolls.yawRoll = profile.init_D + profile.load_D * random.nextDouble();
      rolls.pitchRoll = profile.init_D + profile.load_D * random.nextDouble();
      rolls.stepScaleRoll = profile.read_D + profile.write_D * random.nextDouble();
      rolls.accelRoll = profile.update_D + profile.sync_D * random.nextDouble();
      rolls.yawGoalOffset = randomOffset(random);
      rolls.pitchGoalOffset = randomOffset(random);
      rolls.rolledGoalYaw = goal.yaw();
      rolls.rolledGoalPitch = goal.pitch();
      return rolls;
   }

   private static double randomOffset(Random random) {
      double magnitude = 0.75 + 1.25 * random.nextDouble();
      return random.nextBoolean() ? magnitude : -magnitude;
   }

   private static boolean needsReroll(RotationStreamPlanner.StreamState s, RotationMath.Rotation goal, RotationStreamPlanner.MotionProfile profile) {
      if (s.activeOffsets == null) {
         return true;
      }

      if (s.activeOffsets.profile != profile) {
         return true;
      }

      double dYaw = wrapDegrees(goal.yaw() - s.activeOffsets.rolledGoalYaw);
      double dPitch = goal.pitch() - s.activeOffsets.rolledGoalPitch;
      return Math.max(Math.abs(dYaw), Math.abs(dPitch)) > 4.0;
   }

   private static int chooseStepCount(RotationStreamPlanner.AxisApproach axis, int lastK, double peak, double ease, Random random, int dither) {
      double desired = axis.remainingAbs <= 3.0 ? axis.remainingAbs : Math.min(Math.min(axis.remainingAbs * ease, axis.maxStep * peak), axis.accelCap);
      int k = axis.sign * (int)Math.round(desired / axis.stepUnit);
      if (!axis.isSettled() && (k == 0 || Integer.signum(k) != axis.sign)) {
         k = axis.sign;
      }

      while (k != 0 && !axis.acceptsStep(k)) {
         k -= Integer.signum(k);
      }

      if (k == lastK && Math.abs(k * axis.stepUnit) > 1.25) {
         int first = random.nextBoolean() ? 1 : -1;
         if (axis.acceptsStep(k + first)) {
            k += first;
         } else if (axis.acceptsStep(k - first)) {
            k -= first;
         }
      }

      if (dither != 0 && axis.acceptsStep(k + dither) && isNonRepeatingStep(k + dither, lastK, axis.stepUnit)) {
         k += dither;
      }

      if (k != 0 && isRoundStep(Math.abs(k * axis.stepUnit))) {
         int first = random.nextBoolean() ? 1 : -1;
         if (axis.acceptsStep(k + first) && isNonRepeatingStep(k + first, lastK, axis.stepUnit) && !isRoundStep(Math.abs((k + first) * axis.stepUnit))) {
            k += first;
         } else if (axis.acceptsStep(k - first) && isNonRepeatingStep(k - first, lastK, axis.stepUnit) && !isRoundStep(Math.abs((k - first) * axis.stepUnit))) {
            k -= first;
         }
      }

      return k;
   }

   private static boolean isNonRepeatingStep(int k, int lastK, double gcd) {
      return k != lastK || Math.abs(k * gcd) <= 1.25;
   }

   private static int randomNudge(RotationStreamPlanner.AxisApproach axis, Random random) {
      int sign = random.nextBoolean() ? 1 : -1;
      if (axis.acceptsStep(sign)) {
         return sign;
      } else {
         return axis.acceptsStep(-sign) ? -sign : 0;
      }
   }

   private static int nextDither(int state, Random random) {
      double r = random.nextDouble();
      if (state == 0) {
         return r < 0.25 ? (random.nextBoolean() ? 1 : -1) : 0;
      } else if (r < 0.45) {
         return 0;
      } else {
         return r < 0.55 ? -state : state;
      }
   }

   private static double wanderWeight(double w, Random random) {
      w += -0.3 * (w - 1.0) + (random.nextDouble() - 0.5) * 0.08;
      return Math.max(0.9, Math.min(1.1, w));
   }

   private static boolean isQuarterMultiple(double absDelta) {
      return Math.abs(absDelta - 0.25 * Math.round(absDelta / 0.25)) <= 2.0E-5;
   }

   private static boolean isRoundStep(double absDelta) {
      return Math.abs(absDelta - 0.1 * Math.round(absDelta / 0.1)) <= 1.0E-6 || Math.abs(absDelta - 0.25 * Math.round(absDelta / 0.25)) <= 1.0E-6;
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

   private static double clampPitch(double pitch) {
      return Math.max(-89.9, Math.min(89.9, pitch));
   }

   private static final class RolledOffsets {
      RotationStreamPlanner.MotionProfile profile;
      double yawRoll;
      double pitchRoll;
      double stepScaleRoll;
      double accelRoll;
      double yawGoalOffset;
      double pitchGoalOffset;
      double rolledGoalYaw;
      double rolledGoalPitch;
   }

   private static final class AxisQuantizer {
      private final double gcd;
      private final int lastK;
      private final double maxStep;
      private final double minStep;
      private final double currentPitch;
      private final float emitted;

      AxisQuantizer(float cap, double gcd, double lastStep, int lastK, double accelUp, double accelDown, double pitchAcc) {
         this(cap, gcd, lastStep, lastK, accelUp, accelDown, pitchAcc, 0.0F / 0.0F);
      }

      AxisQuantizer(float cap, double gcd, double lastStep, int lastK, double accelUp, double accelDown, double pitchAcc, float emitted) {
         this.gcd = gcd;
         this.lastK = lastK;
         this.maxStep = Math.min(Math.max(0.0, cap), lastStep + accelUp);
         this.minStep = Math.min(this.maxStep, Math.max(0.0, lastStep - accelDown));
         this.currentPitch = pitchAcc;
         this.emitted = emitted;
      }

      int quantizeDelta(double delta, Random random) {
         int k = (int)Math.round(delta / this.gcd);

         while (k != 0 && Math.abs(k * this.gcd) > this.maxStep + 1.0E-9) {
            k -= Integer.signum(k);
         }

         if (Math.abs(k * this.gcd) < this.minStep - 1.0E-9) {
            int dir = k != 0 ? Integer.signum(k) : (this.lastK != 0 ? Integer.signum(this.lastK) : 1);
            k = dir * (int)Math.ceil(this.minStep / this.gcd - 1.0E-9);

            while (k != 0 && Math.abs(k * this.gcd) > this.maxStep + 1.0E-9) {
               k -= Integer.signum(k);
            }
         }

         while (k != 0 && !this.keepsPitchInRange(k)) {
            k -= Integer.signum(k);
         }

         if (k != 0 && RotationStreamPlanner.isQuarterMultiple(Math.abs(k * this.gcd))) {
            int first = random.nextBoolean() ? 1 : -1;
            if (this.withinStepBounds(k + first) && !RotationStreamPlanner.isQuarterMultiple(Math.abs((k + first) * this.gcd))) {
               k += first;
            } else if (this.withinStepBounds(k - first) && !RotationStreamPlanner.isQuarterMultiple(Math.abs((k - first) * this.gcd))) {
               k -= first;
            }
         }

         if (Math.abs(k) == Math.abs(this.lastK) && Math.abs(k * this.gcd) > 1.25) {
            int first = random.nextBoolean() ? 1 : -1;
            if (this.isAcceptableStep(k + first)) {
               k += first;
            } else if (this.isAcceptableStep(k - first)) {
               k -= first;
            }
         }

         if (Math.abs(k * this.gcd) > 1.25 && Math.abs((float)Math.abs(k * this.gcd) - this.emitted) <= (float)this.gcd * 0.5F) {
            int first = random.nextBoolean() ? 1 : -1;
            if (this.isAcceptableStep(k + first)) {
               k += first;
            } else if (this.isAcceptableStep(k - first)) {
               k -= first;
            }
         }

         return k;
      }

      int randomMinimalStep(Random random) {
         int sign = random.nextBoolean() ? 1 : -1;
         if (this.withinStepBounds(sign)) {
            return sign;
         } else {
            return this.withinStepBounds(-sign) ? -sign : 0;
         }
      }

      private boolean withinStepBounds(int k) {
         double delta = Math.abs(k * this.gcd);
         return delta <= this.maxStep + 1.0E-9 && delta >= this.minStep - 1.0E-9 && this.keepsPitchInRange(k);
      }

      private boolean isAcceptableStep(int k) {
         double delta = Math.abs(k * this.gcd);
         return this.withinStepBounds(k)
            && (
               delta <= 1.25
                  || Math.abs(k) != Math.abs(this.lastK) && (Float.isNaN(this.emitted) || Math.abs((float)delta - this.emitted) > (float)this.gcd * 0.5F)
            );
      }

      private boolean keepsPitchInRange(int k) {
         return Double.isNaN(this.currentPitch) || Math.abs(this.currentPitch + k * this.gcd) <= 89.90000000100001;
      }
   }

   public static final class StepResult {
      private final RotationStreamPlanner.StreamState owner;
      private final int kYaw;
      private final int pitchSteps;
      private final double gcd;

      private StepResult(RotationStreamPlanner.StreamState owner, int kYaw, int kPitch, double gcd) {
         this.owner = owner;
         this.kYaw = kYaw;
         this.pitchSteps = kPitch;
         this.gcd = gcd;
      }

      public RotationMath.Rotation resultRotation() {
         return new RotationMath.Rotation(
            (float)RotationStreamPlanner.applyGcdSteps(this.owner.yaw, this.kYaw, this.gcd),
            (float)RotationStreamPlanner.clampPitch(RotationStreamPlanner.applyGcdSteps(this.owner.pitch, this.pitchSteps, this.gcd))
         );
      }
   }

   private static final class AxisApproach {
      final double remainingAbs;
      final int sign;
      final double maxStep;
      final double stepUnit;
      final double accelCap;
      final double currentPitch;
      final boolean settled;

      AxisApproach(double remaining, float cap, double gcd, double lastStep, double accel, double pitchAcc) {
         this.remainingAbs = Math.abs(remaining);
         this.sign = remaining > 0.0 ? 1 : (remaining < 0.0 ? -1 : 0);
         this.maxStep = cap;
         this.stepUnit = gcd;
         this.accelCap = lastStep + accel;
         this.currentPitch = pitchAcc;
         this.settled = !Double.isNaN(pitchAcc)
            && this.sign != 0
            && Math.abs(pitchAcc) <= 89.90000000100001
            && Math.abs(pitchAcc + this.sign * gcd) > 89.90000000100001;
      }

      boolean isSettled() {
         return this.remainingAbs <= 0.5 || this.settled;
      }

      boolean acceptsStep(int k) {
         double delta = Math.abs(k * this.stepUnit);
         if (delta > this.maxStep) {
            return false;
         } else if (delta > this.remainingAbs + this.stepUnit + 1.0E-9) {
            return false;
         } else if (delta > this.accelCap + 1.0E-9) {
            return false;
         } else {
            return this.isSettled() || k != 0 && Integer.signum(k) == this.sign
               ? Double.isNaN(this.currentPitch) || !(Math.abs(this.currentPitch + k * this.stepUnit) > 89.90000000100001)
               : false;
         }
      }
   }

   public enum MotionProfile {
      STANDARD(0.7, 0.3, 0.55, 0.35, 8.0, 10.0),
      DANGER_RETURN(0.97, 0.03, 0.92, 0.06, 16.0, 2.0),
      TELLY_FLICK(0.97, 0.03, 0.97, 0.03, 60.0, 8.0),
      TELLY_AIR_FLICK(0.95, 0.05, 0.985, 0.015, 68.0, 8.0),
      SURROUND_FAST_2(0.8, 0.2, 0.65, 0.3, 11.0, 7.0),
      SURROUND_FAST_3(0.9, 0.1, 0.75, 0.24, 14.0, 4.0),
      SURROUND_FAST_4(0.95, 0.05, 0.91, 0.08, 31.5, 9.0),
      SURROUND_FAST_5(0.97, 0.03, 0.97, 0.02, 62.0, 18.0),
      BED_SHELL(0.88, 0.12, 0.982, 0.016, 46.0, 12.0);

      private final double init_D;
      private final double load_D;
      private final double read_D;
      private final double write_D;
      private final double update_D;
      private final double sync_D;

      MotionProfile(double peakMin, double peakSpan, double easeMin, double easeSpan, double accelMin, double accelSpan) {
         this.init_D = peakMin;
         this.load_D = peakSpan;
         this.read_D = easeMin;
         this.write_D = easeSpan;
         this.update_D = accelMin;
         this.sync_D = accelSpan;
      }
   }

   public static final class StreamState {
      private final Random random;
      private boolean seeded;
      private double yaw;
      private double pitch;
      private double lastYawStep;
      private double lastPitchStep;
      private int kYaw;
      private int kPitch;
      private RotationStreamPlanner.RolledOffsets activeOffsets;
      private RotationStreamPlanner.RolledOffsets pendingOffsets;
      private int yawDither;
      private int pitchDither;
      private float exactYaw = 0.0F / 0.0F;
      private float exactClosestYaw = 0.0F / 0.0F;
      private float lastYawDelta;
      private double wanderWeight = 1.0;
      private double goalYaw;
      private double goalPitch;

      public StreamState() {
         this(new Random());
      }

      public StreamState(Random random) {
         this.random = Objects.requireNonNull(random, "random");
      }
   }
}
