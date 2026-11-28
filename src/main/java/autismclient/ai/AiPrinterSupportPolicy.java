package autismclient.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class AiPrinterSupportPolicy {
   private AiPrinterSupportPolicy() {
   }

   public static boolean isPlainBlock(BlockState state) {
      Block block = state.getBlock();
      return state.getProperties().isEmpty()
         && !state.hasBlockEntity()
         && !state.isSignalSource()
         && state.getFluidState().isEmpty()
         && !(block instanceof FallingBlock)
         && !AiPrintRules.isMachineBlock(block)
         && block != Blocks.MAGMA_BLOCK
         && block != Blocks.SLIME_BLOCK
         && block != Blocks.HONEY_BLOCK
         && block != Blocks.ICE
         && block != Blocks.PACKED_ICE
         && block != Blocks.BLUE_ICE
         && block.asItem() instanceof BlockItem item
         && item.getBlock() == block
         && Block.isShapeFullBlock(state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
   }
}
