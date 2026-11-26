package autismclient.gui.macro.editor.capture;

public final class ActiveCaptureSession {
   private String init;

   public String init_Str() {
      return this.init;
   }

   public boolean init_Z() {
      return this.init != null;
   }

   public boolean init(String key) {
      return key != null && key.equals(this.init);
   }

   public void init(String key, Runnable beginCapture) {
      if (key != null && !key.isBlank()) {
         this.init = key;
         if (beginCapture != null) {
            beginCapture.run();
         }
      }
   }

   public boolean init(Runnable endCapture) {
      if (this.init == null) {
         return false;
      }

      this.init = null;
      if (endCapture != null) {
         endCapture.run();
      }

      return true;
   }

   public void init(String key, Runnable beginCapture, Runnable endCapture) {
      if (this.init(key)) {
         this.init(endCapture);
      } else {
         this.init(key, beginCapture);
      }
   }

   public void init_V() {
      this.init = null;
   }
}
