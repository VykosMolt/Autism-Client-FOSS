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
   private static volatile AiMenuClose.Inner_DataUtil init;
   private static AiMenuClose.Inner_DataUtil load;

   private AiMenuClose() {
   }

   public static boolean init_Z() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && mc.getConnection() != null) {
         LocalPlayer player = mc.player;
         if (player.containerMenu == player.inventoryMenu) {
            return true;
         }

         if (player.containerMenu.getCarried().isEmpty() && init == null) {
            AiMenuClose.Inner_DataUtil receipt = new AiMenuClose.Inner_DataUtil(player, mc.level, player.containerMenu.containerId, System.nanoTime());
            load = receipt;
            init = receipt;

            try {
               player.closeContainer();
            } finally {
               load = null;
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
      AiMenuClose.Inner_DataUtil receipt = load;
      if (receipt != null && receipt.init_I == packet.getContainerId()) {
         receipt.init_Packet = packet;
         return true;
      } else {
         return false;
      }
   }

   public static void init(Packet<?> packet) {
      AiMenuClose.Inner_DataUtil receipt = init;
      if (receipt != null) {
         receipt.init(packet);
      }
   }

   public static void init(int menu, boolean cursorEmpty) {
      AiMenuClose.Inner_DataUtil receipt = init;
      if (receipt != null) {
         receipt.init(menu, cursorEmpty);
      }
   }

   public static boolean load() {
      return init != null;
   }

   public static AiMenuClose.Inner_StreamUtil init_aq_StreamUtil() {
      AiMenuClose.Inner_DataUtil receipt = init;
      if (receipt == null) {
         return AiMenuClose.Inner_StreamUtil.READY;
      }

      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      AiMenuClose.Inner_StreamUtil state = receipt.init(player, mc.level, player != null && player.containerMenu == player.inventoryMenu, System.nanoTime());
      if (state == AiMenuClose.Inner_StreamUtil.READY) {
         init = null;
         return state;
      }

      if (state == AiMenuClose.Inner_StreamUtil.WAITING && player != null && mc.getConnection() != null) {
         if (receipt.init_Z && receipt.load_Packet == null && player.inventoryMenu.getCarried().isEmpty()) {
            ServerboundContainerClickPacket packet = new ServerboundContainerClickPacket(
               0, AiContainerTransactions.init(player.inventoryMenu.getStateId()), (short)36, (byte)0, ContainerInput.SWAP, new Int2ObjectOpenHashMap<>(), HashedStack.EMPTY
            );
            receipt.load_Packet = packet;
            mc.getConnection().send(packet);
         }

         return AiMenuClose.Inner_StreamUtil.WAITING;
      } else {
         return state;
      }
   }

   static void init_V() {
      load = null;
      init = null;
   }

   static final class Inner_DataUtil {
      final Object init_Obj;
      final Object load_Obj;
      final int init_I;
      final long init_J;
      volatile Packet<?> init_Packet;
      volatile Packet<?> load_Packet;
      volatile boolean init_Z;
      volatile boolean load_Z;
      boolean read;
      AiMenuClose.Inner_StreamUtil init_aq_StreamUtil;

      Inner_DataUtil(Object player, Object world, int menu, long started) {
         this.init_Obj = player;
         this.load_Obj = world;
         this.init_I = menu;
         this.init_J = started;
      }

      void init(Packet<?> packet) {
         if (packet == this.init_Packet) {
            this.init_Z = true;
         }

         if (packet == this.load_Packet) {
            this.load_Z = true;
         }
      }

      void init(int id, boolean cursorEmpty) {
         if (this.init_Z && this.load_Z && id == 0) {
            this.read = cursorEmpty;
         }
      }

      AiMenuClose.Inner_StreamUtil init(Object player, Object world, boolean inventoryMenu, long now) {
         if (player != this.init_Obj || world != this.load_Obj || !inventoryMenu) {
            return this.init_aq_StreamUtil = AiMenuClose.Inner_StreamUtil.UNCERTAIN;
         } else if (this.read) {
            return this.init_aq_StreamUtil = AiMenuClose.Inner_StreamUtil.READY;
         } else if (this.init_aq_StreamUtil != null) {
            return this.init_aq_StreamUtil;
         } else {
            return now - this.init_J >= 10000000000L ? (this.init_aq_StreamUtil = AiMenuClose.Inner_StreamUtil.UNCERTAIN) : AiMenuClose.Inner_StreamUtil.WAITING;
         }
      }
   }

   public enum Inner_StreamUtil {
      READY,
      WAITING,
      UNCERTAIN;
   }
}
