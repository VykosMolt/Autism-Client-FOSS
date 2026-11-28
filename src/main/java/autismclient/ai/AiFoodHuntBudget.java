package autismclient.ai;

final class AiFoodHuntBudget {
   static final int init_I = 30;
   static final int load = 5;
   static final int read = 30;
   static final int write = 90;
   private static final long init_J = 1000000000L;

   static AiFoodHuntBudget.Admission admit(long deadlineNanos, long nowNanos, boolean graceUsed) {
      long proposedDeadline = deadlineNanos;
      boolean proposedGrace = graceUsed;
      long required = 35000000000L;
      if (proposedDeadline - nowNanos < required && !graceUsed) {
         proposedDeadline += 30000000000L;
         proposedGrace = true;
      }

      long remaining = proposedDeadline - nowNanos;
      if (remaining < required) {
         return new AiFoodHuntBudget.Admission(0, deadlineNanos, graceUsed);
      }

      int childSeconds = (int)Math.min(90L, remaining / 1000000000L - 5L);
      return new AiFoodHuntBudget.Admission(childSeconds, proposedDeadline, proposedGrace);
   }

   static boolean canInterrupt(boolean interruptible, boolean pendingEffects) {
      return interruptible && !pendingEffects;
   }

   private AiFoodHuntBudget() {
   }

   record Admission(int childSeconds, long deadlineNanos, boolean graceUsed) {
      boolean admitted() {
         return this.childSeconds >= 30;
      }
   }
}
