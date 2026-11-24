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

public final class cS {
   private final List<cS.Inner_BufferHelper> init_List = new ArrayList<>();
   private cS.Inner_DataUtil init_cS_DataUtil;
   private cS.Inner_DataUtil load_cS_DataUtil;
   private cT.Inner_StreamUtil init_cT_StreamUtil;
   private boolean init_Z;
   private boolean load_Z;

   public List<cS.Inner_BufferHelper> init_List() {
      return List.copyOf(this.init_List);
   }

   public cS.Inner_BufferHelper init_cS_BufferHelper() {
      return this.init_List.isEmpty() ? null : this.init_List.getLast();
   }

   public cS.Inner_DataUtil init_cS_DataUtil() {
      return this.init_cS_DataUtil != null ? this.init_cS_DataUtil : this.load_cS_DataUtil;
   }

   public cT.Inner_StreamUtil init_cT_StreamUtil() {
      return this.init_cS_DataUtil == null ? this.init_cT_StreamUtil : null;
   }

   public int init_I() {
      return this.init_List.size();
   }

   public void init_V_Z_Z(boolean enabled, boolean legacy) {
      if (this.init_Z != enabled || this.load_Z != legacy) {
         this.init_Z = enabled;
         this.load_Z = legacy;
         this.init_cT_StreamUtil = null;
         this.load_cS_DataUtil = null;
         this.load();
      }
   }

   private void load() {
      if (this.init_Z && this.init_cS_DataUtil == null) {
         cT.Inner_StreamUtil fresh = cT.init(this.init_List, this.load_Z);
         if (fresh == null) {
            if (this.load_cS_DataUtil != null
               && this.init_cS_BufferHelper() != null
               && init_Z_Vec3_cS_BufferHelper(this.load_cS_DataUtil.init_Vec3, this.init_cS_BufferHelper())) {
               return;
            }

            if (this.init_List.size() > 1) {
               cS.Inner_BufferHelper last = this.init_cS_BufferHelper();
               fresh = cT.init(last, this.load_Z);
               if (fresh != null) {
                  this.init_List.clear();
                  this.init_List.add(last);
               }
            }
         }

         this.init_cT_StreamUtil = fresh;
         this.load_cS_DataUtil = this.init_cT_StreamUtil == null
            ? null
            : new cS.Inner_DataUtil(this.init_cT_StreamUtil.init_Vec3(), this.init_cT_StreamUtil.init_D());
      } else {
         this.init_cT_StreamUtil = null;
         this.load_cS_DataUtil = null;
      }
   }

   public void init_V() {
      this.init_List.clear();
      this.init_cS_DataUtil = this.load_cS_DataUtil = null;
      this.init_cT_StreamUtil = null;
   }

   public boolean init_Z_Vec3_Vec3(Vec3 origin, Vec3 travel) {
      if (init_Z_Vec3(origin) && init_Z_Vec3(travel)) {
         double length = travel.horizontalDistance();
         if (length < 4.0) {
            return false;
         }

         cS.Inner_BufferHelper ray = new cS.Inner_BufferHelper(origin, new Vec3(travel.x / length, 0.0, travel.z / length));
         cS.Inner_DataUtil previous = this.init_cS_DataUtil;
         if (previous != null && !init_Z_Vec3_cS_BufferHelper(previous.init_Vec3, ray)) {
            this.init_List.clear();
            this.init_cS_DataUtil = null;
            previous = null;
         }

         if (!this.init_List.isEmpty() && init_D_Vec3_Vec3(origin, this.init_List.getLast().init) < 8.0) {
            this.init_List.removeLast();
         }

         this.init_List.add(ray);
         if (this.init_List.size() > 8) {
            this.init_List.removeFirst();
         }

         this.init_cS_DataUtil = init_cS_DataUtil_List(this.init_List);
         if (this.init_cS_DataUtil == null && previous != null) {
            boolean agrees = true;

            for (cS.Inner_BufferHelper r : this.init_List) {
               if (!init_Z_Vec3_cS_BufferHelper(previous.init_Vec3, r)) {
                  agrees = false;
                  break;
               }
            }

            if (agrees) {
               this.init_cS_DataUtil = previous;
            }
         }

         if (this.init_cS_DataUtil == null && this.init_List.size() >= 3) {
            List<cS.Inner_BufferHelper> fresh = List.of(this.init_List.get(this.init_List.size() - 2), this.init_List.getLast());
            if (init_cS_DataUtil_List(fresh) != null) {
               this.init_List.clear();
               this.init_List.add(ray);
            }
         }

         this.load();
         return true;
      } else {
         return false;
      }
   }

