package autismclient.ai;

import autismclient.api.ai.AiOperation;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

final class AiDeferredOperation implements AiOperation {
   private Supplier<AiOperation> pendingFactory;
   private AiOperation delegate;
   private boolean cancelled;

   AiDeferredOperation(Supplier<AiOperation> factory) {
      this.pendingFactory = Objects.requireNonNull(factory);
   }

   @Override
   public AiOperation.Result tick() {
      if (this.cancelled) {
         return new AiOperation.Result(AiOperation.Status.CANCELLED, "Operation cancelled before dispatch", "{}", List.of());
      }

      if (this.delegate == null) {
         Supplier<AiOperation> create = this.pendingFactory;
         this.pendingFactory = null;
         this.delegate = Objects.requireNonNull(create.get());
      }

      return this.delegate.tick();
   }

   @Override
   public boolean canInterruptNow() {
      return this.delegate == null || this.delegate.canInterruptNow();
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.delegate != null && this.delegate.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      if (!this.cancelled) {
         this.cancelled = true;
         this.pendingFactory = null;
         if (this.delegate != null) {
            this.delegate.cancel();
         }
      }
   }
}
