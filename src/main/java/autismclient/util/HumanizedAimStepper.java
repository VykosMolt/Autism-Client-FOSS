package autismclient.util;

import java.util.Random;

public final class HumanizedAimStepper {
   private static final double init_D = 50.0;
   private static final double load_D = 8.0;
   private static final double read_D = 5.7735;
   private static final double write_D = 1.875;
   private static final double update = 14.0;
   private static final double sync = 2.0;
   private static final double flush = 0.75;
   private static final double close = 0.35;
   private static final double open = 0.45;
   private static final double poll = 0.12;
   private static final double push = 20.0;
   private static final double pop = 0.96;
   private final Random init_Random;
   private final HumanHandProfile init_am;
   private boolean init_Z;
   private double tick;
   private double run;
   private boolean load_Z;
   private double get;
   private double set;
   private double put;
   private double add;
   private boolean read_Z;
   private double clear;
   private double reset;
   private double start;
   private double stop;
   private double next;
   private double prev;
   private double valid;
   private double size;
   private double data;
   private double value;
   private int init_I;
   private boolean write_Z;
   private double cache;
   private double state;
   private double queue;
   private double buffer;

   public HumanizedAimStepper(HumanHandProfile hand, Random random) {
      this.init_am = hand;
      this.init_Random = random;
   }

   public static HumanizedAimStepper init_an() {
      Random random = new Random();
      return new HumanizedAimStepper(HumanHandProfile.init_am().init_am_Random(random), random);
   }

   public HumanHandProfile init_am() {
      return this.init_am;
   }

   public void init_V_Z(boolean on) {
      this.write_Z = on;
   }

   public void init_V() {
      this.init_Z = false;
      this.load_Z = false;
      this.read_Z = false;
      this.write_Z = false;
      this.init_I = 0;
      this.put = this.add = 0.0;
      this.cache = this.state = this.queue = this.buffer = 0.0;
   }

   public boolean init_Z() {
      return this.read_Z;
   }

