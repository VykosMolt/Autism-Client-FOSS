package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

final class AiContainer {
   private static final int init = 40;
   private static final int load = 1;
   private static final int read = 80;

   private AiContainer() {
   }

   static AiOperation init(final AiOperation route) {
      return new AiOperation() {
         final AiPreparationReceipt init_bb = new AiPreparationReceipt();
         AiOperation.Result init_AiOperation_Result;

         @Override
         public AiOperation.Result tick() {
            if (this.init_AiOperation_Result != null) {
               return this.init_AiOperation_Result;
            }

            AiOperation.Result result = this.init_bb.init_AiOperation_Result(route);
            this.init_bb.init(result.status() == AiOperation.Status.UNCERTAIN);
            if (!result.terminal()) {
               return result;
            }

            this.init_AiOperation_Result = this.init_bb.init(result, this.init_bb.init_Z(route));
            this.init_bb.init_V(route);
            return this.init_AiOperation_Result;
         }

         @Override
         public void cancel() {
            this.init_bb.init_V(route);
            if (this.init_AiOperation_Result == null) {
               this.init_AiOperation_Result = this.init_bb
                  .init(new AiOperation.Result(AiOperation.Status.CANCELLED, "Container navigation cancelled", "{}", List.of()), this.init_bb.init_Z(null));
            }
         }

         @Override
         public boolean canInterruptNow() {
            return !this.init_bb.init_Z(route) && (this.init_AiOperation_Result != null || route.canInterruptNow());
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_bb.init_Z(route);
         }
      };
   }

   static AiOperation.Result init(AiOperation.Result route, JsonObject containerData) {
      JsonObject data = containerData.deepCopy();
      data.add("navigation", AiRunResources.init_JsonObject(route.dataJson()));
      data.addProperty("navigationStatus", route.status().name());
      return new AiOperation.Result(route.status(), "Container navigation stopped: " + route.message(), data.toString(), route.evidence(), route.failure());
   }

   static AiOperation init_AiOperation(JsonObject args) {
      return init(args, Set.of());
   }

   static AiOperation init(JsonObject args, final Set<Long> protectedCells) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final AiContainer.Inner_DataUtil action = init(AiJson.init_Str_JsonObject(args, "action", "inspect"));
         final BlockPos pos = AiGameOperations.init_BlockPos(args);
         final boolean navigate = AiJson.init_Z_JsonObject(args, "navigate", true);
         final int timeoutSeconds = Math.max(5, Math.min(1800, AiJson.init_I_JsonObject(args, "timeoutSeconds", 120)));
         final List<AiContainer.Inner_CachePool> requests = init(args, action);
         final List<AiItems.ItemSpec> keep = init_List(args);
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
            final Object init_Obj = mc.level;
            final Object load_Obj = mc.getConnection();
            final Object read_Obj = mc.player;
            final JsonArray init_JsonArray = new JsonArray();
            final Map<String, Integer> init_Map = new LinkedHashMap<>();
            final int[] init_IArr = new int[requests.size()];
            AiOperation init_AiOperation;
            AiOperation.Result init_AiOperation_Result;
            AiOperation load_AiOperation;
            int init_I;
            int load_I = -1;
            int read_I;
            int write_I;
            int update = -1;
            int sync;
            long load_J;
            String init_Str = "";
            int flush = -1;
            ItemStack init_ItemStack = ItemStack.EMPTY;
            final Set<Integer> init_Set = new HashSet<>();
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            boolean write_Z;

