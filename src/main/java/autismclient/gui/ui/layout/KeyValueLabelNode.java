package autismclient.gui.ui.layout;

public final class KeyValueLabelNode extends LayoutNode {
   private String key;
   private String value;
   private int keyColor = -4743522;
   private int valueColor = -791321;

   public KeyValueLabelNode(String key, String value) {
      this.key = key == null ? "" : key;
      this.value = value == null ? "" : value;
      this.nodeHeight = 11.0F;
   }

   public KeyValueLabelNode init(String value) {
      String next = value == null ? "" : value;
      if (!this.value.equals(next)) {
         this.value = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public KeyValueLabelNode init(int keyColor) {
      this.keyColor = keyColor;
      return this;
   }

   public KeyValueLabelNode load(int valueColor) {
      this.valueColor = valueColor;
      return this;
   }

   public KeyValueLabelNode withGrowX(boolean growX) {
      super.setGrowX(growX);
      return this;
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      return autismclient.gui.f.UiHelper.init(context.font()).measureWidth(this.key + this.value);
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      return 11.0F;
   }

   @Override
   public void render(LayoutContext context) {
      if (this.nodeVisible) {
         autismclient.gui.ui.UiTextRenderer text = autismclient.gui.f.UiHelper.init(context.font());
         int drawX = Math.round(this.nodeX);
         int drawY = Math.round(this.nodeY) + Math.max(1, (Math.round(this.nodeHeight) - 9) / 2);
         String safeKey = this.key == null ? "" : this.key;
         String safeValue = this.value == null ? "" : this.value;
         text.draw(context.graphics(), safeKey, drawX, drawY, context.applyAlpha(this.keyColor));
         int keyWidth = text.measureWidth(safeKey);
         text.drawFitted(
            context.graphics(),
            safeValue,
            drawX + keyWidth,
            drawY,
            Math.max(1, Math.round(this.nodeWidth) - keyWidth),
            context.applyAlpha(this.valueColor)
         );
      }
   }
}
