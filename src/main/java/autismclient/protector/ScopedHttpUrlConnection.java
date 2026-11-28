package autismclient.protector;

import autismclient.util.ProxyProtocolType;
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
   private final ResourcePackDownloadScope.Snapshot scopeSnapshot;
   private final Proxy connectionProxy;
   private final PackUrlValidator.HostResolver resolver;
   private final ScopedHttpUrlConnection.PlainSocketFactory socketFactory;
   private final ScopedHttpUrlConnection.TlsSocketFactory tlsFactory;
   private final BooleanSupplier stillCurrent;
   private final LinkedHashMap<String, List<String>> requestHeaders = new LinkedHashMap<>();
   private int scopedConnectTimeout = -1;
   private int scopedReadTimeout = -1;
   private volatile Socket activeSocket;
   private InputStream socketInput;
   private volatile boolean disconnected;
   private boolean socketConnected;
   private boolean viaProxy;
   private ScopedHttpUrlConnection.ResponseHead response;
   private InputStream responseBody;
   private PackUrlValidator.PackRoute route;
   private boolean routeResolved;

   public ScopedHttpUrlConnection(ResourcePackDownloadScope.Snapshot snapshot, URL url, Proxy proxy) throws IOException, UnknownHostException {
      this(
         snapshot,
         url,
         proxy,
         host -> Arrays.asList(InetAddress.getAllByName(host)),
         Socket::new,
         (plain, host, port, autoClose) -> ((SSLSocketFactory)SSLSocketFactory.getDefault()).createSocket(plain, host, port, autoClose),
         snapshot::isCurrent
      );
   }

   public ScopedHttpUrlConnection(
      ResourcePackDownloadScope.Snapshot snapshot,
      URL url,
      Proxy proxy,
      PackUrlValidator.HostResolver resolver,
      ScopedHttpUrlConnection.PlainSocketFactory socketFactory,
      ScopedHttpUrlConnection.TlsSocketFactory tlsFactory,
      BooleanSupplier currency
   ) throws IOException {
      super(Objects.requireNonNull(url, "url"));
      this.scopeSnapshot = Objects.requireNonNull(snapshot, "snapshot");
      ConnectionPrivacyRegistry.ProxyEndpoint connectionProxy = snapshot.proxyEndpoint();
      this.connectionProxy = connectionProxy == null
         ? (proxy == null ? Proxy.NO_PROXY : proxy)
         : new Proxy(Type.SOCKS, InetSocketAddress.createUnresolved(connectionProxy.host(), connectionProxy.port()));
      this.resolver = Objects.requireNonNull(resolver, "resolver");
      this.socketFactory = Objects.requireNonNull(socketFactory, "socketFactory");
      this.tlsFactory = Objects.requireNonNull(tlsFactory, "tlsFactory");
      this.stillCurrent = Objects.requireNonNull(currency, "currency");
      PackUrlValidator.parseUrl(url);
      snapshot.ensureCurrent();
   }

   @Override
   public void connect() throws IOException {
      this.ensureResponse();
   }

   @Override
   public void disconnect() {
      this.disconnected = true;
      this.closeActiveSocket();
   }

   @Override
   public boolean usingProxy() {
      return this.viaProxy;
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

      this.scopedConnectTimeout = timeout;
   }

   @Override
   public int getConnectTimeout() {
      return this.scopedConnectTimeout == -1 ? 0 : this.scopedConnectTimeout;
   }

   @Override
   public void setReadTimeout(int timeout) {
      if (timeout < 0) {
         throw new IllegalArgumentException("timeout can not be negative");
      }

      this.scopedReadTimeout = timeout;
   }

   @Override
   public int getReadTimeout() {
      return this.scopedReadTimeout == -1 ? 0 : this.scopedReadTimeout;
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
      this.ensureNotConnected();
      validateHeader(key, value);
      this.requestHeaders.remove(key);
      removeHeader(this.requestHeaders, key);
      this.requestHeaders.put(key, new ArrayList<>(List.of(value)));
   }

   @Override
   public void addRequestProperty(String key, String value) {
      this.ensureNotConnected();
      validateHeader(key, value);

      for (Entry<String, List<String>> entry : this.requestHeaders.entrySet()) {
         if (entry.getKey().equalsIgnoreCase(key)) {
            entry.getValue().add(value);
            return;
         }
      }

      this.requestHeaders.put(key, new ArrayList<>(List.of(value)));
   }

   @Override
   public String getRequestProperty(String key) {
      List<String> values = headerValues(this.requestHeaders, key);
      return values.isEmpty() ? null : String.join(", ", values);
   }

   @Override
   public Map<String, List<String>> getRequestProperties() {
      return copyHeaders(this.requestHeaders);
   }

   private void ensureNotConnected() {
      if (this.socketConnected) {
         throw new IllegalStateException("Already connected");
      }
   }

   @Override
   public InputStream getInputStream() throws IOException {
      this.ensureResponse();
      if (this.responseCode >= 400) {
         throw new IOException("Server returned HTTP response code: " + this.responseCode + " for URL: " + this.url);
      } else {
         return this.openResponseStream();
      }
   }

   @Override
   public InputStream getErrorStream() {
      try {
         this.ensureResponse();
         return this.responseCode >= 400 ? this.openResponseStream() : null;
      } catch (IOException e) {
         return null;
      }
   }

   @Override
   public int getResponseCode() throws IOException {
      this.ensureResponse();
      return this.responseCode;
   }

   @Override
   public String getResponseMessage() throws IOException {
      this.ensureResponse();
      return this.responseMessage;
   }

   @Override
   public long getContentLengthLong() {
      try {
         this.ensureResponse();
         return this.response.contentLength;
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
         this.ensureResponse();
         if (name == null) {
            return this.response.statusLine;
         }

         List<String> values = this.response.header(name);
         return values.isEmpty() ? null : String.join(", ", values);
      } catch (IOException e) {
         return null;
      }
   }

   @Override
   public String getHeaderField(int index) {
      try {
         this.ensureResponse();
         if (index == 0) {
            return this.response.statusLine;
         }

         int current = 1;

         for (List<String> values : this.response.headers.values()) {
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
         this.ensureResponse();
         if (index == 0) {
            return null;
         }

         int current = 1;

         for (String key : this.response.headers.keySet()) {
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
         this.ensureResponse();
         LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
         result.put(null, List.of(this.response.statusLine));
         result.putAll(copyHeaders(this.response.headers));
         return Collections.unmodifiableMap(result);
      } catch (IOException e) {
         return Map.of();
      }
   }

   private void ensureResponse() throws IOException {
      if (this.response != null) {
         if (this.disconnected) {
            throw new IOException("Connection disconnected");
         }
      } else {
         if (this.disconnected) {
            throw new IOException("Connection disconnected");
         }

         if (this.socketConnected) {
            throw new IOException("Connection attempt already failed");
         }

         this.socketConnected = true;
         this.ensureCurrent();
         PackUrlValidator.ParsedTarget target = PackUrlValidator.parseUrl(this.url);
         PackUrlValidator.ParsedTarget initialTarget = target;
         PackUrlValidator.ParsedTarget previousTarget = null;
         PackUrlValidator.ResolvedEndpoint endpoint = null;
         PackUrlValidator.PackRoute route = this.resolveRoute();
         boolean used305 = false;
         int redirects = 0;

         while (true) {
            this.ensureCurrent();
            if (endpoint == null) {
               endpoint = PackUrlValidator.resolveEndpoint(target, this.scopeSnapshot, this.resolver);
            }

            Socket socket = this.openSocketFor(endpoint, route);
            this.activeSocket = socket;
            this.viaProxy = route != null;

            try {
               if ("https".equals(target.init_Str())) {
                  if (route != null && route.proxyType() == Type.HTTP) {
                     this.init(socket, target, route);
                  }

                  socket = this.layerTls(socket, target);
                  this.activeSocket = socket;
               }

               this.init(socket, target, previousTarget == null ? null : initialTarget, route);
               ScopedHttpUrlConnection.ResponseHead response = this.readResponseHead(this.socketInputFor(socket));
               String location = this.instanceFollowRedirects ? this.redirectLocation(response) : null;
               if (!isRedirectStatus(response.code) || location == null) {
                  this.response = this.applyTransferEncoding(response);
                  this.responseCode = response.code;
                  this.responseMessage = response.message;
                  this.url = target.uri().toURL();
                  this.connected = true;
                  return;
               }

               if (redirects >= maxRedirects()) {
                  throw new ProtocolException("Server redirected too many times (" + maxRedirects() + ")");
               }

               redirects++;
               this.closeActiveSocket();
               if (response.code == 305) {
                  if (route != null && route.viaProxy()) {
                     throw new ProtocolException("HTTP 305 cannot replace the configured resource-pack proxy");
                  }

                  if (used305) {
                     throw new ProtocolException("Repeated HTTP 305 proxy routes are refused");
                  }

                  route = PackUrlValidator.routeForLocation(location, this.scopeSnapshot, this.resolver);
                  used305 = true;
               } else {
                  PackUrlValidator.ParsedTarget next = resolveRedirectTarget(target, location);
                  if (!target.init_Str().equals(next.init_Str())) {
                     throw new ProtocolException("Resource-pack redirect changed URL scheme");
                  }

                  if (!PackUrlValidator.sameAuthority(initialTarget, next)) {
                     this.requestHeaders.entrySet().removeIf(e -> {
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
                  route = this.resolveRoute();
                  used305 = false;
               }
            } catch (IOException | RuntimeException e) {
               this.closeActiveSocket();
               throw e;
            }
         }
      }
   }

   private PackUrlValidator.PackRoute resolveRoute() throws IOException {
      if (!this.routeResolved) {
         this.route = PackUrlValidator.routeFor(this.connectionProxy, this.resolver);
         this.routeResolved = true;
      }

      return this.route;
   }

   private Socket openSocketFor(PackUrlValidator.ResolvedEndpoint target, PackUrlValidator.PackRoute route) throws IOException {
      PackUrlValidator.ResolvedEndpoint connectionEndpoint = route == null ? target : route.endpoint();
      IOException last = null;

      for (InetAddress address : connectionEndpoint.addresses()) {
         this.ensureCurrent();
         Socket socket = null;

         try {
            socket = this.socketFactory.create();
            this.activeSocket = socket;
            this.socketInput = null;
            ResourcePackDownloadScope.init(this.scopeSnapshot, socket);
            socket.setSoTimeout(timeoutOrProperty(this.scopedReadTimeout, "sun.net.client.defaultReadTimeout"));
            InetSocketAddress selected = new InetSocketAddress(address, connectionEndpoint.port());
            socket.connect(selected, timeoutOrProperty(this.scopedConnectTimeout, "sun.net.client.defaultConnectTimeout"));
            this.verifyConnectedPeer(socket, selected);
            this.socketInput = new BufferedInputStream(socket.getInputStream());
            if (route != null && route.proxyType() == Type.SOCKS) {
               this.init(socket, target, route);
            }

            return socket;
         } catch (IOException | RuntimeException e) {
            if (socket != null) {
               this.closeSocket(socket);
            }

            last = e instanceof IOException io ? io : new IOException("Resource-pack socket failed", e);
         }
      }

      throw last == null ? new IOException("No validated resource-pack address") : last;
   }

   private void verifyConnectedPeer(Socket socket, InetSocketAddress selected) throws IOException {
      if (socket.getRemoteSocketAddress() instanceof InetSocketAddress inet
         && inet.getAddress() != null
         && inet.getPort() == selected.getPort()
         && inet.getAddress().equals(selected.getAddress())) {
         this.ensureCurrent();
      } else {
         throw new IOException("Connected peer differs from the validated address");
      }
   }

   private InputStream socketInputFor(Socket socket) throws IOException {
      if (socket != this.activeSocket) {
         throw new IOException("Resource-pack socket is no longer active");
      }

      if (this.socketInput == null) {
         this.socketInput = new BufferedInputStream(socket.getInputStream());
      }

      return this.socketInput;
   }

   private Socket layerTls(Socket plain, PackUrlValidator.ParsedTarget target) throws IOException {
      Socket layered = this.tlsFactory.create(plain, target.load(), target.port(), true);
      if (layered == null) {
         throw new IOException("TLS factory returned no socket");
      }

      if (layered != plain) {
         ResourcePackDownloadScope.init(this.scopeSnapshot, layered);
         ResourcePackDownloadScope.load(this.scopeSnapshot, plain);
         this.activeSocket = layered;
         this.socketInput = new BufferedInputStream(layered.getInputStream());
      }

      if (layered instanceof SSLSocket ssl) {
         SSLParameters parameters = ssl.getSSLParameters();
         parameters.setEndpointIdentificationAlgorithm("HTTPS");
         if (!isIpLiteralHost(target.load())) {
            parameters.setServerNames(List.of(new SNIHostName(target.load())));
         }

         ssl.setSSLParameters(parameters);
         ssl.startHandshake();
      }

      this.ensureCurrent();
      return layered;
   }

   private void init(Socket socket, PackUrlValidator.ResolvedEndpoint target, PackUrlValidator.PackRoute route) throws IOException {
      OutputStream output = socket.getOutputStream();
      InputStream input = this.socketInputFor(socket);
      ConnectionPrivacyRegistry.ProxyEndpoint frozen = this.scopeSnapshot.proxyEndpoint();
      if (frozen == null
         || route.viaProxy()
            && frozen.port() == route.endpoint().port()
            && frozen.host().equalsIgnoreCase(route.endpoint().host())) {
         ResourcePackDownloadScope.ProxyAuth authentication = frozen == null ? ResourcePackDownloadScope.init(this.scopeSnapshot, route) : null;
         byte[] username = null;
         byte[] password = null;
         ScopedHttpUrlConnection.ByteBuilder authenticationRequest = null;

         try {
            if (frozen != null) {
               username = frozen.username().getBytes(StandardCharsets.UTF_8);
               password = frozen.password().getBytes(StandardCharsets.UTF_8);
               if (username.length > 255 || password.length > 255 || frozen.username().indexOf(0) >= 0) {
                  throw new IOException("Invalid connection proxy credentials");
               }

               if (frozen.type() == ProxyProtocolType.Socks4) {
                  byte[] address = target.addresses().getFirst().getAddress();
                  if (address.length != 4) {
                     throw new IOException("SOCKS4 resource packs require an IPv4 target");
                  }

                  output.write(new byte[]{4, 1, (byte)(target.port() >>> 8), (byte)target.port()});
                  output.write(address);
                  output.write(username);
                  output.write(0);
                  output.flush();
                  byte[] reply = readExactly(input, 8);
                  if (reply[0] != 0 || reply[1] != 90) {
                     throw new IOException("SOCKS4 resource-pack connection refused");
                  }

                  this.ensureCurrent();
                  return;
               }
            }

            boolean authenticated = authentication != null || frozen != null && (username.length != 0 || password.length != 0);
            output.write(authenticated ? new byte[]{5, 2, 0, 2} : new byte[]{5, 1, 0});
            output.flush();
            byte[] greeting = readExactly(input, 2);
            if (greeting[0] != 5) {
               throw new IOException("SOCKS5 proxy returned an invalid greeting");
            }

            int method = greeting[1] & 255;
            if (method == 2) {
               if (!authenticated) {
                  throw new IOException("SOCKS5 proxy requires unsupported authentication");
               }

               if (authentication != null) {
                  username = authentication.usernameBytes();
                  password = authentication.password();
               }

               if (username.length == 0 || password.length == 0) {
                  throw new IOException("Invalid SOCKS5 proxy credentials");
               }

               authenticationRequest = new ScopedHttpUrlConnection.ByteBuilder(3 + username.length + password.length);
               authenticationRequest.appendByte(1);
               authenticationRequest.appendByte(username.length);
               authenticationRequest.appendBytes(username);
               authenticationRequest.appendByte(password.length);
               authenticationRequest.appendBytes(password);
               output.write(authenticationRequest.rawBytes(), 0, authenticationRequest.byteCount());
               output.flush();
               byte[] authenticationReply = readExactly(input, 2);
               if (authenticationReply[0] != 1 || authenticationReply[1] != 0) {
                  throw new IOException("SOCKS5 proxy authentication failed");
               }
            } else if (method != 0) {
               throw new IOException("SOCKS5 proxy requires unsupported authentication");
            }

            InetAddress address = target.addresses().getFirst();
            byte[] raw = address.getAddress();
            ScopedHttpUrlConnection.ByteBuilder request = new ScopedHttpUrlConnection.ByteBuilder(7 + raw.length);
            request.appendByte(5);
            request.appendByte(1);
            request.appendByte(0);
            request.appendByte(raw.length == 4 ? 1 : 4);
            request.appendBytes(raw);
            request.appendByte(target.port() >>> 8);
            request.appendByte(target.port() & 0xFF);
            output.write(request.rawBytes(), 0, request.byteCount());
            output.flush();
            byte[] reply = readExactly(input, 4);
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
                  var10000 = readExactly(input, 1)[0] & 255;
                  break;
               case 4:
                  var10000 = 16;
            }

            int length = var10000;
            if (length == 0) {
               throw new IOException("SOCKS5 proxy returned an empty address");
            }

            readExactly(input, length + 2);
            this.ensureCurrent();
         } finally {
            if (username != null) {
               Arrays.fill(username, (byte)0);
            }

            if (password != null) {
               Arrays.fill(password, (byte)0);
            }

            if (authenticationRequest != null) {
               Arrays.fill(authenticationRequest.rawBytes(), (byte)0);
            }
         }
      } else {
         throw new IOException("Resource-pack proxy differs from its connection route");
      }
   }

   private void init(Socket socket, PackUrlValidator.ParsedTarget target, PackUrlValidator.PackRoute route) throws IOException {
      String host = target.load().indexOf(58) >= 0 ? "[" + target.load() + "]" : target.load();
      String connectAuthority = host + ":" + target.port();
      StringBuilder request = new StringBuilder("CONNECT ").append(connectAuthority).append(" HTTP/1.1\r\nHost: ").append(target.read()).append("\r\n");
      this.appendHeaderOrDefault(request, "Accept", "*/*");
      this.appendHeaderOrDefault(request, "Proxy-Connection", "keep-alive");
      this.appendProxyAuthorization(request, route);
      request.append("\r\n");
      socket.getOutputStream().write(request.toString().getBytes(StandardCharsets.ISO_8859_1));
      socket.getOutputStream().flush();
      ScopedHttpUrlConnection.ResponseHead response = this.readResponseHead(this.socketInputFor(socket));
      if (response.code != 200) {
         throw new IOException("HTTP proxy CONNECT failed with status " + response.code);
      }

      this.ensureCurrent();
   }

   private void init(Socket socket, PackUrlValidator.ParsedTarget target, PackUrlValidator.ParsedTarget previousTarget, PackUrlValidator.PackRoute route) throws IOException {
      String requestTarget = target.write();
      boolean plainHttpProxy = route != null && route.proxyType() == Type.HTTP && "http".equals(target.init_Str());
      if (plainHttpProxy) {
         requestTarget = target.init_Str() + "://" + target.uri().getRawAuthority() + target.write();
      }

      StringBuilder request = new StringBuilder("GET ").append(requestTarget).append(" HTTP/1.1\r\nHost: ").append(target.read()).append("\r\n");
      boolean authorityChanged = previousTarget != null && !PackUrlValidator.sameAuthority(previousTarget, target);

      for (Entry<String, List<String>> entry : this.requestHeaders.entrySet()) {
         String name = entry.getKey();
         if (!name.equalsIgnoreCase("Host")
            && !name.equalsIgnoreCase("Proxy-Authorization")
            && (!authorityChanged || !name.equalsIgnoreCase("Authorization") && !name.equalsIgnoreCase("Cookie") && !name.equalsIgnoreCase("Cookie2"))) {
            for (String value : entry.getValue()) {
               appendHeader(request, name, value);
            }
         }
      }

      if (plainHttpProxy) {
         this.appendProxyAuthorization(request, route);
         if (headerValues(this.requestHeaders, "Proxy-Connection").isEmpty()) {
            request.append("Proxy-Connection: keep-alive\r\n");
         }
      } else if (headerValues(this.requestHeaders, "Connection").isEmpty()) {
         request.append("Connection: keep-alive\r\n");
      }

      if (headerValues(this.requestHeaders, "Accept").isEmpty()) {
         request.append("Accept: */*\r\n");
      }

      request.append("\r\n");
      byte[] bytes = request.toString().getBytes(StandardCharsets.ISO_8859_1);
      this.ensureCurrent();
      socket.getOutputStream().write(bytes);
      socket.getOutputStream().flush();
   }

   private void appendHeaderOrDefault(StringBuilder request, String name, String defaultValue) {
      List<String> values = headerValues(this.requestHeaders, name);
      if (values.isEmpty()) {
         appendHeader(request, name, defaultValue);
      } else {
         for (String value : values) {
            appendHeader(request, name, value);
         }
      }
   }

   private void appendProxyAuthorization(StringBuilder request, PackUrlValidator.PackRoute route) {
      if (route.viaProxy()) {
         for (String value : headerValues(this.requestHeaders, "Proxy-Authorization")) {
            appendHeader(request, "Proxy-Authorization", value);
         }
      }
   }

   private static void appendHeader(StringBuilder request, String name, String value) {
      request.append(name).append(": ").append(value).append("\r\n");
   }

   private ScopedHttpUrlConnection.ResponseHead readResponseHead(InputStream input) throws IOException {
      int aggregate = 0;
      int interim = 0;

      while (true) {
         String status = readHeaderLine(input);
         aggregate += status.length() + 2;
         if (aggregate > 65536) {
            throw new IOException("HTTP response headers are too large");
         }

         ScopedHttpUrlConnection.StatusLine statusLine = parseStatusLine(status);
         LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();

         while (true) {
            String line = readHeaderLine(input);
            aggregate += line.length() + 2;
            if (aggregate > 65536) {
               throw new IOException("HTTP response headers are too large");
            }

            if (line.isEmpty()) {
               if (statusLine.code >= 100 && statusLine.code < 200) {
                  if (statusLine.code != 101 && interim < 7) {
                     interim++;
                     break;
                  }

                  throw new IOException("Too many or unsupported interim HTTP responses");
               }

               return new ScopedHttpUrlConnection.ResponseHead(status, statusLine.code, statusLine.reasonPhrase, headers, -1L, false, false);
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
               validateHeader(name, value);
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

   private static ScopedHttpUrlConnection.StatusLine parseStatusLine(String line) throws IOException {
      int first = line.indexOf(32);
      if (first >= 0 && (line.startsWith("HTTP/1.1") || line.startsWith("HTTP/1.0"))) {
         int second = line.indexOf(32, first + 1);
         String code = second < 0 ? line.substring(first + 1) : line.substring(first + 1, second);
         if (code.length() == 3 && code.chars().allMatch(Character::isDigit)) {
            String message = second < 0 ? "" : line.substring(second + 1);
            return new ScopedHttpUrlConnection.StatusLine(Integer.parseInt(code), message);
         } else {
            throw new IOException("Malformed HTTP response status");
         }
      } else {
         throw new IOException("Malformed HTTP status line");
      }
   }

   private ScopedHttpUrlConnection.ResponseHead applyTransferEncoding(ScopedHttpUrlConnection.ResponseHead response) throws IOException {
      List<String> transfer = response.header("Transfer-Encoding");
      List<String> lengths = response.header("Content-Length");
      if (!transfer.isEmpty() && !lengths.isEmpty()) {
         throw new IOException("Ambiguous HTTP response framing");
      }

      boolean noBody = response.code == 204 || response.code == 304 || response.code >= 100 && response.code < 200;
      boolean chunked = false;
      if (!transfer.isEmpty()) {
         List<String> codings = splitHeaderValues(transfer);
         if (codings.size() != 1 || !codings.getFirst().equalsIgnoreCase("chunked")) {
            throw new IOException("Unsupported HTTP transfer coding");
         }

         chunked = true;
      }

      long contentLength = -1L;
      if (!lengths.isEmpty()) {
         for (String value : splitHeaderValues(lengths)) {
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

      return response.withBody(contentLength, chunked, noBody);
   }

   private InputStream openResponseStream() throws IOException {
      if (this.responseBody != null) {
         return this.responseBody;
      }

      Socket socket = this.activeSocket;
      if (socket == null) {
         throw new IOException("Response body has no active socket");
      }

      ScopedHttpUrlConnection.ResponseHead response = this.response;
      InputStream input = this.socketInputFor(socket);
      if (response.noBody) {
         this.responseBody = new ScopedHttpUrlConnection.BodyStream(this, socket, InputStream.nullInputStream(), true, response.code >= 400);
      } else if (response.chunked) {
         this.responseBody = new ScopedHttpUrlConnection.ChunkedBodyStream(this, socket, input, response.code >= 400);
      } else if (response.contentLength >= 0L) {
         this.responseBody = new ScopedHttpUrlConnection.FixedLengthBodyStream(this, socket, input, response.contentLength, response.code >= 400);
      } else {
         this.responseBody = new ScopedHttpUrlConnection.UntilCloseBodyStream(this, socket, input, response.code >= 400);
      }

      return this.responseBody;
   }

   private String redirectLocation(ScopedHttpUrlConnection.ResponseHead response) throws ProtocolException {
      if (!isRedirectStatus(response.code)) {
         return null;
      }

      List<String> locations = response.header("Location");
      if (locations.isEmpty()) {
         if (response.code == 305) {
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

   private static PackUrlValidator.ParsedTarget resolveRedirectTarget(PackUrlValidator.ParsedTarget current, String location) throws MalformedURLException {
      try {
         URI resolved = current.uri().resolve(new URI(location));
         return PackUrlValidator.parseUri(resolved);
      } catch (URISyntaxException | IllegalArgumentException e) {
         MalformedURLException malformed = new MalformedURLException("Invalid resource-pack redirect");
         malformed.initCause(e);
         throw malformed;
      }
   }

   private static boolean isRedirectStatus(int status) {
      return status == 300 || status == 301 || status == 302 || status == 303 || status == 305 || status == 307;
   }

   private static int maxRedirects() {
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

   private static int timeoutOrProperty(int explicit, String property) {
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

   private void ensureCurrent() throws IOException {
      if (this.disconnected || !this.stillCurrent.getAsBoolean() || !this.scopeSnapshot.isCurrent()) {
         throw new IOException("The resource-pack connection is no longer current");
      }
   }

   private void closeActiveSocket() {
      Socket socket = this.activeSocket;
      this.activeSocket = null;
      this.socketInput = null;
      if (socket != null) {
         this.closeSocket(socket);
      }
   }

   private void closeSocket(Socket socket) {
      if (this.activeSocket == socket) {
         this.activeSocket = null;
         this.socketInput = null;
      }

      try {
         socket.close();
      } catch (IOException var6) {
      } finally {
         ResourcePackDownloadScope.load(this.scopeSnapshot, socket);
      }
   }

   private void releaseSocket(Socket socket, Socket expected) {
      if (this.activeSocket == expected) {
         this.closeActiveSocket();
      } else {
         this.closeSocket(socket);
      }
   }

   private static String readHeaderLine(InputStream input) throws EOFException, IOException {
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

   private static byte[] readExactly(InputStream input, int count) throws EOFException, IOException {
      byte[] result = input.readNBytes(count);
      if (result.length != count) {
         throw new EOFException("Unexpected EOF in proxy negotiation");
      } else {
         return result;
      }
   }

   private static void validateHeader(String name, String value) {
      if (name == null || name.isEmpty() || !name.chars().allMatch(ScopedHttpUrlConnection::isTokenChar)) {
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

   private static boolean isTokenChar(int c) {
      return c > 32 && c < 127 ? "()<>@,;:\\\"/[]?={}\t".indexOf(c) < 0 : false;
   }

   private static boolean isIpLiteralHost(String host) {
      return host.indexOf(58) >= 0 ? true : host.matches("[0-9.]+");
   }

   private static List<String> headerValues(Map<String, List<String>> headers, String name) {
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

   private static void removeHeader(Map<String, List<String>> headers, String name) {
      headers.keySet().removeIf(key -> key.equalsIgnoreCase(name));
   }

   private static Map<String, List<String>> copyHeaders(Map<String, List<String>> source) {
      LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();

      for (Entry<String, List<String>> entry : source.entrySet()) {
         result.put(entry.getKey(), Collections.unmodifiableList(List.copyOf(entry.getValue())));
      }

      return Collections.unmodifiableMap(result);
   }

   private static List<String> splitHeaderValues(List<String> values) {
      List<String> result = new ArrayList<>();

      for (String value : values) {
         for (String part : value.split(",", -1)) {
            result.add(part.trim());
         }
      }

      return result;
   }

   private static final class NoBodyStream extends ScopedHttpUrlConnection.BodyStream {
      private NoBodyStream(ScopedHttpUrlConnection owner, Socket socket, boolean closeOnEof) {
         super(owner, socket, InputStream.nullInputStream(), true, closeOnEof);
      }
   }

   private static final class ChunkedBodyStream extends ScopedHttpUrlConnection.BodyStream {
      private long chunkRemaining;
      private boolean chunkEndPending;
      private boolean reachedEnd;

      private ChunkedBodyStream(ScopedHttpUrlConnection owner, Socket socket, InputStream input, boolean closeOnEof) {
         super(owner, socket, input, false, closeOnEof);
      }

      @Override
      protected int readBody(byte[] bytes, int offset, int length) throws EOFException, IOException {
         if (this.reachedEnd) {
            return -1;
         }

         if (this.chunkEndPending) {
            byte[] ending = ScopedHttpUrlConnection.readExactly(this.delegate, 2);
            if (ending[0] != 13 || ending[1] != 10) {
               throw new IOException("Malformed chunk ending");
            }

            this.chunkEndPending = false;
         }

         if (this.chunkRemaining == 0L) {
            String line = ScopedHttpUrlConnection.readHeaderLine(this.delegate);
            int extension = line.indexOf(59);
            String size = (extension < 0 ? line : line.substring(0, extension)).trim();
            if (size.isEmpty() || !size.chars().allMatch(c -> Character.digit(c, 16) >= 0)) {
               throw new IOException("Malformed chunk size");
            }

            try {
               this.chunkRemaining = Long.parseLong(size, 16);
            } catch (NumberFormatException e) {
               throw new IOException("Chunk size is too large", e);
            }

            if (this.chunkRemaining == 0L) {
               skipTrailers(this.delegate);
               this.reachedEnd = true;
               return -1;
            }
         }

         int read = this.delegate.read(bytes, offset, (int)Math.min(length, this.chunkRemaining));
         if (read < 0) {
            throw new EOFException("Unexpected EOF in chunked body");
         }

         this.chunkRemaining -= read;
         if (this.chunkRemaining == 0L) {
            this.chunkEndPending = true;
         }

         return read;
      }

      private static void skipTrailers(InputStream input) throws IOException {
         int aggregate = 0;

         while (true) {
            String line = ScopedHttpUrlConnection.readHeaderLine(input);
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
               ScopedHttpUrlConnection.validateHeader(line.substring(0, colon), line.substring(colon + 1).trim());
            } catch (IllegalArgumentException e) {
               throw new IOException("Malformed HTTP trailer", e);
            }
         }
      }
   }

   private static final class UntilCloseBodyStream extends ScopedHttpUrlConnection.BodyStream {
      private UntilCloseBodyStream(ScopedHttpUrlConnection owner, Socket socket, InputStream input, boolean closeOnEof) {
         super(owner, socket, input, false, closeOnEof);
      }
   }

   private static class BodyStream extends InputStream {
      private final ScopedHttpUrlConnection owner;
      private final Socket socket;
      protected final InputStream delegate;
      private final boolean closeOnEof;
      private boolean eof;

      private BodyStream(ScopedHttpUrlConnection owner, Socket socket, InputStream input, boolean done, boolean closeOnEof) {
         this.owner = owner;
         this.socket = socket;
         this.delegate = input;
         this.closeOnEof = closeOnEof;
         this.eof = done;
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

         if (this.eof) {
            return -1;
         }

         this.owner.ensureCurrent();
         int result = this.readBody(bytes, offset, length);
         if (result < 0) {
            this.eof = true;
            if (this.closeOnEof) {
               this.close();
            }
         }

         return result;
      }

      protected int readBody(byte[] bytes, int offset, int length) throws IOException {
         return this.delegate.read(bytes, offset, length);
      }

      @Override
      public int available() throws IOException {
         return this.eof ? 0 : this.delegate.available();
      }

      @Override
      public void close() {
         if (this.eof && !this.closeOnEof) {
            this.owner.releaseSocket(this.socket, this.socket);
         } else {
            this.eof = true;
            this.owner.releaseSocket(this.socket, this.socket);
         }
      }
   }

   private static final class ResponseHead {
      private final String statusLine;
      private final int code;
      private final String message;
      private final LinkedHashMap<String, List<String>> headers;
      private final long contentLength;
      private final boolean chunked;
      private final boolean noBody;

      private ResponseHead(
         String statusLine, int code, String message, LinkedHashMap<String, List<String>> headers, long contentLength, boolean chunked, boolean noBody
      ) {
         this.statusLine = statusLine;
         this.code = code;
         this.message = message;
         this.headers = headers;
         this.contentLength = contentLength;
         this.chunked = chunked;
         this.noBody = noBody;
      }

      private List<String> header(String name) {
         return ScopedHttpUrlConnection.headerValues(this.headers, name);
      }

      private ScopedHttpUrlConnection.ResponseHead withBody(long length, boolean chunked, boolean noBody) {
         return new ScopedHttpUrlConnection.ResponseHead(this.statusLine, this.code, this.message, this.headers, length, chunked, noBody);
      }
   }

   @FunctionalInterface
   public interface PlainSocketFactory {
      Socket create();
   }

   private record StatusLine(int code, String reasonPhrase) {
   }

   private static final class ByteBuilder {
      private final byte[] bytes;
      private int length;

      private ByteBuilder(int capacity) {
         this.bytes = new byte[capacity];
      }

      private void appendByte(int value) {
         this.bytes[this.length++] = (byte)value;
      }

      private void appendBytes(byte[] value) {
         System.arraycopy(value, 0, this.bytes, this.length, value.length);
         this.length += value.length;
      }

      private byte[] rawBytes() {
         return this.bytes;
      }

      private int byteCount() {
         return this.length;
      }
   }

   @FunctionalInterface
   public interface TlsSocketFactory {
      Socket create(Socket var1, String var2, int var3, boolean var4) throws IOException;
   }

   private static final class FixedLengthBodyStream extends ScopedHttpUrlConnection.BodyStream {
      private long remaining;

      private FixedLengthBodyStream(ScopedHttpUrlConnection owner, Socket socket, InputStream input, long length, boolean closeOnEof) {
         super(owner, socket, input, length == 0L, closeOnEof);
         this.remaining = length;
      }

      @Override
      protected int readBody(byte[] bytes, int offset, int length) throws EOFException, IOException {
         if (this.remaining == 0L) {
            return -1;
         }

         int read = this.delegate.read(bytes, offset, (int)Math.min(length, this.remaining));
         if (read < 0) {
            throw new EOFException("HTTP body ended before Content-Length");
         }

         this.remaining -= read;
         return read;
      }

      @Override
      public int available() throws IOException {
         return (int)Math.min(super.available(), this.remaining);
      }
   }
}
