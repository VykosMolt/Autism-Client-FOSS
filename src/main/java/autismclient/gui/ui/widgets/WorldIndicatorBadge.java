package autismclient.gui.ui.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public final class WorldIndicatorBadge {
   private WorldIndicatorBadge() {
   }

   public static WorldIndicatorBadge.BadgeSize size(int captionWidth, int lineHeight, boolean caption) {
      return size(captionWidth, lineHeight, caption, true);
   }

   public static WorldIndicatorBadge.BadgeSize size(int captionWidth, int lineHeight, boolean caption, boolean arrow) {
      return new WorldIndicatorBadge.BadgeSize(
         (arrow ? 36 : 24) + (caption ? Math.max(0, captionWidth) + 4 : 0), Math.max(20, caption ? Math.max(1, lineHeight) + 4 : 20)
      );
   }

   public static WorldIndicatorBadge.BadgePlacement place(
      float clipX, float clipY, float clipW, double relativeYaw, int screenW, int screenH, WorldIndicatorBadge.BadgeSize size, float scale, float ring
   ) {
      if (screenW >= 64
         && screenH >= 64
         && Float.isFinite(clipX)
         && Float.isFinite(clipY)
         && Float.isFinite(clipW)
         && Double.isFinite(relativeYaw)
         && Float.isFinite(scale)
         && !(scale <= 0.0F)
         && Float.isFinite(ring)) {
         float halfW = size.init * scale / 2.0F;
         float halfH = size.load * scale / 2.0F;
         if (!(halfW + 4.0F > screenW / 2.0F) && !(halfH + 4.0F > screenH / 2.0F)) {
            float nx = clipX / Math.max(0.001F, Math.abs(clipW));
            float ny = -clipY / Math.max(0.001F, Math.abs(clipW));
            float x;
            float y;
            float angle;
            if (clipW > 0.0F && Math.abs(nx) < 0.9 && Math.abs(ny) < 0.85) {
               x = (nx + 1.0F) * screenW * 0.5F;
               y = (ny + 1.0F) * screenH * 0.5F - 12.0F * scale - halfH;
               angle = 3.1415927F;
            } else {
               angle = (float)Math.toRadians(relativeYaw);
               x = screenW * 0.5F + (float)Math.sin(angle) * ring;
               y = screenH * 0.5F - (float)Math.cos(angle) * ring;
            }

            x = Math.max(halfW + 4.0F, Math.min(x, screenW - halfW - 4.0F));
            y = Math.max(halfH + 4.0F, Math.min(y, screenH - halfH - 4.0F));
            return new WorldIndicatorBadge.BadgePlacement(x, y, angle);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static autismclient.gui.f.NetHelper bounds(WorldIndicatorBadge.BadgePlacement placement, WorldIndicatorBadge.BadgeSize size, float scale) {
      if (placement == null) {
         return autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
      }

      int x = (int)Math.floor(placement.init - size.init * scale / 2.0F);
      int y = (int)Math.floor(placement.load - size.load * scale / 2.0F);
      int right = (int)Math.ceil(placement.init + size.init * scale / 2.0F);
      int bottom = (int)Math.ceil(placement.load + size.load * scale / 2.0F);
      return autismclient.gui.f.NetHelper.init(x, y, right - x, bottom - y);
   }

   public static void render(
      autismclient.gui.f.SyncManager ui,
      WorldIndicatorBadge.BadgePlacement placement,
      WorldIndicatorBadge.BadgeSize size,
      float scale,
      ItemStack icon,
      String caption,
      int color,
      boolean arrow
   ) {
      if (placement != null) {
         GuiGraphicsExtractor graphics = ui.init_GuiGraphicsExtract();
         graphics.pose().pushMatrix();

         try {
            graphics.pose().translate(placement.init, placement.load);
            graphics.pose().scale(scale, scale);
            graphics.pose().translate(-size.init / 2.0F, -size.load / 2.0F);
            autismclient.gui.f.PacketUtil.read(
               graphics,
               autismclient.gui.f.NetHelper.init(0, 0, size.init, size.load),
               3,
               ui.init_TableIndex().colors().windowSurface,
               color & 16777215 | -1879048192
            );
            if (arrow) {
               graphics.pose().pushMatrix();

               try {
                  graphics.pose().translate(8.0F, size.load / 2.0F);
                  graphics.pose().rotate(placement.read);
                  autismclient.gui.f.PacketUtil.write(graphics, autismclient.gui.f.NetHelper.init(-4, -5, 8, 10), color);
               } finally {
                  graphics.pose().popMatrix();
               }
            }

            int iconX = arrow ? 17 : 4;
            graphics.pose().pushMatrix();

            try {
               graphics.pose().translate(0.0F, (size.load - 16) / 2.0F);
               graphics.item(icon, iconX, 0);
            } finally {
               graphics.pose().popMatrix();
            }

            if (caption != null && !caption.isBlank()) {
               int textX = iconX + 20;
               int textWidth = Math.max(0, size.init - textX - 3);
               ui.init_PoolEntry()
                  .drawEllipsizedInBounds(graphics, caption, autismclient.gui.f.NetHelper.init(textX, 0, textWidth, size.load), ui.init_TableIndex().colors().textPrimary);
            }
         } finally {
            graphics.pose().popMatrix();
         }
      }
   }

   public record BadgePlacement(float init, float load, float read) {
   }

   public record BadgeSize(int init, int load) {
   }
}
