package autismclient.modules;

import autismclient.util.AutismLiteVariant;
import java.util.Locale;
import java.util.Map;

public final class GifOverlayState {
   private static volatile GifOverlayState.OverlaySnapshot current = GifOverlayState.OverlaySnapshot.DEFAULTS;

   private GifOverlayState() {
   }

   static void setSnapshot(GifOverlayState.OverlaySnapshot snapshot) {
      current = snapshot == null ? GifOverlayState.OverlaySnapshot.DEFAULTS : snapshot;
   }

   private static boolean renderingAllowed() {
      return AutismLiteVariant.enabled() || autismclient.e.RenderState.init();
   }

   public static boolean isCapeActive() {
      GifOverlayState.OverlaySnapshot s = current;
      return s.enabled() && s.capeEnabled() && (!s.capePath().isBlank() || !s.capeOverrides().isEmpty()) && renderingAllowed();
   }

   public static boolean isShieldActive() {
      GifOverlayState.OverlaySnapshot s = current;
      return s.enabled() && s.read_Z() && (!s.load_Str().isBlank() || !s.load_Map().isEmpty()) && renderingAllowed();
   }

   public static String capePath(String playerName) {
      return overrideFor(current, playerName, true);
   }

   public static String shieldPath(String playerName) {
      return overrideFor(current, playerName, false);
   }

   private static String overrideFor(GifOverlayState.OverlaySnapshot s, String playerName, boolean cape) {
      if (playerName != null && !playerName.isBlank()) {
         if (s.enabled() && (cape ? s.capeEnabled() : s.read_Z()) && renderingAllowed()) {
            String custom = (cape ? s.capeOverrides() : s.load_Map()).get(playerName.trim().toLowerCase(Locale.ROOT));
            if (custom != null && !custom.isBlank()) {
               return custom;
            } else {
               String path = cape ? s.capePath() : s.load_Str();
               if (path.isBlank()) {
                  return null;
               } else {
                  return targetMatches(cape ? s.read_Str() : s.write_Str(), playerName, s.update_Str()) ? path : null;
               }
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static boolean targetMatches(String target, String playerName, String selfName) {
      if ("Everyone".equalsIgnoreCase(target)) {
         return true;
      }

      boolean isSelf = selfName != null && !selfName.isBlank() && selfName.equalsIgnoreCase(playerName);
      return "Others".equalsIgnoreCase(target) ? !isSelf : isSelf;
   }

   public static double capeFps() {
      return current.init_D();
   }

   public static double shieldFps() {
      return current.load_D();
   }

   public static boolean capeKeepAspect() {
      return current.write_Z();
   }

   public static boolean shieldKeepAspect() {
      return current.update_Z();
   }

   public static double capeQuality() {
      return current.read_D();
   }

   public static double shieldQuality() {
      return current.write_D();
   }

   public static String targetName() {
      return current.update_Str();
   }

   record OverlaySnapshot(
      boolean enabled,
      boolean capeEnabled,
      boolean read_Z,
      String capePath,
      String load_Str,
      String read_Str,
      String write_Str,
      double init_D,
      double load_D,
      boolean write_Z,
      boolean update_Z,
      double read_D,
      double write_D,
      Map<String, String> capeOverrides,
      Map<String, String> load_Map,
      String update_Str
   ) {
      private static final GifOverlayState.OverlaySnapshot DEFAULTS = new GifOverlayState.OverlaySnapshot(
         false, false, false, "", "", "Self", "Self", 0.0, 0.0, false, false, 1.0, 1.0, Map.of(), Map.of(), ""
      );
   }
}
