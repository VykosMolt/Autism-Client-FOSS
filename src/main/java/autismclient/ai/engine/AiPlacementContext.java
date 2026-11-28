package autismclient.ai.engine;

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

public final class AiPlacementContext extends BlockPlaceContext {
   private final float yaw;
   private final float pitch;
   private final float headYaw;
   private final boolean secondaryUse;

   static boolean satisfies(Item item, PlacementGoal.PlaceConstraint constraint, BlockPos target, Function<BlockPos, BlockState> states) {
      if (constraint == null) {
         return false;
      }

      if (!constraint.crouchForLook() && !(item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ChestBlock)) {
         for (Direction direction : Direction.values()) {
            if (constraint.acceptsFace(direction.getOpposite())) {
               BlockPos support = target.relative(direction);
               BlockState state = states.apply(support);
               if (state.hasBlockEntity()
                  || BlockPlacementPlanner.requiresSneakToPlace(state, support)
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

   AiPlacementContext(Player player, ItemStack stack, BlockHitResult hit, Vec3 eye) {
      super(player, InteractionHand.MAIN_HAND, stack, hit);
      Vec3 ray = hit.getLocation().subtract(eye);
      this.yaw = (float)(Math.toDegrees(Math.atan2(ray.z, ray.x)) - 90.0);
      this.pitch = (float)(-Math.toDegrees(Math.atan2(ray.y, Math.hypot(ray.x, ray.z))));
      this.headYaw = this.yaw;
      this.secondaryUse = true;
   }

   public AiPlacementContext(Player player, ItemStack stack, BlockHitResult hit, float yaw, float pitch, boolean secondary) {
      this(player, stack, hit, yaw, yaw, pitch, secondary);
   }

   public AiPlacementContext(Player player, ItemStack stack, BlockHitResult hit, float yaw, float headYaw, float pitch, boolean secondary) {
      super(player, InteractionHand.MAIN_HAND, stack, hit);
      this.yaw = yaw;
      this.headYaw = headYaw;
      this.pitch = pitch;
      this.secondaryUse = secondary;
   }

   @Override
   public Direction getHorizontalDirection() {
      return Direction.fromYRot(this.yaw);
   }

   @Override
   public float getRotation() {
      return this.yaw;
   }

   @Override
   public Direction getNearestLookingDirection() {
      return facingOrder(this.headYaw, this.pitch)[0];
   }

   @Override
   public Direction getNearestLookingVerticalDirection() {
      return this.pitch < 0.0F ? Direction.UP : Direction.DOWN;
   }

   @Override
   public boolean isSecondaryUseActive() {
      return this.secondaryUse;
   }

   @Override
   public Direction[] getNearestLookingDirections() {
      Direction[] directions = facingOrder(this.headYaw, this.pitch);
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

   public static Direction[] facingOrder(float yaw, float pitch) {
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
            return directionsFrom(y, x, z);
         } else {
            return zMag > yMag ? directionsFrom(x, z, y) : directionsFrom(x, y, z);
         }
      } else if (yMag > zMag) {
         return directionsFrom(y, z, x);
      } else {
         return xMag > yMag ? directionsFrom(z, x, y) : directionsFrom(z, y, x);
      }
   }

   private static Direction[] directionsFrom(Direction first, Direction second, Direction third) {
      return new Direction[]{first, second, third, third.getOpposite(), second.getOpposite(), first.getOpposite()};
   }
}
