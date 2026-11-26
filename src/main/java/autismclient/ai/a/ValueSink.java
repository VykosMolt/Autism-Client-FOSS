package autismclient.ai.a;

import autismclient.util.BlockPlacementPlanner;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class ValueSink extends BlockPlaceContext {
   private final float init_F;
   private final float load;
   private final float read;
   private final boolean init_Z;

   static boolean init(Item item, u.Inner_DataUtil constraint, BlockPos target, Function<BlockPos, BlockState> states) {
      if (constraint == null) {
         return false;
      }

      if (!constraint.crouchForLook() && !(item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ChestBlock)) {
         for (Direction direction : Direction.values()) {
            if (constraint.acceptsFace(direction.getOpposite())) {
               BlockPos support = target.relative(direction);
               BlockState state = states.apply(support);
               if (state.hasBlockEntity()
                  || BlockPlacementPlanner.init_Z(state, support)
                  || state.getBlock() instanceof DoorBlock
                  || state.getBlock() instanceof TrapDoorBlock
                  || state.getBlock() instanceof FenceGateBlock) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   ValueSink(Player player, ItemStack stack, BlockHitResult hit, Vec3 eye) {
      super(player, InteractionHand.MAIN_HAND, stack, hit);
      Vec3 ray = hit.getLocation().subtract(eye);
      this.init_F = (float)(Math.toDegrees(Math.atan2(ray.z, ray.x)) - 90.0);
      this.load = (float)(-Math.toDegrees(Math.atan2(ray.y, Math.hypot(ray.x, ray.z))));
      this.read = this.init_F;
      this.init_Z = true;
   }

   public ValueSink(Player player, ItemStack stack, BlockHitResult hit, float yaw, float pitch, boolean secondary) {
      this(player, stack, hit, yaw, yaw, pitch, secondary);
   }

   public ValueSink(Player player, ItemStack stack, BlockHitResult hit, float yaw, float headYaw, float pitch, boolean secondary) {
      super(player, InteractionHand.MAIN_HAND, stack, hit);
      this.init_F = yaw;
      this.read = headYaw;
      this.load = pitch;
      this.init_Z = secondary;
   }

   @Override
   public Direction getHorizontalDirection() {
      return Direction.fromYRot(this.init_F);
   }

   @Override
   public float getRotation() {
      return this.init_F;
   }

   @Override
   public Direction getNearestLookingDirection() {
      return init(this.read, this.load)[0];
   }

   @Override
   public Direction getNearestLookingVerticalDirection() {
      return this.load < 0.0F ? Direction.UP : Direction.DOWN;
   }

   @Override
   public boolean isSecondaryUseActive() {
      return this.init_Z;
   }

   @Override
   public Direction[] getNearestLookingDirections() {
      Direction[] directions = init(this.read, this.load);
      if (!this.replacingClickedOnBlock()) {
         Direction first = this.getClickedFace().getOpposite();
         int index = 0;

         while (directions[index] != first) {
            index++;
         }

         System.arraycopy(directions, 0, directions, 1, index);
         directions[0] = first;
      }

      return directions;
   }

   public static Direction[] init(float yaw, float pitch) {
      float pitchSin = Mth.sin(pitch * 0.017453292F);
      float pitchCos = Mth.cos(pitch * 0.017453292F);
      float yawSin = Mth.sin(-yaw * 0.017453292F);
      float yawCos = Mth.cos(-yaw * 0.017453292F);
      float xYaw = Math.abs(yawSin);
      float yMag = Math.abs(pitchSin);
      float zYaw = Math.abs(yawCos);
      float xMag = xYaw * pitchCos;
      float zMag = zYaw * pitchCos;
      Direction x = yawSin > 0.0F ? Direction.EAST : Direction.WEST;
      Direction y = pitchSin < 0.0F ? Direction.UP : Direction.DOWN;
      Direction z = yawCos > 0.0F ? Direction.SOUTH : Direction.NORTH;
      if (xYaw > zYaw) {
         if (yMag > xMag) {
            return init(y, x, z);
         } else {
            return zMag > yMag ? init(x, z, y) : init(x, y, z);
         }
      } else if (yMag > zMag) {
         return init(y, z, x);
      } else {
         return xMag > yMag ? init(z, x, y) : init(z, y, x);
      }
   }

   private static Direction[] init(Direction first, Direction second, Direction third) {
      return new Direction[]{first, second, third, third.getOpposite(), second.getOpposite(), first.getOpposite()};
   }
}
