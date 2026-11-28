package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.ToDoubleFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

final class AiLavaEscape {
   static final int init = 16;
   static final int load = 2048;
   static final int read = 600;

   private AiLavaEscape() {
   }

   static boolean isInLava(Minecraft mc) {
      return mc.player != null && mc.player.isInLava();
   }

   static BlockPos nearestShore(Minecraft mc) {
      BlockPos feet = mc.player.blockPosition();
      BlockPos best = null;
      double bestScore = 1.7976931348623157E308;

      for (BlockPos p : BlockPos.betweenClosed(feet.offset(-16, -2, -16), feet.offset(16, 3, 16))) {
         BlockPos below = p.below();
         BlockState floor = mc.level.getBlockState(below);
         if (!floor.getCollisionShape(mc.level, below).isEmpty() && !floor.getFluidState().is(FluidTags.LAVA)) {
            boolean clear = true;

            for (int dy = 0; dy < 2 && clear; dy++) {
               BlockPos c = p.above(dy);
               BlockState s = mc.level.getBlockState(c);
               if (!s.getCollisionShape(mc.level, c).isEmpty() || !s.getFluidState().isEmpty() || s.getBlock() instanceof BaseFireBlock) {
                  clear = false;
               }
            }

            if (clear) {
               boolean under = false;

               for (int dy = 2; dy <= 4 && !under; dy++) {
                  if (mc.level.getFluidState(p.above(dy)).is(FluidTags.LAVA)) {
                     under = true;
                  }
               }

               if (!under) {
                  double dx = p.getX() + 0.5 - mc.player.getX();
                  double dz = p.getZ() + 0.5 - mc.player.getZ();
                  double score = Math.hypot(dx, dz) + Math.max(0.0, p.getY() - mc.player.getY()) * 0.5;
                  if (score < bestScore) {
                     bestScore = score;
                     best = p.immutable();
                  }
               }
            }
         }
      }

      return best;
   }

   static double pathCost(List<BlockPos> path, Vec3 at) {
      BlockPos end = path.getLast();
      return Math.max(0, path.size() - 1) + Math.max(0.0, end.getY() - at.y) * 0.5 + Math.hypot(end.getX() + 0.5 - at.x, end.getZ() + 0.5 - at.z) * 1.0E-4;
   }

   static List<BlockPos> escapePath(Minecraft mc) {
      BlockPos start = mc.player.blockPosition();
      BlockPos shore = nearestShore(mc);
      Vec3 look = mc.player.getLookAngle();
      ToDoubleFunction<BlockPos> guide = cell -> {
         if (shore == null) {
            double dx = cell.getX() - start.getX();
            double dz = cell.getZ() - start.getZ();
            double distance = Math.hypot(dx, dz);
            double alignment = distance == 0.0 ? 1.0 : (dx * look.x + dz * look.z) / distance;
            return -distance + (1.0 - alignment) * 2.0;
         } else {
            return Math.abs(cell.getX() - shore.getX()) + Math.abs(cell.getZ() - shore.getZ()) + Math.max(0, shore.getY() - cell.getY()) * 2;
         }
      };
      PriorityQueue<AiLavaEscape.Inner_DataUtil> queue = new PriorityQueue<>(
         Comparator.comparingDouble(AiLavaEscape.Inner_DataUtil::priority).thenComparingInt(AiLavaEscape.Inner_DataUtil::cost)
      );
      Map<BlockPos, BlockPos> parent = new HashMap<>();
      Map<BlockPos, Integer> costs = new HashMap<>();
      List<BlockPos> best = null;
      BlockPos closest = start;
      double bestScore = 1.7976931348623157E308;
      double closestScore = 1.7976931348623157E308;
      queue.add(new AiLavaEscape.Inner_DataUtil(start, 0, guide.applyAsDouble(start)));
      parent.put(start, start);
      costs.put(start, 0);
      int searched = 0;

      while (!queue.isEmpty() && searched++ < 2048) {
         AiLavaEscape.Inner_DataUtil node = queue.poll();
         if (costs.getOrDefault(node.cell(), 2147483647) == node.cost()) {
            if (best != null && node.cost() >= bestScore) {
               break;
            }

            BlockPos n = node.cell();
            if (node.cost() > 0) {
               double score = guide.applyAsDouble(n);
               if (score < closestScore) {
                  closestScore = score;
                  closest = n;
               }
            }

            for (Direction d : Plane.HORIZONTAL) {
               BlockPos side = n.relative(d);

               for (int up = 0; up <= 1; up++) {
                  BlockPos g = side.above(up);
                  if (isSafeStand(mc, g) && (up != 1 || isPassable(mc, n.above()))) {
                     List<BlockPos> path = tracePath(parent, start, n);
                     path.add(g);
                     double score = pathCost(path, mc.player.position());
                     if (score < bestScore) {
                        bestScore = score;
                        best = path;
                     }
                  }
               }

               if (Math.abs(side.getX() - start.getX()) <= 16 && Math.abs(side.getZ() - start.getZ()) <= 16 && isPassable(mc, side)) {
                  int next = node.cost() + 1;
                  if (next < costs.getOrDefault(side, 2147483647)) {
                     costs.put(side, next);
                     parent.put(side, n);
                     queue.add(new AiLavaEscape.Inner_DataUtil(side, next, next + guide.applyAsDouble(side)));
                  }
               }
            }

            BlockPos up = n.above();
            if (up.getY() <= start.getY() + 3 && isPassable(mc, up)) {
               int next = node.cost() + 2;
               if (next < costs.getOrDefault(up, 2147483647)) {
                  costs.put(up, next);
                  parent.put(up, n);
                  queue.add(new AiLavaEscape.Inner_DataUtil(up, next, next + guide.applyAsDouble(up)));
               }
            }
         }
      }

      return best != null ? best : (closest.equals(start) ? null : tracePath(parent, start, closest));
   }

