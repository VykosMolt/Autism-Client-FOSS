package autismclient.modules;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class QueuedAttackBuffer {
   private final ArrayDeque<QueuedAttackBuffer.Inner_DataUtil> init = new ArrayDeque<>();

   boolean init(Object target, Runnable attack, Runnable receipt) {
      if (this.init.size() >= 4) {
         return false;
      }

      this.init.addLast(new QueuedAttackBuffer.Inner_DataUtil(target, Objects.requireNonNull(attack), receipt == null ? () -> {} : receipt, null));
      return true;
   }

   boolean enqueue(Runnable swing) {
      QueuedAttackBuffer.Inner_DataUtil last = this.init.peekLast();
      if (last != null && last.read == null) {
         this.init.removeLast();
         this.init.addLast(new QueuedAttackBuffer.Inner_DataUtil(last.target, last.action, last.load, swing));
         return true;
      } else {
         return false;
      }
   }

   boolean hasQueued() {
      return !this.init.isEmpty();
   }

   int size() {
      return this.init.size();
   }

   int init_I_Predicate(Predicate<Object> valid) {
      return this.init(valid, target -> {});
   }

   int init(Predicate<Object> valid, Consumer<Object> sent) {
      ArrayList<QueuedAttackBuffer.Inner_DataUtil> hits = new ArrayList<>(this.init);
      this.init.clear();
      int count = 0;

      for (QueuedAttackBuffer.Inner_DataUtil hit : hits) {
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
      this.init.clear();
   }

   static boolean init_Z_Str(String owner) {
      return owner == null || owner.isBlank() || "kill-aura".equals(owner) || "kb-displacement".equals(owner);
   }

   private record Inner_DataUtil(Object target, Runnable action, Runnable load, Runnable read) {
   }

   static final class Inner_StreamUtil {
      private long init_J;
      private boolean init_Z;

      void init_V(long now) {
         this.init(now, 350000000L);
      }

      void init(long now, long duration) {
         this.init_J = now + Math.max(0L, duration);
         this.init_Z = true;
      }

      boolean init_Z() {
         return this.init_Z;
      }

      boolean init_Z(long now) {
         if (this.init_Z && now >= this.init_J) {
            this.load();
         }

         return this.init_Z;
      }

      void init_V() {
         this.load();
      }

      void load() {
         this.init_Z = false;
         this.init_J = 0L;
      }
   }
}
