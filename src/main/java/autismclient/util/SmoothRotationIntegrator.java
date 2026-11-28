package autismclient.util;

public final class SmoothRotationIntegrator {
   private static final double init_D = 1.0E-8;
   private static final double load_D = 0.65;
   private double yawVelocity;
   private double pitchVelocity;
   private double yawWander;
   private double pitchWander;
   private double wobblePhase;
   private long lastYawCounts;
   private long lastPitchCounts;
   private long rngState = 7640891576956012809L;
   private boolean hasLastCounts;
   private boolean preferYawNext;

   public void resetDefaultSeed() {
      this.reseed(7640891576956012809L);
   }

   public void reseed(long seed) {
      this.rngState = mixSeed(seed);
      this.reset();
      this.wobblePhase = this.nextRandom() * 3.141592653589793 * 2.0;
   }

   public void reset() {
      this.yawVelocity = 0.0;
      this.pitchVelocity = 0.0;
      this.yawWander = 0.0;
      this.pitchWander = 0.0;
      this.lastYawCounts = 0L;
      this.lastPitchCounts = 0L;
      this.hasLastCounts = false;
      this.preferYawNext = false;
   }

   public SmoothRotationIntegrator.CountStep stepBySpeed(
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
      return this.integrate(
         yawErrorDegrees,
         pitchErrorDegrees,
         degreesPerCount,
         speedLimitFor(horizontalSpeed),
         speedLimitFor(verticalSpeed),
         1.0,
         directionChange,
         midpoint,
         allowYaw,
         allowPitch
      );
   }

   public SmoothRotationIntegrator.CountStep stepByDegrees(
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
         ? this.integrate(
            yawErrorDegrees,
            pitchErrorDegrees,
            degreesPerCount,
            countsPerTick(horizontalDegreesPerTick, degreesPerCount),
            countsPerTick(verticalDegreesPerTick, degreesPerCount),
            1.3,
            directionChange,
            midpoint,
            allowYaw,
            allowPitch
         )
         : this.idleResult();
   }

