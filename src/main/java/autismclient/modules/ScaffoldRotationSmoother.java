package autismclient.modules;

import autismclient.api.module.ValueRange;
import autismclient.util.RotationMath;
import java.util.Random;

final class ScaffoldRotationSmoother {
   private final Random init_Random;
   private final autismclient.util.HumanHandProfile init_am;
   private double init_D;
   private double load_D;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private RotationMath.Rotation init_ce_DataUtil;
   private RotationMath.Rotation load_ce_DataUtil;
   private RotationMath.Rotation read_ce_DataUtil;
   private double read_D;
   private double write_D;
   private double update_D;
   private double sync;
   private int init_I = -2147483648;
   private int load_I = -2147483648;
   private double flush;
   private double close;
   private double open;
   private boolean write_Z;
   private boolean update_Z;

   ScaffoldRotationSmoother(autismclient.util.HumanHandProfile hand, Random random) {
      this.init_am = hand;
      this.init_Random = random;
   }

   static ScaffoldRotationSmoother init_aM() {
      Random random = new Random();
      return new ScaffoldRotationSmoother(autismclient.util.HumanHandProfile.init_am().init_am_Random(random), random);
   }

   void init_V() {
      this.init_Z = this.load_Z = this.write_Z = false;
      this.init_ce_DataUtil = this.load_ce_DataUtil = this.read_ce_DataUtil = null;
      this.init_I = this.load_I = -2147483648;
      this.read_D = 0.0;
   }

   void init_V_ValueRange(ValueRange band) {
      this.init_V_aM_DataUtil(this.init_aM_DataUtil_ValueRange(band));
   }

   ScaffoldRotationSmoother.Inner_DataUtil init_aM_DataUtil_ValueRange(ValueRange band) {
      ValueRange safe = band.clamp(0.0, 100.0);
      return new ScaffoldRotationSmoother.Inner_DataUtil(safe.random(this.init_Random), safe.random(this.init_Random));
   }

   void init_V_aM_DataUtil(ScaffoldRotationSmoother.Inner_DataUtil selected) {
      this.init(selected, false);
   }

   void init(ScaffoldRotationSmoother.Inner_DataUtil selected, boolean efficient) {
      this.init_V();
      this.init_D = selected.init_D();
      this.load_D = selected.load_D();
      this.update_Z = efficient;
      this.init_Z = true;
   }

   static double init_D_D(double percent) {
      return 2.0 + Math.max(0.0, Math.min(100.0, percent)) * 0.01;
   }

   static int init_I_D(double percent) {
      return (int)Math.ceil(init_D_D(percent) - 1.0E-9);
   }

   int init_I_ValueRange(ValueRange band) {
      return Math.max(0, this.load(band) - 2);
   }

   int load(ValueRange band) {
      return init_I_D(this.init_Z ? this.init_D : band.max());
   }

   int read(ValueRange band) {
      return init_I_D(this.init_Z ? this.load_D : band.max());
   }

   int init(ValueRange band, RotationMath.Rotation from, RotationMath.Rotation target, double gcd) {
      if (this.init_Z && this.update_Z && from != null && target != null) {
         double quantum = Double.isFinite(gcd) && gcd > 0.0 ? gcd : 0.15;
         RotationMath.Rotation endpoint = new RotationMath.Rotation(from.init() + RotationMath.init(target.init(), from.init()), target.load());
         if (this.init(from, endpoint, band, quantum)) {
            return 1;
         }

         double limit = this.init(band, true) * 2.0F - quantum;
         if (Math.abs(RotationMath.init(target.init(), from.init())) <= limit && Math.abs(target.load() - from.load()) <= limit) {
            return 2;
         }
      }

      return this.read(band);
   }

   int write(ValueRange band) {
      return !this.load_Z ? this.load(band) : (this.read_Z ? 0 : this.init_I());
   }

   float init_F_ValueRange(ValueRange band) {
      return this.init(band, this.read_Z);
   }

   float init(ValueRange band, boolean isReturn) {
      double percent = this.init_Z ? (isReturn ? this.load_D : this.init_D) : band.max();
      return (float)(95.0 - 0.15 * percent);
   }

