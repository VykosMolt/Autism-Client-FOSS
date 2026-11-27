package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.KeybindSetting;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismLiteVariant;
import autismclient.util.g.PkgCls_a;
import java.util.Locale;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class TpClickModule extends Module {
   private final autismclient.ai.engine.AiStandingTarget.Inner_DataUtil init_j_DataUtil = new autismclient.ai.engine.AiStandingTarget.Inner_DataUtil();
   private autismclient.ai.engine.AiStandingTarget.Inner_StreamUtil init_j_StreamUtil;
   private boolean init_Z;

   TpClickModule() {
      super("tp-click", "TpClick", ae.read, "Teleport where you click.");
      this.add(new IntSetting("reach", "Reach", 50, 10, 250, 5).description("Maximum click teleport distance.").build());
      this.add(new KeybindSetting("click-key", "Click Key", autismclient.util.ValueSink.init_I(1)).description("Button that teleports you.").build());
      this.add(new BoolSetting("block-movement", "Block Movement", true).description("Freeze movement keys while teleporting.").build());
      this.add(new BoolSetting("no-fall", "No Fall", true).description("Prevent fall damage while teleporting.").build());
      this.add(new BoolSetting("anti-kick", "Anti Kick", true).description("Prevent floating kicks while airborne.").build());
   }

   @Override
   public void onDisable() {
      this.resetState();
   }

   @Override
   public void onGameLeft() {
      this.resetState();
   }

   private void resetState() {
      this.init_j_DataUtil.init();
      this.init_j_StreamUtil = null;
      this.init_Z = false;
   }

   public static boolean init_Z_30() {
      return al.init_Module_Str("tp-click") instanceof TpClickModule tpClick && tpClick.isEnabled() && tpClick.bool("block-movement");
   }

   public static boolean hasNoFall() {
      return al.init_Module_Str("tp-click") instanceof TpClickModule tpClick && tpClick.bool("no-fall");
   }

   public static boolean read() {
      return al.init_Module_Str("tp-click") instanceof TpClickModule tpClick && tpClick.bool("anti-kick");
   }

   @Override
   public void tick() {
      if (!this.isEnabled()) {
         this.resetState();
      } else if (MC != null && MC.player != null && MC.level != null && !aD.read_Z()) {
         int bind = this.clickKey();
         boolean down = bind != -1 && autismclient.util.ValueSink.init(MC, bind);
         boolean pressed = down && !this.init_Z;
         this.init_Z = down;
         if (!AutismLiteVariant.enabled() && autismclient.ai.aP.load_Z()) {
            this.init_j_StreamUtil = null;
         } else if (MC.gui.screen() == null && !MC.options.keyShift.isDown()) {
            Vec3 eye = FreecamController.isActive() ? FreecamController.getInterpolatedEyePos(1.0F) : MC.player.getEyePosition();
            Vec3 direction = FreecamController.isActive() ? Vec3.directionFromRotation(FreecamController.getInterpolatedPitch(1.0F), FreecamController.getInterpolatedYaw(1.0F)) : MC.player.getViewVector(1.0F);
            this.init_j_StreamUtil = this.init_j_DataUtil
               .init(MC, eye, direction, Math.max(10, this.integer("reach")), MC.player.getVehicle() != null, pressed);
            if (pressed && this.init_j_StreamUtil != null && this.init_j_StreamUtil.valid() && !AutismLiteVariant.enabled()) {
               Vec3 destination = this.init_j_StreamUtil.feet();
               String result = PkgCls_a.init_Str_Str(String.format(Locale.ROOT, "%.6f %.6f %.6f", destination.x, destination.y, destination.z));
               if (result != null && !result.startsWith("TP started")) {
                  AutismClientMessaging.sendPrefixed("§e" + result);
               }
            }
         } else {
            this.init_j_StreamUtil = null;
         }
      } else {
         this.resetState();
      }
   }

   private int clickKey() {
      try {
         return Integer.parseInt(this.value("click-key"));
      } catch (NumberFormatException ignored) {
         return autismclient.util.ValueSink.init_I(1);
      }
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      return MC != null && MC.options != null && MC.options.keyShift.isDown()
         ? false
         : this.clickKey() == autismclient.util.ValueSink.init_I(1) && this.init_j_StreamUtil != null && this.init_j_StreamUtil.valid();
   }

   public AABB targetBox() {
      return this.init_j_StreamUtil == null ? null : this.init_j_StreamUtil.box();
   }

   public int targetColor() {
      return this.init_j_StreamUtil == null ? 0 : this.init_j_StreamUtil.color();
   }
}