            {
               this.init_DataUtil.init_V_DataUtil_DataUtil(autismclient.ai.engine.AiAim.AimProfile.NORMAL);
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.read_Z && mc.player != null && mc.level != null) {
                  if (mc.level == this.init_Obj && mc.getConnection() == this.load_Obj && mc.player == this.read_Obj && mc.player.isAlive()) {
                     if (System.nanoTime() > this.init_J) {
                        return this.init(this.update >= 0 ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT, "Container deadline reached");
                     }

                     if (!this.init_Z) {
                        AiOperation.Result open = this.init_AiOperation_Result_10();
                        if (open != null) {
                           return open;
                        }
                     }

                     if (mc.player.containerMenu.containerId != this.load_I) {
                        return this.init(
                           this.update >= 0 ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED, "The container closed before the transfers finished"
                        );
                     }

                     if (this.load_AiOperation != null) {
                        AiOperation.Result exact = this.load_AiOperation.tick();
                        if (!exact.terminal()) {
                           return this.init(exact.message());
                        }

                        this.load_AiOperation.cancel();
                        this.load_AiOperation = null;
                        if (exact.status() != AiOperation.Status.SUCCEEDED) {
                           return this.init(exact.status(), "Exact transfer stopped: " + exact.message());
                        }

                        ItemStack now = mc.player.containerMenu.slots.get(this.update).getItem();
                        int delta = this.init_ItemStack.getCount() - (ItemStack.isSameItemSameComponents(now, this.init_ItemStack) ? now.getCount() : 0);
                        if (delta != this.init_I) {
                           return this.init(AiOperation.Status.UNCERTAIN, "Source changed during exact transfer; reconcile before retrying");
                        }

                        this.init_Map.merge(this.init_Str, delta, Integer::sum);
                        if (this.flush >= 0) {
                           this.init_IArr[this.flush] = this.init_IArr[this.flush] + delta;
                        }

                        this.init_JsonArray.add(AiJson.init_JsonObject_ObjArr("slot", this.update, "item", this.init_Str, "count", delta, "confirmed", true));
                        this.update = -1;
                        this.init_ItemStack = ItemStack.EMPTY;
                        this.sync = 0;
                        return this.init("Exact transfer confirmed");
                     } else {
                        if (!mc.player.containerMenu.getCarried().isEmpty()) {
                           return this.init(AiOperation.Status.FAILED, "Cursor is holding a stack; clear it before container transfers");
                        }

                        if (this.update >= 0) {
                           if (AiServerEvidence.init_J_I(this.load_I) > this.load_J) {
                              ItemStack now = mc.player.containerMenu.slots.get(this.update).getItem();
                              int delta = this.init_ItemStack.getCount() - (ItemStack.isSameItemSameComponents(now, this.init_ItemStack) ? now.getCount() : 0);
                              if (delta > 0) {
                                 this.init_Map.merge(this.init_Str, delta, Integer::sum);
                                 if (this.flush >= 0) {
                                    this.init_IArr[this.flush] = this.init_IArr[this.flush] + delta;
                                 }
                              } else {
                                 this.init_Set.add(this.update);
                              }

                              this.init_JsonArray
                                 .add(
                                    AiJson.init_JsonObject_ObjArr("slot", this.update, "item", this.init_Str, "count", Math.max(delta, 0), "confirmed", delta > 0)
                                 );
                              this.update = -1;
                              this.init_ItemStack = ItemStack.EMPTY;
                              this.sync = 0;
                              return this.init(delta > 0 ? "Transfer confirmed" : "Stack would not move; skipping it");
                           } else if (++this.sync > 40) {
                              this.init_JsonArray.add(AiJson.init_JsonObject_ObjArr("slot", this.update, "item", this.init_Str, "count", 0, "confirmed", false));
                              this.update = -1;
                              this.init_ItemStack = ItemStack.EMPTY;
                              this.sync = 0;
                              return this.init(AiOperation.Status.UNCERTAIN, "A transfer was not confirmed by the server; reobserve before repeating");
                           } else {
                              return this.init("Waiting for the server to apply the transfer");
                           }
                        } else {
                           if (action == AiContainer.Inner_DataUtil.INSPECT) {
                              return this.init(AiOperation.Status.SUCCEEDED, "Container contents read");
                           }

                           for (int issued = 0; issued < 1; issued++) {
                              AiContainer.Inner_StreamUtil next = this.init_a_StreamUtil();
                              if (next == null) {
                                 if (this.init_Set.isEmpty()) {
                                    return this.init(AiOperation.Status.SUCCEEDED, this.init_Str_8());
                                 }

                                 if (this.init_Map.isEmpty()) {
                                    return this.init(
                                       AiOperation.Status.FAILED,
                                       "Nothing could move: " + this.init_Set.size() + " matching stack(s) had no destination (full or incompatible)"
                                    );
                                 }

                                 return this.init(
                                    AiOperation.Status.FAILED,
                                    this.init_Str_8() + "; skipped " + this.init_Set.size() + " stuck stack(s) (no destination room)"
                                 );
                              }

                              this.load_J = AiServerEvidence.init_J_I(this.load_I);
                              this.update = next.slot();
                              this.init_Str = next.item();
                              this.flush = next.request();
                              this.init_ItemStack = mc.player.containerMenu.slots.get(next.slot()).getItem().copy();
                              if (next.count() < this.init_ItemStack.getCount()) {
                                 this.init_I = next.count();
                                 int target = AiGameState.init_I_Str(next.item()) + (action == AiContainer.Inner_DataUtil.DEPOSIT ? -this.init_I : this.init_I);
                                 this.load_AiOperation = new AiInventoryController(
                                    AiJson.init_JsonObject_ObjArr(
                                       "item", next.item(), "count", target, "containerId", this.load_I, "timeoutSeconds", timeoutSeconds
                                    ),
                                    Set.of(next.slot())
                                 );
                              } else {
                                 AiContainerTransactions.init(this.load_I, next.slot(), 0, ContainerInput.QUICK_MOVE);
                              }
                           }

                           return this.init("Moving items");
                        }
                     }
                  } else {
                     return this.init(AiOperation.Status.UNCERTAIN, "Container player or world changed; pending transfers require reconciliation");
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Container operation cancelled");
               }
            }

