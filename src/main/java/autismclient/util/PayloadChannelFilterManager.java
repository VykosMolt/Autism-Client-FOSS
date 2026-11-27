package autismclient.util;

import autismclient.AutismClientAddon;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class PayloadChannelFilterManager {
   private static final List<PayloadChannelFilterManager.Inner_CachePool> PRESET_CATALOG = loadPresetCatalog();
   private static final List<String> CATALOG_WARNINGS = validateCatalog(PRESET_CATALOG);
   private final List<AutismConfig.PayloadChannelFilterRule> rules = new java.util.concurrent.CopyOnWriteArrayList<>();

   public PayloadChannelFilterManager() {
      this.reloadFromConfig();
   }

   public synchronized void reloadFromConfig() {
      this.rules.clear();
      AutismConfig config = AutismConfig.getGlobal();
      if (config.packetLoggerPayloadFilters == null) {
         config.packetLoggerPayloadFilters = new ArrayList<>();
      }

      Map<String, AutismConfig.PayloadChannelFilterRule> merged = new LinkedHashMap<>();

      for (AutismConfig.PayloadChannelFilterRule rule : config.packetLoggerPayloadFilters) {
         AutismConfig.PayloadChannelFilterRule clean = sanitizeRule(rule);
         if (clean != null) {
            String key = ruleKey(clean.pattern);
            AutismConfig.PayloadChannelFilterRule existing = merged.get(key);
            if (existing == null) {
               merged.put(key, clean);
            } else {
               existing.enabled = existing.enabled | clean.enabled;
               existing.preset = existing.preset | clean.preset;
               if ((existing.label == null || existing.label.isBlank() || existing.label.equals(existing.pattern))
                  && clean.label != null
                  && !clean.label.isBlank()) {
                  existing.label = clean.label;
               }
            }
         }
      }

      this.rules.addAll(merged.values());
   }

   public List<AutismConfig.PayloadChannelFilterRule> getRules() {
      return Collections.unmodifiableList(this.rules);
   }

   List<AutismConfig.PayloadChannelFilterRule> getMutableRules() {
      return this.rules;
   }

   public List<PayloadChannelFilterManager.Inner_CachePool> getPresets() {
      return PRESET_CATALOG;
   }

   public static List<PayloadChannelFilterManager.Inner_CachePool> presets() {
      return PRESET_CATALOG;
   }

   public static List<String> catalogWarnings() {
      return CATALOG_WARNINGS;
   }

   public static boolean matchesPreset(PayloadChannelFilterManager.Inner_CachePool preset, String channelName) {
      return preset == null ? false : matchesNormalized(preset.read(), normalizeChannel(channelName));
   }

   public static boolean matchesPattern(String pattern, String channelName) {
      return matchesNormalized(pattern, normalizeChannel(channelName));
   }

   public static boolean isExactPluginChannel(PayloadChannelFilterManager.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      if (preset.kind() != PayloadChannelFilterManager.Inner_BufferHelper.EXACT) {
         return false;
      }

      String label = preset.load() == null ? "" : preset.load().trim().toLowerCase(Locale.ROOT);
      String pattern = normalizePattern(preset.read());
      if (pattern.isBlank() || "Minecraft".equalsIgnoreCase(preset.write())) {
         return false;
      }

      if (!label.contains("family") && !label.contains("namespace")) {
         int colon = pattern.indexOf(58);
         if (colon <= 0) {
            return false;
         }

         String namespace = pattern.substring(0, colon);
         return !isGenericServerNamespace(namespace);
      } else {
         return false;
      }
   }

   private static boolean isGenericServerNamespace(String namespace) {
      boolean var10000;
      switch (namespace == null ? "" : namespace.toLowerCase(Locale.ROOT)) {
         case "ah":
         case "auction":
         case "auctions":
         case "auctionhouse":
         case "shop":
         case "shops":
         case "store":
         case "stores":
         case "crate":
         case "crates":
         case "casino":
         case "slots":
         case "roulette":
         case "gui":
         case "menu":
         case "menus":
         case "inventory":
         case "inventories":
         case "chest":
         case "chests":
         case "storage":
         case "container":
         case "containers":
         case "backpack":
         case "backpacks":
         case "shulker":
         case "shulkers":
         case "enderchest":
         case "echest":
         case "pv":
         case "vault":
         case "vaults":
         case "minion":
         case "minions":
         case "mine":
         case "mines":
         case "claim":
         case "claims":
         case "region":
         case "regions":
         case "protection":
         case "protections":
         case "c":
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   public boolean addRuleAndSave(String label, String pattern, PayloadChannelFilterManager.Inner_DataUtil direction) {
      return this.upsertRuleAndSave(label, pattern, direction, false, true);
   }

   public boolean addRuleInMemory(String label, String pattern, PayloadChannelFilterManager.Inner_DataUtil direction) {
      return this.upsertRuleInMemory(label, pattern, direction, false, true);
   }

   public boolean addPresetRuleAndSave(PayloadChannelFilterManager.Inner_CachePool preset) {
      return preset == null ? false : this.upsertRuleAndSave(preset.load(), preset.read(), preset.direction(), true, true);
   }

   public boolean enablePresetAndSave(PayloadChannelFilterManager.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      String key = preset.init_Str();

      for (AutismConfig.PayloadChannelFilterRule rule : this.rules) {
         if (ruleKey(rule.pattern).equals(key)) {
            if (!rule.enabled) {
               rule.enabled = true;
               this.saveToConfig();
               return true;
            }

            return false;
         }
      }

      return this.addPresetRuleAndSave(preset);
   }

   public boolean enablePresetInMemory(PayloadChannelFilterManager.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      String key = preset.init_Str();

      for (AutismConfig.PayloadChannelFilterRule rule : this.rules) {
         if (ruleKey(rule.pattern).equals(key)) {
            boolean changed = false;
            if (!rule.enabled) {
               rule.enabled = true;
               changed = true;
            }

            if (!rule.preset) {
               rule.preset = true;
               changed = true;
            }

            return changed;
         }
      }

      return this.upsertRuleInMemory(preset.load(), preset.read(), preset.direction(), true, true);
   }

   public void toggleRuleAndSave(int index) {
      if (index >= 0 && index < this.rules.size()) {
         AutismConfig.PayloadChannelFilterRule rule = this.rules.get(index);
         rule.enabled = !rule.enabled;
         this.saveToConfig();
      }
   }

   public void toggleRuleInMemory(int index) {
      if (index >= 0 && index < this.rules.size()) {
         AutismConfig.PayloadChannelFilterRule rule = this.rules.get(index);
         if (rule != null) {
            rule.enabled = !rule.enabled;
         }
      }
   }

   public void removeRuleAndSave(int index) {
      if (index >= 0 && index < this.rules.size()) {
         this.rules.remove(index);
         this.saveToConfig();
      }
   }

   public void removeRuleInMemory(int index) {
      if (index >= 0 && index < this.rules.size()) {
         this.rules.remove(index);
      }
   }

   public synchronized void removeAllPresetRules() {
      boolean changed = this.rules.removeIf(rule -> rule != null && rule.preset);
      if (changed) {
         this.saveToConfig();
      }
   }

   public boolean hasPresetRule(PayloadChannelFilterManager.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      String key = preset.init_Str();

      for (AutismConfig.PayloadChannelFilterRule rule : this.rules) {
         if (ruleKey(rule.pattern).equals(key)) {
            return true;
         }
      }

      return false;
   }

   public boolean isPresetEnabled(PayloadChannelFilterManager.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      String key = preset.init_Str();

      for (AutismConfig.PayloadChannelFilterRule rule : this.rules) {
         if (ruleKey(rule.pattern).equals(key)) {
            return rule.enabled;
         }
      }

      return false;
   }

   public boolean hasAnyEnabledRule() {
      for (AutismConfig.PayloadChannelFilterRule rule : this.rules) {
         if (rule != null && rule.enabled) {
            return true;
         }
      }

      return false;
   }

   public List<String> getSubscribedChannels() {
      return this.getSubscribedChannels(false);
   }

   public List<String> getSubscribedChannels(boolean includeLoggerDefaults) {
      return PayloadSubscriptionManager.init(includeLoggerDefaults).channels();
   }

   public int countPresetsInGroup(String group) {
      if (group != null && !group.isBlank()) {
         int count = 0;

         for (PayloadChannelFilterManager.Inner_CachePool preset : PRESET_CATALOG) {
            if (group.equals(preset.write())) {
               count++;
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   public int countEnabledPresetsInGroup(String group) {
      if (group != null && !group.isBlank()) {
         int count = 0;

         for (PayloadChannelFilterManager.Inner_CachePool preset : PRESET_CATALOG) {
            if (group.equals(preset.write()) && this.isPresetEnabled(preset)) {
               count++;
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   public boolean isGroupFullyEnabled(String group) {
      int total = this.countPresetsInGroup(group);
      return total > 0 && this.countEnabledPresetsInGroup(group) == total;
   }

   public void toggleGroup(String group) {
      if (group != null && !group.isBlank()) {
         this.setGroupEnabled(group, !this.isGroupFullyEnabled(group));
      }
   }

   private void setGroupEnabled(String group, boolean enabled) {
      if (group != null && !group.isBlank()) {
         boolean changed = false;
         Set<String> groupKeys = new HashSet<>();

         for (PayloadChannelFilterManager.Inner_CachePool preset : PRESET_CATALOG) {
            if (group.equals(preset.write())) {
               groupKeys.add(preset.init_Str());
            }
         }

         if (!groupKeys.isEmpty()) {
            if (!enabled) {
               changed = this.rules.removeIf(rule -> rule != null && rule.preset && groupKeys.contains(ruleKey(rule.pattern)));
            } else {
               for (PayloadChannelFilterManager.Inner_CachePool preset : PRESET_CATALOG) {
                  if (group.equals(preset.write()) && this.ensurePresetEnabledInMemory(preset)) {
                     changed = true;
                  }
               }
            }

            if (changed) {
               this.saveToConfig();
            }
         }
      }
   }

   public void togglePresetAndSave(PayloadChannelFilterManager.Inner_CachePool preset) {
      if (preset != null) {
         String key = preset.init_Str();

         for (int i = 0; i < this.rules.size(); i++) {
            AutismConfig.PayloadChannelFilterRule rule = this.rules.get(i);
            if (ruleKey(rule.pattern).equals(key)) {
               if (rule.enabled) {
                  this.rules.remove(i);
                  this.saveToConfig();
               } else {
                  rule.enabled = true;
                  rule.preset = true;
                  this.saveToConfig();
               }

               return;
            }
         }

         this.addPresetRuleAndSave(preset);
      }
   }

   public void togglePresetInMemory(PayloadChannelFilterManager.Inner_CachePool preset) {
      if (preset != null) {
         String key = preset.init_Str();

         for (int i = 0; i < this.rules.size(); i++) {
            AutismConfig.PayloadChannelFilterRule rule = this.rules.get(i);
            if (ruleKey(rule.pattern).equals(key)) {
               if (rule.enabled) {
                  this.rules.remove(i);
               } else {
                  rule.enabled = true;
                  rule.preset = true;
               }

               return;
            }
         }

         this.upsertRuleInMemory(preset.load(), preset.read(), preset.direction(), true, true);
      }
   }

   private boolean ensurePresetEnabledInMemory(PayloadChannelFilterManager.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      String key = preset.init_Str();

      for (AutismConfig.PayloadChannelFilterRule rule : this.rules) {
         if (ruleKey(rule.pattern).equals(key)) {
            boolean changed = false;
            if (!rule.enabled) {
               rule.enabled = true;
               changed = true;
            }

            if (!rule.preset) {
               rule.preset = true;
               changed = true;
            }

            return changed;
         }
      }

      AutismConfig.PayloadChannelFilterRule rule = new AutismConfig.PayloadChannelFilterRule();
      rule.pattern = normalizePattern(preset.read());
      rule.label = preset.load();
      rule.enabled = true;
      rule.preset = true;
      this.rules.add(rule);
      return true;
   }

   public void enableAllRulesAndPresets() {
      boolean changed = false;

      for (AutismConfig.PayloadChannelFilterRule rule : this.rules) {
         if (rule != null && !rule.enabled) {
            rule.enabled = true;
            changed = true;
         }
      }

      for (PayloadChannelFilterManager.Inner_CachePool preset : PRESET_CATALOG) {
         if (this.ensurePresetEnabledInMemory(preset)) {
            changed = true;
         }
      }

      if (changed) {
         this.saveToConfig();
      }
   }

   public synchronized void disableAllAndClearPresets() {
      boolean[] changed = new boolean[]{false};
      this.rules.removeIf(rule -> {
         if (rule == null || rule.preset) {
            changed[0] = true;
            return true;
         }
         if (rule.enabled) {
            rule.enabled = false;
            changed[0] = true;
         }
         return false;
      });
      if (changed[0]) {
         this.saveToConfig();
      }
   }

   public boolean areAllDefaultsEnabled() {
      int total = 0;
      int enabled = 0;

      for (PayloadChannelFilterManager.Inner_CachePool preset : PRESET_CATALOG) {
         if (isDefaultPreset(preset)) {
            total++;
            if (this.isPresetEnabled(preset)) {
               enabled++;
            }
         }
      }

      return total > 0 && enabled == total;
   }

   public boolean isOnlyDefaultsEnabled() {
      Set<String> defaultKeys = defaultPresetKeys();
      if (defaultKeys.isEmpty()) {
         return false;
      }

      for (String key : defaultKeys) {
         boolean keyEnabled = false;

         for (AutismConfig.PayloadChannelFilterRule rule : this.rules) {
            if (rule != null && rule.enabled && key.equals(ruleKey(rule.pattern))) {
               keyEnabled = true;
               break;
            }
         }

         if (!keyEnabled) {
            return false;
         }
      }

      for (AutismConfig.PayloadChannelFilterRule rule : this.rules) {
         if (rule != null && rule.enabled && !defaultKeys.contains(ruleKey(rule.pattern))) {
            return false;
         }
      }

      return true;
   }

   public synchronized void resetToDefaultsAndSave() {
      Map<String, PayloadChannelFilterManager.Inner_CachePool> defaults = defaultPresetsByKey();
      if (!defaults.isEmpty()) {
         boolean[] changed = new boolean[]{false};
         this.rules.removeIf(rule -> {
            if (rule == null) {
               changed[0] = true;
               return true;
            }
            String key = ruleKey(rule.pattern);
            PayloadChannelFilterManager.Inner_CachePool defaultPreset = defaults.get(key);
            if (defaultPreset != null) {
               if (!rule.enabled) {
                  rule.enabled = true;
                  changed[0] = true;
               }
               if (!rule.preset) {
                  rule.preset = true;
                  changed[0] = true;
               }
               String label = defaultPreset.load();
               if (label != null && !label.isBlank() && (rule.label == null || rule.label.isBlank() || rule.label.equals(rule.pattern))) {
                  rule.label = label;
                  changed[0] = true;
               }
               return false;
            }
            if (rule.preset) {
               changed[0] = true;
               return true;
            }
            if (rule.enabled) {
               rule.enabled = false;
               changed[0] = true;
            }
            return false;
         });

         for (PayloadChannelFilterManager.Inner_CachePool preset : defaults.values()) {
            if (this.ensurePresetEnabledInMemory(preset)) {
               changed[0] = true;
            }
         }

         if (changed[0]) {
            this.saveToConfig();
         }
      }
   }

   public synchronized void resetToDefaultsInMemory() {
      Map<String, PayloadChannelFilterManager.Inner_CachePool> defaults = defaultPresetsByKey();
      if (!defaults.isEmpty()) {
         this.rules.removeIf(rule -> {
            if (rule == null) {
               return true;
            }
            String key = ruleKey(rule.pattern);
            PayloadChannelFilterManager.Inner_CachePool defaultPreset = defaults.get(key);
            if (defaultPreset != null) {
               rule.enabled = true;
               rule.preset = true;
               String label = defaultPreset.load();
               if (label != null && !label.isBlank() && (rule.label == null || rule.label.isBlank() || rule.label.equals(rule.pattern))) {
                  rule.label = label;
               }
               return false;
            }
            if (rule.preset) {
               return true;
            }
            rule.enabled = false;
            return false;
         });

         for (PayloadChannelFilterManager.Inner_CachePool preset : defaults.values()) {
            this.ensurePresetEnabledInMemory(preset);
         }
      }
   }

   public synchronized void disableAllInMemory() {
      this.rules.removeIf(rule -> {
         if (rule != null && !rule.preset) {
            rule.enabled = false;
            return false;
         }
         return true;
      });
   }

   public void ensureDefaultsEnabledAndSave() {
      boolean changed = false;

      for (PayloadChannelFilterManager.Inner_CachePool preset : PRESET_CATALOG) {
         if (isDefaultPreset(preset) && this.ensurePresetEnabledInMemory(preset)) {
            changed = true;
         }
      }

      if (changed) {
         this.saveToConfig();
      }
   }

   private static Set<String> defaultPresetKeys() {
      return new HashSet<>(defaultPresetsByKey().keySet());
   }

   private static Map<String, PayloadChannelFilterManager.Inner_CachePool> defaultPresetsByKey() {
      Map<String, PayloadChannelFilterManager.Inner_CachePool> defaults = new LinkedHashMap<>();

      for (PayloadChannelFilterManager.Inner_CachePool preset : PRESET_CATALOG) {
         if (isDefaultPreset(preset)) {
            defaults.put(preset.init_Str(), preset);
         }
      }

      return defaults;
   }

   public PayloadChannelFilterManager.Inner_StreamUtil findMatchingRuleForEntry(AutismPayloadSupport.PayloadEntry snapshot, String packetDirection) {
      return snapshot != null && snapshot.channel() != null && !snapshot.channel().isBlank()
         ? this.findMatchingRule(snapshot.channel(), packetDirection)
         : null;
   }

   public PayloadChannelFilterManager.Inner_StreamUtil findMatchingRule(String channelName, String packetDirection) {
      String channel = normalizeChannel(channelName);
      if (channel.isBlank()) {
         return null;
      }

      for (AutismConfig.PayloadChannelFilterRule rule : this.rules) {
         if (rule != null && rule.enabled && matchesNormalized(rule.pattern, channel)) {
            String label = rule.label != null && !rule.label.isBlank() ? rule.label : rule.pattern;
            return new PayloadChannelFilterManager.Inner_StreamUtil(label, rule.pattern, PayloadChannelFilterManager.Inner_DataUtil.ANY);
         }
      }

      return null;
   }

   public String matchSearchText(AutismPayloadSupport.PayloadEntry snapshot, String packetDirection) {
      PayloadChannelFilterManager.Inner_StreamUtil match = this.findMatchingRuleForEntry(snapshot, packetDirection);
      return match == null ? "" : match.init_Str();
   }

   public static boolean isDefaultCorePreset(PayloadChannelFilterManager.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      String pattern = normalizePattern(preset.read());
      boolean var10000;
      switch (pattern) {
         case "minecraft:brand":
         case "minecraft:register":
         case "minecraft:unregister":
         case "register":
         case "unregister":
         case "bungeecord":
         case "bungeecord:main":
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   public static boolean isRegisterableExactPreset(PayloadChannelFilterManager.Inner_CachePool preset) {
      return preset != null && preset.kind() == PayloadChannelFilterManager.Inner_BufferHelper.EXACT ? PayloadRegistrationManager.isExactChannel(preset.read()) : false;
   }

   public static String editablePrefixFor(PayloadChannelFilterManager.Inner_CachePool preset) {
      if (preset == null) {
         return "";
      }

      String prefix = preset.templatePrefix();
      return prefix != null && !prefix.isBlank() ? prefix : stripWildcardSuffix(preset.read());
   }

   private static boolean isDefaultPreset(PayloadChannelFilterManager.Inner_CachePool preset) {
      return isDefaultCorePreset(preset);
   }

   public void saveToConfig() {
      this.syncToConfig(true);
   }

   public void syncToConfig(boolean writeFile) {
      AutismConfig config = AutismConfig.getGlobal();
      config.packetLoggerPayloadFilters = new ArrayList<>(this.rules);
      config.packetLoggerPayloadListeners = new ArrayList<>();
      if (writeFile) {
         config.save();
      }

      autismclient.modules.ModuleRuntimeManager.getInstance().invalidatePayloadFilterCache();
   }

   public static String normalizePattern(String pattern) {
      return pattern == null ? "" : normalizeChannel(pattern.trim());
   }

   private static PayloadChannelFilterManager.Inner_BufferHelper inferPresetKind(String pattern) {
      String normalized = normalizePattern(pattern);
      if (isCoreChannel(normalized)) {
         return PayloadChannelFilterManager.Inner_BufferHelper.CORE;
      } else if (normalized.indexOf(42) >= 0) {
         return PayloadChannelFilterManager.Inner_BufferHelper.TEMPLATE;
      } else {
         return PayloadRegistrationManager.isExactChannel(normalized) ? PayloadChannelFilterManager.Inner_BufferHelper.UNVERIFIED : PayloadChannelFilterManager.Inner_BufferHelper.TEMPLATE;
      }
   }

   private static String stripWildcardSuffix(String pattern) {
      String normalized = normalizePattern(pattern);
      int star = normalized.indexOf(42);
      return star < 0 ? normalized : normalized.substring(0, star);
   }

   private static boolean isCoreChannel(String normalized) {
      boolean var10000;
      switch (normalizePattern(normalized)) {
         case "minecraft:brand":
         case "minecraft:register":
         case "minecraft:unregister":
         case "register":
         case "unregister":
         case "bungeecord":
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   private static AutismConfig.PayloadChannelFilterRule sanitizeRule(AutismConfig.PayloadChannelFilterRule rule) {
      if (rule == null) {
         return null;
      } else {
         String pattern = normalizePattern(rule.pattern);
         if (pattern.isBlank()) {
            return null;
         } else {
            String label = rule.label == null ? "" : rule.label.trim();
            if (!isLegacyProbePattern(pattern) && !isLegacyProbePattern(label)) {
               AutismConfig.PayloadChannelFilterRule clean = new AutismConfig.PayloadChannelFilterRule();
               clean.pattern = pattern;
               clean.label = label.isBlank() ? pattern : label;
               clean.enabled = rule.enabled;
               clean.preset = rule.preset;
               return clean;
            } else {
               return null;
            }
         }
      }
   }

   private boolean upsertRuleAndSave(String label, String pattern, PayloadChannelFilterManager.Inner_DataUtil direction, boolean preset, boolean enabled) {
      boolean changed = this.upsertRuleInMemory(label, pattern, direction, preset, enabled);
      if (changed) {
         this.saveToConfig();
      }

      return changed;
   }

   private boolean upsertRuleInMemory(String label, String pattern, PayloadChannelFilterManager.Inner_DataUtil direction, boolean preset, boolean enabled) {
      String normalized = normalizePattern(pattern);
      if (normalized.isBlank()) {
         return false;
      }

      PayloadChannelFilterManager.Inner_DataUtil dir = PayloadChannelFilterManager.Inner_DataUtil.ANY;
      String key = ruleKey(normalized);

      for (AutismConfig.PayloadChannelFilterRule existing : this.rules) {
         if (ruleKey(existing.pattern).equals(key)) {
            boolean changed = false;
            if (!existing.enabled && enabled) {
               existing.enabled = true;
               changed = true;
            }

            if (existing.label == null || existing.label.isBlank() || existing.label.equals(existing.pattern)) {
               existing.label = label != null && !label.isBlank() ? label.trim() : normalized;
               changed = true;
            }

            return changed;
         }
      }

      AutismConfig.PayloadChannelFilterRule rule = new AutismConfig.PayloadChannelFilterRule();
      rule.pattern = normalized;
      rule.label = label != null && !label.isBlank() ? label.trim() : normalized;
      rule.enabled = enabled;
      rule.preset = preset;
      this.rules.add(rule);
      return true;
   }

   private static boolean matchesNormalized(String pattern, String normalizedChannel) {
      String normalizedPattern = normalizePattern(pattern);
      if (normalizedPattern.isEmpty() || normalizedChannel.isEmpty()) {
         return false;
      } else if ("*".equals(normalizedPattern)) {
         return true;
      } else {
         return normalizedPattern.indexOf(42) < 0 ? normalizedChannel.equals(normalizedPattern) : matchesWildcard(normalizedPattern, normalizedChannel);
      }
   }

   private static boolean matchesWildcard(String pattern, String text) {
      int p = 0;
      int t = 0;
      int star = -1;
      int mark = 0;

      while (t < text.length()) {
         if (p < pattern.length() && pattern.charAt(p) == text.charAt(t)) {
            p++;
            t++;
         } else if (p < pattern.length() && pattern.charAt(p) == '*') {
            star = p++;
            mark = t;
         } else {
            if (star == -1) {
               return false;
            }

            p = star + 1;
            t = ++mark;
         }
      }

      while (p < pattern.length() && pattern.charAt(p) == '*') {
         p++;
      }

      return p == pattern.length();
   }

   private static String normalizeChannel(String channel) {
      return channel == null ? "" : channel.trim().toLowerCase(Locale.ROOT);
   }

   private static String ruleKey(String pattern) {
      return normalizePattern(pattern);
   }

   private static boolean isLegacyProbePattern(String value) {
      String normalized = normalizePattern(value);
      return normalized.isBlank()
         ? false
         : normalized.contains("autismtest")
            || normalized.contains("payloadprobe")
            || normalized.contains("payload_probe")
            || normalized.contains("brandlike")
            || normalized.contains("mc_string")
            || normalized.contains("java_utf");
   }

   private static List<String> validateCatalog(List<PayloadChannelFilterManager.Inner_CachePool> presets) {
      if (presets != null && !presets.isEmpty()) {
         List<String> warnings = new ArrayList<>();
         Set<String> keys = new HashSet<>();
         Map<String, String> groupCase = new LinkedHashMap<>();

         for (PayloadChannelFilterManager.Inner_CachePool preset : presets) {
            if (preset == null) {
               warnings.add("null preset row");
            } else {
               String key = preset.init_Str();
               if (!keys.add(key)) {
                  warnings.add("duplicate pattern " + preset.read());
               }

               String group = preset.write() == null ? "" : preset.write().trim();
               if (group.isBlank()) {
                  warnings.add("missing group for " + preset.read());
               }

               String folded = group.toLowerCase(Locale.ROOT);
               String existing = groupCase.putIfAbsent(folded, group);
               if (existing != null && !existing.equals(group)) {
                  warnings.add("category casing differs: " + existing + " / " + group);
               }

               String pattern = normalizePattern(preset.read());
               if (preset.kind() == PayloadChannelFilterManager.Inner_BufferHelper.EXACT) {
                  if (pattern.indexOf(42) >= 0) {
                     warnings.add("exact preset uses wildcard " + preset.read());
                  }

                  if (!PayloadRegistrationManager.isExactChannel(pattern)) {
                     warnings.add("exact preset is not registerable " + preset.read());
                  }

                  if (preset.update() == null || preset.update().isBlank()) {
                     warnings.add("exact preset missing evidence " + preset.read());
                  }
               }

               if (preset.kind() == PayloadChannelFilterManager.Inner_BufferHelper.CORE && !isCoreChannel(pattern)) {
                  warnings.add("core preset has non-core channel " + preset.read());
               }

               if (preset.kind().isEditableTemplate() && pattern.indexOf(42) < 0 && (preset.templatePrefix() == null || preset.templatePrefix().isBlank())) {
                  warnings.add("template preset missing editable prefix " + preset.read());
               }
            }
         }

         return Collections.unmodifiableList(warnings);
      } else {
         return List.of("catalog is empty");
      }
   }

   private static List<PayloadChannelFilterManager.Inner_CachePool> loadPresetCatalog() {
      List<PayloadChannelFilterManager.Inner_CachePool> presets = new ArrayList<>();
      try (InputStream in = PayloadChannelFilterManager.class.getResourceAsStream("/assets/autismclient/plugin_channels.json")) {
         if (in == null) {
            AutismClientAddon.LOG.error("[PayloadFilters] Missing preset catalog resource /assets/autismclient/plugin_channels.json");
            return Collections.unmodifiableList(presets);
         }

         JsonElement root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
         if (root instanceof JsonArray array) {
            for (JsonElement element : array) {
               if (element instanceof JsonObject obj) {
                  String label = jsonString(obj, "label");
                  String pattern = jsonString(obj, "pattern");
                  String group = jsonString(obj, "group");
                  String kind = jsonString(obj, "kind");
                  if ("core".equals(kind)) {
                     addCorePreset(presets, label, pattern, group, jsonString(obj, "evidence"));
                  } else if ("exact".equals(kind)) {
                     addExactPreset(presets, label, pattern, group, jsonString(obj, "evidence"));
                  } else {
                     addTemplatePreset(presets, label, pattern, group);
                  }
               }
            }
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[PayloadFilters] Failed to load preset catalog resource", e);
      }

      return Collections.unmodifiableList(presets);
   }

   private static String jsonString(JsonObject obj, String key) {
      JsonElement el = obj.get(key);
      return el != null && !el.isJsonNull() ? el.getAsString() : "";
   }

   private static void addTemplatePreset(List<PayloadChannelFilterManager.Inner_CachePool> presets, String label, String pattern, String group) {
      String normalized = normalizePattern(pattern);
      PayloadChannelFilterManager.Inner_BufferHelper kind = inferPresetKind(normalized);
      String evidence = kind == PayloadChannelFilterManager.Inner_BufferHelper.TEMPLATE ? "Template only; no exact public channel proof." : "";
      addPresetEntry(presets, label, normalized, group, kind, evidence, stripWildcardSuffix(normalized));
   }

   private static void addExactPreset(List<PayloadChannelFilterManager.Inner_CachePool> presets, String label, String pattern, String group, String evidence) {
      addPresetEntry(presets, label, normalizePattern(pattern), group, PayloadChannelFilterManager.Inner_BufferHelper.EXACT, evidence, "");
   }

   private static void addCorePreset(List<PayloadChannelFilterManager.Inner_CachePool> presets, String label, String pattern, String group, String evidence) {
      addPresetEntry(presets, label, normalizePattern(pattern), group, PayloadChannelFilterManager.Inner_BufferHelper.CORE, evidence, "");
   }

   private static void addPresetEntry(
      List<PayloadChannelFilterManager.Inner_CachePool> presets, String label, String normalized, String group, PayloadChannelFilterManager.Inner_BufferHelper kind, String evidence, String templatePrefix
   ) {
      String key = ruleKey(normalized);

      for (PayloadChannelFilterManager.Inner_CachePool preset : presets) {
         if (preset.init_Str().equals(key)) {
            return;
         }
      }

      PayloadChannelFilterManager.Inner_BufferHelper safeKind = kind == null ? inferPresetKind(normalized) : kind;
      String safeEvidence = evidence == null ? "" : evidence;
      String safeTemplate = templatePrefix == null ? "" : templatePrefix;
      boolean invalidExact = safeKind == PayloadChannelFilterManager.Inner_BufferHelper.EXACT
         && (safeEvidence.isBlank() || normalized.indexOf(42) >= 0 || !PayloadRegistrationManager.isExactChannel(normalized));
      if (invalidExact) {
         safeKind = PayloadChannelFilterManager.Inner_BufferHelper.UNVERIFIED;
         safeTemplate = safeTemplate.isBlank() ? stripWildcardSuffix(normalized) : safeTemplate;
         safeEvidence = safeEvidence.isBlank() ? "Exact channel proof missing; kept as editable template." : safeEvidence;
      }

      presets.add(new PayloadChannelFilterManager.Inner_CachePool(label, normalized, PayloadChannelFilterManager.Inner_DataUtil.ANY, group, safeKind, safeEvidence, safeTemplate));
   }

   static {
      for (String warning : CATALOG_WARNINGS) {
         AutismClientAddon.LOG.warn("[PayloadFilters] Preset catalog warning: {}", warning);
      }
   }

   public enum Inner_BufferHelper {
      EXACT("Exact"),
      CORE("Core"),
      TEMPLATE("Template"),
      UNVERIFIED("Unver");

      private final String badge;

      Inner_BufferHelper(String badge) {
         this.badge = badge;
      }

      public String badge() {
         return this.badge;
      }

      public boolean isEditableTemplate() {
         return this == TEMPLATE || this == UNVERIFIED;
      }
   }

   public record Inner_CachePool(
      String init_Str, String load, PayloadChannelFilterManager.Inner_DataUtil direction, String read, PayloadChannelFilterManager.Inner_BufferHelper kind, String write, String update
   ) {
      public Inner_CachePool(String label, String pattern, PayloadChannelFilterManager.Inner_DataUtil direction, String group) {
         this(label, pattern, direction, group, PayloadChannelFilterManager.inferPresetKind(pattern), "", PayloadChannelFilterManager.stripWildcardSuffix(pattern));
      }

      public String init_Str() {
         return PayloadChannelFilterManager.ruleKey(this.load);
      }

      public String load() {
         return this.init_Str;
      }

      public String read() {
         return this.load;
      }

      public String write() {
         return this.read;
      }

      public String update() {
         return this.write;
      }

      public String templatePrefix() {
         return this.update;
      }
   }

   public enum Inner_DataUtil {
      ANY,
      C2S,
      S2C;

      public static PayloadChannelFilterManager.Inner_DataUtil parse(String value) {
         if (value == null) {
            return ANY;
         }

         try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException ignored) {
            return ANY;
         }
      }

      public boolean matchesDirection(String packetDirection) {
         return true;
      }
   }

   public record Inner_StreamUtil(String init_Str, String load, PayloadChannelFilterManager.Inner_DataUtil direction) {
      public String init_Str() {
         return (this.init_Str + " " + this.load).toLowerCase(Locale.ROOT);
      }

      public String load() {
         return this.init_Str;
      }

      public String pattern() {
         return this.load;
      }
   }
}
