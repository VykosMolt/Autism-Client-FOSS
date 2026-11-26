package autismclient.b;

import autismclient.AutismClientAddon;
import autismclient.api.ApiVersion;
import autismclient.api.AutismAddon;
import autismclient.api.ai.AiCapabilities;
import autismclient.api.custommenu.CustomMenuAdapterRegistry;
import autismclient.api.event.AddonEvents;
import autismclient.api.hud.HudElements;
import autismclient.api.macro.MacroActionRegistry;
import autismclient.api.macro.MacroPresetRegistry;
import autismclient.modules.al;
import autismclient.util.AutismConfig;
import autismclient.util.bi;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.fabricmc.loader.api.metadata.CustomValue.CvType;

public final class DataUtil {
   private static final int init_I = 22;
   private static final List<AutismAddon> init_List = new ArrayList<>();
   private static final Map<String, DataUtil.StreamUtil> init_Map = new LinkedHashMap<>();
   private static final Map<String, DataUtil.ConfigStore> load = new LinkedHashMap<>();
   private static final Pattern init_Pattern = Pattern.compile("[a-z][a-z0-9_-]{1,31}");
   private static final ThreadLocal<DataUtil.BufferHelper> AUDIT_CURRENT_ADDON = new ThreadLocal<>();
   private static boolean init_Z = false;

   private DataUtil() {
   }

   public static void init_V() {
      init_List.clear();
      init_Map.clear();
      init_Z = false;
      AutismConfig config = AutismConfig.getGlobal();
      List<DataUtil.CachePool> accepted = new ArrayList<>();

      for (EntrypointContainer<AutismAddon> container : FabricLoader.getInstance().getEntrypointContainers("autism", AutismAddon.class)) {
         ModContainer mod;
         try {
            mod = container.getProvider();
         } catch (Throwable t) {
            AutismClientAddon.LOG.error("[Addons] Failed to inspect an addon entrypoint", t);
            continue;
         }

         String modId = mod.getMetadata().getId();
         DataUtil.StreamUtil report = init(mod.getMetadata());
         if (read(modId)) {
            report.load("Disabled in AUTISM config. Enable it and restart to load.");
         } else {
            AutismAddon addon;
            try {
               addon = container.getEntrypoint();
            } catch (Throwable t) {
               report.init("construct", "Failed to construct entrypoint: " + init(t));
               AutismClientAddon.LOG.error("[Addons] Failed to construct addon '{}'", modId, t);
               continue;
            }

            try {
               int declared = addon.apiVersion();
               report.init(declared);
               if (declared <= 0) {
                  report.init("apiVersion", "Invalid API version " + declared + ".");
                  AutismClientAddon.LOG.warn("[Addons] Addon '{}' declared invalid API v{}; skipping it.", modId, declared);
               } else if (declared > ApiVersion.CURRENT) {
                  report.read("Needs API v" + declared + ", host provides v" + ApiVersion.CURRENT + ".");
                  AutismClientAddon.LOG.warn("[Addons] Addon '{}' needs API v{} but this client provides v{}; skipping it.", modId, declared, ApiVersion.CURRENT);
               } else {
                  if (declared < ApiVersion.CURRENT) {
                     AutismClientAddon.LOG.info("[Addons] Addon '{}' built against API v{} (current v{}); loading.", modId, declared, ApiVersion.CURRENT);
                  }

                  init(addon, mod.getMetadata());
                  report.init(addon);
                  accepted.add(new DataUtil.CachePool(modId, addon));
               }
            } catch (AbstractMethodError e) {
               report.write("Missing apiVersion() override.");
               AutismClientAddon.LOG.error("[Addons] Addon '{}' does not declare apiVersion(); skipping it.", modId, e);
            } catch (Throwable t) {
               report.init("prepare", "Failed to prepare: " + init(t));
               AutismClientAddon.LOG.error("[Addons] Error preparing addon '{}'", modId, t);
            }
         }
      }

      List<DataUtil.CachePool> categoryReady = new ArrayList<>();

      for (DataUtil.CachePool la : accepted) {
         if (init(la.init_Str, la.init_AutismAddon, "onRegisterCategories", la.init_AutismAddon::onRegisterCategories)) {
            categoryReady.add(la);
         }
      }

      for (DataUtil.CachePool la : categoryReady) {
         if (init(la.init_Str, la.init_AutismAddon, "onInitialize", la.init_AutismAddon::onInitialize)) {
            init(la.init_Str, la.init_AutismAddon);
         }
      }

      if (!init_List.isEmpty()) {
         AutismClientAddon.LOG.info("[Addons] Loaded {} addon(s).", init_List.size());
      }

      if (config.disabledAddonIds != null) {
         for (String id : config.disabledAddonIds) {
            if (id != null && !id.isBlank() && !init_Map.containsKey(id)) {
               DataUtil.StreamUtil report = new DataUtil.StreamUtil(id.trim(), id.trim(), "", "", -5609729);
               report.load("Disabled in AUTISM config, but no matching addon entrypoint was discovered.");
               init_Map.put(report.init_Str(), report);
            }
         }
      }
   }

