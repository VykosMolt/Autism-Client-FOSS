package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;

final class AiDeferredControls implements AiOperation {
   private final AiOperation construction;
   private final List<AiBuilder.CellSpec> controls;
   private final Function<AiBuilder.CellSpec, AiOperation> activate;
   private final Consumer<AiDeferredControls> showVisual;
   private final Consumer<AiDeferredControls> removeVisual;
   private final LongSupplier clock;
   private final long deadline;
   private AiOperation activeControl;
   private AiOperation.Result constructionResult;
   private AiOperation.Result finalResult;
   private int controlIndex;

   AiDeferredControls(
      AiOperation construction,
      List<AiBuilder.CellSpec> controls,
      Function<AiBuilder.CellSpec, AiOperation> activate,
      long deadline,
      LongSupplier clock,
      Consumer<AiDeferredControls> visual,
      Consumer<AiDeferredControls> removeVisual
   ) {
      this.construction = construction;
      this.controls = List.copyOf(controls);
      this.activate = activate;
      this.deadline = deadline;
      this.clock = clock;
      this.showVisual = visual;
      this.removeVisual = removeVisual;
   }

   @Override
   public AiOperation.Result tick() {
      if (this.finalResult != null) {
         return this.finalResult;
      }

      if (this.constructionResult == null) {
         AiOperation.Result result = this.construction.tick();
         if (!result.terminal()) {
            return result;
         }

         if (result.status() != AiOperation.Status.SUCCEEDED) {
            return this.init(result, result.message(), AiJson.parseObject(result.dataJson()));
         }

         this.constructionResult = result;
         this.construction.cancel();
      }

      this.showVisual.accept(this);
      if (this.clock.getAsLong() > this.deadline) {
         return this.init(AiOperation.Status.TIMED_OUT, "Structure verified; control activation deadline reached", this.init());
      }

      if (this.controlIndex == this.controls.size()) {
         return this.init(AiOperation.Status.SUCCEEDED, "Blueprint complete; captured switch positions restored", this.init());
      }

      if (this.activeControl == null) {
         this.activeControl = this.activate.apply(this.controls.get(this.controlIndex));
      }

      AiOperation.Result result = this.activeControl.tick();
      if (!result.terminal()) {
         return AiGameOperations.result(
            AiOperation.Status.RUNNING, "Restoring switch " + (this.controlIndex + 1) + "/" + this.controls.size() + ": " + result.message(), this.init()
         );
      } else {
         this.activeControl.cancel();
         this.activeControl = null;
         if (result.status() != AiOperation.Status.SUCCEEDED) {
            JsonObject data = this.init();
            data.add("controlResult", AiJson.parseObject(result.dataJson()));
            return this.init(result, "Structure verified; switch activation stopped: " + result.message(), data);
         } else {
            this.controlIndex++;
            return AiGameOperations.result(AiOperation.Status.RUNNING, "Switch position verified", this.init());
         }
      }
   }

   private JsonObject init() {
      JsonObject data = this.constructionResult == null ? new JsonObject() : AiJson.parseObject(this.constructionResult.dataJson());
      data.addProperty("constructionVerifiedBeforeActivation", this.constructionResult != null);
      data.addProperty("activatedControls", this.controlIndex);
      data.addProperty("controls", this.controls.size());
      return data;
   }

   private AiOperation.Result init(AiOperation.Status status, String message, JsonObject data) {
      return this.init(AiGameOperations.result(status, message, data), message, data);
   }

   private AiOperation.Result init(AiOperation.Result result, String message, JsonObject data) {
      this.finalResult = new AiOperation.Result(result.status(), message, AiJson.GSON.toJson(data), result.evidence(), result.failure());
      this.construction.cancel();
      if (this.activeControl != null) {
         this.activeControl.cancel();
         this.activeControl = null;
      }

      this.removeVisual.accept(this);
      return this.finalResult;
   }

   @Override
   public boolean canInterruptNow() {
      return this.constructionResult == null
         ? this.construction.canInterruptNow()
         : this.activeControl == null || this.activeControl.canInterruptNow();
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.constructionResult == null
         ? this.construction.requiresReconciliationAfterEmergency()
         : this.activeControl != null && this.activeControl.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      if (this.finalResult == null) {
         this.init(AiOperation.Status.CANCELLED, "Build or switch activation cancelled", this.init());
      }
   }
}
