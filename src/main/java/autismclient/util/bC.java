package autismclient.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;

public final class bC {
   public static final String init_Str = "custom";
   public static final String load = "preset";
   public static final String read = "learned";
   private final List<AutismConfig.PayloadChannelRegistrationRule> init_List = new ArrayList<>();

   public bC() {
      this.init_V();
   }

   public void init_V() {
      this.init_List.clear();
      AutismConfig config = AutismConfig.getGlobal();
      if (config.packetLoggerPayloadRegistrations == null) {
         config.packetLoggerPayloadRegistrations = new ArrayList<>();
      }

      Map<String, AutismConfig.PayloadChannelRegistrationRule> merged = new LinkedHashMap<>();

      for (AutismConfig.PayloadChannelRegistrationRule rule : config.packetLoggerPayloadRegistrations) {
         AutismConfig.PayloadChannelRegistrationRule clean = init_AutismConfig_Paylo_AutismConfig_Paylo(rule);
         if (clean != null) {
            init_V_Map_AutismConfig_Paylo(merged, clean);
         }
      }

      if (merged.isEmpty() && config.packetLoggerPayloadFilters != null) {
         this.init_V_Map_List(merged, config.packetLoggerPayloadFilters);
      }

      this.init_List.addAll(merged.values());
   }

   public List<AutismConfig.PayloadChannelRegistrationRule> init_List() {
      return Collections.unmodifiableList(this.init_List);
   }

   List<AutismConfig.PayloadChannelRegistrationRule> load_List() {
      return this.init_List;
   }

   public List<String> read_List() {
      List<String> channels = new ArrayList<>();

      for (AutismConfig.PayloadChannelRegistrationRule rule : this.init_List) {
         if (rule != null && rule.enabled && load_Z_Str(rule.channel)) {
            channels.add(init_Str_Str(rule.channel));
         }
      }

      return channels;
   }

   public boolean init_Z_Str(String channel) {
      String normalized = init_Str_Str(channel);
      if (normalized.isBlank()) {
         return false;
      }

      for (AutismConfig.PayloadChannelRegistrationRule rule : this.init_List) {
         if (rule != null && rule.enabled && normalized.equals(init_Str_Str(rule.channel))) {
            return true;
         }
      }

      return false;
   }

   public int init_I_Str(String channel) {
      String normalized = init_Str_Str(channel);

      for (int i = 0; i < this.init_List.size(); i++) {
         AutismConfig.PayloadChannelRegistrationRule rule = this.init_List.get(i);
         if (rule != null && normalized.equals(init_Str_Str(rule.channel))) {
            return i;
         }
      }

      return -1;
   }

   public boolean init(String label, String channel, String source) {
      boolean changed = this.load(label, channel, source);
      if (changed) {
         this.sync();
      }

      return changed;
   }

   public boolean load(String label, String channel, String source) {
      String normalized = init_Str_Str(channel);
      if (!load_Z_Str(normalized)) {
         return false;
      }

      int index = this.init_I_Str(normalized);
      if (index >= 0) {
         AutismConfig.PayloadChannelRegistrationRule rule = this.init_List.get(index);
         boolean changed = false;
         if (!rule.enabled) {
            rule.enabled = true;
            changed = true;
         }

         String cleanLabel = init_Str_Str_Str(label, normalized);
         if ((rule.label == null || rule.label.isBlank() || rule.label.equals(rule.channel)) && !cleanLabel.equals(normalized)) {
            rule.label = cleanLabel;
            changed = true;
         }

         if (rule.source == null || rule.source.isBlank() || "learned".equals(rule.source)) {
            rule.source = read_Str_Str(source);
            changed = true;
         }

         return changed;
      } else {
         AutismConfig.PayloadChannelRegistrationRule rule = new AutismConfig.PayloadChannelRegistrationRule();
         rule.channel = normalized;
         rule.label = init_Str_Str_Str(label, normalized);
         rule.enabled = true;
         rule.source = read_Str_Str(source);
         this.init_List.add(rule);
         return true;
      }
   }

   public boolean init_Z_Str_Str(String label, String channel) {
      String normalized = init_Str_Str(channel);
      if (!load_Z_Str(normalized)) {
         return false;
      }

      if (this.init_I_Str(normalized) >= 0) {
         return false;
      }

      AutismConfig.PayloadChannelRegistrationRule rule = new AutismConfig.PayloadChannelRegistrationRule();
      rule.channel = normalized;
      rule.label = init_Str_Str_Str(label, normalized);
      rule.enabled = false;
      rule.source = "learned";
      this.init_List.add(rule);
      this.sync();
      return true;
   }

