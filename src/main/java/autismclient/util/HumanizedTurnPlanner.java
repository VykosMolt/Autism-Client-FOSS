package autismclient.util;

import java.util.Random;

public final class HumanizedTurnPlanner {
   private static final double init_D = 24.0;
   private static final double load = 2.0;
   private final HumanHandProfile init_am;
   private final Random init_Random;
   private double read;
   private double write;
   private double update;
   private double sync;
   private double flush;
   private double close;
   private double open;
   private double poll;
   private double push;
   private double pop;
   private double tick;
   private double run;
   private double get;
   private double set;
   private double put;
   private double add;
   private double clear;
   private long init_J = -9223372036854775808L;
   private boolean init_Z;

   public HumanizedTurnPlanner(HumanHandProfile hand, Random random) {
      this.init_am = hand;
      this.init_Random = random;
   }

   public void start(float yaw, float pitch, float targetYaw, int ticks) {
      this.reset();
      if (Float.isFinite(yaw) && Float.isFinite(pitch) && Float.isFinite(targetYaw)) {
         this.read = this.set = yaw;
         this.write = this.put = init(pitch, -90.0, 90.0);
         this.update = init((double)targetYaw - yaw);
         double distance = Math.abs(this.update);
         double tempo = init(1.0 + this.init_am.open() * 0.55 * this.load(), 0.9, 1.12);
         this.sync = Math.max(1, ticks) * tempo;
         this.sync = Math.max(this.sync, Math.sqrt(8.0 * distance / 14.0));
         this.sync = Math.max(this.sync, 2.7 * distance / 24.0);
         this.flush = 0.8 + 0.06 * this.init_Random.nextDouble();
         this.close = this.flush - 0.12 - 0.05 * this.init_Random.nextDouble();
         this.open = init(this.init_am.read() + this.init_am.write() * 0.35 * this.load(), 0.9, 0.98);
         this.poll = init(this.init_am.close() + 0.025 * this.load(), 0.78, 1.0);
         this.push = 0.07 * this.load();
         this.pop = init((this.init_am.sync() + this.init_am.flush() * this.load()) * distance * 0.35, -0.28, 0.28);
         this.tick = Math.min(distance * 0.006, this.init_am.tick() * 0.55);
         this.run = this.init_Random.nextDouble() * 3.141592653589793 * 2.0;
         this.init_Z = true;
      }
   }

   public boolean isActive() {
      return this.init_Z;
   }

   public double duration() {
      return this.sync;
   }

   public HumanizedTurnPlanner.Inner_DataUtil step(double elapsedTicks, float actualYaw, float actualPitch, double quantum) {
      if (!this.init_Z) {
         return new HumanizedTurnPlanner.Inner_DataUtil(0L, 0L, true);
      }

      if (Double.isFinite(elapsedTicks) && Float.isFinite(actualYaw) && Float.isFinite(actualPitch) && Double.isFinite(quantum) && !(quantum <= 1.0E-8)) {
         this.get = Math.max(this.get, init(elapsedTicks / this.sync, 0.0, 1.0));
         double time = this.get + this.push * Math.sin(6.283185307179586 * this.get) * this.get * (1.0 - this.get);
         double first = HumanizedAimStepper.smootherStep(Math.pow(init(time / this.flush, 0.0, 1.0), this.poll));
         double finish = HumanizedAimStepper.smootherStep((time - this.close) / (1.0 - this.close));
         double along = this.open * first + (1.0 - this.open) * finish;
         double envelope = 64.0 * Math.pow(this.get * (1.0 - this.get), 3.0);
         double pitchArc = envelope * (this.pop + this.tick * Math.sin(9.42477796076938 * this.get + this.run));
         double goalYaw = this.read + this.update * along;
         double goalPitch = init(this.write + pitchArc, -90.0, 90.0);
         if (this.get >= 1.0) {
            goalYaw = this.read + this.update;
            goalPitch = this.write;
         }

         long yawCounts = Math.round(init(goalYaw - actualYaw) / quantum);
         long pitchCounts = Math.round((goalPitch - actualPitch) / quantum);
         if (this.get >= 1.0 && yawCounts == 0L && pitchCounts == 0L) {
            this.init_Z = false;
            return new HumanizedTurnPlanner.Inner_DataUtil(0L, 0L, true);
         }

         long nowTick = Math.max(this.init_J, (long)Math.floor(Math.max(0.0, elapsedTicks)));
         if (nowTick != this.init_J) {
            this.init_J = nowTick;
            this.add = this.clear = 0.0;
         } else {
            this.add = this.add + Math.abs(init(actualYaw - this.set));
            this.clear = this.clear + Math.abs(actualPitch - this.put);
         }

         this.set = actualYaw;
         this.put = actualPitch;
         long yawBudget = (long)Math.floor(Math.max(0.0, 24.0 - this.add) / quantum + 1.0E-5);
         long pitchBudget = (long)Math.floor(Math.max(0.0, 2.0 - this.clear) / quantum + 1.0E-5);
         return new HumanizedTurnPlanner.Inner_DataUtil(Math.clamp(yawCounts, -yawBudget, yawBudget), Math.clamp(pitchCounts, -pitchBudget, pitchBudget), false);
      } else {
         return new HumanizedTurnPlanner.Inner_DataUtil(0L, 0L, false);
      }
   }

   public void reset() {
      this.init_Z = false;
      this.get = this.add = this.clear = 0.0;
      this.init_J = -9223372036854775808L;
   }

   private double load() {
      return init(this.init_Random.nextGaussian(), -2.0, 2.0);
   }

   private static double init(double v, double lo, double hi) {
      return Math.max(lo, Math.min(hi, v));
   }

   private static double init(double value) {
      double angle = value % 360.0;
      if (angle >= 180.0) {
         angle -= 360.0;
      }

      if (angle < -180.0) {
         angle += 360.0;
      }

      return angle;
   }

   public record Inner_DataUtil(long yawCounts, long load, boolean done) {
   }
}
