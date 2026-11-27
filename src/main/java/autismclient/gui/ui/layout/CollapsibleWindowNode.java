package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.WindowFrameRenderer;
import autismclient.gui.f.b.l;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class CollapsibleWindowNode extends LayoutContainer {
   private final ColumnLayout body = new ColumnLayout();
   private String title;
   private Identifier titleIcon;
   private boolean expanded = true;
   private boolean active = true;
   private boolean headerHovered = false;
   private boolean centerTitle = true;
   private boolean showCollapseButton = false;
   private boolean showCloseButton = false;
   private l titleTone = l.init;
   private int titleLeftInset = 6;
   private int titleRightInset = 6;
   private final autismclient.gui.ui.EaseOutAnimator revealAnimator = new autismclient.gui.ui.EaseOutAnimator(1.0F, 180);
   private float bodyViewportHeight;
   private int lastHeaderHeight;
   private int lastDrawnHeight;
   private boolean scrollable;
   private float scrollOffset;
   private float bodyContentHeight;
   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics;
   private boolean scrollbarDragging;

   public CollapsibleWindowNode(String title) {
      this.title = title == null ? "" : title;
      this.body.init(2).init(EdgeInsets.uniform(4));
      this.addChild(this.body);
   }

   public ColumnLayout body() {
      return this.body;
   }

   public CollapsibleWindowNode setScrollable(boolean scrollable) {
      if (this.scrollable != scrollable) {
         this.scrollable = scrollable;
         this.scrollOffset = 0.0F;
         this.markLayoutDirty();
      }

      return this;
   }

   public float maxScroll() {
      return this.scrollable ? Math.max(0.0F, this.bodyContentHeight - this.bodyViewportHeight) : 0.0F;
   }

   public float scrollOffset() {
      return this.scrollOffset;
   }

   public void setScrollOffset(float scroll) {
      float next = Math.max(0.0F, Math.min(scroll, this.maxScroll()));
      if (next != this.scrollOffset) {
         this.scrollOffset = next;
         this.markLayoutDirty();
      }
   }

   public autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics() {
      return this.scrollbarMetrics;
   }

   public CollapsibleWindowNode setTitle(String title) {
      String next = title == null ? "" : title;
      if (!this.title.equals(next)) {
         this.title = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public CollapsibleWindowNode setTitleIcon(Identifier titleIcon) {
      this.titleIcon = titleIcon;
      return this;
   }

   public CollapsibleWindowNode setExpanded(boolean showBody) {
      if (this.expanded != showBody) {
         this.revealAnimator.init_F_F(showBody ? 1.0F : 0.0F);
         this.expanded = showBody;
         this.markLayoutDirty();
      }

      return this;
   }

   public CollapsibleWindowNode setExpandedImmediate(boolean showBody) {
      this.revealAnimator.init_V_F(showBody ? 1.0F : 0.0F);
      if (this.expanded != showBody) {
         this.expanded = showBody;
         this.markLayoutDirty();
      }

      return this;
   }

   public CollapsibleWindowNode setShowBody(boolean showBody) {
      return this.setExpanded(showBody);
   }

   public boolean pushBodyClip(GuiGraphicsExtractor graphics) {
      if (this.lastDrawnHeight <= this.lastHeaderHeight) {
         return false;
      }

      autismclient.gui.ui.UiClipStack.getInstance()
         .push(
            graphics,
            autismclient.gui.f.NetHelper.init(
               Math.round(this.nodeX), Math.round(this.nodeY) + this.lastHeaderHeight, Math.round(this.nodeWidth), this.lastDrawnHeight - this.lastHeaderHeight
            )
         );
      return true;
   }

   public int drawnBottomY() {
      return Math.round(this.nodeY) + this.lastDrawnHeight;
   }

   @Override
   public boolean mouseClicked(LayoutContext context, float mouseX, float mouseY, int button) {
      return this.scrollable && mouseY < this.nodeY + this.lastHeaderHeight
         ? false
         : this.expanded && mouseY < this.drawnBottomY() && super.mouseClicked(context, mouseX, mouseY, button);
   }

   @Override
   public boolean mouseScrolled(LayoutContext context, float mouseX, float mouseY, float amount) {
      return this.expanded && mouseY < this.drawnBottomY() && super.mouseScrolled(context, mouseX, mouseY, amount);
   }

   @Override
   public boolean keyPressed(LayoutContext context, int keyCode, int scanCode, int modifiers) {
      return this.expanded && super.keyPressed(context, keyCode, scanCode, modifiers);
   }

   @Override
   public boolean charTyped(LayoutContext context, char chr, int modifiers) {
      return this.expanded && super.charTyped(context, chr, modifiers);
   }

   public CollapsibleWindowNode setActive(boolean active) {
      this.active = active;
      return this;
   }

   public CollapsibleWindowNode setHeaderHovered(boolean headerHovered) {
      this.headerHovered = headerHovered;
      return this;
   }

   public CollapsibleWindowNode setCenterTitle(boolean centerTitle) {
      this.centerTitle = centerTitle;
      return this;
   }

   public CollapsibleWindowNode setHeaderButtons(boolean collapse, boolean close) {
      if (this.showCollapseButton != collapse || this.showCloseButton != close) {
         this.showCollapseButton = collapse;
         this.showCloseButton = close;
         this.markLayoutDirty();
      }

      return this;
   }

   public CollapsibleWindowNode setTitleTone(l titleTone) {
      l next = titleTone == null ? l.init : titleTone;
      if (this.titleTone != next) {
         this.titleTone = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public CollapsibleWindowNode setTitleInsets(int titleLeftInset, int titleRightInset) {
      int nextLeft = Math.max(0, titleLeftInset);
      int nextRight = Math.max(0, titleRightInset);
      if (this.titleLeftInset != nextLeft || this.titleRightInset != nextRight) {
         this.titleLeftInset = nextLeft;
         this.titleRightInset = nextRight;
         this.markLayoutDirty();
      }

      return this;
   }

   private void renderScrollbar(LayoutContext context, int bodyX, int bodyY, int bodyW, int bodyH) {
      if (this.scrollable && !(this.maxScroll() <= 0.0F) && bodyH > 0) {
         int inset = 2;
         this.scrollbarMetrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.computeFloat(
            Math.round(this.bodyContentHeight), Math.round(this.bodyViewportHeight), bodyX + bodyW - 6 - inset, bodyY + inset, 6, Math.max(1, bodyH - inset * 2), this.scrollOffset
         );
         boolean hovered = this.scrollbarMetrics.containsTrack(context.mouseX(), context.mouseY());
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(context.graphics(), this.scrollbarMetrics, hovered, this.scrollbarDragging);
      } else {
         this.scrollbarMetrics = null;
      }
   }

   public void setScrollbarDragging(boolean dragging) {
      this.scrollbarDragging = dragging;
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      int headerH = Math.max(context.theme().load(), context.theme().init_I_l_I(this.titleTone, 4));
      float bodyHeight = this.expanded ? this.body.measureHeight(context, availableWidth) : 0.0F;
      return headerH + bodyHeight;
   }

   @Override
   protected void layoutChildren(LayoutContext context) {
      int headerH = Math.max(context.theme().load(), context.theme().init_I_l_I(this.titleTone, 4));
      if (this.expanded) {
         this.bodyViewportHeight = Math.max(0.0F, this.nodeHeight - headerH);
      }

      this.body.setVisible(this.expanded || this.revealAnimator.init_F() > 0.0F);
      if (this.scrollable) {
         float contentH = this.body.measureHeight(context, this.nodeWidth);
         float contentW = this.nodeWidth;
         if (contentH > this.bodyViewportHeight + 0.5F) {
            contentW = Math.max(1.0F, this.nodeWidth - 6.0F);
            contentH = this.body.measureHeight(context, contentW);
         }

         this.bodyContentHeight = contentH;
         this.scrollOffset = Math.max(0.0F, Math.min(this.scrollOffset, this.maxScroll()));
         this.body.setBounds(this.nodeX, this.nodeY + headerH - this.scrollOffset, contentW, Math.max(this.bodyViewportHeight, contentH));
      } else {
         this.body.setBounds(this.nodeX, this.nodeY + headerH, this.nodeWidth, this.bodyViewportHeight);
      }
   }

   @Override
   public void render(LayoutContext context) {
      if (this.nodeVisible) {
         LayoutContext drawContext = context;
         int drawX = Math.round(this.nodeX);
         int drawY = Math.round(this.nodeY);
         int drawW = Math.round(this.nodeWidth);
         int headerH = Math.max(context.theme().load(), context.theme().init_I_l_I(this.titleTone, 4));
         if (this.expanded) {
            this.bodyViewportHeight = Math.max(0.0F, this.nodeHeight - headerH);
         }

         float reveal = this.revealAnimator.init_F_F(this.expanded ? 1.0F : 0.0F);
         int drawH = headerH + Math.round(this.bodyViewportHeight * reveal);
         this.lastHeaderHeight = headerH;
         this.lastDrawnHeight = drawH;
         this.body.setVisible(this.expanded || reveal > 0.0F);
         WindowFrameRenderer.init(
            autismclient.gui.f.UiHelper.init(
               drawContext.graphics(), drawContext.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
            ),
            autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH),
            this.title,
            !this.expanded,
            this.showCollapseButton,
            this.showCloseButton,
            this.headerHovered,
            this.active,
            Math.max(4, context.theme().load(this.titleLeftInset)),
            Math.max(34, context.theme().load(this.titleRightInset)),
            headerH
         );
         if (reveal > 0.0F) {
            int clipTop = Math.round(this.nodeY + headerH);
            int clipBottom = drawY + drawH;
            if (clipBottom > clipTop) {
               drawContext.viewport()
                  .pushClip(drawContext.graphics(), this.nodeX, this.nodeY + headerH, this.nodeX + this.nodeWidth, clipBottom);

               try {
                  super.render(drawContext);
                  this.renderScrollbar(drawContext, drawX, drawY + headerH, drawW, clipBottom - (drawY + headerH));
               } finally {
                  drawContext.viewport().popClip(drawContext.graphics());
               }
            }
         }

         if (reveal <= 0.0F) {
            this.scrollbarMetrics = null;
         }

         if (!this.active) {
            autismclient.gui.f.PacketUtil.load(
               drawContext.graphics(),
               autismclient.gui.f.NetHelper.init(drawX + 1, drawY + 1, Math.max(0, drawW - 2), Math.max(0, drawH - 2)),
               7,
               603979776
            );
         }
      }
   }
}
