package autismclient.mixin;

import autismclient.c.DisconnectedScreenDuck;
import autismclient.gui.d.StreamGate;
import autismclient.gui.f.b.CachePool;
import autismclient.modules.AutoReconnectController;
import autismclient.modules.aD;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class AutismDisconnectedScreenMixin extends Screen implements DisconnectedScreenDuck {
   @Shadow
   @Final
   private Screen parent;
   @Shadow
   @Final
   private DisconnectionDetails details;

   protected AutismDisconnectedScreenMixin(Component title) {
      super(title);
   }

   @Override
   public Screen autism$parent() {
      return this.parent;
   }

   @Override
   public String autism$reason() {
      return this.details != null && this.details.reason() != null ? this.details.reason().getString() : "";
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void autism$addReconnectButton(CallbackInfo ci) {
      if (!aD.init_Z() && AutoReconnectController.read()) {
         int w = 148;
         this.addRenderableWidget(
            new StreamGate(
               this.width / 2 - w / 2,
               this.height - 38,
               w,
               20,
               Component.literal("Auto Reconnect"),
               CachePool.Inner_DataUtil.read,
               AutoReconnectController::init_Str,
               button -> AutoReconnectController.init_V()
            )
         );
      }
   }
}
