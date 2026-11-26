package autismclient.util;

import autismclient.modules.ClutchModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class ServerRotationTracker {
   private static volatile ServerRotationTracker.TickPair latestTickPair = ServerRotationTracker.TickPair.empty();
   private static volatile float lastWireYaw = 0.0F / 0.0F;

   private ServerRotationTracker() {
   }

   public static void onOutgoingPacket(Packet<?> packet) {
      if (!AutismLiteVariant.enabled()) {
         ClutchModule.init_V_Packet_2(packet);
      }

      if (packet instanceof ServerboundMovePlayerPacket movement) {
         Vec3 position = null;
         if (movement.hasPosition()) {
            double x = movement.getX(0.0);
            double y = movement.getY(0.0);
            double z = movement.getZ(0.0);
            if (Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)) {
               position = new Vec3(x, y, z);
            }
         }

         float yaw = movement.getYRot(0.0F);
         if (movement.hasRotation() && Float.isFinite(yaw)) {
            lastWireYaw = yaw;
         }

         recordMovementState(movement.getYRot(0.0F), movement.getXRot(0.0F), AutismSessionState.getInstance().getClientTick(), position, movement.hasRotation());
      }
   }

   static void recordRotation(float yaw, float pitch) {
      recordRotation(yaw, pitch, AutismSessionState.getInstance().getClientTick());
   }

   static void recordRotation(float yaw, float pitch, int tick) {
      recordMovementState(yaw, pitch, tick, null, true);
   }

   private static synchronized void recordMovementState(float yaw, float pitch, int tick, Vec3 position, boolean hasRotation) {
      ServerRotationTracker.TickPair old = latestTickPair;
      Vec3 nextPosition = position == null ? old.position() : position;
      if (hasRotation && Float.isFinite(yaw) && Float.isFinite(pitch)) {
         ServerRotationTracker.Angles next = normalizeAngles(yaw, pitch);
         if (old.current() == null) {
            latestTickPair = new ServerRotationTracker.TickPair(next, next, tick, nextPosition);
         } else if (old.tick() == tick) {
            latestTickPair = new ServerRotationTracker.TickPair(old.previous(), next, tick, nextPosition);
         } else {
            latestTickPair = new ServerRotationTracker.TickPair(old.current(), next, tick, nextPosition);
         }
      } else {
         latestTickPair = new ServerRotationTracker.TickPair(old.previous(), old.current(), old.tick(), nextPosition);
      }
   }

   public static synchronized void reset() {
      latestTickPair = ServerRotationTracker.TickPair.empty();
      lastWireYaw = 0.0F / 0.0F;
   }

   public static float getLastWireYaw() {
      return lastWireYaw;
   }

   public static ServerRotationTracker.RotationSnapshot getSnapshot() {
      ServerRotationTracker.TickPair value = latestTickPair;
      ServerRotationTracker.Angles previous = value.previous();
      ServerRotationTracker.Angles current = value.current();
      return new ServerRotationTracker.RotationSnapshot(
         previous == null ? 0.0F / 0.0F : previous.yaw(),
         previous == null ? 0.0F / 0.0F : previous.pitch(),
         current == null ? 0.0F / 0.0F : current.yaw(),
         current == null ? 0.0F / 0.0F : current.pitch(),
         value.tick(),
         current != null,
         value.position()
      );
   }

   static ServerRotationTracker.Angles getCurrentAngles() {
      return latestTickPair.current();
   }

   static ServerRotationTracker.TickPair getLatestTickPair() {
      return latestTickPair;
   }

   public static void applyRenderRotation(Entity entity, EntityRenderState state, float partialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      if (entity != null && entity == minecraft.player && entity == minecraft.getCameraEntity() && state instanceof LivingEntityRenderState livingState) {
         boolean thirdPerson = minecraft.options != null && !minecraft.options.getCameraType().isFirstPerson();
         boolean freecam = autismclient.modules.FreecamController.isActive();
         boolean hidden = autismclient.modules.aD.init_Z();
         if (thirdPerson && !freecam && !hidden) {
            boolean silentOwner = minecraft.player != null && SilentMovementCoordinator.getRenderRotation(minecraft.player) != null;
            if (silentOwner) {
               ServerRotationTracker.Angles rotation = interpolateForTick(latestTickPair, partialTick, AutismSessionState.getInstance().getClientTick());
               if (rotation != null) {
                  applyToLivingRenderState(livingState, rotation);
                  if (state instanceof AvatarRenderState avatarState) {
                     applyFallFlyingYaw(entity, avatarState, rotation);
                  }
               }
            }
         }
      }
   }

   static boolean shouldApplyRenderRotation(boolean localPlayer, boolean cameraEntity, boolean thirdPerson, boolean freecam, boolean hidden, boolean silentOwner) {
      return localPlayer && cameraEntity && thirdPerson && !freecam && !hidden && silentOwner;
   }

   static ServerRotationTracker.Angles interpolateForTick(ServerRotationTracker.TickPair value, float partialTick, int renderTick) {
      if (value != null && value.current() != null) {
         ServerRotationTracker.Angles previous = value.previous() == null ? value.current() : value.previous();
         float progress;
         if (renderTick < value.tick()) {
            progress = 0.0F;
         } else if (renderTick == value.tick()) {
            progress = Mth.clamp(partialTick, 0.0F, 1.0F);
         } else {
            progress = 1.0F;
         }

         return lerpAngles(previous, value.current(), progress);
      } else {
         return null;
      }
   }

   static ServerRotationTracker.Angles lerpAngles(ServerRotationTracker.Angles previous, ServerRotationTracker.Angles current, float progress) {
      if (previous == null) {
         return current;
      }

      if (current == null) {
         return previous;
      }

      float amount = Mth.clamp(progress, 0.0F, 1.0F);
      float yawDelta = Mth.wrapDegrees(current.yaw() - previous.yaw());
      float yaw = Mth.wrapDegrees(previous.yaw() + yawDelta * amount);
      float pitch = previous.pitch() + (current.pitch() - previous.pitch()) * amount;
      return normalizeAngles(yaw, pitch);
   }

   private static ServerRotationTracker.Angles normalizeAngles(float yaw, float pitch) {
      return new ServerRotationTracker.Angles(Mth.wrapDegrees(yaw), Mth.clamp(pitch, -90.0F, 90.0F));
   }

   static void applyToLivingRenderState(LivingEntityRenderState state, ServerRotationTracker.Angles rotation) {
      if (state != null && rotation != null) {
         state.bodyRot = rotation.yaw();
         state.yRot = 0.0F;
         state.xRot = state.isUpsideDown ? -rotation.pitch() : rotation.pitch();
      }
   }

   private static void applyFallFlyingYaw(Entity entity, AvatarRenderState state, ServerRotationTracker.Angles rotation) {
      if (state.isFallFlying && entity != null) {
         Vec3 look = Vec3.directionFromRotation(rotation.pitch(), rotation.yaw());
         Vec3 movement = entity.getDeltaMovement();
         if (!(movement.horizontalDistanceSqr() <= 1.0E-5) && !(look.horizontalDistanceSqr() <= 1.0E-5)) {
            Vec3 horizontalMovement = movement.horizontal().normalize();
            Vec3 horizontalLook = look.horizontal().normalize();
            double dot = Mth.clamp(horizontalMovement.dot(horizontalLook), -1.0, 1.0);
            double sign = movement.x * look.z - movement.z * look.x;
            state.shouldApplyFlyingYRot = true;
            state.flyingYRot = (float)(Math.signum(sign) * Math.acos(Math.abs(dot)));
         } else {
            state.shouldApplyFlyingYRot = false;
            state.flyingYRot = 0.0F;
         }
      }
   }

   public record RotationSnapshot(float previousYaw, float previousPitch, float currentYaw, float currentPitch, int tick, boolean initialized, Vec3 position) {
      public RotationSnapshot(float previousYaw, float previousPitch, float currentYaw, float currentPitch, int tick, boolean initialized) {
         this(previousYaw, previousPitch, currentYaw, currentPitch, tick, initialized, null);
      }
   }

   record Angles(float yaw, float pitch) {
   }

   record TickPair(ServerRotationTracker.Angles previous, ServerRotationTracker.Angles current, int tick, Vec3 position) {
      TickPair(ServerRotationTracker.Angles previous, ServerRotationTracker.Angles current, int tick) {
         this(previous, current, tick, null);
      }

      static ServerRotationTracker.TickPair empty() {
         return new ServerRotationTracker.TickPair(null, null, -2147483648);
      }
   }
}
