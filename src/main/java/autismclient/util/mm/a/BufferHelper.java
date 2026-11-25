package autismclient.util.mm.a;

import java.util.concurrent.atomic.AtomicLong;

public final class BufferHelper {
   private final AtomicLong init = new AtomicLong(0L);

   public long init_J() {
      return this.init.incrementAndGet();
   }

   public byte[] init_BArr() {
      return StreamUtil.init_BArr_I(12);
   }
}
