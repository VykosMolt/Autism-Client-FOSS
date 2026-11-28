package autismclient.util;

import java.util.ArrayDeque;
import java.util.function.Consumer;

public final class BacktrackPacketQueue<T> {
   public static final long init_J = 300000000L;
   private final ArrayDeque<BacktrackPacketQueue.Inner_DataUtil<T>> queue = new ArrayDeque<>();
   private final int capacity;
   private Object init_Obj;
   private Object load_Obj;
   private Object read;
   private Object write;
   private long load_J;

   public BacktrackPacketQueue(int capacity) {
      this.capacity = Math.max(1, capacity);
   }

   public void bind(Object world, Object player, Object listener, Object target) {
      if (!this.isBoundTo(world, player, listener, target)) {
         this.clear();
         this.init_Obj = world;
         this.load_Obj = player;
         this.read = listener;
         this.write = target;
      }
   }

   public boolean isBoundTo(Object world, Object player, Object listener, Object target) {
      return this.init_Obj == world && this.load_Obj == player && this.read == listener && this.write == target;
   }

   public boolean offer(T value, long now, long delay) {
      if (this.init_Obj != null && this.load_Obj != null && this.read != null && this.write != null && delay > 0L && this.queue.size() < this.capacity) {
         this.queue.addLast(new BacktrackPacketQueue.Inner_DataUtil<>(value, now, Math.min(300000000L, delay)));
         return true;
      } else {
         return false;
      }
   }

   public int release(long now, boolean all, Consumer<T> apply) {
      long expected = this.load_J;

      int count;
      for (count = 0; this.load_J == expected && !this.queue.isEmpty(); count++) {
         BacktrackPacketQueue.Inner_DataUtil<T> first = this.queue.peekFirst();
         if (!all && now - first.receivedAt() < first.load()) {
            break;
         }

         this.queue.removeFirst();

         try {
            apply.accept(first.packet());
         } catch (RuntimeException | Error failure) {
            this.clear();
            throw failure;
         }
      }

      return count;
   }

   public long oldestAgeNanos(long now) {
      return this.queue.isEmpty() ? 0L : Math.max(0L, now - this.queue.peekFirst().receivedAt());
   }

   public int size() {
      return this.queue.size();
   }

   public void clear() {
      this.queue.clear();
      this.load_J++;
      this.init_Obj = this.load_Obj = this.read = this.write = null;
   }

   private record Inner_DataUtil<T>(T packet, long receivedAt, long load) {
   }
}
