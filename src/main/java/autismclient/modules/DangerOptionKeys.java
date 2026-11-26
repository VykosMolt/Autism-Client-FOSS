package autismclient.modules;

import java.util.HashMap;
import java.util.Map;

final class DangerOptionKeys {
   private DangerOptionKeys() {
   }

   static Map<String, String> init(Map<String, String> saved) {
      Map<String, String> result = new HashMap<>();

      for (IncomingProjectileScanner.Inner_StreamUtil kind : IncomingProjectileScanner.Inner_StreamUtil.values()) {
         for (String option : new String[]{"hud", "aim-preview"}) {
            String key = kind.init_Str() + "-" + option;
            if (!saved.containsKey(key) && saved.containsKey(option)) {
               result.put(key, saved.get(option));
            }
         }

         String trajectory = kind.init_Str() + "-trajectory";
         if (!saved.containsKey(trajectory)
            && (
               saved.containsKey(kind.init_Str() + "-trajectories")
                  || saved.containsKey(kind.init_Str() + "-landings")
                  || saved.containsKey("trajectories")
                  || saved.containsKey("landings")
            )) {
            result.put(
               trajectory,
               Boolean.toString(init(saved, kind.init_Str() + "-trajectories", "trajectories") || init(saved, kind.init_Str() + "-landings", "landings"))
            );
         }

         String key = kind.init_Str() + "-radius";
         if (!saved.containsKey(key) && saved.containsKey("relevance-radius")) {
            try {
               double radius = Double.parseDouble(saved.get("relevance-radius"));
               if (Double.isFinite(radius)) {
                  result.put(key, Double.toString(Math.clamp(radius, 1.0, 30.0) * (kind == IncomingProjectileScanner.Inner_StreamUtil.PEARL ? 2 : 1)));
               }
            } catch (NumberFormatException var11) {
            }
         }
      }

      return result;
   }

   private static boolean init(Map<String, String> saved, String key, String shared) {
      String value = saved.getOrDefault(key, saved.getOrDefault(shared, "true"));
      return value != null && Boolean.parseBoolean(value.trim());
   }
}
