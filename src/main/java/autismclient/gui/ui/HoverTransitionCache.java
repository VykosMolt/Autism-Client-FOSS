package autismclient.gui.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import autismclient.gui.f.NetHelper;

public final class HoverTransitionCache {
   private static final long TRANSITION_DURATION_NANOS = 110000000L;
   private static final long ENTRY_TTL_NANOS = 30000000000L;
   private static final Map<String, HoverTransitionCache.HoverTransition> STATES = new HashMap<>();
   private static long lastPruneNanos;

   private HoverTransitionCache() {
   }

   public static float sample(String key, boolean hovered) {
      long now = System.nanoTime();
      HoverTransitionCache.HoverTransition state = STATES.get(key);
      if (state == null) {
         state = new HoverTransitionCache.HoverTransition(hovered ? 1.0F : 0.0F, hovered, now);
         STATES.put(key, state);
         return state.currentValue;
      }

      if (state.targetHovered != hovered) {
         state.startValue = state.sample(now);
         state.targetHovered = hovered;
         state.transitionStartNanos = now;
      }

      state.lastAccessNanos = now;
      if (now - lastPruneNanos > 30000000000L) {
         lastPruneNanos = now;
         Iterator<HoverTransitionCache.HoverTransition> it = STATES.values().iterator();

         while (it.hasNext()) {
            if (now - it.next().lastAccessNanos > 30000000000L) {
               it.remove();
            }
         }
      }

      return state.sample(now);
   }

   public static String boundsKey(NetHelper bounds) {
      return bounds.read() + ":" + bounds.write() + ":" + bounds.update() + ":" + bounds.sync();
   }

   private static final class HoverTransition {
      float startValue;
      float currentValue;
      boolean targetHovered;
      long transitionStartNanos;
      long lastAccessNanos;

      HoverTransition(float value, boolean target, long now) {
         this.currentValue = value;
         this.startValue = value;
         this.targetHovered = target;
         this.transitionStartNanos = now;
         this.lastAccessNanos = now;
      }

      float sample(long now) {
         float elapsed = Math.min(1.0F, (float)(now - this.transitionStartNanos) / 1.1E8F);
         float eased = elapsed * elapsed * (3.0F - 2.0F * elapsed);
         this.currentValue = this.startValue + ((this.targetHovered ? 1.0F : 0.0F) - this.startValue) * eased;
         return this.currentValue;
      }
   }
}
