package autismclient.util;

import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public final class HashUtil {
   private HashUtil.Inner_DataUtil init;

   public <T> int init(List<T> options, Function<T, U.Inner_TaskQueue> option) {
      if (this.init != null) {
         for (int i = 0; i < options.size(); i++) {
            if (this.init.equals(HashUtil.Inner_DataUtil.init(option.apply(options.get(i))))) {
               return i;
            }
         }
      }

      return 0;
   }

   public void init(U.Inner_TaskQueue option) {
      this.init = HashUtil.Inner_DataUtil.init(option);
   }

   public void init() {
      this.init = null;
   }

   private record Inner_DataUtil(BlockPos init_BlockPos, BlockPos load, Direction init_Direction, AABB init_AABB) {
      static HashUtil.Inner_DataUtil init(U.Inner_TaskQueue option) {
         return new HashUtil.Inner_DataUtil(option.init_BlockPos().immutable(), option.load_BlockPos().immutable(), option.init_Direction(), option.init_AABB());
      }
   }
}
