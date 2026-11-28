package autismclient.util;

public final class ActivationModeLatch {
   private boolean wasDown;
   private boolean toggled;
   private boolean holdConsumed;

   public boolean update(String mode, boolean keyDown) {
      boolean rising = keyDown && !this.wasDown;
      this.wasDown = keyDown;
      if (!keyDown) {
         this.holdConsumed = false;
      }

      boolean var10000;
      switch (mode) {
         case "Hold":
            this.toggled = false;
            var10000 = keyDown && !this.holdConsumed;
            break;
         case "Press":
            if (rising) {
               this.toggled = !this.toggled;
            }

            var10000 = this.toggled;
            break;
         default:
            this.toggled = false;
            var10000 = true;
      }

      return var10000;
   }

   public void init(String mode) {
      if ("Press".equals(mode)) {
         this.toggled = false;
      }

      if ("Hold".equals(mode)) {
         this.holdConsumed = true;
      }
   }

   public void reset() {
      this.wasDown = false;
      this.toggled = false;
      this.holdConsumed = false;
   }
}
