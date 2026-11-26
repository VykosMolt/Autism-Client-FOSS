package autismclient.gui.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import autismclient.gui.f.NetHelper;

public final class HoverTransitionCache {
   private static final long init_J = 110000000L;
   private static final long load = 30000000000L;
   private static final Map<String, HoverTransitionCache.Inner_DataUtil> init_Map = new HashMap<>();
   private static long read;

   private HoverTransitionCache() {
   }

   public static float init(String key, boolean hovered) {
      long now = System.nanoTime();
      HoverTransitionCache.Inner_DataUtil state = init_Map.get(key);
      if (state == null) {
         state = new HoverTransitionCache.Inner_DataUtil(hovered ? 1.0F : 0.0F, hovered, now);
         init_Map.put(key, state);
         return state.load_F;
      }

      if (state.init_Z != hovered) {
         state.init_F = state.init(now);
         state.init_Z = hovered;
         state.init_J = now;
      }

      state.load_J = now;
      if (now - read > 30000000000L) {
         read = now;
         Iterator<HoverTransitionCache.Inner_DataUtil> it = init_Map.values().iterator();

         while (it.hasNext()) {
            if (now - it.next().load_J > 30000000000L) {
               it.remove();
            }
         }
      }

      return state.init(now);
   }

   public static String init(NetHelper bounds) {
      return bounds.read() + ":" + bounds.write() + ":" + bounds.update() + ":" + bounds.sync();
   }

   private static final class Inner_DataUtil {
      float init_F;
      float load_F;
      boolean init_Z;
      long init_J;
      long load_J;

      Inner_DataUtil(float value, boolean target, long now) {
         this.load_F = value;
         this.init_F = value;
         this.init_Z = target;
         this.init_J = now;
         this.load_J = now;
      }

      float init(long now) {
         float elapsed = Math.min(1.0F, (float)(now - this.init_J) / 1.1E8F);
         float eased = elapsed * elapsed * (3.0F - 2.0F * elapsed);
         this.load_F = this.init_F + ((this.init_Z ? 1.0F : 0.0F) - this.init_F) * eased;
         return this.load_F;
      }
   }
}
