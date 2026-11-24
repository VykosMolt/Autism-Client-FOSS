package autismclient.ai.b;

import net.minecraft.world.level.GameType;

public final class ByteReader {
   private ByteReader() {
   }

   public static boolean init(boolean assumeFly, GameType mode, boolean mayfly, boolean instabuild) {
      return mayfly && (mode == GameType.CREATIVE && instabuild || mode == GameType.SURVIVAL && assumeFly);
   }

   public static boolean init(boolean flightControls, boolean flightAllowed, boolean flying) {
      return flightControls ? flightAllowed : !flying;
   }
}
