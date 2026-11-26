package autismclient.mixin;

import autismclient.AutismClientAddon;
import autismclient.modules.AutoReconnectController;
import autismclient.util.AutismLiteVariant;
import autismclient.util.bV;
import autismclient.util.cx;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class AutismConnectScreenMixin {
   @ModifyExpressionValue(
      method = "startConnecting",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;")
   )
   private static Screen autism$connectionBehindEditor(Screen foreground) {
      return cx.init_Screen(Minecraft.getInstance());
   }

   @Inject(method = "startConnecting", at = @At("HEAD"))
   private static void autism$rememberConnectAttempt(
      Screen parent, Minecraft minecraft, ServerAddress hostAndPort, ServerData data, boolean isQuickPlay, TransferState transferState, CallbackInfo ci
   ) {
      AutoReconnectController.init(data, hostAndPort);
      if (!AutismLiteVariant.enabled()) {
         try {
            bV.init_bV().init_V_ServerAddress_ServerData(hostAndPort, data);
         } catch (Throwable t) {
            AutismClientAddon.LOG.error("Profiles: failed to apply on-connect profile", t);
         }
      }
   }
}