   public void init_V_I(int index) {
      if (index >= 0 && index < this.init_List.size()) {
         AutismConfig.PayloadChannelRegistrationRule rule = this.init_List.get(index);
         if (rule != null) {
            rule.enabled = !rule.enabled;
            this.sync();
         }
      }
   }

   public void load_V_I(int index) {
      if (index >= 0 && index < this.init_List.size()) {
         AutismConfig.PayloadChannelRegistrationRule rule = this.init_List.get(index);
         if (rule != null) {
            rule.enabled = !rule.enabled;
         }
      }
   }

   public void read_V_I(int index) {
      if (index >= 0 && index < this.init_List.size()) {
         this.init_List.remove(index);
         this.sync();
      }
   }

   public void write(int index) {
      if (index >= 0 && index < this.init_List.size()) {
         this.init_List.remove(index);
      }
   }

   public void load_V() {
      boolean changed = false;

      for (AutismConfig.PayloadChannelRegistrationRule rule : this.init_List) {
         if (rule != null && rule.enabled) {
            rule.enabled = false;
            changed = true;
         }
      }

      if (changed) {
         this.sync();
      }
   }

   public void read_V() {
      for (AutismConfig.PayloadChannelRegistrationRule rule : this.init_List) {
         if (rule != null) {
            rule.enabled = false;
         }
      }
   }

   public void init_V_Collection(Collection<String> channels) {
      Set<String> applied = new LinkedHashSet<>();
      if (channels != null) {
         for (String channel : channels) {
            String normalized = init_Str_Str(channel);
            if (load_Z_Str(normalized)) {
               applied.add(normalized);
            }
         }
      }

      boolean changed = false;

      for (String channel : applied) {
         int index = this.init_I_Str(channel);
         if (index >= 0) {
            AutismConfig.PayloadChannelRegistrationRule rule = this.init_List.get(index);
            if (!rule.enabled) {
               rule.enabled = true;
               changed = true;
            }
         } else {
            AutismConfig.PayloadChannelRegistrationRule rule = new AutismConfig.PayloadChannelRegistrationRule();
            rule.channel = channel;
            rule.label = channel;
            rule.enabled = true;
            rule.source = "custom";
            this.init_List.add(rule);
            changed = true;
         }
      }

      for (AutismConfig.PayloadChannelRegistrationRule rule : this.init_List) {
         if (rule != null) {
            boolean shouldEnable = applied.contains(init_Str_Str(rule.channel));
            if (rule.enabled != shouldEnable) {
               rule.enabled = shouldEnable;
               changed = true;
            }
         }
      }

      if (changed) {
         this.sync();
      }
   }

   public void write() {
      boolean changed = false;
      Set<String> defaults = new LinkedHashSet<>();

      for (bB.Inner_CachePool preset : bB.write_List()) {
         if (bB.flush(preset)) {
            String pattern = bB.init_Str_Str(preset.read());
            if (bB.close(preset)) {
               defaults.add(pattern);
            }
         }
      }

      for (AutismConfig.PayloadChannelRegistrationRule rule : this.init_List) {
         if (rule != null) {
            boolean shouldEnable = defaults.contains(init_Str_Str(rule.channel));
            if (rule.enabled != shouldEnable) {
               rule.enabled = shouldEnable;
               changed = true;
            }
         }
      }

      for (bB.Inner_CachePool preset : bB.write_List()) {
         if (bB.flush(preset)) {
            String pattern = bB.init_Str_Str(preset.read());
            if (bB.close(preset) && this.read(preset.load(), pattern, "preset")) {
               changed = true;
            }
         }
      }

      if (changed) {
         this.sync();
      }
   }

   public void update() {
      Set<String> defaults = new LinkedHashSet<>();

      for (bB.Inner_CachePool preset : bB.write_List()) {
         if (bB.flush(preset)) {
            String pattern = bB.init_Str_Str(preset.read());
            if (bB.close(preset)) {
               defaults.add(pattern);
            }
         }
      }

      for (AutismConfig.PayloadChannelRegistrationRule rule : this.init_List) {
         if (rule != null) {
            rule.enabled = defaults.contains(init_Str_Str(rule.channel));
         }
      }

      for (bB.Inner_CachePool preset : bB.write_List()) {
         if (bB.flush(preset)) {
            String pattern = bB.init_Str_Str(preset.read());
            if (bB.close(preset)) {
               this.read(preset.load(), pattern, "preset");
            }
         }
      }
   }

   public static boolean load_Z_Str(String channel) {
      String normalized = init_Str_Str(channel);
      if (!normalized.isBlank() && normalized.indexOf(42) < 0 && normalized.indexOf(58) > 0) {
         return !"minecraft:register".equals(normalized) && !"minecraft:unregister".equals(normalized) && !"minecraft:brand".equals(normalized)
            ? Identifier.tryParse(normalized) != null
            : false;
      } else {
         return false;
      }
   }

