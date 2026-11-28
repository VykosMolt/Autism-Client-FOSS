package autismclient.util;

import java.util.Random;

public final class HumanizedAimStepper {
   private static final double MS_PER_TICK = 50.0;
   private static final double GOAL_JUMP_DEG = 8.0;
   private static final double SMOOTHERSTEP_PEAK_ACCEL = 5.7735;
   private static final double SMOOTHERSTEP_PEAK_SPEED = 1.875;
   private static final double update = 14.0;
   private static final double sync = 2.0;
   private static final double flush = 0.75;
   private static final double close = 0.35;
   private static final double open = 0.45;
   private static final double poll = 0.12;
   private static final double push = 20.0;
   private static final double pop = 0.96;
   private final Random random;
   private final HumanHandProfile hand;
   private boolean aimInitialized;
   private double aimYaw;
   private double aimPitch;
   private boolean hasLastGoal;
   private double lastGoalYaw;
   private double lastGoalPitch;
   private double goalVelocityYaw;
   private double goalVelocityPitch;
   private boolean stroking;
   private double strokeYaw;
   private double strokePitch;
   private double perpYaw;
   private double perpPitch;
   private double strokeCurve;
   private double strokeTicks;
   private double strokeTick;
   private double strokeEase;
   private double strokeOffsetYaw;
   private double strokeOffsetPitch;
   private int pauseTicks;
   private boolean handBack;
   private double tremorYaw;
   private double tremorPitch;
   private double driftYaw;
   private double driftPitch;

   public HumanizedAimStepper(HumanHandProfile hand, Random random) {
      this.hand = hand;
      this.random = random;
   }

   public static HumanizedAimStepper create() {
      Random random = new Random();
      return new HumanizedAimStepper(HumanHandProfile.shared().forSession(random), random);
   }

   public HumanHandProfile hand() {
      return this.hand;
   }

   public void setHandBack(boolean on) {
      this.handBack = on;
   }

   public void reset() {
      this.aimInitialized = false;
      this.hasLastGoal = false;
      this.stroking = false;
      this.handBack = false;
      this.pauseTicks = 0;
      this.goalVelocityYaw = this.goalVelocityPitch = 0.0;
      this.tremorYaw = this.tremorPitch = this.driftYaw = this.driftPitch = 0.0;
   }

   public boolean isStroking() {
      return this.stroking;
   }

   public double[] step(
      double goalYawDeg,
      double goalPitchDeg,
      double actualYawDeg,
      double actualPitchDeg,
      double toleranceDeg,
      double maxYawStep,
      double maxPitchStep,
      boolean move
   ) {
      double tolerance = Math.max(0.5, toleranceDeg);
      if (!this.aimInitialized) {
         this.aimInitialized = true;
         this.aimYaw = actualYawDeg;
         this.aimPitch = actualPitchDeg;
      } else {
         this.aimYaw = actualYawDeg + clamp(wrapDegrees(this.aimYaw - actualYawDeg), -0.75, 0.75);
         this.aimPitch = actualPitchDeg + clamp(this.aimPitch - actualPitchDeg, -0.75, 0.75);
      }

      double goalYaw = this.aimYaw + wrapDegrees(goalYawDeg - this.aimYaw);
      this.trackGoalVelocity(goalYaw, goalPitchDeg);
      double errYaw = goalYaw - this.aimYaw;
      double errPitch = goalPitchDeg - this.aimPitch;
      if (move) {
         this.aimYaw = this.aimYaw + this.hand.pursuitGain() * this.goalVelocityYaw;
         this.aimPitch = this.aimPitch + this.hand.pursuitGain() * this.goalVelocityPitch;
         if (this.stroking) {
            this.advanceStroke();
         } else if (this.pauseTicks > 0) {
            this.pauseTicks--;
         } else if (Math.hypot(errYaw, errPitch) > this.settleTolerance(tolerance)) {
            this.beginStroke(errYaw, errPitch, tolerance, maxYawStep, maxPitchStep);
            this.advanceStroke();
         } else {
            this.aimYaw = this.aimYaw + this.hand.push() * errYaw;
            this.aimPitch = this.aimPitch + this.hand.push() * errPitch * this.hand.pitchGainScale();
         }
      }

      this.updateNoise();
      if (!move) {
         return new double[]{0.0, 0.0};
      }

      double wantYaw = wrapDegrees(this.aimYaw + this.tremorYaw + this.driftYaw - actualYawDeg);
      double wantPitch = this.aimPitch + this.tremorPitch + this.driftPitch - actualPitchDeg;
      double scale = 1.0;
      if (maxYawStep > 0.0 && Math.abs(wantYaw) > maxYawStep) {
         scale = Math.min(scale, maxYawStep / Math.abs(wantYaw));
      }

      if (maxPitchStep > 0.0 && Math.abs(wantPitch) > maxPitchStep) {
         scale = Math.min(scale, maxPitchStep / Math.abs(wantPitch));
      }

      if (maxPitchStep <= 0.0) {
         wantPitch = 0.0;
      }

      return new double[]{wantYaw * scale, wantPitch * scale};
   }

   private double settleTolerance(double tolerance) {
      return Math.max(this.hand.minSettleTolerance(), 0.35 * tolerance);
   }

