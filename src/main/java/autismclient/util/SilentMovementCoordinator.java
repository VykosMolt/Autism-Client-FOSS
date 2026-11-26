package autismclient.util;

import autismclient.modules.ClutchModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public final class SilentMovementCoordinator {
   private static final MovementInputFixer combatMoveFixer = new MovementInputFixer();
   private static final MovementInputFixer bedNukerMoveFixer = new MovementInputFixer();
   private static final MovementInputFixer clutchMoveFixer = new MovementInputFixer();
   private static volatile float lastMoveYaw = 0.0F / 0.0F;
   private static volatile int lastMoveTick = -2147483648;
   private static volatile float[] debugTelemetry = new float[0];
   private static volatile float lastCameraYaw = 0.0F / 0.0F;
   private static volatile float lastCameraPitch = 0.0F / 0.0F;

   private SilentMovementCoordinator() {
   }

   public static boolean isSafeWalkActive() {
      if (!AutismLiteVariant.enabled() && ClutchModule.init_Z_49()) {
         return true;
      } else {
         return !AutismLiteVariant.enabled() && autismclient.modules.DangerModule.read_Z_22()
            ? false
            : autismclient.modules.ScaffoldModule.write_Z_5() || autismclient.modules.ScaffoldModule.flush_Z_5();
      }
   }

   public static RotationMath.Rotation activeRotation() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null) {
         RotationMath.Rotation rotation = SilentRotationManager.getCurrentRotation();
         if (rotation == null) {
            return null;
         } else if (!AutismLiteVariant.enabled() && "ai-assistant".equals(SilentRotationManager.getActiveOwnerId())) {
            return autismclient.ai.AiAssistant.init_ConfigStore().load_Z_23() ? rotation : null;
         } else {
            return autismclient.modules.al.init_Module_Str(SilentRotationManager.getActiveOwnerId()) instanceof SilentMovementCoordinator.Participant owner && owner.load_Z_23() ? rotation : null;
         }
      } else {
         return null;
      }
   }

   public static Input rewriteMovementInput(ClientInput source, Input input) {
      Minecraft mc = Minecraft.getInstance();
      if (input != null && mc != null && mc.player != null && mc.player.input == source) {
         RotationMath.Rotation rotation = activeRotation();
         if (rotation == null) {
            return input;
         }

         if (!AutismLiteVariant.enabled() && autismclient.modules.DangerModule.read_Z_22() && autismclient.modules.ScaffoldModule.sync_Z_6()) {
            return input;
         }

         if (SilentRotationManager.isVisibleAimActive() || shouldSkipMoveFix(rotation)) {
            combatMoveFixer.init();
            bedNukerMoveFixer.init();
            return input;
         }

         if (!AutismLiteVariant.enabled() && "kb-displacement".equals(SilentRotationManager.getActiveOwnerId())) {
            return combatMoveFixer.load(input, autismclient.modules.KBDisplacementModule.init_F_F_2(mc.player.getYRot()), rotation.yaw(), AutismSessionState.getInstance().getClientTick(), LedgeFootingPredictor.init(mc.player));
         }

         String aimOwner = SilentRotationManager.getActiveOwnerId();
         if ("kill-aura".equals(aimOwner)) {
            if (!AutismLiteVariant.enabled()) {
               Input course = autismclient.modules.KBDisplacementModule.init_Input_Input(input, mc.player.getYRot(), rotation.yaw());
               if (course != null) {
                  return course;
               }
            }

            return combatMoveFixer.load(input, mc.player.getYRot(), rotation.yaw(), AutismSessionState.getInstance().getClientTick(), LedgeFootingPredictor.init(mc.player));
         } else if (!"crystal-aura".equals(aimOwner) && !"anchor-aura".equals(aimOwner)) {
            combatMoveFixer.init();
            if ("bed-nuker".equals(SilentRotationManager.getActiveOwnerId())) {
               return bedNukerMoveFixer.load(input, mc.player.getYRot(), rotation.yaw(), AutismSessionState.getInstance().getClientTick(), LedgeFootingPredictor.init(mc.player));
            }

            bedNukerMoveFixer.init();
            if (isClutchMoveFixActive()) {
               return clutchMoveFixer.load(input, mc.player.getYRot(), rotation.yaw(), AutismSessionState.getInstance().getClientTick());
            }

            clutchMoveFixer.init();
            return autismclient.modules.ScaffoldModule.init_Input_Input_2(input, mc.player.getYRot(), rotation.yaw());
         } else {
            return combatMoveFixer.load(input, mc.player.getYRot(), rotation.yaw(), AutismSessionState.getInstance().getClientTick(), LedgeFootingPredictor.init(mc.player));
         }
      } else {
         return input;
      }
   }

   public static Input correctInputForYaw(Input input, float referenceYaw, float yaw) {
      Minecraft mc = Minecraft.getInstance();
      return combatMoveFixer.init(input, referenceYaw, yaw, AutismSessionState.getInstance().getClientTick(), LedgeFootingPredictor.init(mc == null ? null : mc.player));
   }

   private static boolean isClutchMoveFixActive() {
      return AutismLiteVariant.enabled() ? false : "clutch".equals(SilentRotationManager.getActiveOwnerId()) && !ClutchModule.sync_Z_10();
   }

   private static boolean shouldSkipMoveFix(RotationMath.Rotation rotation) {
      return rotation == null
            || !(
               autismclient.modules.al.init_Module_Str(SilentRotationManager.getActiveOwnerId()) instanceof SilentMovementCoordinator.Participant owner
                  && !owner.write_Z_10()
                  && !SilentRotationManager.isReturningToPlayer()
                  && !autismclient.modules.FreecamController.isAiControllingBody()
            )
            || autismclient.modules.al.init_Module_Str("scaffold") instanceof autismclient.modules.ScaffoldModule scaffold && scaffold.isEnabled()
         ? false
         : AutismLiteVariant.enabled() || !ClutchModule.sync_Z_10();
   }

   public static float getMovementYaw(LocalPlayer player) {
      RotationMath.Rotation rotation = activeRotationFor(player);
      return shouldSkipMoveFix(rotation) ? rotation.yaw() : player.getYRot();
   }

   public static float getEntityYaw(Entity entity, float vanillaYaw) {
      RotationMath.Rotation rotation = activeRotationFor(entity);
      return rotation == null ? vanillaYaw : rotation.yaw();
   }

   public static float getPlayerYaw(LocalPlayer player, float vanillaYaw) {
      return getEntityYaw(player, vanillaYaw);
   }

   public static float getPlayerPitch(LocalPlayer player, float vanillaPitch) {
      RotationMath.Rotation rotation = activeRotationFor(player);
      return rotation == null ? vanillaPitch : rotation.pitch();
   }

   public static Vec3 getPlayerLookVector(LocalPlayer player, Vec3 vanillaVector) {
      RotationMath.Rotation rotation = activeRotationFor(player);
      return rotation == null ? vanillaVector : Vec3.directionFromRotation(rotation.pitch(), rotation.yaw());
   }

   public static Vec3 getJumpImpulse(LivingEntity entity, Vec3 vanillaImpulse) {
      RotationMath.Rotation rotation = activeRotationFor(entity);
      if (rotation == null) {
         return vanillaImpulse;
      }

      float yaw = rotation.yaw() * 0.017453292F;
      return new Vec3(-Mth.sin(yaw) * 0.2F, vanillaImpulse.y, Mth.cos(yaw) * 0.2F);
   }

   public static float getEntityPitch(LivingEntity entity, float vanillaPitch) {
      RotationMath.Rotation rotation = activeRotationFor(entity);
      return rotation == null ? vanillaPitch : rotation.pitch();
   }

   public static Vec3 getEntityLookVector(LivingEntity entity, Vec3 vanillaLook) {
      RotationMath.Rotation rotation = activeRotationFor(entity);
      return rotation == null ? vanillaLook : Vec3.directionFromRotation(rotation.pitch(), rotation.yaw());
   }

   private static RotationMath.Rotation activeRotationFor(Entity entity) {
      Minecraft mc = Minecraft.getInstance();
      return entity != null && mc != null && entity == mc.player ? activeRotation() : null;
   }

   public static void recordMovementYaw(Entity entity, float yaw) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && entity == mc.player) {
         lastMoveYaw = yaw;
         lastMoveTick = AutismSessionState.getInstance().getClientTick();
      }
   }

   public static void setCameraYaw(float yaw) {
      lastCameraYaw = yaw;
   }

   public static void setCameraPitch(float pitch) {
      lastCameraPitch = pitch;
   }

   public static float getCameraYaw() {
      return lastCameraYaw;
   }

   public static float getCameraPitch() {
      return lastCameraPitch;
   }

   public static void captureDebugTelemetry(Minecraft mc) {
      if (mc != null && mc.player != null) {
         RotationMath.Rotation live = activeRotation();
         double wireYaw = ServerRotationTracker.getSnapshot().initialized() ? ServerRotationTracker.getSnapshot().currentYaw() : 0.0 / 0.0;
         debugTelemetry = new float[]{lastMoveYaw, (float)wireYaw, live == null ? 0.0F / 0.0F : live.yaw(), lastMoveTick, lastCameraYaw};
      }
   }

   public static float[] getDebugTelemetry() {
      return (float[])debugTelemetry.clone();
   }

   public static RotationMath.Rotation getRenderRotation(LocalPlayer player) {
      Minecraft mc = Minecraft.getInstance();
      if (player == null || mc == null || player != mc.player) {
         return null;
      }

      if (!AutismLiteVariant.enabled() && ClutchModule.close_Z_8()) {
         return activeRotation();
      }

      RotationMath.Rotation scaffold = autismclient.modules.ScaffoldModule.init_ce_DataUtil_3();
      return scaffold != null ? scaffold : activeRotation();
   }

   public static RotationMath.Rotation getHeadRenderRotation(LocalPlayer player) {
      return getRenderRotation(player);
   }

   public interface Participant {
      boolean load_Z_23();

      default boolean write_Z_10() {
         return true;
      }

      default boolean update_Z_9() {
         return false;
      }
   }
}
