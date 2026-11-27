package autismclient.util;

import autismclient.gui.macro.editor.ActionEditorOverlay;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

public final class LoggedPacketActions {
   private LoggedPacketActions() {
   }

   public static boolean init(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      return entry != null && "C2S".equalsIgnoreCase(entry.direction) && entry.packet != null;
   }

   public static boolean load(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      return init(entry);
   }

   public static boolean read(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (!load(entry)) {
         AutismClientMessaging.sendPrefixed("§cOnly C2S packets can be sent.");
         return false;
      }

      if (entry.packet instanceof ServerboundCustomPayloadPacket) {
         return tick(entry);
      }

      Packet<?> regenerated = PacketRegenerator.init(entry.packet);
      if (regenerated == null) {
         AutismClientMessaging.sendPrefixed("§cCannot regenerate: " + entry.name);
         return false;
      }

      try {
         DirectPacketSender.init(regenerated);
         AutismClientMessaging.sendPrefixed("Sent: " + entry.name);
         return true;
      } catch (Exception e) {
         AutismClientMessaging.sendPrefixed("§cSend failed: " + e.getMessage());
         return false;
      }
   }

   private static boolean tick(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      AutismPayloadSupport.PayloadEntry snapshot = AutismPayloadSupport.resolveEntryPayload(entry);
      if (snapshot != null && snapshot.channel() != null && !snapshot.channel().isBlank()) {
         byte[] rawBytes = snapshot.rawBytes();
         if (!AutismPayloadSupport.sendCustomPayload(snapshot.channel(), rawBytes, snapshot.protocolPhase())) {
            AutismClientMessaging.sendPrefixed("§cFailed to send payload: " + entry.name);
            return false;
         } else {
            AutismClientMessaging.sendPrefixed("Sent payload: " + snapshot.channel());
            return true;
         }
      } else {
         Packet<?> regenerated = PacketRegenerator.init(entry.packet);
         if (regenerated != null) {
            try {
               DirectPacketSender.init(regenerated);
               AutismClientMessaging.sendPrefixed("Sent: " + entry.name);
               return true;
            } catch (Exception e) {
               AutismClientMessaging.sendPrefixed("§cSend failed: " + e.getMessage());
               return false;
            }
         } else {
            AutismClientMessaging.sendPrefixed("§cCannot send: no payload data for " + entry.name);
            return false;
         }
      }
   }

   public static boolean write(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      return init(entry);
   }

   public static boolean update(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      return entry != null && entry.name != null && !entry.name.isBlank() && entry.direction != null && !entry.direction.isBlank();
   }

   public static boolean sync(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      return entry != null && entry.payloadPacket && "C2S".equalsIgnoreCase(entry.direction);
   }

   public static boolean flush(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      return sync(entry);
   }

   public static boolean close(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (!init(entry)) {
         AutismClientMessaging.sendPrefixed("§cOnly C2S packets can be queued.");
         return false;
      } else {
         AutismSessionState.getInstance().enqueueDelayedPacket(entry.packet);
         AutismClientMessaging.sendPrefixed("Queued: " + entry.name);
         return true;
      }
   }

   public static boolean open(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
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
            action.packets.add(new AutismSessionState.DelayedPacket(entry.packet, 0, AutismSessionState.ReplayMode.EXACT));
            macroEditor.appendAction(action);
            OverlayManager.getInstance().bringToFront(macroEditor);
            AutismClientMessaging.sendPrefixed("Added send action: " + entry.name);
            return true;
         }
      }
   }

   public static boolean poll(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (!update(entry)) {
         AutismClientMessaging.sendPrefixed("§cCannot add this packet as a wait condition.");
         return false;
      } else {
         AutismMacroEditorOverlay macroEditor = init_AutismMacroEditorO();
         if (macroEditor == null) {
            AutismClientMessaging.sendPrefixed("§cCannot open the macro editor.");
            return false;
         } else {
            String target = autismclient.util.macro.WaitForPacketAction.init(entry.direction, entry.name);
            if (target.isEmpty()) {
               AutismClientMessaging.sendPrefixed("§cCannot add this packet as a wait condition.");
               return false;
            } else {
               autismclient.util.macro.WaitForPacketAction action = new autismclient.util.macro.WaitForPacketAction(target);
               action.packetNames.add(target);
               macroEditor.appendAction(action);
               OverlayManager.getInstance().bringToFront(macroEditor);
               AutismClientMessaging.sendPrefixed("Added wait condition: " + autismclient.util.macro.WaitForPacketAction.write(target));
               return true;
            }
         }
      }
   }

   public static boolean push(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (!sync(entry)) {
         AutismClientMessaging.sendPrefixed("§cOnly captured C2S custom payload packets can be edited.");
         return false;
      } else {
         autismclient.util.macro.PayloadAction action = AutismPayloadSupport.toPayloadAction(entry);
         if (action == null) {
            AutismClientMessaging.sendPrefixed("§cFailed to seed payload editor from packet.");
            return false;
         } else {
            ActionEditorOverlay.getInstance().openPayload(action);
            OverlayManager.getInstance().bringToFront(ActionEditorOverlay.getInstance());
            return true;
         }
      }
   }

   public static boolean pop(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (!flush(entry)) {
         AutismClientMessaging.sendPrefixed("§cOnly captured C2S custom payload packets can be added as payload actions.");
         return false;
      } else {
         AutismMacroEditorOverlay macroEditor = init_AutismMacroEditorO();
         if (macroEditor == null) {
            AutismClientMessaging.sendPrefixed("§cCannot open the macro editor.");
            return false;
         } else {
            autismclient.util.macro.PayloadAction action = AutismPayloadSupport.toPayloadAction(entry);
            if (action == null) {
               AutismClientMessaging.sendPrefixed("§cFailed to create payload action from packet.");
               return false;
            } else {
               macroEditor.appendAction(action);
               OverlayManager.getInstance().bringToFront(macroEditor);
               AutismClientMessaging.sendPrefixed("Added payload action: " + entry.name);
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
         OverlayManager.getInstance().bringToFront(macroEditor);
         return macroEditor;
      }

      macroEditor = AutismMacroEditorOverlay.getInstance();
      if (macroEditor == null) {
         return null;
      }

      AutismMacro existingMacro = AutismSessionState.getInstance().getEditingMacro();
      macroEditor.openEditor(existingMacro);
      OverlayManager.getInstance().bringToFront(macroEditor);
      return macroEditor;
   }

   private static AutismMacroEditorOverlay load() {
      for (AutismOverlay overlay : OverlayManager.getInstance().getOverlays()) {
         if (overlay instanceof AutismMacroEditorOverlay macroEditor && macroEditor.isVisible()) {
            return macroEditor;
         }
      }

      return null;
   }
}
