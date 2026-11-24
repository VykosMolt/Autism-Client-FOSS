package autismclient.util.g;

import java.util.ArrayDeque;
import java.util.Iterator;
import net.minecraft.world.phys.Vec3;

final class TableIndex {
   private static final int init_I = 64;
   private static final long init_J = 4000L;
   private static final double init_D = 0.04000000000000001;
   private static final double load_D = 1.0E-8;
   private static final long load_J = 750L;
   private final ArrayDeque<TableIndex.Inner_StreamUtil> init_ArrayDeque = new ArrayDeque<>();
   private Vec3 init_Vec3;
   private long read;
   private boolean init_Z;

   synchronized void init_V(Vec3 observed, long now) {
      this.init_ArrayDeque.clear();
      this.init_Vec3 = observed;
      this.read = now + 750L;
      this.init_Z = false;
   }

   synchronized void load(Vec3 position, long now) {
      if (position != null) {
         this.init_ArrayDeque.addLast(new TableIndex.Inner_StreamUtil(position, now));

         while (this.init_ArrayDeque.size() > 64) {
            this.init_ArrayDeque.removeFirst();
         }

         this.init(now);
      }
   }

   synchronized TableIndex.Inner_DataUtil init_TableIndex_DataUti(Vec3 observed, long now) {
      if (observed == null) {
         return TableIndex.Inner_DataUtil.init;
      }

      if (this.init_Vec3 != null && this.init_Vec3.distanceToSqr(observed) <= 1.0E-8) {
         this.init(now);
         if (this.init_Z && now >= this.read) {
            this.init_Z = false;
            this.init_ArrayDeque.clear();
            return TableIndex.Inner_DataUtil.read;
         } else {
            return TableIndex.Inner_DataUtil.init;
         }
      } else {
         this.init_Vec3 = observed;
         this.init(now);
         TableIndex.Inner_StreamUtil matched = null;
         Iterator<TableIndex.Inner_StreamUtil> it = this.init_ArrayDeque.descendingIterator();

         while (it.hasNext()) {
            TableIndex.Inner_StreamUtil candidate = it.next();
            if (candidate.init_Vec3().distanceToSqr(observed) <= 0.04000000000000001) {
               matched = candidate;
               break;
            }
         }

         if (matched == null) {
            if (now < this.read) {
               this.init_Z = true;
               return TableIndex.Inner_DataUtil.init;
            } else {
               this.init_Z = false;
               this.init_ArrayDeque.clear();
               return TableIndex.Inner_DataUtil.read;
            }
         } else {
            this.init_Z = false;

            while (!this.init_ArrayDeque.isEmpty()) {
               TableIndex.Inner_StreamUtil first = this.init_ArrayDeque.removeFirst();
               if (first == matched) {
                  break;
               }
            }

            return TableIndex.Inner_DataUtil.load;
         }
      }
   }

   private void init(long now) {
      while (!this.init_ArrayDeque.isEmpty() && now - this.init_ArrayDeque.peekFirst().init_J() > 4000L) {
         this.init_ArrayDeque.removeFirst();
      }
   }

   enum Inner_DataUtil {
      init,
      load,
      read;
   }

   private record Inner_StreamUtil(Vec3 init_Vec3, long init_J) {
   }
}
