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

public final class EventBus {
   private static final File init_File = new File(AutismClientAddon.FOLDER, "mm_prefs.properties");
   private static final File load_File = new File(AutismClientAddon.FOLDER, "mm_prefs.properties.tmp");
   private static final int init_I = 4096;
   private static final long init_J = 400L;
   private static volatile EventBus init_EventBus;
   private static final ScheduledExecutorService init_ScheduledExecutorS = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "mm-prefs-writer");
      t.setDaemon(true);
      return t;
   });
   private final AtomicBoolean init_AtomicBoolean = new AtomicBoolean(false);
   private final AtomicBoolean load_AtomicBoolean = new AtomicBoolean(false);
   private volatile boolean init_Z;
   private volatile boolean load_Z;
   private volatile boolean read_Z;
   private volatile boolean write_Z;
   private volatile boolean update_Z = true;
   private volatile boolean sync_Z;
   private volatile int load_I = 8;
   private volatile boolean flush = true;
   private volatile String init_Str = "";
   private volatile String load_Str = "";
   private volatile String read_Str = "";
   private volatile String write_Str = "";
   private volatile String update_Str = "";
   private volatile String sync_Str = "";
   private volatile int read_I = -1;
   private final Set<String> init_Set = Collections.synchronizedSet(new LinkedHashSet<>());
   private final Set<String> load_Set = Collections.synchronizedSet(new LinkedHashSet<>());
   private final Map<String, String> init_Map = Collections.synchronizedMap(new LinkedHashMap<>());
   private final Map<String, String> load_Map = Collections.synchronizedMap(new LinkedHashMap<>());
   private final Map<String, boolean[]> read_Map = Collections.synchronizedMap(new LinkedHashMap<>());

   private EventBus() {
   }

   public static EventBus init_EventBus() {
      EventBus local = init_EventBus;
      if (local == null) {
         synchronized (EventBus.class) {
            if (init_EventBus == null) {
               init_EventBus = load_EventBus();
            }

            local = init_EventBus;
         }
      }

      return local;
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public void init_V_Z(boolean v) {
      this.init_Z = v;
      this.load_V();
   }

   public boolean load_Z() {
      return this.load_Z;
   }

   public void load(boolean v) {
      this.load_Z = v;
      this.load_V();
   }

   public boolean read_Z() {
      return this.read_Z;
   }

   public void read(boolean v) {
      this.read_Z = v;
      this.load_V();
   }

   public boolean init_Z_Str(String lobbyId) {
      return this.init_Z_Str_I(lobbyId, 0);
   }

   public boolean load_Z(String lobbyId) {
      return this.init_Z_Str_I(lobbyId, 1);
   }

   public void init_V_Str_Z(String lobbyId, boolean v) {
      this.init_V_Str(lobbyId, 0, v);
   }

   public void load_V_Str_Z(String lobbyId, boolean v) {
      this.init_V_Str(lobbyId, 1, v);
   }

   private boolean init_Z_Str_I(String lobbyId, int idx) {
      boolean[] cur = lobbyId == null ? null : this.read_Map.get(lobbyId);
      return cur != null && cur[idx];
   }

   private void init_V_Str(String lobbyId, int idx, boolean v) {
      if (lobbyId != null && !lobbyId.isBlank() && lobbyId.length() <= 128) {
         synchronized (this.read_Map) {
            boolean[] cur = this.read_Map.get(lobbyId);
            if (cur == null) {
               if (!v) {
                  return;
               }

               if (this.read_Map.size() >= 4096) {
                  return;
               }

               cur = new boolean[2];
               this.read_Map.put(lobbyId, cur);
            }

            cur[idx] = v;
            if (!cur[0] && !cur[1]) {
               this.read_Map.remove(lobbyId);
            }
         }

         this.load_V();
      }
   }

   public boolean write_Z() {
      return this.write_Z;
   }

   public void write(boolean v) {
      this.write_Z = v;
      this.load_V();
   }

   public boolean update_Z() {
      return this.update_Z;
   }

   public void update(boolean v) {
      this.update_Z = v;
      this.load_V();
   }

   public boolean sync_Z() {
      return this.sync_Z;
   }

   public void sync(boolean v) {
      this.sync_Z = v;
      this.load_V();
   }

   public int init_I() {
      return this.load_I;
   }

   public void init_V_I(int v) {
      this.load_I = v <= 0 ? 0 : Math.max(2, Math.min(500, v));
      this.load_V();
   }

   public boolean flush() {
      return this.flush;
   }

   public void flush_V_Z(boolean v) {
      this.flush = v;
      this.load_V();
   }

   public String init_Str() {
      return this.write_Str;
   }

   public void init_V_Str(String v) {
      this.write_Str = v == null ? "" : v;
      this.load_V();
   }

   public String load_Str() {
      return this.update_Str;
   }

   public void load_V(String v) {
      this.update_Str = v == null ? "" : v;
      this.load_V();
   }

   public String read_Str() {
      return this.sync_Str;
   }

   public void read_V(String v) {
      this.sync_Str = v == null ? "" : v;
      this.load_V();
   }

   public int load_I() {
      return this.read_I;
   }

   public void load(int v) {
      this.read_I = v >= 0 && v <= 1 ? v : -1;
      this.load_V();
   }

   public String write_Str() {
      return this.init_Str;
   }

   public String update_Str() {
      return this.load_Str;
   }

   public String sync_Str() {
      return this.read_Str;
   }

   public void init_V_Str_2(String sealedSession, String name, String idToken) {
      this.init_Str = sealedSession == null ? "" : sealedSession;
      this.load_Str = name == null ? "" : name;
      this.read_Str = idToken == null ? "" : idToken;
      this.load_V();
   }

   public void write_V(String idToken) {
      this.read_Str = idToken == null ? "" : idToken;
      this.load_V();
   }

   public void update_V(String sealedSession) {
      this.init_Str = sealedSession == null ? "" : sealedSession;
      this.load_V();
   }

   public void init_V() {
      this.init_Str = "";
      this.load_Str = "";
      this.read_Str = "";
      this.load_V();
   }

   public boolean read_Z(String fpHex) {
      return this.init_Set.contains(fpHex);
   }

   public void sync_V(String fpHex) {
      if (flush_Z_Str(fpHex)) {
         load_V_Set_Str(this.init_Set, fpHex);
         this.load_Set.remove(fpHex);
         this.load_V();
      }
   }

   public void flush_V_Str(String fpHex) {
      if (this.init_Set.remove(fpHex)) {
         this.load_V();
      }
   }

   public boolean write_Z(String fpHex) {
      return this.load_Set.contains(fpHex);
   }

   public void close_V_Str(String fpHex) {
      if (flush_Z_Str(fpHex)) {
         load_V_Set_Str(this.load_Set, fpHex);
         this.init_Set.remove(fpHex);
         this.load_V();
      }
   }

   public void open(String fpHex) {
      if (this.load_Set.remove(fpHex)) {
         this.load_V();
      }
   }

   public Set<String> init_Set() {
      synchronized (this.init_Set) {
         return new LinkedHashSet<>(this.init_Set);
      }
   }

   public boolean update_Z(String id) {
      return id != null && this.init_Map.containsKey(id);
   }

   public void init_V_Str_Str(String id, String name) {
      if (close_Z_Str(id)) {
         init_V_Map(this.init_Map, id, name);
         this.load_V();
      }
   }

   public void poll(String id) {
      if (this.init_Map.remove(id) != null) {
         this.load_V();
      }
   }

   public boolean sync_Z(String id) {
      return id != null && this.load_Map.containsKey(id);
   }

   public void load_V_Str_Str(String id, String name) {
      if (close_Z_Str(id)) {
         init_V_Map(this.load_Map, id, name);
         this.load_V();
      }
   }

   public void push(String id) {
      if (this.load_Map.remove(id) != null) {
         this.load_V();
      }
   }

   public Map<String, String> init_Map() {
      synchronized (this.init_Map) {
         return new LinkedHashMap<>(this.init_Map);
      }
   }

   public Map<String, String> load_Map() {
      synchronized (this.load_Map) {
         return new LinkedHashMap<>(this.load_Map);
      }
   }

   public void read(String id, String name) {
      if (close_Z_Str(id) && name != null && !name.isBlank()) {
         String clean = init_Str_Str(name);
         if (!clean.isEmpty()) {
            boolean changed = false;
            synchronized (this.init_Map) {
               if (this.init_Map.containsKey(id) && !clean.equals(this.init_Map.get(id))) {
                  this.init_Map.put(id, clean);
                  changed = true;
               }
            }

            synchronized (this.load_Map) {
               if (this.load_Map.containsKey(id) && !clean.equals(this.load_Map.get(id))) {
                  this.load_Map.put(id, clean);
                  changed = true;
               }
            }

            if (changed) {
               this.load_V();
            }
         }
      }
   }

   private static EventBus load_EventBus() {
      EventBus p = new EventBus();
      if (init_File.exists()) {
         try (FileInputStream in = new FileInputStream(init_File)) {
            Properties props = new Properties();
            props.load(in);
            p.init_Z = init_Z_Properties_Str(props, "shareServer");
            p.load_Z = init_Z_Properties_Str(props, "shareLocation");
            p.read_Z = init_Z_Properties_Str(props, "autoJoinPublic");
            p.write_Z = init_Z_Properties_Str(props, "killSwitch");
            p.update_Z = init_Z_Properties(props, "chatMirror", true);
            p.sync_Z = init_Z_Properties_Str(props, "debugLog");
            p.flush = init_Z_Properties(props, "defaultPublic", true);

            try {
               p.load_I = Integer.parseInt(props.getProperty("defaultMaxPlayers", "8"));
            } catch (NumberFormatException var6) {
            }

            p.load_I = p.load_I <= 0 ? 0 : Math.max(2, Math.min(500, p.load_I));
            p.write_Str = props.getProperty("defaultLobbyName", "");
            p.update_Str = props.getProperty("defaultServer", "");
            p.sync_Str = props.getProperty("defaultPlugins", "");
            p.init_Str = props.getProperty("discordSession", "");
            p.load_Str = props.getProperty("discordName", "");
            p.read_Str = props.getProperty("discordIdToken", "");

            try {
               p.read_I = Integer.parseInt(props.getProperty("myDupeStatus", "-1"));
            } catch (NumberFormatException var5) {
            }

            init_V_Set_Str(p.init_Set, props.getProperty("blocked", ""));
            init_V_Set_Str(p.load_Set, props.getProperty("trusted", ""));
            init_V_Map_Str(p.init_Map, props.getProperty("blockedDiscord", ""));
            init_V_Map_Str(p.load_Map, props.getProperty("bannedDiscord", ""));
            init_V_Map_Properties(p.read_Map, props);
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("Failed to read Matchmaking prefs", t);
         }
      }

      Runtime.getRuntime().addShutdownHook(new Thread(p::read_V, "mm-prefs-flush"));
      return p;
   }

   private void load_V() {
      this.init_AtomicBoolean.set(true);
      if (this.load_AtomicBoolean.compareAndSet(false, true)) {
         try {
            init_ScheduledExecutorS.schedule(() -> {
               this.load_AtomicBoolean.set(false);
               this.read_V();
            }, 400L, TimeUnit.MILLISECONDS);
         } catch (Throwable t) {
            this.load_AtomicBoolean.set(false);
            this.read_V();
         }
      }
   }

   private void read_V() {
      if (this.init_AtomicBoolean.compareAndSet(true, false)) {
         this.write_V();
      }
   }

   private synchronized void write_V() {
      try {
         File dir = init_File.getParentFile();
         if (dir != null) {
            dir.mkdirs();
         }

         Properties props = new Properties();
         props.setProperty("shareServer", Boolean.toString(this.init_Z));
         props.setProperty("shareLocation", Boolean.toString(this.load_Z));
         props.setProperty("autoJoinPublic", Boolean.toString(this.read_Z));
         props.setProperty("killSwitch", Boolean.toString(this.write_Z));
         props.setProperty("chatMirror", Boolean.toString(this.update_Z));
         props.setProperty("debugLog", Boolean.toString(this.sync_Z));
         props.setProperty("defaultPublic", Boolean.toString(this.flush));
         props.setProperty("defaultMaxPlayers", Integer.toString(this.load_I));
         props.setProperty("defaultLobbyName", this.write_Str);
         props.setProperty("defaultServer", this.update_Str);
         props.setProperty("defaultPlugins", this.sync_Str);
         props.setProperty("discordSession", this.init_Str);
         props.setProperty("discordName", this.load_Str);
         props.setProperty("discordIdToken", this.read_Str);
         props.setProperty("myDupeStatus", Integer.toString(this.read_I));
         props.setProperty("blocked", String.join(",", this.init_Set()));
         synchronized (this.load_Set) {
            props.setProperty("trusted", String.join(",", this.load_Set));
         }

         props.setProperty("blockedDiscord", init_Str_Map(this.init_Map));
         props.setProperty("bannedDiscord", init_Str_Map(this.load_Map));
         load_V_Map_Properties(this.read_Map, props);

         // Holds the Discord ID token, so owner-only.
         autismclient.util.mm.a.DataUtil.auditPrivateFile(load_File);
         try (FileOutputStream out = new FileOutputStream(load_File)) {
            props.store(out, "Autism Matchmaking preferences (passphrases are never stored)");
         }

         try {
            Files.move(load_File.toPath(), init_File.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException notAtomic) {
            Files.move(load_File.toPath(), init_File.toPath(), StandardCopyOption.REPLACE_EXISTING);
         }
      } catch (Throwable t) {
         try {
            Files.deleteIfExists(load_File.toPath());
         } catch (Throwable ignored) {
         }
         AutismClientAddon.LOG.warn("Failed to save Matchmaking prefs", t);
      }
   }

   private static boolean init_Z_Properties_Str(Properties p, String key) {
      return init_Z_Properties(p, key, false);
   }

   private static boolean init_Z_Properties(Properties p, String key, boolean def) {
      return Boolean.parseBoolean(p.getProperty(key, Boolean.toString(def)));
   }

   private static void init_V_Set_Str(Set<String> set, String csv) {
      if (csv != null && !csv.isBlank()) {
         for (String s : csv.split(",")) {
            if (flush_Z_Str(s)) {
               load_V_Set_Str(set, s.trim());
            }
         }
      }
   }

   private static void init_V_Map_Properties(Map<String, boolean[]> out, Properties props) {
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

   private static void load_V_Map_Properties(Map<String, boolean[]> map, Properties props) {
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

   private static void init_V_Map_Str(Map<String, String> map, String csv) {
      if (csv != null && !csv.isBlank()) {
         for (String entry : csv.split(",")) {
            int eq = entry.indexOf(61);
            String id = (eq >= 0 ? entry.substring(0, eq) : entry).trim();
            String name = eq >= 0 ? entry.substring(eq + 1).trim() : "";
            if (close_Z_Str(id)) {
               init_V_Map(map, id, name);
            }
         }
      }
   }

   private static String init_Str_Map(Map<String, String> map) {
      StringBuilder sb = new StringBuilder();
      synchronized (map) {
         for (Entry<String, String> e : map.entrySet()) {
            if (sb.length() > 0) {
               sb.append(',');
            }

            sb.append(e.getKey()).append('=').append(init_Str_Str(e.getValue()));
         }
      }

      return sb.toString();
   }

   private static String init_Str_Str(String n) {
      return n == null ? "" : n.replace(",", "").replace("=", "").trim();
   }

   private static void init_V_Map(Map<String, String> map, String id, String name) {
      synchronized (map) {
         map.put(id, init_Str_Str(name));

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

   private static void load_V_Set_Str(Set<String> set, String value) {
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

   private static boolean flush_Z_Str(String s) {
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

   private static boolean close_Z_Str(String s) {
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
