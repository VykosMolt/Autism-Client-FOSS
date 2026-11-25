package autismclient.util.mm.c;

import java.util.concurrent.atomic.AtomicLong;

public final class BufferHelper {
   public final String init_Str;
   public volatile boolean init_Z;
   public volatile String load_Str = "";
   public final AtomicLong init_AtomicLong = new AtomicLong();
   public final AtomicLong load_AtomicLong = new AtomicLong();
   public final AtomicLong read = new AtomicLong();
   public volatile long init_J;

   public BufferHelper(String name) {
      this.init_Str = name;
   }

   public void init_V() {
      this.init_J = System.currentTimeMillis();
   }

   public String init_Str() {
      return this.init_Str
         + (this.init_Z ? " ●" : " ○")
         + "  ↑"
         + this.init_AtomicLong.get()
         + " ↓"
         + this.load_AtomicLong.get()
         + (this.load_Str.isEmpty() ? "" : "  (" + this.load_Str + ")");
   }
}
