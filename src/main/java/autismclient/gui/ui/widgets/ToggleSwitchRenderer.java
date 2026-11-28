package autismclient.gui.ui.widgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ToggleSwitchRenderer {
   private static final long init_J = 150000000L;
   private static final long load_J = 30000000000L;
   private static final int init_I = -12961216;
   private static final int load_I = -1447444;
   private static final Map<String, ToggleSwitchRenderer.SwitchAnimation> STATES = new HashMap<>();
   private static long read;

   private ToggleSwitchRenderer() {
   }

   public static void draw(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, boolean enabled, boolean hovered) {
      draw(context, bounds, enabled, hovered, bounds.read() + ":" + bounds.write() + ":" + bounds.update());
   }

   public static void draw(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, boolean enabled, boolean hovered, String animationKey) {
      drawAt(context, bounds, init(animationKey, enabled), hovered);
   }

   public static void drawAt(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, float progress, boolean hovered) {
      float eased = progress * progress * (3.0F - 2.0F * progress);
      GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
      int track = init(-12961216, context.init_TableIndex().colors().accent, eased);
      if (hovered) {
         track = init(track, 0.12F);
      }

      autismclient.gui.f.PacketUtil.load(graphics, bounds, bounds.sync() / 2, track);
      float r = Math.max(1.5F, bounds.sync() / 2.0F - 1.0F);
      float cxLeft = bounds.read() + 1.0F + r;
      float cxRight = bounds.init() - 1.0F - r;
      float cx = cxLeft + (cxRight - cxLeft) * eased;
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(graphics, cx, bounds.write() + bounds.sync() / 2.0F, r, -1447444);
   }

   public static void init(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, boolean enabled, boolean hovered, String animationKey
   ) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      int pillW = Math.min(26, Math.max(20, bounds.sync() * 2 - 4));
      int pillH = Math.min(12, Math.max(9, bounds.sync() - 6));
      autismclient.gui.f.NetHelper pill = autismclient.gui.f.NetHelper.init(
         bounds.init() - pillW - 2, bounds.write() + Math.max(0, (bounds.sync() - pillH) / 2), pillW, pillH
      );
      context.init_PoolEntry()
         .drawFitted(
            context.init_GuiGraphicsExtract(),
            label == null ? "" : label,
            bounds.read() + 5,
            context.init_PoolEntry().centerTextY(bounds),
            Math.max(1, pill.read() - bounds.read() - 9),
            enabled ? colors.textPrimary : colors.textSecondary
         );
      draw(context, pill, enabled, hovered, animationKey);
   }

   private static float init(String key, boolean target) {
      float t = load(key, target);
      return t * t * (3.0F - 2.0F * t);
   }

   private static float load(String key, boolean target) {
      long now = System.nanoTime();
      ToggleSwitchRenderer.SwitchAnimation state = STATES.get(key);
      if (state == null) {
         state = new ToggleSwitchRenderer.SwitchAnimation(target ? 1.0F : 0.0F, target, now);
         STATES.put(key, state);
         return state.init(now);
      }

      if (state.init_Z != target) {
         state.init_F = state.init(now);
         state.init_Z = target;
         state.init_J = now;
      }

      state.load = now;
      if (now - read > 30000000000L) {
         read = now;
         Iterator<ToggleSwitchRenderer.SwitchAnimation> it = STATES.values().iterator();

         while (it.hasNext()) {
            if (now - it.next().load > 30000000000L) {
               it.remove();
            }
         }
      }

      return state.init(now);
   }

   private static int init(int from, int to, float t) {
      int ar = from >> 16 & 0xFF;
      int ag = from >> 8 & 0xFF;
      int ab = from & 0xFF;
      int aa = from >>> 24 & 0xFF;
      int br = to >> 16 & 0xFF;
      int bg = to >> 8 & 0xFF;
      int bb = to & 0xFF;
      int ba = to >>> 24 & 0xFF;
      int r = Math.round(ar + (br - ar) * t);
      int g = Math.round(ag + (bg - ag) * t);
      int b = Math.round(ab + (bb - ab) * t);
      int a = Math.round(aa + (ba - aa) * t);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static int init(int color, float amount) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      r += Math.round((255 - r) * amount);
      g += Math.round((255 - g) * amount);
      b += Math.round((255 - b) * amount);
      return color & 0xFF000000 | r << 16 | g << 8 | b;
   }

   private static final class SwitchAnimation {
      float init_F;
      boolean init_Z;
      long init_J;
      long load;

      SwitchAnimation(float start, boolean target, long now) {
         this.init_F = start;
         this.init_Z = target;
         this.init_J = now;
         this.load = now;
      }

      float init(long now) {
         float elapsed = Math.min(1.0F, (float)(now - this.init_J) / 1.5E8F);
         float eased = elapsed * elapsed * (3.0F - 2.0F * elapsed);
         return this.init_F + ((this.init_Z ? 1.0F : 0.0F) - this.init_F) * eased;
      }
   }
}