   private static boolean init(DataUtil.CachePool la, String phase, Runnable body) {
      String name = la.init_AutismAddon.name != null && !la.init_AutismAddon.name.isBlank() ? la.init_AutismAddon.name : la.init_Str;
      DataUtil.BufferHelper prevCtx = AUDIT_CURRENT_ADDON.get();
      DataUtil.BufferHelper currentCtx = new DataUtil.BufferHelper(la.init_Str, name);
      AUDIT_CURRENT_ADDON.set(currentCtx);
      DataUtil.StreamUtil report = init_Map.get(la.init_Str);
      if (report != null) {
         report.init(phase);
      }

      try {
         body.run();
         return true;
      } catch (Throwable t) {
         if (report != null) {
            report.init(phase, "Threw during " + phase + ": " + init(t));
         }

         AutismClientAddon.LOG.error("[Addons] Addon '{}' threw during {}", la.init_Str, phase, t);
         return false;
      } finally {
         if (prevCtx == null) {
            AUDIT_CURRENT_ADDON.remove();
         } else {
            AUDIT_CURRENT_ADDON.set(prevCtx);
         }
      }
   }

   private static boolean init(String addonId, AutismAddon instance, String phase, Runnable body) {
      DataUtil.CachePool la = new DataUtil.CachePool(addonId, instance);
      if (init(la, phase, body)) {
         return true;
      }

      init(la);
      init_V(addonId);
      return false;
   }

   private static void init(String addonId, AutismAddon instance) {
      init_List.add(instance);
      DataUtil.StreamUtil report = init_Map.get(addonId);
      if (report != null) {
         report.init_V();
      }
   }

   private static boolean init(String addonId, AutismAddon instance, String contextLabel) {
      if (!init(addonId, instance, "onRegisterCategories", instance::onRegisterCategories)) {
         return false;
      }

      if (!init(addonId, instance, "onInitialize", instance::onInitialize)) {
         return false;
      }

      init(addonId, instance);
      AutismClientAddon.LOG.info("[Addons] {} '{}' loaded.", contextLabel, addonId);
      return true;
   }

   private static void init(DataUtil.CachePool la) {
      if (la != null && la.init_AutismAddon != null) {
         try {
            la.init_AutismAddon.onUnload();
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[Addons] Addon '{}' threw during onUnload", la.init_Str, t);
         }
      }
   }

   private static void init_V(String addonId) {
      read(addonId, "Rolled back partial registrations after load failure.");
   }

   private static void read(String addonId, String completionNote) {
      AiCapabilities.unregisterAddon(addonId);
      if (addonId != null && !addonId.isBlank()) {
         DataUtil.StreamUtil report = init_Map.get(addonId);
         init(addonId, report, "macro actions", () -> MacroActionRegistry.unregisterAddon(addonId));
         init(addonId, report, "custom menu adapters", () -> CustomMenuAdapterRegistry.unregisterAddon(addonId));
         init(addonId, report, "presets", () -> MacroPresetRegistry.unregisterAddon(addonId));
         init(addonId, report, "events", () -> AddonEvents.unregisterAddon(addonId));
         init(addonId, report, "hud", () -> HudElements.unregisterAddon(addonId));
         init(addonId, report, "modules", () -> al.init_V_Str(addonId));
         init(addonId, report, "commands", () -> autismclient.commands.DataUtil.init_V(addonId));
         if (report != null && completionNote != null && !completionNote.isBlank()) {
            report.update(completionNote);
         }
      }
   }

