package autismclient.modules;

import autismclient.util.AutismLiteVariant;
import java.util.Locale;
import java.util.Map;

public final class GifOverlayState {
   private static volatile GifOverlayState.Inner_DataUtil init = GifOverlayState.Inner_DataUtil.init_L_DataUtil;

   private GifOverlayState() {
   }

   static void init(GifOverlayState.Inner_DataUtil snapshot) {
      init = snapshot == null ? GifOverlayState.Inner_DataUtil.init_L_DataUtil : snapshot;
   }

   private static boolean update() {
      return AutismLiteVariant.enabled() || autismclient.e.RenderState.init();
   }

   public static boolean init_Z() {
      GifOverlayState.Inner_DataUtil s = init;
      return s.init_Z() && s.load_Z() && (!s.init_Str().isBlank() || !s.init_Map().isEmpty()) && update();
   }

   public static boolean load_Z() {
      GifOverlayState.Inner_DataUtil s = init;
      return s.init_Z() && s.read_Z() && (!s.load_Str().isBlank() || !s.load_Map().isEmpty()) && update();
   }

   public static String init(String playerName) {
      return init(init, playerName, true);
   }

   public static String load(String playerName) {
      return init(init, playerName, false);
   }

   private static String init(GifOverlayState.Inner_DataUtil s, String playerName, boolean cape) {
      if (playerName != null && !playerName.isBlank()) {
         if (s.init_Z() && (cape ? s.load_Z() : s.read_Z()) && update()) {
            String custom = (cape ? s.init_Map() : s.load_Map()).get(playerName.trim().toLowerCase(Locale.ROOT));
            if (custom != null && !custom.isBlank()) {
               return custom;
            } else {
               String path = cape ? s.init_Str() : s.load_Str();
               if (path.isBlank()) {
                  return null;
               } else {
                  return init(cape ? s.read_Str() : s.write_Str(), playerName, s.update_Str()) ? path : null;
               }
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static boolean init(String target, String playerName, String selfName) {
      if ("Everyone".equalsIgnoreCase(target)) {
         return true;
      }

      boolean isSelf = selfName != null && !selfName.isBlank() && selfName.equalsIgnoreCase(playerName);
      return "Others".equalsIgnoreCase(target) ? !isSelf : isSelf;
   }

   public static double init_D() {
      return init.init_D();
   }

   public static double load_D() {
      return init.load_D();
   }

   public static boolean read_Z() {
      return init.write_Z();
   }

   public static boolean write_Z() {
      return init.update_Z();
   }

   public static double read_D() {
      return init.read_D();
   }

   public static double write_D() {
      return init.write_D();
   }

   public static String init_Str() {
      return init.update_Str();
   }

   record Inner_DataUtil(
      boolean init_Z,
      boolean load_Z,
      boolean read_Z,
      String init_Str,
      String load_Str,
      String read_Str,
      String write_Str,
      double init_D,
      double load_D,
      boolean write_Z,
      boolean update_Z,
      double read_D,
      double write_D,
      Map<String, String> init_Map,
      Map<String, String> load_Map,
      String update_Str
   ) {
      private static final GifOverlayState.Inner_DataUtil init_L_DataUtil = new GifOverlayState.Inner_DataUtil(
         false, false, false, "", "", "Self", "Self", 0.0, 0.0, false, false, 1.0, 1.0, Map.of(), Map.of(), ""
      );
   }
}
