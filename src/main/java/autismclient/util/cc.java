package autismclient.util;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class cc {
   private static final Map<String, String> init = new ConcurrentHashMap<>();
   private static final Map<String, String> load = new ConcurrentHashMap<>();
   private static final Map<String, String> read = new ConcurrentHashMap<>();
   private static final Map<String, String> write = new ConcurrentHashMap<>();

   private cc() {
   }

   public static String init(String rawId) {
      String key = poll(rawId);
      return init.computeIfAbsent(key, id -> init(id, BuiltInRegistries.ITEM, item -> item.getName(item.getDefaultInstance()).getString()));
   }

   public static String load(String rawId) {
      String key = poll(rawId);
      return load.computeIfAbsent(key, id -> init(id, BuiltInRegistries.BLOCK, block -> block.getName().getString()));
   }

   public static String read(String rawId) {
      String key = poll(rawId);
      return read.computeIfAbsent(key, id -> init(id, BuiltInRegistries.ENTITY_TYPE, entity -> entity.getDescription().getString()));
   }

   public static String write(String rawId) {
      return update(rawId);
   }

   public static String update(String rawId) {
      String key = poll(rawId);
      return write.computeIfAbsent(key, cc::flush);
   }

   public static String sync(String rawId) {
      String key = poll(rawId);
      if (key.isEmpty()) {
         return "";
      }

      Identifier parsed = Identifier.tryParse(key);
      return parsed == null ? key : parsed.getPath();
   }

   public static boolean init(String text, String rawId) {
      String trimmed = text == null ? "" : text.trim();
      String key = poll(rawId);
      return trimmed.isEmpty() ? true : trimmed.equalsIgnoreCase(key) || trimmed.equalsIgnoreCase(sync(key)) || trimmed.equalsIgnoreCase(update(key));
   }

   private static <T> String init(String rawId, Registry<T> registry, Function<T, String> nameProvider) {
      if (rawId.isEmpty()) {
         return "";
      }

      Identifier parsed = Identifier.tryParse(rawId);
      String fallback = flush(rawId);

      try {
         if (parsed != null && registry.containsKey(parsed)) {
            T value = registry.getValue(parsed);
            if (value == null) {
               return fallback;
            }

            String resolved = nameProvider.apply(value);
            return resolved != null && !resolved.isBlank() ? resolved : fallback;
         } else {
            return fallback;
         }
      } catch (Throwable t) {
         return fallback;
      }
   }

   private static String flush(String rawId) {
      if (rawId.isEmpty()) {
         return "";
      }

      Identifier parsed = Identifier.tryParse(rawId);
      if (parsed == null) {
         return close(rawId);
      }

      String pathLabel = close(parsed.getPath());
      if ("minecraft".equals(parsed.getNamespace())) {
         return pathLabel;
      }

      String namespaceLabel = close(parsed.getNamespace());
      return namespaceLabel.isEmpty() ? pathLabel : namespaceLabel + ": " + pathLabel;
   }

   private static String close(String raw) {
      if (raw != null && !raw.isBlank()) {
         String cleaned = raw.trim().replaceAll("[:_/.-]+", " ").replaceAll("\\s+", " ");
         if (cleaned.isEmpty()) {
            return raw.trim();
         }

         StringBuilder out = new StringBuilder(cleaned.length());

         for (String part : cleaned.split(" ")) {
            if (!part.isEmpty()) {
               if (out.length() > 0) {
                  out.append(' ');
               }

               out.append(open(part));
            }
         }

         return out.toString();
      } else {
         return "";
      }
   }

   private static String open(String token) {
      if (token.isEmpty()) {
         return token;
      } else {
         return token.length() <= 3 && token.equals(token.toUpperCase(Locale.ROOT))
            ? token
            : Character.toUpperCase(token.charAt(0)) + token.substring(1).toLowerCase(Locale.ROOT);
      }
   }

   private static String poll(String rawId) {
      return rawId == null ? "" : rawId.trim();
   }
}
