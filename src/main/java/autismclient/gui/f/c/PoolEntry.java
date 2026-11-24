package autismclient.gui.f.c;

import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;

public class PoolEntry extends TableIndex {
   private String init_Str;
   private l init_l;
   private boolean write_Z;
   private boolean update_Z;

   public PoolEntry(String text, l tone) {
      this.init_Str = text == null ? "" : text;
      this.init_l = tone == null ? l.read : tone;
      this.write = 10.0F;
   }

   public PoolEntry init(String text) {
      String next = text == null ? "" : text;
      if (!this.init_Str.equals(next)) {
         this.init_Str = next;
         this.load_V();
      }

      return this;
   }

   public PoolEntry init(l tone) {
      l next = tone == null ? l.read : tone;
      if (this.init_l != next) {
         this.init_l = next;
         this.load_V();
      }

      return this;
   }

   public PoolEntry init_PoolEntry(boolean shadow) {
      this.write_Z = shadow;
      return this;
   }

   public PoolEntry load_PoolEntry(boolean trimToBounds) {
      this.update_Z = trimToBounds;
      return this;
   }

   public PoolEntry read_PoolEntry(boolean growX) {
      super.init_TableIndex_Z(growX);
      return this;
   }

   @Override
   public float init_F_SyncManager(SyncManager context) {
      return k.init(context.init_Font(), this.init_Str, context.init_CharEncoder().init_Identifier_l(this.init_l), context.init_CharEncoder().load(this.init_l));
   }

   @Override
   public float init_F_SyncManager_F(SyncManager context, float availableWidth) {
      return context.init_CharEncoder().init_I_l_I(this.init_l, 3);
   }

   @Override
   public void init_V_SyncManager(SyncManager context) {
      if (this.init_Z) {
         String displayComponent = this.init_Str;
         if (this.update_Z && this.read_F > 0.0F) {
            displayComponent = k.init(
               context.init_Font(),
               this.init_Str,
               Math.round(this.read_F),
               context.init_CharEncoder().init_Identifier_l(this.init_l),
               context.init_CharEncoder().load(this.init_l)
            );
         }

         k.init(
            context.init_GuiGraphicsExtract(),
            context.init_Font(),
            displayComponent,
            context.init_CharEncoder().init_Identifier_l(this.init_l),
            context.init_I_I(context.init_CharEncoder().load(this.init_l)),
            Math.round(this.init_F),
            j.init(Math.round(this.load_F), Math.round(this.write), context.init_CharEncoder().init_I_l(this.init_l), context.init_CharEncoder().flush()),
            this.write_Z
         );
      }
   }
}
