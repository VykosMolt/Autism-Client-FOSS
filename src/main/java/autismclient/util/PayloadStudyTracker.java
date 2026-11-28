package autismclient.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class PayloadStudyTracker {
   private static final int init_I = 512;
   private static final int load_I = 6;
   private static volatile boolean studying;
   private static String currentLabel = "";
   private static long studyStartedAt;
   private static final List<String> init_List = new ArrayList<>();
   private static final LinkedHashSet<String> ignoredChannels = new LinkedHashSet<>();
   private static final LinkedHashMap<String, PayloadStudyTracker.ChannelStats> statsByChannel = new LinkedHashMap<>();
   private static final LinkedHashSet<String> load_LinkedHashSet = new LinkedHashSet<>();
   private static final LinkedHashSet<String> seenFingerprints = new LinkedHashSet<>();
   private static int payloadCount;
   private static int write;
   private static int update;

   private PayloadStudyTracker() {
   }

   public static synchronized void startStudy(String studyLabel, Collection<String> studyPatterns, Collection<String> registeredNow) {
      studying = true;
      currentLabel = studyLabel != null && !studyLabel.isBlank() ? studyLabel.trim() : "Payloads";
      studyStartedAt = System.currentTimeMillis();
      init_List.clear();
      ignoredChannels.clear();
      statsByChannel.clear();
      load_LinkedHashSet.clear();
      seenFingerprints.clear();
      payloadCount = 0;
      write = 0;
      update = 0;
      if (studyPatterns != null) {
         for (String pattern : studyPatterns) {
            String normalized = PayloadChannelFilterManager.normalizePattern(pattern);
            if (!normalized.isBlank() && !init_List.contains(normalized)) {
               init_List.add(normalized);
            }
         }
      }

      if (registeredNow != null) {
         for (String channel : registeredNow) {
            String normalized = PayloadRegistrationManager.normalize(channel);
            if (PayloadRegistrationManager.isExactChannel(normalized)) {
               ignoredChannels.add(normalized);
            }
         }
      }

      PacketCaptureState.write();
   }

   public static boolean isActive() {
      return studying;
   }

   public static synchronized String title() {
      return studying ? "Payload Study: " + currentLabel : "";
   }

   public static synchronized String statusLine() {
      return !studying ? "" : payloadCount + " payloads | " + statsByChannel.size() + " channels | ESC to finish";
   }

   public static synchronized String studyStatus() {
      if (!studying) {
         return "";
      }

      if (statsByChannel.isEmpty()) {
         return "Interact with the plugin GUI/commands now.";
      }

      List<PayloadStudyTracker.ChannelStats> sorted = new ArrayList<>(statsByChannel.values());
      sorted.sort((a, b) -> Integer.compare(b.count, a.count));
      StringBuilder sb = new StringBuilder("Top: ");
      int shown = 0;

      for (PayloadStudyTracker.ChannelStats stats : sorted) {
         if (shown >= 3) {
            break;
         }

         if (shown > 0) {
            sb.append(", ");
         }

         sb.append(stats.channel).append(" x").append(stats.count);
         shown++;
      }

      if (sorted.size() > shown) {
         sb.append(" +").append(sorted.size() - shown);
      }

      return sb.toString();
   }

   public static synchronized void reset() {
      studying = false;
      currentLabel = "";
      studyStartedAt = 0L;
      init_List.clear();
      ignoredChannels.clear();
      statsByChannel.clear();
      load_LinkedHashSet.clear();
      seenFingerprints.clear();
      payloadCount = 0;
      write = 0;
      update = 0;
      PacketCaptureState.write();
   }

   public static synchronized boolean recordPayload(AutismPayloadSupport.PayloadEntry snapshot) {
      if (studying && snapshot != null) {
         String channel = PayloadRegistrationManager.normalize(snapshot.channel());
         if (channel.isBlank()) {
            return false;
         }

         String fingerprint = fingerprint(snapshot);
         if (!fingerprint.isBlank()) {
            if (!seenFingerprints.add(fingerprint)) {
               return false;
            }

            while (seenFingerprints.size() > 512) {
               String first = seenFingerprints.iterator().next();
               seenFingerprints.remove(first);
            }
         }

         PayloadSubscriptionManager.observe(snapshot);
         payloadCount++;
         PayloadStudyTracker.ChannelStats stats = statsByChannel.computeIfAbsent(channel, PayloadStudyTracker.ChannelStats::new);
         stats.count++;
         stats.init_J = stats.init_J + Math.max(0, snapshot.byteLength());
         String direction = snapshot.direction() == null ? "" : snapshot.direction().trim().toUpperCase(Locale.ROOT);
         if ("C2S".equals(direction)) {
            stats.c2sCount++;
         } else if ("S2C".equals(direction)) {
            stats.s2cCount++;
         }

         boolean matched = init_List.isEmpty();

         for (String pattern : init_List) {
            if (PayloadChannelFilterManager.matchesPattern(pattern, channel)) {
               stats.patterns.add(pattern);
               matched = true;
            }
         }

         if (matched) {
            write++;
         }

         List<String> registerChannels = AutismPayloadSupport.extractRegisteredChannels(channel, snapshot.rawBytes());
         if (!registerChannels.isEmpty()) {
            update++;

            for (String hinted : registerChannels) {
               String normalized = PayloadRegistrationManager.normalize(hinted);
               if (PayloadRegistrationManager.isExactChannel(normalized)) {
                  load_LinkedHashSet.add(normalized);
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static synchronized boolean finish() {
      if (!studying) {
         return false;
      }

      PayloadStudyTracker.StudySummary summary = init_bI_StreamUtil();
      if (summary.payloads() == 0) {
         bi.read("Payload study finished: no payloads captured.");
         return true;
      }

      bi.init("Payload study: " + summary.payloads() + " payloads, " + summary.channels() + " channels.");
      if (!summary.load_Str().isBlank()) {
         bi.init(summary.load_Str(), -7346000);
      }

      if (summary.update() > 0) {
         bi.read("Learned " + summary.update() + " exact channels. Open Channels -> Capture.");
      }

      return true;
   }

   public static synchronized PayloadStudyTracker.StudySummary init_bI_StreamUtil() {
      long durationMs = studying ? Math.max(0L, System.currentTimeMillis() - studyStartedAt) : 0L;
      studying = false;
      List<PayloadStudyTracker.ChannelStats> sorted = new ArrayList<>(statsByChannel.values());
      sorted.sort((a, b) -> {
         int count = Integer.compare(b.count, a.count);
         return count != 0 ? count : a.channel.compareToIgnoreCase(b.channel);
      });
      StringBuilder top = new StringBuilder();
      int shown = 0;

      for (PayloadStudyTracker.ChannelStats stats : sorted) {
         if (shown >= 6) {
            break;
         }

         if (!top.isEmpty()) {
            top.append(", ");
         }

         top.append(stats.channel).append(" x").append(stats.count);
         shown++;
      }

      if (sorted.size() > shown) {
         top.append(" +").append(sorted.size() - shown);
      }

      int learned = collectLearned().size();
      PayloadStudyTracker.StudySummary summary = new PayloadStudyTracker.StudySummary(currentLabel, durationMs, payloadCount, statsByChannel.size(), write, update, learned, top.toString());
      PacketCaptureState.write();
      return summary;
   }

   public static synchronized List<String> learnedChannels() {
      return List.copyOf(collectLearned());
   }

   private static LinkedHashSet<String> collectLearned() {
      LinkedHashSet<String> learned = new LinkedHashSet<>();
      learned.addAll(statsByChannel.keySet());
      learned.addAll(load_LinkedHashSet);
      learned.removeAll(ignoredChannels);
      return learned;
   }

   private static String fingerprint(AutismPayloadSupport.PayloadEntry snapshot) {
      byte[] bytes = snapshot.rawBytes();
      return (snapshot.direction() == null ? "" : snapshot.direction())
         + "|"
         + (snapshot.protocolPhase() == null ? "" : snapshot.protocolPhase())
         + "|"
         + snapshot.packetId()
         + "|"
         + (snapshot.channel() == null ? "" : snapshot.channel())
         + "|"
         + snapshot.byteLength()
         + "|"
         + Arrays.hashCode(bytes);
   }

   private static final class ChannelStats {
      private final String channel;
      private final Set<String> patterns = new LinkedHashSet<>();
      private int count;
      private int c2sCount;
      private int s2cCount;
      private long init_J;

      private ChannelStats(String channel) {
         this.channel = channel;
      }
   }

   public record StudySummary(String label, long durationMs, int payloads, int channels, int read, int write, int update, String load_Str) {
   }
}
