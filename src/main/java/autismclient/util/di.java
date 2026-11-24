package autismclient.util;

import autismclient.api.module.Kind;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class di {
   private di() {
   }

   public static boolean init(String registryId, String entry) {
      if (registryId != null && entry != null) {
         String normalized = entry.trim().toLowerCase(Locale.ROOT);
         return registryId.equalsIgnoreCase(normalized) || registryId.toLowerCase(Locale.ROOT).endsWith(":" + normalized);
      } else {
         return false;
      }
   }

   public static List<String> init(Kind type, List<String> values) {
      List<String> invalid = new ArrayList<>();
      if (values == null) {
         return invalid;
      }

      for (String value : values) {
         if (!value.isBlank() && !init(type, value)) {
            invalid.add(value);
         }
      }

      return invalid;
   }

   public static boolean init(Kind type, String id) {
      String normalized = init(id);
      if (type == Kind.STORAGE_LIST) {
         return autismclient.modules.ap.init_ap_BufferHelper(normalized) != null;
      }

      Identifier parsed = Identifier.tryParse(normalized);
      if (parsed == null) {
         return false;
      }

      boolean var10000;
      switch (type) {
         case ITEM_LIST:
            var10000 = BuiltInRegistries.ITEM.getOptional(parsed).isPresent();
            break;
         case BLOCK_LIST:
            var10000 = BuiltInRegistries.BLOCK.getOptional(parsed).isPresent();
            break;
         case ENTITY_TYPE_LIST:
            var10000 = BuiltInRegistries.ENTITY_TYPE.getOptional(parsed).isPresent();
            break;
         case SOUND_EVENT_LIST:
            var10000 = BuiltInRegistries.SOUND_EVENT.getOptional(parsed).isPresent();
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   public static String init(String id) {
      if (id == null) {
         return "";
      }

      String trimmed = id.trim().toLowerCase(Locale.ROOT);
      return trimmed.contains(":") ? trimmed : "minecraft:" + trimmed;
   }
}
