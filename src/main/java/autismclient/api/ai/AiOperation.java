package autismclient.api.ai;

import autismclient.ai.AiFailure;
import java.util.List;
import java.util.Objects;

public interface AiOperation {
   AiOperation.Result tick();

   default boolean canInterruptNow() {
      return true;
   }

   default boolean requiresReconciliationAfterEmergency() {
      return !this.canInterruptNow();
   }

   void cancel();

   record Result(AiOperation.Status status, String message, String dataJson, List<AiObservation> evidence, AiFailure failure) {
      public Result(AiOperation.Status status, String message, String dataJson, List<AiObservation> evidence) {
         this(status, message, dataJson, evidence, null);
      }

      public Result {
         Objects.requireNonNull(status);
         Objects.requireNonNull(message);
         Objects.requireNonNull(dataJson);
         evidence = List.copyOf(evidence);
      }

      public boolean terminal() {
         return this.status != AiOperation.Status.RUNNING;
      }

      public static AiOperation.Result failed(AiFailure failure, String message, String dataJson) {
         return new AiOperation.Result(AiOperation.Status.FAILED, message, dataJson, List.of(), failure);
      }

      public static AiOperation.Result timedOut(AiFailure failure, String message, String dataJson) {
         return new AiOperation.Result(AiOperation.Status.TIMED_OUT, message, dataJson, List.of(), failure == null ? AiFailure.prev : failure);
      }

      public AiFailure failure() {
         if (this.failure != null) {
            return this.failure;
         }

         AiFailure var10000;
         switch (this.status) {
            case FAILED:
               var10000 = AiFailure.flush;
               break;
            case CANCELLED:
               var10000 = AiFailure.size;
               break;
            case TIMED_OUT:
               var10000 = AiFailure.prev;
               break;
            default:
               var10000 = AiFailure.value;
         }

         return var10000;
      }
   }

   enum Status {
      RUNNING,
      SUCCEEDED,
      FAILED,
      CANCELLED,
      TIMED_OUT,
      UNCERTAIN;
   }
}
