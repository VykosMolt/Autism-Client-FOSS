package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.compat263.Compat263;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import net.minecraft.util.Util;

public final class aW {
   private static final String load_Str = "c36a9fb6-4f2a-41ff-90bd-ae7cc92031eb";
   public static final String init_Str = init_Str();
   public static final int init_I = 9675;
   private static final String read = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize";
   private static final String write = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
   private static final String update = "XboxLive.SignIn XboxLive.offline_access";
   private static volatile String sync = "";
   private static volatile String flush = "";
   private static volatile HttpServer init_HttpServer;
   private static volatile Consumer<String> init_Consumer;
   private static final Semaphore init_Semaphore = new Semaphore(2, true);
   private static final Object init_Obj = new Object();
   private static volatile long init_J;
   private static final long load_J = 400L;
   private static final String close = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";
   private static final HttpClient init_HttpClient = HttpClient.newBuilder()
      .proxy(MapStore.init_ProxySelector())
      .connectTimeout(Duration.ofSeconds(20L))
      .build();

   private static String init_Str() {
      String prop = System.getProperty("autism.msauth.clientId");
      if (prop != null && !prop.isBlank()) {
         return prop.trim();
      }

      String env = System.getenv("AUTISM_MSAUTH_CLIENT_ID");
      return env != null && !env.isBlank() ? env.trim() : "c36a9fb6-4f2a-41ff-90bd-ae7cc92031eb";
   }

   private aW() {
   }

   public static String init(Consumer<String> callback) {
      init_Consumer = callback;
      sync = read();
      flush = read();
      load_V();
      String url = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize?client_id="
         + init_Str
         + "&response_type=code&redirect_uri="
         + init_Str(load_Str())
         + "&scope="
         + init_Str("XboxLive.SignIn XboxLive.offline_access")
         + "&code_challenge="
         + init_Str(load_Str(sync))
         + "&code_challenge_method=S256&state="
         + init_Str(flush)
         + "&prompt=select_account";
      Compat263.openUri(Util.getPlatform(), url);
      return url;
   }

   public static aW.Inner_StreamUtil init_aW_StreamUtil(String refreshToken) {
      try {
         init_Semaphore.acquire();
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         return aW.Inner_StreamUtil.init("Login interrupted");
      }

      try {
         synchronized (init_Obj) {
            long wait = init_J + 400L - System.currentTimeMillis();
            if (wait > 0L) {
               Thread.sleep(wait);
            }

            init_J = System.currentTimeMillis();
         }

         return load_aW_StreamUtil(refreshToken);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         return aW.Inner_StreamUtil.init("Login interrupted");
      } finally {
         init_Semaphore.release();
      }
   }

