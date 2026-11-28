package autismclient.gui.sdf;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class SteppedRowContour {
   private static final float read = 2.0F;
   final java.awt.geom.Path2D.Float path;
   final List<SteppedRowContour.Edge> edges;
   final float width;
   final float load;

   SteppedRowContour(List<Float> widths, List<Float> heights, float radius) {
      radius = Math.min(radius, 4.0F);
      widths = smoothWidths(widths, radius);
      float widest = 0.0F;

      for (float value : widths) {
         widest = Math.max(widest, value);
      }

      this.width = widest;
      float[] tops = new float[heights.size() + 1];

      for (int i = 0; i < heights.size(); i++) {
         tops[i + 1] = tops[i] + heights.get(i);
      }

      this.load = tops[heights.size()];
      ArrayList<java.awt.geom.Point2D.Float> vertices = new ArrayList<>();
      vertices.add(new java.awt.geom.Point2D.Float(-widths.getFirst() / 2.0F, 0.0F));
      vertices.add(new java.awt.geom.Point2D.Float(widths.getFirst() / 2.0F, 0.0F));

      for (int i = 1; i < widths.size(); i++) {
         if (!widths.get(i).equals(widths.get(i - 1))) {
            vertices.add(new java.awt.geom.Point2D.Float(widths.get(i - 1) / 2.0F, tops[i]));
            vertices.add(new java.awt.geom.Point2D.Float(widths.get(i) / 2.0F, tops[i]));
         }
      }

      vertices.add(new java.awt.geom.Point2D.Float(widths.getLast() / 2.0F, this.load));
      vertices.add(new java.awt.geom.Point2D.Float(-widths.getLast() / 2.0F, this.load));

      for (int i = widths.size() - 1; i > 0; i--) {
         if (!widths.get(i).equals(widths.get(i - 1))) {
            vertices.add(new java.awt.geom.Point2D.Float(-widths.get(i) / 2.0F, tops[i]));
            vertices.add(new java.awt.geom.Point2D.Float(-widths.get(i - 1) / 2.0F, tops[i]));
         }
      }

      this.path = new java.awt.geom.Path2D.Float();
      float[] corners = new float[vertices.size()];

      for (int i = 0; i < vertices.size(); i++) {
         java.awt.geom.Point2D.Float v = vertices.get(i);
         java.awt.geom.Point2D.Float before = vertices.get((i + vertices.size() - 1) % vertices.size());
         java.awt.geom.Point2D.Float after = vertices.get((i + 1) % vertices.size());
         float turn = (v.x - before.x) * (after.y - v.y) - (v.y - before.y) * (after.x - v.x);
         corners[i] = turn > 0.0F ? radius : Math.min(radius, 2.0F);
      }

      for (int i = 0; i < vertices.size(); i++) {
         java.awt.geom.Point2D.Float v = vertices.get(i);
         java.awt.geom.Point2D.Float before = vertices.get((i + vertices.size() - 1) % vertices.size());
         java.awt.geom.Point2D.Float after = vertices.get((i + 1) % vertices.size());
         double a = v.distance(before);
         double b = v.distance(after);
         float corner = corners[i];
         double r = corner == 0.0F
            ? 0.0
            : Math.min(
               corner,
               Math.min(
                  a * corner / (corner + corners[(i + vertices.size() - 1) % vertices.size()]), b * corner / (corner + corners[(i + 1) % vertices.size()])
               )
            );
         float ax = (float)(v.x + (before.x - v.x) * r / a);
         float ay = (float)(v.y + (before.y - v.y) * r / a);
         float bx = (float)(v.x + (after.x - v.x) * r / b);
         float by = (float)(v.y + (after.y - v.y) * r / b);
         if (i == 0) {
            this.path.moveTo(ax, ay);
         } else {
            this.path.lineTo(ax, ay);
         }

         this.path.quadTo(v.x, v.y, bx, by);
      }

      this.path.closePath();
      ArrayList<SteppedRowContour.Edge> lines = new ArrayList<>();
      float[] p = new float[6];
      float startX = 0.0F;
      float startY = 0.0F;
      float x = 0.0F;
      float y = 0.0F;

      for (PathIterator iterator = this.path.getPathIterator(null, 0.04); !iterator.isDone(); iterator.next()) {
         int kind = iterator.currentSegment(p);
         if (kind == 0) {
            startX = x = p[0];
            startY = y = p[1];
         } else {
            float nextX = kind == 4 ? startX : p[0];
            float nextY = kind == 4 ? startY : p[1];
            float dx = nextX - x;
            float dy = nextY - y;
            if (dx * dx + dy * dy > 1.0E-10) {
               lines.add(new SteppedRowContour.Edge(x, y, dx, dy, dx * dx + dy * dy));
            }

            x = nextX;
            y = nextY;
         }
      }

      this.edges = List.copyOf(lines);
   }

   static List<Float> smoothWidths(List<Float> widths, float radius) {
      if (widths.size() < 2) {
         return List.copyOf(widths);
      }

      float threshold = Math.max(8.0F, 2.0F * (radius + Math.min(radius, 2.0F)));
      ArrayList<Float> sorted = new ArrayList<>(widths);
      sorted.sort(Comparator.reverseOrder());
      ArrayList<Float> aligned = new ArrayList<>(widths);
      Float anchor = sorted.getFirst();

      for (Float width : sorted) {
         if (anchor - width > threshold) {
            anchor = width;
         }

         for (int i = 0; i < widths.size(); i++) {
            if (widths.get(i).equals(width)) {
               aligned.set(i, anchor);
            }
         }
      }

      return List.copyOf(aligned);
   }

   static float distance(SteppedRowContour.Edge segment, float x, float y) {
      float t = Math.clamp(((x - segment.init) * segment.read + (y - segment.load) * segment.write) / segment.update, 0.0F, 1.0F);
      float dx = x - segment.init - segment.read * t;
      float dy = y - segment.load - segment.write * t;
      return dx * dx + dy * dy;
   }

   record Edge(float init, float load, float read, float write, float update) {
   }
}
