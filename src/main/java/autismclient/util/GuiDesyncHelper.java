package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

public final class GuiDesyncHelper {
   private GuiDesyncHelper() {
   }

   public static boolean init(Minecraft mc) {
      return init(mc, true);
   }

   public static boolean init(Minecraft mc, boolean notify) {
      if (mc != null && mc.gui.screen() != null && mc.player != null) {
         AutismSessionState.getInstance().storeGuiScreen(mc.gui.screen(), mc.player.containerMenu);
         if (notify) {
            bi.init(init(), -13248397);
         }

         return true;
      } else {
         if (notify) {
            bi.write("Failed to store GUI.");
         }

         return false;
      }
   }

   private static String init() {
      int keyCode = AutismConfig.getGlobal().keybindLoadGui;
      return keyCode == -1 ? "GUI stored." : "GUI stored. Press " + AutismKeybindOverlay.keyName(keyCode) + " to restore.";
   }

   public static boolean load(Minecraft mc, boolean sendPacket) {
      return init(mc, sendPacket, true);
   }

   public static boolean init(Minecraft mc, boolean sendPacket, boolean notify) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      if (mc != null && mc.gui.screen() != null) {
         if (mc.gui.screen() instanceof DesyncableScreen special) {
            if (sendPacket) {
               special.autism$closeWithPacket(notify);
            } else {
               special.autism$closeWithoutPacket(notify);
            }

            return true;
         } else {
            if (sendPacket) {
               if (mc.player != null && mc.player.containerMenu != null && mc.player.containerMenu != mc.player.inventoryMenu) {
                  mc.player.closeContainer();
               } else {
                  mc.gui.setScreen(null);
               }
            } else if (mc.player != null && mc.player.containerMenu != null && mc.player.containerMenu != mc.player.inventoryMenu) {
               AutismSessionState.getInstance().setDropNextContainerClose(true);
               mc.player.closeContainer();
               if (notify) {
                  AutismClientMessaging.sendPrefixed("GUI closed without packet.");
               }
            } else {
               mc.gui.setScreen(null);
               if (notify) {
                  AutismClientMessaging.sendPrefixed("Screen closed locally.");
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean load(Minecraft mc) {
      return read(mc, true);
   }

   public static boolean read(Minecraft mc, boolean notify) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      if (mc != null && mc.gui.screen() != null) {
         if (mc.gui.screen() instanceof DesyncableScreen special) {
            special.autism$desync(notify);
            return true;
         } else if (mc.getConnection() != null && mc.player != null && mc.player.containerMenu != null) {
            mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
            if (notify) {
               AutismClientMessaging.sendPrefixed("GUI desynced: close packet sent while client screen stays open.");
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
