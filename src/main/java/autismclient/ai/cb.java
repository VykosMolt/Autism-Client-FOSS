package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.modules.PoolEntry;
import autismclient.util.AutismConfig;
import autismclient.util.AutismDupeRadar;
import autismclient.util.AutismServerInfoOverlay;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public final class cb {
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

   private cb() {
   }

   static String init(boolean connectedToServer) {
      return connectedToServer ? null : "Not connected to a multiplayer server; join one first (singleplayer has no server intelligence)";
   }

   static String init(boolean connectedToServer, boolean scanCached, boolean authenticated) {
      String server = init(connectedToServer);
      if (server != null) {
         return server;
      } else if (!scanCached) {
         return "No plugin scan is cached for this server; run client:plugin_scan first";
      } else {
         return !authenticated ? "DupeDB login required once; log in via the server info overlay, then retry" : null;
      }
   }

   static List<cb.Inner_DataUtil> init(
      List<String> plugins,
      Map<String, List<String>> commands,
      Map<String, String> evidence,
      Map<String, List<String>> channels,
      Map<String, List<String>> guis,
      Map<String, String> confidence,
      int maxCommands
   ) {
      List<cb.Inner_DataUtil> out = new ArrayList<>();
      if (plugins == null) {
         return out;
      }

      for (String name : plugins) {
         if (name != null && !name.isBlank()) {
            String display = name.trim();
            out.add(
               new cb.Inner_DataUtil(
                  display,
                  autismclient.util.bQ.init_Str(display),
                  load_Str(init_Str(confidence, display)),
                  load_Str(init_Str(evidence, display)),
                  init_List(init_List(commands, display), maxCommands),
                  init_List(init_List(channels, display), 24),
                  init_List(init_List(guis, display), 24)
               )
            );
         }
      }

      out.sort(Comparator.comparing(cb.Inner_DataUtil::displayName, String.CASE_INSENSITIVE_ORDER));
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

   static List<cb.Inner_DataUtil> init(List<cb.Inner_DataUtil> plugins) {
      List<cb.Inner_DataUtil> out = new ArrayList<>();
      if (plugins != null) {
         for (cb.Inner_DataUtil plugin : plugins) {
            if (plugin != null && !plugin.displayName().isBlank() && load_Z(plugin.confidence()) && !init_Z(plugin.evidence())) {
               out.add(plugin);
            }
         }
      }

      out.sort(Comparator.comparing(cb.Inner_DataUtil::displayName, String.CASE_INSENSITIVE_ORDER));
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

   static JsonObject init_JsonObject(List<cb.Inner_DataUtil> plugins, int charBudget) {
      JsonArray entries = new JsonArray();
      int included = 0;
      if (plugins != null) {
         for (cb.Inner_DataUtil plugin : plugins) {
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

   private static JsonObject init(cb.Inner_DataUtil plugin) {
      JsonObject entry = new JsonObject();
      entry.addProperty("name", plugin.displayName());
      if (!plugin.key().isBlank()) {
         entry.addProperty("key", plugin.key());
      }

      entry.addProperty("confidence", plugin.confidence());
      entry.addProperty("evidence", plugin.evidence());
      entry.addProperty("radarEligible", load_Z(plugin.confidence()) && !init_Z(plugin.evidence()));
      if (!plugin.commands().isEmpty()) {
         entry.add("commands", ag.init.toJsonTree(plugin.commands()));
      }

      if (!plugin.channels().isEmpty()) {
         entry.add("channels", ag.init.toJsonTree(plugin.channels()));
      }

      if (!plugin.guis().isEmpty()) {
         entry.add("guis", ag.init.toJsonTree(plugin.guis()));
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

         String key = autismclient.util.bQ.init_Str(plugin);

         for (Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getKey() != null && autismclient.util.bQ.init_Str(entry.getKey()).equals(key)) {
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

         String key = autismclient.util.bQ.init_Str(plugin);

         for (Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey() != null && autismclient.util.bQ.init_Str(entry.getKey()).equals(key)) {
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

   public static AiOperation init_AiOperation(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      final String address = init_Str(mc);
      String precondition = init(!address.isBlank());
      if (precondition != null) {
         throw new IllegalArgumentException(precondition);
      }

      boolean force = ag.init_Z_JsonObject(args, "force", false);
      final int timeoutSeconds = Math.max(1, Math.min(150, ag.init_I_JsonObject(args, "timeoutSeconds", 150)));
      final AutismServerInfoOverlay overlay = PoolEntry.init_PoolEntry().init_AutismServerInfoOv();
      if (force) {
         overlay.open_V_12();
      }

      final long startRevision = AutismConfig.getGlobal().pluginScanRevision();
      final String brand = load(mc);
      overlay.tick_Z_6();
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
               return R.init_AiOperation_Result(AiOperation.Status.CANCELLED, "Plugin scan cancelled", new JsonObject());
            }

            String now = cb.init_Str(Minecraft.getInstance());
            if (!now.equals(address)) {
               this.load_Z = true;
               return R.init_AiOperation_Result(AiOperation.Status.CANCELLED, "Left the server during the plugin scan", new JsonObject());
            }

            if (autismclient.modules.aD.init_Z()) {
               return R.init_AiOperation_Result(AiOperation.Status.FAILED, "Client is hidden (panic mode); plugin probing is suspended", new JsonObject());
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

            boolean scanning = overlay.run_Z_5();
            this.init_Z |= scanning;
            this.load_I++;
            if (!scanning && this.init_I >= 5) {
               if (entry != null) {
                  return R.init_AiOperation_Result(AiOperation.Status.SUCCEEDED, "Plugin scan results cached for " + address, this.init(entry, false));
               }

               if (this.init_Z || revision != startRevision) {
                  return R.init_AiOperation_Result(AiOperation.Status.SUCCEEDED, "Plugin scan completed with no detected plugins", this.init(null, false));
               }

               if (this.load_I % 20 == 0) {
                  overlay.tick_Z_6();
               }
            }

            if (System.nanoTime() > this.init_J) {
               this.load_Z = true;
               return R.init_AiOperation_Result(
                  AiOperation.Status.TIMED_OUT, "Plugin scan did not finish in time; returning cached partial results", this.init(entry, true)
               );
            } else {
               JsonObject progress = new JsonObject();
               progress.addProperty("scanning", scanning);
               progress.addProperty("cachedEntry", entry != null);
               return R.init_AiOperation_Result(AiOperation.Status.RUNNING, scanning ? "Probing server plugins" : "Waiting for plugin scan results", progress);
            }
         }

         private JsonObject init(AutismConfig.PluginScanCacheEntry entry, boolean partial) {
            return cb.init(address, entry, false, partial, brand);
         }

         @Override
         public void cancel() {
            this.load_Z = true;
         }
      };
   }

   public static JsonObject init_JsonObject(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      AutismConfig config = AutismConfig.getGlobal();
      String requested = ag.init_Str_JsonObject(args, "address", "").trim().toLowerCase(Locale.ROOT);
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

   public static AiOperation load(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      final String address = init_Str(mc);
      AutismConfig.PluginScanCacheEntry entry = address.isBlank() ? null : AutismConfig.getGlobal().getPluginScanByAddress(address);
      String precondition = init(!address.isBlank(), entry != null, AutismDupeRadar.init_AutismDupeRadar_Ra().authenticated());
      if (precondition != null) {
         throw new IllegalArgumentException(precondition);
      }

      final boolean forceRefresh = ag.init_Z_JsonObject(args, "forceRefresh", false);
      final int timeoutSeconds = Math.max(1, Math.min(60, ag.init_I_JsonObject(args, "timeoutSeconds", 60)));
      List<cb.Inner_DataUtil> candidates = init(init(entry.plugins, entry.commands, entry.evidence, entry.channels, entry.guis, entry.confidence, 40));
      List<AutismDupeRadar.RadarPluginSnapshot> snapshots = new ArrayList<>(candidates.size());

      for (cb.Inner_DataUtil plugin : candidates) {
         snapshots.add(
            new AutismDupeRadar.RadarPluginSnapshot(
               plugin.displayName(), plugin.key(), init_Str(plugin.confidence()), plugin.commands().size(), plugin.channels(), plugin.guis(), false
            )
         );
      }

      AutismDupeRadar.RadarServerSnapshot server = init_AutismDupeRadar_Ra(mc);
      final long resultsBefore = AutismDupeRadar.init_AutismDupeRadar_Ra().updatedAtMs();
      AutismDupeRadar.init_V_List(snapshots, server, forceRefresh);
      final int submittedPlugins = snapshots.size();
      return new AiOperation() {
         final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.init_Z) {
               return R.init_AiOperation_Result(AiOperation.Status.CANCELLED, "DupeDB check cancelled", new JsonObject());
            }

            AutismDupeRadar.RadarState state = AutismDupeRadar.init_AutismDupeRadar_Ra();
            if (!state.checking() && !state.authenticating()) {
               if (state.error() != null && !state.error().isBlank() && state.updatedAtMs() <= resultsBefore) {
                  return R.init_AiOperation_Result(
                     AiOperation.Status.FAILED, "DupeDB check failed: " + state.error(), cb.init(address, state, submittedPlugins, forceRefresh, true)
                  );
               }

               if (state.updatedAtMs() > resultsBefore) {
                  return R.init_AiOperation_Result(
                     AiOperation.Status.SUCCEEDED, "DupeDB radar results for " + address, cb.init(address, state, submittedPlugins, forceRefresh, false)
                  );
               }
            }

            if (System.nanoTime() > this.init_J) {
               this.init_Z = true;
               return R.init_AiOperation_Result(
                  AiOperation.Status.TIMED_OUT, "DupeDB check did not finish in time", cb.init(address, state, submittedPlugins, forceRefresh, true)
               );
            } else {
               return R.init_AiOperation_Result(AiOperation.Status.RUNNING, state.status() == null ? "Checking DupeDB" : state.status(), new JsonObject());
            }
         }

         @Override
         public void cancel() {
            this.init_Z = true;
         }
      };
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

   private static AutismDupeRadar.RadarServerSnapshot init_AutismDupeRadar_Ra(Minecraft mc) {
      List<AutismDupeRadar.RadarServerIdentity> identities = new ArrayList<>();
      ServerData server = mc.getCurrentServer();
      if (server != null && server.ip != null) {
         String host = server.ip.trim();
         int colon = host.lastIndexOf(58);
         if (colon > 0 && host.indexOf(58) == colon) {
            host = host.substring(0, colon);
         }

         init(identities, host);
      }

      try {
         if (mc.getConnection() != null
            && mc.getConnection().getConnection() != null
            && mc.getConnection().getConnection().getRemoteAddress() instanceof InetSocketAddress socket
            && socket.getAddress() != null) {
            identities.add(new AutismDupeRadar.RadarServerIdentity(socket.getAddress().getHostAddress(), true));
         }
      } catch (Throwable var5) {
      }

      return new AutismDupeRadar.RadarServerSnapshot(identities);
   }

   private static void init(List<AutismDupeRadar.RadarServerIdentity> identities, String host) {
      if (host != null && !host.isBlank()) {
         boolean ip = host.matches("^\\d{1,3}(\\.\\d{1,3}){3}$") || host.contains(":");
         identities.add(new AutismDupeRadar.RadarServerIdentity(host, ip));
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
         ag.init_JsonObject_ObjArr("total", entry.totalProbes, "answered", entry.answeredProbes, "retried", entry.retriedProbes, "failed", entry.failedProbes)
      );
      List<cb.Inner_DataUtil> plugins = init(entry.plugins, entry.commands, entry.evidence, entry.channels, entry.guis, entry.confidence, 40);
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
         List<autismclient.util.bQ.Inner_CachePool> fingerprints;
         try {
            fingerprints = autismclient.util.bQ.init_List(address, brand);
         } catch (Throwable ignored) {
            return;
         }

         if (fingerprints != null && !fingerprints.isEmpty()) {
            JsonArray array = new JsonArray();
            int included = 0;

            for (autismclient.util.bQ.Inner_CachePool fingerprint : fingerprints) {
               if (included++ >= 24) {
                  break;
               }

               array.add(
                  ag.init_JsonObject_ObjArr(
                     "plugin",
                     read(fingerprint.init_Str()),
                     "key",
                     read(fingerprint.load()),
                     "channels",
                     init_List(fingerprint.init_List(), 8),
                     "score",
                     fingerprint.init_I(),
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

   private static JsonObject init(String address, AutismDupeRadar.RadarState state, int submittedPlugins, boolean forceRefresh, boolean partial) {
      JsonObject out = new JsonObject();
      out.addProperty("serverAddress", address);
      out.addProperty("provider", read(state.providerLabel()));
      out.addProperty("forceRefresh", forceRefresh);
      if (partial) {
         out.addProperty("partial", true);
      }

      out.addProperty("status", read(state.status()));
      if (state.error() != null && !state.error().isBlank()) {
         out.addProperty("error", state.error());
      }

      out.addProperty("submittedPluginCount", submittedPlugins);
      out.addProperty("detectedPluginCount", Math.max(state.detectedPluginCount(), submittedPlugins));
      out.addProperty("updatedAtMs", state.updatedAtMs());
      out.addProperty("stale", state.stale());
      List<AutismDupeRadar.RadarMatch> matches = state.matches();
      out.addProperty("matchCount", matches.size());
      JsonArray array = new JsonArray();
      List<AutismDupeRadar.RadarFinding> allFindings = new ArrayList<>();
      int included = 0;

      for (AutismDupeRadar.RadarMatch match : matches) {
         allFindings.addAll(match.findings());
         if (included++ < 40) {
            JsonObject row = new JsonObject();
            row.addProperty("detectedPlugin", match.detectedPlugin());
            if (!match.providerPlugin().isBlank()) {
               row.addProperty("providerPlugin", match.providerPlugin());
            }

            row.addProperty("confidence", match.matchConfidence());
            row.addProperty("source", match.matchSource().label());
            row.addProperty("score", Math.round(match.matchScore() * 1000.0) / 1000.0);
            if (!match.matchedInput().isBlank()) {
               row.addProperty("matchedInput", match.matchedInput());
            }

            if (!match.sourceUrl().isBlank()) {
               row.addProperty("url", match.sourceUrl());
            }

            JsonArray findings = new JsonArray();
            int findingCount = 0;

            for (AutismDupeRadar.RadarFinding finding : match.findings()) {
               if (findingCount++ >= 10) {
                  break;
               }

               findings.add(
                  ag.init_JsonObject_ObjArr(
                     "title",
                     read(finding.title()),
                     "status",
                     read(finding.status()),
                     "summary",
                     init(finding.summary(), 280),
                     "url",
                     read(finding.sourceUrl())
                  )
               );
            }

            row.add("findings", findings);
            if (match.findings().size() > 10) {
               row.addProperty("findingsTruncated", match.findings().size() - 10);
            }

            array.add(row);
         }
      }

      out.add("matches", array);
      if (matches.size() > 40) {
         out.addProperty("matchesTruncated", matches.size() - 40);
      }

      AutismDupeRadar.StatusCounts counts = AutismDupeRadar.init_AutismDupeRadar_St_List(allFindings);
      out.add("statusCounts", ag.init_JsonObject_ObjArr("working", counts.working(), "patched", counts.patched(), "verified", counts.verified()));
      return out;
   }

   record Inner_DataUtil(String displayName, String key, String confidence, String evidence, List<String> commands, List<String> channels, List<String> guis) {
   }
}
