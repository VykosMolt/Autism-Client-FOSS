package autismclient.util;

import autismclient.AutismClientAddon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

public final class AutismDupeRadar {
   private static final Gson init_Gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private static final String init_Str = "dupedb";
   private static final String load_Str = "DupeDB";
   private static final String read_Str = "https://dupedb.net";
   private static final String write_Str = "catr-dupedb-radar";
   private static final long init_J = TimeUnit.MINUTES.toMillis(30L);
   private static final int init_I = 8;
   private static final int load_I = 8;
   private static final String update_Str = "working,verified,patched,unverified";
   private static final int read_I = 8000;
   private static final int write_I = 12000;
   private static final int update_I = 50;
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private static final ExecutorService init_ExecutorService = Executors.newCachedThreadPool(new ThreadFactory() {
      @Override
      public Thread newThread(Runnable runnable) {
         Thread thread = new Thread(runnable, "AUTISM-DupeRadar-" + AutismDupeRadar.init_AtomicInteger.incrementAndGet());
         thread.setDaemon(true);
         return thread;
      }
   });
   private static final AtomicInteger load_AtomicInteger = new AtomicInteger();
   private static final Object init_Obj = new Object();
   private static final Object AUDIT_STATE_LOCK = new Object();
   private static final Object AUDIT_REFRESH_LOCK = new Object();
   private static final AtomicReference<ServerSocket> AUDIT_OAUTH_SERVER = new AtomicReference<>();
   private static final int AUDIT_MAX_CACHE_ENTRIES = 512;
   private static final int AUDIT_MAX_HTTP_CHARS = 4 * 1024 * 1024;
   private static final Path init_Path = AutismClientAddon.FOLDER.toPath().resolve("providers").resolve("dupedb-cache.json");
   private static AutismDupeRadar.ProviderCache init_AutismDupeRadar_Pr = init_AutismDupeRadar_Pr();
   private static boolean auditCacheChanged;
   private static volatile AutismDupeRadar.RadarState init_AutismDupeRadar_Ra = load_AutismDupeRadar_Ra();

   private AutismDupeRadar() {
   }

   private static void auditCloseActiveOAuthServer() {
      ServerSocket active = AUDIT_OAUTH_SERVER.getAndSet(null);
      if (active != null) {
         try {
            active.close();
         } catch (IOException ignored) {
         }
      }
   }

   private static void auditUpdateState(UnaryOperator<AutismDupeRadar.RadarState> fn) {
      synchronized (AUDIT_STATE_LOCK) {
         init_AutismDupeRadar_Ra = fn.apply(init_AutismDupeRadar_Ra);
      }
   }

   private static void auditUpdateStateIfGeneration(int generation, UnaryOperator<AutismDupeRadar.RadarState> fn) {
      synchronized (AUDIT_STATE_LOCK) {
         if (generation == load_AtomicInteger.get()) {
            init_AutismDupeRadar_Ra = fn.apply(init_AutismDupeRadar_Ra);
         }
      }
   }

   private static String auditGetUsername() {
      synchronized (init_Obj) {
         return init_AutismDupeRadar_Pr == null ? null : init_AutismDupeRadar_Pr.username;
      }
   }

   public static AutismDupeRadar.RadarState init_AutismDupeRadar_Ra() {
      sync_V();
      return init_AutismDupeRadar_Ra;
   }

   public static String init_Str() {
      return "DupeDB";
   }

   public static String load_Str() {
      return "https://dupedb.net";
   }

   public static void init_V() {
      int generation;
      synchronized (AUDIT_STATE_LOCK) {
         AutismDupeRadar.RadarState current = init_AutismDupeRadar_Ra;
         if (current.authenticating() || current.authenticated()) {
            return;
         }

         generation = load_AtomicInteger.incrementAndGet();
         init_AutismDupeRadar_Ra = current.withBusy(true, false, "Opening DupeDB login...", null);
      }

      auditCloseActiveOAuthServer();
      CompletableFuture.runAsync(() -> init_V_I(generation), init_ExecutorService);
   }

   public static void load_V() {
      load_AtomicInteger.incrementAndGet();
      auditCloseActiveOAuthServer();
      synchronized (init_Obj) {
         init_AutismDupeRadar_Pr.accessToken = null;
         init_AutismDupeRadar_Pr.refreshToken = null;
         init_AutismDupeRadar_Pr.username = null;
         init_AutismDupeRadar_Pr.userUpdatedAt = 0L;
         flush();
      }

      auditUpdateState(s -> s.withAuth(false, null).withBusy(false, false, "Logged out.", null).withMatches(List.of(), 0, false));
   }

   public static void read_V() {
      int generation;
      synchronized (AUDIT_STATE_LOCK) {
         generation = load_AtomicInteger.incrementAndGet();
         init_AutismDupeRadar_Ra = init_AutismDupeRadar_Ra.withBusy(true, false, "Refreshing DupeDB user...", null);
      }

      CompletableFuture.runAsync(
         () -> {
            try {
               String token = sync_Str();
               if (token == null || token.isBlank()) {
                  auditUpdateStateIfGeneration(
                     generation, s -> s.withAuth(false, null).withBusy(false, false, "Login required.", "No DupeDB token saved.")
                  );
                  return;
               }

               String username = write_Str();
               synchronized (init_Obj) {
                  init_AutismDupeRadar_Pr.username = username;
                  init_AutismDupeRadar_Pr.userUpdatedAt = System.currentTimeMillis();
                  flush();
               }

               auditUpdateStateIfGeneration(generation, s -> s.withAuth(true, username).withBusy(false, false, "User refreshed.", null));
            } catch (Exception ex) {
               auditUpdateStateIfGeneration(generation, s -> s.withBusy(false, false, "User refresh failed.", init_Str_Exception(ex)));
            }
         },
         init_ExecutorService
      );
   }

   public static void init_V_List_Z(List<AutismDupeRadar.RadarPluginSnapshot> plugins, boolean forceRefresh) {
      init_V_List(plugins, AutismDupeRadar.RadarServerSnapshot.EMPTY, forceRefresh);
   }

   public static void init_V_List(List<AutismDupeRadar.RadarPluginSnapshot> plugins, AutismDupeRadar.RadarServerSnapshot server, boolean forceRefresh) {
      List<AutismDupeRadar.RadarPluginSnapshot> snapshots = plugins == null ? List.of() : List.copyOf(plugins);
      AutismDupeRadar.RadarServerSnapshot serverSnapshot = server == null ? AutismDupeRadar.RadarServerSnapshot.EMPTY : server;
      int generation;
      synchronized (AUDIT_STATE_LOCK) {
         generation = load_AtomicInteger.incrementAndGet();
         init_AutismDupeRadar_Ra = init_AutismDupeRadar_Ra.withBusy(false, true, forceRefresh ? "Refreshing radar data..." : "Checking server...", null)
            .withPluginCount(snapshots.size());
      }

      CompletableFuture.runAsync(() -> init_V_I(generation, snapshots, serverSnapshot, forceRefresh), init_ExecutorService);
   }

   public static void write_V() {
      synchronized (AUDIT_STATE_LOCK) {
         load_AtomicInteger.incrementAndGet();
         init_AutismDupeRadar_Ra = init_AutismDupeRadar_Ra.withMatches(List.of(), 0, false).withBusy(false, false, "No server checked.", null);
      }
   }

   public static void update_V() {
      synchronized (AUDIT_STATE_LOCK) {
         load_AtomicInteger.incrementAndGet();
         init_AutismDupeRadar_Ra = init_AutismDupeRadar_Ra.withBusy(false, false, "Canceled.", null);
      }

      auditCloseActiveOAuthServer();
   }

   public static String read_Str() {
      AutismDupeRadar.RadarState current = init_AutismDupeRadar_Ra();
      StringBuilder sb = new StringBuilder();
      sb.append("Radar - ").append("DupeDB").append('\n');
      sb.append("Status: ").append(current.status()).append('\n');
      if (current.error() != null && !current.error().isBlank()) {
         sb.append("Error: ").append(current.error()).append('\n');
      }

      sb.append("Plugins checked: ").append(current.detectedPluginCount()).append('\n');
      sb.append("Matches: ").append(current.matches().size()).append("\n\n");
      if (current.matches().isEmpty()) {
         sb.append("No matches.\n");
         return sb.toString();
      }

      for (AutismDupeRadar.RadarMatch match : current.matches()) {
         sb.append("- ")
            .append(match.displayLabel())
            .append(" [")
            .append(match.matchConfidence())
            .append(" ")
            .append(match.matchSource().label())
            .append("]")
            .append(" findings=")
            .append(match.findings().size())
            .append(" source=")
            .append(match.sourceUrl())
            .append('\n');
      }

      return sb.toString();
   }

