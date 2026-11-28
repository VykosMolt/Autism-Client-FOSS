package autismclient.util;

import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public final class StickyPlacementSelector {
   private StickyPlacementSelector.StickyKey stickyKey;

   public <T> int startIndex(List<T> options, Function<T, BlockPlacementPlanner.SupportCandidate> option) {
      if (this.stickyKey != null) {
         for (int i = 0; i < options.size(); i++) {
            if (this.stickyKey.equals(StickyPlacementSelector.StickyKey.of(option.apply(options.get(i))))) {
               return i;
            }
         }
      }

      return 0;
   }

   public void remember(BlockPlacementPlanner.SupportCandidate option) {
      this.stickyKey = StickyPlacementSelector.StickyKey.of(option);
   }

   public void clear() {
      this.stickyKey = null;
   }

   private record StickyKey(BlockPos init_BlockPos, BlockPos load, Direction init_Direction, AABB init_AABB) {
      static StickyPlacementSelector.StickyKey of(BlockPlacementPlanner.SupportCandidate option) {
         return new StickyPlacementSelector.StickyKey(option.targetCell().immutable(), option.supportPos().immutable(), option.face(), option.faceRect());
      }
   }
}
