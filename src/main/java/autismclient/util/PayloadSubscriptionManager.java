package autismclient.util;

import autismclient.AutismClientAddon;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;

public final class PayloadSubscriptionManager {
   private static final boolean TRACE = Boolean.getBoolean("autism.payload.trace");
   public static final int init_I = 1;
   private static final int load_I = 96;
   private static final int read_I = 120;
   private static final int write = 64;
   private static volatile boolean load_Z = true;
   private static int update;
   private static String init_Str = "";
   private static long init_J;
   private static PayloadSubscriptionManager.RegistrationStatus status = PayloadSubscriptionManager.RegistrationStatus.idle(96);
   private static final LinkedHashSet<String> init_LinkedHashSet = new LinkedHashSet<>();
   private static final LinkedHashSet<String> load_LinkedHashSet = new LinkedHashSet<>();
   private static final LinkedHashSet<String> read_LinkedHashSet = new LinkedHashSet<>();

   private PayloadSubscriptionManager() {
   }

   public static synchronized void requestSync() {
      load_Z = true;
      update = 0;
   }

   public static synchronized void reset() {
      load_Z = true;
      update = 0;
      init_Str = "";
      init_J = 0L;
      load_LinkedHashSet.clear();
      read_LinkedHashSet.clear();
      init_LinkedHashSet.clear();
      status = PayloadSubscriptionManager.RegistrationStatus.idle(96);
   }

   public static synchronized PayloadSubscriptionManager.RegistrationStatus status() {
      if (autismclient.modules.aD.read_Z()) {
         status = PayloadSubscriptionManager.RegistrationStatus.idle(configuredLimit(), 0);
         return status;
      }

      if (!isRegistrationUnlocked() && !status.locked()) {
         status = PayloadSubscriptionManager.RegistrationStatus.idle(configuredLimit(), status.write());
      }

      return status;
   }

   public static boolean isRegistrationUnlocked() {
      AutismConfig config = AutismConfig.getGlobal();
      return config != null && config.payloadRegistrationUnlocked && config.payloadRegistrationWarningAcceptedVersion >= 1;
   }

   public static synchronized void read() {
      if (!autismclient.modules.aD.read_Z()) {
         AutismConfig config = AutismConfig.getGlobal();
         config.payloadRegistrationUnlocked = true;
         config.payloadRegistrationWarningAcceptedVersion = 1;
         config.save();
         requestSync();
         status = PayloadSubscriptionManager.RegistrationStatus.idle(configuredLimit());
      }
   }

   public static void lockRegistration(Minecraft mc) {
      if (autismclient.modules.aD.read_Z()) {
         synchronized (PayloadSubscriptionManager.class) {
            AutismConfig config = AutismConfig.getGlobal();
            config.payloadRegistrationUnlocked = false;
            config.save();
            load_Z = false;
            update = 0;
            init_Str = "";
            init_J = 0L;
            init_LinkedHashSet.clear();
            status = PayloadSubscriptionManager.RegistrationStatus.idle(configuredLimit(), 0);
         }
      } else {
         PayloadSubscriptionManager.UnregisterResult unregister = new PayloadSubscriptionManager.UnregisterResult(0, true);
         if (isConnected(mc)) {
            unregister = init_bD_TaskQueue(List.of());
         }

         synchronized (PayloadSubscriptionManager.class) {
            AutismConfig config = AutismConfig.getGlobal();
            config.payloadRegistrationUnlocked = false;
            config.save();
            load_Z = false;
            update = 0;
            init_Str = "";
            init_J = System.currentTimeMillis();
            if (unregister.init_Z()) {
               init_LinkedHashSet.clear();
            }

            status = PayloadSubscriptionManager.RegistrationStatus.idle(configuredLimit(), unregister.init_I());
         }
      }
   }

   public static synchronized void learnCodecChannel(String channel) {
      String normalized = normalize(channel);
      if (isRegisterable(normalized) && !load_Z(normalized)) {
         moveToEnd(load_LinkedHashSet, normalized);
      }
   }

   public static synchronized void learnObservedChannel(String channel) {
      String normalized = normalize(channel);
      if (isRegisterable(normalized) && !load_Z(normalized)) {
         moveToEnd(read_LinkedHashSet, normalized);
      }
   }

