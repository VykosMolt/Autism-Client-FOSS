package autismclient.util;

import java.util.Objects;
import java.util.Random;

public final class ap {
   private static final double init = 0.15;
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
   private static final double reset = 1.1;
   private static final double start = 2.75;
   private static final double stop = 0.5;
   private static final double next = 89.9;

   private ap() {
   }

   public static double init_D_D(double gcd) {
      return 0.5 + (gcd > 0.0 ? gcd : 0.15);
   }

   public static double[] init_DArr_ap_TaskQueue(ap.Inner_TaskQueue s) {
      return new double[]{s.read_D, s.write_D};
   }

   public static boolean init_Z_ap_TaskQueue(ap.Inner_TaskQueue s) {
      return s != null && s.init_Z;
   }

   public static ce.Inner_DataUtil init_ce_DataUtil_ap_TaskQueue(ap.Inner_TaskQueue s) {
      return init_Z_ap_TaskQueue(s) ? new ce.Inner_DataUtil((float)s.init_D, (float)s.load_D) : null;
   }

   public static void init_V_ap_TaskQueue_ce_DataUtil(ap.Inner_TaskQueue s, ce.Inner_DataUtil from) {
      Objects.requireNonNull(s, "stream");
      Objects.requireNonNull(from, "from");
      s.init_D = from.init();
      s.load_D = from.load();
      s.read_D = 0.0;
      s.write_D = 0.0;
      s.init_I = 0;
      s.load_I = 0;
      s.read_F = 0.0F;
      s.init_ap_BufferHelper = null;
      s.load_ap_BufferHelper = null;
      s.sync = from.init();
      s.flush = from.load();
      s.init_Z = true;
   }

   public static void init_V_ap_TaskQueue(ap.Inner_TaskQueue s) {
      Objects.requireNonNull(s, "stream");
      s.init_Z = false;
      s.init_D = 0.0;
      s.load_D = 0.0;
      s.read_D = 0.0;
      s.write_D = 0.0;
      s.init_I = 0;
      s.load_I = 0;
      s.read_F = 0.0F;
      s.init_ap_BufferHelper = null;
      s.load_ap_BufferHelper = null;
      s.sync = 0.0;
      s.flush = 0.0;
   }

   public static void init_V_ap_TaskQueue_3(ap.Inner_TaskQueue s, ce.Inner_DataUtil at, double lastStepYaw, double lastStepPitch, double gcd) {
      Objects.requireNonNull(s, "stream");
      Objects.requireNonNull(at, "at");
      double g = gcd > 0.0 ? gcd : 0.15;
      s.init_D = at.init();
      s.load_D = at.load();
      s.read_D = Math.abs(lastStepYaw);
      s.write_D = Math.abs(lastStepPitch);
      s.init_I = (int)Math.round(s.read_D / g);
      s.load_I = (int)Math.round(s.write_D / g);
      s.read_F = (float)s.read_D;
      s.init_ap_BufferHelper = null;
      s.load_ap_BufferHelper = null;
      s.sync = at.init();
      s.flush = at.load();
      s.init_Z = true;
   }

   public static ap.Inner_ConfigStore init_ap_ConfigStore_ap_TaskQueue(
      ap.Inner_TaskQueue s, ce.Inner_DataUtil goal, float maxYawStep, float maxPitchStep, double gcd
   ) {
      return init_ap_ConfigStore_ap_TaskQueue(s, goal, maxYawStep, maxPitchStep, gcd, true);
   }

   public static ap.Inner_ConfigStore init_ap_ConfigStore_ap_TaskQueue(
      ap.Inner_TaskQueue s, ce.Inner_DataUtil goal, float maxYawStep, float maxPitchStep, double gcd, boolean applyGoalOffset
   ) {
      return init_ap_ConfigStore_ap_TaskQueue(s, goal, maxYawStep, maxPitchStep, gcd, applyGoalOffset, ap.Inner_StreamUtil.STANDARD);
   }

