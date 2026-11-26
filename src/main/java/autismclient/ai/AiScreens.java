package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import autismclient.modules.ModuleRuntimeManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.ServerData.State;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;

final class AiScreens {
   static final int init = 64;
   static final int load = 64;
   static final int read = 256;
   static final int write = 1024;
   static final int update = 256;
   static final int sync = 255;

   private AiScreens() {
   }

   static JsonObject init_JsonObject(Minecraft mc) {
      JsonObject out = new JsonObject();

      try {
         Screen screen = mc.gui.screen();
         out.addProperty("screen", screen == null ? "none" : screen.getClass().getSimpleName());
         out.addProperty("title", screen == null ? "" : init(init(screen.getTitle()), 256));
         out.addProperty("connected", mc.player != null && mc.level != null);
         if (screen instanceof DisconnectedScreen disconnected) {
            String reason = init(disconnected);
            if (!reason.isEmpty()) {
               out.addProperty("disconnectReason", init(reason, 1024));
            }
         }

         JsonArray widgets = new JsonArray();
         boolean widgetsTruncated = false;
         if (screen != null) {
            int index = 0;

            for (Object child : screen.children()) {
               if (child instanceof AbstractWidget widget) {
                  if (index >= 64) {
                     widgetsTruncated = true;
                     break;
                  }

                  widgets.add(init(widget, index++).json());
               }
            }

            if (screen instanceof autismclient.gui.d.ScreenButtonBoundsProvider access && !widgetsTruncated) {
               for (Entry<String, autismclient.gui.f.NetHelper> entry : access.init_Map().entrySet()) {
                  if (index >= 80) {
                     break;
                  }

                  autismclient.gui.f.NetHelper b = entry.getValue();
                  widgets.add(
                     new AiScreens.Inner_CachePool(index++, "button", init(entry.getKey(), 256), "", true, true, b.read(), b.write(), b.update(), b.sync()).json()
                  );
               }
            }
         }

         out.add("widgets", widgets);
         if (widgetsTruncated) {
            out.addProperty("widgetsTruncated", true);
         }

         if (screen instanceof JoinMultiplayerScreen multiplayer) {
            ServerList servers = multiplayer.getServers();
            JsonArray list = new JsonArray();
            boolean serversTruncated = false;
            if (servers != null) {
               int size = servers.size();

               for (int i = 0; i < size; i++) {
                  if (i >= 64) {
                     serversTruncated = true;
                     break;
                  }

                  ServerData data = servers.get(i);
                  if (data != null) {
                     list.add(init(data, i));
                  }
               }
            }

            out.add("serverList", list);
            if (serversTruncated) {
               out.addProperty("serverListTruncated", true);
            }
         }

         out.add("menus", AiGameplay.init());
      } catch (Throwable t) {
         out.addProperty("screen", "unavailable");
         out.addProperty("error", init(String.valueOf(t.getMessage()), 128));
      }

      return out;
   }

   static JsonObject init_JsonObject(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      AiScreens.Inner_DataUtil live = init_bY_DataUtil(mc);
      int position = init(live.infos(), args);
      AiScreens.Inner_CachePool target = live.infos().get(position);
      if (position >= live.widgets().size()) {
         autismclient.gui.f.NetHelper b = live.customBounds().get(position - live.widgets().size());
         double centerX = b.read() + b.update() / 2.0;
         double centerY = b.write() + b.sync() / 2.0;
         if (!live.screen().mouseClicked(new MouseButtonEvent(centerX, centerY, Compat263.newGlfwMouseButtonInfo(0, 0)), false)) {
            throw new IllegalArgumentException("Click was not accepted by button \"" + init(target.label(), 64) + "\"");
         }

         JsonObject customOut = init_JsonObject(mc);
         customOut.add("clicked", target.json());
         return customOut;
      } else {
         AbstractWidget widget = live.widgets().get(position);
         if (widget instanceof EditBox) {
            throw new IllegalArgumentException("Widget " + position + " is a text field; use client:screen_type");
         }

         if (!(widget instanceof AbstractButton)) {
            throw new IllegalArgumentException("Widget " + position + " (" + target.type() + ") is not clickable");
         }

         if (!widget.visible) {
            throw new IllegalArgumentException("Widget \"" + init(target.label(), 64) + "\" is not visible");
         }

         if (!widget.active) {
            throw new IllegalArgumentException("Widget \"" + init(target.label(), 64) + "\" is disabled");
         }

         if (!init(widget)) {
            throw new IllegalArgumentException("Click was not accepted by widget \"" + init(target.label(), 64) + "\"");
         }

         JsonObject out = init_JsonObject(mc);
         out.add("clicked", target.json());
         return out;
      }
   }