   public static synchronized void observe(AutismPayloadSupport.PayloadEntry snapshot) {
      if (snapshot != null && "S2C".equalsIgnoreCase(snapshot.direction())) {
         learnObservedChannel(snapshot.channel());

         for (String channel : AutismPayloadSupport.extractRegisteredChannels(snapshot.channel(), snapshot.rawBytes())) {
            String normalized = normalize(channel);
            if (isRegisterable(normalized) && !load_Z(normalized)) {
               moveToEnd(read_LinkedHashSet, normalized);
            }
         }
      }
   }

   public static synchronized List<String> learnedChannels() {
      LinkedHashSet<String> channels = new LinkedHashSet<>();
      channels.addAll(load_LinkedHashSet);
      channels.addAll(read_LinkedHashSet);
      return List.copyOf(channels);
   }

   public static void tick(Minecraft mc, boolean packetLoggerCapturing) {
      if (load_Z && !autismclient.modules.aD.read_Z() && isConnected(mc)) {
         synchronized (PayloadSubscriptionManager.class) {
            if (update > 0) {
               update--;
               return;
            }

            if (!load_Z) {
               return;
            }
         }

         if (!isRegistrationUnlocked()) {
            synchronized (PayloadSubscriptionManager.class) {
               load_Z = false;
               init_Str = "";
               init_J = 0L;
               status = PayloadSubscriptionManager.RegistrationStatus.idle(configuredLimit(), 0);
            }
         } else {
            PayloadSubscriptionManager.RegistrationPlan plan = plan(packetLoggerCapturing);
            if (plan.channels().isEmpty()) {
               PayloadSubscriptionManager.UnregisterResult unregister = init_bD_TaskQueue(List.of());
               synchronized (PayloadSubscriptionManager.class) {
                  load_Z = false;
                  init_Str = "";
                  init_J = 0L;
                  status = new PayloadSubscriptionManager.RegistrationStatus(
                     0, plan.init_I(), plan.load_I(), unregister.init_Z(), System.currentTimeMillis(), List.of(), plan.load_List(), unregister.init_I(), false
                  );
               }
            } else {
               String key = String.join("\n", plan.channels());
               synchronized (PayloadSubscriptionManager.class) {
                  long now = System.currentTimeMillis();
                  if (!load_Z && key.equals(init_Str)) {
                     return;
                  }

                  init_Str = key;
                  init_J = now;
                  load_Z = false;
               }

               byte[] body = encodeChannels(plan.channels());
               if (body.length != 0) {
                  PayloadSubscriptionManager.UnregisterResult unregister = init_bD_TaskQueue(plan.channels());
                  boolean sent = AutismPayloadSupport.sendCustomPayloadSilent("minecraft:register", body, "play");
                  PayloadSubscriptionManager.RegistrationStatus newStatus = new PayloadSubscriptionManager.RegistrationStatus(
                     plan.channels().size(),
                     plan.init_I(),
                     plan.load_I(),
                     sent,
                     System.currentTimeMillis(),
                     plan.channels(),
                     plan.load_List(),
                     unregister.init_I(),
                     false
                  );
                  synchronized (PayloadSubscriptionManager.class) {
                     status = newStatus;
                     if (sent) {
                        init_LinkedHashSet.clear();
                        init_LinkedHashSet.addAll(plan.channels());
                     }
                  }

                  if (TRACE) {
                     AutismClientAddon.LOG
                        .info(
                           "[Autism Payload] registered {} / {} plugin channels, skipped {}, unregistered {}, sent={}, sample={}",
                           newStatus.registered(),
                           newStatus.limit(),
                           newStatus.read(),
                           unregister.init_I(),
                           sent,
                           newStatus.appliedSummary()
                        );
                  }
               }
            }
         }
      }
   }

   public static PayloadSubscriptionManager.RegistrationPlan plan(boolean packetLoggerCapturing) {
      int limit = configuredLimit();
      PayloadSubscriptionManager.PlanCollector collector = new PayloadSubscriptionManager.PlanCollector(limit);
      PayloadRegistrationManager registrations = new PayloadRegistrationManager();

      for (String channel : registrations.enabledChannels()) {
         collector.offer(channel);
      }

      return collector.build();
   }