            AiOperation.Result init_AiOperation_Result_10() {
               if (this.init_AiOperation != null) {
                  AiOperation.Result legResult = this.init_AiOperation.tick();
                  this.init_AiOperation_Result = legResult;
                  if (!legResult.terminal()) {
                     return this.init("Travelling to the container");
                  }

                  this.write_Z = this.write_Z | this.init_AiOperation.requiresReconciliationAfterEmergency();
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  if (legResult.status() != AiOperation.Status.SUCCEEDED) {
                     AiOperation.Result result = AiContainer.init(legResult, this.init_JsonObject_24());
                     this.cancel();
                     return result;
                  }
               }

               if (mc.player.containerMenu == mc.player.inventoryMenu || this.load_I >= 0) {
                  BlockHitResult interactionHit = autismclient.ai.engine.AiBlockInteraction.init(mc, pos);
                  if (interactionHit == null) {
                     if (!navigate) {
                        return this.init(AiOperation.Status.FAILED, "Container is out of reach and navigate is disabled");
                     }

                     this.init_AiOperation = AiContainer.init(AiGoto.load(pos, Math.min(120, timeoutSeconds), protectedCells, false));
                     return this.init("Travelling to the container");
                  } else {
                     if (!AiTerrain.init(mc.level, pos)) {
                        return this.init(AiOperation.Status.FAILED, "Container is in unloaded terrain");
                     }

                     this.init_DataUtil.init_V_Vec3(interactionHit.getLocation());
                     this.init_DataUtil.load_V();
                     if (++this.write_I > 80) {
                        return this.init(AiOperation.Status.FAILED, "Could not open the container at " + pos.toShortString());
                     }

                     if (!autismclient.ai.engine.AiAim.init_Z_BlockPos(pos)) {
                        return this.init("Turning toward the container");
                     }

                     BlockHitResult hit = autismclient.ai.engine.AiAim.init_BlockHitResult();
                     if (hit == null) {
                        return this.init("Turning toward the container");
                     }

                     this.load_Z = true;
                     mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                     Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                     return mc.player.containerMenu != mc.player.inventoryMenu ? this.load() : this.init("Opening the container");
                  }
               } else {
                  if (this.load_Z) {
                     return this.load();
                  }

                  mc.player.closeContainer();
                  mc.gui.setScreen(null);
                  return this.init("Closing the earlier container before opening the requested position");
               }
            }

            AiOperation.Result load() {
               AbstractContainerMenu menu = mc.player.containerMenu;
               if (!AiContainer.init_Z(menu)) {
                  return this.init(
                     AiOperation.Status.FAILED, "The open menu is not a storage container (chest, barrel, shulker box, hopper or dispenser); close it first"
                  );
               }

               this.load_I = menu.containerId;
               this.read_I = AiContainer.init_I(menu);
               this.init_Z = true;
               return null;
            }

