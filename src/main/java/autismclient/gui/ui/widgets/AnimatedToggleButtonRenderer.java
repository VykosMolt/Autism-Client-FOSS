package autismclient.gui.ui.widgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class AnimatedToggleButtonRenderer {
   private static final int init_I = 40;
   private static final int load_I = 96;
   private static final long init_J = 205000000L;
   private static final long load_J = 128000000L;
   private static final long read = 200000000L;
   private static final long write = 30000000000L;
   private static final Map<String, AnimatedToggleButtonRenderer.Inner_DataUtil> init_Map = new HashMap<>();
   private static long update;

   private AnimatedToggleButtonRenderer() {
   }

   public static void init(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, boolean enabled, boolean hovered, String animationKey
   ) {
      long now = System.nanoTime();
      AnimatedToggleButtonRenderer.Inner_DataUtil state = init_Map.get(animationKey);
      if (state == null) {
         state = new AnimatedToggleButtonRenderer.Inner_DataUtil(enabled ? 1.0F : 0.0F, enabled, now);
         init_Map.put(animationKey, state);
      } else if (state.targetEnabled != enabled) {
         state.startProgress = state.init(now, 200000000L);
         state.targetEnabled = enabled;
         state.init_J = now;
      }

      state.load = now;
      init(context, bounds, label, hovered, state.init(now, 200000000L));
      pruneStale(now);
   }

   private static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, boolean hovered, float progress) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      float p = clamp01(progress);
      int fill = UiLayoutMath.lerpColor(colors.buttonSurface, colors.success, 0.22F * p);
      int border = UiLayoutMath.lerpColor(colors.borderControl, colors.success, p);
      float hoverT = autismclient.gui.ui.HoverTransitionCache.sample(autismclient.gui.ui.HoverTransitionCache.boundsKey(bounds), hovered);
      fill = UiLayoutMath.lerpColor(fill, colors.buttonSurfaceHover, hoverT * 0.6F);
      border = UiLayoutMath.lerpColor(border, p > 0.5F ? colors.success : colors.borderStrong, hoverT * 0.65F);
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, 3, fill, border);
      if (label != null && !label.isEmpty()) {
         context.init_PoolEntry().drawCentered(context.init_GuiGraphicsExtract(), label, bounds, colors.textPrimary);
      }
   }

   public static long init_J_I(int width) {
      float t = clamp01((float)(width - 40) / Math.max(1, 56));
      return Math.round(2.05E8F + -7.7E7F * t);
   }

   private static void pruneStale(long now) {
      if (now - update >= 30000000000L) {
         update = now;
         Iterator<AnimatedToggleButtonRenderer.Inner_DataUtil> iterator = init_Map.values().iterator();

         while (iterator.hasNext()) {
            if (now - iterator.next().load > 30000000000L) {
               iterator.remove();
            }
         }
      }
   }

   private static float clamp01(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }

   private static final class Inner_DataUtil {
      private float startProgress;
      private boolean targetEnabled;
      private long init_J;
      private long load;

      private Inner_DataUtil(float startProgress, boolean targetEnabled, long now) {
         this.startProgress = startProgress;
         this.targetEnabled = targetEnabled;
         this.init_J = now;
         this.load = now;
      }

      private float init(long now, long durationNanos) {
         float elapsed = Math.min(1.0F, (float)(now - this.init_J) / (float)Math.max(1L, durationNanos));
         float eased = elapsed * elapsed * (3.0F - 2.0F * elapsed);
         float target = this.targetEnabled ? 1.0F : 0.0F;
         return this.startProgress + (target - this.startProgress) * eased;
      }
   }
}
