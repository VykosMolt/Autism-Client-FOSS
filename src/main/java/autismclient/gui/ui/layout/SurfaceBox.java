package autismclient.gui.ui.layout;

public class SurfaceBox extends LayoutContainer {
   private final ColumnLayout init_PacketUtil = new ColumnLayout();
   private boolean write_Z = true;
   private EdgeInsets init_IoWorker = EdgeInsets.init(4);
   private boolean update_Z = true;
   private boolean sync = true;

   public SurfaceBox() {
      this.init_TableIndex_TableIndex(this.init_PacketUtil);
   }

   public ColumnLayout init_PacketUtil_3() {
      return this.init_PacketUtil;
   }

   public SurfaceBox init_StateCache(boolean active) {
      this.write_Z = active;
      return this;
   }

   public SurfaceBox init(EdgeInsets padding) {
      EdgeInsets next = padding == null ? EdgeInsets.init_IoWorker : padding;
      if (!this.init_IoWorker.equals(next)) {
         this.init_IoWorker = next;
         this.load_V();
      }

      return this;
   }

   public SurfaceBox load_StateCache(boolean drawBorder) {
      this.update_Z = drawBorder;
      return this;
   }

   public SurfaceBox read_StateCache(boolean drawFill) {
      this.sync = drawFill;
      return this;
   }

   @Override
   public float init_F_SyncManager(LayoutContext context) {
      EdgeInsets scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      return scaledPadding.init_I() + this.init_PacketUtil.init_F_SyncManager(context);
   }

   @Override
   public float init_F_SyncManager_F(LayoutContext context, float availableWidth) {
      EdgeInsets scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      return scaledPadding.load() + this.init_PacketUtil.init_F_SyncManager_F(context, Math.max(0.0F, availableWidth - scaledPadding.init_I()));
   }

   @Override
   protected void load_V(LayoutContext context) {
      EdgeInsets scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      this.init_PacketUtil
         .init_TableIndex_F(
            this.init_F + scaledPadding.read(),
            this.load_F + scaledPadding.write(),
            Math.max(0.0F, this.read_F - scaledPadding.init_I()),
            Math.max(0.0F, this.write - scaledPadding.load())
         );
   }

   @Override
   public void init_V_SyncManager(LayoutContext context) {
      if (this.init_Z) {
         LayoutContext drawContext = context;
         int drawX = Math.round(this.init_F);
         int drawY = Math.round(this.load_F);
         int drawW = Math.round(this.read_F);
         int drawH = Math.round(this.write);
         int fill = this.write_Z ? drawContext.init_CharEncoder().poll() : drawContext.init_CharEncoder().push();
         int border = this.write_Z ? drawContext.init_CharEncoder().get() : drawContext.init_CharEncoder().set();
         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH);
         if (this.sync && this.update_Z) {
            autismclient.gui.f.PacketUtil.read(drawContext.init_GuiGraphicsExtract(), bounds, 5, drawContext.init_I_I(fill), drawContext.init_I_I(border));
         } else if (this.sync) {
            autismclient.gui.f.PacketUtil.load(drawContext.init_GuiGraphicsExtract(), bounds, 5, drawContext.init_I_I(fill));
         } else if (this.update_Z) {
            autismclient.gui.f.PacketUtil.load(drawContext.init_GuiGraphicsExtract(), bounds, 5, 0, drawContext.init_I_I(border));
         }

         super.init_V_SyncManager(drawContext);
         if (!this.write_Z) {
            autismclient.gui.f.PacketUtil.load(drawContext.init_GuiGraphicsExtract(), bounds, 5, drawContext.init_I_I(603979776));
         }
      }
   }
}