   private static void init(String addonId, DataUtil.StreamUtil report, String kind, Runnable body) {
      try {
         body.run();
      } catch (Throwable t) {
         String detail = "Failed to rollback " + kind + ": " + init(t);
         if (report != null) {
            report.update(detail);
         }

         AutismClientAddon.LOG.warn("[Addons] Addon '{}' rollback failed for {}", addonId, kind, t);
      }
   }

   private static DataUtil.StreamUtil init(ModMetadata meta) {
      String modId = meta.getId();
      DataUtil.StreamUtil existing = init_Map.get(modId);
      if (existing != null) {
         return existing;
      }

      DataUtil.StreamUtil report = new DataUtil.StreamUtil(
         modId,
         meta.getName() != null && !meta.getName().isBlank() ? meta.getName() : modId,
         meta.getVersion().getFriendlyString(),
         meta.getAuthors().stream().map(Person::getName).collect(Collectors.joining(", ")),
         init(meta, -5609729)
      );
      init_Map.put(modId, report);
      return report;
   }

   private static void init(AutismAddon addon, ModMetadata meta) {
      addon.name = meta.getName();
      addon.authors = meta.getAuthors().stream().map(Person::getName).collect(Collectors.joining(", "));
      addon.color = init(meta, addon.color);
   }

   private static int init(ModMetadata meta, int fallback) {
      CustomValue color = meta.containsCustomValue("autism:color") ? meta.getCustomValue("autism:color") : null;
      return color != null && color.getType() == CvType.STRING ? init(color.getAsString(), fallback) : fallback;
   }

   private static int init(String raw, int fallback) {
      try {
         String[] parts = raw.split(",");
         if (parts.length != 3) {
            return fallback;
         }

         int r = init(Integer.parseInt(parts[0].trim()));
         int g = init(Integer.parseInt(parts[1].trim()));
         int b = init(Integer.parseInt(parts[2].trim()));
         return 0xFF000000 | r << 16 | g << 8 | b;
      } catch (Exception e) {
         return fallback;
      }
   }

   private static int init(int v) {
      return Math.max(0, Math.min(255, v));
   }

   private static String read() {
      DataUtil.BufferHelper ctx = AUDIT_CURRENT_ADDON.get();
      return ctx == null ? null : ctx.init();
   }

   private static String write() {
      DataUtil.BufferHelper ctx = AUDIT_CURRENT_ADDON.get();
      return ctx == null ? null : ctx.load();
   }

   public static String init_Str() {
      return read();
   }

   public static boolean init_Z() {
      String id = read();
      return id != null && !id.isBlank();
   }

   public static String load_Str() {
      return write();
   }

   public static String init_Str(String name, String fallback) {
      String value = name == null ? "" : name.replaceAll("\\s+", " ").trim();
      if (value.isBlank()) {
         value = fallback == null ? "" : fallback.replaceAll("\\s+", " ").trim();
      }

      return value.isBlank() ? "Addon" : value;
   }

   public static String load_Str(String name, String fallback) {
      String value = init_Str(name, fallback);
      return value.length() <= 22 ? value : value.substring(0, Math.max(1, 19)).trim() + "...";
   }

   public static String init_Str(String rawId) {
      String raw = rawId == null ? "" : rawId.trim();
      if (raw.contains(":")) {
         return raw;
      }

      String owner = read();
      return owner != null && !owner.isBlank() ? owner + ":" + raw : raw;
   }

   public static String load_Str(String customLabel) {
      DataUtil.BufferHelper ctx = AUDIT_CURRENT_ADDON.get();
      String id = ctx == null ? null : ctx.init();
      String name = ctx == null ? null : ctx.load();
      if (name == null || name.isBlank()) {
         name = id;
      }

      return load_Str(name, id);
   }

