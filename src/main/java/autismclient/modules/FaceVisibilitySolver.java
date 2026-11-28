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

final class FaceVisibilitySolver {
   private static final double EPSILON = 1.0E-7;

   private FaceVisibilitySolver() {
   }

   static List<Vec3> visiblePoints(Vec3 eye, AABB face, Direction side, List<AABB> blockers, Vec3 preferred) {
      return project(eye, face, side, List.of()).visiblePoints(blockers, preferred);
   }

   static FaceVisibilitySolver.FaceProjection project(Vec3 eye, AABB face, Direction side, List<AABB> hard) {
      return new FaceVisibilitySolver.FaceProjection(eye, face, side, hard);
   }

   private static List<FaceVisibilitySolver.Point2> init(List<FaceVisibilitySolver.Point2> points) {
      points.sort(Comparator.comparingDouble(FaceVisibilitySolver.Point2::init).thenComparingDouble(FaceVisibilitySolver.Point2::load));
      ArrayList<FaceVisibilitySolver.Point2> out = new ArrayList<>();

      for (FaceVisibilitySolver.Point2 p : points) {
         while (out.size() > 1 && init(out.get(out.size() - 2), out.getLast(), p) <= 0.0) {
            out.removeLast();
         }

         out.add(p);
      }

      int lower = out.size();

      for (int i = points.size() - 2; i >= 0; i--) {
         FaceVisibilitySolver.Point2 p = points.get(i);

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

   private static double init(FaceVisibilitySolver.Point2 a, FaceVisibilitySolver.Point2 b, FaceVisibilitySolver.Point2 c) {
      return (b.init - a.init) * (c.load - a.load) - (b.load - a.load) * (c.init - a.init);
   }

   private static double coord(Vec3 point, Axis axis) {
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

   private record Point2(double init, double load) {
   }

   private record Edge(FaceVisibilitySolver.Point2 init, FaceVisibilitySolver.Point2 load) {
   }

   static final class FaceProjection {
      private final Vec3 eye;
      private final AABB face;
      private final Axis normalAxis;
      private final Axis uAxis;
      private final Axis vAxis;
      private final double planeCoord;
      private final double eyeOffset;
      private final double facingSign;
      private final double depth;
      private final double eyeU;
      private final double eyeV;
      private final Area visibleArea;

      private FaceProjection(Vec3 eye, AABB face, Direction side, List<AABB> hard) {
         this.eye = eye;
         this.face = face;
         this.normalAxis = side.getAxis();
         this.uAxis = this.normalAxis == Axis.X ? Axis.Y : Axis.X;
         this.vAxis = this.normalAxis == Axis.Z ? Axis.Y : Axis.Z;
         this.planeCoord = side.getAxisDirection() == AxisDirection.POSITIVE ? face.max(this.normalAxis) : face.min(this.normalAxis);
         this.eyeOffset = this.planeCoord - FaceVisibilitySolver.coord(eye, this.normalAxis);
         this.facingSign = Math.signum(this.eyeOffset);
         this.depth = Math.abs(this.eyeOffset);
         this.eyeU = FaceVisibilitySolver.coord(eye, this.uAxis) - face.min(this.uAxis);
         this.eyeV = FaceVisibilitySolver.coord(eye, this.vAxis) - face.min(this.vAxis);
         double width = face.max(this.uAxis) - face.min(this.uAxis);
         double height = face.max(this.vAxis) - face.min(this.vAxis);
         double inset = Math.min(1.0E-5, Math.min(width, height) / 8.0);
         this.visibleArea = !(this.depth < 1.0E-7) && !(this.facingSign * side.getAxisDirection().getStep() > 0.0) && !(width < 1.0E-7) && !(height < 1.0E-7)
            ? new Area(new java.awt.geom.Rectangle2D.Double(inset, inset, width - 2.0 * inset, height - 2.0 * inset))
            : new Area();
         this.init(this.visibleArea, hard);
      }

      private void init(Area open, List<AABB> blockers) {
         if (!open.isEmpty()) {
            for (AABB box : blockers) {
               double a = (box.min(this.normalAxis) - FaceVisibilitySolver.coord(this.eye, this.normalAxis)) * this.facingSign;
               double b = (box.max(this.normalAxis) - FaceVisibilitySolver.coord(this.eye, this.normalAxis)) * this.facingSign;
               double near = Math.max(1.0E-7, Math.min(a, b));
               double far = Math.min(this.depth - 1.0E-7, Math.max(a, b));
               if (!(near >= far)) {
                  ArrayList<FaceVisibilitySolver.Point2> projected = new ArrayList<>(8);

                  for (double n : new double[]{near, far}) {
                     for (double x : new double[]{box.min(this.uAxis), box.max(this.uAxis)}) {
                        for (double y : new double[]{box.min(this.vAxis), box.max(this.vAxis)}) {
                           projected.add(
                              new FaceVisibilitySolver.Point2(
                                 this.eyeU + (x - FaceVisibilitySolver.coord(this.eye, this.uAxis)) * this.depth / n,
                                 this.eyeV + (y - FaceVisibilitySolver.coord(this.eye, this.vAxis)) * this.depth / n
                              )
                           );
                        }
                     }
                  }

                  List<FaceVisibilitySolver.Point2> hull = FaceVisibilitySolver.init(projected);
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

      List<Vec3> visiblePoints(List<AABB> blockers, Vec3 preferred) {
         if (this.visibleArea.isEmpty()) {
            return List.of();
         }

         Area open = new Area(this.visibleArea);
         this.init(open, blockers);
         if (open.isEmpty()) {
            return List.of();
         }

         ArrayList<FaceVisibilitySolver.Edge> edges = new ArrayList<>();
         TreeSet<Double> levels = new TreeSet<>();
         double[] c = new double[6];
         FaceVisibilitySolver.Point2 first = null;
         FaceVisibilitySolver.Point2 last = null;

         for (PathIterator it = open.getPathIterator(null); !it.isDone(); it.next()) {
            int kind = it.currentSegment(c);
            if (kind == 0) {
               first = last = new FaceVisibilitySolver.Point2(c[0], c[1]);
               levels.add(last.load);
            } else if (kind == 1) {
               FaceVisibilitySolver.Point2 next = new FaceVisibilitySolver.Point2(c[0], c[1]);
               edges.add(new FaceVisibilitySolver.Edge(last, next));
               levels.add(next.load);
               last = next;
            } else if (kind == 4 && last != null) {
               edges.add(new FaceVisibilitySolver.Edge(last, first));
            }
         }

         ArrayList<Double> ys = new ArrayList<>(levels);
         ArrayList<Vec3> points = new ArrayList<>();
         double wantedU = FaceVisibilitySolver.coord(preferred, this.uAxis) - this.face.min(this.uAxis);
         double wantedV = FaceVisibilitySolver.coord(preferred, this.vAxis) - this.face.min(this.vAxis);

         for (int i = 1; i < ys.size(); i++) {
            double low = ys.get(i - 1);
            double high = ys.get(i);
            if (!(high - low < 1.0E-7)) {
               double y = (low + high) / 2.0;
               TreeSet<Double> xs = new TreeSet<>();

               for (FaceVisibilitySolver.Edge edge : edges) {
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
                           FaceVisibilitySolver.init(
                              this.normalAxis,
                              this.uAxis,
                              this.vAxis,
                              this.planeCoord,
                              this.face.min(this.uAxis) + x,
                              this.face.min(this.vAxis) + y
                           )
                        );
                        double closeX = Math.clamp(wantedU, left + (right - left) * 0.2, right - (right - left) * 0.2);
                        double closeY = Math.clamp(wantedV, low + (high - low) * 0.2, high - (high - low) * 0.2);
                        if (open.contains(closeX, closeY)) {
                           points.add(
                              FaceVisibilitySolver.init(
                                 this.normalAxis,
                                 this.uAxis,
                                 this.vAxis,
                                 this.planeCoord,
                                 this.face.min(this.uAxis) + closeX,
                                 this.face.min(this.vAxis) + closeY
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
