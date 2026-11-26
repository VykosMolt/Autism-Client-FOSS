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

   static boolean init_Z(Minecraft mc) {
      return mc.player != null && mc.player.isInLava();
   }

   static BlockPos init_BlockPos(Minecraft mc) {
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

   static double init(List<BlockPos> path, Vec3 at) {
      BlockPos end = path.getLast();
      return Math.max(0, path.size() - 1) + Math.max(0.0, end.getY() - at.y) * 0.5 + Math.hypot(end.getX() + 0.5 - at.x, end.getZ() + 0.5 - at.z) * 1.0E-4;
   }

   static List<BlockPos> init_List(Minecraft mc) {
      BlockPos start = mc.player.blockPosition();
      BlockPos shore = init_BlockPos(mc);
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
                  if (load(mc, g) && (up != 1 || init(mc, n.above()))) {
                     List<BlockPos> path = init(parent, start, n);
                     path.add(g);
                     double score = init(path, mc.player.position());
                     if (score < bestScore) {
                        bestScore = score;
                        best = path;
                     }
                  }
               }

               if (Math.abs(side.getX() - start.getX()) <= 16 && Math.abs(side.getZ() - start.getZ()) <= 16 && init(mc, side)) {
                  int next = node.cost() + 1;
                  if (next < costs.getOrDefault(side, 2147483647)) {
                     costs.put(side, next);
                     parent.put(side, n);
                     queue.add(new AiLavaEscape.Inner_DataUtil(side, next, next + guide.applyAsDouble(side)));
                  }
               }
            }

            BlockPos up = n.above();
            if (up.getY() <= start.getY() + 3 && init(mc, up)) {
               int next = node.cost() + 2;
               if (next < costs.getOrDefault(up, 2147483647)) {
                  costs.put(up, next);
                  parent.put(up, n);
                  queue.add(new AiLavaEscape.Inner_DataUtil(up, next, next + guide.applyAsDouble(up)));
               }
            }
         }
      }

      return best != null ? best : (closest.equals(start) ? null : init(parent, start, closest));
   }

   static List<BlockPos> init(Map<BlockPos, BlockPos> parent, BlockPos start, BlockPos end) {
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

   static boolean init(Minecraft mc, BlockPos p) {
      return mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
         && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty();
   }

   static boolean load(Minecraft mc, BlockPos p) {
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

   static int init(List<BlockPos> path, BlockPos at, int previous) {
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

   static AiOperation init() {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
         final autismclient.ai.engine.AiPlacer init_DataPipe = new autismclient.ai.engine.AiPlacer().init_DataPipe();
         BlockPos init_BlockPos;
         List<BlockPos> init_List;
         int init_I = -1;
         int load_I;
         int read_I;
         int write_I;
         boolean init_Z;
         boolean load_Z;
         boolean read_Z;
         boolean write_Z;

         {
            this.init_DataUtil.init_V_DataUtil_DataUtil(autismclient.ai.engine.AiAim.Inner_DataUtil.FAST);
         }

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && mc.player != null && mc.level != null) {
               if (!mc.player.isInLava()) {
                  if (mc.player.onGround() || ++this.read_I > 20) {
                     return this.init(AiOperation.Status.SUCCEEDED, "Out of the lava");
                  }
               } else {
                  this.read_I = 0;
               }

               if (++this.load_I > 600) {
                  return this.init(AiOperation.Status.FAILED, "Could not swim out of the lava");
               }

               BlockPos at = mc.player.blockPosition();
               if (this.read_Z) {
                  autismclient.ai.engine.AiPlacer.Inner_DataUtil status = this.init_DataPipe.init_DataPipe_DataUtil();
                  if (status == autismclient.ai.engine.AiPlacer.Inner_DataUtil.PLACED) {
                     this.init_DataPipe.init_V();
                     this.read_Z = false;
                     this.write_Z = true;
                     this.load_Z = false;
                  } else {
                     if (status != autismclient.ai.engine.AiPlacer.Inner_DataUtil.FAILED && ++this.write_I < 12) {
                        return AiGameOperations.init_AiOperation_Result(AiOperation.Status.RUNNING, "Placing a lava refuge", new JsonObject());
                     }

                     if (Boolean.getBoolean("autism.ai.quick")) {
                        System.out.println("[AI quick] lava refuge stopped: " + this.init_DataPipe.load_Str() + "; " + this.init_DataPipe.init_Str());
                     }

                     this.init_DataPipe.init_V();
                     this.read_Z = false;
                     this.write_Z = true;
                  }
               }

               if (!this.read_Z && !this.write_Z && this.init_List != null && AiLavaEscape.init(this.init_List, mc.player.position()) > 1.0) {
                  for (int slot = 0; slot < 36 && !this.read_Z; slot++) {
                     ItemStack stack = mc.player.getInventory().getItem(slot);
                     if (stack.getItem() instanceof BlockItem block
                        && !stack.isEmpty()
                        && autismclient.ai.engine.AiRestock.init_Z_ItemStack(stack, mc.player.getInventory(), true)
                        && autismclient.ai.engine.AiRestock.init_I(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()) >= 0) {
                        for (BlockPos refuge : this.init_List) {
                           if (!refuge.equals(at)
                              && !mc.level.getBlockState(refuge.below()).getCollisionShape(mc.level, refuge.below()).isEmpty()
                              && mc.level.isUnobstructed(block.getBlock().defaultBlockState(), refuge, CollisionContext.placementContext(mc.player))) {
                              this.init_DataPipe.init_V_BlockPos_ItemStack(refuge, stack.copy());
                              this.read_Z = true;
                              this.write_I = 0;
                              return AiGameOperations.init_AiOperation_Result(AiOperation.Status.RUNNING, "Placing a lava refuge", new JsonObject());
                           }
                        }
                     }
                  }
               }

               if (this.init_List == null || this.load_I % 20 == 0) {
                  List<BlockPos> next = AiLavaEscape.init_List(mc);
                  if (next != null) {
                     this.init_List = next;
                     this.load_Z = AiLavaEscape.load(mc, next.getLast());
                     this.init_I = AiLavaEscape.init(this.init_List, at, this.init_I);
                  }
               }

               if (this.init_List != null && !this.init_List.isEmpty()) {
                  this.init_I = AiLavaEscape.init(this.init_List, at, this.init_I);
                  this.init_BlockPos = this.init_List.get(Math.min(this.init_I, this.init_List.size() - 1));
                  if (this.load_Z && this.init_I == this.init_List.size() - 1 && !AiLavaEscape.load(mc, this.init_BlockPos)) {
                     this.init_List = null;
                     this.load_Z = false;
                     this.init_I = -1;
                     this.init_BlockPos = null;
                  }
               } else if (this.init_BlockPos == null || !AiLavaEscape.load(mc, this.init_BlockPos)) {
                  this.init_BlockPos = AiLavaEscape.init_BlockPos(mc);
               }

               if (this.load_I % 5 == 0 && Boolean.getBoolean("autism.ai.quick")) {
                  System.out
                     .println(
                        "[AI quick] lava escape at "
                           + mc.player.position()
                           + " health "
                           + mc.player.getHealth()
                           + " target "
                           + this.init_BlockPos
                           + " standable "
                           + (this.init_BlockPos != null && AiLavaEscape.load(mc, this.init_BlockPos))
                           + " waypoint "
                           + this.init_I
                           + " path "
                           + this.init_List
                     );
               }

               if (this.init_BlockPos != null) {
                  this.init_DataUtil.init_V_Vec3(new Vec3(this.init_BlockPos.getX() + 0.5, this.init_BlockPos.getY() + 0.6, this.init_BlockPos.getZ() + 0.5));
                  this.init_DataUtil.load_V();
               } else {
                  this.init_DataUtil.init_V();
               }

               mc.options.keyUp.setDown(true);
               mc.options.keyJump.setDown(true);
               mc.options.keySprint.setDown(true);
               mc.options.keyShift.setDown(false);
               return AiGameOperations.init_AiOperation_Result(
                  AiOperation.Status.RUNNING,
                  this.init_BlockPos == null ? "In lava; swimming outward" : "In lava; swimming to " + this.init_BlockPos.toShortString(),
                  new JsonObject()
               );
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Lava escape cancelled");
            }
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            this.init();
            return AiGameOperations.init_AiOperation_Result(status, message, new JsonObject());
         }

         void init() {
            mc.options.keyUp.setDown(false);
            mc.options.keyJump.setDown(false);
            mc.options.keySprint.setDown(false);
            this.init_DataUtil.init_V();
         }

         @Override
         public void cancel() {
            this.init_Z = true;
            this.init();
         }
      };
   }

   record Inner_DataUtil(BlockPos cell, int cost, double priority) {
   }
}
