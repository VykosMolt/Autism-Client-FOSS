package autismclient.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AutismProfile {
   public String id = "";
   public String displayName = "";
   public boolean autoSave = false;
   public boolean ownMacroLibrary = false;
   public boolean ownThemeColor = true;
   public List<String> serverPatterns = new ArrayList<>();
   public long createdAt = 0L;
   public long updatedAt = 0L;
   public int schemaVersion = 1;
   public AutismConfig snapshot = new AutismConfig();

   public AutismProfile() {
   }

   public AutismProfile(String id, String displayName) {
      this.id = id;
      this.displayName = displayName;
   }

   public static String sanitizeId(String raw) {
      String base = (raw == null ? "" : raw).toLowerCase(Locale.ROOT).strip().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+|-+$)", "");
      return base.isBlank() ? "profile" : base;
   }

   public void normalize() {
      this.id = sanitizeId(this.id);
      if (this.displayName == null || this.displayName.isBlank()) {
         this.displayName = this.id;
      }

      if (this.serverPatterns == null) {
         this.serverPatterns = new ArrayList<>();
      }

      if (this.snapshot == null) {
         this.snapshot = new AutismConfig();
      }

      this.snapshot.applyRuntimeDefaults();
      if (this.schemaVersion <= 0) {
         this.schemaVersion = 1;
      }
   }
}
