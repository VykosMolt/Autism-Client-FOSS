package autismclient.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ae {
   private static final List<ae> init_List = new ArrayList<>();
   public static final ae init_ae = init_ae("Combat");
   public static final ae load_ae = init_ae("Movement");
   public static final ae read = init_ae("Player");
   public static final ae write = init_ae("Misc");
   public static final ae update = init_ae("Render");
   public static final ae sync = init_ae("Dupes");
   private final String init_Str;
   private final String load_Str;
   private final boolean init_Z;

   private ae(String key, String label, boolean addon) {
      this.init_Str = key;
      this.load_Str = label;
      this.init_Z = addon;
   }

   public static ae init_ae(String label) {
      return init(init_Str(label), label, false);
   }

   public static ae init(String addonId, String label) {
      String ns = addonId != null && !addonId.isBlank() ? addonId : "addon";
      String safeLabel = label != null && !label.isBlank() ? label : ns;
      String key = init_Str("ADDON " + ns);
      return init(key, safeLabel, true);
   }

   private static synchronized ae init(String key, String label, boolean addon) {
      for (ae existing : init_List) {
         if (existing.init_Str.equals(key)) {
            return existing;
         }
      }

      ae category = new ae(key, label, addon);
      init_List.add(category);
      return category;
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   private static String init_Str(String label) {
      return label == null ? "" : label.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");
   }

   public static List<ae> init_List() {
      return Collections.unmodifiableList(new ArrayList<>(init_List));
   }

   public String init_Str() {
      return this.init_Str;
   }

   public String load() {
      return this.load_Str;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof ae that ? Objects.equals(this.init_Str, that.init_Str) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.init_Str);
   }

   @Override
   public String toString() {
      return this.load_Str;
   }
}
