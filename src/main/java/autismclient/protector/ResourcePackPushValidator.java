package autismclient.protector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;

public final class ResourcePackPushValidator {
   private ResourcePackPushValidator() {
   }

   public static ResourcePackSessionTracker.PackPolicy selectPolicy(ConnectionPrivacyRegistry.PrivacyMode mode, boolean sharedBypass, boolean serializedBypass) {
      return serializedBypass ? ResourcePackSessionTracker.PackPolicy.ISOLATED : ResourcePackSessionTracker.PackPolicy.FULL;
   }

   public static ResourcePackPushValidator.Outcome init(ClientboundResourcePackPushPacket packet, boolean forceDeny, boolean bypass) {
      if (packet != null && packet.id() != null) {
         try {
            URL url = new URL(packet.url());
            String protocol = url.getProtocol();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
               return ResourcePackPushValidator.Outcome.init(ResourcePackPushValidator.OutcomeKind.INVALID_URL, "unsupported resource pack URL scheme");
            }
         } catch (MalformedURLException e) {
            return ResourcePackPushValidator.Outcome.init(ResourcePackPushValidator.OutcomeKind.INVALID_URL, "malformed resource pack URL");
         }

         if (forceDeny) {
            return ResourcePackPushValidator.Outcome.init(ResourcePackPushValidator.OutcomeKind.DECLINE, "resource pack auto-deny");
         } else {
            return bypass
               ? ResourcePackPushValidator.Outcome.init(ResourcePackPushValidator.OutcomeKind.BYPASS_SUCCESS, "resource pack bypass compatibility response")
               : ResourcePackPushValidator.Outcome.init_RefQueue_StreamUti();
         }
      } else {
         return ResourcePackPushValidator.Outcome.init_RefQueue_StreamUti();
      }
   }

   public static boolean init(ServerboundResourcePackPacket packet) {
      return false;
   }

   public static void init(UUID id) {
   }

   public static void init() {
   }

   public enum OutcomeKind {
      PASS,
      BYPASS_SUCCESS,
      DECLINE,
      FAILED_DOWNLOAD,
      INVALID_URL;
   }

   public static final class Outcome {
      private static final ResourcePackPushValidator.Outcome init_RefQueue_StreamUti = new ResourcePackPushValidator.Outcome(ResourcePackPushValidator.OutcomeKind.PASS, "");
      private final ResourcePackPushValidator.OutcomeKind kind;
      private final String init_Str;

      private Outcome(ResourcePackPushValidator.OutcomeKind kind, String reason) {
         this.kind = kind;
         this.init_Str = reason == null ? "" : reason;
      }

      public static ResourcePackPushValidator.Outcome init_RefQueue_StreamUti() {
         return init_RefQueue_StreamUti;
      }

      public static ResourcePackPushValidator.Outcome init(ResourcePackPushValidator.OutcomeKind kind, String reason) {
         return kind == ResourcePackPushValidator.OutcomeKind.PASS ? init_RefQueue_StreamUti : new ResourcePackPushValidator.Outcome(kind, reason);
      }

      public ResourcePackPushValidator.OutcomeKind verdict() {
         return this.kind;
      }

      public String init_Str() {
         return this.init_Str;
      }

      public boolean init_Z() {
         return this.kind != ResourcePackPushValidator.OutcomeKind.PASS;
      }
   }
}
