package autismclient.util;

import autismclient.mixin.accessor.AutismHandledScreenAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ArrayListDeque;
import net.minecraft.world.inventory.Slot;

public final class aG {
   public static final String init_Str = "Type message or /command...";
   private static final Minecraft init_Minecraft = Minecraft.getInstance();

   private aG() {
   }

   public static void init_V() {
      boolean newValue = !ck.init_ck().read_Z();
      autismclient.modules.ModuleRuntimeManager.init_PoolEntry().init_Z_Z(newValue);
      bi.init("Send Packets " + (newValue ? "on" : "off"), newValue ? -13248397 : -50373);
   }

   public static void load_V() {
      boolean newValue = !ck.init_ck().write_Z();
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
      int sent = module.init_I_Z(newValue);
      module.init(newValue, sent);
   }

   public static void read() {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
      module.load(module.read_I());
   }

   public static void write() {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
      module.read(module.write_I());
   }

   public static void update() {
      af.load(init_Minecraft, false);
   }

   public static void sync() {
      if (!af.load(init_Minecraft)) {
         AutismClientMessaging.sendPrefixed("Failed to desync: no open networked GUI.");
      }
   }

   public static void flush() {
      af.init(init_Minecraft);
   }

   public static void close() {
      if (autismclient.modules.ModuleRuntimeManager.init_PoolEntry().task()) {
         bi.init("GUI restored.", -13248397);
      } else {
         bi.write("No stored GUI.");
      }
   }

   public static void open() {
      ag.init();
   }

   public static void poll() {
      if (!autismclient.modules.aD.read_Z()) {
         autismclient.modules.ModuleRuntimeManager.init_PoolEntry().push(false);
         if (init_Minecraft.getConnection() != null) {
            ck.init_ck().init_I_ClientPacketListen(init_Minecraft.getConnection());
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

         if (autismclient.commands.CommandManager.init_Z(message)) {
            if (autismclient.commands.CommandManager.load_Z(message)) {
               return "";
            }

            String body = autismclient.commands.CommandManager.init_Str(message);
            if (!body.isBlank()) {
               autismclient.commands.CommandManager.read(body);
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
               slot = Integer.toString(aw.init_I(init_Minecraft, focused.index));
            }
         }
      }

      return new String[]{revision, syncId, slot};
   }

   public static int init_I() {
      return ct.init_I_I_ct_DataUtil(-4743522, ct.Inner_DataUtil.TEXT);
   }

   public static int[] init_IArr() {
      return new int[]{
         ct.init_I_I_ct_DataUtil(-46518, ct.Inner_DataUtil.ACCENT),
         ct.init_I_I_ct_DataUtil(-791321, ct.Inner_DataUtil.TEXT),
         ct.init_I_I_ct_DataUtil(-7350273, ct.Inner_DataUtil.ACCENT)
      };
   }
}
