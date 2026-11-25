package autismclient.util;

import autismclient.AutismClientAddon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public final class AutismConfig implements Cloneable {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static volatile AutismConfig globalInstance;
   public static Runnable afterSave;
   static Consumer<AutismConfig> afterPersistenceSnapshot;
   public transient boolean sendGuiPackets = true;
   public transient boolean delayGuiPackets = false;
   public boolean useCustomPackets = false;
   public List<String> c2sPackets = new ArrayList<>();
   public List<String> s2cPackets = new ArrayList<>();
   public List<String> packetLoggerBlocked = new ArrayList<>();
   public boolean packetLoggerBlockedInit = false;
   public int packetLoggerBlockedDefaultsVersion = 0;
   public List<AutismConfig.PayloadChannelFilterRule> packetLoggerPayloadFilters = new ArrayList<>();
   public List<AutismConfig.PayloadChannelRegistrationRule> packetLoggerPayloadRegistrations = new ArrayList<>();
   @Deprecated
   public List<AutismConfig.PayloadChannelListenerRule> packetLoggerPayloadListeners = new ArrayList<>();
   public boolean payloadRegistrationUnlocked = false;
   public int payloadRegistrationWarningAcceptedVersion = 0;
   public boolean packetLoggerCapturing = false;
   public boolean allowSignEditing = true;
   public boolean autoDenyResourcePack = false;
   public boolean pretendPackAccepted = false;
   public int packResponseDelayMs = 20000;
   public boolean resourcePackChoiceInitialized = false;
   public boolean spoofClientVanilla = true;
   public boolean protectorEnabled = true;
   public boolean protectorSpoofBrand = true;
   public boolean protectorFilterChannels = true;
   public boolean protectorTranslationProtection = true;
   public boolean protectorDisableTelemetry = true;
   public boolean protectorBlockLocalUrls = true;
   public boolean protectorIsolatePackCache = true;
   public boolean protectorStripServerPacks = false;
   public boolean protectorChatSigningOff = false;
   public boolean performanceDebug = false;
   public boolean inventoryMove = false;
   public boolean xCarry = false;
   public boolean noPauseOnLostFocus = true;
   public boolean showItemIds = true;
   public boolean autoProbePlugins = false;
   public transient Map<String, AutismConfig.PluginScanCacheEntry> serverPluginScans = new LinkedHashMap<>();
   public boolean lanSyncEnabled = true;
   public boolean staggeredPacketSend = false;
   public int staggeredSendDelay = 1;
   public String executionPreset = "DEFAULT";
   public boolean packetBurstMode = true;
   public boolean useMsSleepMode = false;
   public int msSleepInterval = 5;
   public boolean instantExecutionMode = true;
   public int actionDelayUs = 0;
   public boolean useDirectFlush = true;
   public boolean forceChannelFlush = true;
   public boolean flushQueueOnDelayDisable = true;
   public boolean captureAsExact = false;
   public String commandPrefix = "";
   public boolean joinMacroEnabled = false;
   public String joinMacroName = "";
   public String joinMacroTiming = "PLAYABLE";
   public boolean joinMacroTimingPlayableDefault = false;
   public boolean clickDefaultsHuman = false;
   public boolean aimAssistReachDefaults = false;
   public boolean combatResponsiveDefaults = false;
   public boolean auraProtectionDefaults = false;
   public boolean triggerBotZeroReaction = false;
   public boolean combatOptimizedDefaults = false;
   public boolean combatFairPlayClickDefaults = false;
   public boolean combatSwingSpreadDefaults = false;
   public boolean killAuraEfficiencyDefaults = false;
   public boolean clutchRecoveryDefaults = false;
   public String joinMacroTriggerJoin = "FIRST";
   public boolean joinMacroKeepEnabled = false;
   public boolean stopMacroOnLeave = true;
   public Map<String, String> joinMacroFormValues = new LinkedHashMap<>();
   public Map<Integer, String> commandBinds = new LinkedHashMap<>();
   public Map<String, AutismConfig.ModuleState> modules = new LinkedHashMap<>();
   public List<String> hideRestoreModules = new ArrayList<>();
   public Map<String, AutismConfig.ModuleCategoryLayout> moduleCategoryLayouts = new LinkedHashMap<>();
   public Map<String, AutismConfig.WindowSize> windowSizes = new LinkedHashMap<>();
   public Map<String, List<String>> moduleCategoryOrder = new LinkedHashMap<>();
   public Map<String, AutismConfig.HudElementState> hudElements = new LinkedHashMap<>();
   public boolean hudLayoutMigrated = false;
   public boolean hudLayoutNormalizedV2 = false;
   public int hudSnapRange = 10;
   public int hudEdgePadding = 4;
   public boolean hudEditorGrid = true;
   public int keybindLoadGui = 86;
   public int keybindModuleMenu = 344;
   public int keybindAiAssistant = -1;
   public int keybindFlushQueue = -1;
   public int keybindClearQueue = -1;
   public int keybindToggleLogger = -1;
   public int keybindToggleSend = -1;
   public int keybindToggleDelay = -1;
   public boolean keybindInsideGui = false;
   public boolean customMainMenu = true;
   public boolean infiniChat = true;
   public boolean chatFeedback = true;
   public double overlayScale = 1.0;
   public double uiFontScale = 1.0;
   public String moduleMenuStyle = "DEFAULT";
   public int menuV2X = -1;
   public int menuV2Y = -1;
   public boolean classicMenuLayoutCustom = false;
   public int tpMaxPackets = 20;
   public int tpPauseMs = 500;
   public boolean voiceChatModdedPromptShown = false;
   public String activeProfileId = "";
   public List<String> disabledAddonIds = new ArrayList<>();
   public AutismConfig.ThemeColors themeColors = new AutismConfig.ThemeColors();
   private static final Object SAVE_LOCK = new Object();

   private static synchronized AutismConfig auditInitializeGlobal() {
      if (globalInstance == null) {
         setGlobal(load());
      }

      return globalInstance;
   }

   public static synchronized void setGlobal(AutismConfig config) {
      bK.init(config);
      autismclient.modules.aD.init(config);
      autismclient.g.HashUtil.init_V(config);
      globalInstance = config;
   }

   public static AutismConfig load() {
      AutismConfig config;
      label26: {
         File file = configFile();
         config = null;
         if (file.exists()) {
            config = tryRead(file);
            if (config != null) {
               break label26;
            }

            backupCorruptConfig();
         }

         File backup = backupFile();
         if (backup.exists()) {
            config = tryRead(backup);
            if (config != null) {
               AutismClientAddon.LOG.warn("Autism config was corrupt; recovered from backup {}", backup.getName());
            }
         }
      }

      if (config == null) {
         config = new AutismConfig();
      }

      Map<String, AutismConfig.PluginScanCacheEntry> legacyScans = config.serverPluginScans;
      ServerPluginScanCache cache = ServerPluginScanCache.init_ServerPluginScanCa();
      boolean migrated = cache.init_Z_Map(legacyScans);
      config.serverPluginScans = cache.init_Map();
      config.applyRuntimeDefaults();
      if (migrated) {
         config.save();
      }

      return config;
   }

   private static AutismConfig tryRead(File file) {
      try (FileReader reader = new FileReader(file)) {
         JsonElement root = JsonParser.parseReader(reader);
         AutismConfig loaded = GSON.fromJson(root, AutismConfig.class);
         if (loaded != null && root instanceof JsonObject object && object.has("serverPluginScans")) {
            Map<String, AutismConfig.PluginScanCacheEntry> legacy = GSON.fromJson(
               object.get("serverPluginScans"), (new TypeToken<Map<String, AutismConfig.PluginScanCacheEntry>>() {}).getType()
            );
            loaded.serverPluginScans = legacy == null ? new LinkedHashMap<>() : legacy;
         }

         return loaded != null ? loaded : null;
      } catch (Throwable t) {
         AutismClientAddon.LOG.error("Failed to read Autism config from {}", file.getName(), t);
         return null;
      }
   }

   private static File backupFile() {
      return new File(configFile().getParentFile(), "config.json.bak");
   }

   static File configFile() {
      return new File(AutismClientAddon.FOLDER, "config.json");
   }

   private static void backupCorruptConfig() {
      File file = configFile();

      try {
         File corrupt = new File(file.getParentFile(), "config.json.corrupt-" + System.currentTimeMillis());
         Files.move(file.toPath(), corrupt.toPath(), StandardCopyOption.REPLACE_EXISTING);
         AutismClientAddon.LOG.error("Autism config was corrupt; moved it to {}", corrupt.getName());
      } catch (Throwable t) {
         AutismClientAddon.LOG.error("Failed to set aside corrupt Autism config", t);
      }
   }

   public void save() {
      bK.init(this);
      autismclient.modules.aD.init(this);
      autismclient.g.HashUtil.init_V(this);
      B.init(this);
      Runnable hook = AuditConfigSafety.deferHook(afterSave);
      if (hook != null) {
         try {
            hook.run();
         } catch (Throwable t) {
            AutismClientAddon.LOG.error("Autism config afterSave hook failed", t);
         }
      }
   }

   public static void enqueuePendingSaveNow() {
      B.init();
      if (!AutismLiteVariant.enabled()) {
         bV.init_V();
      }
   }

   public static void flushPendingSaves(long timeoutMs) {
      enqueuePendingSaveNow();
      B.init(timeoutMs);
   }

   static String toJson(AutismConfig config) {
      return toJson(config, ServerPluginScanCache.init_ServerPluginScanCa());
   }

   static String toJson(AutismConfig config, ServerPluginScanCache pluginScanCache) {
      Map<String, AutismConfig.PluginScanCacheEntry> fallback = pluginScanCache.load_Map();
      if (fallback.isEmpty()) {
         return GSON.toJson(config);
      }

      JsonObject root = GSON.toJsonTree(config).getAsJsonObject();
      root.add("serverPluginScans", GSON.toJsonTree(fallback));
      return GSON.toJson(root);
   }

   static void onPersistenceSnapshot(AutismConfig snapshot) {
      Consumer<AutismConfig> hook = afterPersistenceSnapshot;
      if (hook != null) {
         try {
            hook.accept(snapshot);
         } catch (Throwable t) {
            AutismClientAddon.LOG.error("Autism config snapshot hook failed", t);
         }
      }
   }

   static void writeToDisk(String json) {
      synchronized (SAVE_LOCK) {
         long perfStart = bK.init_J();
         File file = configFile();
         file.getParentFile().mkdirs();
         File tmp = new File(file.getParentFile(), "config.json.tmp");

         // The config holds secrets such as the AutoLogin password, so it and its backup are owner-only.
         try {
            autismclient.util.mm.a.DataUtil.auditPrivateFile(tmp);
            try (FileWriter writer = new FileWriter(tmp)) {
               writer.write(json);
            }
         } catch (Throwable t) {
            AutismClientAddon.LOG.error("Failed to write Autism config", t);
            tmp.delete();
            return;
         }

         try {
            if (file.exists() && AuditConfigSafety.backupConfig(tryRead(file), backupFile()) != null) {
               autismclient.util.mm.a.DataUtil.auditCopyPrivateAtomic(file, backupFile());
            }
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("Failed to back up Autism config before save", t);
         }

         try {
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (Throwable atomicFailed) {
            try {
               Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Throwable t) {
               AutismClientAddon.LOG.error("Failed to swap in Autism config", t);
            }
         }

         bK.load("config.write", perfStart, 100000000L);
      }
   }

   public AutismConfig deepCopy() {
      return A.init(this);
   }

   public AutismConfig snapshotForProfile() {
      return stripMachineStateForProfile(this.deepCopy());
   }

   static AutismConfig profileSnapshotFromDetached(AutismConfig detached) {
      return detached == null ? stripMachineStateForProfile(new AutismConfig()) : stripMachineStateForProfile(detached.shallowDetachedCopy());
   }

   private AutismConfig shallowDetachedCopy() {
      try {
         return (AutismConfig)super.clone();
      } catch (CloneNotSupportedException impossible) {
         throw new AssertionError(impossible);
      }
   }

   private static AutismConfig stripMachineStateForProfile(AutismConfig copy) {
      copy.activeProfileId = "";
      copy.packetLoggerCapturing = false;
      copy.serverPluginScans = new LinkedHashMap<>();
      return copy;
   }

   public static boolean sameThemeColors(AutismConfig a, AutismConfig b) {
      return a != null && b != null ? GSON.toJson(a.themeColors).equals(GSON.toJson(b.themeColors)) : a == b;
   }

   public static boolean samePayloadRules(AutismConfig a, AutismConfig b) {
      return a != null && b != null
         ? GSON.toJson(a.packetLoggerPayloadFilters).equals(GSON.toJson(b.packetLoggerPayloadFilters))
            && GSON.toJson(a.packetLoggerPayloadRegistrations).equals(GSON.toJson(b.packetLoggerPayloadRegistrations))
         : a == b;
   }

   private void moveUntouched(String moduleId, String settingId, String oldDefault, String newDefault) {
      AutismConfig.ModuleState state = this.modules == null ? null : this.modules.get(moduleId);
      if (state != null && state.settings != null && oldDefault.equals(state.settings.get(settingId))) {
         state.settings.put(settingId, newDefault);
      }
   }

   private void migrateAimAssist() {
      AutismConfig.ModuleState state = this.modules == null ? null : this.modules.get("aim-assist");
      if (state != null && state.settings != null) {
         Map<String, String> settings = state.settings;
         rescaleStrength(settings, "horizontal-speed", "40", 0.4);
         rescaleStrength(settings, "vertical-speed", "10", 0.1);
         if ("Interpolation".equals(settings.get("smooth-mode"))) {
            settings.put("smooth-mode", "Human");
         }

         if ("true".equals(settings.get("only-while-moving"))) {
            settings.put("only-while-moving", "false");
         }

         String axis = settings.remove("axis");
         if (axis != null) {
            settings.put("x-axis", String.valueOf(!"Vertical".equals(axis)));
            settings.put("y-axis", String.valueOf("Vertical".equals(axis)));
         }
      }
   }

   private static void rescaleStrength(Map<String, String> settings, String id, String oldDefault, double oldMax) {
      String value = settings.get(id);
      if (value != null) {
         if (oldDefault.equals(value)) {
            settings.put(id, "33");
         } else {
            try {
               int old = (int)Math.round(Double.parseDouble(value));
               settings.put(id, String.valueOf(Math.max(1, Math.min(100, (int)Math.round(old / (oldMax * 100.0) * 100.0)))));
            } catch (NumberFormatException ignored) {
               settings.remove(id);
            }
         }
      }
   }

   private void strengthenAimDefault(String id) {
      AutismConfig.ModuleState state = this.modules == null ? null : this.modules.get("aim-assist");
      if (state != null && state.settings != null) {
         String old = state.settings.get(id);
         if (old != null) {
            if ("33".equals(old)) {
               state.settings.put(id, "80");
            } else {
               try {
                  int strength = (int)Math.round(Double.parseDouble(old) / 2.0);
                  state.settings.put(id, String.valueOf(Math.max(1, Math.min(100, strength))));
               } catch (NumberFormatException ignored) {
                  state.settings.remove(id);
               }
            }
         }
      }
   }

   static void migrateTellySmoothness(Map<String, String> settings) {
      if (settings != null) {
         String old = settings.remove("rotation-ticks");
         if (old != null && !settings.containsKey("rotation-smoothness")) {
            try {
               int ticks = Math.max(2, Math.min(4, Integer.parseInt(old)));
               int percent = (int)Math.floor((ticks - 1) * 100.0 / 3.0);
               settings.put("rotation-smoothness", percent + "," + percent);
            } catch (NumberFormatException var4) {
            }
         }
      }
   }

   private void migrateAuraRotationSpeeds() {
      for (String id : new String[]{"kill-aura", "crystal-aura", "anchor-aura"}) {
         AutismConfig.ModuleState state = this.modules.get(id);
         if (state != null && state.settings != null) {
            String old = state.settings.remove("rotation-speed");
            if (old != null && !state.settings.containsKey("rotation-speed-multiplier")) {
               try {
                  double multiplier = Math.clamp(Integer.parseInt(old), 1, 5) / 2.0;
                  state.settings.put("rotation-speed-multiplier", Double.toString(multiplier));
               } catch (NumberFormatException var9) {
               }
            }
         }
      }
   }

   public void applyRuntimeDefaults() {
      if (!this.auraProtectionDefaults) {
         this.auraProtectionDefaults = true;

         for (String module : new String[]{"crystal-aura", "anchor-aura"}) {
            for (String stage : new String[]{"place-delay", "destroy-delay", "step-delay", "charge-delay", "detonate-delay"}) {
               this.moveUntouched(module, stage, "75", "0");
            }

            this.moveUntouched(module, "jitter", "20", "0");
            this.moveUntouched(module, "step-jitter", "20", "0");
         }

         this.moveUntouched("anchor-aura", "force-engage", "true", "false");
         this.moveUntouched("backtrack", "position-color", "5535C9F2", "C035C9F2");
      }

      if (!this.clickDefaultsHuman) {
         this.clickDefaultsHuman = true;
         this.moveUntouched("trigger-bot", "swing-offset", "-10,30", "-10,120");
         this.moveUntouched("kill-aura", "swing-offset", "-20,40", "-10,120");
         this.moveUntouched("auto-clicker", "technique", "Normal", "Butterfly");
      }

      if (!this.aimAssistReachDefaults) {
         this.aimAssistReachDefaults = true;
         this.moveUntouched("trigger-bot", "pre-reach", "0.3,0.7", "0.1,0.4");
         this.moveUntouched("kill-aura", "pre-reach", "0.3,0.7", "0.1,0.4");
         this.migrateAimAssist();
      }

      if (!this.combatResponsiveDefaults) {
         this.combatResponsiveDefaults = true;
         this.strengthenAimDefault("horizontal-speed");
         this.strengthenAimDefault("vertical-speed");
         this.moveUntouched("trigger-bot", "reaction", "120,260", "40,100");
         this.moveUntouched("trigger-bot", "legacy-reaction", "120,260", "40,100");
         this.moveUntouched("trigger-bot", "pre-reach", "0.1,0.4", "0.2,0.6");
         this.moveUntouched("kill-aura", "pre-reach", "0.1,0.4", "0.2,0.6");
      }

      if (!this.triggerBotZeroReaction) {
         this.triggerBotZeroReaction = true;
         this.moveUntouched("trigger-bot", "reaction", "40,100", "0,0");
         this.moveUntouched("trigger-bot", "legacy-reaction", "40,100", "0,0");
         this.moveUntouched("trigger-bot", "reaction", "120,260", "0,0");
         this.moveUntouched("trigger-bot", "legacy-reaction", "120,260", "0,0");
      }

      if (!this.clutchRecoveryDefaults) {
         this.clutchRecoveryDefaults = true;
         this.moveUntouched("clutch", "safe-fall", "1", "3");
         this.moveUntouched("clutch", "guard-time", "2", "0");
         this.moveUntouched("clutch", "guard-time", "2.0", "0");
      }

      if (!this.combatOptimizedDefaults) {
         this.combatOptimizedDefaults = true;
         this.moveUntouched("trigger-bot", "swing-offset", "-10,120", "-25,75");
         this.moveUntouched("kill-aura", "swing-offset", "-10,120", "-25,75");
         this.moveUntouched("trigger-bot", "hesitation", "2,8", "0,2");
         this.moveUntouched("trigger-bot", "butterfly-cps", "10,15", "14,19");
         this.moveUntouched("kill-aura", "butterfly-cps", "10,15", "14,19");
      }

      if (!this.combatFairPlayClickDefaults) {
         this.combatFairPlayClickDefaults = true;

         for (String module : new String[]{"kill-aura", "trigger-bot", "auto-clicker"}) {
            this.moveUntouched(module, "butterfly-cps", "14,19", "12,14.5");
            this.moveUntouched(module, "butterfly-cps", "10,15", "12,14.5");
            this.moveUntouched(module, "drag-cps", "12,18", "12,14.5");
            this.moveUntouched(module, "scroll-cps", "10,16", "10,14.5");
         }
      }

      if (!this.combatSwingSpreadDefaults) {
         this.combatSwingSpreadDefaults = true;
         this.moveUntouched("trigger-bot", "swing-offset", "-25,75", "-50,125");
         this.moveUntouched("kill-aura", "swing-offset", "-25,75", "-50,125");
      }

      if (!this.killAuraEfficiencyDefaults) {
         this.killAuraEfficiencyDefaults = true;
         this.moveUntouched("kill-aura", "miss-chance", "8", "0");
         this.moveUntouched("kill-aura", "swing-offset", "-50,125", "-100,50");
         this.moveUntouched("kill-aura", "swing-offset", "-25,75", "-100,50");
         this.moveUntouched("trigger-bot", "swing-offset", "-50,125", "-75,50");
         this.moveUntouched("trigger-bot", "swing-offset", "-25,75", "-75,50");
      }

      if (!this.joinMacroTimingPlayableDefault) {
         this.joinMacroTimingPlayableDefault = true;
         if (this.joinMacroTiming == null || this.joinMacroTiming.isBlank() || "WORLD".equalsIgnoreCase(this.joinMacroTiming)) {
            this.joinMacroTiming = "PLAYABLE";
         }
      }

      this.sendGuiPackets = true;
      this.delayGuiPackets = false;
      this.staggeredPacketSend = false;
      if (this.c2sPackets == null) {
         this.c2sPackets = new ArrayList<>();
      }

      if (this.s2cPackets == null) {
         this.s2cPackets = new ArrayList<>();
      }

      if (this.packetLoggerPayloadFilters == null) {
         this.packetLoggerPayloadFilters = new ArrayList<>();
      }

      if (this.packetLoggerPayloadRegistrations == null) {
         this.packetLoggerPayloadRegistrations = new ArrayList<>();
      }

      if (this.packetLoggerPayloadListeners == null) {
         this.packetLoggerPayloadListeners = new ArrayList<>();
      }

      if (this.packetLoggerBlocked == null) {
         this.packetLoggerBlocked = new ArrayList<>();
      }

      if (this.joinMacroFormValues == null) {
         this.joinMacroFormValues = new LinkedHashMap<>();
      }

      if (this.commandBinds == null) {
         this.commandBinds = new LinkedHashMap<>();
      }

      if (this.packetLoggerPayloadFilters.isEmpty() && !this.packetLoggerPayloadListeners.isEmpty()) {
         for (AutismConfig.PayloadChannelListenerRule oldRule : this.packetLoggerPayloadListeners) {
            if (oldRule != null) {
               AutismConfig.PayloadChannelFilterRule rule = new AutismConfig.PayloadChannelFilterRule();
               rule.label = oldRule.label;
               rule.pattern = oldRule.pattern;
               rule.enabled = oldRule.enabled;
               rule.preset = oldRule.preset;
               this.packetLoggerPayloadFilters.add(rule);
            }
         }
      }

      if (!this.packetLoggerPayloadListeners.isEmpty()) {
         this.packetLoggerPayloadListeners = new ArrayList<>();
      }

      if (this.modules == null) {
         this.modules = new LinkedHashMap<>();
      }

      this.migrateAuraRotationSpeeds();
      AutismConfig.ModuleState scaffold = this.modules.get("scaffold");
      if (scaffold != null) {
         migrateTellySmoothness(scaffold.settings);
      }

      if (this.hideRestoreModules == null) {
         this.hideRestoreModules = new ArrayList<>();
      }

      if (this.moduleCategoryLayouts == null) {
         this.moduleCategoryLayouts = new LinkedHashMap<>();
      }

      if (this.windowSizes == null) {
         this.windowSizes = new LinkedHashMap<>();
      }

      if (this.moduleCategoryOrder == null) {
         this.moduleCategoryOrder = new LinkedHashMap<>();
      }

      if (this.hudElements == null) {
         this.hudElements = new LinkedHashMap<>();
      }

      if (this.serverPluginScans == null) {
         this.serverPluginScans = new LinkedHashMap<>();
      }

      if (this.disabledAddonIds == null) {
         this.disabledAddonIds = new ArrayList<>();
      }

      if (this.themeColors == null) {
         this.themeColors = new AutismConfig.ThemeColors();
      }

      this.overlayScale = cA.load_D_D(this.overlayScale);
      if (!(this.uiFontScale >= 0.75) || !(this.uiFontScale <= 1.4)) {
         this.uiFontScale = 1.0;
      }

      if (!"CSGO".equals(this.moduleMenuStyle)) {
         this.moduleMenuStyle = "DEFAULT";
      }

      this.tpMaxPackets = Math.max(1, Math.min(100, this.tpMaxPackets));
      this.tpPauseMs = Math.max(50, Math.min(10000, this.tpPauseMs));
      if (this.hudSnapRange <= 0) {
         this.hudSnapRange = 10;
      }

      if (this.hudEdgePadding < 0) {
         this.hudEdgePadding = 4;
      }

      this.commandPrefix = z.init_Str(this.commandPrefix);
      if (!this.resourcePackChoiceInitialized) {
         this.pretendPackAccepted = false;
         this.resourcePackChoiceInitialized = true;
      }

      for (AutismConfig.HudElementState state : this.hudElements.values()) {
         if (state != null && state.settings == null) {
            state.settings = new LinkedHashMap<>();
         }
      }
   }

   private static String pluginScanKey(String address, String contextSignature) {
      String a = address == null ? "" : address.trim().toLowerCase(Locale.ROOT);
      String c = contextSignature == null ? "" : contextSignature.trim().toLowerCase(Locale.ROOT);
      return c.isEmpty() ? a : a + "|" + Integer.toHexString(c.hashCode());
   }

   public AutismConfig.PluginScanCacheEntry getPluginScan(String address, String contextSignature) {
      return address != null && !address.isBlank()
         ? ServerPluginScanCache.init_ServerPluginScanCa().init_AutismConfig_Plugi_Str(pluginScanKey(address, contextSignature))
         : null;
   }

   public long getPluginScanTimestamp(String address, String contextSignature) {
      AutismConfig.PluginScanCacheEntry e = this.getPluginScan(address, contextSignature);
      return e == null ? 0L : e.scannedAtMs;
   }

   public AutismConfig.PluginScanCacheEntry getPluginScanByAddress(String address) {
      return address != null && !address.isBlank()
         ? ServerPluginScanCache.init_ServerPluginScanCa().load_AutismConfig_Plugi_Str(address.trim().toLowerCase(Locale.ROOT))
         : null;
   }

   public long getPluginScanTimestampByAddress(String address) {
      AutismConfig.PluginScanCacheEntry e = this.getPluginScanByAddress(address);
      return e == null ? 0L : e.scannedAtMs;
   }

   public Map<String, AutismConfig.PluginScanCacheEntry> allPluginScans() {
      return ServerPluginScanCache.init_ServerPluginScanCa().read_Map();
   }

   public long pluginScanRevision() {
      return ServerPluginScanCache.init_ServerPluginScanCa().init_J();
   }

   public void putPluginScan(String address, String contextSignature, List<String> plugins, Map<String, List<String>> commands, Map<String, String> evidence) {
      this.putPluginScan(address, contextSignature, plugins, commands, evidence, Map.of());
   }

   public void putPluginScan(
      String address,
      String contextSignature,
      List<String> plugins,
      Map<String, List<String>> commands,
      Map<String, String> evidence,
      Map<String, List<String>> channels
   ) {
      this.putPluginScan(address, contextSignature, plugins, commands, evidence, channels, Map.of(), Map.of());
   }

   public void putPluginScan(
      String address,
      String contextSignature,
      List<String> plugins,
      Map<String, List<String>> commands,
      Map<String, String> evidence,
      Map<String, List<String>> channels,
      Map<String, List<String>> guis,
      Map<String, String> confidence
   ) {
      this.putPluginScan(address, contextSignature, plugins, commands, evidence, channels, guis, confidence, Map.of(), Map.of());
   }

   public void putPluginScan(
      String address,
      String contextSignature,
      List<String> plugins,
      Map<String, List<String>> commands,
      Map<String, String> evidence,
      Map<String, List<String>> channels,
      Map<String, List<String>> guis,
      Map<String, String> confidence,
      Map<String, String> copyEvidence,
      Map<String, List<String>> copyCommands
   ) {
      this.putPluginScan(
         address, contextSignature, plugins, commands, evidence, channels, guis, confidence, copyEvidence, copyCommands, "COMPLETE", 0, 0, 0, 0, "", ""
      );
   }

   public void putPluginScan(
      String address,
      String contextSignature,
      List<String> plugins,
      Map<String, List<String>> commands,
      Map<String, String> evidence,
      Map<String, List<String>> channels,
      Map<String, List<String>> guis,
      Map<String, String> confidence,
      Map<String, String> copyEvidence,
      Map<String, List<String>> copyCommands,
      String scanStatus,
      int totalProbes,
      int answeredProbes,
      int retriedProbes,
      int failedProbes,
      String serverName,
      String serverAddress
   ) {
      if (address != null && !address.isBlank()) {
         AutismConfig.PluginScanCacheEntry e = new AutismConfig.PluginScanCacheEntry();
         e.contextSignature = contextSignature == null ? "" : contextSignature;
         e.serverName = serverName == null ? "" : serverName;
         e.serverAddress = serverAddress != null && !serverAddress.isBlank() ? serverAddress : address;
         e.plugins = plugins == null ? new ArrayList<>() : new ArrayList<>(plugins);
         e.commands = commands == null ? new LinkedHashMap<>() : new LinkedHashMap<>(commands);
         e.evidence = evidence == null ? new LinkedHashMap<>() : new LinkedHashMap<>(evidence);
         e.channels = channels == null ? new LinkedHashMap<>() : new LinkedHashMap<>(channels);
         e.guis = guis == null ? new LinkedHashMap<>() : new LinkedHashMap<>(guis);
         e.confidence = confidence == null ? new LinkedHashMap<>() : new LinkedHashMap<>(confidence);
         e.copyEvidence = copyEvidence == null ? new LinkedHashMap<>() : new LinkedHashMap<>(copyEvidence);
         e.copyCommands = copyCommands == null ? new LinkedHashMap<>() : new LinkedHashMap<>(copyCommands);
         e.scanStatus = scanStatus != null && !scanStatus.isBlank() ? scanStatus : "COMPLETE";
         e.totalProbes = Math.max(0, totalProbes);
         e.answeredProbes = Math.max(0, answeredProbes);
         e.retriedProbes = Math.max(0, retriedProbes);
         e.failedProbes = Math.max(0, failedProbes);
         e.scannedAtMs = System.currentTimeMillis();
         ServerPluginScanCache.init_ServerPluginScanCa().init_V_Str_AutismConfig_Plugi(pluginScanKey(address, contextSignature), e);
         this.serverPluginScans = ServerPluginScanCache.init_ServerPluginScanCa().init_Map();
      }
   }

   public void removePluginScan(String address, String contextSignature) {
      if (address != null) {
         ServerPluginScanCache.init_ServerPluginScanCa().init_V_Str(pluginScanKey(address, contextSignature));
         this.serverPluginScans = ServerPluginScanCache.init_ServerPluginScanCa().init_Map();
      }
   }

   public void removePluginScan(String address) {
      if (address != null) {
         String a = pluginScanKey(address, "");
         ServerPluginScanCache.init_ServerPluginScanCa().load_V_Str(a);
         this.serverPluginScans = ServerPluginScanCache.init_ServerPluginScanCa().init_Map();
      }
   }

   public static AutismConfig getGlobal() {
      AutismConfig current = globalInstance;
      if (current == null) {
         current = AuditConfigSafety.initialize(AutismConfig::auditInitializeGlobal);
      }

      return current;
   }

   public static final class HudElementState {
      public boolean enabled = true;
      public int f_x_I = 8;
      public int f_y_I = 8;
      public double scale = 1.0;
      public String anchor = "TOP_LEFT";
      public Map<String, String> settings = new LinkedHashMap<>();
   }

   public static final class ModuleCategoryLayout {
      public int f_x_I = -1;
      public int f_y_I = -1;
      public boolean collapsed = false;
      public int visibleRows = 0;
      public int width = 0;
      public int height = 0;
   }

   public static final class ModuleState {
      public boolean enabled = false;
      public int keybind = -1;
      public Map<String, String> settings = new LinkedHashMap<>();
   }

   public static class PayloadChannelFilterRule {
      public String label = "";
      public String pattern = "";
      public boolean enabled = true;
      public boolean preset = false;
   }

   @Deprecated
   public static final class PayloadChannelListenerRule extends AutismConfig.PayloadChannelFilterRule {
      public String direction = "ANY";
   }

   public static class PayloadChannelRegistrationRule {
      public String label = "";
      public String channel = "";
      public boolean enabled = true;
      public String source = "custom";
   }

   public static final class PluginScanCacheEntry {
      public String contextSignature = "";
      public String serverName = "";
      public String serverAddress = "";
      public List<String> plugins = new ArrayList<>();
      public Map<String, List<String>> commands = new LinkedHashMap<>();
      public Map<String, String> evidence = new LinkedHashMap<>();
      public Map<String, List<String>> channels = new LinkedHashMap<>();
      public Map<String, List<String>> guis = new LinkedHashMap<>();
      public Map<String, String> confidence = new LinkedHashMap<>();
      public Map<String, String> copyEvidence = new LinkedHashMap<>();
      public Map<String, List<String>> copyCommands = new LinkedHashMap<>();
      public String scanStatus = "COMPLETE";
      public int totalProbes = 0;
      public int answeredProbes = 0;
      public int retriedProbes = 0;
      public int failedProbes = 0;
      public long scannedAtMs = 0L;
   }

   public static final class ThemeColors {
      public boolean advanced = false;
      public int master = -50373;
      public int accent = -50373;
      public int outline = -5035221;
      public int text = -791321;
      public int toggle = -50373;
      public int backdrop = -5093304;
      public int success = -13248397;
      public int danger = -1938838;
      public int button = -7397596;
      public int header = -50373;
      public int hover = -39836;
   }

   public static final class WindowSize {
      public int width;
      public int height;
   }
}
