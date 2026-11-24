package autismclient.gui.f.c;

public record IoWorker(int init_I, int load, int read, int write) {
   public static final IoWorker init_IoWorker = new IoWorker(0, 0, 0, 0);

   public static IoWorker init(int value) {
      return new IoWorker(value, value, value, value);
   }

   public static IoWorker init(int horizontal, int vertical) {
      return new IoWorker(horizontal, vertical, horizontal, vertical);
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
