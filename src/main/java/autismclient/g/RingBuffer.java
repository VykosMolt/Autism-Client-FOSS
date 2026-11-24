package autismclient.g;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class RingBuffer {
   public static volatile String init;

   private RingBuffer() {
   }

   public static boolean init(String host) throws UnknownHostException {
      if (host == null) {
         return false;
      }

      for (InetAddress address : InetAddress.getAllByName(host)) {
         if (init(address)) {
            return true;
         }
      }

      return false;
   }

   private static boolean init(InetAddress address) {
      if (!address.isAnyLocalAddress() && !address.isLoopbackAddress() && !address.isSiteLocalAddress() && !address.isLinkLocalAddress()) {
         byte[] bytes = address.getAddress();
         if (bytes.length == 4) {
            int b0 = bytes[0] & 255;
            int b1 = bytes[1] & 255;
            if (b0 == 0) {
               return true;
            }

            if (b0 == 100 && b1 >= 64 && b1 <= 127) {
               return true;
            }
         } else if (bytes.length == 16) {
            int b0 = bytes[0] & 255;
            if ((b0 & 254) == 252) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public static boolean load(String host) {
      try {
         if (!init(host)) {
            return false;
         } else {
            return read(host) ? true : !init(init);
         }
      } catch (UnknownHostException unresolved) {
         return false;
      }
   }

   private static boolean read(String host) throws UnknownHostException {
      if (host == null) {
         return false;
      }

      for (InetAddress address : InetAddress.getAllByName(host)) {
         if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
            return true;
         }
      }

      return false;
   }
}
