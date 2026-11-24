package autismclient.gui.f.c;

public class CachePool extends HashUtil {
   private final TableIndex init_TableIndex;
   private final TableIndex load_TableIndex;
   private IoWorker init_IoWorker;
   private int init_I;
   private float sync;
   private boolean write_Z;

   public CachePool(TableIndex labelNode, TableIndex controlNode) {
      this.init_IoWorker = IoWorker.init_IoWorker;
      this.init_I = 0;
      this.sync = -1.0F;
      this.write_Z = false;
      this.init_TableIndex = this.init_TableIndex_TableIndex(labelNode);
      this.load_TableIndex = this.init_TableIndex_TableIndex(controlNode);
   }

   public CachePool init_CachePool_IoWorker(IoWorker padding) {
      IoWorker next = padding == null ? IoWorker.init_IoWorker : padding;
      if (!this.init_IoWorker.equals(next)) {
         this.init_IoWorker = next;
         this.load_V();
      }

      return this;
   }

   public CachePool init_CachePool_I(int gap) {
      int next = Math.max(0, gap);
      if (this.init_I != next) {
         this.init_I = next;
         this.load_V();
      }

      return this;
   }

   public CachePool init_CachePool_F(float labelWidth) {
      float next = Math.max(0.0F, labelWidth);
      if (Float.compare(this.sync, next) != 0) {
         this.sync = next;
         this.load_V();
      }

      return this;
   }

   public CachePool init_CachePool_Z(boolean alignControlEnd) {
      if (this.write_Z != alignControlEnd) {
         this.write_Z = alignControlEnd;
         this.load_V();
      }

      return this;
   }

   public TableIndex init_TableIndex() {
      return this.init_TableIndex;
   }

   public TableIndex load_TableIndex() {
      return this.load_TableIndex;
   }

   private float load_F(SyncManager context) {
      if (this.sync > 0.0F) {
         return this.sync;
      } else {
         return this.init_TableIndex == null ? 0.0F : this.init_TableIndex.init_F_SyncManager(context);
      }
   }

   @Override
   public float init_F_SyncManager(SyncManager context) {
      IoWorker scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      int scaledGap = context.init_CharEncoder().load(this.init_I);
      float total = scaledPadding.init_I();
      total += this.load_F(context);
      if (this.init_TableIndex != null && this.init_TableIndex.write_Z() && this.load_TableIndex != null && this.load_TableIndex.write_Z()) {
         total += scaledGap;
      }

      if (this.load_TableIndex != null && this.load_TableIndex.write_Z()) {
         total += this.load_TableIndex.init_F_SyncManager(context);
      }

      return total;
   }

   @Override
   public float init_F_SyncManager_F(SyncManager context, float availableWidth) {
      IoWorker scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      float labelHeight = this.init_TableIndex != null && this.init_TableIndex.write_Z()
         ? this.init_TableIndex.init_F_SyncManager_F(context, availableWidth)
         : 0.0F;
      float controlHeight = this.load_TableIndex != null && this.load_TableIndex.write_Z()
         ? this.load_TableIndex.init_F_SyncManager_F(context, availableWidth)
         : 0.0F;
      return scaledPadding.write() + Math.max(labelHeight, controlHeight) + scaledPadding.sync();
   }

   @Override
   protected void load_V(SyncManager context) {
      IoWorker scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      int scaledGap = context.init_CharEncoder().load(this.init_I);
      float innerX = this.init_F + scaledPadding.read();
      float innerY = this.load_F + scaledPadding.write();
      float innerWidth = Math.max(0.0F, this.read_F - scaledPadding.init_I());
      float innerHeight = Math.max(0.0F, this.write - scaledPadding.load());
      float resolvedLabelWidth = Math.min(this.load_F(context), innerWidth);
      float controlX = innerX;
      if (this.init_TableIndex != null && this.init_TableIndex.write_Z()) {
         float labelHeight = Math.min(innerHeight, this.init_TableIndex.init_F_SyncManager_F(context, resolvedLabelWidth));
         float labelY = innerY + Math.max(0.0F, (innerHeight - labelHeight) / 2.0F);
         this.init_TableIndex.init_TableIndex_F(innerX, labelY, resolvedLabelWidth, labelHeight);
         controlX = innerX + resolvedLabelWidth + (this.load_TableIndex != null && this.load_TableIndex.write_Z() ? scaledGap : 0);
      }

      if (this.load_TableIndex != null && this.load_TableIndex.write_Z()) {
         float controlWidth = Math.max(0.0F, innerX + innerWidth - controlX);
         if (!this.load_TableIndex.sync_Z()) {
            controlWidth = Math.min(controlWidth, this.load_TableIndex.init_F_SyncManager(context));
         }

         if (this.write_Z && !this.load_TableIndex.sync_Z()) {
            controlX = Math.max(controlX, innerX + innerWidth - controlWidth);
         }

         float controlHeight = Math.min(innerHeight, this.load_TableIndex.init_F_SyncManager_F(context, controlWidth));
         float controlY = innerY + Math.max(0.0F, (innerHeight - controlHeight) / 2.0F);
         this.load_TableIndex.init_TableIndex_F(controlX, controlY, controlWidth, controlHeight);
      }
   }
}
