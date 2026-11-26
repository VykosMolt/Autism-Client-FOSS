package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.util.PacketCommandBuilder;
import autismclient.util.PacketRegenerator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;

public final class AiPacketSend {
   private static final int init_I = 2000;
   private static final int load = 300000;
   private static final int read = 6000;
   private static final int write = 100;
   private static final long init_J = 30000000000L;

   private AiPacketSend() {
   }

   public static JsonObject init_JsonObject(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.getConnection() == null) {
         throw new IllegalArgumentException("No active server connection; join a world or server first");
      }

      if (autismclient.modules.aD.read_Z()) {
         throw new IllegalArgumentException("Packet sending is hard-locked by the pack-hide safety state");
      }

      String name = AiJson.getString(args, "name", "").trim();
      if (name.isEmpty()) {
         throw new IllegalArgumentException("name is required; resolve packet names with client:packets first");
      }

      Class<? extends Packet<?>> packetClass = autismclient.util.PacketTypeRegistry.init_Class_Str(name);
      if (packetClass == null) {
         throw new IllegalArgumentException("Unknown packet '" + name + "'; search exact names with client:packets first");
      }

      if (!autismclient.util.PacketTypeRegistry.load().contains(packetClass)) {
         throw new IllegalArgumentException(
            autismclient.util.PacketTypeRegistry.init_Set().contains(packetClass)
               ? name + " is S2C-only; the client cannot originate it"
               : name + " is not registered as a C2S packet"
         );
      }

