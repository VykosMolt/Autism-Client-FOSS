package autismclient.gui.f.c;

public class StateCache extends HashUtil {
   private final PacketUtil init_PacketUtil = new PacketUtil();
   private boolean write_Z = true;
   private IoWorker init_IoWorker = IoWorker.init(4);
   private boolean update_Z = true;
   private boolean sync = true;

   public StateCache() {
      this.init_TableIndex_TableIndex(this.init_PacketUtil);
   }

   public PacketUtil init_PacketUtil_3() {
      return this.init_PacketUtil;
   }

   public StateCache init_StateCache(boolean active) {
      this.write_Z = active;
      return this;
   }

   public StateCache init(IoWorker padding) {
      IoWorker next = padding == null ? IoWorker.init_IoWorker : padding;
      if (!this.init_IoWorker.equals(next)) {
         this.init_IoWorker = next;
         this.load_V();
      }

      return this;
   }

   public StateCache load_StateCache(boolean drawBorder) {
      this.update_Z = drawBorder;
      return this;
   }

   public StateCache read_StateCache(boolean drawFill) {
      this.sync = drawFill;
      return this;
   }

   @Override
   public float init_F_SyncManager(SyncManager context) {
      IoWorker scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      return scaledPadding.init_I() + this.init_PacketUtil.init_F_SyncManager(context);
   }

   @Override
   public float init_F_SyncManager_F(SyncManager context, float availableWidth) {
      IoWorker scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      return scaledPadding.load() + this.init_PacketUtil.init_F_SyncManager_F(context, Math.max(0.0F, availableWidth - scaledPadding.init_I()));
   }

   @Override
   protected void load_V(SyncManager context) {
      IoWorker scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      this.init_PacketUtil
         .init_TableIndex_F(
            this.init_F + scaledPadding.read(),
            this.load_F + scaledPadding.write(),
            Math.max(0.0F, this.read_F - scaledPadding.init_I()),
            Math.max(0.0F, this.write - scaledPadding.load())
         );
   }

   @Override
   public void init_V_SyncManager(SyncManager context) {
      if (this.init_Z) {
         SyncManager drawContext = context;
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
