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

   public static JsonObject sendPacket(JsonObject args) {
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

      Class<? extends Packet<?>> packetClass = autismclient.util.PacketTypeRegistry.resolvePacketClass(name);
      if (packetClass == null) {
         throw new IllegalArgumentException("Unknown packet '" + name + "'; search exact names with client:packets first");
      }

      if (!autismclient.util.PacketTypeRegistry.getC2sPackets().contains(packetClass)) {
         throw new IllegalArgumentException(
            autismclient.util.PacketTypeRegistry.getS2cPackets().contains(packetClass)
               ? name + " is S2C-only; the client cannot originate it"
               : name + " is not registered as a C2S packet"
         );
      }

      String rawArgs = AiJson.getString(args, "args", "");
      PacketCommandBuilder.BuildResult built = PacketCommandBuilder.build(packetClass, rawArgs);
      if (built != null && built.success() && built.packet() != null) {
         Packet<?> packet = built.packet();
         boolean regenerate = AiJson.getBoolean(args, "regenerate", false);
         String mode = AiJson.getString(args, "mode", "send").trim().toLowerCase(Locale.ROOT);
         JsonObject out = new JsonObject();
         out.addProperty("name", autismclient.util.PacketTypeRegistry.getPacketName(packetClass));
         out.addProperty("packetClass", packetClass.getName());
         out.addProperty("mode", mode);
         out.addProperty("numericPacketId", AiPacketLog.numericId(packet.type(), "C2S"));
         autismclient.util.PacketSchemaRegistry.Inner_StreamUtil schema = autismclient.util.PacketSchemaRegistry.init(packetClass);
         out.addProperty("schemaComplete", schema != null && schema.complete() && !schema.inherited());
         out.addProperty("builderSource", built.source() == null ? "" : built.source());
         switch (mode) {
            case "send":
               Packet<?> wire = regenerate ? PacketRegenerator.regenerate(packet) : packet;
               if (wire == null) {
                  throw new IllegalArgumentException("PacketRegenerator produced no packet for " + name);
               }

               autismclient.util.DirectPacketSender.init(wire);
               out.addProperty("sent", true);
               out.addProperty("regenerated", regenerate && wire != packet);
               break;
            case "queue":
               autismclient.util.AutismSessionState.getInstance().enqueueDelayedPacket(packet, init(regenerate));
               out.addProperty("queued", true);
               out.addProperty("replayMode", init(regenerate).name());
               out.addProperty("queueSize", autismclient.util.AutismSessionState.getInstance().getDelayedPackets().size());
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
                  autismclient.util.AutismSessionState shared = autismclient.util.AutismSessionState.getInstance();
                  shared.setDelayUnit(hasMs ? autismclient.util.AutismSessionState.DelayUnit.MS : autismclient.util.AutismSessionState.DelayUnit.TICKS);
                  List<autismclient.util.AutismSessionState.DelayedPacket> queued = shared.getDelayedPackets();
                  queued.add(new autismclient.util.AutismSessionState.DelayedPacket(packet, delay, init(regenerate)));
                  shared.replaceDelayedPackets(queued);
                  out.addProperty("queued", true);
                  out.addProperty("delay", delay);
                  out.addProperty("delayMode", hasMs ? "MS" : "TICKS");
                  out.addProperty("replayMode", init(regenerate).name());
                  out.addProperty("queueSize", shared.getDelayedPackets().size());
                  break;
               }

               throw new IllegalArgumentException((hasMs ? "delayMs" : "delayTicks") + " must be 0.." + cap);
            default:
               throw new IllegalArgumentException("Unknown mode '" + mode + "' (send|queue|delay)");
         }

         return out;
      } else {
         String reason = built == null ? "no builder accepted the arguments" : built.message();
         String help = init(PacketCommandBuilder.usage(packetClass), 2000);
         throw new IllegalArgumentException("Could not build " + name + ": " + reason + (help.isEmpty() ? "" : "\n" + help));
      }
   }

   public static JsonObject load_JsonObject(JsonObject args) {
      String action = AiJson.getString(args, "action", "list").trim().toLowerCase(Locale.ROOT);
      autismclient.util.AutismSessionState shared = autismclient.util.AutismSessionState.getInstance();
      JsonObject var10000;
      switch (action) {
         case "list":
            var10000 = init(shared, args);
            break;
         case "clear":
            int cleared = shared.getDelayedPackets().size();
            shared.clearDelayedPackets();
            var10000 = AiJson.objectOf("cleared", cleared, "queueSize", 0);
            break;
         case "flush":
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getConnection() == null) {
               throw new IllegalArgumentException("No active server connection; join a world or server first");
            }

            if (shared.isSendingQueuedPackets()) {
               throw new IllegalStateException("A queued send is already draining; wait for it to finish");
            }

            if (!shared.hasDelayedPackets()) {
               throw new IllegalArgumentException("The delayed-packet queue is empty");
            }

            int flushed = shared.flushDelayedPackets(mc.getConnection());
            var10000 = AiJson.objectOf("flushed", flushed, "staggering", shared.isSendingQueuedPackets(), "drained", !shared.isSendingQueuedPackets());
            break;
         case "restore":
         case "restorelastflushed":
            if (!shared.restoreLastFlushedQueue()) {
               throw new IllegalArgumentException("No previously flushed queue to restore");
            }

            var10000 = AiJson.objectOf("restored", true, "queueSize", shared.getDelayedPackets().size());
            break;
         default:
            throw new IllegalArgumentException("Unknown queue action '" + action + "' (list|clear|flush|restore)");
      }

      return var10000;
   }

   private static JsonObject init(autismclient.util.AutismSessionState shared, JsonObject args) {
      List<autismclient.util.AutismSessionState.DelayedPacket> packets = shared.getDelayedPackets();
      int offset = Math.max(0, AiJson.getInt(args, "offset", 0));
      int limit = Math.max(1, Math.min(AiJson.getInt(args, "limit", 50), 100));
      JsonArray out = new JsonArray();
      int end = Math.min(packets.size(), offset + limit);

      for (int i = offset; i < end; i++) {
         autismclient.util.AutismSessionState.DelayedPacket queued = packets.get(i);
         if (queued != null && queued.packet != null) {
            out.add(
               AiJson.objectOf(
                  "index",
                  i,
                  "id",
                  queued.getId(),
                  "name",
                  init(queued.packet),
                  "packetClass",
                  queued.packet.getClass().getName(),
                  "delay",
                  queued.getDelay(),
                  "replayMode",
                  queued.getReplayMode().name(),
                  "sent",
                  queued.isSent()
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
         shared.getDelayUnit().name(),
         "staggering",
         shared.isSendingQueuedPackets(),
         "staggeredRemaining",
         shared.getSendingPackets().size()
      );
   }

   public static AiOperation send(final JsonObject args) {
      return new AiOperation() {
         @Override
         public AiOperation.Result tick() {
            JsonObject result = AiPacketSend.sendPacket(args);
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
                  return !autismclient.util.AutismSessionState.getInstance().isSendingQueuedPackets()
                     ? AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Flushed queued packet(s)", this.init_JsonObject)
                     : AiGameOperations.result(AiOperation.Status.RUNNING, "Queue draining", this.init_JsonObject);
               } else {
                  autismclient.util.AutismSessionState shared = autismclient.util.AutismSessionState.getInstance();
                  if (!shared.isSendingQueuedPackets() && !shared.hasDelayedPackets()) {
                     this.init_JsonObject.addProperty("drained", true);
                     return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Queue drained onto the wire", this.init_JsonObject);
                  } else if (System.nanoTime() > this.init_J) {
                     this.init_JsonObject.addProperty("drained", false);
                     this.init_JsonObject.addProperty("staggeredRemaining", shared.getSendingPackets().size());
                     return AiGameOperations.result(
                        AiOperation.Status.UNCERTAIN, "Queue still draining after 30s; inspect client:packet_queue for the remainder", this.init_JsonObject
                     );
                  } else {
                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING, "Queue draining (" + shared.getSendingPackets().size() + " remaining)", this.init_JsonObject
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
      String name = autismclient.util.PacketTypeRegistry.getPacketName((Class<? extends Packet<?>>)packet.getClass());
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
