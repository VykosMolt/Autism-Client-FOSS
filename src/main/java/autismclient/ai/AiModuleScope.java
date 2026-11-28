package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.modules.Module;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

final class AiModuleScope implements AiOperation {
   private static final ThreadLocal<AiModuleScope> CURRENT = new ThreadLocal<>();
   private final List<AutoCloseable> controls = new ArrayList<>();
   private AiOperation operation;
   private boolean closed;
   private boolean cancelled;

   static AiOperation wrap(Supplier<AiOperation> factory) {
      return load(() -> new AiDeferredOperation(factory));
   }

   static AiOperation load(Supplier<AiOperation> factory) {
      AiModuleScope scope = new AiModuleScope();
      AiModuleScope previous = CURRENT.get();
      CURRENT.set(scope);

      try {
         scope.operation = Objects.requireNonNull(factory.get());
         return scope;
      } catch (Throwable error) {
         scope.closeAll(error);
         throw error;
      } finally {
         restore(previous);
      }
   }

   static <T extends AutoCloseable> T register(T control) {
      AiModuleScope scope = CURRENT.get();
      if (scope != null) {
         if (scope.closed) {
            try {
               control.close();
            } catch (Exception cleanup) {
               throw new IllegalStateException("Operation module scope is closed", cleanup);
            }

            throw new IllegalStateException("Operation module scope is closed");
         }

         scope.controls.add(control);
      }

      return control;
   }

   static autismclient.modules.ScopedToggleOverride.Inner_StreamUtil init(Module module, boolean enabled) {
      return register(module.borrowEnabled(enabled));
   }

   static void init() {
      for (String id : List.of("scaffold", "kill-aura", "auto-armor", "inventory-tweaks", "auto-farm", "auto-tool")) {
         Module module = autismclient.modules.al.init_Module_Str(id);
         if (module != null && module.isEnabled()) {
            init(module, false);
         }
      }
   }

   @Override
   public AiOperation.Result tick() {
      if (this.closed) {
         return new AiOperation.Result(AiOperation.Status.CANCELLED, "Operation has released its modules", "{}", List.of());
      }

      AiModuleScope previous = CURRENT.get();
      CURRENT.set(this);

      try {
         AiOperation.Result result = this.operation.tick();
         if (result.terminal()) {
            this.cancelOnce();
            this.closeAll(null);
         }

         return result;
      } catch (Throwable error) {
         try {
            this.cancelOnce();
         } catch (Throwable cleanup) {
            if (cleanup != error) {
               error.addSuppressed(cleanup);
            }
         }

         this.closeAll(error);
         throw error;
      } finally {
         restore(previous);
      }
   }

   @Override
   public boolean canInterruptNow() {
      return this.operation.canInterruptNow();
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.operation.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      if (!this.closed) {
         AiModuleScope previous = CURRENT.get();
         CURRENT.set(this);
         Throwable failed = null;

         try {
            this.cancelOnce();
         } catch (Throwable error) {
            failed = error;
            throw error;
         } finally {
            try {
               this.closeAll(failed);
            } finally {
               restore(previous);
            }
         }
      }
   }

   private void cancelOnce() {
      if (!this.cancelled) {
         this.cancelled = true;
         this.operation.cancel();
      }
   }

   private void closeAll(Throwable failure) {
      if (!this.closed) {
         this.closed = true;
         Throwable first = failure;

         for (int i = this.controls.size() - 1; i >= 0; i--) {
            try {
               this.controls.get(i).close();
            } catch (Throwable error) {
               if (first == null) {
                  first = error;
               } else if (first != error) {
                  first.addSuppressed(error);
               }
            }
         }

         this.controls.clear();
         if (failure == null && first != null) {
            throw new IllegalStateException("Module cleanup failed", first);
         }
      }
   }

   private static void restore(AiModuleScope previous) {
      if (previous == null) {
         CURRENT.remove();
      } else {
         CURRENT.set(previous);
      }
   }
}
