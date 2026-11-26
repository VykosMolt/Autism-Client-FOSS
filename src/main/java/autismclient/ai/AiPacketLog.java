package autismclient.ai;

import autismclient.modules.ModuleRuntimeManager;
import autismclient.util.AutismPacketLoggerOverlay;
import autismclient.util.PacketWireCaptureStore;
import autismclient.util.PacketSchemaSnapshot;
import autismclient.util.PacketInspectorEngine;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.configuration.ConfigurationPacketTypes;
import net.minecraft.network.protocol.cookie.CookiePacketTypes;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.handshake.HandshakePacketTypes;
import net.minecraft.network.protocol.login.LoginPacketTypes;
import net.minecraft.network.protocol.ping.PingPacketTypes;
import net.minecraft.network.protocol.status.StatusPacketTypes;

public final class AiPacketLog {
   static final int init_I = 50;
   static final int load = 10;
   static final int read = 30;
   static final int write = 64;
   private static final int update = 2048;
   private static final int sync = 2048;
   private static final int flush = 4096;
   private static final int close = 64;
   private static final int open = 100000;
   private static final Class<?>[] init_ClassArr = new Class[]{
      HandshakePacketTypes.class,
      StatusPacketTypes.class,
      LoginPacketTypes.class,
      ConfigurationPacketTypes.class,
      CommonPacketTypes.class,
      CookiePacketTypes.class,
      PingPacketTypes.class,
      GamePacketTypes.class
   };
   private static volatile Map<Class<?>, PacketType<?>> init_Map;

   private AiPacketLog() {
   }

   public static JsonObject init(JsonObject args) {
      AutismPacketLoggerOverlay overlay = ModuleRuntimeManager.init_PoolEntry().init_AutismPacketLogger();
      if (overlay == null) {
         throw new IllegalStateException("Packet logger overlay is unavailable");
      }

      String action = AiJson.init_Str_JsonObject(args, "action", "state").trim().toLowerCase(Locale.ROOT);
      switch (action) {
         case "start":
            overlay.read(false);
            autismclient.util.PacketCaptureState.write();
            break;
         case "stop":
            overlay.read(true);
            break;
         case "clear":
            overlay.sync_V_14();
         case "state":
            break;
         default:
            throw new IllegalArgumentException("Unknown packet_log action '" + action + "' (start|stop|clear|state; use client:packet_log read for entries)");
      }

      if (args.has("filters") && args.get("filters").isJsonObject()) {
         init(overlay, args.getAsJsonObject("filters"));
      }

      return overlay.init_JsonObject_2();
   }

   private static void init(AutismPacketLoggerOverlay overlay, JsonObject filters) {
      String search = filters.has("search") ? AiJson.init_Str_JsonObject(filters, "search", "") : null;
      String direction = filters.has("direction") ? init(AiJson.init_Str_JsonObject(filters, "direction", "")) : null;
      String category = filters.has("category") ? load(AiJson.init_Str_JsonObject(filters, "category", "")) : null;
      if (search != null || direction != null || category != null) {
         overlay.init_V_Str_5(search, direction, category);
      }

      if (filters.has("block") && filters.get("block").isJsonArray()) {
         int applied = 0;

         for (JsonElement name : filters.getAsJsonArray("block")) {
            if (++applied > 64) {
               throw new IllegalArgumentException("block accepts at most 64 names per call");
            }

            overlay.load(name.getAsString());
         }
      }

      if (filters.has("unblock") && filters.get("unblock").isJsonArray()) {
         int applied = 0;

         for (JsonElement name : filters.getAsJsonArray("unblock")) {
            if (++applied > 64) {
               throw new IllegalArgumentException("unblock accepts at most 64 names per call");
            }

            overlay.init_V_Str_9(name.getAsString());
         }
      }
   }

