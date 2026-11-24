package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.modules.Module;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

final class ax implements AiOperation {
   private static final ThreadLocal<ax> init_ThreadLocal = new ThreadLocal<>();
   private final List<AutoCloseable> init_List = new ArrayList<>();
   private AiOperation init_AiOperation;
   private boolean init_Z;
   private boolean load;

   static AiOperation init_AiOperation_Supplier(Supplier<AiOperation> factory) {
      return load(() -> new m(factory));
   }

   static AiOperation load(Supplier<AiOperation> factory) {
      ax scope = new ax();
      ax previous = init_ThreadLocal.get();
      init_ThreadLocal.set(scope);

      try {
         scope.init_AiOperation = Objects.requireNonNull(factory.get());
         return scope;
      } catch (Throwable error) {
         scope.init_V_Throwable(error);
         throw error;
      } finally {
         init_V_ax(previous);
      }
   }

   static <T extends AutoCloseable> T init_AutoCloseable_AutoCloseable(T control) {
      ax scope = init_ThreadLocal.get();
      if (scope != null) {
         if (scope.init_Z) {
            try {
               control.close();
            } catch (Exception cleanup) {
               throw new IllegalStateException("Operation module scope is closed", cleanup);
            }

            throw new IllegalStateException("Operation module scope is closed");
         }

         scope.init_List.add(control);
      }

      return control;
   }

   static autismclient.modules.aP.Inner_StreamUtil init(Module module, boolean enabled) {
      return init_AutoCloseable_AutoCloseable(module.borrowEnabled(enabled));
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
      if (this.init_Z) {
         return new AiOperation.Result(AiOperation.Status.CANCELLED, "Operation has released its modules", "{}", List.of());
      }

      ax previous = init_ThreadLocal.get();
      init_ThreadLocal.set(this);

      try {
         AiOperation.Result result = this.init_AiOperation.tick();
         if (result.terminal()) {
            this.load();
            this.init_V_Throwable(null);
         }

         return result;
      } catch (Throwable error) {
         try {
            this.load();
         } catch (Throwable cleanup) {
            if (cleanup != error) {
               error.addSuppressed(cleanup);
            }
         }

         this.init_V_Throwable(error);
         throw error;
      } finally {
         init_V_ax(previous);
      }
   }

   @Override
   public boolean canInterruptNow() {
      return this.init_AiOperation.canInterruptNow();
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_AiOperation.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      if (!this.init_Z) {
         ax previous = init_ThreadLocal.get();
         init_ThreadLocal.set(this);
         Throwable failed = null;

         try {
            this.load();
         } catch (Throwable error) {
            failed = error;
            throw error;
         } finally {
            try {
               this.init_V_Throwable(failed);
            } finally {
               init_V_ax(previous);
            }
         }
      }
   }

   private void load() {
      if (!this.load) {
         this.load = true;
         this.init_AiOperation.cancel();
      }
   }

   private void init_V_Throwable(Throwable failure) {
      if (!this.init_Z) {
         this.init_Z = true;
         Throwable first = failure;

         for (int i = this.init_List.size() - 1; i >= 0; i--) {
            try {
               this.init_List.get(i).close();
            } catch (Throwable error) {
               if (first == null) {
                  first = error;
               } else if (first != error) {
                  first.addSuppressed(error);
               }
            }
         }

         this.init_List.clear();
         if (failure == null && first != null) {
            throw new IllegalStateException("Module cleanup failed", first);
         }
      }
   }

   private static void init_V_ax(ax previous) {
      if (previous == null) {
         init_ThreadLocal.remove();
      } else {
         init_ThreadLocal.set(previous);
      }
   }
}
