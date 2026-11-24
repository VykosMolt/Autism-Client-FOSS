package autismclient.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class PkgCls_g {
   private static final long init_J = 500L;
   private static final int init_I = 6;
   private static final int load_I = 10;
   private static final Map<Integer, Long> init_Map = new ConcurrentHashMap<>();
   private static final Map<Integer, PkgCls_g.Inner_DataUtil> load_Map = new ConcurrentHashMap<>();

   private PkgCls_g() {
   }

   public static void init_V(Entity entity) {
      if (entity instanceof LivingEntity living) {
         load_Map.put(living.getId(), new PkgCls_g.Inner_DataUtil(living.hurtTime, 6));
      }
   }

   public static void load(Entity entity) {
      if (entity != null) {
         init_Map.put(entity.getId(), System.currentTimeMillis());
      }
   }

   public static void init() {
      if (!load_Map.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.level != null) {
            Iterator<Entry<Integer, PkgCls_g.Inner_DataUtil>> it = load_Map.entrySet().iterator();

            while (it.hasNext()) {
               Entry<Integer, PkgCls_g.Inner_DataUtil> e = it.next();
               Entity entity = mc.level.getEntity(e.getKey());
               PkgCls_g.Inner_DataUtil p = e.getValue();
               boolean landed = entity instanceof LivingEntity living && (living.hurtTime > p.init() || p.init() >= 10 && living.hurtTime >= 10);
               if (landed) {
                  init_Map.put(e.getKey(), System.currentTimeMillis());
                  it.remove();
               } else if (entity != null && p.load() > 1) {
                  e.setValue(new PkgCls_g.Inner_DataUtil(p.init(), p.load() - 1));
               } else {
                  it.remove();
               }
            }
         } else {
            load_Map.clear();
         }
      }
   }

   public static boolean init_Z(Entity entity) {
      if (entity == null) {
         return false;
      }

      Long at = init_Map.get(entity.getId());
      if (at == null) {
         return false;
      }

      if (System.currentTimeMillis() - at < 500L) {
         return true;
      }

      init_Map.remove(entity.getId());
      return false;
   }

   private record Inner_DataUtil(int init, int load) {
   }
}
