package autismclient.util;

import autismclient.gui.macro.editor.ActionEditorOverlay;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

public final class LoggedPacketActions {
   private LoggedPacketActions() {
   }

   public static boolean init(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      return entry != null && "C2S".equalsIgnoreCase(entry.init_Str) && entry.init_Packet != null;
   }

   public static boolean load(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      return init(entry);
   }

   public static boolean read(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      if (!load(entry)) {
         AutismClientMessaging.sendPrefixed("§cOnly C2S packets can be sent.");
         return false;
      }

      if (entry.init_Packet instanceof ServerboundCustomPayloadPacket) {
         return tick(entry);
      }

      Packet<?> regenerated = PacketRegenerator.init(entry.init_Packet);
      if (regenerated == null) {
         AutismClientMessaging.sendPrefixed("§cCannot regenerate: " + entry.load_Str);
         return false;
      }

      try {
         DirectPacketSender.init(regenerated);
         AutismClientMessaging.sendPrefixed("Sent: " + entry.load_Str);
         return true;
      } catch (Exception e) {
         AutismClientMessaging.sendPrefixed("§cSend failed: " + e.getMessage());
         return false;
      }
   }

   private static boolean tick(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      AutismPayloadSupport.PayloadEntry snapshot = AutismPayloadSupport.init_AutismPayloadSuppo(entry);
      if (snapshot != null && snapshot.load_Str() != null && !snapshot.load_Str().isBlank()) {
         byte[] rawBytes = snapshot.init_BArr();
         if (!AutismPayloadSupport.init_Z(snapshot.load_Str(), rawBytes, snapshot.update())) {
            AutismClientMessaging.sendPrefixed("§cFailed to send payload: " + entry.load_Str);
            return false;
         } else {
            AutismClientMessaging.sendPrefixed("Sent payload: " + snapshot.load_Str());
            return true;
         }
      } else {
         Packet<?> regenerated = PacketRegenerator.init(entry.init_Packet);
         if (regenerated != null) {
            try {
               DirectPacketSender.init(regenerated);
               AutismClientMessaging.sendPrefixed("Sent: " + entry.load_Str);
               return true;
            } catch (Exception e) {
               AutismClientMessaging.sendPrefixed("§cSend failed: " + e.getMessage());
               return false;
            }
         } else {
            AutismClientMessaging.sendPrefixed("§cCannot send: no payload data for " + entry.load_Str);
            return false;
         }
      }
   }

   public static boolean write(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      return init(entry);
   }

   public static boolean update(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      return entry != null && entry.load_Str != null && !entry.load_Str.isBlank() && entry.init_Str != null && !entry.init_Str.isBlank();
   }

   public static boolean sync(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      return entry != null && entry.read_Z && "C2S".equalsIgnoreCase(entry.init_Str);
   }

   public static boolean flush(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      return sync(entry);
   }

   public static boolean close(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      if (!init(entry)) {
         AutismClientMessaging.sendPrefixed("§cOnly C2S packets can be queued.");
         return false;
      } else {
         AutismSessionState.init_ck().init_V_Packet(entry.init_Packet);
         AutismClientMessaging.sendPrefixed("Queued: " + entry.load_Str);
         return true;
      }
   }

   public static boolean open(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      if (!write(entry)) {
         AutismClientMessaging.sendPrefixed("§cOnly C2S packets can be added as send actions.");
         return false;
      } else {
         AutismMacroEditorOverlay macroEditor = init_AutismMacroEditorO();
         if (macroEditor == null) {
            AutismClientMessaging.sendPrefixed("§cCannot open the macro editor.");
            return false;
         } else {
            autismclient.util.macro.SendPacketAction action = new autismclient.util.macro.SendPacketAction();
            action.waitForGuiBefore = false;
            action.waitForGuiAfter = false;
            action.guiName = "";
            action.packets.add(new AutismSessionState.DelayedPacket(entry.init_Packet, 0, AutismSessionState.ReplayMode.EXACT));
            macroEditor.init_V_MacroAction_2(action);
            OverlayManager.init_bk().update_V(macroEditor);
            AutismClientMessaging.sendPrefixed("Added send action: " + entry.load_Str);
            return true;
         }
      }
   }