   public static void init_V_Str(String url) {
      if (url != null && !url.isBlank()) {
         try {
            aJ.init_V(init_Str_Str_Str(url, ""));
         } catch (Exception var2) {
         }
      }
   }

   private static AutismDupeRadar.RadarState load_AutismDupeRadar_Ra() {
      AutismDupeRadar.ProviderCache loaded = init_AutismDupeRadar_Pr;
      boolean authed = loaded != null && loaded.accessToken != null && !loaded.accessToken.isBlank();
      String username = loaded == null ? null : loaded.username;
      return new AutismDupeRadar.RadarState(
         "dupedb", "DupeDB", authed, false, false, username, authed ? "Ready." : "Login required.", null, List.of(), 0, 0L, false
      );
   }

   private static void sync_V() {
      boolean authed;
      String username;
      synchronized (init_Obj) {
         AutismDupeRadar.ProviderCache loaded = init_AutismDupeRadar_Pr;
         if (loaded == null) {
            return;
         }
         authed = loaded.accessToken != null && !loaded.accessToken.isBlank();
         username = loaded.username;
      }

      synchronized (AUDIT_STATE_LOCK) {
         AutismDupeRadar.RadarState current = init_AutismDupeRadar_Ra;
         if (current.authenticated() != authed || !Objects.equals(current.username(), username)) {
            init_AutismDupeRadar_Ra = current.withAuth(authed, username);
         }
      }
   }