            AiContainer.Inner_StreamUtil init_a_StreamUtil() {
               AbstractContainerMenu menu = mc.player.containerMenu;
               boolean depositing = action == AiContainer.Inner_DataUtil.DEPOSIT;
               int from = depositing ? this.read_I : 0;
               int to = depositing ? menu.slots.size() : this.read_I;

               for (int index = from; index < to; index++) {
                  ItemStack stack = menu.slots.get(index).getItem();
                  if (!stack.isEmpty() && !this.init_Z(stack) && !this.init_Set.contains(index)) {
                     AiContainer.Inner_CachePool request = this.init_a_CachePool(stack);
                     if (request != null || action != AiContainer.Inner_DataUtil.WITHDRAW && requests.isEmpty()) {
                        int requestIndex = request == null ? -1 : requests.indexOf(request);
                        int count = request == null ? stack.getCount() : Math.min(stack.getCount(), this.init(requestIndex));
                        if (depositing) {
                           count = Math.min(count, Math.max(0, AiGameState.load(AiContainer.init(stack)) - AiRunResources.init(stack.getItem())));
                        }

                        if (count > 0) {
                           if (!depositing && AiItems.init_I() == 0 && !this.init(menu, stack)) {
                              return null;
                           }

                           return new AiContainer.Inner_StreamUtil(index, AiContainer.init(stack), count, requestIndex);
                        }
                     }
                  }
               }

               return null;
            }

            boolean init(AbstractContainerMenu menu, ItemStack stack) {
               for (int index = this.read_I; index < menu.slots.size(); index++) {
                  ItemStack held = menu.slots.get(index).getItem();
                  if (!held.isEmpty() && ItemStack.isSameItemSameComponents(held, stack) && held.getCount() < held.getMaxStackSize()) {
                     return true;
                  }
               }

               return false;
            }

            boolean init_Z(ItemStack stack) {
               for (AiItems.ItemSpec target : keep) {
                  if (target.matches(stack)) {
                     return true;
                  }
               }

               return false;
            }

            AiContainer.Inner_CachePool init_a_CachePool(ItemStack stack) {
               for (AiContainer.Inner_CachePool request : requests) {
                  if (request.target().matches(stack)) {
                     return request;
                  }
               }

               return null;
            }

            int init(int requestIndex) {
               if (requestIndex < 0) {
                  return 2147483647;
               }

               AiContainer.Inner_CachePool request = requests.get(requestIndex);
               return request.count() <= 0 ? 2147483647 : request.count() - this.init_IArr[requestIndex];
            }

            String init_Str_8() {
               if (this.init_Map.isEmpty()) {
                  return "Nothing matched; the container is unchanged";
               }

               StringBuilder text = new StringBuilder(action == AiContainer.Inner_DataUtil.DEPOSIT ? "Deposited " : "Took ");
               boolean first = true;

               for (Entry<String, Integer> entry : this.init_Map.entrySet()) {
                  if (!first) {
                     text.append(", ");
                  }

                  text.append(entry.getValue()).append(" ").append(entry.getKey());
                  first = false;
               }

               return text.toString();
            }

            JsonObject init_JsonObject_24() {
               JsonObject out = AiJson.init_JsonObject_ObjArr(
                  "action",
                  action.name().toLowerCase(Locale.ROOT),
                  "x",
                  pos.getX(),
                  "y",
                  pos.getY(),
                  "z",
                  pos.getZ(),
                  "containerId",
                  this.load_I,
                  "containerSlots",
                  this.read_I
               );
               out.add("transfers", this.init_JsonArray);
               JsonObject totals = new JsonObject();
               this.init_Map.forEach(totals::addProperty);
               out.add("moved", totals);
               if (this.init_AiOperation_Result != null) {
                  out.add("navigation", AiRunResources.init_JsonObject(this.init_AiOperation_Result.dataJson()));
                  out.addProperty("navigationStatus", this.init_AiOperation_Result.status().name());
               }

               if (this.init_Z && mc.player != null && mc.player.containerMenu.containerId == this.load_I) {
                  out.add("contents", AiContainer.init(mc.player.containerMenu, this.read_I));
               }

               return out;
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_24().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               JsonObject data = this.init_JsonObject_24();
               this.write_Z = this.write_Z | status == AiOperation.Status.UNCERTAIN;
               boolean pending = this.requiresReconciliationAfterEmergency();
               this.cancel();
               return AiGuardLayer.init_AiOperation_Result_Z_AiOperation_Result(
                  pending || this.write_Z, new AiOperation.Result(status, message, data.toString(), List.of())
               );
            }

