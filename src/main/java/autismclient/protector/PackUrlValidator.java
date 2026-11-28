package autismclient.protector;

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

   public static PackUrlValidator.ParsedTarget parseUrl(URL url) throws MalformedURLException {
      Objects.requireNonNull(url, "url");

      try {
         return parseUri(url.toURI());
      } catch (URISyntaxException e) {
         throw malformed(url.toExternalForm(), e);
      }
   }

   public static PackUrlValidator.ParsedTarget parseUri(URI uri) throws MalformedURLException {
      Objects.requireNonNull(uri, "uri");
      String text = uri.toASCIIString();
      if (text.equals(uri.toString()) && !hasUnsafeChars(text) && !uri.isOpaque()) {
         String scheme = uri.getScheme();
         if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            String rawAuthority = uri.getRawAuthority();
            if (rawAuthority != null && !rawAuthority.isBlank() && uri.getRawUserInfo() == null && rawAuthority.indexOf(64) < 0) {
               String host = uri.getHost();
               if (host != null && !host.isBlank()) {
                  host = stripBrackets(host);
                  if (host.isBlank() || hasUnsafeChars(host) || host.indexOf(37) >= 0 || host.indexOf(47) >= 0) {
                     throw malformed(text, null);
                  }

                  if (host.chars().anyMatch(c -> c > 127)) {
                     throw malformed(text, null);
                  }

                  if (host.matches("[0-9.]+") && parseIpv4(host) == null) {
                     throw malformed(text, null);
                  }

                  if (host.indexOf(58) >= 0) {
                     if (!hasBracketedHost(rawAuthority) || host.indexOf(46) >= 0) {
                        throw malformed(text, null);
                     }

                     try {
                        InetAddress numeric = InetAddress.getByName(host);
                        if (!(numeric instanceof Inet6Address)) {
                           throw malformed(text, null);
                        }
                     } catch (UnknownHostException e) {
                        throw malformed(text, e);
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

                     String authority = formatHostPort(host, port, scheme.equalsIgnoreCase("https") ? 443 : 80);
                     return new PackUrlValidator.ParsedTarget(uri, host, port, authority, requestTarget);
                  } else {
                     throw malformed(text, null);
                  }
               } else {
                  throw malformed(text, null);
               }
            } else {
               throw malformed(text, null);
            }
         } else {
            throw new MalformedURLException("Only HTTP and HTTPS resource-pack URLs are accepted");
         }
      } else {
         throw malformed(text, null);
      }
   }

   public static PackUrlValidator.ResolvedEndpoint resolveUrlEndpoint(URL url, ResourcePackDownloadScope.Snapshot snapshot, PackUrlValidator.HostResolver resolver) throws IOException {
      return resolveEndpoint(parseUrl(url), snapshot, resolver);
   }

   public static PackUrlValidator.ResolvedEndpoint resolveEndpoint(
      PackUrlValidator.ParsedTarget target, ResourcePackDownloadScope.Snapshot snapshot, PackUrlValidator.HostResolver resolver
   ) throws IOException {
      Objects.requireNonNull(snapshot, "snapshot");
      List<InetAddress> addresses = resolveHost(target.load(), resolver);
      PackUrlValidator.AddressKind kind = classifyAddresses(addresses);

      for (InetAddress address : addresses) {
         if (!isAllowedAddress(address, target.port(), kind, snapshot)) {
            throw new IOException("Resource-pack address is outside the connected server's allowed scope");
         }
      }

      return new PackUrlValidator.ResolvedEndpoint(target.uri(), target.load(), target.port(), addresses);
   }

   public static PackUrlValidator.PackRoute routeFor(Proxy proxy, PackUrlValidator.HostResolver resolver) throws IOException {
      if (proxy != null && proxy.type() != Type.DIRECT) {
         if (proxy.address() instanceof InetSocketAddress socketAddress) {
            PackUrlValidator.ParsedTarget target = targetFromSocket(socketAddress);
            List<InetAddress> addresses = resolveHost(target.load(), resolver);
            PackUrlValidator.AddressKind kind = classifyAddresses(addresses);
            if (kind == PackUrlValidator.AddressKind.SPECIAL) {
               throw new IOException("The configured proxy address is not a unicast address");
            } else {
               return new PackUrlValidator.PackRoute(
                  proxy.type(),
                  new PackUrlValidator.ResolvedEndpoint(target.uri(), target.load(), target.port(), addresses),
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

   public static PackUrlValidator.PackRoute routeForLocation(
      String location, ResourcePackDownloadScope.Snapshot snapshot, PackUrlValidator.HostResolver resolver
   ) throws IOException, MalformedURLException {
      if (location != null && !location.isBlank()) {
         URI uri;
         try {
            uri = new URI(location);
         } catch (URISyntaxException e) {
            throw malformed(location, e);
         }

         PackUrlValidator.ParsedTarget target = parseUri(uri);
         if (!target.init_Str().equals("http")
            || uri.getRawQuery() != null
            || uri.getRawFragment() != null
            || uri.getRawPath() != null && !uri.getRawPath().isEmpty() && !uri.getRawPath().equals("/")) {
            throw new IOException("HTTP 305 requires an unambiguous HTTP proxy URI");
         }

         PackUrlValidator.ResolvedEndpoint endpoint = resolveEndpoint(target, snapshot, resolver);
         return new PackUrlValidator.PackRoute(Type.HTTP, endpoint, "http://" + target.read(), false);
      } else {
         throw new IOException("HTTP 305 has no proxy URI");
      }
   }

   public static String endpointKey(ConnectionPrivacyRegistry.ServerEndpoint endpoint) throws IOException {
      Objects.requireNonNull(endpoint, "endpoint");
      String host = endpoint.host();
      if (host != null && !host.isBlank() && !hasUnsafeChars(host) && host.indexOf(37) < 0) {
         host = stripBrackets(host.trim());

         while (host.endsWith(".") && host.length() > 1) {
            host = host.substring(0, host.length() - 1);
         }

         if (host.isEmpty()) {
            throw new IOException("Invalid original Minecraft origin");
         }

         int port = endpoint.port() == 0 ? 25565 : endpoint.port();
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
                  if (host.matches("[0-9.]+") && parseIpv4(host) == null) {
                     throw new IOException("Invalid original numeric origin");
                  }

                  host = IDN.toASCII(host, 2).toLowerCase(Locale.ROOT);
               }
            } catch (IllegalArgumentException | UnknownHostException e) {
               throw new IOException("Invalid original Minecraft origin", e);
            }

            return formatHostPort(host, port, 25565);
         } else {
            throw new IOException("Invalid original Minecraft port");
         }
      } else {
         throw new IOException("Invalid original Minecraft origin");
      }
   }

   public static String peerKey(SocketAddress socketAddress) throws IOException {
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
         if (!local.isBlank() && !hasUnsafeChars(local)) {
            return "local:" + local;
         } else {
            throw new IOException("The connected local peer has no stable identity");
         }
      }
   }

   public static String formatAuthority(InetAddress address, int port, String scheme) {
      int defaultPort = scheme.equalsIgnoreCase("https") ? 443 : 80;
      return formatHostPort(address.getHostAddress(), port, defaultPort);
   }

   public static boolean sameAuthority(PackUrlValidator.ParsedTarget first, PackUrlValidator.ParsedTarget second) {
      return first.init_Str().equals(second.init_Str()) && first.load().equalsIgnoreCase(second.load()) && first.port() == second.port();
   }

   private static PackUrlValidator.ParsedTarget targetFromSocket(InetSocketAddress address) throws IOException {
      String host = address.getHostString();
      int port = address.getPort();
      if (host != null && !host.isBlank() && port >= 1 && port <= 65535) {
         String literal = host.indexOf(58) >= 0 && !host.startsWith("[") ? "[" + host + "]" : host;

         try {
            return parseUri(new URI("http://" + literal + ":" + port + "/"));
         } catch (URISyntaxException e) {
            throw malformed(address.toString(), e);
         }
      } else {
         throw new IOException("Invalid configured proxy endpoint");
      }
   }

   private static List<InetAddress> resolveHost(String host, PackUrlValidator.HostResolver resolver) throws UnknownHostException {
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

   private static PackUrlValidator.AddressKind classifyAddresses(List<InetAddress> addresses) throws IOException {
      PackUrlValidator.AddressKind kind = classifyAddress(addresses.getFirst());

      for (InetAddress address : addresses) {
         if (classifyAddress(address) != kind) {
            throw new IOException("Mixed DNS answer categories are not accepted");
         }
      }

      if (kind == PackUrlValidator.AddressKind.SPECIAL) {
         throw new IOException("Special-use DNS addresses are not accepted");
      } else {
         return kind;
      }
   }

   private static boolean isAllowedAddress(InetAddress address, int port, PackUrlValidator.AddressKind kind, ResourcePackDownloadScope.Snapshot snapshot) {
      if (kind == PackUrlValidator.AddressKind.PUBLIC) {
         return true;
      }

      if (snapshot.isLocal()) {
         return kind == PackUrlValidator.AddressKind.LOOPBACK;
      }

      if (snapshot.peerAddress() instanceof InetSocketAddress peer && peer.getAddress() != null) {
         boolean sameIp = Arrays.equals(peer.getAddress().getAddress(), address.getAddress());
         if (!sameIp) {
            return false;
         } else {
            return snapshot.serverType() == net.minecraft.client.multiplayer.ServerData.Type.LAN
               ? kind == PackUrlValidator.AddressKind.PRIVATE || kind == PackUrlValidator.AddressKind.LINK_LOCAL
               : kind == PackUrlValidator.AddressKind.PRIVATE && port == peer.getPort();
         }
      } else {
         return false;
      }
   }

   private static PackUrlValidator.AddressKind classifyAddress(InetAddress address) {
      byte[] bytes = address.getAddress();
      if (address.isAnyLocalAddress() || address.isMulticastAddress() || init_Z_BArr(bytes) || init_Z_InetAddress_BArr(address, bytes)) {
         return PackUrlValidator.AddressKind.SPECIAL;
      } else if (address.isLoopbackAddress()) {
         return PackUrlValidator.AddressKind.LOOPBACK;
      } else if (address.isLinkLocalAddress()) {
         return PackUrlValidator.AddressKind.LINK_LOCAL;
      } else if (address.isSiteLocalAddress() || isCarrierGradeNat(bytes) || isUniqueLocalIpv6(bytes)) {
         return PackUrlValidator.AddressKind.PRIVATE;
      } else if (address instanceof Inet4Address) {
         return PackUrlValidator.AddressKind.PUBLIC;
      } else {
         return address instanceof Inet6Address ? PackUrlValidator.AddressKind.PUBLIC : PackUrlValidator.AddressKind.SPECIAL;
      }
   }

   private static boolean init_Z_BArr(byte[] b) {
      if (b.length != 16) {
         return false;
      } else if (hasPrefix(b, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 255)) {
         return true;
      } else {
         return !hasPrefix(b, 0, 100, 255, 155) && !hasPrefix(b, 0, 100, 255, 155, 1) ? b[0] == 32 && b[1] == 2 : true;
      }
   }

   private static boolean init_Z_InetAddress_BArr(InetAddress address, byte[] b) {
      if (!(address instanceof Inet4Address)) {
         if (b.length != 16) {
            return true;
         } else if (hasPrefix(b, 32, 1, 13, 184)) {
            return true;
         } else {
            return b[0] == 32 && b[1] == 1 && (unsigned(b[2]) & 254) == 0 ? true : hasPrefix(b, 1, 0) && isZeroRange(b, 2, 12);
         }
      } else {
         int a = unsigned(b[0]);
         int second = unsigned(b[1]);
         int third = unsigned(b[2]);
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

   private static boolean isCarrierGradeNat(byte[] b) {
      return b.length == 4 && unsigned(b[0]) == 100 && (unsigned(b[1]) & 192) == 64;
   }

   private static boolean isUniqueLocalIpv6(byte[] bytes) {
      return bytes.length == 16 && (bytes[0] & 254) == 252;
   }

   private static boolean hasPrefix(byte[] value, int... prefix) {
      if (value.length < prefix.length) {
         return false;
      }

      for (int i = 0; i < prefix.length; i++) {
         if (unsigned(value[i]) != prefix[i]) {
            return false;
         }
      }

      return true;
   }

   private static boolean isZeroRange(byte[] value, int from, int to) {
      for (int i = from; i < to; i++) {
         if (value[i] != 0) {
            return false;
         }
      }

      return true;
   }

   private static int unsigned(byte value) {
      return value & 0xFF;
   }

   private static byte[] parseIpv4(String host) {
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

   private static boolean hasUnsafeChars(String value) {
      for (int i = 0; i < value.length(); i++) {
         char c = value.charAt(i);
         if (c <= ' ' || c == 127 || c == '\\') {
            return true;
         }
      }

      return false;
   }

   private static boolean hasBracketedHost(String rawAuthority) {
      String hostPort = rawAuthority.substring(rawAuthority.lastIndexOf(64) + 1);
      return hostPort.startsWith("[") && hostPort.indexOf(93) > 0;
   }

   private static String stripBrackets(String host) {
      return host.startsWith("[") && host.endsWith("]") ? host.substring(1, host.length() - 1) : host;
   }

   private static String formatHostPort(String host, int port, int defaultPort) {
      String formatted = host.indexOf(58) >= 0 ? "[" + host + "]" : host;
      return port == defaultPort ? formatted : formatted + ":" + port;
   }

   private static MalformedURLException malformed(String value, Exception cause) {
      MalformedURLException exception = new MalformedURLException("Invalid resource-pack URL: " + value);
      if (cause != null) {
         exception.initCause(cause);
      }

      return exception;
   }

   public record PackRoute(Type proxyType, PackUrlValidator.ResolvedEndpoint endpoint, String init_Str, boolean viaProxy) {
   }

   public record ResolvedEndpoint(URI uri, String init_Str, int port, List<InetAddress> addresses) {
      public ResolvedEndpoint {
         addresses = List.copyOf(addresses);
      }

      public String init_Str() {
         return this.uri.getScheme().toLowerCase(Locale.ROOT);
      }

      public PackUrlValidator.ParsedTarget parsedTarget() {
         try {
            return PackUrlValidator.parseUri(this.uri);
         } catch (IOException e) {
            throw new IllegalStateException(e);
         }
      }

      public String host() {
         return this.init_Str;
      }
   }

   public record ParsedTarget(URI uri, String init_Str, int port, String load, String read) {
      public String init_Str() {
         return this.uri.getScheme().toLowerCase(Locale.ROOT);
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

   private enum AddressKind {
      PUBLIC,
      PRIVATE,
      LOOPBACK,
      LINK_LOCAL,
      SPECIAL;
   }

   @FunctionalInterface
   public interface HostResolver {
      List<InetAddress> resolve(String var1) throws UnknownHostException;
   }
}
