package autismclient.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public final class ProxyConnectivityTester {
   private ProxyConnectivityTester() {
   }

   public static ProxyConnectivityTester.TestResult init(bW proxy, String destHost, int destPort, int timeoutMs) {
      if (proxy != null && proxy.init_bZ != null && proxy.init_Z()) {
         String host = destHost == null ? "" : destHost.trim();
         if (!host.isBlank() && destPort > 0 && destPort <= 65535 && host.getBytes(StandardCharsets.UTF_8).length <= 255 && !hasOversizedCredentials(proxy)) {
            int timeout = Math.max(1, timeoutMs);
            ProxyProtocolType primary = proxy.init_bZ == ProxyProtocolType.Socks4 ? ProxyProtocolType.Socks4 : ProxyProtocolType.Socks5;
            ProxyProtocolType secondary = primary == ProxyProtocolType.Socks4 ? ProxyProtocolType.Socks5 : ProxyProtocolType.Socks4;
            ProxyConnectivityTester.TestResult first = runTest(proxy, primary, host, destPort, timeout);
            if (first.success()) {
               return first;
            } else {
               ProxyConnectivityTester.TestResult second = runTest(proxy, secondary, host, destPort, timeout);
               if (second.success()) {
                  return second;
               } else {
                  return (first.outcome() == ProxyConnectivityTester.TestOutcome.REFUSED || first.outcome() == ProxyConnectivityTester.TestOutcome.TIMEOUT)
                        && second.outcome() == ProxyConnectivityTester.TestOutcome.OFFLINE
                     ? first
                     : second;
               }
            }
         } else {
            return ProxyConnectivityTester.TestResult.of(ProxyConnectivityTester.TestOutcome.INVALID);
         }
      } else {
         return ProxyConnectivityTester.TestResult.of(ProxyConnectivityTester.TestOutcome.INVALID);
      }
   }

   private static boolean hasOversizedCredentials(bW proxy) {
      if (proxy.write != null && !proxy.write.isBlank()) {
         byte[] user = proxy.write.getBytes(StandardCharsets.UTF_8);
         byte[] pass = (proxy.update == null ? "" : proxy.update).getBytes(StandardCharsets.UTF_8);
         return user.length > 255 || pass.length > 255;
      } else {
         return false;
      }
   }

   private static ProxyConnectivityTester.TestResult runTest(bW proxy, ProxyProtocolType type, String host, int port, int timeout) {
      long started = System.nanoTime();

      try (Socket socket = new Socket()) {
         socket.setSoTimeout(timeout);
         socket.connect(new InetSocketAddress(proxy.read, proxy.init_I), timeout);
         OutputStream out = socket.getOutputStream();
         InputStream in = socket.getInputStream();
         ProxyConnectivityTester.TestOutcome state = type == ProxyProtocolType.Socks4 ? socks4Handshake(out, in, proxy, host, port) : socks5Handshake(out, in, proxy, host, port);
         if (state != ProxyConnectivityTester.TestOutcome.VERIFIED) {
            return ProxyConnectivityTester.TestResult.of(state);
         }

         long latency = Math.max(0L, (System.nanoTime() - started) / 1000000L);
         return new ProxyConnectivityTester.TestResult(true, latency, type, ProxyConnectivityTester.TestOutcome.VERIFIED);
      } catch (SocketTimeoutException ignored) {
         return ProxyConnectivityTester.TestResult.of(ProxyConnectivityTester.TestOutcome.TIMEOUT);
      } catch (ConnectException ignored) {
         return ProxyConnectivityTester.TestResult.of(ProxyConnectivityTester.TestOutcome.REFUSED);
      } catch (IOException | RuntimeException ignored) {
         return ProxyConnectivityTester.TestResult.of(ProxyConnectivityTester.TestOutcome.OFFLINE);
      }
   }

   private static ProxyConnectivityTester.TestOutcome socks5Handshake(OutputStream out, InputStream in, bW proxy, String host, int port) throws IOException {
      boolean hasAuth = proxy.write != null && !proxy.write.isBlank();
      if (hasAuth) {
         out.write(new byte[]{5, 2, 0, 2});
      } else {
         out.write(new byte[]{5, 1, 0});
      }

      out.flush();
      byte[] method = readExactly(in, 2);
      if (method.length >= 2 && method[0] == 5) {
         int chosen = method[1] & 255;
         if (chosen == 255) {
            return ProxyConnectivityTester.TestOutcome.REFUSED;
         }

         if (chosen == 2) {
            if (!hasAuth) {
               return ProxyConnectivityTester.TestOutcome.OFFLINE;
            }

            ProxyConnectivityTester.TestOutcome authenticated = socks5Auth(out, in, proxy);
            if (authenticated != ProxyConnectivityTester.TestOutcome.VERIFIED) {
               return authenticated;
            }
         } else if (chosen != 0) {
            return ProxyConnectivityTester.TestOutcome.OFFLINE;
         }

         byte[] domain = host.getBytes(StandardCharsets.UTF_8);
         ByteArrayOutputStream request = new ByteArrayOutputStream(7 + domain.length);
         request.write(5);
         request.write(1);
         request.write(0);
         request.write(3);
         request.write(domain.length);
         request.write(domain, 0, domain.length);
         request.write(port >> 8 & 0xFF);
         request.write(port & 0xFF);
         out.write(request.toByteArray());
         out.flush();
         byte[] reply = readExactly(in, 4);
         if (reply.length >= 4 && reply[0] == 5 && reply[2] == 0) {
            byte var10000;
            switch (reply[3] & 0xFF) {
               case 1:
                  var10000 = 4;
                  break;
               case 2:
               default:
                  var10000 = -1;
                  break;
               case 3:
                  byte[] length = readExactly(in, 1);
                  var10000 = (byte)(length.length >= 1 && (length[0] & 255) != 0 ? length[0] & 0xFF : -1);
                  break;
               case 4:
                  var10000 = 16;
            }

            int addressLength = var10000;
            if (addressLength >= 0 && readExactly(in, addressLength + 2).length >= addressLength + 2) {
               return reply[1] == 0 ? ProxyConnectivityTester.TestOutcome.VERIFIED : ProxyConnectivityTester.TestOutcome.REFUSED;
            } else {
               return ProxyConnectivityTester.TestOutcome.OFFLINE;
            }
         } else {
            return ProxyConnectivityTester.TestOutcome.OFFLINE;
         }
      } else {
         return ProxyConnectivityTester.TestOutcome.OFFLINE;
      }
   }

   private static ProxyConnectivityTester.TestOutcome socks5Auth(OutputStream out, InputStream in, bW proxy) throws IOException {
      byte[] user = (proxy.write == null ? "" : proxy.write).getBytes(StandardCharsets.UTF_8);
      byte[] pass = (proxy.update == null ? "" : proxy.update).getBytes(StandardCharsets.UTF_8);
      ByteArrayOutputStream auth = new ByteArrayOutputStream(3 + user.length + pass.length);
      auth.write(1);
      auth.write(user.length);
      auth.write(user, 0, user.length);
      auth.write(pass.length);
      auth.write(pass, 0, pass.length);
      out.write(auth.toByteArray());
      out.flush();
      byte[] reply = readExactly(in, 2);
      if (reply.length >= 2 && reply[0] == 1) {
         return reply[1] == 0 ? ProxyConnectivityTester.TestOutcome.VERIFIED : ProxyConnectivityTester.TestOutcome.REFUSED;
      } else {
         return ProxyConnectivityTester.TestOutcome.OFFLINE;
      }
   }

   private static ProxyConnectivityTester.TestOutcome socks4Handshake(OutputStream out, InputStream in, bW proxy, String host, int port) throws IOException {
      byte[] user = (proxy.write == null ? "" : proxy.write).getBytes(StandardCharsets.UTF_8);
      ByteArrayOutputStream request = new ByteArrayOutputStream();
      request.write(4);
      request.write(1);
      request.write(port >> 8 & 0xFF);
      request.write(port & 0xFF);
      byte[] ipv4 = parseIpv4(host);
      if (ipv4 != null) {
         request.write(ipv4, 0, 4);
         request.write(user, 0, user.length);
         request.write(0);
      } else {
         request.write(new byte[]{0, 0, 0, 1});
         request.write(user, 0, user.length);
         request.write(0);
         byte[] domain = host.getBytes(StandardCharsets.UTF_8);
         request.write(domain, 0, domain.length);
         request.write(0);
      }

      out.write(request.toByteArray());
      out.flush();
      byte[] reply = readExactly(in, 8);
      if (reply.length >= 8 && reply[0] == 0) {
         return reply[1] == 90 ? ProxyConnectivityTester.TestOutcome.VERIFIED : ProxyConnectivityTester.TestOutcome.REFUSED;
      } else {
         return ProxyConnectivityTester.TestOutcome.OFFLINE;
      }
   }

   private static byte[] readExactly(InputStream in, int count) throws IOException {
      return in.readNBytes(count);
   }

   private static byte[] parseIpv4(String value) {
      if (value == null) {
         return null;
      }

      String[] parts = value.split("\\.");
      if (parts.length != 4) {
         return null;
      }

      byte[] out = new byte[4];

      for (int i = 0; i < 4; i++) {
         try {
            int octet = Integer.parseInt(parts[i]);
            if (octet < 0 || octet > 255) {
               return null;
            }

            out[i] = (byte)octet;
         } catch (NumberFormatException e) {
            return null;
         }
      }

      return out;
   }

   public record TestResult(boolean success, long latencyMs, ProxyProtocolType protocol, ProxyConnectivityTester.TestOutcome outcome) {
      public TestResult(boolean ok, long latencyMs, ProxyProtocolType workingType) {
         this(ok, latencyMs, workingType, ok ? ProxyConnectivityTester.TestOutcome.VERIFIED : ProxyConnectivityTester.TestOutcome.OFFLINE);
      }

      public TestResult(boolean ok, long latencyMs) {
         this(ok, latencyMs, null);
      }

      static ProxyConnectivityTester.TestResult of(ProxyConnectivityTester.TestOutcome state) {
         return new ProxyConnectivityTester.TestResult(false, 0L, null, state);
      }
   }

   public enum TestOutcome {
      INVALID,
      OFFLINE,
      TIMEOUT,
      REFUSED,
      VERIFIED;
   }
}
