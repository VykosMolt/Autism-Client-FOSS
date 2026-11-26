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
   private static final List<AABB> init_List = List.of(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
   private static final List<List<AABB>> load_List = new CopyOnWriteArrayList<>();
   private static final ConcurrentHashMap<List<AABB>, Short> init_ConcurrentHashMap = new ConcurrentHashMap<>();

   private AiShapes() {
   }

   public static short init_S_List(List<AABB> boxes) {
      if (boxes != null && !boxes.isEmpty()) {
         List<AABB> key = List.copyOf(boxes);
         if (key.size() == 1 && init_Z_AABB(key.get(0))) {
            return 1;
         }

         Short known = init_ConcurrentHashMap.get(key);
         if (known != null) {
            return known;
         }

         synchronized (load_List) {
            known = init_ConcurrentHashMap.get(key);
            if (known != null) {
               return known;
            }

            if (load_List.size() >= 8192) {
               return 1;
            }

            short id = (short)load_List.size();
            load_List.add(key);
            init_ConcurrentHashMap.put(key, id);
            return id;
         }
      } else {
         return 0;
      }
   }

   public static List<AABB> init_List_S(short id) {
      return id >= 0 && id < load_List.size() ? load_List.get(id) : init_List;
   }

   public static double init_D_S(short id) {
      double top = 0.0;

      for (AABB b : init_List_S(id)) {
         top = Math.max(top, b.maxY);
      }

      return top;
   }

   public static short init_S_I(int top16) {
      if (top16 <= 0) {
         return 0;
      } else {
         return top16 >= 16 ? 1 : init_S_List(List.of(new AABB(0.0, 0.0, 0.0, 1.0, top16 / 16.0, 1.0)));
      }
   }

   public static short init(boolean loaded, boolean passable, int top16) {
      if (!loaded) {
         return 1;
      } else {
         return passable ? 0 : init_S_I(top16);
      }
   }

   public static short init_S_ClassCache_FileCac(AiPathfinder.Inner_FileCache probe) {
      return probe == null ? -1 : init(probe.loaded(), probe.passable(), probe.top16());
   }

   private static boolean init_Z_AABB(AABB b) {
      return b.minX <= 1.0E-6 && b.minY <= 1.0E-6 && b.minZ <= 1.0E-6 && b.maxX >= 0.999999 && b.maxY >= 0.999999 && b.maxZ >= 0.999999;
   }

   static {
      load_List.add(List.of());
      load_List.add(init_List);
      init_ConcurrentHashMap.put(load_List.get(0), (short)0);
      init_ConcurrentHashMap.put(load_List.get(1), (short)1);
   }
}
