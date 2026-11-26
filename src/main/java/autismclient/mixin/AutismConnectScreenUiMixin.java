package autismclient.mixin;

import autismclient.gui.d.StyledButton;
import autismclient.gui.f.b.CachePool;
import autismclient.modules.AutoReconnectController;
import autismclient.modules.aD;
import autismclient.util.ThemePaletteManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class AutismConnectScreenUiMixin extends Screen {
   private static final String[] STEP_KEYS = new String[]{
      "connect.connecting", "connect.authorizing", "connect.encrypting", "connect.negotiating", "connect.joining", null
   };
   private static final String[] STEP_LABELS = new String[]{"Connecting", "Authorizing", "Encrypting", "Negotiating", "Joining World", "Loading Terrain"};
   private static final int DONE_COLOR = -7697782;
   private static final int UPCOMING_COLOR = -11908534;
   private static final char[] SPINNER = new char[]{'|', '/', '-', '\\'};
   private int autism$step;
   @Unique
   private StyledButton autism$reconnectToggle;

   protected AutismConnectScreenUiMixin(Component title) {
      super(title);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void autism$addReconnectToggle(CallbackInfo ci) {
      if (!aD.init_Z() && AutoReconnectController.read()) {
         int w = 148;
         int h = 16;
         this.autism$reconnectToggle = new StyledButton(
            this.width / 2 - w / 2,
            this.height - 18 - h,
            w,
            h,
            Component.literal("Auto Reconnect"),
            CachePool.Inner_DataUtil.read,
            AutoReconnectController::init_Str,
            button -> AutoReconnectController.init_V()
         );
         this.addRenderableWidget(this.autism$reconnectToggle);
      }
   }

   @Inject(method = "extractRenderState", at = @At("HEAD"))
   private void autism$hideToggleInPanic(CallbackInfo ci) {
      if (this.autism$reconnectToggle != null) {
         boolean show = !aD.init_Z();
         this.autism$reconnectToggle.visible = show;
         this.autism$reconnectToggle.active = show;
      }
   }

   @Inject(method = "updateStatus", at = @At("HEAD"))
   private void autism$trackStep(Component status, CallbackInfo ci) {
      if (status.getContents() instanceof TranslatableContents translatable) {
         String var6 = translatable.getKey();

         for (int i = 0; i < STEP_KEYS.length; i++) {
            if (STEP_KEYS[i] != null && STEP_KEYS[i].equals(var6)) {
               if (i > this.autism$step) {
                  this.autism$step = i;
               }

               return;
            }
         }
      }
   }

   @Redirect(
      method = "extractRenderState",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;centeredText(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
      )
   )
   private void autism$renderJoinSteps(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color) {
      if (aD.init_Z()) {
         graphics.centeredText(font, text, x, y, color);
      } else {
         int accent = ThemePaletteManager.init_I_I_ct_DataUtil(-50373, ThemePaletteManager.Inner_DataUtil.ACCENT) & 16777215;
         int rowHeight = 12;
         int top = y - STEP_LABELS.length * rowHeight / 2 + 1;
         char spin = SPINNER[(int)(System.currentTimeMillis() / 100L % SPINNER.length)];

         for (int i = 0; i < STEP_LABELS.length; i++) {
            String label;
            int rgb;
            if (i < this.autism$step) {
               label = STEP_LABELS[i];
               rgb = -7697782;
            } else if (i == this.autism$step) {
               label = spin + " " + STEP_LABELS[i];
               rgb = 0xFF000000 | accent;
            } else {
               label = STEP_LABELS[i];
               rgb = -11908534;
            }

            graphics.centeredText(font, Component.literal(label), x, top + i * rowHeight, rgb);
         }
      }
   }
}
