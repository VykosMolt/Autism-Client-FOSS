package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.modules.Module;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class AiFish {
   private static final int init_I = 1200;
   private static final int load = 3;
   private static final int read = 1;
   private static final List<String> CATCH_IDS = List.of(
      "minecraft:cod",
      "minecraft:salmon",
      "minecraft:tropical_fish",
      "minecraft:pufferfish",
      "minecraft:enchanted_book",
      "minecraft:name_tag",
      "minecraft:nautilus_shell",
      "minecraft:saddle",
      "minecraft:bow",
      "minecraft:fishing_rod",
      "minecraft:lily_pad",
      "minecraft:bowl",
      "minecraft:leather",
      "minecraft:bone",
      "minecraft:ink_sac",
      "minecraft:tripwire_hook",
      "minecraft:rotten_flesh",
      "minecraft:string",
      "minecraft:stick",
      "minecraft:bamboo",
      "minecraft:cocoa_beans"
   );

   private AiFish() {
   }

   static AiOperation create(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final Module module = autismclient.modules.al.init_Module_Str("auto-fish");
         if (module == null) {
            throw new IllegalStateException("The auto-fish module is not installed");
         }

         final String spec = AiJson.getString(args, "item", "");
         final AiItems.ItemSpec wanted = spec.isBlank() ? null : AiItems.resolveItem(spec);
         int count = Math.max(1, Math.min(2304, AiJson.getInt(args, "count", 1)));
         boolean additional = AiJson.getBoolean(args, "additional", false);
         final int timeoutSeconds = Math.max(30, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 600)));
         final int held = wanted == null ? countCatch() : AiItems.countInInventory(wanted);
         final int goal = additional ? held + count : count;
         return new AiOperation() {
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final boolean moduleWasEnabled = module.isEnabled();
            final int originalSlot = mc.player.getInventory().getSelectedSlot();
            final Map<String, Integer> initialCounts = AiFish.catchCounts();
            int idleTicks;
            int bestCount = held;
            boolean started;
            boolean cancelled;
            autismclient.modules.ScopedToggleOverride.OverrideHandle moduleOverride;

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.deadline) {
                     return this.finish(
                        this.currentCount() > held ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT,
                        "Fishing deadline reached with " + this.currentCount() + "/" + goal
                     );
                  }

                  if (this.currentCount() >= goal) {
                     return this.finish(AiOperation.Status.SUCCEEDED, "Fishing target reached (" + this.currentCount() + "/" + goal + ")");
                  }

                  ItemStack rod = mc.player.getMainHandItem();
                  if (!rod.is(Items.FISHING_ROD)) {
                     if (AiItems.equipHotbar(AiItems.resolveItem("minecraft:fishing_rod")) < 0) {
                        JsonObject missing = new JsonObject();
                        missing.addProperty("minecraft:fishing_rod", 1);
                        return this.finish(AiOperation.Status.FAILED, "No fishing rod in inventory", AiJson.objectOf("missing", missing));
                     } else {
                        return this.running("Holding the fishing rod");
                     }
                  } else {
                     if (rod.isDamageableItem() && rod.getMaxDamage() - rod.getDamageValue() <= 3) {
                        return this.finish(
                           AiOperation.Status.FAILED,
                           "Rod is about to break (" + (rod.getMaxDamage() - rod.getDamageValue()) + " uses left); stopped before losing it"
                        );
                     }

                     if (AiItems.countFreeSlots() < 1) {
                        return this.finish(AiOperation.Status.FAILED, "Inventory is full; further catches would be lost");
                     }

                     if (!this.started) {
                        this.started = true;
                        if (!this.moduleWasEnabled) {
                           this.moduleOverride = AiModuleScope.borrow(module, true);
                        }

                        return this.running("Casting");
                     } else {
                        if (!module.isEnabled()) {
                           return this.finish(AiOperation.Status.CANCELLED, "AutoFish was disabled while the operation ran");
                        }

                        int now = this.currentCount();
                        if (now > this.bestCount) {
                           this.bestCount = now;
                           this.idleTicks = 0;
                        } else if (++this.idleTicks > 1200) {
                           return this.finish(AiOperation.Status.FAILED, "Nothing caught for 60s; check that the bobber is in open water");
                        }

                        return this.running("Fishing (" + now + "/" + goal + ")");
                     }
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Fishing cancelled");
               }
            }

            int currentCount() {
               return wanted == null ? AiFish.countCatch() : AiItems.countInInventory(wanted);
            }

            JsonObject progressJson() {
               JsonObject out = AiJson.objectOf(
                  "item", spec.isBlank() ? "any catch" : spec, "goal", goal, "have", this.currentCount(), "startedWith", held, "freeSlots", AiItems.countFreeSlots()
               );
               ItemStack rod = mc.player == null ? ItemStack.EMPTY : mc.player.getMainHandItem();
               if (rod.isDamageableItem()) {
                  out.addProperty("rodDurabilityLeft", rod.getMaxDamage() - rod.getDamageValue());
               }

               JsonArray caught = new JsonArray();
               AiFish.catchCounts().forEach((item, amount) -> {
                  int delta = amount - this.initialCounts.getOrDefault(item, 0);
                  if (delta > 0) {
                     caught.add(AiJson.objectOf("item", item, "count", delta));
                  }
               });
               out.add("caught", caught);
               return out;
            }

            AiOperation.Result running(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressJson().toString(), List.of());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               return this.finish(status, message, null);
            }

            AiOperation.Result finish(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.progressJson();
               if (extra != null) {
                  for (Entry<String, JsonElement> entry : extra.entrySet()) {
                     data.add(entry.getKey(), entry.getValue());
                  }
               }

               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of());
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  if (this.moduleOverride != null) {
                     this.moduleOverride.close();
                     this.moduleOverride = null;
                  }

                  if (mc.player != null && mc.player.getInventory().getSelectedSlot() != this.originalSlot) {
                     autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, this.originalSlot);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Fishing requires a connected living player");
      }
   }

   private static int countCatch() {
      int total = 0;

      for (String id : CATCH_IDS) {
         total += AiGameState.countInventoryItem(id);
      }

      return total;
   }

   private static Map<String, Integer> catchCounts() {
      Map<String, Integer> counts = new LinkedHashMap<>();

      for (String id : CATCH_IDS) {
         int amount = AiGameState.countInventoryItem(id);
         if (amount > 0) {
            counts.put(id, amount);
         }
      }

      return counts;
   }
}
