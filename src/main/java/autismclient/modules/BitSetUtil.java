package autismclient.modules;

import java.util.function.IntSupplier;

final class BitSetUtil {
   static final int init_I = -1;
   static final int load_I = 8;
   private static final int read_I = 2;
   private static final int write_I = 4;
   private BitSetUtil.Inner_StreamUtil init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.init;
   private long init_J;
   private long load_J;
   private long read_J;
   private long write_J;
   private long update_J;
   private long sync_J;
   private int update_I = -1;
   private int sync_I = -1;
   private int flush_I;
   private long flush_J = -9223372036854775808L;
   private long close_J = -9223372036854775808L;
   private BitSetUtil.Inner_CachePool init_BitSetUtil_CachePo;
   private BitSetUtil.Inner_StreamUtil load_BitSetUtil_StreamU;
   private boolean init_Z;
   private int close_I = 8;
   private IntSupplier init_IntSupplier = () -> 10;

   BitSetUtil.Inner_StreamUtil init_BitSetUtil_StreamU() {
      return this.init_BitSetUtil_StreamU;
   }

   boolean init_Z() {
      return this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.read
         || this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.write
         || this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.update
         || this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.sync
         || this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.flush;
   }

   BitSetUtil.Inner_CachePool init(int observedHook, boolean autoCast, int timeoutTicks, int settleTicks, boolean movementBusy, IntSupplier recastDelay) {
      this.init_J++;
      this.close_I = Math.max(8, settleTicks);
      this.init_IntSupplier = recastDelay;
      this.load(observedHook);
      if (this.init_BitSetUtil_CachePo != null) {
         if (this.init_J - this.init_BitSetUtil_CachePo.load() > 4L) {
            this.init(this.init_BitSetUtil_CachePo, false, observedHook);
         }

         return null;
      } else {
         if (this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.read && this.init_J >= this.update_J) {
            this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.write;
         }

         if (this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.flush) {
            if (observedHook == -1 && (this.update_I != -1 ? this.flush_I >= 2 : this.init_J - this.sync_J >= this.close_I)) {
               this.write();
            }

            return null;
         } else {
            if (this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.write && autoCast && timeoutTicks > 0 && this.init_J - this.write_J >= timeoutTicks
               )
             {
               this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.update;
               this.read_J = this.init_J;
            }

            if (this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.update && this.init_J >= this.read_J) {
               return this.init_BitSetUtil_CachePo_BitSetUtil_DataUti_I(BitSetUtil.Inner_DataUtil.load, observedHook);
            } else {
               return (this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.init || this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.close)
                     && this.init_J >= this.read_J
                     && autoCast
                     && !movementBusy
                     && observedHook == -1
                  ? this.init_BitSetUtil_CachePo_BitSetUtil_DataUti_I(BitSetUtil.Inner_DataUtil.init, -1)
                  : null;
            }
         }
      }
   }

   private void load(int observed) {
      if (observed != -1) {
         this.flush_I = 0;
         if (observed != this.sync_I
            || this.init_BitSetUtil_StreamU != BitSetUtil.Inner_StreamUtil.close && this.init_BitSetUtil_StreamU != BitSetUtil.Inner_StreamUtil.load) {
            if ((this.update_I == -1 || observed == this.update_I)
               && this.init_BitSetUtil_StreamU != BitSetUtil.Inner_StreamUtil.init
               && this.init_BitSetUtil_StreamU != BitSetUtil.Inner_StreamUtil.close
               && this.init_BitSetUtil_StreamU != BitSetUtil.Inner_StreamUtil.load) {
               this.update_I = observed;
               if (this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.read && this.init_J >= this.update_J) {
                  this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.write;
               }
            } else {
               this.read(observed);
            }
         } else {
            this.update();
            this.update_I = observed;
            this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.flush;
         }
      } else {
         if (this.flush_J != this.init_J) {
            this.flush_I++;
            this.flush_J = this.init_J;
         }

         if (this.update_I != -1
            && this.flush_I >= 2
            && (
               this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.write
                  || this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.update
                  || this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.sync
            )) {
            this.write();
         }
      }
   }

   boolean init_Z(int delay) {
      if (this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.write && this.init_J >= this.update_J) {
         this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.update;
         this.read_J = this.init_J + Math.max(0, delay);
         return true;
      } else {
         return false;
      }
   }

   boolean init_Z_I_J(int delay, long clientTick) {
      boolean fresh = this.close_J == -9223372036854775808L || clientTick - this.close_J >= 4L;
      this.close_J = clientTick;
      if (fresh && this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.read) {
         this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.update;
         this.read_J = Math.max(this.update_J, this.init_J + Math.max(0, delay));
         return true;
      } else {
         return fresh && this.init_Z(delay);
      }
   }

