package autismclient.util;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

final class cW {
   final String init_Str;
   private final BooleanSupplier init_BooleanSupplier;
   private final Consumer<Boolean> init_Consumer;
   private boolean init_Z;
   private boolean load;

   cW(String owner, BooleanSupplier guard, Consumer<Boolean> receipt) {
      this.init_Str = owner;
      this.init_BooleanSupplier = guard;
      this.init_Consumer = receipt;
   }

   boolean init_Z() {
      return !this.load && this.init_BooleanSupplier.getAsBoolean();
   }

   void init_V() {
      if (!this.load) {
         this.init_Z = true;
      }
   }

   void load() {
      this.init(this.init_Z);
   }

   void read() {
      this.init(false);
   }

   private void init(boolean sent) {
      if (!this.load) {
         this.load = true;
         this.init_Consumer.accept(sent);
      }
   }
}
