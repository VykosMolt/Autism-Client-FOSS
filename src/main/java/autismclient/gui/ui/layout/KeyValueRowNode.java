package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.ThemePaletteManager;

public class KeyValueRowNode extends LayoutNode {
   private String labelText;
   private String valueText;
   private float labelWidth = 76.0F;
   private l labelTone = l.write;
   private l valueTone = l.read;
   private Integer valueColorOverride;
   private int textYOffset = 0;
   private Runnable onPress;

   public KeyValueRowNode(String label, String value) {
      this.labelText = label == null ? "" : label;
      this.valueText = value == null ? "--" : value;
      this.nodeHeight = 12.0F;
   }

   public KeyValueRowNode label(String label) {
      this.labelText = label == null ? "" : label;
      return this;
   }

   public KeyValueRowNode value(String value) {
      this.valueText = value == null ? "--" : value;
      return this;
   }

   public KeyValueRowNode setLabelWidth(float labelWidth) {
      this.labelWidth = Math.max(24.0F, labelWidth);
      return this;
   }

   public KeyValueRowNode labelTone(l labelTone) {
      this.labelTone = labelTone == null ? l.write : labelTone;
      return this;
   }

   public KeyValueRowNode valueTone(l valueTone) {
      this.valueTone = valueTone == null ? l.read : valueTone;
      return this;
   }

   public KeyValueRowNode setValueColor(Integer valueColorOverride) {
      this.valueColorOverride = valueColorOverride;
      return this;
   }

   public KeyValueRowNode textYOffset(int textYOffset) {
      this.textYOffset = textYOffset;
      return this;
   }

   public KeyValueRowNode setOnPress(Runnable onPress) {
      this.onPress = onPress;
      return this;
   }

   private boolean isPressable() {
      return this.onPress != null && this.nodeEnabled;
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      int baselineHeight = 12;
      int labelHeight = context.theme().init_I_l(this.labelTone) + context.theme().load(3);
      int valueHeight = context.theme().init_I_l(this.valueTone) + context.theme().load(3);
      return Math.max(baselineHeight, Math.max(labelHeight, valueHeight));
   }

   @Override
   public void render(LayoutContext context) {
      if (this.nodeVisible) {
         int drawX = Math.round(this.nodeX);
         int drawY = Math.round(this.nodeY);
         int drawW = Math.round(this.nodeWidth);
         int drawH = Math.round(this.nodeHeight);
         int minSegment = context.theme().load(24);
         int labelW = Math.round(Math.min(this.labelWidth, Math.max(minSegment, drawW - minSegment)));
         int valueX = drawX + labelW;
         int valueW = Math.max(1, drawW - labelW);
         boolean clickable = this.isPressable();
         boolean hovered = clickable && this.containsPoint(context.mouseX(), context.mouseY());
         if (hovered) {
            context.graphics()
               .fill(drawX, drawY, drawX + drawW, drawY + drawH, context.applyAlpha(ThemePaletteManager.recolor(339024671, ThemePaletteManager.ColorRole.ACCENT)));
         }

         int labelColor = clickable && hovered
            ? UiLayoutMath.lerpColor(context.theme().load(this.labelTone), ThemePaletteManager.recolor(-791321, ThemePaletteManager.ColorRole.TEXT), 0.35F)
            : context.theme().load(this.labelTone);
         int valueColor = this.valueColorOverride != null ? this.valueColorOverride : context.theme().load(this.valueTone);
         if (clickable && hovered) {
            valueColor = UiLayoutMath.lerpColor(valueColor, ThemePaletteManager.recolor(-2314, ThemePaletteManager.ColorRole.TEXT), 0.28F);
         }

         int labelTextY = UiLayoutMath.centerTextY(drawY, drawH, context.theme().init_I_l(this.labelTone), context.theme().flush() + this.textYOffset);
         int valueTextY = UiLayoutMath.centerTextY(drawY, drawH, context.theme().init_I_l(this.valueTone), context.theme().flush() + this.textYOffset);
         String displayLabel = k.init(
            context.font(),
            this.labelText,
            Math.max(1, labelW - context.theme().load(4)),
            context.theme().init_Identifier_l(this.labelTone),
            labelColor
         );
         String displayValue = k.init(
            context.font(),
            this.valueText,
            Math.max(1, valueW - context.theme().load(2)),
            context.theme().init_Identifier_l(this.valueTone),
            valueColor
         );
         k.init(
            context.graphics(),
            context.font(),
            displayLabel,
            context.theme().init_Identifier_l(this.labelTone),
            context.applyAlpha(labelColor),
            drawX,
            labelTextY,
            false
         );
         k.init(
            context.graphics(),
            context.font(),
            displayValue,
            context.theme().init_Identifier_l(this.valueTone),
            context.applyAlpha(valueColor),
            valueX,
            valueTextY,
            false
         );
      }
   }

   @Override
   public boolean mouseClicked(LayoutContext context, float mouseX, float mouseY, int button) {
      if (button == 0 && this.isPressable() && this.containsPoint(mouseX, mouseY)) {
         this.onPress.run();
         return true;
      } else {
         return false;
      }
   }
}
