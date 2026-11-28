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
   private static final Class<?>[] packetTypeHolders = new Class[]{
      HandshakePacketTypes.class,
      StatusPacketTypes.class,
      LoginPacketTypes.class,
      ConfigurationPacketTypes.class,
      CommonPacketTypes.class,
      CookiePacketTypes.class,
      PingPacketTypes.class,
      GamePacketTypes.class
   };
   private static volatile Map<Class<?>, PacketType<?>> packetTypeCache;

   private AiPacketLog() {
   }

   public static JsonObject controlCapture(JsonObject args) {
      AutismPacketLoggerOverlay overlay = ModuleRuntimeManager.getInstance().getOrCreatePacketLoggerOverlay();
      if (overlay == null) {
         throw new IllegalStateException("Packet logger overlay is unavailable");
      }

      String action = AiJson.getString(args, "action", "state").trim().toLowerCase(Locale.ROOT);
      switch (action) {
         case "start":
            overlay.setPaused(false);
            autismclient.util.PacketCaptureState.write();
            break;
         case "stop":
            overlay.setPaused(true);
            break;
         case "clear":
            overlay.clearEntries();
         case "state":
            break;
         default:
            throw new IllegalArgumentException("Unknown packet_log action '" + action + "' (start|stop|clear|state; use client:packet_log read for entries)");
      }

      if (args.has("filters") && args.get("filters").isJsonObject()) {
         applyFilters(overlay, args.getAsJsonObject("filters"));
      }

      return overlay.stateJson();
   }

   private static void applyFilters(AutismPacketLoggerOverlay overlay, JsonObject filters) {
      String search = filters.has("search") ? AiJson.getString(filters, "search", "") : null;
      String direction = filters.has("direction") ? normalizeDirection(AiJson.getString(filters, "direction", "")) : null;
      String category = filters.has("category") ? normalizeCategory(AiJson.getString(filters, "category", "")) : null;
      if (search != null || direction != null || category != null) {
         overlay.applyFilters(search, direction, category);
      }

      if (filters.has("block") && filters.get("block").isJsonArray()) {
         int applied = 0;

         for (JsonElement name : filters.getAsJsonArray("block")) {
            if (++applied > 64) {
               throw new IllegalArgumentException("block accepts at most 64 names per call");
            }

            overlay.blockPacket(name.getAsString());
         }
      }

      if (filters.has("unblock") && filters.get("unblock").isJsonArray()) {
         int applied = 0;

         for (JsonElement name : filters.getAsJsonArray("unblock")) {
            if (++applied > 64) {
               throw new IllegalArgumentException("unblock accepts at most 64 names per call");
            }

            overlay.unblock(name.getAsString());
         }
      }
   }

   public static JsonObject readLog(JsonObject args) {
      AutismPacketLoggerOverlay overlay = ModuleRuntimeManager.getInstance().getOrCreatePacketLoggerOverlay();
      if (overlay == null) {
         throw new IllegalStateException("Packet logger overlay is unavailable");
      }

      String category = args.has("category") ? normalizeCategory(AiJson.getString(args, "category", "ALL")) : "ALL";
      boolean detail = AiJson.getBoolean(args, "detail", false);
      int limit = clampLimit(AiJson.getInt(args, "limit", 20), detail ? 10 : 50);
      int offset = clampOffset(AiJson.getInt(args, "offset", 0));
      String query = AiJson.getString(args, "query", "").toLowerCase(Locale.ROOT);
      List<AutismPacketLoggerOverlay.LoggedPacketEntry> buffered = overlay.queryEntries(category, 0, -1);
      List<AutismPacketLoggerOverlay.LoggedPacketEntry> matched = new ArrayList<>();

      for (AutismPacketLoggerOverlay.LoggedPacketEntry entry : buffered) {
         if (query.isEmpty() || entry.name != null && entry.name.toLowerCase(Locale.ROOT).contains(query)) {
            matched.add(entry);
         }
      }

      JsonArray entries = new JsonArray();
      int end = Math.min(matched.size(), offset + limit);
      int budget = 100000;
      boolean truncated = false;

      for (int i = offset; i < end; i++) {
         JsonObject described = entryJson(matched.get(i), i, detail);
         int cost = described.toString().length() + 1;
         if (entries.size() > 0 && cost > budget) {
            truncated = true;
            break;
         }

         budget -= cost;
         entries.add(described);
      }

      return AiJson.objectOf(
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
         overlay.stateJson(),
         "entries",
         entries
      );
   }

   private static JsonObject entryJson(AutismPacketLoggerOverlay.LoggedPacketEntry entry, int index, boolean detail) {
      JsonObject out = AiJson.objectOf(
         "index",
         index,
         "timestampMs",
         entry.timestampMs,
         "gameTick",
         entry.tick,
         "direction",
         entry.direction,
         "shortName",
         entry.name,
         "packetClass",
         entry.packetClass == null ? "" : entry.packetClass.getName(),
         "groupKey",
         entry.key,
         "inventory",
         entry.inventoryPacket,
         "movement",
         entry.movementPacket,
         "payload",
         entry.payloadPacket
      );
      if (entry.capturedBlockState != null) {
         out.addProperty("blockState", entry.capturedBlockState);
      }

      if (entry.capturedScreen != null) {
         out.addProperty("screen", entry.capturedScreen);
      }

      if (entry.payloadSnapshot != null) {
         out.addProperty("payloadChannel", entry.payloadSnapshot.channel());
         out.addProperty("payloadSize", entry.payloadSnapshot.byteLength());
      }

      if (detail) {
         appendPacketDetail(out, entry);
      }

      return out;
   }

   private static void appendPacketDetail(JsonObject out, AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (entry.packetClass != null) {
         autismclient.util.PacketSchemaRegistry.Inner_StreamUtil schema = autismclient.util.PacketSchemaRegistry.schemaFor(entry.packetClass);
         if (schema != null) {
            JsonArray fields = new JsonArray();

            for (autismclient.util.PacketSchemaRegistry.Inner_DataUtil field : schema.fields()) {
               fields.add(
                  AiJson.objectOf("name", field.name(), "javaType", field.load(), "valueKind", field.read(), "editableCandidate", field.editableCandidate())
               );
               if (fields.toString().length() > 2048) {
                  break;
               }
            }

            out.add(
               "schema",
               AiJson.objectOf(
                  "complete",
                  schema.complete(),
                  "direction",
                  schema.write(),
                  "codecStyle",
                  schema.update(),
                  "packetType",
                  schema.sync(),
                  "inheritedFallback",
                  schema.inherited(),
                  "fields",
                  fields
               )
            );
         }
      }

      if (entry.packet != null) {
         PacketSchemaSnapshot view = PacketSchemaSnapshot.capture(entry.packet);
         JsonArray decoded = new JsonArray();

         for (autismclient.util.PacketFieldSnapshot field : view.fields()) {
            decoded.add(
               AiJson.objectOf(
                  "name", field.name(), "value", truncate(field.write(), 220), "readable", field.readable(), "editableCandidate", field.editableCandidate()
               )
            );
            if (decoded.toString().length() > 2048) {
               break;
            }
         }

         out.add("decoded", AiJson.objectOf("status", view.status(), "complete", view.read(), "fields", decoded));
         PacketWireCaptureStore.PacketCapture snapshot = PacketWireCaptureStore.captureFor(entry.packet);
         if (snapshot != null) {
            byte[] plaintext = snapshot.plaintext();
            out.add(
               "wire",
               AiJson.objectOf(
                  "numericPacketId",
                  snapshot.packetId(),
                  "protocolPhase",
                  snapshot.load(),
                  "plaintextLength",
                  plaintext.length,
                  "hexPrefix",
                  PacketWireCaptureStore.hexPreview(plaintext, 64)
               )
            );
         }
      }

      if (entry.packetClass != null) {
         out.addProperty("inspector", truncate(PacketInspectorEngine.inspect(entry).copyText(), 4096));
      }
   }

   public static JsonObject readSchemas(JsonObject args) {
      String query = AiJson.getString(args, "query", "").toLowerCase(Locale.ROOT);
      String direction = args.has("direction") ? normalizeDirection(AiJson.getString(args, "direction", "Both")) : "Both";
      int offset = clampOffset(AiJson.getInt(args, "offset", 0));
      int limit = clampLimit(AiJson.getInt(args, "limit", 10), 30);
      Collection<Class<? extends Packet<?>>> source = "C2S".equals(direction)
         ? autismclient.util.PacketTypeRegistry.getC2sPackets()
         : ("S2C".equals(direction) ? autismclient.util.PacketTypeRegistry.getS2cPackets() : autismclient.util.PacketTypeRegistry.ALL_PACKETS);
      List<Class<? extends Packet<?>>> matched = source.stream()
         .filter(c -> c.getSimpleName().toLowerCase(Locale.ROOT).contains(query))
         .sorted(Comparator.comparing(Class::getName))
         .toList();
      JsonArray packets = new JsonArray();
      int end = Math.min(matched.size(), offset + limit);

      for (int i = offset; i < end; i++) {
         Class<? extends Packet<?>> packetClass = matched.get(i);
         String flow = directionOf(packetClass);
         autismclient.util.PacketSchemaRegistry.Inner_StreamUtil schema = autismclient.util.PacketSchemaRegistry.schemaFor(packetClass);
         JsonObject entry = AiJson.objectOf("name", autismclient.util.PacketTypeRegistry.getPacketName(packetClass), "schema", schema);
         entry.addProperty("direction", flow);
         entry.addProperty("numericPacketId", packetIdFor(packetClass, "BOTH".equals(flow) ? "C2S" : flow));
         if (schema != null) {
            JsonArray editable = new JsonArray();

            for (autismclient.util.PacketSchemaRegistry.Inner_DataUtil field : schema.fields()) {
               if (field.editableCandidate()) {
                  editable.add(field.name());
               }
            }

            entry.add("editableFields", editable);
         }

         packets.add(entry);
      }

      return AiJson.objectOf("packets", packets, "matched", matched.size(), "offset", offset, "limit", limit, "direction", direction);
   }

   static String directionOf(Class<? extends Packet<?>> packetClass) {
      boolean c2s = autismclient.util.PacketTypeRegistry.getC2sPackets().contains(packetClass);
      boolean s2c = autismclient.util.PacketTypeRegistry.getS2cPackets().contains(packetClass);
      return c2s && s2c ? "BOTH" : (c2s ? "C2S" : (s2c ? "S2C" : "ANY"));
   }

   static int packetIdFor(Class<?> packetClass, String direction) {
      return numericId(packetTypesByClass().get(packetClass), direction);
   }

   static int numericId(PacketType<?> type, String direction) {
      if (type == null) {
         return -1;
      }

      try {
         int id = PacketWireCaptureStore.numericId("PLAY", direction, type);
         if (id < 0) {
            id = PacketWireCaptureStore.numericId("CONFIGURATION", direction, type);
         }

         return id;
      } catch (Throwable t) {
         return -1;
      }
   }

   private static Map<Class<?>, PacketType<?>> packetTypesByClass() {
      Map<Class<?>, PacketType<?>> current = packetTypeCache;
      if (current != null) {
         return current;
      }

      synchronized (AiPacketLog.class) {
         current = packetTypeCache;
         if (current != null) {
            return current;
         }

         Map<Class<?>, PacketType<?>> built = new HashMap<>();

         for (Class<?> holder : packetTypeHolders) {
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

         packetTypeCache = Map.copyOf(built);
         return packetTypeCache;
      }
   }

   static String normalizeDirection(String raw) {
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

   static String normalizeCategory(String raw) {
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

   static int clampLimit(int requested, int max) {
      return Math.max(1, Math.min(requested, max));
   }

   static int clampOffset(int offset) {
      return Math.max(0, offset);
   }

   private static String truncate(String value, int max) {
      if (value == null) {
         return "";
      } else {
         return value.length() <= max ? value : value.substring(0, max);
      }
   }
}