   static JsonObject load_JsonObject(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      Screen screen = mc.gui.screen();
      if (screen == null) {
         throw new IllegalArgumentException("No screen is open");
      }

      boolean sendPacket = AiJson.init_Z_JsonObject(args, "sendPacket", true);
      String title = init(screen.getTitle() == null ? screen.getClass().getSimpleName() : screen.getTitle().getString(), 256);
      boolean closed = sendPacket ? autismclient.util.GuiDesyncHelper.init(mc, true, false) : autismclient.util.GuiDesyncHelper.read(mc, false);
      if (!closed) {
         throw new IllegalStateException(
            "The screen could not be closed" + (sendPacket ? "" : " without notifying the server (no container open or the client is locked)")
         );
      }

      JsonObject out = init_JsonObject(mc);
      out.addProperty("closed", title);
      out.addProperty("sendPacket", sendPacket);
      return out;
   }

   static JsonObject read(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      String action = AiJson.init_Str_JsonObject(args, "action", "save").trim().toLowerCase(Locale.ROOT);
      switch (action) {
         case "save":
            if (mc.gui.screen() == null) {
               throw new IllegalArgumentException("No screen is open to store");
            } else {
               if (!autismclient.util.GuiDesyncHelper.init(mc, false)) {
                  throw new IllegalStateException("The screen could not be stored (no container or no player)");
               }

               JsonObject out = init_JsonObject(mc);
               out.addProperty("stored", true);
               return out;
            }
         case "restore":
         case "load":
            if (!ModuleRuntimeManager.init_PoolEntry().task()) {
               throw new IllegalStateException("No stored GUI to restore");
            }

            JsonObject out = init_JsonObject(mc);
            out.addProperty("restored", true);
            return out;
         default:
            throw new IllegalArgumentException("Unknown action '" + action + "'; use save or restore");
      }
   }

   static JsonObject write(JsonObject args) {
      String text = init_Str(args);
      boolean append = AiJson.init_Z_JsonObject(args, "append", false);
      Minecraft mc = Minecraft.getInstance();
      AiScreens.Inner_DataUtil live = init_bY_DataUtil(mc);
      int position = init(live.infos(), args);
      if (position < 0 || position >= live.widgets().size()) {
         throw new IllegalArgumentException("Widget " + position + " is not a standard text field");
      }

      AbstractWidget widget = live.widgets().get(position);
      if (widget instanceof EditBox box) {
         String requested = append ? box.getValue() + text : text;
         GuiEventListener previousFocus = live.screen().getFocused();
         live.screen().setFocused(box);

         try {
            box.setValue(requested);
         } finally {
            live.screen().setFocused(previousFocus);
         }

         JsonObject out = init_JsonObject(mc);
         out.add("typed", AiJson.init_JsonObject_ObjArr("index", position, "value", init(box.getValue(), 256), "truncated", !box.getValue().equals(requested)));
         return out;
      } else {
         throw new IllegalArgumentException("Widget " + position + " (" + live.infos().get(position).type() + ") is not a text field");
      }
   }