   public double[] init(
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
      if (!this.init_Z) {
         this.init_Z = true;
         this.tick = actualYawDeg;
         this.run = actualPitchDeg;
      } else {
         this.tick = actualYawDeg + init(read(this.tick - actualYawDeg), -0.75, 0.75);
         this.run = actualPitchDeg + init(this.run - actualPitchDeg, -0.75, 0.75);
      }

      double goalYaw = this.tick + read(goalYawDeg - this.tick);
      this.init(goalYaw, goalPitchDeg);
      double errYaw = goalYaw - this.tick;
      double errPitch = goalPitchDeg - this.run;
      if (move) {
         this.tick = this.tick + this.init_am.poll() * this.put;
         this.run = this.run + this.init_am.poll() * this.add;
         if (this.read_Z) {
            this.load();
         } else if (this.init_I > 0) {
            this.init_I--;
         } else if (Math.hypot(errYaw, errPitch) > this.load(tolerance)) {
            this.init(errYaw, errPitch, tolerance, maxYawStep, maxPitchStep);
            this.load();
         } else {
            this.tick = this.tick + this.init_am.push() * errYaw;
            this.run = this.run + this.init_am.push() * errPitch * this.init_am.set();
         }
      }

      this.read();
      if (!move) {
         return new double[]{0.0, 0.0};
      }

      double wantYaw = read(this.tick + this.cache + this.queue - actualYawDeg);
      double wantPitch = this.run + this.state + this.buffer - actualPitchDeg;
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

   private double load(double tolerance) {
      return Math.max(this.init_am.pop(), 0.35 * tolerance);
   }

   private void init(double goalYaw, double goalPitch) {
      if (this.load_Z) {
         double dYaw = goalYaw - this.get;
         double dPitch = goalPitch - this.set;
         if (Math.hypot(dYaw, dPitch) > 8.0) {
            this.put = 0.0;
            this.add = 0.0;
         } else {
            this.put = this.put + 0.45 * (dYaw - this.put);
            this.add = this.add + 0.45 * (dPitch - this.add);
         }
      }

      this.load_Z = true;
      this.get = goalYaw;
      this.set = goalPitch;
   }

   private void init(double errYaw, double errPitch, double tolerance, double maxYawStep, double maxPitchStep) {
      double distance = Math.hypot(errYaw, errPitch);
      double gain = this.write_Z
         ? 0.96 + 0.040000000000000036 * this.init_Random.nextDouble()
         : init(this.init_am.read() + this.init_am.write() * this.init_D(), 0.8, 1.06);
      double ny = -errPitch / distance;
      double np = errYaw / distance;
      double lateral = this.init_am.update() * distance * this.init_D();
      this.clear = errYaw * gain + ny * lateral;
      this.reset = errPitch * gain * this.init_am.set() + np * lateral;
      double length = Math.hypot(this.clear, this.reset);
      this.start = length > 1.0E-9 ? -this.reset / length : 0.0;
      this.stop = length > 1.0E-9 ? this.clear / length : 0.0;
      this.next = (this.init_am.sync() + this.init_am.flush() * init(this.init_D(), -2.5, 2.5)) * length;
      this.size = init(this.init_am.close() * (1.0 + 0.05 * this.init_D()), 0.75, 1.0);
      double width = Math.max(1.0, 2.0 * tolerance);
      double fittsMs = this.init_am.init_D() + this.init_am.load_D() * (Math.log(1.0 + length / width) / Math.log(2.0));
      double ticks = this.write_Z ? 0.0 : fittsMs / 50.0 * Math.exp(this.init_am.open() * this.init_D());
      ticks = Math.max(ticks, Math.sqrt(5.7735 * length / ((this.write_Z ? 20.0 : 14.0) * this.size * this.size)));
      if (maxYawStep > 0.0) {
         ticks = Math.max(ticks, 1.875 * Math.abs(this.clear) / maxYawStep);
      }

      if (maxPitchStep > 0.0) {
         ticks = Math.max(ticks, 1.875 * Math.abs(this.reset) / maxPitchStep);
      }

      this.prev = Math.max(2.0, ticks) * (this.write_Z ? 1.0 + 0.12 * this.init_Random.nextDouble() : 1.0);
      this.valid = 0.0;
      this.data = 0.0;
      this.value = 0.0;
      this.read_Z = true;
   }

   private void load() {
      this.valid++;
      double s = Math.min(1.0, this.valid / this.prev);
      double u = Math.pow(s, this.size);
      double along = init_D_D(u);
      double side = Math.sin(3.141592653589793 * u) * this.next;
      double y = this.clear * along + this.start * side;
      double p = this.reset * along + this.stop * side;
      this.tick = this.tick + (y - this.data);
      this.run = this.run + (p - this.value);
      this.data = y;
      this.value = p;
      if (s >= 1.0) {
         this.read_Z = false;
         this.init_I = this.write_Z
            ? this.init_Random.nextInt(2)
            : this.init_am.init_I() + this.init_Random.nextInt(Math.max(1, this.init_am.load_I() - this.init_am.init_I() + 1));
      }
   }

   public static double init_D_D(double progress) {
      double u = Math.max(0.0, Math.min(1.0, progress));
      return u * u * u * (10.0 + u * (-15.0 + 6.0 * u));
   }

   private void read() {
      double innovation = Math.sqrt(0.8775000000000001);
      this.cache = 0.35 * this.cache + this.init_am.tick() * innovation * this.init_D();
      this.state = 0.35 * this.state + this.init_am.tick() * innovation * this.init_D();
      this.queue = this.queue + (-this.init_am.get() * this.queue + this.init_am.run() * this.init_D());
      this.buffer = this.buffer + (-this.init_am.get() * this.buffer + this.init_am.run() * this.init_D());
   }

   private double init_D() {
      return init(this.init_Random.nextGaussian(), -3.0, 3.0);
   }

   private static double init(double v, double lo, double hi) {
      return Math.max(lo, Math.min(hi, v));
   }

   private static double read(double degrees) {
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
