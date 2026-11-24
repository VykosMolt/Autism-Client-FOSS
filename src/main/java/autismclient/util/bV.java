package autismclient.util;

import autismclient.AutismClientAddon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public final class bV {
   private static bV init_bV;
   public static final String init_Str = "default";
   private final Gson init_Gson = new GsonBuilder().setPrettyPrinting().create();
   private final File init_File = new File(AutismClientAddon.FOLDER, "profiles");
   private final List<AutismProfile> init_List = new ArrayList<>();
   private String load_Str = "";
   private volatile boolean init_Z = false;
   private volatile long init_J = 0L;
   private boolean load_Z = false;
   private static final long load_J = 750L;
   private volatile boolean read_Z = false;
   private long read_J = 0L;
   private AutismConfig init_AutismConfig;
   private volatile boolean write = false;

   private bV() {
      this.sync();
      this.flush();
      this.close();
      this.open();
      AutismConfig.afterSave = this::write;
      AutismConfig.afterPersistenceSnapshot = this::init_V_AutismConfig;
   }

   public static synchronized bV init_bV() {
      if (init_bV == null) {
         init_bV = new bV();
      }

      return init_bV;
   }

   static void init_V() {
      bV current = init_bV;
      if (current != null) {
         current.read();
      }
   }

   public synchronized List<AutismProfile> init_List() {
      List<AutismProfile> out = new ArrayList<>(this.init_List);
      out.sort((a, b) -> {
         boolean da = "default".equals(a.id);
         boolean db = "default".equals(b.id);
         if (da != db) {
            return da ? -1 : 1;
         } else {
            return Long.compare(a.createdAt, b.createdAt);
         }
      });
      return out;
   }

   public synchronized int init_I() {
      return this.init_List.size();
   }

   public static boolean init_Z_AutismProfile(AutismProfile p) {
      return p != null && "default".equals(p.id);
   }

   public synchronized String init_Str() {
      return this.load_Str;
   }

   public synchronized boolean init_Z() {
      return this.init_Z;
   }

   public long init_J() {
      return this.init_J;
   }

   public synchronized AutismProfile init_AutismProfile() {
      return this.init_AutismProfile_Str(this.load_Str);
   }

   public synchronized AutismProfile init_AutismProfile_Str(String id) {
      if (id == null) {
         return null;
      }

      for (AutismProfile p : this.init_List) {
         if (p != null && id.equals(p.id)) {
            return p;
         }
      }

      return null;
   }

   private boolean init_Z_Str(String id) {
      return this.init_AutismProfile_Str(id) != null;
   }

   public synchronized boolean init_Z_Str_Str(String name, String excludeId) {
      if (name == null) {
         return false;
      }

      String want = name.strip();
      if (want.isEmpty()) {
         return false;
      }

      for (AutismProfile p : this.init_List) {
         if (p != null && p.displayName != null && (excludeId == null || !excludeId.equals(p.id)) && p.displayName.strip().equalsIgnoreCase(want)) {
            return true;
         }
      }

      return false;
   }

   public synchronized AutismProfile load_AutismProfile(String name) {
      return this.init(name, true, false, true);
   }

   public synchronized AutismProfile init(String name, boolean autoSave, boolean ownMacroLibrary, boolean ownThemeColor) {
      if (this.init_Z_Str_Str(name, null)) {
         return null;
      }

      AutismProfile p = new AutismProfile(this.load_Str(name), name != null && !name.isBlank() ? name.strip() : "Profile");
      p.autoSave = autoSave;
      p.ownMacroLibrary = ownMacroLibrary;
      p.ownThemeColor = ownThemeColor;
      p.createdAt = p.updatedAt = System.currentTimeMillis();
      p.snapshot = this.init_AutismConfig();
      if (ownMacroLibrary) {
         aN.init_V_File(this.init_File_AutismProfile(p));
      }

      this.init_List.add(p);
      this.init_V_AutismProfile_AutismConfig(p, p.snapshot);
      this.init_J++;
      return p;
   }

   public synchronized AutismProfile init_AutismProfile_AutismProfile_Str(AutismProfile src, String name) {
      if (src == null) {
         return this.load_AutismProfile(name);
      }

      if (this.init_Z_Str_Str(name, null)) {
         return null;
      }

      AutismProfile p = new AutismProfile(this.load_Str(name), name != null && !name.isBlank() ? name.strip() : src.displayName + " copy");
      p.autoSave = src.autoSave;
      p.ownMacroLibrary = src.ownMacroLibrary;
      p.ownThemeColor = src.ownThemeColor;
      p.serverPatterns = new ArrayList<>(src.serverPatterns);
      p.createdAt = p.updatedAt = System.currentTimeMillis();
      if (src.autoSave && this.load_Str.equals(src.id)) {
         this.write();
         this.read();
      }

      p.snapshot = src.snapshot.deepCopy();
      this.init_List.add(p);
      this.sync(p);
      if (src.ownMacroLibrary) {
         try {
            File from = this.init_File_AutismProfile(src);
            if (AuditMacroRead.duplicateExists(from)) {
               AuditMacroRead.copy(from.toPath(), this.init_File_AutismProfile(p).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
         } catch (Exception e) {
            AutismClientAddon.LOG.warn("Could not copy macro library while duplicating profile", e);
         }
      }

      this.init_J++;
      return p;
   }

   public synchronized boolean init_Z_AutismProfile_Str(AutismProfile p, String name) {
      if (p == null || name == null || name.isBlank()) {
         return false;
      }

      if (this.init_Z_Str_Str(name, p.id)) {
         return false;
      }

      p.displayName = name.strip();
      p.updatedAt = System.currentTimeMillis();
      this.sync(p);
      this.init_J++;
      return true;
   }

   public synchronized boolean load_Z(AutismProfile p) {
      if (p != null && !"default".equals(p.id) && this.init_List.size() > 1) {
         boolean wasActive = p.id.equals(this.load_Str);
         this.init_List.remove(p);
         this.close(p);
         if (wasActive) {
            AutismProfile fallback = this.init_AutismProfile_Str("default");
            if (fallback == null) {
               fallback = this.init_List.get(0);
            }

            this.load_V(fallback);
         }

         if (AuditMacroRead.sameFile(aN.init_aN().load_File(), this.init_File_AutismProfile(p))) {
            AutismProfile var10000 = this.init_AutismProfile();
            if (var10000 != null) {
               this.update(var10000);
            } else {
               aN.init_aN().write();
            }
         }

         AuditMacroRead.delete(this.init_File_AutismProfile(p), new File(this.init_File, p.id + ".macros.nbt.bak"));
         this.init_J++;
         return true;
      } else {
         return false;
      }
   }

   public synchronized void init_V_AutismProfile_Z(AutismProfile p, boolean autoSave) {
      if (p != null && p.autoSave != autoSave) {
         p.autoSave = autoSave;
         p.updatedAt = System.currentTimeMillis();
         if (autoSave && p.id.equals(this.load_Str)) {
            p.snapshot = this.init_AutismConfig();
            this.init_Z = false;
         }

         this.init_V_AutismProfile_AutismConfig(p, autoSave && p.id.equals(this.load_Str) ? p.snapshot : null);
         this.init_J++;
      }
   }

   public synchronized void load_V_AutismProfile_Z(AutismProfile p, boolean own) {
      if (p != null && p.ownMacroLibrary != own) {
         p.ownMacroLibrary = own;
         p.updatedAt = System.currentTimeMillis();
         if (own) {
            this.write(p);
         }

         this.sync(p);
         if (p.id.equals(this.load_Str)) {
            if (own) {
               aN.init_aN().init(this.init_File_AutismProfile(p), false, true);
            } else {
               aN.init_aN().write();
            }
         }

         this.init_J++;
      }
   }

   public synchronized void read(AutismProfile p, boolean own) {
      if (p != null && p.ownThemeColor != own) {
         p.ownThemeColor = own;
         p.updatedAt = System.currentTimeMillis();
         this.sync(p);
         this.init_J++;
      }
   }

   public synchronized void init_V_AutismProfile_List(AutismProfile p, List<String> patterns) {
      if (p != null) {
         List<String> clean = new ArrayList<>();
         if (patterns != null) {
            for (String s : patterns) {
               if (s != null) {
                  String n = s.strip().toLowerCase(Locale.ROOT);
                  if (!n.isEmpty() && !clean.contains(n)) {
                     clean.add(n);
                  }
               }
            }
         }

         p.serverPatterns = clean;
         p.updatedAt = System.currentTimeMillis();
         this.sync(p);
         this.init_J++;
      }
   }

   public synchronized void init_V_AutismProfile(AutismProfile p) {
      if (p != null) {
         p.snapshot = this.init_AutismConfig();
         p.updatedAt = System.currentTimeMillis();
         this.init_V_AutismProfile_AutismConfig(p, p.snapshot);
         if (p.id.equals(this.load_Str)) {
            this.init_Z = false;
         }

         this.init_J++;
      }
   }

   public synchronized void load_V(AutismProfile p) {
      if (p != null) {
         AutismConfig.enqueuePendingSaveNow();
         this.read();
         this.load_Z = true;

         try {
            AutismConfig next = p.snapshot.deepCopy();
            next.activeProfileId = p.id;
            if (!p.ownThemeColor) {
               AutismConfig liveNow = AutismConfig.getGlobal();
               if (liveNow != null) {
                  next.themeColors = liveNow.deepCopy().themeColors;
               }
            }

            autismclient.modules.PoolEntry.init_PoolEntry().init_V_AutismConfig(next);
            this.update(p);
            this.load_Str = p.id;
            this.init_Z = false;
            this.init_J++;
         } finally {
            this.load_Z = false;
         }

         AutismClientMessaging.sendPrefixed("§aLoaded profile: " + p.displayName);
      }
   }

   public synchronized void read(AutismProfile p) {
      if (p != null && !this.write && !p.id.equals(this.load_Str)) {
         this.write = true;
         Runnable applyJob = autismclient.gui.CachePool.init("Switching Profile", "Applying profile…");
         autismclient.gui.CachePool.init(() -> this.init_V_AutismProfile_Runnable(p, applyJob));
      }
   }

   private synchronized void init_V_AutismProfile_Runnable(AutismProfile p, Runnable applyJob) {
      Runnable macroJob = autismclient.gui.CachePool.init("Switching Profile", "Loading macros…");
      boolean macroKicked = false;

      try {
         AutismConfig.enqueuePendingSaveNow();
         this.read();
         this.load_Z = true;
         AutismConfig next = p.snapshot.deepCopy();
         next.activeProfileId = p.id;
         if (!p.ownThemeColor) {
            AutismConfig live = AutismConfig.getGlobal();
            if (live != null) {
               next.themeColors = live.deepCopy().themeColors;
            }
         }

         autismclient.modules.PoolEntry.init_PoolEntry().init_V_AutismConfig(next);
         this.load_Str = p.id;
         this.init_Z = false;
         this.init_J++;
         AutismClientMessaging.sendPrefixed("§aLoaded profile: " + p.displayName);
         AutismProfile fp = p;
         Runnable mj = macroJob;
         ClassCache.init_Future_Str_Runnable("profile-macros", () -> {
            // Same monitor as profile delete, so a delete cannot land between the check and the switch.
            synchronized (this) {
               if (this.load_Str.equals(fp.id) && this.init_List.contains(fp)) {
                  try {
                     this.update(fp);
                  } catch (Throwable tx) {
                     AutismClientAddon.LOG.warn("Profile macro switch failed", tx);
                  } finally {
                     mj.run();
                  }
               } else {
                  mj.run();
               }
            }
         });
         macroKicked = true;
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("Profile switch failed", t);
      } finally {
         this.load_Z = false;
         this.write = false;
         applyJob.run();
         if (!macroKicked) {
            macroJob.run();
         }
      }
   }

   public synchronized void init_V_ServerAddress_ServerData(ServerAddress addr, ServerData data) {
      String address = data != null && data.ip != null && !data.ip.isBlank() ? data.ip : (addr != null ? addr.getHost() + ":" + addr.getPort() : "");
      AutismProfile match = this.read(address);
      if (match != null && !match.id.equals(this.load_Str)) {
         AutismProfile current = this.init_AutismProfile();
         if (current != null && !current.autoSave && this.init_Z) {
            this.init_V_AutismProfile(current);
         }

         this.load_V(match);
      }
   }

   public synchronized AutismProfile read(String address) {
      if (address != null && !address.isBlank()) {
         bV.Inner_DataUtil server = init_bV_DataUtil_Str(address, 25565);
         AutismProfile best = null;
         int bestScore = 0;
         long bestCreated = 9223372036854775807L;

         for (AutismProfile p : this.init_List) {
            if (p != null && p.serverPatterns != null) {
               for (String pattern : p.serverPatterns) {
                  int score = auditScore(server.init_Str(), server.init_I(), pattern);
                  if (score > bestScore || score > 0 && score == bestScore && p.createdAt < bestCreated) {
                     best = p;
                     bestScore = score;
                     bestCreated = p.createdAt;
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   public static boolean load_Z_Str_Str(String address, String pattern) {
      if (address != null && !address.isBlank()) {
         bV.Inner_DataUtil server = init_bV_DataUtil_Str(address, 25565);
         return auditScore(server.init_Str(), server.init_I(), pattern) > 0;
      } else {
         return false;
      }
   }

   // Bracketed or bare IPv6 hosts; a single trailing dot is dropped from the host.
   static bV.Inner_DataUtil init_bV_DataUtil_Str(String address, int defaultPort) {
      String host = address.strip().toLowerCase(Locale.ROOT);
      int port = defaultPort;
      int close = host.indexOf(']');
      int colon = host.indexOf(':');
      try {
         if (host.startsWith("[") && close > 0) {
            if (host.startsWith(":", close + 1)) {
               port = Integer.parseInt(host.substring(close + 2));
            }

            host = host.substring(1, close);
         } else if (colon > 0 && colon == host.lastIndexOf(':')) {
            port = Integer.parseInt(host.substring(colon + 1));
            host = host.substring(0, colon);
         }
      } catch (NumberFormatException ignored) {
      }

      if (host.endsWith(".")) {
         host = host.substring(0, host.length() - 1);
      }

      return new bV.Inner_DataUtil(host, port);
   }

   // 0 = no match. Exact hosts beat wildcards, longer wildcard suffixes beat shorter ones, an explicit port breaks ties.
   static int auditScore(String host, int port, String pattern) {
      if (pattern == null || pattern.isBlank()) {
         return 0;
      }

      bV.Inner_DataUtil pat = init_bV_DataUtil_Str(pattern, -1);
      String patHost = pat.init_Str();
      if (pat.init_I() != -1 && pat.init_I() != port) {
         return 0;
      }

      int portBonus = pat.init_I() == -1 ? 0 : 1;
      if (patHost.equals("*")) {
         return 1 + portBonus;
      }

      if (patHost.startsWith("*.")) {
         String suffix = patHost.substring(2);
         return !host.equals(suffix) && !host.endsWith("." + suffix) ? 0 : 2 + 2 * suffix.length() + portBonus;
      }

      return host.equals(patHost) ? 1000000 + portBonus : 0;
   }

   private synchronized void write() {
      if (!this.load_Z) {
         AutismProfile a = this.init_AutismProfile();
         if (a != null) {
            if (a.autoSave) {
               this.init_AutismConfig = null;
               this.read_J = System.currentTimeMillis() + 750L;
               this.read_Z = true;
            } else {
               this.init_Z = true;
               this.init_J++;
            }
         }
      }
   }

   private synchronized void init_V_AutismConfig(AutismConfig snapshot) {
      if (!this.load_Z && snapshot != null && this.read_Z) {
         AutismProfile a = this.init_AutismProfile();
         if (a != null && a.autoSave) {
            this.init_AutismConfig = AutismConfig.profileSnapshotFromDetached(snapshot);
         }
      }
   }

   public void load() {
      if (this.read_Z) {
         synchronized (this) {
            if (this.read_Z && System.currentTimeMillis() >= this.read_J) {
               this.update();
            }
         }
      }
   }

   public synchronized void read() {
      if (this.read_Z && this.init_AutismConfig == null) {
         B.init();
      }

      if (this.read_Z) {
         this.update();
      }
   }

   private void update() {
      this.read_Z = false;
      AutismProfile a = this.init_AutismProfile();
      if (a != null && a.autoSave) {
         AutismConfig snap = this.init_AutismConfig;
         this.init_AutismConfig = null;
         if (snap == null) {
            snap = this.init_AutismConfig();
         }

         a.snapshot = snap;
         a.updatedAt = System.currentTimeMillis();
         this.init_V_AutismProfile_AutismConfig(a, snap);
         this.init_Z = false;
      }
   }

   private AutismConfig init_AutismConfig() {
      return AutismConfig.getGlobal().snapshotForProfile();
   }

   private File init_File_AutismProfile(AutismProfile p) {
      return new File(this.init_File, p.id + ".macros.nbt");
   }

   private void write(AutismProfile p) {
      File comp = this.init_File_AutismProfile(p);
      File bak = new File(this.init_File, p.id + ".macros.nbt.bak");
      if (!comp.exists() && !bak.exists()) {
         try {
            this.init_File.mkdirs();
            File shared = aN.init_File();
            if (shared.exists()) {
               Files.copy(shared.toPath(), comp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
         } catch (Exception e) {
            AutismClientAddon.LOG.warn("Could not seed profile macro library", e);
         }
      }
   }

   private void update(AutismProfile p) {
      if (p.ownMacroLibrary) {
         this.write(p);
         aN.init_aN().init(this.init_File_AutismProfile(p), false, false);
      } else {
         aN.init_aN().write();
      }
   }

   private File load_File(AutismProfile p) {
      return new File(this.init_File, p.id + ".json");
   }

   private String init_Str_Str(String id) {
      return "profile:" + new File(this.init_File, id + ".json").getAbsolutePath();
   }

   private void sync() {
      this.init_List.clear();
      if (this.init_File.exists()) {
         File[] files = this.init_File
            .listFiles(
               (var0, var1x) -> var1x.endsWith(".json")
                  ? true
                  : var1x.endsWith(".json.bak") && new File(var0, var1x).isFile() && !new File(var0, var1x.substring(0, var1x.length() - 4)).exists()
            );
         if (files != null) {
            for (File f : files) {
               AutismProfile p = this.init_AutismProfile_File(f);
               if (p == null) {
                  this.init_V_File(f);
                  File bak = new File(this.init_File, f.getName() + ".bak");
                  if (bak.exists()) {
                     p = this.init_AutismProfile_File(bak);
                  }
               }

               if (p != null && !p.id.isBlank() && this.init_AutismProfile_Str(p.id) == null) {
                  this.init_List.add(p);
               }
            }
         }
      }
   }

   private AutismProfile init_AutismProfile_File(File f) {
      try (FileReader r = new FileReader(f)) {
         AutismProfile p = this.init_Gson.fromJson(r, AutismProfile.class);
         if (p == null) {
            return null;
         }

         p.normalize();
         return p;
      } catch (Throwable t) {
         AutismClientAddon.LOG.error("Failed to read profile {}", f.getName(), t);
         return null;
      }
   }

   private void init_V_File(File f) {
      try {
         File aside = new File(this.init_File, f.getName() + ".corrupt-" + System.currentTimeMillis());
         Files.move(f.toPath(), aside.toPath(), StandardCopyOption.REPLACE_EXISTING);
         AutismClientAddon.LOG.error("Profile {} was corrupt; moved it to {}", f.getName(), aside.getName());
      } catch (Throwable t) {
         AutismClientAddon.LOG.error("Failed to set aside corrupt profile {}", f.getName(), t);
      }
   }

   private void sync(AutismProfile p) {
      this.init_V_AutismProfile_AutismConfig(p, null);
   }

   private void init_V_AutismProfile_AutismConfig(AutismProfile p, AutismConfig detachedSnapshot) {
      if (p != null) {
         AutismProfile snapshot = init_AutismProfile_AutismProfile_AutismConfig(p, detachedSnapshot);
         dj.init(this.init_Str_Str(snapshot.id), () -> this.flush(snapshot));
      }
   }

   private void flush(AutismProfile p) {
      this.init_File.mkdirs();
      File target = this.load_File(p);
      File tmp = new File(this.init_File, p.id + ".json.tmp");
      File bak = new File(this.init_File, p.id + ".json.bak");

      // Profile snapshots carry module settings (AutoLogin password), so they are owner-only like the config.
      try {
         autismclient.util.mm.a.DataUtil.auditPrivateFile(tmp);
         try (FileWriter w = new FileWriter(tmp)) {
            this.init_Gson.toJson(p, w);
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG.error("Failed to write profile {}", p.id, t);
         tmp.delete();
         return;
      }

      try {
         if (target.exists() && this.auditReadProfileForBackup(target) != null) {
            autismclient.util.mm.a.DataUtil.auditCopyPrivateAtomic(target, bak);
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("Failed to back up profile {} before save", p.id, t);
      }

      try {
         Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (Throwable atomicFailed) {
         try {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
         } catch (Throwable t) {
            AutismClientAddon.LOG.error("Failed to swap in profile {}", p.id, t);
         }
      }
   }

   private static AutismProfile init_AutismProfile_AutismProfile_AutismConfig(AutismProfile source, AutismConfig detachedSnapshot) {
      AutismProfile copy = new AutismProfile();
      copy.id = source.id == null ? "" : source.id;
      copy.displayName = source.displayName == null ? "" : source.displayName;
      copy.autoSave = source.autoSave;
      copy.ownMacroLibrary = source.ownMacroLibrary;
      copy.ownThemeColor = source.ownThemeColor;
      copy.serverPatterns = source.serverPatterns == null ? new ArrayList<>() : new ArrayList<>(source.serverPatterns);
      copy.createdAt = source.createdAt;
      copy.updatedAt = source.updatedAt;
      copy.schemaVersion = source.schemaVersion;
      copy.snapshot = detachedSnapshot != null ? detachedSnapshot : (source.snapshot == null ? new AutismConfig() : source.snapshot.deepCopy());
      return copy;
   }

   private void close(AutismProfile profile) {
      if (profile != null) {
         String id = profile.id;
         dj.init(this.init_Str_Str(id), () -> {
            this.load(new File(this.init_File, id + ".json"));
            this.load(new File(this.init_File, id + ".json.bak"));
            this.load(new File(this.init_File, id + ".json.tmp"));
         });
      }
   }

   private void load(File f) {
      try {
         if (f != null && f.exists()) {
            Files.deleteIfExists(f.toPath());
         }
      } catch (Throwable var3) {
      }
   }

   private void flush() {
      if (this.init_List.isEmpty()) {
         AutismProfile def = new AutismProfile("default", "Default");
         def.autoSave = true;
         def.createdAt = def.updatedAt = System.currentTimeMillis();
         def.snapshot = this.init_AutismConfig();
         this.init_List.add(def);
         this.load_Str = def.id;
         this.sync(def);
      } else if (this.init_AutismProfile_Str("default") == null) {
         AutismConfig stock = new AutismConfig();
         stock.applyRuntimeDefaults();
         AutismProfile def = new AutismProfile("default", "Default");
         def.autoSave = true;
         def.createdAt = def.updatedAt = System.currentTimeMillis();
         def.snapshot = stock.snapshotForProfile();
         this.init_List.add(def);
         this.sync(def);
      }
   }

   private void close() {
      Set<String> taken = new HashSet<>();
      AutismProfile def = this.init_AutismProfile_Str("default");
      if (def != null && def.displayName != null) {
         taken.add(def.displayName.strip().toLowerCase(Locale.ROOT));
      }

      for (AutismProfile p : this.init_List) {
         if (p != null && p != def) {
            String base = p.displayName == null ? "" : p.displayName.strip();
            if (base.isEmpty()) {
               base = "Profile";
            }

            if (!taken.add(base.toLowerCase(Locale.ROOT))) {
               int n = 2;

               String candidate;
               do {
                  candidate = base + " (" + n++ + ")";
               } while (!taken.add(candidate.toLowerCase(Locale.ROOT)));

               p.displayName = candidate;
               p.updatedAt = System.currentTimeMillis();
               this.sync(p);
            }
         }
      }
   }

   private void open() {
      String want = AutismConfig.getGlobal().activeProfileId;
      if (want != null && !want.isBlank() && this.init_Z_Str(want)) {
         this.load_Str = want;
      } else if (this.load_Str == null || this.load_Str.isBlank() || !this.init_Z_Str(this.load_Str)) {
         this.load_Str = this.init_List.isEmpty() ? "" : this.init_List.get(0).id;
         AutismConfig.getGlobal().activeProfileId = this.load_Str;
      }

      AutismProfile a = this.init_AutismProfile();
      if (a != null && !a.autoSave) {
         try {
            String live = this.init_Gson.toJson(this.init_AutismConfig());
            String saved = this.init_Gson.toJson(a.snapshot);
            if (!live.equals(saved)) {
               this.init_Z = true;
            }
         } catch (Throwable var5) {
         }
      }
   }

   private String load_Str(String name) {
      String var10000 = AutismProfile.sanitizeId(name);
      String base = var10000.substring(0, Math.min(var10000.length(), 200)).replaceFirst("-+$", "");
      String candidate = base;
      int n = 2;

      while (this.init_Z_Str(candidate)) {
         candidate = base + "-" + n++;
      }

      return candidate;
   }

   private AutismProfile auditReadProfileForBackup(File f) {
      try (FileReader r = new FileReader(f)) {
         AutismProfile p = this.init_Gson.fromJson(r, AutismProfile.class);
         return p == null ? null : AuditConfigSafety.normalizeBackupProfile(p);
      } catch (Throwable t) {
         AutismClientAddon.LOG.error("Failed to read profile {}", f.getName(), t);
         return null;
      }
   }

   record Inner_DataUtil(String init_Str, int init_I) {
   }
}
