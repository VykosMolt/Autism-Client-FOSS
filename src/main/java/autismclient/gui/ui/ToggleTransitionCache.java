package autismclient.gui.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import autismclient.gui.f.NetHelper;

public final class ToggleTransitionCache {
   private static final long init_J = 150000000L;
   private static final long load = 30000000000L;
   private static final Map<String, ToggleTransitionCache.Inner_DataUtil> STATES = new HashMap<>();
   private static long lastPruneNanos;

   private ToggleTransitionCache() {
   }

   public static float init(String key, boolean on) {
      long now = System.nanoTime();
      ToggleTransitionCache.Inner_DataUtil state = STATES.get(key);
      if (state == null) {
         state = new ToggleTransitionCache.Inner_DataUtil(on ? 1.0F : 0.0F, on, now);
         STATES.put(key, state);
         return state.load_F;
      }

      if (state.init_Z != on) {
         state.init_F = state.progress(now);
         state.init_Z = on;
         state.init_J = now;
      }

      state.load_J = now;
      if (now - lastPruneNanos > 30000000000L) {
         lastPruneNanos = now;
         Iterator<ToggleTransitionCache.Inner_DataUtil> it = STATES.values().iterator();

         while (it.hasNext()) {
            if (now - it.next().load_J > 30000000000L) {
               it.remove();
            }
         }
      }

      return state.progress(now);
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

      float progress(long now) {
         float elapsed = Math.min(1.0F, (float)(now - this.init_J) / 1.5E8F);
         float eased = elapsed * elapsed * (3.0F - 2.0F * elapsed);
         this.load_F = this.init_F + ((this.init_Z ? 1.0F : 0.0F) - this.init_F) * eased;
         return this.load_F;
      }
   }
}
