package autismclient.gui.ui.widgets;

import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemeTextureRecolorer;
import net.minecraft.resources.Identifier;
import autismclient.gui.f.b.CachePool;

public final class ConnectedButtonRenderer {
   public static final ConnectedButtonRenderer.ConnectedEdges init = new ConnectedButtonRenderer.ConnectedEdges(true, true, true, true);
   public static final ConnectedButtonRenderer.ConnectedEdges load = new ConnectedButtonRenderer.ConnectedEdges(true, true, false, true);
   public static final ConnectedButtonRenderer.ConnectedEdges read = new ConnectedButtonRenderer.ConnectedEdges(false, true, true, true);
   public static final ConnectedButtonRenderer.ConnectedEdges write = new ConnectedButtonRenderer.ConnectedEdges(false, false, false, false, true);

   public static ConnectedButtonRenderer.ConnectedEdges edges(boolean left, boolean right) {
      return new ConnectedButtonRenderer.ConnectedEdges(left, false, right, true, true);
   }

   private ConnectedButtonRenderer() {
   }

   public static void render(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String label,
      Identifier icon,
      CachePool.Inner_DataUtil tone,
      boolean hovered,
      ConnectedButtonRenderer.ConnectedEdges edges
   ) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      int var10000;
      switch (tone) {
         case read:
            var10000 = ThemePaletteManager.recolor(-870181350, ThemePaletteManager.ColorRole.BUTTON);
            break;
         case write:
            var10000 = ThemePaletteManager.recolor(-871354339, ThemePaletteManager.ColorRole.SUCCESS);
            break;
         case update:
            var10000 = ThemePaletteManager.recolor(-868936937, ThemePaletteManager.ColorRole.DANGER);
            break;
         case init:
         case load:
            var10000 = -1156179423;
            break;
         default:
            throw new MatchException(null, null);
      }