   public static ap.Inner_ConfigStore init_ap_ConfigStore_ap_TaskQueue(
      ap.Inner_TaskQueue s, ce.Inner_DataUtil goal, float maxYawStep, float maxPitchStep, double gcd, boolean applyGoalOffset, ap.Inner_StreamUtil profile
   ) {
      Objects.requireNonNull(s, "stream");
      Objects.requireNonNull(goal, "goal");
      Objects.requireNonNull(profile, "profile");
      if (!s.init_Z) {
         throw new IllegalStateException("stream is not seeded; seed() from the current server rotation first");
      }

      double g = gcd > 0.0 ? gcd : 0.15;
      ap.Inner_BufferHelper rolls;
      if (init_Z_ap_TaskQueue(s, goal, profile)) {
         s.load_ap_BufferHelper = init_ap_BufferHelper_Random(s.init_Random, goal, profile);
         rolls = s.load_ap_BufferHelper;
      } else {
         s.load_ap_BufferHelper = null;
         rolls = s.init_ap_BufferHelper;
      }

      s.sync = goal.init();
      s.flush = goal.load();
      s.update = profile == ap.Inner_StreamUtil.STANDARD ? init_D_D_Random(s.update, s.init_Random) : 1.0;
      s.read_I = init_I_I_Random(s.read_I, s.init_Random);
      s.write_I = init_I_I_Random(s.write_I, s.init_Random);
      double effectiveYaw = goal.init() + (applyGoalOffset ? rolls.update : 0.0);
      double effectivePitch = read(goal.load() + (applyGoalOffset ? rolls.sync : 0.0));
      ap.Inner_DataUtil yawAxis = new ap.Inner_DataUtil(load_D(effectiveYaw - s.init_D), maxYawStep, g, s.read_D, rolls.write, 0.0 / 0.0);
      ap.Inner_DataUtil pitchAxis = new ap.Inner_DataUtil(effectivePitch - s.load_D, maxPitchStep, g, s.write_D, rolls.write, s.load_D);
      int kYaw = init_I_ap_DataUtil(yawAxis, s.init_I, rolls.init_D, rolls.read * s.update, s.init_Random, s.read_I);
      int kPitch = init_I_ap_DataUtil(pitchAxis, s.load_I, rolls.load, rolls.read * s.update, s.init_Random, s.write_I);
      if (kYaw == 0 && Math.abs(kPitch * g) > 2.75 && yawAxis.init() && s.init_Random.nextDouble() < 0.5) {
         kYaw = init_I_ap_DataUtil_Random(yawAxis, s.init_Random);
      }

      if (kPitch == 0 && Math.abs(kYaw * g) > 2.75 && pitchAxis.init() && s.init_Random.nextDouble() < 0.5) {
         kPitch = init_I_ap_DataUtil_Random(pitchAxis, s.init_Random);
      }

      return new ap.Inner_ConfigStore(s, kYaw, kPitch, g);
   }

   public static ce.Inner_DataUtil init_ce_DataUtil_ap_TaskQueue_ap_ConfigStore(ap.Inner_TaskQueue s, ap.Inner_ConfigStore step) {
      Objects.requireNonNull(s, "stream");
      Objects.requireNonNull(step, "step");
      if (!s.init_Z) {
         throw new IllegalStateException("stream is not seeded; seed() first");
      }

      if (step.init_ap_TaskQueue != s) {
         throw new IllegalArgumentException("step was computed on a different stream");
      }

      if (s.load_ap_BufferHelper != null) {
         s.init_ap_BufferHelper = s.load_ap_BufferHelper;
         s.load_ap_BufferHelper = null;
      }

      float yawBeforeTick = (float)s.init_D;
      s.init_D = init_D_D(s.init_D, step.init_I, step.init_D);
      s.load_D = read(init_D_D(s.load_D, step.load, step.init_D));
      s.read_D = Math.abs(step.init_I * step.init_D);
      s.write_D = Math.abs(step.load * step.init_D);
      s.init_I = step.init_I;
      s.load_I = step.load;
      init_V_ap_TaskQueue_F(s, yawBeforeTick);
      return init_ce_DataUtil_ap_TaskQueue(s);
   }

   public static ce.Inner_DataUtil init_ce_DataUtil_ap_TaskQueue(ap.Inner_TaskQueue s, ce.Inner_DataUtil goal, float maxYawStep, float maxPitchStep, double gcd) {
      return init_ce_DataUtil_ap_TaskQueue(s, goal, maxYawStep, maxPitchStep, gcd, true);
   }

