package autismclient.util;

public final class SmoothRotationIntegrator {
   private static final double init_D = 1.0E-8;
   private static final double load_D = 0.65;
   private double read_D;
   private double write;
   private double update;
   private double sync;
   private double flush;
   private long init_J;
   private long load_J;
   private long read_J = 7640891576956012809L;
   private boolean init_Z;
   private boolean load_Z;

   public void init_V() {
      this.init_V_J(7640891576956012809L);
   }

   public void init_V_J(long seed) {
      this.read_J = init_J_J(seed);
      this.load_V();
      this.flush = this.load_D() * 3.141592653589793 * 2.0;
   }

   public void load_V() {
      this.read_D = 0.0;
      this.write = 0.0;
      this.update = 0.0;
      this.sync = 0.0;
      this.init_J = 0L;
      this.load_J = 0L;
      this.init_Z = false;
      this.load_Z = false;
   }

   public SmoothRotationIntegrator.Inner_DataUtil init(
      float yawErrorDegrees,
      float pitchErrorDegrees,
      double degreesPerCount,
      float horizontalSpeed,
      float verticalSpeed,
      float directionChange,
      float midpoint,
      boolean allowYaw,
      boolean allowPitch
   ) {
      return this.init_dg_DataUtil_F(
         yawErrorDegrees,
         pitchErrorDegrees,
         degreesPerCount,
         init_D_F(horizontalSpeed),
         init_D_F(verticalSpeed),
         1.0,
         directionChange,
         midpoint,
         allowYaw,
         allowPitch
      );
   }

   public SmoothRotationIntegrator.Inner_DataUtil load(
      float yawErrorDegrees,
      float pitchErrorDegrees,
      double degreesPerCount,
      float horizontalDegreesPerTick,
      float verticalDegreesPerTick,
      float directionChange,
      float midpoint,
      boolean allowYaw,
      boolean allowPitch
   ) {
      return Double.isFinite(degreesPerCount) && !(degreesPerCount <= 1.0E-8)
         ? this.init_dg_DataUtil_F(
            yawErrorDegrees,
            pitchErrorDegrees,
            degreesPerCount,
            init_D_F_D(horizontalDegreesPerTick, degreesPerCount),
            init_D_F_D(verticalDegreesPerTick, degreesPerCount),
            1.3,
            directionChange,
            midpoint,
            allowYaw,
            allowPitch
         )
         : this.init_dg_DataUtil();
   }

   public SmoothRotationIntegrator.Inner_DataUtil init_dg_DataUtil_F_2(
      float yawErrorDegrees,
      float pitchErrorDegrees,
      double degreesPerCount,
      float horizontalDegreesPerTick,
      float verticalDegreesPerTick,
      float accelerationDegreesPerTick,
      float directionChange,
      float midpoint,
      boolean allowYaw,
      boolean allowPitch
   ) {
      if (Double.isFinite(degreesPerCount) && !(degreesPerCount <= 1.0E-8)) {
         double cap = Math.max(Math.max(0.01, horizontalDegreesPerTick), Math.max(0.01, verticalDegreesPerTick));
         double acceleration = Double.isFinite(accelerationDegreesPerTick) ? Math.max(cap, accelerationDegreesPerTick) : cap;
         return this.init_dg_DataUtil_F(
            yawErrorDegrees,
            pitchErrorDegrees,
            degreesPerCount,
            init_D_F_D(horizontalDegreesPerTick, degreesPerCount),
            init_D_F_D(verticalDegreesPerTick, degreesPerCount),
            Math.max(1.3, acceleration / cap),
            directionChange,
            midpoint,
            allowYaw,
            allowPitch
         );
      } else {
         return this.init_dg_DataUtil();
      }
   }

   private SmoothRotationIntegrator.Inner_DataUtil init_dg_DataUtil_F(
      float yawErrorDegrees,
      float pitchErrorDegrees,
      double degreesPerCount,
      double horizontalLimit,
      double verticalLimit,
      double responseScale,
      float directionChange,
      float midpoint,
      boolean allowYaw,
      boolean allowPitch
   ) {
      if (Double.isFinite(degreesPerCount) && !(degreesPerCount <= 1.0E-8)) {
         long yawError = allowYaw ? Math.round(yawErrorDegrees / degreesPerCount) : 0L;
         long pitchError = allowPitch ? Math.round(pitchErrorDegrees / degreesPerCount) : 0L;
         if (!allowYaw) {
            this.read_D = 0.0;
         }

         if (!allowPitch) {
            this.write = 0.0;
         }

         double direction = load(directionChange, 0.0, 1.0);
         double curvePoint = load(midpoint, 0.0, 1.0);
         this.read_D = this.init(this.read_D, yawError, horizontalLimit, direction, curvePoint, responseScale);
         this.write = this.init(this.write, pitchError, verticalLimit, direction, curvePoint, responseScale);
         this.update = this.update * 0.72 + this.init_D() * 0.28;
         this.sync = this.sync * 0.67 + this.init_D() * 0.33;
         this.flush = this.flush + (0.51 + this.load_D() * 0.23);
         double distance = Math.hypot(yawError, pitchError);
         double yawCandidate = this.read_D;
         double pitchCandidate = this.write;
         if (allowYaw && allowPitch && distance >= 7.0) {
            double curvature = Math.sin(this.flush) * Math.min(1.15, 0.32 + Math.sqrt(distance) * 0.045) * (0.55 + direction * 0.45);
            yawCandidate += -pitchError / distance * curvature;
            pitchCandidate += yawError / distance * curvature;
         }

         double noiseScale = distance >= 5.0 ? 0.72 + direction * 0.28 : 0.0;
         yawCandidate += this.update * noiseScale;
         pitchCandidate += this.sync * noiseScale * 0.83;
         long yawCounts = init_J_D_J(yawCandidate, yawError);
         long pitchCounts = init_J_D_J(pitchCandidate, pitchError);
         SmoothRotationIntegrator.Inner_DataUtil varied = this.init(yawCounts, pitchCounts, yawError, pitchError);
         this.init_J = varied.init;
         this.load_J = varied.load;
         this.init_Z = true;
         return varied;
      } else {
         return this.init_dg_DataUtil();
      }
   }

