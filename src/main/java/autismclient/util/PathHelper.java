package autismclient.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.services.FriendsService;
import com.mojang.authlib.services.MinecraftServicesDiscoveryService;
import com.mojang.authlib.services.FriendsService.ResultCode;
import com.mojang.authlib.services.response.FriendData;
import com.mojang.authlib.services.response.PresenceResponse;
import com.mojang.util.UndashedUuid;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class PathHelper {
   private static final URI init_URI = URI.create("http://authserver.thealtening.com/authenticate");
   private static final int init_I = 1048576;
   private static final String init_Str = "AUTISM Client";
   public static final FriendsService init_FriendsService = new FriendsService() {
      private ResultCode init() {
         return ResultCode.SERVICE_NOT_AVAILABLE;
      }

      @Override
      public ResultCode getFriendData(Consumer<FriendData> consumer) {
         consumer.accept(FriendData.empty());
         return this.init();
      }

      @Override
      public ResultCode removeFriend(UUID id) {
         return this.init();
      }

      @Override
      public ResultCode acceptIncomingFriendRequest(UUID id) {
         return this.init();
      }

      @Override
      public ResultCode declineIncomingFriendRequest(UUID id) {
         return this.init();
      }

      @Override
      public ResultCode sendFriendRequest(String name) {
         return this.init();
      }

      @Override
      public ResultCode sendFriendRequest(UUID id) {
         return this.init();
      }

      @Override
      public ResultCode revokeOutgoingFriendRequest(UUID id) {
         return this.init();
      }

      @Override
      public ResultCode updateFriendSettings(boolean requests, boolean presence) {
         return this.init();
      }

      @Override
      public PresenceResponse presence(String activity) {
         return PresenceResponse.empty();
      }

      @Override
      public Optional<Duration> getFriendsPollInterval() {
         return Optional.empty();
      }

      @Override
      public Optional<Duration> getPresencePollInterval() {
         return Optional.empty();
      }
   };

   private PathHelper() {
   }

   public static PathHelper.Inner_DataUtil init(String accountToken) throws IOException {
      return init(init_URI, accountToken);
   }

   static PathHelper.Inner_DataUtil init(URI endpoint, String accountToken) throws IOException, MalformedURLException, ProtocolException {
      String credential = accountToken == null ? "" : accountToken.strip();
      if (credential.isEmpty()) {
         throw new IOException("Missing TheAltening account token");
      }

      String clientToken = UUID.randomUUID().toString();
      JsonObject agent = new JsonObject();
      agent.addProperty("name", "Minecraft");
      agent.addProperty("version", 1);
      JsonObject request = new JsonObject();
      request.add("agent", agent);
      request.addProperty("username", credential);
      request.addProperty("password", "AUTISM Client");
      request.addProperty("clientToken", clientToken);
      request.addProperty("requestUser", true);
      HttpURLConnection connection = (HttpURLConnection)endpoint.toURL().openConnection(MapStore.init_Proxy());

      try {
         connection.setConnectTimeout(15000);
         connection.setReadTimeout(15000);
         connection.setInstanceFollowRedirects(false);
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
         connection.setRequestProperty("Accept", "application/json");
         connection.setRequestProperty("User-Agent", "AutismClient");
         connection.setDoOutput(true);
         byte[] body = request.toString().getBytes(StandardCharsets.UTF_8);
         connection.setFixedLengthStreamingMode(body.length);

         try (OutputStream output = connection.getOutputStream()) {
            output.write(body);
         }

         int status = connection.getResponseCode();
         if (status == 401 || status == 403) {
            throw new IOException("TheAltening rejected or expired this account token");
         }

         if (status == 429) {
            throw new IOException("TheAltening rate limit reached; try again shortly");
         }

         if (status >= 500) {
            throw new IOException("TheAltening service is unavailable (HTTP " + status + ")");
         }

         if (status >= 200 && status < 300) {
            byte[] bytes;
            try (InputStream input = connection.getInputStream()) {
               bytes = input.readNBytes(1048577);
            }

            if (bytes.length > 1048576) {
               throw new IOException("TheAltening login response is too large");
            } else {
               return init(new String(bytes, StandardCharsets.UTF_8), clientToken);
            }
         } else {
            throw new IOException("TheAltening login failed (HTTP " + status + ")");
         }
      } finally {
         connection.disconnect();
      }
   }

   static PathHelper.Inner_DataUtil init(String json, String clientToken) throws IOException {
      try {
         JsonElement parsed = JsonParser.parseString(json);
         if (!parsed.isJsonObject()) {
            throw new IOException("TheAltening returned an invalid login response");
         }

         JsonObject response = parsed.getAsJsonObject();
         if (!clientToken.equals(init(response, "clientToken"))) {
            throw new IOException("TheAltening login response has a different client identifier");
         }

         String accessToken = init(response, "accessToken");
         if (accessToken.isBlank()) {
            throw new IOException("TheAltening returned no session token");
         }

         JsonElement selected = response.get("selectedProfile");
         if (selected != null && selected.isJsonObject()) {
            JsonObject profile = selected.getAsJsonObject();
            String name = init(profile, "name");
            String id = init(profile, "id");
            if (!name.isBlank() && !id.isBlank()) {
               UUID uuid;
               try {
                  if (!id.matches("(?i)[0-9a-f]{32}|[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                     throw new IllegalArgumentException();
                  }

                  uuid = UndashedUuid.fromStringLenient(id);
               } catch (IllegalArgumentException error) {
                  throw new IOException("TheAltening returned an invalid Minecraft profile ID");
               }

               return new PathHelper.Inner_DataUtil(new GameProfile(uuid, name), accessToken);
            } else {
               throw new IOException("TheAltening returned an incomplete Minecraft profile");
            }
         } else {
            throw new IOException("TheAltening returned no selected Minecraft profile");
         }
      } catch (RuntimeException error) {
         throw new IOException("TheAltening returned an invalid login response");
      }
   }

   private static String init(JsonObject json, String key) {
      JsonElement field = json.get(key);
      return field != null && field.isJsonPrimitive() && field.getAsJsonPrimitive().isString() ? field.getAsString() : "";
   }

   public static MinecraftServicesDiscoveryService init_YggdrasilAuthentic() {
      return MinecraftServicesDiscoveryService.createOffline(MapStore.init_Proxy());
   }

   public record Inner_DataUtil(GameProfile init_GameProfile, String init_Str) {
      @Override
      public String toString() {
         return "TheAltening session for " + this.init_GameProfile.name();
      }
   }
}
