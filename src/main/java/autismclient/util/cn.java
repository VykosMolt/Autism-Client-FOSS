package autismclient.util;

import autismclient.modules.j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public final class cn {
   private static final bb init_bb = new bb();
   private static final bb load_bb = new bb();
   private static final bb read_bb = new bb();
   private static volatile float init_F = 0.0F / 0.0F;
   private static volatile int init_I = -2147483648;
   private static volatile float[] init_FArr = new float[0];
   private static volatile float load_F = 0.0F / 0.0F;
   private static volatile float read_F = 0.0F / 0.0F;

   private cn() {
   }

   public static boolean init_Z() {
      if (!AutismLiteVariant.enabled() && j.init_Z_49()) {
         return true;
      } else {
         return !AutismLiteVariant.enabled() && autismclient.modules.z.read_Z_22()
            ? false
            : autismclient.modules.ScaffoldModule.write_Z_5() || autismclient.modules.ScaffoldModule.flush_Z_5();
      }
   }

   public static ce.Inner_DataUtil init_ce_DataUtil() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null) {
         ce.Inner_DataUtil rotation = aD.init_ce_DataUtil();
         if (rotation == null) {
            return null;
         } else if (!AutismLiteVariant.enabled() && "ai-assistant".equals(aD.init_Str())) {
            return autismclient.ai.ConfigStore.init_ConfigStore().load_Z_23() ? rotation : null;
         } else {
            return autismclient.modules.al.init_Module_Str(aD.init_Str()) instanceof cn.Inner_DataUtil owner && owner.load_Z_23() ? rotation : null;
         }
      } else {
         return null;
      }
   }

   public static Input init(ClientInput source, Input input) {
      Minecraft mc = Minecraft.getInstance();
      if (input != null && mc != null && mc.player != null && mc.player.input == source) {
         ce.Inner_DataUtil rotation = init_ce_DataUtil();
         if (rotation == null) {
            return input;
         }

         if (!AutismLiteVariant.enabled() && autismclient.modules.z.read_Z_22() && autismclient.modules.ScaffoldModule.sync_Z_6()) {
            return input;
         }

         if (aD.load_Z() || init(rotation)) {
            init_bb.init();
            load_bb.init();
            return input;
         }

         if (!AutismLiteVariant.enabled() && "kb-displacement".equals(aD.init_Str())) {
            return init_bb.load(input, autismclient.modules.X.init_F_F_2(mc.player.getYRot()), rotation.init(), ck.init_ck().state_I(), cD.init(mc.player));
         }

         String aimOwner = aD.init_Str();
         if ("kill-aura".equals(aimOwner)) {
            if (!AutismLiteVariant.enabled()) {
               Input course = autismclient.modules.X.init_Input_Input(input, mc.player.getYRot(), rotation.init());
               if (course != null) {
                  return course;
               }
            }

            return init_bb.load(input, mc.player.getYRot(), rotation.init(), ck.init_ck().state_I(), cD.init(mc.player));
         } else if (!"crystal-aura".equals(aimOwner) && !"anchor-aura".equals(aimOwner)) {
            init_bb.init();
            if ("bed-nuker".equals(aD.init_Str())) {
               return load_bb.load(input, mc.player.getYRot(), rotation.init(), ck.init_ck().state_I(), cD.init(mc.player));
            }

            load_bb.init();
            if (load_Z()) {
               return read_bb.load(input, mc.player.getYRot(), rotation.init(), ck.init_ck().state_I());
            }

            read_bb.init();
            return autismclient.modules.ScaffoldModule.init_Input_Input_2(input, mc.player.getYRot(), rotation.init());
         } else {
            return init_bb.load(input, mc.player.getYRot(), rotation.init(), ck.init_ck().state_I(), cD.init(mc.player));
         }
      } else {
         return input;
      }
   }

   public static Input init(Input input, float referenceYaw, float yaw) {
      Minecraft mc = Minecraft.getInstance();
      return init_bb.init(input, referenceYaw, yaw, ck.init_ck().state_I(), cD.init(mc == null ? null : mc.player));
   }

   private static boolean load_Z() {
      return AutismLiteVariant.enabled() ? false : "clutch".equals(aD.init_Str()) && !j.sync_Z_10();
   }

   private static boolean init(ce.Inner_DataUtil rotation) {
      return rotation == null
            || !(
               autismclient.modules.al.init_Module_Str(aD.init_Str()) instanceof cn.Inner_DataUtil owner
                  && !owner.write_Z_10()
                  && !aD.init_Z()
                  && !autismclient.modules.aC.write()
            )
            || autismclient.modules.al.init_Module_Str("scaffold") instanceof autismclient.modules.ScaffoldModule scaffold && scaffold.isEnabled()
         ? false
         : AutismLiteVariant.enabled() || !j.sync_Z_10();
   }

   public static float init_F(LocalPlayer player) {
      ce.Inner_DataUtil rotation = init(player);
      return init(rotation) ? rotation.init() : player.getYRot();
   }

   public static float init_F(Entity entity, float vanillaYaw) {
      ce.Inner_DataUtil rotation = init(entity);
      return rotation == null ? vanillaYaw : rotation.init();
   }

   public static float init(LocalPlayer player, float vanillaYaw) {
      return init_F(player, vanillaYaw);
   }

   public static float load(LocalPlayer player, float vanillaPitch) {
      ce.Inner_DataUtil rotation = init(player);
      return rotation == null ? vanillaPitch : rotation.load();
   }

   public static Vec3 init(LocalPlayer player, Vec3 vanillaVector) {
      ce.Inner_DataUtil rotation = init(player);
      return rotation == null ? vanillaVector : Vec3.directionFromRotation(rotation.load(), rotation.init());
   }

   public static Vec3 init(LivingEntity entity, Vec3 vanillaImpulse) {
      ce.Inner_DataUtil rotation = init(entity);
      if (rotation == null) {
         return vanillaImpulse;
      }

      float yaw = rotation.init() * 0.017453292F;
      return new Vec3(-Mth.sin(yaw) * 0.2F, vanillaImpulse.y, Mth.cos(yaw) * 0.2F);
   }

   public static float init(LivingEntity entity, float vanillaPitch) {
      ce.Inner_DataUtil rotation = init(entity);
      return rotation == null ? vanillaPitch : rotation.load();
   }

   public static Vec3 load(LivingEntity entity, Vec3 vanillaLook) {
      ce.Inner_DataUtil rotation = init(entity);
      return rotation == null ? vanillaLook : Vec3.directionFromRotation(rotation.load(), rotation.init());
   }

   private static ce.Inner_DataUtil init(Entity entity) {
      Minecraft mc = Minecraft.getInstance();
      return entity != null && mc != null && entity == mc.player ? init_ce_DataUtil() : null;
   }

   public static void init_V(Entity entity, float yaw) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && entity == mc.player) {
         init_F = yaw;
         init_I = ck.init_ck().state_I();
      }
   }

   public static void init(float yaw) {
      load_F = yaw;
   }

   public static void load(float pitch) {
      read_F = pitch;
   }

   public static float init_F() {
      return load_F;
   }

   public static float load_F() {
      return read_F;
   }

   public static void init(Minecraft mc) {
      if (mc != null && mc.player != null) {
         ce.Inner_DataUtil live = init_ce_DataUtil();
         double wireYaw = ch.init_ch_CachePool().init_Z() ? ch.init_ch_CachePool().read() : 0.0 / 0.0;
         init_FArr = new float[]{init_F, (float)wireYaw, live == null ? 0.0F / 0.0F : live.init(), init_I, load_F};
      }
   }

   public static float[] init_FArr() {
      return (float[])init_FArr.clone();
   }

   public static ce.Inner_DataUtil init_ce_DataUtil(LocalPlayer player) {
      Minecraft mc = Minecraft.getInstance();
      if (player == null || mc == null || player != mc.player) {
         return null;
      }

      if (!AutismLiteVariant.enabled() && j.close_Z_8()) {
         return init_ce_DataUtil();
      }

      ce.Inner_DataUtil scaffold = autismclient.modules.ScaffoldModule.init_ce_DataUtil_3();
      return scaffold != null ? scaffold : init_ce_DataUtil();
   }

   public static ce.Inner_DataUtil load(LocalPlayer player) {
      return init_ce_DataUtil(player);
   }

   public interface Inner_DataUtil {
      boolean load_Z_23();

      default boolean write_Z_10() {
         return true;
      }

      default boolean update_Z_9() {
         return false;
      }
   }
}
