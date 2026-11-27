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
   private static final CustomPayloadFilter.Inner_DataUtil init_IoWorker_DataUtil = new CustomPayloadFilter.Inner_DataUtil(CustomPayloadFilter.Inner_DataUtil.Inner_DataUtil_2.PASS, null);
   private static final CustomPayloadFilter.Inner_DataUtil load_IoWorker_DataUtil = new CustomPayloadFilter.Inner_DataUtil(CustomPayloadFilter.Inner_DataUtil.Inner_DataUtil_2.DROP, null);
   private static final String init_Str = "minecraft";
   private static final String load_Str = "register";
   private static final String read = "unregister";
   private static final String write = "mco";

   private CustomPayloadFilter() {
   }

   public static CustomPayloadFilter.Inner_DataUtil init() {
      return init_IoWorker_DataUtil;
   }

   public static CustomPayloadFilter.Inner_DataUtil load() {
      return load_IoWorker_DataUtil;
   }

   public static CustomPayloadFilter.Inner_DataUtil init(Packet<?> packet) {
      return init(null, packet, ProtectorFeatureFlags.sync());
   }

   static CustomPayloadFilter.Inner_DataUtil init(Packet<?> packet, boolean vanillaMode) {
      return init(null, packet, vanillaMode);
   }

   static CustomPayloadFilter.Inner_DataUtil init(Connection connection, Packet<?> packet, boolean vanillaMode) {
      if (packet == null) {
         return init_IoWorker_DataUtil;
      }

      if (packet instanceof ServerboundCustomPayloadPacket customPayload) {
         CustomPacketPayload payload;
         try {
            payload = customPayload.payload();
         } catch (Throwable t) {
            return vanillaMode ? load_IoWorker_DataUtil : init_IoWorker_DataUtil;
         }

         if (payload == null) {
            return vanillaMode ? load_IoWorker_DataUtil : init_IoWorker_DataUtil;
         }

         if (payload instanceof BrandPayload) {
            return init_IoWorker_DataUtil;
         }

         Identifier id = init_Identifier(payload);
         if (vanillaMode) {
            return load_IoWorker_DataUtil;
         }

         if (!ProtectorFeatureFlags.flush()) {
            init(connection, id);
            return init_IoWorker_DataUtil;
         }

         if (ProtectorFeatureFlags.isDirect(packet)) {
            init(connection, id);
            return init_IoWorker_DataUtil;
         }

         if (id == null) {
            return init_IoWorker_DataUtil;
         }

         String namespace = id.getNamespace();
         String path = id.getPath();
         if (!"minecraft".equals(namespace) || !"register".equals(path) && !"unregister".equals(path)) {
            if ("minecraft".equals(namespace) && "mco".equals(path)) {
               return load_IoWorker_DataUtil;
            } else {
               return ModNamespaceCatalog.init(id) ? init_IoWorker_DataUtil : load_IoWorker_DataUtil;
            }
         } else {
            CustomPayloadFilter.Inner_DataUtil verdict = init(connection, payload);
            if ("register".equals(path) && verdict.kind != CustomPayloadFilter.Inner_DataUtil.Inner_DataUtil_2.DROP) {
               LateRegisterSynthesizer.init_V(connection);
            }

            return verdict;
         }
      } else {
         return init_IoWorker_DataUtil;
      }
   }

   private static void init(Connection connection, Identifier id) {
      if (id != null && "minecraft".equals(id.getNamespace()) && "register".equals(id.getPath())) {
         LateRegisterSynthesizer.init_V(connection);
      }
   }

   private static Identifier init_Identifier(CustomPacketPayload payload) {
      try {
         Type<?> type = payload.type();
         if (type != null) {
            return type.id();
         }
      } catch (Throwable var2) {
      }

      return null;
   }

   private static CustomPayloadFilter.Inner_DataUtil init(Connection connection, CustomPacketPayload payload) {
      if (payload instanceof RegistrationPayload registrationPayload) {
         List<Identifier> kept = init(registrationPayload.channels(), ModNamespaceCatalog::init);
         if (kept.size() == registrationPayload.channels().size()) {
            return init_IoWorker_DataUtil;
         } else {
            return kept.isEmpty()
               ? load_IoWorker_DataUtil
               : new CustomPayloadFilter.Inner_DataUtil(
                  CustomPayloadFilter.Inner_DataUtil.Inner_DataUtil_2.REPLACE, new ServerboundCustomPayloadPacket(new RegistrationPayload(registrationPayload.type(), kept))
               );
         }
      } else {
         List<Identifier> channels = init_List(payload);
         if (channels == null) {
            init(connection, "uninspectable " + payload.getClass().getName() + " on " + init_Identifier(payload));
            return init_IoWorker_DataUtil;
         }

         for (Identifier channel : channels) {
            if (!ModNamespaceCatalog.init(channel)) {
               init(connection, "dropping non-whitelisted register channel " + channel);
               return load_IoWorker_DataUtil;
            }
         }

         return init_IoWorker_DataUtil;
      }
   }

   private static List<Identifier> init_List(CustomPacketPayload payload) {
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

   private static void init(Connection connection, String detail) {
      if (LateRegisterSynthesizer.init_Z(connection)) {
         AutismClientAddon.LOG.warn("[AutismProtector] Register fallback engaged ({}); passing by whitelist verdict only.", detail);
      }
   }

   static List<Identifier> init(Collection<Identifier> channels, Predicate<Identifier> whitelist) {
      List<Identifier> kept = new ArrayList<>(channels.size());

      for (Identifier channel : channels) {
         if (whitelist.test(channel)) {
            kept.add(channel);
         }
      }

      return kept;
   }

   public static final class Inner_DataUtil {
      public final CustomPayloadFilter.Inner_DataUtil.Inner_DataUtil_2 kind;
      public final Packet<?> replacement;

      private Inner_DataUtil(CustomPayloadFilter.Inner_DataUtil.Inner_DataUtil_2 kind, Packet<?> replacement) {
         this.kind = kind;
         this.replacement = replacement;
      }

      public enum Inner_DataUtil_2 {
         PASS,
         DROP,
         REPLACE;
      }
   }
}
