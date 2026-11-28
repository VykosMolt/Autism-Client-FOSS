package autismclient.modules;

import autismclient.util.OverlayScreenController;
import autismclient.util.macro.MacroExecutor;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public final class AutoReconnectController {
   private static final Minecraft MC = Minecraft.getInstance();
   private static ServerData rememberedServer;
   private static ServerAddress rememberedAddress;
   private static Screen countdownScreen;
   private static int ticksLeft;

   private AutoReconnectController() {
   }

   public static void remember(ServerData server) {
      if (server != null && server.ip != null && !server.ip.isBlank()) {
         remember(server, ServerAddress.parseString(server.ip));
      }
   }

   public static void remember(ServerData server, ServerAddress address) {
      if (server != null && (server.ip != null && !server.ip.isBlank() || address != null)) {
         ServerData copy = new ServerData(server.name, server.ip, server.type());
         copy.copyFrom(server);
         rememberedServer = copy;
         rememberedAddress = address != null ? address : ServerAddress.parseString(server.ip);
         countdownScreen = null;
         ticksLeft = delayTicks();
      }
   }

   public static ServerData lastServer() {
      ServerData last = rememberedServer;
      if (last == null) {
         return null;
      }

      ServerData copy = new ServerData(last.name, last.ip, last.type());
      copy.copyFrom(last);
      return copy;
   }

   public static boolean hasReconnectTarget() {
      Module module = al.init_Module_Str("auto-reconnect");
      return module != null && module.isEnabled() && rememberedServer != null && rememberedAddress != null && MC.allowsMultiplayer();
   }

   public static boolean isModuleEnabled() {
      Module module = al.init_Module_Str("auto-reconnect");
      return module != null && module.isEnabled();
   }

   public static boolean canReconnect() {
      return rememberedServer != null && rememberedAddress != null && MC.allowsMultiplayer();
   }

   public static void toggle() {
      Module module = al.init_Module_Str("auto-reconnect");
      if (module != null) {
         module.setConfiguredEnabled(!module.isEnabled());
      }
   }

   public static String buttonLabel() {
      if (!isModuleEnabled()) {
         return "Auto Reconnect: OFF";
      }

      if (macroHandlesRejoin()) {
         return "Auto Reconnect: ON  (paused)";
      }

      double seconds = isCountingDown() ? Math.max(0.0, ticksLeft / 20.0) : delaySeconds();
      return String.format(Locale.ROOT, "Auto Reconnect: ON  (%.1fs)", seconds);
   }

   private static double delaySeconds() {
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

   public static void tickDisconnectScreen(Screen screen, Screen parent) {
      if (!hasReconnectTarget()) {
         countdownScreen = null;
      } else {
         if (countdownScreen != screen) {
            countdownScreen = screen;
            ticksLeft = delayTicks();
         }

         if (macroHandlesRejoin()) {
            ticksLeft = delayTicks();
         } else if (ticksLeft > 0) {
            ticksLeft--;
         } else {
            reconnect(parent);
         }
      }
   }

   public static boolean isCountingDown() {
      return hasReconnectTarget() && ticksLeft > 0 && !macroHandlesRejoin();
   }

   private static boolean macroHandlesRejoin() {
      return MacroExecutor.hasRejoiningMacro();
   }

   public static void tick() {
      Screen screen = OverlayScreenController.getActualScreen(MC);
      if (screen instanceof DisconnectedScreen) {
         Screen parent = screen instanceof autismclient.mixin.duck.DisconnectedScreenDuck access ? access.autism$parent() : screen;
         tickDisconnectScreen(screen, parent);
      } else {
         countdownScreen = null;
      }
   }

   public static void reconnect(Screen parent) {
      if (hasReconnectTarget()) {
         Screen nextParent = parent == null ? OverlayScreenController.getActualScreen(MC) : parent;
         ConnectScreen.startConnecting(nextParent, MC, rememberedAddress, rememberedServer, false, null);
         countdownScreen = null;
         ticksLeft = delayTicks();
      }
   }

   public static String countdownText() {
      if (!hasReconnectTarget()) {
         return "";
      }

      double seconds = Math.max(0.0, ticksLeft / 20.0);
      return String.format(Locale.ROOT, "Auto reconnect in %.1fs", seconds);
   }

   private static int delayTicks() {
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
