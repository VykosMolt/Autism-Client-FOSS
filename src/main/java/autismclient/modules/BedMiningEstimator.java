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
   private static final Minecraft MC = Minecraft.getInstance();
   private static final Map<BlockPos, BedMiningEstimator.Inner_DataUtil> cache = new LinkedHashMap<>();
   private static Object trackedLevel;

   static void clearCache() {
      cache.clear();
      trackedLevel = null;
   }

   static BedBreachRouteSolver.BreachRoute localRoute(BlockPos bed, boolean tools, BlockPos mining, float progress) {
      return estimateRoute(bed, tools, mining, progress, 0.0);
   }

   static BedBreachRouteSolver.BreachRoute nukerRoute(BlockPos bed, boolean tools, BlockPos mining, float progress) {
      return estimateRoute(bed, tools, mining, progress, 8.0);
   }

   private static BedBreachRouteSolver.BreachRoute estimateRoute(BlockPos bed, boolean tools, BlockPos mining, float progress, double coverDepth) {
      if (trackedLevel != MC.level) {
         clearCache();
         trackedLevel = MC.level;
      }

      boolean ideal = !BedEspModule.policy().init_Z();
      List<ItemStack> recommended = ideal ? BedEspModule.recommendedTools() : List.of();
      Map<BlockPos, Double> actualCosts = ideal ? new HashMap<>() : null;
      Map<BlockState, Double> idealCosts = ideal ? new HashMap<>() : null;
      BlockState state = MC.level.getBlockState(bed);
      if (!(state.getBlock() instanceof BedBlock)) {
         return null;
      }

      BlockPos other = bed.relative(BedBlock.getConnectedDirection(state));
      BlockState partner = MC.level.getBlockState(other);
      boolean paired = partner.is(state.getBlock())
         && partner.getValue(BedBlock.PART) != state.getValue(BedBlock.PART)
         && partner.getValue(BedBlock.FACING) == state.getValue(BedBlock.FACING);
      Vec3 eye = MC.player.getEyePosition();
      AABB bedBounds = new AABB(bed).minmax(new AABB(paired ? other : bed));
      double distance = MC.player.blockInteractionRange() + coverDepth;
      if (bedBounds.distanceToSqr(eye) > distance * distance) {
         return null;
      }

      AABB bounds = new AABB(bed).minmax(new AABB(paired ? other : bed)).minmax(new AABB(eye, eye)).inflate(0.01);
      ArrayList<BedBreachRouteSolver.BreachCell> cells = new ArrayList<>();
      HashMap<BlockState, BedBreachRouteSolver.BreachCell> costs = new HashMap<>();

      for (BlockPos mutable : BlockPos.betweenClosed(
         BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ), BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ)
      )) {
         BlockPos p = mutable.immutable();
         if (!MC.level.isLoaded(p)) {
            cells.add(new BedBreachRouteSolver.BreachCell(p, List.of(new AABB(p)), 1.0 / 0.0, -1, false));
         } else {
            BlockState block = MC.level.getBlockState(p);
            if (!block.isAir()) {
               VoxelShape shape = block.getShape(MC.level, p, CollisionContext.of(MC.player));
               if (!shape.isEmpty()) {
                  List<AABB> boxes = shape.toAabbs().stream().map(b -> b.move(p)).toList();
                  boolean target = p.equals(bed) || paired && p.equals(other);
                  boolean breakable = target || BedEspModule.init_Z_Block(block.getBlock());
                  int slot = MC.player.getInventory().getSelectedSlot();
                  double ticks = 1.0 / 0.0;
                  if (breakable) {
                     BedBreachRouteSolver.BreachCell cost = costs.get(block);
                     if (cost == null) {
                        if (tools) {
                           slot = bestSlotFor(block);
                        }

                        ticks = breakTicks(block, p, MC.player.getInventory().getItem(slot));
                        cost = new BedBreachRouteSolver.BreachCell(p, List.of(), ticks, slot, target);
                        costs.put(block, cost);
                     }

                     ticks = cost.ticks();
                     slot = cost.slot();
                  }

                  if (ideal && breakable) {
                     actualCosts.put(p, ticks);
                     ticks = idealCosts.computeIfAbsent(block, b -> recommended.stream().mapToDouble(stack -> breakTicks(b, p, stack)).min().orElse(1.0 / 0.0));
                  }

                  cells.add(new BedBreachRouteSolver.BreachCell(p, boxes, ticks, slot, target));
               }
            }
         }
      }

      double reach = MC.player.blockInteractionRange();
      Vec3 preferred = eye.add(Vec3.directionFromRotation(MC.player.getXRot(), MC.player.getYRot()).scale(reach));
      BedMiningEstimator.Inner_DataUtil cached = cache.get(bed);
      if (cached == null || !eye.equals(cached.eye) || reach != cached.reach || !cells.equals(cached.cells)) {
         cached = new BedMiningEstimator.Inner_DataUtil(eye, reach, List.copyOf(cells), BedBreachRouteSolver.solve(cells, eye, reach, preferred));
         if (cache.size() >= 16 && !cache.containsKey(bed)) {
            cache.remove(cache.keySet().iterator().next());
         }

         cache.put(bed.immutable(), cached);
      }

      BedBreachRouteSolver.BreachRoute route = cached.solver.bestRoute(mining, progress, preferred);
      if (ideal && route != null) {
         List<BedBreachRouteSolver.BreachCell> actual = route.cells().stream().map(c -> {
            double ticksx = actualCosts.get(c.pos());
            if (c.pos().equals(mining)) {
               ticksx = Math.max(1.0, Math.ceil(ticksx * (1.0F - Math.clamp(progress, 0.0F, 1.0F))));
            }

            return new BedBreachRouteSolver.BreachCell(c.pos(), c.boxes(), ticksx, c.slot(), c.init_Z());
         }).toList();
         return new BedBreachRouteSolver.BreachRoute(
            actual, route.eye(), actual.stream().mapToDouble(BedBreachRouteSolver.BreachCell::ticks).sum() + Math.max(0, actual.size() - 1) * 5, route.init_Z()
         );
      } else {
         return route;
      }
   }

   static BedMiningEstimator.Inner_StreamUtil track(BlockPos pos, Vec3 preferred, float progress, Vec3 nextEye) {
      if (!MC.level.isLoaded(pos)) {
         return null;
      }

      BlockState state = MC.level.getBlockState(pos);
      if (!state.isAir() && (state.getBlock() instanceof BedBlock || BedEspModule.init_Z_Block(state.getBlock()))) {
         Vec3 eye = MC.player.getEyePosition();
         List<AABB> boxes = state.getShape(MC.level, pos, CollisionContext.of(MC.player))
            .toAabbs()
            .stream()
            .map(b -> b.move(pos))
            .toList();
         double reach = MC.player.blockInteractionRange();
         if (!BedTargetAimSolver.anyInReach(boxes, eye, reach)) {
            return null;
         }

         AABB bounds = new AABB(pos).minmax(new AABB(eye, eye)).minmax(new AABB(nextEye, nextEye)).inflate(0.01);
         ArrayList<AABB> blockers = new ArrayList<>();

         for (BlockPos p : BlockPos.betweenClosed(
            BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ), BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ)
         )) {
            if (!p.equals(pos)) {
               if (!MC.level.isLoaded(p)) {
                  blockers.add(new AABB(p));
               } else {
                  BlockState block = MC.level.getBlockState(p);
                  if (!block.isAir()) {
                     for (AABB box : block.getShape(MC.level, p, CollisionContext.of(MC.player)).toAabbs()) {
                        blockers.add(box.move(p));
                     }
                  }
               }
            }
         }

         double remaining = Math.max(1.0, Math.ceil(breakTicks(state, pos, MC.player.getMainHandItem()) * (1.0F - Math.clamp(progress, 0.0F, 1.0F))));
         Vec3 visible = BedTargetAimSolver.visibleAimPoint(boxes, blockers, eye, reach, preferred);
         Vec3 future = BedTargetAimSolver.visibleAimPoint(boxes, blockers, nextEye, reach, preferred);
         return new BedMiningEstimator.Inner_StreamUtil(
            new BedBreachRouteSolver.BreachCell(pos, boxes, remaining, MC.player.getInventory().getSelectedSlot(), state.getBlock() instanceof BedBlock),
            future == null ? visible : future,
            visible != null
         );
      } else {
         return null;
      }
   }

   private static int bestSlotFor(BlockState state) {
      int selected = MC.player.getInventory().getSelectedSlot();
      int best = autismclient.util.BlockBreakSpeedHelper.bestToolSlot(MC, state, 9, true, false);
      if (best >= 0 && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(best, "bed-nuker")) {
         return autismclient.util.BlockBreakSpeedHelper.breakSpeed(MC, MC.player.getInventory().getItem(best), state)
               > Math.max(1.0F, autismclient.util.BlockBreakSpeedHelper.breakSpeed(MC, MC.player.getMainHandItem(), state))
            ? best
            : selected;
      } else {
         return selected;
      }
   }

   static double breakTicks(BlockState state, BlockPos pos, ItemStack stack) {
      if (MC.player.hasInfiniteMaterials()) {
         return state.getDestroySpeed(MC.level, pos) < 0.0F ? 1.0 / 0.0 : 1.0;
      }

      double speed = stack.isEmpty() ? 1.0 : autismclient.util.BlockBreakSpeedHelper.breakSpeed(MC, stack, state);
      int haste = MobEffectUtil.hasDigSpeed(MC.player) ? MobEffectUtil.getDigSpeedAmplification(MC.player) + 1 : 0;
      speed *= 1.0 + 0.2 * haste;
      MobEffectInstance fatigue = MC.player.getEffect(MobEffects.MINING_FATIGUE);
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
      AttributeInstance live = MC.player.getAttribute(attribute);
      if (live != null) {
         AttributeInstance candidate = new AttributeInstance(attribute, ignored -> {});
         candidate.replaceFrom(live);
         MC.player.getMainHandItem().forEachModifier(EquipmentSlot.MAINHAND, (type, modifier) -> {
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

      if (MC.player.isEyeInFluid(FluidTags.WATER)) {
         speed *= MC.player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
      }

      if (!MC.player.onGround()) {
         speed /= 5.0;
      }

      return BedEspModule.breakTicks(
         state.getDestroySpeed(MC.level, pos), speed, !state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state), false
      );
   }

   private record Inner_DataUtil(Vec3 eye, double reach, List<BedBreachRouteSolver.BreachCell> cells, BedBreachRouteSolver.RouteSet solver) {
   }

   record Inner_StreamUtil(BedBreachRouteSolver.BreachCell init_DataPipe_DataUtil, Vec3 init_Vec3, boolean init_Z) {
   }
}
