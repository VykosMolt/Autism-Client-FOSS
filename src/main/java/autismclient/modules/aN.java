package autismclient.modules;

import java.util.Random;

final class aN {
   static final int init = 200;
   private int load;
   private int read;
   private int write = -1;
   private int update;
   private int sync;

   boolean init(int percentage, boolean climbing, Random random) {
      this.sync = 0;
      int rate = Math.clamp(percentage, 0, 200);
      if (rate == 0) {
         this.load();
         return false;
      }

      if (climbing) {
         return false;
      }

      this.sync = this.init(rate, random);
      this.read = this.load(rate);
      this.load = rate;
      this.write = -1;
      return this.init_Z();
   }

   int init(int percentage, Random random) {
      int rate = Math.clamp(percentage, 0, 200);
      if (rate == 0) {
         return 0;
      }

      if (this.write != rate) {
         this.write = rate;
         this.update = random.nextInt(100);
      }

      return Math.clamp(Math.floorDiv(this.load(rate) + 99 - this.update, 100), rate / 100, init(rate));
   }

   private int load(int rate) {
      return Math.min(100 * (init(rate) + 1), (rate == this.load ? this.read : 0) + rate);
   }

   boolean init_Z() {
      return this.sync > 0;
   }

   int init_I() {
      return this.sync;
   }

   static int init(int percentage) {
      return (Math.clamp(percentage, 0, 200) + 99) / 100;
   }

   void init_V() {
      if (this.sync > 0) {
         this.sync--;
         this.read = Math.max(-100, this.read - 100);
      }
   }

   void load() {
      this.load = 0;
      this.read = 0;
      this.write = -1;
      this.sync = 0;
   }
}