   private void trackGoalVelocity(double goalYaw, double goalPitch) {
      if (this.hasLastGoal) {
         double dYaw = goalYaw - this.lastGoalYaw;
         double dPitch = goalPitch - this.lastGoalPitch;
         if (Math.hypot(dYaw, dPitch) > 8.0) {
            this.goalVelocityYaw = 0.0;
            this.goalVelocityPitch = 0.0;
         } else {
            this.goalVelocityYaw = this.goalVelocityYaw + 0.45 * (dYaw - this.goalVelocityYaw);
            this.goalVelocityPitch = this.goalVelocityPitch + 0.45 * (dPitch - this.goalVelocityPitch);
         }
      }

      this.hasLastGoal = true;
      this.lastGoalYaw = goalYaw;
      this.lastGoalPitch = goalPitch;
   }

   private void beginStroke(double errYaw, double errPitch, double tolerance, double maxYawStep, double maxPitchStep) {
      double distance = Math.hypot(errYaw, errPitch);
      double gain = this.handBack
         ? 0.96 + 0.040000000000000036 * this.random.nextDouble()
         : clamp(this.hand.firstPhaseWeight() + this.hand.firstPhaseJitter() * this.clampedGaussian(), 0.8, 1.06);
      double ny = -errPitch / distance;
      double np = errYaw / distance;
      double lateral = this.hand.lateralJitter() * distance * this.clampedGaussian();
      this.strokeYaw = errYaw * gain + ny * lateral;
      this.strokePitch = errPitch * gain * this.hand.pitchGainScale() + np * lateral;
      double length = Math.hypot(this.strokeYaw, this.strokePitch);
      this.perpYaw = length > 1.0E-9 ? -this.strokePitch / length : 0.0;
      this.perpPitch = length > 1.0E-9 ? this.strokeYaw / length : 0.0;
      this.strokeCurve = (this.hand.curvature() + this.hand.curvatureJitter() * clamp(this.clampedGaussian(), -2.5, 2.5)) * length;
      this.strokeEase = clamp(this.hand.strokeEase() * (1.0 + 0.05 * this.clampedGaussian()), 0.75, 1.0);
      double width = Math.max(1.0, 2.0 * tolerance);
      double fittsMs = this.hand.fittsIntercept() + this.hand.fittsSlope() * (Math.log(1.0 + length / width) / Math.log(2.0));
      double ticks = this.handBack ? 0.0 : fittsMs / 50.0 * Math.exp(this.hand.open() * this.clampedGaussian());
      ticks = Math.max(ticks, Math.sqrt(5.7735 * length / ((this.handBack ? 20.0 : 14.0) * this.strokeEase * this.strokeEase)));
      if (maxYawStep > 0.0) {
         ticks = Math.max(ticks, 1.875 * Math.abs(this.strokeYaw) / maxYawStep);
      }

      if (maxPitchStep > 0.0) {
         ticks = Math.max(ticks, 1.875 * Math.abs(this.strokePitch) / maxPitchStep);
      }

      this.strokeTicks = Math.max(2.0, ticks) * (this.handBack ? 1.0 + 0.12 * this.random.nextDouble() : 1.0);
      this.strokeTick = 0.0;
      this.strokeOffsetYaw = 0.0;
      this.strokeOffsetPitch = 0.0;
      this.stroking = true;
   }

   private void advanceStroke() {
      this.strokeTick++;
      double s = Math.min(1.0, this.strokeTick / this.strokeTicks);
      double u = Math.pow(s, this.strokeEase);
      double along = smootherStep(u);
      double side = Math.sin(3.141592653589793 * u) * this.strokeCurve;
      double y = this.strokeYaw * along + this.perpYaw * side;
      double p = this.strokePitch * along + this.perpPitch * side;
      this.aimYaw = this.aimYaw + (y - this.strokeOffsetYaw);
      this.aimPitch = this.aimPitch + (p - this.strokeOffsetPitch);
      this.strokeOffsetYaw = y;
      this.strokeOffsetPitch = p;
      if (s >= 1.0) {
         this.stroking = false;
         this.pauseTicks = this.handBack
            ? this.random.nextInt(2)
            : this.hand.minPauseTicks() + this.random.nextInt(Math.max(1, this.hand.maxPauseTicks() - this.hand.minPauseTicks() + 1));
      }
   }

   public static double smootherStep(double progress) {
      double u = Math.max(0.0, Math.min(1.0, progress));
      return u * u * u * (10.0 + u * (-15.0 + 6.0 * u));
   }

   private void updateNoise() {
      double innovation = Math.sqrt(0.8775000000000001);
      this.tremorYaw = 0.35 * this.tremorYaw + this.hand.tick() * innovation * this.clampedGaussian();
      this.tremorPitch = 0.35 * this.tremorPitch + this.hand.tick() * innovation * this.clampedGaussian();
      this.driftYaw = this.driftYaw + (-this.hand.driftDecay() * this.driftYaw + this.hand.driftNoise() * this.clampedGaussian());
      this.driftPitch = this.driftPitch + (-this.hand.driftDecay() * this.driftPitch + this.hand.driftNoise() * this.clampedGaussian());
   }

   private double clampedGaussian() {
      return clamp(this.random.nextGaussian(), -3.0, 3.0);
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
}
