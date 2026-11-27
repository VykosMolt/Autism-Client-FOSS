package autismclient.ai;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiShield {
   static final int init_I = 4;
   static final double init_D = 4.2;

   private AiShield() {
   }

   static BlockPos init(Minecraft mc, Entity shooter) {
      BlockPos feet = mc.player.blockPosition();
      long own1 = feet.asLong();
      long own2 = feet.above().asLong();
      Vec3 eye = mc.player.getEyePosition();
      List<BlockPos> line = new ArrayList<>();
      BlockGetter.traverseBlocks(eye, shooter.getEyePosition(), mc.level, (getter, pos) -> {
         if (pos.asLong() == own1 || pos.asLong() == own2) {
            return null;
         }

         if (Vec3.atCenterOf(pos).distanceTo(eye) > 4.2) {
            return Boolean.TRUE;
         }

         line.add(pos.immutable());
         return null;
      }, getter -> Boolean.FALSE);
      BlockPos best = null;
      int bestDepth = 2147483647;

      for (BlockPos first : line) {
         if (init(mc, first)) {
            BlockPos cell = first;

            for (int depth = 0; depth < 4 && depth < bestDepth && !(Vec3.atCenterOf(cell).distanceTo(eye) > 4.2); depth++) {
               if (load(mc, cell)) {
                  best = cell;
                  bestDepth = depth;
                  break;
               }

               BlockPos under = cell.below();
               if (under.asLong() == own1 || under.asLong() == own2 || !init(mc, under)) {
                  break;
               }

               cell = under;
            }

            if (bestDepth == 0) {
               return best;
            }
         }
      }

      return best;
   }

   static BlockPos load(Minecraft mc, Entity shooter) {
      BlockPos feet = mc.player.blockPosition();
      double dx = mc.player.getX() - shooter.getX();
      double dz = mc.player.getZ() - shooter.getZ();
      Direction away = Math.abs(dx) >= Math.abs(dz) ? (dx > 0.0 ? Direction.EAST : Direction.WEST) : (dz > 0.0 ? Direction.SOUTH : Direction.NORTH);
      BlockPos cell = feet.relative(away);
      if (!mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()) {
         return null;
      }

      if (!init(mc, cell)) {
         return null;
      }

      if (load(mc, cell)) {
         return cell;
      }

      BlockPos under = cell.below();
      return init(mc, under) && load(mc, under) ? under : null;
   }

   static boolean init(Minecraft mc, BlockPos cell) {
      BlockState state = mc.level.getBlockState(cell);
      if (state.getCollisionShape(mc.level, cell).isEmpty()
         && (state.getFluidState().isEmpty() || state.getFluidState().is(FluidTags.WATER))
         && state.canBeReplaced()) {
         return init(mc.player.getBoundingBox()).intersects(new AABB(cell))
            ? false
            : mc.level.getEntitiesOfClass(LivingEntity.class, new AABB(cell), e -> e.isAlive() && !e.isSpectator()).isEmpty();
      } else {
         return false;
      }
   }

   static AABB init(AABB body) {
      return new AABB(body.minX, body.minY, body.minZ, body.maxX, Math.max(body.maxY, body.minY + 1.8), body.maxZ);
   }

   static boolean load(Minecraft mc, BlockPos cell) {
      for (Direction d : Direction.values()) {
         BlockPos side = cell.relative(d);
         if (!mc.level.getBlockState(side).getCollisionShape(mc.level, side).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   static Item init(Minecraft mc) {
      Inventory inventory = mc.player.getInventory();
      Item best = null;
      int bestRank = 2147483647;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (stack.getItem() instanceof BlockItem item && !(item.getBlock() instanceof FallingBlock)) {
            int rank = autismclient.ai.engine.AiRestock.scaffoldRank(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            if (rank >= 0 && rank < bestRank && autismclient.ai.engine.AiRestock.canSpendStack(stack, inventory, true)) {
               best = stack.getItem();
               bestRank = rank;
            }
         }
      }

      return best;
   }

   static boolean init_Z(Minecraft mc, double radius) {
      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof Enemy && entity.isAlive() && !autismclient.ai.engine.AiSight.init(entity) && entity.distanceTo(mc.player) <= radius) {
            return true;
         }
      }

      return false;
   }

   static Entity init_Entity(Minecraft mc, double beyond) {
      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof Mob mob && mob instanceof Enemy && mob.isAlive() && autismclient.ai.engine.AiSight.init(mob)) {
            double d = mob.distanceTo(mc.player);
            if (!(d <= beyond)
               && !(d > 16.5)
               && autismclient.ai.engine.AiSight.hasLineOfSight(
                  mc.level, mob.getEyePosition(), mc.player.getEyePosition(), -9223372036854775808L, -9223372036854775808L
               )) {
               return mob;
            }
         }
      }

      return null;
   }
}
