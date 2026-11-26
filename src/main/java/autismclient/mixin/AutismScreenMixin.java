package autismclient.mixin;

import autismclient.c.ExternalButtonScreenDuck;
import autismclient.e.TableIndex;
import autismclient.gui.f.b.DataPipe;
import autismclient.mixin.accessor.AutismScreenAccessor;
import autismclient.modules.PoolEntry;
import autismclient.modules.aD;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismPacketLoggerOverlay;
import autismclient.util.AutismQueueEditorOverlay;
import autismclient.util.aH;
import autismclient.util.aS;
import autismclient.util.bk;
import autismclient.util.cA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class AutismScreenMixin {
   @Unique
   private static final Minecraft MC = Minecraft.getInstance();
   @Unique
   private boolean autism$lecternInitialized;
   @Unique
   private AutismQueueEditorOverlay autism$queueEditorOverlay;
   @Unique
   private AutismPacketLoggerOverlay autism$packetLoggerOverlay;

   @Inject(method = "init(II)V", at = @At("TAIL"))
   private void autism$onScreenInitStartTextInput(int width, int height, CallbackInfo ci) {
      Screen screen = (Screen)(Object)this;
      if (screen.getClass().getName().startsWith("autismclient.") && MC != null) {
         MC.textInputManager().startTextInput(screen);
      }
   }

   @Inject(method = "init()V", at = @At("TAIL"))
   private void autism$onInit(CallbackInfo ci) {
      Screen screen = (Screen)(Object)this;
      if (screen instanceof LecternScreen && this.autism$isModuleActive()) {
         if (this.autism$lecternInitialized) {
            if (this.autism$queueEditorOverlay != null) {
               this.autism$queueEditorOverlay.sync_V_17();
            }

            if (this.autism$queueEditorOverlay != null) {
               bk.init_bk().init_V(this.autism$queueEditorOverlay);
            }
         } else {
            Font textRenderer = ((AutismScreenAccessor)(Object)this).getFont();
            this.autism$queueEditorOverlay = new AutismQueueEditorOverlay(textRenderer);
            this.autism$queueEditorOverlay.sync_V_17();
            bk.init_bk().init_V(this.autism$queueEditorOverlay);
            this.autism$lecternInitialized = true;
         }
      }
   }

   @Inject(method = "extractRenderState", at = @At("TAIL"))
   private void autism$render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      Screen screen = (Screen)(Object)this;
      if (screen instanceof ExternalButtonScreenDuck externalButtonScreen) {
         externalButtonScreen.autism$renderExternalButtons(context, mouseX, mouseY, delta);
      }

      if (screen instanceof LecternScreen && MC.player != null && this.autism$isModuleActive()) {
         Font textRenderer = ((AutismScreenAccessor)(Object)this).getFont();
         if (textRenderer != null) {
            AbstractContainerMenu handler = MC.player.containerMenu;
            if (handler != null) {
               int virtualMouseX = cA.init_I_D(mouseX);
               int virtualMouseY = cA.init_I_D(mouseY);
               cA.init_V_GuiGraphicsExtract(context);

               try {
                  for (DataPipe button : aH.init(MC, this.autism$queueEditorOverlay)) {
                     button.init(context, textRenderer, virtualMouseX, virtualMouseY);
                  }
               } finally {
                  cA.load_V_GuiGraphicsExtract(context);
               }
            }
         }
      }
   }

   @Inject(method = "extractRenderState", at = @At("TAIL"))
   private void autism$renderMenuMacroProgress(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (aS.init_Z_Minecraft(MC) && (AutismLiteVariant.enabled() || TableIndex.init())) {
         context.nextStratum();
         cA.init_V_GuiGraphicsExtract(context);

         try {
            aS.init_V_GuiGraphicsExtract(context, (Screen)(Object)this, mouseX, mouseY);
         } catch (Throwable var10) {
         } finally {
            cA.load_V_GuiGraphicsExtract(context);
         }
      }
   }

   @Inject(method = "onClose", at = @At("HEAD"))
   private void autism$onClose(CallbackInfo ci) {
      AutismConfig.enqueuePendingSaveNow();
      Screen screen = (Screen)(Object)this;
      if (screen.getClass().getName().startsWith("autismclient.") && MC != null) {
         MC.textInputManager().stopTextInput(screen);
      }

      if (screen instanceof LecternScreen) {
         bk.init_bk().read_V(this.autism$queueEditorOverlay);
         bk.init_bk().read_V(this.autism$packetLoggerOverlay);
      }
   }

   @Inject(method = "isInputCaptured", at = @At("HEAD"), cancellable = true)
   private void autism$enableSdlTextInputForCustomScreens(CallbackInfoReturnable<Boolean> cir) {
      Screen screen = (Screen)(Object)this;
      if (screen.getClass().getName().startsWith("autismclient.")) {
         cir.setReturnValue(true);
      }
   }

   @Unique
   private boolean autism$isModuleActive() {
      if (aD.read_Z()) {
         return false;
      }

      PoolEntry module = PoolEntry.init_PoolEntry();
      return module != null && module.init_Z();
   }
}