   private SmoothRotationIntegrator.Inner_DataUtil init_dg_DataUtil() {
      this.read_D = 0.0;
      this.write = 0.0;
      this.init_J = 0L;
      this.load_J = 0L;
      this.init_Z = false;
      return new SmoothRotationIntegrator.Inner_DataUtil(0L, 0L);
   }

   private double init(double velocity, long error, double speedLimit, double directionChange, double midpoint, double responseScale) {
      if (error == 0L) {
         return init(velocity, 0.0, Math.max(0.65, speedLimit * 0.28));
      }

      double acceleration = Math.max(0.65, speedLimit * (0.14 + (1.0 - midpoint) * 0.1 + directionChange * 0.06)) * Math.max(1.0, responseScale);
      double brakingAcceleration = acceleration * (0.72 + midpoint * 0.75);
      double brakingSpeed = Math.sqrt(2.0 * brakingAcceleration * Math.abs((double)error));
      double desired = Math.copySign(Math.min(speedLimit, brakingSpeed), error);
      return init(velocity, desired, acceleration);
   }

   private SmoothRotationIntegrator.Inner_DataUtil init(long yaw, long pitch, long yawError, long pitchError) {
      if (this.init_Z && (yaw != 0L || pitch != 0L) && yaw == this.init_J && pitch == this.load_J) {
         boolean preferYaw = Math.abs(yawError) - Math.abs(yaw) >= Math.abs(pitchError) - Math.abs(pitch);
         long variedYaw = yaw;
         long variedPitch = pitch;
         if (preferYaw && this.init_Z_J_J(yaw, yawError)) {
            variedYaw = this.init_J_J_J(yaw, yawError);
         } else if (this.init_Z_J_J(pitch, pitchError)) {
            variedPitch = this.init_J_J_J(pitch, pitchError);
         } else if (this.init_Z_J_J(yaw, yawError)) {
            variedYaw = this.init_J_J_J(yaw, yawError);
         }

         this.load_Z = !this.load_Z;
         return new SmoothRotationIntegrator.Inner_DataUtil(variedYaw, variedPitch);
      } else {
         return new SmoothRotationIntegrator.Inner_DataUtil(yaw, pitch);
      }
   }

   private boolean init_Z_J_J(long count, long error) {
      return count != 0L && Math.abs(error) > Math.abs(count) + 1L;
   }

   private long init_J_J_J(long count, long error) {
      long direction = Long.signum(error);
      long candidate = count + (this.load_Z ? direction : -direction);
      if (candidate == count || Long.signum(candidate) != direction || Math.abs(candidate) > Math.abs(error)) {
         candidate = count + direction;
      }

      return candidate;
   }

   private static long init_J_D_J(double candidate, long error) {
      if (error != 0L && Double.isFinite(candidate)) {
         long rounded = Math.round(candidate);
         long direction = Long.signum(error);
         if (rounded != 0L && Long.signum(rounded) != direction) {
            return 0L;
         }

         long magnitude = Math.min(Math.abs(rounded), Math.abs(error));
         if (magnitude == 0L && Math.abs(candidate) >= 0.35) {
            magnitude = 1L;
         }

         return direction * magnitude;
      } else {
         return 0L;
      }
   }

   private static double init_D_F(float configuredSpeed) {
      return 2.0 + 68.0 * load(configuredSpeed, 0.01, 1.0);
   }

   private static double init_D_F_D(float degreesPerTick, double degreesPerCount) {
      double degrees = Double.isFinite(degreesPerTick) ? Math.max(0.01, degreesPerTick) : 0.01;
      return Math.max(2.0, degrees / degreesPerCount);
   }

   private static double init(double current, double target, double amount) {
      return current < target ? Math.min(target, current + amount) : Math.max(target, current - amount);
   }

   private double init_D() {
      return this.load_D() * 2.0 - 1.0;
   }

   private double load_D() {
      long x = this.read_J;
      x ^= x << 13;
      x ^= x >>> 7;
      x ^= x << 17;
      this.read_J = x;
      return (x >>> 11) * 1.1102230246251565E-16;
   }

   private static long init_J_J(long seed) {
      long value = seed == 0L ? -7046029254386353131L : seed;
      value ^= value >>> 30;
      value *= -4658895280553007687L;
      value ^= value >>> 27;
      value *= -7723592293110705685L;
      value ^= value >>> 31;
      return value == 0L ? -3335678366873096957L : value;
   }

   private static double load(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   public record Inner_DataUtil(long init, long load) {
      public RotationMath.Rotation init(double degreesPerCount) {
         return Double.isFinite(degreesPerCount) && !(degreesPerCount <= 0.0)
            ? new RotationMath.Rotation((float)(this.init * degreesPerCount), (float)(this.load * degreesPerCount))
            : new RotationMath.Rotation(0.0F, 0.0F);
      }
   }
}
