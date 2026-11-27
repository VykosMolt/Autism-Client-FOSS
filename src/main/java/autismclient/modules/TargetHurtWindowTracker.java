package autismclient.modules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

final class TargetHurtWindowTracker {
   static final int HURT_WINDOW_TICKS = 10;
   static final int load = 3;
   private static final int read = -2147483648;
   private static final int write = 8;
   private final Map<Integer, TargetHurtWindowTracker.Inner_DataUtil> windows = new HashMap<>();
   private final int[] init_IArr = new int[8];
   private int update;
   private int sync;

   int init_I(int fallback) {
      if (this.update == 0) {
         return Math.max(0, Math.min(40, fallback));
      }

      int[] sorted = Arrays.copyOf(this.init_IArr, this.update);
      Arrays.sort(sorted);
      return sorted[this.update / 2];
   }

   boolean init_Z(int target, int tick, int fallback) {
      TargetHurtWindowTracker.Inner_DataUtil w = this.windows.get(target);
      if (w == null) {
         return false;
      }

      this.init(w, tick, fallback);
      return tick < this.init(w);
   }

   void init_V(int target, int tick, int fallback) {
      TargetHurtWindowTracker.Inner_DataUtil w = this.init(target, tick);
      this.init(w, tick, fallback);
      if (tick >= this.init(w)) {
         w.init = tick;
      }
   }

   void init(int target, int tick, boolean ours, int fallback) {
      TargetHurtWindowTracker.Inner_DataUtil w = this.init(target, tick);
      this.init(w, tick, fallback);
      if (ours && w.init != -2147483648 && tick >= w.init) {
         this.load(tick - w.init);
         w.load = w.init + 10;
         w.init = -2147483648;
      } else {
         int started = tick - this.init_I(fallback);
         if (started >= Math.max(w.load, w.read)) {
            w.read = started + 10;
            if (w.init != -2147483648 && w.init >= started && w.init < w.read) {
               w.init = -2147483648;
            }
         }
      }
   }

   void init() {
      this.windows.clear();
      this.update = this.sync = 0;
   }

   void forget(int target) {
      this.windows.remove(target);
   }

   private TargetHurtWindowTracker.Inner_DataUtil init(int target, int tick) {
      TargetHurtWindowTracker.Inner_DataUtil w = this.windows.computeIfAbsent(target, ignored -> new TargetHurtWindowTracker.Inner_DataUtil());
      w.write = tick;
      if (this.windows.size() > 64) {
         this.windows.values().removeIf(old -> tick - old.write > 400);
      }

      return w;
   }

   private int init(TargetHurtWindowTracker.Inner_DataUtil w) {
      int until = Math.max(w.load, w.read);
      return w.init == -2147483648 ? until : Math.max(until, w.init + 10);
   }

   private void init(TargetHurtWindowTracker.Inner_DataUtil w, int tick, int fallback) {
      if (w.init != -2147483648 && tick > w.init + Math.max(1, this.init_I(fallback)) + 3) {
         w.init = -2147483648;
      }
   }

   private void load(int ticks) {
      this.init_IArr[this.sync] = Math.max(0, Math.min(40, ticks));
      this.sync = (this.sync + 1) % 8;
      this.update = Math.min(8, this.update + 1);
   }

   private static final class Inner_DataUtil {
      int init = -2147483648;
      int load = -2147483648;
      int read = -2147483648;
      int write;
   }
}
