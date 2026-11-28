package autismclient.util;

import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public final class StickyPlacementSelector {
   private StickyPlacementSelector.Inner_DataUtil stickyKey;

   public <T> int init(List<T> options, Function<T, BlockPlacementPlanner.SupportCandidate> option) {
      if (this.stickyKey != null) {
         for (int i = 0; i < options.size(); i++) {
            if (this.stickyKey.equals(StickyPlacementSelector.Inner_DataUtil.of(option.apply(options.get(i))))) {
               return i;
            }
         }
      }

      return 0;
   }

   public void init(BlockPlacementPlanner.SupportCandidate option) {
      this.stickyKey = StickyPlacementSelector.Inner_DataUtil.of(option);
   }

   public void init() {
      this.stickyKey = null;
   }

   private record Inner_DataUtil(BlockPos init_BlockPos, BlockPos load, Direction init_Direction, AABB init_AABB) {
      static StickyPlacementSelector.Inner_DataUtil of(BlockPlacementPlanner.SupportCandidate option) {
         return new StickyPlacementSelector.Inner_DataUtil(option.targetCell().immutable(), option.supportPos().immutable(), option.face(), option.faceRect());
      }
   }
}
