package autismclient.gui.ui.layout;

public final class KeyValueLabelNode extends LayoutNode {
   private String init_Str;
   private String load_Str;
   private int init_I = -4743522;
   private int load_I = -791321;

   public KeyValueLabelNode(String key, String value) {
      this.init_Str = key == null ? "" : key;
      this.load_Str = value == null ? "" : value;
      this.nodeHeight = 11.0F;
   }

   public KeyValueLabelNode init(String value) {
      String next = value == null ? "" : value;
      if (!this.load_Str.equals(next)) {
         this.load_Str = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public KeyValueLabelNode init(int keyColor) {
      this.init_I = keyColor;
      return this;
   }

   public KeyValueLabelNode load(int valueColor) {
      this.load_I = valueColor;
      return this;
   }

   public KeyValueLabelNode init_JsonUtil(boolean growX) {
      super.setGrowX(growX);
      return this;
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      return autismclient.gui.f.UiHelper.init(context.font()).measureWidth(this.init_Str + this.load_Str);
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
         String safeKey = this.init_Str == null ? "" : this.init_Str;
         String safeValue = this.load_Str == null ? "" : this.load_Str;
         text.draw(context.graphics(), safeKey, drawX, drawY, context.applyAlpha(this.init_I));
         int keyWidth = text.measureWidth(safeKey);
         text.drawFitted(
            context.graphics(),
            safeValue,
            drawX + keyWidth,
            drawY,
            Math.max(1, Math.round(this.nodeWidth) - keyWidth),
            context.applyAlpha(this.load_I)
         );
      }
   }
}
