package autismclient.gui.b;

public final class SyncManager {
   private static final int init_I = 50;
   private final StringBuilder init_StringBuilder = new StringBuilder();
   private boolean init_Z;

   public boolean init_Z() {
      return this.init_Z;
   }

   public void init_V() {
      this.init_Z = true;
   }

   public void load() {
      this.init_Z = false;
   }

   public String init_Str() {
      return this.init_StringBuilder.toString();
   }

   public void init(String value) {
      this.init_StringBuilder.setLength(0);
      if (value != null) {
         this.init_StringBuilder.append(value.length() > 50 ? value.substring(0, 50) : value);
      }
   }

   public boolean init(char c) {
      if (!this.init_Z) {
         return false;
      } else if (c >= ' ' && c != 127 && this.init_StringBuilder.length() < 50) {
         this.init_StringBuilder.append(c);
         return true;
      } else {
         return false;
      }
   }

   public boolean init(int keyCode) {
      if (!this.init_Z) {
         return false;
      }

      switch (keyCode) {
         case 256:
         case 257:
         case 335:
            this.init_Z = false;
            return true;
         case 259:
            if (this.init_StringBuilder.length() > 0) {
               this.init_StringBuilder.deleteCharAt(this.init_StringBuilder.length() - 1);
            }

            return true;
         default:
            return false;
      }
   }
}
