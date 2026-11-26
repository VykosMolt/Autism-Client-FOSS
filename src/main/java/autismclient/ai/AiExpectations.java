package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.api.custommenu.CustomMenuSnapshot;
import com.google.gson.JsonObject;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

final class AiExpectations implements AiOperation {
   private final JsonObject init_JsonObject;
   private final String init_Str;
   private final long init_J = System.currentTimeMillis();
   private final long load;
   private final long read = AiServerEvidence.init_J();
   private final long write;
   private final long update = AiServerEvidence.read_J();
   private int init_I;
   private boolean init_Z;

   AiExpectations(JsonObject args) {
      if (args.has("expected") && args.get("expected").isJsonObject()) {
         this.init_JsonObject = args.getAsJsonObject("expected").deepCopy();
         this.init_Str = AiJson.init_Str_JsonObject(args, "failureContains", "");
         if (this.init_JsonObject.isEmpty()) {
            throw new IllegalArgumentException("Expected result must declare an observable inventory, slot, menu, or message condition");
         }

         for (String key : this.init_JsonObject.keySet()) {
            if (!Set.of("inventory", "slot", "menu", "message").contains(key)) {
               throw new IllegalArgumentException("Unknown workflow postcondition " + key);
            }
         }

         if (this.init_JsonObject.has("inventory")) {
            JsonObject e = this.init_JsonObject.getAsJsonObject("inventory");
            if (AiJson.init_Str_JsonObject(e, "item", "").isBlank() || AiJson.init_I_JsonObject(e, "count", -1) < 0) {
               throw new IllegalArgumentException("Expected inventory requires item and nonnegative count");
            }
         }

         if (this.init_JsonObject.has("slot")) {
            JsonObject e = this.init_JsonObject.getAsJsonObject("slot");
            if (AiJson.init_I_JsonObject(e, "slot", -1) < 0 || AiJson.init_I_JsonObject(e, "count", -1) < 0 || AiJson.init_Str_JsonObject(e, "item", "").isBlank()) {
               throw new IllegalArgumentException("Expected slot requires slot, item and count");
            }
         }

         if (this.init_JsonObject.has("menu")) {
            JsonObject e = this.init_JsonObject.getAsJsonObject("menu");
            if (AiJson.init_Str_JsonObject(e, "adapter", "").isBlank()) {
               throw new IllegalArgumentException("Expected menu requires adapter ID");
            }
         }

         if (this.init_JsonObject.has("message") && AiJson.init_Str_JsonObject(this.init_JsonObject.getAsJsonObject("message"), "contains", "").isBlank()) {
            throw new IllegalArgumentException("Expected server message requires nonempty contains text");
         }

         CustomMenuSnapshot menu = autismclient.util.b.CustomMenuTracker.init_CustomMenuSnapshot();
         this.write = menu == null ? -1L : menu.generation();
         this.load = System.nanoTime() + Math.max(1, Math.min(300, AiJson.init_I_JsonObject(args, "timeoutSeconds", 20))) * 1000000000L;
      } else {
         throw new IllegalArgumentException("Expected result must declare an observable inventory, slot, menu, or message condition");
      }
   }

   @Override
   public AiOperation.Result tick() {
      Minecraft mc = Minecraft.getInstance();
      if (this.init_Z || mc.player == null || mc.level == null) {
         return this.init(AiOperation.Status.CANCELLED, "Workflow observation cancelled; dispatched effects may remain");
      }

      if (!this.init_Str.isBlank() && this.init(this.init_Str)) {
         return this.init(AiOperation.Status.FAILED, "Observed the configured server rejection response");
      }

      if (this.init()) {
         if (++this.init_I >= 5) {
            return this.init(AiOperation.Status.SUCCEEDED, "Observed configured server workflow postconditions");
         }
      } else {
         this.init_I = 0;
      }

      return System.nanoTime() > this.load
         ? this.init(AiOperation.Status.UNCERTAIN, "Submission was sent once but its configured outcome was not observed; inspect before retrying")
         : this.init(AiOperation.Status.RUNNING, "Waiting for the configured server workflow outcome");
   }

   private boolean init() {
      Minecraft mc = Minecraft.getInstance();
      if (this.init_JsonObject.has("inventory")) {
         if (AiServerEvidence.init_J() <= this.read) {
            return false;
         }

         JsonObject e = this.init_JsonObject.getAsJsonObject("inventory");
         int count = AiGameState.init_I_Str(AiJson.init_Str_JsonObject(e, "item", ""));
         int target = AiJson.init_I_JsonObject(e, "count", 0);
         if (AiJson.init_Z_JsonObject(e, "exact", false) ? count != target : count < target) {
            return false;
         }
      }

      if (this.init_JsonObject.has("slot")) {
         if (AiServerEvidence.init_J_I(mc.player.containerMenu.containerId) <= this.read) {
            return false;
         }

         JsonObject e = this.init_JsonObject.getAsJsonObject("slot");
         int index = AiJson.init_I_JsonObject(e, "slot", -1);
         AbstractContainerMenu menu = mc.player.containerMenu;
         if (index < 0 || index >= menu.slots.size()) {
            return false;
         }

         if (e.has("containerId") && menu.containerId != AiJson.init_I_JsonObject(e, "containerId", -1)) {
            return false;
         }

         ItemStack stack = menu.getSlot(index).getItem();
         if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(AiJson.init_Str_JsonObject(e, "item", ""))
            || stack.getCount() != AiJson.init_I_JsonObject(e, "count", -1)) {
            return false;
         }
      }

      if (this.init_JsonObject.has("menu")) {
         CustomMenuSnapshot current = autismclient.util.b.CustomMenuTracker.init_CustomMenuSnapshot();
         JsonObject e = this.init_JsonObject.getAsJsonObject("menu");
         if (current == null
            || current.generation() <= this.write
            || !current.adapterId().equals(AiJson.init_Str_JsonObject(e, "adapter", ""))
            || !current.title().contains(AiJson.init_Str_JsonObject(e, "titleContains", ""))) {
            return false;
         }
      }

      return !this.init_JsonObject.has("message") || this.init(AiJson.init_Str_JsonObject(this.init_JsonObject.getAsJsonObject("message"), "contains", ""));
   }

   private boolean init(String text) {
      return AiServerEvidence.init_List_I(32).stream().anyMatch(m -> ((Number)m.get("revision")).longValue() > this.update && m.get("text").toString().contains(text));
   }

   private AiOperation.Result init(AiOperation.Status status, String text) {
      return AiGameOperations.init_AiOperation_Result(status, text, AiJson.init_JsonObject_ObjArr("expected", this.init_JsonObject, "startedAt", this.init_J));
   }

   @Override
   public void cancel() {
      this.init_Z = true;
   }
}
