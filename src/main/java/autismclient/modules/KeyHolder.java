package autismclient.modules;

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class KeyHolder {
   private static final double init = 1.0E-7;

   private KeyHolder() {
   }

   static List<Vec3> init(Vec3 eye, AABB face, Direction side, List<AABB> blockers, Vec3 preferred) {
      return init(eye, face, side, List.of()).init(blockers, preferred);
   }

   static KeyHolder.Inner_StreamUtil init(Vec3 eye, AABB face, Direction side, List<AABB> hard) {
      return new KeyHolder.Inner_StreamUtil(eye, face, side, hard);
   }

   private static List<KeyHolder.Inner_CachePool> init(List<KeyHolder.Inner_CachePool> points) {
      points.sort(Comparator.comparingDouble(KeyHolder.Inner_CachePool::init).thenComparingDouble(KeyHolder.Inner_CachePool::load));
      ArrayList<KeyHolder.Inner_CachePool> out = new ArrayList<>();

      for (KeyHolder.Inner_CachePool p : points) {
         while (out.size() > 1 && init(out.get(out.size() - 2), out.getLast(), p) <= 0.0) {
            out.removeLast();
         }

         out.add(p);
      }

      int lower = out.size();

      for (int i = points.size() - 2; i >= 0; i--) {
         KeyHolder.Inner_CachePool p = points.get(i);

         while (out.size() > lower && init(out.get(out.size() - 2), out.getLast(), p) <= 0.0) {
            out.removeLast();
         }

         out.add(p);
      }

      if (out.size() > 1) {
         out.removeLast();
      }

      return out;
   }

   private static double init(KeyHolder.Inner_CachePool a, KeyHolder.Inner_CachePool b, KeyHolder.Inner_CachePool c) {
      return (b.init - a.init) * (c.load - a.load) - (b.load - a.load) * (c.init - a.init);
   }

   private static double init(Vec3 point, Axis axis) {
      double var10000;
      switch (axis) {
         case X:
            var10000 = point.x;
            break;
         case Y:
            var10000 = point.y;
            break;
         case Z:
            var10000 = point.z;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static Vec3 init(Axis n, Axis u, Axis v, double plane, double a, double b) {
      double[] xyz = new double[3];
      xyz[n.ordinal()] = plane;
      xyz[u.ordinal()] = a;
      xyz[v.ordinal()] = b;
      return new Vec3(xyz[0], xyz[1], xyz[2]);
   }

   private record Inner_CachePool(double init, double load) {
   }

   private record Inner_DataUtil(KeyHolder.Inner_CachePool init, KeyHolder.Inner_CachePool load) {
   }

   static final class Inner_StreamUtil {
      private final Vec3 init_Vec3;
      private final AABB init_AABB;
      private final Axis init_Direction_Axis;
      private final Axis load_Direction_Axis;
      private final Axis read_Direction_Axis;
      private final double init_D;
      private final double load_D;
      private final double read_D;
      private final double write;
      private final double update;
      private final double sync;
      private final Area init_Area;

      private Inner_StreamUtil(Vec3 eye, AABB face, Direction side, List<AABB> hard) {
         this.init_Vec3 = eye;
         this.init_AABB = face;
         this.init_Direction_Axis = side.getAxis();
         this.load_Direction_Axis = this.init_Direction_Axis == Axis.X ? Axis.Y : Axis.X;
         this.read_Direction_Axis = this.init_Direction_Axis == Axis.Z ? Axis.Y : Axis.Z;
         this.init_D = side.getAxisDirection() == AxisDirection.POSITIVE ? face.max(this.init_Direction_Axis) : face.min(this.init_Direction_Axis);
         this.load_D = this.init_D - KeyHolder.init(eye, this.init_Direction_Axis);
         this.read_D = Math.signum(this.load_D);
         this.write = Math.abs(this.load_D);
         this.update = KeyHolder.init(eye, this.load_Direction_Axis) - face.min(this.load_Direction_Axis);
         this.sync = KeyHolder.init(eye, this.read_Direction_Axis) - face.min(this.read_Direction_Axis);
         double width = face.max(this.load_Direction_Axis) - face.min(this.load_Direction_Axis);
         double height = face.max(this.read_Direction_Axis) - face.min(this.read_Direction_Axis);
         double inset = Math.min(1.0E-5, Math.min(width, height) / 8.0);
         this.init_Area = !(this.write < 1.0E-7) && !(this.read_D * side.getAxisDirection().getStep() > 0.0) && !(width < 1.0E-7) && !(height < 1.0E-7)
            ? new Area(new java.awt.geom.Rectangle2D.Double(inset, inset, width - 2.0 * inset, height - 2.0 * inset))
            : new Area();
         this.init(this.init_Area, hard);
      }

      private void init(Area open, List<AABB> blockers) {
         if (!open.isEmpty()) {
            for (AABB box : blockers) {
               double a = (box.min(this.init_Direction_Axis) - KeyHolder.init(this.init_Vec3, this.init_Direction_Axis)) * this.read_D;
               double b = (box.max(this.init_Direction_Axis) - KeyHolder.init(this.init_Vec3, this.init_Direction_Axis)) * this.read_D;
               double near = Math.max(1.0E-7, Math.min(a, b));
               double far = Math.min(this.write - 1.0E-7, Math.max(a, b));
               if (!(near >= far)) {
                  ArrayList<KeyHolder.Inner_CachePool> projected = new ArrayList<>(8);

                  for (double n : new double[]{near, far}) {
                     for (double x : new double[]{box.min(this.load_Direction_Axis), box.max(this.load_Direction_Axis)}) {
                        for (double y : new double[]{box.min(this.read_Direction_Axis), box.max(this.read_Direction_Axis)}) {
                           projected.add(
                              new KeyHolder.Inner_CachePool(
                                 this.update + (x - KeyHolder.init(this.init_Vec3, this.load_Direction_Axis)) * this.write / n,
                                 this.sync + (y - KeyHolder.init(this.init_Vec3, this.read_Direction_Axis)) * this.write / n
                              )
                           );
                        }
                     }
                  }

                  List<KeyHolder.Inner_CachePool> hull = KeyHolder.init(projected);
                  if (hull.size() >= 3) {
                     java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
                     path.moveTo(hull.getFirst().init, hull.getFirst().load);

                     for (int i = 1; i < hull.size(); i++) {
                        path.lineTo(hull.get(i).init, hull.get(i).load);
                     }

                     path.closePath();
                     if (open.getBounds2D().intersects(path.getBounds2D())) {
                        open.subtract(new Area(path));
                        if (open.isEmpty()) {
                           return;
                        }
                     }
                  }
               }
            }
         }
      }

      List<Vec3> init(List<AABB> blockers, Vec3 preferred) {
         if (this.init_Area.isEmpty()) {
            return List.of();
         }

         Area open = new Area(this.init_Area);
         this.init(open, blockers);
         if (open.isEmpty()) {
            return List.of();
         }

         ArrayList<KeyHolder.Inner_DataUtil> edges = new ArrayList<>();
         TreeSet<Double> levels = new TreeSet<>();
         double[] c = new double[6];
         KeyHolder.Inner_CachePool first = null;
         KeyHolder.Inner_CachePool last = null;

         for (PathIterator it = open.getPathIterator(null); !it.isDone(); it.next()) {
            int kind = it.currentSegment(c);
            if (kind == 0) {
               first = last = new KeyHolder.Inner_CachePool(c[0], c[1]);
               levels.add(last.load);
            } else if (kind == 1) {
               KeyHolder.Inner_CachePool next = new KeyHolder.Inner_CachePool(c[0], c[1]);
               edges.add(new KeyHolder.Inner_DataUtil(last, next));
               levels.add(next.load);
               last = next;
            } else if (kind == 4 && last != null) {
               edges.add(new KeyHolder.Inner_DataUtil(last, first));
            }
         }

         ArrayList<Double> ys = new ArrayList<>(levels);
         ArrayList<Vec3> points = new ArrayList<>();
         double wantedU = KeyHolder.init(preferred, this.load_Direction_Axis) - this.init_AABB.min(this.load_Direction_Axis);
         double wantedV = KeyHolder.init(preferred, this.read_Direction_Axis) - this.init_AABB.min(this.read_Direction_Axis);

         for (int i = 1; i < ys.size(); i++) {
            double low = ys.get(i - 1);
            double high = ys.get(i);
            if (!(high - low < 1.0E-7)) {
               double y = (low + high) / 2.0;
               TreeSet<Double> xs = new TreeSet<>();

               for (KeyHolder.Inner_DataUtil edge : edges) {
                  if (!(y <= Math.min(edge.init.load, edge.load.load)) && !(y >= Math.max(edge.init.load, edge.load.load))) {
                     xs.add(edge.init.init + (edge.load.init - edge.init.init) * (y - edge.init.load) / (edge.load.load - edge.init.load));
                  }
               }

               ArrayList<Double> positions = new ArrayList<>(xs);

               for (int j = 1; j < positions.size(); j++) {
                  double left = positions.get(j - 1);
                  double right = positions.get(j);
                  if (!(right - left < 1.0E-7)) {
                     double x = (left + right) / 2.0;
                     if (open.contains(x, y)) {
                        points.add(
                           KeyHolder.init(
                              this.init_Direction_Axis,
                              this.load_Direction_Axis,
                              this.read_Direction_Axis,
                              this.init_D,
                              this.init_AABB.min(this.load_Direction_Axis) + x,
                              this.init_AABB.min(this.read_Direction_Axis) + y
                           )
                        );
                        double closeX = Math.clamp(wantedU, left + (right - left) * 0.2, right - (right - left) * 0.2);
                        double closeY = Math.clamp(wantedV, low + (high - low) * 0.2, high - (high - low) * 0.2);
                        if (open.contains(closeX, closeY)) {
                           points.add(
                              KeyHolder.init(
                                 this.init_Direction_Axis,
                                 this.load_Direction_Axis,
                                 this.read_Direction_Axis,
                                 this.init_D,
                                 this.init_AABB.min(this.load_Direction_Axis) + closeX,
                                 this.init_AABB.min(this.read_Direction_Axis) + closeY
                              )
                           );
                        }
                     }
                  }
               }
            }
         }

         return points.stream().distinct().sorted(Comparator.comparingDouble(p -> p.distanceToSqr(preferred))).toList();
      }
   }
}