      String rawArgs = AiJson.getString(args, "args", "");
      PacketCommandBuilder.Inner_StreamUtil built = PacketCommandBuilder.init_bm_StreamUtil_Class_Str(packetClass, rawArgs);
      if (built != null && built.init_Z() && built.init_Packet() != null) {
         Packet<?> packet = built.init_Packet();
         boolean regenerate = AiJson.getBoolean(args, "regenerate", false);
         String mode = AiJson.getString(args, "mode", "send").trim().toLowerCase(Locale.ROOT);
         JsonObject out = new JsonObject();
         out.addProperty("name", autismclient.util.PacketTypeRegistry.init_Str_Class(packetClass));
         out.addProperty("packetClass", packetClass.getName());
         out.addProperty("mode", mode);
         out.addProperty("numericPacketId", AiPacketLog.init(packet.type(), "C2S"));
         autismclient.util.PacketSchemaRegistry.Inner_StreamUtil schema = autismclient.util.PacketSchemaRegistry.init(packetClass);
         out.addProperty("schemaComplete", schema != null && schema.init_Z() && !schema.load_Z());
         out.addProperty("builderSource", built.load_Str() == null ? "" : built.load_Str());
         switch (mode) {
            case "send":
               Packet<?> wire = regenerate ? PacketRegenerator.init(packet) : packet;
               if (wire == null) {
                  throw new IllegalArgumentException("PacketRegenerator produced no packet for " + name);
               }

               autismclient.util.DirectPacketSender.init(wire);
               out.addProperty("sent", true);
               out.addProperty("regenerated", regenerate && wire != packet);
               break;
            case "queue":
               autismclient.util.AutismSessionState.init_ck().init_V_Packet_ck_BufferHelper(packet, init(regenerate));
               out.addProperty("queued", true);
               out.addProperty("replayMode", init(regenerate).name());
               out.addProperty("queueSize", autismclient.util.AutismSessionState.init_ck().load_List().size());
               break;
            case "delay":
               boolean hasMs = args.has("delayMs");
               boolean hasTicks = args.has("delayTicks");
               if (hasMs == hasTicks) {
                  throw new IllegalArgumentException("delay mode needs exactly one of delayMs or delayTicks");
               }

               int delay = AiJson.getInt(args, hasMs ? "delayMs" : "delayTicks", 0);
               int cap = hasMs ? 300000 : 6000;
               if (delay >= 0 && delay <= cap) {
                  autismclient.util.AutismSessionState shared = autismclient.util.AutismSessionState.init_ck();
                  shared.init_V_ck_DataUtil(hasMs ? autismclient.util.AutismSessionState.DelayUnit.MS : autismclient.util.AutismSessionState.DelayUnit.TICKS);
                  List<autismclient.util.AutismSessionState.DelayedPacket> queued = shared.load_List();
                  queued.add(new autismclient.util.AutismSessionState.DelayedPacket(packet, delay, init(regenerate)));
                  shared.load_V_List(queued);
                  out.addProperty("queued", true);
                  out.addProperty("delay", delay);
                  out.addProperty("delayMode", hasMs ? "MS" : "TICKS");
                  out.addProperty("replayMode", init(regenerate).name());
                  out.addProperty("queueSize", shared.load_List().size());
                  break;
               }

               throw new IllegalArgumentException((hasMs ? "delayMs" : "delayTicks") + " must be 0.." + cap);
            default:
               throw new IllegalArgumentException("Unknown mode '" + mode + "' (send|queue|delay)");
         }

         return out;
      } else {
         String reason = built == null ? "no builder accepted the arguments" : built.init_Str();
         String help = init(PacketCommandBuilder.init_Str_Class(packetClass), 2000);
         throw new IllegalArgumentException("Could not build " + name + ": " + reason + (help.isEmpty() ? "" : "\n" + help));
      }
   }

   public static JsonObject load_JsonObject(JsonObject args) {
      String action = AiJson.getString(args, "action", "list").trim().toLowerCase(Locale.ROOT);
      autismclient.util.AutismSessionState shared = autismclient.util.AutismSessionState.init_ck();
      JsonObject var10000;
      switch (action) {
         case "list":
            var10000 = init(shared, args);
            break;
         case "clear":
            int cleared = shared.load_List().size();
            shared.init_V();
            var10000 = AiJson.objectOf("cleared", cleared, "queueSize", 0);
            break;
         case "flush":
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getConnection() == null) {
               throw new IllegalArgumentException("No active server connection; join a world or server first");
            }

            if (shared.tick_Z()) {
               throw new IllegalStateException("A queued send is already draining; wait for it to finish");
            }

            if (!shared.open_Z()) {
               throw new IllegalArgumentException("The delayed-packet queue is empty");
            }

            int flushed = shared.init_I_ClientPacketListen(mc.getConnection());
            var10000 = AiJson.objectOf("flushed", flushed, "staggering", shared.tick_Z(), "drained", !shared.tick_Z());
            break;
         case "restore":
         case "restorelastflushed":
            if (!shared.poll_Z()) {
               throw new IllegalArgumentException("No previously flushed queue to restore");
            }

            var10000 = AiJson.objectOf("restored", true, "queueSize", shared.load_List().size());
            break;
         default:
            throw new IllegalArgumentException("Unknown queue action '" + action + "' (list|clear|flush|restore)");
      }

      return var10000;
   }

   private static JsonObject init(autismclient.util.AutismSessionState shared, JsonObject args) {
      List<autismclient.util.AutismSessionState.DelayedPacket> packets = shared.load_List();
      int offset = Math.max(0, AiJson.getInt(args, "offset", 0));
      int limit = Math.max(1, Math.min(AiJson.getInt(args, "limit", 50), 100));
      JsonArray out = new JsonArray();
      int end = Math.min(packets.size(), offset + limit);

      for (int i = offset; i < end; i++) {
         autismclient.util.AutismSessionState.DelayedPacket queued = packets.get(i);
         if (queued != null && queued.init_Packet != null) {
            out.add(
               AiJson.objectOf(
                  "index",
                  i,
                  "id",
                  queued.init_I(),
                  "name",
                  init(queued.init_Packet),
                  "packetClass",
                  queued.init_Packet.getClass().getName(),
                  "delay",
                  queued.load_I(),
                  "replayMode",
                  queued.init_ck_BufferHelper().name(),
                  "sent",
                  queued.load_Z()
               )
            );
         }
      }

      return AiJson.objectOf(
         "packets",
         out,
         "total",
         packets.size(),
         "offset",
         offset,
         "limit",
         limit,
         "delayMode",
         shared.init_ck_DataUtil().name(),
         "staggering",
         shared.tick_Z(),
         "staggeredRemaining",
         shared.read_List().size()
      );
   }

   public static AiOperation init_AiOperation(final JsonObject args) {
      return new AiOperation() {
         @Override
         public AiOperation.Result tick() {
            JsonObject result = AiPacketSend.init_JsonObject(args);
            boolean sent = result.has("sent") && result.get("sent").getAsBoolean();
            return AiGameOperations.result(
               sent ? AiOperation.Status.UNCERTAIN : AiOperation.Status.SUCCEEDED,
               sent ? "Packet dispatched on the wire; reobserve for the server outcome" : "Packet staged in the delayed-packet queue",
               result
            );
         }

         @Override
         public void cancel() {
         }
      };
   }

   public static AiOperation load_AiOperation(final JsonObject args) {
      String action = AiJson.getString(args, "action", "list").trim().toLowerCase(Locale.ROOT);
      return !"flush".equals(action)
         ? new AiOperation() {
            @Override
            public AiOperation.Result tick() {
               return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Packet queue action applied", AiPacketSend.load_JsonObject(args));
            }

            @Override
            public void cancel() {
            }
         }
         : new AiOperation() {
            boolean init_Z;
            long init_J;
            JsonObject init_JsonObject;

            @Override
            public AiOperation.Result tick() {
               if (!this.init_Z) {
                  this.init_Z = true;
                  this.init_J = System.nanoTime() + 30000000000L;
                  this.init_JsonObject = AiPacketSend.load_JsonObject(args);
                  return !autismclient.util.AutismSessionState.init_ck().tick_Z()
                     ? AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Flushed queued packet(s)", this.init_JsonObject)
                     : AiGameOperations.result(AiOperation.Status.RUNNING, "Queue draining", this.init_JsonObject);
               } else {
                  autismclient.util.AutismSessionState shared = autismclient.util.AutismSessionState.init_ck();
                  if (!shared.tick_Z() && !shared.open_Z()) {
                     this.init_JsonObject.addProperty("drained", true);
                     return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Queue drained onto the wire", this.init_JsonObject);
                  } else if (System.nanoTime() > this.init_J) {
                     this.init_JsonObject.addProperty("drained", false);
                     this.init_JsonObject.addProperty("staggeredRemaining", shared.read_List().size());
                     return AiGameOperations.result(
                        AiOperation.Status.UNCERTAIN, "Queue still draining after 30s; inspect client:packet_queue for the remainder", this.init_JsonObject
                     );
                  } else {
                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING, "Queue draining (" + shared.read_List().size() + " remaining)", this.init_JsonObject
                     );
                  }
               }
            }

            @Override
            public void cancel() {
            }
         };
   }

   private static autismclient.util.AutismSessionState.ReplayMode init(boolean regenerate) {
      return regenerate ? autismclient.util.AutismSessionState.ReplayMode.REGENERATE : autismclient.util.AutismSessionState.ReplayMode.EXACT;
   }

   private static String init(Packet<?> packet) {
      String name = autismclient.util.PacketTypeRegistry.init_Str_Class((Class<? extends Packet<?>>)packet.getClass());
      return name == null ? packet.getClass().getSimpleName() : name;
   }

   private static String init(String value, int max) {
      if (value == null) {
         return "";
      } else {
         return value.length() <= max ? value : value.substring(0, max);
      }
   }
}
