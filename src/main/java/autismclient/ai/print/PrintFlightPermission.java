package autismclient.ai.print;

import net.minecraft.world.level.GameType;

public final class PrintFlightPermission {
   private PrintFlightPermission() {
   }

   public static boolean canFly(boolean assumeFly, GameType mode, boolean mayfly, boolean instabuild) {
      return mayfly && (mode == GameType.CREATIVE && instabuild || mode == GameType.SURVIVAL && assumeFly);
   }

   public static boolean flightMatches(boolean flightControls, boolean flightAllowed, boolean flying) {
      return flightControls ? flightAllowed : !flying;
   }
}
