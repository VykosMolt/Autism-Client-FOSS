package autismclient.util;

public final class Q {
   private Q() {
   }

   public static void init(AutismConfig config) {
   }

   public static void load(AutismConfig config) {
      if (config != null && config.essentialHiddenByPanic) {
         config.essentialHiddenByPanic = false;
         config.save();
      }
   }

   public static void read(AutismConfig config) {
      if (config != null && config.essentialHiddenByPanic) {
         config.essentialHiddenByPanic = false;
      }
   }
}
