package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AiInventoryController implements AiOperation {
   private final Minecraft mc = Minecraft.getInstance();
   private final String itemId;
   private final int targetCount;
   private final int containerId;
   private final long deadlineNanos;
   private int sourceSlot = -1;
   private int destinationSlot = -1;
   private int transferAmount;
   private int sync;
   private ItemStack expectedCursor = ItemStack.EMPTY;
   private ItemStack expectedSource = ItemStack.EMPTY;
   private ItemStack expectedDestination = ItemStack.EMPTY;
   private long inventoryRevision;
   private AiContainerTransactions.TransactionReceipt pendingPickup;
   private boolean cancelled;
   private boolean clicked;
   private final Set<Integer> allowedSources;
   private final Set<Integer> allowedDestinations;
   private final ItemStack savedStack;
   private final int flush;
   private final ItemStack exactInventoryStack;
   private final Object startLevel = this.mc == null ? null : this.mc.level;
   private final Object startPlayer = this.mc == null ? null : this.mc.player;
   private final Object startMenu = this.mc != null && this.mc.player != null ? this.mc.player.containerMenu : null;

   AiInventoryController(JsonObject args) {
      this(args, null);
   }

   AiInventoryController(JsonObject args, Set<Integer> allowedSources) {
      this(args, allowedSources, null);
   }

   AiInventoryController(JsonObject args, Set<Integer> allowedSources, Set<Integer> allowedDestinations) {
      this(args, allowedSources, allowedDestinations, -1, ItemStack.EMPTY);
   }

   AiInventoryController(int containerId, int destination, ItemStack wanted) {
      this(
         AiJson.objectOf("item", BuiltInRegistries.ITEM.getKey(wanted.getItem()).toString(), "count", 0, "containerId", containerId),
         null,
         Set.of(destination),
         destination,
         wanted
      );
   }

   AiInventoryController(int containerId, ItemStack exact, int count, Set<Integer> allowedSources, Set<Integer> allowedDestinations) {
      this(
         AiJson.objectOf("item", BuiltInRegistries.ITEM.getKey(exact.getItem()).toString(), "count", count, "containerId", containerId),
         allowedSources,
         allowedDestinations,
         -1,
         ItemStack.EMPTY,
         exact
      );
      if (exact.isEmpty()) {
         throw new IllegalArgumentException("An exact withdrawal requires a nonempty item");
      }
   }

   private AiInventoryController(JsonObject args, Set<Integer> allowedSources, Set<Integer> allowedDestinations, int savedDestination, ItemStack savedStack) {
      this(args, allowedSources, allowedDestinations, savedDestination, savedStack, ItemStack.EMPTY);
   }

   private AiInventoryController(
      JsonObject args, Set<Integer> allowedSources, Set<Integer> allowedDestinations, int savedDestination, ItemStack savedStack, ItemStack exactInventoryStack
   ) {
      this.exactInventoryStack = exactInventoryStack.copy();
      this.allowedSources = allowedSources == null ? null : Set.copyOf(allowedSources);
      this.allowedDestinations = allowedDestinations == null ? null : Set.copyOf(allowedDestinations);
      this.savedStack = savedStack.copy();
      this.flush = savedDestination;
      if (savedDestination >= 0 && savedStack.isEmpty()) {
         throw new IllegalArgumentException("A saved storage slot requires a nonempty stack");
      }

      this.itemId = AiJson.getString(args, "item", "");
      this.targetCount = AiJson.getInt(args, "count", -1);
      this.containerId = AiJson.getInt(args, "containerId", -1);
      if (this.itemId.isBlank() || this.targetCount < 0 || this.targetCount > 2304 && allowedSources == null) {
         throw new IllegalArgumentException("Expected item registry id and inventory count 0..2304");
      }

      this.deadlineNanos = System.nanoTime() + Math.max(1, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 180))) * 1000000000L;
   }

   @Override
   public AiOperation.Result tick() {
      if (this.cancelled || this.mc == null || this.mc.player == null) {
         return this.finish(
            this.clicked ? AiOperation.Status.UNCERTAIN : AiOperation.Status.CANCELLED, "Reconciliation cancelled; inspect cursor and storage before continuing"
         );
      }

      if (this.mc.level == this.startLevel && this.mc.player == this.startPlayer) {
         AbstractContainerMenu menu = this.mc.player.containerMenu;
         if (menu.containerId != this.containerId || menu != this.startMenu) {
            return this.finish(
               this.clicked ? AiOperation.Status.UNCERTAIN : AiOperation.Status.CANCELLED, "Storage container changed; inspect cursor before continuing"
            );
         }

         if (!AiContainer.isStorageMenu(menu)) {
            return this.finish(
               AiOperation.Status.FAILED, "Open a storage chest, barrel, shulker box, hopper or dispenser first"
            );
         }

         if (System.nanoTime() > this.deadlineNanos) {
            return this.finish(
               this.clicked ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT, "Reconciliation deadline; no uncertain click is repeated"
            );
         }

         if (this.pendingPickup != null) {
            AiContainerTransactions.ReceiptState state = this.pendingPickup.checkStatus(menu, System.nanoTime());
            if (state == AiContainerTransactions.ReceiptState.WAITING) {
               return this.init("Waiting for the server's storage slots and cursor");
            }

            if (state != AiContainerTransactions.ReceiptState.CONFIRMED) {
               return this.finish(
                  AiOperation.Status.UNCERTAIN, "The server did not confirm the storage transfer. Inspect the open storage and cursor before resuming."
               );
            }

            this.pendingPickup = null;
         }

         if (this.sync == 0) {
            if (!menu.getCarried().isEmpty()) {
               return this.finish(AiOperation.Status.FAILED, "Clear the cursor stack before reconciliation");
            }

            if (this.flush >= 0) {
               AiInventoryController.Inner_StreamUtil transfer = planTransferTo(
                  menu, this.mc.player.getInventory(), this.mc.player, this.flush, this.savedStack
               );
               if (transfer.state() == AiInventoryController.Inner_DataUtil.SATISFIED) {
                  return this.finish(AiOperation.Status.SUCCEEDED, "Saved storage slot and empty cursor verified");
               }

               if (transfer.state() == AiInventoryController.Inner_DataUtil.CONFLICT) {
                  return this.finish(
                     AiOperation.Status.FAILED, "Saved storage slot has incompatible contents or capacity; existing items retained"
                  );
               }

               if (transfer.state() == AiInventoryController.Inner_DataUtil.MISSING_STOCK) {
                  return this.finish(
                     AiOperation.Status.FAILED, "Player inventory lacks the saved item's exact components"
                  );
               }

               this.sourceSlot = transfer.source();
               this.destinationSlot = this.flush;
               this.transferAmount = transfer.amount();
               return this.init_AiOperation_Result_AbstractContainerM_Str(menu, "Picking up the saved storage item");
            } else {
               int count = this.inventoryCount();
               int delta = count - this.targetCount;
               if (delta == 0) {
                  return this.finish(
                     AiOperation.Status.SUCCEEDED, "Exact player inventory target verified; surplus retained in storage"
                  );
               }

               boolean deposit = delta > 0;
               this.sourceSlot = -1;
               this.destinationSlot = -1;

               for (int pass = 0; pass < 2 && this.sourceSlot < 0; pass++) {
                  for (Slot slot : menu.slots) {
                     if ((this.allowedSources == null || this.allowedSources.contains(slot.index))
                        && slot.container == this.mc.player.getInventory() == deposit
                        && this.init(slot.getItem())
                        && slot.mayPickup(this.mc.player)
                        && (pass != 0 || AiItems.isPlainItem(slot.getItem()))) {
                        this.sourceSlot = slot.index;
                        break;
                     }
                  }
               }

               if (this.sourceSlot < 0) {
                  return this.finish(
                     AiOperation.Status.FAILED, deposit ? "No player stack available to deposit" : "Storage lacks the requested item quantity"
                  );
               }

               ItemStack stack = menu.getSlot(this.sourceSlot).getItem();
               this.destinationSlot = findDestinationSlot(menu, this.mc.player.getInventory(), stack, deposit, this.allowedDestinations);
               if (this.destinationSlot < 0) {
                  return this.finish(
                     AiOperation.Status.FAILED, deposit ? "Storage is full; surplus retained" : "Player inventory is full"
                  );
               }

               this.transferAmount = Math.min(
                  Math.abs(delta),
                  Math.min(stack.getCount(), menu.getSlot(this.destinationSlot).getMaxStackSize(stack) - menu.getSlot(this.destinationSlot).getItem().getCount())
               );
               return this.init_AiOperation_Result_AbstractContainerM_Str(menu, "Picking up a stack for exact transfer");
            }
         } else {
            if (this.sync != 1) {
               this.sync = 0;
               return this.init("Checking reconciled inventory");
            }

            if (this.transferAmount > 0) {
               AiInventoryController.Inner_CachePool choice = init(menu, this.destinationSlot, this.transferAmount);
               if (choice == null) {
                  return this.finish(
                     AiOperation.Status.UNCERTAIN, "Storage or cursor changed before the next transfer; inspect before continuing"
                  );
               }

               ItemStack cursor = menu.getCarried();
               ItemStack existing = menu.getSlot(this.destinationSlot).getItem();
               if (!this.sendVerifiedClick(
                  this.destinationSlot,
                  choice.button(),
                  menu.getSlot(this.sourceSlot).getItem(),
                  cursor.copyWithCount(existing.getCount() + choice.amount()),
                  cursor.copyWithCount(cursor.getCount() - choice.amount())
               )) {
                  return this.finish(
                     AiOperation.Status.UNCERTAIN, "Could not start the next storage click. Inspect the cursor before resuming."
                  );
               }

               this.transferAmount = this.transferAmount - choice.amount();
               return this.init("Transferring the exact quantity into storage or inventory");
            } else if (menu.getCarried().isEmpty()) {
               this.sync = 0;
               return this.init("Checking reconciled inventory");
            } else {
               ItemStack cursor = menu.getCarried();
               Slot slot = menu.getSlot(this.sourceSlot);
               ItemStack existing = slot.getItem();
               int count = existing.getCount() + cursor.getCount();
               if (slot.mayPlace(cursor)
                  && count <= slot.getMaxStackSize(cursor)
                  && (existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, cursor))
                  && this.sendVerifiedClick(this.sourceSlot, 0, cursor.copyWithCount(count), menu.getSlot(this.destinationSlot).getItem(), ItemStack.EMPTY)) {
                  this.sync = 2;
                  return this.init("Returning the unused stack without disposal");
               } else {
                  return this.finish(
                     AiOperation.Status.UNCERTAIN, "The source slot cannot safely take back the cursor stack. Inspect the open storage."
                  );
               }
            }
         }
      } else {
         return this.finish(
            this.clicked ? AiOperation.Status.UNCERTAIN : AiOperation.Status.CANCELLED, "World or player changed; storage actions are not replayed"
         );
      }
   }

   static AiInventoryController.Inner_CachePool init(AbstractContainerMenu menu, int destination, int remaining) {
      if (remaining > 0 && destination >= 0 && destination < menu.slots.size()) {
         ItemStack cursor = menu.getCarried();
         Slot slot = menu.getSlot(destination);
         ItemStack current = slot.getItem();
         if (!cursor.isEmpty() && slot.mayPlace(cursor) && (current.isEmpty() || ItemStack.isSameItemSameComponents(current, cursor))) {
            int capacity = slot.getMaxStackSize(cursor) - current.getCount();
            int merged = Math.min(cursor.getCount(), capacity);
            if (merged <= 0) {
               return null;
            } else {
               return merged == remaining ? new AiInventoryController.Inner_CachePool(0, merged) : new AiInventoryController.Inner_CachePool(1, 1);
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static AiInventoryController.Inner_StreamUtil planTransferTo(
      AbstractContainerMenu menu, Inventory inventory, Player player, int destination, ItemStack wanted
   ) {
      if (!wanted.isEmpty() && destination >= 0 && destination < menu.slots.size() && menu.getCarried().isEmpty()) {
         Slot target = menu.getSlot(destination);
         ItemStack current = target.getItem();
         if (target.container != inventory
            && target.mayPlace(wanted)
            && wanted.getCount() <= target.getMaxStackSize(wanted)
            && (current.isEmpty() || ItemStack.isSameItemSameComponents(current, wanted))
            && current.getCount() <= wanted.getCount()) {
            if (current.getCount() == wanted.getCount()) {
               return new AiInventoryController.Inner_StreamUtil(AiInventoryController.Inner_DataUtil.SATISFIED, -1, 0);
            }

            for (Slot candidate : menu.slots) {
               ItemStack stack = candidate.getItem();
               if (candidate.container == inventory && !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, wanted) && candidate.mayPickup(player)) {
                  return new AiInventoryController.Inner_StreamUtil(AiInventoryController.Inner_DataUtil.READY, candidate.index, Math.min(wanted.getCount() - current.getCount(), stack.getCount()));
               }
            }

            return new AiInventoryController.Inner_StreamUtil(AiInventoryController.Inner_DataUtil.MISSING_STOCK, -1, 0);
         } else {
            return new AiInventoryController.Inner_StreamUtil(AiInventoryController.Inner_DataUtil.CONFLICT, -1, 0);
         }
      } else {
         return new AiInventoryController.Inner_StreamUtil(AiInventoryController.Inner_DataUtil.CONFLICT, -1, 0);
      }
   }

   static int findDestinationSlot(AbstractContainerMenu menu, Inventory inventory, ItemStack stack, boolean deposit, Set<Integer> allowed) {
      for (int pass = 0; pass < 2; pass++) {
         for (Slot slot : menu.slots) {
            if (allowed == null || allowed.contains(slot.index)) {
               ItemStack current = slot.getItem();
               if (slot.container == inventory != deposit
                  && slot.mayPlace(stack)
                  && (pass == 0 ? !current.isEmpty() && ItemStack.isSameItemSameComponents(stack, current) : current.isEmpty())
                  && current.getCount() < slot.getMaxStackSize(stack)) {
                  return slot.index;
               }
            }
         }
      }

      return -1;
   }

   private AiOperation.Result init_AiOperation_Result_AbstractContainerM_Str(AbstractContainerMenu menu, String message) {
      if (!this.sendVerifiedClick(this.sourceSlot, 0, ItemStack.EMPTY, menu.getSlot(this.destinationSlot).getItem(), menu.getSlot(this.sourceSlot).getItem())) {
         return this.finish(
            this.clicked ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED, "Could not start the storage transfer. Inspect the inventory and cursor."
         );
      }

      this.sync = 1;
      return this.init(message);
   }

   private boolean sendVerifiedClick(int slot, int button, ItemStack from, ItemStack to, ItemStack cursor) {
      this.inventoryRevision = AiServerEvidence.inventoryRevision();
      AbstractContainerMenu menu = this.mc.player.containerMenu;
      this.pendingPickup = AiContainerTransactions.sendVerifiedPickup(menu, slot, button, Map.of(this.sourceSlot, from, this.destinationSlot, to), cursor);
      if (this.pendingPickup == null) {
         return false;
      }

      this.clicked = true;
      this.expectedCursor = cursor.copy();
      this.expectedSource = from.copy();
      this.expectedDestination = to.copy();
      return true;
   }

   static int countMatching(Container inventory, ItemStack wanted) {
      int count = 0;

      for (int slot = 0; slot < Math.min(36, inventory.getContainerSize()); slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (ItemStack.isSameItemSameComponents(stack, wanted)) {
            count += stack.getCount();
         }
      }

      return count;
   }

   private int inventoryCount() {
      return this.exactInventoryStack.isEmpty()
         ? AiGameState.countInventoryItem(this.itemId)
         : countMatching(this.mc.player.getInventory(), this.exactInventoryStack);
   }

   private boolean init(ItemStack stack) {
      return !stack.isEmpty()
         && (
            this.exactInventoryStack.isEmpty()
               ? BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(this.itemId)
               : ItemStack.isSameItemSameComponents(stack, this.exactInventoryStack)
         );
   }

   private AiOperation.Result init(String text) {
      return this.finish(AiOperation.Status.RUNNING, text);
   }

   String receiptJson() {
      JsonObject data = AiJson.objectOf(
         "containerId",
         this.containerId,
         "phase",
         this.sync,
         "sourceSlot",
         this.sourceSlot,
         "destinationSlot",
         this.destinationSlot,
         "remainingRequestedItems",
         this.transferAmount,
         "clickDispatched",
         this.clicked,
         "waitingForNativeReceipt",
         this.pendingPickup != null,
         "expectedBeforeRevision",
         this.inventoryRevision,
         "cancelled",
         this.cancelled
      );
      RegistryAccess registries = this.startLevel instanceof ClientLevel level ? level.registryAccess() : null;
      data.add("expectedCursor", stackJson(this.expectedCursor, registries));
      data.add("expectedSource", stackJson(this.expectedSource, registries));
      data.add("expectedDestination", stackJson(this.expectedDestination, registries));
      boolean current = this.mc != null
         && this.mc.player == this.startPlayer
         && this.mc.level == this.startLevel
         && this.mc.player != null
         && this.mc.player.containerMenu == this.startMenu;
      data.addProperty("observedMenuIdentityMatches", current);
      if (current) {
         AbstractContainerMenu menu = this.mc.player.containerMenu;
         data.addProperty("observedNativeRevision", AiServerEvidence.inventoryRevision());
         data.add("observedCursor", stackJson(menu.getCarried(), registries));
         if (this.sourceSlot >= 0 && this.sourceSlot < menu.slots.size()) {
            data.add("observedSource", stackJson(menu.getSlot(this.sourceSlot).getItem(), registries));
         }

         if (this.destinationSlot >= 0 && this.destinationSlot < menu.slots.size()) {
            data.add("observedDestination", stackJson(menu.getSlot(this.destinationSlot).getItem(), registries));
         }
      }

      return data.toString();
   }

   static JsonObject stackJson(ItemStack stack, Provider registries) {
      JsonObject data = AiJson.objectOf("empty", stack.isEmpty(), "count", stack.getCount());
      if (stack.isEmpty()) {
         return data;
      }

      data.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
      if (registries == null) {
         data.addProperty("exactEncodingAvailable", false);
         return data;
      }

      DataResult<JsonElement> encoded = ItemStack.CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), stack);
      data.addProperty("exactEncodingAvailable", encoded.error().isEmpty());
      if (encoded.error().isEmpty()) {
         data.add("exactStack", encoded.getOrThrow());
      }

      return data;
   }

   private AiOperation.Result finish(AiOperation.Status status, String text) {
      return AiGameOperations.result(
         status,
         text,
         AiJson.objectOf(
            "item",
            this.itemId,
            "count",
            this.mc != null && this.mc.player != null ? this.inventoryCount() : 0,
            "target",
            this.targetCount,
            "containerId",
            this.containerId,
            "savedSlot",
            this.flush,
            "savedCount",
            this.savedStack.getCount(),
            "cursorMayContainItems",
            this.mc != null && this.mc.player != null && !this.mc.player.containerMenu.getCarried().isEmpty()
         )
      );
   }

   @Override
   public boolean canInterruptNow() {
      return !this.requiresReconciliationAfterEmergency();
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.pendingPickup != null || !this.expectedCursor.isEmpty();
   }

   @Override
   public void cancel() {
      this.cancelled = true;
   }

   record Inner_CachePool(int button, int amount) {
   }

   public enum Inner_DataUtil {
      READY,
      SATISFIED,
      CONFLICT,
      MISSING_STOCK;
   }

   public record Inner_StreamUtil(AiInventoryController.Inner_DataUtil state, int source, int amount) {
   }
}
