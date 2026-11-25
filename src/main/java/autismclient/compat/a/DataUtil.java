package autismclient.compat.a;

import autismclient.util.ProxyEntry;
import autismclient.util.ProxyManager;
import autismclient.util.ProxyType;
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

public final class DataUtil {
   private static final String init_Str = "viafabricplus-clientconnection";
   private static final String load = "vv:mod_details";
   private static final ThreadLocal<DataUtil.StreamUtil> init_ThreadLocal = new ThreadLocal<>();

   private DataUtil() {
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

   public static Socket init(String host, int port, DataUtil.BufferHelper original) throws IOException {
      DataUtil.StreamUtil scoped = init_ThreadLocal.get();
      if (scoped != null) {
         return init(
            scoped.init_InetSocketAddress().getAddress().getHostAddress(),
            scoped.init_InetSocketAddress().getPort(),
            scoped.init_DataUtil_ConfigSto(),
            (ignoredHost, ignoredPort) -> {
               Socket socket = init();

               try {
                  socket.setSoTimeout(3000);
                  socket.connect(scoped.init_InetSocketAddress(), 3000);
                  return socket;
               } catch (Throwable failure) {
                  socket.close();
                  throw failure;
               }
            }
         );
      }

      ProxyManager manager = ProxyManager.init_bY();
      DataUtil.ConfigStore route;
      synchronized (manager) {
         route = DataUtil.ConfigStore.init(manager.init_bW());
      }

      return init(host, port, route, original);
   }

   public static DataUtil.CachePool init(ProxyEntry proxy, InetSocketAddress address) throws IOException {
      return init(proxy, address, 10000L);
   }

   static DataUtil.CachePool init(ProxyEntry proxy, InetSocketAddress address, long timeoutMs) throws IOException {
      if (address == null || address.isUnresolved()) {
         throw new IOException("AUTO protocol destination is unresolved");
      }

      if (timeoutMs <= 0L) {
         throw new IOException("Invalid AUTO protocol deadline");
      }

      DataUtil.StreamUtil previous = init_ThreadLocal.get();
      DataUtil.StreamUtil current = new DataUtil.StreamUtil(DataUtil.ConfigStore.init(proxy), address);
      current.init_ScheduledFuture = DataUtil.Inner_DataUtil.init.schedule(current::init_V, timeoutMs, TimeUnit.MILLISECONDS);
      init_ThreadLocal.set(current);
      return () -> {
         current.init_V();
         if (previous == null) {
            init_ThreadLocal.remove();
         } else {
            init_ThreadLocal.set(previous);
         }
      };
   }

   private static Socket init() throws IOException {
      Socket socket = new Socket(Proxy.NO_PROXY);
      DataUtil.StreamUtil scope = init_ThreadLocal.get();
      if (scope != null) {
         scope.init(socket);
      }

      return socket;
   }

   static Socket init(String host, int port, ProxyEntry proxy, DataUtil.BufferHelper original) throws IOException {
      return init(host, port, DataUtil.ConfigStore.init(proxy), original);
   }

   private static Socket init(String host, int port, DataUtil.ConfigStore proxy, DataUtil.BufferHelper original) throws IOException, SocketException {
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
            byte[] username = proxy.load().getBytes(StandardCharsets.UTF_8);
            byte[] password = proxy.read().getBytes(StandardCharsets.UTF_8);
            Socket socket = null;
            boolean connected = false;

            try {
               if (username.length <= 255 && password.length <= 255 && proxy.load().indexOf(0) < 0) {
                  socket = init();
                  socket.setSoTimeout(3000);
                  socket.connect(new InetSocketAddress(proxy.init_Str(), proxy.init_I()), 3000);
                  DataInputStream input = new DataInputStream(socket.getInputStream());
                  OutputStream output = socket.getOutputStream();
                  if (proxy.init_bZ() == ProxyType.init) {
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
      Connection owner = init(userConnection);
      autismclient.g.BufferHelper.Inner_StreamUtil context = autismclient.g.BufferHelper.init_BufferHelper_Strea(owner);
      return context != null && context.init_BufferHelper_Cache() != autismclient.g.BufferHelper.Inner_CachePool.init
         ? !"vv:mod_details".equals(channel)
         : false;
   }

   private static Connection init(Object userConnection) {
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
   public interface BufferHelper {
      Socket open(String var1, int var2) throws IOException;
   }

   @FunctionalInterface
   public interface CachePool extends AutoCloseable {
      @Override
      void close();
   }

   private record ConfigStore(ProxyType init_bZ, String init_Str, int init_I, String load, String read) {
      private static DataUtil.ConfigStore init(ProxyEntry proxy) throws IOException {
         if (proxy == null) {
            return null;
         } else if (proxy.init_bZ != null && proxy.read != null && !proxy.read.isBlank() && proxy.init_I >= 1 && proxy.init_I <= 65535) {
            return new DataUtil.ConfigStore(
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

   private static final class Inner_DataUtil {
      private static final ScheduledThreadPoolExecutor init = init();

      private static ScheduledThreadPoolExecutor init() {
         ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, task -> {
            Thread thread = new Thread(task, "Autism protocol detection deadline");
            thread.setDaemon(true);
            return thread;
         });
         executor.setRemoveOnCancelPolicy(true);
         return executor;
      }
   }

   private static final class StreamUtil {
      private final DataUtil.ConfigStore init_DataUtil_ConfigSto;
      private final InetSocketAddress init_InetSocketAddress;
      private ScheduledFuture<?> init_ScheduledFuture;
      private Socket init_Socket;
      private boolean init_Z;

      private StreamUtil(DataUtil.ConfigStore proxy, InetSocketAddress address) {
         this.init_DataUtil_ConfigSto = proxy;
         this.init_InetSocketAddress = address;
      }

      private DataUtil.ConfigStore init_DataUtil_ConfigSto() {
         return this.init_DataUtil_ConfigSto;
      }

      private InetSocketAddress init_InetSocketAddress() {
         return this.init_InetSocketAddress;
      }

      private synchronized void init(Socket value) throws IOException {
         if (this.init_Z) {
            value.close();
            throw new IOException("AUTO protocol detection deadline expired");
         }

         this.init_Socket = value;
      }

      private synchronized void init_V() {
         this.init_Z = true;
         if (this.init_ScheduledFuture != null) {
            this.init_ScheduledFuture.cancel(false);
         }

         if (this.init_Socket != null) {
            try {
               this.init_Socket.close();
            } catch (IOException var2) {
            }

            this.init_Socket = null;
         }
      }
   }
}
