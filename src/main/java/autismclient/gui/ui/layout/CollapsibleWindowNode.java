package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.WindowFrameRenderer;
import autismclient.gui.f.b.l;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class CollapsibleWindowNode extends LayoutContainer {
   private final ColumnLayout init_PacketUtil = new ColumnLayout();
   private String init_Str;
   private Identifier init_Identifier;
   private boolean write_Z = true;
   private boolean update_Z = true;
   private boolean sync_Z = false;
   private boolean flush_Z = true;
   private boolean close_Z = false;
   private boolean open = false;
   private l init_l = l.init;
   private int init_I = 6;
   private int load_I = 6;
   private final autismclient.gui.ui.EaseOutAnimator init_RingBuffer = new autismclient.gui.ui.EaseOutAnimator(1.0F, 180);
   private float sync_F;
   private int read_I;
   private int write_I;
   private boolean poll;
   private float flush_F;
   private float close_F;
   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics init_PoolEntry_DataUtil;
   private boolean push;

   public CollapsibleWindowNode(String title) {
      this.init_Str = title == null ? "" : title;
      this.init_PacketUtil.init(2).init(EdgeInsets.init(4));
      this.init_TableIndex_TableIndex(this.init_PacketUtil);
   }

   public ColumnLayout init_PacketUtil() {
      return this.init_PacketUtil;
   }

   public CollapsibleWindowNode init_CharEncoder(boolean scrollable) {
      if (this.poll != scrollable) {
         this.poll = scrollable;
         this.flush_F = 0.0F;
         this.load_V();
      }

      return this;
   }

   public float init_F_2() {
      return this.poll ? Math.max(0.0F, this.close_F - this.sync_F) : 0.0F;
   }

   public float sync_F() {
      return this.flush_F;
   }

   public void init(float scroll) {
      float next = Math.max(0.0F, Math.min(scroll, this.init_F_2()));
      if (next != this.flush_F) {
         this.flush_F = next;
         this.load_V();
      }
   }

   public autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics init_PoolEntry_DataUtil() {
      return this.init_PoolEntry_DataUtil;
   }

   public CollapsibleWindowNode init(String title) {
      String next = title == null ? "" : title;
      if (!this.init_Str.equals(next)) {
         this.init_Str = next;
         this.load_V();
      }

      return this;
   }

   public CollapsibleWindowNode init(Identifier titleIcon) {
      this.init_Identifier = titleIcon;
      return this;
   }

   public CollapsibleWindowNode load_CharEncoder(boolean showBody) {
      if (this.write_Z != showBody) {
         this.init_RingBuffer.init_F_F(showBody ? 1.0F : 0.0F);
         this.write_Z = showBody;
         this.load_V();
      }

      return this;
   }

   public CollapsibleWindowNode read_CharEncoder(boolean showBody) {
      this.init_RingBuffer.init_V_F(showBody ? 1.0F : 0.0F);
      if (this.write_Z != showBody) {
         this.write_Z = showBody;
         this.load_V();
      }

      return this;
   }

   public CollapsibleWindowNode write(boolean showBody) {
      return this.load_CharEncoder(showBody);
   }

   public boolean init(GuiGraphicsExtractor graphics) {
      if (this.write_I <= this.read_I) {
         return false;
      }

      autismclient.gui.ui.UiClipStack.init_HashUtil()
         .init(
            graphics,
            autismclient.gui.f.NetHelper.init(
               Math.round(this.init_F), Math.round(this.load_F) + this.read_I, Math.round(this.read_F), this.write_I - this.read_I
            )
         );
      return true;
   }

   public int init_I_2() {
      return Math.round(this.load_F) + this.write_I;
   }

   @Override
   public boolean init_Z_SyncManager_2(LayoutContext context, float mouseX, float mouseY, int button) {
      return this.poll && mouseY < this.load_F + this.read_I
         ? false
         : this.write_Z && mouseY < this.init_I_2() && super.init_Z_SyncManager_2(context, mouseX, mouseY, button);
   }

   @Override
   public boolean init_Z_SyncManager(LayoutContext context, float mouseX, float mouseY, float amount) {
      return this.write_Z && mouseY < this.init_I_2() && super.init_Z_SyncManager(context, mouseX, mouseY, amount);
   }

   @Override
   public boolean init_Z_SyncManager_3(LayoutContext context, int keyCode, int scanCode, int modifiers) {
      return this.write_Z && super.init_Z_SyncManager_3(context, keyCode, scanCode, modifiers);
   }

   @Override
   public boolean init_Z_SyncManager_2(LayoutContext context, char chr, int modifiers) {
      return this.write_Z && super.init_Z_SyncManager_2(context, chr, modifiers);
   }

   public CollapsibleWindowNode update(boolean active) {
      this.update_Z = active;
      return this;
   }

   public CollapsibleWindowNode sync(boolean headerHovered) {
      this.sync_Z = headerHovered;
      return this;
   }

   public CollapsibleWindowNode flush(boolean centerTitle) {
      this.flush_Z = centerTitle;
      return this;
   }

   public CollapsibleWindowNode init_CharEncoder_Z_Z(boolean collapse, boolean close) {
      if (this.close_Z != collapse || this.open != close) {
         this.close_Z = collapse;
         this.open = close;
         this.load_V();
      }

      return this;
   }

   public CollapsibleWindowNode init(l titleTone) {
      l next = titleTone == null ? l.init : titleTone;
      if (this.init_l != next) {
         this.init_l = next;
         this.load_V();
      }

      return this;
   }

   public CollapsibleWindowNode init_CharEncoder_I_I(int titleLeftInset, int titleRightInset) {
      int nextLeft = Math.max(0, titleLeftInset);
      int nextRight = Math.max(0, titleRightInset);
      if (this.init_I != nextLeft || this.load_I != nextRight) {
         this.init_I = nextLeft;
         this.load_I = nextRight;
         this.load_V();
      }

      return this;
   }

   private void init(LayoutContext context, int bodyX, int bodyY, int bodyW, int bodyH) {
      if (this.poll && !(this.init_F_2() <= 0.0F) && bodyH > 0) {
         int inset = 2;
         this.init_PoolEntry_DataUtil = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I(
            Math.round(this.close_F), Math.round(this.sync_F), bodyX + bodyW - 6 - inset, bodyY + inset, 6, Math.max(1, bodyH - inset * 2), this.flush_F
         );
         boolean hovered = this.init_PoolEntry_DataUtil.init(context.init_F(), context.load());
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(context.init_GuiGraphicsExtract(), this.init_PoolEntry_DataUtil, hovered, this.push);
      } else {
         this.init_PoolEntry_DataUtil = null;
      }
   }

   public void init_V(boolean dragging) {
      this.push = dragging;
   }

   @Override
   public float init_F_SyncManager_F(LayoutContext context, float availableWidth) {
      int headerH = Math.max(context.init_CharEncoder().load(), context.init_CharEncoder().init_I_l_I(this.init_l, 4));
      float bodyHeight = this.write_Z ? this.init_PacketUtil.init_F_SyncManager_F(context, availableWidth) : 0.0F;
      return headerH + bodyHeight;
   }

   @Override
   protected void load_V(LayoutContext context) {
      int headerH = Math.max(context.init_CharEncoder().load(), context.init_CharEncoder().init_I_l_I(this.init_l, 4));
      if (this.write_Z) {
         this.sync_F = Math.max(0.0F, this.write - headerH);
      }

      this.init_PacketUtil.load_TableIndex_Z(this.write_Z || this.init_RingBuffer.init_F() > 0.0F);
      if (this.poll) {
         float contentH = this.init_PacketUtil.init_F_SyncManager_F(context, this.read_F);
         float contentW = this.read_F;
         if (contentH > this.sync_F + 0.5F) {
            contentW = Math.max(1.0F, this.read_F - 6.0F);
            contentH = this.init_PacketUtil.init_F_SyncManager_F(context, contentW);
         }

         this.close_F = contentH;
         this.flush_F = Math.max(0.0F, Math.min(this.flush_F, this.init_F_2()));
         this.init_PacketUtil.init_TableIndex_F(this.init_F, this.load_F + headerH - this.flush_F, contentW, Math.max(this.sync_F, contentH));
      } else {
         this.init_PacketUtil.init_TableIndex_F(this.init_F, this.load_F + headerH, this.read_F, this.sync_F);
      }
   }

   @Override
   public void init_V_SyncManager(LayoutContext context) {
      if (this.init_Z) {
         LayoutContext drawContext = context;
         int drawX = Math.round(this.init_F);
         int drawY = Math.round(this.load_F);
         int drawW = Math.round(this.read_F);
         int headerH = Math.max(context.init_CharEncoder().load(), context.init_CharEncoder().init_I_l_I(this.init_l, 4));
         if (this.write_Z) {
            this.sync_F = Math.max(0.0F, this.write - headerH);
         }

         float reveal = this.init_RingBuffer.init_F_F(this.write_Z ? 1.0F : 0.0F);
         int drawH = headerH + Math.round(this.sync_F * reveal);
         this.read_I = headerH;
         this.write_I = drawH;
         this.init_PacketUtil.load_TableIndex_Z(this.write_Z || reveal > 0.0F);
         WindowFrameRenderer.init(
            autismclient.gui.f.UiHelper.init(
               drawContext.init_GuiGraphicsExtract(), drawContext.init_Font(), Math.round(context.init_F()), Math.round(context.load())
            ),
            autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH),
            this.init_Str,
            !this.write_Z,
            this.close_Z,
            this.open,
            this.sync_Z,
            this.update_Z,
            Math.max(4, context.init_CharEncoder().load(this.init_I)),
            Math.max(34, context.init_CharEncoder().load(this.load_I)),
            headerH
         );
         if (reveal > 0.0F) {
            int clipTop = Math.round(this.load_F + headerH);
            int clipBottom = drawY + drawH;
            if (clipBottom > clipTop) {
               drawContext.init_RingBuffer()
                  .init(drawContext.init_GuiGraphicsExtract(), this.init_F, this.load_F + headerH, this.init_F + this.read_F, clipBottom);

               try {
                  super.init_V_SyncManager(drawContext);
                  this.init(drawContext, drawX, drawY + headerH, drawW, clipBottom - (drawY + headerH));
               } finally {
                  drawContext.init_RingBuffer().read(drawContext.init_GuiGraphicsExtract());
               }
            }
         }

         if (reveal <= 0.0F) {
            this.init_PoolEntry_DataUtil = null;
         }

         if (!this.update_Z) {
            autismclient.gui.f.PacketUtil.load(
               drawContext.init_GuiGraphicsExtract(),
               autismclient.gui.f.NetHelper.init(drawX + 1, drawY + 1, Math.max(0, drawW - 2), Math.max(0, drawH - 2)),
               7,
               603979776
            );
         }
      }
   }
}
