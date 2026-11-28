package autismclient.modules;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

final class EspVoxelMeshBuilder {
   private static final double EPSILON = 1.0E-7;

   private EspVoxelMeshBuilder() {
   }

   static EspVoxelMeshBuilder.VoxelMesh build(List<EspVoxelMeshBuilder.ColoredBox> input) {
      if (input != null && !input.isEmpty()) {
         Map<Integer, LongOpenHashSet> voxelsByColor = new HashMap<>();
         List<EspVoxelMeshBuilder.ColoredBox> partial = new ArrayList<>();

         for (EspVoxelMeshBuilder.ColoredBox target : input) {
            if (target != null && target.box() != null) {
               int[] voxel = voxelOf(target.box());
               if (voxel == null) {
                  partial.add(target);
               } else {
                  voxelsByColor.computeIfAbsent(target.color(), ignored -> new LongOpenHashSet()).add(BlockPos.asLong(voxel[0], voxel[1], voxel[2]));
               }
            }
         }

         List<EspVoxelMeshBuilder.MeshQuad> quads = new ArrayList<>();
         List<EspVoxelMeshBuilder.MeshEdge> edges = new ArrayList<>();

         for (Entry<Integer, LongOpenHashSet> group : voxelsByColor.entrySet()) {
            buildGroup(group.getValue(), group.getKey(), quads, edges);
         }

         for (EspVoxelMeshBuilder.ColoredBox box : mergeBoxes(partial)) {
            addBox(box.box(), box.color(), quads, edges);
         }

         return new EspVoxelMeshBuilder.VoxelMesh(List.copyOf(quads), List.copyOf(edges));
      } else {
         return EspVoxelMeshBuilder.VoxelMesh.EMPTY;
      }
   }

   static boolean isVoxelAligned(AABB box) {
      return voxelOf(box) != null;
   }

   private static int[] voxelOf(AABB box) {
      if (box == null) {
         return null;
      }

      int x = (int)Math.rint(box.minX);
      int y = (int)Math.rint(box.minY);
      int z = (int)Math.rint(box.minZ);
      return nearlyEqual(box.minX, x)
            && nearlyEqual(box.minY, y)
            && nearlyEqual(box.minZ, z)
            && nearlyEqual(box.maxX, x + 1.0)
            && nearlyEqual(box.maxY, y + 1.0)
            && nearlyEqual(box.maxZ, z + 1.0)
         ? new int[]{x, y, z}
         : null;
   }

   private static void buildGroup(LongOpenHashSet occupied, int color, List<EspVoxelMeshBuilder.MeshQuad> quads, List<EspVoxelMeshBuilder.MeshEdge> outputEdges) {
      if (occupied != null && !occupied.isEmpty()) {
         LongOpenHashSet[] candidates = new LongOpenHashSet[]{
            new LongOpenHashSet(occupied.size() * 4), new LongOpenHashSet(occupied.size() * 4), new LongOpenHashSet(occupied.size() * 4)
         };
         LongIterator iterator = occupied.iterator();

         while (iterator.hasNext()) {
            long packed = iterator.nextLong();
            int x = BlockPos.getX(packed);
            int y = BlockPos.getY(packed);
            int z = BlockPos.getZ(packed);
            if (!isOccupied(occupied, x, y - 1, z)) {
               addFace(quads, 0, x, y, z, x + 1, y + 1, z + 1, color);
            }

            if (!isOccupied(occupied, x, y + 1, z)) {
               addFace(quads, 1, x, y, z, x + 1, y + 1, z + 1, color);
            }

            if (!isOccupied(occupied, x, y, z + 1)) {
               addFace(quads, 2, x, y, z, x + 1, y + 1, z + 1, color);
            }

            if (!isOccupied(occupied, x, y, z - 1)) {
               addFace(quads, 3, x, y, z, x + 1, y + 1, z + 1, color);
            }

            if (!isOccupied(occupied, x - 1, y, z)) {
               addFace(quads, 4, x, y, z, x + 1, y + 1, z + 1, color);
            }

            if (!isOccupied(occupied, x + 1, y, z)) {
               addFace(quads, 5, x, y, z, x + 1, y + 1, z + 1, color);
            }

            addEdgeCandidates(candidates, x, y, z);
         }

         List<EspVoxelMeshBuilder.VisibleFace> visible = new ArrayList<>();

         for (int axis = 0; axis < candidates.length; axis++) {
            LongIterator iteratorx = candidates[axis].iterator();

            while (iteratorx.hasNext()) {
               long packed = iteratorx.nextLong();
               int x = BlockPos.getX(packed);
               int y = BlockPos.getY(packed);
               int z = BlockPos.getZ(packed);
               if (isVisibleEdge(occupied, axis, x, y, z)) {
                  visible.add(new EspVoxelMeshBuilder.VisibleFace(axis, x, y, z, color));
               }
            }
         }

         buildEdges(visible, outputEdges);
      }
   }

