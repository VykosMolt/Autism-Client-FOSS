package autismclient.util;

import java.util.Random;

public final class HumanizedTurnPlanner {
   private static final double init_D = 24.0;
   private static final double load = 2.0;
   private final HumanHandProfile hand;
   private final Random random;
   private double startYaw;
   private double startPitch;
   private double yawDelta;
   private double durationTicks;
   private double firstPhaseEnd;
   private double finishPhaseStart;
   private double firstPhaseWeight;
   private double firstPhaseExponent;
   private double timeWobble;
   private double pitchArcOffset;
   private double pitchArcWave;
   private double pitchArcPhase;
   private double progress;
   private double lastYaw;
   private double lastPitch;
   private double tickYawMoved;
   private double tickPitchMoved;
   private long init_J = -9223372036854775808L;
   private boolean init_Z;

   public HumanizedTurnPlanner(HumanHandProfile hand, Random random) {
      this.hand = hand;
      this.random = random;
   }

   public void start(float yaw, float pitch, float targetYaw, int ticks) {
      this.reset();
      if (Float.isFinite(yaw) && Float.isFinite(pitch) && Float.isFinite(targetYaw)) {
         this.startYaw = this.lastYaw = yaw;
         this.startPitch = this.lastPitch = clamp(pitch, -90.0, 90.0);
         this.yawDelta = wrapDegrees((double)targetYaw - yaw);
         double distance = Math.abs(this.yawDelta);
         double tempo = clamp(1.0 + this.hand.open() * 0.55 * this.gaussianJitter(), 0.9, 1.12);
         this.durationTicks = Math.max(1, ticks) * tempo;
         this.durationTicks = Math.max(this.durationTicks, Math.sqrt(8.0 * distance / 14.0));
         this.durationTicks = Math.max(this.durationTicks, 2.7 * distance / 24.0);
         this.firstPhaseEnd = 0.8 + 0.06 * this.random.nextDouble();
         this.finishPhaseStart = this.firstPhaseEnd - 0.12 - 0.05 * this.random.nextDouble();
         this.firstPhaseWeight = clamp(this.hand.firstPhaseWeight() + this.hand.firstPhaseJitter() * 0.35 * this.gaussianJitter(), 0.9, 0.98);
         this.firstPhaseExponent = clamp(this.hand.strokeEase() + 0.025 * this.gaussianJitter(), 0.78, 1.0);
         this.timeWobble = 0.07 * this.gaussianJitter();
         this.pitchArcOffset = clamp((this.hand.curvature() + this.hand.curvatureJitter() * this.gaussianJitter()) * distance * 0.35, -0.28, 0.28);
         this.pitchArcWave = Math.min(distance * 0.006, this.hand.tick() * 0.55);
         this.pitchArcPhase = this.random.nextDouble() * 3.141592653589793 * 2.0;
         this.init_Z = true;
      }
   }

   public boolean isActive() {
      return this.init_Z;
   }

   public double duration() {
      return this.durationTicks;
   }

   public HumanizedTurnPlanner.TurnStep step(double elapsedTicks, float actualYaw, float actualPitch, double quantum) {
      if (!this.init_Z) {
         return new HumanizedTurnPlanner.TurnStep(0L, 0L, true);
      }

      if (Double.isFinite(elapsedTicks) && Float.isFinite(actualYaw) && Float.isFinite(actualPitch) && Double.isFinite(quantum) && !(quantum <= 1.0E-8)) {
         this.progress = Math.max(this.progress, clamp(elapsedTicks / this.durationTicks, 0.0, 1.0));
         double time = this.progress + this.timeWobble * Math.sin(6.283185307179586 * this.progress) * this.progress * (1.0 - this.progress);
         double first = HumanizedAimStepper.smootherStep(Math.pow(clamp(time / this.firstPhaseEnd, 0.0, 1.0), this.firstPhaseExponent));
         double finish = HumanizedAimStepper.smootherStep((time - this.finishPhaseStart) / (1.0 - this.finishPhaseStart));
         double along = this.firstPhaseWeight * first + (1.0 - this.firstPhaseWeight) * finish;
         double envelope = 64.0 * Math.pow(this.progress * (1.0 - this.progress), 3.0);
         double pitchArc = envelope * (this.pitchArcOffset + this.pitchArcWave * Math.sin(9.42477796076938 * this.progress + this.pitchArcPhase));
         double goalYaw = this.startYaw + this.yawDelta * along;
         double goalPitch = clamp(this.startPitch + pitchArc, -90.0, 90.0);
         if (this.progress >= 1.0) {
            goalYaw = this.startYaw + this.yawDelta;
            goalPitch = this.startPitch;
         }

         long yawCounts = Math.round(wrapDegrees(goalYaw - actualYaw) / quantum);
         long pitchCounts = Math.round((goalPitch - actualPitch) / quantum);
         if (this.progress >= 1.0 && yawCounts == 0L && pitchCounts == 0L) {
            this.init_Z = false;
            return new HumanizedTurnPlanner.TurnStep(0L, 0L, true);
         }

         long nowTick = Math.max(this.init_J, (long)Math.floor(Math.max(0.0, elapsedTicks)));
         if (nowTick != this.init_J) {
            this.init_J = nowTick;
            this.tickYawMoved = this.tickPitchMoved = 0.0;
         } else {
            this.tickYawMoved = this.tickYawMoved + Math.abs(wrapDegrees(actualYaw - this.lastYaw));
            this.tickPitchMoved = this.tickPitchMoved + Math.abs(actualPitch - this.lastPitch);
         }

         this.lastYaw = actualYaw;
         this.lastPitch = actualPitch;
         long yawBudget = (long)Math.floor(Math.max(0.0, 24.0 - this.tickYawMoved) / quantum + 1.0E-5);
         long pitchBudget = (long)Math.floor(Math.max(0.0, 2.0 - this.tickPitchMoved) / quantum + 1.0E-5);
         return new HumanizedTurnPlanner.TurnStep(Math.clamp(yawCounts, -yawBudget, yawBudget), Math.clamp(pitchCounts, -pitchBudget, pitchBudget), false);
      } else {
         return new HumanizedTurnPlanner.TurnStep(0L, 0L, false);
      }
   }

   public void reset() {
      this.init_Z = false;
      this.progress = this.tickYawMoved = this.tickPitchMoved = 0.0;
      this.init_J = -9223372036854775808L;
   }

   private double gaussianJitter() {
      return clamp(this.random.nextGaussian(), -2.0, 2.0);
   }

   private static double clamp(double v, double lo, double hi) {
      return Math.max(lo, Math.min(hi, v));
   }

   private static double wrapDegrees(double value) {
      double angle = value % 360.0;
      if (angle >= 180.0) {
         angle -= 360.0;
      }

      if (angle < -180.0) {
         angle += 360.0;
      }

      return angle;
   }

   public record TurnStep(long yawCounts, long pitchCounts, boolean done) {
   }
}
