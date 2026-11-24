package autismclient.ai.b;

import java.util.function.IntPredicate;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.properties.RailShape;

final class FieldCache {
   static Direction[] init(ListPool target) {
      if (target.wanted().getBlock() instanceof BaseRailBlock rail) {
         Direction[] var10000;
         switch ((RailShape)target.wanted().getValue(rail.getShapeProperty())) {
            case NORTH_EAST:
               var10000 = new Direction[]{Direction.NORTH, Direction.EAST};
               break;
            case NORTH_WEST:
               var10000 = new Direction[]{Direction.NORTH, Direction.WEST};
               break;
            case SOUTH_EAST:
               var10000 = new Direction[]{Direction.SOUTH, Direction.EAST};
               break;
            case SOUTH_WEST:
               var10000 = new Direction[]{Direction.SOUTH, Direction.WEST};
               break;
            default:
               var10000 = null;
         }

         return var10000;
      } else {
         return null;
      }
   }

   static boolean init(int cell, ListPool[] targets, ToIntFunction<BlockPos> index, IntPredicate placed) {
      ListPool target = targets[cell];
      if (!(target.wanted().getBlock() instanceof BaseRailBlock)) {
         return true;
      }

      Direction[] wanted = init(target);
      if (wanted != null && init(target, targets, index)) {
         for (Direction side : wanted) {
            int neighbor = index.applyAsInt(target.pos().relative(side));
            if (neighbor >= 0 && targets[neighbor].wanted().getBlock() instanceof BaseRailBlock && !placed.test(neighbor)) {
               return false;
            }
         }
      }

      for (Direction side : Plane.HORIZONTAL) {
         int neighbor = index.applyAsInt(target.pos().relative(side));
         if (neighbor >= 0 && !placed.test(neighbor)) {
            ListPool center = targets[neighbor];
            Direction[] connections = init(center);
            if (connections != null && init(center, targets, index)) {
               Direction toward = side.getOpposite();
               if (connections[0] != toward && connections[1] != toward) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private static boolean init(ListPool center, ListPool[] targets, ToIntFunction<BlockPos> index) {
      int neighbors = 0;

      for (Direction side : Plane.HORIZONTAL) {
         int i = index.applyAsInt(center.pos().relative(side));
         if (i >= 0 && targets[i].wanted().getBlock() instanceof BaseRailBlock) {
            neighbors++;
         }
      }

      return neighbors > 2;
   }
}