   private static void addEdgeCandidates(LongOpenHashSet[] candidates, int x, int y, int z) {
      candidates[0].add(BlockPos.asLong(x, y, z));
      candidates[0].add(BlockPos.asLong(x, y + 1, z));
      candidates[0].add(BlockPos.asLong(x, y, z + 1));
      candidates[0].add(BlockPos.asLong(x, y + 1, z + 1));
      candidates[1].add(BlockPos.asLong(x, y, z));
      candidates[1].add(BlockPos.asLong(x + 1, y, z));
      candidates[1].add(BlockPos.asLong(x, y, z + 1));
      candidates[1].add(BlockPos.asLong(x + 1, y, z + 1));
      candidates[2].add(BlockPos.asLong(x, y, z));
      candidates[2].add(BlockPos.asLong(x + 1, y, z));
      candidates[2].add(BlockPos.asLong(x, y + 1, z));
      candidates[2].add(BlockPos.asLong(x + 1, y + 1, z));
   }

   private static boolean isVisibleEdge(LongOpenHashSet occupied, int axis, int x, int y, int z) {
      boolean a;
      boolean b;
      boolean c;
      boolean d;
      if (axis == 0) {
         a = isOccupied(occupied, x, y - 1, z - 1);
         b = isOccupied(occupied, x, y, z - 1);
         c = isOccupied(occupied, x, y - 1, z);
         d = isOccupied(occupied, x, y, z);
      } else if (axis == 1) {
         a = isOccupied(occupied, x - 1, y, z - 1);
         b = isOccupied(occupied, x, y, z - 1);
         c = isOccupied(occupied, x - 1, y, z);
         d = isOccupied(occupied, x, y, z);
      } else {
         a = isOccupied(occupied, x - 1, y - 1, z);
         b = isOccupied(occupied, x, y - 1, z);
         c = isOccupied(occupied, x - 1, y, z);
         d = isOccupied(occupied, x, y, z);
      }

      int count = (a ? 1 : 0) + (b ? 1 : 0) + (c ? 1 : 0) + (d ? 1 : 0);
      return count == 1 || count == 3 || count == 2 && (a && d || b && c);
   }

   private static void buildEdges(List<EspVoxelMeshBuilder.VisibleFace> input, List<EspVoxelMeshBuilder.MeshEdge> output) {
      input.sort((left, right) -> {
         int compared = Integer.compare(left.color(), right.color());
         if (compared != 0) {
            return compared;
         }

         compared = Integer.compare(left.axis(), right.axis());
         if (compared != 0) {
            return compared;
         }

         compared = Integer.compare(crossA(left), crossA(right));
         if (compared != 0) {
            return compared;
         }

         compared = Integer.compare(crossB(left), crossB(right));
         return compared != 0 ? compared : Integer.compare(alongAxis(left), alongAxis(right));
      });
      int index = 0;

      while (index < input.size()) {
         EspVoxelMeshBuilder.VisibleFace first = input.get(index++);
         int end = alongAxis(first) + 1;

         while (index < input.size()) {
            EspVoxelMeshBuilder.VisibleFace next = input.get(index);
            if (next.color() != first.color() || next.axis() != first.axis() || crossA(next) != crossA(first) || crossB(next) != crossB(first) || alongAxis(next) != end) {
               break;
            }

            end++;
            index++;
         }

         emitEdge(output, first, end);
      }
   }

   private static int alongAxis(EspVoxelMeshBuilder.VisibleFace edge) {
      return edge.axis() == 0 ? edge.x() : (edge.axis() == 1 ? edge.y() : edge.z());
   }

   private static int crossA(EspVoxelMeshBuilder.VisibleFace edge) {
      return edge.axis() == 0 ? edge.y() : edge.x();
   }

   private static int crossB(EspVoxelMeshBuilder.VisibleFace edge) {
      return edge.axis() == 2 ? edge.y() : edge.z();
   }

