package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.WindowFrameRenderer;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;

public class CollapsibleSectionNode extends LayoutContainer {
   private final ColumnLayout content = new ColumnLayout();
   private String title;
   private boolean expandedState = true;
   private int headerHeight = 14;
   private int contentTopGap = 2;
   private final autismclient.gui.ui.EaseOutAnimator expandAnimator = new autismclient.gui.ui.EaseOutAnimator(1.0F, 180);
   private float contentHeight;

   public CollapsibleSectionNode(String title) {
      this.title = title == null ? "" : title;
      this.content.withPadding(EdgeInsets.ZERO).withGap(2);
      this.addChild(this.content);
   }

   public ColumnLayout getContent() {
      return this.content;
   }

   public CollapsibleSectionNode withTitle(String title) {
      String next = title == null ? "" : title;
      if (!this.title.equals(next)) {
         this.title = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public CollapsibleSectionNode setExpanded(boolean expanded) {
      if (this.expandedState != expanded) {
         this.expandedState = expanded;
         this.expandAnimator.animateTo(expanded ? 1.0F : 0.0F);
         this.markLayoutDirty();
      }

      return this;
   }

   public CollapsibleSectionNode snapExpanded(boolean expanded) {
      if (this.expandedState != expanded) {
         this.expandedState = expanded;
         this.markLayoutDirty();
      }

      this.expandAnimator.snapTo(expanded ? 1.0F : 0.0F);
      return this;
   }

   public boolean isExpanded() {
      return this.expandedState;
   }

   public CollapsibleSectionNode withHeaderHeight(int headerHeight) {
      int next = Math.max(12, headerHeight);
      if (this.headerHeight != next) {
         this.headerHeight = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public CollapsibleSectionNode withContentTopGap(int contentTopGap) {
      int next = Math.max(0, contentTopGap);
      if (this.contentTopGap != next) {
         this.contentTopGap = next;
         this.markLayoutDirty();
      }

      return this;
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      return Math.max(
         k.init(context.font(), this.title, context.theme().init_Identifier_l(l.load), context.theme().load(l.load))
            + context.theme().load(18),
         this.content.measurePreferredWidth(context)
      );
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      int scaledHeaderHeight = Math.max(context.theme().load(this.headerHeight), context.theme().init_I_l_I(l.load, 4));
      if (!this.expandedState) {
         return scaledHeaderHeight;
      }

      int scaledContentTopGap = context.theme().load(this.contentTopGap);
      float bodyHeight = scaledContentTopGap + this.content.measureHeight(context, availableWidth);
      return scaledHeaderHeight + bodyHeight;
   }

   @Override
   protected void layoutChildren(LayoutContext context) {
      int scaledHeaderHeight = Math.max(context.theme().load(this.headerHeight), context.theme().init_I_l_I(l.load, 4));
      int scaledContentTopGap = context.theme().load(this.contentTopGap);
      if (this.expandedState) {
         this.contentHeight = Math.max(0.0F, this.nodeHeight - scaledHeaderHeight);
      }

      float contentY = this.nodeY + scaledHeaderHeight + scaledContentTopGap;
      float contentH = Math.max(0.0F, this.contentHeight - scaledContentTopGap);
      this.content.setVisible(this.expandedState || this.expandAnimator.value() > 0.0F);
      this.content.setBounds(this.nodeX, contentY, this.nodeWidth, contentH);
   }

   @Override
   public void render(LayoutContext context) {
      if (this.nodeVisible) {
         int drawX = Math.round(this.nodeX);
         int drawY = Math.round(this.nodeY);
         int drawW = Math.round(this.nodeWidth);
         int drawHeaderH = Math.max(context.theme().load(this.headerHeight), context.theme().init_I_l_I(l.load, 4));
         boolean hovered = context.mouseX() >= this.nodeX
            && context.mouseX() <= this.nodeX + this.nodeWidth
            && context.mouseY() >= this.nodeY
            && context.mouseY() <= this.nodeY + drawHeaderH;
         if (this.expandedState) {
            this.contentHeight = Math.max(0.0F, this.nodeHeight - drawHeaderH);
         }

         float shown = this.expandAnimator.animateTo(this.expandedState ? 1.0F : 0.0F);
         int drawH = drawHeaderH + Math.round(this.contentHeight * shown);
         this.content.setVisible(this.expandedState || shown > 0.0F);
         WindowFrameRenderer.drawFrame(
            autismclient.gui.f.UiHelper.init(context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())),
            autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH),
            this.title,
            !this.expandedState,
            true,
            false,
            hovered,
            true,
            4,
            4,
            drawHeaderH
         );
         this.renderClipped(context, drawHeaderH, drawY + drawH);
      }
   }

   private void renderClipped(LayoutContext context, int drawHeaderH, int clipBottom) {
      int clipTop = Math.round(this.nodeY + drawHeaderH);
      if (clipBottom > clipTop) {
         context.viewport().pushClip(context.graphics(), this.nodeX, this.nodeY + drawHeaderH, this.nodeX + this.nodeWidth, clipBottom);

         try {
            super.render(context);
         } finally {
            context.viewport().popClip(context.graphics());
         }
      }
   }

   @Override
   public boolean mouseClicked(LayoutContext context, float mouseX, float mouseY, int button) {
      int scaledHeaderHeight = Math.max(context.theme().load(this.headerHeight), context.theme().init_I_l_I(l.load, 4));
      if (button == 0 && mouseX >= this.nodeX && mouseX <= this.nodeX + this.nodeWidth && mouseY >= this.nodeY && mouseY <= this.nodeY + scaledHeaderHeight) {
         this.setExpanded(!this.expandedState);
         return true;
      } else {
         return this.expandedState && super.mouseClicked(context, mouseX, mouseY, button);
      }
   }
}
