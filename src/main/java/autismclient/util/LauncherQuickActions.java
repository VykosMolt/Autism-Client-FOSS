package autismclient.util;

import autismclient.mixin.accessor.AutismHandledScreenAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ArrayListDeque;
import net.minecraft.world.inventory.Slot;

public final class LauncherQuickActions {
   public static final String INPUT_PLACEHOLDER = "Type message or /command...";
   private static final Minecraft MINECRAFT = Minecraft.getInstance();

   private LauncherQuickActions() {
   }

   public static void toggleSendPackets() {
      boolean newValue = !AutismSessionState.getInstance().isSendPacketsEnabled();
      autismclient.modules.ModuleRuntimeManager.getInstance().setAndSaveSendGuiPackets(newValue);
      bi.init("Send Packets " + (newValue ? "on" : "off"), newValue ? -13248397 : -50373);
   }

   public static void toggleDelayPackets() {
      boolean newValue = !AutismSessionState.getInstance().isDelayPacketsEnabled();
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      int sent = module.toggleDelayPackets(newValue);
      module.notifyDelayToggle(newValue, sent);
   }

   public static void flushQueue() {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      module.notifyQueueFlushed(module.flushPacketQueue());
   }

   public static void clearQueue() {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      module.notifyQueueCleared(module.clearPacketQueue());
   }

   public static void update() {
      GuiDesyncHelper.load(MINECRAFT, false);
   }

   public static void desync() {
      if (!GuiDesyncHelper.load(MINECRAFT)) {
         AutismClientMessaging.sendPrefixed("Failed to desync: no open networked GUI.");
      }
   }

   public static void flush() {
      GuiDesyncHelper.saveGui(MINECRAFT);
   }

   public static void restoreGui() {
      if (autismclient.modules.ModuleRuntimeManager.getInstance().restoreStoredGuiAsync()) {
         bi.init("GUI restored.", -13248397);
      } else {
         bi.write("No stored GUI.");
      }
   }

   public static void copyScreenTitle() {
      ScreenTitleCopier.copy();
   }

   public static void poll() {
      if (!autismclient.modules.aD.read_Z()) {
         autismclient.modules.ModuleRuntimeManager.getInstance().setDelayGuiPackets(false);
         if (MINECRAFT.getConnection() != null) {
            AutismSessionState.getInstance().flushDelayedPackets(MINECRAFT.getConnection());
            MINECRAFT.getConnection().getConnection().disconnect(Component.literal("Disconnecting (Autism)"));
         }
      }
   }

   public static String init(String raw) {
      if (!autismclient.modules.aD.read_Z() && raw != null) {
         String message = raw.trim();
         if (message.isEmpty()) {
            String last = lastSentMessage();
            if (last == null || last.isBlank()) {
               return null;
            }

            message = last.trim();
         }

         if (autismclient.commands.CommandManager.hasCommandPrefix(message)) {
            if (autismclient.commands.CommandManager.shouldSuppressInHiddenMode(message)) {
               return "";
            }

            String body = autismclient.commands.CommandManager.stripPrefix(message);
            if (!body.isBlank()) {
               autismclient.commands.CommandManager.execute(body);
            }
         } else {
            if (MINECRAFT.getConnection() == null) {
               return null;
            }

            if (message.startsWith("/") && message.length() > 1) {
               MINECRAFT.getConnection().sendCommand(message.substring(1));
            } else {
               MINECRAFT.getConnection().sendChat(message);
            }
         }

         AutismClientMessaging.rememberRecentChat(message);
         return message;
      } else {
         return null;
      }
   }

   public static String lastSentMessage() {
      try {
         if (MINECRAFT.gui != null && MINECRAFT.gui.hud.getChat() != null) {
            ArrayListDeque<String> recent = MINECRAFT.gui.hud.getChat().getRecentChat();
            return recent == null ? null : recent.peekLast();
         } else {
            return null;
         }
      } catch (Throwable ignored) {
         return null;
      }
   }

   public static String inputPlaceholder() {
      String last = lastSentMessage();
      return last != null && !last.isBlank() ? last : "Type message or /command...";
   }

   public static List<String> recentChat() {
      try {
         if (MINECRAFT.gui != null && MINECRAFT.gui.hud.getChat() != null) {
            ArrayListDeque<String> recent = MINECRAFT.gui.hud.getChat().getRecentChat();
            return recent == null ? List.of() : new ArrayList<>(recent);
         } else {
            return List.of();
         }
      } catch (Throwable ignored) {
         return List.of();
      }
   }

   public static String[] containerMetrics() {
      String revision = "--";
      String syncId = "--";
      String slot = "--";
      if (MINECRAFT.player != null && MINECRAFT.player.containerMenu != null) {
         revision = Integer.toString(MINECRAFT.player.containerMenu.getStateId());
         syncId = Integer.toString(MINECRAFT.player.containerMenu.containerId);
         if (MINECRAFT.gui.screen() instanceof AbstractContainerScreen<?> handledScreen) {
            Slot focused = ((AutismHandledScreenAccessor)handledScreen).getFocusedSlot();
            if (focused != null) {
               slot = Integer.toString(InventorySlotHelper.toVisibleSlot(MINECRAFT, focused.index));
            }
         }
      }

      return new String[]{revision, syncId, slot};
   }

   public static int keyColor() {
      return ThemePaletteManager.recolor(-4743522, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int[] valueColors() {
      return new int[]{
         ThemePaletteManager.recolor(-46518, ThemePaletteManager.ColorRole.ACCENT),
         ThemePaletteManager.recolor(-791321, ThemePaletteManager.ColorRole.TEXT),
         ThemePaletteManager.recolor(-7350273, ThemePaletteManager.ColorRole.ACCENT)
      };
   }
}
