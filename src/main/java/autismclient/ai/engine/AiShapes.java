package autismclient.ai.engine;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.world.phys.AABB;

public final class AiShapes {
   public static final short init_S = 0;
   public static final short load_S = 1;
   public static final short read = -1;
   private static final int init_I = 8192;
   private static final List<AABB> FULL_BLOCK = List.of(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
   private static final List<List<AABB>> shapesById = new CopyOnWriteArrayList<>();
   private static final ConcurrentHashMap<List<AABB>, Short> idsByShape = new ConcurrentHashMap<>();

   private AiShapes() {
   }

   public static short shapeId(List<AABB> boxes) {
      if (boxes != null && !boxes.isEmpty()) {
         List<AABB> key = List.copyOf(boxes);
         if (key.size() == 1 && isFullCube(key.get(0))) {
            return 1;
         }

         Short known = idsByShape.get(key);
         if (known != null) {
            return known;
         }

         synchronized (shapesById) {
            known = idsByShape.get(key);
            if (known != null) {
               return known;
            }

            if (shapesById.size() >= 8192) {
               return 1;
            }

            short id = (short)shapesById.size();
            shapesById.add(key);
            idsByShape.put(key, id);
            return id;
         }
      } else {
         return 0;
      }
   }

   public static List<AABB> boxes(short id) {
      return id >= 0 && id < shapesById.size() ? shapesById.get(id) : FULL_BLOCK;
   }

   public static double topOf(short id) {
      double top = 0.0;

      for (AABB b : boxes(id)) {
         top = Math.max(top, b.maxY);
      }

      return top;
   }

   public static short heightShapeId(int top16) {
      if (top16 <= 0) {
         return 0;
      } else {
         return top16 >= 16 ? 1 : shapeId(List.of(new AABB(0.0, 0.0, 0.0, 1.0, top16 / 16.0, 1.0)));
      }
   }

   public static short shapeId(boolean loaded, boolean passable, int top16) {
      if (!loaded) {
         return 1;
      } else {
         return passable ? 0 : heightShapeId(top16);
      }
   }

   public static short shapeIdOf(AiPathfinder.CellInfo probe) {
      return probe == null ? -1 : shapeId(probe.loaded(), probe.passable(), probe.top16());
   }

   private static boolean isFullCube(AABB b) {
      return b.minX <= 1.0E-6 && b.minY <= 1.0E-6 && b.minZ <= 1.0E-6 && b.maxX >= 0.999999 && b.maxY >= 0.999999 && b.maxZ >= 0.999999;
   }

   static {
      shapesById.add(List.of());
      shapesById.add(FULL_BLOCK);
      idsByShape.put(shapesById.get(0), (short)0);
      idsByShape.put(shapesById.get(1), (short)1);
   }
}
