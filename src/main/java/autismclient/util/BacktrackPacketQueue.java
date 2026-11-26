package autismclient.util;

import java.util.ArrayDeque;
import java.util.function.Consumer;

public final class BacktrackPacketQueue<T> {
   public static final long init_J = 300000000L;
   private final ArrayDeque<BacktrackPacketQueue.Inner_DataUtil<T>> init_ArrayDeque = new ArrayDeque<>();
   private final int init_I;
   private Object init_Obj;
   private Object load_Obj;
   private Object read;
   private Object write;
   private long load_J;

   public BacktrackPacketQueue(int capacity) {
      this.init_I = Math.max(1, capacity);
   }

   public void init_V(Object world, Object player, Object listener, Object target) {
      if (!this.init_Z(world, player, listener, target)) {
         this.init_V();
         this.init_Obj = world;
         this.load_Obj = player;
         this.read = listener;
         this.write = target;
      }
   }

   public boolean init_Z(Object world, Object player, Object listener, Object target) {
      return this.init_Obj == world && this.load_Obj == player && this.read == listener && this.write == target;
   }

   public boolean init(T value, long now, long delay) {
      if (this.init_Obj != null && this.load_Obj != null && this.read != null && this.write != null && delay > 0L && this.init_ArrayDeque.size() < this.init_I) {
         this.init_ArrayDeque.addLast(new BacktrackPacketQueue.Inner_DataUtil<>(value, now, Math.min(300000000L, delay)));
         return true;
      } else {
         return false;
      }
   }

   public int init(long now, boolean all, Consumer<T> apply) {
      long expected = this.load_J;

      int count;
      for (count = 0; this.load_J == expected && !this.init_ArrayDeque.isEmpty(); count++) {
         BacktrackPacketQueue.Inner_DataUtil<T> first = this.init_ArrayDeque.peekFirst();
         if (!all && now - first.init_J() < first.load()) {
            break;
         }

         this.init_ArrayDeque.removeFirst();

         try {
            apply.accept(first.init_Obj());
         } catch (RuntimeException | Error failure) {
            this.init_V();
            throw failure;
         }
      }

      return count;
   }

   public long init(long now) {
      return this.init_ArrayDeque.isEmpty() ? 0L : Math.max(0L, now - this.init_ArrayDeque.peekFirst().init_J());
   }

   public int init_I() {
      return this.init_ArrayDeque.size();
   }

   public void init_V() {
      this.init_ArrayDeque.clear();
      this.load_J++;
      this.init_Obj = this.load_Obj = this.read = this.write = null;
   }

   private record Inner_DataUtil<T>(T init_Obj, long init_J, long load) {
   }
}
