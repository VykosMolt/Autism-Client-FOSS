package autismclient.mixin;

import autismclient.ai.aP;
import autismclient.ai.cc;
import autismclient.compat263.Compat263;
import autismclient.compat263.CompatGLFW;
import autismclient.modules.AutoTotemModule;
import autismclient.modules.FreeLookModule;
import autismclient.modules.AutoArmorModule;
import autismclient.modules.aC;
import autismclient.modules.al;
import autismclient.util.AutismLiteVariant;
import autismclient.util.G;
import autismclient.util.aS;
import autismclient.util.aZ;
import autismclient.util.ba;
import autismclient.util.cf;
import autismclient.util.ck;
import autismclient.util.cx;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class AutismMouseHandlerMixin {
   @Shadow
   @Final
   private Minecraft minecraft;
   @Shadow
   private double accumulatedDX;
   @Shadow
   private double accumulatedDY;
   @Unique
   private float autism$turnStartYaw;
   @Unique
   private float autism$turnStartPitch;
   @Unique
   private boolean autism$turnHadPlayer;
   @Unique
   private double autism$simulatedDX;
   @Unique
   private double autism$simulatedDY;

   @WrapMethod(method = "onButton")
   private void autism$userClick(long window, MouseButtonInfo button, int action, Operation<Void> original) {
      cx.init_V();

      try {
         if (AutismLiteVariant.enabled()
            || button == null
            || window != Minecraft.getInstance().getWindow().handle()
            || !aP.init_Z_I_I(Compat263.mouseInfoGlfwButton(button), action)) {
            original.call(window, button, action);
            return;
         }
      } finally {
         cx.load_V();
      }
   }

   @WrapMethod(method = "onScroll")
   private void autism$userScroll(long window, double x, double y, Operation<Void> original) {
      cx.init_V();

      try {
         if (AutismLiteVariant.enabled() || window != Minecraft.getInstance().getWindow().handle() || !aP.init_Z_D(y)) {
            original.call(window, x, y);
            return;
         }
      } finally {
         cx.load_V();
      }
   }

   @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
   private void autism$keepEditorCursor(CallbackInfo ci) {
      if (cx.load_Z()) {
         ci.cancel();
      }
   }

   @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
   private void autism$freeLookTurn(LocalPlayer player, double x, double y, Operation<Void> original) {
      double assistX = this.autism$simulatedDX;
      double assistY = this.autism$simulatedDY;
      if (assistX != 0.0 || assistY != 0.0) {
         this.autism$simulatedDX = 0.0;
         this.autism$simulatedDY = 0.0;
         if (FreeLookModule.init(x, y)) {
            original.call(player, assistX, assistY);
         } else {
            original.call(player, x + assistX, y + assistY);
         }
      } else if (!FreeLookModule.init(x, y)) {
         original.call(player, x, y);
      }
   }

   @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"))
   private void autism$clearQueuedMouseInputWhenUnavailable(CallbackInfo ci) {
      aZ.init(this.accumulatedDX, this.accumulatedDY, ck.init_ck().state_I());
      if (!AutismLiteVariant.enabled() && this.minecraft.gui.screen() == null && (Math.abs(this.accumulatedDX) > 0.5 || Math.abs(this.accumulatedDY) > 0.5)) {
         cc ai = cc.load_cc();
         if (ai != null) {
            ai.flush_V("Manual camera movement; AI controls released");
         }
      }

      ba.init_V();
   }

   @Inject(method = "onButton", at = @At("HEAD"))
   private void autism$trackCps(long window, MouseButtonInfo button, int action, CallbackInfo ci) {
      if (action == 1 && button != null) {
         if (!AutismLiteVariant.enabled()) {
            Screen screen = this.minecraft == null ? null : this.minecraft.gui.screen();
            boolean gameplay = screen == null ? ((MouseHandler)(Object)this).isMouseGrabbed() : screen instanceof AbstractContainerScreen;
            if (gameplay && CompatGLFW.glfwGetMouseButton(window, Compat263.mouseInfoGlfwButton(button)) == 1) {
               cc ai = cc.load_cc();
               if (ai != null) {
                  ai.flush_V("Manual interaction; AI operation paused");
               }
            }
         }

         if (this.minecraft != null && this.minecraft.gui.screen() == null) {
            int b = Compat263.mouseInfoGlfwButton(button);
            if (b == 0) {
               G.init_V();
            } else if (b == 1) {
               G.load_V();
            }
         }
      }
   }

   @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
   private void autism$menuMacroStop(long window, MouseButtonInfo button, int action, CallbackInfo ci) {
      if (action == 1
         && button != null
         && Compat263.mouseInfoGlfwButton(button) == 0
         && this.minecraft != null
         && this.minecraft.gui.screen() != null
         && this.minecraft.gui.overlay() == null) {
         Window gameWindow = this.minecraft.getWindow();
         if (window == gameWindow.handle()) {
            MouseHandler self = (MouseHandler)(Object)this;
            if (aS.init_Z_D_D(self.getScaledXPos(gameWindow), self.getScaledYPos(gameWindow))) {
               ci.cancel();
            }
         }
      }
   }

   @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
   private void autism$freecamScrollSpeed(long window, double xOffset, double yOffset, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled() && this.minecraft != null && this.minecraft.gui.screen() == null && yOffset != 0.0) {
         cc ai = cc.load_cc();
         if (ai != null) {
            ai.flush_V("Manual hotbar selection; AI operation paused");
         }
      }

      if (AutoTotemModule.init_Z_6() || AutoArmorModule.init_Z_20()) {
         ci.cancel();
      } else if (aC.init_Z_D(yOffset)) {
         ci.cancel();
      } else {
         if (!AutismLiteVariant.enabled()) {
            cc ai = cc.load_cc();
            if (this.minecraft != null && this.minecraft.gui.screen() == null && ai != null && ai.close_Z()) {
               ci.cancel();
               return;
            }
         }
      }
   }

   @Inject(method = "handleAccumulatedMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;turnPlayer(D)V"))
   private void autism$applyQueuedRawMouseInput(CallbackInfo ci) {
      ba.Inner_DataUtil delta = ba.init_ba_DataUtil();
      if (ba.load_Z()) {
         this.accumulatedDX = delta.init_D();
         this.accumulatedDY = delta.load();
      } else if (!delta.init_Z()) {
         if (this.minecraft.player != null && FreeLookModule.init_I_4() != null) {
            this.autism$simulatedDX = this.autism$simulatedDX + delta.init_D();
            this.autism$simulatedDY = this.autism$simulatedDY + delta.load();
         } else {
            this.accumulatedDX = this.accumulatedDX + autism$additiveAssist(this.accumulatedDX, delta.init_D());
            this.accumulatedDY = this.accumulatedDY + autism$additiveAssist(this.accumulatedDY, delta.load());
         }
      }
   }

   @ModifyExpressionValue(method = "turnPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;smoothCamera:Z"))
   private boolean autism$avoidDoubleSmoothingTelly(boolean original) {
      return original && !ba.init_Z(ba.Inner_StreamUtil.SCAFFOLD_TELLY) && !ba.init_Z();
   }

   @Unique
   private static double autism$additiveAssist(double realInput, double assistInput) {
      if (Math.abs(assistInput) < 1.0E-7) {
         return 0.0;
      } else if (Math.abs(realInput) < 1.0E-4) {
         return assistInput;
      } else {
         return Math.signum(realInput) == Math.signum(assistInput) ? assistInput : 0.0;
      }
   }

   @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
   private void autism$swallowTurnDuringTotemOperation(double deltaTime, CallbackInfo ci) {
      if (AutoTotemModule.init_Z_6() || AutoArmorModule.load_Z_9()) {
         this.accumulatedDX = 0.0;
         this.accumulatedDY = 0.0;
         this.autism$simulatedDX = 0.0;
         this.autism$simulatedDY = 0.0;
         ba.load_V();
         ci.cancel();
      }
   }

   @Inject(method = "turnPlayer", at = @At("HEAD"))
   private void autism$beforeTurnPlayer(double deltaTime, CallbackInfo ci) {
      if (!cf.init_Z_J(16L)) {
         this.autism$turnHadPlayer = false;
      } else {
         this.autism$turnHadPlayer = this.minecraft != null && this.minecraft.player != null;
         if (this.autism$turnHadPlayer) {
            this.autism$turnStartYaw = this.minecraft.player.getYRot();
            this.autism$turnStartPitch = this.minecraft.player.getXRot();
         }
      }
   }

   @Inject(method = "turnPlayer", at = @At("TAIL"))
   private void autism$afterTurnPlayer(double deltaTime, CallbackInfo ci) {
      if (this.autism$turnHadPlayer && this.minecraft != null && this.minecraft.player != null) {
         double deltaYaw = this.minecraft.player.getYRot() - this.autism$turnStartYaw;
         double deltaPitch = this.minecraft.player.getXRot() - this.autism$turnStartPitch;
         if (Math.abs(deltaYaw) > 1.0E-6 || Math.abs(deltaPitch) > 1.0E-6) {
            al.init_V_D_D(deltaYaw, deltaPitch);
         }
      }
   }
}
