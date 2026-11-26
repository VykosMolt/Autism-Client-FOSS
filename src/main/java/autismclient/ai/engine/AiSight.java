package autismclient.ai.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AiSight {
   public static final double init_D = 16.0;
   public static final double load_D = 1.62;
   public static final long init_J = -9223372036854775808L;
   public static final double read = 2.0;
   public static final long load_J = 90000000000L;
   private static final Map<Integer, Long> init_Map = new ConcurrentHashMap<>();

   private AiSight() {
   }

   public static boolean init(Entity entity) {
      return entity instanceof AbstractSkeleton skeleton && !(entity instanceof WitherSkeleton) && skeleton.getMainHandItem().is(Items.BOW)
         || entity instanceof Pillager
         || entity instanceof Witch
         || entity instanceof Blaze
         || entity instanceof Ghast;
   }

   public static boolean init_Z_BlockGetter_3(BlockGetter level, Vec3 from, Vec3 to, long ownA, long ownB) {
      if (from.distanceToSqr(to) < 1.0E-7) {
         return true;
      }

      Boolean blocked = BlockGetter.traverseBlocks(from, to, level, (getter, pos) -> {
         long key = pos.asLong();
         if (key != ownA && key != ownB) {
            BlockState state = getter.getBlockState(pos);
            if (state.isAir()) {
               return null;
            }

            VoxelShape shape = state.getCollisionShape(getter, pos);
            return !shape.isEmpty() && shape.clip(from, to, pos) != null ? Boolean.TRUE : null;
         } else {
            return null;
         }
      }, getter -> Boolean.FALSE);
      return !Boolean.TRUE.equals(blocked);
   }

   public static Vec3 init(int x, int y, int z) {
      return new Vec3(x + 0.5, y + 1.62, z + 0.5);
   }

   public static List<Vec3> init(Vec3 eye, Vec3 toward) {
      double dx = toward.x - eye.x;
      double dz = toward.z - eye.z;
      double len = Math.hypot(dx, dz);
      if (len < 0.001) {
         return List.of(eye);
      }

      double px = -dz / len;
      double pz = dx / len;
      return List.of(eye, new Vec3(eye.x + px, eye.y, eye.z + pz), new Vec3(eye.x - px, eye.y, eye.z - pz));
   }

   public static boolean init_Z_BlockGetter(BlockGetter level, List<Vec3> eyes, int x, int y, int z) {
      Vec3 eye = init(x, y, z);
      long feet = BlockPos.asLong(x, y, z);
      long head = BlockPos.asLong(x, y + 1, z);

      for (Vec3 shooter : eyes) {
         if (!(shooter.distanceToSqr(eye) > 289.0)) {
            for (Vec3 point : init(shooter, eye)) {
               if (init_Z_BlockGetter_3(level, point, eye, feet, head)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static List<BlockPos> init(BlockGetter level, BlockPos feet, List<Vec3> eyes, int horizontal, int up, int down, Set<Long> rejected, int max) {
      return init(level, feet, eyes, List.of(), 0.0, false, horizontal, up, down, rejected, max);
   }

   public static List<BlockPos> init(
      BlockGetter level,
      BlockPos feet,
      List<Vec3> eyes,
      List<Vec3> keepAway,
      double distance,
      boolean openOnly,
      int horizontal,
      int up,
      int down,
      Set<Long> rejected,
      int max
   ) {
      return init(level, feet, eyes, keepAway, distance, openOnly, horizontal, up, down, rejected, max, false);
   }

   public static List<BlockPos> init(
      BlockGetter level,
      BlockPos feet,
      List<Vec3> eyes,
      List<Vec3> keepAway,
      double distance,
      boolean openOnly,
      int horizontal,
      int up,
      int down,
      Set<Long> rejected,
      int max,
      boolean allowSurfaceWater
   ) {
      List<BlockPos> found = new ArrayList<>();
      MutableBlockPos cell = new MutableBlockPos();

      for (int dy = -down; dy <= up; dy++) {
         for (int dx = -horizontal; dx <= horizontal; dx++) {
            for (int dz = -horizontal; dz <= horizontal; dz++) {
               int x = feet.getX() + dx;
               int y = feet.getY() + dy;
               int z = feet.getZ() + dz;
               if (!rejected.contains(BlockPos.asLong(x, y, z)) && !init_Z_List(keepAway, distance, x, y, z)) {
                  boolean water = allowSurfaceWater && read(level, cell, x, y, z);
                  if ((water || load(level, cell, x, y, z))
                     && (!openOnly || init_Z_BlockGetter_2(level, cell, x, y, z))
                     && !init_Z_BlockGetter(level, eyes, x, y, z)
                     && (!water || !init_Z_BlockGetter(level, eyes, x, y + 1, z))) {
                     found.add(new BlockPos(x, y, z));
                  }
               }
            }
         }
      }

      found.sort(Comparator.comparingDouble(p -> init(feet, p)));
      return found.size() > max ? new ArrayList<>(found.subList(0, max)) : found;
   }

   private static boolean read(BlockGetter level, MutableBlockPos cell, int x, int y, int z) {
      BlockState feet = level.getBlockState(cell.set(x, y, z));
      if (feet.getFluidState().is(FluidTags.WATER) && feet.getCollisionShape(level, cell).isEmpty()) {
         BlockState head = level.getBlockState(cell.set(x, y + 1, z));
         return head.getFluidState().isEmpty() && head.getCollisionShape(level, cell).isEmpty()
            ? !level.getFluidState(cell.set(x, y - 1, z)).is(FluidTags.LAVA)
            : false;
      } else {
         return false;
      }
   }

   static boolean init_Z_List(List<Vec3> points, double distance, int x, int y, int z) {
      for (Vec3 p : points) {
         double dx = x + 0.5 - p.x;
         double dy = y + 1 - p.y;
         double dz = z + 0.5 - p.z;
         if (dx * dx + dy * dy + dz * dz < distance * distance) {
            return true;
         }
      }

      return false;
   }

   public static double init(BlockPos feet, BlockPos stance) {
      double dx = stance.getX() - feet.getX();
      double dz = stance.getZ() - feet.getZ();
      double dy = stance.getY() - feet.getY();
      return Math.hypot(dx, dz) + (dy > 0.0 ? dy * 2.0 : -dy * 1.2);
   }

   public static double init(BlockGetter level, BlockPos feet, BlockPos stance) {
      double cost = init(feet, stance);
      if (stance.getY() < feet.getY()) {
         return cost;
      }

      MutableBlockPos cell = new MutableBlockPos();

      for (int h = 0; h <= 1; h++) {
         if (!level.getBlockState(cell.set(stance.getX(), stance.getY() + h, stance.getZ())).getCollisionShape(level, cell).isEmpty()) {
            cost += 2.0;
         }
      }

      return cost;
   }

   public static void init(int id) {
      long now = System.nanoTime();
      init_Map.values().removeIf(t -> now - t > 90000000000L);
      init_Map.put(id, now);
   }

   public static boolean load(Entity shooter) {
      Long at = shooter == null ? null : init_Map.get(shooter.getId());
      return at != null && System.nanoTime() - at <= 90000000000L;
   }

   static boolean init_Z_BlockGetter_2(BlockGetter level, MutableBlockPos cell, int x, int y, int z) {
      for (int h = 0; h <= 1; h++) {
         if (!level.getBlockState(cell.set(x, y + h, z)).getCollisionShape(level, cell).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   static boolean load(BlockGetter level, MutableBlockPos cell, int x, int y, int z) {
      BlockState floor = level.getBlockState(cell.set(x, y - 1, z));
      if (floor.getFluidState().isEmpty() && !floor.getCollisionShape(level, cell).isEmpty()) {
         if (!(floor.getBlock() instanceof MagmaBlock) && !(floor.getBlock() instanceof CampfireBlock)) {
            for (int h = 0; h <= 1; h++) {
               for (Direction side : Plane.HORIZONTAL) {
                  if (!level.getFluidState(cell.set(x + side.getStepX(), y + h, z + side.getStepZ())).isEmpty()) {
                     return false;
                  }
               }
            }

            for (int dx = -1; dx <= 1; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                  for (int h = 0; h <= 2; h++) {
                     if (level.getFluidState(cell.set(x + dx, y + h, z + dz)).is(FluidTags.LAVA)) {
                        return false;
                     }
                  }
               }
            }

            for (int dx = -1; dx <= 1; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                  if (dx != 0 || dz != 0) {
                     boolean open = true;

                     for (int dy = 0; dy >= -5 && open; dy--) {
                        BlockState c = level.getBlockState(cell.set(x + dx, y + dy, z + dz));
                        if (!c.getCollisionShape(level, cell).isEmpty() || c.getFluidState().is(FluidTags.WATER)) {
                           open = false;
                        }
                     }

                     if (open) {
                        return false;
                     }
                  }
               }
            }

            for (int h = 0; h <= 1; h++) {
               BlockState state = level.getBlockState(cell.set(x, y + h, z));
               if (!state.getFluidState().isEmpty()) {
                  return false;
               }

               if (state.getCollisionShape(level, cell).isEmpty()) {
                  if (state.getBlock() instanceof FireBlock
                     || state.getBlock() instanceof WebBlock
                     || state.getBlock() instanceof SweetBerryBushBlock
                     || state.getBlock() instanceof PowderSnowBlock) {
                     return false;
                  }
               } else if (state.getDestroySpeed(level, cell) < 0.0F || state.hasBlockEntity() || state.getBlock() instanceof FallingBlock) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static AiPathfinder.Inner_PoolEntry init(BlockGetter level, List<Vec3> shooterEyes) {
      List<Double> charges = new ArrayList<>();

      for (int i = 0; i < shooterEyes.size(); i++) {
         charges.add(10.0);
      }

      return init(level, shooterEyes, charges);
   }

   public static AiPathfinder.Inner_PoolEntry init(BlockGetter level, List<Vec3> shooterEyes, List<Double> shooterCharges) {
      List<Vec3> eyes = List.copyOf(shooterEyes);
      List<Double> charges = List.copyOf(shooterCharges);
      if (eyes.isEmpty()) {
         return AiPathfinder.Inner_PoolEntry.write;
      }

      Map<Long, Double> memo = new ConcurrentHashMap<>();
      double range = 289.0;
      return (x, y, z) -> {
         boolean near = false;

         for (Vec3 eye : eyes) {
            double dx = x + 0.5 - eye.x;
            double dy = y + 1.62 - eye.y;
            double dz = z + 0.5 - eye.z;
            if (dx * dx + dy * dy + dz * dz <= range) {
               near = true;
               break;
            }
         }

         if (!near) {
            return 0.0;
         }

         Double seen = memo.get(BlockPos.asLong(x, y, z));
         if (seen == null) {
            Vec3 target = init(x, y, z);
            long feet = BlockPos.asLong(x, y, z);
            long head = BlockPos.asLong(x, y + 1, z);
            double charge = 0.0;

            try {
               for (int i = 0; i < eyes.size(); i++) {
                  Vec3 eye = eyes.get(i);
                  double c = i < charges.size() ? charges.get(i) : 10.0;
                  if (c > charge && eye.distanceToSqr(target) <= range && init_Z_BlockGetter_3(level, eye, target, feet, head)) {
                     charge = c;
                  }
               }
            } catch (RuntimeException raced) {
               charge = 0.0;
            }

            seen = charge;
            memo.put(BlockPos.asLong(x, y, z), seen);
         }

         return seen;
      };
   }
}
