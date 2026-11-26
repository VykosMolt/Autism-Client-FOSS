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
   public static final String init_Str = "Type message or /command...";
   private static final Minecraft init_Minecraft = Minecraft.getInstance();

   private LauncherQuickActions() {
   }

   public static void init_V() {
      boolean newValue = !AutismSessionState.getInstance().isSendPacketsEnabled();
      autismclient.modules.ModuleRuntimeManager.getInstance().setAndSaveSendGuiPackets(newValue);
      bi.init("Send Packets " + (newValue ? "on" : "off"), newValue ? -13248397 : -50373);
   }

   public static void load_V() {
      boolean newValue = !AutismSessionState.getInstance().isDelayPacketsEnabled();
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      int sent = module.toggleDelayPackets(newValue);
      module.notifyDelayToggle(newValue, sent);
   }

   public static void read() {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      module.notifyQueueFlushed(module.flushPacketQueue());
   }

   public static void write() {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      module.notifyQueueCleared(module.clearPacketQueue());
   }

   public static void update() {
      GuiDesyncHelper.load(init_Minecraft, false);
   }

   public static void sync() {
      if (!GuiDesyncHelper.load(init_Minecraft)) {
         AutismClientMessaging.sendPrefixed("Failed to desync: no open networked GUI.");
      }
   }

   public static void flush() {
      GuiDesyncHelper.init(init_Minecraft);
   }

   public static void close() {
      if (autismclient.modules.ModuleRuntimeManager.getInstance().restoreStoredGuiAsync()) {
         bi.init("GUI restored.", -13248397);
      } else {
         bi.write("No stored GUI.");
      }
   }

   public static void open() {
      ScreenTitleCopier.init();
   }

   public static void poll() {
      if (!autismclient.modules.aD.read_Z()) {
         autismclient.modules.ModuleRuntimeManager.getInstance().setDelayGuiPackets(false);
         if (init_Minecraft.getConnection() != null) {
            AutismSessionState.getInstance().flushDelayedPackets(init_Minecraft.getConnection());
            init_Minecraft.getConnection().getConnection().disconnect(Component.literal("Disconnecting (Autism)"));
         }
      }
   }

   public static String init(String raw) {
      if (!autismclient.modules.aD.read_Z() && raw != null) {
         String message = raw.trim();
         if (message.isEmpty()) {
            String last = init_Str();
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
            if (init_Minecraft.getConnection() == null) {
               return null;
            }

            if (message.startsWith("/") && message.length() > 1) {
               init_Minecraft.getConnection().sendCommand(message.substring(1));
            } else {
               init_Minecraft.getConnection().sendChat(message);
            }
         }

         AutismClientMessaging.rememberRecentChat(message);
         return message;
      } else {
         return null;
      }
   }

   public static String init_Str() {
      try {
         if (init_Minecraft.gui != null && init_Minecraft.gui.hud.getChat() != null) {
            ArrayListDeque<String> recent = init_Minecraft.gui.hud.getChat().getRecentChat();
            return recent == null ? null : recent.peekLast();
         } else {
            return null;
         }
      } catch (Throwable ignored) {
         return null;
      }
   }

   public static String load_Str() {
      String last = init_Str();
      return last != null && !last.isBlank() ? last : "Type message or /command...";
   }

   public static List<String> init_List() {
      try {
         if (init_Minecraft.gui != null && init_Minecraft.gui.hud.getChat() != null) {
            ArrayListDeque<String> recent = init_Minecraft.gui.hud.getChat().getRecentChat();
            return recent == null ? List.of() : new ArrayList<>(recent);
         } else {
            return List.of();
         }
      } catch (Throwable ignored) {
         return List.of();
      }
   }

   public static String[] init_StrArr() {
      String revision = "--";
      String syncId = "--";
      String slot = "--";
      if (init_Minecraft.player != null && init_Minecraft.player.containerMenu != null) {
         revision = Integer.toString(init_Minecraft.player.containerMenu.getStateId());
         syncId = Integer.toString(init_Minecraft.player.containerMenu.containerId);
         if (init_Minecraft.gui.screen() instanceof AbstractContainerScreen<?> handledScreen) {
            Slot focused = ((AutismHandledScreenAccessor)handledScreen).getFocusedSlot();
            if (focused != null) {
               slot = Integer.toString(InventorySlotHelper.toVisibleSlot(init_Minecraft, focused.index));
            }
         }
      }

      return new String[]{revision, syncId, slot};
   }

   public static int init_I() {
      return ThemePaletteManager.recolor(-4743522, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int[] init_IArr() {
      return new int[]{
         ThemePaletteManager.recolor(-46518, ThemePaletteManager.ColorRole.ACCENT),
         ThemePaletteManager.recolor(-791321, ThemePaletteManager.ColorRole.TEXT),
         ThemePaletteManager.recolor(-7350273, ThemePaletteManager.ColorRole.ACCENT)
      };
   }
}