   public static String init_Str_Str(String channel) {
      return channel == null ? "" : channel.trim().toLowerCase(Locale.ROOT);
   }

   public static String load_Str_Str(String source) {
      String var10000;
      switch (read_Str_Str(source)) {
         case "preset":
            var10000 = "Preset";
            break;
         case "learned":
            var10000 = "Learned";
            break;
         default:
            var10000 = "Custom";
      }

      return var10000;
   }

   private void init_V_Map_List(Map<String, AutismConfig.PayloadChannelRegistrationRule> merged, List<AutismConfig.PayloadChannelFilterRule> filterRules) {
      for (AutismConfig.PayloadChannelFilterRule filter : filterRules) {
         if (filter != null && filter.enabled) {
            String pattern = bB.init_Str_Str(filter.pattern);
            if (load_Z_Str(pattern)) {
               AutismConfig.PayloadChannelRegistrationRule rule = new AutismConfig.PayloadChannelRegistrationRule();
               rule.channel = pattern;
               rule.label = init_Str_Str_Str(filter.label, pattern);
               rule.enabled = true;
               rule.source = filter.preset ? "preset" : "custom";
               init_V_Map_AutismConfig_Paylo(merged, rule);
            }
         }
      }
   }

   private boolean read(String label, String channel, String source) {
      String normalized = init_Str_Str(channel);
      if (!load_Z_Str(normalized)) {
         return false;
      }

      int index = this.init_I_Str(normalized);
      if (index >= 0) {
         AutismConfig.PayloadChannelRegistrationRule rule = this.init_List.get(index);
         boolean changed = false;
         if (!rule.enabled) {
            rule.enabled = true;
            changed = true;
         }

         if ("learned".equals(rule.source)) {
            rule.source = read_Str_Str(source);
            changed = true;
         }

         return changed;
      } else {
         AutismConfig.PayloadChannelRegistrationRule rule = new AutismConfig.PayloadChannelRegistrationRule();
         rule.channel = normalized;
         rule.label = init_Str_Str_Str(label, normalized);
         rule.enabled = true;
         rule.source = read_Str_Str(source);
         this.init_List.add(rule);
         return true;
      }
   }

   private static void init_V_Map_AutismConfig_Paylo(
      Map<String, AutismConfig.PayloadChannelRegistrationRule> merged, AutismConfig.PayloadChannelRegistrationRule clean
   ) {
      String key = init_Str_Str(clean.channel);
      AutismConfig.PayloadChannelRegistrationRule existing = merged.get(key);
      if (existing == null) {
         merged.put(key, clean);
      } else {
         existing.enabled = existing.enabled | clean.enabled;
         if ("learned".equals(existing.source) && !"learned".equals(clean.source)) {
            existing.source = clean.source;
         }

         if ((existing.label == null || existing.label.isBlank() || existing.label.equals(existing.channel)) && clean.label != null && !clean.label.isBlank()) {
            existing.label = clean.label;
         }
      }
   }

   private static AutismConfig.PayloadChannelRegistrationRule init_AutismConfig_Paylo_AutismConfig_Paylo(AutismConfig.PayloadChannelRegistrationRule rule) {
      if (rule == null) {
         return null;
      } else {
         String channel = init_Str_Str(rule.channel);
         if (!load_Z_Str(channel)) {
            return null;
         } else {
            String label = rule.label == null ? "" : rule.label.trim();
            if (!read_Z_Str(channel) && !read_Z_Str(label)) {
               AutismConfig.PayloadChannelRegistrationRule clean = new AutismConfig.PayloadChannelRegistrationRule();
               clean.channel = channel;
               clean.label = init_Str_Str_Str(label, channel);
               clean.enabled = rule.enabled;
               clean.source = read_Str_Str(rule.source);
               return clean;
            } else {
               return null;
            }
         }
      }
   }

   private static String init_Str_Str_Str(String label, String fallback) {
      String value = label == null ? "" : label.trim();
      return value.isBlank() ? fallback : value;
   }

   private static String read_Str_Str(String source) {
      String value = source == null ? "" : source.trim().toLowerCase(Locale.ROOT);
      String var10000;
      switch (value) {
         case "preset":
         case "learned":
            var10000 = value;
            break;
         default:
            var10000 = "custom";
      }

      return var10000;
   }

   private static boolean read_Z_Str(String value) {
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

   private void sync() {
      this.init_V_Z(true);
   }

   public void init_V_Z(boolean writeFile) {
      AutismConfig config = AutismConfig.getGlobal();
      config.packetLoggerPayloadRegistrations = new ArrayList<>(this.init_List);
      if (writeFile) {
         config.save();
      }

      autismclient.modules.PoolEntry.init_PoolEntry().init_V_Z(false);
   }
}
