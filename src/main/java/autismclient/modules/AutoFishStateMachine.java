package autismclient.modules;

import java.util.function.IntSupplier;

final class AutoFishStateMachine {
   static final int NO_HOOK = -1;
   static final int load_I = 8;
   private static final int read_I = 2;
   private static final int write_I = 4;
   private AutoFishStateMachine.FishState state = AutoFishStateMachine.FishState.IDLE;
   private long tick;
   private long actionSeq;
   private long nextActionTick;
   private long castTick;
   private long castSettleTick;
   private long reelTick;
   private int trackedHook = -1;
   private int reeledHook = -1;
   private int missingHookTicks;
   private long lastMissingTick = -9223372036854775808L;
   private long lastBiteTick = -9223372036854775808L;
   private AutoFishStateMachine.PendingAction pendingAction;
   private AutoFishStateMachine.FishState resumeState;
   private boolean catchCompleted;
   private int settleTicks = 8;
   private IntSupplier recastDelay = () -> 10;

   AutoFishStateMachine.FishState state() {
      return this.state;
   }

   boolean unusedIsFishing() {
      return this.state == AutoFishStateMachine.FishState.CAST_WAIT
         || this.state == AutoFishStateMachine.FishState.FISHING
         || this.state == AutoFishStateMachine.FishState.CATCH_DELAY
         || this.state == AutoFishStateMachine.FishState.REEL_PENDING
         || this.state == AutoFishStateMachine.FishState.REEL_WAIT;
   }

   AutoFishStateMachine.PendingAction init(int observedHook, boolean autoCast, int timeoutTicks, int settleTicks, boolean movementBusy, IntSupplier recastDelay) {
      this.tick++;
      this.settleTicks = Math.max(8, settleTicks);
      this.recastDelay = recastDelay;
      this.observeHook(observedHook);
      if (this.pendingAction != null) {
         if (this.tick - this.pendingAction.queuedTick() > 4L) {
            this.init(this.pendingAction, false, observedHook);
         }

         return null;
      } else {
         if (this.state == AutoFishStateMachine.FishState.CAST_WAIT && this.tick >= this.castSettleTick) {
            this.state = AutoFishStateMachine.FishState.FISHING;
         }

         if (this.state == AutoFishStateMachine.FishState.REEL_WAIT) {
            if (observedHook == -1 && (this.trackedHook != -1 ? this.missingHookTicks >= 2 : this.tick - this.reelTick >= this.settleTicks)) {
               this.completeCatch();
            }

            return null;
         } else {
            if (this.state == AutoFishStateMachine.FishState.FISHING && autoCast && timeoutTicks > 0 && this.tick - this.castTick >= timeoutTicks
               )
             {
               this.state = AutoFishStateMachine.FishState.CATCH_DELAY;
               this.nextActionTick = this.tick;
            }

            if (this.state == AutoFishStateMachine.FishState.CATCH_DELAY && this.tick >= this.nextActionTick) {
               return this.beginAction(AutoFishStateMachine.RodAction.REEL, observedHook);
            } else {
               return (this.state == AutoFishStateMachine.FishState.IDLE || this.state == AutoFishStateMachine.FishState.RECAST_DELAY)
                     && this.tick >= this.nextActionTick
                     && autoCast
                     && !movementBusy
                     && observedHook == -1
                  ? this.beginAction(AutoFishStateMachine.RodAction.CAST, -1)
                  : null;
            }
         }
      }
   }

   private void observeHook(int observed) {
      if (observed != -1) {
         this.missingHookTicks = 0;
         if (observed != this.reeledHook
            || this.state != AutoFishStateMachine.FishState.RECAST_DELAY && this.state != AutoFishStateMachine.FishState.CAST_PENDING) {
            if ((this.trackedHook == -1 || observed == this.trackedHook)
               && this.state != AutoFishStateMachine.FishState.IDLE
               && this.state != AutoFishStateMachine.FishState.RECAST_DELAY
               && this.state != AutoFishStateMachine.FishState.CAST_PENDING) {
               this.trackedHook = observed;
               if (this.state == AutoFishStateMachine.FishState.CAST_WAIT && this.tick >= this.castSettleTick) {
                  this.state = AutoFishStateMachine.FishState.FISHING;
               }
            } else {
               this.adoptCastHook(observed);
            }
         } else {
            this.clearPendingAction();
            this.trackedHook = observed;
            this.state = AutoFishStateMachine.FishState.REEL_WAIT;
         }
      } else {
         if (this.lastMissingTick != this.tick) {
            this.missingHookTicks++;
            this.lastMissingTick = this.tick;
         }

         if (this.trackedHook != -1
            && this.missingHookTicks >= 2
            && (
               this.state == AutoFishStateMachine.FishState.FISHING
                  || this.state == AutoFishStateMachine.FishState.CATCH_DELAY
                  || this.state == AutoFishStateMachine.FishState.REEL_PENDING
            )) {
            this.completeCatch();
         }
      }
   }

   boolean init_Z(int delay) {
      if (this.state == AutoFishStateMachine.FishState.FISHING && this.tick >= this.castSettleTick) {
         this.state = AutoFishStateMachine.FishState.CATCH_DELAY;
         this.nextActionTick = this.tick + Math.max(0, delay);
         return true;
      } else {
         return false;
      }
   }

