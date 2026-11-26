package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.function.IntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class AiEnchant {
   static final int init = 0;
   static final int load = 1;
   static final int read = 3;

   private AiEnchant() {
   }

   static JsonObject init(
      int button, int requiredLevel, int clueId, int clueLevel, String clueEnchantment, int lapisAvailable, int xpLevel, boolean inputPresent
   ) {
      int lapisCost = button + 1;
      int xpLevelsTaken = button + 1;
      int levelNeeded = Math.max(button + 1, requiredLevel);
      boolean enoughLapis = lapisAvailable >= lapisCost;
      boolean enoughLevels = xpLevel >= levelNeeded;
      JsonObject option = AiJson.objectOf(
         "button",
         button,
         "requiredLevel",
         requiredLevel,
         "lapisCost",
         lapisCost,
         "xpLevelsTaken",
         xpLevelsTaken,
         "enoughLapis",
         enoughLapis,
         "enoughLevels",
         enoughLevels,
         "selectable",
         requiredLevel > 0 && inputPresent && enoughLapis && enoughLevels
      );
      if (clueId >= 0) {
         option.addProperty("levelClue", clueLevel);
         if (clueEnchantment != null) {
            option.addProperty("enchantClue", clueEnchantment);
         } else {
            option.addProperty("enchantClueId", clueId);
         }
      }

      return option;
   }

   static String init(int button, int requiredLevel, int lapisAvailable, int xpLevel, boolean inputPresent) {
      if (!inputPresent) {
         return "Enchanting input slot is empty; place the item into the table via game:inventory_click first";
      }

      if (requiredLevel <= 0) {
         return "Option " + button + " is not offered for this item and bookshelf setup (required level 0)";
      }

      int lapisCost = button + 1;
      if (lapisAvailable < lapisCost) {
         return "Lapis deficit: option "
            + button
            + " needs "
            + lapisCost
            + " lapis in the table slot, found "
            + lapisAvailable
            + "; add lapis via game:inventory_click first";
      }

      int levelNeeded = Math.max(button + 1, requiredLevel);
      return xpLevel < levelNeeded ? "XP level deficit: option " + button + " requires experience level " + levelNeeded + ", player level is " + xpLevel : null;
   }

   static JsonObject init_JsonObject(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null && mc.level != null) {
         if (mc.player.containerMenu instanceof EnchantmentMenu menu) {
            return init(mc, menu);
         } else {
            throw new IllegalArgumentException(
               "Open an enchanting table first; the currently open menu is " + mc.player.containerMenu.getClass().getSimpleName()
            );
         }
      } else {
         throw new IllegalArgumentException("Enchanting table observation requires a connected living player");
      }
   }

   private static JsonObject init(Minecraft mc, EnchantmentMenu menu) {
      int lapisInSlot = init(menu.getSlot(1).getItem());
      int xpLevel = mc.player.experienceLevel;
      ItemStack input = menu.getSlot(0).getItem();
      IntFunction<String> resolver = init_IntFunction(mc);
      JsonArray options = new JsonArray();

      for (int i = 0; i < 3; i++) {
         options.add(
            init(i, menu.costs[i], menu.enchantClue[i], menu.levelClue[i], resolver.apply(menu.enchantClue[i]), lapisInSlot, xpLevel, !input.isEmpty())
         );
      }

      JsonObject inputJson = AiJson.objectOf(
         "slot",
         0,
         "item",
         input.isEmpty() ? "minecraft:air" : BuiltInRegistries.ITEM.getKey(input.getItem()).toString(),
         "count",
         input.getCount(),
         "enchantable",
         !input.isEmpty() && input.isEnchantable()
      );
      if (!input.isEmpty()) {
         inputJson.add("components", AiWorldReaders.init(input));
      }

      return AiJson.objectOf(
         "containerId",
         menu.containerId,
         "inputSlot",
         0,
         "lapisSlot",
         1,
         "input",
         inputJson,
         "lapisInTable",
         lapisInSlot,
         "lapisInInventory",
         init_I(mc),
         "experienceLevel",
         xpLevel,
         "options",
         options,
         "clueNote",
         "enchantClue hints at ONE enchantment the server may apply; the full result set is revealed only after enchanting.",
         "placementNote",
         "Place the item and lapis into the table via game:inventory_click before performing game:enchant."
      );
   }

   static AiOperation init_AiOperation(JsonObject args) {
      if (args.has("button") && !args.get("button").isJsonNull()) {
         final int button = AiJson.getInt(args, "button", -1);
         if (button >= 0 && button < 3) {
            int timeout = Math.max(1, Math.min(120, AiJson.getInt(args, "timeoutSeconds", 30)));
            final Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null && mc.level != null && mc.gameMode != null) {
               if (mc.player.containerMenu instanceof EnchantmentMenu menu) {
                  String deficit = init(
                     button, menu.costs[button], init(menu.getSlot(1).getItem()), mc.player.experienceLevel, !menu.getSlot(0).getItem().isEmpty()
                  );
                  if (deficit != null) {
                     return AiGameOperations.immediate(
                        AiOperation.Status.FAILED,
                        deficit,
                        AiJson.objectOf("button", button, "option", init(mc, menu).getAsJsonArray("options").get(button))
                     );
                  }

                  final int containerId = menu.containerId;
                  final ItemStack before = menu.getSlot(0).getItem().copy();
                  final long deadline = System.nanoTime() + timeout * 1000000000L;
                  return new AiOperation() {
                     long init_J;
                     int init_I;
                     boolean init_Z;
                     boolean load;

                     @Override
                     public AiOperation.Result tick() {
                        if (this.load) {
                           return this.init(AiOperation.Status.CANCELLED, "Enchant operation cancelled", new JsonObject());
                        }

                        if (mc.player == null || !(mc.player.containerMenu instanceof EnchantmentMenu current && current.containerId == containerId)) {
                           return this.init(AiOperation.Status.CANCELLED, "Enchanting table closed before the server confirmed the click", new JsonObject());
                        }

                        if (!this.init_Z) {
                           String again = AiEnchant.init(
                              button,
                              current.costs[button],
                              AiEnchant.init(current.getSlot(1).getItem()),
                              mc.player.experienceLevel,
                              !current.getSlot(0).getItem().isEmpty()
                           );
                           if (again != null) {
                              return this.init(AiOperation.Status.FAILED, again, AiJson.objectOf("button", button));
                           }

                           this.init_J = AiServerEvidence.containerRevision(containerId);
                           mc.gameMode.handleInventoryButtonClick(containerId, button);
                           this.init_Z = true;
                           return this.init("Enchant option clicked once; waiting for the server-applied result");
                        } else {
                           ItemStack now = current.getSlot(0).getItem();
                           boolean changed = !ItemStack.matches(before, now);
                           boolean confirmed = AiServerEvidence.containerRevision(containerId) > this.init_J;
                           if (!changed || !confirmed) {
                              return System.nanoTime() > deadline
                                 ? this.init(
                                    AiOperation.Status.UNCERTAIN,
                                    "Enchant click not confirmed; inspect the table before retrying",
                                    AiJson.objectOf("button", button, "containerId", containerId, "changed", changed)
                                 )
                                 : this.init(
                                    changed
                                       ? "Slot changed locally; waiting for the applied server container snapshot"
                                       : "Waiting for the server to apply the enchantment"
                                 );
                           }

                           if (++this.init_I < 3) {
                              return this.init("Enchant result applied; verifying it is stable");
                           }

                           JsonObject data = AiJson.objectOf(
                              "button",
                              button,
                              "containerId",
                              containerId,
                              "lapisSpent",
                              button + 1,
                              "xpLevelsSpent",
                              button + 1,
                              "item",
                              BuiltInRegistries.ITEM.getKey(now.getItem()).toString(),
                              "count",
                              now.getCount(),
                              "components",
                              AiWorldReaders.init(now),
                              "resultNote",
                              "These are the enchantments the server actually applied; use game:inspect_item for the full component dump."
                           );
                           return this.init(AiOperation.Status.SUCCEEDED, "Server applied the enchantment; result reported from the applied slot", data);
                        }
                     }

                     AiOperation.Result init(String message) {
                        return AiGameOperations.result(AiOperation.Status.RUNNING, message, AiJson.objectOf("button", button));
                     }

                     AiOperation.Result init(AiOperation.Status status, String message, JsonObject data) {
                        this.load = true;
                        return AiGameOperations.result(status, message, data);
                     }

                     @Override
                     public void cancel() {
                        this.load = true;
                     }
                  };
               } else {
                  return init("Open an enchanting table first; the currently open menu is " + mc.player.containerMenu.getClass().getSimpleName());
               }
            } else {
               return init("Enchanting requires a connected living player");
            }
         } else {
            throw new IllegalArgumentException("button must be 0..2");
         }
      } else {
         throw new IllegalArgumentException("button (0..2) is required; read game:enchant_table first");
      }
   }

   private static AiOperation init(String message) {
      return AiGameOperations.immediate(AiOperation.Status.FAILED, message, new JsonObject());
   }

   private static IntFunction<String> init_IntFunction(Minecraft mc) {
      return clue -> {
         if (clue >= 0 && mc.level != null) {
            try {
               return mc.level
                  .registryAccess()
                  .lookupOrThrow(Registries.ENCHANTMENT)
                  .get(clue)
                  .map(holder -> holder.unwrapKey().map(key -> key.identifier().toString()).orElse(null))
                  .orElse(null);
            } catch (Throwable t) {
               return null;
            }
         } else {
            return null;
         }
      };
   }

   private static int init(ItemStack stack) {
      return stack.is(Items.LAPIS_LAZULI) ? stack.getCount() : 0;
   }

   private static int init_I(Minecraft mc) {
      int count = 0;
      Inventory inventory = mc.player.getInventory();

      for (int i = 0; i < inventory.getContainerSize(); i++) {
         count += init(inventory.getItem(i));
      }

      return count;
   }
}
