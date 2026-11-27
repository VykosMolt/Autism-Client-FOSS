package autismclient.ai;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AiContainerTransactions {
   private static AiContainerTransactions.Inner_DataUtil activeReceipt;
   private static boolean ignoreNextSwapUpdatesForTest;

   private AiContainerTransactions() {
   }

   public static int mismatchStateId(int applied) {
      return (applied ^ 16384) & 32767;
   }

   public static AiContainerTransactions.Inner_DataUtil sendVerifiedSwap(AbstractContainerMenu menu, int source, int destination, int hotbar) {
      ItemStack from = menu.getSlot(source).getItem();
      ItemStack to = menu.getSlot(destination).getItem();
      return menu.getCarried().isEmpty() && !ItemStack.matches(from, to)
         ? sendVerifiedClick(menu, source, hotbar, ContainerInput.SWAP, Map.of(source, to, destination, from), ItemStack.EMPTY, true)
         : null;
   }

   static AiContainerTransactions.Inner_DataUtil sendVerifiedPickup(AbstractContainerMenu menu, int slot, int button, Map<Integer, ItemStack> expected, ItemStack cursor) {
      return sendVerifiedClick(menu, slot, button, ContainerInput.PICKUP, expected, cursor, false);
   }

   private static AiContainerTransactions.Inner_DataUtil sendVerifiedClick(
      AbstractContainerMenu menu, int slot, int button, ContainerInput type, Map<Integer, ItemStack> expected, ItemStack cursor, boolean swap
   ) {
      Minecraft mc = Minecraft.getInstance();
      ClientPacketListener connection = mc.getConnection();
      long now = System.nanoTime();
      if (mc.player == null || connection == null || mc.player.containerMenu != menu) {
         return null;
      }

      if (activeReceipt != null && activeReceipt.menu == menu && !activeReceipt.isTimedOut(now)) {
         return null;
      }

      AiContainerTransactions.Inner_DataUtil receipt = new AiContainerTransactions.Inner_DataUtil(menu, expected, cursor, swap, now);
      if (swap) {
         receipt.swapHotbarButton = button;
         receipt.ignoreUpdates = ignoreNextSwapUpdatesForTest;
         ignoreNextSwapUpdatesForTest = false;
      }

      connection.send(
         new ServerboundContainerClickPacket(
            menu.containerId,
            menu.getStateId(),
            (short)slot,
            (byte)button,
            type,
            new Int2ObjectOpenHashMap<>(),
            HashedStack.create(menu.getCarried(), connection.decoratedHashOpsGenenerator())
         )
      );
      activeReceipt = receipt;
      return receipt;
   }

   public static void onContainerSetSlot(int id, int slot, ItemStack stack) {
      AiContainerTransactions.Inner_DataUtil receipt = currentActiveReceipt();
      if (receipt != null && !receipt.ignoreUpdates) {
         if (receipt.menu.containerId == id) {
            receipt.recordSlotUpdate(slot, stack);
         } else {
            LocalPlayer player = Minecraft.getInstance().player;
            if (id == player.inventoryMenu.containerId && slot >= 0 && slot < player.inventoryMenu.slots.size()) {
               Slot cell = player.inventoryMenu.getSlot(slot);
               if (cell.container == player.getInventory()) {
                  onPlayerInventorySlot(cell.getContainerSlot(), stack);
               }
            }
         }
      }
   }

   public static void onPlayerInventorySlot(int slot, ItemStack stack) {
      AiContainerTransactions.Inner_DataUtil receipt = currentActiveReceipt();
      if (receipt != null && !receipt.ignoreUpdates) {
         Inventory inventory = Minecraft.getInstance().player.getInventory();

         for (int index : receipt.expectedSlots.keySet()) {
            Slot cell = receipt.menu.getSlot(index);
            if (cell.container == inventory && cell.getContainerSlot() == slot) {
               receipt.recordSlotUpdate(index, stack);
            }
         }
      }
   }

   public static void onCarriedSlot(ItemStack stack) {
      AiContainerTransactions.Inner_DataUtil receipt = currentActiveReceipt();
      if (receipt != null) {
         receipt.recordCursorUpdate(stack);
      }
   }

   public static void onContainerSetContent(int id, List<ItemStack> stacks, ItemStack carried) {
      AiMenuClose.init(id, carried.isEmpty());
      AiContainerTransactions.Inner_DataUtil receipt = currentActiveReceipt();
      if (receipt != null && receipt.menu.containerId == id) {
         receipt.recordFullSync(stacks, carried);
      }
   }

   private static AiContainerTransactions.Inner_DataUtil currentActiveReceipt() {
      if (activeReceipt == null) {
         return null;
      }

      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null || player.containerMenu != activeReceipt.menu) {
         activeReceipt = null;
      }

      return activeReceipt;
   }

   public static void tickActiveSwap() {
      AiContainerTransactions.Inner_DataUtil receipt = currentActiveReceipt();
      if (receipt != null && receipt.isSwap) {
         receipt.pollAndResync(Minecraft.getInstance().player.containerMenu, System.nanoTime());
      }
   }

   static boolean hasActiveSwap() {
      AiContainerTransactions.Inner_DataUtil receipt = currentActiveReceipt();
      return receipt != null && receipt.isSwap;
   }

   static void suppressNextSwapUpdatesForTest() {
      ignoreNextSwapUpdatesForTest = true;
   }

   static void clearAll() {
      activeReceipt = null;
      ignoreNextSwapUpdatesForTest = false;
      AiMenuClose.reset();
   }

   public static void sendPredictedClick(int containerId, int slot, int button, ContainerInput type) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      ClientPacketListener connection = mc.getConnection();
      if (player != null && connection != null) {
         AbstractContainerMenu menu = player.containerMenu;
         if (containerId == menu.containerId
            && slot >= 0
            && slot < menu.slots.size()
            && slot <= 32767
            && button >= 0
            && (button <= 8 || type == ContainerInput.SWAP && button == 40)) {
            ArrayList<ItemStack> before = new ArrayList<>(menu.slots.size());

            for (Slot cell : menu.slots) {
               before.add(cell.getItem().copy());
            }

            menu.clicked(slot, button, type, player);
            Int2ObjectOpenHashMap<HashedStack> changed = new Int2ObjectOpenHashMap<>();

            for (int i = 0; i < before.size(); i++) {
               if (!ItemStack.matches(before.get(i), menu.getSlot(i).getItem())) {
                  changed.put(i, HashedStack.create(menu.getSlot(i).getItem(), connection.decoratedHashOpsGenenerator()));
               }
            }

            connection.send(
               new ServerboundContainerClickPacket(
                  containerId,
                  mismatchStateId(menu.getStateId()),
                  (short)slot,
                  (byte)button,
                  type,
                  changed,
                  HashedStack.create(menu.getCarried(), connection.decoratedHashOpsGenenerator())
               )
            );
         } else {
            throw new IllegalArgumentException("Stale or invalid container transaction");
         }
      } else {
         throw new IllegalStateException("Disconnected before container transaction");
      }
   }

   public static final class Inner_DataUtil {
      private static final long TIMEOUT_NANOS = 8000000000L;
      private static final long RESYNC_DELAY_NANOS = 750000000L;
      private final AbstractContainerMenu menu;
      private final Map<Integer, ItemStack> initialSlots = new HashMap<>();
      private final Map<Integer, ItemStack> expectedSlots = new HashMap<>();
      private final Map<Integer, ItemStack> confirmedSlotUpdates = new HashMap<>();
      private final ItemStack expectedCursor;
      private final boolean cursorChanged;
      private final boolean isSwap;
      private final long startedAtNanos;
      private ItemStack confirmedCursorUpdate;
      private AiContainerTransactions.Inner_StreamUtil terminalState;
      private int swapHotbarButton = -1;
      private boolean resyncRequested;
      private boolean ignoreUpdates;

      Inner_DataUtil(AbstractContainerMenu menu, Map<Integer, ItemStack> expected, ItemStack cursor, boolean swap, long started) {
         this.menu = menu;
         expected.forEach((slot, stack) -> {
            this.initialSlots.put(slot, menu.getSlot(slot).getItem().copy());
            this.expectedSlots.put(slot, stack.copy());
         });
         this.expectedCursor = cursor.copy();
         this.cursorChanged = !ItemStack.matches(cursor, menu.getCarried());
         this.isSwap = swap;
         this.startedAtNanos = started;
      }

      void recordSlotUpdate(int slot, ItemStack stack) {
         if (this.expectedSlots.containsKey(slot)) {
            this.confirmedSlotUpdates.put(slot, stack.copy());
         }
      }

      void recordCursorUpdate(ItemStack stack) {
         this.confirmedCursorUpdate = stack.copy();
      }

      void recordFullSync(List<ItemStack> stacks, ItemStack carried) {
         if (stacks.size() == this.menu.slots.size()) {
            this.expectedSlots.keySet().forEach(slot -> this.recordSlotUpdate(slot, stacks.get(slot)));
            this.recordCursorUpdate(carried);
         }
      }

      public AiContainerTransactions.Inner_StreamUtil checkStatus(AbstractContainerMenu current, long now) {
         if (this.terminalState != null) {
            return this.terminalState;
         }

         if (current != this.menu) {
            return this.complete(AiContainerTransactions.Inner_StreamUtil.UNCERTAIN);
         }

         if (ItemStack.matches(this.expectedCursor, this.menu.getCarried())
            && (this.confirmedCursorUpdate == null ? !this.cursorChanged : ItemStack.matches(this.expectedCursor, this.confirmedCursorUpdate))) {
            boolean confirmed = true;
            boolean exchanged = this.isSwap;
            boolean preserved = false;

            for (Entry<Integer, ItemStack> entry : this.expectedSlots.entrySet()) {
               int slot = entry.getKey();
               ItemStack wanted = entry.getValue();
               ItemStack actual = this.menu.getSlot(slot).getItem();
               ItemStack update = this.confirmedSlotUpdates.get(slot);
               boolean matches = ItemStack.matches(wanted, actual);
               boolean authoritative = update != null && ItemStack.matches(update, actual);
               confirmed &= matches && (authoritative || update == null && ItemStack.matches(wanted, this.initialSlots.get(slot)));
               exchanged &= authoritative && !ItemStack.matches(actual, this.initialSlots.get(slot));
               preserved |= matches;
            }

            if (confirmed) {
               return this.complete(AiContainerTransactions.Inner_StreamUtil.CONFIRMED);
            }

            if (exchanged && preserved) {
               return this.complete(AiContainerTransactions.Inner_StreamUtil.SWAPPED_DIFFERENT_STACK);
            }
         }

         return this.isTimedOut(now) ? this.complete(AiContainerTransactions.Inner_StreamUtil.UNCERTAIN) : AiContainerTransactions.Inner_StreamUtil.WAITING;
      }

      public AiContainerTransactions.Inner_StreamUtil pollAndResync(AbstractContainerMenu current, long now) {
         AiContainerTransactions.Inner_StreamUtil state = this.checkStatus(current, now);
         if (state == AiContainerTransactions.Inner_StreamUtil.WAITING && this.swapHotbarButton >= 0 && this.shouldSendSwapResync(now)) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            ClientPacketListener connection = mc.getConnection();
            if (player != null
               && connection != null
               && player.inventoryMenu == this.menu
               && player.containerMenu == this.menu
               && this.menu.getCarried().isEmpty()) {
               int slot = 36 + this.swapHotbarButton;
               Slot cell = this.menu.getSlot(slot);
               if (cell.container == player.getInventory() && cell.getContainerSlot() == this.swapHotbarButton) {
                  this.resyncRequested = true;
                  connection.send(
                     new ServerboundContainerClickPacket(
                        this.menu.containerId,
                        AiContainerTransactions.mismatchStateId(this.menu.getStateId()),
                        (short)slot,
                        (byte)this.swapHotbarButton,
                        ContainerInput.SWAP,
                        new Int2ObjectOpenHashMap<>(),
                        HashedStack.create(ItemStack.EMPTY, connection.decoratedHashOpsGenenerator())
                     )
                  );
                  return state;
               } else {
                  return state;
               }
            } else {
               return state;
            }
         } else {
            return state;
         }
      }

      boolean shouldSendSwapResync(long now) {
         return this.isSwap && !this.resyncRequested && this.terminalState == null && now - this.startedAtNanos >= 750000000L && !this.isTimedOut(now);
      }

      private boolean isTimedOut(long now) {
         return now - this.startedAtNanos >= 8000000000L;
      }

      private AiContainerTransactions.Inner_StreamUtil complete(AiContainerTransactions.Inner_StreamUtil state) {
         if (AiContainerTransactions.activeReceipt == this) {
            AiContainerTransactions.activeReceipt = null;
         }

         this.terminalState = state;
         return state;
      }
   }

   public enum Inner_StreamUtil {
      WAITING,
      CONFIRMED,
      SWAPPED_DIFFERENT_STACK,
      UNCERTAIN;
   }
}
