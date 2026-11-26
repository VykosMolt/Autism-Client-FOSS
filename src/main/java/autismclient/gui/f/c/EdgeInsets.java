package autismclient.gui.f.c;

public record EdgeInsets(int init_I, int load, int read, int write) {
   public static final EdgeInsets init_IoWorker = new EdgeInsets(0, 0, 0, 0);

   public static EdgeInsets init(int value) {
      return new EdgeInsets(value, value, value, value);
   }

   public static EdgeInsets init(int horizontal, int vertical) {
      return new EdgeInsets(horizontal, vertical, horizontal, vertical);
   }

   public int init_I() {
      return this.init_I + this.read;
   }

   public int load() {
      return this.load + this.write;
   }

   public int read() {
      return this.init_I;
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
