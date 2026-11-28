package autismclient.protector;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;

public final class OutboundChannelPolicy {
   private static final int init_I = 256;
   private static final int load_I = 1024;
   private static final Map<ConnectionPrivacyRegistry.ConnectionSession, Set<ServerboundCustomQueryAnswerPacket>> authorizedAnswers = new IdentityHashMap<>();
   private static final OutboundChannelPolicy.Verdict PASS = new OutboundChannelPolicy.Verdict(
      OutboundChannelPolicy.Verdict.VerdictKind.PASS, null
   );
   private static final OutboundChannelPolicy.Verdict DROP = new OutboundChannelPolicy.Verdict(
      OutboundChannelPolicy.Verdict.VerdictKind.DROP, null
   );

   private OutboundChannelPolicy() {
   }

   public static OutboundChannelPolicy.Verdict verdict(Connection connection, Packet<?> packet) {
      if (!(packet instanceof ServerboundCustomPayloadPacket) && !(packet instanceof ServerboundCustomQueryAnswerPacket)) {
         return PASS;
      } else {
         ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getActive(connection);
         if (context == null) {
            return DROP;
         } else if (context.isLocalOrMemory()) {
            return PASS;
         } else {
            return packet instanceof ServerboundCustomQueryAnswerPacket answer
               ? filterQueryAnswer(context, answer)
               : filterPayload(connection, context, (ServerboundCustomPayloadPacket)packet);
         }
      }
   }

   public static boolean init_Z(Connection connection) {
      return connection != null && (connection.getReceiving() == PacketFlow.SERVERBOUND || connection.isMemoryConnection())
         ? true
         : isModded(ConnectionPrivacyRegistry.getActive(connection));
   }

   public static boolean init(PacketListener listener) {
      return isModded(ConnectionPrivacyRegistry.getByListener(listener));
   }

   private static boolean isModded(ConnectionPrivacyRegistry.ConnectionSession context) {
      return context != null && context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.MODDED;
   }

   public static void init(Connection connection, ServerboundCustomQueryAnswerPacket answer) {
      if (connection != null && answer != null) {
         ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getActive(connection);
         if (context != null && context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.VANILLA) {
            try {
               if (answer.payload() != null) {
                  return;
               }
            } catch (Throwable t) {
               return;
            }

            synchronized (authorizedAnswers) {
               Set<ServerboundCustomQueryAnswerPacket> authorized = authorizedAnswers.get(context);
               if (authorized == null) {
                  if (authorizedAnswers.size() >= 1024) {
                     return;
                  }

                  authorized = Collections.newSetFromMap(new IdentityHashMap<>());
                  authorizedAnswers.put(context, authorized);
               }

               if (authorized.size() < 256) {
                  authorized.add(answer);
               }
            }
         }
      }
   }

   public static void init_V(Connection connection) {
      if (connection != null) {
         forget(ConnectionPrivacyRegistry.getActive(connection));
      }
   }

   public static void forget(ConnectionPrivacyRegistry.ConnectionSession context) {
      if (context != null) {
         synchronized (authorizedAnswers) {
            authorizedAnswers.remove(context);
         }

         boolean current = ConnectionPrivacyRegistry.isActive(context);
         if (current) {
            OutboundPacketFilterHandler.forget(context.connection());
         }

         ResourcePackSessionTracker.init_V(context);
         if (current) {
            LateRegisterSynthesizer.forget(context.connection());
         }

         ConnectionPrivacyRegistry.unregisterSession(context);
      }
   }

   private static OutboundChannelPolicy.Verdict filterPayload(Connection connection, ConnectionPrivacyRegistry.ConnectionSession context, ServerboundCustomPayloadPacket packet) {
      CustomPacketPayload payload;
      try {
         payload = packet.payload();
      } catch (Throwable t) {
         return DROP;
      }

      if (context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.VANILLA) {
         return payload instanceof BrandPayload ? replaceWith(new ServerboundCustomPayloadPacket(new BrandPayload("vanilla"))) : DROP;
      }

      CustomPayloadFilter.Verdict verdict = CustomPayloadFilter.verdict(connection, packet, false);
      OutboundChannelPolicy.Verdict var10000;
      switch (verdict.kind) {
         case PASS:
            var10000 = PASS;
            break;
         case DROP:
            var10000 = DROP;
            break;
         case REPLACE:
            var10000 = replaceWith(verdict.replacement);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static OutboundChannelPolicy.Verdict filterQueryAnswer(ConnectionPrivacyRegistry.ConnectionSession context, ServerboundCustomQueryAnswerPacket answer) {
      if (context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.MODDED) {
         return PASS;
      }

      boolean authorized;
      synchronized (authorizedAnswers) {
         Set<ServerboundCustomQueryAnswerPacket> answers = authorizedAnswers.get(context);
         authorized = answers != null && answers.remove(answer);
         if (answers != null && answers.isEmpty()) {
            authorizedAnswers.remove(context);
         }
      }

      if (!authorized) {
         return DROP;
      }

      try {
         return answer.payload() == null ? PASS : DROP;
      } catch (Throwable t) {
         return DROP;
      }
   }

   private static OutboundChannelPolicy.Verdict replaceWith(Packet<?> replacement) {
      return replacement == null ? DROP : new OutboundChannelPolicy.Verdict(OutboundChannelPolicy.Verdict.VerdictKind.REPLACE, replacement);
   }

   static void reset() {
      synchronized (authorizedAnswers) {
         authorizedAnswers.clear();
      }

      LateRegisterSynthesizer.reset();
   }

   public static final class Verdict {
      public final OutboundChannelPolicy.Verdict.VerdictKind kind;
      public final Packet<?> replacement;

      private Verdict(OutboundChannelPolicy.Verdict.VerdictKind kind, Packet<?> replacement) {
         this.kind = kind;
         this.replacement = replacement;
      }

      public enum VerdictKind {
         PASS,
         DROP,
         REPLACE;
      }
   }
}