   private static void init_V_I(int generation) {
      String verifier = init_Str_I(48);
      String challenge = poll(verifier);
      String stateToken = init_Str_I(24);
      ServerSocket boundServer = null;

      try (ServerSocket server = new ServerSocket()) {
         boundServer = server;
         server.setReuseAddress(true);
         server.bind(new InetSocketAddress("127.0.0.1", 0));
         ServerSocket prev = AUDIT_OAUTH_SERVER.getAndSet(server);
         if (prev != null && prev != server) {
            try {
               prev.close();
            } catch (IOException ignored) {
            }
         }
         if (generation != load_AtomicInteger.get()) {
            return;
         }

         int port = server.getLocalPort();
         String redirect = "http://127.0.0.1:" + port + "/callback";
         String loginUrl = "https://dupedb.net/api/oauth/authorize?response_type=code&client_id="
            + push("catr-dupedb-radar")
            + "&redirect_uri="
            + push(redirect)
            + "&code_challenge="
            + push(challenge)
            + "&code_challenge_method=S256&state="
            + push(stateToken);
         aJ.init_V(loginUrl);
         auditUpdateStateIfGeneration(generation, s -> s.withBusy(true, false, "Waiting for browser login...", null));

         long deadline = System.currentTimeMillis() + 120000L;
         String code = null;
         String returnedState = null;

         while (true) {
            if (generation != load_AtomicInteger.get()) {
               return;
            }

            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0L) {
               throw new IOException("Login timed out.");
            }

            server.setSoTimeout((int)Math.min(remaining, 120000L));
            Socket accepted;
            try {
               accepted = server.accept();
            } catch (SocketTimeoutException timedOut) {
               continue;
            }

            try (Socket socket = accepted) {
               socket.setSoTimeout(5000);
               BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
               String request = reader.readLine();
               if (request == null || request.isBlank()) {
                  continue;
               }

               int start = request.indexOf(32);
               int end = start >= 0 ? request.indexOf(32, start + 1) : -1;
               if (start < 0 || end <= start) {
                  continue;
               }

               URI uri = URI.create("http://127.0.0.1" + request.substring(start + 1, end));
               if (!"/callback".equals(uri.getPath())) {
                  byte[] notFound = "Not Found".getBytes(StandardCharsets.UTF_8);
                  OutputStream out = socket.getOutputStream();
                  out.write(("HTTP/1.1 404 Not Found\r\nContent-Length: " + notFound.length + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                  out.write(notFound);
                  out.flush();
                  continue;
               }

               Map<String, String> params = init_Map_Str(uri.getRawQuery());
               code = params.get("code");
               returnedState = params.get("state");

               String responseBody = "<html><body style=\"font-family:sans-serif;background:#111;color:#eee\"><h2>AUTISM Radar login received.</h2>You can close this tab.</body></html>";
               byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
               OutputStream out = socket.getOutputStream();
               out.write(
                  ("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: " + responseBytes.length + "\r\nConnection: close\r\n\r\n")
                     .getBytes(StandardCharsets.UTF_8)
               );
               out.write(responseBytes);
               out.flush();
               break;
            } catch (IOException stray) {
               // One bad connection (preconnect, reset) must not end the login; a callback already read still counts.
               if (code != null) {
                  break;
               }
            }
         }

         if (generation != load_AtomicInteger.get()) {
            return;
         }

         if (code == null || code.isBlank()) {
            throw new IOException("No authorization code returned.");
         }

         if (!stateToken.equals(returnedState)) {
            throw new IOException("OAuth state did not match.");
         }

         AutismDupeRadar.TokenResponse token = init_AutismDupeRadar_To_Str(code, verifier, redirect);
         String username = read(token.accessToken);
         if (generation != load_AtomicInteger.get()) {
            return;
         }

         synchronized (init_Obj) {
            init_AutismDupeRadar_Pr.accessToken = token.accessToken;
            init_AutismDupeRadar_Pr.refreshToken = token.refreshToken;
            init_AutismDupeRadar_Pr.username = username;
            init_AutismDupeRadar_Pr.userUpdatedAt = System.currentTimeMillis();
            flush();
         }

         auditUpdateStateIfGeneration(generation, s -> s.withAuth(true, username).withBusy(false, false, "Logged in.", null));
      } catch (Exception ex) {
         auditUpdateStateIfGeneration(generation, s -> s.withBusy(false, false, "Login failed.", init_Str_Exception(ex)));
      } finally {
         if (boundServer != null) {
            AUDIT_OAUTH_SERVER.compareAndSet(boundServer, null);
         }
      }
   }

   private static void init_V_I(
      int generation, List<AutismDupeRadar.RadarPluginSnapshot> plugins, AutismDupeRadar.RadarServerSnapshot server, boolean forceRefresh
   ) {
      try {
         String token = sync_Str();
         if (token == null || token.isBlank()) {
            auditUpdateStateIfGeneration(
               generation, s -> s.withAuth(false, null).withBusy(false, false, "Login required.", "Log in to DupeDB before checking.")
            );
            return;
         }

         Map<String, AutismDupeRadar.RadarFindingHit> dedupedHits = new LinkedHashMap<>();
         List<AutismDupeRadar.RadarPluginSnapshot> orderedPlugins = new ArrayList<>(plugins);
         orderedPlugins.removeIf(snapshot -> snapshot == null || !snapshot.isPluginIdentity());
         orderedPlugins.sort(Comparator.comparing(AutismDupeRadar.RadarPluginSnapshot::displayName, String.CASE_INSENSITIVE_ORDER));

         for (AutismDupeRadar.RadarPluginSnapshot snapshot : orderedPlugins) {
            if (generation != load_AtomicInteger.get()) {
               auditFlushIfCacheChanged();
               return;
            }

            try {
               List<AutismDupeRadar.RadarFinding> findings = init_List_Str_Z(snapshot.displayName(), forceRefresh);
               if (!findings.isEmpty()) {
                  boolean exact = init_Z_List_Str(findings, snapshot.displayName());
                  String provider = init_Str_List_Str(findings, snapshot.displayName());
                  AutismDupeRadar.RadarMatch match = new AutismDupeRadar.RadarMatch(
                     snapshot.displayName(),
                     provider,
                     exact ? "Exact" : "Fuzzy",
                     exact ? AutismDupeRadar.MatchSource.PLUGIN : AutismDupeRadar.MatchSource.PLUGIN_FUZZY,
                     exact ? 1.0 : 0.9,
                     snapshot.displayName(),
                     "https://dupedb.net/plugins/" + pop(provider),
                     "DupeDB",
                     findings,
                     init_J_List(findings)
                  );
                  init_V_Map_AutismDupeRadar_Ra(dedupedHits, match);
               }
            } catch (Exception var13) {
            }
         }

         for (AutismDupeRadar.RadarServerIdentity identity : server.identities()) {
            if (generation != load_AtomicInteger.get()) {
               auditFlushIfCacheChanged();
               return;
            }

            if (identity != null && !identity.value().isBlank()) {
               List<AutismDupeRadar.RadarFinding> findings = init_List_AutismDupeRadar_Ra_Z(identity, forceRefresh);
               if (!findings.isEmpty()) {
                  AutismDupeRadar.MatchSource source = identity.ip() ? AutismDupeRadar.MatchSource.SERVER_IP : AutismDupeRadar.MatchSource.SERVER_DOMAIN;
                  AutismDupeRadar.RadarMatch match = new AutismDupeRadar.RadarMatch(
                     identity.value(),
                     identity.value(),
                     "Server",
                     source,
                     1.0,
                     identity.value(),
                     "https://dupedb.net",
                     "DupeDB",
                     findings,
                     init_J_List(findings)
                  );
                  init_V_Map_AutismDupeRadar_Ra(dedupedHits, match);
               }
            }
         }

         auditFlushIfCacheChanged();

         List<AutismDupeRadar.RadarMatch> matches = init_List_Map(dedupedHits);
         matches.sort(
            Comparator.comparingLong(AutismDupeRadar.RadarMatch::sortTimestampMs)
               .reversed()
               .thenComparingInt(matchx -> init_I_AutismDupeRadar_Ma(matchx.matchSource()))
               .thenComparing(AutismDupeRadar.RadarMatch::detectedPlugin, String.CASE_INSENSITIVE_ORDER)
         );
         String checkedLabel = plugins.isEmpty() ? "Checked server." : "Checked " + plugins.size() + " plugin" + (plugins.size() == 1 ? "." : "s.");
         String username = auditGetUsername();
         auditUpdateStateIfGeneration(
            generation,
            s -> s.withAuth(true, username)
               .withBusy(false, false, checkedLabel, null)
               .withMatches(matches, plugins.size(), false)
         );
      } catch (Exception ex) {
         auditUpdateStateIfGeneration(generation, s -> s.withBusy(false, false, "Radar check failed.", init_Str_Exception(ex)).markStale());
      }
   }

   private static boolean init_Z_List_Str(List<AutismDupeRadar.RadarFinding> findings, String plugin) {
      String want = bP.init_Str_Str(plugin);
      if (want.isBlank()) {
         return false;
      }

      for (AutismDupeRadar.RadarFinding finding : findings) {
         if (finding != null && want.equals(bP.init_Str_Str(finding.affectedPlugin()))) {
            return true;
         }
      }

      return false;
   }

   private static String init_Str_List_Str(List<AutismDupeRadar.RadarFinding> findings, String fallback) {
      for (AutismDupeRadar.RadarFinding finding : findings) {
         if (finding != null && finding.affectedPlugin() != null && !finding.affectedPlugin().isBlank()) {
            return finding.affectedPlugin();
         }
      }

      return fallback;
   }

   private static void init_V_Map_AutismDupeRadar_Ra(Map<String, AutismDupeRadar.RadarFindingHit> hits, AutismDupeRadar.RadarMatch match) {
      if (hits != null && match != null && match.findings() != null) {
         for (AutismDupeRadar.RadarFinding finding : match.findings()) {
            if (finding != null) {
               String key = init_Str_AutismDupeRadar_Ra(finding);
               if (!key.isBlank()) {
                  AutismDupeRadar.RadarFindingHit next = new AutismDupeRadar.RadarFindingHit(match, finding);
                  AutismDupeRadar.RadarFindingHit current = hits.get(key);
                  if (current == null || init_Z_AutismDupeRadar_Ra_AutismDupeRadar_Ra(next, current)) {
                     hits.put(key, next);
                  }
               }
            }
         }
      }
   }

   private static List<AutismDupeRadar.RadarMatch> init_List_Map(Map<String, AutismDupeRadar.RadarFindingHit> hits) {
      if (hits != null && !hits.isEmpty()) {
         Map<String, AutismDupeRadar.RadarMatchBuilder> builders = new LinkedHashMap<>();

         for (AutismDupeRadar.RadarFindingHit hit : hits.values()) {
            AutismDupeRadar.RadarMatch match = hit.match;
            String key = match.matchSource().name() + "|" + close(match.detectedPlugin()) + "|" + close(match.providerPlugin());
            AutismDupeRadar.RadarMatchBuilder builder = builders.computeIfAbsent(key, unused -> new AutismDupeRadar.RadarMatchBuilder(match));
            builder.findings.add(hit.finding);
         }

         List<AutismDupeRadar.RadarMatch> out = new ArrayList<>();

         for (AutismDupeRadar.RadarMatchBuilder builder : builders.values()) {
            builder.findings
               .sort(
                  Comparator.comparingLong(AutismDupeRadar.RadarFinding::sortTimestampMs)
                     .reversed()
                     .thenComparing(AutismDupeRadar.RadarFinding::title, String.CASE_INSENSITIVE_ORDER)
               );
            out.add(builder.toMatch());
         }

         return out;
      } else {
         return List.of();
      }
   }

   private static boolean init_Z_AutismDupeRadar_Ra_AutismDupeRadar_Ra(AutismDupeRadar.RadarFindingHit next, AutismDupeRadar.RadarFindingHit current) {
      int bySource = Integer.compare(init_I_AutismDupeRadar_Ma(next.match.matchSource()), init_I_AutismDupeRadar_Ma(current.match.matchSource()));
      if (bySource != 0) {
         return bySource < 0;
      }

      int byScore = Double.compare(next.match.matchScore(), current.match.matchScore());
      return byScore != 0 ? byScore > 0 : next.finding.sortTimestampMs() > current.finding.sortTimestampMs();
   }

   private static String init_Str_AutismDupeRadar_Ra(AutismDupeRadar.RadarFinding finding) {
      if (finding == null) {
         return "";
      } else if (finding.id() != null && !finding.id().isBlank()) {
         return "id:" + close(finding.id());
      } else {
         return finding.sourceUrl() != null && !finding.sourceUrl().isBlank()
            ? "url:" + finding.sourceUrl().trim().toLowerCase(Locale.ROOT)
            : "title:" + close(finding.title() + ":" + finding.status());
      }
   }

   private static long init_J_List(List<AutismDupeRadar.RadarFinding> findings) {
      long best = 0L;
      if (findings != null) {
         for (AutismDupeRadar.RadarFinding finding : findings) {
            if (finding != null) {
               best = Math.max(best, finding.sortTimestampMs());
            }
         }
      }

      return best == 0L ? System.currentTimeMillis() : best;
   }

   private static int init_I_AutismDupeRadar_Ma(AutismDupeRadar.MatchSource source) {
      byte var10000;
      switch (source == null ? AutismDupeRadar.MatchSource.PLUGIN_FUZZY : source) {
         case PLUGIN:
            var10000 = 0;
            break;
         case PLUGIN_FUZZY:
            var10000 = 1;
            break;
         case SERVER_DOMAIN:
            var10000 = 2;
            break;
         case SERVER_IP:
            var10000 = 3;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static String init_Str_Str(String plugin) {
      return "https://dupedb.net/api/exploits/search?scope=plugin&q="
         + push(plugin)
         + "&status=working,verified,patched,unverified&sort=date_submitted&order=desc&page=1&limit=50";
   }

   private static String load_Str(String host) {
      return "https://dupedb.net/api/exploits/search?scope=serverip&q="
         + push(host)
         + "&status=working,verified,patched,unverified&sort=date_submitted&order=desc&page=1&limit=50";
   }

   private static List<AutismDupeRadar.RadarFinding> init_List_Str_Z(String plugin, boolean forceRefresh) throws IOException {
      long now = System.currentTimeMillis();
      String key = close(plugin);
      synchronized (init_Obj) {
         AutismDupeRadar.CachedFindings cached = init_AutismDupeRadar_Pr.findings.get(key);
         if (!forceRefresh && cached != null && now - cached.updatedAt <= init_J && cached.items != null) {
            return List.copyOf(cached.items);
         }
      }

      String raw = update(init_Str_Str(plugin));
      List<AutismDupeRadar.RadarFinding> findings = init_List_JsonElement(JsonParser.parseString(raw), false, plugin);
      synchronized (init_Obj) {
         AutismDupeRadar.CachedFindings cached = new AutismDupeRadar.CachedFindings();
         cached.updatedAt = now;
         cached.items = new ArrayList<>(findings);
         init_AutismDupeRadar_Pr.findings.remove(key);
         init_AutismDupeRadar_Pr.findings.put(key, cached);
         auditPruneCache(init_AutismDupeRadar_Pr.findings);
         auditCacheChanged = true;
         return findings;
      }
   }

   private static List<AutismDupeRadar.RadarFinding> init_List_AutismDupeRadar_Ra_Z(AutismDupeRadar.RadarServerIdentity identity, boolean forceRefresh) {
      long now = System.currentTimeMillis();
      String key = (identity.ip() ? "ip:" : "domain:") + identity.value().toLowerCase(Locale.ROOT);
      synchronized (init_Obj) {
         AutismDupeRadar.CachedFindings cached = init_AutismDupeRadar_Pr.serverFindings.get(key);
         if (!forceRefresh && cached != null && now - cached.updatedAt <= init_J && cached.items != null) {
            return List.copyOf(cached.items);
         }
      }

      List<AutismDupeRadar.RadarFinding> findings;
      try {
         String raw = update(load_Str(identity.value()));
         findings = init_List_JsonElement(JsonParser.parseString(raw), true, identity.value());
      } catch (Exception ignored) {
         return List.of();
      }

      synchronized (init_Obj) {
         AutismDupeRadar.CachedFindings cached = new AutismDupeRadar.CachedFindings();
         cached.updatedAt = now;
         cached.items = new ArrayList<>(findings);
         init_AutismDupeRadar_Pr.serverFindings.remove(key);
         init_AutismDupeRadar_Pr.serverFindings.put(key, cached);
         auditPruneCache(init_AutismDupeRadar_Pr.serverFindings);
         auditCacheChanged = true;
         return findings;
      }
   }

   private static void auditPruneCache(Map<String, AutismDupeRadar.CachedFindings> map) {
      if (map == null) {
         return;
      }
      long now = System.currentTimeMillis();
      map.values().removeIf(v -> v == null || now - v.updatedAt > init_J * 4L);
      while (map.size() > AUDIT_MAX_CACHE_ENTRIES) {
         Iterator<String> it = map.keySet().iterator();
         if (!it.hasNext()) {
            break;
         }
         it.next();
         it.remove();
      }
   }

   private static void init_V_Map_List(Map<String, AutismDupeRadar.RadarFinding> unique, List<AutismDupeRadar.RadarFinding> findings) {
      if (unique != null && findings != null && !findings.isEmpty()) {
         for (AutismDupeRadar.RadarFinding finding : findings) {
            if (finding != null) {
               String key = init_Str_AutismDupeRadar_Ra(finding);
               if (key.isBlank()) {
                  key = close(finding.title() + ":" + finding.sourceUrl());
               }

               AutismDupeRadar.RadarFinding existing = unique.get(key);
               if (existing == null || finding.sortTimestampMs() > existing.sortTimestampMs()) {
                  unique.put(key, finding);
               }
            }
         }
      }
   }

   private static List<AutismDupeRadar.RadarFinding> init_List_JsonElement(JsonElement element, boolean serverScope, String queried) {
      JsonArray cards = init_JsonArray_JsonElement(element);
      Map<String, AutismDupeRadar.RadarFinding> unique = new LinkedHashMap<>();
      long fallbackTimestamp = System.currentTimeMillis();

      for (JsonElement el : cards) {
         if (el != null && el.isJsonObject()) {
            JsonObject object = el.getAsJsonObject();
            String id = init_Str_JsonObject_StrArr(object, "id");
            String title = init_Str_JsonObject_StrArr(object, "name");
            if (title != null && !title.isBlank()) {
               String status = load(object);
               String finalId = id != null && !id.isBlank() ? id.trim() : close(title + ":" + status);
               String affectedPlugin = serverScope ? queried : init_Str_JsonObject_Str(object, queried);
               long timestamp = init_J_JsonObject_J(object, fallbackTimestamp);
               String sourceUrl = init_Str_Str_Str(null, id != null && !id.isBlank() ? id : title);
               unique.putIfAbsent(
                  finalId,
                  new AutismDupeRadar.RadarFinding(
                     finalId, title.trim(), status, init_Str_JsonObject(object), affectedPlugin, sourceUrl, "DupeDB", timestamp, init_List_JsonObject(object)
                  )
               );
            }
         }
      }

      List<AutismDupeRadar.RadarFinding> out = new ArrayList<>(unique.values());
      out.sort(
         Comparator.comparingLong(AutismDupeRadar.RadarFinding::sortTimestampMs)
            .reversed()
            .thenComparing(AutismDupeRadar.RadarFinding::title, String.CASE_INSENSITIVE_ORDER)
      );
      return out;
   }

   private static JsonArray init_JsonArray_JsonElement(JsonElement element) {
      if (element == null || element.isJsonNull()) {
         return new JsonArray();
      }

      if (element.isJsonArray()) {
         return element.getAsJsonArray();
      }

      if (element.isJsonObject()) {
         JsonObject object = element.getAsJsonObject();

         for (String key : List.of("exploits", "results", "data", "items")) {
            JsonElement value = object.get(key);
            if (value != null && value.isJsonArray()) {
               return value.getAsJsonArray();
            }
         }
      }

      return new JsonArray();
   }

   private static String init_Str_JsonObject_Str(JsonObject object, String fallback) {
      JsonElement plugins = object.get("plugins");
      if (plugins != null && plugins.isJsonArray()) {
         for (JsonElement el : plugins.getAsJsonArray()) {
            if (el != null && !el.isJsonNull()) {
               if (el.isJsonObject()) {
                  String name = init_Str_JsonObject_StrArr(el.getAsJsonObject(), "name", "plugin", "pluginName");
                  if (name != null && !name.isBlank()) {
                     return name.trim();
                  }
               } else if (el.isJsonPrimitive()) {
                  String name = el.getAsString();
                  if (name != null && !name.isBlank()) {
                     return name.trim();
                  }
               }
            }
         }
      }

      String legacy = init_Str_JsonObject_StrArr(object, "plugin_name");
      return legacy != null && !legacy.isBlank() ? legacy.trim() : fallback;
   }

   private static List<String> init_List_JsonObject(JsonObject object) {
      JsonElement ips = object.get("server_ips");
      if (ips != null && ips.isJsonArray()) {
         List<String> out = new ArrayList<>();

         for (JsonElement el : ips.getAsJsonArray()) {
            if (el != null && el.isJsonPrimitive()) {
               String value = el.getAsString();
               if (value != null && !value.isBlank()) {
                  out.add(value.trim());
               }
            }
         }

         return out;
      } else {
         return List.of();
      }
   }

   private static String init_Str_JsonObject(JsonObject object) {
      List<String> parts = new ArrayList<>();
      String type = init_Str_JsonObject_StrArr(object, "type");
      if (type != null && !type.isBlank()) {
         parts.add(type.trim().toLowerCase(Locale.ROOT));
      }

      long upvotes = init_J_JsonObject_Str(object, "upvotes");
      if (upvotes > 0L) {
         parts.add(upvotes + "▲");
      }

      String version = init_Str_JsonElement(object.get("minecraft_versions"));
      if (!version.isBlank()) {
         parts.add(version);
      }

      return String.join(" · ", parts);
   }

   private static long init_J_JsonObject_Str(JsonObject object, String key) {
      JsonElement value = object.get(key);
      if (value != null && !value.isJsonNull() && value.isJsonPrimitive()) {
         try {
            return value.getAsLong();
         } catch (Exception ignored) {
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   private static String init_Str_JsonElement(JsonElement versions) {
      if (versions != null && versions.isJsonArray()) {
         String best = "";

         for (JsonElement el : versions.getAsJsonArray()) {
            if (el != null && el.isJsonPrimitive()) {
               String value = el.getAsString();
               if (value != null && !value.isBlank()) {
                  value = value.trim();
                  if (best.isBlank() || init_I_Str_Str(value, best) > 0) {
                     best = value;
                  }
               }
            }
         }

         return best;
      } else {
         return "";
      }
   }

   private static int init_I_Str_Str(String a, String b) {
      String[] pa = a.split("\\.");
      String[] pb = b.split("\\.");
      int n = Math.max(pa.length, pb.length);

      for (int i = 0; i < n; i++) {
         int va = i < pa.length ? load_I(pa[i]) : 0;
         int vb = i < pb.length ? load_I(pb[i]) : 0;
         if (va != vb) {
            return Integer.compare(va, vb);
         }
      }

      return 0;
   }

   private static int load_I(String value) {
      try {
         return Integer.parseInt(value.replaceAll("[^0-9].*$", ""));
      } catch (Exception ignored) {
         return 0;
      }
   }

   private static String init_Str_Str_Str(String rawUrl, String fallback) {
      String clean = rawUrl == null ? "" : rawUrl.trim();
      String fallbackText = fallback == null ? "" : fallback.trim();
      if (clean.isBlank()) {
         return init_Z_Str(fallbackText) ? "https://dupedb.net/exploit/" + pop(fallbackText) : "https://dupedb.net/?q=" + push(fallbackText);
      }

      String lower = clean.toLowerCase(Locale.ROOT);
      if (lower.startsWith("http://") || lower.startsWith("https://")) {
         try {
            URI uri = URI.create(clean);
            String host = uri.getHost();
            if (host == null || !"dupedb.net".equalsIgnoreCase(host) && !host.toLowerCase(Locale.ROOT).endsWith(".dupedb.net")) {
               return init_Z_Str(fallbackText) ? "https://dupedb.net/exploit/" + pop(fallbackText) : "https://dupedb.net/?q=" + push(fallbackText);
            }

            String path = uri.getPath() == null ? "" : uri.getPath();
            if (!path.startsWith("/exploit/") && !path.startsWith("/exploits/")) {
               return "https://dupedb.net/?q=" + push(fallbackText.isBlank() ? clean : fallbackText);
            }

            String id = path.substring(path.lastIndexOf(47) + 1);
            return "https://dupedb.net/exploit/" + pop(id);
         } catch (Exception ignored) {
            return init_Z_Str(fallbackText) ? "https://dupedb.net/exploit/" + pop(fallbackText) : "https://dupedb.net/?q=" + push(fallbackText);
         }
      } else if (lower.startsWith("/exploit/")
         || lower.startsWith("/exploits/")
         || lower.startsWith("https://dupedb.net/exploit/")
         || lower.startsWith("https://dupedb.net/exploits/")) {
         String id = clean.substring(clean.lastIndexOf(47) + 1);
         return "https://dupedb.net/exploit/" + pop(id);
      } else if (clean.startsWith("/")) {
         return "https://dupedb.net" + clean;
      } else if (init_Z_Str(clean)) {
         return "https://dupedb.net/exploit/" + pop(clean);
      } else {
         return init_Z_Str(fallbackText) ? "https://dupedb.net/exploit/" + pop(fallbackText) : "https://dupedb.net/?q=" + push(fallbackText);
      }
   }

   private static boolean init_Z_Str(String value) {
      return value != null && value.matches("[A-Za-z0-9_-]{8,64}") && !value.contains(" ");
   }

   private static AutismDupeRadar.TokenResponse init_AutismDupeRadar_To_Str(String code, String verifier, String redirectUri) throws IOException {
      String body = "grant_type=authorization_code&client_id="
         + push("catr-dupedb-radar")
         + "&code="
         + push(code)
         + "&code_verifier="
         + push(verifier)
         + "&redirect_uri="
         + push(redirectUri);
      String raw = read("https://dupedb.net/api/oauth/token", body);
      JsonObject object = JsonParser.parseString(raw).getAsJsonObject();
      String accessToken = init_Str_JsonObject_StrArr(object, "access_token", "accessToken", "token");
      if (accessToken != null && !accessToken.isBlank()) {
         return new AutismDupeRadar.TokenResponse(accessToken, init_Str_JsonObject_StrArr(object, "refresh_token", "refreshToken"));
      } else {
         throw new IOException("No access token returned.");
      }
   }

   private static AutismDupeRadar.TokenResponse init_AutismDupeRadar_To_Str(String refreshToken) throws IOException {
      if (refreshToken != null && !refreshToken.isBlank()) {
         String body = "grant_type=refresh_token&client_id=" + push("catr-dupedb-radar") + "&refresh_token=" + push(refreshToken);
         String raw = read("https://dupedb.net/api/oauth/token", body);
         JsonObject object = JsonParser.parseString(raw).getAsJsonObject();
         String accessToken = init_Str_JsonObject_StrArr(object, "access_token", "accessToken", "token");
         if (accessToken != null && !accessToken.isBlank()) {
            return new AutismDupeRadar.TokenResponse(accessToken, init_Str_JsonObject_StrArr(object, "refresh_token", "refreshToken"));
         } else {
            throw new IOException("No refreshed access token returned.");
         }
      } else {
         throw new IOException("No refresh token saved.");
      }
   }

   private static String read(String token) throws IOException {
      String raw = load("https://dupedb.net/api/oauth/userinfo", token);
      return write(raw);
   }

   private static String write_Str() throws IOException {
      String raw = update("https://dupedb.net/api/oauth/userinfo");
      return write(raw);
   }

   private static String write(String raw) {
      JsonElement element = JsonParser.parseString(raw);
      if (element.isJsonObject()) {
         String user = init_Str_JsonObject_StrArr(element.getAsJsonObject(), "username", "name", "display_name", "email");
         if (user != null && !user.isBlank()) {
            return user.trim();
         }
      }

      return "DupeDB user";
   }

   private static String update(String url) throws IOException {
      String token = sync_Str();
      if (token != null && !token.isBlank()) {
         try {
            return load(url, token);
         } catch (AutismDupeRadar.HttpStatusException ex) {
            if (!init_Z_I(ex.code())) {
               throw ex;
            } else if (!auditRefreshTokens(token)) {
               throw ex;
            } else {
               String refreshed = update_Str();
               if (refreshed != null && !refreshed.isBlank() && !Objects.equals(refreshed, token)) {
                  return load(url, refreshed);
               } else {
                  throw ex;
               }
            }
         }
      } else {
         throw new IOException("No DupeDB token saved.");
      }
   }

   private static String load(String url, String token) throws IOException, MalformedURLException, ProtocolException {
      HttpURLConnection connection = (HttpURLConnection)URI.create(url).toURL().openConnection();
      try {
         connection.setRequestMethod("GET");
         connection.setConnectTimeout(8000);
         connection.setReadTimeout(12000);
         connection.setRequestProperty("Accept", "application/json");
         connection.setRequestProperty("User-Agent", "AUTISM-Client-Radar");
         if (token != null && !token.isBlank()) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
         }

         return init_Str_HttpURLConnection(connection);
      } finally {
         connection.disconnect();
      }
   }

   private static String read(String url, String body) throws IOException, MalformedURLException, ProtocolException {
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      HttpURLConnection connection = (HttpURLConnection)URI.create(url).toURL().openConnection();
      try {
         connection.setRequestMethod("POST");
         connection.setConnectTimeout(8000);
         connection.setReadTimeout(12000);
         connection.setDoOutput(true);
         connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         connection.setRequestProperty("Accept", "application/json");
         connection.setRequestProperty("User-Agent", "AUTISM-Client-Radar");
         connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));

         try (OutputStream out = connection.getOutputStream()) {
            out.write(bytes);
         }

         return init_Str_HttpURLConnection(connection);
      } finally {
         connection.disconnect();
      }
   }

   private static String init_Str_HttpURLConnection(HttpURLConnection connection) throws IOException {
      int code = connection.getResponseCode();
      InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
      if (stream == null) {
         throw new IOException("HTTP " + code);
      }

      try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
         StringBuilder sb = new StringBuilder();
         char[] buffer = new char[2048];

         int read;
         while ((read = reader.read(buffer)) >= 0) {
            if (sb.length() + read > AUDIT_MAX_HTTP_CHARS) {
               throw new IOException("Response exceeded size limit");
            }
            sb.append(buffer, 0, read);
         }

         if (code < 200 || code >= 300) {
            throw new AutismDupeRadar.HttpStatusException(code, sb.toString());
         } else {
            return sb.toString();
         }
      }
   }

   private static String sync(String value) {
      if (value != null && !value.isBlank()) {
         byte[] sealed = autismclient.util.mm.a.DataUtil.init(value.getBytes(StandardCharsets.UTF_8));
         return sealed == null ? "" : Base64.getEncoder().encodeToString(sealed);
      } else {
         return "";
      }
   }

   private static String flush(String encoded) {
      if (encoded != null && !encoded.isBlank()) {
         try {
            byte[] plain = autismclient.util.mm.a.DataUtil.read(Base64.getDecoder().decode(encoded));
            return plain == null ? null : new String(plain, StandardCharsets.UTF_8);
         } catch (Throwable t) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static AutismDupeRadar.ProviderCache init_AutismDupeRadar_Pr() {
      try {
         if (Files.exists(init_Path)) {
            String text = Files.readString(init_Path, StandardCharsets.UTF_8);
            AutismDupeRadar.ProviderCache loaded = init_Gson.fromJson(text, AutismDupeRadar.ProviderCache.class);
            if (loaded != null) {
               loaded.ensure();
               loaded.accessToken = flush(loaded.encAccessToken);
               loaded.refreshToken = flush(loaded.encRefreshToken);
               if (loaded.accessToken == null || loaded.refreshToken == null) {
                  try {
                     JsonObject raw = JsonParser.parseString(text).getAsJsonObject();
                     if (loaded.accessToken == null) {
                        loaded.accessToken = init_Str_JsonObject_StrArr(raw, "accessToken", "access_token");
                     }

                     if (loaded.refreshToken == null) {
                        loaded.refreshToken = init_Str_JsonObject_StrArr(raw, "refreshToken", "refresh_token");
                     }
                  } catch (Exception var5) {
                  }
               }

               return loaded;
            }
         }
      } catch (Exception var6) {
      }

      try {
         Path oldTokenFile = AutismClientAddon.FOLDER.toPath().getParent().resolve("dupedb-token.json");
         if (Files.exists(oldTokenFile)) {
            JsonObject object = JsonParser.parseString(Files.readString(oldTokenFile, StandardCharsets.UTF_8)).getAsJsonObject();
            String token = init_Str_JsonObject_StrArr(object, "access_token", "accessToken", "token");
            if (token != null && !token.isBlank()) {
               AutismDupeRadar.ProviderCache migrated = new AutismDupeRadar.ProviderCache();
               migrated.ensure();
               migrated.accessToken = token;
               migrated.refreshToken = init_Str_JsonObject_StrArr(object, "refresh_token", "refreshToken");
               return migrated;
            }
         }
      } catch (Exception var4) {
      }

      AutismDupeRadar.ProviderCache fresh = new AutismDupeRadar.ProviderCache();
      fresh.ensure();
      return fresh;
   }

   // Callers hold init_Obj: serialize here, write the file on the persistence thread so the lock stays short.
   private static void flush() {
      auditCacheChanged = false;

      try {
         init_AutismDupeRadar_Pr.encAccessToken = sync(init_AutismDupeRadar_Pr.accessToken);
         init_AutismDupeRadar_Pr.encRefreshToken = sync(init_AutismDupeRadar_Pr.refreshToken);
         String json = init_Gson.toJson(init_AutismDupeRadar_Pr);
         dj.init("dupedb-cache", () -> auditWriteCache(json));
      } catch (Exception var5) {
      }
   }

   private static void auditFlushIfCacheChanged() {
      synchronized (init_Obj) {
         if (auditCacheChanged) {
            flush();
         }
      }
   }

   private static void auditWriteCache(String json) {
      try {
         Files.createDirectories(init_Path.getParent());
         Path tmp = init_Path.resolveSibling("dupedb-cache.json.tmp");
         Files.writeString(tmp, json, StandardCharsets.UTF_8);

         try {
            Files.move(tmp, init_Path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(tmp, init_Path, StandardCopyOption.REPLACE_EXISTING);
         }
      } catch (Exception var5) {
      }
   }

   private static String update_Str() {
      synchronized (init_Obj) {
         return init_AutismDupeRadar_Pr == null ? null : init_AutismDupeRadar_Pr.accessToken;
      }
   }

   private static String sync_Str() {
      synchronized (init_Obj) {
         if (init_AutismDupeRadar_Pr != null && init_AutismDupeRadar_Pr.accessToken != null && !init_AutismDupeRadar_Pr.accessToken.isBlank()) {
            return init_AutismDupeRadar_Pr.accessToken;
         }
      }

      return init_Z() ? update_Str() : null;
   }

   private static boolean init_Z() {
      return auditRefreshTokens(null);
   }

   private static boolean auditRefreshTokens(String failedAccessToken) {
      synchronized (AUDIT_REFRESH_LOCK) {
         String refreshToken;
         synchronized (init_Obj) {
            if (init_AutismDupeRadar_Pr == null) {
               return false;
            }
            if (failedAccessToken != null
               && init_AutismDupeRadar_Pr.accessToken != null
               && !init_AutismDupeRadar_Pr.accessToken.isBlank()
               && !Objects.equals(init_AutismDupeRadar_Pr.accessToken, failedAccessToken)) {
               return true;
            }
            refreshToken = init_AutismDupeRadar_Pr.refreshToken;
         }

         if (refreshToken != null && !refreshToken.isBlank()) {
            try {
               AutismDupeRadar.TokenResponse token = init_AutismDupeRadar_To_Str(refreshToken);
               String username;
               synchronized (init_Obj) {
                  if (!Objects.equals(init_AutismDupeRadar_Pr.refreshToken, refreshToken)) {
                     return init_AutismDupeRadar_Pr.accessToken != null && !init_AutismDupeRadar_Pr.accessToken.isBlank();
                  }
                  init_AutismDupeRadar_Pr.accessToken = token.accessToken;
                  if (token.refreshToken != null && !token.refreshToken.isBlank()) {
                     init_AutismDupeRadar_Pr.refreshToken = token.refreshToken;
                  }
                  username = init_AutismDupeRadar_Pr.username;
                  flush();
               }

               auditUpdateState(s -> s.withAuth(true, username));
               return true;
            } catch (Exception ex) {
               if (ex instanceof AutismDupeRadar.HttpStatusException http && init_Z_I(http.code())) {
                  load_V("DupeDB session expired.");
               }

               return false;
            }
         } else {
            return false;
         }
      }
   }

   private static void load_V(String status) {
      synchronized (init_Obj) {
         if (init_AutismDupeRadar_Pr != null) {
            init_AutismDupeRadar_Pr.accessToken = null;
            init_AutismDupeRadar_Pr.refreshToken = null;
            init_AutismDupeRadar_Pr.username = null;
            init_AutismDupeRadar_Pr.userUpdatedAt = 0L;
            flush();
         }
      }

      auditUpdateState(s -> s.withAuth(false, null).withBusy(false, false, status == null ? "Login required." : status, null).withMatches(List.of(), 0, false));
   }

   private static boolean init_Z_I(int code) {
      return code == 401 || code == 403;
   }

   private static String close(String value) {
      if (value == null) {
         return "";
      }

      String lower = value.trim().toLowerCase(Locale.ROOT);
      StringBuilder sb = new StringBuilder(lower.length());

      for (int i = 0; i < lower.length(); i++) {
         char c = lower.charAt(i);
         if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
            sb.append(c);
         }
      }

      return sb.toString();
   }

   private static String open(String value) {
      if (value != null && !value.isBlank()) {
         String lower = value.toLowerCase(Locale.ROOT);
         if (lower.contains("reject")) {
            return "Rejected";
         } else if (lower.contains("pending")) {
            return "Pending";
         } else if (lower.contains("unverif")) {
            return "Unverified";
         } else if (lower.contains("unpatch") || lower.contains("not_patched") || lower.contains("not patched")) {
            return "Unpatched";
         } else if (lower.contains("work")) {
            return "Working";
         } else if (lower.contains("patch")) {
            return "Patched";
         } else {
            return lower.contains("verif") ? "Verified" : AutismDupeRadar.UiTextShortener.titleCase(value.trim());
         }
      } else {
         return "Unknown";
      }
   }

   private static String load(JsonObject object) {
      String raw = open(init_Str_JsonObject_StrArr(object, "status"));
      if (!raw.equals("Unverified") && !raw.equals("Pending") && !raw.equals("Rejected")) {
         long patchedAt = load(object, "marked_patched_at");
         long workingAt = load(object, "marked_working_at");
         if (patchedAt > 0L && patchedAt >= workingAt) {
            return "Patched";
         } else {
            return workingAt > 0L ? "Working" : raw;
         }
      } else {
         return raw;
      }
   }

   private static long load(JsonObject object, String key) {
      return object != null && object.has(key) ? init_J_JsonElement(object.get(key)) : 0L;
   }

   public static int init_I_Str(String status) {
      String lower = status == null ? "" : status.toLowerCase(Locale.ROOT);
      if (lower.contains("work")) {
         return 0;
      } else if (lower.contains("verif")) {
         return 1;
      } else {
         return lower.contains("patch") ? 2 : 3;
      }
   }

   public static int init_I_List(List<AutismDupeRadar.RadarFinding> findings) {
      int best = 3;
      if (findings != null) {
         for (AutismDupeRadar.RadarFinding finding : findings) {
            best = Math.min(best, init_I_Str(finding.status()));
         }
      }

      return best;
   }

   public static String init_Str_List(List<AutismDupeRadar.RadarFinding> findings) {
      int rank = init_I_List(findings);
      String var10000;
      switch (rank) {
         case 0:
            var10000 = "Working";
            break;
         case 1:
            var10000 = "Verified";
            break;
         case 2:
            var10000 = "Patched";
            break;
         default:
            var10000 = "Unknown";
      }

      return var10000;
   }

   public static AutismDupeRadar.StatusCounts init_AutismDupeRadar_St_List(List<AutismDupeRadar.RadarFinding> findings) {
      int working = 0;
      int patched = 0;
      int verified = 0;
      if (findings != null) {
         for (AutismDupeRadar.RadarFinding finding : findings) {
            String lower = finding.status() == null ? "" : finding.status().toLowerCase(Locale.ROOT);
            if (lower.contains("work")) {
               working++;
            } else if (lower.contains("patch")) {
               patched++;
            } else if (lower.contains("verif")) {
               verified++;
            }
         }
      }

      return new AutismDupeRadar.StatusCounts(working, patched, verified);
   }

   private static String init_Str_JsonObject_StrArr(JsonObject object, String... keys) {
      if (object != null && keys != null) {
         for (String key : keys) {
            if (key != null && object.has(key)) {
               JsonElement value = object.get(key);
               if (value != null && value.isJsonPrimitive()) {
                  try {
                     String text = value.getAsString();
                     if (text != null && !text.isBlank()) {
                        return text;
                     }
                  } catch (Exception var8) {
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static long init_J_JsonObject_J(JsonObject object, long fallbackMs) {
      if (object == null) {
         return fallbackMs;
      }

      for (String key : List.of(
         "updated_at",
         "updatedAt",
         "published_at",
         "publishedAt",
         "submitted_at",
         "submittedAt",
         "date_submitted",
         "dateSubmitted",
         "created_at",
         "createdAt",
         "last_activity_at",
         "lastActivityAt",
         "marked_working_at",
         "markedWorkingAt",
         "marked_patched_at",
         "markedPatchedAt",
         "verified_at",
         "verifiedAt",
         "working_at",
         "patched_at",
         "sighting_created_at",
         "sightingCreatedAt",
         "sighting_updated_at",
         "sightingUpdatedAt",
         "sighting_date",
         "sightingDate"
      )) {
         if (object.has(key)) {
            JsonElement value = object.get(key);
            long parsed = init_J_JsonElement(value);
            if (parsed > 0L) {
               return parsed;
            }
         }
      }

      return fallbackMs;
   }

   private static long init_J_JsonElement(JsonElement value) {
      if (value != null && !value.isJsonNull() && value.isJsonPrimitive()) {
         try {
            if (value.getAsJsonPrimitive().isNumber()) {
               long raw = value.getAsLong();
               return raw < 10000000000L ? raw * 1000L : raw;
            }
         } catch (Exception var3) {
         }

         try {
            String text = value.getAsString();
            return text != null && !text.isBlank() && !"null".equalsIgnoreCase(text) ? Instant.parse(text.trim()).toEpochMilli() : 0L;
         } catch (Exception var4) {
            try {
               String text = value.getAsString();
               return text != null && !text.isBlank() ? OffsetDateTime.parse(text.trim()).toInstant().toEpochMilli() : 0L;
            } catch (Exception ignored) {
               return 0L;
            }
         }
      } else {
         return 0L;
      }
   }

   private static String init_Str_Exception(Exception ex) {
      if (ex == null) {
         return "Unknown error.";
      }

      String msg = ex.getMessage();
      if (msg == null || msg.isBlank()) {
         msg = ex.getClass().getSimpleName();
      }

      return AutismDupeRadar.UiTextShortener.shorten(msg, 220);
   }

   private static String init_Str_I(int bytes) {
      byte[] data = new byte[Math.max(16, bytes)];
      new SecureRandom().nextBytes(data);
      return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
   }

   private static String poll(String verifier) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest(verifier.getBytes(StandardCharsets.US_ASCII)));
      } catch (Exception ex) {
         return verifier;
      }
   }

   private static Map<String, String> init_Map_Str(String raw) {
      if (raw != null && !raw.isBlank()) {
         Map<String, String> out = new LinkedHashMap<>();

         for (String part : raw.split("&")) {
            int eq = part.indexOf(61);
            String key = eq >= 0 ? part.substring(0, eq) : part;
            String value = eq >= 0 ? part.substring(eq + 1) : "";
            out.put(URLDecoder.decode(key, StandardCharsets.UTF_8), URLDecoder.decode(value, StandardCharsets.UTF_8));
         }

         return out;
      } else {
         return Map.of();
      }
   }

   private static String push(String value) {
      return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
   }

   private static String pop(String value) {
      return push(value).replace("+", "%20");
   }

   private static final class CachedFindings {
      long updatedAt;
      List<AutismDupeRadar.RadarFinding> items = new ArrayList<>();
   }

   private static final class HttpStatusException extends IOException {
      private final int code;

      private HttpStatusException(int code, String body) {
         super("HTTP " + code + ": " + AutismDupeRadar.UiTextShortener.shorten(body == null ? "" : body, 180));
         this.code = code;
      }

      private int code() {
         return this.code;
      }
   }

   public enum MatchSource {
      PLUGIN("Plugin"),
      PLUGIN_FUZZY("Fuzzy"),
      SERVER_DOMAIN("Server"),
      SERVER_IP("IP");

      private final String label;

      MatchSource(String label) {
         this.label = label;
      }

      public String label() {
         return this.label;
      }
   }

   private static final class ProviderCache {
      transient String accessToken;
      transient String refreshToken;
      String encAccessToken;
      String encRefreshToken;
      String username;
      long userUpdatedAt;
      Map<String, AutismDupeRadar.CachedFindings> findings = new LinkedHashMap<>();
      Map<String, AutismDupeRadar.CachedFindings> serverFindings = new LinkedHashMap<>();
      int findingsVersion = 8;
      int serverFindingsVersion = 8;

      void ensure() {
         if (this.findings == null) {
            this.findings = new LinkedHashMap<>();
         }

         if (this.serverFindings == null) {
            this.serverFindings = new LinkedHashMap<>();
         }

         if (this.findingsVersion != 8) {
            this.findings = new LinkedHashMap<>();
            this.findingsVersion = 8;
         }

         if (this.serverFindingsVersion != 8) {
            this.serverFindings = new LinkedHashMap<>();
            this.serverFindingsVersion = 8;
         }
      }
   }

   public record RadarFinding(
      String id,
      String title,
      String status,
      String summary,
      String affectedPlugin,
      String sourceUrl,
      String provider,
      long sortTimestampMs,
      List<String> affectedServers
   ) {
      public RadarFinding {
         affectedServers = affectedServers == null ? List.of() : List.copyOf(affectedServers);
      }
   }

   private static final class RadarFindingHit {
      final AutismDupeRadar.RadarMatch match;
      final AutismDupeRadar.RadarFinding finding;

      RadarFindingHit(AutismDupeRadar.RadarMatch match, AutismDupeRadar.RadarFinding finding) {
         this.match = match;
         this.finding = finding;
      }
   }

   public record RadarMatch(
      String detectedPlugin,
      String providerPlugin,
      String matchConfidence,
      AutismDupeRadar.MatchSource matchSource,
      double matchScore,
      String matchedInput,
      String sourceUrl,
      String provider,
      List<AutismDupeRadar.RadarFinding> findings,
      long sortTimestampMs
   ) {
      public RadarMatch {
         detectedPlugin = detectedPlugin == null ? "" : detectedPlugin;
         providerPlugin = providerPlugin == null ? "" : providerPlugin;
         matchConfidence = matchConfidence == null ? "Unknown" : matchConfidence;
         matchSource = matchSource == null ? AutismDupeRadar.MatchSource.PLUGIN_FUZZY : matchSource;
         matchedInput = matchedInput == null ? "" : matchedInput;
         findings = findings == null ? List.of() : List.copyOf(findings);
      }

      public String displayLabel() {
         if (this.matchSource != AutismDupeRadar.MatchSource.SERVER_DOMAIN && this.matchSource != AutismDupeRadar.MatchSource.SERVER_IP) {
            return this.providerPlugin != null && !this.providerPlugin.isBlank() && !this.providerPlugin.equalsIgnoreCase(this.detectedPlugin)
               ? this.detectedPlugin + " -> " + this.providerPlugin
               : this.detectedPlugin;
         } else {
            return this.matchedInput != null && !this.matchedInput.isBlank() ? this.matchedInput : this.detectedPlugin;
         }
      }
   }

   private static final class RadarMatchBuilder {
      final AutismDupeRadar.RadarMatch prototype;
      final List<AutismDupeRadar.RadarFinding> findings = new ArrayList<>();

      RadarMatchBuilder(AutismDupeRadar.RadarMatch prototype) {
         this.prototype = prototype;
      }

      AutismDupeRadar.RadarMatch toMatch() {
         long newest = AutismDupeRadar.init_J_List(this.findings);
         return new AutismDupeRadar.RadarMatch(
            this.prototype.detectedPlugin(),
            this.prototype.providerPlugin(),
            this.prototype.matchConfidence(),
            this.prototype.matchSource(),
            this.prototype.matchScore(),
            this.prototype.matchedInput(),
            this.prototype.sourceUrl(),
            this.prototype.provider(),
            this.findings,
            newest
         );
      }
   }

   public record RadarPluginSnapshot(
      String displayName, String canonicalKey, String confidence, int commandCount, List<String> channels, List<String> guis, boolean feature
   ) {
      public RadarPluginSnapshot {
         displayName = displayName == null ? "" : displayName.trim();
         canonicalKey = canonicalKey == null ? AutismDupeRadar.close(displayName) : canonicalKey.trim();
         confidence = confidence == null ? "Unknown" : confidence.trim();
         channels = channels == null ? List.of() : List.copyOf(channels);
         guis = guis == null ? List.of() : List.copyOf(guis);
      }

      public boolean isPluginIdentity() {
         if (!this.feature && !this.displayName.isBlank()) {
            String lowerConfidence = this.confidence.toLowerCase(Locale.ROOT);
            return lowerConfidence.contains("exact") || lowerConfidence.contains("strong");
         } else {
            return false;
         }
      }
   }

   public record RadarServerIdentity(String value, boolean ip) {
      public RadarServerIdentity {
         value = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      }
   }

   public record RadarServerSnapshot(List<AutismDupeRadar.RadarServerIdentity> identities) {
      public static final AutismDupeRadar.RadarServerSnapshot EMPTY = new AutismDupeRadar.RadarServerSnapshot(List.of());

      public RadarServerSnapshot(List<AutismDupeRadar.RadarServerIdentity> identities) {
         if (identities == null) {
            identities = List.of();
         } else {
            Map<String, AutismDupeRadar.RadarServerIdentity> unique = new LinkedHashMap<>();

            for (AutismDupeRadar.RadarServerIdentity identity : identities) {
               if (identity != null && !identity.value().isBlank()) {
                  unique.putIfAbsent((identity.ip() ? "ip:" : "domain:") + identity.value(), identity);
               }
            }

            identities = List.copyOf(unique.values());
         }

         this.identities = identities;
      }
   }

   public record RadarState(
      String providerId,
      String providerLabel,
      boolean authenticated,
      boolean authenticating,
      boolean checking,
      String username,
      String status,
      String error,
      List<AutismDupeRadar.RadarMatch> matches,
      int detectedPluginCount,
      long updatedAtMs,
      boolean stale
   ) {
      public RadarState {
         matches = matches == null ? List.of() : List.copyOf(matches);
      }

      public AutismDupeRadar.RadarState withAuth(boolean authenticated, String username) {
         return new AutismDupeRadar.RadarState(
            this.providerId,
            this.providerLabel,
            authenticated,
            this.authenticating,
            this.checking,
            username,
            this.status,
            this.error,
            this.matches,
            this.detectedPluginCount,
            this.updatedAtMs,
            this.stale
         );
      }

      public AutismDupeRadar.RadarState withBusy(boolean authenticating, boolean checking, String status, String error) {
         return new AutismDupeRadar.RadarState(
            this.providerId,
            this.providerLabel,
            this.authenticated,
            authenticating,
            checking,
            this.username,
            status == null ? this.status : status,
            error,
            this.matches,
            this.detectedPluginCount,
            this.updatedAtMs,
            this.stale
         );
      }

      public AutismDupeRadar.RadarState withMatches(List<AutismDupeRadar.RadarMatch> matches, int detectedPluginCount, boolean stale) {
         return new AutismDupeRadar.RadarState(
            this.providerId,
            this.providerLabel,
            this.authenticated,
            this.authenticating,
            this.checking,
            this.username,
            this.status,
            this.error,
            matches,
            detectedPluginCount,
            System.currentTimeMillis(),
            stale
         );
      }

      public AutismDupeRadar.RadarState withPluginCount(int count) {
         return new AutismDupeRadar.RadarState(
            this.providerId,
            this.providerLabel,
            this.authenticated,
            this.authenticating,
            this.checking,
            this.username,
            this.status,
            this.error,
            this.matches,
            count,
            this.updatedAtMs,
            this.stale
         );
      }

      public AutismDupeRadar.RadarState markStale() {
         return new AutismDupeRadar.RadarState(
            this.providerId,
            this.providerLabel,
            this.authenticated,
            this.authenticating,
            this.checking,
            this.username,
            this.status,
            this.error,
            this.matches,
            this.detectedPluginCount,
            this.updatedAtMs,
            true
         );
      }
   }

   public record StatusCounts(int working, int patched, int verified) {
   }

   private record TokenResponse(String accessToken, String refreshToken) {
   }

   private static final class UiTextShortener {
      static String shorten(String value, int max) {
         if (value == null) {
            return "";
         }

         String clean = value.trim();
         return clean.length() <= max ? clean : clean.substring(0, Math.max(0, max - 3)).trim() + "...";
      }

      static String titleCase(String value) {
         if (value != null && !value.isBlank()) {
            String lower = value.trim().toLowerCase(Locale.ROOT).replace('_', ' ').replace('-', ' ');
            StringBuilder sb = new StringBuilder(lower.length());
            boolean upperNext = true;

            for (int i = 0; i < lower.length(); i++) {
               char c = lower.charAt(i);
               if (Character.isWhitespace(c)) {
                  upperNext = true;
                  sb.append(c);
               } else if (upperNext) {
                  sb.append(Character.toUpperCase(c));
                  upperNext = false;
               } else {
                  sb.append(c);
               }
            }

            return sb.toString();
         } else {
            return "";
         }
      }
   }
}
