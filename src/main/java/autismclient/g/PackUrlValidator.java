package autismclient.g;

import java.io.IOException;
import java.net.IDN;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class PackUrlValidator {
   private static final int init = 25565;

   private PackUrlValidator() {
   }

   public static PackUrlValidator.Inner_ConfigStore init_TextFormat_ConfigS_URL(URL url) throws MalformedURLException {
      Objects.requireNonNull(url, "url");

      try {
         return init_TextFormat_ConfigS_URI(url.toURI());
      } catch (URISyntaxException e) {
         throw init_MalformedURLExcept_Str_Exception(url.toExternalForm(), e);
      }
   }

   public static PackUrlValidator.Inner_ConfigStore init_TextFormat_ConfigS_URI(URI uri) throws MalformedURLException {
      Objects.requireNonNull(uri, "uri");
      String text = uri.toASCIIString();
      if (text.equals(uri.toString()) && !init_Z_Str(text) && !uri.isOpaque()) {
         String scheme = uri.getScheme();
         if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            String rawAuthority = uri.getRawAuthority();
            if (rawAuthority != null && !rawAuthority.isBlank() && uri.getRawUserInfo() == null && rawAuthority.indexOf(64) < 0) {
               String host = uri.getHost();
               if (host != null && !host.isBlank()) {
                  host = init_Str_Str(host);
                  if (host.isBlank() || init_Z_Str(host) || host.indexOf(37) >= 0 || host.indexOf(47) >= 0) {
                     throw init_MalformedURLExcept_Str_Exception(text, null);
                  }

                  if (host.chars().anyMatch(c -> c > 127)) {
                     throw init_MalformedURLExcept_Str_Exception(text, null);
                  }

                  if (host.matches("[0-9.]+") && init_BArr_Str(host) == null) {
                     throw init_MalformedURLExcept_Str_Exception(text, null);
                  }

                  if (host.indexOf(58) >= 0) {
                     if (!load(rawAuthority) || host.indexOf(46) >= 0) {
                        throw init_MalformedURLExcept_Str_Exception(text, null);
                     }

                     try {
                        InetAddress numeric = InetAddress.getByName(host);
                        if (!(numeric instanceof Inet6Address)) {
                           throw init_MalformedURLExcept_Str_Exception(text, null);
                        }
                     } catch (UnknownHostException e) {
                        throw init_MalformedURLExcept_Str_Exception(text, e);
                     }
                  }

                  int port = uri.getPort();
                  if (port != 0 && port <= 65535 && port >= -1) {
                     if (port == -1) {
                        port = scheme.equalsIgnoreCase("https") ? 443 : 80;
                     }

                     String requestTarget = uri.getRawPath();
                     if (requestTarget == null || requestTarget.isEmpty()) {
                        requestTarget = "/";
                     }

                     if (uri.getRawQuery() != null) {
                        requestTarget = requestTarget + "?" + uri.getRawQuery();
                     }

                     String authority = init_Str_Str(host, port, scheme.equalsIgnoreCase("https") ? 443 : 80);
                     return new PackUrlValidator.Inner_ConfigStore(uri, host, port, authority, requestTarget);
                  } else {
                     throw init_MalformedURLExcept_Str_Exception(text, null);
                  }
               } else {
                  throw init_MalformedURLExcept_Str_Exception(text, null);
               }
            } else {
               throw init_MalformedURLExcept_Str_Exception(text, null);
            }
         } else {
            throw new MalformedURLException("Only HTTP and HTTPS resource-pack URLs are accepted");
         }
      } else {
         throw init_MalformedURLExcept_Str_Exception(text, null);
      }
   }

   public static PackUrlValidator.Inner_CachePool init_TextFormat_CachePo_URL(URL url, ResourcePackDownloadScope.Inner_BufferHelper snapshot, PackUrlValidator.Inner_StreamUtil resolver) throws IOException {
      return init_TextFormat_CachePo_TextFormat_ConfigS(init_TextFormat_ConfigS_URL(url), snapshot, resolver);
   }

   public static PackUrlValidator.Inner_CachePool init_TextFormat_CachePo_TextFormat_ConfigS(
      PackUrlValidator.Inner_ConfigStore target, ResourcePackDownloadScope.Inner_BufferHelper snapshot, PackUrlValidator.Inner_StreamUtil resolver
   ) throws IOException {
      Objects.requireNonNull(snapshot, "snapshot");
      List<InetAddress> addresses = init_List_Str_TextFormat_StreamU(target.load(), resolver);
      PackUrlValidator.Inner_DataUtil kind = init_TextFormat_DataUti_List(addresses);

      for (InetAddress address : addresses) {
         if (!init_Z_InetAddress(address, target.init_I(), kind, snapshot)) {
            throw new IOException("Resource-pack address is outside the connected server's allowed scope");
         }
      }

      return new PackUrlValidator.Inner_CachePool(target.init_URI(), target.load(), target.init_I(), addresses);
   }

   public static PackUrlValidator.Inner_BufferHelper init_TextFormat_BufferH_Proxy_TextFormat_StreamU(Proxy proxy, PackUrlValidator.Inner_StreamUtil resolver) throws IOException {
      if (proxy != null && proxy.type() != Type.DIRECT) {
         if (proxy.address() instanceof InetSocketAddress socketAddress) {
            PackUrlValidator.Inner_ConfigStore target = init_TextFormat_ConfigS_InetSocketAddress(socketAddress);
            List<InetAddress> addresses = init_List_Str_TextFormat_StreamU(target.load(), resolver);
            PackUrlValidator.Inner_DataUtil kind = init_TextFormat_DataUti_List(addresses);
            if (kind == PackUrlValidator.Inner_DataUtil.SPECIAL) {
               throw new IOException("The configured proxy address is not a unicast address");
            } else {
               return new PackUrlValidator.Inner_BufferHelper(
                  proxy.type(),
                  new PackUrlValidator.Inner_CachePool(target.init_URI(), target.load(), target.init_I(), addresses),
                  proxy.type().name().toLowerCase(Locale.ROOT) + "://" + target.read(),
                  true
               );
            }
         } else {
            throw new IOException("The configured proxy has no socket address");
         }
      } else {
         return null;
      }
   }

   public static PackUrlValidator.Inner_BufferHelper init_TextFormat_BufferH_Str(
      String location, ResourcePackDownloadScope.Inner_BufferHelper snapshot, PackUrlValidator.Inner_StreamUtil resolver
   ) throws IOException, MalformedURLException {
      if (location != null && !location.isBlank()) {
         URI uri;
         try {
            uri = new URI(location);
         } catch (URISyntaxException e) {
            throw init_MalformedURLExcept_Str_Exception(location, e);
         }

         PackUrlValidator.Inner_ConfigStore target = init_TextFormat_ConfigS_URI(uri);
         if (!target.init_Str().equals("http")
            || uri.getRawQuery() != null
            || uri.getRawFragment() != null
            || uri.getRawPath() != null && !uri.getRawPath().isEmpty() && !uri.getRawPath().equals("/")) {
            throw new IOException("HTTP 305 requires an unambiguous HTTP proxy URI");
         }

         PackUrlValidator.Inner_CachePool endpoint = init_TextFormat_CachePo_TextFormat_ConfigS(target, snapshot, resolver);
         return new PackUrlValidator.Inner_BufferHelper(Type.HTTP, endpoint, "http://" + target.read(), false);
      } else {
         throw new IOException("HTTP 305 has no proxy URI");
      }
   }

   public static String init_Str_BufferHelper_Buffe(ConnectionPrivacyRegistry.Inner_BufferHelper endpoint) throws IOException {
      Objects.requireNonNull(endpoint, "endpoint");
      String host = endpoint.init_Str();
      if (host != null && !host.isBlank() && !init_Z_Str(host) && host.indexOf(37) < 0) {
         host = init_Str_Str(host.trim());

         while (host.endsWith(".") && host.length() > 1) {
            host = host.substring(0, host.length() - 1);
         }

         if (host.isEmpty()) {
            throw new IOException("Invalid original Minecraft origin");
         }

         int port = endpoint.init_I() == 0 ? 25565 : endpoint.init_I();
         if (port >= 1 && port <= 65535) {
            try {
               if (host.indexOf(58) >= 0) {
                  InetAddress address = InetAddress.getByName(host);
                  if (!(address instanceof Inet6Address)) {
                     throw new IOException("Invalid original IPv6 origin");
                  }

                  host = address.getHostAddress();
                  int zone = host.indexOf(37);
                  if (zone >= 0) {
                     host = host.substring(0, zone);
                  }
               } else {
                  if (host.matches("[0-9.]+") && init_BArr_Str(host) == null) {
                     throw new IOException("Invalid original numeric origin");
                  }

                  host = IDN.toASCII(host, 2).toLowerCase(Locale.ROOT);
               }
            } catch (IllegalArgumentException | UnknownHostException e) {
               throw new IOException("Invalid original Minecraft origin", e);
            }

            return init_Str_Str(host, port, 25565);
         } else {
            throw new IOException("Invalid original Minecraft port");
         }
      } else {
         throw new IOException("Invalid original Minecraft origin");
      }
   }

   public static String init_Str_SocketAddress(SocketAddress socketAddress) throws IOException {
      Objects.requireNonNull(socketAddress, "peerAddress");
      if (socketAddress instanceof InetSocketAddress inet) {
         InetAddress address = inet.getAddress();
         if (address != null && inet.getPort() >= 1 && inet.getPort() <= 65535) {
            return address.getHostAddress() + ":" + inet.getPort();
         } else {
            throw new IOException("The connected peer is unresolved");
         }
      } else {
         String local = socketAddress.toString();
         if (!local.isBlank() && !init_Z_Str(local)) {
            return "local:" + local;
         } else {
            throw new IOException("The connected local peer has no stable identity");
         }
      }
   }

   public static String init_Str_InetAddress(InetAddress address, int port, String scheme) {
      int defaultPort = scheme.equalsIgnoreCase("https") ? 443 : 80;
      return init_Str_Str(address.getHostAddress(), port, defaultPort);
   }

   public static boolean init_Z_TextFormat_ConfigS_TextFormat_ConfigS(PackUrlValidator.Inner_ConfigStore first, PackUrlValidator.Inner_ConfigStore second) {
      return first.init_Str().equals(second.init_Str()) && first.load().equalsIgnoreCase(second.load()) && first.init_I() == second.init_I();
   }

   private static PackUrlValidator.Inner_ConfigStore init_TextFormat_ConfigS_InetSocketAddress(InetSocketAddress address) throws IOException {
      String host = address.getHostString();
      int port = address.getPort();
      if (host != null && !host.isBlank() && port >= 1 && port <= 65535) {
         String literal = host.indexOf(58) >= 0 && !host.startsWith("[") ? "[" + host + "]" : host;

         try {
            return init_TextFormat_ConfigS_URI(new URI("http://" + literal + ":" + port + "/"));
         } catch (URISyntaxException e) {
            throw init_MalformedURLExcept_Str_Exception(address.toString(), e);
         }
      } else {
         throw new IOException("Invalid configured proxy endpoint");
      }
   }

   private static List<InetAddress> init_List_Str_TextFormat_StreamU(String host, PackUrlValidator.Inner_StreamUtil resolver) throws UnknownHostException {
      Objects.requireNonNull(resolver, "resolver");
      List<InetAddress> resolved = resolver.resolve(host);
      if (resolved != null && !resolved.isEmpty()) {
         List<InetAddress> addresses = new ArrayList<>(resolved.size());

         for (InetAddress address : resolved) {
            if (address == null) {
               throw new UnknownHostException(host);
            }

            addresses.add(address);
         }

         return List.copyOf(addresses);
      } else {
         throw new UnknownHostException(host);
      }
   }

   private static PackUrlValidator.Inner_DataUtil init_TextFormat_DataUti_List(List<InetAddress> addresses) throws IOException {
      PackUrlValidator.Inner_DataUtil kind = init_TextFormat_DataUti_InetAddress(addresses.getFirst());

      for (InetAddress address : addresses) {
         if (init_TextFormat_DataUti_InetAddress(address) != kind) {
            throw new IOException("Mixed DNS answer categories are not accepted");
         }
      }

      if (kind == PackUrlValidator.Inner_DataUtil.SPECIAL) {
         throw new IOException("Special-use DNS addresses are not accepted");
      } else {
         return kind;
      }
   }

   private static boolean init_Z_InetAddress(InetAddress address, int port, PackUrlValidator.Inner_DataUtil kind, ResourcePackDownloadScope.Inner_BufferHelper snapshot) {
      if (kind == PackUrlValidator.Inner_DataUtil.PUBLIC) {
         return true;
      }

      if (snapshot.init_Z()) {
         return kind == PackUrlValidator.Inner_DataUtil.LOOPBACK;
      }

      if (snapshot.init_SocketAddress() instanceof InetSocketAddress peer && peer.getAddress() != null) {
         boolean sameIp = Arrays.equals(peer.getAddress().getAddress(), address.getAddress());
         if (!sameIp) {
            return false;
         } else {
            return snapshot.init_ServerData_Type() == net.minecraft.client.multiplayer.ServerData.Type.LAN
               ? kind == PackUrlValidator.Inner_DataUtil.PRIVATE || kind == PackUrlValidator.Inner_DataUtil.LINK_LOCAL
               : kind == PackUrlValidator.Inner_DataUtil.PRIVATE && port == peer.getPort();
         }
      } else {
         return false;
      }
   }

   private static PackUrlValidator.Inner_DataUtil init_TextFormat_DataUti_InetAddress(InetAddress address) {
      byte[] bytes = address.getAddress();
      if (address.isAnyLocalAddress() || address.isMulticastAddress() || init_Z_BArr(bytes) || init_Z_InetAddress_BArr(address, bytes)) {
         return PackUrlValidator.Inner_DataUtil.SPECIAL;
      } else if (address.isLoopbackAddress()) {
         return PackUrlValidator.Inner_DataUtil.LOOPBACK;
      } else if (address.isLinkLocalAddress()) {
         return PackUrlValidator.Inner_DataUtil.LINK_LOCAL;
      } else if (address.isSiteLocalAddress() || load(bytes) || read(bytes)) {
         return PackUrlValidator.Inner_DataUtil.PRIVATE;
      } else if (address instanceof Inet4Address) {
         return PackUrlValidator.Inner_DataUtil.PUBLIC;
      } else {
         return address instanceof Inet6Address ? PackUrlValidator.Inner_DataUtil.PUBLIC : PackUrlValidator.Inner_DataUtil.SPECIAL;
      }
   }

   private static boolean init_Z_BArr(byte[] b) {
      if (b.length != 16) {
         return false;
      } else if (init_Z_BArr_IArr(b, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 255)) {
         return true;
      } else {
         return !init_Z_BArr_IArr(b, 0, 100, 255, 155) && !init_Z_BArr_IArr(b, 0, 100, 255, 155, 1) ? b[0] == 32 && b[1] == 2 : true;
      }
   }

   private static boolean init_Z_InetAddress_BArr(InetAddress address, byte[] b) {
      if (!(address instanceof Inet4Address)) {
         if (b.length != 16) {
            return true;
         } else if (init_Z_BArr_IArr(b, 32, 1, 13, 184)) {
            return true;
         } else {
            return b[0] == 32 && b[1] == 1 && (init_I_B(b[2]) & 254) == 0 ? true : init_Z_BArr_IArr(b, 1, 0) && init_Z_BArr(b, 2, 12);
         }
      } else {
         int a = init_I_B(b[0]);
         int second = init_I_B(b[1]);
         int third = init_I_B(b[2]);
         return a == 0
            || a >= 224
            || a == 192 && second == 0 && third == 0
            || a == 192 && second == 0 && third == 2
            || a == 192 && second == 88 && third == 99
            || a == 198 && (second == 18 || second == 19)
            || a == 198 && second == 51 && third == 100
            || a == 203 && second == 0 && third == 113;
      }
   }

   private static boolean load(byte[] b) {
      return b.length == 4 && init_I_B(b[0]) == 100 && (init_I_B(b[1]) & 192) == 64;
   }

   private static boolean read(byte[] bytes) {
      return bytes.length == 16 && (bytes[0] & 254) == 252;
   }

   private static boolean init_Z_BArr_IArr(byte[] value, int... prefix) {
      if (value.length < prefix.length) {
         return false;
      }

      for (int i = 0; i < prefix.length; i++) {
         if (init_I_B(value[i]) != prefix[i]) {
            return false;
         }
      }

      return true;
   }

   private static boolean init_Z_BArr(byte[] value, int from, int to) {
      for (int i = from; i < to; i++) {
         if (value[i] != 0) {
            return false;
         }
      }

      return true;
   }

   private static int init_I_B(byte value) {
      return value & 0xFF;
   }

   private static byte[] init_BArr_Str(String host) {
      String[] parts = host.split("\\.", -1);
      if (parts.length != 4) {
         return null;
      }

      byte[] bytes = new byte[4];

      for (int i = 0; i < 4; i++) {
         String part = parts[i];
         if (part.isEmpty() || part.length() > 3 || !part.chars().allMatch(Character::isDigit)) {
            return null;
         }

         int value;
         try {
            value = Integer.parseInt(part);
         } catch (NumberFormatException e) {
            return null;
         }

         if (value > 255) {
            return null;
         }

         bytes[i] = (byte)value;
      }

      return bytes;
   }

   private static boolean init_Z_Str(String value) {
      for (int i = 0; i < value.length(); i++) {
         char c = value.charAt(i);
         if (c <= ' ' || c == 127 || c == '\\') {
            return true;
         }
      }

      return false;
   }

   private static boolean load(String rawAuthority) {
      String hostPort = rawAuthority.substring(rawAuthority.lastIndexOf(64) + 1);
      return hostPort.startsWith("[") && hostPort.indexOf(93) > 0;
   }

   private static String init_Str_Str(String host) {
      return host.startsWith("[") && host.endsWith("]") ? host.substring(1, host.length() - 1) : host;
   }

   private static String init_Str_Str(String host, int port, int defaultPort) {
      String formatted = host.indexOf(58) >= 0 ? "[" + host + "]" : host;
      return port == defaultPort ? formatted : formatted + ":" + port;
   }

   private static MalformedURLException init_MalformedURLExcept_Str_Exception(String value, Exception cause) {
      MalformedURLException exception = new MalformedURLException("Invalid resource-pack URL: " + value);
      if (cause != null) {
         exception.initCause(cause);
      }

      return exception;
   }

   public record Inner_BufferHelper(Type init_Proxy_Type, PackUrlValidator.Inner_CachePool init_TextFormat_CachePo, String init_Str, boolean init_Z) {
   }

   public record Inner_CachePool(URI init_URI, String init_Str, int init_I, List<InetAddress> init_List) {
      public Inner_CachePool {
         init_List = List.copyOf(init_List);
      }

      public String init_Str() {
         return this.init_URI.getScheme().toLowerCase(Locale.ROOT);
      }

      public PackUrlValidator.Inner_ConfigStore init_TextFormat_ConfigS() {
         try {
            return PackUrlValidator.init_TextFormat_ConfigS_URI(this.init_URI);
         } catch (IOException e) {
            throw new IllegalStateException(e);
         }
      }

      public String load() {
         return this.init_Str;
      }
   }

   public record Inner_ConfigStore(URI init_URI, String init_Str, int init_I, String load, String read) {
      public String init_Str() {
         return this.init_URI.getScheme().toLowerCase(Locale.ROOT);
      }

      public String load() {
         return this.init_Str;
      }

      public String read() {
         return this.load;
      }

      public String write() {
         return this.read;
      }
   }

   private enum Inner_DataUtil {
      PUBLIC,
      PRIVATE,
      LOOPBACK,
      LINK_LOCAL,
      SPECIAL;
   }

   @FunctionalInterface
   public interface Inner_StreamUtil {
      List<InetAddress> resolve(String var1) throws UnknownHostException;
   }
}
