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
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final String init_Str;
   private final int init_I;
   private final int load_I;
   private final long init_J;
   private int read_I = -1;
   private int write_I = -1;
   private int update_I;
   private int sync;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private ItemStack load_ItemStack = ItemStack.EMPTY;
   private ItemStack read_ItemStack = ItemStack.EMPTY;
   private long load_J;
   private AiContainerTransactions.Inner_DataUtil init_b_DataUtil;
   private boolean init_Z;
   private boolean load_Z;
   private final Set<Integer> init_Set;
   private final Set<Integer> load_Set;
   private final ItemStack write_ItemStack;
   private final int flush;
   private final ItemStack update_ItemStack;
   private final Object init_Obj = this.init_Minecraft == null ? null : this.init_Minecraft.level;
   private final Object load_Obj = this.init_Minecraft == null ? null : this.init_Minecraft.player;
   private final Object read_Obj = this.init_Minecraft != null && this.init_Minecraft.player != null ? this.init_Minecraft.player.containerMenu : null;

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
         AiJson.init_JsonObject_ObjArr("item", BuiltInRegistries.ITEM.getKey(wanted.getItem()).toString(), "count", 0, "containerId", containerId),
         null,
         Set.of(destination),
         destination,
         wanted
      );
   }

   AiInventoryController(int containerId, ItemStack exact, int count, Set<Integer> allowedSources, Set<Integer> allowedDestinations) {
      this(
         AiJson.init_JsonObject_ObjArr("item", BuiltInRegistries.ITEM.getKey(exact.getItem()).toString(), "count", count, "containerId", containerId),
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
      this.update_ItemStack = exactInventoryStack.copy();
      this.init_Set = allowedSources == null ? null : Set.copyOf(allowedSources);
      this.load_Set = allowedDestinations == null ? null : Set.copyOf(allowedDestinations);
      this.write_ItemStack = savedStack.copy();
      this.flush = savedDestination;
      if (savedDestination >= 0 && savedStack.isEmpty()) {
         throw new IllegalArgumentException("A saved storage slot requires a nonempty stack");
      }

      this.init_Str = AiJson.init_Str_JsonObject(args, "item", "");
      this.init_I = AiJson.init_I_JsonObject(args, "count", -1);
      this.load_I = AiJson.init_I_JsonObject(args, "containerId", -1);
      if (this.init_Str.isBlank() || this.init_I < 0 || this.init_I > 2304 && allowedSources == null) {
         throw new IllegalArgumentException("Expected item registry id and inventory count 0..2304");
      }

      this.init_J = System.nanoTime() + Math.max(1, Math.min(3600, AiJson.init_I_JsonObject(args, "timeoutSeconds", 180))) * 1000000000L;
   }

   @Override
   public AiOperation.Result tick() {
      if (this.init_Z || this.init_Minecraft == null || this.init_Minecraft.player == null) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_7(
            this.load_Z ? AiOperation.Status.UNCERTAIN : AiOperation.Status.CANCELLED, "Reconciliation cancelled; inspect cursor and storage before continuing"
         );
      }

      if (this.init_Minecraft.level == this.init_Obj && this.init_Minecraft.player == this.load_Obj) {
         AbstractContainerMenu menu = this.init_Minecraft.player.containerMenu;
         if (menu.containerId != this.load_I || menu != this.read_Obj) {
            return this.init_AiOperation_Result_AiOperation_Status_Str_7(
               this.load_Z ? AiOperation.Status.UNCERTAIN : AiOperation.Status.CANCELLED, "Storage container changed; inspect cursor before continuing"
            );
         }

         if (!AiContainer.init_Z(menu)) {
            return this.init_AiOperation_Result_AiOperation_Status_Str_7(
               AiOperation.Status.FAILED, "Open a storage chest, barrel, shulker box, hopper or dispenser first"
            );
         }

         if (System.nanoTime() > this.init_J) {
            return this.init_AiOperation_Result_AiOperation_Status_Str_7(
               this.load_Z ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT, "Reconciliation deadline; no uncertain click is repeated"
            );
         }

         if (this.init_b_DataUtil != null) {
            AiContainerTransactions.Inner_StreamUtil state = this.init_b_DataUtil.init(menu, System.nanoTime());
            if (state == AiContainerTransactions.Inner_StreamUtil.init) {
               return this.init("Waiting for the server's storage slots and cursor");
            }

            if (state != AiContainerTransactions.Inner_StreamUtil.load) {
               return this.init_AiOperation_Result_AiOperation_Status_Str_7(
                  AiOperation.Status.UNCERTAIN, "The server did not confirm the storage transfer. Inspect the open storage and cursor before resuming."
               );
            }

            this.init_b_DataUtil = null;
         }

         if (this.sync == 0) {
            if (!menu.getCarried().isEmpty()) {
               return this.init_AiOperation_Result_AiOperation_Status_Str_7(AiOperation.Status.FAILED, "Clear the cursor stack before reconciliation");
            }

            if (this.flush >= 0) {
               AiInventoryController.Inner_StreamUtil transfer = init_ae_StreamUtil_AbstractContainerM(
                  menu, this.init_Minecraft.player.getInventory(), this.init_Minecraft.player, this.flush, this.write_ItemStack
               );
               if (transfer.state() == AiInventoryController.Inner_DataUtil.load) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_7(AiOperation.Status.SUCCEEDED, "Saved storage slot and empty cursor verified");
               }

               if (transfer.state() == AiInventoryController.Inner_DataUtil.read) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_7(
                     AiOperation.Status.FAILED, "Saved storage slot has incompatible contents or capacity; existing items retained"
                  );
               }

               if (transfer.state() == AiInventoryController.Inner_DataUtil.write) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_7(
                     AiOperation.Status.FAILED, "Player inventory lacks the saved item's exact components"
                  );
               }

               this.read_I = transfer.source();
               this.write_I = this.flush;
               this.update_I = transfer.amount();
               return this.init_AiOperation_Result_AbstractContainerM_Str(menu, "Picking up the saved storage item");
            } else {
               int count = this.init_I_23();
               int delta = count - this.init_I;
               if (delta == 0) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_7(
                     AiOperation.Status.SUCCEEDED, "Exact player inventory target verified; surplus retained in storage"
                  );
               }

               boolean deposit = delta > 0;
               this.read_I = -1;
               this.write_I = -1;

               for (int pass = 0; pass < 2 && this.read_I < 0; pass++) {
                  for (Slot slot : menu.slots) {
                     if ((this.init_Set == null || this.init_Set.contains(slot.index))
                        && slot.container == this.init_Minecraft.player.getInventory() == deposit
                        && this.init(slot.getItem())
                        && slot.mayPickup(this.init_Minecraft.player)
                        && (pass != 0 || AiItems.init_Z(slot.getItem()))) {
                        this.read_I = slot.index;
                        break;
                     }
                  }
               }

               if (this.read_I < 0) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_7(
                     AiOperation.Status.FAILED, deposit ? "No player stack available to deposit" : "Storage lacks the requested item quantity"
                  );
               }

               ItemStack stack = menu.getSlot(this.read_I).getItem();
               this.write_I = init_I_AbstractContainerM(menu, this.init_Minecraft.player.getInventory(), stack, deposit, this.load_Set);
               if (this.write_I < 0) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_7(
                     AiOperation.Status.FAILED, deposit ? "Storage is full; surplus retained" : "Player inventory is full"
                  );
               }

               this.update_I = Math.min(
                  Math.abs(delta),
                  Math.min(stack.getCount(), menu.getSlot(this.write_I).getMaxStackSize(stack) - menu.getSlot(this.write_I).getItem().getCount())
               );
               return this.init_AiOperation_Result_AbstractContainerM_Str(menu, "Picking up a stack for exact transfer");
            }
         } else {
            if (this.sync != 1) {
               this.sync = 0;
               return this.init("Checking reconciled inventory");
            }

            if (this.update_I > 0) {
               AiInventoryController.Inner_CachePool choice = init(menu, this.write_I, this.update_I);
               if (choice == null) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_7(
                     AiOperation.Status.UNCERTAIN, "Storage or cursor changed before the next transfer; inspect before continuing"
                  );
               }

               ItemStack cursor = menu.getCarried();
               ItemStack existing = menu.getSlot(this.write_I).getItem();
               if (!this.init_Z_I(
                  this.write_I,
                  choice.button(),
                  menu.getSlot(this.read_I).getItem(),
                  cursor.copyWithCount(existing.getCount() + choice.amount()),
                  cursor.copyWithCount(cursor.getCount() - choice.amount())
               )) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_7(
                     AiOperation.Status.UNCERTAIN, "Could not start the next storage click. Inspect the cursor before resuming."
                  );
               }

               this.update_I = this.update_I - choice.amount();
               return this.init("Transferring the exact quantity into storage or inventory");
            } else if (menu.getCarried().isEmpty()) {
               this.sync = 0;
               return this.init("Checking reconciled inventory");
            } else {
               ItemStack cursor = menu.getCarried();
               Slot slot = menu.getSlot(this.read_I);
               ItemStack existing = slot.getItem();
               int count = existing.getCount() + cursor.getCount();
               if (slot.mayPlace(cursor)
                  && count <= slot.getMaxStackSize(cursor)
                  && (existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, cursor))
                  && this.init_Z_I(this.read_I, 0, cursor.copyWithCount(count), menu.getSlot(this.write_I).getItem(), ItemStack.EMPTY)) {
                  this.sync = 2;
                  return this.init("Returning the unused stack without disposal");
               } else {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_7(
                     AiOperation.Status.UNCERTAIN, "The source slot cannot safely take back the cursor stack. Inspect the open storage."
                  );
               }
            }
         }
      } else {
         return this.init_AiOperation_Result_AiOperation_Status_Str_7(
            this.load_Z ? AiOperation.Status.UNCERTAIN : AiOperation.Status.CANCELLED, "World or player changed; storage actions are not replayed"
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

   public static AiInventoryController.Inner_StreamUtil init_ae_StreamUtil_AbstractContainerM(
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
               return new AiInventoryController.Inner_StreamUtil(AiInventoryController.Inner_DataUtil.load, -1, 0);
            }

            for (Slot candidate : menu.slots) {
               ItemStack stack = candidate.getItem();
               if (candidate.container == inventory && !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, wanted) && candidate.mayPickup(player)) {
                  return new AiInventoryController.Inner_StreamUtil(AiInventoryController.Inner_DataUtil.init, candidate.index, Math.min(wanted.getCount() - current.getCount(), stack.getCount()));
               }
            }

            return new AiInventoryController.Inner_StreamUtil(AiInventoryController.Inner_DataUtil.write, -1, 0);
         } else {
            return new AiInventoryController.Inner_StreamUtil(AiInventoryController.Inner_DataUtil.read, -1, 0);
         }
      } else {
         return new AiInventoryController.Inner_StreamUtil(AiInventoryController.Inner_DataUtil.read, -1, 0);
      }
   }

   static int init_I_AbstractContainerM(AbstractContainerMenu menu, Inventory inventory, ItemStack stack, boolean deposit, Set<Integer> allowed) {
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
      if (!this.init_Z_I(this.read_I, 0, ItemStack.EMPTY, menu.getSlot(this.write_I).getItem(), menu.getSlot(this.read_I).getItem())) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_7(
            this.load_Z ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED, "Could not start the storage transfer. Inspect the inventory and cursor."
         );
      }

      this.sync = 1;
      return this.init(message);
   }

   private boolean init_Z_I(int slot, int button, ItemStack from, ItemStack to, ItemStack cursor) {
      this.load_J = AiServerEvidence.init_J();
      AbstractContainerMenu menu = this.init_Minecraft.player.containerMenu;
      this.init_b_DataUtil = AiContainerTransactions.init(menu, slot, button, Map.of(this.read_I, from, this.write_I, to), cursor);
      if (this.init_b_DataUtil == null) {
         return false;
      }

      this.load_Z = true;
      this.init_ItemStack = cursor.copy();
      this.load_ItemStack = from.copy();
      this.read_ItemStack = to.copy();
      return true;
   }

   static int init_I_Container_ItemStack_2(Container inventory, ItemStack wanted) {
      int count = 0;

      for (int slot = 0; slot < Math.min(36, inventory.getContainerSize()); slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (ItemStack.isSameItemSameComponents(stack, wanted)) {
            count += stack.getCount();
         }
      }

      return count;
   }

   private int init_I_23() {
      return this.update_ItemStack.isEmpty()
         ? AiGameState.init_I_Str(this.init_Str)
         : init_I_Container_ItemStack_2(this.init_Minecraft.player.getInventory(), this.update_ItemStack);
   }

   private boolean init(ItemStack stack) {
      return !stack.isEmpty()
         && (
            this.update_ItemStack.isEmpty()
               ? BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(this.init_Str)
               : ItemStack.isSameItemSameComponents(stack, this.update_ItemStack)
         );
   }

   private AiOperation.Result init(String text) {
      return this.init_AiOperation_Result_AiOperation_Status_Str_7(AiOperation.Status.RUNNING, text);
   }

   String init_Str_13() {
      JsonObject data = AiJson.init_JsonObject_ObjArr(
         "containerId",
         this.load_I,
         "phase",
         this.sync,
         "sourceSlot",
         this.read_I,
         "destinationSlot",
         this.write_I,
         "remainingRequestedItems",
         this.update_I,
         "clickDispatched",
         this.load_Z,
         "waitingForNativeReceipt",
         this.init_b_DataUtil != null,
         "expectedBeforeRevision",
         this.load_J,
         "cancelled",
         this.init_Z
      );
      RegistryAccess registries = this.init_Obj instanceof ClientLevel level ? level.registryAccess() : null;
      data.add("expectedCursor", init_JsonObject_ItemStack_HolderLookup_Provi(this.init_ItemStack, registries));
      data.add("expectedSource", init_JsonObject_ItemStack_HolderLookup_Provi(this.load_ItemStack, registries));
      data.add("expectedDestination", init_JsonObject_ItemStack_HolderLookup_Provi(this.read_ItemStack, registries));
      boolean current = this.init_Minecraft != null
         && this.init_Minecraft.player == this.load_Obj
         && this.init_Minecraft.level == this.init_Obj
         && this.init_Minecraft.player != null
         && this.init_Minecraft.player.containerMenu == this.read_Obj;
      data.addProperty("observedMenuIdentityMatches", current);
      if (current) {
         AbstractContainerMenu menu = this.init_Minecraft.player.containerMenu;
         data.addProperty("observedNativeRevision", AiServerEvidence.init_J());
         data.add("observedCursor", init_JsonObject_ItemStack_HolderLookup_Provi(menu.getCarried(), registries));
         if (this.read_I >= 0 && this.read_I < menu.slots.size()) {
            data.add("observedSource", init_JsonObject_ItemStack_HolderLookup_Provi(menu.getSlot(this.read_I).getItem(), registries));
         }

         if (this.write_I >= 0 && this.write_I < menu.slots.size()) {
            data.add("observedDestination", init_JsonObject_ItemStack_HolderLookup_Provi(menu.getSlot(this.write_I).getItem(), registries));
         }
      }

      return data.toString();
   }

   static JsonObject init_JsonObject_ItemStack_HolderLookup_Provi(ItemStack stack, Provider registries) {
      JsonObject data = AiJson.init_JsonObject_ObjArr("empty", stack.isEmpty(), "count", stack.getCount());
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

   private AiOperation.Result init_AiOperation_Result_AiOperation_Status_Str_7(AiOperation.Status status, String text) {
      return AiGameOperations.init_AiOperation_Result(
         status,
         text,
         AiJson.init_JsonObject_ObjArr(
            "item",
            this.init_Str,
            "count",
            this.init_Minecraft != null && this.init_Minecraft.player != null ? this.init_I_23() : 0,
            "target",
            this.init_I,
            "containerId",
            this.load_I,
            "savedSlot",
            this.flush,
            "savedCount",
            this.write_ItemStack.getCount(),
            "cursorMayContainItems",
            this.init_Minecraft != null && this.init_Minecraft.player != null && !this.init_Minecraft.player.containerMenu.getCarried().isEmpty()
         )
      );
   }

   @Override
   public boolean canInterruptNow() {
      return !this.requiresReconciliationAfterEmergency();
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_b_DataUtil != null || !this.init_ItemStack.isEmpty();
   }

   @Override
   public void cancel() {
      this.init_Z = true;
   }

   record Inner_CachePool(int button, int amount) {
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write;
   }

   public record Inner_StreamUtil(AiInventoryController.Inner_DataUtil state, int source, int amount) {
   }
}