   private static cS.Inner_DataUtil init_cS_DataUtil_List(List<cS.Inner_BufferHelper> rays) {
      if (rays.size() < 2) {
         return null;
      }

      cS.Inner_DataUtil best = null;

      for (int i = 0; i < rays.size() - 1; i++) {
         for (int j = i + 1; j < rays.size(); j++) {
            cS.Inner_BufferHelper a = rays.get(i);
            cS.Inner_BufferHelper b = rays.get(j);
            double cross = load(a.load, b.load);
            if (!(init_D_Vec3_Vec3(a.init, b.init) < 32.0) && !(Math.abs(cross) < 0.04)) {
               Vec3 separation = b.init.subtract(a.init);
               double t = load(separation, b.load) / cross;
               double u = load(separation, a.load) / cross;
               if (!(t < 0.0) && !(u < 0.0) && !(t > 25000.0) && !(u > 25000.0)) {
                  Vec3 point = a.init.add(a.load.scale(t));
                  double uncertainty = Math.max(16.0, (t + u) * 0.003 / Math.abs(cross));
                  if (!(uncertainty > 512.0)) {
                     boolean consistent = true;

                     for (cS.Inner_BufferHelper r : rays) {
                        if (!init_Z_Vec3_cS_BufferHelper(point, r)) {
                           consistent = false;
                           break;
                        }
                     }

                     if (consistent && (best == null || uncertainty < best.init_D)) {
                        best = new cS.Inner_DataUtil(point, uncertainty);
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   private static boolean init_Z_Vec3_cS_BufferHelper(Vec3 point, cS.Inner_BufferHelper ray) {
      Vec3 delta = point.subtract(ray.init);
      return delta.dot(ray.load) >= -8.0 && Math.abs(load(delta, ray.load)) <= Math.max(8.0, delta.horizontalDistance() * 0.008);
   }

   public Vec3 init_Vec3_Vec3(Vec3 player) {
      cS.Inner_DataUtil destination = this.init_cS_DataUtil();
      if (destination != null) {
         return new Vec3(destination.init_Vec3.x, player.y, destination.init_Vec3.z);
      }

      cS.Inner_BufferHelper ray = this.init_cS_BufferHelper();
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

   public static Vec3 init_Vec3_Collection(Collection<cS.Inner_CachePool> frames) {
      List<Vec3> centers = init_List_Collection(frames);
      return centers.isEmpty() ? null : centers.getFirst();
   }

   public static List<Vec3> init_List_Collection(Collection<cS.Inner_CachePool> frames) {
      Map<BlockPos, Direction> byPos = new HashMap<>();
      Set<BlockPos> candidates = new HashSet<>();

      for (cS.Inner_CachePool f : frames) {
         if (!f.init_Direction.getAxis().isVertical()) {
            byPos.put(f.init_BlockPos, f.init_Direction);
            BlockPos middle = f.init_BlockPos.relative(f.init_Direction, 2);
            Direction sideways = f.init_Direction.getClockWise();

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

   public static double init_D_Vec3_Vec3(Vec3 a, Vec3 b) {
      return Math.hypot(a.x - b.x, a.z - b.z);
   }

   public static boolean init_Z_Vec3(Vec3 p) {
      return p != null && Double.isFinite(p.x) && Double.isFinite(p.y) && Double.isFinite(p.z) && Math.abs(p.x) <= 3.0E7 && Math.abs(p.z) <= 3.0E7;
   }

   public record Inner_BufferHelper(Vec3 init, Vec3 load) {
   }

   public record Inner_CachePool(BlockPos init_BlockPos, Direction init_Direction) {
   }

   public record Inner_DataUtil(Vec3 init_Vec3, double init_D) {
   }

   public static final class Inner_StreamUtil {
      private final Vec3 init_Vec3;
      private Vec3 load_Vec3;
      private Vec3 read_Vec3;
      private double init_D;
      private double load_D;
      private double read_D;
      private double write;
      private double update;
      private int init_I;
      private boolean init_Z;
      private boolean load_Z;

      public Inner_StreamUtil(Vec3 origin) {
         this.init_Vec3 = origin;
         this.load_Vec3 = origin;
         this.init_Z = !cS.init_Z_Vec3(origin);
      }

      public cS.Inner_BufferHelper init(Vec3 at, int age) {
         if (!this.init_Z && !this.load_Z && age <= 60 && cS.init_Z_Vec3(at)) {
            if (at.distanceToSqr(this.load_Vec3) > 9.0) {
               this.init_Z = true;
               return null;
            }

            Vec3 travel = at.subtract(this.init_Vec3);
            double length = travel.horizontalDistance();
            if (!(length > 16.0) && !(length + 0.2 < this.load_Vec3.subtract(this.init_Vec3).horizontalDistance())) {
               if (length >= 0.5) {
                  Vec3 direction = new Vec3(travel.x / length, 0.0, travel.z / length);
                  if (this.read_Vec3 == null) {
                     this.read_Vec3 = direction;
                  } else if (this.read_Vec3.dot(direction) < 0.9998) {
                     this.init_Z = true;
                     return null;
                  }
               }

               this.load_Vec3 = at;
               if (length < 0.1) {
                  return null;
               }

               this.init_I++;
               this.init_D = this.init_D + travel.x;
               this.load_D = this.load_D + travel.z;
               this.read_D = this.read_D + travel.x * travel.x;
               this.write = this.write + travel.z * travel.z;
               this.update = this.update + travel.x * travel.z;
               if (!(length < 8.0) && this.init_I >= 6) {
                  double xx = this.read_D - this.init_D * this.init_D / this.init_I;
                  double zz = this.write - this.load_D * this.load_D / this.init_I;
                  double xz = this.update - this.init_D * this.load_D / this.init_I;
                  double angle = 0.5 * Math.atan2(2.0 * xz, xx - zz);
                  double x = Math.cos(angle);
                  double z = Math.sin(angle);
                  if (x * this.init_D + z * this.load_D < 0.0) {
                     x = -x;
                     z = -z;
                  }

                  this.load_Z = true;
                  return new cS.Inner_BufferHelper(this.init_Vec3, new Vec3(x, 0.0, z));
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
