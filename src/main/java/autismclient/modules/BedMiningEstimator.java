package autismclient.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

final class BedMiningEstimator {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final Map<BlockPos, BedMiningEstimator.Inner_DataUtil> init_Map = new LinkedHashMap<>();
   private static Object init_Obj;

   static void init() {
      init_Map.clear();
      init_Obj = null;
   }

   static BedBreachRouteSolver.Inner_BufferHelper init(BlockPos bed, boolean tools, BlockPos mining, float progress) {
      return init(bed, tools, mining, progress, 0.0);
   }

   static BedBreachRouteSolver.Inner_BufferHelper load(BlockPos bed, boolean tools, BlockPos mining, float progress) {
      return init(bed, tools, mining, progress, 8.0);
   }

   private static BedBreachRouteSolver.Inner_BufferHelper init(BlockPos bed, boolean tools, BlockPos mining, float progress, double coverDepth) {
      if (init_Obj != init_Minecraft.level) {
         init();
         init_Obj = init_Minecraft.level;
      }

      boolean ideal = !BedEspModule.init_ClassCache_CachePo().init_Z();
      List<ItemStack> recommended = ideal ? BedEspModule.load_List() : List.of();
      Map<BlockPos, Double> actualCosts = ideal ? new HashMap<>() : null;
      Map<BlockState, Double> idealCosts = ideal ? new HashMap<>() : null;
      BlockState state = init_Minecraft.level.getBlockState(bed);
      if (!(state.getBlock() instanceof BedBlock)) {
         return null;
      }

      BlockPos other = bed.relative(BedBlock.getConnectedDirection(state));
      BlockState partner = init_Minecraft.level.getBlockState(other);
      boolean paired = partner.is(state.getBlock())
         && partner.getValue(BedBlock.PART) != state.getValue(BedBlock.PART)
         && partner.getValue(BedBlock.FACING) == state.getValue(BedBlock.FACING);
      Vec3 eye = init_Minecraft.player.getEyePosition();
      AABB bedBounds = new AABB(bed).minmax(new AABB(paired ? other : bed));
      double distance = init_Minecraft.player.blockInteractionRange() + coverDepth;
      if (bedBounds.distanceToSqr(eye) > distance * distance) {
         return null;
      }

      AABB bounds = new AABB(bed).minmax(new AABB(paired ? other : bed)).minmax(new AABB(eye, eye)).inflate(0.01);
      ArrayList<BedBreachRouteSolver.Inner_DataUtil> cells = new ArrayList<>();
      HashMap<BlockState, BedBreachRouteSolver.Inner_DataUtil> costs = new HashMap<>();

      for (BlockPos mutable : BlockPos.betweenClosed(
         BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ), BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ)
      )) {
         BlockPos p = mutable.immutable();
         if (!init_Minecraft.level.isLoaded(p)) {
            cells.add(new BedBreachRouteSolver.Inner_DataUtil(p, List.of(new AABB(p)), 1.0 / 0.0, -1, false));
         } else {
            BlockState block = init_Minecraft.level.getBlockState(p);
            if (!block.isAir()) {
               VoxelShape shape = block.getShape(init_Minecraft.level, p, CollisionContext.of(init_Minecraft.player));
               if (!shape.isEmpty()) {
                  List<AABB> boxes = shape.toAabbs().stream().map(b -> b.move(p)).toList();
                  boolean target = p.equals(bed) || paired && p.equals(other);
                  boolean breakable = target || BedEspModule.init_Z_Block(block.getBlock());
                  int slot = init_Minecraft.player.getInventory().getSelectedSlot();
                  double ticks = 1.0 / 0.0;
                  if (breakable) {
                     BedBreachRouteSolver.Inner_DataUtil cost = costs.get(block);
                     if (cost == null) {
                        if (tools) {
                           slot = init(block);
                        }

                        ticks = init(block, p, init_Minecraft.player.getInventory().getItem(slot));
                        cost = new BedBreachRouteSolver.Inner_DataUtil(p, List.of(), ticks, slot, target);
                        costs.put(block, cost);
                     }

                     ticks = cost.init_D();
                     slot = cost.init_I();
                  }

                  if (ideal && breakable) {
                     actualCosts.put(p, ticks);
                     ticks = idealCosts.computeIfAbsent(block, b -> recommended.stream().mapToDouble(stack -> init(b, p, stack)).min().orElse(1.0 / 0.0));
                  }

                  cells.add(new BedBreachRouteSolver.Inner_DataUtil(p, boxes, ticks, slot, target));
               }
            }
         }
      }

      double reach = init_Minecraft.player.blockInteractionRange();
      Vec3 preferred = eye.add(Vec3.directionFromRotation(init_Minecraft.player.getXRot(), init_Minecraft.player.getYRot()).scale(reach));
      BedMiningEstimator.Inner_DataUtil cached = init_Map.get(bed);
      if (cached == null || !eye.equals(cached.init_Vec3) || reach != cached.init_D || !cells.equals(cached.init_List)) {
         cached = new BedMiningEstimator.Inner_DataUtil(eye, reach, List.copyOf(cells), BedBreachRouteSolver.init_DataPipe_CachePool(cells, eye, reach, preferred));
         if (init_Map.size() >= 16 && !init_Map.containsKey(bed)) {
            init_Map.remove(init_Map.keySet().iterator().next());
         }

         init_Map.put(bed.immutable(), cached);
      }

      BedBreachRouteSolver.Inner_BufferHelper route = cached.init_DataPipe_CachePool.init(mining, progress, preferred);
      if (ideal && route != null) {
         List<BedBreachRouteSolver.Inner_DataUtil> actual = route.init_List().stream().map(c -> {
            double ticksx = actualCosts.get(c.init_BlockPos());
            if (c.init_BlockPos().equals(mining)) {
               ticksx = Math.max(1.0, Math.ceil(ticksx * (1.0F - Math.clamp(progress, 0.0F, 1.0F))));
            }

            return new BedBreachRouteSolver.Inner_DataUtil(c.init_BlockPos(), c.init_List(), ticksx, c.init_I(), c.init_Z());
         }).toList();
         return new BedBreachRouteSolver.Inner_BufferHelper(
            actual, route.init_Vec3(), actual.stream().mapToDouble(BedBreachRouteSolver.Inner_DataUtil::init_D).sum() + Math.max(0, actual.size() - 1) * 5, route.init_Z()
         );
      } else {
         return route;
      }
   }

   static BedMiningEstimator.Inner_StreamUtil init(BlockPos pos, Vec3 preferred, float progress, Vec3 nextEye) {
      if (!init_Minecraft.level.isLoaded(pos)) {
         return null;
      }

      BlockState state = init_Minecraft.level.getBlockState(pos);
      if (!state.isAir() && (state.getBlock() instanceof BedBlock || BedEspModule.init_Z_Block(state.getBlock()))) {
         Vec3 eye = init_Minecraft.player.getEyePosition();
         List<AABB> boxes = state.getShape(init_Minecraft.level, pos, CollisionContext.of(init_Minecraft.player))
            .toAabbs()
            .stream()
            .map(b -> b.move(pos))
            .toList();
         double reach = init_Minecraft.player.blockInteractionRange();
         if (!BedTargetAimSolver.init(boxes, eye, reach)) {
            return null;
         }

         AABB bounds = new AABB(pos).minmax(new AABB(eye, eye)).minmax(new AABB(nextEye, nextEye)).inflate(0.01);
         ArrayList<AABB> blockers = new ArrayList<>();

         for (BlockPos p : BlockPos.betweenClosed(
            BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ), BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ)
         )) {
            if (!p.equals(pos)) {
               if (!init_Minecraft.level.isLoaded(p)) {
                  blockers.add(new AABB(p));
               } else {
                  BlockState block = init_Minecraft.level.getBlockState(p);
                  if (!block.isAir()) {
                     for (AABB box : block.getShape(init_Minecraft.level, p, CollisionContext.of(init_Minecraft.player)).toAabbs()) {
                        blockers.add(box.move(p));
                     }
                  }
               }
            }
         }

         double remaining = Math.max(1.0, Math.ceil(init(state, pos, init_Minecraft.player.getMainHandItem()) * (1.0F - Math.clamp(progress, 0.0F, 1.0F))));
         Vec3 visible = BedTargetAimSolver.init_Vec3(boxes, blockers, eye, reach, preferred);
         Vec3 future = BedTargetAimSolver.init_Vec3(boxes, blockers, nextEye, reach, preferred);
         return new BedMiningEstimator.Inner_StreamUtil(
            new BedBreachRouteSolver.Inner_DataUtil(pos, boxes, remaining, init_Minecraft.player.getInventory().getSelectedSlot(), state.getBlock() instanceof BedBlock),
            future == null ? visible : future,
            visible != null
         );
      } else {
         return null;
      }
   }

   private static int init(BlockState state) {
      int selected = init_Minecraft.player.getInventory().getSelectedSlot();
      int best = autismclient.util.BlockBreakSpeedHelper.init(init_Minecraft, state, 9, true, false);
      if (best >= 0 && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(best, "bed-nuker")) {
         return autismclient.util.BlockBreakSpeedHelper.init(init_Minecraft, init_Minecraft.player.getInventory().getItem(best), state)
               > Math.max(1.0F, autismclient.util.BlockBreakSpeedHelper.init(init_Minecraft, init_Minecraft.player.getMainHandItem(), state))
            ? best
            : selected;
      } else {
         return selected;
      }
   }

   static double init(BlockState state, BlockPos pos, ItemStack stack) {
      if (init_Minecraft.player.hasInfiniteMaterials()) {
         return state.getDestroySpeed(init_Minecraft.level, pos) < 0.0F ? 1.0 / 0.0 : 1.0;
      }

      double speed = stack.isEmpty() ? 1.0 : autismclient.util.BlockBreakSpeedHelper.init(init_Minecraft, stack, state);
      int haste = MobEffectUtil.hasDigSpeed(init_Minecraft.player) ? MobEffectUtil.getDigSpeedAmplification(init_Minecraft.player) + 1 : 0;
      speed *= 1.0 + 0.2 * haste;
      MobEffectInstance fatigue = init_Minecraft.player.getEffect(MobEffects.MINING_FATIGUE);
      if (fatigue != null) {
         double var10001;
         switch (fatigue.getAmplifier()) {
            case 0:
               var10001 = 0.3;
               break;
            case 1:
               var10001 = 0.09;
               break;
            case 2:
               var10001 = 0.0027;
               break;
            default:
               var10001 = 8.1E-4;
         }

         speed *= var10001;
      }

      Holder<Attribute> attribute = Attributes.BLOCK_BREAK_SPEED;
      AttributeInstance live = init_Minecraft.player.getAttribute(attribute);
      if (live != null) {
         AttributeInstance candidate = new AttributeInstance(attribute, ignored -> {});
         candidate.replaceFrom(live);
         init_Minecraft.player.getMainHandItem().forEachModifier(EquipmentSlot.MAINHAND, (type, modifier) -> {
            if (type.equals(attribute)) {
               candidate.removeModifier(modifier.id());
            }
         });
         stack.forEachModifier(EquipmentSlot.MAINHAND, (type, modifier) -> {
            if (type.equals(attribute)) {
               candidate.removeModifier(modifier.id());
               candidate.addTransientModifier(modifier);
            }
         });
         speed *= candidate.getValue();
      }

      if (init_Minecraft.player.isEyeInFluid(FluidTags.WATER)) {
         speed *= init_Minecraft.player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
      }

      if (!init_Minecraft.player.onGround()) {
         speed /= 5.0;
      }

      return BedEspModule.init(
         state.getDestroySpeed(init_Minecraft.level, pos), speed, !state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state), false
      );
   }

   private record Inner_DataUtil(Vec3 init_Vec3, double init_D, List<BedBreachRouteSolver.Inner_DataUtil> init_List, BedBreachRouteSolver.Inner_CachePool init_DataPipe_CachePool) {
   }

   record Inner_StreamUtil(BedBreachRouteSolver.Inner_DataUtil init_DataPipe_DataUtil, Vec3 init_Vec3, boolean init_Z) {
   }
}
