package autismclient.util;

import java.util.Random;

public final class ao {
   private static final Random init = new Random();

   private ao() {
   }

   public static long init_J_D(double targetCps, ao.Inner_StreamUtil profile, long debtNanos) {
      return init(targetCps, profile, debtNanos, new ao.Inner_DataUtil());
   }

   public static long init(double targetCps, ao.Inner_StreamUtil profile, long debtNanos, ao.Inner_DataUtil state) {
      double cps = Math.max(0.1, targetCps);
      long mean = Math.max(1L, Math.round(1.0E9 / cps));
      ao.Inner_DataUtil profileState = state == null ? new ao.Inner_DataUtil() : state;
      double interval;
      if (mean <= 50000000L) {
         interval = mean;
      } else {
         double var10000;
         switch (profile == null ? ao.Inner_StreamUtil.init : profile) {
            case init:
               var10000 = mean;
               break;
            case load:
               var10000 = init(mean, profileState);
               break;
            case read:
               var10000 = load(mean, profileState);
               break;
            default:
               throw new MatchException(null, null);
         }

         interval = var10000;
      }

      double var21;
      switch (profile == null ? ao.Inner_StreamUtil.init : profile) {
         case init:
            var21 = 0.1;
            break;
         case load:
            var21 = 0.6;
            break;
         case read:
            var21 = 0.45;
            break;
         default:
            throw new MatchException(null, null);
      }

      double floorMultiplier = var21;
      long floor = Math.max(1L, Math.round(mean * floorMultiplier));
      long ceiling = Math.max(floor, Math.round(mean * 3.0));
      profileState.load = Math.max(1L, Math.round(interval));
      long adjusted = Math.round(interval - debtNanos);
      return Math.max(floor, Math.min(ceiling, adjusted));
   }

   public static long init() {
      double v = 50.0 + init.nextGaussian() * 30.0;
      return (long)Math.max(0.0, Math.min(150.0, v));
   }

   private static double init(long mean, ao.Inner_DataUtil state) {
      init(state, 0.055, 0.11, -0.14, 0.16);
      double micro = init_D_D(0.11 * init.nextGaussian(), -0.25, 0.28);
      double raw = mean * (1.0 + state.init_D + micro);
      return init(mean, state, raw, 0.6, 1.5, 0.28);
   }

   private static double load(long mean, ao.Inner_DataUtil state) {
      init(state, 0.035, 0.13, -0.18, 0.2);
      if (state.init_I > 0) {
         state.init_I--;
      }

      double micro = init_D_D(0.12 * init.nextGaussian(), -0.3, 0.32);
      double raw = mean * (1.0 + state.init_D + micro);
      if (state.init_I <= 0 && init.nextDouble() < init(mean)) {
         double extra = mean * (0.75 + init.nextDouble() * 1.15) + (2.5E7 + init.nextDouble() * 8.5E7);
         raw += extra;
         double cps = 1.0E9 / mean;
         double cooldownSeconds = 4.0 + init.nextDouble() * 8.0;
         state.init_I = Math.max(8, (int)Math.round(cooldownSeconds * cps));
      } else if (state.init_J > mean * 0.75 && init.nextDouble() < 0.28) {
         raw *= 0.72 + init.nextDouble() * 0.16;
      } else if (init.nextDouble() < 0.035) {
         raw *= 0.78 + init.nextDouble() * 0.18;
      }

      return init(mean, state, raw, 0.45, 3.0, 0.34);
   }

   private static double init(long mean) {
      double secondsPerClick = mean / 1.0E9;
      return init_D_D(secondsPerClick / 9.0, 0.0045, 0.024);
   }

   private static void init(ao.Inner_DataUtil state, double rate, double noise, double min, double max) {
      double target = init.nextGaussian() * noise;
      state.init_D = state.init_D + (target - state.init_D) * rate;
      state.init_D = init_D_D(state.init_D, min, max);
   }

   private static double init(long mean, ao.Inner_DataUtil state, double raw, double minFactor, double maxFactor, double maxCorrectionFactor) {
      double min = mean * minFactor;
      double max = mean * maxFactor;
      double maxCorrection = mean * maxCorrectionFactor;
      double correction = init_D_D(state.init_J, -maxCorrection, maxCorrection);
      double balanced = init_D_D(raw - correction, min, max);
      state.init_J = Math.round(init_D_D(state.init_J + balanced - mean, -mean * 8.0, mean * 8.0));
      return balanced;
   }

   private static double init_D_D(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   public static final class Inner_DataUtil {
      private double init_D;
      private long init_J;
      private int init_I;
      private long load;

      public void init_V() {
         this.init_D = 0.0;
         this.init_J = 0L;
         this.init_I = 0;
         this.load = 0L;
      }

      public long init_J() {
         return this.load;
      }
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read;
   }
}
