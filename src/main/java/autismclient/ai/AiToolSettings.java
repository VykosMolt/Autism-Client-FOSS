package autismclient.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

public final class AiToolSettings {
   public static final Set<String> STRATEGIES = Set.of("fastest", "safest", "saver");
   public static final int MIN_TIMEOUT_SECONDS = 30;
   public static final int MAX_TIMEOUT_SECONDS = 1800;
   public static final int MIN_DIG_RELUCTANCE = 0;
   public static final int MAX_DIG_RELUCTANCE = 60;
   public int version = 1;
   public String strategy = "fastest";
   public boolean allowDig = true;
   public boolean allowPillar = true;
   public boolean allowBridge = true;
   public boolean allowInventory;
   public int digReluctance = 2;
   public boolean useShield;
   public int timeoutSeconds = 300;
   public String verbosity = "normal";

   public AiToolSettings.Verbosity verbosityLevel() {
      return AiToolSettings.Verbosity.parse(this.verbosity);
   }

   public void clamp() {
      if (!STRATEGIES.contains(this.strategy)) {
         this.strategy = "fastest";
      }

      this.timeoutSeconds = Math.max(30, Math.min(1800, this.timeoutSeconds));
      this.digReluctance = Math.max(0, Math.min(60, this.digReluctance));
      this.verbosity = AiToolSettings.Verbosity.parse(this.verbosity).id();
   }

   public synchronized AiToolSettings copy() {
      return AiJson.GSON.fromJson(AiJson.GSON.toJson(this), AiToolSettings.class);
   }

   public synchronized void save(Path directory) throws IOException {
      this.clamp();
      AiJson.writeAtomicJson(directory.resolve("tools-settings.json"), this);
   }

   public static AiToolSettings loadFrom(Path directory) {
      Path path = directory.resolve("tools-settings.json");
      if (!Files.exists(path)) {
         return new AiToolSettings();
      }

      try {
         AiToolSettings s = AiJson.GSON.fromJson(Files.readString(path), AiToolSettings.class);
         if (s != null && s.version == 1) {
            s.clamp();
            return s;
         } else {
            return new AiToolSettings();
         }
      } catch (Exception e) {
         return new AiToolSettings();
      }
   }

   public enum Verbosity {
      QUIET,
      NORMAL,
      VERBOSE;

      public boolean reportsProgress() {
         return this != QUIET;
      }

      public long progressIntervalMs() {
         return this == VERBOSE ? 2000L : 5000L;
      }

      public boolean includesData() {
         return this == VERBOSE;
      }

      public boolean includesFailureSuggestions() {
         return this == VERBOSE;
      }

      public String id() {
         return this.name().toLowerCase(Locale.ROOT);
      }

      public static AiToolSettings.Verbosity parse(String value) {
         if (value != null) {
            for (AiToolSettings.Verbosity v : values()) {
               if (v.id().equalsIgnoreCase(value.trim())) {
                  return v;
               }
            }
         }

         return NORMAL;
      }
   }
}
