package autismclient.modules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

final class TargetHurtWindowTracker {
   static final int HURT_WINDOW_TICKS = 10;
   static final int LATE_GRACE_TICKS = 3;
   private static final int UNSET = -2147483648;
   private static final int RING_SIZE = 8;
   private final Map<Integer, TargetHurtWindowTracker.HurtWindow> windows = new HashMap<>();
   private final int[] ringSamples = new int[8];
   private int ringCount;
   private int ringIndex;

   int init_I(int fallback) {
      if (this.ringCount == 0) {
         return Math.max(0, Math.min(40, fallback));
      }

      int[] sorted = Arrays.copyOf(this.ringSamples, this.ringCount);
      Arrays.sort(sorted);
      return sorted[this.ringCount / 2];
   }

   boolean init_Z(int target, int tick, int fallback) {
      TargetHurtWindowTracker.HurtWindow w = this.windows.get(target);
      if (w == null) {
         return false;
      }

      this.expireIfLate(w, tick, fallback);
      return tick < this.windowEnd(w);
   }

   void init_V(int target, int tick, int fallback) {
      TargetHurtWindowTracker.HurtWindow w = this.windowFor(target, tick);
      this.expireIfLate(w, tick, fallback);
      if (tick >= this.windowEnd(w)) {
         w.pendingAttackTick = tick;
      }
   }

   void init(int target, int tick, boolean ours, int fallback) {
      TargetHurtWindowTracker.HurtWindow w = this.windowFor(target, tick);
      this.expireIfLate(w, tick, fallback);
      if (ours && w.pendingAttackTick != -2147483648 && tick >= w.pendingAttackTick) {
         this.recordInterval(tick - w.pendingAttackTick);
         w.ownHurtEndTick = w.pendingAttackTick + 10;
         w.pendingAttackTick = -2147483648;
      } else {
         int started = tick - this.init_I(fallback);
         if (started >= Math.max(w.ownHurtEndTick, w.inferredHurtEndTick)) {
            w.inferredHurtEndTick = started + 10;
            if (w.pendingAttackTick != -2147483648 && w.pendingAttackTick >= started && w.pendingAttackTick < w.inferredHurtEndTick) {
               w.pendingAttackTick = -2147483648;
            }
         }
      }
   }

   void clear() {
      this.windows.clear();
      this.ringCount = this.ringIndex = 0;
   }

   void forget(int target) {
      this.windows.remove(target);
   }

   private TargetHurtWindowTracker.HurtWindow windowFor(int target, int tick) {
      TargetHurtWindowTracker.HurtWindow w = this.windows.computeIfAbsent(target, ignored -> new TargetHurtWindowTracker.HurtWindow());
      w.lastSeenTick = tick;
      if (this.windows.size() > 64) {
         this.windows.values().removeIf(old -> tick - old.lastSeenTick > 400);
      }

      return w;
   }

   private int windowEnd(TargetHurtWindowTracker.HurtWindow w) {
      int until = Math.max(w.ownHurtEndTick, w.inferredHurtEndTick);
      return w.pendingAttackTick == -2147483648 ? until : Math.max(until, w.pendingAttackTick + 10);
   }

   private void expireIfLate(TargetHurtWindowTracker.HurtWindow w, int tick, int fallback) {
      if (w.pendingAttackTick != -2147483648 && tick > w.pendingAttackTick + Math.max(1, this.init_I(fallback)) + 3) {
         w.pendingAttackTick = -2147483648;
      }
   }

   private void recordInterval(int ticks) {
      this.ringSamples[this.ringIndex] = Math.max(0, Math.min(40, ticks));
      this.ringIndex = (this.ringIndex + 1) % 8;
      this.ringCount = Math.min(8, this.ringCount + 1);
   }

   private static final class HurtWindow {
      int pendingAttackTick = -2147483648;
      int ownHurtEndTick = -2147483648;
      int inferredHurtEndTick = -2147483648;
      int lastSeenTick;
   }
}