   RotationMath.Rotation init(RotationMath.Rotation from, RotationMath.Rotation target, boolean isReturn, ValueRange band, double gcd, int tick) {
      if (this.init_I == tick) {
         return this.read_ce_DataUtil;
      }

      if (!this.init_Z) {
         this.init_V_ValueRange(band);
      }

      if (this.load_Z && this.read_ce_DataUtil != null) {
         float shift = 360.0F * Math.round((from.init() - this.read_ce_DataUtil.init()) / 360.0F);
         if (shift != 0.0F) {
            this.init_ce_DataUtil = new RotationMath.Rotation(this.init_ce_DataUtil.init() + shift, this.init_ce_DataUtil.load());
            if (this.load_ce_DataUtil != null) {
               this.load_ce_DataUtil = new RotationMath.Rotation(this.load_ce_DataUtil.init() + shift, this.load_ce_DataUtil.load());
            }

            this.close += shift;
            this.open += shift;
         }
      }

      boolean changedCourse = this.load_Z
         && this.read_Z
         && isReturn
         && this.load_ce_DataUtil != null
         && Math.abs(RotationMath.init(target.init(), this.load_ce_DataUtil.init())) > 45.0F;
      if (changedCourse) {
         this.init_ce_DataUtil = from;
         this.read_D = 0.0;
         this.write_D = init_D_D(this.load_D);
         this.close = from.init() + RotationMath.init(target.init(), from.init());
      }

      if (!this.load_Z || this.read_Z != isReturn) {
         this.load_Z = true;
         this.read_Z = isReturn;
         this.init_ce_DataUtil = from;
         if (!isReturn) {
            this.close = from.init();
            this.write_Z = true;
         }

         this.open = isReturn && this.write_Z ? this.init_D_ce_DataUtil(target) : from.init() + RotationMath.init(target.init(), from.init());
         this.read_D = 0.0;
         this.write_D = init_D_D(isReturn ? this.load_D : this.init_D);
         this.update_D = Math.max(0.84, Math.min(1.08, this.init_am.close() + 0.06 * this.init_Random.nextGaussian()));
         this.sync = Math.max(-0.012, Math.min(0.012, this.init_am.sync() * 0.25 + this.init_am.flush() * 0.2 * this.init_Random.nextGaussian()));
      }

      if (isReturn && this.write_Z) {
         this.open = this.init_D_ce_DataUtil(target);
      } else {
         this.open = this.open + RotationMath.init(target.init(), (float)this.open);
      }

      this.load_ce_DataUtil = new RotationMath.Rotation((float)this.open, target.load());
      if (this.update_Z
         && this.read_D == 0.0
         && Math.abs(this.load_ce_DataUtil.init() - this.init_ce_DataUtil.init()) <= this.init(band, isReturn) * 2.0F - gcd
         && Math.abs(this.load_ce_DataUtil.load() - this.init_ce_DataUtil.load()) <= this.init(band, isReturn) * 2.0F - gcd) {
         this.write_D = 2.0;
      }

      if (isReturn && this.read_D == 0.0 && this.init(from, this.load_ce_DataUtil, band, gcd)) {
         this.write_D = 1.0;
      }

      if (this.update_Z) {
         double quantum = Double.isFinite(gcd) && gcd > 0.0 ? gcd : 0.15;
         double limit = Math.max(quantum, Math.floor(this.init(band, isReturn) / quantum) * quantum);
         double remaining = Math.max(Math.abs(this.load_ce_DataUtil.init() - from.init()), Math.abs(this.load_ce_DataUtil.load() - from.load()));
         int needed = Math.max(0, (int)Math.ceil((remaining - quantum * 0.501) / limit));
         this.write_D = Math.max(this.write_D, this.read_D + needed);
      }

      this.load_I = tick;
      this.flush = Math.min(this.write_D, this.read_D + 1.0);
      return this.init(from, this.flush, band, gcd);
   }

   private double init_D_ce_DataUtil(RotationMath.Rotation target) {
      double courseChange = RotationMath.init(target.init(), (float)this.close);
      return Math.abs(courseChange) <= 45.0 ? this.close + courseChange : this.init_ce_DataUtil.init() + RotationMath.init(target.init(), this.init_ce_DataUtil.init());
   }

