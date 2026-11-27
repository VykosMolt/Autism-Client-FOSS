package autismclient.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.phys.Vec3;

public final class StrongholdTriangulator {
   private final List<StrongholdTriangulator.Inner_BufferHelper> samples = new ArrayList<>();
   private StrongholdTriangulator.Inner_DataUtil solution;
   private StrongholdTriangulator.Inner_DataUtil provisional;
   private StrongholdRingEstimator.Inner_StreamUtil ringEstimate;
   private boolean enabled;
   private boolean legacy;

   public List<StrongholdTriangulator.Inner_BufferHelper> getSamples() {
      return List.copyOf(this.samples);
   }

   public StrongholdTriangulator.Inner_BufferHelper lastThrow() {
      return this.samples.isEmpty() ? null : this.samples.getLast();
   }

   public StrongholdTriangulator.Inner_DataUtil estimate() {
      return this.solution != null ? this.solution : this.provisional;
   }

   public StrongholdRingEstimator.Inner_StreamUtil ringEstimate() {
      return this.solution == null ? this.ringEstimate : null;
   }

   public int throwCount() {
      return this.samples.size();
   }

   public void configure(boolean enabled, boolean legacy) {
      if (this.enabled != enabled || this.legacy != legacy) {
         this.enabled = enabled;
         this.legacy = legacy;
         this.ringEstimate = null;
         this.provisional = null;
         this.load();
      }
   }

   private void load() {
      if (this.enabled && this.solution == null) {
         StrongholdRingEstimator.Inner_StreamUtil fresh = StrongholdRingEstimator.init(this.samples, this.legacy);
         if (fresh == null) {
            if (this.provisional != null
               && this.lastThrow() != null
               && init_Z_Vec3_cS_BufferHelper(this.provisional.position, this.lastThrow())) {
               return;
            }

            if (this.samples.size() > 1) {
               StrongholdTriangulator.Inner_BufferHelper last = this.lastThrow();
               fresh = StrongholdRingEstimator.init(last, this.legacy);
               if (fresh != null) {
                  this.samples.clear();
                  this.samples.add(last);
               }
            }
         }

         this.ringEstimate = fresh;
         this.provisional = this.ringEstimate == null
            ? null
            : new StrongholdTriangulator.Inner_DataUtil(this.ringEstimate.center(), this.ringEstimate.uncertainty());
      } else {
         this.ringEstimate = null;
         this.provisional = null;
      }
   }

   public void reset() {
      this.samples.clear();
      this.solution = this.provisional = null;
      this.ringEstimate = null;
   }

   public boolean addThrow(Vec3 origin, Vec3 travel) {
      if (isValidPoint(origin) && isValidPoint(travel)) {
         double length = travel.horizontalDistance();
         if (length < 4.0) {
            return false;
         }

         StrongholdTriangulator.Inner_BufferHelper ray = new StrongholdTriangulator.Inner_BufferHelper(origin, new Vec3(travel.x / length, 0.0, travel.z / length));
         StrongholdTriangulator.Inner_DataUtil previous = this.solution;
         if (previous != null && !init_Z_Vec3_cS_BufferHelper(previous.position, ray)) {
            this.samples.clear();
            this.solution = null;
            previous = null;
         }

         if (!this.samples.isEmpty() && horizontalDistance(origin, this.samples.getLast().init) < 8.0) {
            this.samples.removeLast();
         }

         this.samples.add(ray);
         if (this.samples.size() > 8) {
            this.samples.removeFirst();
         }

         this.solution = triangulate(this.samples);
         if (this.solution == null && previous != null) {
            boolean agrees = true;

            for (StrongholdTriangulator.Inner_BufferHelper r : this.samples) {
               if (!init_Z_Vec3_cS_BufferHelper(previous.position, r)) {
                  agrees = false;
                  break;
               }
            }

            if (agrees) {
               this.solution = previous;
            }
         }

         if (this.solution == null && this.samples.size() >= 3) {
            List<StrongholdTriangulator.Inner_BufferHelper> fresh = List.of(this.samples.get(this.samples.size() - 2), this.samples.getLast());
            if (triangulate(fresh) != null) {
               this.samples.clear();
               this.samples.add(ray);
            }
         }

         this.load();
         return true;
      } else {
         return false;
      }
   }

