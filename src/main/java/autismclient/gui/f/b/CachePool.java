package autismclient.gui.f.b;

import autismclient.util.ct;
import autismclient.util.cu;
import net.minecraft.resources.Identifier;

public final class CachePool {
   private CachePool() {
   }

   public static void init(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, CachePool.Inner_DataUtil tone, boolean hovered, boolean active
   ) {
      load(context, bounds, label, null, tone, hovered, active, autismclient.gui.f.BufferHelper.init(autismclient.gui.f.BufferHelper.init(bounds), active));
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
      autismclient.gui.f.JsonUtil colors = context.init_TableIndex().init_JsonUtil();
      float progress = Math.min(1.0F, Math.max(0.0F, activeProgress));
      int var10000;
      switch (tone) {
         case read:
            var10000 = colors.clear;
            break;
         case write:
            var10000 = colors.stop;
            break;
         case update:
            var10000 = colors.data;
            break;
         default:
            var10000 = colors.set;
      }

      int toneColor = var10000;
      boolean colored = tone == CachePool.Inner_DataUtil.read || tone == CachePool.Inner_DataUtil.write || tone == CachePool.Inner_DataUtil.update;
      float hoverT = autismclient.gui.f.DataUtil.init(autismclient.gui.f.DataUtil.init(bounds), hovered);
      int fill = j.init_I_I(colors.load, toneColor, colored ? 0.08F + 0.14F * progress : 0.12F * progress);
      int border = colored ? j.init_I_I(fill, toneColor, 0.5F + 0.5F * progress) : colors.set;
      fill = j.init_I_I(fill, colors.read, hoverT * 0.6F);
      border = j.init_I_I(border, colored ? toneColor : colors.run, hoverT * 0.65F);
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, 3, fill, border);
      if (icon == null) {
         context.init_PoolEntry().load(context.init_GuiGraphicsExtract(), label, bounds, colors.prev);
      } else {
         int iconSize = Math.min(12, Math.max(8, bounds.sync() - 5));
         int textW = context.init_PoolEntry().init(label);
         int groupW = iconSize + 4 + textW;
         int iconX = bounds.read() + Math.max(3, (bounds.update() - groupW) / 2);
         int iconY = bounds.write() + Math.max(1, (bounds.sync() - iconSize) / 2);
         if (!autismclient.gui.e.CachePool.init(context.init_GuiGraphicsExtract(), icon, iconX, iconY, iconSize, iconSize, colors.prev)) {
            context.init_GuiGraphicsExtract()
               .blit(cu.init(icon, ct.Inner_DataUtil.init), iconX, iconY, iconX + iconSize, iconY + iconSize, 0.0F, 1.0F, 0.0F, 1.0F);
         }

         context.init_PoolEntry()
            .load(
               context.init_GuiGraphicsExtract(),
               label,
               iconX + iconSize + 4,
               context.init_PoolEntry().init_I(bounds),
               Math.max(1, bounds.init() - (iconX + iconSize + 4) - 4),
               colors.prev
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