   private static void emitEdge(List<EspVoxelMeshBuilder.MeshEdge> output, EspVoxelMeshBuilder.VisibleFace edge, int end) {
      if (edge.axis() == 0) {
         output.add(new EspVoxelMeshBuilder.MeshEdge(edge.x(), edge.y(), edge.z(), end, edge.y(), edge.z(), edge.color()));
      } else if (edge.axis() == 1) {
         output.add(new EspVoxelMeshBuilder.MeshEdge(edge.x(), edge.y(), edge.z(), edge.x(), end, edge.z(), edge.color()));
      } else {
         output.add(new EspVoxelMeshBuilder.MeshEdge(edge.x(), edge.y(), edge.z(), edge.x(), edge.y(), end, edge.color()));
      }
   }

   private static boolean isOccupied(LongOpenHashSet occupied, int x, int y, int z) {
      return occupied.contains(BlockPos.asLong(x, y, z));
   }

   private static List<EspVoxelMeshBuilder.ColoredBox> mergeBoxes(List<EspVoxelMeshBuilder.ColoredBox> boxes) {
      if (boxes.size() < 2) {
         return boxes;
      }

      List<EspVoxelMeshBuilder.ColoredBox> merged = new ArrayList<>(boxes);
      int passes = 0;

      boolean changed;
      do {
         int before = merged.size();
         List<EspVoxelMeshBuilder.ColoredBox> var5 = mergeAlongAxis(merged, 0);
         List<EspVoxelMeshBuilder.ColoredBox> var6 = mergeAlongAxis(var5, 1);
         merged = mergeAlongAxis(var6, 2);
         changed = merged.size() < before;
      } while (changed && ++passes < 12);

      return merged;
   }

   private static List<EspVoxelMeshBuilder.ColoredBox> mergeAlongAxis(List<EspVoxelMeshBuilder.ColoredBox> boxes, int axis) {
      if (boxes.size() < 2) {
         return boxes;
      }

      List<EspVoxelMeshBuilder.ColoredBox> sorted = new ArrayList<>(boxes);
      sorted.sort(mergeOrder(axis));
      List<EspVoxelMeshBuilder.ColoredBox> output = new ArrayList<>(sorted.size());
      EspVoxelMeshBuilder.ColoredBox current = sorted.getFirst();

      for (int i = 1; i < sorted.size(); i++) {
         EspVoxelMeshBuilder.ColoredBox next = sorted.get(i);
         if (canMerge(current, next, axis) && minOnAxis(next.box(), axis) <= maxOnAxis(current.box(), axis) + 1.0E-7) {
            current = new EspVoxelMeshBuilder.ColoredBox(union(current.box(), next.box(), axis), current.color());
         } else {
            output.add(current);
            current = next;
         }
      }

      output.add(current);
      return output;
   }

   private static Comparator<EspVoxelMeshBuilder.ColoredBox> mergeOrder(int axis) {
      return (left, right) -> {
         int compared = Integer.compare(left.color(), right.color());
         if (compared != 0) {
            return compared;
         }

         AABB a = left.box();
         AABB b = right.box();

         for (int candidate = 0; candidate < 3; candidate++) {
            if (candidate != axis) {
               compared = Double.compare(minOnAxis(a, candidate), minOnAxis(b, candidate));
               if (compared != 0) {
                  return compared;
               }

               compared = Double.compare(maxOnAxis(a, candidate), maxOnAxis(b, candidate));
               if (compared != 0) {
                  return compared;
               }
            }
         }

         compared = Double.compare(minOnAxis(a, axis), minOnAxis(b, axis));
         return compared != 0 ? compared : Double.compare(maxOnAxis(a, axis), maxOnAxis(b, axis));
      };
   }

   private static boolean canMerge(EspVoxelMeshBuilder.ColoredBox left, EspVoxelMeshBuilder.ColoredBox right, int axis) {
      if (left.color() != right.color()) {
         return false;
      }

      for (int candidate = 0; candidate < 3; candidate++) {
         if (candidate != axis
            && (
               !nearlyEqual(minOnAxis(left.box(), candidate), minOnAxis(right.box(), candidate))
                  || !nearlyEqual(maxOnAxis(left.box(), candidate), maxOnAxis(right.box(), candidate))
            )) {
            return false;
         }
      }

      return true;
   }

