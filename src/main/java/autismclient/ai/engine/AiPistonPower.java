package autismclient.ai.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.SignalGetter;

public final class AiPistonPower {
   private static final Direction[] init = Direction.values();

   private AiPistonPower() {
   }

   public static boolean init(SignalGetter world, BlockPos position, Direction facing) {
      for (Direction direction : init) {
         if (direction != facing && world.hasSignal(position.relative(direction), direction)) {
            return true;
         }
      }

      if (world.hasSignal(position, Direction.DOWN)) {
         return true;
      }

      BlockPos above = position.above();

      for (Direction direction : init) {
         if (direction != Direction.DOWN && world.hasSignal(above.relative(direction), direction)) {
            return true;
         }
      }

      return false;
   }
}
