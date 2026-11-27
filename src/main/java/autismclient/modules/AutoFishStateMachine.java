package autismclient.modules;

import java.util.function.IntSupplier;

final class AutoFishStateMachine {
   static final int init_I = -1;
   static final int load_I = 8;
   private static final int read_I = 2;
   private static final int write_I = 4;
   private AutoFishStateMachine.Inner_StreamUtil state = AutoFishStateMachine.Inner_StreamUtil.IDLE;
   private long tick;
   private long actionSeq;
   private long read_J;
   private long write_J;
   private long update_J;
   private long sync_J;
   private int update_I = -1;
   private int sync_I = -1;
   private int flush_I;
   private long flush_J = -9223372036854775808L;
   private long close_J = -9223372036854775808L;
   private AutoFishStateMachine.Inner_CachePool pendingAction;
   private AutoFishStateMachine.Inner_StreamUtil resumeState;
   private boolean init_Z;
   private int settleTicks = 8;
   private IntSupplier recastDelay = () -> 10;

   AutoFishStateMachine.Inner_StreamUtil state() {
      return this.state;
   }

   boolean init_Z() {
      return this.state == AutoFishStateMachine.Inner_StreamUtil.CAST_WAIT
         || this.state == AutoFishStateMachine.Inner_StreamUtil.FISHING
         || this.state == AutoFishStateMachine.Inner_StreamUtil.CATCH_DELAY
         || this.state == AutoFishStateMachine.Inner_StreamUtil.REEL_PENDING
         || this.state == AutoFishStateMachine.Inner_StreamUtil.REEL_WAIT;
   }

   AutoFishStateMachine.Inner_CachePool init(int observedHook, boolean autoCast, int timeoutTicks, int settleTicks, boolean movementBusy, IntSupplier recastDelay) {
      this.tick++;
      this.settleTicks = Math.max(8, settleTicks);
      this.recastDelay = recastDelay;
      this.load(observedHook);
      if (this.pendingAction != null) {
         if (this.tick - this.pendingAction.load() > 4L) {
            this.init(this.pendingAction, false, observedHook);
         }

         return null;
      } else {
         if (this.state == AutoFishStateMachine.Inner_StreamUtil.CAST_WAIT && this.tick >= this.update_J) {
            this.state = AutoFishStateMachine.Inner_StreamUtil.FISHING;
         }

         if (this.state == AutoFishStateMachine.Inner_StreamUtil.REEL_WAIT) {
            if (observedHook == -1 && (this.update_I != -1 ? this.flush_I >= 2 : this.tick - this.sync_J >= this.settleTicks)) {
               this.write();
            }

            return null;
         } else {
            if (this.state == AutoFishStateMachine.Inner_StreamUtil.FISHING && autoCast && timeoutTicks > 0 && this.tick - this.write_J >= timeoutTicks
               )
             {
               this.state = AutoFishStateMachine.Inner_StreamUtil.CATCH_DELAY;
               this.read_J = this.tick;
            }

            if (this.state == AutoFishStateMachine.Inner_StreamUtil.CATCH_DELAY && this.tick >= this.read_J) {
               return this.init_BitSetUtil_CachePo_BitSetUtil_DataUti_I(AutoFishStateMachine.Inner_DataUtil.REEL, observedHook);
            } else {
               return (this.state == AutoFishStateMachine.Inner_StreamUtil.IDLE || this.state == AutoFishStateMachine.Inner_StreamUtil.RECAST_DELAY)
                     && this.tick >= this.read_J
                     && autoCast
                     && !movementBusy
                     && observedHook == -1
                  ? this.init_BitSetUtil_CachePo_BitSetUtil_DataUti_I(AutoFishStateMachine.Inner_DataUtil.CAST, -1)
                  : null;
            }
         }
      }
   }

   private void load(int observed) {
      if (observed != -1) {
         this.flush_I = 0;
         if (observed != this.sync_I
            || this.state != AutoFishStateMachine.Inner_StreamUtil.RECAST_DELAY && this.state != AutoFishStateMachine.Inner_StreamUtil.CAST_PENDING) {
            if ((this.update_I == -1 || observed == this.update_I)
               && this.state != AutoFishStateMachine.Inner_StreamUtil.IDLE
               && this.state != AutoFishStateMachine.Inner_StreamUtil.RECAST_DELAY
               && this.state != AutoFishStateMachine.Inner_StreamUtil.CAST_PENDING) {
               this.update_I = observed;
               if (this.state == AutoFishStateMachine.Inner_StreamUtil.CAST_WAIT && this.tick >= this.update_J) {
                  this.state = AutoFishStateMachine.Inner_StreamUtil.FISHING;
               }
            } else {
               this.read(observed);
            }
         } else {
            this.update();
            this.update_I = observed;
            this.state = AutoFishStateMachine.Inner_StreamUtil.REEL_WAIT;
         }
      } else {
         if (this.flush_J != this.tick) {
            this.flush_I++;
            this.flush_J = this.tick;
         }

         if (this.update_I != -1
            && this.flush_I >= 2
            && (
               this.state == AutoFishStateMachine.Inner_StreamUtil.FISHING
                  || this.state == AutoFishStateMachine.Inner_StreamUtil.CATCH_DELAY
                  || this.state == AutoFishStateMachine.Inner_StreamUtil.REEL_PENDING
            )) {
            this.write();
         }
      }
   }

   boolean init_Z(int delay) {
      if (this.state == AutoFishStateMachine.Inner_StreamUtil.FISHING && this.tick >= this.update_J) {
         this.state = AutoFishStateMachine.Inner_StreamUtil.CATCH_DELAY;
         this.read_J = this.tick + Math.max(0, delay);
         return true;
      } else {
         return false;
      }
   }