   public static List<AutismAddon> init_List() {
      return Collections.unmodifiableList(init_List);
   }

   public static List<DataUtil.StreamUtil> load_List() {
      return Collections.unmodifiableList(new ArrayList<>(init_Map.values()));
   }

   public static DataUtil.StreamUtil init_DataUtil_StreamUti(String addonId) {
      return addonId == null ? null : init_Map.get(addonId);
   }

   public static synchronized DataUtil.TaskQueue init(String addonId, String displayName, Path classesDir, String entryClassName) {
      if (addonId == null || !init_Pattern.matcher(addonId).matches()) {
         throw new IllegalArgumentException("Invalid addon id '" + addonId + "'; must match [a-z][a-z0-9_-]{1,31}.");
      }

      if (entryClassName == null || entryClassName.isBlank()) {
         throw new IllegalArgumentException("entryClassName must not be blank.");
      }

      if (classesDir == null || !Files.isDirectory(classesDir)) {
         throw new IllegalArgumentException("classesDir does not exist or is not a directory: " + classesDir);
      }

      if (load.containsKey(addonId)) {
         throw new IllegalStateException("Addon '" + addonId + "' is already loaded; unload it before loading it again.");
      }

      DataUtil.StreamUtil previous = init_Map.get(addonId);
      if (previous != null && !previous.init_Z()) {
         throw new IllegalStateException("Addon '" + addonId + "' was discovered at startup; it is restart-gated and cannot be runtime-loaded.");
      }

      String name = init_Str(displayName, addonId);
      DataUtil.StreamUtil report = new DataUtil.StreamUtil(addonId, name, "", "", -5609729);
      report.load_V();
      init_Map.put(addonId, report);

      URLClassLoader loader;
      try {
         loader = new URLClassLoader(new URL[]{classesDir.toUri().toURL()}, DataUtil.class.getClassLoader());
      } catch (Exception e) {
         report.init("classloader", "Could not create class loader over " + classesDir + ": " + init(e));
         return init(addonId, DataUtil.Inner_DataUtil.FAILED, report);
      }

      try {
         Class<?> entryClass;
         try {
            entryClass = Class.forName(entryClassName.trim(), true, loader);
         } catch (Throwable t) {
            report.init("load", "Could not load entry class '" + entryClassName + "': " + init(t));
            return init(addonId, DataUtil.Inner_DataUtil.FAILED, report);
         }

         if (!AutismAddon.class.isAssignableFrom(entryClass)) {
            report.init("load", entryClass.getName() + " does not extend " + AutismAddon.class.getName() + ".");
            return init(addonId, DataUtil.Inner_DataUtil.FAILED, report);
         }

         AutismAddon addon;
         try {
            addon = (AutismAddon)entryClass.getDeclaredConstructor().newInstance();
         } catch (Throwable t) {
            report.init("construct", "Failed to construct " + entryClass.getName() + ": " + init(t));
            return init(addonId, DataUtil.Inner_DataUtil.FAILED, report);
         }

         int declared;
         try {
            declared = addon.apiVersion();
         } catch (AbstractMethodError e) {
            report.write("Missing apiVersion() override.");
            return init(addonId, DataUtil.Inner_DataUtil.MISSING_API_VERSION, report);
         } catch (Throwable t) {
            report.init("apiVersion", "apiVersion() threw: " + init(t));
            return init(addonId, DataUtil.Inner_DataUtil.FAILED, report);
         }

         report.init(declared);
         if (declared <= 0) {
            report.write("Invalid API version " + declared + ".");
            return init(addonId, DataUtil.Inner_DataUtil.MISSING_API_VERSION, report);
         }

         if (declared > ApiVersion.CURRENT) {
            report.read("Needs API v" + declared + ", host provides v" + ApiVersion.CURRENT + ".");
            return init(addonId, DataUtil.Inner_DataUtil.NEEDS_NEWER_API, report);
         }

         addon.name = name;
         addon.authors = "";
         report.init(addon);
         if (!init(addonId, addon, "Runtime addon")) {
            return init(addonId, DataUtil.Inner_DataUtil.FAILED, report);
         }

         load.put(addonId, new DataUtil.ConfigStore(addon, loader));
         return new DataUtil.TaskQueue(addonId, DataUtil.Inner_DataUtil.LOADED.name(), "Loaded runtime addon '" + name + "'.", init(report));
      } finally {
         if (!load.containsKey(addonId)) {
            init(addonId, loader);
         }
      }
   }

