package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;

public final class I extends Module {
   private float init_F;
   private float load;
   private CameraType init_CameraType;

   public I() {
      super("free-look", "FreeLook", ae.update, "Look around without rotating the player.");
      this.add(new ChoiceSetting("perspective", "Perspective", "Back", "Back", "Front").build());
      this.add(new BoolSetting("no-pitch-limit", "No Pitch Limit", true).build());
   }

   @Override
   public boolean holdToActivate() {
      return true;
   }

   @Override
   public void onEnable() {
      if (MC.player != null) {
         this.init_F = MC.player.getYRot();
         this.load = MC.player.getXRot();
      }

      if (MC.options != null) {
         this.init_CameraType = MC.options.getCameraType();
         MC.options.setCameraType(this.init_CameraType());
      }
   }

   @Override
   public void onDisable() {
      if (MC.options != null && this.init_CameraType != null) {
         MC.options.setCameraType(this.init_CameraType);
      }

      this.init_CameraType = null;
   }

   @Override
   public void onGameLeft() {
      if (this.isEnabled()) {
         this.setEnabled(false);
      }
   }

   @Override
   public void preMovementTick() {
      if (MC.options != null && MC.options.getCameraType() != this.init_CameraType()) {
         MC.options.setCameraType(this.init_CameraType());
      }
   }

   private CameraType init_CameraType() {
      return this.load_Z_4() ? CameraType.THIRD_PERSON_FRONT : CameraType.THIRD_PERSON_BACK;
   }

   private boolean load_Z_4() {
      return "Front".equals(this.choice("perspective"));
   }

   private static I load_I_3() {
      return al.init_Module_Str("free-look") instanceof I freeLook && freeLook.isEnabled() ? freeLook : null;
   }

   public static I init_I_4() {
      return MC != null && MC.player != null ? load_I_3() : null;
   }

   public boolean init_Z_8() {
      return this.load_Z_4();
   }

   public float init_F_3() {
      return this.init_F;
   }

   public float load_F() {
      return this.load;
   }

   public static boolean init(double deltaX, double deltaY) {
      I freeLook = load_I_3();
      if (freeLook != null && MC.player != null) {
         freeLook.init_F += (float)(deltaX * 0.15);
         freeLook.load += (float)(deltaY * 0.15);
         if (!freeLook.bool("no-pitch-limit")) {
            freeLook.load = Mth.clamp(freeLook.load, -90.0F, 90.0F);
         }

         return true;
      } else {
         return false;
      }
   }
}