   private static aW.Inner_StreamUtil load_aW_StreamUtil(String refreshToken) {
      aW.Inner_DataUtil tokenReply = init(
         "https://login.microsoftonline.com/consumers/oauth2/v2.0/token",
         "client_id="
            + init_Str
            + "&refresh_token="
            + init_Str(refreshToken)
            + "&grant_type=refresh_token&scope="
            + init_Str("XboxLive.SignIn XboxLive.offline_access")
      );
      JsonObject tokenResponse = tokenReply.init_JsonObject();
      if (tokenResponse != null && tokenResponse.has("access_token") && tokenResponse.has("refresh_token")) {
         String accessToken = tokenResponse.get("access_token").getAsString();
         refreshToken = tokenResponse.get("refresh_token").getAsString();
         aW.Inner_DataUtil xblReply = load(
            "https://user.auth.xboxlive.com/user/authenticate",
            "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d="
               + accessToken
               + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}"
         );
         JsonObject xbl = xblReply.init_JsonObject();
         if (xblReply.init_Z() && xbl != null && xbl.has("Token")) {
            aW.Inner_DataUtil xstsReply = load(
               "https://xsts.auth.xboxlive.com/xsts/authorize",
               "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\""
                  + xbl.get("Token").getAsString()
                  + "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}"
            );
            JsonObject xsts = xstsReply.init_JsonObject();
            if (xstsReply.init_Z() && xsts != null && xsts.has("Token")) {
               String uhs = init(xbl);
               if (uhs == null) {
                  uhs = init(xsts);
               }

               if (uhs == null) {
                  return auditFailed("Xbox Live response was malformed (no user hash)", refreshToken);
               }

               aW.Inner_DataUtil mcReply = load(
                  "https://api.minecraftservices.com/authentication/login_with_xbox",
                  "{\"identityToken\":\"XBL3.0 x=" + uhs + ";" + xsts.get("Token").getAsString() + "\"}"
               );
               JsonObject mc = mcReply.init_JsonObject();
               if (mcReply.init_Z() && mc != null && mc.has("access_token")) {
                  String mcToken = mc.get("access_token").getAsString();
                  long expiresIn = mc.has("expires_in") ? mc.get("expires_in").getAsLong() : 86400L;
                  aW.Inner_DataUtil profileReply = read("https://api.minecraftservices.com/minecraft/profile", mcToken);
                  JsonObject profile = profileReply.init_JsonObject();
                  if (profileReply.init_I() != 404 && profile != null && profile.has("id") && profile.has("name")) {
                     return new aW.Inner_StreamUtil(mcToken, refreshToken, profile.get("id").getAsString(), profile.get("name").getAsString(), expiresIn);
                  }

                  String reason = profileReply.init_I() == 404
                     ? "This Microsoft account doesn't own Minecraft: Java Edition"
                     : "Couldn't read the Minecraft profile (" + init(profileReply) + ")";
                  AutismClientAddon.LOG.warn("MS login: {}", reason);
                  return auditFailed(reason, refreshToken);
               } else {
                  String reason = "Minecraft services login failed (" + init(mcReply) + ")";
                  AutismClientAddon.LOG.warn("MS login: {}", reason);
                  return auditFailed(reason, refreshToken);
               }
            } else {
               String reason = load(xsts);
               AutismClientAddon.LOG.warn("MS login: XSTS failed ({}): {}", xstsReply.init_I(), reason);
               return auditFailed(reason, refreshToken);
            }
         } else {
            String reason = "Xbox Live authentication failed (" + init(xblReply) + ")";
            AutismClientAddon.LOG.warn("MS login: {}", reason);
            return auditFailed(reason, refreshToken);
         }
      } else {
         String reason = load(tokenReply);
         AutismClientAddon.LOG.warn("MS login: token refresh failed ({}): {}", tokenReply.init_I(), reason);
         return aW.Inner_StreamUtil.init(reason);
      }
   }

   // Microsoft already rotated the refresh token, so a failure must still hand back the new one.
   private static aW.Inner_StreamUtil auditFailed(String reason, String newRefreshToken) {
      return new aW.Inner_StreamUtil(null, newRefreshToken, null, null, 0L, reason);
   }

   private static String init(JsonObject xboxResponse) {
      try {
         return xboxResponse.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
      } catch (Exception e) {
         return null;
      }
   }

   private static String init(aW.Inner_DataUtil reply) {
      if (reply == null) {
         return "no response";
      }

      JsonObject body = reply.init_JsonObject();
      if (body != null) {
         for (String key : new String[]{"error_description", "message", "errorMessage", "error"}) {
            if (body.has(key)) {
               try {
                  String value = body.get(key).getAsString();
                  if (value != null && !value.isBlank()) {
                     return "HTTP " + reply.init_I() + ": " + value.split("\\r?\\n")[0].trim();
                  }
               } catch (Exception var7) {
               }
            }
         }
      }

      return reply.init_I() < 0 ? "no response (network)" : "HTTP " + reply.init_I();
   }

   private static String load(aW.Inner_DataUtil reply) {
      JsonObject body = reply == null ? null : reply.init_JsonObject();
      if (body != null) {
         try {
            if (body.has("error_description") && !body.get("error_description").getAsString().isBlank()) {
               return "Microsoft: " + body.get("error_description").getAsString().split("\\r?\\n")[0].trim();
            }

            if (body.has("error") && !body.get("error").getAsString().isBlank()) {
               return "Microsoft error: " + body.get("error").getAsString();
            }
         } catch (Exception var3) {
         }
      }

      int status = reply == null ? -1 : reply.init_I();
      return status < 0 ? "Couldn't reach Microsoft (check your connection)" : "Microsoft token request failed (HTTP " + status + ")";
   }

   private static String load(JsonObject xsts) {
      if (xsts == null) {
         return "Xbox authorization failed";
      }

      if (xsts.has("XErr")) {
         long code = xsts.get("XErr").getAsLong();
         if (code == 2148916233L) {
            return "This account has no Xbox profile — create one at xbox.com first";
         }

         if (code == 2148916235L) {
            return "Xbox Live isn't available in this account's country/region";
         }

         if (code == 2148916236L || code == 2148916237L) {
            return "This account needs adult verification";
         }

         if (code == 2148916238L) {
            return "Child account — an adult must add it to a Microsoft Family";
         }
      }

      return "Xbox authorization failed";
   }

   private static String load_Str() {
      return "http://127.0.0.1:9675";
   }

   private static String init_Str(String value) {
      return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
   }

   private static String read() {
      byte[] bytes = new byte[64];
      new SecureRandom().nextBytes(bytes);
      return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
   }

   private static String load_Str(String verifier) {
      try {
         byte[] digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
         return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
      } catch (Exception e) {
         return verifier;
      }
   }

   private static Builder init_HttpRequest_Builde(String url) {
      return HttpRequest.newBuilder(URI.create(url))
         .timeout(Duration.ofSeconds(20L))
         .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36")
         .header("Accept", "application/json");
   }

   private static aW.Inner_DataUtil init(HttpRequest request) {
      try {
         HttpResponse<String> res = init_HttpClient.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
         return new aW.Inner_DataUtil(res.statusCode(), res.body());
      } catch (Exception e) {
         return new aW.Inner_DataUtil(-1, null);
      }
   }

   private static aW.Inner_DataUtil init(String url, String body) {
      return auditSend(MapStore.init_Proxy(), url, "application/x-www-form-urlencoded", body, null);
   }

   private static aW.Inner_DataUtil load(String url, String body) {
      return auditSend(MapStore.init_Proxy(), url, "application/json", body, null);
   }

   private static aW.Inner_DataUtil read(String url, String bearer) {
      return auditSend(MapStore.init_Proxy(), url, null, null, bearer);
   }

   // java.net.http only knows HTTP proxies: handed the game's launch proxy, which is a SOCKS one, it connects
   // direct without a word. Those requests go through HttpURLConnection, which does use it.
   static aW.Inner_DataUtil auditSend(Proxy proxy, String url, String contentType, String body, String bearer) {
      if (proxy.type() != Proxy.Type.SOCKS) {
         Builder request = init_HttpRequest_Builde(url);
         if (bearer != null) {
            request.header("Authorization", "Bearer " + bearer);
         }

         return init(body == null ? request.GET().build() : request.header("Content-Type", contentType).POST(BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build());
      }

      try {
         HttpURLConnection connection = (HttpURLConnection)URI.create(url).toURL().openConnection(proxy);
         connection.setConnectTimeout(20000);
         connection.setReadTimeout(20000);
         connection.setInstanceFollowRedirects(false);
         connection.setRequestProperty("User-Agent", close);
         connection.setRequestProperty("Accept", "application/json");
         if (bearer != null) {
            connection.setRequestProperty("Authorization", "Bearer " + bearer);
         }

         if (body != null) {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", contentType);

            try (OutputStream out = connection.getOutputStream()) {
               out.write(body.getBytes(StandardCharsets.UTF_8));
            }
         }

         int status = connection.getResponseCode();

         try (InputStream in = status >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
            return new aW.Inner_DataUtil(status, in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8));
         }
      } catch (Exception e) {
         return new aW.Inner_DataUtil(-1, null);
      }
   }

   private static void load_V() {
      if (init_HttpServer == null) {
         try {
            init_HttpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 9675), 0);
            init_HttpServer.createContext("/", aW::init);
            init_HttpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            init_HttpServer.start();
         } catch (IOException e) {
            init_V();
            AutismClientMessaging.sendPrefixed("Failed to start Microsoft login server.");
         }
      }
   }

   public static void init_V() {
      if (init_HttpServer != null) {
         init_HttpServer.stop(0);
         init_HttpServer = null;
         init_Consumer = null;
         flush = "";
      }
   }

   private static void init(HttpExchange request) throws IOException {
      try {
         // Stray requests (favicon, preflight) get an error and the server keeps waiting for the real callback.
         if (!"GET".equals(request.getRequestMethod())) {
            init(request, 405, "Method not allowed.");
            return;
         }

         List<aW.Inner_CachePool> query = init_List(request.getRequestURI().getRawQuery());
         String code = null;
         String returnedState = null;
         String error = null;

         for (aW.Inner_CachePool pair : query) {
            if ("code".equals(pair.init())) {
               code = pair.load();
            } else if ("state".equals(pair.init())) {
               returnedState = pair.load();
            } else if ("error".equals(pair.init())) {
               error = pair.load();
            }
         }

         if (code == null && error == null) {
            init(request, 400, "Missing authorization code.");
            return;
         }

         boolean ok = code != null && !flush.isBlank() && flush.equals(returnedState);
         if (ok) {
            init_V(code);
         }

         if (!ok) {
            init(request, 200, "Cannot authenticate.");
            Consumer<String> current = init_Consumer;
            if (current != null) {
               current.accept(null);
            }
         } else {
            init(request, 200, "You may now close this page.");
         }
      } finally {
         request.close();
      }

      init_V();
   }

   private static void init_V(String code) {
      aW.Inner_DataUtil reply = init(
         "https://login.microsoftonline.com/consumers/oauth2/v2.0/token",
         "client_id="
            + init_Str
            + "&code="
            + init_Str(code)
            + "&grant_type=authorization_code&redirect_uri="
            + init_Str(load_Str())
            + "&scope="
            + init_Str("XboxLive.SignIn XboxLive.offline_access")
            + "&code_verifier="
            + init_Str(sync)
      );
      JsonObject response = reply.init_JsonObject();
      Consumer<String> current = init_Consumer;
      if (current != null) {
         current.accept(response != null && response.has("refresh_token") ? response.get("refresh_token").getAsString() : null);
      }
   }

   private static void init(HttpExchange request, int status, String text) throws IOException {
      byte[] responseBody = text.getBytes(StandardCharsets.UTF_8);
      request.sendResponseHeaders(status, responseBody.length);

      try (OutputStream output = request.getResponseBody()) {
         output.write(responseBody);
      }
   }

   private static List<aW.Inner_CachePool> init_List(String string) {
      List<aW.Inner_CachePool> query = new ArrayList<>();
      if (string == null) {
         return query;
      }

      char[] buf = string.toCharArray();
      int i = 0;

      while (i < buf.length) {
         StringBuilder name = new StringBuilder();
         StringBuilder value = new StringBuilder();

         while (i < buf.length && buf[i] != '&' && buf[i] != ';' && buf[i] != '=') {
            name.append(buf[i]);
            i++;
         }

         if (i < buf.length) {
            char ch = buf[i++];
            if (ch == '=') {
               while (i < buf.length) {
                  if (buf[i] == '&' || buf[i] == ';') {
                     i++;
                     break;
                  }

                  value.append(buf[i]);
                  i++;
               }
            }
         }

         if (!name.isEmpty()) {
            query.add(new aW.Inner_CachePool(read(name.toString()), read(value.toString())));
         }
      }

      return query;
   }

   private static String read(String s) {
      if (s == null) {
         return null;
      }

      ByteBuffer bb = ByteBuffer.allocate(s.length());
      CharBuffer cb = CharBuffer.wrap(s);

      while (cb.hasRemaining()) {
         char c = cb.get();
         if (c == '%' && cb.remaining() >= 2) {
            char uc = cb.get();
            char lc = cb.get();
            int u = Character.digit(uc, 16);
            int l = Character.digit(lc, 16);
            if (u != -1 && l != -1) {
               bb.put((byte)((u << 4) + l));
            } else {
               bb.put((byte)37);
               bb.put((byte)uc);
               bb.put((byte)lc);
            }
         } else if (c == '+') {
            bb.put((byte)32);
         } else {
            bb.put((byte)c);
         }
      }

      bb.flip();
      return StandardCharsets.UTF_8.decode(bb).toString();
   }

   private record Inner_CachePool(String init, String load) {
   }

   record Inner_DataUtil(int init_I, String init_Str) {
      boolean init_Z() {
         return this.init_I >= 200 && this.init_I < 300;
      }

      JsonObject init_JsonObject() {
         try {
            return this.init_Str != null && !this.init_Str.isBlank() ? JsonParser.parseString(this.init_Str).getAsJsonObject() : null;
         } catch (Exception e) {
            return null;
         }
      }
   }

   public static final class Inner_StreamUtil {
      public final String init_Str;
      public final String load;
      public final String read;
      public final String write;
      public final String update;
      public final long init_J;

      public Inner_StreamUtil() {
         this(null, null, null, null, 0L, null);
      }

      public Inner_StreamUtil(String mcToken, String newRefreshToken, String uuid, String username, long expiresInSeconds) {
         this(mcToken, newRefreshToken, uuid, username, expiresInSeconds, null);
      }

      private Inner_StreamUtil(String mcToken, String newRefreshToken, String uuid, String username, long expiresInSeconds, String error) {
         this.init_Str = mcToken;
         this.load = newRefreshToken;
         this.read = uuid;
         this.write = username;
         this.init_J = expiresInSeconds;
         this.update = error;
      }

      static aW.Inner_StreamUtil init(String error) {
         return new aW.Inner_StreamUtil(null, null, null, null, 0L, error);
      }

      public boolean init_Z() {
         return this.init_Str != null;
      }

      public long init_J() {
         return this.init_J > 0L ? System.currentTimeMillis() + this.init_J * 1000L : 0L;
      }
   }
}
