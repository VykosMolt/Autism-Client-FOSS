package autismclient.util;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

final class GuardedInputTicket {
   final String owner;
   private final BooleanSupplier init_BooleanSupplier;
   private final Consumer<Boolean> init_Consumer;
   private boolean init_Z;
   private boolean load;

   GuardedInputTicket(String owner, BooleanSupplier guard, Consumer<Boolean> receipt) {
      this.owner = owner;
      this.init_BooleanSupplier = guard;
      this.init_Consumer = receipt;
   }

   boolean isValid() {
      return !this.load && this.init_BooleanSupplier.getAsBoolean();
   }

   void markSent() {
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