   static List<BlockPos> tracePath(Map<BlockPos, BlockPos> parent, BlockPos start, BlockPos end) {
      List<BlockPos> path = new ArrayList<>();
      BlockPos cell = end;

      while (true) {
         path.addFirst(cell);
         if (cell.equals(start)) {
            return path;
         }

         cell = parent.get(cell);
      }
   }

   static boolean isPassable(Minecraft mc, BlockPos p) {
      return mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
         && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty();
   }

   static boolean isSafeStand(Minecraft mc, BlockPos p) {
      BlockPos below = p.below();
      BlockState floor = mc.level.getBlockState(below);
      if (!floor.getCollisionShape(mc.level, below).isEmpty() && !floor.getFluidState().is(FluidTags.LAVA)) {
         for (int dy = 0; dy < 2; dy++) {
            BlockPos c = p.above(dy);
            BlockState s = mc.level.getBlockState(c);
            if (!s.getCollisionShape(mc.level, c).isEmpty() || !s.getFluidState().isEmpty() || s.getBlock() instanceof BaseFireBlock) {
               return false;
            }
         }

         for (int dy = 2; dy <= 4; dy++) {
            if (mc.level.getFluidState(p.above(dy)).is(FluidTags.LAVA)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static int nearestPathIndex(List<BlockPos> path, BlockPos at, int previous) {
      if (path.isEmpty()) {
         return -1;
      }

      int nearest = -1;
      double best = 1.7976931348623157E308;

      for (int i = 0; i < path.size(); i++) {
         if (path.get(i).equals(at)) {
            return Math.min(i + 1, path.size() - 1);
         }

         double distance = path.get(i).distSqr(at);
         if (distance < best) {
            best = distance;
            nearest = i;
         }
      }

      if (previous >= 0) {
         nearest = Math.max(nearest, previous);
      }

      return Math.min(nearest + 1, path.size() - 1);
   }

   static AiOperation escape() {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
         final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer().fast();
         BlockPos waypoint;
         List<BlockPos> path;
         int pathIndex = -1;
         int ticks;
         int airTicks;
         int placeTicks;
         boolean cancelled;
         boolean pathEndsSafe;
         boolean placingBlock;
         boolean bridgeTried;

         {
            this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.FAST);
         }

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && mc.player != null && mc.level != null) {
               if (!mc.player.isInLava()) {
                  if (mc.player.onGround() || ++this.airTicks > 20) {
                     return this.finish(AiOperation.Status.SUCCEEDED, "Out of the lava");
                  }
               } else {
                  this.airTicks = 0;
               }

               if (++this.ticks > 600) {
                  return this.finish(AiOperation.Status.FAILED, "Could not swim out of the lava");
               }

               BlockPos at = mc.player.blockPosition();
               if (this.placingBlock) {
                  autismclient.ai.engine.AiPlacer.PlaceStatus status = this.placer.tick();
                  if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                     this.placer.reset();
                     this.placingBlock = false;
                     this.bridgeTried = true;
                     this.pathEndsSafe = false;
                  } else {
                     if (status != autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED && ++this.placeTicks < 12) {
                        return AiGameOperations.result(AiOperation.Status.RUNNING, "Placing a lava refuge", new JsonObject());
                     }

                     if (Boolean.getBoolean("autism.ai.quick")) {
                        System.out.println("[AI quick] lava refuge stopped: " + this.placer.statusSummary() + "; " + this.placer.failureReason());
                     }

                     this.placer.reset();
                     this.placingBlock = false;
                     this.bridgeTried = true;
                  }
               }

               if (!this.placingBlock && !this.bridgeTried && this.path != null && AiLavaEscape.pathCost(this.path, mc.player.position()) > 1.0) {
                  for (int slot = 0; slot < 36 && !this.placingBlock; slot++) {
                     ItemStack stack = mc.player.getInventory().getItem(slot);
                     if (stack.getItem() instanceof BlockItem block
                        && !stack.isEmpty()
                        && autismclient.ai.engine.AiRestock.canSpendStack(stack, mc.player.getInventory(), true)
                        && autismclient.ai.engine.AiRestock.scaffoldRank(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()) >= 0) {
                        for (BlockPos refuge : this.path) {
                           if (!refuge.equals(at)
                              && !mc.level.getBlockState(refuge.below()).getCollisionShape(mc.level, refuge.below()).isEmpty()
                              && mc.level.isUnobstructed(block.getBlock().defaultBlockState(), refuge, CollisionContext.placementContext(mc.player))) {
                              this.placer.startWithExactStack(refuge, stack.copy());
                              this.placingBlock = true;
                              this.placeTicks = 0;
                              return AiGameOperations.result(AiOperation.Status.RUNNING, "Placing a lava refuge", new JsonObject());
                           }
                        }
                     }
                  }
               }

               if (this.path == null || this.ticks % 20 == 0) {
                  List<BlockPos> next = AiLavaEscape.escapePath(mc);
                  if (next != null) {
                     this.path = next;
                     this.pathEndsSafe = AiLavaEscape.isSafeStand(mc, next.getLast());
                     this.pathIndex = AiLavaEscape.nearestPathIndex(this.path, at, this.pathIndex);
                  }
               }

               if (this.path != null && !this.path.isEmpty()) {
                  this.pathIndex = AiLavaEscape.nearestPathIndex(this.path, at, this.pathIndex);
                  this.waypoint = this.path.get(Math.min(this.pathIndex, this.path.size() - 1));
                  if (this.pathEndsSafe && this.pathIndex == this.path.size() - 1 && !AiLavaEscape.isSafeStand(mc, this.waypoint)) {
                     this.path = null;
                     this.pathEndsSafe = false;
                     this.pathIndex = -1;
                     this.waypoint = null;
                  }
               } else if (this.waypoint == null || !AiLavaEscape.isSafeStand(mc, this.waypoint)) {
                  this.waypoint = AiLavaEscape.nearestShore(mc);
               }

               if (this.ticks % 5 == 0 && Boolean.getBoolean("autism.ai.quick")) {
                  System.out
                     .println(
                        "[AI quick] lava escape at "
                           + mc.player.position()
                           + " health "
                           + mc.player.getHealth()
                           + " target "
                           + this.waypoint
                           + " standable "
                           + (this.waypoint != null && AiLavaEscape.isSafeStand(mc, this.waypoint))
                           + " waypoint "
                           + this.pathIndex
                           + " path "
                           + this.path
                     );
               }

               if (this.waypoint != null) {
                  this.aim.aimAt(new Vec3(this.waypoint.getX() + 0.5, this.waypoint.getY() + 0.6, this.waypoint.getZ() + 0.5));
                  this.aim.tick();
               } else {
                  this.aim.clear();
               }

               mc.options.keyUp.setDown(true);
               mc.options.keyJump.setDown(true);
               mc.options.keySprint.setDown(true);
               mc.options.keyShift.setDown(false);
               return AiGameOperations.result(
                  AiOperation.Status.RUNNING,
                  this.waypoint == null ? "In lava; swimming outward" : "In lava; swimming to " + this.waypoint.toShortString(),
                  new JsonObject()
               );
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Lava escape cancelled");
            }
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            this.releaseKeys();
            return AiGameOperations.result(status, message, new JsonObject());
         }

         void releaseKeys() {
            mc.options.keyUp.setDown(false);
            mc.options.keyJump.setDown(false);
            mc.options.keySprint.setDown(false);
            this.aim.clear();
         }

         @Override
         public void cancel() {
            this.cancelled = true;
            this.releaseKeys();
         }
      };
   }

   record Inner_DataUtil(BlockPos cell, int cost, double priority) {
   }
}
