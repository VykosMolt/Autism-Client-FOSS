package autismclient.util;

import autismclient.AutismClientAddon;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Random;

public record HumanHandProfile(
   double fittsIntercept,
   double fittsSlope,
   double firstPhaseWeight,
   double firstPhaseJitter,
   double lateralJitter,
   double curvature,
   double curvatureJitter,
   double strokeEase,
   double open,
   double pursuitGain,
   double push,
   double minSettleTolerance,
   int minPauseTicks,
   int maxPauseTicks,
   double tick,
   double driftNoise,
   double driftDecay,
   double pitchGainScale
) {
   private static final String SEED_FILE = "hand.seed";
   private static volatile HumanHandProfile instance;

   public static HumanHandProfile shared() {
      HumanHandProfile hand = instance;
      if (hand == null) {
         synchronized (HumanHandProfile.class) {
            hand = instance;
            if (hand == null) {
               hand = generate(loadSeed());
               instance = hand;
            }
         }
      }

      return hand;
   }

   public static HumanHandProfile generate(long seed) {
      Random r = new Random(seed);
      double curvatureMagnitude = 0.01 + 0.03 * r.nextDouble();
      return new HumanHandProfile(
         60.0 + 70.0 * r.nextDouble(),
         70.0 + 50.0 * r.nextDouble(),
         0.9 + 0.07 * r.nextDouble(),
         0.03 + 0.04 * r.nextDouble(),
         0.02 + 0.03 * r.nextDouble(),
         r.nextBoolean() ? curvatureMagnitude : -curvatureMagnitude,
         0.01 + 0.015 * r.nextDouble(),
         0.82 + 0.16 * r.nextDouble(),
         0.06 + 0.06 * r.nextDouble(),
         0.78 + 0.17 * r.nextDouble(),
         0.1 + 0.12 * r.nextDouble(),
         0.9 + 0.9 * r.nextDouble(),
         1,
         2 + r.nextInt(2),
         0.015 + 0.035 * r.nextDouble(),
         0.008 + 0.017 * r.nextDouble(),
         0.06 + 0.06 * r.nextDouble(),
         0.85 + 0.15 * r.nextDouble()
      );
   }

   public HumanHandProfile forSession(Random session) {
      double tempo = 1.0 + 0.03 * clampedGaussian(session);
      return new HumanHandProfile(
         this.fittsIntercept * tempo,
         this.fittsSlope * tempo,
         this.firstPhaseWeight,
         this.firstPhaseJitter,
         this.lateralJitter,
         this.curvature,
         this.curvatureJitter,
         this.strokeEase,
         this.open,
         this.pursuitGain,
         this.push,
         this.minSettleTolerance,
         this.minPauseTicks,
         this.maxPauseTicks,
         this.tick * (1.0 + 0.05 * clampedGaussian(session)),
         this.driftNoise,
         this.driftDecay,
         this.pitchGainScale
      );
   }

   private static double clampedGaussian(Random random) {
      return Math.max(-2.5, Math.min(2.5, random.nextGaussian()));
   }

   private static long loadSeed() {
      File file = new File(AutismClientAddon.FOLDER, "hand.seed");

      try {
         if (file.isFile()) {
            return Long.parseLong(Files.readString(file.toPath(), StandardCharsets.UTF_8).trim());
         }
      } catch (Exception var5) {
      }

      long seed = new SecureRandom().nextLong();

      try {
         File parent = file.getParentFile();
         if (parent != null) {
            parent.mkdirs();
         }

         Files.writeString(file.toPath(), Long.toString(seed), StandardCharsets.UTF_8);
      } catch (Exception var4) {
      }

      return seed;
   }
}
