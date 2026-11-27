package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.ConnectedButtonRenderer;
import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemeTextureRecolorer;
import net.minecraft.resources.Identifier;

public final class IconHeaderNode extends LayoutNode {
   private String text;
   private Identifier icon;
   private int iconSize = 12;
   private int iconGap = 5;
   private boolean connectedStyle;

   public IconHeaderNode(String text, Identifier icon) {
      this.text = text == null ? "" : text;
      this.icon = icon;
      this.nodeHeight = 14.0F;
   }

   public IconHeaderNode init(int iconSize) {
      this.iconSize = Math.max(1, iconSize);
      this.markLayoutDirty();
      return this;
   }

   public IconHeaderNode setConnectedStyle(boolean connectedStyle) {
      this.connectedStyle = connectedStyle;
      return this;
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      int textWidth = autismclient.gui.f.UiHelper.init(context.font()).measureWidth(this.text);
      return textWidth + (this.icon != null ? this.iconSize + this.iconGap : 0);
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      return this.connectedStyle ? autismclient.gui.ui.UiDensityMetrics.scaledHeight(14) : Math.max(11, this.iconSize + 1);
   }

   @Override
   public void render(LayoutContext context) {
      if (this.nodeVisible) {
         int drawX = Math.round(this.nodeX);
         int drawY = Math.round(this.nodeY);
         int drawH = Math.round(this.nodeHeight);
         if (this.connectedStyle) {
            ConnectedButtonRenderer.init(
               autismclient.gui.f.UiHelper.init(
                  context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
               ),
               autismclient.gui.f.NetHelper.init(drawX, drawY, Math.round(this.nodeWidth), drawH),
               this.text,
               this.icon
            );
         } else {
            int contentX = drawX;
            if (this.icon != null) {
               int iconY = drawY + Math.max(1, (drawH - this.iconSize) / 2);
               context.blitTexture(ThemeTextureRecolorer.init(this.icon, ThemePaletteManager.ColorRole.ACCENT), drawX, iconY, drawX + this.iconSize, iconY + this.iconSize);
               contentX += this.iconSize + this.iconGap;
            }

            autismclient.gui.f.UiHelper.init(context.font())
               .draw(
                  context.graphics(),
                  this.text,
                  contentX,
                  drawY + Math.max(1, (drawH - 9) / 2),
                  context.applyAlpha(ThemePaletteManager.recolor(-791321, ThemePaletteManager.ColorRole.TEXT))
               );
         }
      }
   }
}