   public static JsonObject load(JsonObject args) {
      AutismPacketLoggerOverlay overlay = ModuleRuntimeManager.init_PoolEntry().init_AutismPacketLogger();
      if (overlay == null) {
         throw new IllegalStateException("Packet logger overlay is unavailable");
      }

      String category = args.has("category") ? load(AiJson.init_Str_JsonObject(args, "category", "ALL")) : "ALL";
      boolean detail = AiJson.init_Z_JsonObject(args, "detail", false);
      int limit = init(AiJson.init_I_JsonObject(args, "limit", 20), detail ? 10 : 50);
      int offset = init(AiJson.init_I_JsonObject(args, "offset", 0));
      String query = AiJson.init_Str_JsonObject(args, "query", "").toLowerCase(Locale.ROOT);
      List<AutismPacketLoggerOverlay.LoggedPacketEntry> buffered = overlay.init_List_Str_3(category, 0, -1);
      List<AutismPacketLoggerOverlay.LoggedPacketEntry> matched = new ArrayList<>();

      for (AutismPacketLoggerOverlay.LoggedPacketEntry entry : buffered) {
         if (query.isEmpty() || entry.load_Str != null && entry.load_Str.toLowerCase(Locale.ROOT).contains(query)) {
            matched.add(entry);
         }
      }

      JsonArray entries = new JsonArray();
      int end = Math.min(matched.size(), offset + limit);
      int budget = 100000;
      boolean truncated = false;

      for (int i = offset; i < end; i++) {
         JsonObject described = init(matched.get(i), i, detail);
         int cost = described.toString().length() + 1;
         if (entries.size() > 0 && cost > budget) {
            truncated = true;
            break;
         }

         budget -= cost;
         entries.add(described);
      }

      return AiJson.init_JsonObject_ObjArr(
         "category",
         category,
         "offset",
         offset,
         "limit",
         limit,
         "buffered",
         buffered.size(),
         "matched",
         matched.size(),
         "returned",
         entries.size(),
         "truncated",
         truncated,
         "capture",
         overlay.init_JsonObject_2(),
         "entries",
         entries
      );
   }

   private static JsonObject init(AutismPacketLoggerOverlay.LoggedPacketEntry entry, int index, boolean detail) {
      JsonObject out = AiJson.init_JsonObject_ObjArr(
         "index",
         index,
         "timestampMs",
         entry.load_J,
         "gameTick",
         entry.init_I,
         "direction",
         entry.init_Str,
         "shortName",
         entry.load_Str,
         "packetClass",
         entry.init_Class == null ? "" : entry.init_Class.getName(),
         "groupKey",
         entry.read_Str,
         "inventory",
         entry.init_Z,
         "movement",
         entry.load_Z,
         "payload",
         entry.read_Z
      );
      if (entry.write != null) {
         out.addProperty("blockState", entry.write);
      }

      if (entry.update != null) {
         out.addProperty("screen", entry.update);
      }

      if (entry.init_AutismPayloadSuppo != null) {
         out.addProperty("payloadChannel", entry.init_AutismPayloadSuppo.load_Str());
         out.addProperty("payloadSize", entry.init_AutismPayloadSuppo.init_I());
      }

      if (detail) {
         init(out, entry);
      }

      return out;
   }

