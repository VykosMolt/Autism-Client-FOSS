package autismclient.util;

public final class OverlayWindowSizeStore {
   private final String key;

   public OverlayWindowSizeStore(String key) {
      this.key = key;
   }

   public boolean hasSavedSize() {
      AutismConfig.WindowSize s = this.storedSize();
      return s != null && (s.width > 0 || s.height > 0);
   }

   public int width(int auto, int min, int max) {
      AutismConfig.WindowSize s = this.storedSize();
      return s != null && s.width > 0 ? clamp(s.width, min, max) : auto;
   }

   public int height(int auto, int min, int max) {
      AutismConfig.WindowSize s = this.storedSize();
      return s != null && s.height > 0 ? clamp(s.height, min, max) : auto;
   }

   public void save(int width, int height) {
      try {
         AutismConfig.WindowSize s = AutismConfig.getGlobal().windowSizes.computeIfAbsent(this.key, k -> new AutismConfig.WindowSize());
         s.width = Math.max(0, width);
         s.height = Math.max(0, height);
      } catch (Throwable var4) {
      }
   }

   public void save() {
      try {
         AutismConfig.getGlobal().save();
      } catch (Throwable var2) {
      }
   }

   public void clear() {
      try {
         if (AutismConfig.getGlobal().windowSizes.remove(this.key) != null) {
            AutismConfig.getGlobal().save();
         }
      } catch (Throwable var2) {
      }
   }

   private AutismConfig.WindowSize storedSize() {
      try {
         return AutismConfig.getGlobal().windowSizes.get(this.key);
      } catch (Throwable t) {
         return null;
      }
   }

   static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(value, Math.max(min, max)));
   }
}
