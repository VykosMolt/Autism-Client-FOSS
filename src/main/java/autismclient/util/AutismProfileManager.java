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

public final class AutismProfileManager {
   private static AutismProfileManager INSTANCE;
   public static final String init_Str = "default";
   private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final File profilesDir = new File(AutismClientAddon.FOLDER, "profiles");
   private final List<AutismProfile> profiles = new ArrayList<>();
   private String load_Str = "";
   private volatile boolean init_Z = false;
   private volatile long revision = 0L;
   private boolean switchInProgress = false;
   private static final long load_J = 750L;
   private volatile boolean snapshotPending = false;
   private long snapshotDueAt = 0L;
   private AutismConfig pendingSnapshot;
   private volatile boolean write = false;

   private AutismProfileManager() {
      this.sync();
      this.flush();
      this.close();
      this.open();
      AutismConfig.afterSave = this::write;
      AutismConfig.afterPersistenceSnapshot = this::init_V_AutismConfig;
   }

   public static synchronized AutismProfileManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new AutismProfileManager();
      }

      return INSTANCE;
   }

   static void init_V() {
      AutismProfileManager current = INSTANCE;
      if (current != null) {
         current.read();
      }
   }

   public synchronized List<AutismProfile> profiles() {
      List<AutismProfile> out = new ArrayList<>(this.profiles);
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

   public synchronized int count() {
      return this.profiles.size();
   }

   public static boolean isDefault(AutismProfile p) {
      return p != null && "default".equals(p.id);
   }

   public synchronized String activeId() {
      return this.load_Str;
   }

   public synchronized boolean hasUnsavedChanges() {
      return this.init_Z;
   }

   public long currentRevision() {
      return this.revision;
   }

   public synchronized AutismProfile active() {
      return this.byId(this.load_Str);
   }

   public synchronized AutismProfile byId(String id) {
      if (id == null) {
         return null;
      }

      for (AutismProfile p : this.profiles) {
         if (p != null && id.equals(p.id)) {
            return p;
         }
      }

      return null;
   }

   private boolean hasProfile(String id) {
      return this.byId(id) != null;
   }

   public synchronized boolean isNameTaken(String name, String excludeId) {
      if (name == null) {
         return false;
      }

      String want = name.strip();
      if (want.isEmpty()) {
         return false;
      }

      for (AutismProfile p : this.profiles) {
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
      if (this.isNameTaken(name, null)) {
         return null;
      }

      AutismProfile p = new AutismProfile(this.load_Str(name), name != null && !name.isBlank() ? name.strip() : "Profile");
      p.autoSave = autoSave;
      p.ownMacroLibrary = ownMacroLibrary;
      p.ownThemeColor = ownThemeColor;
      p.createdAt = p.updatedAt = System.currentTimeMillis();
      p.snapshot = this.captureConfigSnapshot();
      if (ownMacroLibrary) {
         MacroLibraryManager.writeEmptyLibrary(this.macrosFileFor(p));
      }

      this.profiles.add(p);
      this.init_V_AutismProfile_AutismConfig(p, p.snapshot);
      this.revision++;
      return p;
   }

   public synchronized AutismProfile duplicate(AutismProfile src, String name) {
      if (src == null) {
         return this.load_AutismProfile(name);
      }

      if (this.isNameTaken(name, null)) {
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
      this.profiles.add(p);
      this.sync(p);
      if (src.ownMacroLibrary) {
         try {
            File from = this.macrosFileFor(src);
            if (AuditMacroRead.duplicateExists(from)) {
               AuditMacroRead.copy(from.toPath(), this.macrosFileFor(p).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
         } catch (Exception e) {
            AutismClientAddon.LOG.warn("Could not copy macro library while duplicating profile", e);
         }
      }

      this.revision++;
      return p;
   }

   public synchronized boolean rename(AutismProfile p, String name) {
      if (p == null || name == null || name.isBlank()) {
         return false;
      }

      if (this.isNameTaken(name, p.id)) {
         return false;
      }

      p.displayName = name.strip();
      p.updatedAt = System.currentTimeMillis();
      this.sync(p);
      this.revision++;
      return true;
   }

   public synchronized boolean delete(AutismProfile p) {
      if (p != null && !"default".equals(p.id) && this.profiles.size() > 1) {
         boolean wasActive = p.id.equals(this.load_Str);
         this.profiles.remove(p);
         this.close(p);
         if (wasActive) {
            AutismProfile fallback = this.byId("default");
            if (fallback == null) {
               fallback = this.profiles.get(0);
            }

            this.switchTo(fallback);
         }

         if (AuditMacroRead.sameFile(MacroLibraryManager.getInstance().getLibraryFile(), this.macrosFileFor(p))) {
            AutismProfile var10000 = this.active();
            if (var10000 != null) {
               this.update(var10000);
            } else {
               MacroLibraryManager.getInstance().resetToDefaultLibraryFile();
            }
         }

         AuditMacroRead.delete(this.macrosFileFor(p), new File(this.profilesDir, p.id + ".macros.nbt.bak"));
         this.revision++;
         return true;
      } else {
         return false;
      }
   }

   public synchronized void setAutoSave(AutismProfile p, boolean autoSave) {
      if (p != null && p.autoSave != autoSave) {
         p.autoSave = autoSave;
         p.updatedAt = System.currentTimeMillis();
         if (autoSave && p.id.equals(this.load_Str)) {
            p.snapshot = this.captureConfigSnapshot();
            this.init_Z = false;
         }

         this.init_V_AutismProfile_AutismConfig(p, autoSave && p.id.equals(this.load_Str) ? p.snapshot : null);
         this.revision++;
      }
   }

   public synchronized void setOwnMacroLibrary(AutismProfile p, boolean own) {
      if (p != null && p.ownMacroLibrary != own) {
         p.ownMacroLibrary = own;
         p.updatedAt = System.currentTimeMillis();
         if (own) {
            this.write(p);
         }

         this.sync(p);
         if (p.id.equals(this.load_Str)) {
            if (own) {
               MacroLibraryManager.getInstance().switchLibraryFile(this.macrosFileFor(p), false, true);
            } else {
               MacroLibraryManager.getInstance().resetToDefaultLibraryFile();
            }
         }

         this.revision++;
      }
   }

   public synchronized void read(AutismProfile p, boolean own) {
      if (p != null && p.ownThemeColor != own) {
         p.ownThemeColor = own;
         p.updatedAt = System.currentTimeMillis();
         this.sync(p);
         this.revision++;
      }
   }

   public synchronized void setServerPatterns(AutismProfile p, List<String> patterns) {
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
         this.revision++;
      }
   }

   public synchronized void saveSnapshot(AutismProfile p) {
      if (p != null) {
         p.snapshot = this.captureConfigSnapshot();
         p.updatedAt = System.currentTimeMillis();
         this.init_V_AutismProfile_AutismConfig(p, p.snapshot);
         if (p.id.equals(this.load_Str)) {
            this.init_Z = false;
         }

         this.revision++;
      }
   }

   public synchronized void switchTo(AutismProfile p) {
      if (p != null) {
         AutismConfig.enqueuePendingSaveNow();
         this.read();
         this.switchInProgress = true;

         try {
            AutismConfig next = p.snapshot.deepCopy();
            next.activeProfileId = p.id;
            if (!p.ownThemeColor) {
               AutismConfig liveNow = AutismConfig.getGlobal();
               if (liveNow != null) {
                  next.themeColors = liveNow.deepCopy().themeColors;
               }
            }

            autismclient.modules.ModuleRuntimeManager.getInstance().applyConfig(next);
            this.update(p);
            this.load_Str = p.id;
            this.init_Z = false;
            this.revision++;
         } finally {
            this.switchInProgress = false;
         }

         AutismClientMessaging.sendPrefixed("§aLoaded profile: " + p.displayName);
      }
   }

   public synchronized void read(AutismProfile p) {
      if (p != null && !this.write && !p.id.equals(this.load_Str)) {
         this.write = true;
         Runnable applyJob = autismclient.gui.ThemeApplyProgressOverlay.init("Switching Profile", "Applying profile…");
         autismclient.gui.ThemeApplyProgressOverlay.init(() -> this.init_V_AutismProfile_Runnable(p, applyJob));
      }
   }

   private synchronized void init_V_AutismProfile_Runnable(AutismProfile p, Runnable applyJob) {
      Runnable macroJob = autismclient.gui.ThemeApplyProgressOverlay.init("Switching Profile", "Loading macros…");
      boolean macroKicked = false;

      try {
         AutismConfig.enqueuePendingSaveNow();
         this.read();
         this.switchInProgress = true;
         AutismConfig next = p.snapshot.deepCopy();
         next.activeProfileId = p.id;
         if (!p.ownThemeColor) {
            AutismConfig live = AutismConfig.getGlobal();
            if (live != null) {
               next.themeColors = live.deepCopy().themeColors;
            }
         }

         autismclient.modules.ModuleRuntimeManager.getInstance().applyConfig(next);
         this.load_Str = p.id;
         this.init_Z = false;
         this.revision++;
         AutismClientMessaging.sendPrefixed("§aLoaded profile: " + p.displayName);
         AutismProfile fp = p;
         Runnable mj = macroJob;
         BackgroundTaskExecutor.submit("profile-macros", () -> {
            // Same monitor as profile delete, so a delete cannot land between the check and the switch.
            synchronized (this) {
               if (this.load_Str.equals(fp.id) && this.profiles.contains(fp)) {
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
         this.switchInProgress = false;
         this.write = false;
         applyJob.run();
         if (!macroKicked) {
            macroJob.run();
         }
      }
   }

   public synchronized void onConnect(ServerAddress addr, ServerData data) {
      String address = data != null && data.ip != null && !data.ip.isBlank() ? data.ip : (addr != null ? addr.getHost() + ":" + addr.getPort() : "");
      AutismProfile match = this.read(address);
      if (match != null && !match.id.equals(this.load_Str)) {
         AutismProfile current = this.active();
         if (current != null && !current.autoSave && this.init_Z) {
            this.saveSnapshot(current);
         }

         this.switchTo(match);
      }
   }

   public synchronized AutismProfile read(String address) {
      if (address != null && !address.isBlank()) {
         AutismProfileManager.Inner_DataUtil server = init_bV_DataUtil_Str(address, 25565);
         AutismProfile best = null;
         int bestScore = 0;
         long bestCreated = 9223372036854775807L;

         for (AutismProfile p : this.profiles) {
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

   public static boolean matchesAddress(String address, String pattern) {
      if (address != null && !address.isBlank()) {
         AutismProfileManager.Inner_DataUtil server = init_bV_DataUtil_Str(address, 25565);
         return auditScore(server.init_Str(), server.init_I(), pattern) > 0;
      } else {
         return false;
      }
   }

   // Bracketed or bare IPv6 hosts; a single trailing dot is dropped from the host.
   static AutismProfileManager.Inner_DataUtil init_bV_DataUtil_Str(String address, int defaultPort) {
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

      return new AutismProfileManager.Inner_DataUtil(host, port);
   }

   // 0 = no match. Exact hosts beat wildcards, longer wildcard suffixes beat shorter ones, an explicit port breaks ties.
   static int auditScore(String host, int port, String pattern) {
      if (pattern == null || pattern.isBlank()) {
         return 0;
      }

      AutismProfileManager.Inner_DataUtil pat = init_bV_DataUtil_Str(pattern, -1);
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
      if (!this.switchInProgress) {
         AutismProfile a = this.active();
         if (a != null) {
            if (a.autoSave) {
               this.pendingSnapshot = null;
               this.snapshotDueAt = System.currentTimeMillis() + 750L;
               this.snapshotPending = true;
            } else {
               this.init_Z = true;
               this.revision++;
            }
         }
      }
   }

   private synchronized void init_V_AutismConfig(AutismConfig snapshot) {
      if (!this.switchInProgress && snapshot != null && this.snapshotPending) {
         AutismProfile a = this.active();
         if (a != null && a.autoSave) {
            this.pendingSnapshot = AutismConfig.profileSnapshotFromDetached(snapshot);
         }
      }
   }

   public void load() {
      if (this.snapshotPending) {
         synchronized (this) {
            if (this.snapshotPending && System.currentTimeMillis() >= this.snapshotDueAt) {
               this.update();
            }
         }
      }
   }

   public synchronized void read() {
      if (this.snapshotPending && this.pendingSnapshot == null) {
         AutismConfigPersistence.init();
      }

      if (this.snapshotPending) {
         this.update();
      }
   }

   private void update() {
      this.snapshotPending = false;
      AutismProfile a = this.active();
      if (a != null && a.autoSave) {
         AutismConfig snap = this.pendingSnapshot;
         this.pendingSnapshot = null;
         if (snap == null) {
            snap = this.captureConfigSnapshot();
         }

         a.snapshot = snap;
         a.updatedAt = System.currentTimeMillis();
         this.init_V_AutismProfile_AutismConfig(a, snap);
         this.init_Z = false;
      }
   }

   private AutismConfig captureConfigSnapshot() {
      return AutismConfig.getGlobal().snapshotForProfile();
   }

   private File macrosFileFor(AutismProfile p) {
      return new File(this.profilesDir, p.id + ".macros.nbt");
   }

   private void write(AutismProfile p) {
      File comp = this.macrosFileFor(p);
      File bak = new File(this.profilesDir, p.id + ".macros.nbt.bak");
      if (!comp.exists() && !bak.exists()) {
         try {
            this.profilesDir.mkdirs();
            File shared = MacroLibraryManager.defaultLibraryFile();
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
         MacroLibraryManager.getInstance().switchLibraryFile(this.macrosFileFor(p), false, false);
      } else {
         MacroLibraryManager.getInstance().resetToDefaultLibraryFile();
      }
   }

   private File profileFileFor(AutismProfile p) {
      return new File(this.profilesDir, p.id + ".json");
   }

   private String profileSourceKey(String id) {
      return "profile:" + new File(this.profilesDir, id + ".json").getAbsolutePath();
   }

   private void sync() {
      this.profiles.clear();
      if (this.profilesDir.exists()) {
         File[] files = this.profilesDir
            .listFiles(
               (var0, var1x) -> var1x.endsWith(".json")
                  ? true
                  : var1x.endsWith(".json.bak") && new File(var0, var1x).isFile() && !new File(var0, var1x.substring(0, var1x.length() - 4)).exists()
            );
         if (files != null) {
            for (File f : files) {
               AutismProfile p = this.readProfileFile(f);
               if (p == null) {
                  this.quarantineCorruptFile(f);
                  File bak = new File(this.profilesDir, f.getName() + ".bak");
                  if (bak.exists()) {
                     p = this.readProfileFile(bak);
                  }
               }

               if (p != null && !p.id.isBlank() && this.byId(p.id) == null) {
                  this.profiles.add(p);
               }
            }
         }
      }
   }

   private AutismProfile readProfileFile(File f) {
      try (FileReader r = new FileReader(f)) {
         AutismProfile p = this.GSON.fromJson(r, AutismProfile.class);
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

   private void quarantineCorruptFile(File f) {
      try {
         File aside = new File(this.profilesDir, f.getName() + ".corrupt-" + System.currentTimeMillis());
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
         AutismProfile snapshot = copyWithConfig(p, detachedSnapshot);
         DebouncedPersistenceScheduler.init(this.profileSourceKey(snapshot.id), () -> this.flush(snapshot));
      }
   }

   private void flush(AutismProfile p) {
      this.profilesDir.mkdirs();
      File target = this.profileFileFor(p);
      File tmp = new File(this.profilesDir, p.id + ".json.tmp");
      File bak = new File(this.profilesDir, p.id + ".json.bak");

      // Profile snapshots carry module settings (AutoLogin password), so they are owner-only like the config.
      try {
         autismclient.util.mm.a.DataUtil.auditPrivateFile(tmp);
         try (FileWriter w = new FileWriter(tmp)) {
            this.GSON.toJson(p, w);
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

   private static AutismProfile copyWithConfig(AutismProfile source, AutismConfig detachedSnapshot) {
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
         DebouncedPersistenceScheduler.init(this.profileSourceKey(id), () -> {
            this.load(new File(this.profilesDir, id + ".json"));
            this.load(new File(this.profilesDir, id + ".json.bak"));
            this.load(new File(this.profilesDir, id + ".json.tmp"));
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
      if (this.profiles.isEmpty()) {
         AutismProfile def = new AutismProfile("default", "Default");
         def.autoSave = true;
         def.createdAt = def.updatedAt = System.currentTimeMillis();
         def.snapshot = this.captureConfigSnapshot();
         this.profiles.add(def);
         this.load_Str = def.id;
         this.sync(def);
      } else if (this.byId("default") == null) {
         AutismConfig stock = new AutismConfig();
         stock.applyRuntimeDefaults();
         AutismProfile def = new AutismProfile("default", "Default");
         def.autoSave = true;
         def.createdAt = def.updatedAt = System.currentTimeMillis();
         def.snapshot = stock.snapshotForProfile();
         this.profiles.add(def);
         this.sync(def);
      }
   }

   private void close() {
      Set<String> taken = new HashSet<>();
      AutismProfile def = this.byId("default");
      if (def != null && def.displayName != null) {
         taken.add(def.displayName.strip().toLowerCase(Locale.ROOT));
      }

      for (AutismProfile p : this.profiles) {
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
      if (want != null && !want.isBlank() && this.hasProfile(want)) {
         this.load_Str = want;
      } else if (this.load_Str == null || this.load_Str.isBlank() || !this.hasProfile(this.load_Str)) {
         this.load_Str = this.profiles.isEmpty() ? "" : this.profiles.get(0).id;
         AutismConfig.getGlobal().activeProfileId = this.load_Str;
      }

      AutismProfile a = this.active();
      if (a != null && !a.autoSave) {
         try {
            String live = this.GSON.toJson(this.captureConfigSnapshot());
            String saved = this.GSON.toJson(a.snapshot);
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

      while (this.hasProfile(candidate)) {
         candidate = base + "-" + n++;
      }

      return candidate;
   }

   private AutismProfile auditReadProfileForBackup(File f) {
      try (FileReader r = new FileReader(f)) {
         AutismProfile p = this.GSON.fromJson(r, AutismProfile.class);
         return p == null ? null : AuditConfigSafety.normalizeBackupProfile(p);
      } catch (Throwable t) {
         AutismClientAddon.LOG.error("Failed to read profile {}", f.getName(), t);
         return null;
      }
   }

   record Inner_DataUtil(String init_Str, int init_I) {
   }
}
