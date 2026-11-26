package autismclient.util;

import java.util.Locale;
import java.util.Random;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class CombatAimEngine {
   static final double init_D = 0.06;
   static final double load_D = 0.6;
   private static final double update_D = 0.25;
   private static final double sync_D = 50.0;
   private static final double flush_D = 16.5;
   private static final double close_D = 0.82;
   private static final double open_D = 0.13;
   private static final double poll_D = 0.5;
   private static final double push_D = 0.42;
   private static final double pop_D = 0.7;
   static final double read_D = 0.7;
   private static final double tick_D = 0.6;
   private static final double run_D = 0.4;
   private static final double get_D = 0.9;
   static final double write_D = 4.0;
   private static final double set_D = 3.2;
   private static final double put = 18.0;
   private static final double add = 0.75;
   private static final double clear = 0.84;
   private static final int init_I = 3;
   private static final double reset = 0.25;
   private static final double start = 0.3;
   private static final double stop = 0.12;
   private static final double next = 3.0;
   private static final double prev = 0.25;
   private static final double valid = 2.0;
   private static final double size = 12.0;
   private static final double data = 0.15;
   private static final double value = 6.0;
   private static final double cache = 1.8;
   private static final double state = 1.0;
   private static final double queue = 0.48;
   private static final double buffer = 2.9;
   private static final double entry = 2.2;
   private static final double node = 0.82;
   private static final int load_I = 41;
   private static final int read_I = 20;
   private static final int write_I = 33;
   private static final int update_I = 8;
   private static final double task = 4.0;
   private static final double pool = 3.5;
   private static final double index = 12.5;
   private final Random init_Random;
   private final HumanHandProfile init_am;
   private final double count;
   private final double flags;
   private boolean init_Z;
   private double level;
   private double frame;
   private boolean load_Z;
   private double block;
   private boolean read_Z;
   private double chunk;
   private double table;
   private double store;
   private double drain;
   private double fill;
   private double mark;
   private double scan;
   private boolean write_Z;
   private boolean update_Z;
   private double parse;
   private int sync_I;
   private CombatAimEngine.Inner_StreamUtil init_q_StreamUtil;
   private double bind = 1.0;
   private double f_a_D;
   private double f_b_D = 1.0;
   private double f_c_D;
   private double f_d_D;
   private double f_e_D;
   private double f_f_D = 1.0;
   private boolean sync_Z;
   private double f_g_D;
   private double f_h_D = 0.0 / 0.0;
   private double f_i_D = 3.0;
   private int flush_I;
   private int close_I;
   private double f_j_D;
   private int open_I = 2147483647;
   private boolean flush_Z;
   private int poll_I;
   private boolean close_Z;
   private double f_k_D;
   private double f_l_D;
   private final double[] init_DArr = new double[41];
   private final double[] load_DArr = new double[41];
   private int push_I;
   private int pop_I;
   private int tick_I;
   private int run_I;
   private int get_I;
   private int set_I = -1;
   private double f_m_D;

   public static double init_D_D_2(double ridingCap, double outsideBox, boolean ready, double roll) {
      double base = ready ? Math.max(ridingCap, Math.min(18.0, outsideBox + ridingCap)) : ridingCap;
      return base * (0.45 + 0.55 * init_D_D(roll, 0.0, 1.0));
   }

   public CombatAimEngine(HumanHandProfile hand, Random random) {
      this.init_am = hand;
      this.init_Random = random;
      this.count = init_D_D(hand.poll() - 0.02, 0.76, 0.92);
      this.flags = 0.8 + 0.35 * sync(hand.push());
      this.fill = this.count;
   }

   public static CombatAimEngine init_q() {
      Random random = new Random();
      return new CombatAimEngine(HumanHandProfile.init_am().init_am_Random(random), random);
   }

   public void init_V() {
      this.init_Z = false;
      this.load_Z = false;
      this.f_g_D = 0.0;
      this.read_Z = false;
      this.store = this.drain = 0.0;
      this.init_q_StreamUtil = null;
      this.sync_I = 0;
      this.close_Z = false;
      this.push_I = 0;
      this.pop_I = 0;
      this.tick_I = 0;
      this.run_I = 0;
      this.get_I = 0;
      this.set_I = -1;
   }

   public double init_D() {
      return 1.22 + 0.2 * sync(this.init_am.update());
   }

   public double[] init(CombatAimEngine.Inner_DataUtil f, double atYaw, double atPitch, double maxYaw, double maxPitch) {
      if (this.bind != f.pop_D) {
         this.init_q_StreamUtil = null;
         this.bind = f.pop_D;
      }

      this.write_Z = f.write_Z;
      boolean boundedRests = f.poll_Z && !f.pop_Z;
      double realizedYawStep = this.load_Z ? Math.abs(flush(atYaw - this.block)) : 0.0;
      double realizedPitchStep = this.load_Z ? atPitch - this.f_c_D : 0.0;
      boolean pitchRepeating = this.load_Z && realizedPitchStep != 0.0 && Math.abs(realizedPitchStep - this.f_d_D) < 0.001;
      if (this.load_Z) {
         this.f_g_D = realizedYawStep > 2.75 && realizedPitchStep == 0.0 ? this.f_g_D + 1.0 : Math.max(0.0, this.f_g_D - (boundedRests ? 0.25 : 0.75));
      }

      this.f_d_D = realizedPitchStep;
      this.block = atYaw;
      this.f_c_D = atPitch;
      this.load_Z = true;
      if (this.init_Z && !f.load_Z) {
         this.level = atYaw + init_D_D(flush(this.level - atYaw), -0.75, 0.75);
         this.frame = atPitch + init_D_D(this.frame - atPitch, -0.75, 0.75);
         if (f.update_Z && this.read_Z) {
            this.level = this.level + f.get;
            if (!load_Z_q_DataUtil(f)) {
               this.frame = this.frame + f.set;
            }

            this.chunk = this.chunk + f.get;
            this.table = this.table + f.set;
         }
      } else {
         this.level = atYaw;
         this.frame = atPitch;
         this.init_Z = true;
         this.read_Z = false;
         this.store = this.drain = 0.0;
         this.init_q_StreamUtil = null;
         this.sync_I = 0;
         this.parse = 0.0;
         this.fill = this.count;
         this.scan = (0.06 + 0.1 * this.init_Random.nextDouble()) * (this.init_Random.nextBoolean() ? 1.0 : -1.0) * Math.max(0.8, f.read_D);
      }

      double carried = this.read_Z ? flush(this.level - this.chunk) : 0.0 / 0.0;
      double expected = this.read_Z ? carried - this.store : 0.0 / 0.0;
      double centerYaw = this.level + flush(f.init_D + this.scan - this.level);
      this.init_V(centerYaw, f.load_D);
      double halfWidth = Math.max(0.8, f.read_D);
      this.f_e_D = f.open_D;
      this.f_f_D = halfWidth;
      double error = this.level - centerYaw;
      if (Double.isNaN(expected)) {
         expected = error;
         carried = error;
      }

      boolean pitchForced = false;
      if (load_Z_q_DataUtil(f)) {
         double slack = f.flush_D > 0.0 && !f.pop_Z && !f.push_Z && !this.read(f) ? f.flush_D * init_D_D((f.sync_D - 3.0) / 4.0, 0.0, 1.0) : 0.0;
         double kept = this.frame - f.load_D;
         double follow = 0.4 + 0.5 * this.init_Random.nextDouble();
         double softUp = 0.6 * f.write_D + slack;
         double softDown = 0.6 * f.update_D + slack;
         if (kept < -softUp) {
            kept += (-softUp - kept) * follow;
         } else if (kept > softDown) {
            kept -= (kept - softDown) * follow;
         }

         kept = init_D_D(kept, -0.7 * f.write_D - slack, 0.7 * f.update_D + slack);
         pitchForced = Math.abs(f.load_D + kept - atPitch) > Math.abs(this.frame - atPitch) + 1.0E-6;
         this.frame = f.load_D + kept;
         this.parse = init_D_D(kept, -0.45 * f.write_D, 0.45 * f.update_D);
      }

      double pitchError = this.frame - f.load_D;
      boolean strike = f.sync_D <= 3.0;
      if (f.load_Z) {
         this.mark = this.init_D_D_2(halfWidth, strike, this.read_D());
      }

      this.fill = this.fill + (0.15 * (this.count - this.fill) + 0.025 * this.update());
      this.fill = init_D_D(this.fill, this.count - 0.08, Math.min(1.0, this.count + 0.08));
      double pursuit = strike ? Math.max(this.fill, 0.86) : this.fill;
      if (this.init_q_StreamUtil != null && this.init_q_StreamUtil.init_Z && !f.read_Z) {
         this.init_q_StreamUtil = null;
      }

      if (f.open_Z && strike && !f.push_Z) {
         if (this.init_q_StreamUtil != null && (this.init_q_StreamUtil.init_Z || this.init_q_StreamUtil.read_Z)) {
            this.init_q_StreamUtil = null;
         }

         this.sync_I = 0;
      }

      if (this.init_q_StreamUtil != null && !this.init_q_StreamUtil.init_Z && strike && !this.init_q_StreamUtil.load_Z) {
         this.init_q_StreamUtil = null;
      }

      if (this.init_q_StreamUtil != null && !this.init_q_StreamUtil.init_Z && !this.init_q_StreamUtil.load_Z && f.read_Z && this.init(f, true)) {
         this.init_q_StreamUtil = null;
      }

      boolean whiffNow = f.push_Z;
      if (this.init_q_StreamUtil != null && !this.init_q_StreamUtil.init_Z && whiffNow && (this.init_q_StreamUtil.load_Z || !this.init_q_StreamUtil.read_Z)) {
         this.init_q_StreamUtil = null;
      }

      if (this.init_q_StreamUtil == null && this.sync_I > 0 && !f.push_Z) {
         this.sync_I--;
      }

      if (this.init_q_StreamUtil == null) {
         double distance = load_Z_q_DataUtil(f) ? Math.abs(error) : Math.hypot(error, pitchError);
         if (distance > Math.max(18.0, 3.2 * halfWidth)) {
            boolean prompt = f.load_Z && f.flush_Z;
            this.mark = this.init_D_D_2(halfWidth, true, this.read_D());
            if (prompt) {
               this.mark = init_D_D(this.mark, -0.45 * halfWidth, 0.45 * halfWidth);
            }

            this.update_Z = true;
            double flickDuration = prompt && f.close_Z ? 1.0 : this.init_D(distance, halfWidth) * (prompt ? 0.7 : 1.0);
            double flickGain = 0.92 + 0.06 * this.init_Random.nextDouble();
            this.init(f, carried, pitchError, this.mark, flickDuration, prompt ? 1.0 : flickGain, false, maxYaw, maxPitch);
            this.init_q_StreamUtil.load_Z = true;
         } else if (!whiffNow || strike && !f.open_Z) {
            if (f.read_Z && (!strike || this.write_Z()) && this.init(f, false)) {
               double side = Math.abs(error) > 0.5 ? Math.signum(error) : (this.init_Random.nextBoolean() ? 1.0 : -1.0);
               double target = side * this.f_a_D;
               this.init(f, carried, pitchError, target, this.write(Math.abs(target - carried)), 1.0, true, maxYaw, maxPitch);
            } else if (strike) {
               this.init(f, error, carried, pitchError, halfWidth, pursuit, maxYaw, maxPitch);
            } else if (this.sync_I <= 0 && (this.update_Z && this.load_Z() || this.init_Z() || Math.abs(error - this.mark) > this.read(halfWidth))) {
               this.mark = this.init_D_D_2(halfWidth, !this.load_Z(), this.read_D());
               this.update_Z = !this.load_Z();
               this.init(
                  f,
                  carried,
                  pitchError,
                  this.mark,
                  this.write(Math.abs(this.mark - carried)),
                  0.85 + 0.3 * this.init_Random.nextDouble(),
                  false,
                  maxYaw,
                  maxPitch
               );
            }
         } else {
            double side = Math.abs(error) > 0.3 ? Math.signum(error) : (this.init_Random.nextBoolean() ? 1.0 : -1.0);
            this.mark = side * this.init_D_q_DataUtil(f);
            this.update_Z = false;
            this.init(f, carried, pitchError, this.mark, this.poll_I == 0 ? 1.0 : 2.0, 1.0, false, maxYaw, maxPitch);
            this.init_q_StreamUtil.read_Z = true;
         }
      }

      double startYaw = this.level;
      if (this.init_q_StreamUtil != null) {
         this.load_V_D(expected);
      } else {
         this.level = this.level + pursuit * this.store;
      }

      if (!load_Z_q_DataUtil(f)) {
         this.frame = this.frame + pursuit * this.drain;
      }

      boolean sweep = f.poll_D > 0.5;
      if (this.init_q_StreamUtil == null || this.init_q_StreamUtil.read_D == 0.0) {
         if (sweep) {
            double top = Math.max(-0.82 * f.write_D, -8.0);
            double bottom = Math.min(0.82 * f.update_D, 10.0);
            if (Double.isNaN(this.f_h_D)) {
               this.f_h_D = this.init_Random.nextBoolean() ? top : bottom;
            }

            if (Math.abs(this.f_h_D - this.parse) <= this.f_i_D) {
               this.parse = this.f_h_D;
               if (this.flush_I-- <= 0) {
                  boolean upNext = this.f_h_D > 0.5 * (top + bottom);
                  this.f_h_D = upNext ? top + 1.2 * this.init_Random.nextDouble() : bottom - 1.2 * this.init_Random.nextDouble();
                  this.f_i_D = 3.0 + 1.0 * this.init_Random.nextDouble();
                  this.flush_I = 2 + this.init_Random.nextInt(3);
               }
            }

            this.f_h_D = init_D_D(this.f_h_D, top, bottom);
            double push = this.f_i_D * this.load_D();
            this.parse = this.parse + init_D_D(this.f_h_D - this.parse, -push, push);
         } else {
            this.f_h_D = 0.0 / 0.0;
            this.parse = this.parse + (-0.08 * this.parse + 0.9 * this.update());
            this.parse = init_D_D(this.parse, -0.45 * f.write_D, 0.45 * f.update_D);
         }

         double want = f.load_D + this.parse - this.frame;
         double rate = sweep ? 0.9 : (this.init(f, pitchError) ? 0.6 : 0.35);
         double limit = sweep ? 4.2 : (this.init(f, pitchError) ? (this.read(f) ? 2.0 : 4.0) : 0.42);
         double step = init_D_D(want * rate, -limit, limit) * this.load_D();
         if (!sweep && !this.init(f, pitchError)) {
            step = this.init_D_D(step);
         }

         this.frame += step;
      }

      double speed = Math.abs(this.level - startYaw);
      this.level = this.level + ((0.08 + 0.12 * speed) * this.update() + this.init_am.tick() * this.update());
      this.frame = this.frame + this.init_am.tick() * 0.6 * this.update();
      double wantYaw = this.level - atYaw;
      double wantPitch = this.frame - atPitch;
      double scale = 1.0;
      if (maxYaw > 0.0 && Math.abs(wantYaw) > maxYaw) {
         scale = Math.min(scale, maxYaw / Math.abs(wantYaw));
      }

      if (maxPitch > 0.0 && Math.abs(wantPitch) > maxPitch) {
         scale = Math.min(scale, maxPitch / Math.abs(wantPitch));
      }

      wantYaw *= scale;
      wantPitch *= scale;
      double minPitch = Math.max(0.13, f.poll_D * 1.02);
      double fastYaw = Math.min(2.6, 2.75 - 1.6 * f.poll_D);
      boolean slowYaw = Math.abs(wantYaw) <= fastYaw && realizedYawStep <= 20.4;
      boolean fastRest = !slowYaw && Math.abs(wantYaw) < 12.0 && realizedYawStep <= 20.4 && this.f_g_D <= (boundedRests ? 0.0 : 2.0);
      boolean restable = slowYaw || fastRest || !boundedRests && this.read(f);
      if (this.sync_Z) {
         if (!restable || this.init(f, pitchError) || pitchForced || this.init_Random.nextDouble() < 0.3) {
            this.sync_Z = false;
         }
      } else if (restable
         && this.init_q_StreamUtil == null
         && !this.init(f, pitchError)
         && !pitchForced
         && this.init_Random.nextDouble() < (slowYaw ? 0.25 : (this.read(f) ? 0.12 : 0.25))) {
         this.sync_Z = true;
      }

      if (this.sync_Z) {
         wantPitch = 0.0;
         this.frame = atPitch;
      } else if (Math.abs(wantYaw) > 2.9 && !this.init(f, pitchError) && !pitchForced && !(f.poll_D > 0.5) && Math.abs(wantPitch) > 0.48) {
         wantPitch = this.init_D_D(wantPitch);
         this.frame = atPitch + wantPitch;
      }

      if (load_Z_q_DataUtil(f) && f.close_D > 0.0) {
         double needed = Math.max(0.0, Math.abs(f.load_D - atPitch) - (f.load_D > atPitch ? f.write_D : f.update_D));
         double cap = init_D_D_2(f.close_D, needed, f.open_Z && strike && !f.push_Z, this.init_Random.nextDouble());
         double last = Math.abs(realizedPitchStep);
         if (last > 0.0 && Math.abs(cap - last) <= 0.1 * Math.max(cap, last)) {
            cap = last * (1.12 + 0.04 * this.init_Random.nextDouble());
         }

         if (Math.abs(wantPitch) > cap) {
            wantPitch = Math.copySign(cap, wantPitch);
            this.frame = atPitch + wantPitch;
         }
      }

      boolean steadyMove = this.init_q_StreamUtil == null || !this.init_q_StreamUtil.load_Z && !this.init_q_StreamUtil.read_Z && !this.init_q_StreamUtil.init_Z;
      if (steadyMove
         && !f.push_Z
         && f.sync_D > 3.0
         && Math.abs(f.get) > 1.0
         && Math.abs(wantYaw) < 6.0
         && Math.abs(wantPitch) >= f.poll_D
         && Math.abs(wantPitch) < 1.8
         && this.init_Random.nextDouble() < 0.15) {
         wantYaw = 0.0;
      }

      if (!this.sync_Z && (Math.abs(wantYaw) > fastYaw || realizedYawStep > 20.4) && Math.abs(wantPitch) < minPitch && maxPitch >= minPitch) {
         double toward = f.load_D + this.parse - atPitch;
         if (Math.abs(toward) > minPitch) {
            this.f_b_D = Math.signum(toward);
         } else {
            this.f_b_D = -this.f_b_D;
         }

         double step = minPitch * (this.init_Random.nextDouble() < 0.6 ? 1.0 : 1.0 + this.init_Random.nextDouble());
         wantPitch = this.f_b_D * Math.min(maxPitch, step);
         this.frame = atPitch + wantPitch;
      }

      if (pitchRepeating && wantPitch != 0.0 && f.poll_D > 0.0 && f.sync_D > 3.0 && Math.round(wantPitch / f.poll_D) == Math.round(this.f_d_D / f.poll_D)) {
         double count = Math.abs(wantPitch) > 1.5 * f.poll_D ? -f.poll_D : f.poll_D;
         wantPitch += Math.copySign(count, wantPitch);
         this.frame = atPitch + wantPitch;
      }

      return new double[]{wantYaw, wantPitch, this.sync_Z ? 1.0 : 0.0};
   }

   public static boolean init_Z_DArr(double[] want) {
      return want.length > 2 && want[2] != 0.0;
   }

   private double init_D_D(double step) {
      double cap = 0.48 * (0.62 + 0.38 * this.init_Random.nextDouble());
      return Math.abs(step) <= cap ? step : Math.copySign(cap, step);
   }

   private double load_D() {
      return 0.55 + 0.9 * this.init_Random.nextDouble();
   }

   public void load_V() {
      this.close_I++;
   }

   public void init_V_D(double fraction) {
      fraction = Math.max(0.0, fraction);
      if (fraction != this.f_j_D) {
         this.f_j_D = fraction;
         this.open_I = this.f_j_D > 0.0 ? Math.max(1, (int)Math.round((0.8 + 0.4 * this.init_Random.nextDouble()) / this.f_j_D)) : 2147483647;
      }
   }

   public boolean init_Z_q_DataUtil(CombatAimEngine.Inner_DataUtil f) {
      if (this.f_j_D <= 0.0 || this.close_I < this.open_I || f.init_I < 1) {
         return false;
      } else if (f.pop_Z) {
         return f.sync_D >= 3.0;
      } else {
         return f.init_I <= 2 ? f.sync_D >= 8.0 : this.flush_Z && f.init_I <= 8 && f.sync_D >= 5.0;
      }
   }

   public void read_V() {
      this.close_I = 0;
      this.open_I = this.f_j_D > 0.0 ? Math.max(1, (int)Math.round((0.8 + 0.4 * this.init_Random.nextDouble()) / this.f_j_D)) : 2147483647;
      this.flush_Z = this.init_Random.nextDouble() < 0.3;
      double roll = this.init_Random.nextDouble();
      this.poll_I = roll < 0.7 ? 0 : (roll < 0.9 ? 1 : 2);
   }

   private double init_D_q_DataUtil(CombatAimEngine.Inner_DataUtil f) {
      double lateral;
      if (this.poll_I == 0) {
         lateral = f.tick + 0.1 + 0.05 * (this.init_Random.nextDouble() - 0.5);
      } else if (this.poll_I == 1) {
         lateral = f.tick + 0.16 + 0.06 * this.init_Random.nextDouble();
      } else {
         lateral = f.tick + 0.2 + 0.04 * this.init_Random.nextDouble();
      }

      return Math.toDegrees(Math.atan(lateral / Math.max(0.3, f.run)));
   }

   public void init_V_D(double yaw, double pitch, double perfectYaw, boolean sampled) {
      if (!this.close_Z) {
         this.close_Z = true;
         this.f_k_D = yaw;
         this.f_l_D = pitch;
      } else {
         double yawSpeed = Math.abs(flush(yaw - this.f_k_D));
         boolean changed = yawSpeed > 1.0E-6 || Math.abs(pitch - this.f_l_D) > 1.0E-6;
         this.f_k_D = yaw;
         this.f_l_D = pitch;
         if (changed && sampled && !Double.isNaN(perfectYaw)) {
            double error = Math.abs(flush(yaw - perfectYaw));
            int direction = perfectYaw > yaw ? 1 : 0;
            if (direction != this.set_I) {
               this.get_I = 0;
            } else if (yawSpeed > 3.0) {
               double deviation = Math.abs(flush(this.f_m_D - error));
               double increase = init_D_D((1.0 - deviation) * 4.0, -0.2, 4.0);
               this.get_I = (int)init_D_D(this.get_I + increase, 0.0, 100.0);
            }

            this.set_I = direction;
            this.f_m_D = error;
            if (yawSpeed > 1.0) {
               this.run_I = error > 4.0 ? 0 : this.run_I + 1;
            }

            double needed = this.write_D();
            this.init_DArr[this.pop_I] = yawSpeed;
            this.load_DArr[this.pop_I] = error;
            this.pop_I = (this.pop_I + 1) % 41;
            this.push_I = Math.min(this.push_I + 1, 41);
            this.tick_I = error >= needed ? 0 : this.tick_I + 1;
         }
      }
   }

   private void init(CombatAimEngine.Inner_DataUtil f, double error, double carried, double pitchError, double halfWidth, double pursuit, double maxYaw, double maxPitch) {
      double ticks = Math.ceil(f.sync_D - 1.0E-6) + 1.0;
      double predicted = error + (pursuit - 1.0) * this.store * ticks + this.store;
      double low = load_D_D(halfWidth);
      double high = 0.86 * halfWidth;
      double magnitude = Math.abs(predicted);
      boolean onBand = magnitude <= high && (magnitude >= low || Math.abs(this.store) < 1.2) && (!this.read_Z() || predicted * this.f_e_D > 0.0);
      if (onBand && !this.init(f, pitchError) && !this.init_Z()) {
         this.mark = predicted;
         this.update_Z = true;
      } else {
         this.mark = this.init_D_D_2(halfWidth, true, this.read_D());
         this.update_Z = true;
         this.init(
            f,
            carried,
            pitchError,
            this.mark,
            Math.min(ticks, this.write(Math.abs(predicted - this.mark))),
            0.96 + 0.08 * this.init_Random.nextDouble(),
            false,
            maxYaw,
            maxPitch
         );
         this.init_q_StreamUtil.load_Z = true;
      }
   }

   private static boolean load_Z_q_DataUtil(CombatAimEngine.Inner_DataUtil f) {
      return f.sync_Z && f.poll_D <= 0.5;
   }

   private boolean init(CombatAimEngine.Inner_DataUtil f, double pitchError) {
      return load_Z_q_DataUtil(f) ? false : pitchError < -0.8 * f.write_D || pitchError > 0.8 * f.update_D;
   }

   private boolean init_Z() {
      return this.get_I >= 8;
   }

   private boolean read(CombatAimEngine.Inner_DataUtil f) {
      return !this.load_Z() && Math.abs(f.get) > 3.0;
   }

   private boolean load_Z() {
      return Math.abs(this.store) >= 1.2;
   }

   private double read_D() {
      if (this.read_Z()) {
         return Math.signum(this.f_e_D);
      }

      if (!this.load_Z()) {
         return this.init_Random.nextBoolean() ? 1.0 : -1.0;
      }

      double trailing = -Math.signum(this.store);
      return this.init_Random.nextDouble() < 0.82 ? trailing : -trailing;
   }

   private boolean read_Z() {
      return Math.abs(this.f_e_D) >= this.f_f_D;
   }

   private static double load_D_D(double halfWidth) {
      return Math.min(2.3000000000000003, 0.5 * halfWidth);
   }

   private double init_D_D_2(double halfWidth, boolean forStrike, double side) {
      double low;
      double high;
      if (forStrike) {
         double bottom = load_D_D(halfWidth);
         double top = Math.max(bottom + 0.4, 0.84 * halfWidth);
         double middle = (bottom + top) * 0.5;
         boolean wasOuter = this.update_Z && Math.abs(this.mark) >= middle;
         low = wasOuter ? bottom : middle;
         high = wasOuter ? middle : top;
      } else {
         double inner = Math.max(2.3000000000000003, 0.36 * halfWidth);
         double outer = Math.max(inner + 2.8, 0.96 * halfWidth);
         boolean wasOuter = Math.abs(this.mark) >= outer - 1.0E-6;
         if (wasOuter) {
            low = inner;
            high = Math.max(inner + 0.3, 0.48 * halfWidth);
         } else {
            low = outer;
            high = Math.max(outer + 0.8, (this.init_Random.nextDouble() < 0.3 ? 1.3 : 1.12) * halfWidth);
         }
      }

      double picked = side * (low + (high - low) * this.init_Random.nextDouble());
      if (this.write_Z) {
         picked = init_D_D(picked, -0.84 * halfWidth, 0.84 * halfWidth);
      }

      return picked;
   }

   private double read(double halfWidth) {
      return Math.max(1.2, 0.32 * halfWidth) * this.flags * (0.8 + 0.4 * this.init_Random.nextDouble());
   }

   private double init_D(double distance, double halfWidth) {
      double width = Math.max(2.0, 2.0 * halfWidth);
      double ms = (this.init_am.init_D() + this.init_am.load_D() * (Math.log(1.0 + distance / width) / Math.log(2.0))) * 0.42;
      return ms / 50.0 * Math.exp(this.init_am.open() * this.update());
   }

   private double write(double distance) {
      return distance < 2.5 ? 1.0 : (distance < 8.0 ? 2.0 : 3.0);
   }

   private static double update(double s) {
      double u = Math.pow(Math.max(0.0, Math.min(1.0, s)), 0.82);
      return u * u * u * (10.0 + u * (-15.0 + 6.0 * u));
   }

   private static double init_D_D(double travel, double minTicks, double accelUp, double cap) {
      double distance = Math.abs(travel);

      int ticks;
      for (ticks = (int)Math.max(1.0, Math.round(minTicks)); ticks < 60; ticks++) {
         double previous = 0.0;
         boolean fits = true;

         for (int k = 1; k <= ticks && fits; k++) {
            double step = distance * (update((double)k / ticks) - update((double)(k - 1) / ticks));
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

   private void init(
      CombatAimEngine.Inner_DataUtil f, double start, double pitchError, double targetError, double ticks, double gainRoll, boolean excursion, double maxYaw, double maxPitch
   ) {
      double duration = load(ticks, f.pop_D);
      double pitchTarget = init_D_D(this.parse, -0.45 * f.write_D, 0.45 * f.update_D);
      double dPitch = !excursion && this.init(f, pitchError) ? pitchTarget - pitchError : 0.0;
      double travel = (targetError - start) * gainRoll;
      if (f.pop_D < 1.0) {
         double normal = init_D_D(travel, Math.max(1L, Math.round(ticks)), f.push_D, maxYaw > 0.0 ? maxYaw / f.pop_D - Math.abs(this.store) : 0.0);
         if (dPitch != 0.0) {
            normal = Math.max(normal, init_D_D(dPitch, normal, f.push_D, maxPitch / f.pop_D));
         }

         duration = load(normal, f.pop_D);
      }

      duration = init_D_D(travel, duration, f.push_D, maxYaw > 0.0 ? maxYaw - Math.abs(this.store) : 0.0);
      if (dPitch != 0.0) {
         duration = Math.max(duration, init_D_D(dPitch, duration, f.push_D, maxPitch));
      }

      CombatAimEngine.Inner_StreamUtil next = new CombatAimEngine.Inner_StreamUtil();
      next.init_D = start;
      next.load_D = travel;
      next.read_D = dPitch;
      next.write = duration;
      next.init_Z = excursion;
      next.flush = (this.init_am.sync() + this.init_am.flush() * init_D_D(this.update(), -2.0, 2.0)) * Math.abs(travel);
      this.init_q_StreamUtil = next;
   }

   private void load_V_D(double expected) {
      CombatAimEngine.Inner_StreamUtil m = this.init_q_StreamUtil;
      m.update++;
      double s = Math.min(1.0, m.update / m.write);
      double progress = update(s);
      double side = Math.sin(3.141592653589793 * s) * m.flush;
      this.level = this.level + (m.init_D + m.load_D * progress - expected);
      double p = m.read_D * progress + side * 0.25;
      this.frame = this.frame + (p - m.sync);
      m.sync = p;
      if (s >= 1.0) {
         this.init_q_StreamUtil = null;
         this.sync_I = m.init_Z ? 0 : this.init_Random.nextInt(2);
         if (m.init_Z) {
            this.update_Z = true;
         }
      }
   }

   private static double load(double ticks, double scale) {
      return Math.max(1L, Math.round(ticks / scale));
   }

   private void init_V(double centerYaw, double centerPitch) {
      if (this.read_Z) {
         double dYaw = flush(centerYaw - this.chunk);
         double dPitch = centerPitch - this.table;
         dYaw = init_D_D(dYaw, -72.0, 72.0);
         this.store = this.store + 0.55 * (dYaw - this.store);
         this.drain = this.drain + 0.4 * (dPitch - this.drain);
      }

      this.read_Z = true;
      this.chunk = centerYaw;
      this.table = centerPitch;
   }

   private boolean init(CombatAimEngine.Inner_DataUtil f, boolean preempt) {
      if (f.open_Z && f.sync_D <= 3.0) {
         return false;
      }

      boolean longTerm = this.run_I > 26;
      if (this.tick_I < 20 && !longTerm) {
         return false;
      }

      boolean urgent = this.write_Z();
      if (!f.open_Z && urgent || !f.init_Z && f.init_I >= 2) {
         double needed = this.tick_I >= 20 ? this.write_D() : 5.5 + 2.0 * this.init_Random.nextDouble();
         double travel = init_D_D(needed, this.write(needed), 18.0, 0.0) * 2.0 + 1.0;
         if (preempt && !urgent && f.init_I > 3) {
            return false;
         }

         if ((f.open_Z || !urgent) && f.sync_D < travel) {
            return false;
         }

         this.f_a_D = Math.min(28.0, needed + 2.0 + 2.5 * this.init_Random.nextDouble());
         return true;
      } else {
         return false;
      }
   }

   private boolean write_Z() {
      return this.tick_I >= 33 || this.run_I > 44;
   }

   private double write_D() {
      if (this.push_I == 0) {
         return 13.0;
      }

      double speed = 0.0;
      double error = 0.0;

      for (int i = 0; i < this.push_I; i++) {
         speed += this.init_DArr[i];
         error += this.load_DArr[i];
      }

      speed /= this.push_I;
      error /= this.push_I;
      double needed = 2.3 * speed;
      if (speed >= 2.5 || error < speed + 1.0 && speed >= 2.0) {
         needed = Math.max(needed, 13.2);
      }

      return Math.min(Math.max(needed, 4.5), 26.0);
   }

   String init_Str() {
      return String.format(
         Locale.ROOT,
         "omega=%.2f gain=%.2f offset=%.2f move=%s since=%d need=%.1f lt=%d",
         this.store,
         this.fill,
         this.mark,
         this.init_q_StreamUtil == null
            ? "-"
            : (this.init_q_StreamUtil.init_Z ? "EXC" : (this.init_q_StreamUtil.load_Z ? "STR" : "COR"))
               + (int)this.init_q_StreamUtil.update
               + "/"
               + (int)this.init_q_StreamUtil.write,
         this.tick_I,
         this.write_D(),
         this.run_I
      );
   }

   private double update() {
      return init_D_D(this.init_Random.nextGaussian(), -3.0, 3.0);
   }

   private static double sync(double trait) {
      double v = Math.abs(trait * 1000.0);
      return v - Math.floor(v);
   }

   private static double init_D_D(double v, double lo, double hi) {
      return Math.max(lo, Math.min(hi, v));
   }

   private static double flush(double degrees) {
      double wrapped = degrees % 360.0;
      if (wrapped >= 180.0) {
         wrapped -= 360.0;
      }

      if (wrapped < -180.0) {
         wrapped += 360.0;
      }

      return wrapped;
   }

   public static final class Inner_CachePool {
      private CombatAimEngine init_q;
      private double init_D;
      private double load;
      private double read;
      private double write;
      private double update;
      private double sync;
      private double flush;
      private double close;

      public Inner_CachePool() {
      }

      Inner_CachePool(CombatAimEngine hand) {
         this.init_q = hand;
      }

      public void init() {
         this.update = 0.0;
      }

      public double[] init(RotationMath.Rotation at, RotationMath.Rotation goal, float maxYaw, float maxPitch, double scale) {
         double yaw = at.yaw() + CombatAimEngine.flush(goal.yaw() - at.yaw());
         double pitch = goal.pitch();
         if (this.update == 0.0 || scale != this.close || Math.abs(CombatAimEngine.flush(yaw - this.read)) > 12.0 || Math.abs(pitch - this.write) > 12.0) {
            if (this.init_q == null) {
               this.init_q = CombatAimEngine.init_q();
            }

            this.init_D = at.yaw();
            this.load = at.pitch();
            this.read = yaw;
            this.write = pitch;
            this.close = scale;
            this.sync = 0.0;
            double dy = this.read - this.init_D;
            double dp = this.write - this.load;
            double distance = Math.hypot(dy, dp);
            double ticks = distance < 18.0 ? this.init_q.write(distance) : this.init_q.init_D(distance, 2.0);
            double preferred = CombatAimEngine.load(ticks, scale);
            this.update = Math.max(CombatAimEngine.init_D_D(dy, preferred, 18.0, maxYaw), CombatAimEngine.init_D_D(dp, preferred, 18.0, maxPitch));
            this.flush = (this.init_q.init_am.sync() + this.init_q.init_am.flush() * CombatAimEngine.init_D_D(this.init_q.update(), -2.0, 2.0)) * dy;
         }

         this.sync = Math.min(this.update, this.sync + 1.0);
         double t = this.sync / this.update;
         double progress = CombatAimEngine.update(t);
         double nextYaw = this.init_D + CombatAimEngine.flush(yaw - this.init_D) * progress;
         double nextPitch = this.load + (pitch - this.load) * progress + Math.sin(3.141592653589793 * t) * this.flush * 0.25;
         return new double[]{CombatAimEngine.flush(nextYaw - at.yaw()), nextPitch - at.pitch()};
      }
   }

   public static final class Inner_DataUtil {
      public double init_D;
      public double load_D;
      public double read_D;
      public double write_D;
      public double update_D;
      public double sync_D;
      public int init_I;
      public boolean init_Z;
      public boolean load_Z;
      public boolean read_Z = true;
      public boolean write_Z;
      public boolean update_Z;
      public boolean sync_Z;
      public double flush_D;
      public double close_D;
      public boolean flush_Z;
      public boolean close_Z;
      public boolean open_Z;
      public boolean poll_Z;
      public double open_D;
      private Vec3 init_Vec3;
      private double get;
      private double set;
      public double poll_D = 0.15;
      public double push_D = 18.0;
      public double pop_D = 1.0;
      public boolean push_Z;
      public boolean pop_Z;
      public double tick = 0.3;
      public double run = 3.0;
      public float init_F = 0.0F / 0.0F;
      public float load_F = 0.0F / 0.0F;

      public CombatAimEngine.Inner_DataUtil init_q_DataUtil_Vec3(Vec3 eyes, AABB box, double aimY) {
         return this.init_q_DataUtil_Vec3_2(eyes, box, init(box, aimY));
      }

      public CombatAimEngine.Inner_DataUtil init_q_DataUtil_Vec3_2(Vec3 eyes, AABB box, Vec3 anchor) {
         return this.init_q_DataUtil_Vec3(eyes, box, box, anchor);
      }

      public CombatAimEngine.Inner_DataUtil init_q_DataUtil_Vec3(Vec3 eyes, AABB box, AABB region, Vec3 anchor) {
         return this.init_q_DataUtil_Vec3(eyes, box, region, anchor, null);
      }

      public CombatAimEngine.Inner_DataUtil init_q_DataUtil_Vec3(Vec3 eyes, AABB box, AABB region, Vec3 anchor, Vec3 receivedTarget) {
         return this.init(eyes, box, region, anchor, receivedTarget, null);
      }

      public CombatAimEngine.Inner_DataUtil init(Vec3 eyes, AABB box, AABB region, Vec3 anchor, Vec3 receivedTarget, AABB receivedBox) {
         double width = Math.min(region.getXsize(), region.getZsize());
         double height = region.getYsize();
         double dx = anchor.x - eyes.x;
         double dz = anchor.z - eyes.z;
         double horizontal = Math.max(1.0E-6, Math.hypot(dx, dz));
         this.init_D = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
         this.read_D = Math.toDegrees(Math.atan(Math.max(0.02, width * 0.5 - 0.04) / horizontal));
         double observerAnchorX = anchor.x;
         double observerAnchorZ = anchor.z;
         if (receivedTarget != null) {
            double bearing = Math.toDegrees(Math.atan2(receivedTarget.z - eyes.z, receivedTarget.x - eyes.x)) - 90.0;
            double shift = CombatAimEngine.flush(bearing - this.init_D);
            double corridor = Math.toDegrees(0.2);
            this.read_D = Math.min(this.read_D, corridor);
            double low = Math.max(-this.read_D, shift - corridor);
            double high = Math.min(this.read_D, shift + corridor);
            if (low <= high) {
               this.init_D += (low + high) * 0.5;
               this.read_D = (high - low) * 0.5;
            } else {
               this.init_D = this.init_D + CombatAimEngine.init_D_D(shift, -this.read_D, this.read_D);
               this.read_D = 0.0;
            }

            observerAnchorX = eyes.x - Math.sin(Math.toRadians(this.init_D)) * horizontal;
            observerAnchorZ = eyes.z + Math.cos(Math.toRadians(this.init_D)) * horizontal;
         }

         if (receivedBox != null
            && (!(eyes.x >= receivedBox.minX) || !(eyes.x <= receivedBox.maxX) || !(eyes.z >= receivedBox.minZ) || !(eyes.z <= receivedBox.maxZ))) {
            double bearing = Math.toDegrees(Math.atan2(receivedBox.getCenter().z - eyes.z, receivedBox.getCenter().x - eyes.x)) - 90.0;
            double shift = CombatAimEngine.flush(bearing - this.init_D);
            double left = 1.0 / 0.0;
            double right = -1.0 / 0.0;

            for (double x : new double[]{receivedBox.minX + 0.003, receivedBox.maxX - 0.003}) {
               for (double z : new double[]{receivedBox.minZ + 0.003, receivedBox.maxZ - 0.003}) {
                  double angle = Math.toDegrees(Math.atan2(z - eyes.z, x - eyes.x)) - 90.0;
                  double offset = shift + CombatAimEngine.flush(angle - bearing);
                  left = Math.min(left, offset);
                  right = Math.max(right, offset);
               }
            }

            double low = Math.max(-this.read_D, left);
            double high = Math.min(this.read_D, right);
            if (low <= high) {
               this.init_D += (low + high) * 0.5;
               this.read_D = (high - low) * 0.5;
            } else {
               this.init_D = this.init_D + CombatAimEngine.init_D_D(shift, -this.read_D, this.read_D);
               this.read_D = 0.0;
            }

            observerAnchorX = eyes.x - Math.sin(Math.toRadians(this.init_D)) * horizontal;
            observerAnchorZ = eyes.z + Math.cos(Math.toRadians(this.init_D)) * horizontal;
         }

         this.load_D = -Math.toDegrees(Math.atan2(anchor.y - eyes.y, horizontal));
         this.init_F = (float)Math.toDegrees(Math.atan2(box.getCenter().z - eyes.z, box.getCenter().x - eyes.x)) - 90.0F;
         this.load_F = (float)Math.toDegrees(Math.atan2(CombatAimEngine.init_D_D(eyes.z, box.minZ, box.maxZ) - eyes.z, CombatAimEngine.init_D_D(eyes.x, box.minX, box.maxX) - eyes.x))
            - 90.0F;
         this.get = this.set = 0.0;
         if (this.init_Vec3 != null) {
            double oldDx = observerAnchorX - this.init_Vec3.x;
            double oldDz = observerAnchorZ - this.init_Vec3.z;
            double oldHorizontal = Math.max(1.0E-6, Math.hypot(oldDx, oldDz));
            double share = CombatAimEngine.init_D_D((eyes.distanceTo(this.init_Vec3) - 0.12) / 0.12, 0.0, 1.0);
            this.get = share * CombatAimEngine.flush(this.init_D - (Math.toDegrees(Math.atan2(oldDz, oldDx)) - 90.0));
            this.set = share * (this.load_D + Math.toDegrees(Math.atan2(anchor.y - this.init_Vec3.y, oldHorizontal)));
         }

         this.init_Vec3 = eyes;
         this.tick = width * 0.5;
         this.run = Math.max(0.3, horizontal);
         double top = -Math.toDegrees(Math.atan2(region.maxY - 0.08 * height - eyes.y, horizontal));
         double bottom = -Math.toDegrees(Math.atan2(region.minY + 0.1 * height - eyes.y, horizontal));
         this.write_D = Math.max(0.2, this.load_D - top);
         this.update_D = Math.max(0.2, bottom - this.load_D);
         return this;
      }

      public static Vec3 init(AABB box, double aimY) {
         double height = box.getYsize();
         double y = CombatAimEngine.init_D_D(aimY, box.minY + 0.22 * height, box.maxY - 0.12 * height);
         return new Vec3((box.minX + box.maxX) * 0.5, y, (box.minZ + box.maxZ) * 0.5);
      }

      public static Vec3 init_Vec3_Vec3(Vec3 eyes, AABB box, Vec3 anchor, double reach) {
         if (box.contains(eyes)) {
            return anchor;
         }

         double limit = reach - 0.06;
         if (init_D_Vec3(eyes, box, anchor) <= limit) {
            return anchor;
         }

         Vec3 near = init(eyes, box);
         double nearest = init_D_Vec3(eyes, box, near);
         if (nearest > limit) {
            return nearest <= reach + 0.6 ? near : anchor;
         }

         double low = 0.0;
         double high = 1.0;

         for (int i = 0; i < 12; i++) {
            double mid = (low + high) * 0.5;
            if (init_D_Vec3(eyes, box, init_Vec3_Vec3(anchor, near, mid)) <= limit) {
               high = mid;
            } else {
               low = mid;
            }
         }

         return init_Vec3_Vec3(anchor, near, high);
      }

      public static AABB init_AABB_Vec3(Vec3 eyes, AABB box, Vec3 anchor, double reach) {
         double ex = Math.max(0.0, Math.min(anchor.x - box.minX, box.maxX - anchor.x));
         double ey = Math.max(0.0, Math.min(anchor.y - box.minY, box.maxY - anchor.y));
         double ez = Math.max(0.0, Math.min(anchor.z - box.minZ, box.maxZ - anchor.z));
         if (!box.contains(eyes) && !(ex <= 1.0E-4) && !(ey <= 1.0E-4) && !(ez <= 1.0E-4)) {
            double limit = reach - 0.03;
            if (init(eyes, box, anchor, ex, ey, ez, 1.0) <= limit) {
               return init_AABB_Vec3(anchor, ex, ey, ez, 1.0);
            }

            if (init(eyes, box, anchor, ex, ey, ez, 0.25) > limit) {
               return init_AABB_Vec3(anchor, ex, ey, ez, 0.25);
            }

            double low = 0.25;
            double high = 1.0;

            for (int i = 0; i < 10; i++) {
               double mid = (low + high) * 0.5;
               if (init(eyes, box, anchor, ex, ey, ez, mid) <= limit) {
                  low = mid;
               } else {
                  high = mid;
               }
            }

            return init_AABB_Vec3(anchor, ex, ey, ez, low);
         } else {
            return box;
         }
      }

      public static double init_D_Vec3(Vec3 eyes, AABB box, Vec3 point) {
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

      static Vec3 init(Vec3 eyes, AABB box) {
         double ix = Math.min(0.003, box.getXsize() * 0.01);
         double iz = Math.min(0.003, box.getZsize() * 0.01);
         double iy = Math.min(0.003, box.getYsize() * 0.01);
         return new Vec3(
            CombatAimEngine.init_D_D(eyes.x, box.minX + ix, box.maxX - ix),
            CombatAimEngine.init_D_D(eyes.y, box.minY + iy, box.maxY - iy),
            CombatAimEngine.init_D_D(eyes.z, box.minZ + iz, box.maxZ - iz)
         );
      }

      private static double init(Vec3 eyes, AABB box, Vec3 anchor, double ex, double ey, double ez, double scale) {
         double dx = anchor.x - eyes.x;
         double dz = anchor.z - eyes.z;
         double horizontal = Math.max(1.0E-6, Math.hypot(dx, dz));
         double side = Math.min(ex, ez) * scale;
         Vec3 across = new Vec3(-dz / horizontal * side, 0.0, dx / horizontal * side);
         double worst = init_D_Vec3(eyes, box, anchor);
         worst = Math.max(worst, init_D_Vec3(eyes, box, anchor.add(across)));
         worst = Math.max(worst, init_D_Vec3(eyes, box, anchor.subtract(across)));
         worst = Math.max(worst, init_D_Vec3(eyes, box, anchor.add(0.0, ey * scale, 0.0)));
         return Math.max(worst, init_D_Vec3(eyes, box, anchor.add(0.0, -ey * scale, 0.0)));
      }

      private static AABB init_AABB_Vec3(Vec3 anchor, double ex, double ey, double ez, double scale) {
         return new AABB(
            anchor.x - ex * scale, anchor.y - ey * scale, anchor.z - ez * scale, anchor.x + ex * scale, anchor.y + ey * scale, anchor.z + ez * scale
         );
      }

      private static Vec3 init_Vec3_Vec3(Vec3 from, Vec3 to, double t) {
         return new Vec3(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t, from.z + (to.z - from.z) * t);
      }
   }

   private static final class Inner_StreamUtil {
      double init_D;
      double load_D;
      double read_D;
      double write;
      double update;
      double sync;
      double flush;
      boolean init_Z;
      boolean load_Z;
      boolean read_Z;
   }
}
