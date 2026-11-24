package autismclient.util;

import autismclient.AutismClientAddon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class bB {
   private static final List<bB.Inner_CachePool> init = flush_List();
   private static final List<String> load = init_List_List(init);
   private final List<AutismConfig.PayloadChannelFilterRule> read = new java.util.concurrent.CopyOnWriteArrayList<>();

   public bB() {
      this.init_V();
   }

   public synchronized void init_V() {
      this.read.clear();
      AutismConfig config = AutismConfig.getGlobal();
      if (config.packetLoggerPayloadFilters == null) {
         config.packetLoggerPayloadFilters = new ArrayList<>();
      }

      Map<String, AutismConfig.PayloadChannelFilterRule> merged = new LinkedHashMap<>();

      for (AutismConfig.PayloadChannelFilterRule rule : config.packetLoggerPayloadFilters) {
         AutismConfig.PayloadChannelFilterRule clean = init_AutismConfig_Paylo_AutismConfig_Paylo(rule);
         if (clean != null) {
            String key = write_Str_Str(clean.pattern);
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

      this.read.addAll(merged.values());
   }

   public List<AutismConfig.PayloadChannelFilterRule> init_List() {
      return Collections.unmodifiableList(this.read);
   }

   List<AutismConfig.PayloadChannelFilterRule> load_List() {
      return this.read;
   }

   public List<bB.Inner_CachePool> read_List() {
      return init;
   }

   public static List<bB.Inner_CachePool> write_List() {
      return init;
   }

   public static List<String> update_List() {
      return load;
   }

   public static boolean init_Z_bB_CachePool_Str(bB.Inner_CachePool preset, String channelName) {
      return preset == null ? false : load(preset.read(), read_Str_Str(channelName));
   }

   public static boolean init_Z_Str_Str(String pattern, String channelName) {
      return load(pattern, read_Str_Str(channelName));
   }

   public static boolean init_Z_bB_CachePool(bB.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      if (preset.init_bB_BufferHelper() != bB.Inner_BufferHelper.init_bB_BufferHelper) {
         return false;
      }

      String label = preset.load() == null ? "" : preset.load().trim().toLowerCase(Locale.ROOT);
      String pattern = init_Str_Str(preset.read());
      if (pattern.isBlank() || "Minecraft".equalsIgnoreCase(preset.write())) {
         return false;
      }

      if (!label.contains("family") && !label.contains("namespace")) {
         int colon = pattern.indexOf(58);
         if (colon <= 0) {
            return false;
         }

         String namespace = pattern.substring(0, colon);
         return !load_Z_Str(namespace);
      } else {
         return false;
      }
   }

   private static boolean load_Z_Str(String namespace) {
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

   public boolean init(String label, String pattern, bB.Inner_DataUtil direction) {
      return this.init_Z_Str(label, pattern, direction, false, true);
   }

   public boolean load(String label, String pattern, bB.Inner_DataUtil direction) {
      return this.load_Z_Str(label, pattern, direction, false, true);
   }

   public boolean load_Z_bB_CachePool(bB.Inner_CachePool preset) {
      return preset == null ? false : this.init_Z_Str(preset.load(), preset.read(), preset.init_bB_DataUtil(), true, true);
   }

   public boolean read_Z_bB_CachePool(bB.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      String key = preset.init_Str();

      for (AutismConfig.PayloadChannelFilterRule rule : this.read) {
         if (write_Str_Str(rule.pattern).equals(key)) {
            if (!rule.enabled) {
               rule.enabled = true;
               this.open();
               return true;
            }

            return false;
         }
      }

      return this.load_Z_bB_CachePool(preset);
   }

   public boolean write_Z_bB_CachePool(bB.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      String key = preset.init_Str();

      for (AutismConfig.PayloadChannelFilterRule rule : this.read) {
         if (write_Str_Str(rule.pattern).equals(key)) {
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

      return this.load_Z_Str(preset.load(), preset.read(), preset.init_bB_DataUtil(), true, true);
   }

   public void init_V_I(int index) {
      if (index >= 0 && index < this.read.size()) {
         AutismConfig.PayloadChannelFilterRule rule = this.read.get(index);
         rule.enabled = !rule.enabled;
         this.open();
      }
   }

   public void load_V_I(int index) {
      if (index >= 0 && index < this.read.size()) {
         AutismConfig.PayloadChannelFilterRule rule = this.read.get(index);
         if (rule != null) {
            rule.enabled = !rule.enabled;
         }
      }
   }

   public void read_V_I(int index) {
      if (index >= 0 && index < this.read.size()) {
         this.read.remove(index);
         this.open();
      }
   }

   public void write_V_I(int index) {
      if (index >= 0 && index < this.read.size()) {
         this.read.remove(index);
      }
   }

   public synchronized void load_V() {
      boolean changed = this.read.removeIf(rule -> rule != null && rule.preset);
      if (changed) {
         this.open();
      }
   }

   public boolean update(bB.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      String key = preset.init_Str();

      for (AutismConfig.PayloadChannelFilterRule rule : this.read) {
         if (write_Str_Str(rule.pattern).equals(key)) {
            return true;
         }
      }

      return false;
   }

   public boolean sync(bB.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      String key = preset.init_Str();

      for (AutismConfig.PayloadChannelFilterRule rule : this.read) {
         if (write_Str_Str(rule.pattern).equals(key)) {
            return rule.enabled;
         }
      }

      return false;
   }

   public boolean init_Z() {
      for (AutismConfig.PayloadChannelFilterRule rule : this.read) {
         if (rule != null && rule.enabled) {
            return true;
         }
      }

      return false;
   }

   public List<String> sync_List() {
      return this.init_List_Z(false);
   }

   public List<String> init_List_Z(boolean includeLoggerDefaults) {
      return bD.init(includeLoggerDefaults).init_List();
   }

   public int init_I_Str(String group) {
      if (group != null && !group.isBlank()) {
         int count = 0;

         for (bB.Inner_CachePool preset : init) {
            if (group.equals(preset.write())) {
               count++;
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   public int load_I_Str(String group) {
      if (group != null && !group.isBlank()) {
         int count = 0;

         for (bB.Inner_CachePool preset : init) {
            if (group.equals(preset.write()) && this.sync(preset)) {
               count++;
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   public boolean init_Z_Str(String group) {
      int total = this.init_I_Str(group);
      return total > 0 && this.load_I_Str(group) == total;
   }

   public void init_V_Str(String group) {
      if (group != null && !group.isBlank()) {
         this.init_V_Str_Z(group, !this.init_Z_Str(group));
      }
   }

   private void init_V_Str_Z(String group, boolean enabled) {
      if (group != null && !group.isBlank()) {
         boolean changed = false;
         Set<String> groupKeys = new HashSet<>();

         for (bB.Inner_CachePool preset : init) {
            if (group.equals(preset.write())) {
               groupKeys.add(preset.init_Str());
            }
         }

         if (!groupKeys.isEmpty()) {
            if (!enabled) {
               changed = this.read.removeIf(rule -> rule != null && rule.preset && groupKeys.contains(write_Str_Str(rule.pattern)));
            } else {
               for (bB.Inner_CachePool preset : init) {
                  if (group.equals(preset.write()) && this.open(preset)) {
                     changed = true;
                  }
               }
            }

            if (changed) {
               this.open();
            }
         }
      }
   }

   public void init_V_bB_CachePool(bB.Inner_CachePool preset) {
      if (preset != null) {
         String key = preset.init_Str();

         for (int i = 0; i < this.read.size(); i++) {
            AutismConfig.PayloadChannelFilterRule rule = this.read.get(i);
            if (write_Str_Str(rule.pattern).equals(key)) {
               if (rule.enabled) {
                  this.read.remove(i);
                  this.open();
               } else {
                  rule.enabled = true;
                  rule.preset = true;
                  this.open();
               }

               return;
            }
         }

         this.load_Z_bB_CachePool(preset);
      }
   }

   public void load_V_bB_CachePool(bB.Inner_CachePool preset) {
      if (preset != null) {
         String key = preset.init_Str();

         for (int i = 0; i < this.read.size(); i++) {
            AutismConfig.PayloadChannelFilterRule rule = this.read.get(i);
            if (write_Str_Str(rule.pattern).equals(key)) {
               if (rule.enabled) {
                  this.read.remove(i);
               } else {
                  rule.enabled = true;
                  rule.preset = true;
               }

               return;
            }
         }

         this.load_Z_Str(preset.load(), preset.read(), preset.init_bB_DataUtil(), true, true);
      }
   }

   private boolean open(bB.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      String key = preset.init_Str();

      for (AutismConfig.PayloadChannelFilterRule rule : this.read) {
         if (write_Str_Str(rule.pattern).equals(key)) {
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
      rule.pattern = init_Str_Str(preset.read());
      rule.label = preset.load();
      rule.enabled = true;
      rule.preset = true;
      this.read.add(rule);
      return true;
   }

   public void read_V() {
      boolean changed = false;

      for (AutismConfig.PayloadChannelFilterRule rule : this.read) {
         if (rule != null && !rule.enabled) {
            rule.enabled = true;
            changed = true;
         }
      }

      for (bB.Inner_CachePool preset : init) {
         if (this.open(preset)) {
            changed = true;
         }
      }

      if (changed) {
         this.open();
      }
   }

   public synchronized void write_V() {
      boolean[] changed = new boolean[]{false};
      this.read.removeIf(rule -> {
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
         this.open();
      }
   }

   public boolean load_Z() {
      int total = 0;
      int enabled = 0;

      for (bB.Inner_CachePool preset : init) {
         if (poll(preset)) {
            total++;
            if (this.sync(preset)) {
               enabled++;
            }
         }
      }

      return total > 0 && enabled == total;
   }

   public boolean read_Z() {
      Set<String> defaultKeys = init_Set();
      if (defaultKeys.isEmpty()) {
         return false;
      }

      for (String key : defaultKeys) {
         boolean keyEnabled = false;

         for (AutismConfig.PayloadChannelFilterRule rule : this.read) {
            if (rule != null && rule.enabled && key.equals(write_Str_Str(rule.pattern))) {
               keyEnabled = true;
               break;
            }
         }

         if (!keyEnabled) {
            return false;
         }
      }

      for (AutismConfig.PayloadChannelFilterRule rule : this.read) {
         if (rule != null && rule.enabled && !defaultKeys.contains(write_Str_Str(rule.pattern))) {
            return false;
         }
      }

      return true;
   }

   public synchronized void update_V() {
      Map<String, bB.Inner_CachePool> defaults = init_Map();
      if (!defaults.isEmpty()) {
         boolean[] changed = new boolean[]{false};
         this.read.removeIf(rule -> {
            if (rule == null) {
               changed[0] = true;
               return true;
            }
            String key = write_Str_Str(rule.pattern);
            bB.Inner_CachePool defaultPreset = defaults.get(key);
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

         for (bB.Inner_CachePool preset : defaults.values()) {
            if (this.open(preset)) {
               changed[0] = true;
            }
         }

         if (changed[0]) {
            this.open();
         }
      }
   }

   public synchronized void sync_V() {
      Map<String, bB.Inner_CachePool> defaults = init_Map();
      if (!defaults.isEmpty()) {
         this.read.removeIf(rule -> {
            if (rule == null) {
               return true;
            }
            String key = write_Str_Str(rule.pattern);
            bB.Inner_CachePool defaultPreset = defaults.get(key);
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

         for (bB.Inner_CachePool preset : defaults.values()) {
            this.open(preset);
         }
      }
   }

   public synchronized void flush_V() {
      this.read.removeIf(rule -> {
         if (rule != null && !rule.preset) {
            rule.enabled = false;
            return false;
         }
         return true;
      });
   }

   public void close() {
      boolean changed = false;

      for (bB.Inner_CachePool preset : init) {
         if (poll(preset) && this.open(preset)) {
            changed = true;
         }
      }

      if (changed) {
         this.open();
      }
   }

   private static Set<String> init_Set() {
      return new HashSet<>(init_Map().keySet());
   }

   private static Map<String, bB.Inner_CachePool> init_Map() {
      Map<String, bB.Inner_CachePool> defaults = new LinkedHashMap<>();

      for (bB.Inner_CachePool preset : init) {
         if (poll(preset)) {
            defaults.put(preset.init_Str(), preset);
         }
      }

      return defaults;
   }

   public bB.Inner_StreamUtil init_bB_StreamUtil_AutismPayloadSuppo_Str(AutismPayloadSupport.Inner_StreamUtil snapshot, String packetDirection) {
      return snapshot != null && snapshot.load_Str() != null && !snapshot.load_Str().isBlank()
         ? this.init_bB_StreamUtil_Str_Str(snapshot.load_Str(), packetDirection)
         : null;
   }

   public bB.Inner_StreamUtil init_bB_StreamUtil_Str_Str(String channelName, String packetDirection) {
      String channel = read_Str_Str(channelName);
      if (channel.isBlank()) {
         return null;
      }

      for (AutismConfig.PayloadChannelFilterRule rule : this.read) {
         if (rule != null && rule.enabled && load(rule.pattern, channel)) {
            String label = rule.label != null && !rule.label.isBlank() ? rule.label : rule.pattern;
            return new bB.Inner_StreamUtil(label, rule.pattern, bB.Inner_DataUtil.init);
         }
      }

      return null;
   }

   public String init_Str_AutismPayloadSuppo_Str(AutismPayloadSupport.Inner_StreamUtil snapshot, String packetDirection) {
      bB.Inner_StreamUtil match = this.init_bB_StreamUtil_AutismPayloadSuppo_Str(snapshot, packetDirection);
      return match == null ? "" : match.init_Str();
   }

   public static boolean flush(bB.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      String pattern = init_Str_Str(preset.read());
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

   public static boolean close(bB.Inner_CachePool preset) {
      return preset != null && preset.init_bB_BufferHelper() == bB.Inner_BufferHelper.init_bB_BufferHelper ? bC.load_Z_Str(preset.read()) : false;
   }

   public static String init_Str_bB_CachePool(bB.Inner_CachePool preset) {
      if (preset == null) {
         return "";
      }

      String prefix = preset.sync();
      return prefix != null && !prefix.isBlank() ? prefix : load_Str_Str(preset.read());
   }

   private static boolean poll(bB.Inner_CachePool preset) {
      return flush(preset);
   }

   public void open() {
      this.init_V_Z(true);
   }

   public void init_V_Z(boolean writeFile) {
      AutismConfig config = AutismConfig.getGlobal();
      config.packetLoggerPayloadFilters = new ArrayList<>(this.read);
      config.packetLoggerPayloadListeners = new ArrayList<>();
      if (writeFile) {
         config.save();
      }

      autismclient.modules.PoolEntry.init_PoolEntry().update_V();
   }

   public static String init_Str_Str(String pattern) {
      return pattern == null ? "" : read_Str_Str(pattern.trim());
   }

   private static bB.Inner_BufferHelper init_bB_BufferHelper_Str(String pattern) {
      String normalized = init_Str_Str(pattern);
      if (read_Z_Str(normalized)) {
         return bB.Inner_BufferHelper.load;
      } else if (normalized.indexOf(42) >= 0) {
         return bB.Inner_BufferHelper.read;
      } else {
         return bC.load_Z_Str(normalized) ? bB.Inner_BufferHelper.write : bB.Inner_BufferHelper.read;
      }
   }

   private static String load_Str_Str(String pattern) {
      String normalized = init_Str_Str(pattern);
      int star = normalized.indexOf(42);
      return star < 0 ? normalized : normalized.substring(0, star);
   }

   private static boolean read_Z_Str(String normalized) {
      boolean var10000;
      switch (init_Str_Str(normalized)) {
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

   private static AutismConfig.PayloadChannelFilterRule init_AutismConfig_Paylo_AutismConfig_Paylo(AutismConfig.PayloadChannelFilterRule rule) {
      if (rule == null) {
         return null;
      } else {
         String pattern = init_Str_Str(rule.pattern);
         if (pattern.isBlank()) {
            return null;
         } else {
            String label = rule.label == null ? "" : rule.label.trim();
            if (!write_Z_Str(pattern) && !write_Z_Str(label)) {
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

   private boolean init_Z_Str(String label, String pattern, bB.Inner_DataUtil direction, boolean preset, boolean enabled) {
      boolean changed = this.load_Z_Str(label, pattern, direction, preset, enabled);
      if (changed) {
         this.open();
      }

      return changed;
   }

   private boolean load_Z_Str(String label, String pattern, bB.Inner_DataUtil direction, boolean preset, boolean enabled) {
      String normalized = init_Str_Str(pattern);
      if (normalized.isBlank()) {
         return false;
      }

      bB.Inner_DataUtil dir = bB.Inner_DataUtil.init;
      String key = write_Str_Str(normalized);

      for (AutismConfig.PayloadChannelFilterRule existing : this.read) {
         if (write_Str_Str(existing.pattern).equals(key)) {
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
      this.read.add(rule);
      return true;
   }

   private static boolean load(String pattern, String normalizedChannel) {
      String normalizedPattern = init_Str_Str(pattern);
      if (normalizedPattern.isEmpty() || normalizedChannel.isEmpty()) {
         return false;
      } else if ("*".equals(normalizedPattern)) {
         return true;
      } else {
         return normalizedPattern.indexOf(42) < 0 ? normalizedChannel.equals(normalizedPattern) : read(normalizedPattern, normalizedChannel);
      }
   }

   private static boolean read(String pattern, String text) {
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

   private static String read_Str_Str(String channel) {
      return channel == null ? "" : channel.trim().toLowerCase(Locale.ROOT);
   }

   private static String write_Str_Str(String pattern) {
      return init_Str_Str(pattern);
   }

   private static boolean write_Z_Str(String value) {
      String normalized = init_Str_Str(value);
      return normalized.isBlank()
         ? false
         : normalized.contains("autismtest")
            || normalized.contains("payloadprobe")
            || normalized.contains("payload_probe")
            || normalized.contains("brandlike")
            || normalized.contains("mc_string")
            || normalized.contains("java_utf");
   }

   private static List<String> init_List_List(List<bB.Inner_CachePool> presets) {
      if (presets != null && !presets.isEmpty()) {
         List<String> warnings = new ArrayList<>();
         Set<String> keys = new HashSet<>();
         Map<String, String> groupCase = new LinkedHashMap<>();

         for (bB.Inner_CachePool preset : presets) {
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

               String pattern = init_Str_Str(preset.read());
               if (preset.init_bB_BufferHelper() == bB.Inner_BufferHelper.init_bB_BufferHelper) {
                  if (pattern.indexOf(42) >= 0) {
                     warnings.add("exact preset uses wildcard " + preset.read());
                  }

                  if (!bC.load_Z_Str(pattern)) {
                     warnings.add("exact preset is not registerable " + preset.read());
                  }

                  if (preset.update() == null || preset.update().isBlank()) {
                     warnings.add("exact preset missing evidence " + preset.read());
                  }
               }

               if (preset.init_bB_BufferHelper() == bB.Inner_BufferHelper.load && !read_Z_Str(pattern)) {
                  warnings.add("core preset has non-core channel " + preset.read());
               }

               if (preset.init_bB_BufferHelper().init_Z() && pattern.indexOf(42) < 0 && (preset.sync() == null || preset.sync().isBlank())) {
                  warnings.add("template preset missing editable prefix " + preset.read());
               }
            }
         }

         return Collections.unmodifiableList(warnings);
      } else {
         return List.of("catalog is empty");
      }
   }

   private static List<bB.Inner_CachePool> flush_List() {
      List<bB.Inner_CachePool> presets = new ArrayList<>();
      load_V_List(
         presets, "Minecraft Brand", "minecraft:brand", "Minecraft", "Vanilla brand custom payload; observed/logged, not a Bukkit subscription target."
      );
      load_V_List(
         presets, "Minecraft Register", "minecraft:register", "Minecraft", "Vanilla/legacy plugin-channel registration list; observed/logged, not registered."
      );
      load_V_List(
         presets,
         "Minecraft Unregister",
         "minecraft:unregister",
         "Minecraft",
         "Vanilla/legacy plugin-channel unregister list; observed/logged, not registered."
      );
      load_V_List(presets, "Legacy REGISTER", "REGISTER", "Minecraft", "Legacy registration payload name observed in plugin-channel traffic.");
      load_V_List(presets, "Legacy UNREGISTER", "UNREGISTER", "Minecraft", "Legacy unregister payload name observed in plugin-channel traffic.");
      load_V_List(presets, "BungeeCord", "BungeeCord", "Proxy", "Legacy BungeeCord plugin messaging channel; watch/highlight compatibility row.");
      init_V_List(
         presets,
         "BungeeCord Main",
         "bungeecord:main",
         "Proxy",
         "Spigot/Paper proxy plugin messaging channel documented by BungeeCord/Velocity compatibility guides."
      );
      init_V_List(
         presets,
         "ViaVersion Server Details",
         "vv:server_details",
         "Proxy",
         "ViaVersion docs/source: ConnectionDetails.SERVER_CHANNEL sends server details JSON to clients."
      );
      init_V_List(
         presets, "ViaVersion Proxy Details", "vv:proxy_details", "Proxy", "ViaVersion docs/source: ConnectionDetails.PROXY_CHANNEL sends proxy details JSON."
      );
      init_V_List(
         presets, "ViaVersion Mod Details", "vv:mod_details", "Proxy", "ViaVersion docs/source: ConnectionDetails.MOD_CHANNEL is used for client mod details."
      );
      init_V_List(
         presets,
         "ViaVersion App Details",
         "vv:app_details",
         "Proxy",
         "ViaVersion docs/source: ConnectionDetails.APP_CHANNEL is used for standalone app details."
      );
      init(presets, "Velocity", "velocity:*", "Proxy");
      init(presets, "ViaVersion", "viaversion:*", "Proxy");
      init(presets, "ViaVersion Details", "vv:*", "Proxy");
      init(presets, "ViaBackwards", "viabackwards:*", "Proxy");
      init(presets, "Geyser", "geyser:*", "Proxy");
      init_V_List(
         presets, "Floodgate Skin", "floodgate:skin", "Proxy", "Floodgate/Geyser source: SkinChannel / PluginMessageChannels.SKIN returns floodgate:skin."
      );
      init_V_List(
         presets, "Floodgate Form", "floodgate:form", "Proxy", "Floodgate/Geyser source: FormChannel / PluginMessageChannels.FORM returns floodgate:form."
      );
      init_V_List(
         presets,
         "Floodgate Transfer",
         "floodgate:transfer",
         "Proxy",
         "Floodgate/Geyser source: TransferChannel / PluginMessageChannels.TRANSFER returns floodgate:transfer."
      );
      init_V_List(
         presets,
         "Floodgate Packet",
         "floodgate:packet",
         "Proxy",
         "Floodgate/Geyser source: PacketChannel / PluginMessageChannels.PACKET returns floodgate:packet."
      );
      init(presets, "Floodgate", "floodgate:*", "Proxy");
      init(presets, "Fabric", "fabric:*", "Mod API");
      init_V_List(
         presets,
         "Fabric Split",
         "fabric:split",
         "Mod API",
         "Fabric API source: FabricSplitPacketPayload.TYPE is Identifier.fromNamespaceAndPath(\"fabric\", \"split\")."
      );
      init_V_List(
         presets, "Fabric Common Register", "c:register", "Mod API", "Fabric API source: CommonRegisterPayload.TYPE is Identifier.parse(\"c:register\")."
      );
      init_V_List(presets, "Fabric Common Version", "c:version", "Mod API", "Fabric API source: CommonVersionPayload.TYPE is Identifier.parse(\"c:version\").");
      init(presets, "WorldEdit", "worldedit:*", "Mod API");
      init_V_List(
         presets,
         "WorldEdit CUI",
         "worldedit:cui",
         "Mod API",
         "WorldEdit source: WorldEditPlugin.CUI_PLUGIN_CHANNEL registered incoming/outgoing and sent to CUI clients."
      );
      init(presets, "WorldEdit CUI Prefix", "worldedit:cui*", "Mod API");
      init(presets, "Oraxen", "oraxen:*", "Items");
      init(presets, "ItemsAdder", "itemsadder:*", "Items");
      init(presets, "Nexo", "nexo:*", "Items");
      init(presets, "ModelEngine", "modelengine:*", "Items");
      init(presets, "MythicCrucible", "mythiccrucible:*", "Items");
      init(presets, "MMOItems", "mmoitems:*", "Items");
      init(presets, "ExecutableItems", "executableitems:*", "Items");
      init(presets, "ExecutableBlocks", "executableblocks:*", "Items");
      init(presets, "Nova", "nova:*", "Items");
      init(presets, "EcoItems", "ecoitems:*", "Items");
      init(presets, "EcoArmor", "ecoarmor:*", "Items");
      init(presets, "EcoEnchants", "ecoenchants:*", "Items");
      init(presets, "Vault", "vault:*", "Economy");
      init(presets, "Treasury", "treasury:*", "Economy");
      init(presets, "EssentialsX", "essentials:*", "Economy");
      init(presets, "EssentialsX Alt", "essentialsx:*", "Economy");
      init(presets, "CMI", "cmi:*", "Economy");
      init(presets, "TheNewEconomy", "theneweconomy:*", "Economy");
      init(presets, "TNE", "tne:*", "Economy");
      init(presets, "PlayerPoints", "playerpoints:*", "Economy");
      init(presets, "VotingPlugin Points", "votingplugin:*", "Economy");
      init(presets, "CoinsEngine", "coinsengine:*", "Economy");
      init(presets, "UltraEconomy", "ultraeconomy:*", "Economy");
      init(presets, "XConomy", "xconomy:*", "Economy");
      init(presets, "PEconomy", "peconomy:*", "Economy");
      init(presets, "CMIEInjector", "cmieinjector:*", "Economy");
      init(presets, "GemsEconomy", "gemseconomy:*", "Economy");
      init(presets, "TokenManager", "tokenmanager:*", "Economy");
      init(presets, "RedisEconomy", "rediseconomy:*", "Economy");
      init(presets, "JobsReborn", "jobs:*", "Economy");
      init(presets, "JobsReborn Alt", "jobsreborn:*", "Economy");
      init(presets, "Aurelium", "aurelium:*", "Economy");
      init(presets, "AureliumSkills", "aureliumskills:*", "Economy");
      init(presets, "AuctionHouse", "auctionhouse:*", "Auctions");
      init(presets, "AuctionMaster", "auctionmaster:*", "Auctions");
      init(presets, "CrazyAuctions", "crazyauctions:*", "Auctions");
      init(presets, "CrazyAuctions Plus", "crazyauctionsplus:*", "Auctions");
      init(presets, "ExcellentAuctionHouse", "excellentauctionhouse:*", "Auctions");
      init(presets, "ExcellentAuctions", "excellentauctions:*", "Auctions");
      init(presets, "zAuctionHouse", "zauctionhouse:*", "Auctions");
      init(presets, "AuctionGUIPlus", "auctionguiplus:*", "Auctions");
      init(presets, "PlayerAuctions", "playerauctions:*", "Auctions");
      init(presets, "BetterAuction", "betterauction:*", "Auctions");
      init(presets, "EzAuction", "ezauction:*", "Auctions");
      init(presets, "NexusAuctionHouse", "nexusauctionhouse:*", "Auctions");
      init(presets, "AxAuctions", "axauctions:*", "Auctions");
      init(presets, "AdvancedAuctions", "advancedauctions:*", "Auctions");
      init(presets, "UltimateAuction", "ultimateauction:*", "Auctions");
      init(presets, "Auction", "auction:*", "Auctions");
      init(presets, "ShopGUIPlus", "shopguiplus:*", "Shops");
      init(presets, "ShopGUI+", "shopgui:*", "Shops");
      init(presets, "EconomyShopGUI", "economyshopgui:*", "Shops");
      init(presets, "EconomyShopGUI Premium", "economyshopguipremium:*", "Shops");
      init(presets, "BossShopPro", "bossshoppro:*", "Shops");
      init(presets, "BossShop", "bossshop:*", "Shops");
      init(presets, "ChestShop", "chestshop:*", "Shops");
      init_V_List(
         presets,
         "QuickShop Bungee",
         "quickshop:bungee",
         "Shops",
         "QuickShop-Hikari source: CHAT_FORWARD_CHANNEL / QUICKSHOP_BUNGEE_CHANNEL registered and sent."
      );
      init(presets, "QuickShop", "quickshop:*", "Shops");
      init(presets, "QuickShop Reremake", "quickshop-hikari:*", "Shops");
      init(presets, "QuickShop Reremake Alt", "quickshopreremake:*", "Shops");
      init(presets, "GUIShop", "guishop:*", "Shops");
      init(presets, "UltimateShop", "ultimateshop:*", "Shops");
      init(presets, "zShop", "zshop:*", "Shops");
      init(presets, "BetterShop", "bettershop:*", "Shops");
      init(presets, "Shopkeepers", "shopkeepers:*", "Shops");
      init(presets, "dtlTraders", "dtltraders:*", "Shops");
      init(presets, "PlayerShops", "playershops:*", "Shops");
      init(presets, "PlayerWarps", "playerwarps:*", "Shops");
      init(presets, "DeluxeMenus", "deluxemenus:*", "Shops");
      init(presets, "CommandPanels", "commandpanels:*", "Shops");
      init(presets, "TradeSystem", "tradesystem:*", "Shops");
      init(presets, "TradePlus", "tradeplus:*", "Shops");
      init(presets, "Trades", "trades:*", "Shops");
      init(presets, "ExcellentShop", "excellentshop:*", "Shops");
      init(presets, "SimpleShop", "simpleshop:*", "Shops");
      init(presets, "SimpleShops", "simpleshops:*", "Shops");
      init(presets, "SimpleShopGUI", "simpleshopgui:*", "Shops");
      init(presets, "SimpleShopX", "simpleshopx:*", "Shops");
      init(presets, "ShopX", "shopx:*", "Shops");
      init(presets, "Shop X", "shop_x:*", "Shops");
      init(presets, "ShopPlus", "shopplus:*", "Shops");
      init(presets, "ShopsPlus", "shopsplus:*", "Shops");
      init(presets, "ShopPro", "shoppro:*", "Shops");
      init(presets, "ShopsPro", "shopspro:*", "Shops");
      init(presets, "BetterShops", "bettershops:*", "Shops");
      init(presets, "BetterShopsPro", "bettershopspro:*", "Shops");
      init(presets, "UltimateShops", "ultimateshops:*", "Shops");
      init(presets, "UltraShop", "ultrashop:*", "Shops");
      init(presets, "UltraShops", "ultrashops:*", "Shops");
      init(presets, "PremiumShop", "premiumshop:*", "Shops");
      init(presets, "PremiumShops", "premiumshops:*", "Shops");
      init(presets, "DeluxeShop", "deluxeshop:*", "Shops");
      init(presets, "DeluxeShops", "deluxeshops:*", "Shops");
      init(presets, "DeluxeShopGUI", "deluxeshopgui:*", "Shops");
      init(presets, "ShopSystem", "shopsystem:*", "Shops");
      init(presets, "ShopsSystem", "shopssystem:*", "Shops");
      init(presets, "ShopCore", "shopcore:*", "Shops");
      init(presets, "ShopsCore", "shopscore:*", "Shops");
      init(presets, "ShopEngine", "shopengine:*", "Shops");
      init(presets, "ShopAPI", "shopapi:*", "Shops");
      init(presets, "ShopsAPI", "shopsapi:*", "Shops");
      init(presets, "ShopMenu", "shopmenu:*", "Shops");
      init(presets, "ShopMenus", "shopmenus:*", "Shops");
      init(presets, "ShopGUIs", "shopguis:*", "Shops");
      init(presets, "GUIShopPlus", "guishopplus:*", "Shops");
      init(presets, "GUIShopPro", "guishoppro:*", "Shops");
      init(presets, "EconomyShopGUIPlus", "economyshopguiplus:*", "Shops");
      init(presets, "ESGUI", "esgui:*", "Shops");
      init(presets, "BossShopGUI", "bossshopgui:*", "Shops");
      init(presets, "CommandShop", "commandshop:*", "Shops");
      init(presets, "CommandShops", "commandshops:*", "Shops");
      init(presets, "PlayerShop", "playershop:*", "Shops");
      init(presets, "PlayerShopGUI", "playershopgui:*", "Shops");
      init(presets, "GlobalShop", "globalshop:*", "Shops");
      init(presets, "GlobalShops", "globalshops:*", "Shops");
      init(presets, "CommunityShop", "communityshop:*", "Shops");
      init(presets, "CommunityShops", "communityshops:*", "Shops");
      init(presets, "ServerShop", "servershop:*", "Shops");
      init(presets, "ServerShops", "servershops:*", "Shops");
      init(presets, "VirtualShop", "virtualshop:*", "Shops");
      init(presets, "VirtualShops", "virtualshops:*", "Shops");
      init(presets, "ItemShop", "itemshop:*", "Shops");
      init(presets, "ItemShops", "itemshops:*", "Shops");
      init(presets, "DailyShop", "dailyshop:*", "Shops");
      init(presets, "DailyShops", "dailyshops:*", "Shops");
      init(presets, "RotatingShop", "rotatingshop:*", "Shops");
      init(presets, "RotatingShops", "rotatingshops:*", "Shops");
      init(presets, "VillagerShop", "villagershop:*", "Shops");
      init(presets, "VillagerShops", "villagershops:*", "Shops");
      init(presets, "NPCShop", "npcshop:*", "Shops");
      init(presets, "NPCShops", "npcshops:*", "Shops");
      init(presets, "CitizensShop", "citizensshop:*", "Shops");
      init(presets, "CitizensShops", "citizensshops:*", "Shops");
      init(presets, "HoloShop", "holoshop:*", "Shops");
      init(presets, "HoloShops", "holoshops:*", "Shops");
      init(presets, "ChestShops", "chestshops:*", "Shops");
      init(presets, "ChestShopPlus", "chestshopplus:*", "Shops");
      init(presets, "ShopChestPlus", "shopchestplus:*", "Shops");
      init(presets, "QuickShopHikari", "quickshophikari:*", "Shops");
      init(presets, "QS", "qs:*", "Shops");
      init(presets, "QShop", "qshop:*", "Shops");
      init(presets, "MMOShop", "mmoshop:*", "Shops");
      init(presets, "EpicShop", "epicshop:*", "Shops");
      init(presets, "BlackMarketPlus", "blackmarketplus:*", "Shops");
      init(presets, "BazaarPlus", "bazaarplus:*", "Shops");
      init(presets, "MarketPlus", "marketplus:*", "Shops");
      init(presets, "Markets", "markets:*", "Shops");
      init(presets, "MarketplaceGUI", "marketplacegui:*", "Shops");
      init(presets, "NightExpress", "nightexpress:*", "Frameworks");
      init(presets, "NexEngine", "nexengine:*", "Frameworks");
      init(presets, "PhoenixPlugins", "phoenixplugins:*", "Frameworks");
      init(presets, "PlaceholderAPI", "placeholderapi:*", "Frameworks");
      init(presets, "ProtocolLib", "protocollib:*", "Frameworks");
      init(presets, "PacketEvents", "packetevents:*", "Frameworks");
      init(presets, "LoneLibs", "lonelibs:*", "Frameworks");
      init(presets, "NBTAPI", "nbtapi:*", "Frameworks");
      init(presets, "DonutSMP", "donutsmp:*", "Protection");
      init(presets, "Donut", "donut:*", "Protection");
      init(presets, "Donut Claims", "donutclaims:*", "Protection");
      init(presets, "Claims", "claims:*", "Protection");
      init(presets, "Claim", "claim:*", "Protection");
      init(presets, "Regions", "regions:*", "Protection");
      init(presets, "Region", "region:*", "Protection");
      init(presets, "Protection", "protection:*", "Protection");
      init(presets, "Protections", "protections:*", "Protection");
      init(presets, "AntiGrief", "antigrief:*", "Protection");
      init(presets, "WorldGuard", "worldguard:*", "Protection");
      init(presets, "WorldGuard Regions", "worldguardregions:*", "Protection");
      init(presets, "WorldGuard ExtraFlags", "worldguardextraflags:*", "Protection");
      init(presets, "WG", "wg:*", "Protection");
      init(presets, "WG Region", "wgregion:*", "Protection");
      init(presets, "Residence", "residence:*", "Protection");
      init(presets, "Residence Plus", "residenceplus:*", "Protection");
      init(presets, "GriefPrevention", "griefprevention:*", "Protection");
      init(presets, "Grief Prevention", "grief_prevention:*", "Protection");
      init(presets, "GriefDefender", "griefdefender:*", "Protection");
      init(presets, "GPFlags", "gpflags:*", "Protection");
      init(presets, "Lands", "lands:*", "Protection");
      init(presets, "Land", "land:*", "Protection");
      init(presets, "LandClaim", "landclaim:*", "Protection");
      init(presets, "LandClaims", "landclaims:*", "Protection");
      init(presets, "UltimateClaims", "ultimateclaims:*", "Protection");
      init(presets, "UltimateLandClaim", "ultimatelandclaim:*", "Protection");
      init(presets, "ProtectionStones", "protectionstones:*", "Protection");
      init(presets, "Protection Stones", "protection_stones:*", "Protection");
      init(presets, "PreciousStones", "preciousstones:*", "Protection");
      init(presets, "RedProtect", "redprotect:*", "Protection");
      init(presets, "ClaimChunk", "claimchunk:*", "Protection");
      init(presets, "ChunkClaim", "chunkclaim:*", "Protection");
      init(presets, "ChunkClaims", "chunkclaims:*", "Protection");
      init(presets, "ChunkyClaims", "chunkyclaims:*", "Protection");
      init(presets, "ChunkyProtection", "chunkyprotection:*", "Protection");
      init(presets, "BetterChunkClaims", "betterchunkclaims:*", "Protection");
      init(presets, "RClaim", "rclaim:*", "Protection");
      init(presets, "BetterClaims", "betterclaims:*", "Protection");
      init(presets, "SimpleClaimSystem", "simpleclaimsystem:*", "Protection");
      init(presets, "ClaimSystem", "claimsystem:*", "Protection");
      init(presets, "ClaimPlus", "claimplus:*", "Protection");
      init(presets, "ClaimFly", "claimfly:*", "Protection");
      init(presets, "ClaimShield", "claimshield:*", "Protection");
      init(presets, "ClaimMessenger", "claimmessenger:*", "Protection");
      init(presets, "ClaimMaps", "claimmaps:*", "Protection");
      init_V_List(
         presets,
         "ClaimMessenger Claim",
         "claimmaps:claim",
         "Protection",
         "ClaimMessenger source: CLAIM_CHANNEL = channel(\"claim\") and registered as outgoing plugin channel."
      );
      init_V_List(
         presets,
         "ClaimMessenger No Claims",
         "claimmaps:no_claims",
         "Protection",
         "ClaimMessenger source: NO_CLAIMS_CHANNEL = channel(\"no_claims\") and registered as outgoing plugin channel."
      );
      init_V_List(
         presets,
         "ClaimMessenger Delete",
         "claimmaps:delete_claim",
         "Protection",
         "ClaimMessenger source: DELETE_CHANNEL = channel(\"delete_claim\") and registered as outgoing plugin channel."
      );
      init(presets, "ClaimMap", "claimmap:*", "Protection");
      init(presets, "MapClaims", "mapclaims:*", "Protection");
      init(presets, "ClaimMaps Plugin", "claimmapsplugin:*", "Protection");
      init(presets, "ProtectionCore", "protectioncore:*", "Protection");
      init(presets, "ProtectionPlus", "protectionplus:*", "Protection");
      init(presets, "Homestead", "homestead:*", "Protection");
      init(presets, "Bell Claims", "bellclaims:*", "Protection");
      init(presets, "ClaimsPlus", "claimsplus:*", "Protection");
      init(presets, "ClaimBlocks", "claimblocks:*", "Protection");
      init(presets, "HuskClaims", "huskclaims:*", "Protection");
      init(presets, "HuskTowns", "husktowns:*", "Protection");
      init(presets, "Towny", "towny:*", "Protection");
      init(presets, "TownyAdvanced", "townyadvanced:*", "Protection");
      init(presets, "Factions", "factions:*", "Protection");
      init(presets, "FactionsUUID", "factionsuuid:*", "Protection");
      init(presets, "MassiveFactions", "massivefactions:*", "Protection");
      init(presets, "SaberFactions", "saberfactions:*", "Protection");
      init(presets, "SuperiorFactions", "superiorfactions:*", "Protection");
      init(presets, "Kingdoms", "kingdoms:*", "Protection");
      init(presets, "KingdomsX", "kingdomsx:*", "Protection");
      init(presets, "Konquest", "konquest:*", "Protection");
      init(presets, "PlotSquared", "plotsquared:*", "Protection");
      init(presets, "Plots", "plots:*", "Protection");
      init(presets, "Plot", "plot:*", "Protection");
      init(presets, "PlotMe", "plotme:*", "Protection");
      init(presets, "SuperiorSkyblock", "superiorskyblock:*", "Protection");
      init(presets, "BentoBox", "bentobox:*", "Protection");
      init(presets, "ASkyBlock", "askyblock:*", "Protection");
      init(presets, "BSkyBlock", "bskyblock:*", "Protection");
      init(presets, "FabledSkyBlock", "fabledskyblock:*", "Protection");
      init(presets, "IridiumSkyblock", "iridiumskyblock:*", "Protection");
      init(presets, "LWC", "lwc:*", "Protection");
      init(presets, "LWCX", "lwcx:*", "Protection");
      init(presets, "Lockette", "lockette:*", "Protection");
      init(presets, "LockettePro", "lockettepro:*", "Protection");
      init(presets, "Bolt", "bolt:*", "Protection");
      init(presets, "ChestProtect", "chestprotect:*", "Protection");
      init(presets, "ChestProtection", "chestprotection:*", "Protection");
      init(presets, "CoreProtect", "coreprotect:*", "Protection");
      init_V_List(
         presets,
         "CoreProtect Handshake",
         "coreprotect:handshake",
         "Protection",
         "CoreProtect docs/source: PluginChannelHandshakeListener.pluginChannel is coreprotect:handshake."
      );
      init_V_List(
         presets, "CoreProtect Data", "coreprotect:data", "Protection", "CoreProtect docs/source: PluginChannelListener.pluginChannel is coreprotect:data."
      );
      init(presets, "Prism", "prism:*", "Protection");
      init(presets, "LogBlock", "logblock:*", "Protection");
      init(presets, "BlockLocker", "blocklocker:*", "Protection");
      init(presets, "Open Parties And Claims", "openpartiesandclaims:*", "Protection");
      init_V_List(
         presets,
         "OpenPAC Main",
         "openpartiesandclaims:main",
         "Protection",
         "Open Parties and Claims source: MOD_ID=openpartiesandclaims and MAIN_CHANNEL_LOCATION = ResourceLocation(MOD_ID, \"main\")."
      );
      init(presets, "Open Parties Claims", "openpartiesclaims:*", "Protection");
      init(presets, "OpenPAC", "openpac:*", "Protection");
      init(presets, "Flan", "flan:*", "Protection");
      init(presets, "Cadmus", "cadmus:*", "Protection");
      init(presets, "FTB Chunks", "ftbchunks:*", "Protection");
      init_V_List(
         presets,
         "FTB Chunks Login Data",
         "ftbchunks:login_data_packet",
         "Protection",
         "FTB Chunks source: LoginDataPacket TYPE = FTBChunksAPI.id(\"login_data_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks General Data",
         "ftbchunks:send_general_data_packet",
         "Protection",
         "FTB Chunks source: SendGeneralDataPacket TYPE = FTBChunksAPI.id(\"send_general_data_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Many Chunks",
         "ftbchunks:send_many_chunks_packet",
         "Protection",
         "FTB Chunks source: SendManyChunksPacket TYPE = FTBChunksAPI.id(\"send_many_chunks_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Send Chunk",
         "ftbchunks:send_chunk_packet",
         "Protection",
         "FTB Chunks source: SendChunkPacket TYPE = FTBChunksAPI.id(\"send_chunk_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Loaded View",
         "ftbchunks:loaded_chunk_view_packet",
         "Protection",
         "FTB Chunks source: LoadedChunkViewPacket TYPE = FTBChunksAPI.id(\"loaded_chunk_view_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Request Map",
         "ftbchunks:request_map_data_packet",
         "Protection",
         "FTB Chunks source: RequestMapDataPacket TYPE = FTBChunksAPI.id(\"request_map_data_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Request Change",
         "ftbchunks:request_chunk_change_packet",
         "Protection",
         "FTB Chunks source: RequestChunkChangePacket TYPE = FTBChunksAPI.id(\"request_chunk_change_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Change Response",
         "ftbchunks:chunk_change_response_packet",
         "Protection",
         "FTB Chunks source: ChunkChangeResponsePacket TYPE = FTBChunksAPI.id(\"chunk_change_response_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Open Claim GUI",
         "ftbchunks:open_claim_gui_packet",
         "Protection",
         "FTB Chunks source: OpenClaimGUIPacket TYPE = FTBChunksAPI.id(\"open_claim_gui_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Force Expiry",
         "ftbchunks:update_force_load_expiry_packet",
         "Protection",
         "FTB Chunks source: UpdateForceLoadExpiryPacket TYPE = FTBChunksAPI.id(\"update_force_load_expiry_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Teleport Map",
         "ftbchunks:teleport_from_map_packet",
         "Protection",
         "FTB Chunks source: TeleportFromMapPacket TYPE = FTBChunksAPI.id(\"teleport_from_map_packet\")."
      );
      init_V_List(
         presets, "FTB Chunks TX Sync", "ftbchunks:sync_tx_packet", "Protection", "FTB Chunks source: SyncTXPacket TYPE = FTBChunksAPI.id(\"sync_tx_packet\")."
      );
      init_V_List(
         presets, "FTB Chunks RX Sync", "ftbchunks:sync_rx_packet", "Protection", "FTB Chunks source: SyncRXPacket TYPE = FTBChunksAPI.id(\"sync_rx_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Player Visibility",
         "ftbchunks:player_visibility_packet",
         "Protection",
         "FTB Chunks source: PlayerVisibilityPacket TYPE = FTBChunksAPI.id(\"player_visibility_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Player Death",
         "ftbchunks:player_death_packet",
         "Protection",
         "FTB Chunks source: PlayerDeathPacket TYPE = FTBChunksAPI.id(\"player_death_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Block Color",
         "ftbchunks:request_block_color_packet",
         "Protection",
         "FTB Chunks source: RequestBlockColorPacket TYPE = FTBChunksAPI.id(\"request_block_color_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Add Waypoint",
         "ftbchunks:add_waypoint_packet",
         "Protection",
         "FTB Chunks source: AddWaypointPacket TYPE = FTBChunksAPI.id(\"add_waypoint_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Share Waypoint",
         "ftbchunks:share_waypoint_packet",
         "Protection",
         "FTB Chunks source: ShareWaypointPacket TYPE = FTBChunksAPI.id(\"share_waypoint_packet\")."
      );
      init_V_List(
         presets,
         "FTB Chunks Player Position",
         "ftbchunks:send_player_position_packet",
         "Protection",
         "FTB Chunks source: SendPlayerPositionPacket TYPE = FTBChunksAPI.id(\"send_player_position_packet\")."
      );
      init(presets, "FTB Teams", "ftbteams:*", "Protection");
      init_V_List(presets, "FTB Teams Open GUI", "ftbteams:open_gui", "Protection", "FTB Teams source: OpenGUIMessage TYPE = FTBTeamsAPI.id(\"open_gui\").");
      init_V_List(
         presets,
         "FTB Teams Open My Team",
         "ftbteams:open_my_team_gui",
         "Protection",
         "FTB Teams source: OpenMyTeamGUIMessage TYPE = FTBTeamsAPI.id(\"open_my_team_gui\")."
      );
      init_V_List(
         presets,
         "FTB Teams Create Party",
         "ftbteams:create_party",
         "Protection",
         "FTB Teams source: CreatePartyMessage TYPE = FTBTeamsAPI.id(\"create_party\")."
      );
      init_V_List(
         presets,
         "FTB Teams Player GUI Op",
         "ftbteams:player_gui_operation",
         "Protection",
         "FTB Teams source: PlayerGUIOperationMessage TYPE = FTBTeamsAPI.id(\"player_gui_operation\")."
      );
      init_V_List(
         presets,
         "FTB Teams Send Message",
         "ftbteams:send_message",
         "Protection",
         "FTB Teams source: SendMessageMessage TYPE = FTBTeamsAPI.id(\"send_message\")."
      );
      init_V_List(
         presets,
         "FTB Teams Message Response",
         "ftbteams:send_message_response",
         "Protection",
         "FTB Teams source: SendMessageResponseMessage TYPE = FTBTeamsAPI.id(\"send_message_response\")."
      );
      init_V_List(
         presets, "FTB Teams Sync Teams", "ftbteams:sync_teams", "Protection", "FTB Teams source: SyncTeamsMessage TYPE = FTBTeamsAPI.id(\"sync_teams\")."
      );
      init_V_List(
         presets,
         "FTB Teams Sync History",
         "ftbteams:sync_msg_history",
         "Protection",
         "FTB Teams source: SyncMessageHistoryMessage TYPE = FTBTeamsAPI.id(\"sync_msg_history\")."
      );
      init_V_List(
         presets,
         "FTB Teams Update Request",
         "ftbteams:update_properties_request",
         "Protection",
         "FTB Teams source: UpdatePropertiesRequestMessage TYPE = FTBTeamsAPI.id(\"update_properties_request\")."
      );
      init_V_List(
         presets,
         "FTB Teams Update Response",
         "ftbteams:update_properties_response",
         "Protection",
         "FTB Teams source: UpdatePropertiesResponseMessage TYPE = FTBTeamsAPI.id(\"update_properties_response\")."
      );
      init_V_List(
         presets,
         "FTB Teams Presence",
         "ftbteams:update_presence",
         "Protection",
         "FTB Teams source: UpdatePresenceMessage TYPE = FTBTeamsAPI.id(\"update_presence\")."
      );
      init_V_List(
         presets,
         "FTB Teams Chat Redirect",
         "ftbteams:toggle_chat_redirection",
         "Protection",
         "FTB Teams source: ToggleChatRedirectionMessage TYPE = FTBTeamsAPI.id(\"toggle_chat_redirection\")."
      );
      init_V_List(
         presets,
         "FTB Teams Chat Response",
         "ftbteams:toggle_chat_response",
         "Protection",
         "FTB Teams source: ToggleChatResponseMessage TYPE = FTBTeamsAPI.id(\"toggle_chat_response\")."
      );
      init(presets, "WorldBorder", "worldborder:*", "Protection");
      init(presets, "World Border", "world_border:*", "Protection");
      init(presets, "WorldProtection", "worldprotection:*", "Protection");
      init(presets, "WorldProtector", "worldprotector:*", "Protection");
      init(presets, "RegionClaim", "regionclaim:*", "Protection");
      init(presets, "RegionClaims", "regionclaims:*", "Protection");
      init(presets, "RegionClaims Plus", "regionclaimsplus:*", "Protection");
      init(presets, "WorldGuard Events", "worldguardevents:*", "Protection");
      init(presets, "WorldGuard Events Alt", "worldguard_events:*", "Protection");
      init(presets, "WorldGuard Flags", "worldguardflags:*", "Protection");
      init(presets, "WG Flags", "wgflags:*", "Protection");
      init(presets, "FastAsyncWorldEdit", "fastasyncworldedit:*", "Protection");
      init(presets, "FAWE", "fawe:*", "Protection");
      init(presets, "AdvancedRegionMarket", "advancedregionmarket:*", "Protection");
      init(presets, "ARM", "arm:*", "Protection");
      init(presets, "AreaShop", "areashop:*", "Protection");
      init(presets, "AreaSell", "areasell:*", "Protection");
      init(presets, "Landlord", "landlord:*", "Protection");
      init(presets, "RealEstate", "realestate:*", "Protection");
      init(presets, "RegionMarket", "regionmarket:*", "Protection");
      init(presets, "RegionShop", "regionshop:*", "Protection");
      init(presets, "RegionForSale", "regionforsale:*", "Protection");
      init(presets, "RegionRent", "regionrent:*", "Protection");
      init(presets, "RentIt", "rentit:*", "Protection");
      init(presets, "PlotBorder", "plotborder:*", "Protection");
      init(presets, "PlotSquared v6", "plotsquared6:*", "Protection");
      init(presets, "PlotSquared v7", "plotsquared7:*", "Protection");
      init(presets, "PlotGUI", "plotgui:*", "Protection");
      init(presets, "PlotManager", "plotmanager:*", "Protection");
      init(presets, "PlotSystem", "plotsystem:*", "Protection");
      init(presets, "PlotClaim", "plotclaim:*", "Protection");
      init(presets, "PlotClaims", "plotclaims:*", "Protection");
      init(presets, "SuperiorSkyblock2", "superiorskyblock2:*", "Protection");
      init(presets, "IridiumSkyblock2", "iridiumskyblock2:*", "Protection");
      init(presets, "SkyBlock", "skyblock:*", "Protection");
      init(presets, "USkyBlock", "uskyblock:*", "Protection");
      init(presets, "AcidIsland", "acidisland:*", "Protection");
      init(presets, "IslandWorld", "islandworld:*", "Protection");
      init(presets, "OneBlock", "oneblock:*", "Protection");
      init(presets, "OneBlockSkyBlock", "oneblockskyblock:*", "Protection");
      init(presets, "Island", "island:*", "Protection");
      init(presets, "Islands", "islands:*", "Protection");
      init(presets, "MedievalFactions", "medievalfactions:*", "Protection");
      init(presets, "FactionsKore", "factionskore:*", "Protection");
      init(presets, "FactionsBlue", "factionsblue:*", "Protection");
      init(presets, "FactionsX", "factionsx:*", "Protection");
      init(presets, "FactionsOne", "factionsone:*", "Protection");
      init(presets, "FactionsTop", "factionstop:*", "Protection");
      init(presets, "Clans", "clans:*", "Protection");
      init(presets, "SimpleClans", "simpleclans:*", "Protection");
      init(presets, "UltimateClans", "ultimateclans:*", "Protection");
      init(presets, "BetterTeams", "betterteams:*", "Protection");
      init(presets, "Teams", "teams:*", "Protection");
      init(presets, "Guilds", "guilds:*", "Protection");
      init(presets, "Parties", "parties:*", "Protection");
      init(presets, "Party And Friends", "partyandfriends:*", "Protection");
      init(presets, "Nations", "nations:*", "Protection");
      init(presets, "NationsX", "nationsx:*", "Protection");
      init(presets, "Civs", "civs:*", "Protection");
      init(presets, "Civilizations", "civilizations:*", "Protection");
      init(presets, "Dominion", "dominion:*", "Protection");
      init(presets, "Settlements", "settlements:*", "Protection");
      init(presets, "Villages", "villages:*", "Protection");
      init(presets, "NationsGlory", "nationsglory:*", "Protection");
      init(presets, "BlockProt", "blockprot:*", "Protection");
      init(presets, "ColdContainerLock", "coldcontainerlock:*", "Protection");
      init(presets, "PrivateStorage", "privatestorage:*", "Protection");
      init(presets, "PrivateChests", "privatechests:*", "Protection");
      init(presets, "SecureChests", "securechests:*", "Protection");
      init(presets, "ChestLock", "chestlock:*", "Protection");
      init(presets, "ContainerLock", "containerlock:*", "Protection");
      init(presets, "ContainerLocks", "containerlocks:*", "Protection");
      init(presets, "LockSecurity", "locksecurity:*", "Protection");
      init(presets, "SafeDoors", "safedoors:*", "Protection");
      init(presets, "DoorsReloaded", "doorsreloaded:*", "Protection");
      init(presets, "Ledger", "ledger:*", "Protection");
      init(presets, "HawkEye", "hawkeye:*", "Protection");
      init(presets, "Guardian", "guardian:*", "Protection");
      init(presets, "BlockLogger", "blocklogger:*", "Protection");
      init(presets, "RollbackCore", "rollbackcore:*", "Protection");
      init(presets, "Rollback", "rollback:*", "Protection");
      init(presets, "Insights", "insights:*", "Protection");
      init(presets, "Plan AntiGrief", "planantigrief:*", "Protection");
      init(presets, "IllegalStack", "illegalstack:*", "Protection");
      init(presets, "ExploitFixer", "exploitfixer:*", "Protection");
      init(presets, "IllegalItems", "illegalitems:*", "Protection");
      init(presets, "CrazyCrates", "crazycrates:*", "Crates");
      init(presets, "ExcellentCrates", "excellentcrates:*", "Crates");
      init(presets, "GoldenCrates", "goldencrates:*", "Crates");
      init(presets, "SpecializedCrates", "specializedcrates:*", "Crates");
      init(presets, "CrateReloaded", "cratereloaded:*", "Crates");
      init(presets, "PhoenixCrates", "phoenixcrates:*", "Crates");
      init(presets, "Phoenix Crates Alt", "phoenix_crates:*", "Crates");
      init(presets, "AdvancedCrates", "advancedcrates:*", "Crates");
      init(presets, "AquaticCrates", "aquaticcrates:*", "Crates");
      init(presets, "PulseCrates", "pulsecrates:*", "Crates");
      init(presets, "MysteryCrates", "mysterycrates:*", "Crates");
      init(presets, "CratesPlus", "cratesplus:*", "Crates");
      init(presets, "CrateKeys", "cratekeys:*", "Crates");
      init(presets, "GoldenKey", "goldenkey:*", "Crates");
      init(presets, "DeluxeCrates", "deluxecrates:*", "Crates");
      init(presets, "VoidCrates", "voidcrates:*", "Crates");
      init(presets, "CrazyEnvoys", "crazyenvoys:*", "Crates");
      init(presets, "ExcellentEnchants Crates", "excellentenchants:*", "Crates");
      init(presets, "CrazyRewards", "crazyrewards:*", "Crates");
      init(presets, "LootChest", "lootchest:*", "Crates");
      init(presets, "CrazyCrates Plus", "crazycratesplus:*", "Crates");
      init(presets, "ExcellentCrates Plus", "excellentcratesplus:*", "Crates");
      init(presets, "GoldenCrates Plus", "goldencratesplus:*", "Crates");
      init(presets, "AdvancedCrates Plus", "advancedcratesplus:*", "Crates");
      init(presets, "CrateReloaded Plus", "cratereloadedplus:*", "Crates");
      init(presets, "MagicCrates", "magiccrates:*", "Crates");
      init(presets, "MagicCrate", "magiccrate:*", "Crates");
      init(presets, "SimpleLootCrates", "simplelootcrates:*", "Crates");
      init(presets, "SimpleLootCrate", "simplelootcrate:*", "Crates");
      init(presets, "MineCrates", "minecrates:*", "Crates");
      init(presets, "MineCrate", "minecrate:*", "Crates");
      init(presets, "WonderCrates", "wondercrates:*", "Crates");
      init(presets, "WonderCrate", "wondercrate:*", "Crates");
      init(presets, "SurpriseCrates", "surprisecrates:*", "Crates");
      init(presets, "SurpriseCrate", "surprisecrate:*", "Crates");
      init(presets, "NightCrates", "nightcrates:*", "Crates");
      init(presets, "NightCrate", "nightcrate:*", "Crates");
      init(presets, "CloudCrates", "cloudcrates:*", "Crates");
      init(presets, "CloudCrate", "cloudcrate:*", "Crates");
      init(presets, "EmberCrates", "embercrates:*", "Crates");
      init(presets, "EmberCrate", "embercrate:*", "Crates");
      init(presets, "RewardPro", "rewardpro:*", "Rewards");
      init(presets, "DailyRewards", "dailyrewards:*", "Rewards");
      init(presets, "AdvancedDailyRewards", "advanceddailyrewards:*", "Rewards");
      init(presets, "DeluxeRewards", "deluxerewards:*", "Rewards");
      init(presets, "ExcellentRewards", "excellentrewards:*", "Rewards");
      init(presets, "CasinoSlots", "casinoslots:*", "Gambling");
      init(presets, "SlotMachine", "slotmachine:*", "Gambling");
      init(presets, "SlotMachinePlus", "slotmachineplus:*", "Gambling");
      init(presets, "Slots", "slots:*", "Gambling");
      init(presets, "Roulette", "roulette:*", "Gambling");
      init(presets, "Casino", "casino:*", "Gambling");
      init(presets, "CrazyCasino", "crazycasino:*", "Gambling");
      init(presets, "GambleBar", "gamblebar:*", "Gambling");
      init(presets, "Gamble", "gamble:*", "Gambling");
      init(presets, "Lottery", "lottery:*", "Gambling");
      init(presets, "LotteryPlus", "lotteryplus:*", "Gambling");
      init(presets, "ExcellentCasino", "excellentcasino:*", "Gambling");
      init(presets, "CoinFlip", "coinflip:*", "Gambling");
      init(presets, "DeluxeCoinflip", "deluxecoinflip:*", "Gambling");
      init(presets, "CrazyCoinFlip", "crazycoinflip:*", "Gambling");
      init(presets, "Jackpot", "jackpot:*", "Gambling");
      init(presets, "Crash", "crash:*", "Gambling");
      init(presets, "Mines", "mines:*", "Gambling");
      init(presets, "Blackjack", "blackjack:*", "Gambling");
      init(presets, "RPS", "rps:*", "Gambling");
      init(presets, "Betting", "betting:*", "Gambling");
      init(presets, "Bets", "bets:*", "Gambling");
      init(presets, "BetonQuest", "betonquest:*", "Quests");
      init(presets, "Quests", "quests:*", "Quests");
      init(presets, "BeautyQuests", "beautyquests:*", "Quests");
      init(presets, "Citizens", "citizens:*", "NPCs");
      init(presets, "ZNPCsPlus", "znpcsplus:*", "NPCs");
      init(presets, "DecentHolograms", "decentholograms:*", "Holograms");
      init(presets, "HolographicDisplays", "holographicdisplays:*", "Holograms");
      init_V_List(
         presets,
         "TAB Bridge",
         "tab:bridge-6",
         "UI",
         "TAB source: TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME = \"tab:bridge-6\" and proxy platforms register/send this channel."
      );
      init(presets, "TAB", "tab:*", "UI");
      init(presets, "LibsDisguises", "libsdisguises:*", "Network");
      init_V_List(
         presets,
         "Simple Voice Secret",
         "voicechat:secret",
         "Voice",
         "Simple Voice Chat source: SecretPacket type and proxy SECRET_CHANNEL use voicechat:secret."
      );
      init_V_List(
         presets,
         "Simple Voice Request Secret",
         "voicechat:request_secret",
         "Voice",
         "Simple Voice Chat source: RequestSecretPacket type and proxy REQUEST_SECRET_CHANNEL use voicechat:request_secret."
      );
      init_V_List(presets, "Simple Voice Secret Legacy", "vc:secret", "Voice", "Simple Voice Chat proxy source: SECRET_CHANNEL_1_12 uses vc:secret.");
      init_V_List(
         presets,
         "Simple Voice Request Legacy",
         "vc:request_secret",
         "Voice",
         "Simple Voice Chat proxy source: REQUEST_SECRET_CHANNEL_1_12 uses vc:request_secret."
      );
      init_V_List(presets, "Simple Voice States", "voicechat:states", "Voice", "Simple Voice Chat source: PlayerStatesPacket type is voicechat:states.");
      init_V_List(presets, "Simple Voice State", "voicechat:state", "Voice", "Simple Voice Chat source: PlayerStatePacket type is voicechat:state.");
      init_V_List(
         presets, "Simple Voice Update State", "voicechat:update_state", "Voice", "Simple Voice Chat source: UpdateStatePacket type is voicechat:update_state."
      );
      init_V_List(
         presets,
         "Simple Voice Remove State",
         "voicechat:remove_state",
         "Voice",
         "Simple Voice Chat source: RemovePlayerStatePacket type is voicechat:remove_state."
      );
      init_V_List(presets, "Simple Voice Set Group", "voicechat:set_group", "Voice", "Simple Voice Chat source: JoinGroupPacket type is voicechat:set_group.");
      init_V_List(presets, "Simple Voice Add Group", "voicechat:add_group", "Voice", "Simple Voice Chat source: AddGroupPacket type is voicechat:add_group.");
      init_V_List(
         presets, "Simple Voice Create Group", "voicechat:create_group", "Voice", "Simple Voice Chat source: CreateGroupPacket type is voicechat:create_group."
      );
      init_V_List(
         presets, "Simple Voice Leave Group", "voicechat:leave_group", "Voice", "Simple Voice Chat source: LeaveGroupPacket type is voicechat:leave_group."
      );
      init_V_List(
         presets, "Simple Voice Remove Group", "voicechat:remove_group", "Voice", "Simple Voice Chat source: RemoveGroupPacket type is voicechat:remove_group."
      );
      init_V_List(
         presets, "Simple Voice Joined Group", "voicechat:joined_group", "Voice", "Simple Voice Chat source: JoinedGroupPacket type is voicechat:joined_group."
      );
      init_V_List(
         presets, "Simple Voice Add Category", "voicechat:add_category", "Voice", "Simple Voice Chat source: AddCategoryPacket type is voicechat:add_category."
      );
      init_V_List(
         presets,
         "Simple Voice Remove Category",
         "voicechat:remove_category",
         "Voice",
         "Simple Voice Chat source: RemoveCategoryPacket type is voicechat:remove_category."
      );
      init(presets, "Simple Voice Chat", "voicechat:*", "Voice");
      init(presets, "Simple Voice Legacy", "vc:*", "Voice");
      init_V_List(presets, "Plasmo Voice", "plasmo:voice/v2", "Voice", "Plasmo Voice source: BaseVoiceServer.CHANNEL_STRING is plasmo:voice/v2.");
      init_V_List(
         presets,
         "Plasmo Voice Installed",
         "plasmo:voice/v2/installed",
         "Voice",
         "Plasmo Voice source: BaseVoiceServer.FLAG_CHANNEL_STRING is plasmo:voice/v2/installed."
      );
      init_V_List(
         presets,
         "Plasmo Voice Service",
         "plasmo:voice/v2/service",
         "Voice",
         "Plasmo Voice source: BaseVoiceServer.SERVICE_CHANNEL_STRING is plasmo:voice/v2/service."
      );
      init(presets, "Plasmo Voice", "plasmovoice:*", "Voice");
      init(presets, "Plasmo Voice Protocol", "plasmo:voice/*", "Voice");
      init(presets, "FancyHolograms", "fancyholograms:*", "Plugins");
      init(presets, "FancyNpcs", "fancynpcs:*", "Plugins");
      init(presets, "MythicMobs", "mythicmobs:*", "Plugins");
      init_V_List(
         presets,
         "LuckPerms Update",
         "luckperms:update",
         "Plugins",
         "LuckPerms source: AbstractPluginMessageMessenger.CHANNEL = \"luckperms:update\" and Bukkit/Velocity/Sponge/Forge/Fabric messengers register it."
      );
      init(presets, "LuckPerms", "luckperms:*", "Plugins");
      init(presets, "Forge", "forge:*", "Mod API");
      init(presets, "FML", "fml:*", "Mod API");
      init(presets, "FML Handshake", "fml:handshake*", "Mod API");
      init(presets, "NeoForge", "neoforge:*", "Mod API");
      init(presets, "Quilt", "quilt:*", "Mod API");
      init(presets, "Architectury", "architectury:*", "Mod API");
      init(presets, "Cloth Config", "cloth-config:*", "Mod API");
      init(presets, "ModMenu", "modmenu:*", "Mod API");
      init(presets, "Cardinal Components", "cardinal-components:*", "Mod API");
      init_V_List(
         presets,
         "CCA Entity Sync",
         "cardinal-components:entity_sync",
         "Mod API",
         "Cardinal Components API source: CardinalComponentsEntity.PACKET_ID = ComponentUpdatePayload.id(\"entity_sync\")."
      );
      init_V_List(
         presets,
         "CCA Self Message",
         "cardinal-components:player_message_c2s",
         "Mod API",
         "Cardinal Components API source: CardinalComponentsEntity.C2S_SELF_PACKET_ID = ComponentUpdatePayload.id(\"player_message_c2s\")."
      );
      init_V_List(
         presets,
         "CCA Block Entity Sync",
         "cardinal-components:block_entity_sync",
         "Mod API",
         "Cardinal Components API source: CardinalComponentsBlock.PACKET_ID = ComponentUpdatePayload.id(\"block_entity_sync\")."
      );
      init_V_List(
         presets,
         "CCA Chunk Sync",
         "cardinal-components:chunk_sync",
         "Mod API",
         "Cardinal Components API source: CardinalComponentsChunk.PACKET_ID = ComponentUpdatePayload.id(\"chunk_sync\")."
      );
      init_V_List(
         presets,
         "CCA World Sync",
         "cardinal-components:world_sync",
         "Mod API",
         "Cardinal Components API source: CardinalComponentsLevel.PACKET_ID = ComponentUpdatePayload.id(\"world_sync\")."
      );
      init_V_List(
         presets,
         "CCA Level Sync",
         "cardinal-components:level_sync",
         "Mod API",
         "Cardinal Components API source: CardinalComponentsLevelData.PACKET_ID = ComponentUpdatePayload.id(\"level_sync\")."
      );
      init_V_List(
         presets,
         "CCA Scoreboard Sync",
         "cardinal-components:scoreboard_sync",
         "Mod API",
         "Cardinal Components API source: CardinalComponentsScoreboard.SCOREBOARD_PACKET_ID = ComponentUpdatePayload.id(\"scoreboard_sync\")."
      );
      init_V_List(
         presets,
         "CCA Team Sync",
         "cardinal-components:team_sync",
         "Mod API",
         "Cardinal Components API source: CardinalComponentsScoreboard.TEAM_PACKET_ID = ComponentUpdatePayload.id(\"team_sync\")."
      );
      init(presets, "CCA", "cca:*", "Mod API");
      init(presets, "Polymer", "polymer:*", "Mod API");
      init(presets, "PolyMc", "polymc:*", "Mod API");
      init(presets, "Litematica", "litematica:*", "Client Mods");
      init(presets, "MiniHUD", "minihud:*", "Client Mods");
      init(presets, "Tweakeroo", "tweakeroo:*", "Client Mods");
      init(presets, "MaLiLib", "malilib:*", "Client Mods");
      init(presets, "Xaero Minimap", "xaerominimap:*", "Client Mods");
      init_V_List(
         presets,
         "Xaero Minimap Main",
         "xaerominimap:main",
         "Client Mods",
         "MapModCompanion/minimap-control source: XAERO_MINIMAP_CHANNEL / XAEROS_CHANNEL = \"xaerominimap:main\" and sends plugin messages on it."
      );
      init(presets, "Xaero World Map", "xaeroworldmap:*", "Client Mods");
      init_V_List(
         presets,
         "Xaero World Map Main",
         "xaeroworldmap:main",
         "Client Mods",
         "MapModCompanion/minimap-control source: XAERO_WORLDMAP_CHANNEL / XAEROS_MAP_CHANNEL = \"xaeroworldmap:main\" and sends plugin messages on it."
      );
      init(presets, "JourneyMap", "journeymap:*", "Client Mods");
      init_V_List(
         presets,
         "JourneyMap Permissions",
         "journeymap:perm_req",
         "Client Mods",
         "minimap-control source: JavaMinimapPlugin registers journeymap:perm_req and JMHandler replies on the same channel."
      );
      init_V_List(
         presets,
         "WorldInfo World ID",
         "worldinfo:world_id",
         "Client Mods",
         "MapModCompanion/minimap-control source: WORLDID_CHANNEL / WORLDINFO_CHANNEL = \"worldinfo:world_id\" and sends world-id responses on it."
      );
      init(presets, "VoxelMap", "voxelmap:*", "Client Mods");
      init_V_List(
         presets,
         "VoxelMap Settings",
         "voxelmap:settings",
         "Client Mods",
         "minimap-control source: VoxelHandler.VOXEL_SETTINGS_CHANNEL = \"voxelmap:settings\" and sends settings payloads on it."
      );
      init(presets, "Essential", "essential:*", "Client Mods");
      init_V_List(
         presets,
         "Essential Outfits",
         "essential:outfits",
         "Client Mods",
         "Essential source: OutfitUpdatesPayload.CHANNEL_ID = UIdentifier(\"essential\", \"outfits\") and the custom payload handler registers it."
      );
      init_V_List(
         presets,
         "Emotecraft Emote",
         "emotecraft:emote",
         "Client Mods",
         "Emotecraft source: MOD_ID=emotecraft and CommonData.playEmoteID = \"emote\"; Bukkit registers CommonData.getIDAsString(playEmoteID)."
      );
      init_V_List(
         presets,
         "Emotecraft Stream",
         "emotecraft:stream",
         "Client Mods",
         "Emotecraft source: MOD_ID=emotecraft and CommonData.emoteStreamID = \"stream\"; Fabric/NeoForge register the stream payload."
      );
      init(presets, "Emotecraft", "emotecraft:*", "Client Mods");
      init(presets, "Feather", "feather:*", "Client Mods");
      init(presets, "Lunar Client", "lunarclient:*", "Client Mods");
      init(presets, "Badlion", "badlion:*", "Client Mods");
      init_V_List(
         presets,
         "LabyMod Neo",
         "labymod:neo",
         "Client Mods",
         "LabyMod 4 Server API source: LabyModProtocol uses PayloadChannelIdentifier.create(\"labymod\", \"neo\")."
      );
      init_V_List(presets, "LabyMod 3 Main", "labymod3:main", "Client Mods", "LabyMod docs: protocol messages are sent on plugin channel labymod3:main.");
      init(presets, "LabyMod", "labymod:*", "Client Mods");
      init(presets, "Cosmetics", "cosmetics:*", "Client Mods");
      init(presets, "Cosmetic", "cosmetic:*", "Client Mods");
      init(presets, "SkinsRestorer", "skinsrestorer:*", "Network");
      init_V_List(
         presets,
         "SkinsRestorer Message",
         "sr:messagechannel",
         "Network",
         "SkinsRestorer source: SRHelpers.MESSAGE_CHANNEL = \"sr:messagechannel\" and Bukkit/Bungee/Velocity/Fabric/NeoForge register/send it."
      );
      init(presets, "ViaRewind", "viarewind:*", "Proxy");
      init(presets, "ProtocolSupport", "protocolsupport:*", "Proxy");
      init(presets, "RedisBungee", "redisbungee:*", "Proxy");
      init(presets, "LimboAPI", "limboapi:*", "Proxy");
      init_V_List(
         presets,
         "LimboAuth Mod",
         "limboauth:mod/541f59e4256a337ea252bc482a009d46",
         "Proxy",
         "LimboAuth source: MOD_CHANNEL is MinecraftChannelIdentifier.create(\"limboauth\", \"mod/541f59e4256a337ea252bc482a009d46\") and token messages are sent on getChannelIdentifier(player)."
      );
      init(presets, "LimboAuth", "limboauth:*", "Proxy");
      init_V_List(
         presets,
         "FastLogin Success",
         "fastlogin:succ",
         "Proxy",
         "FastLogin source: SuccessMessage.SUCCESS_CHANNEL = \"succ\"; Bukkit/Velocity register/send namespace fastlogin:succ."
      );
      init_V_List(
         presets,
         "FastLogin Change",
         "fastlogin:ch-st",
         "Proxy",
         "FastLogin source: ChangePremiumMessage.CHANGE_CHANNEL = \"ch-st\"; Bukkit/Velocity register/send namespace fastlogin:ch-st."
      );
      init_V_List(
         presets,
         "FastLogin Force",
         "fastlogin:force",
         "Proxy",
         "FastLogin source: LoginActionMessage.FORCE_CHANNEL = \"force\"; Bukkit registers namespace fastlogin:force as incoming proxy message channel."
      );
      init(presets, "FastLogin", "fastlogin:*", "Proxy");
      init_V_List(
         presets,
         "AuthMe Main",
         "authme:main",
         "Proxy",
         "AuthMeReloaded source: Bukkit service sends authme:main; Bungee/Velocity bridges register and send the same authme:main channel."
      );
      init(presets, "AuthMe", "authme:*", "Proxy");
      init(presets, "AuthMeReloaded", "authmereloaded:*", "Proxy");
      init(presets, "LibreLogin", "librelogin:*", "Proxy");
      init(presets, "NLogin", "nlogin:*", "Proxy");
      init(presets, "OpeNLogin", "openlogin:*", "Proxy");
      init(presets, "LoginSecurity", "loginsecurity:*", "Proxy");
      init(presets, "BungeeGuard", "bungeeguard:*", "Proxy");
      init(presets, "Auth", "auth:*", "Proxy");
      init(presets, "ServerSelectorX", "serverselectorx:*", "Proxy");
      init(presets, "DeluxeHub", "deluxehub:*", "Proxy");
      init(presets, "HubBasics", "hubbasic:*", "Proxy");
      init(presets, "Lobby", "lobby:*", "Proxy");
      init(presets, "Hub", "hub:*", "Proxy");
      init(presets, "ServerListPlus", "serverlistplus:*", "Proxy");
      init(presets, "ServerList", "serverlist:*", "Proxy");
      init(presets, "Economy", "economy:*", "Economy");
      init(presets, "Money", "money:*", "Economy");
      init(presets, "Balance", "balance:*", "Economy");
      init(presets, "Bank", "bank:*", "Economy");
      init(presets, "Banks", "banks:*", "Economy");
      init(presets, "Currency", "currency:*", "Economy");
      init(presets, "Currencies", "currencies:*", "Economy");
      init(presets, "Coins", "coins:*", "Economy");
      init(presets, "Coin", "coin:*", "Economy");
      init(presets, "Tokens", "tokens:*", "Economy");
      init(presets, "Token", "token:*", "Economy");
      init(presets, "Credits", "credits:*", "Economy");
      init(presets, "Credit", "credit:*", "Economy");
      init(presets, "Points", "points:*", "Economy");
      init(presets, "Point", "point:*", "Economy");
      init(presets, "Gems", "gems:*", "Economy");
      init(presets, "Gem", "gem:*", "Economy");
      init(presets, "Crystals", "crystals:*", "Economy");
      init(presets, "Crystal", "crystal:*", "Economy");
      init(presets, "Shards", "shards:*", "Economy");
      init(presets, "Shard", "shard:*", "Economy");
      init(presets, "Bits", "bits:*", "Economy");
      init(presets, "Bit", "bit:*", "Economy");
      init(presets, "CraftConomy", "craftconomy:*", "Economy");
      init(presets, "iConomy", "iconomy:*", "Economy");
      init(presets, "BossEconomy", "bosseconomy:*", "Economy");
      init(presets, "BetterEconomy", "bettereconomy:*", "Economy");
      init(presets, "SimpleEconomy", "simpleeconomy:*", "Economy");
      init(presets, "CrownEconomy", "crowneconomy:*", "Economy");
      init(presets, "RoyaleEconomy", "royaleeconomy:*", "Economy");
      init(presets, "BeastTokens", "beasttokens:*", "Economy");
      init(presets, "TokenEnchant", "tokenenchant:*", "Economy");
      init(presets, "Shop", "shop:*", "Shops");
      init(presets, "Shops", "shops:*", "Shops");
      init(presets, "Store", "store:*", "Shops");
      init(presets, "Stores", "stores:*", "Shops");
      init(presets, "Market", "market:*", "Shops");
      init(presets, "Marketplace", "marketplace:*", "Shops");
      init(presets, "PlayerMarket", "playermarket:*", "Shops");
      init(presets, "PlayerMarketGUI", "playermarketgui:*", "Shops");
      init(presets, "CommunityMarket", "communitymarket:*", "Shops");
      init(presets, "ServerMarket", "servermarket:*", "Shops");
      init(presets, "TradeMarket", "trademarket:*", "Shops");
      init(presets, "TradePost", "tradepost:*", "Shops");
      init(presets, "Bazaar", "bazaar:*", "Shops");
      init(presets, "BlackMarket", "blackmarket:*", "Shops");
      init(presets, "SignShop", "signshop:*", "Shops");
      init(presets, "ShopChest", "shopchest:*", "Shops");
      init(presets, "AdminShop", "adminshop:*", "Shops");
      init(presets, "AutoSell", "autosell:*", "Shops");
      init(presets, "AutoSellChests", "autosellchests:*", "Shops");
      init(presets, "SellChest", "sellchest:*", "Shops");
      init(presets, "SellWands", "sellwands:*", "Shops");
      init(presets, "DynamicShop", "dynamicshop:*", "Shops");
      init(presets, "DynamicShopGUI", "dynamicshopgui:*", "Shops");
      init(presets, "AShop", "ashop:*", "Shops");
      init(presets, "AxShop", "axshop:*", "Shops");
      init(presets, "RoseStacker Shop", "roseshop:*", "Shops");
      init(presets, "AuctionHouse Pro", "auctionhousepro:*", "Auctions");
      init(presets, "AH", "ah:*", "Auctions");
      init(presets, "Auctions", "auctions:*", "Auctions");
      init(presets, "AuctionGUI", "auctiongui:*", "Auctions");
      init(presets, "AuctionPlus", "auctionplus:*", "Auctions");
      init(presets, "AuctionLite", "auctionlite:*", "Auctions");
      init(presets, "AuctionPro", "auctionpro:*", "Auctions");
      init(presets, "Auctioneer", "auctioneer:*", "Auctions");
      init(presets, "CrazyAH", "crazyah:*", "Auctions");
      init(presets, "ZAuction", "zauction:*", "Auctions");
      init(presets, "HuskAuctions", "huskauctions:*", "Auctions");
      init(presets, "PlayerAuction", "playerauction:*", "Auctions");
      init(presets, "CommunityAuction", "communityauction:*", "Auctions");
      init(presets, "MarketAuctions", "marketauctions:*", "Auctions");
      init(presets, "Crate", "crate:*", "Crates");
      init(presets, "Crates", "crates:*", "Crates");
      init(presets, "Keys", "keys:*", "Crates");
      init(presets, "Key", "key:*", "Crates");
      init(presets, "VaultCrates", "vaultcrates:*", "Crates");
      init(presets, "PhoenixCrates Lite", "phoenixcrateslite:*", "Crates");
      init(presets, "Phoenix Crates Lite", "phoenix_crates_lite:*", "Crates");
      init(presets, "LootCrates", "lootcrates:*", "Crates");
      init(presets, "LootCrate", "lootcrate:*", "Crates");
      init(presets, "VoteCrates", "votecrates:*", "Crates");
      init(presets, "VoteCrate", "votecrate:*", "Crates");
      init(presets, "ProCrates", "procrates:*", "Crates");
      init(presets, "EpicCrates", "epiccrates:*", "Crates");
      init(presets, "UltraCrates", "ultracrates:*", "Crates");
      init(presets, "RoyalCrates", "royalcrates:*", "Crates");
      init(presets, "CrateOpener", "crateopener:*", "Crates");
      init(presets, "CratePreview", "cratepreview:*", "Crates");
      init(presets, "MysteryBox", "mysterybox:*", "Crates");
      init(presets, "MysteryBoxes", "mysteryboxes:*", "Crates");
      init(presets, "Cases", "cases:*", "Crates");
      init(presets, "CaseOpening", "caseopening:*", "Crates");
      init(presets, "LootBoxes", "lootboxes:*", "Crates");
      init(presets, "LootBox", "lootbox:*", "Crates");
      init(presets, "RewardCrates", "rewardcrates:*", "Crates");
      init(presets, "CasinoGames", "casinogames:*", "Gambling");
      init(presets, "Vegas", "vegas:*", "Gambling");
      init(presets, "SlotMachines", "slotmachines:*", "Gambling");
      init(presets, "SlotsCasino", "slotscasino:*", "Gambling");
      init(presets, "RoulettePlus", "rouletteplus:*", "Gambling");
      init(presets, "LuckyWheel", "luckywheel:*", "Gambling");
      init(presets, "WheelOfFortune", "wheeloffortune:*", "Gambling");
      init(presets, "Wheel", "wheel:*", "Gambling");
      init(presets, "Dice", "dice:*", "Gambling");
      init(presets, "DiceGames", "dicegames:*", "Gambling");
      init(presets, "Poker", "poker:*", "Gambling");
      init(presets, "Keno", "keno:*", "Gambling");
      init(presets, "Baccarat", "baccarat:*", "Gambling");
      init(presets, "Plinko", "plinko:*", "Gambling");
      init(presets, "CaseBattles", "casebattles:*", "Gambling");
      init(presets, "CoinToss", "cointoss:*", "Gambling");
      init(presets, "HeadsOrTails", "heads_or_tails:*", "Gambling");
      init(presets, "LuckyBlock", "luckyblock:*", "Rewards");
      init(presets, "LuckyBlocks", "luckyblocks:*", "Rewards");
      init(presets, "VoteParty", "voteparty:*", "Rewards");
      init(presets, "Voting", "voting:*", "Rewards");
      init(presets, "Vote", "vote:*", "Rewards");
      init(presets, "NuVotifier", "nuvotifier:*", "Rewards");
      init(presets, "Votifier", "votifier:*", "Rewards");
      init(presets, "Reward", "reward:*", "Rewards");
      init(presets, "Rewards", "rewards:*", "Rewards");
      init(presets, "Daily", "daily:*", "Rewards");
      init(presets, "Streaks", "streaks:*", "Rewards");
      init(presets, "BattlePass", "battlepass:*", "Rewards");
      init(presets, "AdvancedBattlePass", "advancedbattlepass:*", "Rewards");
      init(presets, "Pass", "pass:*", "Rewards");
      init(presets, "QuestsPlus", "questsplus:*", "Quests");
      init(presets, "Quest", "quest:*", "Quests");
      init(presets, "QuestCreator", "questcreator:*", "Quests");
      init(presets, "NotQuests", "notquests:*", "Quests");
      init(presets, "Bounties", "bounties:*", "Quests");
      init(presets, "BountyHunters", "bountyhunters:*", "Quests");
      init(presets, "PlayerBounties", "playerbounties:*", "Quests");
      init(presets, "McMMO", "mcmmo:*", "Skills");
      init(presets, "AuraSkills", "auraskills:*", "Skills");
      init(presets, "AureliumSkills Alt", "skills:*", "Skills");
      init(presets, "LevelledMobs", "levelledmobs:*", "Mobs");
      init(presets, "MythicLib", "mythiclib:*", "Mobs");
      init(presets, "EliteMobs", "elitemobs:*", "Mobs");
      init(presets, "BossMania", "bossmania:*", "Mobs");
      init(presets, "Boss", "boss:*", "Mobs");
      init(presets, "Bosses", "bosses:*", "Mobs");
      init(presets, "ModelEngine4", "modelengine4:*", "Items");
      init(presets, "MythicDungeons", "mythicdungeons:*", "Items");
      init(presets, "CustomItems", "customitems:*", "Items");
      init(presets, "CustomBlocks", "customblocks:*", "Items");
      init(presets, "CustomCrops", "customcrops:*", "Items");
      init(presets, "CustomFishing", "customfishing:*", "Items");
      init(presets, "Items", "items:*", "Items");
      init(presets, "Item", "item:*", "Items");
      init(presets, "ItemBridge", "itembridge:*", "Items");
      init(presets, "Eco", "eco:*", "Items");
      init(presets, "EcoSkills", "ecoskills:*", "Items");
      init(presets, "EcoPets", "ecopets:*", "Items");
      init(presets, "EcoCrates", "ecocrates:*", "Items");
      init(presets, "EcoJobs", "ecojobs:*", "Items");
      init(presets, "CustomEnchants", "customenchants:*", "Items");
      init(presets, "AdvancedEnchantments", "advancedenchantments:*", "Items");
      init(presets, "CrazyEnchantments", "crazyenchantments:*", "Items");
      init(presets, "Enchants", "enchants:*", "Items");
      init(presets, "Enchantments", "enchantments:*", "Items");
      init(presets, "Menus", "menus:*", "Menus");
      init(presets, "Menu", "menu:*", "Menus");
      init(presets, "GUI", "gui:*", "Menus");
      init(presets, "GUIs", "guis:*", "Menus");
      init(presets, "InventoryGUI", "inventorygui:*", "Menus");
      init(presets, "SmartInvs", "smartinvs:*", "Menus");
      init(presets, "TriumphGUI", "triumphgui:*", "Menus");
      init(presets, "IF", "if:*", "Menus");
      init(presets, "InventoryFramework", "inventoryframework:*", "Menus");
      init(presets, "CommandPanel", "commandpanel:*", "Menus");
      init(presets, "AnimatedMenu", "animatedmenu:*", "Menus");
      init(presets, "DeluxeTags", "deluxetags:*", "Chat");
      init(presets, "ChatControl", "chatcontrol:*", "Chat");
      init(presets, "ChatControlRed", "chatcontrolred:*", "Chat");
      init(presets, "VentureChat", "venturechat:*", "Chat");
      init(presets, "DeluxeChat", "deluxechat:*", "Chat");
      init(presets, "HeroChat", "herochat:*", "Chat");
      init_V_List(
         presets,
         "InteractiveChat Main",
         "interchat:main",
         "Chat",
         "InteractiveChat source: Bukkit registers interchat:main incoming/outgoing and proxy code registers/sends interchat:main."
      );
      init(presets, "InteractiveChat", "interactivechat:*", "Chat");
      init(presets, "InteractiveChatDiscordSRV", "interactivechatdiscordsrv:*", "Chat");
      init(presets, "DiscordSRV", "discordsrv:*", "Chat");
      init(presets, "Chat", "chat:*", "Chat");
      init(presets, "ChatGames", "chatgames:*", "Chat");
      init(presets, "TAB Premium", "tabpremium:*", "UI");
      init(presets, "TAB Reborn", "tabreborn:*", "UI");
      init(presets, "NametagEdit", "nametagedit:*", "UI");
      init(presets, "AnimatedNames", "animatednames:*", "UI");
      init(presets, "Scoreboard", "scoreboard:*", "UI");
      init(presets, "Scoreboards", "scoreboards:*", "UI");
      init(presets, "FeatherBoard", "featherboard:*", "UI");
      init(presets, "AnimatedScoreboard", "animatedscoreboard:*", "UI");
      init(presets, "Hologram", "hologram:*", "Holograms");
      init(presets, "Holograms", "holograms:*", "Holograms");
      init(presets, "CMIHolograms", "cmiholograms:*", "Holograms");
      init(presets, "HD", "hd:*", "Holograms");
      init(presets, "FancyNPCs Alt", "fancynpc:*", "NPCs");
      init(presets, "ZNPCs", "znpcs:*", "NPCs");
      init(presets, "NPC", "npc:*", "NPCs");
      init(presets, "NPCs", "npcs:*", "NPCs");
      init(presets, "FancyCitizens", "fancycitizens:*", "NPCs");
      init(presets, "Sentinel", "sentinel:*", "NPCs");
      init(presets, "Denizen", "denizen:*", "NPCs");
      init(presets, "LibsDisguises Alt", "libsdisguise:*", "Network");
      init(presets, "Disguise", "disguise:*", "Network");
      init(presets, "Disguises", "disguises:*", "Network");
      init(presets, "Protocolize", "protocolize:*", "Network");
      init(presets, "PacketWrapper", "packetwrapper:*", "Network");
      init(presets, "CommandAPI", "commandapi:*", "Frameworks");
      init(presets, "Cloud", "cloud:*", "Frameworks");
      init(presets, "Aikar Commands", "acf:*", "Frameworks");
      init(presets, "InventoryLib", "inventorylib:*", "Frameworks");
      init(presets, "XSeries", "xseries:*", "Frameworks");
      init(presets, "RoseGarden", "rosegarden:*", "Frameworks");
      init(presets, "BlueSlimeCore", "blueslimecore:*", "Frameworks");
      init(presets, "Placeholder", "placeholder:*", "Frameworks");
      init(presets, "PAPI", "papi:*", "Frameworks");
      init(presets, "Worlds", "worlds:*", "Worlds");
      init(presets, "World", "world:*", "Worlds");
      init(presets, "Multiverse", "multiverse:*", "Worlds");
      init(presets, "MultiverseCore", "multiversecore:*", "Worlds");
      init(presets, "MultiverseInventories", "multiverseinventories:*", "Worlds");
      init(presets, "MultiWorld", "multiworld:*", "Worlds");
      init(presets, "WorldManager", "worldmanager:*", "Worlds");
      init(presets, "Terra", "terra:*", "Worlds");
      init(presets, "TerraformGenerator", "terraformgenerator:*", "Worlds");
      init(presets, "Iris", "iris:*", "Worlds");
      init(presets, "Dynmap", "dynmap:*", "Maps");
      init(presets, "BlueMap", "bluemap:*", "Maps");
      init(presets, "Squaremap", "squaremap:*", "Maps");
      init(presets, "Pl3xMap", "pl3xmap:*", "Maps");
      init(presets, "Overviewer", "overviewer:*", "Maps");
      init_V_List(presets, "Axiom Enable", "axiom:enable", "Admin", "Axiom Paper source: AxiomPaper registers/sends axiom:enable to clients.");
      init_V_List(
         presets,
         "Axiom Redo Handshake",
         "axiom:redo_handshake",
         "Admin",
         "Axiom Paper source: AxiomPaper sends axiom:redo_handshake when permissions recover."
      );
      init_V_List(
         presets,
         "Axiom Chunk Data",
         "axiom:response_chunk_data",
         "Admin",
         "Axiom Paper source: AxiomPaper registers axiom:response_chunk_data and RequestChunkDataPacketListener sends it."
      );
      init_V_List(
         presets,
         "Axiom Entity Data",
         "axiom:response_entity_data",
         "Admin",
         "Axiom Paper source: RequestEntityDataPacketListener RESPONSE_ID is axiom:response_entity_data."
      );
      init_V_List(
         presets,
         "Axiom Blueprint",
         "axiom:response_blueprint",
         "Admin",
         "Axiom Paper source: BlueprintRequestPacketListener RESPONSE_PACKET_IDENTIFIER is axiom:response_blueprint."
      );
      init_V_List(
         presets,
         "Axiom Blueprint Manifest",
         "axiom:blueprint_manifest",
         "Admin",
         "Axiom Paper source: ServerBlueprintManager PACKET_BLUEPRINT_MANIFEST_IDENTIFIER is axiom:blueprint_manifest."
      );
      init_V_List(
         presets,
         "Axiom World Properties",
         "axiom:register_world_properties",
         "Admin",
         "Axiom Paper source: AxiomPaper registers/sends axiom:register_world_properties."
      );
      init_V_List(
         presets,
         "Axiom Set World Property",
         "axiom:set_world_property",
         "Admin",
         "Axiom Paper source: AxiomPaper registers axiom:set_world_property as an outgoing plugin channel."
      );
      init_V_List(
         presets,
         "Axiom Ack World Properties",
         "axiom:ack_world_properties",
         "Admin",
         "Axiom Paper source: AxiomPaper registers axiom:ack_world_properties and SetWorldPropertyListener sends it."
      );
      init_V_List(
         presets, "Axiom Restrictions", "axiom:restrictions", "Admin", "Axiom Paper source: AxiomPaper registers axiom:restrictions and Restrictions sends it."
      );
      init_V_List(
         presets, "Axiom Marker Data", "axiom:marker_data", "Admin", "Axiom Paper source: AxiomPaper registers axiom:marker_data and WorldExtension sends it."
      );
      init_V_List(
         presets,
         "Axiom Marker NBT",
         "axiom:marker_nbt_response",
         "Admin",
         "Axiom Paper source: AxiomPaper registers axiom:marker_nbt_response and MarkerNbtRequestPacketListener sends it."
      );
      init_V_List(
         presets,
         "Axiom Annotation",
         "axiom:annotation_update",
         "Admin",
         "Axiom Paper source: AxiomPaper registers axiom:annotation_update and ServerAnnotations sends it."
      );
      init_V_List(
         presets,
         "Axiom Heightmap",
         "axiom:add_server_heightmap",
         "Admin",
         "Axiom Paper source: AxiomPaper registers axiom:add_server_heightmap and ServerHeightmaps sends it."
      );
      init_V_List(
         presets,
         "Axiom Custom Blocks",
         "axiom:custom_blocks",
         "Admin",
         "Axiom Paper source: AxiomPaper registers axiom:custom_blocks and ImplServerCustomBlocks sends it."
      );
      init_V_List(
         presets,
         "Axiom Custom Block V2",
         "axiom:register_custom_block_v2",
         "Admin",
         "Axiom Paper source: AxiomPaper registers/sends axiom:register_custom_block_v2."
      );
      init_V_List(
         presets,
         "Axiom Ignore Displays",
         "axiom:ignore_display_entities",
         "Admin",
         "Axiom Paper source: AxiomPaper registers axiom:ignore_display_entities as an outgoing plugin channel."
      );
      init_V_List(
         presets,
         "Axiom Custom Items",
         "axiom:register_custom_items",
         "Admin",
         "Axiom Paper source: AxiomPaper registers axiom:register_custom_items and ImplServerCustomDisplays sends it."
      );
      init_V_List(
         presets,
         "Axiom Dispatch Budget",
         "axiom:update_available_dispatch_sends",
         "Admin",
         "Axiom Paper source: AxiomPaper sends axiom:update_available_dispatch_sends."
      );
      init(presets, "Plan", "plan:*", "Admin");
      init(presets, "Spark", "spark:*", "Admin");
      init(presets, "LiteBans", "litebans:*", "Admin");
      init(presets, "AdvancedBan", "advancedban:*", "Admin");
      init(presets, "LibertyBans", "libertybans:*", "Admin");
      init(presets, "BanManager", "banmanager:*", "Admin");
      init(presets, "StaffPlus", "staffplus:*", "Admin");
      init(presets, "StaffPlusPlus", "staffplusplus:*", "Admin");
      init(presets, "StaffFacilities", "stafffacilities:*", "Admin");
      init(presets, "SuperVanish", "supervanish:*", "Admin");
      init(presets, "PremiumVanish", "premiumvanish:*", "Admin");
      init(presets, "Vanish", "vanish:*", "Admin");
      init(presets, "VanishNoPacket", "vanishnopacket:*", "Admin");
      init(presets, "Maintenance", "maintenance:*", "Admin");
      init(presets, "LuckPerms Verbose", "lp:*", "Admin");
      init(presets, "Permissions", "permissions:*", "Admin");
      init(presets, "PermissionsEx", "permissionsex:*", "Admin");
      init(presets, "UltraPermissions", "ultrapermissions:*", "Admin");
      init(presets, "Vulcan", "vulcan:*", "AntiCheat");
      init(presets, "Grim", "grim:*", "AntiCheat");
      init(presets, "GrimAC", "grimac:*", "AntiCheat");
      init(presets, "Matrix", "matrix:*", "AntiCheat");
      init(presets, "Karhu", "karhu:*", "AntiCheat");
      init(presets, "Spartan", "spartan:*", "AntiCheat");
      init(presets, "AAC", "aac:*", "AntiCheat");
      init(presets, "NoCheatPlus", "nocheatplus:*", "AntiCheat");
      init(presets, "NCP", "ncp:*", "AntiCheat");
      init(presets, "Intave", "intave:*", "AntiCheat");
      init(presets, "Polar", "polar:*", "AntiCheat");
      init(presets, "Verus", "verus:*", "AntiCheat");
      init(presets, "Themis", "themis:*", "AntiCheat");
      init(presets, "Negativity", "negativity:*", "AntiCheat");
      init(presets, "Horizon", "horizon:*", "AntiCheat");
      init(presets, "Reflex", "reflex:*", "AntiCheat");
      init(presets, "AntiAura", "antiaura:*", "AntiCheat");
      init(presets, "WatchCat", "watchcat:*", "AntiCheat");
      init(presets, "AntiCheat", "anticheat:*", "AntiCheat");
      init(presets, "AntiCheats", "anticheats:*", "AntiCheat");
      init(presets, "BedWars1058", "bedwars1058:*", "Minigames");
      init(presets, "BedWars", "bedwars:*", "Minigames");
      init(presets, "BW", "bw:*", "Minigames");
      init(presets, "SkyWars", "skywars:*", "Minigames");
      init(presets, "SW", "sw:*", "Minigames");
      init(presets, "SurvivalGames", "survivalgames:*", "Minigames");
      init(presets, "HungerGames", "hungergames:*", "Minigames");
      init(presets, "MurderMystery", "murdermystery:*", "Minigames");
      init(presets, "BuildBattle", "buildbattle:*", "Minigames");
      init(presets, "Spleef", "spleef:*", "Minigames");
      init(presets, "TNTRun", "tntrun:*", "Minigames");
      init(presets, "BlockHunt", "blockhunt:*", "Minigames");
      init(presets, "HideAndSeek", "hideandseek:*", "Minigames");
      init(presets, "Duels", "duels:*", "Minigames");
      init(presets, "KitPvP", "kitpvp:*", "Minigames");
      init(presets, "Arena", "arena:*", "Minigames");
      init(presets, "Arenas", "arenas:*", "Minigames");
      init(presets, "Minigames", "minigames:*", "Minigames");
      init(presets, "MiniGame", "minigame:*", "Minigames");
      init(presets, "PartyGames", "partygames:*", "Minigames");
      init(presets, "Parkour", "parkour:*", "Minigames");
      init(presets, "ParkourPlus", "parkourplus:*", "Minigames");
      init(presets, "TheBridge", "thebridge:*", "Minigames");
      init(presets, "Bridge", "bridge:*", "Minigames");
      init(presets, "SkyBlock Menu", "skyblockmenu:*", "Minigames");
      init(presets, "Prison", "prison:*", "Prison");
      init(presets, "PrisonMines", "prisonmines:*", "Prison");
      init(presets, "MinePacks", "minepacks:*", "Prison");
      init(presets, "Mine", "mine:*", "Prison");
      init(presets, "EzRanksPro", "ezrankspro:*", "Prison");
      init(presets, "AutoRank", "autorank:*", "Prison");
      init(presets, "Rankup", "rankup:*", "Prison");
      init(presets, "RankupX", "rankupx:*", "Prison");
      init(presets, "Rankup3", "rankup3:*", "Prison");
      init(presets, "Backpacks", "backpacks:*", "Storage");
      init(presets, "Backpack", "backpack:*", "Storage");
      init(presets, "AdvancedBackpacks", "advancedbackpacks:*", "Storage");
      init(presets, "PlayerVaults", "playervaults:*", "Storage");
      init(presets, "PlayerVaultsX", "playervaultsx:*", "Storage");
      init(presets, "EnderVaults", "endervaults:*", "Storage");
      init(presets, "PV", "pv:*", "Storage");
      init(presets, "Vaults", "vaults:*", "Storage");
      init(presets, "Lifesteal", "lifesteal:*", "SMP");
      init(presets, "LifeStealCore", "lifestealcore:*", "SMP");
      init(presets, "Hearts", "hearts:*", "SMP");
      init(presets, "Heart", "heart:*", "SMP");
      init(presets, "CombatLogX", "combatlogx:*", "SMP");
      init(presets, "CombatLog", "combatlog:*", "SMP");
      init(presets, "CombatPlus", "combatplus:*", "SMP");
      init(presets, "Combat", "combat:*", "SMP");
      init_V_List(
         presets,
         "Consumable Optimizer Disable",
         "consumable_optimizer:disable_payload",
         "Security",
         "Consumable Optimizer source/docs: Paper opt-out registers/sends consumable_optimizer:disable_payload."
      );
      init_V_List(
         presets,
         "Consumable Optimizer Handshake",
         "consumable_optimizer:handshake_payload",
         "Security",
         "Consumable Optimizer source/docs: Paper opt-out registers consumable_optimizer:handshake_payload as the client handshake channel."
      );
      init_V_List(
         presets,
         "FastCrystal Disable",
         "clientcrystal:disable_fast_crystal",
         "Security",
         "FastCrystal source/docs: Paper/Spigot opt-out sends clientcrystal:disable_fast_crystal."
      );
      init_V_List(
         presets,
         "FastCrystal Fabric Disable",
         "fastcrystal:disable_fast_crystal",
         "Security",
         "FastCrystal source/docs: Fabric server opt-out payload id is fastcrystal:disable_fast_crystal."
      );
      init_V_List(presets, "OptiPlus Disable", "optiplus:disable", "Security", "OptiPlus public docs: optiplus:disable disables both OptiPlus variants.");
      init_V_List(
         presets,
         "OptiPlus Crystal Disable",
         "optiplus-crystal:disable",
         "Security",
         "OptiPlus-Crystal docs: Paper/Spigot/Bukkit opt-out registers/sends optiplus-crystal:disable."
      );
      init_V_List(
         presets,
         "OptiPlus Anchor Disable",
         "optiplus-anchor:disable",
         "Security",
         "OptiPlus-Anchor docs: Paper/Spigot/Bukkit opt-out registers/sends optiplus-anchor:disable."
      );
      init(presets, "DupeFixes", "dupefixes:*", "Security");
      init(presets, "DupeFix", "dupefix:*", "Security");
      init(presets, "ExploitFixes", "exploitfixes:*", "Security");
      init(presets, "AntiExploit", "antiexploit:*", "Security");
      init(presets, "AntiDupe", "antidupe:*", "Security");
      init(presets, "ItemFilter", "itemfilter:*", "Security");
      init(presets, "IllegalItem", "illegalitem:*", "Security");
      init(presets, "IllegalItemsPlus", "illegalitemsplus:*", "Security");
      init(presets, "LifeStealZ", "lifestealz:*", "SMP");
      init(presets, "LifeStealPlus", "lifestealplus:*", "SMP");
      init(presets, "LifeSteal SMP", "lifestealsmp:*", "SMP");
      init(presets, "LifeStealCoreX", "lifestealcorex:*", "SMP");
      init(presets, "SimpleLifeSteal", "simplelifesteal:*", "SMP");
      init(presets, "PLifeSteal", "plifesteal:*", "SMP");
      init(presets, "LS SMP", "lssmp:*", "SMP");
      init(presets, "SMP", "smp:*", "SMP");
      init(presets, "Survival", "survival:*", "SMP");
      init(presets, "SurvivalCore", "survivalcore:*", "SMP");
      init(presets, "SurvivalPlus", "survivalplus:*", "SMP");
      init(presets, "SurvivalSystem", "survivalsystem:*", "SMP");
      init(presets, "SMPCore", "smpcore:*", "SMP");
      init(presets, "SMPSystem", "smpsystem:*", "SMP");
      init(presets, "SMPPlus", "smpplus:*", "SMP");
      init(presets, "HeartsPlus", "heartsplus:*", "SMP");
      init(presets, "HeartSystem", "heartsystem:*", "SMP");
      init(presets, "Revive", "revive:*", "SMP");
      init(presets, "Revives", "revives:*", "SMP");
      init(presets, "ReviveMe", "reviveme:*", "SMP");
      init(presets, "RevivePlus", "reviveplus:*", "SMP");
      init(presets, "DeathBan", "deathban:*", "SMP");
      init(presets, "DeathBans", "deathbans:*", "SMP");
      init(presets, "CombatTag", "combattag:*", "SMP");
      init(presets, "CombatTagPlus", "combattagplus:*", "SMP");
      init(presets, "CombatTimer", "combattimer:*", "SMP");
      init(presets, "CombatManager", "combatmanager:*", "SMP");
      init(presets, "PvPManager", "pvpmanager:*", "SMP");
      init(presets, "PvPManager Lite", "pvpmanagerlite:*", "SMP");
      init(presets, "PvPManager Premium", "pvpmanagerpremium:*", "SMP");
      init(presets, "PvPToggle", "pvptoggle:*", "SMP");
      init(presets, "PvPControl", "pvpcontrol:*", "SMP");
      init(presets, "KeepInventory", "keepinventory:*", "SMP");
      init(presets, "DeathChest", "deathchest:*", "SMP");
      init(presets, "DeathChests", "deathchests:*", "SMP");
      init(presets, "AngelChest", "angelchest:*", "SMP");
      init(presets, "Graves", "graves:*", "SMP");
      init(presets, "Grave", "grave:*", "SMP");
      init(presets, "GraveStones", "gravestones:*", "SMP");
      init(presets, "GraveStone", "gravestone:*", "SMP");
      init(presets, "BetterRTP", "betterrtp:*", "SMP");
      init(presets, "RandomTeleport", "randomteleport:*", "SMP");
      init(presets, "RTP", "rtp:*", "SMP");
      init(presets, "Homes", "homes:*", "SMP");
      init(presets, "Home", "home:*", "SMP");
      init(presets, "SetHome", "sethome:*", "SMP");
      init(presets, "Warp", "warp:*", "SMP");
      init(presets, "Warps", "warps:*", "SMP");
      init(presets, "BetterWarps", "betterwarps:*", "SMP");
      init(presets, "Teleport", "teleport:*", "SMP");
      init(presets, "TPA", "tpa:*", "SMP");
      init(presets, "Back", "back:*", "SMP");
      init(presets, "Spawn", "spawn:*", "SMP");
      init(presets, "SetSpawn", "setspawn:*", "SMP");
      init(presets, "SleepMost", "sleepmost:*", "SMP");
      init(presets, "Harbor", "harbor:*", "SMP");
      init(presets, "OnePlayerSleep", "oneplayersleep:*", "SMP");
      init(presets, "SinglePlayerSleep", "singleplayersleep:*", "SMP");
      init(presets, "VeinMiner", "veinminer:*", "SMP");
      init(presets, "OreAnnouncer", "oreannouncer:*", "SMP");
      init(presets, "TreeFeller", "treefeller:*", "SMP");
      init(presets, "TreeAssist", "treeassist:*", "SMP");
      init(presets, "Timber", "timber:*", "SMP");
      init(presets, "SilkSpawners", "silkspawners:*", "SMP");
      init(presets, "WildStacker", "wildstacker:*", "SMP");
      init(presets, "RoseStacker", "rosestacker:*", "SMP");
      init(presets, "StackMob", "stackmob:*", "SMP");
      init(presets, "Chunky", "chunky:*", "SMP");
      init(presets, "ChunkyBorder", "chunkyborder:*", "SMP");
      init(presets, "ChunkyBorder Alt", "chunky:border*", "SMP");
      init(presets, "AnarchyExploitFixes", "anarchyexploitfixes:*", "Security");
      init(presets, "AEF", "aef:*", "Security");
      init(presets, "ExploitFixer API", "exploitfixerapi:*", "Security");
      init(presets, "HamsterAPI", "hamsterapi:*", "Security");
      init(presets, "DupeFixer", "dupefixer:*", "Security");
      init(presets, "DupePatches", "dupepatches:*", "Security");
      init(presets, "DupeProtection", "dupeprotection:*", "Security");
      init(presets, "AntiDuplication", "antiduplication:*", "Security");
      init(presets, "AntiBookBan", "antibookban:*", "Security");
      init(presets, "BookExploitFix", "bookexploitfix:*", "Security");
      init(presets, "BookFix", "bookfix:*", "Security");
      init(presets, "BookLimiter", "booklimiter:*", "Security");
      init(presets, "BookGuard", "bookguard:*", "Security");
      init(presets, "PacketLimiter", "packetlimiter:*", "Security");
      init(presets, "PacketFilter", "packetfilter:*", "Security");
      init(presets, "PacketGuard", "packetguard:*", "Security");
      init(presets, "PacketSecurity", "packetsecurity:*", "Security");
      init(presets, "PacketEvents Security", "packeteventssecurity:*", "Security");
      init(presets, "PacketAntiCrash", "packetanticrash:*", "Security");
      init(presets, "AntiCrash", "anticrash:*", "Security");
      init(presets, "CrashFix", "crashfix:*", "Security");
      init(presets, "CrashFixer", "crashfixer:*", "Security");
      init(presets, "CrashGuard", "crashguard:*", "Security");
      init(presets, "AntiCrashPlus", "anticrashplus:*", "Security");
      init(presets, "ExploitPatch", "exploitpatch:*", "Security");
      init(presets, "ExploitPatcher", "exploitpatcher:*", "Security");
      init(presets, "ExploitProtection", "exploitprotection:*", "Security");
      init(presets, "ExploitGuard", "exploitguard:*", "Security");
      init(presets, "AntiExploitPlus", "antiexploitplus:*", "Security");
      init(presets, "AntiCrashReloaded", "anticrashreloaded:*", "Security");
      init(presets, "IllegalStackRemover", "illegalstackremover:*", "Security");
      init(presets, "IllegalStackFixer", "illegalstackfixer:*", "Security");
      init(presets, "IllegalStackFix", "illegalstackfix:*", "Security");
      init(presets, "IllegalItemRemover", "illegalitemremover:*", "Security");
      init(presets, "IllegalItemFixer", "illegalitemfixer:*", "Security");
      init(presets, "IllegalItemFix", "illegalitemfix:*", "Security");
      init(presets, "IllegalEnchants", "illegalenchants:*", "Security");
      init(presets, "OverstackFix", "overstackfix:*", "Security");
      init(presets, "OverstackFixer", "overstackfixer:*", "Security");
      init(presets, "StackFix", "stackfix:*", "Security");
      init(presets, "StackFixer", "stackfixer:*", "Security");
      init(presets, "ItemGuard", "itemguard:*", "Security");
      init(presets, "InventoryGuard", "inventoryguard:*", "Security");
      init(presets, "InventorySecurity", "inventorysecurity:*", "Security");
      init(presets, "OpenInvGuard", "openinvguard:*", "Security");
      init(presets, "CommandWhitelist", "commandwhitelist:*", "Security");
      init(presets, "CommandBlocker", "commandblocker:*", "Security");
      init(presets, "CommandGuard", "commandguard:*", "Security");
      init(presets, "ChatGuard", "chatguard:*", "Security");
      init(presets, "AntiSpam", "antispam:*", "Security");
      init(presets, "AntiSpamPlus", "antispamplus:*", "Security");
      init(presets, "AntiBot", "antibot:*", "Security");
      init(presets, "BotSentry", "botsentry:*", "Security");
      init(presets, "BotGuard", "botguard:*", "Security");
      init(presets, "BotFilter", "botfilter:*", "Security");
      init(presets, "AntiBotDeluxe", "antibotdeluxe:*", "Security");
      init(presets, "AntiVPN", "antivpn:*", "Security");
      init(presets, "VPNGuard", "vpnguard:*", "Security");
      init(presets, "VPNBlocker", "vpnblocker:*", "Security");
      init(presets, "ProxyCheck", "proxycheck:*", "Security");
      init(presets, "ProxyGuard", "proxyguard:*", "Security");
      init(presets, "FastAntiBot", "fastantibot:*", "Security");
      init(presets, "EpicGuard", "epicguard:*", "Security");
      init(presets, "FlameCord", "flamecord:*", "Security");
      init(presets, "BotFilterPlus", "botfilterplus:*", "Security");
      init(presets, "AccountGuard", "accountguard:*", "Security");
      init(presets, "LoginGuard", "loginguard:*", "Security");
      init(presets, "NameSecurity", "namesecurity:*", "Security");
      init(presets, "UUIDSpoofFix", "uuidspooffix:*", "Security");
      init(presets, "UUIDGuard", "uuidguard:*", "Security");
      init(presets, "NettyGuard", "nettyguard:*", "Security");
      init(presets, "PayloadGuard", "payloadguard:*", "Security");
      init(presets, "PayloadFilter", "payloadfilter:*", "Security");
      init(presets, "PayloadSecurity", "payloadsecurity:*", "Security");
      init(presets, "ChannelGuard", "channelguard:*", "Security");
      init(presets, "PluginMessageGuard", "pluginmessageguard:*", "Security");
      init(presets, "PluginMessageFilter", "pluginmessagefilter:*", "Security");
      init(presets, "AxCrates", "axcrates:*", "Crates");
      init(presets, "AxCrate", "axcrate:*", "Crates");
      init(presets, "CratesX", "cratesx:*", "Crates");
      init(presets, "CrateX", "cratex:*", "Crates");
      init(presets, "CratesPro", "cratespro:*", "Crates");
      init(presets, "CratePro", "cratepro:*", "Crates");
      init(presets, "CrateCore", "cratecore:*", "Crates");
      init(presets, "CratesCore", "cratescore:*", "Crates");
      init(presets, "CrateSystem", "cratesystem:*", "Crates");
      init(presets, "CratesSystem", "cratessystem:*", "Crates");
      init(presets, "CrateGUI", "crategui:*", "Crates");
      init(presets, "CratesGUI", "cratesgui:*", "Crates");
      init(presets, "CrateMenu", "cratemenu:*", "Crates");
      init(presets, "CratesMenu", "cratesmenu:*", "Crates");
      init(presets, "CrateMenus", "cratemenus:*", "Crates");
      init(presets, "CratesAPI", "cratesapi:*", "Crates");
      init(presets, "CrateAPI", "crateapi:*", "Crates");
      init(presets, "CratePreviewer", "cratepreviewer:*", "Crates");
      init(presets, "CratesPreview", "cratespreview:*", "Crates");
      init(presets, "CratesPreviewer", "cratespreviewer:*", "Crates");
      init(presets, "CrateAnimation", "crateanimation:*", "Crates");
      init(presets, "CrateAnimations", "crateanimations:*", "Crates");
      init(presets, "CrateRoll", "crateroll:*", "Crates");
      init(presets, "CrateRolls", "craterolls:*", "Crates");
      init(presets, "CrateWheel", "cratewheel:*", "Crates");
      init(presets, "CrateWheels", "cratewheels:*", "Crates");
      init(presets, "CrateSpin", "cratespin:*", "Crates");
      init(presets, "CrateSpins", "cratespins:*", "Crates");
      init(presets, "CrateOpenerPro", "crateopenerpro:*", "Crates");
      init(presets, "CrateOpeners", "crateopeners:*", "Crates");
      init(presets, "CrateKey", "cratekey:*", "Crates");
      init(presets, "CrateKeysPro", "cratekeyspro:*", "Crates");
      init(presets, "KeyCrates", "keycrates:*", "Crates");
      init(presets, "KeyCrate", "keycrate:*", "Crates");
      init(presets, "KeySystem", "keysystem:*", "Crates");
      init(presets, "KeysSystem", "keyssystem:*", "Crates");
      init(presets, "SimpleCrates", "simplecrates:*", "Crates");
      init(presets, "SimpleCrate", "simplecrate:*", "Crates");
      init(presets, "EasyCrates", "easycrates:*", "Crates");
      init(presets, "EasyCrate", "easycrate:*", "Crates");
      init(presets, "BetterCrates", "bettercrates:*", "Crates");
      init(presets, "BetterCrate", "bettercrate:*", "Crates");
      init(presets, "PremiumCrates", "premiumcrates:*", "Crates");
      init(presets, "PremiumCrate", "premiumcrate:*", "Crates");
      init(presets, "UltimateCrates", "ultimatecrates:*", "Crates");
      init(presets, "UltimateCrate", "ultimatecrate:*", "Crates");
      init(presets, "SupremeCrates", "supremecrates:*", "Crates");
      init(presets, "SupremeCrate", "supremecrate:*", "Crates");
      init(presets, "DivineCrates", "divinecrates:*", "Crates");
      init(presets, "DivineCrate", "divinecrate:*", "Crates");
      init(presets, "LegendaryCrates", "legendarycrates:*", "Crates");
      init(presets, "LegendaryCrate", "legendarycrate:*", "Crates");
      init(presets, "RareCrates", "rarecrates:*", "Crates");
      init(presets, "RareCrate", "rarecrate:*", "Crates");
      init(presets, "EpicCrate", "epiccrate:*", "Crates");
      init(presets, "UltraCrate", "ultracrate:*", "Crates");
      init(presets, "RoyalCrate", "royalcrate:*", "Crates");
      init(presets, "GoldenCrate", "goldencrate:*", "Crates");
      init(presets, "CrazyCrate", "crazycrate:*", "Crates");
      init(presets, "AdvancedCrate", "advancedcrate:*", "Crates");
      init(presets, "ExcellentCrate", "excellentcrate:*", "Crates");
      init(presets, "SpecializedCrate", "specializedcrate:*", "Crates");
      init(presets, "PhoenixCrate", "phoenixcrate:*", "Crates");
      init(presets, "AquaticCrate", "aquaticcrate:*", "Crates");
      init(presets, "AquaCrates", "aquacrates:*", "Crates");
      init(presets, "AquaCrate", "aquacrate:*", "Crates");
      init(presets, "PulseCrate", "pulsecrate:*", "Crates");
      init(presets, "MysticCrates", "mysticcrates:*", "Crates");
      init(presets, "MysticCrate", "mysticcrate:*", "Crates");
      init(presets, "MythicCrates", "mythiccrates:*", "Crates");
      init(presets, "MythicCrate", "mythiccrate:*", "Crates");
      init(presets, "OPCrates", "opcrates:*", "Crates");
      init(presets, "OPCrate", "opcrate:*", "Crates");
      init(presets, "VoteKey", "votekey:*", "Crates");
      init(presets, "VoteKeys", "votekeys:*", "Crates");
      init(presets, "Case", "case:*", "Crates");
      init(presets, "CasesPro", "casespro:*", "Crates");
      init(presets, "CaseSystem", "casesystem:*", "Crates");
      init(presets, "CaseOpeningPro", "caseopeningpro:*", "Crates");
      init(presets, "OpenCase", "opencase:*", "Crates");
      init(presets, "OpenCases", "opencases:*", "Crates");
      init(presets, "CaseKeys", "casekeys:*", "Crates");
      init(presets, "CaseKey", "casekey:*", "Crates");
      init(presets, "Boxes", "boxes:*", "Crates");
      init(presets, "Box", "box:*", "Crates");
      init(presets, "RewardBox", "rewardbox:*", "Crates");
      init(presets, "RewardBoxes", "rewardboxes:*", "Crates");
      init(presets, "PrizeCrates", "prizecrates:*", "Crates");
      init(presets, "PrizeCrate", "prizecrate:*", "Crates");
      init(presets, "PrizeBox", "prizebox:*", "Crates");
      init(presets, "PrizeBoxes", "prizeboxes:*", "Crates");
      init(presets, "LootCratesPro", "lootcratespro:*", "Crates");
      init(presets, "LootBoxPro", "lootboxpro:*", "Crates");
      init(presets, "LootBoxesPro", "lootboxespro:*", "Crates");
      init(presets, "RewardCase", "rewardcase:*", "Crates");
      init(presets, "RewardCases", "rewardcases:*", "Crates");
      init(presets, "CasinoCore", "casinocore:*", "Gambling");
      init(presets, "CasinoSystem", "casinosystem:*", "Gambling");
      init(presets, "CasinoPlus", "casinoplus:*", "Gambling");
      init(presets, "CasinoPro", "casinopro:*", "Gambling");
      init(presets, "CasinoGUI", "casinogui:*", "Gambling");
      init(presets, "CasinoMenu", "casinomenu:*", "Gambling");
      init(presets, "Casinos", "casinos:*", "Gambling");
      init(presets, "Gambling", "gambling:*", "Gambling");
      init(presets, "GamblingCore", "gamblingcore:*", "Gambling");
      init(presets, "GamblingSystem", "gamblingsystem:*", "Gambling");
      init(presets, "GamblingPlus", "gamblingplus:*", "Gambling");
      init(presets, "GamblingPro", "gamblingpro:*", "Gambling");
      init(presets, "Bet", "bet:*", "Gambling");
      init(presets, "BetsPlus", "betsplus:*", "Gambling");
      init(presets, "BettingPlus", "bettingplus:*", "Gambling");
      init(presets, "BettingSystem", "bettingsystem:*", "Gambling");
      init(presets, "BettingCore", "bettingcore:*", "Gambling");
      init(presets, "Wager", "wager:*", "Gambling");
      init(presets, "Wagers", "wagers:*", "Gambling");
      init(presets, "Wagering", "wagering:*", "Gambling");
      init(presets, "Stake", "stake:*", "Gambling");
      init(presets, "Stakes", "stakes:*", "Gambling");
      init(presets, "CoinFlips", "coinflips:*", "Gambling");
      init(presets, "CoinFlipPlus", "coinflipplus:*", "Gambling");
      init(presets, "CoinFlipPro", "coinflippro:*", "Gambling");
      init(presets, "CoinFlipSystem", "coinflipsystem:*", "Gambling");
      init(presets, "CoinFlipCore", "coinflipcore:*", "Gambling");
      init(presets, "CoinTossPlus", "cointossplus:*", "Gambling");
      init(presets, "CoinTossPro", "cointosspro:*", "Gambling");
      init(presets, "HeadsTails", "headstails:*", "Gambling");
      init(presets, "HeadsOrTailsPlus", "heads_or_tails_plus:*", "Gambling");
      init(presets, "Jackpots", "jackpots:*", "Gambling");
      init(presets, "JackpotPlus", "jackpotplus:*", "Gambling");
      init(presets, "JackpotPro", "jackpotpro:*", "Gambling");
      init(presets, "JackpotSystem", "jackpotsystem:*", "Gambling");
      init(presets, "LotteryCore", "lotterycore:*", "Gambling");
      init(presets, "LotterySystem", "lotterysystem:*", "Gambling");
      init(presets, "LotteryPro", "lotterypro:*", "Gambling");
      init(presets, "Lotto", "lotto:*", "Gambling");
      init(presets, "LottoPlus", "lottoplus:*", "Gambling");
      init(presets, "LottoSystem", "lottosystem:*", "Gambling");
      init(presets, "Raffle", "raffle:*", "Gambling");
      init(presets, "Raffles", "raffles:*", "Gambling");
      init(presets, "RafflePlus", "raffleplus:*", "Gambling");
      init(presets, "RouletteCore", "roulettecore:*", "Gambling");
      init(presets, "RouletteSystem", "roulettesystem:*", "Gambling");
      init(presets, "RoulettePro", "roulettepro:*", "Gambling");
      init(presets, "RouletteGUI", "roulettegui:*", "Gambling");
      init(presets, "RussianRoulette", "russianroulette:*", "Gambling");
      init(presets, "Slot", "slot:*", "Gambling");
      init(presets, "SlotPlus", "slotplus:*", "Gambling");
      init(presets, "SlotPro", "slotpro:*", "Gambling");
      init(presets, "SlotSystem", "slotsystem:*", "Gambling");
      init(presets, "SlotsCore", "slotscore:*", "Gambling");
      init(presets, "SlotsPlus", "slotsplus:*", "Gambling");
      init(presets, "SlotsPro", "slotspro:*", "Gambling");
      init(presets, "SlotsSystem", "slotssystem:*", "Gambling");
      init(presets, "SlotMachinesPlus", "slotmachinesplus:*", "Gambling");
      init(presets, "SlotMachinesPro", "slotmachinespro:*", "Gambling");
      init(presets, "OneArmedBandit", "onearmedbandit:*", "Gambling");
      init(presets, "LuckySlots", "luckyslots:*", "Gambling");
      init(presets, "LuckySlot", "luckyslot:*", "Gambling");
      init(presets, "WheelPlus", "wheelplus:*", "Gambling");
      init(presets, "WheelPro", "wheelpro:*", "Gambling");
      init(presets, "WheelSystem", "wheelsystem:*", "Gambling");
      init(presets, "LuckyWheelPlus", "luckywheelplus:*", "Gambling");
      init(presets, "LuckyWheelPro", "luckywheelpro:*", "Gambling");
      init(presets, "WheelSpin", "wheelspin:*", "Gambling");
      init(presets, "WheelSpins", "wheelspins:*", "Gambling");
      init(presets, "SpinWheel", "spinwheel:*", "Gambling");
      init(presets, "SpinToWin", "spintowin:*", "Gambling");
      init(presets, "DicePlus", "diceplus:*", "Gambling");
      init(presets, "DicePro", "dicepro:*", "Gambling");
      init(presets, "DiceSystem", "dicesystem:*", "Gambling");
      init(presets, "DiceRoll", "diceroll:*", "Gambling");
      init(presets, "DiceRolls", "dicerolls:*", "Gambling");
      init(presets, "RollDice", "rolldice:*", "Gambling");
      init(presets, "BlackjackPlus", "blackjackplus:*", "Gambling");
      init(presets, "BlackjackPro", "blackjackpro:*", "Gambling");
      init(presets, "BlackjackSystem", "blackjacksystem:*", "Gambling");
      init(presets, "PokerPlus", "pokerplus:*", "Gambling");
      init(presets, "PokerPro", "pokerpro:*", "Gambling");
      init(presets, "PokerSystem", "pokersystem:*", "Gambling");
      init(presets, "TexasHoldem", "texasholdem:*", "Gambling");
      init(presets, "Holdem", "holdem:*", "Gambling");
      init(presets, "BaccaratPlus", "baccaratplus:*", "Gambling");
      init(presets, "KenoPlus", "kenoplus:*", "Gambling");
      init(presets, "PlinkoPlus", "plinkoplus:*", "Gambling");
      init(presets, "PlinkoPro", "plinkopro:*", "Gambling");
      init(presets, "CrashGame", "crashgame:*", "Gambling");
      init(presets, "CrashPlus", "crashplus:*", "Gambling");
      init(presets, "CrashPro", "crashpro:*", "Gambling");
      init(presets, "MinesGame", "minesgame:*", "Gambling");
      init(presets, "MinesPlus", "minesplus:*", "Gambling");
      init(presets, "MinesPro", "minespro:*", "Gambling");
      init(presets, "RPSPlus", "rpsplus:*", "Gambling");
      init(presets, "RockPaperScissors", "rockpaperscissors:*", "Gambling");
      init(presets, "CaseBattle", "casebattle:*", "Gambling");
      init(presets, "CaseBattlePlus", "casebattleplus:*", "Gambling");
      init(presets, "CaseBattlePro", "casebattlepro:*", "Gambling");
      init(presets, "LootBattle", "lootbattle:*", "Gambling");
      init(presets, "LootBattles", "lootbattles:*", "Gambling");
      init(presets, "Ax Family", "ax*", "Frameworks");
      init(presets, "Ax Namespace", "ax:*", "Frameworks");
      init(presets, "Artillex", "artillex:*", "Frameworks");
      init(presets, "ArtillexStudios", "artillexstudios:*", "Frameworks");
      init(presets, "Artillex Studios", "artillex_studios:*", "Frameworks");
      init(presets, "AxVaults", "axvaults:*", "Storage");
      init(presets, "AxVault", "axvault:*", "Storage");
      init(presets, "AxBackpacks", "axbackpacks:*", "Storage");
      init(presets, "AxBackpack", "axbackpack:*", "Storage");
      init(presets, "AxStorage", "axstorage:*", "Storage");
      init(presets, "AxStorages", "axstorages:*", "Storage");
      init(presets, "AxShulkers", "axshulkers:*", "Storage");
      init(presets, "AxShulker", "axshulker:*", "Storage");
      init(presets, "AxEnderChest", "axenderchest:*", "Storage");
      init(presets, "AxEnderChests", "axenderchests:*", "Storage");
      init(presets, "AxTrade", "axtrade:*", "Shops");
      init(presets, "AxTrades", "axtrades:*", "Shops");
      init(presets, "AxMarket", "axmarket:*", "Shops");
      init(presets, "AxMarkets", "axmarkets:*", "Shops");
      init(presets, "AxEconomy", "axeconomy:*", "Economy");
      init(presets, "AxCoins", "axcoins:*", "Economy");
      init(presets, "AxGems", "axgems:*", "Economy");
      init(presets, "AxTokens", "axtokens:*", "Economy");
      init(presets, "AxRewards", "axrewards:*", "Rewards");
      init(presets, "AxReward", "axreward:*", "Rewards");
      init(presets, "AxDailyRewards", "axdailyrewards:*", "Rewards");
      init(presets, "AxKits", "axkits:*", "Rewards");
      init(presets, "AxKit", "axkit:*", "Rewards");
      init(presets, "AxMines", "axmines:*", "Prison");
      init(presets, "AxMine", "axmine:*", "Prison");
      init(presets, "AxPrison", "axprison:*", "Prison");
      init(presets, "AxRankup", "axrankup:*", "Prison");
      init(presets, "AxRanks", "axranks:*", "Prison");
      init(presets, "AxSellwands", "axsellwands:*", "Prison");
      init(presets, "AxSellwand", "axsellwand:*", "Prison");
      init(presets, "AxEnvoys", "axenvoys:*", "Crates");
      init(presets, "AxEnvoy", "axenvoy:*", "Crates");
      init(presets, "AxBoosters", "axboosters:*", "Prison");
      init(presets, "AxBooster", "axbooster:*", "Prison");
      init(presets, "AxGens", "axgens:*", "SMP");
      init(presets, "AxGen", "axgen:*", "SMP");
      init(presets, "AxGenerators", "axgenerators:*", "SMP");
      init(presets, "AxGenerator", "axgenerator:*", "SMP");
      init(presets, "AxCombat", "axcombat:*", "SMP");
      init(presets, "AxDuels", "axduels:*", "Minigames");
      init(presets, "AxDuelsPlus", "axduelsplus:*", "Minigames");
      init(presets, "AxParkour", "axparkour:*", "Minigames");
      init(presets, "AxQuests", "axquests:*", "Quests");
      init(presets, "AxQuest", "axquest:*", "Quests");
      init(presets, "AxMenus", "axmenus:*", "Menus");
      init(presets, "AxMenu", "axmenu:*", "Menus");
      init(presets, "AxChat", "axchat:*", "Chat");
      init(presets, "AxTags", "axtags:*", "Chat");
      init(presets, "AxScoreboard", "axscoreboard:*", "UI");
      init(presets, "AxTab", "axtab:*", "UI");
      init(presets, "AxHolograms", "axholograms:*", "Holograms");
      init(presets, "AxHologram", "axhologram:*", "Holograms");
      init(presets, "AxNPCs", "axnpcs:*", "NPCs");
      init(presets, "AxNPC", "axnpc:*", "NPCs");
      init(presets, "AxSecurity", "axsecurity:*", "Security");
      init(presets, "AxGuard", "axguard:*", "Security");
      init(presets, "AxAntiCrash", "axanticrash:*", "Security");
      init(presets, "AxAntiBot", "axantibot:*", "Security");
      init(presets, "AxAntiVPN", "axantivpn:*", "Security");
      init(presets, "Az Family", "az*", "Frameworks");
      init(presets, "Az Namespace", "az:*", "Frameworks");
      init(presets, "AzAuctionHouse", "azauctionhouse:*", "Auctions");
      init(presets, "AzAuctions", "azauctions:*", "Auctions");
      init(presets, "AzAuction", "azauction:*", "Auctions");
      init(presets, "AzShop", "azshop:*", "Shops");
      init(presets, "AzShops", "azshops:*", "Shops");
      init(presets, "AzMarket", "azmarket:*", "Shops");
      init(presets, "AzMarkets", "azmarkets:*", "Shops");
      init(presets, "AzVault", "azvault:*", "Storage");
      init(presets, "AzVaults", "azvaults:*", "Storage");
      init(presets, "AzStorage", "azstorage:*", "Storage");
      init(presets, "AzStorages", "azstorages:*", "Storage");
      init(presets, "AzBackpack", "azbackpack:*", "Storage");
      init(presets, "AzBackpacks", "azbackpacks:*", "Storage");
      init(presets, "AzMines", "azmines:*", "Prison");
      init(presets, "AzMine", "azmine:*", "Prison");
      init(presets, "AzPrison", "azprison:*", "Prison");
      init(presets, "AzCrates", "azcrates:*", "Crates");
      init(presets, "AzCrate", "azcrate:*", "Crates");
      init(presets, "AzCasino", "azcasino:*", "Gambling");
      init(presets, "AzGamble", "azgamble:*", "Gambling");
      init(presets, "AzRewards", "azrewards:*", "Rewards");
      init(presets, "AzReward", "azreward:*", "Rewards");
      init(presets, "AzKits", "azkits:*", "Rewards");
      init(presets, "AzKit", "azkit:*", "Rewards");
      init(presets, "AzLink", "azlink:*", "Proxy");
      init(presets, "AzAuth", "azauth:*", "Proxy");
      init(presets, "AzLogin", "azlogin:*", "Proxy");
      init(presets, "AzCore", "azcore:*", "Frameworks");
      init(presets, "AzLib", "azlib:*", "Frameworks");
      init(presets, "AzAPI", "azapi:*", "Frameworks");
      init(presets, "AzMenu", "azmenu:*", "Menus");
      init(presets, "AzMenus", "azmenus:*", "Menus");
      init(presets, "AzQuests", "azquests:*", "Quests");
      init(presets, "AzQuest", "azquest:*", "Quests");
      init(presets, "AzChat", "azchat:*", "Chat");
      init(presets, "AzTab", "aztab:*", "UI");
      init(presets, "AzHolograms", "azholograms:*", "Holograms");
      init(presets, "AzNPCs", "aznpcs:*", "NPCs");
      init(presets, "VirtualStorages", "virtualstorages:*", "Storage");
      init(presets, "VirtualStorage", "virtualstorage:*", "Storage");
      init(presets, "Virtual Storage", "virtual_storage:*", "Storage");
      init(presets, "VirtualStoragePlugin", "virtualstorageplugin:*", "Storage");
      init(presets, "VirtualBackpacks", "virtualbackpacks:*", "Storage");
      init(presets, "VirtualBackpack", "virtualbackpack:*", "Storage");
      init(presets, "Virtual Backpack", "virtual_backpack:*", "Storage");
      init(presets, "VirtualBags", "virtualbags:*", "Storage");
      init(presets, "VirtualBag", "virtualbag:*", "Storage");
      init(presets, "VirtualChest", "virtualchest:*", "Storage");
      init(presets, "VirtualChests", "virtualchests:*", "Storage");
      init(presets, "VirtualVault", "virtualvault:*", "Storage");
      init(presets, "VirtualVaults", "virtualvaults:*", "Storage");
      init(presets, "VirtualInventory", "virtualinventory:*", "Storage");
      init(presets, "VirtualInventories", "virtualinventories:*", "Storage");
      init(presets, "VirtualPack", "virtualpack:*", "Storage");
      init(presets, "VirtualPacks", "virtualpacks:*", "Storage");
      init(presets, "PersonalVaults", "personalvaults:*", "Storage");
      init(presets, "PersonalVault", "personalvault:*", "Storage");
      init(presets, "PersonalStorage", "personalstorage:*", "Storage");
      init(presets, "PersonalStorages", "personalstorages:*", "Storage");
      init(presets, "PlayerStorage", "playerstorage:*", "Storage");
      init(presets, "PlayerStorages", "playerstorages:*", "Storage");
      init(presets, "CloudStorage", "cloudstorage:*", "Storage");
      init(presets, "CloudStorages", "cloudstorages:*", "Storage");
      init(presets, "CloudVaults", "cloudvaults:*", "Storage");
      init(presets, "CloudVault", "cloudvault:*", "Storage");
      init(presets, "MineStorage", "minestorage:*", "Storage");
      init(presets, "MineStorages", "minestorages:*", "Storage");
      init(presets, "StoragePlus", "storageplus:*", "Storage");
      init(presets, "StoragePro", "storagepro:*", "Storage");
      init(presets, "StorageSystem", "storagesystem:*", "Storage");
      init(presets, "StorageCore", "storagecore:*", "Storage");
      init(presets, "EasyMines", "easymines:*", "Prison");
      init(presets, "EasyMine", "easymine:*", "Prison");
      init(presets, "EasyPrisonMines", "easyprisonmines:*", "Prison");
      init(presets, "EasyPrisonMine", "easyprisonmine:*", "Prison");
      init(presets, "EasyMineReset", "easyminereset:*", "Prison");
      init(presets, "EasyMineResets", "easymineresets:*", "Prison");
      init(presets, "CataMines", "catamines:*", "Prison");
      init(presets, "CataMine", "catamine:*", "Prison");
      init(presets, "PrisonMine", "prisonmine:*", "Prison");
      init(presets, "PrisonMinesPlus", "prisonminesplus:*", "Prison");
      init(presets, "PrisonMinePlus", "prisonmineplus:*", "Prison");
      init(presets, "PrisonMinesPro", "prisonminespro:*", "Prison");
      init(presets, "PrisonMinePro", "prisonminepro:*", "Prison");
      init(presets, "MineReset", "minereset:*", "Prison");
      init(presets, "MineResets", "mineresets:*", "Prison");
      init(presets, "MineResetLite", "mineresetlite:*", "Prison");
      init(presets, "MineResetPro", "mineresetpro:*", "Prison");
      init(presets, "AutoMineReset", "autominereset:*", "Prison");
      init(presets, "AutoMineResets", "automineresets:*", "Prison");
      init(presets, "AutoMines", "automines:*", "Prison");
      init(presets, "AutoMine", "automine:*", "Prison");
      init(presets, "MineManager", "minemanager:*", "Prison");
      init(presets, "MinesManager", "minesmanager:*", "Prison");
      init(presets, "MineSystem", "minesystem:*", "Prison");
      init(presets, "MinesSystem", "minessystem:*", "Prison");
      init(presets, "MineCore", "minecore:*", "Prison");
      init(presets, "MinesCore", "minescore:*", "Prison");
      init(presets, "BlockMines", "blockmines:*", "Prison");
      init(presets, "BlockMine", "blockmine:*", "Prison");
      init(presets, "AdvancedMines", "advancedmines:*", "Prison");
      init(presets, "AdvancedMine", "advancedmine:*", "Prison");
      init(presets, "UltimateMines", "ultimatemines:*", "Prison");
      init(presets, "UltimateMine", "ultimatemine:*", "Prison");
      init(presets, "MineWorld", "mineworld:*", "Prison");
      init(presets, "MineWorlds", "mineworlds:*", "Prison");
      init(presets, "MineRegion", "mineregion:*", "Prison");
      init(presets, "MineRegions", "mineregions:*", "Prison");
      init(presets, "Container", "container:*", "Storage");
      init(presets, "Containers", "containers:*", "Storage");
      init(presets, "ContainerGUI", "containergui:*", "Storage");
      init(presets, "ContainerMenu", "containermenu:*", "Storage");
      init(presets, "ContainerPlus", "containerplus:*", "Storage");
      init(presets, "ContainerPro", "containerpro:*", "Storage");
      init(presets, "OpenContainer", "opencontainer:*", "Storage");
      init(presets, "OpenContainers", "opencontainers:*", "Storage");
      init(presets, "PortableStorage", "portablestorage:*", "Storage");
      init(presets, "PortableStorages", "portablestorages:*", "Storage");
      init(presets, "PortableChest", "portablechest:*", "Storage");
      init(presets, "PortableChests", "portablechests:*", "Storage");
      init(presets, "PortableVault", "portablevault:*", "Storage");
      init(presets, "PortableVaults", "portablevaults:*", "Storage");
      init(presets, "PortableBackpack", "portablebackpack:*", "Storage");
      init(presets, "PortableBackpacks", "portablebackpacks:*", "Storage");
      init(presets, "PowerfulBackpacks", "powerfulbackpacks:*", "Storage");
      init(presets, "PowerfulBackpack", "powerfulbackpack:*", "Storage");
      init(presets, "MyBackpack", "mybackpack:*", "Storage");
      init(presets, "MyBackpacks", "mybackpacks:*", "Storage");
      init(presets, "My Backpack", "my_backpack:*", "Storage");
      init(presets, "BackpackPlus", "backpackplus:*", "Storage");
      init(presets, "Backpack Plus", "backpack_plus:*", "Storage");
      init(presets, "BetterBackpacks", "betterbackpacks:*", "Storage");
      init(presets, "BetterBackpack", "betterbackpack:*", "Storage");
      init(presets, "AdvancedBackpack", "advancedbackpack:*", "Storage");
      init(presets, "AdvancedBackpacks NBT", "advancedbackpacksnbt:*", "Storage");
      init(presets, "ExpendableBackpacks", "expendablebackpacks:*", "Storage");
      init(presets, "ExpendableBackpack", "expendablebackpack:*", "Storage");
      init(presets, "Minepack", "minepack:*", "Storage");
      init(presets, "MineBackpacks", "minebackpacks:*", "Storage");
      init(presets, "MineBackpack", "minebackpack:*", "Storage");
      init(presets, "SimpleBackpacks", "simplebackpacks:*", "Storage");
      init(presets, "SimpleBackpack", "simplebackpack:*", "Storage");
      init(presets, "EasyBackpacks", "easybackpacks:*", "Storage");
      init(presets, "EasyBackpack", "easybackpack:*", "Storage");
      init(presets, "UltimateBackpacks", "ultimatebackpacks:*", "Storage");
      init(presets, "UltimateBackpack", "ultimatebackpack:*", "Storage");
      init(presets, "EpicBackpacks", "epicbackpacks:*", "Storage");
      init(presets, "EpicBackpack", "epicbackpack:*", "Storage");
      init(presets, "DeluxeBackpacks", "deluxebackpacks:*", "Storage");
      init(presets, "DeluxeBackpack", "deluxebackpack:*", "Storage");
      init(presets, "PremiumBackpacks", "premiumbackpacks:*", "Storage");
      init(presets, "PremiumBackpack", "premiumbackpack:*", "Storage");
      init(presets, "RoyalBackpacks", "royalbackpacks:*", "Storage");
      init(presets, "RoyalBackpack", "royalbackpack:*", "Storage");
      init(presets, "UpgradeableBackpacks", "upgradeablebackpacks:*", "Storage");
      init(presets, "UpgradeableBackpack", "upgradeablebackpack:*", "Storage");
      init(presets, "SmartBackpacks", "smartbackpacks:*", "Storage");
      init(presets, "SmartBackpack", "smartbackpack:*", "Storage");
      init(presets, "BackpackItem", "backpackitem:*", "Storage");
      init(presets, "BackpackItems", "backpackitems:*", "Storage");
      init(presets, "BackpackGUI", "backpackgui:*", "Storage");
      init(presets, "BackpackMenu", "backpackmenu:*", "Storage");
      init(presets, "BackpackSystem", "backpacksystem:*", "Storage");
      init(presets, "BackpacksSystem", "backpackssystem:*", "Storage");
      init(presets, "BackpackCore", "backpackcore:*", "Storage");
      init(presets, "BackpacksCore", "backpackscore:*", "Storage");
      init(presets, "OpenShulker", "openshulker:*", "Storage");
      init(presets, "OpenShulkers", "openshulkers:*", "Storage");
      init(presets, "OpenShulkerBox", "openshulkerbox:*", "Storage");
      init(presets, "OpenShulkerBoxes", "openshulkerboxes:*", "Storage");
      init(presets, "Open Shulker", "open_shulker:*", "Storage");
      init(presets, "Open Shulker Box", "open_shulker_box:*", "Storage");
      init(presets, "ShulkerBackpacks", "shulkerbackpacks:*", "Storage");
      init(presets, "ShulkerBackpack", "shulkerbackpack:*", "Storage");
      init(presets, "Shulker Backpacks", "shulker_backpacks:*", "Storage");
      init(presets, "Shulker Backpack", "shulker_backpack:*", "Storage");
      init(presets, "ShulkerBoxBackpacks", "shulkerboxbackpacks:*", "Storage");
      init(presets, "ShulkerBoxBackpack", "shulkerboxbackpack:*", "Storage");
      init(presets, "ShulkerBoxPlus", "shulkerboxplus:*", "Storage");
      init(presets, "ShulkerPlus", "shulkerplus:*", "Storage");
      init(presets, "EShulkerBox", "eshulkerbox:*", "Storage");
      init(presets, "EShulker", "eshulker:*", "Storage");
      init(presets, "BetterShulker", "bettershulker:*", "Storage");
      init(presets, "BetterShulkers", "bettershulkers:*", "Storage");
      init(presets, "BetterShulkerBoxes", "bettershulkerboxes:*", "Storage");
      init(presets, "BetterShulkerBox", "bettershulkerbox:*", "Storage");
      init(presets, "ShulkerUtils", "shulkerutils:*", "Storage");
      init(presets, "ShulkerUtility", "shulkerutility:*", "Storage");
      init(presets, "ShulkerUtilities", "shulkerutilities:*", "Storage");
      init(presets, "ShulkerOpener", "shulkeropener:*", "Storage");
      init(presets, "ShulkerOpen", "shulkeropen:*", "Storage");
      init(presets, "ShulkerPreview", "shulkerpreview:*", "Storage");
      init(presets, "ShulkerPreviews", "shulkerpreviews:*", "Storage");
      init(presets, "ShulkerViewer", "shulkerviewer:*", "Storage");
      init(presets, "ShulkerView", "shulkerview:*", "Storage");
      init(presets, "ShulkerPacks", "shulkerpacks:*", "Storage");
      init(presets, "ShulkerPack", "shulkerpack:*", "Storage");
      init(presets, "EnderChestPlus", "enderchestplus:*", "Storage");
      init(presets, "EnderChestVault", "enderchestvault:*", "Storage");
      init(presets, "EnderChestVaults", "enderchestvaults:*", "Storage");
      init(presets, "EnderChest", "enderchest:*", "Storage");
      init(presets, "EnderChests", "enderchests:*", "Storage");
      init(presets, "Ender Chest", "ender_chest:*", "Storage");
      init(presets, "Ender Chests", "ender_chests:*", "Storage");
      init(presets, "EnderChestX", "enderchestx:*", "Storage");
      init(presets, "EnderChestPro", "enderchestpro:*", "Storage");
      init(presets, "EnderChestCore", "enderchestcore:*", "Storage");
      init(presets, "EnderChestSystem", "enderchestsystem:*", "Storage");
      init(presets, "EnderStorage", "enderstorage:*", "Storage");
      init(presets, "EnderStorages", "enderstorages:*", "Storage");
      init(presets, "EnderContainer", "endercontainer:*", "Storage");
      init(presets, "EnderContainers", "endercontainers:*", "Storage");
      init(presets, "EnderBackpack", "enderbackpack:*", "Storage");
      init(presets, "EnderBackpacks", "enderbackpacks:*", "Storage");
      init(presets, "EnderPack", "enderpack:*", "Storage");
      init(presets, "EnderPacks", "enderpacks:*", "Storage");
      init(presets, "BetterEnderChest", "betterenderchest:*", "Storage");
      init(presets, "BetterEnderChests", "betterenderchests:*", "Storage");
      init(presets, "AdvancedEnderChest", "advancedenderchest:*", "Storage");
      init(presets, "AdvancedEnderChests", "advancedenderchests:*", "Storage");
      init(presets, "DeluxeEnderChest", "deluxeenderchest:*", "Storage");
      init(presets, "DeluxeEnderChests", "deluxeenderchests:*", "Storage");
      init(presets, "PrivateEnderChest", "privateenderchest:*", "Storage");
      init(presets, "PrivateEnderChests", "privateenderchests:*", "Storage");
      init(presets, "PVaults", "pvaults:*", "Storage");
      init(presets, "PVault", "pvault:*", "Storage");
      init(presets, "PVs", "pvs:*", "Storage");
      init(presets, "PlayerVault", "playervault:*", "Storage");
      init(presets, "PlayerVaultPlus", "playervaultplus:*", "Storage");
      init(presets, "PlayerVaultsPlus", "playervaultsplus:*", "Storage");
      init(presets, "PlayerVaultPro", "playervaultpro:*", "Storage");
      init(presets, "PlayerVaultsPro", "playervaultspro:*", "Storage");
      init(presets, "PlayerVaultsGUI", "playervaultsgui:*", "Storage");
      init(presets, "PlayerVaultGUI", "playervaultgui:*", "Storage");
      init(presets, "PlayerVaultsMenu", "playervaultsmenu:*", "Storage");
      init(presets, "PlayerVaultMenu", "playervaultmenu:*", "Storage");
      init(presets, "PlayerVaultsCore", "playervaultscore:*", "Storage");
      init(presets, "PlayerVaultCore", "playervaultcore:*", "Storage");
      init(presets, "PlayerVaultsSystem", "playervaultssystem:*", "Storage");
      init(presets, "PlayerVaultSystem", "playervaultsystem:*", "Storage");
      init(presets, "BetterPlayerVaults", "betterplayervaults:*", "Storage");
      init(presets, "BetterPlayerVault", "betterplayervault:*", "Storage");
      init(presets, "AdvancedPlayerVaults", "advancedplayervaults:*", "Storage");
      init(presets, "AdvancedPlayerVault", "advancedplayervault:*", "Storage");
      init(presets, "DeluxePlayerVaults", "deluxeplayervaults:*", "Storage");
      init(presets, "DeluxePlayerVault", "deluxeplayervault:*", "Storage");
      init(presets, "InsaneVaults", "insanevaults:*", "Storage");
      init(presets, "InsaneVault", "insanevault:*", "Storage");
      init(presets, "SuperVaults", "supervaults:*", "Storage");
      init(presets, "SuperVault", "supervault:*", "Storage");
      init(presets, "PrivateVaults", "privatevaults:*", "Storage");
      init(presets, "PrivateVault", "privatevault:*", "Storage");
      init(presets, "VaultPlus", "vaultplus:*", "Storage");
      init(presets, "VaultPro", "vaultpro:*", "Storage");
      init(presets, "VaultSystem", "vaultsystem:*", "Storage");
      init(presets, "VaultCore", "vaultcore:*", "Storage");
      init(presets, "VaultGUI", "vaultgui:*", "Storage");
      init(presets, "VaultMenu", "vaultmenu:*", "Storage");
      init(presets, "StorageGUI", "storagegui:*", "Storage");
      init(presets, "StorageMenu", "storagemenu:*", "Storage");
      init(presets, "StorageVaults", "storagevaults:*", "Storage");
      init(presets, "StorageVault", "storagevault:*", "Storage");
      init(presets, "TopMinions", "topminions:*", "Minions");
      init(presets, "TopMinion", "topminion:*", "Minions");
      init(presets, "JetsMinions", "jetsminions:*", "Minions");
      init(presets, "JetsMinion", "jetsminion:*", "Minions");
      init(presets, "JetMinions", "jetminions:*", "Minions");
      init(presets, "JetMinion", "jetminion:*", "Minions");
      init(presets, "MiniMinions", "miniminions:*", "Minions");
      init(presets, "MiniMinion", "miniminion:*", "Minions");
      init(presets, "Minions", "minions:*", "Minions");
      init(presets, "Minion", "minion:*", "Minions");
      init(presets, "MinionPlugin", "minionplugin:*", "Minions");
      init(presets, "MinionsPlugin", "minionsplugin:*", "Minions");
      init(presets, "MinionCore", "minioncore:*", "Minions");
      init(presets, "MinionsCore", "minionscore:*", "Minions");
      init(presets, "MinionSystem", "minionsystem:*", "Minions");
      init(presets, "MinionsSystem", "minionssystem:*", "Minions");
      init(presets, "MinionPlus", "minionplus:*", "Minions");
      init(presets, "MinionsPlus", "minionsplus:*", "Minions");
      init(presets, "MinionPro", "minionpro:*", "Minions");
      init(presets, "MinionsPro", "minionspro:*", "Minions");
      init(presets, "MinionGUI", "miniongui:*", "Minions");
      init(presets, "MinionsGUI", "minionsgui:*", "Minions");
      init(presets, "MinionMenu", "minionmenu:*", "Minions");
      init(presets, "MinionsMenu", "minionsmenu:*", "Minions");
      init(presets, "MinionUpgrades", "minionupgrades:*", "Minions");
      init(presets, "MinionUpgrade", "minionupgrade:*", "Minions");
      init(presets, "MinionStorage", "minionstorage:*", "Minions");
      init(presets, "MinionsStorage", "minionsstorage:*", "Minions");
      init(presets, "MinionChest", "minionchest:*", "Minions");
      init(presets, "MinionChests", "minionchests:*", "Minions");
      init(presets, "MinionShop", "minionshop:*", "Minions");
      init(presets, "MinionsShop", "minionsshop:*", "Minions");
      init(presets, "MinionManager", "minionmanager:*", "Minions");
      init(presets, "MinionsManager", "minionsmanager:*", "Minions");
      init(presets, "SuperiorMinions", "superiorminions:*", "Minions");
      init(presets, "SuperiorMinion", "superiorminion:*", "Minions");
      init(presets, "EpicMinions", "epicminions:*", "Minions");
      init(presets, "EpicMinion", "epicminion:*", "Minions");
      init(presets, "AdvancedMinions", "advancedminions:*", "Minions");
      init(presets, "AdvancedMinion", "advancedminion:*", "Minions");
      init(presets, "UltimateMinions", "ultimateminions:*", "Minions");
      init(presets, "UltimateMinion", "ultimateminion:*", "Minions");
      init(presets, "DeluxeMinions", "deluxeminions:*", "Minions");
      init(presets, "DeluxeMinion", "deluxeminion:*", "Minions");
      init(presets, "BetterMinions", "betterminions:*", "Minions");
      init(presets, "BetterMinion", "betterminion:*", "Minions");
      init(presets, "SimpleMinions", "simpleminions:*", "Minions");
      init(presets, "SimpleMinion", "simpleminion:*", "Minions");
      init(presets, "SkyblockMinions", "skyblockminions:*", "Minions");
      init(presets, "SkyblockMinion", "skyblockminion:*", "Minions");
      init(presets, "HypixelMinions", "hypixelminions:*", "Minions");
      init(presets, "HypixelMinion", "hypixelminion:*", "Minions");
      init(presets, "Robots", "robots:*", "Minions");
      init(presets, "Robot", "robot:*", "Minions");
      init(presets, "Worker", "worker:*", "Minions");
      init(presets, "Workers", "workers:*", "Minions");
      init(presets, "Helpers", "helpers:*", "Minions");
      init(presets, "Helper", "helper:*", "Minions");
      init(presets, "AutoFarmers", "autofarmers:*", "Minions");
      init(presets, "AutoFarmer", "autofarmer:*", "Minions");
      init(presets, "AutoMiners", "autominers:*", "Minions");
      init(presets, "AutoMiner", "autominer:*", "Minions");
      init(presets, "MinerMinions", "minerminions:*", "Minions");
      init(presets, "MinerMinion", "minerminion:*", "Minions");
      init(presets, "FarmerMinions", "farmerminions:*", "Minions");
      init(presets, "FarmerMinion", "farmerminion:*", "Minions");
      init(presets, "Vaults Fabricate", "vaultsfabricate:*", "Storage");
      init(presets, "Vault Fabricate", "vaultfabricate:*", "Storage");
      init(presets, "VaultsFabricated", "vaultsfabricated:*", "Storage");
      init(presets, "FabricatedVaults", "fabricatedvaults:*", "Storage");
      init(presets, "FabricatedVault", "fabricatedvault:*", "Storage");
      init(presets, "FabricateVaults", "fabricatevaults:*", "Storage");
      init(presets, "FabricateVault", "fabricatevault:*", "Storage");
      init(presets, "BsruEnderChest", "bsruenderchest:*", "Storage");
      init(presets, "BSRUEnderChest", "bsru_enderchest:*", "Storage");
      init(presets, "BSRU Ender Chest", "bsru_ender_chest:*", "Storage");
      init(presets, "TH Backpacks", "th_backpacks:*", "Storage");
      init(presets, "TH Backpack", "th_backpack:*", "Storage");
      init(presets, "THBackpacks", "thbackpacks:*", "Storage");
      init(presets, "THBackpack", "thbackpack:*", "Storage");
      init(presets, "Kix Simple Backpacks", "kixsimplebackpacks:*", "Storage");
      init(presets, "Kix Simple Backpack", "kixsimplebackpack:*", "Storage");
      init(presets, "Kix's Simple Backpacks", "kixssimplebackpacks:*", "Storage");
      init(presets, "Kix Backpacks", "kixbackpacks:*", "Storage");
      init(presets, "Kix Backpack", "kixbackpack:*", "Storage");
      init(presets, "Kix", "kix:*", "Storage");
      init(presets, "BundleBack", "bundleback:*", "Storage");
      init(presets, "BundleBackpack", "bundlebackpack:*", "Storage");
      init(presets, "BundleBackpacks", "bundlebackpacks:*", "Storage");
      init(presets, "BundleBag", "bundlebag:*", "Storage");
      init(presets, "BundleBags", "bundlebags:*", "Storage");
      init(presets, "BundleStorage", "bundlestorage:*", "Storage");
      init(presets, "BundleStorages", "bundlestorages:*", "Storage");
      init(presets, "BundleChest", "bundlechest:*", "Storage");
      init(presets, "BundleChests", "bundlechests:*", "Storage");
      init(presets, "BundleVault", "bundlevault:*", "Storage");
      init(presets, "BundleVaults", "bundlevaults:*", "Storage");
      init(presets, "EasyShulkers", "easyshulkers:*", "Storage");
      init(presets, "EasyShulker", "easyshulker:*", "Storage");
      init(presets, "EasyShulkerBox", "easyshulkerbox:*", "Storage");
      init(presets, "EasyShulkerBoxes", "easyshulkerboxes:*", "Storage");
      init(presets, "Xiaoklunar Shulker", "xiaoklunarshulker:*", "Storage");
      init(presets, "Xiaoklunar Shulkers", "xiaoklunarshulkers:*", "Storage");
      init(presets, "XiaoklunarShulker", "xiaoklunar_shulker:*", "Storage");
      init(presets, "XiaoklunarShulkers", "xiaoklunar_shulkers:*", "Storage");
      init(presets, "Xiaoklunar", "xiaoklunar:*", "Storage");
      init(presets, "Bigger Chest", "biggerchest:*", "Storage");
      init(presets, "Bigger Chests", "biggerchests:*", "Storage");
      init(presets, "BiggerChest", "bigger_chest:*", "Storage");
      init(presets, "BiggerChests", "bigger_chests:*", "Storage");
      init(presets, "LargeChest", "largechest:*", "Storage");
      init(presets, "LargeChests", "largechests:*", "Storage");
      init(presets, "HugeChest", "hugechest:*", "Storage");
      init(presets, "HugeChests", "hugechests:*", "Storage");
      init(presets, "ExpandedChest", "expandedchest:*", "Storage");
      init(presets, "ExpandedChests", "expandedchests:*", "Storage");
      init(presets, "ExtendedChest", "extendedchest:*", "Storage");
      init(presets, "ExtendedChests", "extendedchests:*", "Storage");
      init(presets, "DeathChestPro", "deathchestpro:*", "Storage");
      init(presets, "DeathChestPlus", "deathchestplus:*", "Storage");
      init(presets, "DeathChestX", "deathchestx:*", "Storage");
      init(presets, "DeathChestReloaded", "deathchestreloaded:*", "Storage");
      init(presets, "DeathChestSystem", "deathchestsystem:*", "Storage");
      init(presets, "DeathChestsSystem", "deathchestssystem:*", "Storage");
      init(presets, "GraveChest", "gravechest:*", "Storage");
      init(presets, "GraveChests", "gravechests:*", "Storage");
      init(presets, "CorpseChest", "corpsechest:*", "Storage");
      init(presets, "CorpseChests", "corpsechests:*", "Storage");
      init(presets, "RoseStacker GUI", "rosestackergui:*", "Storage");
      init(presets, "RoseStacker Menu", "rosestackermenu:*", "Storage");
      init(presets, "RoseStacker Stacker", "rosestackerstacker:*", "Storage");
      init(presets, "EnderEX", "enderex:*", "Storage");
      init(presets, "EnderEx", "ender_ex:*", "Storage");
      init(presets, "EnderEX Storage", "enderexstorage:*", "Storage");
      init(presets, "EnderEX Chest", "enderexchest:*", "Storage");
      init(presets, "EnderEX Vault", "enderexvault:*", "Storage");
      init(presets, "EnderExpansion", "enderexpansion:*", "Storage");
      init(presets, "EnderExtended", "enderextended:*", "Storage");
      init(presets, "EnderExtender", "enderextender:*", "Storage");
      init(presets, "EnderPlus", "enderplus:*", "Storage");
      init(presets, "EnderPro", "enderpro:*", "Storage");
      init(presets, "GUIContainers", "guicontainers:*", "Storage");
      init(presets, "GUIContainer", "guicontainer:*", "Storage");
      init(presets, "VirtualContainers", "virtualcontainers:*", "Storage");
      init(presets, "VirtualContainer", "virtualcontainer:*", "Storage");
      init(presets, "RemoteStorage", "remotestorage:*", "Storage");
      init(presets, "RemoteStorages", "remotestorages:*", "Storage");
      init(presets, "RemoteChest", "remotechest:*", "Storage");
      init(presets, "RemoteChests", "remotechests:*", "Storage");
      init(presets, "RemoteVault", "remotevault:*", "Storage");
      init(presets, "RemoteVaults", "remotevaults:*", "Storage");
      init(presets, "AnywhereStorage", "anywherestorage:*", "Storage");
      init(presets, "AnywhereChest", "anywherechest:*", "Storage");
      init(presets, "AnywhereVault", "anywherevault:*", "Storage");
      init(presets, "PortableShulker", "portableshulker:*", "Storage");
      init(presets, "PortableShulkers", "portableshulkers:*", "Storage");
      init(presets, "PortableShulkerBox", "portableshulkerbox:*", "Storage");
      init(presets, "PortableShulkerBoxes", "portableshulkerboxes:*", "Storage");
      init(presets, "PocketChest", "pocketchest:*", "Storage");
      init(presets, "PocketChests", "pocketchests:*", "Storage");
      init(presets, "PocketStorage", "pocketstorage:*", "Storage");
      init(presets, "PocketVault", "pocketvault:*", "Storage");
      init(presets, "PocketVaults", "pocketvaults:*", "Storage");
      init(presets, "PocketBackpack", "pocketbackpack:*", "Storage");
      init(presets, "PocketBackpacks", "pocketbackpacks:*", "Storage");
      init(presets, "PersonalChest", "personalchest:*", "Storage");
      init(presets, "PersonalChests", "personalchests:*", "Storage");
      init(presets, "PersonalEnderChest", "personalenderchest:*", "Storage");
      init(presets, "PersonalEnderChests", "personalenderchests:*", "Storage");
      init(presets, "CustomChest", "customchest:*", "Storage");
      init(presets, "CustomChests", "customchests:*", "Storage");
      init(presets, "CustomVault", "customvault:*", "Storage");
      init(presets, "CustomVaults", "customvaults:*", "Storage");
      init(presets, "CustomBackpack", "custombackpack:*", "Storage");
      init(presets, "CustomBackpacks", "custombackpacks:*", "Storage");
      init(presets, "MinionBackpack", "minionbackpack:*", "Minions");
      init(presets, "MinionBackpacks", "minionbackpacks:*", "Minions");
      init(presets, "MinionChestPlus", "minionchestplus:*", "Minions");
      init(presets, "MinionVault", "minionvault:*", "Minions");
      init(presets, "MinionVaults", "minionvaults:*", "Minions");
      init(presets, "MinionContainer", "minioncontainer:*", "Minions");
      init(presets, "MinionContainers", "minioncontainers:*", "Minions");
      init(presets, "VShulker", "vshulker:*", "Storage");
      init(presets, "VShulkers", "vshulkers:*", "Storage");
      init(presets, "VShulkerBox", "vshulkerbox:*", "Storage");
      init(presets, "VShulkerBoxes", "vshulkerboxes:*", "Storage");
      init(presets, "VirtualShulker", "virtualshulker:*", "Storage");
      init(presets, "VirtualShulkers", "virtualshulkers:*", "Storage");
      init(presets, "VirtualShulkerBox", "virtualshulkerbox:*", "Storage");
      init(presets, "VirtualShulkerBoxes", "virtualshulkerboxes:*", "Storage");
      init(presets, "DonutEC", "donutec:*", "Storage");
      init(presets, "Donut EChest", "donutechest:*", "Storage");
      init(presets, "Donut EnderChest", "donutenderchest:*", "Storage");
      init(presets, "Donut EnderChests", "donutenderchests:*", "Storage");
      init(presets, "Donut Ender Chest", "donut_ender_chest:*", "Storage");
      init(presets, "Donut Ender Chests", "donut_ender_chests:*", "Storage");
      init(presets, "Donut Ender Storage", "donutenderstorage:*", "Storage");
      init(presets, "Donut Storage", "donutstorage:*", "Storage");
      init(presets, "Donut Vault", "donutvault:*", "Storage");
      init(presets, "Donut Vaults", "donutvaults:*", "Storage");
      init(presets, "Donut Backpack", "donutbackpack:*", "Storage");
      init(presets, "Donut Backpacks", "donutbackpacks:*", "Storage");
      init(presets, "GUI Family", "gui*", "Menus");
      init(presets, "Menu Family", "menu*", "Menus");
      init(presets, "Inventory Family", "inventory*", "Menus");
      init(presets, "Inventory Namespace", "inventory:*", "Menus");
      init(presets, "Chest Family", "chest*", "Storage");
      init(presets, "Chest Namespace", "chest:*", "Storage");
      init(presets, "OpenInv Family", "openinv*", "Storage");
      init(presets, "OpenInventory Family", "openinventory*", "Storage");
      init(presets, "ChestCommands", "chestcommands:*", "Menus");
      init(presets, "ChestCommand", "chestcommand:*", "Menus");
      init(presets, "ChestCommandsPlus", "chestcommandsplus:*", "Menus");
      init(presets, "ChestCommandsPro", "chestcommandspro:*", "Menus");
      init(presets, "ChestGUI", "chestgui:*", "Menus");
      init(presets, "ChestGUIs", "chestguis:*", "Menus");
      init(presets, "ChestMenu", "chestmenu:*", "Menus");
      init(presets, "ChestMenus", "chestmenus:*", "Menus");
      init(presets, "ChestInventory", "chestinventory:*", "Menus");
      init(presets, "ChestInventories", "chestinventories:*", "Menus");
      init(presets, "ZMenu", "zmenu:*", "Menus");
      init(presets, "ZMenus", "zmenus:*", "Menus");
      init(presets, "ZMenuPlus", "zmenuplus:*", "Menus");
      init(presets, "ZMenuPro", "zmenupro:*", "Menus");
      init(presets, "ZMenuGui", "zmenugui:*", "Menus");
      init(presets, "TrMenu", "trmenu:*", "Menus");
      init(presets, "TrMenus", "trmenus:*", "Menus");
      init(presets, "TrMenuPlus", "trmenuplus:*", "Menus");
      init(presets, "TrMenuPro", "trmenupro:*", "Menus");
      init(presets, "CustomGUI", "customgui:*", "Menus");
      init(presets, "CustomGUIs", "customguis:*", "Menus");
      init(presets, "CustomGuiPlus", "customguiplus:*", "Menus");
      init(presets, "CustomGuiPro", "customguipro:*", "Menus");
      init(presets, "CustomMenu", "custommenu:*", "Menus");
      init(presets, "CustomMenus", "custommenus:*", "Menus");
      init(presets, "CustomMenuPlus", "custommenuplus:*", "Menus");
      init(presets, "CustomMenuPro", "custommenupro:*", "Menus");
      init(presets, "CustomInventory", "custominventory:*", "Menus");
      init(presets, "CustomInventories", "custominventories:*", "Menus");
      init(presets, "InventoryMenu", "inventorymenu:*", "Menus");
      init(presets, "InventoryMenus", "inventorymenus:*", "Menus");
      init(presets, "InventoryGUIPlus", "inventoryguiplus:*", "Menus");
      init(presets, "InventoryGUIPro", "inventoryguipro:*", "Menus");
      init(presets, "InventoryMenuPlus", "inventorymenuplus:*", "Menus");
      init(presets, "InventoryMenuPro", "inventorymenupro:*", "Menus");
      init(presets, "GUIPlus", "guiplus:*", "Menus");
      init(presets, "GUIPro", "guipro:*", "Menus");
      init(presets, "GUICore", "guicore:*", "Menus");
      init(presets, "GUISystem", "guisystem:*", "Menus");
      init(presets, "GUIMenu", "guimenu:*", "Menus");
      init(presets, "GUIMenus", "guimenus:*", "Menus");
      init(presets, "BetterGUI", "bettergui:*", "Menus");
      init(presets, "BetterGUIs", "betterguis:*", "Menus");
      init(presets, "BetterMenu", "bettermenu:*", "Menus");
      init(presets, "BetterMenus", "bettermenus:*", "Menus");
      init(presets, "SimpleGUI", "simplegui:*", "Menus");
      init(presets, "SimpleGUIs", "simpleguis:*", "Menus");
      init(presets, "SimpleMenu", "simplemenu:*", "Menus");
      init(presets, "SimpleMenus", "simplemenus:*", "Menus");
      init(presets, "EasyGUI", "easygui:*", "Menus");
      init(presets, "EasyGUIs", "easyguis:*", "Menus");
      init(presets, "EasyMenu", "easymenu:*", "Menus");
      init(presets, "EasyMenus", "easymenus:*", "Menus");
      init(presets, "AdvancedGUI", "advancedgui:*", "Menus");
      init(presets, "AdvancedGUIs", "advancedguis:*", "Menus");
      init(presets, "AdvancedMenu", "advancedmenu:*", "Menus");
      init(presets, "AdvancedMenus", "advancedmenus:*", "Menus");
      init(presets, "DeluxeGUI", "deluxegui:*", "Menus");
      init(presets, "DeluxeGUIs", "deluxeguis:*", "Menus");
      init(presets, "UltraGUI", "ultragui:*", "Menus");
      init(presets, "UltraGUIs", "ultraguis:*", "Menus");
      init(presets, "UltraMenu", "ultramenu:*", "Menus");
      init(presets, "UltraMenus", "ultramenus:*", "Menus");
      init(presets, "PremiumGUI", "premiumgui:*", "Menus");
      init(presets, "PremiumGUIs", "premiumguis:*", "Menus");
      init(presets, "PremiumMenu", "premiummenu:*", "Menus");
      init(presets, "PremiumMenus", "premiummenus:*", "Menus");
      init(presets, "SmartMenus", "smartmenus:*", "Menus");
      init(presets, "SmartMenu", "smartmenu:*", "Menus");
      init(presets, "MenuGUI", "menugui:*", "Menus");
      init(presets, "MenuGUIs", "menuguis:*", "Menus");
      init(presets, "MenuBuilder", "menubuilder:*", "Menus");
      init(presets, "GUIBuilder", "guibuilder:*", "Menus");
      init(presets, "InventoryBuilder", "inventorybuilder:*", "Menus");
      init(presets, "MenuEngine", "menuengine:*", "Menus");
      init(presets, "GUIEngine", "guiengine:*", "Menus");
      init(presets, "InventoryEngine", "inventoryengine:*", "Menus");
      init(presets, "MenuAPI", "menuapi:*", "Menus");
      init(presets, "GUIAPI", "guiapi:*", "Menus");
      init(presets, "InventoryAPI", "inventoryapi:*", "Menus");
      init(presets, "NoobShulkerBoxes", "noobshulkerboxes:*", "Storage");
      init(presets, "NoobShulkerBox", "noobshulkerbox:*", "Storage");
      init(presets, "NoobShulker", "noobshulker:*", "Storage");
      init(presets, "NoobShulkers", "noobshulkers:*", "Storage");
      init(presets, "FastShulker", "fastshulker:*", "Storage");
      init(presets, "FastShulkers", "fastshulkers:*", "Storage");
      init(presets, "FastShulkerBox", "fastshulkerbox:*", "Storage");
      init(presets, "FastShulkerBoxes", "fastshulkerboxes:*", "Storage");
      init(presets, "UltimateShulker", "ultimateshulker:*", "Storage");
      init(presets, "UltimateShulkers", "ultimateshulkers:*", "Storage");
      init(presets, "UltimateShulkerBox", "ultimateshulkerbox:*", "Storage");
      init(presets, "UltimateShulkerBoxes", "ultimateshulkerboxes:*", "Storage");
      init(presets, "SuperShulker", "supershulker:*", "Storage");
      init(presets, "SuperShulkers", "supershulkers:*", "Storage");
      init(presets, "SmartShulker", "smartshulker:*", "Storage");
      init(presets, "SmartShulkers", "smartshulkers:*", "Storage");
      init(presets, "ShulkerFast", "shulkerfast:*", "Storage");
      init(presets, "ShulkersFast", "shulkersfast:*", "Storage");
      init(presets, "AH Family", "ah*", "Auctions");
      init(presets, "Auction Family", "auction*", "Auctions");
      init(presets, "AuctionHouse Family", "auctionhouse*", "Auctions");
      init(presets, "ZelAuction", "zelauction:*", "Auctions");
      init(presets, "ZelAuctionHouse", "zelauctionhouse:*", "Auctions");
      init(presets, "Zel Auction", "zel_auction:*", "Auctions");
      init(presets, "Zel Auction House", "zel_auction_house:*", "Auctions");
      init(presets, "ZelAH", "zelah:*", "Auctions");
      init(presets, "Zel AH", "zel_ah:*", "Auctions");
      init(presets, "OutAuction", "outauction:*", "Auctions");
      init(presets, "OutAuctions", "outauctions:*", "Auctions");
      init(presets, "OutAuctionHouse", "outauctionhouse:*", "Auctions");
      init(presets, "Out Auction", "out_auction:*", "Auctions");
      init(presets, "Out Auction House", "out_auction_house:*", "Auctions");
      init(presets, "AuctionHousePlus", "auctionhouseplus:*", "Auctions");
      init(presets, "AuctionHouse+", "auctionhouse_plus:*", "Auctions");
      init(presets, "AuctionHousePlugin", "auctionhouseplugin:*", "Auctions");
      init(presets, "Auction House Plugin", "auction_house_plugin:*", "Auctions");
      init(presets, "Auction-House", "auction-house:*", "Auctions");
      init(presets, "Auction House", "auction_house:*", "Auctions");
      init(presets, "AuctionHouseRewrite", "auctionhouserewrite:*", "Auctions");
      init(presets, "Auction House Rewrite", "auction_house_rewrite:*", "Auctions");
      init(presets, "AuctionHouseReloaded", "auctionhousereloaded:*", "Auctions");
      init(presets, "AuctionHouseRevived", "auctionhouserevived:*", "Auctions");
      init(presets, "AuctionHouseModern", "auctionhousemodern:*", "Auctions");
      init(presets, "AuctionHouseLite", "auctionhouselite:*", "Auctions");
      init(presets, "AuctionHouseFree", "auctionhousefree:*", "Auctions");
      init(presets, "AuctionHousePremium", "auctionhousepremium:*", "Auctions");
      init(presets, "AuctionHouseRedis", "auctionhouseredis:*", "Auctions");
      init(presets, "AuctionRedis", "auctionredis:*", "Auctions");
      init(presets, "AuctionHouseIlius", "auctionhouseilius:*", "Auctions");
      init(presets, "KiranAuctionHouse", "kiranauctionhouse:*", "Auctions");
      init(presets, "KiranAuction", "kiranauction:*", "Auctions");
      init(presets, "FAuction", "fauction:*", "Auctions");
      init(presets, "FAuctions", "fauctions:*", "Auctions");
      init(presets, "FAuctionHouse", "fauctionhouse:*", "Auctions");
      init(presets, "F Auction", "f_auction:*", "Auctions");
      init(presets, "F Auction House", "f_auction_house:*", "Auctions");
      init(presets, "AdvancedAuctionHouse", "advancedauctionhouse:*", "Auctions");
      init(presets, "Advanced Auction House", "advanced_auction_house:*", "Auctions");
      init(presets, "UltimateAuctionHouse", "ultimateauctionhouse:*", "Auctions");
      init(presets, "Ultimate Auction House", "ultimate_auction_house:*", "Auctions");
      init(presets, "TheUltimateAuctionHouse", "theultimateauctionhouse:*", "Auctions");
      init(presets, "The Ultimate Auction House", "the_ultimate_auction_house:*", "Auctions");
      init(presets, "FateAuctionHouse", "fateauctionhouse:*", "Auctions");
      init(presets, "FateAuction", "fateauction:*", "Auctions");
      init(presets, "FADAH", "fadah:*", "Auctions");
      init(presets, "FADAuctionHouse", "fadauctionhouse:*", "Auctions");
      init(presets, "FAD Auction House", "fad_auction_house:*", "Auctions");
      init(presets, "ZAuctionHouse Alt", "z_auctionhouse:*", "Auctions");
      init(presets, "Z Auction House", "z_auction_house:*", "Auctions");
      init(presets, "Z Auctions", "zauctions:*", "Auctions");
      init(presets, "zAuctionHousePro", "zauctionhousepro:*", "Auctions");
      init(presets, "zAuctionHousePlus", "zauctionhouseplus:*", "Auctions");
      init(presets, "AzAuctionHouse Plus", "azauctionhouseplus:*", "Auctions");
      init(presets, "AzAuctionHouse Pro", "azauctionhousepro:*", "Auctions");
      init(presets, "AxAuctionHouse", "axauctionhouse:*", "Auctions");
      init(presets, "AxAuction", "axauction:*", "Auctions");
      init(presets, "AxAuctionHouse Plus", "axauctionhouseplus:*", "Auctions");
      init(presets, "AxAuctionHouse Pro", "axauctionhousepro:*", "Auctions");
      init(presets, "NexusAuction", "nexusauction:*", "Auctions");
      init(presets, "NexusAuctions", "nexusauctions:*", "Auctions");
      init(presets, "NexusAH", "nexusah:*", "Auctions");
      init(presets, "Nexus Auction House", "nexus_auction_house:*", "Auctions");
      init(presets, "PlayerAuctionHouse", "playerauctionhouse:*", "Auctions");
      init(presets, "Player Auction House", "player_auction_house:*", "Auctions");
      init(presets, "PlayerAH", "playerah:*", "Auctions");
      init(presets, "Player AH", "player_ah:*", "Auctions");
      init(presets, "OlziePlayerAuctions", "olzieplayerauctions:*", "Auctions");
      init(presets, "Olzie Auctions", "olzieauctions:*", "Auctions");
      init(presets, "CrazyAuctionHouse", "crazyauctionhouse:*", "Auctions");
      init(presets, "Crazy Auction House", "crazy_auction_house:*", "Auctions");
      init(presets, "CrazyAuction", "crazyauction:*", "Auctions");
      init(presets, "Crazy Auction", "crazy_auction:*", "Auctions");
      init(presets, "ExcellentAuction", "excellentauction:*", "Auctions");
      init(presets, "ExcellentAuctionHouse Plus", "excellentauctionhouseplus:*", "Auctions");
      init(presets, "ExcellentAuctionHouse Pro", "excellentauctionhousepro:*", "Auctions");
      init(presets, "ExcellentAH", "excellentah:*", "Auctions");
      init(presets, "AuctionMasterPro", "auctionmasterpro:*", "Auctions");
      init(presets, "AuctionMasterPlus", "auctionmasterplus:*", "Auctions");
      init(presets, "AuctionMasters", "auctionmasters:*", "Auctions");
      init(presets, "AuctionGUIs", "auctionguis:*", "Auctions");
      init(presets, "AuctionGUIPro", "auctionguipro:*", "Auctions");
      init(presets, "AuctionGUIPlus Alt", "auctiongui_plus:*", "Auctions");
      init(presets, "AuctionMenu", "auctionmenu:*", "Auctions");
      init(presets, "AuctionMenus", "auctionmenus:*", "Auctions");
      init(presets, "AuctionMenuPlus", "auctionmenuplus:*", "Auctions");
      init(presets, "AuctionMenuPro", "auctionmenupro:*", "Auctions");
      init(presets, "AuctionSystem", "auctionsystem:*", "Auctions");
      init(presets, "AuctionsSystem", "auctionssystem:*", "Auctions");
      init(presets, "AuctionCore", "auctioncore:*", "Auctions");
      init(presets, "AuctionsCore", "auctionscore:*", "Auctions");
      init(presets, "AuctionAPI", "auctionapi:*", "Auctions");
      init(presets, "AuctionsAPI", "auctionsapi:*", "Auctions");
      init(presets, "AuctionEngine", "auctionengine:*", "Auctions");
      init(presets, "AuctionMarket", "auctionmarket:*", "Auctions");
      init(presets, "AuctionMarkets", "auctionmarkets:*", "Auctions");
      init(presets, "AuctionMarketplace", "auctionmarketplace:*", "Auctions");
      init(presets, "AuctionMarketPlace", "auctionmarket_place:*", "Auctions");
      init(presets, "AuctionListings", "auctionlistings:*", "Auctions");
      init(presets, "AuctionListing", "auctionlisting:*", "Auctions");
      init(presets, "ListingAuction", "listingauction:*", "Auctions");
      init(presets, "ListingsAuction", "listingsauction:*", "Auctions");
      init(presets, "ItemAuction", "itemauction:*", "Auctions");
      init(presets, "ItemAuctions", "itemauctions:*", "Auctions");
      init(presets, "ItemAuctionHouse", "itemauctionhouse:*", "Auctions");
      init(presets, "ItemAH", "itemah:*", "Auctions");
      init(presets, "SellAuction", "sellauction:*", "Auctions");
      init(presets, "SellAuctions", "sellauctions:*", "Auctions");
      init(presets, "BidAuction", "bidauction:*", "Auctions");
      init(presets, "BidAuctions", "bidauctions:*", "Auctions");
      init(presets, "Bidding", "bidding:*", "Auctions");
      init(presets, "Bids", "bids:*", "Auctions");
      init(presets, "Bid", "bid:*", "Auctions");
      init(presets, "MarketplaceAuctions", "marketplaceauctions:*", "Auctions");
      init(presets, "MarketplaceAuction", "marketplaceauction:*", "Auctions");
      init(presets, "MarketAuctionHouse", "marketauctionhouse:*", "Auctions");
      init(presets, "MarketAH", "marketah:*", "Auctions");
      init(presets, "BazaarAuction", "bazaarauction:*", "Auctions");
      init(presets, "BazaarAuctions", "bazaarauctions:*", "Auctions");
      init(presets, "BazaarAH", "bazaarah:*", "Auctions");
      init(presets, "TradeAuction", "tradeauction:*", "Auctions");
      init(presets, "TradeAuctions", "tradeauctions:*", "Auctions");
      init(presets, "TradeAH", "tradeah:*", "Auctions");
      init(presets, "GlobalAuction", "globalauction:*", "Auctions");
      init(presets, "GlobalAuctions", "globalauctions:*", "Auctions");
      init(presets, "GlobalAuctionHouse", "globalauctionhouse:*", "Auctions");
      init(presets, "GlobalMarket", "globalmarket:*", "Auctions");
      init(presets, "GlobalMarketPlus", "globalmarketplus:*", "Auctions");
      init(presets, "GlobalMarketChestShop", "globalmarketchestshop:*", "Auctions");
      init(presets, "ServerAuction", "serverauction:*", "Auctions");
      init(presets, "ServerAuctions", "serverauctions:*", "Auctions");
      init(presets, "ServerAuctionHouse", "serverauctionhouse:*", "Auctions");
      init(presets, "CommunityAuctionHouse", "communityauctionhouse:*", "Auctions");
      init(presets, "CommunityAH", "communityah:*", "Auctions");
      init(presets, "LiteAuction", "liteauction:*", "Auctions");
      init(presets, "LiteAuctions", "liteauctions:*", "Auctions");
      init(presets, "SimpleAuction", "simpleauction:*", "Auctions");
      init(presets, "SimpleAuctions", "simpleauctions:*", "Auctions");
      init(presets, "SimpleAuctionHouse", "simpleauctionhouse:*", "Auctions");
      init(presets, "BetterAuctionHouse", "betterauctionhouse:*", "Auctions");
      init(presets, "BetterAuctions", "betterauctions:*", "Auctions");
      init(presets, "BetterAH", "betterah:*", "Auctions");
      init(presets, "EasyAuction", "easyauction:*", "Auctions");
      init(presets, "EasyAuctions", "easyauctions:*", "Auctions");
      init(presets, "EasyAuctionHouse", "easyauctionhouse:*", "Auctions");
      init(presets, "DeluxeAuction", "deluxeauction:*", "Auctions");
      init(presets, "DeluxeAuctions", "deluxeauctions:*", "Auctions");
      init(presets, "DeluxeAuctionHouse", "deluxeauctionhouse:*", "Auctions");
      init(presets, "DeluxeAuctionsRedis", "deluxeauctionsredis:*", "Auctions");
      init(presets, "Deluxe Auction Redis", "deluxe_auction_redis:*", "Auctions");
      init(presets, "Deluxe Auctions Redis", "deluxe_auctions_redis:*", "Auctions");
      init(presets, "PremiumAuction", "premiumauction:*", "Auctions");
      init(presets, "PremiumAuctions", "premiumauctions:*", "Auctions");
      init(presets, "PremiumAuctionHouse", "premiumauctionhouse:*", "Auctions");
      init(presets, "UltraAuction", "ultraauction:*", "Auctions");
      init(presets, "UltraAuctions", "ultraauctions:*", "Auctions");
      init(presets, "UltraAuctionHouse", "ultraauctionhouse:*", "Auctions");
      init(presets, "RoyalAuction", "royalauction:*", "Auctions");
      init(presets, "RoyalAuctions", "royalauctions:*", "Auctions");
      init(presets, "RoyalAuctionHouse", "royalauctionhouse:*", "Auctions");
      init(presets, "SupremeAuction", "supremeauction:*", "Auctions");
      init(presets, "SupremeAuctions", "supremeauctions:*", "Auctions");
      init(presets, "SupremeAuctionHouse", "supremeauctionhouse:*", "Auctions");
      init(presets, "HuskAuction", "huskauction:*", "Auctions");
      init(presets, "HuskAuctionHouse", "huskauctionhouse:*", "Auctions");
      init(presets, "HuskAH", "huskah:*", "Auctions");
      return Collections.unmodifiableList(presets);
   }

   private static void init(List<bB.Inner_CachePool> presets, String label, String pattern, String group) {
      String normalized = init_Str_Str(pattern);
      bB.Inner_BufferHelper kind = init_bB_BufferHelper_Str(normalized);
      String evidence = kind == bB.Inner_BufferHelper.read ? "Template only; no exact public channel proof." : "";
      init(presets, label, normalized, group, kind, evidence, load_Str_Str(normalized));
   }

   private static void init_V_List(List<bB.Inner_CachePool> presets, String label, String pattern, String group, String evidence) {
      init(presets, label, init_Str_Str(pattern), group, bB.Inner_BufferHelper.init_bB_BufferHelper, evidence, "");
   }

   private static void load_V_List(List<bB.Inner_CachePool> presets, String label, String pattern, String group, String evidence) {
      init(presets, label, init_Str_Str(pattern), group, bB.Inner_BufferHelper.load, evidence, "");
   }

   private static void init(
      List<bB.Inner_CachePool> presets, String label, String normalized, String group, bB.Inner_BufferHelper kind, String evidence, String templatePrefix
   ) {
      String key = write_Str_Str(normalized);

      for (bB.Inner_CachePool preset : presets) {
         if (preset.init_Str().equals(key)) {
            return;
         }
      }

      bB.Inner_BufferHelper safeKind = kind == null ? init_bB_BufferHelper_Str(normalized) : kind;
      String safeEvidence = evidence == null ? "" : evidence;
      String safeTemplate = templatePrefix == null ? "" : templatePrefix;
      boolean invalidExact = safeKind == bB.Inner_BufferHelper.init_bB_BufferHelper
         && (safeEvidence.isBlank() || normalized.indexOf(42) >= 0 || !bC.load_Z_Str(normalized));
      if (invalidExact) {
         safeKind = bB.Inner_BufferHelper.write;
         safeTemplate = safeTemplate.isBlank() ? load_Str_Str(normalized) : safeTemplate;
         safeEvidence = safeEvidence.isBlank() ? "Exact channel proof missing; kept as editable template." : safeEvidence;
      }

      presets.add(new bB.Inner_CachePool(label, normalized, bB.Inner_DataUtil.init, group, safeKind, safeEvidence, safeTemplate));
   }

   static {
      for (String warning : load) {
         AutismClientAddon.LOG.warn("[PayloadFilters] Preset catalog warning: {}", warning);
      }
   }

   public enum Inner_BufferHelper {
      init_bB_BufferHelper("Exact"),
      load("Core"),
      read("Template"),
      write("Unver");

      private final String init_Str;

      Inner_BufferHelper(String badge) {
         this.init_Str = badge;
      }

      public String init_Str() {
         return this.init_Str;
      }

      public boolean init_Z() {
         return this == read || this == write;
      }
   }

   public record Inner_CachePool(
      String init_Str, String load, bB.Inner_DataUtil init_bB_DataUtil, String read, bB.Inner_BufferHelper init_bB_BufferHelper, String write, String update
   ) {
      public Inner_CachePool(String label, String pattern, bB.Inner_DataUtil direction, String group) {
         this(label, pattern, direction, group, bB.init_bB_BufferHelper_Str(pattern), "", bB.load_Str_Str(pattern));
      }

      public String init_Str() {
         return bB.write_Str_Str(this.load);
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

      public String sync() {
         return this.update;
      }
   }

   public enum Inner_DataUtil {
      init,
      load,
      read;

      public static bB.Inner_DataUtil init_bB_DataUtil_Str(String value) {
         if (value == null) {
            return init;
         }

         try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException ignored) {
            return init;
         }
      }

      public boolean init_Z_Str(String packetDirection) {
         return true;
      }
   }

   public record Inner_StreamUtil(String init_Str, String load, bB.Inner_DataUtil init_bB_DataUtil) {
      public String init_Str() {
         return (this.init_Str + " " + this.load).toLowerCase(Locale.ROOT);
      }

      public String load() {
         return this.init_Str;
      }

      public String read() {
         return this.load;
      }
   }
}