   private static AABB union(AABB left, AABB right, int axis) {
      AABB var10000;
      switch (axis) {
         case 0:
            var10000 = new AABB(Math.min(left.minX, right.minX), left.minY, left.minZ, Math.max(left.maxX, right.maxX), left.maxY, left.maxZ);
            break;
         case 1:
            var10000 = new AABB(left.minX, Math.min(left.minY, right.minY), left.minZ, left.maxX, Math.max(left.maxY, right.maxY), left.maxZ);
            break;
         default:
            var10000 = new AABB(left.minX, left.minY, Math.min(left.minZ, right.minZ), left.maxX, left.maxY, Math.max(left.maxZ, right.maxZ));
      }

      return var10000;
   }

   private static double minOnAxis(AABB box, int axis) {
      return axis == 0 ? box.minX : (axis == 1 ? box.minY : box.minZ);
   }

   private static double maxOnAxis(AABB box, int axis) {
      return axis == 0 ? box.maxX : (axis == 1 ? box.maxY : box.maxZ);
   }

   private static boolean nearlyEqual(double left, double right) {
      return Math.abs(left - right) <= 1.0E-7;
   }

   private static void addBox(AABB box, int color, List<EspVoxelMeshBuilder.MeshQuad> quads, List<EspVoxelMeshBuilder.MeshEdge> edges) {
      for (int face = 0; face < 6; face++) {
         addFace(quads, face, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color);
      }

      double x1 = box.minX;
      double y1 = box.minY;
      double z1 = box.minZ;
      double x2 = box.maxX;
      double y2 = box.maxY;
      double z2 = box.maxZ;
      edges.add(new EspVoxelMeshBuilder.MeshEdge(x1, y1, z1, x2, y1, z1, color));
      edges.add(new EspVoxelMeshBuilder.MeshEdge(x2, y1, z1, x2, y1, z2, color));
      edges.add(new EspVoxelMeshBuilder.MeshEdge(x2, y1, z2, x1, y1, z2, color));
      edges.add(new EspVoxelMeshBuilder.MeshEdge(x1, y1, z2, x1, y1, z1, color));
      edges.add(new EspVoxelMeshBuilder.MeshEdge(x1, y2, z1, x2, y2, z1, color));
      edges.add(new EspVoxelMeshBuilder.MeshEdge(x2, y2, z1, x2, y2, z2, color));
      edges.add(new EspVoxelMeshBuilder.MeshEdge(x2, y2, z2, x1, y2, z2, color));
      edges.add(new EspVoxelMeshBuilder.MeshEdge(x1, y2, z2, x1, y2, z1, color));
      edges.add(new EspVoxelMeshBuilder.MeshEdge(x1, y1, z1, x1, y2, z1, color));
      edges.add(new EspVoxelMeshBuilder.MeshEdge(x2, y1, z1, x2, y2, z1, color));
      edges.add(new EspVoxelMeshBuilder.MeshEdge(x2, y1, z2, x2, y2, z2, color));
      edges.add(new EspVoxelMeshBuilder.MeshEdge(x1, y1, z2, x1, y2, z2, color));
   }

   private static void addFace(List<EspVoxelMeshBuilder.MeshQuad> quads, int face, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
      switch (face) {
         case 0:
            quads.add(new EspVoxelMeshBuilder.MeshQuad(x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, color));
            break;
         case 1:
            quads.add(new EspVoxelMeshBuilder.MeshQuad(x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, color));
            break;
         case 2:
            quads.add(new EspVoxelMeshBuilder.MeshQuad(x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, color));
            break;
         case 3:
            quads.add(new EspVoxelMeshBuilder.MeshQuad(x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, color));
            break;
         case 4:
            quads.add(new EspVoxelMeshBuilder.MeshQuad(x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, color));
            break;
         default:
            quads.add(new EspVoxelMeshBuilder.MeshQuad(x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2, color));
      }
   }

   private record VisibleFace(int axis, int x, int y, int z, int color) {
   }

   record VoxelMesh(List<EspVoxelMeshBuilder.MeshQuad> faces, List<EspVoxelMeshBuilder.MeshEdge> edges) {
      private static final EspVoxelMeshBuilder.VoxelMesh EMPTY = new EspVoxelMeshBuilder.VoxelMesh(List.of(), List.of());
   }

   record MeshQuad(
      double init_D,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      double x3,
      double y3,
      double z3,
      double x4,
      double y4,
      double z4,
      int color
   ) {
   }

   record ColoredBox(AABB box, int color) {
   }

   record MeshEdge(double init_D, double y1, double z1, double x2, double y2, double z2, int color) {
   }
}
