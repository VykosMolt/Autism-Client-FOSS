package autismclient.ai;

import autismclient.modules.ModuleRuntimeManager;
import autismclient.util.AuditDeathWaypoints;
import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismConfig;
import autismclient.util.AutismProfile;
import autismclient.util.AutismWaypoints;
import autismclient.util.PacketPresetManager;
import autismclient.util.macro.MacroExecutor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;

final class AiClientConfig {
   static final List<String> init = List.of("master", "accent", "outline", "text", "toggle", "backdrop", "success", "danger", "button", "header", "hover");
   private static final List<String> load = List.of(
      "TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT", "TOP_CENTER", "BOTTOM_CENTER", "CENTER_LEFT", "CENTER_RIGHT", "CENTER"
   );

   private AiClientConfig() {
   }

   static <T> T init(Supplier<T> action) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.isSameThread()) {
         return action.get();
      }

      CompletableFuture<T> future = new CompletableFuture<>();
      mc.execute(() -> {
         try {
            future.complete(action.get());
         } catch (Throwable t) {
            future.completeExceptionally(t);
         }
      });

      try {
         return future.get(15L, TimeUnit.SECONDS);
      } catch (ExecutionException e) {
         Throwable cause = e.getCause();
         if (cause instanceof RuntimeException r) {
            throw r;
         } else {
            throw new IllegalStateException(cause);
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new CancellationException();
      } catch (TimeoutException e) {
         throw new IllegalStateException("The client thread did not answer in time");
      }
   }

   private static String init(JsonObject args, String fallback) {
      return AiJson.getString(args, "action", fallback).trim().toLowerCase(Locale.ROOT);
   }

   private static String load(JsonObject args, String key) {
      String value = AiJson.getString(args, key, "").trim();
      if (value.isEmpty()) {
         throw new IllegalArgumentException(key + " is required");
      } else if (value.length() > 96) {
         throw new IllegalArgumentException(key + " is too long (96 characters at most)");
      } else {
         return value;
      }
   }

   static JsonObject profiles(JsonObject args) {
      String action = init(args, "list");
      return init(
         () -> {
            autismclient.util.AutismProfileManager m = autismclient.util.AutismProfileManager.init_bV();
            switch (action) {
               case "list":
                  return init(m);
               case "create":
                  String name = load(args, "name");
                  if (m.init_Z_Str_Str(name, null)) {
                     throw new IllegalArgumentException("A profile named '" + name + "' already exists");
                  } else {
                     AutismProfile px = m.init(
                        name,
                        AiJson.getBoolean(args, "autoSave", true),
                        AiJson.getBoolean(args, "ownMacroLibrary", false),
                        AiJson.getBoolean(args, "ownThemeColor", true)
                     );
                     if (px == null) {
                        throw new IllegalStateException("The profile could not be created");
                     }

                     if (AiJson.getBoolean(args, "switchTo", true)) {
                        m.load_V(px);
                     }

                     JsonObject out = init(m);
                     out.add("created", init(px, m));
                     return out;
                  }
               case "switch":
               case "load": {
                  AutismProfile p = init(m, args, "name");
                  m.load_V(p);
                  return init(m);
               }
               case "save": {
                  AutismProfile p = args.has("name") ? init(m, args, "name") : m.init_AutismProfile();
                  if (p == null) {
                     throw new IllegalStateException("No active profile");
                  }

                  m.init_V_AutismProfile(p);
                  JsonObject out = init(m);
                  out.add("saved", init(p, m));
                  return out;
               }
               case "rename": {
                  AutismProfile p = init(m, args, "name");
                  if (!m.init_Z_AutismProfile_Str(p, load(args, "newName"))) {
                     throw new IllegalArgumentException("Rename refused: blank or duplicate name");
                  }

                  return init(m);
               }
               case "duplicate": {
                  AutismProfile p = init(m, args, "name");
                  AutismProfile copy = m.init_AutismProfile_AutismProfile_Str(p, load(args, "newName"));
                  if (copy == null) {
                     throw new IllegalArgumentException("Duplicate refused: blank or duplicate name");
                  }

                  JsonObject out = init(m);
                  out.add("created", init(copy, m));
                  return out;
               }
               case "delete": {
                  AutismProfile p = init(m, args, "name");
                  if (!m.load_Z(p)) {
                     throw new IllegalArgumentException("The default profile and the last remaining profile cannot be deleted");
                  }

                  return init(m);
               }
               case "set": {
                  AutismProfile p = init(m, args, "name");
                  if (args.has("autoSave")) {
                     m.init_V_AutismProfile_Z(p, AiJson.getBoolean(args, "autoSave", p.autoSave));
                  }

                  if (args.has("ownMacroLibrary")) {
                     m.load_V_AutismProfile_Z(p, AiJson.getBoolean(args, "ownMacroLibrary", p.ownMacroLibrary));
                  }

                  if (args.has("ownThemeColor")) {
                     m.read(p, AiJson.getBoolean(args, "ownThemeColor", p.ownThemeColor));
                  }

                  return init(m);
               }
               default:
                  throw new IllegalArgumentException(
                     "Unknown profile action '" + action + "'; use list, create, switch, save, rename, duplicate, delete or set"
                  );
            }
         }
      );
   }

   private static AutismProfile init(autismclient.util.AutismProfileManager m, JsonObject args, String key) {
      String name = load(args, key);
      AutismProfile byId = m.init_AutismProfile_Str(AutismProfile.sanitizeId(name));
      if (byId != null) {
         return byId;
      }

      for (AutismProfile p : m.init_List()) {
         if (p.displayName.equalsIgnoreCase(name)) {
            return p;
         }
      }

      throw new IllegalArgumentException("No profile named '" + name + "'; list the profiles first");
   }

   private static JsonObject init(AutismProfile p, autismclient.util.AutismProfileManager m) {
      return AiJson.objectOf(
         "id",
         p.id,
         "name",
         p.displayName,
         "active",
         p.id.equals(m.init_Str()),
         "autoSave",
         p.autoSave,
         "ownMacroLibrary",
         p.ownMacroLibrary,
         "ownThemeColor",
         p.ownThemeColor,
         "serverPatterns",
         p.serverPatterns,
         "isDefault",
         autismclient.util.AutismProfileManager.init_Z_AutismProfile(p)
      );
   }

   private static JsonObject init(autismclient.util.AutismProfileManager m) {
      JsonArray list = new JsonArray();

      for (AutismProfile p : m.init_List()) {
         list.add(init(p, m));
      }

      return AiJson.objectOf("active", m.init_Str(), "unsavedChanges", m.init_Z(), "profiles", list);
   }

   static JsonObject load(JsonObject args) {
      String action = init(args, "get");
      return init(() -> {
         AutismConfig cfg = AutismConfig.getGlobal();
         AutismConfig.ThemeColors colors = cfg.themeColors;
         switch (action) {
            case "get":
               return init(colors);
            case "set":
               JsonObject wanted = args.has("colors") && args.get("colors").isJsonObject() ? args.getAsJsonObject("colors") : args;
               int changed = 0;
               boolean perChannel = false;

               for (String channel : init) {
                  if (wanted.has(channel) && !wanted.get(channel).isJsonNull()) {
                     init(colors, channel, init(wanted.get(channel).getAsString(), channel));
                     changed++;
                     if (!channel.equals("master")) {
                        perChannel = true;
                     }
                  }
               }

               if (changed == 0) {
                  throw new IllegalArgumentException("Give at least one channel in colors: " + init);
               } else {
                  if (args.has("advanced")) {
                     colors.advanced = AiJson.getBoolean(args, "advanced", colors.advanced);
                  } else if (perChannel) {
                     colors.advanced = true;
                  }

                  autismclient.util.ThemePaletteManager.reload();
                  cfg.save();
                  JsonObject out = init(colors);
                  out.addProperty("changed", changed);
                  return out;
               }
            case "reset":
               AutismConfig.ThemeColors stock = new AutismConfig.ThemeColors();

               for (String channel : init) {
                  init(colors, channel, init(stock, channel));
               }

               colors.advanced = false;
               autismclient.util.ThemePaletteManager.reload();
               cfg.save();
               return init(colors);
            default:
               throw new IllegalArgumentException("Unknown theme action '" + action + "'; use get, set or reset");
         }
      });
   }

   static int init(String raw, String channel) {
      String s = raw == null ? "" : raw.trim();
      if (s.startsWith("#")) {
         s = s.substring(1);
      }

      if (s.startsWith("0x") || s.startsWith("0X")) {
         s = s.substring(2);
      }

      if (!s.matches("[0-9a-fA-F]{6}|[0-9a-fA-F]{8}")) {
         throw new IllegalArgumentException(channel + " must be #RRGGBB or #AARRGGBB, got '" + raw + "'");
      }

      long value = Long.parseLong(s, 16);
      if (s.length() == 6) {
         value |= 4278190080L;
      }

      return (int)value;
   }

   static String init(int argb) {
      return String.format(Locale.ROOT, "#%08X", argb);
   }

   static int init(AutismConfig.ThemeColors c, String channel) {
      int var10000;
      switch (channel) {
         case "master":
            var10000 = c.master;
            break;
         case "accent":
            var10000 = c.accent;
            break;
         case "outline":
            var10000 = c.outline;
            break;
         case "text":
            var10000 = c.text;
            break;
         case "toggle":
            var10000 = c.toggle;
            break;
         case "backdrop":
            var10000 = c.backdrop;
            break;
         case "success":
            var10000 = c.success;
            break;
         case "danger":
            var10000 = c.danger;
            break;
         case "button":
            var10000 = c.button;
            break;
         case "header":
            var10000 = c.header;
            break;
         case "hover":
            var10000 = c.hover;
            break;
         default:
            throw new IllegalArgumentException("Unknown theme channel '" + channel + "'");
      }

      return var10000;
   }

   static void init(AutismConfig.ThemeColors c, String channel, int argb) {
      switch (channel) {
         case "master":
            c.master = argb;
            break;
         case "accent":
            c.accent = argb;
            break;
         case "outline":
            c.outline = argb;
            break;
         case "text":
            c.text = argb;
            break;
         case "toggle":
            c.toggle = argb;
            break;
         case "backdrop":
            c.backdrop = argb;
            break;
         case "success":
            c.success = argb;
            break;
         case "danger":
            c.danger = argb;
            break;
         case "button":
            c.button = argb;
            break;
         case "header":
            c.header = argb;
            break;
         case "hover":
            c.hover = argb;
            break;
         default:
            throw new IllegalArgumentException("Unknown theme channel '" + channel + "'");
      }
   }

   private static JsonObject init(AutismConfig.ThemeColors c) {
      JsonObject colors = new JsonObject();

      for (String channel : init) {
         colors.addProperty(channel, init(init(c, channel)));
      }

      return AiJson.objectOf("advanced", c.advanced, "customized", autismclient.util.ThemePaletteManager.isCustomActive(), "colors", colors);
   }

   static JsonObject read(JsonObject args) {
      String action = init(args, "list");
      return init(
         () -> {
            Minecraft mc = Minecraft.getInstance();
            switch (action) {
               case "list":
                  JsonArray elements = new JsonArray();

                  for (String id : autismclient.util.AutismHudManager.init_List()) {
                     elements.add(init(id, false));
                  }

                  return AiJson.objectOf("elements", elements, "revision", autismclient.util.AutismHudManager.load_I());
               case "get":
                  return init(init_Str(args), true);
               case "set": {
                  String id = init_Str(args);
                  AutismConfig.HudElementState state = autismclient.util.AutismHudManager.init_AutismConfig_HudEl_Str(id);
                  if (args.has("enabled")) {
                     autismclient.util.AutismHudManager.init_V_Str_Z(id, AiJson.getBoolean(args, "enabled", state.enabled));
                  }

                  boolean positioned = args.has("x") || args.has("y");
                  if (args.has("anchor")) {
                     String anchor = AiJson.getString(args, "anchor", "").trim().toUpperCase(Locale.ROOT);
                     if (!load.contains(anchor)) {
                        throw new IllegalArgumentException("anchor must be one of " + load);
                     }

                     state.anchor = anchor;
                     if (positioned) {
                        state.f_x_I = AiJson.getInt(args, "x", state.f_x_I);
                        state.f_y_I = AiJson.getInt(args, "y", state.f_y_I);
                     }
                  } else if (positioned) {
                     autismclient.util.AutismHudManager.init_V_Str(
                        id,
                        AiJson.getInt(args, "x", state.f_x_I),
                        AiJson.getInt(args, "y", state.f_y_I),
                        mc.getWindow().getGuiScaledWidth(),
                        mc.getWindow().getGuiScaledHeight()
                     );
                  }

                  if (args.has("scale")) {
                     state.scale = Math.max(0.5, Math.min(3.0, AiJson.getDouble(args, "scale", state.scale)));
                  }

                  if (args.has("settings") && args.get("settings").isJsonObject()) {
                     for (Entry<String, JsonElement> entry : args.getAsJsonObject("settings").entrySet()) {
                        String value = entry.getValue().isJsonPrimitive() ? entry.getValue().getAsString() : entry.getValue().toString();
                        autismclient.util.AutismHudManager.init_V_Str(id, entry.getKey(), value);
                     }
                  }

                  autismclient.util.AutismHudManager.init_V_Str_Z(id, autismclient.util.AutismHudManager.init_AutismConfig_HudEl_Str(id).enabled);
                  return init(id, true);
               }
               case "reset": {
                  String id = init_Str(args);
                  autismclient.util.AutismHudManager.load_V(id);
                  return init(id, true);
               }
               default:
                  throw new IllegalArgumentException("Unknown HUD action '" + action + "'; use list, get, set or reset");
            }
         }
      );
   }

   private static String init_Str(JsonObject args) {
      String id = load(args, "id");

      for (String known : autismclient.util.AutismHudManager.init_List()) {
         if (known.equalsIgnoreCase(id)) {
            return known;
         }
      }

      throw new IllegalArgumentException("Unknown HUD element '" + id + "'; list the elements first");
   }

   private static JsonObject init(String id, boolean withSettings) {
      AutismConfig.HudElementState s = autismclient.util.AutismHudManager.init_AutismConfig_HudEl_Str(id);
      JsonObject out = AiJson.objectOf(
         "id",
         id,
         "label",
         autismclient.util.AutismHudManager.init_Str_Str(id),
         "description",
         autismclient.util.AutismHudManager.load_Str(id),
         "enabled",
         s.enabled,
         "x",
         s.f_x_I,
         "y",
         s.f_y_I,
         "anchor",
         s.anchor,
         "scale",
         s.scale
      );
      if (withSettings) {
         out.add("settings", AiJson.GSON.toJsonTree(s.settings));
      }

      return out;
   }

   static JsonObject write(JsonObject args) {
      String action = init(args, "state");
      return init(() -> {
         ModuleRuntimeManager module = ModuleRuntimeManager.getInstance();
         autismclient.util.AutismSessionState shared = autismclient.util.AutismSessionState.getInstance();
         switch (action) {
            case "state":
               break;
            case "send":
               module.setAndSaveSendGuiPackets(AiJson.getBoolean(args, "value", true));
               break;
            case "delay":
               module.toggleDelayPackets(AiJson.getBoolean(args, "value", true));
               break;
            case "flush":
               return AiJson.objectOf("flushed", module.flushPacketQueue(), "queued", shared.hasDelayedPackets());
            case "clear":
               return AiJson.objectOf("cleared", module.clearPacketQueue(), "queued", shared.hasDelayedPackets());
            case "delayMode":
               String mode = AiJson.getString(args, "mode", "").trim().toUpperCase(Locale.ROOT);
               if (!mode.equals("TICKS") && !mode.equals("MS")) {
                  throw new IllegalArgumentException("mode must be TICKS or MS");
               }

               shared.setDelayUnit(autismclient.util.AutismSessionState.DelayUnit.valueOf(mode));
               break;
            default:
               throw new IllegalArgumentException("Unknown action '" + action + "'; use state, send, delay, flush, clear or delayMode");
         }

         return init(module, shared);
      });
   }

   private static JsonObject init(ModuleRuntimeManager module, autismclient.util.AutismSessionState shared) {
      return AiJson.objectOf(
         "send",
         shared.isSendPacketsEnabled(),
         "delay",
         shared.isDelayPacketsEnabled(),
         "queued",
         shared.hasDelayedPackets(),
         "delayMode",
         shared.getDelayUnit().name(),
         "flushOnDelayDisable",
         module.isFlushQueueOnDelayDisable()
      );
   }

   static JsonObject update(JsonObject args) {
      String action = init(args, "list");
      return init(
         () -> {
            ModuleRuntimeManager module = ModuleRuntimeManager.getInstance();
            switch (action) {
               case "list":
                  return init(module);
               case "block":
               case "unblock":
                  boolean c2s = init_Z(args);
                  Class<? extends Packet<?>> packet = autismclient.util.PacketTypeRegistry.resolvePacketClass(load(args, "packet"));
                  if (packet == null) {
                     throw new IllegalArgumentException(
                        "Unknown packet '" + AiJson.getString(args, "packet", "") + "'; use the friendly names from action list"
                     );
                  }

                  Set<Class<? extends Packet<?>>> set = new LinkedHashSet<>(c2s ? module.getCustomC2sPacketsCopy() : module.getCustomS2cPacketsCopy());
                  if (action.equals("block")) {
                     set.add(packet);
                  } else {
                     set.remove(packet);
                  }

                  if (c2s) {
                     module.setCustomC2sPackets(set);
                  } else {
                     module.setCustomS2cPackets(set);
                  }

                  return init(module);
               case "clear":
                  boolean c2sClear = init_Z(args);
                  if (c2sClear) {
                     module.setCustomC2sPackets(new LinkedHashSet<>());
                  } else {
                     module.setCustomS2cPackets(new LinkedHashSet<>());
                  }

                  return init(module);
               case "presets":
                  JsonArray presets = new JsonArray();

                  for (PacketPresetManager.Inner_DataUtil e : PacketPresetManager.getInstance().builtInPresets()) {
                     presets.add(init(e));
                  }

                  for (PacketPresetManager.Inner_DataUtil e : PacketPresetManager.getInstance().userPresets()) {
                     presets.add(init(e));
                  }

                  return AiJson.objectOf("presets", presets);
               case "loadPreset":
                  if (!PacketPresetManager.getInstance().loadPreset(load(args, "name"))) {
                     throw new IllegalArgumentException("Unknown preset; action presets lists them");
                  }

                  return init(module);
               case "savePreset":
                  String name = load(args, "name");
                  if (!PacketPresetManager.getInstance().savePreset(name)) {
                     throw new IllegalArgumentException("The preset name is reserved or invalid");
                  }

                  return AiJson.objectOf("saved", name);
               case "deletePreset":
                  if (!PacketPresetManager.getInstance().deletePreset(load(args, "name"))) {
                     throw new IllegalArgumentException("Unknown or built-in preset");
                  }

                  return AiJson.objectOf("deleted", AiJson.getString(args, "name", ""));
               default:
                  throw new IllegalArgumentException(
                     "Unknown action '" + action + "'; use list, block, unblock, clear, presets, loadPreset, savePreset or deletePreset"
                  );
            }
         }
      );
   }

   private static boolean init_Z(JsonObject args) {
      String d = AiJson.getString(args, "direction", "c2s").trim().toLowerCase(Locale.ROOT);
      if (d.equals("c2s")) {
         return true;
      } else if (d.equals("s2c")) {
         return false;
      } else {
         throw new IllegalArgumentException("direction must be c2s or s2c");
      }
   }

   private static JsonArray init(Set<Class<? extends Packet<?>>> packets) {
      JsonArray out = new JsonArray();
      List<String> sorted = new ArrayList<>();

      for (Class<? extends Packet<?>> p : packets) {
         sorted.add(autismclient.util.PacketTypeRegistry.getPacketName(p));
      }

      Collections.sort(sorted);
      sorted.forEach(out::add);
      return out;
   }

   private static JsonObject init(ModuleRuntimeManager module) {
      return AiJson.objectOf("blockedC2S", init(module.getCustomC2sPacketsCopy()), "blockedS2C", init(module.getCustomS2cPacketsCopy()));
   }

   private static JsonObject init(PacketPresetManager.Inner_DataUtil e) {
      return AiJson.objectOf("name", e.name(), "c2s", e.c2sCount(), "s2c", e.s2cCount(), "builtIn", e.isBuiltIn());
   }

   static JsonObject sync(JsonObject args) {
      String action = init(args, "list");
      return init(() -> {
         AutismWaypoints w = AutismWaypoints.init_AutismWaypoints();
         String scope = AuditWaypointScope.current(Minecraft.getInstance());
         switch (action) {
            case "list":
               break;
            case "add":
               BlockPos pos = init_BlockPos(args);
               int color = args.has("color") ? init(AiJson.getString(args, "color", ""), "color") : -8337153;
               w.init(scope, new AutismWaypoints.Waypoint(load(args, "name"), pos.getX(), pos.getY(), pos.getZ(), color, System.currentTimeMillis()));
               break;
            case "remove":
               if (!w.init_Z(scope, load(args, "name"))) {
                  throw new IllegalArgumentException("Unknown waypoint");
               }
               break;
            case "clear":
               w.init_V_Str(scope);
               break;
            case "rename":
               if (!w.init(scope, load(args, "name"), load(args, "newName"))) {
                  throw new IllegalArgumentException("Unknown waypoint or duplicate name");
               }
               break;
            case "color":
               if (!w.init_Z(scope, load(args, "name"), init(load(args, "color"), "color"))) {
                  throw new IllegalArgumentException("Unknown waypoint");
               }
               break;
            case "pruneDeaths":
               w.init(scope, AuditDeathWaypoints.retention(args));
               break;
            default:
               throw new IllegalArgumentException("Unknown action '" + action + "'; use list, add, remove, clear, rename, color or pruneDeaths");
         }

         JsonArray list = new JsonArray();

         for (AutismWaypoints.Waypoint wp : w.init_List_Str(scope)) {
            list.add(AiJson.objectOf("name", wp.name(), "x", wp.x(), "y", wp.y(), "z", wp.z(), "color", init(wp.color()), "death", wp.death()));
         }

         return AiJson.objectOf("scope", scope, "waypoints", list);
      });
   }

   private static BlockPos init_BlockPos(JsonObject args) {
      if (args.has("x") && args.has("y") && args.has("z")) {
         return new BlockPos(AiJson.getInt(args, "x", 0), AiJson.getInt(args, "y", 0), AiJson.getInt(args, "z", 0));
      } else {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null) {
            throw new IllegalStateException("Give x, y, z (no player to read a position from)");
         } else {
            return player.blockPosition();
         }
      }
   }

   static JsonObject close(JsonObject args) {
      String action = init(args, "list");
      return init(
         () -> {
            autismclient.util.ProxyListManager m = autismclient.util.ProxyListManager.getInstance();
            switch (action) {
               case "list":
                  break;
               case "add": {
                  autismclient.util.bW proxy = new autismclient.util.bW();
                  proxy.load_Str = AiJson.getString(args, "name", load(args, "address"));
                  proxy.init_bZ = AiJson.getString(args, "type", "Socks5").equalsIgnoreCase("Socks4")
                     ? autismclient.util.ProxyProtocolType.Socks4
                     : autismclient.util.ProxyProtocolType.Socks5;
                  proxy.read = load(args, "address");
                  proxy.init_I = AiJson.getInt(args, "port", 0);
                  if (proxy.init_I < 1 || proxy.init_I > 65535) {
                     throw new IllegalArgumentException("port must be 1-65535");
                  }

                  proxy.write = AiJson.getString(args, "username", "");
                  proxy.update = AiJson.getString(args, "password", "");
                  if (!m.addProxy(proxy)) {
                     throw new IllegalStateException("The proxy could not be added");
                  }
                  break;
               }
               case "enable":
               case "disable": {
                  autismclient.util.bW proxy = init(m, args);
                  m.setProxyEnabled(proxy, action.equals("enable"));
                  break;
               }
               case "remove":
                  m.removeProxy(init(m, args));
                  break;
               default:
                  throw new IllegalArgumentException("Unknown action '" + action + "'; use list, add, enable, disable or remove");
            }

            JsonArray list = new JsonArray();
            autismclient.util.bW active = m.getActiveProxy();

            for (autismclient.util.bW p : m) {
               list.add(
                  AiJson.objectOf(
                     "id",
                     p.init_Str,
                     "name",
                     p.load_Str,
                     "type",
                     p.init_bZ.name(),
                     "address",
                     p.read,
                     "port",
                     p.init_I,
                     "active",
                     active != null && active.init_Str.equals(p.init_Str),
                     "hasAuth",
                     p.write != null && !p.write.isEmpty(),
                     "status",
                     p.init_bW_CachePool == null ? "" : p.init_bW_CachePool.name()
                  )
               );
            }

            return AiJson.objectOf("proxies", list);
         }
      );
   }

   private static autismclient.util.bW init(autismclient.util.ProxyListManager m, JsonObject args) {
      String key = load(args, "proxy");
      autismclient.util.bW byId = m.findById(key);
      if (byId != null) {
         return byId;
      }

      for (autismclient.util.bW p : m) {
         if (key.equalsIgnoreCase(p.load_Str) || key.equalsIgnoreCase(p.read + ":" + p.init_I)) {
            return p;
         }
      }

      throw new IllegalArgumentException("No proxy '" + key + "'; list proxies first");
   }

   static JsonObject open(JsonObject args) {
      String action = init(args, "get");
      return init(
         () -> {
            if (action.equals("set")) {
               if (args.has("enabled")) {
                  autismclient.util.JoinMacroController.init(AiJson.getBoolean(args, "enabled", false));
               }

               if (args.has("macro")) {
                  String name = AiJson.getString(args, "macro", "").trim();
                  if (!name.isEmpty() && autismclient.util.MacroLibraryManager.getInstance().findByName(name) == null) {
                     throw new IllegalArgumentException("Unknown macro '" + name + "'");
                  }

                  autismclient.util.JoinMacroController.init_V(name);
               }

               if (args.has("timing")) {
                  autismclient.util.JoinMacroController.init(autismclient.util.JoinMacroController.Inner_StreamUtil.init(AiJson.getString(args, "timing", "PLAYABLE")));
               }

               if (args.has("keepEnabled")) {
                  autismclient.util.JoinMacroController.load(AiJson.getBoolean(args, "keepEnabled", false));
               }
            } else if (!action.equals("get")) {
               throw new IllegalArgumentException("Unknown action '" + action + "'; use get or set");
            }

            return AiJson.objectOf(
               "enabled",
               autismclient.util.JoinMacroController.load_Z(),
               "macro",
               autismclient.util.JoinMacroController.init_Str(),
               "timing",
               autismclient.util.JoinMacroController.init_aA_StreamUtil().init_Str(),
               "keepEnabled",
               autismclient.util.JoinMacroController.init_Z()
            );
         }
      );
   }

   static JsonObject poll(JsonObject args) {
      return init(() -> {
         Minecraft mc = Minecraft.getInstance();
         ClientPacketListener connection = mc.getConnection();
         if (connection == null) {
            return AiJson.objectOf("connected", false);
         }

         autismclient.util.AutismSessionState shared = autismclient.util.AutismSessionState.getInstance();
         ServerData server = mc.getCurrentServer();
         JsonObject out = AiJson.objectOf("connected", true, "brand", connection.serverBrand() == null ? "" : connection.serverBrand());
         if (server != null) {
            out.addProperty("address", server.ip);
            out.addProperty("ping", server.ping);
            out.addProperty("protocol", server.protocol);
            out.addProperty("reportedVersion", server.version == null ? "" : server.version.getString());
            String real = shared.getDetectedServerVersion(server.ip);
            if (real != null && !real.isBlank()) {
               out.addProperty("realVersion", real);
            }
         }

         double tps = shared.getEstimatedTps();
         if (tps > 0.0) {
            out.addProperty("estimatedTps", Math.round(tps * 10.0) / 10.0);
         }

         if (mc.level != null) {
            out.addProperty("difficulty", mc.level.getDifficulty().name());
         }

         return out;
      });
   }

   static JsonObject pop(JsonObject args) {
      String action = init(args, "list");
      return init(() -> {
         autismclient.util.MacroLibraryManager m = autismclient.util.MacroLibraryManager.getInstance();
         switch (action) {
            case "list":
               return init(m);
            case "create":
               if (!m.createFolder(load(args, "name"))) {
                  throw new IllegalArgumentException("Folder exists already or the name is invalid");
               }
               break;
            case "rename":
               if (!m.renameFolder(load(args, "name"), load(args, "newName"))) {
                  throw new IllegalArgumentException("Rename refused: unknown folder or duplicate name");
               }
               break;
            case "delete":
               if (!m.deleteFolder(load(args, "name"))) {
                  throw new IllegalArgumentException("Unknown folder");
               }
               break;
            case "move":
               autismclient.util.AutismMacro macro = m.findByName(load(args, "macro"));
               if (macro == null) {
                  throw new IllegalArgumentException("Unknown macro; read client:macros first");
               }

               String folder = AiJson.getString(args, "folder", "").trim();
               if (!folder.isEmpty() && m.getFolders().stream().noneMatch(f -> f.equalsIgnoreCase(folder))) {
                  m.createFolder(folder);
               }

               if (!m.moveMacroToFolder(macro, folder)) {
                  throw new IllegalArgumentException("The macro could not be moved to '" + folder + "'");
               }
               break;
            default:
               throw new IllegalArgumentException("Unknown folder action '" + action + "'; use list, create, rename, delete or move");
         }

         JsonObject out = init(m);
         out.addProperty("action", action);
         return out;
      });
   }

   static JsonObject tick(JsonObject args) {
      String action = init(args, "running");
      return init(() -> {
         autismclient.util.MacroLibraryManager m = autismclient.util.MacroLibraryManager.getInstance();
         switch (action) {
            case "running":
               return AiJson.objectOf("running", MacroExecutor.isRunning());
            case "delete": {
               autismclient.util.AutismMacro macro = init(m, args);
               m.deleteMacro(macro);
               return AiJson.objectOf("deleted", macro.name, "macros", m.getMacros().size());
            }
            case "rename": {
               autismclient.util.AutismMacro macro = init(m, args);
               String next = load(args, "newName");
               if (m.findByName(next) != null) {
                  throw new IllegalArgumentException("A macro named '" + next + "' already exists");
               }

               m.deleteMacro(macro);
               macro.name = next;
               m.saveMacro(macro);
               return AiJson.objectOf("renamed", next, "folder", macro.folder);
            }
            case "duplicate": {
               autismclient.util.AutismMacro macro = init(m, args);
               autismclient.util.AutismMacro copy = m.importMacro(macro, AiJson.getString(args, "newName", macro.name + " copy").trim());
               if (copy == null) {
                  throw new IllegalStateException("The macro could not be duplicated");
               }

               return AiJson.objectOf("created", copy.name, "folder", copy.folder);
            }
            case "export": {
               autismclient.util.AutismMacro macro = init(m, args);
               return AiJson.objectOf("name", macro.name, "share", autismclient.util.MacroClipboardCodec.encodeMacro(macro));
            }
            case "stop":
               String name = AiJson.getString(args, "name", "").trim();
               if (name.isEmpty()) {
                  MacroExecutor.stopAllFromAi();
               } else {
                  MacroExecutor.stopMacroByName(name);
               }

               return AiJson.objectOf("stopped", name.isEmpty() ? "all" : name, "running", MacroExecutor.isRunning());
            default:
               throw new IllegalArgumentException("Unknown macro action '" + action + "'; use running, delete, rename, duplicate, export or stop");
         }
      });
   }

   private static autismclient.util.AutismMacro init(autismclient.util.MacroLibraryManager m, JsonObject args) {
      autismclient.util.AutismMacro macro = m.findByName(load(args, "name"));
      if (macro == null) {
         throw new IllegalArgumentException("Unknown macro; read client:macros first");
      } else {
         return macro;
      }
   }

   private static JsonObject init(autismclient.util.MacroLibraryManager m) {
      Map<String, Integer> counts = new LinkedHashMap<>();

      for (String folder : m.getFolders()) {
         counts.put(folder, 0);
      }

      int unfiled = 0;

      for (autismclient.util.AutismMacro macro : m.getMacros()) {
         String folder = macro.folder == null ? "" : macro.folder;
         if (folder.isEmpty()) {
            unfiled++;
         } else {
            counts.merge(folder, 1, Integer::sum);
         }
      }

      JsonArray folders = new JsonArray();

      for (Entry<String, Integer> entry : counts.entrySet()) {
         folders.add(AiJson.objectOf("name", entry.getKey(), "macros", entry.getValue()));
      }

      return AiJson.objectOf("folders", folders, "unfiledMacros", unfiled);
   }
}
