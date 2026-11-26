package autismclient.util;

public final class OverlayWindowSizeStore {
   private final String init;

   public OverlayWindowSizeStore(String key) {
      this.init = key;
   }

   public boolean init_Z() {
      AutismConfig.WindowSize s = this.init_AutismConfig_Windo();
      return s != null && (s.width > 0 || s.height > 0);
   }

   public int init(int auto, int min, int max) {
      AutismConfig.WindowSize s = this.init_AutismConfig_Windo();
      return s != null && s.width > 0 ? read(s.width, min, max) : auto;
   }

   public int load(int auto, int min, int max) {
      AutismConfig.WindowSize s = this.init_AutismConfig_Windo();
      return s != null && s.height > 0 ? read(s.height, min, max) : auto;
   }

   public void init(int width, int height) {
      try {
         AutismConfig.WindowSize s = AutismConfig.getGlobal().windowSizes.computeIfAbsent(this.init, k -> new AutismConfig.WindowSize());
         s.width = Math.max(0, width);
         s.height = Math.max(0, height);
      } catch (Throwable var4) {
      }
   }

   public void init_V() {
      try {
         AutismConfig.getGlobal().save();
      } catch (Throwable var2) {
      }
   }

   public void load() {
      try {
         if (AutismConfig.getGlobal().windowSizes.remove(this.init) != null) {
            AutismConfig.getGlobal().save();
         }
      } catch (Throwable var2) {
      }
   }

   private AutismConfig.WindowSize init_AutismConfig_Windo() {
      try {
         return AutismConfig.getGlobal().windowSizes.get(this.init);
      } catch (Throwable t) {
         return null;
      }
   }

   static int read(int value, int min, int max) {
      return Math.max(min, Math.min(value, Math.max(min, max)));
   }
}
