package autismclient.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.phys.Vec3;

public final class StrongholdRingEstimator {
   public static final double init_D = 2.5E-5;
   private static final double load = 181.0;
   private static final double RING_HALF_WIDTH = 640.0;
   private static final double SEARCH_BOUND = 24373.0;
   private static final int[] RING_STRONGHOLDS = new int[]{3, 6, 10, 15, 21, 28, 36, 9};

   private StrongholdRingEstimator() {
   }

   public static StrongholdRingEstimator.RingEstimate estimateOne(StrongholdTriangulator.ThrowSample ray, boolean legacyTargets) {
      return ray == null ? null : estimate(List.of(ray), legacyTargets);
   }

   public static StrongholdRingEstimator.RingEstimate estimate(List<StrongholdTriangulator.ThrowSample> observations, boolean legacyTargets) {
      if (observations != null && !observations.isEmpty() && observations.size() <= 8) {
         StrongholdTriangulator.ThrowSample ray = observations.getLast();
         if (ray != null
            && StrongholdTriangulator.isValidPoint(ray.init())
            && StrongholdTriangulator.isValidPoint(ray.load())
            && !(ray.init().horizontalDistance() > 40000.0)
            && !(ray.load().horizontalDistance() < 0.99)) {
            Vec3 origin = ray.init();
            Vec3 direction = new Vec3(ray.load().x, 0.0, ray.load().z).normalize();
            boolean alongX = Math.abs(direction.x) >= Math.abs(direction.z);
            double primary = alongX ? direction.x : direction.z;
            double secondary = alongX ? direction.z : direction.x;
            double fromOrigin = origin.horizontalDistance();
            double nearestBound = Math.sqrt(
                  Math.max(fromOrigin * fromOrigin + 1982464.0 - fromOrigin * 1408.0, fromOrigin * fromOrigin + 7225344.0 - fromOrigin * 2688.0)
               )
               + 181.0
               + 16.0;
            double originPrimary = alongX ? origin.x : origin.z;
            int offset = legacyTargets ? 8 : 0;
            int first = (int)Math.ceil((Math.max(-24373.0, originPrimary - nearestBound) - offset) / 16.0);
            int last = (int)Math.floor((Math.min(24373.0, originPrimary + nearestBound) - offset) / 16.0);
            double[][] distances = sampleRingDistances(origin);
            List<StrongholdRingEstimator.WeightedCandidate> candidates = new ArrayList<>();
            double maxLog = -1.0 / 0.0;

            for (int chunk = first; chunk <= last; chunk++) {
               double a = chunk * 16.0 + offset;
               double t = (a - (alongX ? origin.x : origin.z)) / primary;
               if (!(t <= 12.0)) {
                  double center = (alongX ? origin.z : origin.x) + secondary * t;
                  double band = (t * 2.5E-5 * 4.0 + 0.04) / Math.abs(primary);
                  int low = (int)Math.ceil((center - band - offset) / 16.0);
                  int high = (int)Math.floor((center + band - offset) / 16.0);

                  for (int other = low; other <= high; other++) {
                     double b = other * 16.0 + offset;
                     double x = alongX ? a : b;
                     double z = alongX ? b : a;
                     double radius = Math.hypot(x, z);
                     int ring = ringFor(radius);
                     if (ring >= 0) {
                        double dx = x - origin.x;
                        double dz = z - origin.z;
                        double forward = dx * direction.x + dz * direction.z;
                        if (!(forward <= 12.0)) {
                           double angle = Math.atan2(dx * direction.z - dz * direction.x, forward);
                           double error = angle / 2.5E-5;
                           if (!(Math.abs(error) > 4.0)) {
                              double distance = Math.hypot(dx, dz);
                              if (!(distance > nearestBound)) {
                                 double prior = ringPrior(radius, ring);
                                 double likelihood = 0.0;
                                 boolean consistent = true;

                                 for (StrongholdTriangulator.ThrowSample observation : observations) {
                                    if (observation != null && StrongholdTriangulator.isValidPoint(observation.init()) && StrongholdTriangulator.isValidPoint(observation.load())) {
                                       double ox = x - observation.init().x;
                                       double oz = z - observation.init().z;
                                       double ahead = ox * observation.load().x + oz * observation.load().z;
                                       double angularError = Math.atan2(ox * observation.load().z - oz * observation.load().x, ahead) / 2.5E-5;
                                       if (!(ahead <= 12.0) && !(Math.abs(angularError) > 4.0)) {
                                          likelihood -= 0.5 * angularError * angularError;
                                          continue;
                                       }

                                       consistent = false;
                                       break;
                                    }

                                    consistent = false;
                                    break;
                                 }

                                 if (consistent) {
                                    double weight = Math.log(prior) + likelihood + otherRingsLogLikelihood(distances, distance, ring);
                                    candidates.add(new StrongholdRingEstimator.WeightedCandidate(new Vec3(x, 0.0, z), weight));
                                    maxLog = Math.max(maxLog, weight);
                                    if (candidates.size() > 8192) {
                                       return null;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (candidates.isEmpty()) {
               return null;
            }

            candidates.sort(
               Comparator.comparingDouble(StrongholdRingEstimator.WeightedCandidate::init_D)
                  .reversed()
                  .thenComparingDouble(cx -> cx.init_Vec3.x)
                  .thenComparingDouble(cx -> cx.init_Vec3.z)
            );
            double total = 0.0;

            for (StrongholdRingEstimator.WeightedCandidate c : candidates) {
               total += Math.exp(c.init_D - maxLog);
            }

            if (Double.isFinite(total) && !(total <= 0.0)) {
               StrongholdRingEstimator.WeightedCandidate best = candidates.getFirst();
               double mass = 0.0;
               double uncertainty = 32.0;
               List<StrongholdRingEstimator.RankedCandidate> top = new ArrayList<>();

               for (StrongholdRingEstimator.WeightedCandidate c : candidates) {
                  double probability = Math.exp(c.init_D - maxLog) / total;
                  if (top.size() < 5) {
                     top.add(new StrongholdRingEstimator.RankedCandidate(c.init_Vec3, probability));
                  }

                  if (mass < 0.999) {
                     uncertainty = Math.max(uncertainty, StrongholdTriangulator.horizontalDistance(best.init_Vec3, c.init_Vec3) + 16.0);
                  }

                  mass += probability;
               }

               return new StrongholdRingEstimator.RingEstimate(best.init_Vec3, uncertainty, Math.exp(best.init_D - maxLog) / total, candidates.size(), List.copyOf(top));
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static int ringFor(double radius) {
      for (int i = 0; i < RING_STRONGHOLDS.length; i++) {
         if (Math.abs(radius - (2048 + 3072 * i)) <= 821.0) {
            return i;
         }
      }

      return -1;
   }

   private static double ringPrior(double radius, int ring) {
      double edge = Math.abs(radius - (2048 + 3072 * ring)) - 640.0;
      double snapWeight = edge <= 0.0 ? 1.0 : Math.max(1.0E-4, 1.0 - edge / 181.0);
      return RING_STRONGHOLDS[ring] * snapWeight / Math.max(1.0, radius);
   }

   private static double[][] sampleRingDistances(Vec3 origin) {
      double[][] samples = new double[RING_STRONGHOLDS.length][1024];

      for (int ring = 0; ring < RING_STRONGHOLDS.length; ring++) {
         int at = 0;

         for (int r = 0; r < 16; r++) {
            for (int a = 0; a < 64; a++) {
               double radius = 2048 + 3072 * ring - 640.0 + (r + 0.5) * 640.0 * 2.0 / 16.0;
               double angle = (a + 0.5) * 3.141592653589793 * 2.0 / 64.0;
               samples[ring][at++] = Math.hypot(radius * Math.cos(angle) - origin.x, radius * Math.sin(angle) - origin.z);
            }
         }

         Arrays.sort(samples[ring]);
      }

      return samples;
   }

   private static double otherRingsLogLikelihood(double[][] samples, double distance, int candidateRing) {
      double log = 0.0;

      for (int ring = 0; ring < RING_STRONGHOLDS.length; ring++) {
         double[] sorted = samples[ring];
         int low = 0;
         int high = sorted.length;
         double threshold = distance - 181.0;

         while (low < high) {
            int mid = low + high >>> 1;
            if (sorted[mid] < threshold) {
               low = mid + 1;
            } else {
               high = mid;
            }
         }

         double closer = (double)low / sorted.length;
         log += (RING_STRONGHOLDS[ring] - (ring == candidateRing ? 1 : 0)) * Math.log(Math.max(1.0E-4, 1.0 - closer));
      }

      return log;
   }

   private record WeightedCandidate(Vec3 init_Vec3, double init_D) {
   }

   public record RankedCandidate(Vec3 init_Vec3, double init_D) {
   }

   public record RingEstimate(Vec3 center, double uncertainty, double load, int init_I, List<StrongholdRingEstimator.RankedCandidate> init_List) {
   }
}
