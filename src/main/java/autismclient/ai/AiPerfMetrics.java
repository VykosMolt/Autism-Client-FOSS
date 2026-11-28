package autismclient.ai;

import com.google.gson.JsonObject;
import java.util.Arrays;

final class AiPerfMetrics {
   private final long[] samples = new long[1200];
   private long total;
   private volatile long sampleCount;

   void add(long nanos) {
      this.total += nanos;
   }

   void endSample(long nanos) {
      this.samples[(int)(this.sampleCount % this.samples.length)] = this.total + nanos;
      this.total = 0L;
      this.sampleCount++;
   }

   JsonObject summary() {
      int size = (int)Math.min(this.sampleCount, this.samples.length);
      long[] copy = Arrays.copyOf(this.samples, size);
      Arrays.sort(copy);
      return AiJson.objectOf(
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