   boolean init_Z_I_J(int delay, long clientTick) {
      boolean fresh = this.lastBiteTick == -9223372036854775808L || clientTick - this.lastBiteTick >= 4L;
      this.lastBiteTick = clientTick;
      if (fresh && this.state == AutoFishStateMachine.FishState.CAST_WAIT) {
         this.state = AutoFishStateMachine.FishState.CATCH_DELAY;
         this.nextActionTick = Math.max(this.castSettleTick, this.tick + Math.max(0, delay));
         return true;
      } else {
         return fresh && this.init_Z(delay);
      }
   }

   private AutoFishStateMachine.PendingAction beginAction(AutoFishStateMachine.RodAction action, int observed) {
      this.resumeState = this.state;
      this.state = action == AutoFishStateMachine.RodAction.CAST ? AutoFishStateMachine.FishState.CAST_PENDING : AutoFishStateMachine.FishState.REEL_PENDING;
      this.pendingAction = new AutoFishStateMachine.PendingAction(++this.actionSeq, action, observed, this.tick);
      return this.pendingAction;
   }

   boolean init_Z_BitSetUtil_CachePo_I(AutoFishStateMachine.PendingAction use, int observed) {
      if (this.pendingAction == null || !this.pendingAction.equals(use) || this.tick - use.queuedTick() > 4L) {
         return false;
      } else if (use.action() == AutoFishStateMachine.RodAction.CAST) {
         return observed == -1;
      } else {
         return use.init_I() == -1 ? this.trackedHook == -1 || observed == this.trackedHook : observed == use.init_I();
      }
   }

   void init(AutoFishStateMachine.PendingAction use, boolean dispatched, int observed) {
      if (this.pendingAction != null && this.pendingAction.equals(use)) {
         boolean valid = this.init_Z_BitSetUtil_CachePo_I(use, observed);
         this.pendingAction = null;
         if (dispatched && valid) {
            if (use.action() == AutoFishStateMachine.RodAction.CAST) {
               this.trackedHook = -1;
               this.reeledHook = -1;
               this.missingHookTicks = 0;
               this.castTick = this.tick;
               this.castSettleTick = this.tick + 8L;
               this.state = AutoFishStateMachine.FishState.CAST_WAIT;
            } else {
               this.trackedHook = observed != -1 ? observed : use.init_I();
               this.reeledHook = this.trackedHook;
               this.missingHookTicks = 0;
               this.reelTick = this.tick;
               this.state = AutoFishStateMachine.FishState.REEL_WAIT;
            }
         } else {
            this.state = this.resumeState;
            this.nextActionTick = this.tick + 2L;
            this.observeHook(observed);
         }
      }
   }

   private void completeCatch() {
      this.clearPendingAction();
      this.reeledHook = this.trackedHook;
      this.state = AutoFishStateMachine.FishState.RECAST_DELAY;
      this.nextActionTick = this.tick + Math.max(1, this.recastDelay.getAsInt());
      this.catchCompleted = true;
   }

   boolean load_Z() {
      boolean result = this.catchCompleted;
      this.catchCompleted = false;
      return result;
   }

   private void adoptCastHook(int observed) {
      this.clearPendingAction();
      this.trackedHook = observed;
      this.reeledHook = -1;
      this.missingHookTicks = 0;
      this.lastMissingTick = -9223372036854775808L;
      this.castTick = this.tick;
      this.castSettleTick = this.tick + 8L;
      this.state = AutoFishStateMachine.FishState.CAST_WAIT;
      this.catchCompleted = false;
   }

   void init_V(int beforeUseHook) {
      this.clearPendingAction();
      this.catchCompleted = false;
      if (beforeUseHook != -1) {
         this.trackedHook = beforeUseHook;
         this.reeledHook = this.trackedHook;
         this.missingHookTicks = 0;
         this.reelTick = this.tick;
         this.state = AutoFishStateMachine.FishState.REEL_WAIT;
      } else {
         this.trackedHook = this.reeledHook = -1;
         this.missingHookTicks = 0;
         this.castTick = this.tick;
         this.castSettleTick = this.tick + 8L;
         this.state = AutoFishStateMachine.FishState.CAST_WAIT;
      }
   }

   void init_V() {
      boolean wasCast = this.state == AutoFishStateMachine.FishState.CAST_WAIT
         || this.state == AutoFishStateMachine.FishState.FISHING
         || this.state == AutoFishStateMachine.FishState.CATCH_DELAY
         || this.state == AutoFishStateMachine.FishState.REEL_PENDING;
      this.init_V(-1);
      if (wasCast) {
         this.reelTick = this.tick;
         this.state = AutoFishStateMachine.FishState.REEL_WAIT;
      }
   }

   void load_V() {
      if (this.pendingAction != null) {
         this.state = this.resumeState;
         this.clearPendingAction();
      }

      if (this.state == AutoFishStateMachine.FishState.CATCH_DELAY) {
         this.state = AutoFishStateMachine.FishState.FISHING;
      }

      this.catchCompleted = false;
   }

   private void clearPendingAction() {
      this.pendingAction = null;
      this.actionSeq++;
   }

   void read() {
      this.clearPendingAction();
      this.state = AutoFishStateMachine.FishState.IDLE;
      this.trackedHook = this.reeledHook = -1;
      this.missingHookTicks = 0;
      this.nextActionTick = this.castTick = this.castSettleTick = this.reelTick = this.tick = 0L;
      this.lastBiteTick = this.lastMissingTick = -9223372036854775808L;
      this.catchCompleted = false;
   }

   record PendingAction(long init_J, AutoFishStateMachine.RodAction action, int init_I, long queuedTick) {
   }

   enum RodAction {
      CAST,
      REEL;
   }

   enum FishState {
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
