package autismclient.util;

import autismclient.modules.Module;
import autismclient.modules.j;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ah {
   public static final int init_I = 0;
   public static final int load_I = 100;
   public static final int read_I = 40;
   private static final Set<String> init_Set = Set.of("auto-totem", "clutch");
   private static final Object init_Obj = new Object();
   private static final Map<Integer, String> init_Map = new HashMap<>();
   private static String init_Str;
   private static String load_Str;
   private static int write_I;
   private static boolean init_Z;
   private static String read_Str;
   private static String write_Str;
   private static int update;

   private ah() {
   }

   public static int init_I(String moduleId) {
      String id = init_Str(moduleId);
      return id != null && init_Set.contains(id) ? 100 : 0;
   }

   public static boolean init_Z(String moduleId) {
      String id = init_Str(moduleId);
      if (id == null) {
         return false;
      }

      synchronized (init_Obj) {
         String owner = write();
         if (owner != null && !owner.equals(id) && init_I(id) <= init_I(owner)) {
            return false;
         }

         init_Str = id;
         return true;
      }
   }

   public static String init() {
      synchronized (init_Obj) {
         return write();
      }
   }

   public static boolean load_Z(String moduleId) {
      String id = init_Str(moduleId);
      synchronized (init_Obj) {
         String owner = write();
         return owner != null && !owner.equals(id);
      }
   }

   public static void init_V(String moduleId) {
      String id = init_Str(moduleId);
      if (id != null) {
         synchronized (init_Obj) {
            if (id.equals(init_Str)) {
               init_Str = null;
            }
         }
      }
   }

   public static boolean init_Z(String moduleId, int inventorySlot) {
      String id = init_Str(moduleId);
      if (id != null && init_Z(inventorySlot)) {
         synchronized (init_Obj) {
            String owner = load(inventorySlot);
            if (owner != null && !owner.equals(id) && init_I(id) <= init_I(owner)) {
               return false;
            }

            init_Map.values().removeIf(id::equals);
            init_Map.put(inventorySlot, id);
            return true;
         }
      } else {
         return false;
      }
   }

   public static void init_V(String moduleId, int inventorySlot) {
      String id = init_Str(moduleId);
      if (id != null && init_Z(inventorySlot)) {
         synchronized (init_Obj) {
            if (id.equals(init_Map.get(inventorySlot))) {
               init_Map.remove(inventorySlot);
            }
         }
      }
   }

   public static void load_V(String moduleId) {
      String id = init_Str(moduleId);
      if (id != null) {
         synchronized (init_Obj) {
            init_Map.values().removeIf(id::equals);
         }
      }
   }

   public static boolean init(int inventorySlot, String exceptModuleId) {
      if (!init_Z(inventorySlot)) {
         return false;
      }

      String id = init_Str(exceptModuleId);
      synchronized (init_Obj) {
         String owner = load(inventorySlot);
         return owner != null && !owner.equals(id);
      }
   }

   public static String init_Str(int inventorySlot) {
      if (!init_Z(inventorySlot)) {
         return null;
      }

      synchronized (init_Obj) {
         return load(inventorySlot);
      }
   }

   public static boolean read_Z(String moduleId) {
      String id = init_Str(moduleId);
      if (id == null) {
         return false;
      }

      if (AutismLiteVariant.enabled() || !j.init_Z_49() || "clutch".equals(id) || "auto-totem".equals(id) || "danger".equals(id) && j.open_Z_5()) {
         if (!AutismLiteVariant.enabled() && autismclient.modules.z.sync_Z_12() && !"danger".equals(id) && !"clutch".equals(id) && !"auto-totem".equals(id)) {
            return false;
         }

         synchronized (init_Obj) {
            return load(id, ck.init_ck().state_I());
         }
      } else {
         return false;
      }
   }

   public static boolean write_Z(String moduleId) {
      String id = init_Str(moduleId);
      if (id == null) {
         return false;
      }

      if (AutismLiteVariant.enabled() || !j.init_Z_49() || "clutch".equals(id) || "auto-totem".equals(id) || "danger".equals(id) && j.open_Z_5()) {
         if (!AutismLiteVariant.enabled() && autismclient.modules.z.sync_Z_12() && !"danger".equals(id) && !"clutch".equals(id) && !"auto-totem".equals(id)) {
            return false;
         }

         synchronized (init_Obj) {
            int tick = ck.init_ck().state_I();
            if (!load(id, tick)) {
               return false;
            }

            load_Str = id;
            write_I = tick;
            init_Z = true;
            return true;
         }
      } else {
         return false;
      }
   }

   public static void read_V(String moduleId) {
      String id = init_Str(moduleId);
      if (id != null) {
         synchronized (init_Obj) {
            if (id.equals(load_Str)) {
               init_Z = false;
            }
         }
      }
   }

   public static String load() {
      synchronized (init_Obj) {
         if (init_Z && load_Str != null) {
            return write_I == ck.init_ck().state_I() ? load_Str : null;
         } else {
            return null;
         }
      }
   }

   public static boolean update_Z(String moduleId) {
      String id = init_Str(moduleId);
      if (id == null) {
         return false;
      }

      synchronized (init_Obj) {
         String owner = read();
         if (owner != null && !owner.equals(id) && init_I(id) <= init_I(owner)) {
            return false;
         }

         read_Str = id;
         return true;
      }
   }

   public static void write_V(String moduleId) {
      String id = init_Str(moduleId);
      if (id != null) {
         synchronized (init_Obj) {
            if (id.equals(read_Str)) {
               read_Str = null;
               write_Str = id;
               update = ck.init_ck().state_I();
            }
         }
      }
   }

   public static boolean sync(String moduleId) {
      String id = init_Str(moduleId);
      synchronized (init_Obj) {
         if (write_Str == null) {
            return false;
         } else {
            int tick = ck.init_ck().state_I();
            if (tick != update && tick != update + 1) {
               write_Str = null;
               return false;
            } else {
               return !write_Str.equals(id);
            }
         }
      }
   }

   public static void update_V(String moduleId) {
      String id = init_Str(moduleId);
      if (id != null) {
         synchronized (init_Obj) {
            if (id.equals(init_Str)) {
               init_Str = null;
            }

            init_Map.values().removeIf(id::equals);
            if (id.equals(load_Str)) {
               load_Str = null;
               init_Z = false;
            }

            if (id.equals(read_Str)) {
               read_Str = null;
               write_Str = id;
               update = ck.init_ck().state_I();
            }
         }
      }
   }

   private static boolean load(String id, int tick) {
      return init(id, tick, load_Str, write_I, init_Z);
   }

   static boolean init(String id, int tick, String groupOwner, int groupTick, boolean groupOpen) {
      if (groupOwner == null || groupTick != tick) {
         return true;
      } else if (id.equals(groupOwner)) {
         return true;
      } else if (groupOpen) {
         return false;
      } else {
         return !"danger".equals(id) && !"clutch".equals(id) ? init_I(id) > init_I(groupOwner) : true;
      }
   }

   private static String read() {
      if (read_Str != null && !flush(read_Str)) {
         read_Str = null;
      }

      return read_Str;
   }

   private static String write() {
      if (init_Str != null && !flush(init_Str)) {
         init_Str = null;
      }

      return init_Str;
   }

   private static String load(int inventorySlot) {
      String owner = init_Map.get(inventorySlot);
      if (owner == null) {
         return null;
      } else if (!flush(owner)) {
         init_Map.remove(inventorySlot);
         return null;
      } else {
         return owner;
      }
   }

   private static boolean flush(String moduleId) {
      Module module = autismclient.modules.al.init_Module_Str(moduleId);
      return module != null && module.isEnabled();
   }

   private static boolean init_Z(int inventorySlot) {
      return inventorySlot >= 0 && inventorySlot < 41;
   }

   private static String init_Str(String moduleId) {
      if (moduleId == null) {
         return null;
      }

      String trimmed = moduleId.trim();
      return trimmed.isEmpty() ? null : trimmed;
   }
}