   private static PayloadSubscriptionManager.UnregisterResult init_bD_TaskQueue(List<String> nextChannels) {
      LinkedHashSet<String> removed = new LinkedHashSet<>();
      synchronized (PayloadSubscriptionManager.class) {
         if (init_LinkedHashSet.isEmpty()) {
            return new PayloadSubscriptionManager.UnregisterResult(0, true);
         }

         Set<String> next = new LinkedHashSet<>(nextChannels == null ? List.of() : nextChannels);

         for (String channel : init_LinkedHashSet) {
            if (!next.contains(channel)) {
               removed.add(channel);
            }
         }
      }

      if (removed.isEmpty()) {
         return new PayloadSubscriptionManager.UnregisterResult(0, true);
      }

      boolean sent = AutismPayloadSupport.sendCustomPayloadSilent("minecraft:unregister", encodeChannels(List.copyOf(removed)), "play");
      if (sent) {
         synchronized (PayloadSubscriptionManager.class) {
            init_LinkedHashSet.removeAll(removed);
         }
      }

      if (TRACE) {
         AutismClientAddon.LOG.info("[Autism Payload] unregistered {} plugin channels: sent={}, sample={}", removed.size(), sent, describeChannels(removed));
      }

      return new PayloadSubscriptionManager.UnregisterResult(removed.size(), sent);
   }

   public static PayloadSubscriptionManager.PatternImpact impactOf(String pattern) {
      String normalized = PayloadChannelFilterManager.normalizePattern(pattern);
      if (normalized.isBlank()) {
         return new PayloadSubscriptionManager.PatternImpact(false, false, 0, List.of());
      }

      LinkedHashSet<String> channels = new LinkedHashSet<>();
      boolean wildcard = normalized.indexOf(42) >= 0;
      if (!wildcard) {
         if (isRegisterable(normalized)) {
            channels.add(normalized);
         }

         return new PayloadSubscriptionManager.PatternImpact(false, !channels.isEmpty(), channels.size(), List.copyOf(channels));
      } else {
         for (PayloadChannelFilterManager.ChannelPreset preset : PayloadChannelFilterManager.presets()) {
            String presetPattern = PayloadChannelFilterManager.normalizePattern(preset.read());
            if (presetPattern.indexOf(42) < 0 && PayloadChannelFilterManager.isRegisterableExactPreset(preset) && matchesPattern(normalized, presetPattern)) {
               channels.add(presetPattern);
            }
         }

         return new PayloadSubscriptionManager.PatternImpact(true, false, channels.size(), List.copyOf(channels));
      }
   }

   public static PayloadSubscriptionManager.PatternProjection init(Collection<String> patterns, boolean packetLoggerCapturing) {
      PayloadSubscriptionManager.RegistrationPlan plan = plan(packetLoggerCapturing);
      LinkedHashSet<String> projected = new LinkedHashSet<>(plan.channels());
      int before = projected.size();
      if (patterns != null) {
         for (String pattern : patterns) {
            PayloadSubscriptionManager.PatternImpact impact = impactOf(pattern);
            projected.addAll(impact.channels());
         }
      }

      int exactAdded = Math.max(0, projected.size() - before);
      int overflow = Math.max(0, projected.size() - plan.init_I());
      return new PayloadSubscriptionManager.PatternProjection(projected.size(), plan.init_I(), exactAdded, overflow);
   }

   private static int configuredLimit() {
      String raw = System.getProperty("autism.payload.subscription.limit");
      if (raw != null && !raw.isBlank()) {
         try {
            return Math.max(1, Math.min(120, Integer.parseInt(raw.trim())));
         } catch (NumberFormatException ignored) {
            return 96;
         }
      } else {
         return 96;
      }
   }

   private static boolean isConnected(Minecraft mc) {
      return mc != null && mc.getConnection() != null && mc.player != null;
   }

   private static byte[] encodeChannels(List<String> channels) {
      StringBuilder builder = new StringBuilder();

      for (String channel : channels) {
         if (isRegisterable(channel)) {
            if (!builder.isEmpty()) {
               builder.append('\u0000');
            }

            builder.append(channel);
         }
      }

      return builder.isEmpty() ? new byte[0] : builder.toString().getBytes(StandardCharsets.US_ASCII);
   }

   private static boolean isRegisterable(String channel) {
      return PayloadRegistrationManager.isExactChannel(channel);
   }

   private static boolean load_Z(String channel) {
      if (TRACE) {
         return false;
      }

      String normalized = normalize(channel);
      return normalized.contains("autismtest")
         || normalized.contains("payloadprobe")
         || normalized.contains("payload_probe")
         || normalized.contains("brandlike")
         || normalized.contains("mc_string")
         || normalized.contains("java_utf");
   }

   private static String normalize(String channel) {
      return channel == null ? "" : channel.trim().toLowerCase(Locale.ROOT);
   }