            @Override
            public void cancel() {
               if (!this.read_Z) {
                  this.write_Z = this.write_Z | this.requiresReconciliationAfterEmergency();
                  this.read_Z = true;
                  this.init_DataUtil.init_V();
                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }

                  if (this.load_AiOperation != null) {
                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                  }
               }
            }

            @Override
            public boolean canInterruptNow() {
               return !this.write_Z
                  && this.update < 0
                  && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                  && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow());
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.write_Z
                  || this.update >= 0
                  || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                  || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency();
            }
         };
      } else {
         throw new IllegalArgumentException("Container operations require a connected living player");
      }
   }

   static JsonArray init(AbstractContainerMenu menu, int containerSlots) {
      JsonArray out = new JsonArray();

      for (int index = 0; index < Math.min(containerSlots, menu.slots.size()); index++) {
         ItemStack stack = menu.slots.get(index).getItem();
         if (!stack.isEmpty()) {
            out.add(AiJson.init_JsonObject_ObjArr("slot", index, "item", init(stack), "count", stack.getCount(), "name", stack.getHoverName().getString()));
         }
      }

      return out;
   }

   static boolean init_Z(AbstractContainerMenu menu) {
      return menu instanceof ChestMenu
         || menu instanceof ShulkerBoxMenu
         || menu instanceof HopperMenu
         || menu instanceof DispenserMenu
         || menu instanceof CrafterMenu;
   }

   static int init_I(AbstractContainerMenu menu) {
      return menu instanceof CrafterMenu ? 9 : Math.max(0, menu.slots.size() - 36);
   }

   private static String init(ItemStack stack) {
      return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
   }

   private static AiContainer.Inner_DataUtil init(String text) {
      try {
         return AiContainer.Inner_DataUtil.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be inspect, loot, withdraw or deposit");
      }
   }

   private static List<AiContainer.Inner_CachePool> init(JsonObject args, AiContainer.Inner_DataUtil action) {
      List<AiContainer.Inner_CachePool> requests = new ArrayList<>();
      if (args.has("items") && args.get("items").isJsonArray()) {
         for (JsonElement element : args.getAsJsonArray("items")) {
            if (element.isJsonPrimitive()) {
               requests.add(new AiContainer.Inner_CachePool(AiItems.init_af_DataUtil(element.getAsString()), 0));
            } else {
               if (!element.isJsonObject()) {
                  throw new IllegalArgumentException("items entries must be a spec string or {item,count}");
               }

               JsonObject entry = element.getAsJsonObject();
               String spec = AiJson.init_Str_JsonObject(entry, "item", "");
               requests.add(new AiContainer.Inner_CachePool(AiItems.init_af_DataUtil(spec), Math.max(0, AiJson.init_I_JsonObject(entry, "count", 0))));
            }
         }
      }

      if (requests.isEmpty() && action == AiContainer.Inner_DataUtil.WITHDRAW) {
         throw new IllegalArgumentException("withdraw needs an items list; use action loot to take everything");
      } else {
         return List.copyOf(requests);
      }
   }

   private static List<AiItems.ItemSpec> init_List(JsonObject args) {
      List<AiItems.ItemSpec> keep = new ArrayList<>();
      if (args.has("keep") && args.get("keep").isJsonArray()) {
         for (JsonElement element : args.getAsJsonArray("keep")) {
            keep.add(AiItems.init_af_DataUtil(element.getAsString()));
         }
      }

      return List.copyOf(keep);
   }

   record Inner_CachePool(AiItems.ItemSpec target, int count) {
   }

   enum Inner_DataUtil {
      INSPECT,
      LOOT,
      WITHDRAW,
      DEPOSIT;
   }

   record Inner_StreamUtil(int slot, String item, int count, int request) {
   }
}