   public static ce.Inner_DataUtil init_ce_DataUtil_ap_TaskQueue(
      ap.Inner_TaskQueue s, ce.Inner_DataUtil goal, float maxYawStep, float maxPitchStep, double gcd, boolean applyGoalOffset
   ) {
      return init_ce_DataUtil_ap_TaskQueue_2(s, goal, maxYawStep, maxPitchStep, gcd, applyGoalOffset, ap.Inner_StreamUtil.STANDARD);
   }

   public static ce.Inner_DataUtil init_ce_DataUtil_ap_TaskQueue_2(
      ap.Inner_TaskQueue s, ce.Inner_DataUtil goal, float maxYawStep, float maxPitchStep, double gcd, boolean applyGoalOffset, ap.Inner_StreamUtil profile
   ) {
      Objects.requireNonNull(s, "stream");
      Objects.requireNonNull(goal, "goal");
      Objects.requireNonNull(profile, "profile");
      if (!s.init_Z) {
         init_V_ap_TaskQueue_ce_DataUtil(s, goal);
         return init_ce_DataUtil_ap_TaskQueue(s);
      } else {
         return init_ce_DataUtil_ap_TaskQueue_ap_ConfigStore(
            s, init_ap_ConfigStore_ap_TaskQueue(s, goal, maxYawStep, maxPitchStep, gcd, applyGoalOffset, profile)
         );
      }
   }

   public static ce.Inner_DataUtil init_ce_DataUtil_ap_TaskQueue(
      ap.Inner_TaskQueue s, double yawDelta, double pitchDelta, float maxYawStep, float maxPitchStep, double gcd, double accelLimit
   ) {
      return init(s, yawDelta, pitchDelta, maxYawStep, maxPitchStep, gcd, accelLimit, accelLimit);
   }

   public static ce.Inner_DataUtil init(
      ap.Inner_TaskQueue s, double yawDelta, double pitchDelta, float maxYawStep, float maxPitchStep, double gcd, double accelUp, double accelDown
   ) {
      return init(s, yawDelta, pitchDelta, maxYawStep, maxPitchStep, gcd, accelUp, accelDown, false);
   }

   public static ce.Inner_DataUtil init(
      ap.Inner_TaskQueue s,
      double yawDelta,
      double pitchDelta,
      float maxYawStep,
      float maxPitchStep,
      double gcd,
      double accelUp,
      double accelDown,
      boolean holdPitch
   ) {
      return init(s, yawDelta, pitchDelta, maxYawStep, maxPitchStep, gcd, accelUp, accelDown, holdPitch, false);
   }

   public static ce.Inner_DataUtil init(
      ap.Inner_TaskQueue s,
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
      if (!s.init_Z) {
         throw new IllegalStateException("stream is not seeded; seed() from the current server rotation first");
      }

      double g = gcd > 0.0 ? gcd : 0.15;
      ap.Inner_CachePool yaw = new ap.Inner_CachePool(
         maxYawStep, g, s.read_D, s.init_I, s.read_D > 0.0 ? accelUp : accelDown, accelDown, 0.0 / 0.0, Math.abs(s.read_F)
      );
      ap.Inner_CachePool pitch = new ap.Inner_CachePool(maxPitchStep, g, s.write_D, s.load_I, s.write_D > 0.0 ? accelUp : accelDown, accelDown, s.load_D);
      int kYaw = yaw.init(yawDelta, s.init_Random);
      int kPitch = pitch.init(pitchDelta, s.init_Random);
      if (kYaw == 0 && Math.abs(kPitch * g) > 2.75 && s.init_Random.nextDouble() < 0.5) {
         kYaw = yaw.init(s.init_Random);
      }

      if (kPitch == 0 && !holdPitch && Math.abs(kYaw * g) > 2.75 && s.init_Random.nextDouble() < 0.5) {
         kPitch = pitch.init(s.init_Random);
      }

      s.init_ap_BufferHelper = null;
      s.load_ap_BufferHelper = null;
      float yawBeforeTick = (float)s.init_D;
      float pitchBeforeTick = (float)s.load_D;
      s.init_D = init_D_D(s.init_D, kYaw, g);
      s.load_D = read(init_D_D(s.load_D, kPitch, g));
      s.read_D = Math.abs(kYaw * g);
      s.write_D = Math.abs(kPitch * g);
      s.init_I = kYaw;
      s.load_I = kPitch;
      boolean moving = Math.abs((float)s.init_D - yawBeforeTick) + Math.abs((float)s.load_D - pitchBeforeTick) > 0.75F;
      init_V_ap_TaskQueue_2(s, g, yawBeforeTick, yaw, naturalMouseAngles && moving);
      if (naturalMouseAngles) {
         init_V_ap_TaskQueue(s, g, pitchBeforeTick, yawBeforeTick, pitch);
      }

      init_V_ap_TaskQueue_F(s, yawBeforeTick);
      return init_ce_DataUtil_ap_TaskQueue(s);
   }

