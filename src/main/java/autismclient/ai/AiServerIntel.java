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
   static final long init_J = 5184000000L;
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

   static String init(boolean connectedToServer) {
      return connectedToServer ? null : "Not connected to a multiplayer server; join one first (singleplayer has no server intelligence)";
   }

   static List<AiServerIntel.Inner_DataUtil> init(
      List<String> plugins,
      Map<String, List<String>> commands,
      Map<String, String> evidence,
      Map<String, List<String>> channels,
      Map<String, List<String>> guis,
      Map<String, String> confidence,
      int maxCommands
   ) {
      List<AiServerIntel.Inner_DataUtil> out = new ArrayList<>();
      if (plugins == null) {
         return out;
      }

      for (String name : plugins) {
         if (name != null && !name.isBlank()) {
            String display = name.trim();
            out.add(
               new AiServerIntel.Inner_DataUtil(
                  display,
                  autismclient.util.PluginSignatureCatalog.canonicalName(display),
                  load_Str(init_Str(confidence, display)),
                  load_Str(init_Str(evidence, display)),
                  init_List(init_List(commands, display), maxCommands),
                  init_List(init_List(channels, display), 24),
                  init_List(init_List(guis, display), 24)
               )
            );
         }
      }

      out.sort(Comparator.comparing(AiServerIntel.Inner_DataUtil::displayName, String.CASE_INSENSITIVE_ORDER));
      return out;
   }

   static boolean init_Z(String evidence) {
      boolean var10000;
      switch (load_Str(evidence)) {
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

   static boolean load_Z(String confidence) {
      boolean var10000;
      switch (load_Str(confidence)) {
         case "EXACT":
         case "STRONG":
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   static List<AiServerIntel.Inner_DataUtil> init(List<AiServerIntel.Inner_DataUtil> plugins) {
      List<AiServerIntel.Inner_DataUtil> out = new ArrayList<>();
      if (plugins != null) {
         for (AiServerIntel.Inner_DataUtil plugin : plugins) {
            if (plugin != null && !plugin.displayName().isBlank() && load_Z(plugin.confidence()) && !init_Z(plugin.evidence())) {
               out.add(plugin);
            }
         }
      }

      out.sort(Comparator.comparing(AiServerIntel.Inner_DataUtil::displayName, String.CASE_INSENSITIVE_ORDER));
      return out;
   }

   static String init_Str(String stored) {
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

   static JsonObject init_JsonObject(List<AiServerIntel.Inner_DataUtil> plugins, int charBudget) {
      JsonArray entries = new JsonArray();
      int included = 0;
      if (plugins != null) {
         for (AiServerIntel.Inner_DataUtil plugin : plugins) {
            JsonObject entry = init(plugin);
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

   private static JsonObject init(AiServerIntel.Inner_DataUtil plugin) {
      JsonObject entry = new JsonObject();
      entry.addProperty("name", plugin.displayName());
      if (!plugin.key().isBlank()) {
         entry.addProperty("key", plugin.key());
      }

      entry.addProperty("confidence", plugin.confidence());
      entry.addProperty("evidence", plugin.evidence());
      entry.addProperty("radarEligible", load_Z(plugin.confidence()) && !init_Z(plugin.evidence()));
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

   private static String load_Str(String value) {
      String clean = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
      return clean.isBlank() ? "UNKNOWN" : clean;
   }

   private static List<String> init_List(Map<String, List<String>> map, String plugin) {
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

   private static String init_Str(Map<String, String> map, String plugin) {
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

   private static List<String> init_List(List<String> values, int maxItems) {
      List<String> out = new ArrayList<>();
      if (values == null) {
         return out;
      }

      for (String value : values) {
         if (out.size() >= maxItems) {
            break;
         }

         if (value != null && !value.isBlank()) {
            out.add(init(value.trim(), 120));
         }
      }

      return out;
   }

   private static String init(String value, int max) {
      if (value == null) {
         return "";
      } else {
         return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + "…";
      }
   }

   private static String read(String value) {
      return value == null ? "" : value;
   }

   public static AiOperation create(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      final String address = init_Str(mc);
      String precondition = init(!address.isBlank());
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
      final String brand = load(mc);
      overlay.startAutoScan();
      return new AiOperation() {
         final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
         long load_J = startRevision;
         int init_I;
         int load_I;
         boolean init_Z;
         boolean load_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.load_Z) {
               return AiGameOperations.result(AiOperation.Status.CANCELLED, "Plugin scan cancelled", new JsonObject());
            }

            String now = AiServerIntel.init_Str(Minecraft.getInstance());
            if (!now.equals(address)) {
               this.load_Z = true;
               return AiGameOperations.result(AiOperation.Status.CANCELLED, "Left the server during the plugin scan", new JsonObject());
            }

            if (autismclient.modules.aD.init_Z()) {
               return AiGameOperations.result(AiOperation.Status.FAILED, "Client is hidden (panic mode); plugin probing is suspended", new JsonObject());
            }

            AutismConfig config = AutismConfig.getGlobal();
            AutismConfig.PluginScanCacheEntry entry = config.getPluginScanByAddress(address);
            long revision = config.pluginScanRevision();
            if (revision != this.load_J) {
               this.load_J = revision;
               this.init_I = 0;
            } else {
               this.init_I++;
            }

            boolean scanning = overlay.isScanRunning();
            this.init_Z |= scanning;
            this.load_I++;
            if (!scanning && this.init_I >= 5) {
               if (entry != null) {
                  return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Plugin scan results cached for " + address, this.init(entry, false));
               }

               if (this.init_Z || revision != startRevision) {
                  return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Plugin scan completed with no detected plugins", this.init(null, false));
               }

               if (this.load_I % 20 == 0) {
                  overlay.startAutoScan();
               }
            }

            if (System.nanoTime() > this.init_J) {
               this.load_Z = true;
               return AiGameOperations.result(
                  AiOperation.Status.TIMED_OUT, "Plugin scan did not finish in time; returning cached partial results", this.init(entry, true)
               );
            } else {
               JsonObject progress = new JsonObject();
               progress.addProperty("scanning", scanning);
               progress.addProperty("cachedEntry", entry != null);
               return AiGameOperations.result(AiOperation.Status.RUNNING, scanning ? "Probing server plugins" : "Waiting for plugin scan results", progress);
            }
         }

         private JsonObject init(AutismConfig.PluginScanCacheEntry entry, boolean partial) {
            return AiServerIntel.init(address, entry, false, partial, brand);
         }

         @Override
         public void cancel() {
            this.load_Z = true;
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
         address = init_Str(mc);
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
                  address = !read(value.serverAddress).isBlank() ? value.serverAddress.trim().toLowerCase(Locale.ROOT) : candidate.getKey().split("\\|", 2)[0];
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
         return init(address, entry, true, false, load(mc));
      }
   }

   private static String init_Str(Minecraft mc) {
      if (mc == null) {
         return "";
      }

      ServerData server = mc.getCurrentServer();
      return server != null && server.ip != null && !server.ip.isBlank() ? server.ip.trim().toLowerCase(Locale.ROOT) : "";
   }

   private static String load(Minecraft mc) {
      try {
         return mc != null && mc.getConnection() != null && mc.getConnection().serverBrand() != null ? mc.getConnection().serverBrand() : "";
      } catch (Throwable ignored) {
         return "";
      }
   }

   private static JsonObject init(String address, AutismConfig.PluginScanCacheEntry entry, boolean cached, boolean partial, String brand) {
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
      if (!read(entry.serverName).isBlank()) {
         out.addProperty("serverName", entry.serverName);
      }

      out.addProperty("scannedAtMs", entry.scannedAtMs);
      out.addProperty("scanStatus", read(entry.scanStatus).isBlank() ? "UNKNOWN" : entry.scanStatus);
      out.addProperty("fresh", entry.scannedAtMs > 0L && System.currentTimeMillis() - entry.scannedAtMs < 5184000000L);
      out.add(
         "probes",
         AiJson.objectOf("total", entry.totalProbes, "answered", entry.answeredProbes, "retried", entry.retriedProbes, "failed", entry.failedProbes)
      );
      List<AiServerIntel.Inner_DataUtil> plugins = init(entry.plugins, entry.commands, entry.evidence, entry.channels, entry.guis, entry.confidence, 40);
      out.addProperty("pluginCount", plugins.size());
      JsonObject bounded = init_JsonObject(plugins, 36000);

      for (Entry<String, JsonElement> field : bounded.entrySet()) {
         out.add(field.getKey(), field.getValue());
      }

      init(out, address, brand);
      return out;
   }

   private static void init(JsonObject out, String address, String brand) {
      if (!address.isBlank()) {
         List<autismclient.util.PluginSignatureCatalog.Inner_CachePool> fingerprints;
         try {
            fingerprints = autismclient.util.PluginSignatureCatalog.fingerprints(address, brand);
         } catch (Throwable ignored) {
            return;
         }

         if (fingerprints != null && !fingerprints.isEmpty()) {
            JsonArray array = new JsonArray();
            int included = 0;

            for (autismclient.util.PluginSignatureCatalog.Inner_CachePool fingerprint : fingerprints) {
               if (included++ >= 24) {
                  break;
               }

               array.add(
                  AiJson.objectOf(
                     "plugin",
                     read(fingerprint.plugin()),
                     "key",
                     read(fingerprint.load()),
                     "channels",
                     init_List(fingerprint.channels(), 8),
                     "score",
                     fingerprint.score(),
                     "basis",
                     read(fingerprint.read())
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

   record Inner_DataUtil(String displayName, String key, String confidence, String evidence, List<String> commands, List<String> channels, List<String> guis) {
   }
}
