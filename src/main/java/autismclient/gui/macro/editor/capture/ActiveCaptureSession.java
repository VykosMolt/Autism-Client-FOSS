package autismclient.gui.macro.editor.capture;

public final class ActiveCaptureSession {
   private String activeKey;

   public String key() {
      return this.activeKey;
   }

   public boolean isActive() {
      return this.activeKey != null;
   }

   public boolean isCapturing(String key) {
      return key != null && key.equals(this.activeKey);
   }

   public void begin(String key, Runnable beginCapture) {
      if (key != null && !key.isBlank()) {
         this.activeKey = key;
         if (beginCapture != null) {
            beginCapture.run();
         }
      }
   }

   public boolean end(Runnable endCapture) {
      if (this.activeKey == null) {
         return false;
      }

      this.activeKey = null;
      if (endCapture != null) {
         endCapture.run();
      }

      return true;
   }

   public void toggle(String key, Runnable beginCapture, Runnable endCapture) {
      if (this.isCapturing(key)) {
         this.end(endCapture);
      } else {
         this.begin(key, beginCapture);
      }
   }

   public void clear() {
      this.activeKey = null;
   }
}
