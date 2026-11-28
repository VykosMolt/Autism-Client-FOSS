package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;

public final class FreeLookModule extends Module {
   private float cameraYaw;
   private float cameraPitch;
   private CameraType previousCameraType;

   public FreeLookModule() {
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
         this.cameraYaw = MC.player.getYRot();
         this.cameraPitch = MC.player.getXRot();
      }

      if (MC.options != null) {
         this.previousCameraType = MC.options.getCameraType();
         MC.options.setCameraType(this.freeLookCameraType());
      }
   }

   @Override
   public void onDisable() {
      if (MC.options != null && this.previousCameraType != null) {
         MC.options.setCameraType(this.previousCameraType);
      }

      this.previousCameraType = null;
   }

   @Override
   public void onGameLeft() {
      if (this.isEnabled()) {
         this.setEnabled(false);
      }
   }

   @Override
   public void preMovementTick() {
      if (MC.options != null && MC.options.getCameraType() != this.freeLookCameraType()) {
         MC.options.setCameraType(this.freeLookCameraType());
      }
   }

   private CameraType freeLookCameraType() {
      return this.isFrontView() ? CameraType.THIRD_PERSON_FRONT : CameraType.THIRD_PERSON_BACK;
   }

   private boolean isFrontView() {
      return "Front".equals(this.choice("perspective"));
   }

   private static FreeLookModule activeInstance() {
      return al.init_Module_Str("free-look") instanceof FreeLookModule freeLook && freeLook.isEnabled() ? freeLook : null;
   }

   public static FreeLookModule active() {
      return MC != null && MC.player != null ? activeInstance() : null;
   }

   public boolean init_Z_8() {
      return this.isFrontView();
   }

   public float cameraYaw() {
      return this.cameraYaw;
   }

   public float cameraPitch() {
      return this.cameraPitch;
   }

   public static boolean turn(double deltaX, double deltaY) {
      FreeLookModule freeLook = activeInstance();
      if (freeLook != null && MC.player != null) {
         freeLook.cameraYaw += (float)(deltaX * 0.15);
         freeLook.cameraPitch += (float)(deltaY * 0.15);
         if (!freeLook.bool("no-pitch-limit")) {
            freeLook.cameraPitch = Mth.clamp(freeLook.cameraPitch, -90.0F, 90.0F);
         }

         return true;
      } else {
         return false;
      }
   }
}
