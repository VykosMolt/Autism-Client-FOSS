package autismclient.ai;

import com.google.gson.JsonObject;
import java.util.Arrays;

final class AiPerfMetrics {
   private final long[] init_JArr = new long[1200];
   private long init_J;
   private volatile long load;

   void init(long nanos) {
      this.init_J += nanos;
   }

   void load(long nanos) {
      this.init_JArr[(int)(this.load % this.init_JArr.length)] = this.init_J + nanos;
      this.init_J = 0L;
      this.load++;
   }

   JsonObject init() {
      int size = (int)Math.min(this.load, this.init_JArr.length);
      long[] copy = Arrays.copyOf(this.init_JArr, size);
      Arrays.sort(copy);
      return AiJson.init_JsonObject_ObjArr(
         "samples",
         size,
         "p95Milliseconds",
         size == 0 ? 0.0 : copy[Math.max(0, (int)Math.ceil(size * 0.95) - 1)] / 1000000.0,
         "maxMilliseconds",
         size == 0 ? 0.0 : copy[size - 1] / 1000000.0,
         "scope",
         "Recent AI client-thread ticks and dispatched actions; rendering and provider latency excluded"
      );
   }
}
