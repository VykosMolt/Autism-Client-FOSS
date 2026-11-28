package autismclient.ai;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.ContainerInput;

public final class AiMenuClose {
   private static volatile AiMenuClose.CloseReceipt activeReceipt;
   private static AiMenuClose.CloseReceipt closingReceipt;

   private AiMenuClose() {
   }

   public static boolean closeMenu() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && mc.getConnection() != null) {
         LocalPlayer player = mc.player;
         if (player.containerMenu == player.inventoryMenu) {
            return true;
         }

         if (player.containerMenu.getCarried().isEmpty() && activeReceipt == null) {
            AiMenuClose.CloseReceipt receipt = new AiMenuClose.CloseReceipt(player, mc.level, player.containerMenu.containerId, System.nanoTime());
            closingReceipt = receipt;
            activeReceipt = receipt;

            try {
               player.closeContainer();
            } finally {
               closingReceipt = null;
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean init(ServerboundContainerClosePacket packet) {
      AiMenuClose.CloseReceipt receipt = closingReceipt;
      if (receipt != null && receipt.menu == packet.getContainerId()) {
         receipt.closePacket = packet;
         return true;
      } else {
         return false;
      }
   }

   public static void init(Packet<?> packet) {
      AiMenuClose.CloseReceipt receipt = activeReceipt;
      if (receipt != null) {
         receipt.onSent(packet);
      }
   }

   public static void init(int menu, boolean cursorEmpty) {
      AiMenuClose.CloseReceipt receipt = activeReceipt;
      if (receipt != null) {
         receipt.onInventorySync(menu, cursorEmpty);
      }
   }

   public static boolean load() {
      return activeReceipt != null;
   }

   public static AiMenuClose.ReceiptState receiptState() {
      AiMenuClose.CloseReceipt receipt = activeReceipt;
      if (receipt == null) {
         return AiMenuClose.ReceiptState.READY;
      }

      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      AiMenuClose.ReceiptState state = receipt.evaluate(player, mc.level, player != null && player.containerMenu == player.inventoryMenu, System.nanoTime());
      if (state == AiMenuClose.ReceiptState.READY) {
         activeReceipt = null;
         return state;
      }

      if (state == AiMenuClose.ReceiptState.WAITING && player != null && mc.getConnection() != null) {
         if (receipt.closeSent && receipt.probePacket == null && player.inventoryMenu.getCarried().isEmpty()) {
            ServerboundContainerClickPacket packet = new ServerboundContainerClickPacket(
               0, AiContainerTransactions.mismatchStateId(player.inventoryMenu.getStateId()), (short)36, (byte)0, ContainerInput.SWAP, new Int2ObjectOpenHashMap<>(), HashedStack.EMPTY
            );
            receipt.probePacket = packet;
            mc.getConnection().send(packet);
         }

         return AiMenuClose.ReceiptState.WAITING;
      } else {
         return state;
      }
   }

   static void reset() {
      closingReceipt = null;
      activeReceipt = null;
   }

   static final class CloseReceipt {
      final Object player;
      final Object world;
      final int menu;
      final long started;
      volatile Packet<?> closePacket;
      volatile Packet<?> probePacket;
      volatile boolean closeSent;
      volatile boolean probeSent;
      boolean cursorConfirmedEmpty;
      AiMenuClose.ReceiptState settledState;

      CloseReceipt(Object player, Object world, int menu, long started) {
         this.player = player;
         this.world = world;
         this.menu = menu;
         this.started = started;
      }

      void onSent(Packet<?> packet) {
         if (packet == this.closePacket) {
            this.closeSent = true;
         }

         if (packet == this.probePacket) {
            this.probeSent = true;
         }
      }

      void onInventorySync(int id, boolean cursorEmpty) {
         if (this.closeSent && this.probeSent && id == 0) {
            this.cursorConfirmedEmpty = cursorEmpty;
         }
      }

      AiMenuClose.ReceiptState evaluate(Object player, Object world, boolean inventoryMenu, long now) {
         if (player != this.player || world != this.world || !inventoryMenu) {
            return this.settledState = AiMenuClose.ReceiptState.UNCERTAIN;
         } else if (this.cursorConfirmedEmpty) {
            return this.settledState = AiMenuClose.ReceiptState.READY;
         } else if (this.settledState != null) {
            return this.settledState;
         } else {
            return now - this.started >= 10000000000L ? (this.settledState = AiMenuClose.ReceiptState.UNCERTAIN) : AiMenuClose.ReceiptState.WAITING;
         }
      }
   }

   public enum ReceiptState {
      READY,
      WAITING,
      UNCERTAIN;
   }
}