   boolean init_Z_I_J(int delay, long clientTick) {
      boolean fresh = this.close_J == -9223372036854775808L || clientTick - this.close_J >= 4L;
      this.close_J = clientTick;
      if (fresh && this.state == AutoFishStateMachine.Inner_StreamUtil.CAST_WAIT) {
         this.state = AutoFishStateMachine.Inner_StreamUtil.CATCH_DELAY;
         this.read_J = Math.max(this.update_J, this.tick + Math.max(0, delay));
         return true;
      } else {
         return fresh && this.init_Z(delay);
      }
   }

   private AutoFishStateMachine.Inner_CachePool init_BitSetUtil_CachePo_BitSetUtil_DataUti_I(AutoFishStateMachine.Inner_DataUtil action, int observed) {
      this.resumeState = this.state;
      this.state = action == AutoFishStateMachine.Inner_DataUtil.CAST ? AutoFishStateMachine.Inner_StreamUtil.CAST_PENDING : AutoFishStateMachine.Inner_StreamUtil.REEL_PENDING;
      this.pendingAction = new AutoFishStateMachine.Inner_CachePool(++this.actionSeq, action, observed, this.tick);
      return this.pendingAction;
   }

   boolean init_Z_BitSetUtil_CachePo_I(AutoFishStateMachine.Inner_CachePool use, int observed) {
      if (this.pendingAction == null || !this.pendingAction.equals(use) || this.tick - use.load() > 4L) {
         return false;
      } else if (use.action() == AutoFishStateMachine.Inner_DataUtil.CAST) {
         return observed == -1;
      } else {
         return use.init_I() == -1 ? this.update_I == -1 || observed == this.update_I : observed == use.init_I();
      }
   }

   void init(AutoFishStateMachine.Inner_CachePool use, boolean dispatched, int observed) {
      if (this.pendingAction != null && this.pendingAction.equals(use)) {
         boolean valid = this.init_Z_BitSetUtil_CachePo_I(use, observed);
         this.pendingAction = null;
         if (dispatched && valid) {
            if (use.action() == AutoFishStateMachine.Inner_DataUtil.CAST) {
               this.update_I = -1;
               this.sync_I = -1;
               this.flush_I = 0;
               this.write_J = this.tick;
               this.update_J = this.tick + 8L;
               this.state = AutoFishStateMachine.Inner_StreamUtil.CAST_WAIT;
            } else {
               this.update_I = observed != -1 ? observed : use.init_I();
               this.sync_I = this.update_I;
               this.flush_I = 0;
               this.sync_J = this.tick;
               this.state = AutoFishStateMachine.Inner_StreamUtil.REEL_WAIT;
            }
         } else {
            this.state = this.resumeState;
            this.read_J = this.tick + 2L;
            this.load(observed);
         }
      }
   }

   private void write() {
      this.update();
      this.sync_I = this.update_I;
      this.state = AutoFishStateMachine.Inner_StreamUtil.RECAST_DELAY;
      this.read_J = this.tick + Math.max(1, this.recastDelay.getAsInt());
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
      this.write_J = this.tick;
      this.update_J = this.tick + 8L;
      this.state = AutoFishStateMachine.Inner_StreamUtil.CAST_WAIT;
      this.init_Z = false;
   }

   void init_V(int beforeUseHook) {
      this.update();
      this.init_Z = false;
      if (beforeUseHook != -1) {
         this.update_I = beforeUseHook;
         this.sync_I = this.update_I;
         this.flush_I = 0;
         this.sync_J = this.tick;
         this.state = AutoFishStateMachine.Inner_StreamUtil.REEL_WAIT;
      } else {
         this.update_I = this.sync_I = -1;
         this.flush_I = 0;
         this.write_J = this.tick;
         this.update_J = this.tick + 8L;
         this.state = AutoFishStateMachine.Inner_StreamUtil.CAST_WAIT;
      }
   }

   void init_V() {
      boolean wasCast = this.state == AutoFishStateMachine.Inner_StreamUtil.CAST_WAIT
         || this.state == AutoFishStateMachine.Inner_StreamUtil.FISHING
         || this.state == AutoFishStateMachine.Inner_StreamUtil.CATCH_DELAY
         || this.state == AutoFishStateMachine.Inner_StreamUtil.REEL_PENDING;
      this.init_V(-1);
      if (wasCast) {
         this.sync_J = this.tick;
         this.state = AutoFishStateMachine.Inner_StreamUtil.REEL_WAIT;
      }
   }

   void load_V() {
      if (this.pendingAction != null) {
         this.state = this.resumeState;
         this.update();
      }

      if (this.state == AutoFishStateMachine.Inner_StreamUtil.CATCH_DELAY) {
         this.state = AutoFishStateMachine.Inner_StreamUtil.FISHING;
      }

      this.init_Z = false;
   }

   private void update() {
      this.pendingAction = null;
      this.actionSeq++;
   }

   void read() {
      this.update();
      this.state = AutoFishStateMachine.Inner_StreamUtil.IDLE;
      this.update_I = this.sync_I = -1;
      this.flush_I = 0;
      this.read_J = this.write_J = this.update_J = this.sync_J = this.tick = 0L;
      this.close_J = this.flush_J = -9223372036854775808L;
      this.init_Z = false;
   }

   record Inner_CachePool(long init_J, AutoFishStateMachine.Inner_DataUtil action, int init_I, long load) {
   }

   enum Inner_DataUtil {
      CAST,
      REEL;
   }

   enum Inner_StreamUtil {
      IDLE,
      CAST_PENDING,
      CAST_WAIT,
      FISHING,
      CATCH_DELAY,
      REEL_PENDING,
      REEL_WAIT,
      RECAST_DELAY;
   }
}
