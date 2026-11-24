package autismclient.g;

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

public final class PathHelper {
   private static final boolean init_Z = Boolean.getBoolean("autism.protector.debug");
   private static final Object init_Obj = new Object();
   private static volatile Set<String> init_Set;

   private PathHelper() {
   }

   public static boolean init(String key) {
      return key != null && !key.isEmpty() ? init_Set().contains(key) : false;
   }

   public static boolean init_Z() {
      return init_Set != null;
   }

   private static Set<String> init_Set() {
      Set<String> current = init_Set;
      if (current != null) {
         return current;
      }

      synchronized (init_Obj) {
         if (init_Set == null) {
            init_Set = load();
         }

         return init_Set;
      }
   }

   private static Set<String> load() {
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

         if (init_Z) {
            AutismClientAddon.LOG.debug("[AutismProtector] Loaded {} vanilla translation keys.", out.size());
         }

         return Collections.unmodifiableSet(out);
      } catch (Exception e) {
         AutismClientAddon.LOG.warn("[AutismProtector] Failed to load vanilla en_us.json: {}", e.getMessage());
         return Collections.emptySet();
      }
   }

   public static void init_V() {
      Thread t = new Thread(PathHelper::init_Set, "AutismProtector-Vanilla-Keys");
      t.setDaemon(true);
      t.start();
   }
}
