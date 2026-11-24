package autismclient.mixin;

import autismclient.ai.a.AiCameraTurn;
import autismclient.modules.EventBus;
import autismclient.modules.I;
import autismclient.modules.aC;
import autismclient.modules.z;
import autismclient.util.AuditClientFixes;
import autismclient.util.AutismLiteVariant;
import autismclient.util.ce;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class AutismCameraMixin {
   @Shadow
   private boolean detached;
   @Shadow
   private Entity entity;

   @ModifyArgs(method = "setupPerspective", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Projection;setupPerspective(FFFFF)V"))
   private void autism$aspectRatio(Args args) {
      if (!AutismLiteVariant.enabled()) {
         args.set(3, EventBus.init(args.<Float>get(3), args.<Float>get(4)));
      }
   }

   @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 0))
   private void autism$dangerVisibleRotation(Args args, float partialTick) {
      if (!AutismLiteVariant.enabled()) {
         Minecraft mc = Minecraft.getInstance();
         if (this.entity != mc.player) {
            AiCameraTurn.putAt(null);
            return;
         }

         ce.Inner_DataUtil rotation = z.init_ce_DataUtil_F_2(partialTick);
         if (rotation == null
            && mc.player != null
            && mc.options.getCameraType().isFirstPerson()
            && mc.gui.screen() == null
            && !mc.player.isPassenger()
            && !aC.init_Z()
            && I.init_I_4() == null) {
            rotation = AiCameraTurn.putAt(mc.player);
         } else {
            AiCameraTurn.putAt(null);
         }

         if (rotation != null) {
            args.set(0, rotation.init());
            args.set(1, rotation.load());
         }
      }
   }

   @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 1))
   private void autism$dangerVisibleWalkingRotation(Args args, float partialTick) {
      this.autism$dangerVisibleRotation(args, partialTick);
   }

   @Shadow
   protected abstract void setPosition(Vec3 var1);

   @Shadow
   protected abstract void setRotation(float var1, float var2);

   @Shadow
   protected abstract void move(float var1, float var2, float var3);

   @Shadow
   protected abstract float getMaxZoom(float var1);

   @Inject(
      method = "update(Lnet/minecraft/client/DeltaTracker;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;alignWithEntity(F)V", shift = Shift.AFTER)
   )
   private void autism$freecamCamera(DeltaTracker deltaTracker, CallbackInfo ci) {
      if (aC.init_Z()) {
         float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
         this.detached = true;
         this.setPosition(new Vec3(aC.init_D_F(partialTicks), aC.load_D_F(partialTicks), aC.read(partialTicks)));
         this.setRotation(aC.init_F_F(partialTicks), aC.load_F_F(partialTicks));
      }
   }

   @Inject(
      method = "update(Lnet/minecraft/client/DeltaTracker;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;alignWithEntity(F)V", shift = Shift.AFTER)
   )
   private void autism$freeLookCamera(DeltaTracker deltaTracker, CallbackInfo ci) {
      I freeLook = I.init_I_4();
      if (freeLook != null && !aC.init_Z()) {
         float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
         this.detached = true;
         this.setPosition(
            new Vec3(
               Mth.lerp(partialTicks, this.entity.xo, this.entity.getX()),
               Mth.lerp(partialTicks, this.entity.yo, this.entity.getY()) + this.entity.getEyeHeight(),
               Mth.lerp(partialTicks, this.entity.zo, this.entity.getZ())
            )
         );
         if (freeLook.init_Z_8()) {
            this.setRotation(freeLook.init_F_3() + 180.0F, -freeLook.load_F());
         } else {
            this.setRotation(freeLook.init_F_3(), freeLook.load_F());
         }

         float scale = this.entity instanceof LivingEntity living ? living.getScale() : 1.0F;
         this.move(-this.getMaxZoom(4.0F * scale), 0.0F, 0.0F);
      }
   }

   @Inject(method = "extractRenderState", at = @At("TAIL"))
   private void autism$disableSmartCull(CameraRenderState cameraState, DeltaTracker deltaTracker, CallbackInfo ci) {
      if (AuditClientFixes.shouldDisableOcclusion() || aC.init_Z()) {
         cameraState.smartCull = false;
      }
   }

   @ModifyExpressionValue(
      method = "tickFov",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getFieldOfViewModifier(ZF)F"),
      require = 0
   )
   private float autism$freecamNoBodyFov(float modifier) {
      return aC.init_Z() ? 1.0F : modifier;
   }
}
