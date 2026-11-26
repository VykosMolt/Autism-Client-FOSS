package autismclient.util;

public class OverlayBounds {
   public int init_I;
   public int load_I;
   public int read;
   public int write;
   public boolean init_Z;
   public boolean load_Z;

   public OverlayBounds() {
   }

   public OverlayBounds(int x, int y, int width, int height, boolean visible, boolean collapsed) {
      this.init_I = x;
      this.load_I = y;
      this.read = width;
      this.write = height;
      this.init_Z = visible;
      this.load_Z = collapsed;
   }
}
