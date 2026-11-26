package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;

final class AiDeferredControls implements AiOperation {
   private final AiOperation init_AiOperation;
   private final List<AiBuilder.CellSpec> init_List;
   private final Function<AiBuilder.CellSpec, AiOperation> init_Function;
   private final Consumer<AiDeferredControls> init_Consumer;
   private final Consumer<AiDeferredControls> load_Consumer;
   private final LongSupplier init_LongSupplier;
   private final long init_J;
   private AiOperation load_AiOperation;
   private AiOperation.Result init_AiOperation_Result;
   private AiOperation.Result load_AiOperation_Result;
   private int init_I;

   AiDeferredControls(
      AiOperation construction,
      List<AiBuilder.CellSpec> controls,
      Function<AiBuilder.CellSpec, AiOperation> activate,
      long deadline,
      LongSupplier clock,
      Consumer<AiDeferredControls> visual,
      Consumer<AiDeferredControls> removeVisual
   ) {
      this.init_AiOperation = construction;
      this.init_List = List.copyOf(controls);
      this.init_Function = activate;
      this.init_J = deadline;
      this.init_LongSupplier = clock;
      this.init_Consumer = visual;
      this.load_Consumer = removeVisual;
   }

   @Override
   public AiOperation.Result tick() {
      if (this.load_AiOperation_Result != null) {
         return this.load_AiOperation_Result;
      }

      if (this.init_AiOperation_Result == null) {
         AiOperation.Result result = this.init_AiOperation.tick();
         if (!result.terminal()) {
            return result;
         }

         if (result.status() != AiOperation.Status.SUCCEEDED) {
            return this.init(result, result.message(), AiJson.parseObject(result.dataJson()));
         }

         this.init_AiOperation_Result = result;
         this.init_AiOperation.cancel();
      }

      this.init_Consumer.accept(this);
      if (this.init_LongSupplier.getAsLong() > this.init_J) {
         return this.init(AiOperation.Status.TIMED_OUT, "Structure verified; control activation deadline reached", this.init());
      }

      if (this.init_I == this.init_List.size()) {
         return this.init(AiOperation.Status.SUCCEEDED, "Blueprint complete; captured switch positions restored", this.init());
      }

      if (this.load_AiOperation == null) {
         this.load_AiOperation = this.init_Function.apply(this.init_List.get(this.init_I));
      }

      AiOperation.Result result = this.load_AiOperation.tick();
      if (!result.terminal()) {
         return AiGameOperations.result(
            AiOperation.Status.RUNNING, "Restoring switch " + (this.init_I + 1) + "/" + this.init_List.size() + ": " + result.message(), this.init()
         );
      } else {
         this.load_AiOperation.cancel();
         this.load_AiOperation = null;
         if (result.status() != AiOperation.Status.SUCCEEDED) {
            JsonObject data = this.init();
            data.add("controlResult", AiJson.parseObject(result.dataJson()));
            return this.init(result, "Structure verified; switch activation stopped: " + result.message(), data);
         } else {
            this.init_I++;
            return AiGameOperations.result(AiOperation.Status.RUNNING, "Switch position verified", this.init());
         }
      }
   }

   private JsonObject init() {
      JsonObject data = this.init_AiOperation_Result == null ? new JsonObject() : AiJson.parseObject(this.init_AiOperation_Result.dataJson());
      data.addProperty("constructionVerifiedBeforeActivation", this.init_AiOperation_Result != null);
      data.addProperty("activatedControls", this.init_I);
      data.addProperty("controls", this.init_List.size());
      return data;
   }

   private AiOperation.Result init(AiOperation.Status status, String message, JsonObject data) {
      return this.init(AiGameOperations.result(status, message, data), message, data);
   }

   private AiOperation.Result init(AiOperation.Result result, String message, JsonObject data) {
      this.load_AiOperation_Result = new AiOperation.Result(result.status(), message, AiJson.GSON.toJson(data), result.evidence(), result.failure());
      this.init_AiOperation.cancel();
      if (this.load_AiOperation != null) {
         this.load_AiOperation.cancel();
         this.load_AiOperation = null;
      }

      this.load_Consumer.accept(this);
      return this.load_AiOperation_Result;
   }

   @Override
   public boolean canInterruptNow() {
      return this.init_AiOperation_Result == null
         ? this.init_AiOperation.canInterruptNow()
         : this.load_AiOperation == null || this.load_AiOperation.canInterruptNow();
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_AiOperation_Result == null
         ? this.init_AiOperation.requiresReconciliationAfterEmergency()
         : this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      if (this.load_AiOperation_Result == null) {
         this.init(AiOperation.Status.CANCELLED, "Build or switch activation cancelled", this.init());
      }
   }
}
