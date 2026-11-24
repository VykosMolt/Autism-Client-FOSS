package autismclient.gui.f;

public record NetHelper(int init, int load, int read, int write) {
   public static NetHelper init(int x, int y, int width, int height) {
      return new NetHelper(x, y, Math.max(0, width), Math.max(0, height));
   }

   public int init() {
      return this.init + this.read;
   }

   public int load() {
      return this.load + this.write;
   }

   public boolean init_Z(int px, int py) {
      return px >= this.init && px < this.init() && py >= this.load && py < this.load();
   }

   public NetHelper init(int amount) {
      return this.load(amount, amount, amount, amount);
   }

   public NetHelper load(int left, int top, int right, int bottom) {
      int nx = this.init + left;
      int ny = this.load + top;
      return init(nx, ny, this.read - left - right, this.write - top - bottom);
   }

   public NetHelper init_NetHelper(int screenWidth, int screenHeight) {
      int w = Math.min(this.read, Math.max(0, screenWidth));
      int h = Math.min(this.write, Math.max(0, screenHeight));
      int nx = init(this.init, 0, Math.max(0, screenWidth - w));
      int ny = init(this.load, 0, Math.max(0, screenHeight - h));
      return init(nx, ny, w, h);
   }

   public NetHelper init(NetHelper other) {
      if (other == null) {
         return this;
      }

      int nx = Math.min(this.init, other.init);
      int ny = Math.min(this.load, other.load);
      return init(nx, ny, Math.max(this.init(), other.init()) - nx, Math.max(this.load(), other.load()) - ny);
   }

   private static int init(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   public int read() {
      return this.init;
   }

   public int write() {
      return this.load;
   }

   public int update() {
      return this.read;
   }

   public int sync() {
      return this.write;
   }
}
