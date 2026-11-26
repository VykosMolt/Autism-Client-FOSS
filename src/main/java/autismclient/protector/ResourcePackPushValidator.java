package autismclient.protector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;

public final class ResourcePackPushValidator {
   private ResourcePackPushValidator() {
   }

   public static ResourcePackSessionTracker.Inner_DataUtil init(ConnectionPrivacyRegistry.Inner_CachePool mode, boolean sharedBypass, boolean serializedBypass) {
      return serializedBypass ? ResourcePackSessionTracker.Inner_DataUtil.ISOLATED : ResourcePackSessionTracker.Inner_DataUtil.FULL;
   }

   public static ResourcePackPushValidator.Inner_StreamUtil init(ClientboundResourcePackPushPacket packet, boolean forceDeny, boolean bypass) {
      if (packet != null && packet.id() != null) {
         try {
            URL url = new URL(packet.url());
            String protocol = url.getProtocol();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
               return ResourcePackPushValidator.Inner_StreamUtil.init(ResourcePackPushValidator.Inner_DataUtil.INVALID_URL, "unsupported resource pack URL scheme");
            }
         } catch (MalformedURLException e) {
            return ResourcePackPushValidator.Inner_StreamUtil.init(ResourcePackPushValidator.Inner_DataUtil.INVALID_URL, "malformed resource pack URL");
         }

         if (forceDeny) {
            return ResourcePackPushValidator.Inner_StreamUtil.init(ResourcePackPushValidator.Inner_DataUtil.DECLINE, "resource pack auto-deny");
         } else {
            return bypass
               ? ResourcePackPushValidator.Inner_StreamUtil.init(ResourcePackPushValidator.Inner_DataUtil.BYPASS_SUCCESS, "resource pack bypass compatibility response")
               : ResourcePackPushValidator.Inner_StreamUtil.init_RefQueue_StreamUti();
         }
      } else {
         return ResourcePackPushValidator.Inner_StreamUtil.init_RefQueue_StreamUti();
      }
   }

   public static boolean init(ServerboundResourcePackPacket packet) {
      return false;
   }

   public static void init(UUID id) {
   }

   public static void init() {
   }

   public enum Inner_DataUtil {
      PASS,
      BYPASS_SUCCESS,
      DECLINE,
      FAILED_DOWNLOAD,
      INVALID_URL;
   }

   public static final class Inner_StreamUtil {
      private static final ResourcePackPushValidator.Inner_StreamUtil init_RefQueue_StreamUti = new ResourcePackPushValidator.Inner_StreamUtil(ResourcePackPushValidator.Inner_DataUtil.PASS, "");
      private final ResourcePackPushValidator.Inner_DataUtil init_RefQueue_DataUtil;
      private final String init_Str;

      private Inner_StreamUtil(ResourcePackPushValidator.Inner_DataUtil kind, String reason) {
         this.init_RefQueue_DataUtil = kind;
         this.init_Str = reason == null ? "" : reason;
      }

      public static ResourcePackPushValidator.Inner_StreamUtil init_RefQueue_StreamUti() {
         return init_RefQueue_StreamUti;
      }

      public static ResourcePackPushValidator.Inner_StreamUtil init(ResourcePackPushValidator.Inner_DataUtil kind, String reason) {
         return kind == ResourcePackPushValidator.Inner_DataUtil.PASS ? init_RefQueue_StreamUti : new ResourcePackPushValidator.Inner_StreamUtil(kind, reason);
      }

      public ResourcePackPushValidator.Inner_DataUtil init_RefQueue_DataUtil() {
         return this.init_RefQueue_DataUtil;
      }

      public String init_Str() {
         return this.init_Str;
      }

      public boolean init_Z() {
         return this.init_RefQueue_DataUtil != ResourcePackPushValidator.Inner_DataUtil.PASS;
      }
   }
}
