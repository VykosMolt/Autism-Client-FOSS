package autismclient.util.oresim;

final class NetHelper {
   private final double[] init_DArr;
   private final long[] init_JArr;
   private final int[] init_IArr;
   private int init_I;
   private boolean init_Z;

   NetHelper(int capacity) {
      if (capacity <= 0) {
         throw new IllegalArgumentException("capacity must be positive");
      }

      this.init_DArr = new double[capacity];
      this.init_JArr = new long[capacity];
      this.init_IArr = new int[capacity];
   }

   boolean init_Z() {
      return this.init_I == this.init_DArr.length;
   }

   double init_D() {
      if (this.init_I == 0) {
         throw new IllegalStateException("selector is empty");
      } else {
         return this.init_DArr[0];
      }
   }

   boolean load() {
      return this.init_Z;
   }

   void init(double distanceSquared, long packedPosition, int stateId) {
      if (this.init_I < this.init_DArr.length) {
         this.init_DArr[this.init_I] = distanceSquared;
         this.init_JArr[this.init_I] = packedPosition;
         this.init_IArr[this.init_I] = stateId;
         this.init(this.init_I++);
      } else {
         this.init_Z = true;
         if (init(distanceSquared, packedPosition, stateId, this.init_DArr[0], this.init_JArr[0], this.init_IArr[0]) < 0) {
            this.init_DArr[0] = distanceSquared;
            this.init_JArr[0] = packedPosition;
            this.init_IArr[0] = stateId;
            this.init_V(0, this.init_I);
         }
      }
   }

   int init(long[] outPositions, int[] outStates) {
      if (outPositions.length >= this.init_I && outStates.length >= this.init_I) {
         int remaining = this.init_I;

         while (remaining > 1) {
            this.load(0, remaining - 1);
            this.init_V(0, --remaining);
         }

         System.arraycopy(this.init_JArr, 0, outPositions, 0, this.init_I);
         System.arraycopy(this.init_IArr, 0, outStates, 0, this.init_I);
         return this.init_I;
      } else {
         throw new IllegalArgumentException("output arrays are smaller than the selection");
      }
   }

   private void init(int index) {
      while (index > 0) {
         int parent = index - 1 >>> 1;
         if (this.init_I(parent, index) >= 0) {
            return;
         }

         this.load(parent, index);
         index = parent;
      }
   }

   private void init_V(int index, int heapSize) {
      while (true) {
         int left = index * 2 + 1;
         if (left >= heapSize) {
            return;
         }

         int largest = left;
         int right = left + 1;
         if (right < heapSize && this.init_I(right, left) > 0) {
            largest = right;
         }

         if (this.init_I(index, largest) >= 0) {
            return;
         }

         this.load(index, largest);
         index = largest;
      }
   }

   private int init_I(int a, int b) {
      return init(this.init_DArr[a], this.init_JArr[a], this.init_IArr[a], this.init_DArr[b], this.init_JArr[b], this.init_IArr[b]);
   }

   private static int init(double distanceA, long positionA, int stateA, double distanceB, long positionB, int stateB) {
      int distanceOrder = Double.compare(distanceA, distanceB);
      if (distanceOrder != 0) {
         return distanceOrder;
      }

      int positionOrder = Long.compare(positionA, positionB);
      return positionOrder != 0 ? positionOrder : Integer.compare(stateA, stateB);
   }

   private void load(int a, int b) {
      double distance = this.init_DArr[a];
      this.init_DArr[a] = this.init_DArr[b];
      this.init_DArr[b] = distance;
      long position = this.init_JArr[a];
      this.init_JArr[a] = this.init_JArr[b];
      this.init_JArr[b] = position;
      int state = this.init_IArr[a];
      this.init_IArr[a] = this.init_IArr[b];
      this.init_IArr[b] = state;
   }
}
