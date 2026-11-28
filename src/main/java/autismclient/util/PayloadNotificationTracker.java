package autismclient.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public final class PayloadNotificationTracker {
   private static final long init_J = 2000L;
   private static final long load = 30000L;
   private static final int init_I = -14249;
   private static final Map<String, PayloadNotificationTracker.Inner_DataUtil> init_Map = new LinkedHashMap<>();
   private static volatile boolean init_Z;

   private PayloadNotificationTracker() {
   }

   public static synchronized void init(String channel, String direction, PayloadChannelFilterManager.MatchedRule match) {
      if ("S2C".equalsIgnoreCase(direction) && !autismclient.modules.aD.init_Z()) {
         String normalized = init(channel);
         if (!normalized.isBlank()) {
            long now = System.currentTimeMillis();
            PayloadNotificationTracker.Inner_DataUtil state = init_Map.computeIfAbsent(normalized, unused -> new PayloadNotificationTracker.Inner_DataUtil());
            init_Z = true;
            state.init_Str = normalized;
            state.load_Str = init(normalized, match);
            state.load_J = now;
            if (now >= state.init_J) {
               if (state.init_I > 0) {
                  state.init_I++;
                  init(state);
               } else {
                  bi.init("Payload: " + normalized, -14249);
               }

               state.init_I = 0;
               state.init_J = now + 2000L;
            } else {
               state.init_I++;
            }
         }
      }
   }

   public static void init() {
      if (init_Z) {
         synchronized (PayloadNotificationTracker.class) {
            if (init_Map.isEmpty()) {
               init_Z = false;
            } else if (autismclient.modules.aD.read_Z()) {
               init_Map.clear();
               init_Z = false;
            } else {
               long now = System.currentTimeMillis();
               Iterator<Entry<String, PayloadNotificationTracker.Inner_DataUtil>> it = init_Map.entrySet().iterator();

               while (it.hasNext()) {
                  PayloadNotificationTracker.Inner_DataUtil state = it.next().getValue();
                  if (state.init_I > 0 && now >= state.init_J) {
                     init(state);
                     state.init_I = 0;
                     state.init_J = now + 2000L;
                  }

                  if (state.init_I == 0 && now - state.load_J > 30000L) {
                     it.remove();
                  }
               }

               init_Z = !init_Map.isEmpty();
            }
         }
      }
   }

   public static synchronized void load() {
      init_Map.clear();
      init_Z = false;
   }

   private static void init(PayloadNotificationTracker.Inner_DataUtil state) {
      String label = state.load_Str != null && !state.load_Str.isBlank() ? state.load_Str : state.init_Str;
      bi.init("Payload: " + label + " +" + state.init_I, -14249);
   }

   private static String init(String channel, PayloadChannelFilterManager.MatchedRule match) {
      if (match != null && match.pattern() != null && !match.pattern().isBlank()) {
         String pattern = match.pattern().trim().toLowerCase(Locale.ROOT);
         return pattern.contains("*") ? pattern : channel;
      } else {
         return channel;
      }
   }

   private static String init(String channel) {
      return channel == null ? "" : channel.trim().toLowerCase(Locale.ROOT);
   }

   private static final class Inner_DataUtil {
      String init_Str = "";
      String load_Str = "";
      long init_J;
      long load_J;
      int init_I;
   }
}
