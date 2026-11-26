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
   private static final double init = 1.0E-7;

   private EspVoxelMeshBuilder() {
   }

   static EspVoxelMeshBuilder.Inner_CachePool init_ag_CachePool(List<EspVoxelMeshBuilder.Inner_DataUtil> input) {
      if (input != null && !input.isEmpty()) {
         Map<Integer, LongOpenHashSet> voxelsByColor = new HashMap<>();
         List<EspVoxelMeshBuilder.Inner_DataUtil> partial = new ArrayList<>();

         for (EspVoxelMeshBuilder.Inner_DataUtil target : input) {
            if (target != null && target.init_AABB() != null) {
               int[] voxel = init_IArr(target.init_AABB());
               if (voxel == null) {
                  partial.add(target);
               } else {
                  voxelsByColor.computeIfAbsent(target.init_I(), ignored -> new LongOpenHashSet()).add(BlockPos.asLong(voxel[0], voxel[1], voxel[2]));
               }
            }
         }

         List<EspVoxelMeshBuilder.Inner_ConfigStore> quads = new ArrayList<>();
         List<EspVoxelMeshBuilder.Inner_StreamUtil> edges = new ArrayList<>();

         for (Entry<Integer, LongOpenHashSet> group : voxelsByColor.entrySet()) {
            init(group.getValue(), group.getKey(), quads, edges);
         }

         for (EspVoxelMeshBuilder.Inner_DataUtil box : init_List(partial)) {
            init(box.init_AABB(), box.init_I(), quads, edges);
         }

         return new EspVoxelMeshBuilder.Inner_CachePool(List.copyOf(quads), List.copyOf(edges));
      } else {
         return EspVoxelMeshBuilder.Inner_CachePool.init_ag_CachePool;
      }
   }

   static boolean init_Z(AABB box) {
      return init_IArr(box) != null;
   }

   private static int[] init_IArr(AABB box) {
      if (box == null) {
         return null;
      }

      int x = (int)Math.rint(box.minX);
      int y = (int)Math.rint(box.minY);
      int z = (int)Math.rint(box.minZ);
      return init_Z_D_D(box.minX, x)
            && init_Z_D_D(box.minY, y)
            && init_Z_D_D(box.minZ, z)
            && init_Z_D_D(box.maxX, x + 1.0)
            && init_Z_D_D(box.maxY, y + 1.0)
            && init_Z_D_D(box.maxZ, z + 1.0)
         ? new int[]{x, y, z}
         : null;
   }

   private static void init(LongOpenHashSet occupied, int color, List<EspVoxelMeshBuilder.Inner_ConfigStore> quads, List<EspVoxelMeshBuilder.Inner_StreamUtil> outputEdges) {
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
            if (!init(occupied, x, y - 1, z)) {
               init(quads, 0, x, y, z, x + 1, y + 1, z + 1, color);
            }

            if (!init(occupied, x, y + 1, z)) {
               init(quads, 1, x, y, z, x + 1, y + 1, z + 1, color);
            }

            if (!init(occupied, x, y, z + 1)) {
               init(quads, 2, x, y, z, x + 1, y + 1, z + 1, color);
            }

            if (!init(occupied, x, y, z - 1)) {
               init(quads, 3, x, y, z, x + 1, y + 1, z + 1, color);
            }

            if (!init(occupied, x - 1, y, z)) {
               init(quads, 4, x, y, z, x + 1, y + 1, z + 1, color);
            }

            if (!init(occupied, x + 1, y, z)) {
               init(quads, 5, x, y, z, x + 1, y + 1, z + 1, color);
            }

            init(candidates, x, y, z);
         }

         List<EspVoxelMeshBuilder.Inner_BufferHelper> visible = new ArrayList<>();

         for (int axis = 0; axis < candidates.length; axis++) {
            LongIterator iteratorx = candidates[axis].iterator();

            while (iteratorx.hasNext()) {
               long packed = iteratorx.nextLong();
               int x = BlockPos.getX(packed);
               int y = BlockPos.getY(packed);
               int z = BlockPos.getZ(packed);
               if (init(occupied, axis, x, y, z)) {
                  visible.add(new EspVoxelMeshBuilder.Inner_BufferHelper(axis, x, y, z, color));
               }
            }
         }

         init_V_List_List(visible, outputEdges);
      }
   }

   private static void init(LongOpenHashSet[] candidates, int x, int y, int z) {
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

   private static boolean init(LongOpenHashSet occupied, int axis, int x, int y, int z) {
      boolean a;
      boolean b;
      boolean c;
      boolean d;
      if (axis == 0) {
         a = init(occupied, x, y - 1, z - 1);
         b = init(occupied, x, y, z - 1);
         c = init(occupied, x, y - 1, z);
         d = init(occupied, x, y, z);
      } else if (axis == 1) {
         a = init(occupied, x - 1, y, z - 1);
         b = init(occupied, x, y, z - 1);
         c = init(occupied, x - 1, y, z);
         d = init(occupied, x, y, z);
      } else {
         a = init(occupied, x - 1, y - 1, z);
         b = init(occupied, x, y - 1, z);
         c = init(occupied, x - 1, y, z);
         d = init(occupied, x, y, z);
      }

      int count = (a ? 1 : 0) + (b ? 1 : 0) + (c ? 1 : 0) + (d ? 1 : 0);
      return count == 1 || count == 3 || count == 2 && (a && d || b && c);
   }

   private static void init_V_List_List(List<EspVoxelMeshBuilder.Inner_BufferHelper> input, List<EspVoxelMeshBuilder.Inner_StreamUtil> output) {
      input.sort((left, right) -> {
         int compared = Integer.compare(left.update(), right.update());
         if (compared != 0) {
            return compared;
         }

         compared = Integer.compare(left.init(), right.init());
         if (compared != 0) {
            return compared;
         }

         compared = Integer.compare(load(left), load(right));
         if (compared != 0) {
            return compared;
         }

         compared = Integer.compare(read(left), read(right));
         return compared != 0 ? compared : Integer.compare(init(left), init(right));
      });
      int index = 0;

      while (index < input.size()) {
         EspVoxelMeshBuilder.Inner_BufferHelper first = input.get(index++);
         int end = init(first) + 1;

         while (index < input.size()) {
            EspVoxelMeshBuilder.Inner_BufferHelper next = input.get(index);
            if (next.update() != first.update() || next.init() != first.init() || load(next) != load(first) || read(next) != read(first) || init(next) != end) {
               break;
            }

            end++;
            index++;
         }

         init(output, first, end);
      }
   }

   private static int init(EspVoxelMeshBuilder.Inner_BufferHelper edge) {
      return edge.init() == 0 ? edge.load() : (edge.init() == 1 ? edge.read() : edge.write());
   }

   private static int load(EspVoxelMeshBuilder.Inner_BufferHelper edge) {
      return edge.init() == 0 ? edge.read() : edge.load();
   }

   private static int read(EspVoxelMeshBuilder.Inner_BufferHelper edge) {
      return edge.init() == 2 ? edge.read() : edge.write();
   }

   private static void init(List<EspVoxelMeshBuilder.Inner_StreamUtil> output, EspVoxelMeshBuilder.Inner_BufferHelper edge, int end) {
      if (edge.init() == 0) {
         output.add(new EspVoxelMeshBuilder.Inner_StreamUtil(edge.load(), edge.read(), edge.write(), end, edge.read(), edge.write(), edge.update()));
      } else if (edge.init() == 1) {
         output.add(new EspVoxelMeshBuilder.Inner_StreamUtil(edge.load(), edge.read(), edge.write(), edge.load(), end, edge.write(), edge.update()));
      } else {
         output.add(new EspVoxelMeshBuilder.Inner_StreamUtil(edge.load(), edge.read(), edge.write(), edge.load(), edge.read(), end, edge.update()));
      }
   }

   private static boolean init(LongOpenHashSet occupied, int x, int y, int z) {
      return occupied.contains(BlockPos.asLong(x, y, z));
   }

   private static List<EspVoxelMeshBuilder.Inner_DataUtil> init_List(List<EspVoxelMeshBuilder.Inner_DataUtil> boxes) {
      if (boxes.size() < 2) {
         return boxes;
      }

      List<EspVoxelMeshBuilder.Inner_DataUtil> merged = new ArrayList<>(boxes);
      int passes = 0;

      boolean changed;
      do {
         int before = merged.size();
         List<EspVoxelMeshBuilder.Inner_DataUtil> var5 = init_List_List_I(merged, 0);
         List<EspVoxelMeshBuilder.Inner_DataUtil> var6 = init_List_List_I(var5, 1);
         merged = init_List_List_I(var6, 2);
         changed = merged.size() < before;
      } while (changed && ++passes < 12);

      return merged;
   }

   private static List<EspVoxelMeshBuilder.Inner_DataUtil> init_List_List_I(List<EspVoxelMeshBuilder.Inner_DataUtil> boxes, int axis) {
      if (boxes.size() < 2) {
         return boxes;
      }

      List<EspVoxelMeshBuilder.Inner_DataUtil> sorted = new ArrayList<>(boxes);
      sorted.sort(init(axis));
      List<EspVoxelMeshBuilder.Inner_DataUtil> output = new ArrayList<>(sorted.size());
      EspVoxelMeshBuilder.Inner_DataUtil current = sorted.getFirst();

      for (int i = 1; i < sorted.size(); i++) {
         EspVoxelMeshBuilder.Inner_DataUtil next = sorted.get(i);
         if (init(current, next, axis) && init_D_AABB_I(next.init_AABB(), axis) <= load(current.init_AABB(), axis) + 1.0E-7) {
            current = new EspVoxelMeshBuilder.Inner_DataUtil(init(current.init_AABB(), next.init_AABB(), axis), current.init_I());
         } else {
            output.add(current);
            current = next;
         }
      }

      output.add(current);
      return output;
   }

   private static Comparator<EspVoxelMeshBuilder.Inner_DataUtil> init(int axis) {
      return (left, right) -> {
         int compared = Integer.compare(left.init_I(), right.init_I());
         if (compared != 0) {
            return compared;
         }

         AABB a = left.init_AABB();
         AABB b = right.init_AABB();

         for (int candidate = 0; candidate < 3; candidate++) {
            if (candidate != axis) {
               compared = Double.compare(init_D_AABB_I(a, candidate), init_D_AABB_I(b, candidate));
               if (compared != 0) {
                  return compared;
               }

               compared = Double.compare(load(a, candidate), load(b, candidate));
               if (compared != 0) {
                  return compared;
               }
            }
         }

         compared = Double.compare(init_D_AABB_I(a, axis), init_D_AABB_I(b, axis));
         return compared != 0 ? compared : Double.compare(load(a, axis), load(b, axis));
      };
   }

   private static boolean init(EspVoxelMeshBuilder.Inner_DataUtil left, EspVoxelMeshBuilder.Inner_DataUtil right, int axis) {
      if (left.init_I() != right.init_I()) {
         return false;
      }

      for (int candidate = 0; candidate < 3; candidate++) {
         if (candidate != axis
            && (
               !init_Z_D_D(init_D_AABB_I(left.init_AABB(), candidate), init_D_AABB_I(right.init_AABB(), candidate))
                  || !init_Z_D_D(load(left.init_AABB(), candidate), load(right.init_AABB(), candidate))
            )) {
            return false;
         }
      }

      return true;
   }

   private static AABB init(AABB left, AABB right, int axis) {
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

   private static double init_D_AABB_I(AABB box, int axis) {
      return axis == 0 ? box.minX : (axis == 1 ? box.minY : box.minZ);
   }

   private static double load(AABB box, int axis) {
      return axis == 0 ? box.maxX : (axis == 1 ? box.maxY : box.maxZ);
   }

   private static boolean init_Z_D_D(double left, double right) {
      return Math.abs(left - right) <= 1.0E-7;
   }

   private static void init(AABB box, int color, List<EspVoxelMeshBuilder.Inner_ConfigStore> quads, List<EspVoxelMeshBuilder.Inner_StreamUtil> edges) {
      for (int face = 0; face < 6; face++) {
         init(quads, face, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color);
      }

      double x1 = box.minX;
      double y1 = box.minY;
      double z1 = box.minZ;
      double x2 = box.maxX;
      double y2 = box.maxY;
      double z2 = box.maxZ;
      edges.add(new EspVoxelMeshBuilder.Inner_StreamUtil(x1, y1, z1, x2, y1, z1, color));
      edges.add(new EspVoxelMeshBuilder.Inner_StreamUtil(x2, y1, z1, x2, y1, z2, color));
      edges.add(new EspVoxelMeshBuilder.Inner_StreamUtil(x2, y1, z2, x1, y1, z2, color));
      edges.add(new EspVoxelMeshBuilder.Inner_StreamUtil(x1, y1, z2, x1, y1, z1, color));
      edges.add(new EspVoxelMeshBuilder.Inner_StreamUtil(x1, y2, z1, x2, y2, z1, color));
      edges.add(new EspVoxelMeshBuilder.Inner_StreamUtil(x2, y2, z1, x2, y2, z2, color));
      edges.add(new EspVoxelMeshBuilder.Inner_StreamUtil(x2, y2, z2, x1, y2, z2, color));
      edges.add(new EspVoxelMeshBuilder.Inner_StreamUtil(x1, y2, z2, x1, y2, z1, color));
      edges.add(new EspVoxelMeshBuilder.Inner_StreamUtil(x1, y1, z1, x1, y2, z1, color));
      edges.add(new EspVoxelMeshBuilder.Inner_StreamUtil(x2, y1, z1, x2, y2, z1, color));
      edges.add(new EspVoxelMeshBuilder.Inner_StreamUtil(x2, y1, z2, x2, y2, z2, color));
      edges.add(new EspVoxelMeshBuilder.Inner_StreamUtil(x1, y1, z2, x1, y2, z2, color));
   }

   private static void init(List<EspVoxelMeshBuilder.Inner_ConfigStore> quads, int face, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
      switch (face) {
         case 0:
            quads.add(new EspVoxelMeshBuilder.Inner_ConfigStore(x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, color));
            break;
         case 1:
            quads.add(new EspVoxelMeshBuilder.Inner_ConfigStore(x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, color));
            break;
         case 2:
            quads.add(new EspVoxelMeshBuilder.Inner_ConfigStore(x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, color));
            break;
         case 3:
            quads.add(new EspVoxelMeshBuilder.Inner_ConfigStore(x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, color));
            break;
         case 4:
            quads.add(new EspVoxelMeshBuilder.Inner_ConfigStore(x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, color));
            break;
         default:
            quads.add(new EspVoxelMeshBuilder.Inner_ConfigStore(x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2, color));
      }
   }

   private record Inner_BufferHelper(int init, int load, int read, int write, int update) {
   }

   record Inner_CachePool(List<EspVoxelMeshBuilder.Inner_ConfigStore> init_List, List<EspVoxelMeshBuilder.Inner_StreamUtil> load) {
      private static final EspVoxelMeshBuilder.Inner_CachePool init_ag_CachePool = new EspVoxelMeshBuilder.Inner_CachePool(List.of(), List.of());
   }

   record Inner_ConfigStore(
      double init_D,
      double load,
      double read,
      double write,
      double update,
      double sync,
      double flush,
      double close,
      double open,
      double poll,
      double push,
      double pop,
      int init_I
   ) {
   }

   record Inner_DataUtil(AABB init_AABB, int init_I) {
   }

   record Inner_StreamUtil(double init_D, double load, double read, double write, double update, double sync, int init_I) {
   }
}
