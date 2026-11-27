package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.ConnectedButtonRenderer;
import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemeTextureRecolorer;
import net.minecraft.resources.Identifier;

public final class IconHeaderNode extends LayoutNode {
   private String init_Str;
   private Identifier init_Identifier;
   private int init_I = 12;
   private int load_I = 5;
   private boolean write_Z;

   public IconHeaderNode(String text, Identifier icon) {
      this.init_Str = text == null ? "" : text;
      this.init_Identifier = icon;
      this.write = 14.0F;
   }

   public IconHeaderNode init(int iconSize) {
      this.init_I = Math.max(1, iconSize);
      this.load_V();
      return this;
   }

   public IconHeaderNode init_ConfigStore(boolean connectedStyle) {
      this.write_Z = connectedStyle;
      return this;
   }

   @Override
   public float init_F_SyncManager(LayoutContext context) {
      int textWidth = autismclient.gui.f.UiHelper.init(context.font()).measureWidth(this.init_Str);
      return textWidth + (this.init_Identifier != null ? this.init_I + this.load_I : 0);
   }

   @Override
   public float init_F_SyncManager_F(LayoutContext context, float availableWidth) {
      return this.write_Z ? autismclient.gui.ui.UiDensityMetrics.scaledHeight(14) : Math.max(11, this.init_I + 1);
   }

   @Override
   public void init_V_SyncManager(LayoutContext context) {
      if (this.init_Z) {
         int drawX = Math.round(this.init_F);
         int drawY = Math.round(this.load_F);
         int drawH = Math.round(this.write);
         if (this.write_Z) {
            ConnectedButtonRenderer.init(
               autismclient.gui.f.UiHelper.init(
                  context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
               ),
               autismclient.gui.f.NetHelper.init(drawX, drawY, Math.round(this.read_F), drawH),
               this.init_Str,
               this.init_Identifier
            );
         } else {
            int contentX = drawX;
            if (this.init_Identifier != null) {
               int iconY = drawY + Math.max(1, (drawH - this.init_I) / 2);
               context.blitTexture(ThemeTextureRecolorer.init(this.init_Identifier, ThemePaletteManager.ColorRole.ACCENT), drawX, iconY, drawX + this.init_I, iconY + this.init_I);
               contentX += this.init_I + this.load_I;
            }

            autismclient.gui.f.UiHelper.init(context.font())
               .draw(
                  context.graphics(),
                  this.init_Str,
                  contentX,
                  drawY + Math.max(1, (drawH - 9) / 2),
                  context.applyAlpha(ThemePaletteManager.recolor(-791321, ThemePaletteManager.ColorRole.TEXT))
               );
         }
      }
   }
}
