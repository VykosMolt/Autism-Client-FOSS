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
   private static volatile ServerRotationTracker.Inner_StreamUtil init_ch_StreamUtil = ServerRotationTracker.Inner_StreamUtil.init_ch_StreamUtil();
   private static volatile float init_F = 0.0F / 0.0F;

   private ServerRotationTracker() {
   }

   public static void init(Packet<?> packet) {
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
            init_F = yaw;
         }

         init(movement.getYRot(0.0F), movement.getXRot(0.0F), ck.init_ck().state_I(), position, movement.hasRotation());
      }
   }

   static void init_V(float yaw, float pitch) {
      init_V_F(yaw, pitch, ck.init_ck().state_I());
   }

   static void init_V_F(float yaw, float pitch, int tick) {
      init(yaw, pitch, tick, null, true);
   }

   private static synchronized void init(float yaw, float pitch, int tick, Vec3 position, boolean hasRotation) {
      ServerRotationTracker.Inner_StreamUtil old = init_ch_StreamUtil;
      Vec3 nextPosition = position == null ? old.init_Vec3() : position;
      if (hasRotation && Float.isFinite(yaw) && Float.isFinite(pitch)) {
         ServerRotationTracker.Inner_DataUtil next = init_ch_DataUtil(yaw, pitch);
         if (old.load() == null) {
            init_ch_StreamUtil = new ServerRotationTracker.Inner_StreamUtil(next, next, tick, nextPosition);
         } else if (old.init_I() == tick) {
            init_ch_StreamUtil = new ServerRotationTracker.Inner_StreamUtil(old.init_ch_DataUtil(), next, tick, nextPosition);
         } else {
            init_ch_StreamUtil = new ServerRotationTracker.Inner_StreamUtil(old.load(), next, tick, nextPosition);
         }
      } else {
         init_ch_StreamUtil = new ServerRotationTracker.Inner_StreamUtil(old.init_ch_DataUtil(), old.load(), old.init_I(), nextPosition);
      }
   }

   public static synchronized void init_V() {
      init_ch_StreamUtil = ServerRotationTracker.Inner_StreamUtil.init_ch_StreamUtil();
      init_F = 0.0F / 0.0F;
   }

   public static float init_F() {
      return init_F;
   }

   public static ServerRotationTracker.Inner_CachePool init_ch_CachePool() {
      ServerRotationTracker.Inner_StreamUtil value = init_ch_StreamUtil;
      ServerRotationTracker.Inner_DataUtil previous = value.init_ch_DataUtil();
      ServerRotationTracker.Inner_DataUtil current = value.load();
      return new ServerRotationTracker.Inner_CachePool(
         previous == null ? 0.0F / 0.0F : previous.init(),
         previous == null ? 0.0F / 0.0F : previous.load(),
         current == null ? 0.0F / 0.0F : current.init(),
         current == null ? 0.0F / 0.0F : current.load(),
         value.init_I(),
         current != null,
         value.init_Vec3()
      );
   }

   static ServerRotationTracker.Inner_DataUtil init_ch_DataUtil() {
      return init_ch_StreamUtil.load();
   }

   static ServerRotationTracker.Inner_StreamUtil init_ch_StreamUtil() {
      return init_ch_StreamUtil;
   }

   public static void init_V_Entity_2(Entity entity, EntityRenderState state, float partialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      if (entity != null && entity == minecraft.player && entity == minecraft.getCameraEntity() && state instanceof LivingEntityRenderState livingState) {
         boolean thirdPerson = minecraft.options != null && !minecraft.options.getCameraType().isFirstPerson();
         boolean freecam = autismclient.modules.FreecamController.init_Z();
         boolean hidden = autismclient.modules.aD.init_Z();
         if (thirdPerson && !freecam && !hidden) {
            boolean silentOwner = minecraft.player != null && SilentMovementCoordinator.init_ce_DataUtil(minecraft.player) != null;
            if (silentOwner) {
               ServerRotationTracker.Inner_DataUtil rotation = init_ch_DataUtil_ch_StreamUtil(init_ch_StreamUtil, partialTick, ck.init_ck().state_I());
               if (rotation != null) {
                  init(livingState, rotation);
                  if (state instanceof AvatarRenderState avatarState) {
                     init_V_Entity(entity, avatarState, rotation);
                  }
               }
            }
         }
      }
   }

   static boolean init(boolean localPlayer, boolean cameraEntity, boolean thirdPerson, boolean freecam, boolean hidden, boolean silentOwner) {
      return localPlayer && cameraEntity && thirdPerson && !freecam && !hidden && silentOwner;
   }

   static ServerRotationTracker.Inner_DataUtil init_ch_DataUtil_ch_StreamUtil(ServerRotationTracker.Inner_StreamUtil value, float partialTick, int renderTick) {
      if (value != null && value.load() != null) {
         ServerRotationTracker.Inner_DataUtil previous = value.init_ch_DataUtil() == null ? value.load() : value.init_ch_DataUtil();
         float progress;
         if (renderTick < value.init_I()) {
            progress = 0.0F;
         } else if (renderTick == value.init_I()) {
            progress = Mth.clamp(partialTick, 0.0F, 1.0F);
         } else {
            progress = 1.0F;
         }

         return init_ch_DataUtil_ch_DataUtil(previous, value.load(), progress);
      } else {
         return null;
      }
   }

   static ServerRotationTracker.Inner_DataUtil init_ch_DataUtil_ch_DataUtil(ServerRotationTracker.Inner_DataUtil previous, ServerRotationTracker.Inner_DataUtil current, float progress) {
      if (previous == null) {
         return current;
      }

      if (current == null) {
         return previous;
      }

      float amount = Mth.clamp(progress, 0.0F, 1.0F);
      float yawDelta = Mth.wrapDegrees(current.init() - previous.init());
      float yaw = Mth.wrapDegrees(previous.init() + yawDelta * amount);
      float pitch = previous.load() + (current.load() - previous.load()) * amount;
      return init_ch_DataUtil(yaw, pitch);
   }

   private static ServerRotationTracker.Inner_DataUtil init_ch_DataUtil(float yaw, float pitch) {
      return new ServerRotationTracker.Inner_DataUtil(Mth.wrapDegrees(yaw), Mth.clamp(pitch, -90.0F, 90.0F));
   }

   static void init(LivingEntityRenderState state, ServerRotationTracker.Inner_DataUtil rotation) {
      if (state != null && rotation != null) {
         state.bodyRot = rotation.init();
         state.yRot = 0.0F;
         state.xRot = state.isUpsideDown ? -rotation.load() : rotation.load();
      }
   }

   private static void init_V_Entity(Entity entity, AvatarRenderState state, ServerRotationTracker.Inner_DataUtil rotation) {
      if (state.isFallFlying && entity != null) {
         Vec3 look = Vec3.directionFromRotation(rotation.load(), rotation.init());
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

   public record Inner_CachePool(float init_F, float load, float read, float write, int init_I, boolean init_Z, Vec3 init_Vec3) {
      public Inner_CachePool(float previousYaw, float previousPitch, float currentYaw, float currentPitch, int tick, boolean initialized) {
         this(previousYaw, previousPitch, currentYaw, currentPitch, tick, initialized, null);
      }
   }

   record Inner_DataUtil(float init, float load) {
   }

   record Inner_StreamUtil(ServerRotationTracker.Inner_DataUtil init_ch_DataUtil, ServerRotationTracker.Inner_DataUtil load, int init_I, Vec3 init_Vec3) {
      Inner_StreamUtil(ServerRotationTracker.Inner_DataUtil previous, ServerRotationTracker.Inner_DataUtil current, int tick) {
         this(previous, current, tick, null);
      }

      static ServerRotationTracker.Inner_StreamUtil init_ch_StreamUtil() {
         return new ServerRotationTracker.Inner_StreamUtil(null, null, -2147483648);
      }
   }
}
