package autismclient.g;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;

public final class RefQueue {
   private RefQueue() {
   }

   public static FileCache.Inner_DataUtil init(BufferHelper.Inner_CachePool mode, boolean sharedBypass, boolean serializedBypass) {
      return serializedBypass ? FileCache.Inner_DataUtil.ISOLATED : FileCache.Inner_DataUtil.FULL;
   }

   public static RefQueue.Inner_StreamUtil init(ClientboundResourcePackPushPacket packet, boolean forceDeny, boolean bypass) {
      if (packet != null && packet.id() != null) {
         try {
            URL url = new URL(packet.url());
            String protocol = url.getProtocol();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
               return RefQueue.Inner_StreamUtil.init(RefQueue.Inner_DataUtil.INVALID_URL, "unsupported resource pack URL scheme");
            }
         } catch (MalformedURLException e) {
            return RefQueue.Inner_StreamUtil.init(RefQueue.Inner_DataUtil.INVALID_URL, "malformed resource pack URL");
         }

         if (forceDeny) {
            return RefQueue.Inner_StreamUtil.init(RefQueue.Inner_DataUtil.DECLINE, "resource pack auto-deny");
         } else {
            return bypass
               ? RefQueue.Inner_StreamUtil.init(RefQueue.Inner_DataUtil.BYPASS_SUCCESS, "resource pack bypass compatibility response")
               : RefQueue.Inner_StreamUtil.init_RefQueue_StreamUti();
         }
      } else {
         return RefQueue.Inner_StreamUtil.init_RefQueue_StreamUti();
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
      private static final RefQueue.Inner_StreamUtil init_RefQueue_StreamUti = new RefQueue.Inner_StreamUtil(RefQueue.Inner_DataUtil.PASS, "");
      private final RefQueue.Inner_DataUtil init_RefQueue_DataUtil;
      private final String init_Str;

      private Inner_StreamUtil(RefQueue.Inner_DataUtil kind, String reason) {
         this.init_RefQueue_DataUtil = kind;
         this.init_Str = reason == null ? "" : reason;
      }

      public static RefQueue.Inner_StreamUtil init_RefQueue_StreamUti() {
         return init_RefQueue_StreamUti;
      }

      public static RefQueue.Inner_StreamUtil init(RefQueue.Inner_DataUtil kind, String reason) {
         return kind == RefQueue.Inner_DataUtil.PASS ? init_RefQueue_StreamUti : new RefQueue.Inner_StreamUtil(kind, reason);
      }

      public RefQueue.Inner_DataUtil init_RefQueue_DataUtil() {
         return this.init_RefQueue_DataUtil;
      }

      public String init_Str() {
         return this.init_Str;
      }

      public boolean init_Z() {
         return this.init_RefQueue_DataUtil != RefQueue.Inner_DataUtil.PASS;
      }
   }
}
