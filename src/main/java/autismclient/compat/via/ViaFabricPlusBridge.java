package autismclient.compat.via;

import autismclient.util.bW;
import autismclient.util.ProxyListManager;
import autismclient.util.ProxyProtocolType;
import com.google.common.net.InetAddresses;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.IDN;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;

public final class ViaFabricPlusBridge {
   private static final String init_Str = "viafabricplus-clientconnection";
   private static final String load = "vv:mod_details";
   private static final ThreadLocal<ViaFabricPlusBridge.DetectionAttempt> CURRENT_ATTEMPT = new ThreadLocal<>();

   private ViaFabricPlusBridge() {
   }

   public static void init_V(Connection connection) {
   }

   public static boolean init_Z(Connection connection) {
      if (connection != null && !connection.isMemoryConnection()) {
         try {
            Object target = connection.getClass().getMethod("viaFabricPlus$getTargetVersion").invoke(connection);
            if (target == null) {
               return false;
            }

            int protocol = ((Number)target.getClass().getMethod("getVersion").invoke(target)).intValue();
            return protocol >= 0 && protocol < SharedConstants.getCurrentVersion().protocolVersion();
         } catch (ReflectiveOperationException | RuntimeException | LinkageError absent) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static Socket connect(String host, int port, ViaFabricPlusBridge.SocketOpener original) throws IOException {
      ViaFabricPlusBridge.DetectionAttempt scoped = CURRENT_ATTEMPT.get();
      if (scoped != null) {
         return connectVia(
            scoped.getAddress().getAddress().getHostAddress(),
            scoped.getAddress().getPort(),
            scoped.getProxy(),
            (ignoredHost, ignoredPort) -> {
               Socket socket = openSocket();

               try {
                  socket.setSoTimeout(3000);
                  socket.connect(scoped.getAddress(), 3000);
                  return socket;
               } catch (Throwable failure) {
                  socket.close();
                  throw failure;
               }
            }
         );
      }

      ProxyListManager manager = ProxyListManager.getInstance();
      ViaFabricPlusBridge.ProxyRoute route;
      synchronized (manager) {
         route = ViaFabricPlusBridge.ProxyRoute.of(manager.getActiveProxy());
      }

      return connectVia(host, port, route, original);
   }

   public static ViaFabricPlusBridge.DetectionScope detect(bW proxy, InetSocketAddress address) throws IOException {
      return detect(proxy, address, 10000L);
   }

   static ViaFabricPlusBridge.DetectionScope detect(bW proxy, InetSocketAddress address, long timeoutMs) throws IOException {
      if (address == null || address.isUnresolved()) {
         throw new IOException("AUTO protocol destination is unresolved");
      }

      if (timeoutMs <= 0L) {
         throw new IOException("Invalid AUTO protocol deadline");
      }

      ViaFabricPlusBridge.DetectionAttempt previous = CURRENT_ATTEMPT.get();
      ViaFabricPlusBridge.DetectionAttempt current = new ViaFabricPlusBridge.DetectionAttempt(ViaFabricPlusBridge.ProxyRoute.of(proxy), address);
      current.deadline = ViaFabricPlusBridge.DeadlineScheduler.EXECUTOR.schedule(current::abort, timeoutMs, TimeUnit.MILLISECONDS);
      CURRENT_ATTEMPT.set(current);
      return () -> {
         current.abort();
         if (previous == null) {
            CURRENT_ATTEMPT.remove();
         } else {
            CURRENT_ATTEMPT.set(previous);
         }
      };
   }

   private static Socket openSocket() throws IOException {
      Socket socket = new Socket(Proxy.NO_PROXY);
      ViaFabricPlusBridge.DetectionAttempt scope = CURRENT_ATTEMPT.get();
      if (scope != null) {
         scope.attach(socket);
      }

      return socket;
   }

   static Socket connect(String host, int port, bW proxy, ViaFabricPlusBridge.SocketOpener original) throws IOException {
      return connectVia(host, port, ViaFabricPlusBridge.ProxyRoute.of(proxy), original);
   }

   private static Socket connectVia(String host, int port, ViaFabricPlusBridge.ProxyRoute proxy, ViaFabricPlusBridge.SocketOpener original) throws IOException, SocketException {
      if (proxy == null) {
         return original.open(host, port);
      }

      if (host != null && !host.isBlank() && port >= 1 && port <= 65535) {
         String target = host.startsWith("[") && host.endsWith("]") ? host.substring(1, host.length() - 1) : host;
         boolean literal = InetAddresses.isInetAddress(target);

         byte[] address;
         try {
            address = literal ? InetAddresses.forString(target).getAddress() : IDN.toASCII(target).getBytes(StandardCharsets.US_ASCII);
         } catch (IllegalArgumentException failure) {
            throw new IOException("Invalid AUTO protocol destination", failure);
         }

         if (address.length >= 1 && address.length <= 255) {
            byte[] username = proxy.username().getBytes(StandardCharsets.UTF_8);
            byte[] password = proxy.password().getBytes(StandardCharsets.UTF_8);
            Socket socket = null;
            boolean connected = false;

            try {
               if (username.length <= 255 && password.length <= 255 && proxy.username().indexOf(0) < 0) {
                  socket = openSocket();
                  socket.setSoTimeout(3000);
                  socket.connect(new InetSocketAddress(proxy.host(), proxy.port()), 3000);
                  DataInputStream input = new DataInputStream(socket.getInputStream());
                  OutputStream output = socket.getOutputStream();
                  if (proxy.type() == ProxyProtocolType.Socks4) {
                     if (literal && address.length != 4) {
                        throw new IOException("SOCKS4 does not support IPv6 destinations");
                     }

                     output.write(new byte[]{4, 1, (byte)(port >>> 8), (byte)port});
                     output.write(literal ? address : new byte[]{0, 0, 0, 1});
                     output.write(username);
                     output.write(0);
                     if (!literal) {
                        output.write(address);
                        output.write(0);
                     }

                     output.flush();
                     int version = input.readUnsignedByte();
                     int status = input.readUnsignedByte();
                     input.readFully(new byte[6]);
                     if (version != 0 || status != 90) {
                        throw new IOException("SOCKS4 AUTO connection refused");
                     }
                  } else {
                     boolean authenticated = username.length != 0 || password.length != 0;
                     output.write(authenticated ? new byte[]{5, 2, 0, 2} : new byte[]{5, 1, 0});
                     output.flush();
                     if (input.readUnsignedByte() != 5) {
                        throw new IOException("Invalid SOCKS5 AUTO greeting");
                     }

                     int method = input.readUnsignedByte();
                     if (method == 2 && authenticated && username.length > 0 && password.length > 0) {
                        output.write(1);
                        output.write(username.length);
                        output.write(username);
                        output.write(password.length);
                        output.write(password);
                        output.flush();
                        if (input.readUnsignedByte() != 1 || input.readUnsignedByte() != 0) {
                           throw new IOException("SOCKS5 AUTO authentication refused");
                        }
                     } else if (method != 0) {
                        throw new IOException("SOCKS5 AUTO authentication refused");
                     }

                     output.write(new byte[]{5, 1, 0, (byte)(literal ? (address.length == 4 ? 1 : 4) : 3)});
                     if (!literal) {
                        output.write(address.length);
                     }

                     output.write(address);
                     output.write(port >>> 8);
                     output.write(port);
                     output.flush();
                     if (input.readUnsignedByte() != 5 || input.readUnsignedByte() != 0 || input.readUnsignedByte() != 0) {
                        throw new IOException("SOCKS5 AUTO connection refused");
                     }

                     int var10000;
                     switch (input.readUnsignedByte()) {
                        case 1:
                           var10000 = 4;
                           break;
                        case 2:
                        default:
                           throw new IOException("Invalid SOCKS5 AUTO bind address");
                        case 3:
                           var10000 = input.readUnsignedByte();
                           break;
                        case 4:
                           var10000 = 16;
                     }

                     int boundSize = var10000;
                     if (boundSize == 0) {
                        throw new IOException("Invalid SOCKS5 AUTO bind address");
                     }

                     input.readFully(new byte[boundSize + 2]);
                  }

                  connected = true;
                  return socket;
               } else {
                  throw new IOException("Invalid AUTO proxy credentials");
               }
            } finally {
               Arrays.fill(username, (byte)0);
               Arrays.fill(password, (byte)0);
               if (!connected && socket != null) {
                  socket.close();
               }
            }
         } else {
            throw new IOException("Invalid AUTO protocol destination");
         }
      } else {
         throw new IOException("Invalid AUTO protocol destination");
      }
   }

   public static boolean init(Object userConnection, String channel, byte[] data) {
      Connection owner = connectionOf(userConnection);
      autismclient.protector.ConnectionPrivacyRegistry.ConnectionSession context = autismclient.protector.ConnectionPrivacyRegistry.getActive(owner);
      return context != null && context.privacyMode() != autismclient.protector.ConnectionPrivacyRegistry.PrivacyMode.VANILLA
         ? !"vv:mod_details".equals(channel)
         : false;
   }

   private static Connection connectionOf(Object userConnection) {
      if (userConnection == null) {
         return null;
      }

      try {
         Method getChannel = userConnection.getClass().getMethod("getChannel");
         if (getChannel.invoke(userConnection) instanceof Channel channel) {
            return channel.attr(AttributeKey.valueOf("viafabricplus-clientconnection")).get() instanceof Connection connection ? connection : null;
         } else {
            return null;
         }
      } catch (Throwable ignored) {
         return null;
      }
   }

   @FunctionalInterface
   public interface SocketOpener {
      Socket open(String var1, int var2) throws IOException;
   }

   @FunctionalInterface
   public interface DetectionScope extends AutoCloseable {
      @Override
      void close();
   }

   private record ProxyRoute(ProxyProtocolType type, String host, int port, String username, String password) {
      private static ViaFabricPlusBridge.ProxyRoute of(bW proxy) throws IOException {
         if (proxy == null) {
            return null;
         } else if (proxy.init_bZ != null && proxy.read != null && !proxy.read.isBlank() && proxy.init_I >= 1 && proxy.init_I <= 65535) {
            return new ViaFabricPlusBridge.ProxyRoute(
               proxy.init_bZ, proxy.read, proxy.init_I, proxy.write == null ? "" : proxy.write, proxy.update == null ? "" : proxy.update
            );
         } else {
            throw new IOException("Invalid AUTO proxy endpoint");
         }
      }

      @Override
      public String toString() {
         return "AUTO proxy route";
      }
   }

   private static final class DeadlineScheduler {
      private static final ScheduledThreadPoolExecutor EXECUTOR = createExecutor();

      private static ScheduledThreadPoolExecutor createExecutor() {
         ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, task -> {
            Thread thread = new Thread(task, "Autism protocol detection deadline");
            thread.setDaemon(true);
            return thread;
         });
         executor.setRemoveOnCancelPolicy(true);
         return executor;
      }
   }

   private static final class DetectionAttempt {
      private final ViaFabricPlusBridge.ProxyRoute proxy;
      private final InetSocketAddress address;
      private ScheduledFuture<?> deadline;
      private Socket socket;
      private boolean closed;

      private DetectionAttempt(ViaFabricPlusBridge.ProxyRoute proxy, InetSocketAddress address) {
         this.proxy = proxy;
         this.address = address;
      }

      private ViaFabricPlusBridge.ProxyRoute getProxy() {
         return this.proxy;
      }

      private InetSocketAddress getAddress() {
         return this.address;
      }

      private synchronized void attach(Socket value) throws IOException {
         if (this.closed) {
            value.close();
            throw new IOException("AUTO protocol detection deadline expired");
         }

         this.socket = value;
      }

      private synchronized void abort() {
         this.closed = true;
         if (this.deadline != null) {
            this.deadline.cancel(false);
         }

         if (this.socket != null) {
            try {
               this.socket.close();
            } catch (IOException var2) {
            }

            this.socket = null;
         }
      }
   }
}
