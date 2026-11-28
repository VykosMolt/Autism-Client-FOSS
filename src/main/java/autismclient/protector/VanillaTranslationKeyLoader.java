package autismclient.protector;

import autismclient.AutismClientAddon;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;

public final class VanillaTranslationKeyLoader {
   private static final boolean DEBUG = Boolean.getBoolean("autism.protector.debug");
   private static final Object LOCK = new Object();
   private static volatile Set<String> vanillaKeys;

   private VanillaTranslationKeyLoader() {
   }

   public static boolean isVanillaKey(String key) {
      return key != null && !key.isEmpty() ? vanillaKeySet().contains(key) : false;
   }

   public static boolean isLoaded() {
      return vanillaKeys != null;
   }

   private static Set<String> vanillaKeySet() {
      Set<String> current = vanillaKeys;
      if (current != null) {
         return current;
      }

      synchronized (LOCK) {
         if (vanillaKeys == null) {
            vanillaKeys = loadKeys();
         }

         return vanillaKeys;
      }
   }

   private static Set<String> loadKeys() {
      try (InputStream in = Minecraft.class.getResourceAsStream("/assets/minecraft/lang/en_us.json")) {
         if (in == null) {
            AutismClientAddon.LOG.warn("[AutismProtector] vanilla en_us.json not on classpath; falling back to blocklist only.");
            return Collections.emptySet();
         }

         JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
         Set<String> out = new HashSet<>(root.size() * 2);

         for (Entry<String, JsonElement> entry : root.entrySet()) {
            JsonElement value = entry.getValue();
            if (value != null && value.isJsonPrimitive()) {
               out.add(entry.getKey());
            }
         }

         if (DEBUG) {
            AutismClientAddon.LOG.debug("[AutismProtector] Loaded {} vanilla translation keys.", out.size());
         }

         return Collections.unmodifiableSet(out);
      } catch (Exception e) {
         AutismClientAddon.LOG.warn("[AutismProtector] Failed to load vanilla en_us.json: {}", e.getMessage());
         return Collections.emptySet();
      }
   }

   public static void loadAsync() {
      Thread t = new Thread(VanillaTranslationKeyLoader::vanillaKeySet, "AutismProtector-Vanilla-Keys");
      t.setDaemon(true);
      t.start();
   }
}
