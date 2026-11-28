package autismclient.util;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class RegistryDisplayNameCache {
   private static final Map<String, String> itemNames = new ConcurrentHashMap<>();
   private static final Map<String, String> blockNames = new ConcurrentHashMap<>();
   private static final Map<String, String> entityNames = new ConcurrentHashMap<>();
   private static final Map<String, String> genericNames = new ConcurrentHashMap<>();

   private RegistryDisplayNameCache() {
   }

   public static String itemName(String rawId) {
      String key = normalizeKey(rawId);
      return itemNames.computeIfAbsent(key, id -> lookupName(id, BuiltInRegistries.ITEM, item -> item.getName(item.getDefaultInstance()).getString()));
   }

   public static String blockName(String rawId) {
      String key = normalizeKey(rawId);
      return blockNames.computeIfAbsent(key, id -> lookupName(id, BuiltInRegistries.BLOCK, block -> block.getName().getString()));
   }

   public static String entityName(String rawId) {
      String key = normalizeKey(rawId);
      return entityNames.computeIfAbsent(key, id -> lookupName(id, BuiltInRegistries.ENTITY_TYPE, entity -> entity.getDescription().getString()));
   }

   public static String displayName(String rawId) {
      return genericName(rawId);
   }

   public static String genericName(String rawId) {
      String key = normalizeKey(rawId);
      return genericNames.computeIfAbsent(key, RegistryDisplayNameCache::fallbackName);
   }

   public static String shortName(String rawId) {
      String key = normalizeKey(rawId);
      if (key.isEmpty()) {
         return "";
      }

      Identifier parsed = Identifier.tryParse(key);
      return parsed == null ? key : parsed.getPath();
   }

   public static boolean isDefaultName(String text, String rawId) {
      String trimmed = text == null ? "" : text.trim();
      String key = normalizeKey(rawId);
      return trimmed.isEmpty() ? true : trimmed.equalsIgnoreCase(key) || trimmed.equalsIgnoreCase(shortName(key)) || trimmed.equalsIgnoreCase(genericName(key));
   }

   private static <T> String lookupName(String rawId, Registry<T> registry, Function<T, String> nameProvider) {
      if (rawId.isEmpty()) {
         return "";
      }

      Identifier parsed = Identifier.tryParse(rawId);
      String fallback = fallbackName(rawId);

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

   private static String fallbackName(String rawId) {
      if (rawId.isEmpty()) {
         return "";
      }

      Identifier parsed = Identifier.tryParse(rawId);
      if (parsed == null) {
         return humanize(rawId);
      }

      String pathLabel = humanize(parsed.getPath());
      if ("minecraft".equals(parsed.getNamespace())) {
         return pathLabel;
      }

      String namespaceLabel = humanize(parsed.getNamespace());
      return namespaceLabel.isEmpty() ? pathLabel : namespaceLabel + ": " + pathLabel;
   }

   private static String humanize(String raw) {
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

               out.append(capitalize(part));
            }
         }

         return out.toString();
      } else {
         return "";
      }
   }

   private static String capitalize(String token) {
      if (token.isEmpty()) {
         return token;
      } else {
         return token.length() <= 3 && token.equals(token.toUpperCase(Locale.ROOT))
            ? token
            : Character.toUpperCase(token.charAt(0)) + token.substring(1).toLowerCase(Locale.ROOT);
      }
   }

   private static String normalizeKey(String rawId) {
      return rawId == null ? "" : rawId.trim();
   }
}
