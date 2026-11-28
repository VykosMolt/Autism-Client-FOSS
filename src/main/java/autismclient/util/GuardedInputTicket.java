package autismclient.util;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

final class GuardedInputTicket {
   final String owner;
   private final BooleanSupplier guard;
   private final Consumer<Boolean> receipt;
   private boolean dispatched;
   private boolean resolved;

   GuardedInputTicket(String owner, BooleanSupplier guard, Consumer<Boolean> receipt) {
      this.owner = owner;
      this.guard = guard;
      this.receipt = receipt;
   }

   boolean isValid() {
      return !this.resolved && this.guard.getAsBoolean();
   }

   void markSent() {
      if (!this.resolved) {
         this.dispatched = true;
      }
   }

   void load() {
      this.resolve(this.dispatched);
   }

   void read() {
      this.resolve(false);
   }

   private void resolve(boolean sent) {
      if (!this.resolved) {
         this.resolved = true;
         this.receipt.accept(sent);
      }
   }
}
