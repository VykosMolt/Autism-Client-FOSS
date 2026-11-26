package autismclient.util.e;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class LeafDupeValidator {
   public static final double init_D = 0.05;
   public static final double load = 0.2;
   private static final Set<Block> init_Set = Set.of(
      Blocks.LEVER,
      Blocks.NOTE_BLOCK,
      Blocks.REPEATER,
      Blocks.COMPARATOR,
      Blocks.DAYLIGHT_DETECTOR,
      Blocks.CAKE,
      Blocks.COMPOSTER,
      Blocks.RESPAWN_ANCHOR,
      Blocks.JUKEBOX,
      Blocks.LECTERN,
      Blocks.BELL,
      Blocks.CRAFTING_TABLE,
      Blocks.GRINDSTONE,
      Blocks.STONECUTTER,
      Blocks.LOOM,
      Blocks.CARTOGRAPHY_TABLE,
      Blocks.SMITHING_TABLE,
      Blocks.ENCHANTING_TABLE,
      Blocks.DRAGON_EGG,
      Blocks.TNT,
      Blocks.PUMPKIN,
      Blocks.BEE_NEST,
      Blocks.BEEHIVE,
      Blocks.SWEET_BERRY_BUSH,
      Blocks.CAVE_VINES,
      Blocks.CAVE_VINES_PLANT,
      Blocks.COMMAND_BLOCK,
      Blocks.CHAIN_COMMAND_BLOCK,
      Blocks.REPEATING_COMMAND_BLOCK,
      Blocks.STRUCTURE_BLOCK,
      Blocks.JIGSAW,
      Blocks.BEACON,
      Blocks.CHISELED_BOOKSHELF,
      Blocks.DECORATED_POT,
      Blocks.VAULT,
      Blocks.TRIAL_SPAWNER,
      Blocks.CRAFTER,
      Blocks.END_PORTAL_FRAME,
      Blocks.REDSTONE_WIRE
   );

   private LeafDupeValidator() {
   }

   public static BlockPos init(String raw) {
      if (raw != null && !raw.isBlank()) {
         String[] parts = raw.trim().split("[\\s,]+");
         if (parts.length != 3) {
            return null;
         }

         try {
            return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
         } catch (NumberFormatException ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static String init(BlockPos pos) {
      return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
   }

   public static String init_Str_BlockState(BlockState state, BlockState below, boolean loaded) {
      if (!loaded) {
         return "chunk is not loaded.";
      } else if (!state.is(Blocks.SMALL_DRIPLEAF)) {
         return "that is " + state.getBlock().getName().getString() + ", not a small dripleaf.";
      } else if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.UPPER) {
         return "that is the lower half of a small dripleaf, it must be the upper half.";
      } else {
         return below.is(Blocks.SMALL_DRIPLEAF) && below.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
            ? "that dripleaf still has its lower half, it must be an upper half only."
            : null;
      }
   }

   public static String init_Str_BlockState_2(BlockState state, boolean loaded, boolean menu) {
      if (!loaded) {
         return "chunk is not loaded.";
      } else if (state.isAir()) {
         return "it is air.";
      } else if (!state.getFluidState().isEmpty()) {
         return "it contains a fluid.";
      } else if (state.getBlock() instanceof BonemealableBlock) {
         return "it is bone-mealable, use a plain block instead.";
      } else if (state.hasBlockEntity()) {
         return "it has a block entity.";
      } else if (menu) {
         return "it opens a menu.";
      } else {
         return !state.is(BlockTags.DOORS)
               && !state.is(BlockTags.TRAPDOORS)
               && !state.is(BlockTags.FENCE_GATES)
               && !state.is(BlockTags.BUTTONS)
               && !state.is(BlockTags.BEDS)
               && !state.is(BlockTags.ANVIL)
               && !state.is(BlockTags.CAULDRONS)
               && !state.is(BlockTags.CAMPFIRES)
               && !state.is(BlockTags.CANDLES)
               && !state.is(BlockTags.CANDLE_CAKES)
               && !state.is(BlockTags.FLOWER_POTS)
               && !init_Set.contains(state.getBlock())
            ? null
            : "it is interactive, use a plain block instead.";
      }
   }

   public static float[] init_FArr_Vec3(Vec3 eye, Vec3 point, float lastYaw) {
      double dx = point.x - eye.x;
      double dy = point.y - eye.y;
      double dz = point.z - eye.z;
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
      yaw = lastYaw + Mth.wrapDegrees(yaw - lastYaw);
      return new float[]{yaw, Mth.clamp(pitch, -90.0F, 90.0F)};
   }

   public static Vec3 init(Vec3 eye, BlockPos pos) {
      AABB box = new AABB(pos).deflate(0.2);
      return new Vec3(Mth.clamp(eye.x, box.minX, box.maxX), Mth.clamp(eye.y, box.minY, box.maxY), Mth.clamp(eye.z, box.minZ, box.maxZ));
   }

   public static boolean init(Vec3 eye, float yaw, float pitch, BlockPos pos, double reach) {
      AABB box = new AABB(pos);
      if (box.contains(eye)) {
         return true;
      }

      Vec3 end = eye.add(Vec3.directionFromRotation(pitch, yaw).scale(reach - 0.05));
      return box.deflate(0.05).clip(eye, end).isPresent();
   }

   public static boolean init(Vec3 eye, float[][] looks, BlockPos pos, double reach) {
      for (float[] look : looks) {
         if (init(eye, look[0], look[1], pos, reach)) {
            return true;
         }
      }

      return false;
   }

   public static float[] init(Vec3 eye, float lastYaw, float lastPitch, BlockPos reset, BlockPos leaf, double reach) {
      if (reset != null && leaf != null) {
         float[] best = null;
         double bestCost = 1.7976931348623157E308;

         for (BlockPos pos : new BlockPos[]{reset, leaf}) {
            for (int x = 0; x < 5; x++) {
               for (int y = 0; y < 5; y++) {
                  for (int z = 0; z < 5; z++) {
                     Vec3 point = new Vec3(pos.getX() + 0.1 + 0.2 * x, pos.getY() + 0.1 + 0.2 * y, pos.getZ() + 0.1 + 0.2 * z);
                     float[] look = init_FArr_Vec3(eye, point, lastYaw);
                     if (init(eye, look[0], look[1], reset, reach) && init(eye, look[0], look[1], leaf, reach)) {
                        double cost = Math.abs(look[0] - lastYaw) + Math.abs(look[1] - lastPitch);
                        if (cost < bestCost) {
                           bestCost = cost;
                           best = look;
                        }
                     }
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   public static Direction init(Vec3 eye, BlockPos pos, Direction preferred, boolean avoidFluids, Predicate<BlockPos> fluidAt) {
      Direction best = null;
      double bestDistance = 0.0;

      for (Direction face : Direction.values()) {
         double var10000;
         switch (face) {
            case UP:
               var10000 = eye.y - (pos.getY() + 1);
               break;
            case DOWN:
               var10000 = pos.getY() - eye.y;
               break;
            case SOUTH:
               var10000 = eye.z - (pos.getZ() + 1);
               break;
            case NORTH:
               var10000 = pos.getZ() - eye.z;
               break;
            case EAST:
               var10000 = eye.x - (pos.getX() + 1);
               break;
            case WEST:
               var10000 = pos.getX() - eye.x;
               break;
            default:
               throw new MatchException(null, null);
         }

         double distance = var10000;
         if (!(distance < 0.0) && (!avoidFluids || !fluidAt.test(pos.relative(face)))) {
            if (face == preferred) {
               return face;
            }

            if (best == null || distance > bestDistance) {
               best = face;
               bestDistance = distance;
            }
         }
      }

      return best == null ? preferred : best;
   }

   public static Vec3 init_Vec3_BlockPos(BlockPos pos, Direction face, double height) {
      return face == Direction.UP
         ? new Vec3(pos.getX() + 0.5, pos.getY() + height, pos.getZ() + 0.5)
         : Vec3.atCenterOf(pos).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
   }

   public static double init(Vec3 eye, AABB box) {
      double dx = Math.max(box.minX - eye.x, Math.max(0.0, eye.x - box.maxX));
      double dy = Math.max(box.minY - eye.y, Math.max(0.0, eye.y - box.maxY));
      double dz = Math.max(box.minZ - eye.z, Math.max(0.0, eye.z - box.maxZ));
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   public static String init(
      Vec3 serverEye, Vec3 liveEye, List<BlockPos> positions, double reach, Function<BlockPos, AABB> bounds, BiPredicate<BlockPos, Vec3> visible
   ) {
      for (BlockPos pos : positions) {
         AABB box = bounds.apply(pos);
         if (!(init(serverEye, box) > reach) && !(init(liveEye, box) > reach)) {
            if (visible.test(pos, serverEye) && (!(liveEye.distanceToSqr(serverEye) > 1.0E-8) || visible.test(pos, liveEye))) {
               continue;
            }

            return "Blocks obstructed";
         }

         return "Out of reach";
      }

      return null;
   }

   public static <T> List<T> init_List_List(List<T> candidates, long page, int limit) {
      if (candidates.size() <= limit) {
         return candidates;
      }

      int start = (int)((long)Math.floorMod(page, candidates.size()) * limit % candidates.size());
      List<T> result = new ArrayList<>(limit);

      for (int i = 0; i < limit; i++) {
         result.add(candidates.get((start + i) % candidates.size()));
      }

      return result;
   }
}
