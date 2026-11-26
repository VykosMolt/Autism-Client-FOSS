package autismclient.mixin;

import autismclient.AutismClientAddon;
import autismclient.e.PoolEntry;
import autismclient.gui.d.JsonUtil;
import autismclient.gui.screens.StealthPauseScreen;
import autismclient.gui.screens.StealthTitleScreen;
import autismclient.gui.screens.AutismTitleScreen;
import autismclient.gui.ui.widgets.TextInputWidget;
import autismclient.modules.aD;
import autismclient.util.AutismLiteVariant;
import autismclient.util.CustomMainMenuGuard;
import autismclient.util.bi;
import autismclient.util.cA;
import autismclient.util.OverlayScreenController;
import autismclient.util.macro.MacroExecutor;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.class)
public class AutismGuiSetScreenMixin {
   @Shadow
   @Final
   private Minecraft minecraft;
   @Shadow
   private Screen screen;
   @Unique
   private boolean autism$replacingScreen;
   @Unique
   private boolean autism$toastFailureLogged;

   @Inject(method = "screen", at = @At("RETURN"), cancellable = true)
   private void autism$foregroundScreen(CallbackInfoReturnable<Screen> cir) {
      cir.setReturnValue(OverlayScreenController.resolveEffectiveScreen(cir.getReturnValue()));
   }

   @ModifyExpressionValue(
      method = {"extractRenderState", "update", "setPauseScreen", "handleKeybinds", "canInterruptScreen"},
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Gui;screen:Lnet/minecraft/client/gui/screens/Screen;")
   )
   private Screen autism$renderForeground(Screen actual) {
      return OverlayScreenController.resolveEffectiveScreen(actual);
   }

   @WrapOperation(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;removed()V"))
   private void autism$keepEditorAlive(Screen previous, Operation<Void> original) {
      if (!OverlayScreenController.shouldPreserveOnRemove(previous)) {
         original.call(previous);
      }
   }

   @WrapMethod(method = "tick")
   private void autism$tickConnectionAndEditor(Operation<Void> original) {
      OverlayScreenController.beginRawScreenRead();

      try {
         original.call();
      } finally {
         OverlayScreenController.endRawScreenRead();
      }

      OverlayScreenController.tickRetainedForeground(this.screen);
   }

   @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
   private void autism$replaceScreen(Screen screen, CallbackInfo ci) {
      if (aD.init_Z()) {
         OverlayScreenController.clearRetainedForeground();
      }

      if (!AutismLiteVariant.enabled() && screen instanceof JsonUtil) {
         OverlayScreenController.clearRetainedForeground();
         TextInputWidget.init_V();
      } else if (!this.autism$replacingScreen && OverlayScreenController.interceptSetScreen(this.minecraft, this.screen, screen)) {
         ci.cancel();
      } else {
         if (!OverlayScreenController.shouldInterceptBackgroundScreen()) {
            TextInputWidget.init_V();
         }

         if (!this.autism$replacingScreen) {
            MacroExecutor.onScreenOpened(screen);
         }

         if (!this.autism$replacingScreen && screen != null) {
            if (aD.init_Z() && screen instanceof PauseScreen pauseScreen && pauseScreen.showsPauseMenu()) {
               this.autism$setScreen(new StealthPauseScreen());
               ci.cancel();
            } else if (screen instanceof TitleScreen) {
               if (!aD.init_Z() && !AutismLiteVariant.enabled() && !PoolEntry.init()) {
                  this.autism$setScreen(new JsonUtil());
                  ci.cancel();
               } else if (aD.init_Z()) {
                  this.autism$setScreen(new StealthTitleScreen());
                  ci.cancel();
               } else if (!AutismLiteVariant.enabled() && CustomMainMenuGuard.init()) {
                  this.autism$setScreen(new AutismTitleScreen());
                  ci.cancel();
               }
            }
         }
      }
   }

   @Unique
   private void autism$setScreen(Screen screen) {
      this.autism$replacingScreen = true;

      try {
         this.minecraft.gui.setScreen(screen);
      } finally {
         this.autism$replacingScreen = false;
      }
   }

   @WrapOperation(
      method = "extractRenderState",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;applyCursor(Lcom/mojang/blaze3d/platform/Window;)V")
   )
   private void autism$renderNotificationsLast(GuiGraphicsExtractor graphics, Window window, Operation<Void> original) {
      if (bi.init_Z()) {
         graphics.nextStratum();
         cA.init_V_GuiGraphicsExtract(graphics);

         try {
            bi.init(graphics);
         } catch (RuntimeException error) {
            if (!this.autism$toastFailureLogged) {
               this.autism$toastFailureLogged = true;
               AutismClientAddon.LOG.warn("[UI] Notification render failed", error);
            }
         } finally {
            cA.load_V_GuiGraphicsExtract(graphics);
         }
      }

      original.call(graphics, window);
   }
}
