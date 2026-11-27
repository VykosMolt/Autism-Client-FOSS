package autismclient.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class ClutchFallPhysics {
   static final double MAX_SIMULATED_DROP = 12.0;
   static final int MAX_SIMULATION_TICKS = 64;
   static final double VOID_FALL_THRESHOLD = 40.0;

   static boolean matchesPlacementHit(BlockHitResult written, BlockPos support, Direction face) {
      return written != null
         && written.getType() == Type.BLOCK
         && written.getBlockPos().equals(support)
         && written.getDirection() == face
         && !written.isInside();
   }

   static AABB predictLeadBox(AABB drawn, Vec3 latestOffset, Vec3 step, int lead) {
      int ticks = Math.max(0, Math.min(10, lead));
      Vec3 motion = isFiniteVec(step) ? step : Vec3.ZERO;
      Vec3 offset = isFiniteVec(latestOffset) ? latestOffset : Vec3.ZERO;
      double interpolationTicks = 3.0;
      return drawn.expandTowards(motion.scale(ticks))
         .minmax(drawn.move(offset).expandTowards(motion.scale(Math.max(0, ticks - 3))))
         .minmax(drawn.expandTowards(offset.scale(Math.max(1.0, ticks / interpolationTicks))));
   }

   static AABB predictSweptLeadBox(AABB drawn, Vec3 latestOffset, Vec3 step, int lead) {
      Vec3 motion = isFiniteVec(step) ? step : Vec3.ZERO;
      Vec3 offset = isFiniteVec(latestOffset) ? latestOffset : Vec3.ZERO;
      double speed = Math.max(motion.horizontalDistance(), offset.horizontalDistance() / 3.0);
      double radius = speed * Math.max(0, Math.min(10, lead));
      return predictLeadBox(drawn, offset, motion, lead).minmax(drawn.inflate(radius, 0.0, radius));
   }

   static boolean isAllowedCause(ClutchFallPhysics.Inner_TaskQueue hit, boolean ownAllowed, boolean melee, boolean projectiles, boolean explosions) {
      if (hit == null || !hit.confirmed) {
         return false;
      }

      if (hit.own && hit.cause != ClutchFallPhysics.Inner_StreamUtil.FALL) {
         return false;
      }

      boolean var10000;
      switch (hit.cause) {
         case MELEE:
         case SERVER_PUSH:
            var10000 = melee;
            break;
         case FIREBALL:
         case WIND:
         case BLAST:
            var10000 = explosions;
            break;
         case ARROW:
         case SNOWBALL:
            var10000 = projectiles;
            break;
         case FALL:
            var10000 = ownAllowed;
            break;
         case GUARD:
            var10000 = true;
            break;
         case OTHER:
            var10000 = false;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static boolean isSourceEnabled(ClutchFallPhysics.Inner_TaskQueue hit, boolean enemyEnabled, boolean ownEnabled) {
      return isSelfOrGuardCause(hit) ? ownEnabled : enemyEnabled;
   }

   static boolean isSelfOrGuardCause(ClutchFallPhysics.Inner_TaskQueue hit) {
      return hit == null || hit.own() || hit.cause() == ClutchFallPhysics.Inner_StreamUtil.FALL || hit.cause() == ClutchFallPhysics.Inner_StreamUtil.GUARD;
   }

   static int choosePlacementTick(int earliest, int latest, boolean enemyFlight) {
      return enemyFlight ? Math.max(1, earliest) : Math.max(1, latest >= 3 ? latest - 1 : latest);
   }

   static int clampEarliestTick(int earliest, int latest) {
      return Math.max(earliest, Math.min(latest, Math.max(1, earliest)));
   }

   static int normalizeMode(int mode) {
      return mode == 3 ? 0 : mode;
   }

   static boolean isEnemyCause(ClutchFallPhysics.Inner_TaskQueue hit) {
      return hit != null && !isSelfOrGuardCause(hit);
   }

   static boolean isWithinTickWindow(int now, int then, int window) {
      return now - then >= 0 && now - then <= window;
   }

   static boolean isRisingInFlight(boolean continuousFlight, double climb, double vertical) {
      return continuousFlight && climb >= 1.3 && vertical > 0.15;
   }

   static boolean isRecentKnockbackLaunch(int now, int damageTick, int pushTick, int groundTick, int window) {
      int launch = isWithinTickWindow(pushTick, damageTick, 4) ? pushTick : damageTick;
      return isWithinTickWindow(now, launch, window) && !isWithinTickWindow(groundTick, launch + 2, window);
   }

   static boolean shouldDeployRescue(int tick, int groundTick, double feetY, double rowY, double vertical, boolean boost, boolean scaffold) {
      if (boost) {
         return vertical < 0.0 && feetY < rowY;
      } else {
         return !scaffold ? isWithinTickWindow(tick, groundTick, 40) : vertical < 0.0 && feetY < rowY + 0.98 && isWithinTickWindow(tick, groundTick, 40);
      }
   }

   static ClutchFallPhysics.Inner_ConfigStore simulateJumpTrajectory(
      autismclient.ai.engine.AiPhysics.MotionState start,
      autismclient.ai.engine.AiPhysics.MoveInput pressed,
      autismclient.ai.engine.AiPhysics.PhysicsWorld world,
      double maxDrop,
      Boolean axis,
      ClutchFallPhysics.Inner_JsonUtil medium
   ) {
      return simulateMotionTrajectory(start, pressed, world, maxDrop, axis, medium, true);
   }

   static ClutchFallPhysics.Inner_ConfigStore simulateWalkTrajectory(
      autismclient.ai.engine.AiPhysics.MotionState start,
      autismclient.ai.engine.AiPhysics.MoveInput pressed,
      autismclient.ai.engine.AiPhysics.PhysicsWorld world,
      double maxDrop,
      ClutchFallPhysics.Inner_JsonUtil medium
   ) {
      return simulateMotionTrajectory(start, pressed, world, maxDrop, null, medium, false);
   }

   private static ClutchFallPhysics.Inner_ConfigStore simulateMotionTrajectory(
      autismclient.ai.engine.AiPhysics.MotionState start,
      autismclient.ai.engine.AiPhysics.MoveInput pressed,
      autismclient.ai.engine.AiPhysics.PhysicsWorld world,
      double maxDrop,
      Boolean axis,
      ClutchFallPhysics.Inner_JsonUtil medium,
      boolean jump
   ) {
      List<ClutchFallPhysics.Inner_UiHelper> steps = new ArrayList<>(32);
      autismclient.ai.engine.AiPhysics.MotionState state = start;
      boolean airborne = false;
      double fallen = 0.0;
      int edge = 2147483647;

      for (int tick = 1; tick <= 32; tick++) {
         autismclient.ai.engine.AiPhysics.MoveInput input = pressed.withJump(jump && tick == 1);
         if (tick > 1 && axis != null) {
            Vec3 wanted = brakingAxisDirection(new Vec3(state.vx(), state.vy(), state.vz()), axis);
            double yaw = Math.toRadians(pressed.yaw());
            double forward = wanted.z * Math.cos(yaw) - wanted.x * Math.sin(yaw);
            double side = wanted.x * Math.cos(yaw) + wanted.z * Math.sin(yaw);
            input = new autismclient.ai.engine.AiPhysics.MoveInput(
               Math.abs(forward) > 0.08 ? Math.signum(forward) : 0.0, Math.abs(side) > 0.08 ? Math.signum(side) : 0.0, false, false, false, pressed.yaw()
            );
         }

         double beforeY = state.y();
         state = jump ? autismclient.ai.engine.AiPhysics.stepCompat(state, input, world) : autismclient.ai.engine.AiPhysics.stepUnclippedCompat(state, input, world);
         AABB body = state.box(false);
         if (!state.onGround()) {
            airborne = true;
            if (edge == 2147483647) {
               edge = tick;
            }
         }

         steps.add(new ClutchFallPhysics.Inner_UiHelper(body, new Vec3(state.vx(), state.vy(), state.vz()), tick, state.onGround()));
         ClutchFallPhysics.Inner_BufferHelper contact = medium == null ? ClutchFallPhysics.Inner_BufferHelper.NONE : medium.at(body);
         if (contact == ClutchFallPhysics.Inner_BufferHelper.CUSHION) {
            return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), false, edge);
         }

         fallen += Math.max(0.0, beforeY - state.y());
         if (contact == ClutchFallPhysics.Inner_BufferHelper.HAZARD || fallen > maxDrop && state.onGround()) {
            return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), true, edge);
         }

         if (airborne
            && state.onGround()
            && world.init_List(body.move(0.0, -0.04, 0.0)).stream().anyMatch(block -> block.intersects(body.move(0.0, -0.04, 0.0)))
            && canMotionSettleSafely(state, world, maxDrop, medium)) {
            return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), false, edge);
         }

         if (state.onGround()) {
            fallen = 0.0;
         }
      }

      return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), jump || airborne, edge);
   }

   static autismclient.ai.engine.AiPhysics.MotionState withStandingPose(autismclient.ai.engine.AiPhysics.MotionState s) {
      return new autismclient.ai.engine.AiPhysics.MotionState(
         s.x(),
         s.y(),
         s.z(),
         s.vx(),
         s.vy(),
         s.vz(),
         s.onGround(),
         s.jumpDelay(),
         s.horizontalCollision(),
         s.verticalCollision(),
         s.support(),
         s.sprintCut(),
         0,
         s.stuck()
      );
   }

   static ClutchFallPhysics.Inner_ConfigStore simulateMotionFall(
      autismclient.ai.engine.AiPhysics.MotionState start,
      int sneakTicks,
      float yaw,
      double maxDrop,
      autismclient.ai.engine.AiPhysics.PhysicsWorld world,
      ClutchFallPhysics.Inner_SyncManager steering,
      ClutchFallPhysics.Inner_JsonUtil medium
   ) {
      return simulateMotionFall(start, sneakTicks, yaw, maxDrop, world, steering, medium, List.of(), 0);
   }

   static ClutchFallPhysics.Inner_ConfigStore simulateMotionFall(
      autismclient.ai.engine.AiPhysics.MotionState start,
      int sneakTicks,
      float yaw,
      double maxDrop,
      autismclient.ai.engine.AiPhysics.PhysicsWorld world,
      ClutchFallPhysics.Inner_SyncManager steering,
      ClutchFallPhysics.Inner_JsonUtil medium,
      List<AABB> extra,
      int placementTick
   ) {
      return simulateMotionFall(start, sneakTicks, yaw, maxDrop, world, steering, medium, extra, placementTick, 0.0);
   }

   static ClutchFallPhysics.Inner_ConfigStore simulateMotionFall(
      autismclient.ai.engine.AiPhysics.MotionState start,
      int sneakTicks,
      float yaw,
      double maxDrop,
      final autismclient.ai.engine.AiPhysics.PhysicsWorld world,
      ClutchFallPhysics.Inner_SyncManager steering,
      ClutchFallPhysics.Inner_JsonUtil medium,
      final List<AABB> extra,
      final int placementTick,
      double fallenSoFar
   ) {
      List<ClutchFallPhysics.Inner_UiHelper> steps = new ArrayList<>(64);
      List<Vec3> eyes = new ArrayList<>(65);
      autismclient.ai.engine.AiPhysics.MotionState state = start;
      eyes.add(eyePosition(state));
      int edge = 2147483647;
      boolean exceeded = false;
      double fallen = Math.max(0.0, fallenSoFar);
      double allowed = Math.max(0.5, Math.min(maxDrop, 12.0));
      final int[] at = new int[]{0};
      autismclient.ai.engine.AiPhysics.PhysicsWorld collisionWorld = extra.isEmpty() ? world : new autismclient.ai.engine.AiPhysics.PhysicsWorld() {
         @Override
         public List<AABB> init_List(AABB query) {
            List<AABB> base = world.init_List(query);
            if (at[0] <= placementTick) {
               return base;
            }

            List<AABB> boxes = new ArrayList<>(base);

            for (AABB block : extra) {
               if (query.intersects(block)) {
                  boxes.add(block);
               }
            }

            return boxes;
         }

         @Override
         public double load() {
            return world.load();
         }

         @Override
         public double init_D_I(int x, int y, int z) {
            return world.init_D_I(x, y, z);
         }

         @Override
         public double load_D(int x, int y, int z) {
            return world.load_D(x, y, z);
         }

         @Override
         public double write_D(int x, int y, int z) {
            return world.write_D(x, y, z);
         }

         @Override
         public double read_D(int x, int y, int z) {
            return world.read_D(x, y, z);
         }

         @Override
         public boolean sync(int x, int y, int z) {
            return world.sync(x, y, z);
         }

         @Override
         public boolean init_Z() {
            return world.init_Z();
         }

         @Override
         public int init_I(AABB box) {
            return world.init_I(box);
         }

         @Override
         public boolean load_Z(int x, int y, int z) {
            return world.load_Z(x, y, z);
         }

         @Override
         public boolean init_Z_I(int x, int y, int z) {
            return world.init_Z_I(x, y, z);
         }

         @Override
         public boolean read_Z(int x, int y, int z) {
            return world.read_Z(x, y, z);
         }

         @Override
         public boolean write_Z(int x, int y, int z) {
            return world.write_Z(x, y, z);
         }

         @Override
         public boolean update(int x, int y, int z) {
            return world.update(x, y, z);
         }
      };

      for (int tick = 1; tick <= 64; tick++) {
         at[0] = tick;
         Vec3 motion = new Vec3(state.vx(), state.vy(), state.vz());
         Vec3 wanted = steering.acceleration(state.box(false), motion, state.onGround());
         double angle = Math.toRadians(yaw);
         double forward = wanted.z * Math.cos(angle) - wanted.x * Math.sin(angle);
         double side = wanted.x * Math.cos(angle) + wanted.z * Math.sin(angle);
         autismclient.ai.engine.AiPhysics.MoveInput keys = new autismclient.ai.engine.AiPhysics.MoveInput(
            Math.abs(forward) > 0.08 ? Math.signum(forward) : 0.0, Math.abs(side) > 0.08 ? Math.signum(side) : 0.0, false, false, tick <= sneakTicks, yaw
         );
         double previousY = state.y();
         state = autismclient.ai.engine.AiPhysics.stepCompat(state, keys, collisionWorld);
         AABB body = state.box(false);
         if (!state.onGround() && edge == 2147483647) {
            edge = tick;
         }

         steps.add(new ClutchFallPhysics.Inner_UiHelper(body, new Vec3(state.vx(), state.vy(), state.vz()), tick, state.onGround()));
         eyes.add(eyePosition(state));
         ClutchFallPhysics.Inner_BufferHelper contact = medium == null ? ClutchFallPhysics.Inner_BufferHelper.NONE : medium.at(body);
         fallen += Math.max(0.0, previousY - state.y());
         exceeded |= fallen > allowed;
         if (contact == ClutchFallPhysics.Inner_BufferHelper.CUSHION) {
            return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), false, edge, List.copyOf(eyes));
         }

         if (contact == ClutchFallPhysics.Inner_BufferHelper.HAZARD || exceeded && state.onGround()) {
            return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), true, edge, List.copyOf(eyes));
         }

         if (state.onGround()) {
            AABB feet = body.move(0.0, -0.04, 0.0);
            if (!exceeded
               && collisionWorld.init_List(feet).stream().anyMatch(feet::intersects)
               && canMotionSettleSafely(state, collisionWorld, allowed, medium)) {
               return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), false, edge, List.copyOf(eyes));
            }

            fallen = 0.0;
         }
      }

      return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), true, edge, List.copyOf(eyes));
   }

   private static Vec3 eyePosition(autismclient.ai.engine.AiPhysics.MotionState state) {
      return state.position().add(0.0, state.pose() == 0 ? 1.62 : (state.pose() == 2 ? 0.4 : 1.27), 0.0);
   }

   static Vec3 computeRelativeMoveInput(Vec3 wanted, float cameraYaw, float outgoingYaw, float slowdown) {
      double yaw = Math.toRadians(cameraYaw);
      double forward = wanted.z * Math.cos(yaw) - wanted.x * Math.sin(yaw);
      double side = wanted.x * Math.cos(yaw) + wanted.z * Math.sin(yaw);
      Input keys = new Input(forward > 0.08, forward < -0.08, side > 0.08, side < -0.08, false, false, false);
      keys = ScaffoldModule.init_Input_Input_2(keys, cameraYaw, outgoingYaw);
      Vec2 input = new Vec2((keys.left() ? 1 : 0) - (keys.right() ? 1 : 0), (keys.forward() ? 1 : 0) - (keys.backward() ? 1 : 0))
         .normalized()
         .scale(0.98F)
         .scale(slowdown);
      float length = input.length();
      if (length <= 0.0F) {
         return Vec3.ZERO;
      }

      Vec2 direction = input.scale(1.0F / length);
      float x = Math.abs(direction.x);
      float z = Math.abs(direction.y);
      float tan = z > x ? x / z : z / x;
      float square = Mth.sqrt(1.0F + tan * tan);
      input = direction.scale(Math.min(length * square, 1.0F));
      return new Vec3(input.x, 0.0, input.y);
   }

   static ClutchFallPhysics.Inner_DataUtil predictAirSweep(
      AABB start, Vec3 velocity, double eyeHeight, float cameraYaw, float outgoingYaw, int lead, ClutchFallPhysics.Inner_SyncManager steering, ClutchFallPhysics.Inner_StateCache nativeInput
   ) {
      if (lead >= 0 && lead <= 1) {
         AABB body = start;
         AABB swept = start;
         Vec3 motion = velocity;
         Vec3 eye = null;

         for (int tick = 0; tick <= lead; tick++) {
            if (tick == lead) {
               eye = eyePosition(body, eyeHeight);
            }

            boolean stopped = motion.horizontalDistanceSqr() < 9.0E-6;
            motion = new Vec3(stopped ? 0.0 : motion.x, Math.abs(motion.y) < 0.003 ? 0.0 : motion.y, stopped ? 0.0 : motion.z);
            Vec3 wanted = steering.acceleration(body, motion, false);
            Vec3 relative = computeRelativeMoveInput(wanted, cameraYaw, outgoingYaw, 1.0F);
            Vec3 moved = motion.add(nativeInput.movement(relative, 0.02F, outgoingYaw));
            body = body.move(moved);
            swept = swept.minmax(body);
            motion = new Vec3(moved.x * 0.9100000262260437, (moved.y - 0.08) * 0.9800000190734863, moved.z * 0.9100000262260437);
         }

         return new ClutchFallPhysics.Inner_DataUtil(eye, eyePosition(body, eyeHeight), swept);
      } else {
         return null;
      }
   }

   private static Vec3 eyePosition(AABB body, double height) {
      return new Vec3(body.getCenter().x, body.minY + height, body.getCenter().z);
   }

   static boolean hasImpulseChange(Vec3 before, Vec3 after, boolean hurt) {
      if (isFiniteVec(before) && isFiniteVec(after)) {
         Vec3 change = after.subtract(before);
         if (hurt && change.lengthSqr() > 1.0E-6 && after.lengthSqr() > 1.0E-4) {
            return true;
         }

         double horizontal = before.horizontalDistanceSqr();
         double retained = horizontal > 1.0E-9 ? Math.max(0.0, Math.min(1.0, (after.x * before.x + after.z * before.z) / horizontal)) : 0.0;
         double x = after.x - before.x * retained;
         double z = after.z - before.z * retained;
         double gravityStep = Math.min(0.0, (before.y - 0.08) * 0.98);
         return x * x + z * z >= 1.0E-4 || change.y >= 0.01 && after.y > 0.0 || after.y < gravityStep - 0.01;
      } else {
         return false;
      }
   }

   static ClutchFallPhysics.Inner_ConfigStore simulateBoxFallWithAccel(AABB start, Vec3 velocity, Vec3 acceleration, double maxDrop, ClutchFallPhysics.Inner_CachePool world) {
      return simulateBoxFallWithAccel(start, velocity, acceleration, maxDrop, world, null);
   }

   static ClutchFallPhysics.Inner_ConfigStore simulateBoxFallWithAccel(AABB start, Vec3 velocity, Vec3 acceleration, double maxDrop, ClutchFallPhysics.Inner_CachePool world, ClutchFallPhysics.Inner_JsonUtil medium) {
      return start != null && isFiniteVec(velocity) && isFiniteVec(acceleration) && world != null
         ? simulateBoxFall(start, velocity, maxDrop, world, (body, motion, supported) -> acceleration, medium)
         : new ClutchFallPhysics.Inner_ConfigStore(List.of(), false, 2147483647);
   }

   static ClutchFallPhysics.Inner_ConfigStore simulateBoxFallWithAccel(
      AABB start, Vec3 velocity, Vec3 acceleration, double maxDrop, ClutchFallPhysics.Inner_CachePool world, ClutchFallPhysics.Inner_JsonUtil medium, double fallenSoFar
   ) {
      return !isFiniteVec(acceleration)
         ? new ClutchFallPhysics.Inner_ConfigStore(List.of(), false, 2147483647)
         : simulateBoxFall(start, velocity, maxDrop, world, (body, motion, supported) -> acceleration, medium, world, 0, fallenSoFar);
   }

   static ClutchFallPhysics.Inner_ConfigStore simulateBoxFall(AABB start, Vec3 velocity, double maxDrop, ClutchFallPhysics.Inner_CachePool world, ClutchFallPhysics.Inner_SyncManager steering) {
      return simulateBoxFall(start, velocity, maxDrop, world, steering, null);
   }

   static ClutchFallPhysics.Inner_ConfigStore simulateBoxFall(AABB start, Vec3 velocity, double maxDrop, ClutchFallPhysics.Inner_CachePool world, ClutchFallPhysics.Inner_SyncManager steering, ClutchFallPhysics.Inner_JsonUtil medium) {
      return simulateBoxFall(start, velocity, maxDrop, world, steering, medium, world, 0);
   }

   static ClutchFallPhysics.Inner_ConfigStore simulateBoxFall(
      AABB start,
      Vec3 velocity,
      double maxDrop,
      ClutchFallPhysics.Inner_CachePool world,
      ClutchFallPhysics.Inner_SyncManager steering,
      ClutchFallPhysics.Inner_JsonUtil medium,
      ClutchFallPhysics.Inner_CachePool afterPlacement,
      int placementTick
   ) {
      return simulateBoxFall(start, velocity, maxDrop, world, steering, medium, afterPlacement, placementTick, 0.0);
   }

   static ClutchFallPhysics.Inner_ConfigStore simulateBoxFall(
      AABB start,
      Vec3 velocity,
      double maxDrop,
      ClutchFallPhysics.Inner_CachePool world,
      ClutchFallPhysics.Inner_SyncManager steering,
      ClutchFallPhysics.Inner_JsonUtil medium,
      ClutchFallPhysics.Inner_CachePool afterPlacement,
      int placementTick,
      double fallenSoFar
   ) {
      if (start != null && isFiniteVec(velocity) && world != null && steering != null) {
         List<ClutchFallPhysics.Inner_UiHelper> steps = new ArrayList<>(64);
         AABB body = start;
         Vec3 motion = velocity;
         double fallen = Math.max(0.0, fallenSoFar);
         int edge = 2147483647;
         boolean supported = false;
         boolean exceeded = false;
         boolean grounded = isSupported(start, world);
         double allowed = Math.max(0.5, Math.min(maxDrop, 12.0));

         for (int t = 1; t <= 64; t++) {
            Vec3 acceleration = steering.acceleration(body, motion, grounded);
            if (!isFiniteVec(acceleration)) {
               return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), false, edge);
            }

            Vec3 wanted = motion.add(acceleration.x, 0.0, acceleration.z);
            if (Math.abs(wanted.y) < 1.0E-8) {
               wanted = new Vec3(wanted.x, -0.01, wanted.z);
            }

            Vec3 actual = (t > placementTick ? afterPlacement : world).clip(body, wanted);
            if (!isFiniteVec(actual)) {
               return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), false, edge);
            }

            supported = wanted.y < 0.0 && actual.y > wanted.y + 1.0E-7;
            body = body.move(actual);
            if (!supported && edge == 2147483647) {
               edge = t;
            }

            Vec3 var28 = new Vec3(
               Math.abs(actual.x - wanted.x) > 1.0E-7 ? 0.0 : wanted.x,
               Math.abs(actual.y - wanted.y) > 1.0E-7 ? 0.0 : wanted.y,
               Math.abs(actual.z - wanted.z) > 1.0E-7 ? 0.0 : wanted.z
            );
            motion = new Vec3(var28.x * (grounded ? 0.546 : 0.91), (var28.y - 0.08) * 0.98, var28.z * (grounded ? 0.546 : 0.91));
            grounded = supported;
            steps.add(new ClutchFallPhysics.Inner_UiHelper(body, motion, t, supported));
            ClutchFallPhysics.Inner_BufferHelper contact = medium == null ? ClutchFallPhysics.Inner_BufferHelper.NONE : medium.at(body);
            if (contact == ClutchFallPhysics.Inner_BufferHelper.HAZARD) {
               return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), true, edge);
            }

            if (contact == ClutchFallPhysics.Inner_BufferHelper.CUSHION) {
               return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), false, edge);
            }

            fallen += Math.max(0.0, -actual.y);
            if (fallen > allowed) {
               if (supported) {
                  return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), true, edge);
               }

               exceeded = true;
            }

            if (supported) {
               ClutchFallPhysics.Inner_CachePool landingWorld = t > placementTick ? afterPlacement : world;
               if (!exceeded && isSupported(body, landingWorld) && canBoxSettleSafely(body, motion, allowed, landingWorld, medium)) {
                  return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), false, edge);
               }

               fallen = 0.0;
            }
         }

         return new ClutchFallPhysics.Inner_ConfigStore(List.copyOf(steps), true, edge);
      } else {
         return new ClutchFallPhysics.Inner_ConfigStore(List.of(), false, 2147483647);
      }
   }

   private static boolean canBoxSettleSafely(AABB body, Vec3 motion, double allowed, ClutchFallPhysics.Inner_CachePool world, ClutchFallPhysics.Inner_JsonUtil medium) {
      if (motion.horizontalDistanceSqr() < 9.0E-6) {
         return true;
      }

      boolean grounded = true;
      double fallen = 0.0;

      for (int tick = 0; tick < 64; tick++) {
         if (motion.horizontalDistanceSqr() < 9.0E-6) {
            motion = new Vec3(0.0, motion.y, 0.0);
         }

         Vec3 wanted = Math.abs(motion.y) < 1.0E-8 ? new Vec3(motion.x, -0.01, motion.z) : motion;
         Vec3 actual = world.clip(body, wanted);
         if (!isFiniteVec(actual)) {
            return false;
         }

         boolean supported = wanted.y < 0.0 && actual.y > wanted.y + 1.0E-7;
         body = body.move(actual);
         ClutchFallPhysics.Inner_BufferHelper contact = medium == null ? ClutchFallPhysics.Inner_BufferHelper.NONE : medium.at(body);
         if (contact == ClutchFallPhysics.Inner_BufferHelper.HAZARD) {
            return false;
         }

         if (contact == ClutchFallPhysics.Inner_BufferHelper.CUSHION) {
            return true;
         }

         fallen += Math.max(0.0, -actual.y);
         if (fallen > allowed) {
            return false;
         }

         motion = new Vec3(
            Math.abs(actual.x - wanted.x) > 1.0E-7 ? 0.0 : wanted.x * (grounded ? 0.546 : 0.91),
            (Math.abs(actual.y - wanted.y) > 1.0E-7 ? 0.0 : wanted.y) - 0.08,
            Math.abs(actual.z - wanted.z) > 1.0E-7 ? 0.0 : wanted.z * (grounded ? 0.546 : 0.91)
         );
         motion = new Vec3(motion.x, motion.y * 0.98, motion.z);
         grounded = supported;
         if (supported) {
            if (motion.horizontalDistanceSqr() < 9.0E-6 && isSupported(body, world)) {
               return true;
            }

            fallen = 0.0;
         }
      }

      return false;
   }

   private static boolean canMotionSettleSafely(
      autismclient.ai.engine.AiPhysics.MotionState start, autismclient.ai.engine.AiPhysics.PhysicsWorld world, double allowed, ClutchFallPhysics.Inner_JsonUtil medium
   ) {
      autismclient.ai.engine.AiPhysics.MotionState state = start;
      double fallen = 0.0;

      for (int tick = 0; tick < 64; tick++) {
         double before = state.y();
         state = autismclient.ai.engine.AiPhysics.stepUnclippedCompat(state, autismclient.ai.engine.AiPhysics.MoveInput.NONE, world);
         AABB body = state.box(false);
         ClutchFallPhysics.Inner_BufferHelper contact = medium == null ? ClutchFallPhysics.Inner_BufferHelper.NONE : medium.at(body);
         if (contact == ClutchFallPhysics.Inner_BufferHelper.HAZARD) {
            return false;
         }

         if (contact == ClutchFallPhysics.Inner_BufferHelper.CUSHION) {
            return true;
         }

         fallen += Math.max(0.0, before - state.y());
         if (fallen > allowed) {
            return false;
         }

         if (state.onGround()) {
            AABB feet = body.move(0.0, -0.04, 0.0);
            if (state.vx() * state.vx() + state.vz() * state.vz() < 1.0E-12 && world.init_List(feet).stream().anyMatch(feet::intersects)) {
               return true;
            }

            fallen = 0.0;
         }
      }

      return false;
   }

   static boolean isSupported(AABB body, ClutchFallPhysics.Inner_CachePool world) {
      Vec3 probe = world.clip(body, new Vec3(0.0, -0.01, 0.0));
      return isFiniteVec(probe) && probe.y > -0.0099999;
   }

   static boolean isFiniteVec(Vec3 value) {
      return value != null && Double.isFinite(value.x) && Double.isFinite(value.y) && Double.isFinite(value.z);
   }

   static boolean isGroundedSafe(AABB body, Vec3 velocity, boolean grounded, double maxDrop, ClutchFallPhysics.Inner_CachePool world, ClutchFallPhysics.Inner_JsonUtil medium) {
      if (!grounded || body == null || !isFiniteVec(velocity) || world == null) {
         return false;
      }

      if (velocity.y > 0.0) {
         return false;
      }

      Vec3 support = world.clip(body, new Vec3(0.0, -0.05, 0.0));
      return isFiniteVec(support) && !(support.y <= -0.0499999) ? !simulateBoxFallWithAccel(body, velocity, Vec3.ZERO, maxDrop, world, medium).unsafe() : false;
   }

   static boolean isDangerousDrop(double fallen, double below, boolean voidOnly, double height) {
      if (Double.isNaN(below)) {
         return false;
      } else {
         return !Double.isInfinite(below) && !(fallen + below >= 40.0) ? !voidOnly && fallen + below >= height : true;
      }
   }

   static double estimateFallDamage(double drop) {
      return Math.max(0.0, Math.ceil(drop - 3.0));
   }

   static Vec3 brakingDirection(Vec3 motion) {
      return isFiniteVec(motion) && !(motion.horizontalDistanceSqr() <= 6.25E-4) ? new Vec3(-motion.x, 0.0, -motion.z).normalize() : Vec3.ZERO;
   }

   static Vec3 brakingAxisDirection(Vec3 motion, boolean x) {
      if (!isFiniteVec(motion)) {
         return Vec3.ZERO;
      } else {
         double component = x ? motion.x : motion.z;
         if (Math.abs(component) <= 0.025) {
            return brakingDirection(motion);
         } else {
            return x ? new Vec3(-Math.signum(component), 0.0, 0.0) : new Vec3(0.0, 0.0, -Math.signum(component));
         }
      }
   }

   enum Inner_BufferHelper {
      NONE,
      CUSHION,
      HAZARD;
   }

   interface Inner_CachePool {
      Vec3 clip(AABB var1, Vec3 var2);
   }

   record Inner_ConfigStore(List<ClutchFallPhysics.Inner_UiHelper> steps, boolean unsafe, int edgeTick, List<Vec3> eyePositions) {
      Inner_ConfigStore(List<ClutchFallPhysics.Inner_UiHelper> steps, boolean unsafe, int edgeTick) {
         this(steps, unsafe, edgeTick, List.of());
      }
   }

   record Inner_DataUtil(Vec3 startEye, Vec3 endEye, AABB sweptBox) {
   }

   interface Inner_JsonUtil {
      ClutchFallPhysics.Inner_BufferHelper at(AABB var1);
   }

   static final class Inner_NetHelper {
      private final int lifetime;
      private int activeTick = -2147483648;

      Inner_NetHelper(int lifetime) {
         this.lifetime = lifetime;
      }

      void trigger(int tick) {
         this.activeTick = tick;
      }

      boolean isActive(int tick) {
         return this.activeTick != -2147483648 && ClutchFallPhysics.isWithinTickWindow(tick, this.activeTick, this.lifetime);
      }

      void update(int tick, boolean onGround, double vertical) {
         if (!this.isActive(tick) || onGround && vertical <= 0.0 && tick > this.activeTick) {
            this.clear();
         }
      }

      void clear() {
         this.activeTick = -2147483648;
      }
   }

   @FunctionalInterface
   interface Inner_StateCache {
      Vec3 movement(Vec3 var1, float var2, float var3);
   }

   enum Inner_StreamUtil {
      MELEE,
      SERVER_PUSH,
      FIREBALL,
      WIND,
      BLAST,
      ARROW,
      SNOWBALL,
      FALL,
      GUARD,
      OTHER;
   }

   interface Inner_SyncManager {
      Vec3 acceleration(AABB var1, Vec3 var2, boolean var3);
   }

   record Inner_TaskQueue(ClutchFallPhysics.Inner_StreamUtil cause, boolean own, boolean confirmed, int tick) {
   }

   record Inner_UiHelper(AABB body, Vec3 velocity, int tick, boolean onGround) {
   }
}
