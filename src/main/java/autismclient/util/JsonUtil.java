package autismclient.util;

public final class JsonUtil {
   private boolean init;
   private boolean load;
   private boolean read;

   public boolean init(String mode, boolean keyDown) {
      boolean rising = keyDown && !this.init;
      this.init = keyDown;
      if (!keyDown) {
         this.read = false;
      }

      boolean var10000;
      switch (mode) {
         case "Hold":
            this.load = false;
            var10000 = keyDown && !this.read;
            break;
         case "Press":
            if (rising) {
               this.load = !this.load;
            }

            var10000 = this.load;
            break;
         default:
            this.load = false;
            var10000 = true;
      }

      return var10000;
   }

   public void init(String mode) {
      if ("Press".equals(mode)) {
         this.load = false;
      }

      if ("Hold".equals(mode)) {
         this.read = true;
      }
   }

   public void init() {
      this.init = false;
      this.load = false;
      this.read = false;
   }
}
