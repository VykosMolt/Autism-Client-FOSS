package autismclient.ai.a;

import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AiStance {
   private AiStance() {
   }

   public static BlockPos init_BlockPos(Vec3 feet, Function<BlockPos, VoxelShape> shapes) {
      int y = (int)Math.ceil(feet.y - 0.001);
      BlockPos nominal = BlockPos.containing(feet.x, y, feet.z);
      BlockPos best = null;
      double bestDistance = 1.7976931348623157E308;

      for (int x = (int)Math.floor(feet.x - 0.299); x <= (int)Math.floor(feet.x + 0.299); x++) {
         for (int z = (int)Math.floor(feet.z - 0.299); z <= (int)Math.floor(feet.z + 0.299); z++) {
            BlockPos floor = new BlockPos(x, y - 1, z);
            VoxelShape shape = shapes.apply(floor);
            if (!shape.isEmpty() && !(Math.abs(floor.getY() + shape.max(Axis.Y) - feet.y) > 0.02)) {
               double distance = Math.pow(x + 0.5 - feet.x, 2.0) + Math.pow(z + 0.5 - feet.z, 2.0);
               if (distance < bestDistance) {
                  bestDistance = distance;
                  best = floor.above();
               }
            }
         }
      }

      return best == null ? nominal : best;
   }

   public static boolean init_Z(Vec3 feet, Function<BlockPos, VoxelShape> shapes) {
      if (Double.isFinite(feet.x) && Double.isFinite(feet.y) && Double.isFinite(feet.z)) {
         AABB body = new AABB(
            feet.x - 0.3 + 1.0E-7, feet.y + 1.0E-7, feet.z - 0.3 + 1.0E-7, feet.x + 0.3 - 1.0E-7, feet.y + 1.8 - 1.0E-7, feet.z + 0.3 - 1.0E-7
         );

         for (int x = (int)Math.floor(body.minX); x <= (int)Math.floor(body.maxX); x++) {
            for (int y = (int)Math.floor(body.minY) - 1; y <= (int)Math.floor(body.maxY); y++) {
               for (int z = (int)Math.floor(body.minZ); z <= (int)Math.floor(body.maxZ); z++) {
                  BlockPos at = new BlockPos(x, y, z);

                  for (AABB collision : shapes.apply(at).toAabbs()) {
                     if (body.intersects(collision.move(x, y, z))) {
                        return false;
                     }
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static boolean init(VoxelShape feet, VoxelShape head, VoxelShape floor) {
      return feet.isEmpty() && head.isEmpty() && !floor.isEmpty();
   }

   public static BlockPos init(Minecraft mc) {
      LocalPlayer player = mc.player;
      BlockPos block = player.blockPosition();
      if (player.isInWater() && mc.level.getFluidState(block).isEmpty() && mc.level.getFluidState(block.below()).is(FluidTags.WATER)) {
         return block.below();
      }

      if (!player.onGround()) {
         return block;
      }

      BlockPos up = new BlockPos(block.getX(), (int)Math.ceil(player.getY() - 0.001), block.getZ());
      Level level = mc.level;
      if (up.getY() == block.getY()) {
         if (level.getBlockState(up.below()).getCollisionShape(level, up.below()).isEmpty()) {
            BlockPos held = init(level, player, up.getY());
            if (held != null) {
               return held;
            }
         }

         return up;
      } else {
         BlockPos tallFloor = up.below(2);
         VoxelShape highSupport = level.getBlockState(tallFloor).getCollisionShape(level, tallFloor);
         AABB body = player.getBoundingBox();

         for (AABB local : highSupport.toAabbs()) {
            if (!(local.maxY <= 1.0) && !(Math.abs(tallFloor.getY() + local.maxY - player.getY()) >= 0.05)) {
               AABB support = local.move(tallFloor);
               if (body.minX < support.maxX && body.maxX > support.minX && body.minZ < support.maxZ && body.maxZ > support.minZ) {
                  return tallFloor.above();
               }
            }
         }

         BlockPos low = up.below();
         boolean lowOpen = init(
            level.getBlockState(low).getCollisionShape(level, low),
            level.getBlockState(low.above()).getCollisionShape(level, low.above()),
            level.getBlockState(low.below()).getCollisionShape(level, low.below())
         );
         if (lowOpen && player.getY() - low.getY() > 0.6) {
            BlockPos held = init(level, player, up.getY());
            if (held != null) {
               return held;
            }
         }

         return lowOpen ? low : up;
      }
   }

   public static BlockPos init(Level level, Player player, int y) {
      AABB box = player.getBoundingBox();
      BlockPos best = null;
      double bestDistance = 1.7976931348623157E308;

      for (int x = (int)Math.floor(box.minX); x <= (int)Math.floor(box.maxX - 1.0E-7); x++) {
         for (int z = (int)Math.floor(box.minZ); z <= (int)Math.floor(box.maxZ - 1.0E-7); z++) {
            BlockPos floor = new BlockPos(x, y - 1, z);
            VoxelShape shape = level.getBlockState(floor).getCollisionShape(level, floor);
            if (!shape.isEmpty() && !(Math.abs(floor.getY() + shape.max(Axis.Y) - player.getY()) > 0.05)) {
               BlockPos cell = floor.above();
               if (level.getBlockState(cell).getCollisionShape(level, cell).isEmpty()
                  && level.getBlockState(cell.above()).getCollisionShape(level, cell.above()).isEmpty()) {
                  double distance = Math.pow(x + 0.5 - player.getX(), 2.0) + Math.pow(z + 0.5 - player.getZ(), 2.0);
                  if (distance < bestDistance) {
                     bestDistance = distance;
                     best = cell;
                  }
               }
            }
         }
      }

      return best;
   }
}
