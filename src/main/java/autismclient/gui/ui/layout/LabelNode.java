package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;

public class LabelNode extends LayoutNode {
   private String init_Str;
   private l init_l;
   private boolean write_Z;
   private boolean update_Z;

   public LabelNode(String text, l tone) {
      this.init_Str = text == null ? "" : text;
      this.init_l = tone == null ? l.read : tone;
      this.nodeHeight = 10.0F;
   }

   public LabelNode init(String text) {
      String next = text == null ? "" : text;
      if (!this.init_Str.equals(next)) {
         this.init_Str = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public LabelNode init(l tone) {
      l next = tone == null ? l.read : tone;
      if (this.init_l != next) {
         this.init_l = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public LabelNode init_PoolEntry(boolean shadow) {
      this.write_Z = shadow;
      return this;
   }

   public LabelNode load_PoolEntry(boolean trimToBounds) {
      this.update_Z = trimToBounds;
      return this;
   }

   public LabelNode read_PoolEntry(boolean growX) {
      super.setGrowX(growX);
      return this;
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      return k.init(context.font(), this.init_Str, context.theme().init_Identifier_l(this.init_l), context.theme().load(this.init_l));
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      return context.theme().init_I_l_I(this.init_l, 3);
   }

   @Override
   public void render(LayoutContext context) {
      if (this.nodeVisible) {
         String displayComponent = this.init_Str;
         if (this.update_Z && this.nodeWidth > 0.0F) {
            displayComponent = k.init(
               context.font(),
               this.init_Str,
               Math.round(this.nodeWidth),
               context.theme().init_Identifier_l(this.init_l),
               context.theme().load(this.init_l)
            );
         }

         k.init(
            context.graphics(),
            context.font(),
            displayComponent,
            context.theme().init_Identifier_l(this.init_l),
            context.applyAlpha(context.theme().load(this.init_l)),
            Math.round(this.nodeX),
            UiLayoutMath.centerTextY(Math.round(this.nodeY), Math.round(this.nodeHeight), context.theme().init_I_l(this.init_l), context.theme().flush()),
            this.write_Z
         );
      }
   }
}