   private BitSetUtil.Inner_CachePool init_BitSetUtil_CachePo_BitSetUtil_DataUti_I(BitSetUtil.Inner_DataUtil action, int observed) {
      this.load_BitSetUtil_StreamU = this.init_BitSetUtil_StreamU;
      this.init_BitSetUtil_StreamU = action == BitSetUtil.Inner_DataUtil.init ? BitSetUtil.Inner_StreamUtil.load : BitSetUtil.Inner_StreamUtil.sync;
      this.init_BitSetUtil_CachePo = new BitSetUtil.Inner_CachePool(++this.load_J, action, observed, this.init_J);
      return this.init_BitSetUtil_CachePo;
   }

   boolean init_Z_BitSetUtil_CachePo_I(BitSetUtil.Inner_CachePool use, int observed) {
      if (this.init_BitSetUtil_CachePo == null || !this.init_BitSetUtil_CachePo.equals(use) || this.init_J - use.load() > 4L) {
         return false;
      } else if (use.init_BitSetUtil_DataUti() == BitSetUtil.Inner_DataUtil.init) {
         return observed == -1;
      } else {
         return use.init_I() == -1 ? this.update_I == -1 || observed == this.update_I : observed == use.init_I();
      }
   }

   void init(BitSetUtil.Inner_CachePool use, boolean dispatched, int observed) {
      if (this.init_BitSetUtil_CachePo != null && this.init_BitSetUtil_CachePo.equals(use)) {
         boolean valid = this.init_Z_BitSetUtil_CachePo_I(use, observed);
         this.init_BitSetUtil_CachePo = null;
         if (dispatched && valid) {
            if (use.init_BitSetUtil_DataUti() == BitSetUtil.Inner_DataUtil.init) {
               this.update_I = -1;
               this.sync_I = -1;
               this.flush_I = 0;
               this.write_J = this.init_J;
               this.update_J = this.init_J + 8L;
               this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.read;
            } else {
               this.update_I = observed != -1 ? observed : use.init_I();
               this.sync_I = this.update_I;
               this.flush_I = 0;
               this.sync_J = this.init_J;
               this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.flush;
            }
         } else {
            this.init_BitSetUtil_StreamU = this.load_BitSetUtil_StreamU;
            this.read_J = this.init_J + 2L;
            this.load(observed);
         }
      }
   }

   private void write() {
      this.update();
      this.sync_I = this.update_I;
      this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.close;
      this.read_J = this.init_J + Math.max(1, this.init_IntSupplier.getAsInt());
      this.init_Z = true;
   }

   boolean load_Z() {
      boolean result = this.init_Z;
      this.init_Z = false;
      return result;
   }

   private void read(int observed) {
      this.update();
      this.update_I = observed;
      this.sync_I = -1;
      this.flush_I = 0;
      this.flush_J = -9223372036854775808L;
      this.write_J = this.init_J;
      this.update_J = this.init_J + 8L;
      this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.read;
      this.init_Z = false;
   }

   void init_V(int beforeUseHook) {
      this.update();
      this.init_Z = false;
      if (beforeUseHook != -1) {
         this.update_I = beforeUseHook;
         this.sync_I = this.update_I;
         this.flush_I = 0;
         this.sync_J = this.init_J;
         this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.flush;
      } else {
         this.update_I = this.sync_I = -1;
         this.flush_I = 0;
         this.write_J = this.init_J;
         this.update_J = this.init_J + 8L;
         this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.read;
      }
   }

   void init_V() {
      boolean wasCast = this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.read
         || this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.write
         || this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.update
         || this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.sync;
      this.init_V(-1);
      if (wasCast) {
         this.sync_J = this.init_J;
         this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.flush;
      }
   }

   void load_V() {
      if (this.init_BitSetUtil_CachePo != null) {
         this.init_BitSetUtil_StreamU = this.load_BitSetUtil_StreamU;
         this.update();
      }

      if (this.init_BitSetUtil_StreamU == BitSetUtil.Inner_StreamUtil.update) {
         this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.write;
      }

      this.init_Z = false;
   }

   private void update() {
      this.init_BitSetUtil_CachePo = null;
      this.load_J++;
   }

   void read() {
      this.update();
      this.init_BitSetUtil_StreamU = BitSetUtil.Inner_StreamUtil.init;
      this.update_I = this.sync_I = -1;
      this.flush_I = 0;
      this.read_J = this.write_J = this.update_J = this.sync_J = this.init_J = 0L;
      this.close_J = this.flush_J = -9223372036854775808L;
      this.init_Z = false;
   }

   record Inner_CachePool(long init_J, BitSetUtil.Inner_DataUtil init_BitSetUtil_DataUti, int init_I, long load) {
   }

   enum Inner_DataUtil {
      init,
      load;
   }

   enum Inner_StreamUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close;
   }
}
