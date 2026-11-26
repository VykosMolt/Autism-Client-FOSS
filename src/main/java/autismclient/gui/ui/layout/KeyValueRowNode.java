package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.ThemePaletteManager;

public class KeyValueRowNode extends LayoutNode {
   private String init_Str;
   private String load_Str;
   private float sync = 76.0F;
   private l init_l = l.write;
   private l load_l = l.read;
   private Integer init_Integer;
   private int init_I = 0;
   private Runnable init_Runnable;

   public KeyValueRowNode(String label, String value) {
      this.init_Str = label == null ? "" : label;
      this.load_Str = value == null ? "--" : value;
      this.write = 12.0F;
   }

   public KeyValueRowNode init_TaskQueue_Str(String label) {
      this.init_Str = label == null ? "" : label;
      return this;
   }

   public KeyValueRowNode load(String value) {
      this.load_Str = value == null ? "--" : value;
      return this;
   }

   public KeyValueRowNode init_TaskQueue_F(float labelWidth) {
      this.sync = Math.max(24.0F, labelWidth);
      return this;
   }

   public KeyValueRowNode init_TaskQueue_l(l labelTone) {
      this.init_l = labelTone == null ? l.write : labelTone;
      return this;
   }

   public KeyValueRowNode load(l valueTone) {
      this.load_l = valueTone == null ? l.read : valueTone;
      return this;
   }

   public KeyValueRowNode init_TaskQueue_Integer(Integer valueColorOverride) {
      this.init_Integer = valueColorOverride;
      return this;
   }

   public KeyValueRowNode init_TaskQueue_I(int textYOffset) {
      this.init_I = textYOffset;
      return this;
   }

   public KeyValueRowNode init_TaskQueue_Runnable(Runnable onPress) {
      this.init_Runnable = onPress;
      return this;
   }

   private boolean init_Z_4() {
      return this.init_Runnable != null && this.load_Z;
   }

   @Override
   public float init_F_SyncManager_F(LayoutContext context, float availableWidth) {
      int baselineHeight = 12;
      int labelHeight = context.init_CharEncoder().init_I_l(this.init_l) + context.init_CharEncoder().load(3);
      int valueHeight = context.init_CharEncoder().init_I_l(this.load_l) + context.init_CharEncoder().load(3);
      return Math.max(baselineHeight, Math.max(labelHeight, valueHeight));
   }

   @Override
   public void init_V_SyncManager(LayoutContext context) {
      if (this.init_Z) {
         int drawX = Math.round(this.init_F);
         int drawY = Math.round(this.load_F);
         int drawW = Math.round(this.read_F);
         int drawH = Math.round(this.write);
         int minSegment = context.init_CharEncoder().load(24);
         int labelW = Math.round(Math.min(this.sync, Math.max(minSegment, drawW - minSegment)));
         int valueX = drawX + labelW;
         int valueW = Math.max(1, drawW - labelW);
         boolean clickable = this.init_Z_4();
         boolean hovered = clickable && this.init_Z_F_F(context.init_F(), context.load());
         if (hovered) {
            context.init_GuiGraphicsExtract()
               .fill(drawX, drawY, drawX + drawW, drawY + drawH, context.init_I_I(ThemePaletteManager.recolor(339024671, ThemePaletteManager.ColorRole.ACCENT)));
         }

         int labelColor = clickable && hovered
            ? UiLayoutMath.lerpColor(context.init_CharEncoder().load(this.init_l), ThemePaletteManager.recolor(-791321, ThemePaletteManager.ColorRole.TEXT), 0.35F)
            : context.init_CharEncoder().load(this.init_l);
         int valueColor = this.init_Integer != null ? this.init_Integer : context.init_CharEncoder().load(this.load_l);
         if (clickable && hovered) {
            valueColor = UiLayoutMath.lerpColor(valueColor, ThemePaletteManager.recolor(-2314, ThemePaletteManager.ColorRole.TEXT), 0.28F);
         }

         int labelTextY = UiLayoutMath.centerTextY(drawY, drawH, context.init_CharEncoder().init_I_l(this.init_l), context.init_CharEncoder().flush() + this.init_I);
         int valueTextY = UiLayoutMath.centerTextY(drawY, drawH, context.init_CharEncoder().init_I_l(this.load_l), context.init_CharEncoder().flush() + this.init_I);
         String displayLabel = k.init(
            context.init_Font(),
            this.init_Str,
            Math.max(1, labelW - context.init_CharEncoder().load(4)),
            context.init_CharEncoder().init_Identifier_l(this.init_l),
            labelColor
         );
         String displayValue = k.init(
            context.init_Font(),
            this.load_Str,
            Math.max(1, valueW - context.init_CharEncoder().load(2)),
            context.init_CharEncoder().init_Identifier_l(this.load_l),
            valueColor
         );
         k.init(
            context.init_GuiGraphicsExtract(),
            context.init_Font(),
            displayLabel,
            context.init_CharEncoder().init_Identifier_l(this.init_l),
            context.init_I_I(labelColor),
            drawX,
            labelTextY,
            false
         );
         k.init(
            context.init_GuiGraphicsExtract(),
            context.init_Font(),
            displayValue,
            context.init_CharEncoder().init_Identifier_l(this.load_l),
            context.init_I_I(valueColor),
            valueX,
            valueTextY,
            false
         );
      }
   }

   @Override
   public boolean init_Z_SyncManager_2(LayoutContext context, float mouseX, float mouseY, int button) {
      if (button == 0 && this.init_Z_4() && this.init_Z_F_F(mouseX, mouseY)) {
         this.init_Runnable.run();
         return true;
      } else {
         return false;
      }
   }
}
