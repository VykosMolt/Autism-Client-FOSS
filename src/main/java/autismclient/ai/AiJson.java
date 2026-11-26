package autismclient.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class AiJson {
   public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

   private AiJson() {
   }

   public static JsonObject objectOf(Object... pairs) {
      JsonObject o = new JsonObject();

      for (int i = 0; i < pairs.length; i += 2) {
         o.add(pairs[i].toString(), GSON.toJsonTree(pairs[i + 1]));
      }

      return o;
   }

   public static String getString(JsonObject o, String key, String fallback) {
      return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : fallback;
   }

   private static boolean hasNumber(JsonObject o, String key) {
      if (!o.has(key)) {
         return false;
      }

      JsonElement value = o.get(key);
      return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber();
   }

   public static int getInt(JsonObject o, String key, int fallback) {
      return !hasNumber(o, key) ? fallback : o.get(key).getAsBigDecimal().setScale(0, RoundingMode.FLOOR).intValueExact();
   }

   public static double getDouble(JsonObject o, String key, double fallback) {
      if (!hasNumber(o, key)) {
         return fallback;
      } else {
         double v = o.get(key).getAsDouble();
         if (!Double.isFinite(v)) {
            throw new IllegalArgumentException("Non-finite " + key);
         } else {
            return v;
         }
      }
   }

   public static boolean getBoolean(JsonObject o, String key, boolean fallback) {
      if (!o.has(key)) {
         return fallback;
      }

      JsonElement value = o.get(key);
      if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
         return value.getAsBoolean();
      }

      if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
         int n = value.getAsBigDecimal().intValueExact();
         if (n == 0 || n == 1) {
            return n == 1;
         }
      }

      throw new IllegalArgumentException("Expected boolean or NBT byte 0/1: " + key);
   }

   public static JsonObject parseObject(String json) {
      if (json.length() > 2000000) {
         throw new IllegalArgumentException("JSON too large");
      } else {
         return JsonParser.parseString(json).getAsJsonObject();
      }
   }

   public static String sha256Hex(String value) {
      try {
         return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
      } catch (Exception e) {
         throw new IllegalStateException(e);
      }
   }

   public static void writeAtomicJson(Path path, Object value) throws IOException {
      Files.createDirectories(path.toAbsolutePath().getParent());
      Path temp = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");

      try {
         Files.writeString(temp, GSON.toJson(value), StandardCharsets.UTF_8);

         try {
            Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
         } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
         }
      } finally {
         Files.deleteIfExists(temp);
      }
   }

   // What the host keeps in the task state stays the host's: a state written by the model neither drops nor replaces it.
   static JsonObject auditKeepHostKeys(JsonObject current, JsonObject next) {
      for (String key : new String[]{"hostChatMode", "hostGetTargets", "builtInAction", "activeOperation"}) {
         next.remove(key);
         if (current.has(key)) {
            next.add(key, current.get(key).deepCopy());
         }
      }

      return next;
   }

   // Append-only AI files start owner-only, like the temp-file writes above.
   static void auditPrivateFile(Path file) throws IOException {
      if (Files.notExists(file)) {
         try {
            Files.createFile(file, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
         } catch (FileAlreadyExistsException | UnsupportedOperationException e) {
         }
      }
   }
}
