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
   private static final List<String> init_List = List.of(
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

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final Module module = autismclient.modules.al.init_Module_Str("auto-fish");
         if (module == null) {
            throw new IllegalStateException("The auto-fish module is not installed");
         }

         final String spec = AiJson.init_Str_JsonObject(args, "item", "");
         final AiItems.ItemSpec wanted = spec.isBlank() ? null : AiItems.load_af_DataUtil(spec);
         int count = Math.max(1, Math.min(2304, AiJson.init_I_JsonObject(args, "count", 1)));
         boolean additional = AiJson.init_Z_JsonObject(args, "additional", false);
         final int timeoutSeconds = Math.max(30, Math.min(3600, AiJson.init_I_JsonObject(args, "timeoutSeconds", 600)));
         final int held = wanted == null ? init_I() : AiItems.init_I(wanted);
         final int goal = additional ? held + count : count;
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final boolean init_Z = module.isEnabled();
            final int init_I = mc.player.getInventory().getSelectedSlot();
            final Map<String, Integer> init_Map = AiFish.init_Map();
            int load_I;
            int read_I = held;
            boolean load_Z;
            boolean read_Z;
            autismclient.modules.ScopedToggleOverride.Inner_StreamUtil init_aP_StreamUtil;

            @Override
            public AiOperation.Result tick() {
               if (!this.read_Z && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.init_J) {
                     return this.init(
                        this.init_I_6() > held ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT,
                        "Fishing deadline reached with " + this.init_I_6() + "/" + goal
                     );
                  }

                  if (this.init_I_6() >= goal) {
                     return this.init(AiOperation.Status.SUCCEEDED, "Fishing target reached (" + this.init_I_6() + "/" + goal + ")");
                  }

                  ItemStack rod = mc.player.getMainHandItem();
                  if (!rod.is(Items.FISHING_ROD)) {
                     if (AiItems.read(AiItems.load_af_DataUtil("minecraft:fishing_rod")) < 0) {
                        JsonObject missing = new JsonObject();
                        missing.addProperty("minecraft:fishing_rod", 1);
                        return this.init(AiOperation.Status.FAILED, "No fishing rod in inventory", AiJson.init_JsonObject_ObjArr("missing", missing));
                     } else {
                        return this.init("Holding the fishing rod");
                     }
                  } else {
                     if (rod.isDamageableItem() && rod.getMaxDamage() - rod.getDamageValue() <= 3) {
                        return this.init(
                           AiOperation.Status.FAILED,
                           "Rod is about to break (" + (rod.getMaxDamage() - rod.getDamageValue()) + " uses left); stopped before losing it"
                        );
                     }

                     if (AiItems.init_I() < 1) {
                        return this.init(AiOperation.Status.FAILED, "Inventory is full; further catches would be lost");
                     }

                     if (!this.load_Z) {
                        this.load_Z = true;
                        if (!this.init_Z) {
                           this.init_aP_StreamUtil = AiModuleScope.init(module, true);
                        }

                        return this.init("Casting");
                     } else {
                        if (!module.isEnabled()) {
                           return this.init(AiOperation.Status.CANCELLED, "AutoFish was disabled while the operation ran");
                        }

                        int now = this.init_I_6();
                        if (now > this.read_I) {
                           this.read_I = now;
                           this.load_I = 0;
                        } else if (++this.load_I > 1200) {
                           return this.init(AiOperation.Status.FAILED, "Nothing caught for 60s; check that the bobber is in open water");
                        }

                        return this.init("Fishing (" + now + "/" + goal + ")");
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Fishing cancelled");
               }
            }

            int init_I_6() {
               return wanted == null ? AiFish.init_I() : AiItems.init_I(wanted);
            }

            JsonObject init_JsonObject_12() {
               JsonObject out = AiJson.init_JsonObject_ObjArr(
                  "item", spec.isBlank() ? "any catch" : spec, "goal", goal, "have", this.init_I_6(), "startedWith", held, "freeSlots", AiItems.init_I()
               );
               ItemStack rod = mc.player == null ? ItemStack.EMPTY : mc.player.getMainHandItem();
               if (rod.isDamageableItem()) {
                  out.addProperty("rodDurabilityLeft", rod.getMaxDamage() - rod.getDamageValue());
               }

               JsonArray caught = new JsonArray();
               AiFish.init_Map().forEach((item, amount) -> {
                  int delta = amount - this.init_Map.getOrDefault(item, 0);
                  if (delta > 0) {
                     caught.add(AiJson.init_JsonObject_ObjArr("item", item, "count", delta));
                  }
               });
               out.add("caught", caught);
               return out;
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_12().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, null);
            }

            AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.init_JsonObject_12();
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
               if (!this.read_Z) {
                  this.read_Z = true;
                  if (this.init_aP_StreamUtil != null) {
                     this.init_aP_StreamUtil.close();
                     this.init_aP_StreamUtil = null;
                  }

                  if (mc.player != null && mc.player.getInventory().getSelectedSlot() != this.init_I) {
                     autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, this.init_I);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Fishing requires a connected living player");
      }
   }

   private static int init_I() {
      int total = 0;

      for (String id : init_List) {
         total += AiGameState.init_I_Str(id);
      }

      return total;
   }

   private static Map<String, Integer> init_Map() {
      Map<String, Integer> counts = new LinkedHashMap<>();

      for (String id : init_List) {
         int amount = AiGameState.init_I_Str(id);
         if (amount > 0) {
            counts.put(id, amount);
         }
      }

      return counts;
   }
}