   private boolean init(RotationMath.Rotation from, RotationMath.Rotation target, ValueRange band, double gcd) {
      double quantum = Double.isFinite(gcd) && gcd > 0.0 ? gcd : 0.15;
      long limit = (long)Math.floor(this.init(band, true) / quantum + 1.0E-9);
      return Math.abs(Math.round((target.init() - from.init()) / quantum)) <= limit && Math.abs(Math.round((target.load() - from.load()) / quantum)) <= limit;
   }

   private RotationMath.Rotation init(RotationMath.Rotation from, double time, ValueRange band, double gcd) {
      double s = Math.min(1.0, time / this.write_D);
      double progress = autismclient.util.HumanizedAimStepper.init_D_D(Math.pow(s, this.update_D));
      double yaw = this.load_ce_DataUtil.init() - this.init_ce_DataUtil.init();
      double pitch = this.load_ce_DataUtil.load() - this.init_ce_DataUtil.load();
      double distance = Math.hypot(yaw, pitch);
      double bend = s >= 1.0
         ? 0.0
         : Math.sin(3.141592653589793 * s) * Math.min(1.5, distance * Math.abs(this.sync)) * Math.signum(this.sync) * (this.write_D - 1.0);
      RotationMath.Rotation desired = new RotationMath.Rotation(
         (float)(this.init_ce_DataUtil.init() + yaw * progress - (distance > 0.001 ? pitch / distance * bend : 0.0)),
         (float)(this.init_ce_DataUtil.load() + pitch * progress + (distance > 0.001 ? yaw / distance * bend : 0.0))
      );
      double quantum = Double.isFinite(gcd) && gcd > 0.0 ? gcd : 0.15;
      double interval = Math.floor(this.init_F_ValueRange(band) / quantum + 1.0E-9) * quantum;
      double reserve = Math.max(0.0, Math.ceil(this.write_D - time - 1.0E-9)) * Math.max(0.0, interval - quantum * 0.5);
      desired = new RotationMath.Rotation(
         (float)Math.max(this.load_ce_DataUtil.init() - reserve, Math.min(this.load_ce_DataUtil.init() + reserve, desired.init())),
         (float)Math.max(this.load_ce_DataUtil.load() - reserve, Math.min(this.load_ce_DataUtil.load() + reserve, desired.load()))
      );
      return RotationMath.load(from, desired, this.init_F_ValueRange(band), this.init_F_ValueRange(band), gcd);
   }

   void init(RotationMath.Rotation rotation, int tick) {
      if (this.init_I != tick && this.load_I == tick) {
         this.read_D = this.flush;
      }

      this.read_ce_DataUtil = rotation;
      this.init_I = tick;
      if (this.read_Z
         && this.read_D >= this.write_D
         && this.load_ce_DataUtil != null
         && Math.abs(rotation.init() - this.load_ce_DataUtil.init()) < 1.0F
         && Math.abs(rotation.load() - this.load_ce_DataUtil.load()) < 1.0F) {
         this.write_Z = false;
      }
   }

   float init(RotationMath.Rotation from, ValueRange band, double gcd) {
      return this.load_Z && this.load_ce_DataUtil != null ? this.init(from, Math.min(this.write_D, this.read_D + 1.0), band, gcd).init() : from.init();
   }

   int init_I() {
      return this.load_Z ? Math.max(0, (int)Math.ceil(this.write_D - this.read_D)) : 0;
   }

   boolean init_Z() {
      return this.load_Z && this.read_Z && this.write_Z;
   }

   @Override
   public String toString() {
      return "smooth=" + this.init_D + "/" + this.load_D + " return=" + this.read_Z + " time=" + this.read_D + "/" + this.write_D;
   }

   record Inner_DataUtil(double init, double load) {
      int init_I() {
         return ScaffoldRotationSmoother.init_I_D(this.init);
      }

      int load_I() {
         return ScaffoldRotationSmoother.init_I_D(this.load);
      }

      public double init_D() {
         return this.init;
      }

      public double load_D() {
         return this.load;
      }
   }
}