   private static void moveToEnd(LinkedHashSet<String> set, String channel) {
      if (set.remove(channel)) {
         set.add(channel);
      } else {
         set.add(channel);

         while (set.size() > 64) {
            String first = set.iterator().next();
            set.remove(first);
         }
      }
   }

   private static boolean matchesPattern(String pattern, String text) {
      String normalizedPattern = PayloadChannelFilterManager.normalizePattern(pattern);
      String normalizedText = normalize(text);
      if (!normalizedPattern.isEmpty() && !normalizedText.isEmpty()) {
         if ("*".equals(normalizedPattern)) {
            return true;
         }

         if (normalizedPattern.indexOf(42) < 0) {
            return normalizedText.equals(normalizedPattern);
         }

         int p = 0;
         int t = 0;
         int star = -1;
         int mark = 0;

         while (t < normalizedText.length()) {
            if (p < normalizedPattern.length() && normalizedPattern.charAt(p) == normalizedText.charAt(t)) {
               p++;
               t++;
            } else if (p < normalizedPattern.length() && normalizedPattern.charAt(p) == '*') {
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

         while (p < normalizedPattern.length() && normalizedPattern.charAt(p) == '*') {
            p++;
         }

         return p == normalizedPattern.length();
      } else {
         return false;
      }
   }

   private static String describeChannels(Set<String> channels) {
      if (channels != null && !channels.isEmpty()) {
         List<String> values = new ArrayList<>(channels);
         int count = Math.min(8, values.size());
         return String.join(", ", values.subList(0, count)) + (values.size() > count ? ", ..." : "");
      } else {
         return "";
      }
   }

   public record RegistrationStatus(
      int registered, int limit, int read, boolean sent, long init_J, List<String> applied, List<String> load_List, int write, boolean locked
   ) {
      private static PayloadSubscriptionManager.RegistrationStatus idle(int limit) {
         return new PayloadSubscriptionManager.RegistrationStatus(0, limit, 0, false, 0L, List.of(), List.of(), 0, false);
      }

      private static PayloadSubscriptionManager.RegistrationStatus idle(int limit, int lastUnregisteredCount) {
         return new PayloadSubscriptionManager.RegistrationStatus(0, limit, 0, false, System.currentTimeMillis(), List.of(), List.of(), Math.max(0, lastUnregisteredCount), true);
      }

      public String describe() {
         if (this.locked) {
            return this.write > 0 ? "Registration locked -" + this.write : "Registration locked";
         }

         String base = "Registered " + this.registered + "/" + this.limit;
         if (this.write > 0) {
            base = base + " -" + this.write;
         }

         return this.read > 0 ? base + " +" + this.read : base;
      }

      public String appliedSummary() {
         if (this.applied != null && !this.applied.isEmpty()) {
            int count = Math.min(8, this.applied.size());
            return String.join(", ", this.applied.subList(0, count)) + (this.applied.size() > count ? ", ..." : "");
         } else {
            return "";
         }
      }
   }

   public record PatternImpact(boolean init_Z, boolean load, int channelCount, List<String> channels) {
      public String label() {
         return this.load ? "1ch" : this.channelCount + "ch";
      }
   }

   public record RegistrationPlan(List<String> channels, int init_I, int load_I, List<String> load_List) {
   }

   private static final class PlanCollector {
      private final int limit;
      private final LinkedHashMap<String, String> accepted = new LinkedHashMap<>();
      private final LinkedHashSet<String> overflow = new LinkedHashSet<>();

      private PlanCollector(int limit) {
         this.limit = limit;
      }

      private void offer(String channel) {
         String normalized = PayloadSubscriptionManager.normalize(channel);
         if (PayloadSubscriptionManager.isRegisterable(normalized) && !this.accepted.containsKey(normalized)) {
            if (this.accepted.size() >= this.limit) {
               this.overflow.add(normalized);
            } else {
               this.accepted.put(normalized, normalized);
            }
         }
      }

      private PayloadSubscriptionManager.RegistrationPlan build() {
         return new PayloadSubscriptionManager.RegistrationPlan(
            List.copyOf(this.accepted.values()), this.limit, this.overflow.size(), List.copyOf(this.overflow)
         );
      }
   }

   public record PatternProjection(int init, int load, int read, int write) {
      public boolean init_Z() {
         return this.write > 0;
      }

      public int init_I() {
         return this.init;
      }
   }

   private record UnregisterResult(int init_I, boolean init_Z) {
   }
}