   private static void init(JsonObject out, AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (entry.init_Class != null) {
         autismclient.util.PacketSchemaRegistry.Inner_StreamUtil schema = autismclient.util.PacketSchemaRegistry.init(entry.init_Class);
         if (schema != null) {
            JsonArray fields = new JsonArray();

            for (autismclient.util.PacketSchemaRegistry.Inner_DataUtil field : schema.init_List()) {
               fields.add(
                  AiJson.init_JsonObject_ObjArr("name", field.init_Str(), "javaType", field.load(), "valueKind", field.read(), "editableCandidate", field.init_Z())
               );
               if (fields.toString().length() > 2048) {
                  break;
               }
            }

            out.add(
               "schema",
               AiJson.init_JsonObject_ObjArr(
                  "complete",
                  schema.init_Z(),
                  "direction",
                  schema.write(),
                  "codecStyle",
                  schema.update(),
                  "packetType",
                  schema.sync(),
                  "inheritedFallback",
                  schema.load_Z(),
                  "fields",
                  fields
               )
            );
         }
      }

      if (entry.init_Packet != null) {
         PacketSchemaSnapshot view = PacketSchemaSnapshot.init_br_Packet(entry.init_Packet);
         JsonArray decoded = new JsonArray();

         for (autismclient.util.PacketFieldSnapshot field : view.init_List()) {
            decoded.add(
               AiJson.init_JsonObject_ObjArr(
                  "name", field.init_Str(), "value", init(field.write(), 220), "readable", field.init_Z(), "editableCandidate", field.load_Z()
               )
            );
            if (decoded.toString().length() > 2048) {
               break;
            }
         }

         out.add("decoded", AiJson.init_JsonObject_ObjArr("status", view.init_Str(), "complete", view.read(), "fields", decoded));
         PacketWireCaptureStore.Inner_StreamUtil snapshot = PacketWireCaptureStore.init(entry.init_Packet);
         if (snapshot != null) {
            byte[] plaintext = snapshot.init_BArr();
            out.add(
               "wire",
               AiJson.init_JsonObject_ObjArr(
                  "numericPacketId",
                  snapshot.init_I(),
                  "protocolPhase",
                  snapshot.load(),
                  "plaintextLength",
                  plaintext.length,
                  "hexPrefix",
                  PacketWireCaptureStore.init(plaintext, 64)
               )
            );
         }
      }

      if (entry.init_Class != null) {
         out.addProperty("inspector", init(PacketInspectorEngine.init_bu_JsonUtil_AutismPacketLogger(entry).load(), 4096));
      }
   }

   public static JsonObject read(JsonObject args) {
      String query = AiJson.init_Str_JsonObject(args, "query", "").toLowerCase(Locale.ROOT);
      String direction = args.has("direction") ? init(AiJson.init_Str_JsonObject(args, "direction", "Both")) : "Both";
      int offset = init(AiJson.init_I_JsonObject(args, "offset", 0));
      int limit = init(AiJson.init_I_JsonObject(args, "limit", 10), 30);
      Collection<Class<? extends Packet<?>>> source = "C2S".equals(direction)
         ? autismclient.util.PacketTypeRegistry.load()
         : ("S2C".equals(direction) ? autismclient.util.PacketTypeRegistry.init_Set() : autismclient.util.PacketTypeRegistry.init_Set);
      List<Class<? extends Packet<?>>> matched = source.stream()
         .filter(c -> c.getSimpleName().toLowerCase(Locale.ROOT).contains(query))
         .sorted(Comparator.comparing(Class::getName))
         .toList();
      JsonArray packets = new JsonArray();
      int end = Math.min(matched.size(), offset + limit);

      for (int i = offset; i < end; i++) {
         Class<? extends Packet<?>> packetClass = matched.get(i);
         String flow = init(packetClass);
         autismclient.util.PacketSchemaRegistry.Inner_StreamUtil schema = autismclient.util.PacketSchemaRegistry.init(packetClass);
         JsonObject entry = AiJson.init_JsonObject_ObjArr("name", autismclient.util.PacketTypeRegistry.init_Str_Class(packetClass), "schema", schema);
         entry.addProperty("direction", flow);
         entry.addProperty("numericPacketId", init(packetClass, "BOTH".equals(flow) ? "C2S" : flow));
         if (schema != null) {
            JsonArray editable = new JsonArray();

            for (autismclient.util.PacketSchemaRegistry.Inner_DataUtil field : schema.init_List()) {
               if (field.init_Z()) {
                  editable.add(field.init_Str());
               }
            }

            entry.add("editableFields", editable);
         }

         packets.add(entry);
      }

      return AiJson.init_JsonObject_ObjArr("packets", packets, "matched", matched.size(), "offset", offset, "limit", limit, "direction", direction);
   }

