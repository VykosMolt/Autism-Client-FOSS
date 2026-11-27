package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.ConnectedButtonRenderer;
import autismclient.util.ThemePaletteManager;

public class RowLayout extends LayoutContainer {
   private EdgeInsets init_IoWorker;
   private int init_I;
   private boolean write_Z;
   private int load_I;
   private float sync;

   public RowLayout() {
      this.init_IoWorker = EdgeInsets.ZERO;
      this.init_I = 0;
      this.write_Z = false;
      this.load_I = -50373;
      this.sync = 0.0F;
   }

   public RowLayout init(EdgeInsets padding) {
      EdgeInsets next = padding == null ? EdgeInsets.ZERO : padding;
      if (!this.init_IoWorker.equals(next)) {
         this.init_IoWorker = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public RowLayout init(int gap) {
      int next = Math.max(0, gap);
      if (this.init_I != next) {
         this.init_I = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public RowLayout init_StreamUtil(boolean underlineOnHover) {
      this.write_Z = underlineOnHover;
      return this;
   }

   public RowLayout load(int underlineColor) {
      this.load_I = underlineColor;
      return this;
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.init_IoWorker);
      int scaledGap = context.theme().load(this.init_I);
      float total = scaledPadding.init_I();
      int visibleCount = 0;

      for (LayoutNode child : this.children) {
         if (child != null && child.isVisible()) {
            total += child.measurePreferredWidth(context);
            visibleCount++;
         }
      }

      if (visibleCount > 1) {
         total += (visibleCount - 1) * scaledGap;
      }

      return total;
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.init_IoWorker);
      float tallest = 0.0F;

      for (LayoutNode child : this.children) {
         if (child != null && child.isVisible()) {
            tallest = Math.max(tallest, child.measureHeight(context, availableWidth));
         }
      }

      return scaledPadding.write() + tallest + scaledPadding.bottom();
   }

   @Override
   protected void layoutChildren(LayoutContext context) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.init_IoWorker);
      int scaledGap = context.theme().load(this.init_I);
      float innerX = this.nodeX + scaledPadding.read();
      float innerY = this.nodeY + scaledPadding.write();
      float innerWidth = Math.max(0.0F, this.nodeWidth - scaledPadding.init_I());
      float innerHeight = Math.max(0.0F, this.nodeHeight - scaledPadding.load());
      float fixedWidth = 0.0F;
      int growCount = 0;
      int visibleCount = 0;

      for (LayoutNode child : this.children) {
         if (child != null && child.isVisible()) {
            visibleCount++;
            if (child.isGrowX()) {
               growCount++;
            } else {
               fixedWidth += child.measurePreferredWidth(context);
            }
         }
      }

      float totalGap = Math.max(0, visibleCount - 1) * scaledGap;
      float freeWidth = Math.max(0.0F, innerWidth - fixedWidth - totalGap);
      float growWidth = growCount > 0 ? freeWidth / growCount : 0.0F;
      float cursorX = innerX;
      this.sync = 0.0F;

      for (LayoutNode child : this.children) {
         if (child != null && child.isVisible()) {
            float childWidth = child.isGrowX() ? growWidth : child.measurePreferredWidth(context);
            float childHeight = Math.min(innerHeight, child.measureHeight(context, childWidth));
            float childY = innerY + Math.max(0.0F, (innerHeight - childHeight) / 2.0F);
            child.setBounds(cursorX, childY, childWidth, childHeight);
            cursorX += childWidth + scaledGap;
         }
      }

      this.sync = Math.max(0.0F, cursorX - innerX - (visibleCount > 0 ? scaledGap : 0));
   }

   @Override
   public void render(LayoutContext context) {
      if (this.nodeVisible) {
         super.render(context);
         this.write(context);
         if (this.write_Z) {
            float hover = this.stepHover(this.containsPoint(context.mouseX(), context.mouseY()), context.delta());
            if (hover <= 0.0F || context.graphics() == null) {
               return;
            }

            EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.init_IoWorker);
            int drawX = Math.round(this.nodeX + scaledPadding.read());
            int drawY = Math.round(this.nodeY + this.nodeHeight - context.theme().load(1));
            float underlineAreaWidth = Math.max(this.sync, this.nodeWidth - scaledPadding.init_I());
            int underlineWidth = Math.max(1, Math.round(underlineAreaWidth * hover));
            int underlineX = drawX + Math.max(0, Math.round((underlineAreaWidth - underlineWidth) / 2.0F));
            context.graphics()
               .fill(
                  underlineX,
                  drawY,
                  underlineX + underlineWidth,
                  drawY + context.theme().load(1),
                  ThemePaletteManager.recolor(this.load_I, ThemePaletteManager.ColorRole.ACCENT)
               );
         }
      }
   }

   private void write(LayoutContext context) {
      if (context.graphics() != null) {
         autismclient.gui.f.SyncManager ui = null;
         LayoutNode previous = null;

         for (LayoutNode child : this.children) {
            if (child != null && child.isVisible()) {
               if (previous instanceof ButtonNode left
                  && child instanceof ButtonNode right
                  && left.isConnectedStyle()
                  && right.isConnectedStyle()
                  && Math.abs(left.x() + left.width() - right.x()) <= 0.5F) {
                  if (ui == null) {
                     ui = autismclient.gui.f.UiHelper.init(
                        context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
                     );
                  }

                  int leftColor = left.resolveConnectedColor(ui);
                  int rightColor = right.resolveConnectedColor(ui);
                  float leftWeight = left.resolveConnectedWeight();
                  float rightWeight = right.resolveConnectedWeight();
                  int seamColor = ConnectedButtonRenderer.init(leftColor, leftWeight, rightColor, rightWeight);
                  int seamX = Math.round(right.x());
                  int seamY = Math.round(Math.max(left.y(), right.y()));
                  int seamBottom = Math.round(Math.min(left.y() + left.height(), right.y() + right.height()));
                  ConnectedButtonRenderer.init(ui, seamX, seamY, Math.max(0, seamBottom - seamY), seamColor);
               }

               previous = child;
            }
         }
      }
   }
}
