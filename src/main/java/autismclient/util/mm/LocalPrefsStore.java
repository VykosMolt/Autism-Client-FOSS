package autismclient.util.mm;

import autismclient.AutismClientAddon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LocalPrefsStore {
   private static final File PREFS_FILE = new File(AutismClientAddon.FOLDER, "mm_prefs.properties");
   private static final File TEMP_FILE = new File(AutismClientAddon.FOLDER, "mm_prefs.properties.tmp");
   private static final int init_I = 4096;
   private static final long SAVE_DELAY_MS = 400L;
   private static volatile LocalPrefsStore instance;
   private static final ScheduledExecutorService SAVE_EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "mm-prefs-writer");
      t.setDaemon(true);
      return t;
   });
   private final AtomicBoolean dirty = new AtomicBoolean(false);
   private final AtomicBoolean saveScheduled = new AtomicBoolean(false);
   private volatile boolean shareServer;
   private volatile boolean shareLocation;
   private volatile boolean autoJoinPublic;
   private volatile boolean killSwitch;
   private volatile boolean chatMirror = true;
   private volatile boolean debugLog;
   private volatile int defaultMaxPlayers = 8;
   private volatile boolean defaultPublic = true;
   private volatile String discordSession = "";
   private volatile String discordName = "";
   private volatile String discordIdToken = "";
   private volatile String defaultLobbyName = "";
   private volatile String defaultServer = "";
   private volatile String defaultPlugins = "";
   private volatile int myDupeStatus = -1;
   private final Set<String> blocked = Collections.synchronizedSet(new LinkedHashSet<>());
   private final Set<String> trusted = Collections.synchronizedSet(new LinkedHashSet<>());
   private final Map<String, String> blockedDiscord = Collections.synchronizedMap(new LinkedHashMap<>());
   private final Map<String, String> bannedDiscord = Collections.synchronizedMap(new LinkedHashMap<>());
   private final Map<String, boolean[]> lobbyFlags = Collections.synchronizedMap(new LinkedHashMap<>());

   private LocalPrefsStore() {
   }

   public static LocalPrefsStore getInstance() {
      LocalPrefsStore local = instance;
      if (local == null) {
         synchronized (LocalPrefsStore.class) {
            if (instance == null) {
               instance = load();
            }

            local = instance;
         }
      }

      return local;
   }

   public boolean isShareServer() {
      return this.shareServer;
   }

   public void setShareServer(boolean v) {
      this.shareServer = v;
      this.scheduleSave();
   }

   public boolean isShareLocation() {
      return this.shareLocation;
   }

   public void setShareLocation(boolean v) {
      this.shareLocation = v;
      this.scheduleSave();
   }

   public boolean isAutoJoinPublic() {
      return this.autoJoinPublic;
   }

   public void setAutoJoinPublic(boolean v) {
      this.autoJoinPublic = v;
      this.scheduleSave();
   }

   public boolean init_Z_Str(String lobbyId) {
      return this.lobbyFlag(lobbyId, 0);
   }

   public boolean load_Z(String lobbyId) {
      return this.lobbyFlag(lobbyId, 1);
   }

   public void init_V_Str_Z(String lobbyId, boolean v) {
      this.setLobbyFlag(lobbyId, 0, v);
   }

   public void load_V_Str_Z(String lobbyId, boolean v) {
      this.setLobbyFlag(lobbyId, 1, v);
   }

   private boolean lobbyFlag(String lobbyId, int idx) {
      boolean[] cur = lobbyId == null ? null : this.lobbyFlags.get(lobbyId);
      return cur != null && cur[idx];
   }

   private void setLobbyFlag(String lobbyId, int idx, boolean v) {
      if (lobbyId != null && !lobbyId.isBlank() && lobbyId.length() <= 128) {
         synchronized (this.lobbyFlags) {
            boolean[] cur = this.lobbyFlags.get(lobbyId);
            if (cur == null) {
               if (!v) {
                  return;
               }

               if (this.lobbyFlags.size() >= 4096) {
                  return;
               }

               cur = new boolean[2];
               this.lobbyFlags.put(lobbyId, cur);
            }

            cur[idx] = v;
            if (!cur[0] && !cur[1]) {
               this.lobbyFlags.remove(lobbyId);
            }
         }

         this.scheduleSave();
      }
   }

   public boolean isKillSwitch() {
      return this.killSwitch;
   }

   public void setKillSwitch(boolean v) {
      this.killSwitch = v;
      this.scheduleSave();
   }

   public boolean isChatMirror() {
      return this.chatMirror;
   }

   public void setChatMirror(boolean v) {
      this.chatMirror = v;
      this.scheduleSave();
   }

   public boolean sync_Z() {
      return this.debugLog;
   }

   public void setDebugLog(boolean v) {
      this.debugLog = v;
      this.scheduleSave();
   }

   public int getDefaultMaxPlayers() {
      return this.defaultMaxPlayers;
   }

   public void setDefaultMaxPlayers(int v) {
      this.defaultMaxPlayers = v <= 0 ? 0 : Math.max(2, Math.min(500, v));
      this.scheduleSave();
   }

   public boolean isDefaultPublic() {
      return this.defaultPublic;
   }

   public void setDefaultPublic(boolean v) {
      this.defaultPublic = v;
      this.scheduleSave();
   }

   public String getDefaultLobbyName() {
      return this.defaultLobbyName;
   }

   public void setDefaultLobbyName(String v) {
      this.defaultLobbyName = v == null ? "" : v;
      this.scheduleSave();
   }

   public String getDefaultServer() {
      return this.defaultServer;
   }

   public void setDefaultServer(String v) {
      this.defaultServer = v == null ? "" : v;
      this.scheduleSave();
   }

   public String getDefaultPlugins() {
      return this.defaultPlugins;
   }

   public void setDefaultPlugins(String v) {
      this.defaultPlugins = v == null ? "" : v;
      this.scheduleSave();
   }

   public int getMyDupeStatus() {
      return this.myDupeStatus;
   }

   public void setMyDupeStatus(int v) {
      this.myDupeStatus = v >= 0 && v <= 1 ? v : -1;
      this.scheduleSave();
   }

   public String sealedSession() {
      return this.discordSession;
   }

   public String accountName() {
      return this.discordName;
   }

   public String idToken() {
      return this.discordIdToken;
   }

   public void saveSession(String sealedSession, String name, String idToken) {
      this.discordSession = sealedSession == null ? "" : sealedSession;
      this.discordName = name == null ? "" : name;
      this.discordIdToken = idToken == null ? "" : idToken;
      this.scheduleSave();
   }

   public void setIdToken(String idToken) {
      this.discordIdToken = idToken == null ? "" : idToken;
      this.scheduleSave();
   }

   public void setSealedSession(String sealedSession) {
      this.discordSession = sealedSession == null ? "" : sealedSession;
      this.scheduleSave();
   }

   public void clearSession() {
      this.discordSession = "";
      this.discordName = "";
      this.discordIdToken = "";
      this.scheduleSave();
   }

   public boolean isBlocked(String fpHex) {
      return this.blocked.contains(fpHex);
   }

   public void block(String fpHex) {
      if (isValidFingerprint(fpHex)) {
         addBounded(this.blocked, fpHex);
         this.trusted.remove(fpHex);
         this.scheduleSave();
      }
   }

   public void unblock(String fpHex) {
      if (this.blocked.remove(fpHex)) {
         this.scheduleSave();
      }
   }

   public boolean isTrusted(String fpHex) {
      return this.trusted.contains(fpHex);
   }

   public void trust(String fpHex) {
      if (isValidFingerprint(fpHex)) {
         addBounded(this.trusted, fpHex);
         this.blocked.remove(fpHex);
         this.scheduleSave();
      }
   }

   public void untrust(String fpHex) {
      if (this.trusted.remove(fpHex)) {
         this.scheduleSave();
      }
   }

   public Set<String> blockedSnapshot() {
      synchronized (this.blocked) {
         return new LinkedHashSet<>(this.blocked);
      }
   }

   public boolean isDiscordBlocked(String id) {
      return id != null && this.blockedDiscord.containsKey(id);
   }

   public void blockDiscord(String id, String name) {
      if (isValidDiscordId(id)) {
         putName(this.blockedDiscord, id, name);
         this.scheduleSave();
      }
   }

   public void unblockDiscord(String id) {
      if (this.blockedDiscord.remove(id) != null) {
         this.scheduleSave();
      }
   }

   public boolean isDiscordBanned(String id) {
      return id != null && this.bannedDiscord.containsKey(id);
   }

   public void banDiscord(String id, String name) {
      if (isValidDiscordId(id)) {
         putName(this.bannedDiscord, id, name);
         this.scheduleSave();
      }
   }

   public void unbanDiscord(String id) {
      if (this.bannedDiscord.remove(id) != null) {
         this.scheduleSave();
      }
   }

   public Map<String, String> blockedDiscordSnapshot() {
      synchronized (this.blockedDiscord) {
         return new LinkedHashMap<>(this.blockedDiscord);
      }
   }

   public Map<String, String> bannedDiscordSnapshot() {
      synchronized (this.bannedDiscord) {
         return new LinkedHashMap<>(this.bannedDiscord);
      }
   }

   public void refreshBlockedName(String id, String name) {
      if (isValidDiscordId(id) && name != null && !name.isBlank()) {
         String clean = sanitizeName(name);
         if (!clean.isEmpty()) {
            boolean changed = false;
            synchronized (this.blockedDiscord) {
               if (this.blockedDiscord.containsKey(id) && !clean.equals(this.blockedDiscord.get(id))) {
                  this.blockedDiscord.put(id, clean);
                  changed = true;
               }
            }

            synchronized (this.bannedDiscord) {
               if (this.bannedDiscord.containsKey(id) && !clean.equals(this.bannedDiscord.get(id))) {
                  this.bannedDiscord.put(id, clean);
                  changed = true;
               }
            }

            if (changed) {
               this.scheduleSave();
            }
         }
      }
   }

   private static LocalPrefsStore load() {
      LocalPrefsStore p = new LocalPrefsStore();
      if (PREFS_FILE.exists()) {
         try (FileInputStream in = new FileInputStream(PREFS_FILE)) {
            Properties props = new Properties();
            props.load(in);
            p.shareServer = readBool(props, "shareServer");
            p.shareLocation = readBool(props, "shareLocation");
            p.autoJoinPublic = readBool(props, "autoJoinPublic");
            p.killSwitch = readBool(props, "killSwitch");
            p.chatMirror = readBool(props, "chatMirror", true);
            p.debugLog = readBool(props, "debugLog");
            p.defaultPublic = readBool(props, "defaultPublic", true);

            try {
               p.defaultMaxPlayers = Integer.parseInt(props.getProperty("defaultMaxPlayers", "8"));
            } catch (NumberFormatException var6) {
            }

            p.defaultMaxPlayers = p.defaultMaxPlayers <= 0 ? 0 : Math.max(2, Math.min(500, p.defaultMaxPlayers));
            p.defaultLobbyName = props.getProperty("defaultLobbyName", "");
            p.defaultServer = props.getProperty("defaultServer", "");
            p.defaultPlugins = props.getProperty("defaultPlugins", "");
            p.discordSession = props.getProperty("discordSession", "");
            p.discordName = props.getProperty("discordName", "");
            p.discordIdToken = props.getProperty("discordIdToken", "");

            try {
               p.myDupeStatus = Integer.parseInt(props.getProperty("myDupeStatus", "-1"));
            } catch (NumberFormatException var5) {
            }

            parseSet(p.blocked, props.getProperty("blocked", ""));
            parseSet(p.trusted, props.getProperty("trusted", ""));
            parseMap(p.blockedDiscord, props.getProperty("blockedDiscord", ""));
            parseMap(p.bannedDiscord, props.getProperty("bannedDiscord", ""));
            readLobbyFlags(p.lobbyFlags, props);
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("Failed to read Matchmaking prefs", t);
         }
      }

      Runtime.getRuntime().addShutdownHook(new Thread(p::flushIfDirty, "mm-prefs-flush"));
      return p;
   }

   private void scheduleSave() {
      this.dirty.set(true);
      if (this.saveScheduled.compareAndSet(false, true)) {
         try {
            SAVE_EXECUTOR.schedule(() -> {
               this.saveScheduled.set(false);
               this.flushIfDirty();
            }, 400L, TimeUnit.MILLISECONDS);
         } catch (Throwable t) {
            this.saveScheduled.set(false);
            this.flushIfDirty();
         }
      }
   }

   private void flushIfDirty() {
      if (this.dirty.compareAndSet(true, false)) {
         this.write_V();
      }
   }

   private synchronized void write_V() {
      try {
         File dir = PREFS_FILE.getParentFile();
         if (dir != null) {
            dir.mkdirs();
         }

         Properties props = new Properties();
         props.setProperty("shareServer", Boolean.toString(this.shareServer));
         props.setProperty("shareLocation", Boolean.toString(this.shareLocation));
         props.setProperty("autoJoinPublic", Boolean.toString(this.autoJoinPublic));
         props.setProperty("killSwitch", Boolean.toString(this.killSwitch));
         props.setProperty("chatMirror", Boolean.toString(this.chatMirror));
         props.setProperty("debugLog", Boolean.toString(this.debugLog));
         props.setProperty("defaultPublic", Boolean.toString(this.defaultPublic));
         props.setProperty("defaultMaxPlayers", Integer.toString(this.defaultMaxPlayers));
         props.setProperty("defaultLobbyName", this.defaultLobbyName);
         props.setProperty("defaultServer", this.defaultServer);
         props.setProperty("defaultPlugins", this.defaultPlugins);
         props.setProperty("discordSession", this.discordSession);
         props.setProperty("discordName", this.discordName);
         props.setProperty("discordIdToken", this.discordIdToken);
         props.setProperty("myDupeStatus", Integer.toString(this.myDupeStatus));
         props.setProperty("blocked", String.join(",", this.blockedSnapshot()));
         synchronized (this.trusted) {
            props.setProperty("trusted", String.join(",", this.trusted));
         }

         props.setProperty("blockedDiscord", joinMap(this.blockedDiscord));
         props.setProperty("bannedDiscord", joinMap(this.bannedDiscord));
         writeLobbyFlags(this.lobbyFlags, props);

         // Holds the Discord ID token, so owner-only.
         autismclient.util.mm.a.DataUtil.auditPrivateFile(TEMP_FILE);
         try (FileOutputStream out = new FileOutputStream(TEMP_FILE)) {
            props.store(out, "Autism Matchmaking preferences (passphrases are never stored)");
         }

         try {
            Files.move(TEMP_FILE.toPath(), PREFS_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException notAtomic) {
            Files.move(TEMP_FILE.toPath(), PREFS_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
         }
      } catch (Throwable t) {
         try {
            Files.deleteIfExists(TEMP_FILE.toPath());
         } catch (Throwable ignored) {
         }
         AutismClientAddon.LOG.warn("Failed to save Matchmaking prefs", t);
      }
   }

   private static boolean readBool(Properties p, String key) {
      return readBool(p, key, false);
   }

   private static boolean readBool(Properties p, String key, boolean def) {
      return Boolean.parseBoolean(p.getProperty(key, Boolean.toString(def)));
   }

   private static void parseSet(Set<String> set, String csv) {
      if (csv != null && !csv.isBlank()) {
         for (String s : csv.split(",")) {
            if (isValidFingerprint(s)) {
               addBounded(set, s.trim());
            }
         }
      }
   }

   private static void readLobbyFlags(Map<String, boolean[]> out, Properties props) {
      for (String key : props.stringPropertyNames()) {
         if (key.startsWith("lobby.")) {
            String rest = key.substring("lobby.".length());
            int idx = rest.endsWith(".shareServer") ? 0 : (rest.endsWith(".shareLocation") ? 1 : -1);
            if (idx >= 0 && Boolean.parseBoolean(props.getProperty(key))) {
               String id = rest.substring(0, rest.length() - (idx == 0 ? ".shareServer" : ".shareLocation").length());
               if (!id.isBlank() && id.length() <= 128) {
                  synchronized (out) {
                     if (out.size() < 4096 || out.containsKey(id)) {
                        boolean[] cur = out.get(id);
                        if (cur == null) {
                           cur = new boolean[2];
                           out.put(id, cur);
                        }

                        cur[idx] = true;
                     }
                  }
               }
            }
         }
      }
   }

   private static void writeLobbyFlags(Map<String, boolean[]> map, Properties props) {
      synchronized (map) {
         for (Entry<String, boolean[]> e : map.entrySet()) {
            if (e.getValue()[0]) {
               props.setProperty("lobby." + e.getKey() + ".shareServer", "true");
            }

            if (e.getValue()[1]) {
               props.setProperty("lobby." + e.getKey() + ".shareLocation", "true");
            }
         }
      }
   }

   private static void parseMap(Map<String, String> map, String csv) {
      if (csv != null && !csv.isBlank()) {
         for (String entry : csv.split(",")) {
            int eq = entry.indexOf(61);
            String id = (eq >= 0 ? entry.substring(0, eq) : entry).trim();
            String name = eq >= 0 ? entry.substring(eq + 1).trim() : "";
            if (isValidDiscordId(id)) {
               putName(map, id, name);
            }
         }
      }
   }

   private static String joinMap(Map<String, String> map) {
      StringBuilder sb = new StringBuilder();
      synchronized (map) {
         for (Entry<String, String> e : map.entrySet()) {
            if (sb.length() > 0) {
               sb.append(',');
            }

            sb.append(e.getKey()).append('=').append(sanitizeName(e.getValue()));
         }
      }

      return sb.toString();
   }

   private static String sanitizeName(String n) {
      return n == null ? "" : n.replace(",", "").replace("=", "").trim();
   }

   private static void putName(Map<String, String> map, String id, String name) {
      synchronized (map) {
         map.put(id, sanitizeName(name));

         while (map.size() > 4096) {
            Iterator<String> it = map.keySet().iterator();
            if (!it.hasNext()) {
               break;
            }

            it.next();
            it.remove();
         }
      }
   }

   private static void addBounded(Set<String> set, String value) {
      synchronized (set) {
         set.add(value);

         while (set.size() > 4096) {
            Iterator<String> it = set.iterator();
            if (!it.hasNext()) {
               break;
            }

            it.next();
            it.remove();
         }
      }
   }

   private static boolean isValidFingerprint(String s) {
      if (s == null) {
         return false;
      }

      String t = s.trim();
      if (!t.isEmpty() && t.length() <= 64) {
         for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            boolean hex = c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
            if (!hex) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean isValidDiscordId(String s) {
      if (s == null) {
         return false;
      }

      String t = s.trim();
      if (!t.isEmpty() && t.length() <= 32) {
         for (int i = 0; i < t.length(); i++) {
            if (t.charAt(i) < '0' || t.charAt(i) > '9') {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
