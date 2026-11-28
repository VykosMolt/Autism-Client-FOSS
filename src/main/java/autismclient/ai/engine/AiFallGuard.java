package autismclient.ai.engine;

import autismclient.AutismClientAddon;
import autismclient.ai.AiService;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.LongPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class AiFallGuard {
   private static final int init_I = 3;
   private static final double init_D = 3.0;
   private static final Set<AiPathfinder.MoveType> AIRBORNE_MOVES = EnumSet.of(
      AiPathfinder.MoveType.JUMP,
      AiPathfinder.MoveType.JUMP_GAP_2,
      AiPathfinder.MoveType.JUMP_GAP_3,
      AiPathfinder.MoveType.PARKOUR_ASCEND,
      AiPathfinder.MoveType.SPRINT_JUMP,
      AiPathfinder.MoveType.DESCEND_1,
      AiPathfinder.MoveType.DESCEND_2,
      AiPathfinder.MoveType.DESCEND_3,
      AiPathfinder.MoveType.DESCEND_SOFT,
      AiPathfinder.MoveType.DIG_DESCEND
   );
   private static volatile long lastDrivenNanos;
   private static volatile AiPathfinder.MoveType currentMove;
   private static volatile boolean moveMayLeaveEdge;
   private static volatile int moveTick = -1073741824;
   private static volatile long holdCount;
   private static volatile int lastHoldTick = -1073741824;
   private static volatile int bypassTick = -1073741824;
   private static volatile LongPredicate waterTraps;
   private static volatile long trapsUpdatedNanos;

   private AiFallGuard() {
   }

   public static void markDriven() {
      lastDrivenNanos = System.nanoTime();
   }

   static void noteMove(AiPathfinder.MoveType type, boolean tellyBridge) {
      setMoveContext(type, tellyBridge, false);
   }

   static void setMoveContext(AiPathfinder.MoveType type, boolean tellyBridge, boolean provedRunUp) {
      currentMove = type;
      moveMayLeaveEdge = provedRunUp || AIRBORNE_MOVES.contains(type) || type == AiPathfinder.MoveType.BRIDGE_CELL && tellyBridge;
      LocalPlayer player = Minecraft.getInstance().player;
      moveTick = player == null ? -1073741824 : player.tickCount;
      lastDrivenNanos = System.nanoTime();
   }

   public static void stampTick() {
      LocalPlayer player = Minecraft.getInstance().player;
      bypassTick = player == null ? -1073741824 : player.tickCount;
   }

   public static void setWaterTraps(LongPredicate traps) {
      waterTraps = traps;
      trapsUpdatedNanos = System.nanoTime();
   }

   private static boolean isTrappedWater(AiPhysics.MotionState s) {
      LongPredicate traps = waterTraps;
      return traps != null && System.nanoTime() - trapsUpdatedNanos <= 250000000L
         ? traps.test(BlockPos.asLong((int)Math.floor(s.x()), (int)Math.floor(s.y() + 0.1), (int)Math.floor(s.z())))
         : false;
   }

   public static boolean isHolding() {
      LocalPlayer player = Minecraft.getInstance().player;
      return player != null && player.tickCount - lastHoldTick <= 1;
   }

   public static long init_J() {
      return holdCount;
   }

   public static Input filterInput(ClientInput source, Input original) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (original != null && player != null && mc.level != null && player.input == source) {
         if (!original.shift()
            && player.onGround()
            && !player.isInWater()
            && !player.onClimbable()
            && !player.getAbilities().flying
            && mc.level.dimension() == Level.END) {
            boolean drivenNow = System.nanoTime() - lastDrivenNanos < 250000000L;
            if (!drivenNow) {
               AiService service = AiService.getExistingInstance();
               drivenNow = service != null && service.isActivelyDriving() || autismclient.ai.AiDirectOps.isDrivingInput();
            }

            if (drivenNow) {
               double f = (original.forward() ? 1 : 0) - (original.backward() ? 1 : 0);
               double st = (original.left() ? 1 : 0) - (original.right() ? 1 : 0);
               AiPhysics.MoveInput voidInput = new AiPhysics.MoveInput(f, st, original.jump(), original.sprint(), false, player.getYRot());
               if (wouldFallIntoVoid(new AiLiveWorld(mc.level, player), AiPhysics.MotionState.of(player), voidInput)) {
                  holdCount++;
                  if (player.tickCount - lastHoldTick > 1) {
                     AutismClientAddon.LOG.info("[AI] fall guard: held at the void's edge at {}", player.blockPosition().toShortString());
                  }

                  lastHoldTick = player.tickCount;
                  return new Input(original.forward(), original.backward(), original.left(), original.right(), false, true, false);
               }
            }
         }

         if (!original.shift() && !original.jump()) {
            long now = System.nanoTime();
            boolean driven = now - lastDrivenNanos < 250000000L;
            if (!driven) {
               AiService service = AiService.getExistingInstance();
               driven = service != null && service.isActivelyDriving() || autismclient.ai.AiDirectOps.isDrivingInput();
            }

            if (!driven) {
               return original;
            }

            if (player.onGround()
               && !player.isInWater()
               && !player.isInLava()
               && !player.onClimbable()
               && !player.isPassenger()
               && !player.getAbilities().flying
               && !player.isFallFlying()) {
               AiPathfinder.MoveType current = currentMove;
               if (current != null && moveMayLeaveEdge && player.tickCount - moveTick <= 1) {
                  return original;
               }

               if (player.tickCount - bypassTick <= 1) {
                  return original;
               }

               if (player.getBlockStateOn().is(Blocks.MAGMA_BLOCK) && !player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                  return new Input(original.forward(), original.backward(), original.left(), original.right(), false, true, false);
               }

               double forward = (original.forward() ? 1 : 0) - (original.backward() ? 1 : 0);
               double strafe = (original.left() ? 1 : 0) - (original.right() ? 1 : 0);
               AiPhysics.MoveInput input = new AiPhysics.MoveInput(forward, strafe, false, original.sprint(), false, player.getYRot());
               AiLiveWorld world = new AiLiveWorld(mc.level, player);
               if (!wouldFallUnsafely(world, AiPhysics.MotionState.of(player), input)) {
                  return original;
               }

               holdCount++;
               if (player.tickCount - lastHoldTick > 1) {
                  AutismClientAddon.LOG
                     .info("[AI] fall guard: held at the edge at {} (move {})", player.blockPosition().toShortString(), current == null ? "-" : current);
               }

               lastHoldTick = player.tickCount;
               return new Input(original.forward(), original.backward(), original.left(), original.right(), false, true, original.sprint());
            } else {
               return original;
            }
         } else {
            return original;
         }
      } else {
         return original;
      }
   }

   public static boolean isSafeStep(AiPhysics.PhysicsWorld world, AiPhysics.MotionState state, AiPhysics.MoveInput input) {
      if (!input.sneak() && !input.jump() && state.onGround()) {
         int x = (int)Math.floor(state.x());
         int y = (int)Math.floor(state.y() + 0.001);
         int z = (int)Math.floor(state.z());
         return !world.isWater(x, y, z) && !world.isLavaOrUnloaded(x, y, z) && !world.isClimbable(x, y, z) && !world.isClimbable(x, y + 1, z)
            ? wouldFallUnsafely(world, state, new AiPhysics.MoveInput(input.forward(), input.strafe(), false, input.sprint(), false, input.yaw()))
            : false;
      } else {
         return false;
      }
   }

   static boolean wouldFallUnsafely(AiPhysics.PhysicsWorld world, AiPhysics.MotionState state, AiPhysics.MoveInput input) {
      double top = state.y();
      AiPhysics.MotionState s = state;

      for (int tick = 0; tick < 3; tick++) {
         AiPhysics.MotionState next = AiPhysics.stepCompat(s, input, world);
         if (AiPhysics.hitsHazardCompat(world, s, next)) {
            return true;
         }

         s = next;
         if (!s.onGround() && !isClimbableAt(world, s)) {
            return fallIsUnsafe(world, s, top);
         }
      }

      return false;
   }

   static boolean wouldFallIntoVoid(AiPhysics.PhysicsWorld world, AiPhysics.MotionState state, AiPhysics.MoveInput input) {
      AiPhysics.MotionState s = state;
      double top = state.y();
      AiPhysics.MoveInput held = new AiPhysics.MoveInput(input.forward(), input.strafe(), false, input.sprint(), false, input.yaw());

      for (int tick = 0; tick < 90; tick++) {
         s = AiPhysics.stepCompat(s, tick == 0 ? input : (tick < 3 ? held : AiPhysics.MoveInput.NONE), world);
         if (isWaterAt(world, s) || isClimbableAt(world, s)) {
            return false;
         }

         if (s.onGround() && tick >= 3) {
            return false;
         }

         if (top - s.y() > 64.0) {
            return true;
         }
      }

      return false;
   }

   private static boolean fallIsUnsafe(AiPhysics.PhysicsWorld world, AiPhysics.MotionState s, double top) {
      for (int tick = 0; tick < 80; tick++) {
         AiPhysics.MotionState next = AiPhysics.stepCompat(s, AiPhysics.MoveInput.NONE, world);
         if (AiPhysics.hitsHazardCompat(world, s, next)) {
            return true;
         }

         if (isWaterAt(world, next)) {
            return isTrappedWater(next);
         }

         if (isClimbableAt(world, next)) {
            return false;
         }

         s = next;
         if (s.onGround()) {
            return top - s.y() > 3.000001;
         }
      }

      return true;
   }

   private static boolean isClimbableAt(AiPhysics.PhysicsWorld world, AiPhysics.MotionState s) {
      int x = (int)Math.floor(s.x());
      int y = (int)Math.floor(s.y() + 0.001);
      int z = (int)Math.floor(s.z());
      return world.isClimbable(x, y, z) || world.isClimbable(x, y + 1, z);
   }

   private static boolean isWaterAt(AiPhysics.PhysicsWorld world, AiPhysics.MotionState s) {
      return world.isWater((int)Math.floor(s.x()), (int)Math.floor(s.y() + 0.1), (int)Math.floor(s.z()));
   }
}
