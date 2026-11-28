package autismclient.ai.print;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

public record PrintTarget(int index, BlockPos pos, BlockState wanted, PrintTarget.TargetKind kind, Item item, Object cell) {
   public boolean fluid() {
      return this.kind == PrintTarget.TargetKind.FLUID;
   }

   public enum TargetKind {
      PLACE,
      PARTNER,
      FLUID,
      FLOW,
      SOIL,
      POT,
      CAULDRON,
      COMPOSTER;
   }
}
