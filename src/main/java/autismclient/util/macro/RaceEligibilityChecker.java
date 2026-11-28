package autismclient.util.macro;

public final class RaceEligibilityChecker {
   private RaceEligibilityChecker() {
   }

   public static boolean isEligible(MacroAction action) {
      return action != null && !(action instanceof DelayAction) && RaceAction.isRaceCondition(action);
   }

   public static boolean hasEligible(autismclient.util.AutismMacro macro) {
      if (macro != null && macro.actions != null) {
         for (MacroAction action : macro.actions) {
            if (action != null && action.isEnabled()) {
               return isEligible(action);
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
