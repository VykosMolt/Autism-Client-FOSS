package autismclient.util;

final class ServerScanIdAllocator {
   static final int init = 10000;
   private static final int load = 1000000;
   private int read = 1000000;

   int init() {
      if (this.read > 1999980000) {
         this.read = 1000000;
      }

      int base = this.read;
      this.read += 10000;
      return base;
   }
}