   static JsonObject update(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.gui.screen() instanceof JoinMultiplayerScreen multiplayer) {
         ServerList servers = multiplayer.getServers();
         int size = servers == null ? 0 : servers.size();
         if (size == 0) {
            throw new IllegalArgumentException("Server list is empty");
         }

         boolean hasIndex = args.has("index") && args.get("index").isJsonPrimitive();
         String ip = args.has("ip") && !args.get("ip").isJsonNull() ? args.get("ip").getAsString().trim() : "";
         if (hasIndex == !ip.isEmpty()) {
            throw new IllegalArgumentException("Provide exactly one selector: index or ip");
         }

         int matchIndex;
         if (hasIndex) {
            matchIndex = AiJson.init_I_JsonObject(args, "index", -1);
            if (matchIndex < 0 || matchIndex >= size) {
               throw new IllegalArgumentException("Server index " + matchIndex + " out of range; " + size + " servers (0.." + (size - 1) + ")");
            }
         } else {
            matchIndex = -1;

            for (int i = 0; i < size; i++) {
               if (servers.get(i).ip.equalsIgnoreCase(ip)) {
                  matchIndex = i;
                  break;
               }
            }

            if (matchIndex < 0) {
               String needle = ip.toLowerCase(Locale.ROOT);

               for (int i = 0; i < size; i++) {
                  if (servers.get(i).ip.toLowerCase(Locale.ROOT).contains(needle)) {
                     matchIndex = i;
                     break;
                  }
               }
            }

            if (matchIndex < 0) {
               List<String> ips = new ArrayList<>();

               for (int i = 0; i < size && ips.size() < 16; i++) {
                  ips.add(init(servers.get(i).ip, 48));
               }

               throw new IllegalArgumentException("No server ip matches \"" + init(ip, 64) + "\". Available: " + String.join(", ", ips));
            }
         }

         JsonObject out = load(servers.get(matchIndex), matchIndex);
         out.addProperty("serverListSize", size);
         return out;
      } else {
         throw new IllegalArgumentException("No server list on the current screen; open the multiplayer screen first");
      }
   }

   static AiOperation init_AiOperation(JsonObject args) {
      final String address = load_Str(args);
      String name = init(AiJson.init_Str_JsonObject(args, "name", "").trim(), 64);
      if (name.isEmpty()) {
         name = address;
      }

      final int timeout = init(args, 45, 120);
      final Minecraft mc = Minecraft.getInstance();
      if (autismclient.util.OverlayScreenController.init_Screen(mc) instanceof ConnectScreen) {
         throw new IllegalArgumentException("A connection attempt is already in progress");
      }

      ServerData current = mc.getCurrentServer();
      if (mc.getConnection() != null && current != null && init(current.ip, address)) {
         throw new IllegalArgumentException("Already connected to " + address);
      }

      final Object initialLevel = mc.level;
      ServerData data = new ServerData(name, address, Type.OTHER);
      Screen parent = mc.gui.screen() != null ? mc.gui.screen() : new TitleScreen();
      ConnectScreen.startConnecting(parent, mc, ServerAddress.parseString(address), data, false, null);
      return new AiOperation() {
         final long init_J = System.nanoTime() + timeout * 1000000000L;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.init_Z) {
               return this.init(AiOperation.Status.CANCELLED, "Connection to " + address + " cancelled", AiJson.init_JsonObject_ObjArr("server", address));
            } else if (mc.player != null && mc.level != null && mc.level != initialLevel) {
               return this.init(
                  AiOperation.Status.SUCCEEDED,
                  "Connected to " + address + "; world observed",
                  AiJson.init_JsonObject_ObjArr("server", address, "worldObserved", true)
               );
            } else {
               Screen screen = autismclient.util.OverlayScreenController.init_Screen(mc);
               if (screen instanceof DisconnectedScreen disconnected) {
                  String reason = AiScreens.init(AiScreens.init(disconnected), 1024);
                  return this.init(
                     AiOperation.Status.FAILED,
                     "Connection to " + address + " rejected: " + AiScreens.init(reason, 256),
                     AiJson.init_JsonObject_ObjArr("server", address, "reason", reason, "screen", "DisconnectedScreen")
                  );
               } else if (System.nanoTime() > this.init_J) {
                  this.cancel();
                  return AiGameOperations.init_AiOperation_Result(
                     AiOperation.Status.TIMED_OUT,
                     "Connection to " + address + " not established within " + timeout + "s",
                     AiJson.init_JsonObject_ObjArr("server", address, "screen", AiScreens.init_JsonObject(mc))
                  );
               } else {
                  return AiGameOperations.init_AiOperation_Result(
                     AiOperation.Status.RUNNING,
                     "Connecting to " + address,
                     AiJson.init_JsonObject_ObjArr("server", address, "screen", screen == null ? "none" : screen.getClass().getSimpleName())
                  );
               }
            }
         }

         AiOperation.Result init(AiOperation.Status status, String message, JsonObject datax) {
            this.init_Z = true;
            return AiGameOperations.init_AiOperation_Result(status, message, datax);
         }

         @Override
         public void cancel() {
            if (!this.init_Z) {
               this.init_Z = true;
               AiScreens.init(autismclient.util.OverlayScreenController.init_Screen(mc));
            }
         }
      };
   }

   static JsonObject sync(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.getConnection() == null) {
         throw new IllegalArgumentException("Not connected to a server or world");
      }

      boolean wasMultiplayer = !mc.isLocalServer();
      mc.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE);
      JsonObject out = init_JsonObject(mc);
      out.addProperty("disconnected", true);
      out.addProperty("wasMultiplayer", wasMultiplayer);
      return out;
   }

   static int init(List<AiScreens.Inner_CachePool> widgets, JsonObject args) {
      boolean hasIndex = args.has("index") && args.get("index").isJsonPrimitive();
      String label = args.has("label") && !args.get("label").isJsonNull() ? args.get("label").getAsString() : "";
      if (hasIndex == !label.isBlank()) {
         throw new IllegalArgumentException("Provide exactly one selector: index or label");
      }

      if (hasIndex) {
         int index = AiJson.init_I_JsonObject(args, "index", -1);
         if (index >= 0 && index < widgets.size()) {
            return index;
         } else {
            throw new IllegalArgumentException(
               widgets.isEmpty()
                  ? "No widgets on the current screen"
                  : "Widget index " + index + " out of range; " + widgets.size() + " widgets on the current screen (0.." + (widgets.size() - 1) + ")"
            );
         }
      } else {
         String needle = label.toLowerCase(Locale.ROOT);

         for (int i = 0; i < widgets.size(); i++) {
            if (widgets.get(i).label().toLowerCase(Locale.ROOT).contains(needle)) {
               return i;
            }
         }

         throw new IllegalArgumentException("No widget label contains \"" + init(label, 64) + "\". Available labels: " + init(widgets));
      }
   }

   static String init_Str(JsonObject args) {
      if (args.has("text") && !args.get("text").isJsonNull()) {
         String text = args.get("text").getAsString();
         if (text.length() > 256) {
            throw new IllegalArgumentException("text must be at most 256 characters");
         } else {
            return text;
         }
      } else {
         throw new IllegalArgumentException("text required");
      }
   }

   static String load_Str(JsonObject args) {
      String address = AiJson.init_Str_JsonObject(args, "address", "").trim();
      if (address.isEmpty()) {
         throw new IllegalArgumentException("address required (host or host:port)");
      } else if (address.length() > 255) {
         throw new IllegalArgumentException("address must be at most 255 characters");
      } else if (!ServerAddress.isValidAddress(address)) {
         throw new IllegalArgumentException("Invalid server address \"" + init(address, 64) + "\"; expected host or host:port with port 0..65535");
      } else {
         String host = ServerAddress.parseString(address).getHost();
         if (!host.isEmpty() && !host.chars().anyMatch(Character::isWhitespace)) {
            return address;
         } else {
            throw new IllegalArgumentException("Invalid server address \"" + init(address, 64) + "\"; host must not contain whitespace");
         }
      }
   }

   static int init(JsonObject args, int fallback, int max) {
      return Math.max(1, Math.min(max, AiJson.init_I_JsonObject(args, "timeoutSeconds", fallback)));
   }

   static boolean init(String a, String b) {
      try {
         ServerAddress first = ServerAddress.parseString(a);
         ServerAddress second = ServerAddress.parseString(b);
         return first.getPort() == second.getPort() && first.getHost().equalsIgnoreCase(second.getHost());
      } catch (Throwable t) {
         return false;
      }
   }

   static String init(String text, int max) {
      if (text == null) {
         return "";
      } else {
         return text.length() <= max ? text : text.substring(0, max);
      }
   }

   private static String init(List<AiScreens.Inner_CachePool> widgets) {
      List<String> labels = new ArrayList<>();

      for (AiScreens.Inner_CachePool widget : widgets) {
         if (!widget.label().isBlank() && labels.size() < 16) {
            labels.add("\"" + init(widget.label(), 48) + "\"");
         }
      }

      return labels.isEmpty() ? "(none)" : String.join(", ", labels);
   }

   private static AiScreens.Inner_DataUtil init_bY_DataUtil(Minecraft mc) {
      Screen screen = mc.gui.screen();
      if (screen == null) {
         throw new IllegalArgumentException("No screen is open; nothing to interact with");
      }

      List<AbstractWidget> widgets = new ArrayList<>();
      List<AiScreens.Inner_CachePool> infos = new ArrayList<>();
      List<autismclient.gui.f.NetHelper> customBounds = new ArrayList<>();

      for (Object child : screen.children()) {
         if (child instanceof AbstractWidget widget) {
            if (widgets.size() >= 64) {
               break;
            }

            infos.add(init(widget, widgets.size()));
            widgets.add(widget);
         }
      }

      if (screen instanceof autismclient.gui.d.ScreenButtonBoundsProvider access) {
         int index = infos.size();

         for (Entry<String, autismclient.gui.f.NetHelper> entry : access.init_Map().entrySet()) {
            if (infos.size() >= 80) {
               break;
            }

            autismclient.gui.f.NetHelper b = entry.getValue();
            infos.add(new AiScreens.Inner_CachePool(index++, "button", init(entry.getKey(), 256), "", true, true, b.read(), b.write(), b.update(), b.sync()));
            customBounds.add(b);
         }
      }

      return new AiScreens.Inner_DataUtil(screen, widgets, infos, customBounds);
   }

   private static AiScreens.Inner_CachePool init(AbstractWidget widget, int index) {
      String type = widget instanceof EditBox ? "edit" : (widget instanceof Checkbox ? "checkbox" : (widget instanceof AbstractButton ? "button" : "other"));
      String value = widget instanceof EditBox box ? init(box.getValue(), 256) : "";
      return new AiScreens.Inner_CachePool(
         index,
         type,
         init(init(widget.getMessage()), 256),
         value,
         widget.active,
         widget.visible,
         widget.getX(),
         widget.getY(),
         widget.getWidth(),
         widget.getHeight()
      );
   }

   private static boolean init(AbstractWidget widget) {
      double centerX = widget.getX() + widget.getWidth() / 2.0;
      double centerY = widget.getY() + widget.getHeight() / 2.0;
      return widget.mouseClicked(new MouseButtonEvent(centerX, centerY, Compat263.newGlfwMouseButtonInfo(0, 0)), false);
   }

   private static void init(Screen screen) {
      if (screen instanceof ConnectScreen) {
         try {
            for (Object child : screen.children()) {
               if (child instanceof AbstractButton button && button.active && init(button)) {
                  return;
               }
            }
         } catch (Throwable var4) {
         }
      }
   }

   private static JsonObject init(ServerData data, int index) {
      JsonObject out = AiJson.init_JsonObject_ObjArr(
         "index", index, "name", init(data.name, 128), "ip", init(data.ip, 128), "motd", init(init(data.motd), 256), "state", data.state().name()
      );
      if (data.players != null) {
         out.addProperty("onlinePlayers", data.players.online());
         out.addProperty("maxPlayers", data.players.max());
      }

      if (data.state() == State.SUCCESSFUL) {
         out.addProperty("latency", data.ping);
      }

      return out;
   }

   private static JsonObject load(ServerData data, int index) {
      JsonObject out = init(data, index);
      out.addProperty("status", init(init(data.status), 256));
      out.addProperty("version", init(init(data.version), 128));
      out.addProperty("protocol", data.protocol);
      out.addProperty("ping", data.ping);
      out.addProperty("type", data.type().name());
      out.addProperty("lan", data.isLan());
      out.addProperty("realm", data.isRealm());
      out.addProperty("resourcePack", data.getResourcePackStatus().name());
      JsonArray hover = new JsonArray();

      for (Object line : data.playerList) {
         if (hover.size() < 16) {
            hover.add(init(line instanceof Component component ? component.getString() : "", 64));
         }
      }

      out.add("playerList", hover);
      return out;
   }

   private static String init(DisconnectedScreen screen) {
      try {
         Field field = AiScreens.Inner_StreamUtil.init;
         if (field == null) {
            return "";
         } else {
            return field.get(screen) instanceof DisconnectionDetails details ? details.reason().getString() : "";
         }
      } catch (Throwable t) {
         return "";
      }
   }

   private static String init(Component component) {
      return component == null ? "" : component.getString();
   }

   record Inner_CachePool(int index, String type, String label, String value, boolean active, boolean visible, int x, int y, int width, int height) {
      JsonObject json() {
         JsonObject out = AiJson.init_JsonObject_ObjArr(
            "index",
            this.index,
            "type",
            this.type,
            "label",
            this.label,
            "active",
            this.active,
            "visible",
            this.visible,
            "x",
            this.x,
            "y",
            this.y,
            "width",
            this.width,
            "height",
            this.height
         );
         if ("edit".equals(this.type)) {
            out.addProperty("value", this.value);
         }

         return out;
      }
   }

   private record Inner_DataUtil(Screen screen, List<AbstractWidget> widgets, List<AiScreens.Inner_CachePool> infos, List<autismclient.gui.f.NetHelper> customBounds) {
   }

   private static final class Inner_StreamUtil {
      private static final Field init = init(DisconnectedScreen.class, "details");

      private static Field init(Class<?> type, String name) {
         try {
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field;
         } catch (Throwable t) {
            return null;
         }
      }
   }
}
