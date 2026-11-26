package autismclient.modules;

import autismclient.util.cx;
import autismclient.util.macro.MacroExecutor;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public final class aB {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static ServerData init_ServerData;
   private static ServerAddress init_ServerAddress;
   private static Screen init_Screen;
   private static int init_I;

   private aB() {
   }

   public static void init(ServerData server) {
      if (server != null && server.ip != null && !server.ip.isBlank()) {
         init(server, ServerAddress.parseString(server.ip));
      }
   }

   public static void init(ServerData server, ServerAddress address) {
      if (server != null && (server.ip != null && !server.ip.isBlank() || address != null)) {
         ServerData copy = new ServerData(server.name, server.ip, server.type());
         copy.copyFrom(server);
         init_ServerData = copy;
         init_ServerAddress = address != null ? address : ServerAddress.parseString(server.ip);
         init_Screen = null;
         init_I = init_I();
      }
   }

   public static ServerData init_ServerData() {
      ServerData last = init_ServerData;
      if (last == null) {
         return null;
      }

      ServerData copy = new ServerData(last.name, last.ip, last.type());
      copy.copyFrom(last);
      return copy;
   }

   public static boolean init_Z() {
      Module module = al.init_Module_Str("auto-reconnect");
      return module != null && module.isEnabled() && init_ServerData != null && init_ServerAddress != null && init_Minecraft.allowsMultiplayer();
   }

   public static boolean load_Z() {
      Module module = al.init_Module_Str("auto-reconnect");
      return module != null && module.isEnabled();
   }

   public static boolean read() {
      return init_ServerData != null && init_ServerAddress != null && init_Minecraft.allowsMultiplayer();
   }

   public static void init_V() {
      Module module = al.init_Module_Str("auto-reconnect");
      if (module != null) {
         module.setConfiguredEnabled(!module.isEnabled());
      }
   }

   public static String init_Str() {
      if (!load_Z()) {
         return "Auto Reconnect: OFF";
      }

      if (update()) {
         return "Auto Reconnect: ON  (paused)";
      }

      double seconds = write() ? Math.max(0.0, init_I / 20.0) : init_D();
      return String.format(Locale.ROOT, "Auto Reconnect: ON  (%.1fs)", seconds);
   }

   private static double init_D() {
      Module module = al.init_Module_Str("auto-reconnect");
      if (module == null) {
         return 3.5;
      }

      try {
         return Math.max(0.0, Double.parseDouble(module.value("delay")));
      } catch (NumberFormatException ignored) {
         return 3.5;
      }
   }

   public static void init(Screen screen, Screen parent) {
      if (!init_Z()) {
         init_Screen = null;
      } else {
         if (init_Screen != screen) {
            init_Screen = screen;
            init_I = init_I();
         }

         if (update()) {
            init_I = init_I();
         } else if (init_I > 0) {
            init_I--;
         } else {
            init(parent);
         }
      }
   }

   public static boolean write() {
      return init_Z() && init_I > 0 && !update();
   }

   private static boolean update() {
      return MacroExecutor.sync_Z();
   }

   public static void load_V() {
      Screen screen = cx.init_Screen(init_Minecraft);
      if (screen instanceof DisconnectedScreen) {
         Screen parent = screen instanceof autismclient.c.DisconnectedScreenDuck access ? access.autism$parent() : screen;
         init(screen, parent);
      } else {
         init_Screen = null;
      }
   }

   public static void init(Screen parent) {
      if (init_Z()) {
         Screen nextParent = parent == null ? cx.init_Screen(init_Minecraft) : parent;
         ConnectScreen.startConnecting(nextParent, init_Minecraft, init_ServerAddress, init_ServerData, false, null);
         init_Screen = null;
         init_I = init_I();
      }
   }

   public static String load_Str() {
      if (!init_Z()) {
         return "";
      }

      double seconds = Math.max(0.0, init_I / 20.0);
      return String.format(Locale.ROOT, "Auto reconnect in %.1fs", seconds);
   }

   private static int init_I() {
      Module module = al.init_Module_Str("auto-reconnect");
      if (module == null) {
         return 70;
      }

      try {
         return Math.max(0, (int)Math.round(Double.parseDouble(module.value("delay")) * 20.0));
      } catch (NumberFormatException ignored) {
         return 70;
      }
   }
}
