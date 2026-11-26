package autismclient.ai.b;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity.AnimationStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PrintContainerAccess {
   private PrintContainerAccess() {
   }

   public static AABB init(BlockState state, BlockPos at) {
      return Shulker.getProgressDeltaAabb(1.0F, state.getValue(ShulkerBoxBlock.FACING), 0.0F, 0.5F, Vec3.atBottomCenterOf(at)).deflate(1.0E-6);
   }

   static boolean init(PrintView view, BlockPos at, BlockState state, AABB body) {
      if (!(state.getBlock() instanceof ShulkerBoxBlock)) {
         return true;
      }

      AABB lid = init(state, at);
      return (body == null || !lid.intersects(body)) && view.init_Z_AABB(lid);
   }

   public static boolean init(Level level, BlockPos at) {
      BlockState state = level.getBlockState(at);
      if (!(state.getBlock() instanceof ShulkerBoxBlock)) {
         return true;
      } else {
         return level.getBlockEntity(at) instanceof ShulkerBoxBlockEntity box
            ? box.getAnimationStatus() != AnimationStatus.CLOSED || level.noCollision(init(state, at))
            : false;
      }
   }
}
