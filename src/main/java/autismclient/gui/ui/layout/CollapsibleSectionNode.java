package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.WindowFrameRenderer;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;

public class CollapsibleSectionNode extends LayoutContainer {
   private final ColumnLayout init_PacketUtil = new ColumnLayout();
   private String init_Str;
   private boolean write_Z = true;
   private int init_I = 14;
   private int load_I = 2;
   private final autismclient.gui.ui.EaseOutAnimator init_RingBuffer = new autismclient.gui.ui.EaseOutAnimator(1.0F, 180);
   private float sync;

   public CollapsibleSectionNode(String title) {
      this.init_Str = title == null ? "" : title;
      this.init_PacketUtil.init(EdgeInsets.ZERO).init(2);
      this.addChild(this.init_PacketUtil);
   }

   public ColumnLayout init_PacketUtil_2() {
      return this.init_PacketUtil;
   }

   public CollapsibleSectionNode init(String title) {
      String next = title == null ? "" : title;
      if (!this.init_Str.equals(next)) {
         this.init_Str = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public CollapsibleSectionNode init_DataUtil(boolean expanded) {
      if (this.write_Z != expanded) {
         this.write_Z = expanded;
         this.init_RingBuffer.init_F_F(expanded ? 1.0F : 0.0F);
         this.markLayoutDirty();
      }

      return this;
   }

   public CollapsibleSectionNode load_DataUtil(boolean expanded) {
      if (this.write_Z != expanded) {
         this.write_Z = expanded;
         this.markLayoutDirty();
      }

      this.init_RingBuffer.init_V_F(expanded ? 1.0F : 0.0F);
      return this;
   }

   public boolean init_Z_3() {
      return this.write_Z;
   }

   public CollapsibleSectionNode init(int headerHeight) {
      int next = Math.max(12, headerHeight);
      if (this.init_I != next) {
         this.init_I = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public CollapsibleSectionNode load(int contentTopGap) {
      int next = Math.max(0, contentTopGap);
      if (this.load_I != next) {
         this.load_I = next;
         this.markLayoutDirty();
      }

      return this;
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      return Math.max(
         k.init(context.font(), this.init_Str, context.theme().init_Identifier_l(l.load), context.theme().load(l.load))
            + context.theme().load(18),
         this.init_PacketUtil.measurePreferredWidth(context)
      );
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      int scaledHeaderHeight = Math.max(context.theme().load(this.init_I), context.theme().init_I_l_I(l.load, 4));
      if (!this.write_Z) {
         return scaledHeaderHeight;
      }

      int scaledContentTopGap = context.theme().load(this.load_I);
      float bodyHeight = scaledContentTopGap + this.init_PacketUtil.measureHeight(context, availableWidth);
      return scaledHeaderHeight + bodyHeight;
   }

   @Override
   protected void layoutChildren(LayoutContext context) {
      int scaledHeaderHeight = Math.max(context.theme().load(this.init_I), context.theme().init_I_l_I(l.load, 4));
      int scaledContentTopGap = context.theme().load(this.load_I);
      if (this.write_Z) {
         this.sync = Math.max(0.0F, this.nodeHeight - scaledHeaderHeight);
      }

      float contentY = this.nodeY + scaledHeaderHeight + scaledContentTopGap;
      float contentH = Math.max(0.0F, this.sync - scaledContentTopGap);
      this.init_PacketUtil.setVisible(this.write_Z || this.init_RingBuffer.init_F() > 0.0F);
      this.init_PacketUtil.setBounds(this.nodeX, contentY, this.nodeWidth, contentH);
   }

   @Override
   public void render(LayoutContext context) {
      if (this.nodeVisible) {
         int drawX = Math.round(this.nodeX);
         int drawY = Math.round(this.nodeY);
         int drawW = Math.round(this.nodeWidth);
         int drawHeaderH = Math.max(context.theme().load(this.init_I), context.theme().init_I_l_I(l.load, 4));
         boolean hovered = context.mouseX() >= this.nodeX
            && context.mouseX() <= this.nodeX + this.nodeWidth
            && context.mouseY() >= this.nodeY
            && context.mouseY() <= this.nodeY + drawHeaderH;
         if (this.write_Z) {
            this.sync = Math.max(0.0F, this.nodeHeight - drawHeaderH);
         }

         float shown = this.init_RingBuffer.init_F_F(this.write_Z ? 1.0F : 0.0F);
         int drawH = drawHeaderH + Math.round(this.sync * shown);
         this.init_PacketUtil.setVisible(this.write_Z || shown > 0.0F);
         WindowFrameRenderer.init(
            autismclient.gui.f.UiHelper.init(context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())),
            autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH),
            this.init_Str,
            !this.write_Z,
            true,
            false,
            hovered,
            true,
            4,
            4,
            drawHeaderH
         );
         this.init(context, drawHeaderH, drawY + drawH);
      }
   }

   private void init(LayoutContext context, int drawHeaderH, int clipBottom) {
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
      int scaledHeaderHeight = Math.max(context.theme().load(this.init_I), context.theme().init_I_l_I(l.load, 4));
      if (button == 0 && mouseX >= this.nodeX && mouseX <= this.nodeX + this.nodeWidth && mouseY >= this.nodeY && mouseY <= this.nodeY + scaledHeaderHeight) {
         this.init_DataUtil(!this.write_Z);
         return true;
      } else {
         return this.write_Z && super.mouseClicked(context, mouseX, mouseY, button);
      }
   }
}
