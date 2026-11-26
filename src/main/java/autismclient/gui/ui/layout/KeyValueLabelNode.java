package autismclient.gui.ui.layout;

public final class KeyValueLabelNode extends LayoutNode {
   private String init_Str;
   private String load_Str;
   private int init_I = -4743522;
   private int load_I = -791321;

   public KeyValueLabelNode(String key, String value) {
      this.init_Str = key == null ? "" : key;
      this.load_Str = value == null ? "" : value;
      this.write = 11.0F;
   }

   public KeyValueLabelNode init(String value) {
      String next = value == null ? "" : value;
      if (!this.load_Str.equals(next)) {
         this.load_Str = next;
         this.load_V();
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
      super.init_TableIndex_Z(growX);
      return this;
   }

   @Override
   public float init_F_SyncManager(LayoutContext context) {
      return autismclient.gui.f.UiHelper.init(context.init_Font()).measureWidth(this.init_Str + this.load_Str);
   }

   @Override
   public float init_F_SyncManager_F(LayoutContext context, float availableWidth) {
      return 11.0F;
   }

   @Override
   public void init_V_SyncManager(LayoutContext context) {
      if (this.init_Z) {
         autismclient.gui.ui.UiTextRenderer text = autismclient.gui.f.UiHelper.init(context.init_Font());
         int drawX = Math.round(this.init_F);
         int drawY = Math.round(this.load_F) + Math.max(1, (Math.round(this.write) - 9) / 2);
         String safeKey = this.init_Str == null ? "" : this.init_Str;
         String safeValue = this.load_Str == null ? "" : this.load_Str;
         text.draw(context.init_GuiGraphicsExtract(), safeKey, drawX, drawY, context.init_I_I(this.init_I));
         int keyWidth = text.measureWidth(safeKey);
         text.drawFitted(
            context.init_GuiGraphicsExtract(),
            safeValue,
            drawX + keyWidth,
            drawY,
            Math.max(1, Math.round(this.read_F) - keyWidth),
            context.init_I_I(this.load_I)
         );
      }
   }
}