   public static synchronized boolean init_Z(String addonId) {
      if (addonId != null && !addonId.isBlank()) {
         DataUtil.ConfigStore entry = load.remove(addonId);
         if (entry == null) {
            return false;
         }

         init(new DataUtil.CachePool(addonId, entry.init_AutismAddon()));
         read(addonId, "Unloaded at runtime; all registrations removed.");
         init_List.remove(entry.init_AutismAddon());
         DataUtil.StreamUtil report = init_Map.get(addonId);
         if (report != null) {
            report.init("unloaded");
         }

         init(addonId, entry.init_URLClassLoader());
         AutismClientAddon.LOG.info("[Addons] Runtime addon '{}' unloaded.", addonId);
         return true;
      } else {
         return false;
      }
   }

   public static synchronized boolean load_Z(String addonId) {
      return addonId != null && load.containsKey(addonId);
   }

   private static DataUtil.TaskQueue init(String addonId, DataUtil.Inner_DataUtil status, DataUtil.StreamUtil report) {
      return new DataUtil.TaskQueue(addonId, status.name(), report.sync_Str(), init(report));
   }

   private static List<String> init(DataUtil.StreamUtil report) {
      if (report == null) {
         return List.of();
      }

      List<String> entries = new ArrayList<>();

      for (Entry<String, Integer> entry : report.init_Map().entrySet()) {
         entries.add(entry.getKey() + " x" + entry.getValue());
      }

      return Collections.unmodifiableList(entries);
   }

