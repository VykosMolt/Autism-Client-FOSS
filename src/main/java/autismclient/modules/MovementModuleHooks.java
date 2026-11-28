package autismclient.modules;

import autismclient.util.AutismLiteVariant;
import autismclient.util.cf;
import autismclient.util.g.PkgCls_a;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class MovementModuleHooks {
   private static final Minecraft MC = Minecraft.getInstance();
   private static volatile MovementModuleHooks.ToggleSnapshot toggleSnapshot = MovementModuleHooks.ToggleSnapshot.of(-1);
   private static volatile MovementModuleHooks.ModuleRefSnapshot moduleRefs = MovementModuleHooks.ModuleRefSnapshot.of(-1);

   private MovementModuleHooks() {
   }

   public static boolean suppressesSlimeBounce(Entity entity) {
      return !AutismLiteVariant.enabled() && MC != null && entity == MC.player && autismclient.ai.AiDirectOps.isRunning()
         ? false
         : currentModuleRefs().init_Z() && MC != null && entity == MC.player;
   }

   public static float speedTimerMultiplier() {
      float multiplier = 1.0F;

      for (Module module : al.load_List()) {
         if (module.shouldApplySpeedTimer()) {
            multiplier = Math.max(multiplier, module.speedTimerMultiplier());
         }
      }

      return Math.max(0.01F, Math.min(10.0F, multiplier));
   }

   public static float flyingSpeed(Player player) {
      f.Inner_FileCache flight = currentModuleRefs().init_f_FileCache();
      return flight != null ? flight.init_F_7() : -1.0F;
   }

   public static boolean init_Z(Player player) {
      f.Inner_FileCache flight = currentModuleRefs().init_f_FileCache();
      return flight != null && flight.load_Z_21();
   }

   public static boolean init_Z() {
      return isOmniSprintActive();
   }

   public static boolean isOmniSprintActive() {
      return currentToggles().init_Z() && !ScaffoldModule.open_Z_3();
   }

   public static boolean ignoresSprintCollision() {
      return currentToggles().load() && !ScaffoldModule.open_Z_3();
   }

   public static boolean ignoresSprintBlindness() {
      return currentToggles().read() && !ScaffoldModule.open_Z_3();
   }

   public static boolean wantsSprint(boolean original, boolean movementTick) {
      if (!AutismLiteVariant.enabled() && WTappingModule.isTapping()) {
         return false;
      } else if (!AutismLiteVariant.enabled() && ClutchModule.ownsCameraKeys()) {
         return false;
      } else if (!AutismLiteVariant.enabled() && autismclient.ai.engine.AiMotionSafety.isSprintSuppressed()) {
         return false;
      } else if (ScaffoldModule.open_Z_3()) {
         return original;
      } else {
         return al.init_Module_Str("sprint") instanceof f.Inner_MapStore sprint && sprint.isEnabled() ? sprint.init(original, movementTick) : original;
      }
   }

   public static boolean forceStopSprint() {
      if (!AutismLiteVariant.enabled() && (WTappingModule.isTapping() || autismclient.ai.engine.AiMotionSafety.isSprintSuppressed())) {
         return true;
      } else {
         return ScaffoldModule.open_Z_3() ? false : al.init_Module_Str("sprint") instanceof f.Inner_MapStore sprint && sprint.isEnabled() && sprint.init();
      }
   }

   public static boolean announcedSprint(boolean original) {
      return AntiHungerModule.suppressesSprint() ? false : original;
   }

   public static boolean useSprintJumpYaw() {
      return al.init_Module_Str("sprint") instanceof f.Inner_MapStore sprint && sprint.isEnabled() && sprint.read();
   }

   public static float sprintJumpYaw(LocalPlayer player) {
      Input keys = player.input.keyPresses;
      float yaw = player.getYRot();
      float multiplier;
      if (keys.backward() && !keys.forward()) {
         yaw += 180.0F;
         multiplier = -0.5F;
      } else if (keys.forward() && !keys.backward()) {
         multiplier = 0.5F;
      } else {
         multiplier = 1.0F;
      }

      if (keys.left() && !keys.right()) {
         yaw -= 90.0F * multiplier;
      }

      if (keys.right() && !keys.left()) {
         yaw += 90.0F * multiplier;
      }

      return yaw;
   }

   public static Vec3 steerVelocity(LocalPlayer player, Vec3 velocity, double speed) {
      Input keys = player.input.keyPresses;
      if (keys.forward() == keys.backward() && keys.left() == keys.right()) {
         return new Vec3(0.0, velocity.y, 0.0);
      }

      double angle = Math.toRadians(sprintJumpYaw(player));
      return new Vec3(-Math.sin(angle) * speed, velocity.y, Math.cos(angle) * speed);
   }

   public static boolean isSprintEnabled() {
      return al.init_Module_Str("sprint") instanceof f.Inner_MapStore sprint && sprint.isEnabled();
   }

   private static MovementModuleHooks.ToggleSnapshot currentToggles() {
      int revision = al.init_I();
      MovementModuleHooks.ToggleSnapshot snapshot = toggleSnapshot;
      if (snapshot.revision() == revision) {
         return snapshot;
      }

      if (al.init_Module_Str("sprint") instanceof f.Inner_MapStore sprint && sprint.isEnabled()) {
         snapshot = new MovementModuleHooks.ToggleSnapshot(revision, sprint.load(), sprint.write(), sprint.update());
      } else {
         snapshot = MovementModuleHooks.ToggleSnapshot.of(revision);
      }

      toggleSnapshot = snapshot;
      return snapshot;
   }

   private static MovementModuleHooks.ModuleRefSnapshot currentModuleRefs() {
      int revision = al.init_I();
      MovementModuleHooks.ModuleRefSnapshot snapshot = moduleRefs;
      if (snapshot.revision() == revision) {
         return snapshot;
      }

      f.Inner_FileCache flight = al.init_Module_Str("flight") instanceof f.Inner_FileCache typed && typed.isEnabled() ? typed : null;
      f.Inner_ListPool speed = al.init_Module_Str("speed") instanceof f.Inner_ListPool typed && typed.isEnabled() ? typed : null;
      Module noFall = al.init_Module_Str("no-fall");
      boolean noFallAntiBounce = noFall != null && noFall.isEnabled() && Boolean.parseBoolean(noFall.value("anti-bounce"));
      snapshot = new MovementModuleHooks.ModuleRefSnapshot(revision, flight, speed, noFallAntiBounce);
      moduleRefs = snapshot;
      return snapshot;
   }

   public static void init_V() {
      if ((AutismLiteVariant.enabled() || !PkgCls_a.init_Z()) && cf.init_Z_J(4L)) {
         al.flush_V();
      }
   }

   public static Vec3 adjustMovement(Entity entity, MoverType type, Vec3 movement) {
      if (AutismLiteVariant.enabled()
         || !PkgCls_a.init_Z()
         || MC.player == null
         || entity != MC.player && entity != MC.player.getVehicle()) {
         if (AutoTotemModule.init_Z_6()) {
            return movement;
         }

         if (entity != MC.player) {
            if (MC.player != null && entity == MC.player.getVehicle() && !aD.init_Z()) {
               Vec3 adjusted = EntityControlModule.adjustMovement(entity, type, movement);
               adjusted = BoatFlyModule.adjustMovement(entity, type, adjusted);
               if (type == MoverType.SELF && adjusted != movement) {
                  entity.setDeltaMovement(adjusted);
               }

               return adjusted;
            } else {
               return movement;
            }
         } else {
            if (!cf.init_Z_J(8L)) {
               return movement;
            }

            Vec3 adjusted = al.init_Vec3_MoverType_Vec3(type, movement);
            if (type == MoverType.SELF && adjusted != movement) {
               MC.player.setDeltaMovement(adjusted);
            }

            return adjusted;
         }
      } else {
         return Vec3.ZERO;
      }
   }

   public static void afterTravelInFluid(Entity entity) {
      if (AutismLiteVariant.enabled()
         || !PkgCls_a.init_Z()
         || MC.player == null
         || entity != MC.player && entity != MC.player.getVehicle()) {
         if (entity == MC.player && !aD.init_Z()) {
            MovementModuleHooks.ModuleRefSnapshot state = currentModuleRefs();
            f.Inner_ListPool speed = state.init_f_ListPool();
            if (speed != null) {
               Vec3 movement = entity.getDeltaMovement();
               Vec3 adjusted = speed.init(movement);
               if (adjusted != null && adjusted != movement) {
                  entity.setDeltaMovement(adjusted);
               }
            }
         }
      }
   }

   private record ModuleRefSnapshot(int revision, f.Inner_FileCache init_f_FileCache, f.Inner_ListPool init_f_ListPool, boolean init_Z) {
      static MovementModuleHooks.ModuleRefSnapshot of(int revision) {
         return new MovementModuleHooks.ModuleRefSnapshot(revision, null, null, false);
      }
   }

   private record ToggleSnapshot(int revision, boolean init_Z, boolean load, boolean read) {
      static MovementModuleHooks.ToggleSnapshot of(int revision) {
         return new MovementModuleHooks.ToggleSnapshot(revision, false, false, false);
      }
   }
}