      int fill = var10000;
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
         case init:
         case load:
            var10000 = colors.borderControl;
            break;
         default:
            throw new MatchException(null, null);
      }

      int border = var10000;
      init(context, bounds, label, icon, hovered, fill, border, edges);
   }

   public static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String label,
      Identifier icon,
      boolean hovered,
      float enabledProgress,
      ConnectedButtonRenderer.ConnectedEdges edges
   ) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      float progress = clamp01(enabledProgress);
      init(context, bounds, -1156179423, colors.borderControl, edges);
      init(context, bounds, hovered, edges);
      int pillH = Math.min(12, Math.max(9, bounds.sync() - 6));
      int pillW = Math.min(26, Math.max(20, pillH * 2 - 2));
      autismclient.gui.f.NetHelper pill = autismclient.gui.f.NetHelper.init(
         bounds.init() - pillW - 5, bounds.write() + Math.max(1, (bounds.sync() - pillH) / 2), pillW, pillH
      );
      int maxLabel = Math.max(1, pill.read() - (bounds.read() + 5) - 6);
      if (icon == null) {
         int drawW = Math.min(context.init_PoolEntry().measureWidth(label), maxLabel);
         int labelX = bounds.read() + Math.max(3, (pill.read() - 4 - bounds.read() - drawW) / 2);
         context.init_PoolEntry().drawFitted(context.init_GuiGraphicsExtract(), label, labelX, context.init_PoolEntry().centerTextY(bounds), drawW, colors.textPrimary);
      } else {
         int iconSize = Math.min(12, Math.max(8, bounds.sync() - 5));
         int drawW = Math.min(context.init_PoolEntry().measureWidth(label), maxLabel);
         int groupW = iconSize + 4 + drawW;
         int iconX = bounds.read() + Math.max(3, (pill.read() - 4 - bounds.read() - groupW) / 2);
         int iconY = bounds.write() + Math.max(1, (bounds.sync() - iconSize) / 2);
         context.init_GuiGraphicsExtract()
            .blit(ThemeTextureRecolorer.recolored(icon, ThemePaletteManager.ColorRole.ACCENT), iconX, iconY, iconX + iconSize, iconY + iconSize, 0.0F, 1.0F, 0.0F, 1.0F);
         context.init_PoolEntry()
            .drawFitted(
               context.init_GuiGraphicsExtract(),
               label,
               iconX + iconSize + 4,
               context.init_PoolEntry().centerTextY(bounds),
               Math.max(1, pill.read() - (iconX + iconSize + 4) - 4),
               colors.textPrimary
            );
      }

      ToggleSwitchRenderer.drawAt(context, pill, progress, hovered);
   }

   public static void drawIconButton(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, Identifier icon) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      autismclient.gui.f.PacketUtil.init(context.init_GuiGraphicsExtract(), bounds, -1441458403);
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(
         context.init_GuiGraphicsExtract(), bounds.read(), bounds.load() - 1, bounds.update(), colors.divider
      );
      init(context, bounds, label, icon, colors.textSecondary);
   }

   private static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String label,
      Identifier icon,
      boolean hovered,
      int fill,
      int border,
      ConnectedButtonRenderer.ConnectedEdges edges
   ) {
      init(context, bounds, fill, border, edges);
      init(context, bounds, hovered, edges);
      load(context, bounds, label, icon);
   }

   private static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, int fill, int border, ConnectedButtonRenderer.ConnectedEdges edges) {
      int radius = edges.update ? Math.max(1, 7) : 3;
      if (edges.allConnected()) {
         if (edges.update) {
            autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, radius, fill);
         } else {
            autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, radius, fill, border);
         }
      } else {
         autismclient.gui.f.NetHelper grown = init_NetHelper_NetHelper(bounds, edges, radius + 1);
         context.init_GuiGraphicsExtract().enableScissor(bounds.read(), bounds.write(), bounds.init(), bounds.load());

         try {
            if (edges.update) {
               autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), grown, radius, fill);
            } else {
               autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), grown, radius, fill, border);
            }
         } finally {
            context.init_GuiGraphicsExtract().disableScissor();
         }

         if (!edges.write) {
            autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(
               context.init_GuiGraphicsExtract(), bounds.read(), bounds.load() - 1, bounds.update(), context.init_TableIndex().colors().divider
            );
         }
      }
   }

   private static autismclient.gui.f.NetHelper init_NetHelper_NetHelper(autismclient.gui.f.NetHelper bounds, ConnectedButtonRenderer.ConnectedEdges edges, int by) {
      int left = edges.init ? 0 : by;
      int top = edges.load ? 0 : by;
      int right = edges.read ? 0 : by;
      int bottom = edges.write ? 0 : by;
      return autismclient.gui.f.NetHelper.init(bounds.read() - left, bounds.write() - top, bounds.update() + left + right, bounds.sync() + top + bottom);
   }

   private static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, boolean hovered, ConnectedButtonRenderer.ConnectedEdges edges) {
      float hoverT = autismclient.gui.ui.HoverTransitionCache.sample(autismclient.gui.ui.HoverTransitionCache.boundsKey(bounds), hovered);
      if (!(hoverT <= 0.001F)) {
         int color = Math.round(20.0F * hoverT) << 24 | 16777215;
         int radius = Math.max(1, (edges.update ? 7 : 3) - 1);
         autismclient.gui.f.NetHelper content = init(bounds, edges);
         if (edges.allConnected()) {
            autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), content, radius, color);
         } else {
            context.init_GuiGraphicsExtract().enableScissor(bounds.read(), bounds.write(), bounds.init(), bounds.load());

            try {
               autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), init_NetHelper_NetHelper(content, edges, radius + 1), radius, color);
            } finally {
               context.init_GuiGraphicsExtract().disableScissor();
            }
         }
      }
   }

   private static void load(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, Identifier icon) {
      init(context, bounds, label, icon, context.init_TableIndex().colors().textPrimary);
   }

   private static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, Identifier icon, int color) {
      if (icon == null) {
         context.init_PoolEntry().drawCentered(context.init_GuiGraphicsExtract(), label, bounds, color);
      } else {
         int iconSize = Math.min(12, Math.max(8, bounds.sync() - 5));
         int iconX = bounds.read() + 5;
         int iconY = bounds.write() + Math.max(1, (bounds.sync() - iconSize) / 2);
         context.init_GuiGraphicsExtract()
            .blit(ThemeTextureRecolorer.recolored(icon, ThemePaletteManager.ColorRole.ACCENT), iconX, iconY, iconX + iconSize, iconY + iconSize, 0.0F, 1.0F, 0.0F, 1.0F);
         int textX = iconX + iconSize + 4;
         context.init_PoolEntry()
            .drawFitted(context.init_GuiGraphicsExtract(), label, textX, context.init_PoolEntry().centerTextY(bounds), Math.max(1, bounds.init() - textX - 4), color);
      }
   }

   public static int init(autismclient.gui.f.SyncManager context, CachePool.Inner_DataUtil tone) {
      return context.init_TableIndex().colors().divider;
   }

   public static int init(autismclient.gui.f.SyncManager context, float progress) {
      return context.init_TableIndex().colors().divider;
   }

   public static float init_F_CachePool_DataUtil(CachePool.Inner_DataUtil tone, boolean toggle, float progress) {
      return 0.0F;
   }

   public static int dominantColor(int leftColor, float leftWeight, int rightColor, float rightWeight) {
      return rightWeight > leftWeight ? rightColor : leftColor;
   }

   public static void drawSeam(autismclient.gui.f.SyncManager context, int x, int y, int height, int color) {
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), x, y, Math.max(0, height - 1), color);
   }

   private static autismclient.gui.f.NetHelper init(autismclient.gui.f.NetHelper bounds, ConnectedButtonRenderer.ConnectedEdges edges) {
      return bounds.load(edges.init ? 1 : 0, edges.load ? 1 : 0, edges.read ? 1 : 0, edges.write ? 1 : 0);
   }

   private static float clamp01(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }

   public record ConnectedEdges(boolean init, boolean load, boolean read, boolean write, boolean update) {
      public ConnectedEdges(boolean left, boolean top, boolean right, boolean bottom) {
         this(left, top, right, bottom, false);
      }

      boolean allConnected() {
         return this.init && this.load && this.read && this.write;
      }

      public boolean load() {
         return this.init;
      }

      public boolean read() {
         return this.load;
      }

      public boolean write() {
         return this.read;
      }

      public boolean update() {
         return this.write;
      }

      public boolean sync() {
         return this.update;
      }
   }
}