   private static void init_V_ap_TaskQueue_2(ap.Inner_TaskQueue s, double gcd, float before, ap.Inner_CachePool axis, boolean naturalMouseAngles) {
      float emitted = (float)s.init_D;
      float wrapped = init_F_F(emitted);
      boolean forbidden = init_Z_F(wrapped);
      if (!forbidden && !Float.isNaN(s.init_F)) {
         forbidden = Float.compare(wrapped, init_F_F(s.init_F)) == 0;
      }

      if (!forbidden && !Float.isNaN(s.load_F)) {
         forbidden = Float.compare(wrapped, init_F_F(s.load_F)) == 0;
      }

      if (!forbidden && naturalMouseAngles) {
         forbidden = init_Z_F_D(emitted, gcd);
      }

      if (forbidden) {
         int step = s.init_I >= 0 ? 1 : -1;

         for (int distance = 1; distance <= (naturalMouseAngles ? 8 : 4); distance++) {
            for (int direction : new int[]{step, -step}) {
               int offset = distance * direction;
               int candidate = s.init_I + offset;
               if (axis.load(candidate) && (!naturalMouseAngles || !init_Z_D(Math.abs(candidate * gcd)))) {
                  double corrected = init_D_D(before, candidate, gcd);
                  float next = init_F_F((float)corrected);
                  if (!init_Z_F(next)
                     && (Float.isNaN(s.init_F) || Float.compare(next, init_F_F(s.init_F)) != 0)
                     && (Float.isNaN(s.load_F) || Float.compare(next, init_F_F(s.load_F)) != 0)) {
                     float delta = Math.abs((float)corrected - before);
                     if ((!naturalMouseAngles || !init_Z_F_D((float)corrected, gcd)) && (!(delta > 1.25) || !(Math.abs(delta - Math.abs(s.read_F)) < 1.0E-4F))) {
                        s.init_D = corrected;
                        s.init_I = candidate;
                        s.read_D = Math.abs(candidate * gcd);
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean init_Z_F_D(float angle, double gcd) {
      float factor = (float)Math.cbrt(gcd / 1.2);
      float quantum = factor * factor * factor * 1.2F;
      return quantum > 0.0F && Math.abs(angle - (angle - angle % quantum)) == 0.0F;
   }

   private static void init_V_ap_TaskQueue(ap.Inner_TaskQueue s, double gcd, float before, float yawBefore, ap.Inner_CachePool axis) {
      float delta = (float)s.load_D - before;
      float yawDelta = (float)s.init_D - yawBefore;
      if (!(Math.abs(yawDelta) + Math.abs(delta) <= 0.75F) && init_Z_F_D((float)s.load_D, gcd)) {
         int sign = s.load_I == 0 ? (before > 0.0F ? -1 : 1) : Integer.signum(s.load_I);

         for (int distance = 1; distance <= 8; distance++) {
            for (int direction : new int[]{sign, -sign}) {
               int candidate = s.load_I + distance * direction;
               if (Integer.signum(candidate) == sign && axis.load(candidate) && !init_Z_D(Math.abs(candidate * gcd))) {
                  double corrected = init_D_D(before, candidate, gcd);
                  if (!init_Z_F_D((float)corrected, gcd)) {
                     s.load_D = corrected;
                     s.load_I = candidate;
                     s.write_D = Math.abs(candidate * gcd);
                     return;
                  }
               }
            }
         }
      }
   }

   static double init_D_D(double angle, int counts, double gcd) {
      return counts == 0 ? angle : (float)angle + (float)(counts * (gcd / 0.15)) * 0.15F;
   }

   private static void init_V_ap_TaskQueue_F(ap.Inner_TaskQueue s, float before) {
      s.read_F = (float)s.init_D - before;
   }

   private static boolean init_Z_F(float wrappedYaw) {
      float quarter = wrappedYaw / 90.0F;
      return Float.compare(quarter, Math.round(quarter)) == 0;
   }

   private static float init_F_F(float yaw) {
      float wrapped = yaw % 360.0F;
      if (wrapped > 180.0F) {
         wrapped -= 360.0F;
      }

      if (wrapped <= -180.0F) {
         wrapped += 360.0F;
      }

      return wrapped;
   }

   public static void init_V_ap_TaskQueue(ap.Inner_TaskQueue s, float exactYaw, float exactClosestYaw) {
      Objects.requireNonNull(s, "stream");
      s.init_F = exactYaw;
      s.load_F = exactClosestYaw;
   }

   static double[] load(ap.Inner_TaskQueue s) {
      return init_Z_ap_TaskQueue(s) && s.init_ap_BufferHelper != null
         ? new double[]{s.sync + s.init_ap_BufferHelper.update, read(s.flush + s.init_ap_BufferHelper.sync)}
         : null;
   }

   private static ap.Inner_BufferHelper init_ap_BufferHelper_Random(Random random, ce.Inner_DataUtil goal, ap.Inner_StreamUtil profile) {
      ap.Inner_BufferHelper rolls = new ap.Inner_BufferHelper();
      rolls.init_ap_StreamUtil = profile;
      rolls.init_D = profile.init_D + profile.load_D * random.nextDouble();
      rolls.load = profile.init_D + profile.load_D * random.nextDouble();
      rolls.read = profile.read_D + profile.write_D * random.nextDouble();
      rolls.write = profile.update_D + profile.sync_D * random.nextDouble();
      rolls.update = init_D_Random(random);
      rolls.sync = init_D_Random(random);
      rolls.flush = goal.init();
      rolls.close = goal.load();
      return rolls;
   }

   private static double init_D_Random(Random random) {
      double magnitude = 0.75 + 1.25 * random.nextDouble();
      return random.nextBoolean() ? magnitude : -magnitude;
   }

   private static boolean init_Z_ap_TaskQueue(ap.Inner_TaskQueue s, ce.Inner_DataUtil goal, ap.Inner_StreamUtil profile) {
      if (s.init_ap_BufferHelper == null) {
         return true;
      }

      if (s.init_ap_BufferHelper.init_ap_StreamUtil != profile) {
         return true;
      }

      double dYaw = load_D(goal.init() - s.init_ap_BufferHelper.flush);
      double dPitch = goal.load() - s.init_ap_BufferHelper.close;
      return Math.max(Math.abs(dYaw), Math.abs(dPitch)) > 4.0;
   }

   private static int init_I_ap_DataUtil(ap.Inner_DataUtil axis, int lastK, double peak, double ease, Random random, int dither) {
      double desired = axis.init_D <= 3.0 ? axis.init_D : Math.min(Math.min(axis.init_D * ease, axis.load * peak), axis.write);
      int k = axis.init_I * (int)Math.round(desired / axis.read);
      if (!axis.init() && (k == 0 || Integer.signum(k) != axis.init_I)) {
         k = axis.init_I;
      }

      while (k != 0 && !axis.init(k)) {
         k -= Integer.signum(k);
      }

      if (k == lastK && Math.abs(k * axis.read) > 1.25) {
         int first = random.nextBoolean() ? 1 : -1;
         if (axis.init(k + first)) {
            k += first;
         } else if (axis.init(k - first)) {
            k -= first;
         }
      }

      if (dither != 0 && axis.init(k + dither) && init_Z_I(k + dither, lastK, axis.read)) {
         k += dither;
      }

      if (k != 0 && load_Z(Math.abs(k * axis.read))) {
         int first = random.nextBoolean() ? 1 : -1;
         if (axis.init(k + first) && init_Z_I(k + first, lastK, axis.read) && !load_Z(Math.abs((k + first) * axis.read))) {
            k += first;
         } else if (axis.init(k - first) && init_Z_I(k - first, lastK, axis.read) && !load_Z(Math.abs((k - first) * axis.read))) {
            k -= first;
         }
      }

      return k;
   }

   private static boolean init_Z_I(int k, int lastK, double gcd) {
      return k != lastK || Math.abs(k * gcd) <= 1.25;
   }

   private static int init_I_ap_DataUtil_Random(ap.Inner_DataUtil axis, Random random) {
      int sign = random.nextBoolean() ? 1 : -1;
      if (axis.init(sign)) {
         return sign;
      } else {
         return axis.init(-sign) ? -sign : 0;
      }
   }

   private static int init_I_I_Random(int state, Random random) {
      double r = random.nextDouble();
      if (state == 0) {
         return r < 0.25 ? (random.nextBoolean() ? 1 : -1) : 0;
      } else if (r < 0.45) {
         return 0;
      } else {
         return r < 0.55 ? -state : state;
      }
   }

   private static double init_D_D_Random(double w, Random random) {
      w += -0.3 * (w - 1.0) + (random.nextDouble() - 0.5) * 0.08;
      return Math.max(0.9, Math.min(1.1, w));
   }

   private static boolean init_Z_D(double absDelta) {
      return Math.abs(absDelta - 0.25 * Math.round(absDelta / 0.25)) <= 2.0E-5;
   }

   private static boolean load_Z(double absDelta) {
      return Math.abs(absDelta - 0.1 * Math.round(absDelta / 0.1)) <= 1.0E-6 || Math.abs(absDelta - 0.25 * Math.round(absDelta / 0.25)) <= 1.0E-6;
   }

   private static double load_D(double degrees) {
      double wrapped = degrees % 360.0;
      if (wrapped >= 180.0) {
         wrapped -= 360.0;
      }

      if (wrapped < -180.0) {
         wrapped += 360.0;
      }

      return wrapped;
   }

   private static double read(double pitch) {
      return Math.max(-89.9, Math.min(89.9, pitch));
   }

   private static final class Inner_BufferHelper {
      ap.Inner_StreamUtil init_ap_StreamUtil;
      double init_D;
      double load;
      double read;
      double write;
      double update;
      double sync;
      double flush;
      double close;
   }

   private static final class Inner_CachePool {
      private final double init_D;
      private final int init_I;
      private final double load;
      private final double read;
      private final double write;
      private final float init_F;

      Inner_CachePool(float cap, double gcd, double lastStep, int lastK, double accelUp, double accelDown, double pitchAcc) {
         this(cap, gcd, lastStep, lastK, accelUp, accelDown, pitchAcc, 0.0F / 0.0F);
      }

      Inner_CachePool(float cap, double gcd, double lastStep, int lastK, double accelUp, double accelDown, double pitchAcc, float emitted) {
         this.init_D = gcd;
         this.init_I = lastK;
         this.load = Math.min(Math.max(0.0, cap), lastStep + accelUp);
         this.read = Math.min(this.load, Math.max(0.0, lastStep - accelDown));
         this.write = pitchAcc;
         this.init_F = emitted;
      }

      int init(double delta, Random random) {
         int k = (int)Math.round(delta / this.init_D);

         while (k != 0 && Math.abs(k * this.init_D) > this.load + 1.0E-9) {
            k -= Integer.signum(k);
         }

         if (Math.abs(k * this.init_D) < this.read - 1.0E-9) {
            int dir = k != 0 ? Integer.signum(k) : (this.init_I != 0 ? Integer.signum(this.init_I) : 1);
            k = dir * (int)Math.ceil(this.read / this.init_D - 1.0E-9);

            while (k != 0 && Math.abs(k * this.init_D) > this.load + 1.0E-9) {
               k -= Integer.signum(k);
            }
         }

         while (k != 0 && !this.read(k)) {
            k -= Integer.signum(k);
         }

         if (k != 0 && ap.init_Z_D(Math.abs(k * this.init_D))) {
            int first = random.nextBoolean() ? 1 : -1;
            if (this.init(k + first) && !ap.init_Z_D(Math.abs((k + first) * this.init_D))) {
               k += first;
            } else if (this.init(k - first) && !ap.init_Z_D(Math.abs((k - first) * this.init_D))) {
               k -= first;
            }
         }

         if (Math.abs(k) == Math.abs(this.init_I) && Math.abs(k * this.init_D) > 1.25) {
            int first = random.nextBoolean() ? 1 : -1;
            if (this.load(k + first)) {
               k += first;
            } else if (this.load(k - first)) {
               k -= first;
            }
         }

         if (Math.abs(k * this.init_D) > 1.25 && Math.abs((float)Math.abs(k * this.init_D) - this.init_F) <= (float)this.init_D * 0.5F) {
            int first = random.nextBoolean() ? 1 : -1;
            if (this.load(k + first)) {
               k += first;
            } else if (this.load(k - first)) {
               k -= first;
            }
         }

         return k;
      }

      int init(Random random) {
         int sign = random.nextBoolean() ? 1 : -1;
         if (this.init(sign)) {
            return sign;
         } else {
            return this.init(-sign) ? -sign : 0;
         }
      }

      private boolean init(int k) {
         double delta = Math.abs(k * this.init_D);
         return delta <= this.load + 1.0E-9 && delta >= this.read - 1.0E-9 && this.read(k);
      }

      private boolean load(int k) {
         double delta = Math.abs(k * this.init_D);
         return this.init(k)
            && (
               delta <= 1.25
                  || Math.abs(k) != Math.abs(this.init_I) && (Float.isNaN(this.init_F) || Math.abs((float)delta - this.init_F) > (float)this.init_D * 0.5F)
            );
      }

      private boolean read(int k) {
         return Double.isNaN(this.write) || Math.abs(this.write + k * this.init_D) <= 89.90000000100001;
      }
   }

   public static final class Inner_ConfigStore {
      private final ap.Inner_TaskQueue init_ap_TaskQueue;
      private final int init_I;
      private final int load;
      private final double init_D;

      private Inner_ConfigStore(ap.Inner_TaskQueue owner, int kYaw, int kPitch, double gcd) {
         this.init_ap_TaskQueue = owner;
         this.init_I = kYaw;
         this.load = kPitch;
         this.init_D = gcd;
      }

      public ce.Inner_DataUtil init() {
         return new ce.Inner_DataUtil(
            (float)ap.init_D_D(this.init_ap_TaskQueue.init_D, this.init_I, this.init_D),
            (float)ap.read(ap.init_D_D(this.init_ap_TaskQueue.load_D, this.load, this.init_D))
         );
      }
   }

   private static final class Inner_DataUtil {
      final double init_D;
      final int init_I;
      final double load;
      final double read;
      final double write;
      final double update;
      final boolean init_Z;

      Inner_DataUtil(double remaining, float cap, double gcd, double lastStep, double accel, double pitchAcc) {
         this.init_D = Math.abs(remaining);
         this.init_I = remaining > 0.0 ? 1 : (remaining < 0.0 ? -1 : 0);
         this.load = cap;
         this.read = gcd;
         this.write = lastStep + accel;
         this.update = pitchAcc;
         this.init_Z = !Double.isNaN(pitchAcc)
            && this.init_I != 0
            && Math.abs(pitchAcc) <= 89.90000000100001
            && Math.abs(pitchAcc + this.init_I * gcd) > 89.90000000100001;
      }

      boolean init() {
         return this.init_D <= 0.5 || this.init_Z;
      }

      boolean init(int k) {
         double delta = Math.abs(k * this.read);
         if (delta > this.load) {
            return false;
         } else if (delta > this.init_D + this.read + 1.0E-9) {
            return false;
         } else if (delta > this.write + 1.0E-9) {
            return false;
         } else {
            return this.init() || k != 0 && Integer.signum(k) == this.init_I
               ? Double.isNaN(this.update) || !(Math.abs(this.update + k * this.read) > 89.90000000100001)
               : false;
         }
      }
   }

   public enum Inner_StreamUtil {
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

      Inner_StreamUtil(double peakMin, double peakSpan, double easeMin, double easeSpan, double accelMin, double accelSpan) {
         this.init_D = peakMin;
         this.load_D = peakSpan;
         this.read_D = easeMin;
         this.write_D = easeSpan;
         this.update_D = accelMin;
         this.sync_D = accelSpan;
      }
   }

   public static final class Inner_TaskQueue {
      private final Random init_Random;
      private boolean init_Z;
      private double init_D;
      private double load_D;
      private double read_D;
      private double write_D;
      private int init_I;
      private int load_I;
      private ap.Inner_BufferHelper init_ap_BufferHelper;
      private ap.Inner_BufferHelper load_ap_BufferHelper;
      private int read_I;
      private int write_I;
      private float init_F = 0.0F / 0.0F;
      private float load_F = 0.0F / 0.0F;
      private float read_F;
      private double update = 1.0;
      private double sync;
      private double flush;

      public Inner_TaskQueue() {
         this(new Random());
      }

      public Inner_TaskQueue(Random random) {
         this.init_Random = Objects.requireNonNull(random, "random");
      }
   }
}