   public static boolean poll(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      if (!update(entry)) {
         AutismClientMessaging.sendPrefixed("§cCannot add this packet as a wait condition.");
         return false;
      } else {
         AutismMacroEditorOverlay macroEditor = init_AutismMacroEditorO();
         if (macroEditor == null) {
            AutismClientMessaging.sendPrefixed("§cCannot open the macro editor.");
            return false;
         } else {
            String target = autismclient.util.macro.WaitForPacketAction.init(entry.init_Str, entry.load_Str);
            if (target.isEmpty()) {
               AutismClientMessaging.sendPrefixed("§cCannot add this packet as a wait condition.");
               return false;
            } else {
               autismclient.util.macro.WaitForPacketAction action = new autismclient.util.macro.WaitForPacketAction(target);
               action.packetNames.add(target);
               macroEditor.init_V_MacroAction_2(action);
               OverlayManager.init_bk().update_V(macroEditor);
               AutismClientMessaging.sendPrefixed("Added wait condition: " + autismclient.util.macro.WaitForPacketAction.write(target));
               return true;
            }
         }
      }
   }

   public static boolean push(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      if (!sync(entry)) {
         AutismClientMessaging.sendPrefixed("§cOnly captured C2S custom payload packets can be edited.");
         return false;
      } else {
         autismclient.util.macro.PayloadAction action = AutismPayloadSupport.init_H(entry);
         if (action == null) {
            AutismClientMessaging.sendPrefixed("§cFailed to seed payload editor from packet.");
            return false;
         } else {
            ActionEditorOverlay.init_ActionEditorOverla_2().init_V_H(action);
            OverlayManager.init_bk().update_V(ActionEditorOverlay.init_ActionEditorOverla_2());
            return true;
         }
      }
   }

   public static boolean pop(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      if (!flush(entry)) {
         AutismClientMessaging.sendPrefixed("§cOnly captured C2S custom payload packets can be added as payload actions.");
         return false;
      } else {
         AutismMacroEditorOverlay macroEditor = init_AutismMacroEditorO();
         if (macroEditor == null) {
            AutismClientMessaging.sendPrefixed("§cCannot open the macro editor.");
            return false;
         } else {
            autismclient.util.macro.PayloadAction action = AutismPayloadSupport.init_H(entry);
            if (action == null) {
               AutismClientMessaging.sendPrefixed("§cFailed to create payload action from packet.");
               return false;
            } else {
               macroEditor.init_V_MacroAction_2(action);
               OverlayManager.init_bk().update_V(macroEditor);
               AutismClientMessaging.sendPrefixed("Added payload action: " + entry.load_Str);
               return true;
            }
         }
      }
   }

   public static boolean init_Z() {
      return load() != null;
   }

   private static AutismMacroEditorOverlay init_AutismMacroEditorO() {
      AutismMacroEditorOverlay macroEditor = load();
      if (macroEditor != null) {
         OverlayManager.init_bk().update_V(macroEditor);
         return macroEditor;
      }

      macroEditor = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
      if (macroEditor == null) {
         return null;
      }

      AutismMacro existingMacro = AutismSessionState.init_ck().init_aM();
      macroEditor.init_V_aM(existingMacro);
      OverlayManager.init_bk().update_V(macroEditor);
      return macroEditor;
   }

   private static AutismMacroEditorOverlay load() {
      for (AutismOverlay overlay : OverlayManager.init_bk().init_List()) {
         if (overlay instanceof AutismMacroEditorOverlay macroEditor && macroEditor.sync_Z()) {
            return macroEditor;
         }
      }

      return null;
   }
}
