package autismclient.g;

import autismclient.util.bZ;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.Proxy.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ScopedHttpUrlConnection extends HttpURLConnection {
   private static final int init_I = 65536;
   private static final int load_I = 16384;
   private static final int read_I = 8;
   private static final int write_I = -1;
   private final ResourcePackDownloadScope.Inner_BufferHelper init_UiHelper_BufferHel;
   private final Proxy init_Proxy;
   private final PackUrlValidator.Inner_StreamUtil init_TextFormat_StreamU;
   private final ScopedHttpUrlConnection.Inner_NetHelper init_RenderState_NetHel;
   private final ScopedHttpUrlConnection.Inner_SyncManager init_RenderState_SyncMa;
   private final BooleanSupplier init_BooleanSupplier;
   private final LinkedHashMap<String, List<String>> init_LinkedHashMap = new LinkedHashMap<>();
   private int update = -1;
   private int sync = -1;
   private volatile Socket init_Socket;
   private InputStream init_InputStream;
   private volatile boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private ScopedHttpUrlConnection.Inner_JsonUtil init_RenderState_JsonUt;
   private InputStream load_InputStream;
   private PackUrlValidator.Inner_BufferHelper init_TextFormat_BufferH;
   private boolean write_Z;

   public ScopedHttpUrlConnection(ResourcePackDownloadScope.Inner_BufferHelper snapshot, URL url, Proxy proxy) throws IOException, UnknownHostException {
      this(
         snapshot,
         url,
         proxy,
         host -> Arrays.asList(InetAddress.getAllByName(host)),
         Socket::new,
         (plain, host, port, autoClose) -> ((SSLSocketFactory)SSLSocketFactory.getDefault()).createSocket(plain, host, port, autoClose),
         snapshot::load_Z
      );
   }

   public ScopedHttpUrlConnection(
      ResourcePackDownloadScope.Inner_BufferHelper snapshot,
      URL url,
      Proxy proxy,
      PackUrlValidator.Inner_StreamUtil resolver,
      ScopedHttpUrlConnection.Inner_NetHelper socketFactory,
      ScopedHttpUrlConnection.Inner_SyncManager tlsFactory,
      BooleanSupplier currency
   ) throws IOException {
      super(Objects.requireNonNull(url, "url"));
      this.init_UiHelper_BufferHel = Objects.requireNonNull(snapshot, "snapshot");
      ConnectionPrivacyRegistry.Inner_ConfigStore connectionProxy = snapshot.init_BufferHelper_Confi();
      this.init_Proxy = connectionProxy == null
         ? (proxy == null ? Proxy.NO_PROXY : proxy)
         : new Proxy(Type.SOCKS, InetSocketAddress.createUnresolved(connectionProxy.init_Str(), connectionProxy.init_I()));
      this.init_TextFormat_StreamU = Objects.requireNonNull(resolver, "resolver");
      this.init_RenderState_NetHel = Objects.requireNonNull(socketFactory, "socketFactory");
      this.init_RenderState_SyncMa = Objects.requireNonNull(tlsFactory, "tlsFactory");
      this.init_BooleanSupplier = Objects.requireNonNull(currency, "currency");
      PackUrlValidator.init_TextFormat_ConfigS_URL(url);
      snapshot.init_V();
   }

   @Override
   public void connect() throws IOException {
      this.load();
   }

   @Override
   public void disconnect() {
      this.init_Z = true;
      this.write();
   }

   @Override
   public boolean usingProxy() {
      return this.read_Z;
   }

   @Override
   public void setInstanceFollowRedirects(boolean follow) {
      this.instanceFollowRedirects = follow;
   }

   @Override
   public void setConnectTimeout(int timeout) {
      if (timeout < 0) {
         throw new IllegalArgumentException("timeout can not be negative");
      }

      this.update = timeout;
   }

   @Override
   public int getConnectTimeout() {
      return this.update == -1 ? 0 : this.update;
   }

   @Override
   public void setReadTimeout(int timeout) {
      if (timeout < 0) {
         throw new IllegalArgumentException("timeout can not be negative");
      }

      this.sync = timeout;
   }

   @Override
   public int getReadTimeout() {
      return this.sync == -1 ? 0 : this.sync;
   }

   @Override
   public void setRequestMethod(String method) throws ProtocolException {
      if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
         throw new ProtocolException("Resource-pack requests only support GET");
      }

      super.setRequestMethod("GET");
   }

   @Override
   public void setDoOutput(boolean output) {
      if (output) {
         throw new IllegalArgumentException("Resource-pack GET requests have no request body");
      }

      super.setDoOutput(false);
   }

   @Override
   public void setRequestProperty(String key, String value) {
      this.init_V();
      init_V_Str_Str(key, value);
      this.init_LinkedHashMap.remove(key);
      init_V_Map_Str(this.init_LinkedHashMap, key);
      this.init_LinkedHashMap.put(key, new ArrayList<>(List.of(value)));
   }

   @Override
   public void addRequestProperty(String key, String value) {
      this.init_V();
      init_V_Str_Str(key, value);

      for (Entry<String, List<String>> entry : this.init_LinkedHashMap.entrySet()) {
         if (entry.getKey().equalsIgnoreCase(key)) {
            entry.getValue().add(value);
            return;
         }
      }

      this.init_LinkedHashMap.put(key, new ArrayList<>(List.of(value)));
   }

   @Override
   public String getRequestProperty(String key) {
      List<String> values = init_List_Map_Str(this.init_LinkedHashMap, key);
      return values.isEmpty() ? null : String.join(", ", values);
   }

   @Override
   public Map<String, List<String>> getRequestProperties() {
      return init_Map_Map(this.init_LinkedHashMap);
   }

   private void init_V() {
      if (this.load_Z) {
         throw new IllegalStateException("Already connected");
      }
   }

   @Override
   public InputStream getInputStream() throws IOException {
      this.load();
      if (this.responseCode >= 400) {
         throw new IOException("Server returned HTTP response code: " + this.responseCode + " for URL: " + this.url);
      } else {
         return this.init_InputStream();
      }
   }

   @Override
   public InputStream getErrorStream() {
      try {
         this.load();
         return this.responseCode >= 400 ? this.init_InputStream() : null;
      } catch (IOException e) {
         return null;
      }
   }

   @Override
   public int getResponseCode() throws IOException {
      this.load();
      return this.responseCode;
   }

   @Override
   public String getResponseMessage() throws IOException {
      this.load();
      return this.responseMessage;
   }

   @Override
   public long getContentLengthLong() {
      try {
         this.load();
         return this.init_RenderState_JsonUt.init_J;
      } catch (IOException e) {
         return -1L;
      }
   }

   @Override
   public int getContentLength() {
      long length = this.getContentLengthLong();
      return length > 2147483647L ? -1 : (int)length;
   }

   @Override
   public String getHeaderField(String name) {
      try {
         this.load();
         if (name == null) {
            return this.init_RenderState_JsonUt.init_Str;
         }

         List<String> values = this.init_RenderState_JsonUt.init(name);
         return values.isEmpty() ? null : String.join(", ", values);
      } catch (IOException e) {
         return null;
      }
   }

   @Override
   public String getHeaderField(int index) {
      try {
         this.load();
         if (index == 0) {
            return this.init_RenderState_JsonUt.init_Str;
         }

         int current = 1;

         for (List<String> values : this.init_RenderState_JsonUt.init_LinkedHashMap.values()) {
            if (!values.isEmpty() && current++ == index) {
               return values.getLast();
            }
         }

         return null;
      } catch (IOException | RuntimeException e) {
         return null;
      }
   }

   @Override
   public String getHeaderFieldKey(int index) {
      try {
         this.load();
         if (index == 0) {
            return null;
         }

         int current = 1;

         for (String key : this.init_RenderState_JsonUt.init_LinkedHashMap.keySet()) {
            if (current++ == index) {
               return key;
            }
         }

         return null;
      } catch (IOException e) {
         return null;
      }
   }

   @Override
   public Map<String, List<String>> getHeaderFields() {
      try {
         this.load();
         LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
         result.put(null, List.of(this.init_RenderState_JsonUt.init_Str));
         result.putAll(init_Map_Map(this.init_RenderState_JsonUt.init_LinkedHashMap));
         return Collections.unmodifiableMap(result);
      } catch (IOException e) {
         return Map.of();
      }
   }

   private void load() throws IOException {
      if (this.init_RenderState_JsonUt != null) {
         if (this.init_Z) {
            throw new IOException("Connection disconnected");
         }
      } else {
         if (this.init_Z) {
            throw new IOException("Connection disconnected");
         }

         if (this.load_Z) {
            throw new IOException("Connection attempt already failed");
         }

         this.load_Z = true;
         this.read();
         PackUrlValidator.Inner_ConfigStore target = PackUrlValidator.init_TextFormat_ConfigS_URL(this.url);
         PackUrlValidator.Inner_ConfigStore initialTarget = target;
         PackUrlValidator.Inner_ConfigStore previousTarget = null;
         PackUrlValidator.Inner_CachePool endpoint = null;
         PackUrlValidator.Inner_BufferHelper route = this.init_TextFormat_BufferH();
         boolean used305 = false;
         int redirects = 0;

         while (true) {
            this.read();
            if (endpoint == null) {
               endpoint = PackUrlValidator.init_TextFormat_CachePo_TextFormat_ConfigS(target, this.init_UiHelper_BufferHel, this.init_TextFormat_StreamU);
            }

            Socket socket = this.init_Socket_TextFormat_CachePo_TextFormat_BufferH(endpoint, route);
            this.init_Socket = socket;
            this.read_Z = route != null;

            try {
               if ("https".equals(target.init_Str())) {
                  if (route != null && route.init_Proxy_Type() == Type.HTTP) {
                     this.init(socket, target, route);
                  }

                  socket = this.init_Socket_Socket_TextFormat_ConfigS(socket, target);
                  this.init_Socket = socket;
               }

               this.init(socket, target, previousTarget == null ? null : initialTarget, route);
               ScopedHttpUrlConnection.Inner_JsonUtil response = this.init_RenderState_JsonUt_InputStream(this.init_InputStream_Socket(socket));
               String location = this.instanceFollowRedirects ? this.init_Str_RenderState_JsonUt(response) : null;
               if (!init_Z_I(response.init_I) || location == null) {
                  this.init_RenderState_JsonUt = this.init_RenderState_JsonUt_RenderState_JsonUt(response);
                  this.responseCode = response.init_I;
                  this.responseMessage = response.load_Str;
                  this.url = target.init_URI().toURL();
                  this.connected = true;
                  return;
               }

               if (redirects >= init_I()) {
                  throw new ProtocolException("Server redirected too many times (" + init_I() + ")");
               }

               redirects++;
               this.write();
               if (response.init_I == 305) {
                  if (route != null && route.init_Z()) {
                     throw new ProtocolException("HTTP 305 cannot replace the configured resource-pack proxy");
                  }

                  if (used305) {
                     throw new ProtocolException("Repeated HTTP 305 proxy routes are refused");
                  }

                  route = PackUrlValidator.init_TextFormat_BufferH_Str(location, this.init_UiHelper_BufferHel, this.init_TextFormat_StreamU);
                  used305 = true;
               } else {
                  PackUrlValidator.Inner_ConfigStore next = init_TextFormat_ConfigS_TextFormat_ConfigS_Str(target, location);
                  if (!target.init_Str().equals(next.init_Str())) {
                     throw new ProtocolException("Resource-pack redirect changed URL scheme");
                  }

                  if (!PackUrlValidator.init_Z_TextFormat_ConfigS_TextFormat_ConfigS(initialTarget, next)) {
                     this.init_LinkedHashMap.entrySet().removeIf(e -> {
                        String n = e.getKey();
                        return n != null
                           && (n.equalsIgnoreCase("Authorization")
                              || n.equalsIgnoreCase("Cookie")
                              || n.equalsIgnoreCase("Cookie2")
                              || n.equalsIgnoreCase("Proxy-Authorization"));
                     });
                  }

                  previousTarget = target;
                  target = next;
                  endpoint = null;
                  route = this.init_TextFormat_BufferH();
                  used305 = false;
               }
            } catch (IOException | RuntimeException e) {
               this.write();
               throw e;
            }
         }
      }
   }

   private PackUrlValidator.Inner_BufferHelper init_TextFormat_BufferH() throws IOException {
      if (!this.write_Z) {
         this.init_TextFormat_BufferH = PackUrlValidator.init_TextFormat_BufferH_Proxy_TextFormat_StreamU(this.init_Proxy, this.init_TextFormat_StreamU);
         this.write_Z = true;
      }

      return this.init_TextFormat_BufferH;
   }

   private Socket init_Socket_TextFormat_CachePo_TextFormat_BufferH(PackUrlValidator.Inner_CachePool target, PackUrlValidator.Inner_BufferHelper route) throws IOException {
      PackUrlValidator.Inner_CachePool connectionEndpoint = route == null ? target : route.init_TextFormat_CachePo();
      IOException last = null;

      for (InetAddress address : connectionEndpoint.init_List()) {
         this.read();
         Socket socket = null;

         try {
            socket = this.init_RenderState_NetHel.create();
            this.init_Socket = socket;
            this.init_InputStream = null;
            ResourcePackDownloadScope.init(this.init_UiHelper_BufferHel, socket);
            socket.setSoTimeout(init_I_I_Str(this.sync, "sun.net.client.defaultReadTimeout"));
            InetSocketAddress selected = new InetSocketAddress(address, connectionEndpoint.init_I());
            socket.connect(selected, init_I_I_Str(this.update, "sun.net.client.defaultConnectTimeout"));
            this.init_V_Socket_InetSocketAddress(socket, selected);
            this.init_InputStream = new BufferedInputStream(socket.getInputStream());
            if (route != null && route.init_Proxy_Type() == Type.SOCKS) {
               this.init(socket, target, route);
            }

            return socket;
         } catch (IOException | RuntimeException e) {
            if (socket != null) {
               this.init_V_Socket(socket);
            }

            last = e instanceof IOException io ? io : new IOException("Resource-pack socket failed", e);
         }
      }

      throw last == null ? new IOException("No validated resource-pack address") : last;
   }

   private void init_V_Socket_InetSocketAddress(Socket socket, InetSocketAddress selected) throws IOException {
      if (socket.getRemoteSocketAddress() instanceof InetSocketAddress inet
         && inet.getAddress() != null
         && inet.getPort() == selected.getPort()
         && inet.getAddress().equals(selected.getAddress())) {
         this.read();
      } else {
         throw new IOException("Connected peer differs from the validated address");
      }
   }

   private InputStream init_InputStream_Socket(Socket socket) throws IOException {
      if (socket != this.init_Socket) {
         throw new IOException("Resource-pack socket is no longer active");
      }

      if (this.init_InputStream == null) {
         this.init_InputStream = new BufferedInputStream(socket.getInputStream());
      }

      return this.init_InputStream;
   }

   private Socket init_Socket_Socket_TextFormat_ConfigS(Socket plain, PackUrlValidator.Inner_ConfigStore target) throws IOException {
      Socket layered = this.init_RenderState_SyncMa.create(plain, target.load(), target.init_I(), true);
      if (layered == null) {
         throw new IOException("TLS factory returned no socket");
      }

      if (layered != plain) {
         ResourcePackDownloadScope.init(this.init_UiHelper_BufferHel, layered);
         ResourcePackDownloadScope.load(this.init_UiHelper_BufferHel, plain);
         this.init_Socket = layered;
         this.init_InputStream = new BufferedInputStream(layered.getInputStream());
      }

      if (layered instanceof SSLSocket ssl) {
         SSLParameters parameters = ssl.getSSLParameters();
         parameters.setEndpointIdentificationAlgorithm("HTTPS");
         if (!init_Z_Str(target.load())) {
            parameters.setServerNames(List.of(new SNIHostName(target.load())));
         }

         ssl.setSSLParameters(parameters);
         ssl.startHandshake();
      }

      this.read();
      return layered;
   }

   private void init(Socket socket, PackUrlValidator.Inner_CachePool target, PackUrlValidator.Inner_BufferHelper route) throws IOException {
      OutputStream output = socket.getOutputStream();
      InputStream input = this.init_InputStream_Socket(socket);
      ConnectionPrivacyRegistry.Inner_ConfigStore frozen = this.init_UiHelper_BufferHel.init_BufferHelper_Confi();
      if (frozen == null
         || route.init_Z()
            && frozen.init_I() == route.init_TextFormat_CachePo().init_I()
            && frozen.init_Str().equalsIgnoreCase(route.init_TextFormat_CachePo().load())) {
         ResourcePackDownloadScope.Inner_ConfigStore authentication = frozen == null ? ResourcePackDownloadScope.init(this.init_UiHelper_BufferHel, route) : null;
         byte[] username = null;
         byte[] password = null;
         ScopedHttpUrlConnection.Inner_StreamUtil authenticationRequest = null;

         try {
            if (frozen != null) {
               username = frozen.load().getBytes(StandardCharsets.UTF_8);
               password = frozen.read().getBytes(StandardCharsets.UTF_8);
               if (username.length > 255 || password.length > 255 || frozen.load().indexOf(0) >= 0) {
                  throw new IOException("Invalid connection proxy credentials");
               }

               if (frozen.init_bZ() == bZ.Socks4) {
                  byte[] address = target.init_List().getFirst().getAddress();
                  if (address.length != 4) {
                     throw new IOException("SOCKS4 resource packs require an IPv4 target");
                  }

                  output.write(new byte[]{4, 1, (byte)(target.init_I() >>> 8), (byte)target.init_I()});
                  output.write(address);
                  output.write(username);
                  output.write(0);
                  output.flush();
                  byte[] reply = init_BArr_InputStream_I(input, 8);
                  if (reply[0] != 0 || reply[1] != 90) {
                     throw new IOException("SOCKS4 resource-pack connection refused");
                  }

                  this.read();
                  return;
               }
            }

            boolean authenticated = authentication != null || frozen != null && (username.length != 0 || password.length != 0);
            output.write(authenticated ? new byte[]{5, 2, 0, 2} : new byte[]{5, 1, 0});
            output.flush();
            byte[] greeting = init_BArr_InputStream_I(input, 2);
            if (greeting[0] != 5) {
               throw new IOException("SOCKS5 proxy returned an invalid greeting");
            }

            int method = greeting[1] & 255;
            if (method == 2) {
               if (!authenticated) {
                  throw new IOException("SOCKS5 proxy requires unsupported authentication");
               }

               if (authentication != null) {
                  username = authentication.init_BArr();
                  password = authentication.load();
               }

               if (username.length == 0 || password.length == 0) {
                  throw new IOException("Invalid SOCKS5 proxy credentials");
               }

               authenticationRequest = new ScopedHttpUrlConnection.Inner_StreamUtil(3 + username.length + password.length);
               authenticationRequest.init(1);
               authenticationRequest.init(username.length);
               authenticationRequest.init(username);
               authenticationRequest.init(password.length);
               authenticationRequest.init(password);
               output.write(authenticationRequest.init_BArr(), 0, authenticationRequest.init_I());
               output.flush();
               byte[] authenticationReply = init_BArr_InputStream_I(input, 2);
               if (authenticationReply[0] != 1 || authenticationReply[1] != 0) {
                  throw new IOException("SOCKS5 proxy authentication failed");
               }
            } else if (method != 0) {
               throw new IOException("SOCKS5 proxy requires unsupported authentication");
            }

            InetAddress address = target.init_List().getFirst();
            byte[] raw = address.getAddress();
            ScopedHttpUrlConnection.Inner_StreamUtil request = new ScopedHttpUrlConnection.Inner_StreamUtil(7 + raw.length);
            request.init(5);
            request.init(1);
            request.init(0);
            request.init(raw.length == 4 ? 1 : 4);
            request.init(raw);
            request.init(target.init_I() >>> 8);
            request.init(target.init_I() & 0xFF);
            output.write(request.init_BArr(), 0, request.init_I());
            output.flush();
            byte[] reply = init_BArr_InputStream_I(input, 4);
            if (reply[0] != 5 || reply[1] != 0 || reply[2] != 0) {
               throw new IOException("SOCKS5 proxy refused the connection");
            }

            int var10000;
            switch (reply[3]) {
               case 1:
                  var10000 = 4;
                  break;
               case 2:
               default:
                  throw new IOException("SOCKS5 proxy returned an unsupported address");
               case 3:
                  var10000 = init_BArr_InputStream_I(input, 1)[0] & 255;
                  break;
               case 4:
                  var10000 = 16;
            }

            int length = var10000;
            if (length == 0) {
               throw new IOException("SOCKS5 proxy returned an empty address");
            }

            init_BArr_InputStream_I(input, length + 2);
            this.read();
         } finally {
            if (username != null) {
               Arrays.fill(username, (byte)0);
            }

            if (password != null) {
               Arrays.fill(password, (byte)0);
            }

            if (authenticationRequest != null) {
               Arrays.fill(authenticationRequest.init_BArr(), (byte)0);
            }
         }
      } else {
         throw new IOException("Resource-pack proxy differs from its connection route");
      }
   }

   private void init(Socket socket, PackUrlValidator.Inner_ConfigStore target, PackUrlValidator.Inner_BufferHelper route) throws IOException {
      String host = target.load().indexOf(58) >= 0 ? "[" + target.load() + "]" : target.load();
      String connectAuthority = host + ":" + target.init_I();
      StringBuilder request = new StringBuilder("CONNECT ").append(connectAuthority).append(" HTTP/1.1\r\nHost: ").append(target.read()).append("\r\n");
      this.init(request, "Accept", "*/*");
      this.init(request, "Proxy-Connection", "keep-alive");
      this.init_V_StringBuilder_TextFormat_BufferH(request, route);
      request.append("\r\n");
      socket.getOutputStream().write(request.toString().getBytes(StandardCharsets.ISO_8859_1));
      socket.getOutputStream().flush();
      ScopedHttpUrlConnection.Inner_JsonUtil response = this.init_RenderState_JsonUt_InputStream(this.init_InputStream_Socket(socket));
      if (response.init_I != 200) {
         throw new IOException("HTTP proxy CONNECT failed with status " + response.init_I);
      }

      this.read();
   }

   private void init(Socket socket, PackUrlValidator.Inner_ConfigStore target, PackUrlValidator.Inner_ConfigStore previousTarget, PackUrlValidator.Inner_BufferHelper route) throws IOException {
      String requestTarget = target.write();
      boolean plainHttpProxy = route != null && route.init_Proxy_Type() == Type.HTTP && "http".equals(target.init_Str());
      if (plainHttpProxy) {
         requestTarget = target.init_Str() + "://" + target.init_URI().getRawAuthority() + target.write();
      }

      StringBuilder request = new StringBuilder("GET ").append(requestTarget).append(" HTTP/1.1\r\nHost: ").append(target.read()).append("\r\n");
      boolean authorityChanged = previousTarget != null && !PackUrlValidator.init_Z_TextFormat_ConfigS_TextFormat_ConfigS(previousTarget, target);

      for (Entry<String, List<String>> entry : this.init_LinkedHashMap.entrySet()) {
         String name = entry.getKey();
         if (!name.equalsIgnoreCase("Host")
            && !name.equalsIgnoreCase("Proxy-Authorization")
            && (!authorityChanged || !name.equalsIgnoreCase("Authorization") && !name.equalsIgnoreCase("Cookie") && !name.equalsIgnoreCase("Cookie2"))) {
            for (String value : entry.getValue()) {
               load(request, name, value);
            }
         }
      }

      if (plainHttpProxy) {
         this.init_V_StringBuilder_TextFormat_BufferH(request, route);
         if (init_List_Map_Str(this.init_LinkedHashMap, "Proxy-Connection").isEmpty()) {
            request.append("Proxy-Connection: keep-alive\r\n");
         }
      } else if (init_List_Map_Str(this.init_LinkedHashMap, "Connection").isEmpty()) {
         request.append("Connection: keep-alive\r\n");
      }

      if (init_List_Map_Str(this.init_LinkedHashMap, "Accept").isEmpty()) {
         request.append("Accept: */*\r\n");
      }

      request.append("\r\n");
      byte[] bytes = request.toString().getBytes(StandardCharsets.ISO_8859_1);
      this.read();
      socket.getOutputStream().write(bytes);
      socket.getOutputStream().flush();
   }

   private void init(StringBuilder request, String name, String defaultValue) {
      List<String> values = init_List_Map_Str(this.init_LinkedHashMap, name);
      if (values.isEmpty()) {
         load(request, name, defaultValue);
      } else {
         for (String value : values) {
            load(request, name, value);
         }
      }
   }

   private void init_V_StringBuilder_TextFormat_BufferH(StringBuilder request, PackUrlValidator.Inner_BufferHelper route) {
      if (route.init_Z()) {
         for (String value : init_List_Map_Str(this.init_LinkedHashMap, "Proxy-Authorization")) {
            load(request, "Proxy-Authorization", value);
         }
      }
   }

   private static void load(StringBuilder request, String name, String value) {
      request.append(name).append(": ").append(value).append("\r\n");
   }

   private ScopedHttpUrlConnection.Inner_JsonUtil init_RenderState_JsonUt_InputStream(InputStream input) throws IOException {
      int aggregate = 0;
      int interim = 0;

      while (true) {
         String status = init_Str_InputStream(input);
         aggregate += status.length() + 2;
         if (aggregate > 65536) {
            throw new IOException("HTTP response headers are too large");
         }

         ScopedHttpUrlConnection.Inner_StateCache statusLine = init_RenderState_StateC_Str(status);
         LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();

         while (true) {
            String line = init_Str_InputStream(input);
            aggregate += line.length() + 2;
            if (aggregate > 65536) {
               throw new IOException("HTTP response headers are too large");
            }

            if (line.isEmpty()) {
               if (statusLine.init_I >= 100 && statusLine.init_I < 200) {
                  if (statusLine.init_I != 101 && interim < 7) {
                     interim++;
                     break;
                  }

                  throw new IOException("Too many or unsupported interim HTTP responses");
               }

               return new ScopedHttpUrlConnection.Inner_JsonUtil(status, statusLine.init_I, statusLine.init_Str, headers, -1L, false, false);
            }

            if (line.charAt(0) == ' ' || line.charAt(0) == '\t') {
               throw new IOException("Folded HTTP headers are refused");
            }

            int colon = line.indexOf(58);
            if (colon <= 0) {
               throw new IOException("Malformed HTTP response header");
            }

            String name = line.substring(0, colon);
            String value = line.substring(colon + 1).trim();

            try {
               init_V_Str_Str(name, value);
            } catch (IllegalArgumentException e) {
               throw new IOException("Malformed HTTP response header", e);
            }

            List<String> values = null;
            Iterator var12 = headers.entrySet().iterator();

            while (true) {
               if (var12.hasNext()) {
                  Entry<String, List<String>> entry = (Entry<String, List<String>>)var12.next();
                  if (!entry.getKey().equalsIgnoreCase(name)) {
                     continue;
                  }

                  values = entry.getValue();
               }

               if (values == null) {
                  values = new ArrayList<>();
                  headers.put(name, values);
               }

               values.add(value);
               break;
            }
         }
      }
   }

   private static ScopedHttpUrlConnection.Inner_StateCache init_RenderState_StateC_Str(String line) throws IOException {
      int first = line.indexOf(32);
      if (first >= 0 && (line.startsWith("HTTP/1.1") || line.startsWith("HTTP/1.0"))) {
         int second = line.indexOf(32, first + 1);
         String code = second < 0 ? line.substring(first + 1) : line.substring(first + 1, second);
         if (code.length() == 3 && code.chars().allMatch(Character::isDigit)) {
            String message = second < 0 ? "" : line.substring(second + 1);
            return new ScopedHttpUrlConnection.Inner_StateCache(Integer.parseInt(code), message);
         } else {
            throw new IOException("Malformed HTTP response status");
         }
      } else {
         throw new IOException("Malformed HTTP status line");
      }
   }

   private ScopedHttpUrlConnection.Inner_JsonUtil init_RenderState_JsonUt_RenderState_JsonUt(ScopedHttpUrlConnection.Inner_JsonUtil response) throws IOException {
      List<String> transfer = response.init("Transfer-Encoding");
      List<String> lengths = response.init("Content-Length");
      if (!transfer.isEmpty() && !lengths.isEmpty()) {
         throw new IOException("Ambiguous HTTP response framing");
      }

      boolean noBody = response.init_I == 204 || response.init_I == 304 || response.init_I >= 100 && response.init_I < 200;
      boolean chunked = false;
      if (!transfer.isEmpty()) {
         List<String> codings = init_List_List(transfer);
         if (codings.size() != 1 || !codings.getFirst().equalsIgnoreCase("chunked")) {
            throw new IOException("Unsupported HTTP transfer coding");
         }

         chunked = true;
      }

      long contentLength = -1L;
      if (!lengths.isEmpty()) {
         for (String value : init_List_List(lengths)) {
            if (value.isEmpty() || !value.chars().allMatch(Character::isDigit)) {
               throw new IOException("Malformed HTTP Content-Length");
            }

            long parsed;
            try {
               parsed = Long.parseLong(value);
            } catch (NumberFormatException e) {
               throw new IOException("HTTP Content-Length is too large", e);
            }

            if (contentLength >= 0L && contentLength != parsed) {
               throw new IOException("Conflicting HTTP Content-Length");
            }

            contentLength = parsed;
         }
      }

      return response.init(contentLength, chunked, noBody);
   }

   private InputStream init_InputStream() throws IOException {
      if (this.load_InputStream != null) {
         return this.load_InputStream;
      }

      Socket socket = this.init_Socket;
      if (socket == null) {
         throw new IOException("Response body has no active socket");
      }

      ScopedHttpUrlConnection.Inner_JsonUtil response = this.init_RenderState_JsonUt;
      InputStream input = this.init_InputStream_Socket(socket);
      if (response.load_Z) {
         this.load_InputStream = new ScopedHttpUrlConnection.Inner_DataUtil(this, socket, InputStream.nullInputStream(), true, response.init_I >= 400);
      } else if (response.init_Z) {
         this.load_InputStream = new ScopedHttpUrlConnection.Inner_CachePool(this, socket, input, response.init_I >= 400);
      } else if (response.init_J >= 0L) {
         this.load_InputStream = new ScopedHttpUrlConnection.Inner_TaskQueue(this, socket, input, response.init_J, response.init_I >= 400);
      } else {
         this.load_InputStream = new ScopedHttpUrlConnection.Inner_ConfigStore(this, socket, input, response.init_I >= 400);
      }

      return this.load_InputStream;
   }

   private String init_Str_RenderState_JsonUt(ScopedHttpUrlConnection.Inner_JsonUtil response) throws ProtocolException {
      if (!init_Z_I(response.init_I)) {
         return null;
      }

      List<String> locations = response.init("Location");
      if (locations.isEmpty()) {
         if (response.init_I == 305) {
            throw new ProtocolException("HTTP 305 has no Location header");
         } else {
            return null;
         }
      } else if (locations.size() != 1) {
         throw new ProtocolException("Ambiguous redirect Location headers");
      } else {
         return locations.getFirst();
      }
   }

   private static PackUrlValidator.Inner_ConfigStore init_TextFormat_ConfigS_TextFormat_ConfigS_Str(PackUrlValidator.Inner_ConfigStore current, String location) throws MalformedURLException {
      try {
         URI resolved = current.init_URI().resolve(new URI(location));
         return PackUrlValidator.init_TextFormat_ConfigS_URI(resolved);
      } catch (URISyntaxException | IllegalArgumentException e) {
         MalformedURLException malformed = new MalformedURLException("Invalid resource-pack redirect");
         malformed.initCause(e);
         throw malformed;
      }
   }

   private static boolean init_Z_I(int status) {
      return status == 300 || status == 301 || status == 302 || status == 303 || status == 305 || status == 307;
   }

   private static int init_I() {
      int value = 20;
      String configured = System.getProperty("http.maxRedirects");
      if (configured != null) {
         try {
            value = Integer.parseInt(configured);
         } catch (NumberFormatException ignored) {
            value = 20;
         }
      }

      return Math.max(0, Math.min(50, value));
   }

   private static int init_I_I_Str(int explicit, String property) {
      if (explicit != -1) {
         return explicit;
      }

      String configured = System.getProperty(property);
      if (configured != null) {
         try {
            int value = Integer.parseInt(configured);
            if (value > 0) {
               return value;
            }
         } catch (NumberFormatException var4) {
         }
      }

      return 0;
   }

   private void read() throws IOException {
      if (this.init_Z || !this.init_BooleanSupplier.getAsBoolean() || !this.init_UiHelper_BufferHel.load_Z()) {
         throw new IOException("The resource-pack connection is no longer current");
      }
   }

   private void write() {
      Socket socket = this.init_Socket;
      this.init_Socket = null;
      this.init_InputStream = null;
      if (socket != null) {
         this.init_V_Socket(socket);
      }
   }

   private void init_V_Socket(Socket socket) {
      if (this.init_Socket == socket) {
         this.init_Socket = null;
         this.init_InputStream = null;
      }

      try {
         socket.close();
      } catch (IOException var6) {
      } finally {
         ResourcePackDownloadScope.load(this.init_UiHelper_BufferHel, socket);
      }
   }

   private void init_V_Socket_Socket(Socket socket, Socket expected) {
      if (this.init_Socket == expected) {
         this.write();
      } else {
         this.init_V_Socket(socket);
      }
   }

   private static String init_Str_InputStream(InputStream input) throws EOFException, IOException {
      byte[] buffer = new byte[256];
      int size = 0;

      while (true) {
         int next = input.read();
         if (next < 0) {
            throw new EOFException("Unexpected EOF in HTTP headers");
         }

         if (next == 13) {
            int lf = input.read();
            if (lf != 10) {
               throw new IOException("Malformed HTTP line ending");
            }

            return new String(buffer, 0, size, StandardCharsets.ISO_8859_1);
         }

         if (next == 10 || next == 0) {
            throw new IOException("Malformed HTTP line");
         }

         if (size >= 16384) {
            throw new IOException("HTTP line is too long");
         }

         if (size == buffer.length) {
            buffer = Arrays.copyOf(buffer, Math.min(buffer.length * 2, 16385));
         }

         buffer[size++] = (byte)next;
      }
   }

   private static byte[] init_BArr_InputStream_I(InputStream input, int count) throws EOFException, IOException {
      byte[] result = input.readNBytes(count);
      if (result.length != count) {
         throw new EOFException("Unexpected EOF in proxy negotiation");
      } else {
         return result;
      }
   }

   private static void init_V_Str_Str(String name, String value) {
      if (name == null || name.isEmpty() || !name.chars().allMatch(ScopedHttpUrlConnection::load)) {
         throw new IllegalArgumentException("Invalid HTTP header name");
      }

      if (value == null) {
         throw new NullPointerException("HTTP header value");
      }

      for (int i = 0; i < value.length(); i++) {
         char c = value.charAt(i);
         if (c == '\r' || c == '\n' || c == 0 || c < ' ' && c != '\t' || c == 127 || c > 255) {
            throw new IllegalArgumentException("Invalid HTTP header value");
         }
      }
   }

   private static boolean load(int c) {
      return c > 32 && c < 127 ? "()<>@,;:\\\"/[]?={}\t".indexOf(c) < 0 : false;
   }

   private static boolean init_Z_Str(String host) {
      return host.indexOf(58) >= 0 ? true : host.matches("[0-9.]+");
   }

   private static List<String> init_List_Map_Str(Map<String, List<String>> headers, String name) {
      if (name == null) {
         return List.of();
      }

      for (Entry<String, List<String>> entry : headers.entrySet()) {
         if (entry.getKey().equalsIgnoreCase(name)) {
            return entry.getValue();
         }
      }

      return List.of();
   }

   private static void init_V_Map_Str(Map<String, List<String>> headers, String name) {
      headers.keySet().removeIf(key -> key.equalsIgnoreCase(name));
   }

   private static Map<String, List<String>> init_Map_Map(Map<String, List<String>> source) {
      LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();

      for (Entry<String, List<String>> entry : source.entrySet()) {
         result.put(entry.getKey(), Collections.unmodifiableList(List.copyOf(entry.getValue())));
      }

      return Collections.unmodifiableMap(result);
   }

   private static List<String> init_List_List(List<String> values) {
      List<String> result = new ArrayList<>();

      for (String value : values) {
         for (String part : value.split(",", -1)) {
            result.add(part.trim());
         }
      }

      return result;
   }

   private static final class Inner_BufferHelper extends ScopedHttpUrlConnection.Inner_DataUtil {
      private Inner_BufferHelper(ScopedHttpUrlConnection owner, Socket socket, boolean closeOnEof) {
         super(owner, socket, InputStream.nullInputStream(), true, closeOnEof);
      }
   }

   private static final class Inner_CachePool extends ScopedHttpUrlConnection.Inner_DataUtil {
      private long init_J;
      private boolean init_Z_2;
      private boolean load_Z;

      private Inner_CachePool(ScopedHttpUrlConnection owner, Socket socket, InputStream input, boolean closeOnEof) {
         super(owner, socket, input, false, closeOnEof);
      }

      @Override
      protected int init(byte[] bytes, int offset, int length) throws EOFException, IOException {
         if (this.load_Z) {
            return -1;
         }

         if (this.init_Z_2) {
            byte[] ending = ScopedHttpUrlConnection.init_BArr_InputStream_I(this.init_InputStream, 2);
            if (ending[0] != 13 || ending[1] != 10) {
               throw new IOException("Malformed chunk ending");
            }

            this.init_Z_2 = false;
         }

         if (this.init_J == 0L) {
            String line = ScopedHttpUrlConnection.init_Str_InputStream(this.init_InputStream);
            int extension = line.indexOf(59);
            String size = (extension < 0 ? line : line.substring(0, extension)).trim();
            if (size.isEmpty() || !size.chars().allMatch(c -> Character.digit(c, 16) >= 0)) {
               throw new IOException("Malformed chunk size");
            }

            try {
               this.init_J = Long.parseLong(size, 16);
            } catch (NumberFormatException e) {
               throw new IOException("Chunk size is too large", e);
            }

            if (this.init_J == 0L) {
               init(this.init_InputStream);
               this.load_Z = true;
               return -1;
            }
         }

         int read = this.init_InputStream.read(bytes, offset, (int)Math.min(length, this.init_J));
         if (read < 0) {
            throw new EOFException("Unexpected EOF in chunked body");
         }

         this.init_J -= read;
         if (this.init_J == 0L) {
            this.init_Z_2 = true;
         }

         return read;
      }

      private static void init(InputStream input) throws IOException {
         int aggregate = 0;

         while (true) {
            String line = ScopedHttpUrlConnection.init_Str_InputStream(input);
            aggregate += line.length() + 2;
            if (aggregate > 65536) {
               throw new IOException("HTTP trailers are too large");
            }

            if (line.isEmpty()) {
               return;
            }

            int colon = line.indexOf(58);
            if (colon <= 0) {
               throw new IOException("Malformed HTTP trailer");
            }

            try {
               ScopedHttpUrlConnection.init_V_Str_Str(line.substring(0, colon), line.substring(colon + 1).trim());
            } catch (IllegalArgumentException e) {
               throw new IOException("Malformed HTTP trailer", e);
            }
         }
      }
   }

   private static final class Inner_ConfigStore extends ScopedHttpUrlConnection.Inner_DataUtil {
      private Inner_ConfigStore(ScopedHttpUrlConnection owner, Socket socket, InputStream input, boolean closeOnEof) {
         super(owner, socket, input, false, closeOnEof);
      }
   }

   private static class Inner_DataUtil extends InputStream {
      private final ScopedHttpUrlConnection init_RenderState;
      private final Socket init_Socket;
      protected final InputStream init_InputStream;
      private final boolean init_Z;
      private boolean load;

      private Inner_DataUtil(ScopedHttpUrlConnection owner, Socket socket, InputStream input, boolean done, boolean closeOnEof) {
         this.init_RenderState = owner;
         this.init_Socket = socket;
         this.init_InputStream = input;
         this.init_Z = closeOnEof;
         this.load = done;
      }

      @Override
      public int read() throws IOException {
         byte[] one = new byte[1];
         int read = this.read(one, 0, 1);
         return read < 0 ? -1 : one[0] & 0xFF;
      }

      @Override
      public int read(byte[] bytes, int offset, int length) throws IOException {
         Objects.checkFromIndexSize(offset, length, bytes.length);
         if (length == 0) {
            return 0;
         }

         if (this.load) {
            return -1;
         }

         this.init_RenderState.read();
         int result = this.init(bytes, offset, length);
         if (result < 0) {
            this.load = true;
            if (this.init_Z) {
               this.close();
            }
         }

         return result;
      }

      protected int init(byte[] bytes, int offset, int length) throws IOException {
         return this.init_InputStream.read(bytes, offset, length);
      }

      @Override
      public int available() throws IOException {
         return this.load ? 0 : this.init_InputStream.available();
      }

      @Override
      public void close() {
         if (this.load && !this.init_Z) {
            this.init_RenderState.init_V_Socket_Socket(this.init_Socket, this.init_Socket);
         } else {
            this.load = true;
            this.init_RenderState.init_V_Socket_Socket(this.init_Socket, this.init_Socket);
         }
      }
   }

   private static final class Inner_JsonUtil {
      private final String init_Str;
      private final int init_I;
      private final String load_Str;
      private final LinkedHashMap<String, List<String>> init_LinkedHashMap;
      private final long init_J;
      private final boolean init_Z;
      private final boolean load_Z;

      private Inner_JsonUtil(
         String statusLine, int code, String message, LinkedHashMap<String, List<String>> headers, long contentLength, boolean chunked, boolean noBody
      ) {
         this.init_Str = statusLine;
         this.init_I = code;
         this.load_Str = message;
         this.init_LinkedHashMap = headers;
         this.init_J = contentLength;
         this.init_Z = chunked;
         this.load_Z = noBody;
      }

      private List<String> init(String name) {
         return ScopedHttpUrlConnection.init_List_Map_Str(this.init_LinkedHashMap, name);
      }

      private ScopedHttpUrlConnection.Inner_JsonUtil init(long length, boolean chunked, boolean noBody) {
         return new ScopedHttpUrlConnection.Inner_JsonUtil(this.init_Str, this.init_I, this.load_Str, this.init_LinkedHashMap, length, chunked, noBody);
      }
   }

   @FunctionalInterface
   public interface Inner_NetHelper {
      Socket create();
   }

   private record Inner_StateCache(int init_I, String init_Str) {
   }

   private static final class Inner_StreamUtil {
      private final byte[] init_BArr;
      private int init_I;

      private Inner_StreamUtil(int capacity) {
         this.init_BArr = new byte[capacity];
      }

      private void init(int value) {
         this.init_BArr[this.init_I++] = (byte)value;
      }

      private void init(byte[] value) {
         System.arraycopy(value, 0, this.init_BArr, this.init_I, value.length);
         this.init_I += value.length;
      }

      private byte[] init_BArr() {
         return this.init_BArr;
      }

      private int init_I() {
         return this.init_I;
      }
   }

   @FunctionalInterface
   public interface Inner_SyncManager {
      Socket create(Socket var1, String var2, int var3, boolean var4) throws IOException;
   }

   private static final class Inner_TaskQueue extends ScopedHttpUrlConnection.Inner_DataUtil {
      private long init_J;

      private Inner_TaskQueue(ScopedHttpUrlConnection owner, Socket socket, InputStream input, long length, boolean closeOnEof) {
         super(owner, socket, input, length == 0L, closeOnEof);
         this.init_J = length;
      }

      @Override
      protected int init(byte[] bytes, int offset, int length) throws EOFException, IOException {
         if (this.init_J == 0L) {
            return -1;
         }

         int read = this.init_InputStream.read(bytes, offset, (int)Math.min(length, this.init_J));
         if (read < 0) {
            throw new EOFException("HTTP body ended before Content-Length");
         }

         this.init_J -= read;
         return read;
      }

      @Override
      public int available() throws IOException {
         return (int)Math.min(super.available(), this.init_J);
      }
   }
}