   private static StrongholdTriangulator.Inner_DataUtil triangulate(List<StrongholdTriangulator.Inner_BufferHelper> rays) {
      if (rays.size() < 2) {
         return null;
      }

      StrongholdTriangulator.Inner_DataUtil best = null;

      for (int i = 0; i < rays.size() - 1; i++) {
         for (int j = i + 1; j < rays.size(); j++) {
            StrongholdTriangulator.Inner_BufferHelper a = rays.get(i);
            StrongholdTriangulator.Inner_BufferHelper b = rays.get(j);
            double cross = load(a.load, b.load);
            if (!(horizontalDistance(a.init, b.init) < 32.0) && !(Math.abs(cross) < 0.04)) {
               Vec3 separation = b.init.subtract(a.init);
               double t = load(separation, b.load) / cross;
               double u = load(separation, a.load) / cross;
               if (!(t < 0.0) && !(u < 0.0) && !(t > 25000.0) && !(u > 25000.0)) {
                  Vec3 point = a.init.add(a.load.scale(t));
                  double uncertainty = Math.max(16.0, (t + u) * 0.003 / Math.abs(cross));
                  if (!(uncertainty > 512.0)) {
                     boolean consistent = true;

                     for (StrongholdTriangulator.Inner_BufferHelper r : rays) {
                        if (!init_Z_Vec3_cS_BufferHelper(point, r)) {
                           consistent = false;
                           break;
                        }
                     }

                     if (consistent && (best == null || uncertainty < best.uncertainty)) {
                        best = new StrongholdTriangulator.Inner_DataUtil(point, uncertainty);
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   private static boolean init_Z_Vec3_cS_BufferHelper(Vec3 point, StrongholdTriangulator.Inner_BufferHelper ray) {
      Vec3 delta = point.subtract(ray.init);
      return delta.dot(ray.load) >= -8.0 && Math.abs(load(delta, ray.load)) <= Math.max(8.0, delta.horizontalDistance() * 0.008);
   }

   public Vec3 destinationFor(Vec3 player) {
      StrongholdTriangulator.Inner_DataUtil destination = this.estimate();
      if (destination != null) {
         return new Vec3(destination.position.x, player.y, destination.position.z);
      }

      StrongholdTriangulator.Inner_BufferHelper ray = this.lastThrow();
      if (ray == null) {
         return null;
      }

      double ahead = Math.max(0.0, player.subtract(ray.init).dot(ray.load)) + 128.0;
      return new Vec3(ray.init.x + ray.load.x * ahead, player.y, ray.init.z + ray.load.z * ahead);
   }

   public static double init(Vec3 camera, Vec3 target, float yaw) {
      double degrees = Math.toDegrees(Math.atan2(-(target.x - camera.x), target.z - camera.z)) - yaw;
      double relative = Math.IEEEremainder(degrees, 360.0);
      return relative == 0.0 ? 0.0 : relative;
   }

   public static Vec3 init_Vec3_Collection(Collection<StrongholdTriangulator.Inner_CachePool> frames) {
      List<Vec3> centers = portalCentersFromFrames(frames);
      return centers.isEmpty() ? null : centers.getFirst();
   }

   public static List<Vec3> portalCentersFromFrames(Collection<StrongholdTriangulator.Inner_CachePool> frames) {
      Map<BlockPos, Direction> byPos = new HashMap<>();
      Set<BlockPos> candidates = new HashSet<>();

      for (StrongholdTriangulator.Inner_CachePool f : frames) {
         if (!f.facing.getAxis().isVertical()) {
            byPos.put(f.pos, f.facing);
            BlockPos middle = f.pos.relative(f.facing, 2);
            Direction sideways = f.facing.getClockWise();

            for (int offset = -1; offset <= 1; offset++) {
               candidates.add(middle.relative(sideways, offset));
            }
         }
      }

      List<Vec3> centers = new ArrayList<>();

      for (BlockPos center : candidates) {
         boolean complete = true;

         for (Direction outward : Plane.HORIZONTAL) {
            for (int offset = -1; offset <= 1; offset++) {
               BlockPos p = center.relative(outward, 2).relative(outward.getClockWise(), offset);
               if (byPos.get(p) != outward.getOpposite()) {
                  complete = false;
                  break;
               }
            }
         }

         if (complete) {
            centers.add(Vec3.atCenterOf(center));
         }
      }

      centers.sort(Comparator.<Vec3>comparingDouble(px -> px.x).thenComparingDouble(px -> px.y).thenComparingDouble(px -> px.z));
      return List.copyOf(centers);
   }

   private static double load(Vec3 a, Vec3 b) {
      return a.x * b.z - a.z * b.x;
   }

   public static double horizontalDistance(Vec3 a, Vec3 b) {
      return Math.hypot(a.x - b.x, a.z - b.z);
   }

   public static boolean isValidPoint(Vec3 p) {
      return p != null && Double.isFinite(p.x) && Double.isFinite(p.y) && Double.isFinite(p.z) && Math.abs(p.x) <= 3.0E7 && Math.abs(p.z) <= 3.0E7;
   }

   public record Inner_BufferHelper(Vec3 init, Vec3 load) {
   }

   public record Inner_CachePool(BlockPos pos, Direction facing) {
   }

   public record Inner_DataUtil(Vec3 position, double uncertainty) {
   }

   public static final class Inner_StreamUtil {
      private final Vec3 origin;
      private Vec3 last;
      private Vec3 read_Vec3;
      private double sumX;
      private double sumZ;
      private double sumXX;
      private double write;
      private double update;
      private int count;
      private boolean init_Z;
      private boolean load_Z;

      public Inner_StreamUtil(Vec3 origin) {
         this.origin = origin;
         this.last = origin;
         this.init_Z = !StrongholdTriangulator.isValidPoint(origin);
      }

      public StrongholdTriangulator.Inner_BufferHelper init(Vec3 at, int age) {
         if (!this.init_Z && !this.load_Z && age <= 60 && StrongholdTriangulator.isValidPoint(at)) {
            if (at.distanceToSqr(this.last) > 9.0) {
               this.init_Z = true;
               return null;
            }

            Vec3 travel = at.subtract(this.origin);
            double length = travel.horizontalDistance();
            if (!(length > 16.0) && !(length + 0.2 < this.last.subtract(this.origin).horizontalDistance())) {
               if (length >= 0.5) {
                  Vec3 direction = new Vec3(travel.x / length, 0.0, travel.z / length);
                  if (this.read_Vec3 == null) {
                     this.read_Vec3 = direction;
                  } else if (this.read_Vec3.dot(direction) < 0.9998) {
                     this.init_Z = true;
                     return null;
                  }
               }

               this.last = at;
               if (length < 0.1) {
                  return null;
               }

               this.count++;
               this.sumX = this.sumX + travel.x;
               this.sumZ = this.sumZ + travel.z;
               this.sumXX = this.sumXX + travel.x * travel.x;
               this.write = this.write + travel.z * travel.z;
               this.update = this.update + travel.x * travel.z;
               if (!(length < 8.0) && this.count >= 6) {
                  double xx = this.sumXX - this.sumX * this.sumX / this.count;
                  double zz = this.write - this.sumZ * this.sumZ / this.count;
                  double xz = this.update - this.sumX * this.sumZ / this.count;
                  double angle = 0.5 * Math.atan2(2.0 * xz, xx - zz);
                  double x = Math.cos(angle);
                  double z = Math.sin(angle);
                  if (x * this.sumX + z * this.sumZ < 0.0) {
                     x = -x;
                     z = -z;
                  }

                  this.load_Z = true;
                  return new StrongholdTriangulator.Inner_BufferHelper(this.origin, new Vec3(x, 0.0, z));
               } else {
                  return null;
               }
            } else {
               this.init_Z = true;
               return null;
            }
         } else {
            return null;
         }
      }

      public boolean init() {
         return this.init_Z || this.load_Z;
      }
   }
}
