package autismclient.util.g;

import autismclient.util.bZ;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class ArrayUtil {
   private static final ConcurrentMap<Thread, ArrayUtil.Inner_DataUtil> init_ConcurrentMap = new ConcurrentHashMap<>();
   private static ArrayUtil.Inner_CachePool init_ArrayUtil_CachePoo;

   private ArrayUtil() {
   }

   static Proxy init_Proxy(ConfigStore.Inner_DataUtil spec) throws IOException {
      if (spec == null) {
         return Proxy.NO_PROXY;
      }

      ArrayUtil.Inner_StreamUtil settings = init_ArrayUtil_StreamUt(spec);
      return new Proxy(Type.SOCKS, InetSocketAddress.createUnresolved(settings.init_Str(), settings.init_I()));
   }

   static ArrayUtil.Inner_DataUtil init_ArrayUtil_DataUtil(ConfigStore.Inner_DataUtil spec) throws IOException {
      if (spec == null) {
         return null;
      }

      ArrayUtil.Inner_StreamUtil settings = init_ArrayUtil_StreamUt(spec);
      synchronized (Authenticator.class) {
         if (init_ArrayUtil_CachePoo == null) {
            try {
               init_ArrayUtil_CachePoo = new ArrayUtil.Inner_CachePool(Authenticator.getDefault());
               Authenticator.setDefault(init_ArrayUtil_CachePoo);
            } catch (SecurityException failure) {
               init_ArrayUtil_CachePoo = null;
               throw new IOException("Cannot install Multi resource-pack authentication", failure);
            }
         } else if (Authenticator.getDefault() != init_ArrayUtil_CachePoo) {
            throw new IOException("Multi resource-pack authentication was replaced");
         }
      }

      ArrayUtil.Inner_DataUtil scope = new ArrayUtil.Inner_DataUtil(Thread.currentThread(), settings);
      if (init_ConcurrentMap.putIfAbsent(scope.init_Thread, scope) != null) {
         throw new IOException("Multi resource-pack authentication is already active on this worker");
      }

      try {
         autismclient.g.UiHelper.Inner_BufferHelper snapshot = autismclient.g.UiHelper.load();
         if (settings.init_Z() && snapshot != null) {
            scope.init_UiHelper_CachePool = autismclient.g.UiHelper.init(snapshot, settings.init_Str(), settings.init_I(), settings.load(), settings.read());
         }

         return scope;
      } catch (IOException | RuntimeException e) {
         scope.close();
         throw e;
      }
   }

   private static ArrayUtil.Inner_StreamUtil init_ArrayUtil_StreamUt(ConfigStore.Inner_DataUtil spec) throws IOException {
      if (spec.init_bZ() != bZ.load) {
         throw new IOException("Multi resource-pack downloads support only SOCKS5 proxies");
      }

      String address = spec.init_Str() == null ? "" : spec.init_Str().trim();
      if (!address.isEmpty() && spec.init_I() >= 1 && spec.init_I() <= 65535) {
         String username = spec.load() == null ? "" : spec.load();
         String password = spec.read() == null ? "" : spec.read();
         boolean authenticated = !username.isEmpty() || !password.isEmpty();
         if (authenticated && (username.isEmpty() || password.isEmpty() || !init(username) || !init(password))) {
            throw new IOException("Multi resource-pack SOCKS5 credentials must be nonempty ISO-8859-1 values of at most 255 bytes");
         } else {
            return new ArrayUtil.Inner_StreamUtil(address, spec.init_I(), username, password, authenticated);
         }
      } else {
         throw new IOException("Invalid Multi resource-pack proxy endpoint");
      }
   }

   private static boolean init(String value) {
      return value.length() <= 255 && StandardCharsets.ISO_8859_1.newEncoder().canEncode(value);
   }

   private static final class Inner_CachePool extends Authenticator {
      private final Authenticator init;

      private Inner_CachePool(Authenticator delegate) {
         this.init = delegate;
      }

      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
         ArrayUtil.Inner_DataUtil scope = ArrayUtil.init_ConcurrentMap.get(Thread.currentThread());
         if (scope != null && "SOCKS5".equals(this.getRequestingProtocol())) {
            return scope.init(this.getRequestingHost(), this.getRequestingPort());
         } else {
            return this.init == null
               ? null
               : this.init
                  .requestPasswordAuthenticationInstance(
                     this.getRequestingHost(),
                     this.getRequestingSite(),
                     this.getRequestingPort(),
                     this.getRequestingProtocol(),
                     this.getRequestingPrompt(),
                     this.getRequestingScheme(),
                     this.getRequestingURL(),
                     this.getRequestorType()
                  );
         }
      }
   }

   static final class Inner_DataUtil implements AutoCloseable {
      private final Thread init_Thread;
      private final ArrayUtil.Inner_StreamUtil init_ArrayUtil_StreamUt;
      private final PasswordAuthentication init_PasswordAuthentica;
      private volatile autismclient.g.UiHelper.Inner_CachePool init_UiHelper_CachePool;

      private Inner_DataUtil(Thread owner, ArrayUtil.Inner_StreamUtil settings) {
         this.init_Thread = owner;
         this.init_ArrayUtil_StreamUt = settings;
         this.init_PasswordAuthentica = new PasswordAuthentication(settings.load(), settings.read().toCharArray());
      }

      private PasswordAuthentication init(String host, int port) {
         if (!this.init_ArrayUtil_StreamUt.init_Z()) {
            return null;
         } else {
            return this.init_ArrayUtil_StreamUt.init_Str().equalsIgnoreCase(host) && this.init_ArrayUtil_StreamUt.init_I() == port
               ? this.init_PasswordAuthentica
               : new PasswordAuthentication("", new char[0]);
         }
      }

      @Override
      public void close() {
         ArrayUtil.init_ConcurrentMap.remove(this.init_Thread, this);
         autismclient.g.UiHelper.Inner_CachePool closing;
         synchronized (this) {
            closing = this.init_UiHelper_CachePool;
            this.init_UiHelper_CachePool = null;
         }

         if (closing != null) {
            closing.close();
         }
      }
   }

   private static final class Inner_StreamUtil {
      private final String init_Str;
      private final int init_I;
      private final String load;
      private final String read;
      private final boolean init_Z;

      private Inner_StreamUtil(String host, int port, String username, String password, boolean authenticated) {
         this.init_Str = host;
         this.init_I = port;
         this.load = username;
         this.read = password;
         this.init_Z = authenticated;
      }

      private String init_Str() {
         return this.init_Str;
      }

      private int init_I() {
         return this.init_I;
      }

      private String load() {
         return this.load;
      }

      private String read() {
         return this.read;
      }

      private boolean init_Z() {
         return this.init_Z;
      }
   }
}
