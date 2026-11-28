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
         final AiPreparationReceipt receipt = new AiPreparationReceipt();
         AiOperation.Result finalResult;

         @Override
         public AiOperation.Result tick() {
            if (this.finalResult != null) {
               return this.finalResult;
            }

            AiOperation.Result result = this.receipt.tickChild(route);
            this.receipt.markReconciliationPending(result.status() == AiOperation.Status.UNCERTAIN);
            if (!result.terminal()) {
               return result;
            }

            this.finalResult = this.receipt.finalizeResult(result, this.receipt.requiresReconciliation(route));
            this.receipt.cancelChild(route);
            return this.finalResult;
         }

         @Override
         public void cancel() {
            this.receipt.cancelChild(route);
            if (this.finalResult == null) {
               this.finalResult = this.receipt
                  .finalizeResult(new AiOperation.Result(AiOperation.Status.CANCELLED, "Container navigation cancelled", "{}", List.of()), this.receipt.requiresReconciliation(null));
            }
         }

         @Override
         public boolean canInterruptNow() {
            return !this.receipt.requiresReconciliation(route) && (this.finalResult != null || route.canInterruptNow());
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.receipt.requiresReconciliation(route);
         }
      };
   }

   static AiOperation.Result mergeNavigation(AiOperation.Result route, JsonObject containerData) {
      JsonObject data = containerData.deepCopy();
      data.add("navigation", AiRunResources.parseEvidenceJson(route.dataJson()));
      data.addProperty("navigationStatus", route.status().name());
      return new AiOperation.Result(route.status(), "Container navigation stopped: " + route.message(), data.toString(), route.evidence(), route.failure());
   }

   static AiOperation create(JsonObject args) {
      return create(args, Set.of());
   }

   static AiOperation create(JsonObject args, final Set<Long> protectedCells) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final AiContainer.Inner_DataUtil action = init(AiJson.getString(args, "action", "inspect"));
         final BlockPos pos = AiGameOperations.requireBlockPos(args);
         final boolean navigate = AiJson.getBoolean(args, "navigate", true);
         final int timeoutSeconds = Math.max(5, Math.min(1800, AiJson.getInt(args, "timeoutSeconds", 120)));
         final List<AiContainer.Inner_CachePool> requests = init(args, action);
         final List<AiItems.ItemSpec> keep = parseKeep(args);
         return new AiOperation() {
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final Object startLevel = mc.level;
            final Object startConnection = mc.getConnection();
            final Object startPlayer = mc.player;
            final JsonArray transfers = new JsonArray();
            final Map<String, Integer> movedByItem = new LinkedHashMap<>();
            final int[] movedByRequest = new int[requests.size()];
            AiOperation approach;
            AiOperation.Result approachResult;
            AiOperation exactTransfer;
            int exactCount;
            int containerId = -1;
            int containerSlots;
            int openAttempts;
            int update = -1;
            int sync;
            long revisionBefore;
            String pendingItem = "";
            int flush = -1;
            ItemStack sourceBefore = ItemStack.EMPTY;
            final Set<Integer> stuckSlots = new HashSet<>();
            boolean opened;
            boolean openClicked;
            boolean cancelled;
            boolean needsReconciliation;

            {
               this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.NORMAL);
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && mc.player != null && mc.level != null) {
                  if (mc.level == this.startLevel && mc.getConnection() == this.startConnection && mc.player == this.startPlayer && mc.player.isAlive()) {
                     if (System.nanoTime() > this.deadline) {
                        return this.init(this.update >= 0 ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT, "Container deadline reached");
                     }

                     if (!this.opened) {
                        AiOperation.Result open = this.openStep();
                        if (open != null) {
                           return open;
                        }
                     }

                     if (mc.player.containerMenu.containerId != this.containerId) {
                        return this.init(
                           this.update >= 0 ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED, "The container closed before the transfers finished"
                        );
                     }

                     if (this.exactTransfer != null) {
                        AiOperation.Result exact = this.exactTransfer.tick();
                        if (!exact.terminal()) {
                           return this.init(exact.message());
                        }

                        this.exactTransfer.cancel();
                        this.exactTransfer = null;
                        if (exact.status() != AiOperation.Status.SUCCEEDED) {
                           return this.init(exact.status(), "Exact transfer stopped: " + exact.message());
                        }

                        ItemStack now = mc.player.containerMenu.slots.get(this.update).getItem();
                        int delta = this.sourceBefore.getCount() - (ItemStack.isSameItemSameComponents(now, this.sourceBefore) ? now.getCount() : 0);
                        if (delta != this.exactCount) {
                           return this.init(AiOperation.Status.UNCERTAIN, "Source changed during exact transfer; reconcile before retrying");
                        }

                        this.movedByItem.merge(this.pendingItem, delta, Integer::sum);
                        if (this.flush >= 0) {
                           this.movedByRequest[this.flush] = this.movedByRequest[this.flush] + delta;
                        }

                        this.transfers.add(AiJson.objectOf("slot", this.update, "item", this.pendingItem, "count", delta, "confirmed", true));
                        this.update = -1;
                        this.sourceBefore = ItemStack.EMPTY;
                        this.sync = 0;
                        return this.init("Exact transfer confirmed");
                     } else {
                        if (!mc.player.containerMenu.getCarried().isEmpty()) {
                           return this.init(AiOperation.Status.FAILED, "Cursor is holding a stack; clear it before container transfers");
                        }

                        if (this.update >= 0) {
                           if (AiServerEvidence.containerRevision(this.containerId) > this.revisionBefore) {
                              ItemStack now = mc.player.containerMenu.slots.get(this.update).getItem();
                              int delta = this.sourceBefore.getCount() - (ItemStack.isSameItemSameComponents(now, this.sourceBefore) ? now.getCount() : 0);
                              if (delta > 0) {
                                 this.movedByItem.merge(this.pendingItem, delta, Integer::sum);
                                 if (this.flush >= 0) {
                                    this.movedByRequest[this.flush] = this.movedByRequest[this.flush] + delta;
                                 }
                              } else {
                                 this.stuckSlots.add(this.update);
                              }

                              this.transfers
                                 .add(
                                    AiJson.objectOf("slot", this.update, "item", this.pendingItem, "count", Math.max(delta, 0), "confirmed", delta > 0)
                                 );
                              this.update = -1;
                              this.sourceBefore = ItemStack.EMPTY;
                              this.sync = 0;
                              return this.init(delta > 0 ? "Transfer confirmed" : "Stack would not move; skipping it");
                           } else if (++this.sync > 40) {
                              this.transfers.add(AiJson.objectOf("slot", this.update, "item", this.pendingItem, "count", 0, "confirmed", false));
                              this.update = -1;
                              this.sourceBefore = ItemStack.EMPTY;
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
                              AiContainer.Inner_StreamUtil next = this.nextTransfer();
                              if (next == null) {
                                 if (this.stuckSlots.isEmpty()) {
                                    return this.init(AiOperation.Status.SUCCEEDED, this.summary());
                                 }

                                 if (this.movedByItem.isEmpty()) {
                                    return this.init(
                                       AiOperation.Status.FAILED,
                                       "Nothing could move: " + this.stuckSlots.size() + " matching stack(s) had no destination (full or incompatible)"
                                    );
                                 }

                                 return this.init(
                                    AiOperation.Status.FAILED,
                                    this.summary() + "; skipped " + this.stuckSlots.size() + " stuck stack(s) (no destination room)"
                                 );
                              }

                              this.revisionBefore = AiServerEvidence.containerRevision(this.containerId);
                              this.update = next.slot();
                              this.pendingItem = next.item();
                              this.flush = next.request();
                              this.sourceBefore = mc.player.containerMenu.slots.get(next.slot()).getItem().copy();
                              if (next.count() < this.sourceBefore.getCount()) {
                                 this.exactCount = next.count();
                                 int target = AiGameState.countInventoryItem(next.item()) + (action == AiContainer.Inner_DataUtil.DEPOSIT ? -this.exactCount : this.exactCount);
                                 this.exactTransfer = new AiInventoryController(
                                    AiJson.objectOf(
                                       "item", next.item(), "count", target, "containerId", this.containerId, "timeoutSeconds", timeoutSeconds
                                    ),
                                    Set.of(next.slot())
                                 );
                              } else {
                                 AiContainerTransactions.sendPredictedClick(this.containerId, next.slot(), 0, ContainerInput.QUICK_MOVE);
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

            AiOperation.Result openStep() {
               if (this.approach != null) {
                  AiOperation.Result legResult = this.approach.tick();
                  this.approachResult = legResult;
                  if (!legResult.terminal()) {
                     return this.init("Travelling to the container");
                  }

                  this.needsReconciliation = this.needsReconciliation | this.approach.requiresReconciliationAfterEmergency();
                  this.approach.cancel();
                  this.approach = null;
                  if (legResult.status() != AiOperation.Status.SUCCEEDED) {
                     AiOperation.Result result = AiContainer.mergeNavigation(legResult, this.reportJson());
                     this.cancel();
                     return result;
                  }
               }

               if (mc.player.containerMenu == mc.player.inventoryMenu || this.containerId >= 0) {
                  BlockHitResult interactionHit = autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, pos);
                  if (interactionHit == null) {
                     if (!navigate) {
                        return this.init(AiOperation.Status.FAILED, "Container is out of reach and navigate is disabled");
                     }

                     this.approach = AiContainer.init(AiGoto.approachBlockToInteractCompat(pos, Math.min(120, timeoutSeconds), protectedCells, false));
                     return this.init("Travelling to the container");
                  } else {
                     if (!AiTerrain.isLoaded(mc.level, pos)) {
                        return this.init(AiOperation.Status.FAILED, "Container is in unloaded terrain");
                     }

                     this.aim.aimAt(interactionHit.getLocation());
                     this.aim.tick();
                     if (++this.openAttempts > 80) {
                        return this.init(AiOperation.Status.FAILED, "Could not open the container at " + pos.toShortString());
                     }

                     if (!autismclient.ai.engine.AiAim.isLookingAtBlock(pos)) {
                        return this.init("Turning toward the container");
                     }

                     BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                     if (hit == null) {
                        return this.init("Turning toward the container");
                     }

                     this.openClicked = true;
                     mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                     Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                     return mc.player.containerMenu != mc.player.inventoryMenu ? this.load() : this.init("Opening the container");
                  }
               } else {
                  if (this.openClicked) {
                     return this.load();
                  }

                  mc.player.closeContainer();
                  mc.gui.setScreen(null);
                  return this.init("Closing the earlier container before opening the requested position");
               }
            }

            AiOperation.Result load() {
               AbstractContainerMenu menu = mc.player.containerMenu;
               if (!AiContainer.isStorageMenu(menu)) {
                  return this.init(
                     AiOperation.Status.FAILED, "The open menu is not a storage container (chest, barrel, shulker box, hopper or dispenser); close it first"
                  );
               }

               this.containerId = menu.containerId;
               this.containerSlots = AiContainer.containerSlotCount(menu);
               this.opened = true;
               return null;
            }

            AiContainer.Inner_StreamUtil nextTransfer() {
               AbstractContainerMenu menu = mc.player.containerMenu;
               boolean depositing = action == AiContainer.Inner_DataUtil.DEPOSIT;
               int from = depositing ? this.containerSlots : 0;
               int to = depositing ? menu.slots.size() : this.containerSlots;

               for (int index = from; index < to; index++) {
                  ItemStack stack = menu.slots.get(index).getItem();
                  if (!stack.isEmpty() && !this.isKept(stack) && !this.stuckSlots.contains(index)) {
                     AiContainer.Inner_CachePool request = this.requestFor(stack);
                     if (request != null || action != AiContainer.Inner_DataUtil.WITHDRAW && requests.isEmpty()) {
                        int requestIndex = request == null ? -1 : requests.indexOf(request);
                        int count = request == null ? stack.getCount() : Math.min(stack.getCount(), this.init(requestIndex));
                        if (depositing) {
                           count = Math.min(count, Math.max(0, AiGameState.countMainInventoryItem(AiContainer.init(stack)) - AiRunResources.totalReservedCount(stack.getItem())));
                        }

                        if (count > 0) {
                           if (!depositing && AiItems.countFreeSlots() == 0 && !this.init(menu, stack)) {
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
               for (int index = this.containerSlots; index < menu.slots.size(); index++) {
                  ItemStack held = menu.slots.get(index).getItem();
                  if (!held.isEmpty() && ItemStack.isSameItemSameComponents(held, stack) && held.getCount() < held.getMaxStackSize()) {
                     return true;
                  }
               }

               return false;
            }

            boolean isKept(ItemStack stack) {
               for (AiItems.ItemSpec target : keep) {
                  if (target.matches(stack)) {
                     return true;
                  }
               }

               return false;
            }

            AiContainer.Inner_CachePool requestFor(ItemStack stack) {
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
               return request.count() <= 0 ? 2147483647 : request.count() - this.movedByRequest[requestIndex];
            }

            String summary() {
               if (this.movedByItem.isEmpty()) {
                  return "Nothing matched; the container is unchanged";
               }

               StringBuilder text = new StringBuilder(action == AiContainer.Inner_DataUtil.DEPOSIT ? "Deposited " : "Took ");
               boolean first = true;

               for (Entry<String, Integer> entry : this.movedByItem.entrySet()) {
                  if (!first) {
                     text.append(", ");
                  }

                  text.append(entry.getValue()).append(" ").append(entry.getKey());
                  first = false;
               }

               return text.toString();
            }

            JsonObject reportJson() {
               JsonObject out = AiJson.objectOf(
                  "action",
                  action.name().toLowerCase(Locale.ROOT),
                  "x",
                  pos.getX(),
                  "y",
                  pos.getY(),
                  "z",
                  pos.getZ(),
                  "containerId",
                  this.containerId,
                  "containerSlots",
                  this.containerSlots
               );
               out.add("transfers", this.transfers);
               JsonObject totals = new JsonObject();
               this.movedByItem.forEach(totals::addProperty);
               out.add("moved", totals);
               if (this.approachResult != null) {
                  out.add("navigation", AiRunResources.parseEvidenceJson(this.approachResult.dataJson()));
                  out.addProperty("navigationStatus", this.approachResult.status().name());
               }

               if (this.opened && mc.player != null && mc.player.containerMenu.containerId == this.containerId) {
                  out.add("contents", AiContainer.init(mc.player.containerMenu, this.containerSlots));
               }

               return out;
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.reportJson().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               JsonObject data = this.reportJson();
               this.needsReconciliation = this.needsReconciliation | status == AiOperation.Status.UNCERTAIN;
               boolean pending = this.requiresReconciliationAfterEmergency();
               this.cancel();
               return AiGuardLayer.wrapPendingTermination(
                  pending || this.needsReconciliation, new AiOperation.Result(status, message, data.toString(), List.of())
               );
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.needsReconciliation = this.needsReconciliation | this.requiresReconciliationAfterEmergency();
                  this.cancelled = true;
                  this.aim.clear();
                  if (this.approach != null) {
                     this.approach.cancel();
                     this.approach = null;
                  }

                  if (this.exactTransfer != null) {
                     this.exactTransfer.cancel();
                     this.exactTransfer = null;
                  }
               }
            }

            @Override
            public boolean canInterruptNow() {
               return !this.needsReconciliation
                  && this.update < 0
                  && (this.approach == null || this.approach.canInterruptNow())
                  && (this.exactTransfer == null || this.exactTransfer.canInterruptNow());
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.needsReconciliation
                  || this.update >= 0
                  || this.approach != null && this.approach.requiresReconciliationAfterEmergency()
                  || this.exactTransfer != null && this.exactTransfer.requiresReconciliationAfterEmergency();
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
            out.add(AiJson.objectOf("slot", index, "item", init(stack), "count", stack.getCount(), "name", stack.getHoverName().getString()));
         }
      }

      return out;
   }

   static boolean isStorageMenu(AbstractContainerMenu menu) {
      return menu instanceof ChestMenu
         || menu instanceof ShulkerBoxMenu
         || menu instanceof HopperMenu
         || menu instanceof DispenserMenu
         || menu instanceof CrafterMenu;
   }

   static int containerSlotCount(AbstractContainerMenu menu) {
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
               requests.add(new AiContainer.Inner_CachePool(AiItems.resolve(element.getAsString()), 0));
            } else {
               if (!element.isJsonObject()) {
                  throw new IllegalArgumentException("items entries must be a spec string or {item,count}");
               }

               JsonObject entry = element.getAsJsonObject();
               String spec = AiJson.getString(entry, "item", "");
               requests.add(new AiContainer.Inner_CachePool(AiItems.resolve(spec), Math.max(0, AiJson.getInt(entry, "count", 0))));
            }
         }
      }

      if (requests.isEmpty() && action == AiContainer.Inner_DataUtil.WITHDRAW) {
         throw new IllegalArgumentException("withdraw needs an items list; use action loot to take everything");
      } else {
         return List.copyOf(requests);
      }
   }

   private static List<AiItems.ItemSpec> parseKeep(JsonObject args) {
      List<AiItems.ItemSpec> keep = new ArrayList<>();
      if (args.has("keep") && args.get("keep").isJsonArray()) {
         for (JsonElement element : args.getAsJsonArray("keep")) {
            keep.add(AiItems.resolve(element.getAsString()));
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
