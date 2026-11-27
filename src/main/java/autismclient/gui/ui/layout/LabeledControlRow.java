package autismclient.gui.ui.layout;

public class LabeledControlRow extends LayoutContainer {
   private final LayoutNode init_TableIndex;
   private final LayoutNode load_TableIndex;
   private EdgeInsets init_IoWorker;
   private int init_I;
   private float sync;
   private boolean write_Z;

   public LabeledControlRow(LayoutNode labelNode, LayoutNode controlNode) {
      this.init_IoWorker = EdgeInsets.ZERO;
      this.init_I = 0;
      this.sync = -1.0F;
      this.write_Z = false;
      this.init_TableIndex = this.addChild(labelNode);
      this.load_TableIndex = this.addChild(controlNode);
   }

   public LabeledControlRow setPadding(EdgeInsets padding) {
      EdgeInsets next = padding == null ? EdgeInsets.ZERO : padding;
      if (!this.init_IoWorker.equals(next)) {
         this.init_IoWorker = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public LabeledControlRow setGap(int gap) {
      int next = Math.max(0, gap);
      if (this.init_I != next) {
         this.init_I = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public LabeledControlRow setLabelWidth(float labelWidth) {
      float next = Math.max(0.0F, labelWidth);
      if (Float.compare(this.sync, next) != 0) {
         this.sync = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public LabeledControlRow setAlignControlEnd(boolean alignControlEnd) {
      if (this.write_Z != alignControlEnd) {
         this.write_Z = alignControlEnd;
         this.markLayoutDirty();
      }

      return this;
   }

   public LayoutNode getLabel() {
      return this.init_TableIndex;
   }

   public LayoutNode getControl() {
      return this.load_TableIndex;
   }

   private float load_F(LayoutContext context) {
      if (this.sync > 0.0F) {
         return this.sync;
      } else {
         return this.init_TableIndex == null ? 0.0F : this.init_TableIndex.measurePreferredWidth(context);
      }
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.init_IoWorker);
      int scaledGap = context.theme().load(this.init_I);
      float total = scaledPadding.init_I();
      total += this.load_F(context);
      if (this.init_TableIndex != null && this.init_TableIndex.isVisible() && this.load_TableIndex != null && this.load_TableIndex.isVisible()) {
         total += scaledGap;
      }

      if (this.load_TableIndex != null && this.load_TableIndex.isVisible()) {
         total += this.load_TableIndex.measurePreferredWidth(context);
      }

      return total;
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.init_IoWorker);
      float labelHeight = this.init_TableIndex != null && this.init_TableIndex.isVisible()
         ? this.init_TableIndex.measureHeight(context, availableWidth)
         : 0.0F;
      float controlHeight = this.load_TableIndex != null && this.load_TableIndex.isVisible()
         ? this.load_TableIndex.measureHeight(context, availableWidth)
         : 0.0F;
      return scaledPadding.write() + Math.max(labelHeight, controlHeight) + scaledPadding.bottom();
   }

   @Override
   protected void layoutChildren(LayoutContext context) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.init_IoWorker);
      int scaledGap = context.theme().load(this.init_I);
      float innerX = this.nodeX + scaledPadding.read();
      float innerY = this.nodeY + scaledPadding.write();
      float innerWidth = Math.max(0.0F, this.nodeWidth - scaledPadding.init_I());
      float innerHeight = Math.max(0.0F, this.nodeHeight - scaledPadding.load());
      float resolvedLabelWidth = Math.min(this.load_F(context), innerWidth);
      float controlX = innerX;
      if (this.init_TableIndex != null && this.init_TableIndex.isVisible()) {
         float labelHeight = Math.min(innerHeight, this.init_TableIndex.measureHeight(context, resolvedLabelWidth));
         float labelY = innerY + Math.max(0.0F, (innerHeight - labelHeight) / 2.0F);
         this.init_TableIndex.setBounds(innerX, labelY, resolvedLabelWidth, labelHeight);
         controlX = innerX + resolvedLabelWidth + (this.load_TableIndex != null && this.load_TableIndex.isVisible() ? scaledGap : 0);
      }

      if (this.load_TableIndex != null && this.load_TableIndex.isVisible()) {
         float controlWidth = Math.max(0.0F, innerX + innerWidth - controlX);
         if (!this.load_TableIndex.isGrowX()) {
            controlWidth = Math.min(controlWidth, this.load_TableIndex.measurePreferredWidth(context));
         }

         if (this.write_Z && !this.load_TableIndex.isGrowX()) {
            controlX = Math.max(controlX, innerX + innerWidth - controlWidth);
         }

         float controlHeight = Math.min(innerHeight, this.load_TableIndex.measureHeight(context, controlWidth));
         float controlY = innerY + Math.max(0.0F, (innerHeight - controlHeight) / 2.0F);
         this.load_TableIndex.setBounds(controlX, controlY, controlWidth, controlHeight);
      }
   }
}
