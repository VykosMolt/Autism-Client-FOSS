package autismclient.util.macro;

public final class RaceEligibilityChecker {
   private RaceEligibilityChecker() {
   }

   public static boolean init(MacroAction action) {
      return action != null && !(action instanceof DelayAction) && RaceAction.init_Z(action);
   }

   public static boolean init(autismclient.util.AutismMacro macro) {
      if (macro != null && macro.actions != null) {
         for (MacroAction action : macro.actions) {
            if (action != null && action.isEnabled()) {
               return init(action);
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