   public SmoothRotationIntegrator.CountStep step(
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
         return this.integrate(
            yawErrorDegrees,
            pitchErrorDegrees,
            degreesPerCount,
            countsPerTick(horizontalDegreesPerTick, degreesPerCount),
            countsPerTick(verticalDegreesPerTick, degreesPerCount),
            Math.max(1.3, acceleration / cap),
            directionChange,
            midpoint,
            allowYaw,
            allowPitch
         );
      } else {
         return this.idleResult();
      }
   }

   private SmoothRotationIntegrator.CountStep integrate(
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
            this.yawVelocity = 0.0;
         }

         if (!allowPitch) {
            this.pitchVelocity = 0.0;
         }

         double direction = clamp(directionChange, 0.0, 1.0);
         double curvePoint = clamp(midpoint, 0.0, 1.0);
         this.yawVelocity = this.updateVelocity(this.yawVelocity, yawError, horizontalLimit, direction, curvePoint, responseScale);
         this.pitchVelocity = this.updateVelocity(this.pitchVelocity, pitchError, verticalLimit, direction, curvePoint, responseScale);
         this.yawWander = this.yawWander * 0.72 + this.signedRandom() * 0.28;
         this.pitchWander = this.pitchWander * 0.67 + this.signedRandom() * 0.33;
         this.wobblePhase = this.wobblePhase + (0.51 + this.nextRandom() * 0.23);
         double distance = Math.hypot(yawError, pitchError);
         double yawCandidate = this.yawVelocity;
         double pitchCandidate = this.pitchVelocity;
         if (allowYaw && allowPitch && distance >= 7.0) {
            double curvature = Math.sin(this.wobblePhase) * Math.min(1.15, 0.32 + Math.sqrt(distance) * 0.045) * (0.55 + direction * 0.45);
            yawCandidate += -pitchError / distance * curvature;
            pitchCandidate += yawError / distance * curvature;
         }

         double noiseScale = distance >= 5.0 ? 0.72 + direction * 0.28 : 0.0;
         yawCandidate += this.yawWander * noiseScale;
         pitchCandidate += this.pitchWander * noiseScale * 0.83;
         long yawCounts = toCounts(yawCandidate, yawError);
         long pitchCounts = toCounts(pitchCandidate, pitchError);
         SmoothRotationIntegrator.CountStep varied = this.varyCounts(yawCounts, pitchCounts, yawError, pitchError);
         this.lastYawCounts = varied.init;
         this.lastPitchCounts = varied.load;
         this.hasLastCounts = true;
         return varied;
      } else {
         return this.idleResult();
      }
   }

   private SmoothRotationIntegrator.CountStep idleResult() {
      this.yawVelocity = 0.0;
      this.pitchVelocity = 0.0;
      this.lastYawCounts = 0L;
      this.lastPitchCounts = 0L;
      this.hasLastCounts = false;
      return new SmoothRotationIntegrator.CountStep(0L, 0L);
   }

   private double updateVelocity(double velocity, long error, double speedLimit, double directionChange, double midpoint, double responseScale) {
      if (error == 0L) {
         return approach(velocity, 0.0, Math.max(0.65, speedLimit * 0.28));
      }

      double acceleration = Math.max(0.65, speedLimit * (0.14 + (1.0 - midpoint) * 0.1 + directionChange * 0.06)) * Math.max(1.0, responseScale);
      double brakingAcceleration = acceleration * (0.72 + midpoint * 0.75);
      double brakingSpeed = Math.sqrt(2.0 * brakingAcceleration * Math.abs((double)error));
      double desired = Math.copySign(Math.min(speedLimit, brakingSpeed), error);
      return approach(velocity, desired, acceleration);
   }

   private SmoothRotationIntegrator.CountStep varyCounts(long yaw, long pitch, long yawError, long pitchError) {
      if (this.hasLastCounts && (yaw != 0L || pitch != 0L) && yaw == this.lastYawCounts && pitch == this.lastPitchCounts) {
         boolean preferYaw = Math.abs(yawError) - Math.abs(yaw) >= Math.abs(pitchError) - Math.abs(pitch);
         long variedYaw = yaw;
         long variedPitch = pitch;
         if (preferYaw && this.canVary(yaw, yawError)) {
            variedYaw = this.variedCount(yaw, yawError);
         } else if (this.canVary(pitch, pitchError)) {
            variedPitch = this.variedCount(pitch, pitchError);
         } else if (this.canVary(yaw, yawError)) {
            variedYaw = this.variedCount(yaw, yawError);
         }

         this.preferYawNext = !this.preferYawNext;
         return new SmoothRotationIntegrator.CountStep(variedYaw, variedPitch);
      } else {
         return new SmoothRotationIntegrator.CountStep(yaw, pitch);
      }
   }

   private boolean canVary(long count, long error) {
      return count != 0L && Math.abs(error) > Math.abs(count) + 1L;
   }

   private long variedCount(long count, long error) {
      long direction = Long.signum(error);
      long candidate = count + (this.preferYawNext ? direction : -direction);
      if (candidate == count || Long.signum(candidate) != direction || Math.abs(candidate) > Math.abs(error)) {
         candidate = count + direction;
      }

      return candidate;
   }

   private static long toCounts(double candidate, long error) {
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

   private static double speedLimitFor(float configuredSpeed) {
      return 2.0 + 68.0 * clamp(configuredSpeed, 0.01, 1.0);
   }

   private static double countsPerTick(float degreesPerTick, double degreesPerCount) {
      double degrees = Double.isFinite(degreesPerTick) ? Math.max(0.01, degreesPerTick) : 0.01;
      return Math.max(2.0, degrees / degreesPerCount);
   }

   private static double approach(double current, double target, double amount) {
      return current < target ? Math.min(target, current + amount) : Math.max(target, current - amount);
   }

   private double signedRandom() {
      return this.nextRandom() * 2.0 - 1.0;
   }

   private double nextRandom() {
      long x = this.rngState;
      x ^= x << 13;
      x ^= x >>> 7;
      x ^= x << 17;
      this.rngState = x;
      return (x >>> 11) * 1.1102230246251565E-16;
   }

   private static long mixSeed(long seed) {
      long value = seed == 0L ? -7046029254386353131L : seed;
      value ^= value >>> 30;
      value *= -4658895280553007687L;
      value ^= value >>> 27;
      value *= -7723592293110705685L;
      value ^= value >>> 31;
      return value == 0L ? -3335678366873096957L : value;
   }

   private static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   public record CountStep(long init, long load) {
      public RotationMath.Rotation init(double degreesPerCount) {
         return Double.isFinite(degreesPerCount) && !(degreesPerCount <= 0.0)
            ? new RotationMath.Rotation((float)(this.init * degreesPerCount), (float)(this.load * degreesPerCount))
            : new RotationMath.Rotation(0.0F, 0.0F);
      }
   }
}
