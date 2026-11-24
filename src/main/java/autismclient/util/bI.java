package autismclient.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class bI {
   private static final int init_I = 512;
   private static final int load_I = 6;
   private static volatile boolean init_Z;
   private static String init_Str = "";
   private static long init_J;
   private static final List<String> init_List = new ArrayList<>();
   private static final LinkedHashSet<String> init_LinkedHashSet = new LinkedHashSet<>();
   private static final LinkedHashMap<String, bI.Inner_DataUtil> init_LinkedHashMap = new LinkedHashMap<>();
   private static final LinkedHashSet<String> load_LinkedHashSet = new LinkedHashSet<>();
   private static final LinkedHashSet<String> read_LinkedHashSet = new LinkedHashSet<>();
   private static int read_I;
   private static int write;
   private static int update;

   private bI() {
   }

   public static synchronized void init(String studyLabel, Collection<String> studyPatterns, Collection<String> registeredNow) {
      init_Z = true;
      init_Str = studyLabel != null && !studyLabel.isBlank() ? studyLabel.trim() : "Payloads";
      init_J = System.currentTimeMillis();
      init_List.clear();
      init_LinkedHashSet.clear();
      init_LinkedHashMap.clear();
      load_LinkedHashSet.clear();
      read_LinkedHashSet.clear();
      read_I = 0;
      write = 0;
      update = 0;
      if (studyPatterns != null) {
         for (String pattern : studyPatterns) {
            String normalized = bB.init_Str_Str(pattern);
            if (!normalized.isBlank() && !init_List.contains(normalized)) {
               init_List.add(normalized);
            }
         }
      }

      if (registeredNow != null) {
         for (String channel : registeredNow) {
            String normalized = bC.init_Str_Str(channel);
            if (bC.load_Z_Str(normalized)) {
               init_LinkedHashSet.add(normalized);
            }
         }
      }

      bg.write();
   }

   public static boolean init_Z() {
      return init_Z;
   }

   public static synchronized String init_Str() {
      return init_Z ? "Payload Study: " + init_Str : "";
   }

   public static synchronized String load_Str() {
      return !init_Z ? "" : read_I + " payloads | " + init_LinkedHashMap.size() + " channels | ESC to finish";
   }

   public static synchronized String read() {
      if (!init_Z) {
         return "";
      }

      if (init_LinkedHashMap.isEmpty()) {
         return "Interact with the plugin GUI/commands now.";
      }

      List<bI.Inner_DataUtil> sorted = new ArrayList<>(init_LinkedHashMap.values());
      sorted.sort((a, b) -> Integer.compare(b.init_I, a.init_I));
      StringBuilder sb = new StringBuilder("Top: ");
      int shown = 0;

      for (bI.Inner_DataUtil stats : sorted) {
         if (shown >= 3) {
            break;
         }

         if (shown > 0) {
            sb.append(", ");
         }

         sb.append(stats.init_Str).append(" x").append(stats.init_I);
         shown++;
      }

      if (sorted.size() > shown) {
         sb.append(" +").append(sorted.size() - shown);
      }

      return sb.toString();
   }

   public static synchronized void init_V() {
      init_Z = false;
      init_Str = "";
      init_J = 0L;
      init_List.clear();
      init_LinkedHashSet.clear();
      init_LinkedHashMap.clear();
      load_LinkedHashSet.clear();
      read_LinkedHashSet.clear();
      read_I = 0;
      write = 0;
      update = 0;
      bg.write();
   }

   public static synchronized boolean init_Z(AutismPayloadSupport.Inner_StreamUtil snapshot) {
      if (init_Z && snapshot != null) {
         String channel = bC.init_Str_Str(snapshot.load_Str());
         if (channel.isBlank()) {
            return false;
         }

         String fingerprint = init_Str(snapshot);
         if (!fingerprint.isBlank()) {
            if (!read_LinkedHashSet.add(fingerprint)) {
               return false;
            }

            while (read_LinkedHashSet.size() > 512) {
               String first = read_LinkedHashSet.iterator().next();
               read_LinkedHashSet.remove(first);
            }
         }

         bD.init(snapshot);
         read_I++;
         bI.Inner_DataUtil stats = init_LinkedHashMap.computeIfAbsent(channel, bI.Inner_DataUtil::new);
         stats.init_I++;
         stats.init_J = stats.init_J + Math.max(0, snapshot.init_I());
         String direction = snapshot.init_Str() == null ? "" : snapshot.init_Str().trim().toUpperCase(Locale.ROOT);
         if ("C2S".equals(direction)) {
            stats.load++;
         } else if ("S2C".equals(direction)) {
            stats.read++;
         }

         boolean matched = init_List.isEmpty();

         for (String pattern : init_List) {
            if (bB.init_Z_Str_Str(pattern, channel)) {
               stats.init_Set.add(pattern);
               matched = true;
            }
         }

         if (matched) {
            write++;
         }

         List<String> registerChannels = AutismPayloadSupport.init_List(channel, snapshot.init_BArr());
         if (!registerChannels.isEmpty()) {
            update++;

            for (String hinted : registerChannels) {
               String normalized = bC.init_Str_Str(hinted);
               if (bC.load_Z_Str(normalized)) {
                  load_LinkedHashSet.add(normalized);
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static synchronized boolean load_Z() {
      if (!init_Z) {
         return false;
      }

      bI.Inner_StreamUtil summary = init_bI_StreamUtil();
      if (summary.init_I() == 0) {
         bi.read("Payload study finished: no payloads captured.");
         return true;
      }

      bi.init("Payload study: " + summary.init_I() + " payloads, " + summary.load_I() + " channels.");
      if (!summary.load_Str().isBlank()) {
         bi.init(summary.load_Str(), -7346000);
      }

      if (summary.update() > 0) {
         bi.read("Learned " + summary.update() + " exact channels. Open Channels -> Capture.");
      }

      return true;
   }

   public static synchronized bI.Inner_StreamUtil init_bI_StreamUtil() {
      long durationMs = init_Z ? Math.max(0L, System.currentTimeMillis() - init_J) : 0L;
      init_Z = false;
      List<bI.Inner_DataUtil> sorted = new ArrayList<>(init_LinkedHashMap.values());
      sorted.sort((a, b) -> {
         int count = Integer.compare(b.init_I, a.init_I);
         return count != 0 ? count : a.init_Str.compareToIgnoreCase(b.init_Str);
      });
      StringBuilder top = new StringBuilder();
      int shown = 0;

      for (bI.Inner_DataUtil stats : sorted) {
         if (shown >= 6) {
            break;
         }

         if (!top.isEmpty()) {
            top.append(", ");
         }

         top.append(stats.init_Str).append(" x").append(stats.init_I);
         shown++;
      }

      if (sorted.size() > shown) {
         top.append(" +").append(sorted.size() - shown);
      }

      int learned = init_LinkedHashSet().size();
      bI.Inner_StreamUtil summary = new bI.Inner_StreamUtil(init_Str, durationMs, read_I, init_LinkedHashMap.size(), write, update, learned, top.toString());
      bg.write();
      return summary;
   }

   public static synchronized List<String> init_List() {
      return List.copyOf(init_LinkedHashSet());
   }

   private static LinkedHashSet<String> init_LinkedHashSet() {
      LinkedHashSet<String> learned = new LinkedHashSet<>();
      learned.addAll(init_LinkedHashMap.keySet());
      learned.addAll(load_LinkedHashSet);
      learned.removeAll(init_LinkedHashSet);
      return learned;
   }

   private static String init_Str(AutismPayloadSupport.Inner_StreamUtil snapshot) {
      byte[] bytes = snapshot.init_BArr();
      return (snapshot.init_Str() == null ? "" : snapshot.init_Str())
         + "|"
         + (snapshot.update() == null ? "" : snapshot.update())
         + "|"
         + snapshot.load_I()
         + "|"
         + (snapshot.load_Str() == null ? "" : snapshot.load_Str())
         + "|"
         + snapshot.init_I()
         + "|"
         + Arrays.hashCode(bytes);
   }

   private static final class Inner_DataUtil {
      private final String init_Str;
      private final Set<String> init_Set = new LinkedHashSet<>();
      private int init_I;
      private int load;
      private int read;
      private long init_J;

      private Inner_DataUtil(String channel) {
         this.init_Str = channel;
      }
   }

   public record Inner_StreamUtil(String init_Str, long init_J, int init_I, int load_I, int read, int write, int update, String load_Str) {
   }
}
