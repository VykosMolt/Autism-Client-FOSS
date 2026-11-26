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

public final class ai {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static volatile ai.Inner_StreamUtil init_ai_StreamUtil = ai.Inner_StreamUtil.init(-1);
   private static volatile ai.Inner_DataUtil init_ai_DataUtil = ai.Inner_DataUtil.init(-1);

   private ai() {
   }

   public static boolean init_Z(Entity entity) {
      return !AutismLiteVariant.enabled() && init_Minecraft != null && entity == init_Minecraft.player && autismclient.ai.o.init_Z()
         ? false
         : init_ai_DataUtil().init_Z() && init_Minecraft != null && entity == init_Minecraft.player;
   }

   public static float init_F() {
      float multiplier = 1.0F;

      for (Module module : al.load_List()) {
         if (module.shouldApplySpeedTimer()) {
            multiplier = Math.max(multiplier, module.speedTimerMultiplier());
         }
      }

      return Math.max(0.01F, Math.min(10.0F, multiplier));
   }

   public static float init_F(Player player) {
      f.Inner_FileCache flight = init_ai_DataUtil().init_f_FileCache();
      return flight != null ? flight.init_F_7() : -1.0F;
   }

   public static boolean init_Z(Player player) {
      f.Inner_FileCache flight = init_ai_DataUtil().init_f_FileCache();
      return flight != null && flight.load_Z_21();
   }

   public static boolean init_Z() {
      return load();
   }

   public static boolean load() {
      return init_ai_StreamUtil().init_Z() && !ScaffoldModule.open_Z_3();
   }

   public static boolean read() {
      return init_ai_StreamUtil().load() && !ScaffoldModule.open_Z_3();
   }

   public static boolean write() {
      return init_ai_StreamUtil().read() && !ScaffoldModule.open_Z_3();
   }

   public static boolean init(boolean original, boolean movementTick) {
      if (!AutismLiteVariant.enabled() && WTappingModule.init_Z_36()) {
         return false;
      } else if (!AutismLiteVariant.enabled() && ClutchModule.flush_Z_9()) {
         return false;
      } else if (!AutismLiteVariant.enabled() && autismclient.ai.a.AiMotionSafety.putAt_Z()) {
         return false;
      } else if (ScaffoldModule.open_Z_3()) {
         return original;
      } else {
         return al.init_Module_Str("sprint") instanceof f.Inner_MapStore sprint && sprint.isEnabled() ? sprint.init(original, movementTick) : original;
      }
   }

   public static boolean update() {
      if (!AutismLiteVariant.enabled() && (WTappingModule.init_Z_36() || autismclient.ai.a.AiMotionSafety.putAt_Z())) {
         return true;
      } else {
         return ScaffoldModule.open_Z_3() ? false : al.init_Module_Str("sprint") instanceof f.Inner_MapStore sprint && sprint.isEnabled() && sprint.init();
      }
   }

   public static boolean init(boolean original) {
      return AntiHungerModule.load() ? false : original;
   }

   public static boolean sync() {
      return al.init_Module_Str("sprint") instanceof f.Inner_MapStore sprint && sprint.isEnabled() && sprint.read();
   }

   public static float init(LocalPlayer player) {
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

   public static Vec3 init(LocalPlayer player, Vec3 velocity, double speed) {
      Input keys = player.input.keyPresses;
      if (keys.forward() == keys.backward() && keys.left() == keys.right()) {
         return new Vec3(0.0, velocity.y, 0.0);
      }

      double angle = Math.toRadians(init(player));
      return new Vec3(-Math.sin(angle) * speed, velocity.y, Math.cos(angle) * speed);
   }

   public static boolean flush() {
      return al.init_Module_Str("sprint") instanceof f.Inner_MapStore sprint && sprint.isEnabled();
   }

   private static ai.Inner_StreamUtil init_ai_StreamUtil() {
      int revision = al.init_I();
      ai.Inner_StreamUtil snapshot = init_ai_StreamUtil;
      if (snapshot.init_I() == revision) {
         return snapshot;
      }

      if (al.init_Module_Str("sprint") instanceof f.Inner_MapStore sprint && sprint.isEnabled()) {
         snapshot = new ai.Inner_StreamUtil(revision, sprint.load(), sprint.write(), sprint.update());
      } else {
         snapshot = ai.Inner_StreamUtil.init(revision);
      }

      init_ai_StreamUtil = snapshot;
      return snapshot;
   }

   private static ai.Inner_DataUtil init_ai_DataUtil() {
      int revision = al.init_I();
      ai.Inner_DataUtil snapshot = init_ai_DataUtil;
      if (snapshot.init_I() == revision) {
         return snapshot;
      }

      f.Inner_FileCache flight = al.init_Module_Str("flight") instanceof f.Inner_FileCache typed && typed.isEnabled() ? typed : null;
      f.Inner_ListPool speed = al.init_Module_Str("speed") instanceof f.Inner_ListPool typed && typed.isEnabled() ? typed : null;
      Module noFall = al.init_Module_Str("no-fall");
      boolean noFallAntiBounce = noFall != null && noFall.isEnabled() && Boolean.parseBoolean(noFall.value("anti-bounce"));
      snapshot = new ai.Inner_DataUtil(revision, flight, speed, noFallAntiBounce);
      init_ai_DataUtil = snapshot;
      return snapshot;
   }

   public static void init_V() {
      if ((AutismLiteVariant.enabled() || !PkgCls_a.init_Z()) && cf.init_Z_J(4L)) {
         al.flush_V();
      }
   }

   public static Vec3 init(Entity entity, MoverType type, Vec3 movement) {
      if (AutismLiteVariant.enabled()
         || !PkgCls_a.init_Z()
         || init_Minecraft.player == null
         || entity != init_Minecraft.player && entity != init_Minecraft.player.getVehicle()) {
         if (AutoTotemModule.init_Z_6()) {
            return movement;
         }

         if (entity != init_Minecraft.player) {
            if (init_Minecraft.player != null && entity == init_Minecraft.player.getVehicle() && !aD.init_Z()) {
               Vec3 adjusted = EntityControlModule.init(entity, type, movement);
               adjusted = BoatFlyModule.init(entity, type, adjusted);
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
               init_Minecraft.player.setDeltaMovement(adjusted);
            }

            return adjusted;
         }
      } else {
         return Vec3.ZERO;
      }
   }

   public static void init_V(Entity entity) {
      if (AutismLiteVariant.enabled()
         || !PkgCls_a.init_Z()
         || init_Minecraft.player == null
         || entity != init_Minecraft.player && entity != init_Minecraft.player.getVehicle()) {
         if (entity == init_Minecraft.player && !aD.init_Z()) {
            ai.Inner_DataUtil state = init_ai_DataUtil();
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

   private record Inner_DataUtil(int init_I, f.Inner_FileCache init_f_FileCache, f.Inner_ListPool init_f_ListPool, boolean init_Z) {
      static ai.Inner_DataUtil init(int revision) {
         return new ai.Inner_DataUtil(revision, null, null, false);
      }
   }

   private record Inner_StreamUtil(int init_I, boolean init_Z, boolean load, boolean read) {
      static ai.Inner_StreamUtil init(int revision) {
         return new ai.Inner_StreamUtil(revision, false, false, false);
      }
   }
}
