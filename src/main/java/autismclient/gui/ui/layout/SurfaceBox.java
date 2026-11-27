package autismclient.gui.ui.layout;

public class SurfaceBox extends LayoutContainer {
   private final ColumnLayout content = new ColumnLayout();
   private boolean write_Z = true;
   private EdgeInsets init_IoWorker = EdgeInsets.uniform(4);
   private boolean update_Z = true;
   private boolean sync = true;

   public SurfaceBox() {
      this.addChild(this.content);
   }

   public ColumnLayout getContent() {
      return this.content;
   }

   public SurfaceBox setActive(boolean active) {
      this.write_Z = active;
      return this;
   }

   public SurfaceBox init(EdgeInsets padding) {
      EdgeInsets next = padding == null ? EdgeInsets.ZERO : padding;
      if (!this.init_IoWorker.equals(next)) {
         this.init_IoWorker = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public SurfaceBox setDrawBorder(boolean drawBorder) {
      this.update_Z = drawBorder;
      return this;
   }

   public SurfaceBox setDrawFill(boolean drawFill) {
      this.sync = drawFill;
      return this;
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.init_IoWorker);
      return scaledPadding.init_I() + this.content.measurePreferredWidth(context);
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.init_IoWorker);
      return scaledPadding.load() + this.content.measureHeight(context, Math.max(0.0F, availableWidth - scaledPadding.init_I()));
   }

   @Override
   protected void layoutChildren(LayoutContext context) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.init_IoWorker);
      this.content
         .setBounds(
            this.nodeX + scaledPadding.read(),
            this.nodeY + scaledPadding.write(),
            Math.max(0.0F, this.nodeWidth - scaledPadding.init_I()),
            Math.max(0.0F, this.nodeHeight - scaledPadding.load())
         );
   }

   @Override
   public void render(LayoutContext context) {
      if (this.nodeVisible) {
         LayoutContext drawContext = context;
         int drawX = Math.round(this.nodeX);
         int drawY = Math.round(this.nodeY);
         int drawW = Math.round(this.nodeWidth);
         int drawH = Math.round(this.nodeHeight);
         int fill = this.write_Z ? drawContext.theme().poll() : drawContext.theme().push();
         int border = this.write_Z ? drawContext.theme().get() : drawContext.theme().set();
         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH);
         if (this.sync && this.update_Z) {
            autismclient.gui.f.PacketUtil.read(drawContext.graphics(), bounds, 5, drawContext.applyAlpha(fill), drawContext.applyAlpha(border));
         } else if (this.sync) {
            autismclient.gui.f.PacketUtil.load(drawContext.graphics(), bounds, 5, drawContext.applyAlpha(fill));
         } else if (this.update_Z) {
            autismclient.gui.f.PacketUtil.load(drawContext.graphics(), bounds, 5, 0, drawContext.applyAlpha(border));
         }

         super.render(drawContext);
         if (!this.write_Z) {
            autismclient.gui.f.PacketUtil.load(drawContext.graphics(), bounds, 5, drawContext.applyAlpha(603979776));
         }
      }
   }
}
