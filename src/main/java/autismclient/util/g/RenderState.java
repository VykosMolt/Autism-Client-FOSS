package autismclient.util.g;

import autismclient.util.AutismConfig;
import java.util.regex.Pattern;

public final class RenderState {
   public static final int init_I = 0;
   public static final int load = 10000;
   public static final int read = 100;
   public static final int write = 4;
   private static final Pattern init_Pattern = Pattern.compile("\\d{0,2}(\\.\\d?)?");

   private RenderState() {
   }

   public static int init_I() {
      return init_I_I(AutismConfig.getGlobal().multiMacroStartDelayMs);
   }

   public static void init_V_I(int ms) {
      AutismConfig.getGlobal().multiMacroStartDelayMs = init_I_I(ms);
   }

   public static void init_V() {
      AutismConfig.getGlobal().save();
   }

   public static int init_I_I(int ms) {
      int bounded = Math.max(0, Math.min(10000, ms));
      return Math.round(bounded / 100.0F) * 100;
   }

   public static double init_D_I(int ms) {
      return init_I_I(ms) / 10000.0;
   }

   public static int init_I_D(double mouseX, int trackX, int trackWidth) {
      if (trackWidth <= 0) {
         return 0;
      }

      double ratio = Math.max(0.0, Math.min(1.0, (mouseX - trackX) / trackWidth));
      return init_I_I((int)Math.round(ratio * 10000.0));
   }

   public static int init(int ms, int direction) {
      return init_I_I(init_I_I(ms) + Integer.signum(direction) * 100);
   }

   public static void load_V(int direction) {
      if (direction != 0) {
         init_V_I(init(init_I(), direction));
         init_V();
      }
   }

   public static boolean init_Z_Str(String text) {
      return text != null && init_Pattern.matcher(text).matches();
   }

   public static String init_Str_I(int ms) {
      int value = init_I_I(ms);
      return value % 1000 == 0 ? Integer.toString(value / 1000) : value / 1000 + "." + value % 1000 / 100;
   }

   public static int init(String typed, int fallback) {
      double seconds;
      try {
         seconds = Double.parseDouble(typed == null ? "" : typed);
      } catch (NumberFormatException e) {
         return init_I_I(fallback);
      }

      if (Double.isNaN(seconds)) {
         return init_I_I(fallback);
      }

      double bounded = Math.max(0.0, Math.min(10.0, seconds));
      return init_I_I((int)Math.round(bounded * 1000.0));
   }

   public static String load_Str(int ms) {
      int value = init_I_I(ms);
      if (value <= 0) {
         return "Off";
      } else {
         return value % 1000 == 0 ? value / 1000 + "s" : value / 1000 + "." + value % 1000 / 100 + "s";
      }
   }

   public static String init_Str_J(long remainingMs) {
      long tenths = Math.max(0L, (remainingMs + 99L) / 100L);
      return tenths / 10L + "." + tenths % 10L + "s";
   }

   public static long init_J_J(long launchedAt, int index, int gapMs) {
      return launchedAt + (long)Math.max(0, index) * Math.max(0, gapMs);
   }
}
