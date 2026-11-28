package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class AiBreathe {
   static final double init_D = 0.4;
   static final int MAX_AIR_SEARCH_HEIGHT = 24;
   static final int load = 2000;

   private AiBreathe() {
   }

   static boolean needsAir(Player player) {
      if (!player.isUnderWater()) {
         return false;
      }

      if (!player.hasEffect(MobEffects.WATER_BREATHING) && !player.hasEffect(MobEffects.CONDUIT_POWER)) {
         double floor = player.getMaxAirSupply() * 0.4;
         Minecraft mc = Minecraft.getInstance();
         if (mc.player == player && mc.level != null) {
            floor = Math.max(floor, autismclient.ai.engine.AiBlockDigger.airTicksToSurfaceCompat(mc) + 40);
         }

         return player.getAirSupply() < floor;
      } else {
         return false;
      }
   }

   public static AiOperation create(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int seconds = Math.max(5, Math.min(60, AiJson.getInt(args, "timeoutSeconds", 25)));
         if (mc.player.isUnderWater()) {
            autismclient.ai.engine.AiBlockMine.markBreatheStart();
         }

         return new AiOperation() {
            final long deadlineNanos = System.nanoTime() + seconds * 1000000000L;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            BlockPos airTarget;
            List<BlockPos> airPath = new ArrayList<>();
            int replanCooldown;
            boolean cancelled;

            @Override
            public AiOperation.Result tick() {
               LocalPlayer player = mc.player;
               if (!this.cancelled && player != null && mc.level != null) {
                  boolean headOut = !player.isUnderWater();
                  if (headOut && player.getAirSupply() >= player.getMaxAirSupply() * 0.9) {
                     return this.finish(AiOperation.Status.SUCCEEDED, "Breathing again (air " + player.getAirSupply() + "/" + player.getMaxAirSupply() + ")");
                  }

                  if (System.nanoTime() > this.deadlineNanos) {
                     return this.finish(AiOperation.Status.FAILED, "Could not reach air in time (air " + player.getAirSupply() + ")");
                  }

                  mc.options.keyJump.setDown(true);
                  mc.options.keyShift.setDown(false);
                  if (headOut) {
                     mc.options.keyUp.setDown(false);
                     return this.running("Treading water while the air refills (" + player.getAirSupply() + ")");
                  }

                  if (--this.replanCooldown <= 0) {
                     this.airPath = new ArrayList<>();
                     this.airTarget = AiBreathe.findAir(mc, player.blockPosition(), this.airPath);
                     this.replanCooldown = 10;
                  }

                  BlockPos head = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
                  BlockPos roof = head.above();
                  BlockState roofState = mc.level.getBlockState(roof);
                  boolean roofSolid = !roofState.getCollisionShape(mc.level, roof).isEmpty();
                  if (roofSolid
                     && (this.airTarget == null || this.airPath.size() > 5)
                     && roofState.getDestroySpeed(mc.level, roof) >= 0.0F
                     && roofState.getDestroySpeed(mc.level, roof) <= 3.0F
                     && AiBreathe.hasAirAbove(mc, roof)) {
                     mc.options.keyUp.setDown(false);
                     this.aim.aimAt(Vec3.atCenterOf(roof));
                     this.aim.tick();
                     int slot = AiItems.bestToolFor(roofState, 36);
                     if (slot >= 0 && player.getInventory().getSelectedSlot() != slot) {
                        AiItems.selectOrMoveToHotbar(slot);
                     }

                     autismclient.ai.engine.AiBlockDigger.continueBreakingBlockCompat(mc, roof, Direction.DOWN);
                     return this.running("Breaking the roof at " + roof.toShortString() + " (air " + player.getAirSupply() + ")");
                  } else {
                     if (this.airTarget == null) {
                        mc.options.keyUp.setDown(false);
                        return this.running("No air found nearby; swimming up");
                     }

                     BlockPos step = this.airTarget;
                     BlockPos feetNow = player.blockPosition();

                     for (int i = 0; i < this.airPath.size(); i++) {
                        if (this.airPath.get(i).equals(feetNow) || this.airPath.get(i).equals(head)) {
                           step = i + 1 < this.airPath.size() ? this.airPath.get(i + 1) : this.airTarget;
                        }
                     }

                     if (step == this.airTarget && !this.airPath.isEmpty() && !this.airPath.contains(feetNow) && !this.airPath.contains(head)) {
                        step = this.airPath.get(0);
                     }

                     Vec3 target = Vec3.atCenterOf(step);
                     this.aim.aimAt(target);
                     this.aim.tick();
                     double flat = Math.hypot(target.x - player.getX(), target.z - player.getZ());
                     boolean boxBlocked = !mc.level.noCollision(player, player.getBoundingBox().move(0.0, 0.6, 0.0));
                     mc.options.keyUp.setDown(flat > 0.4 || boxBlocked && flat > 0.08);
                     return this.running("Swimming to air at " + this.airTarget.toShortString() + " (air " + player.getAirSupply() + ")");
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Cancelled");
               }
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, new JsonObject());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               this.releaseKeys();
               return AiGameOperations.result(status, message, AiJson.objectOf("air", mc.player == null ? 0 : mc.player.getAirSupply()));
            }

            void releaseKeys() {
               mc.options.keyJump.setDown(false);
               mc.options.keyUp.setDown(false);
               this.aim.clear();
            }

            @Override
            public void cancel() {
               this.cancelled = true;
               this.releaseKeys();
            }
         };
      } else {
         throw new IllegalArgumentException("Breathing requires a connected living player");
      }
   }

   static boolean hasAirAbove(Minecraft mc, BlockPos roof) {
      for (int dy = 1; dy <= 4; dy++) {
         BlockPos c = roof.above(dy);
         boolean water = mc.level.getFluidState(c).is(FluidTags.WATER);
         boolean open = mc.level.getBlockState(c).getCollisionShape(mc.level, c).isEmpty();
         if (open && !water) {
            return true;
         }

         if (!open || !water) {
            return false;
         }
      }

      return false;
   }

   static BlockPos findAirPocket(Minecraft mc, BlockPos feet) {
      return findAir(mc, feet, new ArrayList<>());
   }

   static BlockPos findAir(Minecraft mc, BlockPos feet, List<BlockPos> path) {
      ClientLevel level = mc.level;

      for (int dy = 1; dy <= 24; dy++) {
         BlockPos cell = feet.above(dy);
         boolean water = level.getFluidState(cell).is(FluidTags.WATER);
         boolean open = level.getBlockState(cell).getCollisionShape(level, cell).isEmpty();
         if (!open && !water) {
            break;
         }

         if (open && !water) {
            return cell;
         }
      }

      ArrayDeque<BlockPos> queue = new ArrayDeque<>();
      Map<BlockPos, BlockPos> parent = new HashMap<>();
      Set<BlockPos> seen = new HashSet<>();
      queue.add(feet);
      seen.add(feet);

      while (!queue.isEmpty() && seen.size() < 2000) {
         BlockPos at = queue.poll();

         for (Direction d : Direction.values()) {
            BlockPos next = at.relative(d);
            if (seen.add(next) && Math.abs(next.getX() - feet.getX()) <= 12 && Math.abs(next.getZ() - feet.getZ()) <= 12) {
               boolean water = level.getFluidState(next).is(FluidTags.WATER);
               boolean open = level.getBlockState(next).getCollisionShape(level, next).isEmpty();
               parent.put(next, at);
               if (open && !water && level.getFluidState(next.below()).is(FluidTags.WATER)) {
                  for (BlockPos c = next; c != null && !c.equals(feet); c = parent.get(c)) {
                     path.add(0, c);
                  }

                  return next;
               }

               if (water && (open || level.getBlockState(next).getBlock() instanceof LiquidBlock)) {
                  queue.add(next);
               }
            }
         }
      }

      return null;
   }
}
