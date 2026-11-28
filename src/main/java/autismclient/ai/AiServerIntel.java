package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.modules.ModuleRuntimeManager;
import autismclient.util.AutismConfig;
import autismclient.util.AutismServerInfoOverlay;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public final class AiServerIntel {
   static final long FRESH_SCAN_MS = 5184000000L;
   static final int init_I = 40;
   static final int load = 120;
   static final int read = 24;
   static final int write = 36000;
   static final int update = 24;
   static final int sync = 40;
   static final int flush = 10;
   static final int close = 280;
   private static final int open = 5;

   private AiServerIntel() {
   }

   static String notConnectedError(boolean connectedToServer) {
      return connectedToServer ? null : "Not connected to a multiplayer server; join one first (singleplayer has no server intelligence)";
   }

   static List<AiServerIntel.PluginReport> buildReports(
      List<String> plugins,
      Map<String, List<String>> commands,
      Map<String, String> evidence,
      Map<String, List<String>> channels,
      Map<String, List<String>> guis,
      Map<String, String> confidence,
      int maxCommands
   ) {
      List<AiServerIntel.PluginReport> out = new ArrayList<>();
      if (plugins == null) {
         return out;
      }

      for (String name : plugins) {
         if (name != null && !name.isBlank()) {
            String display = name.trim();
            out.add(
               new AiServerIntel.PluginReport(
                  display,
                  autismclient.util.PluginSignatureCatalog.canonicalName(display),
                  normalizeTag(valueFor(confidence, display)),
                  normalizeTag(valueFor(evidence, display)),
                  capped(listFor(commands, display), maxCommands),
                  capped(listFor(channels, display), 24),
                  capped(listFor(guis, display), 24)
               )
            );
         }
      }

      out.sort(Comparator.comparing(AiServerIntel.PluginReport::displayName, String.CASE_INSENSITIVE_ORDER));
      return out;
   }

   static boolean isWeakEvidence(String evidence) {
      boolean var10000;
      switch (normalizeTag(evidence)) {
         case "USER_AUTOCOMPLETE":
         case "SCANNER_AUTOCOMPLETE":
         case "FEATURE":
         case "HELP_HINT":
         case "UNKNOWN":
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   static boolean isConfident(String confidence) {
      boolean var10000;
      switch (normalizeTag(confidence)) {
         case "EXACT":
         case "STRONG":
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   static List<AiServerIntel.PluginReport> unusedRadarPlugins(List<AiServerIntel.PluginReport> plugins) {
      List<AiServerIntel.PluginReport> out = new ArrayList<>();
      if (plugins != null) {
         for (AiServerIntel.PluginReport plugin : plugins) {
            if (plugin != null && !plugin.displayName().isBlank() && isConfident(plugin.confidence()) && !isWeakEvidence(plugin.evidence())) {
               out.add(plugin);
            }
         }
      }

      out.sort(Comparator.comparing(AiServerIntel.PluginReport::displayName, String.CASE_INSENSITIVE_ORDER));
      return out;
   }

   static String unusedConfidenceLabel(String stored) {
      String var10000;
      switch (stored == null ? "" : stored.trim().toUpperCase(Locale.ROOT)) {
         case "EXACT":
            var10000 = "Exact";
            break;
         case "STRONG":
            var10000 = "Strong";
            break;
         case "FEATURE":
            var10000 = "Feature";
            break;
         default:
            var10000 = "Unknown";
      }

      return var10000;
   }

   static JsonObject boundedPluginsJson(List<AiServerIntel.PluginReport> plugins, int charBudget) {
      JsonArray entries = new JsonArray();
      int included = 0;
      if (plugins != null) {
         for (AiServerIntel.PluginReport plugin : plugins) {
            JsonObject entry = reportJson(plugin);
            if (entries.toString().length() + entry.toString().length() > charBudget) {
               break;
            }

            entries.add(entry);
            included++;
         }
      }

      int total = plugins == null ? 0 : plugins.size();
      JsonObject out = new JsonObject();
      out.add("entries", entries);
      out.addProperty("includedEntries", included);
      out.addProperty("totalEntries", total);
      if (included < total) {
         out.addProperty("truncated", true);
      }

      return out;
   }

   private static JsonObject reportJson(AiServerIntel.PluginReport plugin) {
      JsonObject entry = new JsonObject();
      entry.addProperty("name", plugin.displayName());
      if (!plugin.key().isBlank()) {
         entry.addProperty("key", plugin.key());
      }

      entry.addProperty("confidence", plugin.confidence());
      entry.addProperty("evidence", plugin.evidence());
      entry.addProperty("radarEligible", isConfident(plugin.confidence()) && !isWeakEvidence(plugin.evidence()));
      if (!plugin.commands().isEmpty()) {
         entry.add("commands", AiJson.GSON.toJsonTree(plugin.commands()));
      }

      if (!plugin.channels().isEmpty()) {
         entry.add("channels", AiJson.GSON.toJsonTree(plugin.channels()));
      }

      if (!plugin.guis().isEmpty()) {
         entry.add("guis", AiJson.GSON.toJsonTree(plugin.guis()));
      }

      return entry;
   }

   private static String normalizeTag(String value) {
      String clean = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
      return clean.isBlank() ? "UNKNOWN" : clean;
   }

   private static List<String> listFor(Map<String, List<String>> map, String plugin) {
      if (map != null && !map.isEmpty() && plugin != null && !plugin.isBlank()) {
         List<String> direct = map.get(plugin);
         if (direct != null) {
            return direct;
         }

         String key = autismclient.util.PluginSignatureCatalog.canonicalName(plugin);

         for (Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getKey() != null && autismclient.util.PluginSignatureCatalog.canonicalName(entry.getKey()).equals(key)) {
               return entry.getValue();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static String valueFor(Map<String, String> map, String plugin) {
      if (map != null && !map.isEmpty() && plugin != null && !plugin.isBlank()) {
         String direct = map.get(plugin);
         if (direct != null) {
            return direct;
         }

         String key = autismclient.util.PluginSignatureCatalog.canonicalName(plugin);

         for (Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey() != null && autismclient.util.PluginSignatureCatalog.canonicalName(entry.getKey()).equals(key)) {
               return entry.getValue();
            }
         }

         return "";
      } else {
         return "";
      }
   }

   private static List<String> capped(List<String> values, int maxItems) {
      List<String> out = new ArrayList<>();
      if (values == null) {
         return out;
      }

      for (String value : values) {
         if (out.size() >= maxItems) {
            break;
         }

         if (value != null && !value.isBlank()) {
            out.add(truncate(value.trim(), 120));
         }
      }

      return out;
   }

   private static String truncate(String value, int max) {
      if (value == null) {
         return "";
      } else {
         return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + "…";
      }
   }

   private static String nullToEmpty(String value) {
      return value == null ? "" : value;
   }

   public static AiOperation create(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      final String address = serverAddress(mc);
      String precondition = notConnectedError(!address.isBlank());
      if (precondition != null) {
         throw new IllegalArgumentException(precondition);
      }

      boolean force = AiJson.getBoolean(args, "force", false);
      final int timeoutSeconds = Math.max(1, Math.min(150, AiJson.getInt(args, "timeoutSeconds", 150)));
      final AutismServerInfoOverlay overlay = ModuleRuntimeManager.getInstance().getOrCreateServerInfoOverlay();
      if (force) {
         overlay.clearSearch();
      }

      final long startRevision = AutismConfig.getGlobal().pluginScanRevision();
      final String brand = serverBrand(mc);
      overlay.startAutoScan();
      return new AiOperation() {
         final long scanDeadline = System.nanoTime() + timeoutSeconds * 1000000000L;
         long lastRevision = startRevision;
         int stableTicks;
         int waitTicks;
         boolean sawScanning;
         boolean cancelled;

         @Override
         public AiOperation.Result tick() {
            if (this.cancelled) {
               return AiGameOperations.result(AiOperation.Status.CANCELLED, "Plugin scan cancelled", new JsonObject());
            }

            String now = AiServerIntel.serverAddress(Minecraft.getInstance());
            if (!now.equals(address)) {
               this.cancelled = true;
               return AiGameOperations.result(AiOperation.Status.CANCELLED, "Left the server during the plugin scan", new JsonObject());
            }

            if (autismclient.modules.aD.init_Z()) {
               return AiGameOperations.result(AiOperation.Status.FAILED, "Client is hidden (panic mode); plugin probing is suspended", new JsonObject());
            }

            AutismConfig config = AutismConfig.getGlobal();
            AutismConfig.PluginScanCacheEntry entry = config.getPluginScanByAddress(address);
            long revision = config.pluginScanRevision();
            if (revision != this.lastRevision) {
               this.lastRevision = revision;
               this.stableTicks = 0;
            } else {
               this.stableTicks++;
            }

            boolean scanning = overlay.isScanRunning();
            this.sawScanning |= scanning;
            this.waitTicks++;
            if (!scanning && this.stableTicks >= 5) {
               if (entry != null) {
                  return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Plugin scan results cached for " + address, this.resultData(entry, false));
               }

               if (this.sawScanning || revision != startRevision) {
                  return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Plugin scan completed with no detected plugins", this.resultData(null, false));
               }

               if (this.waitTicks % 20 == 0) {
                  overlay.startAutoScan();
               }
            }

            if (System.nanoTime() > this.scanDeadline) {
               this.cancelled = true;
               return AiGameOperations.result(
                  AiOperation.Status.TIMED_OUT, "Plugin scan did not finish in time; returning cached partial results", this.resultData(entry, true)
               );
            } else {
               JsonObject progress = new JsonObject();
               progress.addProperty("scanning", scanning);
               progress.addProperty("cachedEntry", entry != null);
               return AiGameOperations.result(AiOperation.Status.RUNNING, scanning ? "Probing server plugins" : "Waiting for plugin scan results", progress);
            }
         }

         private JsonObject resultData(AutismConfig.PluginScanCacheEntry entry, boolean partial) {
            return AiServerIntel.cacheEntryJson(address, entry, false, partial, brand);
         }

         @Override
         public void cancel() {
            this.cancelled = true;
         }
      };
   }

   public static JsonObject cachedScan(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      AutismConfig config = AutismConfig.getGlobal();
      String requested = AiJson.getString(args, "address", "").trim().toLowerCase(Locale.ROOT);
      String address = requested;
      AutismConfig.PluginScanCacheEntry entry = address.isBlank() ? null : config.getPluginScanByAddress(address);
      if (entry == null && requested.isBlank()) {
         address = serverAddress(mc);
         if (!address.isBlank()) {
            entry = config.getPluginScanByAddress(address);
         }

         if (entry == null && address.isBlank()) {
            long newest = -1L;

            for (Entry<String, AutismConfig.PluginScanCacheEntry> candidate : config.allPluginScans().entrySet()) {
               AutismConfig.PluginScanCacheEntry value = candidate.getValue();
               if (value != null && value.scannedAtMs > newest) {
                  newest = value.scannedAtMs;
                  entry = value;
                  address = !nullToEmpty(value.serverAddress).isBlank() ? value.serverAddress.trim().toLowerCase(Locale.ROOT) : candidate.getKey().split("\\|", 2)[0];
               }
            }
         }
      }

      if (entry == null) {
         JsonObject out = new JsonObject();
         out.addProperty("cached", true);
         out.addProperty("present", false);
         if (!address.isBlank()) {
            out.addProperty("serverAddress", address);
         }

         out.addProperty("hint", "No plugin scan cached; run client:plugin_scan while connected to the server");
         return out;
      } else {
         return cacheEntryJson(address, entry, true, false, serverBrand(mc));
      }
   }

   private static String serverAddress(Minecraft mc) {
      if (mc == null) {
         return "";
      }

      ServerData server = mc.getCurrentServer();
      return server != null && server.ip != null && !server.ip.isBlank() ? server.ip.trim().toLowerCase(Locale.ROOT) : "";
   }

   private static String serverBrand(Minecraft mc) {
      try {
         return mc != null && mc.getConnection() != null && mc.getConnection().serverBrand() != null ? mc.getConnection().serverBrand() : "";
      } catch (Throwable ignored) {
         return "";
      }
   }

   private static JsonObject cacheEntryJson(String address, AutismConfig.PluginScanCacheEntry entry, boolean cached, boolean partial, String brand) {
      JsonObject out = new JsonObject();
      out.addProperty("cached", cached);
      if (partial) {
         out.addProperty("partial", true);
      }

      if (!address.isBlank()) {
         out.addProperty("serverAddress", address);
      }

      if (entry == null) {
         out.addProperty("present", false);
         out.addProperty("scanStatus", "NONE");
         out.addProperty("pluginCount", 0);
         out.add("entries", new JsonArray());
         out.addProperty("includedEntries", 0);
         out.addProperty("totalEntries", 0);
         return out;
      }

      out.addProperty("present", true);
      if (!nullToEmpty(entry.serverName).isBlank()) {
         out.addProperty("serverName", entry.serverName);
      }

      out.addProperty("scannedAtMs", entry.scannedAtMs);
      out.addProperty("scanStatus", nullToEmpty(entry.scanStatus).isBlank() ? "UNKNOWN" : entry.scanStatus);
      out.addProperty("fresh", entry.scannedAtMs > 0L && System.currentTimeMillis() - entry.scannedAtMs < 5184000000L);
      out.add(
         "probes",
         AiJson.objectOf("total", entry.totalProbes, "answered", entry.answeredProbes, "retried", entry.retriedProbes, "failed", entry.failedProbes)
      );
      List<AiServerIntel.PluginReport> plugins = buildReports(entry.plugins, entry.commands, entry.evidence, entry.channels, entry.guis, entry.confidence, 40);
      out.addProperty("pluginCount", plugins.size());
      JsonObject bounded = boundedPluginsJson(plugins, 36000);

      for (Entry<String, JsonElement> field : bounded.entrySet()) {
         out.add(field.getKey(), field.getValue());
      }

      addFingerprints(out, address, brand);
      return out;
   }

   private static void addFingerprints(JsonObject out, String address, String brand) {
      if (!address.isBlank()) {
         List<autismclient.util.PluginSignatureCatalog.PluginFingerprint> fingerprints;
         try {
            fingerprints = autismclient.util.PluginSignatureCatalog.fingerprints(address, brand);
         } catch (Throwable ignored) {
            return;
         }

         if (fingerprints != null && !fingerprints.isEmpty()) {
            JsonArray array = new JsonArray();
            int included = 0;

            for (autismclient.util.PluginSignatureCatalog.PluginFingerprint fingerprint : fingerprints) {
               if (included++ >= 24) {
                  break;
               }

               array.add(
                  AiJson.objectOf(
                     "plugin",
                     nullToEmpty(fingerprint.plugin()),
                     "key",
                     nullToEmpty(fingerprint.key()),
                     "channels",
                     capped(fingerprint.channels(), 8),
                     "score",
                     fingerprint.score(),
                     "basis",
                     nullToEmpty(fingerprint.bestSource())
                  )
               );
            }

            out.add("payloadFingerprints", array);
            if (fingerprints.size() > 24) {
               out.addProperty("payloadFingerprintsTruncated", fingerprints.size() - 24);
            }
         }
      }
   }

   record PluginReport(String displayName, String key, String confidence, String evidence, List<String> commands, List<String> channels, List<String> guis) {
   }
}
