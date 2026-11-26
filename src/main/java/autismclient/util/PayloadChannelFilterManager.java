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
   private static final List<PayloadChannelFilterManager.Inner_CachePool> init = flush_List();
   private static final List<String> load = init_List_List(init);
   private final List<AutismConfig.PayloadChannelFilterRule> read = new java.util.concurrent.CopyOnWriteArrayList<>();

   public PayloadChannelFilterManager() {
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

   public List<PayloadChannelFilterManager.Inner_CachePool> read_List() {
      return init;
   }

   public static List<PayloadChannelFilterManager.Inner_CachePool> write_List() {
      return init;
   }

   public static List<String> update_List() {
      return load;
   }

   public static boolean init_Z_bB_CachePool_Str(PayloadChannelFilterManager.Inner_CachePool preset, String channelName) {
      return preset == null ? false : load(preset.read(), read_Str_Str(channelName));
   }

   public static boolean init_Z_Str_Str(String pattern, String channelName) {
      return load(pattern, read_Str_Str(channelName));
   }

   public static boolean init_Z_bB_CachePool(PayloadChannelFilterManager.Inner_CachePool preset) {
      if (preset == null) {
         return false;
      }

      if (preset.init_bB_BufferHelper() != PayloadChannelFilterManager.Inner_BufferHelper.EXACT) {
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

   public boolean init(String label, String pattern, PayloadChannelFilterManager.Inner_DataUtil direction) {
      return this.init_Z_Str(label, pattern, direction, false, true);
   }

   public boolean load(String label, String pattern, PayloadChannelFilterManager.Inner_DataUtil direction) {
      return this.load_Z_Str(label, pattern, direction, false, true);
   }

   public boolean load_Z_bB_CachePool(PayloadChannelFilterManager.Inner_CachePool preset) {
      return preset == null ? false : this.init_Z_Str(preset.load(), preset.read(), preset.init_bB_DataUtil(), true, true);
   }

   public boolean read_Z_bB_CachePool(PayloadChannelFilterManager.Inner_CachePool preset) {
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

   public boolean write_Z_bB_CachePool(PayloadChannelFilterManager.Inner_CachePool preset) {
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

   public boolean update(PayloadChannelFilterManager.Inner_CachePool preset) {
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

   public boolean sync(PayloadChannelFilterManager.Inner_CachePool preset) {
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
      return PayloadSubscriptionManager.init(includeLoggerDefaults).init_List();
   }

   public int init_I_Str(String group) {
      if (group != null && !group.isBlank()) {
         int count = 0;

         for (PayloadChannelFilterManager.Inner_CachePool preset : init) {
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

         for (PayloadChannelFilterManager.Inner_CachePool preset : init) {
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

         for (PayloadChannelFilterManager.Inner_CachePool preset : init) {
            if (group.equals(preset.write())) {
               groupKeys.add(preset.init_Str());
            }
         }

         if (!groupKeys.isEmpty()) {
            if (!enabled) {
               changed = this.read.removeIf(rule -> rule != null && rule.preset && groupKeys.contains(write_Str_Str(rule.pattern)));
            } else {
               for (PayloadChannelFilterManager.Inner_CachePool preset : init) {
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

   public void init_V_bB_CachePool(PayloadChannelFilterManager.Inner_CachePool preset) {
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

   public void load_V_bB_CachePool(PayloadChannelFilterManager.Inner_CachePool preset) {
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

   private boolean open(PayloadChannelFilterManager.Inner_CachePool preset) {
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

      for (PayloadChannelFilterManager.Inner_CachePool preset : init) {
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

      for (PayloadChannelFilterManager.Inner_CachePool preset : init) {
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
      Map<String, PayloadChannelFilterManager.Inner_CachePool> defaults = init_Map();
      if (!defaults.isEmpty()) {
         boolean[] changed = new boolean[]{false};
         this.read.removeIf(rule -> {
            if (rule == null) {
               changed[0] = true;
               return true;
            }
            String key = write_Str_Str(rule.pattern);
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
      Map<String, PayloadChannelFilterManager.Inner_CachePool> defaults = init_Map();
      if (!defaults.isEmpty()) {
         this.read.removeIf(rule -> {
            if (rule == null) {
               return true;
            }
            String key = write_Str_Str(rule.pattern);
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

      for (PayloadChannelFilterManager.Inner_CachePool preset : init) {
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

   private static Map<String, PayloadChannelFilterManager.Inner_CachePool> init_Map() {
      Map<String, PayloadChannelFilterManager.Inner_CachePool> defaults = new LinkedHashMap<>();

      for (PayloadChannelFilterManager.Inner_CachePool preset : init) {
         if (poll(preset)) {
            defaults.put(preset.init_Str(), preset);
         }
      }

      return defaults;
   }

   public PayloadChannelFilterManager.Inner_StreamUtil init_bB_StreamUtil_AutismPayloadSuppo_Str(AutismPayloadSupport.Inner_StreamUtil snapshot, String packetDirection) {
      return snapshot != null && snapshot.load_Str() != null && !snapshot.load_Str().isBlank()
         ? this.init_bB_StreamUtil_Str_Str(snapshot.load_Str(), packetDirection)
         : null;
   }

   public PayloadChannelFilterManager.Inner_StreamUtil init_bB_StreamUtil_Str_Str(String channelName, String packetDirection) {
      String channel = read_Str_Str(channelName);
      if (channel.isBlank()) {
         return null;
      }

      for (AutismConfig.PayloadChannelFilterRule rule : this.read) {
         if (rule != null && rule.enabled && load(rule.pattern, channel)) {
            String label = rule.label != null && !rule.label.isBlank() ? rule.label : rule.pattern;
            return new PayloadChannelFilterManager.Inner_StreamUtil(label, rule.pattern, PayloadChannelFilterManager.Inner_DataUtil.ANY);
         }
      }

      return null;
   }

   public String init_Str_AutismPayloadSuppo_Str(AutismPayloadSupport.Inner_StreamUtil snapshot, String packetDirection) {
      PayloadChannelFilterManager.Inner_StreamUtil match = this.init_bB_StreamUtil_AutismPayloadSuppo_Str(snapshot, packetDirection);
      return match == null ? "" : match.init_Str();
   }

   public static boolean flush(PayloadChannelFilterManager.Inner_CachePool preset) {
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

   public static boolean close(PayloadChannelFilterManager.Inner_CachePool preset) {
      return preset != null && preset.init_bB_BufferHelper() == PayloadChannelFilterManager.Inner_BufferHelper.EXACT ? PayloadRegistrationManager.load_Z_Str(preset.read()) : false;
   }

   public static String init_Str_bB_CachePool(PayloadChannelFilterManager.Inner_CachePool preset) {
      if (preset == null) {
         return "";
      }

      String prefix = preset.sync();
      return prefix != null && !prefix.isBlank() ? prefix : load_Str_Str(preset.read());
   }

   private static boolean poll(PayloadChannelFilterManager.Inner_CachePool preset) {
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

      autismclient.modules.ModuleRuntimeManager.init_PoolEntry().update_V();
   }

   public static String init_Str_Str(String pattern) {
      return pattern == null ? "" : read_Str_Str(pattern.trim());
   }

   private static PayloadChannelFilterManager.Inner_BufferHelper init_bB_BufferHelper_Str(String pattern) {
      String normalized = init_Str_Str(pattern);
      if (read_Z_Str(normalized)) {
         return PayloadChannelFilterManager.Inner_BufferHelper.CORE;
      } else if (normalized.indexOf(42) >= 0) {
         return PayloadChannelFilterManager.Inner_BufferHelper.TEMPLATE;
      } else {
         return PayloadRegistrationManager.load_Z_Str(normalized) ? PayloadChannelFilterManager.Inner_BufferHelper.UNVERIFIED : PayloadChannelFilterManager.Inner_BufferHelper.TEMPLATE;
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

   private boolean init_Z_Str(String label, String pattern, PayloadChannelFilterManager.Inner_DataUtil direction, boolean preset, boolean enabled) {
      boolean changed = this.load_Z_Str(label, pattern, direction, preset, enabled);
      if (changed) {
         this.open();
      }

      return changed;
   }

   private boolean load_Z_Str(String label, String pattern, PayloadChannelFilterManager.Inner_DataUtil direction, boolean preset, boolean enabled) {
      String normalized = init_Str_Str(pattern);
      if (normalized.isBlank()) {
         return false;
      }

      PayloadChannelFilterManager.Inner_DataUtil dir = PayloadChannelFilterManager.Inner_DataUtil.ANY;
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

   private static List<String> init_List_List(List<PayloadChannelFilterManager.Inner_CachePool> presets) {
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

               String pattern = init_Str_Str(preset.read());
               if (preset.init_bB_BufferHelper() == PayloadChannelFilterManager.Inner_BufferHelper.EXACT) {
                  if (pattern.indexOf(42) >= 0) {
                     warnings.add("exact preset uses wildcard " + preset.read());
                  }

                  if (!PayloadRegistrationManager.load_Z_Str(pattern)) {
                     warnings.add("exact preset is not registerable " + preset.read());
                  }

                  if (preset.update() == null || preset.update().isBlank()) {
                     warnings.add("exact preset missing evidence " + preset.read());
                  }
               }

               if (preset.init_bB_BufferHelper() == PayloadChannelFilterManager.Inner_BufferHelper.CORE && !read_Z_Str(pattern)) {
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

   private static List<PayloadChannelFilterManager.Inner_CachePool> flush_List() {
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
                     load_V_List(presets, label, pattern, group, jsonString(obj, "evidence"));
                  } else if ("exact".equals(kind)) {
                     init_V_List(presets, label, pattern, group, jsonString(obj, "evidence"));
                  } else {
                     init(presets, label, pattern, group);
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

   private static void init(List<PayloadChannelFilterManager.Inner_CachePool> presets, String label, String pattern, String group) {
      String normalized = init_Str_Str(pattern);
      PayloadChannelFilterManager.Inner_BufferHelper kind = init_bB_BufferHelper_Str(normalized);
      String evidence = kind == PayloadChannelFilterManager.Inner_BufferHelper.TEMPLATE ? "Template only; no exact public channel proof." : "";
      init(presets, label, normalized, group, kind, evidence, load_Str_Str(normalized));
   }

   private static void init_V_List(List<PayloadChannelFilterManager.Inner_CachePool> presets, String label, String pattern, String group, String evidence) {
      init(presets, label, init_Str_Str(pattern), group, PayloadChannelFilterManager.Inner_BufferHelper.EXACT, evidence, "");
   }

   private static void load_V_List(List<PayloadChannelFilterManager.Inner_CachePool> presets, String label, String pattern, String group, String evidence) {
      init(presets, label, init_Str_Str(pattern), group, PayloadChannelFilterManager.Inner_BufferHelper.CORE, evidence, "");
   }

   private static void init(
      List<PayloadChannelFilterManager.Inner_CachePool> presets, String label, String normalized, String group, PayloadChannelFilterManager.Inner_BufferHelper kind, String evidence, String templatePrefix
   ) {
      String key = write_Str_Str(normalized);

      for (PayloadChannelFilterManager.Inner_CachePool preset : presets) {
         if (preset.init_Str().equals(key)) {
            return;
         }
      }

      PayloadChannelFilterManager.Inner_BufferHelper safeKind = kind == null ? init_bB_BufferHelper_Str(normalized) : kind;
      String safeEvidence = evidence == null ? "" : evidence;
      String safeTemplate = templatePrefix == null ? "" : templatePrefix;
      boolean invalidExact = safeKind == PayloadChannelFilterManager.Inner_BufferHelper.EXACT
         && (safeEvidence.isBlank() || normalized.indexOf(42) >= 0 || !PayloadRegistrationManager.load_Z_Str(normalized));
      if (invalidExact) {
         safeKind = PayloadChannelFilterManager.Inner_BufferHelper.UNVERIFIED;
         safeTemplate = safeTemplate.isBlank() ? load_Str_Str(normalized) : safeTemplate;
         safeEvidence = safeEvidence.isBlank() ? "Exact channel proof missing; kept as editable template." : safeEvidence;
      }

      presets.add(new PayloadChannelFilterManager.Inner_CachePool(label, normalized, PayloadChannelFilterManager.Inner_DataUtil.ANY, group, safeKind, safeEvidence, safeTemplate));
   }

   static {
      for (String warning : load) {
         AutismClientAddon.LOG.warn("[PayloadFilters] Preset catalog warning: {}", warning);
      }
   }

   public enum Inner_BufferHelper {
      EXACT("Exact"),
      CORE("Core"),
      TEMPLATE("Template"),
      UNVERIFIED("Unver");

      private final String init_Str;

      Inner_BufferHelper(String badge) {
         this.init_Str = badge;
      }

      public String init_Str() {
         return this.init_Str;
      }

      public boolean init_Z() {
         return this == TEMPLATE || this == UNVERIFIED;
      }
   }

   public record Inner_CachePool(
      String init_Str, String load, PayloadChannelFilterManager.Inner_DataUtil init_bB_DataUtil, String read, PayloadChannelFilterManager.Inner_BufferHelper init_bB_BufferHelper, String write, String update
   ) {
      public Inner_CachePool(String label, String pattern, PayloadChannelFilterManager.Inner_DataUtil direction, String group) {
         this(label, pattern, direction, group, PayloadChannelFilterManager.init_bB_BufferHelper_Str(pattern), "", PayloadChannelFilterManager.load_Str_Str(pattern));
      }

      public String init_Str() {
         return PayloadChannelFilterManager.write_Str_Str(this.load);
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
      ANY,
      C2S,
      S2C;

      public static PayloadChannelFilterManager.Inner_DataUtil init_bB_DataUtil_Str(String value) {
         if (value == null) {
            return ANY;
         }

         try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException ignored) {
            return ANY;
         }
      }

      public boolean init_Z_Str(String packetDirection) {
         return true;
      }
   }

   public record Inner_StreamUtil(String init_Str, String load, PayloadChannelFilterManager.Inner_DataUtil init_bB_DataUtil) {
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
