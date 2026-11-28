package autismclient.gui.ui.layout;

public class LabeledControlRow extends LayoutContainer {
   private final LayoutNode label;
   private final LayoutNode control;
   private EdgeInsets padding;
   private int gap;
   private float fixedLabelWidth;
   private boolean alignControlEnd;

   public LabeledControlRow(LayoutNode labelNode, LayoutNode controlNode) {
      this.padding = EdgeInsets.ZERO;
      this.gap = 0;
      this.fixedLabelWidth = -1.0F;
      this.alignControlEnd = false;
      this.label = this.addChild(labelNode);
      this.control = this.addChild(controlNode);
   }

   public LabeledControlRow setPadding(EdgeInsets padding) {
      EdgeInsets next = padding == null ? EdgeInsets.ZERO : padding;
      if (!this.padding.equals(next)) {
         this.padding = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public LabeledControlRow setGap(int gap) {
      int next = Math.max(0, gap);
      if (this.gap != next) {
         this.gap = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public LabeledControlRow setLabelWidth(float labelWidth) {
      float next = Math.max(0.0F, labelWidth);
      if (Float.compare(this.fixedLabelWidth, next) != 0) {
         this.fixedLabelWidth = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public LabeledControlRow setAlignControlEnd(boolean alignControlEnd) {
      if (this.alignControlEnd != alignControlEnd) {
         this.alignControlEnd = alignControlEnd;
         this.markLayoutDirty();
      }

      return this;
   }

   public LayoutNode getLabel() {
      return this.label;
   }

   public LayoutNode getControl() {
      return this.control;
   }

   private float labelWidthFor(LayoutContext context) {
      if (this.fixedLabelWidth > 0.0F) {
         return this.fixedLabelWidth;
      } else {
         return this.label == null ? 0.0F : this.label.measurePreferredWidth(context);
      }
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.padding);
      int scaledGap = context.theme().load(this.gap);
      float total = scaledPadding.init_I();
      total += this.labelWidthFor(context);
      if (this.label != null && this.label.isVisible() && this.control != null && this.control.isVisible()) {
         total += scaledGap;
      }

      if (this.control != null && this.control.isVisible()) {
         total += this.control.measurePreferredWidth(context);
      }

      return total;
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.padding);
      float labelHeight = this.label != null && this.label.isVisible()
         ? this.label.measureHeight(context, availableWidth)
         : 0.0F;
      float controlHeight = this.control != null && this.control.isVisible()
         ? this.control.measureHeight(context, availableWidth)
         : 0.0F;
      return scaledPadding.write() + Math.max(labelHeight, controlHeight) + scaledPadding.bottom();
   }

   @Override
   protected void layoutChildren(LayoutContext context) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.padding);
      int scaledGap = context.theme().load(this.gap);
      float innerX = this.nodeX + scaledPadding.read();
      float innerY = this.nodeY + scaledPadding.write();
      float innerWidth = Math.max(0.0F, this.nodeWidth - scaledPadding.init_I());
      float innerHeight = Math.max(0.0F, this.nodeHeight - scaledPadding.load());
      float resolvedLabelWidth = Math.min(this.labelWidthFor(context), innerWidth);
      float controlX = innerX;
      if (this.label != null && this.label.isVisible()) {
         float labelHeight = Math.min(innerHeight, this.label.measureHeight(context, resolvedLabelWidth));
         float labelY = innerY + Math.max(0.0F, (innerHeight - labelHeight) / 2.0F);
         this.label.setBounds(innerX, labelY, resolvedLabelWidth, labelHeight);
         controlX = innerX + resolvedLabelWidth + (this.control != null && this.control.isVisible() ? scaledGap : 0);
      }

      if (this.control != null && this.control.isVisible()) {
         float controlWidth = Math.max(0.0F, innerX + innerWidth - controlX);
         if (!this.control.isGrowX()) {
            controlWidth = Math.min(controlWidth, this.control.measurePreferredWidth(context));
         }

         if (this.alignControlEnd && !this.control.isGrowX()) {
            controlX = Math.max(controlX, innerX + innerWidth - controlWidth);
         }

         float controlHeight = Math.min(innerHeight, this.control.measureHeight(context, controlWidth));
         float controlY = innerY + Math.max(0.0F, (innerHeight - controlHeight) / 2.0F);
         this.control.setBounds(controlX, controlY, controlWidth, controlHeight);
      }
   }
}
