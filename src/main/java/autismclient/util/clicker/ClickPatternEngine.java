package autismclient.util.clicker;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public final class ClickPatternEngine {
   public static final long init_J = 33333334L;
   public static final long load_J = 2000000L;
   public static final long read_J = 1000000L;
   public static final long write_J = 120000000L;
   private static final long update_J = -9223372036854775808L;
   private static final double load_D = 6.283185307179586;
   static final double init_D = 2.5;
   private static final double read_D = 6.0;
   private static final double write_D = 0.08;
   private static final double update_D = 0.06;
   private static final double sync_D = 0.25;
   private static final double flush_D = 0.15;
   private static final double close_D = 0.2;
   private static final double open_D = 0.65;
   private static final double poll_D = 0.7;
   private static final double push_D = 0.03333333333333333;
   private static final double pop_D = 0.06666666666666667;
   private static final double tick_D = 0.2;
   private static final double run_D = 0.1;
   private static final double get_D = 1.5;
   private static final long sync_J = 1000000000L;
   private static final double set_D = 0.25;
   private static final double put_D = 0.5;
   private static final double add_D = 0.06;
   private static final double clear_D = 0.04;
   private static final double reset_D = 2.0;
   private static final double start_D = 0.01;
   private static final double stop_D = 0.006;
   private static final double next_D = 0.25;
   private static final double prev = 0.1;
   private static final double valid = 0.5;
   private static final long flush_J = 1000000L;
   private static final int init_I = 6000;
   private static final double size = 0.7;
   private static final double data = 0.35;
   private static final double value = -0.8;
   private static final double cache = 1.6;
   private static final double state = 3.0;
   private static final double queue = 0.25;
   private static final int load_I = 24;
   private static final long close_J = 15000000L;
   private static final double buffer = 11.0;
   private static final int read_I = 3;
   private static final double entry = 0.8;
   private static final double node = 0.4;
   private static final double task = 0.4;
   private static final double pool = 2.2;
   private static final double index = 0.86;
   private static final double count = 1.15;
   private static final double flags = 0.25;
   private final Random random;
   private ClickPatternEngine.Inner_ConfigStore personality;
   private final Map<ClickTechnique, ClickPatternEngine.Inner_CachePool> generators = new EnumMap<>(ClickTechnique.class);
   private ClickProfile profile = ClickProfile.forTechnique(ClickTechnique.NORMAL);
   private ClickPatternEngine.Inner_CachePool activeGenerator;
   private double level;
   private double frame;
   private long open_J = -9223372036854775808L;
   private double block;
   private long poll_J = -9223372036854775808L;
   private double chunk;
   private long push_J;
   private double table;
   private long pop_J = -9223372036854775808L;
   private long tick_J = -9223372036854775808L;
   private long run_J = -9223372036854775808L;
   private boolean init_Z;
   private boolean load_Z;
   private long get_J = -9223372036854775808L;
   private long set_J = -9223372036854775808L;
   private long put_J = -9223372036854775808L;
   private int write_I = -1;
   private long add_J;
   private long clear_J;
   private double store = 1.0;
   private long reset_J;
   private long start_J;
   private long stop_J;
   private long next_J;

   public void setSpeedMultiplier(double multiplier) {
      this.store = Double.isFinite(multiplier) ? Math.max(1.0, Math.min(2.0, multiplier)) : 1.0;
   }

   public ClickPatternEngine() {
      this(new Random(), ClickPatternEngine.Inner_ConfigStore.DEFAULT);
   }

   public ClickPatternEngine(Random seed, ClickPatternEngine.Inner_ConfigStore personality) {
      this.random = seed == null ? new Random() : seed;
      this.personality = personality == null ? ClickPatternEngine.Inner_ConfigStore.DEFAULT : personality;
      this.level = ClickTechnique.NORMAL.sync_D * 2.5 * this.random.nextGaussian();
      this.frame = 0.2 * this.random.nextGaussian();
      this.generators.put(ClickTechnique.NORMAL, new ClickPatternEngine.Inner_BufferHelper(this.random.nextLong()));
      this.generators.put(ClickTechnique.JITTER, new ClickPatternEngine.Inner_StreamUtil(this.random.nextLong()));
      this.generators.put(ClickTechnique.BUTTERFLY, new ClickPatternEngine.Inner_DataUtil(this.random.nextLong()));
      this.generators.put(ClickTechnique.DRAG, new ClickPatternEngine.Inner_JsonUtil(this.random.nextLong(), false));
      this.generators.put(ClickTechnique.SCROLL, new ClickPatternEngine.Inner_JsonUtil(this.random.nextLong(), true));
      this.generators.put(ClickTechnique.RAGE, new ClickPatternEngine.Inner_TaskQueue(this.random.nextLong()));
      this.activeGenerator = this.generators.get(ClickTechnique.NORMAL);
   }

   public void setPersonality(ClickPatternEngine.Inner_ConfigStore personality) {
      if (personality != null) {
         this.personality = personality;
      }
   }

   public void init(long now, boolean active, ClickProfile profile, ClickPatternEngine.Inner_NetHelper sink) {
      if (profile != null) {
         this.profile = profile;
      }

      this.read(now);
      this.init_V_J_Z(now, active);
      if (this.load_Z && now >= this.get_J) {
         this.init_V_J(now, this.get_J, sink);
      }

      if (!active) {
         this.load_V();
      } else {
         ClickPatternEngine.Inner_CachePool wanted = this.generators.get(this.profile.technique);
         if (wanted != this.activeGenerator) {
            this.activeGenerator = wanted;
            this.load_V();
         }

         if (this.tick_J == -9223372036854775808L) {
            this.load_V_J(now);
         }

         this.load(now, sink);
      }
   }

   public void init_V_J_DataUtil_NetHelper(long now, ClickPatternEngine.Inner_NetHelper sink) {
      if (this.load_Z) {
         this.init_V_J(now, now, sink);
      }

      this.load_V();
   }

   public void init_V_J(long now) {
      this.set_J = now;
      this.load_V();
   }

   public void applyProfile(long now, ClickProfile profile, ClickPatternEngine.Inner_NetHelper sink) {
      if (profile != null) {
         this.profile = profile;
      }

      this.activeGenerator = this.generators.get(this.profile.technique);
      this.init_V_J(now);
      this.load_Z = true;
      this.get_J = now + Math.max(1000000L, this.activeGenerator.init_J());
      if (sink != null) {
         sink.onClick(now);
      }
   }

   public long nextClickNanos(long now) {
      if (this.tick_J == -9223372036854775808L) {
         return 9223372036854775807L;
      }

      long earliest = Math.max(this.tick_J + this.push_J, Math.max(offsetUnlessUnset(this.set_J, this.close()), offsetUnlessUnset(this.put_J, 2000000L)));
      if (this.load_Z) {
         earliest = Math.max(earliest, this.get_J + 2000000L);
      }

      return Math.max(0L, earliest - now);
   }

   public boolean init_Z() {
      return this.load_Z;
   }

   public long init_J() {
      return this.set_J;
   }

   public double init_D() {
      return this.block;
   }

   public long load_J() {
      return this.add_J;
   }

   public long read_J() {
      return this.reset_J;
   }

   public long write_J() {
      return this.start_J;
   }

   public long update() {
      return this.stop_J;
   }

   public long sync() {
      return this.next_J;
   }

   public long flush() {
      return this.clear_J;
   }

   private void load_V_J(long now) {
      long start = now;
      if (this.set_J != -9223372036854775808L && now - this.set_J < this.profile.maxIntervalNanos) {
         long interval = this.profile.technique.init_Z()
            ? this.init(now - this.push_J, 0.0, this.open(), this.poll(), 0.0, this.activeGenerator.init)
            : this.close();
         start = Math.max(now, this.set_J + interval);
      }

      this.activeGenerator.init_V_J(start - this.push_J);
      this.activeGenerator.init_V();
   }

   private void load(long now, ClickPatternEngine.Inner_NetHelper sink) {
      for (int attempt = 0; attempt < 3; attempt++) {
         if (this.tick_J == -9223372036854775808L || this.load_Z) {
            return;
         }

         long due = this.tick_J + this.push_J;
         if (due > now) {
            return;
         }

         if (now - due <= 120000000L) {
            long earliest = Math.max(offsetUnlessUnset(this.set_J, this.close()), offsetUnlessUnset(this.put_J, 2000000L));
            if (now < earliest) {
               if (!this.init_Z) {
                  this.start_J++;
                  this.init_Z = true;
               }

               return;
            }

            if (sink != null && sink.init_Z_J(now)) {
               this.init_V_J_J(now, Math.max(now, sink.earliestClickNanos(now)));
               return;
            }

            this.stop_J++;
            return;
         }

         this.next_J++;
         this.activeGenerator.load_V(now - this.push_J);
      }
   }

   private void init_V_J_J(long now, long at) {
      this.reset_J++;
      if (this.set_J != -9223372036854775808L) {
         long interval = at - this.set_J;
         if (interval > 0L && interval < 1000000000L) {
            this.chunk = this.chunk + 0.2 * (1.0E9 / interval - this.chunk);
         }
      }

      this.set_J = at;
      this.load_Z = true;
      long hold = Math.max(1000000L, this.run_J - this.tick_J);
      long late = now - (this.tick_J + this.push_J);
      if (late > 1.5 * this.table) {
         this.push_J = this.push_J + (late - (long)(0.5 * this.table));
      }

      this.get_J = at + this.activeGenerator.init_J_J(hold);
      this.activeGenerator.read(at - this.push_J);
      this.activeGenerator.init_V();
      this.init_V();
   }

   private void init_V() {
      double every = this.load_D();
      if (every <= 0.0) {
         this.write_I = -1;
      } else {
         if (this.write_I < 0) {
            this.write_I = this.init_I_D(every);
         }

         if (this.write_I-- <= 0) {
            this.write_I = this.init_I_D(every);
            double size = 0.8 * Math.exp(0.4 * this.random.nextGaussian());
            this.push_J = this.push_J + (long)(Math.max(0.4, Math.min(2.2, size)) * this.store * this.push());
            this.clear_J++;
         }
      }
   }

   private double load_D() {
      ClickTechnique technique = this.profile.technique;
      boolean lapses = technique == ClickTechnique.NORMAL
         || technique == ClickTechnique.JITTER
         || technique == ClickTechnique.BUTTERFLY;
      return lapses && this.profile.sync > 0.0 ? 11.0 / this.profile.sync : 0.0;
   }

   private int init_I_D(double every) {
      double sum = 0.0;

      for (int i = 0; i < 3; i++) {
         sum -= Math.log(1.0 - this.random.nextDouble());
      }

      return Math.max(2, (int)Math.round(sum * every / 3.0));
   }

   private double init_D_J_J(long fastNs, long slowNs) {
      double every = this.load_D();
      if (!(every <= 0.0) && slowNs > fastNs) {
         double mid = 0.5 * (fastNs + slowNs);
         double target = mid - 0.86 * this.store * this.push() / every;
         double f = Math.max(0.25, Math.min(0.5, (target - fastNs) / (slowNs - fastNs)));
         return 1.15 * Math.log(f / (1.0 - f));
      } else {
         return 0.0;
      }
   }

   private void read(long now) {
      if (this.pop_J != -9223372036854775808L) {
         long dt = now - this.pop_J;
         if (dt > 0L) {
            if (this.table <= 0.0) {
               this.table = Math.min(dt, 120000000L);
            } else {
               this.table = this.table + 0.1 * (Math.min(dt, 2.0 * this.table) - this.table);
            }
         }
      }

      this.pop_J = now;
   }

   private void init_V_J(long now, long due, ClickPatternEngine.Inner_NetHelper sink) {
      this.load_Z = false;
      this.get_J = -9223372036854775808L;
      this.put_J = Math.min(now, due);
      if (sink != null) {
         sink.init_V_J(now);
      }
   }

   private void load_V() {
      this.tick_J = -9223372036854775808L;
      this.run_J = -9223372036854775808L;
      this.init_Z = false;
   }

   private void load(long down, long up) {
      this.tick_J = down;
      this.run_J = Math.max(up, down + 1000000L);
      this.init_Z = false;
      this.add_J++;
   }

   private long close() {
      return Math.max(this.profile.minIntervalNanos, 33333334L);
   }

   private long open() {
      return this.profile.minIntervalNanos;
   }

   private long poll() {
      return this.profile.maxIntervalNanos;
   }

   private long push() {
      return this.open() + (this.poll() - this.open()) / 2L;
   }

   private static long offsetUnlessUnset(long time, long delay) {
      return time == -9223372036854775808L ? -9223372036854775808L : time + delay;
   }

   private double timingSpread() {
      return 2.5 * this.profile.sync * this.personality.init_D();
   }

   private void write(long at) {
      if (this.open_J == -9223372036854775808L) {
         this.open_J = at;
      } else if (at > this.open_J) {
         double dt = (at - this.open_J) / 1.0E9;
         this.open_J = at;
         double spread = this.timingSpread();
         this.level = init(
            this.level, dt, this.profile.technique.update_D, this.profile.technique.sync_D * spread, this.random
         );
         this.frame = init(this.frame, dt, 6.0, 0.08 * spread, this.random);
      }
   }

   private long init(long at, double envelope, long fastNs, long slowNs, double boundary, Random r) {
      this.write(at);
      if (slowNs <= fastNs) {
         return fastNs;
      }

      double latent = this.level
         + this.frame
         + this.profile.technique.flush * this.timingSpread() * r.nextGaussian()
         + boundary
         + envelope
         + 0.7 * this.profile.flush * this.block
         + this.init_D_J_J(fastNs, slowNs);
      return fastNs + (long)Math.ceil((slowNs - fastNs) * sigmoid(latent));
   }

   private long init_J_J(long interval, double fraction, double state) {
      long high = Math.min(ClickTimingMath.load(this.profile.update), Math.min((long)(fraction * interval), interval - 2000000L));
      high = Math.max(1000000L, high);
      long low = Math.min(Math.max(1000000L, ClickTimingMath.load(this.profile.write)), high);
      return low + (long)Math.ceil((high - low) * sigmoid(state));
   }

   private long load_J_J(long interval) {
      long pulse = ClickTimingMath.load(this.profile.pop);
      long room = Math.min(interval / 2L, interval - 2000000L);
      return Math.max(1000000L, Math.min(pulse, room));
   }

   private void init_V_J_Z(long now, boolean active) {
      if (this.poll_J == -9223372036854775808L) {
         this.poll_J = now;
      } else {
         double dt = Math.min(3600.0, Math.max(0L, now - this.poll_J) / 1.0E9);
         this.poll_J = Math.max(this.poll_J, now);
         if (!(dt <= 0.0)) {
            boolean working = active && this.set_J != -9223372036854775808L && now - this.set_J < 1000000000L;
            double load = working ? this.write_D() : 0.0;
            double k = 0.03333333333333333 * load + 0.06666666666666667 * (1.0 - load);
            double equilibrium = 0.03333333333333333 * load / k;
            this.block = equilibrium + (this.block - equilibrium) * Math.exp(-k * dt);
         }
      }
   }

   private double write_D() {
      ClickTechnique technique = this.profile.technique;
      if (!technique.init_Z()) {
         return 0.0;
      }

      double motorRate = technique == ClickTechnique.BUTTERFLY ? this.chunk * 0.5 : this.chunk;
      double comfort = technique.close * this.personality.load();
      double high = technique.open * this.personality.load();
      double q = Math.max(0.0, Math.min(1.0, (motorRate - comfort) / (high - comfort)));
      return q * q * (3.0 - 2.0 * q);
   }

   private static double init(double x, double dt, double tau, double sd, Random r) {
      double a = Math.exp(-dt / tau);
      return a * x + sd * Math.sqrt(Math.max(0.0, 1.0 - a * a)) * r.nextGaussian();
   }

   private static double sigmoid(double x) {
      return 1.0 / (1.0 + Math.exp(-x));
   }

   private static double wrapRadians(double radians) {
      double w = radians % 6.283185307179586;
      if (w >= 3.141592653589793) {
         w -= 6.283185307179586;
      }

      if (w < -3.141592653589793) {
         w += 6.283185307179586;
      }

      return w;
   }

   private final class Inner_BufferHelper extends ClickPatternEngine.Inner_CachePool {
      private long init_J;
      private double init_D = 0.15 * this.init.nextGaussian();
      private double load = 0.5 * this.init.nextGaussian();

      Inner_BufferHelper(long seed) {
         super(seed);
      }

      @Override
      void init_V_J(long at) {
         this.init_J = at;
      }

      @Override
      void init_V() {
         long down = this.init_J;
         double spread = ClickPatternEngine.this.timingSpread();
         double motor = 0.06 * spread * this.init.nextGaussian();
         long interval = ClickPatternEngine.this.init(down, 0.0, ClickPatternEngine.this.open(), ClickPatternEngine.this.poll(), motor - this.init_D, this.init);
         this.load = 0.25 * this.load + 0.15 * this.init_D + 0.2 * spread * this.init.nextGaussian();
         long hold = ClickPatternEngine.this.init_J_J(interval, 0.65, this.load);
         this.init_D = motor;
         this.init_J = down + interval;
         ClickPatternEngine.this.load(down, down + hold);
      }

      @Override
      long init_J() {
         return ClickPatternEngine.this.init_J_J(ClickPatternEngine.this.push(), 0.65, this.load);
      }
   }

   private abstract class Inner_CachePool {
      final Random init;

      Inner_CachePool(long seed) {
         this.init = new Random(seed);
      }

      abstract void init_V_J(long var1);

      abstract void init_V();

      abstract long init_J();

      void load_V(long now) {
         this.init_V_J(now);
         this.init_V();
      }

      long init_J_J(long plannedHold) {
         return plannedHold;
      }

      void read(long acceptedAt) {
      }

      final void write(long now) {
         for (int i = 0; i < 64; i++) {
            this.init_V();
            if (ClickPatternEngine.this.tick_J >= now) {
               return;
            }
         }

         this.init_V_J(now);
         this.init_V();
      }
   }

   public record Inner_ConfigStore(double init_D, double load, boolean init_Z) {
      public static final ClickPatternEngine.Inner_ConfigStore DEFAULT = new ClickPatternEngine.Inner_ConfigStore(1.0, 1.0, true);

      public Inner_ConfigStore {
         init_D = Double.isFinite(init_D) ? Math.max(0.25, Math.min(4.0, init_D)) : 1.0;
         load = Double.isFinite(load) ? Math.max(0.25, Math.min(4.0, load)) : 1.0;
      }
   }

   private final class Inner_DataUtil extends ClickPatternEngine.Inner_CachePool {
      private long init_J;
      private double init_D;
      private double load_D;
      private double read_D;
      private double write;
      private double update;
      private long load_J = -9223372036854775808L;
      private long read_J = -9223372036854775808L;
      private double sync = 0.5 * this.init.nextGaussian();
      private double flush = 0.5 * this.init.nextGaussian();
      private boolean init_Z;

      Inner_DataUtil(long seed) {
         super(seed);
      }

      @Override
      void init_V_J(long at) {
         this.init_J = at;
         this.init_D = 0.0;
         this.load_D = 3.141592653589793;
         this.update = this.init_D(at);
         double sd = this.init_D();
         this.read_D = sd * this.init.nextGaussian();
         this.write = sd * this.init.nextGaussian();
         this.load_J = -9223372036854775808L;
         this.read_J = -9223372036854775808L;
         this.init_Z = true;
      }

      @Override
      void init_V() {
         long down = -9223372036854775808L;
         if (this.init_Z) {
            this.init_Z = false;
            down = this.init_J;
            this.init(true);
         }

         for (int step = 0; step < 6000; step++) {
            double omega = 3.141592653589793 * this.update;
            double pull = 1.0 * ClickPatternEngine.this.profile.open * Math.sin(ClickPatternEngine.wrapRadians(this.load_D - this.init_D - 3.141592653589793));
            double detune = ClickPatternEngine.this.personality.init_Z() ? 0.01 : -0.01;
            double rateA = Math.max(0.1 * omega, omega + detune + this.read_D + pull);
            double rateB = Math.max(0.1 * omega, omega - detune + this.write - pull);
            long event = this.init_J + 1000000L;
            int kind = 0;
            if (this.load_J != -9223372036854775808L && this.load_J <= event) {
               event = this.load_J;
               kind = 3;
            }

            if (this.read_J != -9223372036854775808L && this.read_J < event) {
               event = this.read_J;
               kind = 4;
            }

            long pressA = this.init(this.init_D, rateA);
            if (pressA < event) {
               event = pressA;
               kind = 1;
            }

            long pressB = this.init(this.load_D, rateB);
            if (pressB < event) {
               event = pressB;
               kind = 2;
            }

            double dt = (event - this.init_J) / 1.0E9;
            this.init_D += rateA * dt;
            this.load_D += rateB * dt;
            double sd = this.init_D();
            this.read_D = ClickPatternEngine.init(this.read_D, dt, 0.25, sd, this.init);
            this.write = ClickPatternEngine.init(this.write, dt, 0.25, sd, this.init);
            this.init_J = event;
            switch (kind) {
               case 1:
               case 2:
                  boolean wasDown = this.load_J != -9223372036854775808L || this.read_J != -9223372036854775808L;
                  this.init(kind == 1);
                  if (!wasDown && down == -9223372036854775808L) {
                     down = this.init_J;
                  }
                  break;
               case 3:
               case 4:
                  if (kind == 3) {
                     this.load_J = -9223372036854775808L;
                  } else {
                     this.read_J = -9223372036854775808L;
                  }

                  if (this.load_J == -9223372036854775808L && this.read_J == -9223372036854775808L && down != -9223372036854775808L) {
                     ClickPatternEngine.this.load(down, this.init_J);
                     return;
                  }
            }
         }

         this.init_V_J(this.init_J);
         this.init_Z = false;
         this.init(true);
         ClickPatternEngine.this.load(this.init_J, this.load_J);
         this.load_J = -9223372036854775808L;
      }

      private void init(boolean fingerA) {
         double fingerRate = 3.141592653589793 * this.update;
         long period = (long)(6.283185307179586 / fingerRate * 1.0E9);
         double spread = ClickPatternEngine.this.timingSpread();
         if (fingerA) {
            this.init_D = Math.max(0.0, this.init_D - 6.283185307179586);
            this.sync = 0.25 * this.sync + 0.2 * spread * this.init.nextGaussian();
            this.load_J = this.init_J + ClickPatternEngine.this.init_J_J(period, 0.5, this.sync);
         } else {
            this.load_D = Math.max(0.0, this.load_D - 6.283185307179586);
            this.flush = 0.25 * this.flush + 0.2 * spread * this.init.nextGaussian();
            this.read_J = this.init_J + ClickPatternEngine.this.init_J_J(period, 0.5, this.flush);
         }

         this.update = this.init_D(this.init_J);
      }

      private long init(double phase, double rate) {
         double left = Math.max(0.0, 6.283185307179586 - phase);
         return this.init_J + Math.max(1L, (long)Math.ceil(left / rate * 1.0E9));
      }

      private double init_D(long at) {
         long fastNs = ClickPatternEngine.this.open();
         long headroom = Math.min(fastNs + (long)(fastNs * 0.1), fastNs + (ClickPatternEngine.this.poll() - fastNs) / 2L);
         long interval = ClickPatternEngine.this.init(at, 0.0, headroom, ClickPatternEngine.this.poll(), 0.0, this.init);
         return 1.0E9 / interval;
      }

      private double init_D() {
         return 0.01884955592153876 * this.update * ClickPatternEngine.this.timingSpread();
      }

      @Override
      void load_V(long now) {
         this.write(now);
      }

      @Override
      long init_J() {
         return ClickPatternEngine.this.init_J_J(2L * ClickPatternEngine.this.push(), 0.5, this.sync);
      }
   }

   private final class Inner_JsonUtil extends ClickPatternEngine.Inner_CachePool {
      private final boolean init_Z;
      private long init_J;
      private int init_I;
      private int load_I;
      private long load_J;
      private double init_D;
      private double load_D;
      private long read_J = -9223372036854775808L;
      private long write_J = -9223372036854775808L;
      private double read_D;
      private double write_D;
      private int read_I;

      Inner_JsonUtil(long seed, boolean scroll) {
         super(seed);
         this.init_Z = scroll;
         this.init_D = 0.5 * this.init.nextGaussian();
         this.load_D = 0.625 * this.init.nextGaussian();
      }

      @Override
      void init_V_J(long at) {
         this.init_J = at;
         this.load_I = 0;
         this.load_J = 0L;
         this.write_J = -9223372036854775808L;
      }

      @Override
      void init_V() {
         if (this.init_Z && ClickPatternEngine.this.profile.freeSpin) {
            this.load();
         } else {
            if (this.load_I == 0) {
               this.init_I = ClickPatternEngine.this.profile.init_I
                  + this.init.nextInt(ClickPatternEngine.this.profile.load_I - ClickPatternEngine.this.profile.init_I + 1);
            }

            long down = this.init_J;
            boolean last = this.load_I >= this.init_I - 1;
            long interval;
            if (last) {
               interval = this.load_J > 0L ? this.load_J : ClickPatternEngine.this.push();
            } else {
               double s = this.init_I <= 2 ? 0.5 : (double)this.load_I / (this.init_I - 2);
               double envelope = this.init_Z ? -0.8 + 1.6 * s : 0.7 * Math.cos(6.283185307179586 * s);
               interval = ClickPatternEngine.this.init(down, envelope, ClickPatternEngine.this.open(), ClickPatternEngine.this.poll(), 0.0, this.init);
               this.load_J = interval;
            }

            long hold = this.init_Z ? ClickPatternEngine.this.load_J_J(interval) : this.load_J(interval);
            if (last) {
               this.init_J = down + hold + this.init(down + hold, hold);
               this.load_I = 0;
               this.load_J = 0L;
            } else {
               this.init_J = down + interval;
               this.load_I++;
            }

            ClickPatternEngine.this.load(down, down + hold);
         }
      }

      private void load() {
         if (this.write_J == -9223372036854775808L) {
            long first = ClickPatternEngine.this.init(this.init_J, -0.8, ClickPatternEngine.this.open(), ClickPatternEngine.this.poll(), 0.0, this.init);
            double seconds = first / 1.0E9;
            this.write_D = ClickPatternEngine.this.profile.tick;
            this.read_D = this.write_D / (1.0 - Math.exp(-this.write_D * seconds));
            this.write_J = this.init_J;
            this.read_I = 0;
            this.load_J = first;
         }

         long offset = this.init_J_I(this.read_I);
         if (offset == -9223372036854775808L) {
            this.write_J = -9223372036854775808L;
            this.load();
         } else {
            long down = this.write_J + offset;
            long nextOffset = this.read_I + 1 < 24 ? this.init_J_I(this.read_I + 1) : -9223372036854775808L;
            long interval = nextOffset == -9223372036854775808L ? -9223372036854775808L : nextOffset - offset;
            boolean last = interval == -9223372036854775808L || interval > ClickPatternEngine.this.poll();
            long hold = ClickPatternEngine.this.load_J_J(last ? this.load_J : interval);
            if (last) {
               this.init_J = down + hold + this.init(down + hold, hold);
               this.write_J = -9223372036854775808L;
            } else {
               this.read_I++;
               this.load_J = interval;
            }

            ClickPatternEngine.this.load(down, down + hold);
         }
      }

      private long init_J_I(int k) {
         if (k == 0) {
            return 0L;
         }

         double b = this.write_D;
         double x = b * k / this.read_D;
         return x >= 1.0 ? -9223372036854775808L : (long)Math.ceil(-Math.log1p(-x) / b * 1.0E9);
      }

      private long load_J(long interval) {
         this.init_D = 0.25 * this.init_D + 0.2 * ClickPatternEngine.this.timingSpread() * this.init.nextGaussian();
         return ClickPatternEngine.this.init_J_J(interval, 0.35, this.init_D);
      }

      private long init(long at, long hold) {
         if (this.read_J != -9223372036854775808L && at > this.read_J) {
            double dt = Math.min(3600.0, (at - this.read_J) / 1.0E9);
            this.load_D = ClickPatternEngine.init(this.load_D, dt, 3.0, 0.25 * ClickPatternEngine.this.timingSpread(), this.init);
         }

         this.read_J = at;
         long low = ClickTimingMath.load(ClickPatternEngine.this.profile.poll);
         long high = ClickTimingMath.load(ClickPatternEngine.this.profile.push);
         long gap = low + (long)Math.ceil((high - low) * ClickPatternEngine.sigmoid(this.load_D));
         return Math.max(gap, ClickPatternEngine.this.open() - hold);
      }

      @Override
      long init_J() {
         return this.init_Z ? ClickPatternEngine.this.load_J_J(ClickPatternEngine.this.push()) : ClickPatternEngine.this.init_J_J(ClickPatternEngine.this.push(), 0.35, this.init_D);
      }
   }

   public interface Inner_NetHelper {
      boolean init_Z_J(long var1);

      void init_V_J(long var1);

      default void onClick(long now) {
      }

      default long earliestClickNanos(long now) {
         return now;
      }
   }

   private final class Inner_StreamUtil extends ClickPatternEngine.Inner_CachePool {
      private long start;
      private long period;
      private double amplitude;
      private double load_D;
      private boolean init_Z;
      private double read_D;
      private long read_J = -9223372036854775808L;

      Inner_StreamUtil(long seed) {
         super(seed);
         this.read_D = 0.06 * this.init.nextGaussian();
      }

      @Override
      void init_V_J(long at) {
         long period = this.load_J_2(at);
         double amp = this.init_D_J(at);
         double lead = amp > 0.25 ? this.init_D_D(amp) : 0.0;
         this.init(at - (long)(period * lead), period, amp);
      }

      private void init(long start, long period, double amp) {
         this.start = start;
         this.period = period;
         this.amplitude = amp;
         this.init_Z = false;
      }

      @Override
      void init_V() {
         long down = -9223372036854775808L;

         for (int half = 0; half < 512; half++) {
            if (!this.init_Z) {
               if (down == -9223372036854775808L && this.amplitude > 0.25) {
                  down = this.start + (long)(this.period * this.init_D_D(this.amplitude));
               }

               this.init_Z = true;
               this.load_D = this.init_D_J(this.start + this.period / 2L);
            } else {
               long up = -9223372036854775808L;
               if (down != -9223372036854775808L && this.load_D > 0.25) {
                  up = this.start + this.period / 2L + (long)(this.period * this.init_D_D(this.load_D));
               }

               long nextStart = this.start + this.period;
               this.init(nextStart, this.load_J_2(nextStart), this.init_D_J(nextStart));
               if (up != -9223372036854775808L) {
                  ClickPatternEngine.this.load(down, up);
                  return;
               }
            }
         }

         this.read_D = 0.0;
         long at = this.start + (long)(this.period * this.init_D_D(1.0));
         ClickPatternEngine.this.load(at, at + this.period / 2L);
         this.init(this.start + this.period, this.load_J_2(this.start + this.period), 1.0);
      }

      @Override
      void load_V(long now) {
         this.write(now);
      }

      @Override
      long init_J() {
         return ClickPatternEngine.this.push() / 2L;
      }

      private long load_J_2(long at) {
         long fastNs = ClickPatternEngine.this.open();
         long headroom = Math.min(fastNs + (long)(fastNs * 0.04), fastNs + (ClickPatternEngine.this.poll() - fastNs) / 2L);
         return ClickPatternEngine.this.init(at, 0.0, headroom, ClickPatternEngine.this.poll(), 0.0, this.init);
      }

      private double init_D_J(long at) {
         if (this.read_J != -9223372036854775808L && at > this.read_J) {
            this.read_D = ClickPatternEngine.init(this.read_D, (at - this.read_J) / 1.0E9, 0.5, 0.06 * ClickPatternEngine.this.profile.close, this.init);
         }

         if (this.read_J == -9223372036854775808L || at > this.read_J) {
            this.read_J = at;
         }

         return Math.exp(this.read_D);
      }

      private double init_D_D(double amplitude) {
         return Math.asin(Math.min(1.0, 0.25 / amplitude)) / 6.283185307179586;
      }
   }

   private final class Inner_TaskQueue extends ClickPatternEngine.Inner_CachePool {
      private long init_J;

      Inner_TaskQueue(long seed) {
         super(seed);
      }

      @Override
      void init_V_J(long at) {
         this.init_J = at;
      }

      @Override
      void init_V() {
         ClickPatternEngine.this.load(this.init_J, this.init_J + this.load());
      }

      @Override
      long init_J_J(long plannedHold) {
         return this.load();
      }

      @Override
      void read(long acceptedAt) {
         this.init_J = acceptedAt + ClickPatternEngine.this.close();
      }

      @Override
      long init_J() {
         return this.load();
      }

      private long load() {
         return Math.min(15000000L, ClickPatternEngine.this.close() / 2L);
      }
   }
}
