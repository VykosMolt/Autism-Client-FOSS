package autismclient.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;

public final class AutismHttp {
   private AutismHttp() {
   }

   public static JsonObject init_JsonObject(String url, String bearerToken) {
      return init_JsonObject(url, bearerToken, Map.of());
   }

   public static JsonObject load_JsonObject(String url, String bearerToken) {
      HttpURLConnection connection = null;
      try {
         connection = load(url, 15000);
         connection.setRequestMethod("GET");
         if (bearerToken != null && !bearerToken.isBlank()) {
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
         }

         connection.setRequestProperty("Accept", "application/json");
         return init(connection);
      } catch (Exception e) {
         return null;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public static JsonObject init_JsonObject(String url, String bearerToken, Map<String, String> headers) {
      HttpURLConnection connection = null;
      try {
         connection = init(url);
         connection.setRequestMethod("GET");
         if (bearerToken != null && !bearerToken.isBlank()) {
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
         }

         init(connection, headers);
         connection.setRequestProperty("Accept", "application/json");
         return init(connection);
      } catch (Exception e) {
         return null;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public static AutismHttp.Inner_DataUtil init_AutismHttp_DataUti(String url, String bearerToken) {
      return init_AutismHttp_DataUti(url, bearerToken, Map.of());
   }

   public static AutismHttp.Inner_DataUtil init_AutismHttp_DataUti(String url, String bearerToken, Map<String, String> headers) {
      HttpURLConnection connection = null;
      try {
         connection = init(url);
         connection.setRequestMethod("GET");
         if (bearerToken != null && !bearerToken.isBlank()) {
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
         }

         init(connection, headers);
         connection.setRequestProperty("Accept", "application/json");
         int code = connection.getResponseCode();
         return new AutismHttp.Inner_DataUtil(code, init_JsonObject(connection, code));
      } catch (Exception e) {
         return new AutismHttp.Inner_DataUtil(-1, null);
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public static JsonObject read_JsonObject(String url, String body) {
      HttpURLConnection connection = null;
      try {
         connection = init(url);
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setDoOutput(true);
         init(connection, body);
         return init(connection);
      } catch (Exception e) {
         return null;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public static AutismHttp.Inner_DataUtil load_AutismHttp_DataUti(String url, String body) {
      return init(url, body, 15000, Map.of());
   }

   public static AutismHttp.Inner_DataUtil init(String url, String body, int timeoutMs) {
      return init(url, body, timeoutMs, Map.of());
   }

   public static AutismHttp.Inner_DataUtil load(String url, String body, Map<String, String> headers) {
      return init(url, body, 15000, headers);
   }

   public static AutismHttp.Inner_DataUtil init(String url, String body, int timeoutMs, Map<String, String> headers) {
      HttpURLConnection connection = null;
      try {
         connection = init(url, timeoutMs);
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setRequestProperty("Accept", "application/json");
         init(connection, headers);
         connection.setDoOutput(true);
         init(connection, body);
         int code = connection.getResponseCode();
         return new AutismHttp.Inner_DataUtil(code, init_JsonObject(connection, code));
      } catch (Exception e) {
         return new AutismHttp.Inner_DataUtil(-1, null);
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   private static byte[] auditReadBounded(InputStream input) throws IOException {
      int maxBytes = 4 * 1024 * 1024;
      byte[] chunk = input.readNBytes(maxBytes + 1);
      if (chunk.length > maxBytes) {
         throw new IOException("HTTP response exceeds 4 MiB limit");
      }
      return chunk;
   }

   private static JsonObject init_JsonObject(HttpURLConnection connection, int code) {
      try (InputStream input = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream()) {
         if (input == null) {
            return null;
         }

         String text = new String(auditReadBounded(input), StandardCharsets.UTF_8);
         return text.isEmpty() ? null : JsonParser.parseString(text).getAsJsonObject();
      } catch (Exception e) {
         return null;
      }
   }

   public static JsonObject postForm(String url, String body) {
      return postForm(url, body, Map.of());
   }

   public static JsonObject postForm(String url, String body, Map<String, String> headers) {
      HttpURLConnection connection = null;
      try {
         connection = init(url);
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         init(connection, headers);
         connection.setDoOutput(true);
         init(connection, body);
         return init(connection);
      } catch (Exception e) {
         return null;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public static AutismHttp.Inner_DataUtil read_AutismHttp_DataUti(String url, String body) {
      return read(url, body, Map.of());
   }

   public static AutismHttp.Inner_DataUtil read(String url, String body, Map<String, String> headers) {
      HttpURLConnection connection = null;
      try {
         connection = init(url);
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         connection.setRequestProperty("Accept", "application/json");
         init(connection, headers);
         connection.setDoOutput(true);
         init(connection, body);
         int code = connection.getResponseCode();
         return new AutismHttp.Inner_DataUtil(code, init_JsonObject(connection, code));
      } catch (Exception e) {
         return new AutismHttp.Inner_DataUtil(-1, null);
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   private static HttpURLConnection init(String url) throws IOException {
      return init(url, 15000);
   }

   private static HttpURLConnection init(String url, int timeoutMs) throws IOException {
      Minecraft mc = Minecraft.getInstance();
      java.net.Proxy proxy = mc == null ? java.net.Proxy.NO_PROXY : mc.getProxy();
      HttpURLConnection connection = (HttpURLConnection)URI.create(url).toURL().openConnection(proxy);
      init_V(connection, timeoutMs);
      return connection;
   }

   private static HttpURLConnection load(String url, int timeoutMs) throws IOException {
      HttpURLConnection connection = (HttpURLConnection)URI.create(url).toURL().openConnection(MapStore.init_Proxy());
      init_V(connection, timeoutMs);
      return connection;
   }

   private static void init_V(HttpURLConnection connection, int timeoutMs) {
      connection.setConnectTimeout(timeoutMs);
      connection.setReadTimeout(timeoutMs);
      connection.setRequestProperty(
         "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36"
      );
   }

   private static void init(HttpURLConnection connection, String body) throws IOException {
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));

      try (OutputStream output = connection.getOutputStream()) {
         output.write(bytes);
      }
   }

   private static void init(HttpURLConnection connection, Map<String, String> headers) {
      if (headers != null) {
         for (Entry<String, String> e : headers.entrySet()) {
            if (e.getKey() != null && e.getValue() != null) {
               connection.setRequestProperty(e.getKey(), e.getValue());
            }
         }
      }
   }

   private static JsonObject init(HttpURLConnection connection) throws IOException {
      int code = connection.getResponseCode();
      if (code >= 200 && code < 300) {
         try (InputStream input = connection.getInputStream()) {
            String text = new String(auditReadBounded(input), StandardCharsets.UTF_8);
            return JsonParser.parseString(text).getAsJsonObject();
         }
      } else {
         try (InputStream err = connection.getErrorStream()) {
            if (err != null) {
               err.readNBytes(4096);
            }
         } catch (Exception ignored) {
         }
         return null;
      }
   }

   public record Inner_DataUtil(int init_I, JsonObject init_JsonObject) {
      public boolean init_Z() {
         return this.init_I >= 200 && this.init_I < 300;
      }

      public String init_Str() {
         try {
            return this.init_JsonObject != null && this.init_JsonObject.has("error") ? this.init_JsonObject.get("error").getAsString() : "";
         } catch (Exception e) {
            return "";
         }
      }
   }
}
