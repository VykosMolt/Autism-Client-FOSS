package autismclient.gui.f.c;

public class PacketUtil extends HashUtil {
   private IoWorker init_IoWorker;
   private int init_I;

   public PacketUtil() {
      this.init_IoWorker = IoWorker.init_IoWorker;
      this.init_I = 0;
   }

   public PacketUtil init(IoWorker padding) {
      IoWorker next = padding == null ? IoWorker.init_IoWorker : padding;
      if (!this.init_IoWorker.equals(next)) {
         this.init_IoWorker = next;
         this.load_V();
      }

      return this;
   }

   public PacketUtil init(int gap) {
      int next = Math.max(0, gap);
      if (this.init_I != next) {
         this.init_I = next;
         this.load_V();
      }

      return this;
   }

   @Override
   public float init_F_SyncManager_F(SyncManager context, float availableWidth) {
      IoWorker scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      int scaledGap = context.init_CharEncoder().load(this.init_I);
      float innerWidth = Math.max(0.0F, availableWidth - scaledPadding.init_I());
      float total = scaledPadding.write() + scaledPadding.sync();
      boolean first = true;

      for (TableIndex child : this.init_List) {
         if (child != null && child.write_Z()) {
            if (!first) {
               total += scaledGap;
            }

            total += child.init_F_SyncManager_F(context, innerWidth);
            first = false;
         }
      }

      return total;
   }

   @Override
   protected void load_V(SyncManager context) {
      IoWorker scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      int scaledGap = context.init_CharEncoder().load(this.init_I);
      float innerX = this.init_F + scaledPadding.read();
      float cursorY = this.load_F + scaledPadding.write();
      float innerWidth = Math.max(0.0F, this.read_F - scaledPadding.init_I());

      for (TableIndex child : this.init_List) {
         if (child != null && child.write_Z()) {
            float childHeight = child.init_F_SyncManager_F(context, innerWidth);
            child.init_TableIndex_F(innerX, cursorY, innerWidth, childHeight);
            cursorY += childHeight + scaledGap;
         }
      }
   }
}