   private static void init(String addonId, URLClassLoader loader) {
      try {
         loader.close();
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Addons] Failed to close class loader for addon '{}'", addonId, t);
      }
   }

   public static File init_File() {
      return FabricLoader.getInstance().getGameDir().resolve("mods").toFile();
   }

   public static boolean read(String addonId) {
      if (addonId != null && !addonId.isBlank()) {
         AutismConfig config = AutismConfig.getGlobal();
         if (config.disabledAddonIds == null) {
            return false;
         }

         String needle = addonId.trim().toLowerCase(Locale.ROOT);

         for (String id : config.disabledAddonIds) {
            if (id != null && needle.equals(id.trim().toLowerCase(Locale.ROOT))) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static void init(String addonId, boolean disabled) {
      if (addonId != null && !addonId.isBlank()) {
         AutismConfig config = AutismConfig.getGlobal();
         if (config.disabledAddonIds == null) {
            config.disabledAddonIds = new ArrayList<>();
         }

         String normalized = addonId.trim();
         boolean currently = read(normalized);
         if (disabled && !currently) {
            config.disabledAddonIds.add(normalized);
         } else if (!disabled && currently) {
            config.disabledAddonIds.removeIf(id -> id != null && normalized.equalsIgnoreCase(id.trim()));
         }

         config.save();
      }
   }

   public static void init_V(String kind, String id) {
      DataUtil.StreamUtil report = init_Map.get(read());
      if (report != null) {
         report.load(kind, id);
      }
   }

   public static void init(String addonId, String kind, String id, String reason) {
      DataUtil.StreamUtil report = init_Map.get(addonId != null && !addonId.isBlank() ? addonId : read());
      if (report != null) {
         report.init(kind, id, reason);
      }
   }

   public static void load_V(String addonId, String detail) {
      DataUtil.StreamUtil report = init_Map.get(addonId);
      if (report != null) {
         report.sync(detail);
      }
   }

   public static void load_V() {
      if (!init_Z) {
         int issues = init_I();
         if (issues > 0) {
            init_Z = true;
            bi.read(issues + " addon issue(s). Open Addons for details.");
         }
      }
   }

   private static int init_I() {
      int count = 0;

      for (DataUtil.StreamUtil report : init_Map.values()) {
         if (report.init_DataUtil_DataUtil().init_Z()) {
            count++;
         }
      }

      return count;
   }

   private static String init(Throwable t) {
      if (t == null) {
         return "Unknown error";
      }

      String msg = t.getMessage();
      return msg != null && !msg.isBlank() ? t.getClass().getSimpleName() + ": " + msg : t.getClass().getSimpleName();
   }

   private record BufferHelper(String init, String load) {
   }

   private record CachePool(String init_Str, AutismAddon init_AutismAddon) {
   }

   private record ConfigStore(AutismAddon init_AutismAddon, URLClassLoader init_URLClassLoader) {
   }

   public enum Inner_DataUtil {
      LOADED("Loaded", false),
      DISABLED("Disabled on restart", false),
      FAILED("Failed", true),
      NEEDS_NEWER_API("Needs newer API", true),
      MISSING_API_VERSION("Missing apiVersion", true);

      private final String init_Str;
      private final boolean init_Z;

      Inner_DataUtil(String label, boolean issue) {
         this.init_Str = label;
         this.init_Z = issue;
      }

      public String init_Str() {
         return this.init_Str;
      }

      public boolean init_Z() {
         return this.init_Z;
      }
   }

   public static final class StreamUtil {
      private final String init_Str;
      private String load_Str;
      private String read_Str;
      private String write_Str;
      private int init_I;
      private int load_I = -1;
      private int read_I = 4;
      private DataUtil.Inner_DataUtil init_DataUtil_DataUtil = DataUtil.Inner_DataUtil.FAILED;
      private String update = "discovered";
      private String sync = "";
      private final Map<String, Integer> init_Map = new LinkedHashMap<>();
      private final Map<String, Integer> load_Map = new LinkedHashMap<>();
      private final List<String> init_List = new ArrayList<>();
      private final List<String> load_List = new ArrayList<>();
      private int write_I;
      private String flush = "";
      private boolean init_Z;

      private StreamUtil(String modId, String name, String version, String authors, int color) {
         this.init_Str = modId == null ? "" : modId;
         this.load_Str = name != null && !name.isBlank() ? name : this.init_Str;
         this.read_Str = version == null ? "" : version;
         this.write_Str = authors == null ? "" : authors;
         this.init_I = color;
      }

      private void init(AutismAddon addon) {
         if (addon != null) {
            if (addon.name != null && !addon.name.isBlank()) {
               this.load_Str = addon.name;
            }

            if (addon.authors != null && !addon.authors.isBlank()) {
               this.write_Str = addon.authors;
            }

            this.init_I = addon.color;
         }
      }

      private void init(int apiVersion) {
         this.load_I = apiVersion;
      }

      private void init(String phase) {
         this.update = phase != null && !phase.isBlank() ? phase : this.update;
      }

      private void init_V() {
         this.init_DataUtil_DataUtil = DataUtil.Inner_DataUtil.LOADED;
         this.update = "loaded";
         this.sync = "";
      }

      private void load(String reason) {
         this.init_DataUtil_DataUtil = DataUtil.Inner_DataUtil.DISABLED;
         this.update = "disabled";
         this.sync = reason == null ? "" : reason;
      }

      private void init(String phase, String reason) {
         this.init_DataUtil_DataUtil = DataUtil.Inner_DataUtil.FAILED;
         this.update = phase != null && !phase.isBlank() ? phase : "failed";
         this.sync = reason == null ? "" : reason;
      }

      private void read(String reason) {
         this.init_DataUtil_DataUtil = DataUtil.Inner_DataUtil.NEEDS_NEWER_API;
         this.update = "apiVersion";
         this.sync = reason == null ? "" : reason;
      }

      private void write(String reason) {
         this.init_DataUtil_DataUtil = DataUtil.Inner_DataUtil.MISSING_API_VERSION;
         this.update = "apiVersion";
         this.sync = reason == null ? "" : reason;
      }

      private void load_V() {
         this.init_Z = true;
      }

      private void load(String kind, String id) {
         String key = kind != null && !kind.isBlank() ? kind : "item";
         this.init_Map.merge(key, 1, Integer::sum);
      }

      private void init(String kind, String id, String reason) {
         String key = kind != null && !kind.isBlank() ? kind : "item";
         this.load_Map.merge(key, 1, Integer::sum);
         String detail = key + (id != null && !id.isBlank() ? " " + id : "") + ": " + (reason != null && !reason.isBlank() ? reason : "rejected");
         this.init_List.add(detail);
      }

      private void update(String detail) {
         if (detail != null && !detail.isBlank()) {
            this.load_List.add(detail);
         }
      }

      private void sync(String detail) {
         this.write_I++;
         this.flush = detail != null && !detail.isBlank() ? detail : "Runtime error";
      }

      public String init_Str() {
         return this.init_Str;
      }

      public String load_Str() {
         return this.load_Str;
      }

      public String read_Str() {
         return this.read_Str;
      }

      public String write_Str() {
         return this.write_Str;
      }

      public int init_I() {
         return this.init_I;
      }

      public int load_I() {
         return this.load_I;
      }

      public int read_I() {
         return this.read_I;
      }

      public DataUtil.Inner_DataUtil init_DataUtil_DataUtil() {
         return this.init_DataUtil_DataUtil;
      }

      public String update_Str() {
         return this.update;
      }

      public String sync_Str() {
         return this.sync;
      }

      public int write_I() {
         return this.write_I;
      }

      public String flush() {
         return this.flush;
      }

      public boolean init_Z() {
         return this.init_Z;
      }

      public Map<String, Integer> init_Map() {
         return Collections.unmodifiableMap(this.init_Map);
      }

      public Map<String, Integer> load_Map() {
         return Collections.unmodifiableMap(this.load_Map);
      }

      public List<String> init_List() {
         return Collections.unmodifiableList(this.init_List);
      }

      public List<String> load_List() {
         return Collections.unmodifiableList(this.load_List);
      }

      public int update_I() {
         int total = 0;

         for (int value : this.init_Map.values()) {
            total += value;
         }

         return total;
      }

      public int sync_I() {
         int total = 0;

         for (int value : this.load_Map.values()) {
            total += value;
         }

         return total;
      }

      public String close() {
         if (this.init_Map.isEmpty()) {
            return "No registered extensions";
         }

         List<String> parts = new ArrayList<>();

         for (Entry<String, Integer> entry : this.init_Map.entrySet()) {
            parts.add(entry.getValue() + " " + entry.getKey());
         }

         return String.join(", ", parts);
      }

      public String open() {
         StringBuilder sb = new StringBuilder();
         sb.append("Addon: ").append(this.load_Str).append('\n');
         sb.append("ID: ").append(this.init_Str).append('\n');
         if (!this.read_Str.isBlank()) {
            sb.append("Version: ").append(this.read_Str).append('\n');
         }

         if (!this.write_Str.isBlank()) {
            sb.append("Authors: ").append(this.write_Str).append('\n');
         }

         sb.append("Status: ").append(this.init_DataUtil_DataUtil.init_Str()).append('\n');
         sb.append("Phase: ").append(this.update).append('\n');
         sb.append("API: addon v").append(this.load_I < 0 ? "unknown" : Integer.toString(this.load_I)).append(", host v").append(this.read_I).append('\n');
         sb.append("Disabled on restart: ").append(DataUtil.read(this.init_Str)).append('\n');
         if (this.init_Z) {
            sb.append("Runtime: loaded in-game without restart").append('\n');
         }

         if (!this.sync.isBlank()) {
            sb.append("Reason: ").append(this.sync).append('\n');
         }

         sb.append("Accepted: ").append(this.init_Map).append('\n');
         if (!this.load_Map.isEmpty()) {
            sb.append("Rejected: ").append(this.load_Map).append('\n');
         }

         for (String detail : this.init_List) {
            sb.append("- ").append(detail).append('\n');
         }

         for (String detail : this.load_List) {
            sb.append("- ").append(detail).append('\n');
         }

         if (this.write_I > 0) {
            sb.append("Runtime errors: ").append(this.write_I).append('\n');
            sb.append("Last runtime error: ").append(this.flush).append('\n');
         }

         return sb.toString();
      }
   }

   public record TaskQueue(String init_Str, String load, String read, List<String> init_List) {
   }
}
