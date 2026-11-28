package autismclient.protector;

import autismclient.AutismClientAddon;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public final class CustomPayloadFilter {
   private static final CustomPayloadFilter.Verdict PASS_VERDICT = new CustomPayloadFilter.Verdict(CustomPayloadFilter.Verdict.VerdictKind.PASS, null);
   private static final CustomPayloadFilter.Verdict DROP_VERDICT = new CustomPayloadFilter.Verdict(CustomPayloadFilter.Verdict.VerdictKind.DROP, null);
   private static final String init_Str = "minecraft";
   private static final String load_Str = "register";
   private static final String read = "unregister";
   private static final String write = "mco";

   private CustomPayloadFilter() {
   }

   public static CustomPayloadFilter.Verdict init() {
      return PASS_VERDICT;
   }

   public static CustomPayloadFilter.Verdict load() {
      return DROP_VERDICT;
   }

   public static CustomPayloadFilter.Verdict init(Packet<?> packet) {
      return verdict(null, packet, ProtectorFeatureFlags.spoofClientVanilla());
   }

   static CustomPayloadFilter.Verdict unusedVerdictFor(Packet<?> packet, boolean vanillaMode) {
      return verdict(null, packet, vanillaMode);
   }

   static CustomPayloadFilter.Verdict verdict(Connection connection, Packet<?> packet, boolean vanillaMode) {
      if (packet == null) {
         return PASS_VERDICT;
      }

      if (packet instanceof ServerboundCustomPayloadPacket customPayload) {
         CustomPacketPayload payload;
         try {
            payload = customPayload.payload();
         } catch (Throwable t) {
            return vanillaMode ? DROP_VERDICT : PASS_VERDICT;
         }

         if (payload == null) {
            return vanillaMode ? DROP_VERDICT : PASS_VERDICT;
         }

         if (payload instanceof BrandPayload) {
            return PASS_VERDICT;
         }

         Identifier id = payloadId(payload);
         if (vanillaMode) {
            return DROP_VERDICT;
         }

         if (!ProtectorFeatureFlags.filterChannels()) {
            noteRegisterChannel(connection, id);
            return PASS_VERDICT;
         }

         if (ProtectorFeatureFlags.isDirect(packet)) {
            noteRegisterChannel(connection, id);
            return PASS_VERDICT;
         }

         if (id == null) {
            return PASS_VERDICT;
         }

         String namespace = id.getNamespace();
         String path = id.getPath();
         if (!"minecraft".equals(namespace) || !"register".equals(path) && !"unregister".equals(path)) {
            if ("minecraft".equals(namespace) && "mco".equals(path)) {
               return DROP_VERDICT;
            } else {
               return ModNamespaceCatalog.isVanillaOrFabricChannel(id) ? PASS_VERDICT : DROP_VERDICT;
            }
         } else {
            CustomPayloadFilter.Verdict verdict = inspectPayload(connection, payload);
            if ("register".equals(path) && verdict.kind != CustomPayloadFilter.Verdict.VerdictKind.DROP) {
               LateRegisterSynthesizer.markRegisterSeen(connection);
            }

            return verdict;
         }
      } else {
         return PASS_VERDICT;
      }
   }

   private static void noteRegisterChannel(Connection connection, Identifier id) {
      if (id != null && "minecraft".equals(id.getNamespace()) && "register".equals(id.getPath())) {
         LateRegisterSynthesizer.markRegisterSeen(connection);
      }
   }

   private static Identifier payloadId(CustomPacketPayload payload) {
      try {
         Type<?> type = payload.type();
         if (type != null) {
            return type.id();
         }
      } catch (Throwable var2) {
      }

      return null;
   }

   private static CustomPayloadFilter.Verdict inspectPayload(Connection connection, CustomPacketPayload payload) {
      if (payload instanceof RegistrationPayload registrationPayload) {
         List<Identifier> kept = filter(registrationPayload.channels(), ModNamespaceCatalog::isVanillaOrFabricChannel);
         if (kept.size() == registrationPayload.channels().size()) {
            return PASS_VERDICT;
         } else {
            return kept.isEmpty()
               ? DROP_VERDICT
               : new CustomPayloadFilter.Verdict(
                  CustomPayloadFilter.Verdict.VerdictKind.REPLACE, new ServerboundCustomPayloadPacket(new RegistrationPayload(registrationPayload.type(), kept))
               );
         }
      } else {
         List<Identifier> channels = registeredChannels(payload);
         if (channels == null) {
            warnOnce(connection, "uninspectable " + payload.getClass().getName() + " on " + payloadId(payload));
            return PASS_VERDICT;
         }

         for (Identifier channel : channels) {
            if (!ModNamespaceCatalog.isVanillaOrFabricChannel(channel)) {
               warnOnce(connection, "dropping non-whitelisted register channel " + channel);
               return DROP_VERDICT;
            }
         }

         return PASS_VERDICT;
      }
   }

   private static List<Identifier> registeredChannels(CustomPacketPayload payload) {
      try {
         Method method = payload.getClass().getMethod("channels");
         if (method.invoke(payload) instanceof List<?> list) {
            List<Identifier> channels = new ArrayList<>(list.size());

            for (Object entry : list) {
               if (!(entry instanceof Identifier identifier)) {
                  return null;
               }

               channels.add(identifier);
            }

            return channels;
         } else {
            return null;
         }
      } catch (Throwable t) {
         return null;
      }
   }

   private static void warnOnce(Connection connection, String detail) {
      if (LateRegisterSynthesizer.shouldWarnOnce(connection)) {
         AutismClientAddon.LOG.warn("[AutismProtector] Register fallback engaged ({}); passing by whitelist verdict only.", detail);
      }
   }

   static List<Identifier> filter(Collection<Identifier> channels, Predicate<Identifier> whitelist) {
      List<Identifier> kept = new ArrayList<>(channels.size());

      for (Identifier channel : channels) {
         if (whitelist.test(channel)) {
            kept.add(channel);
         }
      }

      return kept;
   }

   public static final class Verdict {
      public final CustomPayloadFilter.Verdict.VerdictKind kind;
      public final Packet<?> replacement;

      private Verdict(CustomPayloadFilter.Verdict.VerdictKind kind, Packet<?> replacement) {
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
