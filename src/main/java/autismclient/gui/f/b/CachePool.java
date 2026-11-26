package autismclient.gui.f.b;

import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemeTextureRecolorer;
import net.minecraft.resources.Identifier;
import autismclient.gui.ui.widgets.UiLayoutMath;

public final class CachePool {
   private CachePool() {
   }

   public static void init(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, CachePool.Inner_DataUtil tone, boolean hovered, boolean active
   ) {
      load(context, bounds, label, null, tone, hovered, active, autismclient.gui.ui.ToggleTransitionCache.init(autismclient.gui.ui.ToggleTransitionCache.init(bounds), active));
   }

   public static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String label,
      Identifier icon,
      CachePool.Inner_DataUtil tone,
      boolean hovered,
      boolean active,
      float activeProgress
   ) {
      load(context, bounds, label, icon, tone, hovered, active, activeProgress);
   }

   private static void load(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String label,
      Identifier icon,
      CachePool.Inner_DataUtil tone,
      boolean hovered,
      boolean active,
      float activeProgress
   ) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      float progress = Math.min(1.0F, Math.max(0.0F, activeProgress));
      int var10000;
      switch (tone) {
         case read:
            var10000 = colors.accent;
            break;
         case write:
            var10000 = colors.success;
            break;
         case update:
            var10000 = colors.danger;
            break;
         default:
            var10000 = colors.borderControl;
      }

      int toneColor = var10000;
      boolean colored = tone == CachePool.Inner_DataUtil.read || tone == CachePool.Inner_DataUtil.write || tone == CachePool.Inner_DataUtil.update;
      float hoverT = autismclient.gui.ui.HoverTransitionCache.init(autismclient.gui.ui.HoverTransitionCache.init(bounds), hovered);
      int fill = UiLayoutMath.lerpColor(colors.buttonSurface, toneColor, colored ? 0.08F + 0.14F * progress : 0.12F * progress);
      int border = colored ? UiLayoutMath.lerpColor(fill, toneColor, 0.5F + 0.5F * progress) : colors.borderControl;
      fill = UiLayoutMath.lerpColor(fill, colors.buttonSurfaceHover, hoverT * 0.6F);
      border = UiLayoutMath.lerpColor(border, colored ? toneColor : colors.borderStrong, hoverT * 0.65F);
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, 3, fill, border);
      if (icon == null) {
         context.init_PoolEntry().drawCentered(context.init_GuiGraphicsExtract(), label, bounds, colors.textPrimary);
      } else {
         int iconSize = Math.min(12, Math.max(8, bounds.sync() - 5));
         int textW = context.init_PoolEntry().measureWidth(label);
         int groupW = iconSize + 4 + textW;
         int iconX = bounds.read() + Math.max(3, (bounds.update() - groupW) / 2);
         int iconY = bounds.write() + Math.max(1, (bounds.sync() - iconSize) / 2);
         if (!autismclient.gui.sdf.SdfIconRenderer.init(context.init_GuiGraphicsExtract(), icon, iconX, iconY, iconSize, iconSize, colors.textPrimary)) {
            context.init_GuiGraphicsExtract()
               .blit(ThemeTextureRecolorer.init(icon, ThemePaletteManager.ColorRole.ACCENT), iconX, iconY, iconX + iconSize, iconY + iconSize, 0.0F, 1.0F, 0.0F, 1.0F);
         }

         context.init_PoolEntry()
            .drawFitted(
               context.init_GuiGraphicsExtract(),
               label,
               iconX + iconSize + 4,
               context.init_PoolEntry().centerTextY(bounds),
               Math.max(1, bounds.init() - (iconX + iconSize + 4) - 4),
               colors.textPrimary
            );
      }
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update;
   }
}