   static String init(Class<? extends Packet<?>> packetClass) {
      boolean c2s = autismclient.util.PacketTypeRegistry.load().contains(packetClass);
      boolean s2c = autismclient.util.PacketTypeRegistry.init_Set().contains(packetClass);
      return c2s && s2c ? "BOTH" : (c2s ? "C2S" : (s2c ? "S2C" : "ANY"));
   }

   static int init(Class<?> packetClass, String direction) {
      return init(init().get(packetClass), direction);
   }

   static int init(PacketType<?> type, String direction) {
      if (type == null) {
         return -1;
      }

      try {
         int id = PacketWireCaptureStore.init("PLAY", direction, type);
         if (id < 0) {
            id = PacketWireCaptureStore.init("CONFIGURATION", direction, type);
         }

         return id;
      } catch (Throwable t) {
         return -1;
      }
   }

   private static Map<Class<?>, PacketType<?>> init() {
      Map<Class<?>, PacketType<?>> current = init_Map;
      if (current != null) {
         return current;
      }

      synchronized (AiPacketLog.class) {
         current = init_Map;
         if (current != null) {
            return current;
         }

         Map<Class<?>, PacketType<?>> built = new HashMap<>();

         for (Class<?> holder : init_ClassArr) {
            for (Field field : holder.getDeclaredFields()) {
               int modifiers = field.getModifiers();
               if (Modifier.isStatic(modifiers)
                  && PacketType.class.isAssignableFrom(field.getType())
                  && field.getGenericType() instanceof ParameterizedType parameterized) {
                  Type[] typeArgs = parameterized.getActualTypeArguments();
                  if (typeArgs.length == 1 && typeArgs[0] instanceof Class<?> packetClass && Packet.class.isAssignableFrom(packetClass)) {
                     try {
                        field.setAccessible(true);
                        if (field.get(null) instanceof PacketType<?> type) {
                           built.putIfAbsent(packetClass, type);
                        }
                     } catch (Throwable var18) {
                     }
                  }
               }
            }
         }

         init_Map = Map.copyOf(built);
         return init_Map;
      }
   }

   static String init(String raw) {
      if (raw == null) {
         return null;
      }

      String var10000;
      switch (raw.trim().toUpperCase(Locale.ROOT)) {
         case "C2S":
         case "SERVERBOUND":
         case "OUT":
            var10000 = "C2S";
            break;
         case "S2C":
         case "CLIENTBOUND":
         case "IN":
            var10000 = "S2C";
            break;
         case "BOTH":
         case "ANY":
         case "ALL":
         case "":
            var10000 = "Both";
            break;
         default:
            throw new IllegalArgumentException("direction must be Both, C2S, or S2C (got '" + raw + "')");
      }

      return var10000;
   }

   static String load(String raw) {
      if (raw == null) {
         return null;
      }

      String var10000;
      switch (raw.trim().toUpperCase(Locale.ROOT)) {
         case "ALL":
         case "ANY":
         case "":
            var10000 = "ALL";
            break;
         case "INVENTORY":
         case "INV":
            var10000 = "INVENTORY";
            break;
         case "MOVEMENT":
         case "MOVE":
            var10000 = "MOVEMENT";
            break;
         case "PAYLOAD":
            var10000 = "PAYLOAD";
            break;
         default:
            throw new IllegalArgumentException("category must be ALL, INVENTORY, MOVEMENT, or PAYLOAD (got '" + raw + "')");
      }

      return var10000;
   }

   static int init(int requested, int max) {
      return Math.max(1, Math.min(requested, max));
   }

   static int init(int offset) {
      return Math.max(0, offset);
   }

   private static String init(String value, int max) {
      if (value == null) {
         return "";
      } else {
         return value.length() <= max ? value : value.substring(0, max);
      }
   }
}
