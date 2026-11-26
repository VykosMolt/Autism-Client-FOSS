package autismclient.util;

import java.util.concurrent.atomic.AtomicInteger;

public final class CommandProbeIdAllocator {
   public static final int init_I = 2000000000;
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger(2000000000);

   public static int init() {
      return init_AtomicInteger.getAndUpdate(id -> id >= 2147483646 ? 2000000000 : id + 1);
   }

   public static boolean init(int id) {
      return id >= 2000000000;
   }

   private CommandProbeIdAllocator() {
   }
}
