package autismclient.util;

final class ServerScanIdAllocator {
   static final int init = 10000;
   private static final int load = 1000000;
   private int nextBase = 1000000;

   int nextBase() {
      if (this.nextBase > 1999980000) {
         this.nextBase = 1000000;
      }

      int base = this.nextBase;
      this.nextBase += 10000;
      return base;
   }
}
