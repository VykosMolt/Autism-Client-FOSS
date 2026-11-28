package autismclient.modules;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class QueuedAttackBuffer {
   private final ArrayDeque<QueuedAttackBuffer.QueuedAttack> pending = new ArrayDeque<>();

   boolean queue(Object target, Runnable attack, Runnable receipt) {
      if (this.pending.size() >= 4) {
         return false;
      }

      this.pending.addLast(new QueuedAttackBuffer.QueuedAttack(target, Objects.requireNonNull(attack), receipt == null ? () -> {} : receipt, null));
      return true;
   }

   boolean enqueue(Runnable swing) {
      QueuedAttackBuffer.QueuedAttack last = this.pending.peekLast();
      if (last != null && last.read == null) {
         this.pending.removeLast();
         this.pending.addLast(new QueuedAttackBuffer.QueuedAttack(last.target, last.action, last.load, swing));
         return true;
      } else {
         return false;
      }
   }

   boolean hasQueued() {
      return !this.pending.isEmpty();
   }

   int size() {
      return this.pending.size();
   }

   int init_I_Predicate(Predicate<Object> valid) {
      return this.dispatch(valid, target -> {});
   }

   int dispatch(Predicate<Object> valid, Consumer<Object> sent) {
      ArrayList<QueuedAttackBuffer.QueuedAttack> hits = new ArrayList<>(this.pending);
      this.pending.clear();
      int count = 0;

      for (QueuedAttackBuffer.QueuedAttack hit : hits) {
         if (valid.test(hit.target)) {
            hit.action.run();
            sent.accept(hit.target);
            hit.load.run();
            if (hit.read != null) {
               hit.read.run();
            }

            count++;
         }
      }

      return count;
   }

   void clear() {
      this.pending.clear();
   }

   static boolean isCompatibleOwner(String owner) {
      return owner == null || owner.isBlank() || "kill-aura".equals(owner) || "kb-displacement".equals(owner);
   }

   private record QueuedAttack(Object target, Runnable action, Runnable load, Runnable read) {
   }

   static final class HoldWindow {
      private long until;
      private boolean holding;

      void openDefault(long now) {
         this.open(now, 350000000L);
      }

      void open(long now, long duration) {
         this.until = now + Math.max(0L, duration);
         this.holding = true;
      }

      boolean isHolding() {
         return this.holding;
      }

      boolean isHoldingAt(long now) {
         if (this.holding && now >= this.until) {
            this.clear();
         }

         return this.holding;
      }

      void release() {
         this.clear();
      }

      void clear() {
         this.holding = false;
         this.until = 0L;
      }
   }
}
