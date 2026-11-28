package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;

public class LabelNode extends LayoutNode {
   private String text;
   private l tone;
   private boolean shadow;
   private boolean trimToBounds;

   public LabelNode(String text, l tone) {
      this.text = text == null ? "" : text;
      this.tone = tone == null ? l.read : tone;
      this.nodeHeight = 10.0F;
   }

   public LabelNode text(String text) {
      String next = text == null ? "" : text;
      if (!this.text.equals(next)) {
         this.text = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public LabelNode tone(l tone) {
      l next = tone == null ? l.read : tone;
      if (this.tone != next) {
         this.tone = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public LabelNode withShadow(boolean shadow) {
      this.shadow = shadow;
      return this;
   }

   public LabelNode setTrimToBounds(boolean trimToBounds) {
      this.trimToBounds = trimToBounds;
      return this;
   }

   public LabelNode withGrowX(boolean growX) {
      super.setGrowX(growX);
      return this;
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      return k.init(context.font(), this.text, context.theme().init_Identifier_l(this.tone), context.theme().load(this.tone));
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      return context.theme().init_I_l_I(this.tone, 3);
   }

   @Override
   public void render(LayoutContext context) {
      if (this.nodeVisible) {
         String displayComponent = this.text;
         if (this.trimToBounds && this.nodeWidth > 0.0F) {
            displayComponent = k.init(
               context.font(),
               this.text,
               Math.round(this.nodeWidth),
               context.theme().init_Identifier_l(this.tone),
               context.theme().load(this.tone)
            );
         }

         k.init(
            context.graphics(),
            context.font(),
            displayComponent,
            context.theme().init_Identifier_l(this.tone),
            context.applyAlpha(context.theme().load(this.tone)),
            Math.round(this.nodeX),
            UiLayoutMath.centerTextY(Math.round(this.nodeY), Math.round(this.nodeHeight), context.theme().init_I_l(this.tone), context.theme().flush()),
            this.shadow
         );
      }
   }
}
