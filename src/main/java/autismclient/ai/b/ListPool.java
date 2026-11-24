package autismclient.ai.b;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

public record ListPool(int index, BlockPos pos, BlockState wanted, ListPool.Inner_DataUtil kind, Item item, Object cell) {
   public boolean fluid() {
      return this.kind == ListPool.Inner_DataUtil.read;
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close;
   }
}
